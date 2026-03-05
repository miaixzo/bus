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

import org.miaixz.bus.core.center.map.concurrent.ConcurrentLinkedHashMap;
import org.miaixz.bus.core.lang.Normal;

/**
 * A thread-safe {@link java.util.Set} implementation backed by a {@link ConcurrentLinkedHashMap}. This class provides a
 * concurrent hash set that maintains insertion order, similar to {@link java.util.LinkedHashSet}, but with thread-safe
 * operations.
 *
 * @param <E> The type of elements in this set.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConcurrentLinkedHashSet<E> extends SetFromMap<E> {

    @Serial
    private static final long serialVersionUID = 2852280151086L;

    /**
     * Constructs a new, empty {@code ConcurrentLinkedHashSet} with a default maximum weighted capacity (64). The
     * underlying map is a {@link ConcurrentLinkedHashMap}.
     */
    public ConcurrentLinkedHashSet() {
        super(new ConcurrentLinkedHashMap.Builder<E, Boolean>().maximumWeightedCapacity(Normal._64).build());
    }

    /**
     * Constructs a new, empty {@code ConcurrentLinkedHashSet} with the specified initial capacity. The underlying map
     * is a {@link ConcurrentLinkedHashMap} with its maximum weighted capacity set to the initial capacity.
     *
     * @param initialCapacity The initial capacity of the hash map.
     */
    public ConcurrentLinkedHashSet(final int initialCapacity) {
        super(new ConcurrentLinkedHashMap.Builder<E, Boolean>().initialCapacity(initialCapacity)
                .maximumWeightedCapacity(initialCapacity).build());
    }

    /**
     * Constructs a new, empty {@code ConcurrentLinkedHashSet} with the specified initial capacity and concurrency
     * level. The underlying map is a {@link ConcurrentLinkedHashMap} with its maximum weighted capacity set to the
     * initial capacity.
     *
     * @param initialCapacity  The initial capacity of the hash map.
     * @param concurrencyLevel The estimated number of concurrently updating threads.
     */
    public ConcurrentLinkedHashSet(final int initialCapacity, final int concurrencyLevel) {
        super(new ConcurrentLinkedHashMap.Builder<E, Boolean>().initialCapacity(initialCapacity)
                .maximumWeightedCapacity(initialCapacity).concurrencyLevel(concurrencyLevel).build());
    }

    /**
     * Constructs a new {@code ConcurrentLinkedHashSet} containing the elements in the specified iterable collection.
     * The initial capacity of the backing map is determined by the size of the iterable, if it is a {@link Collection}.
     *
     * @param iter The collection whose elements are to be placed into this set.
     */
    public ConcurrentLinkedHashSet(final Iterable<E> iter) {
        super(iter instanceof Collection
                ? new ConcurrentLinkedHashMap.Builder<E, Boolean>().initialCapacity(((Collection<E>) iter).size())
                        .build()
                : new ConcurrentLinkedHashMap.Builder<E, Boolean>().build());
        if (iter instanceof Collection) {
            this.addAll((Collection<E>) iter);
        } else {
            for (final E e : iter) {
                this.add(e);
            }
        }
    }

}
