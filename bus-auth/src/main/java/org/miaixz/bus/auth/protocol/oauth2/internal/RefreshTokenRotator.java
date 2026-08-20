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
package org.miaixz.bus.auth.protocol.oauth2.internal;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.cache.RefreshTokenStore;
import org.miaixz.bus.auth.guard.ScopeValidator;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.server.OAuth2ProviderSettings;
import org.miaixz.bus.auth.resolver.ClientResolver;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Rotates OAuth refresh-token families with reuse detection and fixed absolute family expiration.
 * <p>
 * A ReplayStore family marker closes the lookup gap created by token-digest indexing: once any rotated generation is
 * reused, every later active generation observes the marker and is rejected. New access and refresh values remain
 * unreturned until the old active generation is atomically replaced with its rotated state.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RefreshTokenRotator {

    /**
     * Maximum create-if-absent attempts for a generated refresh-token digest collision.
     */
    private static final int MAXIMUM_CREATE_ATTEMPTS = 3;

    /**
     * Domain-separation label used in the irreversible refresh-family replay key.
     */
    private static final String FAMILY_KEY_PURPOSE = "refresh-family";

    /**
     * Non-sensitive ReplayStore value identifying a compromised refresh-token family.
     */
    private static final String FAMILY_REUSED = "oauth2-refresh-family-reused";

    /**
     * Safe failure detail member carrying a registered OAuth error code.
     */
    private static final String OAUTH_ERROR = "oauth_error";

    /**
     * Provider identifier used in token and family key isolation.
     */
    private final String providerId;

    /**
     * Frozen Provider grant and lifetime policy.
     */
    private final OAuth2ProviderSettings settings;

    /**
     * Externally implemented client, token, and replay ports.
     */
    private final ExecutionServices services;

    /**
     * Standard scope validator used to enforce non-expanding rotation.
     */
    private final ScopeValidator scopeValidator;

    /**
     * Internal common opaque access-token issuer.
     */
    private final AccessTokenIssuer issuer;

    /**
     * Creates a refresh-token rotator for one compiled OAuth Provider.
     *
     * @param providerId     compiled server-role Source identifier
     * @param settings       validated Provider settings
     * @param services       caller-owned runtime dependencies
     * @param scopeValidator standard scope validator
     * @param issuer         common internal access-token issuer
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public RefreshTokenRotator(final String providerId, final OAuth2ProviderSettings settings,
            final ExecutionServices services, final ScopeValidator scopeValidator, final AccessTokenIssuer issuer) {
        this.providerId = Assert.notBlank(providerId, "OAuth 2.x Provider id must not be blank");
        this.settings = Assert.notNull(settings, "OAuth 2.x Provider settings must not be null");
        this.services = Assert.notNull(services, "OAuth 2.x execution services must not be null");
        this.scopeValidator = Assert.notNull(scopeValidator, "OAuth 2.x scope validator must not be null");
        this.issuer = Assert.notNull(issuer, "OAuth 2.x access token issuer must not be null");
    }

    /**
     * Computes a positive remaining TTL without extending the absolute family expiration.
     *
     * @param expiresAt fixed family expiration
     * @param now       current shared-clock instant
     * @return remaining milliseconds, or zero when expired
     */
    private static long remainingTtl(final Instant expiresAt, final Instant now) {
        final long milliseconds = Duration.between(now, expiresAt).toMillis();
        return Math.max(0L, milliseconds);
    }

    /**
     * Creates a standard invalid-grant rejection without exposing token or family material.
     *
     * @param description safe diagnostic description
     * @return rejected token outcome
     */
    private static Outcome<TokenResponse> invalidGrant(final String description) {
        return Outcome.rejected(failure(ErrorCode._400, OAuth2ErrorCode.INVALID_GRANT, description));
    }

    /**
     * Creates a normalized operational failure.
     *
     * @param description safe diagnostic description
     * @return failed token outcome
     */
    private static Outcome<TokenResponse> storeFailure(final String description) {
        return Outcome.failed(failure(ErrorCode._500, OAuth2ErrorCode.SERVER_ERROR, description));
    }

    /**
     * Creates a safe framework failure carrying one registered OAuth error identifier.
     *
     * @param error       existing Bus error definition
     * @param oauthError  registered OAuth error code
     * @param description non-sensitive diagnostic description
     * @return immutable safe failure
     */
    private static Outcome.Failure failure(
            final Errors error,
            final OAuth2ErrorCode oauthError,
            final String description) {
        return new Outcome.Failure(error, description,
                new JsonValue.ObjectValue(Map.of(OAUTH_ERROR, new JsonValue.StringValue(oauthError.value()))));
    }

    /**
     * Creates an already-completed token outcome stage.
     *
     * @param outcome token outcome
     * @return completed stage
     */
    private static CompletionStage<Outcome<TokenResponse>> completed(final Outcome<TokenResponse> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Validates and rotates one standard refresh-token grant.
     *
     * @param request standard token request containing a refresh-token grant
     * @param context invocation context carrying a verified client identifier
     * @param timeout shared end-to-end operation budget
     * @return asynchronous standard token response outcome
     */
    public CompletionStage<Outcome<TokenResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(request, "OAuth 2.x refresh token request must not be null");
        Assert.notNull(context, "OAuth 2.x refresh token context must not be null");
        Assert.notNull(timeout, "OAuth 2.x refresh token time budget must not be null");
        if (!(request.grant() instanceof RefreshTokenGrant grant)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE,
                                    "OAuth 2.x refresh processor accepts only the refresh_token grant")));
        }
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE,
                                    "OAuth 2.x refresh request has no remaining time budget")));
        }
        final String clientId = context.clientId().getOrNull();
        if (clientId == null) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._401,
                                    OAuth2ErrorCode.INVALID_CLIENT,
                                    "OAuth 2.x refresh request requires an authenticated or identified client")));
        }
        if (!settings.refreshTokenRotationRequired()
                || !settings.grantTypesSupported().contains(GrantType.REFRESH_TOKEN)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE,
                                    "OAuth 2.x refresh-token rotation is disabled by the Provider")));
        }

        final CompletionStage<Outcome<ClientResolver.Client>> resolution;
        try {
            resolution = services.clientResolver().resolve(clientId, context, timeout);
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x refresh client resolution failed"));
        }
        return resolution
                .handle(
                        (outcome, thrown) -> thrown == null && outcome != null ? outcome
                                : Outcome.<ClientResolver.Client>failed(
                                        failure(
                                                ErrorCode._500,
                                                OAuth2ErrorCode.SERVER_ERROR,
                                                "OAuth 2.x refresh client resolution failed")))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<ClientResolver.Client> success -> {
                        final ClientResolver.Client client = success.value();
                        if (client == null || !clientId.equals(client.id())) {
                            yield completed(
                                    Outcome.rejected(
                                            failure(
                                                    ErrorCode._401,
                                                    OAuth2ErrorCode.INVALID_CLIENT,
                                                    "OAuth 2.x refresh client registration is unavailable")));
                        }
                        if (!client.grantTypes().contains(GrantType.REFRESH_TOKEN.value())) {
                            yield completed(
                                    Outcome.rejected(
                                            failure(
                                                    ErrorCode._400,
                                                    OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
                                                    "OAuth 2.x client is not registered for refresh_token")));
                        }
                        yield rotate(grant, client, context, timeout);
                    }
                    case Outcome.Rejected<ClientResolver.Client> rejected -> completed(
                            Outcome.rejected(
                                    failure(
                                            rejected.failure().error(),
                                            OAuth2ErrorCode.INVALID_CLIENT,
                                            "OAuth 2.x refresh client registration was rejected")));
                    case Outcome.Failed<ClientResolver.Client> failed -> completed(
                            Outcome.failed(
                                    failure(
                                            failed.failure().error(),
                                            OAuth2ErrorCode.SERVER_ERROR,
                                            "OAuth 2.x refresh client resolution failed")));
                });
    }

    /**
     * Reads one refresh-token generation and selects active rotation or reuse handling.
     *
     * @param grant   standard refresh-token grant
     * @param client  resolved client registration
     * @param context immutable invocation context
     * @param timeout shared operation budget
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenResponse>> rotate(
            final RefreshTokenGrant grant,
            final ClientResolver.Client client,
            final Context context,
            final Timeout.Budget timeout) {
        final String oldKey = issuer.tokenKey(grant.refreshToken());
        final CompletionStage<ExpiringValue<RefreshTokenStore.Entry>> lookup;
        try {
            lookup = services.refreshTokenStore().get(oldKey);
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x refresh token lookup failed"));
        }
        return lookup.handle((stored, thrown) -> new StoreResult(stored, thrown)).thenCompose(result -> {
            if (result.failure() != null) {
                return completed(storeFailure("OAuth 2.x refresh token lookup failed"));
            }
            final ExpiringValue<RefreshTokenStore.Entry> stored = result.value();
            final Instant now = timeout.clock().now();
            if (stored == null || !stored.expiresAt().isAfter(now)) {
                return completed(invalidGrant("OAuth 2.x refresh token is invalid or expired"));
            }
            final RefreshTokenStore.Entry entry = stored.value();
            if (!providerId.equals(entry.providerId()) || !client.id().equals(entry.clientId())) {
                return completed(invalidGrant("OAuth 2.x refresh token binding is invalid"));
            }
            return switch (entry.status()) {
                case ACTIVE -> checkFamily(grant, client, oldKey, stored, entry, context, timeout);
                case ROTATED, REUSED -> markReuse(oldKey, stored, entry, timeout);
                case REVOKED -> completed(invalidGrant("OAuth 2.x refresh token family is revoked"));
            };
        });
    }

    /**
     * Checks the family replay marker before permitting an active generation to rotate.
     *
     * @param grant   standard refresh-token grant
     * @param client  resolved client registration
     * @param oldKey  isolated current refresh-token digest
     * @param stored  current expiring state
     * @param entry   active current generation
     * @param context immutable invocation context
     * @param timeout shared operation budget
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenResponse>> checkFamily(
            final RefreshTokenGrant grant,
            final ClientResolver.Client client,
            final String oldKey,
            final ExpiringValue<RefreshTokenStore.Entry> stored,
            final RefreshTokenStore.Entry entry,
            final Context context,
            final Timeout.Budget timeout) {
        final CompletionStage<ExpiringValue<String>> lookup;
        try {
            lookup = services.replayStore().get(familyKey(entry.familyId()));
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x refresh family state lookup failed"));
        }
        return lookup.handle((marker, thrown) -> new MarkerResult(marker, thrown)).thenCompose(result -> {
            if (result.failure() != null) {
                return completed(storeFailure("OAuth 2.x refresh family state lookup failed"));
            }
            if (result.marker() != null && result.marker().expiresAt().isAfter(timeout.clock().now())) {
                return revokeCompromised(oldKey, stored, entry, timeout);
            }
            final List<String> scope = grant.scope().isEmpty() ? entry.scope() : grant.scope().getOrNull().values();
            if (!validScope(scope, entry.scope(), client.scopes(), settings.scopesSupported())) {
                return completed(
                        Outcome.rejected(
                                failure(
                                        ErrorCode._400,
                                        OAuth2ErrorCode.INVALID_SCOPE,
                                        "OAuth 2.x refreshed scope exceeds the original authorization")));
            }
            if (entry.generation() == Long.MAX_VALUE) {
                return completed(storeFailure("OAuth 2.x refresh token generation is exhausted"));
            }
            return createSuccessor(client, oldKey, stored, entry, scope, context, timeout, 1);
        });
    }

    /**
     * Creates an unreturned successor generation before issuing its corresponding access token.
     *
     * @param client  resolved client registration
     * @param oldKey  isolated current refresh-token digest
     * @param stored  current expiring refresh state
     * @param entry   active current generation
     * @param scope   effective non-expanding scope
     * @param context immutable invocation context
     * @param timeout shared operation budget
     * @param attempt one-based successor digest create attempt
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenResponse>> createSuccessor(
            final ClientResolver.Client client,
            final String oldKey,
            final ExpiringValue<RefreshTokenStore.Entry> stored,
            final RefreshTokenStore.Entry entry,
            final List<String> scope,
            final Context context,
            final Timeout.Budget timeout,
            final int attempt) {
        final long ttlMillis = remainingTtl(stored.expiresAt(), timeout.clock().now());
        if (ttlMillis <= 0L) {
            return completed(invalidGrant("OAuth 2.x refresh token is expired"));
        }
        final String refreshToken = issuer.opaqueToken();
        final String refreshKey = issuer.tokenKey(refreshToken);
        final RefreshTokenStore.Entry successor = new RefreshTokenStore.Entry(providerId, client.id(),
                entry.subjectId(), entry.familyId(), entry.generation() + 1L, scope, entry.audience(),
                entry.confirmation(), entry.openIdBinding(), RefreshTokenStore.Status.ACTIVE);
        final CompletionStage<Boolean> creation;
        try {
            creation = services.refreshTokenStore()
                    .create(refreshKey, new ExpiringValue<>(successor, stored.expiresAt()), ttlMillis);
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x refresh successor persistence failed"));
        }
        return creation.handle((created, thrown) -> new CreateResult(Boolean.TRUE.equals(created), thrown))
                .thenCompose(result -> {
                    if (result.failure() != null) {
                        return completed(storeFailure("OAuth 2.x refresh successor persistence failed"));
                    }
                    if (!result.created() && attempt < MAXIMUM_CREATE_ATTEMPTS) {
                        return createSuccessor(client, oldKey, stored, entry, scope, context, timeout, attempt + 1);
                    }
                    if (!result.created()) {
                        return completed(storeFailure("OAuth 2.x refresh successor allocation failed"));
                    }
                    final AccessTokenIssuer.Grant accessGrant = new AccessTokenIssuer.Grant(client.id(),
                            entry.subjectId(), scope, entry.audience(), GrantType.REFRESH_TOKEN, false,
                            Optional.empty(), Optional.empty(), entry.confirmation(), entry.openIdBinding());
                    return issuer.issue(accessGrant, context, timeout)
                            .thenCompose(accessOutcome -> switch (accessOutcome) {
                                case Outcome.Succeeded<TokenEndpointResponse> success -> success
                                        .value() instanceof TokenResponse response
                                                ? rotateOld(
                                                        oldKey,
                                                        stored,
                                                        entry,
                                                        refreshToken,
                                                        refreshKey,
                                                        response,
                                                        timeout)
                                                : cleanupRefresh(
                                                        refreshKey,
                                                        storeFailure(
                                                                "OAuth 2.x refresh issuance returned a non-core response"));
                                case Outcome.Rejected<TokenEndpointResponse> rejected -> cleanupRefresh(
                                        refreshKey,
                                        Outcome.rejected(rejected.failure()));
                                case Outcome.Failed<TokenEndpointResponse> failed -> cleanupRefresh(
                                        refreshKey,
                                        Outcome.failed(failed.failure()));
                            });
                });
    }

    /**
     * Atomically marks the prior generation rotated and publishes the already persisted successor values.
     *
     * @param oldKey         isolated prior refresh-token digest
     * @param stored         current prior expiring state
     * @param entry          current active prior generation
     * @param refreshToken   unreturned successor refresh token
     * @param refreshKey     isolated successor digest
     * @param accessResponse unreturned persisted access-token response
     * @param timeout        shared operation budget
     * @return asynchronous complete token response or cleaned failure
     */
    private CompletionStage<Outcome<TokenResponse>> rotateOld(
            final String oldKey,
            final ExpiringValue<RefreshTokenStore.Entry> stored,
            final RefreshTokenStore.Entry entry,
            final String refreshToken,
            final String refreshKey,
            final TokenResponse accessResponse,
            final Timeout.Budget timeout) {
        final RefreshTokenStore.Entry rotated = new RefreshTokenStore.Entry(entry.providerId(), entry.clientId(),
                entry.subjectId(), entry.familyId(), entry.generation(), entry.scope(), entry.audience(),
                entry.confirmation(), entry.openIdBinding(), RefreshTokenStore.Status.ROTATED);
        final long ttlMillis = remainingTtl(stored.expiresAt(), timeout.clock().now());
        if (ttlMillis <= 0L) {
            return cleanupIssued(
                    refreshKey,
                    accessResponse.accessToken(),
                    invalidGrant("OAuth 2.x refresh token is expired"));
        }
        final CompletionStage<Boolean> replacement;
        try {
            replacement = services.refreshTokenStore()
                    .replace(oldKey, stored, new ExpiringValue<>(rotated, stored.expiresAt()), ttlMillis);
        } catch (RuntimeException exception) {
            return cleanupIssued(
                    refreshKey,
                    accessResponse.accessToken(),
                    storeFailure("OAuth 2.x refresh rotation persistence failed"));
        }
        return replacement.handle((replaced, thrown) -> new CreateResult(Boolean.TRUE.equals(replaced), thrown))
                .thenCompose(result -> {
                    if (result.failure() != null) {
                        return cleanupIssued(
                                refreshKey,
                                accessResponse.accessToken(),
                                storeFailure("OAuth 2.x refresh rotation persistence failed"));
                    }
                    if (!result.created()) {
                        return cleanupIssued(
                                refreshKey,
                                accessResponse.accessToken(),
                                invalidGrant("OAuth 2.x refresh token was concurrently consumed"))
                                        .thenCompose(ignored -> markReuse(oldKey, stored, entry, timeout));
                    }
                    return completed(
                            Outcome.succeeded(
                                    new TokenResponse(accessResponse.accessToken(), accessResponse.tokenType(),
                                            accessResponse.expiresIn(), Optional.of(refreshToken),
                                            accessResponse.scope(), accessResponse.extensions())));
                });
    }

    /**
     * Marks a reused generation and creates the family replay marker for every later generation.
     *
     * @param oldKey  isolated reused token digest
     * @param stored  current reused generation state
     * @param entry   reused generation entry
     * @param timeout shared operation budget
     * @return asynchronous invalid-grant outcome after marker persistence
     */
    private CompletionStage<Outcome<TokenResponse>> markReuse(
            final String oldKey,
            final ExpiringValue<RefreshTokenStore.Entry> stored,
            final RefreshTokenStore.Entry entry,
            final Timeout.Budget timeout) {
        final long ttlMillis = remainingTtl(stored.expiresAt(), timeout.clock().now());
        if (ttlMillis <= 0L) {
            return completed(invalidGrant("OAuth 2.x refresh token is expired"));
        }
        final CompletionStage<Boolean> marker;
        try {
            marker = services.replayStore().create(
                    familyKey(entry.familyId()),
                    new ExpiringValue<>(FAMILY_REUSED, stored.expiresAt()),
                    ttlMillis);
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x refresh family reuse persistence failed"));
        }
        return marker.handle((ignored, thrown) -> thrown).thenCompose(thrown -> {
            if (thrown != null) {
                return completed(storeFailure("OAuth 2.x refresh family reuse persistence failed"));
            }
            if (entry.status() == RefreshTokenStore.Status.ROTATED) {
                final RefreshTokenStore.Entry reused = new RefreshTokenStore.Entry(entry.providerId(), entry.clientId(),
                        entry.subjectId(), entry.familyId(), entry.generation(), entry.scope(), entry.audience(),
                        entry.confirmation(), entry.openIdBinding(), RefreshTokenStore.Status.REUSED);
                try {
                    return services.refreshTokenStore()
                            .replace(oldKey, stored, new ExpiringValue<>(reused, stored.expiresAt()), ttlMillis)
                            .handle((ignored, failure) -> invalidGrant("OAuth 2.x refresh token reuse was detected"));
                } catch (RuntimeException exception) {
                    return completed(invalidGrant("OAuth 2.x refresh token reuse was detected"));
                }
            }
            return completed(invalidGrant("OAuth 2.x refresh token reuse was detected"));
        });
    }

    /**
     * Best-effort revokes an active generation belonging to a family already marked as reused.
     *
     * @param oldKey  isolated active token digest
     * @param stored  current active generation state
     * @param entry   active generation entry
     * @param timeout shared operation budget
     * @return asynchronous invalid-grant outcome
     */
    private CompletionStage<Outcome<TokenResponse>> revokeCompromised(
            final String oldKey,
            final ExpiringValue<RefreshTokenStore.Entry> stored,
            final RefreshTokenStore.Entry entry,
            final Timeout.Budget timeout) {
        final RefreshTokenStore.Entry revoked = new RefreshTokenStore.Entry(entry.providerId(), entry.clientId(),
                entry.subjectId(), entry.familyId(), entry.generation(), entry.scope(), entry.audience(),
                entry.confirmation(), entry.openIdBinding(), RefreshTokenStore.Status.REVOKED);
        final long ttlMillis = remainingTtl(stored.expiresAt(), timeout.clock().now());
        if (ttlMillis <= 0L) {
            return completed(invalidGrant("OAuth 2.x refresh token family is expired"));
        }
        try {
            return services.refreshTokenStore()
                    .replace(oldKey, stored, new ExpiringValue<>(revoked, stored.expiresAt()), ttlMillis)
                    .handle((ignored, thrown) -> invalidGrant("OAuth 2.x refresh token family reuse was detected"));
        } catch (RuntimeException exception) {
            return completed(invalidGrant("OAuth 2.x refresh token family reuse was detected"));
        }
    }

    /**
     * Cleans an unreturned successor refresh token while preserving the original issuer outcome.
     *
     * @param refreshKey isolated successor refresh-token digest
     * @param outcome    original access-token issuance outcome
     * @return asynchronous original outcome after cleanup
     */
    private CompletionStage<Outcome<TokenResponse>> cleanupRefresh(
            final String refreshKey,
            final Outcome<TokenResponse> outcome) {
        try {
            return services.refreshTokenStore().delete(refreshKey).handle((ignored, thrown) -> outcome);
        } catch (RuntimeException exception) {
            return completed(outcome);
        }
    }

    /**
     * Cleans both unreturned successor credentials after the old-generation CAS cannot publish them.
     *
     * @param refreshKey  isolated successor refresh-token digest
     * @param accessToken unreturned opaque access token
     * @param outcome     rejection or failure to preserve
     * @return asynchronous original outcome after both cleanup attempts
     */
    private CompletionStage<Outcome<TokenResponse>> cleanupIssued(
            final String refreshKey,
            final String accessToken,
            final Outcome<TokenResponse> outcome) {
        final CompletionStage<Boolean> refreshDelete;
        final CompletionStage<Boolean> accessDelete;
        try {
            refreshDelete = services.refreshTokenStore().delete(refreshKey);
            accessDelete = services.accessTokenStore().delete(issuer.tokenKey(accessToken));
        } catch (RuntimeException exception) {
            return completed(outcome);
        }
        return refreshDelete.handle((ignored, thrown) -> null)
                .thenCombine(accessDelete.handle((ignored, thrown) -> null), (first, second) -> outcome);
    }

    /**
     * Validates a refreshed scope against original, client, and Provider bounds.
     *
     * @param scope    candidate effective scope
     * @param original original refresh-token scope
     * @param client   client-registered scope set
     * @param provider Provider-supported scope set
     * @return whether the candidate is a valid subset of every bound
     */
    private boolean validScope(
            final List<String> scope,
            final List<String> original,
            final java.util.Set<String> client,
            final java.util.Set<String> provider) {
        try {
            scopeValidator.validateReduced(scope, original);
            scopeValidator.validateRequested(scope, client);
            scopeValidator.validateRequested(scope, provider);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /**
     * Produces the irreversible domain-separated replay marker key for one family identifier.
     *
     * @param familyId internal random family identifier
     * @return hexadecimal SHA-256 ReplayStore key
     */
    private String familyKey(final String familyId) {
        return Builder.sha256Hex(providerId + '\0' + FAMILY_KEY_PURPOSE + '\0' + familyId);
    }

    /**
     * Couples a refresh Store value with its completion failure.
     *
     * @param value   returned refresh state
     * @param failure completion failure, or {@code null}
     * @author Kimi Liu
     */
    private record StoreResult(ExpiringValue<RefreshTokenStore.Entry> value, Throwable failure) {

    }

    /**
     * Couples a family marker with its completion failure.
     *
     * @param marker  returned replay marker
     * @param failure completion failure, or {@code null}
     * @author Kimi Liu
     */
    private record MarkerResult(ExpiringValue<String> marker, Throwable failure) {

    }

    /**
     * Couples an atomic create or replace result with its completion failure.
     *
     * @param created whether the atomic operation succeeded
     * @param failure completion failure, or {@code null}
     * @author Kimi Liu
     */
    private record CreateResult(boolean created, Throwable failure) {

    }

}
