/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.auth.metric.jwt;

import java.util.Date;

/**
 * Interface for registering standard JWT (JSON Web Token) claims (Payload). This interface defines common claims used
 * in JWT payloads and provides default methods for setting them.
 *
 * @param <T> the type of the class implementing this interface
 * @author Kimi Liu
 * @since Java 17+
 */
public interface JWTRegister<T extends JWTRegister<T>> {

    /**
     * The "iss" (issuer) claim identifies the principal that issued the JWT.
     */
    String ISSUER = "iss";
    /**
     * The "sub" (subject) claim identifies the principal that is the subject of the JWT.
     */
    String SUBJECT = "sub";
    /**
     * The "aud" (audience) claim identifies the recipients that the JWT is intended for.
     */
    String AUDIENCE = "aud";
    /**
     * The "exp" (expiration time) claim identifies the expiration time on or after which the JWT MUST NOT be accepted
     * for processing. This expiration time must be after the issued at time.
     */
    String EXPIRES_AT = "exp";
    /**
     * The "nbf" (not before) claim identifies the time before which the JWT MUST NOT be accepted for processing.
     */
    String NOT_BEFORE = "nbf";
    /**
     * The "iat" (issued at time) claim identifies the time at which the JWT was issued.
     */
    String ISSUED_AT = "iat";
    /**
     * The "jti" (JWT ID) claim provides a unique identifier for the JWT. Primarily used as a one-time token to prevent
     * replay attacks.
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
