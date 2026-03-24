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
package org.miaixz.bus.http.accord;

import org.miaixz.bus.http.Address;

import java.util.concurrent.TimeUnit;

/**
 * Manages reuse of HTTP and HTTP/2 connections for reduced network latency. HTTP requests that share the same
 * {@link Address} may share a {@link Connection}. This class implements the policy of which connections to keep open
 * for future use.
 *
 * @author Kimi Liu
 * @since Java 21+
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
