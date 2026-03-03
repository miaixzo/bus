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
package org.miaixz.bus.core.compare;

import java.util.Comparator;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * Comparator that sorts elements based on their order in a given array. The position of an element in the array
 * determines its sort order. By default, if an element to be sorted is not in the array, it will be placed at the
 * beginning. This behavior can be changed by setting {@code atEndIfMiss}.
 *
 * @param <T> the type of elements to be compared.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ArrayCompare<T> implements Comparator<T> {

    /**
     * Whether to place elements not found in the array at the end of the sort order.
     */
    private final boolean atEndIfMiss;
    /**
     * The array that defines the sort order for elements.
     */
    private final T[] array;

    /**
     * Constructs a new {@code ArrayCompare}.
     *
     * @param objs the array of objects that defines the sort order.
     */
    public ArrayCompare(final T... objs) {
        this(false, objs);
    }

    /**
     * Constructs a new {@code ArrayCompare}.
     *
     * @param atEndIfMiss if {@code true}, elements not in the array will be placed at the end; otherwise, at the
     *                    beginning.
     * @param objs        the array of objects that defines the sort order.
     */
    public ArrayCompare(final boolean atEndIfMiss, final T... objs) {
        Assert.notNull(objs, "'objs' array must not be null");
        this.atEndIfMiss = atEndIfMiss;
        this.array = objs;
    }

    /**
     * Compares two objects based on their position in the order-defining array.
     * <p>
     * Elements found earlier in the array are considered "smaller" and will be sorted before elements found later.
     * Elements not present in the array are placed either at the beginning or end based on {@link #atEndIfMiss}.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     *         than the second.
     */
    @Override
    public int compare(final T o1, final T o2) {
        final int index1 = getOrder(o1);
        final int index2 = getOrder(o2);

        // If both elements have the same index (either found or not found)
        if (index1 == index2) {
            // If the index indicates the element is not in the list, maintain original order.
            if (index1 < 0 || index1 == this.array.length) {
                return 1; // Keep original order for elements not in the list
            }
        }

        return Integer.compare(index1, index2);
    }

    /**
     * Finds the position of the given object in the sort-order array.
     *
     * @param object the object to find.
     * @return the index of the object in the array. If not found, returns -1 if {@link #atEndIfMiss} is false, or the
     *         length of the array if {@link #atEndIfMiss} is true.
     */
    private int getOrder(final T object) {
        int order = ArrayKit.indexOf(array, object);
        if (order < 0) {
            order = this.atEndIfMiss ? this.array.length : -1;
        }
        return order;
    }

}
