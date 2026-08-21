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
package org.miaixz.bus.auth.vendor.kujiale;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceWorkflow;
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
 * Implements Kujiale browser login while publishing only its standard OAuth authorization operation.
 * <p>
 * Authorization retains the standard {@link AuthorizationRequest} contract. Kujiale's query-carried client secret,
 * empty form body, private token result, OpenID lookup, and profile envelope are consumed only inside Source
 * authentication and never escape as fabricated OAuth token, introspection, or OpenID Connect UserInfo values.
 * </p>
 *
 * @author Kimi Liu
 */
public final class KujialeSourceAdapter implements VendorAdapter {

    /**
     * Trusted Kujiale authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://oauth.kujiale.com";

    /**
     * Maximum accepted Kujiale JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Maximum accepted Kujiale JSON response nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = Normal._64;

    /**
     * Registered Source identifier copied into verified external identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Kujiale manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Kujiale Source options.
     */
    private final KujialeOptions options;

    /**
     * Caller-owned runtime, secret, JSON, network, clock, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Unified public OAuth capability router for Kujiale authorization.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time state lifecycle for Kujiale's browser flow.
     */
    private final RedirectManager redirectManager;

    /**
     * Creates one Source-bound Kujiale adapter from the frozen default manifest.
     *
     * @param namespaceId registration namespace used to isolate browser state and credentials
     * @param sourceId    registered Source identifier
     * @param manifest    selected Kujiale manifest
     * @param variant     exact selected default manifest
     * @param options     decoded externally loaded Kujiale options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, manifest, or callback options differ from the manifest
     */
    public KujialeSourceAdapter(final String namespaceId, final String sourceId, final KujialeManifest manifest,
            final VariantManifest.Variant variant, final KujialeOptions options, final DriverServices services) {
        final KujialeManifest selectedProfile = Assert.notNull(manifest, "Kujiale manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Kujiale Source id must not be blank");
        this.variant = Assert.notNull(variant, "Kujiale manifest must not be null");
        this.options = Assert.notNull(options, "Kujiale options must not be null");
        this.services = Assert.notNull(services, "Kujiale execution services must not be null");
        if (!KujialeManifest.ID.equals(selectedProfile.vendor())
                || !selectedProfile.variant(KujialeManifest.DEFAULT).equals(variant)
                || !KujialeManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !KujialeManifest.ID.equals(options.vendor()) || !KujialeManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("Kujiale adapter requires the kujiale/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION,
                                (request, context, timeout) -> authorize(request))));
    }

    /**
     * Validates the common Kujiale envelope and returns its exact decimal-string result code.
     *
     * @param object decoded envelope
     * @return exact result-code string
     * @throws ValidateException if members, code, or auxiliary failure data are invalid
     */
    private static String envelope(final JsonValue.ObjectValue object) {
        if (!envelopeMembers(object)) {
            throw new ValidateException("Kujiale envelope contains an unregistered member");
        }
        final String code = requiredString(object, "c");
        if (!decimal(code)) {
            throw new ValidateException("Kujiale envelope code must be a decimal string");
        }
        final JsonValue auxiliary = object.values().get("f");
        if (auxiliary != null && !(auxiliary instanceof JsonValue.NullValue)) {
            throw new ValidateException("Kujiale envelope contains unknown auxiliary failure data");
        }
        return code;
    }

    /**
     * Verifies the documented members of a Kujiale response envelope.
     *
     * @param object decoded response envelope
     * @return whether every present member has a registered envelope meaning
     */
    private static boolean envelopeMembers(final JsonValue.ObjectValue object) {
        for (String member : object.values().keySet()) {
            final boolean known = switch (member) {
                case "c", "m", "d", "f" -> true;
                default -> false;
            };
            if (!known) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies the exact members of a Kujiale token success object.
     *
     * @param object decoded token data
     * @return whether the object contains exactly access, refresh, and lifetime members
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue object) {
        return object.values().size() == 3 && object.values().containsKey("accessToken")
                && object.values().containsKey("refreshToken") && object.values().containsKey("expiresIn");
    }

    /**
     * Verifies that a Kujiale profile object contains only documented members.
     *
     * @param object decoded profile data
     * @return whether every present member has a registered profile meaning
     */
    private static boolean profileMembers(final JsonValue.ObjectValue object) {
        for (String member : object.values().keySet()) {
            final boolean known = switch (member) {
                case "userName", "openId", "avatar" -> true;
                default -> false;
            };
            if (!known) {
                return false;
            }
        }
        return true;
    }

    /**
     * Identifies OAuth authorization errors documented for the Kujiale callback.
     *
     * @param error returned OAuth error code
     * @return whether the error is registered by the platform contract
     */
    private static boolean callbackError(final String error) {
        return switch (error) {
            case "access_denied", "invalid_request", "unauthorized_client", "unsupported_response_type", "invalid_scope" -> true;
            default -> false;
        };
    }

    /**
     * Copies an open client-secret lease into a transient request value and clears the intermediate characters.
     *
     * @param lease open client-secret lease
     * @return transient client-secret request value
     */
    private static String secret(final SecretLease lease) {
        final char[] material = lease.material();
        try {
            return new String(material);
        } finally {
            java.util.Arrays.fill(material, Symbol.C_NUL);
        }
    }

    /**
     * Validates the error-only fields of a nonzero Kujiale envelope and returns a safe failure.
     *
     * @param code        exact nonzero platform result code
     * @param envelope    decoded envelope
     * @param description non-sensitive operation description
     * @param <T>         expected success type
     * @return failed outcome containing only the platform result code
     * @throws ValidateException if the error branch contains success data or omits its message
     */
    private static <T> Outcome<T> business(
            final String code,
            final JsonValue.ObjectValue envelope,
            final String description) {
        if (Symbol.ZERO.equals(code)) {
            throw new ValidateException("Kujiale error envelope must use a nonzero code");
        }
        requiredString(envelope, "m");
        final JsonValue data = envelope.values().get("d");
        if (data != null && !(data instanceof JsonValue.NullValue)) {
            throw new ValidateException("Kujiale error envelope must not contain success data");
        }
        return failed(ErrorCode._502, description, Map.of("c", new JsonValue.StringValue(code)));
    }

    /**
     * Validates an optional Kujiale message as a JSON string without copying it into outcomes.
     *
     * @param envelope decoded success envelope
     * @throws ValidateException if a present message has another JSON type
     */
    private static void optionalMessage(final JsonValue.ObjectValue envelope) {
        final JsonValue message = envelope.values().get("m");
        if (message != null && !(message instanceof JsonValue.NullValue)
                && !(message instanceof JsonValue.StringValue)) {
            throw new ValidateException("Kujiale envelope message must be a string when present");
        }
    }

    /**
     * Reads one required JSON object member.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return required object value
     * @throws ValidateException if absent or another JSON type
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.ObjectValue nested)) {
            throw new ValidateException("Kujiale response requires an object member");
        }
        return nested;
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
            throw new ValidateException("Kujiale response requires a non-blank string member");
        }
        return text.value();
    }

    /**
     * Reads one optional JSON string member while preserving an empty platform value.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return string value or {@code null} when absent or JSON null
     * @throws ValidateException if a present value has another JSON type
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException("Kujiale optional response member must be a string");
        }
        return text.value();
    }

    /**
     * Reads one required positive exact integral JSON number.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return positive long value
     * @throws ValidateException if absent, non-integral, out of range, or not positive
     */
    private static long positiveLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Kujiale response requires an integral numeric member");
        }
        try {
            final long result = number.value().longValueExact();
            if (result <= 0L) {
                throw new ValidateException("Kujiale numeric member must be positive");
            }
            return result;
        } catch (ArithmeticException cause) {
            throw new ValidateException("Kujiale numeric member must be an exact long", cause);
        }
    }

    /**
     * Reports whether one non-empty string contains only an optional leading minus and decimal digits.
     *
     * @param value candidate platform result code
     * @return whether the value is a decimal string
     */
    private static boolean decimal(final String value) {
        final int first = value.charAt(0) == Symbol.C_MINUS ? 1 : 0;
        if (first == value.length()) {
            return false;
        }
        for (int index = first; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < Symbol.C_ZERO || character > Symbol.C_NINE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Classifies a Kujiale HTTP status before attempting to decode its JSON envelope.
     *
     * @param status      exact HTTP status
     * @param description non-sensitive operation description
     * @param <T>         expected success type
     * @return {@code null} for HTTP 200, otherwise a rejected client condition or failed upstream condition
     */
    private static <T> Outcome<T> status(final int status, final String description) {
        if (status == Http.Status.OK) {
            return null;
        }
        if (status == Http.Status.TOO_MANY_REQUESTS || status >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(status == Http.Status.TOO_MANY_REQUESTS ? ErrorCode._429 : ErrorCode._502, description);
        }
        if (status >= Http.Status.BAD_REQUEST && status < Http.Status.INTERNAL_SERVER_ERROR) {
            return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, emptyObject()));
        }
        return failed(ErrorCode._502, description);
    }

    /**
     * Returns one required non-blank private value.
     *
     * @param value   candidate value
     * @param message safe validation message
     * @return non-blank value
     * @throws IllegalArgumentException if the value is {@code null} or blank
     */
    private static String required(final String value, final String message) {
        return Assert.notBlank(value, message);
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
     * Creates a safe expected rejection without platform-sensitive details.
     *
     * @param description non-sensitive rejection description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, emptyObject()));
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
     * Creates a safe operational failure with the explicitly allowed platform code detail.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param details     non-sensitive structured details
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
     * Creates one immutable provider-neutral empty JSON object.
     *
     * @return empty JSON object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Returns the exact capability manifest frozen by the selected Kujiale manifest.
     *
     * @return immutable Source-authentication and OAuth authorization manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and the single registered OAuth authorization operation.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Kujiale-private token or profile records
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Kujiale capability must not be null");
        Assert.notNull(context, "Kujiale invocation context must not be null");
        Assert.notNull(timeout, "Kujiale invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Kujiale capability is not declared"));
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
        return completed(rejected("Kujiale capability request is invalid"));
    }

    /**
     * Builds Kujiale's exact ordered authorization redirect from generated one-time state.
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
        Assert.notNull(context, "Kujiale authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Kujiale authorization has no remaining time budget"));
        }
        if (initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(
                    failed(
                            ErrorCode._500,
                            "Kujiale browser flow generated security material outside its registered policy"));
        }
        final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                options.redirectUri(), Optional.of(new Scope(options.scopes())), Optional.of(initiation.state()),
                Optional.empty(), Optional.empty(), emptyObject());
        return standardAdapter.invoke(OAuth2ClientScheme.AUTHORIZATION, request, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<UnoUrl> success -> Outcome
                            .succeeded(new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                    case Outcome.Rejected<UnoUrl> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<UnoUrl> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Encodes a standard OAuth Authorization Request using Kujiale's comma-delimited scope deviation.
     *
     * @param request standard OAuth Authorization Request
     * @return exact Kujiale authorization URL or a safe rejection
     */
    private CompletionStage<Outcome<UnoUrl>> authorize(final AuthorizationRequest request) {
        try {
            if (!valid(request)) {
                return completed(rejected("Kujiale Authorization Request differs from the registered manifest"));
            }
            final UnoUrl url = variant.targets().resolve(options).authorization().getOrNull().url().newBuilder()
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.STATE, request.state().getOrNull())
                    .query(OAuth2.Parameters.SCOPE, String.join(Symbol.COMMA, request.scope().getOrNull().values()))
                    .build();
            return completed(Outcome.succeeded(url));
        } catch (RuntimeException cause) {
            return completed(rejected("Kujiale Authorization Request is invalid"));
        }
    }

    /**
     * Validates the exact standard Authorization Request subset accepted by Kujiale.
     *
     * @param request request to inspect
     * @return whether all registered values match and PKCE is absent
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request == null ? null : request.scope().getOrNull();
        return request != null && ResponseType.CODE.equals(request.responseType())
                && options.clientId().equals(request.clientId()) && options.redirectUri().equals(request.redirectUri())
                && scope != null && options.scopes().equals(scope.values()) && request.state().isPresent()
                && request.codeChallenge().isEmpty() && request.codeChallengeMethod().isEmpty()
                && request.extensions().values().isEmpty();
    }

    /**
     * Extracts the unique state before consuming the browser correlation.
     * <p>
     * Full success or error branch validation occurs after atomic state consumption so malformed callbacks cannot be
     * retried against the same one-time interaction.
     * </p>
     *
     * @param callback raw inbound callback
     * @return non-blank unique state
     * @throws IllegalArgumentException if the callback or state is absent
     * @throws ValidateException        if callback transport or parameter multiplicity is invalid
     */
    private String state(final Callback.Inbound callback) {
        return required(parameters(callback).state(), "Kujiale callback state must not be blank");
    }

    /**
     * Resolves one client-secret lease and completes Kujiale's private token, OpenID, and profile chain.
     *
     * @param completion consumed browser correlation
     * @param context    immutable invocation context used for secret resolution
     * @param timeout    shared end-to-end budget
     * @return verified Kujiale external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "Kujiale authorization callback is malformed"));
        }
        if (completion.codeVerifier().isPresent()) {
            return completed(failed(ErrorCode._500, "Kujiale callback unexpectedly carried a PKCE verifier"));
        }
        if (values.error() != null) {
            if (callbackError(values.error())) {
                return completed(rejected("Kujiale authorization endpoint returned an OAuth error"));
            }
            return completed(failed(ErrorCode._502, "Kujiale authorization endpoint returned an unknown error"));
        }
        final CompletionStage<Outcome<SecretLease>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.secretLoader().load(services.registration(), options.credential(), context, timeout),
                    loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded));
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "Kujiale client-secret resolution failed"));
        }
        if (resolution == null) {
            return completed(failed(ErrorCode._502, "Kujiale client-secret loader returned no stage"));
        }
        return resolution
                .handle(
                        (outcome, cause) -> cause == null && outcome != null ? outcome
                                : KujialeSourceAdapter
                                        .<SecretLease>failed(ErrorCode._502, "Kujiale client-secret resolution failed"))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(
                            values.code(),
                            success.value(),
                            timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Executes Kujiale's private T/I/U sequence under one owned client-secret lease.
     *
     * @param code    consumed authorization code
     * @param secret  owned client-secret lease closed after the complete private chain
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
                    case Outcome.Succeeded<Access> token -> switch (openId(token.value(), timeout)) {
                        case Outcome.Succeeded<String> subject -> profile(token.value(), subject.value(), timeout);
                        case Outcome.Rejected<String> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<String> failed -> Outcome.failed(failed.failure());
                    };
                    case Outcome.Rejected<Access> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Access> failed -> Outcome.failed(failed.failure());
                };
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Kujiale authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Sends Kujiale's query-authenticated authorization-code request with an exact empty form body.
     *
     * @param code    sensitive one-time authorization code
     * @param secret  still-open client-secret lease
     * @param timeout shared end-to-end budget
     * @return private token result or safely classified failure
     */
    private Outcome<Access> token(final String code, final SecretLease secret, final Timeout.Budget timeout) {
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Kujiale token request has no remaining time budget");
            }
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST)
                    .query(OAuth2.Parameters.CODE, required(code, "Kujiale authorization code must not be blank"))
                    .query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.CLIENT_SECRET, secret(secret))
                    .query(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(Normal.EMPTY_BYTE_ARRAY, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Kujiale token request failed");
        }
    }

    /**
     * Strictly decodes Kujiale's private token success or platform error envelope.
     *
     * @param response owned token endpoint response
     * @return private token result without fabricating {@code token_type}, scope, or OpenID
     */
    private Outcome<Access> token(final HttpResponse response) {
        final Outcome<Access> status = status(response.code(), "Kujiale token endpoint rejected or failed the request");
        if (status != null) {
            return status;
        }
        try {
            final JsonValue.ObjectValue envelope = object(response, "token");
            final String code = envelope(envelope);
            if (!Symbol.ZERO.equals(code)) {
                return business(code, envelope, "Kujiale token endpoint returned a platform error");
            }
            optionalMessage(envelope);
            final JsonValue.ObjectValue data = requiredObject(envelope, "d");
            if (!tokenMembers(data)) {
                throw new ValidateException("Kujiale token data members are invalid");
            }
            final String accessToken = requiredString(data, "accessToken");
            final String refreshToken = requiredString(data, "refreshToken");
            final long expiresIn = positiveLong(data, "expiresIn");
            return Outcome.succeeded(new Access(accessToken, refreshToken, expiresIn));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Kujiale token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves the canonical Kujiale OpenID using the private non-RFC 7662 lookup endpoint.
     *
     * @param access  private token result
     * @param timeout shared end-to-end budget
     * @return canonical client-scoped OpenID
     */
    private Outcome<String> openId(final Access access, final Timeout.Budget timeout) {
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Kujiale OpenID lookup has no remaining time budget");
            }
            final var endpoint = variant.targets().resolve(options).introspection().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET).query(OAuth2.Parameters.ACCESS_TOKEN, access.accessToken())
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return openId(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Kujiale OpenID lookup failed");
        }
    }

    /**
     * Strictly decodes one Kujiale OpenID lookup envelope.
     *
     * @param response owned OpenID lookup response
     * @return canonical OpenID or safely classified failure
     */
    private Outcome<String> openId(final HttpResponse response) {
        final Outcome<String> status = status(
                response.code(),
                "Kujiale OpenID endpoint rejected or failed the request");
        if (status != null) {
            return status;
        }
        try {
            final JsonValue.ObjectValue envelope = object(response, "OpenID");
            final String code = envelope(envelope);
            if (!Symbol.ZERO.equals(code)) {
                return business(code, envelope, "Kujiale OpenID endpoint returned a platform error");
            }
            optionalMessage(envelope);
            return Outcome.succeeded(requiredString(envelope, "d"));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Kujiale OpenID endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves and verifies Kujiale's private profile using the token and lookup-bound OpenID.
     *
     * @param access  private token result
     * @param openId  canonical OpenID returned by the preceding lookup
     * @param timeout shared end-to-end budget and evidence clock
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> profile(final Access access, final String openId, final Timeout.Budget timeout) {
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Kujiale profile request has no remaining time budget");
            }
            final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET).query(OAuth2.Parameters.ACCESS_TOKEN, access.accessToken())
                    .query("open_id", required(openId, "Kujiale canonical OpenID must not be blank"))
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return profile(response, openId, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Kujiale profile request failed");
        }
    }

    /**
     * Strictly binds one Kujiale profile OpenID to the preceding private lookup.
     *
     * @param response owned profile response
     * @param openId   canonical lookup OpenID
     * @param timeout  shared clock used for identity evidence
     * @return verified identity or safely classified failure
     */
    private Outcome<ExternalIdentity> profile(
            final HttpResponse response,
            final String openId,
            final Timeout.Budget timeout) {
        final Outcome<ExternalIdentity> status = status(
                response.code(),
                "Kujiale profile endpoint rejected or failed the request");
        if (status != null) {
            return status;
        }
        try {
            final JsonValue.ObjectValue envelope = object(response, "profile");
            final String code = envelope(envelope);
            if (!Symbol.ZERO.equals(code)) {
                return business(code, envelope, "Kujiale profile endpoint returned a platform error");
            }
            optionalMessage(envelope);
            final JsonValue.ObjectValue data = requiredObject(envelope, "d");
            if (!profileMembers(data)) {
                throw new ValidateException("Kujiale profile data contains an unregistered member");
            }
            final String userName = requiredString(data, "userName");
            final String profileOpenId = requiredString(data, "openId");
            final String avatar = optionalString(data, "avatar");
            if (!openId.equals(profileOpenId)) {
                return failed(ErrorCode._502, "Kujiale profile subject does not match the OpenID lookup");
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            attributes.put("name", new JsonValue.StringValue(userName));
            attributes.put("displayName", new JsonValue.StringValue(userName));
            if (avatar != null) {
                attributes.put("avatar", new JsonValue.StringValue(avatar));
            }
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("openId", new JsonValue.StringValue(profileOpenId), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, profileOpenId, new JsonValue.ObjectValue(attributes),
                            List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Kujiale profile endpoint returned an invalid response");
        }
    }

    /**
     * Validates one exact Kujiale callback success or OAuth error branch.
     *
     * @param callback raw inbound callback
     * @return immutable unique callback values
     * @throws ValidateException if target, transport, names, branch, or values violate the frozen contract
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        final CallbackWire values = parameters(callback);
        final boolean success = values.code() != null && values.error() == null && values.errorDescription() == null
                && values.memberCount() == 2;
        final boolean error = values.code() == null && values.error() != null
                && values.memberCount() == (values.errorDescription() == null ? 2 : 3);
        if ((!success && !error) || !values.knownMembers() || values.blank()) {
            throw new ValidateException("Kujiale callback must contain one exact success or OAuth error branch");
        }
        return values;
    }

    /**
     * Validates callback transport and indexes each parameter exactly once.
     *
     * @param callback raw inbound callback
     * @return typed callback values with member-shape metadata
     * @throws ValidateException if target, method, or parameter multiplicity is invalid
     */
    private CallbackWire parameters(final Callback.Inbound callback) {
        Assert.notNull(callback, "Kujiale callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Kujiale callback transport or target is invalid");
        }
        final Map<String, String> values = new LinkedHashMap<>();
        for (Callback.Parameter parameter : callback.parameters()) {
            if (values.putIfAbsent(parameter.name(), parameter.value()) != null) {
                throw new ValidateException("Kujiale callback parameter names must be unique");
            }
        }
        boolean knownMembers = true;
        for (String member : values.keySet()) {
            if (!OAuth2.Parameters.CODE.equals(member) && !OAuth2.Parameters.ERROR.equals(member)
                    && !OAuth2.Parameters.ERROR_DESCRIPTION.equals(member) && !OAuth2.Parameters.STATE.equals(member)) {
                knownMembers = false;
                break;
            }
        }
        return new CallbackWire(values.get(OAuth2.Parameters.CODE), values.get(OAuth2.Parameters.ERROR),
                values.get(OAuth2.Parameters.ERROR_DESCRIPTION), values.get(OAuth2.Parameters.STATE), values.size(),
                knownMembers, values.values().stream().anyMatch(String::isBlank));
    }

    /**
     * Decodes one bounded HTTP 200 Kujiale JSON object.
     *
     * @param response  response whose body remains open
     * @param operation safe operation label used only in validation messages
     * @return strict provider-neutral JSON object
     * @throws ValidateException if media, JSON shape, depth, or duplicate members are invalid
     */
    private JsonValue.ObjectValue object(final HttpResponse response, final String operation) {
        if (response.code() != Http.Status.OK
                || !MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Kujiale " + operation + " response must use HTTP 200 application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Kujiale " + operation + " response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries Kujiale's private token fields through the immediate OpenID and profile requests.
     *
     * @param accessToken  sensitive access token
     * @param refreshToken sensitive refresh token retained only for token-success validation
     * @param expiresIn    positive access-token lifetime in seconds
     * @author Kimi Liu
     */
    private record Access(String accessToken, String refreshToken, long expiresIn) {

        /**
         * Validates one private Kujiale token success value.
         *
         * @throws IllegalArgumentException if either token is blank
         * @throws ValidateException        if the lifetime is not positive
         */
        private Access {
            Assert.notBlank(accessToken, "Kujiale private access token must not be blank");
            Assert.notBlank(refreshToken, "Kujiale private refresh token must not be blank");
            if (expiresIn <= 0L) {
                throw new ValidateException("Kujiale private access-token lifetime must be positive");
            }
        }

        /**
         * Returns a diagnostic representation without token material.
         *
         * @return redacted private token summary
         */
        @Override
        public String toString() {
            return "Access[accessToken=[REDACTED], refreshToken=[REDACTED], expiresIn=" + expiresIn
                    + Symbol.C_BRACKET_RIGHT;
        }

    }

    /**
     * Carries indexed Kujiale callback values without exposing a field-name map as a contract.
     *
     * @param code             authorization code on the success branch
     * @param error            OAuth error on the error branch
     * @param errorDescription optional OAuth error description
     * @param state            browser correlation state
     * @param memberCount      exact number of received parameters
     * @param knownMembers     whether every received name belongs to the callback contract
     * @param blank            whether any received value is blank
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String error, String errorDescription, String state, int memberCount,
            boolean knownMembers, boolean blank) {

        /**
         * Retains callback values after transport and multiplicity validation.
         */
        private CallbackWire {
            // No initialization required.
        }

    }

}
