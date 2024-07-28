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
package org.miaixz.bus.http.metric;

import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.ConnectionPool;
import org.miaixz.bus.http.accord.ConnectionSuite;
import org.miaixz.bus.http.accord.Exchange;
import org.miaixz.bus.http.accord.RealConnectionPool;

import javax.net.ssl.SSLSocket;

/**
 * Escalate internal APIs in {@code http} so they can be used from Http's implementation packages. The only
 * implementation of this interface is in {@link Httpd}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Internal {

    public static Internal instance;

    public abstract void addLenient(Headers.Builder builder, String line);

    public abstract void addLenient(Headers.Builder builder, String name, String value);

    public abstract RealConnectionPool realConnectionPool(ConnectionPool connectionPool);

    public abstract boolean equalsNonHost(Address a, Address b);

    public abstract int code(Response.Builder responseBuilder);

    public abstract void apply(ConnectionSuite tlsConfiguration, SSLSocket sslSocket, boolean isFallback);

    public abstract NewCall newWebSocketCall(Httpd client, Request request);

    public abstract void initExchange(Response.Builder responseBuilder, Exchange exchange);

    public abstract Exchange exchange(Response response);

}
