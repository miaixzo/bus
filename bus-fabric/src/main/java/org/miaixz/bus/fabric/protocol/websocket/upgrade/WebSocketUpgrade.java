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
import org.miaixz.bus.core.net.HTTP;
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
     * Handshake random source.
     */
    private static final Random RANDOM = RandomKit.getSecureRandom();

    /**
     * Handshake key.
     */
    private final String key;

    /**
     * Creates an upgrade adapter.
     */
    public WebSocketUpgrade() {
        this.key = randomKey();
    }

    /**
     * Builds WebSocket upgrade request headers.
     *
     * @param source source headers
     * @return upgrade headers
     */
    public Headers headers(final Headers source) {
        final Headers checked = require(source, "WebSocket headers");
        return checked.with(HTTP.UPGRADE, HTTP.WEBSOCKET).with(HTTP.CONNECTION, HTTP.UPGRADE)
                .with(HTTP.SEC_WEBSOCKET_VERSION, HTTP.SEC_WEBSOCKET_VERSION_13).with(HTTP.SEC_WEBSOCKET_KEY, key);
    }

    /**
     * Builds WebSocket upgrade response headers.
     *
     * @param request request headers
     * @return response headers
     */
    public static Headers responseHeaders(final Headers request) {
        final Headers checked = require(request, "WebSocket request headers");
        return Headers.empty().with(HTTP.UPGRADE, HTTP.WEBSOCKET).with(HTTP.CONNECTION, HTTP.UPGRADE)
                .with(HTTP.SEC_WEBSOCKET_ACCEPT, acceptKey(checked.get(HTTP.SEC_WEBSOCKET_KEY)));
    }

    /**
     * Creates a Sec-WebSocket-Accept value.
     *
     * @param key Sec-WebSocket-Key value
     * @return accept value
     */
    public static String acceptKey(final String key) {
        final String checkedKey = validateHeader(key, "WebSocket key");
        if (Base64.decode(checkedKey).length != HTTP.SEC_WEBSOCKET_KEY_BYTES) {
            throw new ProtocolException("WebSocket key must decode to 16 bytes");
        }
        return Base64.encode(
                org.miaixz.bus.crypto.Builder.sha1(checkedKey + HTTP.SEC_WEBSOCKET_ACCEPT_GUID, Charset.ISO_8859_1));
    }

    /**
     * Validates an upgrade response.
     *
     * @param status  response status
     * @param headers response headers
     */
    public void validate(final int status, final Headers headers) {
        final Headers checked = require(headers, "WebSocket response headers");
        if (status != HTTP.HTTP_SWITCHING_PROTOCOL) {
            throw new ProtocolException("WebSocket upgrade response must be 101");
        }
        final String header = checked.get(HTTP.SEC_WEBSOCKET_ACCEPT);
        if (!accept(key, header)) {
            throw new ProtocolException("Invalid WebSocket accept header");
        }
    }

    /**
     * Returns handshake key.
     *
     * @return key
     */
    public String key() {
        return key;
    }

    /**
     * Validates a Sec-WebSocket-Accept value.
     *
     * @param key    key
     * @param accept accept value
     * @return true when accepted
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
        return Base64.encode(RandomKit.randomBytes(HTTP.SEC_WEBSOCKET_KEY_BYTES, RANDOM));
    }

    /**
     * Converts a WebSocket URI to HTTP.
     *
     * @param uri WebSocket URI
     * @return HTTP URI
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
     * @param uri URI
     * @return address
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
     * @param ingress         accepted ingress
     * @param path            required request path
     * @param responseHeaders configured response headers
     * @param validator       opening request validator
     * @return upgraded source, sink and request headers
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
     * @param source          handshaken TLS source
     * @param sink            handshaken TLS sink
     * @param path            required request path
     * @param responseHeaders configured response headers
     * @param validator       opening request validator
     * @return upgraded source, sink and request headers
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
     * @param source buffered source
     * @return request snapshot
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
     * @param request      request
     * @param requiredPath configured path
     */
    private static void validateRequest(final Request request, final String requiredPath) {
        if (!HTTP.GET.equals(request.method())) {
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
        if (!token(request.headers().get(HTTP.UPGRADE), HTTP.WEBSOCKET)) {
            throw new ProtocolException("WebSocket Upgrade header is invalid");
        }
        if (!token(request.headers().get(HTTP.CONNECTION), HTTP.UPGRADE)) {
            throw new ProtocolException("WebSocket Connection header must contain Upgrade");
        }
        if (!HTTP.SEC_WEBSOCKET_VERSION_13.equals(request.headers().get(HTTP.SEC_WEBSOCKET_VERSION))) {
            throw new ProtocolException("WebSocket version must be 13");
        }
        acceptKey(request.headers().get(HTTP.SEC_WEBSOCKET_KEY));
    }

    /**
     * Builds and validates the server response, including a configured subprotocol selection.
     *
     * @param request    request headers
     * @param configured configured response headers
     * @return response headers
     */
    private static Headers serverResponse(final Headers request, final Headers configured) {
        Headers response = responseHeaders(request);
        for (final Map.Entry<String, List<String>> entry : configured.asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                response = response.with(entry.getKey(), value);
            }
        }
        final String selected = response.get(HTTP.SEC_WEBSOCKET_PROTOCOL);
        if (selected != null && !token(request.get(HTTP.SEC_WEBSOCKET_PROTOCOL), selected)) {
            throw new ProtocolException("Selected WebSocket subprotocol was not requested by the client");
        }
        return response.with(HTTP.UPGRADE, HTTP.WEBSOCKET).with(HTTP.CONNECTION, HTTP.UPGRADE)
                .with(HTTP.SEC_WEBSOCKET_ACCEPT, acceptKey(request.get(HTTP.SEC_WEBSOCKET_KEY)));
    }

    /**
     * Writes and flushes a complete HTTP 101 response before handing transport ownership to a session.
     *
     * @param sink    response sink
     * @param headers response headers
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
     * @param value    header value
     * @param expected expected token
     * @return true when present
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
     * @param path path
     * @return path
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
     * @param resource resource
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
     * @param value value
     * @param name  name
     * @return value
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
     * @param value value
     * @param name  name
     * @param <T>   value type
     * @return value
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
     * @param method  method
     * @param target  request target
     * @param version HTTP version
     * @param headers request headers
     */
    private record Request(String method, String target, String version, Headers headers) {
    }

}
