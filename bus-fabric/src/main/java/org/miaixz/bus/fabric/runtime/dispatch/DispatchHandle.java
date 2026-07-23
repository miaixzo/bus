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
     * Trimmed, single-line key used for per-key dispatch accounting.
     */
    private final String key;

    /**
     * Optional identity used to group cancellation requests.
     */
    private final Object tag;

    /**
     * Activity controlled by this handle.
     */
    private final Activity activity;

    /**
     * Completion channel reflecting this handle's terminal transition.
     */
    private final CompletableFuture<Void> future;

    /**
     * Authoritative dispatch state shared by every dispatcher channel.
     */
    private final AtomicInteger state;

    /**
     * Creates a queued dispatch handle and a new incomplete future.
     *
     * @param key      non-blank, single-line dispatch key
     * @param tag      optional cancellation tag
     * @param activity activity controlled by the handle
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
     * @param key      non-blank, single-line dispatch key
     * @param tag      optional cancellation tag, which may be {@code null}
     * @param activity activity controlled by the handle
     * @return newly queued handle with an incomplete execution future
     * @throws ValidateException if the key is blank or multi-line, or if {@code activity} is {@code null}
     */
    public static DispatchHandle of(final String key, final Object tag, final Activity activity) {
        return new DispatchHandle(key, tag, activity);
    }

    /**
     * Returns the dispatch key.
     *
     * @return trimmed, single-line key used for dispatch accounting
     */
    public String key() {
        return key;
    }

    /**
     * Returns the cancellation tag.
     *
     * @return cancellation tag, or {@code null} when none was assigned
     */
    public Object tag() {
        return tag;
    }

    /**
     * Returns the activity.
     *
     * @return activity controlled by this handle
     */
    public Activity activity() {
        return activity;
    }

    /**
     * Returns the execution future.
     *
     * @return future completed, failed, or cancelled with this handle
     */
    public CompletableFuture<Void> future() {
        return future;
    }

    /**
     * Returns the handle lifecycle state.
     *
     * @return current queued, running, or terminal dispatch status
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
     * @return {@code true} only for the invocation that changed the state from queued to running
     */
    boolean markRunning() {
        return state.compareAndSet(QUEUED, RUNNING);
    }

    /**
     * Atomically changes a queued or running handle to cancelled, then cancels its activity and future.
     *
     * @return {@code true} if this invocation won the terminal state transition
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
     * @return {@code true} if the authoritative handle state is cancelled
     */
    public boolean cancelled() {
        return state.get() == CANCELLED;
    }

    /**
     * Atomically completes a queued or running handle and its future successfully.
     *
     * @throws StatefulException if the handle has already failed or been cancelled
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
     * Atomically fails a queued or running handle and completes its future exceptionally.
     *
     * @param cause failure stored in the execution future
     * @throws ValidateException if {@code cause} is {@code null}
     * @throws StatefulException if the handle has already completed successfully or been cancelled
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
     * @param value candidate dispatch key
     * @return trimmed, non-blank, single-line dispatch key
     * @throws ValidateException if the candidate is blank or contains a carriage return or line feed
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
