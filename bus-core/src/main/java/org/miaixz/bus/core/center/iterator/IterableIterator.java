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

/**
 * Provides a composite interface that combines the functionalities of {@link Iterable} and {@link Iterator}. This
 * allows an object to be both iterable (meaning it can be used in a for-each loop) and an iterator itself.
 *
 * @param <T> the type of elements returned by this iterator
 * @author Kimi Liu
 * @since Java 21+
 */
public interface IterableIterator<T> extends Iterable<T>, Iterator<T> {

    /**
     * Returns an iterator over elements of type {@code T}. This default implementation returns {@code this}, allowing
     * the {@code IterableIterator} itself to be used as its own iterator.
     *
     * @return an Iterator.
     */
    @Override
    default Iterator<T> iterator() {
        return this;
    }

}
