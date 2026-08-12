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

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Client;
import org.miaixz.bus.auth.metric.AuthMetric.Grant;
import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.OAuth2.*;
import org.miaixz.bus.auth.metric.oauth2.AuthorizationCodeFlow.RefreshState;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;

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
     * Validated authentication runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one token introspection service.
     *
     * @param runtime validated authentication runtime
     */
    public TokenIntrospection(final Runtime runtime) {
        this.runtime = Assert.notNull(runtime, "Authentication runtime must be not null!");
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
     */
    public CompletionStage<IntrospectionResponse> introspect(
            final Invocation invocation,
            final IntrospectionRequest request) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final IntrospectionRequest input = Assert.notNull(request, "Introspection request must be not null!");
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
     * Introspects one access token and optionally falls back to refresh state when no hint was supplied.
     *
     * @param invocation tenant-scoped operation context
     * @param client     authenticated caller
     * @param token      opaque token
     * @param hint       optional type hint
     * @return stage containing minimal token state
     */
    private CompletionStage<IntrospectionResponse> access(
            final Invocation invocation,
            final Client client,
            final String token,
            final TokenTypeHint hint) {
        final String id = AuthorizationCodeFlow.tokenKey(invocation, "access", token);
        final CompletionStage<Optional<Grant>> found = Assert
                .notNull(runtime.grants().find(invocation, id), "Grant-store find stage must be not null!");
        return found.thenCompose(optional -> {
            if (optional != null && optional.isPresent()) {
                final Grant grant = optional.get();
                final Instant now = clock();
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
     */
    private CompletionStage<IntrospectionResponse> refresh(
            final Invocation invocation,
            final Client client,
            final String token) {
        final String key = AuthorizationCodeFlow.tokenKey(invocation, "refresh", token);
        final CompletionStage<Optional<byte[]>> found = Assert
                .notNull(runtime.states().get(invocation, key), "State-store read stage must be not null!");
        return found.thenCompose(optional -> {
            if (optional == null || optional.isEmpty() || optional.get().length == Normal._0
                    || optional.get()[Normal._0] == Normal._0) {
                return CompletableFuture.completedFuture(inactive());
            }
            final RefreshState state = AuthorizationCodeFlow.decodeRefresh(optional.get(), runtime);
            if (!state.clientId().equals(client.id()) || !state.expiresAt().isAfter(clock())) {
                return CompletableFuture.completedFuture(inactive());
            }
            final CompletionStage<Optional<byte[]>> revoked = Assert.notNull(
                    runtime.states().get(
                            invocation,
                            AuthorizationCodeFlow.tokenKey(invocation, "refresh-family", state.familyId())),
                    "State-store read stage must be not null!");
            return revoked.thenApply(
                    marker -> marker != null && marker.isPresent() ? inactive()
                            : new IntrospectionResponse(true, state.clientId(), state.subjectId(), state.scopes(), null,
                                    state.expiresAt(), null));
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
