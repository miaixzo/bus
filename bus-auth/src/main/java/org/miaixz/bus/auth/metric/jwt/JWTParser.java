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
package org.miaixz.bus.auth.metric.jwt;

import java.util.Map;

import org.miaixz.bus.auth.metric.AuthMetric.Limits;
import org.miaixz.bus.auth.metric.JWT;
import org.miaixz.bus.auth.metric.shared.json.StrictJsonReader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.extra.json.JsonProvider;

/**
 * Parses bounded compact JWT input into strict protected-header and immutable payload representations.
 * <p>
 * The hardened path validates compact structure and canonical unpadded Base64url before invoking
 * {@link StrictJsonReader} exactly once per JSON segment. The legacy {@link #parseToken(String)} entry point remains
 * unchanged for API compatibility and is not used by hardened verification.
 * </p>
 *
 * @author Kimi Liu
 */
public class JWTParser {

    /**
     * Constructs a stateless parser compatibility instance.
     */
    public JWTParser() {
        // No initialization required.
    }

    /**
     * Preserves the existing compatibility parser.
     *
     * @param token compact JWT
     * @return compatibility JWT object
     */
    public static JWT parseToken(final String token) {
        return JWT.of(token);
    }

    /**
     * Parses one bounded compact JWT through the strict product JSON provider.
     *
     * @param token    compact JWT
     * @param provider product-supplied JSON provider
     * @param limits   authentication input limits
     * @return immutable parsed representation
     */
    public static Parsed parse(final String token, final JsonProvider provider, final Limits limits) {
        if (token == null || token.isEmpty() || limits == null || token.length() > limits.maxJwtBytes()) {
            reject();
        }
        final int first = token.indexOf(Symbol.C_DOT);
        final int second = first < 0 ? -1 : token.indexOf(Symbol.C_DOT, first + 1);
        if (first <= 0 || second <= first + 1 || second >= token.length() - 1
                || token.indexOf(Symbol.C_DOT, second + 1) >= 0) {
            reject();
        }
        final String headerSegment = token.substring(0, first);
        final String payloadSegment = token.substring(first + 1, second);
        final String signatureSegment = token.substring(second + 1);
        final byte[] headerBytes = decode(headerSegment);
        final byte[] payloadBytes = decode(payloadSegment);
        decode(signatureSegment);
        if (headerBytes.length == 0 || headerBytes.length > limits.maxHeaderBytes() || payloadBytes.length == 0) {
            reject();
        }
        final StrictJsonReader reader = new StrictJsonReader(provider, limits);
        final Map<String, Object> headerClaims = object(reader.read(headerBytes, Map.class));
        final Map<String, Object> payloadClaims = object(reader.read(payloadBytes, Map.class));
        final JWTHeader header = new JWTHeader().addHeaders(headerClaims);
        return new Parsed(headerSegment, payloadSegment, signatureSegment, header.snapshot(),
                JWTPayload.immutable(payloadClaims));
    }

    /**
     * Strictly decodes one canonical non-empty unpadded Base64url segment.
     *
     * @param segment compact segment
     * @return decoded bytes
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
     * Validates one provider result as a JSON object with string member names.
     *
     * @param value mapped provider result
     * @return typed object map
     */
    private static Map<String, Object> object(final Object value) {
        if (!(value instanceof Map<?, ?>)) {
            reject();
        }
        final Map<?, ?> source = (Map<?, ?>) value;
        for (final Object key : source.keySet()) {
            if (!(key instanceof String)) {
                reject();
            }
        }
        @SuppressWarnings("unchecked")
        final Map<String, Object> result = (Map<String, Object>) source;
        return result;
    }

    /**
     * Rejects malformed compact input with the shared token-format error.
     */
    private static void reject() {
        throw new ProtocolException(ErrorCode._100533);
    }

    /**
     * Immutable strict parse result retaining the exact compact segments used for signature verification.
     */
    public static final class Parsed {

        /**
         * Exact encoded protected-header segment.
         */
        private final String headerSegment;

        /**
         * Exact encoded payload segment.
         */
        private final String payloadSegment;

        /**
         * Exact encoded signature segment.
         */
        private final String signatureSegment;

        /**
         * Recursively immutable protected-header claims.
         */
        private final Map<String, Object> headerClaims;

        /**
         * Permanently immutable payload.
         */
        private final JWTPayload payload;

        /**
         * Creates one immutable strict parse result.
         *
         * @param headerSegment    exact encoded protected-header segment
         * @param payloadSegment   exact encoded payload segment
         * @param signatureSegment exact encoded signature segment
         * @param headerClaims     immutable protected-header claims
         * @param payload          immutable payload
         */
        private Parsed(final String headerSegment, final String payloadSegment, final String signatureSegment,
                final Map<String, Object> headerClaims, final JWTPayload payload) {
            this.headerSegment = headerSegment;
            this.payloadSegment = payloadSegment;
            this.signatureSegment = signatureSegment;
            this.headerClaims = headerClaims;
            this.payload = payload;
        }

        /**
         * Returns an independent header view for policy validation.
         *
         * @return copied protected header
         */
        public JWTHeader header() {
            return new JWTHeader().addHeaders(headerClaims);
        }

        /**
         * Returns the permanently immutable payload.
         *
         * @return immutable payload
         */
        public JWTPayload payload() {
            return payload;
        }

        /**
         * Returns the exact encoded protected-header segment.
         *
         * @return protected-header segment
         */
        public String headerSegment() {
            return headerSegment;
        }

        /**
         * Returns the exact encoded payload segment.
         *
         * @return payload segment
         */
        public String payloadSegment() {
            return payloadSegment;
        }

        /**
         * Returns the exact encoded signature segment.
         *
         * @return signature segment
         */
        public String signatureSegment() {
            return signatureSegment;
        }

    }

}
