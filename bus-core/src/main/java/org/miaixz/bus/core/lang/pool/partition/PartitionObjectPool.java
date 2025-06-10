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

import java.io.IOException;
import java.io.Serial;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.miaixz.bus.core.lang.pool.ObjectFactory;
import org.miaixz.bus.core.lang.pool.ObjectPool;
import org.miaixz.bus.core.lang.pool.Poolable;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * 分区对象池实现
 *
 * @param <T> 对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class PartitionObjectPool<T> implements ObjectPool<T> {

    @Serial
    private static final long serialVersionUID = 2852272377912L;

    private final PartitionPoolConfig config;
    /**
     * 分区，创建后不再变更，线程安全
     */
    private final PoolPartition<T>[] partitions;

    private boolean closed;

    /**
     * 构造
     *
     * @param config  配置
     * @param factory 对象工厂，用于创建、验证和销毁对象
     */
    public PartitionObjectPool(final PartitionPoolConfig config, final ObjectFactory<T> factory) {
        this.config = config;

        final int partitionSize = config.getPartitionSize();
        this.partitions = new PoolPartition[partitionSize];
        for (int i = 0; i < partitionSize; i++) {
            partitions[i] = new PoolPartition<>(config, createBlockingQueue(config), factory);
        }
    }

    /**
     * 获取持有对象总数
     *
     * @return 总数
     */
    @Override
    public int getTotal() {
        int size = 0;
        for (final PoolPartition<T> subPool : partitions) {
            size += subPool.getTotal();
        }
        return size;
    }

    @Override
    public int getIdleCount() {
        int size = 0;
        for (final PoolPartition<T> subPool : partitions) {
            size += subPool.getIdleCount();
        }
        return size;
    }

    @Override
    public int getActiveCount() {
        int size = 0;
        for (final PoolPartition<T> subPool : partitions) {
            size += subPool.getActiveCount();
        }
        return size;
    }

    @Override
    public T borrowObject() {
        checkClosed();
        return this.partitions[getPartitionIndex(this.config)].borrowObject();
    }

    @Override
    public PartitionObjectPool<T> returnObject(final T object) {
        checkClosed();
        this.partitions[getPartitionIndex(this.config)].returnObject(object);
        return this;
    }

    @Override
    public ObjectPool<T> free(final T object) {
        checkClosed();
        this.partitions[getPartitionIndex(this.config)].free(object);
        return this;
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
        IoKit.closeQuietly(this.partitions);
    }

    /**
     * 创建阻塞队列，默认为{@link ArrayBlockingQueue} 如果需要自定义队列类型，子类重写此方法
     *
     * @param poolConfig 池配置
     * @return 队列
     */
    protected BlockingQueue<Poolable<T>> createBlockingQueue(final PartitionPoolConfig poolConfig) {
        return new ArrayBlockingQueue<>(poolConfig.getMaxSize());
    }

    /**
     * 获取当前线程被分配的分区 默认根据线程ID（TID）取分区大小余数 如果需要自定义，子类重写此方法
     *
     * @param poolConfig 池配置
     * @return 分配的分区
     */
    protected int getPartitionIndex(final PartitionPoolConfig poolConfig) {
        return (int) (ThreadKit.currentThreadId() % poolConfig.getPartitionSize());
    }

    /**
     * 检查池是否关闭
     */
    private void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("Object Pool is closed!");
        }
    }

    @Override
    public String toString() {
        return StringKit.format("PartitionObjectPool: total: {}, idle: {}, active: {}", getTotal(), getIdleCount(),
                getActiveCount());
    }

}
