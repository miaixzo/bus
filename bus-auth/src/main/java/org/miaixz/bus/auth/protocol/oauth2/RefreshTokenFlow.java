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

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
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
 * Implements mandatory refresh-token rotation with concurrent reuse detection and family revocation.
 * <p>
 * Each active token state is atomically replaced by a retained spent marker at the state-store linearization point.
 * Exactly one exchange can perform that replacement. Any later observation of the spent marker creates a family
 * revocation marker that all remaining family members must check before rotating. The fixed family expiration is
 * preserved across every rotation, so refresh activity cannot extend the authorized session.
 * </p>
 *
 * @author Kimi Liu
 */
public final class RefreshTokenFlow {

    /**
     * Prefix distinguishing a retained spent state from a regular authenticated envelope.
     */
    private static final byte SPENT_PREFIX = Normal._0;

    /**
     * Non-sensitive value stored for a revoked refresh family.
     */
    private static final byte[] REVOKED_VALUE = { Normal._1 };

    /**
     * Trusted OAuth policy.
     */
    private final Policy policy;

    /**
     * Resolvers and grant persistence required by token issuance.
     */
    private final OAuth2Dependencies dependencies;

    /**
     * Atomic state storage for active tokens, spent markers, and family revocation.
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
     * Cryptographically secure source for replacement credentials.
     */
    private final SecureRandom random;

    /**
     * JSON provider used only through bounded state codecs.
     */
    private final JsonProvider json;

    /**
     * Closed parsing and allocation limits for persisted state.
     */
    private final Limits limits;

    /**
     * Creates one refresh-token rotation state machine.
     *
     * @param policy       trusted OAuth policy
     * @param dependencies non-null OAuth resolver and grant dependencies
     * @param states       non-null atomic state store
     * @param secrets      non-null client-secret resolver
     * @param clock        non-null security clock
     * @param random       non-null cryptographically secure random source
     * @param json         non-null JSON provider
     * @param limits       non-null parser and allocation limits
     * @throws IllegalArgumentException if any collaborator is {@code null}
     */
    public RefreshTokenFlow(final Policy policy, final OAuth2Dependencies dependencies, final StateStore states,
            final SecretResolver secrets, final Clock clock, final SecureRandom random, final JsonProvider json,
            final Limits limits) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.dependencies = Assert.notNull(dependencies, "OAuth dependencies must be not null!");
        this.states = Assert.notNull(states, "State store must be not null!");
        this.secrets = Assert.notNull(secrets, "Secret resolver must be not null!");
        this.clock = Assert.notNull(clock, "Clock must be not null!");
        this.random = Assert.notNull(random, "Secure random must be not null!");
        this.json = Assert.notNull(json, "JSON provider must be not null!");
        this.limits = Assert.notNull(limits, "Limits must be not null!");
    }

    /**
     * Rejects fields belonging to grants other than refresh-token rotation.
     *
     * @param request token request
     * @throws ProtocolException if a required field is absent or a foreign-grant field is present
     */
    private static void validate(final TokenRequest request) {
        if (request.grantType() != GrantType.REFRESH_TOKEN || StringKit.isBlank(request.clientId())
                || StringKit.isBlank(request.refreshToken()) || request.code() != null || request.redirectUri() != null
                || request.codeVerifier() != null || request.deviceCode() != null) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
    }

    /**
     * Wraps one active state in the retained spent-marker representation.
     *
     * @param active active state envelope
     * @return copied spent representation
     * @throws ArithmeticException if the envelope length cannot be represented
     */
    private static byte[] markSpent(final byte[] active) {
        final byte[] result = new byte[Math.addExact(active.length, Normal._1)];
        result[Normal._0] = SPENT_PREFIX;
        System.arraycopy(active, Normal._0, result, Normal._1, active.length);
        return result;
    }

    /**
     * Reports whether stored bytes represent a retained spent state.
     *
     * @param value stored state bytes
     * @return {@code true} for a spent representation
     */
    private static boolean isSpent(final byte[] value) {
        return value.length > Normal._1 && value[Normal._0] == SPENT_PREFIX;
    }

    /**
     * Extracts a copied active envelope from a spent representation.
     *
     * @param value spent state bytes
     * @return original active state envelope
     */
    private static byte[] active(final byte[] value) {
        return Arrays.copyOfRange(value, Normal._1, value.length);
    }

    /**
     * Derives one tenant-isolated family revocation key.
     *
     * @param invocation tenant-scoped operation context
     * @param familyId   refresh family identifier
     * @return protected family key
     */
    private static String familyKey(final Context invocation, final String familyId) {
        return AuthorizationCodeFlow.tokenKey(invocation, "refresh-family", familyId);
    }

    /**
     * Authenticates the client, rotates one refresh token, and rejects every reuse.
     *
     * @param invocation tenant-scoped operation context
     * @param request    refresh-token request
     * @return stage containing rotated access and refresh tokens
     * @throws IllegalArgumentException if a required input or dependency stage is {@code null}
     * @throws ProtocolException        if the request is not a valid enabled refresh-token grant
     */
    public CompletionStage<TokenResponse> exchange(final Context invocation, final TokenRequest request) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final TokenRequest input = Assert.notNull(request, "Token request must be not null!");
        OAuth2Validator.grant(input.grantType(), policy.grants());
        validate(input);
        final CompletionStage<Optional<RegisteredClient>> resolved = Assert.notNull(
                dependencies.clients().resolve(context, input.clientId()),
                "RegisteredClient resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final RegisteredClient client = OAuth2Validator.client(optional, input.clientId());
            return OAuth2Validator.authenticate(context, client, input.clientSecret(), secrets);
        }).thenCompose(client -> rotate(context, client, input));
    }

    /**
     * Performs one compare-and-set rotation after client authentication.
     *
     * @param invocation tenant-scoped operation context
     * @param client     authenticated client
     * @param request    validated token request
     * @return stage containing rotated tokens
     * @throws IllegalArgumentException if a state-store stage is {@code null}
     */
    private CompletionStage<TokenResponse> rotate(
            final Context invocation,
            final RegisteredClient client,
            final TokenRequest request) {
        final String tokenKey = AuthorizationCodeFlow.tokenKey(invocation, "refresh", request.refreshToken());
        final CompletionStage<Optional<byte[]>> loaded = Assert
                .notNull(states.get(invocation, tokenKey), "State-store read stage must be not null!");
        return loaded.thenCompose(optional -> {
            if (optional == null || optional.isEmpty()) {
                return OAuth2Support.failed(ProtocolError.INVALID_GRANT);
            }
            final byte[] current = optional.get();
            if (isSpent(current)) {
                return reuse(invocation, active(current));
            }
            final RefreshState state = AuthorizationCodeFlow.decodeRefresh(current, json, limits);
            final Instant now = OAuth2Support.now(clock);
            if (!state.clientId().equals(client.id()) || !state.expiresAt().isAfter(now)) {
                return OAuth2Support.failed(ProtocolError.INVALID_GRANT);
            }
            final Set<String> scopes = request.scopes().isEmpty() ? state.scopes()
                    : OAuth2Validator.scopes(request.scopes(), state.scopes(), limits);
            OAuth2Validator.scopes(scopes, policy.scopes(), limits);
            final Duration remaining = Duration.between(now, state.expiresAt());
            final String familyKey = familyKey(invocation, state.familyId());
            final CompletionStage<Optional<byte[]>> revoked = Assert
                    .notNull(states.get(invocation, familyKey), "State-store read stage must be not null!");
            return revoked.thenCompose(marker -> {
                if (marker != null && marker.isPresent()) {
                    return OAuth2Support.failed(ProtocolError.INVALID_GRANT);
                }
                final CompletionStage<Boolean> replaced = Assert.notNull(
                        states.compareAndSet(invocation, tokenKey, current, markSpent(current), remaining),
                        "State-store replace stage must be not null!");
                return replaced
                        .thenCompose(
                                success -> Boolean.TRUE.equals(success)
                                        ? AuthorizationCodeFlow.issueTokens(
                                                invocation,
                                                state.subjectId(),
                                                state.clientId(),
                                                scopes,
                                                true,
                                                state.familyId(),
                                                policy,
                                                dependencies,
                                                states,
                                                clock,
                                                random,
                                                json,
                                                limits)
                                        : collision(invocation, tokenKey));
            });
        });
    }

    /**
     * Resolves a lost compare-and-set race and records family reuse when observable.
     *
     * @param invocation tenant-scoped operation context
     * @param tokenKey   protected refresh-token key
     * @return failed token stage
     * @throws IllegalArgumentException if the state-store stage is {@code null}
     */
    private CompletionStage<TokenResponse> collision(final Context invocation, final String tokenKey) {
        final CompletionStage<Optional<byte[]>> loaded = Assert
                .notNull(states.get(invocation, tokenKey), "State-store read stage must be not null!");
        return loaded.thenCompose(
                optional -> optional != null && optional.isPresent() && isSpent(optional.get())
                        ? reuse(invocation, active(optional.get()))
                        : OAuth2Support.failed(ProtocolError.INVALID_GRANT));
    }

    /**
     * Creates the family revocation marker retained until the original family expiration.
     *
     * @param invocation tenant-scoped operation context
     * @param envelope   original active refresh state
     * @return failed token stage after revocation recording
     * @throws IllegalArgumentException if the state-store stage is {@code null}
     */
    private CompletionStage<TokenResponse> reuse(final Context invocation, final byte[] envelope) {
        final RefreshState state = AuthorizationCodeFlow.decodeRefresh(envelope, json, limits);
        final Instant now = OAuth2Support.now(clock);
        if (!state.expiresAt().isAfter(now)) {
            return OAuth2Support.failed(ProtocolError.INVALID_GRANT);
        }
        final CompletionStage<Boolean> recorded = Assert.notNull(
                states.putIfAbsent(
                        invocation,
                        familyKey(invocation, state.familyId()),
                        REVOKED_VALUE,
                        Duration.between(now, state.expiresAt())),
                "State-store create stage must be not null!");
        return recorded.thenCompose(ignored -> OAuth2Support.failed(ProtocolError.INVALID_GRANT));
    }

}
