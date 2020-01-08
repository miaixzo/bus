/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.http.metric.http;

import org.aoju.bus.core.io.segment.Buffer;
import org.aoju.bus.core.io.segment.BufferSink;
import org.aoju.bus.core.io.segment.DelegateSink;
import org.aoju.bus.core.io.segment.Sink;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.accord.RealConnection;
import org.aoju.bus.http.accord.StreamAllocation;
import org.aoju.bus.http.bodys.ResponseBody;
import org.aoju.bus.http.metric.Interceptor;

import java.io.IOException;
import java.net.ProtocolException;

/**
 * 这是链中的最后一个拦截器
 * 它对服务器进行网络调用
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
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
            if ("100-continue".equalsIgnoreCase(request.header("Expect"))) {
                httpCodec.flushRequest();
                realChain.eventListener().responseHeadersStart(realChain.call());
                responseBuilder = httpCodec.readResponseHeaders(true);
            }

            if (responseBuilder == null) {
                realChain.eventListener().requestBodyStart(realChain.call());
                long contentLength = request.body().contentLength();
                CountingSink requestBodyOut =
                        new CountingSink(httpCodec.createRequestBody(request, contentLength));
                BufferSink bufferedRequestBody = IoUtils.buffer(requestBodyOut);

                request.body().writeTo(bufferedRequestBody);
                bufferedRequestBody.close();
                realChain.eventListener()
                        .requestBodyEnd(realChain.call(), requestBodyOut.successfulCount);
            } else if (!connection.isMultiplexed()) {
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
        if (code == Http.HTTP_CONTINUE) {
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

        if (forWebSocket && code == Http.HTTP_SWITCHING_PROTOCOL) {
            response = response.newBuilder()
                    .body(ResponseBody.create(null, Normal.EMPTY_BYTE_ARRAY))
                    .build();
        } else {
            response = response.newBuilder()
                    .body(httpCodec.openResponseBody(response))
                    .build();
        }

        if ("close".equalsIgnoreCase(response.request().header(org.aoju.bus.core.lang.Header.CONNECTION))
                || "close".equalsIgnoreCase(response.header(org.aoju.bus.core.lang.Header.CONNECTION))) {
            streamAllocation.noNewStreams();
        }

        if ((code == Http.HTTP_NO_CONTENT || code == Http.HTTP_RESET) && response.body().contentLength() > 0) {
            throw new ProtocolException(
                    "HTTP " + code + " had non-zero Content-Length: " + response.body().contentLength());
        }

        return response;
    }

    static final class CountingSink extends DelegateSink {
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
