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
package org.miaixz.bus.http.metric;

import org.miaixz.bus.http.NewCall;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.accord.Connection;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A chain of interceptors that can process an HTTP request and response.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface NewChain {

    /**
     * Returns the request currently being processed.
     *
     * @return The network request.
     */
    Request request();

    /**
     * Proceeds with the request to the next interceptor in the chain.
     *
     * @param request The network request.
     * @return The response from the next interceptor.
     * @throws IOException if an I/O error occurs.
     */
    Response proceed(Request request) throws IOException;

    /**
     * Returns the connection that will be used to execute the request. This is only available in the network
     * interceptor chain; for application interceptors, this will always be null.
     *
     * @return The connection information.
     */
    Connection connection();

    /**
     * Returns the actual call that is ready to execute the request.
     *
     * @return The {@link NewCall} instance.
     */
    NewCall call();

    /**
     * Returns the connect timeout in milliseconds.
     *
     * @return The connect timeout in milliseconds.
     */
    int connectTimeoutMillis();

    /**
     * Sets the connect timeout for this chain.
     *
     * @param timeout The timeout value.
     * @param unit    The time unit.
     * @return This {@link NewChain} instance.
     */
    NewChain withConnectTimeout(int timeout, TimeUnit unit);

    /**
     * Returns the read timeout in milliseconds.
     *
     * @return The read timeout in milliseconds.
     */
    int readTimeoutMillis();

    /**
     * Configures the read timeout for this chain.
     *
     * @param timeout The timeout value.
     * @param unit    The time unit.
     * @return This {@link NewChain} instance.
     */
    NewChain withReadTimeout(int timeout, TimeUnit unit);

    /**
     * Returns the write timeout in milliseconds.
     *
     * @return The write timeout in milliseconds.
     */
    int writeTimeoutMillis();

    /**
     * Configures the write timeout for this chain.
     *
     * @param timeout The timeout value.
     * @param unit    The time unit.
     * @return This {@link NewChain} instance.
     */
    NewChain withWriteTimeout(int timeout, TimeUnit unit);

}
