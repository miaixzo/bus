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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
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
    private final Map<HttpRequest, Http2Stream> calls;

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
        this.calls = Collections.synchronizedMap(new IdentityHashMap<>());
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
        calls.put(current, stream);
        active.set(stream);
        connection.startReader();
        connection.writeHeaders(stream.id(), headers, current.body().length() == Normal._0);
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
        final Http2Stream stream = newStream(current);
        if (current.body().length() > Normal._0 && current.method() != HTTP.Method.GET
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
        try {
            final Headers headers = stream.awaitResponseHeaders(current.timeout().read());
            final String status = headers.get(HTTP.RESPONSE_STATUS_UTF8);
            if (status == null) {
                throw new ProtocolException("HTTP/2 response is missing :status");
            }
            final int code = parseStatus(status);
            final Payload payload = stream.payload();
            return HttpResponse.builder().request(current).code(code).message(Normal.EMPTY).headers(headers)
                    .body(PayloadBody.of(payload, media(headers))).protocol(Protocol.HTTP_2).trailers(stream::trailers)
                    .build();
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
        if (stream != null) {
            stream.close();
            synchronized (calls) {
                calls.values().removeIf(value -> value == stream);
            }
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
            long read = input.read(buffer, Normal._8192);
            while (read >= Normal._0) {
                if (read > Normal._0) {
                    final boolean end = declared >= Normal._0 && (remaining -= read) <= Normal._0;
                    final Buffer data = new Buffer();
                    data.write(buffer, read);
                    connection.writeData(stream.id(), data, end, request.timeout().write());
                }
                read = input.read(buffer, Normal._8192);
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
        final ArrayList<Http2Header> headers = new ArrayList<>();
        headers.add(Http2Header.of(HTTP.TARGET_METHOD_UTF8, request.method().value()));
        headers.add(Http2Header.of(HTTP.TARGET_SCHEME_UTF8, url.address().scheme()));
        headers.add(Http2Header.of(HTTP.TARGET_AUTHORITY_UTF8, authority(url)));
        headers.add(Http2Header.of(HTTP.TARGET_PATH_UTF8, path(url.toUri())));
        for (final Map.Entry<String, List<String>> entry : request.headers().asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                final String name = entry.getKey().toLowerCase(Locale.ROOT);
                if (HTTP.TE.equalsIgnoreCase(name) && !HTTP.TRAILERS.equalsIgnoreCase(value.trim())) {
                    throw new ProtocolException("HTTP/2 TE header must be trailers");
                }
                headers.add(Http2Header.of(name, value));
            }
        }
        return List.copyOf(headers);
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

}
