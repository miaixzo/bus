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
package org.miaixz.bus.auth.protocol.oidc;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.codec.json.JsonValues;
import org.miaixz.bus.auth.protocol.jwt.JWT.VerificationPolicy;
import org.miaixz.bus.auth.protocol.jwt.JWTPayload;
import org.miaixz.bus.auth.protocol.jwt.JWTVerifier;
import org.miaixz.bus.auth.protocol.jwt.KeyResolver;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.ProtocolError;
import org.miaixz.bus.auth.protocol.oidc.OIDC.Identity;
import org.miaixz.bus.auth.protocol.oidc.OIDC.RelyingPartyConfiguration;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

/**
 * Validates ID Tokens through the shared hardened JWT verifier and then applies OpenID Connect's exact subject,
 * authorized-party, and nonce rules. Signature, issuer, audience, expiration, issued-at, and replay checks remain in
 * the single JWT verification pipeline and use the product-selected algorithm and runtime ports.
 *
 * @author Kimi Liu
 */
public final class IdTokenValidator {

    /**
     * Subject claim name.
     */
    private static final String SUBJECT = "sub";

    /**
     * Authorized-party claim name.
     */
    private static final String AUTHORIZED_PARTY = "azp";

    /**
     * Nonce claim name.
     */
    private static final String NONCE = "nonce";

    /**
     * Trusted relying-party configuration.
     */
    private final RelyingPartyConfiguration configuration;

    /**
     * JSON provider used by the shared bounded JWT verifier.
     */
    private final JsonProvider json;

    /**
     * Trusted source of the current security time.
     */
    private final Clock clock;

    /**
     * Tenant-aware key resolver used by the shared JWT verifier.
     */
    private final KeyResolver keys;

    /**
     * Atomic replay store used by the shared JWT verifier.
     */
    private final StateStore states;

    /**
     * Closed parsing and allocation limits for the compact JWT.
     */
    private final Limits limits;

    /**
     * Creates one ID Token validator.
     *
     * @param configuration trusted relying-party configuration
     * @param json          non-null JSON provider
     * @param clock         non-null security clock
     * @param keys          non-null JWT verification-key resolver
     * @param states        non-null replay state store
     * @param limits        non-null parser and allocation limits
     * @throws ValidateException if any collaborator is {@code null}
     */
    public IdTokenValidator(final RelyingPartyConfiguration configuration, final JsonProvider json, final Clock clock,
            final KeyResolver keys, final StateStore states, final Limits limits) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("Relying-party configuration must not be null"));
        this.json = Assert.notNull(json, () -> new ValidateException("JSON provider must not be null"));
        this.clock = Assert.notNull(clock, () -> new ValidateException("Clock must not be null"));
        this.keys = Assert.notNull(keys, () -> new ValidateException("Key resolver must not be null"));
        this.states = Assert.notNull(states, () -> new ValidateException("State store must not be null"));
        this.limits = Assert.notNull(limits, () -> new ValidateException("Limits must not be null"));
    }

    /**
     * Creates the fixed invalid-request JSON member failure.
     *
     * @return new OAuth invalid-request protocol failure
     */
    private static RuntimeException invalidRequest() {
        return new ProtocolException(ProtocolError.INVALID_REQUEST);
    }

    /**
     * Produces a stable protocol rejection without exposing verification details.
     *
     * @param failure verification failure
     * @return stable protocol exception
     */
    private static ProtocolException invalid(final Throwable failure) {
        if (failure instanceof ProtocolException protocol
                && ProtocolError.INVALID_REQUEST.getKey().equals(protocol.getErrcode())) {
            return protocol;
        }
        return new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(), ProtocolError.INVALID_REQUEST.getValue(),
                failure);
    }

    /**
     * Returns a verified payload or maps the closed outcome to one stable rejection.
     *
     * @param outcome closed JWT verification outcome
     * @return successfully verified payload
     * @throws ProtocolException if verification did not produce a success value
     */
    private static JWTPayload success(final Outcome<JWTPayload> outcome) {
        if (outcome instanceof Outcome.Success<JWTPayload> success) {
            return success.value();
        }
        if (outcome instanceof Outcome.Failed<JWTPayload> failed) {
            throw invalid(failed.cause());
        }
        throw invalid(null);
    }

    /**
     * Validates one compact ID Token and exact authorization nonce.
     *
     * @param invocation operation context
     * @param token      compact ID Token
     * @param nonce      exact authorization nonce
     * @return stage containing verified identity
     * @throws IllegalArgumentException if the context, token, or nonce is absent
     */
    public CompletionStage<Identity> validate(final Context invocation, final String token, final String nonce) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final String compact = Assert.notBlank(token, "ID Token must be not blank!");
        final String expectedNonce = Assert.notBlank(nonce, "ID Token nonce must be not blank!");
        final VerificationPolicy policy = new VerificationPolicy(configuration.algorithm(), configuration.issuer(),
                Set.of(configuration.clientId()), configuration.skew(), configuration.maximumLifetime(), true);
        final CompletionStage<Outcome<JWTPayload>> verified;
        try {
            verified = JWTVerifier.verify(
                    compact,
                    policy,
                    context,
                    json,
                    clock,
                    keys,
                    states,
                    limits.maxJwtBytes(),
                    limits.maxHeaderBytes(),
                    limits.maxJsonBytes(),
                    limits.maxJsonDepth(),
                    limits.maxParameters());
        } catch (final RuntimeException failure) {
            return java.util.concurrent.CompletableFuture.failedFuture(invalid(failure));
        }
        return verified.thenApply(outcome -> identity(success(outcome), expectedNonce)).exceptionallyCompose(
                failure -> java.util.concurrent.CompletableFuture.failedFuture(invalid(ExceptionKit.unwrap(failure))));
    }

    /**
     * Applies OpenID Connect claims that are additional to the JWT profile.
     *
     * @param payload       verified JWT payload
     * @param expectedNonce exact authorization nonce
     * @return verified identity
     * @throws ProtocolException if a required OIDC claim is absent or violates exact binding rules
     */
    private Identity identity(final JWTPayload payload, final String expectedNonce) {
        final Map<String, Object> claims = payload.snapshot();
        final int maximum = limits.maxParameterBytes();
        final String subject = JsonValues.requiredText(claims, SUBJECT, maximum, IdTokenValidator::invalidRequest);
        final String nonce = JsonValues.requiredText(claims, NONCE, maximum, IdTokenValidator::invalidRequest);
        if (!expectedNonce.equals(nonce)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final Set<String> audiences = payload.audiences();
        if (!audiences.contains(configuration.clientId())) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final Object partyValue = claims.get(AUTHORIZED_PARTY);
        final String party = partyValue == null ? null
                : JsonValues.requiredText(claims, AUTHORIZED_PARTY, maximum, IdTokenValidator::invalidRequest);
        if (audiences.size() > 1 && party == null || party != null && !configuration.clientId().equals(party)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final String issuer = payload.issuer().orElseThrow(() -> new ProtocolException(ProtocolError.INVALID_REQUEST));
        if (!configuration.issuer().equals(issuer)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        return new Identity(subject, issuer, audiences, party, nonce, claims);
    }

}
