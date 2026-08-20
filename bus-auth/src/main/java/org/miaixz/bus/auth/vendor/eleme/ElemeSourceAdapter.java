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
package org.miaixz.bus.auth.vendor.eleme;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.Parameter;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.*;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationResponseDecoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenResponseDecoder;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.data.id.UUID;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.auth.HttpAuth;

/**
 * Implements Eleme service-provider authentication while preserving its standard OAuth 2.0 operations.
 * <p>
 * Public authorization and token capabilities delegate to the shared OAuth client after registration-bound request
 * validation. Browser authentication consumes the authorization code, token, client secret, and signed merchant RPC
 * only inside one bounded completion chain and publishes solely a verified merchant identity.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ElemeSourceAdapter implements VendorAdapter {

    /**
     * Trusted Eleme authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://open-api.shop.ele.me";

    /**
     * Fixed Eleme NOP protocol version.
     */
    private static final String NOP_VERSION = "1.0.0";

    /**
     * Fixed merchant-profile RPC action.
     */
    private static final String USER_ACTION = "eleme.user.getUser";

    /**
     * Maximum accepted Eleme merchant JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum accepted Eleme merchant JSON nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Registered Source identifier copied into verified external identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Eleme manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Eleme options.
     */
    private final ElemeOptions options;

    /**
     * Caller-owned secret, JSON, network, clock, and execution dependencies.
     */
    private final ExecutionServices services;

    /**
     * Shared standard OAuth 2.0 authorization and token implementation.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time browser-state lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Strict standard OAuth authorization response decoder.
     */
    private final AuthorizationResponseDecoder authorizationResponseDecoder;

    /**
     * Standard token grant encoder reused by the single-lease authentication chain.
     */
    private final TokenRequestEncoder tokenRequestEncoder;

    /**
     * Standard token response decoder reused by the single-lease authentication chain.
     */
    private final TokenResponseDecoder tokenResponseDecoder;

    /**
     * Shared strict application/x-www-form-urlencoded codec.
     */
    private final FormCodec formCodec;

    /**
     * Creates one Source-bound Eleme adapter for the frozen default variant.
     *
     * @param namespaceId registration namespace used to isolate state and credential resolution
     * @param sourceId    registered Source identifier
     * @param manifest    selected Eleme manifest
     * @param variant     exact selected default manifest
     * @param options     decoded externally loaded Eleme options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, variant, protocol, manifest, or options routing is inconsistent
     */
    public ElemeSourceAdapter(final String namespaceId, final String sourceId, final ElemeManifest manifest,
            final VariantManifest.Variant variant, final ElemeOptions options, final ExecutionServices services) {
        final ElemeManifest selectedProfile = Assert.notNull(manifest, "Eleme manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Eleme Source id must not be blank");
        this.variant = Assert.notNull(variant, "Eleme manifest must not be null");
        this.options = Assert.notNull(options, "Eleme options must not be null");
        this.services = Assert.notNull(services, "Eleme execution services must not be null");
        if (!ElemeManifest.ID.equals(selectedProfile.vendor())
                || !selectedProfile.variant(ElemeManifest.DEFAULT).equals(variant)
                || !ElemeManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !ElemeManifest.ID.equals(options.vendor()) || !ElemeManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("Eleme adapter requires the eleme/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.standardAdapter = standardAdapter(variant, options, services, redirectManager);
        this.authorizationResponseDecoder = new AuthorizationResponseDecoder();
        this.tokenRequestEncoder = new TokenRequestEncoder();
        this.tokenResponseDecoder = new TokenResponseDecoder(services.jsonProvider());
        this.formCodec = new FormCodec();
    }

    /**
     * Composes Eleme's public OAuth operations from protocol-owned clients and codecs.
     *
     * @param variant         selected Eleme variant
     * @param options         validated Source options
     * @param services        caller-owned execution services
     * @param redirectManager shared browser correlation lifecycle
     * @return standard authorization and token adapter
     */
    private static StandardAdapter standardAdapter(
            final VariantManifest.Variant variant,
            final ElemeOptions options,
            final ExecutionServices services,
            final RedirectManager redirectManager) {
        final var targets = variant.targets().resolve(options);
        final OAuth2ClientOptions oauthSettings = new OAuth2ClientOptions(targets.authorization(), targets.token(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                options.clientId(), Set.of(options.redirectUri().getOrNull()),
                Endpoint.Authentication.CLIENT_SECRET_BASIC, Optional.of(options.credential()), false, false);
        final OAuth2Client oauthClient = new OAuth2Client(
                new AuthorizationClient(oauthSettings,
                        new AuthorizationRequestEncoder(targets.authorization().getOrNull())),
                new TokenClient(oauthSettings, services, new TokenRequestEncoder(),
                        new TokenResponseDecoder(services.jsonProvider())),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        return new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION, oauthClient::authorize),
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.TOKEN, oauthClient::token)));
    }

    /**
     * Validates the Eleme-specific constraints on a standard OAuth token success.
     *
     * @param response decoded standard token response
     * @return unchanged valid response or safe failed outcome
     */
    private static Outcome<TokenResponse> validateToken(final TokenResponse response) {
        try {
            if (!TokenType.BEARER.equals(response.tokenType()) || response.expiresIn().isEmpty()
                    || response.expiresIn().getOrNull() <= 0L || response.refreshToken().isEmpty()
                    || response.scope().isEmpty() || !response.scope().getOrNull().values().contains("all")) {
                throw new ValidateException("Eleme token response violates the registered OAuth manifest");
            }
            final Map<String, JsonValue> extensions = response.extensions().values();
            if (extensions.size() > 1 || extensions.size() == 1 && !extensions.containsKey("trace_id")) {
                throw new ValidateException("Eleme token response contains an unregistered extension");
            }
            final JsonValue trace = extensions.get("trace_id");
            if (trace != null && (!(trace instanceof JsonValue.StringValue value) || value.value().isBlank())) {
                throw new ValidateException("Eleme token trace identifier is invalid");
            }
            return Outcome.succeeded(response);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Eleme token endpoint returned an invalid success response");
        }
    }

    /**
     * Classifies one strict Eleme RPC error without exposing its message.
     *
     * @param error strict platform error object
     * @return rejected known client error or failed unknown platform error
     */
    private static Outcome<ExternalIdentity> merchantError(final JsonValue.ObjectValue error) {
        if (error.values().size() != 2 || !error.values().containsKey("code")
                || !error.values().containsKey("message")) {
            throw new ValidateException("Eleme merchant error has unexpected or missing members");
        }
        final String code = requiredString(error, "code");
        requiredString(error, "message");
        final Outcome.Failure failure = new Outcome.Failure(ErrorCode._400,
                "Eleme merchant endpoint returned a platform error",
                new JsonValue.ObjectValue(Map.of("vendor_error", new JsonValue.StringValue(code))));
        return rejectedMerchantError(code) || code.startsWith("BIZ_") ? Outcome.rejected(failure)
                : Outcome.failed(
                        new Outcome.Failure(ErrorCode._502,
                                "Eleme merchant endpoint returned an unknown platform error",
                                new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Classifies one documented Eleme merchant error as an expected caller or authorization rejection.
     *
     * @param code exact platform error code
     * @return whether the error belongs to the documented rejection category
     */
    private static boolean rejectedMerchantError(final String code) {
        return switch (code) {
            case "ACCESS_DENIED", "EXCEED_LIMIT", "INVALID_SIGNATURE", "INVALID_TIMESTAMP", "METHOD_NOT_ALLOWED", "PERMISSION_DENIED", "UNAUTHORIZED", "VALIDATION_FAILED", "BUSINESS_ERROR" -> true;
            default -> false;
        };
    }

    /**
     * Converts one standard authorization error to a safe rejected outcome.
     *
     * @param error decoded standard authorization error branch
     * @param <T>   expected success type
     * @return rejected outcome retaining only the registered OAuth error identifier
     */
    private static <T> Outcome<T> oauthError(final AuthorizationResponseDecoder.Error error) {
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400, "Eleme authorization endpoint returned a standard error",
                        new JsonValue.ObjectValue(
                                Map.of("oauth_error", new JsonValue.StringValue(error.response().error().value())))));
    }

    /**
     * Maps a decoded standard token error without exposing diagnostic or credential material.
     *
     * @param error decoded OAuth token error and original HTTP status
     * @return rejected 4xx or failed rate-limit/upstream outcome
     */
    private static Outcome<TokenResponse> tokenError(final TokenResponseDecoder.Error error) {
        final Map<String, JsonValue> details = Map
                .of("oauth_error", new JsonValue.StringValue(error.response().error().value()));
        if (error.status() == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "Eleme token endpoint rate limited the request", details);
        }
        if (error.status() >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(ErrorCode._502, "Eleme token endpoint returned an upstream error", details);
        }
        final Errors code = error.status() == Http.Status.UNAUTHORIZED ? ErrorCode._401 : ErrorCode._400;
        return Outcome.rejected(
                new Outcome.Failure(code, "Eleme token endpoint returned a standard OAuth error",
                        new JsonValue.ObjectValue(details)));
    }

    /**
     * Produces the exact non-secret prefix of the Eleme signature input.
     *
     * @param accessToken   sensitive access token
     * @param appKeyJson    compact JSON application-key value
     * @param timestampJson compact JSON epoch-millisecond value
     * @return newly allocated UTF-8 signature prefix
     */
    private static byte[] signaturePrefix(
            final String accessToken,
            final byte[] appKeyJson,
            final byte[] timestampJson) {
        final byte[] action = USER_ACTION.getBytes(Charset.UTF_8);
        final byte[] token = accessToken.getBytes(Charset.UTF_8);
        final byte[] appKeyName = "app_key=".getBytes(Charset.UTF_8);
        final byte[] timestampName = "timestamp=".getBytes(Charset.UTF_8);
        final byte[] result = join(action, token, appKeyName, appKeyJson, timestampName, timestampJson);
        clear(action);
        clear(token);
        clear(appKeyName);
        clear(timestampName);
        return result;
    }

    /**
     * Appends UTF-8 encoded secret characters to a non-secret signature prefix.
     *
     * @param prefix non-secret signature prefix
     * @param secret exclusively leased secret characters
     * @return newly allocated complete signature input
     */
    private static byte[] append(final byte[] prefix, final char[] secret) {
        final byte[] encoded = new String(secret).getBytes(Charset.UTF_8);
        try {
            final byte[] result = Arrays.copyOf(prefix, prefix.length + encoded.length);
            System.arraycopy(encoded, 0, result, prefix.length, encoded.length);
            return result;
        } finally {
            clear(encoded);
        }
    }

    /**
     * Concatenates byte arrays in their exact supplied order.
     *
     * @param values ordered byte arrays
     * @return newly allocated concatenation
     */
    private static byte[] join(final byte[]... values) {
        int length = 0;
        for (byte[] value : values) {
            length = Math.addExact(length, value.length);
        }
        final byte[] result = new byte[length];
        int offset = 0;
        for (byte[] value : values) {
            System.arraycopy(value, 0, result, offset, value.length);
            offset += value.length;
        }
        return result;
    }

    /**
     * Reads one required non-blank JSON string member.
     *
     * @param object decoded JSON object
     * @param name   exact member name
     * @return non-blank string value
     * @throws ValidateException if the member is absent, blank, null, or another JSON type
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue text) || text.value().isBlank()) {
            throw new ValidateException("Eleme response requires a non-blank string member");
        }
        return text.value();
    }

    /**
     * Requires the exact Eleme merchant RPC envelope shape.
     *
     * @param object decoded merchant response
     * @throws ValidateException if an envelope member is missing or unexpected
     */
    private static void merchantEnvelope(final JsonValue.ObjectValue object) {
        if (object.values().size() != 3 || !object.values().containsKey("id") || !object.values().containsKey("result")
                || !object.values().containsKey(OAuth2.Parameters.ERROR)) {
            throw new ValidateException("Eleme merchant response has unexpected or missing members");
        }
    }

    /**
     * Requires the typed Eleme merchant result shape with optional authorized shops.
     *
     * @param object decoded merchant result
     * @throws ValidateException if a result member is missing or unexpected
     */
    private static void merchantResult(final JsonValue.ObjectValue object) {
        if (object.values().size() < 2 || object.values().size() > 3 || !object.values().containsKey("userId")
                || !object.values().containsKey("userName")) {
            throw new ValidateException("Eleme merchant result has unexpected or missing members");
        }
        for (String member : object.values().keySet()) {
            if (!"userId".equals(member) && !"userName".equals(member) && !"authorizedShops".equals(member)) {
                throw new ValidateException("Eleme merchant result contains an unexpected member");
            }
        }
    }

    /**
     * Creates a provider-neutral empty JSON object.
     *
     * @return immutable empty object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Narrows one delegated outcome through the declared capability response type.
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
     * Creates an already completed outcome stage.
     *
     * @param outcome completed outcome
     * @param <T>     success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe missing-capability rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejected stage
     */
    private static <T> CompletionStage<Outcome<T>> missing() {
        return completed(rejected("Eleme capability is not declared by the selected manifest"));
    }

    /**
     * Creates a safe request-contract rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejected stage
     */
    private static <T> CompletionStage<Outcome<T>> mismatch() {
        return completed(rejected("Eleme request does not match the selected capability contract"));
    }

    /**
     * Creates a safe expected rejection.
     *
     * @param description non-sensitive rejection description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates a safe operational failure without details.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final Errors code, final String description) {
        return failed(code, description, Map.of());
    }

    /**
     * Creates a safe operational failure with an immutable non-sensitive detail map.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param details     non-sensitive structured details
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(
            final Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.failed(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Erases one optional mutable byte array.
     *
     * @param value mutable sensitive or temporary bytes, or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) 0);
        }
    }

    /**
     * Returns the exact capability manifest frozen by the selected Eleme manifest.
     *
     * @return immutable Source authentication and standard OAuth capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and the two registered standard OAuth operations.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Eleme private response objects
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Eleme capability must not be null");
        Assert.notNull(context, "Eleme invocation context must not be null");
        Assert.notNull(timeout, "Eleme invocation budget must not be null");
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
        if (capability.equals(OAuth2ClientScheme.AUTHORIZATION)
                && request instanceof AuthorizationRequest authorization) {
            return valid(authorization) ? standardAdapter.invoke(capability, request, context, timeout) : mismatch();
        }
        if (capability.equals(OAuth2ClientScheme.TOKEN) && request instanceof TokenRequest tokenRequest) {
            if (!valid(tokenRequest)) {
                return mismatch();
            }
            return narrow(
                    standardAdapter.invoke(capability, request, context, timeout)
                            .thenApply(outcome -> switch (outcome) {
                                case Outcome.Succeeded<?> success -> validateToken((TokenResponse) success.value());
                                case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
                                case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
                            }),
                    capability.responseType());
        }
        return mismatch();
    }

    /**
     * Builds the exact Eleme authorization redirect from generated browser state.
     *
     * @param initiation generated state without nonce or PKCE material
     * @param context    immutable invocation context retained for operation consistency
     * @param timeout    shared end-to-end budget
     * @return exact redirect and state correlation
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "Eleme authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Eleme authorization request has no remaining time budget"));
        }
        if (initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(
                    failed(
                            ErrorCode._500,
                            "Eleme browser flow generated security material outside its registered OAuth policy"));
        }
        try {
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final String location = endpoint.url().newBuilder()
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.STATE, initiation.state()).query(OAuth2.Parameters.SCOPE, "all").build()
                    .toString();
            return completed(Outcome.succeeded(new RedirectManager.Prepared(location, initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("Eleme authorization request is invalid"));
        }
    }

    /**
     * Extracts the unique callback state from an exact Eleme success or error branch.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return unique non-blank state
     * @throws ValidateException if callback transport, members, branch, or state is invalid
     */
    private String state(final Callback.Inbound callback) {
        return switch (callback(callback)) {
            case AuthorizationResponseDecoder.Success success -> success.response().state()
                    .orElseThrow(() -> new ValidateException("Eleme authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Eleme authorization error requires state"));
        };
    }

    /**
     * Redeems one correlated authorization code and resolves the signed merchant identity.
     *
     * @param completion consumed callback correlation without PKCE verifier
     * @param context    immutable invocation context used for one secret resolution
     * @param timeout    shared end-to-end budget
     * @return verified merchant identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Eleme authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(oauthError(error));
        }
        if (completion.codeVerifier().isPresent()) {
            return completed(failed(ErrorCode._500, "Eleme callback unexpectedly carried a PKCE verifier"));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        if (response.issuer().isPresent() || response.scope().isPresent()
                || !response.extensions().values().isEmpty()) {
            return completed(rejected("Eleme authorization callback contains unregistered success parameters"));
        }
        final TokenRequest request = new TokenRequest(new AuthorizationCodeGrant(response.code(), options.redirectUri(),
                Optional.of(options.clientId()), Optional.empty()), emptyObject());
        return org.miaixz.bus.auth.runtime.LoadResult
                .parse(
                        services.secretLoader().load(options.credential(), context, timeout),
                        loaded -> services.secretParser().parse(options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(request, success.value(), timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Executes the token and merchant RPC steps under one exclusively owned client-secret lease.
     *
     * @param request exact standard authorization-code token request
     * @param secret  owned client-secret lease closed by this method's asynchronous boundary
     * @param timeout shared end-to-end budget
     * @return verified merchant identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> authenticate(
            final TokenRequest request,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try (secret) {
                return switch (token(request, secret, timeout)) {
                    case Outcome.Succeeded<TokenResponse> success -> merchant(
                            success.value().accessToken(),
                            secret,
                            timeout);
                    case Outcome.Rejected<TokenResponse> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<TokenResponse> failed -> Outcome.failed(failed.failure());
                };
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Eleme authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Sends one standard Eleme token request using an existing client-secret lease.
     *
     * @param request exact standard token request
     * @param secret  still-open client-secret lease shared with the merchant RPC
     * @param timeout shared end-to-end budget
     * @return validated standard token response or closed protocol failure
     */
    private Outcome<TokenResponse> token(
            final TokenRequest request,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Eleme token request has no remaining time budget");
            }
            body = formCodec.encode(tokenRequestEncoder.encode(request));
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            final String authorization = HttpAuth
                    .basic(formComponent(options.clientId()), formComponent(new String(secret.material()))).value();
            final var response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST).header(Http.Header.AUTHORIZATION, authorization)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute();
            return switch (tokenResponseDecoder.decode(response)) {
                case TokenResponseDecoder.Success success -> success.response() instanceof TokenResponse token
                        ? validateToken(token)
                        : failed(ErrorCode._502, "Eleme token endpoint returned an unsupported success type");
                case TokenResponseDecoder.Error error -> tokenError(error);
            };
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Eleme token endpoint request failed");
        } finally {
            if (body != null) {
                Arrays.fill(body, (byte) 0);
            }
        }
    }

    /**
     * Sends the exact signed Eleme merchant RPC and maps its verified identity.
     *
     * @param accessToken sensitive Bearer access token returned by the token endpoint
     * @param secret      still-open client-secret lease used only for the RPC signature
     * @param timeout     shared end-to-end budget
     * @return verified external merchant identity or a closed platform failure
     */
    private Outcome<ExternalIdentity> merchant(
            final String accessToken,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        byte[] body = null;
        byte[] signatureInput = null;
        byte[] appKeyJson = null;
        byte[] timestampJson = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Eleme merchant request has no remaining time budget");
            }
            final long timestamp = timeout.clock().now().toEpochMilli();
            final String requestId = UUID.randomUUID().toString();
            appKeyJson = services.jsonProvider().writeValue(new JsonValue.StringValue(options.clientId()));
            timestampJson = services.jsonProvider()
                    .writeValue(new JsonValue.NumberValue(BigDecimal.valueOf(timestamp)));
            final byte[] prefix = signaturePrefix(accessToken, appKeyJson, timestampJson);
            signatureInput = append(prefix, secret.material());
            Arrays.fill(prefix, (byte) 0);
            final String signature = Builder.md5Hex(signatureInput).toUpperCase(Locale.ROOT);

            final Map<String, JsonValue> metas = new LinkedHashMap<>();
            metas.put("app_key", new JsonValue.StringValue(options.clientId()));
            metas.put("timestamp", new JsonValue.NumberValue(BigDecimal.valueOf(timestamp)));
            final Map<String, JsonValue> members = new LinkedHashMap<>();
            members.put("nop", new JsonValue.StringValue(NOP_VERSION));
            members.put("id", new JsonValue.StringValue(requestId));
            members.put("metas", new JsonValue.ObjectValue(metas));
            members.put("action", new JsonValue.StringValue(USER_ACTION));
            members.put("token", new JsonValue.StringValue(accessToken));
            members.put("params", emptyObject());
            members.put("signature", new JsonValue.StringValue(signature));
            body = services.jsonProvider().writeValue(new JsonValue.ObjectValue(members));

            final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
            final MediaType json = MediaType.APPLICATION_JSON_TYPE.withCharset(Charset.UTF_8);
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(body, json).execute()) {
                return merchant(response, requestId, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Eleme merchant profile request failed");
        } finally {
            clear(body);
            clear(signatureInput);
            clear(appKeyJson);
            clear(timestampJson);
        }
    }

    /**
     * Decodes and classifies one bounded Eleme merchant RPC response.
     *
     * @param response  owned Fabric response
     * @param requestId exact outbound RPC identifier
     * @param timeout   shared clock used for evidence verification time
     * @return verified identity, expected platform rejection, or upstream failure
     */
    private Outcome<ExternalIdentity> merchant(
            final HttpResponse response,
            final String requestId,
            final Timeout.Budget timeout) {
        if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "Eleme merchant endpoint rate limited the request");
        }
        if (response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(ErrorCode._502, "Eleme merchant endpoint is unavailable");
        }
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            return failed(ErrorCode._502, "Eleme merchant endpoint returned a non-JSON response");
        }
        final JsonValue parsed = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(parsed instanceof JsonValue.ObjectValue object)) {
            return failed(ErrorCode._502, "Eleme merchant response root must be a JSON object");
        }
        return merchant(object, requestId, timeout);
    }

    /**
     * Enforces the mutually exclusive Eleme RPC success and error branches.
     *
     * @param object    strict decoded RPC envelope
     * @param requestId exact outbound RPC identifier
     * @param timeout   shared clock used for evidence verification time
     * @return verified merchant identity or a safely classified platform outcome
     */
    private Outcome<ExternalIdentity> merchant(
            final JsonValue.ObjectValue object,
            final String requestId,
            final Timeout.Budget timeout) {
        try {
            merchantEnvelope(object);
            if (!requestId.equals(requiredString(object, "id"))) {
                return failed(ErrorCode._502, "Eleme merchant response id does not match its request");
            }
            final JsonValue result = object.values().get("result");
            final JsonValue error = object.values().get(OAuth2.Parameters.ERROR);
            final boolean success = result instanceof JsonValue.ObjectValue && error instanceof JsonValue.NullValue;
            final boolean failure = result instanceof JsonValue.NullValue && error instanceof JsonValue.ObjectValue;
            if (success == failure) {
                return failed(ErrorCode._502, "Eleme merchant response mixes or omits result and error branches");
            }
            if (failure) {
                return merchantError((JsonValue.ObjectValue) error);
            }
            return identity((JsonValue.ObjectValue) result, timeout);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Eleme merchant response is invalid");
        }
    }

    /**
     * Maps one strict successful merchant result to a provider-neutral external identity.
     *
     * @param result  strict Eleme merchant result
     * @param timeout shared clock used for evidence verification time
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> identity(final JsonValue.ObjectValue result, final Timeout.Budget timeout) {
        final Merchant merchant = Merchant.decode(result);
        final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                new Evidence.Claim("eleme_user_id", new JsonValue.StringValue(merchant.subject()), AUTHORITY,
                        timeout.clock().now()));
        return Outcome.succeeded(
                new ExternalIdentity(sourceId, merchant.subject(), merchant.attributes(), List.of(evidence)));
    }

    /**
     * Validates the exact standard Eleme authorization request.
     *
     * @param request standard OAuth authorization request
     * @return {@code true} only when every registration-bound field is exact
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request.scope().getOrNull();
        return ResponseType.CODE.equals(request.responseType()) && options.clientId().equals(request.clientId())
                && options.redirectUri().equals(request.redirectUri()) && scope != null
                && List.of("all").equals(scope.values()) && request.state().isPresent()
                && request.codeChallenge().isEmpty() && request.codeChallengeMethod().isEmpty()
                && request.extensions().values().isEmpty();
    }

    /**
     * Validates the two standard Eleme token grants without inspecting sensitive token values.
     *
     * @param request standard token request
     * @return {@code true} for the exact authorization-code or refresh-token request shape
     */
    private boolean valid(final TokenRequest request) {
        if (!request.extensions().values().isEmpty()) {
            return false;
        }
        if (request.grant() instanceof AuthorizationCodeGrant grant) {
            return options.redirectUri().equals(grant.redirectUri())
                    && Optional.of(options.clientId()).equals(grant.clientId()) && grant.codeVerifier().isEmpty();
        }
        return request.grant() instanceof RefreshTokenGrant grant && grant.scope().isEmpty();
    }

    /**
     * Validates the exact registered callback URI, method, uniqueness, and branch members.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     * @throws ValidateException if callback transport or exact parameter vocabulary is invalid
     */
    private AuthorizationResponseDecoder.Decoded callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Eleme callback must not be null");
        if (!options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Eleme callback URI does not match the registered redirect URI");
        }
        final AuthorizationResponseDecoder.Decoded decoded = authorizationResponseDecoder.decode(callback);
        int code = 0;
        int state = 0;
        int error = 0;
        int descriptionCount = 0;
        for (Callback.Parameter parameter : callback.parameters()) {
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code++;
                case OAuth2.Parameters.STATE -> state++;
                case OAuth2.Parameters.ERROR -> error++;
                case OAuth2.Parameters.ERROR_DESCRIPTION -> descriptionCount++;
                default -> throw new ValidateException("Eleme authorization callback contains an unexpected parameter");
            }
        }
        if (decoded instanceof AuthorizationResponseDecoder.Success) {
            if (code != 1 || state != 1 || error != 0 || descriptionCount != 0) {
                throw new ValidateException("Eleme authorization success has unexpected callback parameters");
            }
        } else {
            if (code != 0 || error != 1 || descriptionCount != 1 || state > 1) {
                throw new ValidateException("Eleme authorization error has unexpected callback parameters");
            }
            final String description = ((AuthorizationResponseDecoder.Error) decoded).response().errorDescription()
                    .getOrNull();
            if (description == null || description.isBlank()) {
                throw new ValidateException("Eleme authorization error requires a non-blank error_description");
            }
        }
        return decoded;
    }

    /**
     * Applies RFC 6749 Appendix B form encoding to one HTTP Basic credential component.
     *
     * @param value decoded client identifier or client password
     * @return form-encoded credential component
     */
    private String formComponent(final String value) {
        final byte[] encoded = formCodec.encode(List.of(new Parameter(Normal.EMPTY, value)));
        try {
            return new String(encoded, 1, encoded.length - 1, Charset.UTF_8);
        } finally {
            clear(encoded);
        }
    }

    /**
     * Carries one validated private Eleme merchant result.
     *
     * @param subject    positive merchant user identifier
     * @param attributes validated merchant attributes
     * @author Kimi Liu
     */
    private record Merchant(String subject, JsonValue.ObjectValue attributes) {

        /**
         * Validates one typed private merchant result.
         *
         * @throws IllegalArgumentException if a component is absent or blank
         */
        private Merchant {
            Assert.notBlank(subject, "Eleme merchant subject must not be blank");
            attributes = Assert.notNull(attributes, "Eleme merchant attributes must not be null");
        }

        /**
         * Decodes the private Eleme merchant result into a typed value.
         *
         * @param result strict merchant result object
         * @return typed merchant result
         * @throws ValidateException if the identifier or registered members are invalid
         */
        private static Merchant decode(final JsonValue.ObjectValue result) {
            merchantResult(result);
            final JsonValue identifier = result.values().get("userId");
            if (!(identifier instanceof JsonValue.NumberValue number)) {
                throw new ValidateException("Eleme merchant userId must be an exact JSON number");
            }
            final BigInteger value;
            try {
                value = number.value().toBigIntegerExact();
            } catch (ArithmeticException cause) {
                throw new ValidateException("Eleme merchant userId must be integral", cause);
            }
            if (value.signum() <= 0) {
                throw new ValidateException("Eleme merchant userId must be positive");
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            attributes.put("userName", new JsonValue.StringValue(requiredString(result, "userName")));
            final JsonValue shops = result.values().get("authorizedShops");
            if (shops != null && !(shops instanceof JsonValue.NullValue)) {
                attributes.put("authorizedShops", shops);
            }
            return new Merchant(value.toString(), new JsonValue.ObjectValue(attributes));
        }

    }

}
