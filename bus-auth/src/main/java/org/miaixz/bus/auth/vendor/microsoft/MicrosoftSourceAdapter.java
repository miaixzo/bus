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
package org.miaixz.bus.auth.vendor.microsoft;

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
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
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
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements Microsoft browser authentication for global and China cloud registrations.
 * <p>
 * Standard OAuth authorization-code and refresh-token operations are delegated unchanged to the shared OAuth 2.0
 * client. This adapter owns only state correlation and the Bearer-authenticated Microsoft Graph {@code /me} call that
 * maps the verified Graph {@code id} to an {@link ExternalIdentity}.
 * </p>
 *
 * @author Kimi Liu
 */
public final class MicrosoftSourceAdapter implements VendorAdapter {

    /**
     * Trusted global Microsoft Graph evidence authority.
     */
    private static final String GLOBAL_AUTHORITY = "https://graph.microsoft.com";

    /**
     * Trusted Microsoft Graph China evidence authority.
     */
    private static final String CHINA_AUTHORITY = "https://microsoftgraph.chinacloudapi.cn";

    /**
     * Maximum accepted Microsoft Graph JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum accepted Microsoft Graph JSON nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Microsoft cloud manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Microsoft options.
     */
    private final MicrosoftOptions options;

    /**
     * Caller-owned runtime, JSON, network, and execution dependencies.
     */
    private final ExecutionServices services;

    /**
     * Shared standard OAuth authorization and token implementation.
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
     * Creates one Source-bound Microsoft cloud adapter.
     *
     * @param namespaceId registration namespace used to isolate state and credential resolution
     * @param sourceId    registered Source identifier
     * @param manifest    selected Microsoft platform manifest
     * @param variant     exact selected global or China manifest
     * @param options     decoded externally loaded Microsoft options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, variant, protocol, options, or required standard operations disagree
     */
    public MicrosoftSourceAdapter(final String namespaceId, final String sourceId, final MicrosoftManifest manifest,
            final VariantManifest.Variant variant, final MicrosoftOptions options, final ExecutionServices services) {
        final MicrosoftManifest selected = Assert.notNull(manifest, "Microsoft manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Microsoft Source id must not be blank");
        this.variant = Assert.notNull(variant, "Microsoft manifest must not be null");
        this.options = Assert.notNull(options, "Microsoft options must not be null");
        this.services = Assert.notNull(services, "Microsoft execution services must not be null");
        if (!MicrosoftManifest.ID.equals(selected.vendor()) || !selected.variant(options.variant()).equals(variant)
                || !variant.variant().equals(options.variant())
                || !MicrosoftManifest.GLOBAL.equals(variant.variant())
                        && !MicrosoftManifest.CHINA.equals(variant.variant())
                || variant.protocol() != Protocol.OAUTH2 || !MicrosoftManifest.ID.equals(options.vendor())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("Microsoft adapter requires a matching global or China OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        final var targets = variant.targets().resolve(options);
        final OAuth2ClientOptions oauthSettings = new OAuth2ClientOptions(targets.authorization(), targets.token(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                options.clientId(), Set.of(options.redirectUri().getOrNull()),
                Endpoint.Authentication.CLIENT_SECRET_POST, Optional.of(options.credential()), false, false);
        final OAuth2Client oauthClient = new OAuth2Client(
                new AuthorizationClient(oauthSettings,
                        new AuthorizationRequestEncoder(targets.authorization().getOrNull())),
                new TokenClient(oauthSettings, services, new TokenRequestEncoder(),
                        new TokenResponseDecoder(services.jsonProvider())),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION, oauthClient::authorize),
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.TOKEN, oauthClient::token)));
        this.authorizationResponseDecoder = new AuthorizationResponseDecoder();
    }

    /**
     * Converts one standard authorization error to a safe rejected outcome.
     *
     * @param error decoded standard OAuth error branch
     * @param <T>   expected success type
     * @return rejected outcome retaining only registered non-sensitive identifiers
     */
    private static <T> Outcome<T> oauthError(final AuthorizationResponseDecoder.Error error) {
        final Map<String, JsonValue> details = new LinkedHashMap<>();
        details.put("oauth_error", new JsonValue.StringValue(error.response().error().value()));
        final String errorUri = error.response().errorUri().getOrNull();
        if (errorUri != null) {
            details.put(OAuth2.Parameters.ERROR_URI, new JsonValue.StringValue(errorUri));
        }
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400, "Microsoft authorization endpoint returned a standard error",
                        new JsonValue.ObjectValue(details)));
    }

    /**
     * Reads one required non-blank Microsoft Graph string member.
     *
     * @param object decoded Graph object
     * @param name   exact member name
     * @return non-blank member value
     * @throws ValidateException if the member is absent, blank, or not a string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Microsoft Graph required current-user member is invalid");
        }
        return string.value();
    }

    /**
     * Reads one optional non-blank Microsoft Graph string member.
     *
     * @param object decoded Graph object
     * @param name   exact property name
     * @return property value or {@code null} when absent or JSON null
     * @throws ValidateException if a present value is blank or not a string
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Microsoft Graph optional current-user member is invalid");
        }
        return string.value();
    }

    /**
     * Verifies the only Microsoft callback extension consumed by this Source.
     *
     * @param extensions decoded standard authorization extensions
     * @return whether every extension is the documented session state
     */
    private static boolean callbackExtensions(final JsonValue.ObjectValue extensions) {
        for (String member : extensions.values().keySet()) {
            if (!"session_state".equals(member)) {
                return false;
            }
        }
        return true;
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
        return completed(rejected("Microsoft capability is not declared"));
    }

    /**
     * Creates a completed request-contract rejection.
     *
     * @param <S> expected successful response type
     * @return completed rejection stage
     */
    private static <S> CompletionStage<Outcome<S>> mismatch() {
        return completed(rejected("Microsoft capability request is invalid"));
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
     * Returns the exact capability manifest frozen by the selected Microsoft manifest.
     *
     * @return immutable Source authentication, authorization, and token manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and the two standard Microsoft OAuth operations.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Microsoft-private Graph response objects
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Microsoft capability must not be null");
        Assert.notNull(context, "Microsoft invocation context must not be null");
        Assert.notNull(timeout, "Microsoft invocation budget must not be null");
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
            return narrow(token(tokenRequest, context, timeout), capability.responseType());
        }
        return mismatch();
    }

    /**
     * Builds the standard Microsoft authorization-code redirect from generated state.
     *
     * @param initiation generated state with no nonce or PKCE material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return standard-client-produced redirect bound to the generated state
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        try {
            if (initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
                return completed(
                        failed(ErrorCode._500, "Microsoft OAuth flow generated unregistered nonce or PKCE material"));
            }
            final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                    options.redirectUri(), Optional.of(new Scope(requestedScopes())), Optional.of(initiation.state()),
                    Optional.empty(), Optional.empty(), emptyObject());
            return standardAdapter.invoke(OAuth2ClientScheme.AUTHORIZATION, request, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<org.miaixz.bus.fabric.UnoUrl> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<org.miaixz.bus.fabric.UnoUrl> rejected -> Outcome
                                .rejected(rejected.failure());
                        case Outcome.Failed<org.miaixz.bus.fabric.UnoUrl> failed -> Outcome.failed(failed.failure());
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Microsoft authorization request is invalid"));
        }
    }

    /**
     * Extracts the required state from exactly one standard Microsoft callback branch.
     *
     * @param callback raw inbound callback captured by the external web project
     * @return unique non-blank state value
     * @throws ValidateException if target, method, branch, multiplicity, or state is invalid
     */
    private String state(final Callback.Inbound callback) {
        return switch (callback(callback)) {
            case AuthorizationResponseDecoder.Success success -> success.response().state()
                    .orElseThrow(() -> new ValidateException("Microsoft authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Microsoft authorization error requires state"));
        };
    }

    /**
     * Redeems one correlated code and retrieves the corresponding Microsoft Graph identity.
     *
     * @param completion consumed callback correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return verified Microsoft Graph identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Microsoft authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(oauthError(error));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        if (response.issuer().isPresent() || !callbackExtensions(response.extensions())
                || completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(rejected("Microsoft authorization response contains unregistered flow material"));
        }
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
     * Executes only Microsoft authorization-code and refresh-token grants through the standard client.
     *
     * @param request standard token request
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return standard token response with Microsoft Bearer type validation
     */
    private CompletionStage<Outcome<TokenResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        if (!request.extensions().values().isEmpty() || !valid(request.grant())) {
            return completed(rejected("Microsoft token request does not match its registered grant contract"));
        }
        return standardAdapter.invoke(OAuth2ClientScheme.TOKEN, request, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<TokenEndpointResponse> success -> success
                            .value() instanceof TokenResponse token && TokenType.BEARER.equals(token.tokenType())
                                    ? Outcome.succeeded(token)
                                    : MicrosoftSourceAdapter.<TokenResponse>rejected(
                                            "Microsoft token endpoint must return a Bearer OAuth token response");
                    case Outcome.Rejected<TokenEndpointResponse> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<TokenEndpointResponse> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Retrieves the Microsoft Graph current-user resource asynchronously.
     *
     * @param token   standard OAuth token response containing the sensitive access token
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> profile(
            final TokenResponse token,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> profile(token.accessToken(), timeout), services.executor());
    }

    /**
     * Executes and strictly maps one Microsoft Graph {@code /me} response.
     *
     * @param accessToken sensitive Bearer access token
     * @param timeout     shared end-to-end budget
     * @return verified external identity or safely classified upstream outcome
     */
    private Outcome<ExternalIdentity> profile(final String accessToken, final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Microsoft Graph request has no remaining time budget");
        }
        try {
            final String endpoint = variant.targets().resolve(options).userInfo().getOrNull().url().toString();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + accessToken)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
                    return failed(ErrorCode._429, "Microsoft Graph rate limited the current-user request");
                }
                if (response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
                    return failed(ErrorCode._502, "Microsoft Graph is unavailable");
                }
                if (response.code() == Http.Status.UNAUTHORIZED || response.code() == Http.Status.FORBIDDEN
                        || response.code() == Http.Status.BAD_REQUEST) {
                    return rejected("Microsoft Graph rejected the access token or delegated permission");
                }
                if (response.code() != Http.Status.OK
                        || !MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
                    return failed(ErrorCode._502, "Microsoft Graph returned an invalid current-user response");
                }
                final JsonValue parsed = services.jsonProvider()
                        .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
                if (!(parsed instanceof JsonValue.ObjectValue object)) {
                    return failed(ErrorCode._502, "Microsoft Graph current-user response must be a JSON object");
                }
                return identity(object, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Microsoft Graph current-user request failed");
        }
    }

    /**
     * Maps the stable Graph identifier and validated optional profile strings.
     *
     * @param profile decoded Microsoft Graph current-user object
     * @param timeout shared clock used to timestamp evidence
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> identity(final JsonValue.ObjectValue profile, final Timeout.Budget timeout) {
        try {
            if (profile.values().containsKey(OAuth2.Parameters.ERROR)) {
                return failed(ErrorCode._502, "Microsoft Graph returned an unexpected error envelope");
            }
            final GraphProfile currentUser = GraphProfile.decode(profile);
            final String subject = currentUser.id();
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("microsoft_graph_id", new JsonValue.StringValue(subject), authority(),
                            timeout.clock().now()));
            return Outcome
                    .succeeded(new ExternalIdentity(sourceId, subject, currentUser.attributes(), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Microsoft Graph current-user response is invalid");
        }
    }

    /**
     * Validates a public standard authorization request against this Microsoft registration.
     *
     * @param request standard OAuth authorization request
     * @return {@code true} when client, callback, scope, state, response type, and extensions are exact
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request.scope().getOrNull();
        return ResponseType.CODE.equals(request.responseType()) && options.clientId().equals(request.clientId())
                && options.redirectUri().equals(request.redirectUri()) && scope != null
                && requestedScopes().equals(scope.values()) && request.state().isPresent()
                && request.codeChallenge().isEmpty() && request.codeChallengeMethod().isEmpty()
                && request.extensions().values().isEmpty();
    }

    /**
     * Validates one standard token grant against the Microsoft web registration.
     *
     * @param grant standard OAuth token grant
     * @return {@code true} for a bound authorization code or non-expanding refresh-token grant
     */
    private boolean valid(final TokenRequest.Grant grant) {
        if (grant instanceof AuthorizationCodeGrant authorization) {
            final String clientId = authorization.clientId().getOrNull();
            return options.redirectUri().equals(authorization.redirectUri())
                    && (clientId == null || options.clientId().equals(clientId))
                    && authorization.codeVerifier().isEmpty();
        }
        if (grant instanceof RefreshTokenGrant refresh) {
            final Scope scope = refresh.scope().getOrNull();
            return scope == null || requestedScopes().containsAll(scope.values());
        }
        return false;
    }

    /**
     * Validates the exact registered callback target before standard response decoding.
     *
     * @param callback raw inbound callback
     * @return discriminated standard OAuth response
     * @throws ValidateException if the callback target differs from the registered URI
     */
    private AuthorizationResponseDecoder.Decoded callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Microsoft callback must not be null");
        if (!options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Microsoft callback URI does not match the registered redirect URI");
        }
        return authorizationResponseDecoder.decode(callback);
    }

    /**
     * Returns the trusted Graph authority matching the selected cloud variant.
     *
     * @return immutable global or China Graph authority
     */
    private String authority() {
        return MicrosoftManifest.CHINA.equals(variant.variant()) ? CHINA_AUTHORITY : GLOBAL_AUTHORITY;
    }

    /**
     * Returns explicit registered scopes or immutable manifest defaults.
     *
     * @return ordered effective Microsoft scopes
     */
    private List<String> requestedScopes() {
        return options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
    }

    /**
     * Carries the typed subset of Microsoft Graph current-user data used by identity mapping.
     *
     * @param id                stable Graph user identifier
     * @param userPrincipalName optional principal name
     * @param displayName       optional display name
     * @param officeLocation    optional office location
     * @param mail              optional mail address
     * @param givenName         optional given name
     * @param surname           optional surname
     * @param preferredLanguage optional preferred language
     * @param jobTitle          optional job title
     * @param mobilePhone       optional mobile telephone number
     * @author Kimi Liu
     */
    private record GraphProfile(String id, String userPrincipalName, String displayName, String officeLocation,
            String mail, String givenName, String surname, String preferredLanguage, String jobTitle,
            String mobilePhone) {

        /**
         * Decodes the exact typed Graph projection from one validated JSON object.
         *
         * @param object decoded current-user object
         * @return typed current-user projection
         */
        private static GraphProfile decode(final JsonValue.ObjectValue object) {
            return new GraphProfile(requiredString(object, "id"), optionalString(object, "userPrincipalName"),
                    optionalString(object, "displayName"), optionalString(object, "officeLocation"),
                    optionalString(object, "mail"), optionalString(object, "givenName"),
                    optionalString(object, "surname"), optionalString(object, "preferredLanguage"),
                    optionalString(object, "jobTitle"), optionalString(object, "mobilePhone"));
        }

        /**
         * Adds one present typed profile value to the external attribute object.
         *
         * @param target mutable attribute map
         * @param name   Graph property name
         * @param value  optional typed value
         */
        private static void put(final Map<String, JsonValue> target, final String name, final String value) {
            if (value != null) {
                target.put(name, new JsonValue.StringValue(value));
            }
        }

        /**
         * Converts optional typed profile values into immutable external identity attributes.
         *
         * @return provider-neutral immutable attribute object
         */
        private JsonValue.ObjectValue attributes() {
            final Map<String, JsonValue> values = new LinkedHashMap<>();
            put(values, "userPrincipalName", userPrincipalName);
            put(values, "displayName", displayName);
            put(values, "officeLocation", officeLocation);
            put(values, "mail", mail);
            put(values, "givenName", givenName);
            put(values, "surname", surname);
            put(values, "preferredLanguage", preferredLanguage);
            put(values, "jobTitle", jobTitle);
            put(values, "mobilePhone", mobilePhone);
            return new JsonValue.ObjectValue(values);
        }

    }

}
