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

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.KeyMaterial;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.JWT;
import org.miaixz.bus.auth.metric.JWT.TrustedAlgorithm;
import org.miaixz.bus.auth.metric.JWT.VerificationPolicy;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSignerBuilder;
import org.miaixz.bus.auth.metric.jwt.signature.NoneJWTSigner;
import org.miaixz.bus.auth.metric.shared.security.ReplayKey;
import org.miaixz.bus.auth.metric.shared.state.StateEnvelopeCodec;
import org.miaixz.bus.auth.metric.shared.validation.TimeValidator;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Keeper;

/**
 * JWT verifier retaining compatibility validation and providing one hardened asynchronous verification pipeline.
 * <ul>
 * <li>Compact format, Base64url, and strict JSON parsing.</li>
 * <li>Protected-header policy validation.</li>
 * <li>Tenant-, use-, algorithm-, and key-ID-scoped candidate resolution.</li>
 * <li>Signature verification before claim inspection.</li>
 * <li>Issuer, audience, expiration, not-before, and issued-at validation.</li>
 * <li>Atomic JWT ID replay admission.</li>
 * </ul>
 *
 * @author Kimi Liu
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
     * Executes the fixed hardened verification sequence without blocking a resolver or state stage.
     *
     * @param token      untrusted compact JWT
     * @param policy     trusted product-selected verification policy
     * @param invocation tenant-scoped operation context
     * @param runtime    validated authentication runtime
     * @return stage containing the immutable verified payload
     */
    public static CompletionStage<JWTPayload> verify(
            final String token,
            final VerificationPolicy policy,
            final Invocation invocation,
            final Runtime runtime) {
        final VerificationPolicy trusted = Assert.notNull(policy, "JWT policy must be not null!");
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final Runtime ports = Assert.notNull(runtime, "Authentication runtime must be not null!");
        final JWTParser.Parsed parsed = JWTParser.parse(token, ports.json(), ports.limits());
        final JWTHeader header = parsed.header().validate(trusted.algorithm());
        final Object keyIdValue = header.getClaim(JWTHeader.KEY_ID);
        final String keyId = keyIdValue == null ? null : (String) keyIdValue;
        final CompletionStage<List<KeyMaterial>> resolved = Assert.notNull(
                ports.keys().resolve(context, "sig", trusted.algorithm().identifier(), keyId),
                "Key resolver stage must be not null!");
        return resolved.thenCompose(candidates -> {
            verifySignature(parsed, trusted.algorithm(), keyId, candidates, ports.limits().maxParameters());
            final JWTPayload payload = parsed.payload();
            verifyClaims(payload, trusted, ports);
            return admitReplay(payload, trusted, context, ports);
        });
    }

    /**
     * Verifies the signature against bounded, metadata-matching candidate keys.
     *
     * @param parsed            strict parsed token
     * @param algorithm         trusted algorithm
     * @param keyId             optional protected-header key identifier
     * @param candidates        resolved key candidates
     * @param maximumCandidates maximum accepted candidate count
     */
    private static void verifySignature(
            final JWTParser.Parsed parsed,
            final TrustedAlgorithm algorithm,
            final String keyId,
            final List<KeyMaterial> candidates,
            final int maximumCandidates) {
        if (candidates == null || candidates.isEmpty() || candidates.size() > maximumCandidates) {
            invalidSignature();
        }
        boolean verified = false;
        for (final KeyMaterial candidate : candidates) {
            if (candidate == null || !"sig".equals(candidate.use())
                    || !algorithm.identifier().equals(candidate.algorithm())
                    || keyId != null && !keyId.equals(candidate.keyId())) {
                continue;
            }
            try {
                final JWTSigner signer = signer(algorithm, candidate.material());
                verified |= signer.verify(parsed.headerSegment(), parsed.payloadSegment(), parsed.signatureSegment());
            } catch (final RuntimeException ignored) {
                // Invalid candidates do not prevent evaluation of another resolver-scoped candidate.
            }
        }
        if (!verified) {
            invalidSignature();
        }
    }

    /**
     * Builds one verification-only signer from encoded resolver material.
     *
     * @param algorithm trusted algorithm
     * @param material  encoded key material or raw HMAC secret
     * @return signer bound to the trusted algorithm
     */
    private static JWTSigner signer(final TrustedAlgorithm algorithm, final byte[] material) {
        if (material == null || material.length == Normal._0) {
            invalidSignature();
        }
        if (algorithm == TrustedAlgorithm.HS256) {
            return JWTSignerBuilder.createSigner(algorithm.identifier(), material);
        }
        final String keyAlgorithm = switch (algorithm) {
            case RS256, PS256 -> Algorithm.RSA.getValue();
            case ES256 -> Algorithm.EC.getValue();
            case EDDSA -> Algorithm.ED25519.getValue();
            case HS256 -> throw new IllegalStateException("HMAC key material is not asymmetric");
        };
        final Key key = Keeper.generatePublicKey(keyAlgorithm, material);
        return JWTSignerBuilder.createSigner(algorithm.identifier(), key);
    }

    /**
     * Validates claims after successful signature verification.
     *
     * @param payload immutable payload
     * @param policy  trusted verification policy
     * @param runtime authentication runtime
     */
    private static void verifyClaims(final JWTPayload payload, final VerificationPolicy policy, final Runtime runtime) {
        if (!payload.issuer().filter(policy.issuer()::equals).isPresent()) {
            invalidToken();
        }
        final boolean audienceMatch = payload.audiences().stream().anyMatch(policy.audiences()::contains);
        if (!audienceMatch) {
            invalidToken();
        }
        final Instant issuedAt = payload.issuedAt().orElseThrow(JWTVerifier::tokenFailure);
        final Instant expiresAt = payload.expiresAt().orElseThrow(JWTVerifier::tokenFailure);
        final Instant notBefore = payload.notBefore().orElse(null);
        new TimeValidator(runtime.clock(), policy.skew())
                .validate(issuedAt, notBefore, expiresAt, policy.maximumLifetime());
    }

    /**
     * Atomically admits the JWT identifier after every preceding verification step.
     *
     * @param payload    verified immutable payload
     * @param policy     trusted verification policy
     * @param invocation tenant-scoped operation context
     * @param runtime    authentication runtime
     * @return stage containing the verified payload
     */
    private static CompletionStage<JWTPayload> admitReplay(
            final JWTPayload payload,
            final VerificationPolicy policy,
            final Invocation invocation,
            final Runtime runtime) {
        if (!policy.requireReplay()) {
            return java.util.concurrent.CompletableFuture.completedFuture(payload);
        }
        final String identifier = payload.jwtId().orElseThrow(JWTVerifier::tokenFailure);
        final Instant expiresAt = payload.expiresAt().orElseThrow(JWTVerifier::tokenFailure);
        final Duration maximumTtl;
        try {
            maximumTtl = policy.maximumLifetime().plus(policy.skew());
        } catch (final ArithmeticException failure) {
            throw new ProtocolException(ErrorCode._100301.getKey(), ErrorCode._100301.getValue(), failure);
        }
        final Duration ttl = new TimeValidator(runtime.clock(), policy.skew()).ttl(expiresAt, maximumTtl);
        final String key = ReplayKey.derive(invocation.tenantId(), "jwt", "jti", identifier);
        final byte[] value = StateEnvelopeCodec.encode(new byte[] { Normal._1 });
        final CompletionStage<Boolean> admitted = Assert.notNull(
                runtime.states().putIfAbsent(invocation, key, value, ttl),
                "State store stage must be not null!");
        return admitted.thenApply(created -> {
            if (!Boolean.TRUE.equals(created)) {
                invalidToken();
            }
            return payload;
        });
    }

    /**
     * Creates a shared invalid-token failure for optional extraction.
     *
     * @return invalid-token failure
     */
    private static ProtocolException tokenFailure() {
        return new ProtocolException(ErrorCode._100533);
    }

    /**
     * Rejects an invalid token.
     */
    private static void invalidToken() {
        throw tokenFailure();
    }

    /**
     * Rejects an invalid signature.
     */
    private static void invalidSignature() {
        throw new ProtocolException(ErrorCode._100532);
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
     * @throws ValidateException if the algorithm does not match or the signature is invalid
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
