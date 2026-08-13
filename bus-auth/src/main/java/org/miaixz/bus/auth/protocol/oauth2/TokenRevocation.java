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
package org.miaixz.bus.auth.protocol.oauth2;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationCodeFlow.RefreshState;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.ProtocolError;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.RevocationRequest;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.TokenTypeHint;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

/**
 * Authenticates OAuth revocation callers and performs idempotent access or refresh revocation.
 * <p>
 * Unknown, expired, rotated, and cross-client tokens complete successfully without disclosing token state. Access
 * tokens are revoked through the grant port. Revoking an active refresh token removes that member and creates a family
 * marker retained until the family's original expiration, invalidating every remaining family member.
 * </p>
 *
 * @author Kimi Liu
 */
public final class TokenRevocation {

    /**
     * Non-sensitive value stored for a revoked refresh family.
     */
    private static final byte[] REVOKED_VALUE = { Normal._1 };

    /**
     * Resolvers and authorization-grant persistence used for access-token revocation.
     */
    private final OAuth2Dependencies dependencies;

    /**
     * Atomic state store used for refresh-token family revocation.
     */
    private final StateStore states;

    /**
     * Tenant-aware resolver for confidential-client secrets.
     */
    private final SecretResolver secrets;

    /**
     * Trusted source of the current security time.
     */
    private final Clock clock;

    /**
     * JSON provider used only through bounded refresh-state codecs.
     */
    private final JsonProvider json;

    /**
     * Closed parser and allocation limits for persisted refresh state.
     */
    private final Limits limits;

    /**
     * Creates one token revocation service.
     *
     * @param dependencies non-null OAuth resolver and grant dependencies
     * @param states       non-null atomic state store
     * @param secrets      non-null client-secret resolver
     * @param clock        non-null security clock
     * @param json         non-null JSON provider
     * @param limits       non-null parser and allocation limits
     * @throws IllegalArgumentException if any collaborator is {@code null}
     */
    public TokenRevocation(final OAuth2Dependencies dependencies, final StateStore states, final SecretResolver secrets,
            final Clock clock, final JsonProvider json, final Limits limits) {
        this.dependencies = Assert.notNull(dependencies, "OAuth dependencies must be not null!");
        this.states = Assert.notNull(states, "State store must be not null!");
        this.secrets = Assert.notNull(secrets, "Secret resolver must be not null!");
        this.clock = Assert.notNull(clock, "Clock must be not null!");
        this.json = Assert.notNull(json, "JSON provider must be not null!");
        this.limits = Assert.notNull(limits, "Limits must be not null!");
    }

    /**
     * Authenticates the caller and idempotently revokes its presented token.
     *
     * @param invocation tenant-scoped operation context
     * @param request    revocation request
     * @return stage completed after revocation processing
     * @throws IllegalArgumentException if a required input or dependency stage is {@code null}
     * @throws ProtocolException        if client authentication or request syntax fails
     */
    public CompletionStage<Void> revoke(final Context invocation, final RevocationRequest request) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final RevocationRequest input = Assert.notNull(request, "Revocation request must be not null!");
        if (StringKit.isBlank(input.clientId())) {
            throw new ProtocolException(ProtocolError.INVALID_CLIENT);
        }
        final CompletionStage<Optional<RegisteredClient>> resolved = Assert.notNull(
                dependencies.clients().resolve(context, input.clientId()),
                "RegisteredClient resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final RegisteredClient client = OAuth2Validator.client(optional, input.clientId());
            return OAuth2Validator.authenticate(context, client, input.clientSecret(), secrets);
        }).thenCompose(client -> {
            if (StringKit.isBlank(input.token())) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
            return input.hint() == TokenTypeHint.REFRESH_TOKEN ? refresh(context, client, input.token())
                    : access(context, client, input.token(), input.hint());
        });
    }

    /**
     * Revokes one client-owned access token or falls back to refresh processing without a hint.
     *
     * @param invocation tenant-scoped operation context
     * @param client     authenticated caller
     * @param token      opaque token
     * @param hint       optional type hint
     * @return completion stage
     * @throws IllegalArgumentException if a grant-store stage is {@code null}
     */
    private CompletionStage<Void> access(
            final Context invocation,
            final RegisteredClient client,
            final String token,
            final TokenTypeHint hint) {
        final String id = AuthorizationCodeFlow.tokenKey(invocation, "access", token);
        final CompletionStage<Optional<AuthorizationGrant>> found = Assert.notNull(
                dependencies.grants().find(invocation, id),
                "AuthorizationGrant-store find stage must be not null!");
        return found.thenCompose(optional -> {
            if (optional != null && optional.isPresent() && optional.get().clientId().equals(client.id())) {
                final CompletionStage<Boolean> revoked = Assert.notNull(
                        dependencies.grants().revoke(invocation, id),
                        "AuthorizationGrant-store revoke stage must be not null!");
                return revoked.thenApply(ignored -> null);
            }
            return hint == null ? refresh(invocation, client, token) : CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Revokes one active client-owned refresh token family.
     *
     * @param invocation tenant-scoped operation context
     * @param client     authenticated caller
     * @param token      opaque refresh token
     * @return completion stage
     * @throws IllegalArgumentException if a state-store stage is {@code null}
     */
    private CompletionStage<Void> refresh(final Context invocation, final RegisteredClient client, final String token) {
        final String tokenKey = AuthorizationCodeFlow.tokenKey(invocation, "refresh", token);
        final CompletionStage<Optional<byte[]>> found = Assert
                .notNull(states.get(invocation, tokenKey), "State-store read stage must be not null!");
        return found.thenCompose(optional -> {
            if (optional == null || optional.isEmpty() || optional.get().length == Normal._0
                    || optional.get()[Normal._0] == Normal._0) {
                return CompletableFuture.completedFuture(null);
            }
            final RefreshState state = AuthorizationCodeFlow.decodeRefresh(optional.get(), json, limits);
            final Instant now = OAuth2Support.now(clock);
            if (!state.clientId().equals(client.id()) || !state.expiresAt().isAfter(now)) {
                return CompletableFuture.completedFuture(null);
            }
            final Duration ttl = Duration.between(now, state.expiresAt());
            final CompletionStage<Boolean> family = Assert.notNull(
                    states.putIfAbsent(
                            invocation,
                            AuthorizationCodeFlow.tokenKey(invocation, "refresh-family", state.familyId()),
                            REVOKED_VALUE,
                            ttl),
                    "State-store create stage must be not null!");
            return family.thenCompose(ignored -> {
                final CompletionStage<Boolean> removed = Assert
                        .notNull(states.remove(invocation, tokenKey), "State-store remove stage must be not null!");
                return removed.thenApply(value -> null);
            });
        });
    }

}
