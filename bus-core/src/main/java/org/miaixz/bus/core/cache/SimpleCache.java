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
package org.miaixz.bus.core.cache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

import org.miaixz.bus.core.center.function.PredicateX;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.center.map.AbstractEntry;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Assert;

/**
 * A simple cache implementation with no expiration. By default, it uses a {@link WeakConcurrentMap} to allow for
 * automatic garbage collection of entries when business keys or values are no longer referenced elsewhere. Both default
 * and caller-supplied storage use business keys directly, without temporary key wrappers.
 * <p>
 * Native-image serialization of application-defined storage, cache subclasses, or business key/value types requires the
 * application's own registrations for those types.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 */
public class SimpleCache<K, V> implements Iterable<Map.Entry<K, V>>, Serializable {

    @Serial
    private static final long serialVersionUID = 2535362150002L;

    /**
     * Loading locks with registered user counts, including waiters. Runtime coordination is not serialized.
     */
    protected transient Map<K, Lock> keyLockMap = new ConcurrentHashMap<>();

    /**
     * Backing storage keyed directly by business keys.
     */
    private final Map<K, V> rawMap;

    /**
     * Coordinates cache-method reads and writes, but not direct external access to caller-supplied storage.
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Constructs a new cache that uses a {@link WeakConcurrentMap} by default for automatic cleanup.
     */
    public SimpleCache() {
        this(new WeakConcurrentMap<>());
    }

    /**
     * Constructs a new cache with a custom underlying map.
     * <p>
     * This allows for flexible cache implementations. For example, using a {@link WeakHashMap} enables automatic key
     * cleanup, while a standard {@link java.util.HashMap} will not. The provided map can also be pre-populated with
     * initial key-value pairs. Keys are business keys, not Mutable wrappers. The supplied map is shared rather than
     * copied; direct external access must follow that map's own synchronization rules.
     *
     * @param initMap The initial business-key map to use for the cache, must not be {@code null}.
     * @throws NullPointerException if {@code initMap} is {@code null}
     */
    public SimpleCache(final Map<K, V> initMap) {
        this.rawMap = Objects.requireNonNull(initMap, "initMap");
    }

    /**
     * Checks if the cache contains the specified key.
     *
     * @param key The key.
     * @return {@code true} if the cache contains the key, otherwise {@code false}.
     */
    public boolean containsKey(final K key) {
        lock.readLock().lock();
        try {
            return rawMap.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Retrieves a value from the cache.
     *
     * @param key The key.
     * @return The value, or {@code null} if the key is not found.
     */
    public V get(final K key) {
        lock.readLock().lock();
        try {
            return rawMap.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Retrieves a value from the cache. If the value is not present, it is generated by the provided supplier.
     *
     * @param key      The key.
     * @param supplier A callback to generate the value if it is not in the cache.
     * @return The value.
     */
    public V get(final K key, final SupplierX<V> supplier) {
        return get(key, null, supplier);
    }

    /**
     * Retrieves a value from the cache. If the value is not present, is null, or fails the validation predicate, it is
     * generated by the provided supplier.
     *
     * @param key            The key.
     * @param validPredicate A predicate to check if the retrieved value is valid (e.g., not disconnected).
     * @param supplier       A callback to generate the value if it is not present or invalid.
     * @return The value.
     */
    public V get(final K key, final PredicateX<V> validPredicate, final SupplierX<V> supplier) {
        V v = get(key);
        // If a validator is provided, check if the existing value is valid.
        if ((null != validPredicate && null != v && !validPredicate.test(v))) {
            v = null;
        }

        // If the value is null and a supplier is provided, generate a new value.
        if (null == v && null != supplier) {
            // Acquire a lock for the specific key to ensure thread-safe generation.
            final Lock loading = acquireKeyLock(key);
            try {
                // Double-check to prevent re-generation if another thread has already written the value.
                v = get(key);
                if (null == v || (null != validPredicate && !validPredicate.test(v))) {
                    v = supplier.get();
                    put(key, v);
                }
            } finally {
                loading.unlock();
                unregisterKeyLock(key, loading);
            }
        }

        return v;
    }

    /**
     * Puts a value into the cache.
     *
     * @param key   The key.
     * @param value The value.
     * @return The stored value.
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
    public V put(final K key, final V value) {
        Assert.notNull(value, "'value' must not be null");
        // Acquire exclusive write lock.
        lock.writeLock().lock();
        try {
            rawMap.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
        return value;
    }

    /**
     * Removes a value from the cache.
     *
     * @param key The key.
     * @return The removed value, or {@code null} if the key was not found.
     */
    public V remove(final K key) {
        // Acquire exclusive write lock.
        lock.writeLock().lock();
        try {
            return rawMap.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Clears the entire cache.
     */
    public void clear() {
        // Acquire exclusive write lock.
        lock.writeLock().lock();
        try {
            rawMap.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns an iterator over the backing map's entries. Weak storage omits reclaimed entries, while custom storage
     * retains its own iteration and null-key behavior. Entry writes reject null values just like {@link #put}.
     * Concurrent-map removal is conditional on the observed value; other maps retain their iterator removal semantics.
     * Traversal itself does not acquire a cache-wide lock and follows the backing map's concurrency requirements.
     *
     * @return An iterator of {@link Map.Entry}.
     */
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        final Iterator<Map.Entry<K, V>> entries = rawMap.entrySet().iterator();
        return new Iterator<>() {

            /**
             * Last returned entry, independent of the backing iterator's lookahead.
             */
            private Map.Entry<K, V> last;

            /**
             * Reports whether the backing iterator has another entry without changing this wrapper's removal target.
             *
             * @return whether another backing entry is available
             */
            @Override
            public boolean hasNext() {
                return entries.hasNext();
            }

            /**
             * Returns the next backing entry through a write-through wrapper and records it as the removal target.
             *
             * @return the next entry, retaining the backing map's key and value behavior
             * @throws NoSuchElementException if the backing iterator is exhausted
             */
            @Override
            public Map.Entry<K, V> next() {
                final Map.Entry<K, V> entry = entries.next();
                last = new AbstractEntry<>() {

                    /**
                     * Returns the key exposed by the backing entry.
                     *
                     * @return the business key, including null if the backing map permits it
                     */
                    @Override
                    public K getKey() {
                        return entry.getKey();
                    }

                    /**
                     * Returns the value currently exposed by the backing entry.
                     *
                     * @return the backing entry's value, including a pre-populated null value when supported
                     */
                    @Override
                    public V getValue() {
                        return entry.getValue();
                    }

                    /**
                     * Replaces this entry's value under the cache write lock using the backing entry's write semantics.
                     *
                     * @param value non-null replacement value
                     * @return the value previously observed by the backing entry
                     * @throws IllegalArgumentException if {@code value} is {@code null}
                     */
                    @Override
                    public V setValue(final V value) {
                        Assert.notNull(value, "'value' must not be null");
                        lock.writeLock().lock();
                        try {
                            return entry.setValue(value);
                        } finally {
                            lock.writeLock().unlock();
                        }
                    }
                };
                return last;
            }

            /**
             * Removes the last returned mapping under the cache write lock. Concurrent storage removes only a mapping
             * still equal to the last observed key/value pair; other storage uses its original iterator's removal.
             *
             * @throws IllegalStateException if no entry can currently be removed
             */
            @Override
            public void remove() {
                if (last == null) {
                    throw new IllegalStateException();
                }
                lock.writeLock().lock();
                try {
                    if (rawMap instanceof ConcurrentMap<?, ?>) {
                        rawMap.remove(last.getKey(), last.getValue());
                    } else {
                        entries.remove();
                    }
                    last = null;
                } finally {
                    lock.writeLock().unlock();
                }
            }
        };
    }

    /**
     * Collects a detached list from the backing map's current key traversal. Weakly consistent storage may change
     * during collection, while non-concurrent custom maps require callers to prevent concurrent structural changes.
     *
     * @return A list of keys.
     */
    public List<K> keys() {
        final List<K> keys = new ArrayList<>();
        for (K key : rawMap.keySet()) {
            keys.add(key);
        }
        return keys;
    }

    /**
     * Registers before waiting without holding a registry computation while acquiring the delegate lock.
     *
     * @param key cache key
     * @return acquired Lock-valued loading registration
     */
    private Lock acquireKeyLock(final K key) {
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
     * Lock-valued runtime registration, preserving the protected registry's existing subclass contract. Delegate
     * acquisition never takes place inside the registry's atomic update callback.
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
     * Restores an empty runtime loading registry, without serialized locks or waiters. The serialization version
     * rejects streams written before direct business-key storage was introduced.
     *
     * @param input source stream containing this cache's persistent fields
     * @throws IOException            if persistent fields cannot be read
     * @throws ClassNotFoundException if a stored class is unavailable
     */
    @Serial
    private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        keyLockMap = new ConcurrentHashMap<>();
    }

}
