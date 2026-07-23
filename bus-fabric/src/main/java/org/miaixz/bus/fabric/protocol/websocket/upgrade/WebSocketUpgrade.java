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
package org.miaixz.bus.fabric.protocol.websocket.upgrade;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.network.Ingress;

/**
 * HTTP upgrade adapter for WebSocket handshakes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketUpgrade {

    /**
     * Cryptographically secure random source shared by handshake key generation.
     */
    private static final Random RANDOM = RandomKit.getSecureRandom();

    /**
     * Base64-encoded 16-byte nonce sent in this adapter's client handshake.
     */
    private final String key;

    /**
     * Creates an upgrade adapter with a freshly generated client handshake key.
     */
    public WebSocketUpgrade() {
        this.key = randomKey();
    }

    /**
     * Builds WebSocket upgrade request headers.
     *
     * @param source existing request headers to augment
     * @return headers containing the required WebSocket upgrade fields and this adapter's handshake key
     * @throws ValidateException if {@code source} is {@code null}
     */
    public Headers headers(final Headers source) {
        final Headers checked = require(source, "WebSocket headers");
        return checked.with(Http.Header.UPGRADE, Http.WebSocket.UPGRADE_TOKEN)
                .with(Http.Header.CONNECTION, Http.Header.UPGRADE)
                .with(Http.WebSocket.VERSION, Http.WebSocket.VERSION_13).with(Http.WebSocket.KEY, key);
    }

    /**
     * Builds WebSocket upgrade response headers.
     *
     * @param request validated opening request headers containing {@code Sec-WebSocket-Key}
     * @return required server upgrade headers, including the computed handshake digest
     * @throws ProtocolException if the request key does not decode to 16 bytes
     * @throws ValidateException if the headers or request key are invalid
     */
    public static Headers responseHeaders(final Headers request) {
        final Headers checked = require(request, "WebSocket request headers");
        return Headers.empty().with(Http.Header.UPGRADE, Http.WebSocket.UPGRADE_TOKEN)
                .with(Http.Header.CONNECTION, Http.Header.UPGRADE)
                .with(Http.WebSocket.ACCEPT, acceptKey(checked.get(Http.WebSocket.KEY)));
    }

    /**
     * Creates a Sec-WebSocket-Accept value.
     *
     * @param key base64-encoded nonce from the {@code Sec-WebSocket-Key} header
     * @return base64-encoded SHA-1 handshake digest for {@code Sec-WebSocket-Accept}
     * @throws ProtocolException if the decoded key is not exactly 16 bytes
     * @throws ValidateException if the key is blank or contains a line break
     */
    public static String acceptKey(final String key) {
        final String checkedKey = validateHeader(key, "WebSocket key");
        if (Base64.decode(checkedKey).length != Http.WebSocket.KEY_BYTES) {
            throw new ProtocolException("WebSocket key must decode to 16 bytes");
        }
        return Base64.encode(
                org.miaixz.bus.crypto.Builder.sha1(checkedKey + Http.WebSocket.ACCEPT_GUID, Charset.ISO_8859_1));
    }

    /**
     * Validates an upgrade response.
     *
     * @param status  HTTP response status code
     * @param headers response headers containing {@code Sec-WebSocket-Accept}
     * @throws ProtocolException if the response is not a valid switching-protocols handshake
     * @throws ValidateException if the headers or accept value are invalid
     */
    public void validate(final int status, final Headers headers) {
        final Headers checked = require(headers, "WebSocket response headers");
        if (status != Http.Status.SWITCHING_PROTOCOLS) {
            throw new ProtocolException("WebSocket upgrade response must be 101");
        }
        final String header = checked.get(Http.WebSocket.ACCEPT);
        if (!accept(key, header)) {
            throw new ProtocolException("Invalid WebSocket accept header");
        }
    }

    /**
     * Returns handshake key.
     *
     * @return base64-encoded client handshake key generated for this adapter
     */
    public String key() {
        return key;
    }

    /**
     * Validates a Sec-WebSocket-Accept value.
     *
     * @param key    base64-encoded client handshake key
     * @param accept content of the server's {@code Sec-WebSocket-Accept} header
     * @return {@code true} when the supplied accept value matches the key in constant time
     * @throws ProtocolException if the key does not decode to 16 bytes
     * @throws ValidateException if either header value is blank or contains a line break
     */
    public boolean accept(final String key, final String accept) {
        final String checkedKey = validateHeader(key, "WebSocket key");
        final String checkedAccept = validateHeader(accept, "WebSocket accept");
        final String expected = acceptKey(checkedKey);
        return MessageDigest.isEqual(
                ByteString.encodeString(expected, Charset.ISO_8859_1).toByteArray(),
                ByteString.encodeString(checkedAccept, Charset.ISO_8859_1).toByteArray());
    }

    /**
     * Creates a random WebSocket handshake key.
     *
     * @return base64 handshake key
     */
    private static String randomKey() {
        return Base64.encode(RandomKit.randomBytes(Http.WebSocket.KEY_BYTES, RANDOM));
    }

    /**
     * Converts a WebSocket URI to HTTP.
     *
     * @param uri WebSocket URI whose authority, path, and query are preserved
     * @return equivalent HTTP or HTTPS URI without a fragment
     * @throws ProtocolException if the converted URI cannot be constructed
     */
    public URI httpUri(final URI uri) {
        final URI checked = require(uri, "WebSocket URI");
        final String scheme = Protocol.WSS.name.equalsIgnoreCase(checked.getScheme()) ? Protocol.HTTPS.name
                : Protocol.HTTP.name;
        try {
            return new URI(scheme, checked.getUserInfo(), checked.getHost(), checked.getPort(), checked.getPath(),
                    checked.getQuery(), null);
        } catch (final URISyntaxException e) {
            throw new ProtocolException("Unable to create HTTP upgrade URI", e);
        }
    }

    /**
     * Converts HTTP response URI to WebSocket address.
     *
     * @param uri HTTP response URI whose authority, path, and query are preserved
     * @return equivalent WS or WSS protocol address without a fragment
     * @throws ProtocolException if the converted URI or address cannot be constructed
     */
    public Address address(final URI uri) {
        final URI checked = require(uri, "HTTP upgrade URI");
        final String scheme = Protocol.HTTPS.name.equalsIgnoreCase(checked.getScheme()) ? Protocol.WSS.name
                : Protocol.WS.name;
        try {
            return Address.from(
                    new URI(scheme, checked.getUserInfo(), checked.getHost(), checked.getPort(), checked.getPath(),
                            checked.getQuery(), null));
        } catch (final URISyntaxException e) {
            throw new ProtocolException("Unable to create WebSocket address", e);
        }
    }

    /**
     * Performs a plain WebSocket server upgrade directly on an ingress transport.
     *
     * @param ingress         accepted transport whose source and sink are transferred on success
     * @param path            exact endpoint path required from the request target
     * @param responseHeaders additional server response headers
     * @param validator       callback that validates the parsed opening request headers
     * @return upgraded transport views and validated request headers
     * @throws RuntimeException if request validation or the handshake fails; the ingress is closed before rethrowing
     */
    public static Result upgrade(
            final Ingress ingress,
            final String path,
            final Headers responseHeaders,
            final Consumer<Headers> validator) {
        final Ingress current = require(ingress, "WebSocket ingress");
        try {
            return upgrade(current.source(), current.sink(), path, responseHeaders, validator);
        } catch (final RuntimeException e) {
            close(current);
            throw e;
        }
    }

    /**
     * Performs a secure WebSocket server upgrade on an already handshaken TLS source and sink.
     *
     * @param source          handshaken TLS source transferred to the result on success
     * @param sink            handshaken TLS sink transferred to the result on success
     * @param path            exact endpoint path required from the request target
     * @param responseHeaders additional server response headers
     * @param validator       callback that validates the parsed opening request headers
     * @return upgraded transport views and validated request headers
     * @throws RuntimeException if request validation or the handshake fails; both transport views are closed before
     *                          rethrowing
     */
    public static Result upgrade(
            final Source source,
            final Sink sink,
            final String path,
            final Headers responseHeaders,
            final Consumer<Headers> validator) {
        final Source currentSource = require(source, "WebSocket upgrade source");
        final Sink currentSink = require(sink, "WebSocket upgrade sink");
        final String requiredPath = validatePath(path);
        final Headers configured = require(responseHeaders, "WebSocket response headers");
        final Consumer<Headers> openingValidator = require(validator, "WebSocket opening validator");
        try {
            final BufferSource buffered = IoKit.buffer(currentSource);
            final Request request = readRequest(buffered);
            validateRequest(request, requiredPath);
            openingValidator.accept(request.headers());
            final Headers response = serverResponse(request.headers(), configured);
            writeResponse(currentSink, response);
            return new Result(buffered, currentSink, request.headers());
        } catch (final RuntimeException e) {
            close(currentSource);
            close(currentSink);
            throw e;
        }
    }

    /**
     * Reads one bounded HTTP/1.1 upgrade request while retaining bytes buffered after its header terminator.
     *
     * @param source buffered transport source positioned at the request line
     * @return parsed request-line fields and immutable header snapshot
     * @throws ProtocolException if the request line or a header line is malformed or exceeds configured limits
     * @throws SocketException   if the request cannot be read from the transport
     */
    private static Request readRequest(final BufferSource source) {
        try {
            long total = Normal.LONG_ZERO;
            final String requestLine = source.readUtf8LineStrict(Builder.WEBSOCKET_UPGRADE_MAX_LINE_BYTES);
            total += requestLine.length() + Normal._2;
            final String[] parts = requestLine.split(" ", Normal._3);
            if (parts.length != Normal._3) {
                throw new ProtocolException("Invalid WebSocket HTTP request line");
            }
            final Headers.Builder headers = Headers.builder();
            while (true) {
                final String line = source.readUtf8LineStrict(Builder.WEBSOCKET_UPGRADE_MAX_LINE_BYTES);
                total += line.length() + Normal._2;
                if (total > Builder.WEBSOCKET_UPGRADE_MAX_HEADER_BYTES) {
                    throw new ProtocolException("WebSocket upgrade headers exceed 64 KiB");
                }
                if (line.isEmpty()) {
                    break;
                }
                final int colon = line.indexOf(Symbol.C_COLON);
                if (colon <= Normal._0) {
                    throw new ProtocolException("Invalid WebSocket HTTP header line");
                }
                headers.add(line.substring(Normal._0, colon).trim(), line.substring(colon + Normal._1).trim());
            }
            return new Request(parts[Normal._0], parts[Normal._1], parts[Normal._2], headers.build());
        } catch (final IOException e) {
            throw new SocketException("Unable to read WebSocket upgrade request", e);
        }
    }

    /**
     * Validates the complete server-side WebSocket request contract.
     *
     * @param request      parsed opening request to validate
     * @param requiredPath exact configured endpoint path
     * @throws ProtocolException if any required WebSocket opening condition is not satisfied
     */
    private static void validateRequest(final Request request, final String requiredPath) {
        if (!Http.Method.GET.value().equals(request.method())) {
            throw new ProtocolException("WebSocket upgrade method must be GET");
        }
        if (!Protocol.HTTP_1_1.name.equals(request.version())) {
            throw new ProtocolException("WebSocket upgrade requires HTTP/1.1");
        }
        final String target = request.target();
        final int query = target.indexOf(Symbol.C_QUESTION_MARK);
        final String requestPath = query < Normal._0 ? target : target.substring(Normal._0, query);
        if (!requiredPath.equals(requestPath)) {
            throw new ProtocolException("WebSocket upgrade path does not match the configured endpoint");
        }
        if (!token(request.headers().get(Http.Header.UPGRADE), Http.WebSocket.UPGRADE_TOKEN)) {
            throw new ProtocolException("WebSocket Upgrade header is invalid");
        }
        if (!token(request.headers().get(Http.Header.CONNECTION), Http.Header.UPGRADE)) {
            throw new ProtocolException("WebSocket Connection header must contain Upgrade");
        }
        if (!Http.WebSocket.VERSION_13.equals(request.headers().get(Http.WebSocket.VERSION))) {
            throw new ProtocolException("WebSocket version must be 13");
        }
        acceptKey(request.headers().get(Http.WebSocket.KEY));
    }

    /**
     * Builds and validates the server response, including a configured subprotocol selection.
     *
     * @param request    validated client opening headers
     * @param configured additional response headers and optional selected subprotocol
     * @return complete response headers with mandatory handshake fields restored
     * @throws ProtocolException if a configured subprotocol was not offered by the client
     */
    private static Headers serverResponse(final Headers request, final Headers configured) {
        Headers response = responseHeaders(request);
        for (final Map.Entry<String, List<String>> entry : configured.asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                response = response.with(entry.getKey(), value);
            }
        }
        final String selected = response.get(Http.WebSocket.PROTOCOL);
        if (selected != null && !token(request.get(Http.WebSocket.PROTOCOL), selected)) {
            throw new ProtocolException("Selected WebSocket subprotocol was not requested by the client");
        }
        return response.with(Http.Header.UPGRADE, Http.WebSocket.UPGRADE_TOKEN)
                .with(Http.Header.CONNECTION, Http.Header.UPGRADE)
                .with(Http.WebSocket.ACCEPT, acceptKey(request.get(Http.WebSocket.KEY)));
    }

    /**
     * Writes and flushes a complete HTTP 101 response before handing transport ownership to a session.
     *
     * @param sink    transport sink that receives the serialized response
     * @param headers complete response headers to serialize
     * @throws SocketException if the response cannot be written and flushed
     */
    private static void writeResponse(final Sink sink, final Headers headers) {
        final Buffer response = new Buffer().writeUtf8("HTTP/1.1 101 Switching Protocols\r\n");
        for (final Map.Entry<String, List<String>> entry : headers.asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                response.writeUtf8(entry.getKey()).writeUtf8(": ").writeUtf8(value).writeUtf8("\r\n");
            }
        }
        response.writeUtf8("\r\n");
        try {
            sink.write(response, response.size());
            sink.flush();
        } catch (final IOException e) {
            throw new SocketException("Unable to write WebSocket upgrade response", e);
        }
    }

    /**
     * Tests for one case-insensitive comma-separated HTTP token.
     *
     * @param value    comma-separated header value, or {@code null}
     * @param expected token to locate without regard to case
     * @return {@code true} when one trimmed field value equals the expected token
     */
    private static boolean token(final String value, final String expected) {
        if (value == null) {
            return false;
        }
        final String target = expected.toLowerCase(Locale.ROOT);
        for (final String candidate : value.split(Symbol.COMMA)) {
            if (target.equals(candidate.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates the configured endpoint path.
     *
     * @param path endpoint path to validate
     * @return validated non-blank, single-line path beginning with {@code /}
     * @throws ValidateException if the path is invalid or lacks a leading slash
     */
    private static String validatePath(final String path) {
        final String value = validateHeader(path, "WebSocket path");
        if (!value.startsWith(Symbol.SLASH)) {
            throw new ValidateException("WebSocket path must start with '/'");
        }
        return value;
    }

    /**
     * Closes one failed-upgrade resource without replacing the handshake failure.
     *
     * @param resource failed-upgrade transport resource to close, or {@code null}
     */
    private static void close(final AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (final Exception ignored) {
            // The handshake failure remains authoritative.
        }
    }

    /**
     * Validates a single-line header.
     *
     * @param value header or path value to validate
     * @param name  logical value name included in the validation error
     * @return unchanged non-blank, single-line text
     * @throws ValidateException if the value is blank or contains a line break
     */
    private static String validateHeader(final String value, final String name) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value;
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  logical reference name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Successful server upgrade transport snapshot.
     *
     * @param source         source retaining any bytes prefetched after the HTTP header terminator
     * @param sink           response sink
     * @param requestHeaders validated request headers
     */
    public record Result(Source source, Sink sink, Headers requestHeaders) {

        /**
         * Creates a validated result.
         *
         * @param source         source retaining prefetched post-header bytes
         * @param sink           sink used to write the upgrade response
         * @param requestHeaders validated request headers
         * @throws ValidateException if any component is {@code null}
         */
        public Result {
            source = require(source, "WebSocket result source");
            sink = require(sink, "WebSocket result sink");
            requestHeaders = require(requestHeaders, "WebSocket request headers");
        }

    }

    /**
     * Parsed HTTP request-line and header snapshot.
     *
     * @param method  HTTP method parsed from the request line
     * @param target  origin-form request target, optionally including a query
     * @param version HTTP version parsed from the request line
     * @param headers immutable parsed request headers
     */
    private record Request(String method, String target, String version, Headers headers) {
    }

}
