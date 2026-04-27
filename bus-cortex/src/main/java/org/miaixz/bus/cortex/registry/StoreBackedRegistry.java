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
package org.miaixz.bus.cortex.registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.*;
import org.miaixz.bus.cortex.magic.identity.Sequence;
import org.miaixz.bus.cortex.magic.event.CortexChangeLogStore;
import org.miaixz.bus.cortex.magic.event.CortexChangeRecord;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;

/**
 * Store-backed registry base that coordinates durable writes, cache warming and change events.
 *
 * @param <T> asset type
 * @author Kimi Liu
 * @since Java 21+
 */
public class StoreBackedRegistry<T extends Assets> extends AbstractRegistry<T> {

    /**
     * Durable storage adapter.
     */
    protected final RegistryStore<T> store;
    /**
     * Post-commit listeners.
     */
    private final List<Listener<RegistryChange<T>>> listeners;
    /**
     * Sequence source used for ordered registry change events.
     */
    private final Sequence sequence;
    /**
     * Optional reliable change log used as a first-stage outbox.
     */
    private final CortexChangeLogStore changeLogStore;

    /**
     * Creates a store-backed registry.
     *
     * @param cacheX       shared cache backend
     * @param watchManager watch manager
     * @param store        durable store adapter
     * @param type         managed entry type
     * @param registryType registry type
     * @param listeners    post-commit listeners
     */
    protected StoreBackedRegistry(CacheX<String, Object> cacheX, WatchManager watchManager, RegistryStore<T> store,
            Class<T> type, Type registryType, List<Listener<RegistryChange<T>>> listeners) {
        this(cacheX, watchManager, store, type, registryType, listeners, null);
    }

    /**
     * Creates a store-backed registry with optional outbox recording.
     *
     * @param cacheX         shared cache backend
     * @param watchManager   watch manager
     * @param store          durable store adapter
     * @param type           managed entry type
     * @param registryType   registry type
     * @param listeners      post-commit listeners
     * @param changeLogStore optional outbox store
     */
    protected StoreBackedRegistry(CacheX<String, Object> cacheX, WatchManager watchManager, RegistryStore<T> store,
            Class<T> type, Type registryType, List<Listener<RegistryChange<T>>> listeners,
            CortexChangeLogStore changeLogStore) {
        super(cacheX, watchManager, type, registryType);
        this.store = store;
        this.listeners = listeners == null ? List.of() : List.copyOf(listeners);
        this.sequence = new Sequence(cacheX);
        this.changeLogStore = changeLogStore;
    }

    /**
     * Finds a single entry by namespace and ID.
     *
     * @param namespace namespace
     * @param id        identifier
     * @return matching entry or {@code null}
     */
    public T find(String namespace, String id) {
        String ns = normalizeNamespace(namespace);
        T cached = deserialize(cacheX.read(buildKey(ns, id)));
        if (cached != null) {
            cached.setNamespace_id(ns);
            if (cached.getType() == null) {
                cached.setType(registryType.key());
            }
            return cached;
        }
        if (store == null) {
            return null;
        }
        T loaded = store.find(registryType, ns, id);
        if (loaded == null) {
            return null;
        }
        T prepared = normalizeEntry(loaded);
        cacheEntry(prepared);
        return prepared;
    }

    /**
     * Reloads a single entry from the durable store into cache without emitting a mutation event.
     *
     * @param namespace entry namespace
     * @param id        entry identifier
     * @return refreshed entry, or {@code null} when the entry no longer exists in the durable store
     */
    public T refresh(String namespace, String id) {
        String ns = normalizeNamespace(namespace);
        if (id == null) {
            return null;
        }
        if (store == null) {
            return deserialize(cacheX.read(buildKey(ns, id)));
        }
        T loaded = store.find(registryType, ns, id);
        if (loaded == null) {
            cacheX.remove(buildKey(ns, id));
            return null;
        }
        T prepared = normalizeEntry(loaded);
        cacheEntry(prepared);
        return prepared;
    }

    /**
     * Reloads a single entry by method and version when the durable store supports that lookup mode.
     *
     * @param namespace namespace containing the entry
     * @param app_id    application identifier
     * @param method    route method
     * @param version   route version
     * @return refreshed entry, or {@code null} when not found
     */
    public T refreshByMethodVersion(String namespace, String app_id, String method, String version) {
        String ns = normalizeNamespace(namespace);
        if (method == null || version == null || store == null) {
            return null;
        }
        if (!storeSupports(Trait.ROUTE_QUERY)) {
            capabilityFallback("refreshByMethodVersion", Trait.ROUTE_QUERY, "cache route scan");
            T cached = findCachedByRoute(ns, app_id, method, version);
            return cached == null ? null : normalizeEntry(cached);
        }
        T loaded = store.findByMethodVersion(registryType, ns, app_id, method, version);
        if (loaded == null) {
            return null;
        }
        T prepared = normalizeEntry(loaded);
        cacheEntry(prepared);
        return prepared;
    }

    /**
     * Removes a cached entry without mutating the durable store.
     *
     * @param namespace entry namespace
     * @param id        entry identifier
     */
    public void evict(String namespace, String id) {
        if (id == null) {
            return;
        }
        cacheX.remove(buildKey(normalizeNamespace(namespace), id));
    }

    /**
     * Refreshes a set of entries from the durable store and returns the reloaded result set.
     *
     * @param vector query criteria
     * @return refreshed entries
     */
    public List<T> refresh(Vector vector) {
        RegistryRefreshScope scope = RegistryScopeMapping.refresh(vector, registryType, "refresh");
        Vector criteria = RegistryScopeMapping.toVector(scope);
        if (criteria.getId() != null) {
            T single = refresh(criteria.getNamespace_id(), criteria.getId());
            return single == null ? List.of() : List.of(single);
        }
        if (store == null || !storeSupports(Trait.QUERY)) {
            if (store != null) {
                capabilityFallback("refresh", Trait.QUERY, "cache query");
            }
            return super.query(criteria);
        }
        List<T> entries = store.query(registryType, criteria);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<T> result = new ArrayList<>(entries.size());
        for (T entry : entries) {
            T prepared = normalizeEntry(entry);
            cacheEntry(prepared);
            result.add(prepared);
        }
        return result;
    }

    /**
     * Rebuilds cached entries for the given scope by evicting current cache state and reloading from the durable store.
     *
     * @param vector query criteria
     * @return reloaded entries
     */
    public List<T> rebuild(Vector vector) {
        RegistryRefreshScope scope = RegistryScopeMapping.refresh(vector, registryType, "rebuild");
        Vector criteria = RegistryScopeMapping.toVector(scope);
        if (store == null || !storeSupports(Trait.DURABLE) || !storeSupports(Trait.QUERY)) {
            if (store != null) {
                capabilityFallback("rebuild", Trait.DURABLE, "cache query");
            }
            return super.query(criteria);
        }
        if (criteria.getId() != null) {
            evict(criteria.getNamespace_id(), criteria.getId());
            return refresh(criteria);
        }
        String prefix = buildScanPrefix(criteria.getNamespace_id());
        Map<String, Object> current = cacheX.scan(prefix);
        if (current != null && !current.isEmpty()) {
            cacheX.remove(current.keySet().toArray(String[]::new));
        }
        return refresh(criteria);
    }

    /**
     * Persists and caches the entry, then emits a registry change event.
     *
     * @param entry entry to create or update
     */
    @Override
    public void register(T entry) {
        if (entry == null) {
            return;
        }
        T prepared = normalizeEntry(entry);
        T existing = loadExisting(prepared);
        persistEntry(prepared, null);
        publishChange(
                existing == null ? RegistryChange.Action.REGISTER : RegistryChange.Action.UPDATE,
                prepared,
                existing,
                null,
                null);
    }

    /**
     * Removes an entry from the durable store and cache, then emits a deregistration event.
     *
     * @param namespace entry namespace
     * @param id        entry identifier
     */
    @Override
    public void deregister(String namespace, String id) {
        String ns = normalizeNamespace(namespace);
        T existing = find(ns, id);
        if (existing == null) {
            cacheX.remove(buildKey(ns, id));
            return;
        }
        long tombstoneTime = System.currentTimeMillis();
        deleteEntry(ns, id);
        existing.setStatus(-1);
        existing.setModified(tombstoneTime);
        publishChange(RegistryChange.Action.DEREGISTER, existing, existing, null, null);
    }

    /**
     * Queries the durable store first and warms cache with normalized results.
     *
     * @param vector lookup criteria
     * @return matching entries from the durable store or cache fallback
     */
    @Override
    public List<T> query(Vector vector) {
        RegistryQuery query = RegistryScopeMapping.query(vector, registryType);
        Vector criteria = RegistryScopeMapping.toVector(query);
        if (store == null || !storeSupports(Trait.QUERY)) {
            if (store != null) {
                capabilityFallback("query", Trait.QUERY, "cache query");
            }
            return super.query(criteria);
        }
        List<T> entries = store.query(registryType, query);
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<T> result = new java.util.ArrayList<>(entries.size());
        for (T entry : entries) {
            T prepared = normalizeEntry(entry);
            cacheEntry(prepared);
            result.add(prepared);
        }
        return result;
    }

    /**
     * Saves an entry together with a runtime instance and emits one post-commit event.
     *
     * @param entry    entry to save
     * @param instance runtime instance
     */
    @Override
    public void register(T entry, Instance instance) {
        if (entry == null) {
            return;
        }
        T prepared = normalizeEntry(entry);
        T existing = loadExisting(prepared);
        persistEntry(prepared, instance);
        publishChange(
                existing == null ? RegistryChange.Action.REGISTER : RegistryChange.Action.UPDATE,
                prepared,
                existing,
                instance,
                null);
    }

    /**
     * Writes a normalized entry into cache only.
     *
     * @param entry entry to cache
     */
    protected void cacheEntry(T entry) {
        super.register(normalizeEntry(entry));
    }

    /**
     * Normalizes namespace and type before durable-store or cache writes.
     *
     * @param entry incoming entry
     * @return normalized entry
     */
    protected T normalizeEntry(T entry) {
        return RegistryIdentity.normalize(entry, registryType);
    }

    /**
     * Normalizes namespace values to the default namespace when absent.
     *
     * @param namespace raw namespace
     * @return canonical namespace
     */
    protected String normalizeNamespace(String namespace) {
        return RegistryIdentity.namespace(namespace);
    }

    /**
     * Emits a post-commit change event.
     *
     * @param action           mutation action
     * @param entry            changed entry
     * @param previousEntry    previous entry snapshot before the mutation
     * @param instance         runtime instance when applicable
     * @param previousInstance previous runtime instance snapshot before the mutation
     */
    protected void publishChange(
            RegistryChange.Action action,
            T entry,
            T previousEntry,
            Instance instance,
            Instance previousInstance) {
        if (entry == null) {
            return;
        }
        RegistryChange<T> event = new RegistryChange<>();
        event.setAction(action);
        event.setType(registryType);
        event.setNamespace_id(entry.getNamespace_id());
        event.setApp_id(entry.getApp_id());
        event.setId(entry.getId());
        event.setMethod(entry.getMethod());
        event.setVersion(entry.getVersion());
        event.setAsset(entry);
        event.setPreviousAsset(previousEntry);
        event.setInstance(instance);
        event.setPreviousInstance(previousInstance);
        event.setFingerprint(
                instance != null ? instance.getFingerprint()
                        : previousInstance != null ? previousInstance.getFingerprint() : null);
        event.setEventType(registryEventType(action, instance, previousInstance));
        event.setChangeSet(changeSet(entry, previousEntry, instance, previousInstance));
        long now = System.currentTimeMillis();
        event.setSequence(sequence.next("registry:" + registryType.name().toLowerCase()));
        event.setTimestamp(now);
        appendChangeLog(event);
        for (Listener<RegistryChange<T>> listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                try {
                    listener.onError(event, e);
                } catch (Exception errorHandlerFailure) {
                    Logger.warn(
                            "Registry listener error handler failed [{}:{}]: {}",
                            registryType,
                            entry.getId(),
                            errorHandlerFailure.getMessage());
                }
                Logger.warn(
                        "Registry listener execution failed [{}:{}]: {}",
                        registryType,
                        entry.getId(),
                        e.getMessage());
            }
        }
        publishWatchChange(event);
    }

    /**
     * Appends the committed registry change to the optional outbox.
     *
     * @param event registry event
     */
    protected void appendChangeLog(RegistryChange<T> event) {
        if (changeLogStore == null || event == null) {
            return;
        }
        CortexChangeRecord record = new CortexChangeRecord();
        record.setDomain("registry");
        record.setAction(registryOutboxAction(event));
        record.setResourceType(event.getType() == null ? null : event.getType().name());
        record.setResourceId(event.getId());
        record.setNamespace_id(event.getNamespace_id());
        record.setPayload(JsonKit.toJsonString(event));
        record.setSequence(event.getSequence());
        record.setIdempotencyKey(
                "registry:" + event.getType() + ":" + event.getNamespace_id() + ":" + event.getId() + ":"
                        + record.getAction() + ":" + registryOutboxResource(event));
        changeLogStore.append(record);
    }

    /**
     * Resolves the outbox action name for a registry change event.
     *
     * @param event registry change event
     * @return outbox action name
     */
    private String registryOutboxAction(RegistryChange<T> event) {
        if (event.getEventType() != null && event.getEventType().startsWith("registry-instance-")) {
            return event.getEventType().substring("registry-".length());
        }
        return event.getAction() == null ? null : event.getAction().name().toLowerCase();
    }

    /**
     * Builds the resource identifier used by registry outbox idempotency.
     *
     * @param event registry change event
     * @return resource identifier
     */
    private String registryOutboxResource(RegistryChange<T> event) {
        if (event.getFingerprint() != null) {
            return "instance:" + event.getFingerprint();
        }
        if (event.getId() != null) {
            return event.getId();
        }
        if (event.getApp_id() != null || event.getMethod() != null || event.getVersion() != null) {
            return (event.getApp_id() == null ? "" : event.getApp_id()) + ":"
                    + (event.getMethod() == null ? "" : event.getMethod()) + ":"
                    + (event.getVersion() == null ? "" : event.getVersion());
        }
        return "unknown";
    }

    /**
     * Mirrors committed registry changes into the shared watch channel.
     *
     * @param event registry change event
     */
    protected void publishWatchChange(RegistryChange<T> event) {
        if (watchManager == null || event == null) {
            return;
        }
        watchManager.notifyRegistry(
                event.getEventType(),
                event.getAction(),
                event.getAsset(),
                event.getPreviousAsset(),
                event.getInstance(),
                event.getPreviousInstance());
    }

    /**
     * Loads the current durable snapshot before a mutation.
     *
     * @param prepared normalized entry
     * @return current durable snapshot or {@code null}
     */
    protected T loadExisting(T prepared) {
        return store != null ? store.find(registryType, prepared.getNamespace_id(), prepared.getId()) : null;
    }

    /**
     * Persists the entry to durable state and refreshes the local cache projection.
     *
     * @param prepared normalized entry
     * @param instance optional runtime instance
     */
    protected void persistEntry(T prepared, Instance instance) {
        if (store != null && storeSupports(Trait.DURABLE)) {
            if (instance == null) {
                store.save(prepared);
            } else {
                store.save(prepared, instance);
            }
        } else if (store != null) {
            capabilityFallback("persist", Trait.DURABLE, "cache write");
        }
        cacheEntry(prepared);
    }

    /**
     * Deletes the entry from durable state and local cache.
     *
     * @param namespace namespace
     * @param id        entry identifier
     */
    protected void deleteEntry(String namespace, String id) {
        if (store != null && storeSupports(Trait.DURABLE)) {
            store.delete(registryType, namespace, id);
        } else if (store != null) {
            capabilityFallback("delete", Trait.DURABLE, "cache remove");
        }
        cacheX.remove(buildKey(namespace, id));
    }

    /**
     * Builds a lightweight change set used by bridge and audit listeners.
     *
     * @param entry            current entry
     * @param previousEntry    previous entry
     * @param instance         current instance
     * @param previousInstance previous instance
     * @return change set
     */
    protected Map<String, Object> changeSet(T entry, T previousEntry, Instance instance, Instance previousInstance) {
        Map<String, Object> changeSet = new LinkedHashMap<>();
        if (previousEntry == null) {
            changeSet.put("mutation", "create");
        } else if (entry.getStatus() != null && entry.getStatus() < 0) {
            changeSet.put("mutation", "delete");
        } else {
            changeSet.put("mutation", "update");
        }
        changeSet.put("id", entry.getId());
        changeSet.put("method", entry.getMethod());
        changeSet.put("version", entry.getVersion());
        if (previousEntry != null) {
            changeSet.put("previousModified", previousEntry.getModified());
        }
        if (instance != null) {
            changeSet.put("fingerprint", instance.getFingerprint());
        }
        if (previousInstance != null) {
            changeSet.put("previousFingerprint", previousInstance.getFingerprint());
        }
        return changeSet;
    }

    /**
     * Resolves the watch event type for a registry mutation.
     *
     * @param action           registry action
     * @param instance         current instance
     * @param previousInstance previous instance
     * @return watch event type
     */
    private String registryEventType(RegistryChange.Action action, Instance instance, Instance previousInstance) {
        if (instance != null && previousInstance != null && healthChanged(instance, previousInstance)) {
            return WatchManager.REGISTRY_INSTANCE_HEALTH_CHANGE_EVENT;
        }
        if (instance != null) {
            return WatchManager.REGISTRY_INSTANCE_UP_EVENT;
        }
        if (action == RegistryChange.Action.DEREGISTER && previousInstance != null) {
            return WatchManager.REGISTRY_INSTANCE_DOWN_EVENT;
        }
        return switch (action) {
            case REGISTER -> WatchManager.REGISTRY_REGISTER_EVENT;
            case UPDATE -> WatchManager.REGISTRY_UPDATE_EVENT;
            case DEREGISTER -> WatchManager.REGISTRY_DEREGISTER_EVENT;
        };
    }

    /**
     * Returns whether health-related instance fields changed.
     *
     * @param current  current instance
     * @param previous previous instance
     * @return {@code true} when health state changed
     */
    private boolean healthChanged(Instance current, Instance previous) {
        return !Objects.equals(current.getState(), previous.getState())
                || !Objects.equals(current.getHealthy(), previous.getHealthy())
                || !Objects.equals(current.getHealthSource(), previous.getHealthSource());
    }

    /**
     * Returns whether the backing registry store supports a trait.
     *
     * @param capability required store trait
     * @return {@code true} when the trait is supported
     */
    protected boolean storeSupports(Trait capability) {
        return store != null && store.storeCapabilities().supports(capability);
    }

    /**
     * Logs that an operation is using a fallback because a store trait is missing.
     *
     * @param operation  operation name
     * @param capability missing store trait
     * @param fallback   fallback behavior description
     */
    protected void capabilityFallback(String operation, Trait capability, String fallback) {
        Logger.warn(
                "Registry store capability missing [type={}, operation={}, capability={}], using {}",
                registryType,
                operation,
                capability == null ? null : capability.key(),
                fallback);
    }

    /**
     * Finds one cached entry by route identity.
     *
     * @param namespace namespace
     * @param app_id    application identifier
     * @param method    method or route name
     * @param version   route version
     * @return cached entry or {@code null}
     */
    private T findCachedByRoute(String namespace, String app_id, String method, String version) {
        Map<String, Object> raw = cacheX.scan(buildScanPrefix(namespace));
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        for (Object value : raw.values()) {
            T entry = deserialize(value);
            if (entry != null && Objects.equals(app_id, entry.getApp_id()) && Objects.equals(method, entry.getMethod())
                    && Objects.equals(version, entry.getVersion())) {
                return entry;
            }
        }
        return null;
    }

}
