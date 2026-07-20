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
package org.miaixz.bus.fabric.network.aio;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;

/**
 * Owns a JDK asynchronous channel group and the channels opened from it.
 * <p>
 * The dispatcher is used only as a borrowed runtime service by channels. A group created without an explicit dispatcher
 * owns that dispatcher and closes it after the channel group and channel lifecycle scope have closed.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class AioGroup implements AutoCloseable {

    /**
     * JDK asynchronous channel group.
     */
    final AsynchronousChannelGroup channelGroup;

    /**
     * Runtime dispatcher borrowed by channels opened from this group.
     */
    private final Dispatcher dispatcher;

    /**
     * Whether this group owns the dispatcher lifecycle.
     */
    private final boolean ownsDispatcher;

    /**
     * Lifecycle scope owning channels opened from this group.
     */
    private final LifecycleScope scope;

    /**
     * Whether this group still accepts new channels.
     */
    private final AtomicBoolean opened;

    /**
     * Creates an active AIO group.
     *
     * @param channelGroup   JDK asynchronous channel group
     * @param dispatcher     runtime dispatcher borrowed by channels
     * @param ownsDispatcher true when this group owns the dispatcher lifecycle
     */
    private AioGroup(final AsynchronousChannelGroup channelGroup, final Dispatcher dispatcher,
            final boolean ownsDispatcher) {
        this.channelGroup = Assert
                .notNull(channelGroup, () -> new ValidateException("AIO channel group must not be null"));
        this.dispatcher = Assert.notNull(dispatcher, () -> new ValidateException("Dispatcher must not be null"));
        this.ownsDispatcher = ownsDispatcher;
        this.scope = LifecycleScope.resource(this, "aio-group", null, EventObserver.noop());
        this.opened = new AtomicBoolean(true);
        this.scope.start();
        this.scope.open();
    }

    /**
     * Creates a group with an owned dispatcher.
     *
     * @param ioThreads JDK asynchronous channel group thread count
     * @return active group
     */
    public static AioGroup create(final int ioThreads) {
        return create(validateThreadCount(ioThreads), Dispatcher.create(), true);
    }

    /**
     * Creates a group with a borrowed dispatcher.
     *
     * @param ioThreads  JDK asynchronous channel group thread count
     * @param dispatcher borrowed runtime dispatcher
     * @return active group
     */
    public static AioGroup create(final int ioThreads, final Dispatcher dispatcher) {
        return create(ioThreads, dispatcher, false);
    }

    /**
     * Creates a group with the requested dispatcher ownership.
     *
     * @param ioThreads      JDK asynchronous channel group thread count
     * @param dispatcher     runtime dispatcher
     * @param ownsDispatcher true when this group owns the dispatcher lifecycle
     * @return active group
     */
    private static AioGroup create(final int ioThreads, final Dispatcher dispatcher, final boolean ownsDispatcher) {
        final int checkedThreads = validateThreadCount(ioThreads);
        final Dispatcher checkedDispatcher = Assert
                .notNull(dispatcher, () -> new ValidateException("Dispatcher must not be null"));
        AsynchronousChannelGroup group = null;
        try {
            group = AsynchronousChannelGroup
                    .withFixedThreadPool(checkedThreads, task -> ThreadKit.newThread(task, "fabric-aio-channel", true));
            return new AioGroup(group, checkedDispatcher, ownsDispatcher);
        } catch (final IOException | RuntimeException e) {
            closeAfterCreateFailure(group, checkedDispatcher, ownsDispatcher, e);
            throw new InternalException("Unable to create AIO group", e);
        }
    }

    /**
     * Returns the runtime dispatcher borrowed by channels.
     *
     * @return runtime dispatcher
     */
    public Dispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * Keeps the active group lifecycle compatible with callers that explicitly start resources.
     */
    public void start() {
        // Factory methods return an active group because providers may open channels immediately.
    }

    /**
     * Returns the lifecycle scope used to own channels opened by the package provider.
     *
     * @return lifecycle scope
     */
    LifecycleScope scope() {
        return scope;
    }

    /**
     * Returns whether this group accepts new channels.
     *
     * @return true when open
     */
    boolean opened() {
        return opened.get();
    }

    /**
     * Shuts down this group.
     */
    public void shutdown() {
        close();
    }

    /**
     * Rejects new channels, closes the JDK group and owned channels, and then closes an owned dispatcher.
     */
    @Override
    public void close() {
        if (!opened.compareAndSet(true, false)) {
            return;
        }
        RuntimeException failure = null;
        try {
            channelGroup.shutdownNow();
        } catch (final IOException e) {
            failure = new InternalException("Unable to shut down AIO channel group", e);
        }
        try {
            scope.close();
        } catch (final RuntimeException e) {
            failure = append(failure, e);
        }
        if (ownsDispatcher) {
            try {
                dispatcher.close();
            } catch (final RuntimeException e) {
                failure = append(failure, e);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Waits for JDK asynchronous channel group termination.
     *
     * @param timeout maximum wait duration
     * @return true when terminated before the timeout
     */
    public boolean awaitTermination(final Duration timeout) {
        final Duration checkedTimeout = Assert
                .notNull(timeout, () -> new ValidateException("Termination timeout must be non-null and non-negative"));
        Assert.isTrue(
                !checkedTimeout.isNegative(),
                () -> new ValidateException("Termination timeout must be non-null and non-negative"));
        final long timeoutNanos;
        try {
            timeoutNanos = checkedTimeout.toNanos();
        } catch (final ArithmeticException e) {
            throw new ValidateException("Termination timeout is out of range", e);
        }
        final long startedNanos = System.nanoTime();
        while (!channelGroup.isTerminated()) {
            final long elapsed = System.nanoTime() - startedNanos;
            if (elapsed >= timeoutNanos) {
                return false;
            }
            final long remaining = timeoutNanos - elapsed;
            final long pause = Math.min(remaining, TimeUnit.MILLISECONDS.toNanos(Normal._1));
            if (!ThreadKit.sleep(pause, TimeUnit.NANOSECONDS)) {
                throw new InternalException("Interrupted while awaiting AIO termination");
            }
        }
        return true;
    }

    /**
     * Closes partially created resources before reporting a factory failure.
     *
     * @param group          JDK group, when created
     * @param dispatcher     runtime dispatcher
     * @param ownsDispatcher true when the dispatcher was created for this group
     * @param cause          creation failure receiving cleanup failures as suppressed exceptions
     */
    private static void closeAfterCreateFailure(
            final AsynchronousChannelGroup group,
            final Dispatcher dispatcher,
            final boolean ownsDispatcher,
            final Throwable cause) {
        if (group != null) {
            try {
                group.shutdownNow();
            } catch (final IOException | RuntimeException e) {
                cause.addSuppressed(e);
            }
        }
        if (ownsDispatcher) {
            try {
                dispatcher.close();
            } catch (final RuntimeException e) {
                cause.addSuppressed(e);
            }
        }
    }

    /**
     * Adds a cleanup failure to an existing failure.
     *
     * @param current current failure, when present
     * @param next    next cleanup failure
     * @return failure to throw
     */
    private static RuntimeException append(final RuntimeException current, final RuntimeException next) {
        if (current == null) {
            return next;
        }
        current.addSuppressed(next);
        return current;
    }

    /**
     * Validates the JDK asynchronous channel group thread count.
     *
     * @param ioThreads requested thread count
     * @return validated thread count
     */
    private static int validateThreadCount(final int ioThreads) {
        return Assert.checkBetween(
                ioThreads,
                Normal._1,
                Normal._256,
                () -> new ValidateException("AIO thread count out of range"));
    }

}
