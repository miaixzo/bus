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
package org.miaixz.bus.fabric.runtime.resource;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.logger.Logger;

/**
 * Shared cancellation scope for one logical operation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Cancellation {

    /**
     * VarHandle used for the single successful active-to-cancelled transition.
     */
    private static final VarHandle STATE;

    /**
     * VarHandle used to install and detach the lazily allocated callback deque.
     */
    private static final VarHandle CALLBACKS;

    /**
     * Shared no-op unregister action.
     */
    private static final Runnable NOOP = Cancellation::noop;

    /** Shared scope for synchronous operations that expose no cancellation handle. */
    private static final Cancellation NONE = new Cancellation(false);

    static {
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            STATE = lookup.findVarHandle(Cancellation.class, "state", int.class);
            CALLBACKS = lookup.findVarHandle(Cancellation.class, "callbacks", ConcurrentLinkedDeque.class);
        } catch (final ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Lazily allocated concurrent deque of registered cancellation callbacks.
     */
    private volatile ConcurrentLinkedDeque<Runnable> callbacks;

    /**
     * Integer cancellation state: zero while active and one after the winning cancellation transition.
     */
    private volatile int state;

    /**
     * Cause supplied by the invocation that won cancellation, published after the state transition.
     */
    private volatile Throwable cause;

    /** Whether callers can transition this scope to cancelled. */
    private final boolean cancellable;

    /**
     * Creates an active cancellation scope without allocating callback storage.
     */
    private Cancellation() {
        this(true);
    }

    /**
     * Creates a scope with the requested cancellation capability.
     *
     * @param cancellable true for caller-owned scopes
     */
    private Cancellation(final boolean cancellable) {
        this.cancellable = cancellable;
        // Callback storage is allocated only when cancellation cleanup is registered.
    }

    /**
     * Creates a cancellation scope.
     *
     * @return new active cancellation scope
     */
    public static Cancellation create() {
        return new Cancellation();
    }

    /**
     * Returns a shared scope for synchronous operations that cannot be cancelled externally.
     *
     * @return immutable active no-cancellation scope
     */
    public static Cancellation none() {
        return NONE;
    }

    /**
     * Returns whether this scope accepts cancellation and cleanup registrations.
     *
     * @return true for caller-owned cancellation scopes
     */
    public boolean cancellable() {
        return cancellable;
    }

    /**
     * Cancels this scope with a new {@link CancellationException}.
     *
     * @return {@code true} if this invocation won cancellation and drained registered callbacks
     */
    public boolean cancel() {
        return cancel(new CancellationException("Operation cancelled"));
    }

    /**
     * Cancels this scope with a cause.
     *
     * @param reason non-null cause retained as the winning cancellation reason
     * @return {@code true} if this invocation changed the state and drained registered callbacks
     * @throws ValidateException if {@code reason} is {@code null}
     */
    public boolean cancel(final Throwable reason) {
        final Throwable current = require(reason, "Cancellation cause");
        if (!cancellable) {
            return false;
        }
        if (!(boolean) STATE.compareAndSet(this, 0, 1)) {
            return false;
        }
        cause = current;
        drain();
        return true;
    }

    /**
     * Returns whether this scope is cancelled.
     *
     * @return {@code true} after any invocation wins the cancellation transition
     */
    public boolean cancelled() {
        return state != 0;
    }

    /**
     * Returns the first cancellation cause.
     *
     * @return winning cancellation cause, or {@code null} before cancellation or during the brief publication window
     */
    public Throwable cause() {
        return cause;
    }

    /**
     * Registers cleanup for reverse-order cancellation execution, or runs it immediately when already cancelled.
     *
     * @param callback non-null cleanup action
     * @return action that removes this registration when still queued, or a shared no-op action when cleanup already
     *         ran
     * @throws ValidateException if {@code callback} is {@code null}
     */
    public Runnable onCancel(final Runnable callback) {
        final Runnable current = require(callback, "Cancellation callback");
        if (!cancellable) {
            return NOOP;
        }
        if (state != 0) {
            run(current);
            return NOOP;
        }
        ConcurrentLinkedDeque<Runnable> queue = callbacks;
        if (queue == null) {
            final ConcurrentLinkedDeque<Runnable> created = new ConcurrentLinkedDeque<>();
            if ((boolean) CALLBACKS.compareAndSet(this, null, created)) {
                queue = created;
            } else {
                queue = callbacks;
            }
        }
        queue.addLast(current);
        if (state != 0 && queue.remove(current)) {
            run(current);
            return NOOP;
        }
        final ConcurrentLinkedDeque<Runnable> registered = queue;
        return () -> registered.remove(current);
    }

    /**
     * Registers a resource to be closed when cancellation occurs, closing it immediately if already cancelled.
     *
     * @param resource non-null closeable resource
     * @param <T>      closeable resource type
     * @return the same resource reference
     * @throws ValidateException if {@code resource} is {@code null}
     */
    public <T extends AutoCloseable> T closeOnCancel(final T resource) {
        final T current = require(resource, "Cancellation resource");
        onCancel(() -> close(current));
        return current;
    }

    /**
     * Throws a new cancellation exception when this scope is cancelled, chaining the recorded reason when available.
     *
     * @throws CancellationException if this scope is cancelled
     */
    public void throwIfCancelled() {
        if (state == 0) {
            return;
        }
        final CancellationException exception = new CancellationException("Operation cancelled");
        final Throwable current = cause;
        if (current != null && current != exception) {
            exception.initCause(current);
        }
        throw exception;
    }

    /**
     * Removes and runs all currently registered callbacks in reverse registration order, then detaches the deque.
     */
    private void drain() {
        final ConcurrentLinkedDeque<Runnable> queue = callbacks;
        if (queue == null) {
            return;
        }
        Runnable callback;
        while ((callback = queue.pollLast()) != null) {
            run(callback);
        }
        CALLBACKS.compareAndSet(this, queue, null);
    }

    /**
     * Runs a callback without allowing cleanup failures to block cancellation.
     *
     * @param callback cancellation cleanup action to invoke
     */
    private static void run(final Runnable callback) {
        try {
            callback.run();
        } catch (final RuntimeException e) {
            Logger.warn(false, "Fabric", e, "Cancellation cleanup failed: exception={}", e.getClass().getSimpleName());
        }
    }

    /**
     * Closes a resource without allowing cleanup failures to block cancellation.
     *
     * @param resource cancellation-owned resource to close
     */
    private static void close(final AutoCloseable resource) {
        try {
            resource.close();
        } catch (final Exception e) {
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "Cancellation resource cleanup failed: resource={}, exception={}",
                    resource.getClass().getName(),
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Performs no work for registrations whose callback has already run.
     */
    private static void noop() {
        // no-op
    }

    /**
     * Validates and returns a required reference.
     *
     * @param value reference to validate
     * @param name  logical reference name used in the validation message
     * @param <T>   reference type
     * @return the validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
