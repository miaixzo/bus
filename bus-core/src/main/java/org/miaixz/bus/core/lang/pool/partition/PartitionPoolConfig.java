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
package org.miaixz.bus.core.lang.pool.partition;

import java.io.Serial;

import org.miaixz.bus.core.lang.pool.PoolConfig;

/**
 * Configuration class for a partitioned object pool. This extends {@link PoolConfig} to add specific settings for
 * partitioned pools, such as the number of partitions.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PartitionPoolConfig extends PoolConfig {

    @Serial
    private static final long serialVersionUID = 2852272551279L;
    /**
     * The number of partitions in the partitioned object pool. Each partition acts as an independent sub-pool.
     */
    private int partitionSize = 4;

    /**
     * Creates a new {@code PartitionPoolConfig} instance with default settings.
     *
     * @return a new {@code PartitionPoolConfig} instance
     */
    public static PartitionPoolConfig of() {
        return new PartitionPoolConfig();
    }

    /**
     * Retrieves the number of partitions configured for the object pool.
     *
     * @return the partition size
     */
    public int getPartitionSize() {
        return partitionSize;
    }

    /**
     * Sets the number of partitions for the object pool.
     *
     * @param partitionSize the new partition size
     * @return this {@code PartitionPoolConfig} instance for method chaining
     */
    public PartitionPoolConfig setPartitionSize(final int partitionSize) {
        this.partitionSize = partitionSize;
        return this;
    }

}
