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
package org.miaixz.bus.fabric.network;

import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Transport families used by fabric network implementations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum Transport {

    /**
     * TCP transport.
     */
    TCP("tcp", true, false, Protocol.TCP),

    /**
     * UDP transport.
     */
    UDP("udp", false, false, Protocol.UDP),

    /**
     * TLS transport over a connection-oriented channel.
     */
    TLS("tls", true, true, Protocol.TLS),

    /**
     * KCP transport over UDP.
     */
    KCP("kcp", false, false, Protocol.UDP);

    /**
     * Scheme lookup table.
     */
    private static final Map<String, Transport> BY_SCHEME = Map.ofEntries(
            Map.entry("tcp", TCP),
            Map.entry("socket", TCP),
            Map.entry("aio", TCP),
            Map.entry("http", TCP),
            Map.entry("ws", TCP),
            Map.entry("udp", UDP),
            Map.entry("kcp", KCP),
            Map.entry("tls", TLS),
            Map.entry("https", TLS),
            Map.entry("wss", TLS));

    /**
     * Default scheme.
     */
    private final String scheme;

    /**
     * Connection-oriented flag.
     */
    private final boolean connectionOriented;

    /**
     * Secure transport flag.
     */
    private final boolean secure;

    /**
     * Bus-core protocol.
     */
    private final Protocol protocol;

    /**
     * Creates a transport enum constant.
     *
     * @param scheme             default scheme
     * @param connectionOriented connection-oriented flag
     * @param secure             secure flag
     * @param protocol           bus-core protocol
     */
    Transport(final String scheme, final boolean connectionOriented, final boolean secure, final Protocol protocol) {
        this.scheme = normalize(scheme);
        this.connectionOriented = connectionOriented;
        this.secure = secure;
        if (protocol == null) {
            throw new ValidateException("Transport protocol must not be null");
        }
        this.protocol = protocol;
    }

    /**
     * Maps a scheme to a transport.
     *
     * @param scheme scheme
     * @return transport
     */
    public static Transport fromScheme(final String scheme) {
        final String normalized = normalize(scheme);
        final Transport transport = BY_SCHEME.get(normalized);
        if (transport == null) {
            throw new ValidateException("Unsupported network transport scheme: " + normalized);
        }
        return transport;
    }

    /**
     * Returns the default scheme.
     *
     * @return scheme
     */
    public String scheme() {
        return scheme;
    }

    /**
     * Returns whether this transport is connection-oriented.
     *
     * @return true when connection-oriented
     */
    public boolean connectionOriented() {
        return connectionOriented;
    }

    /**
     * Returns whether this transport is secure.
     *
     * @return true when secure
     */
    public boolean secure() {
        return secure;
    }

    /**
     * Returns the bus-core protocol.
     *
     * @return protocol
     */
    public Protocol protocol() {
        return protocol;
    }

    /**
     * Normalizes a scheme.
     *
     * @param scheme scheme
     * @return normalized scheme
     */
    private static String normalize(final String scheme) {
        if (StringKit.isBlank(scheme) || StringKit.containsAny(scheme, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Network transport scheme must be non-blank and single-line");
        }
        // Single-line normalization keeps the lookup table deterministic.
        return scheme.trim().toLowerCase(Locale.ROOT);
    }

}
