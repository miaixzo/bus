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
package org.miaixz.bus.auth.vendor.oschina;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationResponseDecoder;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceWorkflow;
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
 * Implements the frozen OSChina OAuth 2.0 Source and registered token transport deviations.
 * <p>
 * Registry callers use only {@link AuthorizationRequest}, {@link TokenRequest}, and {@link TokenResponse}. The
 * platform-specific GET query client authentication, {@code dataType}, {@code uid}, and profile representation remain
 * private implementation details and never become framework-wide OAuth fields.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OsChinaSourceAdapter implements VendorAdapter {

    /**
     * Trusted OSChina authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://www.oschina.net";

    /**
     * Maximum bounded JSON response size accepted from OSChina.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Maximum JSON nesting accepted from OSChina endpoint responses.
     */
    private static final int MAXIMUM_JSON_DEPTH = Normal._16;

    /**
     * Standard OAuth errors classified as request rejection rather than upstream failure.
     */
    private static final Set<OAuth2ErrorCode> REJECTED_ERRORS = Set.of(
            OAuth2ErrorCode.INVALID_REQUEST,
            OAuth2ErrorCode.INVALID_CLIENT,
            OAuth2ErrorCode.INVALID_GRANT,
            OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
            OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE);

    /**
     * Registered Source identifier copied into verified external identities.
     */
    private final String sourceId;

    /**
     * Selected immutable OSChina variant manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded OSChina registration options.
     */
    private final OsChinaOptions options;

    /**
     * Caller-owned runtime, secret, JSON, network, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Unified router for OSChina's public standard authorization and token capabilities.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time state coordinator for browser authentication.
     */
    private final RedirectManager redirectManager;

    /**
     * Standard authorization request encoder bound to the fixed OSChina endpoint.
     */
    private final AuthorizationRequestEncoder authorizationEncoder;

    /**
     * Strict standard authorization response decoder.
     */
    private final AuthorizationResponseDecoder callbackDecoder;

    /**
     * Creates one Source-bound OSChina adapter from the frozen default manifest.
     *
     * @param namespaceId registration namespace isolating browser and credential state
     * @param sourceId    registered Source identifier
     * @param manifest    selected OSChina manifest
     * @param variant     selected default variant manifest
     * @param options     decoded externally loaded OSChina options
     * @param services    caller-owned runtime dependencies
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, manifest, options, or routing differ from the frozen variant
     */
    public OsChinaSourceAdapter(final String namespaceId, final String sourceId, final OsChinaManifest manifest,
            final VariantManifest.Variant variant, final OsChinaOptions options, final DriverServices services) {
        Assert.notNull(manifest, "OSChina manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "OSChina Source id must not be blank");
        this.variant = Assert.notNull(variant, "OSChina manifest must not be null");
        this.options = Assert.notNull(options, "OSChina options must not be null");
        this.services = Assert.notNull(services, "OSChina execution services must not be null");
        if (!OsChinaManifest.ID.equals(manifest.vendor()) || !manifest.variant(OsChinaManifest.DEFAULT).equals(variant)
                || !OsChinaManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !OsChinaManifest.ID.equals(options.vendor()) || !OsChinaManifest.DEFAULT.equals(options.variant())) {
            throw new ValidateException("OSChina adapter requires the oschina/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.authorizationEncoder = new AuthorizationRequestEncoder(
                variant.targets().resolve(options).authorization().getOrNull());
        this.callbackDecoder = new AuthorizationResponseDecoder();
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION,
                                (request, context, timeout) -> authorization(request)),
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.TOKEN, this::token)));
    }

    /**
     * Maps a standard authorization error to a safe rejection.
     *
     * @param response decoded standard OAuth error response
     * @return rejected identity outcome
     */
    private static Outcome<ExternalIdentity> authorizationError(final AuthorizationErrorResponse response) {
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400, "OSChina authorization endpoint returned a standard error",
                        new JsonValue.ObjectValue(
                                Map.of(Builder.OAUTH_ERROR, new JsonValue.StringValue(response.error().value())))));
    }

    /**
     * Returns one required JSON member.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return present JSON value
     * @throws ValidateException if the member is absent
     */
    private static JsonValue required(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            throw new ValidateException("OSChina response lacks required member: " + name);
        }
        return value;
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
            throw new ValidateException("OSChina response requires non-blank string member: " + name);
        }
        return value;
    }

    /**
     * Verifies the mandatory and optional members of an OSChina token success.
     *
     * @param object decoded token response
     * @return whether only documented members are present and required members exist
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue object) {
        if (!object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                || !object.values().containsKey(OAuth2.Parameters.TOKEN_TYPE)
                || !object.values().containsKey(OAuth2.Parameters.EXPIRES_IN) || !object.values().containsKey("uid")) {
            return false;
        }
        for (String member : object.values().keySet()) {
            if (!OAuth2.Parameters.ACCESS_TOKEN.equals(member) && !OAuth2.Parameters.REFRESH_TOKEN.equals(member)
                    && !OAuth2.Parameters.TOKEN_TYPE.equals(member) && !OAuth2.Parameters.EXPIRES_IN.equals(member)
                    && !"uid".equals(member)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies the standard OSChina token error member shape.
     *
     * @param object decoded error object
     * @return whether error is mandatory and error_description is the only optional member
     */
    private static boolean errorMembers(final JsonValue.ObjectValue object) {
        return object.values().containsKey(OAuth2.Parameters.ERROR)
                && object.values().size() == (object.values().containsKey(OAuth2.Parameters.ERROR_DESCRIPTION) ? 2 : 1);
    }

    /**
     * Verifies that an OSChina profile contains only documented current-user members.
     *
     * @param object decoded profile object
     * @return whether every present member has a registered profile meaning
     */
    private static boolean profileMembers(final JsonValue.ObjectValue object) {
        for (String member : object.values().keySet()) {
            final boolean known = switch (member) {
                case "id", "email", "name", "gender", "avatar", "location", "url" -> true;
                default -> false;
            };
            if (!known) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copies the active client secret into a transient query value and clears intermediate characters.
     *
     * @param lease open client-secret lease
     * @return transient query value
     */
    private static String secret(final SecretLease lease) {
        final char[] material = lease.material();
        try {
            return new String(material);
        } finally {
            java.util.Arrays.fill(material, Symbol.C_NUL);
        }
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
            throw new ValidateException("OSChina response member must be a JSON string: " + name);
        }
        return string.value();
    }

    /**
     * Validates a token response identifier represented as string or exact integral number.
     *
     * @param value identifier JSON value
     * @param name  safe member name
     * @throws ValidateException if the identifier is blank, fractional, or another JSON type
     */
    private static void identifier(final JsonValue value, final String name) {
        if (value instanceof JsonValue.StringValue string && !string.value().isBlank()) {
            return;
        }
        if (value instanceof JsonValue.NumberValue number) {
            try {
                number.value().toBigIntegerExact();
                return;
            } catch (ArithmeticException cause) {
                throw new ValidateException("OSChina identifier must be an exact integer: " + name, cause);
            }
        }
        throw new ValidateException("OSChina identifier has an invalid JSON type: " + name);
    }

    /**
     * Reads one positive exact integral JSON lifetime.
     *
     * @param value JSON number candidate
     * @param name  safe member name
     * @return positive exact long value
     * @throws ValidateException if the value is not a positive exact long
     */
    private static long positiveLong(final JsonValue value, final String name) {
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("OSChina response member must be numeric: " + name);
        }
        try {
            final long decoded = number.value().longValueExact();
            if (decoded <= 0L) {
                throw new ValidateException("OSChina response lifetime must be positive: " + name);
            }
            return decoded;
        } catch (ArithmeticException cause) {
            throw new ValidateException("OSChina response lifetime must be an exact long: " + name, cause);
        }
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
     * Creates a safe operational failure with non-sensitive protocol details.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param details     validated error code and status only
     * @param <T>         expected successful value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(
            final Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.failed(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Returns the exact frozen OSChina capability manifest.
     *
     * @return immutable Source authentication and OAuth capability set
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication, standard authorization, and the registered token deviation.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source authentication request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing a private platform model
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "OSChina capability must not be null");
        Assert.notNull(context, "OSChina invocation context must not be null");
        Assert.notNull(timeout, "OSChina invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("OSChina capability is not declared"));
        }
        if (capability.key().equals(SourceWorkflow.INITIATE.key())
                && request instanceof SourceWorkflow.Request.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (capability.key().equals(SourceWorkflow.COMPLETE.key())
                && request instanceof SourceWorkflow.Request.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::identity, context, timeout),
                    capability.responseType());
        }
        if (standardAdapter.manifest().capabilities().contains(capability)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return completed(rejected("OSChina capability request is invalid"));
    }

    /**
     * Builds the standard authorization redirect around generated one-time state.
     *
     * @param initiation generated browser correlation material
     * @param context    immutable invocation context retained for the uniform operation signature
     * @param timeout    shared end-to-end time budget
     * @return prepared authorization redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "OSChina authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "OSChina browser material violates the frozen manifest"));
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
            return completed(rejected("OSChina authorization request is invalid"));
        }
    }

    /**
     * Validates one public standard authorization request against the registered Source.
     *
     * @param request standard OAuth authorization request
     * @return whether client, callback, scope, state, response type, and extensions are exact
     */
    private boolean valid(final AuthorizationRequest request) {
        return ResponseType.CODE.equals(request.responseType()) && options.clientId().equals(request.clientId())
                && options.redirectUri().equals(request.redirectUri()) && scope().equals(request.scope())
                && request.state().isPresent() && request.codeChallenge().isEmpty()
                && request.codeChallengeMethod().isEmpty() && request.extensions().values().isEmpty();
    }

    /**
     * Validates and encodes one public standard OSChina authorization request.
     *
     * @param request standard authorization request
     * @return exact authorization URL or a safe rejection
     */
    private CompletionStage<Outcome<UnoUrl>> authorization(final AuthorizationRequest request) {
        try {
            return valid(request) ? completed(Outcome.succeeded(authorizationEncoder.encode(request)))
                    : completed(rejected("OSChina authorization request differs from the registered Source"));
        } catch (RuntimeException cause) {
            return completed(rejected("OSChina authorization request is invalid"));
        }
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
                    .orElseThrow(() -> new ValidateException("OSChina authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("OSChina authorization error requires state"));
        };
    }

    /**
     * Completes the correlated authorization code flow and retrieves the private OSChina profile.
     *
     * @param completion consumed callback correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @return verified OSChina external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = decode(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("OSChina authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(authorizationError(error.response()));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        if (completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(failed(ErrorCode._500, "OSChina callback contains unexpected browser material"));
        }
        final TokenRequest request = new TokenRequest(
                new AuthorizationCodeGrant(response.code(), options.redirectUri(), Optional.empty(), Optional.empty()),
                emptyObject());
        return token(request, context, timeout).thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<TokenResponse> success -> profile(success.value(), context, timeout);
            case Outcome.Rejected<TokenResponse> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<TokenResponse> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Executes one supported authorization-code token request under an operation-scoped secret lease.
     *
     * @param request standard token request
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return standard token response or a closed failure
     */
    private CompletionStage<Outcome<TokenResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        if (!valid(request)) {
            return completed(rejected("OSChina token request differs from the registered grant contract"));
        }
        return Outcome
                .mapStage(
                        () -> services.secretLoader()
                                .load(services.registration(), options.credential(), context, timeout),
                        loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SecretLease> success -> CompletableFuture.supplyAsync(() -> {
                        try (SecretLease secret = success.value()) {
                            return sendToken(request, secret, timeout);
                        } catch (RuntimeException cause) {
                            return failed(ErrorCode._502, "OSChina token operation failed");
                        }
                    }, services.executor());
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Validates the only standard grant shape registered by the OSChina profile.
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
     * Sends the historical OSChina GET query token request and strictly maps its response.
     *
     * @param request validated authorization-code token request
     * @param secret  open client-secret lease
     * @param timeout shared end-to-end time budget
     * @return standard token response or classified error
     */
    private Outcome<TokenResponse> sendToken(
            final TokenRequest request,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "OSChina token request has no remaining time budget");
        }
        try {
            final AuthorizationCodeGrant grant = (AuthorizationCodeGrant) request.grant();
            final String location = variant.targets().resolve(options).token().getOrNull().url().newBuilder()
                    .query(OAuth2.Parameters.CODE, grant.code()).query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.CLIENT_SECRET, secret(secret))
                    .query(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()).query("dataType", "json")
                    .build().toString();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(location).method(Http.Method.GET)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "OSChina token endpoint request failed");
        }
    }

    /**
     * Decodes one exact OSChina token success or standard error object.
     *
     * @param response owned token endpoint response
     * @return standard token response or classified OAuth error
     */
    private Outcome<TokenResponse> token(final HttpResponse response) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (object.values().containsKey(OAuth2.Parameters.ERROR)) {
                return tokenError(response.code(), object);
            }
            if (response.code() != Http.Status.OK || !tokenMembers(object)) {
                throw new ValidateException("OSChina token success shape is invalid");
            }
            final TokenType type = new TokenType(requiredString(object, OAuth2.Parameters.TOKEN_TYPE));
            if (!TokenType.BEARER.equals(type)) {
                throw new ValidateException("OSChina access token must use the Bearer token type");
            }
            final JsonValue uid = required(object, "uid");
            identifier(uid, "uid");
            return Outcome.succeeded(
                    new TokenResponse(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN), type, Optional.of(
                            positiveLong(required(object, OAuth2.Parameters.EXPIRES_IN), OAuth2.Parameters.EXPIRES_IN)),
                            Optional.ofNullable(optionalString(object, OAuth2.Parameters.REFRESH_TOKEN)),
                            Optional.empty(), new JsonValue.ObjectValue(Map.of("uid", uid))));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "OSChina token endpoint returned an invalid response");
        }
    }

    /**
     * Classifies one strict OSChina OAuth error object.
     *
     * @param status HTTP response status
     * @param object decoded error object
     * @return rejected client error or failed upstream error
     */
    private Outcome<TokenResponse> tokenError(final int status, final JsonValue.ObjectValue object) {
        if (!errorMembers(object)) {
            throw new ValidateException("OSChina token error shape is invalid");
        }
        final OAuth2ErrorCode error = new OAuth2ErrorCode(requiredString(object, OAuth2.Parameters.ERROR));
        optionalString(object, OAuth2.Parameters.ERROR_DESCRIPTION);
        final Map<String, JsonValue> details = Map.of(
                Builder.OAUTH_ERROR,
                new JsonValue.StringValue(error.value()),
                "status",
                new JsonValue.NumberValue(BigDecimal.valueOf(status)));
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "OSChina token endpoint rate limited the request", details);
        }
        if (status >= Http.Status.INTERNAL_SERVER_ERROR || OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE.equals(error)) {
            return failed(ErrorCode._502, "OSChina token endpoint returned an upstream error", details);
        }
        return REJECTED_ERRORS.contains(error) || "400".equals(error.value()) || "401".equals(error.value())
                ? Outcome.rejected(
                        new Outcome.Failure(ErrorCode._400, "OSChina token endpoint rejected the request",
                                new JsonValue.ObjectValue(details)))
                : failed(ErrorCode._502, "OSChina token endpoint returned an unknown OAuth error", details);
    }

    /**
     * Retrieves the private OSChina profile using the access-token query transport.
     *
     * @param token   standard token response carrying the bearer access token
     * @param context immutable invocation context retained for the uniform operation signature
     * @param timeout shared end-to-end time budget
     * @return verified profile identity stage
     */
    private CompletionStage<Outcome<ExternalIdentity>> profile(
            final TokenResponse token,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "OSChina profile context must not be null");
        return CompletableFuture.supplyAsync(() -> sendProfile(token, timeout), services.executor());
    }

    /**
     * Sends one private profile request and maps the stable OSChina identity.
     *
     * @param token   standard token response
     * @param timeout shared end-to-end time budget
     * @return verified external identity or classified failure
     */
    private Outcome<ExternalIdentity> sendProfile(final TokenResponse token, final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "OSChina profile request has no remaining time budget");
        }
        try {
            final String location = variant.targets().resolve(options).userInfo().getOrNull().url().newBuilder()
                    .query(OAuth2.Parameters.ACCESS_TOKEN, token.accessToken()).query("dataType", "json").build()
                    .toString();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(location).method(Http.Method.GET)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return profile(response, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "OSChina profile endpoint request failed");
        }
    }

    /**
     * Strictly validates and maps one OSChina profile response.
     *
     * @param response owned profile response
     * @param timeout  shared clock used for evidence timestamping
     * @return verified external identity or classified error
     */
    private Outcome<ExternalIdentity> profile(final HttpResponse response, final Timeout.Budget timeout) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (object.values().containsKey(OAuth2.Parameters.ERROR)) {
                final Outcome<TokenResponse> error = tokenError(response.code(), object);
                return switch (error) {
                    case Outcome.Rejected<TokenResponse> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<TokenResponse> failed -> Outcome.failed(failed.failure());
                    case Outcome.Succeeded<TokenResponse> ignored -> throw new ValidateException(
                            "OSChina profile error cannot be successful");
                };
            }
            if (response.code() != Http.Status.OK || !profileMembers(object)) {
                throw new ValidateException("OSChina profile success shape is invalid");
            }
            final ProfileWire profile = ProfileWire.decode(object);
            final String subject = profile.id();
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("id", new JsonValue.StringValue(subject), AUTHORITY, timeout.clock().now()));
            return Outcome.succeeded(new ExternalIdentity(sourceId, subject, profile.attributes(), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "OSChina profile endpoint returned an invalid response");
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
        final Callback.Inbound inbound = Assert.notNull(callback, "OSChina callback must not be null");
        if (inbound.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(inbound.requestUri())) {
            throw new ValidateException("OSChina callback URI or method is invalid");
        }
        return callbackDecoder.decode(inbound);
    }

    /**
     * Returns the configured scope container in standard model form.
     *
     * @return empty scope when none is configured, otherwise the ordered standard scope
     */
    private Optional<Scope> scope() {
        return options.scopes().isEmpty() ? Optional.empty() : Optional.of(new Scope(options.scopes()));
    }

    /**
     * Strictly reads one bounded OSChina JSON response object.
     *
     * @param response response whose body remains owned by the caller
     * @return immutable provider-neutral JSON object
     * @throws ValidateException if media type, size, JSON syntax, duplicate names, or root shape is invalid
     */
    private JsonValue.ObjectValue object(final HttpResponse response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("OSChina response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("OSChina response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries the typed OSChina profile projection used by external identity mapping.
     *
     * @param id       stable OSChina account identifier
     * @param email    optional email address
     * @param name     optional display name
     * @param gender   optional platform gender value
     * @param avatar   optional avatar URL
     * @param location optional location text
     * @param url      optional profile URL
     * @author Kimi Liu
     */
    private record ProfileWire(String id, String email, String name, String gender, String avatar, String location,
            String url) {

        /**
         * Decodes one validated profile object into its typed projection.
         *
         * @param object decoded profile object
         * @return typed profile projection
         */
        private static ProfileWire decode(final JsonValue.ObjectValue object) {
            return new ProfileWire(requiredString(object, "id"), optionalString(object, "email"),
                    optionalString(object, "name"), optionalString(object, "gender"), optionalString(object, "avatar"),
                    optionalString(object, "location"), optionalString(object, "url"));
        }

        /**
         * Adds one present typed profile value to the attribute object.
         *
         * @param target mutable attribute map
         * @param name   profile member name
         * @param value  optional typed value
         */
        private static void put(final Map<String, JsonValue> target, final String name, final String value) {
            if (value != null) {
                target.put(name, new JsonValue.StringValue(value));
            }
        }

        /**
         * Converts optional typed profile values into immutable identity attributes.
         *
         * @return immutable provider-neutral attribute object
         */
        private JsonValue.ObjectValue attributes() {
            final Map<String, JsonValue> values = new LinkedHashMap<>();
            put(values, "email", email);
            put(values, "name", name);
            put(values, "gender", gender);
            put(values, "avatar", avatar);
            put(values, "location", location);
            put(values, "url", url);
            return new JsonValue.ObjectValue(values);
        }

    }

}
