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
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.runtime.Activity;

/**
 * Cancellable handle for a queued or running activity.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DispatchHandle {

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
     * Handle lifecycle state.
     */
    private final AtomicReference<Status> state;

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
        this.state = new AtomicReference<>(Status.QUEUED);
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
     * Cancels this handle and its activity.
     *
     * @return true when this invocation changed the state
     */
    public boolean cancel() {
        if (state.compareAndSet(Status.QUEUED, Status.CANCELLED)
                || state.compareAndSet(Status.RUNNING, Status.CANCELLED)
                || state.compareAndSet(Status.OPENED, Status.CANCELLED)) {
            activity.cancel();
            future.cancel(false);
            return true;
        }
        return false;
    }

    /**
     * Returns whether this handle is cancelled.
     *
     * @return true when cancelled
     */
    public boolean cancelled() {
        return state.get() == Status.CANCELLED || future.isCancelled() || activity.cancelled();
    }

    /**
     * Completes this handle successfully.
     */
    public void complete() {
        while (true) {
            final Status current = state.get();
            if (current == Status.DONE) {
                return;
            }
            if (current == Status.CANCELLED || current == Status.FAILED) {
                throw new StatefulException("Dispatch handle cannot complete from state " + current);
            }
            if (state.compareAndSet(current, Status.DONE)) {
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
        require(cause, "Failure cause");
        while (true) {
            final Status current = state.get();
            if (current == Status.FAILED) {
                return;
            }
            if (current == Status.DONE || current == Status.CANCELLED) {
                throw new StatefulException("Dispatch handle cannot fail from state " + current);
            }
            if (state.compareAndSet(current, Status.FAILED)) {
                future.completeExceptionally(cause);
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
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Dispatch key must be non-blank and single-line");
        }
        return value.trim();
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
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
