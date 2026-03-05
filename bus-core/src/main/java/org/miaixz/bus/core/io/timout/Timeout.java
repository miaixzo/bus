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
package org.miaixz.bus.core.io.timout;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

/**
 * A policy for how much time to spend on a task before giving up. When a task times out, it is in an unspecified state
 * and should be abandoned. For example, if reading from a source times out, the source should be closed and reading
 * should be retried later. If writing to a sink times out, the same rule applies: close the sink and retry later.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Timeout {

    /**
     * An empty timeout that neither tracks nor detects timeouts. Use this when no timeout is needed, for example, in
     * implementations where operations do not block.
     */
    public static final Timeout NONE = new Timeout() {

        /**
         * Returns the timeout for cache entries in milliseconds.
         *
         * @return the timeout in milliseconds
         */
        @Override
        public Timeout timeout(long timeout, TimeUnit unit) {
            return this;
        }

        /**
         * Deadlinenanotime method.
         *
         * @return the Timeout value
         */
        @Override
        public Timeout deadlineNanoTime(long deadlineNanoTime) {
            return this;
        }

        /**
         * Throwifreached method.
         *
         * @return the void value
         */
        @Override
        public void throwIfReached() {
        }
    };

    /**
     * True if a {@code deadlineNanoTime} is defined. There is no equivalent to null or 0 for {@link System#nanoTime}.
     */
    private boolean hasDeadline;
    /**
     * The deadline in nanoseconds, as returned by {@link System#nanoTime()}.
     */
    private long deadlineNanoTime;
    /**
     * The timeout duration in nanoseconds.
     */
    private long timeoutNanos;

    /**
     * Constructs a new {@code Timeout} instance with no timeout and no deadline set.
     */
    public Timeout() {

    }

    /**
     * Waits for at most {@code timeout} duration before aborting an operation. Using per-operation timeouts means that
     * any sequence of operations will not fail as long as progress is being made.
     *
     * @param timeout The maximum time to wait.
     * @param unit    The time unit of the {@code timeout} argument.
     * @return This {@code Timeout} instance for method chaining.
     * @throws IllegalArgumentException If {@code timeout} is negative or {@code unit} is null.
     */
    public Timeout timeout(long timeout, TimeUnit unit) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout < 0: " + timeout);
        if (unit == null)
            throw new IllegalArgumentException("unit == null");
        this.timeoutNanos = unit.toNanos(timeout);
        return this;
    }

    /**
     * Returns the timeout duration in nanoseconds, or {@code 0} if no timeout is set.
     *
     * @return The timeout duration in nanoseconds.
     */
    public long timeoutNanos() {
        return timeoutNanos;
    }

    /**
     * Returns {@code true} if a deadline is enabled for this timeout.
     *
     * @return {@code true} if a deadline is set, {@code false} otherwise.
     */
    public boolean hasDeadline() {
        return hasDeadline;
    }

    /**
     * Returns the deadline in {@linkplain System#nanoTime()} units.
     *
     * @return The deadline in nanoseconds.
     * @throws IllegalStateException If no deadline has been set.
     */
    public long deadlineNanoTime() {
        if (!hasDeadline)
            throw new IllegalStateException("No deadline");
        return deadlineNanoTime;
    }

    /**
     * Sets the deadline in {@linkplain System#nanoTime()} units. All operations must complete before this time. Use a
     * deadline to set an upper bound on the total time spent for a sequence of operations.
     *
     * @param deadlineNanoTime The deadline in nanoseconds.
     * @return This {@code Timeout} instance for method chaining.
     */
    public Timeout deadlineNanoTime(long deadlineNanoTime) {
        this.hasDeadline = true;
        this.deadlineNanoTime = deadlineNanoTime;
        return this;
    }

    /**
     * Sets the deadline to be {@code duration} from now.
     *
     * @param duration The duration from now until the deadline.
     * @param unit     The time unit of the {@code duration} argument.
     * @return This {@code Timeout} instance for method chaining.
     * @throws IllegalArgumentException If {@code duration} is less than or equal to 0, or {@code unit} is null.
     */
    public final Timeout deadline(long duration, TimeUnit unit) {
        if (duration <= 0)
            throw new IllegalArgumentException("duration <= 0: " + duration);
        if (unit == null)
            throw new IllegalArgumentException("unit == null");
        return deadlineNanoTime(System.nanoTime() + unit.toNanos(duration));
    }

    /**
     * Clears any timeout set for this instance.
     *
     * @return This {@code Timeout} instance for method chaining.
     */
    public Timeout clearTimeout() {
        this.timeoutNanos = 0;
        return this;
    }

    /**
     * Clears any deadline set for this instance.
     *
     * @return This {@code Timeout} instance for method chaining.
     */
    public Timeout clearDeadline() {
        this.hasDeadline = false;
        return this;
    }

    /**
     * Throws an {@link InterruptedIOException} if the deadline has been reached or if the current thread has been
     * interrupted. This method does not detect timeouts; timeouts should be implemented to abort ongoing operations
     * asynchronously.
     *
     * @throws IOException If the deadline is reached or the thread is interrupted.
     */
    public void throwIfReached() throws IOException {
        if (Thread.interrupted()) {
            // Preserve the interrupted status
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("interrupted");
        }

        if (hasDeadline && deadlineNanoTime - System.nanoTime() <= 0) {
            throw new InterruptedIOException("deadline reached");
        }
    }

    /**
     * Waits on {@code monitor} until notified. If the thread is interrupted or times out before being notified on
     * {@code monitor}, an {@link InterruptedIOException} is thrown. The caller must synchronize on {@code monitor}.
     *
     * @param monitor The object to wait on.
     * @throws InterruptedIOException       If the wait is interrupted or times out.
     * @throws IllegalMonitorStateException If the current thread is not the owner of the monitor's object.
     */
    public final void waitUntilNotified(Object monitor) throws InterruptedIOException {
        try {
            boolean hasDeadline = hasDeadline();
            long timeoutNanos = timeoutNanos();

            if (!hasDeadline && timeoutNanos == 0L) {
                // No timeout: wait indefinitely
                monitor.wait();
                return;
            }

            // Calculate how long to wait
            long waitNanos;
            long start = System.nanoTime();
            if (hasDeadline && timeoutNanos != 0) {
                long deadlineNanos = deadlineNanoTime() - start;
                waitNanos = Math.min(timeoutNanos, deadlineNanos);
            } else if (hasDeadline) {
                waitNanos = deadlineNanoTime() - start;
            } else {
                waitNanos = timeoutNanos;
            }

            // Attempt to wait for that duration. This will trigger early if the monitor is notified.
            long elapsedNanos = 0L;
            if (waitNanos > 0L) {
                long waitMillis = waitNanos / 1000000L;
                monitor.wait(waitMillis, (int) (waitNanos - waitMillis * 1000000L));
                elapsedNanos = System.nanoTime() - start;
            }

            // If the timeout occurred before the monitor was notified, throw an exception.
            if (elapsedNanos >= waitNanos) {
                throw new InterruptedIOException("timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("interrupted");
        }
    }

}
