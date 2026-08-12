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
package org.miaixz.bus.auth.metric.oauth2;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Client;
import org.miaixz.bus.auth.metric.AuthMetric.Grant;
import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.OAuth2.ProtocolError;
import org.miaixz.bus.auth.metric.OAuth2.RevocationRequest;
import org.miaixz.bus.auth.metric.OAuth2.TokenTypeHint;
import org.miaixz.bus.auth.metric.oauth2.AuthorizationCodeFlow.RefreshState;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;

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
     * Validated authentication runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one token revocation service.
     *
     * @param runtime validated authentication runtime
     */
    public TokenRevocation(final Runtime runtime) {
        this.runtime = Assert.notNull(runtime, "Authentication runtime must be not null!");
    }

    /**
     * Authenticates the caller and idempotently revokes its presented token.
     *
     * @param invocation tenant-scoped operation context
     * @param request    revocation request
     * @return stage completed after revocation processing
     */
    public CompletionStage<Void> revoke(final Invocation invocation, final RevocationRequest request) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final RevocationRequest input = Assert.notNull(request, "Revocation request must be not null!");
        if (StringKit.isBlank(input.clientId())) {
            throw new ProtocolException(ProtocolError.INVALID_CLIENT);
        }
        final CompletionStage<Optional<Client>> resolved = Assert.notNull(
                runtime.clients().resolve(context, input.clientId()),
                "Client resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final Client client = OAuth2Validator.client(optional, input.clientId());
            return OAuth2Validator.authenticate(context, client, input.clientSecret(), runtime);
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
     */
    private CompletionStage<Void> access(
            final Invocation invocation,
            final Client client,
            final String token,
            final TokenTypeHint hint) {
        final String id = AuthorizationCodeFlow.tokenKey(invocation, "access", token);
        final CompletionStage<Optional<Grant>> found = Assert
                .notNull(runtime.grants().find(invocation, id), "Grant-store find stage must be not null!");
        return found.thenCompose(optional -> {
            if (optional != null && optional.isPresent() && optional.get().clientId().equals(client.id())) {
                final CompletionStage<Boolean> revoked = Assert
                        .notNull(runtime.grants().revoke(invocation, id), "Grant-store revoke stage must be not null!");
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
     */
    private CompletionStage<Void> refresh(final Invocation invocation, final Client client, final String token) {
        final String tokenKey = AuthorizationCodeFlow.tokenKey(invocation, "refresh", token);
        final CompletionStage<Optional<byte[]>> found = Assert
                .notNull(runtime.states().get(invocation, tokenKey), "State-store read stage must be not null!");
        return found.thenCompose(optional -> {
            if (optional == null || optional.isEmpty() || optional.get().length == Normal._0
                    || optional.get()[Normal._0] == Normal._0) {
                return CompletableFuture.completedFuture(null);
            }
            final RefreshState state = AuthorizationCodeFlow.decodeRefresh(optional.get(), runtime);
            final Instant now = clock();
            if (!state.clientId().equals(client.id()) || !state.expiresAt().isAfter(now)) {
                return CompletableFuture.completedFuture(null);
            }
            final Duration ttl = Duration.between(now, state.expiresAt());
            final CompletionStage<Boolean> family = Assert.notNull(
                    runtime.states().putIfAbsent(
                            invocation,
                            AuthorizationCodeFlow.tokenKey(invocation, "refresh-family", state.familyId()),
                            REVOKED_VALUE,
                            ttl),
                    "State-store create stage must be not null!");
            return family.thenCompose(ignored -> {
                final CompletionStage<Boolean> removed = Assert.notNull(
                        runtime.states().remove(invocation, tokenKey),
                        "State-store remove stage must be not null!");
                return removed.thenApply(value -> null);
            });
        });
    }

    /**
     * Returns the non-null security-clock instant.
     *
     * @return current instant
     */
    private Instant clock() {
        return Assert.notNull(runtime.clock().now(), "Clock value must be not null!");
    }

}
