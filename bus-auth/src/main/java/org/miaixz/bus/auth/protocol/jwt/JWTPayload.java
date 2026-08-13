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

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.miaixz.bus.auth.Claims;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable JWT payload wrapper around the protocol-neutral root claim snapshot.
 *
 * @param claims immutable JWT claims
 * @author Kimi Liu
 */
public record JWTPayload(Claims claims) {

    /**
     * Validates the required claim snapshot.
     */
    public JWTPayload {
        if (claims == null) {
            throw new ValidateException("JWT payload claims must not be null");
        }
    }

    /**
     * @param values raw claim values; @return immutable payload
     */
    public static JWTPayload from(final Map<String, ?> values) {
        return new JWTPayload(Claims.from(values));
    }

    /**
     * @return optional issuer
     */
    public Optional<String> issuer() {
        return claims.issuer();
    }

    /**
     * @return optional subject
     */
    public Optional<String> subject() {
        return claims.subject();
    }

    /**
     * @return immutable audience set
     */
    public Set<String> audiences() {
        return claims.audience();
    }

    /**
     * @return optional expiration instant
     */
    public Optional<Instant> expiresAt() {
        return claims.expiresAt();
    }

    /**
     * @return optional issued-at instant
     */
    public Optional<Instant> issuedAt() {
        return claims.issuedAt();
    }

    /**
     * @return optional not-before instant
     */
    public Optional<Instant> notBefore() {
        final Object value = claims.snapshot().get(RegisteredClaims.NOT_BEFORE);
        if (value instanceof Instant instant) {
            return Optional.of(instant);
        }
        if (value instanceof Number number) {
            return Optional.of(Instant.ofEpochSecond(number.longValue()));
        }
        return Optional.empty();
    }

    /**
     * @return optional JWT identifier
     */
    public Optional<String> jwtId() {
        return claims.find(RegisteredClaims.JWT_ID, String.class);
    }

    /**
     * @return immutable claim map
     */
    public Map<String, Object> snapshot() {
        return claims.snapshot();
    }

    /**
     * Returns a representation that does not expose claims.
     */
    @Override
    public String toString() {
        return "JWTPayload[REDACTED]";
    }

}
