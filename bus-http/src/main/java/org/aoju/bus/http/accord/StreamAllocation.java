/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.accord;

import org.aoju.bus.http.*;
import org.aoju.bus.http.internal.http.HttpCodec;
import org.aoju.bus.http.internal.http.second.ErrorCode;
import org.aoju.bus.http.internal.http.second.StreamResetException;
import org.aoju.bus.http.offers.EventListener;
import org.aoju.bus.http.offers.Interceptor;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.List;

/**
 * This class coordinates the relationship between three entities:
 *
 * <ul>
 * <li><strong>Connections:</strong> physical socket connections to remote servers. These are
 * potentially slow to establish so it is necessary to be able to cancel a connection
 * currently being connected.
 * <li><strong>Streams:</strong> logical HTTP request/response pairs that are layered on
 * connections. Each connection has its own allocation limit, which defines how many
 * concurrent streams that connection can carry. HTTP/1.x connections can carry 1 stream
 * at a time, HTTP/2 typically carry multiple.
 * <li><strong>Calls:</strong> a logical sequence of streams, typically an initial request and
 * its follow up requests. We prefer to keep all streams of a single call on the same
 * connection for better behavior and locality.
 * </ul>
 *
 * <p>Instances of this class act on behalf of the call, using first or more streams over first or more
 * connections. This class has APIs to release each of the above resources:
 *
 * <ul>
 * <li>{@link #noNewStreams()} prevents the connection from being used for new streams in the
 * future. Use this after a {@code Connection: close} header, or when the connection may be
 * inconsistent.
 * <li>{@link #streamFinished streamFinished()} releases the active stream from this allocation.
 * Note that only first stream may be active at a given time, so it is necessary to call
 * {@link #streamFinished streamFinished()} before creating a subsequent stream with {@link
 * #newStream newStream()}.
 * <li>{@link #release()} removes the call's hold on the connection. Note that this won't
 * immediately free the connection if there is a stream still lingering. That happens when a
 * call is complete but its response body has yet to be fully consumed.
 * </ul>
 *
 * <p>This class supports {@linkplain #cancel asynchronous canceling}. This is intended to have the
 * smallest blast radius possible. If an HTTP/2 stream is active, canceling will cancel that stream
 * but not the other streams sharing its connection. But if the TLS handshake is still in progress
 * then canceling may break the entire connection.
 *
 * @author Kimi Liu
 * @version 5.3.1
 * @since JDK 1.8+
 */
public final class StreamAllocation {

    public final Address address;
    public final Call call;
    public final EventListener eventListener;
    private final ConnectionPool connectionPool;
    private final Object callStackTrace;
    private final RouteSelector routeSelector;
    private RouteSelector.Selection routeSelection;
    private Route route;
    private int refusedStreamCount;
    private RealConnection connection;
    private boolean reportedAcquired;
    private boolean released;
    private boolean canceled;
    private HttpCodec codec;

    public StreamAllocation(ConnectionPool connectionPool, Address address, Call call,
                            EventListener eventListener, Object callStackTrace) {
        this.connectionPool = connectionPool;
        this.address = address;
        this.call = call;
        this.eventListener = eventListener;
        this.routeSelector = new RouteSelector(address, routeDatabase(), call, eventListener);
        this.callStackTrace = callStackTrace;
    }

    public HttpCodec newStream(
            Client client, Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
        int connectTimeout = chain.connectTimeoutMillis();
        int readTimeout = chain.readTimeoutMillis();
        int writeTimeout = chain.writeTimeoutMillis();
        int pingIntervalMillis = client.pingIntervalMillis();
        boolean connectionRetryEnabled = client.retryOnConnectionFailure();

        try {
            RealConnection resultConnection = findHealthyConnection(connectTimeout, readTimeout,
                    writeTimeout, pingIntervalMillis, connectionRetryEnabled, doExtensiveHealthChecks);
            HttpCodec resultCodec = resultConnection.newCodec(client, chain, this);

            synchronized (connectionPool) {
                codec = resultCodec;
                return resultCodec;
            }
        } catch (IOException e) {
            throw new RouteException(e);
        }
    }

    private RealConnection findHealthyConnection(int connectTimeout, int readTimeout,
                                                 int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled,
                                                 boolean doExtensiveHealthChecks) throws IOException {
        while (true) {
            RealConnection candidate = findConnection(connectTimeout, readTimeout, writeTimeout,
                    pingIntervalMillis, connectionRetryEnabled);

            // If this is a brand new connection, we can skip the extensive health checks.
            synchronized (connectionPool) {
                if (candidate.successCount == 0) {
                    return candidate;
                }
            }

            // Do a (potentially slow) check to confirm that the pooled connection is still good. If it
            // isn't, take it out of the pool and start again.
            if (!candidate.isHealthy(doExtensiveHealthChecks)) {
                noNewStreams();
                continue;
            }

            return candidate;
        }
    }

    private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout,
                                          int pingIntervalMillis, boolean connectionRetryEnabled) throws IOException {
        boolean foundPooledConnection = false;
        RealConnection result = null;
        Route selectedRoute = null;
        Connection releasedConnection;
        Socket toClose;
        synchronized (connectionPool) {
            if (released) throw new IllegalStateException("released");
            if (codec != null) throw new IllegalStateException("codec != null");
            if (canceled) throw new IOException("Canceled");

            // Attempt to use an already-allocated connection. We need to be careful here because our
            // already-allocated connection may have been restricted from creating new streams.
            releasedConnection = this.connection;
            toClose = releaseIfNoNewStreams();
            if (this.connection != null) {
                // We had an already-allocated connection and it's good.
                result = this.connection;
                releasedConnection = null;
            }
            if (!reportedAcquired) {
                // If the connection was never reported acquired, don't report it as released!
                releasedConnection = null;
            }

            if (result == null) {
                // Attempt to get a connection from the pool.
                Internal.instance.get(connectionPool, address, this, null);
                if (connection != null) {
                    foundPooledConnection = true;
                    result = connection;
                } else {
                    selectedRoute = route;
                }
            }
        }
        Internal.closeQuietly(toClose);

        if (releasedConnection != null) {
            eventListener.connectionReleased(call, releasedConnection);
        }
        if (foundPooledConnection) {
            eventListener.connectionAcquired(call, result);
        }
        if (result != null) {
            // If we found an already-allocated or pooled connection, we're done.
            return result;
        }

        // If we need a route selection, make first. This is a blocking operation.
        boolean newRouteSelection = false;
        if (selectedRoute == null && (routeSelection == null || !routeSelection.hasNext())) {
            newRouteSelection = true;
            routeSelection = routeSelector.next();
        }

        synchronized (connectionPool) {
            if (canceled) throw new IOException("Canceled");

            if (newRouteSelection) {
                // Now that we have a set of IP addresses, make another attempt at getting a connection from
                // the pool. This could match due to connection coalescing.
                List<Route> routes = routeSelection.getAll();
                for (int i = 0, size = routes.size(); i < size; i++) {
                    Route route = routes.get(i);
                    Internal.instance.get(connectionPool, address, this, route);
                    if (connection != null) {
                        foundPooledConnection = true;
                        result = connection;
                        this.route = route;
                        break;
                    }
                }
            }

            if (!foundPooledConnection) {
                if (selectedRoute == null) {
                    selectedRoute = routeSelection.next();
                }

                // Create a connection and assign it to this allocation immediately. This makes it possible
                // for an asynchronous cancel() to interrupt the handshake we're about to do.
                route = selectedRoute;
                refusedStreamCount = 0;
                result = new RealConnection(connectionPool, selectedRoute);
                acquire(result, false);
            }
        }

        // If we found a pooled connection on the 2nd time around, we're done.
        if (foundPooledConnection) {
            eventListener.connectionAcquired(call, result);
            return result;
        }

        // Do TCP + TLS handshakes. This is a blocking operation.
        result.connect(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis,
                connectionRetryEnabled, call, eventListener);
        routeDatabase().connected(result.route());

        Socket socket = null;
        synchronized (connectionPool) {
            reportedAcquired = true;

            // Pool the connection.
            Internal.instance.put(connectionPool, result);

            // If another multiplexed connection to the same address was created concurrently, then
            // release this connection and acquire that first.
            if (result.isMultiplexed()) {
                socket = Internal.instance.deduplicate(connectionPool, address, this);
                result = connection;
            }
        }
        Internal.closeQuietly(socket);

        eventListener.connectionAcquired(call, result);
        return result;
    }

    /**
     * Releases the currently held connection and returns a socket to close if the held connection
     * restricts new streams from being created. With HTTP/2 multiple requests share the same
     * connection so it's possible that our connection is restricted from creating new streams during
     * a follow-up request.
     */
    private Socket releaseIfNoNewStreams() {
        assert (Thread.holdsLock(connectionPool));
        RealConnection allocatedConnection = this.connection;
        if (allocatedConnection != null && allocatedConnection.noNewStreams) {
            return deallocate(false, false, true);
        }
        return null;
    }

    public void streamFinished(boolean noNewStreams, HttpCodec codec, long bytesRead, IOException e) {
        eventListener.responseBodyEnd(call, bytesRead);

        Socket socket;
        Connection releasedConnection;
        boolean callEnd;
        synchronized (connectionPool) {
            if (codec == null || codec != this.codec) {
                throw new IllegalStateException("expected " + this.codec + " but was " + codec);
            }
            if (!noNewStreams) {
                connection.successCount++;
            }
            releasedConnection = connection;
            socket = deallocate(noNewStreams, false, true);
            if (connection != null) releasedConnection = null;
            callEnd = this.released;
        }
        Internal.closeQuietly(socket);
        if (releasedConnection != null) {
            eventListener.connectionReleased(call, releasedConnection);
        }

        if (e != null) {
            e = Internal.instance.timeoutExit(call, e);
            eventListener.callFailed(call, e);
        } else if (callEnd) {
            Internal.instance.timeoutExit(call, null);
            eventListener.callEnd(call);
        }
    }

    public HttpCodec codec() {
        synchronized (connectionPool) {
            return codec;
        }
    }

    private RouteDatabase routeDatabase() {
        return Internal.instance.routeDatabase(connectionPool);
    }

    public Route route() {
        return route;
    }

    public synchronized RealConnection connection() {
        return connection;
    }

    public void release() {
        Socket socket;
        Connection releasedConnection;
        synchronized (connectionPool) {
            releasedConnection = connection;
            socket = deallocate(false, true, false);
            if (connection != null) releasedConnection = null;
        }
        Internal.closeQuietly(socket);
        if (releasedConnection != null) {
            Internal.instance.timeoutExit(call, null);
            eventListener.connectionReleased(call, releasedConnection);
            eventListener.callEnd(call);
        }
    }

    public void noNewStreams() {
        Socket socket;
        Connection releasedConnection;
        synchronized (connectionPool) {
            releasedConnection = connection;
            socket = deallocate(true, false, false);
            if (connection != null) releasedConnection = null;
        }
        Internal.closeQuietly(socket);
        if (releasedConnection != null) {
            eventListener.connectionReleased(call, releasedConnection);
        }
    }

    private Socket deallocate(boolean noNewStreams, boolean released, boolean streamFinished) {
        assert (Thread.holdsLock(connectionPool));

        if (streamFinished) {
            this.codec = null;
        }
        if (released) {
            this.released = true;
        }
        Socket socket = null;
        if (connection != null) {
            if (noNewStreams) {
                connection.noNewStreams = true;
            }
            if (this.codec == null && (this.released || connection.noNewStreams)) {
                release(connection);
                if (connection.allocations.isEmpty()) {
                    connection.idleAtNanos = System.nanoTime();
                    if (Internal.instance.connectionBecameIdle(connectionPool, connection)) {
                        socket = connection.socket();
                    }
                }
                connection = null;
            }
        }
        return socket;
    }

    public void cancel() {
        HttpCodec codecToCancel;
        RealConnection connectionToCancel;
        synchronized (connectionPool) {
            canceled = true;
            codecToCancel = codec;
            connectionToCancel = connection;
        }
        if (codecToCancel != null) {
            codecToCancel.cancel();
        } else if (connectionToCancel != null) {
            connectionToCancel.cancel();
        }
    }

    public void streamFailed(IOException e) {
        Socket socket;
        Connection releasedConnection;
        boolean noNewStreams = false;

        synchronized (connectionPool) {
            if (e instanceof StreamResetException) {
                ErrorCode errorCode = ((StreamResetException) e).errorCode;
                if (errorCode == ErrorCode.REFUSED_STREAM) {
                    // Retry REFUSED_STREAM errors once on the same connection.
                    refusedStreamCount++;
                    if (refusedStreamCount > 1) {
                        noNewStreams = true;
                        route = null;
                    }
                } else if (errorCode != ErrorCode.CANCEL) {
                    // Keep the connection for CANCEL errors. Everything else wants a fresh connection.
                    noNewStreams = true;
                    route = null;
                }
            } else if (connection != null
                    && (!connection.isMultiplexed() || e instanceof IOException)) {
                noNewStreams = true;

                // If this route hasn't completed a call, avoid it for new connections.
                if (connection.successCount == 0) {
                    if (route != null && e != null) {
                        routeSelector.connectFailed(route, e);
                    }
                    route = null;
                }
            }
            releasedConnection = connection;
            socket = deallocate(noNewStreams, false, true);
            if (connection != null || !reportedAcquired) releasedConnection = null;
        }

        Internal.closeQuietly(socket);
        if (releasedConnection != null) {
            eventListener.connectionReleased(call, releasedConnection);
        }
    }

    public void acquire(RealConnection connection, boolean reportedAcquired) {
        assert (Thread.holdsLock(connectionPool));
        if (this.connection != null) throw new IllegalStateException();

        this.connection = connection;
        this.reportedAcquired = reportedAcquired;
        connection.allocations.add(new StreamAllocationReference(this, callStackTrace));
    }

    private void release(RealConnection connection) {
        for (int i = 0, size = connection.allocations.size(); i < size; i++) {
            Reference<StreamAllocation> reference = connection.allocations.get(i);
            if (reference.get() == this) {
                connection.allocations.remove(i);
                return;
            }
        }
        throw new IllegalStateException();
    }

    public Socket releaseAndAcquire(RealConnection newConnection) {
        assert (Thread.holdsLock(connectionPool));
        if (codec != null || connection.allocations.size() != 1) throw new IllegalStateException();

        // Release the old connection.
        Reference<StreamAllocation> onlyAllocation = connection.allocations.get(0);
        Socket socket = deallocate(true, false, false);

        // Acquire the new connection.
        this.connection = newConnection;
        newConnection.allocations.add(onlyAllocation);

        return socket;
    }

    public boolean hasMoreRoutes() {
        return route != null
                || (routeSelection != null && routeSelection.hasNext())
                || routeSelector.hasNext();
    }

    @Override
    public String toString() {
        RealConnection connection = connection();
        return connection != null ? connection.toString() : address.toString();
    }

    public static final class StreamAllocationReference extends WeakReference<StreamAllocation> {

        public final Object callStackTrace;

        StreamAllocationReference(StreamAllocation referent, Object callStackTrace) {
            super(referent);
            this.callStackTrace = callStackTrace;
        }
    }

}
