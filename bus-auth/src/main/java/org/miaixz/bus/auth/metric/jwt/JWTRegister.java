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

import java.util.Date;

/**
 * Maps the registered JWT claim names defined by RFC 7519 to the existing payload mutation contract.
 * <p>
 * This interface contains protocol registration values only. Cryptographic algorithms, implementation errors, and
 * process-wide registries belong to their owning Bus components and must not be declared here.
 * </p>
 *
 * @param <T> the type of the class implementing this interface
 * @author Kimi Liu
 */
public interface JWTRegister<T extends JWTRegister<T>> {

    /**
     * Registered name of the issuer claim.
     */
    String ISSUER = "iss";

    /**
     * Registered name of the subject claim.
     */
    String SUBJECT = "sub";

    /**
     * Registered name of the audience claim.
     */
    String AUDIENCE = "aud";

    /**
     * Registered name of the expiration-time claim.
     */
    String EXPIRES_AT = "exp";

    /**
     * Registered name of the not-before claim.
     */
    String NOT_BEFORE = "nbf";

    /**
     * Registered name of the issued-at claim.
     */
    String ISSUED_AT = "iat";

    /**
     * Registered name of the JWT identifier claim.
     */
    String JWT_ID = "jti";

    /**
     * Sets the "iss" (issuer) claim value in the Payload.
     *
     * @param issuer the issuer of the JWT
     * @return this instance for method chaining
     */
    default T setIssuer(final String issuer) {
        return setPayload(ISSUER, issuer);
    }

    /**
     * Sets the "sub" (subject) claim value in the Payload.
     *
     * @param subject the subject of the JWT
     * @return this instance for method chaining
     */
    default T setSubject(final String subject) {
        return setPayload(SUBJECT, subject);
    }

    /**
     * Sets the "aud" (audience) claim value in the Payload.
     *
     * @param audience the recipients that the JWT is intended for
     * @return this instance for method chaining
     */
    default T setAudience(final String... audience) {
        return setPayload(AUDIENCE, audience);
    }

    /**
     * Sets the "exp" (expiration time) claim value in the Payload. This expiration time must be after the issued at
     * time.
     *
     * @param expiresAt the expiration time of the JWT
     * @return this instance for method chaining
     * @see #setIssuedAt(Date)
     */
    default T setExpiresAt(final Date expiresAt) {
        return setPayload(EXPIRES_AT, expiresAt);
    }

    /**
     * Sets the "nbf" (not before) claim value in the Payload.
     *
     * @param notBefore the time before which the JWT MUST NOT be accepted for processing
     * @return this instance for method chaining
     */
    default T setNotBefore(final Date notBefore) {
        return setPayload(NOT_BEFORE, notBefore);
    }

    /**
     * Sets the "iat" (issued at time) claim value in the Payload.
     *
     * @param issuedAt the time at which the JWT was issued
     * @return this instance for method chaining
     */
    default T setIssuedAt(final Date issuedAt) {
        return setPayload(ISSUED_AT, issuedAt);
    }

    /**
     * Sets the "jti" (JWT ID) claim value in the Payload.
     *
     * @param jwtId the unique identifier for the JWT
     * @return this instance for method chaining
     */
    default T setJWTId(final String jwtId) {
        return setPayload(JWT_ID, jwtId);
    }

    /**
     * Sets a custom Payload value.
     *
     * @param name  the name of the payload claim
     * @param value the value of the payload claim
     * @return this instance for method chaining
     */
    T setPayload(String name, Object value);

}
