/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.core.lang;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * 简单缓存,无超时实现,使用{@link WeakHashMap}实现缓存自动清理
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 5.5.9
 * @since JDK 1.8+
 */
public class SimpleCache<K, V> {

    /**
     * 池
     */
    private final Map<K, V> cache = new WeakHashMap<>();

    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private final ReadLock readLock = cacheLock.readLock();
    private final WriteLock writeLock = cacheLock.writeLock();

    /**
     * 从缓存池中查找值
     *
     * @param key 键
     * @return 值
     */
    public V get(K key) {
        // 尝试读取缓存
        readLock.lock();
        V value;
        try {
            value = cache.get(key);
        } finally {
            readLock.unlock();
        }
        return value;
    }

    /**
     * 放入缓存
     *
     * @param key   键
     * @param value 值
     * @return 值
     */
    public V put(K key, V value) {
        writeLock.lock();
        try {
            cache.put(key, value);
        } finally {
            writeLock.unlock();
        }
        return value;
    }

    /**
     * 移除缓存
     *
     * @param key 键
     * @return 移除的值
     */
    public V remove(K key) {
        writeLock.lock();
        try {
            return cache.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 清空缓存池
     */
    public void clear() {
        writeLock.lock();
        try {
            this.cache.clear();
        } finally {
            writeLock.unlock();
        }
    }

}
