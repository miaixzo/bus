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
package org.miaixz.bus.fabric.runtime.dispatch;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Lifecycle;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.runtime.Activity;

/**
 * Cancellable handle for a queued or running activity.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DispatchHandle implements Lifecycle {

    /**
     * Handle is queued and may still be cancelled before execution.
     */
    private static final int QUEUED = 0;

    /**
     * Handle activity is currently running.
     */
    private static final int RUNNING = 1;

    /**
     * Handle activity completed successfully.
     */
    private static final int DONE = 2;

    /**
     * Handle activity completed with a failure.
     */
    private static final int FAILED = 3;

    /**
     * Handle was cancelled before a successful terminal transition.
     */
    private static final int CANCELLED = 4;

    /**
     * Dispatch key.
     */
    private final String key;

    /**
     * Cancellation tag.
     */
    private final Object tag;

    /**
     * Activity.
     */
    private final Activity activity;

    /**
     * Execution result.
     */
    private final CompletableFuture<Void> future;

    /**
     * Authoritative dispatch state shared by every dispatcher channel.
     */
    private final AtomicInteger state;

    /**
     * Creates a dispatch handle.
     *
     * @param key      dispatch key
     * @param tag      cancellation tag
     * @param activity activity
     */
    private DispatchHandle(final String key, final Object tag, final Activity activity) {
        this.key = validateKey(key);
        this.tag = tag;
        this.activity = require(activity, "Activity");
        this.future = new CompletableFuture<>();
        this.state = new AtomicInteger(QUEUED);
    }

    /**
     * Creates a dispatch handle.
     *
     * @param key      dispatch key
     * @param tag      cancellation tag
     * @param activity activity
     * @return dispatch handle
     */
    public static DispatchHandle of(final String key, final Object tag, final Activity activity) {
        return new DispatchHandle(key, tag, activity);
    }

    /**
     * Returns the dispatch key.
     *
     * @return dispatch key
     */
    public String key() {
        return key;
    }

    /**
     * Returns the cancellation tag.
     *
     * @return cancellation tag
     */
    public Object tag() {
        return tag;
    }

    /**
     * Returns the activity.
     *
     * @return activity
     */
    public Activity activity() {
        return activity;
    }

    /**
     * Returns the execution future.
     *
     * @return execution future
     */
    public CompletableFuture<Void> future() {
        return future;
    }

    /**
     * Returns the handle lifecycle state.
     *
     * @return lifecycle state
     */
    @Override
    public Status state() {
        return switch (state.get()) {
            case QUEUED -> Status.QUEUED;
            case RUNNING -> Status.RUNNING;
            case DONE -> Status.DONE;
            case FAILED -> Status.FAILED;
            case CANCELLED -> Status.CANCELLED;
            default -> throw new IllegalStateException("Unknown dispatch state");
        };
    }

    /**
     * Atomically promotes this handle before a worker invokes its activity.
     *
     * @return true only for the worker that changed queued to running
     */
    boolean markRunning() {
        return state.compareAndSet(QUEUED, RUNNING);
    }

    /**
     * Cancels this handle and its activity.
     *
     * @return true when this invocation changed the state
     */
    public boolean cancel() {
        while (true) {
            final int current = state.get();
            if (current != QUEUED && current != RUNNING) {
                return false;
            }
            if (state.compareAndSet(current, CANCELLED)) {
                try {
                    activity.cancel();
                } finally {
                    future.cancel(false);
                }
                return true;
            }
        }
    }

    /**
     * Returns whether this handle is cancelled.
     *
     * @return true when cancelled
     */
    public boolean cancelled() {
        return state.get() == CANCELLED;
    }

    /**
     * Completes this handle successfully.
     */
    public void complete() {
        while (true) {
            final int current = state.get();
            if (current == DONE) {
                return;
            }
            if (current != QUEUED && current != RUNNING) {
                throw new StatefulException("Dispatch handle cannot complete from state " + state());
            }
            if (state.compareAndSet(current, DONE)) {
                future.complete(null);
                return;
            }
        }
    }

    /**
     * Fails this handle.
     *
     * @param cause failure cause
     */
    public void fail(final Throwable cause) {
        final Throwable failure = require(cause, "Failure cause");
        while (true) {
            final int current = state.get();
            if (current == FAILED) {
                return;
            }
            if (current != QUEUED && current != RUNNING) {
                throw new StatefulException("Dispatch handle cannot fail from state " + state());
            }
            if (state.compareAndSet(current, FAILED)) {
                future.completeExceptionally(failure);
                return;
            }
        }
    }

    /**
     * Validates dispatch keys.
     *
     * @param value key
     * @return normalized key
     */
    private static String validateKey(final String value) {
        final String current = Assert
                .notBlank(value, () -> new ValidateException("Dispatch key must be non-blank and single-line"));
        Assert.isFalse(
                StringKit.containsAny(current, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("Dispatch key must be non-blank and single-line"));
        return current.trim();
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
