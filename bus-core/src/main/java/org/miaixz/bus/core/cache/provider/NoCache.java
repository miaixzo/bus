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
import java.util.NoSuchElementException;

import org.miaixz.bus.core.cache.Cache;
import org.miaixz.bus.core.center.function.SupplierX;

/**
 * A no-op cache implementation that does not store any data. This is useful for quickly disabling caching without
 * changing application code.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 */
public class NoCache<K, V> implements Cache<K, V> {

    @Serial
    private static final long serialVersionUID = 2535620253556L;

    /**
     * Constructs a new NoCache instance.
     */
    public NoCache() {
        // No initialization required.
    }

    /**
     * Returns the capacity of this disabled cache.
     *
     * @return Always returns {@code 0}.
     */
    @Override
    public int capacity() {
        return 0;
    }

    /**
     * Returns the timeout of this disabled cache.
     *
     * @return Always returns {@code 0}.
     */
    @Override
    public long timeout() {
        return 0;
    }

    /**
     * This is a no-op; the object is not cached.
     *
     * @param key    The key.
     * @param object The object to be cached.
     */
    @Override
    public void put(final K key, final V object) {
        // Skip
    }

    /**
     * This is a no-op; the object is not cached.
     *
     * @param key     The key.
     * @param object  The object to be cached.
     * @param timeout The timeout for the object in milliseconds.
     */
    @Override
    public void put(final K key, final V object, final long timeout) {
        // Skip
    }

    /**
     * Reports that this cache never contains a key.
     *
     * @param key The parameter name in the Bean object.
     * @return Always returns {@code false}.
     */
    @Override
    public boolean containsKey(final K key) {
        return false;
    }

    /**
     * Returns no cached value because this cache stores no entries.
     *
     * @param key The key.
     * @return Always returns {@code null}.
     */
    @Override
    public V get(final K key) {
        return null;
    }

    /**
     * Returns no cached value because this cache stores no entries.
     *
     * @param key                The key.
     * @param isUpdateLastAccess Whether to update the last access time, effectively resetting the timeout.
     * @return Always returns {@code null}.
     */
    @Override
    public V get(final K key, final boolean isUpdateLastAccess) {
        return null;
    }

    /**
     * Invokes the supplier to generate a value but does not cache it.
     *
     * @param key                The key.
     * @param isUpdateLastAccess Whether to update the last access time.
     * @param timeout            A custom timeout for the new object if it is generated.
     * @param supplier           A callback to generate the object if it's not in the cache.
     * @return The value from the supplier, or {@code null} if the supplier is null.
     */
    @Override
    public V get(final K key, final boolean isUpdateLastAccess, final long timeout, final SupplierX<V> supplier) {
        return (null == supplier) ? null : supplier.get();
    }

    /**
     * Returns an iterator with no cached values.
     *
     * @return An empty iterator.
     */
    @Override
    public Iterator<V> iterator() {
        return new Iterator<>() {

            /**
             * Returns true if the iteration has more elements.
             *
             * @return true if the iteration has more elements
             */
            @Override
            public boolean hasNext() {
                return false;
            }

            /**
             * Returns the next element in the iteration.
             *
             * @return the next element
             */
            @Override
            public V next() {
                throw new NoSuchElementException();
            }

        };
    }

    /**
     * Returns no cache-entry iterator because this cache stores no entries.
     *
     * @return Always returns {@code null}.
     */
    @Override
    public Iterator<CacheObject<K, V>> cacheObjIterator() {
        return null;
    }

    /**
     * Reports that there are no entries to prune.
     *
     * @return Always returns {@code 0}.
     */
    @Override
    public int prune() {
        return 0;
    }

    /**
     * Reports that a cache with no stored entries is never full.
     *
     * @return Always returns {@code false}.
     */
    @Override
    public boolean isFull() {
        return false;
    }

    /**
     * This is a no-op.
     *
     * @param key The key of the object to remove.
     */
    @Override
    public void remove(final K key) {
        // Skip
    }

    /**
     * This is a no-op.
     */
    @Override
    public void clear() {
        // Skip
    }

    /**
     * Returns the number of entries retained by this disabled cache.
     *
     * @return Always returns {@code 0}.
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * Reports that this disabled cache is always empty.
     *
     * @return Always returns {@code true}.
     */
    @Override
    public boolean isEmpty() {
        return true;
    }

}
