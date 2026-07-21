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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.protocol.http.http2.Http2Connection;
import org.miaixz.bus.fabric.protocol.http.http2.Http2Header;
import org.miaixz.bus.fabric.protocol.http.http2.Http2Stream;

/**
 * HTTP/2 codec bound to an HTTP/2 connection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Codec implements HttpCodec {

    /**
     * HTTP/2 connection.
     */
    private final Http2Connection connection;

    /**
     * Request to stream map.
     */
    private volatile HttpRequest activeRequest;

    /**
     * Stream owned by the current codec call.
     */
    private final AtomicReference<Http2Stream> active;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Creates an HTTP/2 codec.
     *
     * @param connection connection
     */
    public Http2Codec(final Http2Connection connection) {
        this.connection = require(connection, "HTTP/2 connection");
        this.active = new AtomicReference<>();
        this.state = new AtomicReference<>(Status.OPENED);
    }

    /**
     * Creates a stream and writes request headers.
     *
     * @param request request
     * @return stream
     */
    public Http2Stream newStream(final HttpRequest request) {
        final HttpRequest current = require(request, "HTTP request");
        final List<Http2Header> headers = requestHeaders(current);
        final Http2Stream stream = connection.newStream(Headers.empty(), true);
        if (!active.compareAndSet(null, stream)) {
            stream.close();
            throw new StatefulException("HTTP/2 codec already has an active stream");
        }
        activeRequest = current;
        try {
            connection.startReader();
            connection.writeHeaders(stream.id(), headers, current.body().length() == Normal._0);
            return stream;
        } catch (final RuntimeException e) {
            active.compareAndSet(stream, null);
            activeRequest = null;
            stream.close();
            throw e;
        }
    }

    /**
     * Writes request headers and data frames.
     *
     * @param request request
     */
    @Override
    public void writeRequest(final HttpRequest request) {
        final HttpRequest current = require(request, "HTTP request");
        state.set(Status.RUNNING);
        try {
            final Http2Stream stream = newStream(current);
            if (current.body().length() != Normal._0) {
                writeBody(stream, current);
            }
        } finally {
            if (state.get() == Status.RUNNING) {
                state.set(Status.OPENED);
            }
        }
    }

    /**
     * Reads response headers and data frames for a request stream.
     *
     * @param request request
     * @return response
     */
    @Override
    public HttpResponse readResponse(final HttpRequest request) {
        final HttpRequest current = require(request, "HTTP request");
        final Http2Stream stream = active.get();
        if (stream == null || activeRequest != current) {
            throw new StatefulException("HTTP/2 stream is missing for request");
        }
        state.set(Status.RUNNING);
        try {
            final Headers headers = validateResponseHeaders(
                    stream.awaitResponseHeaders(current.timeout().read()),
                    stream.responseStatus());
            final String status = stream.responseStatus();
            final int code = parseStatus(status);
            final Payload payload = new LengthCheckedPayload(stream.payload(), contentLength(headers));
            return HttpResponse.builder().request(current).code(code).message(Normal.EMPTY).headers(headers)
                    .body(PayloadBody.of(payload, media(headers))).protocol(Protocol.HTTP_2)
                    .trailers(() -> validateTrailers(stream.trailers())).build();
        } finally {
            state.set(Status.OPENED);
        }
    }

    /**
     * Cancels this codec.
     */
    @Override
    public void cancel() {
        final Status previous = state.getAndSet(Status.CANCELLED);
        if (previous == Status.CANCELLED || previous == Status.CLOSED) {
            return;
        }
        final Http2Stream stream = active.getAndSet(null);
        activeRequest = null;
        if (stream != null) {
            stream.close();
        }
    }

    /**
     * Returns whether the connection can be reused.
     *
     * @return true when reusable
     */
    @Override
    public boolean reusable() {
        return state.get() == Status.OPENED;
    }

    /**
     * Writes request body DATA frames.
     *
     * @param stream  stream
     * @param request request
     */
    private void writeBody(final Http2Stream stream, final HttpRequest request) {
        final Buffer buffer = new Buffer();
        try (Source input = request.body().source()) {
            final long declared = request.body().length();
            long remaining = declared;
            while (true) {
                final long read = input.read(
                        buffer,
                        Math.min(Normal._16384, declared < Normal._0 ? Normal._16384 : Math.max(Normal._1, remaining)));
                if (read < Normal._0) {
                    break;
                }
                if (read == Normal._0) {
                    continue;
                }
                if (declared >= Normal._0 && read > remaining) {
                    throw new ProtocolException("HTTP/2 request body exceeds declared length");
                }
                if (declared >= Normal._0) {
                    remaining -= read;
                }
                connection.writeData(
                        stream.id(),
                        buffer,
                        declared >= Normal._0 && remaining == Normal._0,
                        request.timeout().write());
                if (declared >= Normal._0 && remaining == Normal._0) {
                    break;
                }
            }
            if (declared >= Normal._0 && remaining != Normal._0) {
                throw new ProtocolException("HTTP/2 request body is shorter than declared length");
            }
            if (declared < Normal._0) {
                connection.writeData(stream.id(), new Buffer(), true, request.timeout().write());
            }
        } catch (final IOException e) {
            throw new SocketException("Unable to read HTTP/2 request body", e);
        }
    }

    /**
     * Builds HTTP/2 pseudo headers.
     *
     * @param request request
     * @return headers
     */
    private static List<Http2Header> requestHeaders(final HttpRequest request) {
        final UnoUrl url = request.url();
        final Headers requestHeaders = request.headers();
        final ArrayList<Http2Header> headers = new ArrayList<>(Normal._4 + requestHeaders.size());
        headers.add(Http2Header.of(HTTP.TARGET_METHOD_UTF8, request.method().value()));
        headers.add(Http2Header.of(HTTP.TARGET_SCHEME_UTF8, url.address().scheme()));
        headers.add(Http2Header.of(HTTP.TARGET_AUTHORITY_UTF8, request.authority()));
        headers.add(Http2Header.of(HTTP.TARGET_PATH_UTF8, request.requestTarget()));
        final Set<String> nominated = connectionNominated(requestHeaders);
        for (int index = Normal._0; index < requestHeaders.size(); index++) {
            final String name = requestHeaders.name(index).toLowerCase(Locale.ROOT);
            final String value = requestHeaders.value(index);
            if (forbidden(name, nominated) || Builder.HOST.equals(name)) {
                continue;
            }
            if (HTTP.TE.equalsIgnoreCase(name) && !HTTP.TRAILERS.equalsIgnoreCase(value.trim())) {
                throw new ProtocolException("HTTP/2 TE header must be trailers");
            }
            headers.add(Http2Header.of(name, value));
        }
        return headers;
    }

    /**
     * Returns lower-case fields nominated by the HTTP/1 Connection header.
     */
    private static Set<String> connectionNominated(final Headers headers) {
        final String value = headers.get(HTTP.CONNECTION);
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        final HashSet<String> names = new HashSet<>();
        for (final String token : value.split(",")) {
            final String name = token.trim().toLowerCase(Locale.ROOT);
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * Returns whether a field is prohibited on an HTTP/2 wire.
     */
    private static boolean forbidden(final String name, final Set<String> nominated) {
        return nominated.contains(name) || switch (name) {
            case "connection", "keep-alive", "proxy-connection", "transfer-encoding", "upgrade" -> true;
            default -> false;
        };
    }

    /**
     * Validates one initial response block and preserves its immutable ordering.
     */
    private static Headers validateResponseHeaders(final Headers headers, final String status) {
        parseStatus(status);
        for (int index = Normal._0; index < headers.size(); index++) {
            final String name = headers.name(index).toLowerCase(Locale.ROOT);
            if (name.startsWith(":")) {
                throw new ProtocolException("Invalid HTTP/2 response pseudo-header");
            } else {
                if (forbidden(name, Set.of()) || HTTP.TE.equalsIgnoreCase(name)) {
                    throw new ProtocolException("Connection-specific HTTP/2 response header");
                }
            }
        }
        contentLength(headers);
        return headers;
    }

    /**
     * Validates trailers, which may not contain pseudo or connection fields.
     */
    private static Headers validateTrailers(final Headers trailers) {
        for (int index = Normal._0; index < trailers.size(); index++) {
            final String name = trailers.name(index).toLowerCase(Locale.ROOT);
            if (name.startsWith(":") || forbidden(name, Set.of()) || HTTP.TE.equalsIgnoreCase(name)) {
                throw new ProtocolException("Invalid HTTP/2 trailer field");
            }
        }
        return trailers;
    }

    /**
     * Parses consistent non-negative Content-Length values, or -1 when absent.
     */
    private static long contentLength(final Headers headers) {
        long length = Normal.__1;
        for (int index = Normal._0; index < headers.size(); index++) {
            if (!HTTP.CONTENT_LENGTH.equalsIgnoreCase(headers.name(index))) {
                continue;
            }
            final long current;
            try {
                current = Long.parseLong(headers.value(index));
            } catch (final NumberFormatException e) {
                throw new ProtocolException("Invalid HTTP/2 Content-Length", e);
            }
            if (current < Normal.LONG_ZERO || (length >= Normal.LONG_ZERO && length != current)) {
                throw new ProtocolException("Conflicting HTTP/2 Content-Length");
            }
            length = current;
        }
        return length;
    }

    /**
     * Parses status code.
     *
     * @param value value
     * @return code
     */
    private static int parseStatus(final String value) {
        if (value == null || value.length() != Normal._3) {
            throw new ProtocolException("Invalid HTTP/2 status");
        }
        try {
            final int code = Integer.parseInt(value);
            if (code < HTTP.HTTP_CONTINUE || code >= Normal._600) {
                throw new ProtocolException("Invalid HTTP/2 status");
            }
            return code;
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid HTTP/2 status", e);
        }
    }

    /**
     * Parses response media.
     *
     * @param headers headers
     * @return media
     */
    private static MediaType media(final Headers headers) {
        final String contentType = headers.get(HTTP.CONTENT_TYPE);
        return contentType == null ? MediaType.APPLICATION_OCTET_STREAM_TYPE : MediaType.parse(contentType);
    }

    /**
     * Validates required value.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * One-shot response payload that verifies the received DATA total against Content-Length at end of stream.
     */
    private static final class LengthCheckedPayload implements Payload {

        /**
         * Connection-backed payload supplying the response DATA source.
         */
        private final Payload delegate;

        /**
         * Declared Content-Length, or a negative value when no length was declared.
         */
        private final long expected;

        /**
         * Wraps a response payload with Content-Length verification.
         *
         * @param delegate connection-backed response payload
         * @param expected declared length, or a negative value when absent
         */
        private LengthCheckedPayload(final Payload delegate, final long expected) {
            this.delegate = require(delegate, "HTTP/2 response payload");
            this.expected = expected;
        }

        /**
         * Returns the declared response length.
         *
         * @return declared length, or a negative value when unknown
         */
        @Override
        public long length() {
            return expected;
        }

        /**
         * Creates the one-shot source that performs cumulative length checks.
         *
         * @return length-checking response source
         */
        @Override
        public Source source() {
            return new LengthCheckedSource(delegate.source(), expected);
        }

        /**
         * Reports that the connection-backed payload cannot be replayed.
         *
         * @return always {@code false}
         */
        @Override
        public boolean repeatable() {
            return false;
        }
    }

    /**
     * Source-level Content-Length verifier for an HTTP/2 DATA stream.
     */
    private static final class LengthCheckedSource implements Source {

        /**
         * Underlying stream source.
         */
        private final Source delegate;

        /**
         * Declared Content-Length, or a negative value when no length was declared.
         */
        private final long expected;

        /**
         * Number of DATA bytes returned to the caller so far.
         */
        private long received;

        /**
         * Creates a cumulative response-length verifier.
         *
         * @param delegate underlying response source
         * @param expected declared length, or a negative value when absent
         */
        private LengthCheckedSource(final Source delegate, final long expected) {
            this.delegate = require(delegate, "HTTP/2 response source");
            this.expected = expected;
        }

        /**
         * Reads bytes and validates that the cumulative total never exceeds, and at EOF equals, Content-Length.
         *
         * @param target    destination buffer
         * @param byteCount maximum bytes to read
         * @return bytes read, or {@code -1} at end of stream
         * @throws IOException if the delegate read fails
         */
        @Override
        public long read(final Buffer target, final long byteCount) throws IOException {
            final long read = delegate.read(target, byteCount);
            if (read > Normal.LONG_ZERO) {
                received = Math.addExact(received, read);
                if (expected >= Normal.LONG_ZERO && received > expected) {
                    throw new ProtocolException("HTTP/2 response body exceeds Content-Length");
                }
            } else if (read < Normal.LONG_ZERO && expected >= Normal.LONG_ZERO && received != expected) {
                throw new ProtocolException("HTTP/2 response body does not match Content-Length");
            }
            return read;
        }

        /**
         * Returns the timeout owned by the underlying stream source.
         *
         * @return delegate timeout
         */
        @Override
        public Timeout timeout() {
            return delegate.timeout();
        }

        /**
         * Closes the underlying stream source.
         *
         * @throws IOException if the delegate close fails
         */
        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

}
