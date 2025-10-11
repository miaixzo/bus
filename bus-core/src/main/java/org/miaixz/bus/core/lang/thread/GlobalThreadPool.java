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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Global common thread pool. This thread pool is an unbounded thread pool, meaning that submitted tasks are executed
 * directly without waiting for other tasks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GlobalThreadPool {

    /**
     * The underlying {@link ExecutorService} for the global thread pool.
     */
    private static ExecutorService executor;

    static {
        init();
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private GlobalThreadPool() {
    }

    /**
     * Initializes or re-initializes the global thread pool. If an existing executor is present, it will be shut down
     * immediately before creating a new one. The new executor uses a {@link SynchronousQueue} to ensure immediate
     * execution of tasks.
     */
    synchronized public static void init() {
        if (null != executor) {
            executor.shutdownNow();
        }
        executor = ExecutorBuilder.of().useSynchronousQueue().build();
    }

    /**
     * Shuts down the global common thread pool.
     *
     * @param isNow {@code true} to attempt to stop all actively executing tasks, halts the processing of waiting tasks,
     *              and returns a list of the tasks that were awaiting execution. {@code false} to allow previously
     *              submitted tasks to execute, but no new tasks will be accepted.
     */
    synchronized public static void shutdown(final boolean isNow) {
        if (null != executor) {
            if (isNow) {
                executor.shutdownNow();
            } else {
                executor.shutdown();
            }
        }
    }

    /**
     * Retrieves the {@link ExecutorService} instance of the global thread pool.
     *
     * @return The {@link ExecutorService} used by the global thread pool.
     */
    public static ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Executes the given command in the global thread pool.
     *
     * @param runnable The {@link Runnable} task to execute.
     * @throws InternalException if an exception occurs during task submission.
     */
    public static void execute(final Runnable runnable) {
        try {
            executor.execute(runnable);
        } catch (final Exception e) {
            throw new InternalException(e, "Exception when running task!");
        }
    }

    /**
     * Submits a {@link Callable} task for execution and returns a {@link Future} representing that task. The
     * {@code Future}'s {@code get} method will return the task's result upon successful completion.
     *
     * @param <T>  The type of the task's result.
     * @param task The {@link Callable} task to submit.
     * @return A {@link Future} representing the pending completion of the task.
     */
    public static <T> Future<T> submit(final Callable<T> task) {
        return executor.submit(task);
    }

    /**
     * Submits a {@link Runnable} task for execution and returns a {@link Future} representing that task. The
     * {@code Future}'s {@code get} method will return {@code null} upon successful completion.
     *
     * @param runnable The {@link Runnable} task to submit.
     * @return A {@link Future} representing the pending completion of the task.
     */
    public static Future<?> submit(final Runnable runnable) {
        return executor.submit(runnable);
    }

}
