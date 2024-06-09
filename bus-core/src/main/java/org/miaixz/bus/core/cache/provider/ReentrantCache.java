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
package org.miaixz.bus.core.cache.provider;

import org.miaixz.bus.core.center.iterator.CopiedIterator;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用{@link ReentrantLock}保护的缓存，读写都使用悲观锁完成，主要避免某些Map无法使用读写锁的问题
 * 例如使用了LinkedHashMap的缓存，由于get方法也会改变Map的结构，因此读写必须加互斥锁
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ReentrantCache<K, V> extends AbstractCache<K, V> {

    private static final long serialVersionUID = -1L;

    /**
     * 特殊缓存，例如使用了LinkedHashMap的缓存，由于get方法也会改变Map的结构，导致无法使用读写锁
     */
    protected final ReentrantLock lock = new ReentrantLock();

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
            onRemove(co.key, co.obj);
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            cacheMap.clear();
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
