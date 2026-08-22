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
package org.miaixz.bus.auth.shared.dpop;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.guard.ReplayGuard;
import org.miaixz.bus.auth.guard.SecretGuard;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Completes DPoP validation by binding a verified proof to an optional token {@code cnf.jkt} and atomically recording
 * its key-scoped {@code jti} against replay.
 * <p>
 * Expected protocol refusals become rejected outcomes. Replay-cache operational failures retain the failed outcome
 * classification produced by {@link ReplayGuard}; no proof, token, or thumbprint material enters failure details.
 * </p>
 *
 * @author Kimi Liu
 */
public class DpopValidator {

    /**
     * Cryptographic and request-binding verifier.
     */
    private final DpopVerifier verifier;
    /**
     * Atomic proof replay guard.
     */
    private final ReplayGuard replayGuard;
    /**
     * Constant-time confirmation thumbprint comparator.
     */
    private final SecretGuard secretGuard;

    /**
     * Creates the complete DPoP validation coordinator.
     *
     * @param verifier    proof signature and request-binding verifier
     * @param replayGuard atomic replay registration primitive
     * @param secretGuard constant-time token-confirmation comparator
     */
    public DpopValidator(final DpopVerifier verifier, final ReplayGuard replayGuard, final SecretGuard secretGuard) {
        this.verifier = Assert.notNull(verifier, "DPoP verifier must not be null");
        this.replayGuard = Assert.notNull(replayGuard, "DPoP replay guard must not be null");
        this.secretGuard = Assert.notNull(secretGuard, "DPoP secret guard must not be null");
    }

    /**
     * Creates an already completed outcome stage.
     *
     * @param outcome completed internal outcome
     * @param <T>     outcome success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a non-sensitive refusal using an existing Bus error definition.
     *
     * @param description safe generic description
     * @return structured outcome failure
     */
    private static Outcome.Failure failure(final String description) {
        return new Outcome.Failure(ErrorCode._401, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Verifies, optionally binds, and atomically replay-registers one received DPoP proof.
     *
     * @param compact      received compact proof
     * @param verification exact HTTP request and proof freshness policy
     * @param requirements replay isolation and optional access-token confirmation requirements
     * @param context      current immutable authentication context
     * @param timeout      shared end-to-end operation timeout
     * @return stage containing the accepted proof, expected rejection, or replay-cache failure
     */
    public CompletionStage<Outcome<DpopProof>> validate(
            final String compact,
            final DpopVerifier.Request verification,
            final Requirements requirements,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(verification, "DPoP verification request must not be null");
        Assert.notNull(requirements, "DPoP validation requirements must not be null");
        Assert.notNull(context, "DPoP validation context must not be null");
        Assert.notNull(timeout, "DPoP validation timeout must not be null");
        final DpopProof proof;
        try {
            proof = verifier.verify(compact, verification, timeout);
            requirements.confirmationThumbprint()
                    .ifPresent(expected -> compare(expected, proof.confirmationThumbprint()));
        } catch (ValidateException cause) {
            return completed(Outcome.rejected(failure("DPoP proof validation failed")));
        }
        final Instant expiresAt;
        try {
            expiresAt = proof.issuedAt().plus(verification.maximumAge());
        } catch (ArithmeticException cause) {
            return completed(Outcome.rejected(failure("DPoP proof lifetime is invalid")));
        }
        final String artifact = proof.confirmationThumbprint() + Symbol.C_COLON + proof.jwtId();
        return replayGuard.register(
                requirements.namespace(),
                requirements.protocol(),
                requirements.authority(),
                "dpop-proof",
                artifact,
                expiresAt,
                timeout).thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Void> ignored -> Outcome.succeeded(proof);
                    case Outcome.Rejected<Void> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Void> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Compares a token confirmation thumbprint against the proof key thumbprint in constant time.
     *
     * @param expected  token {@code cnf.jkt} value
     * @param presented proof-key RFC 7638 thumbprint
     */
    private void compare(final String expected, final String presented) {
        final char[] expectedCharacters = expected.toCharArray();
        final char[] presentedCharacters = presented.toCharArray();
        try {
            if (!secretGuard.matches(expectedCharacters, presentedCharacters)) {
                throw new ValidateException("DPoP public key does not match the access-token confirmation");
            }
        } finally {
            Arrays.fill(expectedCharacters, Symbol.C_NUL);
            Arrays.fill(presentedCharacters, Symbol.C_NUL);
        }
    }

    /**
     * Defines replay isolation and optional access-token proof-of-possession binding for one validation.
     *
     * @param namespace              external registration namespace
     * @param protocol               formal protocol owning the DPoP operation
     * @param authority              stable Provider or Source authority
     * @param confirmationThumbprint optional access-token {@code cnf.jkt} value
     * @author Kimi Liu
     */
    public record Requirements(String namespace, Protocol protocol, String authority,
            Optional<String> confirmationThumbprint) {

        /**
         * Validates replay isolation and token-confirmation values.
         *
         * @throws IllegalArgumentException if a component is {@code null} or a required string is blank
         */
        public Requirements {
            Assert.notBlank(namespace, "DPoP replay namespace must not be blank");
            Assert.notNull(protocol, "DPoP replay protocol must not be null");
            Assert.notBlank(authority, "DPoP replay authority must not be blank");
            Assert.notNull(confirmationThumbprint, "DPoP confirmation container must not be null");
            confirmationThumbprint
                    .ifPresent(value -> Assert.notBlank(value, "DPoP confirmation thumbprint must not be blank"));
        }

    }

}
