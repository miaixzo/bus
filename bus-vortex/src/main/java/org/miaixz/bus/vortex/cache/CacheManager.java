/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.vortex.metrics.CacheStats;

/**
 * A generic two-level cache manager combining L1 (ConcurrentHashMap) and L2 (Caffeine) caches.
 * <p>
 * This cache manager provides a high-performance caching solution with the following characteristics:
 * </p>
 * <ul>
 * <li>L1 Cache: ConcurrentHashMap for ultra-fast access to hot data</li>
 * <li>L2 Cache: Caffeine for large-capacity storage with LRU eviction</li>
 * <li>Query Flow: L1 → L2 → Cache miss</li>
 * <li>Write Strategy: Update both L1 and L2 simultaneously</li>
 * <li>Eviction Policy: Automatic LRU eviction in L2 cache</li>
 * </ul>
 *
 * <p>
 * <b>Configuration Parameters:</b>
 * </p>
 * <ul>
 * <li>Cache configuration is obtained from {@link Holder}</li>
 * <li>Cache size: {@link Holder#getCacheSize()} (default: 10000)</li>
 * <li>Cache expiration: {@link Holder#getCacheExpireMs()} (default: 300000ms)</li>
 * </ul>
 *
 * <p>
 * <b>Performance Monitoring:</b>
 * </p>
 * <ul>
 * <li>Automatic statistics collection for hit/miss counts</li>
 * <li>Optional integration with {@link Monitor}</li>
 * <li>Statistics query support via {@link CacheStats}</li>
 * </ul>
 *
 * <p>
 * <b>Use Cases:</b>
 * </p>
 * <ul>
 * <li>Two-level cache for {@link org.miaixz.bus.vortex.registry.AbstractRegistry}</li>
 * <li>Permission information caching</li>
 * <li>Configuration data caching</li>
 * <li>Any scenario requiring high-performance caching</li>
 * </ul>
 *
 * <p>
 * <b>Example Usage:</b>
 * </p>
 * 
 * <pre>{@code
 * // Create cache manager
 * CacheManager<String, Assets> cacheManager = new CacheManager<>();
 *
 * // Optional: Set performance monitor
 * cacheManager.setPerformanceMonitor(new DefaultMonitor());
 *
 * // Query cache (automatic L1 → L2 flow)
 * Assets asset = cacheManager.get("user.getProfile:1.0.0");
 *
 * // Write to cache (updates both L1 and L2)
 * cacheManager.put("user.getProfile:1.0.0", asset);
 *
 * // Get statistics
 * CacheStats stats = cacheManager.getStats();
 * System.out.println("Hit rate: " + stats.getHitRate());
 * }</pre>
 *
 * @param <K> the type of keys maintained by this cache manager
 * @param <V> the type of mapped values
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheManager<K, V> {

    /**
     * 一级缓存：ConcurrentHashMap（热数据，极快访问）
     */
    private final Map<K, V> cache;

    /**
     * 二级缓存：Caffeine（全量数据，LRU淘汰）
     */
    private final CacheX<K, V> cachex;

    /**
     * L2缓存配置
     */
    private final long cacheSize;
    private final long cacheExpireMs;

    /**
     * 访问统计
     */
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);

    /**
     * 性能监控器（可选）
     */
    private volatile Monitor monitor;

    /**
     * 构造函数：从 Holder 获取配置
     */
    public CacheManager() {
        this.cacheSize = Holder.getCacheSize();
        this.cacheExpireMs = Holder.getCacheExpireMs();

        this.cache = new ConcurrentHashMap<>();
        this.cachex = new GuavaCache<>(cacheSize, cacheExpireMs);

        Logger.debug(
                "CacheManager初始化: L1=ConcurrentHashMap, L2=Caffeine(size={}, expireMs={})",
                cacheSize,
                cacheExpireMs);
    }

    /**
     * 构造函数：自定义配置
     *
     * @param cacheSize     L2缓存最大容量
     * @param cacheExpireMs L2缓存过期时间（毫秒）
     */
    public CacheManager(long cacheSize, long cacheExpireMs) {
        this.cacheSize = cacheSize;
        this.cacheExpireMs = cacheExpireMs;

        this.cache = new ConcurrentHashMap<>();
        this.cachex = new CaffeineCache(cacheSize, cacheExpireMs);

        Logger.debug(
                "CacheManager初始化: L1=ConcurrentHashMap, L2=Caffeine(size={}, expireMs={})",
                cacheSize,
                cacheExpireMs);
    }

    /**
     * 设置性能监控器
     *
     * @param monitor 性能监控器
     */
    public void setPerformanceMonitor(Monitor monitor) {
        this.monitor = monitor;
        Logger.debug("CacheManager性能监控器已设置: {}", monitor.getClass().getSimpleName());
    }

    /**
     * 查询缓存（L1 → L2 → 未命中）
     *
     * @param key 键
     * @return 值，如果未找到返回null
     */
    public V get(K key) {
        long startTime = System.nanoTime();

        try {
            // 1. 查询L1缓存
            V value = this.cache.get(key);

            if (value != null) {
                // L1命中
                hitCount.incrementAndGet();

                if (monitor != null) {
                    monitor.access(String.valueOf(key), true, System.nanoTime() - startTime);
                }

                return value;
            }

            // 2. L1未命中，查询L2缓存
            value = this.cachex.read(key);

            if (value != null) {
                // L2命中，同步到L1
                this.cache.put(key, value);
                hitCount.incrementAndGet();

                if (monitor != null) {
                    monitor.access(String.valueOf(key), true, System.nanoTime() - startTime);
                }

                return value;
            }

            // 3. 完全未命中
            missCount.incrementAndGet();

            if (monitor != null) {
                monitor.access(String.valueOf(key), false, System.nanoTime() - startTime);
            }

            return null;

        } catch (Exception e) {
            Logger.error("CacheManager查询失败: key={}, error={}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 写入缓存（同时更新L1+L2）
     *
     * @param key   键
     * @param value 值
     */
    public void put(K key, V value) {
        try {
            this.cache.put(key, value);
            this.cachex.write(key, value, cacheExpireMs);
        } catch (Exception e) {
            Logger.error("CacheManager写入失败: key={}, error={}", key, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 移除缓存（同时清理L1+L2）
     *
     * @param key 键
     */
    public void remove(K key) {
        try {
            this.cache.remove(key);
            this.cachex.remove(key);
        } catch (Exception e) {
            Logger.error("CacheManager移除失败: key={}, error={}", key, e.getMessage(), e);
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        this.cache.clear();
        this.cachex.clear();
        this.hitCount.set(0);
        this.missCount.set(0);
    }

    /**
     * 获取L1缓存大小
     *
     * @return L1缓存当前大小
     */
    public long getL1Size() {
        return this.cache.size();
    }

    /**
     * 获取统计信息
     *
     * @return 缓存统计信息
     */
    public CacheStats getStats() {
        long hits = hitCount.get();
        long misses = missCount.get();
        long total = hits + misses;

        return CacheStats.builder().hitCount(hits).missCount(misses).hitRate(total > 0 ? (double) hits / total : 0.0)
                .cacheSize(cache.size()).build();
    }

    /**
     * 计算命中率
     *
     * @return 命中率（0.0 - 1.0）
     */
    public double hitRate() {
        long hits = hitCount.get();
        long misses = missCount.get();
        long total = hits + misses;

        return total > 0 ? (double) hits / total : 0.0;
    }

}
