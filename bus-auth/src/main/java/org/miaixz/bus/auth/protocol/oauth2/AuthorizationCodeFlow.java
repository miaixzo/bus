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
import java.util.*;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Callback.Mode;
import org.miaixz.bus.auth.Callback.Outbound;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.codec.json.JsonValues;
import org.miaixz.bus.auth.codec.state.StateJsonCodec;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.*;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.auth.resolver.SubjectResolver;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Options;

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
    private final OAuth2Dependencies dependencies;

    /**
     * Tenant-isolated atomic state store for authorization codes and refresh families.
     */
    private final StateStore states;

    /**
     * Product subject resolver used before an authorization code is issued.
     */
    private final SubjectResolver subjects;

    /**
     * Product secret resolver used for confidential-client authentication.
     */
    private final SecretResolver secrets;

    /**
     * Fabric clock used for every lifetime and expiration calculation.
     */
    private final Clock clock;

    /**
     * Caller-owned cryptographically secure random source used for opaque credentials.
     */
    private final SecureRandom random;

    /**
     * Explicit JSON provider used only inside bounded state envelopes.
     */
    private final JsonProvider json;

    /**
     * Closed allocation and parser limits applied to persisted state.
     */
    private final Limits limits;

    /**
     * Creates one authorization-code state machine.
     *
     * @param policy       trusted OAuth policy
     * @param dependencies registered-client and authorization-grant product ports
     * @param states       tenant-isolated atomic state store
     * @param subjects     trusted subject resolver
     * @param secrets      confidential-client secret resolver
     * @param clock        Fabric clock used for all protocol time
     * @param random       secure random source used for opaque credentials
     * @param json         explicit JSON provider for bounded state envelopes
     * @param limits       closed parser and allocation limits
     */
    public AuthorizationCodeFlow(final Policy policy, final OAuth2Dependencies dependencies, final StateStore states,
            final SubjectResolver subjects, final SecretResolver secrets, final Clock clock, final SecureRandom random,
            final JsonProvider json, final Limits limits) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.dependencies = Assert.notNull(dependencies, "OAuth dependencies must be not null!");
        this.states = Assert.notNull(states, "State store must be not null!");
        this.subjects = Assert.notNull(subjects, "Subject resolver must be not null!");
        this.secrets = Assert.notNull(secrets, "Secret resolver must be not null!");
        this.clock = Assert.notNull(clock, "Clock must be not null!");
        this.random = Assert.notNull(random, "Secure random must be not null!");
        this.json = Assert.notNull(json, "JSON provider must be not null!");
        this.limits = Assert.notNull(limits, "Limits must be not null!");
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
     * @return stage containing issued opaque tokens
     */
    static CompletionStage<TokenResponse> issueTokens(
            final Context invocation,
            final String subjectId,
            final String clientId,
            final Set<String> scopes,
            final boolean includeRefresh,
            final String existingFamily,
            final Policy policy,
            final OAuth2Dependencies dependencies,
            final StateStore states,
            final Clock clock,
            final SecureRandom random,
            final JsonProvider json,
            final Limits limits) {
        final Instant now = OAuth2Support.now(clock);
        final Instant accessExpiration = OAuth2Support
                .add(now, policy.accessTokenLifetime(), ProtocolError.TEMPORARILY_UNAVAILABLE);
        final String accessToken = OAuth2Support
                .credential(random, CREDENTIAL_BYTES, ProtocolError.TEMPORARILY_UNAVAILABLE);
        final String accessId = tokenKey(invocation, "access", accessToken);
        final String family = includeRefresh ? StringKit.isBlank(existingFamily)
                ? OAuth2Support.credential(random, CREDENTIAL_BYTES, ProtocolError.TEMPORARILY_UNAVAILABLE)
                : existingFamily : null;
        Options grantOptions = Options.of(AuthorizationGrant.TOKEN_TYPE, "access");
        if (family != null) {
            grantOptions = grantOptions.with(AuthorizationGrant.FAMILY_ID, family);
        }
        final AuthorizationGrant grant = new AuthorizationGrant(accessId, subjectId, clientId, scopes, accessExpiration,
                grantOptions);
        if (!includeRefresh) {
            final CompletionStage<Void> saved = Assert.notNull(
                    dependencies.grants().save(invocation, grant),
                    "AuthorizationGrant-store save stage must be not null!");
            return saved.thenApply(ignored -> response(accessToken, null, policy.accessTokenLifetime(), scopes));
        }
        final String refreshToken = OAuth2Support
                .credential(random, CREDENTIAL_BYTES, ProtocolError.TEMPORARILY_UNAVAILABLE);
        final Instant familyExpiration = OAuth2Support
                .add(now, policy.refreshTokenLifetime(), ProtocolError.TEMPORARILY_UNAVAILABLE);
        final RefreshState refresh = new RefreshState(family, subjectId, clientId, scopes, familyExpiration);
        final String refreshKey = tokenKey(invocation, "refresh", refreshToken);
        final Duration refreshTtl = Duration.between(now, familyExpiration);
        final CompletionStage<Boolean> stored = Assert.notNull(
                states.putIfAbsent(invocation, refreshKey, encodeRefresh(refresh, json, limits), refreshTtl),
                "State-store create stage must be not null!");
        return stored.thenCompose(created -> {
            if (!Boolean.TRUE.equals(created)) {
                reject(ProtocolError.TEMPORARILY_UNAVAILABLE);
            }
            final CompletionStage<Void> saved = Assert.notNull(
                    dependencies.grants().save(invocation, grant),
                    "AuthorizationGrant-store save stage must be not null!");
            final CompletionStage<TokenResponse> issued = saved
                    .thenApply(ignored -> response(accessToken, refreshToken, policy.accessTokenLifetime(), scopes));
            return issued.exceptionallyCompose(failure -> {
                final CompletionStage<Boolean> removed = Assert
                        .notNull(states.remove(invocation, refreshKey), "State-store remove stage must be not null!");
                return removed.handle((removedValue, cleanupFailure) -> {
                    if (cleanupFailure != null) {
                        failure.addSuppressed(cleanupFailure);
                    }
                    final Throwable cause = ExceptionKit.unwrap(failure);
                    throw cause instanceof RuntimeException runtimeFailure ? runtimeFailure
                            : new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE.getKey(),
                                    ProtocolError.TEMPORARILY_UNAVAILABLE.getValue(), cause);
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
    static String tokenKey(final Context invocation, final String kind, final String token) {
        return OAuth2Support.key(invocation, kind, token);
    }

    /**
     * Decodes and validates one refresh-family state envelope.
     *
     * @param envelope stored state envelope
     * @return immutable refresh state
     */
    static RefreshState decodeRefresh(final byte[] envelope, final JsonProvider json, final Limits limits) {
        final Map<String, Object> values = new StateJsonCodec(json, limits.maxJsonBytes(), limits.maxJsonDepth())
                .decode(envelope);
        final int maximum = limits.maxJsonBytes();
        final String family = JsonValues.requiredText(values, "family", maximum, AuthorizationCodeFlow::invalidGrant);
        final String subject = JsonValues.requiredText(values, "subject", maximum, AuthorizationCodeFlow::invalidGrant);
        final String client = JsonValues.requiredText(values, "client", maximum, AuthorizationCodeFlow::invalidGrant);
        final Set<String> scopes = OAuth2Support.decodeScopes(
                JsonValues.text(values, "scopes", maximum, AuthorizationCodeFlow::invalidGrant),
                ProtocolError.INVALID_GRANT);
        final Instant expiresAt = instant(values, "expires_at");
        return new RefreshState(family, subject, client, scopes, expiresAt);
    }

    /**
     * Encodes one refresh-family state into the common authenticated envelope.
     *
     * @param state refresh state
     * @return encoded state envelope
     */
    static byte[] encodeRefresh(final RefreshState state, final JsonProvider json, final Limits limits) {
        final Map<String, Object> values = Map.of(
                "family",
                state.familyId(),
                "subject",
                state.subjectId(),
                "client",
                state.clientId(),
                "scopes",
                OAuth2Support.encodeScopes(state.scopes()),
                "expires_at",
                state.expiresAt().getEpochSecond());
        return envelope(values, json, limits);
    }

    /**
     * Encodes one authorization-code state.
     *
     * @param state immutable code state
     * @return encoded state envelope
     */
    private static byte[] encodeCode(final CodeState state, final JsonProvider json, final Limits limits) {
        final Map<String, Object> values = Map.of(
                "client",
                state.clientId(),
                "subject",
                state.subjectId(),
                "redirect",
                state.redirectUri(),
                "scopes",
                OAuth2Support.encodeScopes(state.scopes()),
                "challenge",
                state.codeChallenge());
        return envelope(values, json, limits);
    }

    /**
     * Decodes one authorization-code state.
     *
     * @param envelope stored state envelope
     * @return validated code state
     */
    private static CodeState decodeCode(final byte[] envelope, final JsonProvider json, final Limits limits) {
        final Map<String, Object> values = new StateJsonCodec(json, limits.maxJsonBytes(), limits.maxJsonDepth())
                .decode(envelope);
        final int maximum = limits.maxJsonBytes();
        return new CodeState(JsonValues.requiredText(values, "client", maximum, AuthorizationCodeFlow::invalidGrant),
                JsonValues.requiredText(values, "subject", maximum, AuthorizationCodeFlow::invalidGrant),
                JsonValues.requiredText(values, "redirect", maximum, AuthorizationCodeFlow::invalidGrant),
                OAuth2Support.decodeScopes(
                        JsonValues.text(values, "scopes", maximum, AuthorizationCodeFlow::invalidGrant),
                        ProtocolError.INVALID_GRANT),
                JsonValues.requiredText(values, "challenge", maximum, AuthorizationCodeFlow::invalidGrant));
    }

    /**
     * Encodes one bounded JSON object into the common state envelope.
     *
     * @param values state values
     * @return encoded envelope
     */
    private static byte[] envelope(final Map<String, Object> values, final JsonProvider json, final Limits limits) {
        try {
            return new StateJsonCodec(json, limits.maxJsonBytes(), limits.maxJsonDepth()).encode(values);
        } catch (final RuntimeException failure) {
            throw new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE.getKey(),
                    ProtocolError.TEMPORARILY_UNAVAILABLE.getValue(), failure);
        }
    }

    /**
     * Reads one required integral epoch-second state member.
     *
     * @param values state values
     * @param name   member name
     * @return represented instant
     */
    private static Instant instant(final Map<String, Object> values, final String name) {
        try {
            return Instant.ofEpochSecond(JsonValues.integer(values, name, AuthorizationCodeFlow::invalidGrant));
        } catch (final RuntimeException failure) {
            if (failure instanceof ProtocolException) {
                throw failure;
            }
            throw new ProtocolException(ProtocolError.INVALID_GRANT.getKey(), ProtocolError.INVALID_GRANT.getValue(),
                    failure);
        }
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
     * Derives one tenant-isolated authorization-code state key.
     *
     * @param invocation tenant-scoped operation context
     * @param code       opaque authorization code
     * @return protected state key
     */
    private static String codeKey(final Context invocation, final String code) {
        return OAuth2Support.key(invocation, "code", code);
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
     * Creates the fixed invalid-grant JSON member failure.
     */
    private static RuntimeException invalidGrant() {
        return new ProtocolException(ProtocolError.INVALID_GRANT);
    }

    /**
     * Rejects input with one fixed OAuth protocol error.
     *
     * @param error fixed protocol error
     */
    private static void reject(final ProtocolError error) {
        throw new ProtocolException(error);
    }

    private static Mode mode(final ResponseMode value) {
        return switch (value) {
            case QUERY -> Mode.QUERY;
            case FORM_POST -> Mode.FORM_POST;
            case QUERY_JWT -> Mode.QUERY_JWT;
            case FORM_POST_JWT -> Mode.FORM_POST_JWT;
        };
    }

    /**
     * Validates a product authorization decision and atomically creates a one-time code.
     *
     * @param invocation tenant-scoped operation context
     * @param request    authorization endpoint request
     * @param decision   product authorization-page decision
     * @return stage containing the redirect-safe authorization result
     */
    public CompletionStage<Outbound> authorize(
            final Context invocation,
            final AuthorizationRequest request,
            final AuthorizationDecision decision) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final AuthorizationRequest input = Assert.notNull(request, "Authorization request must be not null!");
        final AuthorizationDecision approval = Assert.notNull(decision, "Authorization decision must be not null!");
        OAuth2Validator.grant(GrantType.AUTHORIZATION_CODE, policy.grants());
        final CompletionStage<Optional<RegisteredClient>> resolved = Assert.notNull(
                dependencies.clients().resolve(context, input.clientId()),
                "RegisteredClient resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final RegisteredClient client = OAuth2Validator.client(optional, input.clientId());
            OAuth2Validator.authorization(input, client, policy.scopes(), limits);
            if (!approval.approved()) {
                reject(ProtocolError.ACCESS_DENIED);
            }
            if (StringKit.isBlank(approval.subjectId()) || !input.scopes().containsAll(approval.approvedScopes())) {
                reject(ProtocolError.INVALID_SCOPE);
            }
            final Set<String> scopes = OAuth2Validator.scopes(approval.approvedScopes(), policy.scopes(), limits);
            final CompletionStage<Optional<Subject>> subject = Assert.notNull(
                    subjects.resolve(context, approval.subjectId()),
                    "Subject resolver stage must be not null!");
            return subject.thenCompose(optionalSubject -> {
                if (optionalSubject == null || optionalSubject.isEmpty()
                        || !approval.subjectId().equals(optionalSubject.get().id())) {
                    reject(ProtocolError.ACCESS_DENIED);
                }
                final String code = OAuth2Support
                        .credential(random, CREDENTIAL_BYTES, ProtocolError.TEMPORARILY_UNAVAILABLE);
                final String key = codeKey(context, code);
                final byte[] state = encodeCode(
                        new CodeState(client.id(), approval.subjectId(), input.redirectUri().toASCIIString(), scopes,
                                input.codeChallenge()),
                        json,
                        limits);
                final CompletionStage<Boolean> created = Assert.notNull(
                        states.putIfAbsent(context, key, state, CODE_LIFETIME),
                        "State-store create stage must be not null!");
                return created.thenApply(inserted -> {
                    if (!Boolean.TRUE.equals(inserted)) {
                        reject(ProtocolError.TEMPORARILY_UNAVAILABLE);
                    }
                    final Outbound.Builder callback = Callback.outbound(context)
                            .destination(org.miaixz.bus.fabric.Address.from(input.redirectUri()))
                            .mode(mode(input.responseMode())).parameter("code", code);
                    if (input.state() != null) {
                        callback.parameter("state", input.state());
                    }
                    return callback.build();
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
    public CompletionStage<TokenResponse> exchange(final Context invocation, final TokenRequest request) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final TokenRequest input = Assert.notNull(request, "Token request must be not null!");
        if (input.grantType() != GrantType.AUTHORIZATION_CODE || StringKit.isBlank(input.code())) {
            return OAuth2Support.failed(ProtocolError.INVALID_GRANT);
        }
        if (input.refreshToken() != null || input.deviceCode() != null || !input.scopes().isEmpty()) {
            return OAuth2Support.failed(ProtocolError.INVALID_REQUEST);
        }
        OAuth2Validator.grant(input.grantType(), policy.grants());
        final CompletionStage<Optional<RegisteredClient>> resolved = Assert.notNull(
                dependencies.clients().resolve(context, input.clientId()),
                "RegisteredClient resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final RegisteredClient client = OAuth2Validator.client(optional, input.clientId());
            return OAuth2Validator.authenticate(context, client, input.clientSecret(), secrets);
        }).thenCompose(authenticated -> {
            final String key = codeKey(context, input.code());
            final CompletionStage<Optional<byte[]>> taken = Assert
                    .notNull(states.take(context, key), "State-store take stage must be not null!");
            return taken.thenCompose(value -> {
                if (value == null || value.isEmpty()) {
                    reject(ProtocolError.INVALID_GRANT);
                }
                final CodeState state = decodeCode(value.get(), json, limits);
                if (!state.clientId().equals(authenticated.id()) || input.redirectUri() == null
                        || !state.redirectUri().equals(input.redirectUri().toASCIIString())) {
                    reject(ProtocolError.INVALID_GRANT);
                }
                OAuth2Validator.pkce(
                        state.codeChallenge(),
                        org.miaixz.bus.auth.protocol.oauth2.OAuth2.CodeChallengeMethod.S256,
                        input.codeVerifier());
                return issueTokens(
                        context,
                        state.subjectId(),
                        state.clientId(),
                        state.scopes(),
                        true,
                        null,
                        policy,
                        dependencies,
                        states,
                        clock,
                        random,
                        json,
                        limits);
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
     * @author Kimi Liu
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
     * @author Kimi Liu
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
