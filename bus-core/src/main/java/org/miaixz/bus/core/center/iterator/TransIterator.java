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
package org.miaixz.bus.core.center.iterator;

import java.util.Iterator;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;

/**
 * Transforms a source {@link Iterator} into a new {@link Iterator} of a different type by applying a given
 * transformation function to each element.
 *
 * @param <F> the type of elements in the source iterator
 * @param <T> the type of elements in the transformed iterator
 * @author Kimi Liu
 * @since Java 17+
 */
public class TransIterator<F, T> implements Iterator<T> {

    /**
     * The backing iterator providing the source elements.
     */
    private final Iterator<? extends F> backingIterator;
    /**
     * The function used to transform elements from type F to type T.
     */
    private final Function<? super F, ? extends T> func;

    /**
     * Constructs a {@code TransIterator} with a backing iterator and a transformation function.
     *
     * @param backingIterator the source {@link Iterator}, must not be {@code null}
     * @param func            the transformation {@link Function}, must not be {@code null}
     * @throws NullPointerException if {@code backingIterator} or {@code func} is {@code null}
     */
    public TransIterator(final Iterator<? extends F> backingIterator, final Function<? super F, ? extends T> func) {
        this.backingIterator = Assert.notNull(backingIterator, "Backing iterator must not be null");
        this.func = Assert.notNull(func, "Transformation function must not be null");
    }

    /**
     * Returns {@code true} if the iteration has more elements. This is determined by whether the backing iterator has
     * more elements.
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public final boolean hasNext() {
        return backingIterator.hasNext();
    }

    /**
     * Returns the next transformed element in the iteration. The element is obtained from the backing iterator and then
     * transformed using the provided function.
     *
     * @return the next transformed element in the iteration
     * @throws java.util.NoSuchElementException if the iteration has no more elements
     */
    @Override
    public final T next() {
        return func.apply(backingIterator.next());
    }

    /**
     * Removes from the underlying collection the last element returned by this iterator. This method delegates the
     * remove operation to the backing iterator. This method can be called only once per call to {@link #next()}.
     *
     * @throws IllegalStateException         if the {@code next()} method has not yet been called, or the
     *                                       {@code remove()} method has already been called after the last call to the
     *                                       {@code next()} method.
     * @throws UnsupportedOperationException if the {@code remove} operation is not supported by the backing iterator.
     */
    @Override
    public final void remove() {
        backingIterator.remove();
    }

}
