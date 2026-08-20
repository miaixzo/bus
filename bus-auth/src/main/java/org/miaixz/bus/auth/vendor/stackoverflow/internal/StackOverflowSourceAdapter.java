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
package org.miaixz.bus.auth.vendor.stackoverflow.internal;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.Parameter;
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
import org.miaixz.bus.auth.vendor.stackoverflow.StackOverflowDefinition;
import org.miaixz.bus.auth.vendor.stackoverflow.StackOverflowSourceSettings;
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
 * Implements Stack Overflow browser authentication and its standard authorization operation.
 * <p>
 * The public boundary exposes the RFC 6749 authorization request only. The comma-delimited scope, duplicated query/form
 * token request, token response without {@code token_type}, and Stack Exchange {@code /me} envelope are confined to the
 * Source-authentication completion chain.
 * </p>
 *
 * @author Kimi Liu
 */
public final class StackOverflowSourceAdapter implements VendorAdapter {

    /**
     * Trusted Stack Overflow authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://stackoverflow.com";

    /**
     * Maximum bounded JSON response size accepted from Stack Overflow and Stack Exchange.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum JSON nesting accepted from Stack Overflow and Stack Exchange.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Stack Overflow definition.
     */
    private final VendorDefinition.Definition variantDefinition;

    /**
     * Validated externally loaded Stack Overflow settings.
     */
    private final StackOverflowSourceSettings settings;

    /**
     * Caller-owned secret, JSON, network, clock, and execution dependencies.
     */
    private final ExecutionServices services;

    /**
     * Shared one-time browser-state lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Uniform adapter that owns the public OAuth authorization capability.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Strict standard OAuth authorization callback decoder.
     */
    private final AuthorizationResponseDecoder callbackDecoder;

    /**
     * Shared strict form encoder for the private token operation.
     */
    private final FormCodec formCodec;

    /**
     * Creates one Source-bound Stack Overflow adapter from the frozen default definition.
     *
     * @param namespaceId       registration namespace isolating browser state and credentials
     * @param sourceId          registered Source identifier
     * @param vendorDefinition  selected Stack Overflow definition
     * @param variantDefinition exact selected default definition
     * @param settings          decoded externally loaded Stack Overflow settings
     * @param services          caller-owned runtime dependencies
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, definition, settings, or routing differ from the frozen variant
     */
    public StackOverflowSourceAdapter(final String namespaceId, final String sourceId,
            final StackOverflowDefinition vendorDefinition, final VendorDefinition.Definition variantDefinition,
            final StackOverflowSourceSettings settings, final ExecutionServices services) {
        final StackOverflowDefinition selected = Assert
                .notNull(vendorDefinition, "Stack Overflow definition must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Stack Overflow Source id must not be blank");
        this.variantDefinition = Assert.notNull(variantDefinition, "Stack Overflow definition must not be null");
        this.settings = Assert.notNull(settings, "Stack Overflow settings must not be null");
        this.services = Assert.notNull(services, "Stack Overflow execution services must not be null");
        if (!StackOverflowDefinition.ID.equals(selected.type())
                || !selected.variant(StackOverflowDefinition.DEFAULT).equals(variantDefinition)
                || !StackOverflowDefinition.DEFAULT.equals(variantDefinition.variant())
                || variantDefinition.protocol() != Protocol.OAUTH2
                || !StackOverflowDefinition.ID.equals(settings.vendor())
                || !StackOverflowDefinition.DEFAULT.equals(settings.variant())) {
            throw new ValidateException(
                    "Stack Overflow adapter requires the stackoverflow/default OAuth 2.0 definition");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variantDefinition, settings, services);
        this.callbackDecoder = new AuthorizationResponseDecoder();
        this.formCodec = new FormCodec();
        this.standardAdapter = new StandardAdapter(variantDefinition, settings, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2SourceProfile.AUTHORIZATION,
                                (request, context, timeout) -> authorization(request))));
    }

    /**
     * Materializes an operation-scoped client secret and clears the intermediate character buffer.
     *
     * @param lease open secret lease owned by the caller
     * @return transient string required by the private request encoder
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
     * Verifies whether one member belongs to a selected private Stack Exchange document.
     *
     * @param kind selected document kind
     * @param name exact decoded member name
     * @return whether the member has registered semantics
     */
    private static boolean member(final WireKind kind, final String name) {
        return switch (kind) {
            case TOKEN -> switch (name) {
                case OAuth2.Parameters.ACCESS_TOKEN, "expires" -> true;
                default -> false;
            };
            case RESPONSE -> switch (name) {
                case "items", "has_more", "quota_max", "quota_remaining", "backoff", "type" -> true;
                default -> false;
            };
            case USER -> switch (name) {
                case "user_id", "user_type", "creation_date", "display_name", "profile_image", "link", "website_url", "location", "account_id", "is_employee", "last_access_date", "reputation", "reputation_change_day", "reputation_change_week", "reputation_change_month", "reputation_change_quarter", "reputation_change_year", "accept_rate", "badge_counts", "view_count", "down_vote_count", "up_vote_count", "answer_count", "question_count", "timed_penalty_date", "last_modified_date" -> true;
                default -> false;
            };
        };
    }

    /**
     * Verifies every decoded member of one selected private Stack Exchange document.
     *
     * @param kind   selected document kind
     * @param object decoded object
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
     * Reads one required non-empty JSON array member.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return required array value
     */
    private static JsonValue.ArrayValue requiredArray(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.ArrayValue array) || array.values().isEmpty()) {
            throw new ValidateException("Stack Exchange response requires a non-empty array member: " + name);
        }
        return array;
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
            throw new ValidateException("Stack Overflow response requires a non-blank string member: " + name);
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
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Stack Overflow response member must be a non-blank string: " + name);
        }
        return string.value();
    }

    /**
     * Reads one exact positive integral JSON number.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return positive long value
     */
    private static long requiredPositiveLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Stack Overflow response requires a numeric member: " + name);
        }
        try {
            final long exact = number.value().longValueExact();
            if (exact <= 0L) {
                throw new ValidateException("Stack Overflow numeric member must be positive: " + name);
            }
            return exact;
        } catch (ArithmeticException cause) {
            throw new ValidateException("Stack Overflow numeric member must be an exact long: " + name, cause);
        }
    }

    /**
     * Reads one optional exact positive integral JSON number.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return positive long value or {@code null} when absent or explicit null
     */
    private static Long optionalPositiveLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        return requiredPositiveLong(object, name);
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
                new Outcome.Failure(ErrorCode._400, "Stack Overflow authorization endpoint returned a standard error",
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
     * Returns the exact frozen Stack Overflow capability manifest.
     *
     * @return immutable Source authentication and OAuth authorization capability set
     */
    @Override
    public Capability.Manifest manifest() {
        return variantDefinition.manifest();
    }

    /**
     * Routes Source authentication and the standard OAuth authorization operation.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing private Stack Overflow models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Stack Overflow capability must not be null");
        Assert.notNull(context, "Stack Overflow invocation context must not be null");
        Assert.notNull(timeout, "Stack Overflow invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Stack Overflow capability is not declared"));
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
        return completed(rejected("Stack Overflow capability request is invalid"));
    }

    /**
     * Builds the Stack Overflow redirect around generated one-time state.
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
        Assert.notNull(context, "Stack Overflow authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "Stack Overflow browser material violates the frozen definition"));
        }
        try {
            final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, settings.clientId(),
                    settings.redirectUri(), Optional.of(new Scope(effectiveScopes())), Optional.of(initiation.state()),
                    Optional.empty(), Optional.empty(), emptyObject());
            return standardAdapter.invoke(OAuth2SourceProfile.AUTHORIZATION, request, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<UnoUrl> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<UnoUrl> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<UnoUrl> failed -> Outcome.failed(failed.failure());
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Stack Overflow authorization request is invalid"));
        }
    }

    /**
     * Encodes one validated request using Stack Overflow's comma-delimited scope.
     *
     * @param request standard authorization request
     * @return asynchronous exact Stack Overflow authorization URL outcome
     */
    private CompletionStage<Outcome<UnoUrl>> authorization(final AuthorizationRequest request) {
        if (!valid(request)) {
            return completed(rejected("Stack Overflow authorization request differs from the registered Source"));
        }
        try {
            final var endpoint = variantDefinition.targets().resolve(settings).authorization().getOrNull();
            final UnoUrl location = endpoint.url().newBuilder()
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query(OAuth2.Parameters.CLIENT_ID, request.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, request.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.STATE, request.state().getOrNull())
                    .query(OAuth2.Parameters.SCOPE, String.join(Symbol.COMMA, effectiveScopes())).build();
            return completed(Outcome.succeeded(location));
        } catch (RuntimeException cause) {
            return completed(rejected("Stack Overflow authorization request is invalid"));
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
                && effectiveScopes().equals(scope.values()) && request.state().isPresent()
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
                    .orElseThrow(() -> new ValidateException("Stack Overflow authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Stack Overflow authorization error requires state"));
        };
    }

    /**
     * Completes the correlated private token and profile chain.
     *
     * @param completion consumed browser correlation
     * @param context    immutable invocation context used for secret resolution
     * @param timeout    shared end-to-end budget
     * @return verified Stack Overflow identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = decode(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Stack Overflow authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(oauthError(error));
        }
        if (completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(failed(ErrorCode._500, "Stack Overflow callback contains unexpected browser material"));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        try {
            final CompletionStage<Outcome<SecretLease>> resolution = services.secretResolver()
                    .resolve(settings.credential(), context, timeout);
            if (resolution == null) {
                return completed(failed(ErrorCode._502, "Stack Overflow secret resolver returned no stage"));
            }
            return resolution
                    .handle(
                            (outcome, cause) -> cause == null && outcome != null ? outcome
                                    : StackOverflowSourceAdapter.<SecretLease>failed(
                                            ErrorCode._502,
                                            "Stack Overflow secret resolution failed"))
                    .thenCompose(outcome -> switch (outcome) {
                        case Outcome.Succeeded<SecretLease> success -> authenticate(
                                response.code(),
                                success.value(),
                                timeout);
                        case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    });
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "Stack Overflow secret resolution failed"));
        }
    }

    /**
     * Executes private token and profile operations while owning the client-secret lease.
     *
     * @param code    consumed authorization code
     * @param secret  owned client-secret lease
     * @param timeout shared end-to-end budget
     * @return verified Stack Overflow external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> authenticate(
            final String code,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try (secret) {
                return switch (token(code, secret, timeout)) {
                    case Outcome.Succeeded<Access> success -> profile(success.value(), timeout);
                    case Outcome.Rejected<Access> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Access> failed -> Outcome.failed(failed.failure());
                };
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Stack Overflow authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Sends the historical query-bearing form POST without publishing a standard token result.
     *
     * @param code    consumed authorization code
     * @param secret  open client-secret lease
     * @param timeout shared end-to-end budget
     * @return private access result or safely classified failure
     */
    private Outcome<Access> token(final String code, final SecretLease secret, final Timeout.Budget timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Stack Overflow token request has no remaining time budget");
            }
            final String clientSecret = secret(secret);
            final List<Parameter> parameters = List.of(
                    new Parameter(OAuth2.Parameters.CODE,
                            Assert.notBlank(code, "Stack Overflow authorization code must not be blank")),
                    new Parameter(OAuth2.Parameters.CLIENT_ID, settings.clientId()),
                    new Parameter(OAuth2.Parameters.CLIENT_SECRET, clientSecret),
                    new Parameter(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()),
                    new Parameter(OAuth2.Parameters.REDIRECT_URI, settings.redirectUri().getOrNull()));
            body = formCodec.encode(parameters);
            final var endpoint = variantDefinition.targets().resolve(settings).token().getOrNull();
            final var request = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST);
            for (Parameter parameter : parameters) {
                request.query(parameter.name(), parameter.value());
            }
            try (HttpResponse response = request.header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Stack Overflow token request failed");
        } finally {
            if (body != null) {
                Arrays.fill(body, (byte) 0);
            }
        }
    }

    /**
     * Strictly decodes a private Stack Overflow token success without inventing {@code token_type}.
     *
     * @param response owned token endpoint response
     * @return private access result or safely classified failure
     */
    private Outcome<Access> token(final HttpResponse response) {
        if (response.code() < Http.Status.OK || response.code() >= Http.Status.MULTIPLE_CHOICES) {
            return status(response.code(), "Stack Overflow token endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue object = object(response, "token");
            if (!members(WireKind.TOKEN, object)) {
                return failed(ErrorCode._502, "Stack Overflow token success response is invalid");
            }
            final Long expiresIn = optionalPositiveLong(object, "expires");
            if (expiresIn == null && !effectiveScopes().contains("no_expiry")) {
                return failed(ErrorCode._502, "Stack Overflow expiring token response omits expires");
            }
            return Outcome.succeeded(
                    new Access(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN), Optional.ofNullable(expiresIn)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Stack Overflow token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves the authenticated Stack Exchange user with the registered key and site.
     *
     * @param access  private access result
     * @param timeout shared end-to-end budget
     * @return verified identity or safely classified failure
     */
    private Outcome<ExternalIdentity> profile(final Access access, final Timeout.Budget timeout) {
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Stack Exchange profile request has no remaining time budget");
            }
            final var endpoint = variantDefinition.targets().resolve(settings).userInfo().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET).query(OAuth2.Parameters.ACCESS_TOKEN, access.accessToken())
                    .query("key", settings.key()).query("site", settings.siteId())
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return profile(response, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Stack Exchange profile request failed");
        }
    }

    /**
     * Strictly decodes one Stack Exchange {@code /me} envelope and maps its stable numeric user identifier.
     *
     * @param response owned profile response
     * @param timeout  shared clock used for evidence timestamping
     * @return verified Stack Overflow identity
     */
    private Outcome<ExternalIdentity> profile(final HttpResponse response, final Timeout.Budget timeout) {
        if (response.code() != Http.Status.OK) {
            return status(response.code(), "Stack Exchange profile endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue root = object(response, "profile");
            if (!members(WireKind.RESPONSE, root)) {
                return failed(ErrorCode._502, "Stack Exchange profile envelope contains an unknown member");
            }
            final JsonValue.ArrayValue items = requiredArray(root, "items");
            if (items.values().size() != 1 || !(items.values().get(0) instanceof JsonValue.ObjectValue user)
                    || !members(WireKind.USER, user)) {
                return rejected("Stack Exchange profile must contain exactly one registered user item");
            }
            final String subject = Long.toString(requiredPositiveLong(user, "user_id"));
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            ProfileWire.decode(user).copyAttributes(attributes);
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("user_id", new JsonValue.StringValue(subject), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Stack Exchange profile endpoint returned an invalid response");
        }
    }

    /**
     * Validates callback ownership before strict standard response decoding.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     */
    private AuthorizationResponseDecoder.Decoded decode(final Callback.Inbound callback) {
        final Callback.Inbound inbound = Assert.notNull(callback, "Stack Overflow callback must not be null");
        if (inbound.method() != Http.Method.GET || !settings.redirectUri().getOrNull().equals(inbound.requestUri())) {
            throw new ValidateException("Stack Overflow callback URI or method is invalid");
        }
        return callbackDecoder.decode(inbound);
    }

    /**
     * Strictly reads one bounded JSON object response.
     *
     * @param response  response whose body remains owned by the caller
     * @param operation safe operation name used in validation failures
     * @return immutable provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final HttpResponse response, final String operation) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Stack Overflow " + operation + " response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Stack Overflow " + operation + " response root must be an object");
        }
        return object;
    }

    /**
     * Returns explicit scopes or the immutable definition default.
     *
     * @return ordered effective Stack Overflow scopes
     */
    private List<String> effectiveScopes() {
        return settings.scopes().isEmpty() ? variantDefinition.defaultScopes() : settings.scopes();
    }

    /**
     * Identifies each private Stack Overflow or Stack Exchange response document validated by this adapter.
     */
    private enum WireKind {

        /**
         * Private Stack Overflow token response.
         */
        TOKEN,

        /**
         * Stack Exchange API response envelope.
         */
        RESPONSE,

        /**
         * Stack Exchange user item.
         */
        USER

    }

    /**
     * Carries the retained non-sensitive Stack Exchange profile projection.
     *
     * @param displayName  optional display name
     * @param profileImage optional profile image URL
     * @param websiteUrl   optional website URL
     * @param location     optional location text
     * @param link         optional Stack Exchange profile URL
     * @param userType     optional Stack Exchange user classification
     */
    private record ProfileWire(String displayName, String profileImage, String websiteUrl, String location, String link,
            String userType) {

        /**
         * Decodes one already member-validated Stack Exchange user item.
         *
         * @param user private user response object
         * @return immutable typed profile projection
         */
        private static ProfileWire decode(final JsonValue.ObjectValue user) {
            return new ProfileWire(optionalString(user, "display_name"), optionalString(user, "profile_image"),
                    optionalString(user, "website_url"), optionalString(user, "location"), optionalString(user, "link"),
                    optionalString(user, "user_type"));
        }

        /**
         * Copies one optional string attribute into the identity projection.
         *
         * @param attributes mutable destination owned by the identity mapper
         * @param name       exact Stack Exchange wire member name
         * @param value      optional decoded value
         */
        private static void put(final Map<String, JsonValue> attributes, final String name, final String value) {
            if (value != null) {
                attributes.put(name, new JsonValue.StringValue(value));
            }
        }

        /**
         * Copies present profile attributes using their exact Stack Exchange wire names.
         *
         * @param attributes mutable destination owned by the identity mapper
         */
        private void copyAttributes(final Map<String, JsonValue> attributes) {
            put(attributes, "display_name", displayName);
            put(attributes, "profile_image", profileImage);
            put(attributes, "website_url", websiteUrl);
            put(attributes, "location", location);
            put(attributes, "link", link);
            put(attributes, "user_type", userType);
        }

    }

    /**
     * Carries the private Stack Overflow token fields needed by the immediate profile request.
     *
     * @param accessToken sensitive access token
     * @param expiresIn   positive access-token lifetime in seconds, or empty for a requested {@code no_expiry} token
     * @author Kimi Liu
     */
    private record Access(String accessToken, Optional<Long> expiresIn) {

        /**
         * Validates one private token result without adding a synthetic token type.
         *
         * @throws IllegalArgumentException if the access token is blank
         * @throws IllegalArgumentException if the lifetime container is {@code null}
         * @throws ValidateException        if a present lifetime is not positive
         */
        private Access {
            Assert.notBlank(accessToken, "Stack Overflow private access token must not be blank");
            Assert.notNull(expiresIn, "Stack Overflow private access-token lifetime must not be null");
            expiresIn = Optional.ofNullable(expiresIn.getOrNull());
            if (expiresIn.isPresent() && expiresIn.getOrNull() <= 0L) {
                throw new ValidateException("Stack Overflow private access-token lifetime must be positive");
            }
        }

        /**
         * Returns a diagnostic representation without token material.
         *
         * @return redacted private token summary
         */
        @Override
        public String toString() {
            return "Access[accessToken=[REDACTED], expiresIn=" + expiresIn + Symbol.C_BRACKET_RIGHT;
        }

    }

}
