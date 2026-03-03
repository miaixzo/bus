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
