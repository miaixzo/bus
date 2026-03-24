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
package org.miaixz.bus.core.center;

import java.util.Collection;

/**
 * Defines a collection with a fixed capacity limit. A bounded collection cannot store more elements than its maximum
 * size. Implementations of this interface determine the behavior when an attempt is made to add an element to a full
 * collection, such as throwing an exception, blocking the operation, or evicting an existing element.
 *
 * @param <E> The type of elements maintained by this collection.
 * @author Kimi Liu
 * @since Java 21+
 */
public interface BoundedCollection<E> extends Collection<E> {

    /**
     * Checks if the collection has reached its maximum capacity. An implementation should return {@code true} if
     * {@code size() >= maxSize()}.
     *
     * @return {@code true} if the collection is full, {@code false} otherwise.
     */
    boolean isFull();

    /**
     * Returns the maximum number of elements that this collection can hold.
     *
     * @return The maximum capacity of the collection, which is a non-negative integer.
     */
    int maxSize();

}
