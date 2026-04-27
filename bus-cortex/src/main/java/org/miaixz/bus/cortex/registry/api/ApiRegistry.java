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
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.Registry;
import org.miaixz.bus.cortex.Trait;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.magic.identity.Fingerprint;
import org.miaixz.bus.cortex.registry.RegistryChange;
import org.miaixz.bus.cortex.registry.RegistryKeys;
import org.miaixz.bus.cortex.registry.RegistryStore;
import org.miaixz.bus.cortex.registry.StoreBackedRegistry;
import org.miaixz.bus.cortex.magic.event.CortexChangeLogStore;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Registry for API service definitions and runtime instances.
 *
 * @author Kimi Liu
 * @since Java 21+
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
        super(cacheX, watchManager, store, ApiAssets.class, Type.API, syncListeners);
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
        super(cacheX, watchManager, store, ApiAssets.class, Type.API, syncListeners, changeLogStore);
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
        persistEntry(prepared, null);
        publishChange(
                existing == null ? RegistryChange.Action.REGISTER : RegistryChange.Action.UPDATE,
                prepared,
                existing,
                null,
                null);
    }

    /**
     * Registers both a service definition and one runtime instance.
     *
     * @param service  service definition to register
     * @param instance runtime instance associated with the service
     */
    @Override
    public void register(ApiAssets service, Instance instance) {
        if (instance == null) {
            register(service);
            return;
        }
        ApiAssets prepared = normalizeService(service, instance);
        String ns = prepared.getNamespace_id();
        String appId = prepared.getApp_id();
        String fingerprint = ensureFingerprint(instance);
        String method = prepared.getMethod();
        String version = prepared.getVersion();
        Instance previous = findInstance(ns, appId, method, version, fingerprint);
        fillInstanceIdentity(prepared, instance);

        long ttl = prepared.getTtl() > 0 ? prepared.getTtl() : 3600_000L;
        String instKey = RegistryKeys.instance(ns, appId, method, version, fingerprint);
        cacheX.write(instKey, JsonKit.toJsonString(instance), ttl);
        ApiAssets existing = loadExisting(prepared);
        persistEntry(prepared, instance);
        publishChange(
                existing == null ? RegistryChange.Action.REGISTER : RegistryChange.Action.UPDATE,
                prepared,
                existing,
                instance,
                previous);
    }

    /**
     * Removes a specific runtime instance from the registry.
     *
     * @param namespace   service namespace
     * @param app_id      application identifier
     * @param method      service method identifier
     * @param version     service version
     * @param fingerprint instance fingerprint
     */
    @Override
    public void deregisterInstance(String namespace, String app_id, String method, String version, String fingerprint) {
        String ns = normalizeNamespace(namespace);
        String instKey = RegistryKeys.instance(ns, app_id, method, version, fingerprint);
        ApiAssets service = findServiceByRoute(ns, app_id, method, version);
        Instance current = findInstance(ns, app_id, method, version, fingerprint);
        if (store != null && storeSupports(Trait.DURABLE) && storeSupports(Trait.INSTANCES)) {
            store.deleteInstance(registryType, ns, app_id, method, version, fingerprint);
        } else if (store != null) {
            capabilityFallback("deleteInstance", Trait.INSTANCES, "cache remove");
        }
        cacheX.remove(instKey);
        if (current != null) {
            ApiAssets prepared = normalizeService(
                    service == null ? synthesizeService(ns, app_id, method, version, current) : service,
                    current);
            publishChange(RegistryChange.Action.DEREGISTER, prepared, prepared, null, current);
        }
    }

    /**
     * Queries runtime instances matching the given criteria.
     *
     * @param namespace target namespace (blank uses {@code Normal.DEFAULT})
     * @param app_id    application identifier filter (null matches all)
     * @param method    service method filter (null matches all)
     * @param version   service version filter (null matches all)
     * @return list of matching instances
     */
    @Override
    public List<Instance> queryInstances(String namespace, String app_id, String method, String version) {
        String ns = normalizeNamespace(namespace);
        if (store != null && storeSupports(Trait.INSTANCES)) {
            List<Instance> persisted = store.queryInstances(registryType, ns, app_id, method, version);
            if (!persisted.isEmpty()) {
                warmInstanceCache(ns, app_id, method, version, persisted);
                return persisted;
            }
        } else if (store != null) {
            capabilityFallback("queryInstances", Trait.INSTANCES, "cache scan");
        }
        String prefix;
        if (app_id == null) {
            prefix = RegistryKeys.instancePrefix(ns, null, null, null);
        } else if (method == null) {
            prefix = RegistryKeys.instancePrefix(ns, app_id, null, null);
        } else if (version == null) {
            prefix = RegistryKeys.instancePrefix(ns, app_id, method, null);
        } else {
            prefix = RegistryKeys.instancePrefix(ns, app_id, method, version);
        }
        Map<String, Object> raw = cacheX.scan(prefix);
        List<Instance> result = new ArrayList<>();
        for (Map.Entry<String, Object> item : raw.entrySet()) {
            Object value = item.getValue();
            if (value instanceof String s) {
                Instance inst = JsonKit.toPojo(s, Instance.class);
                if (inst != null && (app_id == null || app_id.equals(inst.getApp_id()))
                        && (method == null || method.equals(inst.getMethod()))
                        && (version == null || version.equals(inst.getVersion()))) {
                    result.add(inst);
                }
            }
        }
        return result;
    }

    /**
     * Returns whether this registry persists and queries runtime service instances in addition to service definitions.
     *
     * @return {@code true} because API registry supports runtime instance management
     */
    @Override
    public boolean supportsInstances() {
        return true;
    }

    /**
     * Applies API-registry defaults shared by service registration and instance refresh.
     *
     * @param service  API service definition
     * @param instance optional runtime instance
     * @return normalized service definition
     */
    private ApiAssets normalizeService(ApiAssets service, Instance instance) {
        ApiAssets prepared = normalizeEntry(service);
        prepared.normalizeMeta();
        if (instance == null) {
            return prepared;
        }
        if (instance.getLeaseSeconds() == null) {
            instance.setLeaseSeconds(prepared.meta().getLeaseSeconds());
        }
        if (StringKit.isEmpty(instance.getApp_id())) {
            instance.setApp_id(prepared.getApp_id());
        }
        return prepared;
    }

    /**
     * Ensures that one runtime instance has a stable fingerprint derived from host and port when absent.
     *
     * @param instance runtime instance
     * @return ensured fingerprint
     */
    private String ensureFingerprint(Instance instance) {
        if (StringKit.isNotEmpty(instance.getFingerprint())) {
            return instance.getFingerprint();
        }
        if (instance.getHost() == null || instance.getPort() == null) {
            throw new IllegalArgumentException("Instance host and port are required when fingerprint is blank");
        }
        String fingerprint = Fingerprint.of(instance.getHost(), instance.getPort());
        instance.setFingerprint(fingerprint);
        return fingerprint;
    }

    /**
     * Fills missing runtime-instance identity fields from the owning service.
     *
     * @param service  API service definition
     * @param instance runtime instance
     */
    private void fillInstanceIdentity(ApiAssets service, Instance instance) {
        if (instance.getNamespace_id() == null) {
            instance.setNamespace_id(service.getNamespace_id());
        }
        if (StringKit.isEmpty(instance.getApp_id())) {
            instance.setApp_id(service.getApp_id());
        }
        if (StringKit.isEmpty(instance.getServiceId())) {
            instance.setServiceId(service.getId());
        }
        if (StringKit.isEmpty(instance.getMethod())) {
            instance.setMethod(service.getMethod());
        }
        if (StringKit.isEmpty(instance.getVersion())) {
            instance.setVersion(service.getVersion());
        }
    }

    /**
     * Finds a runtime instance by route identity and fingerprint.
     *
     * @param namespace   namespace
     * @param app_id      application identifier
     * @param method      method name
     * @param version     service version
     * @param fingerprint runtime instance fingerprint
     * @return matching runtime instance or {@code null}
     */
    private Instance findInstance(String namespace, String app_id, String method, String version, String fingerprint) {
        if (StringKit.isEmpty(fingerprint)) {
            return null;
        }
        return queryInstances(namespace, app_id, method, version).stream()
                .filter(item -> fingerprint.equals(item.getFingerprint())).findFirst().orElse(null);
    }

    /**
     * Finds a service definition by route identity.
     *
     * @param namespace namespace
     * @param app_id    application identifier
     * @param method    method name
     * @param version   service version
     * @return matching service definition or {@code null}
     */
    private ApiAssets findServiceByRoute(String namespace, String app_id, String method, String version) {
        if (store != null && storeSupports(Trait.ROUTE_QUERY)) {
            ApiAssets persisted = store.findByMethodVersion(registryType, namespace, app_id, method, version);
            if (persisted != null) {
                return persisted;
            }
        } else if (store != null) {
            capabilityFallback("findServiceByRoute", Trait.ROUTE_QUERY, "cache route scan");
        }
        Map<String, Object> raw = cacheX.scan(buildScanPrefix(namespace));
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        for (Object value : raw.values()) {
            ApiAssets asset = deserialize(value);
            if (asset != null && equals(app_id, asset.getApp_id()) && equals(method, asset.getMethod())
                    && equals(version, asset.getVersion())) {
                return asset;
            }
        }
        return null;
    }

    /**
     * Builds a synthetic service definition from a runtime instance.
     *
     * @param namespace namespace
     * @param app_id    application identifier
     * @param method    method name
     * @param version   service version
     * @param instance  runtime instance
     * @return synthetic service definition
     */
    private ApiAssets synthesizeService(
            String namespace,
            String app_id,
            String method,
            String version,
            Instance instance) {
        ApiAssets service = new ApiAssets();
        service.setNamespace_id(namespace);
        service.setApp_id(app_id);
        service.setMethod(method);
        service.setVersion(version);
        service.setId(StringKit.isNotEmpty(instance.getServiceId()) ? instance.getServiceId() : method + ":" + version);
        service.setType(Type.API.key());
        return service;
    }

    /**
     * Returns whether a route value matches an optional expectation.
     *
     * @param expected expected value, or {@code null} to match any value
     * @param actual   actual value
     * @return {@code true} when the value matches
     */
    private boolean equals(String expected, String actual) {
        return expected == null || expected.equals(actual);
    }

    /**
     * Warms the runtime-instance cache projection after durable reads.
     *
     * @param namespace namespace override
     * @param app_id    application identifier override
     * @param method    method override
     * @param version   version override
     * @param instances runtime instances to cache
     */
    private void warmInstanceCache(
            String namespace,
            String app_id,
            String method,
            String version,
            List<Instance> instances) {
        for (Instance instance : instances) {
            if (instance == null || instance.getFingerprint() == null) {
                continue;
            }
            String instNamespace = namespace != null ? namespace : instance.getNamespace_id();
            String instAppId = app_id != null ? app_id : instance.getApp_id();
            String instMethod = method != null ? method : instance.getMethod();
            String instVersion = version != null ? version : instance.getVersion();
            String instKey = RegistryKeys
                    .instance(instNamespace, instAppId, instMethod, instVersion, instance.getFingerprint());
            cacheX.write(instKey, JsonKit.toJsonString(instance), Builder.DEFAULT_HEALTH_INTERVAL_MS * 120);
        }
    }

}
