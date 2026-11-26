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
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Single executable fabric call.
 * <p>
 * A call represents one concrete request, connection open, or protocol write. Implementations may run immediately,
 * dispatch work in the background, or wrap an already-started transport operation, but the public contract stays
 * focused on execution and lifecycle state rather than exposing internal futures.
 *
 * @param <T> result type
 * @author Kimi Liu
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
            public State state() {
                return cancelled.get() ? State.CANCELLED : State.FAILED;
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

    /**
     * Starts this single-use call on an executor and returns an independently owned future.
     * <p>
     * Cancelling the future cancels this call. A successful value that cannot be delivered is closed when it implements
     * {@link AutoCloseable}; the call's internal completion future is never exposed.
     * </p>
     *
     * @param executor executor that must accept the task before execution can start
     * @return future owning delivery of this call's result
     */
    default CompletableFuture<T> toFuture(final Executor executor) {
        return toFuture(executor, value -> {
            if (value instanceof AutoCloseable closeable) {
                IoKit.close(closeable);
            }
        });
    }

    /**
     * Starts this call with an explicit strategy for disposing of an undeliverable result.
     *
     * @param executor  executor that must accept the task before execution can start
     * @param discarded cleanup invoked when execution succeeds but result delivery loses
     * @return future owning delivery of this call's result
     */
    default CompletableFuture<T> toFuture(final Executor executor, final ConsumerX<? super T> discarded) {
        return toFuture(executor, discarded, FunctionX.identity());
    }

    /**
     * Starts this call with explicit result cleanup and failure adaptation.
     * <p>
     * The failure mapper is opt-in; other overloads preserve failures unchanged. The returned future coordinates
     * cancellation with result publication so cancelling a completed result cannot invalidate caller-owned resources.
     * Rejected submission cancels the unstarted call and completes the returned future exceptionally.
     * </p>
     *
     * @param executor      executor that must accept the task before execution can start
     * @param discarded     cleanup invoked when execution succeeds but result delivery loses
     * @param failureMapper mapping applied to execution and submission failures
     * @return future owning delivery of this call's result
     */
    default CompletableFuture<T> toFuture(
            final Executor executor,
            final ConsumerX<? super T> discarded,
            final FunctionX<? super Throwable, ? extends Throwable> failureMapper) {
        Objects.requireNonNull(executor, "Executor must not be null");
        Objects.requireNonNull(discarded, "Discard strategy must not be null");
        Objects.requireNonNull(failureMapper, "Failure mapper must not be null");
        final State current = state();
        if (current != State.NEW && current != State.QUEUED) {
            return CompletableFuture.failedFuture(
                    current == State.CANCELLED ? new CancellationException("Call was cancelled")
                            : new StatefulException("Call was already submitted; cannot bridge to a future"));
        }
        final Call<T> call = this;
        final AtomicBoolean published = new AtomicBoolean();
        final CompletableFuture<T> result = new CompletableFuture<>() {

            /**
             * Completes the future when the supplied value wins publication.
             *
             * @param value result to publish
             * @return {@code true} if the value completed the future
             */
            @Override
            public boolean complete(final T value) {
                return published.compareAndSet(false, true) && super.complete(value);
            }

            /**
             * Completes the future exceptionally when the supplied failure wins publication.
             *
             * @param failure failure to publish
             * @return {@code true} if the failure completed the future
             * @throws NullPointerException if {@code failure} is {@code null}
             */
            @Override
            public boolean completeExceptionally(final Throwable failure) {
                Objects.requireNonNull(failure, "Failure must not be null");
                return published.compareAndSet(false, true) && super.completeExceptionally(failure);
            }

            /**
             * Cancels the underlying call when cancellation wins publication.
             *
             * @param mayInterruptIfRunning interruption hint ignored by this implementation
             * @return {@code true} if the future is cancelled
             */
            @Override
            public boolean cancel(final boolean mayInterruptIfRunning) {
                if (isDone() || !published.compareAndSet(false, true)) {
                    return isCancelled();
                }
                final CancellationException failure = new CancellationException("Call future cancelled");
                try {
                    call.cancel();
                } catch (final Throwable cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
                return super.completeExceptionally(failure);
            }

        };
        final ConsumerX<Throwable> failed = failure -> {
            Throwable mapped;
            try {
                mapped = Objects.requireNonNull(failureMapper.apply(failure), "Mapped failure must not be null");
            } catch (final Throwable mappingFailure) {
                if (mappingFailure != failure) {
                    mappingFailure.addSuppressed(failure);
                }
                mapped = mappingFailure;
            }
            result.completeExceptionally(mapped);
        };
        try {
            executor.execute(() -> {
                try {
                    if (result.isDone()) {
                        call.cancel();
                        return;
                    }
                    final T value = call.execute();
                    if (!result.complete(value)) {
                        result.whenComplete((owned, failure) -> {
                            if (failure != null || owned != value) {
                                discarded.accept(value);
                            }
                        });
                    }
                } catch (final Throwable failure) {
                    failed.accept(failure);
                }
            });
        } catch (final Throwable failure) {
            try {
                call.cancel();
            } catch (final Throwable cleanupFailure) {
                if (cleanupFailure != failure) {
                    failure.addSuppressed(cleanupFailure);
                }
            }
            failed.accept(failure);
        }
        return result;
    }

}
