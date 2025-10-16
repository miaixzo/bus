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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.miaixz.bus.core.cache.Cache;
import org.miaixz.bus.core.cache.CacheListener;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.mutable.Mutable;
import org.miaixz.bus.core.lang.mutable.MutableObject;

/**
 * Abstract base class for cache implementations that support expiration and size limits.
 * <p>
 * Subclasses are required to:
 *
 * <ul>
 * <li>Initialize a new underlying {@code Map} for storage.</li>
 * <li>Implement the {@link #pruneCache()} eviction strategy.</li>
 * </ul>
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852230739085L;

    /**
     * A map of locks for each key to reduce lock granularity during write operations.
     */
    protected final Map<K, Lock> keyLockMap = new ConcurrentHashMap<>();
    /**
     * The underlying map that stores the cache data.
     */
    protected Map<Mutable<K>, CacheObject<K, V>> cacheMap;

    /**
     * The cache capacity. A value of {@code 0} indicates no size limit.
     */
    protected int capacity;
    /**
     * The default cache timeout in milliseconds. A value of {@code 0} indicates no limit.
     */
    protected long timeout;

    /**
     * A flag indicating whether any object has a custom timeout, which determines if pruning is necessary.
     */
    protected boolean existCustomTimeout;

    /**
     * A counter for the number of cache hits.
     */
    protected LongAdder hitCount = new LongAdder();
    /**
     * A counter for the number of cache misses.
     */
    protected LongAdder missCount = new LongAdder();

    /**
     * The listener for cache events.
     */
    protected CacheListener<K, V> listener;

    @Override
    public void put(final K key, final V object) {
        put(key, object, this.timeout);
    }

    /**
     * Puts an object into the cache without acquiring a lock.
     *
     * @param key     The key.
     * @param object  The value.
     * @param timeout The timeout for the object in milliseconds.
     */
    protected void putWithoutLock(final K key, final V object, final long timeout) {
        final CacheObject<K, V> co = new CacheObject<>(key, object, timeout);
        if (timeout != 0) {
            this.existCustomTimeout = true;
        }
        final MutableObject<K> mKey = MutableObject.of(key);

        // Do not check for capacity or prune when replacing an existing entry.
        final CacheObject<K, V> oldObj = this.cacheMap.get(mKey);
        if (null != oldObj) {
            onRemove(oldObj.key, oldObj.object);
            // Replace the existing entry.
            this.cacheMap.put(mKey, co);
        } else {
            // Prune if the cache is full before adding a new entry.
            if (isFull()) {
                pruneCache();
            }
            this.cacheMap.put(mKey, co);
        }
    }

    /**
     * Gets the total number of cache hits.
     *
     * @return The hit count.
     */
    public long getHitCount() {
        return hitCount.sum();
    }

    /**
     * Gets the total number of cache misses.
     *
     * @return The miss count.
     */
    public long getMissCount() {
        return missCount.sum();
    }

    @Override
    public V get(final K key, final boolean isUpdateLastAccess, final long timeout, final SupplierX<V> supplier) {
        V v = get(key, isUpdateLastAccess);
        if (null == v && null != supplier) {
            // Use a per-key lock to reduce contention and improve concurrency.
            final Lock keyLock = keyLockMap.computeIfAbsent(key, k -> new ReentrantLock());
            keyLock.lock();
            try {
                // Double-check to prevent regeneration if another thread has already written the value.
                // This get operation needs to be globally aware as put and pruneCache can modify the map.
                v = get(key, isUpdateLastAccess);
                if (null == v) {
                    // The supplier call can be time-consuming, so it's done under the key-specific lock
                    // to ensure that the value is created only once per key.
                    v = supplier.get();
                    put(key, v, timeout);
                }
            } finally {
                keyLock.unlock();
                keyLockMap.remove(key);
            }
        }
        return v;
    }

    /**
     * Gets the {@link CacheObject} for a given key without locking.
     *
     * @param key The key, which will be wrapped in a {@link MutableObject}.
     * @return The {@link CacheObject}, or {@code null} if not found.
     */
    protected CacheObject<K, V> getWithoutLock(final K key) {
        return this.cacheMap.get(MutableObject.of(key));
    }

    /**
     * Gets a {@link CacheObject} or removes it if it has expired, without locking.
     *
     * @param key The key.
     * @return The {@link CacheObject}, or {@code null} if not found or expired.
     */
    protected CacheObject<K, V> getOrRemoveExpiredWithoutLock(final K key) {
        CacheObject<K, V> co = getWithoutLock(key);
        if (null != co && co.isExpired()) {
            // Remove the expired object.
            removeWithoutLock(key);
            onRemove(co.key, co.object);
            co = null;
        }
        return co;
    }

    @Override
    public Iterator<V> iterator() {
        final CacheObjectIterator<K, V> copiedIterator = (CacheObjectIterator<K, V>) this.cacheObjIterator();
        return new CacheValuesIterator<>(copiedIterator);
    }

    /**
     * Prunes the cache to make space. The specific eviction strategy is implemented by subclasses. Implementations of
     * this method do not need to handle locking.
     *
     * @return The number of items pruned.
     */
    protected abstract int pruneCache();

    @Override
    public int capacity() {
        return this.capacity;
    }

    /**
     * Returns the default cache timeout. Each object can also have its own specific timeout.
     *
     * @return The default timeout in milliseconds.
     */
    @Override
    public long timeout() {
        return this.timeout;
    }

    /**
     * Checks if pruning of expired objects is active. Pruning is active if a global timeout is set or if any object has
     * a custom timeout.
     *
     * @return {@code true} if pruning is active.
     */
    protected boolean isPruneExpiredActive() {
        return (this.timeout != 0) || this.existCustomTimeout;
    }

    @Override
    public boolean isFull() {
        return (this.capacity > 0) && (this.cacheMap.size() >= this.capacity);
    }

    @Override
    public int size() {
        return this.cacheMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.cacheMap.isEmpty();
    }

    @Override
    public String toString() {
        return this.cacheMap.toString();
    }

    /**
     * Sets the cache event listener.
     *
     * @param listener The listener to set.
     * @return This cache instance.
     */
    @Override
    public AbstractCache<K, V> setListener(final CacheListener<K, V> listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Returns a set of all keys in the cache.
     *
     * @return A set of keys.
     */
    public Set<K> keySet() {
        return this.cacheMap.keySet().stream().map(Mutable::get).collect(Collectors.toSet());
    }

    /**
     * Callback method invoked when an object is removed from the cache. By default, this method triggers the registered
     * listener, if any. Subclasses can override this method to implement custom logic.
     *
     * @param key          The key of the removed object.
     * @param cachedObject The value of the removed object.
     */
    protected void onRemove(final K key, final V cachedObject) {
        final CacheListener<K, V> listener = this.listener;
        if (null != listener) {
            listener.onRemove(key, cachedObject);
        }
    }

    /**
     * Removes an object from the cache by its key without locking.
     *
     * @param key The key.
     * @return The removed {@link CacheObject}, or {@code null} if not found.
     */
    protected CacheObject<K, V> removeWithoutLock(final K key) {
        return this.cacheMap.remove(MutableObject.of(key));
    }

    /**
     * Returns an iterator over all {@link CacheObject} values.
     *
     * @return An iterator.
     */
    protected Iterator<CacheObject<K, V>> cacheObjIter() {
        return this.cacheMap.values().iterator();
    }

}
