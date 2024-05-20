/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Builder;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.accord.Exchange;
import org.miaixz.bus.http.metric.Interceptor;

import java.io.IOException;
import java.net.ProtocolException;

/**
 * 这是链中的最后一个拦截器
 * 它对服务器进行网络调用
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CallServerInterceptor implements Interceptor {

    private final boolean forWebSocket;

    public CallServerInterceptor(boolean forWebSocket) {
        this.forWebSocket = forWebSocket;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Exchange exchange = realChain.exchange();
        Request request = realChain.request();

        long sentRequestMillis = System.currentTimeMillis();

        exchange.writeRequestHeaders(request);

        boolean responseHeadersStarted = false;
        Response.Builder responseBuilder = null;
        if (Http.permitsRequestBody(request.method()) && request.body() != null) {
            // If there's a "Expect: 100-continue" header on the request, wait for a "HTTP/1.1 100
            // Continue" response before transmitting the request body. If we don't get that, return
            // what we did get (such as a 4xx response) without ever transmitting the request body.
            if ("100-continue".equalsIgnoreCase(request.header("Expect"))) {
                exchange.flushRequest();
                responseHeadersStarted = true;
                exchange.responseHeadersStart();
                responseBuilder = exchange.readResponseHeaders(true);
            }

            if (responseBuilder == null) {
                if (request.body().isDuplex()) {
                    // Prepare a duplex body so that the application can send a request body later.
                    exchange.flushRequest();
                    BufferSink bufferedRequestBody = IoKit.buffer(
                            exchange.createRequestBody(request, true));
                    request.body().writeTo(bufferedRequestBody);
                } else {
                    // Write the request body if the "Expect: 100-continue" expectation was met.
                    BufferSink bufferedRequestBody = IoKit.buffer(
                            exchange.createRequestBody(request, false));
                    request.body().writeTo(bufferedRequestBody);
                    bufferedRequestBody.close();
                }
            } else {
                exchange.noRequestBody();
                if (!exchange.connection().isMultiplexed()) {
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

        Response response = responseBuilder
                .request(request)
                .handshake(exchange.connection().handshake())
                .sentRequestAtMillis(sentRequestMillis)
                .receivedResponseAtMillis(System.currentTimeMillis())
                .build();

        int code = response.code();
        if (code == 100) {
            // server sent a 100-continue even though we did not request one.
            // try again to read the actual response
            response = exchange.readResponseHeaders(false)
                    .request(request)
                    .handshake(exchange.connection().handshake())
                    .sentRequestAtMillis(sentRequestMillis)
                    .receivedResponseAtMillis(System.currentTimeMillis())
                    .build();

            code = response.code();
        }

        exchange.responseHeadersEnd(response);

        if (forWebSocket && code == 101) {
            // Connection is upgrading, but we need to ensure interceptors see a non-null response body.
            response = response.newBuilder()
                    .body(Builder.EMPTY_RESPONSE)
                    .build();
        } else {
            response = response.newBuilder()
                    .body(exchange.openResponseBody(response))
                    .build();
        }

        if ("close".equalsIgnoreCase(response.request().header(Header.CONNECTION))
                || "close".equalsIgnoreCase(response.header(Header.CONNECTION))) {
            exchange.noNewExchangesOnConnection();
        }

        if ((code == 204 || code == 205) && response.body().length() > 0) {
            throw new ProtocolException(
                    "HTTP " + code + " had non-zero Content-Length: " + response.body().length());
        }

        return response;
    }

}
