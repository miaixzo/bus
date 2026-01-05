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

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.center.iterator.TransIterator;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.mutable.Mutable;
import org.miaixz.bus.core.lang.mutable.MutableObject;

/**
 * A simple cache implementation with no expiration. By default, it uses a {@link WeakConcurrentMap} to allow for
 * automatic garbage collection of entries when keys are no longer referenced elsewhere.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleCache<K, V> implements Iterable<Map.Entry<K, V>>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852230051852L;

    /**
     * A map of locks for each key to reduce lock granularity during write operations.
     */
    protected final Map<K, Lock> keyLockMap = new ConcurrentHashMap<>();
    /**
     * The underlying map that stores the cache data.
     */
    private final Map<Mutable<K>, V> rawMap;
    /**
     * A read-write lock for thread-safe access to the cache.
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
     * initial key-value pairs.
     *
     * @param initMap The initial map to use for the cache.
     */
    public SimpleCache(final Map<Mutable<K>, V> initMap) {
        this.rawMap = initMap;
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
            return rawMap.containsKey(MutableObject.of(key));
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
            return rawMap.get(MutableObject.of(key));
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
    public V get(final K key, final Predicate<V> validPredicate, final SupplierX<V> supplier) {
        V v = get(key);
        // If a validator is provided, check if the existing value is valid.
        if ((null != validPredicate && null != v && !validPredicate.test(v))) {
            v = null;
        }

        // If the value is null and a supplier is provided, generate a new value.
        if (null == v && null != supplier) {
            // Acquire a lock for the specific key to ensure thread-safe generation.
            final Lock keyLock = this.keyLockMap.computeIfAbsent(key, k -> new ReentrantLock());
            keyLock.lock();
            try {
                // Double-check to prevent re-generation if another thread has already written the value.
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
     * Puts a value into the cache.
     *
     * @param key   The key.
     * @param value The value.
     * @return The stored value.
     */
    public V put(final K key, final V value) {
        Assert.notNull(value, "'value' must not be null");
        // Acquire exclusive write lock.
        lock.writeLock().lock();
        try {
            rawMap.put(MutableObject.of(key), value);
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
            return rawMap.remove(MutableObject.of(key));
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
            this.rawMap.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns an iterator over the cache entries.
     *
     * @return An iterator of {@link Map.Entry}.
     */
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new TransIterator<>(this.rawMap.entrySet().iterator(), (entry) -> new Map.Entry<>() {

            /**
             * Gets the key of this entry.
             *
             * @return the key
             */
            @Override
            public K getKey() {
                return entry.getKey().get();
            }

            /**
             * Gets the value of this entry.
             *
             * @return the value
             */
            @Override
            public V getValue() {
                return entry.getValue();
            }

            /**
             * Sets the value of this entry.
             *
             * @param value the new value
             * @return the old value
             */
            @Override
            public V setValue(final V value) {
                return entry.setValue(value);
            }
        });
    }

    /**
     * Returns a list of all keys in the cache.
     *
     * @return A list of keys.
     */
    public List<K> keys() {
        return this.rawMap.keySet().stream().map(Mutable::get).collect(Collectors.toList());
    }

}
