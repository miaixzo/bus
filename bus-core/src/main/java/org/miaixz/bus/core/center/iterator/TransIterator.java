/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
