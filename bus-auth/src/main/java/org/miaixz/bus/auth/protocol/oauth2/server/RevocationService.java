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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.AccessTokenCache;
import org.miaixz.bus.auth.cache.AuthorizationCache;
import org.miaixz.bus.auth.cache.ExpiringValue;
import org.miaixz.bus.auth.cache.RefreshTokenCache;
import org.miaixz.bus.auth.protocol.oauth2.RevocationRequest;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Revokes client-bound opaque access and refresh tokens according to RFC 7009.
 * <p>
 * The service first atomically transitions the shared {@link AuthorizationCache} state. Access and refresh token cache
 * deletion is only idempotent derived-index cleanup, so a partial cleanup cannot preserve credential validity.
 * </p>
 *
 * @author Kimi Liu
 */
public class RevocationService {

    /**
     * Provider identifier used to isolate opaque token digests.
     */
    private final String providerId;

    /**
     * Runtime dependencies containing both token caches.
     */
    private final DriverServices services;

    /**
     * Creates a revocation service for one compiled server-role Source runtime.
     *
     * @param providerId compiled server-role Source identifier
     * @param services   externally owned runtime dependencies
     * @throws IllegalArgumentException if the identifier is blank or services are {@code null}
     */
    public RevocationService(final String providerId, final DriverServices services) {
        this.providerId = Assert.notBlank(providerId, "OAuth 2.x Provider id must not be blank");
        this.services = Assert.notNull(services, "OAuth 2.x execution services must not be null");
    }

    /**
     * Creates a non-sensitive closed failure.
     *
     * @param code        shared Bus error code
     * @param description safe diagnostic text
     * @return framework failure value
     */
    private static Outcome.Failure failure(final Errors code, final String description) {
        return new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Creates a completed stage with inferred success type.
     *
     * @param <T>     outcome success type
     * @param outcome completed outcome
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Revokes any active token bound to the authenticated client while hiding unknown-token state.
     *
     * @param request standard token revocation request
     * @param context invocation context carrying a verified client identifier
     * @param timeout shared end-to-end timeout
     * @return asynchronous empty standard success or closed framework failure
     */
    public CompletionStage<Outcome<Void>> revoke(
            final RevocationRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x revocation request must not be null");
        Assert.notNull(context, "OAuth 2.x revocation context must not be null");
        Assert.notNull(timeout, "OAuth 2.x revocation timeout must not be null");
        final String clientId = context.clientId().getOrNull();
        if (clientId == null) {
            return completed(
                    Outcome.rejected(
                            failure(ErrorCode._401, "OAuth 2.x token revocation requires an identified client")));
        }
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(failure(ErrorCode._408, "OAuth 2.x token revocation has no remaining timeout")));
        }
        final String key = key(request.token());
        final CompletionStage<ExpiringValue<AccessTokenCache.Entry>> access;
        final CompletionStage<ExpiringValue<RefreshTokenCache.Entry>> refresh;
        try {
            access = services.accessTokenCache().find(key);
            refresh = services.refreshTokenCache().find(key);
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure(ErrorCode._500, "OAuth 2.x token lookup failed")));
        }
        return access.thenCombine(refresh, TokenState::new).thenCompose(state -> revoke(key, state, clientId, timeout))
                .handle(
                        (ignored, thrown) -> thrown == null ? Outcome.<Void>succeeded(null)
                                : Outcome.<Void>failed(
                                        failure(ErrorCode._500, "OAuth 2.x token revocation cache failed")));
    }

    /**
     * Deletes only unexpired token entries owned by this Provider and authenticated client.
     *
     * @param key      isolated token digest key
     * @param state    current access and refresh token state
     * @param clientId authenticated client identifier
     * @param timeout  shared end-to-end operation timeout
     * @return stage completed after all applicable atomic deletes
     */
    private CompletionStage<Void> revoke(
            final String key,
            final TokenState state,
            final String clientId,
            final Timeout timeout) {
        final Instant now = timeout.clock().now();
        final boolean deleteAccess = accessOwned(state.access(), clientId, now);
        final boolean deleteRefresh = refreshOwned(state.refresh(), clientId, now);
        CompletionStage<Void> authority = CompletableFuture.completedFuture(null);
        if (deleteAccess) {
            authority = authority.thenCompose(
                    ignored -> revokeAuthorization(state.access().value().authorizationId(), clientId, timeout, 1));
        }
        if (deleteRefresh && (!deleteAccess
                || !state.refresh().value().familyId().equals(state.access().value().authorizationId()))) {
            authority = authority.thenCompose(
                    ignored -> revokeAuthorization(state.refresh().value().familyId(), clientId, timeout, 1));
        }
        return authority.thenCompose(ignored -> {
            final CompletionStage<Boolean> accessDelete = deleteAccess ? services.accessTokenCache().revoke(key)
                    : CompletableFuture.completedFuture(false);
            final CompletionStage<Boolean> refreshDelete = deleteRefresh ? services.refreshTokenCache().revoke(key)
                    : CompletableFuture.completedFuture(false);
            return accessDelete.handle((value, failure) -> null)
                    .thenCombine(refreshDelete.handle((value, failure) -> null), (first, second) -> null);
        });
    }

    /**
     * Atomically transitions an active authorization lifecycle to revoked.
     *
     * @param authorizationId internal authorization identifier
     * @param clientId        authenticated client identifier
     * @param timeout         shared operation timeout
     * @param attempt         one-based compare-and-set attempt
     * @return stage completed after conclusive revocation or an already non-active state
     */
    private CompletionStage<Void> revokeAuthorization(
            final String authorizationId,
            final String clientId,
            final Timeout timeout,
            final int attempt) {
        if (timeout.expired()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("OAuth 2.x authorization revocation exhausted its timeout"));
        }
        final Instant now = timeout.clock().now();
        final String authorizationKey = AuthorizationCache.key(providerId, authorizationId);
        return services.authorizationCache().find(authorizationKey).thenCompose(stored -> {
            if (stored == null || !stored.expiresAt().isAfter(now) || !providerId.equals(stored.value().providerId())
                    || !clientId.equals(stored.value().clientId())
                    || stored.value().status() != AuthorizationCache.Status.ACTIVE) {
                return CompletableFuture.completedFuture(null);
            }
            final AuthorizationCache.Entry revoked = new AuthorizationCache.Entry(providerId, clientId,
                    AuthorizationCache.Status.REVOKED);
            return services.authorizationCache()
                    .update(authorizationKey, stored, new ExpiringValue<>(revoked, stored.expiresAt()))
                    .thenCompose(updated -> {
                        if (Boolean.TRUE.equals(updated)) {
                            return CompletableFuture.completedFuture(null);
                        }
                        if (attempt >= org.miaixz.bus.auth.Builder.MAXIMUM_RETRY_ATTEMPTS) {
                            return confirmAuthorizationInactive(authorizationKey, clientId, timeout);
                        }
                        return revokeAuthorization(authorizationId, clientId, timeout, attempt + 1);
                    });
        });
    }

    /**
     * Confirms that a failed final CAS was caused by another successful non-active transition.
     *
     * @param authorizationKey Provider-isolated authorization key
     * @param clientId         authenticated client identifier
     * @param timeout          shared operation timeout
     * @return stage completed when authorization is conclusively non-active
     */
    private CompletionStage<Void> confirmAuthorizationInactive(
            final String authorizationKey,
            final String clientId,
            final Timeout timeout) {
        return services.authorizationCache().find(authorizationKey).thenCompose(stored -> {
            if (stored != null && stored.expiresAt().isAfter(timeout.clock().now())
                    && providerId.equals(stored.value().providerId()) && clientId.equals(stored.value().clientId())
                    && stored.value().status() != AuthorizationCache.Status.ACTIVE) {
                return CompletableFuture.completedFuture(null);
            }
            return CompletableFuture
                    .failedFuture(new IllegalStateException("OAuth 2.x authorization revocation changed concurrently"));
        });
    }

    /**
     * Tests whether an access-token entry belongs to the current Provider and client and remains active.
     *
     * @param stored   optional stored access-token value
     * @param clientId authenticated client identifier
     * @param now      current operation instant
     * @return whether this entry may be deleted
     */
    private boolean accessOwned(
            final ExpiringValue<AccessTokenCache.Entry> stored,
            final String clientId,
            final Instant now) {
        return stored != null && stored.expiresAt().isAfter(now) && providerId.equals(stored.value().providerId())
                && clientId.equals(stored.value().clientId());
    }

    /**
     * Tests whether a refresh-token entry belongs to the current Provider and client and remains active.
     *
     * @param stored   optional stored refresh-token value
     * @param clientId authenticated client identifier
     * @param now      current operation instant
     * @return whether this entry may be deleted
     */
    private boolean refreshOwned(
            final ExpiringValue<RefreshTokenCache.Entry> stored,
            final String clientId,
            final Instant now) {
        return stored != null && stored.expiresAt().isAfter(now) && providerId.equals(stored.value().providerId())
                && clientId.equals(stored.value().clientId());
    }

    /**
     * Produces a Provider-isolated irreversible lookup key for an opaque token.
     *
     * @param token opaque token value
     * @return SHA-256 hexadecimal cache key
     */
    private String key(final String token) {
        return Builder.sha256Hex(providerId + Symbol.C_NUL + token);
    }

    /**
     * Carries the two possible cache entries addressed by one opaque token digest.
     *
     * @param access  optional access-token state
     * @param refresh optional refresh-token state
     * @author Kimi Liu
     */
    private record TokenState(ExpiringValue<AccessTokenCache.Entry> access,
            ExpiringValue<RefreshTokenCache.Entry> refresh) {

    }

}
