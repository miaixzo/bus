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
package org.miaixz.bus.fabric.network.dns.server;

import org.miaixz.bus.core.net.Protocol;

/**
 * DNS server listener transport.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum DnsTransport {

    /**
     * Plain DNS over UDP.
     */
    UDP(Protocol.UDP.name, null),

    /**
     * Plain DNS over TCP.
     */
    TCP(Protocol.TCP.name, null),

    /**
     * DNS over HTTPS using HTTP/1.1.
     */
    DOH(Protocol.HTTP.name, null),

    /**
     * DNS over QUIC using QUIC datagrams and streams.
     */
    DOQ(Protocol.QUIC.name, "doq"),

    /**
     * DNS over TLS using TCP length-prefixed DNS messages inside a TLS connection.
     */
    DOT(Protocol.TLS.name, "dot");

    /**
     * Underlying bus protocol token.
     */
    private final String protocolName;

    /**
     * TLS or QUIC ALPN token, or {@code null} when the transport does not define ALPN.
     */
    private final String alpn;

    /**
     * Creates a DNS transport definition.
     *
     * @param protocolName underlying bus protocol token
     * @param alpn         TLS or QUIC ALPN token, or {@code null}
     */
    DnsTransport(final String protocolName, final String alpn) {
        this.protocolName = protocolName;
        this.alpn = alpn;
    }

    /**
     * Returns the underlying bus protocol token.
     *
     * @return protocol token
     */
    public String protocolName() {
        return protocolName;
    }

    /**
     * Returns the TLS or QUIC ALPN token for this DNS transport.
     *
     * @return ALPN token, or {@code null} when unavailable
     */
    public String alpn() {
        return alpn;
    }

}
