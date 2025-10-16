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
package org.miaixz.bus.pay.cache;

import java.util.Collection;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.metric.MemoryCache;

/**
 * Default cache implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
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
