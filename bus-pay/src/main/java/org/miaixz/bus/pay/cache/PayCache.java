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
package org.miaixz.bus.pay.cache;

import java.util.Collection;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.metric.MemoryCache;

/**
 * Default cache implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum PayCache implements CacheX<String, Object> {

    /**
     * The singleton instance.
     */
    INSTANCE;

    /**
     * The underlying cache implementation.
     */
    private final CacheX<String, Object> cache;

    /**
     * Private constructor to initialize the cache.
     */
    PayCache() {
        cache = new MemoryCache();
    }

    /**
     * Gets the cached content.
     *
     * @param key The cache key.
     * @return The cached content.
     */
    @Override
    public Object read(String key) {
        return cache.read(key);
    }

    /**
     * Gets the cached content for multiple keys.
     *
     * @param keys A collection of cache keys.
     * @return A map of keys to their cached content.
     */
    @Override
    public Map<String, Object> read(Collection<String> keys) {
        return this.cache.read(keys);
    }

    /**
     * Checks if a key exists. If the value for the key has expired, it also returns false.
     *
     * @param key The cache key.
     * @return {@code true} if the key exists and the value has not expired; {@code false} otherwise.
     */
    @Override
    public boolean containsKey(String key) {
        return this.cache.containsKey(key);
    }

    /**
     * Writes multiple key-value pairs to the cache with a specified expiration time.
     *
     * @param map    The map of key-value pairs to cache.
     * @param expire The expiration time in milliseconds.
     */
    @Override
    public void write(Map<String, Object> map, long expire) {
        this.cache.write(map, expire);
    }

    /**
     * Writes content to the cache.
     *
     * @param key    The cache key.
     * @param value  The content to cache.
     * @param expire The specified expiration time in milliseconds.
     */
    @Override
    public void write(String key, Object value, long expire) {
        this.cache.write(key, value, expire);
    }

    /**
     * Removes multiple keys from the cache.
     *
     * @param keys The keys to remove.
     */
    @Override
    public void remove(String... keys) {
        this.cache.remove(keys);
    }

    /**
     * Clears all entries from the cache.
     */
    @Override
    public void clear() {
        this.cache.clear();
    }

}
