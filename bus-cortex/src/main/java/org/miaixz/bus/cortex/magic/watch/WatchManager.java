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
package org.miaixz.bus.cortex.magic.watch;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.basic.entity.Tenant;
import org.miaixz.bus.core.center.date.Chrono;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.cortex.*;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.builtin.MetadataMatcher;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.magic.identity.Sequence;
import org.miaixz.bus.cortex.magic.runtime.CortexDiagnostics;
import org.miaixz.bus.cortex.magic.runtime.CortexLifecycle;
import org.miaixz.bus.cortex.magic.runtime.DiagnosticsSnapshot;
import org.miaixz.bus.cortex.registry.RegistryChange;
import org.miaixz.bus.cortex.registry.RegistryIdentity;
import org.miaixz.bus.logger.Logger;

/**
 * Manages watch subscriptions and notifies listeners on changes.
 *
 * @author Kimi Liu
 */
public class WatchManager implements AutoCloseable, CortexLifecycle, CortexDiagnostics {

    /**
     * Default source name for setting-domain watch events.
     */
    public static final String SETTING_SOURCE = "setting-center";

    /**
     * Default event type for setting updates.
     */
    public static final String SETTING_UPDATE_EVENT = "setting-update";

    /**
     * Default event type for setting deletes.
     */
    public static final String SETTING_DELETE_EVENT = "setting-delete";

    /**
     * Default source name for registry-domain watch events.
     */
    public static final String REGISTRY_SOURCE = "registry-center";

    /**
     * Event type for registry asset registrations.
     */
    public static final String REGISTRY_REGISTER_EVENT = "registry-register";

    /**
     * Event type for registry asset updates.
     */
    public static final String REGISTRY_UPDATE_EVENT = "registry-update";

    /**
     * Event type for registry asset deregistrations.
     */
    public static final String REGISTRY_DEREGISTER_EVENT = "registry-deregister";

    /**
     * Event type for runtime instance availability.
     */
    public static final String REGISTRY_INSTANCE_UP_EVENT = "registry-instance-up";

    /**
     * Event type for runtime instance removal.
     */
    public static final String REGISTRY_INSTANCE_DOWN_EVENT = "registry-instance-down";

    /**
     * Event type for runtime instance health state changes.
     */
    public static final String REGISTRY_INSTANCE_HEALTH_CHANGE_EVENT = "registry-instance-health-change";

    /**
     * Watch entries keyed by generated watch identifier.
     */
    private final Map<String, WatchSubscription<?>> entries = new ConcurrentHashMap<>();

    /**
     * Maximum number of watch subscriptions per setting tenant or global API partition.
     */
    private final int maxWatchesPerTenant;

    /**
     * Maximum idle time in milliseconds before a watch subscription expires.
     */
    private final long watchExpireMs;

    /**
     * Maximum number of pending events allowed for one watch subscription.
     */
    private final int maxPendingPerWatch;

    /**
     * Strategy applied when one subscription exceeds its pending backlog limit.
     */
    private final OverflowStrategy overflowStrategy;

    /**
     * Shared sequence source used for ordered watch notifications.
     */
    private final Sequence sequence;

    /**
     * Partition watch counters enforcing setting-tenant and global-registry limits without full scans.
     */
    private final Map<String, AtomicInteger> tenantIdCounts = new ConcurrentHashMap<>();

    /**
     * Global watch listeners used for diagnostics and side-channel observation.
     */
    private final List<Listener<Watch<Object>>> globalListeners = new CopyOnWriteArrayList<>();

    /**
     * Shared asynchronous dispatcher used to isolate listener failures from publisher threads.
     */
    private final ExecutorService dispatcher = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Whether this manager has been closed.
     */
    private volatile boolean closed;

    /**
     * Creates a WatchManager with default limits (1000 watches per tenantId, 24 h expiry).
     *
     * @param cacheX shared cache used by the backing sequence generator
     */
    public WatchManager(CacheX<String, Object> cacheX) {
        this(cacheX, Normal._1000, Chrono.DAY.getMillis(), Normal._1024, OverflowStrategy.DROP_LATEST);
    }

    /**
     * Creates a WatchManager with explicit limits.
     *
     * @param cacheX              shared cache used by the backing sequence generator
     * @param maxWatchesPerTenant maximum concurrent watches per tenantId
     * @param watchExpireMs       milliseconds of inactivity after which a watch is removed
     */
    public WatchManager(CacheX<String, Object> cacheX, int maxWatchesPerTenant, long watchExpireMs) {
        this(cacheX, maxWatchesPerTenant, watchExpireMs, Normal._1024, OverflowStrategy.DROP_LATEST);
    }

    /**
     * Creates a WatchManager with explicit limits and backlog capacity.
     *
     * @param cacheX              shared cache used by the backing sequence generator
     * @param maxWatchesPerTenant maximum concurrent watches per tenantId
     * @param watchExpireMs       milliseconds of inactivity after which a watch is removed
     * @param maxPendingPerWatch  maximum queued-but-not-delivered events per watch
     */
    public WatchManager(CacheX<String, Object> cacheX, int maxWatchesPerTenant, long watchExpireMs,
            int maxPendingPerWatch) {
        this(cacheX, maxWatchesPerTenant, watchExpireMs, maxPendingPerWatch, OverflowStrategy.DROP_LATEST);
    }

    /**
     * Creates a WatchManager with explicit limits, backlog capacity and overflow strategy.
     *
     * @param cacheX              shared cache used by the backing sequence generator
     * @param maxWatchesPerTenant maximum concurrent watches per tenantId
     * @param watchExpireMs       milliseconds of inactivity after which a watch is removed
     * @param maxPendingPerWatch  maximum queued-but-not-delivered events per watch
     * @param overflowStrategy    overflow strategy when the backlog is full
     */
    public WatchManager(CacheX<String, Object> cacheX, int maxWatchesPerTenant, long watchExpireMs,
            int maxPendingPerWatch, OverflowStrategy overflowStrategy) {
        this.maxWatchesPerTenant = maxWatchesPerTenant;
        this.watchExpireMs = watchExpireMs;
        this.maxPendingPerWatch = maxPendingPerWatch;
        this.overflowStrategy = overflowStrategy == null ? OverflowStrategy.DROP_LATEST : overflowStrategy;
        this.sequence = new Sequence(cacheX);
    }

    /**
     * Adds a watch subscription and returns its unique ID. Registration and manager shutdown are serialized on this
     * manager so a closed dispatcher never retains a newly accepted subscription.
     *
     * @param vector   vector defining what to watch
     * @param <T>      watched type
     * @param listener listener to notify on change
     * @return watch identifier
     * @throws IllegalStateException if the manager is closed or the tenant watch limit has been reached
     */
    public synchronized <T> String add(Vector vector, Listener<Watch<T>> listener) {
        if (closed) {
            throw new IllegalStateException("WatchManager is closed");
        }
        Objects.requireNonNull(listener, "watch listener must not be null");
        Vector normalized = normalizeVector(vector);
        String resolvedTenant = tenantIdOf(normalized);
        tenantIdCounts.compute(partitionKey(resolvedTenant), (key, counter) -> {
            AtomicInteger current = counter == null ? new AtomicInteger() : counter;
            if (current.get() >= maxWatchesPerTenant) {
                throw new IllegalStateException("Max watches per tenantId exceeded: " + resolvedTenant);
            }
            current.incrementAndGet();
            return current;
        });
        String watchId = ID.fastSimpleUUID();
        long now = DateKit.current();
        WatchSubscription<T> entry = new WatchSubscription<>(normalized, resolvedTenant, listener, now);
        entry.touch(now, watchExpireMs);
        entries.put(watchId, entry);
        return watchId;
    }

    /**
     * Removes a watch subscription by ID.
     *
     * @param watchId watch identifier to remove
     */
    public void remove(String watchId) {
        WatchSubscription<?> removed = entries.remove(watchId);
        if (removed != null) {
            decrementTenantCount(removed.getTenant_id());
        }
    }

    /**
     * Adds one global listener that observes every emitted watch event. Registration shares the manager shutdown
     * boundary so no listener can be retained after closure.
     *
     * @param listener global watch listener
     * @throws IllegalStateException if the manager is closed
     */
    public synchronized void addGlobalListener(Listener<Watch<Object>> listener) {
        if (closed) {
            throw new IllegalStateException("WatchManager is closed");
        }
        if (listener != null) {
            globalListeners.add(listener);
        }
    }

    /**
     * Removes one global listener.
     *
     * @param listener global watch listener
     */
    public void removeGlobalListener(Listener<Watch<Object>> listener) {
        if (listener != null) {
            globalListeners.remove(listener);
        }
    }

    /**
     * Notifies matching watch listeners about a value change.
     *
     * @param key      changed key
     * @param <T>      value type
     * @param newValue new value
     */
    public <T> void notify(String key, T newValue) {
        notifyTyped(key, newValue, "update", "WatchManager", "Generic watch update");
    }

    /**
     * Notifies matching generic watch listeners with explicit event metadata.
     *
     * @param key       changed logical key
     * @param <T>       payload type
     * @param newValue  new payload value
     * @param eventType logical event type
     * @param source    event source component
     * @param summary   event summary
     * @throws IllegalArgumentException if the notification does not carry a tenant identifier
     */
    public <T> void notifyTyped(String key, T newValue, String eventType, String source, String summary) {
        if (closed) {
            return;
        }
        String tenantId;
        if (newValue instanceof Assets value) {
            RegistryIdentity.normalize(value, RegistryIdentity.type(value));
            tenantId = value.getTenant_id();
        } else if (newValue instanceof Tenant value) {
            tenantId = CortexIdentity.tenantId(value.getTenant_id());
        } else {
            throw new IllegalArgumentException("Watch payload must carry tenant_id");
        }
        for (Map.Entry<String, WatchSubscription<?>> item : entries.entrySet()) {
            if (!Objects.equals(tenantId, item.getValue().getTenant_id())) {
                continue;
            }
            Vector vector = item.getValue().getVector();
            if (!matches(vector, key, newValue)) {
                continue;
            }
            long now = DateKit.current();
            item.getValue().touch(now, watchExpireMs);
            WatchSubscription<T> typed = (WatchSubscription<T>) item.getValue();
            Watch<T> event = createEvent(
                    item.getKey(),
                    typed,
                    eventType == null || eventType.isBlank() ? "update" : eventType,
                    source == null || source.isBlank() ? "WatchManager" : source,
                    now);
            if (newValue instanceof Assets asset) {
                event.setTenant_id(asset.getTenant_id());
                event.setType(Type.tryFromKey(asset.getType()).orElse(event.getType()));
            }
            event.setUpdated(newValue != null ? List.of(newValue) : List.of());
            event.setSummary(summary == null || summary.isBlank() ? "Generic watch update" : summary);
            dispatch(item.getKey(), typed, event);
        }
    }

    /**
     * Notifies setting-specific watch listeners.
     *
     * @param tenantId the explicit tenant owning the configuration watch
     * @param key      setting key that changed
     * @param content  new setting content
     */
    public void notifySetting(String tenantId, String key, String content) {
        notifySetting(
                tenantId,
                key,
                content,
                SETTING_SOURCE,
                content == null ? SETTING_DELETE_EVENT : SETTING_UPDATE_EVENT,
                content == null ? "Setting removed" : "Setting updated");
    }

    /**
     * Notifies setting-content listeners without delivering strings to explicitly typed runtime-watch subscriptions.
     *
     * @param tenantId  strict setting tenant identifier
     * @param key       setting key that changed
     * @param content   new setting content
     * @param source    logical source component
     * @param eventType logical event type
     * @param summary   human-readable event summary
     */
    public void notifySetting(
            String tenantId,
            String key,
            String content,
            String source,
            String eventType,
            String summary) {
        String tenant = CortexIdentity.tenantId(tenantId);
        if (closed) {
            return;
        }
        for (Map.Entry<String, WatchSubscription<?>> item : entries.entrySet()) {
            if (!Objects.equals(tenant, item.getValue().getTenant_id())) {
                continue;
            }
            Vector vector = item.getValue().getVector();
            if (vector != null && vector.getType() != null && !Objects.equals(vector.getType(), Type.ITEM.getKey())) {
                continue;
            }
            String watchKey = vector != null ? vector.getId() : null;
            if (!matchesSettingKey(key, watchKey)) {
                continue;
            }
            long now = DateKit.current();
            item.getValue().touch(now, watchExpireMs);
            WatchSubscription<String> typed = (WatchSubscription<String>) item.getValue();
            Watch<String> event = createEvent(
                    item.getKey(),
                    typed,
                    eventType == null || eventType.isBlank()
                            ? content == null ? SETTING_DELETE_EVENT : SETTING_UPDATE_EVENT
                            : eventType,
                    source == null || source.isBlank() ? SETTING_SOURCE : source,
                    now);
            event.setUpdated(content != null ? List.of(content) : List.of());
            event.setRemoved(content == null ? List.of(key) : List.of());
            event.setSummary(
                    summary == null || summary.isBlank() ? content == null ? "Setting removed" : "Setting updated"
                            : summary);
            dispatch(item.getKey(), typed, event);
        }
    }

    /**
     * Notifies registry watch listeners about a committed registry mutation.
     *
     * @param action           registry mutation action
     * @param <T>              asset type
     * @param asset            current asset snapshot
     * @param previousAsset    previous asset snapshot
     * @param instance         current service relationship
     * @param previousInstance previous service relationship
     */
    public <T extends Assets> void notifyRegistry(
            RegistryChange.Action action,
            T asset,
            T previousAsset,
            Instance instance,
            Instance previousInstance) {
        notifyRegistry(null, action, asset, previousAsset, instance, previousInstance);
    }

    /**
     * Notifies registry watch listeners with an explicit logical event type.
     *
     * @param eventType        logical event type override
     * @param <T>              asset type
     * @param action           registry mutation action
     * @param asset            current asset snapshot
     * @param previousAsset    previous asset snapshot
     * @param instance         current service relationship
     * @param previousInstance previous service relationship
     */
    public <T extends Assets> void notifyRegistry(
            String eventType,
            RegistryChange.Action action,
            T asset,
            T previousAsset,
            Instance instance,
            Instance previousInstance) {
        if (closed || action == null || asset == null) {
            return;
        }
        RegistryIdentity.normalize(asset, RegistryIdentity.type(asset));
        if (previousAsset != null) {
            RegistryIdentity
                    .requireTenant(RegistryIdentity.type(asset), asset.getTenant_id(), previousAsset.getTenant_id());
            RegistryIdentity.normalize(previousAsset, RegistryIdentity.type(previousAsset));
        }
        String effectiveEventType = eventType == null || eventType.isBlank()
                ? registryEventType(action, instance, previousInstance)
                : eventType;
        Type assetType = Type.tryFromKey(asset.getType()).orElse(null);
        String matchKey = registryWatchKey(asset, instance, previousInstance);
        for (Map.Entry<String, WatchSubscription<?>> item : entries.entrySet()) {
            Vector vector = item.getValue().getVector();
            if (!Objects.equals(item.getValue().getTenant_id(), asset.getTenant_id())
                    || !matchesRegistry(vector, matchKey, asset, previousAsset, instance, previousInstance)) {
                continue;
            }
            long now = DateKit.current();
            item.getValue().touch(now, watchExpireMs);
            WatchSubscription<T> typed = (WatchSubscription<T>) item.getValue();
            Watch<T> event = createEvent(item.getKey(), typed, effectiveEventType, REGISTRY_SOURCE, now);
            event.setTenant_id(asset.getTenant_id());
            event.setType(assetType);
            fillRegistryPayload(event, action, effectiveEventType, asset, previousAsset);
            event.setSummary(registrySummary(effectiveEventType));
            dispatch(item.getKey(), typed, event);
        }
    }

    /**
     * Removes stale watch entries that have not been accessed recently. Only the successful remover releases a
     * subscription's quota, including when explicit removal or another cleanup runs concurrently.
     */
    public void cleanExpired() {
        long now = DateKit.current();
        entries.forEach((id, subscription) -> {
            if (subscription.getExpiresAt() > 0L && now > subscription.getExpiresAt()
                    && entries.remove(id, subscription)) {
                decrementTenantCount(subscription.getTenant_id());
            }
        });
    }

    /**
     * Returns an unmodifiable view of all watch entries.
     *
     * @return unmodifiable entry map
     */
    public Map<String, WatchSubscription<?>> entries() {
        return Collections.unmodifiableMap(entries);
    }

    /**
     * Stops asynchronous dispatching and clears all live subscriptions.
     */
    public void shutdown() {
        close();
    }

    /**
     * Stops asynchronous dispatching and clears all live subscriptions.
     */
    @Override
    public void stop() {
        close();
    }

    /**
     * Returns whether this watch manager accepts and dispatches watch events.
     *
     * @return {@code true} while the manager is open
     */
    @Override
    public boolean isRunning() {
        return !closed;
    }

    /**
     * Returns current watch-manager diagnostics.
     *
     * @return diagnostics snapshot
     */
    @Override
    public DiagnosticsSnapshot diagnostics() {
        DiagnosticsSnapshot snapshot = new DiagnosticsSnapshot();
        snapshot.setComponent("watch");
        snapshot.setStatus(closed ? "stopped" : "running");
        snapshot.setMetrics(
                Map.of(
                        "entries",
                        entries.size(),
                        "tenants",
                        tenantIdCounts.size(),
                        "dropped",
                        entries.values().stream().mapToLong(WatchSubscription::getDroppedCount).sum(),
                        "failed",
                        entries.values().stream().mapToLong(WatchSubscription::getFailureCount).sum()));
        snapshot.setUpdatedAt(DateKit.current());
        return snapshot;
    }

    /**
     * Stops asynchronous dispatching and clears all live subscriptions. The manager monitor serializes shutdown with
     * subscription and global-listener registration.
     */
    @Override
    public synchronized void close() {
        closed = true;
        entries.clear();
        tenantIdCounts.clear();
        globalListeners.clear();
        dispatcher.shutdownNow();
    }

    /**
     * Returns whether one emitted setting key matches the watch selector key.
     *
     * @param key      emitted setting key
     * @param watchKey watch selector key
     * @return {@code true} when the emitted key matches
     */
    private boolean matchesSettingKey(String key, String watchKey) {
        if (watchKey == null || watchKey.isBlank()) {
            return true;
        }
        if (key == null || key.isBlank()) {
            return false;
        }
        if (watchKey.equals(key)) {
            return true;
        }
        if (key.startsWith(watchKey + Symbol.COLON)) {
            return true;
        }
        return false;
    }

    /**
     * Returns whether one emitted value matches the vector selector attached to a subscription.
     *
     * @param vector watch selector
     * @param key    emitted key
     * @param value  emitted value
     * @return {@code true} when the value should be delivered
     */
    private boolean matches(Vector vector, String key, Object value) {
        if (vector == null) {
            return !(WatchProjection.runtime(value));
        }
        if (value instanceof Assets asset) {
            if (vector.getTenant_id() != null && !Objects.equals(vector.getTenant_id(), asset.getTenant_id())) {
                return false;
            }
            if (vector.getApp_id() != null && !Objects.equals(vector.getApp_id(), asset.getApp_id())) {
                return false;
            }
            if (vector.getType() != null && !Objects.equals(vector.getType(), asset.getType())) {
                return false;
            }
            if (vector.getId() != null && !Objects.equals(vector.getId(), asset.getId())) {
                return false;
            }
            if (vector.getMethod() != null && !Objects.equals(vector.getMethod(), asset.getMethod())) {
                return false;
            }
            if (vector.getVersion() != null && !Objects.equals(vector.getVersion(), asset.getVersion())) {
                return false;
            }
            if (!MetadataMatcher.matches(asset, vector.getLabels(), vector.getSelectors())) {
                return false;
            }
            return true;
        }
        if (value instanceof Instance instance) {
            return (vector.getType() == null || vector.getType() == Type.INSTANCE.key())
                    && (vector.getTenant_id() == null || Objects.equals(vector.getTenant_id(), instance.getTenant_id()))
                    && (vector.getApp_id() == null || Objects.equals(vector.getApp_id(), instance.getApp_id()))
                    && (vector.getId() == null || Objects.equals(vector.getId(), instance.getId())
                            || Objects.equals(vector.getId(), instance.getChecksum()))
                    && vector.getMethod() == null && vector.getVersion() == null
                    && (vector.getState() == null || Objects.equals(vector.getState(), instance.getState()))
                    && MetadataMatcher.matches(instance.getLabels(), vector.getLabels(), vector.getSelectors());
        }
        if (WatchProjection.runtime(value)) {
            return WatchProjection.permits(vector, value)
                    && (vector.getId() == null || matchesSettingKey(key, vector.getId()));
        }
        if (vector.getId() == null) {
            return true;
        }
        return matchesSettingKey(key, vector.getId());
    }

    /**
     * Allocates the next monotonic watch sequence for the supplied logical stream key.
     *
     * @param key logical sequence stream
     * @return next sequence number
     */
    private long nextSequence(String key) {
        return sequence.next(key);
    }

    /**
     * Builds the base watch event envelope before payloads are attached. Dispatch assigns ordering metadata when the
     * completed event enters the subscription's serialized enqueue path.
     *
     * @param watchId      generated watch identifier
     * @param <T>          watched payload type
     * @param subscription receiving subscription
     * @param eventType    logical event type
     * @param source       event source component
     * @param now          event timestamp in epoch milliseconds
     * @return initialized watch event envelope
     */
    private <T> Watch<T> createEvent(
            String watchId,
            WatchSubscription<T> subscription,
            String eventType,
            String source,
            long now) {
        Watch<T> event = new Watch<>();
        event.setWatchId(watchId);
        event.setVector(subscription.getVector());
        event.setTenant_id(subscription.getTenant_id());
        event.setType(
                subscription.getVector() == null ? null
                        : Type.tryFromKey(subscription.getVector().getType()).orElse(null));
        event.setAdded(List.of());
        event.setRemoved(List.of());
        event.setUpdated(List.of());
        event.setEventType(eventType);
        event.setSource(source);
        event.setTimestamp(now);
        return event;
    }

    /**
     * Dispatches one emitted event to global diagnostic listeners.
     *
     * @param event emitted watch event
     * @param <T>   watched payload type
     */
    private <T> void dispatchGlobal(Watch<T> event) {
        for (Listener<Watch<Object>> listener : globalListeners) {
            try {
                listener.onEvent((Watch<Object>) event);
            } catch (Exception e) {
                listener.onError((Watch<Object>) event, e);
                Logger.warn(false, "Cortex", "Global watch listener execution failed: {}", e.getMessage());
            }
        }
    }

    /**
     * Assigns ordering metadata and enqueues one completed event while holding only its subscription monitor. Listener
     * callbacks, including global listeners, execute asynchronously outside that monitor. All event types share one
     * tenant-scoped sequence, so mixed subscriptions cannot move backwards. Dropped events still advance the emitted
     * sequence so existing gap detection remains unchanged.
     *
     * @param watchId      watch identifier
     * @param <T>          watched payload type
     * @param subscription watch subscription
     * @param event        emitted event
     */
    private <T> void dispatch(String watchId, WatchSubscription<T> subscription, Watch<T> event) {
        synchronized (subscription) {
            long sequence = nextSequence(partitionKey(subscription.getTenant_id()) + ":watch");
            event.setPreviousSequence(subscription.getLastSequence());
            subscription.setLastSequence(sequence);
            event.setSequence(sequence);
            long now = DateKit.current();
            if (!subscription.reservePending(maxPendingPerWatch)) {
                handleOverflow(watchId, subscription, event, now);
                return;
            }
            try {
                subscription.enqueueDispatch(() -> {
                    try {
                        subscription.setDispatchCount(subscription.getDispatchCount() + 1);
                        long deliveredAt = DateKit.current();
                        subscription.setLastEventAt(deliveredAt);
                        subscription.getListener().onEvent(event);
                    } catch (Exception e) {
                        subscription.setFailureCount(subscription.getFailureCount() + 1);
                        subscription.setLastError(e.getMessage());
                        event.setErrorMessage(e.getMessage());
                        try {
                            subscription.getListener().onError(event, e);
                        } catch (Exception errorHandlerFailure) {
                            Logger.warn(
                                    false,
                                    "Cortex",
                                    "Watch listener error handler failed {}: {}",
                                    watchId,
                                    errorHandlerFailure.getMessage());
                        }
                        Logger.warn(false, "Cortex", "Watch listener execution failed {}: {}", watchId, e.getMessage());
                    } finally {
                        subscription.completeDelivery(DateKit.current());
                    }
                    dispatchGlobal(event);
                }, dispatcher);
            } catch (RejectedExecutionException e) {
                subscription.releasePending();
                subscription.recordDrop(now, e.getMessage());
                event.setErrorMessage(e.getMessage());
                Logger.warn(false, "Cortex", "Watch dispatch rejected {}: {}", watchId, e.getMessage());
            }
        }
    }

    /**
     * Records one dropped event caused by subscription backlog overflow.
     *
     * @param watchId      watch identifier
     * @param <T>          watched payload type
     * @param subscription watch subscription
     * @param event        dropped event
     * @param now          current timestamp
     */
    private <T> void handleOverflow(String watchId, WatchSubscription<T> subscription, Watch<T> event, long now) {
        String reason = "Watch backlog limit exceeded";
        subscription.recordDrop(now, reason);
        event.setErrorMessage(reason);
        if (overflowStrategy == OverflowStrategy.DROP_LATEST) {
            Logger.warn(false, "Cortex", "Watch event dropped {}: {}", watchId, reason);
        }
    }

    /**
     * Copies and normalizes the selector vector used by one subscription.
     *
     * @param vector incoming watch vector
     * @return copied vector with a strict non-API tenant or global API scope
     */
    private Vector normalizeVector(Vector vector) {
        Vector copy = new Vector();
        if (vector == null) {
            copy.setTenant_id(RegistryIdentity.tenantId(Type.API, null));
            return copy;
        }
        copy.setId(vector.getId());
        copy.setTenant_id(tenantIdOf(vector));
        copy.setType(vector.getType());
        copy.setApp_id(vector.getApp_id());
        copy.setMethod(vector.getMethod());
        copy.setVersion(vector.getVersion());
        copy.setLabels(vector.getLabels() == null ? null : new LinkedHashMap<>(vector.getLabels()));
        copy.setSelectors(vector.getSelectors() == null ? null : new ArrayList<>(vector.getSelectors()));
        copy.setState(vector.getState());
        copy.setPurpose(vector.getPurpose());
        copy.setWatch(vector.isWatch());
        copy.setRefresh(vector.isRefresh());
        copy.setRequestId(vector.getRequestId());
        copy.setIncludeDisabled(vector.isIncludeDisabled());
        copy.setLimit(vector.getLimit());
        copy.setOffset(vector.getOffset());
        return copy;
    }

    /**
     * Resolves a strict non-API tenant or the global API scope of one vector.
     *
     * @param vector watch vector
     * @return explicit MCP, Prompt or setting tenant, or null for global API
     */
    private String tenantIdOf(Vector vector) {
        Type type = vector == null || vector.getType() == null ? null : Type.tryFromKey(vector.getType()).orElse(null);
        if (type != null && type.isRegistry()) {
            return RegistryIdentity.tenantId(type, vector.getTenant_id());
        }
        if (vector == null || type == null && vector.getTenant_id() == null) {
            return RegistryIdentity.tenantId(Type.API, null);
        }
        return CortexIdentity.tenantId(vector.getTenant_id());
    }

    /**
     * Returns whether one registry change should be delivered to the supplied selector.
     *
     * @param vector           watch selector
     * @param key              fallback watch key
     * @param asset            current asset
     * @param previousAsset    previous asset
     * @param instance         current service relationship
     * @param previousInstance previous service relationship
     * @return {@code true} when the change matches
     */
    private boolean matchesRegistry(
            Vector vector,
            String key,
            Assets asset,
            Assets previousAsset,
            Instance instance,
            Instance previousInstance) {
        if (instance != null || previousInstance != null) {
            return instance != null && matchesServiceInstance(vector, asset, instance) || previousInstance != null
                    && matchesServiceInstance(vector, previousAsset == null ? asset : previousAsset, previousInstance);
        }
        return matches(vector, key, asset) || previousAsset != null && matches(vector, key, previousAsset);
    }

    /**
     * Matches a shared process using service route coordinates supplied by the event asset.
     *
     * @param vector   subscription selector
     * @param asset    exact service definition context
     * @param instance shared runtime process
     * @return whether service and process constraints all match
     */
    private boolean matchesServiceInstance(Vector vector, Assets asset, Instance instance) {
        if (vector == null) {
            return true;
        }
        return (vector.getType() == null || Objects.equals(vector.getType(), asset.getType()))
                && (vector.getTenant_id() == null || asset.getType() == Type.API.key()
                        || Objects.equals(vector.getTenant_id(), asset.getTenant_id()))
                && (vector.getApp_id() == null || Objects.equals(vector.getApp_id(), instance.getApp_id()))
                && (vector.getId() == null || Objects.equals(vector.getId(), asset.getId())
                        || Objects.equals(vector.getId(), instance.getId())
                        || Objects.equals(vector.getId(), instance.getChecksum()))
                && (vector.getMethod() == null || Objects.equals(vector.getMethod(), asset.getMethod()))
                && (vector.getVersion() == null || Objects.equals(vector.getVersion(), asset.getVersion()))
                && (vector.getState() == null || Objects.equals(vector.getState(), instance.getState()))
                && MetadataMatcher.matches(asset, vector.getLabels(), vector.getSelectors());
    }

    /**
     * Resolves a logical registry watch event type from registry action and instance state.
     *
     * @param action           registry action
     * @param instance         current service relationship
     * @param previousInstance previous service relationship
     * @return event type
     */
    private String registryEventType(RegistryChange.Action action, Instance instance, Instance previousInstance) {
        if (instance != null) {
            return REGISTRY_INSTANCE_UP_EVENT;
        }
        if (action == RegistryChange.Action.DEREGISTER && previousInstance != null) {
            return REGISTRY_INSTANCE_DOWN_EVENT;
        }
        return switch (action) {
            case REGISTER -> REGISTRY_REGISTER_EVENT;
            case UPDATE -> REGISTRY_UPDATE_EVENT;
            case DEREGISTER -> REGISTRY_DEREGISTER_EVENT;
        };
    }

    /**
     * Attaches registry-domain payload lists using asset snapshots.
     *
     * @param event         watch event to fill
     * @param <T>           asset type
     * @param action        registry action
     * @param eventType     resolved event type
     * @param asset         current asset
     * @param previousAsset previous asset
     */
    private <T extends Assets> void fillRegistryPayload(
            Watch<T> event,
            RegistryChange.Action action,
            String eventType,
            T asset,
            T previousAsset) {
        if (REGISTRY_INSTANCE_UP_EVENT.equals(eventType) || REGISTRY_INSTANCE_DOWN_EVENT.equals(eventType)
                || REGISTRY_INSTANCE_HEALTH_CHANGE_EVENT.equals(eventType)) {
            event.setUpdated(List.of(asset));
            return;
        }
        switch (action) {
            case REGISTER -> event.setAdded(List.of(asset));
            case UPDATE -> event.setUpdated(List.of(asset));
            case DEREGISTER -> event.setRemoved(List.of(previousAsset != null ? previousAsset : asset));
        }
    }

    /**
     * Builds a compact registry watch key for fallback string selectors.
     *
     * @param asset            current asset
     * @param instance         current service relationship
     * @param previousInstance previous service relationship
     * @return logical watch key
     */
    private String registryWatchKey(Assets asset, Instance instance, Instance previousInstance) {
        if (asset != null && asset.getId() != null) {
            return asset.getId();
        }
        Instance runtime = instance != null ? instance : previousInstance;
        if (runtime != null && runtime.getChecksum() != null) {
            return runtime.getChecksum();
        }
        return null;
    }

    /**
     * Returns a short human-readable summary for registry watch events.
     *
     * @param eventType event type
     * @return event summary
     */
    private String registrySummary(String eventType) {
        return switch (eventType) {
            case REGISTRY_REGISTER_EVENT -> "Registry asset registered";
            case REGISTRY_UPDATE_EVENT -> "Registry asset updated";
            case REGISTRY_DEREGISTER_EVENT -> "Registry asset deregistered";
            case REGISTRY_INSTANCE_UP_EVENT -> "Registry instance available";
            case REGISTRY_INSTANCE_DOWN_EVENT -> "Registry instance removed";
            case REGISTRY_INSTANCE_HEALTH_CHANGE_EVENT -> "Registry instance health changed";
            default -> "Registry changed";
        };
    }

    /**
     * Separates global registry counters and sequences from setting-domain tenant partitions.
     *
     * @param tenantId null for global registry subscriptions, otherwise an explicit setting tenant
     * @return an internal cache/counter partition key, never a business tenant identifier
     */
    private String partitionKey(String tenantId) {
        return tenantId == null ? "registry" : CortexIdentity.tenantKey(tenantId);
    }

    /**
     * Atomically decrements the tenantId watch counter and removes it only while still empty, so concurrent
     * registration cannot retain a detached counter.
     *
     * @param tenantId tenantId key
     */
    private void decrementTenantCount(String tenantId) {
        tenantIdCounts.computeIfPresent(
                partitionKey(tenantId),
                (key, counter) -> counter.decrementAndGet() <= 0 ? null : counter);
    }

    /**
     * Backpressure policy applied when one watch subscription is slower than the emitted event rate.
     *
     * @author Kimi Liu
     */
    public enum OverflowStrategy {
        /**
         * Drops the newest event when the per-watch backlog is already full.
         */
        DROP_LATEST

    }

}
