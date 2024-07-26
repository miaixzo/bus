/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import lombok.Getter;
import lombok.Setter;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.xyz.MapKit;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 内存缓存支持
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MemoryCache implements CacheX {

    /**
     * 默认缓存过期时间：3分钟 鉴于授权过程中,根据个人的操作习惯,或者授权平台的不同(google等),每个授权流程的耗时也有差异,不过单个授权流程一般不会太长
     * 本缓存工具默认的过期时间设置为3分钟,即程序默认认为3分钟内的授权有效,超过3分钟则默认失效,失效后删除
     */
    public static long timeout = 3 * 60 * 1000;

    /**
     * 是否开启定时{@link MemoryCache#clear()} ()}的任务
     */
    public static boolean schedulePrune = true;

    private static Map<String, CacheState> map = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock(true);
    private final Lock writeLock = cacheLock.writeLock();
    private final Lock readLock = cacheLock.readLock();

    public MemoryCache() {
        if (schedulePrune) {
            this.schedulePrune(timeout);
        }
    }

    /**
     * 获取缓存
     *
     * @param key 缓存KEY
     * @return 缓存内容
     */
    @Override
    public Object read(String key) {
        readLock.lock();
        try {
            CacheState cacheState = map.get(key);
            if (null == cacheState || cacheState.isExpired()) {
                return null;
            }
            return cacheState.getState();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Map<String, Object> read(Collection<String> keys) {
        Map<String, Object> subCache = new HashMap<>(keys.size());
        for (String key : keys) {
            subCache.put(key, read(key));
        }
        return subCache;
    }

    /**
     * 设置缓存
     *
     * @param keyValueMap 缓存内容
     * @param expire      指定缓存过期时间(毫秒)
     */
    @Override
    public void write(Map<String, Object> keyValueMap, long expire) {
        if (MapKit.isNotEmpty(keyValueMap)) {
            keyValueMap.forEach((key, value) -> write(key, value, expire));
        }
    }

    /**
     * 设置缓存
     *
     * @param key    缓存KEY
     * @param value  缓存内容
     * @param expire 指定缓存过期时间(毫秒)
     */
    @Override
    public void write(String key, Object value, long expire) {
        writeLock.lock();
        try {
            map.put(key, new CacheState(value, expire));
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    /**
     * 清理过期的缓存
     */
    @Override
    public void clear() {
        Iterator<CacheState> values = map.values().iterator();
        while (values.hasNext()) {
            CacheState cache = values.next();
            if (cache.isExpired()) {
                values.remove();
            }
        }
    }

    /**
     * 清理过期的缓存
     */
    @Override
    public void remove(String... keys) {
        for (String key : keys) {
            map.remove(key);
        }
    }

    /**
     * 定时清理
     *
     * @param delay 间隔时长,单位毫秒
     */
    public void schedulePrune(long delay) {
        CacheScheduler.INSTANCE.schedule(this::clear, delay);
    }

    enum CacheScheduler {

        /**
         * 当前实例
         */
        INSTANCE;

        private AtomicInteger cacheTaskNumber = new AtomicInteger(1);
        private ScheduledExecutorService scheduler;

        CacheScheduler() {
            of();
        }

        private void of() {
            this.shutdown();
            this.scheduler = new ScheduledThreadPoolExecutor(10,
                    r -> new Thread(r, String.format("OAuth-Task-%s", cacheTaskNumber.getAndIncrement())));
        }

        public void shutdown() {
            if (null != scheduler) {
                this.scheduler.shutdown();
            }
        }

        public void schedule(Runnable task, long delay) {
            this.scheduler.scheduleAtFixedRate(task, delay, delay, TimeUnit.MILLISECONDS);
        }

    }

    @Getter
    @Setter
    private class CacheState implements Serializable {
        private Object state;
        private long expire;

        CacheState(Object state, long expire) {
            this.state = state;
            // 实际过期时间等于当前时间加上有效期
            this.expire = System.currentTimeMillis() + expire;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > this.expire;
        }
    }

}
