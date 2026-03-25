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
 * @since Java 21+
 */
public class NoCache<K, V> implements Cache<K, V> {

    @Serial
    private static final long serialVersionUID = 2852232053893L;

    /**
     * @return Always returns {@code 0}.
     */
    @Override
    public int capacity() {
        return 0;
    }

    /**
     * @return Always returns {@code 0}.
     */
    @Override
    public long timeout() {
        return 0;
    }

    /**
     * This is a no-op; the object is not cached.
     */
    @Override
    public void put(final K key, final V object) {
        // Skip
    }

    /**
     * This is a no-op; the object is not cached.
     */
    @Override
    public void put(final K key, final V object, final long timeout) {
        // Skip
    }

    /**
     * @return Always returns {@code false}.
     */
    @Override
    public boolean containsKey(final K key) {
        return false;
    }

    /**
     * @return Always returns {@code null}.
     */
    @Override
    public V get(final K key) {
        return null;
    }

    /**
     * @return Always returns {@code null}.
     */
    @Override
    public V get(final K key, final boolean isUpdateLastAccess) {
        return null;
    }

    /**
     * Invokes the supplier to generate a value but does not cache it.
     *
     * @return The value from the supplier, or {@code null} if the supplier is null.
     */
    @Override
    public V get(final K key, final boolean isUpdateLastAccess, final long timeout, final SupplierX<V> supplier) {
        return (null == supplier) ? null : supplier.get();
    }

    /**
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
     * @return Always returns {@code null}.
     */
    @Override
    public Iterator<CacheObject<K, V>> cacheObjIterator() {
        return null;
    }

    /**
     * @return Always returns {@code 0}.
     */
    @Override
    public int prune() {
        return 0;
    }

    /**
     * @return Always returns {@code false}.
     */
    @Override
    public boolean isFull() {
        return false;
    }

    /**
     * This is a no-op.
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
     * @return Always returns {@code 0}.
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * @return Always returns {@code true}.
     */
    @Override
    public boolean isEmpty() {
        return true;
    }

}
