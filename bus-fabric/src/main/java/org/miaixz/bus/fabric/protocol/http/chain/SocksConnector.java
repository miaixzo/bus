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
package org.miaixz.bus.fabric.protocol.http.chain;

import java.nio.ByteBuffer;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.NetKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Strict SOCKS5 CONNECT handshake.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class SocksConnector {

    /**
     * SOCKS protocol version.
     */
    private static final byte VERSION = 0x05;

    /**
     * Opens a SOCKS5 route.
     *
     * @param connection   proxy connection
     * @param target       target address
     * @param timeout      timeout policy
     * @param cancellation cancellation scope
     */
    void connect(
            final Connection connection,
            final Address target,
            final Timeout timeout,
            final Cancellation cancellation) {
        cancellation.throwIfCancelled();
        ProxyIo.writeAll(
                connection.sink(),
                new Buffer().write(new byte[] { VERSION, 0x01, 0x00 }),
                timeout,
                cancellation);
        final byte[] selection = ProxyIo
                .readExact(connection.source(), 2, timeout, "SOCKS method selection timed out", cancellation);
        if (selection[0] != VERSION || selection[1] != 0x00) {
            throw new ProtocolException("SOCKS proxy requires an unsupported authentication method");
        }
        ProxyIo.writeAll(connection.sink(), new Buffer().write(request(target)), timeout, cancellation);
        final byte[] header = ProxyIo
                .readExact(connection.source(), 4, timeout, "SOCKS connect response timed out", cancellation);
        if (header[0] != VERSION || header[2] != 0x00) {
            throw new ProtocolException("Invalid SOCKS response");
        }
        if (header[1] != 0x00) {
            throw new ProtocolException("SOCKS CONNECT failed with reply " + (header[1] & 0xff));
        }
        final int addressLength = switch (header[3]) {
            case 0x01 -> 4;
            case 0x03 -> ProxyIo
                    .readExact(connection.source(), 1, timeout, "SOCKS domain length timed out", cancellation)[0]
                    & 0xff;
            case 0x04 -> 16;
            default -> throw new ProtocolException("Unsupported SOCKS address type");
        };
        ProxyIo.readExact(
                connection.source(),
                addressLength + 2,
                timeout,
                "SOCKS bind address timed out",
                cancellation);
    }

    /**
     * Encodes an unresolved-domain or IPv4 CONNECT request.
     */
    private static byte[] request(final Address target) {
        final byte[] ipv4 = ipv4(target.host());
        final byte[] host = ipv4 == null ? ByteString.encodeString(target.host(), Charset.UTF_8).toByteArray() : ipv4;
        if (host.length > 255) {
            throw new ProtocolException("SOCKS target host is too long");
        }
        final ByteBuffer request = ByteBuffer.allocate((ipv4 == null ? 7 : 6) + host.length);
        request.put(VERSION).put((byte) Normal._1).put((byte) Normal._0);
        if (ipv4 == null) {
            request.put((byte) Normal._3).put((byte) host.length);
        } else {
            request.put((byte) Normal._1);
        }
        return request.put(host).putShort((short) target.port()).array();
    }

    /**
     * Parses an IPv4 literal without DNS.
     */
    private static byte[] ipv4(final String host) {
        try {
            return ByteBuffer.allocate(Integer.BYTES).putInt((int) NetKit.ipv4ToLong(host)).array();
        } catch (final RuntimeException ignored) {
            return null;
        }
    }

}
