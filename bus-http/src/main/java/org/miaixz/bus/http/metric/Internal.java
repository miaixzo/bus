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
package org.miaixz.bus.http.metric;

import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.ConnectionPool;
import org.miaixz.bus.http.accord.ConnectionSuite;
import org.miaixz.bus.http.accord.Exchange;
import org.miaixz.bus.http.accord.RealConnectionPool;

import javax.net.ssl.SSLSocket;

/**
 * Provides a mechanism to access internal APIs within the {@code org.miaixz.bus.http} package. The only implementation
 * of this abstract class is in {@link Httpd}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Internal {

    /**
     * The singleton instance of this internal API.
     */
    public static Internal instance;

    /**
     * Adds a lenient header line to the {@link Headers.Builder}.
     *
     * @param builder The headers builder.
     * @param line    The header line to add.
     */
    public abstract void addLenient(Headers.Builder builder, String line);

    /**
     * Adds a lenient header name and value to the {@link Headers.Builder}.
     *
     * @param builder The headers builder.
     * @param name    The header name.
     * @param value   The header value.
     */
    public abstract void addLenient(Headers.Builder builder, String name, String value);

    /**
     * Returns the real connection pool from the given connection pool.
     *
     * @param connectionPool The connection pool.
     * @return The real connection pool.
     */
    public abstract RealConnectionPool realConnectionPool(ConnectionPool connectionPool);

    /**
     * Checks if two {@link Address} instances are equal, ignoring the host.
     *
     * @param a The first address.
     * @param b The second address.
     * @return {@code true} if the non-host parts of the addresses are equal.
     */
    public abstract boolean equalsNonHost(Address a, Address b);

    /**
     * Returns the HTTP status code from a {@link Response.Builder}.
     *
     * @param responseBuilder The response builder.
     * @return The HTTP status code.
     */
    public abstract int code(Response.Builder responseBuilder);

    /**
     * Applies the given {@link ConnectionSuite} to the {@link SSLSocket}.
     *
     * @param tlsConfiguration The TLS configuration to apply.
     * @param sslSocket        The SSL socket.
     * @param isFallback       Whether this is a fallback connection.
     */
    public abstract void apply(ConnectionSuite tlsConfiguration, SSLSocket sslSocket, boolean isFallback);

    /**
     * Creates a new WebSocket call.
     *
     * @param client  The HTTP client.
     * @param request The request.
     * @return A new WebSocket call.
     */
    public abstract NewCall newWebSocketCall(Httpd client, Request request);

    /**
     * Initializes the exchange for a {@link Response.Builder}.
     *
     * @param responseBuilder The response builder.
     * @param exchange        The exchange.
     */
    public abstract void initExchange(Response.Builder responseBuilder, Exchange exchange);

    /**
     * Returns the {@link Exchange} associated with a {@link Response}.
     *
     * @param response The response.
     * @return The exchange.
     */
    public abstract Exchange exchange(Response response);

}
