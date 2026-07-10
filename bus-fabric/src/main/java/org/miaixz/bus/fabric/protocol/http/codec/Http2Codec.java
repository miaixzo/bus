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
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.HttpBody;
import org.miaixz.bus.fabric.protocol.http.http2.Http2Connection;
import org.miaixz.bus.fabric.protocol.http.http2.Http2Frame;
import org.miaixz.bus.fabric.protocol.http.http2.Http2Header;
import org.miaixz.bus.fabric.protocol.http.http2.Http2Stream;
import org.miaixz.bus.fabric.protocol.http.http2.Http2Writer;

/**
 * HTTP/2 codec bound to an HTTP/2 connection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Codec implements HttpCodec {

    /**
     * Binary media fallback.
     */
    private static final MediaType BINARY = MediaType.parse("application/octet-stream");

    /**
     * HTTP/2 connection.
     */
    private final Http2Connection connection;

    /**
     * HTTP/2 frame writer.
     */
    private final Http2Writer writer;

    /**
     * Request to stream map.
     */
    private final Map<HttpRequest, Http2Stream> calls;

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
        this.writer = new Http2Writer(this.connection);
        this.calls = Collections.synchronizedMap(new IdentityHashMap<>());
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
        final Headers headers = pseudoHeaders(current);
        final Http2Stream stream = connection.newStream(headers, true);
        calls.put(current, stream);
        connection.startReader();
        writer.headers(stream.id(), headers, current.body().length() == 0);
        return stream;
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
        writer.timeout(current.timeout().write());
        final Http2Stream stream = newStream(current);
        if (current.body().length() > 0 && current.method() != HTTP.Method.GET
                && current.method() != HTTP.Method.HEAD) {
            writeBody(stream, current);
        }
        state.set(Status.OPENED);
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
        final Http2Stream stream = calls.get(current);
        if (stream == null) {
            throw new StatefulException("HTTP/2 stream is missing for request");
        }
        state.set(Status.RUNNING);
        int code = -1;
        Headers headers = Headers.empty();
        boolean end = false;
        try {
            while (!end || code < 0) {
                final Http2Frame frame = connection.nextFrame(stream.id(), current.timeout().read());
                if (frame.type() == Http2Frame.HEADERS) {
                    headers = fromHttp2(frame.headers(), true);
                    final String status = pseudo(frame.headers(), ":status");
                    if (status == null) {
                        throw new ProtocolException("HTTP/2 response is missing :status");
                    }
                    code = parseStatus(status);
                } else if (frame.type() == Http2Frame.RST_STREAM) {
                    throw new SocketException("HTTP/2 stream was reset");
                }
                end = frame.endStream();
            }
            return HttpResponse.builder().request(current).code(code).message(Normal.EMPTY).headers(headers).body(
                    HttpBody.of(Payload.stream(stream.source().stream(), stream.source().length()), media(headers)))
                    .protocol(Protocol.HTTP_2).trailers(Headers.empty()).build();
        } finally {
            if (end) {
                connection.discardFrames(stream.id());
                calls.remove(current);
            }
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
        synchronized (calls) {
            for (final Http2Stream stream : calls.values()) {
                writer.rstStream(stream.id(), 0);
            }
        }
        connection.close();
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
        final byte[] buffer = new byte[8192];
        try (InputStream input = request.body().stream()) {
            final long declared = request.body().length();
            long remaining = declared;
            int read = input.read(buffer);
            while (read >= 0) {
                if (read > 0) {
                    final boolean end = declared >= 0 && (remaining -= read) <= 0;
                    writer.data(stream.id(), ByteBuffer.wrap(buffer, 0, read), end);
                }
                read = input.read(buffer);
            }
            if (declared < 0) {
                writer.data(stream.id(), ByteBuffer.allocate(0), true);
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
    private static Headers pseudoHeaders(final HttpRequest request) {
        final UnoUrl url = request.url();
        Headers headers = Headers.builder().add(":method", request.method().value())
                .add(":scheme", url.address().scheme()).add(":authority", authority(url))
                .add(":path", path(url.toUri())).build();
        for (final Map.Entry<String, List<String>> entry : request.headers().asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                headers = headers.with(entry.getKey().toLowerCase(Locale.ROOT), value);
            }
        }
        return headers;
    }

    /**
     * Converts HTTP/2 headers to root headers.
     *
     * @param headers    headers
     * @param skipPseudo whether pseudo headers are skipped
     * @return root headers
     */
    private static Headers fromHttp2(final List<Http2Header> headers, final boolean skipPseudo) {
        final Headers.Builder builder = Headers.builder();
        for (final Http2Header header : headers) {
            if (skipPseudo && header.name().startsWith(Symbol.COLON)) {
                continue;
            }
            builder.add(header.name(), header.value());
        }
        return builder.build();
    }

    /**
     * Reads a pseudo header.
     *
     * @param headers headers
     * @param name    name
     * @return value or null
     */
    private static String pseudo(final List<Http2Header> headers, final String name) {
        for (final Http2Header header : headers) {
            if (name.equals(header.name())) {
                return header.value();
            }
        }
        return null;
    }

    /**
     * Formats authority.
     *
     * @param url URL
     * @return authority
     */
    private static String authority(final UnoUrl url) {
        return url.address().host() + Symbol.C_COLON + url.address().port();
    }

    /**
     * Formats path.
     *
     * @param uri URI
     * @return path
     */
    private static String path(final URI uri) {
        final String rawPath = uri.getRawPath() == null || uri.getRawPath().isBlank() ? Symbol.SLASH : uri.getRawPath();
        return uri.getRawQuery() == null ? rawPath : rawPath + Symbol.C_QUESTION_MARK + uri.getRawQuery();
    }

    /**
     * Parses status code.
     *
     * @param value value
     * @return code
     */
    private static int parseStatus(final String value) {
        try {
            final int code = Integer.parseInt(value);
            if (code < 100 || code > 599) {
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
        final String contentType = headers.get("Content-Type");
        return contentType == null ? BINARY : MediaType.parse(contentType);
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
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
