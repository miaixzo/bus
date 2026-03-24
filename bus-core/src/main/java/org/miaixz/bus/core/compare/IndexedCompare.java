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
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;

/**
 * A comparator that sorts elements based on a predefined index. The position of an element determines its sort order.
 * If an element is not in the index, it is sorted at the beginning by default, but this can be configured with
 * {@code atEndIfMiss}.
 *
 * @param <T> the type of elements to be compared.
 * @author Kimi Liu
 * @since Java 21+
 */
public class IndexedCompare<T> implements Comparator<T> {

    /**
     * Whether to place elements not in the list at the end.
     */
    private final boolean atEndIfMiss;
    /**
     * A map storing the position of each object, where the key is the object and the value is its index.
     */
    private final Map<? super T, Integer> map;

    /**
     * Constructs a new {@code IndexedCompare}.
     *
     * @param objs the array of objects that defines the sort order.
     */
    public IndexedCompare(final T... objs) {
        this(false, objs);
    }

    /**
     * Constructs a new {@code IndexedCompare}.
     *
     * @param atEndIfMiss if {@code true}, elements not in the map will be placed at the end.
     * @param map         the map defining the sort order, where the value determines the priority.
     */
    public IndexedCompare(final boolean atEndIfMiss, final Map<? super T, Integer> map) {
        this.atEndIfMiss = atEndIfMiss;
        this.map = map;
    }

    /**
     * Constructs a new {@code IndexedCompare}.
     *
     * @param atEndIfMiss if {@code true}, elements not in the array will be placed at the end.
     * @param objs        the array of objects that defines the sort order.
     */
    public IndexedCompare(final boolean atEndIfMiss, final T... objs) {
        Assert.notNull(objs, "'objs' array must not be null");
        this.atEndIfMiss = atEndIfMiss;
        map = new HashMap<>(objs.length, 1);
        for (int i = 0; i < objs.length; i++) {
            map.put(objs[i], i);
        }
    }

    /**
     * Compare method.
     *
     * @return the int value
     */
    @Override
    public int compare(final T o1, final T o2) {
        final int index1 = getOrder(o1);
        final int index2 = getOrder(o2);

        if (index1 == index2) {
            if (index1 < 0 || index1 == this.map.size()) {
                // If either element is not in the map, maintain the original order.
                return 1;
            }

            // Same position means they are considered equal.
            return 0;
        }

        return Integer.compare(index1, index2);
    }

    /**
     * Finds the order value for the given object, which corresponds to its position in the original list.
     *
     * @param object the object to find.
     * @return the position. If not found, returns -1 if {@link #atEndIfMiss} is false, otherwise returns the map size.
     */
    private int getOrder(final T object) {
        Integer order = map.get(object);
        if (order == null) {
            order = this.atEndIfMiss ? this.map.size() : -1;
        }
        return order;
    }

}
