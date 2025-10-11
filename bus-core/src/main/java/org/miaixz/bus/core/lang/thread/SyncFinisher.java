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
package org.miaixz.bus.core.lang.thread;

import java.io.Closeable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * A synchronization finisher that allows one or more threads to wait until a set of operations being executed in other
 * threads has completed. This class is useful for coordinating the start and end of concurrent tasks, ensuring all
 * tasks begin at a specific point and all complete before proceeding.
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * // Simulate 1000 concurrent threads
 * SyncFinisher sf = new SyncFinisher(1000);
 * sf.addWorker(() -> {
 *     // Business logic to be concurrently tested
 * });
 * sf.start();
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SyncFinisher implements Closeable {

    /**
     * A set of workers to be executed concurrently.
     */
    private final Set<Worker> workers;
    /**
     * The number of threads to be used for concurrent execution.
     */
    private final int threadSize;
    /**
     * A latch used to ensure all worker threads start at approximately the same time.
     */
    private final CountDownLatch beginLatch;
    /**
     * The {@link ExecutorService} used to execute the worker threads.
     */
    private ExecutorService executorService;
    /**
     * Flag indicating whether all worker threads should begin at the same time.
     */
    private boolean isBeginAtSameTime;
    /**
     * A latch used to wait for all worker threads to complete their execution.
     */
    private CountDownLatch endLatch;

    /**
     * The exception handler for uncaught exceptions in worker threads.
     */
    private Thread.UncaughtExceptionHandler exceptionHandler;

    /**
     * Constructs a new {@code SyncFinisher} with the specified number of threads.
     *
     * @param threadSize The number of threads to be managed by this finisher.
     */
    public SyncFinisher(final int threadSize) {
        this.beginLatch = new CountDownLatch(1);
        this.threadSize = threadSize;
        this.workers = new LinkedHashSet<>();
    }

    /**
     * Sets whether all worker threads should begin execution at the same time.
     *
     * @param isBeginAtSameTime {@code true} if all worker threads should start simultaneously; {@code false} otherwise.
     * @return This {@code SyncFinisher} instance for method chaining.
     */
    public SyncFinisher setBeginAtSameTime(final boolean isBeginAtSameTime) {
        this.isBeginAtSameTime = isBeginAtSameTime;
        return this;
    }

    /**
     * Sets the {@link Thread.UncaughtExceptionHandler} for worker threads.
     *
     * @param exceptionHandler The exception handler to be set.
     * @return This {@code SyncFinisher} instance for method chaining.
     */
    public SyncFinisher setExceptionHandler(final Thread.UncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    /**
     * Adds a specified number of worker tasks, equal to the {@code threadSize}, to be executed. Each worker will
     * execute the provided {@link Runnable}.
     *
     * @param runnable The {@link Runnable} task to be executed by each worker.
     * @return This {@code SyncFinisher} instance for method chaining.
     */
    public SyncFinisher addRepeatWorker(final Runnable runnable) {
        for (int i = 0; i < this.threadSize; i++) {
            addWorker(new Worker() {

                @Override
                public void work() {
                    runnable.run();
                }
            });
        }
        return this;
    }

    /**
     * Adds a single worker task to be executed.
     *
     * @param runnable The {@link Runnable} task to be executed by the worker.
     * @return This {@code SyncFinisher} instance for method chaining.
     */
    public SyncFinisher addWorker(final Runnable runnable) {
        return addWorker(new Worker() {

            @Override
            public void work() {
                runnable.run();
            }
        });
    }

    /**
     * Adds a custom {@link Worker} instance to be executed.
     *
     * @param worker The {@link Worker} instance to add.
     * @return This {@code SyncFinisher} instance for method chaining.
     */
    synchronized public SyncFinisher addWorker(final Worker worker) {
        workers.add(worker);
        return this;
    }

    /**
     * Starts the execution of all added worker tasks. This method blocks until all tasks are completed. If this object
     * is no longer needed after calling this method, {@link #stop()} should be called to release resources.
     */
    public void start() {
        start(true);
    }

    /**
     * Starts the execution of all added worker tasks. If {@code sync} is {@code true}, this method blocks until all
     * tasks are completed. If this object is no longer needed after calling this method, {@link #stop()} should be
     * called to release resources.
     *
     * @param sync {@code true} to block and wait for all tasks to complete; {@code false} to execute asynchronously.
     * @throws InternalException if an {@link InterruptedException} occurs while waiting for tasks to complete.
     */
    public void start(final boolean sync) {
        endLatch = new CountDownLatch(workers.size());

        if (null == this.executorService || this.executorService.isShutdown()) {
            this.executorService = buildExecutor();
        }
        for (final Worker worker : workers) {
            executorService.execute(worker);
        }
        // Ensure all workers start simultaneously
        this.beginLatch.countDown();

        if (sync) {
            try {
                this.endLatch.await();
            } catch (final InterruptedException e) {
                throw new InternalException(e);
            }
        }
    }

    /**
     * Stops the underlying {@link ExecutorService} and clears all worker tasks. This method can be called in two
     * scenarios:
     * <ol>
     * <li>After calling {@code start(true)}, to release resources.</li>
     * <li>After calling {@code start(false)}, when the user determines the completion point and wishes to stop the
     * service.</li>
     * </ol>
     */
    public void stop() {
        stop(false);
    }

    /**
     * Stops the underlying {@link ExecutorService} and clears all worker tasks. This method can be called in two
     * scenarios:
     * <ol>
     * <li>After calling {@code start(true)}, to release resources.</li>
     * <li>After calling {@code start(false)}, when the user determines the completion point and wishes to stop the
     * service.</li>
     * </ol>
     *
     * @param isStopNow {@code true} to immediately shut down all threads (including those currently executing);
     *                  {@code false} to allow currently executing tasks to complete before shutting down.
     */
    public void stop(final boolean isStopNow) {
        if (null != this.executorService) {
            if (isStopNow) {
                this.executorService.shutdownNow();
            } else {
                this.executorService.shutdown();
            }
            this.executorService = null;
        }

        clearWorker();
    }

    /**
     * Clears all registered worker tasks from this finisher.
     */
    public void clearWorker() {
        workers.clear();
    }

    /**
     * Returns the current count of remaining tasks that have not yet completed.
     *
     * @return The number of tasks remaining.
     */
    public long count() {
        return endLatch.getCount();
    }

    /**
     * Closes this {@code SyncFinisher}, stopping the underlying {@link ExecutorService} and releasing resources. This
     * is equivalent to calling {@link #stop()}.
     */
    @Override
    public void close() {
        stop();
    }

    /**
     * Builds and returns an {@link ExecutorService} with a custom exception handler.
     *
     * @return A new {@link ExecutorService} instance.
     */
    private ExecutorService buildExecutor() {
        return ExecutorBuilder.of().setCorePoolSize(threadSize)
                .setThreadFactory(new NamedThreadFactory("X-", null, false, exceptionHandler)).build();
    }

    /**
     * Abstract worker class representing a task to be executed by a thread. Workers can be configured to start
     * simultaneously with other workers.
     */
    public abstract class Worker implements Runnable {

        /**
         * Executes the worker's task. If {@code isBeginAtSameTime} is true, this method waits for the
         * {@code beginLatch} to count down before executing the {@code work()} method. After the work is done, it
         * decrements the {@code endLatch}.
         *
         * @throws InternalException if an {@link InterruptedException} occurs while waiting for the begin latch.
         */
        @Override
        public void run() {
            if (isBeginAtSameTime) {
                try {
                    beginLatch.await();
                } catch (final InterruptedException e) {
                    throw new InternalException(e);
                }
            }
            try {
                work();
            } finally {
                endLatch.countDown();
            }
        }

        /**
         * The core task content to be implemented by concrete worker classes.
         */
        public abstract void work();
    }

}
