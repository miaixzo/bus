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

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;

import lombok.Getter;
import lombok.Setter;

/**
 * 内存缓存支持
 * <p>
 * 基于 ConcurrentHashMap 实现的线程安全内存缓存，支持最大容量、访问后过期时间、写入后过期时间和初始容量配置。 提供定时清理过期缓存、批量读写操作和统计信息获取功能。
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class MemoryCache<K, V> implements CacheX<K, V> {

    /**
     * 默认缓存过期时间：3分钟
     * <p>
     * 鉴于授权过程中，根据个人的操作习惯或授权平台（如 Google）的差异，授权流程耗时不同， 但通常不会过长。本缓存工具默认过期时间为3分钟，超过3分钟的缓存将失效并被删除。
     * </p>
     */
    public static long timeout = 3_600_000;

    /**
     * 是否开启定时清理过期缓存的任务
     */
    public static boolean schedulePrune = true;

    /**
     * 缓存存储 Map，键为缓存键，值为缓存状态
     */
    private final Map<K, CacheState> map;

    /**
     * 读写锁，用于保证线程安全
     */
    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock(true);

    /**
     * 写锁
     */
    private final Lock writeLock = cacheLock.writeLock();

    /**
     * 读锁
     */
    private final Lock readLock = cacheLock.readLock();

    /**
     * 最大缓存条目数
     */
    private final long maximumSize;

    /**
     * 访问后过期时间（毫秒）
     */
    private final long expireAfterAccess;

    /**
     * 写入后过期时间（毫秒）
     */
    private final long expireAfterWrite;

    /**
     * 请求次数统计
     */
    private final AtomicLong requestCount = new AtomicLong();

    /**
     * 命中次数统计
     */
    private final AtomicLong hitCount = new AtomicLong();

    /**
     * 默认构造方法
     * <p>
     * 创建内存缓存实例，使用默认配置（最大容量 1000，写入后过期时间 3 分钟，无访问后过期时间）， 如果开启了定时清理，则启动定时清理任务。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>();
     * cache.write("key1", "value1", timeout);
     * }</pre>
     */
    public MemoryCache() {
        this.map = new ConcurrentHashMap<>(16);
        this.maximumSize = 1000;
        this.expireAfterWrite = timeout;
        this.expireAfterAccess = 0;
        if (schedulePrune) {
            this.schedulePrune(timeout);
        }
    }

    /**
     * 构造函数
     * <p>
     * 使用指定的最大容量和过期时间创建缓存实例。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>(1000, 600_000);
     * cache.write("key1", "value1", 600_000);
     * }</pre>
     *
     * @param size   最大缓存条目数
     * @param expire 写入后过期时间（毫秒）
     */
    public MemoryCache(long size, long expire) {
        this.map = new ConcurrentHashMap<>(16);
        this.maximumSize = size;
        this.expireAfterWrite = expire;
        this.expireAfterAccess = 0;
        if (schedulePrune) {
            this.schedulePrune(expire);
        }
    }

    /**
     * 构造函数
     * <p>
     * 使用 Properties 配置创建缓存实例，支持最大容量、访问后过期时间、写入后过期时间和初始容量。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * Properties props = new Properties();
     * props.setProperty("maximumSize", "1000");
     * props.setProperty("expireAfterWrite", "600000");
     * props.setProperty("expireAfterAccess", "300000");
     * props.setProperty("initialCapacity", "16");
     * MemoryCache<String, String> cache = new MemoryCache<>(props);
     * }</pre>
     *
     * @param properties 配置属性
     */
    public MemoryCache(Properties properties) {
        String prefix = StringKit.isNotEmpty(properties.getProperty("prefix")) ? properties.getProperty("prefix")
                : Normal.EMPTY;
        String maximumSize = properties.getProperty(prefix + "maximumSize");
        String expireAfterAccess = properties.getProperty(prefix + "expireAfterAccess");
        String expireAfterWrite = properties.getProperty(prefix + "expireAfterWrite");
        String initialCapacity = properties.getProperty(prefix + "initialCapacity");

        this.maximumSize = StringKit.isNotEmpty(maximumSize) ? Long.parseLong(maximumSize) : 1000;
        this.expireAfterWrite = StringKit.isNotEmpty(expireAfterWrite) ? Long.parseLong(expireAfterWrite) : timeout;
        this.expireAfterAccess = StringKit.isNotEmpty(expireAfterAccess) ? Long.parseLong(expireAfterAccess) : 0;
        int initCapacity = StringKit.isNotEmpty(initialCapacity) ? Integer.parseInt(initialCapacity) : 16;

        this.map = new ConcurrentHashMap<>(initCapacity);
        if (schedulePrune) {
            this.schedulePrune(Math.min(this.expireAfterWrite,
                    this.expireAfterAccess > 0 ? this.expireAfterAccess : Long.MAX_VALUE));
        }
    }

    /**
     * 获取缓存
     * <p>
     * 从缓存中读取指定键的值，如果键不存在或已过期则返回 null。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>();
     * cache.write("key1", "value1", timeout);
     * String value = cache.read("key1");
     * System.out.println("值: " + value);
     * }</pre>
     *
     * @param key 缓存键
     * @return 缓存值，或 null 如果不存在或已过期
     */
    @Override
    public V read(K key) {
        readLock.lock();
        try {
            requestCount.incrementAndGet();
            CacheState cacheState = map.get(key);
            if (cacheState == null || cacheState.isExpired(expireAfterWrite, expireAfterAccess)) {
                return null;
            }
            cacheState.updateAccessTime();
            hitCount.incrementAndGet();
            return (V) cacheState.getState();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 批量获取缓存
     * <p>
     * 从缓存中批量读取指定键集合的值，不存在的键对应的值为 null。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>();
     * cache.write("key1", "value1", timeout);
     * cache.write("key2", "value2", timeout);
     * Map<String, String> values = cache.read(Arrays.asList("key1", "key2"));
     * System.out.println("批量值: " + values);
     * }</pre>
     *
     * @param keys 缓存键集合
     * @return 缓存键值映射
     */
    @Override
    public Map<K, V> read(Collection<K> keys) {
        Map<K, V> subCache = new HashMap<>(keys.size());
        for (K key : keys) {
            subCache.put(key, read(key));
        }
        return subCache;
    }

    /**
     * 批量设置缓存
     * <p>
     * 向缓存中批量写入键值对，超出最大容量时移除最旧的条目。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>();
     * Map<String, String> map = new HashMap<>();
     * map.put("key1", "value1");
     * map.put("key2", "value2");
     * cache.write(map, timeout);
     * }</pre>
     *
     * @param keyValueMap 缓存键值映射
     * @param expire      过期时间（毫秒）
     */
    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        if (MapKit.isNotEmpty(keyValueMap)) {
            keyValueMap.forEach((key, value) -> write(key, value, expire));
        }
    }

    /**
     * 设置缓存
     * <p>
     * 向缓存中写入单个键值对，超出最大容量时移除最旧的条目。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>();
     * cache.write("key1", "value1", timeout);
     * System.out.println("已写入: key1");
     * }</pre>
     *
     * @param key    缓存键
     * @param value  缓存值
     * @param expire 过期时间（毫秒）
     */
    @Override
    public void write(K key, V value, long expire) {
        writeLock.lock();
        try {
            if (map.size() >= maximumSize && !map.containsKey(key)) {
                evictOldest();
            }
            map.put(key, new CacheState(value, expire));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 判断缓存中是否存在指定的键
     * <p>
     * 检查指定键是否存在且未过期。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>();
     * cache.write("key1", "value1", timeout);
     * boolean exists = cache.containsKey("key1");
     * System.out.println("键存在: " + exists);
     * }</pre>
     *
     * @param key 缓存键
     * @return 如果存在且未过期则返回 true，否则返回 false
     */
    @Override
    public boolean containsKey(K key) {
        readLock.lock();
        try {
            CacheState cacheState = map.get(key);
            return cacheState != null && !cacheState.isExpired(expireAfterWrite, expireAfterAccess);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 清理过期的缓存
     * <p>
     * 移除所有已过期的缓存条目（包括写入后过期和访问后过期）。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>();
     * cache.clear();
     * System.out.println("缓存已清理");
     * }</pre>
     */
    @Override
    public void clear() {
        writeLock.lock();
        try {
            Iterator<Map.Entry<K, CacheState>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<K, CacheState> entry = iterator.next();
                if (entry.getValue().isExpired(expireAfterWrite, expireAfterAccess)) {
                    iterator.remove();
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 移除指定的缓存
     * <p>
     * 移除指定键的缓存条目。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>();
     * cache.write("key1", "value1", timeout);
     * cache.remove("key1");
     * System.out.println("已移除: key1");
     * }</pre>
     *
     * @param keys 要移除的缓存键
     */
    @Override
    public void remove(K... keys) {
        writeLock.lock();
        try {
            for (K key : keys) {
                map.remove(key);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 定时清理过期缓存
     * <p>
     * 调度定时任务以清理过期缓存，清理间隔为过期时间。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>();
     * cache.schedulePrune(600_000);
     * System.out.println("定时清理已调度");
     * }</pre>
     *
     * @param delay 间隔时长，单位毫秒
     */
    public void schedulePrune(long delay) {
        CacheScheduler.INSTANCE.schedule(this::clear, delay);
    }

    /**
     * 获取缓存统计信息
     * <p>
     * 返回缓存的统计信息，包括请求次数、命中次数、命中率和当前大小。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>();
     * cache.read("key1");
     * String stats = cache.getStats();
     * System.out.println("统计信息: " + stats);
     * }</pre>
     *
     * @return 缓存统计信息字符串
     */
    public String getStats() {
        long requests = requestCount.get();
        long hits = hitCount.get();
        double hitRate = requests == 0 ? 0.0 : (double) hits / requests;
        return String.format("MemoryCacheStats[requests=%d, hits=%d, hitRate=%.2f%%, size=%d]", requests, hits,
                hitRate * 100, map.size());
    }

    /**
     * 获取缓存估算大小
     * <p>
     * 返回当前缓存中的条目数量。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>();
     * cache.write("key1", "value1", timeout);
     * long size = cache.estimatedSize();
     * System.out.println("缓存大小: " + size);
     * }</pre>
     *
     * @return 缓存估算大小
     */
    public long estimatedSize() {
        return map.size();
    }

    /**
     * 获取内部缓存实例
     * <p>
     * 返回底层的 ConcurrentHashMap 实例，用于高级操作。 示例代码：
     * </p>
     * 
     * <pre>{@code
     * MemoryCache<String, String> cache = new MemoryCache<>();
     * Map<String, CacheState> nativeCache = cache.getNativeCache();
     * System.out.println("内部缓存: " + nativeCache);
     * }</pre>
     *
     * @return 内部缓存实例
     */
    public Map<K, CacheState> getNativeCache() {
        return map;
    }

    /**
     * 移除最旧的缓存条目
     * <p>
     * 当缓存超出最大容量时，移除最早写入的条目。
     * </p>
     */
    private void evictOldest() {
        writeLock.lock();
        try {
            Map.Entry<K, CacheState> oldest = null;
            long oldestTime = Long.MAX_VALUE;
            for (Map.Entry<K, CacheState> entry : map.entrySet()) {
                long writeTime = entry.getValue().getWriteTime();
                if (writeTime < oldestTime) {
                    oldestTime = writeTime;
                    oldest = entry;
                }
            }
            if (oldest != null) {
                map.remove(oldest.getKey());
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 缓存调度器枚举
     * <p>
     * 单例模式，负责调度定时清理任务。
     * </p>
     */
    enum CacheScheduler {
        /**
         * 当前实例
         */
        INSTANCE;

        /**
         * 缓存任务编号
         */
        private AtomicInteger cacheTaskNumber = new AtomicInteger(1);

        /**
         * 调度器执行服务
         */
        private ScheduledExecutorService scheduler;

        /**
         * 私有构造方法
         * <p>
         * 初始化调度器。
         * </p>
         */
        CacheScheduler() {
            of();
        }

        /**
         * 初始化调度器
         * <p>
         * 创建线程池用于调度定时任务。
         * </p>
         */
        private void of() {
            this.shutdown();
            this.scheduler = new ScheduledThreadPoolExecutor(10,
                    r -> new Thread(r, String.format("Cache-Task-%s", cacheTaskNumber.getAndIncrement())));
        }

        /**
         * 关闭调度器
         * <p>
         * 停止调度器的所有任务。
         * </p>
         */
        public void shutdown() {
            if (scheduler != null) {
                scheduler.shutdown();
            }
        }

        /**
         * 调度定时任务
         * <p>
         * 安排定时任务以固定间隔执行。 示例代码：
         * </p>
         * 
         * <pre>{@code
         * CacheScheduler.INSTANCE.schedule(() -> System.out.println("清理任务"), 600_000);
         * }</pre>
         *
         * @param task  任务
         * @param delay 延迟时间，单位毫秒
         */
        public void schedule(Runnable task, long delay) {
            this.scheduler.scheduleAtFixedRate(task, delay, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 缓存状态类
     * <p>
     * 用于存储缓存值、写入时间、最后访问时间和过期时间，并提供判断是否过期的方法。
     * </p>
     */
    @Getter
    @Setter
    private static class CacheState implements Serializable {
        /**
         * 缓存值
         */
        private Object state;

        /**
         * 写入时间戳
         */
        private final long writeTime;

        /**
         * 最后访问时间戳
         */
        private long lastAccessTime;

        /**
         * 写入后过期时间戳
         */
        private final long expireAfterWrite;

        /**
         * 构造方法
         * <p>
         * 初始化缓存值和过期时间，记录写入时间和初始访问时间。
         * </p>
         *
         * @param state  缓存值
         * @param expire 写入后过期时间（毫秒）
         */
        CacheState(Object state, long expire) {
            this.state = state;
            this.writeTime = System.currentTimeMillis();
            this.lastAccessTime = this.writeTime;
            this.expireAfterWrite = this.writeTime + expire;
        }

        /**
         * 更新最后访问时间
         * <p>
         * 在读取缓存时更新最后访问时间。
         * </p>
         */
        void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        /**
         * 判断缓存是否过期
         * <p>
         * 根据写入后过期时间和访问后过期时间判断缓存是否有效。
         * </p>
         *
         * @param expireAfterWrite  写入后过期时间（毫秒）
         * @param expireAfterAccess 访问后过期时间（毫秒）
         * @return 如果已过期则返回 true，否则返回 false
         */
        boolean isExpired(long expireAfterWrite, long expireAfterAccess) {
            long currentTime = System.currentTimeMillis();
            if (expireAfterWrite > 0 && currentTime > this.expireAfterWrite) {
                return true;
            }
            if (expireAfterAccess > 0 && currentTime > this.lastAccessTime + expireAfterAccess) {
                return true;
            }
            return false;
        }
    }

}