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
 * Manages reuse of HTTP and HTTP/2 connections for reduced network latency. An instance of this class maintains
 * connections to multiple hosts. This class is thread-safe.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class RealConnectionPool {

    /**
     * A single background thread is used to clean up expired connections.
     */
    private static final Executor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), Builder.threadFactory("Http ConnectionPool", true));

    /**
     * The maximum number of idle connections for each address.
     */
    private final int maxIdleConnections;
    /**
     * The duration in nanoseconds to keep idle connections alive.
     */
    private final long keepAliveDurationNs;
    /**
     * A deque of connections in this pool, ordered by most recently used first.
     */
    private final Deque<RealConnection> connections = new ArrayDeque<>();
    /**
     * Database of routes that have failed TCP connection attempts. This is used to blacklist routes that are unlikely
     * to be reachable.
     */
    final RouteDatabase routeDatabase = new RouteDatabase();
    /**
     * True if a background cleanup thread is running. Guarded by this.
     */
    boolean cleanupRunning;

    /**
     * The background task that evicts expired connections.
     */
    private final Runnable cleanupRunnable = () -> {
        while (true) {
            long waitNanos = cleanup(System.nanoTime());
            if (waitNanos == -1)
                return; // No connections to cleanup.
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

    /**
     * Creates a new connection pool with tuning parameters appropriate for a single-user application. The tuning
     * parameters in this pool are subject to change in future releases. Currently this pool holds up to 5 idle
     * connections which will be evicted after 5 minutes of inactivity.
     *
     * @param maxIdleConnections The maximum number of idle connections to keep in the pool.
     * @param keepAliveDuration  The time to keep idle connections alive.
     * @param timeUnit           The time unit for the keep alive duration.
     */
    public RealConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
        this.maxIdleConnections = maxIdleConnections;
        this.keepAliveDurationNs = timeUnit.toNanos(keepAliveDuration);

        // Put a floor on the keep alive duration, otherwise cleanup will spin loop.
        if (keepAliveDuration <= 0) {
            throw new IllegalArgumentException("keepAliveDuration <= 0: " + keepAliveDuration);
        }
    }

    /**
     * Returns the number of idle connections in the pool.
     *
     * @return The number of idle connections.
     */
    public synchronized int idleConnectionCount() {
        int total = 0;
        for (RealConnection connection : connections) {
            if (connection.transmitters.isEmpty())
                total++;
        }
        return total;
    }

    /**
     * Returns the total number of connections in the pool.
     *
     * @return The total number of connections.
     */
    public synchronized int connectionCount() {
        return connections.size();
    }

    /**
     * Attempts to acquire a recycled connection to {@code address} for {@code transmitter}. Returns true if a
     * connection was acquired.
     *
     * @param address            The address of the connection.
     * @param transmitter        The transmitter requesting the connection.
     * @param routes             The list of routes to try.
     * @param requireMultiplexed True if a multiplexed connection is required.
     * @return True if a connection was acquired, false otherwise.
     */
    boolean transmitterAcquirePooledConnection(
            Address address,
            Transmitter transmitter,
            List<Route> routes,
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

    /**
     * Puts a connection back into the pool. If the pool is not already running a cleanup thread, it will start one.
     *
     * @param connection The connection to put back into the pool.
     */
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
     *
     * @param connection The connection that has become idle.
     * @return True if the connection was removed and should be closed.
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

    /**
     * Closes and removes all idle connections in the pool.
     */
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
     *
     * @param now The current time in nanoseconds.
     * @return The duration in nanoseconds to wait until the next cleanup.
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
     *
     * @param connection The connection to prune.
     * @param now        The current time in nanoseconds.
     * @return The number of live transmitters on the connection.
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
     *
     * @param failedRoute The route that failed.
     * @param failure     The exception that caused the failure.
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
