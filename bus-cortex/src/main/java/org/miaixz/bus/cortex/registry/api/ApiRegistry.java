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
package org.miaixz.bus.cortex.registry.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.*;
import org.miaixz.bus.cortex.builtin.RegistryGenerator;
import org.miaixz.bus.cortex.health.HealthInfo;
import org.miaixz.bus.cortex.magic.event.CortexChangeLogStore;
import org.miaixz.bus.cortex.magic.identity.Checksum;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.cortex.registry.RegistryAssets;
import org.miaixz.bus.cortex.registry.RegistryChange;
import org.miaixz.bus.cortex.registry.RegistryStore;
import org.miaixz.bus.cortex.registry.StoreBackedRegistry;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Registry for API service definitions and runtime instances.
 *
 * @author Kimi Liu
 */
public class ApiRegistry extends StoreBackedRegistry<ApiAssets> implements Registry<ApiAssets> {

    /**
     * Creates an ApiRegistry backed by the given CacheX and WatchManager.
     *
     * @param cacheX        shared cache for registry state
     * @param watchManager  watch subscription manager
     * @param store         durable store adapter
     * @param syncListeners post-commit listeners
     */
    public ApiRegistry(CacheX<String, Object> cacheX, WatchManager watchManager, RegistryStore<ApiAssets> store,
            List<Listener<RegistryChange<ApiAssets>>> syncListeners) {
        this(cacheX, watchManager, store, syncListeners, RegistryGenerator.INSTANCE);
    }

    /**
     * Creates an ApiRegistry backed by the given CacheX, WatchManager and keying strategy.
     *
     * @param cacheX        shared cache for registry state
     * @param watchManager  watch subscription manager
     * @param store         durable store adapter
     * @param syncListeners post-commit listeners
     * @param keying        route-key strategy
     */
    public ApiRegistry(CacheX<String, Object> cacheX, WatchManager watchManager, RegistryStore<ApiAssets> store,
            List<Listener<RegistryChange<ApiAssets>>> syncListeners, Keying<Keying.RegistrySpec> keying) {
        super(cacheX, watchManager, store, ApiAssets.class, Type.API, syncListeners, keying);
    }

    /**
     * Creates an ApiRegistry backed by the given CacheX, WatchManager and optional outbox store.
     *
     * @param cacheX         shared cache for registry state
     * @param watchManager   watch subscription manager
     * @param store          durable store adapter
     * @param syncListeners  post-commit listeners
     * @param changeLogStore optional outbox store
     */
    public ApiRegistry(CacheX<String, Object> cacheX, WatchManager watchManager, RegistryStore<ApiAssets> store,
            List<Listener<RegistryChange<ApiAssets>>> syncListeners, CortexChangeLogStore changeLogStore) {
        this(cacheX, watchManager, store, syncListeners, changeLogStore, RegistryGenerator.INSTANCE);
    }

    /**
     * Creates an ApiRegistry backed by the given CacheX, WatchManager, optional outbox store, and keying strategy.
     *
     * @param cacheX         shared cache for registry state
     * @param watchManager   watch subscription manager
     * @param store          durable store adapter
     * @param syncListeners  post-commit listeners
     * @param changeLogStore optional outbox store
     * @param keying         route-key strategy
     */
    public ApiRegistry(CacheX<String, Object> cacheX, WatchManager watchManager, RegistryStore<ApiAssets> store,
            List<Listener<RegistryChange<ApiAssets>>> syncListeners, CortexChangeLogStore changeLogStore,
            Keying<Keying.RegistrySpec> keying) {
        super(cacheX, watchManager, store, ApiAssets.class, Type.API, syncListeners, changeLogStore, keying);
    }

    /**
     * Registers one service definition.
     *
     * @param service service definition to register
     */
    @Override
    public void register(ApiAssets service) {
        synchronized (cacheX) {
            if (service == null) {
                return;
            }
            ApiAssets prepared = normalizeService(service, null);
            ApiAssets existing = loadExisting(prepared);
            persistEntry(prepared, null, existing);
            publishChange(
                    existing == null ? RegistryChange.Action.REGISTER : RegistryChange.Action.UPDATE,
                    prepared,
                    existing,
                    null,
                    null);
        }
    }

    /**
     * Registers one service definition using the snapshot resolved for the current batch.
     *
     * @param service  service definition to register
     * @param existing existing service snapshot or {@code null}
     */
    @Override
    public void registerResolved(ApiAssets service, ApiAssets existing) {
        synchronized (cacheX) {
            if (service == null) {
                return;
            }
            ApiAssets prepared = normalizeService(service, null);
            ApiAssets previous = existing == null ? null : normalizeService(existing, null);
            persistEntry(prepared, null, previous);
            publishChange(
                    previous == null ? RegistryChange.Action.REGISTER : RegistryChange.Action.UPDATE,
                    prepared,
                    previous,
                    null,
                    null);
        }
    }

    /**
     * Registers a shared process with the exact service definition supplied by the caller. Service method and service
     * version stay on the definition and never overwrite process fields.
     *
     * @param service  exact service definition
     * @param instance shared application process
     */
    @Override
    public void register(ApiAssets service, Instance instance) {
        synchronized (cacheX) {
            if (instance == null) {
                register(service);
                return;
            }
            ApiAssets prepared = normalizeService(service, instance);
            String checksum = ensureChecksum(instance);
            Instance previous = null;
            for (Instance known : queryInstances(null, prepared.getApp_id(), null, null)) {
                if (checksum.equals(known.getChecksum())
                        && Objects.equals(instance.getTenant_id(), known.getTenant_id())) {
                    if (instance.getId() == null) {
                        instance.setId(known.getId());
                    } else if (known.getId() != null && !instance.getId().equals(known.getId())) {
                        throw new IllegalArgumentException("Process identity conflicts with an existing registration");
                    }
                }
            }
            previous = findInstance(prepared, checksum);
            if (instance.getId() == null) {
                instance.setId(ID.objectId());
            }
            if (instance.getWeight() == null || instance.getWeight() < 0) {
                throw new IllegalArgumentException("Runtime routing weight must be non-negative");
            }
            String key = keying.key(
                    Keying.RegistrySpec.instance(
                            null,
                            prepared.getApp_id(),
                            prepared.getMethod(),
                            prepared.getVersion(),
                            prepared.getVerb(),
                            checksum));
            ApiAssets existing = loadExisting(prepared);
            persistEntry(prepared, instance, existing);
            long ttl = prepared.getTtl() > 0 ? prepared.getTtl() : 3600_000L;
            Instance saved = BeanKit.toBean(instance, Instance.class);
            if (store != null && storeSupports(Trait.DURABLE)) {
                afterCommit(() -> cacheX.remove(key));
            } else {
                String serviceKey = keying.key(Keying.RegistrySpec.entry(null, Type.API, prepared.getId()));
                Object definition = cacheX.read(serviceKey);
                if (!(definition instanceof String json)
                        || !sameService(prepared, JsonKit.toPojo(json, ApiAssets.class)))
                    throw new IllegalStateException("Service changed during runtime registration");
                for (int attempt = 0;; attempt++) {
                    Object value = cacheX.read(key);
                    if (value != null && !(value instanceof String))
                        throw new IllegalStateException("Invalid cached runtime registration");
                    Instance current = value == null ? null : JsonKit.toPojo((String) value, Instance.class);
                    saved = retainObservation(current, instance);
                    if (cacheX.replace(key, value, JsonKit.toJsonString(saved), ttl, serviceKey, definition)
                            .toCompletableFuture().join())
                        break;
                    if (attempt >= 7)
                        throw new IllegalStateException("Concurrent runtime registration");
                }
                BeanKit.copyProperties(saved, instance);
            }
            publishChange(
                    existing == null ? RegistryChange.Action.REGISTER : RegistryChange.Action.UPDATE,
                    prepared,
                    existing,
                    saved,
                    previous);
        }
    }

    /**
     * Applies a completed probe through an atomic existing-record update, without resaving its service definition.
     * Cache-only writers share their local mutation boundary and use backend compare-and-replace across nodes.
     *
     * @param service     service snapshot captured before the probe
     * @param expected    runtime snapshot captured before the probe
     * @param observation completed result
     * @param started     probe start time in epoch milliseconds
     * @param finished    probe completion time in epoch milliseconds
     * @return whether the result was accepted
     */
    public boolean updateHealth(ApiAssets service, Instance expected, Status observation, long started, long finished) {
        String instanceKey = keying.key(
                Keying.RegistrySpec.instance(
                        null,
                        service.getApp_id(),
                        service.getMethod(),
                        service.getVersion(),
                        service.getVerb(),
                        expected.getChecksum()));
        Instance updated;
        if (store != null && storeSupports(Trait.DURABLE)) {
            updated = store.updateHealth(service, expected, observation, started, finished);
            if (updated == null)
                return false;
            // Invalidation avoids replacing a newer committed cache projection with this transaction's snapshot.
            afterCommit(() -> cacheX.remove(instanceKey));
        } else {
            synchronized (cacheX) {
                String serviceKey = keying.key(Keying.RegistrySpec.entry(null, Type.API, service.getId()));
                Object rawService = cacheX.read(serviceKey);
                if (!(rawService instanceof String definition)
                        || !sameDefinition(service, JsonKit.toPojo(definition, ApiAssets.class)))
                    return false;
                Object rawInstance = cacheX.read(instanceKey);
                if (!(rawInstance instanceof String runtime))
                    return false;
                updated = observe(JsonKit.toPojo(runtime, Instance.class), expected, observation, started, finished);
                if (updated == null)
                    return false;
                long ttl = service.getTtl() > 0 ? service.getTtl() : 3600_000L;
                if (!cacheX.replace(instanceKey, runtime, JsonKit.toJsonString(updated), ttl, serviceKey, rawService)
                        .toCompletableFuture().join())
                    return false;
                publishChange(RegistryChange.Action.UPDATE, service, service, updated, expected);
                return true;
            }
        }
        publishChange(RegistryChange.Action.UPDATE, service, service, updated, expected);
        return true;
    }

    /**
     * Compares complete API definition snapshots without including subtype parsing caches or host-only fields.
     *
     * @param expected captured service definition
     * @param current  current service definition
     * @return whether the active definition is unchanged
     */
    public static boolean sameDefinition(Assets expected, Assets current) {
        return current != null && Integer.valueOf(1).equals(current.getStatus()) && sameService(expected, current);
    }

    /**
     * Compares complete definition snapshots without imposing an enabled-state requirement on management writes.
     *
     * @param expected captured definition
     * @param current  current definition
     * @return whether both normalized definitions are identical
     */
    private static boolean sameService(Assets expected, Assets current) {
        return expected != null && current != null && JsonKit.toValue(RegistryAssets.api(expected).normalizeMeta())
                .equals(JsonKit.toValue(RegistryAssets.api(current).normalizeMeta()));
    }

    /**
     * Preserves authoritative observations when the same process session registers the same probe endpoint again.
     * Registration may update routing fields and explicit administrative states, but cannot replay an older health
     * result. A changed session or endpoint clears observations belonging to the preceding probe context.
     *
     * @param current  authoritative existing registration, or null for a new association
     * @param incoming requested registration
     * @return detached registration containing the applicable current observation
     */
    public static Instance retainObservation(Instance current, Instance incoming) {
        Instance result = BeanKit.toBean(Objects.requireNonNull(incoming, "incoming"), Instance.class);
        if (current == null)
            return result;
        if (current.isActive() && !Objects.equals(current.getId(), incoming.getId())
                || !Objects.equals(current.getTenant_id(), incoming.getTenant_id())
                || !Objects.equals(current.getApp_id(), incoming.getApp_id())
                || !Objects.equals(current.getChecksum(), incoming.getChecksum()))
            throw new IllegalArgumentException("Runtime process identity changed");
        HealthInfo old = current.healthView();
        HealthInfo health = result.healthView();
        boolean same = Objects.equals(current.getId(), incoming.getId())
                && Objects.equals(old.getSession(), health.getSession())
                && Objects.equals(current.getHost(), result.getHost())
                && Objects.equals(current.getPort(), result.getPort())
                && Objects.equals(old.getScheme(), health.getScheme())
                && Objects.equals(old.getPath(), health.getPath());
        health.setTime(same ? old.getTime() : null);
        health.setOrigin(same ? old.getOrigin() : null);
        if (same && old.getTime() != null && incoming.isActive()
                && !Status.Health.MAINTENANCE.matches(incoming.getStatus())
                && (Status.Health.UP.matches(current.getStatus()) || Status.Health.DOWN.matches(current.getStatus())))
            result.setStatus(current.getStatus());
        if (same && current.getModified() != null
                && (result.getModified() == null || result.getModified() < current.getModified()))
            result.setModified(current.getModified());
        result.health(health);
        return result;
    }

    /**
     * Publishes a runtime projection only while its authoritative snapshot remains current. Persistent hosts hold their
     * row fences through publication; cache-only callbacks share the local registry mutation boundary.
     *
     * @param entry    captured definition
     * @param instance captured runtime, or null for a definition-only event
     * @param previous retired runtime when publishing a removal, or null
     * @param action   event construction and dispatch
     */
    @Override
    protected void publishCurrent(ApiAssets entry, Instance instance, Instance previous, Runnable action) {
        if (instance == null && previous == null) {
            action.run();
            return;
        }
        if (store != null && storeSupports(Trait.DURABLE)) {
            store.publish(entry, instance, previous, action);
            return;
        }
        Instance expected = instance == null ? previous : instance;
        synchronized (cacheX) {
            Object definition = cacheX.read(keying.key(Keying.RegistrySpec.entry(null, Type.API, entry.getId())));
            Object runtime = cacheX.read(
                    keying.key(
                            Keying.RegistrySpec.instance(
                                    null,
                                    entry.getApp_id(),
                                    entry.getMethod(),
                                    entry.getVersion(),
                                    entry.getVerb(),
                                    expected.getChecksum())));
            if (definition instanceof String json && sameService(entry, JsonKit.toPojo(json, ApiAssets.class))
                    && (instance == null ? runtime == null
                            : runtime instanceof String value && JsonKit.toValue(instance)
                                    .equals(JsonKit.toValue(JsonKit.toPojo(value, Instance.class)))))
                action.run();
        }
    }

    /**
     * Validates a probe fence and merges observations into a detached current instance. Storage implementations must
     * atomically compare the row used here before committing this result.
     *
     * @param current     authoritative registration snapshot
     * @param expected    snapshot captured before I/O
     * @param observation completed probe result
     * @param started     probe start in epoch milliseconds
     * @param finished    probe completion in epoch milliseconds
     * @return merged observation, or null for a stale or non-probeable instance
     */
    public static Instance observe(
            Instance current,
            Instance expected,
            Status observation,
            long started,
            long finished) {
        Objects.requireNonNull(observation);
        if (started < 0 || finished < started)
            throw new IllegalArgumentException("Invalid probe times");
        if (current == null || expected == null || !current.isActive()
                || Status.Health.MAINTENANCE.matches(current.getStatus())
                || !JsonKit.toValue(BeanKit.toBean(current, Instance.class))
                        .equals(JsonKit.toValue(BeanKit.toBean(expected, Instance.class))))
            return null;
        HealthInfo health = current.healthView();
        if (health.getTime() != null && started <= health.getTime()
                || current.getModified() != null && current.getModified() > started)
            return null;
        Instance updated = BeanKit.toBean(current, Instance.class);
        health.setTime(finished);
        health.setOrigin(observation.getSource());
        updated.health(health);
        updated.setStatus(observation.isHealthy() ? Status.Health.UP.getKey() : Status.Health.DOWN.getKey());
        updated.setModified(finished);
        return updated;
    }

    /**
     * Removes a service and its cache-only runtime registrations without allowing a local probe to republish them.
     *
     * @param tenantId API registry context
     * @param id       service identifier
     */
    @Override
    public void deregister(String tenantId, String id) {
        synchronized (cacheX) {
            ApiAssets service = find(tenantId, id);
            invalidateInstances(service);
            super.deregister(tenantId, id);
        }
    }

    /**
     * Removes an already resolved service under the same local cache mutation boundary as probes.
     *
     * @param tenantId API registry context
     * @param id       service identifier
     * @param existing previously resolved definition
     */
    @Override
    public void deregisterResolved(String tenantId, String id, ApiAssets existing) {
        synchronized (cacheX) {
            invalidateInstances(existing);
            super.deregisterResolved(tenantId, id, existing);
        }
    }

    /**
     * Removes cached relationships after the owning service deletion commits.
     *
     * @param service definition being removed, or null when absent
     */
    private void invalidateInstances(ApiAssets service) {
        if (service == null)
            return;
        String prefix = keying.prefix(
                Keying.RegistrySpec.instance(
                        null,
                        service.getApp_id(),
                        service.getMethod(),
                        service.getVersion(),
                        service.getVerb(),
                        null));
        Map<String, Object> instances = cacheX.scan(prefix);
        if (instances != null)
            instances.keySet().forEach(key -> afterCommit(() -> cacheX.remove(key)));
    }

    /**
     * Removes only the selected service registration, leaving the common process and other registrations intact.
     *
     * @param tenantId registry query context
     * @param app_id   owning application
     * @param method   service method
     * @param version  service version
     * @param checksum process checksum
     */
    @Override
    public void deregisterInstance(String tenantId, String app_id, String method, String version, String checksum) {
        synchronized (cacheX) {
            ApiAssets service = findServiceByRoute(app_id, method, version);
            Instance current = service == null ? null : findInstance(service, checksum);
            if (store != null && storeSupports(Trait.DURABLE) && storeSupports(Trait.INSTANCES)) {
                store.deleteInstance(registryType, normalizeTenant(tenantId), app_id, method, version, checksum);
            } else if (store != null) {
                capabilityFallback("deleteInstance", Trait.INSTANCES, "cache remove");
            }
            if (service != null) {
                String key = keying
                        .key(Keying.RegistrySpec.instance(null, app_id, method, version, service.getVerb(), checksum));
                afterCommit(() -> cacheX.remove(key));
            }
            if (current != null && service != null) {
                publishChange(RegistryChange.Action.DEREGISTER, service, service, null, current);
            }
        }
    }

    /**
     * Reads shared processes associated with the requested service coordinates. Durable stores return current shared
     * process snapshots without warming caches; cache reads scan every matching service before deduplicating processes.
     *
     * @param tenantId registry query context
     * @param app_id   optional application filter
     * @param method   optional service method
     * @param version  optional service version
     * @return distinct runtime process snapshots matching the selected services
     */
    @Override
    public List<Instance> queryInstances(String tenantId, String app_id, String method, String version) {
        Map<List<String>, Instance> result = new LinkedHashMap<>();
        if (store != null && storeSupports(Trait.INSTANCES)) {
            for (Instance instance : store
                    .queryInstances(registryType, normalizeTenant(tenantId), app_id, method, version)) {
                requireInstance(instance, app_id);
                result.put(identity(instance), instance);
            }
            return List.copyOf(result.values());
        }
        Vector vector = Vector.builder().type(Type.API.key()).app_id(app_id).method(method).version(version).build();
        while (true) {
            List<ApiAssets> services = query(vector);
            if (services.isEmpty()) {
                break;
            }
            for (ApiAssets service : services) {
                if (!matches(app_id, service.getApp_id()) || !matches(method, service.getMethod())
                        || !matches(version, service.getVersion())) {
                    continue;
                }
                for (Instance instance : queryInstances(service))
                    result.putIfAbsent(identity(instance), instance);
            }
            if (services.size() < vector.getLimit()) {
                break;
            }
            vector.setOffset(Math.addExact(vector.getOffset(), services.size()));
        }
        return List.copyOf(result.values());
    }

    /**
     * Reads every runtime association for the exact supplied service without deduplicating distinct configurations or
     * warming caches from a durable snapshot. Persistent registrations remain authoritative in their store.
     *
     * @param service complete service definition including its verb
     * @return current snapshots associated with that exact service
     */
    public List<Instance> queryInstances(ApiAssets service) {
        Objects.requireNonNull(service, "service");
        List<Instance> result = new ArrayList<>();
        if (store != null && storeSupports(Trait.INSTANCES)) {
            for (Instance instance : store.queryInstances(service)) {
                requireInstance(instance, service.getApp_id());
                result.add(instance);
            }
        } else {
            String prefix = keying.prefix(
                    Keying.RegistrySpec.instance(
                            null,
                            service.getApp_id(),
                            service.getMethod(),
                            service.getVersion(),
                            service.getVerb(),
                            null));
            Map<String, Object> entries = cacheX.scan(prefix);
            if (entries != null) {
                for (Map.Entry<String, Object> entry : entries.entrySet()) {
                    if (!(entry.getValue() instanceof String json))
                        continue;
                    Instance instance = JsonKit.toPojo(json, Instance.class);
                    requireInstance(instance, service.getApp_id());
                    String exact = keying.key(
                            Keying.RegistrySpec.instance(
                                    null,
                                    service.getApp_id(),
                                    service.getMethod(),
                                    service.getVersion(),
                                    service.getVerb(),
                                    instance.getChecksum()));
                    if (exact.equals(entry.getKey()))
                        result.add(instance);
                }
            }
        }
        return List.copyOf(result);
    }

    /**
     * Reports runtime registration support.
     *
     * @return true
     */
    @Override
    public boolean supportsInstances() {
        return true;
    }

    /**
     * Normalizes the service and fills missing application ownership without changing process version or tenant.
     *
     * @param service  exact service definition
     * @param instance optional shared process
     * @return normalized service definition
     */
    private ApiAssets normalizeService(ApiAssets service, Instance instance) {
        ApiAssets prepared = normalizeEntry(service);
        if (StringKit.isBlank(prepared.getApp_id())) {
            throw new IllegalArgumentException("API application identifier is required");
        }
        prepared.normalizeMeta(keying);
        if (instance != null) {
            if (StringKit.isEmpty(instance.getApp_id())) {
                instance.setApp_id(prepared.getApp_id());
            }
            if (StringKit.isEmpty(instance.getApp_id()) || !Objects.equals(instance.getApp_id(), prepared.getApp_id())
                    || StringKit.isEmpty(prepared.getId()) || StringKit.isEmpty(prepared.getMethod())
                    || StringKit.isEmpty(prepared.getVersion())) {
                throw new IllegalArgumentException("Exact service and owning application process are required");
            }
            if (instance.getWeight() == null)
                instance.setWeight(100);
            if (instance.getStatus() == null)
                instance.setStatus(Status.Health.UNKNOWN.getKey());
            if (!instance.isActive() && !Status.Health.DISABLED.matches(instance.getStatus())
                    && !Status.Health.DELETED.matches(instance.getStatus()))
                throw new IllegalArgumentException("Unknown instance status");
            instance.healthView();
            instance.labelView();
            instance.metadataView();
        }
        return prepared;
    }

    /**
     * Ensures the process checksum using the shared host and port checksum utility.
     *
     * @param instance shared process
     * @return stable process checksum
     */
    private String ensureChecksum(Instance instance) {
        if (StringKit.isEmpty(instance.getChecksum())) {
            if (instance.getHost() == null || instance.getPort() == null) {
                throw new IllegalArgumentException("Instance host and port are required when checksum is blank");
            }
            instance.setChecksum(Checksum.of(instance.getHost(), instance.getPort()));
        }
        return instance.getChecksum();
    }

    /**
     * Finds an exact process within a service registration context.
     *
     * @param service  complete service definition
     * @param checksum process checksum
     * @return runtime snapshot, or null when absent
     */
    private Instance findInstance(ApiAssets service, String checksum) {
        if (StringKit.isEmpty(checksum)) {
            return null;
        }
        return queryInstances(service).stream().filter(instance -> checksum.equals(instance.getChecksum())).findFirst()
                .orElse(null);
    }

    /**
     * Finds the existing service definition without synthesizing a service identifier from a process.
     *
     * @param app     application identifier
     * @param method  service method
     * @param version service version
     * @return service definition, or null when absent
     */
    private ApiAssets findServiceByRoute(String app, String method, String version) {
        if (store != null && storeSupports(Trait.ROUTE_QUERY)) {
            return store.findByMethodVersion(registryType, null, app, method, version);
        }
        List<ApiAssets> services = query(
                Vector.builder().type(Type.API.key()).app_id(app).method(method).version(version).build());
        List<ApiAssets> matches = services.stream()
                .filter(
                        service -> matches(app, service.getApp_id()) && matches(method, service.getMethod())
                                && matches(version, service.getVersion()))
                .toList();
        if (matches.size() > 1)
            throw new IllegalArgumentException("Service route is ambiguous without a verb");
        return matches.isEmpty() ? null : matches.getFirst();
    }

    /**
     * Tests an optional route filter.
     *
     * @param expected optional filter
     * @param actual   actual coordinate
     * @return whether the coordinate matches
     */
    private boolean matches(String expected, String actual) {
        return expected == null || expected.equals(actual);
    }

    /**
     * Validates a shared process snapshot and its application ownership.
     *
     * @param instance returned shared process
     * @param app      optional expected application
     */
    private void requireInstance(Instance instance, String app) {
        if (instance == null || StringKit.isEmpty(instance.getId()) || StringKit.isEmpty(instance.getApp_id())
                || StringKit.isEmpty(instance.getChecksum()) || !matches(app, instance.getApp_id())) {
            throw new IllegalStateException("Invalid shared process snapshot");
        }
    }

    /**
     * Returns exact process identity coordinates for deduplication across service registrations.
     *
     * @param instance validated shared process
     * @return tenant, application and checksum coordinates
     */
    private List<String> identity(Instance instance) {
        return List.of(Objects.toString(instance.getTenant_id(), ""), instance.getApp_id(), instance.getChecksum());
    }

}
