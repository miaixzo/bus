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
package org.miaixz.bus.core.center.iterator;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A copied {@link Iterator} implementation designed to prevent {@link java.util.ConcurrentModificationException} when
 * iterating over a collection that might be modified concurrently. This is achieved by copying the elements of the
 * original iterator into a new list during construction, and then iterating over this new list.
 *
 * <p>
 * The {@code remove()} method of this iterator is not supported and will always throw an
 * {@link UnsupportedOperationException}, as modifications to the copied list would not affect the original collection.
 * 
 *
 * <p>
 * It is important to ensure atomicity during the construction of this object (i.e., the original object should not be
 * modified). It is recommended to acquire a lock before constructing this object and release it afterwards.
 * 
 *
 * @param <E> the type of elements returned by this iterator
 * @author Kimi Liu
 * @since Java 17+
 */
public class CopiedIterator<E> implements IterableIterator<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852259692199L;

    /**
     * The internal iterator that iterates over the copied list of elements.
     */
    private final Iterator<E> listIterator;

    /**
     * Constructs a {@code CopiedIterator} from the given {@link Iterator}. If the provided iterator is {@code null}, an
     * empty iterator will be used.
     *
     * @param iterator the iterator to be copied. Can be {@code null}.
     */
    public CopiedIterator(final Iterator<E> iterator) {
        final List<E> eleList = ListKit.of(ObjectKit.defaultIfNull(iterator, Collections.emptyIterator()));
        this.listIterator = eleList.iterator();
    }

    /**
     * Creates a new {@code CopiedIterator} from the given {@link Iterator}.
     *
     * @param iterator the iterator to be copied
     * @param <E>      the type of elements returned by this iterator
     * @return a new {@code CopiedIterator} instance
     */
    public static <E> CopiedIterator<E> copyOf(final Iterator<E> iterator) {
        return new CopiedIterator<>(iterator);
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return this.listIterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws java.util.NoSuchElementException if the iteration has no more elements
     */
    @Override
    public E next() {
        return this.listIterator.next();
    }

    /**
     * This operation is not supported by this iterator.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("This is a read-only iterator.");
    }

}
