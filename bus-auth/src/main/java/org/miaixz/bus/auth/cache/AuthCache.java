/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 17+
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
