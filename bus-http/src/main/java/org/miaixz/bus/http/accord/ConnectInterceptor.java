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

import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.NewChain;
import org.miaixz.bus.http.metric.http.RealInterceptorChain;

import java.io.IOException;

/**
 * An interceptor that opens a connection to the target server and proceeds to the next interceptor.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ConnectInterceptor implements Interceptor {

    /**
     * The httpd client instance.
     */
    public final Httpd httpd;

    /**
     * Constructs a new ConnectInterceptor.
     *
     * @param httpd The Httpd client instance.
     */
    public ConnectInterceptor(Httpd httpd) {
        this.httpd = httpd;
    }

    /**
     * Intercepts the request to establish a connection and create an exchange.
     *
     * @param chain The interceptor chain.
     * @return The response from the server.
     * @throws IOException if an I/O error occurs during the connection.
     */
    @Override
    public Response intercept(NewChain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        Transmitter transmitter = realChain.transmitter();

        // We need the network to satisfy this request. This may be used to validate a conditional GET.
        boolean doExtensiveHealthChecks = !HTTP.GET.equals(request.method());
        Exchange exchange = transmitter.newExchange(chain, doExtensiveHealthChecks);

        return realChain.proceed(request, transmitter, exchange);
    }

}
