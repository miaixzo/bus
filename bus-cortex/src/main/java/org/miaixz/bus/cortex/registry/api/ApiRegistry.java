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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.*;
import org.miaixz.bus.cortex.builtin.RegistryGenerator;
import org.miaixz.bus.cortex.magic.event.CortexChangeLogStore;
import org.miaixz.bus.cortex.magic.identity.Checksum;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
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

    /**
     * Registers one service definition using the snapshot resolved for the current batch.
     *
     * @param service  service definition to register
     * @param existing existing service snapshot or {@code null}
     */
    @Override
    public void registerResolved(ApiAssets service, ApiAssets existing) {
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

    /**
     * Registers a shared process with the exact service definition supplied by the caller. Service method and service
     * version stay on the definition and never overwrite process fields.
     *
     * @param service  exact service definition
     * @param instance shared application process
     */
    @Override
    public void register(ApiAssets service, Instance instance) {
        if (instance == null) {
            register(service);
            return;
        }
        ApiAssets prepared = normalizeService(service, instance);
        String checksum = ensureChecksum(instance);
        Instance previous = null;
        for (Instance known : queryInstances(null, prepared.getApp_id(), null, null)) {
            if (checksum.equals(known.getChecksum()) && Objects.equals(instance.getTenant_id(), known.getTenant_id())) {
                if (instance.getId() == null) {
                    instance.setId(known.getId());
                } else if (known.getId() != null && !instance.getId().equals(known.getId())) {
                    throw new IllegalArgumentException("Process identity conflicts with an existing registration");
                }
            }
        }
        previous = findInstance(prepared.getApp_id(), prepared.getMethod(), prepared.getVersion(), checksum);
        if (instance.getId() == null) {
            instance.setId(ID.objectId());
        }
        if (instance.getWeight() == null || instance.getWeight() < 0) {
            throw new IllegalArgumentException("Runtime routing weight must be non-negative");
        }
        String key = keying.key(
                Keying.RegistrySpec
                        .instance(null, prepared.getApp_id(), prepared.getMethod(), prepared.getVersion(), checksum));
        ApiAssets existing = loadExisting(prepared);
        persistEntry(prepared, instance, existing);
        long ttl = prepared.getTtl() > 0 ? prepared.getTtl() : 3600_000L;
        afterCommit(() -> cacheX.write(key, JsonKit.toJsonString(instance), ttl));
        publishChange(
                existing == null ? RegistryChange.Action.REGISTER : RegistryChange.Action.UPDATE,
                prepared,
                existing,
                instance,
                previous);
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
        Instance current = findInstance(app_id, method, version, checksum);
        ApiAssets service = findServiceByRoute(app_id, method, version);
        if (store != null && storeSupports(Trait.DURABLE) && storeSupports(Trait.INSTANCES)) {
            store.deleteInstance(registryType, normalizeTenant(tenantId), app_id, method, version, checksum);
        } else if (store != null) {
            capabilityFallback("deleteInstance", Trait.INSTANCES, "cache remove");
        }
        String key = keying.key(Keying.RegistrySpec.instance(null, app_id, method, version, checksum));
        afterCommit(() -> cacheX.remove(key));
        if (current != null && service != null) {
            publishChange(RegistryChange.Action.DEREGISTER, service, service, null, current);
        }
    }

    /**
     * Reads shared processes associated with the requested service coordinates. Durable stores return current shared
     * process snapshots; cache reads obtain routes from existing ApiAssets.
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
                if (app_id != null && method != null && version != null) {
                    String key = keying
                            .key(Keying.RegistrySpec.instance(null, app_id, method, version, instance.getChecksum()));
                    afterCommit(
                            () -> cacheX.write(
                                    key,
                                    JsonKit.toJsonString(instance),
                                    Builder.DEFAULT_HEALTH_INTERVAL_MS * 120));
                }
            }
            return List.copyOf(result.values());
        }
        Vector vector = Vector.builder().type(Type.API.key()).app_id(app_id).method(method).version(version).build();
        for (ApiAssets service : query(vector)) {
            if (!matches(app_id, service.getApp_id()) || !matches(method, service.getMethod())
                    || !matches(version, service.getVersion())) {
                continue;
            }
            String prefix = keying.prefix(
                    Keying.RegistrySpec
                            .instance(null, service.getApp_id(), service.getMethod(), service.getVersion(), null));
            Map<String, Object> raw = cacheX.scan(prefix);
            if (raw == null) {
                continue;
            }
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                if (entry.getValue() instanceof String json) {
                    Instance instance = JsonKit.toPojo(json, Instance.class);
                    requireInstance(instance, service.getApp_id());
                    String exact = keying.key(
                            Keying.RegistrySpec.instance(
                                    null,
                                    service.getApp_id(),
                                    service.getMethod(),
                                    service.getVersion(),
                                    instance.getChecksum()));
                    if (exact.equals(entry.getKey())) {
                        result.putIfAbsent(identity(instance), instance);
                    }
                }
            }
        }
        return List.copyOf(result.values());
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
            if (instance.getLease() == null) {
                instance.setLease(prepared.meta().getLease());
            }
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
     * @param app      owning application
     * @param method   service method
     * @param version  service version
     * @param checksum process checksum
     * @return runtime snapshot, or null when absent
     */
    private Instance findInstance(String app, String method, String version, String checksum) {
        if (StringKit.isEmpty(checksum)) {
            return null;
        }
        return queryInstances(null, app, method, version).stream()
                .filter(instance -> checksum.equals(instance.getChecksum())).findFirst().orElse(null);
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
        return services.stream()
                .filter(
                        service -> matches(app, service.getApp_id()) && matches(method, service.getMethod())
                                && matches(version, service.getVersion()))
                .findFirst().orElse(null);
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
