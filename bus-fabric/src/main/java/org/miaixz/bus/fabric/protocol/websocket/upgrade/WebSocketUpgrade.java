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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
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
     * RFC 6455 accept GUID.
     */
    private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    /**
     * Handshake random source.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

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
        if (source == null) {
            throw new ValidateException("WebSocket headers must not be null");
        }
        return source.with("Upgrade", "websocket").with("Connection", "Upgrade").with("Sec-WebSocket-Version", "13")
                .with("Sec-WebSocket-Key", key);
    }

    /**
     * Validates an upgrade response.
     *
     * @param status  response status
     * @param headers response headers
     */
    public void validate(final int status, final Headers headers) {
        if (headers == null) {
            throw new ValidateException("WebSocket response headers must not be null");
        }
        if (status != 101) {
            throw new ProtocolException("WebSocket upgrade response must be 101");
        }
        final String header = headers.get("Sec-WebSocket-Accept");
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
        final String expected = Base64.encode(sha1((checkedKey + GUID).getBytes(Charset.ISO_8859_1)));
        return MessageDigest.isEqual(expected.getBytes(Charset.ISO_8859_1), checkedAccept.getBytes(Charset.ISO_8859_1));
    }

    /**
     * Creates a random WebSocket handshake key.
     *
     * @return base64 handshake key
     */
    private static String randomKey() {
        final byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.encode(bytes);
    }

    /**
     * Computes SHA-1 digest.
     *
     * @param value value
     * @return digest
     */
    private static byte[] sha1(final byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(value);
        } catch (final NoSuchAlgorithmException e) {
            throw new ProtocolException("SHA-1 digest is not available", e);
        }
    }

    /**
     * Converts a WebSocket URI to HTTP.
     *
     * @param uri WebSocket URI
     * @return HTTP URI
     */
    public URI httpUri(final URI uri) {
        if (uri == null) {
            throw new ValidateException("WebSocket URI must not be null");
        }
        final String scheme = "wss".equalsIgnoreCase(uri.getScheme()) ? "https" : "http";
        try {
            return new URI(scheme, uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(),
                    null);
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
        if (uri == null) {
            throw new ValidateException("HTTP upgrade URI must not be null");
        }
        final String scheme = "https".equalsIgnoreCase(uri.getScheme()) ? "wss" : "ws";
        try {
            return Address.from(
                    new URI(scheme, uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(),
                            null));
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

}
