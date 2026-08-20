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
package org.miaixz.bus.auth.vendor.amazon.internal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.*;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationResponseDecoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenResponseDecoder;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.amazon.AmazonDefinition;
import org.miaixz.bus.auth.vendor.amazon.AmazonSourceSettings;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements Login with Amazon browser authentication while preserving standard OAuth 2.0 public operations.
 * <p>
 * Authorization and token requests are delegated unchanged to the shared OAuth 2.0 clients. The Amazon-only token
 * information and customer profile resources remain private to this adapter and produce only a verified
 * {@link ExternalIdentity} whose subject is the historical {@code user_id} value.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AmazonSourceAdapter implements VendorAdapter {

    /**
     * Trusted authority recorded for verified Login with Amazon identity evidence.
     */
    private static final String AUTHORITY = "https://api.amazon.com";

    /**
     * Maximum accepted bytes for one Amazon JSON resource response.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum accepted JSON container depth for an Amazon resource response.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Registered Source identifier copied into verified external identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Login with Amazon definition.
     */
    private final VendorDefinition.Definition variantDefinition;

    /**
     * Validated externally loaded Login with Amazon settings.
     */
    private final AmazonSourceSettings settings;

    /**
     * Caller-owned runtime, JSON, network, and execution dependencies.
     */
    private final ExecutionServices services;

    /**
     * Shared standard OAuth 2.0 authorization and token implementation.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time state and optional S256 verifier lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Strict standard OAuth 2.0 authorization response decoder.
     */
    private final AuthorizationResponseDecoder authorizationResponseDecoder;

    /**
     * Creates one Source-bound Login with Amazon adapter from the frozen default definition.
     *
     * @param namespaceId       registration namespace used to isolate state and PKCE material
     * @param sourceId          registered Source identifier
     * @param vendorDefinition  selected Login with Amazon definition
     * @param variantDefinition selected default variant definition
     * @param settings          decoded externally loaded Amazon settings
     * @param services          caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, definition, or callback settings differ from the frozen
     *                                  Login with Amazon profile
     */
    public AmazonSourceAdapter(final String namespaceId, final String sourceId, final AmazonDefinition vendorDefinition,
            final VendorDefinition.Definition variantDefinition, final AmazonSourceSettings settings,
            final ExecutionServices services) {
        Assert.notNull(vendorDefinition, "Login with Amazon definition must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Login with Amazon Source id must not be blank");
        this.variantDefinition = Assert.notNull(variantDefinition, "Login with Amazon definition must not be null");
        this.settings = Assert.notNull(settings, "Login with Amazon settings must not be null");
        this.services = Assert.notNull(services, "Login with Amazon execution services must not be null");
        if (!AmazonDefinition.ID.equals(vendorDefinition.type())
                || !vendorDefinition.variant(AmazonDefinition.DEFAULT).equals(variantDefinition)
                || !AmazonDefinition.DEFAULT.equals(variantDefinition.variant())
                || variantDefinition.protocol() != Protocol.OAUTH2 || !AmazonDefinition.ID.equals(settings.vendor())
                || !AmazonDefinition.DEFAULT.equals(settings.variant()) || settings.redirectUri().isEmpty()) {
            throw new ValidateException("Login with Amazon adapter requires the amazon/default OAuth 2.0 definition");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variantDefinition, settings, services);
        this.standardAdapter = standardAdapter(variantDefinition, settings, services, redirectManager);
        this.authorizationResponseDecoder = new AuthorizationResponseDecoder();
    }

    /**
     * Composes Login with Amazon's standard OAuth operations from protocol-owned clients and codecs.
     *
     * @param definition      selected Login with Amazon definition
     * @param settings        validated Source deployment settings
     * @param services        caller-owned execution services
     * @param redirectManager shared browser correlation lifecycle
     * @return adapter containing the standard authorization and token bindings
     */
    private static StandardAdapter standardAdapter(
            final VendorDefinition.Definition definition,
            final AmazonSourceSettings settings,
            final ExecutionServices services,
            final RedirectManager redirectManager) {
        final var targets = definition.targets().resolve(settings);
        final OAuth2ClientSettings oauthSettings = new OAuth2ClientSettings(targets.authorization(), targets.token(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                settings.clientId(), Set.of(settings.redirectUri().getOrNull()),
                Endpoint.Authentication.CLIENT_SECRET_POST, Optional.of(settings.credential()), settings.pkce(), false);
        final OAuth2Client oauthClient = new OAuth2Client(
                new AuthorizationClient(oauthSettings,
                        new AuthorizationRequestEncoder(targets.authorization().getOrNull())),
                new TokenClient(oauthSettings, services, new TokenRequestEncoder(),
                        new TokenResponseDecoder(services.jsonProvider())),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        return new StandardAdapter(definition, settings, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2SourceProfile.AUTHORIZATION, oauthClient::authorize),
                        new StandardAdapter.Binding<>(OAuth2SourceProfile.TOKEN, oauthClient::token)));
    }

    /**
     * Requires the Bearer token type used by the historical Amazon profile operation.
     *
     * @param response decoded standard token response
     * @return unchanged standard response or a safe protocol rejection
     */
    private static Outcome<TokenResponse> token(final TokenResponse response) {
        return TokenType.BEARER.equals(response.tokenType()) ? Outcome.succeeded(response)
                : rejected("Login with Amazon token response must use the Bearer token type");
    }

    /**
     * Reads one required non-blank Amazon string member.
     *
     * @param object decoded object
     * @param name   exact platform member name
     * @return non-blank member value
     * @throws ValidateException if the member is absent, blank, or not a string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Login with Amazon required profile member is invalid");
        }
        return string.value();
    }

    /**
     * Reads one optional non-blank Amazon string member.
     *
     * @param object decoded private Amazon resource object
     * @param name   exact platform member name
     * @return decoded value or {@code null} when absent
     * @throws ValidateException if a present member is not a non-blank string
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Login with Amazon optional profile member is invalid");
        }
        return string.value();
    }

    /**
     * Detects the private Amazon error envelope without retaining its diagnostic content.
     *
     * @param object decoded private Amazon resource object
     * @return whether a non-null error member is present
     */
    private static boolean hasError(final JsonValue.ObjectValue object) {
        final JsonValue value = object.values().get(OAuth2.Parameters.ERROR);
        return value != null && !(value instanceof JsonValue.NullValue);
    }

    /**
     * Converts one standard authorization error to a safe rejected outcome.
     *
     * @param error decoded standard authorization error branch
     * @param <T>   expected success type
     * @return rejected outcome retaining only registered non-sensitive error identifiers
     */
    private static <T> Outcome<T> oauthError(final AuthorizationResponseDecoder.Error error) {
        final Map<String, JsonValue> details = new LinkedHashMap<>();
        details.put("oauth_error", new JsonValue.StringValue(error.response().error().value()));
        final String errorUri = error.response().errorUri().getOrNull();
        if (errorUri != null) {
            details.put(OAuth2.Parameters.ERROR_URI, new JsonValue.StringValue(errorUri));
        }
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400,
                        "Login with Amazon authorization endpoint returned a standard error",
                        new JsonValue.ObjectValue(details)));
    }

    /**
     * Narrows a delegated outcome through the declared capability response type.
     *
     * @param stage        delegated outcome stage
     * @param responseType declared response class
     * @param <S>          expected successful response type
     * @return type-safe delegated outcome
     */
    private static <S> CompletionStage<Outcome<S>> narrow(
            final CompletionStage<? extends Outcome<?>> stage,
            final Class<S> responseType) {
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<?> success -> Outcome.succeeded(responseType.cast(success.value()));
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
        });
    }

    /**
     * Creates a provider-neutral empty JSON object for standard extension members.
     *
     * @return immutable empty JSON object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Creates a completed undeclared-capability rejection.
     *
     * @param <S> expected successful response type
     * @return completed rejection stage
     */
    private static <S> CompletionStage<Outcome<S>> missing() {
        return completed(rejected("Login with Amazon capability is not declared"));
    }

    /**
     * Creates a completed request-shape or unavailable-operation rejection.
     *
     * @param <S> expected successful response type
     * @return completed rejection stage
     */
    private static <S> CompletionStage<Outcome<S>> mismatch() {
        return completed(rejected("Login with Amazon capability request is invalid"));
    }

    /**
     * Creates an already completed asynchronous outcome.
     *
     * @param outcome completed outcome value
     * @param <T>     successful value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe expected protocol or platform rejection.
     *
     * @param description non-sensitive rejection description
     * @param <T>         expected successful value type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, emptyObject()));
    }

    /**
     * Creates a safe operational failure using the shared Bus error taxonomy.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param <T>         expected successful value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final Errors code, final String description) {
        return Outcome.failed(new Outcome.Failure(code, description, emptyObject()));
    }

    /**
     * Returns the exact capability manifest frozen by the selected Login with Amazon definition.
     *
     * @return immutable Source authentication, authorization, and token capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variantDefinition.manifest();
    }

    /**
     * Routes Source authentication and the two registered standard OAuth 2.0 operations.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Amazon response objects
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Login with Amazon capability must not be null");
        Assert.notNull(context, "Login with Amazon invocation context must not be null");
        Assert.notNull(timeout, "Login with Amazon invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return missing();
        }
        if (capability.key().equals(SourceAuthentication.INITIATE.key())
                && request instanceof SourceAuthenticationRequest.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (capability.key().equals(SourceAuthentication.COMPLETE.key())
                && request instanceof SourceAuthenticationRequest.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::identity, context, timeout),
                    capability.responseType());
        }
        if (capability.equals(OAuth2SourceProfile.AUTHORIZATION)
                && request instanceof AuthorizationRequest authorization) {
            return valid(authorization) ? standardAdapter.invoke(capability, request, context, timeout) : mismatch();
        }
        if (capability.equals(OAuth2SourceProfile.TOKEN) && request instanceof TokenRequest tokenRequest) {
            return narrow(token(tokenRequest, context, timeout), capability.responseType());
        }
        if (standardAdapter.manifest().capabilities().contains(capability)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return mismatch();
    }

    /**
     * Builds the standard authorization-code request with optional generated S256 material.
     *
     * @param initiation generated state and optional PKCE challenge
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return standard-client-produced authorization redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        try {
            final var challenge = initiation.codeChallenge().getOrNull();
            if ((settings.pkce() && (challenge == null || !PkceMethod.S256.equals(challenge.method())))
                    || (!settings.pkce() && challenge != null)) {
                return completed(
                        failed(
                                ErrorCode._500,
                                "Login with Amazon browser flow generated PKCE material outside its registration policy"));
            }
            final AuthorizationRequest authorization = new AuthorizationRequest(ResponseType.CODE, settings.clientId(),
                    settings.redirectUri(), Optional.of(new Scope(requestedScopes())), Optional.of(initiation.state()),
                    challenge == null ? Optional.empty() : Optional.of(challenge.value()),
                    challenge == null ? Optional.empty() : Optional.of(PkceMethod.S256.value()), emptyObject());
            return standardAdapter.invoke(OAuth2SourceProfile.AUTHORIZATION, authorization, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<org.miaixz.bus.fabric.UnoUrl> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<org.miaixz.bus.fabric.UnoUrl> rejected -> Outcome
                                .rejected(rejected.failure());
                        case Outcome.Failed<org.miaixz.bus.fabric.UnoUrl> failed -> Outcome.failed(failed.failure());
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Login with Amazon authorization request is invalid"));
        }
    }

    /**
     * Extracts the required callback state from exactly one standard success or error branch.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return unique non-blank state value
     * @throws ValidateException if the callback target, method, branch, multiplicity, or state is invalid
     */
    private String state(final Callback.Inbound callback) {
        return switch (callback(callback)) {
            case AuthorizationResponseDecoder.Success success -> success.response().state()
                    .orElseThrow(() -> new ValidateException("Login with Amazon authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Login with Amazon authorization error requires state"));
        };
    }

    /**
     * Redeems a correlated authorization response and validates the returned Amazon identity.
     *
     * @param completion consumed callback correlation and optional PKCE verifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Login with Amazon authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(oauthError(error));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        if (response.issuer().isPresent()) {
            return completed(rejected("Login with Amazon returned an unregistered authorization response issuer"));
        }
        final String verifier = completion.codeVerifier().isPresent() ? completion.codeVerifier().getOrNull().value()
                : null;
        if (settings.pkce() != (verifier != null)) {
            return completed(
                    failed(
                            ErrorCode._500,
                            "Login with Amazon callback PKCE material does not match its registration policy"));
        }
        final TokenRequest tokenRequest = new TokenRequest(new AuthorizationCodeGrant(response.code(),
                settings.redirectUri(), Optional.empty(), Optional.ofNullable(verifier)), emptyObject());
        return token(tokenRequest, context, timeout).thenCompose(token -> switch (token) {
            case Outcome.Succeeded<TokenResponse> success -> identity(success.value(), context, timeout);
            case Outcome.Rejected<TokenResponse> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<TokenResponse> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Executes only the two standard grants registered by Login with Amazon.
     *
     * @param request standard authorization-code or refresh-token request
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return unchanged standard token response after Amazon profile validation
     */
    private CompletionStage<Outcome<TokenResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        if (!request.extensions().values().isEmpty() || !valid(request.grant())) {
            return completed(rejected("Login with Amazon token endpoint does not support the requested grant type"));
        }
        return standardAdapter.invoke(OAuth2SourceProfile.TOKEN, request, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<TokenEndpointResponse> success -> success
                            .value() instanceof TokenResponse token ? token(token)
                                    : rejected("Login with Amazon token endpoint returned a non-OAuth token response");
                    case Outcome.Rejected<TokenEndpointResponse> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<TokenEndpointResponse> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Validates the standard authorization request against the selected Amazon registration without changing it.
     *
     * @param request standard OAuth 2.0 authorization request
     * @return {@code true} when client, redirect, scope, state, response type, extensions, and PKCE are exact
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request.scope().getOrNull();
        final String method = request.codeChallengeMethod().getOrNull();
        final boolean pkce = request.codeChallenge().isPresent() && PkceMethod.S256.value().equals(method);
        return ResponseType.CODE.equals(request.responseType()) && settings.clientId().equals(request.clientId())
                && settings.redirectUri().equals(request.redirectUri()) && scope != null
                && requestedScopes().equals(scope.values()) && request.state().isPresent()
                && request.extensions().values().isEmpty() && settings.pkce() == pkce
                && (settings.pkce() || request.codeChallengeMethod().isEmpty());
    }

    /**
     * Validates one standard token grant against the selected Amazon registration without interpreting token material.
     *
     * @param grant standard OAuth 2.0 grant
     * @return {@code true} for a registration-bound authorization-code grant or a non-expanding refresh-token grant
     */
    private boolean valid(final TokenRequest.Grant grant) {
        if (grant instanceof AuthorizationCodeGrant authorization) {
            final String clientId = authorization.clientId().getOrNull();
            return settings.redirectUri().equals(authorization.redirectUri())
                    && (clientId == null || settings.clientId().equals(clientId))
                    && settings.pkce() == authorization.codeVerifier().isPresent();
        }
        if (grant instanceof RefreshTokenGrant refresh) {
            final Scope scope = refresh.scope().getOrNull();
            return scope == null || requestedScopes().containsAll(scope.values());
        }
        return false;
    }

    /**
     * Validates token audience before retrieving the Bearer-authenticated customer profile.
     *
     * @param token   standard token response containing sensitive bearer material
     * @param context immutable invocation context retained for operation consistency
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final TokenResponse token,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "Login with Amazon identity context must not be null");
        return CompletableFuture.supplyAsync(() -> tokenInfo(token.accessToken(), timeout), services.executor())
                .thenCompose(info -> switch (info) {
                    case Outcome.Succeeded<JsonValue.ObjectValue> success -> audience(success.value())
                            ? CompletableFuture
                                    .supplyAsync(() -> profile(token.accessToken(), timeout), services.executor())
                            : completed(rejected("Login with Amazon access token audience does not match the client"));
                    case Outcome.Rejected<JsonValue.ObjectValue> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<JsonValue.ObjectValue> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Retrieves the Amazon token information resource using its registered query parameter.
     *
     * @param accessToken sensitive Bearer access token
     * @param timeout     shared end-to-end budget
     * @return strict JSON token information object
     */
    private Outcome<JsonValue.ObjectValue> tokenInfo(final String accessToken, final Timeout.Budget timeout) {
        final String endpoint = variantDefinition.targets().resolve(settings).introspection().getOrNull().url()
                .toString();
        return resource(endpoint, accessToken, true, timeout);
    }

    /**
     * Compares the private Amazon token information audience with the configured client identifier.
     *
     * @param tokenInfo strict token information object
     * @return {@code true} only for an exact non-blank string audience match
     */
    private boolean audience(final JsonValue.ObjectValue tokenInfo) {
        final TokenInformation information;
        try {
            information = TokenInformation.decode(tokenInfo);
        } catch (RuntimeException cause) {
            return false;
        }
        return !information.error() && settings.clientId().equals(information.audience());
    }

    /**
     * Retrieves and maps the verified Login with Amazon customer profile.
     *
     * @param accessToken sensitive Bearer access token
     * @param timeout     shared end-to-end budget
     * @return external identity or a closed platform failure
     */
    private Outcome<ExternalIdentity> profile(final String accessToken, final Timeout.Budget timeout) {
        final String endpoint = variantDefinition.targets().resolve(settings).userInfo().getOrNull().url().toString();
        return switch (resource(endpoint, accessToken, false, timeout)) {
            case Outcome.Succeeded<JsonValue.ObjectValue> success -> profile(success.value(), timeout);
            case Outcome.Rejected<JsonValue.ObjectValue> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<JsonValue.ObjectValue> failed -> Outcome.failed(failed.failure());
        };
    }

    /**
     * Maps one successful Amazon profile after requiring its stable historical {@code user_id}.
     *
     * @param profile strict Amazon customer profile object
     * @param timeout shared clock used to timestamp verification evidence
     * @return verified external identity or a safe rejection
     */
    private Outcome<ExternalIdentity> profile(final JsonValue.ObjectValue profile, final Timeout.Budget timeout) {
        try {
            final CustomerProfile customer = CustomerProfile.decode(profile);
            if (customer.error()) {
                return rejected("Login with Amazon profile resource returned an error");
            }
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("amazon_user_id", new JsonValue.StringValue(customer.userId()), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, customer.userId(), customer.attributes(), List.of(evidence)));
        } catch (RuntimeException cause) {
            return rejected("Login with Amazon profile response is invalid");
        }
    }

    /**
     * Executes one bounded Amazon JSON GET without exposing its token, URI, body, or platform envelope.
     *
     * @param endpoint    fixed definition-owned resource endpoint
     * @param accessToken sensitive access token
     * @param queryToken  whether the token uses the Amazon tokeninfo query parameter instead of Bearer authentication
     * @param timeout     shared end-to-end budget
     * @return strict JSON object or a safely classified failure
     */
    private Outcome<JsonValue.ObjectValue> resource(
            final String endpoint,
            final String accessToken,
            final boolean queryToken,
            final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Login with Amazon resource request has no remaining time budget");
        }
        try {
            final var builder = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy());
            if (queryToken) {
                builder.query(OAuth2.Parameters.ACCESS_TOKEN, accessToken);
            } else {
                builder.header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + accessToken);
            }
            try (HttpResponse response = builder.execute()) {
                if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
                    return failed(ErrorCode._429, "Login with Amazon resource endpoint rate limited the request");
                }
                if (response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
                    return failed(ErrorCode._502, "Login with Amazon resource endpoint is unavailable");
                }
                if (response.code() >= Http.Status.BAD_REQUEST) {
                    return rejected("Login with Amazon resource endpoint rejected the access token");
                }
                if (response.code() != Http.Status.OK
                        || !MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
                    return failed(ErrorCode._502, "Login with Amazon resource endpoint returned an invalid response");
                }
                final JsonValue parsed = services.jsonProvider()
                        .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
                if (!(parsed instanceof JsonValue.ObjectValue object)) {
                    return failed(ErrorCode._502, "Login with Amazon resource response must be a JSON object");
                }
                return Outcome.succeeded(object);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Login with Amazon resource request failed");
        }
    }

    /**
     * Validates the exact registered callback URI before standard OAuth response decoding.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     * @throws ValidateException if the callback target differs from the registered redirect URI
     */
    private AuthorizationResponseDecoder.Decoded callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Login with Amazon callback must not be null");
        if (!settings.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Login with Amazon callback URI does not match the registered redirect URI");
        }
        return authorizationResponseDecoder.decode(callback);
    }

    /**
     * Returns explicit registered scopes or immutable definition defaults.
     *
     * @return ordered effective Login with Amazon scopes
     */
    private List<String> requestedScopes() {
        return settings.scopes().isEmpty() ? variantDefinition.defaultScopes() : settings.scopes();
    }

    /**
     * Carries the private Login with Amazon token-information response used to bind an access token to its client.
     *
     * @param audience client identifier returned in the {@code aud} member
     * @param error    whether the response used Amazon's private error envelope
     * @author Kimi Liu
     */
    private record TokenInformation(String audience, boolean error) {

        /**
         * Decodes one private token-information object.
         *
         * @param object strict JSON token-information object
         * @return typed token information
         * @throws ValidateException if a successful object lacks a valid audience
         */
        private static TokenInformation decode(final JsonValue.ObjectValue object) {
            if (hasError(object)) {
                return new TokenInformation(null, true);
            }
            return new TokenInformation(requiredString(object, JwtClaims.AUDIENCE), false);
        }

    }

    /**
     * Carries the private Login with Amazon customer-profile response used to construct an external identity.
     *
     * @param userId     stable Amazon customer identifier
     * @param name       optional display name
     * @param email      optional email address
     * @param postalCode optional postal code
     * @param error      whether the response used Amazon's private error envelope
     * @author Kimi Liu
     */
    private record CustomerProfile(String userId, String name, String email, String postalCode, boolean error) {

        /**
         * Decodes one private customer-profile object.
         *
         * @param object strict JSON profile object
         * @return typed customer profile
         * @throws ValidateException if a successful response has an invalid registered member
         */
        private static CustomerProfile decode(final JsonValue.ObjectValue object) {
            if (hasError(object)) {
                return new CustomerProfile(null, null, null, null, true);
            }
            return new CustomerProfile(requiredString(object, "user_id"), optionalString(object, "name"),
                    optionalString(object, "email"), optionalString(object, "postal_code"), false);
        }

        /**
         * Adds one present profile value to the private attribute representation.
         *
         * @param values mutable assembly map confined to this conversion
         * @param member exact Amazon member name
         * @param value  decoded value or {@code null} when absent
         */
        private static void add(final Map<String, JsonValue> values, final String member, final String value) {
            if (value != null) {
                values.put(member, new JsonValue.StringValue(value));
            }
        }

        /**
         * Converts present private profile values to opaque external-identity attributes.
         *
         * @return immutable JSON attributes using Amazon member names
         */
        private JsonValue.ObjectValue attributes() {
            final Map<String, JsonValue> values = new LinkedHashMap<>();
            add(values, "name", name);
            add(values, "email", email);
            add(values, "postal_code", postalCode);
            return new JsonValue.ObjectValue(values);
        }

    }

}
