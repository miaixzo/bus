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
 * List partitioning or segmentation for {@link RandomAccess} lists. By specifying the partition length, a given list is
 * divided into different blocks, with each block having the same length (the last block may be shorter). This class
 * extends {@link Partition} and additionally implements {@link RandomAccess}, indicating that its sub-lists can be
 * accessed efficiently by index. Partitioning is performed on the original list. The returned partitions are immutable
 * abstract lists, and changes to the original list's elements will also be reflected in the partitions. Inspired by
 * Guava's Lists#RandomAccessPartition.
 *
 * @param <T> the type of elements in the list
 * @author Kimi Liu
 * @since Java 17+
 */
public class RandomAccessPartition<T> extends Partition<T> implements RandomAccess {

    /**
     * Constructs a {@code RandomAccessPartition} for the given list and partition size.
     *
     * @param list the list to be partitioned, must implement {@link RandomAccess} and not be {@code null}
     * @param size the length of each partition. Must be greater than 0.
     * @throws NullPointerException     if {@code list} is {@code null}
     * @throws IllegalArgumentException if {@code size} is less than or equal to 0
     */
    public RandomAccessPartition(final List<T> list, final int size) {
        super(list, size);
    }

}
