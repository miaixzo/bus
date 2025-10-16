/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http;

import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.accord.ConnectInterceptor;
import org.miaixz.bus.http.accord.Transmitter;
import org.miaixz.bus.http.cache.CacheInterceptor;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.NamedRunnable;
import org.miaixz.bus.http.metric.NewChain;
import org.miaixz.bus.http.metric.http.BridgeInterceptor;
import org.miaixz.bus.http.metric.http.CallServerInterceptor;
import org.miaixz.bus.http.metric.http.RealInterceptorChain;
import org.miaixz.bus.http.metric.http.RetryAndFollowUp;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The concrete implementation of an HTTP call.
 * <p>
 * This class is responsible for executing synchronous and asynchronous HTTP requests by processing them through an
 * interceptor chain. It supports WebSocket connections, request cancellation, timeout management, and retry mechanisms.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class RealCall implements NewCall {

    /**
     * The HTTP client that created this call.
     */
    public final Httpd client;
    /**
     * The original, unmodified request that initiated this call.
     */
    public final Request originalRequest;
    /**
     * True if this call is for a WebSocket connection.
     */
    public final boolean forWebSocket;
    /**
     * The transmitter for this call, which manages the connection and exchange.
     */
    public Transmitter transmitter;
    /**
     * True if this call has been executed.
     */
    public boolean executed;

    private boolean timeoutEarlyExit;

    /**
     * Constructs a new {@code RealCall} instance.
     *
     * @param client          The HTTP client.
     * @param originalRequest The original request.
     * @param forWebSocket    Whether this call is for a WebSocket.
     */
    private RealCall(Httpd client, Request originalRequest, boolean forWebSocket) {
        this.client = client;
        this.originalRequest = originalRequest;
        this.forWebSocket = forWebSocket;
        this.timeoutEarlyExit = false;
    }

    /**
     * Creates a new {@code RealCall} instance.
     *
     * @param client          The HTTP client.
     * @param originalRequest The original request.
     * @param forWebSocket    Whether this call is for a WebSocket.
     * @return A new {@code RealCall} instance.
     */
    static RealCall newRealCall(Httpd client, Request originalRequest, boolean forWebSocket) {
        RealCall call = new RealCall(client, originalRequest, forWebSocket);
        call.transmitter = new Transmitter(client, call);
        return call;
    }

    /**
     * Returns the original request that initiated this call.
     *
     * @return The original request.
     */
    @Override
    public Request request() {
        return originalRequest;
    }

    /**
     * Executes the request synchronously.
     *
     * @return The response from the server.
     * @throws IOException           if the request fails to execute.
     * @throws IllegalStateException if the call has already been executed.
     */
    @Override
    public Response execute() throws IOException {
        synchronized (this) {
            if (executed)
                throw new IllegalStateException("Already Executed");
            executed = true;
        }
        transmitter.timeoutEnter();
        transmitter.callStart();
        try {
            client.dispatcher().executed(this);
            return getResponseWithInterceptorChain();
        } finally {
            client.dispatcher().finished(this);
        }
    }

    /**
     * Executes the request asynchronously.
     *
     * @param responseCallback The callback to be invoked with the response or failure.
     * @throws IllegalStateException if the call has already been executed.
     */
    @Override
    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (executed)
                throw new IllegalStateException("Already Executed");
            executed = true;
        }
        transmitter.callStart();
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }

    /**
     * Cancels the call.
     */
    @Override
    public void cancel() {
        transmitter.cancel();
    }

    /**
     * Returns the timeout configuration for this call.
     *
     * @return The timeout configuration.
     */
    @Override
    public Timeout timeout() {
        return transmitter.timeout();
    }

    /**
     * Marks this call for an early exit due to a timeout.
     */
    public void timeoutEarlyExit() {
        timeoutEarlyExit = true;
    }

    /**
     * Returns whether this call has been executed.
     *
     * @return {@code true} if this call has been executed.
     */
    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    /**
     * Returns whether this call has been canceled.
     *
     * @return {@code true} if this call has been canceled.
     */
    @Override
    public boolean isCanceled() {
        return transmitter.isCanceled();
    }

    /**
     * Creates a new, identical call to this one.
     *
     * @return A new {@code RealCall} instance.
     */
    @Override
    public RealCall clone() {
        return RealCall.newRealCall(client, originalRequest, forWebSocket);
    }

    /**
     * Returns a loggable string representation of this call.
     *
     * @return A string describing the call, without the full URL.
     */
    public String toLoggableString() {
        return (isCanceled() ? "canceled " : Normal.EMPTY) + (forWebSocket ? "web socket" : "call") + " to "
                + redactedUrl();
    }

    /**
     * Returns the URL with sensitive information redacted.
     *
     * @return The redacted URL.
     */
    public String redactedUrl() {
        return originalRequest.url().redact();
    }

    /**
     * Gets the response by processing the request through the interceptor chain.
     *
     * @return The response.
     * @throws IOException if an I/O error occurs.
     */
    public Response getResponseWithInterceptorChain() throws IOException {
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(client.interceptors());
        interceptors.add(new RetryAndFollowUp(client));
        interceptors.add(new BridgeInterceptor(client.cookieJar()));
        interceptors.add(new CacheInterceptor(client.internalCache()));
        interceptors.add(new ConnectInterceptor(client));
        if (!forWebSocket) {
            interceptors.addAll(client.networkInterceptors());
        }
        interceptors.add(new CallServerInterceptor(forWebSocket));

        NewChain chain = new RealInterceptorChain(interceptors, transmitter, null, 0, originalRequest, this,
                client.connectTimeoutMillis(), client.readTimeoutMillis(), client.writeTimeoutMillis());

        boolean calledNoMoreExchanges = false;
        try {
            Response response = chain.proceed(originalRequest);
            if (transmitter.isCanceled()) {
                IoKit.close(response);
                throw new IOException("Canceled");
            }
            return response;
        } catch (IOException e) {
            calledNoMoreExchanges = true;
            throw transmitter.noMoreExchanges(e);
        } finally {
            if (!calledNoMoreExchanges) {
                transmitter.noMoreExchanges(null);
            }
        }
    }

    /**
     * An asynchronous call that can be executed on a background thread.
     */
    public final class AsyncCall extends NamedRunnable {

        /**
         * The callback for the response.
         */
        private final Callback responseCallback;
        /**
         * The number of calls per host.
         */
        private volatile AtomicInteger callsPerHost = new AtomicInteger(0);

        /**
         * Constructs a new asynchronous call.
         *
         * @param responseCallback The response callback.
         */
        AsyncCall(Callback responseCallback) {
            super("Http %s", redactedUrl());
            this.responseCallback = responseCallback;
        }

        /**
         * Returns the number of calls per host.
         *
         * @return The number of calls per host.
         */
        public AtomicInteger callsPerHost() {
            return callsPerHost;
        }

        /**
         * Reuses the host call count from another asynchronous call.
         *
         * @param other The other asynchronous call.
         */
        public void reuseCallsPerHostFrom(AsyncCall other) {
            this.callsPerHost = other.callsPerHost;
        }

        /**
         * Returns the hostname for this call.
         *
         * @return The hostname.
         */
        public String host() {
            return originalRequest.url().host();
        }

        /**
         * Returns the request for this call.
         *
         * @return The request.
         */
        Request request() {
            return originalRequest;
        }

        /**
         * Returns the {@code RealCall} instance for this asynchronous call.
         *
         * @return The {@code RealCall} instance.
         */
        public RealCall get() {
            return RealCall.this;
        }

        /**
         * Executes this asynchronous call on the given executor service.
         *
         * @param executorService The executor service.
         */
        public void executeOn(ExecutorService executorService) {
            assert (!Thread.holdsLock(client.dispatcher()));
            boolean success = false;
            try {
                executorService.execute(this);
                success = true;
            } catch (RejectedExecutionException e) {
                InterruptedIOException ioException = new InterruptedIOException("executor rejected");
                ioException.initCause(e);
                transmitter.noMoreExchanges(ioException);
                responseCallback.onFailure(RealCall.this, ioException);
            } finally {
                if (!success) {
                    client.dispatcher().finished(RealCall.this); // Clean up.
                }
            }
        }

        /**
         * Executes the asynchronous call.
         */
        @Override
        protected void execute() {
            boolean signalledCallback = false;
            transmitter.timeoutEnter();
            try {
                Response response = getResponseWithInterceptorChain();
                signalledCallback = true;
                responseCallback.onResponse(RealCall.this, response);
            } catch (IOException e) {
                if (signalledCallback) {
                    // Do not signal the callback twice.
                    Logger.info("Callback failure for " + toLoggableString(), e);
                } else {
                    responseCallback.onFailure(RealCall.this, e);
                }
            } catch (Throwable t) {
                cancel(); // Cancel the call on any unexpected error.
                if (!signalledCallback) {
                    IOException canceledException = new IOException("canceled due to " + t);
                    canceledException.addSuppressed(t);
                    responseCallback.onFailure(RealCall.this, canceledException);
                }
                throw t;
            } finally {
                client.dispatcher().finished(RealCall.this);
            }
        }
    }

}
