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

import org.miaixz.bus.core.lang.pool.Poolable;

/**
 * 分区可池化对象，此对象会同时持有原始对象和所在的分区
 *
 * @param <T> 对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class PartitionPoolable<T> implements Poolable<T> {

    private final T raw;
    private final PoolPartition<T> partition;
    private long lastBorrow;

    /**
     * 构造
     *
     * @param raw       原始对象
     * @param partition 对象所在分区
     */
    public PartitionPoolable(final T raw, final PoolPartition<T> partition) {
        this.raw = raw;
        this.partition = partition;
        this.lastBorrow = System.currentTimeMillis();
    }

    @Override
    public T getRaw() {
        return this.raw;
    }

    /**
     * 归还对象
     */
    public void returnObject() {
        this.partition.returnObject(this);
    }

    @Override
    public long getLastBorrow() {
        return lastBorrow;
    }

    @Override
    public void setLastBorrow(final long lastBorrow) {
        this.lastBorrow = lastBorrow;
    }

}
