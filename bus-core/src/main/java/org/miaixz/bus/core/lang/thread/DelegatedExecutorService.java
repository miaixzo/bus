/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Wrapper;

/**
 * A delegated implementation of {@link ExecutorService} that wraps an existing {@link ExecutorService}. This class
 * allows for extending or modifying the behavior of an {@link ExecutorService} without directly subclassing it, by
 * delegating all method calls to the wrapped instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DelegatedExecutorService extends AbstractExecutorService implements Wrapper<ExecutorService> {

    /**
     * The underlying {@link ExecutorService} to which all operations are delegated.
     */
    private final ExecutorService raw;

    /**
     * Constructs a new {@code DelegatedExecutorService} that wraps the given {@link ExecutorService}.
     *
     * @param executor The {@link ExecutorService} to be wrapped. Must not be {@code null}.
     * @throws NullPointerException if the provided executor is {@code null}.
     */
    public DelegatedExecutorService(final ExecutorService executor) {
        this.raw = Assert.notNull(executor, "executor must be not null !");
    }

    /**
     * Executes the given command at some time in the future. The command may execute in a new thread, in a pooled
     * thread, or in the calling thread, at the discretion of the {@code Executor} implementation.
     *
     * @param command The runnable task to execute.
     */
    @Override
    public void execute(final Runnable command) {
        this.raw.execute(command);
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be
     * accepted. Invocation has no additional effect if already shut down.
     */
    @Override
    public void shutdown() {
        this.raw.shutdown();
    }

    /**
     * Attempts to stop all actively executing tasks, halts the processing of waiting tasks, and returns a list of the
     * tasks that were awaiting execution.
     *
     * @return A list of tasks that never commenced execution.
     */
    @Override
    public List<Runnable> shutdownNow() {
        return this.raw.shutdownNow();
    }

    /**
     * Returns {@code true} if this executor has been shut down.
     *
     * @return {@code true} if this executor has been shut down.
     */
    @Override
    public boolean isShutdown() {
        return this.raw.isShutdown();
    }

    /**
     * Returns {@code true} if all tasks have completed following shut down. Note that {@code isTerminated} is never
     * {@code true} unless either {@code shutdown()} or {@code shutdownNow()} was called first.
     *
     * @return {@code true} if all tasks have completed following shut down.
     */
    @Override
    public boolean isTerminated() {
        return this.raw.isTerminated();
    }

    /**
     * Blocks until all tasks have completed execution after a shutdown request, or the timeout occurs, or the current
     * thread is interrupted, whichever happens first.
     *
     * @param timeout The maximum time to wait.
     * @param unit    The time unit of the timeout argument.
     * @return {@code true} if this executor terminated and {@code false} if the timeout elapsed before termination.
     * @throws InterruptedException if interrupted while waiting.
     */
    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.raw.awaitTermination(timeout, unit);
    }

    /**
     * Submits a {@link Runnable} task for execution and returns a {@link Future} representing that task. The
     * {@code Future}'s {@code get} method will return {@code null} upon successful completion.
     *
     * @param task The runnable task to submit.
     * @return A {@link Future} representing the pending completion of the task.
     */
    @Override
    public Future<?> submit(final Runnable task) {
        return this.raw.submit(task);
    }

    /**
     * Submits a {@link Callable} task for execution and returns a {@link Future} representing that task. The
     * {@code Future}'s {@code get} method will return the task's result upon successful completion.
     *
     * @param <T>  The type of the task's result.
     * @param task The callable task to submit.
     * @return A {@link Future} representing the pending completion of the task.
     */
    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        return this.raw.submit(task);
    }

    /**
     * Submits a {@link Runnable} task for execution and returns a {@link Future} representing that task. The
     * {@code Future}'s {@code get} method will return the given result upon successful completion.
     *
     * @param task   The runnable task to submit.
     * @param result The result to return on successful completion.
     * @param <T>    The type of the result.
     * @return A {@link Future} representing the pending completion of the task.
     */
    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        return this.raw.submit(task, result);
    }

    /**
     * Executes the given tasks, returning a list of Futures holding their status and results when all complete.
     * {@link Future#isDone} is {@code true} for each element of the returned list. Note that a completed task could
     * have terminated either normally or by throwing an exception. If you need to, you can check for exceptions by
     * calling {@link Future#get()}.
     *
     * @param tasks The collection of tasks.
     * @param <T>   The type of the tasks' results.
     * @return A list of Futures representing the tasks, in the same sequential order as in the input collection.
     * @throws InterruptedException if interrupted while waiting, in which case unfinished tasks are cancelled.
     */
    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return this.raw.invokeAll(tasks);
    }

    /**
     * Executes the given tasks, returning a list of Futures holding their status and results when all complete or the
     * timeout expires, whichever happens first.
     *
     * @param tasks   The collection of tasks.
     * @param timeout The maximum time to wait.
     * @param unit    The time unit of the timeout argument.
     * @param <T>     The type of the tasks' results.
     * @return A list of Futures representing the tasks, in the same sequential order as in the input collection.
     * @throws InterruptedException if interrupted while waiting, in which case unfinished tasks are cancelled.
     */
    @Override
    public <T> List<Future<T>> invokeAll(
            final Collection<? extends Callable<T>> tasks,
            final long timeout,
            final TimeUnit unit) throws InterruptedException {
        return this.raw.invokeAll(tasks, timeout, unit);
    }

    /**
     * Executes the given tasks, returning the result of one that has completed successfully (i.e., without throwing an
     * exception). Upon normal or exceptional completion of a task, all other uncompleted tasks are cancelled and this
     * method returns.
     *
     * @param tasks The collection of tasks.
     * @param <T>   The type of the tasks' results.
     * @return The result of one of the tasks.
     * @throws InterruptedException if interrupted while waiting.
     * @throws ExecutionException   if no task successfully completes.
     */
    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return this.raw.invokeAny(tasks);
    }

    /**
     * Executes the given tasks, returning the result of one that has completed successfully (i.e., without throwing an
     * exception) before the given timeout elapses.
     *
     * @param tasks   The collection of tasks.
     * @param timeout The maximum time to wait.
     * @param unit    The time unit of the timeout argument.
     * @param <T>     The type of the tasks' results.
     * @return The result of one of the tasks.
     * @throws InterruptedException if interrupted while waiting.
     * @throws ExecutionException   if no task successfully completes.
     * @throws TimeoutException     if the timeout elapses before any task successfully completes.
     */
    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return this.raw.invokeAny(tasks, timeout, unit);
    }

    /**
     * Returns the underlying {@link ExecutorService} instance that this object delegates to.
     *
     * @return The wrapped {@link ExecutorService}.
     */
    @Override
    public ExecutorService getRaw() {
        return this.raw;
    }

}
