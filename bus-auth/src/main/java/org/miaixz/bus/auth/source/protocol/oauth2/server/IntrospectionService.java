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
package org.miaixz.bus.auth.source.protocol.oauth2.server;

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
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.source.SourceServices;
import org.miaixz.bus.auth.source.protocol.oauth2.IntrospectionRequest;
import org.miaixz.bus.auth.source.protocol.oauth2.IntrospectionResponse;
import org.miaixz.bus.auth.source.protocol.oauth2.Scope;
import org.miaixz.bus.auth.source.protocol.oauth2.TokenType;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Resolves RFC 7662 token activity from the atomic access-token cache.
 *
 * @author Kimi Liu
 */
public class IntrospectionService {

    /**
     * Source identifier used to isolate opaque token digests.
     */
    private final String sourceId;

    /**
     * Validated authorization-server options supplying the published issuer.
     */
    private final OAuth2ServerOptions options;

    /**
     * Runtime dependencies including the access-token cache.
     */
    private final SourceServices services;

    /**
     * Creates a token introspection service for one compiled server-role Source runtime.
     *
     * @param sourceId compiled server-role Source identifier
     * @param options  validated authorization-server options
     * @param services capability-limited Source services
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public IntrospectionService(final String sourceId, final OAuth2ServerOptions options,
            final SourceServices services) {
        this.sourceId = Assert.notBlank(sourceId, "OAuth 2.x Source id must not be blank");
        this.options = Assert.notNull(options, "OAuth 2.x authorization server options must not be null");
        this.services = Assert.notNull(services, "OAuth 2.x execution services must not be null");
    }

    /**
     * Creates the only valid inactive RFC 7662 response shape.
     *
     * @return response containing only {@code active=false}
     */
    private static IntrospectionResponse inactive() {
        return new IntrospectionResponse(false, Optional.empty(), new JsonValue.ObjectValue(Map.of()));
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
     * Returns active token metadata or the mandatory indistinguishable inactive response.
     *
     * @param request standard opaque-token introspection request
     * @param context invocation context carrying an authenticated client identifier
     * @param timeout shared end-to-end timeout
     * @return asynchronous standard introspection response outcome
     */
    public CompletionStage<Outcome<IntrospectionResponse>> introspect(
            final IntrospectionRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x introspection request must not be null");
        Assert.notNull(context, "OAuth 2.x introspection context must not be null");
        Assert.notNull(timeout, "OAuth 2.x introspection timeout must not be null");
        if (context.clientId().isEmpty()) {
            return completed(
                    Outcome.rejected(
                            failure(ErrorCode._401, "OAuth 2.x introspection requires an authenticated client")));
        }
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(failure(ErrorCode._408, "OAuth 2.x introspection has no remaining timeout")));
        }
        final CompletionStage<ExpiringValue<AccessTokenCache.Entry>> lookup;
        try {
            lookup = services.accessTokenCache().find(key(request.token()));
        } catch (RuntimeException exception) {
            return completed(Outcome.failed(failure(ErrorCode._500, "OAuth 2.x access token lookup failed")));
        }
        return lookup.handle((stored, thrown) -> new CacheResult(stored, thrown)).thenCompose(result -> {
            if (result.failure() != null) {
                return completed(
                        Outcome.<IntrospectionResponse>failed(
                                failure(ErrorCode._500, "OAuth 2.x access token lookup failed")));
            }
            final ExpiringValue<AccessTokenCache.Entry> stored = result.value();
            final Instant now = timeout.clock().now();
            if (stored == null || !stored.expiresAt().isAfter(now) || !sourceId.equals(stored.value().sourceId())) {
                return completed(Outcome.succeeded(inactive()));
            }
            final CompletionStage<ExpiringValue<AuthorizationCache.Entry>> authorization;
            try {
                authorization = services.authorizationCache()
                        .find(AuthorizationCache.key(sourceId, stored.value().authorizationId()));
            } catch (RuntimeException exception) {
                return completed(Outcome.failed(failure(ErrorCode._500, "OAuth 2.x authorization lookup failed")));
            }
            return authorization.handle((state, thrown) -> {
                if (thrown != null) {
                    return Outcome.<IntrospectionResponse>failed(
                            failure(ErrorCode._500, "OAuth 2.x authorization lookup failed"));
                }
                if (state == null || !state.expiresAt().isAfter(now)
                        || state.value().status() != AuthorizationCache.Status.ACTIVE
                        || !sourceId.equals(state.value().sourceId())
                        || !stored.value().clientId().equals(state.value().clientId())) {
                    return Outcome.succeeded(inactive());
                }
                return Outcome.succeeded(active(stored));
            });
        });
    }

    /**
     * Maps one active stored access token to the registered RFC 7662 members.
     *
     * @param stored active isolated access-token state
     * @return active standard introspection response
     */
    private IntrospectionResponse active(final ExpiringValue<AccessTokenCache.Entry> stored) {
        final AccessTokenCache.Entry entry = stored.value();
        final Map<String, JsonValue> extensions = entry.actorSubjectId().isEmpty() ? Map.of()
                : Map.of(
                        "act",
                        new JsonValue.ObjectValue(Map
                                .of(JwtClaims.SUBJECT, new JsonValue.StringValue(entry.actorSubjectId().getOrNull()))));
        final IntrospectionResponse.TokenMetadata metadata = new IntrospectionResponse.TokenMetadata(
                entry.scope().isEmpty() ? Optional.empty() : Optional.of(new Scope(entry.scope())),
                Optional.of(entry.clientId()), Optional.empty(), Optional.of(TokenType.BEARER),
                Optional.of(stored.expiresAt().getEpochSecond()), Optional.empty(), Optional.empty(),
                Optional.of(
                        entry.openIdBinding().isEmpty() ? entry.subjectId()
                                : entry.openIdBinding().getOrNull().subject().getOrNull()),
                entry.audience(), Optional.of(options.issuer()), Optional.empty());
        return new IntrospectionResponse(true, Optional.of(metadata), new JsonValue.ObjectValue(extensions));
    }

    /**
     * Produces a Source-isolated irreversible lookup key for an opaque token.
     *
     * @param token opaque token value
     * @return SHA-256 hexadecimal cache key
     */
    private String key(final String token) {
        return Builder.sha256Hex(sourceId + Symbol.C_NUL + token);
    }

    /**
     * Captures an access-token cache lookup and any asynchronous dependency failure.
     *
     * @param value   cached token validation state, or {@code null}
     * @param failure asynchronous cache failure, or {@code null}
     * @author Kimi Liu
     */
    private record CacheResult(ExpiringValue<AccessTokenCache.Entry> value, Throwable failure) {

    }

}
