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

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.OAuth2.*;
import org.miaixz.bus.auth.metric.shared.json.StrictJsonReader;
import org.miaixz.bus.auth.metric.shared.security.ReplayKey;
import org.miaixz.bus.auth.metric.shared.state.StateEnvelopeCodec;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Implements the authorization-code grant with mandatory S256 PKCE and a one-time 120-second code.
 * <p>
 * Authorization validates the registered client, exact redirect, requested and approved scopes, and product decision
 * before atomically storing an opaque code state. Exchange atomically takes that state before client, redirect, and
 * PKCE verification, so concurrent or repeated exchanges have exactly one possible winner. Issued access and refresh
 * tokens are opaque random values; only tenant-isolated hashes and bounded state are persisted.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AuthorizationCodeFlow {

    /**
     * Exact authorization-code lifetime.
     */
    private static final Duration CODE_LIFETIME = Duration.ofSeconds(120);

    /**
     * Random byte count for authorization and token credentials.
     */
    private static final int CREDENTIAL_BYTES = Normal._32;

    /**
     * Trusted OAuth policy.
     */
    private final Policy policy;

    /**
     * Validated authentication runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one authorization-code state machine.
     *
     * @param policy  trusted OAuth policy
     * @param runtime validated authentication runtime
     */
    public AuthorizationCodeFlow(final Policy policy, final Runtime runtime) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.runtime = Assert.notNull(runtime, "Authentication runtime must be not null!");
    }

    /**
     * Issues an access token and optionally creates one rotating refresh-family member.
     *
     * @param invocation     tenant-scoped operation context
     * @param subjectId      authorized subject identifier
     * @param clientId       authorized client identifier
     * @param scopes         granted scopes
     * @param includeRefresh whether to create a refresh token
     * @param existingFamily optional existing refresh family identifier
     * @param policy         trusted OAuth policy
     * @param runtime        validated authentication runtime
     * @return stage containing issued opaque tokens
     */
    static CompletionStage<TokenResponse> issueTokens(
            final Invocation invocation,
            final String subjectId,
            final String clientId,
            final Set<String> scopes,
            final boolean includeRefresh,
            final String existingFamily,
            final Policy policy,
            final Runtime runtime) {
        final Instant now = clock(runtime);
        final Instant accessExpiration = add(now, policy.accessTokenLifetime());
        final String accessToken = credential(runtime);
        final String accessId = tokenKey(invocation, "access", accessToken);
        final String family = includeRefresh ? StringKit.isBlank(existingFamily) ? credential(runtime) : existingFamily
                : null;
        final Grant grant = new Grant(accessId, subjectId, clientId, scopes, accessExpiration,
                family == null ? Map.of("token_type", "access") : Map.of("token_type", "access", "family_id", family));
        if (!includeRefresh) {
            final CompletionStage<Void> saved = Assert
                    .notNull(runtime.grants().save(invocation, grant), "Grant-store save stage must be not null!");
            return saved.thenApply(ignored -> response(accessToken, null, policy.accessTokenLifetime(), scopes));
        }
        final String refreshToken = credential(runtime);
        final Instant familyExpiration = add(now, policy.refreshTokenLifetime());
        final RefreshState refresh = new RefreshState(family, subjectId, clientId, scopes, familyExpiration);
        final String refreshKey = tokenKey(invocation, "refresh", refreshToken);
        final Duration refreshTtl = Duration.between(now, familyExpiration);
        final CompletionStage<Boolean> stored = Assert.notNull(
                runtime.states().putIfAbsent(invocation, refreshKey, encodeRefresh(refresh, runtime), refreshTtl),
                "State-store create stage must be not null!");
        return stored.thenCompose(created -> {
            if (!Boolean.TRUE.equals(created)) {
                reject(ProtocolError.TEMPORARILY_UNAVAILABLE);
            }
            final CompletionStage<Void> saved = Assert
                    .notNull(runtime.grants().save(invocation, grant), "Grant-store save stage must be not null!");
            final CompletionStage<TokenResponse> issued = saved
                    .thenApply(ignored -> response(accessToken, refreshToken, policy.accessTokenLifetime(), scopes));
            return issued.exceptionallyCompose(failure -> {
                final CompletionStage<Boolean> removed = Assert.notNull(
                        runtime.states().remove(invocation, refreshKey),
                        "State-store remove stage must be not null!");
                return removed.handle((removedValue, cleanupFailure) -> {
                    if (cleanupFailure != null) {
                        failure.addSuppressed(cleanupFailure);
                    }
                    throw propagate(failure);
                });
            });
        });
    }

    /**
     * Derives the tenant-isolated access or refresh token lookup key.
     *
     * @param invocation tenant-scoped operation context
     * @param kind       access or refresh token kind
     * @param token      opaque token
     * @return protected lookup key
     */
    static String tokenKey(final Invocation invocation, final String kind, final String token) {
        return ReplayKey.derive(invocation.tenantId(), "oauth2", kind, token);
    }

    /**
     * Decodes and validates one refresh-family state envelope.
     *
     * @param envelope stored state envelope
     * @param runtime  validated authentication runtime
     * @return immutable refresh state
     */
    static RefreshState decodeRefresh(final byte[] envelope, final Runtime runtime) {
        final Map<String, Object> values = object(StateEnvelopeCodec.decode(envelope), runtime);
        final String family = string(values, "family");
        final String subject = string(values, "subject");
        final String client = string(values, "client");
        final Set<String> scopes = scopeSet(text(values, "scopes"));
        final Instant expiresAt = instant(values, "expires_at");
        return new RefreshState(family, subject, client, scopes, expiresAt);
    }

    /**
     * Encodes one refresh-family state into the common authenticated envelope.
     *
     * @param state   refresh state
     * @param runtime validated authentication runtime
     * @return encoded state envelope
     */
    static byte[] encodeRefresh(final RefreshState state, final Runtime runtime) {
        final Map<String, Object> values = Map.of(
                "family",
                state.familyId(),
                "subject",
                state.subjectId(),
                "client",
                state.clientId(),
                "scopes",
                String.join(Symbol.SPACE, state.scopes()),
                "expires_at",
                state.expiresAt().getEpochSecond());
        return envelope(values, runtime);
    }

    /**
     * Encodes one authorization-code state.
     *
     * @param state   immutable code state
     * @param runtime validated authentication runtime
     * @return encoded state envelope
     */
    private static byte[] encodeCode(final CodeState state, final Runtime runtime) {
        final Map<String, Object> values = Map.of(
                "client",
                state.clientId(),
                "subject",
                state.subjectId(),
                "redirect",
                state.redirectUri(),
                "scopes",
                String.join(Symbol.SPACE, state.scopes()),
                "challenge",
                state.codeChallenge());
        return envelope(values, runtime);
    }

    /**
     * Decodes one authorization-code state.
     *
     * @param envelope stored state envelope
     * @param runtime  validated authentication runtime
     * @return validated code state
     */
    private static CodeState decodeCode(final byte[] envelope, final Runtime runtime) {
        final Map<String, Object> values = object(StateEnvelopeCodec.decode(envelope), runtime);
        return new CodeState(string(values, "client"), string(values, "subject"), string(values, "redirect"),
                scopeSet(text(values, "scopes")), string(values, "challenge"));
    }

    /**
     * Encodes one bounded JSON object into the common state envelope.
     *
     * @param values  state values
     * @param runtime validated authentication runtime
     * @return encoded envelope
     */
    private static byte[] envelope(final Map<String, Object> values, final Runtime runtime) {
        final byte[] json = runtime.json().write(values);
        if (json == null || json.length == Normal._0 || json.length > runtime.limits().maxJsonBytes()) {
            reject(ProtocolError.TEMPORARILY_UNAVAILABLE);
        }
        return StateEnvelopeCodec.encode(json);
    }

    /**
     * Reads one strict JSON state object.
     *
     * @param json    decoded envelope payload
     * @param runtime validated authentication runtime
     * @return string-keyed state object
     */
    private static Map<String, Object> object(final byte[] json, final Runtime runtime) {
        final Object value = new StrictJsonReader(runtime.json(), runtime.limits()).read(json, Map.class);
        if (!(value instanceof Map<?, ?>)) {
            reject(ProtocolError.INVALID_GRANT);
        }
        final Map<?, ?> source = (Map<?, ?>) value;
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (final Map.Entry<?, ?> entry : source.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                reject(ProtocolError.INVALID_GRANT);
            }
            result.put((String) entry.getKey(), entry.getValue());
        }
        return Map.copyOf(result);
    }

    /**
     * Reads one required exact string state member.
     *
     * @param values state values
     * @param name   member name
     * @return required string
     */
    private static String string(final Map<String, Object> values, final String name) {
        final Object value = values.get(name);
        if (!(value instanceof String) || ((String) value).isBlank()) {
            reject(ProtocolError.INVALID_GRANT);
        }
        return (String) value;
    }

    /**
     * Reads one required string state member that may be empty.
     *
     * @param values state values
     * @param name   member name
     * @return exact string value
     */
    private static String text(final Map<String, Object> values, final String name) {
        final Object value = values.get(name);
        if (!(value instanceof String)) {
            reject(ProtocolError.INVALID_GRANT);
        }
        return (String) value;
    }

    /**
     * Reads one required integral epoch-second state member.
     *
     * @param values state values
     * @param name   member name
     * @return represented instant
     */
    private static Instant instant(final Map<String, Object> values, final String name) {
        final Object value = values.get(name);
        if (!(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)) {
            reject(ProtocolError.INVALID_GRANT);
        }
        try {
            return Instant.ofEpochSecond(((Number) value).longValue());
        } catch (final DateTimeException failure) {
            throw new ProtocolException(ProtocolError.INVALID_GRANT.getKey(), ProtocolError.INVALID_GRANT.getValue(),
                    failure);
        }
    }

    /**
     * Parses a non-empty space-delimited scope state value.
     *
     * @param value stored scope value
     * @return immutable scopes
     */
    private static Set<String> scopeSet(final String value) {
        if (value.isEmpty()) {
            return Set.of();
        }
        final List<String> items = StringKit.split(value, Symbol.SPACE);
        if (items.isEmpty()) {
            reject(ProtocolError.INVALID_GRANT);
        }
        final LinkedHashSet<String> result = new LinkedHashSet<>(items);
        if (result.size() != items.size()) {
            reject(ProtocolError.INVALID_GRANT);
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Copies one scope collection while preserving its iteration order.
     *
     * @param scopes  source scopes
     * @param message null-validation message
     * @return immutable insertion-ordered scopes
     */
    private static Set<String> ordered(final Set<String> scopes, final String message) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Assert.notNull(scopes, message)));
    }

    /**
     * Generates one exact-length opaque random credential.
     *
     * @param runtime validated authentication runtime
     * @return unpadded Base64url credential
     */
    private static String credential(final Runtime runtime) {
        final byte[] value = runtime.random().nextBytes(CREDENTIAL_BYTES);
        if (value == null || value.length != CREDENTIAL_BYTES) {
            reject(ProtocolError.TEMPORARILY_UNAVAILABLE);
        }
        return Base64.encodeUrlSafe(value);
    }

    /**
     * Derives one tenant-isolated authorization-code state key.
     *
     * @param invocation tenant-scoped operation context
     * @param code       opaque authorization code
     * @return protected state key
     */
    private static String codeKey(final Invocation invocation, final String code) {
        return ReplayKey.derive(invocation.tenantId(), "oauth2", "code", code);
    }

    /**
     * Returns a non-null security-clock instant.
     *
     * @param runtime validated authentication runtime
     * @return security-clock instant
     */
    private static Instant clock(final Runtime runtime) {
        return Assert.notNull(runtime.clock().now(), "Clock value must be not null!");
    }

    /**
     * Adds a trusted lifetime with overflow handling.
     *
     * @param instant  base instant
     * @param lifetime positive lifetime
     * @return expiration instant
     */
    private static Instant add(final Instant instant, final Duration lifetime) {
        try {
            return instant.plus(lifetime);
        } catch (final DateTimeException | ArithmeticException failure) {
            throw new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE.getKey(),
                    ProtocolError.TEMPORARILY_UNAVAILABLE.getValue(), failure);
        }
    }

    /**
     * Creates one token response with a safe positive lifetime conversion.
     *
     * @param accessToken  issued access token
     * @param refreshToken optional refresh token
     * @param lifetime     access-token lifetime
     * @param scopes       granted scopes
     * @return token response
     */
    private static TokenResponse response(
            final String accessToken,
            final String refreshToken,
            final Duration lifetime,
            final Set<String> scopes) {
        final long seconds = lifetime.getSeconds();
        if (seconds <= Normal._0) {
            reject(ProtocolError.TEMPORARILY_UNAVAILABLE);
        }
        return new TokenResponse(accessToken, TokenType.BEARER, seconds, scopes, refreshToken, null);
    }

    /**
     * Requires one non-blank stored value.
     *
     * @param value stored value
     * @return unchanged non-blank value
     */
    private static String required(final String value) {
        if (StringKit.isBlank(value)) {
            reject(ProtocolError.INVALID_GRANT);
        }
        return value;
    }

    /**
     * Creates an already failed protocol stage.
     *
     * @param error fixed protocol error
     * @param <T>   stage result type
     * @return failed stage
     */
    private static <T> CompletionStage<T> failed(final ProtocolError error) {
        return CompletableFuture.failedFuture(new ProtocolException(error));
    }

    /**
     * Converts asynchronous wrapper failures into runtime exceptions without exposing their messages.
     *
     * @param failure asynchronous failure
     * @return runtime failure
     */
    private static RuntimeException propagate(final Throwable failure) {
        final Throwable cause = failure instanceof java.util.concurrent.CompletionException
                && failure.getCause() != null ? failure.getCause() : failure;
        return cause instanceof RuntimeException runtimeFailure ? runtimeFailure
                : new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE.getKey(),
                        ProtocolError.TEMPORARILY_UNAVAILABLE.getValue(), cause);
    }

    /**
     * Rejects input with one fixed OAuth protocol error.
     *
     * @param error fixed protocol error
     */
    private static void reject(final ProtocolError error) {
        throw new ProtocolException(error);
    }

    /**
     * Validates a product authorization decision and atomically creates a one-time code.
     *
     * @param invocation tenant-scoped operation context
     * @param request    authorization endpoint request
     * @param decision   product authorization-page decision
     * @return stage containing the redirect-safe authorization result
     */
    public CompletionStage<AuthorizationResponse> authorize(
            final Invocation invocation,
            final AuthorizationRequest request,
            final AuthorizationDecision decision) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final AuthorizationRequest input = Assert.notNull(request, "Authorization request must be not null!");
        final AuthorizationDecision approval = Assert.notNull(decision, "Authorization decision must be not null!");
        OAuth2Validator.grant(GrantType.AUTHORIZATION_CODE, policy.grants());
        final CompletionStage<Optional<Client>> resolved = Assert.notNull(
                runtime.clients().resolve(context, input.clientId()),
                "Client resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final Client client = OAuth2Validator.client(optional, input.clientId());
            OAuth2Validator.authorization(input, client, policy.scopes(), runtime.limits());
            if (!approval.approved()) {
                reject(ProtocolError.ACCESS_DENIED);
            }
            if (StringKit.isBlank(approval.subjectId()) || !input.scopes().containsAll(approval.approvedScopes())) {
                reject(ProtocolError.INVALID_SCOPE);
            }
            final Set<String> scopes = OAuth2Validator
                    .scopes(approval.approvedScopes(), policy.scopes(), runtime.limits());
            final CompletionStage<Optional<Subject>> subject = Assert.notNull(
                    runtime.subjects().resolve(context, approval.subjectId()),
                    "Subject resolver stage must be not null!");
            return subject.thenCompose(optionalSubject -> {
                if (optionalSubject == null || optionalSubject.isEmpty()
                        || !approval.subjectId().equals(optionalSubject.get().id())) {
                    reject(ProtocolError.ACCESS_DENIED);
                }
                final String code = credential(runtime);
                final String key = codeKey(context, code);
                final byte[] state = encodeCode(
                        new CodeState(client.id(), approval.subjectId(), input.redirectUri().toASCIIString(), scopes,
                                input.codeChallenge()),
                        runtime);
                final CompletionStage<Boolean> created = Assert.notNull(
                        runtime.states().putIfAbsent(context, key, state, CODE_LIFETIME),
                        "State-store create stage must be not null!");
                return created.thenApply(inserted -> {
                    if (!Boolean.TRUE.equals(inserted)) {
                        reject(ProtocolError.TEMPORARILY_UNAVAILABLE);
                    }
                    return new AuthorizationResponse(input.redirectUri(), code, input.state(), null);
                });
            });
        });
    }

    /**
     * Atomically consumes an authorization code and issues access and refresh tokens.
     *
     * @param invocation tenant-scoped operation context
     * @param request    token endpoint request
     * @return stage containing issued opaque tokens
     */
    public CompletionStage<TokenResponse> exchange(final Invocation invocation, final TokenRequest request) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final TokenRequest input = Assert.notNull(request, "Token request must be not null!");
        if (input.grantType() != GrantType.AUTHORIZATION_CODE || StringKit.isBlank(input.code())) {
            return failed(ProtocolError.INVALID_GRANT);
        }
        if (input.refreshToken() != null || input.deviceCode() != null || !input.scopes().isEmpty()) {
            return failed(ProtocolError.INVALID_REQUEST);
        }
        OAuth2Validator.grant(input.grantType(), policy.grants());
        final CompletionStage<Optional<Client>> resolved = Assert.notNull(
                runtime.clients().resolve(context, input.clientId()),
                "Client resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final Client client = OAuth2Validator.client(optional, input.clientId());
            return OAuth2Validator.authenticate(context, client, input.clientSecret(), runtime);
        }).thenCompose(authenticated -> {
            final String key = codeKey(context, input.code());
            final CompletionStage<Optional<byte[]>> taken = Assert
                    .notNull(runtime.states().take(context, key), "State-store take stage must be not null!");
            return taken.thenCompose(value -> {
                if (value == null || value.isEmpty()) {
                    reject(ProtocolError.INVALID_GRANT);
                }
                final CodeState state = decodeCode(value.get(), runtime);
                if (!state.clientId().equals(authenticated.id()) || input.redirectUri() == null
                        || !state.redirectUri().equals(input.redirectUri().toASCIIString())) {
                    reject(ProtocolError.INVALID_GRANT);
                }
                OAuth2Validator.pkce(
                        state.codeChallenge(),
                        org.miaixz.bus.auth.metric.OAuth2.CodeChallengeMethod.S256,
                        input.codeVerifier());
                return issueTokens(
                        context,
                        state.subjectId(),
                        state.clientId(),
                        state.scopes(),
                        true,
                        null,
                        policy,
                        runtime);
            });
        });
    }

    /**
     * Immutable refresh token family member state shared with rotation processing.
     *
     * @param familyId  refresh family identifier
     * @param subjectId authorized subject identifier
     * @param clientId  authorized client identifier
     * @param scopes    granted scopes
     * @param expiresAt fixed family expiration
     */
    record RefreshState(String familyId, String subjectId, String clientId, Set<String> scopes, Instant expiresAt) {

        /**
         * Snapshots refresh-family state.
         *
         * @param familyId  family identifier
         * @param subjectId subject identifier
         * @param clientId  client identifier
         * @param scopes    granted scopes
         * @param expiresAt family expiration
         */
        RefreshState {
            familyId = required(familyId);
            subjectId = required(subjectId);
            clientId = required(clientId);
            scopes = ordered(scopes, "Refresh scopes must be not null!");
            expiresAt = Assert.notNull(expiresAt, "Refresh expiration must be not null!");
        }
    }

    /**
     * Immutable one-time authorization-code state.
     *
     * @param clientId      authorized client identifier
     * @param subjectId     authorized subject identifier
     * @param redirectUri   exact redirect URI text
     * @param scopes        approved scopes
     * @param codeChallenge mandatory S256 challenge
     */
    private record CodeState(String clientId, String subjectId, String redirectUri, Set<String> scopes,
            String codeChallenge) {

        /**
         * Snapshots one-time authorization-code state.
         *
         * @param clientId      client identifier
         * @param subjectId     subject identifier
         * @param redirectUri   redirect URI
         * @param scopes        approved scopes
         * @param codeChallenge S256 challenge
         */
        private CodeState {
            clientId = required(clientId);
            subjectId = required(subjectId);
            redirectUri = required(redirectUri);
            scopes = ordered(scopes, "Authorization scopes must be not null!");
            codeChallenge = required(codeChallenge);
        }
    }

}
