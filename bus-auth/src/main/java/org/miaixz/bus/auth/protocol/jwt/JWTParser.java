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
package org.miaixz.bus.auth.protocol.jwt;

import java.util.Map;

import org.miaixz.bus.auth.Claims;
import org.miaixz.bus.auth.codec.json.StrictJsonReader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.extra.json.JsonProvider;

/**
 * Parses one bounded compact JWT without selecting an algorithm, key, or policy.
 *
 * @author Kimi Liu
 */
public final class JWTParser {

    /**
     * Prevents construction of the stateless parser.
     */
    private JWTParser() {
        // No initialization required.
    }

    /**
     * Parses the three exact compact segments and strictly maps both JSON documents.
     *
     * @param token              untrusted compact JWT
     * @param provider           product JSON provider
     * @param maximumTokenBytes  maximum compact token UTF-8 bytes
     * @param maximumHeaderBytes maximum decoded header bytes
     * @param maximumJsonBytes   maximum decoded payload bytes
     * @param maximumJsonDepth   maximum JSON nesting depth
     * @return immutable parsed token
     */
    public static Parsed parse(
            final String token,
            final JsonProvider provider,
            final int maximumTokenBytes,
            final int maximumHeaderBytes,
            final int maximumJsonBytes,
            final int maximumJsonDepth) {
        if (token == null || token.isEmpty()
                || token.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > maximumTokenBytes) {
            reject();
        }
        final int first = token.indexOf(Symbol.C_DOT);
        final int second = first < 0 ? -1 : token.indexOf(Symbol.C_DOT, first + 1);
        if (first <= 0 || second <= first + 1 || second >= token.length()
                || token.indexOf(Symbol.C_DOT, second + 1) >= 0) {
            reject();
        }
        final String headerSegment = token.substring(0, first);
        final String payloadSegment = token.substring(first + 1, second);
        final String signatureSegment = token.substring(second + 1);
        final byte[] headerBytes = decode(headerSegment);
        final byte[] payloadBytes = decode(payloadSegment);
        decodeSignature(signatureSegment);
        if (headerBytes.length == 0 || headerBytes.length > maximumHeaderBytes || payloadBytes.length == 0
                || payloadBytes.length > maximumJsonBytes) {
            reject();
        }
        final StrictJsonReader reader = new StrictJsonReader(provider, maximumJsonBytes, maximumJsonDepth);
        return new Parsed(headerSegment, payloadSegment, signatureSegment,
                JWTHeader.from(object(reader.read(headerBytes, Map.class))),
                new JWTPayload(Claims.from(object(reader.read(payloadBytes, Map.class)))));
    }

    /**
     * Strictly decodes one canonical non-empty unpadded Base64url segment.
     */
    private static byte[] decode(final String segment) {
        if (segment == null || segment.isEmpty() || segment.length() % 4 == 1) {
            reject();
        }
        for (int index = 0; index < segment.length(); index++) {
            final char value = segment.charAt(index);
            if (!(value >= 'A' && value <= 'Z') && !(value >= 'a' && value <= 'z') && !(value >= '0' && value <= '9')
                    && value != Symbol.C_MINUS && value != Symbol.C_UNDERLINE) {
                reject();
            }
        }
        try {
            final byte[] decoded = Base64.decode(segment);
            if (!segment.equals(Base64.encodeUrlSafe(decoded))) {
                reject();
            }
            return decoded;
        } catch (final ProtocolException failure) {
            throw failure;
        } catch (final RuntimeException failure) {
            throw new ProtocolException(ErrorCode._100533.getKey(), ErrorCode._100533.getValue(), failure);
        }
    }

    /**
     * Strictly decodes a signature segment while allowing the empty unsecured-JWS representation.
     */
    private static byte[] decodeSignature(final String segment) {
        return segment == null || segment.isEmpty() ? new byte[0] : decode(segment);
    }

    /**
     * Validates a mapped JSON object and returns its string-keyed view.
     */
    private static Map<String, Object> object(final Object value) {
        if (!(value instanceof Map<?, ?> source)) {
            reject();
        }
        final java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
        for (final Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                reject();
            }
            final String key = (String) entry.getKey();
            result.put(key, entry.getValue());
        }
        return result;
    }

    /**
     * Rejects malformed compact input.
     */
    private static void reject() {
        throw new ProtocolException(ErrorCode._100533);
    }

    /**
     * Immutable exact compact segments and parsed protocol models.
     *
     * @param headerSegment    exact protected-header segment
     * @param payloadSegment   exact payload segment
     * @param signatureSegment exact signature segment
     * @param header           immutable protected header
     * @param payload          immutable payload
     * @author Kimi Liu
     */
    public record Parsed(String headerSegment, String payloadSegment, String signatureSegment, JWTHeader header,
            JWTPayload payload) {

        /**
         * @return exact ASCII JWS signing input
         */
        public byte[] signingInput() {
            return (headerSegment + Symbol.DOT + payloadSegment).getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        }

        /**
         * @return decoded signature bytes, or an empty array for unsecured JWS
         */
        public byte[] signature() {
            return decodeSignature(signatureSegment);
        }
    }

}
