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
 * 缓存管理器
 * <p>
 * 负责管理多个缓存实例，提供单键和多键的读写、删除操作。 使用默认缓存和缓存池来优化性能，减少对象创建开销。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Manage {

    /**
     * 默认缓存实例，使用CachePair存储缓存名称和缓存实现
     */
    private CachePair<String, CacheX> defaultCache;

    /**
     * 缓存池，存储多个缓存实例，键为缓存名称，值为CachePair（包含缓存名称和缓存实现）
     */
    private Map<String, CachePair<String, CacheX>> cachePool = new ConcurrentHashMap<>();

    /**
     * 缓存命中率统计组件
     */
    private Metrics metrics;

    /**
     * 构造方法
     *
     * @param caches  缓存映射集合，键为缓存名称，值为缓存实现
     * @param metrics 缓存命中率统计组件
     */
    public Manage(Map<String, CacheX> caches, Metrics metrics) {
        this.metrics = metrics;
        setCachePool(caches);
    }

    /**
     * 设置缓存池
     * <p>
     * 初始化默认缓存和缓存池，使用传入的缓存映射集合。 默认缓存使用映射中的第一个条目。
     * </p>
     *
     * @param caches 缓存映射集合，键为缓存名称，值为缓存实现
     */
    public void setCachePool(Map<String, CacheX> caches) {
        if (caches == null || caches.isEmpty()) {
            throw new IllegalArgumentException("Cache map cannot be null or empty");
        }

        // 设置默认缓存为第一个缓存条目
        Map.Entry<String, CacheX> entry = caches.entrySet().iterator().next();
        this.defaultCache = CachePair.of(entry.getKey(), entry.getValue());

        // 填充缓存池
        caches.forEach((name, cache) -> this.cachePool.put(name, CachePair.of(name, cache)));

        Logger.debug("Initialized cache pool with {} caches, default cache: {}", caches.size(), defaultCache.getLeft());
    }

    /**
     * 读取单个缓存值
     *
     * @param cache 缓存名称，如果为空则使用默认缓存
     * @param key   缓存键
     * @return 缓存值，如果读取失败则返回null
     */
    public Object readSingle(String cache, String key) {
        try {
            CachePair<String, CacheX> cacheImpl = getCacheImpl(cache);
            long start = System.currentTimeMillis();
            Object result = cacheImpl.getRight().read(key);
            Logger.info("cache [{}] read single cost: [{}] ms", cacheImpl.getLeft(),
                    (System.currentTimeMillis() - start));
            return result;
        } catch (Throwable e) {
            Logger.error("read single cache failed, key: {} ", key, e);
            return null;
        }
    }

    /**
     * 写入单个缓存值
     *
     * @param cache  缓存名称，如果为空则使用默认缓存
     * @param key    缓存键
     * @param value  缓存值，如果为null则不执行写入
     * @param expire 过期时间（毫秒）
     */
    public void writeSingle(String cache, String key, Object value, int expire) {
        if (null != value) {
            try {
                CachePair<String, CacheX> cacheImpl = getCacheImpl(cache);
                long start = System.currentTimeMillis();
                cacheImpl.getRight().write(key, value, expire);
                Logger.info("cache [{}] write single cost: [{}] ms", cacheImpl.getLeft(),
                        (System.currentTimeMillis() - start));
            } catch (Throwable e) {
                Logger.error("write single cache failed, key: {} ", key, e);
            }
        }
    }

    /**
     * 批量读取缓存值
     *
     * @param cache 缓存名称，如果为空则使用默认缓存
     * @param keys  缓存键集合
     * @return CacheKeys对象，包含命中的键值对和未命中的键集合
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
                Logger.info("cache [{}] read batch cost: [{}] ms", cacheImpl.getLeft(),
                        (System.currentTimeMillis() - start));

                // 收集未命中的键，保持顺序
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
     * 批量写入缓存值
     *
     * @param cache       缓存名称，如果为空则使用默认缓存
     * @param keyValueMap 键值映射
     * @param expire      过期时间（毫秒）
     */
    public void writeBatch(String cache, Map<String, Object> keyValueMap, int expire) {
        try {
            CachePair<String, CacheX> cacheImpl = getCacheImpl(cache);
            long start = System.currentTimeMillis();
            cacheImpl.getRight().write(keyValueMap, expire);
            Logger.info("cache [{}] write batch cost: [{}] ms", cacheImpl.getLeft(),
                    (System.currentTimeMillis() - start));
        } catch (Exception e) {
            Logger.error("write map multi cache failed, keys: {}", keyValueMap.keySet(), e);
        }
    }

    /**
     * 删除缓存
     *
     * @param cache 缓存名称，如果为空则使用默认缓存
     * @param keys  要删除的缓存键
     */
    public void remove(String cache, String... keys) {
        if (null != keys && keys.length != 0) {
            try {
                CachePair<String, CacheX> cacheImpl = getCacheImpl(cache);
                long start = System.currentTimeMillis();
                cacheImpl.getRight().remove(keys);
                Logger.info("cache [{}] remove cost: [{}] ms", cacheImpl.getLeft(),
                        (System.currentTimeMillis() - start));
            } catch (Throwable e) {
                Logger.error("remove cache failed, keys: {}: ", keys, e);
            }
        }
    }

    /**
     * 获取缓存实现
     *
     * @param cacheName 缓存名称，如果为空则返回默认缓存
     * @return 缓存实现（CachePair包含缓存名称和缓存实例）
     * @throws InternalException 如果找不到指定名称的缓存实现
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
     * 获取缓存命中率统计组件
     *
     * @return 缓存命中率统计组件
     */
    public Metrics getHitting() {
        return metrics;
    }

    /**
     * 设置缓存命中率统计组件
     *
     * @param metrics 缓存命中率统计组件
     */
    public void setHitting(Metrics metrics) {
        this.metrics = metrics;
    }

}