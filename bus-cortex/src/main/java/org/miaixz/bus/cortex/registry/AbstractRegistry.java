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
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.Registry;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Base registry implementation providing CacheX-backed CRUD and watch support.
 *
 * @param <T> registered asset type
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractRegistry<T extends Assets> implements Registry<T> {

    /**
     * Shared CacheX backend for reading and writing entries.
     */
    protected final CacheX<String, Object> cacheX;
    /**
     * Watch subscription manager shared with the registry.
     */
    protected final WatchManager watchManager;
    /**
     * Concrete Java type of managed entries, used for deserialization.
     */
    protected final Class<T> type;
    /**
     * Cache key segment that identifies this registrar's species bucket.
     */
    protected final String typePrefix;

    /**
     * Constructs an AbstractRegistry with shared infrastructure components.
     *
     * @param cacheX       shared cache used for persistence
     * @param watchManager watch subscription manager
     * @param type         Java type of the managed entries
     * @param typePrefix   cache key segment identifying the species bucket
     */
    protected AbstractRegistry(CacheX<String, Object> cacheX, WatchManager watchManager, Class<T> type,
            String typePrefix) {
        this.cacheX = cacheX;
        this.watchManager = watchManager;
        this.type = type;
        this.typePrefix = typePrefix;
    }

    /**
     * Builds the full cache key for an entry.
     *
     * @param namespace entry namespace
     * @param id        entry identifier
     * @return cache key string
     */
    protected String buildKey(String namespace, String id) {
        return Builder.REG_PREFIX + namespace + ":" + typePrefix + ":" + id;
    }

    /**
     * Builds the cache key prefix for scanning all entries in a namespace.
     *
     * @param namespace entry namespace
     * @return cache key prefix string
     */
    protected String buildScanPrefix(String namespace) {
        return Builder.REG_PREFIX + namespace + ":" + typePrefix + ":";
    }

    /**
     * Persists an entry to the cache with its configured TTL.
     *
     * @param entry entry to register
     */
    @Override
    public void register(T entry) {
        long ttl = entry.getTtl() > 0 ? entry.getTtl() : 3600_000L;
        String key = buildKey(entry.getNamespace(), entry.getId());
        cacheX.write(key, JsonKit.toJsonString(entry), ttl);
    }

    /**
     * Removes an entry from the cache by namespace and ID.
     *
     * @param namespace entry namespace
     * @param id        entry identifier
     */
    @Override
    public void deregister(String namespace, String id) {
        cacheX.remove(buildKey(namespace, id));
    }

    /**
     * Queries entries matching the given criteria with offset/limit pagination.
     *
     * @param vector vector parameters including namespace, id filter, offset and limit
     * @return paginated list of matching entries
     */
    @Override
    public List<T> query(Vector vector) {
        String ns = vector.getNamespace() != null ? vector.getNamespace() : Builder.DEFAULT_NAMESPACE;
        String prefix = buildScanPrefix(ns);
        Map<String, Object> raw = cacheX.scan(prefix);
        List<T> result = new ArrayList<>();
        for (Object value : raw.values()) {
            T entry = deserialize(value);
            if (entry == null) {
                continue;
            }
            if (vector.getId() != null && !vector.getId().equals(entry.getId())) {
                continue;
            }
            result.add(entry);
        }
        int offset = vector.getOffset() > 0 ? vector.getOffset() : 0;
        int limit = vector.getLimit() > 0 ? vector.getLimit() : 100;
        int fromIdx = Math.min(offset, result.size());
        int toIdx = Math.min(fromIdx + limit, result.size());
        return result.subList(fromIdx, toIdx);
    }

    /**
     * Subscribes to changes matching the query and returns a watch identifier.
     *
     * @param vector   vector defining what to watch
     * @param listener listener to invoke on change
     * @return watch identifier used to cancel the subscription
     */
    @Override
    public String watch(Vector vector, Listener<T> listener) {
        return watchManager.add(vector, listener);
    }

    /**
     * Cancels a watch subscription by ID.
     *
     * @param watchId watch identifier previously returned by {@link #watch}
     */
    @Override
    public void unwatch(String watchId) {
        watchManager.remove(watchId);
    }

    /**
     * Deserializes a raw cache value into the managed asset type.
     *
     * @param raw raw cache value (String JSON or typed object)
     * @return deserialized entry, or null if raw is null or parsing fails
     */
    protected T deserialize(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof String s) {
            return JsonKit.toPojo(s, type);
        }
        return type.cast(raw);
    }

}
