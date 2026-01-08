/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketException;

/**
 * Transmits a single HTTP request and a response pair. This layers connection management and events on
 * {@link HttpCodec}, which handles the actual I/O.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Exchange {

    /**
     * The transmitter for this exchange, which manages the connection and events.
     */
    final Transmitter transmitter;
    /**
     * The call that initiated this exchange.
     */
    final NewCall call;
    /**
     * The event listener for this exchange.
     */
    final EventListener eventListener;
    /**
     * The finder for this exchange, which locates a connection.
     */
    final ExchangeFinder finder;
    /**
     * The codec for this exchange, which handles HTTP-level protocol details.
     */
    final HttpCodec codec;
    /**
     * True if the request body need not complete before the response body starts.
     */
    private boolean duplex;

    public Exchange(Transmitter transmitter, NewCall call, EventListener eventListener, ExchangeFinder finder,
            HttpCodec codec) {
        this.transmitter = transmitter;
        this.call = call;
        this.eventListener = eventListener;
        this.finder = finder;
        this.codec = codec;
    }

    /**
     * Returns the connection for this exchange.
     *
     * @return The connection.
     */
    public RealConnection connection() {
        return codec.connection();
    }

    /**
     * Returns true if the request body need not complete before the response body starts.
     *
     * @return {@code true} if this is a duplex exchange.
     */
    public boolean isDuplex() {
        return duplex;
    }

    /**
     * Writes the request headers to the network.
     *
     * @param request The request whose headers to write.
     * @throws IOException if an I/O error occurs.
     */
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

    /**
     * Creates a sink to write the request body.
     *
     * @param request The request whose body to write.
     * @param duplex  Whether this is a duplex request.
     * @return A sink for the request body.
     * @throws IOException if an I/O error occurs.
     */
    public Sink createRequestBody(Request request, boolean duplex) throws IOException {
        this.duplex = duplex;
        long contentLength = request.body().contentLength();
        eventListener.requestBodyStart(call);
        Sink rawRequestBody = codec.createRequestBody(request, contentLength);
        return new RequestBodySink(rawRequestBody, contentLength);
    }

    /**
     * Flushes the request to the network.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void flushRequest() throws IOException {
        try {
            codec.flushRequest();
        } catch (IOException e) {
            eventListener.requestFailed(call, e);
            trackFailure(e);
            throw e;
        }
    }

    /**
     * Finishes writing the request to the network.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void finishRequest() throws IOException {
        try {
            codec.finishRequest();
        } catch (IOException e) {
            eventListener.requestFailed(call, e);
            trackFailure(e);
            throw e;
        }
    }

    /**
     * Notifies the event listener that response headers are about to be read.
     */
    public void responseHeadersStart() {
        eventListener.responseHeadersStart(call);
    }

    /**
     * Reads the response headers from the network.
     *
     * @param expectContinue Whether a 100-continue response is expected.
     * @return A builder for the response, or null if no response is available.
     * @throws IOException if an I/O error occurs.
     */
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

    /**
     * Notifies the event listener that the response headers have been read.
     *
     * @param response The response whose headers were read.
     */
    public void responseHeadersEnd(Response response) {
        eventListener.responseHeadersEnd(call, response);
    }

    /**
     * Opens a source to read the response body.
     *
     * @param response The response whose body to open.
     * @return The response body.
     * @throws IOException if an I/O error occurs.
     */
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

    /**
     * Returns the trailer headers for this exchange.
     *
     * @return The trailer headers.
     * @throws IOException if an I/O error occurs.
     */
    public Headers trailers() throws IOException {
        return codec.trailers();
    }

    /**
     * Notifies the transmitter that a timeout has occurred and the exchange should exit early.
     */
    public void timeoutEarlyExit() {
        transmitter.timeoutEarlyExit();
    }

    /**
     * Creates new streams for a WebSocket connection.
     *
     * @return The WebSocket streams.
     * @throws SocketException if a socket error occurs.
     */
    public RealWebSocket.Streams newWebSocketStreams() throws SocketException {
        transmitter.timeoutEarlyExit();
        return codec.connection().newWebSocketStreams(this);
    }

    /**
     * Notifies this exchange that a WebSocket upgrade has failed.
     */
    public void webSocketUpgradeFailed() {
        bodyComplete(-1L, true, true, null);
    }

    /**
     * Prevents new exchanges from being created on the current connection.
     */
    public void noNewExchangesOnConnection() {
        codec.connection().noNewExchanges();
    }

    /**
     * Cancels this exchange.
     */
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

    /**
     * Tracks a failure for this exchange.
     *
     * @param e The exception that occurred.
     */
    void trackFailure(IOException e) {
        finder.trackFailure();
        codec.connection().trackFailure(e);
    }

    /**
     * Completes the body of the exchange, firing events and notifying the transmitter.
     *
     * @param bytesRead    The number of bytes read or written.
     * @param responseDone Whether the response is done.
     * @param requestDone  Whether the request is done.
     * @param e            The exception that occurred, or null if none.
     * @return The exception, or null if none.
     */
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

    /**
     * Notifies the transmitter that there is no request body.
     */
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
                complete(null); // No bytes to read, so the body is complete.
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
