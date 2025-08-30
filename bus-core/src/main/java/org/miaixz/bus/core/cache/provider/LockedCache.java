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
package org.miaixz.bus.core.cache.provider;

import java.io.Serial;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.center.iterator.CopiedIterator;
import org.miaixz.bus.core.lang.mutable.Mutable;
import org.miaixz.bus.core.xyz.SetKit;

/**
 * 使用{@link Lock}保护的缓存，读写都使用悲观锁完成，主要避免某些Map无法使用读写锁的问题, 例如使用了LinkedHashMap的缓存，由于get方法也会改变Map的结构，因此读写必须加互斥锁
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class LockedCache<K, V> extends AbstractCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852231905670L;

    /**
     * 一些特殊缓存，例如使用了LinkedHashMap的缓存，由于get方法也会改变Map的结构，导致无法使用读写锁
     */
    protected Lock lock = new ReentrantLock();

    @Override
    public void put(final K key, final V object, final long timeout) {
        lock.lock();
        try {
            putWithoutLock(key, object, timeout);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsKey(final K key) {
        return null != getOrRemoveExpired(key, false, false);
    }

    @Override
    public V get(final K key, final boolean isUpdateLastAccess) {
        return getOrRemoveExpired(key, isUpdateLastAccess, true);
    }

    @Override
    public V get(final K key, final boolean isUpdateLastAccess, final long timeout, final SupplierX<V> supplier) {
        V v = get(key, isUpdateLastAccess);

        // 对象不存在，则加锁创建
        if (null == v && null != supplier) {
            // 使用key锁可以避免对象创建等待问题，但是会带来循环锁问题。
            // 因此此处依旧采用全局锁，在对象创建过程中，全局等待，避免循环锁依赖
            // 这样避免了循环锁，但是会存在一个缺点，即对象创建过程中，其它线程无法获得锁，从而无法使用缓存，因此需要考虑对象创建的耗时问题
            lock.lock();
            try {
                // 双重检查锁，防止在竞争锁的过程中已经有其它线程写入
                final CacheObject<K, V> co = getOrRemoveExpiredWithoutLock(key);
                if (null == co) {
                    // supplier的创建是一个耗时过程，此处创建与全局锁无关，而与key锁相关，这样就保证每个key只创建一个value，且互斥
                    v = supplier.get();
                    putWithoutLock(key, v, timeout);
                }
            } finally {
                lock.unlock();
            }
        }
        return v;
    }

    @Override
    public Iterator<CacheObject<K, V>> cacheObjIterator() {
        CopiedIterator<CacheObject<K, V>> copiedIterator;
        lock.lock();
        try {
            copiedIterator = CopiedIterator.copyOf(cacheObjIter());
        } finally {
            lock.unlock();
        }
        return new CacheObjectIterator<>(copiedIterator);
    }

    @Override
    public final int prune() {
        lock.lock();
        try {
            return pruneCache();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(final K key) {
        CacheObject<K, V> co;
        lock.lock();
        try {
            co = removeWithoutLock(key);
        } finally {
            lock.unlock();
        }
        if (null != co) {
            onRemove(co.key, co.object);
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            // 获取所有键的副本
            final Set<Mutable<K>> keys = SetKit.of(cacheMap.keySet());
            CacheObject<K, V> co;
            for (final Mutable<K> key : keys) {
                co = removeWithoutLock(key.get());
                if (null != co) {
                    // 触发资源释放
                    onRemove(co.key, co.object);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        lock.lock();
        try {
            return super.toString();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获得值或清除过期值
     * 
     * @param key                键
     * @param isUpdateLastAccess 是否更新最后访问时间
     * @param isUpdateCount      是否更新计数器
     * @return 值或null
     */
    private V getOrRemoveExpired(final K key, final boolean isUpdateLastAccess, final boolean isUpdateCount) {
        CacheObject<K, V> co;
        lock.lock();
        try {
            co = getWithoutLock(key);
            if (null != co && co.isExpired()) {
                // 过期移除
                removeWithoutLock(key);
                onRemove(co.key, co.object);
                co = null;
            }
        } finally {
            lock.unlock();
        }

        // 未命中
        if (null == co) {
            if (isUpdateCount) {
                missCount.increment();
            }
            return null;
        }

        if (isUpdateCount) {
            hitCount.increment();
        }
        return co.get(isUpdateLastAccess);
    }

}
