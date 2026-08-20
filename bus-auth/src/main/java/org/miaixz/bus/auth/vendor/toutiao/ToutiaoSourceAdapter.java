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
package org.miaixz.bus.auth.vendor.toutiao;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationResponseDecoder;
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
 * Implements Toutiao browser authentication while exposing only standard OAuth authorization.
 * <p>
 * The {@code client_key} authorization mapping, query-bearing token POST without {@code token_type}, and enveloped
 * profile remain private to Source-authentication completion. Remote numeric errors are mapped directly to shared Bus
 * outcomes without restoring a Vendor-local error type.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ToutiaoSourceAdapter implements VendorAdapter {

    /**
     * Trusted Toutiao authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://open.snssdk.com";

    /**
     * Toutiao category designating an anonymous profile.
     */
    private static final String ANONYMOUS_UID_TYPE = "14";

    /**
     * Stable display value preserved for anonymous Toutiao profiles.
     */
    private static final String ANONYMOUS_DISPLAY_NAME = "Anonymous User";

    /**
     * Maximum bounded Toutiao JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum Toutiao JSON response nesting.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Toutiao manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Toutiao options.
     */
    private final ToutiaoOptions options;

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
     * Creates one Source-bound Toutiao adapter from the frozen default manifest.
     *
     * @param namespaceId registration namespace isolating browser state and credentials
     * @param sourceId    registered Source identifier
     * @param manifest    selected Toutiao manifest
     * @param variant     exact selected default manifest
     * @param options     decoded externally loaded Toutiao options
     * @param services    caller-owned runtime dependencies
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, manifest, options, or routing differ from the frozen variant
     */
    public ToutiaoSourceAdapter(final String namespaceId, final String sourceId, final ToutiaoManifest manifest,
            final VariantManifest.Variant variant, final ToutiaoOptions options, final ExecutionServices services) {
        final ToutiaoManifest selected = Assert.notNull(manifest, "Toutiao manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Toutiao Source id must not be blank");
        this.variant = Assert.notNull(variant, "Toutiao manifest must not be null");
        this.options = Assert.notNull(options, "Toutiao options must not be null");
        this.services = Assert.notNull(services, "Toutiao execution services must not be null");
        if (!ToutiaoManifest.ID.equals(selected.vendor()) || !selected.variant(ToutiaoManifest.DEFAULT).equals(variant)
                || !ToutiaoManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !ToutiaoManifest.ID.equals(options.vendor()) || !ToutiaoManifest.DEFAULT.equals(options.variant())) {
            throw new ValidateException("Toutiao adapter requires the toutiao/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.callbackDecoder = new AuthorizationResponseDecoder();
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION,
                                (request, context, timeout) -> authorization(request))));
    }

    /**
     * Materializes an operation-scoped client secret and clears the intermediate character buffer.
     *
     * @param lease open secret lease owned by the caller
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
     * Verifies every decoded member of one selected private Toutiao document.
     *
     * @param kind   selected document kind
     * @param object decoded response object
     * @return whether every member has registered semantics
     */
    private static boolean members(final WireKind kind, final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            final boolean known = switch (kind) {
                case TOKEN -> switch (name) {
                    case "error_code", OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.EXPIRES_IN, "open_id" -> true;
                    default -> false;
                };
                case PROFILE_RESPONSE -> switch (name) {
                    case "error_code", "data" -> true;
                    default -> false;
                };
                case PROFILE -> switch (name) {
                    case "uid", "uid_type", "screen_name", "avatar_url", "description", "gender" -> true;
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
     * Maps one exact historical Toutiao numeric error using only shared Bus error categories.
     *
     * @param errorCode   remote numeric error identifier
     * @param description safe operation description
     * @param <T>         expected success type
     * @return rejected client/platform request or failed remote operation
     */
    private static <T> Outcome<T> platformFailure(final String errorCode, final String description) {
        final Map<String, JsonValue> details = Map.of("toutiao_error_code", new JsonValue.StringValue(errorCode));
        if (Symbol.NINE.equals(errorCode) || "999".equals(errorCode)) {
            return Outcome.failed(new Outcome.Failure(ErrorCode._502, description, new JsonValue.ObjectValue(details)));
        }
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(details)));
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
            throw new ValidateException("Toutiao response requires an object member: " + name);
        }
        return nested;
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
        if (value == null) {
            throw new ValidateException("Toutiao response requires a non-blank string member: " + name);
        }
        return value;
    }

    /**
     * Reads one optional non-blank JSON string member.
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
            throw new ValidateException("Toutiao response member must be a non-blank string: " + name);
        }
        return string.value();
    }

    /**
     * Reads one optional Toutiao numeric error identifier from its historical string or JSON-number forms.
     *
     * @param object decoded response object
     * @param name   exact error member name
     * @return canonical decimal error identifier or {@code null} when absent or explicit null
     */
    private static String optionalCode(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (value instanceof JsonValue.StringValue string && string.value().matches("0|[1-9][0-9]*")) {
            return string.value();
        }
        if (value instanceof JsonValue.NumberValue number) {
            try {
                final long code = number.value().longValueExact();
                if (code >= 0L) {
                    return Long.toString(code);
                }
            } catch (ArithmeticException cause) {
                throw new ValidateException("Toutiao error code must be an exact non-negative integer", cause);
            }
        }
        throw new ValidateException("Toutiao error code must be a non-negative decimal value");
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
            throw new ValidateException("Toutiao response requires a numeric member: " + name);
        }
        try {
            final long exact = number.value().longValueExact();
            if (exact <= 0L) {
                throw new ValidateException("Toutiao numeric member must be positive: " + name);
            }
            return exact;
        } catch (ArithmeticException cause) {
            throw new ValidateException("Toutiao numeric member must be an exact long: " + name, cause);
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
                new Outcome.Failure(ErrorCode._400, "Toutiao authorization endpoint returned a standard error",
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
     * Returns the exact frozen Toutiao capability manifest.
     *
     * @return immutable Source authentication and OAuth authorization capability set
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
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
     * @return typed outcome without exposing private Toutiao models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Toutiao capability must not be null");
        Assert.notNull(context, "Toutiao invocation context must not be null");
        Assert.notNull(timeout, "Toutiao invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Toutiao capability is not declared"));
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
        return completed(rejected("Toutiao capability request is invalid"));
    }

    /**
     * Builds the Toutiao redirect around generated one-time state.
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
        Assert.notNull(context, "Toutiao authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "Toutiao browser material violates the frozen manifest"));
        }
        try {
            final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                    options.redirectUri(), Optional.empty(), Optional.of(initiation.state()), Optional.empty(),
                    Optional.empty(), emptyObject());
            return standardAdapter.invoke(OAuth2ClientScheme.AUTHORIZATION, request, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<UnoUrl> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<UnoUrl> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<UnoUrl> failed -> Outcome.failed(failed.failure());
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Toutiao authorization request is invalid"));
        }
    }

    /**
     * Encodes one validated request using Toutiao's official query names and fixed extensions.
     *
     * @param request standard authorization request
     * @return asynchronous exact Toutiao authorization URL outcome
     */
    private CompletionStage<Outcome<UnoUrl>> authorization(final AuthorizationRequest request) {
        if (!valid(request)) {
            return completed(rejected("Toutiao authorization request differs from the registered Source"));
        }
        try {
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final UnoUrl location = endpoint.url().newBuilder()
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query("client_key", request.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, request.redirectUri().getOrNull())
                    .query("auth_only", Symbol.ONE).query("display", Symbol.ZERO)
                    .query(OAuth2.Parameters.STATE, request.state().getOrNull()).build();
            return completed(Outcome.succeeded(location));
        } catch (RuntimeException cause) {
            return completed(rejected("Toutiao authorization request is invalid"));
        }
    }

    /**
     * Validates one public standard authorization request against the registered Source.
     *
     * @param request standard OAuth authorization request
     * @return whether every public field matches the selected Source
     */
    private boolean valid(final AuthorizationRequest request) {
        return ResponseType.CODE.equals(request.responseType()) && options.clientId().equals(request.clientId())
                && options.redirectUri().equals(request.redirectUri()) && request.scope().isEmpty()
                && request.state().isPresent() && request.codeChallenge().isEmpty()
                && request.codeChallengeMethod().isEmpty() && request.extensions().values().isEmpty();
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
                    .orElseThrow(() -> new ValidateException("Toutiao authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Toutiao authorization error requires state"));
        };
    }

    /**
     * Completes the correlated private token and profile chain.
     *
     * @param completion consumed browser correlation
     * @param context    immutable invocation context used for secret resolution
     * @param timeout    shared end-to-end budget
     * @return verified Toutiao identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = decode(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Toutiao authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(oauthError(error));
        }
        if (completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(failed(ErrorCode._500, "Toutiao callback contains unexpected browser material"));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        try {
            final CompletionStage<Outcome<SecretLease>> resolution = org.miaixz.bus.auth.runtime.LoadResult.parse(
                    services.secretLoader().load(options.credential(), context, timeout),
                    loaded -> services.secretParser().parse(options.credential(), loaded));
            if (resolution == null) {
                return completed(failed(ErrorCode._502, "Toutiao secret loader returned no stage"));
            }
            return resolution
                    .handle(
                            (outcome, cause) -> cause == null && outcome != null ? outcome
                                    : ToutiaoSourceAdapter
                                            .<SecretLease>failed(ErrorCode._502, "Toutiao secret resolution failed"))
                    .thenCompose(outcome -> switch (outcome) {
                        case Outcome.Succeeded<SecretLease> success -> authenticate(
                                response.code(),
                                success.value(),
                                timeout);
                        case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    });
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "Toutiao secret resolution failed"));
        }
    }

    /**
     * Executes private token and profile operations while owning the client-secret lease.
     *
     * @param code    consumed authorization code
     * @param secret  owned client-secret lease
     * @param timeout shared end-to-end budget
     * @return verified Toutiao identity
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
                return failed(ErrorCode._502, "Toutiao authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Sends Toutiao's private credential-bearing query POST with an empty form body.
     *
     * @param code    consumed authorization code
     * @param secret  open client-secret lease
     * @param timeout shared end-to-end budget
     * @return private access result or safely classified failure
     */
    private Outcome<Access> token(final String code, final SecretLease secret, final Timeout.Budget timeout) {
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Toutiao token request has no remaining time budget");
            }
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST)
                    .query(
                            OAuth2.Parameters.CODE,
                            Assert.notBlank(code, "Toutiao authorization code must not be blank"))
                    .query("client_key", options.clientId()).query(OAuth2.Parameters.CLIENT_SECRET, secret(secret))
                    .query(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value())
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(Normal.EMPTY_BYTE_ARRAY, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Toutiao token request failed");
        }
    }

    /**
     * Strictly decodes a private Toutiao token response without inventing {@code token_type}.
     *
     * @param response owned token endpoint response
     * @return private access result or safely classified failure
     */
    private Outcome<Access> token(final HttpResponse response) {
        if (response.code() < Http.Status.OK || response.code() >= Http.Status.MULTIPLE_CHOICES) {
            return status(response.code(), "Toutiao token endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue object = object(response, "token");
            if (!members(WireKind.TOKEN, object)) {
                return failed(ErrorCode._502, "Toutiao token response contains an unknown member");
            }
            final String errorCode = optionalCode(object, "error_code");
            if (errorCode != null && !Symbol.ZERO.equals(errorCode)) {
                return platformFailure(errorCode, "Toutiao token endpoint rejected the request");
            }
            optionalString(object, "open_id");
            return Outcome.succeeded(
                    new Access(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN),
                            requiredPositiveLong(object, OAuth2.Parameters.EXPIRES_IN)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Toutiao token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves the Toutiao profile using its official query fields.
     *
     * @param access  private access result
     * @param timeout shared end-to-end budget
     * @return verified identity or safely classified failure
     */
    private Outcome<ExternalIdentity> profile(final Access access, final Timeout.Budget timeout) {
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Toutiao profile request has no remaining time budget");
            }
            final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET).query("client_key", options.clientId())
                    .query(OAuth2.Parameters.ACCESS_TOKEN, access.accessToken())
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return profile(response, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Toutiao profile request failed");
        }
    }

    /**
     * Strictly decodes one Toutiao profile envelope and maps its stable {@code uid}.
     *
     * @param response owned profile response
     * @param timeout  shared clock used for evidence timestamping
     * @return verified Toutiao identity
     */
    private Outcome<ExternalIdentity> profile(final HttpResponse response, final Timeout.Budget timeout) {
        if (response.code() != Http.Status.OK) {
            return status(response.code(), "Toutiao profile endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue root = object(response, "profile");
            if (!members(WireKind.PROFILE_RESPONSE, root)) {
                return failed(ErrorCode._502, "Toutiao profile envelope contains an unknown member");
            }
            final String errorCode = optionalCode(root, "error_code");
            if (errorCode != null && !Symbol.ZERO.equals(errorCode)) {
                return platformFailure(errorCode, "Toutiao profile endpoint rejected the request");
            }
            final JsonValue.ObjectValue user = requiredObject(root, "data");
            if (!members(WireKind.PROFILE, user)) {
                return failed(ErrorCode._502, "Toutiao profile response contains an unknown member");
            }
            final ProfileWire profile = ProfileWire.decode(user);
            final String subject = profile.uid();
            final String uidType = profile.uidType();
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            if (uidType != null) {
                attributes.put("uid_type", new JsonValue.StringValue(uidType));
            }
            final String displayName = ANONYMOUS_UID_TYPE.equals(uidType) ? ANONYMOUS_DISPLAY_NAME
                    : profile.screenName();
            if (displayName != null) {
                attributes.put("screen_name", new JsonValue.StringValue(displayName));
            }
            profile.copyAttributes(attributes);
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("uid", new JsonValue.StringValue(subject), AUTHORITY, timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Toutiao profile endpoint returned an invalid response");
        }
    }

    /**
     * Validates callback ownership before strict standard response decoding.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     */
    private AuthorizationResponseDecoder.Decoded decode(final Callback.Inbound callback) {
        final Callback.Inbound inbound = Assert.notNull(callback, "Toutiao callback must not be null");
        if (inbound.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(inbound.requestUri())) {
            throw new ValidateException("Toutiao callback URI or method is invalid");
        }
        return callbackDecoder.decode(inbound);
    }

    /**
     * Strictly reads one bounded Toutiao JSON object.
     *
     * @param response  response whose body remains owned by the caller
     * @param operation safe operation name used in validation failures
     * @return immutable provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final HttpResponse response, final String operation) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Toutiao " + operation + " response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Toutiao " + operation + " response root must be an object");
        }
        return object;
    }

    /**
     * Identifies each private Toutiao response document validated by this adapter.
     */
    private enum WireKind {

        /**
         * Private token response document.
         */
        TOKEN,

        /**
         * Top-level profile response envelope.
         */
        PROFILE_RESPONSE,

        /**
         * Nested Toutiao profile object.
         */
        PROFILE

    }

    /**
     * Carries the exact retained Toutiao profile projection.
     *
     * @param uid         stable Toutiao user identifier
     * @param uidType     optional user category
     * @param screenName  optional display name
     * @param avatarUrl   optional avatar URL
     * @param description optional profile description
     * @param gender      optional gender text
     */
    private record ProfileWire(String uid, String uidType, String screenName, String avatarUrl, String description,
            String gender) {

        /**
         * Decodes one already member-validated Toutiao profile object.
         *
         * @param object private profile response object
         * @return immutable typed profile projection
         */
        private static ProfileWire decode(final JsonValue.ObjectValue object) {
            return new ProfileWire(requiredString(object, "uid"), optionalString(object, "uid_type"),
                    optionalString(object, "screen_name"), optionalString(object, "avatar_url"),
                    optionalString(object, "description"), optionalString(object, "gender"));
        }

        /**
         * Copies one optional string attribute into the identity projection.
         *
         * @param attributes mutable destination owned by the identity mapper
         * @param name       exact Toutiao wire member name
         * @param value      optional decoded value
         */
        private static void put(final Map<String, JsonValue> attributes, final String name, final String value) {
            if (value != null) {
                attributes.put(name, new JsonValue.StringValue(value));
            }
        }

        /**
         * Copies optional non-display profile attributes using exact Toutiao wire names.
         *
         * @param attributes mutable destination owned by the identity mapper
         */
        private void copyAttributes(final Map<String, JsonValue> attributes) {
            put(attributes, "avatar_url", avatarUrl);
            put(attributes, "description", description);
            put(attributes, "gender", gender);
        }

    }

    /**
     * Carries Toutiao's private token values required by the immediate profile request.
     *
     * @param accessToken sensitive access token
     * @param expiresIn   positive access-token lifetime in seconds
     * @author Kimi Liu
     */
    private record Access(String accessToken, long expiresIn) {

        /**
         * Validates one private Toutiao token result.
         *
         * @throws IllegalArgumentException if the access token is blank
         * @throws ValidateException        if the lifetime is not positive
         */
        private Access {
            Assert.notBlank(accessToken, "Toutiao private access token must not be blank");
            if (expiresIn <= 0L) {
                throw new ValidateException("Toutiao private access-token lifetime must be positive");
            }
        }

        /**
         * Returns a diagnostic representation without token or owner material.
         *
         * @return redacted private token summary
         */
        @Override
        public String toString() {
            return "Access[accessToken=[REDACTED], expiresIn=" + expiresIn + Symbol.C_BRACKET_RIGHT;
        }

    }

}
