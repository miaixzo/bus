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
package org.miaixz.bus.auth.vendor.slack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationResponseDecoder;
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
 * Implements Slack browser authentication and its registered OAuth-facing adaptations.
 * <p>
 * Standard authorization, token, and revocation models remain the only public protocol types. Slack's comma scope,
 * query-authenticated token call, Web API envelopes, user lookup, and Bearer revocation remain private wire details.
 * </p>
 *
 * @author Kimi Liu
 */
public class SlackSourceAdapter implements VendorAdapter {

    /**
     * Trusted Slack authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://slack.com";

    /**
     * Maximum bounded JSON response size accepted from Slack.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Maximum JSON nesting accepted from Slack.
     */
    private static final int MAXIMUM_JSON_DEPTH = Normal._64;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Slack manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Slack options.
     */
    private final SlackOptions options;

    /**
     * Caller-owned secret, JSON, network, clock, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Shared one-time browser-state lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Uniform adapter that owns every public OAuth capability dispatch.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Strict standard authorization callback decoder.
     */
    private final AuthorizationResponseDecoder callbackDecoder;

    /**
     * Creates one Source-bound Slack adapter from the frozen default manifest.
     *
     * @param namespaceId registration namespace isolating state and credentials
     * @param sourceId    registered Source identifier
     * @param manifest    selected Slack manifest
     * @param variant     exact selected default manifest
     * @param options     decoded externally loaded Slack options
     * @param services    caller-owned runtime dependencies
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, manifest, options, or routing differ from the frozen variant
     */
    public SlackSourceAdapter(final String namespaceId, final String sourceId, final SlackManifest manifest,
            final VariantManifest.Variant variant, final SlackOptions options, final DriverServices services) {
        final SlackManifest selected = Assert.notNull(manifest, "Slack manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Slack Source id must not be blank");
        this.variant = Assert.notNull(variant, "Slack manifest must not be null");
        this.options = Assert.notNull(options, "Slack options must not be null");
        this.services = Assert.notNull(services, "Slack execution services must not be null");
        if (!SlackManifest.ID.equals(selected.vendor()) || !selected.variant(SlackManifest.DEFAULT).equals(variant)
                || !SlackManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !SlackManifest.ID.equals(options.vendor()) || !SlackManifest.DEFAULT.equals(options.variant())) {
            throw new ValidateException("Slack adapter requires the slack/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.callbackDecoder = new AuthorizationResponseDecoder();
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION,
                                (request, context, timeout) -> authorization(request)),
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.TOKEN, this::token),
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.REVOCATION,
                                (request, context, timeout) -> revoke(request, timeout))));
    }

    /**
     * Materializes an operation-scoped client secret and clears the intermediate character buffer.
     *
     * @param lease open secret lease owned by the caller
     * @return transient string required by the HTTP query builder
     */
    private static String secret(final SecretLease lease) {
        final char[] material = lease.material();
        try {
            return new String(material);
        } finally {
            Arrays.fill(material, Symbol.C_NUL);
        }
    }

    /**
     * Verifies whether one member belongs to a selected Slack Web API document.
     *
     * @param kind selected private document kind
     * @param name exact decoded member name
     * @return whether the member has registered semantics
     */
    private static boolean member(final WireKind kind, final String name) {
        return switch (kind) {
            case TOKEN -> switch (name) {
                case "ok", OAuth2.Parameters.ERROR, "response_metadata", OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.SCOPE, OAuth2.Parameters.TOKEN_TYPE, "authed_user", "app_id", "team", "enterprise", "is_enterprise_install", "bot_user_id" -> true;
                default -> false;
            };
            case USER_RESPONSE -> switch (name) {
                case "ok", OAuth2.Parameters.ERROR, "response_metadata", "user" -> true;
                default -> false;
            };
            case USER -> switch (name) {
                case "id", "team_id", "name", "deleted", "color", "real_name", "tz", "tz_label", "tz_offset", "profile", "is_admin", "is_owner", "is_primary_owner", "is_restricted", "is_ultra_restricted", "is_bot", "is_app_user", "updated", "is_email_confirmed", "who_can_share_contact_card", "enterprise_user" -> true;
                default -> false;
            };
            case PROFILE -> switch (name) {
                case "title", "phone", "skype", "real_name", "real_name_normalized", "display_name", "display_name_normalized", "status_text", "status_emoji", "status_emoji_display_info", "status_expiration", "avatar_hash", "email", "first_name", "last_name", "image_24", "image_32", "image_48", "image_72", "image_192", "image_512", "image_1024", "image_original", "is_custom_image", "huddle_state", "huddle_state_expiration", "team" -> true;
                default -> false;
            };
            case REVOCATION -> switch (name) {
                case "ok", OAuth2.Parameters.ERROR, "response_metadata", "revoked" -> true;
                default -> false;
            };
        };
    }

    /**
     * Verifies all decoded members of one selected Slack Web API document.
     *
     * @param kind   selected private document kind
     * @param object decoded document object
     * @return whether every member has registered semantics
     */
    private static boolean members(final WireKind kind, final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            if (!member(kind, name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines whether Slack reports a token that RFC 7009 already treats as successfully invalidated.
     *
     * @param error exact Slack error identifier
     * @return whether revocation is idempotently complete
     */
    private static boolean alreadyInvalid(final String error) {
        return switch (error) {
            case "token_revoked", "invalid_auth", "not_authed", "account_inactive" -> true;
            default -> false;
        };
    }

    /**
     * Copies Slack's non-standard token success members into the standard extension object.
     *
     * @param object decoded token response object
     * @return immutable extension object retaining only documented non-null members
     */
    private static JsonValue.ObjectValue tokenExtensions(final JsonValue.ObjectValue object) {
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        copyValue(object, values, "authed_user");
        copyValue(object, values, "app_id");
        copyValue(object, values, "team");
        copyValue(object, values, "enterprise");
        copyValue(object, values, "is_enterprise_install");
        copyValue(object, values, "bot_user_id");
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Copies one present non-null JSON member without changing its Slack wire name or value.
     *
     * @param source      decoded Slack object
     * @param destination mutable extension destination
     * @param name        exact Slack member name
     */
    private static void copyValue(
            final JsonValue.ObjectValue source,
            final Map<String, JsonValue> destination,
            final String name) {
        final JsonValue value = source.values().get(name);
        if (value != null && !(value instanceof JsonValue.NullValue)) {
            destination.put(name, value);
        }
    }

    /**
     * Classifies one strict Slack Web API error envelope without retaining diagnostic messages.
     *
     * @param status      original HTTP status
     * @param object      decoded Slack envelope
     * @param description safe operation description
     * @param <T>         expected success type
     * @return rejected request or failed upstream outcome
     */
    private static <T> Outcome<T> slackFailure(
            final int status,
            final JsonValue.ObjectValue object,
            final String description) {
        final String error = requiredString(object, OAuth2.Parameters.ERROR);
        final Map<String, JsonValue> details = Map.of("slack_error", new JsonValue.StringValue(error));
        if (status == Http.Status.TOO_MANY_REQUESTS || "ratelimited".equals(error)) {
            return failed(ErrorCode._429, description, details);
        }
        if (status >= Http.Status.INTERNAL_SERVER_ERROR || "internal_error".equals(error) || "fatal_error".equals(error)
                || "request_timeout".equals(error)) {
            return failed(ErrorCode._502, description, details);
        }
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Extracts the authorized user identifier retained in token response extensions.
     *
     * @param token standard token response carrying the Slack extension object
     * @return required authorized user identifier
     */
    private static String userId(final TokenResponse token) {
        final JsonValue value = token.extensions().values().get("authed_user");
        if (!(value instanceof JsonValue.ObjectValue user)) {
            throw new ValidateException("Slack token extensions require authed_user");
        }
        return requiredString(user, "id");
    }

    /**
     * Decodes Slack's comma-delimited effective scope into the standard scope model.
     *
     * @param value comma-delimited Slack scope text
     * @return ordered standard scope model
     */
    private static Scope slackScope(final String value) {
        final String[] entries = value.split(Symbol.COMMA, -1);
        final ArrayList<String> scopes = new ArrayList<>(entries.length);
        for (String entry : entries) {
            final String scope = Assert.notBlank(entry, "Slack token scope member must not contain an empty value");
            Scope.parse(scope);
            if (scopes.contains(scope)) {
                throw new ValidateException("Slack token scope member must not contain duplicates");
            }
            scopes.add(scope);
        }
        return new Scope(scopes);
    }

    /**
     * Reads one required JSON object member.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return required object value
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.ObjectValue nested)) {
            throw new ValidateException("Slack response requires an object member: " + name);
        }
        return nested;
    }

    /**
     * Reads one required JSON boolean member.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return required boolean value
     */
    private static boolean requiredBoolean(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.BooleanValue bool)) {
            throw new ValidateException("Slack response requires a boolean member: " + name);
        }
        return bool.value();
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
            throw new ValidateException("Slack response requires a non-blank string member: " + name);
        }
        return value;
    }

    /**
     * Reads one optional JSON string member.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return string value or {@code null} when absent or explicit null
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Slack response member must be a JSON string: " + name);
        }
        return string.value();
    }

    /**
     * Copies one optional non-blank string attribute.
     *
     * @param source decoded Slack object
     * @param target verified identity attribute destination
     * @param name   exact member name
     */
    private static void copyString(
            final JsonValue.ObjectValue source,
            final Map<String, JsonValue> target,
            final String name) {
        final String value = optionalString(source, name);
        if (value != null) {
            if (value.isBlank()) {
                throw new ValidateException("Slack optional identity string must not be blank: " + name);
            }
            target.put(name, new JsonValue.StringValue(value));
        }
    }

    /**
     * Maps one standard authorization error to a safe rejected outcome.
     *
     * @param error decoded standard authorization error branch
     * @param <T>   expected success type
     * @return rejected outcome retaining only the standard error identifier
     */
    private static <T> Outcome<T> oauthError(final AuthorizationResponseDecoder.Error error) {
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400, "Slack authorization endpoint returned a standard error",
                        new JsonValue.ObjectValue(Map.of(
                                Builder.OAUTH_ERROR,
                                new JsonValue.StringValue(error.response().error().value())))));
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
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
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
     * Creates a safe operational failure with non-sensitive Slack error details.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param details     validated Slack error identifier only
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
     * Returns the exact frozen Slack capability manifest.
     *
     * @return immutable Source authentication and OAuth capability set
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and standard authorization, token, and revocation requests.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing private Slack models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "Slack capability must not be null");
        Assert.notNull(context, "Slack invocation context must not be null");
        Assert.notNull(timeout, "Slack invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Slack capability is not declared"));
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
        if (standardAdapter.manifest().capabilities().contains(capability)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return completed(rejected("Slack capability request is invalid"));
    }

    /**
     * Builds the Slack redirect around generated one-time state.
     *
     * @param initiation generated browser correlation material
     * @param context    immutable invocation context retained by the uniform signature
     * @param timeout    shared end-to-end timeout
     * @return prepared authorization redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(context, "Slack authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "Slack browser material violates the frozen manifest"));
        }
        try {
            final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                    options.redirectUri(), Optional.of(new Scope(effectiveScopes())), Optional.of(initiation.state()),
                    Optional.empty(), Optional.empty(), emptyObject());
            return standardAdapter.invoke(OAuth2ClientScheme.AUTHORIZATION, request, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<Url> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<Url> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<Url> failed -> Outcome.failed(failed.failure());
                        default -> throw new IllegalStateException("Unsupported Outcome implementation");
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Slack authorization request is invalid"));
        }
    }

    /**
     * Encodes one validated standard request using Slack's comma-delimited scope.
     *
     * @param request standard authorization request
     * @return asynchronous exact Slack authorization URL outcome
     */
    private CompletionStage<Outcome<Url>> authorization(final AuthorizationRequest request) {
        if (!valid(request)) {
            return completed(rejected("Slack authorization request differs from the registered Source"));
        }
        try {
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final Url location = endpoint.url().newBuilder().query(OAuth2.Parameters.CLIENT_ID, request.clientId())
                    .query(OAuth2.Parameters.STATE, request.state().getOrNull())
                    .query(OAuth2.Parameters.REDIRECT_URI, request.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.SCOPE, String.join(Symbol.COMMA, effectiveScopes())).build();
            return completed(Outcome.succeeded(location));
        } catch (RuntimeException cause) {
            return completed(rejected("Slack authorization request is invalid"));
        }
    }

    /**
     * Validates one public standard authorization request against the Slack registration.
     *
     * @param request standard OAuth authorization request
     * @return whether every public field matches the selected Source
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request.scope().getOrNull();
        return ResponseType.CODE.equals(request.responseType()) && options.clientId().equals(request.clientId())
                && options.redirectUri().equals(request.redirectUri()) && scope != null
                && effectiveScopes().equals(scope.values()) && request.state().isPresent()
                && request.codeChallenge().isEmpty() && request.codeChallengeMethod().isEmpty()
                && request.extensions().values().isEmpty();
    }

    /**
     * Extracts the required state from one strict standard callback branch.
     *
     * @param callback raw inbound callback
     * @return unique correlation state
     */
    private String state(final Callback.Inbound callback) {
        return switch (decode(callback)) {
            case AuthorizationResponseDecoder.Success success -> success.response().state()
                    .orElseThrow(() -> new ValidateException("Slack authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Slack authorization error requires state"));
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
    }

    /**
     * Completes the correlated token and user-profile chain.
     *
     * @param completion consumed browser correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified Slack identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = decode(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Slack authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(oauthError(error));
        }
        if (completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(failed(ErrorCode._500, "Slack callback contains unexpected browser material"));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        final TokenRequest request = new TokenRequest(
                new AuthorizationCodeGrant(response.code(), options.redirectUri(), Optional.empty(), Optional.empty()),
                emptyObject());
        return standardAdapter.invoke(OAuth2ClientScheme.TOKEN, request, context, timeout)
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<TokenEndpointResponse> success -> success
                            .value() instanceof TokenResponse token ? profile(token, timeout)
                                    : completed(rejected("Slack token endpoint returned a non-OAuth token response"));
                    case Outcome.Rejected<TokenEndpointResponse> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<TokenEndpointResponse> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Executes one supported standard authorization-code token request.
     *
     * @param request standard token request
     * @param context immutable invocation context used for secret resolution
     * @param timeout shared end-to-end timeout
     * @return standard token response or safely classified failure
     */
    private CompletionStage<Outcome<TokenResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout timeout) {
        if (!valid(request)) {
            return completed(rejected("Slack token request differs from the registered grant contract"));
        }
        try {
            final CompletionStage<Outcome<SecretLease>> stage = Outcome.mapStage(
                    () -> services.secretLoader().load(
                            new SecretLoader.Request(services.registration(), options.credential()),
                            context,
                            timeout),
                    loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded));
            if (stage == null) {
                return completed(failed(ErrorCode._502, "Slack secret loader returned no stage"));
            }
            return stage
                    .handle(
                            (outcome, cause) -> cause == null && outcome != null ? outcome
                                    : SlackSourceAdapter
                                            .<SecretLease>failed(ErrorCode._502, "Slack secret resolution failed"))
                    .thenCompose(outcome -> switch (outcome) {
                        case Outcome.Succeeded<SecretLease> success -> CompletableFuture.supplyAsync(() -> {
                            try (SecretLease secret = success.value()) {
                                return sendToken(request, secret, timeout);
                            } catch (RuntimeException cause) {
                                return failed(ErrorCode._502, "Slack token operation failed");
                            }
                        }, services.executor());
                        case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                        default -> throw new IllegalStateException("Unsupported Outcome implementation");
                    });
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "Slack secret resolution failed"));
        }
    }

    /**
     * Validates Slack's only public standard token request shape.
     *
     * @param request standard token request
     * @return whether the request is an exact authorization-code grant without PKCE or extensions
     */
    private boolean valid(final TokenRequest request) {
        if (request == null || !request.extensions().values().isEmpty()
                || !(request.grant() instanceof AuthorizationCodeGrant grant)) {
            return false;
        }
        final String clientId = grant.clientId().getOrNull();
        return options.redirectUri().equals(grant.redirectUri())
                && (clientId == null || options.clientId().equals(clientId)) && grant.codeVerifier().isEmpty();
    }

    /**
     * Sends Slack's historical credential-bearing GET token request.
     *
     * @param request validated standard token request
     * @param secret  open client-secret lease
     * @param timeout shared end-to-end timeout
     * @return standard token response with Slack extensions
     */
    private Outcome<TokenResponse> sendToken(
            final TokenRequest request,
            final SecretLease secret,
            final Timeout timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Slack token request has no remaining timeout");
        }
        try {
            final AuthorizationCodeGrant grant = (AuthorizationCodeGrant) request.grant();
            final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
            try (Response response = FabricX.http(services.fabric(), Protocol.OAUTH2, timeout).url(endpoint)
                    .method(Http.Method.GET).query(OAuth2.Parameters.CODE, grant.code())
                    .query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.CLIENT_SECRET, secret(secret))
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Slack token request failed");
        }
    }

    /**
     * Strictly maps one Slack token envelope to a standard token response.
     *
     * @param response owned token endpoint response
     * @return standard token response or safely classified Slack failure
     */
    private Outcome<TokenResponse> token(final Response response) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!members(WireKind.TOKEN, object)) {
                throw new ValidateException("Slack token response members are invalid");
            }
            if (!requiredBoolean(object, "ok")) {
                return slackFailure(response.code(), object, "Slack token endpoint rejected the request");
            }
            if (response.code() != Http.Status.OK) {
                throw new ValidateException("Slack token success has a non-success HTTP status");
            }
            final TokenType tokenType = new TokenType(requiredString(object, OAuth2.Parameters.TOKEN_TYPE));
            if (!TokenType.BEARER.equals(tokenType)) {
                throw new ValidateException("Slack access token must use the Bearer token type");
            }
            final JsonValue.ObjectValue authedUser = requiredObject(object, "authed_user");
            requiredString(authedUser, "id");
            final String scope = optionalString(object, OAuth2.Parameters.SCOPE);
            return Outcome.succeeded(
                    new TokenResponse(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN), tokenType,
                            Optional.empty(), Optional.empty(),
                            scope == null ? Optional.empty() : Optional.of(slackScope(scope)),
                            tokenExtensions(object)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Slack token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves the Slack user selected by the token envelope.
     *
     * @param token   standard token response carrying Slack extensions
     * @param timeout shared end-to-end timeout
     * @return verified Slack identity stage
     */
    private CompletionStage<Outcome<ExternalIdentity>> profile(final TokenResponse token, final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            if (!TokenType.BEARER.equals(token.tokenType())) {
                return SlackSourceAdapter
                        .<ExternalIdentity>rejected("Slack token response must use the Bearer token type");
            }
            final String userId;
            try {
                userId = userId(token);
            } catch (RuntimeException cause) {
                return SlackSourceAdapter
                        .<ExternalIdentity>failed(ErrorCode._502, "Slack token response lacks a valid authorized user");
            }
            if (timeout.expired()) {
                return SlackSourceAdapter
                        .<ExternalIdentity>failed(ErrorCode._408, "Slack profile request has no remaining timeout");
            }
            try {
                final String endpoint = variant.targets().resolve(options).userInfo().getOrNull().url().toString();
                try (Response response = FabricX.http(services.fabric(), Protocol.OAUTH2, timeout).url(endpoint)
                        .method(Http.Method.GET).query("user", userId)
                        .header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + token.accessToken())
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).execute()) {
                    return profile(response, userId, timeout);
                }
            } catch (RuntimeException cause) {
                return SlackSourceAdapter.<ExternalIdentity>failed(ErrorCode._502, "Slack profile request failed");
            }
        }, services.executor());
    }

    /**
     * Strictly maps one Slack user envelope and enforces token-to-profile subject binding.
     *
     * @param response       owned users.info response
     * @param expectedUserId authorized user identifier from the token envelope
     * @param timeout        shared clock used for evidence timestamping
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> profile(
            final Response response,
            final String expectedUserId,
            final Timeout timeout) {
        try {
            final JsonValue.ObjectValue root = object(response);
            if (!members(WireKind.USER_RESPONSE, root)) {
                throw new ValidateException("Slack user response members are invalid");
            }
            if (!requiredBoolean(root, "ok")) {
                return slackFailure(response.code(), root, "Slack users.info rejected the request");
            }
            if (response.code() != Http.Status.OK) {
                throw new ValidateException("Slack user success has a non-success HTTP status");
            }
            final JsonValue.ObjectValue user = requiredObject(root, "user");
            if (!members(WireKind.USER, user)) {
                throw new ValidateException("Slack user members are invalid");
            }
            final String subject = requiredString(user, "id");
            if (!expectedUserId.equals(subject)) {
                return rejected("Slack token and profile user identifiers differ");
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            copyString(user, attributes, "name");
            copyString(user, attributes, "real_name");
            final JsonValue profileValue = user.values().get("profile");
            if (profileValue != null && !(profileValue instanceof JsonValue.NullValue)) {
                if (!(profileValue instanceof JsonValue.ObjectValue profile) || !members(WireKind.PROFILE, profile)) {
                    throw new ValidateException("Slack profile members are invalid");
                }
                copyString(profile, attributes, "display_name");
                copyString(profile, attributes, "email");
                copyString(profile, attributes, "image_original");
            }
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("id", new JsonValue.StringValue(subject), AUTHORITY, timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Slack users.info returned an invalid response");
        }
    }

    /**
     * Executes Slack's Bearer GET revocation adaptation for one standard request.
     *
     * @param request standard RFC 7009 revocation request
     * @param timeout shared end-to-end timeout
     * @return empty standard revocation success or safely classified failure
     */
    private CompletionStage<Outcome<Void>> revoke(final RevocationRequest request, final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            if (timeout.expired()) {
                return SlackSourceAdapter
                        .<Void>failed(ErrorCode._408, "Slack revocation request has no remaining timeout");
            }
            try {
                final String endpoint = variant.targets().resolve(options).revocation().getOrNull().url().toString();
                try (Response response = FabricX.http(services.fabric(), Protocol.OAUTH2, timeout).url(endpoint)
                        .method(Http.Method.GET)
                        .header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + request.token())
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).execute()) {
                    return revoke(response);
                }
            } catch (RuntimeException cause) {
                return SlackSourceAdapter.<Void>failed(ErrorCode._502, "Slack revocation request failed");
            }
        }, services.executor());
    }

    /**
     * Strictly maps one Slack revocation envelope to RFC 7009 empty success semantics.
     *
     * @param response owned auth.revoke response
     * @return successful void outcome or safely classified failure
     */
    private Outcome<Void> revoke(final Response response) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!members(WireKind.REVOCATION, object)) {
                throw new ValidateException("Slack revocation response members are invalid");
            }
            final boolean ok = requiredBoolean(object, "ok");
            if (ok) {
                if (response.code() != Http.Status.OK || !requiredBoolean(object, "revoked")) {
                    throw new ValidateException("Slack revocation success branch is invalid");
                }
                return Outcome.succeeded(null);
            }
            final String error = requiredString(object, OAuth2.Parameters.ERROR);
            if (alreadyInvalid(error)) {
                return Outcome.succeeded(null);
            }
            return slackFailure(response.code(), object, "Slack auth.revoke rejected the request");
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Slack auth.revoke returned an invalid response");
        }
    }

    /**
     * Validates callback ownership before strict standard response decoding.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     */
    private AuthorizationResponseDecoder.Decoded decode(final Callback.Inbound callback) {
        final Callback.Inbound inbound = Assert.notNull(callback, "Slack callback must not be null");
        if (inbound.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(inbound.requestUri())) {
            throw new ValidateException("Slack callback URI or method is invalid");
        }
        return callbackDecoder.decode(inbound);
    }

    /**
     * Strictly reads one bounded Slack JSON object.
     *
     * @param response response whose body remains owned by the caller
     * @return immutable provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final Response response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Slack response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Slack response root must be a JSON object");
        }
        return object;
    }

    /**
     * Returns explicit scopes or the immutable manifest default.
     *
     * @return ordered effective Slack scopes
     */
    private List<String> effectiveScopes() {
        return options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
    }

    /**
     * Identifies each private Slack Web API response document validated by this adapter.
     */
    private enum WireKind {

        /**
         * OAuth token response envelope.
         */
        TOKEN,

        /**
         * Top-level users.info response envelope.
         */
        USER_RESPONSE,

        /**
         * Nested Slack user object.
         */
        USER,

        /**
         * Nested Slack user profile object.
         */
        PROFILE,

        /**
         * auth.revoke response envelope.
         */
        REVOCATION

    }

}
