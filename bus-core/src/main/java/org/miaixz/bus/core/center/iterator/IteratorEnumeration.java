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
import java.util.NoSuchElementException;

/**
 * Adapts an {@link Iterator} to the {@link Enumeration} interface. This class allows treating an {@link Iterator} as an
 * {@link Enumeration}.
 *
 * @param <E> the type of elements returned by this enumeration
 * @author Kimi Liu
 * @since Java 21+
 */
public class IteratorEnumeration<E> implements Enumeration<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852266956805L;

    /**
     * The underlying {@link Iterator} being adapted.
     */
    private final Iterator<E> iterator;

    /**
     * Constructs an {@code IteratorEnumeration} from the given {@link Iterator}.
     *
     * @param iterator the {@link Iterator} to adapt
     */
    public IteratorEnumeration(final Iterator<E> iterator) {
        this.iterator = iterator;
    }

    /**
     * Tests if this enumeration contains more elements.
     *
     * @return {@code true} if and only if this enumeration object contains at least one more element to provide;
     *         {@code false} otherwise.
     */
    @Override
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    /**
     * Returns the next element of this enumeration.
     *
     * @return the next element of this enumeration.
     * @throws NoSuchElementException if this enumeration has no more elements.
     */
    @Override
    public E nextElement() {
        return iterator.next();
    }

}
