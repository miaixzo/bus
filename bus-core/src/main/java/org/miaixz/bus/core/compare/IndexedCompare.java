/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 17+
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
