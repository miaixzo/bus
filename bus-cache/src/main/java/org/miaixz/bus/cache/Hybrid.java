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
package org.miaixz.bus.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Cache wrapper that preserves scan, counter and renew semantics when the primary backend does not provide them
 * directly.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Hybrid implements CacheX<String, Object> {

    /**
     * Default maximum number of local cache entries retained for extended runtime semantics.
     */
    public static final long DEFAULT_MAXIMUM_SIZE = 1024L;

    /**
     * Default expiration used by the local mirror cache.
     */
    public static final long DEFAULT_EXPIRE_MS = 180000L;

    /**
     * Primary cache backend selected by the cache module.
     */
    private final CacheX<String, Object> cache;

    /**
     * Optional local mirror used when the primary backend lacks scan or counter support.
     */
    private final CacheX<String, Object> mirror;

    /**
     * Whether scan and key lookup should be delegated to the primary backend.
     */
    private final boolean primaryScan;

    /**
     * Whether counter operations should be delegated to the primary backend.
     */
    private final boolean primaryCounter;

    /**
     * Creates a cache wrapper around a fully capable delegate.
     *
     * @param cache delegate cache
     */
    public Hybrid(CacheX<String, Object> cache) {
        this(cache, true, true, null);
    }

    /**
     * Creates a cache wrapper around a delegate plus optional capability mirror.
     *
     * @param cache          delegate cache
     * @param primaryScan    whether scan can use the primary backend
     * @param primaryCounter whether increment can use the primary backend
     * @param mirror         local mirror for capability fallback
     */
    public Hybrid(CacheX<String, Object> cache, boolean primaryScan, boolean primaryCounter,
            CacheX<String, Object> mirror) {
        this.cache = cache;
        this.primaryScan = primaryScan;
        this.primaryCounter = primaryCounter;
        this.mirror = mirror;
    }

    /**
     * Reads one value from the primary backend and falls back to the mirror when needed.
     *
     * @param key cache key
     * @return cached value or {@code null}
     */
    @Override
    public Object read(String key) {
        Object value = cache.read(key);
        return value != null || mirror == null ? value : mirror.read(key);
    }

    /**
     * Reads multiple values from the primary backend and fills any misses from the mirror.
     *
     * @param keys cache keys
     * @return cached values
     */
    @Override
    public Map<String, Object> read(Collection<String> keys) {
        Map<String, Object> values = cache.read(keys);
        if (mirror == null || values.size() == keys.size()) {
            return values;
        }
        Map<String, Object> fallback = mirror.read(keys);
        if (fallback.isEmpty()) {
            return values;
        }
        fallback.putAll(values);
        return fallback;
    }

    /**
     * Returns whether the key exists in either the primary backend or the mirror.
     *
     * @param key cache key
     * @return {@code true} when the key exists
     */
    @Override
    public boolean containsKey(String key) {
        return cache.containsKey(key) || mirror != null && mirror.containsKey(key);
    }

    /**
     * Writes one entry to the primary backend and mirrors it when a mirror is configured.
     *
     * @param key    cache key
     * @param value  cache value
     * @param expire expiration in milliseconds
     */
    @Override
    public void write(String key, Object value, long expire) {
        cache.write(key, value, expire);
        if (mirror != null) {
            mirror.write(key, value, expire);
        }
    }

    /**
     * Writes multiple entries to the primary backend and mirrors them when a mirror is configured.
     *
     * @param map    cache entries
     * @param expire expiration in milliseconds
     */
    @Override
    public void write(Map<String, Object> map, long expire) {
        cache.write(map, expire);
        if (mirror != null) {
            mirror.write(map, expire);
        }
    }

    /**
     * Removes keys from both the primary backend and the mirror when present.
     *
     * @param keys cache keys
     */
    @Override
    public void remove(String... keys) {
        cache.remove(keys);
        if (mirror != null) {
            mirror.remove(keys);
        }
    }

    /**
     * Clears both the primary backend and the mirror when present.
     */
    @Override
    public void clear() {
        cache.clear();
        if (mirror != null) {
            mirror.clear();
        }
    }

    /**
     * Scans entries from the primary backend when supported, otherwise from the mirror.
     *
     * @param prefix cache key prefix
     * @return matching entries
     */
    @Override
    public Map<String, Object> scan(String prefix) {
        return primaryScan ? cache.scan(prefix) : mirror == null ? Map.of() : mirror.scan(prefix);
    }

    /**
     * Returns keys from the primary backend when supported, otherwise from the mirror.
     *
     * @param prefix cache key prefix
     * @return matching keys
     */
    @Override
    public List<String> keys(String prefix) {
        return primaryScan ? cache.keys(prefix) : mirror == null ? List.of() : mirror.keys(prefix);
    }

    /**
     * Increments a counter on the primary backend when supported, otherwise on the mirror.
     *
     * @param key counter key
     * @return incremented counter value
     */
    @Override
    public long increment(String key) {
        return primaryCounter ? cache.increment(key) : mirror == null ? cache.increment(key) : mirror.increment(key);
    }

    /**
     * Refreshes TTL on the primary backend and mirror, succeeding when either side is refreshed.
     *
     * @param key    cache key
     * @param expire expiration in milliseconds
     * @return {@code true} when at least one backend refreshed the key
     */
    @Override
    public boolean renew(String key, long expire) {
        boolean renewed = cache.renew(key, expire);
        return mirror == null ? renewed : mirror.renew(key, expire) || renewed;
    }

}
