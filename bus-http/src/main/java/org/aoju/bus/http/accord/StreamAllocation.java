/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.lang.exception.RevisedException;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.http.*;
import org.aoju.bus.http.metric.EventListener;
import org.aoju.bus.http.metric.Interceptor;
import org.aoju.bus.http.metric.http.ErrorCode;
import org.aoju.bus.http.metric.http.HttpCodec;
import org.aoju.bus.http.metric.http.StreamException;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.List;

/**
 * 该类协调三个实体之间的关系
 * 这个类支持{@linkplain #cancel asynchronous canceling}。这是为了使爆炸半径尽可能小。
 * 如果HTTP/2流处于活动状态，取消将取消该流，但不会取消共享其连接的其他流。但是如果TLS握手
 * 仍然在进行中，那么取消可能会中断整个连接
 *
 * @author Kimi Liu
 * @version 6.2.0
 * @since JDK 1.8+
 */
public final class StreamAllocation {

    public final Address address;
    public final NewCall call;
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

    public StreamAllocation(ConnectionPool connectionPool, Address address, NewCall call,
                            EventListener eventListener, Object callStackTrace) {
        this.connectionPool = connectionPool;
        this.address = address;
        this.call = call;
        this.eventListener = eventListener;
        this.routeSelector = new RouteSelector(address, routeDatabase(), call, eventListener);
        this.callStackTrace = callStackTrace;
    }

    public HttpCodec newStream(
            Httpd client, Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
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

    /**
     * 找到一个连接，如果它是健康的，则返回它。如果不健康，则重复此过程，直到找到一个健康的连接
     *
     * @param connectTimeout          连接超时时间
     * @param readTimeout             读取超时时间
     * @param writeTimeout            写入超时时间
     * @param pingIntervalMillis      ping间隔时间
     * @param connectionRetryEnabled  是否重试
     * @param doExtensiveHealthChecks 是否健康检查
     * @return 连接信息
     * @throws IOException 异常
     */
    private RealConnection findHealthyConnection(int connectTimeout, int readTimeout,
                                                 int writeTimeout, int pingIntervalMillis, boolean connectionRetryEnabled,
                                                 boolean doExtensiveHealthChecks) throws IOException {
        while (true) {
            RealConnection candidate = findConnection(connectTimeout, readTimeout, writeTimeout,
                    pingIntervalMillis, connectionRetryEnabled);

            // 如果这是一个全新的连接，可以跳过大量的健康检查
            synchronized (connectionPool) {
                if (candidate.successCount == 0 && !candidate.isMultiplexed()) {
                    return candidate;
                }
            }

            // 执行(可能很慢的)检查以确认池连接仍然良好。如果不是，把它从池中取出，重新开始
            if (!candidate.isHealthy(doExtensiveHealthChecks)) {
                noNewStreams();
                continue;
            }

            return candidate;
        }
    }

    /**
     * 返回主持新流的连接。如果现有连接存在，则优先选择连接池，最后构建新连接
     *
     * @param connectTimeout         连接超时时间
     * @param readTimeout            读取超时时间
     * @param writeTimeout           写入超时时间
     * @param pingIntervalMillis     ping间隔时间
     * @param connectionRetryEnabled 是否重试
     * @return 连接信息
     * @throws IOException 异常
     */
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

            // 尝试使用已分配的连接。在这里需要小心，因为已经分配的连接可能已经被限制不能创建新的流
            releasedConnection = this.connection;
            toClose = releaseIfNoNewStreams();
            if (this.connection != null) {
                // 有一个已经分配的连接
                result = this.connection;
                releasedConnection = null;
            }
            if (!reportedAcquired) {
                // 如果从未报告获得连接，不要将其报告为已发布!
                releasedConnection = null;
            }

            if (result == null) {
                // 尝试从池中获取连接
                Builder.instance.get(connectionPool, address, this, null);
                if (connection != null) {
                    foundPooledConnection = true;
                    result = connection;
                } else {
                    selectedRoute = route;
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
            route = connection.route();
            return result;
        }

        // 如果我们需要选择路线，就选一条。这是一个阻塞操作
        boolean newRouteSelection = false;
        if (selectedRoute == null && (routeSelection == null || !routeSelection.hasNext())) {
            newRouteSelection = true;
            routeSelection = routeSelector.next();
        }

        synchronized (connectionPool) {
            if (canceled) throw new IOException("Canceled");

            if (newRouteSelection) {
                // 现在有了一组IP地址，再尝试从池中获取一个连接。这可能由于连接合并而匹配
                List<Route> routes = routeSelection.getAll();
                for (int i = 0, size = routes.size(); i < size; i++) {
                    Route route = routes.get(i);
                    Builder.instance.get(connectionPool, address, this, route);
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

                // 创建一个连接并立即将其分配给这个分配。这使得异步cancel()可以中断我们将要进行的握手
                route = selectedRoute;
                refusedStreamCount = 0;
                result = new RealConnection(connectionPool, selectedRoute);
                acquire(result, false);
            }
        }

        // 如果在第二次找到池连接，就完成了。
        if (foundPooledConnection) {
            eventListener.connectionAcquired(call, result);
            return result;
        }

        // TCP + TLS握手，这是一个阻塞操作
        result.connect(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis,
                connectionRetryEnabled, call, eventListener);
        routeDatabase().connected(result.route());

        Socket socket = null;
        synchronized (connectionPool) {
            reportedAcquired = true;

            // 连接池信息
            Builder.instance.put(connectionPool, result);

            // 如果并发地创建了到同一地址的另一个多路复用连接，则释放该连接并获取该连接
            if (result.isMultiplexed()) {
                socket = Builder.instance.deduplicate(connectionPool, address, this);
                result = connection;
            }
        }
        IoKit.close(socket);

        eventListener.connectionAcquired(call, result);
        return result;
    }

    /**
     * 释放当前持有的连接并返回一个套接字来关闭，如果持有的连接*限制新流的创建
     * 对于HTTP/2，多个请求共享同一个连接，因此在后续请求期间，我们的连接可能被限制创建新流
     *
     * @return 套接字关闭选项
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
        IoKit.close(socket);
        if (releasedConnection != null) {
            eventListener.connectionReleased(call, releasedConnection);
        }

        if (e != null) {
            e = Builder.instance.timeoutExit(call, e);
            eventListener.callFailed(call, e);
        } else if (callEnd) {
            Builder.instance.timeoutExit(call, null);
            eventListener.callEnd(call);
        }
    }

    public HttpCodec codec() {
        synchronized (connectionPool) {
            return codec;
        }
    }

    private RouteDatabase routeDatabase() {
        return Builder.instance.routeDatabase(connectionPool);
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
        IoKit.close(socket);
        if (releasedConnection != null) {
            Builder.instance.timeoutExit(call, null);
            eventListener.connectionReleased(call, releasedConnection);
            eventListener.callEnd(call);
        }
    }

    /**
     * 禁止在承载此分配的连接上创建新流
     */
    public void noNewStreams() {
        Socket socket;
        Connection releasedConnection;
        synchronized (connectionPool) {
            releasedConnection = connection;
            socket = deallocate(true, false, false);
            if (connection != null) releasedConnection = null;
        }
        IoKit.close(socket);
        if (releasedConnection != null) {
            eventListener.connectionReleased(call, releasedConnection);
        }
    }

    /**
     * 释放由这个分配所持有的资源。如果分配了足够的资源，连接将被分离或关闭。调用方必须在连接池上同步
     * 返回一个关闭选项，调用者应该在同步块完成时将其传递给{@link IoKit#close}。(在连接池上同步时，我们不执行I/O。)
     *
     * @param noNewStreams   是否新的流
     * @param released       是否最终
     * @param streamFinished 是否已经结束
     * @return 套接字关闭选项
     */
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
                    if (Builder.instance.connectionBecameIdle(connectionPool, connection)) {
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
            if (e instanceof StreamException) {
                ErrorCode errorCode = ((StreamException) e).errorCode;
                if (errorCode == ErrorCode.REFUSED_STREAM) {
                    // 在同一个连接上重试一次REFUSED_STREAM错误。
                    refusedStreamCount++;
                    if (refusedStreamCount > 1) {
                        noNewStreams = true;
                        route = null;
                    }
                } else if (errorCode != ErrorCode.CANCEL) {
                    // 为取消错误保留连接。其他的一切都需要一个新的连接
                    noNewStreams = true;
                    route = null;
                }
            } else if (connection != null
                    && (!connection.isMultiplexed() || e instanceof RevisedException)) {
                noNewStreams = true;

                // 如果这条路线还没有完成一个电话，避免它为新的连接
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

        IoKit.close(socket);
        if (releasedConnection != null) {
            eventListener.connectionReleased(call, releasedConnection);
        }
    }

    /**
     * 使用这个分配来保存{@code connection}。每个对它的调用必须与
     * 对同一连接上的{@link #release}的调用配对
     *
     * @param connection       连接信息
     * @param reportedAcquired 是否已经取得报告
     */
    public void acquire(RealConnection connection, boolean reportedAcquired) {
        assert (Thread.holdsLock(connectionPool));
        if (this.connection != null) {
            throw new IllegalStateException();
        }

        this.connection = connection;
        this.reportedAcquired = reportedAcquired;
        connection.allocations.add(new StreamAllocationReference(this, callStackTrace));
    }

    /**
     * 从连接的分配列表中删除此分配
     *
     * @param connection 连接信息
     */
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

    /**
     * 释放该连接持有的连接，并获取{@code newConnection}。只有在持有的
     * 连接是新连接，但是被{@code newConnection}复制时，调用它才是安全
     * 的。通常在并发连接到HTTP/2 webserver时发生这种情况
     * 返回一个关闭选项，调用者应该在同步块完成*时将其传递给{@link IoKit#close(Socket)}
     *
     * @param newConnection 新连接信息
     * @return 套接字关闭选项
     */
    public Socket releaseAndAcquire(RealConnection newConnection) {
        assert (Thread.holdsLock(connectionPool));
        if (codec != null || connection.allocations.size() != 1) throw new IllegalStateException();

        // 释放旧连接
        Reference<StreamAllocation> onlyAllocation = connection.allocations.get(0);
        Socket socket = deallocate(true, false, false);

        // 获得新的连接
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
        /**
         * 在调用执行或进入队列时捕获堆栈跟踪。这有助于确定连接泄漏的来源
         */
        public final Object callStackTrace;

        StreamAllocationReference(StreamAllocation referent, Object callStackTrace) {
            super(referent);
            this.callStackTrace = callStackTrace;
        }
    }

}
