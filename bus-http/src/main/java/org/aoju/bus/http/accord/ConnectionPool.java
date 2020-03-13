/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.http.accord;

import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.Address;
import org.aoju.bus.http.Builder;
import org.aoju.bus.http.Route;
import org.aoju.bus.http.accord.platform.Platform;

import java.lang.ref.Reference;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 管理HTTP和HTTP/2连接的重用，以减少网络延迟。 共享相同的
 * {@link Address}的HTTP请求可能共享一个{@link Connection}
 * 该类实现了哪些连接保持开放以供将来使用的策略
 *
 * @author Kimi Liu
 * @version 5.6.9
 * @since JDK 1.8+
 */
public final class ConnectionPool {

    /**
     * 后台线程用于清除过期的连接。每个连接池最多只能运行一个线程。
     * 线程池执行程序允许池本身被垃圾收集
     */
    private static final Executor executor = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(), Builder.threadFactory("Httpd ConnectionPool", true));
    public final Deque<RealConnection> connections = new ArrayDeque<>();
    public final RouteDatabase routeDatabase = new RouteDatabase();
    /**
     * 每个地址的最大空闲连接数.
     */
    private final int maxIdleConnections;
    private final long keepAliveDurationNs;
    boolean cleanupRunning;
    private final Runnable cleanupRunnable = () -> {
        while (true) {
            long waitNanos = cleanup(System.nanoTime());
            if (waitNanos == -1) return;
            if (waitNanos > 0) {
                long waitMillis = waitNanos / 1000000L;
                waitNanos -= (waitMillis * 1000000L);
                synchronized (ConnectionPool.this) {
                    try {
                        ConnectionPool.this.wait(waitMillis, (int) waitNanos);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    };

    /**
     * 使用适合于单用户应用程序的调优参数创建新的连接池。
     * 这个池中的调优参数可能在将来的Httpd版本中更改。
     * 目前这个池最多可以容纳5个空闲连接，这些连接将在5分钟不活动后被清除
     */
    public ConnectionPool() {
        this(5, 5, TimeUnit.MINUTES);
    }

    public ConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
        this.maxIdleConnections = maxIdleConnections;
        this.keepAliveDurationNs = timeUnit.toNanos(keepAliveDuration);

        if (keepAliveDuration <= 0) {
            throw new IllegalArgumentException("keepAliveDuration <= 0: " + keepAliveDuration);
        }
    }

    /**
     * 返回池中空闲连接的数量
     *
     * @return 连接的数量
     */
    public synchronized int idleConnectionCount() {
        int total = 0;
        for (RealConnection connection : connections) {
            if (connection.allocations.isEmpty()) total++;
        }
        return total;
    }

    /**
     * 返回池中的连接总数。注意，在Httpd 2.7之前，这只包括空闲连接 和HTTP/2连接
     * 因为Httpd 2.7包含了所有的连接，包括活动的和非活动的。
     * 使用{@link #idleConnectionCount()}来计数当前未使用的连接
     *
     * @return 连接总数
     */
    public synchronized int connectionCount() {
        return connections.size();
    }

    /**
     * 返回一个循环连接到{@code address}，如果不存在这样的连接，
     * 则返回null。如果地址尚未被路由，则路由为空.
     *
     * @param address          地址
     * @param streamAllocation 协调者
     * @param route            路由
     * @return 连接信息
     */
    public RealConnection get(Address address, StreamAllocation streamAllocation, Route route) {
        assert (Thread.holdsLock(this));
        for (RealConnection connection : connections) {
            if (connection.isEligible(address, route)) {
                streamAllocation.acquire(connection, true);
                return connection;
            }
        }
        return null;
    }

    /**
     * 如果可能，将{@code streamAllocation}持有的连接替换为共享连接。
     * 当并发地创建多个多路连接时，这将恢复
     *
     * @param address          地址
     * @param streamAllocation 协调者
     * @return 套接字
     */
    public Socket deduplicate(Address address, StreamAllocation streamAllocation) {
        assert (Thread.holdsLock(this));
        for (RealConnection connection : connections) {
            if (connection.isEligible(address, null)
                    && connection.isMultiplexed()
                    && connection != streamAllocation.connection()) {
                return streamAllocation.releaseAndAcquire(connection);
            }
        }
        return null;
    }

    public void put(RealConnection connection) {
        assert (Thread.holdsLock(this));
        if (!cleanupRunning) {
            cleanupRunning = true;
            executor.execute(cleanupRunnable);
        }
        connections.add(connection);
    }

    /**
     * 通知这个池{@code connection}已经空闲。如果连接已从池中删除，并且应该关闭，则返回true。
     *
     * @param connection 连接信息
     * @return the true/false
     */
    public boolean connectionBecameIdle(RealConnection connection) {
        assert (Thread.holdsLock(this));
        if (connection.noNewStreams || maxIdleConnections == 0) {
            connections.remove(connection);
            return true;
        } else {
            // 唤醒清理线程:可能已经超过了空闲连接限制
            notifyAll();
            return false;
        }
    }

    /**
     * 关闭并删除池中的所有空闲连接.
     */
    public void evictAll() {
        List<RealConnection> evictedConnections = new ArrayList<>();
        synchronized (this) {
            for (Iterator<RealConnection> i = connections.iterator(); i.hasNext(); ) {
                RealConnection connection = i.next();
                if (connection.allocations.isEmpty()) {
                    connection.noNewStreams = true;
                    evictedConnections.add(connection);
                    i.remove();
                }
            }
        }

        for (RealConnection connection : evictedConnections) {
            IoUtils.close(connection.socket());
        }
    }

    /**
     * 对这个池执行维护，如果连接超出了keep alive限制或idle connections限制，就会清除空闲时间最长的连接
     * 返回到该方法的下一次预定调用之前在nanos中的睡眠时间。如果不需要进一步清理，则返回-1
     *
     * @param now 空闲时间
     * @return 睡眠时间
     */
    long cleanup(long now) {
        int inUseConnectionCount = 0;
        int idleConnectionCount = 0;
        RealConnection longestIdleConnection = null;
        long longestIdleDurationNs = Long.MIN_VALUE;

        // 找到与清除的联系，或者下一次清除的时间
        synchronized (this) {
            for (Iterator<RealConnection> i = connections.iterator(); i.hasNext(); ) {
                RealConnection connection = i.next();

                // 如果正在使用连接，请继续搜索.
                if (pruneAndGetAllocationCount(connection, now) > 0) {
                    inUseConnectionCount++;
                    continue;
                }

                idleConnectionCount++;

                // 如果连接准备好被驱逐，我们就完成了
                long idleDurationNs = now - connection.idleAtNanos;
                if (idleDurationNs > longestIdleDurationNs) {
                    longestIdleDurationNs = idleDurationNs;
                    longestIdleConnection = connection;
                }
            }

            if (longestIdleDurationNs >= this.keepAliveDurationNs
                    || idleConnectionCount > this.maxIdleConnections) {
                // 我们发现了与驱逐有关的证据。将它从列表中移除，然后在下面(同步块外部)关闭它
                connections.remove(longestIdleConnection);
            } else if (idleConnectionCount > 0) {
                // 一个连接将准备驱逐很快.
                return keepAliveDurationNs - longestIdleDurationNs;
            } else if (inUseConnectionCount > 0) {
                // 所有连接都在使用中。至少能维持生命直到我们再次运行.
                return keepAliveDurationNs;
            } else {
                // 没有连接，空闲或正在使用
                cleanupRunning = false;
                return -1;
            }
        }

        IoUtils.close(longestIdleConnection.socket());
        // 立即清理.
        return 0;
    }

    /**
     * 删除任何泄漏的分配，然后返回{@code connection}上剩余的活动分配的数量。
     * 泄漏检测是不精确的，并且依赖于垃圾收集
     *
     * @param connection 连接信息
     * @param now        时间
     * @return 可分配的数量
     */
    private int pruneAndGetAllocationCount(RealConnection connection, long now) {
        List<Reference<StreamAllocation>> references = connection.allocations;
        for (int i = 0; i < references.size(); ) {
            Reference<StreamAllocation> reference = references.get(i);

            if (reference.get() != null) {
                i++;
                continue;
            }

            // 我们发现了一个泄露的分配。这是一个应用程序错误.
            StreamAllocation.StreamAllocationReference streamAllocRef =
                    (StreamAllocation.StreamAllocationReference) reference;
            String message = "A connection to " + connection.route().address().url()
                    + " was leaked. Did you forget to close a response body?";
            Platform.get().logCloseableLeak(message, streamAllocRef.callStackTrace);

            references.remove(i);
            connection.noNewStreams = true;

            // 如果这是最后一次分配，则该连接可以立即被收回.
            if (references.isEmpty()) {
                connection.idleAtNanos = now - keepAliveDurationNs;
                return 0;
            }
        }
        return references.size();
    }

}
