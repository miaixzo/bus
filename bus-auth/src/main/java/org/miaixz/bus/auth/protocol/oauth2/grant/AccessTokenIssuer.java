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
package org.miaixz.bus.auth.protocol.oauth2.grant;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.*;
import org.miaixz.bus.auth.guard.ClientAuthentication;
import org.miaixz.bus.auth.guard.ScopeValidator;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.resolver.ConsumerMetadata;
import org.miaixz.bus.auth.resolver.ProtectedResource;
import org.miaixz.bus.auth.shared.pkce.CodeChallenge;
import org.miaixz.bus.auth.shared.pkce.CodeVerifier;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.auth.shared.pkce.PkceValidator;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.loader.ResourceLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Executes initial OAuth 2.x token grants and persists opaque access and refresh token state.
 * <p>
 * The class consumes only standard {@link TokenRequest} grant variants. It never accepts transport credentials, exposes
 * grant-specific public methods, or treats a refresh token as an initial grant. All issued bearer values are stored
 * only under Provider-isolated irreversible digests.
 * </p>
 *
 * @author Kimi Liu
 */
public class AccessTokenIssuer {

    /**
     * Maximum create-if-absent attempts used for an opaque token digest collision.
     */
    private static final int MAXIMUM_CREATE_ATTEMPTS = Builder.MAXIMUM_RETRY_ATTEMPTS;

    /**
     * RFC 8628 interval increment applied after every successful excessive-poll update.
     */
    private static final Duration DEVICE_SLOW_DOWN_INCREMENT = Duration.ofSeconds(5);

    /**
     * Safe failure detail member carrying a registered OAuth error code.
     */
    private static final String OAUTH_ERROR = Builder.OAUTH_ERROR;

    /**
     * Provider identifier used to isolate every opaque credential digest.
     */
    private final String providerId;

    /**
     * Frozen Provider options governing enabled grants and token lifetimes.
     */
    private final GrantPolicy options;

    /**
     * Externally implemented registration, resource, and atomic state ports.
     */
    private final DriverServices services;

    /**
     * Standard scope subset validator shared across all supported grants.
     */
    private final ScopeValidator scopeValidator;

    /**
     * Constant-time PKCE validator used after one-time authorization-code consumption.
     */
    private final PkceValidator pkceValidator;

    /**
     * Compile-time selected token-response augmenter for pure OAuth or an extending protocol profile.
     */
    private final Augmenter augmenter;

    /**
     * Provider-isolated opaque token generation and irreversible key derivation.
     */
    private final TokenMaterial tokenMaterial;

    /**
     * Creates an initial token issuer for one compiled OAuth Provider.
     *
     * @param providerId     compiled server-role Source identifier
     * @param options        validated Provider options
     * @param services       caller-owned runtime dependencies
     * @param scopeValidator standard scope validator
     * @param pkceValidator  strict constant-time PKCE validator
     * @param augmenter      compile-time selected token-response augmenter
     * @param tokenMaterial  Provider-isolated token generator and digest service
     * @throws IllegalArgumentException if text is blank or a collaborator is {@code null}
     */
    public AccessTokenIssuer(final String providerId, final GrantPolicy options, final DriverServices services,
            final ScopeValidator scopeValidator, final PkceValidator pkceValidator, final Augmenter augmenter,
            final TokenMaterial tokenMaterial) {
        this.providerId = Assert.notBlank(providerId, "OAuth 2.x Provider id must not be blank");
        this.options = Assert.notNull(options, "OAuth 2.x Provider options must not be null");
        this.services = Assert.notNull(services, "OAuth 2.x execution services must not be null");
        this.scopeValidator = Assert.notNull(scopeValidator, "OAuth 2.x scope validator must not be null");
        this.pkceValidator = Assert.notNull(pkceValidator, "OAuth 2.x PKCE validator must not be null");
        this.augmenter = Assert.notNull(augmenter, "OAuth 2.x token response augmenter must not be null");
        this.tokenMaterial = Assert.notNull(tokenMaterial, "OAuth 2.x token material service must not be null");
    }

    /**
     * Maps one concrete request grant to its registered grant type.
     *
     * @param grant standard grant variant
     * @return registered grant type, or {@code null} for an impossible unknown implementation
     */
    private static GrantType grantType(final TokenRequest.Grant grant) {
        return switch (grant) {
            case AuthorizationCodeGrant ignored -> GrantType.AUTHORIZATION_CODE;
            case RefreshTokenGrant ignored -> GrantType.REFRESH_TOKEN;
            case ClientCredentialsGrant ignored -> GrantType.CLIENT_CREDENTIALS;
            case TokenExchangeGrant ignored -> GrantType.TOKEN_EXCHANGE;
            case DeviceCodeGrant ignored -> GrantType.DEVICE_CODE;
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
    }

    /**
     * Creates a standard invalid-grant rejection without exposing credential material.
     *
     * @param description safe diagnostic description
     * @return rejected token outcome
     */
    private static Outcome<TokenEndpointResponse> invalidGrant(final String description) {
        return Outcome.rejected(failure(ErrorCode._400, OAuth2ErrorCode.INVALID_GRANT, description));
    }

    /**
     * Creates a normalized operational store failure.
     *
     * @param description safe diagnostic description
     * @param <T>         operation success type
     * @return failed operation outcome
     */
    private static <T> Outcome<T> storeFailure(final String description) {
        return Outcome.failed(failure(ErrorCode._500, OAuth2ErrorCode.SERVER_ERROR, description));
    }

    /**
     * Creates a safe framework failure carrying one registered OAuth error identifier.
     *
     * @param error       existing Bus error definition
     * @param oauthError  registered OAuth error code
     * @param description non-sensitive diagnostic description
     * @return immutable safe failure
     */
    private static Outcome.Failure failure(
            final Errors error,
            final OAuth2ErrorCode oauthError,
            final String description) {
        return new Outcome.Failure(error, description,
                new JsonValue.ObjectValue(Map.of(OAUTH_ERROR, new JsonValue.StringValue(oauthError.value()))));
    }

    /**
     * Creates an already-completed token outcome stage.
     *
     * @param outcome token outcome
     * @return completed stage
     */
    private static CompletionStage<Outcome<TokenEndpointResponse>> completed(
            final Outcome<TokenEndpointResponse> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates an already-completed stage for a typed internal outcome.
     *
     * @param outcome typed internal outcome
     * @param <T>     success value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completedValue(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Executes one non-refresh grant through the standard OAuth token operation.
     *
     * @param request standard token request
     * @param context invocation context carrying a verified client identifier
     * @param timeout shared end-to-end operation timeout
     * @return asynchronous standard token response outcome
     */
    public CompletionStage<Outcome<TokenEndpointResponse>> token(
            final TokenRequest request,
            final ClientAuthentication authentication,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(request, "OAuth 2.x token request must not be null");
        Assert.notNull(authentication, "OAuth 2.x client authentication must not be null");
        Assert.notNull(context, "OAuth 2.x token context must not be null");
        Assert.notNull(timeout, "OAuth 2.x token timeout must not be null");
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE,
                                    "OAuth 2.x token request has no remaining timeout")));
        }
        final ConsumerMetadata client = authentication.consumer();
        final GrantType grantType = grantType(request.grant());
        if (grantType == null || request.grant() instanceof RefreshTokenGrant) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE,
                                    "OAuth 2.x token grant is not handled by the initial token issuer")));
        }
        if (!options.grantTypesSupported().contains(grantType)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE,
                                    "OAuth 2.x token grant is disabled by the Provider")));
        }

        if (authentication.federation().isPresent() && !GrantType.CLIENT_CREDENTIALS.equals(grantType)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
                                    "Federated client authentication is restricted to client_credentials")));
        }
        if (!client.grantTypes().contains(grantType)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
                                    "OAuth 2.x client is not registered for the requested grant")));
        }
        final String authenticatedSubject = authentication.federation().isPresent()
                ? authentication.federation().getOrNull().subject().value()
                : client.id();
        return execute(request, client, authenticatedSubject, context, timeout);
    }

    /**
     * Dispatches a validated standard grant without exposing grant-specific public operations.
     *
     * @param request              standard token request
     * @param client               resolved active client registration
     * @param authenticatedSubject subject established by client authentication
     * @param context              immutable invocation context
     * @param timeout              shared operation timeout
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> execute(
            final TokenRequest request,
            final ConsumerMetadata client,
            final String authenticatedSubject,
            final Context context,
            final Timeout timeout) {
        return switch (request.grant()) {
            case AuthorizationCodeGrant grant -> authorizationCode(grant, client, context, timeout);
            case ClientCredentialsGrant grant -> clientCredentials(
                    grant,
                    client,
                    authenticatedSubject,
                    context,
                    timeout);
            case TokenExchangeGrant grant -> tokenExchange(grant, client, context, timeout);
            case DeviceCodeGrant grant -> deviceCode(grant, client, context, timeout, 1);
            case RefreshTokenGrant ignored -> completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE,
                                    "OAuth 2.x refresh tokens require the rotation processor")));
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
    }

    /**
     * Atomically consumes and validates an authorization code before issuing bound tokens.
     *
     * @param grant   standard authorization-code grant
     * @param client  resolved client registration
     * @param context immutable invocation context
     * @param timeout shared operation timeout
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> authorizationCode(
            final AuthorizationCodeGrant grant,
            final ConsumerMetadata client,
            final Context context,
            final Timeout timeout) {
        final CompletionStage<ExpiringValue<AuthorizationCodeCache.Entry>> consumption;
        try {
            consumption = services.authorizationCodeCache().consume(tokenMaterial.key(grant.code()));
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x authorization code consumption failed"));
        }
        return consumption.handle((stored, thrown) -> new CacheResult<>(stored, thrown)).thenCompose(result -> {
            if (result.failure() != null) {
                return completed(storeFailure("OAuth 2.x authorization code consumption failed"));
            }
            final ExpiringValue<AuthorizationCodeCache.Entry> stored = result.value();
            if (stored == null || !stored.expiresAt().isAfter(timeout.clock().now())) {
                return completed(invalidGrant("OAuth 2.x authorization code is invalid or expired"));
            }
            final AuthorizationCodeCache.Entry entry = stored.value();
            if (!validAuthorizationCode(grant, client.id(), entry)) {
                return completed(invalidGrant("OAuth 2.x authorization code binding is invalid"));
            }
            return issue(
                    new Grant(client, entry.subjectId(), entry.scope(), entry.resource(), GrantType.AUTHORIZATION_CODE,
                            options.grantTypesSupported().contains(GrantType.REFRESH_TOKEN), Optional.empty(),
                            Optional.empty(), Optional.empty(), entry.openIdBinding()),
                    context,
                    timeout);
        });
    }

    /**
     * Validates every client, redirect, and PKCE binding stored with a consumed authorization code.
     *
     * @param grant    presented authorization-code grant
     * @param clientId verified client identifier
     * @param entry    consumed authorization-code binding
     * @return whether every binding is valid
     */
    private boolean validAuthorizationCode(
            final AuthorizationCodeGrant grant,
            final String clientId,
            final AuthorizationCodeCache.Entry entry) {
        if (!providerId.equals(entry.providerId()) || !clientId.equals(entry.clientId())) {
            return false;
        }
        final String grantClient = grant.clientId().getOrNull();
        if (grantClient != null && !clientId.equals(grantClient)) {
            return false;
        }
        final String redirect = grant.redirectUri().getOrNull();
        if (entry.redirectUriRequired() != (redirect != null)
                || redirect != null && !entry.redirectUri().equals(redirect)) {
            return false;
        }
        final String challenge = entry.codeChallenge().getOrNull();
        final String verifier = grant.codeVerifier().getOrNull();
        if (challenge == null || verifier == null) {
            return false;
        }
        try {
            final String method = entry.codeChallengeMethod().orElse(PkceMethod.PLAIN.value());
            pkceValidator.validate(new CodeVerifier(verifier), new CodeChallenge(challenge, PkceMethod.of(method)));
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /**
     * Validates a client-credentials scope and issues a bearer token representing the client itself.
     *
     * @param grant                standard client-credentials grant
     * @param client               resolved client registration
     * @param authenticatedSubject subject established by standard or federated authentication
     * @param context              immutable invocation context
     * @param timeout              shared operation timeout
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> clientCredentials(
            final ClientCredentialsGrant grant,
            final ConsumerMetadata client,
            final String authenticatedSubject,
            final Context context,
            final Timeout timeout) {
        final List<String> scope = grant.scope().isEmpty() ? List.of() : grant.scope().getOrNull().values();
        if (!validScope(scope, client.scopes(), options.scopesSupported())) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.INVALID_SCOPE,
                                    "OAuth 2.x client scope is not allowed")));
        }
        return issue(
                new Grant(client, authenticatedSubject, scope, List.of(), GrantType.CLIENT_CREDENTIALS, false,
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
                context,
                timeout);
    }

    /**
     * Validates an RFC 8693 exchange using active opaque access tokens and externally resolved target resources.
     *
     * @param grant   standard token-exchange grant
     * @param client  resolved client registration
     * @param context immutable invocation context
     * @param timeout shared operation timeout
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> tokenExchange(
            final TokenExchangeGrant grant,
            final ConsumerMetadata client,
            final Context context,
            final Timeout timeout) {
        if (!TokenExchangeGrant.ACCESS_TOKEN_TYPE.equals(grant.subjectTokenType())
                || grant.requestedTokenType().isPresent()
                        && !TokenExchangeGrant.ACCESS_TOKEN_TYPE.equals(grant.requestedTokenType().getOrNull())
                || grant.actorTokenType().isPresent()
                        && !TokenExchangeGrant.ACCESS_TOKEN_TYPE.equals(grant.actorTokenType().getOrNull())) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.INVALID_REQUEST,
                                    "OAuth 2.x token exchange supports the access-token type only")));
        }
        return activeToken(grant.subjectToken(), client.id(), timeout)
                .thenCompose(subjectOutcome -> switch (subjectOutcome) {
                    case Outcome.Succeeded<AccessTokenCache.Entry> success -> actor(
                            grant,
                            client.id(),
                            success.value().actorSubjectId(),
                            timeout).thenCompose(actorOutcome -> switch (actorOutcome) {
                                case Outcome.Succeeded<Optional<String>> actor -> exchangeTarget(
                                        grant,
                                        client,
                                        success.value(),
                                        actor.value(),
                                        context,
                                        timeout);
                                case Outcome.Rejected<Optional<String>> rejected -> completed(
                                        Outcome.rejected(rejected.failure()));
                                case Outcome.Failed<Optional<String>> failed -> completed(
                                        Outcome.failed(failed.failure()));
                                default -> throw new IllegalStateException("Unsupported Outcome implementation");
                            });
                    case Outcome.Rejected<AccessTokenCache.Entry> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<AccessTokenCache.Entry> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Validates the optional actor access token without changing the exchange subject.
     *
     * @param grant          standard token-exchange grant
     * @param clientId       verified client identifier
     * @param inheritedActor optional acting subject already bound to the subject token
     * @param timeout        shared operation timeout
     * @return asynchronous optional acting-subject outcome
     */
    private CompletionStage<Outcome<Optional<String>>> actor(
            final TokenExchangeGrant grant,
            final String clientId,
            final Optional<String> inheritedActor,
            final Timeout timeout) {
        final String actorToken = grant.actorToken().getOrNull();
        if (actorToken == null) {
            return completedValue(Outcome.succeeded(inheritedActor));
        }
        if (inheritedActor.isPresent()) {
            return completedValue(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.INVALID_REQUEST,
                                    "OAuth 2.x nested subject delegation is not supported by this token representation")));
        }
        return activeToken(actorToken, clientId, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<AccessTokenCache.Entry> success -> success.value().actorSubjectId().isPresent()
                    ? Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.INVALID_REQUEST,
                                    "OAuth 2.x nested actor delegation is not supported by this token representation"))
                    : Outcome.succeeded(Optional.of(success.value().subjectId()));
            case Outcome.Rejected<AccessTokenCache.Entry> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<AccessTokenCache.Entry> failed -> Outcome.failed(failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Resolves an optional token-exchange target and computes the non-expanding effective scope and audience.
     *
     * @param grant          standard token-exchange grant
     * @param client         resolved client registration
     * @param subject        active subject access-token metadata
     * @param actorSubjectId optional RFC 8693 acting-subject binding
     * @param context        immutable invocation context
     * @param timeout        shared operation timeout
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> exchangeTarget(
            final TokenExchangeGrant grant,
            final ConsumerMetadata client,
            final AccessTokenCache.Entry subject,
            final Optional<String> actorSubjectId,
            final Context context,
            final Timeout timeout) {
        if (grant.resource().isEmpty() && grant.audience().isEmpty()) {
            final List<String> effective = effectiveScope(
                    grant.scope(),
                    subject.scope(),
                    client.scopes(),
                    options.scopesSupported(),
                    null);
            if (effective == null) {
                return completed(
                        Outcome.rejected(
                                failure(
                                        ErrorCode._400,
                                        OAuth2ErrorCode.INVALID_SCOPE,
                                        "OAuth 2.x exchange scope is not allowed")));
            }
            return issue(
                    new Grant(client, subject.subjectId(), effective, subject.audience(), GrantType.TOKEN_EXCHANGE,
                            false, Optional.of(TokenExchangeGrant.ACCESS_TOKEN_TYPE), actorSubjectId,
                            subject.confirmation(), Optional.empty()),
                    context,
                    timeout);
        }
        final CompletionStage<Outcome<ProtectedResource>> resolution;
        try {
            final ResourceLoader.Request request = new ResourceLoader.Request(services.registration(), providerId,
                    grant.audience(), grant.resource());
            resolution = Outcome.mapStage(
                    () -> services.resourceLoader().load(request, context, timeout),
                    loaded -> services.resourceParser().parse(services.registration(), request, loaded));
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x exchange target resolution failed"));
        }
        return resolution
                .handle(
                        (outcome, thrown) -> thrown == null && outcome != null ? outcome
                                : Outcome.<ProtectedResource>failed(
                                        failure(
                                                ErrorCode._500,
                                                OAuth2ErrorCode.SERVER_ERROR,
                                                "OAuth 2.x exchange target resolution failed")))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<ProtectedResource> success -> {
                        final ProtectedResource resource = success.value();
                        if (resource == null || resource.audience().isEmpty()) {
                            yield completed(
                                    Outcome.rejected(
                                            failure(
                                                    ErrorCode._400,
                                                    OAuth2ErrorCode.INVALID_TARGET,
                                                    "OAuth 2.x exchange target has no valid audience")));
                        }
                        final List<String> effective = effectiveScope(
                                grant.scope(),
                                subject.scope(),
                                client.scopes(),
                                options.scopesSupported(),
                                Set.copyOf(resource.scopes()));
                        if (effective == null) {
                            yield completed(
                                    Outcome.rejected(
                                            failure(
                                                    ErrorCode._400,
                                                    OAuth2ErrorCode.INVALID_SCOPE,
                                                    "OAuth 2.x exchange scope exceeds the authorized target")));
                        }
                        yield issue(
                                new Grant(client, subject.subjectId(), effective, resource.audience(),
                                        GrantType.TOKEN_EXCHANGE, false,
                                        Optional.of(TokenExchangeGrant.ACCESS_TOKEN_TYPE), actorSubjectId,
                                        subject.confirmation(), Optional.empty()),
                                context,
                                timeout);
                    }
                    case Outcome.Rejected<ProtectedResource> rejected -> completed(
                            Outcome.rejected(
                                    failure(
                                            rejected.failure().error(),
                                            OAuth2ErrorCode.INVALID_TARGET,
                                            "OAuth 2.x exchange target was rejected")));
                    case Outcome.Failed<ProtectedResource> failed -> completed(
                            Outcome.failed(
                                    failure(
                                            failed.failure().error(),
                                            OAuth2ErrorCode.SERVER_ERROR,
                                            "OAuth 2.x exchange target resolution failed")));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Computes an explicit validated scope or a default intersection of all authorization boundaries.
     *
     * @param requested      optional explicitly requested scope
     * @param subjectScopes  subject-token authorized scopes
     * @param clientScopes   client-registered scopes
     * @param providerScopes Provider-supported scopes
     * @param resourceScopes optional resolved resource scope set
     * @return effective ordered scope, or {@code null} when an explicit scope expands a boundary
     */
    private List<String> effectiveScope(
            final Optional<Scope> requested,
            final List<String> subjectScopes,
            final Set<String> clientScopes,
            final Set<String> providerScopes,
            final Set<String> resourceScopes) {
        if (requested.isPresent()) {
            final List<String> values = requested.getOrNull().values();
            if (!validScope(values, Set.copyOf(subjectScopes), clientScopes, providerScopes)
                    || resourceScopes != null && !validScope(values, resourceScopes)) {
                return null;
            }
            return values;
        }
        final List<String> values = new ArrayList<>();
        for (String value : subjectScopes) {
            if (clientScopes.contains(value) && providerScopes.contains(value)
                    && (resourceScopes == null || resourceScopes.contains(value))) {
                values.add(value);
            }
        }
        return List.copyOf(values);
    }

    /**
     * Resolves one opaque access token and verifies Provider, client, and expiration bindings.
     *
     * @param token    opaque token material
     * @param clientId verified exchange client identifier
     * @param timeout  shared operation timeout
     * @return asynchronous active token metadata outcome
     */
    private CompletionStage<Outcome<AccessTokenCache.Entry>> activeToken(
            final String token,
            final String clientId,
            final Timeout timeout) {
        final CompletionStage<ExpiringValue<AccessTokenCache.Entry>> lookup;
        try {
            lookup = services.accessTokenCache().find(tokenMaterial.key(token));
        } catch (RuntimeException exception) {
            return completedValue(storeFailure("OAuth 2.x exchange token lookup failed"));
        }
        return lookup.handle((stored, thrown) -> new CacheResult<>(stored, thrown)).thenCompose(result -> {
            if (result.failure() != null) {
                return completedValue(
                        Outcome.<AccessTokenCache.Entry>failed(
                                failure(
                                        ErrorCode._500,
                                        OAuth2ErrorCode.SERVER_ERROR,
                                        "OAuth 2.x exchange token lookup failed")));
            }
            final ExpiringValue<AccessTokenCache.Entry> stored = result.value();
            if (stored == null || !stored.expiresAt().isAfter(timeout.clock().now())
                    || !providerId.equals(stored.value().providerId()) || !clientId.equals(stored.value().clientId())) {
                return completedValue(
                        Outcome.<AccessTokenCache.Entry>rejected(
                                failure(
                                        ErrorCode._400,
                                        OAuth2ErrorCode.INVALID_REQUEST,
                                        "OAuth 2.x exchange token is invalid")));
            }
            final CompletionStage<ExpiringValue<AuthorizationCache.Entry>> authorization;
            try {
                authorization = services.authorizationCache()
                        .find(AuthorizationCache.key(providerId, stored.value().authorizationId()));
            } catch (RuntimeException exception) {
                return completedValue(storeFailure("OAuth 2.x exchange authorization lookup failed"));
            }
            return authorization.handle((state, thrown) -> {
                if (thrown != null) {
                    return Outcome.<AccessTokenCache.Entry>failed(
                            failure(
                                    ErrorCode._500,
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OAuth 2.x exchange authorization lookup failed"));
                }
                if (state == null || !state.expiresAt().isAfter(timeout.clock().now())
                        || state.value().status() != AuthorizationCache.Status.ACTIVE
                        || !providerId.equals(state.value().providerId())
                        || !clientId.equals(state.value().clientId())) {
                    return Outcome.<AccessTokenCache.Entry>rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.INVALID_REQUEST,
                                    "OAuth 2.x exchange authorization is inactive"));
                }
                return Outcome.succeeded(stored.value());
            });
        });
    }

    /**
     * Polls RFC 8628 state using compare-and-replace before any terminal token issuance.
     *
     * @param grant   standard device-code grant
     * @param client  resolved client registration
     * @param context immutable invocation context
     * @param timeout shared operation timeout
     * @param attempt one-based compare-and-replace attempt number
     * @return asynchronous token or registered device error outcome
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> deviceCode(
            final DeviceCodeGrant grant,
            final ConsumerMetadata client,
            final Context context,
            final Timeout timeout,
            final int attempt) {
        final String suppliedClient = grant.clientId().getOrNull();
        if (suppliedClient != null && !client.id().equals(suppliedClient)) {
            return completed(invalidGrant("OAuth 2.x device code client binding is invalid"));
        }
        final String key = tokenMaterial.key(grant.deviceCode());
        final CompletionStage<ExpiringValue<DeviceCodeCache.Entry>> lookup;
        try {
            lookup = services.deviceCodeCache().find(key);
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x device code lookup failed"));
        }
        return lookup.handle((stored, thrown) -> new CacheResult<>(stored, thrown)).thenCompose(result -> {
            if (result.failure() != null) {
                return completed(storeFailure("OAuth 2.x device code lookup failed"));
            }
            final ExpiringValue<DeviceCodeCache.Entry> stored = result.value();
            final Instant now = timeout.clock().now();
            if (stored == null || !stored.expiresAt().isAfter(now)) {
                return completed(
                        Outcome.rejected(
                                failure(
                                        ErrorCode._400,
                                        OAuth2ErrorCode.EXPIRED_TOKEN,
                                        "OAuth 2.x device code is expired")));
            }
            final DeviceCodeCache.Entry entry = stored.value();
            if (!providerId.equals(entry.providerId()) || !client.id().equals(entry.clientId())) {
                return completed(invalidGrant("OAuth 2.x device code binding is invalid"));
            }
            return switch (entry.status()) {
                case DENIED -> completed(
                        Outcome.rejected(
                                failure(
                                        ErrorCode._400,
                                        OAuth2ErrorCode.ACCESS_DENIED,
                                        "OAuth 2.x device authorization was denied")));
                case CONSUMED -> completed(
                        Outcome.rejected(
                                failure(
                                        ErrorCode._400,
                                        OAuth2ErrorCode.EXPIRED_TOKEN,
                                        "OAuth 2.x device code was already consumed")));
                case PENDING -> pollPending(grant, key, stored, entry, client, context, timeout, attempt, now);
                case APPROVED -> consumeApproved(grant, key, stored, entry, client, context, timeout, attempt, now);
            };
        });
    }

    /**
     * Records a pending device poll and applies the RFC 8628 slow-down interval increase when necessary.
     *
     * @param grant   original standard device-code grant retained across CAS retries
     * @param key     isolated device-code digest
     * @param stored  current expiring state
     * @param entry   current device entry
     * @param client  resolved client registration
     * @param context immutable invocation context
     * @param timeout shared operation timeout
     * @param attempt one-based compare-and-replace attempt
     * @param now     current shared-clock instant
     * @return asynchronous pending or slow-down outcome
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> pollPending(
            final DeviceCodeGrant grant,
            final String key,
            final ExpiringValue<DeviceCodeCache.Entry> stored,
            final DeviceCodeCache.Entry entry,
            final ConsumerMetadata client,
            final Context context,
            final Timeout timeout,
            final int attempt,
            final Instant now) {
        final Instant last = entry.lastPolledAt().getOrNull();
        final boolean excessive = last != null && now.isBefore(last.plus(entry.interval()));
        final Duration interval = excessive ? entry.interval().plus(DEVICE_SLOW_DOWN_INCREMENT) : entry.interval();
        final DeviceCodeCache.Entry update = new DeviceCodeCache.Entry(entry.providerId(), entry.clientId(),
                entry.userCode(), entry.scope(), DeviceCodeCache.Status.PENDING, interval, Optional.of(now),
                Optional.empty());
        return replaceDevice(key, stored, update, timeout).thenCompose(replaced -> {
            if (replaced == null) {
                return completed(storeFailure("OAuth 2.x device polling state update failed"));
            }
            if (!replaced && attempt < MAXIMUM_CREATE_ATTEMPTS) {
                return deviceCode(grant, client, context, timeout, attempt + 1);
            }
            if (!replaced) {
                return completed(storeFailure("OAuth 2.x device polling state contention exceeded its limit"));
            }
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    excessive ? OAuth2ErrorCode.SLOW_DOWN : OAuth2ErrorCode.AUTHORIZATION_PENDING,
                                    excessive ? "OAuth 2.x device polling is too frequent"
                                            : "OAuth 2.x device authorization is pending")));
        });
    }

    /**
     * Atomically marks an approved device code consumed before issuing its tokens.
     *
     * @param grant   original standard device-code grant retained across CAS retries
     * @param key     isolated device-code digest
     * @param stored  current expiring state
     * @param entry   approved device entry
     * @param client  resolved client registration
     * @param context immutable invocation context
     * @param timeout shared operation timeout
     * @param attempt one-based compare-and-replace attempt
     * @param now     current shared-clock instant
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> consumeApproved(
            final DeviceCodeGrant grant,
            final String key,
            final ExpiringValue<DeviceCodeCache.Entry> stored,
            final DeviceCodeCache.Entry entry,
            final ConsumerMetadata client,
            final Context context,
            final Timeout timeout,
            final int attempt,
            final Instant now) {
        final DeviceCodeCache.Entry consumed = new DeviceCodeCache.Entry(entry.providerId(), entry.clientId(),
                entry.userCode(), entry.scope(), DeviceCodeCache.Status.CONSUMED, entry.interval(), Optional.of(now),
                entry.subjectId());
        return replaceDevice(key, stored, consumed, timeout).thenCompose(replaced -> {
            if (replaced == null) {
                return completed(storeFailure("OAuth 2.x approved device state update failed"));
            }
            if (!replaced && attempt < MAXIMUM_CREATE_ATTEMPTS) {
                return deviceCode(grant, client, context, timeout, attempt + 1);
            }
            if (!replaced) {
                return completed(storeFailure("OAuth 2.x approved device state contention exceeded its limit"));
            }
            return issue(
                    new Grant(client, entry.subjectId().getOrNull(), entry.scope(), List.of(), GrantType.DEVICE_CODE,
                            options.grantTypesSupported().contains(GrantType.REFRESH_TOKEN), Optional.empty(),
                            Optional.empty(), Optional.empty(), Optional.empty()),
                    context,
                    timeout);
        });
    }

    /**
     * Performs one atomic device-state replacement and normalizes backend failures to a nullable result.
     *
     * @param key      isolated device-code digest
     * @param expected current expiring value
     * @param update   replacement device entry
     * @param timeout  shared operation timeout used to compute the remaining state TTL
     * @return stage containing true/false, or {@code null} after a backend failure
     */
    private CompletionStage<Boolean> replaceDevice(
            final String key,
            final ExpiringValue<DeviceCodeCache.Entry> expected,
            final DeviceCodeCache.Entry update,
            final Timeout timeout) {
        final long ttlMillis = Duration.between(timeout.clock().now(), expected.expiresAt()).toMillis();
        if (ttlMillis <= 0L) {
            return CompletableFuture.completedFuture(false);
        }
        try {
            return services.deviceCodeCache().update(key, expected, new ExpiringValue<>(update, expected.expiresAt()))
                    .handle((replaced, thrown) -> thrown == null ? Boolean.TRUE.equals(replaced) : null);
        } catch (RuntimeException exception) {
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Persists access and optional initial refresh token state for an already authorized internal grant.
     *
     * @param grant   immutable authorized token grant
     * @param context immutable invocation context
     * @param timeout shared operation timeout
     * @return asynchronous standard token response outcome
     */
    CompletionStage<Outcome<TokenEndpointResponse>> issue(
            final Grant grant,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(grant, "OAuth 2.x internal token grant must not be null");
        Assert.notNull(context, "OAuth 2.x token context must not be null");
        Assert.notNull(timeout, "OAuth 2.x token timeout must not be null");
        if (grant.authorizationId().isPresent()) {
            return createAccess(grant, context, timeout, 1);
        }
        return createAuthorization(grant, context, timeout, 1);
    }

    /**
     * Allocates and persists a new authorization lifecycle before issuing its credentials.
     *
     * @param grant   validated internal grant without an authorization identifier
     * @param context immutable invocation context
     * @param timeout shared end-to-end operation timeout
     * @param attempt one-based collision retry attempt
     * @return asynchronous token response outcome
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> createAuthorization(
            final Grant grant,
            final Context context,
            final Timeout timeout,
            final int attempt) {
        final String authorizationId = tokenMaterial.create();
        final Instant expiresAt = timeout.clock().now()
                .plus(grant.refreshToken() ? options.refreshTokenLifetime() : options.accessTokenLifetime());
        final AuthorizationCache.Entry entry = new AuthorizationCache.Entry(providerId, grant.clientId(),
                AuthorizationCache.Status.ACTIVE);
        final CompletionStage<Boolean> creation;
        try {
            creation = services.authorizationCache()
                    .issue(AuthorizationCache.key(providerId, authorizationId), new ExpiringValue<>(entry, expiresAt));
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x authorization state persistence failed"));
        }
        return creation.handle((created, thrown) -> new CreateResult(Boolean.TRUE.equals(created), thrown))
                .thenCompose(result -> {
                    if (result.failure() != null) {
                        return completed(storeFailure("OAuth 2.x authorization state persistence failed"));
                    }
                    if (!result.created() && attempt < MAXIMUM_CREATE_ATTEMPTS) {
                        return createAuthorization(grant, context, timeout, attempt + 1);
                    }
                    if (!result.created()) {
                        return completed(storeFailure("OAuth 2.x authorization state allocation failed"));
                    }
                    final Grant authorized = grant.withAuthorizationId(authorizationId);
                    return createAccess(authorized, context, timeout, 1).thenCompose(outcome -> {
                        if (outcome instanceof Outcome.Succeeded<?>) {
                            return completed(outcome);
                        }
                        try {
                            return services.authorizationCache()
                                    .delete(AuthorizationCache.key(providerId, authorizationId))
                                    .handle((ignored, failure) -> outcome);
                        } catch (RuntimeException exception) {
                            return completed(outcome);
                        }
                    });
                });
    }

    /**
     * Creates one opaque access-token entry, retrying only create-if-absent collisions.
     *
     * @param grant   authorized token grant
     * @param context immutable invocation context
     * @param timeout shared operation timeout
     * @param attempt one-based create attempt number
     * @return asynchronous token outcome
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> createAccess(
            final Grant grant,
            final Context context,
            final Timeout timeout,
            final int attempt) {
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE,
                                    "OAuth 2.x token issuance has no remaining timeout")));
        }
        final String accessToken = tokenMaterial.create();
        final String accessKey = tokenMaterial.key(accessToken);
        final Instant expiresAt = timeout.clock().now().plus(options.accessTokenLifetime());
        final ExpiringValue<AccessTokenCache.Entry> value = new ExpiringValue<>(new AccessTokenCache.Entry(providerId,
                grant.clientId(), grant.subjectId(), grant.authorizationId().getOrNull(), grant.scope(),
                grant.audience(), grant.actorSubjectId(), grant.confirmation(), grant.openIdBinding()), expiresAt);
        final CompletionStage<Boolean> creation;
        try {
            creation = services.accessTokenCache().issue(accessKey, value);
        } catch (RuntimeException exception) {
            return completed(storeFailure("OAuth 2.x access token persistence failed"));
        }
        return creation.handle((created, thrown) -> new CreateResult(Boolean.TRUE.equals(created), thrown))
                .thenCompose(result -> {
                    if (result.failure() != null) {
                        return completed(storeFailure("OAuth 2.x access token persistence failed"));
                    }
                    if (!result.created() && attempt < MAXIMUM_CREATE_ATTEMPTS) {
                        return createAccess(grant, context, timeout, attempt + 1);
                    }
                    if (!result.created()) {
                        return completed(storeFailure("OAuth 2.x access token allocation failed"));
                    }
                    if (!grant.refreshToken()) {
                        final TokenEndpointResponse response = response(accessToken, null, grant);
                        return response instanceof TokenResponse tokenResponse
                                ? augment(tokenResponse, grant, accessKey, null, context, timeout)
                                : completed(Outcome.succeeded(response));
                    }
                    return createRefresh(accessToken, accessKey, grant, context, timeout, 1);
                });
    }

    /**
     * Creates an initial active refresh-token family after its access token has been persisted.
     *
     * @param accessToken already persisted opaque access token
     * @param accessKey   isolated access-token digest used for compensating cleanup
     * @param grant       authorized token grant
     * @param context     immutable invocation context
     * @param timeout     shared operation timeout
     * @param attempt     one-based refresh create attempt
     * @return asynchronous complete token response outcome
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> createRefresh(
            final String accessToken,
            final String accessKey,
            final Grant grant,
            final Context context,
            final Timeout timeout,
            final int attempt) {
        final String refreshToken = tokenMaterial.create();
        final String familyId = grant.authorizationId().getOrNull();
        final Instant expiresAt = timeout.clock().now().plus(options.refreshTokenLifetime());
        final ExpiringValue<RefreshTokenCache.Entry> value = new ExpiringValue<>(new RefreshTokenCache.Entry(providerId,
                grant.clientId(), grant.subjectId(), familyId, 0L, grant.scope(), grant.audience(),
                grant.confirmation(), grant.openIdBinding(), RefreshTokenCache.Status.ACTIVE), expiresAt);
        final CompletionStage<Boolean> creation;
        try {
            creation = services.refreshTokenCache().issue(tokenMaterial.key(refreshToken), value);
        } catch (RuntimeException exception) {
            return cleanupAccess(accessKey, "OAuth 2.x refresh token persistence failed");
        }
        return creation.handle((created, thrown) -> new CreateResult(Boolean.TRUE.equals(created), thrown))
                .thenCompose(result -> {
                    if (result.failure() != null) {
                        return cleanupAccess(accessKey, "OAuth 2.x refresh token persistence failed");
                    }
                    if (!result.created() && attempt < MAXIMUM_CREATE_ATTEMPTS) {
                        return createRefresh(accessToken, accessKey, grant, context, timeout, attempt + 1);
                    }
                    if (!result.created()) {
                        return cleanupAccess(accessKey, "OAuth 2.x refresh token allocation failed");
                    }
                    return augment(
                            (TokenResponse) response(accessToken, refreshToken, grant),
                            grant,
                            accessKey,
                            tokenMaterial.key(refreshToken),
                            context,
                            timeout);
                });
    }

    /**
     * Applies the compiled token-response profile and compensates every persisted token when augmentation fails.
     *
     * @param response   base OAuth token response whose ID Token is absent
     * @param grant      exact authorized internal grant
     * @param accessKey  persisted access-token digest
     * @param refreshKey persisted refresh-token digest, or {@code null} when none was issued
     * @param context    immutable invocation context
     * @param timeout    shared operation timeout
     * @return augmented response or a failure after best-effort removal of all unreturned token state
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> augment(
            final TokenResponse response,
            final Grant grant,
            final String accessKey,
            final String refreshKey,
            final Context context,
            final Timeout timeout) {
        final CompletionStage<Outcome<TokenEndpointResponse>> stage;
        try {
            stage = augmenter.augment(response, grant, context, timeout);
        } catch (RuntimeException exception) {
            return cleanupIssued(accessKey, refreshKey, storeFailure("OAuth 2.x token response augmentation failed"));
        }
        return stage
                .handle(
                        (outcome, thrown) -> thrown == null && outcome != null ? outcome
                                : Outcome.<TokenEndpointResponse>failed(
                                        failure(
                                                ErrorCode._500,
                                                OAuth2ErrorCode.SERVER_ERROR,
                                                "OAuth 2.x token response augmentation failed")))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<TokenEndpointResponse> success -> success.value() == null
                            ? cleanupIssued(
                                    accessKey,
                                    refreshKey,
                                    storeFailure("OAuth 2.x token response augmentation returned no response"))
                            : completed(Outcome.succeeded(success.value()));
                    case Outcome.Rejected<TokenEndpointResponse> rejected -> cleanupIssued(
                            accessKey,
                            refreshKey,
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<TokenEndpointResponse> failed -> cleanupIssued(
                            accessKey,
                            refreshKey,
                            Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Deletes all token state that must not survive a failed response augmentation.
     *
     * @param accessKey  persisted access-token digest
     * @param refreshKey persisted refresh-token digest, or {@code null}
     * @param outcome    original rejected or failed outcome to preserve
     * @return stage completing with the original outcome after best-effort cleanup
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> cleanupIssued(
            final String accessKey,
            final String refreshKey,
            final Outcome<TokenEndpointResponse> outcome) {
        CompletionStage<Boolean> cleanup;
        try {
            cleanup = services.accessTokenCache().revoke(accessKey);
        } catch (RuntimeException exception) {
            return completed(outcome);
        }
        if (refreshKey != null) {
            cleanup = cleanup.handle((ignored, thrown) -> Boolean.TRUE).thenCompose(ignored -> {
                try {
                    return services.refreshTokenCache().revoke(refreshKey);
                } catch (RuntimeException exception) {
                    return CompletableFuture.completedFuture(false);
                }
            });
        }
        return cleanup.handle((ignored, thrown) -> outcome);
    }

    /**
     * Deletes an unreturned access token after dependent refresh-token persistence fails.
     *
     * @param accessKey   isolated access-token digest
     * @param description safe failure description
     * @return asynchronous failed token outcome after best-effort atomic cleanup
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> cleanupAccess(
            final String accessKey,
            final String description) {
        try {
            return services.accessTokenCache().revoke(accessKey).handle((ignored, thrown) -> storeFailure(description));
        } catch (RuntimeException exception) {
            return completed(storeFailure(description));
        }
    }

    /**
     * Creates the standard successful bearer response for persisted token state.
     *
     * @param accessToken  issued opaque access token
     * @param refreshToken optional issued opaque refresh token, or {@code null}
     * @param grant        authorized internal grant
     * @return immutable standard token response
     */
    private TokenEndpointResponse response(final String accessToken, final String refreshToken, final Grant grant) {
        final Optional<Scope> scope = grant.scope().isEmpty() ? Optional.empty()
                : Optional.of(new Scope(grant.scope()));
        if (grant.grantType() == GrantType.TOKEN_EXCHANGE) {
            return new TokenExchangeResponse(accessToken, grant.issuedTokenType().getOrNull(), TokenType.BEARER,
                    Optional.of(options.accessTokenLifetime().toSeconds()), scope, Optional.ofNullable(refreshToken),
                    new JsonValue.ObjectValue(Map.of()));
        }
        return new TokenResponse(accessToken, TokenType.BEARER, Optional.of(options.accessTokenLifetime().toSeconds()),
                Optional.ofNullable(refreshToken), scope, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Validates a scope list against every supplied exact allowlist.
     *
     * @param scope   candidate scope-token list
     * @param allowed one or more exact allowlists
     * @return whether the scope is syntactically valid and contained by every allowlist
     */
    @SafeVarargs
    private final boolean validScope(final List<String> scope, final Set<String>... allowed) {
        try {
            for (Set<String> values : allowed) {
                scopeValidator.validateRequested(scope, values);
            }
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /**
     * Implements the pure OAuth profile without anonymous or reflective extension behavior.
     *
     * @author Kimi Liu
     */
    private enum PassThroughAugmenter implements Augmenter {

        /**
         * Shared stateless pass-through instance.
         */
        INSTANCE;

        /**
         * Returns the exact base response without adding an ID Token.
         *
         * @param response base OAuth token response
         * @param grant    exact authorized internal grant
         * @param context  immutable invocation context
         * @param timeout  shared operation timeout
         * @return completed successful outcome containing {@code response}
         */
        @Override
        public CompletionStage<Outcome<TokenEndpointResponse>> augment(
                final TokenResponse response,
                final Grant grant,
                final Context context,
                final Timeout timeout) {
            Assert.notNull(response, "OAuth 2.x base token response must not be null");
            Assert.notNull(grant, "OAuth 2.x internal grant must not be null");
            Assert.notNull(context, "OAuth 2.x token context must not be null");
            Assert.notNull(timeout, "OAuth 2.x token timeout must not be null");
            return completed(Outcome.succeeded(response));
        }

    }

    /**
     * Extends a successfully persisted base token response for a compiled higher-level protocol profile.
     * <p>
     * Implementations may compose the unchanged base response into a higher-level standard token endpoint response. A
     * failure causes the issuer to remove every access or refresh token created for the response before returning the
     * failure to the token endpoint.
     * </p>
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface Augmenter {

        /**
         * Returns the deterministic pure-OAuth augmenter that leaves every response unchanged.
         *
         * @return shared stateless pass-through augmenter
         */
        static Augmenter passThrough() {
            return PassThroughAugmenter.INSTANCE;
        }

        /**
         * Augments one persisted-token response without changing its OAuth members.
         *
         * @param response base OAuth token response with no ID Token
         * @param grant    exact authorized internal grant
         * @param context  immutable invocation context
         * @param timeout  shared end-to-end operation timeout
         * @return stage containing the augmented response, expected rejection, or operational failure
         */
        CompletionStage<Outcome<TokenEndpointResponse>> augment(
                TokenResponse response,
                Grant grant,
                Context context,
                Timeout timeout);

    }

    /**
     * Carries the exact authorized state from a grant processor into common opaque-token persistence.
     *
     * @param consumer        verified immutable consumer registration
     * @param subjectId       authorized subject or client identifier
     * @param scope           effective non-expanding scope
     * @param audience        intended resource audience
     * @param grantType       exact standard grant type that produced this internal grant
     * @param refreshToken    whether an initial refresh-token family must be created
     * @param issuedTokenType optional RFC 8693 issued-token-type response value
     * @param actorSubjectId  optional RFC 8693 acting-subject identifier
     * @param confirmation    optional safe sender-constraining confirmation identifier
     * @param openIdBinding   optional OpenID Connect authorization context inherited from the original code
     * @param authorizationId optional existing authorization identifier; initial grants allocate one before issuance
     * @author Kimi Liu
     */
    public record Grant(ConsumerMetadata consumer, String subjectId, List<String> scope, List<String> audience,
            GrantType grantType, boolean refreshToken, Optional<String> issuedTokenType,
            Optional<String> actorSubjectId, Optional<String> confirmation,
            Optional<AuthorizationCodeCache.OpenIdBinding> openIdBinding, Optional<String> authorizationId) {

        /**
         * Creates a grant that requires the issuer to allocate a new authorization lifecycle.
         *
         * @param consumer        verified immutable consumer registration
         * @param subjectId       authorized subject or client identifier
         * @param scope           effective non-expanding scope
         * @param audience        intended resource audience
         * @param grantType       exact standard grant type
         * @param refreshToken    whether an initial refresh-token family must be created
         * @param issuedTokenType optional RFC 8693 issued-token-type response value
         * @param actorSubjectId  optional RFC 8693 acting-subject identifier
         * @param confirmation    optional sender-constraining confirmation identifier
         * @param openIdBinding   optional OpenID Connect authorization context
         */
        public Grant(ConsumerMetadata consumer, String subjectId, List<String> scope, List<String> audience,
                GrantType grantType, boolean refreshToken, Optional<String> issuedTokenType,
                Optional<String> actorSubjectId, Optional<String> confirmation,
                Optional<AuthorizationCodeCache.OpenIdBinding> openIdBinding) {
            this(consumer, subjectId, scope, audience, grantType, refreshToken, issuedTokenType, actorSubjectId,
                    confirmation, openIdBinding, Optional.empty());
        }

        /**
         * Validates and freezes the internal authorized grant.
         *
         * @throws IllegalArgumentException if required text, a collection, or an optional container is invalid
         */
        public Grant {
            Assert.notNull(consumer, "OAuth 2.x internal grant consumer must not be null");
            Assert.notBlank(subjectId, "OAuth 2.x internal grant subject id must not be blank");
            scope = immutable(scope, "OAuth 2.x internal grant scope");
            audience = immutable(audience, "OAuth 2.x internal grant audience");
            Assert.notNull(grantType, "OAuth 2.x internal grant type must not be null");
            Assert.notNull(issuedTokenType, "OAuth 2.x issued token type container must not be null");
            issuedTokenType = Optional.ofNullable(issuedTokenType.getOrNull());
            Assert.notNull(actorSubjectId, "OAuth 2.x actor subject container must not be null");
            actorSubjectId = Optional.ofNullable(actorSubjectId.getOrNull());
            Assert.notNull(confirmation, "OAuth 2.x token confirmation container must not be null");
            confirmation = Optional.ofNullable(confirmation.getOrNull());
            Assert.notNull(openIdBinding, "OAuth 2.x OpenID Connect binding container must not be null");
            openIdBinding = Optional.ofNullable(openIdBinding.getOrNull());
            Assert.notNull(authorizationId, "OAuth 2.x authorization id container must not be null");
            if (authorizationId.isPresent()) {
                Assert.notBlank(authorizationId.getOrNull(), "OAuth 2.x authorization id must not be blank");
            }
            authorizationId = Optional.ofNullable(authorizationId.getOrNull());
            Assert.isTrue(
                    openIdBinding.isEmpty() || grantType == GrantType.AUTHORIZATION_CODE
                            || grantType == GrantType.REFRESH_TOKEN,
                    "OpenID Connect binding is permitted only for authorization-code and refresh grants");
        }

        /**
         * Returns the identifier from the verified immutable consumer snapshot.
         *
         * @return verified consumer identifier
         */
        public String clientId() {
            return consumer.id();
        }

        /**
         * Returns this immutable grant associated with a newly allocated authorization lifecycle.
         *
         * @param value non-blank authorization identifier
         * @return copied grant carrying the identifier
         */
        private Grant withAuthorizationId(final String value) {
            return new Grant(consumer, subjectId, scope, audience, grantType, refreshToken, issuedTokenType,
                    actorSubjectId, confirmation, openIdBinding, Optional.of(value));
        }

        /**
         * Copies one ordered internal lexical value list.
         *
         * @param values source values
         * @param label  safe semantic label
         * @return immutable detached list
         */
        private static List<String> immutable(final List<String> values, final String label) {
            Assert.notNull(values, label + " must not be null");
            final List<String> copy = new ArrayList<>(values.size());
            for (String value : values) {
                copy.add(Assert.notBlank(value, label + " must not contain blank values"));
            }
            return List.copyOf(copy);
        }

    }

    /**
     * Couples an atomic create result with a normalized completion failure.
     *
     * @param created whether the value was created
     * @param failure completion failure, or {@code null}
     * @author Kimi Liu
     */
    private record CreateResult(boolean created, Throwable failure) {

    }

    /**
     * Couples a cache value with a normalized completion failure.
     *
     * @param value   returned cache value
     * @param failure completion failure, or {@code null}
     * @param <T>     cache value type
     * @author Kimi Liu
     */
    private record CacheResult<T>(T value, Throwable failure) {

    }

}
