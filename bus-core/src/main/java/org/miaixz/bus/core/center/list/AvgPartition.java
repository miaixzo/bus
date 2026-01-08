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

import java.util.List;

import org.miaixz.bus.core.lang.Assert;

/**
 * Partitions a list into a specified number of sublists of nearly equal size. The size difference between any two
 * sublists will not exceed 1.
 *
 * <pre>{@code
 * 
 * List<Integer> list = List.of(1, 2, 3, 4, 5);
 * AvgPartition<Integer> partition = new AvgPartition<>(list, 3);
 * // partition.get(0) -> [1, 2]
 * // partition.get(1) -> [3, 4]
 * // partition.get(2) -> [5]
 * }</pre>
 * <p>
 * The partitioning is done on the original list, and the returned sublists are views (backed by the original list).
 * Changes to the original list will be reflected in the sublists.
 *
 * @param <T> The type of elements in the list.
 * @author Kimi Liu
 * @since Java 17+
 */
public class AvgPartition<T> extends Partition<T> {

    /**
     * The desired number of partitions.
     */
    final int limit;
    /**
     * The number of remaining elements after division, which are distributed one by one among the first
     * {@code remainder} partitions.
     */
    final int remainder;

    /**
     * Constructs a list partitioner.
     *
     * @param list  The list to be partitioned.
     * @param limit The number of partitions.
     */
    public AvgPartition(final List<T> list, final int limit) {
        super(list, list.size() / (limit <= 0 ? 1 : limit));
        Assert.isTrue(limit > 0, "Partition limit must be > 0");
        this.limit = limit;
        this.remainder = list.size() % limit;
    }

    /**
     * Gets the sublist for the specified partition index.
     *
     * @param index The index of the partition.
     * @return The sublist.
     */
    @Override
    public List<T> get(final int index) {
        // The base size of each partition
        final int size = this.size;
        // The number of partitions that will get an extra element
        final int remainder = this.remainder;
        // Calculate the start index, accounting for the extra elements in previous partitions
        final int start = index * size + Math.min(index, remainder);
        int end = start + size;
        if (index < remainder) {
            // This partition is one of the first 'remainder' partitions, so it gets an extra element
            end += 1;
        }
        return list.subList(start, end);
    }

    /**
     * Returns the number of partitions.
     *
     * @return The number of partitions.
     */
    @Override
    public int size() {
        return limit;
    }

}
