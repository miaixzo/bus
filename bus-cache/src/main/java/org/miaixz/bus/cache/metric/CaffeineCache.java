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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.CacheLoader;
import org.miaixz.bus.cache.CacheX;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Caffeine 缓存支持 Caffeine 是一个高性能的 Java 缓存库，提供接近最优的命中率
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CaffeineCache<K, V> implements CacheX<K, V> {

    /**
     * Caffeine 缓存实例
     */
    private final Cache<K, V> caffeineCache;

    /**
     * 构造函数
     *
     * @param size   最大缓存条目数
     * @param expire 过期时间（毫秒）
     */
    public CaffeineCache(long size, long expire) {
        this.caffeineCache = Caffeine.newBuilder()
                // 设置最大缓存条目数
                .maximumSize(size)
                // 设置写入后过期时间
                .expireAfterWrite(expire, TimeUnit.MILLISECONDS)
                // 构建缓存
                .build();
    }

    /**
     * 构造函数（支持异步加载）
     *
     * @param size        最大缓存条目数
     * @param expire      过期时间（毫秒）
     * @param cacheLoader 缓存加载器
     */
    public CaffeineCache(long size, long expire, CacheLoader<K, V> cacheLoader) {
        this.caffeineCache = Caffeine.newBuilder()
                // 设置最大缓存条目数
                .maximumSize(size)
                // 设置写入后过期时间
                .expireAfterWrite(expire, TimeUnit.MILLISECONDS)
                // 设置缓存加载器
                .build(cacheLoader);
    }

    @Override
    public V read(K key) {
        // 使用 getIfPresent 方法，如果键不存在则返回 null
        return caffeineCache.getIfPresent(key);
    }

    @Override
    public Map<K, V> read(Collection<K> keys) {
        // 使用 getAllPresent 方法批量获取存在的键值对
        return caffeineCache.getAllPresent(keys);
    }

    @Override
    public void write(K key, V value, long expire) {
        // Caffeine 的 put 方法不直接支持单个键的过期时间设置
        // 过期时间在构建缓存时统一设置，这里忽略参数中的 expire
        caffeineCache.put(key, value);
    }

    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        // 批量写入键值对，同样忽略参数中的 expire
        caffeineCache.putAll(keyValueMap);
    }

    @Override
    public void remove(K... keys) {
        // 使用 invalidateAll 方法批量删除指定的键
        caffeineCache.invalidateAll(Arrays.asList(keys));
    }

    @Override
    public void clear() {
        // 使用 invalidateAll 方法清空整个缓存
        caffeineCache.invalidateAll();
        // 执行维护操作，清理被标记为删除的条目
        caffeineCache.cleanUp();
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    public String getStats() {
        return caffeineCache.stats().toString();
    }

    /**
     * 获取缓存估算大小
     *
     * @return 缓存估算大小
     */
    public long estimatedSize() {
        return caffeineCache.estimatedSize();
    }

    /**
     * 获取内部缓存实例（用于高级操作）
     *
     * @return 内部缓存实例
     */
    public Cache<K, V> getNativeCache() {
        return caffeineCache;
    }

}