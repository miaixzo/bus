/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Address;
import org.miaixz.bus.http.Builder;
import org.miaixz.bus.http.Route;
import org.miaixz.bus.http.accord.platform.Platform;

import java.io.IOException;
import java.lang.ref.Reference;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 真实连接池
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RealConnectionPool {
    /**
     * 后台线程用于清理过期的连接。每个连接池最多运行一个线程 线程池执行器允许池本身被垃圾收集
     */
    private static final Executor executor = new ThreadPoolExecutor(0 /* corePoolSize */,
            Integer.MAX_VALUE /* maximumPoolSize */, 60L /* keepAliveTime */, TimeUnit.SECONDS,
            new SynchronousQueue<>(), Builder.threadFactory("Http ConnectionPool", true));
    final RouteDatabase routeDatabase = new RouteDatabase();
    /**
     * The maximum number of idle connections for each address.
     */
    private final int maxIdleConnections;
    private final long keepAliveDurationNs;
    private final Deque<RealConnection> connections = new ArrayDeque<>();
    boolean cleanupRunning;
    private final Runnable cleanupRunnable = () -> {
        while (true) {
            long waitNanos = cleanup(System.nanoTime());
            if (waitNanos == -1)
                return;
            if (waitNanos > 0) {
                long waitMillis = waitNanos / 1000000L;
                waitNanos -= (waitMillis * 1000000L);
                synchronized (RealConnectionPool.this) {
                    try {
                        RealConnectionPool.this.wait(waitMillis, (int) waitNanos);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    };

    public RealConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
        this.maxIdleConnections = maxIdleConnections;
        this.keepAliveDurationNs = timeUnit.toNanos(keepAliveDuration);

        // Put a floor on the keep alive duration, otherwise cleanup will spin loop.
        if (keepAliveDuration <= 0) {
            throw new IllegalArgumentException("keepAliveDuration <= 0: " + keepAliveDuration);
        }
    }

    public synchronized int idleConnectionCount() {
        int total = 0;
        for (RealConnection connection : connections) {
            if (connection.transmitters.isEmpty())
                total++;
        }
        return total;
    }

    public synchronized int connectionCount() {
        return connections.size();
    }

    /**
     * Attempts to acquire a recycled connection to {@code address} for {@code transmitter}. Returns true if a
     * connection was acquired.
     */
    boolean transmitterAcquirePooledConnection(Address address, Transmitter transmitter, List<Route> routes,
            boolean requireMultiplexed) {
        assert (Thread.holdsLock(this));
        for (RealConnection connection : connections) {
            if (requireMultiplexed && !connection.isMultiplexed())
                continue;
            if (!connection.isEligible(address, routes))
                continue;
            transmitter.acquireConnectionNoEvents(connection);
            return true;
        }
        return false;
    }

    void put(RealConnection connection) {
        assert (Thread.holdsLock(this));
        if (!cleanupRunning) {
            cleanupRunning = true;
            executor.execute(cleanupRunnable);
        }
        connections.add(connection);
    }

    /**
     * Notify this pool that {@code connection} has become idle. Returns true if the connection has been removed from
     * the pool and should be closed.
     */
    boolean connectionBecameIdle(RealConnection connection) {
        assert (Thread.holdsLock(this));
        if (connection.noNewExchanges || maxIdleConnections == 0) {
            connections.remove(connection);
            return true;
        } else {
            notifyAll(); // Awake the cleanup thread: we may have exceeded the idle connection limit.
            return false;
        }
    }

    public void evictAll() {
        List<RealConnection> evictedConnections = new ArrayList<>();
        synchronized (this) {
            for (Iterator<RealConnection> i = connections.iterator(); i.hasNext();) {
                RealConnection connection = i.next();
                if (connection.transmitters.isEmpty()) {
                    connection.noNewExchanges = true;
                    evictedConnections.add(connection);
                    i.remove();
                }
            }
        }

        for (RealConnection connection : evictedConnections) {
            IoKit.close(connection.socket());
        }
    }

    /**
     * Performs maintenance on this pool, evicting the connection that has been idle the longest if either it has
     * exceeded the keep alive limit or the idle connections limit. Returns the duration in nanos to sleep until the
     * next scheduled call to this method. Returns -1 if no further cleanups are required.
     */
    long cleanup(long now) {
        int inUseConnectionCount = 0;
        int idleConnectionCount = 0;
        RealConnection longestIdleConnection = null;
        long longestIdleDurationNs = Long.MIN_VALUE;

        // Find either a connection to evict, or the time that the next eviction is due.
        synchronized (this) {
            for (Iterator<RealConnection> i = connections.iterator(); i.hasNext();) {
                RealConnection connection = i.next();

                // If the connection is in use, keep searching.
                if (pruneAndGetAllocationCount(connection, now) > 0) {
                    inUseConnectionCount++;
                    continue;
                }

                idleConnectionCount++;

                // If the connection is ready to be evicted, we're done.
                long idleDurationNs = now - connection.idleAtNanos;
                if (idleDurationNs > longestIdleDurationNs) {
                    longestIdleDurationNs = idleDurationNs;
                    longestIdleConnection = connection;
                }
            }

            if (longestIdleDurationNs >= this.keepAliveDurationNs || idleConnectionCount > this.maxIdleConnections) {
                // We've found a connection to evict. Remove it from the list, then close it below (outside
                // of the synchronized block).
                connections.remove(longestIdleConnection);
            } else if (idleConnectionCount > 0) {
                // A connection will be ready to evict soon.
                return keepAliveDurationNs - longestIdleDurationNs;
            } else if (inUseConnectionCount > 0) {
                // All connections are in use. It'll be at least the keep alive duration 'til we run again.
                return keepAliveDurationNs;
            } else {
                // No connections, idle or in use.
                cleanupRunning = false;
                return -1;
            }
        }

        IoKit.close(longestIdleConnection.socket());

        // Cleanup again immediately.
        return 0;
    }

    /**
     * Prunes any leaked transmitters and then returns the number of remaining live transmitters on {@code connection}.
     * Transmitters are leaked if the connection is tracking them but the application code has abandoned them. Leak
     * detection is imprecise and relies on garbage collection.
     */
    private int pruneAndGetAllocationCount(RealConnection connection, long now) {
        List<Reference<Transmitter>> references = connection.transmitters;
        for (int i = 0; i < references.size();) {
            Reference<Transmitter> reference = references.get(i);

            if (reference.get() != null) {
                i++;
                continue;
            }

            // We've discovered a leaked transmitter. This is an application bug.
            Transmitter.TransmitterReference transmitterRef = (Transmitter.TransmitterReference) reference;
            String message = "A connection to " + connection.route().address().url()
                    + " was leaked. Did you forget to close a response body?";
            Platform.get().logCloseableLeak(message, transmitterRef.callStackTrace);

            references.remove(i);
            connection.noNewExchanges = true;

            // If this was the last allocation, the connection is eligible for immediate eviction.
            if (references.isEmpty()) {
                connection.idleAtNanos = now - keepAliveDurationNs;
                return 0;
            }
        }

        return references.size();
    }

    /**
     * Track a bad route in the route database. Other routes will be attempted first.
     */
    public void connectFailed(Route failedRoute, IOException failure) {
        // Tell the proxy selector when we fail to connect on a fresh connection.
        if (failedRoute.proxy().type() != Proxy.Type.DIRECT) {
            Address address = failedRoute.address();
            address.proxySelector().connectFailed(address.url().uri(), failedRoute.proxy().address(), failure);
        }

        routeDatabase.failed(failedRoute);
    }

}
