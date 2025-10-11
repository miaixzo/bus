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
package org.miaixz.bus.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.miaixz.bus.cache.magic.CacheKeys;
import org.miaixz.bus.cache.magic.CachePair;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * Manages multiple cache instances and provides a unified interface for cache operations.
 * <p>
 * This class acts as a facade for interacting with various cache implementations, routing operations to the appropriate
 * cache instance based on a provided cache name. It maintains a pool of caches and a default cache for convenience.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Manage {

    /**
     * The default cache instance, used when no specific cache name is provided.
     */
    private CachePair<String, CacheX> defaultCache;

    /**
     * A thread-safe pool of named cache instances.
     */
    private Map<String, CachePair<String, CacheX>> cachePool = new ConcurrentHashMap<>();

    /**
     * The component responsible for tracking cache metrics, such as hit and miss rates.
     */
    private Metrics metrics;

    /**
     * Constructs a new cache manager with a given set of cache instances and a metrics tracker.
     *
     * @param caches  A map where keys are cache names and values are the corresponding {@link CacheX} instances. The
     *                first entry in the map will be set as the default cache.
     * @param metrics The {@link Metrics} instance for tracking cache statistics.
     * @throws IllegalArgumentException if the {@code caches} map is null or empty.
     */
    public Manage(Map<String, CacheX> caches, Metrics metrics) {
        this.metrics = metrics;
        setCachePool(caches);
    }

    /**
     * Initializes or replaces the cache pool with a new set of cache instances.
     * <p>
     * This method populates the internal cache pool and sets the default cache. The default cache is determined by the
     * iteration order of the provided map's entry set.
     * </p>
     *
     * @param caches A map of cache names to {@link CacheX} instances.
     * @throws IllegalArgumentException if the {@code caches} map is null or empty.
     */
    public void setCachePool(Map<String, CacheX> caches) {
        if (caches == null || caches.isEmpty()) {
            throw new IllegalArgumentException("Cache map cannot be null or empty");
        }

        // Set the default cache to the first cache entry
        Map.Entry<String, CacheX> entry = caches.entrySet().iterator().next();
        this.defaultCache = CachePair.of(entry.getKey(), entry.getValue());

        // Populate the cache pool
        caches.forEach((name, cache) -> this.cachePool.put(name, CachePair.of(name, cache)));

        Logger.debug("Initialized cache pool with {} caches, default cache: {}", caches.size(), defaultCache.getLeft());
    }

    /**
     * Reads a single value from the specified cache.
     * <p>
     * If the cache name is null or empty, the default cache is used. Any exceptions during the read operation are
     * logged, and {@code null} is returned.
     * </p>
     *
     * @param cache The name of the cache to read from.
     * @param key   The key whose associated value is to be returned.
     * @return The value to which the specified key is mapped, or {@code null} if the key is not found or an error
     *         occurs.
     */
    public Object readSingle(String cache, String key) {
        try {
            CachePair<String, CacheX> cacheImpl = getCacheImpl(cache);
            long start = System.currentTimeMillis();
            Object result = cacheImpl.getRight().read(key);
            Logger.info(
                    "cache [{}] read single cost: [{}] ms",
                    cacheImpl.getLeft(),
                    (System.currentTimeMillis() - start));
            return result;
        } catch (Throwable e) {
            Logger.error("read single cache failed, key: {} ", key, e);
            return null;
        }
    }

    /**
     * Writes a single key-value pair to the specified cache with an expiration time.
     * <p>
     * If the cache name is null or empty, the default cache is used. The operation is skipped if the provided value is
     * {@code null}. Any exceptions during the write operation are logged.
     * </p>
     *
     * @param cache  The name of the cache to write to.
     * @param key    The key with which the specified value is to be associated.
     * @param value  The value to be associated with the specified key.
     * @param expire The expiration time in milliseconds.
     */
    public void writeSingle(String cache, String key, Object value, int expire) {
        if (null != value) {
            try {
                CachePair<String, CacheX> cacheImpl = getCacheImpl(cache);
                long start = System.currentTimeMillis();
                cacheImpl.getRight().write(key, value, expire);
                Logger.info(
                        "cache [{}] write single cost: [{}] ms",
                        cacheImpl.getLeft(),
                        (System.currentTimeMillis() - start));
            } catch (Throwable e) {
                Logger.error("write single cache failed, key: {} ", key, e);
            }
        }
    }

    /**
     * Performs a batch read from the specified cache for a collection of keys.
     * <p>
     * This method categorizes the keys into two groups: those found in the cache (hits) and those not found (misses).
     * If the keys collection is empty, an empty result is returned.
     * </p>
     *
     * @param cache The name of the cache to read from; uses the default cache if null or empty.
     * @param keys  A collection of keys to retrieve.
     * @return A {@link CacheKeys} object containing a map of found key-value pairs and a set of keys that were not
     *         found.
     */
    public CacheKeys readBatch(String cache, Collection<String> keys) {
        CacheKeys cacheKeys;
        if (keys.isEmpty()) {
            cacheKeys = new CacheKeys();
        } else {
            try {
                CachePair<String, CacheX> cacheImpl = getCacheImpl(cache);
                long start = System.currentTimeMillis();
                Map<String, Object> cacheMap = cacheImpl.getRight().read(keys);
                Logger.info(
                        "cache [{}] read batch cost: [{}] ms",
                        cacheImpl.getLeft(),
                        (System.currentTimeMillis() - start));

                // Collect missed keys, maintaining order
                Map<String, Object> hitValueMap = new LinkedHashMap<>();
                Set<String> notHitKeys = new LinkedHashSet<>();

                for (String key : keys) {
                    Object value = cacheMap.get(key);
                    if (null == value) {
                        notHitKeys.add(key);
                    } else {
                        hitValueMap.put(key, value);
                    }
                }

                cacheKeys = new CacheKeys(hitValueMap, notHitKeys);
            } catch (Throwable e) {
                Logger.error("read multi cache failed, keys: {}", keys, e);
                cacheKeys = new CacheKeys();
            }
        }
        return cacheKeys;
    }

    /**
     * Performs a batch write to the specified cache.
     * <p>
     * Writes all entries from the given map to the cache with a common expiration time. If the cache name is null or
     * empty, the default cache is used. Any exceptions during the write operation are logged.
     * </p>
     *
     * @param cache       The name of the cache to write to.
     * @param keyValueMap A map of key-value pairs to be stored in the cache.
     * @param expire      The expiration time in milliseconds for all entries.
     */
    public void writeBatch(String cache, Map<String, Object> keyValueMap, int expire) {
        try {
            CachePair<String, CacheX> cacheImpl = getCacheImpl(cache);
            long start = System.currentTimeMillis();
            cacheImpl.getRight().write(keyValueMap, expire);
            Logger.info(
                    "cache [{}] write batch cost: [{}] ms",
                    cacheImpl.getLeft(),
                    (System.currentTimeMillis() - start));
        } catch (Exception e) {
            Logger.error("write map multi cache failed, keys: {}", keyValueMap.keySet(), e);
        }
    }

    /**
     * Removes one or more entries from the specified cache.
     * <p>
     * If the cache name is null or empty, the default cache is used. The operation is ignored if the keys array is null
     * or empty.
     * </p>
     *
     * @param cache The name of the cache from which to remove entries.
     * @param keys  The keys of the entries to be removed.
     */
    public void remove(String cache, String... keys) {
        if (null != keys && keys.length != 0) {
            try {
                CachePair<String, CacheX> cacheImpl = getCacheImpl(cache);
                long start = System.currentTimeMillis();
                cacheImpl.getRight().remove(keys);
                Logger.info(
                        "cache [{}] remove cost: [{}] ms",
                        cacheImpl.getLeft(),
                        (System.currentTimeMillis() - start));
            } catch (Throwable e) {
                Logger.error("remove cache failed, keys: {}: ", keys, e);
            }
        }
    }

    /**
     * Retrieves a specific cache implementation from the pool.
     * <p>
     * If the provided {@code cacheName} is null or empty, the default cache instance is returned.
     * </p>
     *
     * @param cacheName The name of the cache to retrieve.
     * @return The {@link CachePair} containing the name and instance of the requested cache.
     * @throws InternalException if no cache is found for the given name.
     */
    private CachePair<String, CacheX> getCacheImpl(String cacheName) {
        if (StringKit.isEmpty(cacheName)) {
            return defaultCache;
        } else {
            return cachePool.computeIfAbsent(cacheName, (key) -> {
                throw new InternalException(StringKit.format("no cache implementation named [%s].", key));
            });
        }
    }

    /**
     * Gets the metrics component used for tracking cache statistics.
     *
     * @return The current {@link Metrics} instance.
     */
    public Metrics getHitting() {
        return metrics;
    }

    /**
     * Sets the metrics component for tracking cache statistics.
     *
     * @param metrics The {@link Metrics} instance to be used.
     */
    public void setHitting(Metrics metrics) {
        this.metrics = metrics;
    }

}
