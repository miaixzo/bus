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
package org.miaixz.bus.auth.protocol.oidc.server;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Session;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.guard.TimeGuard;
import org.miaixz.bus.auth.protocol.oidc.EndSessionRequest;
import org.miaixz.bus.auth.protocol.oidc.IdToken;
import org.miaixz.bus.auth.protocol.oidc.IdTokenClaims;
import org.miaixz.bus.auth.protocol.oidc.codec.IdTokenCodec;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.resolver.KeyMaterial;
import org.miaixz.bus.auth.shared.jwt.JwtVerifier;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.KeyLoader;
import org.miaixz.bus.auth.worker.SessionCoordinator;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Validates an RP-Initiated Logout request and atomically ends its bound framework session.
 * <p>
 * ID Token hints are cryptographically verified with the Provider's exact configured key before any issuer, client,
 * session, or redirect value is trusted. The service returns no protocol response model; the endpoint codec alone
 * applies a previously validated redirect URI and optional state to the HTTP response.
 * </p>
 *
 * @author Kimi Liu
 */
public final class EndSessionService {

    /**
     * Standard signing-key use passed to the external key inventory.
     */
    private static final String SIGNATURE_USE = Builder.SIGNATURE;

    /**
     * Client metadata member registered by RP-Initiated Logout.
     */
    private static final String POST_LOGOUT_REDIRECT_URIS = "post_logout_redirect_uris";

    /**
     * Frozen issuer and signing options.
     */
    private final OpenIdServerOptions options;

    /**
     * External key, client, session, clock, and security dependencies.
     */
    private final DriverServices services;

    /**
     * JOSE-aware typed ID Token codec.
     */
    private final IdTokenCodec codec;

    /**
     * Source-isolated framework and project Session lifecycle coordinator.
     */
    private final SessionCoordinator sessions;

    /**
     * Shared clock-skew validator scoped to OIDC.
     */
    private final TimeGuard timeGuard;

    /**
     * Creates an end-session service for one compiled OpenID Provider.
     *
     * @param options  validated OpenID Provider options
     * @param services externally implemented runtime dependencies
     * @param codec    JOSE-aware typed ID Token codec
     * @param sessions Source-isolated Session lifecycle coordinator
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public EndSessionService(final OpenIdServerOptions options, final DriverServices services, final IdTokenCodec codec,
            final SessionCoordinator sessions) {
        this.options = Assert.notNull(options, "OpenID Connect end-session options must not be null");
        this.services = Assert.notNull(services, "OpenID Connect end-session execution services must not be null");
        this.codec = Assert.notNull(codec, "OpenID Connect end-session ID Token codec must not be null");
        this.sessions = Assert.notNull(sessions, "OpenID Connect Session coordinator must not be null");
        this.timeGuard = services.securityBaseline().timeGuard(Protocol.OIDC, services.fabricContext().clock());
    }

    /**
     * Determines the exact relying-party client bound by verified audience and authorized-party claims.
     *
     * @param claims verified ID Token claims
     * @return exact client identifier
     * @throws ValidateException if audience and authorized-party claims are ambiguous or inconsistent
     */
    private static String hintClientId(final IdTokenClaims claims) {
        final String authorizedParty = claims.authorizedParty().getOrNull();
        if (claims.audience().size() == 1) {
            final String audience = claims.audience().get(0);
            if (authorizedParty != null && !audience.equals(authorizedParty)) {
                throw new ValidateException("OpenID Connect single-audience hint has an inconsistent azp");
            }
            return audience;
        }
        if (authorizedParty == null || !claims.audience().contains(authorizedParty)) {
            throw new ValidateException("OpenID Connect multi-audience hint requires an audience-bound azp");
        }
        return authorizedParty;
    }

    /**
     * Validates exact client identity and its registered post-logout redirect URI array.
     *
     * @param client            externally resolved client registration
     * @param expectedClientId  verified client identifier
     * @param requestedRedirect exact requested redirect URI
     * @return whether registration and redirect bindings are valid
     */
    private static boolean validRedirectClient(
            final ConsumerMetadata client,
            final String expectedClientId,
            final String requestedRedirect) {
        if (client == null || !expectedClientId.equals(client.id())) {
            return false;
        }
        final JsonValue value = client.metadata().values().get(POST_LOGOUT_REDIRECT_URIS);
        if (!(value instanceof JsonValue.ArrayValue array)) {
            return false;
        }
        final Set<String> registered = new HashSet<>(array.values().size());
        for (JsonValue element : array.values()) {
            if (!(element instanceof JsonValue.StringValue string) || string.value().isBlank()
                    || !registered.add(string.value())) {
                return false;
            }
        }
        return registered.contains(requestedRedirect);
    }

    /**
     * Creates a safe end-session failure without hint, token, redirect, or loader details.
     *
     * @param error       shared Bus error definition
     * @param description non-sensitive diagnostic description
     * @return immutable safe failure
     */
    private static Outcome.Failure failure(final Errors error, final String description) {
        return new Outcome.Failure(error, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates an already completed outcome stage.
     *
     * @param <T>     success value type
     * @param outcome completed outcome
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Validates the complete logout binding and atomically transitions the target session to {@code ENDED}.
     *
     * @param request standard RP-Initiated Logout request
     * @param context immutable invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a successful void outcome or a closed failure
     */
    public CompletionStage<Outcome<Void>> endSession(
            final EndSessionRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "OpenID Connect end-session request must not be null");
        Assert.notNull(context, "OpenID Connect end-session context must not be null");
        Assert.notNull(timeout, "OpenID Connect end-session time budget must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    "OpenID Connect end-session request has no remaining time budget")));
        }
        final String hint = request.idTokenHint().getOrNull();
        if (hint == null) {
            return continueAfterHint(request, context, timeout, Hint.absent());
        }
        return verifyHint(hint, request.clientId().getOrNull(), context, timeout)
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Hint> success -> continueAfterHint(
                            request,
                            context,
                            timeout,
                            success.value());
                    case Outcome.Rejected<Hint> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<Hint> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Resolves the exact signing key and verifies the supplied ID Token hint.
     *
     * @param compact           sensitive ID Token hint
     * @param requestedClientId optional request client identifier
     * @param context           immutable invocation context
     * @param timeout           shared operation budget
     * @return asynchronously verified hint binding
     */
    private CompletionStage<Outcome<Hint>> verifyHint(
            final String compact,
            final String requestedClientId,
            final Context context,
            final Timeout.Budget timeout) {
        final Instant now = timeout.clock().now();
        final KeyLoader.Request query = new KeyLoader.Request(options.issuer(),
                Optional.of(options.idTokenSigningKeyId()), SIGNATURE_USE, options.idTokenSigningAlgorithm().name(),
                now);
        final CompletionStage<Outcome<KeyMaterial>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.keyLoader().load(services.registration(), query, context, timeout),
                    loaded -> services.keyParser().parse(services.registration(), query, loaded));
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(
                            failure(ErrorCode._500, "OpenID Connect end-session signing key resolution failed")));
        }
        return resolution.handle(
                (outcome, thrown) -> thrown == null && outcome != null ? outcome
                        : Outcome.<KeyMaterial>failed(
                                failure(ErrorCode._500, "OpenID Connect end-session signing key resolution failed")))
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<KeyMaterial> success -> decodeHint(
                            compact,
                            requestedClientId,
                            success.value(),
                            timeout);
                    case Outcome.Rejected<KeyMaterial> rejected -> Outcome
                            .rejected(failure(ErrorCode._400, "OpenID Connect ID Token hint cannot be verified"));
                    case Outcome.Failed<KeyMaterial> failed -> Outcome.failed(
                            failure(
                                    failed.failure().error(),
                                    "OpenID Connect end-session signing key resolution failed"));
                });
    }

    /**
     * Verifies JOSE protection and every logout-relevant ID Token claim binding.
     *
     * @param compact           sensitive ID Token hint
     * @param requestedClientId optional request client identifier
     * @param resolvedKey       exact configured signing key
     * @param timeout           shared operation budget
     * @return verified hint outcome
     */
    private Outcome<Hint> decodeHint(
            final String compact,
            final String requestedClientId,
            final KeyMaterial resolvedKey,
            final Timeout.Budget timeout) {
        try {
            final Instant now = timeout.clock().now();
            if (resolvedKey == null || !options.idTokenSigningKeyId().equals(resolvedKey.keyId())
                    || !options.idTokenSigningAlgorithm().name().equals(resolvedKey.algorithm())
                    || now.isBefore(resolvedKey.notBefore()) || !now.isBefore(resolvedKey.notAfter())) {
                throw new ValidateException("OpenID Connect end-session key does not match Provider options");
            }
            final IdTokenCodec.Decoded decoded = codec
                    .decode(new IdToken(compact), new JwtVerifier.Signed(resolvedKey.key(), Set.of()));
            if (!options.idTokenSigningAlgorithm().name().equals(decoded.jwt().header().algorithm())
                    || !decoded.jwt().header().keyId().filter(options.idTokenSigningKeyId()::equals).isPresent()) {
                throw new ValidateException("OpenID Connect ID Token hint header does not match Provider options");
            }
            final IdTokenClaims claims = decoded.claims();
            if (!options.issuer().equals(claims.issuer())) {
                throw new ValidateException("OpenID Connect ID Token hint issuer does not match Provider options");
            }
            timeGuard.validateIssuedAt(claims.issuedAt(), timeout);
            timeGuard.validateExpiration(claims.expiration(), timeout);
            if (!claims.issuedAt().isBefore(claims.expiration())) {
                throw new ValidateException("OpenID Connect ID Token hint has an invalid validity interval");
            }
            final String clientId = hintClientId(claims);
            if (requestedClientId != null && !requestedClientId.equals(clientId)) {
                throw new ValidateException("OpenID Connect logout client_id does not match the ID Token hint");
            }
            return Outcome.succeeded(
                    new Hint(Optional.of(clientId), Optional.ofNullable(claims.sessionId().getOrNull()),
                            Optional.of(claims.subject())));
        } catch (ValidateException exception) {
            return Outcome.rejected(failure(ErrorCode._400, "OpenID Connect ID Token hint validation failed"));
        } catch (RuntimeException exception) {
            return Outcome
                    .failed(failure(ErrorCode._500, "OpenID Connect ID Token hint verification could not complete"));
        }
    }

    /**
     * Validates optional redirection against a verified client and then selects the target session.
     *
     * @param request validated protocol request
     * @param context immutable invocation context
     * @param timeout shared operation budget
     * @param hint    verified or absent ID Token hint facts
     * @return asynchronously completed logout outcome
     */
    private CompletionStage<Outcome<Void>> continueAfterHint(
            final EndSessionRequest request,
            final Context context,
            final Timeout.Budget timeout,
            final Hint hint) {
        final String explicitClient = request.clientId().getOrNull();
        final String clientId = hint.clientId().getOrNull() == null ? explicitClient : hint.clientId().getOrNull();
        if (request.postLogoutRedirectUri().isEmpty()) {
            return endSelectedSession(hint, context, timeout);
        }
        if (clientId == null) {
            return completed(
                    Outcome.rejected(
                            failure(ErrorCode._400, "OpenID Connect post-logout redirect has no verified client")));
        }
        final CompletionStage<Outcome<ConsumerMetadata>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.consumerLoader().load(services.registration(), clientId, context, timeout),
                    loaded -> services.consumerParser().parse(services.registration(), clientId, loaded));
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure(ErrorCode._500, "OpenID Connect logout client resolution failed")));
        }
        return resolution
                .handle(
                        (outcome, thrown) -> thrown == null && outcome != null ? outcome
                                : Outcome.<ConsumerMetadata>failed(
                                        failure(ErrorCode._500, "OpenID Connect logout client resolution failed")))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<ConsumerMetadata> success -> {
                        if (!validRedirectClient(
                                success.value(),
                                clientId,
                                request.postLogoutRedirectUri().getOrNull())) {
                            yield completed(
                                    Outcome.rejected(
                                            failure(
                                                    ErrorCode._400,
                                                    "OpenID Connect post-logout redirect is not registered")));
                        }
                        yield endSelectedSession(hint, context, timeout);
                    }
                    case Outcome.Rejected<ConsumerMetadata> rejected -> completed(
                            Outcome.rejected(failure(ErrorCode._400, "OpenID Connect logout client is unavailable")));
                    case Outcome.Failed<ConsumerMetadata> failed -> completed(
                            Outcome.failed(
                                    failure(
                                            failed.failure().error(),
                                            "OpenID Connect logout client resolution failed")));
                });
    }

    /**
     * Selects a verified hint session before the trusted current-context session.
     *
     * @param hint    verified or absent hint facts
     * @param context immutable invocation context
     * @param timeout shared operation budget
     * @return asynchronously completed session transition outcome
     */
    private CompletionStage<Outcome<Void>> endSelectedSession(
            final Hint hint,
            final Context context,
            final Timeout.Budget timeout) {
        String sessionId = hint.sessionId().getOrNull();
        if (sessionId == null && context.authentication().isPresent()) {
            sessionId = context.authentication().getOrNull().session().key().value();
        }
        if (sessionId == null) {
            return completed(
                    Outcome.rejected(failure(ErrorCode._400, "OpenID Connect logout has no verified session binding")));
        }
        return sessions.end(new Session.Key(sessionId), context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<SessionCoordinator.End> ignored -> Outcome.succeeded(null);
            case Outcome.Rejected<SessionCoordinator.End> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<SessionCoordinator.End> failed -> Outcome.failed(failed.failure());
        });
    }

    /**
     * Carries only facts extracted from a cryptographically verified ID Token hint.
     *
     * @param clientId  verified relying-party client identifier
     * @param sessionId optional verified {@code sid} claim
     * @param subjectId optional verified {@code sub} claim retained for audit correlation only
     * @author Kimi Liu
     */
    private record Hint(Optional<String> clientId, Optional<String> sessionId, Optional<String> subjectId) {

        /**
         * Creates an immutable normalized verified hint view.
         *
         * @throws IllegalArgumentException if an optional container is {@code null}
         */
        private Hint {
            Assert.notNull(clientId, "Verified logout client container must not be null");
            Assert.notNull(sessionId, "Verified logout session container must not be null");
            Assert.notNull(subjectId, "Verified logout subject container must not be null");
            clientId = Optional.ofNullable(clientId.getOrNull());
            sessionId = Optional.ofNullable(sessionId.getOrNull());
            subjectId = Optional.ofNullable(subjectId.getOrNull());
        }

        /**
         * Returns an explicit absence marker when no ID Token hint was supplied.
         *
         * @return empty hint facts
         */
        private static Hint absent() {
            return new Hint(Optional.empty(), Optional.empty(), Optional.empty());
        }

    }

}
