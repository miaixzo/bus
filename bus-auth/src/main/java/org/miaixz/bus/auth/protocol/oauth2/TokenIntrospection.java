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

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationCodeFlow.RefreshState;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.*;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

/**
 * Authenticates OAuth token introspection callers and discloses only client-owned active state.
 * <p>
 * Opaque tokens are converted to tenant-isolated hashes before lookup. Unknown, expired, rotated, revoked-family, and
 * cross-client tokens all produce the identical minimal inactive response. The implementation never returns token
 * material, internal lookup keys, refresh family identifiers, or backend diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
public final class TokenIntrospection {

    /**
     * Resolvers and authorization-grant persistence used for access-token lookup.
     */
    private final OAuth2Dependencies dependencies;

    /**
     * Atomic state store used for refresh-token and family-revocation lookup.
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
     * Creates one token introspection service.
     *
     * @param dependencies non-null OAuth resolver and grant dependencies
     * @param states       non-null atomic state store
     * @param secrets      non-null client-secret resolver
     * @param clock        non-null security clock
     * @param json         non-null JSON provider
     * @param limits       non-null parser and allocation limits
     * @throws IllegalArgumentException if any collaborator is {@code null}
     */
    public TokenIntrospection(final OAuth2Dependencies dependencies, final StateStore states,
            final SecretResolver secrets, final Clock clock, final JsonProvider json, final Limits limits) {
        this.dependencies = Assert.notNull(dependencies, "OAuth dependencies must be not null!");
        this.states = Assert.notNull(states, "State store must be not null!");
        this.secrets = Assert.notNull(secrets, "Secret resolver must be not null!");
        this.clock = Assert.notNull(clock, "Clock must be not null!");
        this.json = Assert.notNull(json, "JSON provider must be not null!");
        this.limits = Assert.notNull(limits, "Limits must be not null!");
    }

    /**
     * Creates the sole minimal inactive response.
     *
     * @return inactive response
     */
    private static IntrospectionResponse inactive() {
        return new IntrospectionResponse(false, null, null, Set.of(), null, null, null);
    }

    /**
     * Authenticates the caller and returns minimal authorized token state.
     *
     * @param invocation tenant-scoped operation context
     * @param request    introspection request
     * @return stage containing active or inactive state
     * @throws IllegalArgumentException if a required input or dependency stage is {@code null}
     * @throws ProtocolException        if client authentication or request syntax fails
     */
    public CompletionStage<IntrospectionResponse> introspect(
            final Context invocation,
            final IntrospectionRequest request) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final IntrospectionRequest input = Assert.notNull(request, "Introspection request must be not null!");
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
     * Introspects one access token and optionally falls back to refresh state when no hint was supplied.
     *
     * @param invocation tenant-scoped operation context
     * @param client     authenticated caller
     * @param token      opaque token
     * @param hint       optional type hint
     * @return stage containing minimal token state
     * @throws IllegalArgumentException if the grant-store stage is {@code null}
     */
    private CompletionStage<IntrospectionResponse> access(
            final Context invocation,
            final RegisteredClient client,
            final String token,
            final TokenTypeHint hint) {
        final String id = AuthorizationCodeFlow.tokenKey(invocation, "access", token);
        final CompletionStage<Optional<AuthorizationGrant>> found = Assert.notNull(
                dependencies.grants().find(invocation, id),
                "AuthorizationGrant-store find stage must be not null!");
        return found.thenCompose(optional -> {
            if (optional != null && optional.isPresent()) {
                final AuthorizationGrant grant = optional.get();
                final Instant now = OAuth2Support.now(clock);
                if (grant.clientId().equals(client.id()) && grant.expiresAt().isAfter(now)) {
                    return CompletableFuture.completedFuture(
                            new IntrospectionResponse(true, grant.clientId(), grant.subjectId(), grant.scopes(), null,
                                    grant.expiresAt(), TokenType.BEARER));
                }
                return CompletableFuture.completedFuture(inactive());
            }
            return hint == null ? refresh(invocation, client, token) : CompletableFuture.completedFuture(inactive());
        });
    }

    /**
     * Introspects one active, unrevoked refresh-family member.
     *
     * @param invocation tenant-scoped operation context
     * @param client     authenticated caller
     * @param token      opaque refresh token
     * @return stage containing minimal token state
     * @throws IllegalArgumentException if a state-store stage is {@code null}
     */
    private CompletionStage<IntrospectionResponse> refresh(
            final Context invocation,
            final RegisteredClient client,
            final String token) {
        final String key = AuthorizationCodeFlow.tokenKey(invocation, "refresh", token);
        final CompletionStage<Optional<byte[]>> found = Assert
                .notNull(states.get(invocation, key), "State-store read stage must be not null!");
        return found.thenCompose(optional -> {
            if (optional == null || optional.isEmpty() || optional.get().length == Normal._0
                    || optional.get()[Normal._0] == Normal._0) {
                return CompletableFuture.completedFuture(inactive());
            }
            final RefreshState state = AuthorizationCodeFlow.decodeRefresh(optional.get(), json, limits);
            if (!state.clientId().equals(client.id()) || !state.expiresAt().isAfter(OAuth2Support.now(clock))) {
                return CompletableFuture.completedFuture(inactive());
            }
            final CompletionStage<Optional<byte[]>> revoked = Assert.notNull(
                    states.get(
                            invocation,
                            AuthorizationCodeFlow.tokenKey(invocation, "refresh-family", state.familyId())),
                    "State-store read stage must be not null!");
            return revoked.thenApply(
                    marker -> marker != null && marker.isPresent() ? inactive()
                            : new IntrospectionResponse(true, state.clientId(), state.subjectId(), state.scopes(), null,
                                    state.expiresAt(), null));
        });
    }

}
