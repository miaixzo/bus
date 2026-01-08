/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
