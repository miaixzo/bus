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
package org.miaixz.bus.auth.vendor.mi.internal;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2SourceProfile;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationResponseDecoder;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.mi.MiDefinition;
import org.miaixz.bus.auth.vendor.mi.MiSourceSettings;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements Xiaomi browser authentication and its registered OAuth-compatible public operations.
 * <p>
 * The adapter preserves Xiaomi's GET query token transport, mandatory response prefix, token extensions, and
 * query-authenticated profile resources without moving them into the standard OAuth client. State correlation remains
 * owned by the shared browser flow, and only the token response {@code openId} becomes the external subject.
 * </p>
 *
 * @author Kimi Liu
 */
public final class MiSourceAdapter implements VendorAdapter {

    /**
     * Prefix required before every Xiaomi token JSON document.
     */
    private static final String TOKEN_PREFIX = "&&&START&&&";

    /**
     * Trusted Xiaomi account resource authority recorded in identity evidence.
     */
    private static final String AUTHORITY = "https://open.account.xiaomi.com";

    /**
     * Maximum accepted Xiaomi JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum accepted Xiaomi JSON nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = 32;

    /**
     * Standard OAuth token errors classified as expected request or credential rejection.
     */
    private static final Set<OAuth2ErrorCode> REJECTED_ERRORS = Set.of(
            OAuth2ErrorCode.INVALID_REQUEST,
            OAuth2ErrorCode.INVALID_CLIENT,
            OAuth2ErrorCode.INVALID_GRANT,
            OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
            OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE,
            OAuth2ErrorCode.INVALID_SCOPE);

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Xiaomi definition.
     */
    private final VendorDefinition.Definition variantDefinition;

    /**
     * Validated externally loaded Xiaomi settings.
     */
    private final MiSourceSettings settings;

    /**
     * Caller-owned runtime, JSON, network, secret, and execution dependencies.
     */
    private final ExecutionServices services;

    /**
     * Unified router for Xiaomi's public standard authorization and token capabilities.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time OAuth state lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Strict standard OAuth authorization response decoder.
     */
    private final AuthorizationResponseDecoder authorizationResponseDecoder;

    /**
     * Creates one Source-bound Xiaomi adapter.
     *
     * @param namespaceId       registration namespace used to isolate state and secret resolution
     * @param sourceId          registered Source identifier
     * @param vendorDefinition  selected Xiaomi definition
     * @param variantDefinition exact default definition
     * @param settings          decoded externally loaded Xiaomi settings
     * @param services          caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, definition, or callback settings are inconsistent
     */
    public MiSourceAdapter(final String namespaceId, final String sourceId, final MiDefinition vendorDefinition,
            final VendorDefinition.Definition variantDefinition, final MiSourceSettings settings,
            final ExecutionServices services) {
        final MiDefinition selected = Assert.notNull(vendorDefinition, "Xiaomi definition must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Xiaomi Source id must not be blank");
        this.variantDefinition = Assert.notNull(variantDefinition, "Xiaomi definition must not be null");
        this.settings = Assert.notNull(settings, "Xiaomi settings must not be null");
        this.services = Assert.notNull(services, "Xiaomi execution services must not be null");
        if (!MiDefinition.ID.equals(selected.type())
                || !selected.variant(MiDefinition.DEFAULT).equals(variantDefinition)
                || !MiDefinition.DEFAULT.equals(variantDefinition.variant())
                || variantDefinition.protocol() != Protocol.OAUTH2 || !MiDefinition.ID.equals(settings.vendor())
                || !MiDefinition.DEFAULT.equals(settings.variant()) || settings.redirectUri().isEmpty()) {
            throw new ValidateException("Xiaomi adapter requires the mi/default OAuth 2.0 definition");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variantDefinition, settings, services);
        this.authorizationResponseDecoder = new AuthorizationResponseDecoder();
        this.standardAdapter = new StandardAdapter(variantDefinition, settings, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2SourceProfile.AUTHORIZATION,
                                (request, context, timeout) -> authorization(request)),
                        new StandardAdapter.Binding<>(OAuth2SourceProfile.TOKEN, this::token)));
    }

    /**
     * Extracts one successful Xiaomi envelope data object.
     *
     * @param envelope decoded profile or contact envelope
     * @param label    safe resource label
     * @return required data object
     * @throws ValidateException if result, description, or data types are invalid
     */
    private static JsonValue.ObjectValue successData(final JsonValue.ObjectValue envelope, final String label) {
        final String result = requiredString(envelope, "result");
        optionalString(envelope, "description");
        if (OAuth2.Parameters.ERROR.equalsIgnoreCase(result)) {
            throw new ValidateException(label + " returned an error result");
        }
        final JsonValue data = envelope.values().get("data");
        if (!(data instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException(label + " lacks a data object");
        }
        return object;
    }

    /**
     * Verifies that a Xiaomi token success contains only documented standard and platform extension members.
     *
     * @param object decoded token response
     * @return whether every present member has a registered token meaning
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue object) {
        for (String member : object.values().keySet()) {
            final boolean known = switch (member) {
                case OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.EXPIRES_IN, OAuth2.Parameters.SCOPE, OAuth2.Parameters.TOKEN_TYPE, OAuth2.Parameters.REFRESH_TOKEN, "openId", "mac_algorithm", "mac_key" -> true;
                default -> false;
            };
            if (!known) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts one standard authorization error to a safe rejected outcome.
     *
     * @param error decoded standard OAuth error branch
     * @param <T>   expected success type
     * @return rejected outcome containing only registered non-sensitive identifiers
     */
    private static <T> Outcome<T> oauthError(final AuthorizationResponseDecoder.Error error) {
        final Map<String, JsonValue> details = new LinkedHashMap<>();
        details.put("oauth_error", new JsonValue.StringValue(error.response().error().value()));
        final String errorUri = error.response().errorUri().getOrNull();
        if (errorUri != null) {
            details.put(OAuth2.Parameters.ERROR_URI, new JsonValue.StringValue(errorUri));
        }
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400, "Xiaomi authorization endpoint returned a standard error",
                        new JsonValue.ObjectValue(details)));
    }

    /**
     * Reads one required non-blank JSON string member.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return required string
     * @throws ValidateException if the member is absent, blank, or not a string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = optionalString(object, name);
        if (value == null) {
            throw new ValidateException("Xiaomi required JSON member is absent or invalid: " + name);
        }
        return value;
    }

    /**
     * Reads one optional non-blank JSON string member.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return string value or {@code null} when absent or JSON null
     * @throws ValidateException if a present value is blank or not a string
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Xiaomi optional JSON member is invalid: " + name);
        }
        return string.value();
    }

    /**
     * Reads one positive exact JSON integer.
     *
     * @param value number candidate
     * @param name  safe member name
     * @return positive exact long
     * @throws ValidateException if the value is not a positive exact integer
     */
    private static long positiveLong(final JsonValue value, final String name) {
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Xiaomi numeric response member is invalid: " + name);
        }
        try {
            final long decoded = number.value().longValueExact();
            if (decoded <= 0L) {
                throw new ValidateException("Xiaomi numeric response member must be positive: " + name);
            }
            return decoded;
        } catch (ArithmeticException cause) {
            throw new ValidateException("Xiaomi numeric response member must be an exact long: " + name, cause);
        }
    }

    /**
     * Classifies an unexpected HTTP status without exposing response data.
     *
     * @param status      HTTP status
     * @param description non-sensitive description
     * @param <T>         expected successful value type
     * @return rejected 4xx or failed upstream outcome
     */
    private static <T> Outcome<T> status(final int status, final String description) {
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, description);
        }
        if (status >= Http.Status.INTERNAL_SERVER_ERROR || status < 100) {
            return failed(ErrorCode._502, description);
        }
        return rejected(description);
    }

    /**
     * Creates one exact integral JSON number.
     *
     * @param value integral value
     * @return JSON number value
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Clears transient sensitive bytes when present.
     *
     * @param value bytes or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) 0);
        }
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
     * Creates a provider-neutral empty JSON object.
     *
     * @return immutable empty object
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
        return completed(rejected("Xiaomi capability is not declared"));
    }

    /**
     * Creates a completed request-contract rejection.
     *
     * @param <S> expected successful response type
     * @return completed rejection stage
     */
    private static <S> CompletionStage<Outcome<S>> mismatch() {
        return completed(rejected("Xiaomi capability request is invalid"));
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
     * Creates a safe operational failure with non-sensitive structured details.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param details     validated error identifier and HTTP status values
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
     * Returns the exact capability manifest frozen by the Xiaomi definition.
     *
     * @return immutable Source authentication, authorization, and token manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variantDefinition.manifest();
    }

    /**
     * Routes Xiaomi Source authentication and its two public OAuth operations.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Xiaomi-private profile envelopes
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Xiaomi capability must not be null");
        Assert.notNull(context, "Xiaomi invocation context must not be null");
        Assert.notNull(timeout, "Xiaomi invocation budget must not be null");
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
        if (standardAdapter.manifest().capabilities().contains(capability)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return mismatch();
    }

    /**
     * Builds the Xiaomi authorization request from generated state and fixed platform extension data.
     *
     * @param initiation generated state without nonce or PKCE challenge
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return Xiaomi redirect bound to the generated state
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "Xiaomi authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Xiaomi authorization has no remaining time budget"));
        }
        try {
            if (initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
                return completed(
                        failed(ErrorCode._500, "Xiaomi OAuth flow generated unregistered nonce or PKCE material"));
            }
            final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, settings.clientId(),
                    settings.redirectUri(), Optional.of(new Scope(requestedScopes())), Optional.of(initiation.state()),
                    Optional.empty(), Optional.empty(),
                    new JsonValue.ObjectValue(Map.of("skip_confirm", new JsonValue.BooleanValue(false))));
            return standardAdapter.invoke(OAuth2SourceProfile.AUTHORIZATION, request, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<UnoUrl> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<UnoUrl> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<UnoUrl> failed -> Outcome.failed(failed.failure());
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Xiaomi authorization request is invalid"));
        }
    }

    /**
     * Encodes Xiaomi's exact authorization query order.
     *
     * @param request validated standard authorization request plus registered Xiaomi extension
     * @return immutable authorization redirect URL
     */
    private UnoUrl authorize(final AuthorizationRequest request) {
        final String state = request.state().getOrNull();
        final Scope scope = request.scope().getOrNull();
        return variantDefinition.targets().resolve(settings).authorization().getOrNull().url().newBuilder()
                .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                .query(OAuth2.Parameters.CLIENT_ID, request.clientId())
                .query(OAuth2.Parameters.REDIRECT_URI, request.redirectUri().getOrNull())
                .query(OAuth2.Parameters.STATE, state).query(OAuth2.Parameters.SCOPE, scope.format())
                .query("skip_confirm", Normal.FALSE).build();
    }

    /**
     * Validates and encodes one public standard authorization request through the Xiaomi deviation boundary.
     *
     * @param request standard OAuth authorization request
     * @return exact redirect URL or a safe rejection
     */
    private CompletionStage<Outcome<UnoUrl>> authorization(final AuthorizationRequest request) {
        try {
            return valid(request) ? completed(Outcome.succeeded(authorize(request)))
                    : completed(rejected("Xiaomi authorization request differs from the registered definition"));
        } catch (RuntimeException cause) {
            return completed(rejected("Xiaomi authorization request is invalid"));
        }
    }

    /**
     * Extracts the required state from exactly one standard callback branch.
     *
     * @param callback raw inbound callback
     * @return unique non-blank state value
     * @throws ValidateException if target, method, branch, multiplicity, or state is invalid
     */
    private String state(final Callback.Inbound callback) {
        return switch (callback(callback)) {
            case AuthorizationResponseDecoder.Success success -> success.response().state()
                    .orElseThrow(() -> new ValidateException("Xiaomi authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Xiaomi authorization error requires state"));
        };
    }

    /**
     * Redeems one correlated Xiaomi code and resolves its stable OpenID identity.
     *
     * @param completion consumed callback correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return verified Xiaomi identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Xiaomi authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(oauthError(error));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        if (response.issuer().isPresent() || !response.extensions().values().isEmpty()
                || completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(rejected("Xiaomi authorization response contains unregistered flow material"));
        }
        final TokenRequest request = new TokenRequest(
                new AuthorizationCodeGrant(response.code(), settings.redirectUri(), Optional.empty(), Optional.empty()),
                emptyObject());
        return token(request, context, timeout).thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<TokenResponse> success -> profile(success.value(), timeout);
            case Outcome.Rejected<TokenResponse> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<TokenResponse> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Resolves one Client Secret lease and executes an allowed Xiaomi token grant.
     *
     * @param request standard authorization-code or refresh-token request
     * @param context immutable invocation context used for secret resolution
     * @param timeout shared end-to-end budget
     * @return mapped standard token response preserving registered Xiaomi extensions
     */
    private CompletionStage<Outcome<TokenResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        if (!request.extensions().values().isEmpty() || !valid(request.grant())) {
            return completed(rejected("Xiaomi token request does not match its registered grant contract"));
        }
        return services.secretResolver().resolve(settings.credential(), context, timeout)
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> CompletableFuture.supplyAsync(() -> {
                        try (SecretLease secret = success.value()) {
                            return send(request.grant(), secret, timeout);
                        } catch (RuntimeException cause) {
                            return failed(ErrorCode._502, "Xiaomi token operation failed");
                        }
                    }, services.executor());
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Sends Xiaomi's query-authenticated GET token request.
     *
     * @param grant   validated authorization-code or refresh-token grant
     * @param secret  open Client Secret lease
     * @param timeout shared end-to-end budget
     * @return decoded standard token response or safely classified failure
     */
    private Outcome<TokenResponse> send(
            final TokenRequest.Grant grant,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Xiaomi token request has no remaining time budget");
        }
        final char[] secretChars = secret.material();
        try {
            final String secretValue = new String(secretChars);
            final UnoUrl.Builder target = variantDefinition.targets().resolve(settings).token().getOrNull().url()
                    .newBuilder();
            if (grant instanceof AuthorizationCodeGrant authorization) {
                target.query(OAuth2.Parameters.CODE, authorization.code())
                        .query(OAuth2.Parameters.CLIENT_ID, settings.clientId())
                        .query(OAuth2.Parameters.CLIENT_SECRET, secretValue)
                        .query(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value())
                        .query(OAuth2.Parameters.REDIRECT_URI, settings.redirectUri().getOrNull());
            } else if (grant instanceof RefreshTokenGrant refresh) {
                target.query(OAuth2.Parameters.CLIENT_ID, settings.clientId())
                        .query(OAuth2.Parameters.CLIENT_SECRET, secretValue)
                        .query(OAuth2.Parameters.REFRESH_TOKEN, refresh.refreshToken())
                        .query(OAuth2.Parameters.GRANT_TYPE, GrantType.REFRESH_TOKEN.value())
                        .query(OAuth2.Parameters.REDIRECT_URI, settings.redirectUri().getOrNull());
            } else {
                return rejected("Xiaomi token grant is unsupported");
            }
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(target.build().toString())
                    .method(Http.Method.GET).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Xiaomi token request failed");
        } finally {
            Arrays.fill(secretChars, '\0');
        }
    }

    /**
     * Strictly decodes Xiaomi's prefixed token document.
     *
     * @param response owned token endpoint response
     * @return mapped standard token response or closed failure
     */
    private Outcome<TokenResponse> token(final HttpResponse response) {
        byte[] body = null;
        byte[] json = null;
        try {
            if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
                return failed(ErrorCode._429, "Xiaomi token endpoint rate limited the request");
            }
            if (response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
                return failed(ErrorCode._502, "Xiaomi token endpoint is unavailable");
            }
            final MediaType media = response.body().media();
            if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(media)
                    && !MediaType.TEXT_PLAIN_TYPE.isCompatible(media)) {
                return failed(ErrorCode._502, "Xiaomi token endpoint returned an unsupported representation");
            }
            body = response.bytes(MAXIMUM_JSON_BYTES);
            final byte[] prefix = TOKEN_PREFIX.getBytes(Charset.UTF_8);
            if (body.length <= prefix.length) {
                throw new ValidateException("Xiaomi token response lacks its required prefix and JSON document");
            }
            for (int index = 0; index < prefix.length; index++) {
                if (body[index] != prefix[index]) {
                    throw new ValidateException("Xiaomi token response lacks its required prefix");
                }
            }
            json = Arrays.copyOfRange(body, prefix.length, body.length);
            final JsonValue value = services.jsonProvider().readValue(json, MAXIMUM_JSON_DEPTH, true);
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("Xiaomi token response must contain a JSON object");
            }
            if (object.values().containsKey(OAuth2.Parameters.ERROR)) {
                return tokenError(response.code(), object);
            }
            if (response.code() != Http.Status.OK || !tokenMembers(object)) {
                return status(response.code(), "Xiaomi token endpoint returned an invalid success branch");
            }
            final String access = requiredString(object, OAuth2.Parameters.ACCESS_TOKEN);
            final long expires = positiveLong(
                    object.values().get(OAuth2.Parameters.EXPIRES_IN),
                    OAuth2.Parameters.EXPIRES_IN);
            final Scope scope = Scope.parse(requiredString(object, OAuth2.Parameters.SCOPE));
            final TokenType type = new TokenType(requiredString(object, OAuth2.Parameters.TOKEN_TYPE));
            final String openId = requiredString(object, "openId");
            final String refresh = optionalString(object, OAuth2.Parameters.REFRESH_TOKEN);
            final String macAlgorithm = optionalString(object, "mac_algorithm");
            final String macKey = optionalString(object, "mac_key");
            if ((macAlgorithm == null) != (macKey == null)) {
                throw new ValidateException("Xiaomi MAC token extension members must occur together");
            }
            final Map<String, JsonValue> extensions = new LinkedHashMap<>();
            extensions.put("openId", new JsonValue.StringValue(openId));
            if (macAlgorithm != null) {
                extensions.put("mac_algorithm", new JsonValue.StringValue(macAlgorithm));
                extensions.put("mac_key", new JsonValue.StringValue(macKey));
            }
            return Outcome.succeeded(
                    new TokenResponse(access, type, Optional.of(expires), Optional.ofNullable(refresh),
                            Optional.of(scope), new JsonValue.ObjectValue(extensions)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Xiaomi token endpoint returned an invalid response");
        } finally {
            clear(body);
            clear(json);
        }
    }

    /**
     * Classifies one exact standard OAuth error object returned by Xiaomi.
     *
     * @param status HTTP response status
     * @param object decoded error object
     * @return rejected client error or failed upstream error
     */
    private Outcome<TokenResponse> tokenError(final int status, final JsonValue.ObjectValue object) {
        if (!object.values().containsKey(OAuth2.Parameters.ERROR) || object.values()
                .size() != (object.values().containsKey(OAuth2.Parameters.ERROR_DESCRIPTION) ? 2 : 1)) {
            throw new ValidateException("Xiaomi token error object is invalid");
        }
        final OAuth2ErrorCode error = new OAuth2ErrorCode(requiredString(object, OAuth2.Parameters.ERROR));
        optionalString(object, OAuth2.Parameters.ERROR_DESCRIPTION);
        final Map<String, JsonValue> details = Map
                .of("oauth_error", new JsonValue.StringValue(error.value()), "status", number(status));
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "Xiaomi token endpoint rate limited the request", details);
        }
        if (status >= Http.Status.INTERNAL_SERVER_ERROR || OAuth2ErrorCode.SERVER_ERROR.equals(error)
                || OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE.equals(error)) {
            return failed(ErrorCode._502, "Xiaomi token endpoint returned an upstream error", details);
        }
        return REJECTED_ERRORS.contains(error)
                ? Outcome.rejected(
                        new Outcome.Failure(ErrorCode._400, "Xiaomi token endpoint rejected the request",
                                new JsonValue.ObjectValue(details)))
                : failed(ErrorCode._502, "Xiaomi token endpoint returned an unknown OAuth error", details);
    }

    /**
     * Retrieves the Xiaomi profile and optional contact projection asynchronously.
     *
     * @param token   mapped token response containing the access token and OpenID extension
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> profile(
            final TokenResponse token,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> profileNow(token, timeout), services.executor());
    }

    /**
     * Retrieves and maps Xiaomi profile resources using the token response OpenID as subject.
     *
     * @param token   mapped token response
     * @param timeout shared end-to-end budget
     * @return verified external identity or safely classified outcome
     */
    private Outcome<ExternalIdentity> profileNow(final TokenResponse token, final Timeout.Budget timeout) {
        final JsonValue openIdValue = token.extensions().values().get("openId");
        if (!(openIdValue instanceof JsonValue.StringValue openId) || openId.value().isBlank()) {
            return failed(ErrorCode._502, "Xiaomi token response lacks its stable OpenID");
        }
        final String endpoint = variantDefinition.targets().resolve(settings).userInfo().getOrNull().url().toString();
        final Outcome<JsonValue.ObjectValue> profile = resource(endpoint, token.accessToken(), timeout);
        return switch (profile) {
            case Outcome.Succeeded<JsonValue.ObjectValue> success -> identity(
                    openId.value(),
                    success.value(),
                    token.accessToken(),
                    timeout);
            case Outcome.Rejected<JsonValue.ObjectValue> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<JsonValue.ObjectValue> failed -> Outcome.failed(failed.failure());
        };
    }

    /**
     * Maps the required Xiaomi profile and best-effort historical contact email.
     *
     * @param subject     stable token OpenID
     * @param envelope    decoded profile envelope
     * @param accessToken sensitive access token used for optional contact lookup
     * @param timeout     shared clock and network budget
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> identity(
            final String subject,
            final JsonValue.ObjectValue envelope,
            final String accessToken,
            final Timeout.Budget timeout) {
        try {
            final JsonValue.ObjectValue data = successData(envelope, "Xiaomi profile");
            final String nickname = requiredString(data, "miliaoNick");
            final String icon = optionalString(data, "miliaoIcon");
            String email = optionalString(data, "mail");
            final String contact = contactEndpoint();
            final Outcome<JsonValue.ObjectValue> contactResult = resource(contact, accessToken, timeout);
            if (contactResult instanceof Outcome.Succeeded<JsonValue.ObjectValue> success) {
                try {
                    final String projected = optionalString(successData(success.value(), "Xiaomi contact"), "email");
                    if (projected != null) {
                        email = projected;
                    }
                } catch (RuntimeException ignored) {
                    // Historical contact projection is optional and cannot invalidate a verified profile.
                }
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            attributes.put("miliaoNick", new JsonValue.StringValue(nickname));
            if (icon != null) {
                attributes.put("miliaoIcon", new JsonValue.StringValue(icon));
            }
            if (email != null) {
                attributes.put("email", new JsonValue.StringValue(email));
            }
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("xiaomi_open_id", new JsonValue.StringValue(subject), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Xiaomi profile response is invalid");
        }
    }

    /**
     * Executes one bounded Xiaomi query-authenticated profile GET.
     *
     * @param endpoint    fixed profile or contact endpoint
     * @param accessToken sensitive access token
     * @param timeout     shared end-to-end budget
     * @return decoded JSON object or safely classified outcome
     */
    private Outcome<JsonValue.ObjectValue> resource(
            final String endpoint,
            final String accessToken,
            final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Xiaomi profile request has no remaining time budget");
        }
        try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                .query("clientId", settings.clientId()).query("token", accessToken)
                .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
            if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
                return failed(ErrorCode._429, "Xiaomi profile endpoint rate limited the request");
            }
            if (response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
                return failed(ErrorCode._502, "Xiaomi profile endpoint is unavailable");
            }
            if (response.code() == Http.Status.UNAUTHORIZED || response.code() == Http.Status.FORBIDDEN
                    || response.code() == Http.Status.BAD_REQUEST) {
                return rejected("Xiaomi profile endpoint rejected the access token");
            }
            if (response.code() != Http.Status.OK
                    || !MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
                return failed(ErrorCode._502, "Xiaomi profile endpoint returned an invalid response");
            }
            final JsonValue parsed = services.jsonProvider()
                    .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
            if (!(parsed instanceof JsonValue.ObjectValue object)) {
                return failed(ErrorCode._502, "Xiaomi profile response must be a JSON object");
            }
            if (OAuth2.Parameters.ERROR.equalsIgnoreCase(optionalString(object, "result"))) {
                return rejected("Xiaomi profile endpoint returned an error result");
            }
            return Outcome.succeeded(object);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Xiaomi profile request failed");
        }
    }

    /**
     * Validates one public Xiaomi authorization request.
     *
     * @param request standard OAuth request with the registered skip-confirm extension
     * @return {@code true} when every registration and platform value is exact
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request.scope().getOrNull();
        final JsonValue skip = request.extensions().values().get("skip_confirm");
        return ResponseType.CODE.equals(request.responseType()) && settings.clientId().equals(request.clientId())
                && settings.redirectUri().equals(request.redirectUri()) && scope != null
                && requestedScopes().equals(scope.values()) && request.state().isPresent()
                && request.codeChallenge().isEmpty() && request.codeChallengeMethod().isEmpty()
                && request.extensions().values().size() == 1 && skip instanceof JsonValue.BooleanValue flag
                && !flag.value();
    }

    /**
     * Validates one Xiaomi token grant against the registered client and callback.
     *
     * @param grant standard token grant
     * @return {@code true} for a bound authorization code or non-expanding refresh token
     */
    private boolean valid(final TokenRequest.Grant grant) {
        if (grant instanceof AuthorizationCodeGrant authorization) {
            final String clientId = authorization.clientId().getOrNull();
            return settings.redirectUri().equals(authorization.redirectUri())
                    && (clientId == null || settings.clientId().equals(clientId))
                    && authorization.codeVerifier().isEmpty();
        }
        if (grant instanceof RefreshTokenGrant refresh) {
            final Scope scope = refresh.scope().getOrNull();
            return scope == null || requestedScopes().containsAll(scope.values());
        }
        return false;
    }

    /**
     * Validates the exact callback target before standard OAuth response decoding.
     *
     * @param callback raw inbound callback
     * @return discriminated standard response
     * @throws ValidateException if the callback target differs from the registered URI
     */
    private AuthorizationResponseDecoder.Decoded callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Xiaomi callback must not be null");
        if (!settings.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Xiaomi callback URI does not match the registered redirect URI");
        }
        return authorizationResponseDecoder.decode(callback);
    }

    /**
     * Returns the sibling Xiaomi contact endpoint derived from the fixed profile path.
     *
     * @return fixed phone-and-email endpoint
     */
    private String contactEndpoint() {
        final URI profile = URI
                .create(variantDefinition.targets().resolve(settings).userInfo().getOrNull().url().toString());
        final String path = profile.getPath();
        return profile.resolve(path.substring(0, path.lastIndexOf(Symbol.C_SLASH) + 1) + "phoneAndEmail").toString();
    }

    /**
     * Returns explicit scopes or immutable definition defaults.
     *
     * @return ordered effective Xiaomi scopes
     */
    private List<String> requestedScopes() {
        return settings.scopes().isEmpty() ? variantDefinition.defaultScopes() : settings.scopes();
    }

}
