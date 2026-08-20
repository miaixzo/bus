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
package org.miaixz.bus.auth.vendor.facebook;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationRequest;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.ResponseType;
import org.miaixz.bus.auth.protocol.oauth2.Scope;
import org.miaixz.bus.auth.protocol.oauth2.client.AuthorizationClient;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
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
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements Facebook Login with Graph API v26.0 while publishing only standard OAuth authorization.
 * <p>
 * The authorization endpoint delegates to the shared OAuth client for public O2A calls. Browser completion keeps
 * Facebook's query-authenticated token response and app-secret-proof protected Graph profile private, resolves the app
 * secret exactly once, and emits only a verified app-scoped identity.
 * </p>
 *
 * @author Kimi Liu
 */
public final class FacebookSourceAdapter implements VendorAdapter {

    /**
     * Trusted Graph authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://graph.facebook.com";

    /**
     * Exact Graph fields requested by the frozen identity projection.
     */
    private static final String PROFILE_FIELDS = "id,name,first_name,last_name,middle_name,name_format,picture,short_name,email";

    /**
     * Maximum accepted Graph JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum accepted Graph JSON nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Facebook manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Facebook options.
     */
    private final FacebookOptions options;

    /**
     * Caller-owned secret, JSON, network, clock, and execution dependencies.
     */
    private final ExecutionServices services;

    /**
     * Shared standard OAuth authorization implementation.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time browser-state lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Creates one Source-bound Facebook Login adapter from the frozen v26.0 manifest.
     *
     * @param namespaceId registration namespace used to isolate state and credential resolution
     * @param sourceId    registered Source identifier
     * @param manifest    selected Facebook manifest
     * @param variant     exact selected default manifest
     * @param options     decoded externally loaded Facebook options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, manifest, callback, or standard authorization is
     *                                  inconsistent
     */
    public FacebookSourceAdapter(final String namespaceId, final String sourceId, final FacebookManifest manifest,
            final VariantManifest.Variant variant, final FacebookOptions options, final ExecutionServices services) {
        final FacebookManifest selectedProfile = Assert.notNull(manifest, "Facebook manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Facebook Source id must not be blank");
        this.variant = Assert.notNull(variant, "Facebook manifest must not be null");
        this.options = Assert.notNull(options, "Facebook options must not be null");
        this.services = Assert.notNull(services, "Facebook execution services must not be null");
        if (!FacebookManifest.ID.equals(selectedProfile.vendor())
                || !selectedProfile.variant(FacebookManifest.DEFAULT).equals(variant)
                || !FacebookManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !FacebookManifest.ID.equals(options.vendor()) || !FacebookManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("Facebook adapter requires the facebook/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        final var targets = variant.targets().resolve(options);
        final OAuth2ClientOptions oauthSettings = new OAuth2ClientOptions(targets.authorization(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                options.clientId(), Set.of(options.redirectUri().getOrNull()), Endpoint.Authentication.NONE,
                Optional.empty(), false, false);
        final AuthorizationClient authorizationClient = new AuthorizationClient(oauthSettings,
                new AuthorizationRequestEncoder(targets.authorization().getOrNull()));
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager), List
                .of(new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION, authorizationClient::authorize)));
    }

    /**
     * Classifies one exact Graph error envelope without retaining personal diagnostic text.
     *
     * @param status original HTTP status
     * @param value  raw error member value
     * @param <T>    expected success type
     * @return rejected request error or failed transient/upstream error
     */
    private static <T> Outcome<T> graphError(final int status, final JsonValue value) {
        if (!(value instanceof JsonValue.ObjectValue error) || !errorMembers(error)) {
            return failed(ErrorCode._502, "Facebook Graph error envelope is invalid");
        }
        try {
            requiredString(error, "message");
            requiredString(error, "type");
            final long code = requiredLong(error, "code");
            final Long subcode = optionalLong(error, "error_subcode");
            optionalNonBlank(error, "error_user_title");
            optionalNonBlank(error, "error_user_msg");
            optionalNonBlank(error, "fbtrace_id");
            final Map<String, JsonValue> details = new LinkedHashMap<>();
            details.put("code", new JsonValue.NumberValue(java.math.BigDecimal.valueOf(code)));
            if (subcode != null) {
                details.put("subcode", new JsonValue.NumberValue(java.math.BigDecimal.valueOf(subcode)));
            }
            if (status == Http.Status.TOO_MANY_REQUESTS || status >= Http.Status.INTERNAL_SERVER_ERROR
                    || transientError(code)) {
                return failed(
                        status == Http.Status.TOO_MANY_REQUESTS ? ErrorCode._429 : ErrorCode._502,
                        "Facebook Graph returned a transient or upstream error",
                        details);
            }
            if (status >= Http.Status.BAD_REQUEST && status < Http.Status.INTERNAL_SERVER_ERROR || rejectedError(code)
                    || code >= 200L && code <= 299L) {
                return Outcome.rejected(
                        new Outcome.Failure(ErrorCode._400, "Facebook Graph rejected the request",
                                new JsonValue.ObjectValue(details)));
            }
            return failed(ErrorCode._502, "Facebook Graph returned an unclassified error", details);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Facebook Graph error envelope is invalid");
        }
    }

    /**
     * Verifies the private Graph error vocabulary without maintaining a field collection.
     *
     * @param error decoded Graph error object
     * @return whether every member is registered
     */
    private static boolean errorMembers(final JsonValue.ObjectValue error) {
        for (String member : error.values().keySet()) {
            switch (member) {
                case "message", "type", "code", "error_subcode", "error_user_title", "error_user_msg", "fbtrace_id" -> {
                    // Registered Graph error member.
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Classifies one Graph error code as transient.
     *
     * @param code Graph error code
     * @return whether retry may succeed
     */
    private static boolean transientError(final long code) {
        return code == 1L || code == 2L || code == 4L || code == 17L || code == 341L || code == 368L;
    }

    /**
     * Classifies one Graph error code as a request or authorization rejection.
     *
     * @param code Graph error code
     * @return whether the caller or authorization caused the error
     */
    private static boolean rejectedError(final long code) {
        return code == 3L || code == 10L || code == 100L || code == 102L || code == 104L || code == 190L;
    }

    /**
     * Verifies the private Graph profile vocabulary.
     *
     * @param profile decoded profile object
     * @return whether every member is registered
     */
    private static boolean profileMembers(final JsonValue.ObjectValue profile) {
        for (String member : profile.values().keySet()) {
            switch (member) {
                case "id", "name", "first_name", "last_name", "middle_name", "name_format", "picture", "short_name", "email" -> {
                    // Registered Graph profile member.
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Reads one required non-blank JSON string member.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return non-blank string value
     * @throws ValidateException if the member is absent, blank, or another JSON type
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue text) || text.value().isBlank()) {
            throw new ValidateException("Facebook Graph requires a non-blank string member");
        }
        return text.value();
    }

    /**
     * Reads one required ASCII-decimal Facebook identifier without numeric conversion.
     *
     * @param object decoded profile object
     * @param name   exact identifier member
     * @return unchanged non-blank decimal text
     * @throws ValidateException if the member is absent or contains a non-digit character
     */
    private static String requiredDigits(final JsonValue.ObjectValue object, final String name) {
        final String value = requiredString(object, name);
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < Symbol.C_ZERO || character > Symbol.C_NINE) {
                throw new ValidateException("Facebook app-scoped identifier must contain only ASCII digits");
            }
        }
        return value;
    }

    /**
     * Copies one optional non-blank Graph string attribute while preserving its official name.
     *
     * @param source decoded profile object
     * @param target verified attribute destination
     * @param name   exact Graph member name
     * @throws ValidateException if a present member is null, blank, or another JSON type
     */
    private static void optionalString(
            final JsonValue.ObjectValue source,
            final Map<String, JsonValue> target,
            final String name) {
        final JsonValue value = source.values().get(name);
        if (value == null) {
            return;
        }
        if (!(value instanceof JsonValue.StringValue text) || text.value().isBlank()) {
            throw new ValidateException("Facebook optional profile member must be a non-blank string");
        }
        target.put(name, value);
    }

    /**
     * Reads one exact integral JSON number.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return exact long value
     * @throws ValidateException if the member is absent, non-integral, out of range, or another type
     */
    private static long requiredLong(final JsonValue.ObjectValue object, final String name) {
        final Long value = optionalLong(object, name);
        if (value == null) {
            throw new ValidateException("Facebook Graph requires an integral numeric member");
        }
        return value;
    }

    /**
     * Reads one exact positive integral JSON number.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return positive long value
     * @throws ValidateException if the value is absent or not positive
     */
    private static long requiredPositiveLong(final JsonValue.ObjectValue object, final String name) {
        final long value = requiredLong(object, name);
        if (value <= 0L) {
            throw new ValidateException("Facebook token lifetime must be positive");
        }
        return value;
    }

    /**
     * Reads one optional exact integral JSON number.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return exact long or {@code null} when absent
     * @throws ValidateException if a present member is non-integral, out of range, null, or another type
     */
    private static Long optionalLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Facebook Graph numeric member has an invalid type");
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException cause) {
            throw new ValidateException("Facebook Graph numeric member must be an exact long", cause);
        }
    }

    /**
     * Validates one optional non-blank Graph diagnostic string without retaining it.
     *
     * @param object decoded Graph error object
     * @param name   exact optional member name
     * @throws ValidateException if a present member is null, blank, or another JSON type
     */
    private static void optionalNonBlank(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value != null && (!(value instanceof JsonValue.StringValue text) || text.value().isBlank())) {
            throw new ValidateException("Facebook Graph optional error member must be a non-blank string");
        }
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
        return completed(rejected("Facebook capability is not declared by the selected manifest"));
    }

    /**
     * Creates a safe request-contract rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejected stage
     */
    private static <T> CompletionStage<Outcome<T>> mismatch() {
        return completed(rejected("Facebook request does not match the selected capability contract"));
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
     * Creates a safe operational failure with non-sensitive structured details.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param details     non-sensitive numeric Graph error details
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
     * @param value mutable sensitive bytes, or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) 0);
        }
    }

    /**
     * Returns the exact capability manifest frozen by the selected Facebook manifest.
     *
     * @return immutable Source authentication and OAuth authorization manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and the single registered standard OAuth operation.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Facebook private models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Facebook capability must not be null");
        Assert.notNull(context, "Facebook invocation context must not be null");
        Assert.notNull(timeout, "Facebook invocation budget must not be null");
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
        if (capability.equals(OAuth2ClientScheme.AUTHORIZATION) && request instanceof AuthorizationRequest authorization
                && valid(authorization)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return mismatch();
    }

    /**
     * Builds the exact Facebook authorization redirect from generated state.
     *
     * @param initiation generated browser correlation without nonce or PKCE
     * @param context    immutable invocation context retained for operation consistency
     * @param timeout    shared end-to-end budget
     * @return exact redirect and state correlation
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "Facebook authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Facebook authorization has no remaining time budget"));
        }
        if (initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(
                    failed(
                            ErrorCode._500,
                            "Facebook browser flow generated security material outside its registered policy"));
        }
        try {
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final String location = endpoint.url().newBuilder().query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.STATE, initiation.state())
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query(OAuth2.Parameters.SCOPE, String.join(Symbol.SPACE, requestedScopes())).build().toString();
            return completed(Outcome.succeeded(new RedirectManager.Prepared(location, initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("Facebook authorization request is invalid"));
        }
    }

    /**
     * Extracts the unique state from one exact Facebook callback branch.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return unique non-blank state
     * @throws ValidateException if callback target, method, branch, values, or multiplicity is invalid
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Exchanges a correlated Facebook callback and resolves the verified app-scoped identity.
     *
     * @param completion consumed callback correlation without PKCE verifier
     * @param context    immutable invocation context used for one secret resolution
     * @param timeout    shared end-to-end budget
     * @return verified Facebook identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final CallbackWire callback;
        try {
            callback = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Facebook authorization callback is invalid"));
        }
        if (completion.codeVerifier().isPresent()) {
            return completed(failed(ErrorCode._500, "Facebook callback unexpectedly carried a PKCE verifier"));
        }
        if (callback.denied()) {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400, "Facebook resource owner denied authorization",
                                    new JsonValue.ObjectValue(
                                            Map.of("oauth_error", new JsonValue.StringValue("access_denied"))))));
        }
        return org.miaixz.bus.auth.runtime.LoadResult
                .parse(
                        services.secretLoader().load(options.credential(), context, timeout),
                        loaded -> services.secretParser().parse(options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(
                            callback.code(),
                            success.value(),
                            timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Executes Facebook token and Graph profile requests under one exclusively owned secret lease.
     *
     * @param code    consumed authorization code
     * @param secret  owned app-secret lease closed by this asynchronous operation
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> authenticate(
            final String code,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try (secret) {
                return switch (token(code, secret, timeout)) {
                    case Outcome.Succeeded<Access> success -> profile(success.value(), secret, timeout);
                    case Outcome.Rejected<Access> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Access> failed -> Outcome.failed(failed.failure());
                };
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Facebook authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Executes the exact Facebook query-authenticated authorization-code request.
     *
     * @param code    sensitive authorization code
     * @param secret  still-open app-secret lease shared with profile proof generation
     * @param timeout shared end-to-end budget
     * @return private access result or safely classified Graph failure
     */
    private Outcome<Access> token(final String code, final SecretLease secret, final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Facebook token request has no remaining time budget");
        }
        try {
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET).query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material()))
                    .query(OAuth2.Parameters.CODE, code).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Facebook token request failed");
        }
    }

    /**
     * Strictly decodes one Facebook token success or Graph error response.
     *
     * @param response owned token endpoint response
     * @return private access result or safely classified Graph failure
     */
    private Outcome<Access> token(final HttpResponse response) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (object.values().size() == 2 && object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                    && object.values().containsKey(OAuth2.Parameters.EXPIRES_IN)) {
                if (response.code() != Http.Status.OK) {
                    return failed(ErrorCode._502, "Facebook token endpoint returned success fields with error status");
                }
                return Outcome.succeeded(
                        new Access(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN),
                                requiredPositiveLong(object, OAuth2.Parameters.EXPIRES_IN)));
            }
            if (object.values().size() == 1 && object.values().containsKey(OAuth2.Parameters.ERROR)) {
                return graphError(response.code(), object.values().get(OAuth2.Parameters.ERROR));
            }
            return failed(ErrorCode._502, "Facebook token response has an invalid branch");
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Facebook token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves the app-secret-proof protected Facebook profile.
     *
     * @param access  private access-token result
     * @param secret  still-open app-secret lease
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> profile(
            final Access access,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        byte[] key = null;
        byte[] message = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Facebook profile request has no remaining time budget");
            }
            key = new String(secret.material()).getBytes(Charset.UTF_8);
            message = access.accessToken().getBytes(Charset.UTF_8);
            final String proof = Builder.hmacSha256(key).digestHex(message);
            final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET)
                    .header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + access.accessToken())
                    .query("fields", PROFILE_FIELDS).query("appsecret_proof", proof)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return profile(response, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Facebook profile request failed");
        } finally {
            clear(key);
            clear(message);
        }
    }

    /**
     * Strictly decodes one Facebook profile success or Graph error response.
     *
     * @param response owned profile endpoint response
     * @param timeout  shared clock used for evidence verification time
     * @return verified identity or safely classified Graph failure
     */
    private Outcome<ExternalIdentity> profile(final HttpResponse response, final Timeout.Budget timeout) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (object.values().size() == 1 && object.values().containsKey(OAuth2.Parameters.ERROR)) {
                return graphError(response.code(), object.values().get(OAuth2.Parameters.ERROR));
            }
            if (response.code() != Http.Status.OK || !profileMembers(object)) {
                return failed(ErrorCode._502, "Facebook profile response has an invalid success branch");
            }
            final String subject = requiredDigits(object, "id");
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            optionalString(object, attributes, "name");
            optionalString(object, attributes, "first_name");
            optionalString(object, attributes, "last_name");
            optionalString(object, attributes, "middle_name");
            optionalString(object, attributes, "name_format");
            optionalString(object, attributes, "short_name");
            optionalString(object, attributes, "email");
            final JsonValue picture = object.values().get("picture");
            if (picture != null) {
                if (!(picture instanceof JsonValue.ObjectValue)) {
                    throw new ValidateException("Facebook picture must retain its Graph object type");
                }
                attributes.put("picture", picture);
            }
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("facebook_user_id", new JsonValue.StringValue(subject), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Facebook profile endpoint returned an invalid response");
        }
    }

    /**
     * Validates one public standard authorization request against the Facebook registration.
     *
     * @param request standard OAuth authorization request
     * @return {@code true} only when every client, redirect, scope, state, response, and extension field is exact
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
     * Validates and indexes one exact Facebook GET callback.
     *
     * @param callback raw inbound callback
     * @return typed exact callback branch
     * @throws ValidateException if target, transport, names, branches, or values violate the frozen contract
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Facebook callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Facebook callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        String error = null;
        String errorDescription = null;
        String errorReason = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert.notBlank(parameter.value(), "Facebook callback value must not be blank");
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code, value);
                case OAuth2.Parameters.STATE -> state = unique(state, value);
                case OAuth2.Parameters.ERROR -> error = unique(error, value);
                case OAuth2.Parameters.ERROR_DESCRIPTION -> errorDescription = unique(errorDescription, value);
                case "error_reason" -> errorReason = unique(errorReason, value);
                default -> throw new ValidateException("Facebook callback contains an unsupported parameter");
            }
        }
        return new CallbackWire(code, state, error, errorDescription, errorReason);
    }

    /**
     * Accepts one callback value only once.
     *
     * @param current previously decoded value, or {@code null}
     * @param value   newly decoded non-blank value
     * @return newly decoded value
     * @throws ValidateException if the callback repeats a parameter
     */
    private String unique(final String current, final String value) {
        if (current != null) {
            throw new ValidateException("Facebook callback parameter names must be unique");
        }
        return value;
    }

    /**
     * Decodes one bounded Graph JSON object for any HTTP status branch.
     *
     * @param response response whose body remains open
     * @return strict provider-neutral JSON object
     * @throws ValidateException if media, JSON shape, depth, or duplicate members are invalid
     */
    private JsonValue.ObjectValue object(final HttpResponse response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Facebook Graph response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Facebook Graph response root must be a JSON object");
        }
        return object;
    }

    /**
     * Returns explicit permissions or immutable manifest defaults.
     *
     * @return ordered effective Facebook Login permissions
     */
    private List<String> requestedScopes() {
        return options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
    }

    /**
     * Carries one exact Facebook authorization callback branch.
     *
     * @param code             authorization code for a successful callback
     * @param state            mandatory browser correlation value
     * @param error            OAuth error for a denied callback
     * @param errorDescription provider diagnostic for a denied callback
     * @param errorReason      Facebook denial reason
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state, String error, String errorDescription, String errorReason) {

        /**
         * Validates the exact success or resource-owner denial branch.
         *
         * @throws IllegalArgumentException if the state is blank
         * @throws ValidateException        if members do not form one exact supported branch
         */
        private CallbackWire {
            Assert.notBlank(state, "Facebook callback state must not be blank");
            final boolean success = code != null && error == null && errorDescription == null && errorReason == null;
            final boolean denial = code == null && "access_denied".equals(error) && errorDescription != null
                    && "user_denied".equals(errorReason);
            if (!success && !denial) {
                throw new ValidateException("Facebook callback must contain one exact success or denial branch");
            }
        }

        /**
         * Reports whether the callback represents resource-owner denial.
         *
         * @return {@code true} for the exact Facebook denial branch
         */
        private boolean denied() {
            return error != null;
        }

    }

    /**
     * Carries Facebook's private token success fields required by the immediate profile request.
     *
     * @param accessToken sensitive access token
     * @param expiresIn   positive access-token lifetime in seconds
     * @author Kimi Liu
     */
    private record Access(String accessToken, long expiresIn) {

        /**
         * Validates one private Facebook token success value.
         *
         * @throws IllegalArgumentException if the access token is blank
         * @throws ValidateException        if the lifetime is not positive
         */
        private Access {
            Assert.notBlank(accessToken, "Facebook private access token must not be blank");
            if (expiresIn <= 0L) {
                throw new ValidateException("Facebook private access-token lifetime must be positive");
            }
        }

        /**
         * Returns a diagnostic representation without bearer material.
         *
         * @return redacted private token summary
         */
        @Override
        public String toString() {
            return "Access[accessToken=[REDACTED], expiresIn=" + expiresIn + Symbol.C_BRACKET_RIGHT;
        }

    }

}
