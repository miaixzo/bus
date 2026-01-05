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
package org.miaixz.bus.http.accord;

import org.miaixz.bus.core.io.timout.AsyncTimeout;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.platform.Platform;
import org.miaixz.bus.http.metric.EventListener;
import org.miaixz.bus.http.metric.Internal;
import org.miaixz.bus.http.metric.NewChain;
import org.miaixz.bus.http.metric.http.HttpCodec;
import org.miaixz.bus.http.secure.CertificatePinner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Bridges the application layer with the network layer, managing the lifecycle of a single call. This class exposes
 * connections, requests, responses, and streams.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Transmitter {

    private final Httpd client;
    private final RealConnectionPool connectionPool;
    private final NewCall call;
    private final EventListener eventListener;
    /**
     * The connection that carries the request and response. Guarded by connectionPool.
     */
    public RealConnection connection;
    private Object callStackTrace;
    private Request request;
    private ExchangeFinder exchangeFinder;
    /**
     * The exchange that is currently in progress. Guarded by connectionPool.
     */
    private Exchange exchange;
    private boolean exchangeRequestDone;
    private boolean exchangeResponseDone;
    private boolean canceled;
    private final AsyncTimeout timeout = new AsyncTimeout() {

        @Override
        protected void timedOut() {
            cancel();
        }
    };
    private boolean timeoutEarlyExit;
    private boolean noMoreExchanges;

    public Transmitter(Httpd client, NewCall call) {
        this.client = client;
        this.connectionPool = Internal.instance.realConnectionPool(client.connectionPool());
        this.call = call;
        this.eventListener = client.eventListenerFactory().of(call);
        this.timeout.timeout(client.callTimeoutMillis(), TimeUnit.MILLISECONDS);
    }

    public Timeout timeout() {
        return timeout;
    }

    public void timeoutEnter() {
        timeout.enter();
    }

    /**
     * Stops applying the timeout before the call is complete. This is used for WebSockets and duplex calls where the
     * timeout only applies to the initial setup.
     */
    public void timeoutEarlyExit() {
        if (timeoutEarlyExit)
            throw new IllegalStateException();
        timeoutEarlyExit = true;
        timeout.exit();
    }

    private IOException timeoutExit(IOException cause) {
        if (timeoutEarlyExit)
            return cause;
        if (!timeout.exit())
            return cause;

        InterruptedIOException e = new InterruptedIOException("timeout");
        if (cause != null)
            e.initCause(cause);

        return e;
    }

    /**
     * Records the stack trace at the point the call is started. This is used to identify the source of connection
     * leaks.
     */
    public void callStart() {
        this.callStackTrace = Platform.get().getStackTraceForCloseable("response.body().close()");
        eventListener.callStart(call);
    }

    /**
     * Prepare to create a stream to carry {@code request}. This prefers to use an existing connection if one exists.
     *
     * @param request The request to prepare for.
     */
    public void prepareToConnect(Request request) {
        if (this.request != null) {
            if (Builder.sameConnection(this.request.url(), request.url()) && exchangeFinder.hasRouteToTry()) {
                return; // Already ready.
            }
            if (exchange != null)
                throw new IllegalStateException();

            if (exchangeFinder != null) {
                maybeReleaseConnection(null, true);
                exchangeFinder = null;
            }
        }

        this.request = request;
        this.exchangeFinder = new ExchangeFinder(this, connectionPool, createAddress(request.url()), call,
                eventListener);
    }

    private Address createAddress(UnoUrl url) {
        SSLSocketFactory sslSocketFactory = null;
        HostnameVerifier hostnameVerifier = null;
        CertificatePinner certificatePinner = null;
        if (url.isHttps()) {
            sslSocketFactory = client.sslSocketFactory();
            hostnameVerifier = client.hostnameVerifier();
            certificatePinner = client.certificatePinner();
        }

        return new Address(url.host(), url.port(), client.dns(), client.socketFactory(), sslSocketFactory,
                hostnameVerifier, certificatePinner, client.proxyAuthenticator(), client.proxy(), client.protocols(),
                client.connectionSpecs(), client.proxySelector());
    }

    /**
     * Returns a new exchange to carry a new request and response.
     *
     * @param chain                   The interceptor chain.
     * @param doExtensiveHealthChecks Whether to perform extensive health checks on the connection.
     * @return A new exchange.
     */
    Exchange newExchange(NewChain chain, boolean doExtensiveHealthChecks) {
        synchronized (connectionPool) {
            if (noMoreExchanges) {
                throw new IllegalStateException("released");
            }
            if (exchange != null) {
                throw new IllegalStateException("cannot make a new request because the previous response "
                        + "is still open: please call response.close()");
            }
        }

        HttpCodec codec = exchangeFinder.find(client, chain, doExtensiveHealthChecks);
        Exchange result = new Exchange(this, call, eventListener, exchangeFinder, codec);

        synchronized (connectionPool) {
            this.exchange = result;
            this.exchangeRequestDone = false;
            this.exchangeResponseDone = false;
            return result;
        }
    }

    /**
     * Acquires a connection without firing any events.
     *
     * @param connection The connection to acquire.
     */
    void acquireConnectionNoEvents(RealConnection connection) {
        assert (Thread.holdsLock(connectionPool));

        if (this.connection != null)
            throw new IllegalStateException();
        this.connection = connection;
        connection.transmitters.add(new TransmitterReference(this, callStackTrace));
    }

    /**
     * Remove the transmitter from the connection's list of allocations. Returns a socket that the caller should close.
     *
     * @return The socket to close, or null if no socket should be closed.
     */
    Socket releaseConnectionNoEvents() {
        assert (Thread.holdsLock(connectionPool));

        int index = -1;
        for (int i = 0, size = this.connection.transmitters.size(); i < size; i++) {
            Reference<Transmitter> reference = this.connection.transmitters.get(i);
            if (reference.get() == this) {
                index = i;
                break;
            }
        }

        if (index == -1)
            throw new IllegalStateException();

        RealConnection released = this.connection;
        released.transmitters.remove(index);
        this.connection = null;

        if (released.transmitters.isEmpty()) {
            released.idleAtNanos = System.nanoTime();
            if (connectionPool.connectionBecameIdle(released)) {
                return released.socket();
            }
        }

        return null;
    }

    /**
     * Notifies this transmitter that the exchange has completed due to an exception.
     */
    public void exchangeDoneDueToException() {
        synchronized (connectionPool) {
            if (noMoreExchanges)
                throw new IllegalStateException();
            exchange = null;
        }
    }

    /**
     * Releases resources held with the request or response of {@code exchange}. This should be called when the request
     * completes normally or when it fails due to an exception, in which case {@code e} should be non-null. If the
     * exchange was canceled or timed out, this will wrap {@code e} in an exception that provides that additional
     * context. Otherwise {@code e} is returned as-is.
     *
     * @param exchange     The exchange that has completed.
     * @param requestDone  Whether the request is done.
     * @param responseDone Whether the response is done.
     * @param e            The exception that occurred, or null if none.
     * @return The exception, or null if none.
     */
    IOException exchangeMessageDone(Exchange exchange, boolean requestDone, boolean responseDone, IOException e) {
        boolean exchangeDone = false;
        synchronized (connectionPool) {
            if (exchange != this.exchange) {
                return e; // This exchange was detached violently!
            }
            boolean changed = false;
            if (requestDone) {
                if (!exchangeRequestDone)
                    changed = true;
                this.exchangeRequestDone = true;
            }
            if (responseDone) {
                if (!exchangeResponseDone)
                    changed = true;
                this.exchangeResponseDone = true;
            }
            if (exchangeRequestDone && exchangeResponseDone && changed) {
                exchangeDone = true;
                this.exchange.connection().successCount++;
                this.exchange = null;
            }
        }
        if (exchangeDone) {
            e = maybeReleaseConnection(e, false);
        }
        return e;
    }

    /**
     * Notifies this transmitter that no more exchanges are expected.
     *
     * @param e The exception that occurred, or null if none.
     * @return The exception, or null if none.
     */
    public IOException noMoreExchanges(IOException e) {
        synchronized (connectionPool) {
            noMoreExchanges = true;
        }
        return maybeReleaseConnection(e, false);
    }

    /**
     * Release the connection if it is no longer needed. This is called after each exchange completes and after the call
     * signals that no more exchanges are expected. If the transmitter was canceled or timed out, this will wrap
     * {@code e} in an exception that provides that additional context. Otherwise {@code e} is returned as-is.
     *
     * @param e     The exception that occurred, or null if none.
     * @param force true to release the connection even if more exchanges are expected for the call.
     * @return The exception, or null if none.
     */
    private IOException maybeReleaseConnection(IOException e, boolean force) {
        Socket socket;
        Connection releasedConnection;
        boolean callEnd;
        synchronized (connectionPool) {
            if (force && exchange != null) {
                throw new IllegalStateException("cannot release connection while it is in use");
            }
            releasedConnection = this.connection;
            socket = this.connection != null && exchange == null && (force || noMoreExchanges)
                    ? releaseConnectionNoEvents()
                    : null;
            if (this.connection != null)
                releasedConnection = null;
            callEnd = noMoreExchanges && exchange == null;
        }
        IoKit.close(socket);

        if (releasedConnection != null) {
            eventListener.connectionReleased(call, releasedConnection);
        }

        if (callEnd) {
            boolean callFailed = (e != null);
            e = timeoutExit(e);
            if (callFailed) {
                eventListener.callFailed(call, e);
            } else {
                eventListener.callEnd(call);
            }
        }
        return e;
    }

    /**
     * Returns true if this transmitter can retry a failed request.
     *
     * @return {@code true} if a retry is possible.
     */
    public boolean canRetry() {
        return exchangeFinder.hasStreamFailure() && exchangeFinder.hasRouteToTry();
    }

    /**
     * Returns true if this transmitter has an active exchange.
     *
     * @return {@code true} if there is an active exchange.
     */
    public boolean hasExchange() {
        synchronized (connectionPool) {
            return exchange != null;
        }
    }

    /**
     * Immediately closes the socket connection if it's currently held. Use this to interrupt an in-flight request from
     * any thread. It's the caller's responsibility to close the request body and response body streams; otherwise
     * resources may be leaked. This method is safe to be called concurrently, but provides limited guarantees. If a
     * transport layer connection has been established (such as a HTTP/2 stream) that is terminated. Otherwise if a
     * socket connection is being established, that is terminated.
     */
    public void cancel() {
        Exchange exchangeToCancel;
        RealConnection connectionToCancel;
        synchronized (connectionPool) {
            canceled = true;
            exchangeToCancel = exchange;
            connectionToCancel = exchangeFinder != null && exchangeFinder.connectingConnection() != null
                    ? exchangeFinder.connectingConnection()
                    : connection;
        }
        if (exchangeToCancel != null) {
            exchangeToCancel.cancel();
        } else if (connectionToCancel != null) {
            connectionToCancel.cancel();
        }
    }

    /**
     * Returns true if this transmitter has been canceled.
     *
     * @return {@code true} if this transmitter has been canceled.
     */
    public boolean isCanceled() {
        synchronized (connectionPool) {
            return canceled;
        }
    }

    /**
     * A weak reference to a transmitter, used to detect connection leaks.
     */
    static final class TransmitterReference extends WeakReference<Transmitter> {

        /**
         * The stack trace at the time the call was executed or enqueued. This is used to identify the source of
         * connection leaks.
         */
        final Object callStackTrace;

        TransmitterReference(Transmitter referent, Object callStackTrace) {
            super(referent);
            this.callStackTrace = callStackTrace;
        }
    }

}
