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
package org.miaixz.bus.auth.cache;

import java.util.Collection;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.metric.MemoryCache;

/**
 * Default cache implementation for authentication-related data. This enum provides a singleton instance of a cache that
 * uses {@link MemoryCache} internally.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum AuthCache implements CacheX<String, Object> {

    /**
     * The singleton instance of this cache.
     */
    INSTANCE;

    private CacheX<String, Object> cache;

    /**
     * Constructs the {@code AuthCache} enum instance. Initializes the internal cache with a new {@link MemoryCache}
     * instance.
     */
    AuthCache() {
        cache = new MemoryCache();
    }

    /**
     * Retrieves the cached content associated with the given key.
     *
     * @param key the cache key
     * @return the cached content, or null if not found or expired
     */
    @Override
    public Object read(String key) {
        return cache.read(key);
    }

    /**
     * Retrieves a map of cached contents for the given collection of keys.
     *
     * @param keys a collection of cache keys
     * @return a map where keys are cache keys and values are their corresponding cached contents
     */
    @Override
    public Map<String, Object> read(Collection<String> keys) {
        return this.cache.read(keys);
    }

    /**
     * Checks if the cache contains the specified key and if its value has not expired.
     *
     * @param key the cache key
     * @return true if the key exists and its value is not expired; false otherwise
     */
    @Override
    public boolean containsKey(String key) {
        return this.cache.containsKey(key);
    }

    /**
     * Writes a map of key-value pairs to the cache with a specified expiration time.
     *
     * @param map    the map of key-value pairs to write
     * @param expire the expiration time for the entries in milliseconds
     */
    @Override
    public void write(Map<String, Object> map, long expire) {
        this.cache.write(map, expire);
    }

    /**
     * Stores content in the cache with a specified key and expiration time.
     *
     * @param key    the cache key
     * @param value  the content to cache
     * @param expire the expiration time for the content in milliseconds
     */
    @Override
    public void write(String key, Object value, long expire) {
        this.cache.write(key, value, expire);
    }

    /**
     * Removes entries from the cache corresponding to the given keys.
     *
     * @param keys an array of keys to remove
     */
    @Override
    public void remove(String[] keys) {
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
