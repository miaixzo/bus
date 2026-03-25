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
 * @since Java 21+
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
