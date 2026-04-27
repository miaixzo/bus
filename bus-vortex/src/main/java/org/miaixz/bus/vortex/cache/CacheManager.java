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
package org.miaixz.bus.vortex.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.metric.CaffeineCache;
import org.miaixz.bus.cache.metric.GuavaCache;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.Monitor;
import org.miaixz.bus.vortex.metric.CacheStats;

/**
 * Generic two-level cache manager.
 * <p>
 * The first-level cache uses {@link ConcurrentHashMap} for hot data, while the second-level cache uses {@link CacheX}
 * for larger-capacity storage. Reads follow the order "L1 -> L2 -> miss", and writes and removals update both cache
 * levels.
 * </p>
 *
 * <p>
 * Default configuration is loaded from {@link Holder}:
 * </p>
 * <ul>
 * <li>Cache size: {@link Holder#getCacheSize()}</li>
 * <li>Cache expiration: {@link Holder#getCacheExpireMs()}</li>
 * </ul>
 *
 * <p>
 * This manager also supports hit-rate statistics and optional performance monitoring.
 * </p>
 *
 * @param <K> cache key type
 * @param <V> cache value type
 * @author Kimi Liu
 * @since Java 21+
 */
public class CacheManager<K, V> {

    /**
     * Level-1 cache for hot data with very fast access.
     */
    private final Map<K, V> cache;

    /**
     * Level-2 cache implementation.
     */
    private final CacheX<K, V> cachex;

    /**
     * Level-2 cache configuration.
     */
    private final long cacheSize;
    /**
     * Level-2 cache expiration in milliseconds.
     */
    private final long cacheExpireMs;

    /**
     * Access statistics.
     */
    private final AtomicLong hitCount = new AtomicLong(0);
    /**
     * Cache miss statistics.
     */
    private final AtomicLong missCount = new AtomicLong(0);

    /**
     * Optional performance monitor.
     */
    private volatile Monitor monitor;

    /**
     * Creates a cache manager with global default configuration.
     */
    public CacheManager() {
        this.cacheSize = Holder.getCacheSize();
        this.cacheExpireMs = Holder.getCacheExpireMs();

        this.cache = new ConcurrentHashMap<>();
        this.cachex = new GuavaCache<>(cacheSize, cacheExpireMs);

        Logger.debug(
                "CacheManager initialized: L1=ConcurrentHashMap, L2=GuavaCache(size={}, expireMs={})",
                cacheSize,
                cacheExpireMs);
    }

    /**
     * Creates a cache manager with explicit configuration.
     *
     * @param cacheSize     maximum level-2 cache size
     * @param cacheExpireMs level-2 cache expiration in milliseconds
     */
    public CacheManager(long cacheSize, long cacheExpireMs) {
        this.cacheSize = cacheSize;
        this.cacheExpireMs = cacheExpireMs;

        this.cache = new ConcurrentHashMap<>();
        this.cachex = new CaffeineCache(cacheSize, cacheExpireMs);

        Logger.debug(
                "CacheManager initialized: L1=ConcurrentHashMap, L2=CaffeineCache(size={}, expireMs={})",
                cacheSize,
                cacheExpireMs);
    }

    /**
     * Sets the performance monitor.
     *
     * @param monitor performance monitor
     */
    public void setPerformanceMonitor(Monitor monitor) {
        this.monitor = monitor;
        Logger.debug("CacheManager performance monitor configured: {}", monitor.getClass().getSimpleName());
    }

    /**
     * Reads a value from the cache.
     *
     * @param key cache key
     * @return cached value, or {@code null} if not found
     */
    public V get(K key) {
        long startTime = System.nanoTime();

        try {
            V value = this.cache.get(key);

            if (value != null) {
                hitCount.incrementAndGet();

                if (monitor != null) {
                    monitor.access(String.valueOf(key), true, System.nanoTime() - startTime);
                }

                return value;
            }

            value = this.cachex.read(key);

            if (value != null) {
                this.cache.put(key, value);
                hitCount.incrementAndGet();

                if (monitor != null) {
                    monitor.access(String.valueOf(key), true, System.nanoTime() - startTime);
                }

                return value;
            }

            missCount.incrementAndGet();

            if (monitor != null) {
                monitor.access(String.valueOf(key), false, System.nanoTime() - startTime);
            }

            return null;

        } catch (Exception e) {
            Logger.error("Cache read failed: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Writes a value to the cache and updates both cache levels.
     *
     * @param key   cache key
     * @param value cache value
     */
    public void put(K key, V value) {
        try {
            this.cache.put(key, value);
            this.cachex.write(key, value, cacheExpireMs);
        } catch (Exception e) {
            Logger.error("Cache write failed: key={}, error={}", key, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Removes a value from the cache and clears both cache levels.
     *
     * @param key cache key
     */
    public void remove(K key) {
        try {
            this.cache.remove(key);
            this.cachex.remove(key);
        } catch (Exception e) {
            Logger.error("Cache removal failed: key={}, error={}", key, e.getMessage(), e);
        }
    }

    /**
     * Clears all cached data and resets statistics.
     */
    public void clear() {
        this.cache.clear();
        this.cachex.clear();
        this.hitCount.set(0);
        this.missCount.set(0);
    }

    /**
     * Returns the current level-1 cache size.
     *
     * @return current level-1 cache size
     */
    public long getL1Size() {
        return this.cache.size();
    }

    /**
     * Returns current cache statistics.
     *
     * @return cache statistics
     */
    public CacheStats getStats() {
        long hits = hitCount.get();
        long misses = missCount.get();
        long total = hits + misses;

        return CacheStats.builder().hitCount(hits).missCount(misses).hitRate(total > 0 ? (double) hits / total : 0.0)
                .cacheSize(cache.size()).build();
    }

    /**
     * Calculates the current cache hit rate.
     *
     * @return hit rate between {@code 0.0} and {@code 1.0}
     */
    public double hitRate() {
        long hits = hitCount.get();
        long misses = missCount.get();
        long total = hits + misses;

        return total > 0 ? (double) hits / total : 0.0;
    }

}
