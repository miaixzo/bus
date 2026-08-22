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
package org.miaixz.bus.auth.protocol.oauth2.grant;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.AuthorizationCache;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.cache.RefreshTokenCache;
import org.miaixz.bus.auth.guard.ScopeValidator;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Rotates OAuth refresh-token families with reuse detection and fixed absolute family expiration.
 * <p>
 * The authoritative {@link AuthorizationCache} record is checked before every rotation and atomically becomes
 * compromised when a rotated generation is reused. Access and refresh caches remain derived token indexes. New token
 * values remain unreturned until the old active generation is atomically replaced with its rotated state.
 * </p>
 *
 * @author Kimi Liu
 */
public class RefreshTokenRotator {

    /**
     * Provider identifier used in token and family key isolation.
     */
    private final String providerId;

    /**
     * Frozen Provider grant and lifetime policy.
     */
    private final GrantPolicy options;

    /**
     * Externally implemented client, token, and replay ports.
     */
    private final DriverServices services;

    /**
     * Standard scope validator used to enforce non-expanding rotation.
     */
    private final ScopeValidator scopeValidator;

    /**
     * Internal common opaque access-token issuer.
     */
    private final AccessTokenIssuer issuer;

    /**
     * Provider-isolated opaque token generation and irreversible key derivation.
     */
    private final TokenMaterial tokenMaterial;

    /**
     * Creates a refresh-token rotator for one compiled OAuth Provider.
     *
     * @param providerId     compiled server-role Source identifier
     * @param options        validated Provider options
     * @param services       caller-owned runtime dependencies
     * @param scopeValidator standard scope validator
     * @param issuer         common internal access-token issuer
     * @param tokenMaterial  shared opaque token material service
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public RefreshTokenRotator(final String providerId, final GrantPolicy options, final DriverServices services,
            final ScopeValidator scopeValidator, final AccessTokenIssuer issuer, final TokenMaterial tokenMaterial) {
        this.providerId = Assert.notBlank(providerId, "OAuth 2.x Provider id must not be blank");
        this.options = Assert.notNull(options, "OAuth 2.x Provider options must not be null");
        this.services = Assert.notNull(services, "OAuth 2.x execution services must not be null");
        this.scopeValidator = Assert.notNull(scopeValidator, "OAuth 2.x scope validator must not be null");
        this.issuer = Assert.notNull(issuer, "OAuth 2.x access token issuer must not be null");
        this.tokenMaterial = Assert.notNull(tokenMaterial, "OAuth 2.x token material service must not be null");
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
                new JsonValue.ObjectValue(Map.of(Builder.OAUTH_ERROR, new JsonValue.StringValue(oauthError.value()))));
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
     * @param client  authenticated OAuth consumer metadata
     * @param context invocation context carrying a verified client identifier
     * @param timeout shared end-to-end operation timeout
     * @return asynchronous standard token response outcome
     */
    public CompletionStage<Outcome<TokenResponse>> token(
            final TokenRequest request,
            final ConsumerMetadata client,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x refresh token request must not be null");
        Assert.notNull(client, "OAuth 2.x refresh client must not be null");
        Assert.notNull(context, "OAuth 2.x refresh token context must not be null");
        Assert.notNull(timeout, "OAuth 2.x refresh token timeout must not be null");
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
                                    "OAuth 2.x refresh request has no remaining timeout")));
        }
        if (!options.refreshTokenRotationRequired()
                || !options.grantTypesSupported().contains(GrantType.REFRESH_TOKEN)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE,
                                    "OAuth 2.x refresh-token rotation is disabled by the Provider")));
        }

        if (!client.grantTypes().contains(GrantType.REFRESH_TOKEN)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
                                    "OAuth 2.x client is not registered for refresh_token")));
        }
        return rotate(grant, client, context, timeout);
    }

    /**
     * Reads one refresh-token generation and selects active rotation or reuse handling.
     *
     * @param grant   standard refresh-token grant
     * @param client  resolved client registration
     * @param context immutable invocation context
     * @param timeout shared operation timeout
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenResponse>> rotate(
            final RefreshTokenGrant grant,
            final ConsumerMetadata client,
            final Context context,
            final Timeout timeout) {
        final String oldKey = tokenMaterial.key(grant.refreshToken());
        final CompletionStage<ExpiringValue<RefreshTokenCache.Entry>> lookup;
        try {
            lookup = services.refreshTokenCache().find(oldKey);
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x refresh token lookup failed"));
        }
        return lookup.handle((stored, thrown) -> new CacheResult(stored, thrown)).thenCompose(result -> {
            if (result.failure() != null) {
                return completed(storeFailure("OAuth 2.x refresh token lookup failed"));
            }
            final ExpiringValue<RefreshTokenCache.Entry> stored = result.value();
            final Instant now = timeout.clock().now();
            if (stored == null || !stored.expiresAt().isAfter(now)) {
                return completed(invalidGrant("OAuth 2.x refresh token is invalid or expired"));
            }
            final RefreshTokenCache.Entry entry = stored.value();
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
     * Checks the authoritative authorization family before permitting an active generation to rotate.
     *
     * @param grant   standard refresh-token grant
     * @param client  resolved client registration
     * @param oldKey  isolated current refresh-token digest
     * @param stored  current expiring state
     * @param entry   active current generation
     * @param context immutable invocation context
     * @param timeout shared operation timeout
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenResponse>> checkFamily(
            final RefreshTokenGrant grant,
            final ConsumerMetadata client,
            final String oldKey,
            final ExpiringValue<RefreshTokenCache.Entry> stored,
            final RefreshTokenCache.Entry entry,
            final Context context,
            final Timeout timeout) {
        final CompletionStage<ExpiringValue<AuthorizationCache.Entry>> lookup;
        try {
            lookup = services.authorizationCache().find(AuthorizationCache.key(providerId, entry.familyId()));
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x refresh family state lookup failed"));
        }
        return lookup.handle((family, thrown) -> new FamilyResult(family, thrown)).thenCompose(result -> {
            if (result.failure() != null) {
                return completed(storeFailure("OAuth 2.x refresh family state lookup failed"));
            }
            final ExpiringValue<AuthorizationCache.Entry> family = result.family();
            if (family == null || !family.expiresAt().isAfter(timeout.clock().now())
                    || !providerId.equals(family.value().providerId())
                    || !client.id().equals(family.value().clientId())) {
                return completed(invalidGrant("OAuth 2.x refresh token authorization is invalid or expired"));
            }
            if (family.value().status() != AuthorizationCache.Status.ACTIVE) {
                return revokeCompromised(oldKey, stored, entry, timeout);
            }
            final List<String> scope = grant.scope().isEmpty() ? entry.scope() : grant.scope().getOrNull().values();
            if (!validScope(scope, entry.scope(), client.scopes(), options.scopesSupported())) {
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
     * @param timeout shared operation timeout
     * @param attempt one-based successor digest create attempt
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenResponse>> createSuccessor(
            final ConsumerMetadata client,
            final String oldKey,
            final ExpiringValue<RefreshTokenCache.Entry> stored,
            final RefreshTokenCache.Entry entry,
            final List<String> scope,
            final Context context,
            final Timeout timeout,
            final int attempt) {
        final long ttlMillis = remainingTtl(stored.expiresAt(), timeout.clock().now());
        if (ttlMillis <= 0L) {
            return completed(invalidGrant("OAuth 2.x refresh token is expired"));
        }
        final String refreshToken = tokenMaterial.create();
        final String refreshKey = tokenMaterial.key(refreshToken);
        final RefreshTokenCache.Entry successor = new RefreshTokenCache.Entry(providerId, client.id(),
                entry.subjectId(), entry.familyId(), entry.generation() + 1L, scope, entry.audience(),
                entry.confirmation(), entry.openIdBinding(), RefreshTokenCache.Status.ACTIVE);
        final CompletionStage<Boolean> creation;
        try {
            creation = services.refreshTokenCache()
                    .issue(refreshKey, new ExpiringValue<>(successor, stored.expiresAt()));
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x refresh successor persistence failed"));
        }
        return creation.handle((created, thrown) -> new CreateResult(Boolean.TRUE.equals(created), thrown))
                .thenCompose(result -> {
                    if (result.failure() != null) {
                        return completed(storeFailure("OAuth 2.x refresh successor persistence failed"));
                    }
                    if (!result.created() && attempt < Builder.MAXIMUM_RETRY_ATTEMPTS) {
                        return createSuccessor(client, oldKey, stored, entry, scope, context, timeout, attempt + 1);
                    }
                    if (!result.created()) {
                        return completed(storeFailure("OAuth 2.x refresh successor allocation failed"));
                    }
                    final AccessTokenIssuer.Grant accessGrant = new AccessTokenIssuer.Grant(client, entry.subjectId(),
                            scope, entry.audience(), GrantType.REFRESH_TOKEN, false, Optional.empty(), Optional.empty(),
                            entry.confirmation(), entry.openIdBinding(), Optional.of(entry.familyId()));
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
                                default -> throw new IllegalStateException("Unsupported Outcome implementation");
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
     * @param timeout        shared operation timeout
     * @return asynchronous complete token response or cleaned failure
     */
    private CompletionStage<Outcome<TokenResponse>> rotateOld(
            final String oldKey,
            final ExpiringValue<RefreshTokenCache.Entry> stored,
            final RefreshTokenCache.Entry entry,
            final String refreshToken,
            final String refreshKey,
            final TokenResponse accessResponse,
            final Timeout timeout) {
        final RefreshTokenCache.Entry rotated = new RefreshTokenCache.Entry(entry.providerId(), entry.clientId(),
                entry.subjectId(), entry.familyId(), entry.generation(), entry.scope(), entry.audience(),
                entry.confirmation(), entry.openIdBinding(), RefreshTokenCache.Status.ROTATED);
        final long ttlMillis = remainingTtl(stored.expiresAt(), timeout.clock().now());
        if (ttlMillis <= 0L) {
            return cleanupIssued(
                    refreshKey,
                    accessResponse.accessToken(),
                    invalidGrant("OAuth 2.x refresh token is expired"));
        }
        final CompletionStage<Boolean> replacement;
        try {
            replacement = services.refreshTokenCache()
                    .rotate(oldKey, stored, new ExpiringValue<>(rotated, stored.expiresAt()));
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
     * Marks the authoritative authorization compromised and records reuse on the presented token generation.
     *
     * @param oldKey  isolated reused token digest
     * @param stored  current reused generation state
     * @param entry   reused generation entry
     * @param timeout shared operation timeout
     * @return asynchronous invalid-grant outcome after authoritative family-state persistence
     */
    private CompletionStage<Outcome<TokenResponse>> markReuse(
            final String oldKey,
            final ExpiringValue<RefreshTokenCache.Entry> stored,
            final RefreshTokenCache.Entry entry,
            final Timeout timeout) {
        final long ttlMillis = remainingTtl(stored.expiresAt(), timeout.clock().now());
        if (ttlMillis <= 0L) {
            return completed(invalidGrant("OAuth 2.x refresh token is expired"));
        }
        return compromiseFamily(entry, timeout, 1).handle((confirmed, thrown) -> thrown).thenCompose(thrown -> {
            if (thrown != null) {
                return completed(storeFailure("OAuth 2.x refresh family reuse persistence failed"));
            }
            if (entry.status() == RefreshTokenCache.Status.ROTATED) {
                final RefreshTokenCache.Entry reused = new RefreshTokenCache.Entry(entry.providerId(), entry.clientId(),
                        entry.subjectId(), entry.familyId(), entry.generation(), entry.scope(), entry.audience(),
                        entry.confirmation(), entry.openIdBinding(), RefreshTokenCache.Status.REUSED);
                try {
                    return services.refreshTokenCache()
                            .rotate(oldKey, stored, new ExpiringValue<>(reused, stored.expiresAt()))
                            .handle((ignored, failure) -> invalidGrant("OAuth 2.x refresh token reuse was detected"));
                } catch (RuntimeException exception) {
                    return completed(invalidGrant("OAuth 2.x refresh token reuse was detected"));
                }
            }
            return completed(invalidGrant("OAuth 2.x refresh token reuse was detected"));
        });
    }

    /**
     * Conclusively transitions an active authorization family to compromised or observes an already non-active state.
     *
     * @param entry   reused refresh-token generation identifying the authorization family
     * @param timeout shared operation timeout
     * @param attempt one-based compare-and-set attempt
     * @return stage completed with a conclusive non-active family state
     */
    private CompletionStage<Boolean> compromiseFamily(
            final RefreshTokenCache.Entry entry,
            final Timeout timeout,
            final int attempt) {
        if (timeout.expired()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("OAuth 2.x refresh family transition exhausted its timeout"));
        }
        final String key = AuthorizationCache.key(providerId, entry.familyId());
        final CompletionStage<ExpiringValue<AuthorizationCache.Entry>> lookup;
        try {
            lookup = services.authorizationCache().find(key);
        } catch (RuntimeException exception) {
            return CompletableFuture.failedFuture(exception);
        }
        if (lookup == null) {
            return CompletableFuture
                    .failedFuture(new IllegalStateException("OAuth 2.x refresh family lookup returned no stage"));
        }
        return lookup.thenCompose(family -> {
            if (family == null || !family.expiresAt().isAfter(timeout.clock().now())
                    || !providerId.equals(family.value().providerId())
                    || !entry.clientId().equals(family.value().clientId())) {
                return CompletableFuture
                        .failedFuture(new IllegalStateException("OAuth 2.x refresh family binding is invalid"));
            }
            if (family.value().status() != AuthorizationCache.Status.ACTIVE) {
                return CompletableFuture.completedFuture(true);
            }
            final AuthorizationCache.Entry compromised = new AuthorizationCache.Entry(providerId, entry.clientId(),
                    AuthorizationCache.Status.COMPROMISED);
            final CompletionStage<Boolean> transition;
            try {
                transition = services.authorizationCache()
                        .update(key, family, new ExpiringValue<>(compromised, family.expiresAt()));
            } catch (RuntimeException exception) {
                return CompletableFuture.failedFuture(exception);
            }
            if (transition == null) {
                return CompletableFuture.failedFuture(
                        new IllegalStateException("OAuth 2.x refresh family transition returned no stage"));
            }
            return transition.thenCompose(updated -> {
                if (Boolean.TRUE.equals(updated)) {
                    return CompletableFuture.completedFuture(true);
                }
                if (attempt >= Builder.MAXIMUM_RETRY_ATTEMPTS) {
                    return confirmFamilyInactive(key, entry, timeout);
                }
                return compromiseFamily(entry, timeout, attempt + 1);
            });
        });
    }

    /**
     * Confirms that the final failed family CAS observed another completed non-active transition.
     *
     * @param key     Provider-isolated authorization family key
     * @param entry   reused refresh-token generation
     * @param timeout shared operation timeout
     * @return stage completed when the family is conclusively non-active
     */
    private CompletionStage<Boolean> confirmFamilyInactive(
            final String key,
            final RefreshTokenCache.Entry entry,
            final Timeout timeout) {
        return services.authorizationCache().find(key).thenCompose(family -> {
            if (family != null && family.expiresAt().isAfter(timeout.clock().now())
                    && providerId.equals(family.value().providerId())
                    && entry.clientId().equals(family.value().clientId())
                    && family.value().status() != AuthorizationCache.Status.ACTIVE) {
                return CompletableFuture.completedFuture(true);
            }
            return CompletableFuture
                    .failedFuture(new IllegalStateException("OAuth 2.x refresh family changed concurrently"));
        });
    }

    /**
     * Best-effort revokes an active generation belonging to a family already marked as reused.
     *
     * @param oldKey  isolated active token digest
     * @param stored  current active generation state
     * @param entry   active generation entry
     * @param timeout shared operation timeout
     * @return asynchronous invalid-grant outcome
     */
    private CompletionStage<Outcome<TokenResponse>> revokeCompromised(
            final String oldKey,
            final ExpiringValue<RefreshTokenCache.Entry> stored,
            final RefreshTokenCache.Entry entry,
            final Timeout timeout) {
        final RefreshTokenCache.Entry revoked = new RefreshTokenCache.Entry(entry.providerId(), entry.clientId(),
                entry.subjectId(), entry.familyId(), entry.generation(), entry.scope(), entry.audience(),
                entry.confirmation(), entry.openIdBinding(), RefreshTokenCache.Status.REVOKED);
        final long ttlMillis = remainingTtl(stored.expiresAt(), timeout.clock().now());
        if (ttlMillis <= 0L) {
            return completed(invalidGrant("OAuth 2.x refresh token family is expired"));
        }
        try {
            return services.refreshTokenCache().rotate(oldKey, stored, new ExpiringValue<>(revoked, stored.expiresAt()))
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
            return services.refreshTokenCache().revoke(refreshKey).handle((ignored, thrown) -> outcome);
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
            refreshDelete = services.refreshTokenCache().revoke(refreshKey);
            accessDelete = services.accessTokenCache().revoke(tokenMaterial.key(accessToken));
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
            final Set<String> client,
            final Set<String> provider) {
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
     * Couples a refresh-cache value with its completion failure.
     *
     * @param value   returned refresh state
     * @param failure completion failure, or {@code null}
     * @author Kimi Liu
     */
    private record CacheResult(ExpiringValue<RefreshTokenCache.Entry> value, Throwable failure) {

    }

    /**
     * Couples an authoritative authorization-family value with its completion failure.
     *
     * @param family  returned authorization-family state
     * @param failure completion failure, or {@code null}
     * @author Kimi Liu
     */
    private record FamilyResult(ExpiringValue<AuthorizationCache.Entry> family, Throwable failure) {

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
