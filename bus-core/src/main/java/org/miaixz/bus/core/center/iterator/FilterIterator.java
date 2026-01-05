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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Assert;

/**
 * A {@code FilterIterator} wraps another {@link Iterator} and filters its elements based on a {@link Predicate}. This
 * implementation is inspired by Apache Commons Collections.
 *
 * @param <E> The type of the elements.
 * @author Kimi Liu
 * @since Java 17+
 */
public class FilterIterator<E> implements Iterator<E> {

    /**
     * The underlying iterator being filtered.
     */
    private final Iterator<? extends E> iterator;
    /**
     * The predicate used to filter elements.
     */
    private final Predicate<? super E> filter;

    /**
     * The next element that matches the predicate.
     */
    private E nextObject;
    /**
     * A flag indicating whether the next element has been calculated.
     */
    private boolean nextObjectSet = false;

    /**
     * Constructs a new {@code FilterIterator}.
     *
     * @param iterator The {@link Iterator} to be wrapped.
     * @param filter   The filter predicate. If {@code null}, no filtering is applied.
     * @throws NullPointerException if the iterator is {@code null}.
     */
    public FilterIterator(final Iterator<? extends E> iterator, final Predicate<? super E> filter) {
        this.iterator = Assert.notNull(iterator);
        this.filter = filter;
    }

    /**
     * Returns true if the iteration has more elements.
     *
     * @return true if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return nextObjectSet || setNextObject();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element
     */
    @Override
    public E next() {
        if (!nextObjectSet && !setNextObject()) {
            throw new NoSuchElementException();
        }
        nextObjectSet = false;
        return nextObject;
    }

    /**
     * Removes from the underlying collection the last element returned by this iterator.
     */
    @Override
    public void remove() {
        if (nextObjectSet) {
            throw new IllegalStateException("remove() cannot be called before next()");
        }
        iterator.remove();
    }

    /**
     * Gets the underlying (wrapped) iterator.
     *
     * @return The wrapped {@link Iterator}.
     */
    public Iterator<? extends E> getIterator() {
        return iterator;
    }

    /**
     * Gets the predicate used for filtering.
     *
     * @return The filter predicate, which may be {@code null}.
     */
    public Predicate<? super E> getFilter() {
        return filter;
    }

    /**
     * Finds and sets the next object that matches the predicate.
     *
     * @return {@code true} if a matching element is found, {@code false} otherwise.
     */
    private boolean setNextObject() {
        while (iterator.hasNext()) {
            final E object = iterator.next();
            if (null == filter || filter.test(object)) {
                nextObject = object;
                nextObjectSet = true;
                return true;
            }
        }
        return false;
    }

}
