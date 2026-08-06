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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.network.udp.UdpDatagramCodec;

/**
 * Strict SOCKS5 UDP relay framing with fragmentation disabled.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Socks5UdpCodec implements UdpDatagramCodec {

    /**
     * Physical UDP relay address returned by SOCKS5 UDP ASSOCIATE.
     */
    private final Address relay;

    /**
     * Creates a strict SOCKS5 UDP datagram codec.
     *
     * @param relay non-null physical UDP relay address returned by the SOCKS server
     * @throws ValidateException if {@code relay} is {@code null}
     */
    public Socks5UdpCodec(final Address relay) {
        this.relay = Assert.notNull(relay, () -> new ValidateException("SOCKS UDP relay must not be null"));
    }

    /**
     * Returns the physical relay endpoint supplied by the SOCKS server.
     *
     * @return non-null UDP relay address
     */
    @Override
    public Address relay() {
        return relay;
    }

    /**
     * Wraps one logical datagram in the SOCKS5 UDP request header.
     * <p>
     * The codec writes an unfragmented frame and preserves IPv4, IPv6, or domain-name addressing for the logical
     * target.
     *
     * @param target  logical destination encoded into the SOCKS5 frame
     * @param payload application datagram payload
     * @return payload containing the SOCKS5 UDP header followed by the original bytes
     * @throws ProtocolException if the target address cannot be represented by SOCKS5
     */
    @Override
    public Payload encode(final Address target, final Payload payload) {
        final byte[] address = address(target.host());
        final byte type = type(target.host(), address);
        final byte[] body = payload.bytes(65_536L);
        final ByteBuffer packet = ByteBuffer.allocate(6 + address.length + (type == 0x03 ? 1 : 0) + body.length);
        packet.putShort((short) 0).put((byte) 0).put(type);
        if (type == 0x03) {
            packet.put((byte) address.length);
        }
        packet.put(address).putShort((short) target.port()).put(body);
        return Payload.of(packet.array());
    }

    /**
     * Removes the SOCKS5 UDP response header and returns its application payload.
     *
     * @param target  logical peer associated with the connected session
     * @param payload framed datagram received from the physical SOCKS relay
     * @return decoded application datagram payload
     * @throws ProtocolException if the frame is fragmented, truncated, or uses an unsupported address type
     */
    @Override
    public Payload decode(final Address target, final Payload payload) {
        final ByteBuffer packet = ByteBuffer.wrap(payload.bytes(65_536L));
        if (packet.remaining() < 4 || packet.getShort() != 0 || packet.get() != 0) {
            throw new ProtocolException("Invalid or fragmented SOCKS5 UDP datagram");
        }
        final int length = switch (packet.get()) {
            case 0x01 -> 4;
            case 0x03 -> packet.hasRemaining() ? packet.get() & 0xff : -1;
            case 0x04 -> 16;
            default -> -1;
        };
        if (length < 0 || packet.remaining() < length + 2) {
            throw new ProtocolException("Invalid SOCKS5 UDP target address");
        }
        packet.position(packet.position() + length + 2);
        final byte[] body = new byte[packet.remaining()];
        packet.get(body);
        return Payload.of(body);
    }

    /**
     * Resolves the SOCKS5 address-type byte for an encoded host.
     *
     * @param host    original host text
     * @param address encoded address bytes
     * @return {@code 0x01} for IPv4, {@code 0x04} for IPv6, or {@code 0x03} for a domain name
     */
    private static byte type(final String host, final byte[] address) {
        if (ipv4(host) != null) {
            return 0x01;
        }
        return host.indexOf(Symbol.C_COLON) >= 0 && address.length == 16 ? (byte) 0x04 : (byte) 0x03;
    }

    /**
     * Encodes a target host using the byte representation required by its SOCKS5 address type.
     *
     * @param host IPv4, IPv6, or domain-name host text
     * @return raw IP bytes or UTF-8 domain-name bytes
     * @throws ProtocolException if IPv6 text is invalid or a domain name exceeds 255 encoded bytes
     */
    private static byte[] address(final String host) {
        final byte[] ipv4 = ipv4(host);
        if (ipv4 != null) {
            return ipv4;
        }
        if (host.indexOf(Symbol.C_COLON) >= 0) {
            try {
                return InetAddress.getByName(host).getAddress();
            } catch (final UnknownHostException e) {
                throw new ProtocolException("Invalid IPv6 SOCKS target", e);
            }
        }
        final byte[] value = ByteString.encodeString(host, Charset.UTF_8).toByteArray();
        if (value.length > 255) {
            throw new ProtocolException("SOCKS target host is too long");
        }
        return value;
    }

    /**
     * Attempts to encode an IPv4 literal without resolving domain names.
     *
     * @param host possible IPv4 literal
     * @return four IPv4 bytes, or {@code null} when {@code host} is not an IPv4 literal
     */
    private static byte[] ipv4(final String host) {
        try {
            return ByteBuffer.allocate(Integer.BYTES).putInt((int) NetKit.ipv4ToLong(host)).array();
        } catch (final RuntimeException ignored) {
            return null;
        }
    }

}
