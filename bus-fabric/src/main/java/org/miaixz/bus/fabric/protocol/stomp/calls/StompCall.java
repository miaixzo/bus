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
package org.miaixz.bus.fabric.protocol.stomp.calls;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.protocol.stomp.StompSession;
import org.miaixz.bus.fabric.protocol.stomp.StompX;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.logger.Logger;

/**
 * Single-use STOMP open call.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StompCall implements Call<StompSession> {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

    /**
     * Dispatcher activity name for opening the STOMP session.
     */
    private static final String ACTIVITY_OPEN = "stomp-open";

    /**
     * Source exchange.
     */
    private final StompX exchange;

    /**
     * Optional dispatcher for default asynchronous submission.
     */
    private final Dispatcher dispatcher;

    /**
     * Result future.
     */
    private final CompletableFuture<StompSession> future;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Dispatch handle.
     */
    private final AtomicReference<DispatchHandle> handle;

    /**
     * Opened session.
     */
    private final AtomicReference<StompSession> session;

    /**
     * Creates a call.
     *
     * @param exchange exchange
     */
    private StompCall(final StompX exchange) {
        this(exchange, null);
    }

    /**
     * Creates a call.
     *
     * @param exchange   exchange
     * @param dispatcher dispatcher used by enqueue()
     */
    private StompCall(final StompX exchange, final Dispatcher dispatcher) {
        this.exchange = require(exchange, "STOMP exchange");
        this.dispatcher = dispatcher;
        this.future = new CompletableFuture<>();
        this.state = new AtomicReference<>(Status.QUEUED);
        this.handle = new AtomicReference<>();
        this.session = new AtomicReference<>();
    }

    /**
     * Creates a call.
     *
     * @param exchange exchange
     * @return call
     */
    public static StompCall create(final StompX exchange) {
        return new StompCall(exchange);
    }

    /**
     * Creates a call.
     *
     * @param exchange   exchange
     * @param dispatcher dispatcher used by enqueue()
     * @return call
     */
    public static StompCall create(final StompX exchange, final Dispatcher dispatcher) {
        return new StompCall(exchange, require(dispatcher, "Dispatcher"));
    }

    /**
     * Opens synchronously.
     *
     * @return session
     */
    public StompSession open() {
        if (!state.compareAndSet(Status.QUEUED, Status.RUNNING)) {
            throw new StatefulException("STOMP call can only be opened once");
        }
        Logger.info(true, LOG_TAG, "STOMP call started: key={}", exchange.dispatchKey());
        try {
            final StompSession opened = exchange.open();
            session.set(opened);
            state.set(Status.DONE);
            future.complete(opened);
            Logger.info(false, LOG_TAG, "STOMP call completed: key={}", exchange.dispatchKey());
            return opened;
        } catch (final RuntimeException e) {
            state.set(Status.FAILED);
            future.completeExceptionally(e);
            Logger.error(
                    false,
                    LOG_TAG,
                    e,
                    "STOMP call failed: key={}, exception={}",
                    exchange.dispatchKey(),
                    e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Executes synchronously.
     *
     * @return session
     */
    @Override
    public StompSession execute() {
        return open();
    }

    /**
     * Enqueues this call to the configured dispatcher.
     *
     * @return this call
     */
    @Override
    public Call<StompSession> enqueue() {
        return enqueue(require(dispatcher, "Dispatcher"));
    }

    /**
     * Enqueues this call to a dispatcher.
     *
     * @param dispatcher dispatcher
     * @return this call
     */
    public Call<StompSession> enqueue(final Dispatcher dispatcher) {
        final Dispatcher currentDispatcher = require(dispatcher, "Dispatcher");
        if (handle.get() != null) {
            return this;
        }
        final Activity activity = Activity.of(ACTIVITY_OPEN, () -> {
            try {
                open();
            } catch (final RuntimeException e) {
                future.completeExceptionally(e);
                throw e;
            }
        });
        final DispatchHandle enqueued = currentDispatcher.enqueue(exchange.dispatchKey(), activity);
        if (!handle.compareAndSet(null, enqueued)) {
            enqueued.cancel();
        } else {
            Logger.info(false, LOG_TAG, "STOMP call enqueued: key={}", exchange.dispatchKey());
        }
        return this;
    }

    /**
     * Waits for this call to complete.
     *
     * @return session
     */
    @Override
    public StompSession await() {
        startIfNeeded();
        return awaitFuture(future);
    }

    /**
     * Waits for this call to complete within a timeout.
     *
     * @param timeout timeout
     * @return session
     */
    @Override
    public StompSession await(final Duration timeout) {
        validateTimeout(timeout);
        startIfNeeded();
        return awaitFuture(future, timeout);
    }

    /**
     * Cancels this call.
     *
     * @return true when this invocation changed the state
     */
    @Override
    public boolean cancel() {
        while (true) {
            final Status current = state.get();
            if (current == Status.CANCELLED || current == Status.DONE || current == Status.FAILED) {
                return false;
            }
            if (state.compareAndSet(current, Status.CANCELLED)) {
                final DispatchHandle currentHandle = handle.get();
                if (currentHandle != null) {
                    currentHandle.cancel();
                }
                final StompSession currentSession = session.get();
                if (currentSession != null) {
                    currentSession.cancel();
                }
                future.cancel(false);
                Logger.info(false, LOG_TAG, "STOMP call cancelled: key={}", exchange.dispatchKey());
                return true;
            }
        }
    }

    /**
     * Returns whether this call is cancelled.
     *
     * @return true when cancelled
     */
    @Override
    public boolean cancelled() {
        final DispatchHandle current = handle.get();
        return state.get() == Status.CANCELLED || future.isCancelled() || current != null && current.cancelled();
    }

    /**
     * Returns whether this call is complete.
     *
     * @return true when complete
     */
    @Override
    public boolean done() {
        return future.isDone();
    }

    /**
     * Starts the call when await() is used as the first terminal operation.
     */
    private void startIfNeeded() {
        if (state.get() != Status.QUEUED || handle.get() != null) {
            return;
        }
        if (dispatcher == null) {
            execute();
        } else {
            enqueue();
        }
    }

    /**
     * Waits for a future to complete.
     *
     * @param future future
     * @return completed value
     */
    private static StompSession awaitFuture(final CompletableFuture<StompSession> future) {
        try {
            return future.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for STOMP call", e);
        } catch (final ExecutionException e) {
            throw new InternalException("STOMP call failed", e.getCause());
        } catch (final CancellationException e) {
            throw new InternalException("STOMP call was cancelled", e);
        }
    }

    /**
     * Waits for a future to complete within a timeout.
     *
     * @param future  future
     * @param timeout timeout
     * @return completed value
     */
    private StompSession awaitFuture(final CompletableFuture<StompSession> future, final Duration timeout) {
        if (timeout.isZero()) {
            if (!future.isDone()) {
                cancel();
                throw new TimeoutException("STOMP call timed out");
            }
            return awaitFuture(future);
        }
        try {
            return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for STOMP call", e);
        } catch (final ExecutionException e) {
            throw new InternalException("STOMP call failed", e.getCause());
        } catch (final CancellationException e) {
            throw new InternalException("STOMP call was cancelled", e);
        } catch (final java.util.concurrent.TimeoutException e) {
            cancel();
            throw new TimeoutException("STOMP call timed out", e);
        } catch (final ArithmeticException e) {
            throw new ValidateException("Timeout is too large");
        }
    }

    /**
     * Validates timeout.
     *
     * @param timeout timeout
     */
    private static void validateTimeout(final Duration timeout) {
        final Duration checked = Assert
                .notNull(timeout, () -> new ValidateException("Timeout must be non-null and non-negative"));
        Assert.isTrue(!checked.isNegative(), () -> new ValidateException("Timeout must be non-null and non-negative"));
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
