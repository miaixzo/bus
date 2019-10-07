/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.storage.metric;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 默认的缓存实现
 *
 * @author Kimi Liu
 * @version 3.6.5
 * @since JDK 1.8
 */
public class DefaultCache implements Cache {

    /**
     * state cache
     */
    private static Map<String, CacheState> stateCache = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock(true);
    private final Lock writeLock = cacheLock.writeLock();
    private final Lock readLock = cacheLock.readLock();

    public DefaultCache() {
        if (CacheConfig.schedulePrune) {
            this.schedulePrune(CacheConfig.timeout);
        }
    }

    /**
     * 设置缓存
     *
     * @param key   缓存KEY
     * @param value 缓存内容
     */
    @Override
    public void set(String key, String value) {
        set(key, value, CacheConfig.timeout);
    }

    /**
     * 设置缓存
     *
     * @param key     缓存KEY
     * @param value   缓存内容
     * @param timeout 指定缓存过期时间（毫秒）
     */
    @Override
    public void set(String key, String value, long timeout) {
        writeLock.lock();
        try {
            stateCache.put(key, new CacheState(value, timeout));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 获取缓存
     *
     * @param key 缓存KEY
     * @return 缓存内容
     */
    @Override
    public String get(String key) {
        readLock.lock();
        try {
            CacheState cacheState = stateCache.get(key);
            if (null == cacheState || cacheState.isExpired()) {
                return null;
            }
            return cacheState.getState();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 是否存在key，如果对应key的value值已过期，也返回false
     *
     * @param key 缓存KEY
     * @return true：存在key，并且value没过期；false：key不存在或者已过期
     */
    @Override
    public boolean containsKey(String key) {
        readLock.lock();
        try {
            CacheState cacheState = stateCache.get(key);
            return null != cacheState && !cacheState.isExpired();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 清理过期的缓存
     */
    @Override
    public void pruneCache() {
        Iterator<CacheState> values = stateCache.values().iterator();
        CacheState cacheState;
        while (values.hasNext()) {
            cacheState = values.next();
            if (cacheState.isExpired()) {
                values.remove();
            }
        }
    }

    /**
     * 定时清理
     *
     * @param delay 间隔时长，单位毫秒
     */
    public void schedulePrune(long delay) {
        CacheScheduler.INSTANCE.schedule(this::pruneCache, delay);
    }

    @Getter
    @Setter
    private class CacheState implements Serializable {
        private String state;
        private long expire;

        CacheState(String state, long expire) {
            this.state = state;
            // 实际过期时间等于当前时间加上有效期
            this.expire = System.currentTimeMillis() + expire;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > this.expire;
        }
    }

}
