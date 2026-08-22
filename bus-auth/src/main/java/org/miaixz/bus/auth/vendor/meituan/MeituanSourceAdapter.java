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
package org.miaixz.bus.auth.vendor.meituan;

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
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationRequest;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.ResponseType;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
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
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements Meituan Waimai browser authentication while publishing only standard OAuth authorization.
 * <p>
 * The platform token, refresh, profile, and HTTP 200 error representations remain private named records. They are
 * consumed only while completing application-level Source authentication and never become fabricated OAuth token or
 * OpenID Connect UserInfo responses.
 * </p>
 *
 * @author Kimi Liu
 */
public class MeituanSourceAdapter implements VendorAdapter {

    /**
     * Trusted Meituan OpenAPI authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://openapi.waimai.meituan.com";

    /**
     * Maximum accepted Meituan JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Maximum accepted Meituan JSON response nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = Normal._32;

    /**
     * Exact platform error code observed and registered for application, client, and code rejection.
     */
    private static final String REJECTED_ERROR_CODE = "10005";

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Meituan manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Meituan options.
     */
    private final MeituanOptions options;

    /**
     * Caller-owned runtime, secret, JSON, transport, clock, and executor dependencies.
     */
    private final DriverServices services;

    /**
     * Unified router for Meituan's public standard OAuth authorization capability.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time state lifecycle for Meituan's browser flow.
     */
    private final RedirectManager redirectManager;

    /**
     * Standard form encoder used for every Meituan credentialed POST.
     */
    private final FormCodec formCodec;

    /**
     * Creates one Source-bound Meituan adapter from the frozen default manifest.
     *
     * @param namespaceId registration namespace used to isolate browser state and credentials
     * @param sourceId    registered Source identifier
     * @param manifest    selected Meituan manifest
     * @param variant     exact selected default manifest
     * @param options     decoded externally loaded Meituan options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, manifest, or options differ from the frozen manifest
     */
    public MeituanSourceAdapter(final String namespaceId, final String sourceId, final MeituanManifest manifest,
            final VariantManifest.Variant variant, final MeituanOptions options, final DriverServices services) {
        final MeituanManifest selectedProfile = Assert.notNull(manifest, "Meituan manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Meituan Source id must not be blank");
        this.variant = Assert.notNull(variant, "Meituan manifest must not be null");
        this.options = Assert.notNull(options, "Meituan options must not be null");
        this.services = Assert.notNull(services, "Meituan execution services must not be null");
        if (!MeituanManifest.ID.equals(selectedProfile.vendor())
                || !selectedProfile.variant(MeituanManifest.DEFAULT).equals(variant)
                || !MeituanManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !MeituanManifest.ID.equals(options.vendor()) || !MeituanManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty() || !options.scopes().isEmpty()) {
            throw new ValidateException("Meituan adapter requires the meituan/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION,
                                (request, context, timeout) -> authorize(request))));
        this.formCodec = new FormCodec();
    }

    /**
     * Reports whether a decoded object attempts to use the platform error branch.
     *
     * @param object decoded response object
     * @return whether either exact error member is present
     */
    private static boolean errorBranch(final JsonValue.ObjectValue object) {
        return object.values().containsKey("error_code") || object.values().containsKey("error_msg");
    }

    /**
     * Identifies a callback error classified as an expected authorization rejection.
     *
     * @param error callback error value
     * @return whether the value is a documented rejection
     */
    private static boolean rejectedCallback(final String error) {
        return "access_denied".equals(error) || "invalid_request".equals(error);
    }

    /**
     * Verifies the exact members of a Meituan token success.
     *
     * @param object decoded token response
     * @return whether all three mandatory members and no others are present
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue object) {
        return object.values().size() == 3 && object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                && object.values().containsKey(OAuth2.Parameters.REFRESH_TOKEN)
                && object.values().containsKey(OAuth2.Parameters.EXPIRES_IN);
    }

    /**
     * Verifies the mandatory and optional members of a Meituan profile success.
     *
     * @param object decoded profile response
     * @return whether only openid, nickname, and optional avatar are present
     */
    private static boolean profileMembers(final JsonValue.ObjectValue object) {
        if (!object.values().containsKey("openid") || !object.values().containsKey("nickname")) {
            return false;
        }
        for (String member : object.values().keySet()) {
            if (!"openid".equals(member) && !"nickname".equals(member) && !"avatar".equals(member)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copies the active application secret into a transient form value and clears intermediate characters.
     *
     * @param lease open application-secret lease
     * @return transient form value
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
     * Decodes the exact corrected Meituan error envelope without accepting historical {@code erroe_msg}.
     *
     * @param object decoded response object
     * @return private error envelope
     * @throws ValidateException if error fields are missing, mixed with success, wrongly typed, or non-decimal
     */
    private static ErrorEnvelope error(final JsonValue.ObjectValue object) {
        if (object.values().size() != 2 || !object.values().containsKey("error_code")
                || !object.values().containsKey("error_msg")) {
            throw new ValidateException("Meituan error response must contain only error_code and error_msg");
        }
        return new ErrorEnvelope(requiredString(object, "error_code"), requiredString(object, "error_msg"));
    }

    /**
     * Classifies a validated Meituan business error and retains only its code in failure details.
     *
     * @param error       private validated error envelope
     * @param description non-sensitive operation description
     * @param <T>         expected success type
     * @return rejected registered error or failed unknown business error
     */
    private static <T> Outcome<T> business(final ErrorEnvelope error, final String description) {
        final Map<String, JsonValue> details = Map.of("error_code", new JsonValue.StringValue(error.code()));
        if (REJECTED_ERROR_CODE.equals(error.code())) {
            return Outcome
                    .rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(details)));
        }
        return failed(ErrorCode._502, description, details);
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
        final String value = optionalString(object, name);
        if (value == null || value.isBlank()) {
            throw new ValidateException("Meituan response requires a non-blank string member: " + name);
        }
        return value;
    }

    /**
     * Reads one optional JSON string member without coercion.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return string value or {@code null} when absent
     * @throws ValidateException if a present value has another JSON type
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException("Meituan optional response member must be a string: " + name);
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
            throw new ValidateException("Meituan response requires an integral numeric member: " + name);
        }
        try {
            final long result = number.value().longValueExact();
            if (result <= 0L) {
                throw new ValidateException("Meituan numeric member must be positive: " + name);
            }
            return result;
        } catch (ArithmeticException cause) {
            throw new ValidateException("Meituan numeric member must be an exact long: " + name, cause);
        }
    }

    /**
     * Classifies an HTTP status before attempting platform JSON decoding.
     *
     * @param status      exact HTTP status
     * @param description non-sensitive operation description
     * @param <T>         expected success type
     * @return {@code null} for HTTP 200, otherwise rejected client status or failed upstream status
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
     * Clears one transient encoded form body when present.
     *
     * @param value transient sensitive bytes or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) 0);
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
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Creates an already completed outcome stage.
     *
     * @param outcome completed outcome
     * @param <T>     successful value type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe expected rejection without platform-sensitive details.
     *
     * @param description non-sensitive rejection description
     * @param <T>         expected successful value type
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
     * @param <T>         expected successful value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final Errors code, final String description) {
        return failed(code, description, Map.of());
    }

    /**
     * Creates a safe operational failure with an allow-listed platform error code.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param details     non-sensitive structured details
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
     * Creates one immutable provider-neutral empty JSON object.
     *
     * @return empty JSON object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Returns the exact capability manifest frozen by the Meituan manifest.
     *
     * @return immutable Source-authentication and OAuth authorization manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and the single published OAuth authorization operation.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing private Meituan records
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "Meituan capability must not be null");
        Assert.notNull(context, "Meituan invocation context must not be null");
        Assert.notNull(timeout, "Meituan invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Meituan capability is not declared"));
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
        return completed(rejected("Meituan capability request is invalid"));
    }

    /**
     * Builds the exact ordered Meituan redirect from generated one-time state.
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
        Assert.notNull(context, "Meituan authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Meituan authorization has no remaining timeout"));
        }
        if (initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(
                    failed(ErrorCode._500, "Meituan browser flow generated material outside its registered policy"));
        }
        final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                options.redirectUri(), Optional.empty(), Optional.of(initiation.state()), Optional.empty(),
                Optional.empty(), emptyObject());
        return standardAdapter.invoke(OAuth2ClientScheme.AUTHORIZATION, request, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Url> success -> Outcome
                            .succeeded(new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                    case Outcome.Rejected<Url> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Url> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Encodes a standard Authorization Request with Meituan's mandatory empty {@code scope=} member.
     *
     * @param request standard OAuth Authorization Request
     * @return exact Meituan authorization URL or safe rejection
     */
    private CompletionStage<Outcome<Url>> authorize(final AuthorizationRequest request) {
        try {
            if (!valid(request)) {
                return completed(rejected("Meituan Authorization Request differs from the registered manifest"));
            }
            final Url url = variant.targets().resolve(options).authorization().getOrNull().url().newBuilder()
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.STATE, request.state().getOrNull())
                    .query(OAuth2.Parameters.SCOPE, Normal.EMPTY).build();
            return completed(Outcome.succeeded(url));
        } catch (RuntimeException cause) {
            return completed(rejected("Meituan Authorization Request is invalid"));
        }
    }

    /**
     * Validates the exact standard Authorization Request subset accepted by Meituan.
     *
     * @param request request to inspect
     * @return whether client, callback, state, empty scope, and prohibited PKCE match the manifest
     */
    private boolean valid(final AuthorizationRequest request) {
        return request != null && ResponseType.CODE.equals(request.responseType())
                && options.clientId().equals(request.clientId()) && options.redirectUri().equals(request.redirectUri())
                && request.scope().isEmpty() && request.state().isPresent() && request.codeChallenge().isEmpty()
                && request.codeChallengeMethod().isEmpty() && request.extensions().values().isEmpty();
    }

    /**
     * Extracts the unique state before atomically consuming the browser correlation.
     *
     * @param callback raw inbound callback
     * @return non-blank unique state
     * @throws ValidateException if callback transport, target, multiplicity, or state is invalid
     */
    private String state(final Callback.Inbound callback) {
        final String state = parameters(callback).state();
        if (state == null || state.isBlank()) {
            throw new ValidateException("Meituan callback state must not be blank");
        }
        return state;
    }

    /**
     * Resolves one application-secret lease and completes Meituan's private token and profile chain.
     *
     * @param completion consumed browser correlation
     * @param context    immutable invocation context used for secret resolution
     * @param timeout    shared end-to-end timeout
     * @return verified Meituan external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "Meituan authorization callback is malformed"));
        }
        if (completion.codeVerifier().isPresent()) {
            return completed(failed(ErrorCode._500, "Meituan callback unexpectedly carried a PKCE verifier"));
        }
        if (values.error() != null) {
            return rejectedCallback(values.error())
                    ? completed(rejected("Meituan authorization endpoint rejected the request"))
                    : completed(failed(ErrorCode._502, "Meituan authorization endpoint returned an unknown error"));
        }
        final CompletionStage<Outcome<SecretLease>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.secretLoader().load(
                            new SecretLoader.Request(services.registration(), options.credential()),
                            context,
                            timeout),
                    loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded));
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "Meituan application-secret resolution failed"));
        }
        if (resolution == null) {
            return completed(failed(ErrorCode._502, "Meituan application-secret loader returned no stage"));
        }
        return resolution
                .handle(
                        (outcome, cause) -> cause == null && outcome != null ? outcome
                                : MeituanSourceAdapter.<SecretLease>failed(
                                        ErrorCode._502,
                                        "Meituan application-secret resolution failed"))
                .thenCompose(outcome -> switch (outcome) {
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
     * Executes Meituan's private token and profile sequence under one owned secret lease.
     *
     * @param code    consumed authorization code
     * @param secret  owned application-secret lease
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
                    case Outcome.Succeeded<TokenEnvelope> token -> profile(token.value(), secret, timeout);
                    case Outcome.Rejected<TokenEnvelope> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<TokenEnvelope> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                };
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Meituan authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Sends the exact authorization-code token form.
     *
     * @param code    sensitive one-time authorization code
     * @param secret  open application-secret lease
     * @param timeout shared end-to-end timeout
     * @return private token envelope or classified failure
     */
    private Outcome<TokenEnvelope> token(final String code, final SecretLease secret, final Timeout timeout) {
        return tokenRequest(
                variant.targets().resolve(options).token().getOrNull().url().toString(),
                OAuth2.Parameters.CODE,
                Assert.notBlank(code, "Meituan authorization code must not be blank"),
                GrantType.AUTHORIZATION_CODE.value(),
                secret,
                timeout);
    }

    /**
     * Performs Meituan's private refresh lifecycle without publishing a standard or custom refresh capability.
     *
     * @param current private token envelope whose refresh token is replaced atomically on success
     * @param context immutable invocation context used for one independent secret lease
     * @param timeout shared end-to-end timeout
     * @return replacement private access and refresh token pair
     */
    private CompletionStage<Outcome<TokenEnvelope>> refresh(
            final TokenEnvelope current,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(current, "Meituan current private token must not be null");
        final CompletionStage<Outcome<SecretLease>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.secretLoader().load(
                            new SecretLoader.Request(services.registration(), options.credential()),
                            context,
                            timeout),
                    loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded));
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "Meituan refresh secret resolution failed"));
        }
        if (resolution == null) {
            return completed(failed(ErrorCode._502, "Meituan refresh secret loader returned no stage"));
        }
        return resolution.thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<SecretLease> success -> CompletableFuture.supplyAsync(() -> {
                try (SecretLease secret = success.value()) {
                    return tokenRequest(
                            variant.targets().resolve(options).refresh().getOrNull().url().toString(),
                            OAuth2.Parameters.REFRESH_TOKEN,
                            current.refreshToken(),
                            OAuth2.Parameters.REFRESH_TOKEN,
                            secret,
                            timeout);
                }
            }, services.executor());
            case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Sends one exact Meituan token or refresh form in the frozen platform order.
     *
     * @param endpoint  exact fixed endpoint
     * @param field     {@code code} or {@code refresh_token}
     * @param value     sensitive grant credential
     * @param grantType exact grant type
     * @param secret    open application-secret lease
     * @param timeout   shared end-to-end timeout
     * @return private replacement token envelope or classified failure
     */
    private Outcome<TokenEnvelope> tokenRequest(
            final String endpoint,
            final String field,
            final String value,
            final String grantType,
            final SecretLease secret,
            final Timeout timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Meituan token request has no remaining timeout");
            }
            body = formCodec.encode(
                    List.of(
                            new NameValue("app_id", options.clientId()),
                            new NameValue("secret", secret(secret)),
                            new NameValue(field, Assert.notBlank(value, "Meituan grant credential must not be blank")),
                            new NameValue(OAuth2.Parameters.GRANT_TYPE, grantType)));
            try (Response response = FabricX.http(services.fabric(), Protocol.OAUTH2, timeout).url(endpoint)
                    .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Meituan token request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Strictly discriminates one Meituan token success or platform error object.
     *
     * @param response owned token or refresh response
     * @return private token envelope or safely classified error
     */
    private Outcome<TokenEnvelope> token(final Response response) {
        final Outcome<TokenEnvelope> status = status(response.code(), "Meituan token endpoint rejected the request");
        if (status != null) {
            return status;
        }
        try {
            final JsonValue.ObjectValue object = object(response);
            if (errorBranch(object)) {
                return business(error(object), "Meituan token endpoint returned a platform error");
            }
            if (!tokenMembers(object)) {
                throw new ValidateException("Meituan token success contains unknown or missing members");
            }
            return Outcome.succeeded(
                    new TokenEnvelope(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN),
                            requiredString(object, OAuth2.Parameters.REFRESH_TOKEN),
                            positiveLong(object, OAuth2.Parameters.EXPIRES_IN)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Meituan token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves and validates Meituan's private profile under the same secret lease as token redemption.
     *
     * @param token   private access and refresh token envelope
     * @param secret  still-open application-secret lease
     * @param timeout shared end-to-end timeout and evidence clock
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> profile(
            final TokenEnvelope token,
            final SecretLease secret,
            final Timeout timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Meituan profile request has no remaining timeout");
            }
            body = formCodec.encode(
                    List.of(
                            new NameValue("app_id", options.clientId()),
                            new NameValue("secret", secret(secret)),
                            new NameValue(OAuth2.Parameters.ACCESS_TOKEN, token.accessToken())));
            final String endpoint = variant.targets().resolve(options).userInfo().getOrNull().url().toString();
            try (Response response = FabricX.http(services.fabric(), Protocol.OAUTH2, timeout).url(endpoint)
                    .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return profile(response, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Meituan profile request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Strictly discriminates a Meituan profile success or platform error object and maps the stable OpenID.
     *
     * @param response owned profile response
     * @param timeout  shared clock used for identity evidence
     * @return verified external identity or classified failure
     */
    private Outcome<ExternalIdentity> profile(final Response response, final Timeout timeout) {
        final Outcome<ExternalIdentity> status = status(
                response.code(),
                "Meituan profile endpoint rejected the request");
        if (status != null) {
            return status;
        }
        try {
            final JsonValue.ObjectValue object = object(response);
            if (errorBranch(object)) {
                return business(error(object), "Meituan profile endpoint returned a platform error");
            }
            if (!profileMembers(object)) {
                throw new ValidateException("Meituan profile success contains unknown or missing members");
            }
            final ProfileEnvelope profile = new ProfileEnvelope(requiredString(object, "openid"),
                    requiredString(object, "nickname"), Optional.ofNullable(optionalString(object, "avatar")));
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            attributes.put("name", new JsonValue.StringValue(profile.nickname()));
            attributes.put("displayName", new JsonValue.StringValue(profile.nickname()));
            profile.avatar().ifPresent(value -> attributes.put("avatar", new JsonValue.StringValue(value)));
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("openid", new JsonValue.StringValue(profile.openId()), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, profile.openId(), new JsonValue.ObjectValue(attributes),
                            List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Meituan profile endpoint returned an invalid response");
        }
    }

    /**
     * Validates one exact Meituan callback success or OAuth error branch.
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
            throw new ValidateException("Meituan callback must contain one exact success or OAuth error branch");
        }
        return values;
    }

    /**
     * Validates callback transport and indexes each parameter exactly once.
     *
     * @param callback raw inbound callback
     * @return typed callback values with member-shape metadata
     * @throws ValidateException if source, target, method, or parameter multiplicity is invalid
     */
    private CallbackWire parameters(final Callback.Inbound callback) {
        Assert.notNull(callback, "Meituan callback must not be null");
        if (!sourceId.equals(callback.sourceId()) || callback.method() != Http.Method.GET
                || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Meituan callback transport or target is invalid");
        }
        final Map<String, String> values = new LinkedHashMap<>();
        for (Callback.Parameter parameter : callback.parameters()) {
            if (values.putIfAbsent(parameter.name(), parameter.value()) != null) {
                throw new ValidateException("Meituan callback parameter names must be unique");
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
     * Decodes one bounded UTF-8-compatible application/json response object.
     *
     * @param response response whose body remains owned by the caller
     * @return strict provider-neutral JSON object
     * @throws ValidateException if media, charset, JSON shape, depth, size, or duplicate members are invalid
     */
    private JsonValue.ObjectValue object(final Response response) {
        final MediaType media = response.body().media();
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(media)) {
            throw new ValidateException("Meituan response must use application/json");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("Meituan JSON response charset must be UTF-8");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Meituan response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries indexed Meituan callback values without exposing a field-name map as a contract.
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

    /**
     * Carries one private Meituan access and refresh token pair through immediate profile or refresh processing.
     *
     * @param accessToken  sensitive access token
     * @param refreshToken sensitive refresh token
     * @param expiresIn    positive access-token lifetime in seconds
     * @author Kimi Liu
     */
    private record TokenEnvelope(String accessToken, String refreshToken, long expiresIn) {

        /**
         * Validates one complete private token success envelope.
         *
         * @throws IllegalArgumentException if either token is blank
         * @throws ValidateException        if the lifetime is not positive
         */
        private TokenEnvelope {
            Assert.notBlank(accessToken, "Meituan private access token must not be blank");
            Assert.notBlank(refreshToken, "Meituan private refresh token must not be blank");
            if (expiresIn <= 0L) {
                throw new ValidateException("Meituan private access-token lifetime must be positive");
            }
        }

        /**
         * Returns a diagnostic representation without token material.
         *
         * @return redacted private token summary
         */
        @Override
        public String toString() {
            return "TokenEnvelope[accessToken=[REDACTED], refreshToken=[REDACTED], expiresIn=" + expiresIn
                    + Symbol.C_BRACKET_RIGHT;
        }

    }

    /**
     * Carries one successful private Meituan profile response after strict branch discrimination.
     *
     * @param openId   stable Meituan account identifier
     * @param nickname display name
     * @param avatar   optional avatar string
     * @author Kimi Liu
     */
    private record ProfileEnvelope(String openId, String nickname, Optional<String> avatar) {

        /**
         * Validates and freezes one private profile envelope.
         *
         * @throws IllegalArgumentException if identity text is blank or the optional container is {@code null}
         */
        private ProfileEnvelope {
            Assert.notBlank(openId, "Meituan profile openid must not be blank");
            Assert.notBlank(nickname, "Meituan profile nickname must not be blank");
            Assert.notNull(avatar, "Meituan profile avatar container must not be null");
            avatar = Optional.ofNullable(avatar.getOrNull());
        }

        /**
         * Returns a diagnostic representation without profile values.
         *
         * @return redacted private profile summary
         */
        @Override
        public String toString() {
            return "ProfileEnvelope[openId=[REDACTED], nickname=[REDACTED], avatar=[REDACTED]]";
        }

    }

    /**
     * Carries one validated private Meituan platform error while preventing message propagation.
     *
     * @param code    exact decimal-string platform code
     * @param message non-blank platform diagnostic retained only during immediate classification
     * @author Kimi Liu
     */
    private record ErrorEnvelope(String code, String message) {

        /**
         * Validates the corrected current error representation.
         *
         * @throws IllegalArgumentException if code or message is blank
         * @throws ValidateException        if the code contains a non-decimal character
         */
        private ErrorEnvelope {
            Assert.notBlank(code, "Meituan error_code must not be blank");
            Assert.notBlank(message, "Meituan error_msg must not be blank");
            for (int index = 0; index < code.length(); index++) {
                final char character = code.charAt(index);
                if (character < Symbol.C_ZERO || character > Symbol.C_NINE) {
                    throw new ValidateException("Meituan error_code must be a decimal string");
                }
            }
        }

        /**
         * Returns a diagnostic representation without the platform message.
         *
         * @return redacted private error summary
         */
        @Override
        public String toString() {
            return "ErrorEnvelope[code=" + code + ", message=[REDACTED]]";
        }

    }

}
