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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.pool.ObjectFactory;
import org.miaixz.bus.core.lang.pool.ObjectPool;
import org.miaixz.bus.core.lang.pool.PoolConfig;
import org.miaixz.bus.core.lang.pool.Poolable;

/**
 * Represents a partitioned object pool. Each partition acts as a small object pool, holding a blocking queue of
 * {@link Poolable} objects. Upon initialization, it creates {@link PoolConfig#getMinSize()} objects as initial pool
 * objects.
 *
 * <p>
 * When an object is borrowed, it is taken from the head of the queue and validated. If validation passes, the object is
 * used. If validation fails, the object is destroyed by calling {@link #free(Object)} and a new attempt to acquire an
 * object is made.\n * If all objects in the pool are borrowed (the queue is empty), new objects are created and added
 * to the queue until it is full. If the queue is full, borrowing threads will wait for an object to be returned; a
 * timeout will result in an error.
 *
 * @param <T> the type of objects managed by this pool partition
 * @author Kimi Liu
 * @since Java 17+
 */
public class PartitionPool<T> implements ObjectPool<T> {

    @Serial
    private static final long serialVersionUID = 2852272813570L;

    /**
     * The configuration settings for this object pool partition.
     */
    private final PoolConfig config;
    /**
     * The factory responsible for creating, validating, and destroying objects.
     */
    private final ObjectFactory<T> objectFactory;
    /**
     * The blocking queue that holds the available poolable objects.
     */
    private BlockingQueue<Poolable<T>> queue;
    /**
     * The total number of objects currently managed by this partition, including both idle and actively borrowed
     * objects.
     */
    private int total;

    /**
     * Constructs a new {@code PoolPartition} with the specified configuration, blocking queue, and object factory.
     * Initializes the pool by creating objects up to the minimum pool size.
     *
     * @param config        the pool configuration
     * @param queue         the blocking queue to store poolable objects
     * @param objectFactory the object factory for managing object lifecycle
     */
    public PartitionPool(final PoolConfig config, final BlockingQueue<Poolable<T>> queue,
            final ObjectFactory<T> objectFactory) {
        this.config = config;
        this.queue = queue;
        this.objectFactory = objectFactory;

        // Initialize the pool by creating objects up to the configured minimum size.
        doIncrease(config.getMinSize());
    }

    /**
     * Borrows an object from the pool. The process involves:
     * <ol>
     * <li>Attempting to retrieve an object from the queue without blocking.</li>
     * <li>If an object is retrieved, it is validated.</li>
     * <li>If valid, it checks if the object has exceeded its maximum idle time.</li>
     * <li>If the object is invalid or has exceeded idle time, it is freed, and the borrowing process is retried.</li>
     * <li>If no object is immediately available, the pool attempts to increase its size.</li>
     * <li>If the pool cannot increase (e.g., at max size), it waits for an object to be returned.</li>
     * <li>If waiting times out or no object becomes available, an {@link InternalException} is thrown.</li>
     * </ol>
     *
     * @return the borrowed object
     * @throws InternalException if the pool is exhausted and no object can be borrowed within the configured wait time
     */
    @Override
    public T borrowObject() {
        // Non-blocking retrieval
        Poolable<T> poolable = this.queue.poll();
        if (null != poolable) {
            final T object = poolable.getRaw();
            // Check object validity
            if (this.objectFactory.validate(object)) {
                // Check if the object has exceeded its maximum idle time
                final long maxIdle = this.config.getMaxIdle();
                if (maxIdle <= 0 || poolable.getIdle() <= maxIdle) {
                    return object;
                }
            }

            // Object is invalid or has exceeded maximum idle time, destroy it
            free(object);
            // Continue to borrow without increasing pool size immediately
            return borrowObject();
        }

        // No object in the pool, attempt to increase pool size
        if (increase(1) <= 0) {
            // Pool partition is full, wait for an object to be returned
            poolable = waitingPoll();
            if (null == poolable) {
                // Pool has reached maximum capacity, but no object is available
                throw new InternalException("Pool exhausted!");
            }
        }

        // Pool expansion successful, continue to borrow an object.
        // If thread 1 expands the pool but thread 2 borrows the object, recursively try to get an object
        // until one is acquired or all are borrowed.
        return borrowObject();
    }

    /**
     * Returns an object to the pool. The process involves:
     * <ol>
     * <li>Checking the object's validity using the {@link ObjectFactory}.</li>
     * <li>If the object is valid, it is wrapped as a {@link Poolable} and added back to the queue.</li>
     * <li>If the object is invalid, it is destroyed via {@link #free(Object)}.</li>
     * </ol>
     *
     * @param object the object to be returned
     * @return this object pool instance for method chaining
     * @throws InternalException if the thread is interrupted while attempting to return the object to the queue
     */
    @Override
    public PartitionPool<T> returnObject(final T object) {
        // Check object validity
        if (this.objectFactory.validate(object)) {
            try {
                this.queue.put(wrapPoolable(object));
            } catch (final InterruptedException e) {
                throw new InternalException(e);
            }
        } else {
            // Object is invalid
            free(object);
        }

        return this;
    }

    /**
     * Increases the capacity of the object pool and populates the queue with new objects. If the requested
     * {@code increaseSize} would exceed the maximum pool size when added to the current total, the actual increase will
     * be limited to reach the maximum size.
     *
     * @param increaseSize the number of objects to add to the pool
     * @return the actual number of objects added to the pool. Returns 0 if the pool is already at its maximum capacity
     *         and no objects could be added.
     */
    public synchronized int increase(final int increaseSize) {
        return doIncrease(increaseSize);
    }

    /**
     * Destroys an object. This method should be used when an object is found to be corrupted or no longer needed and is
     * outside of the pool's queue (e.g., a borrowed object that became invalid).
     *
     * @param object the object to be destroyed
     * @return this object pool instance for method chaining
     */
    @Override
    public synchronized PartitionPool<T> free(final T object) {
        objectFactory.destroy(object);
        total--;
        return this;
    }

    /**
     * Retrieves the total number of objects currently managed by this pool partition. This includes both idle objects
     * in the queue and objects currently borrowed.
     *
     * @return the total count of objects in the partition
     */
    @Override
    public int getTotal() {
        return this.total;
    }

    /**
     * Retrieves the number of idle objects currently available in the pool's queue.
     *
     * @return the count of idle objects
     */
    @Override
    public int getIdleCount() {
        return this.queue.size();
    }

    /**
     * Retrieves the number of objects currently borrowed and in active use from this pool partition.
     *
     * @return the count of actively used objects
     */
    @Override
    public int getActiveCount() {
        return getTotal() - getIdleCount();
    }

    /**
     * Closes the object pool, destroying all objects currently held within it and releasing resources. After calling
     * this method, the pool should no longer be used.
     *
     * @throws IOException if an I/O error occurs during the closing process (though not directly applicable here, it's
     *                     part of Closeable interface).
     */
    @Override
    synchronized public void close() throws IOException {
        this.queue.forEach((poolable) -> objectFactory.destroy(poolable.getRaw()));
        this.queue.clear();
        this.queue = null;
        this.total = 0;
    }

    /**
     * Creates a new {@link Poolable} instance by using the configured {@link ObjectFactory}. If the object factory
     * returns {@code null}, this method also returns {@code null}.
     *
     * @return a new {@link Poolable} instance wrapping a newly created object, or {@code null} if object creation fails
     */
    protected Poolable<T> createPoolable() {
        final T t = objectFactory.create();
        return null == t ? null : wrapPoolable(t);
    }

    /**
     * Wraps a raw object into a {@link Poolable} instance. If the object is already an instance of {@link Poolable}, it
     * is cast and returned directly. Otherwise, a new {@link PartitionPoolable} is created to wrap the object.
     *
     * @param t the raw object to wrap
     * @return a {@link Poolable} instance wrapping the given object
     */
    private Poolable<T> wrapPoolable(final T t) {
        if (t instanceof Poolable) {
            return (Poolable<T>) t;
        }
        return new PartitionPoolable<>(t, this);
    }

    /**
     * Non-thread-safe method to increase the capacity of the object pool and populate the queue with new objects. This
     * method is called by {@link #increase(int)} after synchronization. If the requested {@code increaseSize} would
     * exceed the maximum pool size when added to the current total, the actual increase will be limited to reach the
     * maximum size.
     *
     * @param increaseSize the number of objects to attempt to add to the pool
     * @return the actual number of objects added to the pool. Returns 0 if the pool is already at its maximum capacity
     *         and no objects could be added.
     * @throws InternalException if the thread is interrupted during the process of adding objects to the queue
     */
    private int doIncrease(int increaseSize) {
        final int maxSize = config.getMaxSize();
        if (increaseSize + total > maxSize) {
            increaseSize = maxSize - total;
        }

        try {
            for (int i = 0; i < increaseSize; i++) {
                queue.put(createPoolable());
            }
            total += increaseSize;
        } catch (final InterruptedException e) {
            throw new InternalException(e);
        }
        return increaseSize;
    }

    /**
     * Retrieves an object from the head of the queue, waiting if the queue is empty. The waiting time is determined by
     * {@link PoolConfig#getMaxWait()}. If {@code maxWait} is less than or equal to 0, it waits indefinitely; otherwise,
     * it waits for the specified milliseconds.
     *
     * @return the retrieved poolable object, or {@code null} if the wait time expires
     * @throws InternalException if the thread is interrupted while waiting for an object
     */
    private Poolable<T> waitingPoll() throws InternalException {
        final long maxWait = this.config.getMaxWait();
        try {
            if (maxWait <= 0) {
                return this.queue.take();
            }
            return this.queue.poll(maxWait, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            throw new InternalException(e);
        }
    }

}
