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
import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
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
    private static final ByteString PREFIX = ByteString.encodeString("PROXY" + Symbol.SPACE, Charset.US_ASCII);

    /**
     * ASCII line terminator used by PROXY protocol v1.
     */
    private static final ByteString LINE_END = ByteString.encodeString(Symbol.CR + Symbol.LF, Charset.US_ASCII);

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
    public static Result parse(final ByteString packet) {
        final ByteString current = Assert.notNull(packet, () -> new ValidateException("PROXY packet must not be null"));
        if (!current.startsWith(PREFIX)) {
            return new Result(null, current);
        }
        final int lineEnd = current.indexOf(LINE_END);
        if (lineEnd < Normal._0) {
            throw new ProtocolException("Incomplete PROXY protocol header");
        }
        final ByteString line = current.substring(Normal._0, lineEnd);
        return new Result(ProxyHeader.parse(line.string(Charset.US_ASCII)),
                current.substring(lineEnd + LINE_END.size()));
    }

    /**
     * Parses a first packet through the JDK ByteBuffer compatibility boundary.
     *
     * @param packet first packet
     * @return parse result
     * @deprecated use {@link #parse(ByteString)}
     */
    @Deprecated(since = "8.8.3")
    public static Result parse(final ByteBuffer packet) {
        return parse(snapshot(packet));
    }

    /**
     * Parses a first packet and injects parsed PROXY metadata into session attributes.
     *
     * @param packet     first packet
     * @param attributes session attributes
     * @return parse result
     */
    public static Result parseAndInject(final ByteString packet, final Map<String, Object> attributes) {
        final Map<String, Object> checkedAttributes = Assert
                .notNull(attributes, () -> new ValidateException("Session attributes must not be null"));
        final Result result = parse(packet);
        if (result.header() != null) {
            checkedAttributes.put(ATTRIBUTE_PROXY_HEADER, result.header());
        }
        return result;
    }

    /**
     * Parses a first packet through the JDK ByteBuffer compatibility boundary and injects parsed PROXY metadata.
     *
     * @param packet     first packet
     * @param attributes session attributes
     * @return parse result
     * @deprecated use {@link #parseAndInject(ByteString, Map)}
     */
    @Deprecated(since = "8.8.3")
    public static Result parseAndInject(final ByteBuffer packet, final Map<String, Object> attributes) {
        return parseAndInject(snapshot(packet), attributes);
    }

    /**
     * Returns a copy of session attributes with parsed PROXY metadata included when present.
     *
     * @param result parse result
     * @param base   base attributes
     * @return immutable attributes
     */
    public static Map<String, Object> attributes(final Result result, final Map<String, Object> base) {
        final Result checkedResult = Assert
                .notNull(result, () -> new ValidateException("PROXY parse result must not be null"));
        final LinkedHashMap<String, Object> attributes = new LinkedHashMap<>(base == null ? Map.of() : base);
        if (checkedResult.header() != null) {
            attributes.put(ATTRIBUTE_PROXY_HEADER, checkedResult.header());
        }
        return Map.copyOf(attributes);
    }

    /**
     * Creates an immutable byte snapshot from a JDK buffer boundary.
     *
     * @param packet packet view
     * @return byte snapshot
     */
    private static ByteString snapshot(final ByteBuffer packet) {
        final ByteBuffer view = Assert.notNull(packet, () -> new ValidateException("PROXY packet must not be null"))
                .asReadOnlyBuffer();
        final byte[] bytes = new byte[view.remaining()];
        view.get(bytes);
        return ByteString.of(bytes);
    }

    /**
     * Parsed first packet.
     *
     * @param header       parsed proxy header, or null when absent
     * @param payloadBytes remaining payload bytes
     */
    public record Result(ProxyHeader header, ByteString payloadBytes) {

        /**
         * Creates an immutable parse result and protects the remaining payload from mutation.
         */
        public Result {
            payloadBytes = ByteString.of(
                    Assert.notNull(payloadBytes, () -> new ValidateException("PROXY payload must not be null"))
                            .toByteArray());
        }

        /**
         * Returns the remaining payload through a JDK ByteBuffer compatibility view.
         *
         * @return remaining payload
         * @deprecated use {@link #payloadBytes()}
         */
        @Deprecated(since = "8.8.3")
        public ByteBuffer payload() {
            return payloadBytes.asByteBuffer();
        }

    }

}
