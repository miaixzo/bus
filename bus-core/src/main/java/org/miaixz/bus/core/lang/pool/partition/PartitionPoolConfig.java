/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.lang.pool.partition;

import org.miaixz.bus.core.lang.pool.PoolConfig;

/**
 * 分局对象池配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PartitionPoolConfig extends PoolConfig {

    private static final long serialVersionUID = -1L;
    private int partitionSize = 4;

    /**
     * 创建{@code PartitionPoolConfig}
     *
     * @return {@code PartitionPoolConfig}
     */
    public static PartitionPoolConfig of() {
        return new PartitionPoolConfig();
    }

    /**
     * 获取分区大小
     *
     * @return 分区大小
     */
    public int getPartitionSize() {
        return partitionSize;
    }

    /**
     * 设置分区大小
     *
     * @param partitionSize 分区大小
     * @return this
     */
    public PartitionPoolConfig setPartitionSize(final int partitionSize) {
        this.partitionSize = partitionSize;
        return this;
    }

}
