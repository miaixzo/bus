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
package org.miaixz.bus.core.center.list;

import java.util.List;
import java.util.RandomAccess;

/**
 * List partitioning or segmentation for {@link RandomAccess} lists. By specifying the number of partitions, a given
 * list is divided into different blocks, with the length of each block evenly distributed (the difference in count does
 * not exceed 1). This class extends {@link AvgPartition} and additionally implements {@link RandomAccess}, indicating
 * that its sub-lists can be accessed efficiently by index.
 * 
 * <pre>
 *     Example:
 *     List: [1, 2, 3, 4]
 *     Partition into 2: [1, 2], [3, 4]
 *     Partition into 3: [1, 2], [3], [4]
 *     Partition into 4: [1], [2], [3], [4]
 *     Partition into 5: [1], [2], [3], [4], []
 * </pre>
 * 
 * Partitioning is performed on the original list. The returned partitions are immutable abstract lists, and changes to
 * the original list's elements will also be reflected in the partitions.
 *
 * @param <T> the type of elements in the list
 * @author Kimi Liu
 * @since Java 17+
 */
public class RandomAccessAvgPartition<T> extends AvgPartition<T> implements RandomAccess {

    /**
     * Constructs a {@code RandomAccessAvgPartition} for the given list and number of partitions.
     *
     * @param list  the list to be partitioned, must not be {@code null}
     * @param limit the number of partitions. Must be greater than 0.
     * @throws IllegalArgumentException if {@code limit} is less than or equal to 0
     * @throws NullPointerException     if {@code list} is {@code null}
     */
    public RandomAccessAvgPartition(final List<T> list, final int limit) {
        super(list, limit);
    }

}
