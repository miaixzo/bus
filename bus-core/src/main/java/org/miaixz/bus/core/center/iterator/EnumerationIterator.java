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
