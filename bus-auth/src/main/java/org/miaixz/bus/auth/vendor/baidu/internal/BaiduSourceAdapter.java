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
package org.miaixz.bus.auth.vendor.baidu.internal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.AuthorizationClient;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientSettings;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2SourceProfile;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.baidu.BaiduDefinition;
import org.miaixz.bus.auth.vendor.baidu.BaiduSourceSettings;
import org.miaixz.bus.core.basic.normal.ErrorCode;
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
 * Implements Baidu browser authentication while exposing only its standard OAuth authorization operation.
 * <p>
 * Baidu token and profile documents remain private to the Source-authentication chain because the token success omits
 * the standard {@code token_type} field and the profile resource is not an OpenID Connect UserInfo endpoint.
 * </p>
 *
 * @author Kimi Liu
 */
public final class BaiduSourceAdapter implements VendorAdapter {

    /**
     * Trusted Baidu authority recorded in successful federated identity evidence.
     */
    private static final String AUTHORITY = "https://openapi.baidu.com";

    /**
     * Maximum accepted Baidu JSON response document size.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum accepted Baidu JSON response nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Registered Source identifier copied into verified external identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Baidu definition.
     */
    private final VendorDefinition.Definition variantDefinition;

    /**
     * Validated externally loaded Baidu Source settings.
     */
    private final BaiduSourceSettings settings;

    /**
     * Caller-owned runtime, secret, JSON, network, and execution dependencies.
     */
    private final ExecutionServices services;

    /**
     * Shared one-time state lifecycle for the browser flow.
     */
    private final RedirectManager redirectManager;

    /**
     * Standard OAuth authorization operation composed from protocol-owned settings, client, and codec.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Creates one Source-bound Baidu adapter from the frozen default definition.
     *
     * @param namespaceId       registration namespace used to isolate browser state and credentials
     * @param sourceId          registered Source identifier
     * @param vendorDefinition  selected Baidu definition
     * @param variantDefinition selected default variant definition
     * @param settings          decoded externally loaded Baidu settings
     * @param services          caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, definition, or callback settings differ from the frozen
     *                                  profile
     */
    public BaiduSourceAdapter(final String namespaceId, final String sourceId, final BaiduDefinition vendorDefinition,
            final VendorDefinition.Definition variantDefinition, final BaiduSourceSettings settings,
            final ExecutionServices services) {
        Assert.notNull(vendorDefinition, "Baidu definition must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Baidu Source id must not be blank");
        this.variantDefinition = Assert.notNull(variantDefinition, "Baidu definition must not be null");
        this.settings = Assert.notNull(settings, "Baidu settings must not be null");
        this.services = Assert.notNull(services, "Baidu execution services must not be null");
        if (!BaiduDefinition.ID.equals(vendorDefinition.type())
                || !vendorDefinition.variant(BaiduDefinition.DEFAULT).equals(variantDefinition)
                || !BaiduDefinition.DEFAULT.equals(variantDefinition.variant())
                || variantDefinition.protocol() != Protocol.OAUTH2 || !BaiduDefinition.ID.equals(settings.vendor())
                || !BaiduDefinition.DEFAULT.equals(settings.variant()) || settings.redirectUri().isEmpty()) {
            throw new ValidateException("Baidu adapter requires the baidu/default OAuth 2.0 definition");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variantDefinition, settings, services);
        final var targets = variantDefinition.targets().resolve(settings);
        final OAuth2ClientSettings oauthSettings = new OAuth2ClientSettings(targets.authorization(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                settings.clientId(), Set.of(settings.redirectUri().getOrNull()), Endpoint.Authentication.NONE,
                Optional.empty(), false, false);
        final AuthorizationClient authorizationClient = new AuthorizationClient(oauthSettings,
                new AuthorizationRequestEncoder(targets.authorization().getOrNull()));
        this.standardAdapter = new StandardAdapter(variantDefinition, settings, Optional.of(redirectManager), List
                .of(new StandardAdapter.Binding<>(OAuth2SourceProfile.AUTHORIZATION, authorizationClient::authorize)));
    }

    /**
     * Detects either documented Baidu error branch.
     *
     * @param object decoded Baidu response object
     * @return whether {@code error} or {@code error_code} is present and non-null
     */
    private static boolean hasError(final JsonValue.ObjectValue object) {
        final JsonValue error = object.values().get(OAuth2.Parameters.ERROR);
        final JsonValue code = object.values().get("error_code");
        return error != null && !(error instanceof JsonValue.NullValue)
                || code != null && !(code instanceof JsonValue.NullValue);
    }

    /**
     * Tests one member against Baidu's private token response vocabulary.
     *
     * @param name response member name
     * @return whether the private token decoder recognizes the member
     */
    private static boolean tokenMember(final String name) {
        return switch (name) {
            case OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.SCOPE, OAuth2.Parameters.EXPIRES_IN, "session_key", "session_secret", OAuth2.Parameters.ERROR, "error_code", OAuth2.Parameters.ERROR_DESCRIPTION, "error_msg" -> true;
            default -> false;
        };
    }

    /**
     * Tests one member against Baidu's current and historical private profile vocabulary.
     *
     * @param name response member name
     * @return whether the private profile decoder recognizes the member
     */
    private static boolean profileMember(final String name) {
        return switch (name) {
            case "openid", "unionid", "userid", "username", "userdetail", "sex", "portrait", "birthday", "marriage", "blood", "constellation", "education", "trade", "job", "city", "province", "country", OAuth2.Parameters.ERROR, "error_code", OAuth2.Parameters.ERROR_DESCRIPTION, "error_msg" -> true;
            default -> false;
        };
    }

    /**
     * Reads one mandatory non-blank Baidu string field.
     *
     * @param object decoded response object
     * @param name   exact field name
     * @return non-blank string value
     * @throws ValidateException if the field is absent, blank, or another JSON type
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = optionalString(object, name);
        if (value == null || value.isBlank()) {
            throw new ValidateException("Baidu response requires a non-blank string field");
        }
        return value;
    }

    /**
     * Reads one optional non-blank Baidu string field.
     *
     * @param object decoded response object
     * @param name   exact field name
     * @return string value or {@code null} when absent
     * @throws ValidateException if a present field is blank or another JSON type
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Baidu optional response field must be a non-blank string");
        }
        return string.value();
    }

    /**
     * Reads one exact positive integral Baidu JSON number.
     *
     * @param object decoded response object
     * @param name   exact field name
     * @return positive long value
     * @throws ValidateException if the field is absent, non-integral, non-positive, or another JSON type
     */
    private static long requiredPositiveLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Baidu response requires a numeric lifetime");
        }
        try {
            final long exact = number.value().longValueExact();
            if (exact <= 0L) {
                throw new ValidateException("Baidu response lifetime must be positive");
            }
            return exact;
        } catch (ArithmeticException cause) {
            throw new ValidateException("Baidu response lifetime must be an exact long", cause);
        }
    }

    /**
     * Creates one immutable provider-neutral empty JSON object.
     *
     * @return empty extension object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Narrows an operation outcome through its declared response type.
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
     * Creates a safe expected rejection.
     *
     * @param description non-sensitive rejection description
     * @param <T>         expected successful value type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, emptyObject()));
    }

    /**
     * Creates a safe operational failure.
     *
     * @param description non-sensitive failure description
     * @param <T>         expected successful value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._502, description, emptyObject()));
    }

    /**
     * Returns the exact capability manifest frozen by the selected Baidu definition.
     *
     * @return immutable Source-authentication and OAuth authorization manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variantDefinition.manifest();
    }

    /**
     * Routes Source authentication and the single standard OAuth authorization operation.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Baidu-private token or profile models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Baidu capability must not be null");
        Assert.notNull(context, "Baidu invocation context must not be null");
        Assert.notNull(timeout, "Baidu invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Baidu capability is not declared"));
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
                && request instanceof AuthorizationRequest authorization && valid(authorization)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return completed(rejected("Baidu capability request is invalid"));
    }

    /**
     * Builds the standard Baidu authorization request using generated one-time state.
     *
     * @param initiation generated browser correlation
     * @param context    immutable invocation context retained for the uniform operation signature
     * @param timeout    shared end-to-end budget
     * @return prepared standard authorization redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "Baidu authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed("Baidu authorization security material violates the frozen definition"));
        }
        try {
            final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, settings.clientId(),
                    settings.redirectUri(), Optional.of(new Scope(requestedScopes())), Optional.of(initiation.state()),
                    Optional.empty(), Optional.empty(), emptyObject());
            return standardAdapter.invoke(OAuth2SourceProfile.AUTHORIZATION, request, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<UnoUrl> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<UnoUrl> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<UnoUrl> failed -> Outcome.failed(failed.failure());
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Baidu authorization request is invalid"));
        }
    }

    /**
     * Validates one public standard authorization request against the registered Baidu Source.
     *
     * @param request standard OAuth authorization request
     * @return whether all client, redirect, scope, state, response, extension, and PKCE fields are exact
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request.scope().getOrNull();
        return ResponseType.CODE.equals(request.responseType()) && settings.clientId().equals(request.clientId())
                && settings.redirectUri().equals(request.redirectUri()) && scope != null
                && requestedScopes().equals(scope.values()) && request.state().isPresent()
                && request.codeChallenge().isEmpty() && request.codeChallengeMethod().isEmpty()
                && request.extensions().values().isEmpty();
    }

    /**
     * Extracts the unique callback state from one exact Baidu success or rejection branch.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return unique non-blank state value
     * @throws ValidateException if callback transport, target, branch, or multiplicity is invalid
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Exchanges a consumed Baidu callback and maps the verified profile to an external identity.
     *
     * @param completion consumed callback correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return verified Baidu external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final CallbackWire callback;
        try {
            callback = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Baidu authorization callback is invalid"));
        }
        if (callback.error() != null) {
            return completed(rejected("Baidu resource owner denied authorization"));
        }
        final CompletionStage<Outcome<SecretLease>> resolution;
        try {
            resolution = services.secretResolver().resolve(settings.credential(), context, timeout);
        } catch (RuntimeException cause) {
            return completed(failed("Baidu client-secret resolution failed"));
        }
        if (resolution == null) {
            return completed(failed("Baidu client-secret resolver returned no stage"));
        }
        return resolution
                .handle(
                        (outcome, cause) -> cause == null && outcome != null ? outcome
                                : BaiduSourceAdapter.<SecretLease>failed("Baidu client-secret resolution failed"))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SecretLease> success -> token(callback.code(), success.value(), timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Sends the exact query-authenticated Baidu authorization-code request and closes the secret lease.
     *
     * @param code    consumed authorization code
     * @param secret  owned client-secret lease
     * @param timeout shared end-to-end budget
     * @return verified identity after token and profile processing
     */
    private CompletionStage<Outcome<ExternalIdentity>> token(
            final String code,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try (secret) {
                if (timeout.expired()) {
                    return BaiduSourceAdapter.<Access>failed("Baidu token request has no remaining time budget");
                }
                final String endpoint = variantDefinition.targets().resolve(settings).token().getOrNull().url()
                        .toString();
                try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                        .query(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value())
                        .query(OAuth2.Parameters.CODE, code).query(OAuth2.Parameters.CLIENT_ID, settings.clientId())
                        .query(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material()))
                        .query(OAuth2.Parameters.REDIRECT_URI, settings.redirectUri().getOrNull())
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                        .execute()) {
                    return decodeToken(response);
                }
            } catch (RuntimeException cause) {
                return BaiduSourceAdapter.<Access>failed("Baidu token request failed");
            }
        }, services.executor()).thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<Access> success -> profile(success.value(), timeout);
            case Outcome.Rejected<Access> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<Access> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Strictly decodes one Baidu token response without fabricating a standard token type.
     *
     * @param response owned Baidu HTTP response
     * @return private access-token result or safely classified failure
     */
    private Outcome<Access> decodeToken(final HttpResponse response) {
        if (response.code() == Http.Status.TOO_MANY_REQUESTS || response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed("Baidu token endpoint is unavailable");
        }
        if (response.code() >= Http.Status.BAD_REQUEST) {
            return rejected("Baidu token endpoint rejected the authorization code");
        }
        final JsonValue.ObjectValue object;
        try {
            object = object(response);
        } catch (RuntimeException cause) {
            return failed("Baidu token endpoint returned an invalid response");
        }
        for (String member : object.values().keySet()) {
            if (!tokenMember(member)) {
                return failed("Baidu token response contains an unregistered member");
            }
        }
        if (hasError(object)) {
            return rejected("Baidu token endpoint returned an error");
        }
        try {
            final String accessToken = requiredString(object, OAuth2.Parameters.ACCESS_TOKEN);
            requiredString(object, OAuth2.Parameters.REFRESH_TOKEN);
            final long expiresIn = requiredPositiveLong(object, OAuth2.Parameters.EXPIRES_IN);
            optionalString(object, OAuth2.Parameters.SCOPE);
            optionalString(object, "session_key");
            optionalString(object, "session_secret");
            return Outcome.succeeded(new Access(accessToken, expiresIn));
        } catch (RuntimeException cause) {
            return rejected("Baidu token success response is invalid");
        }
    }

    /**
     * Retrieves the Baidu profile using its official access-token query field.
     *
     * @param access  private token result
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> profile(final Access access, final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final String endpoint = variantDefinition.targets().resolve(settings).userInfo().getOrNull().url()
                        .toString();
                try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                        .query(OAuth2.Parameters.ACCESS_TOKEN, access.accessToken()).query("get_unionid", Symbol.ONE)
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                        .execute()) {
                    return decodeProfile(response, timeout);
                }
            } catch (RuntimeException cause) {
                return BaiduSourceAdapter.<ExternalIdentity>failed("Baidu profile request failed");
            }
        }, services.executor());
    }

    /**
     * Maps one strict Baidu profile using only the current {@code openid} subject identifier.
     *
     * @param response owned profile HTTP response
     * @param timeout  shared clock used for evidence
     * @return verified external identity or safely classified failure
     */
    private Outcome<ExternalIdentity> decodeProfile(final HttpResponse response, final Timeout.Budget timeout) {
        if (response.code() == Http.Status.TOO_MANY_REQUESTS || response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed("Baidu profile endpoint is unavailable");
        }
        if (response.code() >= Http.Status.BAD_REQUEST) {
            return rejected("Baidu profile endpoint rejected the access token");
        }
        try {
            final JsonValue.ObjectValue object = object(response);
            for (String member : object.values().keySet()) {
                if (!profileMember(member)) {
                    throw new ValidateException("Baidu profile response contains an unregistered member");
                }
            }
            if (hasError(object)) {
                return rejected("Baidu profile endpoint returned an error");
            }
            final Profile profile = Profile.decode(object);
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("openid", new JsonValue.StringValue(profile.openId()), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, profile.openId(), profile.attributes(), List.of(evidence)));
        } catch (RuntimeException cause) {
            return rejected("Baidu profile response is invalid");
        }
    }

    /**
     * Strictly decodes one HTTP 200 JSON object response.
     *
     * @param response response whose body remains open
     * @return bounded provider-neutral JSON object
     * @throws ValidateException if status, media type, JSON shape, depth, or duplicate members are invalid
     */
    private JsonValue.ObjectValue object(final HttpResponse response) {
        if (response.code() != Http.Status.OK
                || !MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Baidu response must use HTTP 200 application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Baidu JSON response root must be an object");
        }
        return object;
    }

    /**
     * Validates and indexes one exact Baidu GET callback.
     *
     * @param callback raw inbound callback
     * @return typed private callback value
     * @throws ValidateException if target, transport, names, branches, or values violate the frozen contract
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Baidu callback must not be null");
        if (callback.method() != Http.Method.GET || !settings.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Baidu callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        String error = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert.notBlank(parameter.value(), "Baidu callback value must not be blank");
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> {
                    if (code != null) {
                        throw new ValidateException("Baidu callback parameter names must be unique");
                    }
                    code = value;
                }
                case OAuth2.Parameters.STATE -> {
                    if (state != null) {
                        throw new ValidateException("Baidu callback parameter names must be unique");
                    }
                    state = value;
                }
                case OAuth2.Parameters.ERROR -> {
                    if (error != null) {
                        throw new ValidateException("Baidu callback parameter names must be unique");
                    }
                    error = value;
                }
                default -> throw new ValidateException("Baidu callback contains an unregistered parameter");
            }
        }
        final boolean success = code != null && error == null;
        final boolean denied = code == null && "access_denied".equals(error);
        if ((!success && !denied) || state == null) {
            throw new ValidateException("Baidu callback must contain one exact success or denial branch");
        }
        return new CallbackWire(code, state, error);
    }

    /**
     * Returns explicit registered scopes or the immutable basic default.
     *
     * @return ordered effective Baidu scopes
     */
    private List<String> requestedScopes() {
        return settings.scopes().isEmpty() ? variantDefinition.defaultScopes() : settings.scopes();
    }

    /**
     * Carries Baidu's private authorization callback without exposing a platform response type publicly.
     *
     * @param code  authorization code on the success branch
     * @param state mandatory browser correlation value
     * @param error access-denied value on the rejection branch
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state, String error) {

        /**
         * Verifies the exact success or access-denied branch.
         *
         * @throws IllegalArgumentException if state is blank
         * @throws ValidateException        if the branch is inconsistent
         */
        private CallbackWire {
            Assert.notBlank(state, "Baidu callback state must not be blank");
            if (!(code != null && error == null) && !(code == null && "access_denied".equals(error))) {
                throw new ValidateException("Baidu callback branch is invalid");
            }
        }

    }

    /**
     * Carries the validated Baidu private profile information used by the external identity boundary.
     *
     * @param openId     stable Baidu subject
     * @param attributes validated optional Baidu profile attributes
     * @author Kimi Liu
     */
    private record Profile(String openId, JsonValue.ObjectValue attributes) {

        /**
         * Validates one decoded private profile value.
         *
         * @throws IllegalArgumentException if a component is absent or the subject is blank
         */
        private Profile {
            Assert.notBlank(openId, "Baidu private profile openid must not be blank");
            attributes = Assert.notNull(attributes, "Baidu private profile attributes must not be null");
        }

        /**
         * Decodes known Baidu profile members into one typed private value.
         *
         * @param object strict private profile object
         * @return typed private profile
         * @throws ValidateException if a registered member has an invalid value
         */
        private static Profile decode(final JsonValue.ObjectValue object) {
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            add(attributes, "unionid", optionalString(object, "unionid"));
            add(attributes, "userid", optionalString(object, "userid"));
            add(attributes, "username", optionalString(object, "username"));
            add(attributes, "userdetail", optionalString(object, "userdetail"));
            add(attributes, "sex", optionalString(object, "sex"));
            add(attributes, "portrait", optionalString(object, "portrait"));
            add(attributes, "birthday", optionalString(object, "birthday"));
            add(attributes, "marriage", optionalString(object, "marriage"));
            add(attributes, "blood", optionalString(object, "blood"));
            add(attributes, "constellation", optionalString(object, "constellation"));
            add(attributes, "education", optionalString(object, "education"));
            add(attributes, "trade", optionalString(object, "trade"));
            add(attributes, "job", optionalString(object, "job"));
            add(attributes, "city", optionalString(object, "city"));
            add(attributes, "province", optionalString(object, "province"));
            add(attributes, "country", optionalString(object, "country"));
            return new Profile(requiredString(object, "openid"), new JsonValue.ObjectValue(attributes));
        }

        /**
         * Adds one present private profile value to the opaque identity attributes.
         *
         * @param attributes mutable map confined to profile decoding
         * @param member     exact Baidu member name
         * @param value      decoded value or {@code null} when absent
         */
        private static void add(final Map<String, JsonValue> attributes, final String member, final String value) {
            if (value != null) {
                attributes.put(member, new JsonValue.StringValue(value));
            }
        }

    }

    /**
     * Carries Baidu's private token success fields required by the immediately following profile request.
     *
     * @param accessToken sensitive access token
     * @param expiresIn   positive access-token lifetime
     * @author Kimi Liu
     */
    private record Access(String accessToken, long expiresIn) {

        /**
         * Validates one private Baidu token success value.
         *
         * @throws IllegalArgumentException if either token is blank
         * @throws ValidateException        if the lifetime is not positive
         */
        private Access {
            Assert.notBlank(accessToken, "Baidu private access token must not be blank");
            if (expiresIn <= 0L) {
                throw new ValidateException("Baidu private access-token lifetime must be positive");
            }
        }

        /**
         * Returns a diagnostic representation without token material.
         *
         * @return redacted private token summary
         */
        @Override
        public String toString() {
            return "Access[accessToken=[REDACTED], expiresIn=" + expiresIn + Symbol.BRACKET_RIGHT;
        }

    }

}
