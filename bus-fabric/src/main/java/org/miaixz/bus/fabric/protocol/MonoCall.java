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
package org.miaixz.bus.fabric.protocol;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Callback;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Shared single-result call template for protocol calls.
 *
 * @param <T> result type
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class MonoCall<T> implements Call<T> {

    /**
     * Call name.
     */
    private final String name;

    /**
     * Optional dispatcher used by enqueue().
     */
    private final Dispatcher dispatcher;

    /**
     * Lifecycle scope.
     */
    private final LifecycleScope scope;

    /**
     * Result future.
     */
    private final CompletableFuture<T> future;

    /**
     * Dispatch handle.
     */
    private final AtomicReference<DispatchHandle> handle;

    /**
     * Safe callback.
     */
    private final Callback<T> callback;

    /**
     * Creates a call template.
     *
     * @param name       call name
     * @param dispatcher dispatcher used by enqueue()
     * @param observer   event observer
     */
    protected MonoCall(final String name, final Dispatcher dispatcher, final EventObserver observer) {
        this(name, dispatcher, observer, null);
    }

    /**
     * Creates a call template.
     *
     * @param name       call name
     * @param dispatcher dispatcher used by enqueue()
     * @param observer   event observer
     * @param callback   callback
     */
    protected MonoCall(final String name, final Dispatcher dispatcher, final EventObserver observer,
            final Callback<? super T> callback) {
        this.name = Assert.notBlank(name, () -> new ValidateException("Call name must not be blank"));
        this.dispatcher = dispatcher;
        this.scope = LifecycleScope.call(this.name, observer);
        this.future = new CompletableFuture<>();
        this.handle = new AtomicReference<>();
        this.callback = safe(callback);
    }

    /**
     * Performs the protocol operation.
     *
     * @return result
     */
    protected abstract T perform();

    /**
     * Returns the dispatcher key for this call.
     *
     * @return dispatch key
     */
    protected abstract String dispatchKey();

    /**
     * Cancels running protocol resources.
     */
    protected void cancelRunning() {
        // Default call has no running protocol resource.
    }

    /**
     * Closes a value produced after cancellation.
     *
     * @param value produced value
     */
    protected void closeAfterCancelled(final T value) {
        if (value instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (final Exception ignored) {
                // Cancellation cleanup is best-effort.
            }
        }
    }

    /**
     * Returns the lifecycle cancellation scope.
     *
     * @return cancellation scope
     */
    protected final Cancellation cancellation() {
        return scope.cancellation();
    }

    /**
     * Returns the result future.
     *
     * @return result future
     */
    protected final CompletableFuture<T> future() {
        return future;
    }

    /**
     * Executes this call synchronously.
     *
     * @return result
     */
    @Override
    public T execute() {
        if (!scope.start()) {
            throw new StatefulException(name + " can only execute once");
        }
        try {
            scope.cancellation().throwIfCancelled();
            final T value = perform();
            if (scope.state() == Status.CANCELLED || future.isCancelled()) {
                closeAfterCancelled(value);
                throw new CancellationException(name + " was cancelled");
            }
            scope.complete();
            future.complete(value);
            callback.success(value);
            return value;
        } catch (final CancellationException e) {
            cancel();
            callback.failure(e);
            throw e;
        } catch (final RuntimeException e) {
            fail(e);
            throw e;
        }
    }

    /**
     * Enqueues this call to its configured dispatcher.
     *
     * @return this call
     */
    @Override
    public Call<T> enqueue() {
        return enqueue(Assert.notNull(dispatcher, () -> new ValidateException("Dispatcher must not be null")));
    }

    /**
     * Enqueues this call to a dispatcher.
     *
     * @param dispatcher dispatcher
     * @return this call
     */
    public Call<T> enqueue(final Dispatcher dispatcher) {
        final Dispatcher target = Assert
                .notNull(dispatcher, () -> new ValidateException("Dispatcher must not be null"));
        if (handle.get() != null) {
            return this;
        }
        if (scope.state() != Status.QUEUED) {
            throw new StatefulException(name + " cannot be enqueued from state " + scope.state());
        }
        final Activity activity = Activity.of(name, () -> {
            try {
                execute();
            } catch (final CancellationException e) {
                throw e;
            } catch (final RuntimeException e) {
                throw e;
            }
        }, scope.cancellation());
        final DispatchHandle enqueued = target.enqueue(dispatchKey(), activity);
        if (!handle.compareAndSet(null, enqueued)) {
            target.cancel(enqueued);
        }
        return this;
    }

    /**
     * Cancels this call.
     *
     * @return true when this invocation changed the call state
     */
    @Override
    public boolean cancel() {
        final boolean changed = scope.cancel(new CancellationException(name + " cancelled"));
        if (changed) {
            final DispatchHandle current = handle.get();
            if (current != null) {
                current.cancel();
            }
            future.cancel(false);
            cancelRunning();
        }
        return changed;
    }

    /**
     * Returns the lifecycle state.
     *
     * @return lifecycle state
     */
    @Override
    public Status state() {
        return scope.state();
    }

    /**
     * Returns whether this call is cancelled.
     *
     * @return true when cancelled
     */
    @Override
    public boolean cancelled() {
        final DispatchHandle current = handle.get();
        return Call.super.cancelled() || future.isCancelled() || current != null && current.cancelled();
    }

    /**
     * Returns whether this call has reached a terminal state.
     *
     * @return true when terminal
     */
    @Override
    public boolean done() {
        return Call.super.done() || future.isDone();
    }

    /**
     * Waits for this call to complete.
     *
     * @return result
     */
    @Override
    public T await() {
        startIfNeeded();
        return awaitFuture();
    }

    /**
     * Waits for this call to complete within a timeout.
     *
     * @param timeout timeout
     * @return result
     */
    @Override
    public T await(final Duration timeout) {
        validateTimeout(timeout);
        startIfNeeded();
        return awaitFuture(timeout);
    }

    /**
     * Starts this call when await is the first terminal operation.
     */
    private void startIfNeeded() {
        if (scope.state() != Status.QUEUED || handle.get() != null) {
            return;
        }
        if (dispatcher == null) {
            execute();
        } else {
            enqueue();
        }
    }

    /**
     * Fails this call.
     *
     * @param cause failure cause
     */
    private void fail(final RuntimeException cause) {
        if (scope.state() == Status.CANCELLED) {
            return;
        }
        scope.fail(cause);
        future.completeExceptionally(cause);
        callback.failure(cause);
    }

    /**
     * Waits for the future.
     *
     * @return result
     */
    private T awaitFuture() {
        try {
            return future.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for " + name, e);
        } catch (final ExecutionException e) {
            throw new InternalException(name + " failed", e.getCause());
        } catch (final CancellationException e) {
            throw new InternalException(name + " was cancelled", e);
        }
    }

    /**
     * Waits for the future within a timeout.
     *
     * @param timeout timeout
     * @return result
     */
    private T awaitFuture(final Duration timeout) {
        if (timeout.isZero()) {
            if (!future.isDone()) {
                cancel();
                throw new TimeoutException(name + " timed out");
            }
            return awaitFuture();
        }
        try {
            return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for " + name, e);
        } catch (final ExecutionException e) {
            throw new InternalException(name + " failed", e.getCause());
        } catch (final CancellationException e) {
            throw new InternalException(name + " was cancelled", e);
        } catch (final java.util.concurrent.TimeoutException e) {
            cancel();
            throw new TimeoutException(name + " timed out", e);
        } catch (final ArithmeticException e) {
            throw new ValidateException("Timeout is too large");
        }
    }

    /**
     * Validates timeout.
     *
     * @param timeout timeout
     */
    private static void validateTimeout(final Duration timeout) {
        final Duration checked = Assert
                .notNull(timeout, () -> new ValidateException("Timeout must be non-null and non-negative"));
        Assert.isFalse(checked.isNegative(), () -> new ValidateException("Timeout must be non-null and non-negative"));
    }

    /**
     * Wraps a callback with failure isolation.
     *
     * @param callback callback
     * @param <T>      value type
     * @return safe callback
     */
    private <T> Callback<T> safe(final Callback<? super T> callback) {
        return new SafeCallback<>(callback == null ? noopCallback() : callback);
    }

    /**
     * Returns a no-operation callback.
     *
     * @param <T> value type
     * @return no-operation callback
     */
    private static <T> Callback<T> noopCallback() {
        return (Callback<T>) NoopCallback.INSTANCE;
    }

    /**
     * No-operation callback.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private enum NoopCallback implements Callback<Object> {

        /**
         * Singleton no-operation callback.
         */
        INSTANCE;

        /**
         * Receives a successful value.
         *
         * @param value successful value
         */
        @Override
        public void success(final Object value) {
            // No-op callback intentionally ignores successful values.
        }

        /**
         * Receives a failure cause.
         *
         * @param cause failure cause
         */
        @Override
        public void failure(final Throwable cause) {
            // No-op callback intentionally ignores failures.
        }

    }

    /**
     * Safe callback wrapper.
     *
     * @param <T> value type
     * @author Kimi Liu
     * @since Java 21+
     */
    private final class SafeCallback<T> implements Callback<T> {

        /**
         * Delegate callback.
         */
        private final Callback<? super T> delegate;

        /**
         * Creates a safe callback.
         *
         * @param delegate delegate callback
         */
        private SafeCallback(final Callback<? super T> delegate) {
            this.delegate = Assert.notNull(delegate, () -> new ValidateException("Callback must not be null"));
        }

        /**
         * Receives a successful value.
         *
         * @param value successful value
         */
        @Override
        public void success(final T value) {
            try {
                delegate.success(value);
            } catch (final RuntimeException e) {
                scope.emit(ObservationMarker.LISTENER_FAILED, e);
            }
        }

        /**
         * Receives a failure cause.
         *
         * @param cause failure cause
         */
        @Override
        public void failure(final Throwable cause) {
            try {
                delegate.failure(cause);
            } catch (final RuntimeException e) {
                scope.emit(ObservationMarker.LISTENER_FAILED, e);
            }
        }

    }

}
