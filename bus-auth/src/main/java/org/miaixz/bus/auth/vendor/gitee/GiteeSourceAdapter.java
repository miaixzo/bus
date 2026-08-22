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
package org.miaixz.bus.auth.vendor.gitee;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.AuthorizationClient;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
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

/**
 * Implements Gitee OAuth browser login while publishing only standard OAuth authorization.
 * <p>
 * Gitee's client-secret form, incompletely specified token errors, unauthenticated refresh wire, and query-carried
 * resource token remain private Source behavior. The public surface therefore retains only the standard authorization
 * operation and the common Source authentication lifecycle.
 * </p>
 *
 * @author Kimi Liu
 */
public class GiteeSourceAdapter implements VendorAdapter {

    /**
     * Trusted Gitee authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://gitee.com";

    /**
     * Stable framework User-Agent required by Gitee OAuth requests.
     */
    private static final String USER_AGENT = "miaixz-bus-auth";

    /**
     * Maximum accepted Gitee JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Maximum accepted Gitee JSON nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = Normal._64;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Gitee manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Gitee options.
     */
    private final GiteeOptions options;

    /**
     * Caller-owned secret, JSON, network, clock, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Shared standard OAuth authorization implementation.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time browser-state lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Shared strict UTF-8 form encoder.
     */
    private final FormCodec formCodec;

    /**
     * Creates one Source-bound Gitee.com adapter.
     *
     * @param namespaceId registration namespace used to isolate state and credential resolution
     * @param sourceId    registered Source identifier
     * @param manifest    selected Gitee manifest
     * @param variant     exact selected default manifest
     * @param options     decoded externally loaded Gitee options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, manifest, callback, or authorization is inconsistent
     */
    public GiteeSourceAdapter(final String namespaceId, final String sourceId, final GiteeManifest manifest,
            final VariantManifest.Variant variant, final GiteeOptions options, final DriverServices services) {
        final GiteeManifest selectedProfile = Assert.notNull(manifest, "Gitee manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Gitee Source id must not be blank");
        this.variant = Assert.notNull(variant, "Gitee manifest must not be null");
        this.options = Assert.notNull(options, "Gitee options must not be null");
        this.services = Assert.notNull(services, "Gitee execution services must not be null");
        if (!GiteeManifest.ID.equals(selectedProfile.vendor())
                || !selectedProfile.variant(GiteeManifest.DEFAULT).equals(variant)
                || !GiteeManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !GiteeManifest.ID.equals(options.vendor()) || !GiteeManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("Gitee adapter requires the gitee/default OAuth 2.0 manifest");
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
        this.formCodec = new FormCodec();
    }

    /**
     * Reads one required non-blank JSON string member.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return non-blank string value
     * @throws ValidateException if absent, blank, or another JSON type
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue text) || text.value().isBlank()) {
            throw new ValidateException("Gitee requires a non-blank string member");
        }
        return text.value();
    }

    /**
     * Verifies the exact private Gitee token success shape.
     *
     * @param token decoded token response
     * @return whether every required token member is present without unknown members
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue token) {
        return token.values().size() == 6 && token.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                && token.values().containsKey(OAuth2.Parameters.REFRESH_TOKEN)
                && token.values().containsKey(OAuth2.Parameters.SCOPE)
                && token.values().containsKey(OAuth2.Parameters.TOKEN_TYPE)
                && token.values().containsKey(OAuth2.Parameters.EXPIRES_IN) && token.values().containsKey("created_at");
    }

    /**
     * Verifies the private Gitee current-user vocabulary.
     *
     * @param profile decoded current-user response
     * @return whether every member belongs to the registered profile shape
     */
    private static boolean profileMembers(final JsonValue.ObjectValue profile) {
        for (String member : profile.values().keySet()) {
            switch (member) {
                case "id", "login", "name", "avatar_url", "url", "html_url", "remark", "followers_url", "following_url", "gists_url", "starred_url", "subscriptions_url", "organizations_url", "repos_url", "events_url", "received_events_url", "type", "blog", "weibo", "bio", "public_repos", "public_gists", "followers", "following", "stared", "watched", "created_at", "updated_at", "email", "enterprise", "member_role" -> {
                    // Registered Gitee profile member.
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Reads one required positive exact integral JSON number.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return positive long value
     * @throws ValidateException if absent, non-integral, out of range, or not positive
     */
    private static long requiredPositiveLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Gitee requires an integral numeric member");
        }
        try {
            final long result = number.value().longValueExact();
            if (result <= 0L) {
                throw new ValidateException("Gitee numeric member must be positive");
            }
            return result;
        } catch (ArithmeticException cause) {
            throw new ValidateException("Gitee numeric member must be an exact long", cause);
        }
    }

    /**
     * Classifies one Gitee HTTP status without parsing its undocumented error body.
     *
     * @param status      exact response status
     * @param description non-sensitive operation description
     * @param <T>         expected success type
     * @return rejected client condition or failed upstream condition
     */
    private static <T> Outcome<T> status(final int status, final String description) {
        final Map<String, JsonValue> details = Map.of("status", new JsonValue.NumberValue(BigDecimal.valueOf(status)));
        if (status == Http.Status.TOO_MANY_REQUESTS || status >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(
                    status == Http.Status.TOO_MANY_REQUESTS ? ErrorCode._429 : ErrorCode._502,
                    description,
                    details);
        }
        if (status >= Http.Status.BAD_REQUEST && status < Http.Status.INTERNAL_SERVER_ERROR) {
            return Outcome
                    .rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(details)));
        }
        return failed(ErrorCode._502, description, details);
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
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
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
        return completed(rejected("Gitee capability is not declared by the selected manifest"));
    }

    /**
     * Creates a safe request-contract rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejected stage
     */
    private static <T> CompletionStage<Outcome<T>> mismatch() {
        return completed(rejected("Gitee request does not match the selected capability contract"));
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
     * @param details     non-sensitive HTTP status details
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
     * Returns the exact capability manifest frozen by the selected Gitee manifest.
     *
     * @return immutable Source authentication and OAuth authorization manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and the registered standard OAuth authorization operation.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Gitee-private response models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "Gitee capability must not be null");
        Assert.notNull(context, "Gitee invocation context must not be null");
        Assert.notNull(timeout, "Gitee invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return missing();
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
        return mismatch();
    }

    /**
     * Builds the exact Gitee authorization redirect from generated state.
     *
     * @param initiation generated browser correlation without nonce or PKCE
     * @param context    immutable invocation context retained for operation consistency
     * @param timeout    shared end-to-end timeout
     * @return exact redirect and state correlation
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(context, "Gitee authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Gitee authorization has no remaining timeout"));
        }
        if (initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(
                    failed(
                            ErrorCode._500,
                            "Gitee browser flow generated security material outside its registered policy"));
        }
        try {
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final String redirect = endpoint.url().newBuilder().query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query(OAuth2.Parameters.SCOPE, String.join(Symbol.SPACE, options.scopes()))
                    .query(OAuth2.Parameters.STATE, initiation.state()).build().toString();
            return completed(Outcome.succeeded(new RedirectManager.Prepared(redirect, initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("Gitee authorization request is invalid"));
        }
    }

    /**
     * Extracts the unique state from one exact Gitee callback branch.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return unique non-blank state
     * @throws ValidateException if callback transport, target, branch, or multiplicity is invalid
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Exchanges one correlated Gitee callback and resolves the verified current user.
     *
     * @param completion consumed callback correlation without PKCE material
     * @param context    immutable invocation context used for one secret resolution
     * @param timeout    shared end-to-end timeout
     * @return verified Gitee identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Gitee authorization callback is invalid"));
        }
        if (completion.codeVerifier().isPresent()) {
            return completed(failed(ErrorCode._500, "Gitee callback unexpectedly carried a PKCE verifier"));
        }
        if (values.failed()) {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400, "Gitee authorization endpoint returned an OAuth error",
                                    new JsonValue.ObjectValue(
                                            Map.of(Builder.OAUTH_ERROR, new JsonValue.StringValue(values.error()))))));
        }
        return Outcome
                .mapStage(
                        () -> services.secretLoader().load(
                                new SecretLoader.Request(services.registration(), options.credential()),
                                context,
                                timeout),
                        loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(
                            values.code(),
                            success.value(),
                            timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Executes the private Gitee token and profile requests under one owned secret lease.
     *
     * @param code    consumed authorization code
     * @param secret  owned Client Secret lease closed by this asynchronous operation
     * @param timeout shared end-to-end timeout
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> authenticate(
            final String code,
            final SecretLease secret,
            final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try (secret) {
                return switch (token(code, secret, timeout)) {
                    case Outcome.Succeeded<Access> success -> profile(success.value(), timeout);
                    case Outcome.Rejected<Access> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Access> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                };
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Gitee authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Executes the exact Gitee client-secret form authorization-code request.
     *
     * @param code    sensitive one-time authorization code
     * @param secret  still-open Client Secret lease
     * @param timeout shared end-to-end timeout
     * @return private access result or safely classified Gitee failure
     */
    private Outcome<Access> token(final String code, final SecretLease secret, final Timeout timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Gitee token request has no remaining timeout");
            }
            body = formCodec.encode(
                    List.of(
                            new NameValue(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()),
                            new NameValue(OAuth2.Parameters.CODE,
                                    Assert.notBlank(code, "Gitee authorization code must not be blank")),
                            new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                            new NameValue(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()),
                            new NameValue(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material()))));
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            try (Response response = FabricX.http(services.fabric(), Protocol.OAUTH2, timeout)
                    .url(endpoint.url().toString()).method(Http.Method.POST).header(Http.Header.USER_AGENT, USER_AGENT)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Gitee token request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Strictly decodes one Gitee token success without interpreting undocumented error bodies.
     *
     * @param response owned token endpoint response
     * @return private access result or safely classified failure
     */
    private Outcome<Access> token(final Response response) {
        if (response.code() < Http.Status.OK || response.code() >= Http.Status.MULTIPLE_CHOICES) {
            return status(response.code(), "Gitee token endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue object = object(response, "token");
            if (!tokenMembers(object)) {
                return failed(ErrorCode._502, "Gitee token success response is invalid");
            }
            final String accessToken = requiredString(object, OAuth2.Parameters.ACCESS_TOKEN);
            requiredString(object, OAuth2.Parameters.REFRESH_TOKEN);
            final Scope scope = Scope.parse(requiredString(object, OAuth2.Parameters.SCOPE));
            if (!scope.values().contains("user_info") || !"bearer"
                    .equals(requiredString(object, OAuth2.Parameters.TOKEN_TYPE).toLowerCase(Locale.ROOT))) {
                throw new ValidateException("Gitee token response does not retain the required authorization");
            }
            final long expiresIn = requiredPositiveLong(object, OAuth2.Parameters.EXPIRES_IN);
            requiredPositiveLong(object, "created_at");
            return Outcome.succeeded(new Access(accessToken, expiresIn));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Gitee token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves the Gitee current-user resource using its registered query-token deviation.
     *
     * @param access  private access-token result
     * @param timeout shared end-to-end timeout
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> profile(final Access access, final Timeout timeout) {
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Gitee profile request has no remaining timeout");
            }
            final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
            try (Response response = FabricX.http(services.fabric(), Protocol.OAUTH2, timeout)
                    .url(endpoint.url().toString()).method(Http.Method.GET)
                    .query(OAuth2.Parameters.ACCESS_TOKEN, access.accessToken())
                    .header(Http.Header.USER_AGENT, USER_AGENT).execute()) {
                return profile(response, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Gitee profile request failed");
        }
    }

    /**
     * Strictly decodes one Gitee current-user success and maps its numeric identifier.
     *
     * @param response owned current-user response
     * @param timeout  shared clock used for evidence verification time
     * @return verified identity or safely classified Gitee failure
     */
    private Outcome<ExternalIdentity> profile(final Response response, final Timeout timeout) {
        if (response.code() != Http.Status.OK) {
            return status(response.code(), "Gitee current-user endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue object = object(response, "profile");
            if (!profileMembers(object)) {
                return failed(ErrorCode._502, "Gitee current-user response contains an unknown member");
            }
            final String subject = Long.toUnsignedString(requiredPositiveLong(object, "id"));
            requiredString(object, "login");
            requiredString(object, "name");
            requiredString(object, "avatar_url");
            requiredString(object, "url");
            requiredString(object, "html_url");
            requiredString(object, "type");
            requiredString(object, "created_at");
            requiredString(object, "updated_at");
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            for (Map.Entry<String, JsonValue> entry : object.values().entrySet()) {
                if (!"id".equals(entry.getKey())) {
                    attributes.put(entry.getKey(), entry.getValue());
                }
            }
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("gitee_user_id", new JsonValue.StringValue(subject), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Gitee current-user endpoint returned an invalid response");
        }
    }

    /**
     * Validates one public standard authorization request against the Gitee registration.
     *
     * @param request standard OAuth authorization request
     * @return {@code true} only when client, redirect, scope, state, response, and extensions are exact
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request.scope().getOrNull();
        return ResponseType.CODE.equals(request.responseType()) && options.clientId().equals(request.clientId())
                && options.redirectUri().equals(request.redirectUri()) && scope != null
                && options.scopes().equals(scope.values()) && request.state().isPresent()
                && request.codeChallenge().isEmpty() && request.codeChallengeMethod().isEmpty()
                && request.extensions().values().isEmpty();
    }

    /**
     * Validates and indexes one exact Gitee GET callback branch.
     *
     * @param callback raw inbound callback
     * @return typed exact callback branch
     * @throws ValidateException if target, transport, names, branches, or values violate the frozen contract
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Gitee callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Gitee callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        String error = null;
        String errorDescription = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert.notBlank(parameter.value(), "Gitee callback value must not be blank");
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code, value);
                case OAuth2.Parameters.STATE -> state = unique(state, value);
                case OAuth2.Parameters.ERROR -> error = unique(error, value);
                case OAuth2.Parameters.ERROR_DESCRIPTION -> errorDescription = unique(errorDescription, value);
                default -> throw new ValidateException("Gitee callback contains an unsupported parameter");
            }
        }
        return new CallbackWire(code, state, error, errorDescription);
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
            throw new ValidateException("Gitee callback parameter names must be unique");
        }
        return value;
    }

    /**
     * Decodes one bounded Gitee JSON object.
     *
     * @param response  response whose body remains open
     * @param operation safe operation label used only in validation messages
     * @return strict provider-neutral JSON object
     * @throws ValidateException if media, JSON shape, depth, or duplicate members are invalid
     */
    private JsonValue.ObjectValue object(final Response response, final String operation) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Gitee " + operation + " response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Gitee " + operation + " response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries one exact Gitee authorization callback branch.
     *
     * @param code             authorization code for success
     * @param state            mandatory browser correlation value
     * @param error            OAuth error for failure
     * @param errorDescription optional OAuth error description
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state, String error, String errorDescription) {

        /**
         * Validates the exact success or OAuth error branch.
         *
         * @throws IllegalArgumentException if the state is blank
         * @throws ValidateException        if members do not form one exact branch
         */
        private CallbackWire {
            Assert.notBlank(state, "Gitee callback state must not be blank");
            final boolean success = code != null && error == null && errorDescription == null;
            final boolean failure = code == null && error != null;
            if (!success && !failure) {
                throw new ValidateException("Gitee callback must contain one exact success or OAuth error branch");
            }
        }

        /**
         * Reports whether this callback carries an OAuth error.
         *
         * @return {@code true} for an error callback
         */
        private boolean failed() {
            return error != null;
        }

    }

    /**
     * Carries Gitee's private token success fields required by the immediate profile request.
     *
     * @param accessToken sensitive access token
     * @param expiresIn   positive access-token lifetime in seconds
     * @author Kimi Liu
     */
    private record Access(String accessToken, long expiresIn) {

        /**
         * Validates one private Gitee token success value.
         *
         * @throws IllegalArgumentException if the token is blank
         * @throws ValidateException        if the lifetime is not positive
         */
        private Access {
            Assert.notBlank(accessToken, "Gitee private access token must not be blank");
            if (expiresIn <= 0L) {
                throw new ValidateException("Gitee private access-token lifetime must be positive");
            }
        }

        /**
         * Returns a diagnostic representation without bearer material.
         *
         * @return redacted private token summary
         */
        @Override
        public String toString() {
            return Builder.REDACTED_ACCESS_TOKEN + expiresIn + Symbol.C_BRACKET_RIGHT;
        }

    }

}
