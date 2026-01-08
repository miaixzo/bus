/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.list;

import java.util.AbstractList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;

/**
 * List partitioning or segmentation. By specifying the partition length, a given list is divided into different blocks,
 * with each block having the same length (the last block may be shorter). Partitioning is performed on the original
 * list. The returned partitions are immutable abstract lists, and changes to the original list's elements will also be
 * reflected in the partitions. Inspired by Guava's Lists#Partition.
 *
 * @param <T> the type of elements in the list
 * @author Kimi Liu
 * @since Java 17+
 */
public class Partition<T> extends AbstractList<List<T>> {

    /**
     * The list to be partitioned.
     */
    protected final List<T> list;
    /**
     * The length of each partition.
     */
    protected final int size;

    /**
     * Constructs a {@code Partition} for the given list and partition size.
     *
     * @param list the list to be partitioned, must not be {@code null}
     * @param size the length of each partition. Must be greater than 0.
     * @throws NullPointerException     if {@code list} is {@code null}
     * @throws IllegalArgumentException if {@code size} is less than or equal to 0
     */
    public Partition(final List<T> list, final int size) {
        this.list = Assert.notNull(list);
        this.size = Math.min(size, list.size());
    }

    /**
     * Returns the partition (sub-list) at the specified index.
     *
     * @param index the index of the partition to retrieve
     * @return the partition (a sub-list) at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range ({@code index < 0 || index >= size()})
     */
    @Override
    public List<T> get(final int index) {
        final int start = index * size;
        final int end = Math.min(start + size, list.size());
        return list.subList(start, end);
    }

    /**
     * Returns the total number of partitions. This method dynamically calculates the number of partitions to account
     * for changes in the underlying list.
     *
     * @return the number of partitions
     */
    @Override
    public int size() {
        final int size = this.size;
        if (0 == size) {
            return 0;
        }

        final int total = list.size();
        // Similar to checking the remainder, if the total is not an exact multiple of size,
        // and the remainder is >= 1, it means there's one more partition.
        return (total + size - 1) / size;
    }

    /**
     * Returns {@code true} if this list contains no elements.
     *
     * @return {@code true} if this list contains no elements
     */
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

}
