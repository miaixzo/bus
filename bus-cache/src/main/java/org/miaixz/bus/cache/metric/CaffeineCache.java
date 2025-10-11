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
package org.miaixz.bus.cache.metric;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * An implementation of {@link CacheX} backed by a Caffeine {@link LoadingCache}.
 * <p>
 * This class provides a high-performance, in-memory caching solution known for its near-optimal hit rates. It supports
 * configuration of maximum size, time-based expiration, and other advanced features.
 * </p>
 *
 * @param <K> The type of keys.
 * @param <V> The type of values.
 * @author Kimi Liu
 * @since Java 17+
 */
public class CaffeineCache<K, V> implements CacheX<K, V> {

    /**
     * The underlying Caffeine cache instance.
     */
    private final LoadingCache<K, V> cache;

    /**
     * Constructs a {@code CaffeineCache} with a specified maximum size and expiration time.
     * <p>
     * The cache is configured to expire entries after a fixed duration since they were last written.
     * </p>
     *
     * @param size   The maximum number of entries the cache can hold.
     * @param expire The expiration time in milliseconds, applied as `expireAfterWrite`.
     */
    public CaffeineCache(long size, long expire) {
        this.cache = Caffeine.newBuilder().maximumSize(size).expireAfterWrite(expire, TimeUnit.MILLISECONDS)
                .build(key -> null); // Default loader returns null for misses.
    }

    /**
     * Constructs a {@code CaffeineCache} from a {@link Properties} object.
     * <p>
     * Supported properties:
     * <ul>
     * <li>`maximumSize`: Max entries (default: 1000).</li>
     * <li>`expireAfterAccess`: TTI in ms.</li>
     * <li>`expireAfterWrite`: TTL in ms.</li>
     * <li>`initialCapacity`: Initial map size.</li>
     * </ul>
     *
     * @param properties The configuration properties.
     */
    public CaffeineCache(Properties properties) {
        String prefix = properties.getProperty("prefix", Normal.EMPTY);
        String maximumSize = properties.getProperty(prefix + "maximumSize");
        String expireAfterAccess = properties.getProperty(prefix + "expireAfterAccess");
        String expireAfterWrite = properties.getProperty(prefix + "expireAfterWrite");
        String initialCapacity = properties.getProperty(prefix + "initialCapacity");

        this.cache = Caffeine.newBuilder()
                .maximumSize(StringKit.isNotEmpty(maximumSize) ? Long.parseLong(maximumSize) : 1000)
                .expireAfterAccess(Long.parseLong(expireAfterAccess), TimeUnit.MILLISECONDS)
                .expireAfterWrite(Long.parseLong(expireAfterWrite), TimeUnit.MILLISECONDS)
                .initialCapacity(Integer.parseInt(initialCapacity)).build(key -> null);
    }

    /**
     * Constructs a {@code CaffeineCache} with a custom {@link CacheLoader} for automatic loading.
     *
     * @param size        The maximum number of entries the cache can hold.
     * @param expire      The expiration time in milliseconds, applied as `expireAfterWrite`.
     * @param cacheLoader The loader to use for fetching values on cache misses.
     */
    public CaffeineCache(long size, long expire, CacheLoader<K, V> cacheLoader) {
        this.cache = Caffeine.newBuilder().maximumSize(size).expireAfterWrite(expire, TimeUnit.MILLISECONDS)
                .build(cacheLoader);
    }

    /**
     * Reads a single value from the cache.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value associated with the key, or {@code null} if the key is not present.
     */
    @Override
    public V read(K key) {
        return this.cache.getIfPresent(key);
    }

    /**
     * Reads multiple values from the cache in a batch.
     *
     * @param keys A collection of keys to retrieve.
     * @return A map of keys to their corresponding values for all keys present in the cache.
     */
    @Override
    public Map<K, V> read(Collection<K> keys) {
        return this.cache.getAllPresent(keys);
    }

    /**
     * Writes a key-value pair to the cache.
     * <p>
     * <strong>Note:</strong> This implementation ignores the {@code expire} parameter. The expiration policy is
     * determined by the cache-wide settings configured at construction time.
     * </p>
     *
     * @param key    The key to write.
     * @param value  The value to associate with the key.
     * @param expire This parameter is ignored.
     */
    @Override
    public void write(K key, V value, long expire) {
        this.cache.put(key, value);
    }

    /**
     * Writes multiple key-value pairs to the cache.
     * <p>
     * <strong>Note:</strong> This implementation ignores the {@code expire} parameter. The expiration policy is
     * determined by the cache-wide settings configured at construction time.
     * </p>
     *
     * @param keyValueMap A map of key-value pairs to write.
     * @param expire      This parameter is ignored.
     */
    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        this.cache.putAll(keyValueMap);
    }

    /**
     * Removes one or more entries from the cache.
     *
     * @param keys The keys of the entries to remove.
     */
    @Override
    public void remove(K... keys) {
        this.cache.invalidateAll(Arrays.asList(keys));
    }

    /**
     * Clears all entries from the cache.
     * <p>
     * This method invalidates all entries and then performs a cleanup to remove them.
     * </p>
     */
    @Override
    public void clear() {
        this.cache.invalidateAll();
        this.cache.cleanUp();
    }

    /**
     * Gets a string representation of the current cache statistics.
     *
     * @return A string containing detailed cache statistics.
     */
    public String getStats() {
        return this.cache.stats().toString();
    }

    /**
     * Returns the approximate number of entries in this cache.
     *
     * @return The number of entries.
     */
    public long estimatedSize() {
        return this.cache.estimatedSize();
    }

    /**
     * Returns the underlying Caffeine {@link Cache} instance for advanced operations.
     *
     * @return The native Caffeine cache instance.
     */
    public Cache<K, V> getNativeCache() {
        return this.cache;
    }

}
