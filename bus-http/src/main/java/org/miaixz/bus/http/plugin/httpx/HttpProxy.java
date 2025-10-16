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
package org.miaixz.bus.http.plugin.httpx;

import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.secure.Authenticator;
import org.miaixz.bus.http.secure.Credentials;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Represents the configuration for an HTTP proxy server. This class holds the address, port, credentials, and type of
 * the proxy.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HttpProxy {

    /**
     * The proxy server's hostname or IP address.
     */
    public final String hostAddress;
    /**
     * The proxy server's port number.
     */
    public final int port;
    /**
     * The username for proxy authentication. Can be null if no authentication is required.
     */
    public final String user;
    /**
     * The password for proxy authentication. Can be null if no authentication is required.
     */
    public final String password;
    /**
     * The type of proxy (e.g., HTTP, SOCKS).
     */
    public final Proxy.Type type;

    /**
     * Constructs a new proxy configuration.
     *
     * @param hostAddress The hostname or IP address of the proxy server (e.g., "proxy.example.com", "192.168.1.1").
     * @param port        The port number of the proxy server.
     * @param user        The username for authentication, or null if not required.
     * @param password    The password for authentication, or null if not required.
     * @param type        The type of the proxy (e.g., {@link java.net.Proxy.Type#HTTP}).
     */
    public HttpProxy(String hostAddress, int port, String user, String password, java.net.Proxy.Type type) {
        this.hostAddress = hostAddress;
        this.port = port;
        this.user = user;
        this.password = password;
        this.type = type;
    }

    /**
     * Constructs a new HTTP proxy configuration without authentication.
     *
     * @param hostAddress The hostname or IP address of the proxy server.
     * @param port        The port number of the proxy server.
     */
    public HttpProxy(String hostAddress, int port) {
        this(hostAddress, port, null, null, java.net.Proxy.Type.HTTP);
    }

    /**
     * Creates a {@link java.net.Proxy} object from this configuration.
     *
     * @return A {@link java.net.Proxy} instance suitable for use with an HTTP client.
     */
    public java.net.Proxy proxy() {
        return new java.net.Proxy(type, new InetSocketAddress(hostAddress, port));
    }

    /**
     * Creates an {@link Authenticator} for this proxy configuration. This authenticator adds the 'Proxy-Authorization'
     * header with Basic authentication credentials.
     *
     * @return An {@link Authenticator} instance.
     */
    public Authenticator authenticator() {
        return (route, response) -> {
            String credential = Credentials.basic(user, password);
            return response.request().newBuilder().header(HTTP.PROXY_AUTHORIZATION, credential)
                    .header(HTTP.PROXY_CONNECTION, HTTP.KEEP_ALIVE).build();
        };
    }

}
