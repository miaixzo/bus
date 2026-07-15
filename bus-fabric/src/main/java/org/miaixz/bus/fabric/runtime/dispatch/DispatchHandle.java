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

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Lifecycle;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * Cancellable handle for a queued or running activity.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DispatchHandle implements Lifecycle {

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
     * Handle lifecycle scope.
     */
    private final LifecycleScope scope;

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
        this.scope = LifecycleScope.resource(this, this.key, null, EventObserver.noop());
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
        return scope.state();
    }

    /**
     * Cancels this handle and its activity.
     *
     * @return true when this invocation changed the state
     */
    public boolean cancel() {
        if (scope.cancel(new CancellationException("Dispatch handle cancelled: " + key))) {
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
        return scope.state() == Status.CANCELLED || future.isCancelled() || activity.cancelled();
    }

    /**
     * Completes this handle successfully.
     */
    public void complete() {
        final Status current = scope.state();
        if (current == Status.DONE) {
            return;
        }
        if (current == Status.CANCELLED || current == Status.FAILED) {
            throw new StatefulException("Dispatch handle cannot complete from state " + current);
        }
        if (scope.complete()) {
            future.complete(null);
        }
    }

    /**
     * Fails this handle.
     *
     * @param cause failure cause
     */
    public void fail(final Throwable cause) {
        require(cause, "Failure cause");
        final Status current = scope.state();
        if (current == Status.FAILED) {
            return;
        }
        if (current == Status.DONE || current == Status.CANCELLED) {
            throw new StatefulException("Dispatch handle cannot fail from state " + current);
        }
        if (scope.fail(cause)) {
            future.completeExceptionally(cause);
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
