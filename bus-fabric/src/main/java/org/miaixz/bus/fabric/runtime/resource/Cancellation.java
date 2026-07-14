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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

    /**
     * Registered cleanup callbacks.
     */
    private final ConcurrentLinkedDeque<Runnable> callbacks;

    /**
     * Cancelled flag.
     */
    private final AtomicBoolean cancelled;

    /**
     * First cancellation cause.
     */
    private final AtomicReference<Throwable> cause;

    /**
     * Creates a cancellation scope.
     */
    private Cancellation() {
        this.callbacks = new ConcurrentLinkedDeque<>();
        this.cancelled = new AtomicBoolean();
        this.cause = new AtomicReference<>();
    }

    /**
     * Creates a cancellation scope.
     *
     * @return cancellation scope
     */
    public static Cancellation create() {
        return new Cancellation();
    }

    /**
     * Cancels this scope.
     *
     * @return true when this invocation changed the state
     */
    public boolean cancel() {
        return cancel(new CancellationException("Operation cancelled"));
    }

    /**
     * Cancels this scope with a cause.
     *
     * @param reason cancellation cause
     * @return true when this invocation changed the state
     */
    public boolean cancel(final Throwable reason) {
        final Throwable current = require(reason, "Cancellation cause");
        if (!cancelled.compareAndSet(false, true)) {
            return false;
        }
        cause.set(current);
        drain();
        return true;
    }

    /**
     * Returns whether this scope is cancelled.
     *
     * @return true when cancelled
     */
    public boolean cancelled() {
        return cancelled.get();
    }

    /**
     * Returns the first cancellation cause.
     *
     * @return cause or null
     */
    public Throwable cause() {
        return cause.get();
    }

    /**
     * Registers cleanup to run when this scope is cancelled.
     *
     * @param callback cleanup callback
     * @return unregister callback
     */
    public Runnable onCancel(final Runnable callback) {
        final Runnable current = require(callback, "Cancellation callback");
        if (cancelled.get()) {
            run(current);
            return Cancellation::noop;
        }
        callbacks.addLast(current);
        if (cancelled.get() && callbacks.remove(current)) {
            run(current);
            return Cancellation::noop;
        }
        return () -> callbacks.remove(current);
    }

    /**
     * Closes a resource when this scope is cancelled.
     *
     * @param resource closeable resource
     * @param <T>      resource type
     * @return original resource
     */
    public <T extends AutoCloseable> T closeOnCancel(final T resource) {
        final T current = require(resource, "Cancellation resource");
        onCancel(() -> close(current));
        return current;
    }

    /**
     * Throws when this scope has already been cancelled.
     */
    public void throwIfCancelled() {
        if (!cancelled.get()) {
            return;
        }
        final CancellationException exception = new CancellationException("Operation cancelled");
        final Throwable current = cause.get();
        if (current != null && current != exception) {
            exception.initCause(current);
        }
        throw exception;
    }

    /**
     * Runs all registered callbacks in reverse registration order.
     */
    private void drain() {
        Runnable callback;
        while ((callback = callbacks.pollLast()) != null) {
            run(callback);
        }
    }

    /**
     * Runs a callback without allowing cleanup failures to block cancellation.
     *
     * @param callback callback
     */
    private static void run(final Runnable callback) {
        try {
            callback.run();
        } catch (final RuntimeException e) {
            Logger.warn(false, LOG_TAG, e, "Cancellation cleanup failed: exception={}", e.getClass().getSimpleName());
        }
    }

    /**
     * Closes a resource without allowing cleanup failures to block cancellation.
     *
     * @param resource resource
     */
    private static void close(final AutoCloseable resource) {
        try {
            resource.close();
        } catch (final Exception e) {
            Logger.warn(
                    false,
                    LOG_TAG,
                    e,
                    "Cancellation resource cleanup failed: resource={}, exception={}",
                    resource.getClass().getName(),
                    e.getClass().getSimpleName());
        }
    }

    /**
     * No-op unregister callback.
     */
    private static void noop() {
        // no-op
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
