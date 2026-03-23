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
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Registry;
import org.miaixz.bus.cortex.registry.AbstractRegistry;
import org.miaixz.bus.cortex.registry.WatchManager;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Registry for API service definitions and runtime instances.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ApiRegistry extends AbstractRegistry<ApiDefinition> implements Registry<ApiDefinition> {

    /**
     * Creates an ApiRegistry backed by the given CacheX and WatchManager.
     *
     * @param cacheX       shared cache for persistence
     * @param watchManager watch subscription manager
     */
    public ApiRegistry(CacheX<String, Object> cacheX, WatchManager watchManager) {
        super(cacheX, watchManager, ApiDefinition.class, "service");
    }

    /**
     * Registers both a service definition and a runtime instance atomically.
     *
     * @param service  service definition to register
     * @param instance runtime instance associated with the service
     * @throws IllegalStateException if a different fingerprint is already registered for this service
     */
    @Override
    public void register(ApiDefinition service, Instance instance) {
        String ns = service.getNamespace() != null ? service.getNamespace() : Builder.DEFAULT_NAMESPACE;
        String fingerprint = instance.getFingerprint();
        String method = service.getMethod();
        String version = service.getVersion();

        String lockKey = Builder.REG_PREFIX + ns + ":unique:" + method + ":" + version;
        Object existing = cacheX.read(lockKey);
        if (existing != null && !existing.equals(fingerprint)) {
            throw new IllegalStateException("Service already registered with different fingerprint: " + existing);
        }

        long ttl = service.getTtl() > 0 ? service.getTtl() : 3600_000L;
        String instKey = Builder.REG_PREFIX + ns + ":instance:" + method + ":" + version + ":" + fingerprint;
        cacheX.write(instKey, JsonKit.toJsonString(instance), ttl);
        register(service);
        cacheX.write(lockKey, fingerprint, ttl);
    }

    /**
     * Removes a specific runtime instance from the registry.
     *
     * @param namespace   service namespace
     * @param method      service method identifier
     * @param version     service version
     * @param fingerprint instance fingerprint
     */
    @Override
    public void deregisterInstance(String namespace, String method, String version, String fingerprint) {
        String ns = namespace != null ? namespace : Builder.DEFAULT_NAMESPACE;
        String instKey = Builder.REG_PREFIX + ns + ":instance:" + method + ":" + version + ":" + fingerprint;
        String lockKey = Builder.REG_PREFIX + ns + ":unique:" + method + ":" + version;
        cacheX.remove(instKey, lockKey);
    }

    /**
     * Queries runtime instances matching the given criteria.
     *
     * @param namespace target namespace (null uses default)
     * @param method    service method filter (null matches all)
     * @param version   service version filter (null matches all)
     * @return list of matching instances
     */
    @Override
    public List<Instance> queryInstances(String namespace, String method, String version) {
        String ns = namespace != null ? namespace : Builder.DEFAULT_NAMESPACE;
        String prefix;
        if (method == null) {
            prefix = Builder.REG_PREFIX + ns + ":instance:";
        } else if (version == null) {
            prefix = Builder.REG_PREFIX + ns + ":instance:" + method + ":";
        } else {
            prefix = Builder.REG_PREFIX + ns + ":instance:" + method + ":" + version + ":";
        }
        Map<String, Object> raw = cacheX.scan(prefix);
        List<Instance> result = new ArrayList<>();
        for (Object value : raw.values()) {
            if (value instanceof String s) {
                Instance inst = JsonKit.toPojo(s, Instance.class);
                if (inst != null) {
                    result.add(inst);
                }
            }
        }
        return result;
    }

}
