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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.fabric.Builder;

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
    TCP(Protocol.TCP.name, true, false, Protocol.TCP),

    /**
     * UDP transport.
     */
    UDP(Protocol.UDP.name, false, false, Protocol.UDP),

    /**
     * TLS transport over a connection-oriented channel.
     */
    TLS(Protocol.TLS.name, true, true, Protocol.TLS),

    /**
     * KCP transport over UDP.
     */
    KCP(Builder.SOCKET_X_KCP_SCHEME, false, false, Protocol.UDP);

    /**
     * Scheme lookup table.
     */
    private static final Map<String, Transport> BY_SCHEME = Map.ofEntries(
            Map.entry(TCP.scheme, TCP),
            Map.entry(Protocol.SOCKET.name, TCP),
            Map.entry(Builder.AIO_SCHEME, TCP),
            Map.entry(Protocol.HTTP.name, TCP),
            Map.entry(Protocol.WS.name, TCP),
            Map.entry(UDP.scheme, UDP),
            Map.entry(KCP.scheme, KCP),
            Map.entry(TLS.scheme, TLS),
            Map.entry(Protocol.HTTPS.name, TLS),
            Map.entry(Protocol.WSS.name, TLS));

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
        this.protocol = Assert.notNull(protocol, () -> new ValidateException("Transport protocol must not be null"));
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
        final String normalized = scheme == null ? null : scheme.trim();
        Assert.isTrue(
                UrlKit.isScheme(normalized),
                () -> new ValidateException("Network transport scheme must be non-blank and single-line"));
        // Single-line normalization keeps the lookup table deterministic.
        return normalized.toLowerCase(Locale.ROOT);
    }

}
