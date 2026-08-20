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
package org.miaixz.bus.auth.vendor.proginn.internal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.proginn.ProginnDefinition;
import org.miaixz.bus.auth.vendor.proginn.ProginnSourceSettings;
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
 * Implements Proginn browser authentication while preserving standard OAuth 2.0 public operations.
 * <p>
 * Authorization and token operations delegate unchanged to the shared OAuth client. The historical query-token profile
 * call remains private and produces only a verified external identity keyed by Proginn's {@code uid}.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ProginnSourceAdapter implements VendorAdapter {

    /**
     * Trusted Proginn authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://www.proginn.com";

    /**
     * Maximum bounded JSON response size accepted from Proginn.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum JSON nesting accepted from Proginn.
     */
    private static final int MAXIMUM_JSON_DEPTH = 16;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Proginn definition.
     */
    private final VendorDefinition.Definition variantDefinition;

    /**
     * Validated externally loaded Proginn settings.
     */
    private final ProginnSourceSettings settings;

    /**
     * Caller-owned JSON, network, clock, and execution dependencies.
     */
    private final ExecutionServices services;

    /**
     * Shared standard OAuth authorization and token implementation.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time browser state lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Strict standard authorization response decoder.
     */
    private final AuthorizationResponseDecoder callbackDecoder;

    /**
     * Creates one Source-bound Proginn adapter from the frozen default definition.
     *
     * @param namespaceId       registration namespace isolating state and credentials
     * @param sourceId          registered Source identifier
     * @param vendorDefinition  selected Proginn definition
     * @param variantDefinition selected default variant definition
     * @param settings          decoded externally loaded Proginn settings
     * @param services          caller-owned runtime dependencies
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, definition, settings, or routing differ from the frozen variant
     */
    public ProginnSourceAdapter(final String namespaceId, final String sourceId,
            final ProginnDefinition vendorDefinition, final VendorDefinition.Definition variantDefinition,
            final ProginnSourceSettings settings, final ExecutionServices services) {
        final ProginnDefinition selected = Assert.notNull(vendorDefinition, "Proginn definition must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Proginn Source id must not be blank");
        this.variantDefinition = Assert.notNull(variantDefinition, "Proginn definition must not be null");
        this.settings = Assert.notNull(settings, "Proginn settings must not be null");
        this.services = Assert.notNull(services, "Proginn execution services must not be null");
        if (!ProginnDefinition.ID.equals(selected.type())
                || !selected.variant(ProginnDefinition.DEFAULT).equals(variantDefinition)
                || !ProginnDefinition.DEFAULT.equals(variantDefinition.variant())
                || variantDefinition.protocol() != Protocol.OAUTH2 || !ProginnDefinition.ID.equals(settings.vendor())
                || !ProginnDefinition.DEFAULT.equals(settings.variant())) {
            throw new ValidateException("Proginn adapter requires the proginn/default OAuth 2.0 definition");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variantDefinition, settings, services);
        this.standardAdapter = standardAdapter(variantDefinition, settings, services, redirectManager);
        this.callbackDecoder = new AuthorizationResponseDecoder();
    }

    /**
     * Composes Proginn's wire-compatible OAuth operations from protocol-owned clients and codecs.
     *
     * @param definition      selected Proginn definition
     * @param settings        validated Source deployment settings
     * @param services        caller-owned execution services
     * @param redirectManager shared browser correlation lifecycle
     * @return adapter containing standard authorization and token bindings
     */
    private static StandardAdapter standardAdapter(
            final VendorDefinition.Definition definition,
            final ProginnSourceSettings settings,
            final ExecutionServices services,
            final RedirectManager redirectManager) {
        final var targets = definition.targets().resolve(settings);
        final OAuth2ClientSettings oauthSettings = new OAuth2ClientSettings(targets.authorization(), targets.token(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                settings.clientId(), java.util.Set.of(settings.redirectUri().getOrNull()),
                Endpoint.Authentication.CLIENT_SECRET_POST, Optional.of(settings.credential()), false, false);
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
     * Verifies that an OAuth error object contains only the standard members emitted by Proginn.
     *
     * @param object decoded error object
     * @return whether every member has standard OAuth error semantics
     */
    private static boolean errorMembers(final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            if (!switch (name) {
                case OAuth2.Parameters.ERROR, OAuth2.Parameters.ERROR_DESCRIPTION -> true;
                default -> false;
            }) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies that a profile object contains only the preserved Proginn projection members.
     *
     * @param object decoded profile object
     * @return whether every member belongs to the profile projection
     */
    private static boolean profileMembers(final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            if (!switch (name) {
                case "uid", "nickname", "avatar", "email" -> true;
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
        if (value == null || value.isBlank()) {
            throw new ValidateException("Proginn response requires a non-blank string member: " + name);
        }
        return value;
    }

    /**
     * Reads one optional JSON string member.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return string value or {@code null} when absent
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Proginn response member must be a JSON string: " + name);
        }
        return string.value();
    }

    /**
     * Maps a standard authorization error to a safe rejected outcome.
     *
     * @param error decoded standard authorization error branch
     * @param <T>   expected success type
     * @return rejected outcome retaining only the standard error identifier
     */
    private static <T> Outcome<T> oauthError(final AuthorizationResponseDecoder.Error error) {
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400, "Proginn authorization endpoint returned a standard error",
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
     * Returns the exact frozen Proginn capability manifest.
     *
     * @return immutable Source authentication and OAuth capability set
     */
    @Override
    public Capability.Manifest manifest() {
        return variantDefinition.manifest();
    }

    /**
     * Routes Source authentication and the registered standard OAuth operations.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing private Proginn models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Proginn capability must not be null");
        Assert.notNull(context, "Proginn invocation context must not be null");
        Assert.notNull(timeout, "Proginn invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Proginn capability is not declared"));
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
        return completed(rejected("Proginn capability request is invalid"));
    }

    /**
     * Builds the standard authorization request around generated one-time state.
     *
     * @param initiation generated browser correlation material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @return shared-client-produced authorization redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        if (initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "Proginn browser material violates the frozen definition"));
        }
        final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, settings.clientId(),
                settings.redirectUri(), Optional.of(new Scope(effectiveScopes())), Optional.of(initiation.state()),
                Optional.empty(), Optional.empty(), emptyObject());
        return standardAdapter.invoke(OAuth2SourceProfile.AUTHORIZATION, request, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<org.miaixz.bus.fabric.UnoUrl> success -> Outcome
                            .succeeded(new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                    case Outcome.Rejected<org.miaixz.bus.fabric.UnoUrl> rejected -> Outcome
                            .rejected(rejected.failure());
                    case Outcome.Failed<org.miaixz.bus.fabric.UnoUrl> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Extracts the required state from one strict standard callback branch.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return required correlation state
     */
    private String state(final Callback.Inbound callback) {
        return switch (decode(callback)) {
            case AuthorizationResponseDecoder.Success success -> success.response().state()
                    .orElseThrow(() -> new ValidateException("Proginn authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Proginn authorization error requires state"));
        };
    }

    /**
     * Completes one correlated authorization code and retrieves the Proginn profile.
     *
     * @param completion consumed callback correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @return verified Proginn external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = decode(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Proginn authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(oauthError(error));
        }
        if (completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(failed(ErrorCode._500, "Proginn callback contains unexpected browser material"));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        final TokenRequest request = new TokenRequest(
                new AuthorizationCodeGrant(response.code(), settings.redirectUri(), Optional.empty(), Optional.empty()),
                emptyObject());
        return standardAdapter.invoke(OAuth2SourceProfile.TOKEN, request, context, timeout)
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<TokenEndpointResponse> success -> success
                            .value() instanceof TokenResponse token ? profile(token, timeout)
                                    : completed(rejected("Proginn token endpoint returned a non-OAuth token response"));
                    case Outcome.Rejected<TokenEndpointResponse> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<TokenEndpointResponse> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Retrieves and maps the private query-token Proginn profile.
     *
     * @param token   standard token response carrying sensitive bearer material
     * @param timeout shared end-to-end time budget
     * @return verified Proginn external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> profile(
            final TokenResponse token,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            if (!TokenType.BEARER.equals(token.tokenType())) {
                return rejected("Proginn token response must use the Bearer token type");
            }
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Proginn profile request has no remaining time budget");
            }
            try {
                final String endpoint = variantDefinition.targets().resolve(settings).userInfo().getOrNull().url()
                        .toString();
                try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                        .query(OAuth2.Parameters.ACCESS_TOKEN, token.accessToken())
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                        .execute()) {
                    return profile(response, timeout);
                }
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Proginn profile request failed");
            }
        }, services.executor());
    }

    /**
     * Strictly maps one Proginn profile or standard error response.
     *
     * @param response owned profile endpoint response
     * @param timeout  shared clock used for evidence timestamping
     * @return verified external identity or safely classified failure
     */
    private Outcome<ExternalIdentity> profile(final HttpResponse response, final Timeout.Budget timeout) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (object.values().containsKey(OAuth2.Parameters.ERROR)) {
                if (!errorMembers(object)) {
                    throw new ValidateException("Proginn error response members are invalid");
                }
                requiredString(object, OAuth2.Parameters.ERROR);
                optionalString(object, OAuth2.Parameters.ERROR_DESCRIPTION);
                return response.code() >= Http.Status.INTERNAL_SERVER_ERROR
                        ? failed(ErrorCode._502, "Proginn profile endpoint returned an upstream error")
                        : rejected("Proginn profile endpoint rejected the access token");
            }
            if (response.code() != Http.Status.OK || !profileMembers(object)) {
                throw new ValidateException("Proginn profile success shape is invalid");
            }
            final ProfileWire profile = ProfileWire.decode(object);
            final String subject = profile.uid();
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            profile.copyAttributes(attributes);
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("uid", new JsonValue.StringValue(subject), AUTHORITY, timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Proginn profile endpoint returned an invalid response");
        }
    }

    /**
     * Validates one public standard authorization request against the Proginn registration.
     *
     * @param request standard OAuth authorization request
     * @return whether every standard field matches the registered Source
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request.scope().getOrNull();
        return ResponseType.CODE.equals(request.responseType()) && settings.clientId().equals(request.clientId())
                && settings.redirectUri().equals(request.redirectUri()) && scope != null
                && effectiveScopes().equals(scope.values()) && request.state().isPresent()
                && request.codeChallenge().isEmpty() && request.codeChallengeMethod().isEmpty()
                && request.extensions().values().isEmpty();
    }

    /**
     * Validates one standard token request against the Proginn registration.
     *
     * @param request standard OAuth token request
     * @return whether the request is a bound authorization-code grant without PKCE or extensions
     */
    private boolean valid(final TokenRequest request) {
        if (request == null || !request.extensions().values().isEmpty()
                || !(request.grant() instanceof AuthorizationCodeGrant grant)) {
            return false;
        }
        final String clientId = grant.clientId().getOrNull();
        return settings.redirectUri().equals(grant.redirectUri())
                && (clientId == null || settings.clientId().equals(clientId)) && grant.codeVerifier().isEmpty();
    }

    /**
     * Validates callback ownership before strict standard response decoding.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     */
    private AuthorizationResponseDecoder.Decoded decode(final Callback.Inbound callback) {
        final Callback.Inbound inbound = Assert.notNull(callback, "Proginn callback must not be null");
        if (inbound.method() != Http.Method.GET || !settings.redirectUri().getOrNull().equals(inbound.requestUri())) {
            throw new ValidateException("Proginn callback URI or method is invalid");
        }
        return callbackDecoder.decode(inbound);
    }

    /**
     * Strictly reads one bounded Proginn JSON object.
     *
     * @param response response whose body remains owned by the caller
     * @return immutable provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final HttpResponse response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Proginn response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Proginn response root must be a JSON object");
        }
        return object;
    }

    /**
     * Returns explicit scopes or the immutable definition default.
     *
     * @return ordered effective Proginn scopes
     */
    private List<String> effectiveScopes() {
        return settings.scopes().isEmpty() ? variantDefinition.defaultScopes() : settings.scopes();
    }

    /**
     * Carries the exact private Proginn profile projection without creating a public protocol model.
     *
     * @param uid      stable Proginn account identifier
     * @param nickname optional display name
     * @param avatar   optional avatar URL
     * @param email    optional email address
     */
    private record ProfileWire(String uid, String nickname, String avatar, String email) {

        /**
         * Decodes one already member-validated Proginn profile object.
         *
         * @param object private profile response object
         * @return immutable typed projection
         */
        private static ProfileWire decode(final JsonValue.ObjectValue object) {
            return new ProfileWire(requiredString(object, "uid"), optionalString(object, "nickname"),
                    optionalString(object, "avatar"), optionalString(object, "email"));
        }

        /**
         * Copies one optional string attribute into the identity projection.
         *
         * @param attributes mutable destination owned by the identity mapper
         * @param name       exact Proginn wire member name
         * @param value      optional decoded value
         */
        private static void put(final Map<String, JsonValue> attributes, final String name, final String value) {
            if (value != null) {
                attributes.put(name, new JsonValue.StringValue(value));
            }
        }

        /**
         * Copies present profile attributes using their exact Proginn wire names.
         *
         * @param attributes mutable destination owned by the identity mapper
         */
        private void copyAttributes(final Map<String, JsonValue> attributes) {
            put(attributes, "nickname", nickname);
            put(attributes, "avatar", avatar);
            put(attributes, "email", email);
        }

    }

}
