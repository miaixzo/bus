/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.xyz;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Asynchronous utility class for {@link CompletableFuture}. {@link CompletableFuture} is an improvement over Future,
 * allowing callbacks to be registered and invoked upon task completion.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AsyncKit {

    /**
     * Constructs a new AsyncKit. Utility class constructor for static access.
     */
    private AsyncKit() {
    }

    /**
     * Waits for all tasks to complete, wrapping any exceptions.
     *
     * @param tasks The parallel tasks.
     * @throws UndeclaredThrowableException If an unchecked exception occurs during task execution.
     */
    public static void waitAll(final CompletableFuture<?>... tasks) {
        try {
            CompletableFuture.allOf(tasks).get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Waits for any one of the tasks to complete, wrapping any exceptions.
     *
     * @param <T>   The return type of the task.
     * @param tasks The parallel tasks.
     * @return The return value of the completed task.
     * @throws UndeclaredThrowableException If an unchecked exception occurs during task execution.
     */
    public static <T> T waitAny(final CompletableFuture<?>... tasks) {
        try {
            return (T) CompletableFuture.anyOf(tasks).get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves the result of an asynchronous task, wrapping any exceptions.
     *
     * @param <T>  The return type of the task.
     * @param task The asynchronous task.
     * @return The return value of the task.
     * @throws RuntimeException If an unchecked exception occurs during task execution.
     */
    public static <T> T get(final CompletableFuture<T> task) {
        try {
            return task.get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves the return values of all tasks.
     *
     * @param <T>   The return type of the task.
     * @param tasks The collection of tasks.
     * @return A list of task results.
     */
    public static <T> List<T> allOfGet(final List<CompletableFuture<T>> tasks) {
        Assert.notEmpty(tasks);

        return allOfGet(tasks, null);
    }

    /**
     * Retrieves the return values of all tasks.
     *
     * @param <T>   The return type of the task.
     * @param tasks The collection of tasks.
     * @return A list of task results.
     */
    @SafeVarargs
    public static <T> List<T> allOfGet(final CompletableFuture<T>... tasks) {
        Assert.notEmpty(tasks);

        return allOfGet(Arrays.asList(tasks), null);
    }

    /**
     * Retrieves the return values of all tasks, with an optional exception handler for failed tasks.
     *
     * @param <T>      The return type of the task.
     * @param tasks    The collection of tasks.
     * @param eHandler The exception handler method.
     * @return A list of task results.
     */
    public static <T> List<T> allOfGet(final CompletableFuture<T>[] tasks, final Function<Exception, T> eHandler) {
        Assert.notEmpty(tasks);

        return allOfGet(Arrays.asList(tasks), eHandler);
    }

    /**
     * Retrieves the return values of all tasks, with an optional exception handler for failed tasks.
     *
     * @param <T>      The return type of the task.
     * @param tasks    The collection of tasks.
     * @param eHandler The exception handler method.
     * @return A list of task results.
     */
    public static <T> List<T> allOfGet(final List<CompletableFuture<T>> tasks, final Function<Exception, T> eHandler) {
        Assert.notEmpty(tasks);

        return execute(tasks, eHandler, false);
    }

    /**
     * Retrieves the return values of all tasks, executed in parallel.
     *
     * @param <T>   The return type of the task.
     * @param tasks The collection of tasks.
     * @return A list of task results.
     */
    @SafeVarargs
    public static <T> List<T> parallelAllOfGet(final CompletableFuture<T>... tasks) {
        Assert.notEmpty(tasks);

        return parallelAllOfGet(Arrays.asList(tasks), null);
    }

    /**
     * Retrieves the return values of all tasks, executed in parallel.
     *
     * @param <T>   The return type of the task.
     * @param tasks The collection of tasks.
     * @return A list of task results.
     */
    public static <T> List<T> parallelAllOfGet(final List<CompletableFuture<T>> tasks) {
        Assert.notEmpty(tasks);

        return parallelAllOfGet(tasks, null);
    }

    /**
     * Retrieves the return values of all tasks, executed in parallel, with an optional exception handler for failed
     * tasks.
     *
     * @param <T>      The return type of the task.
     * @param tasks    The collection of tasks.
     * @param eHandler The exception handler method.
     * @return A list of task results.
     */
    public static <T> List<T> parallelAllOfGet(
            final CompletableFuture<T>[] tasks,
            final Function<Exception, T> eHandler) {
        Assert.notEmpty(tasks);

        return parallelAllOfGet(Arrays.asList(tasks), eHandler);
    }

    /**
     * Retrieves the return values of all tasks, executed in parallel, with an optional exception handler for failed
     * tasks.
     *
     * @param <T>      The return type of the task.
     * @param tasks    The collection of tasks.
     * @param eHandler The exception handler method.
     * @return A list of task results.
     */
    public static <T> List<T> parallelAllOfGet(
            final List<CompletableFuture<T>> tasks,
            final Function<Exception, T> eHandler) {
        Assert.notEmpty(tasks);

        return execute(tasks, eHandler, true);
    }

    /**
     * Processes a collection of tasks.
     *
     * @param <T>        The return type of the task.
     * @param tasks      The collection of tasks.
     * @param eHandler   The exception handler method.
     * @param isParallel Whether to execute in parallel using {@link Stream}.
     * @return A list of task results.
     */
    private static <T> List<T> execute(
            final List<CompletableFuture<T>> tasks,
            final Function<Exception, T> eHandler,
            final boolean isParallel) {
        return StreamKit.of(tasks, isParallel).map(e -> {
            try {
                return e.get();
            } catch (final InterruptedException | ExecutionException ex) {
                if (eHandler != null) {
                    return eHandler.apply(ex);
                } else {
                    throw ExceptionKit.wrapRuntime(ex);
                }
            }
        }).collect(Collectors.toList());
    }

}
