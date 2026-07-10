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
package org.miaixz.bus.fabric.protocol.socket;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.proxy.ProxyHeader;

/**
 * PROXY protocol v1 first-packet parser for current socket servers.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketProxyProtocol {

    /**
     * Session attribute key used for parsed PROXY protocol metadata.
     */
    public static final String ATTRIBUTE_PROXY_HEADER = SocketSession.ATTRIBUTE_PROXY_HEADER;

    /**
     * ASCII prefix that identifies a PROXY protocol v1 header in the first packet.
     */
    private static final byte[] PREFIX = "PROXY ".getBytes(StandardCharsets.US_ASCII);

    /**
     * Hidden constructor for first-packet parsing helpers.
     */
    private SocketProxyProtocol() {
        // No initialization required.
    }

    /**
     * Parses a first packet and returns the parsed header plus any remaining payload bytes.
     *
     * @param packet first packet
     * @return parse result
     */
    public static Result parse(final ByteBuffer packet) {
        if (packet == null) {
            throw new ValidateException("PROXY packet must not be null");
        }
        final ByteBuffer view = packet.asReadOnlyBuffer();
        if (!startsWithProxy(view)) {
            return new Result(null, view.slice().asReadOnlyBuffer());
        }
        int lineEnd = -1;
        for (int i = view.position(); i < view.limit() - 1; i++) {
            if (view.get(i) == '\r' && view.get(i + 1) == '\n') {
                lineEnd = i;
                break;
            }
        }
        if (lineEnd < 0) {
            throw new ProtocolException("Incomplete PROXY protocol header");
        }
        final byte[] lineBytes = new byte[lineEnd - view.position()];
        view.get(lineBytes);
        view.position(view.position() + 2);
        return new Result(ProxyHeader.parse(new String(lineBytes, StandardCharsets.US_ASCII)),
                view.slice().asReadOnlyBuffer());
    }

    /**
     * Parses a first packet and injects parsed PROXY metadata into session attributes.
     *
     * @param packet     first packet
     * @param attributes session attributes
     * @return parse result
     */
    public static Result parseAndInject(final ByteBuffer packet, final Map<String, Object> attributes) {
        if (attributes == null) {
            throw new ValidateException("Session attributes must not be null");
        }
        final Result result = parse(packet);
        if (result.header() != null) {
            attributes.put(ATTRIBUTE_PROXY_HEADER, result.header());
        }
        return result;
    }

    /**
     * Returns a copy of session attributes with parsed PROXY metadata included when present.
     *
     * @param result parse result
     * @param base   base attributes
     * @return immutable attributes
     */
    public static Map<String, Object> attributes(final Result result, final Map<String, Object> base) {
        if (result == null) {
            throw new ValidateException("PROXY parse result must not be null");
        }
        final LinkedHashMap<String, Object> attributes = new LinkedHashMap<>(base == null ? Map.of() : base);
        if (result.header() != null) {
            attributes.put(ATTRIBUTE_PROXY_HEADER, result.header());
        }
        return Map.copyOf(attributes);
    }

    /**
     * Checks the first bytes without changing the buffer position.
     *
     * @param packet packet view
     * @return {@code true} when the packet begins with a PROXY v1 header
     */
    private static boolean startsWithProxy(final ByteBuffer packet) {
        if (packet.remaining() < PREFIX.length) {
            return false;
        }
        for (int i = 0; i < PREFIX.length; i++) {
            if (packet.get(packet.position() + i) != PREFIX[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parsed first packet.
     *
     * @param header  parsed proxy header, or null when absent
     * @param payload remaining payload
     */
    public record Result(ProxyHeader header, ByteBuffer payload) {

        /**
         * Creates an immutable parse result and protects the remaining payload from mutation.
         */
        public Result {
            if (payload == null) {
                throw new ValidateException("PROXY payload must not be null");
            }
            payload = payload.asReadOnlyBuffer();
        }

    }

}
