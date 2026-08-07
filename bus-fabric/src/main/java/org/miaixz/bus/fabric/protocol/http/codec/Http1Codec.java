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
package org.miaixz.bus.fabric.protocol.http.codec;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.*;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.NetworkTimeout;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * HTTP/1.1 codec bound to a selected network connection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http1Codec implements HttpCodec {

    /**
     * Connection-local codec activity independent from the public connection lifecycle.
     */
    private enum CodecState {

        /**
         * Ready for another request or response operation.
         */
        IDLE,

        /**
         * Encoding or decoding the current exchange.
         */
        BUSY,

        /**
         * Cancelled because the exchange or owning call was cancelled.
         */
        CANCELLED

    }

    /**
     * Most recently parsed response headers, reused when a server repeats an identical immutable header block.
     */
    private static volatile Headers cachedResponseHeaders;

    /**
     * Most recently parsed response media type.
     */
    private static volatile MediaCache cachedResponseMedia;

    /**
     * Bound connection.
     */
    private final Connection connection;

    /**
     * Sink over the bound connection.
     */
    private final Sink sink;

    /**
     * Reusable request-line, header, and chunk-framing buffer.
     */
    private final Buffer writeBuffer;

    /**
     * Reader over the bound connection.
     */
    private final NetworkReader reader;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<CodecState> state;

    /**
     * Whether the current exchange has not yet received an HTTP status line.
     */
    private volatile boolean beforeResponse;

    /**
     * Response body completion flag.
     */
    private final AtomicBoolean bodyComplete;

    /**
     * Response connection-close flag.
     */
    private final AtomicBoolean connectionClose;

    /**
     * Response trailers.
     */
    private final AtomicReference<Headers> trailers;

    /**
     * Reusable lazy trailer view retained with the connection-local codec.
     */
    private final Supplier<Headers> trailerSupplier;

    /**
     * Complete timeout policy for the current request and its response body.
     */
    private volatile org.miaixz.bus.fabric.Timeout timeout;

    /**
     * Creates an HTTP/1 codec.
     *
     * @param connection bound connection
     */
    public Http1Codec(final Connection connection) {
        this.connection = require(connection, "Network connection");
        this.sink = this.connection.sink();
        this.writeBuffer = new Buffer();
        this.reader = new NetworkReader(connection);
        this.state = new AtomicReference<>(CodecState.IDLE);
        this.bodyComplete = new AtomicBoolean(true);
        this.connectionClose = new AtomicBoolean();
        this.trailers = new AtomicReference<>(Headers.empty());
        this.trailerSupplier = this::trailers;
        this.timeout = org.miaixz.bus.fabric.Timeout.defaults();
    }

    /**
     * Writes request metadata, headers, and body.
     *
     * @param request HTTP request whose metadata and body are written
     */
    @Override
    public void writeRequest(final HttpRequest request) {
        final HttpRequest current = require(request, "HTTP request");
        beforeResponse = true;
        timeout = current.timeout();
        state.set(CodecState.BUSY);
        writeHeaders(current);
        if (current.body().length() == Normal._0) {
            state.set(CodecState.IDLE);
            return;
        }
        try (Sink sink = createRequestBody(current)) {
            writePayload(sink, current.body().payload());
            sink.flush();
        } catch (final IOException e) {
            throw new SocketException("Unable to write HTTP request body", e);
        }
        state.set(CodecState.IDLE);
    }

    /**
     * Writes the HTTP/1 request line and headers.
     *
     * @param request HTTP request supplying the request line and headers
     */
    public void writeHeaders(final HttpRequest request) {
        final HttpRequest current = require(request, "HTTP request");
        Http1Framing.validate(current.headers());
        final Buffer encoded = writeBuffer;
        encoded.writeUtf8(HttpLine.request(current)).writeUtf8(Symbol.CRLF);
        for (int index = Normal._0; index < current.headers().size(); index++) {
            encoded.writeUtf8(HttpLine.header(current.headers().name(index), current.headers().value(index)))
                    .writeUtf8(Symbol.CRLF);
        }
        encoded.writeUtf8(Symbol.CRLF);
        write(encoded);
    }

    /**
     * Creates a request stream sink for the current request framing.
     *
     * @param request HTTP request whose framing determines the sink
     * @return stream sink
     */
    public Sink createRequestBody(final HttpRequest request) {
        final HttpRequest current = require(request, "HTTP request");
        Http1Framing.validate(current.headers());
        final long declared = Http1Framing.declaredLength(current.headers());
        if (Http1Framing.chunked(current.headers())) {
            return new ChunkedSink(this);
        }
        if (declared >= Normal._0) {
            if (current.body().length() >= Normal._0 && current.body().length() != declared) {
                throw new ProtocolException("HTTP request body length mismatch");
            }
            return new FixedSink(this, declared);
        }
        return new UnknownSink(this);
    }

    /**
     * Reads response status, headers, and a streaming body.
     *
     * @param request originating request used to interpret the response
     * @return response
     */
    @Override
    public HttpResponse readResponse(final HttpRequest request) {
        final HttpRequest current = require(request, "HTTP request");
        timeout = current.timeout();
        state.set(CodecState.BUSY);
        trailers.set(Headers.empty());
        beforeResponse = true;
        String line = reader.readLine(timeout.read());
        beforeResponse = false;
        int code = Http1Parser.status(line);
        Headers headers = readHeaders();
        while (code >= Http.Status.CONTINUE && code < Http.Status.OK && code != Http.Status.SWITCHING_PROTOCOLS) {
            line = reader.readLine(timeout.read());
            code = Http1Parser.status(line);
            headers = readHeaders();
        }
        final String message = Http1Parser.reason(line);
        final MediaType responseMedia = media(headers);
        final Source source = openResponseBody(current, code, headers, responseMedia);
        final Payload payload = payload(source);
        final PayloadBody body = payload.length() == Normal._0 ? PayloadBody.empty()
                : PayloadBody.of(payload, responseMedia);
        state.set(CodecState.IDLE);
        return HttpResponse.transport(current, code, message, headers, body, Protocol.HTTP_1_1, trailerSupplier);
    }

    /**
     * Returns response trailers.
     *
     * @return trailers
     */
    private Headers trailers() {
        return trailers.get();
    }

    /**
     * Stores response trailers.
     *
     * @param headers trailers
     */
    private void trailers(final Headers headers) {
        trailers.set(require(headers, "HTTP trailers"));
    }

    /**
     * Opens a response stream source.
     *
     * @param response response metadata used to select body framing
     * @return stream source
     */
    public Source openResponseBody(final HttpResponse response) {
        final HttpResponse current = require(response, "HTTP response");
        return openResponseBody(current.request(), current.code(), current.headers(), media(current.headers()));
    }

    /**
     * Opens a response body directly from parsed response metadata.
     *
     * @param request originating request used to determine body semantics
     * @param code    response status
     * @param headers response headers
     * @return response stream source
     */
    private Source openResponseBody(
            final HttpRequest request,
            final int code,
            final Headers headers,
            final MediaType responseMedia) {
        final long length = Http1Framing.response(request, code, headers).length();
        if (length == Normal._0) {
            bodyComplete.set(true);
            return new EmptySource(this);
        }
        bodyComplete.set(false);
        connectionClose.set(Http1Framing.connectionClose(request.headers()) || Http1Framing.connectionClose(headers));
        final InputStream input;
        if (Http1Framing.chunked(headers)) {
            input = new ChunkedInputStream(reader, timeout, this);
        } else if (length >= Normal._0) {
            input = new FixedInputStream(reader, timeout, length, this);
        } else {
            input = new UnknownInputStream(reader, timeout, this);
            connectionClose.set(true);
        }
        return new NetworkSource(input, length, responseMedia, this);
    }

    /**
     * Cancels this codec.
     */
    @Override
    public void cancel() {
        final CodecState previous = state.getAndSet(CodecState.CANCELLED);
        if (previous != CodecState.CANCELLED) {
            connection.close();
        }
    }

    /**
     * Returns whether the connection can be reused.
     *
     * @return true when reusable
     */
    @Override
    public boolean reusable() {
        return state.get() == CodecState.IDLE && bodyComplete.get() && !connectionClose.get() && connection.healthy();
    }

    /**
     * Returns whether the current exchange is still before its response status line.
     *
     * @return {@code true} until the response status line is read
     */
    @Override
    public boolean beforeResponse() {
        return beforeResponse;
    }

    /**
     * Marks response body complete.
     */
    private void completeBody() {
        bodyComplete.set(true);
    }

    /**
     * Marks response body closed before completion.
     */
    private void abandonBody() {
        bodyComplete.set(false);
        connectionClose.set(true);
    }

    /**
     * Writes ASCII text.
     *
     * @param value ASCII text to encode and write
     */
    private void writeText(final String value) {
        write(writeBuffer.writeUtf8(value));
    }

    /**
     * Writes a whole buffer to the connection.
     *
     * @param source buffer whose remaining bytes are written
     */
    private void write(final Buffer source) {
        final Buffer payload = require(source, "HTTP write buffer");
        configureTimeout(sink.timeout(), timeout.write());
        while (payload.size() > Normal._0) {
            final long before = payload.size();
            try {
                sink.write(payload, payload.size());
            } catch (final IOException e) {
                throw new SocketException("HTTP write failed", e);
            }
            if (payload.size() == before) {
                Thread.yield();
            }
        }
    }

    /**
     * Writes a payload into a core.io sink.
     *
     * @param sink    destination receiving payload bytes
     * @param payload payload whose bytes are copied to the sink
     * @throws IOException when reading or writing fails
     */
    private static void writePayload(final Sink sink, final Payload payload) throws IOException {
        require(sink, "HTTP body sink");
        require(payload, "Payload");
        final Buffer buffer = new Buffer();
        try (Source input = payload.source()) {
            long read = input.read(buffer, Normal._8192);
            while (read != Normal.__1) {
                sink.write(buffer, read);
                read = input.read(buffer, Normal._8192);
            }
        }
    }

    /**
     * Casts a response source to its payload view.
     *
     * @param source response source to expose as a payload
     * @return payload
     */
    private static Payload payload(final Source source) {
        if (source instanceof Payload payload) {
            return payload;
        }
        throw new InternalException("HTTP response source is not payload-backed");
    }

    /**
     * Reads response headers.
     *
     * @return headers
     */
    private Headers readHeaders() {
        final Headers cached = cachedResponseHeaders;
        Headers.Builder builder = cached == null ? Headers.builder() : null;
        int index = Normal._0;
        int rawLength = reader.peekHeaderLine(timeout.read());
        int lineLength = reader.headerContentLength(rawLength);
        while (lineLength != Normal._0) {
            final int colon = reader.headerIndexOf(Symbol.C_COLON, lineLength);
            if (colon <= Normal._0) {
                throw new ProtocolException("Invalid HTTP header line");
            }
            final int valueStart = reader.headerValueStart(colon + Normal._1, lineLength);
            final int valueEnd = reader.headerValueEnd(valueStart, lineLength);
            if (builder == null
                    && (index >= cached.size() || !reader.headerMatches(cached.name(index), Normal._0, colon)
                            || !reader.headerMatches(cached.value(index), valueStart, valueEnd))) {
                builder = Headers.builder();
                for (int prior = Normal._0; prior < index; prior++) {
                    builder.add(cached.name(prior), cached.value(prior));
                }
            }
            if (builder != null) {
                final String line = reader.consumeHeaderLine();
                Http1Parser.addHeader(builder, line, colon, valueStart, valueEnd);
            } else {
                reader.skipHeaderLine(rawLength);
            }
            index++;
            rawLength = reader.peekHeaderLine(timeout.read());
            lineLength = reader.headerContentLength(rawLength);
        }
        reader.skipHeaderLine(rawLength);
        if (builder == null && index == cached.size()) {
            return cached;
        }
        if (builder == null) {
            builder = Headers.builder();
            for (int prior = Normal._0; prior < index; prior++) {
                builder.add(cached.name(prior), cached.value(prior));
            }
        }
        return canonicalResponseHeaders(Http1Parser.headers(builder.build()));
    }

    /**
     * Reuses the most recent immutable response-header block when every ordered name/value pair is identical. Parsing
     * and framing validation still run for every response before this allocation optimization is applied.
     *
     * @param parsed validated response headers
     * @return parsed headers or the identical cached instance
     */
    private static Headers canonicalResponseHeaders(final Headers parsed) {
        final Headers cached = cachedResponseHeaders;
        if (cached != null && sameHeaders(cached, parsed)) {
            return cached;
        }
        cachedResponseHeaders = parsed;
        return parsed;
    }

    /**
     * Compares ordered header pairs without materializing map or list views.
     *
     * @param left  first immutable headers
     * @param right second immutable headers
     * @return true when all ordered names and values are equal
     */
    private static boolean sameHeaders(final Headers left, final Headers right) {
        final int size = left.size();
        if (size != right.size()) {
            return false;
        }
        for (int index = Normal._0; index < size; index++) {
            if (!left.name(index).equals(right.name(index)) || !left.value(index).equals(right.value(index))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parses response media.
     *
     * @param headers HTTP headers containing the response media type
     * @return media
     */
    private static MediaType media(final Headers headers) {
        final String contentType = headers.get(Http.Header.CONTENT_TYPE);
        if (contentType == null) {
            return MediaType.APPLICATION_OCTET_STREAM_TYPE;
        }
        final MediaCache cached = cachedResponseMedia;
        if (cached != null && cached.value.equals(contentType)) {
            return cached.media;
        }
        final MediaType parsed = MediaType.parse(contentType);
        cachedResponseMedia = new MediaCache(contentType, parsed);
        return parsed;
    }

    /**
     * Cached immutable response media parse.
     *
     * @param value original Content-Type header value
     * @param media parsed immutable media type
     */
    private record MediaCache(String value, MediaType media) {
    }

    /**
     * Applies a duration to a core.io timeout policy.
     *
     * @param timeoutPolicy timeout policy
     * @param duration      duration applied to every timeout phase
     */
    private static void configureTimeout(final Timeout timeoutPolicy, final Duration duration) {
        NetworkTimeout.apply(timeoutPolicy, duration);
    }

    /**
     * Chooses the bounded timeout used by an early-close drain.
     *
     * @param configured configured response-body read timeout
     * @return positive drain timeout capped at the HTTP/1 safety limit
     */
    private static Duration drainTimeout(final Duration configured) {
        return configured == null || configured.isZero() || configured.isNegative()
                || configured.compareTo(Builder.HTTP1_CODEC_MAX_DRAIN_DURATION) > Normal._0
                        ? Builder.HTTP1_CODEC_MAX_DRAIN_DURATION
                        : configured;
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  field name
     * @param <T>   value type
     * @return the validated reference
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Network reader that preserves the response stream position.
     */
    private static final class NetworkReader {

        /**
         * Buffered source over the bound connection.
         */
        private final BufferSource source;

        /**
         * Creates a reader.
         *
         * @param connection network connection supplying response bytes
         */
        private NetworkReader(final Connection connection) {
            final Connection current = require(connection, "Network connection");
            this.source = IoKit.buffer(current.source());
        }

        /**
         * Reads one byte.
         *
         * @param timeout maximum duration allowed for the read
         * @return byte or EOF
         */
        private int read(final Duration timeout) {
            while (true) {
                configureTimeout(source.timeout(), timeout);
                try {
                    return source.readByte() & Builder.UNSIGNED_BYTE_MASK;
                } catch (final EOFException e) {
                    return Normal.__1;
                } catch (final IOException e) {
                    throw new SocketException("HTTP read failed", e);
                }
            }
        }

        /**
         * Reads bytes into a target array.
         *
         * @param target  destination byte array
         * @param offset  first destination index to fill
         * @param length  maximum number of bytes to read
         * @param timeout maximum duration allowed for the read
         * @return read count or EOF
         */
        private int read(final byte[] target, final int offset, final int length, final Duration timeout) {
            if (length == Normal._0) {
                return Normal._0;
            }
            while (true) {
                configureTimeout(source.timeout(), timeout);
                final int read;
                try {
                    read = source.read(target, offset, length);
                } catch (final IOException e) {
                    throw new SocketException("HTTP read failed", e);
                }
                if (read < Normal._0) {
                    return Normal.__1;
                }
                if (read == Normal._0) {
                    Thread.yield();
                    continue;
                }
                return read;
            }
        }

        /**
         * Fills one known-length body array through the timeout-aware buffered source. Header read-ahead bytes are
         * consumed before subsequent socket reads.
         */
        private void readFixed(final byte[] target, final int offset, final int length, final Duration timeout) {
            int written = Normal._0;
            final Buffer buffered = source.getBuffer();
            if (buffered.size() > Normal._0) {
                final int count = (int) Math.min(length, buffered.size());
                buffered.read(target, offset, count);
                written = count;
            }
            if (written == length) {
                return;
            }
            configureTimeout(source.timeout(), timeout);
            while (written < length) {
                final int read;
                try {
                    read = source.read(target, offset + written, length - written);
                } catch (final IOException e) {
                    throw new SocketException("HTTP fixed body read failed", e);
                }
                if (read < Normal._0) {
                    throw new SocketException("HTTP fixed body reached EOF");
                }
                if (read == Normal._0) {
                    Thread.yield();
                    continue;
                }
                written += read;
            }
        }

        /**
         * Reads a CRLF-terminated line without the terminator.
         *
         * @param timeout maximum duration allowed while reading the line
         * @return line
         */
        private String readLine(final Duration timeout) {
            configureTimeout(source.timeout(), timeout);
            try {
                return source.readUtf8LineStrict(Normal._8192);
            } catch (final EOFException e) {
                throw new SocketException("HTTP stream reached EOF", e);
            } catch (final IOException e) {
                throw new SocketException("HTTP read failed", e);
            }
        }

        /**
         * Returns the raw byte count before LF while leaving the header line buffered.
         */
        private int peekHeaderLine(final Duration timeout) {
            configureTimeout(source.timeout(), timeout);
            try {
                final long newline = source.indexOf((byte) Symbol.C_LF, Normal._0, Normal._8192 + Normal._1);
                if (newline < Normal._0) {
                    throw new ProtocolException("HTTP response header line exceeds 8192 bytes or reached EOF");
                }
                return (int) newline;
            } catch (final IOException e) {
                throw new SocketException("HTTP read failed", e);
            }
        }

        /**
         * Returns the logical line length without a trailing CR.
         */
        private int headerContentLength(final int rawLength) {
            return rawLength > Normal._0 && source.getBuffer().getByte(rawLength - Normal._1) == Symbol.C_CR
                    ? rawLength - Normal._1
                    : rawLength;
        }

        /**
         * Finds a byte in the currently buffered header line.
         */
        private int headerIndexOf(final int value, final int length) {
            final Buffer buffer = source.getBuffer();
            for (int index = Normal._0; index < length; index++) {
                if ((buffer.getByte(index) & Builder.UNSIGNED_BYTE_MASK) == value) {
                    return index;
                }
            }
            return Normal.__1;
        }

        /**
         * Finds the first non-whitespace header value byte.
         */
        private int headerValueStart(final int start, final int length) {
            final Buffer buffer = source.getBuffer();
            int index = start;
            while (index < length && (buffer.getByte(index) & Builder.UNSIGNED_BYTE_MASK) <= Symbol.C_SPACE) {
                index++;
            }
            return index;
        }

        /**
         * Finds the exclusive final non-whitespace header value byte.
         */
        private int headerValueEnd(final int start, final int length) {
            final Buffer buffer = source.getBuffer();
            int index = length;
            while (index > start
                    && (buffer.getByte(index - Normal._1) & Builder.UNSIGNED_BYTE_MASK) <= Symbol.C_SPACE) {
                index--;
            }
            return index;
        }

        /**
         * Compares a buffered ASCII span with a cached header string.
         */
        private boolean headerMatches(final String value, final int start, final int end) {
            if (value.length() != end - start) {
                return false;
            }
            final Buffer buffer = source.getBuffer();
            for (int offset = Normal._0; offset < value.length(); offset++) {
                if (value.charAt(offset) != (buffer.getByte(start + offset) & Builder.UNSIGNED_BYTE_MASK)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Materializes and consumes the currently buffered header line after a cache miss.
         */
        private String consumeHeaderLine() {
            try {
                return source.readUtf8LineStrict(Normal._8192);
            } catch (final EOFException e) {
                throw new SocketException("HTTP stream reached EOF", e);
            } catch (final IOException e) {
                throw new SocketException("HTTP read failed", e);
            }
        }

        /**
         * Consumes a cached header line and its LF delimiter without decoding.
         */
        private void skipHeaderLine(final int rawLength) {
            try {
                source.skip(rawLength + Normal._1);
            } catch (final IOException e) {
                throw new SocketException("HTTP read failed", e);
            }
        }

        /**
         * Consumes a CRLF sequence.
         *
         * @param timeout maximum duration allowed while consuming the delimiter
         */
        private void expectCrlf(final Duration timeout) {
            final int cr = read(timeout);
            final int lf = read(timeout);
            if (cr != Symbol.C_CR || lf != Symbol.C_LF) {
                throw new ProtocolException("Invalid HTTP chunk delimiter");
            }
        }

    }

    /**
     * Base body sink.
     */
    private abstract static class AbstractSink implements Sink {

        /**
         * Codec.
         */
        final Http1Codec codec;

        /**
         * Written byte count.
         */
        long written;

        /**
         * Closed flag.
         */
        private boolean closed;

        /**
         * Creates a sink.
         *
         * @param codec owning HTTP/1 codec used for network writes
         */
        AbstractSink(final Http1Codec codec) {
            this.codec = require(codec, "HTTP codec");
        }

        /**
         * Returns written bytes.
         *
         * @return written bytes
         */
        public long written() {
            return written;
        }

        /**
         * Flushes this sink.
         */
        @Override
        public void flush() {
            ensureOpen();
        }

        /**
         * Returns sink timeout.
         *
         * @return timeout
         */
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        /**
         * Writes buffered bytes to the connection and consumes them from the source buffer.
         *
         * @param source    source buffer
         * @param byteCount byte count
         * @throws IOException when consuming source bytes fails
         */
        void writeToConnection(final Buffer source, final long byteCount) throws IOException {
            if (byteCount == source.size()) {
                codec.write(source);
                return;
            }
            final Buffer payload = new Buffer();
            payload.write(source, byteCount);
            codec.write(payload);
        }

        /**
         * Closes the sink.
         */
        @Override
        public void close() {
            closed = true;
        }

        /**
         * Ensures sink is open.
         */
        void ensureOpen() {
            if (closed) {
                throw new StatefulException("HTTP body sink is closed");
            }
        }

    }

    /**
     * Fixed-length body sink.
     */
    private static final class FixedSink extends AbstractSink {

        /**
         * Expected length.
         */
        private final long expected;

        /**
         * Creates a sink.
         *
         * @param codec    owning HTTP/1 codec used for network writes
         * @param expected expected length
         */
        private FixedSink(final Http1Codec codec, final long expected) {
            super(codec);
            this.expected = expected;
        }

        /**
         * Writes fixed bytes.
         *
         * @param source    buffer containing the fixed-length body bytes
         * @param byteCount number of bytes to consume from the source
         */
        @Override
        public void write(final Buffer source, final long byteCount) throws IOException {
            require(source, "Source buffer");
            ensureOpen();
            if (byteCount < Normal._0) {
                throw new ProtocolException("HTTP fixed body byte count is negative");
            }
            if (byteCount > expected - written) {
                throw new ProtocolException("HTTP fixed body exceeds Content-Length");
            }
            writeToConnection(source, byteCount);
            written += byteCount;
        }

        /**
         * Closes the sink.
         */
        @Override
        public void close() {
            if (written != expected) {
                throw new ProtocolException("HTTP fixed body length mismatch");
            }
            super.close();
        }

    }

    /**
     * Chunked body sink.
     */
    private static final class ChunkedSink extends AbstractSink {

        /**
         * Creates a sink.
         *
         * @param codec owning HTTP/1 codec used for network writes
         */
        private ChunkedSink(final Http1Codec codec) {
            super(codec);
        }

        /**
         * Writes a chunk.
         *
         * @param source    buffer containing one chunk payload
         * @param byteCount number of chunk payload bytes to consume
         */
        @Override
        public void write(final Buffer source, final long byteCount) throws IOException {
            require(source, "Source buffer");
            ensureOpen();
            if (byteCount < Normal._0) {
                throw new ProtocolException("HTTP chunk byte count is negative");
            }
            if (byteCount == Normal._0) {
                return;
            }
            codec.writeText(Long.toHexString(byteCount) + Symbol.CRLF);
            writeToConnection(source, byteCount);
            codec.writeText(Symbol.CRLF);
            written += byteCount;
        }

        /**
         * Closes the sink with the terminal chunk.
         */
        @Override
        public void close() {
            ensureOpen();
            codec.writeText(Normal._0 + Symbol.CRLF + Symbol.CRLF);
            super.close();
        }

    }

    /**
     * Unknown-length body sink.
     */
    private static final class UnknownSink extends AbstractSink {

        /**
         * Creates a sink.
         *
         * @param codec owning HTTP/1 codec used for network writes
         */
        private UnknownSink(final Http1Codec codec) {
            super(codec);
        }

        /**
         * Writes raw bytes.
         *
         * @param source    buffer containing unframed body bytes
         * @param byteCount number of bytes to consume from the source
         */
        @Override
        public void write(final Buffer source, final long byteCount) throws IOException {
            require(source, "Source buffer");
            ensureOpen();
            if (byteCount < Normal._0) {
                throw new ProtocolException("HTTP body byte count is negative");
            }
            writeToConnection(source, byteCount);
            written += byteCount;
        }

    }

    /**
     * Empty body source.
     */
    private static final class EmptySource implements Source, Payload {

        /**
         * Codec.
         */
        private final Http1Codec codec;

        /**
         * Creates an empty source.
         *
         * @param codec owning HTTP/1 codec associated with the empty body
         */
        private EmptySource(final Http1Codec codec) {
            this.codec = require(codec, "HTTP codec");
            this.codec.completeBody();
        }

        /**
         * Returns zero length.
         *
         * @return zero
         */
        @Override
        public long length() {
            return 0;
        }

        /**
         * Returns this empty body as its own source.
         *
         * @return empty source
         */
        @Override
        public Source source() {
            return this;
        }

        /**
         * Reads from the empty source.
         *
         * @param sink      destination buffer
         * @param byteCount maximum bytes to read
         * @return zero for zero-length reads, otherwise -1
         */
        @Override
        public long read(final Buffer sink, final long byteCount) {
            require(sink, "Target buffer");
            if (byteCount < Normal._0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            }
            return byteCount == Normal._0 ? Normal._0 : Normal.__1;
        }

        /**
         * Returns source timeout.
         *
         * @return timeout
         */
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        /**
         * Returns empty bytes.
         *
         * @return bytes
         */
        @Override
        public byte[] bytes() {
            return Normal.EMPTY_BYTE_ARRAY;
        }

        /**
         * Returns empty bytes after validating the materialize threshold.
         *
         * @param maxBytes maximum bytes to materialize
         * @return empty bytes
         */
        @Override
        public byte[] bytes(final long maxBytes) {
            Payload.validateMaterializeMaxBytes(maxBytes);
            return Normal.EMPTY_BYTE_ARRAY;
        }

        /**
         * Returns empty text.
         *
         * @param charset charset validated for API consistency
         * @return text
         */
        @Override
        public String text(final Charset charset) {
            require(charset, "Charset");
            return Normal.EMPTY;
        }

        /**
         * Returns empty text after validating charset and materialize threshold.
         *
         * @param charset  charset validated for API consistency
         * @param maxBytes maximum bytes to materialize
         * @return empty text
         */
        @Override
        public String text(final Charset charset, final long maxBytes) {
            require(charset, "Charset");
            Payload.validateMaterializeMaxBytes(maxBytes);
            return Normal.EMPTY;
        }

        /**
         * Returns repeatability.
         *
         * @return true
         */
        @Override
        public boolean repeatable() {
            return true;
        }

        /**
         * Closes this source.
         */
        @Override
        public void close() {
            codec.completeBody();
        }

    }

    /**
     * Network response body source.
     */
    private static final class NetworkSource implements Source, Payload {

        /**
         * Input stream.
         */
        private final InputStream input;

        /**
         * Body length.
         */
        private final long length;

        /**
         * Codec.
         */
        private final Http1Codec codec;

        /**
         * Reused transfer scratch; avoids allocating one array per body read.
         */
        private byte[] scratch;

        /**
         * Open flag.
         */
        private boolean opened;

        /**
         * Current stream.
         */
        private InputStream current;

        /**
         * Creates a source.
         *
         * @param input  stream providing the response body bytes
         * @param length declared body length, or {@code -1} when unknown
         * @param media  parsed response media type, or {@code null}
         * @param codec  owning HTTP/1 codec controlling materialization limits
         */
        private NetworkSource(final InputStream input, final long length, final MediaType media,
                final Http1Codec codec) {
            this.input = require(input, "Input stream");
            this.length = length;
            require(media, "MediaType");
            this.codec = require(codec, "HTTP codec");
        }

        /**
         * Returns length.
         *
         * @return length
         */
        @Override
        public long length() {
            return length;
        }

        /**
         * Opens this one-shot network body as a source.
         *
         * @return network source
         */
        @Override
        public Source source() {
            open();
            return this;
        }

        /**
         * Reads bytes from the HTTP response body stream.
         *
         * @param sink      destination buffer
         * @param byteCount maximum bytes to read
         * @return bytes read, or -1 at end of stream
         */
        @Override
        public long read(final Buffer sink, final long byteCount) {
            require(sink, "Target buffer");
            if (byteCount < Normal._0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            }
            if (byteCount == Normal._0) {
                return Normal._0;
            }
            final InputStream source = current == null ? open() : current;
            byte[] transfer = scratch;
            if (transfer == null) {
                transfer = new byte[Normal._8192];
                scratch = transfer;
            }
            final int requested = (int) Math.min(byteCount, transfer.length);
            try {
                final int read = source.read(transfer, Normal._0, requested);
                if (read > Normal._0) {
                    sink.write(transfer, Normal._0, read);
                }
                return read;
            } catch (final IOException e) {
                throw new SocketException("Unable to read HTTP response body", e);
            }
        }

        /**
         * Returns source timeout.
         *
         * @return timeout
         */
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        /**
         * Opens the underlying network input once.
         *
         * @return active input stream
         */
        private InputStream open() {
            if (opened) {
                throw new StatefulException("HTTP response body can only be opened once");
            }
            opened = true;
            current = input;
            return current;
        }

        /**
         * Reads all bytes.
         *
         * @return bytes
         */
        @Override
        public byte[] bytes() {
            return bytes(Normal.MEBI_64);
        }

        /**
         * Materializes the network body using an explicit threshold.
         *
         * @param maxBytes maximum bytes to materialize
         * @return materialized bytes
         */
        @Override
        public byte[] bytes(final long maxBytes) {
            Payload.validateMaterializeMaxBytes(maxBytes);
            if (length > maxBytes) {
                throw Payload.materializeExceeded(length, maxBytes, "Http1Codec.NetworkSource.bytes(long)");
            }
            if (length >= Normal.LONG_ZERO && length <= Integer.MAX_VALUE) {
                final byte[] result = new byte[(int) length];
                final InputStream source = open();
                if (source instanceof FixedInputStream fixed) {
                    fixed.readFully(result);
                    return result;
                }
                int offset = Normal._0;
                try {
                    while (offset < result.length) {
                        final int read = source.read(result, offset, result.length - offset);
                        if (read == Normal.__1) {
                            throw new InternalException("HTTP response body ended before declared length");
                        }
                        if (read > Normal._0) {
                            offset += read;
                        }
                    }
                    return result;
                } catch (final IOException e) {
                    throw new SocketException("Unable to materialize HTTP response body", e);
                }
            }
            return Payload.materialize(this, maxBytes, "Http1Codec.NetworkSource.bytes(long)");
        }

        /**
         * Reads text.
         *
         * @param charset charset used to decode materialized body bytes
         * @return text
         */
        @Override
        public String text(final Charset charset) {
            return text(charset, Normal.MEBI_64);
        }

        /**
         * Materializes and decodes the network body using an explicit threshold.
         *
         * @param charset  charset used to decode materialized body bytes
         * @param maxBytes maximum bytes to materialize
         * @return decoded text
         */
        @Override
        public String text(final Charset charset, final long maxBytes) {
            require(charset, "Charset");
            return new String(bytes(maxBytes), charset);
        }

        /**
         * Returns repeatability.
         *
         * @return false
         */
        @Override
        public boolean repeatable() {
            return false;
        }

        /**
         * Closes current source.
         */
        @Override
        public void close() {
            boolean drained = false;
            if (input instanceof FixedInputStream fixed) {
                drained = fixed.drain();
            } else if (input instanceof ChunkedInputStream chunked) {
                drained = chunked.drain();
            }
            try {
                input.close();
            } catch (final IOException e) {
                throw new SocketException("Unable to close HTTP response body", e);
            }
            if (!drained) {
                codec.abandonBody();
            }
        }

    }

    /**
     * Fixed-length response body stream.
     */
    private static final class FixedInputStream extends InputStream {

        /**
         * Reader.
         */
        private final NetworkReader reader;

        /**
         * Timeout.
         */
        private final org.miaixz.bus.fabric.Timeout timeout;

        /**
         * Codec.
         */
        private final Http1Codec codec;

        /**
         * Remaining bytes.
         */
        private long remaining;

        /**
         * Closed flag.
         */
        private boolean closed;

        /**
         * Creates a stream.
         *
         * @param reader  network reader supplying the fixed-length body
         * @param timeout maximum duration allowed for each network read
         * @param length  exact number of body bytes to expose
         * @param codec   owning codec notified when the stream completes
         */
        private FixedInputStream(final NetworkReader reader, final org.miaixz.bus.fabric.Timeout timeout,
                final long length, final Http1Codec codec) {
            this.reader = require(reader, "Network reader");
            this.timeout = require(timeout, "Timeout");
            this.remaining = length;
            this.codec = require(codec, "HTTP codec");
        }

        /**
         * Reads one byte.
         *
         * @return byte or EOF
         * @throws IOException when reading fails
         */
        @Override
        public int read() throws IOException {
            if (closed || remaining == Normal._0) {
                if (remaining == Normal._0) {
                    codec.completeBody();
                }
                return Normal.__1;
            }
            final int value = reader.read(timeout.read());
            if (value < Normal._0) {
                throw new SocketException("HTTP fixed body reached EOF");
            }
            remaining--;
            if (remaining == Normal._0) {
                codec.completeBody();
            }
            return value;
        }

        /**
         * Reads bytes.
         *
         * @param bytes  destination byte array
         * @param offset first destination index to fill
         * @param length maximum number of bytes to read
         * @return read count or EOF
         */
        @Override
        public int read(final byte[] bytes, final int offset, final int length) {
            if (closed) {
                return Normal.__1;
            }
            if (length == Normal._0) {
                return Normal._0;
            }
            if (remaining == Normal._0) {
                codec.completeBody();
                return Normal.__1;
            }
            final int limit = (int) Math.min(length, remaining);
            final int read = reader.read(bytes, offset, limit, timeout.read());
            if (read < Normal._0) {
                throw new SocketException("HTTP fixed body reached EOF");
            }
            remaining -= read;
            if (remaining == Normal._0) {
                codec.completeBody();
            }
            return read;
        }

        /**
         * Closes the stream.
         */
        @Override
        public void close() {
            closed = true;
            if (remaining > Normal._0) {
                codec.abandonBody();
            }
        }

        /**
         * Drains a small fixed body under the strict early-close byte/time bounds.
         *
         * @return {@code true} when the remaining fixed body was fully consumed
         */
        private boolean drain() {
            if (remaining == Normal._0) {
                return true;
            }
            if (remaining > Builder.BYTES_64_KIB) {
                return false;
            }
            final byte[] buffer = new byte[(int) Math.min(remaining, Normal._8192)];
            final Duration bounded = drainTimeout(timeout.read());
            while (remaining > Normal._0) {
                final int read = reader.read(buffer, Normal._0, (int) Math.min(buffer.length, remaining), bounded);
                if (read < Normal._0) {
                    return false;
                }
                remaining -= read;
            }
            codec.completeBody();
            return true;
        }

        /**
         * Reads the complete remaining fixed body into its final materialized array.
         */
        private void readFully(final byte[] target) {
            if (closed || target.length != remaining) {
                throw new StatefulException("HTTP fixed body is not positioned for direct materialization");
            }
            reader.readFixed(target, Normal._0, target.length, timeout.read());
            remaining = Normal._0;
            codec.completeBody();
        }

    }

    /**
     * Chunked response body stream.
     */
    private static final class ChunkedInputStream extends InputStream {

        /**
         * Reader.
         */
        private final NetworkReader reader;

        /**
         * Timeout.
         */
        private final org.miaixz.bus.fabric.Timeout timeout;

        /**
         * Codec.
         */
        private final Http1Codec codec;

        /**
         * Remaining bytes in the current chunk.
         */
        private int remaining;

        /**
         * Done flag.
         */
        private boolean done;

        /**
         * Closed flag.
         */
        private boolean closed;

        /**
         * Creates a chunked stream.
         *
         * @param reader  network reader supplying chunk frames
         * @param timeout maximum duration allowed for each network read
         * @param codec   owning codec notified when the stream completes
         */
        private ChunkedInputStream(final NetworkReader reader, final org.miaixz.bus.fabric.Timeout timeout,
                final Http1Codec codec) {
            this.reader = require(reader, "Network reader");
            this.timeout = require(timeout, "Timeout");
            this.codec = require(codec, "HTTP codec");
        }

        /**
         * Reads one byte.
         *
         * @return byte or EOF
         * @throws IOException when reading fails
         */
        @Override
        public int read() throws IOException {
            if (closed || done) {
                return Normal.__1;
            }
            if (remaining == Normal._0) {
                readChunkSize();
                if (done) {
                    return Normal.__1;
                }
            }
            final int value = reader.read(timeout.read());
            if (value < Normal._0) {
                throw new SocketException("HTTP chunked body reached EOF");
            }
            if (--remaining == Normal._0) {
                reader.expectCrlf(timeout.read());
            }
            return value;
        }

        /**
         * Reads bytes.
         *
         * @param bytes  destination byte array
         * @param offset first destination index to fill
         * @param length maximum number of decoded chunk bytes to read
         * @return read count or EOF
         */
        @Override
        public int read(final byte[] bytes, final int offset, final int length) {
            if (closed || done) {
                return Normal.__1;
            }
            if (length == Normal._0) {
                return Normal._0;
            }
            if (remaining == Normal._0) {
                readChunkSize();
                if (done) {
                    return Normal.__1;
                }
            }
            final int limit = Math.min(length, remaining);
            final int read = reader.read(bytes, offset, limit, timeout.read());
            if (read < Normal._0) {
                throw new SocketException("HTTP chunked body reached EOF");
            }
            remaining -= read;
            if (remaining == Normal._0) {
                reader.expectCrlf(timeout.read());
            }
            return read;
        }

        /**
         * Closes the stream.
         */
        @Override
        public void close() {
            closed = true;
            if (!done) {
                codec.abandonBody();
            }
        }

        /**
         * Reads the next chunk size.
         */
        private void readChunkSize() {
            final String line = reader.readLine(timeout.read());
            final int semicolon = line.indexOf(Symbol.C_SEMICOLON);
            final String size = semicolon < Normal._0 ? line : line.substring(Normal._0, semicolon);
            try {
                remaining = Integer.parseUnsignedInt(size.trim(), 16);
            } catch (final NumberFormatException e) {
                throw new ProtocolException("Invalid HTTP chunk size", e);
            }
            if (remaining == Normal._0) {
                readTrailers();
                done = true;
                codec.completeBody();
            }
        }

        /**
         * Reads response trailers.
         */
        private void readTrailers() {
            final Headers.Builder builder = Headers.builder();
            String line = reader.readLine(timeout.read());
            while (!line.isEmpty()) {
                final int colon = line.indexOf(Symbol.COLON);
                if (colon <= Normal._0) {
                    throw new ProtocolException("Invalid HTTP trailer line");
                }
                builder.add(line.substring(Normal._0, colon), line.substring(colon + Normal._1).trim());
                line = reader.readLine(timeout.read());
            }
            codec.trailers(builder.build());
        }

        /**
         * Drains at most 64 KiB of chunked framing before deciding reuse.
         *
         * @return {@code true} when the terminal chunk and trailers were consumed
         */
        private boolean drain() {
            if (done) {
                return true;
            }
            final byte[] buffer = new byte[Normal._8192];
            int drained = Normal._0;
            final Duration original = timeout.read();
            while (drained < Builder.BYTES_64_KIB) {
                final int limit = Math.min(buffer.length, Builder.BYTES_64_KIB - drained);
                final int read;
                try {
                    read = readBounded(buffer, Normal._0, limit, drainTimeout(original));
                } catch (final RuntimeException e) {
                    return false;
                }
                if (read < Normal._0) {
                    return done;
                }
                drained += read;
            }
            return done;
        }

        /**
         * Reads chunk data using an explicit timeout for bounded draining.
         *
         * @param bytes         destination byte array
         * @param offset        first destination index
         * @param length        maximum number of decoded chunk bytes to read
         * @param activeTimeout timeout applied to this bounded read
         * @return decoded byte count, or {@code -1} after the terminal chunk
         */
        private int readBounded(final byte[] bytes, final int offset, final int length, final Duration activeTimeout) {
            if (done) {
                return Normal.__1;
            }
            if (remaining == Normal._0) {
                readChunkSize();
                if (done) {
                    return Normal.__1;
                }
            }
            final int limit = Math.min(length, remaining);
            final int read = reader.read(bytes, offset, limit, activeTimeout);
            if (read < Normal._0) {
                throw new SocketException("HTTP chunked body reached EOF");
            }
            remaining -= read;
            if (remaining == Normal._0) {
                reader.expectCrlf(activeTimeout);
            }
            return read;
        }

    }

    /**
     * EOF-terminated response body stream.
     */
    private static final class UnknownInputStream extends InputStream {

        /**
         * Reader.
         */
        private final NetworkReader reader;

        /**
         * Timeout.
         */
        private final org.miaixz.bus.fabric.Timeout timeout;

        /**
         * Codec.
         */
        private final Http1Codec codec;

        /**
         * Done flag.
         */
        private boolean done;

        /**
         * Creates a stream.
         *
         * @param reader  network reader supplying close-delimited body bytes
         * @param timeout maximum duration allowed for each network read
         * @param codec   owning codec notified when the stream completes
         */
        private UnknownInputStream(final NetworkReader reader, final org.miaixz.bus.fabric.Timeout timeout,
                final Http1Codec codec) {
            this.reader = require(reader, "Network reader");
            this.timeout = require(timeout, "Timeout");
            this.codec = require(codec, "HTTP codec");
        }

        /**
         * Reads one byte.
         *
         * @return byte or EOF
         * @throws IOException when reading fails
         */
        @Override
        public int read() throws IOException {
            if (done) {
                return Normal.__1;
            }
            final int value = reader.read(timeout.read());
            if (value < Normal._0) {
                done = true;
                codec.completeBody();
            }
            return value;
        }

        /**
         * Reads bytes.
         *
         * @param bytes  destination byte array
         * @param offset first destination index to fill
         * @param length maximum number of bytes to read
         * @return read count or EOF
         */
        @Override
        public int read(final byte[] bytes, final int offset, final int length) {
            if (done) {
                return Normal.__1;
            }
            if (length == Normal._0) {
                return Normal._0;
            }
            final int read = reader.read(bytes, offset, length, timeout.read());
            if (read < Normal._0) {
                done = true;
                codec.completeBody();
            }
            return read;
        }

    }

}
