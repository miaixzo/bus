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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Condition;
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
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    @Serial
    private static final long serialVersionUID = 2535388650001L;

    /**
     * Per-key loading locks and registered users, including waiters. Runtime coordination is not serialized.
     */
    protected transient Map<K, Lock> keyLockMap = new ConcurrentHashMap<>();

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
    protected volatile CacheListener<K, V> listener;

    /**
     * Creates a new {@code AbstractCache} instance.
     */
    public AbstractCache() {
        // No initialization required.
    }

    /**
     * Puts an object into the cache with the default timeout.
     *
     * @param key    The cache key.
     * @param object The value to cache.
     */
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

    /**
     * Gets a value from the cache, optionally updating the last access time, with a supplier for lazy loading.
     *
     * @param key                The cache key.
     * @param isUpdateLastAccess Whether to update the last access time.
     * @param timeout            The timeout for the cached value in milliseconds.
     * @param supplier           The supplier to generate the value if not present in cache.
     * @return The cached value, or the value generated by the supplier if not cached.
     */
    @Override
    public V get(final K key, final boolean isUpdateLastAccess, final long timeout, final SupplierX<V> supplier) {
        V v = get(key, isUpdateLastAccess);
        if (null == v && null != supplier) {
            final Lock loading = acquireKeyLock(key);
            try {
                // Double-check to prevent regeneration if another thread has already written the value.
                // This get operation needs to be globally aware as put and pruneCache can modify the map.
                v = get(key, isUpdateLastAccess);
                if (null == v) {
                    // Serialize same-key supplier calls and reuse a value installed before this recheck.
                    // A null result or later removal can still require a subsequent load.
                    v = supplier.get();
                    put(key, v, timeout);
                }
            } finally {
                releaseKeyLock(key, loading);
            }
        }
        return v;
    }

    /**
     * Registers before waiting without holding a registry computation while acquiring the delegate lock.
     *
     * @param key cache key
     * @return acquired Lock-valued loading registration
     */
    protected Lock acquireKeyLock(final K key) {
        final RegisteredLock loading = (RegisteredLock) keyLockMap.compute(key, (k, current) -> {
            final RegisteredLock registered = current instanceof RegisteredLock existing ? existing
                    : new RegisteredLock(current);
            registered.users++;
            return registered;
        });
        try {
            loading.lock();
            return loading;
        } catch (RuntimeException | Error failure) {
            unregisterKeyLock(key, loading);
            throw failure;
        }
    }

    /**
     * Releases a loading lock and unregisters the caller after unlocking its original delegate.
     *
     * @param key     cache key
     * @param loading acquired loading registration
     */
    protected void releaseKeyLock(final K key, final Lock loading) {
        loading.unlock();
        unregisterKeyLock(key, loading);
    }

    /**
     * Removes idle internal locks, but restores injected delegates that external callers can still hold or await.
     *
     * @param key     cache key associated with the registration
     * @param loading acquired or failed registration whose user count must be released
     */
    private void unregisterKeyLock(final K key, final Lock loading) {
        keyLockMap.computeIfPresent(key, (k, current) -> {
            if (current != loading) {
                return current;
            }
            final RegisteredLock registered = (RegisteredLock) current;
            if (--registered.users != 0) {
                return registered;
            }
            return registered.injected ? registered.delegate : null;
        });
    }

    /**
     * Lock-valued runtime registration, preserving the protected registry's existing subclass contract.
     */
    private static final class RegisteredLock implements Lock {

        /**
         * Original lock, including one supplied by a subclass.
         */
        private final Lock delegate;
        /**
         * Whether an idle registration must restore the external delegate rather than remove it.
         */
        private final boolean injected;
        /**
         * Registered holders and waiters, mutated only inside atomic registry updates.
         */
        private int users;

        /**
         * Retains an injected delegate, or creates an internal reentrant lock when none exists.
         *
         * @param existing existing subclass-supplied lock, or {@code null} for an internal lock
         */
        private RegisteredLock(final Lock existing) {
            injected = existing != null;
            delegate = injected ? existing : new ReentrantLock();
        }

        /**
         * Acquires the delegate lock, waiting according to that lock's acquisition and interrupt-handling policy.
         */
        @Override
        public void lock() {
            delegate.lock();
        }

        /**
         * Acquires the delegate lock using its interruptible acquisition operation.
         *
         * @throws InterruptedException if the delegate reports interruption before acquisition completes
         */
        @Override
        public void lockInterruptibly() throws InterruptedException {
            delegate.lockInterruptibly();
        }

        /**
         * Attempts to acquire the delegate lock without waiting.
         *
         * @return whether the delegate granted the lock immediately
         */
        @Override
        public boolean tryLock() {
            return delegate.tryLock();
        }

        /**
         * Attempts to acquire the delegate lock within the supplied waiting time.
         *
         * @param time maximum waiting time, interpreted according to the delegate's Lock contract
         * @param unit unit of the waiting time
         * @return whether the delegate acquired the lock before the wait expired
         * @throws InterruptedException if the delegate reports interruption while acquiring
         * @throws NullPointerException if the delegate rejects a null time unit
         */
        @Override
        public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
            return delegate.tryLock(time, unit);
        }

        /**
         * Releases the delegate lock according to its ownership and reentrancy rules.
         *
         * @throws IllegalMonitorStateException if the delegate enforces ownership and the caller does not hold it
         */
        @Override
        public void unlock() {
            delegate.unlock();
        }

        /**
         * Creates a condition associated with the delegate lock.
         *
         * @return the delegate's condition object
         * @throws UnsupportedOperationException if the delegate does not support conditions
         */
        @Override
        public Condition newCondition() {
            return delegate.newCondition();
        }
    }

    /**
     * Restores an empty runtime loading registry rather than deserializing active waiters.
     *
     * @param input source stream containing the persistent cache fields
     * @throws IOException            if persistent fields cannot be read
     * @throws ClassNotFoundException if a stored class is unavailable
     */
    @Serial
    private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        keyLockMap = new ConcurrentHashMap<>();
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

    /**
     * Returns an iterator over the cached values.
     *
     * @return An iterator over the cached values.
     */
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

    /**
     * Returns the cache capacity (maximum number of entries).
     *
     * @return The cache capacity, or {@code 0} if there is no size limit.
     */
    @Override
    public int capacity() {
        return this.capacity;
    }

    /**
     * Returns the default cache timeout in milliseconds.
     *
     * @return The default timeout in milliseconds, or {@code 0} if no timeout is set.
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

    /**
     * Checks if the cache is full (at capacity).
     *
     * @return {@code true} if the cache is full, {@code false} otherwise.
     */
    @Override
    public boolean isFull() {
        return (this.capacity > 0) && (this.cacheMap.size() >= this.capacity);
    }

    /**
     * Returns the number of entries in the cache.
     *
     * @return The cache size.
     */
    @Override
    public int size() {
        return this.cacheMap.size();
    }

    /**
     * Checks if the cache is empty.
     *
     * @return {@code true} if the cache contains no entries, {@code false} otherwise.
     */
    @Override
    public boolean isEmpty() {
        return this.cacheMap.isEmpty();
    }

    /**
     * Returns a string representation of the cache.
     *
     * @return A string representation of the underlying cache map.
     */
    @Override
    public String toString() {
        return this.cacheMap.toString();
    }

    /**
     * Sets the cache event listener.
     *
     * @param listener The listener to set for cache events.
     * @return This cache instance for method chaining.
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
