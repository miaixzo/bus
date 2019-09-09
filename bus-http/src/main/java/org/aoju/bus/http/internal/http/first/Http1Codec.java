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
package org.aoju.bus.http.internal.http.first;

import org.aoju.bus.core.io.*;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.*;
import org.aoju.bus.http.internal.Internal;
import org.aoju.bus.http.internal.connection.RealConnection;
import org.aoju.bus.http.internal.connection.StreamAllocation;
import org.aoju.bus.http.internal.http.*;

import java.io.EOFException;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.concurrent.TimeUnit;

/**
 * A socket connection that can be used to send HTTP/1.1 messages. This class strictly enforces the
 * following lifecycle:
 *
 * <ol>
 * <li>{@linkplain #writeRequest Send request headers}.
 * <li>Open a sink to write the request body. Either {@linkplain #newFixedLengthSink
 * fixed-length} or {@link #newChunkedSink chunked}.
 * <li>Write to and then close that sink.
 * <li>{@linkplain #readResponseHeaders Read response headers}.
 * <li>Open a source to read the response body. Either {@linkplain #newFixedLengthSource
 * fixed-length}, {@linkplain #newChunkedSource chunked} or {@linkplain
 * #newUnknownLengthSource unknown length}.
 * <li>Read from and close that source.
 * </ol>
 *
 * <p>Exchanges that do not have a request body may skip creating and closing the request body.
 * Exchanges that do not have a response body can call {@link #newFixedLengthSource(long)
 * newFixedLengthSource(0)} and may skip reading and closing that source.
 *
 * @author Kimi Liu
 * @version 3.2.5
 * @since JDK 1.8
 */
public final class Http1Codec implements HttpCodec {

    private static final int STATE_IDLE = 0; // Idle connections are ready to write request headers.
    private static final int STATE_OPEN_REQUEST_BODY = 1;
    private static final int STATE_WRITING_REQUEST_BODY = 2;
    private static final int STATE_READ_RESPONSE_HEADERS = 3;
    private static final int STATE_OPEN_RESPONSE_BODY = 4;
    private static final int STATE_READING_RESPONSE_BODY = 5;
    private static final int STATE_CLOSED = 6;
    private static final int HEADER_LIMIT = 256 * 1024;

    /**
     * The client that configures this stream. May be null for HTTPS proxy tunnels.
     */
    final HttpClient client;
    /**
     * The stream allocation that owns this stream. May be null for HTTPS proxy tunnels.
     */
    final StreamAllocation streamAllocation;

    final BufferedSource source;
    final BufferedSink sink;
    int state = STATE_IDLE;
    private long headerLimit = HEADER_LIMIT;

    public Http1Codec(HttpClient client, StreamAllocation streamAllocation, BufferedSource source,
                      BufferedSink sink) {
        this.client = client;
        this.streamAllocation = streamAllocation;
        this.source = source;
        this.sink = sink;
    }

    @Override
    public Sink createRequestBody(Request request, long contentLength) {
        if ("chunked".equalsIgnoreCase(request.header("Transfer-Encoding"))) {
            // Stream a request body of unknown length.
            return newChunkedSink();
        }

        if (contentLength != -1) {
            // Stream a request body of a known length.
            return newFixedLengthSink(contentLength);
        }

        throw new IllegalStateException(
                "Cannot stream a request body without chunked encoding or a known content length!");
    }

    @Override
    public void cancel() {
        RealConnection connection = streamAllocation.connection();
        if (connection != null) connection.cancel();
    }

    @Override
    public void writeRequestHeaders(Request request) throws IOException {
        String requestLine = RequestLine.get(
                request, streamAllocation.connection().route().proxy().type());
        writeRequest(request.headers(), requestLine);
    }

    @Override
    public ResponseBody openResponseBody(Response response) throws IOException {
        streamAllocation.eventListener.responseBodyStart(streamAllocation.call);
        String contentType = response.header("Content-Type");

        if (!HttpHeaders.hasBody(response)) {
            Source source = newFixedLengthSource(0);
            return new RealResponseBody(contentType, 0, IoUtils.buffer(source));
        }

        if ("chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            Source source = newChunkedSource(response.request().url());
            return new RealResponseBody(contentType, -1L, IoUtils.buffer(source));
        }

        long contentLength = HttpHeaders.contentLength(response);
        if (contentLength != -1) {
            Source source = newFixedLengthSource(contentLength);
            return new RealResponseBody(contentType, contentLength, IoUtils.buffer(source));
        }

        return new RealResponseBody(contentType, -1L, IoUtils.buffer(newUnknownLengthSource()));
    }

    public boolean isClosed() {
        return state == STATE_CLOSED;
    }

    @Override
    public void flushRequest() throws IOException {
        sink.flush();
    }

    @Override
    public void finishRequest() throws IOException {
        sink.flush();
    }

    public void writeRequest(Headers headers, String requestLine) throws IOException {
        if (state != STATE_IDLE) throw new IllegalStateException("state: " + state);
        sink.writeUtf8(requestLine).writeUtf8("\r\n");
        for (int i = 0, size = headers.size(); i < size; i++) {
            sink.writeUtf8(headers.name(i))
                    .writeUtf8(": ")
                    .writeUtf8(headers.value(i))
                    .writeUtf8("\r\n");
        }
        sink.writeUtf8("\r\n");
        state = STATE_OPEN_REQUEST_BODY;
    }

    @Override
    public Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
        if (state != STATE_OPEN_REQUEST_BODY && state != STATE_READ_RESPONSE_HEADERS) {
            throw new IllegalStateException("state: " + state);
        }

        try {
            StatusLine statusLine = StatusLine.parse(readHeaderLine());

            Response.Builder responseBuilder = new Response.Builder()
                    .protocol(statusLine.protocol)
                    .code(statusLine.code)
                    .message(statusLine.message)
                    .headers(readHeaders());

            if (expectContinue && statusLine.code == StatusLine.HTTP_CONTINUE) {
                return null;
            } else if (statusLine.code == StatusLine.HTTP_CONTINUE) {
                state = STATE_READ_RESPONSE_HEADERS;
                return responseBuilder;
            }

            state = STATE_OPEN_RESPONSE_BODY;
            return responseBuilder;
        } catch (EOFException e) {
            // Provide more context if the server ends the stream before sending a response.
            IOException exception = new IOException("unexpected end of stream on " + streamAllocation);
            exception.initCause(e);
            throw exception;
        }
    }

    private String readHeaderLine() throws IOException {
        String line = source.readUtf8LineStrict(headerLimit);
        headerLimit -= line.length();
        return line;
    }

    public Headers readHeaders() throws IOException {
        Headers.Builder headers = new Headers.Builder();
        for (String line; (line = readHeaderLine()).length() != 0; ) {
            Internal.instance.addLenient(headers, line);
        }
        return headers.build();
    }

    public Sink newChunkedSink() {
        if (state != STATE_OPEN_REQUEST_BODY) throw new IllegalStateException("state: " + state);
        state = STATE_WRITING_REQUEST_BODY;
        return new ChunkedSink();
    }

    public Sink newFixedLengthSink(long contentLength) {
        if (state != STATE_OPEN_REQUEST_BODY) throw new IllegalStateException("state: " + state);
        state = STATE_WRITING_REQUEST_BODY;
        return new FixedLengthSink(contentLength);
    }

    public Source newFixedLengthSource(long length) throws IOException {
        if (state != STATE_OPEN_RESPONSE_BODY) throw new IllegalStateException("state: " + state);
        state = STATE_READING_RESPONSE_BODY;
        return new FixedLengthSource(length);
    }

    public Source newChunkedSource(HttpUrl url) throws IOException {
        if (state != STATE_OPEN_RESPONSE_BODY) throw new IllegalStateException("state: " + state);
        state = STATE_READING_RESPONSE_BODY;
        return new ChunkedSource(url);
    }

    public Source newUnknownLengthSource() throws IOException {
        if (state != STATE_OPEN_RESPONSE_BODY) throw new IllegalStateException("state: " + state);
        if (streamAllocation == null) throw new IllegalStateException("streamAllocation == null");
        state = STATE_READING_RESPONSE_BODY;
        streamAllocation.noNewStreams();
        return new UnknownLengthSource();
    }

    void detachTimeout(ForwardingTimeout timeout) {
        Timeout oldDelegate = timeout.delegate();
        timeout.setDelegate(Timeout.NONE);
        oldDelegate.clearDeadline();
        oldDelegate.clearTimeout();
    }

    private final class FixedLengthSink implements Sink {
        private final ForwardingTimeout timeout = new ForwardingTimeout(sink.timeout());
        private boolean closed;
        private long bytesRemaining;

        FixedLengthSink(long bytesRemaining) {
            this.bytesRemaining = bytesRemaining;
        }

        @Override
        public Timeout timeout() {
            return timeout;
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            if (closed) throw new IllegalStateException("closed");
            Internal.checkOffsetAndCount(source.size(), 0, byteCount);
            if (byteCount > bytesRemaining) {
                throw new ProtocolException("expected " + bytesRemaining
                        + " bytes but received " + byteCount);
            }
            sink.write(source, byteCount);
            bytesRemaining -= byteCount;
        }

        @Override
        public void flush() throws IOException {
            if (closed) return;
            sink.flush();
        }

        @Override
        public void close() throws IOException {
            if (closed) return;
            closed = true;
            if (bytesRemaining > 0) throw new ProtocolException("unexpected end of stream");
            detachTimeout(timeout);
            state = STATE_READ_RESPONSE_HEADERS;
        }
    }

    private final class ChunkedSink implements Sink {

        private final ForwardingTimeout timeout = new ForwardingTimeout(sink.timeout());
        private boolean closed;

        ChunkedSink() {
        }

        @Override
        public Timeout timeout() {
            return timeout;
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            if (closed) throw new IllegalStateException("closed");
            if (byteCount == 0) return;

            sink.writeHexadecimalUnsignedLong(byteCount);
            sink.writeUtf8("\r\n");
            sink.write(source, byteCount);
            sink.writeUtf8("\r\n");
        }

        @Override
        public synchronized void flush() throws IOException {
            if (closed) return;
            sink.flush();
        }

        @Override
        public synchronized void close() throws IOException {
            if (closed) return;
            closed = true;
            sink.writeUtf8("0\r\n\r\n");
            detachTimeout(timeout);
            state = STATE_READ_RESPONSE_HEADERS;
        }
    }

    private abstract class AbstractSource implements Source {

        protected final ForwardingTimeout timeout = new ForwardingTimeout(source.timeout());
        protected boolean closed;
        protected long bytesRead = 0;

        @Override
        public Timeout timeout() {
            return timeout;
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            try {
                long read = source.read(sink, byteCount);
                if (read > 0) {
                    bytesRead += read;
                }
                return read;
            } catch (IOException e) {
                endOfInput(false, e);
                throw e;
            }
        }

        protected final void endOfInput(boolean reuseConnection, IOException e) throws IOException {
            if (state == STATE_CLOSED) return;
            if (state != STATE_READING_RESPONSE_BODY) throw new IllegalStateException("state: " + state);

            detachTimeout(timeout);

            state = STATE_CLOSED;
            if (streamAllocation != null) {
                streamAllocation.streamFinished(!reuseConnection, Http1Codec.this, bytesRead, e);
            }
        }
    }

    private class FixedLengthSource extends AbstractSource {

        private long bytesRemaining;

        FixedLengthSource(long length) throws IOException {
            bytesRemaining = length;
            if (bytesRemaining == 0) {
                endOfInput(true, null);
            }
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            if (closed) throw new IllegalStateException("closed");
            if (bytesRemaining == 0) return -1;

            long read = super.read(sink, Math.min(bytesRemaining, byteCount));
            if (read == -1) {
                ProtocolException e = new ProtocolException("unexpected end of stream");
                endOfInput(false, e); // The server didn't supply the promised content length.
                throw e;
            }

            bytesRemaining -= read;
            if (bytesRemaining == 0) {
                endOfInput(true, null);
            }
            return read;
        }

        @Override
        public void close() throws IOException {
            if (closed) return;

            if (bytesRemaining != 0 && !Internal.discard(this, DISCARD_STREAM_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                endOfInput(false, null);
            }

            closed = true;
        }
    }

    private class ChunkedSource extends AbstractSource {

        private static final long NO_CHUNK_YET = -1L;
        private final HttpUrl url;
        private long bytesRemainingInChunk = NO_CHUNK_YET;
        private boolean hasMoreChunks = true;

        ChunkedSource(HttpUrl url) {
            this.url = url;
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            if (closed) throw new IllegalStateException("closed");
            if (!hasMoreChunks) return -1;

            if (bytesRemainingInChunk == 0 || bytesRemainingInChunk == NO_CHUNK_YET) {
                readChunkSize();
                if (!hasMoreChunks) return -1;
            }

            long read = super.read(sink, Math.min(byteCount, bytesRemainingInChunk));
            if (read == -1) {
                ProtocolException e = new ProtocolException("unexpected end of stream");
                endOfInput(false, e); // The server didn't supply the promised chunk length.
                throw e;
            }
            bytesRemainingInChunk -= read;
            return read;
        }

        private void readChunkSize() throws IOException {
            // Read the suffix of the previous chunk.
            if (bytesRemainingInChunk != NO_CHUNK_YET) {
                source.readUtf8LineStrict();
            }
            try {
                bytesRemainingInChunk = source.readHexadecimalUnsignedLong();
                String extensions = source.readUtf8LineStrict().trim();
                if (bytesRemainingInChunk < 0 || (!extensions.isEmpty() && !extensions.startsWith(";"))) {
                    throw new ProtocolException("expected chunk size and optional extensions but was \""
                            + bytesRemainingInChunk + extensions + "\"");
                }
            } catch (NumberFormatException e) {
                throw new ProtocolException(e.getMessage());
            }
            if (bytesRemainingInChunk == 0L) {
                hasMoreChunks = false;
                HttpHeaders.receiveHeaders(client.cookieJar(), url, readHeaders());
                endOfInput(true, null);
            }
        }

        @Override
        public void close() throws IOException {
            if (closed) return;
            if (hasMoreChunks && !Internal.discard(this, DISCARD_STREAM_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                endOfInput(false, null);
            }
            closed = true;
        }
    }

    private class UnknownLengthSource extends AbstractSource {

        private boolean inputExhausted;

        UnknownLengthSource() {
        }

        @Override
        public long read(Buffer sink, long byteCount)
                throws IOException {
            if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            if (closed) throw new IllegalStateException("closed");
            if (inputExhausted) return -1;

            long read = super.read(sink, byteCount);
            if (read == -1) {
                inputExhausted = true;
                endOfInput(true, null);
                return -1;
            }
            return read;
        }

        @Override
        public void close() throws IOException {
            if (closed) return;
            if (!inputExhausted) {
                endOfInput(false, null);
            }
            closed = true;
        }
    }

}
