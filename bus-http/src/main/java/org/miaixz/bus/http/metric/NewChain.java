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
