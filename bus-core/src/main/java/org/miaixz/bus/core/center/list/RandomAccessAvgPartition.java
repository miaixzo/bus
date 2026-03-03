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
