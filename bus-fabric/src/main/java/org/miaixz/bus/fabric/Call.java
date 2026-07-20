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
package org.miaixz.bus.fabric;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Single executable fabric call.
 * <p>
 * A call represents one concrete request, connection open, or protocol write. Implementations may run immediately,
 * dispatch work in the background, or wrap an already-started transport operation, but the public contract stays
 * focused on execution and lifecycle state rather than exposing internal futures.
 *
 * @param <T> result type
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Call<T> extends Lifecycle {

    /**
     * Creates a call that represents an unsupported operation.
     *
     * @param operation operation description
     * @param <T>       result type
     * @return unsupported call
     */
    static <T> Call<T> unsupported(final String operation) {
        final String message = operation == null || operation.isBlank() ? "Unsupported fabric call" : operation;
        return new Call<>() {

            /**
             * Cancellation flag.
             */
            private final AtomicBoolean cancelled = new AtomicBoolean();

            /**
             * Stable unsupported-operation failure shared by every start path.
             */
            private final StatefulException failure = new StatefulException(message);

            /**
             * Executes this unsupported call.
             *
             * @return never returns
             */
            @Override
            public T execute() {
                throw failure;
            }

            /**
             * Rejects asynchronous execution of this unsupported call.
             *
             * @return never returns
             */
            @Override
            public Call<T> enqueue() {
                throw failure;
            }

            /**
             * Rejects waiting for this unsupported call.
             *
             * @return never returns
             */
            @Override
            public T await() {
                throw failure;
            }

            /**
             * Rejects timed waiting for this unsupported call.
             *
             * @param timeout ignored timeout
             * @return never returns
             */
            @Override
            public T await(final Duration timeout) {
                throw failure;
            }

            /**
             * Cancels this unsupported call.
             *
             * @return true when this invocation changed the call to cancelled
             */
            @Override
            public boolean cancel() {
                return cancelled.compareAndSet(false, true);
            }

            /**
             * Returns the lifecycle state.
             *
             * @return failed or cancelled state
             */
            @Override
            public Status state() {
                return cancelled.get() ? Status.CANCELLED : Status.FAILED;
            }

        };
    }

    /**
     * Starts and executes this call synchronously. A call may be started only once.
     *
     * @return completed result
     * @throws StatefulException when the call has already been started
     */
    T execute();

    /**
     * Starts and enqueues this call for asynchronous execution. A call may be started only once.
     *
     * @return this call
     * @throws StatefulException when the call has already been started
     */
    Call<T> enqueue();

    /**
     * Cancels this call and propagates cancellation to owned runtime resources.
     *
     * @return true when this invocation changed the call to cancelled
     */
    boolean cancel();

    /**
     * Returns whether this call is cancelled.
     *
     * @return true when cancelled
     */
    @Override
    default boolean cancelled() {
        return Lifecycle.super.cancelled();
    }

    /**
     * Returns whether this call is complete.
     *
     * @return true when complete
     */
    default boolean done() {
        return terminal();
    }

    /**
     * Starts a queued call when necessary and waits for its completion.
     *
     * @return completed value
     */
    default T await() {
        return execute();
    }

    /**
     * Starts a queued call when necessary and waits for its completion within a timeout.
     *
     * @param timeout non-null, non-negative timeout; zero performs an immediate completion check
     * @return completed value
     * @throws ValidateException when timeout is null or negative
     */
    T await(Duration timeout);

}
