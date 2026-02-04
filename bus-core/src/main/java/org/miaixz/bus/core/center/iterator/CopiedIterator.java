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
