/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cortex.bridge;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.net.Specifics;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.magic.runtime.CortexDiagnostics;
import org.miaixz.bus.cortex.magic.runtime.CortexLifecycle;
import org.miaixz.bus.cortex.magic.runtime.DiagnosticsSnapshot;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Callout;
import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.magic.event.CortexChangeLogStore;
import org.miaixz.bus.cortex.magic.event.CortexChangeRecord;
import org.miaixz.bus.cortex.magic.event.CortexChangeStatus;
import org.miaixz.bus.cortex.registry.RegistryChange;
import org.miaixz.bus.cortex.registry.api.ApiAssets;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;

/**
 * Asynchronous bridge that consumes registry change events and forwards them to Vortex.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class VortexBridge
        implements Listener<RegistryChange<ApiAssets>>, AutoCloseable, CortexLifecycle, CortexDiagnostics {

    /**
     * Outbox domain used for Vortex bridge delivery records.
     */
    public static final String OUTBOX_DOMAIN = "bridge";
    /**
     * Outbox resource type used for registry synchronization delivery records.
     */
    public static final String OUTBOX_RESOURCE_TYPE = "VORTEX_REGISTRY_SYNC";

    /**
     * Base gateway URL used for registry sync callbacks.
     */
    private final String syncUrl;
    /**
     * Event source identity.
     */
    private final String source;
    /**
     * Maximum number of delivery retries for a single sync event.
     */
    private final int maxRetries;
    /**
     * Delivery timeout in milliseconds.
     */
    private final long timeoutMs;
    /**
     * Optional reliable outbox used as the source of truth for sync delivery.
     */
    private final CortexChangeLogStore changeLogStore;
    /**
     * Owner name used when claiming outbox records.
     */
    private final String outboxOwner;
    /**
     * Bounded queue holding pending registry sync events.
     */
    private final LinkedBlockingQueue<RegistryChange<Assets>> queue = new LinkedBlockingQueue<>(10000);
    /**
     * Whether the background worker should keep processing events.
     */
    private volatile boolean running = true;
    /**
     * Dropped event count caused by queue overflow or exhausted retries.
     */
    private final AtomicLong droppedCount = new AtomicLong();
    /**
     * Delivered event count.
     */
    private final AtomicLong deliveredCount = new AtomicLong();
    /**
     * Failed delivery attempt count.
     */
    private final AtomicLong failedAttemptCount = new AtomicLong();
    /**
     * Outbox records claimed by this bridge worker.
     */
    private final AtomicLong claimedCount = new AtomicLong();
    /**
     * Outbox records moved to dead letter by this bridge worker.
     */
    private final AtomicLong deadCount = new AtomicLong();
    /**
     * Last delivery failure message.
     */
    private volatile String lastError;
    /**
     * Background worker thread responsible for delivering sync events.
     */
    private final Thread workerThread;

    /**
     * Creates a VortexBridge and starts the background sync worker.
     *
     * @param syncUrl    base URL of the gateway's sync endpoint
     * @param maxRetries maximum delivery attempts per event before dropping
     */
    public VortexBridge(String syncUrl, int maxRetries) {
        this(syncUrl, maxRetries, "cortex");
    }

    /**
     * Creates a VortexBridge and starts the background sync worker.
     *
     * @param syncUrl    base URL of the gateway's sync endpoint
     * @param maxRetries maximum delivery attempts per event before dropping
     * @param source     event source identifier
     */
    public VortexBridge(String syncUrl, int maxRetries, String source) {
        this(syncUrl, maxRetries, source, Builder.DEFAULT_HEALTH_TIMEOUT_MS);
    }

    /**
     * Creates a VortexBridge and starts the background sync worker.
     *
     * @param syncUrl    base URL of the gateway's sync endpoint
     * @param maxRetries maximum delivery attempts per event before dropping
     * @param source     event source identifier
     * @param timeoutMs  delivery timeout in milliseconds
     */
    public VortexBridge(String syncUrl, int maxRetries, String source, long timeoutMs) {
        this(syncUrl, maxRetries, source, timeoutMs, null);
    }

    /**
     * Creates a VortexBridge and starts the background sync worker.
     *
     * @param syncUrl        base URL of the gateway's sync endpoint
     * @param maxRetries     maximum delivery attempts per event before dropping
     * @param source         event source identifier
     * @param timeoutMs      delivery timeout in milliseconds
     * @param changeLogStore optional reliable outbox store
     */
    public VortexBridge(String syncUrl, int maxRetries, String source, long timeoutMs,
            CortexChangeLogStore changeLogStore) {
        this.syncUrl = syncUrl;
        this.maxRetries = Math.max(1, maxRetries);
        this.source = source;
        this.timeoutMs = Math.max(timeoutMs, 1L);
        this.changeLogStore = changeLogStore;
        this.outboxOwner = "vortex-bridge-" + Integer.toHexString(System.identityHashCode(this));
        this.workerThread = new Thread(this::run, "cortex-vortex-bridge");
        this.workerThread.setDaemon(true);
        this.workerThread.start();
    }

    /**
     * Converts a registry change into a transport event and enqueues it for delivery.
     *
     * @param event registry change event to forward
     */
    @Override
    public void onEvent(RegistryChange<ApiAssets> event) {
        if (event == null || event.getAction() == null || event.getAsset() == null) {
            return;
        }
        RegistryChange<Assets> transportEvent = new RegistryChange<>();
        transportEvent.setAction(event.getAction());
        transportEvent.setNamespace_id(event.getNamespace_id());
        transportEvent.setType(event.getType());
        transportEvent.setId(event.getId());
        transportEvent.setMethod(event.getMethod());
        transportEvent.setVersion(event.getVersion());
        transportEvent.setFingerprint(event.getFingerprint());
        transportEvent.setSource(source);
        transportEvent.setSequence(event.getSequence());
        transportEvent.setInstance(event.getInstance());
        transportEvent.setAsset(toGatewayAsset(event.getAsset(), event.getInstance()));
        if (changeLogStore != null) {
            try {
                changeLogStore.append(toOutboxRecord(transportEvent));
                signalWorker(transportEvent);
                return;
            } catch (Exception e) {
                failedAttemptCount.incrementAndGet();
                lastError = e.getMessage();
                Logger.warn(
                        "VortexBridge failed to append outbox record, falling back to local queue: {}",
                        e.getMessage());
            }
        }
        if (!queue.offer(transportEvent)) {
            droppedCount.incrementAndGet();
            Logger.warn(
                    "VortexBridge queue full, dropping {} event for: {}/{}",
                    event.getAction(),
                    event.getMethod(),
                    event.getVersion());
        }
    }

    /**
     * Stops the background sync worker.
     */
    public void stop() {
        running = false;
        workerThread.interrupt();
        try {
            workerThread.join(TimeUnit.SECONDS.toMillis(5));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Closes the bridge by stopping the background sync worker.
     */
    @Override
    public void close() {
        stop();
    }

    /**
     * Drains queued sync events and posts them to the vortex internal sync endpoint.
     */
    private void run() {
        while (running || !queue.isEmpty()) {
            try {
                if (drainOutbox()) {
                    continue;
                }
                RegistryChange<Assets> event = queue.poll(1, TimeUnit.SECONDS);
                if (event == null) {
                    continue;
                }
                if (changeLogStore != null) {
                    continue;
                }
                deliverQueued(event);
            } catch (InterruptedException e) {
                if (running) {
                    continue;
                }
            } catch (Exception e) {
                lastError = e.getMessage();
                Logger.warn("VortexBridge worker error: {}", e.getMessage());
            }
        }
    }

    /**
     * Claims and delivers pending outbox records for the bridge domain.
     *
     * @return {@code true} when at least one record was processed
     */
    private boolean drainOutbox() {
        if (changeLogStore == null) {
            return false;
        }
        List<CortexChangeRecord> records = changeLogStore.claimPending(
                32,
                outboxOwner,
                timeoutMs * Math.max(2L, maxRetries),
                OUTBOX_DOMAIN,
                OUTBOX_RESOURCE_TYPE);
        if (records == null || records.isEmpty()) {
            return false;
        }
        claimedCount.addAndGet(records.size());
        for (CortexChangeRecord record : records) {
            deliverRecord(record);
        }
        return true;
    }

    /**
     * Delivers one locally queued event with retry handling.
     *
     * @param event queued registry change event
     * @throws InterruptedException when retry sleeping is interrupted
     */
    private void deliverQueued(RegistryChange<Assets> event) throws InterruptedException {
        String json = JsonKit.toJsonString(event);
        int attempt = 0;
        while (attempt < maxRetries) {
            DeliveryResult result = deliverJson(json);
            if (result.success()) {
                deliveredCount.incrementAndGet();
                return;
            }
            failedAttemptCount.incrementAndGet();
            lastError = result.error();
            if (!result.retryable()) {
                droppedCount.incrementAndGet();
                Logger.warn("VortexBridge dropped non-retryable response: {}", result.error());
                return;
            }
            attempt++;
            if (attempt >= maxRetries) {
                droppedCount.incrementAndGet();
                Logger.warn("VortexBridge failed to sync after {} retries: {}", maxRetries, result.error());
            } else {
                TimeUnit.MILLISECONDS.sleep(Math.min(1000L, 100L * attempt));
            }
        }
    }

    /**
     * Delivers one reliable outbox record and updates its delivery state.
     *
     * @param record outbox record
     */
    private void deliverRecord(CortexChangeRecord record) {
        DeliveryResult result = deliverJson(record.getPayload());
        if (result.success()) {
            deliveredCount.incrementAndGet();
            changeLogStore.markDelivered(record.getId());
            return;
        }
        failedAttemptCount.incrementAndGet();
        lastError = result.error();
        CortexChangeRecord updated = result.retryable() ? changeLogStore.markFailed(record.getId(), result.error())
                : changeLogStore.deadLetter(record.getId(), result.error());
        if (updated != null && updated.getStatus() == CortexChangeStatus.DEAD) {
            deadCount.incrementAndGet();
            droppedCount.incrementAndGet();
        }
    }

    /**
     * Posts a JSON payload to the internal Vortex sync endpoint.
     *
     * @param json serialized registry change event
     * @return delivery result
     */
    private DeliveryResult deliverJson(String json) {
        String url = syncUrl + Specifics.MAPPING_REGISTRY + Specifics.MAPPING_PUSH;
        try {
            Callout.Response response = Callout.postJson(url, json, timeoutMs);
            if (response.errorMessage() != null) {
                return new DeliveryResult(false, true, response.errorMessage());
            }
            if (response.isSuccessful()) {
                return new DeliveryResult(true, false, null);
            }
            String error = "HTTP " + response.statusCode();
            return new DeliveryResult(false, shouldRetry(response.statusCode()), error);
        } catch (Exception e) {
            return new DeliveryResult(false, true, e.getMessage());
        }
    }

    /**
     * Returns the number of events dropped by the bridge.
     *
     * @return dropped event count
     */
    public long getDroppedCount() {
        return droppedCount.get();
    }

    /**
     * Returns the number of successfully delivered events.
     *
     * @return delivered event count
     */
    public long getDeliveredCount() {
        return deliveredCount.get();
    }

    /**
     * Returns the number of failed delivery attempts.
     *
     * @return failed attempt count
     */
    public long getFailedAttemptCount() {
        return failedAttemptCount.get();
    }

    /**
     * Returns the number of outbox records claimed by the bridge.
     *
     * @return claimed outbox count
     */
    public long getClaimedCount() {
        return claimedCount.get();
    }

    /**
     * Returns the number of outbox records moved to dead letter.
     *
     * @return dead-letter count
     */
    public long getDeadCount() {
        return deadCount.get();
    }

    /**
     * Returns the last delivery error.
     *
     * @return last error message
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Validates that the bridge is still started.
     */
    @Override
    public void start() {
        if (!running) {
            throw new IllegalStateException("VortexBridge cannot be restarted after stop");
        }
    }

    /**
     * Returns whether the background bridge worker is running.
     *
     * @return {@code true} when the bridge worker is alive
     */
    @Override
    public boolean isRunning() {
        return running && workerThread.isAlive();
    }

    /**
     * Returns current bridge diagnostics.
     *
     * @return diagnostics snapshot
     */
    @Override
    public DiagnosticsSnapshot diagnostics() {
        DiagnosticsSnapshot snapshot = new DiagnosticsSnapshot();
        snapshot.setComponent("vortex-bridge");
        snapshot.setStatus(isRunning() ? "running" : "stopped");
        snapshot.setMetrics(
                Map.of(
                        "queueSize",
                        queue.size(),
                        "claimed",
                        claimedCount.get(),
                        "delivered",
                        deliveredCount.get(),
                        "dropped",
                        droppedCount.get(),
                        "failedAttempt",
                        failedAttemptCount.get(),
                        "dead",
                        deadCount.get(),
                        "outbox",
                        changeLogStore != null));
        snapshot.setLastError(lastError);
        snapshot.setUpdatedAt(System.currentTimeMillis());
        return snapshot;
    }

    /**
     * Converts one API service change payload into the gateway-facing asset snapshot.
     *
     * @param service  API service definition
     * @param instance optional runtime instance
     * @return gateway-facing asset snapshot
     */
    private Assets toGatewayAsset(ApiAssets service, Instance instance) {
        if (service == null) {
            return null;
        }
        if (instance != null) {
            return ApiAssetsConverter.convert(service, instance);
        }
        return service;
    }

    /**
     * Returns whether the given HTTP status should trigger a retry attempt.
     *
     * @param status HTTP status code
     * @return {@code true} when delivery should be retried
     */
    private boolean shouldRetry(int status) {
        return status == 408 || status == 429 || status >= 500;
    }

    /**
     * Converts one registry change into a reliable outbox record.
     *
     * @param event registry change event
     * @return outbox record
     */
    private CortexChangeRecord toOutboxRecord(RegistryChange<Assets> event) {
        CortexChangeRecord record = new CortexChangeRecord();
        record.setDomain(OUTBOX_DOMAIN);
        record.setAction(event.getAction() == null ? "sync" : event.getAction().name().toLowerCase());
        record.setResourceType(OUTBOX_RESOURCE_TYPE);
        record.setResourceId(resourceId(event));
        record.setNamespace_id(event.getNamespace_id());
        record.setPayload(JsonKit.toJsonString(event));
        record.setSequence(event.getSequence());
        record.setIdempotencyKey(
                "vortex:" + event.getNamespace_id() + ":" + record.getAction() + ":" + record.getResourceId());
        return record;
    }

    /**
     * Builds a stable resource identifier for bridge delivery records.
     *
     * @param event registry change event
     * @return resource identifier
     */
    private String resourceId(RegistryChange<Assets> event) {
        if (event.getFingerprint() != null) {
            return "instance:" + event.getFingerprint();
        }
        if (event.getId() != null) {
            return event.getId();
        }
        return (event.getMethod() == null ? "" : event.getMethod()) + ":"
                + (event.getVersion() == null ? "" : event.getVersion());
    }

    /**
     * Wakes the worker after a reliable outbox append.
     *
     * @param event registry change event used as a wakeup marker
     */
    private void signalWorker(RegistryChange<Assets> event) {
        if (!queue.offer(event)) {
            Logger.warn("VortexBridge wakeup queue full; outbox record will be picked up by polling");
        }
    }

    /**
     * Delivery outcome for a single HTTP sync attempt.
     *
     * @param success   whether the delivery completed successfully
     * @param retryable whether the failure may be retried
     * @param error     delivery error message
     */
    private record DeliveryResult(boolean success, boolean retryable, String error) {
    }

}
