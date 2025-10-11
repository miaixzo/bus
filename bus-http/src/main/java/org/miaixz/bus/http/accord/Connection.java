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
