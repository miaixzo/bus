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
import java.util.concurrent.TimeUnit;

/**
 * A {@link Timeout} implementation that delegates all calls to another {@link Timeout} instance. This class is useful
 * for wrapping an existing timeout and potentially changing its behavior or for providing a mutable timeout reference.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AssignTimeout extends Timeout {

    /**
     * The underlying {@link Timeout} instance to which all calls are delegated.
     */
    private Timeout delegate;

    /**
     * Constructs a new {@code AssignTimeout} with the specified delegate.
     *
     * @param delegate The {@link Timeout} instance to delegate calls to. Must not be {@code null}.
     * @throws IllegalArgumentException If the {@code delegate} is {@code null}.
     */
    public AssignTimeout(Timeout delegate) {
        if (null == delegate) {
            throw new IllegalArgumentException("delegate == null");
        }
        this.delegate = delegate;
    }

    /**
     * Returns the {@link Timeout} instance to which this instance currently delegates.
     *
     * @return The current delegate {@link Timeout}.
     */
    public final Timeout delegate() {
        return delegate;
    }

    /**
     * Sets the delegate {@link Timeout} instance.
     *
     * @param delegate The new {@link Timeout} instance to delegate calls to. Must not be {@code null}.
     * @return This {@code AssignTimeout} instance for method chaining.
     * @throws IllegalArgumentException If the {@code delegate} is {@code null}.
     */
    public final AssignTimeout setDelegate(Timeout delegate) {
        if (null == delegate) {
            throw new IllegalArgumentException("delegate == null");
        }
        this.delegate = delegate;
        return this;
    }

    /**
     * Delegates the call to {@link Timeout#timeout(long, TimeUnit)} of the underlying delegate.
     *
     * @param timeout The maximum time to wait.
     * @param unit    The time unit of the {@code timeout} argument.
     * @return The delegate {@link Timeout} instance.
     */
    @Override
    public Timeout timeout(long timeout, TimeUnit unit) {
        return delegate.timeout(timeout, unit);
    }

    /**
     * Delegates the call to {@link Timeout#timeoutNanos()} of the underlying delegate.
     *
     * @return The timeout duration in nanoseconds.
     */
    @Override
    public long timeoutNanos() {
        return delegate.timeoutNanos();
    }

    /**
     * Delegates the call to {@link Timeout#hasDeadline()} of the underlying delegate.
     *
     * @return {@code true} if a deadline is set, {@code false} otherwise.
     */
    @Override
    public boolean hasDeadline() {
        return delegate.hasDeadline();
    }

    /**
     * Delegates the call to {@link Timeout#deadlineNanoTime()} of the underlying delegate.
     *
     * @return The deadline in nanoseconds.
     */
    @Override
    public long deadlineNanoTime() {
        return delegate.deadlineNanoTime();
    }

    /**
     * Delegates the call to {@link Timeout#deadlineNanoTime(long)} of the underlying delegate.
     *
     * @param deadlineNanoTime The deadline in nanoseconds.
     * @return The delegate {@link Timeout} instance.
     */
    @Override
    public Timeout deadlineNanoTime(long deadlineNanoTime) {
        return delegate.deadlineNanoTime(deadlineNanoTime);
    }

    /**
     * Delegates the call to {@link Timeout#clearTimeout()} of the underlying delegate.
     *
     * @return The delegate {@link Timeout} instance.
     */
    @Override
    public Timeout clearTimeout() {
        return delegate.clearTimeout();
    }

    /**
     * Delegates the call to {@link Timeout#clearDeadline()} of the underlying delegate.
     *
     * @return The delegate {@link Timeout} instance.
     */
    @Override
    public Timeout clearDeadline() {
        return delegate.clearDeadline();
    }

    /**
     * Delegates the call to {@link Timeout#throwIfReached()} of the underlying delegate.
     *
     * @throws IOException If the deadline is reached or the thread is interrupted.
     */
    @Override
    public void throwIfReached() throws IOException {
        delegate.throwIfReached();
    }

}
