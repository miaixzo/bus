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

import java.net.InetSocketAddress;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;

/**
 * Immutable DNS listener endpoint.
 *
 * @author Kimi Liu
 */
public final class DnsEndpoint {

    /**
     * Listener transport.
     */
    private final DnsTransport transport;

    /**
     * Bind host.
     */
    private final String host;

    /**
     * Bind port.
     */
    private final int port;

    /**
     * Creates an endpoint.
     *
     * @param transport listener transport
     * @param host      bind host
     * @param port      bind port from 1 through 65535
     */
    public DnsEndpoint(final DnsTransport transport, final String host, final int port) {
        if (transport == null) {
            throw new ValidateException("DNS endpoint transport must not be null");
        }
        if (host == null || host.isBlank()) {
            throw new ValidateException("DNS endpoint host must be non-blank");
        }
        if (port < Normal._1 || port > Normal._65535) {
            throw new ValidateException("DNS endpoint port must be from 1 through 65535");
        }
        this.transport = transport;
        this.host = host.trim();
        this.port = port;
    }

    /**
     * Creates a UDP endpoint.
     *
     * @param host bind host
     * @param port bind port
     * @return UDP endpoint
     */
    public static DnsEndpoint udp(final String host, final int port) {
        return new DnsEndpoint(DnsTransport.UDP, host, port);
    }

    /**
     * Creates a TCP endpoint.
     *
     * @param host bind host
     * @param port bind port
     * @return TCP endpoint
     */
    public static DnsEndpoint tcp(final String host, final int port) {
        return new DnsEndpoint(DnsTransport.TCP, host, port);
    }

    /**
     * Creates a DNS-over-HTTPS endpoint.
     *
     * @param host bind host
     * @param port bind port
     * @return DNS-over-HTTPS endpoint
     */
    public static DnsEndpoint doh(final String host, final int port) {
        return new DnsEndpoint(DnsTransport.DOH, host, port);
    }

    /**
     * Creates a DNS-over-QUIC endpoint.
     *
     * @param host bind host
     * @param port bind port
     * @return DNS-over-QUIC endpoint
     */
    public static DnsEndpoint doq(final String host, final int port) {
        return new DnsEndpoint(DnsTransport.DOQ, host, port);
    }

    /**
     * Creates a DNS-over-TLS endpoint.
     *
     * @param host bind host
     * @param port bind port
     * @return DNS-over-TLS endpoint
     */
    public static DnsEndpoint dot(final String host, final int port) {
        return new DnsEndpoint(DnsTransport.DOT, host, port);
    }

    /**
     * Returns the listener transport.
     *
     * @return DNS listener transport
     */
    public DnsTransport transport() {
        return transport;
    }

    /**
     * Returns the bind host.
     *
     * @return bind host
     */
    public String host() {
        return host;
    }

    /**
     * Returns the bind port.
     *
     * @return bind port
     */
    public int port() {
        return port;
    }

    /**
     * Creates a socket address for binding.
     *
     * @return internet socket address
     */
    public InetSocketAddress socketAddress() {
        return new InetSocketAddress(host, port);
    }

    /**
     * Returns the endpoint host and port authority.
     *
     * @return endpoint authority in {@code host:port} form
     */
    public String authority() {
        return host + Symbol.COLON + port;
    }

    /**
     * Creates a Fabric address for this endpoint with the supplied protocol.
     *
     * @param protocol Fabric protocol
     * @return Fabric address
     */
    public Address fabricAddress(final Protocol protocol) {
        if (protocol == null) {
            throw new ValidateException("DNS endpoint fabric protocol must not be null");
        }
        return new Address(protocol.name, host, port, Symbol.SLASH);
    }

}
