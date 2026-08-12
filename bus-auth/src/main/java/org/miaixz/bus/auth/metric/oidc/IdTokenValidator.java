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
package org.miaixz.bus.auth.metric.oidc;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.JWT.VerificationPolicy;
import org.miaixz.bus.auth.metric.OAuth2.ProtocolError;
import org.miaixz.bus.auth.metric.OIDC.Identity;
import org.miaixz.bus.auth.metric.OIDC.RelyingPartyConfiguration;
import org.miaixz.bus.auth.metric.jwt.JWTPayload;
import org.miaixz.bus.auth.metric.jwt.JWTVerifier;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

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
     * Authentication runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one ID Token validator.
     *
     * @param configuration trusted relying-party configuration
     * @param runtime       authentication runtime
     */
    public IdTokenValidator(final RelyingPartyConfiguration configuration, final Runtime runtime) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("Relying-party configuration must not be null"));
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("Authentication runtime must not be null"));
    }

    /**
     * Reads one exact non-blank string claim.
     *
     * @param claims verified claims
     * @param name   claim name
     * @return exact claim value
     */
    private static String text(final Map<String, Object> claims, final String name) {
        final Object value = claims.get(name);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        return text;
    }

    /**
     * Unwraps one asynchronous failure.
     *
     * @param failure asynchronous failure
     * @return root cause
     */
    private static Throwable cause(final Throwable failure) {
        return failure instanceof CompletionException && failure.getCause() != null ? failure.getCause() : failure;
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
     * Validates one compact ID Token and exact authorization nonce.
     *
     * @param invocation operation context
     * @param token      compact ID Token
     * @param nonce      exact authorization nonce
     * @return stage containing verified identity
     */
    public CompletionStage<Identity> validate(final Invocation invocation, final String token, final String nonce) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final String compact = Assert.notBlank(token, "ID Token must be not blank!");
        final String expectedNonce = Assert.notBlank(nonce, "ID Token nonce must be not blank!");
        final VerificationPolicy policy = new VerificationPolicy(configuration.algorithm(), configuration.issuer(),
                Set.of(configuration.clientId()), configuration.skew(), configuration.maximumLifetime(), true);
        final CompletionStage<JWTPayload> verified;
        try {
            verified = JWTVerifier.verify(compact, policy, context, runtime);
        } catch (final RuntimeException failure) {
            return java.util.concurrent.CompletableFuture.failedFuture(invalid(failure));
        }
        return verified.thenApply(payload -> identity(payload, expectedNonce)).exceptionallyCompose(
                failure -> java.util.concurrent.CompletableFuture.failedFuture(invalid(cause(failure))));
    }

    /**
     * Applies OpenID Connect claims that are additional to the JWT profile.
     *
     * @param payload       verified JWT payload
     * @param expectedNonce exact authorization nonce
     * @return verified identity
     */
    private Identity identity(final JWTPayload payload, final String expectedNonce) {
        final Map<String, Object> claims = payload.claims();
        final String subject = text(claims, SUBJECT);
        final String nonce = text(claims, NONCE);
        if (!expectedNonce.equals(nonce)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final Set<String> audiences = payload.audiences();
        if (!audiences.contains(configuration.clientId())) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final Object partyValue = claims.get(AUTHORIZED_PARTY);
        final String party = partyValue == null ? null : text(claims, AUTHORIZED_PARTY);
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
