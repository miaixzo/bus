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
package org.miaixz.bus.auth.vendor.twitter.internal;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.oauth1.*;
import org.miaixz.bus.auth.protocol.oauth1.client.OAuth1Client;
import org.miaixz.bus.auth.protocol.oauth1.client.OAuth1ClientSettings;
import org.miaixz.bus.auth.protocol.oauth1.client.OAuth1SourceProfile;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.twitter.TwitterDefinition;
import org.miaixz.bus.auth.vendor.twitter.TwitterSourceSettings;
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
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements Twitter OAuth 1.0a Source authentication by composing the four RFC 5849 client operations.
 * <p>
 * Temporary credentials, resource-owner authorization, token credentials, signing, and credential-secret storage are
 * delegated unchanged to the shared OAuth 1.0 client. This adapter owns only browser correlation, Twitter's two profile
 * query parameters, strict Twitter User JSON projection, and creation of a verified external identity.
 * </p>
 *
 * @author Kimi Liu
 */
public final class TwitterSourceAdapter implements VendorAdapter {

    /**
     * Trusted Twitter API authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://api.twitter.com";

    /**
     * Twitter callback parameter used when the resource owner denies authorization.
     */
    private static final String DENIAL_PARAMETER = "denied";

    /**
     * Maximum accepted Twitter User JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum accepted Twitter User JSON nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Twitter OAuth 1.0a definition.
     */
    private final VendorDefinition.Definition variantDefinition;

    /**
     * Validated externally loaded Twitter settings.
     */
    private final TwitterSourceSettings settings;

    /**
     * Caller-owned runtime, clock, JSON, and external store dependencies.
     */
    private final ExecutionServices services;

    /**
     * Shared implementation of all public RFC 5849 operations.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time browser correlation and replay-protection lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Creates one Source-bound Twitter OAuth 1.0a adapter.
     *
     * @param namespaceId       registration namespace used to isolate browser and credential state
     * @param sourceId          registered Source identifier
     * @param vendorDefinition  selected Twitter Source definition
     * @param variantDefinition exact selected OAuth 1.0a definition
     * @param settings          decoded externally loaded Twitter settings
     * @param services          caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, definition, or callback settings are inconsistent
     */
    public TwitterSourceAdapter(final String namespaceId, final String sourceId,
            final TwitterDefinition vendorDefinition, final VendorDefinition.Definition variantDefinition,
            final TwitterSourceSettings settings, final ExecutionServices services) {
        final TwitterDefinition selected = Assert.notNull(vendorDefinition, "Twitter definition must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Twitter Source id must not be blank");
        this.variantDefinition = Assert.notNull(variantDefinition, "Twitter definition must not be null");
        this.settings = Assert.notNull(settings, "Twitter settings must not be null");
        this.services = Assert.notNull(services, "Twitter execution services must not be null");
        if (!TwitterDefinition.ID.equals(selected.type())
                || !selected.variant(TwitterDefinition.OAUTH1).equals(variantDefinition)
                || !TwitterDefinition.OAUTH1.equals(variantDefinition.variant())
                || variantDefinition.protocol() != Protocol.OAUTH1 || !TwitterDefinition.ID.equals(settings.vendor())
                || !TwitterDefinition.OAUTH1.equals(settings.variant()) || settings.redirectUri().isEmpty()) {
            throw new ValidateException("Twitter adapter requires the twitter/oauth1 definition");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variantDefinition, settings, services);
        final var targets = variantDefinition.targets().resolve(settings);
        final OAuth1ClientSettings oauthSettings = new OAuth1ClientSettings(targets.temporaryCredentials().getOrNull(),
                targets.authorization().getOrNull(), targets.token().getOrNull(), settings.clientId(),
                settings.credential(), SignatureMethod.HMAC_SHA1, Duration.ofMinutes(10L), Optional.empty());
        final OAuth1Client oauthClient = new OAuth1Client(namespaceId, sourceId, oauthSettings, services);
        this.standardAdapter = new StandardAdapter(variantDefinition, settings, Optional.of(redirectManager), List.of(
                new StandardAdapter.Binding<>(OAuth1SourceProfile.TEMPORARY_CREDENTIALS,
                        oauthClient::temporaryCredentials),
                new StandardAdapter.Binding<>(OAuth1SourceProfile.RESOURCE_OWNER_AUTHORIZATION, oauthClient::authorize),
                new StandardAdapter.Binding<>(OAuth1SourceProfile.TOKEN_CREDENTIALS, oauthClient::tokenCredentials),
                new StandardAdapter.Binding<>(OAuth1SourceProfile.PROTECTED_RESOURCE, oauthClient::access)));
    }

    /**
     * Reads one required non-blank Twitter JSON string member.
     *
     * @param object decoded Twitter object
     * @param name   exact member name
     * @return non-blank decoded string
     * @throws ValidateException if the member is absent, has another type, or is blank
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue text) || text.value().isBlank()) {
            throw new ValidateException("Twitter current-user response requires a non-blank string member");
        }
        return text.value();
    }

    /**
     * Reads one optional Twitter JSON string member while rejecting all other JSON types.
     *
     * @param object decoded Twitter object
     * @param name   exact member name
     * @return decoded string, or {@code null} when the member is absent or explicitly null
     * @throws ValidateException if a present member has another JSON type
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException("Twitter current-user profile member must be a string or null");
        }
        return text.value();
    }

    /**
     * Verifies the decimal lexical form of Twitter's stable {@code id_str} field without numeric narrowing.
     *
     * @param value exact Twitter user identifier
     * @throws ValidateException if the identifier is zero, signed, padded, or non-decimal
     */
    private static void validateIdentifier(final String value) {
        if (value.charAt(0) < Symbol.C_ONE || value.charAt(0) > Symbol.C_NINE) {
            throw new ValidateException("Twitter id_str must be a positive unpadded decimal identifier");
        }
        for (int index = 1; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < Symbol.C_ZERO || character > Symbol.C_NINE) {
                throw new ValidateException("Twitter id_str must be a positive unpadded decimal identifier");
            }
        }
    }

    /**
     * Classifies a Twitter protected-resource status without copying its response body.
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
     * Creates one exact JSON integer for safe failure details.
     *
     * @param value integral value
     * @return provider-neutral JSON number
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
     * Creates one expected request or resource-owner rejection.
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
     * Creates one operational failure containing only explicitly safe details.
     *
     * @param error       shared Bus error code
     * @param description non-sensitive failure description
     * @param details     non-sensitive provider-neutral details
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
     * Accepts one callback value only once.
     *
     * @param current  previously decoded value
     * @param incoming newly decoded value
     * @return incoming value when no value was previously decoded
     * @throws ValidateException if the parameter occurred more than once
     */
    private static String unique(final String current, final String incoming) {
        if (current != null) {
            throw new ValidateException("Twitter callback parameter names must be unique");
        }
        return incoming;
    }

    /**
     * Returns Source authentication and the four standard RFC 5849 capabilities.
     *
     * @return immutable Twitter capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variantDefinition.manifest();
    }

    /**
     * Routes Source authentication locally and delegates public OAuth 1.0 operations unchanged.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific Source or RFC 5849 request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Twitter-private profile models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Twitter capability must not be null");
        Assert.notNull(context, "Twitter invocation context must not be null");
        Assert.notNull(timeout, "Twitter invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Twitter capability is not declared by the selected definition"));
        }
        if (capability.key().equals(SourceAuthentication.INITIATE.key())
                && request instanceof SourceAuthenticationRequest.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (capability.key().equals(SourceAuthentication.COMPLETE.key())
                && request instanceof SourceAuthenticationRequest.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::correlation, this::identity, context, timeout),
                    capability.responseType());
        }
        if (standardAdapter.manifest().capabilities().contains(capability)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return completed(rejected("Twitter request does not match the selected capability contract"));
    }

    /**
     * Obtains temporary credentials and creates the RFC 5849 resource-owner authorization redirect.
     *
     * @param initiation generated browser material; OAuth 1.0 does not send its generated state
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @return redirect correlated by the returned temporary credential identifier
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(initiation, "Twitter browser initiation must not be null");
        if (initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "Twitter OAuth 1.0 flow received non-OAuth1 security material"));
        }
        final TemporaryCredentialsRequest request = new TemporaryCredentialsRequest(
                List.of(new OAuth1Parameter(OAuth1.Parameters.CALLBACK, settings.redirectUri().getOrNull())));
        return standardAdapter.invoke(OAuth1SourceProfile.TEMPORARY_CREDENTIALS, request, context, timeout)
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<TemporaryCredentialsResponse> success -> authorize(
                            success.value(),
                            context,
                            timeout);
                    case Outcome.Rejected<TemporaryCredentialsResponse> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<TemporaryCredentialsResponse> failed -> completed(
                            Outcome.failed(failed.failure()));
                });
    }

    /**
     * Creates the user-agent redirect for one securely stored temporary credential.
     *
     * @param temporary standard temporary credentials response
     * @param context   immutable invocation context
     * @param timeout   shared end-to-end time budget
     * @return redirect and temporary-credential correlation value
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> authorize(
            final TemporaryCredentialsResponse temporary,
            final Context context,
            final Timeout.Budget timeout) {
        final ResourceOwnerAuthorizationRequest request = new ResourceOwnerAuthorizationRequest(temporary.oauthToken(),
                List.of());
        return standardAdapter.invoke(OAuth1SourceProfile.RESOURCE_OWNER_AUTHORIZATION, request, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<UnoUrl> success -> Outcome.succeeded(
                            new RedirectManager.Prepared(success.value().toString(), temporary.oauthToken()));
                    case Outcome.Rejected<UnoUrl> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<UnoUrl> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Extracts the temporary credential identifier from an exact Twitter success or denial callback.
     *
     * @param callback raw inbound Twitter callback
     * @return unique non-blank temporary credential identifier
     */
    private String correlation(final Callback.Inbound callback) {
        return callback(callback).correlation();
    }

    /**
     * Exchanges an authorized temporary credential and retrieves the signed Twitter User resource.
     *
     * @param completion atomically consumed browser correlation and raw callback
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @return verified Twitter identity or a closed rejection/failure
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Twitter resource-owner callback is invalid"));
        }
        if (values.denied()) {
            return completed(rejected("Twitter resource owner denied authorization"));
        }
        if (!values.token().equals(completion.correlation().state())) {
            return completed(rejected("Twitter callback temporary credential does not match its correlation"));
        }
        final TokenCredentialsRequest request = new TokenCredentialsRequest(values.token(), values.verifier(),
                List.of());
        return standardAdapter.invoke(OAuth1SourceProfile.TOKEN_CREDENTIALS, request, context, timeout)
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<TokenCredentialsResponse> success -> profile(
                            success.value().oauthToken(),
                            context,
                            timeout);
                    case Outcome.Rejected<TokenCredentialsResponse> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<TokenCredentialsResponse> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Sends Twitter's signed current-user request with the two historical query extensions.
     *
     * @param oauthToken securely stored token credential identifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> profile(
            final String oauthToken,
            final Context context,
            final Timeout.Budget timeout) {
        final UnoUrl endpoint = variantDefinition.targets().resolve(settings).userInfo().getOrNull().url().newBuilder()
                .query("include_entities", Normal.TRUE).query("include_email", Normal.TRUE).build();
        final ProtectedResourceRequest request = new ProtectedResourceRequest(Http.Method.GET, endpoint,
                List.of(new OAuth1Parameter(OAuth1.Parameters.TOKEN, oauthToken)),
                Headers.of(Http.Header.ACCEPT, MediaType.APPLICATION_JSON), Normal.EMPTY_BYTE_ARRAY);
        return standardAdapter.invoke(OAuth1SourceProfile.PROTECTED_RESOURCE, request, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<HttpResponse> success -> decodeProfile(success.value(), timeout);
                    case Outcome.Rejected<HttpResponse> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<HttpResponse> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Strictly decodes the bounded Twitter User JSON response and maps only the frozen identity fields.
     *
     * @param response caller-owned signed protected-resource response
     * @param timeout  shared clock and time budget
     * @return verified external identity or a safely classified failure
     */
    private Outcome<ExternalIdentity> decodeProfile(final HttpResponse response, final Timeout.Budget timeout) {
        try (response) {
            if (response.code() != Http.Status.OK) {
                return status(response.code(), "Twitter current-user endpoint rejected or failed the request");
            }
            final MediaType media = response.body().media();
            if (media == null || !MediaType.APPLICATION_JSON_TYPE.isCompatible(media)) {
                throw new ValidateException("Twitter current-user response must use application/json");
            }
            final JsonValue value = services.jsonProvider()
                    .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("Twitter current-user response root must be a JSON object");
            }
            final ProfileWire profile = ProfileWire.decode(object);
            final String subject = profile.id();
            validateIdentifier(subject);
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            profile.copyAttributes(attributes);
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("twitter_user_id", new JsonValue.StringValue(subject), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Twitter current-user endpoint returned an invalid response");
        }
    }

    /**
     * Validates and indexes the exact Twitter OAuth 1.0 success or denial callback branch.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return immutable typed callback branch
     * @throws ValidateException if transport, target, cardinality, branch, or values are invalid
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Twitter callback must not be null");
        if (callback.method() != Http.Method.GET || !settings.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Twitter callback transport or target is invalid");
        }
        String token = null;
        String verifier = null;
        String denial = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = parameter.value();
            if (value.isBlank()) {
                throw new ValidateException("Twitter callback parameter values must not be blank");
            }
            switch (parameter.name()) {
                case OAuth1.Parameters.TOKEN -> token = unique(token, value);
                case OAuth1.Parameters.VERIFIER -> verifier = unique(verifier, value);
                case DENIAL_PARAMETER -> denial = unique(denial, value);
                default -> throw new ValidateException("Twitter callback contains an unsupported parameter");
            }
        }
        return new CallbackWire(token, verifier, denial);
    }

    /**
     * Carries one exact Twitter OAuth 1.0 authorization callback branch.
     *
     * @param token    temporary credential identifier for a successful authorization
     * @param verifier authorization verifier for a successful authorization
     * @param denial   temporary credential identifier for a denied authorization
     */
    private record CallbackWire(String token, String verifier, String denial) {

        /**
         * Validates that the callback contains exactly one complete success or denial branch.
         *
         * @throws ValidateException if the callback members do not form one exact branch
         */
        private CallbackWire {
            final boolean success = token != null && verifier != null && denial == null;
            final boolean rejected = token == null && verifier == null && denial != null;
            if (!success && !rejected) {
                throw new ValidateException("Twitter callback must contain one exact success or denial branch");
            }
        }

        /**
         * Returns the temporary credential identifier shared with browser correlation.
         *
         * @return success token or denial token
         */
        private String correlation() {
            return denial != null ? denial : token;
        }

        /**
         * Reports whether the resource owner denied authorization.
         *
         * @return {@code true} for the denial branch
         */
        private boolean denied() {
            return denial != null;
        }

    }

    /**
     * Carries the retained non-sensitive Twitter User profile projection.
     *
     * @param id                required stable decimal Twitter user identifier
     * @param screenName        optional Twitter screen name
     * @param name              optional display name
     * @param description       optional profile description
     * @param profileImageHttps optional HTTPS profile-image URL
     * @param url               optional profile URL
     * @param location          optional location text
     * @param profileImage      optional legacy profile-image URL
     * @param email             optional email returned for an authorized application
     */
    private record ProfileWire(String id, String screenName, String name, String description, String profileImageHttps,
            String url, String location, String profileImage, String email) {

        /**
         * Decodes one member-validated Twitter User object.
         *
         * @param object private current-user response object
         * @return immutable typed profile projection
         */
        private static ProfileWire decode(final JsonValue.ObjectValue object) {
            return new ProfileWire(requiredString(object, "id_str"), optionalString(object, "screen_name"),
                    optionalString(object, "name"), optionalString(object, "description"),
                    optionalString(object, "profile_image_url_https"), optionalString(object, "url"),
                    optionalString(object, "location"), optionalString(object, "profile_image_url"),
                    optionalString(object, "email"));
        }

        /**
         * Copies one optional string attribute into the identity projection.
         *
         * @param attributes mutable destination owned by the identity mapper
         * @param name       exact Twitter wire member name
         * @param value      optional decoded value
         */
        private static void put(final Map<String, JsonValue> attributes, final String name, final String value) {
            if (value != null) {
                attributes.put(name, new JsonValue.StringValue(value));
            }
        }

        /**
         * Copies present profile attributes using their exact Twitter wire names.
         *
         * @param attributes mutable destination owned by the identity mapper
         */
        private void copyAttributes(final Map<String, JsonValue> attributes) {
            put(attributes, "screen_name", screenName);
            put(attributes, "name", name);
            put(attributes, "description", description);
            put(attributes, "profile_image_url_https", profileImageHttps);
            put(attributes, "url", url);
            put(attributes, "location", location);
            put(attributes, "profile_image_url", profileImage);
            put(attributes, "email", email);
        }

    }

}
