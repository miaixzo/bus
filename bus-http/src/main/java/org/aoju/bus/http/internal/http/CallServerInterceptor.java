/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.internal.http;

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.io.BufferedSink;
import org.aoju.bus.core.io.ForwardingSink;
import org.aoju.bus.core.io.Sink;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.Interceptor;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.internal.Internal;
import org.aoju.bus.http.internal.connection.RealConnection;
import org.aoju.bus.http.internal.connection.StreamAllocation;

import java.io.IOException;
import java.net.ProtocolException;

/**
 * This is the last intercept in the chain. It makes a network call to the server.
 *
 * @author Kimi Liu
 * @version 3.2.8
 * @since JDK 1.8
 */
public final class CallServerInterceptor implements Interceptor {

    private final boolean forWebSocket;

    public CallServerInterceptor(boolean forWebSocket) {
        this.forWebSocket = forWebSocket;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        HttpCodec httpCodec = realChain.httpStream();
        StreamAllocation streamAllocation = realChain.streamAllocation();
        RealConnection connection = (RealConnection) realChain.connection();
        Request request = realChain.request();

        long sentRequestMillis = System.currentTimeMillis();

        realChain.eventListener().requestHeadersStart(realChain.call());
        httpCodec.writeRequestHeaders(request);
        realChain.eventListener().requestHeadersEnd(realChain.call(), request);

        Response.Builder responseBuilder = null;
        if (HttpMethod.permitsRequestBody(request.method()) && request.body() != null) {
            // If there's a "Expect: 100-continue" header on the request, wait for a "HTTP/1.1 100
            // Continue" response before transmitting the request body. If we don't get that, return
            // what we did get (such as a 4xx response) without ever transmitting the request body.
            if ("100-continue".equalsIgnoreCase(request.header("Expect"))) {
                httpCodec.flushRequest();
                realChain.eventListener().responseHeadersStart(realChain.call());
                responseBuilder = httpCodec.readResponseHeaders(true);
            }

            if (responseBuilder == null) {
                // Write the request body if the "Expect: 100-continue" expectation was met.
                realChain.eventListener().requestBodyStart(realChain.call());
                long contentLength = request.body().contentLength();
                CountingSink requestBodyOut =
                        new CountingSink(httpCodec.createRequestBody(request, contentLength));
                BufferedSink bufferedRequestBody = IoUtils.buffer(requestBodyOut);

                request.body().writeTo(bufferedRequestBody);
                bufferedRequestBody.close();
                realChain.eventListener()
                        .requestBodyEnd(realChain.call(), requestBodyOut.successfulCount);
            } else if (!connection.isMultiplexed()) {
                // If the "Expect: 100-continue" expectation wasn't met, prevent the HTTP/1 connection
                // from being reused. Otherwise we're still obligated to transmit the request body to
                // leave the connection in a consistent state.
                streamAllocation.noNewStreams();
            }
        }

        httpCodec.finishRequest();

        if (responseBuilder == null) {
            realChain.eventListener().responseHeadersStart(realChain.call());
            responseBuilder = httpCodec.readResponseHeaders(false);
        }

        Response response = responseBuilder
                .request(request)
                .handshake(streamAllocation.connection().handshake())
                .sentRequestAtMillis(sentRequestMillis)
                .receivedResponseAtMillis(System.currentTimeMillis())
                .build();

        int code = response.code();
        if (code == 100) {
            // server sent a 100-continue even though we did not request first.
            // try again to read the actual response
            responseBuilder = httpCodec.readResponseHeaders(false);

            response = responseBuilder
                    .request(request)
                    .handshake(streamAllocation.connection().handshake())
                    .sentRequestAtMillis(sentRequestMillis)
                    .receivedResponseAtMillis(System.currentTimeMillis())
                    .build();

            code = response.code();
        }

        realChain.eventListener()
                .responseHeadersEnd(realChain.call(), response);

        if (forWebSocket && code == 101) {
            // Connection is upgrading, but we need to ensure interceptors see a non-null response body.
            response = response.newBuilder()
                    .body(Internal.EMPTY_RESPONSE)
                    .build();
        } else {
            response = response.newBuilder()
                    .body(httpCodec.openResponseBody(response))
                    .build();
        }

        if ("close".equalsIgnoreCase(response.request().header("Connection"))
                || "close".equalsIgnoreCase(response.header("Connection"))) {
            streamAllocation.noNewStreams();
        }

        if ((code == 204 || code == 205) && response.body().contentLength() > 0) {
            throw new ProtocolException(
                    "HTTP " + code + " had non-zero Content-Length: " + response.body().contentLength());
        }

        return response;
    }

    static final class CountingSink extends ForwardingSink {
        long successfulCount;

        CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            successfulCount += byteCount;
        }
    }

}
