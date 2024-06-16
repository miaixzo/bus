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
import java.util.concurrent.locks.StampedLock;

/**
 * 使用{@link StampedLock}保护的缓存，使用读写乐观锁
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class StampedCache<K, V> extends AbstractCache<K, V> {

    private static final long serialVersionUID = -1L;

    /**
     * 乐观锁，此处使用乐观锁解决读多写少的场景
     * get时乐观读，再检查是否修改，修改则转入悲观读重新读一遍，可以有效解决在写时阻塞大量读操作的情况
     */
    protected final StampedLock lock = new StampedLock();

    @Override
    public void put(final K key, final V object, final long timeout) {
        final long stamp = lock.writeLock();
        try {
            putWithoutLock(key, object, timeout);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public boolean containsKey(final K key) {
        return null != get(key, false, false);
    }

    @Override
    public V get(final K key, final boolean isUpdateLastAccess) {
        return get(key, isUpdateLastAccess, true);
    }

    @Override
    public Iterator<CacheObject<K, V>> cacheObjIterator() {
        CopiedIterator<CacheObject<K, V>> copiedIterator;
        final long stamp = lock.readLock();
        try {
            copiedIterator = CopiedIterator.copyOf(cacheObjIter());
        } finally {
            lock.unlockRead(stamp);
        }
        return new CacheObjectIterator<>(copiedIterator);
    }

    @Override
    public final int prune() {
        final long stamp = lock.writeLock();
        try {
            return pruneCache();
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void remove(final K key) {
        final long stamp = lock.writeLock();
        CacheObject<K, V> co;
        try {
            co = removeWithoutLock(key);
        } finally {
            lock.unlockWrite(stamp);
        }
        if (null != co) {
            onRemove(co.key, co.obj);
        }
    }

    @Override
    public void clear() {
        final long stamp = lock.writeLock();
        try {
            cacheMap.clear();
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * 获取值
     *
     * @param key                键
     * @param isUpdateLastAccess 是否更新最后修改时间
     * @param isUpdateCount      是否更新命中数，get时更新，contains时不更新
     * @return 值或null
     */
    private V get(final K key, final boolean isUpdateLastAccess, final boolean isUpdateCount) {
        // 尝试读取缓存，使用乐观读锁
        long stamp = lock.tryOptimisticRead();
        CacheObject<K, V> co = getWithoutLock(key);
        if (false == lock.validate(stamp)) {
            // 有写线程修改了此对象，悲观读
            stamp = lock.readLock();
            try {
                co = getWithoutLock(key);
            } finally {
                lock.unlockRead(stamp);
            }
        }

        // 未命中
        if (null == co) {
            if (isUpdateCount) {
                missCount.increment();
            }
            return null;
        } else if (false == co.isExpired()) {
            if (isUpdateCount) {
                hitCount.increment();
            }
            return co.get(isUpdateLastAccess);
        }

        // 悲观锁，二次检查
        return getOrRemoveExpired(key, isUpdateCount);
    }

    /**
     * 同步获取值，如果过期则移除之
     *
     * @param key           键
     * @param isUpdateCount 是否更新命中数，get时更新，contains时不更新
     * @return 有效值或null
     */
    private V getOrRemoveExpired(final K key, final boolean isUpdateCount) {
        final long stamp = lock.writeLock();
        CacheObject<K, V> co;
        try {
            co = getWithoutLock(key);
            if (null == co) {
                return null;
            }
            if (false == co.isExpired()) {
                // 首先尝试获取值，如果值存在且有效，返回之
                if (isUpdateCount) {
                    hitCount.increment();
                }
                return co.getValue();
            }

            // 无效移除
            co = removeWithoutLock(key);
            if (isUpdateCount) {
                missCount.increment();
            }
        } finally {
            lock.unlockWrite(stamp);
        }
        if (null != co) {
            onRemove(co.key, co.obj);
        }
        return null;
    }

}
