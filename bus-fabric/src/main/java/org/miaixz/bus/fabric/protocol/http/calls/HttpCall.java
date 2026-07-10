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
package org.miaixz.bus.fabric.protocol.http.calls;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Single-use HTTP call with synchronous and dispatched execution views.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpCall implements Call<HttpResponse> {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

    /**
     * Source exchange.
     */
    private final HttpX exchange;

    /**
     * Request snapshot.
     */
    private final HttpRequest request;

    /**
     * Optional dispatcher for default asynchronous submission.
     */
    private final Dispatcher dispatcher;

    /**
     * Result future.
     */
    private final CompletableFuture<HttpResponse> future;

    /**
     * Shared cancellation scope.
     */
    private final Cancellation cancellation;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Dispatch handle.
     */
    private final AtomicReference<DispatchHandle> handle;

    /**
     * Creates a call.
     *
     * @param exchange source exchange
     */
    private HttpCall(final HttpX exchange) {
        this(exchange, null);
    }

    /**
     * Creates a call.
     *
     * @param exchange   source exchange
     * @param dispatcher dispatcher used by enqueue()
     */
    private HttpCall(final HttpX exchange, final Dispatcher dispatcher) {
        this.exchange = require(exchange, "HTTP exchange");
        this.request = require(exchange.request(), "HTTP request");
        this.dispatcher = dispatcher;
        this.future = new CompletableFuture<>();
        this.cancellation = Cancellation.create();
        this.state = new AtomicReference<>(Status.QUEUED);
        this.handle = new AtomicReference<>();
    }

    /**
     * Creates a call for an exchange.
     *
     * @param exchange source exchange
     * @return HTTP call
     */
    public static HttpCall create(final HttpX exchange) {
        return new HttpCall(exchange);
    }

    /**
     * Creates a call for an exchange.
     *
     * @param exchange   source exchange
     * @param dispatcher dispatcher used by enqueue()
     * @return HTTP call
     */
    public static HttpCall create(final HttpX exchange, final Dispatcher dispatcher) {
        return new HttpCall(exchange, require(dispatcher, "Dispatcher"));
    }

    /**
     * Executes this call synchronously.
     *
     * @return response
     */
    @Override
    public HttpResponse execute() {
        start();
        try {
            cancellation.throwIfCancelled();
            final HttpResponse response = exchange.execute(cancellation);
            complete(response);
            return response;
        } catch (final CancellationException e) {
            cancel();
            throw e;
        } catch (final RuntimeException e) {
            fail(e);
            throw e;
        }
    }

    /**
     * Enqueues this call to its configured dispatcher.
     */
    @Override
    public Call<HttpResponse> enqueue() {
        return enqueue(require(dispatcher, "Dispatcher"));
    }

    /**
     * Enqueues this call to a dispatcher.
     *
     * @param dispatcher dispatcher
     * @return this call
     */
    public Call<HttpResponse> enqueue(final Dispatcher dispatcher) {
        final Dispatcher target = require(dispatcher, "Dispatcher");
        if (handle.get() != null) {
            return this;
        }
        if (state.get() != Status.QUEUED) {
            throw new StatefulException("HTTP call cannot be enqueued from state " + state.get());
        }
        final Activity activity = Activity.of("http-call", () -> {
            try {
                execute();
            } catch (final CancellationException e) {
                cancel();
                throw e;
            } catch (final RuntimeException e) {
                fail(e);
                throw e;
            }
        }, cancellation);
        final DispatchHandle enqueued = target.enqueue(dispatchKey(), activity);
        if (!handle.compareAndSet(null, enqueued)) {
            target.cancel(enqueued);
        } else {
            Logger.info(
                    false,
                    LOG_TAG,
                    "HTTP call enqueued: method={}, scheme={}, host={}, port={}, path={}",
                    request.method().value(),
                    request.url().scheme(),
                    request.url().host(),
                    request.url().port(),
                    request.url().path());
        }
        return this;
    }

    /**
     * Waits for this call to complete.
     *
     * @return response
     */
    @Override
    public HttpResponse await() {
        startIfNeeded();
        return awaitFuture(future);
    }

    /**
     * Waits for this call to complete within a timeout.
     *
     * @param timeout timeout
     * @return response
     */
    @Override
    public HttpResponse await(final Duration timeout) {
        validateTimeout(timeout);
        startIfNeeded();
        return awaitFuture(future, timeout);
    }

    /**
     * Cancels this call.
     *
     * @return true when this invocation changed the call state
     */
    @Override
    public boolean cancel() {
        while (true) {
            final Status current = state.get();
            if (current == Status.CANCELLED || current == Status.DONE || current == Status.FAILED
                    || current == Status.CLOSED) {
                return false;
            }
            if (current == Status.QUEUED || current == Status.RUNNING || current == Status.OPENED) {
                if (state.compareAndSet(current, Status.CANCELLED)) {
                    cancellation.cancel(new CancellationException("HTTP call cancelled"));
                    final DispatchHandle currentHandle = handle.get();
                    if (currentHandle != null) {
                        currentHandle.cancel();
                    }
                    future.cancel(false);
                    Logger.info(
                            false,
                            LOG_TAG,
                            "HTTP call cancelled: method={}, scheme={}, host={}, port={}, path={}",
                            request.method().value(),
                            request.url().scheme(),
                            request.url().host(),
                            request.url().port(),
                            request.url().path());
                    return true;
                }
            } else {
                throw new StatefulException("HTTP call cannot cancel from state " + current);
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
     * Returns whether this call has reached a terminal state.
     *
     * @return true when terminal
     */
    @Override
    public boolean done() {
        final Status current = state.get();
        return future.isDone() || current == Status.DONE || current == Status.FAILED || current == Status.CANCELLED;
    }

    /**
     * Returns the request snapshot.
     *
     * @return request
     */
    public HttpRequest request() {
        return request;
    }

    /**
     * Marks the call running.
     */
    private void start() {
        if (!state.compareAndSet(Status.QUEUED, Status.RUNNING)) {
            throw new StatefulException("HTTP call can only execute once");
        }
        Logger.info(
                true,
                LOG_TAG,
                "HTTP call started: method={}, scheme={}, host={}, port={}, path={}",
                request.method().value(),
                request.url().scheme(),
                request.url().host(),
                request.url().port(),
                request.url().path());
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
     * Builds a stable async dispatch key for this request authority.
     *
     * @return dispatch key
     */
    private String dispatchKey() {
        final Address address = request.url().address();
        return address.scheme() + Symbol.COLON + Symbol.FORWARDSLASH + address.host() + Symbol.C_COLON + address.port();
    }

    /**
     * Completes the call successfully.
     *
     * @param response response
     */
    private void complete(final HttpResponse response) {
        if (state.compareAndSet(Status.RUNNING, Status.DONE)) {
            future.complete(response);
            Logger.info(
                    false,
                    LOG_TAG,
                    "HTTP call completed: method={}, scheme={}, host={}, port={}, path={}, code={}",
                    request.method().value(),
                    request.url().scheme(),
                    request.url().host(),
                    request.url().port(),
                    request.url().path(),
                    response.code());
        } else if (state.get() == Status.CANCELLED) {
            response.close();
        } else {
            throw new StatefulException("HTTP call cannot complete from state " + state.get());
        }
    }

    /**
     * Completes the call with a failure.
     *
     * @param cause cause
     */
    private void fail(final RuntimeException cause) {
        if (state.get() == Status.CANCELLED) {
            return;
        }
        state.set(Status.FAILED);
        future.completeExceptionally(cause);
        Logger.error(
                false,
                LOG_TAG,
                cause,
                "HTTP call failed: method={}, scheme={}, host={}, port={}, path={}, exception={}",
                request.method().value(),
                request.url().scheme(),
                request.url().host(),
                request.url().port(),
                request.url().path(),
                cause.getClass().getSimpleName());
    }

    /**
     * Waits for a future to complete.
     *
     * @param future future
     * @return completed value
     */
    private static HttpResponse awaitFuture(final CompletableFuture<HttpResponse> future) {
        try {
            return future.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for HTTP call", e);
        } catch (final ExecutionException e) {
            throw new InternalException("HTTP call failed", e.getCause());
        } catch (final CancellationException e) {
            throw new InternalException("HTTP call was cancelled", e);
        }
    }

    /**
     * Waits for a future to complete within a timeout.
     *
     * @param future  future
     * @param timeout timeout
     * @return completed value
     */
    private HttpResponse awaitFuture(final CompletableFuture<HttpResponse> future, final Duration timeout) {
        if (timeout.isZero()) {
            if (!future.isDone()) {
                cancel();
                throw new TimeoutException("HTTP call timed out");
            }
            return awaitFuture(future);
        }
        try {
            return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for HTTP call", e);
        } catch (final ExecutionException e) {
            throw new InternalException("HTTP call failed", e.getCause());
        } catch (final CancellationException e) {
            throw new InternalException("HTTP call was cancelled", e);
        } catch (final java.util.concurrent.TimeoutException e) {
            cancel();
            throw new TimeoutException("HTTP call timed out", e);
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
