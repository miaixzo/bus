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
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Lifecycle;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Atomic activity wrapper for named runnable work.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Activity implements Runnable, Lifecycle {

    /**
     * Activity has been accepted but has not started running.
     */
    private static final int QUEUED = 0;

    /**
     * Activity action is currently running.
     */
    private static final int RUNNING = 1;

    /**
     * Activity action completed successfully.
     */
    private static final int DONE = 2;

    /**
     * Activity action terminated with a retained failure.
     */
    private static final int FAILED = 3;

    /**
     * Activity was cancelled before a successful terminal transition.
     */
    private static final int CANCELLED = 4;

    /**
     * Trimmed non-blank, single-line diagnostic name.
     */
    private final String name;

    /**
     * Work reference cleared after execution or successful cancellation to release captured state.
     */
    private volatile Runnable action;

    /**
     * Cancellation scope observed before and during action execution.
     */
    private final Cancellation cancellation;

    /**
     * Atomic lifecycle state encoded by the state constants in this class.
     */
    private final AtomicInteger state;

    /**
     * First action failure retained for diagnostics.
     * <p>
     * The reference is cleared only with the activity instance and is never replaced by later terminal attempts.
     * </p>
     */
    private volatile Throwable failure;

    /**
     * Creates an activity.
     *
     * @param name     diagnostic activity name
     * @param runnable work executed at most once
     */
    private Activity(final String name, final Runnable runnable) {
        this(name, runnable, Cancellation.create());
    }

    /**
     * Creates an activity.
     *
     * @param name         diagnostic activity name
     * @param runnable     work executed at most once
     * @param cancellation scope observed before and after the action and cancelled by {@link #cancel()}
     */
    private Activity(final String name, final Runnable runnable, final Cancellation cancellation) {
        this.name = validateName(name);
        this.action = require(runnable, "Runnable");
        this.cancellation = require(cancellation, "Cancellation");
        this.state = new AtomicInteger(QUEUED);
    }

    /**
     * Creates an activity.
     *
     * @param name     diagnostic activity name
     * @param runnable work executed at most once
     * @return queued activity with a new cancellation scope
     * @throws ValidateException if the name or runnable is invalid
     */
    public static Activity of(final String name, final Runnable runnable) {
        return new Activity(name, runnable);
    }

    /**
     * Creates an activity bound to an existing cancellation scope.
     *
     * @param name         diagnostic activity name
     * @param runnable     work executed at most once
     * @param cancellation existing scope shared with the activity
     * @return queued activity bound to the supplied scope
     * @throws ValidateException if the name, runnable, or scope is invalid
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
        return switch (state.get()) {
            case QUEUED -> Status.QUEUED;
            case RUNNING -> Status.RUNNING;
            case DONE -> Status.DONE;
            case FAILED -> Status.FAILED;
            case CANCELLED -> Status.CANCELLED;
            default -> throw new IllegalStateException("Unknown activity state");
        };
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
     * @return {@code true} when this invocation changes queued/running state to cancelled
     */
    public boolean cancel() {
        while (true) {
            final int current = state.get();
            if (current != QUEUED && current != RUNNING) {
                return false;
            }
            if (state.compareAndSet(current, CANCELLED)) {
                action = null;
                cancellation.cancel(new CancellationException("Activity cancelled: " + name));
                return true;
            }
        }
    }

    /**
     * Returns whether this activity is cancelled.
     *
     * @return {@code true} when the atomic activity state is cancelled
     */
    public boolean cancelled() {
        return state.get() == CANCELLED;
    }

    /**
     * Runs the action once and records done, failed, or cancelled terminal state.
     *
     * @throws StatefulException     if the activity is not queued
     * @throws CancellationException if its scope is cancelled before or during cooperative execution
     * @throws InternalException     if the action throws another runtime exception
     */
    @Override
    public void run() {
        if (!state.compareAndSet(QUEUED, RUNNING)) {
            throw new StatefulException("Activity cannot be run from state " + state());
        }
        final Runnable current = action;
        try {
            cancellation.throwIfCancelled();
            current.run();
            if (cancellation.cancelled()) {
                state.compareAndSet(RUNNING, CANCELLED);
            } else {
                state.compareAndSet(RUNNING, DONE);
            }
        } catch (final RuntimeException e) {
            failure = e;
            if (e instanceof CancellationException || cancellation.cancelled()) {
                state.compareAndSet(RUNNING, CANCELLED);
                throw e;
            }
            state.compareAndSet(RUNNING, FAILED);
            throw new InternalException("Activity failed", e);
        } finally {
            action = null;
        }
    }

    /**
     * Returns the failure cause.
     *
     * @return first runtime failure thrown by the action, including cancellation, or {@code null}
     */
    public Throwable failure() {
        return failure;
    }

    /**
     * Validates activity names.
     *
     * @param value activity name
     * @return normalized name
     */
    private static String validateName(final String value) {
        final String current = Assert
                .notBlank(value, () -> new ValidateException("Activity name must be non-blank and single-line"));
        Assert.isFalse(
                StringKit.containsAny(current, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("Activity name must be non-blank and single-line"));
        return current.trim();
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  logical field name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
