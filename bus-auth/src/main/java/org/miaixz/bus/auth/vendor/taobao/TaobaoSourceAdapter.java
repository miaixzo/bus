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
package org.miaixz.bus.auth.vendor.taobao;

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
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
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
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements Taobao browser authentication and registered OAuth-facing adaptations.
 * <p>
 * Authorization and token capabilities keep standard RFC 6749 Java models. The {@code view} parameter,
 * credential-bearing query POST, identity-bearing token extensions, and URL-encoded nickname remain private wire
 * adaptations. The returned {@code id_token}, when present, is preserved but never used as identity evidence.
 * </p>
 *
 * @author Kimi Liu
 */
public final class TaobaoSourceAdapter implements VendorAdapter {

    /**
     * Trusted Taobao authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://oauth.taobao.com";

    /**
     * Maximum bounded Taobao JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum Taobao JSON response nesting.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Taobao manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Taobao options.
     */
    private final TaobaoOptions options;

    /**
     * Caller-owned secret, JSON, network, clock, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Shared one-time browser-state lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Uniform adapter that owns every public OAuth capability dispatch.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Strict standard OAuth authorization callback decoder.
     */
    private final AuthorizationResponseDecoder callbackDecoder;

    /**
     * Creates one Source-bound Taobao adapter from the frozen default manifest.
     *
     * @param namespaceId registration namespace isolating browser state and credentials
     * @param sourceId    registered Source identifier
     * @param manifest    selected Taobao manifest
     * @param variant     exact selected default manifest
     * @param options     decoded externally loaded Taobao options
     * @param services    caller-owned runtime dependencies
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, manifest, options, or routing differ from the frozen variant
     */
    public TaobaoSourceAdapter(final String namespaceId, final String sourceId, final TaobaoManifest manifest,
            final VariantManifest.Variant variant, final TaobaoOptions options, final DriverServices services) {
        final TaobaoManifest selected = Assert.notNull(manifest, "Taobao manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Taobao Source id must not be blank");
        this.variant = Assert.notNull(variant, "Taobao manifest must not be null");
        this.options = Assert.notNull(options, "Taobao options must not be null");
        this.services = Assert.notNull(services, "Taobao execution services must not be null");
        if (!TaobaoManifest.ID.equals(selected.vendor()) || !selected.variant(TaobaoManifest.DEFAULT).equals(variant)
                || !TaobaoManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !TaobaoManifest.ID.equals(options.vendor()) || !TaobaoManifest.DEFAULT.equals(options.variant())) {
            throw new ValidateException("Taobao adapter requires the taobao/default OAuth 2.0 manifest");
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
     * Verifies that every decoded token member has registered Taobao semantics.
     *
     * @param object decoded token object
     * @return whether every member belongs to the token union
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            if (!switch (name) {
                case OAuth2.Parameters.ERROR, OAuth2.Parameters.ERROR_DESCRIPTION, OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.EXPIRES_IN, OAuth2.Parameters.TOKEN_TYPE, OpenIdConnect.Parameters.ID_TOKEN, OAuth2.Parameters.REFRESH_TOKEN, "taobao_user_id", "taobao_open_uid", "taobao_user_nick" -> true;
                default -> false;
            }) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads one required non-blank JSON string member.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return required non-blank string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = optionalString(object, name);
        if (value == null) {
            throw new ValidateException("Taobao response requires a non-blank string member: " + name);
        }
        return value;
    }

    /**
     * Reads one optional non-blank JSON string member.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return string value or {@code null} when absent or explicit null
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Taobao response member must be a non-blank string: " + name);
        }
        return string.value();
    }

    /**
     * Reads one exact positive integral JSON number.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return positive long value
     */
    private static long requiredPositiveLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Taobao response requires a numeric member: " + name);
        }
        try {
            final long exact = number.value().longValueExact();
            if (exact <= 0L) {
                throw new ValidateException("Taobao numeric member must be positive: " + name);
            }
            return exact;
        } catch (ArithmeticException cause) {
            throw new ValidateException("Taobao numeric member must be an exact long: " + name, cause);
        }
    }

    /**
     * Classifies one HTTP status without exposing upstream response content.
     *
     * @param status      HTTP response status
     * @param description safe operation description
     * @param <T>         expected success type
     * @return rejected request or operational failure
     */
    private static <T> Outcome<T> status(final int status, final String description) {
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, description);
        }
        if (status >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(ErrorCode._502, description);
        }
        return rejected(description);
    }

    /**
     * Maps one standard authorization error to a safe rejected outcome.
     *
     * @param error decoded standard authorization error branch
     * @param <T>   expected success type
     * @return rejected outcome retaining only the standard error identifier
     */
    private static <T> Outcome<T> oauthError(final AuthorizationResponseDecoder.Error error) {
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400, "Taobao authorization endpoint returned a standard error",
                        new JsonValue.ObjectValue(
                                Map.of("oauth_error", new JsonValue.StringValue(error.response().error().value())))));
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
     * Creates an immutable empty JSON object.
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
     * Creates a safe expected request or platform rejection.
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
     * Returns the exact frozen Taobao capability manifest.
     *
     * @return immutable Source authentication and OAuth capability set
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and registered standard OAuth operations.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing private Taobao response records
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Taobao capability must not be null");
        Assert.notNull(context, "Taobao invocation context must not be null");
        Assert.notNull(timeout, "Taobao invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Taobao capability is not declared"));
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
        return completed(rejected("Taobao capability request is invalid"));
    }

    /**
     * Builds the Taobao redirect around generated one-time state.
     *
     * @param initiation generated browser correlation material
     * @param context    immutable invocation context retained by the uniform signature
     * @param timeout    shared end-to-end budget
     * @return prepared authorization redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "Taobao authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "Taobao browser material violates the frozen manifest"));
        }
        try {
            final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                    options.redirectUri(), Optional.empty(), Optional.of(initiation.state()), Optional.empty(),
                    Optional.empty(), emptyObject());
            return standardAdapter.invoke(OAuth2ClientScheme.AUTHORIZATION, request, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<UnoUrl> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<UnoUrl> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<UnoUrl> failed -> Outcome.failed(failed.failure());
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Taobao authorization request is invalid"));
        }
    }

    /**
     * Encodes one validated request with Taobao's required web-view extension.
     *
     * @param request standard authorization request
     * @return asynchronous exact Taobao authorization URL outcome
     */
    private CompletionStage<Outcome<UnoUrl>> authorization(final AuthorizationRequest request) {
        if (!valid(request)) {
            return completed(rejected("Taobao authorization request differs from the registered Source"));
        }
        try {
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final UnoUrl location = endpoint.url().newBuilder()
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query(OAuth2.Parameters.CLIENT_ID, request.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, request.redirectUri().getOrNull()).query("view", "web")
                    .query(OAuth2.Parameters.STATE, request.state().getOrNull()).build();
            return completed(Outcome.succeeded(location));
        } catch (RuntimeException cause) {
            return completed(rejected("Taobao authorization request is invalid"));
        }
    }

    /**
     * Validates one public standard authorization request against the registered Source.
     *
     * @param request standard OAuth authorization request
     * @return whether every public field matches the selected Source
     */
    private boolean valid(final AuthorizationRequest request) {
        return ResponseType.CODE.equals(request.responseType()) && options.clientId().equals(request.clientId())
                && options.redirectUri().equals(request.redirectUri()) && request.scope().isEmpty()
                && request.state().isPresent() && request.codeChallenge().isEmpty()
                && request.codeChallengeMethod().isEmpty() && request.extensions().values().isEmpty();
    }

    /**
     * Extracts required state from one strict standard callback branch.
     *
     * @param callback raw inbound callback
     * @return unique correlation state
     */
    private String state(final Callback.Inbound callback) {
        return switch (decode(callback)) {
            case AuthorizationResponseDecoder.Success success -> success.response().state()
                    .orElseThrow(() -> new ValidateException("Taobao authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Taobao authorization error requires state"));
        };
    }

    /**
     * Completes the correlated token operation and maps its verified Taobao subject extension.
     *
     * @param completion consumed browser correlation
     * @param context    immutable invocation context used for secret resolution
     * @param timeout    shared end-to-end budget
     * @return verified Taobao identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = decode(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Taobao authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(oauthError(error));
        }
        if (completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(failed(ErrorCode._500, "Taobao callback contains unexpected browser material"));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        final TokenRequest request = new TokenRequest(
                new AuthorizationCodeGrant(response.code(), options.redirectUri(), Optional.empty(), Optional.empty()),
                emptyObject());
        return standardAdapter.invoke(OAuth2ClientScheme.TOKEN, request, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<TokenEndpointResponse> success -> success
                            .value() instanceof TokenResponse token ? identity(token, timeout)
                                    : rejected("Taobao token endpoint returned a non-OAuth token response");
                    case Outcome.Rejected<TokenEndpointResponse> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<TokenEndpointResponse> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Executes one supported standard authorization-code token request.
     *
     * @param request standard token request
     * @param context immutable invocation context used for secret resolution
     * @param timeout shared end-to-end budget
     * @return standard token response with exact Taobao extensions
     */
    private CompletionStage<Outcome<TokenResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        if (!valid(request)) {
            return completed(rejected("Taobao token request differs from the registered grant contract"));
        }
        try {
            final CompletionStage<Outcome<SecretLease>> resolution = Outcome.mapStage(
                    () -> services.secretLoader().load(options.credential(), context, timeout),
                    loaded -> services.secretParser().parse(options.credential(), loaded));
            if (resolution == null) {
                return completed(failed(ErrorCode._502, "Taobao secret loader returned no stage"));
            }
            return resolution
                    .handle(
                            (outcome, cause) -> cause == null && outcome != null ? outcome
                                    : TaobaoSourceAdapter
                                            .<SecretLease>failed(ErrorCode._502, "Taobao secret resolution failed"))
                    .thenCompose(outcome -> switch (outcome) {
                        case Outcome.Succeeded<SecretLease> success -> CompletableFuture.supplyAsync(() -> {
                            try (SecretLease secret = success.value()) {
                                return sendToken(request, secret, timeout);
                            } catch (RuntimeException cause) {
                                return failed(ErrorCode._502, "Taobao token operation failed");
                            }
                        }, services.executor());
                        case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    });
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "Taobao secret resolution failed"));
        }
    }

    /**
     * Validates Taobao's only public standard token request shape.
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
     * Sends Taobao's historical credential-bearing query POST with an empty form body.
     *
     * @param request validated standard token request
     * @param secret  open client-secret lease
     * @param timeout shared end-to-end budget
     * @return standard token response or safely classified failure
     */
    private Outcome<TokenResponse> sendToken(
            final TokenRequest request,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Taobao token request has no remaining time budget");
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
            return failed(ErrorCode._502, "Taobao token request failed");
        }
    }

    /**
     * Strictly maps one Taobao token document to the standard token response and registered extensions.
     *
     * @param response owned token endpoint response
     * @return standard token response or safely classified failure
     */
    private Outcome<TokenResponse> token(final HttpResponse response) {
        if (response.code() < Http.Status.OK || response.code() >= Http.Status.MULTIPLE_CHOICES) {
            return status(response.code(), "Taobao token endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!tokenMembers(object)) {
                return failed(ErrorCode._502, "Taobao token response contains an unknown member");
            }
            final JsonValue error = object.values().get(OAuth2.Parameters.ERROR);
            if (error != null && !(error instanceof JsonValue.NullValue)) {
                return rejected("Taobao token endpoint rejected the request");
            }
            final TokenType tokenType = new TokenType(requiredString(object, OAuth2.Parameters.TOKEN_TYPE));
            final TokenIdentity identity = TokenIdentity.decode(object);
            final Map<String, JsonValue> extensions = new LinkedHashMap<>(identity.extensions().values());
            final String idToken = optionalString(object, OpenIdConnect.Parameters.ID_TOKEN);
            if (idToken != null) {
                extensions.put(OpenIdConnect.Parameters.ID_TOKEN, new JsonValue.StringValue(idToken));
            }
            return Outcome.succeeded(
                    new TokenResponse(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN), tokenType,
                            Optional.of(requiredPositiveLong(object, OAuth2.Parameters.EXPIRES_IN)),
                            Optional.ofNullable(optionalString(object, OAuth2.Parameters.REFRESH_TOKEN)),
                            Optional.empty(), new JsonValue.ObjectValue(extensions)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Taobao token endpoint returned an invalid response");
        }
    }

    /**
     * Maps stable Taobao token extensions to an external identity without trusting {@code id_token}.
     *
     * @param token   strictly decoded standard token response
     * @param timeout shared clock used for evidence timestamping
     * @return verified Taobao identity or safely classified failure
     */
    private Outcome<ExternalIdentity> identity(final TokenResponse token, final Timeout.Budget timeout) {
        try {
            final JsonValue.ObjectValue extensions = token.extensions();
            final String userId = optionalString(extensions, "taobao_user_id");
            final String openUid = optionalString(extensions, "taobao_open_uid");
            final String subject;
            final String claim;
            if (userId != null) {
                subject = userId;
                claim = "taobao_user_id";
            } else if (openUid != null) {
                subject = openUid;
                claim = "taobao_open_uid";
            } else {
                return rejected("Taobao token response does not contain a stable user identifier");
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            if (userId != null) {
                attributes.put("taobao_user_id", new JsonValue.StringValue(userId));
            }
            if (openUid != null) {
                attributes.put("taobao_open_uid", new JsonValue.StringValue(openUid));
            }
            final String encodedNickname = optionalString(extensions, "taobao_user_nick");
            if (encodedNickname != null) {
                final String nickname = Assert.notBlank(
                        UrlDecoder.decodeStrict(encodedNickname),
                        "Decoded Taobao nickname must not be blank");
                attributes.put("taobao_user_nick", new JsonValue.StringValue(nickname));
            }
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim(claim, new JsonValue.StringValue(subject), AUTHORITY, timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Taobao token identity extensions are invalid");
        }
    }

    /**
     * Validates callback ownership before strict standard response decoding.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     */
    private AuthorizationResponseDecoder.Decoded decode(final Callback.Inbound callback) {
        final Callback.Inbound inbound = Assert.notNull(callback, "Taobao callback must not be null");
        if (inbound.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(inbound.requestUri())) {
            throw new ValidateException("Taobao callback URI or method is invalid");
        }
        return callbackDecoder.decode(inbound);
    }

    /**
     * Strictly reads one bounded Taobao JSON object.
     *
     * @param response response whose body remains owned by the caller
     * @return immutable provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final HttpResponse response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Taobao token response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Taobao token response root must be an object");
        }
        return object;
    }

    /**
     * Carries Taobao's private identity-bearing token extensions as one typed association.
     *
     * @param userId          optional Taobao user identifier value
     * @param openUid         optional Taobao OpenUID value
     * @param encodedNickname optional URL-encoded nickname value
     */
    private record TokenIdentity(JsonValue userId, JsonValue openUid, JsonValue encodedNickname) {

        /**
         * Decodes documented non-null identity extension values from one validated token object.
         *
         * @param object validated token response object
         * @return immutable typed identity extension association
         */
        private static TokenIdentity decode(final JsonValue.ObjectValue object) {
            return new TokenIdentity(value(object, "taobao_user_id"), value(object, "taobao_open_uid"),
                    value(object, "taobao_user_nick"));
        }

        /**
         * Returns one present non-null JSON value.
         *
         * @param object decoded token object
         * @param name   exact Taobao member name
         * @return retained value or {@code null}
         */
        private static JsonValue value(final JsonValue.ObjectValue object, final String name) {
            final JsonValue value = object.values().get(name);
            return value instanceof JsonValue.NullValue ? null : value;
        }

        /**
         * Copies one present JSON value into the extension object.
         *
         * @param values mutable extension destination
         * @param name   exact Taobao member name
         * @param value  optional retained value
         */
        private static void put(final Map<String, JsonValue> values, final String name, final JsonValue value) {
            if (value != null) {
                values.put(name, value);
            }
        }

        /**
         * Returns a standard extension object retaining exact Taobao wire values.
         *
         * @return immutable token extension object
         */
        private JsonValue.ObjectValue extensions() {
            final Map<String, JsonValue> values = new LinkedHashMap<>();
            put(values, "taobao_user_id", userId);
            put(values, "taobao_open_uid", openUid);
            put(values, "taobao_user_nick", encodedNickname);
            return new JsonValue.ObjectValue(values);
        }

    }

}
