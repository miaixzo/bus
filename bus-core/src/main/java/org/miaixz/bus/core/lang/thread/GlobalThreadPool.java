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
