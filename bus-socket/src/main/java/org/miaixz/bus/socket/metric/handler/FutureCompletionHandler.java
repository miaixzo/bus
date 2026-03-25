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
package org.miaixz.bus.socket.metric.handler;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An adapter that bridges the callback-style {@link CompletionHandler} with the {@link Future} interface. This allows
 * asynchronous I/O operations that use a {@code CompletionHandler} to be treated as a {@code Future}, enabling blocking
 * calls via {@code get()} or polling for completion.
 *
 * @param <V> The result type of the {@code Future} and the {@code CompletionHandler}.
 * @param <A> The type of the attachment passed to the {@code CompletionHandler}.
 * @author Kimi Liu
 * @since Java 21+
 */
public final class FutureCompletionHandler<V, A> implements CompletionHandler<V, A>, Future<V> {

    private V result;
    private boolean done = false;
    private boolean cancel = false;
    private Throwable exception;

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This implementation sets the result and marks the future as complete, notifying any waiting threads.
     * </p>
     *
     * @param result     the result of the I/O operation
     * @param attachment the object attached to the I/O operation
     */
    @Override
    public void completed(V result, A attachment) {
        this.result = result;
        done = true;
        synchronized (this) {
            this.notifyAll();
        }
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This implementation stores the exception to be thrown by {@code Future.get()} and marks the future as complete,
     * notifying any waiting threads.
     * </p>
     *
     * @param exc        the exception thrown by the I/O operation
     * @param attachment the object attached to the I/O operation
     */
    @Override
    public void failed(Throwable exc, A attachment) {
        exception = exc;
        done = true;
        synchronized (this) {
            this.notifyAll();
        }
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * If the task has already completed or been cancelled, this method returns {@code false}. Otherwise, it marks the
     * task as cancelled and complete, notifying any waiting threads.
     * </p>
     *
     * @param mayInterruptIfRunning {@code true} if the thread executing this task should be interrupted; otherwise,
     *                              in-progress tasks are allowed to complete (this parameter is ignored in this
     *                              implementation)
     * @return {@code false} if the task could not be cancelled, {@code true} otherwise
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (done || cancel) {
            return false;
        }
        cancel = true;
        done = true;
        synchronized (this) {
            notifyAll();
        }
        return true;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return {@code true} if this task was cancelled before it completed normally
     */
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return {@code true} if this task completed
     */
    @Override
    public boolean isDone() {
        return done;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This implementation blocks until the task completes.
     * </p>
     *
     * @return the computed result
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException   if the computation threw an exception
     */
    @Override
    public synchronized V get() throws InterruptedException, ExecutionException {
        if (!done) {
            wait();
        }
        if (exception != null) {
            throw new ExecutionException(exception);
        }
        return result;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This implementation blocks for at most the specified time.
     * </p>
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the computed result
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException   if the computation threw an exception
     * @throws TimeoutException     if the wait timed out
     */
    @Override
    public synchronized V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (!done) {
            wait(unit.toMillis(timeout));
        }
        if (done) {
            return get();
        }
        throw new TimeoutException();
    }

}
