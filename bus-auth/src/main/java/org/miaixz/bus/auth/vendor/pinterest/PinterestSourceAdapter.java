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
package org.miaixz.bus.auth.vendor.pinterest;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationResponseDecoder;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements the preserved Pinterest OAuth 2.0 Source behind standard public contracts.
 * <p>
 * Public callers provide standard authorization and token requests. This adapter alone translates them into the
 * historical comma-delimited authorization, query-authenticated empty-form token call, and query profile call, then
 * maps the Pinterest envelope to a standard token response and a verified external identity.
 * </p>
 *
 * @author Kimi Liu
 */
public final class PinterestSourceAdapter implements VendorAdapter {

    /**
     * Trusted Pinterest authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://api.pinterest.com";

    /**
     * Exact historical profile projection requested from Pinterest.
     */
    private static final String PROFILE_FIELDS = "id,username,first_name,last_name,bio,image";

    /**
     * Maximum bounded JSON response size accepted from Pinterest.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum JSON nesting accepted from Pinterest responses.
     */
    private static final int MAXIMUM_JSON_DEPTH = 32;

    /**
     * Exact image variant retained by the historical projection.
     */
    private static final String AVATAR_VARIANT = "60x60";

    /**
     * Registered Source identifier copied into verified external identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Pinterest variant manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Pinterest registration options.
     */
    private final PinterestOptions options;

    /**
     * Caller-owned runtime, secret, JSON, network, clock, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Shared one-time browser-state coordinator.
     */
    private final RedirectManager redirectManager;

    /**
     * Uniform adapter that owns every public OAuth capability dispatch.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Strict standard authorization response decoder.
     */
    private final AuthorizationResponseDecoder callbackDecoder;

    /**
     * Creates one Source-bound Pinterest adapter from the frozen default manifest.
     *
     * @param namespaceId registration namespace isolating browser and credential state
     * @param sourceId    registered Source identifier
     * @param manifest    selected Pinterest manifest
     * @param variant     selected default variant manifest
     * @param options     decoded externally loaded Pinterest options
     * @param services    caller-owned runtime dependencies
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, manifest, options, or routing differ from the frozen variant
     */
    public PinterestSourceAdapter(final String namespaceId, final String sourceId, final PinterestManifest manifest,
            final VariantManifest.Variant variant, final PinterestOptions options, final DriverServices services) {
        final PinterestManifest selected = Assert.notNull(manifest, "Pinterest manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Pinterest Source id must not be blank");
        this.variant = Assert.notNull(variant, "Pinterest manifest must not be null");
        this.options = Assert.notNull(options, "Pinterest options must not be null");
        this.services = Assert.notNull(services, "Pinterest execution services must not be null");
        if (!PinterestManifest.ID.equals(selected.vendor())
                || !selected.variant(PinterestManifest.DEFAULT).equals(variant)
                || !PinterestManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !PinterestManifest.ID.equals(options.vendor())
                || !PinterestManifest.DEFAULT.equals(options.variant())) {
            throw new ValidateException("Pinterest adapter requires the pinterest/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.callbackDecoder = new AuthorizationResponseDecoder();
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION,
                                (request, context, timeout) -> authorization(request)),
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.TOKEN, this::token)));
    }

    /**
     * Validates and returns the exact historical 60-by-60 image object.
     *
     * @param value raw profile image member
     * @return immutable provider-neutral image object
     * @throws ValidateException if the expected variant or URL is absent or mistyped
     */
    private static JsonValue avatar(final JsonValue value) {
        if (!(value instanceof JsonValue.ObjectValue images)
                || !(images.values().get(AVATAR_VARIANT) instanceof JsonValue.ObjectValue avatar)
                || avatar.values().size() != 1 || !avatar.values().containsKey("url")) {
            throw new ValidateException("Pinterest profile image variant is invalid");
        }
        requiredString(avatar, "url");
        return new JsonValue.ObjectValue(Map.of(AVATAR_VARIANT, avatar));
    }

    /**
     * Verifies that a token envelope contains only Pinterest's documented success and failure members.
     *
     * @param object decoded token envelope
     * @return whether every member has registered token semantics
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            if (!switch (name) {
                case "status", "message", OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.TOKEN_TYPE -> true;
                default -> false;
            }) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies that a profile envelope contains only Pinterest's documented status wrapper members.
     *
     * @param object decoded profile envelope
     * @return whether every member has registered envelope semantics
     */
    private static boolean profileEnvelopeMembers(final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            if (!switch (name) {
                case "status", "message", "data" -> true;
                default -> false;
            }) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies that a profile payload contains only the members requested by the preserved projection.
     *
     * @param object decoded profile payload
     * @return whether every member belongs to the projection
     */
    private static boolean profileMembers(final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            if (!switch (name) {
                case "id", "username", "first_name", "last_name", "bio", "image" -> true;
                default -> false;
            }) {
                return false;
            }
        }
        return true;
    }

    /**
     * Materializes an operation-scoped client secret and clears the intermediate character buffer.
     *
     * @param lease open secret lease owned by the caller
     * @return transient string required by the HTTP query builder
     */
    private static String secret(final SecretLease lease) {
        final char[] material = lease.material();
        try {
            return new String(material);
        } finally {
            Arrays.fill(material, '\0');
        }
    }

    /**
     * Classifies one historical Pinterest failure envelope without retaining its message.
     *
     * @param status      original HTTP status
     * @param object      decoded status envelope
     * @param description safe operation description
     * @param <T>         expected success type
     * @return rejected request or failed upstream outcome
     */
    private static <T> Outcome<T> platformFailure(
            final int status,
            final JsonValue.ObjectValue object,
            final String description) {
        if (!"failure".equals(requiredString(object, "status"))) {
            return failed(ErrorCode._502, "Pinterest response contains an unknown status");
        }
        requiredString(object, "message");
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, description);
        }
        if (status >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(ErrorCode._502, description);
        }
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, emptyObject()));
    }

    /**
     * Maps a standard authorization error to a safe rejection.
     *
     * @param response decoded standard OAuth error response
     * @return rejected identity outcome
     */
    private static Outcome<ExternalIdentity> authorizationError(final AuthorizationErrorResponse response) {
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400, "Pinterest authorization endpoint returned a standard error",
                        new JsonValue.ObjectValue(
                                Map.of("oauth_error", new JsonValue.StringValue(response.error().value())))));
    }

    /**
     * Reads one required JSON object member.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return required object value
     * @throws ValidateException if the member is absent or another JSON type
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.ObjectValue nested)) {
            throw new ValidateException("Pinterest response requires an object member: " + name);
        }
        return nested;
    }

    /**
     * Reads one required non-blank JSON string member.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return required non-blank string
     * @throws ValidateException if the member is absent, blank, or mistyped
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = optionalString(object, name);
        if (value == null || value.isBlank()) {
            throw new ValidateException("Pinterest response requires a non-blank string member: " + name);
        }
        return value;
    }

    /**
     * Reads one optional JSON string member.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return string value or {@code null} when absent
     * @throws ValidateException if a present member is not a string
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Pinterest response member must be a JSON string: " + name);
        }
        return string.value();
    }

    /**
     * Narrows a delegated outcome through the capability's exact response class.
     *
     * @param stage        delegated outcome stage
     * @param responseType declared successful response type
     * @param <S>          expected successful response type
     * @return type-safe delegated stage
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
     * Creates an immutable empty JSON extension object.
     *
     * @return provider-neutral empty object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Creates an already completed asynchronous outcome.
     *
     * @param outcome completed outcome
     * @param <T>     successful value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe expected request or protocol rejection.
     *
     * @param description non-sensitive rejection description
     * @param <T>         expected successful value type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, emptyObject()));
    }

    /**
     * Creates a safe operational failure using a shared Bus error code.
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
     * Returns the exact frozen Pinterest capability manifest.
     *
     * @return immutable Source authentication and OAuth capability set
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and standard OAuth authorization and token operations.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing private Pinterest models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Pinterest capability must not be null");
        Assert.notNull(context, "Pinterest invocation context must not be null");
        Assert.notNull(timeout, "Pinterest invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Pinterest capability is not declared"));
        }
        if (capability.key().equals(SourceAuthentication.INITIATE.key())
                && request instanceof SourceAuthentication.Request.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (capability.key().equals(SourceAuthentication.COMPLETE.key())
                && request instanceof SourceAuthentication.Request.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::identity, context, timeout),
                    capability.responseType());
        }
        if (standardAdapter.manifest().capabilities().contains(capability)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return completed(rejected("Pinterest capability request is invalid"));
    }

    /**
     * Builds the Pinterest redirect around generated one-time state.
     *
     * @param initiation generated browser correlation material
     * @param context    immutable invocation context retained for the uniform signature
     * @param timeout    shared end-to-end time budget
     * @return prepared authorization redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "Pinterest authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "Pinterest browser material violates the frozen manifest"));
        }
        try {
            final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                    options.redirectUri(), scope(), Optional.of(initiation.state()), Optional.empty(), Optional.empty(),
                    emptyObject());
            return standardAdapter.invoke(OAuth2ClientScheme.AUTHORIZATION, request, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<UnoUrl> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<UnoUrl> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<UnoUrl> failed -> Outcome.failed(failed.failure());
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Pinterest authorization request is invalid"));
        }
    }

    /**
     * Encodes one validated standard request using Pinterest's registered comma-delimited scope.
     *
     * @param request standard authorization request
     * @return asynchronous exact Pinterest authorization URL outcome
     */
    private CompletionStage<Outcome<UnoUrl>> authorization(final AuthorizationRequest request) {
        if (!valid(request)) {
            return completed(rejected("Pinterest authorization request differs from the registered Source"));
        }
        try {
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final UnoUrl location = endpoint.url().newBuilder()
                    .query(OAuth2.Parameters.RESPONSE_TYPE, request.responseType().value())
                    .query(OAuth2.Parameters.CLIENT_ID, request.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, request.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.STATE, request.state().getOrNull())
                    .query(OAuth2.Parameters.SCOPE, String.join(Symbol.COMMA, effectiveScopes())).build();
            return completed(Outcome.succeeded(location));
        } catch (RuntimeException cause) {
            return completed(rejected("Pinterest authorization request is invalid"));
        }
    }

    /**
     * Validates one public standard authorization request against the Pinterest registration.
     *
     * @param request standard OAuth authorization request
     * @return whether every standard field matches the registered Source
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope requested = request.scope().getOrNull();
        return ResponseType.CODE.equals(request.responseType()) && options.clientId().equals(request.clientId())
                && options.redirectUri().equals(request.redirectUri()) && requested != null
                && effectiveScopes().equals(requested.values()) && request.state().isPresent()
                && request.codeChallenge().isEmpty() && request.codeChallengeMethod().isEmpty()
                && request.extensions().values().isEmpty();
    }

    /**
     * Extracts the unique state from a strict standard callback branch.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return required correlation state
     * @throws ValidateException if callback ownership, branch, or state is invalid
     */
    private String state(final Callback.Inbound callback) {
        return switch (decode(callback)) {
            case AuthorizationResponseDecoder.Success success -> success.response().state()
                    .orElseThrow(() -> new ValidateException("Pinterest authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Pinterest authorization error requires state"));
        };
    }

    /**
     * Completes the correlated authorization-code flow and retrieves the Pinterest profile.
     *
     * @param completion consumed callback correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @return verified Pinterest external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = decode(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Pinterest authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(authorizationError(error.response()));
        }
        if (completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(failed(ErrorCode._500, "Pinterest callback contains unexpected browser material"));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        final TokenRequest request = new TokenRequest(
                new AuthorizationCodeGrant(response.code(), options.redirectUri(), Optional.empty(), Optional.empty()),
                emptyObject());
        return token(request, context, timeout).thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<TokenResponse> success -> profile(success.value(), timeout);
            case Outcome.Rejected<TokenResponse> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<TokenResponse> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Executes one supported standard authorization-code token request.
     *
     * @param request standard token request
     * @param context immutable invocation context used for secret resolution
     * @param timeout shared end-to-end time budget
     * @return standard token response or safely classified failure
     */
    private CompletionStage<Outcome<TokenResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        if (!valid(request)) {
            return completed(rejected("Pinterest token request differs from the registered grant contract"));
        }
        return Outcome.mapStage(
                        () -> services.secretLoader().load(options.credential(), context, timeout),
                        loaded -> services.secretParser().parse(options.credential(), loaded))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SecretLease> success -> CompletableFuture.supplyAsync(() -> {
                        try (SecretLease secret = success.value()) {
                            return sendToken(request, secret, timeout);
                        } catch (RuntimeException cause) {
                            return failed(ErrorCode._502, "Pinterest token operation failed");
                        }
                    }, services.executor());
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Validates the only standard grant shape registered by Pinterest.
     *
     * @param request standard token request
     * @return whether the request is an exact authorization-code grant without PKCE or extensions
     */
    private boolean valid(final TokenRequest request) {
        if (request == null || !request.extensions().values().isEmpty()
                || !(request.grant() instanceof AuthorizationCodeGrant grant)) {
            return false;
        }
        final String clientId = grant.clientId().getOrNull();
        return options.redirectUri().equals(grant.redirectUri())
                && (clientId == null || options.clientId().equals(clientId)) && grant.codeVerifier().isEmpty();
    }

    /**
     * Sends Pinterest's historical query-authenticated empty-form token request.
     *
     * @param request validated standard token request
     * @param secret  open client-secret lease
     * @param timeout shared end-to-end time budget
     * @return standard token response or safely classified failure
     */
    private Outcome<TokenResponse> sendToken(
            final TokenRequest request,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Pinterest token request has no remaining time budget");
        }
        try {
            final AuthorizationCodeGrant grant = (AuthorizationCodeGrant) request.grant();
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST).query(OAuth2.Parameters.CODE, grant.code())
                    .query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.CLIENT_SECRET, secret(secret))
                    .query(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(Normal.EMPTY_BYTE_ARRAY, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Pinterest token endpoint request failed");
        }
    }

    /**
     * Strictly maps one historical Pinterest token envelope to a standard token response.
     *
     * @param response owned token endpoint response
     * @return standard token response or safely classified platform failure
     */
    private Outcome<TokenResponse> token(final HttpResponse response) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!tokenMembers(object)) {
                throw new ValidateException("Pinterest token response members are invalid");
            }
            final String status = requiredString(object, "status");
            if (!"success".equals(status)) {
                return platformFailure(response.code(), object, "Pinterest token endpoint rejected the request");
            }
            if (response.code() != Http.Status.OK || !object.values().containsKey("status")
                    || !object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                    || !object.values().containsKey(OAuth2.Parameters.TOKEN_TYPE)) {
                throw new ValidateException("Pinterest token success branch is invalid");
            }
            final TokenType type = new TokenType(requiredString(object, OAuth2.Parameters.TOKEN_TYPE));
            if (!TokenType.BEARER.equals(type)) {
                throw new ValidateException("Pinterest access token must use the Bearer token type");
            }
            return Outcome.succeeded(
                    new TokenResponse(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN), type, Optional.empty(),
                            Optional.empty(), Optional.empty(), emptyObject()));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Pinterest token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves the private Pinterest profile using the historical access-token query.
     *
     * @param token   standard token response carrying the bearer access token
     * @param timeout shared end-to-end time budget
     * @return verified profile identity stage
     */
    private CompletionStage<Outcome<ExternalIdentity>> profile(
            final TokenResponse token,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> sendProfile(token, timeout), services.executor());
    }

    /**
     * Sends one private Pinterest profile request and maps its status envelope.
     *
     * @param token   standard token response
     * @param timeout shared end-to-end time budget
     * @return verified external identity or safely classified failure
     */
    private Outcome<ExternalIdentity> sendProfile(final TokenResponse token, final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Pinterest profile request has no remaining time budget");
        }
        try {
            final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET).query(OAuth2.Parameters.ACCESS_TOKEN, token.accessToken())
                    .query("fields", PROFILE_FIELDS).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return profile(response, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Pinterest profile endpoint request failed");
        }
    }

    /**
     * Strictly validates and maps one Pinterest profile envelope.
     *
     * @param response owned profile response
     * @param timeout  shared clock used for evidence timestamping
     * @return verified external identity or safely classified failure
     */
    private Outcome<ExternalIdentity> profile(final HttpResponse response, final Timeout.Budget timeout) {
        try {
            final JsonValue.ObjectValue envelope = object(response);
            if (!profileEnvelopeMembers(envelope)) {
                throw new ValidateException("Pinterest profile envelope members are invalid");
            }
            final String status = requiredString(envelope, "status");
            if (!"success".equals(status)) {
                return platformFailure(response.code(), envelope, "Pinterest profile endpoint rejected the request");
            }
            if (response.code() != Http.Status.OK) {
                throw new ValidateException("Pinterest profile success has a non-success HTTP status");
            }
            final JsonValue.ObjectValue data = requiredObject(envelope, "data");
            if (!profileMembers(data)) {
                throw new ValidateException("Pinterest profile members are invalid");
            }
            final ProfileWire profile = ProfileWire.decode(data);
            final String subject = profile.id();
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            profile.copyAttributes(attributes);
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("id", new JsonValue.StringValue(subject), AUTHORITY, timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Pinterest profile endpoint returned an invalid response");
        }
    }

    /**
     * Validates callback ownership before strict standard response decoding.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     * @throws ValidateException if callback URI or method differs from the registered target
     */
    private AuthorizationResponseDecoder.Decoded decode(final Callback.Inbound callback) {
        final Callback.Inbound inbound = Assert.notNull(callback, "Pinterest callback must not be null");
        if (inbound.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(inbound.requestUri())) {
            throw new ValidateException("Pinterest callback URI or method is invalid");
        }
        return callbackDecoder.decode(inbound);
    }

    /**
     * Returns explicit scopes or the immutable manifest default.
     *
     * @return ordered effective Pinterest scopes
     */
    private List<String> effectiveScopes() {
        return options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
    }

    /**
     * Returns the effective scope in standard model form.
     *
     * @return non-empty standard Pinterest scope
     */
    private Optional<Scope> scope() {
        return Optional.of(new Scope(effectiveScopes()));
    }

    /**
     * Strictly reads one bounded Pinterest JSON response object.
     *
     * @param response response whose body remains owned by the caller
     * @return immutable provider-neutral JSON object
     * @throws ValidateException if media type, size, JSON syntax, duplicate names, or root shape is invalid
     */
    private JsonValue.ObjectValue object(final HttpResponse response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Pinterest response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Pinterest response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries the exact private Pinterest profile projection without exposing it as a public protocol model.
     *
     * @param id        stable Pinterest account identifier
     * @param username  optional public username
     * @param firstName optional given name
     * @param lastName  optional family name
     * @param bio       optional public biography
     * @param image     optional validated image projection
     */
    private record ProfileWire(String id, String username, String firstName, String lastName, String bio,
            JsonValue image) {

        /**
         * Decodes one already member-validated Pinterest profile object.
         *
         * @param data private profile response payload
         * @return immutable typed projection
         */
        private static ProfileWire decode(final JsonValue.ObjectValue data) {
            final JsonValue rawImage = data.values().get("image");
            return new ProfileWire(requiredString(data, "id"), optionalString(data, "username"),
                    optionalString(data, "first_name"), optionalString(data, "last_name"), optionalString(data, "bio"),
                    rawImage == null ? null : avatar(rawImage));
        }

        /**
         * Copies one optional string attribute into the identity projection.
         *
         * @param attributes mutable destination owned by the identity mapper
         * @param name       exact Pinterest wire member name
         * @param value      optional decoded value
         */
        private static void put(final Map<String, JsonValue> attributes, final String name, final String value) {
            if (value != null) {
                attributes.put(name, new JsonValue.StringValue(value));
            }
        }

        /**
         * Copies present non-identifier profile attributes using their exact Pinterest wire names.
         *
         * @param attributes mutable destination owned by the identity mapper
         */
        private void copyAttributes(final Map<String, JsonValue> attributes) {
            put(attributes, "username", username);
            put(attributes, "first_name", firstName);
            put(attributes, "last_name", lastName);
            put(attributes, "bio", bio);
            if (image != null) {
                attributes.put("image", image);
            }
        }

    }

}
