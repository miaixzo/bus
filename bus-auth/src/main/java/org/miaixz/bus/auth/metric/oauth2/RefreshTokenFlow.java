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
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Client;
import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.OAuth2.*;
import org.miaixz.bus.auth.metric.oauth2.AuthorizationCodeFlow.RefreshState;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;

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
     * Validated authentication runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one refresh-token rotation state machine.
     *
     * @param policy  trusted OAuth policy
     * @param runtime validated authentication runtime
     */
    public RefreshTokenFlow(final Policy policy, final Runtime runtime) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.runtime = Assert.notNull(runtime, "Authentication runtime must be not null!");
    }

    /**
     * Rejects fields belonging to grants other than refresh-token rotation.
     *
     * @param request token request
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
    private static String familyKey(final Invocation invocation, final String familyId) {
        return AuthorizationCodeFlow.tokenKey(invocation, "refresh-family", familyId);
    }

    /**
     * Creates an already failed invalid-grant stage.
     *
     * @param <T> stage result type
     * @return failed stage
     */
    private static <T> CompletionStage<T> failed() {
        return CompletableFuture.failedFuture(new ProtocolException(ProtocolError.INVALID_GRANT));
    }

    /**
     * Authenticates the client, rotates one refresh token, and rejects every reuse.
     *
     * @param invocation tenant-scoped operation context
     * @param request    refresh-token request
     * @return stage containing rotated access and refresh tokens
     */
    public CompletionStage<TokenResponse> exchange(final Invocation invocation, final TokenRequest request) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final TokenRequest input = Assert.notNull(request, "Token request must be not null!");
        OAuth2Validator.grant(input.grantType(), policy.grants());
        validate(input);
        final CompletionStage<Optional<Client>> resolved = Assert.notNull(
                runtime.clients().resolve(context, input.clientId()),
                "Client resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final Client client = OAuth2Validator.client(optional, input.clientId());
            return OAuth2Validator.authenticate(context, client, input.clientSecret(), runtime);
        }).thenCompose(client -> rotate(context, client, input));
    }

    /**
     * Performs one compare-and-set rotation after client authentication.
     *
     * @param invocation tenant-scoped operation context
     * @param client     authenticated client
     * @param request    validated token request
     * @return stage containing rotated tokens
     */
    private CompletionStage<TokenResponse> rotate(
            final Invocation invocation,
            final Client client,
            final TokenRequest request) {
        final String tokenKey = AuthorizationCodeFlow.tokenKey(invocation, "refresh", request.refreshToken());
        final CompletionStage<Optional<byte[]>> loaded = Assert
                .notNull(runtime.states().get(invocation, tokenKey), "State-store read stage must be not null!");
        return loaded.thenCompose(optional -> {
            if (optional == null || optional.isEmpty()) {
                return failed();
            }
            final byte[] current = optional.get();
            if (isSpent(current)) {
                return reuse(invocation, active(current));
            }
            final RefreshState state = AuthorizationCodeFlow.decodeRefresh(current, runtime);
            final Instant now = clock();
            if (!state.clientId().equals(client.id()) || !state.expiresAt().isAfter(now)) {
                return failed();
            }
            final Set<String> scopes = request.scopes().isEmpty() ? state.scopes()
                    : OAuth2Validator.scopes(request.scopes(), state.scopes(), runtime.limits());
            OAuth2Validator.scopes(scopes, policy.scopes(), runtime.limits());
            final Duration remaining = Duration.between(now, state.expiresAt());
            final String familyKey = familyKey(invocation, state.familyId());
            final CompletionStage<Optional<byte[]>> revoked = Assert
                    .notNull(runtime.states().get(invocation, familyKey), "State-store read stage must be not null!");
            return revoked.thenCompose(marker -> {
                if (marker != null && marker.isPresent()) {
                    return failed();
                }
                final CompletionStage<Boolean> replaced = Assert.notNull(
                        runtime.states().compareAndSet(invocation, tokenKey, current, markSpent(current), remaining),
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
                                                runtime)
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
     */
    private CompletionStage<TokenResponse> collision(final Invocation invocation, final String tokenKey) {
        final CompletionStage<Optional<byte[]>> loaded = Assert
                .notNull(runtime.states().get(invocation, tokenKey), "State-store read stage must be not null!");
        return loaded.thenCompose(
                optional -> optional != null && optional.isPresent() && isSpent(optional.get())
                        ? reuse(invocation, active(optional.get()))
                        : failed());
    }

    /**
     * Creates the family revocation marker retained until the original family expiration.
     *
     * @param invocation tenant-scoped operation context
     * @param envelope   original active refresh state
     * @return failed token stage after revocation recording
     */
    private CompletionStage<TokenResponse> reuse(final Invocation invocation, final byte[] envelope) {
        final RefreshState state = AuthorizationCodeFlow.decodeRefresh(envelope, runtime);
        final Instant now = clock();
        if (!state.expiresAt().isAfter(now)) {
            return failed();
        }
        final CompletionStage<Boolean> recorded = Assert.notNull(
                runtime.states().putIfAbsent(
                        invocation,
                        familyKey(invocation, state.familyId()),
                        REVOKED_VALUE,
                        Duration.between(now, state.expiresAt())),
                "State-store create stage must be not null!");
        return recorded.thenCompose(ignored -> failed());
    }

    /**
     * Returns the non-null security-clock value.
     *
     * @return current security instant
     */
    private Instant clock() {
        return Assert.notNull(runtime.clock().now(), "Clock value must be not null!");
    }

}
