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
package org.miaixz.bus.auth.source.vendor.vk;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Identity.Evidence;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.auth.source.SourceServices;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.*;
import org.miaixz.bus.auth.source.protocol.oauth2.client.AuthorizationClient;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.TokenRequestEncoder;
import org.miaixz.bus.auth.source.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.source.vendor.RedirectManager;
import org.miaixz.bus.auth.source.vendor.StandardAdapter;
import org.miaixz.bus.auth.source.vendor.VendorAdapter;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
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
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements VK ID Source authentication and the registered OAuth 2.0 wire adaptations.
 * <p>
 * Standard authorization is delegated unchanged. Token and revocation retain the standard OAuth request and response
 * contracts while this adapter maps VK's device binding, client identifier placement, error members, and successful
 * revocation marker. The private current-user envelope is consumed only by Source authentication.
 * </p>
 *
 * @author Kimi Liu
 */
public class VkSourceAdapter implements VendorAdapter {

    /**
     * Trusted VK ID authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://id.vk.com";

    /**
     * Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable VK manifest.
     */
    private final VendorManifest.Variant variant;

    /**
     * Validated externally loaded VK options.
     */
    private final VkOptions options;

    /**
     * Caller-owned runtime, JSON, network, clock, and state dependencies.
     */
    private final SourceServices services;

    /**
     * Shared standard OAuth authorization implementation.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time state and mandatory PKCE lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Shared standard grant-to-form parameter encoder.
     */
    private final TokenRequestEncoder tokenEncoder;

    /**
     * Shared strict UTF-8 form representation codec.
     */
    private final FormCodec formCodec;

    /**
     * Creates one Source-bound VK ID adapter.
     *
     * @param spaceId  Source space used to isolate browser state
     * @param sourceId Source identifier
     * @param manifest selected VK Source manifest
     * @param variant  exact selected default manifest
     * @param options  decoded externally loaded VK options
     * @param services capability-limited Source services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, manifest, callback, or PKCE options are inconsistent
     */
    public VkSourceAdapter(final String spaceId, final String sourceId, final VkManifest manifest,
            final VendorManifest.Variant variant, final VkOptions options, final SourceServices services) {
        final VkManifest selected = Assert.notNull(manifest, "VK manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "VK Source id must not be blank");
        this.variant = Assert.notNull(variant, "VK manifest must not be null");
        this.options = Assert.notNull(options, "VK options must not be null");
        this.services = Assert.notNull(services, "VK execution services must not be null");
        if (!VkManifest.ID.equals(selected.vendor()) || !selected.variant(VkManifest.DEFAULT).equals(variant)
                || !VkManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !VkManifest.ID.equals(options.vendor()) || !VkManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("VK adapter requires the vk/default OAuth 2.0 manifest with S256 PKCE");
        }
        this.redirectManager = RedirectManager.create(spaceId, sourceId, variant, options, services);
        final var targets = variant.targets().resolve(options);
        final OAuth2ClientOptions oauthSettings = new OAuth2ClientOptions(targets.authorization(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                options.clientId(), Set.of(options.redirectUri().getOrNull()), Endpoint.Authentication.NONE,
                Optional.empty(), true, false);
        final AuthorizationClient authorizationClient = new AuthorizationClient(oauthSettings,
                new AuthorizationRequestEncoder(targets.authorization().getOrNull()));
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager), List
                .of(new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION, authorizationClient::authorize)));
        this.tokenEncoder = new TokenRequestEncoder();
        this.formCodec = new FormCodec();
    }

    /**
     * Reads one required non-blank JSON string member.
     *
     * @param object decoded VK object
     * @param name   exact member name
     * @return non-blank string value
     * @throws ValidateException if the member is absent, blank, or has another JSON type
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue text) || text.value().isBlank()) {
            throw new ValidateException("VK response requires a non-blank string member");
        }
        return text.value();
    }

    /**
     * Reads one optional nullable string member.
     *
     * @param object decoded VK object
     * @param name   exact member name
     * @return string value or {@code null} when absent or JSON null
     * @throws ValidateException if a present non-null member has another JSON type
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException("VK optional response member must be a string or null");
        }
        return text.value().isBlank() ? null : text.value();
    }

    /**
     * Reads one optional non-negative exact integral lifetime.
     *
     * @param object decoded VK object
     * @param name   exact member name
     * @return lifetime or {@code null} when absent or JSON null
     * @throws ValidateException if a present value is negative, fractional, or has another JSON type
     */
    private static Long optionalNonNegativeLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("VK expires_in must be a JSON number");
        }
        try {
            final long result = number.value().longValueExact();
            if (result < 0L) {
                throw new ValidateException("VK expires_in must not be negative");
            }
            return result;
        } catch (ArithmeticException cause) {
            throw new ValidateException("VK expires_in must be an exact integer", cause);
        }
    }

    /**
     * Reads one required non-blank string extension.
     *
     * @param object token request extension object
     * @param name   exact VK extension name
     * @return non-blank extension value
     * @throws ValidateException if the extension is missing, blank, or not a string
     */
    private static String requiredExtension(final JsonValue.ObjectValue object, final String name) {
        return requiredString(object, name);
    }

    /**
     * Verifies every member of one selected private VK document by semantic document kind.
     *
     * @param kind   selected private document kind
     * @param object decoded VK object
     * @return whether every decoded member is defined for the selected document
     */
    private static boolean members(final WireKind kind, final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            final boolean known = switch (kind) {
                case TOKEN -> switch (name) {
                    case OpenIdConnect.Parameters.ID_TOKEN, OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.TOKEN_TYPE, OAuth2.Parameters.SCOPE, "user_id", OAuth2.Parameters.EXPIRES_IN -> true;
                    default -> false;
                };
                case ERROR -> switch (name) {
                    case OAuth2.Parameters.ERROR, "message" -> true;
                    default -> false;
                };
                case PROFILE_ENVELOPE -> "user".equals(name);
                case PROFILE -> switch (name) {
                    case "user_id", "first_name", "last_name", "avatar", "email" -> true;
                    default -> false;
                };
                case REVOCATION -> "response".equals(name);
            };
            if (!known) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reports whether a private VK object selects its error branch.
     *
     * @param object decoded VK object
     * @return {@code true} when an error or message member is present
     */
    private static boolean error(final JsonValue.ObjectValue object) {
        return object.values().containsKey(OAuth2.Parameters.ERROR) || object.values().containsKey("message");
    }

    /**
     * Recognizes VK's historical string or numeric success marker one.
     *
     * @param value decoded response marker
     * @return {@code true} only for string {@code "1"} or exact numeric {@code 1}
     */
    private static boolean one(final JsonValue value) {
        return value instanceof JsonValue.StringValue text && Symbol.ONE.equals(text.value())
                || value instanceof JsonValue.NumberValue number && BigDecimal.ONE.compareTo(number.value()) == 0;
    }

    /**
     * Creates an empty implementation-neutral JSON object.
     *
     * @return immutable empty object
     */
    private static JsonValue.ObjectValue empty() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Classifies one VK HTTP status without copying remote response text.
     *
     * @param status      exact HTTP status
     * @param description non-sensitive failure description
     * @param <T>         expected success type
     * @return rejected or failed outcome
     */
    private static <T> Outcome<T> status(final int status, final String description) {
        final Map<String, JsonValue> details = Map.of("status", number(status));
        if (status == Http.Status.BAD_REQUEST || status == Http.Status.UNAUTHORIZED
                || status == Http.Status.FORBIDDEN) {
            return Outcome
                    .rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(details)));
        }
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, description, details);
        }
        return failed(ErrorCode._502, description, details);
    }

    /**
     * Creates one implementation-neutral JSON integer.
     *
     * @param value integral value
     * @return immutable JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Narrows a delegated outcome to the capability-declared response type.
     *
     * @param stage        delegated asynchronous stage
     * @param responseType declared response class
     * @param <S>          successful response type
     * @return narrowed outcome stage
     */
    private static <S> CompletionStage<Outcome<S>> narrow(
            final CompletionStage<? extends Outcome<?>> stage,
            final Class<S> responseType) {
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<?> success -> Outcome.succeeded(responseType.cast(success.value()));
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Wraps one already determined outcome in a completed stage.
     *
     * @param outcome outcome to wrap
     * @param <T>     successful response type
     * @return completed asynchronous stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates one expected VK request or authorization rejection.
     *
     * @param description non-sensitive rejection description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates one operational failure without sensitive details.
     *
     * @param error       shared Bus error code
     * @param description non-sensitive failure description
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final Errors error, final String description) {
        return failed(error, description, Map.of());
    }

    /**
     * Creates one operational failure with explicitly safe details.
     *
     * @param error       shared Bus error code
     * @param description non-sensitive failure description
     * @param details     implementation-neutral safe details
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(
            final Errors error,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.failed(new Outcome.Failure(error, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Erases one mutable sensitive form body after transport completion.
     *
     * @param value mutable bytes or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) 0);
        }
    }

    /**
     * Accepts one callback value only once.
     *
     * @param current  previously decoded value
     * @param incoming newly decoded value
     * @return incoming value when no value was previously decoded
     * @throws ValidateException if the parameter occurred more than once
     */
    private static String unique(final String current, final String incoming) {
        if (current != null) {
            throw new ValidateException("VK callback parameter names must be unique");
        }
        return incoming;
    }

    /**
     * Returns the exact Source authentication and OAuth capability manifest.
     *
     * @return immutable VK capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication, standard authorization, adapted token, and adapted revocation operations.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific Source or OAuth request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing private VK response models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "VK capability must not be null");
        Assert.notNull(context, "VK invocation context must not be null");
        Assert.notNull(timeout, "VK invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("VK capability is not declared by the selected manifest"));
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
        if (capability.equals(OAuth2ClientScheme.AUTHORIZATION) && request instanceof AuthorizationRequest authorization
                && valid(authorization)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        if (capability.equals(OAuth2ClientScheme.TOKEN) && request instanceof TokenRequest token) {
            return narrow(token(token, timeout), capability.responseType());
        }
        if (capability.equals(OAuth2ClientScheme.REVOCATION) && request instanceof RevocationRequest revocation) {
            return narrow(revoke(revocation, timeout), capability.responseType());
        }
        return completed(rejected("VK request does not match the selected capability contract"));
    }

    /**
     * Builds the exact VK authorization redirect through the standard OAuth authorization operation.
     *
     * @param initiation generated state and mandatory S256 challenge
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return exact redirect correlated by state
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout timeout) {
        final var challenge = initiation.codeChallenge().getOrNull();
        if (initiation.nonce().isPresent() || challenge == null || !PkceMethod.S256.equals(challenge.method())) {
            return completed(failed(ErrorCode._500, "VK browser flow lacks mandatory S256 PKCE material"));
        }
        final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                options.redirectUri(), Optional.of(new Scope(options.scopes())), Optional.of(initiation.state()),
                Optional.of(challenge.value()), Optional.of(PkceMethod.S256.value()), empty());
        return standardAdapter.invoke(OAuth2ClientScheme.AUTHORIZATION, request, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Url> success -> Outcome
                            .succeeded(new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                    case Outcome.Rejected<Url> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Url> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Extracts the unique callback state after validating the complete VK callback branch.
     *
     * @param callback raw inbound callback
     * @return unique non-blank state value
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Completes one correlated VK browser authorization and resolves the private current-user identity.
     *
     * @param completion consumed state and mandatory PKCE verifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified VK identity
     */
    private CompletionStage<Outcome<Identity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("VK authorization callback is invalid"));
        }
        if (values.failed()) {
            return completed(rejected("VK authorization endpoint rejected the request"));
        }
        if (completion.codeVerifier().isEmpty()) {
            return completed(failed(ErrorCode._500, "VK callback lacks its mandatory PKCE verifier"));
        }
        final AuthorizationCodeGrant grant = new AuthorizationCodeGrant(values.code(), options.redirectUri(),
                Optional.of(options.clientId()), Optional.of(completion.codeVerifier().getOrNull().value()));
        final TokenRequest request = new TokenRequest(grant,
                new JsonValue.ObjectValue(Map.of(
                        OAuth2.Parameters.STATE,
                        new JsonValue.StringValue(values.state()),
                        "device_id",
                        new JsonValue.StringValue(values.deviceId()))));
        return token(request, timeout).thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<TokenResponse> success -> profile(success.value(), timeout);
            case Outcome.Rejected<TokenResponse> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<TokenResponse> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Executes one standard authorization-code or refresh-token grant using VK's registered form extensions.
     *
     * @param request standard OAuth token request
     * @param timeout shared end-to-end timeout
     * @return standard token response with VK user_id retained as an extension
     */
    private CompletionStage<Outcome<TokenResponse>> token(final TokenRequest request, final Timeout timeout) {
        final List<NameValue> parameters;
        try {
            parameters = tokenParameters(request);
        } catch (RuntimeException cause) {
            return completed(rejected("VK token request does not satisfy the registered grant contract"));
        }
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try {
                if (timeout.expired()) {
                    return VkSourceAdapter
                            .<TokenResponse>failed(ErrorCode._408, "VK token request has no remaining timeout");
                }
                body = formCodec.encode(parameters);
                final var endpoint = variant.targets().resolve(options).token().getOrNull();
                try (Response response = FabricX.http(Protocol.OAUTH2, timeout, services.policies())
                        .url(endpoint.url().toString()).method(Http.Method.POST)
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                    return decodeToken(response);
                }
            } catch (RuntimeException cause) {
                return VkSourceAdapter.<TokenResponse>failed(ErrorCode._502, "VK token request failed");
            } finally {
                clear(body);
            }
        }, services.executor());
    }

    /**
     * Converts a supported standard grant into VK's exact form without manufacturing an IP address.
     *
     * @param request validated standard token request
     * @return immutable ordered VK form parameters
     * @throws ValidateException if grant type, registered values, or extension cardinality is invalid
     */
    private List<NameValue> tokenParameters(final TokenRequest request) {
        Assert.notNull(request, "VK token request must not be null");
        TokenBinding.decode(request.extensions());
        if (request.grant() instanceof AuthorizationCodeGrant grant) {
            if (!options.redirectUri().equals(grant.redirectUri())
                    || !Optional.of(options.clientId()).equals(grant.clientId()) || grant.codeVerifier().isEmpty()) {
                throw new ValidateException("VK authorization-code grant configuration values are invalid");
            }
            return tokenEncoder.encode(request);
        }
        if (request.grant() instanceof RefreshTokenGrant) {
            final List<NameValue> result = new ArrayList<>(tokenEncoder.encode(request));
            result.add(new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()));
            return List.copyOf(result);
        }
        throw new ValidateException("VK token endpoint supports authorization_code and refresh_token grants only");
    }

    /**
     * Strictly decodes one VK token success or error response into the standard OAuth result contract.
     *
     * @param response open owned token response
     * @return standard token response or safely classified rejection/failure
     */
    private Outcome<TokenResponse> decodeToken(final Response response) {
        final JsonValue.ObjectValue object;
        try {
            object = object(response);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "VK token endpoint returned invalid JSON");
        }
        if (error(object)) {
            return tokenError(response.code(), object);
        }
        if (response.code() != Http.Status.OK || !members(WireKind.TOKEN, object)
                || !object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                || !object.values().containsKey(OAuth2.Parameters.TOKEN_TYPE)) {
            return status(response.code(), "VK token endpoint returned an invalid success response");
        }
        try {
            final String accessToken = requiredString(object, OAuth2.Parameters.ACCESS_TOKEN);
            final TokenType tokenType = new TokenType(requiredString(object, OAuth2.Parameters.TOKEN_TYPE));
            final Long expiresIn = optionalNonNegativeLong(object, OAuth2.Parameters.EXPIRES_IN);
            final String refreshToken = optionalString(object, OAuth2.Parameters.REFRESH_TOKEN);
            final String scope = optionalString(object, OAuth2.Parameters.SCOPE);
            final String idToken = optionalString(object, OpenIdConnect.Parameters.ID_TOKEN);
            final String userId = optionalString(object, "user_id");
            final Map<String, JsonValue> extensions = new LinkedHashMap<>();
            if (userId != null) {
                extensions.put("user_id", new JsonValue.StringValue(userId));
            }
            if (idToken != null) {
                extensions.put(OpenIdConnect.Parameters.ID_TOKEN, new JsonValue.StringValue(idToken));
            }
            return Outcome.succeeded(
                    new TokenResponse(accessToken, tokenType, Optional.ofNullable(expiresIn),
                            Optional.ofNullable(refreshToken),
                            scope == null ? Optional.empty() : Optional.of(Scope.parse(scope)),
                            new JsonValue.ObjectValue(extensions)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "VK token endpoint returned invalid member values");
        }
    }

    /**
     * Maps one VK token error envelope without exposing its remote message.
     *
     * @param status exact HTTP status
     * @param object decoded VK error object
     * @return expected rejection or upstream failure
     */
    private Outcome<TokenResponse> tokenError(final int status, final JsonValue.ObjectValue object) {
        if (!members(WireKind.ERROR, object) || object.values().isEmpty()) {
            return failed(ErrorCode._502, "VK token endpoint returned an invalid error envelope");
        }
        final String error;
        try {
            error = object.values().containsKey(OAuth2.Parameters.ERROR)
                    ? requiredString(object, OAuth2.Parameters.ERROR)
                    : requiredString(object, "message");
            if (object.values().containsKey("message")) {
                requiredString(object, "message");
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "VK token endpoint returned invalid error members");
        }
        final Map<String, JsonValue> details = Map
                .of(Builder.OAUTH_ERROR, new JsonValue.StringValue(error), "status", number(status));
        return status >= Http.Status.INTERNAL_SERVER_ERROR || status == Http.Status.TOO_MANY_REQUESTS
                ? failed(
                        status == Http.Status.TOO_MANY_REQUESTS ? ErrorCode._429 : ErrorCode._502,
                        "VK token endpoint failed the request",
                        details)
                : Outcome.rejected(
                        new Outcome.Failure(ErrorCode._400, "VK token endpoint rejected the request",
                                new JsonValue.ObjectValue(details)));
    }

    /**
     * Retrieves VK's private current-user envelope with the issued access token.
     *
     * @param token   standard token response produced by the immediately preceding grant
     * @param timeout shared end-to-end timeout
     * @return verified VK external identity
     */
    private CompletionStage<Outcome<Identity>> profile(final TokenResponse token, final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try {
                body = formCodec.encode(
                        List.of(
                                new NameValue(OAuth2.Parameters.ACCESS_TOKEN, token.accessToken()),
                                new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId())));
                final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
                try (Response response = FabricX.http(Protocol.OAUTH2, timeout, services.policies())
                        .url(endpoint.url().toString()).method(Http.Method.POST)
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                    return decodeProfile(response, token, timeout);
                }
            } catch (RuntimeException cause) {
                return VkSourceAdapter.<Identity>failed(ErrorCode._502, "VK current-user request failed");
            } finally {
                clear(body);
            }
        }, services.executor());
    }

    /**
     * Strictly decodes the VK current-user envelope and establishes subject binding.
     *
     * @param response open owned current-user response
     * @param token    immediately preceding standard token response
     * @param timeout  shared clock and timeout
     * @return verified external identity or safely classified failure
     */
    private Outcome<Identity> decodeProfile(final Response response, final TokenResponse token, final Timeout timeout) {
        final JsonValue.ObjectValue envelope;
        try {
            envelope = object(response);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "VK current-user endpoint returned invalid JSON");
        }
        if (error(envelope)) {
            return profileError(response.code(), envelope);
        }
        if (response.code() != Http.Status.OK || !members(WireKind.PROFILE_ENVELOPE, envelope)
                || envelope.values().size() != 1
                || !(envelope.values().get("user") instanceof JsonValue.ObjectValue user)
                || !members(WireKind.PROFILE, user)) {
            return status(response.code(), "VK current-user endpoint returned an invalid success envelope");
        }
        try {
            final String subject = requiredString(user, "user_id");
            final JsonValue tokenSubject = token.extensions().values().get("user_id");
            if (tokenSubject instanceof JsonValue.StringValue text && !subject.equals(text.value())) {
                return rejected("VK token and current-user identifiers do not match");
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            ProfileWire.decode(user).copyAttributes(attributes);
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("vk_user_id", new JsonValue.StringValue(subject), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new Identity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "VK current-user endpoint returned invalid member values");
        }
    }

    /**
     * Maps a private VK profile error envelope without copying its message into diagnostics.
     *
     * @param status exact HTTP status
     * @param object decoded error envelope
     * @return expected rejection or upstream failure
     */
    private Outcome<Identity> profileError(final int status, final JsonValue.ObjectValue object) {
        if (!members(WireKind.ERROR, object) || object.values().isEmpty()) {
            return failed(ErrorCode._502, "VK current-user endpoint returned an invalid error envelope");
        }
        try {
            if (object.values().containsKey(OAuth2.Parameters.ERROR)) {
                requiredString(object, OAuth2.Parameters.ERROR);
            }
            if (object.values().containsKey("message")) {
                requiredString(object, "message");
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "VK current-user endpoint returned invalid error members");
        }
        return status == Http.Status.BAD_REQUEST || status == Http.Status.UNAUTHORIZED
                || status == Http.Status.FORBIDDEN || status == Http.Status.OK
                        ? rejected("VK current-user endpoint rejected the access token")
                        : status(status, "VK current-user endpoint failed the request");
    }

    /**
     * Adapts the standard revocation request to VK's access_token/client_id form and response marker.
     *
     * @param request standard RFC 7009 revocation request
     * @param timeout shared end-to-end timeout
     * @return successful empty result only for VK response marker one
     */
    private CompletionStage<Outcome<Void>> revoke(final RevocationRequest request, final Timeout timeout) {
        if (request.tokenTypeHint().isPresent()
                && !OAuth2.Parameters.ACCESS_TOKEN.equals(request.tokenTypeHint().getOrNull())) {
            return completed(rejected("VK revocation accepts only an access_token hint"));
        }
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try {
                body = formCodec.encode(
                        List.of(
                                new NameValue(OAuth2.Parameters.ACCESS_TOKEN, request.token()),
                                new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId())));
                final var endpoint = variant.targets().resolve(options).revocation().getOrNull();
                try (Response response = FabricX.http(Protocol.OAUTH2, timeout, services.policies())
                        .url(endpoint.url().toString()).method(Http.Method.POST)
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                    final JsonValue.ObjectValue object = object(response);
                    if (error(object)) {
                        return revokeError(response.code(), object);
                    }
                    if (response.code() != Http.Status.OK || !members(WireKind.REVOCATION, object)
                            || object.values().size() != 1 || !one(object.values().get("response"))) {
                        return VkSourceAdapter
                                .<Void>status(response.code(), "VK revocation endpoint returned an invalid response");
                    }
                    return Outcome.succeeded(null);
                }
            } catch (RuntimeException cause) {
                return VkSourceAdapter.<Void>failed(ErrorCode._502, "VK revocation request failed");
            } finally {
                clear(body);
            }
        }, services.executor());
    }

    /**
     * Maps one VK revocation error envelope without exposing remote text.
     *
     * @param status exact HTTP status
     * @param object decoded VK error envelope
     * @return expected rejection or upstream failure
     */
    private Outcome<Void> revokeError(final int status, final JsonValue.ObjectValue object) {
        if (!members(WireKind.ERROR, object) || object.values().isEmpty()) {
            return failed(ErrorCode._502, "VK revocation endpoint returned an invalid error envelope");
        }
        try {
            if (object.values().containsKey(OAuth2.Parameters.ERROR)) {
                requiredString(object, OAuth2.Parameters.ERROR);
            }
            if (object.values().containsKey("message")) {
                requiredString(object, "message");
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "VK revocation endpoint returned invalid error members");
        }
        return status >= Http.Status.INTERNAL_SERVER_ERROR || status == Http.Status.TOO_MANY_REQUESTS
                ? failed(
                        status == Http.Status.TOO_MANY_REQUESTS ? ErrorCode._429 : ErrorCode._502,
                        "VK revocation endpoint failed the request")
                : rejected("VK revocation endpoint rejected the access token");
    }

    /**
     * Validates one public standard VK authorization request against the compiled registration.
     *
     * @param request standard OAuth authorization request
     * @return whether every registered value and S256 field is exact
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request.scope().getOrNull();
        return ResponseType.CODE.equals(request.responseType()) && options.clientId().equals(request.clientId())
                && options.redirectUri().equals(request.redirectUri()) && scope != null
                && options.scopes().equals(scope.values()) && request.state().isPresent()
                && request.codeChallenge().isPresent()
                && PkceMethod.S256.value().equals(request.codeChallengeMethod().getOrNull())
                && request.extensions().values().isEmpty();
    }

    /**
     * Validates and indexes one exact VK success or OAuth error callback branch.
     *
     * @param callback raw inbound callback
     * @return immutable typed callback branch
     * @throws ValidateException if transport, target, cardinality, branch, or value syntax is invalid
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "VK callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("VK callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        String deviceId = null;
        String error = null;
        String errorDescription = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = parameter.value();
            if (value.isBlank()) {
                throw new ValidateException("VK callback parameter values must not be blank");
            }
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code, value);
                case OAuth2.Parameters.STATE -> state = unique(state, value);
                case "device_id" -> deviceId = unique(deviceId, value);
                case OAuth2.Parameters.ERROR -> error = unique(error, value);
                case OAuth2.Parameters.ERROR_DESCRIPTION -> errorDescription = unique(errorDescription, value);
                default -> throw new ValidateException("VK callback contains an unsupported parameter");
            }
        }
        return new CallbackWire(code, state, deviceId, error, errorDescription);
    }

    /**
     * Decodes one bounded duplicate-free VK JSON object while its response remains caller owned.
     *
     * @param response open VK HTTP response
     * @return implementation-neutral JSON object
     * @throws ValidateException if media type, JSON syntax, duplicate names, or root type is invalid
     */
    private JsonValue.ObjectValue object(final Response response) {
        final MediaType media = response.body().media();
        if (media == null || !MediaType.APPLICATION_JSON_TYPE.isCompatible(media)) {
            throw new ValidateException("VK response must use application/json");
        }
        final JsonValue value = JsonKit.readValue(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES), Normal._64, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("VK response root must be a JSON object");
        }
        return object;
    }

    /**
     * Identifies each private VK JSON document whose members have distinct semantics.
     *
     * @author Kimi Liu
     */
    private enum WireKind {

        /**
         * Token success document.
         */
        TOKEN,

        /**
         * Shared private error document.
         */
        ERROR,

        /**
         * Current-user outer document.
         */
        PROFILE_ENVELOPE,

        /**
         * Current-user object.
         */
        PROFILE,

        /**
         * Revocation success document.
         */
        REVOCATION

    }

    /**
     * Carries the two VK token-request extensions as one typed association.
     *
     * @param state    browser correlation value
     * @param deviceId VK device binding identifier
     *
     * @author Kimi Liu
     */
    private record TokenBinding(String state, String deviceId) {

        /**
         * Decodes exactly the registered VK token extensions.
         *
         * @param object standard token extension object
         * @return immutable typed extension association
         * @throws ValidateException if any member is absent, unknown, duplicated by representation, or invalid
         */
        private static TokenBinding decode(final JsonValue.ObjectValue object) {
            Assert.notNull(object, "VK token extension object must not be null");
            for (String name : object.values().keySet()) {
                if (!OAuth2.Parameters.STATE.equals(name) && !"device_id".equals(name)) {
                    throw new ValidateException("VK token request contains an unsupported extension");
                }
            }
            if (object.values().size() != 2) {
                throw new ValidateException("VK token request requires state and device_id extensions");
            }
            return new TokenBinding(requiredExtension(object, OAuth2.Parameters.STATE),
                    requiredExtension(object, "device_id"));
        }

    }

    /**
     * Carries one exact VK authorization callback branch.
     *
     * @param code             authorization code for success
     * @param state            mandatory browser correlation value
     * @param deviceId         mandatory VK device binding for success
     * @param error            OAuth error for failure
     * @param errorDescription mandatory VK error description for failure
     *
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state, String deviceId, String error, String errorDescription) {

        /**
         * Validates one complete success or failure branch.
         *
         * @throws IllegalArgumentException if state is blank
         * @throws ValidateException        if members do not form one exact branch
         */
        private CallbackWire {
            Assert.notBlank(state, "VK callback state must not be blank");
            final boolean success = code != null && deviceId != null && error == null && errorDescription == null;
            final boolean failure = code == null && deviceId == null && error != null && errorDescription != null;
            if (!success && !failure) {
                throw new ValidateException("VK callback must contain one exact success or OAuth error branch");
            }
        }

        /**
         * Reports whether the callback carries an OAuth error.
         *
         * @return {@code true} for the failure branch
         */
        private boolean failed() {
            return error != null;
        }

    }

    /**
     * Carries the retained non-sensitive VK current-user projection.
     *
     * @param firstName optional given name
     * @param lastName  optional family name
     * @param avatar    optional avatar URL
     * @param email     optional email address
     *
     * @author Kimi Liu
     */
    private record ProfileWire(String firstName, String lastName, String avatar, String email) {

        /**
         * Decodes one member-validated VK current-user object.
         *
         * @param object private current-user object
         * @return immutable typed profile projection
         */
        private static ProfileWire decode(final JsonValue.ObjectValue object) {
            return new ProfileWire(optionalString(object, "first_name"), optionalString(object, "last_name"),
                    optionalString(object, "avatar"), optionalString(object, "email"));
        }

        /**
         * Copies one optional string into the identity projection.
         *
         * @param attributes mutable destination owned by the identity mapper
         * @param name       exact VK wire member name
         * @param value      optional decoded value
         */
        private static void put(final Map<String, JsonValue> attributes, final String name, final String value) {
            if (value != null) {
                attributes.put(name, new JsonValue.StringValue(value));
            }
        }

        /**
         * Copies present profile attributes using their exact VK wire names.
         *
         * @param attributes mutable destination owned by the identity mapper
         */
        private void copyAttributes(final Map<String, JsonValue> attributes) {
            put(attributes, "first_name", firstName);
            put(attributes, "last_name", lastName);
            put(attributes, "avatar", avatar);
            put(attributes, "email", email);
        }

    }

}
