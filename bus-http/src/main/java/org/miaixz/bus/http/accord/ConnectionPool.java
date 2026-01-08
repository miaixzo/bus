/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.http.Address;

import java.util.concurrent.TimeUnit;

/**
 * Manages reuse of HTTP and HTTP/2 connections for reduced network latency. HTTP requests that share the same
 * {@link Address} may share a {@link Connection}. This class implements the policy of which connections to keep open
 * for future use.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class ConnectionPool {

    /**
     * The real connection pool implementation.
     */
    public final RealConnectionPool delegate;

    /**
     * Create a new connection pool with tuning parameters appropriate for a single-user application. The tuning
     * parameters in this pool may change in future Httpd releases. Currently this pool holds up to 5 idle connections
     * which will be evicted after 5 minutes of inactivity.
     */
    public ConnectionPool() {
        this(5, 5, TimeUnit.MINUTES);
    }

    public ConnectionPool(int maxIdleConnections, long keepAliveDuration, TimeUnit timeUnit) {
        this.delegate = new RealConnectionPool(maxIdleConnections, keepAliveDuration, timeUnit);
    }

    /**
     * Returns the number of idle connections in the pool.
     *
     * @return The number of idle connections.
     */
    public int idleConnectionCount() {
        return delegate.idleConnectionCount();
    }

    /**
     * Returns the total number of connections in the pool. Note that prior to Httpd 2.7 this included only idle
     * connections and HTTP/2 connections. Since Httpd 2.7 this includes all connections, both active and inactive. Use
     * {@link #idleConnectionCount()} to count connections that are currently unused.
     *
     * @return The total number of connections.
     */
    public int connectionCount() {
        return delegate.connectionCount();
    }

    /**
     * Closes and removes all idle connections in the pool.
     */
    public void evictAll() {
        delegate.evictAll();
    }

}
