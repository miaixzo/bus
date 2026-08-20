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
package org.miaixz.bus.auth.vendor.gitlab;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.Parameter;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.AuthorizationClient;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenResponseDecoder;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements the GitLab.com OAuth application and Source identity runtime.
 * <p>
 * Authorization, token, refresh-token grant, and revocation retain their standard OAuth request and response models.
 * The adapter handles only the registered GitLab form order, {@code created_at} extension, refresh redirect URI,
 * empty-JSON revocation success, and private REST current-user mapping.
 * </p>
 *
 * @author Kimi Liu
 */
public final class GitLabSourceAdapter implements VendorAdapter {

    /**
     * Trusted GitLab authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://gitlab.com";

    /**
     * Maximum accepted private GitLab JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum accepted private GitLab JSON nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable GitLab manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded GitLab options.
     */
    private final GitLabOptions options;

    /**
     * Caller-owned execution services.
     */
    private final ExecutionServices services;

    /**
     * Shared standard OAuth authorization implementation.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time browser-state and PKCE lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Shared strict UTF-8 form encoder.
     */
    private final FormCodec formCodec;

    /**
     * Shared strict standard token response decoder.
     */
    private final TokenResponseDecoder tokenResponseDecoder;

    /**
     * Creates one Source-bound GitLab.com adapter.
     *
     * @param namespaceId registration namespace used to isolate state and credential resolution
     * @param sourceId    registered Source identifier
     * @param manifest    selected GitLab manifest
     * @param variant     exact selected default manifest
     * @param options     decoded externally loaded GitLab options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, manifest, callback, or capabilities are inconsistent
     */
    public GitLabSourceAdapter(final String namespaceId, final String sourceId, final GitLabManifest manifest,
            final VariantManifest.Variant variant, final GitLabOptions options, final ExecutionServices services) {
        final GitLabManifest selected = Assert.notNull(manifest, "GitLab manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "GitLab Source id must not be blank");
        this.variant = Assert.notNull(variant, "GitLab manifest must not be null");
        this.options = Assert.notNull(options, "GitLab options must not be null");
        this.services = Assert.notNull(services, "GitLab execution services must not be null");
        if (!GitLabManifest.ID.equals(selected.vendor()) || !selected.variant(GitLabManifest.DEFAULT).equals(variant)
                || !GitLabManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !GitLabManifest.ID.equals(options.vendor()) || !GitLabManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("GitLab adapter requires the gitlab/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.formCodec = new FormCodec();
        this.tokenResponseDecoder = new TokenResponseDecoder(services.jsonProvider());
        final var targets = variant.targets().resolve(options);
        final OAuth2ClientOptions oauthSettings = new OAuth2ClientOptions(targets.authorization(), targets.token(),
                Optional.empty(), targets.revocation(), Optional.empty(), Optional.empty(), Optional.empty(),
                options.clientId(), Set.of(options.redirectUri().getOrNull()),
                Endpoint.Authentication.CLIENT_SECRET_POST, Optional.of(options.credential()), true, false);
        final AuthorizationClient authorizationClient = new AuthorizationClient(oauthSettings,
                new AuthorizationRequestEncoder(targets.authorization().getOrNull()));
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION, authorizationClient::authorize),
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.TOKEN, this::token),
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.REVOCATION, this::revoke)));
    }

    /**
     * Maps one strict standard OAuth token error using GitLab's frozen classification.
     *
     * @param error decoded error and original status
     * @return expected rejection or operational failure
     */
    private static Outcome<TokenResponse> tokenError(final TokenResponseDecoder.Error error) {
        final String value = error.response().error().value();
        final Map<String, JsonValue> details = Map
                .of("oauth_error", new JsonValue.StringValue(value), "status", number(error.status()));
        if (error.status() == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "GitLab token endpoint rate limited the request", details);
        }
        if (error.status() >= Http.Status.INTERNAL_SERVER_ERROR
                || OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE.equals(error.response().error())) {
            return failed(ErrorCode._502, "GitLab token endpoint returned an upstream error", details);
        }
        if (rejectedTokenError(error.response().error())) {
            final Errors code = error.status() == Http.Status.UNAUTHORIZED ? ErrorCode._401 : ErrorCode._400;
            return Outcome.rejected(
                    new Outcome.Failure(code, "GitLab token endpoint rejected the request",
                            new JsonValue.ObjectValue(details)));
        }
        return failed(ErrorCode._502, "GitLab token endpoint returned an unknown OAuth error", details);
    }

    /**
     * Classifies one standard OAuth error as an expected GitLab request rejection.
     *
     * @param error decoded standard OAuth error code
     * @return whether caller, client, grant, or authorization state caused the error
     */
    private static boolean rejectedTokenError(final OAuth2ErrorCode error) {
        return OAuth2ErrorCode.INVALID_REQUEST.equals(error) || OAuth2ErrorCode.INVALID_CLIENT.equals(error)
                || OAuth2ErrorCode.INVALID_GRANT.equals(error) || OAuth2ErrorCode.UNAUTHORIZED_CLIENT.equals(error)
                || OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE.equals(error) || OAuth2ErrorCode.INVALID_SCOPE.equals(error)
                || OAuth2ErrorCode.ACCESS_DENIED.equals(error) || OAuth2ErrorCode.UNSUPPORTED_TOKEN_TYPE.equals(error);
    }

    /**
     * Verifies one OAuth error envelope without mirroring its members in a collection.
     *
     * @param object decoded OAuth error object
     * @return whether it contains error and only optional standard diagnostic members
     */
    private static boolean errorMembers(final JsonValue.ObjectValue object) {
        if (!object.values().containsKey(OAuth2.Parameters.ERROR) || object.values().size() > 3) {
            return false;
        }
        for (String member : object.values().keySet()) {
            if (!OAuth2.Parameters.ERROR.equals(member) && !OAuth2.Parameters.ERROR_DESCRIPTION.equals(member)
                    && !OAuth2.Parameters.ERROR_URI.equals(member)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates one standard revocation request and its optional registered hint.
     *
     * @param request standard revocation request
     * @return whether the hint is absent, access_token, or refresh_token
     */
    private static boolean valid(final RevocationRequest request) {
        if (request == null) {
            return false;
        }
        final String hint = request.tokenTypeHint().getOrNull();
        return hint == null || OAuth2.Parameters.ACCESS_TOKEN.equals(hint)
                || OAuth2.Parameters.REFRESH_TOKEN.equals(hint);
    }

    /**
     * Reads one required non-blank string member.
     *
     * @param object decoded object
     * @param name   member name
     * @return non-blank string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue text) || text.value().isBlank()) {
            throw new ValidateException("GitLab requires a non-blank string member");
        }
        return text.value();
    }

    /**
     * Reads one required boolean member.
     *
     * @param object decoded object
     * @param name   member name
     * @return boolean value
     */
    private static boolean requiredBoolean(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.BooleanValue flag)) {
            throw new ValidateException("GitLab requires a boolean member");
        }
        return flag.value();
    }

    /**
     * Validates the official JSON type of one present GitLab current-user member.
     *
     * @param name  exact official member name
     * @param value decoded provider-neutral JSON value
     * @throws ValidateException if the member does not retain its registered GitLab JSON type
     */
    private static void profileType(final String name, final JsonValue value) {
        if ("id".equals(name)) {
            exactIntegral(value);
            return;
        }
        if (!profileMember(name)) {
            throw new ValidateException("GitLab profile contains an unknown member");
        }
        if (value instanceof JsonValue.NullValue) {
            if (requiredProfileString(name) || "locked".equals(name) || "bot".equals(name)) {
                throw new ValidateException("GitLab required profile member must not be null");
            }
            return;
        }
        final boolean valid = switch (name) {
            case "username", "name", OAuth2.Parameters.STATE, "avatar_url", "web_url", "created_at", "bio", "location", "public_email", "skype", "linkedin", "twitter", "discord", "website_url", "organization", "job_title", "pronouns", "local_time", "last_sign_in_at", "confirmed_at", "last_activity_on", "email", "current_sign_in_at", "commit_email", "note", "work_information" -> value instanceof JsonValue.StringValue;
            case "locked", "bot", "can_create_group", "can_create_project", "two_factor_enabled", "external", "private_profile", "is_admin", "using_license_seat" -> value instanceof JsonValue.BooleanValue;
            case "theme_id", "color_scheme_id", "projects_limit", "shared_runners_minutes_limit", "extra_shared_runners_minutes_limit", "namespace_id", "followers", "following" -> value instanceof JsonValue.NumberValue
                    && integral(value);
            case "identities", "scim_identities" -> value instanceof JsonValue.ArrayValue;
            case "created_by" -> value instanceof JsonValue.ObjectValue;
            default -> false;
        };
        if (!valid) {
            throw new ValidateException("GitLab profile member has an invalid JSON type");
        }
    }

    /**
     * Tests one name against the GitLab API v4 current-user vocabulary.
     *
     * @param name exact JSON member name
     * @return whether the member is registered by the current-user representation
     */
    private static boolean profileMember(final String name) {
        return switch (name) {
            case "username", "name", OAuth2.Parameters.STATE, "locked", "avatar_url", "web_url", "created_at", "bio", "location", "public_email", "skype", "linkedin", "twitter", "discord", "website_url", "organization", "job_title", "pronouns", "bot", "work_information", "local_time", "last_sign_in_at", "confirmed_at", "last_activity_on", "email", "theme_id", "color_scheme_id", "projects_limit", "current_sign_in_at", "identities", "can_create_group", "can_create_project", "two_factor_enabled", "external", "private_profile", "commit_email", "shared_runners_minutes_limit", "extra_shared_runners_minutes_limit", "scim_identities", "is_admin", "note", "using_license_seat", "namespace_id", "created_by", "followers", "following" -> true;
            default -> false;
        };
    }

    /**
     * Reports whether one GitLab profile string is mandatory and non-null.
     *
     * @param name exact GitLab profile member name
     * @return whether the member is mandatory
     */
    private static boolean requiredProfileString(final String name) {
        return "username".equals(name) || "name".equals(name) || OAuth2.Parameters.STATE.equals(name)
                || "avatar_url".equals(name) || "web_url".equals(name) || "created_at".equals(name);
    }

    /**
     * Reports whether one JSON number is exactly integral and representable as a long.
     *
     * @param value decoded JSON number candidate
     * @return {@code true} only for an exact integral long
     */
    private static boolean integral(final JsonValue value) {
        try {
            exactIntegral(value);
            return true;
        } catch (RuntimeException cause) {
            return false;
        }
    }

    /**
     * Reads one exact integral JSON number value.
     *
     * @param value decoded JSON value
     * @return exact long
     * @throws ValidateException if the value is not an exact integral long
     */
    private static long exactIntegral(final JsonValue value) {
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("GitLab profile member must be a JSON number");
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException cause) {
            throw new ValidateException("GitLab profile number must be an exact long", cause);
        }
    }

    /**
     * Reads one required positive exact integral member.
     *
     * @param object decoded object
     * @param name   member name
     * @return positive exact long
     */
    private static long exactPositiveLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("GitLab requires an integral numeric member");
        }
        try {
            final long result = number.value().longValueExact();
            if (result <= 0L) {
                throw new ValidateException("GitLab numeric member must be positive");
            }
            return result;
        } catch (ArithmeticException cause) {
            throw new ValidateException("GitLab numeric member must be an exact long", cause);
        }
    }

    /**
     * Classifies one HTTP status without copying its response body.
     *
     * @param status      exact HTTP status
     * @param description safe operation description
     * @param <T>         expected success type
     * @return rejected or failed outcome
     */
    private static <T> Outcome<T> status(final int status, final String description) {
        final Map<String, JsonValue> details = Map.of("status", number(status));
        if (status == Http.Status.TOO_MANY_REQUESTS || status >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(
                    status == Http.Status.TOO_MANY_REQUESTS ? ErrorCode._429 : ErrorCode._502,
                    description,
                    details);
        }
        if (status == Http.Status.UNAUTHORIZED || status == Http.Status.FORBIDDEN) {
            return Outcome.rejected(
                    new Outcome.Failure(status == Http.Status.UNAUTHORIZED ? ErrorCode._401 : ErrorCode._403,
                            description, new JsonValue.ObjectValue(details)));
        }
        return failed(ErrorCode._502, description, details);
    }

    /**
     * Creates one exact JSON integer.
     *
     * @param value integral value
     * @return immutable JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Creates one immutable empty JSON object.
     *
     * @return empty object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Narrows one delegated outcome through the declared capability response type.
     *
     * @param stage        delegated outcome stage
     * @param responseType declared response class
     * @param <S>          expected response type
     * @return type-safe delegated outcome
     */
    private static <S> CompletionStage<Outcome<S>> narrow(
            final CompletionStage<? extends Outcome<?>> stage,
            final Class<S> responseType) {
        return stage.thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<?> success -> Outcome
                    .succeeded(success.value() == null ? null : responseType.cast(success.value()));
            case Outcome.Rejected<?> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<?> failed -> Outcome.failed(failed.failure());
        });
    }

    /**
     * Creates one completed outcome stage.
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
     * @return completed rejection
     */
    private static <T> CompletionStage<Outcome<T>> missing() {
        return completed(rejected("GitLab capability is not declared by the selected manifest"));
    }

    /**
     * Creates a safe request-contract rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejection
     */
    private static <T> CompletionStage<Outcome<T>> mismatch() {
        return completed(rejected("GitLab request does not match the selected capability contract"));
    }

    /**
     * Creates an expected rejection.
     *
     * @param description safe description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, emptyObject()));
    }

    /**
     * Creates an operational failure without details.
     *
     * @param code        shared Bus error code
     * @param description safe description
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final Errors code, final String description) {
        return failed(code, description, Map.of());
    }

    /**
     * Creates an operational failure with non-sensitive details.
     *
     * @param code        shared Bus error code
     * @param description safe description
     * @param details     non-sensitive details
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
     * Returns the exact capability manifest frozen by the selected GitLab manifest.
     *
     * @return immutable Source, authorization, token, and revocation manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and the registered standard OAuth operations.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing GitLab-private identity models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "GitLab capability must not be null");
        Assert.notNull(context, "GitLab invocation context must not be null");
        Assert.notNull(timeout, "GitLab invocation budget must not be null");
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
     * Builds the exact GitLab authorization redirect from generated state and S256 challenge.
     *
     * @param initiation generated browser correlation and PKCE challenge
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return exact redirect and state correlation
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "GitLab authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "GitLab authorization has no remaining time budget"));
        }
        try {
            final var challenge = initiation.codeChallenge().getOrNull();
            if (initiation.nonce().isPresent() || challenge == null || !PkceMethod.S256.equals(challenge.method())) {
                return completed(failed(ErrorCode._500, "GitLab browser flow lacks required S256 PKCE material"));
            }
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final String location = endpoint.url().newBuilder().query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query(OAuth2.Parameters.STATE, initiation.state())
                    .query(OAuth2.Parameters.SCOPE, String.join(Symbol.SPACE, options.scopes()))
                    .query(OAuth2.Parameters.CODE_CHALLENGE, challenge.value())
                    .query(OAuth2.Parameters.CODE_CHALLENGE_METHOD, PkceMethod.S256.value()).build().toString();
            return completed(Outcome.succeeded(new RedirectManager.Prepared(location, initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("GitLab authorization request is invalid"));
        }
    }

    /**
     * Extracts the unique state from one exact GitLab callback branch.
     *
     * @param callback raw inbound callback
     * @return unique non-blank state
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Completes one correlated GitLab callback and resolves the verified current user.
     *
     * @param completion consumed callback and PKCE verifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return verified GitLab identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("GitLab authorization callback is invalid"));
        }
        if (values.failed()) {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400,
                                    "GitLab authorization endpoint returned a standard OAuth error",
                                    new JsonValue.ObjectValue(
                                            Map.of("oauth_error", new JsonValue.StringValue(values.error()))))));
        }
        if (completion.codeVerifier().isEmpty()) {
            return completed(failed(ErrorCode._500, "GitLab callback lacks its required PKCE verifier"));
        }
        final AuthorizationCodeGrant grant = new AuthorizationCodeGrant(values.code(), options.redirectUri(),
                Optional.of(options.clientId()), Optional.of(completion.codeVerifier().getOrNull().value()));
        final TokenRequest request = new TokenRequest(grant, emptyObject());
        return org.miaixz.bus.auth.runtime.LoadResult
                .parse(
                        services.secretLoader().load(options.credential(), context, timeout),
                        loaded -> services.secretParser().parse(options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(request, success.value(), timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Runs the private Source token and REST profile chain under one secret lease.
     *
     * @param request standard authorization-code token request
     * @param secret  owned Client Secret lease
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> authenticate(
            final TokenRequest request,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try (secret) {
                return switch (sendToken(request, secret, timeout)) {
                    case Outcome.Succeeded<TokenResponse> success -> profile(success.value(), timeout);
                    case Outcome.Rejected<TokenResponse> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<TokenResponse> failed -> Outcome.failed(failed.failure());
                };
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "GitLab authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Executes one public standard authorization-code or refresh-token grant.
     *
     * @param request standard token request
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return standard token response
     */
    private CompletionStage<Outcome<TokenResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        if (!valid(request)) {
            return completed(rejected("GitLab token request does not match the registered grant contract"));
        }
        return org.miaixz.bus.auth.runtime.LoadResult
                .parse(
                        services.secretLoader().load(options.credential(), context, timeout),
                        loaded -> services.secretParser().parse(options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> CompletableFuture.supplyAsync(() -> {
                        try (SecretLease secret = success.value()) {
                            return sendToken(request, secret, timeout);
                        } catch (RuntimeException cause) {
                            return failed(ErrorCode._502, "GitLab token operation failed");
                        }
                    }, services.executor());
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Sends one exact GitLab token form and decodes its standard response.
     *
     * @param request validated standard token request
     * @param secret  open Client Secret lease
     * @param timeout shared end-to-end budget
     * @return standard token response or safely classified failure
     */
    private Outcome<TokenResponse> sendToken(
            final TokenRequest request,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "GitLab token request has no remaining time budget");
            }
            body = formCodec.encode(tokenParameters(request, secret));
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            final HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute();
            return decoded(tokenResponseDecoder.decode(response));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "GitLab token endpoint returned an invalid response");
        } finally {
            clear(body);
        }
    }

    /**
     * Builds the exact ordered GitLab token form for one supported standard grant.
     *
     * @param request validated standard token request
     * @param secret  open Client Secret lease
     * @return ordered form parameters
     */
    private List<Parameter> tokenParameters(final TokenRequest request, final SecretLease secret) {
        if (request.grant() instanceof AuthorizationCodeGrant grant) {
            return List.of(
                    new Parameter(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()),
                    new Parameter(OAuth2.Parameters.CODE, grant.code()),
                    new Parameter(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()),
                    new Parameter(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                    new Parameter(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material())),
                    new Parameter(OAuth2.Parameters.CODE_VERIFIER, grant.codeVerifier().getOrNull()));
        }
        final RefreshTokenGrant grant = (RefreshTokenGrant) request.grant();
        return List.of(
                new Parameter(OAuth2.Parameters.GRANT_TYPE, GrantType.REFRESH_TOKEN.value()),
                new Parameter(OAuth2.Parameters.REFRESH_TOKEN, grant.refreshToken()),
                new Parameter(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                new Parameter(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material())),
                new Parameter(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()));
    }

    /**
     * Converts and constrains one decoded GitLab token response.
     *
     * @param decoded standard success or error branch
     * @return standard token outcome
     */
    private Outcome<TokenResponse> decoded(final TokenResponseDecoder.Decoded decoded) {
        return switch (decoded) {
            case TokenResponseDecoder.Success success -> success.response() instanceof TokenResponse token
                    ? validate(token)
                    : failed(ErrorCode._502, "GitLab token endpoint returned an unsupported success type");
            case TokenResponseDecoder.Error error -> tokenError(error);
        };
    }

    /**
     * Applies GitLab success requirements without changing the standard token model.
     *
     * @param response decoded standard token response
     * @return unchanged response or safe protocol failure
     */
    private Outcome<TokenResponse> validate(final TokenResponse response) {
        try {
            if (!TokenType.BEARER.equals(response.tokenType()) || response.expiresIn().isEmpty()
                    || response.expiresIn().getOrNull() <= 0L || response.refreshToken().isEmpty()
                    || response.extensions().values().size() != 1
                    || !response.extensions().values().containsKey("created_at")) {
                throw new ValidateException("GitLab token success fields are invalid");
            }
            exactPositiveLong(response.extensions(), "created_at");
            final Scope scope = response.scope().getOrNull();
            if (scope != null && !scope.values().contains("read_user")) {
                throw new ValidateException("GitLab token scope omits read_user");
            }
            return Outcome.succeeded(response);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "GitLab token success response violates the registered manifest");
        }
    }

    /**
     * Executes standard RFC 7009 revocation using GitLab's exact form and response variants.
     *
     * @param request standard revocation request
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return standard empty revocation success
     */
    private CompletionStage<Outcome<Void>> revoke(
            final RevocationRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        if (!valid(request)) {
            return completed(rejected("GitLab revocation token type hint is unsupported"));
        }
        return org.miaixz.bus.auth.runtime.LoadResult
                .parse(
                        services.secretLoader().load(options.credential(), context, timeout),
                        loaded -> services.secretParser().parse(options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> CompletableFuture.supplyAsync(() -> {
                        try (SecretLease secret = success.value()) {
                            return sendRevocation(request, secret, timeout);
                        } catch (RuntimeException cause) {
                            return failed(ErrorCode._502, "GitLab revocation operation failed");
                        }
                    }, services.executor());
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Sends one exact GitLab revocation form.
     *
     * @param request validated standard revocation request
     * @param secret  open Client Secret lease
     * @param timeout shared end-to-end budget
     * @return empty success or classified failure
     */
    private Outcome<Void> sendRevocation(
            final RevocationRequest request,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "GitLab revocation has no remaining time budget");
            }
            final List<Parameter> parameters = new ArrayList<>();
            parameters.add(new Parameter(OAuth2.Parameters.CLIENT_ID, options.clientId()));
            parameters.add(new Parameter(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material())));
            parameters.add(new Parameter("token", request.token()));
            final String hint = request.tokenTypeHint().getOrNull();
            if (hint != null) {
                parameters.add(new Parameter(OAuth2.Parameters.TOKEN_TYPE_HINT, hint));
            }
            body = formCodec.encode(parameters);
            final var endpoint = variant.targets().resolve(options).revocation().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return revocation(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "GitLab revocation request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Decodes GitLab's empty-byte or exact empty-JSON revocation success.
     *
     * @param response owned revocation response
     * @return empty success or safely classified failure
     */
    private Outcome<Void> revocation(final HttpResponse response) {
        if (response.code() == Http.Status.OK) {
            if (response.body().length() == 0L) {
                return Outcome.succeeded(null);
            }
            try {
                final JsonValue.ObjectValue object = object(response);
                return object.values().isEmpty() ? Outcome.succeeded(null)
                        : failed(ErrorCode._502, "GitLab revocation success body must be an empty JSON object");
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "GitLab revocation success response is invalid");
            }
        }
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!errorMembers(object)) {
                throw new ValidateException("GitLab revocation error envelope is invalid");
            }
            final OAuth2ErrorCode error = new OAuth2ErrorCode(requiredString(object, OAuth2.Parameters.ERROR));
            final Map<String, JsonValue> details = Map
                    .of("oauth_error", new JsonValue.StringValue(error.value()), "status", number(response.code()));
            if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
                return failed(ErrorCode._429, "GitLab revocation endpoint rate limited the request", details);
            }
            if (response.code() >= Http.Status.INTERNAL_SERVER_ERROR
                    || OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE.equals(error)) {
                return failed(ErrorCode._502, "GitLab revocation endpoint returned an upstream error", details);
            }
            return rejectedTokenError(error)
                    ? Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400, "GitLab revocation endpoint rejected the request",
                                    new JsonValue.ObjectValue(details)))
                    : failed(ErrorCode._502, "GitLab revocation endpoint returned an unknown OAuth error", details);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "GitLab revocation endpoint returned an invalid error response");
        }
    }

    /**
     * Retrieves and maps the private GitLab current-user resource.
     *
     * @param token   standard token response from the immediately preceding Source completion
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> profile(final TokenResponse token, final Timeout.Budget timeout) {
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "GitLab profile request has no remaining time budget");
            }
            final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET)
                    .header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + token.accessToken())
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                if (response.code() != Http.Status.OK) {
                    return status(response.code(), "GitLab current-user endpoint rejected or failed the request");
                }
                final JsonValue.ObjectValue object = object(response);
                final String subject = Long.toUnsignedString(exactPositiveLong(object, "id"));
                requiredString(object, "username");
                requiredString(object, "name");
                requiredString(object, OAuth2.Parameters.STATE);
                requiredString(object, "avatar_url");
                requiredString(object, "web_url");
                requiredString(object, "created_at");
                requiredBoolean(object, "locked");
                requiredBoolean(object, "bot");
                for (Map.Entry<String, JsonValue> member : object.values().entrySet()) {
                    profileType(member.getKey(), member.getValue());
                }
                final Map<String, JsonValue> attributes = new LinkedHashMap<>(object.values());
                attributes.remove("id");
                final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                        new Evidence.Claim("gitlab_user_id", new JsonValue.StringValue(subject), AUTHORITY,
                                timeout.clock().now()));
                return Outcome.succeeded(
                        new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes),
                                List.of(evidence)));
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "GitLab current-user endpoint returned an invalid response");
        }
    }

    /**
     * Validates one public standard token request.
     *
     * @param request standard token request
     * @return whether the request is an exact supported GitLab grant
     */
    private boolean valid(final TokenRequest request) {
        if (request == null || !request.extensions().values().isEmpty()) {
            return false;
        }
        if (request.grant() instanceof AuthorizationCodeGrant grant) {
            final String clientId = grant.clientId().getOrNull();
            return options.redirectUri().equals(grant.redirectUri())
                    && (clientId == null || options.clientId().equals(clientId)) && grant.codeVerifier().isPresent();
        }
        return request.grant() instanceof RefreshTokenGrant refresh && refresh.scope().isEmpty();
    }

    /**
     * Validates and indexes one exact GitLab GET callback branch.
     *
     * @param callback raw inbound callback
     * @return typed exact callback branch
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "GitLab callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("GitLab callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        String error = null;
        String errorDescription = null;
        String errorUri = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert.notBlank(parameter.value(), "GitLab callback value must not be blank");
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code, value);
                case OAuth2.Parameters.STATE -> state = unique(state, value);
                case OAuth2.Parameters.ERROR -> error = unique(error, value);
                case OAuth2.Parameters.ERROR_DESCRIPTION -> errorDescription = unique(errorDescription, value);
                case OAuth2.Parameters.ERROR_URI -> errorUri = unique(errorUri, value);
                default -> throw new ValidateException("GitLab callback contains an unsupported parameter");
            }
        }
        return new CallbackWire(code, state, error, errorDescription, errorUri);
    }

    /**
     * Accepts one callback value only once.
     *
     * @param current previous value, or {@code null}
     * @param value   newly decoded non-blank value
     * @return newly decoded value
     */
    private String unique(final String current, final String value) {
        if (current != null) {
            throw new ValidateException("GitLab callback parameter names must be unique");
        }
        return value;
    }

    /**
     * Decodes one bounded GitLab JSON object.
     *
     * @param response response whose body remains open
     * @return strict provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final HttpResponse response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("GitLab response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("GitLab response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries one exact GitLab authorization callback branch.
     *
     * @param code             authorization code
     * @param state            browser correlation value
     * @param error            OAuth error
     * @param errorDescription optional OAuth error description
     * @param errorUri         optional OAuth error URI
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state, String error, String errorDescription, String errorUri) {

        /**
         * Validates one exact success or OAuth error branch.
         */
        private CallbackWire {
            Assert.notBlank(state, "GitLab callback state must not be blank");
            final boolean success = code != null && error == null && errorDescription == null && errorUri == null;
            final boolean failure = code == null && error != null;
            if (!success && !failure) {
                throw new ValidateException("GitLab callback has an invalid success or OAuth error branch");
            }
        }

        /**
         * Reports whether the callback carries an OAuth error.
         *
         * @return {@code true} for an error callback
         */
        private boolean failed() {
            return error != null;
        }

    }

}
