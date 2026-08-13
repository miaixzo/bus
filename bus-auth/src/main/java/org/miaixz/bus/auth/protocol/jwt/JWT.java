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

import java.security.SecureRandom;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Claims;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.protocol.jwt.signature.JWTSigner;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

/**
 * Stateless JWT facade exposing only creation, parsing, and verification entry points.
 *
 * @author Kimi Liu
 */
public final class JWT {

    /**
     * Prevents construction of the static facade.
     */
    private JWT() {
        // No initialization required.
    }

    /**
     * Delegates bounded issuance to {@link JWTCreator}.
     */
    public static String create(
            final Claims claims,
            final VerificationPolicy policy,
            final Clock clock,
            final SecureRandom random,
            final JsonProvider provider,
            final int maximumHeaderBytes,
            final int maximumPayloadBytes,
            final int maximumTokenBytes,
            final JWTSigner signer) {
        return JWTCreator.create(
                claims,
                policy,
                clock,
                random,
                provider,
                maximumHeaderBytes,
                maximumPayloadBytes,
                maximumTokenBytes,
                signer);
    }

    /**
     * Delegates compact parsing to {@link JWTParser}.
     */
    public static JWTParser.Parsed parse(
            final String token,
            final JsonProvider provider,
            final int maximumTokenBytes,
            final int maximumHeaderBytes,
            final int maximumPayloadBytes,
            final int maximumJsonDepth) {
        return JWTParser
                .parse(token, provider, maximumTokenBytes, maximumHeaderBytes, maximumPayloadBytes, maximumJsonDepth);
    }

    /**
     * Delegates policy verification to {@link JWTVerifier}.
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
        return JWTVerifier.verify(
                token,
                policy,
                context,
                provider,
                clock,
                keys,
                states,
                maximumTokenBytes,
                maximumHeaderBytes,
                maximumPayloadBytes,
                maximumJsonDepth,
                maximumCandidates);
    }

    /**
     * Closed product-selectable JOSE algorithm allow-list.
     *
     * @author Kimi Liu
     */
    public enum TrustedAlgorithm {

        /**
         * HMAC using SHA-256.
         */
        HS256("HS256"),
        /**
         * RSASSA-PKCS1-v1_5 using SHA-256.
         */
        RS256("RS256"),
        /**
         * RSASSA-PSS using SHA-256.
         */
        PS256("PS256"),
        /**
         * ECDSA using P-256 and SHA-256.
         */
        ES256("ES256"),
        /**
         * EdDSA using Ed25519.
         */
        EDDSA("EdDSA"),
        /**
         * Unsecured JWS, available only through explicit policy selection.
         */
        NONE("none");

        /**
         * Exact case-sensitive JOSE identifier.
         */
        private final String identifier;

        /**
         * Creates one trusted mapping.
         */
        TrustedAlgorithm(final String identifier) {
            this.identifier = identifier;
        }

        /**
         * Resolves an exact identifier without aliases.
         */
        public static TrustedAlgorithm resolve(final String identifier) {
            final String value = Assert
                    .notBlank(identifier, () -> new ValidateException("JWT algorithm must not be blank"));
            for (final TrustedAlgorithm algorithm : values()) {
                if (algorithm.identifier.equals(value)) {
                    return algorithm;
                }
            }
            throw new ValidateException("JWT algorithm is not trusted");
        }

        /**
         * @return exact JOSE identifier
         */
        public String identifier() {
            return identifier;
        }
    }

    /**
     * Immutable product-selected JWT verification and issuance policy.
     *
     * @param algorithm       trusted algorithm
     * @param issuer          exact issuer
     * @param audiences       non-empty accepted audiences
     * @param skew            non-negative clock tolerance
     * @param maximumLifetime positive token lifetime
     * @param requireReplay   whether JWT ID replay admission is mandatory
     * @author Kimi Liu
     */
    public record VerificationPolicy(TrustedAlgorithm algorithm, String issuer, Set<String> audiences, Duration skew,
            Duration maximumLifetime, boolean requireReplay) {

        /**
         * Validates and snapshots all policy values.
         */
        public VerificationPolicy {
            algorithm = Assert.notNull(algorithm, () -> new ValidateException("JWT algorithm must not be null"));
            issuer = Assert.notBlank(issuer, () -> new ValidateException("JWT issuer must not be blank"));
            final Set<String> source = Set
                    .copyOf(Assert.notNull(audiences, () -> new ValidateException("JWT audiences must not be null")));
            if (source.isEmpty()) {
                throw new ValidateException("JWT audiences must not be empty");
            }
            final LinkedHashSet<String> checked = new LinkedHashSet<>();
            for (final String audience : source) {
                checked.add(Assert.notBlank(audience, () -> new ValidateException("JWT audience must not be blank")));
            }
            audiences = Set.copyOf(checked);
            skew = Assert.notNull(skew, () -> new ValidateException("JWT clock skew must not be null"));
            if (skew.isNegative()) {
                throw new ValidateException("JWT clock skew must not be negative");
            }
            maximumLifetime = Assert
                    .notNull(maximumLifetime, () -> new ValidateException("JWT maximum lifetime must not be null"));
            if (maximumLifetime.isZero() || maximumLifetime.isNegative()) {
                throw new ValidateException("JWT maximum lifetime must be positive");
            }
        }

        /**
         * Returns a representation omitting issuer and audience values.
         */
        @Override
        public String toString() {
            return "VerificationPolicy[algorithm=" + algorithm.identifier() + ",requireReplay=" + requireReplay + "]";
        }
    }

}
