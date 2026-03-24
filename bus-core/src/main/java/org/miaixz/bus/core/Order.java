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
package org.miaixz.bus.core;

/**
 * An interface for objects that can be ordered. This is often used for sorting components or plugins.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Order extends Comparable<Order> {

    /**
     * A constant holding the minimum value an {@code int} can have, -2<sup>31</sup>.
     */
    int MIN_VALUE = Integer.MIN_VALUE;

    /**
     * A constant holding the maximum value an {@code int} can have, 2<sup>31</sup>-1.
     */
    int MAX_VALUE = Integer.MAX_VALUE;

    /**
     * Gets the order value of this object. A smaller value represents a higher priority.
     *
     * @return The order value.
     */
    default int order() {
        return MIN_VALUE;
    }

    /**
     * Compares this object with the specified object for order. The comparison is based on the value returned by the
     * {@link #order()} method.
     *
     * @param o The object to be compared.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object.
     */
    @Override
    default int compareTo(Order o) {
        return Integer.compare(order(), o.order());
    }

}
