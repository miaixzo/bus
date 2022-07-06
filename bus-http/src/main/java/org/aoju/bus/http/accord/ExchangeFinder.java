/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.http.accord;

import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.http.*;
import org.aoju.bus.http.metric.EventListener;
import org.aoju.bus.http.metric.Interceptor;
import org.aoju.bus.http.metric.http.HttpCodec;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * 尝试找到一系列交换的连接。使用以下策略
 *
 * <ol>
 *   <li>如果当前调用已经有一个可以满足使用它的请求的连接。对初始交换及其后续使用相同的连接可能会改善局部性</li>
 *   <li>如果池中有可以满足请求的连接，则使用它。请注意，共享交换可以向不同的主机名发出请求！有关详细信息，请参阅 {@link RealConnection#isEligible}</li>
 *   <li>如果没有现有连接，请列出路由（可能需要阻止 DNS 查找）并尝试建立新连接。当发生故障时，重试迭代可用路由列表</li>
 * </ol>
 * <p>
 * 如果在 DNS、TCP 或 TLS 工作正在进行时池获得了合格的连接，则此查找器将首选池连接
 * <p>
 * 可以取消查找过程
 *
 * @author Kimi Liu
 * @since Java 17+
 */
class ExchangeFinder {

    private final Transmitter transmitter;
    private final Address address;
    private final RealConnectionPool connectionPool;
    private final NewCall call;
    private final EventListener eventListener;
    // State guarded by connectionPool.
    private final RouteSelector routeSelector;
    private RouteSelector.Selection routeSelection;
    private RealConnection connectingConnection;
    private boolean hasStreamFailure;
    private Route nextRouteToTry;

    ExchangeFinder(Transmitter transmitter, RealConnectionPool connectionPool,
                   Address address, NewCall call, EventListener eventListener) {
        this.transmitter = transmitter;
        this.connectionPool = connectionPool;
        this.address = address;
        this.call = call;
        this.eventListener = eventListener;
        this.routeSelector = new RouteSelector(
                address, connectionPool.routeDatabase, call, eventListener);
    }

    public HttpCodec find(
            Httpd client, Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
        int connectTimeout = chain.connectTimeoutMillis();
        int readTimeout = chain.readTimeoutMillis();
        int writeTimeout = chain.writeTimeoutMillis();
        int pingIntervalMillis = client.pingIntervalMillis();
        boolean connectionRetryEnabled = client.retryOnConnectionFailure();

        try {
            RealConnection resultConnection = findHealthyConnection(connectTimeout, readTimeout,
                    writeTimeout, pingIntervalMillis, connectionRetryEnabled, doExtensiveHealthChecks);
            return resultConnection.newCodec(client, chain);
        } catch (RouteException e) {
            trackFailure();
            throw e;
        } catch (IOException e) {
            trackFailure();
            throw new RouteException(e);
        }
    }

    /**
     * Finds a connection and returns it if it is healthy. If it is unhealthy the process is repeated
     * until a healthy connection is found.
     */
    private RealConnection findHealthyConnection(int connectTimeout, int readTimeout,
                                                 int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled,
                                                 boolean doExtensiveHealthChecks) throws IOException {
        while (true) {
            RealConnection candidate = findConnection(connectTimeout, readTimeout, writeTimeout,
                    pingIntervalMillis, connectionRetryEnabled);

            // If this is a brand new connection, we can skip the extensive health checks.
            synchronized (connectionPool) {
                if (candidate.successCount == 0 && !candidate.isMultiplexed()) {
                    return candidate;
                }
            }

            // Do a (potentially slow) check to confirm that the pooled connection is still good. If it
            // isn't, take it out of the pool and start again.
            if (!candidate.isHealthy(doExtensiveHealthChecks)) {
                candidate.noNewExchanges();
                continue;
            }

            return candidate;
        }
    }

    /**
     * Returns a connection to host a new stream. This prefers the existing connection if it exists,
     * then the pool, finally building a new connection.
     */
    private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout,
                                          int pingIntervalMillis, boolean connectionRetryEnabled) throws IOException {
        boolean foundPooledConnection = false;
        RealConnection result = null;
        Route selectedRoute = null;
        RealConnection releasedConnection;
        Socket toClose;
        synchronized (connectionPool) {
            if (transmitter.isCanceled()) throw new IOException("Canceled");
            hasStreamFailure = false; // This is a fresh attempt.

            // Attempt to use an already-allocated connection. We need to be careful here because our
            // already-allocated connection may have been restricted from creating new exchanges.
            releasedConnection = transmitter.connection;
            toClose = transmitter.connection != null && transmitter.connection.noNewExchanges
                    ? transmitter.releaseConnectionNoEvents()
                    : null;

            if (transmitter.connection != null) {
                // We had an already-allocated connection and it's good.
                result = transmitter.connection;
                releasedConnection = null;
            }

            if (result == null) {
                // Attempt to get a connection from the pool.
                if (connectionPool.transmitterAcquirePooledConnection(address, transmitter, null, false)) {
                    foundPooledConnection = true;
                    result = transmitter.connection;
                } else if (nextRouteToTry != null) {
                    selectedRoute = nextRouteToTry;
                    nextRouteToTry = null;
                } else if (retryCurrentRoute()) {
                    selectedRoute = transmitter.connection.route();
                }
            }
        }
        IoKit.close(toClose);

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

        // If we need a route selection, make one. This is a blocking operation.
        boolean newRouteSelection = false;
        if (selectedRoute == null && (routeSelection == null || !routeSelection.hasNext())) {
            newRouteSelection = true;
            routeSelection = routeSelector.next();
        }

        List<Route> routes = null;
        synchronized (connectionPool) {
            if (transmitter.isCanceled()) throw new IOException("Canceled");

            if (newRouteSelection) {
                // Now that we have a set of IP addresses, make another attempt at getting a connection from
                // the pool. This could match due to connection coalescing.
                routes = routeSelection.getAll();
                if (connectionPool.transmitterAcquirePooledConnection(
                        address, transmitter, routes, false)) {
                    foundPooledConnection = true;
                    result = transmitter.connection;
                }
            }

            if (!foundPooledConnection) {
                if (selectedRoute == null) {
                    selectedRoute = routeSelection.next();
                }

                // Create a connection and assign it to this allocation immediately. This makes it possible
                // for an asynchronous cancel() to interrupt the handshake we're about to do.
                result = new RealConnection(connectionPool, selectedRoute);
                connectingConnection = result;
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
        connectionPool.routeDatabase.connected(result.route());

        Socket socket = null;
        synchronized (connectionPool) {
            connectingConnection = null;
            // Last attempt at connection coalescing, which only occurs if we attempted multiple
            // concurrent connections to the same host.
            if (connectionPool.transmitterAcquirePooledConnection(address, transmitter, routes, true)) {
                // We lost the race! Close the connection we created and return the pooled connection.
                result.noNewExchanges = true;
                socket = result.socket();
                result = transmitter.connection;

                // It's possible for us to obtain a coalesced connection that is immediately unhealthy. In
                // that case we will retry the route we just successfully connected with.
                nextRouteToTry = selectedRoute;
            } else {
                connectionPool.put(result);
                transmitter.acquireConnectionNoEvents(result);
            }
        }
        IoKit.close(socket);

        eventListener.connectionAcquired(call, result);
        return result;
    }

    RealConnection connectingConnection() {
        assert (Thread.holdsLock(connectionPool));
        return connectingConnection;
    }

    void trackFailure() {
        assert (!Thread.holdsLock(connectionPool));
        synchronized (connectionPool) {
            hasStreamFailure = true; // 允许重试
        }
    }

    /**
     * 如果重试可能会修复失败，则返回 true
     */
    boolean hasStreamFailure() {
        synchronized (connectionPool) {
            return hasStreamFailure;
        }
    }

    /**
     * 如果当前路线仍然很好，或者如果有我们还没有尝试过的路线，则返回 true
     *
     * @return the boolean
     */
    boolean hasRouteToTry() {
        synchronized (connectionPool) {
            if (nextRouteToTry != null) {
                return true;
            }
            if (retryCurrentRoute()) {
                // 锁定路线，因为 retryCurrentRoute() 是活泼的，我们不想调用它两次
                nextRouteToTry = transmitter.connection.route();
                return true;
            }
            return (routeSelection != null && routeSelection.hasNext())
                    || routeSelector.hasNext();
        }
    }

    /**
     * 如果应该重试用于当前连接的路由，则返回 true，即使连接本身不健康
     * 这里最大的问题是我们不应该重用来自合并连接的路由
     */
    private boolean retryCurrentRoute() {
        return transmitter.connection != null
                && transmitter.connection.routeFailureCount == 0
                && Builder.sameConnection(transmitter.connection.route().address().url(), address.url());
    }

}
