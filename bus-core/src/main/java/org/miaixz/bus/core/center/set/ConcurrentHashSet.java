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
package org.miaixz.bus.core.center.set;

import java.io.Serial;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe {@link java.util.Set} implementation backed by a {@link ConcurrentHashMap}. This class provides a
 * concurrent hash set with similar semantics to {@link java.util.HashSet}, but with thread-safe operations.
 *
 * @param <E> The type of elements in this set.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConcurrentHashSet<E> extends SetFromMap<E> {

    @Serial
    private static final long serialVersionUID = 2852280065806L;

    /**
     * Constructs a new, empty {@code ConcurrentHashSet} with a default initial capacity (16) and load factor (0.75).
     */
    public ConcurrentHashSet() {
        super(new ConcurrentHashMap<>());
    }

    /**
     * Constructs a new, empty {@code ConcurrentHashSet} with the specified initial capacity and a default load factor
     * (0.75).
     *
     * @param initialCapacity The initial capacity of the hash map.
     */
    public ConcurrentHashSet(final int initialCapacity) {
        super(new ConcurrentHashMap<>(initialCapacity));
    }

    /**
     * Constructs a new, empty {@code ConcurrentHashSet} with the specified initial capacity and load factor.
     *
     * @param initialCapacity The initial capacity of the hash map.
     * @param loadFactor      The load factor for the hash map. This parameter determines the percentage at which the
     *                        map will grow.
     */
    public ConcurrentHashSet(final int initialCapacity, final float loadFactor) {
        super(new ConcurrentHashMap<>(initialCapacity, loadFactor));
    }

    /**
     * Constructs a new, empty {@code ConcurrentHashSet} with the specified initial capacity, load factor, and
     * concurrency level.
     *
     * @param initialCapacity  The initial capacity of the hash map.
     * @param loadFactor       The load factor for the hash map. This parameter determines the percentage at which the
     *                         map will grow.
     * @param concurrencyLevel The estimated number of concurrently updating threads. The implementation may use this as
     *                         a hint to size tables to minimize the number of resizes.
     */
    public ConcurrentHashSet(final int initialCapacity, final float loadFactor, final int concurrencyLevel) {
        super(new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel));
    }

    /**
     * Constructs a new {@code ConcurrentHashSet} containing the elements in the specified iterable collection. The
     * initial capacity of the backing map is determined by the size of the iterable, if it is a {@link Collection}.
     *
     * @param iter The collection whose elements are to be placed into this set.
     */
    public ConcurrentHashSet(final Iterable<E> iter) {
        super(iter instanceof Collection ? new ConcurrentHashMap<>(((Collection<E>) iter).size())
                : new ConcurrentHashMap<>());
        if (iter instanceof Collection) {
            this.addAll((Collection<E>) iter);
        } else {
            for (final E e : iter) {
                this.add(e);
            }
        }
    }

}
