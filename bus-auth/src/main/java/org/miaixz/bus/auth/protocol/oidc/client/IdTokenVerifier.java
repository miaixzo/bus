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
package org.miaixz.bus.auth.protocol.oidc.client;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.guard.IssuerValidator;
import org.miaixz.bus.auth.guard.TimeGuard;
import org.miaixz.bus.auth.protocol.oidc.IdToken;
import org.miaixz.bus.auth.protocol.oidc.IdTokenClaims;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.protocol.oidc.codec.IdTokenCodec;
import org.miaixz.bus.auth.shared.jwt.JwtVerifier;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.builtin.digest.Digester;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Applies the complete OpenID Connect relying-party validation profile to one ID Token.
 * <p>
 * Key discovery is explicit: the caller supplies a {@link JwtVerifier.Verification} created from a trusted,
 * issuer-bound JWK Set or local key. This class never guesses keys or algorithms and returns only typed ID Token
 * claims.
 * </p>
 *
 * @author Kimi Liu
 */
public class IdTokenVerifier {

    /**
     * Codec that performs JOSE verification and typed claim conversion.
     */
    private final IdTokenCodec codec;

    /**
     * Shared case-sensitive issuer comparison primitive.
     */
    private final IssuerValidator issuerValidator;

    /**
     * Shared clock and skew validation primitive.
     */
    private final TimeGuard timeGuard;

    /**
     * Creates an ID Token verifier from shared protocol and security primitives.
     *
     * @param codec           JOSE-aware ID Token codec
     * @param issuerValidator exact issuer validator
     * @param timeGuard       shared-clock temporal validator
     * @throws IllegalArgumentException if any collaborator is {@code null}
     */
    public IdTokenVerifier(final IdTokenCodec codec, final IssuerValidator issuerValidator, final TimeGuard timeGuard) {
        this.codec = Assert.notNull(codec, "OpenID Connect ID Token codec must not be null");
        this.issuerValidator = Assert.notNull(issuerValidator, "OpenID Connect issuer validator must not be null");
        this.timeGuard = Assert.notNull(timeGuard, "OpenID Connect time guard must not be null");
    }

    /**
     * Validates a max-age request against the ID Token authentication time.
     *
     * @param maximumAge      optional maximum age in seconds
     * @param authenticatedAt optional authentication instant
     * @param timeout         shared operation timeout carrying the common clock
     * @throws ValidateException if auth_time is missing or too old
     */
    private static void validateAuthenticationAge(
            final Long maximumAge,
            final Instant authenticatedAt,
            final Timeout timeout) {
        if (maximumAge == null) {
            return;
        }
        if (authenticatedAt == null) {
            throw new ValidateException("OpenID Connect max_age validation requires auth_time");
        }
        try {
            if (!authenticatedAt.plusSeconds(maximumAge).isAfter(timeout.clock().now())) {
                throw new ValidateException("OpenID Connect authentication exceeds requested max_age");
            }
        } catch (ArithmeticException cause) {
            throw new ValidateException("OpenID Connect max_age calculation exceeds the supported time range", cause);
        }
    }

    /**
     * Validates one optional OIDC artifact hash against its corresponding value.
     *
     * @param expected         optional hash claim
     * @param artifact         optional source artifact
     * @param signingAlgorithm verified ID Token JWS algorithm
     * @param claim            safe registered claim name
     * @throws ValidateException if an artifact is absent, algorithm unsupported, or hash mismatched
     */
    private static void validateHash(
            final String expected,
            final String artifact,
            final String signingAlgorithm,
            final String claim) {
        if (expected == null) {
            return;
        }
        if (artifact == null) {
            throw new ValidateException("OpenID Connect " + claim + " cannot be validated without its artifact");
        }
        final Algorithm digestAlgorithm = digestAlgorithm(signingAlgorithm);
        final byte[] digest = new Digester(digestAlgorithm).digest(artifact.getBytes(Charset.UTF_8));
        final byte[] leftHalf = Arrays.copyOf(digest, digest.length / 2);
        if (!constantTime(expected, Base64.encodeUrlSafe(leftHalf))) {
            throw new ValidateException("OpenID Connect " + claim + " validation failed");
        }
    }

    /**
     * Maps supported JWS algorithm strengths to the OIDC hash-function family.
     *
     * @param signingAlgorithm verified case-sensitive JWS algorithm identifier
     * @return Bus digest algorithm
     * @throws ValidateException if OIDC does not define hash calculation for the algorithm
     */
    private static Algorithm digestAlgorithm(final String signingAlgorithm) {
        Assert.notBlank(signingAlgorithm, "OpenID Connect ID Token signing algorithm must not be blank");
        if (signingAlgorithm.endsWith("256")) {
            return Algorithm.SHA256;
        }
        if (signingAlgorithm.endsWith("384")) {
            return Algorithm.SHA384;
        }
        if (signingAlgorithm.endsWith("512")) {
            return Algorithm.SHA512;
        }
        throw new ValidateException("OpenID Connect hash claims do not support the selected JWS algorithm");
    }

    /**
     * Compares sensitive protocol strings without early exit on matching prefixes.
     *
     * @param left  expected value
     * @param right received value
     * @return whether both strings have identical UTF-8 octets
     */
    private static boolean constantTime(final String left, final String right) {
        final byte[] first = left.getBytes(Charset.UTF_8);
        final byte[] second = right.getBytes(Charset.UTF_8);
        int difference = first.length ^ second.length;
        final int length = Math.max(first.length, second.length);
        for (int index = 0; index < length; index++) {
            difference |= first[index % first.length] ^ second[index % second.length];
        }
        return difference == 0;
    }

    /**
     * Creates a completed asynchronous result.
     *
     * @param <T>     success value type
     * @param outcome completed outcome
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Verifies JOSE protection, registered claims, nonce, temporal state, and artifact hashes.
     *
     * @param request explicit ID Token verification inputs
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return completed stage containing verified typed claims or a closed failure
     */
    public CompletionStage<Outcome<IdTokenClaims>> verify(
            final Request request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OpenID Connect ID Token verification request must not be null");
        Assert.notNull(context, "OpenID Connect ID Token verification context must not be null");
        Assert.notNull(timeout, "OpenID Connect ID Token verification timeout must not be null");
        try {
            timeGuard.validateTimeout(timeout);
            final IdTokenCodec.Decoded decoded = codec.decode(request.idToken(), request.verification());
            validate(request, decoded, timeout);
            return completed(Outcome.succeeded(decoded.claims()));
        } catch (ValidateException exception) {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._401, "OpenID Connect ID Token validation failed",
                                    new JsonValue.ObjectValue(Map.of()))));
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(
                            new Outcome.Failure(ErrorCode._500,
                                    "OpenID Connect ID Token verification could not complete",
                                    new JsonValue.ObjectValue(Map.of()))));
        }
    }

    /**
     * Applies synchronous OpenID Connect claim binding after cryptographic verification.
     *
     * @param request explicit expected values and related artifacts
     * @param decoded cryptographically verified JWT and typed claims
     * @param timeout shared operation timeout
     * @throws ValidateException if any required binding fails
     */
    private void validate(final Request request, final IdTokenCodec.Decoded decoded, final Timeout timeout) {
        final IdTokenClaims claims = decoded.claims();
        issuerValidator.validate(request.expectedIssuer(), claims.issuer());
        if (!claims.audience().contains(request.clientId())) {
            throw new ValidateException("OpenID Connect ID Token audience does not contain the client identifier");
        }
        final String authorizedParty = claims.authorizedParty().getOrNull();
        if (claims.audience().size() > 1 && authorizedParty == null) {
            throw new ValidateException("OpenID Connect multi-audience ID Token requires azp");
        }
        if (authorizedParty != null && !constantTime(request.clientId(), authorizedParty)) {
            throw new ValidateException("OpenID Connect ID Token azp does not match the client identifier");
        }
        timeGuard.validateIssuedAt(claims.issuedAt(), timeout);
        timeGuard.validateExpiration(claims.expiration(), timeout);
        if (!claims.issuedAt().isBefore(claims.expiration())) {
            throw new ValidateException("OpenID Connect ID Token iat must precede exp");
        }
        final String nonce = claims.nonce().getOrNull();
        if (nonce == null || !constantTime(request.expectedNonce(), nonce)) {
            throw new ValidateException("OpenID Connect ID Token nonce does not match the Authentication Request");
        }
        validateAuthenticationAge(request.maxAge().getOrNull(), claims.authenticatedAt().getOrNull(), timeout);

        final String algorithm = decoded.jwt().header().algorithm();
        validateHash(
                claims.accessTokenHash().getOrNull(),
                request.accessToken().getOrNull(),
                algorithm,
                OpenIdConnect.Claims.ACCESS_TOKEN_HASH);
        validateHash(
                claims.codeHash().getOrNull(),
                request.authorizationCode().getOrNull(),
                algorithm,
                OpenIdConnect.Claims.CODE_HASH);
        validateHash(
                claims.stateHash().getOrNull(),
                request.state().getOrNull(),
                algorithm,
                OpenIdConnect.Claims.STATE_HASH);
    }

    /**
     * Carries explicit trusted inputs and related artifacts for one ID Token validation.
     *
     * @param idToken           sensitive ID Token compact serialization
     * @param verification      explicit JOSE key and critical-extension verification input
     * @param expectedIssuer    trusted OpenID Provider issuer
     * @param clientId          relying-party client identifier expected in aud and azp
     * @param expectedNonce     nonce sent in the Authentication Request
     * @param maxAge            optional requested maximum authentication age in seconds
     * @param accessToken       optional access token used when at_hash is present
     * @param authorizationCode optional authorization code used when c_hash is present
     * @param state             optional state value used when s_hash is present
     * @author Kimi Liu
     */
    public record Request(IdToken idToken, JwtVerifier.Verification verification, String expectedIssuer,
            String clientId, String expectedNonce, Optional<Long> maxAge, Optional<String> accessToken,
            Optional<String> authorizationCode, Optional<String> state) {

        /**
         * Validates and normalizes one complete ID Token verification request.
         *
         * @throws IllegalArgumentException if a required component or optional container is {@code null}
         * @throws ValidateException        if text is blank or maximum age is negative
         */
        public Request {
            Assert.notNull(idToken, "OpenID Connect ID Token must not be null");
            Assert.notNull(verification, "OpenID Connect JOSE verification input must not be null");
            expectedIssuer = Assert.notBlank(expectedIssuer, "OpenID Connect expected issuer must not be blank");
            clientId = Assert.notBlank(clientId, "OpenID Connect client identifier must not be blank");
            expectedNonce = Assert.notBlank(expectedNonce, "OpenID Connect expected nonce must not be blank");
            Assert.notNull(maxAge, "OpenID Connect maximum-age container must not be null");
            final Long age = maxAge.getOrNull();
            if (age != null && age < 0L) {
                throw new ValidateException("OpenID Connect maximum age must not be negative");
            }
            maxAge = Optional.ofNullable(age);
            accessToken = optionalSensitive(accessToken, "OpenID Connect access token");
            authorizationCode = optionalSensitive(authorizationCode, "OpenID Connect authorization code");
            state = optionalSensitive(state, "OpenID Connect state");
        }

        /**
         * Normalizes one optional sensitive protocol value.
         *
         * @param value optional source container
         * @param label safe diagnostic label
         * @return normalized Bus optional
         */
        private static Optional<String> optionalSensitive(final Optional<String> value, final String label) {
            Assert.notNull(value, label + " container must not be null");
            final String present = value.getOrNull();
            if (present != null) {
                Assert.notEmpty(present, label + " must not be empty");
            }
            return Optional.ofNullable(present);
        }

        /**
         * Returns a diagnostic summary without token, nonce, code, or state values.
         *
         * @return redacted verification request summary
         */
        @Override
        public String toString() {
            return "Request[idToken=[REDACTED], verification=" + verification.getClass().getSimpleName()
                    + ", expectedIssuer=" + expectedIssuer + ", clientId=[REDACTED], expectedNonce=[REDACTED]"
                    + ", maxAge=" + maxAge + ", accessToken=[REDACTED], authorizationCode=[REDACTED]"
                    + ", state=[REDACTED]]";
        }

    }

}
