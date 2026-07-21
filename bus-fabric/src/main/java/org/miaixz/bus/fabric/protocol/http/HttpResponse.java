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
package org.miaixz.bus.fabric.protocol.http;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.builtin.TlsHandshake;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.DataCodec;
import org.miaixz.bus.fabric.codec.body.ResponseBody;
import org.miaixz.bus.fabric.protocol.http.auth.Challenge;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.protocol.http.cache.HttpCacheControl;

/**
 * Immutable HTTP response snapshot with closeable body.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpResponse implements AutoCloseable {

    /**
     * Close CAS without a per-response atomic wrapper allocation.
     */
    private static final VarHandle CLOSED;

    static {
        try {
            CLOSED = MethodHandles.lookup().findVarHandle(HttpResponse.class, "closed", int.class);
        } catch (final ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Request snapshot.
     */
    private final HttpRequest request;

    /**
     * Status code.
     */
    private final int code;

    /**
     * Reason phrase.
     */
    private final String message;

    /**
     * Header snapshot.
     */
    private final Headers headers;

    /**
     * Response body.
     */
    private final PayloadBody body;

    /**
     * Effective HTTP protocol.
     */
    private final Protocol protocol;

    /**
     * TLS handshake metadata.
     */
    private final TlsHandshake handshake;

    /**
     * Response trailers supplier.
     */
    private final Supplier<Headers> trailers;

    /**
     * Network response metadata.
     */
    private final HttpResponse networkResponse;

    /**
     * Cache response metadata.
     */
    private final HttpResponse cacheResponse;

    /**
     * Prior response metadata.
     */
    private final HttpResponse priorResponse;

    /**
     * Request write timestamp.
     */
    private final long sentRequestAtMillis;

    /**
     * Response read timestamp.
     */
    private final long receivedResponseAtMillis;

    /**
     * Successful flag.
     */
    private final boolean successful;

    /**
     * Closed state.
     */
    private volatile int closed;

    /**
     * Lazily parsed cache control snapshot.
     */
    private volatile HttpCacheControl cacheControl;

    /**
     * Creates a response.
     *
     * @param request request
     * @param code    status code
     * @param message reason phrase
     * @param headers headers
     * @param body    body
     */
    private HttpResponse(final HttpRequest request, final int code, final String message, final Headers headers,
            final PayloadBody body, final Protocol protocol, final TlsHandshake handshake,
            final Supplier<Headers> trailers, final HttpResponse networkResponse, final HttpResponse cacheResponse,
            final HttpResponse priorResponse, final long sentRequestAtMillis, final long receivedResponseAtMillis) {
        this.request = require(request, "HTTP request");
        this.code = validateCode(code);
        this.message = validateMessage(message);
        this.headers = require(headers, "Headers");
        this.body = require(body, "Body");
        this.protocol = protocol == null ? request.url().address().protocol() : protocol;
        this.handshake = handshake;
        this.trailers = require(trailers, "Response trailers");
        this.networkResponse = metadataResponse(networkResponse);
        this.cacheResponse = metadataResponse(cacheResponse);
        this.priorResponse = metadataResponse(priorResponse);
        this.sentRequestAtMillis = validateTimestamp(sentRequestAtMillis, "Sent request timestamp");
        this.receivedResponseAtMillis = validateTimestamp(receivedResponseAtMillis, "Received response timestamp");
        this.successful = code >= HTTP.HTTP_OK && code < HTTP.HTTP_MULT_CHOICE;
    }

    /**
     * Creates a response builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a builder initialized with this response snapshot.
     *
     * @return builder
     */
    public Builder toBuilder() {
        return newBuilder();
    }

    /**
     * Returns a builder initialized with this response snapshot.
     *
     * @return builder
     */
    public Builder newBuilder() {
        final Builder builder = builder().request(request).code(code).message(message).headers(headers).body(body)
                .protocol(protocol).handshake(handshake).trailers(trailers).sentRequestAtMillis(sentRequestAtMillis)
                .receivedResponseAtMillis(receivedResponseAtMillis);
        builder.networkResponse = networkResponse;
        builder.cacheResponse = cacheResponse;
        builder.priorResponse = priorResponse;
        return builder;
    }

    /**
     * Returns request.
     *
     * @return request
     */
    public HttpRequest request() {
        return request;
    }

    /**
     * Returns status code.
     *
     * @return status code
     */
    public int code() {
        return code;
    }

    /**
     * Returns reason phrase.
     *
     * @return reason phrase
     */
    public String message() {
        return message;
    }

    /**
     * Returns headers.
     *
     * @return headers
     */
    public Headers headers() {
        return headers;
    }

    /**
     * Returns body.
     *
     * @return body
     */
    public PayloadBody body() {
        return body;
    }

    /**
     * Returns the effective HTTP protocol.
     *
     * @return protocol
     */
    public Protocol protocol() {
        return protocol;
    }

    /**
     * Returns TLS handshake metadata.
     *
     * @return handshake or null when unavailable
     */
    public TlsHandshake handshake() {
        return handshake;
    }

    /**
     * Returns response trailers.
     *
     * @return trailers
     */
    public Headers trailers() {
        return require(trailers.get(), "Response trailers");
    }

    /**
     * Returns the network response metadata.
     *
     * @return network response or null
     */
    public HttpResponse networkResponse() {
        return networkResponse;
    }

    /**
     * Returns the cache response metadata.
     *
     * @return cache response or null
     */
    public HttpResponse cacheResponse() {
        return cacheResponse;
    }

    /**
     * Returns the prior response metadata.
     *
     * @return prior response or null
     */
    public HttpResponse priorResponse() {
        return priorResponse;
    }

    /**
     * Returns the request write timestamp.
     *
     * @return sent request timestamp in epoch milliseconds, or 0 when unavailable
     */
    public long sentRequestAtMillis() {
        return sentRequestAtMillis;
    }

    /**
     * Returns the response read timestamp.
     *
     * @return received response timestamp in epoch milliseconds, or 0 when unavailable
     */
    public long receivedResponseAtMillis() {
        return receivedResponseAtMillis;
    }

    /**
     * Returns parsed response Cache-Control.
     *
     * @return cache control snapshot
     */
    public HttpCacheControl cacheControl() {
        HttpCacheControl parsed = cacheControl;
        if (parsed == null) {
            parsed = HttpCacheControl.parse(headers);
            cacheControl = parsed;
        }
        return parsed;
    }

    /**
     * Returns authentication challenges for 401 or 407 responses.
     *
     * @return parsed challenges
     */
    public List<Challenge> challenges() {
        final String header = code == HTTP.HTTP_PROXY_AUTH ? HTTP.PROXY_AUTHENTICATE
                : code == HTTP.HTTP_UNAUTHORIZED ? HTTP.WWW_AUTHENTICATE : null;
        if (header == null) {
            return List.of();
        }
        final ArrayList<Challenge> challenges = new ArrayList<>();
        for (final String value : headers.values(header)) {
            challenges.add(Challenge.parse(value));
        }
        return List.copyOf(challenges);
    }

    /**
     * Returns whether the response is 2xx.
     *
     * @return true when successful
     */
    public boolean successful() {
        return successful;
    }

    /**
     * Returns a repeatable snapshot of at most maxBytes from the response body.
     *
     * @param maxBytes maximum bytes to read
     * @return peek body
     */
    public PayloadBody peekBody(final long maxBytes) {
        Assert.isTrue(maxBytes >= Normal._0, () -> new ValidateException("Peek body max bytes must be non-negative"));
        Assert.isTrue(
                body.payload().repeatable(),
                () -> new StatefulException("Streaming HTTP response body cannot be peeked without consuming it"));
        Assert.isTrue(
                maxBytes <= Integer.MAX_VALUE,
                () -> new ValidateException("Peek body max bytes exceeds JVM byte array limit"));
        try (Source input = body.source()) {
            final Buffer output = new Buffer();
            long remaining = maxBytes;
            while (remaining > 0) {
                final long read = input.read(output, Math.min(Normal._8192, remaining));
                if (read < 0) {
                    break;
                }
                remaining -= read;
            }
            return PayloadBody.of(Payload.of(output.readByteArray()), body.media());
        } catch (final IOException e) {
            throw new InternalException("Unable to peek HTTP response body", e);
        }
    }

    /**
     * Reads response body text and closes this response.
     *
     * @return text
     */
    public String text() {
        try {
            final Charset charset = body.media().charset(org.miaixz.bus.core.lang.Charset.UTF_8);
            return new String(bytes(), charset);
        } finally {
            close();
        }
    }

    /**
     * Reads response body bytes.
     *
     * @return bytes
     */
    public byte[] bytes() {
        return Payload.materialize(body.payload(), body.materializeMaxBytes(), "HttpResponse.bytes()");
    }

    /**
     * Reads response body bytes with an explicit materialize threshold.
     *
     * @param maxBytes maximum bytes to materialize
     * @return bytes
     */
    public byte[] bytes(final long maxBytes) {
        return Payload.materialize(body.payload(), maxBytes, "HttpResponse.bytes(long)");
    }

    /**
     * Decodes the response body as a typed object and closes this response.
     *
     * @param codec codec
     * @param type  expected type
     * @param <T>   value type
     * @return decoded value
     */
    public <T> T decode(final DataCodec<?> codec, final Class<T> type) {
        return castDecoded(
                decodeValue(codec, "HttpResponse.decode(DataCodec, Class)"),
                type,
                "HttpResponse.decode(DataCodec, Class)");
    }

    /**
     * Decodes the response body as a typed list and closes this response.
     *
     * @param codec       codec
     * @param elementType expected element type
     * @param <T>         element type
     * @return decoded list
     */
    public <T> List<T> decodeList(final DataCodec<?> codec, final Class<T> elementType) {
        final Object decoded = decodeValue(codec, "HttpResponse.decodeList(DataCodec, Class)");
        if (!(decoded instanceof Collection<?> collection)) {
            throw new ConvertException("Decoded response is not a collection: {}", typeName(decoded));
        }
        final ArrayList<T> values = new ArrayList<>(collection.size());
        int index = 0;
        for (final Object value : collection) {
            values.add(castDecoded(value, elementType, "HttpResponse.decodeList element " + index++));
        }
        return List.copyOf(values);
    }

    /**
     * Decodes the response body as a typed array and closes this response.
     *
     * @param codec       codec
     * @param elementType expected element type
     * @param <T>         element type
     * @return decoded array
     */
    public <T> T[] decodeArray(final DataCodec<?> codec, final Class<T> elementType) {
        final Object decoded = decodeValue(codec, "HttpResponse.decodeArray(DataCodec, Class)");
        if (decoded instanceof Collection<?> collection) {
            return arrayFromCollection(collection, elementType);
        }
        if (decoded == null || !decoded.getClass().isArray()) {
            throw new ConvertException("Decoded response is not an array: {}", typeName(decoded));
        }
        final int length = Array.getLength(decoded);
        @SuppressWarnings("unchecked")
        final T[] values = (T[]) Array.newInstance(require(elementType, "Decoded element type"), length);
        for (int i = 0; i < length; i++) {
            values[i] = castDecoded(Array.get(decoded, i), elementType, "HttpResponse.decodeArray element " + i);
        }
        return values;
    }

    /**
     * Downloads response body to a file.
     *
     * @param target target path
     * @return target path
     */
    public Path download(final Path target) {
        return download(target, null);
    }

    /**
     * Downloads response body to a file and reports progress.
     *
     * @param target   target path
     * @param progress progress listener, receiving written bytes and total bytes
     * @return target path
     */
    public Path download(final Path target, final BiConsumer<Long, Long> progress) {
        final Path checkedTarget = Assert
                .notNull(target, () -> new ValidateException("Download target must not be null"));
        Assert.notNull(checkedTarget.getFileName(), () -> new ValidateException("Download target must not be null"));
        final Path parent = checkedTarget.toAbsolutePath().getParent();
        final Path part = checkedTarget.resolveSibling(checkedTarget.getFileName() + ".part");
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Source input = body.source(); Sink output = IoKit.sink(part)) {
                copyBody(input, output, body.length(), progress);
            }
            Files.move(part, checkedTarget, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return checkedTarget;
        } catch (final IOException | RuntimeException e) {
            try {
                Files.deleteIfExists(part);
            } catch (final IOException ignored) {
                // Best-effort cleanup keeps the original failure.
            }
            if (e instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new InternalException("Unable to download HTTP response", e);
        } finally {
            close();
        }
    }

    /**
     * Downloads response body to a file.
     *
     * @param target target path
     * @return target path
     */
    public Path toFile(final Path target) {
        return download(target);
    }

    /**
     * Downloads response body to a file.
     *
     * @param target target file
     * @return target path
     */
    public Path toFile(final File target) {
        return download(require(target, "Download target").toPath());
    }

    /**
     * Downloads response body to a file.
     *
     * @param target target path
     * @return target path
     */
    public Path toFile(final String target) {
        return download(Path.of(validatePathText(target, "Download target")));
    }

    /**
     * Downloads response body into a folder using response headers or URL filename.
     *
     * @param directory target directory
     * @return target path
     */
    public Path toFolder(final Path directory) {
        final Path checkedDirectory = Assert
                .notNull(directory, () -> new ValidateException("Download directory must not be null"));
        return download(checkedDirectory.resolve(downloadFilename()));
    }

    /**
     * Downloads response body into a folder using response headers or URL filename.
     *
     * @param directory target directory
     * @return target path
     */
    public Path toFolder(final File directory) {
        return toFolder(require(directory, "Download directory").toPath());
    }

    /**
     * Downloads response body into a folder using response headers or URL filename.
     *
     * @param directory target directory
     * @return target path
     */
    public Path toFolder(final String directory) {
        return toFolder(Path.of(validatePathText(directory, "Download directory")));
    }

    /**
     * Closes response body resources.
     */
    @Override
    public void close() {
        if (CLOSED.compareAndSet(this, 0, 1)) {
            body.close();
        }
    }

    /**
     * Copies response body bytes to a sink.
     *
     * @param input    response input
     * @param output   file output
     * @param total    total bytes, or -1 when unknown
     * @param progress progress listener
     */
    private static void copyBody(
            final Source input,
            final Sink output,
            final long total,
            final BiConsumer<Long, Long> progress) {
        final Buffer buffer = new Buffer();
        long written = 0;
        while (true) {
            final long read;
            try {
                read = input.read(buffer, Normal._8192);
            } catch (final IOException e) {
                throw new SocketException("Unable to read HTTP response body", e);
            }
            if (read < 0) {
                break;
            }
            try {
                output.write(buffer, read);
                written += read;
                if (progress != null) {
                    progress.accept(written, total);
                }
            } catch (final IOException e) {
                throw new InternalException("Unable to write HTTP response body", e);
            }
        }
        try {
            output.flush();
        } catch (final IOException e) {
            throw new InternalException("Unable to flush HTTP response body", e);
        }
    }

    /**
     * Resolves a download filename.
     *
     * @return filename
     */
    private String downloadFilename() {
        final String disposition = headers.get(HTTP.CONTENT_DISPOSITION);
        final String headerName = filenameFromDisposition(disposition);
        if (headerName != null) {
            return headerName;
        }
        final String path = request.url().path();
        final int index = path.lastIndexOf(Symbol.C_SLASH);
        final String name = index >= 0 ? path.substring(index + 1) : path;
        return safeFilename(name) ? name : "download.bin";
    }

    /**
     * Extracts a filename from Content-Disposition.
     *
     * @param value header value
     * @return filename or null
     */
    private static String filenameFromDisposition(final String value) {
        if (value == null) {
            return null;
        }
        int start = 0;
        while (start <= value.length()) {
            final int end = value.indexOf(Symbol.C_SEMICOLON, start);
            final String trimmed = value.substring(start, end < 0 ? value.length() : end).trim();
            final int equals = trimmed.indexOf(Symbol.C_EQUAL);
            if (equals <= 0 || !"filename".equalsIgnoreCase(trimmed.substring(0, equals).trim())) {
                if (end < 0) {
                    break;
                }
                start = end + 1;
                continue;
            }
            String name = trimmed.substring(equals + 1).trim();
            if (name.length() >= 2 && name.charAt(0) == Symbol.C_DOUBLE_QUOTES
                    && name.charAt(name.length() - 1) == Symbol.C_DOUBLE_QUOTES) {
                name = name.substring(1, name.length() - 1);
            }
            return safeFilename(name) ? name : null;
        }
        return null;
    }

    /**
     * Returns whether a value is safe as a single path filename.
     *
     * @param value filename
     * @return true when safe
     */
    private static boolean safeFilename(final String value) {
        return StringKit.isNotBlank(value)
                && !StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF, Symbol.C_SLASH, Symbol.C_BACKSLASH);
    }

    /**
     * Validates a path string.
     *
     * @param value path value
     * @param name  value name
     * @return value
     */
    private static String validatePathText(final String value, final String name) {
        Assert.isFalse(
                StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException(name + " must be non-blank and single-line"));
        return value;
    }

    /**
     * Validates status code.
     *
     * @param code code
     * @return code
     */
    private static int validateCode(final int code) {
        Assert.isTrue(
                code >= HTTP.HTTP_CONTINUE && code <= 599,
                () -> new ValidateException("HTTP status code must be between 100 and 599"));
        return code;
    }

    /**
     * Validates reason phrase.
     *
     * @param message message
     * @return message
     */
    private static String validateMessage(final String message) {
        final String current = Assert
                .notNull(message, () -> new ValidateException("HTTP reason phrase must be non-null and single-line"));
        Assert.isFalse(
                StringKit.containsAny(current, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("HTTP reason phrase must be non-null and single-line"));
        return current;
    }

    /**
     * Validates an epoch millisecond timestamp.
     *
     * @param timestamp timestamp
     * @param name      field name
     * @return timestamp
     */
    private static long validateTimestamp(final long timestamp, final String name) {
        Assert.isTrue(timestamp >= Normal._0, () -> new ValidateException(name + " must be non-negative"));
        return timestamp;
    }

    /**
     * Copies response metadata without carrying the response body or nested response links.
     *
     * @param response source response
     * @return metadata response
     */
    private static HttpResponse metadataResponse(final HttpResponse response) {
        if (response == null) {
            return null;
        }
        if (response.body == PayloadBody.empty() && response.networkResponse == null && response.cacheResponse == null
                && response.priorResponse == null) {
            return response;
        }
        return new HttpResponse(response.request, response.code, response.message, response.headers,
                PayloadBody.empty(), response.protocol, response.handshake, response.trailers, null, null, null,
                response.sentRequestAtMillis, response.receivedResponseAtMillis);
    }

    /**
     * Materializes and decodes this response body.
     *
     * @param codec codec
     * @param entry entry name
     * @return decoded value
     */
    private Object decodeValue(final DataCodec<?> codec, final String entry) {
        final DataCodec<?> current = require(codec, "Data codec");
        try {
            final byte[] data = bytes(body.materializeMaxBytes());
            return current.decode(Payload.of(data));
        } finally {
            close();
        }
    }

    /**
     * Casts a decoded value to the expected type.
     *
     * @param value decoded value
     * @param type  expected type
     * @param entry entry name
     * @param <T>   value type
     * @return cast value
     */
    private static <T> T castDecoded(final Object value, final Class<T> type, final String entry) {
        final Class<T> expected = require(type, "Decoded type");
        if (value == null) {
            return null;
        }
        if (!expected.isInstance(value)) {
            throw new ConvertException("{} expected {} but decoded {}", entry, expected.getName(), typeName(value));
        }
        return expected.cast(value);
    }

    /**
     * Converts a decoded collection to a typed array.
     *
     * @param collection  collection
     * @param elementType element type
     * @param <T>         element type
     * @return array
     */
    private static <T> T[] arrayFromCollection(final Collection<?> collection, final Class<T> elementType) {
        final Class<T> expected = require(elementType, "Decoded element type");
        @SuppressWarnings("unchecked")
        final T[] values = (T[]) Array.newInstance(expected, collection.size());
        int index = 0;
        for (final Object value : collection) {
            values[index] = castDecoded(value, expected, "HttpResponse.decodeArray element " + index);
            index++;
        }
        return values;
    }

    /**
     * Returns a readable decoded value type name.
     *
     * @param value value
     * @return type name
     */
    private static String typeName(final Object value) {
        return value == null ? "null" : value.getClass().getName();
    }

    /**
     * Validates required references.
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
     * Builder for responses.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Request candidate.
         */
        private HttpRequest request;

        /**
         * Status code candidate.
         */
        private int code = 200;

        /**
         * Reason phrase candidate.
         */
        private String message = Normal.EMPTY;

        /**
         * Header candidate.
         */
        private Headers headers = Headers.empty();

        /**
         * Body candidate.
         */
        private PayloadBody body = PayloadBody.empty();

        /**
         * Protocol candidate.
         */
        private Protocol protocol;

        /**
         * TLS handshake candidate.
         */
        private TlsHandshake handshake;

        /**
         * Trailers candidate.
         */
        private Supplier<Headers> trailers = Headers::empty;

        /**
         * Network response candidate.
         */
        private HttpResponse networkResponse;

        /**
         * Cache response candidate.
         */
        private HttpResponse cacheResponse;

        /**
         * Prior response candidate.
         */
        private HttpResponse priorResponse;

        /**
         * Sent request timestamp candidate.
         */
        private long sentRequestAtMillis;

        /**
         * Received response timestamp candidate.
         */
        private long receivedResponseAtMillis;

        /**
         * Creates a response builder.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Sets request.
         *
         * @param request request
         * @return this builder
         */
        public Builder request(final HttpRequest request) {
            this.request = require(request, "HTTP request");
            return this;
        }

        /**
         * Sets status code.
         *
         * @param code status code
         * @return this builder
         */
        public Builder code(final int code) {
            this.code = validateCode(code);
            return this;
        }

        /**
         * Sets reason phrase.
         *
         * @param message reason phrase
         * @return this builder
         */
        public Builder message(final String message) {
            this.message = validateMessage(message);
            return this;
        }

        /**
         * Sets headers.
         *
         * @param headers headers
         * @return this builder
         */
        public Builder headers(final Headers headers) {
            this.headers = require(headers, "Headers");
            return this;
        }

        /**
         * Sets body.
         *
         * @param body body
         * @return this builder
         */
        public Builder body(final PayloadBody body) {
            this.body = require(body, "Body");
            return this;
        }

        /**
         * Sets a response body.
         *
         * @param body response body
         * @return this builder
         */
        public Builder body(final ResponseBody body) {
            final ResponseBody current = require(body, "Response body");
            return body(PayloadBody.of(current.payload(), current.media()));
        }

        /**
         * Sets the effective protocol.
         *
         * @param protocol protocol
         * @return this builder
         */
        public Builder protocol(final Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        /**
         * Sets TLS handshake metadata.
         *
         * @param handshake handshake
         * @return this builder
         */
        public Builder handshake(final TlsHandshake handshake) {
            this.handshake = handshake;
            return this;
        }

        /**
         * Sets response trailers.
         *
         * @param trailers trailers
         * @return this builder
         */
        public Builder trailers(final Headers trailers) {
            final Headers current = require(trailers, "Response trailers");
            this.trailers = () -> current;
            return this;
        }

        /**
         * Sets a trailers supplier.
         *
         * @param trailers trailers supplier
         * @return this builder
         */
        public Builder trailers(final Supplier<Headers> trailers) {
            this.trailers = require(trailers, "Response trailers");
            return this;
        }

        /**
         * Sets network response metadata.
         *
         * @param response network response
         * @return this builder
         */
        public Builder networkResponse(final HttpResponse response) {
            this.networkResponse = response;
            return this;
        }

        /**
         * Sets cache response metadata.
         *
         * @param response cache response
         * @return this builder
         */
        public Builder cacheResponse(final HttpResponse response) {
            this.cacheResponse = response;
            return this;
        }

        /**
         * Sets prior response metadata.
         *
         * @param response prior response
         * @return this builder
         */
        public Builder priorResponse(final HttpResponse response) {
            this.priorResponse = response;
            return this;
        }

        /**
         * Sets request write timestamp.
         *
         * @param sentRequestAtMillis epoch milliseconds
         * @return this builder
         */
        public Builder sentRequestAtMillis(final long sentRequestAtMillis) {
            this.sentRequestAtMillis = validateTimestamp(sentRequestAtMillis, "Sent request timestamp");
            return this;
        }

        /**
         * Sets response read timestamp.
         *
         * @param receivedResponseAtMillis epoch milliseconds
         * @return this builder
         */
        public Builder receivedResponseAtMillis(final long receivedResponseAtMillis) {
            this.receivedResponseAtMillis = validateTimestamp(receivedResponseAtMillis, "Received response timestamp");
            return this;
        }

        /**
         * Sets request and response timestamps.
         *
         * @param sentRequestAtMillis      sent request epoch milliseconds
         * @param receivedResponseAtMillis received response epoch milliseconds
         * @return this builder
         */
        public Builder timing(final long sentRequestAtMillis, final long receivedResponseAtMillis) {
            return sentRequestAtMillis(sentRequestAtMillis).receivedResponseAtMillis(receivedResponseAtMillis);
        }

        /**
         * Builds response.
         *
         * @return response
         */
        public HttpResponse build() {
            return new HttpResponse(request, code, message, headers, body, protocol, handshake, trailers,
                    networkResponse, cacheResponse, priorResponse, sentRequestAtMillis, receivedResponseAtMillis);
        }

    }

}
