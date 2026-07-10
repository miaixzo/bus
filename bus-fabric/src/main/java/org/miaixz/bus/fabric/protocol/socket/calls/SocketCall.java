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
package org.miaixz.bus.fabric.protocol.socket.calls;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.protocol.socket.SocketSession;
import org.miaixz.bus.fabric.protocol.socket.SocketX;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.logger.Logger;

/**
 * Single-use socket open call.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketCall implements Call<SocketSession> {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

    /**
     * Exchange.
     */
    private final SocketX exchange;

    /**
     * Optional dispatcher for default asynchronous submission.
     */
    private final Dispatcher dispatcher;

    /**
     * Future.
     */
    private final CompletableFuture<SocketSession> future;

    /**
     * State.
     */
    private final AtomicReference<Status> state;

    /**
     * Dispatch handle.
     */
    private final AtomicReference<DispatchHandle> handle;

    /**
     * Opened session.
     */
    private final AtomicReference<SocketSession> session;

    /**
     * Creates a call.
     *
     * @param exchange exchange
     */
    private SocketCall(final SocketX exchange) {
        this(exchange, null);
    }

    /**
     * Creates a call.
     *
     * @param exchange   exchange
     * @param dispatcher dispatcher used by enqueue()
     */
    private SocketCall(final SocketX exchange, final Dispatcher dispatcher) {
        if (exchange == null) {
            throw new ValidateException("Socket exchange must not be null");
        }
        this.exchange = exchange;
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
    public static SocketCall create(final SocketX exchange) {
        return new SocketCall(exchange);
    }

    /**
     * Creates a call.
     *
     * @param exchange   exchange
     * @param dispatcher dispatcher used by enqueue()
     * @return call
     */
    public static SocketCall create(final SocketX exchange, final Dispatcher dispatcher) {
        return new SocketCall(exchange, require(dispatcher, "Dispatcher"));
    }

    /**
     * Opens synchronously.
     *
     * @return session
     */
    public SocketSession open() {
        if (!state.compareAndSet(Status.QUEUED, Status.RUNNING)) {
            throw new StatefulException("Socket call can only be opened once");
        }
        Logger.info(true, LOG_TAG, "Socket call started: key={}", exchange.dispatchKey());
        try {
            final SocketSession opened = exchange.open();
            session.set(opened);
            state.set(Status.DONE);
            future.complete(opened);
            Logger.info(false, LOG_TAG, "Socket call completed: key={}", exchange.dispatchKey());
            return opened;
        } catch (final RuntimeException e) {
            state.set(Status.FAILED);
            future.completeExceptionally(e);
            Logger.error(
                    false,
                    LOG_TAG,
                    e,
                    "Socket call failed: key={}, exception={}",
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
    public SocketSession execute() {
        return open();
    }

    /**
     * Enqueues this call to its configured dispatcher.
     *
     * @return this call
     */
    @Override
    public Call<SocketSession> enqueue() {
        return enqueue(require(dispatcher, "Dispatcher"));
    }

    /**
     * Enqueues this call to a dispatcher.
     *
     * @param dispatcher dispatcher
     * @return this call
     */
    public Call<SocketSession> enqueue(final Dispatcher dispatcher) {
        if (dispatcher == null) {
            throw new ValidateException("Dispatcher must not be null");
        }
        if (handle.get() != null) {
            return this;
        }
        final Activity activity = Activity.of("socket-open", () -> {
            try {
                open();
            } catch (final RuntimeException e) {
                future.completeExceptionally(e);
                throw e;
            }
        });
        final DispatchHandle enqueued = dispatcher.enqueue(exchange.dispatchKey(), activity);
        if (!handle.compareAndSet(null, enqueued)) {
            enqueued.cancel();
        } else {
            Logger.info(false, LOG_TAG, "Socket call enqueued: key={}", exchange.dispatchKey());
        }
        return this;
    }

    /**
     * Waits for this call to complete.
     */
    @Override
    public SocketSession await() {
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
    public SocketSession await(final Duration timeout) {
        validateTimeout(timeout);
        startIfNeeded();
        return awaitFuture(future, timeout);
    }

    /**
     * Cancels this call.
     *
     * @return true when state changed
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
                final SocketSession currentSession = session.get();
                if (currentSession != null) {
                    currentSession.cancel();
                }
                future.cancel(false);
                Logger.info(false, LOG_TAG, "Socket call cancelled: key={}", exchange.dispatchKey());
                return true;
            }
        }
    }

    /**
     * Returns whether cancelled.
     *
     * @return true when cancelled
     */
    @Override
    public boolean cancelled() {
        final DispatchHandle current = handle.get();
        return state.get() == Status.CANCELLED || future.isCancelled() || current != null && current.cancelled();
    }

    /**
     * Returns whether done.
     *
     * @return true when done
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
    private static SocketSession awaitFuture(final CompletableFuture<SocketSession> future) {
        try {
            return future.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for socket call", e);
        } catch (final ExecutionException e) {
            throw new InternalException("Socket call failed", e.getCause());
        } catch (final CancellationException e) {
            throw new InternalException("Socket call was cancelled", e);
        }
    }

    /**
     * Waits for a future to complete within a timeout.
     *
     * @param future  future
     * @param timeout timeout
     * @return completed value
     */
    private SocketSession awaitFuture(final CompletableFuture<SocketSession> future, final Duration timeout) {
        if (timeout.isZero()) {
            if (!future.isDone()) {
                cancel();
                throw new TimeoutException("Socket call timed out");
            }
            return awaitFuture(future);
        }
        try {
            return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for socket call", e);
        } catch (final ExecutionException e) {
            throw new InternalException("Socket call failed", e.getCause());
        } catch (final CancellationException e) {
            throw new InternalException("Socket call was cancelled", e);
        } catch (final java.util.concurrent.TimeoutException e) {
            cancel();
            throw new TimeoutException("Socket call timed out", e);
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
        if (timeout == null || timeout.isNegative()) {
            throw new ValidateException("Timeout must be non-null and non-negative");
        }
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
