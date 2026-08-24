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
package org.miaixz.bus.auth.shared.jwt;

import java.security.Key;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Carries one verified or locally issued compact JWT and provides the simple static JWT entry points.
 * <p>
 * Instance state preserves a compact JWS or JWE with its parsed JOSE Header and Claims Set. Static operations delegate
 * signing, issuance, verification, and validation to {@link JwtService}; this class does not implement cryptographic
 * primitives, JSON conversion, key inference, or protocol-specific claim policy.
 * </p>
 * <p>
 * Diagnostic rendering always redacts the compact bearer value. Construction identifies serialization shape only;
 * {@link JwtIssuer}, {@link JwtVerifier}, and {@link JwtValidator} remain the advanced execution components.
 * </p>
 *
 * @author Kimi Liu
 */
public class JWT {

    /**
     * Shared stateless parser used by the public unverified entry point.
     */
    private static final JwtParser PARSER = new JwtParser();

    /**
     * Sensitive compact JWS or JWE representation.
     */
    private final String compact;
    /**
     * Parsed outer JOSE Header.
     */
    private final JoseHeader header;
    /**
     * Parsed implementation-neutral JWT Claims Set.
     */
    private final JwtClaims claims;
    /**
     * Serialization kind derived from exact compact segment count.
     */
    private final Kind kind;

    /**
     * Creates an immutable JWT value after validating compact serialization shape.
     *
     * @param compact sensitive compact JWS or JWE value
     * @param header  parsed outer JOSE Header
     * @param claims  parsed JWT Claims Set
     * @throws IllegalArgumentException if a required component is {@code null} or compact is blank
     * @throws ValidateException        if compact contains neither three nor five segments
     */
    public JWT(final String compact, final JoseHeader header, final JwtClaims claims) {
        this.compact = Assert.notBlank(compact, "JWT compact value must not be blank");
        this.header = Assert.notNull(header, "JWT JOSE Header must not be null");
        this.claims = Assert.notNull(claims, "JWT claims must not be null");
        final int segments = compact.split("\\.", -1).length;
        this.kind = switch (segments) {
            case 3 -> Kind.SIGNED;
            case 5 -> Kind.ENCRYPTED;
            default -> throw new ValidateException("JWT compact value must contain three or five segments");
        };
    }

    /**
     * Signs an implementation-neutral Claims Set with an explicit HS256 secret.
     * <p>
     * This operation preserves every caller-supplied claim and does not generate issuer, audience, temporal, or JWT ID
     * claims. The secret overload fixes the expected algorithm to HS256 and never accepts an algorithm from untrusted
     * input.
     * </p>
     *
     * @param claims caller-supplied Claims Set
     * @param secret HMAC secret containing at least 256 bits of key material
     * @return compact signed JWT
     */
    public static String sign(final Map<String, ?> claims, final byte[] secret) {
        return JwtService.hs256(secret).sign(JwtClaims.of(claims)).compact();
    }

    /**
     * Signs an implementation-neutral Claims Set with deterministic HS256 String key material.
     * <p>
     * The String is preserved exactly and converted through the versioned framework key-derivation profile. Every
     * non-empty value is accepted, including one-character values.
     * </p>
     *
     * @param claims caller-supplied Claims Set
     * @param secret non-empty String key material
     * @return compact signed JWT
     */
    public static String sign(final Map<String, ?> claims, final String secret) {
        return JwtService.hs256(secret).sign(JwtClaims.of(claims)).compact();
    }

    /**
     * Signs one public record as an implementation-neutral Claims Set with an explicit HS256 secret.
     *
     * @param claims caller-supplied public record
     * @param secret HMAC secret containing at least 256 bits of key material
     * @param <T>    public record type
     * @return compact signed JWT
     */
    public static <T extends Record> String sign(final T claims, final byte[] secret) {
        return JwtService.hs256(secret).sign(JwtClaims.of(claims)).compact();
    }

    /**
     * Signs one public record with deterministic HS256 String key material.
     *
     * @param claims caller-supplied public record
     * @param secret non-empty String key material
     * @param <T>    public record type
     * @return compact signed JWT
     */
    public static <T extends Record> String sign(final T claims, final String secret) {
        return JwtService.hs256(secret).sign(JwtClaims.of(claims)).compact();
    }

    /**
     * Signs an implementation-neutral Claims Set with one explicit JWS algorithm and key.
     *
     * @param claims    caller-supplied Claims Set
     * @param algorithm exact expected JWS algorithm
     * @param key       private or symmetric signing key
     * @return compact signed JWT
     */
    public static String sign(final Map<String, ?> claims, final JwaAlgorithm algorithm, final Key key) {
        return new JwtService(algorithm, key).sign(JwtClaims.of(claims)).compact();
    }

    /**
     * Signs one public record with one explicit JWS algorithm and key.
     *
     * @param claims    caller-supplied public record
     * @param algorithm exact expected JWS algorithm
     * @param key       private or symmetric signing key
     * @param <T>       public record type
     * @return compact signed JWT
     */
    public static <T extends Record> String sign(final T claims, final JwaAlgorithm algorithm, final Key key) {
        return new JwtService(algorithm, key).sign(JwtClaims.of(claims)).compact();
    }

    /**
     * Issues an HS256 JWT after generating issuer, audience, issued-at, expiration, and JWT ID claims.
     *
     * @param claims   subject and application claims that do not replace issuer-generated claims
     * @param secret   HMAC secret containing at least 256 bits of key material
     * @param issuer   exact issuer StringOrURI
     * @param audience exact audience StringOrURI
     * @param lifetime positive token lifetime
     * @return compact issued JWT
     */
    public static String issue(
            final Map<String, ?> claims,
            final byte[] secret,
            final String issuer,
            final String audience,
            final Duration lifetime) {
        return JwtService.hs256(secret).issue(JwtClaims.of(claims), issuer, audience, lifetime).compact();
    }

    /**
     * Issues an HS256 JWT from deterministic String key material after generating registered issuer-owned claims.
     *
     * @param claims   subject and application claims that do not replace issuer-generated claims
     * @param secret   non-empty String key material
     * @param issuer   exact issuer StringOrURI
     * @param audience exact audience StringOrURI
     * @param lifetime positive token lifetime
     * @return compact issued JWT
     */
    public static String issue(
            final Map<String, ?> claims,
            final String secret,
            final String issuer,
            final String audience,
            final Duration lifetime) {
        return JwtService.hs256(secret).issue(JwtClaims.of(claims), issuer, audience, lifetime).compact();
    }

    /**
     * Issues an HS256 JWT from a public record after generating registered issuer-owned claims.
     *
     * @param claims   subject and application claim record
     * @param secret   HMAC secret containing at least 256 bits of key material
     * @param issuer   exact issuer StringOrURI
     * @param audience exact audience StringOrURI
     * @param lifetime positive token lifetime
     * @param <T>      public record type
     * @return compact issued JWT
     */
    public static <T extends Record> String issue(
            final T claims,
            final byte[] secret,
            final String issuer,
            final String audience,
            final Duration lifetime) {
        return JwtService.hs256(secret).issue(JwtClaims.of(claims), issuer, audience, lifetime).compact();
    }

    /**
     * Issues an HS256 JWT from a public record and deterministic String key material.
     *
     * @param claims   subject and application claim record
     * @param secret   non-empty String key material
     * @param issuer   exact issuer StringOrURI
     * @param audience exact audience StringOrURI
     * @param lifetime positive token lifetime
     * @param <T>      public record type
     * @return compact issued JWT
     */
    public static <T extends Record> String issue(
            final T claims,
            final String secret,
            final String issuer,
            final String audience,
            final Duration lifetime) {
        return JwtService.hs256(secret).issue(JwtClaims.of(claims), issuer, audience, lifetime).compact();
    }

    /**
     * Issues a JWT with one explicit JWS algorithm and key after generating registered issuer-owned claims.
     *
     * @param claims    subject and application claims that do not replace issuer-generated claims
     * @param algorithm exact JWS signing algorithm
     * @param key       private or symmetric signing key
     * @param issuer    exact issuer StringOrURI
     * @param audience  exact audience StringOrURI
     * @param lifetime  positive token lifetime
     * @return compact issued JWT
     */
    public static String issue(
            final Map<String, ?> claims,
            final JwaAlgorithm algorithm,
            final Key key,
            final String issuer,
            final String audience,
            final Duration lifetime) {
        return new JwtService(algorithm, key).issue(JwtClaims.of(claims), issuer, audience, lifetime).compact();
    }

    /**
     * Issues a JWT from one public record with an explicit JWS algorithm and key.
     *
     * @param claims    subject and application claim record
     * @param algorithm exact JWS signing algorithm
     * @param key       private or symmetric signing key
     * @param issuer    exact issuer StringOrURI
     * @param audience  exact audience StringOrURI
     * @param lifetime  positive token lifetime
     * @param <T>       public record type
     * @return compact issued JWT
     */
    public static <T extends Record> String issue(
            final T claims,
            final JwaAlgorithm algorithm,
            final Key key,
            final String issuer,
            final String audience,
            final Duration lifetime) {
        return new JwtService(algorithm, key).issue(JwtClaims.of(claims), issuer, audience, lifetime).compact();
    }

    /**
     * Cryptographically verifies one compact HS256 JWT without applying issuer or audience policy.
     *
     * @param compact compact signed JWT
     * @param secret  HMAC verification secret containing at least 256 bits of key material
     * @return immutable cryptographically verified JWT
     */
    public static JWT verify(final String compact, final byte[] secret) {
        return JwtService.hs256(secret).verify(compact);
    }

    /**
     * Cryptographically verifies one compact HS256 JWT with deterministic String key material.
     *
     * @param compact compact signed JWT
     * @param secret  non-empty String key material
     * @return immutable cryptographically verified JWT
     */
    public static JWT verify(final String compact, final String secret) {
        return JwtService.hs256(secret).verify(compact);
    }

    /**
     * Parses one compact signed JWT without validating its signature.
     * <p>
     * The returned Header and Claims Set are explicitly untrusted. This entry point exists for bounded tenant or key
     * selection before the caller invokes {@link UnverifiedJWT#verify(String)} or another explicit verification method.
     * </p>
     *
     * @param compact compact signed JWT
     * @return immutable explicitly unverified JWT
     */
    public static UnverifiedJWT parse(final String compact) {
        return PARSER.parse(compact);
    }

    /**
     * Cryptographically verifies one compact JWT with an explicit expected algorithm and key.
     *
     * @param compact           compact signed JWT
     * @param expectedAlgorithm exact trusted JWS algorithm
     * @param key               public or symmetric verification key
     * @return immutable cryptographically verified JWT
     */
    public static JWT verify(final String compact, final JwaAlgorithm expectedAlgorithm, final Key key) {
        return new JwtService(expectedAlgorithm, key).verify(compact);
    }

    /**
     * Verifies one compact HS256 JWT and validates every registered temporal claim that is present.
     *
     * @param compact compact signed JWT
     * @param secret  HMAC verification secret containing at least 256 bits of key material
     * @return immutable cryptographically and temporally validated JWT
     */
    public static JWT validate(final String compact, final byte[] secret) {
        return JwtService.hs256(secret).validate(compact);
    }

    /**
     * Verifies one compact HS256 JWT with String key material and validates every registered temporal claim present.
     *
     * @param compact compact signed JWT
     * @param secret  non-empty String key material
     * @return immutable cryptographically and temporally validated JWT
     */
    public static JWT validate(final String compact, final String secret) {
        return JwtService.hs256(secret).validate(compact);
    }

    /**
     * Verifies one compact JWT with an explicit trusted algorithm and validates every registered temporal claim that is
     * present.
     *
     * @param compact           compact signed JWT
     * @param expectedAlgorithm exact trusted JWS algorithm
     * @param key               public or symmetric verification key
     * @return immutable cryptographically and temporally validated JWT
     */
    public static JWT validate(final String compact, final JwaAlgorithm expectedAlgorithm, final Key key) {
        return new JwtService(expectedAlgorithm, key).validate(compact);
    }

    /**
     * Verifies one compact HS256 JWT and applies explicit common-claim requirements.
     *
     * @param compact      compact signed JWT
     * @param secret       HMAC verification secret containing at least 256 bits of key material
     * @param requirements explicit issuer, audience, temporal, and subject requirements
     * @return immutable fully validated JWT
     */
    public static JWT validate(final String compact, final byte[] secret, final Requirements requirements) {
        return JwtService.hs256(secret).validate(compact, requirements);
    }

    /**
     * Verifies one compact HS256 JWT with String key material and applies explicit common-claim requirements.
     *
     * @param compact      compact signed JWT
     * @param secret       non-empty String key material
     * @param requirements explicit issuer, audience, temporal, and subject requirements
     * @return immutable fully validated JWT
     */
    public static JWT validate(final String compact, final String secret, final Requirements requirements) {
        return JwtService.hs256(secret).validate(compact, requirements);
    }

    /**
     * Verifies one compact JWT with an explicit trusted algorithm and applies common-claim requirements.
     *
     * @param compact           compact signed JWT
     * @param expectedAlgorithm exact trusted JWS algorithm
     * @param key               public or symmetric verification key
     * @param requirements      explicit issuer, audience, temporal, and subject requirements
     * @return immutable fully validated JWT
     */
    public static JWT validate(
            final String compact,
            final JwaAlgorithm expectedAlgorithm,
            final Key key,
            final Requirements requirements) {
        return new JwtService(expectedAlgorithm, key).validate(compact, requirements);
    }

    /**
     * Tests whether one compact HS256 JWT has a valid signature and valid registered temporal claims.
     *
     * @param compact compact signed JWT
     * @param secret  HMAC verification secret containing at least 256 bits of key material
     * @return {@code true} when verification and temporal validation succeed
     */
    public static boolean isValid(final String compact, final byte[] secret) {
        return JwtService.hs256(secret).isValid(compact);
    }

    /**
     * Tests whether one compact HS256 JWT has a valid signature and temporal claims under String key material.
     *
     * @param compact compact signed JWT
     * @param secret  non-empty String key material
     * @return {@code true} when verification and temporal validation succeed
     */
    public static boolean isValid(final String compact, final String secret) {
        return JwtService.hs256(secret).isValid(compact);
    }

    /**
     * Tests whether one compact JWT has a valid signature and valid registered temporal claims under an explicit
     * trusted algorithm.
     *
     * @param compact           compact signed JWT
     * @param expectedAlgorithm exact trusted JWS algorithm
     * @param key               public or symmetric verification key
     * @return {@code true} when verification and temporal validation succeed
     */
    public static boolean isValid(final String compact, final JwaAlgorithm expectedAlgorithm, final Key key) {
        return new JwtService(expectedAlgorithm, key).isValid(compact);
    }

    /**
     * Tests whether one compact HS256 JWT satisfies explicit common-claim requirements.
     *
     * @param compact      compact signed JWT
     * @param secret       HMAC verification secret containing at least 256 bits of key material
     * @param requirements explicit issuer, audience, temporal, and subject requirements
     * @return {@code true} when complete validation succeeds
     */
    public static boolean isValid(final String compact, final byte[] secret, final Requirements requirements) {
        return JwtService.hs256(secret).isValid(compact, requirements);
    }

    /**
     * Tests whether one compact HS256 JWT satisfies explicit common-claim requirements under String key material.
     *
     * @param compact      compact signed JWT
     * @param secret       non-empty String key material
     * @param requirements explicit issuer, audience, temporal, and subject requirements
     * @return {@code true} when complete validation succeeds
     */
    public static boolean isValid(final String compact, final String secret, final Requirements requirements) {
        return JwtService.hs256(secret).isValid(compact, requirements);
    }

    /**
     * Tests whether one compact JWT satisfies explicit common-claim requirements under an explicit trusted algorithm.
     *
     * @param compact           compact signed JWT
     * @param expectedAlgorithm exact trusted JWS algorithm
     * @param key               public or symmetric verification key
     * @param requirements      explicit issuer, audience, temporal, and subject requirements
     * @return {@code true} when complete validation succeeds
     */
    public static boolean isValid(
            final String compact,
            final JwaAlgorithm expectedAlgorithm,
            final Key key,
            final Requirements requirements) {
        return new JwtService(expectedAlgorithm, key).isValid(compact, requirements);
    }

    /**
     * Returns the sensitive compact value for an immediate protocol operation.
     *
     * @return compact JWS or JWE representation
     */
    public String compact() {
        return compact;
    }

    /**
     * Returns the parsed outer JOSE Header.
     *
     * @return immutable JOSE Header
     */
    public JoseHeader header() {
        return header;
    }

    /**
     * Returns the parsed JWT Claims Set.
     *
     * @return immutable claims
     */
    public JwtClaims claims() {
        return claims;
    }

    /**
     * Returns whether the compact value is a JWS or JWE representation.
     *
     * @return serialization kind
     */
    public Kind kind() {
        return kind;
    }

    /**
     * Returns a fixed non-sensitive diagnostic representation.
     *
     * @return redacted JWT label and serialization kind
     */
    @Override
    public String toString() {
        return "JWT[kind=" + kind + ", compact=[REDACTED]]";
    }

    /**
     * Identifies the outer JOSE compact serialization used by a JWT.
     *
     * @author Kimi Liu
     */
    public enum Kind {
        /**
         * JWT represented as a three-segment compact JWS.
         */
        SIGNED,
        /**
         * JWT represented as a five-segment compact JWE.
         */
        ENCRYPTED

    }

    /**
     * Defines explicit common-claim requirements for the public static JWT validation entry point.
     *
     * @param issuer             exact required issuer
     * @param audiences          non-empty allowed audience set
     * @param clockSkew          non-negative accepted clock displacement
     * @param subjectRequired    whether {@code sub} must be present
     * @param expirationRequired whether {@code exp} must be present
     * @param issuedAtRequired   whether {@code iat} must be present
     * @param maximumAge         optional positive maximum age measured from {@code iat}
     * @author Kimi Liu
     */
    public record Requirements(String issuer, Set<String> audiences, Duration clockSkew, boolean subjectRequired,
            boolean expirationRequired, boolean issuedAtRequired, Optional<Duration> maximumAge) {

        /**
         * Validates and freezes public common-claim requirements.
         */
        public Requirements {
            Assert.notBlank(issuer, "JWT required issuer must not be blank");
            Assert.notNull(audiences, "JWT allowed audiences must not be null");
            Assert.notNull(clockSkew, "JWT clock skew must not be null");
            Assert.notNull(maximumAge, "JWT maximum-age container must not be null");
            audiences = Set.copyOf(audiences);
            if (audiences.isEmpty()) {
                throw new ValidateException("JWT allowed audiences must not be empty");
            }
            for (String audience : audiences) {
                Assert.notBlank(audience, "JWT allowed audience must not be blank");
            }
            if (clockSkew.isNegative()) {
                throw new ValidateException("JWT clock skew must not be negative");
            }
            if (maximumAge.filter(value -> value.isZero() || value.isNegative()).isPresent()) {
                throw new ValidateException("JWT maximum age must be positive");
            }
        }

        /**
         * Creates the common validation requirements for one issuer and audience.
         *
         * @param issuer    exact required issuer
         * @param audience  exact allowed audience
         * @param clockSkew non-negative accepted clock displacement
         * @return immutable requirements that mandate subject, expiration, and issued-at claims
         */
        public static Requirements of(final String issuer, final String audience, final Duration clockSkew) {
            return new Requirements(issuer, Set.of(Assert.notBlank(audience, "JWT audience must not be blank")),
                    clockSkew, true, true, true, Optional.empty());
        }

    }

}
