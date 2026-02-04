/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.accord;

import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.http.Route;
import org.miaixz.bus.http.socket.Handshake;

import java.net.Socket;

/**
 * The socket and streams of an HTTP, HTTPS, or HTTPS+HTTP/2 connection. May be used for multiple HTTP request/response
 * exchanges. Connections may be direct to the origin server or via a proxy.
 * <p>
 * Typically instances of this class are created, connected, and exercised automatically by the HTTP client.
 * Applications may use this class to monitor HTTP connections as members of a {@linkplain ConnectionPool connection
 * pool}.
 * <p>
 * Do not confuse this class with the misnamed {@code HttpURLConnection}, which isn't so much a connection as a single
 * request/response exchange.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Connection {

    /**
     * Returns the route that this connection follows.
     *
     * @return The route of this connection.
     */
    Route route();

    /**
     * Returns the socket that this connection uses. If this connection is HTTPS, this is an
     * {@link javax.net.ssl.SSLSocket SSLSocket}. If this is an HTTP/2 connection the socket may be shared by multiple
     * concurrent calls.
     *
     * @return The socket for this connection.
     */
    Socket socket();

    /**
     * Returns the TLS handshake used to establish this connection, or null if the connection is not HTTPS.
     *
     * @return The TLS handshake, or null.
     */
    Handshake handshake();

    /**
     * Returns the protocol negotiated by this connection, or {@link Protocol#HTTP_1_1} if no protocol was negotiated.
     * This method returns {@link Protocol#HTTP_1_1} even if the remote peer is using {@link Protocol#HTTP_1_0}.
     *
     * @return The negotiated protocol.
     */
    Protocol protocol();

}
