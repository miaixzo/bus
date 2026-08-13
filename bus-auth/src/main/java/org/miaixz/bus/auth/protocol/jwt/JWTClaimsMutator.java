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

/**
 * Fluent mutation contract for builders that produce immutable JWT claim snapshots.
 *
 * @param <T> concrete fluent builder type
 * @author Kimi Liu
 */
public interface JWTClaimsMutator<T extends JWTClaimsMutator<T>> {

    /**
     * @param issuer issuer; @return this builder
     */
    default T setIssuer(final String issuer) {
        return setPayload(RegisteredClaims.ISSUER, issuer);
    }

    /**
     * @param subject subject; @return this builder
     */
    default T setSubject(final String subject) {
        return setPayload(RegisteredClaims.SUBJECT, subject);
    }

    /**
     * @param audience intended audiences; @return this builder
     */
    default T setAudience(final String... audience) {
        return setPayload(RegisteredClaims.AUDIENCE, audience == null ? null : java.util.List.of(audience));
    }

    /**
     * @param expiresAt expiration instant; @return this builder
     */
    default T setExpiresAt(final Instant expiresAt) {
        return setPayload(RegisteredClaims.EXPIRES_AT, expiresAt == null ? null : expiresAt.getEpochSecond());
    }

    /**
     * @param notBefore not-before instant; @return this builder
     */
    default T setNotBefore(final Instant notBefore) {
        return setPayload(RegisteredClaims.NOT_BEFORE, notBefore == null ? null : notBefore.getEpochSecond());
    }

    /**
     * @param issuedAt issued-at instant; @return this builder
     */
    default T setIssuedAt(final Instant issuedAt) {
        return setPayload(RegisteredClaims.ISSUED_AT, issuedAt == null ? null : issuedAt.getEpochSecond());
    }

    /**
     * @param jwtId unique token identifier; @return this builder
     */
    default T setJwtId(final String jwtId) {
        return setPayload(RegisteredClaims.JWT_ID, jwtId);
    }

    /**
     * Adds or replaces one supported claim value.
     *
     * @param name  claim name
     * @param value root Claims-supported value
     * @return this builder
     */
    T setPayload(String name, Object value);

}
