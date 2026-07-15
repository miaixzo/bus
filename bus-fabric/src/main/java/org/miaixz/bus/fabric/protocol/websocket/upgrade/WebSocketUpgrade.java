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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.Random;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Headers;

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

}
