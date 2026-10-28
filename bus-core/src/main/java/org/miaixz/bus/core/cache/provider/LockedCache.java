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
import org.miaixz.bus.core.lang.thread.lock.NoLock;
import org.miaixz.bus.core.xyz.SetKit;

/**
 * A cache implementation protected by a pessimistic {@link Lock}.
 * <p>
 * Both read and write operations use the configured lock. This is primarily intended for cache implementations where
 * read operations can also modify the underlying map structure (e.g., a {@link java.util.LinkedHashMap} in an LRU
 * cache), making a standard read-write lock unsuitable. Concurrent-storage subclasses may select {@link NoLock}; lazy
 * loading then still uses registered per-key locks.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 */
public abstract class LockedCache<K, V> extends AbstractCache<K, V> {

    @Serial
    private static final long serialVersionUID = 2535518000001L;

    /**
     * The mutual exclusion lock. This is necessary for caches like those based on LinkedHashMap, where the get method
     * can also change the map's structure, requiring a full lock.
     */
    protected Lock lock = new ReentrantLock();

    /**
     * Constructs a new LockedCache instance.
     */
    public LockedCache() {
        // No initialization required.
    }

    /**
     * Adds a value to the cache with the specified timeout.
     *
     * @param key     cache key
     * @param object  value to store, including {@code null} when permitted by the concrete cache
     * @param timeout entry timeout in milliseconds; {@code 0} disables expiration
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
     * Checks if the cache contains the specified key. Expired entries are removed without updating hit or miss counts.
     *
     * @param key key to check
     * @return whether the lookup returns an unexpired non-null value; null-valued entries also return {@code false}
     */
    @Override
    public boolean containsKey(final K key) {
        return null != getOrRemoveExpired(key, false, false);
    }

    /**
     * Gets a value from the cache.
     *
     * @param key                key to look up
     * @param isUpdateLastAccess whether to refresh the entry's last-access timestamp
     * @return the stored value, or {@code null} for an absent, expired, or null-valued entry
     */
    @Override
    public V get(final K key, final boolean isUpdateLastAccess) {
        return getOrRemoveExpired(key, isUpdateLastAccess, true);
    }

    /**
     * Gets a value, rechecking an initial null result before invoking the supplier. A surviving null-valued entry is
     * reused rather than reloaded. Loading holds the configured storage lock, or a registered per-key lock when storage
     * uses {@link NoLock}. Different keys are not globally serialized by the per-key loading path.
     *
     * @param key                key to look up or load
     * @param isUpdateLastAccess whether to refresh the entry's last-access timestamp
     * @param timeout            timeout in milliseconds for a newly loaded entry; {@code 0} disables expiration
     * @param supplier           value supplier, or {@code null} to disable loading
     * @return the cached or loaded value, which may be {@code null}
     */
    @Override
    public V get(final K key, final boolean isUpdateLastAccess, final long timeout, final SupplierX<V> supplier) {
        V v = get(key, isUpdateLastAccess);

        // If the object does not exist, create it under a lock.
        if (null == v && null != supplier) {
            // Concurrent storage still needs coordination for this compound loading operation.
            // Non-concurrent storage retains its original global loading and storage lock order.
            final Lock loading = lock == NoLock.INSTANCE ? acquireKeyLock(key) : null;
            if (loading == null) {
                lock.lock();
            }
            try {
                // Double-check to prevent regeneration if another thread has already written the value.
                final CacheObject<K, V> co = getOrRemoveExpiredWithoutLock(key);
                if (null == co) {
                    v = supplier.get();
                    putWithoutLock(key, v, timeout);
                } else {
                    v = co.get(isUpdateLastAccess);
                }
            } finally {
                if (loading == null) {
                    lock.unlock();
                } else {
                    releaseKeyLock(key, loading);
                }
            }
        }
        return v;
    }

    /**
     * Returns an iterator over the cache objects. The backing traversal is copied while holding the configured lock,
     * then expired entries are filtered.
     *
     * @return an iterator over the captured, unexpired cache objects
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
     * Runs the concrete eviction strategy while holding the configured lock.
     *
     * @return the number of entries removed by that strategy
     */
    @Override
    public int prune() {
        lock.lock();
        try {
            return pruneCache();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes a value from the cache. Listener notification occurs after releasing the configured lock.
     *
     * @param key key whose entry is to be removed
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
     *
     * @return the backing map's representation obtained while holding the configured lock
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
