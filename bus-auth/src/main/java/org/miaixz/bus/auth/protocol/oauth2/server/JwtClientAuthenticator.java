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
package org.miaixz.bus.auth.protocol.oauth2.server;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.FabricX;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.guard.ClientAuthentication;
import org.miaixz.bus.auth.guard.ReplayGuard;
import org.miaixz.bus.auth.guard.TimeGuard;
import org.miaixz.bus.auth.protocol.oauth2.ClientAuthenticationMethod;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.resolver.FederationMetadata;
import org.miaixz.bus.auth.resolver.KeyMaterial;
import org.miaixz.bus.auth.shared.SecurityBaseline;
import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.auth.shared.jose.JwsService;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.loader.FederationLoader;
import org.miaixz.bus.auth.worker.loader.KeyLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Verifies standard private-key JWT client assertions and explicitly enabled federated machine assertions.
 * <p>
 * The standard and federation profiles are selected once from verified registration coordinates and never used as
 * fallback paths. Consumer, federation, and key records remain project-owned Worker data; this service only parses,
 * verifies, validates replay, and returns request-scoped authentication facts.
 * </p>
 *
 * @author Kimi Liu
 */
public class JwtClientAuthenticator {

    private static final String SIGNATURE_USE = "sig";
    private static final String ASSERTION_PURPOSE = "client-assertion";

    private final OAuth2ServerOptions options;
    private final DriverServices services;
    private final String sourceId;
    private final String tokenAudience;
    private final SecurityBaseline.Policy policy;
    private final TimeGuard timeGuard;
    private final ReplayGuard replayGuard;
    private final JwsService jwsService;

    /**
     * Creates one Source-scoped JWT client assertion verifier.
     *
     * @param options  frozen OAuth Provider options
     * @param services dependency-scoped runtime services
     */
    public JwtClientAuthenticator(final OAuth2ServerOptions options, final DriverServices services) {
        this.options = Assert.notNull(options, "OAuth 2.x JWT client options must not be null");
        this.services = Assert.notNull(services, "OAuth 2.x JWT client services must not be null");
        this.sourceId = services.registration().resource().getId();
        this.tokenAudience = options.tokenEndpoint()
                .orElseThrow(
                        () -> new ValidateException("OAuth 2.x JWT client authentication requires a token endpoint"))
                .url().toString();
        this.policy = services.securityBaseline().require(Protocol.OAUTH2);
        this.timeGuard = services.securityBaseline().timeGuard(Protocol.OAUTH2, FabricX.clock(services.fabric()));
        this.replayGuard = services.securityBaseline().replayGuard(services.replayCache());
        this.jwsService = new JwsService(services.jsonProvider(), services.securityBaseline().algorithmGuard(),
                policy.algorithms());
    }

    /**
     * Verifies one client assertion under exactly one selected trust profile.
     *
     * @param consumer       already parsed immutable consumer snapshot
     * @param requestedGrant requested token grant
     * @param compact        three-segment compact assertion
     * @param context        immutable invocation context
     * @param timeout        shared operation timeout
     * @return verified standard or federated client authentication
     */
    public CompletionStage<Outcome<ClientAuthentication>> authenticate(
            final ConsumerMetadata consumer,
            final GrantType requestedGrant,
            final String compact,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(consumer, "OAuth 2.x JWT consumer must not be null");
        Assert.notNull(requestedGrant, "OAuth 2.x JWT requested grant must not be null");
        Assert.notNull(context, "OAuth 2.x JWT client context must not be null");
        Assert.notNull(timeout, "OAuth 2.x JWT client timeout must not be null");
        final Assertion assertion;
        try {
            assertion = parse(compact, timeout);
        } catch (RuntimeException exception) {
            return completed(Outcome.rejected(failure(false, "OAuth 2.x client assertion is invalid")));
        }
        final boolean standard = consumer.id().equals(assertion.issuer()) && consumer.id().equals(assertion.subject())
                && consumer.clientAssertionKeyId().isPresent();
        if (standard) {
            if (!options.tokenEndpointAuthMethodsSupported().contains(ClientAuthenticationMethod.PRIVATE_KEY_JWT)
                    || !consumer.authenticationMethods().contains(ClientAuthenticationMethod.PRIVATE_KEY_JWT)) {
                return completed(Outcome.rejected(failure(false, "OAuth 2.x private-key JWT is not registered")));
            }
            final String configuredKey = consumer.clientAssertionKeyId().getOrNull();
            if (assertion.keyId() == null || !configuredKey.equals(assertion.keyId())) {
                return completed(Outcome.rejected(failure(false, "OAuth 2.x client assertion key is invalid")));
            }
            return resolveKey(consumer, assertion, configuredKey, null, context, timeout);
        }
        if (!options.federatedJwtEnabled() || !GrantType.CLIENT_CREDENTIALS.equals(requestedGrant)) {
            return completed(Outcome.rejected(failure(false, "OAuth 2.x federated client assertion is disabled")));
        }
        if (assertion.keyId() == null) {
            return completed(Outcome.rejected(failure(false, "OAuth 2.x federated assertion requires a key id")));
        }
        return resolveFederation(consumer, assertion, context, timeout);
    }

    private Assertion parse(final String compact, final Timeout timeout) {
        Assert.notBlank(compact, "OAuth 2.x client assertion must not be blank");
        if (compact.getBytes(Charset.UTF_8).length > policy.maximumMessageBytes()) {
            throw new ValidateException("OAuth 2.x client assertion exceeds the configured size limit");
        }
        final JwsService.Jws parsed = jwsService.parseCompact(compact, Set.of());
        final JwsService.Signature signature = parsed.signatures().getFirst();
        final JoseHeader header = signature.header();
        if (!policy.algorithms().contains(header.algorithm())) {
            throw new ValidateException("OAuth 2.x client assertion algorithm is not allowed");
        }
        final JsonValue decoded = services.jsonProvider().readValue(parsed.payload());
        if (!(decoded instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("OAuth 2.x client assertion claims must be an object");
        }
        final JwtClaims claims = new JwtClaims(object);
        final String issuer = claims.issuer()
                .orElseThrow(() -> new ValidateException("OAuth 2.x client assertion requires iss"));
        final String subject = claims.subject()
                .orElseThrow(() -> new ValidateException("OAuth 2.x client assertion requires sub"));
        final Instant issuedAt = claims.issuedAt()
                .orElseThrow(() -> new ValidateException("OAuth 2.x client assertion requires iat"));
        final Instant expiresAt = claims.expiration()
                .orElseThrow(() -> new ValidateException("OAuth 2.x client assertion requires exp"));
        final String jwtId = claims.jwtId()
                .orElseThrow(() -> new ValidateException("OAuth 2.x client assertion requires jti"));
        if (!claims.audiences().contains(tokenAudience)) {
            throw new ValidateException("OAuth 2.x client assertion audience does not contain the token endpoint");
        }
        timeGuard.validateWindow(issuedAt, claims.notBefore(), expiresAt, timeout);
        return new Assertion(parsed, header.algorithm(), header.keyId().getOrNull(), claims, issuer, subject, jwtId,
                expiresAt);
    }

    private CompletionStage<Outcome<ClientAuthentication>> resolveFederation(
            final ConsumerMetadata consumer,
            final Assertion assertion,
            final Context context,
            final Timeout timeout) {
        final CompletionStage<Outcome<FederationLoader.Record>> loading;
        try {
            loading = services.federationLoader().load(
                    new FederationLoader.Request(services.registration(), consumer.id(), assertion.issuer(),
                            assertion.subject()),
                    context,
                    timeout);
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure(true, "OAuth 2.x federation loading failed")));
        }
        return loading.handle((outcome, thrown) -> thrown == null ? outcome : null).thenCompose(outcome -> {
            if (!(outcome instanceof Outcome.Succeeded<FederationLoader.Record> success)) {
                return completed(
                        outcome instanceof Outcome.Failed<FederationLoader.Record>
                                ? Outcome.failed(failure(true, "OAuth 2.x federation loading failed"))
                                : Outcome.rejected(failure(false, "OAuth 2.x federation relation is unavailable")));
            }
            final FederationMetadata federation;
            try {
                federation = services.federationParser().parse(
                        services.registration(),
                        consumer.id(),
                        assertion.issuer(),
                        assertion.subject(),
                        success.value());
            } catch (RuntimeException exception) {
                return completed(Outcome.failed(failure(true, "OAuth 2.x federation data is invalid")));
            }
            return resolveKey(consumer, assertion, assertion.keyId(), federation, context, timeout);
        });
    }

    private CompletionStage<Outcome<ClientAuthentication>> resolveKey(
            final ConsumerMetadata consumer,
            final Assertion assertion,
            final String keyId,
            final FederationMetadata federation,
            final Context context,
            final Timeout timeout) {
        final String issuer = federation == null ? consumer.id() : assertion.issuer();
        final KeyLoader.Request request = new KeyLoader.Request(services.registration(), issuer, Optional.of(keyId),
                SIGNATURE_USE, assertion.algorithm(), timeout.clock().now());
        final CompletionStage<Outcome<KeyLoader.Record>> loading;
        try {
            loading = services.keyLoader().load(request, context, timeout);
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure(true, "OAuth 2.x assertion key loading failed")));
        }
        return loading.handle((outcome, thrown) -> thrown == null ? outcome : null).thenCompose(outcome -> {
            if (!(outcome instanceof Outcome.Succeeded<KeyLoader.Record> success)) {
                return completed(
                        outcome instanceof Outcome.Failed<KeyLoader.Record>
                                ? Outcome.failed(failure(true, "OAuth 2.x assertion key loading failed"))
                                : Outcome.rejected(failure(false, "OAuth 2.x assertion key is unavailable")));
            }
            final KeyMaterial material;
            try {
                material = services.keyParser().parse(services.registration(), request, success.value());
                final JwsService.Signature signature = assertion.jws().signatures().getFirst();
                jwsService.verify(signature, assertion.jws().payload(), material.key(), Set.of());
            } catch (RuntimeException exception) {
                return completed(Outcome.rejected(failure(false, "OAuth 2.x client assertion signature is invalid")));
            }
            return registerReplay(consumer, assertion, federation, timeout);
        });
    }

    private CompletionStage<Outcome<ClientAuthentication>> registerReplay(
            final ConsumerMetadata consumer,
            final Assertion assertion,
            final FederationMetadata federation,
            final Timeout timeout) {
        final Instant minimum = timeout.clock().now().plus(policy.minimumReplayWindow());
        final Instant replayExpiry = assertion.expiresAt().isAfter(minimum) ? assertion.expiresAt() : minimum;
        final String authority = sourceId + Symbol.C_NUL + consumer.id() + Symbol.C_NUL + assertion.issuer();
        return replayGuard.register(
                sourceId,
                Protocol.OAUTH2,
                authority,
                ASSERTION_PURPOSE,
                assertion.jwtId(),
                replayExpiry,
                timeout).thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Void> ignored -> federation == null
                            ? Outcome.succeeded(
                                    ClientAuthentication.standard(consumer, ClientAuthenticationMethod.PRIVATE_KEY_JWT))
                            : Outcome.succeeded(
                                    ClientAuthentication.federated(
                                            consumer,
                                            federation.issuer(),
                                            federation.externalSubject(),
                                            federation.subject(),
                                            assertion.claims().values()));
                    case Outcome.Rejected<Void> rejected -> Outcome
                            .rejected(failure(false, "OAuth 2.x client assertion was already used"));
                    case Outcome.Failed<Void> failed -> Outcome
                            .failed(failure(true, "OAuth 2.x client assertion replay registration failed"));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    private static Outcome.Failure failure(final boolean operational, final String description) {
        return new Outcome.Failure(operational ? ErrorCode._500 : ErrorCode._401, description,
                new JsonValue.ObjectValue(Map.of()));
    }

    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Retains bounded parsed assertion facts until exact-key verification completes.
     *
     * @author Kimi Liu
     */
    private record Assertion(JwsService.Jws jws, String algorithm, String keyId, JwtClaims claims, String issuer,
            String subject, String jwtId, Instant expiresAt) {

    }

}
