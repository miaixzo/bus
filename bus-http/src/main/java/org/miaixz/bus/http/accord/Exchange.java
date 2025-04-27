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

import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketException;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.AssignSink;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.AssignSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.NewCall;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.bodys.RealResponseBody;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.metric.EventListener;
import org.miaixz.bus.http.metric.Internal;
import org.miaixz.bus.http.metric.http.HttpCodec;
import org.miaixz.bus.http.socket.RealWebSocket;

/**
 * Transmits a single HTTP request and a response pair. This layers connection management and events on
 * {@link HttpCodec}, which handles the actual I/O.
 */
public final class Exchange {
    final Transmitter transmitter;
    final NewCall call;
    final EventListener eventListener;
    final ExchangeFinder finder;
    final HttpCodec codec;
    private boolean duplex;

    public Exchange(Transmitter transmitter, NewCall call, EventListener eventListener, ExchangeFinder finder,
            HttpCodec codec) {
        this.transmitter = transmitter;
        this.call = call;
        this.eventListener = eventListener;
        this.finder = finder;
        this.codec = codec;
    }

    public RealConnection connection() {
        return codec.connection();
    }

    /**
     * Returns true if the request body need not complete before the response body starts.
     */
    public boolean isDuplex() {
        return duplex;
    }

    public void writeRequestHeaders(Request request) throws IOException {
        try {
            eventListener.requestHeadersStart(call);
            codec.writeRequestHeaders(request);
            eventListener.requestHeadersEnd(call, request);
        } catch (IOException e) {
            eventListener.requestFailed(call, e);
            trackFailure(e);
            throw e;
        }
    }

    public Sink createRequestBody(Request request, boolean duplex) throws IOException {
        this.duplex = duplex;
        long contentLength = request.body().length();
        eventListener.requestBodyStart(call);
        Sink rawRequestBody = codec.createRequestBody(request, contentLength);
        return new RequestBodySink(rawRequestBody, contentLength);
    }

    public void flushRequest() throws IOException {
        try {
            codec.flushRequest();
        } catch (IOException e) {
            eventListener.requestFailed(call, e);
            trackFailure(e);
            throw e;
        }
    }

    public void finishRequest() throws IOException {
        try {
            codec.finishRequest();
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
            Response.Builder result = codec.readResponseHeaders(expectContinue);
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
            long contentLength = codec.reportedContentLength(response);
            Source rawSource = codec.openResponseBodySource(response);
            ResponseBodySource source = new ResponseBodySource(rawSource, contentLength);
            return new RealResponseBody(contentType, contentLength, IoKit.buffer(source));
        } catch (IOException e) {
            eventListener.responseFailed(call, e);
            trackFailure(e);
            throw e;
        }
    }

    public Headers trailers() throws IOException {
        return codec.trailers();
    }

    public void timeoutEarlyExit() {
        transmitter.timeoutEarlyExit();
    }

    public RealWebSocket.Streams newWebSocketStreams() throws SocketException {
        transmitter.timeoutEarlyExit();
        return codec.connection().newWebSocketStreams(this);
    }

    public void webSocketUpgradeFailed() {
        bodyComplete(-1L, true, true, null);
    }

    public void noNewExchangesOnConnection() {
        codec.connection().noNewExchanges();
    }

    public void cancel() {
        codec.cancel();
    }

    /**
     * Revoke this exchange's access to streams. This is necessary when a follow-up request is required but the
     * preceding exchange hasn't completed yet.
     */
    public void detachWithViolence() {
        codec.cancel();
        transmitter.exchangeMessageDone(this, true, true, null);
    }

    void trackFailure(IOException e) {
        finder.trackFailure();
        codec.connection().trackFailure(e);
    }

    IOException bodyComplete(long bytesRead, boolean responseDone, boolean requestDone, IOException e) {
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
     * A request body that fires events when it completes.
     */
    private final class RequestBodySink extends AssignSink {
        private boolean completed;
        /**
         * The exact number of bytes to be written, or -1L if that is unknown.
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
            if (closed)
                throw new IllegalStateException("closed");
            if (contentLength != -1L && bytesReceived + byteCount > contentLength) {
                throw new ProtocolException(
                        "expected " + contentLength + " bytes but received " + (bytesReceived + byteCount));
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
            if (closed)
                return;
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
            if (completed)
                return e;
            completed = true;
            return bodyComplete(bytesReceived, false, true, e);
        }
    }

    /**
     * A response body that fires events when it completes.
     */
    final class ResponseBodySource extends AssignSource {
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
            if (closed)
                throw new IllegalStateException("closed");
            try {
                long read = delegate().read(sink, byteCount);
                if (read == -1L) {
                    complete(null);
                    return -1L;
                }

                long newBytesReceived = bytesReceived + read;
                if (contentLength != -1L && newBytesReceived > contentLength) {
                    throw new ProtocolException(
                            "expected " + contentLength + " bytes but received " + newBytesReceived);
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
            if (closed)
                return;
            closed = true;
            try {
                super.close();
                complete(null);
            } catch (IOException e) {
                throw complete(e);
            }
        }

        IOException complete(IOException e) {
            if (completed)
                return e;
            completed = true;
            return bodyComplete(bytesReceived, true, false, e);
        }
    }
}
