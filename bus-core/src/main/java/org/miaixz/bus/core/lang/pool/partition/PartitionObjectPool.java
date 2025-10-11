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
 * An implementation of an object pool that uses partitioning to manage objects. This pool divides its capacity into
 * multiple {@link PartitionPool} instances, each managing a subset of the total objects.
 *
 * @param <T> the type of objects managed by this partitioned object pool
 * @author Kimi Liu
 * @since Java 17+
 */
public class PartitionObjectPool<T> implements ObjectPool<T> {

    @Serial
    private static final long serialVersionUID = 2852272377912L;

    /**
     * The configuration for this partitioned object pool.
     */
    private final PartitionPoolConfig config;
    /**
     * An array of {@link PartitionPool} instances, representing the individual partitions of the pool. This array is
     * initialized once and remains thread-safe.
     */
    private final PartitionPool<T>[] partitions;

    /**
     * A flag indicating whether the object pool has been closed.
     */
    private boolean closed;

    /**
     * Constructs a new {@code PartitionObjectPool}.
     *
     * @param config  the configuration for the partitioned pool
     * @param factory the object factory responsible for creating, validating, and destroying objects
     */
    public PartitionObjectPool(final PartitionPoolConfig config, final ObjectFactory<T> factory) {
        this.config = config;

        final int partitionSize = config.getPartitionSize();
        this.partitions = new PartitionPool[partitionSize];
        for (int i = 0; i < partitionSize; i++) {
            partitions[i] = new PartitionPool<>(config, createBlockingQueue(config), factory);
        }
    }

    /**
     * Retrieves the total number of objects across all partitions in this pool. This includes both idle and actively
     * used objects.
     *
     * @return the total count of objects in the pool
     */
    @Override
    public int getTotal() {
        int size = 0;
        for (final PartitionPool<T> subPool : partitions) {
            size += subPool.getTotal();
        }
        return size;
    }

    /**
     * Retrieves the total number of idle objects across all partitions in this pool.
     *
     * @return the total count of idle objects
     */
    @Override
    public int getIdleCount() {
        int size = 0;
        for (final PartitionPool<T> subPool : partitions) {
            size += subPool.getIdleCount();
        }
        return size;
    }

    /**
     * Retrieves the total number of actively used (borrowed) objects across all partitions in this pool.
     *
     * @return the total count of actively used objects
     */
    @Override
    public int getActiveCount() {
        int size = 0;
        for (final PartitionPool<T> subPool : partitions) {
            size += subPool.getActiveCount();
        }
        return size;
    }

    /**
     * Borrows an object from one of the partitions. The partition is determined by
     * {@link #getPartitionIndex(PartitionPoolConfig)}.
     *
     * @return the borrowed object
     * @throws IllegalStateException if the pool is closed
     */
    @Override
    public T borrowObject() {
        checkClosed();
        return this.partitions[getPartitionIndex(this.config)].borrowObject();
    }

    /**
     * Returns an object to its respective partition. The partition is determined by
     * {@link #getPartitionIndex(PartitionPoolConfig)}.
     *
     * @param object the object to be returned
     * @return this {@code PartitionObjectPool} instance for method chaining
     * @throws IllegalStateException if the pool is closed
     */
    @Override
    public PartitionObjectPool<T> returnObject(final T object) {
        checkClosed();
        this.partitions[getPartitionIndex(this.config)].returnObject(object);
        return this;
    }

    /**
     * Frees (destroys) an object from its respective partition. The partition is determined by
     * {@link #getPartitionIndex(PartitionPoolConfig)}.
     *
     * @param object the object to be freed
     * @return this {@code ObjectPool} instance for method chaining
     * @throws IllegalStateException if the pool is closed
     */
    @Override
    public ObjectPool<T> free(final T object) {
        checkClosed();
        this.partitions[getPartitionIndex(this.config)].free(object);
        return this;
    }

    /**
     * Closes the object pool, releasing all resources and destroying all objects in its partitions. After this method
     * is called, the pool cannot be used.
     *
     * @throws IOException if an I/O error occurs during the closing of partitions
     */
    @Override
    public void close() throws IOException {
        this.closed = true;
        IoKit.closeQuietly(this.partitions);
    }

    /**
     * Creates a {@link BlockingQueue} for a pool partition. By default, an {@link ArrayBlockingQueue} is used.
     * Subclasses can override this method to provide a custom queue implementation.
     *
     * @param poolConfig the pool configuration, used to determine the queue capacity
     * @return a new {@link BlockingQueue} instance
     */
    protected BlockingQueue<Poolable<T>> createBlockingQueue(final PartitionPoolConfig poolConfig) {
        return new ArrayBlockingQueue<>(poolConfig.getMaxSize());
    }

    /**
     * Determines the index of the partition to which the current thread is assigned. By default, this is calculated
     * using the thread ID modulo the partition size. Subclasses can override this method to provide custom partition
     * assignment logic.
     *
     * @param poolConfig the pool configuration, used to get the partition size
     * @return the index of the assigned partition
     */
    protected int getPartitionIndex(final PartitionPoolConfig poolConfig) {
        return (int) (ThreadKit.currentThreadId() % poolConfig.getPartitionSize());
    }

    /**
     * Checks if the object pool is closed and throws an {@link IllegalStateException} if it is.
     *
     * @throws IllegalStateException if the object pool is closed
     */
    private void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("Object Pool is closed!");
        }
    }

    /**
     * Returns a string representation of the partitioned object pool, including total, idle, and active object counts.
     *
     * @return a formatted string representing the pool's current state
     */
    @Override
    public String toString() {
        return StringKit.format(
                "PartitionObjectPool: total: {}, idle: {}, active: {}",
                getTotal(),
                getIdleCount(),
                getActiveCount());
    }

}
