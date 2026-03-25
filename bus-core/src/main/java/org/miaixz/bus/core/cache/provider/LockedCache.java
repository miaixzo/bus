/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * A cache implementation protected by a pessimistic {@link Lock}.
 * <p>
 * Both read and write operations are performed under a mutual exclusion lock. This is primarily intended for cache
 * implementations where read operations can also modify the underlying map structure (e.g., a
 * {@link java.util.LinkedHashMap} in an LRU cache), making a standard read-write lock unsuitable.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class LockedCache<K, V> extends AbstractCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852231905670L;

    /**
     * The mutual exclusion lock. This is necessary for caches like those based on LinkedHashMap, where the get method
     * can also change the map's structure, requiring a full lock.
     */
    protected Lock lock = new ReentrantLock();

    /**
     * Adds a value to the cache with the specified timeout.
     */
    @Override
    public void put(final K key, final V object, final long timeout) {
        lock.lock();
        try {
            putWithoutLock(key, object, timeout);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks if the cache contains the specified key.
     */
    @Override
    public boolean containsKey(final K key) {
        return null != getOrRemoveExpired(key, false, false);
    }

    /**
     * Gets a value from the cache.
     */
    @Override
    public V get(final K key, final boolean isUpdateLastAccess) {
        return getOrRemoveExpired(key, isUpdateLastAccess, true);
    }

    /**
     * Gets a value from the cache, creating it if necessary using the provided supplier.
     */
    @Override
    public V get(final K key, final boolean isUpdateLastAccess, final long timeout, final SupplierX<V> supplier) {
        V v = get(key, isUpdateLastAccess);

        // If the object does not exist, create it under a lock.
        if (null == v && null != supplier) {
            // A global lock is used here to prevent deadlock issues that could arise from per-key locks.
            // This avoids circular dependencies but means that all other cache operations are blocked
            // during the object creation process. The performance impact depends on the creation time.
            lock.lock();
            try {
                // Double-check to prevent regeneration if another thread has already written the value.
                final CacheObject<K, V> co = getOrRemoveExpiredWithoutLock(key);
                if (null == co) {
                    v = supplier.get();
                    putWithoutLock(key, v, timeout);
                }
            } finally {
                lock.unlock();
            }
        }
        return v;
    }

    /**
     * Returns an iterator over the cache objects.
     */
    @Override
    public Iterator<CacheObject<K, V>> cacheObjIterator() {
        CopiedIterator<CacheObject<K, V>> copiedIterator;
        lock.lock();
        try {
            // Create a copy of the iterator to prevent ConcurrentModificationException.
            copiedIterator = CopiedIterator.copyOf(cacheObjIter());
        } finally {
            lock.unlock();
        }
        return new CacheObjectIterator<>(copiedIterator);
    }

    /**
     * Removes expired entries from the cache.
     */
    @Override
    public final int prune() {
        lock.lock();
        try {
            return pruneCache();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes a value from the cache.
     */
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

    /**
     * Removes all of the elements from this cache.
     */
    @Override
    public void clear() {
        lock.lock();
        try {
            // Create a copy of the key set to iterate over.
            final Set<Mutable<K>> keys = SetKit.of(cacheMap.keySet());
            for (final Mutable<K> key : keys) {
                CacheObject<K, V> co = removeWithoutLock(key.get());
                if (null != co) {
                    // Trigger resource release or listener notification.
                    onRemove(co.key, co.object);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the string representation of this cache.
     */
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
     * Gets a value or removes it if it has expired.
     *
     * @param key                The key.
     * @param isUpdateLastAccess Whether to update the last access time.
     * @param isUpdateCount      Whether to update the hit/miss counters.
     * @return The value, or {@code null} if not found or expired.
     */
    private V getOrRemoveExpired(final K key, final boolean isUpdateLastAccess, final boolean isUpdateCount) {
        CacheObject<K, V> co;
        lock.lock();
        try {
            co = getWithoutLock(key);
            if (null != co && co.isExpired()) {
                // Remove the expired object.
                removeWithoutLock(key);
                onRemove(co.key, co.object);
                co = null;
            }
        } finally {
            lock.unlock();
        }

        // Handle cache miss.
        if (null == co) {
            if (isUpdateCount) {
                missCount.increment();
            }
            return null;
        }

        // Handle cache hit.
        if (isUpdateCount) {
            hitCount.increment();
        }
        return co.get(isUpdateLastAccess);
    }

}
