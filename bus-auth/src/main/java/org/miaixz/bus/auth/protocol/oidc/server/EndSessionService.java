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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Session;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.cache.IdTokenCache;
import org.miaixz.bus.auth.protocol.oidc.EndSessionRequest;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.SessionCoordinator;
import org.miaixz.bus.auth.worker.loader.ConsumerLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Validates an RP-Initiated Logout request and atomically ends its bound framework session.
 * <p>
 * ID Token hints are matched only against the irreversible index created after successful issuance. This supports both
 * signed and encrypted ID Tokens without decoding caller-controlled compact content in the logout path. The service
 * returns no protocol response model; the endpoint codec alone applies a previously validated redirect URI and optional
 * state to the HTTP response.
 * </p>
 *
 * @author Kimi Liu
 */
public class EndSessionService {

    /**
     * External key, client, session, clock, and security dependencies.
     */
    private final DriverServices services;

    /**
     * Compiled Source identifier used to isolate irreversible ID Token indexes.
     */
    private final String sourceId;

    /**
     * Source-isolated framework and project Session lifecycle coordinator.
     */
    private final SessionCoordinator sessions;

    /**
     * Creates an end-session service for one compiled OpenID Provider.
     *
     * @param options  validated OpenID Provider options
     * @param services externally implemented runtime dependencies
     * @param sessions Source-isolated Session lifecycle coordinator
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public EndSessionService(final OpenIdServerOptions options, final DriverServices services,
            final SessionCoordinator sessions) {
        Assert.notNull(options, "OpenID Connect end-session options must not be null");
        this.services = Assert.notNull(services, "OpenID Connect end-session execution services must not be null");
        this.sourceId = services.registration().resource().getId();
        this.sessions = Assert.notNull(sessions, "OpenID Connect Session coordinator must not be null");
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
        return client.postLogoutRedirectUris().contains(requestedRedirect);
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
     * @param timeout shared end-to-end operation timeout
     * @return stage containing a successful void outcome or a closed failure
     */
    public CompletionStage<Outcome<Void>> endSession(
            final EndSessionRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OpenID Connect end-session request must not be null");
        Assert.notNull(context, "OpenID Connect end-session context must not be null");
        Assert.notNull(timeout, "OpenID Connect end-session timeout must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(ErrorCode._408, "OpenID Connect end-session request has no remaining timeout")));
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
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Resolves a supplied ID Token hint from the irreversible issued-token index.
     *
     * @param compact           sensitive ID Token hint
     * @param requestedClientId optional request client identifier
     * @param context           immutable invocation context
     * @param timeout           shared operation timeout
     * @return asynchronously verified hint binding
     */
    private CompletionStage<Outcome<Hint>> verifyHint(
            final String compact,
            final String requestedClientId,
            final Context context,
            final Timeout timeout) {
        final String digest = IdTokenCache.key(sourceId, compact);
        final CompletionStage<ExpiringValue<IdTokenCache.Entry>> resolution;
        try {
            resolution = services.idTokenCache().find(digest);
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure(ErrorCode._500, "OpenID Connect ID Token hint lookup failed")));
        }
        return resolution.handle((stored, thrown) -> {
            if (thrown != null) {
                return Outcome.<Hint>failed(failure(ErrorCode._500, "OpenID Connect ID Token hint lookup failed"));
            }
            final Instant now = timeout.clock().now();
            if (stored == null || stored.value() == null || !stored.expiresAt().isAfter(now)) {
                return Outcome.<Hint>rejected(failure(ErrorCode._400, "OpenID Connect ID Token hint is unavailable"));
            }
            final IdTokenCache.Entry entry = stored.value();
            if (!sourceId.equals(entry.sourceId())
                    || requestedClientId != null && !requestedClientId.equals(entry.consumerId())) {
                return Outcome
                        .<Hint>rejected(failure(ErrorCode._400, "OpenID Connect ID Token hint binding is invalid"));
            }
            return Outcome.succeeded(
                    new Hint(Optional.of(entry.consumerId()), entry.sessionId(), Optional.of(entry.subject()),
                            Optional.of(digest)));
        });
    }

    /**
     * Validates optional redirection against a verified client and then selects the target session.
     *
     * @param request validated protocol request
     * @param context immutable invocation context
     * @param timeout shared operation timeout
     * @param hint    verified or absent ID Token hint facts
     * @return asynchronously completed logout outcome
     */
    private CompletionStage<Outcome<Void>> continueAfterHint(
            final EndSessionRequest request,
            final Context context,
            final Timeout timeout,
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
                    () -> services.consumerLoader()
                            .load(new ConsumerLoader.Request(services.registration(), clientId), context, timeout),
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
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Selects a verified hint session before the trusted current-context session.
     *
     * @param hint    verified or absent hint facts
     * @param context immutable invocation context
     * @param timeout shared operation timeout
     * @return asynchronously completed session transition outcome
     */
    private CompletionStage<Outcome<Void>> endSelectedSession(
            final Hint hint,
            final Context context,
            final Timeout timeout) {
        String sessionId = hint.sessionId().getOrNull();
        if (sessionId == null && context.authentication().isPresent()) {
            sessionId = context.authentication().getOrNull().session().key().value();
        }
        if (sessionId == null) {
            return completed(
                    Outcome.rejected(failure(ErrorCode._400, "OpenID Connect logout has no verified session binding")));
        }
        return sessions.end(new Session.Key(sessionId), context, timeout).thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<SessionCoordinator.End> ignored -> revokeHint(hint);
            case Outcome.Rejected<SessionCoordinator.End> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<SessionCoordinator.End> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Revokes a consumed ID Token hint after the project session ended successfully.
     *
     * @param hint verified hint binding
     * @return successful void outcome when no hint exists or the indexed hint was removed
     */
    private CompletionStage<Outcome<Void>> revokeHint(final Hint hint) {
        final String digest = hint.digest().getOrNull();
        if (digest == null) {
            return completed(Outcome.succeeded(null));
        }
        try {
            return services.idTokenCache().revoke(digest).handle(
                    (revoked, thrown) -> thrown == null && Boolean.TRUE.equals(revoked) ? Outcome.<Void>succeeded(null)
                            : Outcome.<Void>failed(
                                    failure(ErrorCode._500, "OpenID Connect ID Token hint revocation failed")));
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure(ErrorCode._500, "OpenID Connect ID Token hint revocation failed")));
        }
    }

    /**
     * Carries only facts extracted from a cryptographically verified ID Token hint.
     *
     * @param clientId  verified relying-party client identifier
     * @param sessionId optional verified {@code sid} claim
     * @param subjectId optional verified {@code sub} claim retained for audit correlation only
     * @param digest    optional irreversible ID Token binding digest
     * @author Kimi Liu
     */
    private record Hint(Optional<String> clientId, Optional<String> sessionId, Optional<String> subjectId,
            Optional<String> digest) {

        /**
         * Creates an immutable normalized verified hint view.
         *
         * @throws IllegalArgumentException if an optional container is {@code null}
         */
        private Hint {
            Assert.notNull(clientId, "Verified logout client container must not be null");
            Assert.notNull(sessionId, "Verified logout session container must not be null");
            Assert.notNull(subjectId, "Verified logout subject container must not be null");
            Assert.notNull(digest, "Verified logout digest container must not be null");
            clientId = Optional.ofNullable(clientId.getOrNull());
            sessionId = Optional.ofNullable(sessionId.getOrNull());
            subjectId = Optional.ofNullable(subjectId.getOrNull());
            digest = Optional.ofNullable(digest.getOrNull());
        }

        /**
         * Returns an explicit absence marker when no ID Token hint was supplied.
         *
         * @return empty hint facts
         */
        private static Hint absent() {
            return new Hint(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

    }

}
