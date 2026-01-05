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

import org.miaixz.bus.core.lang.Assert;

/**
 * A comparator that sorts objects based on the order of their types in a specified array.
 * <p>
 * If two compared objects are of the same type, it returns {@code 0}. By default, if an object's type is not in the
 * specified list, it is sorted at the beginning.
 * 
 * <p>
 * This class is adapted from Spring Framework with some modifications.
 * 
 *
 * @param <T> the type of objects to be compared.
 * @author Kimi Liu
 * @since Java 17+
 */
public class InstanceCompare<T> implements Comparator<T> {

    /**
     * Whether to place objects at the end if their type is not in the specified order.
     */
    private final boolean atEndIfMiss;
    /**
     * The array of classes that defines the sort order.
     */
    private final Class<?>[] instanceOrder;

    /**
     * Constructs a new {@code InstanceCompare}.
     *
     * @param instanceOrder an array of classes that defines the sort order based on their position.
     */
    public InstanceCompare(final Class<?>... instanceOrder) {
        this(false, instanceOrder);
    }

    /**
     * Constructs a new {@code InstanceCompare}.
     *
     * @param atEndIfMiss   if {@code true}, objects whose types are not in the list will be placed at the end.
     * @param instanceOrder an array of classes that defines the sort order based on their position.
     */
    public InstanceCompare(final boolean atEndIfMiss, final Class<?>... instanceOrder) {
        Assert.notNull(instanceOrder, "'instanceOrder' array must not be null");
        this.atEndIfMiss = atEndIfMiss;
        this.instanceOrder = instanceOrder;
    }

    /**
     * Compare method.
     *
     * @return the int value
     */
    @Override
    public int compare(final T o1, final T o2) {
        final int i1 = getOrder(o1);
        final int i2 = getOrder(o2);
        return Integer.compare(i1, i2);
    }

    /**
     * Finds the position of the object's type in the specified order.
     *
     * @param object the object to check.
     * @return the index in the order array. If not found, returns -1 if {@link #atEndIfMiss} is false, or the length of
     *         the array if it is true.
     */
    private int getOrder(final T object) {
        if (object != null) {
            for (int i = 0; i < this.instanceOrder.length; i++) {
                if (this.instanceOrder[i].isInstance(object)) {
                    return i;
                }
            }
        }
        return this.atEndIfMiss ? this.instanceOrder.length : -1;
    }

}
