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
package org.miaixz.bus.fabric;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.PORT;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Immutable protocol address that never resolves DNS during construction or access.
 *
 * @param scheme normalized scheme
 * @param host   normalized host
 * @param port   effective port
 * @param path   normalized path
 * @author Kimi Liu
 * @since Java 21+
 */
public record Address(String scheme, String host, int port, String path) {

    /**
     * Creates a validated address.
     *
     * @param scheme normalized scheme
     * @param host   normalized host
     * @param port   effective port
     * @param path   normalized path
     */
    public Address {
        scheme = normalizeToken(scheme, "Scheme");
        host = normalizeToken(host, "Host");
        port = validatePort(port);
        path = normalizePath(path);
        protocolFor(scheme);
    }

    /**
     * Parses an address string.
     *
     * @param value address string
     * @return parsed address
     */
    public static Address parse(final String value) {
        if (value == null || value.isBlank()) {
            throw new ValidateException("Address value must be non-blank");
        }
        try {
            return from(new URI(value.trim()));
        } catch (final URISyntaxException e) {
            throw new ProtocolException("Invalid address URI", e);
        }
    }

    /**
     * Creates an address from a URI.
     *
     * @param uri source URI
     * @return address
     */
    public static Address from(final URI uri) {
        if (uri == null) {
            throw new ValidateException("URI must not be null");
        }
        final String scheme = normalizeToken(uri.getScheme(), "Scheme");
        final String host = normalizeHost(uri);
        final int port = effectivePort(scheme, uri.getPort());
        final String path = normalizePath(uri.getRawPath());
        return new Address(scheme, host, port, path);
    }

    /**
     * Returns the bus-core protocol.
     *
     * @return protocol
     */
    public Protocol protocol() {
        return protocolFor(scheme);
    }

    /**
     * Returns whether this address uses a secure protocol.
     *
     * @return true for secure protocols
     */
    public boolean secure() {
        return Protocol.HTTPS.name.equals(scheme) || Protocol.WSS.name.equals(scheme)
                || Protocol.TLS.name.equals(scheme);
    }

    /**
     * Creates an unresolved socket address.
     *
     * @return unresolved socket address
     */
    public InetSocketAddress socket() {
        return InetSocketAddress.createUnresolved(host, port);
    }

    /**
     * Converts this address to a URI.
     *
     * @return URI
     */
    public URI toUri() {
        try {
            return new URI(scheme, null, host, port, path, null, null);
        } catch (final URISyntaxException e) {
            throw new ProtocolException("Unable to create address URI", e);
        }
    }

    /**
     * Converts a scheme and optional port into an effective port.
     *
     * @param scheme normalized scheme
     * @param port   parsed port
     * @return effective port
     */
    private static int effectivePort(final String scheme, final int port) {
        if (port >= Normal._0) {
            return validatePort(port);
        }
        if (Protocol.HTTP.name.equals(scheme) || Protocol.WS.name.equals(scheme)) {
            return PORT._80.getPort();
        }
        if (Protocol.HTTPS.name.equals(scheme) || Protocol.WSS.name.equals(scheme)
                || Protocol.TLS.name.equals(scheme)) {
            return PORT._443.getPort();
        }
        if (Protocol.TCP.name.equals(scheme) || Protocol.UDP.name.equals(scheme) || Protocol.SOCKET.name.equals(scheme)
                || Builder.AIO_SCHEME.equals(scheme) || Builder.SOCKET_X_KCP_SCHEME.equals(scheme)) {
            throw new ValidateException("Scheme requires an explicit port: " + scheme);
        }
        throw new ProtocolException("Unsupported address scheme: " + scheme);
    }

    /**
     * Maps a scheme to a bus-core protocol.
     *
     * @param scheme normalized scheme
     * @return protocol
     */
    private static Protocol protocolFor(final String scheme) {
        if (Protocol.HTTP.name.equals(scheme)) {
            return Protocol.HTTP;
        }
        if (Protocol.HTTPS.name.equals(scheme)) {
            return Protocol.HTTPS;
        }
        if (Protocol.WS.name.equals(scheme)) {
            return Protocol.WS;
        }
        if (Protocol.WSS.name.equals(scheme)) {
            return Protocol.WSS;
        }
        if (Protocol.TCP.name.equals(scheme) || Builder.AIO_SCHEME.equals(scheme)) {
            return Protocol.TCP;
        }
        if (Protocol.UDP.name.equals(scheme) || Builder.SOCKET_X_KCP_SCHEME.equals(scheme)) {
            return Protocol.UDP;
        }
        if (Protocol.TLS.name.equals(scheme)) {
            return Protocol.TLS;
        }
        if (Protocol.SOCKET.name.equals(scheme)) {
            return Protocol.SOCKET;
        }
        throw new ProtocolException("Unsupported address scheme: " + scheme);
    }

    /**
     * Normalizes URI host without resolving it.
     *
     * @param uri URI
     * @return normalized host
     */
    private static String normalizeHost(final URI uri) {
        String value = uri.getHost();
        if (value == null && uri.getRawAuthority() != null) {
            value = uri.getRawAuthority();
            final int at = value.lastIndexOf(Symbol.C_AT);
            if (at >= 0) {
                value = value.substring(at + 1);
            }
            final int colon = value.lastIndexOf(Symbol.C_COLON);
            if (colon > 0 && value.indexOf(Symbol.C_BRACKET_RIGHT) < colon) {
                value = value.substring(0, colon);
            }
            if (value.startsWith(Symbol.BRACKET_LEFT) && value.endsWith(Symbol.BRACKET_RIGHT)) {
                value = value.substring(1, value.length() - 1);
            }
        }
        return normalizeToken(value, "Host");
    }

    /**
     * Normalizes a single-line token.
     *
     * @param value token
     * @param name  token name
     * @return normalized token
     */
    private static String normalizeToken(final String value, final String name) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes a path without touching the network.
     *
     * @param value path
     * @return normalized path
     */
    private static String normalizePath(final String value) {
        String normalized = value == null || value.isBlank() ? Symbol.SLASH : value;
        if (!normalized.startsWith(Symbol.SLASH)) {
            normalized = Symbol.SLASH + normalized;
        }
        try {
            normalized = new URI(null, null, normalized, null).normalize().getPath();
        } catch (final URISyntaxException e) {
            throw new ProtocolException("Invalid address path", e);
        }
        return normalized == null || normalized.isBlank() ? Symbol.SLASH : normalized;
    }

    /**
     * Validates effective ports.
     *
     * @param port port
     * @return port
     */
    private static int validatePort(final int port) {
        if (port < Normal._1 || port > Normal._65535) {
            throw new ValidateException("Port must be between 1 and 65535");
        }
        return port;
    }

}
