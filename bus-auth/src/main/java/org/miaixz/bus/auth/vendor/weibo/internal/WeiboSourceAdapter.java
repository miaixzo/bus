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
package org.miaixz.bus.auth.vendor.weibo.internal;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2SourceProfile;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationResponseDecoder;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.weibo.WeiboDefinition;
import org.miaixz.bus.auth.vendor.weibo.WeiboSourceSettings;
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
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements Weibo browser authentication and its registered revocation deviation.
 * <p>
 * OAuth authorization remains public. The token response lacks {@code token_type} and therefore stays inside the
 * Source-authentication chain; the Weibo user resource is never exposed as OpenID Connect UserInfo. Revocation accepts
 * only the standard request model and converts Weibo's JSON marker to the standard successful empty result.
 * </p>
 *
 * @author Kimi Liu
 */
public final class WeiboSourceAdapter implements VendorAdapter {

    /**
     * Trusted Weibo API authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://api.weibo.com";

    /**
     * Maximum bounded Weibo response size.
     */
    private static final long MAXIMUM_RESPONSE_BYTES = Normal.MEBI;

    /**
     * Maximum accepted Weibo JSON nesting.
     */
    private static final int MAXIMUM_JSON_DEPTH = 32;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Exact immutable Weibo definition.
     */
    private final VendorDefinition.Definition variantDefinition;

    /**
     * Validated externally loaded Weibo settings.
     */
    private final WeiboSourceSettings settings;

    /**
     * Caller-owned secret, JSON, network, clock, and execution dependencies.
     */
    private final ExecutionServices services;

    /**
     * Shared one-time browser-state lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Strict standard OAuth authorization callback decoder.
     */
    private final AuthorizationResponseDecoder callbackDecoder;

    /**
     * Uniform adapter that publishes public OAuth capabilities.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Creates one Source-bound Weibo adapter from the frozen default definition.
     *
     * @param namespaceId       registration namespace isolating browser state and credentials
     * @param sourceId          registered Source identifier
     * @param vendorDefinition  selected Weibo definition
     * @param variantDefinition exact selected default definition
     * @param settings          decoded externally loaded Weibo settings
     * @param services          caller-owned runtime dependencies
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, definition, settings, or routing differ from the frozen variant
     */
    public WeiboSourceAdapter(final String namespaceId, final String sourceId, final WeiboDefinition vendorDefinition,
            final VendorDefinition.Definition variantDefinition, final WeiboSourceSettings settings,
            final ExecutionServices services) {
        final WeiboDefinition selected = Assert.notNull(vendorDefinition, "Weibo definition must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Weibo Source id must not be blank");
        this.variantDefinition = Assert.notNull(variantDefinition, "Weibo definition must not be null");
        this.settings = Assert.notNull(settings, "Weibo settings must not be null");
        this.services = Assert.notNull(services, "Weibo execution services must not be null");
        if (!WeiboDefinition.ID.equals(selected.type())
                || !selected.variant(WeiboDefinition.DEFAULT).equals(variantDefinition)
                || !WeiboDefinition.DEFAULT.equals(variantDefinition.variant())
                || variantDefinition.protocol() != Protocol.OAUTH2 || !WeiboDefinition.ID.equals(settings.vendor())
                || !WeiboDefinition.DEFAULT.equals(settings.variant())) {
            throw new ValidateException("Weibo adapter requires the weibo/default OAuth 2.0 definition");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variantDefinition, settings, services);
        this.callbackDecoder = new AuthorizationResponseDecoder();
        this.standardAdapter = new StandardAdapter(variantDefinition, settings, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2SourceProfile.AUTHORIZATION,
                                (request, context, timeout) -> authorization(request)),
                        new StandardAdapter.Binding<>(OAuth2SourceProfile.REVOCATION,
                                (request, context, timeout) -> revoke(request, timeout))));
    }

    /**
     * Reads one required non-blank JSON string.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return required non-blank string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = optionalString(object, name);
        if (value == null) {
            throw new ValidateException("Weibo response requires a non-blank string member: " + name);
        }
        return value;
    }

    /**
     * Reads one optional non-blank JSON string.
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
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Weibo response member must be a non-blank string: " + name);
        }
        return string.value();
    }

    /**
     * Verifies every member of one selected private Weibo document by semantic document kind.
     *
     * @param kind   selected private document kind
     * @param object decoded Weibo object
     * @return whether every member is registered for the selected document
     */
    private static boolean members(final WireKind kind, final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            final boolean known = switch (kind) {
                case TOKEN -> switch (name) {
                    case OAuth2.Parameters.ERROR, OAuth2.Parameters.ERROR_DESCRIPTION, "error_code", "request", OAuth2.Parameters.ACCESS_TOKEN, "remind_in", OAuth2.Parameters.EXPIRES_IN, "uid", "isRealName" -> true;
                    default -> false;
                };
                case PROFILE -> switch (name) {
                    case OAuth2.Parameters.ERROR, "error_code", "request", "id", "idstr", "class", "screen_name", "name", "province", "city", "location", "description", "url", "profile_image_url", "profile_url", "domain", "weihao", "gender", "followers_count", "friends_count", "statuses_count", "favourites_count", "created_at", "following", "allow_all_act_msg", "geo_enabled", "verified", "verified_type", "remark", "status", "allow_all_comment", "avatar_large", "avatar_hd", "verified_reason", "follow_me", "online_status", "bi_followers_count", "lang", "star", "mbtype", "mbrank", "block_word", "block_app", "credit_score", "user_ability", "urank", "story_read_state", "vclub_member" -> true;
                    default -> false;
                };
                case REVOCATION -> switch (name) {
                    case "result", OAuth2.Parameters.ERROR, "error_code", "request" -> true;
                    default -> false;
                };
            };
            if (!known) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads a positive decimal identity from either JSON string or number form.
     *
     * @param object decoded response object
     * @param name   exact identity member name
     * @return canonical unpadded decimal identifier
     */
    private static String requiredIdentifier(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        final String lexical;
        if (value instanceof JsonValue.StringValue string) {
            lexical = string.value();
        } else if (value instanceof JsonValue.NumberValue number) {
            try {
                lexical = number.value().toBigIntegerExact().toString();
            } catch (ArithmeticException cause) {
                throw new ValidateException("Weibo identity must be an exact integer", cause);
            }
        } else {
            throw new ValidateException("Weibo response requires a decimal identity member: " + name);
        }
        if (!lexical.matches("[1-9][0-9]*")) {
            throw new ValidateException("Weibo identity must be positive unpadded decimal text");
        }
        return lexical;
    }

    /**
     * Reads one exact positive integral JSON number or decimal string.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return positive long value
     */
    private static long positiveLong(final JsonValue.ObjectValue object, final String name) {
        final long value = integral(object, name);
        if (value <= 0L) {
            throw new ValidateException("Weibo numeric member must be positive: " + name);
        }
        return value;
    }

    /**
     * Validates one optional non-negative integral JSON number or decimal string.
     *
     * @param object decoded response object
     * @param name   exact optional member name
     */
    private static void optionalNonNegativeLong(final JsonValue.ObjectValue object, final String name) {
        if (!object.values().containsKey(name) || object.values().get(name) instanceof JsonValue.NullValue) {
            return;
        }
        if (integral(object, name) < 0L) {
            throw new ValidateException("Weibo numeric member must not be negative: " + name);
        }
    }

    /**
     * Reads one exact integral JSON number or decimal string.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return exact long value
     */
    private static long integral(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        try {
            if (value instanceof JsonValue.NumberValue number) {
                return number.value().longValueExact();
            }
            if (value instanceof JsonValue.StringValue string && string.value().matches("0|[1-9][0-9]*")) {
                return Long.parseLong(string.value());
            }
        } catch (ArithmeticException | NumberFormatException cause) {
            throw new ValidateException("Weibo numeric member must be an exact long: " + name, cause);
        }
        throw new ValidateException("Weibo response requires an integral member: " + name);
    }

    /**
     * Validates one optional platform boolean.
     *
     * @param object decoded response object
     * @param name   exact optional member name
     */
    private static void optionalBoolean(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value != null && !(value instanceof JsonValue.NullValue) && !(value instanceof JsonValue.BooleanValue)) {
            throw new ValidateException("Weibo optional member must be boolean: " + name);
        }
    }

    /**
     * Classifies one HTTP status without exposing upstream response content.
     *
     * @param status      HTTP response status
     * @param description safe operation description
     * @param <T>         expected success type
     * @return rejected request or operational failure
     */
    private static <T> Outcome<T> status(final int status, final String description) {
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, description);
        }
        if (status >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(ErrorCode._502, description);
        }
        return rejected(description);
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
                new Outcome.Failure(ErrorCode._400, "Weibo authorization endpoint returned a standard error",
                        new JsonValue.ObjectValue(
                                Map.of("oauth_error", new JsonValue.StringValue(error.response().error().value())))));
    }

    /**
     * Narrows a delegated Source outcome through the declared capability response type.
     *
     * @param stage        delegated outcome stage
     * @param responseType declared successful response class
     * @param <S>          expected success type
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
     * Materializes one operation-scoped App Secret and clears the intermediate character buffer.
     *
     * @param lease open secret lease owned by the current operation
     * @return transient string required by the private query encoder
     */
    private static String secret(final SecretLease lease) {
        final char[] material = lease.material();
        try {
            return new String(material);
        } finally {
            Arrays.fill(material, '\0');
        }
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
     * Returns the exact frozen Weibo capability manifest.
     *
     * @return immutable Source authentication, authorization, and revocation capability set
     */
    @Override
    public Capability.Manifest manifest() {
        return variantDefinition.manifest();
    }

    /**
     * Routes Source authentication, OAuth authorization, or OAuth revocation.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing private Weibo models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Weibo capability must not be null");
        Assert.notNull(context, "Weibo invocation context must not be null");
        Assert.notNull(timeout, "Weibo invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Weibo capability is not declared"));
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
        return completed(rejected("Weibo capability request is invalid"));
    }

    /**
     * Builds the Weibo redirect around generated one-time state.
     *
     * @param initiation generated browser correlation material
     * @param context    immutable invocation context retained by the uniform signature
     * @param timeout    shared end-to-end budget
     * @return prepared authorization redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "Weibo authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "Weibo browser material violates the frozen definition"));
        }
        try {
            final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, settings.clientId(),
                    settings.redirectUri(), Optional.of(new Scope(settings.scopes())), Optional.of(initiation.state()),
                    Optional.empty(), Optional.empty(), emptyObject());
            return completed(
                    Outcome.succeeded(new RedirectManager.Prepared(authorize(request).toString(), initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("Weibo authorization request is invalid"));
        }
    }

    /**
     * Encodes one validated authorization request using Weibo's comma-delimited scope.
     *
     * @param request standard OAuth authorization request
     * @return exact Weibo authorization URL
     */
    private UnoUrl authorize(final AuthorizationRequest request) {
        final var endpoint = variantDefinition.targets().resolve(settings).authorization().getOrNull();
        return endpoint.url().newBuilder().query(OAuth2.Parameters.RESPONSE_TYPE, request.responseType().value())
                .query(OAuth2.Parameters.CLIENT_ID, request.clientId())
                .query(OAuth2.Parameters.REDIRECT_URI, request.redirectUri().getOrNull())
                .query(OAuth2.Parameters.STATE, request.state().getOrNull())
                .query(OAuth2.Parameters.SCOPE, String.join(Symbol.COMMA, request.scope().getOrNull().values()))
                .build();
    }

    /**
     * Applies the public OAuth authorization contract before encoding Weibo's registered scope representation.
     *
     * @param request standard OAuth authorization request
     * @return completed authorization URL outcome
     */
    private CompletionStage<Outcome<UnoUrl>> authorization(final AuthorizationRequest request) {
        try {
            return valid(request) ? completed(Outcome.succeeded(authorize(request)))
                    : completed(rejected("Weibo authorization request does not match the compiled Source"));
        } catch (RuntimeException cause) {
            return completed(rejected("Weibo authorization request is invalid"));
        }
    }

    /**
     * Validates one public standard authorization request against the registered Source.
     *
     * @param request standard OAuth authorization request
     * @return whether every public field matches the selected Source
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request.scope().getOrNull();
        return ResponseType.CODE.equals(request.responseType()) && settings.clientId().equals(request.clientId())
                && settings.redirectUri().equals(request.redirectUri()) && scope != null
                && settings.scopes().equals(scope.values()) && request.state().isPresent()
                && request.codeChallenge().isEmpty() && request.codeChallengeMethod().isEmpty()
                && request.extensions().values().isEmpty();
    }

    /**
     * Extracts required state from one strict standard callback branch.
     *
     * @param callback raw inbound callback
     * @return unique correlation state
     */
    private String state(final Callback.Inbound callback) {
        return switch (decode(callback)) {
            case AuthorizationResponseDecoder.Success success -> success.response().state()
                    .orElseThrow(() -> new ValidateException("Weibo authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Weibo authorization error requires state"));
        };
    }

    /**
     * Completes the correlated private token and profile chain.
     *
     * @param completion consumed browser correlation
     * @param context    immutable invocation context used for secret resolution
     * @param timeout    shared end-to-end budget
     * @return verified Weibo identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = decode(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Weibo authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(oauthError(error));
        }
        if (completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(failed(ErrorCode._500, "Weibo callback contains unexpected browser material"));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        if (response.scope().isPresent() || response.issuer().isPresent()
                || !response.extensions().values().isEmpty()) {
            return completed(rejected("Weibo callback contains unregistered success parameters"));
        }
        return resolve(context, timeout).thenCompose(resolved -> switch (resolved) {
            case Outcome.Succeeded<SecretLease> success -> authenticate(response.code(), success.value(), timeout);
            case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Executes private token and profile operations while owning the client-secret lease.
     *
     * @param code    consumed authorization code
     * @param secret  owned client-secret lease
     * @param timeout shared end-to-end budget
     * @return verified Weibo identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> authenticate(
            final String code,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            final Outcome<Access> token;
            try (secret) {
                token = token(code, secret, timeout);
            } catch (RuntimeException cause) {
                return WeiboSourceAdapter.<ExternalIdentity>failed(ErrorCode._502, "Weibo token operation failed");
            }
            return switch (token) {
                case Outcome.Succeeded<Access> success -> profile(success.value(), timeout);
                case Outcome.Rejected<Access> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<Access> failed -> Outcome.failed(failed.failure());
            };
        }, services.executor());
    }

    /**
     * Sends Weibo's query-bearing token POST with an empty form entity.
     *
     * @param code    consumed authorization code
     * @param secret  live client-secret lease
     * @param timeout shared end-to-end budget
     * @return private access result
     */
    private Outcome<Access> token(final String code, final SecretLease secret, final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Weibo token request has no remaining time budget");
        }
        try {
            final var endpoint = variantDefinition.targets().resolve(settings).token().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST)
                    .query(OAuth2.Parameters.CODE, Assert.notBlank(code, "Weibo authorization code must not be blank"))
                    .query(OAuth2.Parameters.CLIENT_ID, settings.clientId())
                    .query(OAuth2.Parameters.CLIENT_SECRET, secret(secret))
                    .query(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value())
                    .query(OAuth2.Parameters.REDIRECT_URI, settings.redirectUri().getOrNull())
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(Normal.EMPTY_BYTE_ARRAY, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Weibo token request failed");
        }
    }

    /**
     * Strictly decodes the private token document without inventing {@code token_type}.
     *
     * @param response owned token endpoint response
     * @return private access result or safely classified failure
     */
    private Outcome<Access> token(final HttpResponse response) {
        if (response.code() != Http.Status.OK) {
            return status(response.code(), "Weibo token endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue object = object(response, "token");
            if (!members(WireKind.TOKEN, object)) {
                throw new ValidateException("Weibo token response contains an unknown member");
            }
            if (object.values().containsKey(OAuth2.Parameters.ERROR)) {
                requiredString(object, OAuth2.Parameters.ERROR);
                optionalString(object, OAuth2.Parameters.ERROR_DESCRIPTION);
                optionalNonNegativeLong(object, "error_code");
                optionalString(object, "request");
                return rejected("Weibo token endpoint returned a platform error");
            }
            positiveLong(object, OAuth2.Parameters.EXPIRES_IN);
            optionalNonNegativeLong(object, "remind_in");
            optionalBoolean(object, "isRealName");
            return Outcome.succeeded(
                    new Access(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN),
                            requiredIdentifier(object, "uid")));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Weibo token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves the Weibo profile using its historical query and OAuth2 authorization header.
     *
     * @param access  private access token and token-bound UID
     * @param timeout shared end-to-end budget
     * @return verified identity
     */
    private Outcome<ExternalIdentity> profile(final Access access, final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Weibo profile request has no remaining time budget");
        }
        try {
            final var endpoint = variantDefinition.targets().resolve(settings).userInfo().getOrNull();
            final String authorization = "OAuth2 uid=" + access.userId() + "&access_token=" + access.accessToken();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET).query(OAuth2.Parameters.ACCESS_TOKEN, access.accessToken())
                    .query("uid", access.userId()).header(Http.Header.AUTHORIZATION, authorization)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return profile(response, access, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Weibo profile request failed");
        }
    }

    /**
     * Strictly maps one profile and binds its durable identifier to the token UID.
     *
     * @param response owned profile endpoint response
     * @param access   private token-bound UID
     * @param timeout  shared clock used for evidence timestamping
     * @return verified Weibo identity
     */
    private Outcome<ExternalIdentity> profile(
            final HttpResponse response,
            final Access access,
            final Timeout.Budget timeout) {
        if (response.code() != Http.Status.OK) {
            return status(response.code(), "Weibo profile endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue object = object(response, "profile");
            if (!members(WireKind.PROFILE, object)) {
                throw new ValidateException("Weibo profile response contains an unknown member");
            }
            if (object.values().containsKey(OAuth2.Parameters.ERROR)) {
                requiredString(object, OAuth2.Parameters.ERROR);
                optionalNonNegativeLong(object, "error_code");
                optionalString(object, "request");
                return rejected("Weibo profile endpoint returned a platform error");
            }
            final String subject = requiredIdentifier(object, "id");
            if (!access.userId().equals(subject)) {
                return rejected("Weibo profile id does not match token uid");
            }
            final String idString = optionalString(object, "idstr");
            if (idString != null && !subject.equals(idString)) {
                return rejected("Weibo profile idstr does not match id");
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            ProfileWire.decode(object).copyAttributes(attributes);
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("id", new JsonValue.StringValue(subject), AUTHORITY, timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Weibo profile endpoint returned an invalid response");
        }
    }

    /**
     * Adapts the standard revocation request to Weibo's GET query and JSON success marker.
     *
     * @param request standard RFC 7009 revocation request
     * @param timeout shared end-to-end budget
     * @return successful empty result only for exact {@code result=true}
     */
    private CompletionStage<Outcome<Void>> revoke(final RevocationRequest request, final Timeout.Budget timeout) {
        if (request.tokenTypeHint().isPresent()
                && !OAuth2.Parameters.ACCESS_TOKEN.equals(request.tokenTypeHint().getOrNull())) {
            return completed(rejected("Weibo revocation accepts only an access_token hint"));
        }
        return CompletableFuture.supplyAsync(() -> {
            if (timeout.expired()) {
                return WeiboSourceAdapter
                        .<Void>failed(ErrorCode._408, "Weibo revocation request has no remaining time budget");
            }
            try {
                final var endpoint = variantDefinition.targets().resolve(settings).revocation().getOrNull();
                try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                        .method(Http.Method.GET).query(OAuth2.Parameters.ACCESS_TOKEN, request.token())
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                        .execute()) {
                    if (response.code() != Http.Status.OK) {
                        return WeiboSourceAdapter.<Void>status(
                                response.code(),
                                "Weibo revocation endpoint rejected or failed the request");
                    }
                    final JsonValue.ObjectValue object = object(response, "revocation");
                    if (!members(WireKind.REVOCATION, object)) {
                        throw new ValidateException("Weibo revocation response contains an unknown member");
                    }
                    if (object.values().containsKey(OAuth2.Parameters.ERROR)) {
                        requiredString(object, OAuth2.Parameters.ERROR);
                        optionalNonNegativeLong(object, "error_code");
                        optionalString(object, "request");
                        return WeiboSourceAdapter.<Void>rejected("Weibo revocation endpoint returned a platform error");
                    }
                    if (object.values().size() != 1 || !object.values().containsKey("result")
                            || !(object.values().get("result") instanceof JsonValue.BooleanValue result)
                            || !result.value()) {
                        throw new ValidateException("Weibo revocation success must be result=true");
                    }
                    return Outcome.succeeded(null);
                }
            } catch (RuntimeException cause) {
                return WeiboSourceAdapter.<Void>failed(ErrorCode._502, "Weibo revocation request failed");
            }
        }, services.executor());
    }

    /**
     * Resolves one operation-scoped Weibo client secret with closed exception handling.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return secret resolution outcome stage
     */
    private CompletionStage<Outcome<SecretLease>> resolve(final Context context, final Timeout.Budget timeout) {
        try {
            final CompletionStage<Outcome<SecretLease>> stage = services.secretResolver()
                    .resolve(settings.credential(), context, timeout);
            if (stage == null) {
                return completed(failed(ErrorCode._502, "Weibo client-secret resolver returned no stage"));
            }
            return stage.handle(
                    (outcome, cause) -> cause == null && outcome != null ? outcome
                            : WeiboSourceAdapter
                                    .<SecretLease>failed(ErrorCode._502, "Weibo client-secret resolution failed"));
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "Weibo client-secret resolution failed"));
        }
    }

    /**
     * Validates callback ownership before strict standard response decoding.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     */
    private AuthorizationResponseDecoder.Decoded decode(final Callback.Inbound callback) {
        final Callback.Inbound inbound = Assert.notNull(callback, "Weibo callback must not be null");
        if (inbound.method() != Http.Method.GET || !settings.redirectUri().getOrNull().equals(inbound.requestUri())) {
            throw new ValidateException("Weibo callback URI or method is invalid");
        }
        return callbackDecoder.decode(inbound);
    }

    /**
     * Strictly reads one bounded duplicate-rejecting Weibo JSON object.
     *
     * @param response  response whose body remains owned by the caller
     * @param operation safe operation name used in validation failures
     * @return immutable provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final HttpResponse response, final String operation) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Weibo " + operation + " response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_RESPONSE_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Weibo " + operation + " response root must be an object");
        }
        return object;
    }

    /**
     * Identifies each private Weibo JSON document with a distinct member contract.
     */
    private enum WireKind {

        /**
         * Private access-token document.
         */
        TOKEN,

        /**
         * User resource document.
         */
        PROFILE,

        /**
         * Revocation result document.
         */
        REVOCATION

    }

    /**
     * Carries the retained non-sensitive Weibo profile projection.
     *
     * @param name         optional display name
     * @param profileImage optional profile-image URL
     * @param url          optional profile URL
     * @param profileUrl   optional profile path
     * @param screenName   optional screen name
     * @param location     optional location text
     * @param description  optional profile description
     * @param gender       optional platform gender code
     */
    private record ProfileWire(String name, String profileImage, String url, String profileUrl, String screenName,
            String location, String description, String gender) {

        /**
         * Decodes one member-validated Weibo profile object.
         *
         * @param object private profile response object
         * @return immutable typed profile projection
         */
        private static ProfileWire decode(final JsonValue.ObjectValue object) {
            return new ProfileWire(optionalString(object, "name"), optionalString(object, "profile_image_url"),
                    optionalString(object, "url"), optionalString(object, "profile_url"),
                    optionalString(object, "screen_name"), optionalString(object, "location"),
                    optionalString(object, "description"), optionalString(object, "gender"));
        }

        /**
         * Copies one optional string into the identity projection.
         *
         * @param attributes mutable destination owned by the identity mapper
         * @param name       exact Weibo wire member name
         * @param value      optional decoded value
         */
        private static void put(final Map<String, JsonValue> attributes, final String name, final String value) {
            if (value != null) {
                attributes.put(name, new JsonValue.StringValue(value));
            }
        }

        /**
         * Copies present profile attributes using their exact Weibo wire names.
         *
         * @param attributes mutable destination owned by the identity mapper
         */
        private void copyAttributes(final Map<String, JsonValue> attributes) {
            put(attributes, "name", name);
            put(attributes, "profile_image_url", profileImage);
            put(attributes, "url", url);
            put(attributes, "profile_url", profileUrl);
            put(attributes, "screen_name", screenName);
            put(attributes, "location", location);
            put(attributes, "description", description);
            put(attributes, "gender", gender);
        }

    }

    /**
     * Holds private Weibo token material and its token-bound user identifier.
     *
     * @param accessToken sensitive access token
     * @param userId      token-bound Weibo UID
     * @author Kimi Liu
     */
    private record Access(String accessToken, String userId) {

        /**
         * Validates private Weibo access material.
         *
         * @throws IllegalArgumentException if a component is blank
         */
        private Access {
            Assert.notBlank(accessToken, "Weibo access token must not be blank");
            Assert.notBlank(userId, "Weibo token UID must not be blank");
        }

    }

}
