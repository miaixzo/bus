/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.core.lang;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 简单缓存,无超时实现,使用{@link WeakHashMap}实现缓存自动清理
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
public class SimpleCache<K, V> implements Iterable<Map.Entry<K, V>>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 缓存池
     */
    private final Map<K, V> cache;
    /**
     * 乐观读写锁
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 构造，默认使用{@link WeakHashMap}实现缓存自动清理
     */
    public SimpleCache() {
        this(new WeakHashMap<>());
    }

    /**
     * 通过自定义Map初始化，可以自定义缓存实现
     * 比如使用{@link WeakHashMap}则会自动清理key，使用HashMap则不会清理
     * 同时，传入的Map对象也可以自带初始化的键值对，防止在get时创建
     *
     * @param initMap 初始Map，用于定义Map类型
     */
    public SimpleCache(Map<K, V> initMap) {
        this.cache = initMap;
    }

    /**
     * 从缓存池中查找值
     *
     * @param key 键
     * @return 值
     */
    public V get(K key) {
        lock.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 从缓存中获得对象，当对象不在缓存中或已经过期返回Func0回调产生的对象
     *
     * @param key      键
     * @param supplier 如果不存在回调方法，用于生产值对象
     * @return 值对象
     */
    public V get(K key, Func.Func0<V> supplier) {
        V v = get(key);

        if (null == v && null != supplier) {
            lock.writeLock().lock();
            try {
                v = cache.get(key);
                // 双重检查，防止在竞争锁的过程中已经有其它线程写入
                if (null == v) {
                    try {
                        v = supplier.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    cache.put(key, v);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        return v;
    }

    /**
     * 放入缓存
     *
     * @param key   键
     * @param value 值
     * @return 值
     */
    public V put(K key, V value) {
        lock.writeLock().lock();
        try {
            cache.put(key, value);
        } finally {
            lock.writeLock().unlock();
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
        lock.writeLock().lock();
        try {
            return cache.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 清空缓存池
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            this.cache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return this.cache.entrySet().iterator();
    }

}
