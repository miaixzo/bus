/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.iterator;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.miaixz.bus.core.lang.Assert;

/**
 * A utility for iterating over data in batches (partitions). This is useful in scenarios such as:
 * <ol>
 * <li>Calling external client APIs that have limitations on the number of input parameters, requiring batch
 * processing.</li>
 * <li>Querying databases (e.g., MySQL/Oracle) with 'IN' clauses, where the number of items exceeds a certain limit
 * (e.g., 1000), necessitating batching.</li>
 * <li>Processing data from database cursors, where data can be handled in chunks.</li>
 * </ol>
 *
 * @param <T> the type of elements being iterated over
 * @author Kimi Liu
 * @since Java 17+
 */
public class PartitionIterator<T> implements IterableIterator<List<T>>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852267311619L;

    /**
     * The underlying iterator whose elements are to be partitioned.
     */
    protected final Iterator<T> iterator;
    /**
     * The size of each partition (batch). The last batch may contain fewer elements than this size.
     */
    protected final int partitionSize;

    /**
     * Constructs a {@code PartitionIterator} with the given iterator and partition size.
     *
     * @param iterator      the iterator to partition, must not be {@code null}
     * @param partitionSize the maximum number of elements in each partition. Must be greater than 0. The last partition
     *                      may contain fewer elements.
     * @throws IllegalArgumentException if {@code partitionSize} is less than or equal to 0, or if {@code iterator} is
     *                                  {@code null}
     */
    public PartitionIterator(final Iterator<T> iterator, final int partitionSize) {
        Assert.isTrue(partitionSize > 0, "partition size must greater than 0");
        this.iterator = Objects.requireNonNull(iterator);
        this.partitionSize = partitionSize;
    }

    /**
     * Returns {@code true} if the iteration has more partitions. (In other words, returns {@code true} if {@link #next}
     * would return a list of elements rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more partitions
     */
    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    /**
     * Returns the next partition (a list of elements) in the iteration. The size of the returned list will be at most
     * {@code partitionSize}. The last list returned may be smaller than {@code partitionSize}.
     *
     * @return the next partition as a {@link List} of elements
     * @throws java.util.NoSuchElementException if the iteration has no more partitions
     */
    @Override
    public List<T> next() {
        final List<T> list = new ArrayList<>(this.partitionSize);
        for (int i = 0; i < this.partitionSize; i++) {
            if (!iterator.hasNext()) {
                break;
            }
            list.add(iterator.next());
        }
        return list;
    }

}
