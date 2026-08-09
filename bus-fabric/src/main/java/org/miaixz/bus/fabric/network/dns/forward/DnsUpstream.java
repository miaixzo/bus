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
package org.miaixz.bus.fabric.network.dns.forward;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Locale;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.network.dns.secure.DnsDohServer;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;

/**
 * Immutable upstream DNS server used by forwarding resolution.
 *
 * @author Kimi Liu
 */
public final class DnsUpstream {

    /**
     * Upstream host.
     */
    private final String host;

    /**
     * Upstream DNS port.
     */
    private final int port;

    /**
     * Per-query timeout.
     */
    private final Duration timeout;

    /**
     * Upstream transport.
     */
    private final DnsUpstreamTransport transport;

    /**
     * DNS-over-HTTPS endpoint URI, or {@code null} for socket transports.
     */
    private final URI endpointUri;

    /**
     * TLS policy used by secure upstream transports, or {@code null} to use JDK defaults.
     */
    private final TlsPolicy tlsPolicy;

    /**
     * Creates an upstream definition.
     *
     * @param host    upstream host or address literal
     * @param port    upstream DNS port from 1 through 65535
     * @param timeout per-query timeout
     */
    public DnsUpstream(final String host, final int port, final Duration timeout) {
        this(host, port, timeout, DnsUpstreamTransport.UDP);
    }

    /**
     * Creates an upstream definition.
     *
     * @param host      upstream host or address literal
     * @param port      upstream DNS port from 1 through 65535
     * @param timeout   per-query timeout
     * @param transport upstream transport
     */
    public DnsUpstream(final String host, final int port, final Duration timeout,
            final DnsUpstreamTransport transport) {
        this(host, port, timeout, transport, null, null);
    }

    /**
     * Creates an upstream definition.
     *
     * @param host        upstream host or address literal
     * @param port        upstream DNS port from 1 through 65535
     * @param timeout     per-query timeout
     * @param transport   upstream transport
     * @param endpointUri DNS-over-HTTPS endpoint URI, or {@code null} for socket transports
     * @param tlsPolicy   TLS policy used by secure upstream transports, or {@code null}
     */
    private DnsUpstream(final String host, final int port, final Duration timeout, final DnsUpstreamTransport transport,
            final URI endpointUri, final TlsPolicy tlsPolicy) {
        if (host == null || host.isBlank()) {
            throw new ValidateException("DNS upstream host must be non-blank");
        }
        if (port < Normal._1 || port > Normal._65535) {
            throw new ValidateException("DNS upstream port must be from 1 through 65535");
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new ValidateException("DNS upstream timeout must be positive");
        }
        if (transport == null) {
            throw new ValidateException("DNS upstream transport must not be null");
        }
        if (transport == DnsUpstreamTransport.DOH && endpointUri == null) {
            throw new ValidateException("DNS-over-HTTPS upstream requires an endpoint URI");
        }
        if (transport != DnsUpstreamTransport.DOH && endpointUri != null) {
            throw new ValidateException("DNS socket upstream must not carry an endpoint URI");
        }
        this.host = host.trim();
        this.port = port;
        this.timeout = timeout;
        this.transport = transport;
        this.endpointUri = endpointUri;
        this.tlsPolicy = tlsPolicy;
    }

    /**
     * Creates a UDP upstream.
     *
     * @param host upstream host or address literal
     * @param port upstream DNS port from 1 through 65535
     * @return upstream definition with a three-second timeout
     */
    public static DnsUpstream udp(final String host, final int port) {
        return new DnsUpstream(host, port, Duration.ofSeconds(3));
    }

    /**
     * Creates a UDP upstream with an explicit timeout.
     *
     * @param host    upstream host or address literal
     * @param port    upstream DNS port from 1 through 65535
     * @param timeout per-query timeout
     * @return upstream definition
     */
    public static DnsUpstream udp(final String host, final int port, final Duration timeout) {
        return new DnsUpstream(host, port, timeout, DnsUpstreamTransport.UDP);
    }

    /**
     * Creates a TCP upstream.
     *
     * @param host upstream host or address literal
     * @param port upstream DNS port from 1 through 65535
     * @return upstream definition with a three-second timeout
     */
    public static DnsUpstream tcp(final String host, final int port) {
        return new DnsUpstream(host, port, Duration.ofSeconds(3), DnsUpstreamTransport.TCP);
    }

    /**
     * Creates a TCP upstream with an explicit timeout.
     *
     * @param host    upstream host or address literal
     * @param port    upstream DNS port from 1 through 65535
     * @param timeout per-query timeout
     * @return upstream definition
     */
    public static DnsUpstream tcp(final String host, final int port, final Duration timeout) {
        return new DnsUpstream(host, port, timeout, DnsUpstreamTransport.TCP);
    }

    /**
     * Creates a DNS-over-TLS upstream.
     *
     * @param host upstream host or address literal
     * @param port upstream DNS port from 1 through 65535
     * @return upstream definition with a three-second timeout
     */
    public static DnsUpstream dot(final String host, final int port) {
        return dot(host, port, Duration.ofSeconds(3));
    }

    /**
     * Creates a DNS-over-TLS upstream with an explicit timeout.
     *
     * @param host    upstream host or address literal
     * @param port    upstream DNS port from 1 through 65535
     * @param timeout per-query timeout
     * @return upstream definition
     */
    public static DnsUpstream dot(final String host, final int port, final Duration timeout) {
        return dot(host, port, timeout, null);
    }

    /**
     * Creates a DNS-over-TLS upstream with an explicit TLS policy.
     *
     * @param host      upstream host or address literal
     * @param port      upstream DNS port from 1 through 65535
     * @param timeout   per-query timeout
     * @param tlsPolicy TLS policy used by the upstream client, or {@code null} to use JDK defaults
     * @return upstream definition
     */
    public static DnsUpstream dot(
            final String host,
            final int port,
            final Duration timeout,
            final TlsPolicy tlsPolicy) {
        return new DnsUpstream(host, port, timeout, DnsUpstreamTransport.DOT, null, tlsPolicy);
    }

    /**
     * Creates a DNS-over-HTTPS upstream.
     *
     * @param endpoint absolute HTTP or HTTPS DNS-over-HTTPS endpoint URI
     * @return upstream definition with a three-second timeout
     */
    public static DnsUpstream doh(final String endpoint) {
        return doh(endpoint, Duration.ofSeconds(3));
    }

    /**
     * Creates a DNS-over-HTTPS upstream with an explicit timeout.
     *
     * @param endpoint absolute HTTP or HTTPS DNS-over-HTTPS endpoint URI
     * @param timeout  per-query timeout
     * @return upstream definition
     */
    public static DnsUpstream doh(final String endpoint, final Duration timeout) {
        return doh(URI.create(endpoint), timeout);
    }

    /**
     * Creates a DNS-over-HTTPS upstream with an explicit timeout.
     *
     * @param endpoint absolute HTTP or HTTPS DNS-over-HTTPS endpoint URI
     * @param timeout  per-query timeout
     * @return upstream definition
     */
    public static DnsUpstream doh(final URI endpoint, final Duration timeout) {
        return doh(endpoint, timeout, null);
    }

    /**
     * Creates a DNS-over-HTTPS upstream with an explicit TLS policy.
     *
     * @param endpoint  absolute HTTP or HTTPS DNS-over-HTTPS endpoint URI
     * @param timeout   per-query timeout
     * @param tlsPolicy TLS policy used by the upstream client, or {@code null} to use JDK defaults
     * @return upstream definition
     */
    public static DnsUpstream doh(final URI endpoint, final Duration timeout, final TlsPolicy tlsPolicy) {
        final URI normalized = normalizeDohEndpoint(endpoint);
        return new DnsUpstream(normalized.getHost(), effectivePort(normalized), timeout, DnsUpstreamTransport.DOH,
                normalized, tlsPolicy);
    }

    /**
     * Creates a DNS-over-QUIC upstream.
     *
     * @param host upstream host or address literal
     * @param port upstream DNS-over-QUIC port from 1 through 65535
     * @return upstream definition with a three-second timeout
     */
    public static DnsUpstream doq(final String host, final int port) {
        return doq(host, port, Duration.ofSeconds(3));
    }

    /**
     * Creates a DNS-over-QUIC upstream with an explicit timeout.
     *
     * @param host    upstream host or address literal
     * @param port    upstream DNS-over-QUIC port from 1 through 65535
     * @param timeout per-query timeout
     * @return upstream definition
     */
    public static DnsUpstream doq(final String host, final int port, final Duration timeout) {
        return doq(host, port, timeout, null);
    }

    /**
     * Creates a DNS-over-QUIC upstream with an explicit TLS policy.
     *
     * @param host      upstream host or address literal
     * @param port      upstream DNS-over-QUIC port from 1 through 65535
     * @param timeout   per-query timeout
     * @param tlsPolicy TLS policy used by the upstream client, or {@code null} to use QUIC defaults
     * @return upstream definition
     */
    public static DnsUpstream doq(
            final String host,
            final int port,
            final Duration timeout,
            final TlsPolicy tlsPolicy) {
        return new DnsUpstream(host, port, timeout, DnsUpstreamTransport.DOQ, null, tlsPolicy);
    }

    /**
     * Returns the upstream host.
     *
     * @return upstream host or address literal
     */
    public String host() {
        return host;
    }

    /**
     * Returns the upstream port.
     *
     * @return DNS port
     */
    public int port() {
        return port;
    }

    /**
     * Returns the per-query timeout.
     *
     * @return positive timeout
     */
    public Duration timeout() {
        return timeout;
    }

    /**
     * Returns the upstream transport.
     *
     * @return upstream transport
     */
    public DnsUpstreamTransport transport() {
        return transport;
    }

    /**
     * Returns the DNS-over-HTTPS endpoint URI.
     *
     * @return DoH endpoint URI, or {@code null} for socket transports
     */
    public URI endpointUri() {
        return endpointUri;
    }

    /**
     * Returns the TLS policy for secure upstream transports.
     *
     * @return TLS policy, or {@code null} when the JDK default TLS context is used
     */
    public TlsPolicy tlsPolicy() {
        return tlsPolicy;
    }

    /**
     * Creates a socket address for this upstream.
     *
     * @return internet socket address
     */
    public InetSocketAddress socketAddress() {
        return new InetSocketAddress(host, port);
    }

    /**
     * Returns the upstream host and port authority.
     *
     * @return upstream authority in {@code host:port} form
     */
    public String authority() {
        return host + Symbol.COLON + port;
    }

    /**
     * Returns the stable upstream target used for diagnostics and health tracking.
     *
     * @return endpoint URI for DoH, otherwise {@link #authority()}
     */
    public String target() {
        return endpointUri == null ? authority() : endpointUri.toASCIIString();
    }

    /**
     * Returns the stable health key for this upstream.
     *
     * @return health key including target and transport
     */
    public String healthKey() {
        return target() + Symbol.OR + transport;
    }

    /**
     * Normalizes a DNS-over-HTTPS endpoint URI.
     *
     * @param endpoint candidate DoH endpoint
     * @return normalized endpoint URI
     */
    private static URI normalizeDohEndpoint(final URI endpoint) {
        if (endpoint == null || endpoint.getScheme() == null || endpoint.getHost() == null) {
            throw new ValidateException("DNS-over-HTTPS upstream URI must be absolute");
        }
        final String scheme = endpoint.getScheme().toLowerCase(Locale.ROOT);
        if (!Protocol.HTTPS.name.equals(scheme) && !Protocol.HTTP.name.equals(scheme)) {
            throw new ValidateException("DNS-over-HTTPS upstream URI scheme must be http or https");
        }
        if (endpoint.getRawQuery() != null || endpoint.getRawFragment() != null) {
            throw new ValidateException("DNS-over-HTTPS upstream URI must not contain query or fragment");
        }
        final String path = endpoint.getRawPath() == null || endpoint.getRawPath().isBlank() ? DnsDohServer.PATH
                : endpoint.getRawPath();
        try {
            return new URI(scheme, endpoint.getUserInfo(), endpoint.getHost(), effectivePort(endpoint), path, null,
                    null);
        } catch (final URISyntaxException e) {
            throw new ValidateException("DNS-over-HTTPS upstream URI is invalid", e);
        }
    }

    /**
     * Resolves the effective URI port after applying the scheme default.
     *
     * @param endpoint normalized or candidate endpoint URI
     * @return effective TCP port
     */
    private static int effectivePort(final URI endpoint) {
        if (endpoint.getPort() > 0) {
            return endpoint.getPort();
        }
        return Protocol.HTTP.name.equalsIgnoreCase(endpoint.getScheme()) ? 80 : 443;
    }

}
