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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.center.function.SupplierX;
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
     * Submission CAS without allocating a per-call atomic wrapper.
     */
    private static final VarHandle SUBMITTED;

    /**
     * Callback atomic exchange without entering the call monitor.
     */
    private static final VarHandle CALLBACK;

    /**
     * Pending terminal outcome marker.
     */
    private static final Object PENDING = new Object();

    /**
     * Encoded successful null result.
     */
    private static final Object NULL_RESULT = new Object();

    static {
        try {
            SUBMITTED = MethodHandles.lookup().findVarHandle(MonoCall.class, "submitted", int.class);
            CALLBACK = MethodHandles.lookup().findVarHandle(MonoCall.class, "callback", Callback.class);
        } catch (final ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

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
     * Atomic guard allowing exactly one submission path.
     */
    private volatile int submitted;

    /**
     * Result future.
     */
    private volatile CompletableFuture<T> future;

    /**
     * Dispatch handle.
     */
    private volatile DispatchHandle handle;

    /**
     * Safe callback.
     */
    private volatile Callback<? super T> callback;

    /**
     * Result or failure retained only until a lazy future observes the terminal state.
     */
    private volatile Object outcome;

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
     * @param callback   optional terminal callback invoked at most once
     */
    protected MonoCall(final String name, final Dispatcher dispatcher, final EventObserver observer,
            final Callback<? super T> callback) {
        this.name = Assert.notBlank(name, () -> new ValidateException("Call name must not be blank"));
        this.dispatcher = dispatcher;
        this.scope = LifecycleScope.call(this.name, observer);
        this.callback = callback;
        this.outcome = PENDING;
    }

    /**
     * Creates a simple single-result call backed by an operation and cancellation action.
     *
     * @param name         call name
     * @param dispatchKey  asynchronous dispatch key
     * @param dispatcher   optional dispatcher used by enqueue()
     * @param observer     event observer
     * @param callback     optional callback
     * @param operation    call operation
     * @param cancelAction optional running-operation cancellation action
     * @param <T>          result type
     * @return simple call
     */
    public static <T> MonoCall<T> create(
            final String name,
            final String dispatchKey,
            final Dispatcher dispatcher,
            final EventObserver observer,
            final Callback<? super T> callback,
            final SupplierX<T> operation,
            final Runnable cancelAction) {
        final String key = Assert
                .notBlank(dispatchKey, () -> new ValidateException("Call dispatch key must not be blank"));
        final SupplierX<T> currentOperation = Assert
                .notNull(operation, () -> new ValidateException("Call operation must not be null"));
        final Runnable currentCancelAction = cancelAction == null ? () -> {
        } : cancelAction;
        return new MonoCall<>(name, dispatcher, observer, callback) {

            /**
             * Runs the supplied operation while preserving runtime failures and errors.
             *
             * @return operation result
             */
            @Override
            protected T perform() {
                try {
                    return currentOperation.getting();
                } catch (final RuntimeException | Error e) {
                    throw e;
                } catch (final Throwable e) {
                    throw new InternalException("Call operation failed", e);
                }
            }

            /**
             * Returns the validated dispatch key.
             *
             * @return dispatch key
             */
            @Override
            protected String dispatchKey() {
                return key;
            }

            /**
             * Runs the supplied cancellation action.
             */
            @Override
            protected void cancelRunning() {
                currentCancelAction.run();
            }

        };
    }

    /**
     * Performs the protocol operation.
     *
     * @return protocol-specific result produced by the operation
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
        return completionFuture();
    }

    /**
     * Executes this call synchronously.
     *
     * @return synchronously produced protocol result
     */
    @Override
    public T execute() {
        claimSubmission("execute");
        return runOnce();
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
     * @param dispatcher target dispatcher used for this asynchronous submission
     * @return this call
     */
    public Call<T> enqueue(final Dispatcher dispatcher) {
        final Dispatcher target = Assert
                .notNull(dispatcher, () -> new ValidateException("Dispatcher must not be null"));
        claimSubmission("enqueue");
        return enqueueClaimed(target);
    }

    /**
     * Cancels this call.
     *
     * @return true when this invocation changed the call state
     */
    @Override
    public boolean cancel() {
        if (scope.state().terminal()) {
            return false;
        }
        return finishCancellation(new CancellationException(name + " cancelled"));
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
        final DispatchHandle current = handle;
        final CompletableFuture<T> completion = future;
        return Call.super.cancelled() || completion != null && completion.isCancelled()
                || current != null && current.cancelled();
    }

    /**
     * Returns whether this call has reached a terminal state.
     *
     * @return true when terminal
     */
    @Override
    public boolean done() {
        final CompletableFuture<T> completion = future;
        return Call.super.done() || completion != null && completion.isDone();
    }

    /**
     * Waits for this call to complete.
     *
     * @return completed result after starting the call when necessary
     */
    @Override
    public T await() {
        startForAwait();
        return awaitFuture();
    }

    /**
     * Waits for this call to complete within a timeout.
     *
     * @param timeout non-negative maximum wait duration
     * @return completed result obtained within the timeout
     */
    @Override
    public T await(final Duration timeout) {
        final Duration checkedTimeout = validateTimeout(timeout);
        final long timeoutNanos = timeoutNanos(checkedTimeout);
        startForAwait();
        return awaitFuture(checkedTimeout, timeoutNanos);
    }

    /**
     * Claims the single submission slot for an explicit start entry.
     *
     * @param entry start entry name
     */
    private void claimSubmission(final String entry) {
        if (!SUBMITTED.compareAndSet(this, 0, 1)) {
            throw new StatefulException(name + " was already submitted; cannot " + entry);
        }
    }

    /**
     * Starts this call once when await is the first entry.
     */
    private void startForAwait() {
        if (!SUBMITTED.compareAndSet(this, 0, 1)) {
            return;
        }
        if (scope.state().terminal()) {
            return;
        }
        if (dispatcher == null) {
            runOnce();
        } else {
            enqueueClaimed(dispatcher);
        }
    }

    /**
     * Enqueues an already claimed call exactly once.
     *
     * @param target target dispatcher
     * @return this call
     */
    private Call<T> enqueueClaimed(final Dispatcher target) {
        final Activity activity = Activity.of(name, this::runDispatched, scope.cancellation());
        try {
            final DispatchHandle enqueued = Assert.notNull(
                    target.enqueue(dispatchKey(), activity),
                    () -> new ValidateException("Dispatcher returned a null handle"));
            handle = enqueued;
            final CompletableFuture<T> completion = future;
            if (scope.state() == Status.CANCELLED || completion != null && completion.isCancelled()) {
                enqueued.cancel();
            }
            return this;
        } catch (final RuntimeException | Error e) {
            finishFailure(e);
            throw e;
        }
    }

    /**
     * Runs a dispatched operation without re-entering a public start method.
     */
    private void runDispatched() {
        runOnce();
    }

    /**
     * Runs the claimed operation and completes its lifecycle exactly once.
     *
     * @return protocol result after the successful terminal transition
     */
    private T runOnce() {
        try {
            if (!scope.start()) {
                scope.cancellation().throwIfCancelled();
                throw new StatefulException(name + " cannot run from state " + scope.state());
            }
            scope.cancellation().throwIfCancelled();
            final T value = perform();
            final CompletableFuture<T> completion = future;
            if (scope.state() == Status.CANCELLED || scope.cancellation().cancelled()
                    || completion != null && completion.isCancelled()) {
                throw closeAfterCancellation(value, cancellationFailure());
            }
            if (!finishSuccess(value)) {
                throw closeAfterCancellation(value, cancellationFailure());
            }
            notifySuccess(value);
            return value;
        } catch (final CancellationException e) {
            try {
                finishCancellation(e);
            } catch (final RuntimeException cleanupFailure) {
                if (cleanupFailure != e) {
                    e.addSuppressed(cleanupFailure);
                }
            }
            throw e;
        } catch (final RuntimeException | Error e) {
            finishFailure(e);
            throw e;
        }
    }

    /**
     * Closes a value produced after cancellation while retaining the cancellation failure.
     *
     * @param value produced value
     * @param cause cancellation failure
     * @return cancellation failure
     */
    private CancellationException closeAfterCancellation(final T value, final CancellationException cause) {
        try {
            closeAfterCancelled(value);
        } catch (final Throwable cleanupFailure) {
            if (cleanupFailure != cause) {
                cause.addSuppressed(cleanupFailure);
            }
        }
        return cause;
    }

    /**
     * Creates a cancellation failure linked to the lifecycle cause.
     *
     * @return cancellation failure
     */
    private CancellationException cancellationFailure() {
        final CancellationException failure = new CancellationException(name + " was cancelled");
        final Throwable cause = scope.cancellation().cause();
        if (cause != null && cause != failure) {
            failure.initCause(cause);
        }
        return failure;
    }

    /**
     * Publishes a successful terminal outcome and completes a previously requested future.
     *
     * @param value successful result, including {@code null}
     * @return {@code true} when this call won the terminal transition
     */
    private boolean finishSuccess(final T value) {
        synchronized (this) {
            outcome = value == null ? NULL_RESULT : value;
            if (!scope.complete()) {
                outcome = PENDING;
                return false;
            }
            final CompletableFuture<T> completion = future;
            if (completion != null) {
                completion.complete(value);
                outcome = PENDING;
            }
            handle = null;
            return true;
        }
    }

    /**
     * Creates the completion holder only when an asynchronous observer actually needs it.
     *
     * @return shared completion future
     */
    private synchronized CompletableFuture<T> completionFuture() {
        CompletableFuture<T> completion = future;
        if (completion != null) {
            return completion;
        }
        completion = new CompletableFuture<>();
        future = completion;
        final Status state = scope.state();
        if (state == Status.DONE) {
            completion.complete(outcome == NULL_RESULT ? null : (T) outcome);
            outcome = PENDING;
        } else if (state == Status.CANCELLED) {
            completion.cancel(false);
        } else if (state == Status.FAILED) {
            completion.completeExceptionally((Throwable) outcome);
            outcome = PENDING;
        }
        return completion;
    }

    /**
     * Completes cancellation, future cancellation, owned work cleanup, and callback once.
     *
     * @param cause cancellation cause
     * @return true when cancellation changed the lifecycle
     */
    private boolean finishCancellation(final CancellationException cause) {
        final DispatchHandle current;
        final CompletableFuture<T> completion;
        synchronized (this) {
            if (!scope.cancel(cause)) {
                return false;
            }
            outcome = PENDING;
            current = handle;
            handle = null;
            completion = future;
            if (completion != null) {
                completion.cancel(false);
            }
        }
        RuntimeException cleanupFailure = null;
        if (current != null) {
            try {
                current.cancel();
            } catch (final RuntimeException e) {
                cleanupFailure = e;
            }
        }
        try {
            cancelRunning();
        } catch (final RuntimeException e) {
            if (cleanupFailure == null) {
                cleanupFailure = e;
            } else if (cleanupFailure != e) {
                cleanupFailure.addSuppressed(e);
            }
        }
        notifyFailure(cause);
        if (cleanupFailure != null) {
            throw cleanupFailure;
        }
        return true;
    }

    /**
     * Completes failure state, future, and callback once.
     *
     * @param cause failure cause
     */
    private void finishFailure(final Throwable cause) {
        final boolean changed;
        synchronized (this) {
            outcome = cause;
            changed = scope.fail(cause);
            if (changed) {
                final CompletableFuture<T> completion = future;
                if (completion != null) {
                    completion.completeExceptionally(cause);
                    outcome = PENDING;
                }
                handle = null;
            } else {
                outcome = PENDING;
            }
        }
        if (changed) {
            notifyFailure(cause);
        }
    }

    /**
     * Waits for the future.
     *
     * @return completed future result
     */
    private T awaitFuture() {
        final CompletableFuture<T> completion = completionFuture();
        try {
            return completion.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for " + name, e);
        } catch (final ExecutionException e) {
            return throwExecutionFailure(e);
        }
    }

    /**
     * Waits for the future within a timeout.
     *
     * @param timeout validated maximum wait duration
     * @param nanos   timeout nanoseconds
     * @return completed future result obtained within the timeout
     */
    private T awaitFuture(final Duration timeout, final long nanos) {
        final CompletableFuture<T> completion = completionFuture();
        if (timeout.isZero()) {
            if (!completion.isDone()) {
                throw cancelForTimeout(new TimeoutException(name + " timed out"));
            }
            return awaitFuture();
        }
        try {
            return completion.get(nanos, TimeUnit.NANOSECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for " + name, e);
        } catch (final ExecutionException e) {
            return throwExecutionFailure(e);
        } catch (final java.util.concurrent.TimeoutException e) {
            throw cancelForTimeout(new TimeoutException(name + " timed out", e));
        }
    }

    /**
     * Cancels this call and retains cleanup failures on the timeout.
     *
     * @param failure timeout failure
     * @return timeout failure
     */
    private TimeoutException cancelForTimeout(final TimeoutException failure) {
        try {
            cancel();
        } catch (final RuntimeException cleanupFailure) {
            if (cleanupFailure != failure) {
                failure.addSuppressed(cleanupFailure);
            }
        }
        return failure;
    }

    /**
     * Rethrows an asynchronous failure without wrapping runtime failures or errors.
     *
     * @param failure execution failure
     * @return never returns
     */
    private T throwExecutionFailure(final ExecutionException failure) {
        final Throwable cause = failure.getCause();
        if (cause instanceof RuntimeException runtime) {
            throw runtime;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        throw new InternalException(name + " failed", cause == null ? failure : cause);
    }

    /**
     * Validates timeout.
     *
     * @param timeout candidate maximum wait duration
     * @return validated timeout
     */
    private static Duration validateTimeout(final Duration timeout) {
        final Duration checked = Assert
                .notNull(timeout, () -> new ValidateException("Timeout must be non-null and non-negative"));
        Assert.isFalse(checked.isNegative(), () -> new ValidateException("Timeout must be non-null and non-negative"));
        return checked;
    }

    /**
     * Converts a timeout to nanoseconds with a stable validation failure.
     *
     * @param timeout validated timeout
     * @return timeout nanoseconds
     */
    private static long timeoutNanos(final Duration timeout) {
        try {
            return timeout.toNanos();
        } catch (final ArithmeticException e) {
            throw new ValidateException("Timeout is too large");
        }
    }

    /**
     * Invokes and clears the optional callback after a successful terminal transition.
     *
     * @param value successful result delivered to the callback
     */
    private void notifySuccess(final T value) {
        final Callback<? super T> current = takeCallback();
        if (current == null) {
            return;
        }
        try {
            current.success(value);
        } catch (final RuntimeException | Error e) {
            scope.emit(ObservationMarker.LISTENER_FAILED, e);
        }
    }

    /**
     * Invokes and clears the optional callback after a failed or cancelled terminal transition.
     *
     * @param cause terminal failure delivered to the callback
     */
    private void notifyFailure(final Throwable cause) {
        final Callback<? super T> current = takeCallback();
        if (current == null) {
            return;
        }
        try {
            current.failure(cause);
        } catch (final RuntimeException | Error e) {
            scope.emit(ObservationMarker.LISTENER_FAILED, e);
        }
    }

    /**
     * Removes the callback strong reference exactly once.
     *
     * @return detached callback, or {@code null} when absent
     */
    private Callback<? super T> takeCallback() {
        return (Callback<? super T>) CALLBACK.getAndSet(this, null);
    }

}
