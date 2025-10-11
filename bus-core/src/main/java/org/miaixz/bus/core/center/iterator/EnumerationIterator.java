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
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Adapts an {@link Enumeration} to the {@link Iterator} interface. This class allows treating an {@link Enumeration} as
 * an {@link Iterator}.
 *
 * @param <E> the type of elements returned by this iterator
 * @author Kimi Liu
 * @since Java 17+
 */
public class EnumerationIterator<E> implements IterableIterator<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852259861780L;

    /**
     * The underlying {@link Enumeration} being adapted.
     */
    private final Enumeration<E> e;

    /**
     * Constructs an {@code EnumerationIterator} from the given {@link Enumeration}.
     *
     * @param enumeration the {@link Enumeration} to adapt
     */
    public EnumerationIterator(final Enumeration<E> enumeration) {
        this.e = enumeration;
    }

    /**
     * Returns {@code true} if this enumeration has more elements. (In other words, returns {@code true} if
     * {@link #next} would return an element rather than throwing an exception.)
     *
     * @return {@code true} if the enumeration has more elements
     */
    @Override
    public boolean hasNext() {
        return e.hasMoreElements();
    }

    /**
     * Returns the next element of this enumeration.
     *
     * @return the next element of this enumeration
     * @throws java.util.NoSuchElementException if this enumeration has no more elements
     */
    @Override
    public E next() {
        return e.nextElement();
    }

    /**
     * This operation is not supported by this iterator.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
