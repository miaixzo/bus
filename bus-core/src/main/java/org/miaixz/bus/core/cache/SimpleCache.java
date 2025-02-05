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
package org.miaixz.bus.core.cache;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.center.iterator.TransIterator;
import org.miaixz.bus.core.center.map.concurrent.SafeConcurrentHashMap;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.mutable.Mutable;
import org.miaixz.bus.core.lang.mutable.MutableObject;

/**
 * 简单缓存，无超时实现，默认使用{@link WeakConcurrentMap}实现缓存自动清理
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleCache<K, V> implements Iterable<Map.Entry<K, V>>, Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 写的时候每个key一把锁，降低锁的粒度
     */
    protected final Map<K, Lock> keyLockMap = new SafeConcurrentHashMap<>();
    /**
     * 池
     */
    private final Map<Mutable<K>, V> rawMap;
    /**
     * 乐观读写锁
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 构造，默认使用{@link WeakHashMap}实现缓存自动清理
     */
    public SimpleCache() {
        this(new WeakConcurrentMap<>());
    }

    /**
     * 构造
     * <p>
     * 通过自定义Map初始化，可以自定义缓存实现。 比如使用{@link WeakHashMap}则会自动清理key，使用HashMap则不会清理 同时，传入的Map对象也可以自带初始化的键值对，防止在get时创建
     * </p>
     *
     * @param initMap 初始Map，用于定义Map类型
     */
    public SimpleCache(final Map<Mutable<K>, V> initMap) {
        this.rawMap = initMap;
    }

    /**
     * 是否包含键
     *
     * @param key 键
     * @return 是否包含
     */
    public boolean containsKey(final K key) {
        lock.readLock().lock();
        try {
            return rawMap.containsKey(MutableObject.of(key));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 从缓存池中查找值
     *
     * @param key 键
     * @return 值
     */
    public V get(final K key) {
        lock.readLock().lock();
        try {
            return rawMap.get(MutableObject.of(key));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 从缓存中获得对象，当对象不在缓存中或已经过期返回SerSupplier回调产生的对象
     *
     * @param key      键
     * @param supplier 如果不存在回调方法，用于生产值对象
     * @return 值对象
     */
    public V get(final K key, final SupplierX<V> supplier) {
        return get(key, null, supplier);
    }

    /**
     * 从缓存中获得对象，当对象不在缓存中或已经过期返回SerSupplier回调产生的对象
     *
     * @param key            键
     * @param validPredicate 检查结果对象是否可用，如是否断开连接等
     * @param supplier       如果不存在回调方法或结果不可用，用于生产值对象
     * @return 值对象
     */
    public V get(final K key, final Predicate<V> validPredicate, final SupplierX<V> supplier) {
        V v = get(key);
        if ((null != validPredicate && null != v && !validPredicate.test(v))) {
            v = null;
        }
        if (null == v && null != supplier) {
            // 每个key单独获取一把锁，降低锁的粒度提高并发能力
            final Lock keyLock = this.keyLockMap.computeIfAbsent(key, k -> new ReentrantLock());
            keyLock.lock();
            try {
                // 双重检查，防止在竞争锁的过程中已经有其它线程写入
                v = get(key);
                if (null == v || (null != validPredicate && !validPredicate.test(v))) {
                    v = supplier.get();
                    put(key, v);
                }
            } finally {
                keyLock.unlock();
                keyLockMap.remove(key);
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
    public V put(final K key, final V value) {
        Assert.notNull(value, "'value' must not be null");
        // 独占写锁
        lock.writeLock().lock();
        try {
            rawMap.put(MutableObject.of(key), value);
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
    public V remove(final K key) {
        // 独占写锁
        lock.writeLock().lock();
        try {
            return rawMap.remove(MutableObject.of(key));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 清空缓存池
     */
    public void clear() {
        // 独占写锁
        lock.writeLock().lock();
        try {
            this.rawMap.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new TransIterator<>(this.rawMap.entrySet().iterator(), (entry) -> new Map.Entry<>() {
            @Override
            public K getKey() {
                return entry.getKey().get();
            }

            @Override
            public V getValue() {
                return entry.getValue();
            }

            @Override
            public V setValue(final V value) {
                return entry.setValue(value);
            }
        });
    }

    /**
     * 获取所有键
     *
     * @return 所有键
     */
    public List<K> keys() {
        return this.rawMap.keySet().stream().map(Mutable::get).collect(Collectors.toList());
    }

}
