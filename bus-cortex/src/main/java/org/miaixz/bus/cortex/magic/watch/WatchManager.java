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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.magic.runtime.CortexDiagnostics;
import org.miaixz.bus.cortex.magic.runtime.CortexLifecycle;
import org.miaixz.bus.cortex.magic.runtime.DiagnosticsSnapshot;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.Watch;
import org.miaixz.bus.cortex.builtin.MetadataMatcher;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.cortex.magic.identity.Sequence;
import org.miaixz.bus.cortex.registry.RegistryChange;
import org.miaixz.bus.logger.Logger;

/**
 * Manages watch subscriptions and notifies listeners on changes.
 *
 * @author Kimi Liu
 * @since Java 21+
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
     * Backpressure policy applied when one watch subscription is slower than the emitted event rate.
     */
    public enum OverflowStrategy {
        /**
         * Drops the newest event when the per-watch backlog is already full.
         */
        DROP_LATEST
    }

    /**
     * Watch entries keyed by generated watch identifier.
     */
    private final Map<String, WatchSubscription<?>> entries = new ConcurrentHashMap<>();
    /**
     * Maximum number of watch subscriptions allowed per namespace.
     */
    private final int maxWatchesPerNamespace;
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
     * Namespace watch counters used to enforce per-namespace subscription limits without full scans.
     */
    private final Map<String, AtomicInteger> namespaceCounts = new ConcurrentHashMap<>();
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
     * Creates a WatchManager with default limits (1000 watches per namespace, 24 h expiry).
     *
     * @param cacheX shared cache used by the backing sequence generator
     */
    public WatchManager(CacheX<String, Object> cacheX) {
        this(cacheX, 1000, 86400000L, 1024, OverflowStrategy.DROP_LATEST);
    }

    /**
     * Creates a WatchManager with explicit limits.
     *
     * @param cacheX                 shared cache used by the backing sequence generator
     * @param maxWatchesPerNamespace maximum concurrent watches per namespace
     * @param watchExpireMs          milliseconds of inactivity after which a watch is removed
     */
    public WatchManager(CacheX<String, Object> cacheX, int maxWatchesPerNamespace, long watchExpireMs) {
        this(cacheX, maxWatchesPerNamespace, watchExpireMs, 1024, OverflowStrategy.DROP_LATEST);
    }

    /**
     * Creates a WatchManager with explicit limits and backlog capacity.
     *
     * @param cacheX                 shared cache used by the backing sequence generator
     * @param maxWatchesPerNamespace maximum concurrent watches per namespace
     * @param watchExpireMs          milliseconds of inactivity after which a watch is removed
     * @param maxPendingPerWatch     maximum queued-but-not-delivered events per watch
     */
    public WatchManager(CacheX<String, Object> cacheX, int maxWatchesPerNamespace, long watchExpireMs,
            int maxPendingPerWatch) {
        this(cacheX, maxWatchesPerNamespace, watchExpireMs, maxPendingPerWatch, OverflowStrategy.DROP_LATEST);
    }

    /**
     * Creates a WatchManager with explicit limits, backlog capacity and overflow strategy.
     *
     * @param cacheX                 shared cache used by the backing sequence generator
     * @param maxWatchesPerNamespace maximum concurrent watches per namespace
     * @param watchExpireMs          milliseconds of inactivity after which a watch is removed
     * @param maxPendingPerWatch     maximum queued-but-not-delivered events per watch
     * @param overflowStrategy       overflow strategy when the backlog is full
     */
    public WatchManager(CacheX<String, Object> cacheX, int maxWatchesPerNamespace, long watchExpireMs,
            int maxPendingPerWatch, OverflowStrategy overflowStrategy) {
        this.maxWatchesPerNamespace = maxWatchesPerNamespace;
        this.watchExpireMs = watchExpireMs;
        this.maxPendingPerWatch = maxPendingPerWatch;
        this.overflowStrategy = overflowStrategy == null ? OverflowStrategy.DROP_LATEST : overflowStrategy;
        this.sequence = new Sequence(cacheX);
    }

    /**
     * Adds a watch subscription and returns its unique ID.
     *
     * @param vector   vector defining what to watch
     * @param listener listener to notify on change
     * @param <T>      watched type
     * @return watch identifier
     */
    public <T> String add(Vector vector, Listener<Watch<T>> listener) {
        if (closed) {
            throw new IllegalStateException("WatchManager is closed");
        }
        Objects.requireNonNull(listener, "watch listener must not be null");
        Vector normalized = normalizeVector(vector);
        String ns = namespaceOf(normalized);
        AtomicInteger counter = namespaceCounts.computeIfAbsent(ns, key -> new AtomicInteger());
        if (counter.incrementAndGet() > maxWatchesPerNamespace) {
            counter.decrementAndGet();
            throw new IllegalStateException("Max watches per namespace exceeded: " + ns);
        }
        String watchId = ID.fastSimpleUUID();
        long now = System.currentTimeMillis();
        WatchSubscription<T> entry = new WatchSubscription<>(normalized, ns, listener, now);
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
            decrementNamespaceCount(removed.getNamespace_id());
        }
    }

    /**
     * Adds one global listener that observes every emitted watch event.
     *
     * @param listener global watch listener
     */
    public void addGlobalListener(Listener<Watch<Object>> listener) {
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
     * @param newValue new value
     * @param <T>      value type
     */
    public <T> void notify(String key, T newValue) {
        notifyTyped(key, newValue, "update", "watch:generic", "WatchManager", "Generic watch update");
    }

    /**
     * Notifies matching generic watch listeners with explicit event metadata.
     *
     * @param key         changed logical key
     * @param newValue    new payload value
     * @param eventType   logical event type
     * @param sequenceKey logical sequence stream key
     * @param source      event source component
     * @param summary     event summary
     * @param <T>         payload type
     */
    public <T> void notifyTyped(
            String key,
            T newValue,
            String eventType,
            String sequenceKey,
            String source,
            String summary) {
        if (closed) {
            return;
        }
        for (Map.Entry<String, WatchSubscription<?>> item : entries.entrySet()) {
            Vector vector = item.getValue().getVector();
            if (!matches(vector, key, newValue)) {
                continue;
            }
            long now = System.currentTimeMillis();
            item.getValue().touch(now, watchExpireMs);
            WatchSubscription<T> typed = (WatchSubscription<T>) item.getValue();
            Watch<T> event = createEvent(
                    item.getKey(),
                    typed,
                    eventType == null || eventType.isBlank() ? "update" : eventType,
                    sequenceKey == null || sequenceKey.isBlank() ? "watch:generic" : sequenceKey,
                    source == null || source.isBlank() ? "WatchManager" : source,
                    now);
            if (newValue instanceof Assets asset) {
                event.setNamespace_id(asset.getNamespace_id());
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
     * @param key     setting key that changed
     * @param content new setting content
     */
    public void notifySetting(String key, String content) {
        notifySetting(
                key,
                content,
                SETTING_SOURCE,
                content == null ? SETTING_DELETE_EVENT : SETTING_UPDATE_EVENT,
                content == null ? "Setting removed" : "Setting updated");
    }

    /**
     * Notifies setting-specific watch listeners with explicit source and event typing metadata.
     *
     * @param key       setting key that changed
     * @param content   new setting content
     * @param source    logical source component
     * @param eventType logical event type
     * @param summary   human-readable event summary
     */
    public void notifySetting(String key, String content, String source, String eventType, String summary) {
        if (closed) {
            return;
        }
        for (Map.Entry<String, WatchSubscription<?>> item : entries.entrySet()) {
            Vector vector = item.getValue().getVector();
            String watchKey = vector != null ? vector.getId() : null;
            if (!matchesSettingKey(key, watchKey)) {
                continue;
            }
            long now = System.currentTimeMillis();
            item.getValue().touch(now, watchExpireMs);
            WatchSubscription<String> typed = (WatchSubscription<String>) item.getValue();
            Watch<String> event = createEvent(
                    item.getKey(),
                    typed,
                    eventType == null || eventType.isBlank()
                            ? content == null ? SETTING_DELETE_EVENT : SETTING_UPDATE_EVENT
                            : eventType,
                    "watch:setting",
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
     * @param asset            current asset snapshot
     * @param previousAsset    previous asset snapshot
     * @param instance         current runtime instance snapshot
     * @param previousInstance previous runtime instance snapshot
     * @param <T>              asset type
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
     * @param action           registry mutation action
     * @param asset            current asset snapshot
     * @param previousAsset    previous asset snapshot
     * @param instance         current runtime instance snapshot
     * @param previousInstance previous runtime instance snapshot
     * @param <T>              asset type
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
        Instance currentRuntime = runtimeSnapshot(asset, instance);
        Instance previousRuntime = runtimeSnapshot(asset, previousInstance);
        String effectiveEventType = eventType == null || eventType.isBlank()
                ? registryEventType(action, currentRuntime, previousRuntime)
                : eventType;
        Type assetType = Type.tryFromKey(asset.getType()).orElse(null);
        String sequenceKey = "watch:registry:" + (assetType == null ? "unknown" : assetType.name().toLowerCase());
        String matchKey = registryWatchKey(asset, currentRuntime, previousRuntime);
        for (Map.Entry<String, WatchSubscription<?>> item : entries.entrySet()) {
            Vector vector = item.getValue().getVector();
            if (!matchesRegistry(vector, matchKey, asset, previousAsset, currentRuntime, previousRuntime)) {
                continue;
            }
            long now = System.currentTimeMillis();
            item.getValue().touch(now, watchExpireMs);
            WatchSubscription<T> typed = (WatchSubscription<T>) item.getValue();
            Watch<T> event = createEvent(item.getKey(), typed, effectiveEventType, sequenceKey, REGISTRY_SOURCE, now);
            event.setNamespace_id(asset.getNamespace_id());
            event.setType(assetType);
            fillRegistryPayload(event, action, effectiveEventType, asset, previousAsset);
            event.setSummary(registrySummary(effectiveEventType));
            dispatch(item.getKey(), typed, event);
        }
    }

    /**
     * Removes stale watch entries that have not been accessed recently.
     */
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        entries.entrySet().removeIf(e -> {
            boolean expired = e.getValue().getExpiresAt() > 0L && now > e.getValue().getExpiresAt();
            if (expired) {
                decrementNamespaceCount(e.getValue().getNamespace_id());
            }
            return expired;
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
                        "namespaces",
                        namespaceCounts.size(),
                        "dropped",
                        entries.values().stream().mapToLong(WatchSubscription::getDroppedCount).sum(),
                        "failed",
                        entries.values().stream().mapToLong(WatchSubscription::getFailureCount).sum()));
        snapshot.setUpdatedAt(System.currentTimeMillis());
        return snapshot;
    }

    /**
     * Stops asynchronous dispatching and clears all live subscriptions.
     */
    @Override
    public void close() {
        closed = true;
        entries.clear();
        namespaceCounts.clear();
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
        if (key.startsWith(watchKey + ":")) {
            return true;
        }
        return key.endsWith(":" + watchKey) || key.contains(":" + watchKey + ":");
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
            return true;
        }
        if (value instanceof Assets asset) {
            if (vector.getNamespace_id() != null
                    && !Objects.equals(vector.getNamespace_id(), asset.getNamespace_id())) {
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
            if (vector.getNamespace_id() != null
                    && !Objects.equals(vector.getNamespace_id(), instance.getNamespace_id())) {
                return false;
            }
            if (vector.getId() != null && !Objects.equals(vector.getId(), instance.getServiceId())
                    && !Objects.equals(vector.getId(), instance.getFingerprint())) {
                return false;
            }
            if (vector.getMethod() != null && !Objects.equals(vector.getMethod(), instance.getMethod())) {
                return false;
            }
            if (vector.getVersion() != null && !Objects.equals(vector.getVersion(), instance.getVersion())) {
                return false;
            }
            if (vector.getState() != null && !Objects.equals(vector.getState(), instance.getState())) {
                return false;
            }
            return true;
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
     * Builds the base watch event envelope before added, removed, or updated payloads are attached.
     *
     * @param watchId      generated watch identifier
     * @param subscription receiving subscription
     * @param eventType    logical event type
     * @param sequenceKey  logical stream key used for ordered sequencing
     * @param source       event source component
     * @param now          event timestamp in epoch milliseconds
     * @param <T>          watched payload type
     * @return initialized watch event envelope
     */
    private <T> Watch<T> createEvent(
            String watchId,
            WatchSubscription<T> subscription,
            String eventType,
            String sequenceKey,
            String source,
            long now) {
        Watch<T> event = new Watch<>();
        long sequence = nextSequence(sequenceKey);
        long previousSequence = subscription.getLastSequence();
        subscription.setLastSequence(sequence);
        event.setWatchId(watchId);
        event.setVector(subscription.getVector());
        event.setNamespace_id(subscription.getNamespace_id());
        event.setType(
                subscription.getVector() == null ? null
                        : Type.tryFromKey(subscription.getVector().getType()).orElse(null));
        event.setAdded(List.of());
        event.setRemoved(List.of());
        event.setUpdated(List.of());
        event.setEventType(eventType);
        event.setSource(source);
        event.setPreviousSequence(previousSequence);
        event.setSequence(sequence);
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
                Logger.warn("Global watch listener execution failed: {}", e.getMessage());
            }
        }
    }

    /**
     * Dispatches one watch event asynchronously and records listener execution state.
     *
     * @param watchId      watch identifier
     * @param subscription watch subscription
     * @param event        emitted event
     * @param <T>          watched payload type
     */
    private <T> void dispatch(String watchId, WatchSubscription<T> subscription, Watch<T> event) {
        long now = System.currentTimeMillis();
        if (!subscription.reservePending(maxPendingPerWatch)) {
            handleOverflow(watchId, subscription, event, now);
            return;
        }
        try {
            subscription.enqueueDispatch(() -> {
                try {
                    subscription.setDispatchCount(subscription.getDispatchCount() + 1);
                    long deliveredAt = System.currentTimeMillis();
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
                                "Watch listener error handler failed [{}]: {}",
                                watchId,
                                errorHandlerFailure.getMessage());
                    }
                    Logger.warn("Watch listener execution failed [{}]: {}", watchId, e.getMessage());
                } finally {
                    subscription.completeDelivery(System.currentTimeMillis());
                }
                dispatchGlobal(event);
            }, dispatcher);
        } catch (RejectedExecutionException e) {
            subscription.releasePending();
            subscription.recordDrop(now, e.getMessage());
            event.setErrorMessage(e.getMessage());
            Logger.warn("Watch dispatch rejected [{}]: {}", watchId, e.getMessage());
        }
    }

    /**
     * Records one dropped event caused by subscription backlog overflow.
     *
     * @param watchId      watch identifier
     * @param subscription watch subscription
     * @param event        dropped event
     * @param now          current timestamp
     * @param <T>          watched payload type
     */
    private <T> void handleOverflow(String watchId, WatchSubscription<T> subscription, Watch<T> event, long now) {
        String reason = "Watch backlog limit exceeded";
        subscription.recordDrop(now, reason);
        event.setErrorMessage(reason);
        if (overflowStrategy == OverflowStrategy.DROP_LATEST) {
            Logger.warn("Watch event dropped [{}]: {}", watchId, reason);
        }
    }

    /**
     * Copies and normalizes the selector vector used by one subscription.
     *
     * @param vector incoming watch vector
     * @return copied vector with canonical namespace
     */
    private Vector normalizeVector(Vector vector) {
        Vector copy = new Vector();
        if (vector == null) {
            copy.setNamespace_id(CortexIdentity.namespace(null));
            return copy;
        }
        copy.setId(vector.getId());
        copy.setNamespace_id(namespaceOf(vector));
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
     * Resolves the canonical namespace of one vector.
     *
     * @param vector watch vector
     * @return namespace key
     */
    private String namespaceOf(Vector vector) {
        return CortexIdentity.namespace(vector == null ? null : vector.getNamespace_id());
    }

    /**
     * Returns whether one registry change should be delivered to the supplied selector.
     *
     * @param vector           watch selector
     * @param key              fallback watch key
     * @param asset            current asset
     * @param previousAsset    previous asset
     * @param instance         current instance
     * @param previousInstance previous instance
     * @return {@code true} when the change matches
     */
    private boolean matchesRegistry(
            Vector vector,
            String key,
            Assets asset,
            Assets previousAsset,
            Instance instance,
            Instance previousInstance) {
        return matches(vector, key, asset) || (previousAsset != null && matches(vector, key, previousAsset))
                || (instance != null && matches(vector, key, instance))
                || (previousInstance != null && matches(vector, key, previousInstance));
    }

    /**
     * Resolves a logical registry watch event type from registry action and instance state.
     *
     * @param action           registry action
     * @param instance         current instance
     * @param previousInstance previous instance
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
     * @param action        registry action
     * @param eventType     resolved event type
     * @param asset         current asset
     * @param previousAsset previous asset
     * @param <T>           asset type
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
     * @param instance         current instance
     * @param previousInstance previous instance
     * @return logical watch key
     */
    private String registryWatchKey(Assets asset, Instance instance, Instance previousInstance) {
        if (asset != null && asset.getId() != null) {
            return asset.getId();
        }
        Instance runtime = instance != null ? instance : previousInstance;
        if (runtime != null && runtime.getFingerprint() != null) {
            return runtime.getFingerprint();
        }
        return null;
    }

    /**
     * Copies an instance snapshot and fills missing identity fields from the owning asset for matching.
     *
     * @param asset    owning asset
     * @param instance runtime instance
     * @return copied runtime instance, or {@code null}
     */
    private Instance runtimeSnapshot(Assets asset, Instance instance) {
        if (instance == null) {
            return null;
        }
        Instance copy = new Instance();
        copy.setNamespace_id(instance.getNamespace_id() != null ? instance.getNamespace_id() : asset.getNamespace_id());
        copy.setApp_id(instance.getApp_id() != null ? instance.getApp_id() : asset.getApp_id());
        copy.setServiceId(instance.getServiceId() != null ? instance.getServiceId() : asset.getId());
        copy.setMethod(instance.getMethod() != null ? instance.getMethod() : asset.getMethod());
        copy.setVersion(instance.getVersion() != null ? instance.getVersion() : asset.getVersion());
        copy.setHost(instance.getHost());
        copy.setPort(instance.getPort());
        copy.setWeight(instance.getWeight());
        copy.setHealthy(instance.getHealthy());
        copy.setState(instance.getState());
        copy.setFingerprint(instance.getFingerprint());
        copy.setPid(instance.getPid());
        copy.setScheme(instance.getScheme());
        copy.setHealthPath(instance.getHealthPath());
        copy.setLeaseSeconds(instance.getLeaseSeconds());
        copy.setLastHeartbeatAt(instance.getLastHeartbeatAt());
        copy.setLastProbeAt(instance.getLastProbeAt());
        copy.setStateChangedAt(instance.getStateChangedAt());
        copy.setHealthSource(instance.getHealthSource());
        copy.setLastStatus(instance.getLastStatus());
        copy.setLabels(instance.getLabels() == null ? null : new LinkedHashMap<>(instance.getLabels()));
        copy.setMetadata(instance.getMetadata() == null ? null : new LinkedHashMap<>(instance.getMetadata()));
        return copy;
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
     * Decrements the namespace watch counter and clears empty counters.
     *
     * @param namespace namespace key
     */
    private void decrementNamespaceCount(String namespace) {
        String ns = CortexIdentity.namespace(namespace);
        AtomicInteger counter = namespaceCounts.get(ns);
        if (counter == null) {
            return;
        }
        if (counter.decrementAndGet() <= 0) {
            namespaceCounts.remove(ns, counter);
        }
    }

}
