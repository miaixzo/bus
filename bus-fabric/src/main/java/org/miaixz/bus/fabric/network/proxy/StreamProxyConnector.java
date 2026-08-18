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
package org.miaixz.bus.fabric.network.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.lang.exception.ConnectionException;
import org.miaixz.bus.core.lang.exception.ConnectionException.Delivery;
import org.miaixz.bus.core.lang.exception.ConnectionException.Phase;
import org.miaixz.bus.core.lang.exception.ConnectionException.Scope;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Shared HTTP CONNECT and SOCKS5 CONNECT handshakes for every stream protocol.
 *
 * @author Kimi Liu
 */
public final class StreamProxyConnector {

    /**
     * SOCKS protocol version implemented by this connector.
     */
    private static final byte SOCKS_VERSION = 0x05;

    /**
     * Creates a stateless stream-proxy handshake state.
     */
    public StreamProxyConnector() {
        // No initialization required.
    }

    /**
     * Establishes the selected proxy route over an already connected proxy transport.
     * <p>
     * Direct routes require no handshake. HTTP routes use CONNECT and SOCKS routes use SOCKS5 CONNECT. Transport I/O
     * failures are converted to structured route failures before any application data is sent; authentication and
     * protocol failures retain their dedicated exception types.
     *
     * @param connection   established transport connected to the selected proxy
     * @param target       logical destination requested by the application protocol
     * @param proxy        resolved direct, HTTP, or SOCKS route plan
     * @param timeout      connect, read, and write timeout policy for the handshake
     * @param cancellation cancellation scope governing the handshake
     * @throws ConnectionException if intermediary communication fails before application-data delivery
     * @throws AuthorizedException if the intermediary requires or rejects authentication
     * @throws ProtocolException   if the resolved plan or intermediary response violates its protocol
     */
    public void connect(
            final Connection connection,
            final Address target,
            final ProxyPlan proxy,
            final Timeout timeout,
            final Cancellation cancellation) {
        try {
            if (proxy.isDirect()) {
                return;
            }
            if (proxy.isHttp()) {
                httpConnect(connection, target, proxy.authorization(), timeout, cancellation);
                return;
            }
            if (proxy.isSocks()) {
                socksConnect(connection, target, timeout, cancellation);
                return;
            }
            throw new ProtocolException("Stream connector requires a resolved proxy plan");
        } catch (final ConnectionException e) {
            throw e;
        } catch (final SocketException e) {
            throw routeFailure(proxy, Phase.ROUTE_NEGOTIATION, "Unable to negotiate stream proxy route", e);
        }
    }

    /**
     * Opens a SOCKS5 UDP ASSOCIATE control session and returns its relay address.
     *
     * @param connection   established stream transport connected to the SOCKS server
     * @param proxy        resolved SOCKS route used to create the control transport
     * @param timeout      read and write timeout policy for the SOCKS handshake
     * @param cancellation cancellation scope governing the handshake
     * @return UDP relay address advertised by the SOCKS server
     * @throws ConnectionException if control-channel communication fails before datagram delivery
     * @throws AuthorizedException if the SOCKS server selects an unsupported authentication method
     * @throws ProtocolException   if the server returns an invalid or unsuccessful response
     */
    public Address udpAssociate(
            final Connection connection,
            final ProxyPlan proxy,
            final Timeout timeout,
            final Cancellation cancellation) {
        try {
            writeAll(
                    connection.sink(),
                    new Buffer().write(new byte[] { SOCKS_VERSION, 0x01, 0x00 }),
                    timeout,
                    cancellation);
            final byte[] selection = readExact(connection.source(), 2, timeout, cancellation);
            if (selection[0] != SOCKS_VERSION || selection[1] != 0x00) {
                throw new AuthorizedException("SOCKS proxy requires an unsupported authentication method");
            }
            writeAll(
                    connection.sink(),
                    new Buffer()
                            .write(new byte[] { SOCKS_VERSION, 0x03, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }),
                    timeout,
                    cancellation);
            final byte[] header = readExact(connection.source(), 4, timeout, cancellation);
            if (header[0] != SOCKS_VERSION || header[1] != 0x00 || header[2] != 0x00) {
                throw new ProtocolException("SOCKS5 UDP ASSOCIATE failed with reply " + (header[1] & 0xff));
            }
            final String host = switch (header[3]) {
                case 0x01 -> addressText(readExact(connection.source(), 4, timeout, cancellation));
                case 0x03 -> new String(readExact(
                        connection.source(),
                        readExact(connection.source(), 1, timeout, cancellation)[0] & 0xff,
                        timeout,
                        cancellation), Charset.UTF_8);
                case 0x04 -> addressText(readExact(connection.source(), 16, timeout, cancellation));
                default -> throw new ProtocolException("Unsupported SOCKS UDP relay address type");
            };
            final byte[] port = readExact(connection.source(), 2, timeout, cancellation);
            return new Address(Protocol.UDP.name, host, ((port[0] & 0xff) << 8) | (port[1] & 0xff), Symbol.SLASH);
        } catch (final ConnectionException e) {
            throw e;
        } catch (final SocketException e) {
            throw routeFailure(proxy, Phase.ROUTE_NEGOTIATION, "Unable to negotiate SOCKS UDP relay", e);
        }
    }

    /**
     * Creates a structured, pre-delivery intermediary failure for ordered route selection.
     *
     * @param proxy   resolved route that failed
     * @param phase   connection phase in which the failure occurred
     * @param message human-readable failure description
     * @param cause   underlying socket failure
     * @return structured failure that permits switching to the next ordered route candidate
     */
    private static ConnectionException routeFailure(
            final ProxyPlan proxy,
            final Phase phase,
            final String message,
            final Throwable cause) {
        return new ConnectionException(phase, Scope.ROUTE, Delivery.NOT_STARTED, proxy.id(), message, cause);
    }

    /**
     * Performs an HTTP CONNECT handshake over an established proxy transport.
     *
     * @param connection    transport connected to the HTTP proxy
     * @param target        logical tunnel destination
     * @param authorization headers forwarded only to the proxy during CONNECT
     * @param timeout       read and write timeout policy
     * @param cancellation  cancellation scope governing the handshake
     * @throws AuthorizedException if the proxy returns status 407
     * @throws ProtocolException   if the proxy returns a non-successful or unsupported response
     * @throws SocketException     if the CONNECT request or response cannot be transferred
     */
    private static void httpConnect(
            final Connection connection,
            final Address target,
            final Headers authorization,
            final Timeout timeout,
            final Cancellation cancellation) {
        final String authority = authority(target);
        final StringBuilder text = new StringBuilder().append(Http.Method.CONNECT.value()).append(Symbol.C_SPACE)
                .append(authority).append(Symbol.C_SPACE).append(Protocol.HTTP_1_1).append(Symbol.CRLF)
                .append(Http.Header.HOST).append(Symbol.COLON).append(Symbol.SPACE).append(authority)
                .append(Symbol.CRLF).append(Http.Header.PROXY_CONNECTION).append(Symbol.COLON).append(Symbol.SPACE)
                .append(Http.Header.CONNECTION_KEEP_ALIVE).append(Symbol.CRLF);
        for (final Map.Entry<String, List<String>> entry : authorization.asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                text.append(entry.getKey()).append(Symbol.COLON).append(Symbol.SPACE).append(value).append(Symbol.CRLF);
            }
        }
        text.append(Symbol.CRLF);
        writeAll(
                connection.sink(),
                new Buffer().write(ByteString.encodeString(text.toString(), Charset.US_ASCII).toByteArray()),
                timeout,
                cancellation);
        final String response = readHeader(connection.source(), timeout, cancellation);
        if (response.startsWith("HTTP/1.1 407 ") || response.startsWith("HTTP/1.0 407 ")) {
            throw new AuthorizedException(authorization.size() == 0 ? "HTTP CONNECT proxy authentication is required"
                    : "HTTP CONNECT proxy rejected the configured authentication");
        }
        if (!response.startsWith("HTTP/1.1 200 ") && !response.startsWith("HTTP/1.0 200 ")
                && !response.startsWith("HTTP/2 200 ")) {
            throw new ProtocolException("HTTP CONNECT tunnel failed");
        }
    }

    /**
     * Performs a no-authentication SOCKS5 CONNECT handshake.
     *
     * @param connection   transport connected to the SOCKS5 server
     * @param target       logical stream destination
     * @param timeout      read and write timeout policy
     * @param cancellation cancellation scope governing the handshake
     * @throws AuthorizedException if the server requires an unsupported authentication method
     * @throws ProtocolException   if the server rejects the target or returns malformed framing
     * @throws SocketException     if handshake bytes cannot be transferred
     */
    private static void socksConnect(
            final Connection connection,
            final Address target,
            final Timeout timeout,
            final Cancellation cancellation) {
        writeAll(
                connection.sink(),
                new Buffer().write(new byte[] { SOCKS_VERSION, 0x01, 0x00 }),
                timeout,
                cancellation);
        final byte[] selection = readExact(connection.source(), 2, timeout, cancellation);
        if (selection[0] != SOCKS_VERSION || selection[1] != 0x00) {
            throw new AuthorizedException("SOCKS proxy requires an unsupported authentication method");
        }
        writeAll(connection.sink(), new Buffer().write(socksRequest(target)), timeout, cancellation);
        final byte[] header = readExact(connection.source(), 4, timeout, cancellation);
        if (header[0] != SOCKS_VERSION || header[2] != 0x00 || header[1] != 0x00) {
            throw new ProtocolException("SOCKS5 CONNECT failed with reply " + (header[1] & 0xff));
        }
        final int length = switch (header[3]) {
            case 0x01 -> 4;
            case 0x03 -> readExact(connection.source(), 1, timeout, cancellation)[0] & 0xff;
            case 0x04 -> 16;
            default -> throw new ProtocolException("Unsupported SOCKS address type");
        };
        readExact(connection.source(), length + 2, timeout, cancellation);
    }

    /**
     * Encodes a SOCKS5 CONNECT request for one logical destination.
     *
     * @param target logical stream destination
     * @return complete SOCKS5 CONNECT request bytes
     * @throws ProtocolException if the target host exceeds the SOCKS domain-name limit
     */
    private static byte[] socksRequest(final Address target) {
        final byte[] ipv4 = ipv4(target.host());
        final byte[] host = ipv4 == null ? ByteString.encodeString(target.host(), Charset.UTF_8).toByteArray() : ipv4;
        if (host.length > 255) {
            throw new ProtocolException("SOCKS target host is too long");
        }
        final ByteBuffer value = ByteBuffer.allocate((ipv4 == null ? 7 : 6) + host.length);
        value.put(SOCKS_VERSION).put((byte) 0x01).put((byte) 0x00);
        if (ipv4 == null) {
            value.put((byte) 0x03).put((byte) host.length);
        } else {
            value.put((byte) 0x01);
        }
        return value.put(host).putShort((short) target.port()).array();
    }

    /**
     * Reads one complete HTTP proxy response header with a fixed safety limit.
     *
     * @param source       proxy response source
     * @param timeout      timeout policy supplying the read duration
     * @param cancellation cancellation scope checked during incremental reads
     * @return header text including its terminating empty line
     * @throws SocketException   if the source fails or reaches EOF before the header completes
     * @throws ProtocolException if the header exceeds the configured 64-KiB limit
     */
    private static String readHeader(final Source source, final Timeout timeout, final Cancellation cancellation) {
        configure(source.timeout(), timeout.read());
        final StringBuilder header = new StringBuilder();
        final Buffer one = new Buffer();
        while (header.length() < Builder.BYTES_64_KIB) {
            cancellation.throwIfCancelled();
            try {
                final long read = source.read(one, Normal._1);
                if (read < 0) {
                    throw new SocketException("HTTP CONNECT response reached EOF");
                }
                if (read == 0) {
                    Thread.onSpinWait();
                    continue;
                }
                header.append((char) (one.readByte() & 0xff));
            } catch (final IOException e) {
                throw new SocketException("HTTP CONNECT read failed", e);
            }
            if (header.indexOf(Symbol.CRLF + Symbol.CRLF) >= 0) {
                return header.toString();
            }
        }
        throw new ProtocolException("HTTP CONNECT response header is too large");
    }

    /**
     * Writes the complete handshake buffer to a proxy transport.
     *
     * @param sink         proxy transport sink
     * @param value        buffer consumed by the write
     * @param timeout      timeout policy supplying the write duration
     * @param cancellation cancellation scope checked before writing
     * @throws SocketException if the sink rejects or cannot complete the write
     */
    private static void writeAll(
            final Sink sink,
            final Buffer value,
            final Timeout timeout,
            final Cancellation cancellation) {
        configure(sink.timeout(), timeout.write());
        cancellation.throwIfCancelled();
        try {
            sink.write(value, value.size());
        } catch (final IOException e) {
            throw new SocketException("Proxy handshake write failed", e);
        }
    }

    /**
     * Reads an exact number of proxy-handshake bytes.
     *
     * @param source       proxy transport source
     * @param length       required byte count
     * @param timeout      timeout policy supplying the read duration
     * @param cancellation cancellation scope checked during incremental reads
     * @return byte array of exactly {@code length} bytes
     * @throws SocketException if the source fails, reaches EOF, or cannot materialize the buffered bytes
     */
    private static byte[] readExact(
            final Source source,
            final int length,
            final Timeout timeout,
            final Cancellation cancellation) {
        configure(source.timeout(), timeout.read());
        final Buffer buffer = new Buffer();
        while (buffer.size() < length) {
            cancellation.throwIfCancelled();
            try {
                final long read = source.read(buffer, length - buffer.size());
                if (read < 0) {
                    throw new SocketException("Proxy handshake reached EOF");
                }
            } catch (final IOException e) {
                throw new SocketException("Proxy handshake read failed", e);
            }
        }
        try {
            return buffer.readByteArray(length);
        } catch (final IOException e) {
            throw new SocketException("Unable to materialize proxy response", e);
        }
    }

    /**
     * Applies a positive finite duration to a core I/O timeout object.
     *
     * @param target   mutable core timeout, or {@code null} when the transport exposes none
     * @param duration requested duration; zero, negative, and {@code null} values leave the target unchanged
     */
    private static void configure(final org.miaixz.bus.core.io.timout.Timeout target, final Duration duration) {
        if (target != null && duration != null && !duration.isZero() && !duration.isNegative()) {
            target.timeout(duration.toNanos(), TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Formats an HTTP CONNECT authority and brackets IPv6 literals when required.
     *
     * @param target logical tunnel destination
     * @return {@code host:port} authority suitable for the request line and Host header
     */
    private static String authority(final Address target) {
        final String host = target.host().indexOf(Symbol.C_COLON) >= 0
                ? Symbol.BRACKET_LEFT + target.host() + Symbol.BRACKET_RIGHT
                : target.host();
        return host + Symbol.C_COLON + target.port();
    }

    /**
     * Attempts to encode an IPv4 literal without resolving domain names.
     *
     * @param host possible IPv4 literal
     * @return four IPv4 bytes, or {@code null} when the host is not an IPv4 literal
     */
    private static byte[] ipv4(final String host) {
        try {
            return ByteBuffer.allocate(Integer.BYTES).putInt((int) NetKit.ipv4ToLong(host)).array();
        } catch (final RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Converts raw SOCKS relay address bytes to canonical host text.
     *
     * @param bytes four-byte IPv4 or sixteen-byte IPv6 address
     * @return canonical textual IP address
     * @throws ProtocolException if the byte count is not a valid IP address representation
     */
    private static String addressText(final byte[] bytes) {
        try {
            return InetAddress.getByAddress(bytes).getHostAddress();
        } catch (final UnknownHostException e) {
            throw new ProtocolException("Invalid SOCKS relay address", e);
        }
    }

}
