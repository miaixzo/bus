/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.http.accord;

import org.aoju.bus.core.io.*;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.http.Headers;
import org.aoju.bus.http.NewCall;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.bodys.RealResponseBody;
import org.aoju.bus.http.bodys.ResponseBody;
import org.aoju.bus.http.metric.EventListener;
import org.aoju.bus.http.metric.Internal;
import org.aoju.bus.http.metric.http.HttpCodec;
import org.aoju.bus.http.socket.RealWebSocket;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketException;

/**
 * 传输单个 HTTP 请求和响应对。这在处理实际 I/O 的 {@link HttpCodec} 上分层连接管理和事件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Exchange {

    final Transmitter transmitter;
    final NewCall call;
    final EventListener eventListener;
    final ExchangeFinder finder;
    final HttpCodec httpCodec;
    private boolean duplex;

    public Exchange(Transmitter transmitter, NewCall call, EventListener eventListener,
                    ExchangeFinder finder, HttpCodec httpCodec) {
        this.transmitter = transmitter;
        this.call = call;
        this.eventListener = eventListener;
        this.finder = finder;
        this.httpCodec = httpCodec;
    }

    public RealConnection connection() {
        return httpCodec.connection();
    }

    /**
     * 如果请求正文不需要在响应正文开始之前完成，则返回 true
     */
    public boolean isDuplex() {
        return duplex;
    }

    public void writeRequestHeaders(Request request) throws IOException {
        try {
            eventListener.requestHeadersStart(call);
            httpCodec.writeRequestHeaders(request);
            eventListener.requestHeadersEnd(call, request);
        } catch (IOException e) {
            eventListener.requestFailed(call, e);
            trackFailure(e);
            throw e;
        }
    }

    public Sink createRequestBody(Request request, boolean duplex) throws IOException {
        this.duplex = duplex;
        long contentLength = request.body().contentLength();
        eventListener.requestBodyStart(call);
        Sink rawRequestBody = httpCodec.createRequestBody(request, contentLength);
        return new RequestBodySink(rawRequestBody, contentLength);
    }

    public void flushRequest() throws IOException {
        try {
            httpCodec.flushRequest();
        } catch (IOException e) {
            eventListener.requestFailed(call, e);
            trackFailure(e);
            throw e;
        }
    }

    public void finishRequest() throws IOException {
        try {
            httpCodec.finishRequest();
        } catch (IOException e) {
            eventListener.requestFailed(call, e);
            trackFailure(e);
            throw e;
        }
    }

    public void responseHeadersStart() {
        eventListener.responseHeadersStart(call);
    }

    public Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
        try {
            Response.Builder result = httpCodec.readResponseHeaders(expectContinue);
            if (result != null) {
                Internal.instance.initExchange(result, this);
            }
            return result;
        } catch (IOException e) {
            eventListener.responseFailed(call, e);
            trackFailure(e);
            throw e;
        }
    }

    public void responseHeadersEnd(Response response) {
        eventListener.responseHeadersEnd(call, response);
    }

    public ResponseBody openResponseBody(Response response) throws IOException {
        try {
            eventListener.responseBodyStart(call);
            String contentType = response.header("Content-Type");
            long contentLength = httpCodec.reportedContentLength(response);
            Source rawSource = httpCodec.openResponseBodySource(response);
            ResponseBodySource source = new ResponseBodySource(rawSource, contentLength);
            return new RealResponseBody(contentType, contentLength, IoKit.buffer(source));
        } catch (IOException e) {
            eventListener.responseFailed(call, e);
            trackFailure(e);
            throw e;
        }
    }

    public Headers trailers() throws IOException {
        return httpCodec.trailers();
    }

    public void timeoutEarlyExit() {
        transmitter.timeoutEarlyExit();
    }

    public RealWebSocket.Streams newWebSocketStreams() throws SocketException {
        transmitter.timeoutEarlyExit();
        return httpCodec.connection().newWebSocketStreams(this);
    }

    public void webSocketUpgradeFailed() {
        bodyComplete(-1L, true, true, null);
    }

    public void noNewExchangesOnConnection() {
        httpCodec.connection().noNewExchanges();
    }

    public void cancel() {
        httpCodec.cancel();
    }

    /**
     * 撤销此交换对流的访问。当需要后续请求但之前的交换尚未完成时，这是必要的
     */
    public void detachWithViolence() {
        httpCodec.cancel();
        transmitter.exchangeMessageDone(this, true, true, null);
    }

    void trackFailure(IOException e) {
        finder.trackFailure();
        httpCodec.connection().trackFailure(e);
    }

    IOException bodyComplete(
            long bytesRead, boolean responseDone, boolean requestDone, IOException e) {
        if (e != null) {
            trackFailure(e);
        }
        if (requestDone) {
            if (e != null) {
                eventListener.requestFailed(call, e);
            } else {
                eventListener.requestBodyEnd(call, bytesRead);
            }
        }
        if (responseDone) {
            if (e != null) {
                eventListener.responseFailed(call, e);
            } else {
                eventListener.responseBodyEnd(call, bytesRead);
            }
        }
        return transmitter.exchangeMessageDone(this, requestDone, responseDone, e);
    }

    public void noRequestBody() {
        transmitter.exchangeMessageDone(this, true, false, null);
    }

    /**
     * 完成时触发事件的请求正文
     */
    private class RequestBodySink extends DelegateSink {
        private boolean completed;
        /**
         * 要写入的确切字节数，如果未知，则为 -1L
         */
        private long contentLength;
        private long bytesReceived;
        private boolean closed;

        RequestBodySink(Sink delegate, long contentLength) {
            super(delegate);
            this.contentLength = contentLength;
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            if (closed) throw new IllegalStateException("closed");
            if (contentLength != -1L && bytesReceived + byteCount > contentLength) {
                throw new ProtocolException("expected " + contentLength
                        + " bytes but received " + (bytesReceived + byteCount));
            }
            try {
                super.write(source, byteCount);
                this.bytesReceived += byteCount;
            } catch (IOException e) {
                throw complete(e);
            }
        }

        @Override
        public void flush() throws IOException {
            try {
                super.flush();
            } catch (IOException e) {
                throw complete(e);
            }
        }

        @Override
        public void close() throws IOException {
            if (closed) return;
            closed = true;
            if (contentLength != -1L && bytesReceived != contentLength) {
                throw new ProtocolException("unexpected end of stream");
            }
            try {
                super.close();
                complete(null);
            } catch (IOException e) {
                throw complete(e);
            }
        }

        private IOException complete(IOException e) {
            if (completed) return e;
            completed = true;
            return bodyComplete(bytesReceived, false, true, e);
        }
    }

    /**
     * 完成时触发事件的响应主体
     */
    class ResponseBodySource extends DelegateSource {
        private final long contentLength;
        private long bytesReceived;
        private boolean completed;
        private boolean closed;

        ResponseBodySource(Source delegate, long contentLength) {
            super(delegate);
            this.contentLength = contentLength;

            if (contentLength == 0L) {
                complete(null);
            }
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            if (closed) throw new IllegalStateException("closed");
            try {
                long read = delegate().read(sink, byteCount);
                if (read == -1L) {
                    complete(null);
                    return -1L;
                }

                long newBytesReceived = bytesReceived + read;
                if (contentLength != -1L && newBytesReceived > contentLength) {
                    throw new ProtocolException("expected " + contentLength
                            + " bytes but received " + newBytesReceived);
                }

                bytesReceived = newBytesReceived;
                if (newBytesReceived == contentLength) {
                    complete(null);
                }

                return read;
            } catch (IOException e) {
                throw complete(e);
            }
        }

        @Override
        public void close() throws IOException {
            if (closed) return;
            closed = true;
            try {
                super.close();
                complete(null);
            } catch (IOException e) {
                throw complete(e);
            }
        }

        IOException complete(IOException e) {
            if (completed) return e;
            completed = true;
            return bodyComplete(bytesReceived, true, false, e);
        }
    }

}
