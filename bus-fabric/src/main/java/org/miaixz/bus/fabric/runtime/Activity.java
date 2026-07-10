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
package org.miaixz.bus.fabric.runtime;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Atomic activity wrapper for named runnable work.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Activity implements Runnable {

    /**
     * Activity name.
     */
    private final String name;

    /**
     * Runnable work.
     */
    private final Runnable runnable;

    /**
     * Shared cancellation scope.
     */
    private final Cancellation cancellation;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Failure cause.
     */
    private final AtomicReference<Throwable> failure;

    /**
     * Creates an activity.
     *
     * @param name     activity name
     * @param runnable runnable
     */
    private Activity(final String name, final Runnable runnable) {
        this(name, runnable, Cancellation.create());
    }

    /**
     * Creates an activity.
     *
     * @param name         activity name
     * @param runnable     runnable
     * @param cancellation cancellation scope
     */
    private Activity(final String name, final Runnable runnable, final Cancellation cancellation) {
        this.name = validateName(name);
        this.runnable = require(runnable, "Runnable");
        this.cancellation = require(cancellation, "Cancellation");
        this.state = new AtomicReference<>(Status.QUEUED);
        this.failure = new AtomicReference<>();
    }

    /**
     * Creates an activity.
     *
     * @param name     activity name
     * @param runnable runnable
     * @return activity
     */
    public static Activity of(final String name, final Runnable runnable) {
        return new Activity(name, runnable);
    }

    /**
     * Creates an activity bound to an existing cancellation scope.
     *
     * @param name         activity name
     * @param runnable     runnable
     * @param cancellation cancellation scope
     * @return activity
     */
    public static Activity of(final String name, final Runnable runnable, final Cancellation cancellation) {
        return new Activity(name, runnable, cancellation);
    }

    /**
     * Returns the activity name.
     *
     * @return activity name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the lifecycle state.
     *
     * @return lifecycle state
     */
    public Status state() {
        return state.get();
    }

    /**
     * Returns the shared cancellation scope.
     *
     * @return cancellation scope
     */
    public Cancellation cancellation() {
        return cancellation;
    }

    /**
     * Cancels this activity.
     *
     * @return true when this invocation changed the state
     */
    public boolean cancel() {
        final boolean changed = state.compareAndSet(Status.QUEUED, Status.CANCELLED)
                || state.compareAndSet(Status.RUNNING, Status.CANCELLED)
                || state.compareAndSet(Status.OPENED, Status.CANCELLED);
        if (changed || !cancellation.cancelled()) {
            cancellation.cancel(new CancellationException("Activity cancelled: " + name));
        }
        return changed;
    }

    /**
     * Returns whether this activity is cancelled.
     *
     * @return true when cancelled
     */
    public boolean cancelled() {
        return state.get() == Status.CANCELLED || cancellation.cancelled();
    }

    /**
     * Runs the activity once.
     */
    @Override
    public void run() {
        if (!state.compareAndSet(Status.QUEUED, Status.RUNNING)) {
            throw new StatefulException("Activity cannot be run from state " + state.get());
        }
        try {
            cancellation.throwIfCancelled();
            runnable.run();
            state.set(cancellation.cancelled() ? Status.CANCELLED : Status.DONE);
        } catch (final RuntimeException e) {
            failure.set(e);
            if (e instanceof CancellationException || cancellation.cancelled()) {
                state.set(Status.CANCELLED);
                throw e;
            }
            state.set(Status.FAILED);
            throw new InternalException("Activity failed", e);
        }
    }

    /**
     * Returns the failure cause.
     *
     * @return failure cause or null
     */
    public Throwable failure() {
        return failure.get();
    }

    /**
     * Validates activity names.
     *
     * @param value activity name
     * @return normalized name
     */
    private static String validateName(final String value) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Activity name must be non-blank and single-line");
        }
        return value.trim();
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
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
