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
import java.util.Map;

import org.miaixz.bus.auth.metric.JWT;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.auth.metric.jwt.signature.NoneJWTSigner;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * JWT data validator, used to verify the algorithm, signature, and time-based claims of a JWT.
 * <ul>
 * <li>Checks if the algorithm matches.</li>
 * <li>Checks if the signature is correct.</li>
 * <li>Checks if time-based claims (e.g., not expired, effective) are valid.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JWTVerifier {

    /**
     * The JWT object to be verified.
     */
    private final JWT jwt;

    /**
     * Constructor, initializes the verifier.
     *
     * @param jwt the JWT object to be verified
     */
    public JWTVerifier(final JWT jwt) {
        this.jwt = jwt;
    }

    /**
     * Creates a JWT verifier, initialized from a token string.
     *
     * @param token the JWT token string
     * @return a new {@link JWTVerifier} instance
     */
    public static JWTVerifier of(final String token) {
        return new JWTVerifier(JWT.of(token));
    }

    /**
     * Creates a JWT verifier, initialized from an existing JWT object.
     *
     * @param jwt the JWT object
     * @return a new {@link JWTVerifier} instance
     */
    public static JWTVerifier of(final JWT jwt) {
        return new JWTVerifier(jwt);
    }

    /**
     * Verifies the validity of a JWT Token using an HS256 (HmacSHA256) key.
     *
     * @param token the JWT Token string
     * @param key   the HS256 (HmacSHA256) secret key
     * @return true if the token is valid, false otherwise
     */
    public static boolean verify(final String token, final byte[] key) {
        return JWT.of(token).setKey(key).verify();
    }

    /**
     * Verifies the validity of a JWT Token using a specified signer.
     *
     * @param token  the JWT Token string
     * @param signer the {@link JWTSigner} to use for verification
     * @return true if the token is valid, false otherwise
     */
    public static boolean verify(final String token, final JWTSigner signer) {
        return JWT.of(token).verify(signer);
    }

    /**
     * Validates the JWT's algorithm and signature.
     *
     * @param jwt    the JWT object
     * @param signer the signer used for verification; if null, the JWT's own signer is used
     * @throws ValidateException if the algorithm does not match or the signature is invalid
     */
    private static void validateAlgorithm(final JWT jwt, JWTSigner signer) throws ValidateException {
        final String algorithmId = jwt.getAlgorithm();
        if (null == signer) {
            signer = jwt.getSigner();
        }
        if (StringKit.isEmpty(algorithmId)) {
            if (null == signer || signer instanceof NoneJWTSigner) {
                return;
            }
            throw new ValidateException("No algorithm defined in header!");
        }
        if (null == signer) {
            throw new IllegalArgumentException("No Signer for validate algorithm!");
        }
        final String algorithmIdInSigner = signer.getAlgorithmId();
        if (!StringKit.equals(algorithmId, algorithmIdInSigner)) {
            throw new ValidateException("Algorithm [{}] defined in header doesn't match to [{}]!", algorithmId,
                    algorithmIdInSigner);
        }
        if (!jwt.verify(signer)) {
            throw new ValidateException("Signature verification failed!");
        }
    }

    /**
     * Validates the time-based claims of the JWT.
     * <p>
     * Checks the following fields:
     * <ul>
     * <li>notBefore (nbf): The effective time must not be later than the current time.</li>
     * <li>expiresAt (exp): The expiration time must not be earlier than the current time.</li>
     * <li>issuedAt (iat): The issuance time must not be later than the current time.</li>
     * </ul>
     * Fields that are not set are not checked.
     * </p>
     *
     * @param payload the JWT payload object
     * @param now     the current time; if null, the system's current time is used
     * @param leeway  the tolerance time in seconds, for leniency in time-based checks
     * @throws ValidateException if any time-based claim is invalid
     */
    private static void validateDate(final JWTPayload payload, Date now, final long leeway) throws ValidateException {
        if (null == now) {
            now = DateKit.now();
            now.setTime(now.getTime() / 1000 * 1000);
        }
        final Map<String, Object> claims = payload.getClaimsJson();
        final Long notBefore = claims.get(JWTPayload.NOT_BEFORE) instanceof Long
                ? (Long) claims.get(JWTPayload.NOT_BEFORE)
                : null;
        final Long expiresAt = claims.get(JWTPayload.EXPIRES_AT) instanceof Long
                ? (Long) claims.get(JWTPayload.EXPIRES_AT)
                : null;
        final Long issueAt = claims.get(JWTPayload.ISSUED_AT) instanceof Long ? (Long) claims.get(JWTPayload.ISSUED_AT)
                : null;

        validateNotAfter(JWTPayload.NOT_BEFORE, notBefore, now, leeway);
        validateNotBefore(JWTPayload.EXPIRES_AT, expiresAt, now, leeway);
        validateNotAfter(JWTPayload.ISSUED_AT, issueAt, now, leeway);
    }

    /**
     * Validates that the specified time field is not after the current time.
     * <p>
     * If the field is not present, the check is skipped.
     * </p>
     *
     * @param fieldName   the name of the field (e.g., nbf, iat)
     * @param dateToCheck the time value to check (seconds timestamp)
     * @param now         the current time
     * @param leeway      the tolerance time in seconds, allowing for a slight delay (checked against `now + leeway`)
     * @throws ValidateException if the time is after the current time (considering leeway)
     */
    private static void validateNotAfter(final String fieldName, final Long dateToCheck, Date now, final long leeway)
            throws ValidateException {
        if (dateToCheck == null) {
            return;
        }
        Date checkDate = new Date(dateToCheck * 1000);
        if (leeway > 0) {
            now = new Date(now.getTime() + leeway * 1000);
        }
        if (checkDate.after(now)) {
            throw new ValidateException("'{}':[{}]] is after now:[{}]", fieldName, DateKit.date(checkDate),
                    DateKit.date(now));
        }
    }

    /**
     * Validates that the specified time field is not before the current time.
     * <p>
     * If the field is not present, the check is skipped.
     * </p>
     *
     * @param fieldName   the name of the field (e.g., exp)
     * @param dateToCheck the time value to check (seconds timestamp)
     * @param now         the current time
     * @param leeway      the tolerance time in seconds, allowing for a slight early check (checked against `now -
     *                    leeway`)
     * @throws ValidateException if the time is before the current time (considering leeway)
     */
    private static void validateNotBefore(final String fieldName, final Long dateToCheck, Date now, final long leeway)
            throws ValidateException {
        if (dateToCheck == null) {
            return;
        }
        Date checkDate = new Date(dateToCheck * 1000);
        if (leeway > 0) {
            now = new Date(now.getTime() - leeway * 1000);
        }
        if (checkDate.before(now)) {
            throw new ValidateException("'{}':[{}]] is before now:[{}]", fieldName, DateKit.date(checkDate),
                    DateKit.date(now));
        }
    }

    /**
     * Validates the JWT's algorithm and signature using the JWT object's own signer.
     *
     * @return the current {@link JWTVerifier} instance
     * @throws ValidateException if the algorithm does not match or the signature is invalid
     */
    public JWTVerifier validateAlgorithm() throws ValidateException {
        return validateAlgorithm(null);
    }

    /**
     * Validates the JWT's algorithm and signature using the specified signer.
     *
     * @param signer the signer used for verification; if null, the JWT's own signer is used
     * @return the current {@link JWTVerifier} instance
     * @throws ValidateException        if the algorithm does not match or the signature is invalid
     * @throws IllegalArgumentException if no signer is provided and the JWT requires a signature
     */
    public JWTVerifier validateAlgorithm(final JWTSigner signer) throws ValidateException {
        validateAlgorithm(this.jwt, signer);
        return this;
    }

    /**
     * Validates the JWT's time-based claims using the current system time.
     * <ul>
     * <li>notBefore (nbf): The effective time must not be later than the current time.</li>
     * <li>expiresAt (exp): The expiration time must not be earlier than the current time.</li>
     * <li>issuedAt (iat): The issuance time must not be later than the current time.</li>
     * </ul>
     * Fields that are not set are not checked.
     *
     * @return the current {@link JWTVerifier} instance
     * @throws ValidateException if any time-based claim is invalid
     */
    public JWTVerifier validateDate() throws ValidateException {
        return validateDate(DateKit.beginOfSecond(DateKit.now()));
    }

    /**
     * Validates the JWT's time-based claims using a specified time.
     *
     * @param dateToCheck the time against which to check, typically the current time
     * @return the current {@link JWTVerifier} instance
     * @throws ValidateException if any time-based claim is invalid
     */
    public JWTVerifier validateDate(final Date dateToCheck) throws ValidateException {
        validateDate(this.jwt.getPayload(), dateToCheck, 0L);
        return this;
    }

    /**
     * Validates the JWT's time-based claims with a specified tolerance time.
     *
     * @param dateToCheck the time against which to check, typically the current time
     * @param leeway      the tolerance time in seconds, for leniency in time-based checks
     * @return the current {@link JWTVerifier} instance
     * @throws ValidateException if any time-based claim is invalid
     */
    public JWTVerifier validateDate(final Date dateToCheck, final long leeway) throws ValidateException {
        validateDate(this.jwt.getPayload(), dateToCheck, leeway);
        return this;
    }

}
