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

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.codec.state.StateEnvelopeCodec;
import org.miaixz.bus.auth.guard.ReplayKey;
import org.miaixz.bus.auth.guard.TimeValidator;
import org.miaixz.bus.auth.protocol.jwt.JWT.TrustedAlgorithm;
import org.miaixz.bus.auth.protocol.jwt.JWT.VerificationPolicy;
import org.miaixz.bus.auth.protocol.jwt.signature.JWTSigner;
import org.miaixz.bus.auth.protocol.jwt.signature.JWTSignerBuilder;
import org.miaixz.bus.auth.protocol.jwt.signature.NoneJWTSigner;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

/**
 * Verifies compact JWTs against one explicit policy, Fabric clock, key resolver, and optional replay store.
 *
 * @author Kimi Liu
 */
public final class JWTVerifier {

    /**
     * Prevents construction of the stateless verifier.
     */
    private JWTVerifier() {
        // No initialization required.
    }

    /**
     * Executes parsing, header policy, key resolution, signature, registered claims, and replay admission in order.
     *
     * @param token               untrusted compact JWT
     * @param policy              trusted product policy
     * @param context             tenant-scoped context
     * @param provider            product JSON provider
     * @param clock               Fabric clock
     * @param keys                tenant-scoped key resolver
     * @param states              atomic state store required when replay checking is enabled
     * @param maximumTokenBytes   maximum compact token bytes
     * @param maximumHeaderBytes  maximum decoded header bytes
     * @param maximumPayloadBytes maximum decoded payload bytes
     * @param maximumJsonDepth    maximum JSON nesting depth
     * @param maximumCandidates   maximum resolved key candidates
     * @return non-null stage containing a non-null root outcome
     */
    public static CompletionStage<Outcome<JWTPayload>> verify(
            final String token,
            final VerificationPolicy policy,
            final Context context,
            final JsonProvider provider,
            final Clock clock,
            final KeyResolver keys,
            final StateStore states,
            final int maximumTokenBytes,
            final int maximumHeaderBytes,
            final int maximumPayloadBytes,
            final int maximumJsonDepth,
            final int maximumCandidates) {
        try {
            final VerificationPolicy trusted = Assert.notNull(policy, "JWT policy must be not null!");
            final Context invocation = Assert.notNull(context, "Context must be not null!");
            final Clock time = Assert.notNull(clock, "Clock must be not null!");
            final KeyResolver resolver = Assert.notNull(keys, "Key resolver must be not null!");
            if (maximumCandidates <= 0) {
                invalidToken();
            }
            final JWTParser.Parsed parsed = JWTParser.parse(
                    token,
                    provider,
                    maximumTokenBytes,
                    maximumHeaderBytes,
                    maximumPayloadBytes,
                    maximumJsonDepth);
            final JWTHeader header = parsed.header().validate(trusted.algorithm());
            if (trusted.algorithm() == TrustedAlgorithm.NONE) {
                if (!NoneJWTSigner.authorized(trusted).verify(parsed.signingInput(), parsed.signature())) {
                    invalidSignature();
                }
                verifyClaims(parsed.payload(), trusted, time);
                return admitReplay(parsed.payload(), trusted, invocation, time, states).handle(
                        (payload, failure) -> failure == null ? new Outcome.Success<>(payload) : failed(failure));
            }
            final CompletionStage<List<KeyMaterial>> resolved = Assert.notNull(
                    resolver.resolve(invocation, "sig", trusted.algorithm().identifier(), header.keyId()),
                    "Key resolver stage must be not null!");
            return resolved.thenCompose(candidates -> {
                verifySignature(parsed, trusted.algorithm(), header.keyId(), candidates, maximumCandidates);
                verifyClaims(parsed.payload(), trusted, time);
                return admitReplay(parsed.payload(), trusted, invocation, time, states);
            }).handle((payload, failure) -> failure == null ? new Outcome.Success<>(payload) : failed(failure));
        } catch (final RuntimeException failure) {
            return CompletableFuture.completedFuture(failed(failure));
        }
    }

    /**
     * Verifies signature against every bounded metadata-matching candidate without early success return.
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
                verified |= signer(algorithm, candidate.material()).verify(parsed.signingInput(), parsed.signature());
            } catch (final RuntimeException ignored) {
                // Every resolver-scoped candidate remains eligible for evaluation.
            }
        }
        if (!verified) {
            invalidSignature();
        }
    }

    /**
     * Creates one verification signer from trusted algorithm and resolver material.
     */
    private static JWTSigner signer(final TrustedAlgorithm algorithm, final byte[] material) {
        if (material == null || material.length == 0) {
            invalidSignature();
        }
        if (algorithm == TrustedAlgorithm.HS256) {
            return JWTSignerBuilder.createSigner(algorithm.identifier(), material);
        }
        final String keyAlgorithm = switch (algorithm) {
            case RS256, PS256 -> Algorithm.RSA.getValue();
            case ES256 -> Algorithm.EC.getValue();
            case EDDSA -> Algorithm.ED25519.getValue();
            case HS256 -> throw new IllegalStateException("Unexpected HMAC branch");
            case NONE -> throw new IllegalStateException("Unsecured JWT does not resolve keys");
        };
        final Key key = Keeper.generatePublicKey(keyAlgorithm, material);
        return JWTSignerBuilder.createSigner(algorithm.identifier(), key);
    }

    /**
     * Validates issuer, audience, issued-at, not-before, expiration, and maximum lifetime.
     */
    private static void verifyClaims(final JWTPayload payload, final VerificationPolicy policy, final Clock clock) {
        if (payload.issuer().filter(policy.issuer()::equals).isEmpty()
                || payload.audiences().stream().noneMatch(policy.audiences()::contains)) {
            invalidToken();
        }
        final Instant issuedAt = payload.issuedAt().orElseThrow(JWTVerifier::tokenFailure);
        final Instant expiresAt = payload.expiresAt().orElseThrow(JWTVerifier::tokenFailure);
        new TimeValidator(clock, policy.skew())
                .validate(issuedAt, payload.notBefore().orElse(null), expiresAt, policy.maximumLifetime());
    }

    /**
     * Atomically admits the JWT identifier when replay checking is enabled.
     */
    private static CompletionStage<JWTPayload> admitReplay(
            final JWTPayload payload,
            final VerificationPolicy policy,
            final Context context,
            final Clock clock,
            final StateStore states) {
        if (!policy.requireReplay()) {
            return CompletableFuture.completedFuture(payload);
        }
        final StateStore store = Assert.notNull(states, "State store must be not null when replay is required!");
        final String identifier = payload.jwtId().orElseThrow(JWTVerifier::tokenFailure);
        final Instant expiresAt = payload.expiresAt().orElseThrow(JWTVerifier::tokenFailure);
        final Duration maximumTtl = policy.maximumLifetime().plus(policy.skew());
        final Duration ttl = new TimeValidator(clock, policy.skew()).ttl(expiresAt, maximumTtl);
        final String key = ReplayKey.derive(context.realm().id(), "jwt", "jti", identifier);
        final CompletionStage<Boolean> admitted = Assert.notNull(
                store.putIfAbsent(context, key, StateEnvelopeCodec.INSTANCE.encode(new byte[] { Normal._1 }), ttl),
                "State store stage must be not null!");
        return admitted.thenApply(created -> {
            if (!Boolean.TRUE.equals(created)) {
                invalidToken();
            }
            return payload;
        });
    }

    /**
     * Converts an internal verification failure to a non-serialized root outcome.
     */
    private static Outcome<JWTPayload> failed(final Throwable failure) {
        final Throwable cause = failure instanceof java.util.concurrent.CompletionException
                && failure.getCause() != null ? failure.getCause() : failure;
        return new Outcome.Failed<>(new Outcome.Failure(Outcome.Kind.AUTHENTICATION, ErrorCode._100533, false), cause);
    }

    /**
     * @return stable invalid-token exception
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

}
