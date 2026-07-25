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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.proxy.ProxyPlan;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * HTTP CONNECT tunnel handshake with bounded response metadata.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class HttpProxyTunnel {

    /**
     * Opens a tunnel through an HTTP proxy.
     *
     * @param connection   proxy connection
     * @param target       target address
     * @param proxy        proxy plan
     * @param timeout      timeout policy
     * @param cancellation cancellation scope
     */
    void connect(
            final Connection connection,
            final Address target,
            final ProxyPlan proxy,
            final Timeout timeout,
            final Cancellation cancellation) {
        cancellation.throwIfCancelled();
        final Buffer request = new Buffer()
                .write(ByteString.encodeString(request(target, proxy.authorization()), Charset.US_ASCII).toByteArray());
        ProxyIo.writeAll(connection.sink(), request, timeout, cancellation);
        final String response = readHeader(connection.source(), timeout, cancellation);
        if (response
                .startsWith(Protocol.HTTP_1_1 + Symbol.SPACE + Http.Status.PROXY_AUTHENTICATION_REQUIRED + Symbol.SPACE)
                || response.startsWith(
                        Protocol.HTTP_1_0 + Symbol.SPACE + Http.Status.PROXY_AUTHENTICATION_REQUIRED + Symbol.SPACE)) {
            throw new ProtocolException(
                    proxy.authorization().size() == 0 ? "HTTP CONNECT proxy authentication is required"
                            : "HTTP CONNECT proxy rejected the configured authentication");
        }
        if (!response.startsWith(Protocol.HTTP_1_1 + Symbol.SPACE + Http.Status.OK + Symbol.SPACE)
                && !response.startsWith(Protocol.HTTP_1_0 + Symbol.SPACE + Http.Status.OK + Symbol.SPACE)
                && !response.startsWith("HTTP/2" + Symbol.SPACE + Http.Status.OK + Symbol.SPACE)) {
            throw new ProtocolException("HTTP CONNECT tunnel failed");
        }
    }

    /**
     * Builds the bounded CONNECT request.
     */
    private static String request(final Address target, final Headers authorization) {
        final String authority = authority(target);
        final StringBuilder value = new StringBuilder();
        value.append(Http.Method.CONNECT.value()).append(Symbol.C_SPACE).append(authority).append(Symbol.C_SPACE)
                .append(Protocol.HTTP_1_1).append(Symbol.CRLF);
        value.append(Http.Header.HOST).append(Symbol.COLON).append(Symbol.SPACE).append(authority).append(Symbol.CRLF);
        value.append(Http.Header.PROXY_CONNECTION).append(Symbol.COLON).append(Symbol.SPACE)
                .append(Http.Header.CONNECTION_KEEP_ALIVE).append(Symbol.CRLF);
        for (final Map.Entry<String, List<String>> entry : authorization.asMap().entrySet()) {
            for (final String header : entry.getValue()) {
                value.append(entry.getKey()).append(Symbol.COLON).append(Symbol.SPACE).append(header)
                        .append(Symbol.CRLF);
            }
        }
        return value.append(Symbol.CRLF).toString();
    }

    /**
     * Reads response headers up to the configured hard limit.
     */
    private static String readHeader(final Source source, final Timeout timeout, final Cancellation cancellation) {
        ProxyIo.configure(source.timeout(), timeout.read());
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
     * Formats an authority without resolving its host.
     */
    private static String authority(final Address address) {
        final String host = address.host().indexOf(Symbol.C_COLON) >= 0
                ? Symbol.BRACKET_LEFT + address.host() + Symbol.BRACKET_RIGHT
                : address.host();
        return host + Symbol.C_COLON + address.port();
    }

}
