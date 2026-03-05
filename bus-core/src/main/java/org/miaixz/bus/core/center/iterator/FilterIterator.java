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
