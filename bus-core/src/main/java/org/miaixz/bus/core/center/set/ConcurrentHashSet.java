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
