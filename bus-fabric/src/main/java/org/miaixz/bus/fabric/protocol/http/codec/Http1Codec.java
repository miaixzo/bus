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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.codec.stream.StreamSink;
import org.miaixz.bus.fabric.codec.stream.StreamSource;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.protocol.http.HttpHeaders;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.HttpBody;

/**
 * HTTP/1.1 codec bound to a selected network connection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http1Codec implements HttpCodec {

    /**
     * Header line length limit.
     */
    private static final int MAX_LINE = 8192;

    /**
     * IO buffer size.
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * Binary body media fallback.
     */
    private static final MediaType BINARY = MediaType.parse("application/octet-stream");

    /**
     * Bound connection.
     */
    private final Connection connection;

    /**
     * Reader over the bound connection.
     */
    private final NetworkReader reader;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

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
     * Current read timeout.
     */
    private volatile Duration readTimeout;

    /**
     * Current write timeout.
     */
    private volatile Duration writeTimeout;

    /**
     * Creates an HTTP/1 codec.
     *
     * @param connection bound connection
     */
    public Http1Codec(final Connection connection) {
        this.connection = require(connection, "Network connection");
        this.reader = new NetworkReader(connection);
        this.state = new AtomicReference<>(Status.OPENED);
        this.bodyComplete = new AtomicBoolean(true);
        this.connectionClose = new AtomicBoolean();
        this.trailers = new AtomicReference<>(Headers.empty());
        this.readTimeout = Duration.ZERO;
        this.writeTimeout = Duration.ZERO;
    }

    /**
     * Writes request metadata, headers, and body.
     *
     * @param request request
     */
    @Override
    public void writeRequest(final HttpRequest request) {
        final HttpRequest current = require(request, "HTTP request");
        writeTimeout = current.timeout().write();
        state.set(Status.RUNNING);
        writeHeaders(current);
        if (current.body().length() == 0 || current.method() == HTTP.Method.GET
                || current.method() == HTTP.Method.HEAD) {
            state.set(Status.OPENED);
            return;
        }
        try (StreamSink sink = createRequestBody(current)) {
            sink.write(current.body().payload());
            sink.flush();
        }
        state.set(Status.OPENED);
    }

    /**
     * Writes the HTTP/1 request line and headers.
     *
     * @param request request
     */
    public void writeHeaders(final HttpRequest request) {
        final HttpRequest current = require(request, "HTTP request");
        writeText(HttpLine.request(current) + Symbol.CRLF);
        for (final Map.Entry<String, List<String>> entry : current.headers().asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                writeText(HttpLine.header(entry.getKey(), value) + Symbol.CRLF);
            }
        }
        writeText(Symbol.CRLF);
    }

    /**
     * Creates a request stream sink for the current request framing.
     *
     * @param request request
     * @return stream sink
     */
    public StreamSink createRequestBody(final HttpRequest request) {
        final HttpRequest current = require(request, "HTTP request");
        final long declared = declaredLength(current.headers());
        if (chunked(current.headers())) {
            return new ChunkedSink(this);
        }
        if (declared >= 0) {
            if (current.body().length() >= 0 && current.body().length() != declared) {
                throw new ProtocolException("HTTP request body length mismatch");
            }
            return new FixedSink(this, declared);
        }
        return new UnknownSink(this);
    }

    /**
     * Reads response status, headers, and a streaming body.
     *
     * @param request request
     * @return response
     */
    @Override
    public HttpResponse readResponse(final HttpRequest request) {
        final HttpRequest current = require(request, "HTTP request");
        readTimeout = current.timeout().read();
        state.set(Status.RUNNING);
        trailers.set(Headers.empty());
        String line = reader.readLine(readTimeout);
        int code = HttpLine.status(line);
        Headers headers = readHeaders();
        while (code == 100) {
            line = reader.readLine(readTimeout);
            code = HttpLine.status(line);
            headers = readHeaders();
        }
        final HttpResponse headerOnly = HttpResponse.builder().request(current).code(code).message(reason(line))
                .headers(headers).body(HttpBody.empty()).protocol(Protocol.HTTP_1_1).trailers(this::trailers).build();
        final StreamSource source = openResponseBody(headerOnly);
        final HttpBody body = source.length() == 0 ? HttpBody.empty()
                : HttpBody.of((Payload) source, media(headerOnly.headers()));
        state.set(Status.OPENED);
        return HttpResponse.builder().request(current).code(code).message(reason(line)).headers(headers).body(body)
                .protocol(Protocol.HTTP_1_1).trailers(this::trailers).build();
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
     * @param response response
     * @return stream source
     */
    public StreamSource openResponseBody(final HttpResponse response) {
        final HttpResponse current = require(response, "HTTP response");
        final long length = responseLength(current.request(), current);
        if (length == 0) {
            bodyComplete.set(true);
            return new EmptySource(this);
        }
        bodyComplete.set(false);
        connectionClose.set(connectionClose(current.headers()));
        final InputStream input;
        if (chunked(current.headers())) {
            input = new ChunkedInputStream(reader, readTimeout, this);
        } else if (length >= 0) {
            input = new FixedInputStream(reader, readTimeout, length, this);
        } else {
            input = new UnknownInputStream(reader, readTimeout, this);
            connectionClose.set(true);
        }
        return new NetworkSource(input, length, media(current.headers()), this);
    }

    /**
     * Cancels this codec.
     */
    @Override
    public void cancel() {
        final Status previous = state.getAndSet(Status.CANCELLED);
        if (previous != Status.CANCELLED && previous != Status.CLOSED) {
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
        return state.get() == Status.OPENED && bodyComplete.get() && !connectionClose.get() && connection.healthy();
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
     * @param value value
     */
    private void writeText(final String value) {
        write(ByteBuffer.wrap(value.getBytes(org.miaixz.bus.core.lang.Charset.US_ASCII)));
    }

    /**
     * Writes a whole buffer to the connection.
     *
     * @param source source
     */
    private void write(final ByteBuffer source) {
        while (source.hasRemaining()) {
            final int position = source.position();
            final int written = await(connection.write(source), writeTimeout, "HTTP write timed out");
            if (written < 0) {
                throw new SocketException("HTTP write reached EOF");
            }
            if (written == 0) {
                Thread.yield();
            } else {
                source.position(position + written);
            }
        }
    }

    /**
     * Reads response headers.
     *
     * @return headers
     */
    private Headers readHeaders() {
        final Headers.Builder builder = Headers.builder();
        String line = reader.readLine(readTimeout);
        while (!line.isEmpty()) {
            final int colon = line.indexOf(Symbol.COLON);
            if (colon <= 0) {
                throw new ProtocolException("Invalid HTTP header line");
            }
            final String name = line.substring(0, colon);
            final String value = line.substring(colon + 1).trim();
            try {
                builder.add(name, value);
            } catch (final RuntimeException e) {
                throw new ProtocolException("Invalid HTTP response header", e);
            }
            line = reader.readLine(readTimeout);
        }
        return builder.build();
    }

    /**
     * Extracts a reason phrase from a validated status line.
     *
     * @param line status line
     * @return reason phrase
     */
    private static String reason(final String line) {
        final int first = line.indexOf(Symbol.SPACE);
        final int second = first < 0 ? -1 : line.indexOf(Symbol.SPACE, first + 1);
        return second < 0 ? Normal.EMPTY : line.substring(second + 1);
    }

    /**
     * Computes response body length semantics.
     *
     * @param request  request
     * @param response response
     * @return length, or -1 for unknown
     */
    private static long responseLength(final HttpRequest request, final HttpResponse response) {
        if (request.method() == HTTP.Method.HEAD || response.code() == 204 || response.code() == 304
                || (response.code() >= 100 && response.code() < 200)) {
            return 0;
        }
        if (chunked(response.headers())) {
            return -1;
        }
        return declaredLength(response.headers());
    }

    /**
     * Returns declared Content-Length.
     *
     * @param headers headers
     * @return length or -1
     */
    private static long declaredLength(final Headers headers) {
        final String value = headers.get("Content-Length");
        if (value == null) {
            return -1;
        }
        try {
            final long length = Long.parseLong(value);
            if (length < 0) {
                throw new ProtocolException("Content-Length must be non-negative");
            }
            return length;
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid Content-Length", e);
        }
    }

    /**
     * Returns whether headers use chunked transfer coding.
     *
     * @param headers headers
     * @return true when chunked
     */
    private static boolean chunked(final Headers headers) {
        for (final String value : HttpHeaders.values(headers, "Transfer-Encoding")) {
            if ("chunked".equals(value.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether response closes the connection.
     *
     * @param headers headers
     * @return true when closed
     */
    private static boolean connectionClose(final Headers headers) {
        for (final String value : HttpHeaders.values(headers, "Connection")) {
            if ("close".equals(value.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses response media.
     *
     * @param headers headers
     * @return media
     */
    private static MediaType media(final Headers headers) {
        final String contentType = headers.get("Content-Type");
        return contentType == null ? BINARY : MediaType.parse(contentType);
    }

    /**
     * Waits for a future with bus exceptions.
     *
     * @param future  future
     * @param timeout timeout
     * @param message timeout message
     * @param <T>     result type
     * @return result
     */
    private static <T> T await(final CompletableFuture<T> future, final Duration timeout, final String message) {
        try {
            if (timeout.isZero()) {
                return future.get();
            }
            return future.get(Math.max(1L, timeout.toMillis()), TimeUnit.MILLISECONDS);
        } catch (final java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            throw new TimeoutException(message, e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for HTTP IO", e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new SocketException("HTTP IO failed", cause);
        }
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
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
         * Bound connection.
         */
        private final Connection connection;

        /**
         * Creates a reader.
         *
         * @param connection connection
         */
        private NetworkReader(final Connection connection) {
            this.connection = require(connection, "Network connection");
        }

        /**
         * Reads one byte.
         *
         * @param timeout timeout
         * @return byte or EOF
         */
        private int read(final Duration timeout) {
            final ByteBuffer buffer = ByteBuffer.allocate(1);
            while (true) {
                final int position = buffer.position();
                final int read = await(connection.read(buffer), timeout, "HTTP read timed out");
                if (read < 0) {
                    return -1;
                }
                if (read == 0) {
                    Thread.yield();
                    continue;
                }
                buffer.position(position + read);
                buffer.flip();
                return buffer.get() & 0xff;
            }
        }

        /**
         * Reads bytes into a target array.
         *
         * @param target  target
         * @param offset  offset
         * @param length  length
         * @param timeout timeout
         * @return read count or EOF
         */
        private int read(final byte[] target, final int offset, final int length, final Duration timeout) {
            if (length == 0) {
                return 0;
            }
            final ByteBuffer buffer = ByteBuffer.wrap(target, offset, length);
            while (true) {
                final int position = buffer.position();
                final int read = await(connection.read(buffer), timeout, "HTTP read timed out");
                if (read < 0) {
                    return read;
                }
                if (read == 0) {
                    Thread.yield();
                    continue;
                }
                buffer.position(position + read);
                return read;
            }
        }

        /**
         * Reads a CRLF-terminated line without the terminator.
         *
         * @param timeout timeout
         * @return line
         */
        private String readLine(final Duration timeout) {
            final StringBuilder builder = new StringBuilder();
            boolean carriage = false;
            while (builder.length() <= MAX_LINE) {
                final int value = read(timeout);
                if (value < 0) {
                    throw new SocketException("HTTP stream reached EOF");
                }
                if (carriage && value == Symbol.C_LF) {
                    return builder.substring(0, builder.length() - 1);
                }
                carriage = value == Symbol.C_CR;
                builder.append((char) value);
            }
            throw new ProtocolException("HTTP line is too large");
        }

        /**
         * Consumes a CRLF sequence.
         *
         * @param timeout timeout
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
    private abstract static class AbstractSink implements StreamSink {

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
         * @param codec codec
         */
        AbstractSink(final Http1Codec codec) {
            this.codec = require(codec, "HTTP codec");
        }

        /**
         * Writes the payload stream.
         *
         * @param payload payload
         */
        @Override
        public void write(final Payload payload) {
            require(payload, "Payload");
            final byte[] buffer = new byte[BUFFER_SIZE];
            try (InputStream input = payload.stream()) {
                int read = input.read(buffer);
                while (read >= 0) {
                    if (read > 0) {
                        write(ByteBuffer.wrap(buffer, 0, read));
                    }
                    read = input.read(buffer);
                }
            } catch (final IOException e) {
                throw new SocketException("Unable to read HTTP request body", e);
            }
        }

        /**
         * Returns written bytes.
         *
         * @return written bytes
         */
        @Override
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
         * @param codec    codec
         * @param expected expected length
         */
        private FixedSink(final Http1Codec codec, final long expected) {
            super(codec);
            this.expected = expected;
        }

        /**
         * Writes fixed bytes.
         *
         * @param source source
         */
        @Override
        public void write(final ByteBuffer source) {
            require(source, "Source buffer");
            ensureOpen();
            if (written + source.remaining() > expected) {
                throw new ProtocolException("HTTP fixed body exceeds Content-Length");
            }
            written += source.remaining();
            codec.write(source);
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
         * @param codec codec
         */
        private ChunkedSink(final Http1Codec codec) {
            super(codec);
        }

        /**
         * Writes a chunk.
         *
         * @param source source
         */
        @Override
        public void write(final ByteBuffer source) {
            require(source, "Source buffer");
            ensureOpen();
            if (!source.hasRemaining()) {
                return;
            }
            final int count = source.remaining();
            codec.write(
                    ByteBuffer.wrap(
                            (Integer.toHexString(count) + Symbol.CRLF)
                                    .getBytes(org.miaixz.bus.core.lang.Charset.US_ASCII)));
            codec.write(source);
            codec.write(ByteBuffer.wrap(Symbol.CRLF.getBytes(org.miaixz.bus.core.lang.Charset.US_ASCII)));
            written += count;
        }

        /**
         * Closes the sink with the terminal chunk.
         */
        @Override
        public void close() {
            ensureOpen();
            codec.write(ByteBuffer.wrap("0\r\n\r\n".getBytes(org.miaixz.bus.core.lang.Charset.US_ASCII)));
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
         * @param codec codec
         */
        private UnknownSink(final Http1Codec codec) {
            super(codec);
        }

        /**
         * Writes raw bytes.
         *
         * @param source source
         */
        @Override
        public void write(final ByteBuffer source) {
            require(source, "Source buffer");
            ensureOpen();
            written += source.remaining();
            codec.write(source);
        }

    }

    /**
     * Empty body source.
     */
    private static final class EmptySource implements StreamSource, Payload {

        /**
         * Codec.
         */
        private final Http1Codec codec;

        /**
         * Creates an empty source.
         *
         * @param codec codec
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
         * Opens an empty stream.
         *
         * @return stream
         */
        @Override
        public InputStream stream() {
            return InputStream.nullInputStream();
        }

        /**
         * Reads bytes into target.
         *
         * @param target target buffer
         * @return EOF
         */
        @Override
        public int read(final ByteBuffer target) {
            require(target, "Target buffer");
            return -1;
        }

        /**
         * Returns empty bytes.
         *
         * @return bytes
         */
        @Override
        public byte[] bytes() {
            return new byte[0];
        }

        @Override
        public byte[] bytes(final long maxBytes) {
            Payload.validateMaterializeMaxBytes(maxBytes);
            return new byte[0];
        }

        /**
         * Returns empty text.
         *
         * @param charset charset
         * @return text
         */
        @Override
        public String text(final Charset charset) {
            require(charset, "Charset");
            return Normal.EMPTY;
        }

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
    private static final class NetworkSource implements StreamSource, Payload {

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
         * Open flag.
         */
        private final AtomicBoolean opened;

        /**
         * Current stream.
         */
        private InputStream current;

        /**
         * Creates a source.
         *
         * @param input  input
         * @param length length
         * @param media  media
         * @param codec  codec
         */
        private NetworkSource(final InputStream input, final long length, final MediaType media,
                final Http1Codec codec) {
            this.input = require(input, "Input stream");
            this.length = length;
            require(media, "MediaType");
            this.codec = require(codec, "HTTP codec");
            this.opened = new AtomicBoolean();
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
         * Opens the source stream.
         *
         * @return stream
         */
        @Override
        public InputStream stream() {
            if (!opened.compareAndSet(false, true)) {
                throw new StatefulException("HTTP response body can only be opened once");
            }
            current = input;
            return input;
        }

        /**
         * Reads bytes into target.
         *
         * @param target target buffer
         * @return read count
         */
        @Override
        public int read(final ByteBuffer target) {
            require(target, "Target buffer");
            if (!target.hasRemaining()) {
                return 0;
            }
            final InputStream source = current == null ? stream() : current;
            final byte[] buffer = new byte[Math.min(target.remaining(), BUFFER_SIZE)];
            try {
                final int read = source.read(buffer);
                if (read > 0) {
                    target.put(buffer, 0, read);
                }
                return read;
            } catch (final IOException e) {
                throw new SocketException("Unable to read HTTP response body", e);
            }
        }

        /**
         * Reads all bytes.
         *
         * @return bytes
         */
        @Override
        public byte[] bytes() {
            return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        @Override
        public byte[] bytes(final long maxBytes) {
            return Payload.materialize(this, maxBytes, "Http1Codec.NetworkSource.bytes(long)");
        }

        /**
         * Reads text.
         *
         * @param charset charset
         * @return text
         */
        @Override
        public String text(final Charset charset) {
            return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

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
            try {
                input.close();
            } catch (final IOException e) {
                throw new SocketException("Unable to close HTTP response body", e);
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
        private final Duration timeout;

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
         * @param reader  reader
         * @param timeout timeout
         * @param length  length
         * @param codec   codec
         */
        private FixedInputStream(final NetworkReader reader, final Duration timeout, final long length,
                final Http1Codec codec) {
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
            final byte[] one = new byte[1];
            final int read = read(one, 0, 1);
            return read < 0 ? -1 : one[0] & 0xff;
        }

        /**
         * Reads bytes.
         *
         * @param bytes  bytes
         * @param offset offset
         * @param length length
         * @return read count or EOF
         */
        @Override
        public int read(final byte[] bytes, final int offset, final int length) {
            if (closed) {
                return -1;
            }
            if (length == 0) {
                return 0;
            }
            if (remaining == 0) {
                codec.completeBody();
                return -1;
            }
            final int limit = (int) Math.min(length, remaining);
            final int read = reader.read(bytes, offset, limit, timeout);
            if (read < 0) {
                throw new SocketException("HTTP fixed body reached EOF");
            }
            remaining -= read;
            if (remaining == 0) {
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
            if (remaining > 0) {
                codec.abandonBody();
            }
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
        private final Duration timeout;

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
         * @param reader  reader
         * @param timeout timeout
         * @param codec   codec
         */
        private ChunkedInputStream(final NetworkReader reader, final Duration timeout, final Http1Codec codec) {
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
            final byte[] one = new byte[1];
            final int read = read(one, 0, 1);
            return read < 0 ? -1 : one[0] & 0xff;
        }

        /**
         * Reads bytes.
         *
         * @param bytes  bytes
         * @param offset offset
         * @param length length
         * @return read count or EOF
         */
        @Override
        public int read(final byte[] bytes, final int offset, final int length) {
            if (closed || done) {
                return -1;
            }
            if (length == 0) {
                return 0;
            }
            if (remaining == 0) {
                readChunkSize();
                if (done) {
                    return -1;
                }
            }
            final int limit = Math.min(length, remaining);
            final int read = reader.read(bytes, offset, limit, timeout);
            if (read < 0) {
                throw new SocketException("HTTP chunked body reached EOF");
            }
            remaining -= read;
            if (remaining == 0) {
                reader.expectCrlf(timeout);
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
            final String line = reader.readLine(timeout);
            final int semicolon = line.indexOf(Symbol.C_SEMICOLON);
            final String size = semicolon < 0 ? line : line.substring(0, semicolon);
            try {
                remaining = Integer.parseUnsignedInt(size.trim(), 16);
            } catch (final NumberFormatException e) {
                throw new ProtocolException("Invalid HTTP chunk size", e);
            }
            if (remaining == 0) {
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
            String line = reader.readLine(timeout);
            while (!line.isEmpty()) {
                final int colon = line.indexOf(Symbol.COLON);
                if (colon <= 0) {
                    throw new ProtocolException("Invalid HTTP trailer line");
                }
                builder.add(line.substring(0, colon), line.substring(colon + 1).trim());
                line = reader.readLine(timeout);
            }
            codec.trailers(builder.build());
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
        private final Duration timeout;

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
         * @param reader  reader
         * @param timeout timeout
         * @param codec   codec
         */
        private UnknownInputStream(final NetworkReader reader, final Duration timeout, final Http1Codec codec) {
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
            final byte[] one = new byte[1];
            final int read = read(one, 0, 1);
            return read < 0 ? -1 : one[0] & 0xff;
        }

        /**
         * Reads bytes.
         *
         * @param bytes  bytes
         * @param offset offset
         * @param length length
         * @return read count or EOF
         */
        @Override
        public int read(final byte[] bytes, final int offset, final int length) {
            if (done) {
                return -1;
            }
            if (length == 0) {
                return 0;
            }
            final int read = reader.read(bytes, offset, length, timeout);
            if (read < 0) {
                done = true;
                codec.completeBody();
            }
            return read;
        }

    }

}
