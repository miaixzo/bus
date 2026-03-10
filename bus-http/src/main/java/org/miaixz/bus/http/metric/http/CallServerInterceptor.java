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
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Builder;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.accord.Exchange;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.NewChain;

import java.io.IOException;
import java.net.ProtocolException;

/**
 * This is the last interceptor in the chain. It makes a network call to the server.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CallServerInterceptor implements Interceptor {

    /**
     * A flag indicating whether this interceptor is being used for a WebSocket connection.
     */
    private final boolean forWebSocket;

    /**
     * Constructs a new CallServerInterceptor.
     *
     * @param forWebSocket true if this is for a WebSocket upgrade request.
     */
    public CallServerInterceptor(boolean forWebSocket) {
        this.forWebSocket = forWebSocket;
    }

    /**
     * Intercepts the request to make a network call to the server. This method handles writing the request headers and
     * body, and reading the response headers and body. It also manages special cases like "100-continue" expectations
     * and WebSocket upgrades.
     *
     * @param chain The interceptor chain.
     * @return The response from the server.
     * @throws IOException if an I/O error occurs during the network call.
     */
    @Override
    public Response intercept(NewChain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Exchange exchange = realChain.exchange();
        Request request = realChain.request();

        long sentRequestMillis = System.currentTimeMillis();

        exchange.writeRequestHeaders(request);

        boolean responseHeadersStarted = false;
        Response.Builder responseBuilder = null;
        if (HTTP.permitsRequestBody(request.method()) && request.body() != null) {
            // If there's a "Expect: 100-continue" header on the request, wait for a "HTTP/1.1 100
            // Continue" response before transmitting the request body. If we don't get that, return
            // what we did get (such as a 4xx response) without ever transmitting the request body.
            if ("100-continue".equalsIgnoreCase(request.header(HTTP.EXPECT))) {
                exchange.flushRequest();
                responseHeadersStarted = true;
                exchange.responseHeadersStart();
                responseBuilder = exchange.readResponseHeaders(true);
            }

            if (responseBuilder == null) {
                if (request.body().isDuplex()) {
                    // Prepare a duplex body so that the application can send a request body later.
                    exchange.flushRequest();
                    BufferSink bufferedRequestBody = IoKit.buffer(exchange.createRequestBody(request, true));
                    request.body().writeTo(bufferedRequestBody);
                } else {
                    // Write the request body if the "Expect: 100-continue" expectation was met.
                    BufferSink bufferedRequestBody = IoKit.buffer(exchange.createRequestBody(request, false));
                    request.body().writeTo(bufferedRequestBody);
                    bufferedRequestBody.close();
                }
            } else {
                exchange.noRequestBody();
                if (!exchange.connection().isMultiplexed()) {
                    // If the "Expect: 100-continue" expectation wasn't met, prevent the connection from being reused.
                    exchange.noNewExchangesOnConnection();
                }
            }
        } else {
            exchange.noRequestBody();
        }

        if (request.body() == null || !request.body().isDuplex()) {
            exchange.finishRequest();
        }

        if (!responseHeadersStarted) {
            exchange.responseHeadersStart();
        }

        if (responseBuilder == null) {
            responseBuilder = exchange.readResponseHeaders(false);
        }

        Response response = responseBuilder.request(request).handshake(exchange.connection().handshake())
                .sentRequestAtMillis(sentRequestMillis).receivedResponseAtMillis(System.currentTimeMillis()).build();

        int code = response.code();
        if (code == 100) {
            // A server sent a 100-continue response even though we did not request one.
            // We must try again to read the actual response.
            responseBuilder = exchange.readResponseHeaders(false);
            response = responseBuilder.request(request).handshake(exchange.connection().handshake())
                    .sentRequestAtMillis(sentRequestMillis).receivedResponseAtMillis(System.currentTimeMillis())
                    .build();

            code = response.code();
        }

        exchange.responseHeadersEnd(response);

        if (forWebSocket && code == 101) {
            // The connection is upgrading, but we need to ensure interceptors see a non-null response body.
            response = response.newBuilder().body(Builder.EMPTY_RESPONSE).build();
        } else {
            response = response.newBuilder().body(exchange.openResponseBody(response)).build();
        }

        if ("close".equalsIgnoreCase(response.request().header(HTTP.CONNECTION))
                || "close".equalsIgnoreCase(response.header(HTTP.CONNECTION))) {
            exchange.noNewExchangesOnConnection();
        }

        if ((code == 204 || code == 205) && response.body().contentLength() > 0) {
            throw new ProtocolException(
                    "HTTP " + code + " had non-zero Content-Length: " + response.body().contentLength());
        }

        return response;
    }

}
