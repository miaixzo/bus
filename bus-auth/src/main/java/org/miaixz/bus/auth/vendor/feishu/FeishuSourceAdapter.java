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
package org.miaixz.bus.auth.vendor.feishu;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.AuthorizationClient;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.*;
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
 * Implements Feishu v3 browser login while publishing only standard OAuth authorization.
 * <p>
 * Feishu's JSON client authentication, token envelope, and user-information envelope remain private to Source
 * authentication. The adapter resolves the App Secret once, consumes mandatory S256 PKCE material, and emits a verified
 * identity keyed only by the stable Feishu union identifier.
 * </p>
 *
 * @author Kimi Liu
 */
public final class FeishuSourceAdapter implements VendorAdapter {

    /**
     * Trusted Feishu authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://open.feishu.cn";

    /**
     * Maximum accepted Feishu JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Maximum accepted Feishu JSON nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = Normal._64;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Feishu manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Feishu options.
     */
    private final FeishuOptions options;

    /**
     * Caller-owned secret, JSON, network, clock, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Shared standard OAuth authorization implementation.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time state and mandatory PKCE lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Creates one Source-bound Feishu v3 adapter.
     *
     * @param namespaceId registration namespace used to isolate state and credential resolution
     * @param sourceId    registered Source identifier
     * @param manifest    selected Feishu manifest
     * @param variant     exact selected default manifest
     * @param options     decoded externally loaded Feishu options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, manifest, callback, or authorization is inconsistent
     */
    public FeishuSourceAdapter(final String namespaceId, final String sourceId, final FeishuManifest manifest,
            final VariantManifest.Variant variant, final FeishuOptions options, final DriverServices services) {
        final FeishuManifest selectedProfile = Assert.notNull(manifest, "Feishu manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Feishu Source id must not be blank");
        this.variant = Assert.notNull(variant, "Feishu manifest must not be null");
        this.options = Assert.notNull(options, "Feishu options must not be null");
        this.services = Assert.notNull(services, "Feishu execution services must not be null");
        if (!FeishuManifest.ID.equals(selectedProfile.vendor())
                || !selectedProfile.variant(FeishuManifest.DEFAULT).equals(variant)
                || !FeishuManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !FeishuManifest.ID.equals(options.vendor()) || !FeishuManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("Feishu adapter requires the feishu/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        final var targets = variant.targets().resolve(options);
        final OAuth2ClientOptions oauthSettings = new OAuth2ClientOptions(targets.authorization(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                options.clientId(), Set.of(options.redirectUri().getOrNull()), Endpoint.Authentication.NONE,
                Optional.empty(), true, false);
        final AuthorizationClient authorizationClient = new AuthorizationClient(oauthSettings,
                new AuthorizationRequestEncoder(targets.authorization().getOrNull()));
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager), List
                .of(new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION, authorizationClient::authorize)));
    }

    /**
     * Classifies one exact Feishu token error without retaining its description.
     *
     * @param status original HTTP status
     * @param code   exact Feishu numeric code
     * @param error  exact OAuth-like platform error token
     * @param <T>    expected success type
     * @return rejected client request or failed upstream condition
     */
    private static <T> Outcome<T> tokenError(final int status, final long code, final String error) {
        final Map<String, JsonValue> details = new LinkedHashMap<>();
        details.put("code", number(code));
        details.put(Builder.OAUTH_ERROR, new JsonValue.StringValue(error));
        if (status == Http.Status.TOO_MANY_REQUESTS || status >= Http.Status.INTERNAL_SERVER_ERROR || code == 20050L) {
            return failed(
                    status == Http.Status.TOO_MANY_REQUESTS ? ErrorCode._429 : ErrorCode._502,
                    "Feishu token endpoint returned a transient or upstream error",
                    details);
        }
        if (code >= 20000L && code <= 20049L) {
            return Outcome.rejected(
                    new Outcome.Failure(ErrorCode._400, "Feishu token endpoint rejected the request",
                            new JsonValue.ObjectValue(details)));
        }
        return failed(ErrorCode._502, "Feishu token endpoint returned an unclassified error", details);
    }

    /**
     * Classifies one Feishu identity error code without retaining profile diagnostics.
     *
     * @param status original HTTP status
     * @param code   exact Feishu numeric code
     * @param <T>    expected success type
     * @return rejected access condition or failed upstream condition
     */
    private static <T> Outcome<T> identityError(final int status, final long code) {
        final Map<String, JsonValue> details = Map.of("code", number(code));
        if (identityRejection(code)) {
            return Outcome.rejected(
                    new Outcome.Failure(ErrorCode._400, "Feishu profile endpoint rejected the access token",
                            new JsonValue.ObjectValue(details)));
        }
        if (status == Http.Status.TOO_MANY_REQUESTS || status >= Http.Status.INTERNAL_SERVER_ERROR || code == 20050L) {
            return failed(
                    status == Http.Status.TOO_MANY_REQUESTS ? ErrorCode._429 : ErrorCode._502,
                    "Feishu profile endpoint returned a transient or upstream error",
                    details);
        }
        return failed(ErrorCode._502, "Feishu profile endpoint returned an unclassified error", details);
    }

    /**
     * Classifies one Feishu identity code as an expected access rejection.
     *
     * @param code exact Feishu identity error code
     * @return whether the profile request was rejected by authorization state
     */
    private static boolean identityRejection(final long code) {
        return code == 20001L || code == 20005L || code == 20008L || code == 20021L || code == 20022L || code == 20023L;
    }

    /**
     * Verifies the private Feishu token success vocabulary.
     *
     * @param token decoded token envelope
     * @return whether every member belongs to the documented success branch
     */
    private static boolean tokenSuccessMembers(final JsonValue.ObjectValue token) {
        for (String member : token.values().keySet()) {
            switch (member) {
                case "code", OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.TOKEN_TYPE, OAuth2.Parameters.EXPIRES_IN, OAuth2.Parameters.REFRESH_TOKEN, "refresh_token_expires_in", OAuth2.Parameters.SCOPE -> {
                    // Registered Feishu token success member.
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Verifies the exact private Feishu token error vocabulary.
     *
     * @param token decoded token error envelope
     * @return whether the envelope contains exactly its three registered members
     */
    private static boolean tokenErrorMembers(final JsonValue.ObjectValue token) {
        return token.values().size() == 3 && token.values().containsKey("code")
                && token.values().containsKey(OAuth2.Parameters.ERROR)
                && token.values().containsKey(OAuth2.Parameters.ERROR_DESCRIPTION);
    }

    /**
     * Verifies the private Feishu profile-envelope vocabulary.
     *
     * @param profile decoded profile envelope
     * @return whether every member is registered
     */
    private static boolean profileMembers(final JsonValue.ObjectValue profile) {
        for (String member : profile.values().keySet()) {
            if (!"code".equals(member) && !"msg".equals(member) && !"data".equals(member)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies the private Feishu user vocabulary.
     *
     * @param user decoded user object
     * @return whether every member belongs to the identity projection
     */
    private static boolean userMembers(final JsonValue.ObjectValue user) {
        for (String member : user.values().keySet()) {
            switch (member) {
                case "name", "en_name", "avatar_url", "avatar_thumb", "avatar_middle", "avatar_big", "open_id", "union_id", "email", "enterprise_email", "user_id", "mobile", "tenant_key", "employee_no" -> {
                    // Registered Feishu user member.
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Copies one optional private Feishu user string into verified attributes.
     *
     * @param source decoded user object
     * @param target verified attribute destination
     * @param name   official Feishu member name
     * @throws ValidateException if a present value is not a JSON string
     */
    private static void optionalAttribute(
            final JsonValue.ObjectValue source,
            final Map<String, JsonValue> target,
            final String name) {
        final JsonValue value = source.values().get(name);
        if (value != null) {
            if (!(value instanceof JsonValue.StringValue)) {
                throw new ValidateException("Feishu profile field has an invalid JSON type");
            }
            target.put(name, value);
        }
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
        final String value = string(object, name);
        if (value.isBlank()) {
            throw new ValidateException("Feishu requires a non-blank string member");
        }
        return value;
    }

    /**
     * Reads one required JSON string member while permitting an empty diagnostic value.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return exact string value
     * @throws ValidateException if the member is absent or another JSON type
     */
    private static String string(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException("Feishu requires a string member");
        }
        return text.value();
    }

    /**
     * Reads one optional non-blank JSON string member.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return non-blank value or {@code null} when absent
     * @throws ValidateException if a present value is blank or another JSON type
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue text) || text.value().isBlank()) {
            throw new ValidateException("Feishu optional string member must be non-blank");
        }
        return text.value();
    }

    /**
     * Reads one required exact integral JSON number.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return exact long value
     * @throws ValidateException if absent, non-integral, out of range, or another JSON type
     */
    private static long requiredLong(final JsonValue.ObjectValue object, final String name) {
        final Long value = optionalLong(object, name);
        if (value == null) {
            throw new ValidateException("Feishu requires an integral numeric member");
        }
        return value;
    }

    /**
     * Reads one required positive exact integral JSON number.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return positive long value
     * @throws ValidateException if absent or not positive
     */
    private static long requiredPositiveLong(final JsonValue.ObjectValue object, final String name) {
        final long value = requiredLong(object, name);
        if (value <= 0L) {
            throw new ValidateException("Feishu lifetime must be positive");
        }
        return value;
    }

    /**
     * Reads one optional exact integral JSON number.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return exact long or {@code null} when absent
     * @throws ValidateException if a present value is non-integral, out of range, or another JSON type
     */
    private static Long optionalLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Feishu numeric member has an invalid JSON type");
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException cause) {
            throw new ValidateException("Feishu numeric member must be an exact long", cause);
        }
    }

    /**
     * Creates one exact JSON integer value.
     *
     * @param value integral value
     * @return immutable JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
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
        return completed(rejected("Feishu capability is not declared by the selected manifest"));
    }

    /**
     * Creates a safe request-contract rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejected stage
     */
    private static <T> CompletionStage<Outcome<T>> mismatch() {
        return completed(rejected("Feishu request does not match the selected capability contract"));
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
     * @param details     non-sensitive numeric and error-token details
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
     * Returns the exact capability manifest frozen by the selected Feishu manifest.
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
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Feishu-private response models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Feishu capability must not be null");
        Assert.notNull(context, "Feishu invocation context must not be null");
        Assert.notNull(timeout, "Feishu invocation budget must not be null");
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
     * Builds the exact Feishu authorization redirect from generated state and S256 challenge.
     *
     * @param initiation generated browser correlation and PKCE challenge
     * @param context    immutable invocation context retained for operation consistency
     * @param timeout    shared end-to-end budget
     * @return exact redirect and state correlation
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "Feishu authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Feishu authorization has no remaining time budget"));
        }
        try {
            final var challenge = initiation.codeChallenge().getOrNull();
            if (initiation.nonce().isPresent() || challenge == null || !PkceMethod.S256.equals(challenge.method())) {
                return completed(
                        failed(ErrorCode._500, "Feishu browser flow did not generate the required S256 PKCE material"));
            }
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final var location = endpoint.url().newBuilder().query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull());
            if (!options.scopes().isEmpty()) {
                location.query(OAuth2.Parameters.SCOPE, String.join(Symbol.SPACE, options.scopes()));
            }
            final String redirect = location.query(OAuth2.Parameters.STATE, initiation.state())
                    .query(OAuth2.Parameters.CODE_CHALLENGE, challenge.value())
                    .query(OAuth2.Parameters.CODE_CHALLENGE_METHOD, PkceMethod.S256.value()).build().toString();
            return completed(Outcome.succeeded(new RedirectManager.Prepared(redirect, initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("Feishu authorization request is invalid"));
        }
    }

    /**
     * Extracts the unique state from one exact Feishu callback branch.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return unique non-blank state
     * @throws ValidateException if callback transport, target, branch, or multiplicity is invalid
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Exchanges one correlated Feishu callback and resolves the verified union identity.
     *
     * @param completion consumed callback correlation with the one-time PKCE verifier
     * @param context    immutable invocation context used for one secret resolution
     * @param timeout    shared end-to-end budget
     * @return verified Feishu identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Feishu authorization callback is invalid"));
        }
        if (values.denied()) {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400, "Feishu resource owner denied authorization",
                                    new JsonValue.ObjectValue(
                                            Map.of(Builder.OAUTH_ERROR, new JsonValue.StringValue("access_denied"))))));
        }
        if (completion.codeVerifier().isEmpty()) {
            return completed(failed(ErrorCode._500, "Feishu callback lacks its required PKCE verifier"));
        }
        final String verifier = completion.codeVerifier().getOrNull().value();
        return Outcome
                .mapStage(
                        () -> services.secretLoader()
                                .load(services.registration(), options.credential(), context, timeout),
                        loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(
                            values.code(),
                            verifier,
                            success.value(),
                            timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Executes the private Feishu token and profile requests under one owned secret lease.
     *
     * @param code     consumed authorization code
     * @param verifier consumed RFC 7636 verifier
     * @param secret   owned App Secret lease closed by this asynchronous operation
     * @param timeout  shared end-to-end budget
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> authenticate(
            final String code,
            final String verifier,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try (secret) {
                return switch (token(code, verifier, secret, timeout)) {
                    case Outcome.Succeeded<Access> success -> profile(success.value(), timeout);
                    case Outcome.Rejected<Access> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Access> failed -> Outcome.failed(failed.failure());
                };
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Feishu authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Executes the exact Feishu v3 JSON authorization-code request.
     *
     * @param code     sensitive one-time authorization code
     * @param verifier sensitive one-time RFC 7636 verifier
     * @param secret   still-open App Secret lease
     * @param timeout  shared end-to-end budget
     * @return private access result or safely classified Feishu failure
     */
    private Outcome<Access> token(
            final String code,
            final String verifier,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Feishu token request has no remaining time budget");
            }
            final Map<String, JsonValue> fields = new LinkedHashMap<>();
            fields.put(OAuth2.Parameters.GRANT_TYPE, new JsonValue.StringValue(GrantType.AUTHORIZATION_CODE.value()));
            fields.put(OAuth2.Parameters.CLIENT_ID, new JsonValue.StringValue(options.clientId()));
            fields.put(OAuth2.Parameters.CLIENT_SECRET, new JsonValue.StringValue(new String(secret.material())));
            fields.put("code", new JsonValue.StringValue(Assert.notBlank(code, "Feishu code must not be blank")));
            fields.put(OAuth2.Parameters.REDIRECT_URI, new JsonValue.StringValue(options.redirectUri().getOrNull()));
            fields.put(
                    OAuth2.Parameters.CODE_VERIFIER,
                    new JsonValue.StringValue(Assert.notBlank(verifier, "Feishu code verifier must not be blank")));
            body = services.jsonProvider().writeValue(new JsonValue.ObjectValue(fields));
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(body, MediaType.APPLICATION_JSON_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Feishu token request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Strictly decodes one Feishu v3 token success or error envelope.
     *
     * @param response owned token endpoint response
     * @return private access result or safely classified failure
     */
    private Outcome<Access> token(final HttpResponse response) {
        try {
            final JsonValue.ObjectValue object = object(response, "token");
            final long code = requiredLong(object, "code");
            if (code != 0L) {
                if (!tokenErrorMembers(object)) {
                    return failed(ErrorCode._502, "Feishu token error envelope is invalid");
                }
                return tokenError(response.code(), code, requiredString(object, OAuth2.Parameters.ERROR));
            }
            if (response.code() != Http.Status.OK || !tokenSuccessMembers(object)
                    || object.values().keySet().size() < 4) {
                return failed(ErrorCode._502, "Feishu token success envelope is invalid");
            }
            if (!object.values().containsKey("code") || !object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                    || !object.values().containsKey(OAuth2.Parameters.TOKEN_TYPE)
                    || !object.values().containsKey(OAuth2.Parameters.EXPIRES_IN)
                    || !TokenType.BEARER.value().equals(requiredString(object, OAuth2.Parameters.TOKEN_TYPE))) {
                return failed(ErrorCode._502, "Feishu token success fields are invalid");
            }
            final String accessToken = requiredString(object, OAuth2.Parameters.ACCESS_TOKEN);
            final long expiresIn = requiredPositiveLong(object, OAuth2.Parameters.EXPIRES_IN);
            final String refreshToken = optionalString(object, OAuth2.Parameters.REFRESH_TOKEN);
            final Long refreshExpiresIn = optionalLong(object, "refresh_token_expires_in");
            if (refreshExpiresIn != null && refreshExpiresIn <= 0L) {
                throw new ValidateException("Feishu refresh-token lifetime must be positive");
            }
            final String scopeText = optionalString(object, OAuth2.Parameters.SCOPE);
            final List<String> effectiveScopes = scopeText == null ? options.scopes() : Scope.parse(scopeText).values();
            if (refreshToken != null && !effectiveScopes.contains("offline_access")) {
                throw new ValidateException("Feishu refresh token requires offline_access scope");
            }
            return Outcome.succeeded(new Access(accessToken, expiresIn));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Feishu token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves the Feishu user profile using the private access result.
     *
     * @param access  private access-token result
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> profile(final Access access, final Timeout.Budget timeout) {
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Feishu profile request has no remaining time budget");
            }
            final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
            final MediaType json = MediaType.APPLICATION_JSON_TYPE.withCharset(Charset.UTF_8);
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET)
                    .header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + access.accessToken())
                    .header(Http.Header.CONTENT_TYPE, json.toString()).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return profile(response, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Feishu profile request failed");
        }
    }

    /**
     * Strictly decodes one Feishu profile envelope and maps its union identifier.
     *
     * @param response owned user-information response
     * @param timeout  shared clock used for evidence verification time
     * @return verified identity or safely classified Feishu failure
     */
    private Outcome<ExternalIdentity> profile(final HttpResponse response, final Timeout.Budget timeout) {
        try {
            final JsonValue.ObjectValue object = object(response, "profile");
            if (!profileMembers(object) || !object.values().containsKey("code")
                    || !object.values().containsKey("msg")) {
                return failed(ErrorCode._502, "Feishu profile envelope contains an unknown member");
            }
            final long code = requiredLong(object, "code");
            string(object, "msg");
            if (code != 0L) {
                return identityError(response.code(), code);
            }
            if (response.code() != Http.Status.OK || object.values().size() != 3 || !object.values().containsKey("data")
                    || !(object.values().get("data") instanceof JsonValue.ObjectValue data) || !userMembers(data)) {
                return failed(ErrorCode._502, "Feishu profile success envelope is invalid");
            }
            requiredString(data, "open_id");
            final String subject = requiredString(data, "union_id");
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            optionalAttribute(data, attributes, "name");
            optionalAttribute(data, attributes, "en_name");
            optionalAttribute(data, attributes, "avatar_url");
            optionalAttribute(data, attributes, "avatar_thumb");
            optionalAttribute(data, attributes, "avatar_middle");
            optionalAttribute(data, attributes, "avatar_big");
            optionalAttribute(data, attributes, "open_id");
            optionalAttribute(data, attributes, "email");
            optionalAttribute(data, attributes, "enterprise_email");
            optionalAttribute(data, attributes, "user_id");
            optionalAttribute(data, attributes, "mobile");
            optionalAttribute(data, attributes, "tenant_key");
            optionalAttribute(data, attributes, "employee_no");
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("feishu_union_id", new JsonValue.StringValue(subject), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Feishu profile endpoint returned an invalid response");
        }
    }

    /**
     * Validates one public standard authorization request against the Feishu registration.
     *
     * @param request standard OAuth authorization request
     * @return {@code true} only when client, redirect, scope, state, response, PKCE, and extensions are exact
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request.scope().getOrNull();
        final boolean scopeMatches = options.scopes().isEmpty() ? scope == null || scope.values().isEmpty()
                : scope != null && options.scopes().equals(scope.values());
        final Map<String, JsonValue> extensions = request.extensions().values();
        final boolean prompt = extensions.isEmpty() || extensions.size() == 1
                && extensions.get("prompt") instanceof JsonValue.StringValue value && "consent".equals(value.value());
        return ResponseType.CODE.equals(request.responseType()) && options.clientId().equals(request.clientId())
                && options.redirectUri().equals(request.redirectUri()) && scopeMatches && request.state().isPresent()
                && request.codeChallenge().isPresent()
                && PkceMethod.S256.value().equals(request.codeChallengeMethod().getOrNull()) && prompt;
    }

    /**
     * Validates and indexes one exact Feishu GET callback.
     *
     * @param callback raw inbound callback
     * @return typed exact callback branch
     * @throws ValidateException if target, transport, names, branches, or values violate the frozen contract
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Feishu callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Feishu callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        String error = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert.notBlank(parameter.value(), "Feishu callback value must not be blank");
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code, value);
                case OAuth2.Parameters.STATE -> state = unique(state, value);
                case OAuth2.Parameters.ERROR -> error = unique(error, value);
                default -> throw new ValidateException("Feishu callback contains an unsupported parameter");
            }
        }
        return new CallbackWire(code, state, error);
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
            throw new ValidateException("Feishu callback parameter names must be unique");
        }
        return value;
    }

    /**
     * Decodes one bounded Feishu JSON object for any HTTP status branch.
     *
     * @param response  response whose body remains open
     * @param operation safe operation label used only in validation messages
     * @return strict provider-neutral JSON object
     * @throws ValidateException if media, JSON shape, depth, or duplicate members are invalid
     */
    private JsonValue.ObjectValue object(final HttpResponse response, final String operation) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Feishu " + operation + " response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Feishu " + operation + " response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries one exact Feishu authorization callback branch.
     *
     * @param code  authorization code for a successful callback
     * @param state mandatory browser correlation value
     * @param error OAuth error for a denied callback
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state, String error) {

        /**
         * Validates the exact success or resource-owner denial branch.
         *
         * @throws IllegalArgumentException if the state is blank
         * @throws ValidateException        if values do not form one supported branch
         */
        private CallbackWire {
            Assert.notBlank(state, "Feishu callback state must not be blank");
            final boolean success = code != null && error == null;
            final boolean denial = code == null && "access_denied".equals(error);
            if (!success && !denial) {
                throw new ValidateException("Feishu callback must contain one exact success or denial branch");
            }
        }

        /**
         * Reports whether the callback represents resource-owner denial.
         *
         * @return {@code true} for the exact denial branch
         */
        private boolean denied() {
            return error != null;
        }

    }

    /**
     * Carries Feishu's private token success fields required by the immediate profile request.
     *
     * @param accessToken sensitive access token
     * @param expiresIn   positive access-token lifetime in seconds
     * @author Kimi Liu
     */
    private record Access(String accessToken, long expiresIn) {

        /**
         * Validates one private Feishu token success value.
         *
         * @throws IllegalArgumentException if the access token is blank
         * @throws ValidateException        if the lifetime is not positive
         */
        private Access {
            Assert.notBlank(accessToken, "Feishu private access token must not be blank");
            if (expiresIn <= 0L) {
                throw new ValidateException("Feishu private access-token lifetime must be positive");
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
