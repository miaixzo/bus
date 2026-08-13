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
import java.util.Set;

import org.miaixz.bus.auth.Claims;
import org.miaixz.bus.auth.protocol.jwt.JWT.TrustedAlgorithm;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable JWT protected-header model backed by the root claim snapshot.
 *
 * @param claims immutable protected-header values
 * @author Kimi Liu
 */
public record JWTHeader(Claims claims) {

    /**
     * Algorithm header name.
     */
    public static final String ALGORITHM = "alg";

    /**
     * Type header name.
     */
    public static final String TYPE = "typ";

    /**
     * Key identifier header name.
     */
    public static final String KEY_ID = "kid";

    /**
     * Critical-extension header name.
     */
    public static final String CRITICAL = "crit";

    /**
     * Registered headers that cannot be declared as critical extensions.
     */
    private static final Set<String> REGISTERED = Set.of(ALGORITHM, TYPE, KEY_ID, CRITICAL);

    /**
     * Validates the required immutable claim snapshot.
     */
    public JWTHeader {
        if (claims == null) {
            throw new ValidateException("JWT header claims must not be null");
        }
    }

    /**
     * @return immutable empty header
     */
    public static JWTHeader empty() {
        return new JWTHeader(Claims.empty());
    }

    /**
     * @param values raw header values; @return immutable header
     */
    public static JWTHeader from(final Map<String, ?> values) {
        return new JWTHeader(Claims.from(values));
    }

    /**
     * @return exact required algorithm identifier
     */
    public String algorithm() {
        return claims.require(ALGORITHM, String.class);
    }

    /**
     * @return optional type identifier or {@code null}
     */
    public String type() {
        return claims.find(TYPE, String.class).orElse(null);
    }

    /**
     * @return optional key identifier or {@code null}
     */
    public String keyId() {
        return claims.find(KEY_ID, String.class).orElse(null);
    }

    /**
     * Validates the product-selected algorithm and supported protected-header subset.
     *
     * @param trusted product-selected algorithm
     * @return this immutable header
     */
    public JWTHeader validate(final TrustedAlgorithm trusted) {
        if (trusted == null || !trusted.identifier().equals(algorithm())) {
            throw new ProtocolException(ErrorCode._100533);
        }
        if (type() != null && !"JWT".equalsIgnoreCase(type())) {
            throw new ProtocolException(ErrorCode._100533);
        }
        if (keyId() != null && keyId().isBlank()) {
            throw new ProtocolException(ErrorCode._100533);
        }
        if (claims.snapshot().containsKey(CRITICAL)) {
            throw new ProtocolException(ErrorCode._100533);
        }
        if (!REGISTERED.containsAll(claims.snapshot().keySet())) {
            throw new ProtocolException(ErrorCode._100533);
        }
        return this;
    }

    /**
     * @return immutable protected-header map
     */
    public Map<String, Object> snapshot() {
        return claims.snapshot();
    }

}
