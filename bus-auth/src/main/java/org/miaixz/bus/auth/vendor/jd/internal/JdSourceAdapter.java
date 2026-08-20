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
package org.miaixz.bus.auth.vendor.jd.internal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.Parameter;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2SourceProfile;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.jd.JdDefinition;
import org.miaixz.bus.auth.vendor.jd.JdSourceSettings;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements JD's frozen OAuth authorization and Zeus account-profile Source flow.
 * <p>
 * The public boundary retains the standard OAuth Authorization Request and application-level SourceAuthentication
 * contracts. JD token responses without {@code token_type}, canonical {@code open_id}/{@code xid}, MD5 router signing,
 * and response envelopes remain private and cannot be mistaken for standard OAuth token or UserInfo responses.
 * </p>
 *
 * @author Kimi Liu
 */
public final class JdSourceAdapter implements VendorAdapter {

    /**
     * Exact JD Zeus account-profile method name.
     */
    private static final String PROFILE_METHOD = "jingdong.user.getUserInfoByOpenId";

    /**
     * Current correctly spelled JD profile response envelope.
     */
    private static final String PROFILE_RESPONSE = "jingdong_user_getUserInfoByOpenId_response";

    /**
     * Historical JD profile response spelling retained by the compatibility contract.
     */
    private static final String PROFILE_RESPONCE = "jingdong_user_getUserInfoByOpenId_responce";

    /**
     * Exact nested JD account-profile result member.
     */
    private static final String PROFILE_RESULT = "getuserinfobyappidandopenid_result";

    /**
     * JD Zeus UTC timestamp formatter built from the shared Bus field pattern.
     */
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern(Fields.NORM_DATETIME);

    /**
     * Maximum bounded JSON body accepted from JD endpoints.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum JSON nesting accepted from JD endpoints.
     */
    private static final int MAXIMUM_JSON_DEPTH = 16;

    /**
     * Registered Source identifier used by produced external identities.
     */
    private final String sourceId;

    /**
     * Selected immutable JD variant definition.
     */
    private final VendorDefinition.Definition variantDefinition;

    /**
     * Validated externally loaded JD settings.
     */
    private final JdSourceSettings settings;

    /**
     * Caller-owned resolver, clock, JSON, executor, and Fabric dependencies.
     */
    private final ExecutionServices services;

    /**
     * Unified public OAuth capability router for the JD authorization operation.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time state lifecycle for JD's browser redirect.
     */
    private final RedirectManager redirectManager;

    /**
     * Shared strict application/x-www-form-urlencoded encoder.
     */
    private final FormCodec formCodec;

    /**
     * Creates one Source-bound JD adapter.
     *
     * @param namespaceId       registration namespace used for state isolation
     * @param sourceId          registered Source identifier
     * @param vendorDefinition  selected JD definition
     * @param variantDefinition selected default definition
     * @param settings          decoded externally loaded JD settings
     * @param services          caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing or protocol differs from jd/default
     */
    public JdSourceAdapter(final String namespaceId, final String sourceId, final JdDefinition vendorDefinition,
            final VendorDefinition.Definition variantDefinition, final JdSourceSettings settings,
            final ExecutionServices services) {
        Assert.notNull(vendorDefinition, "JD definition must not be null");
        this.sourceId = Assert.notBlank(sourceId, "JD Source id must not be blank");
        this.variantDefinition = Assert.notNull(variantDefinition, "JD definition must not be null");
        this.settings = Assert.notNull(settings, "JD settings must not be null");
        this.services = Assert.notNull(services, "JD execution services must not be null");
        if (!JdDefinition.ID.equals(vendorDefinition.type())
                || !vendorDefinition.variant(JdDefinition.DEFAULT).equals(variantDefinition)
                || !JdDefinition.DEFAULT.equals(variantDefinition.variant())
                || variantDefinition.protocol() != Protocol.OAUTH2 || !JdDefinition.ID.equals(settings.vendor())
                || !JdDefinition.DEFAULT.equals(settings.variant())) {
            throw new ValidateException("JD adapter requires the jd/default OAuth 2.0 definition");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variantDefinition, settings, services);
        this.formCodec = new FormCodec();
        this.standardAdapter = new StandardAdapter(variantDefinition, settings, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2SourceProfile.AUTHORIZATION,
                                (request, context, timeout) -> authorize(request))));
    }

    /**
     * Selects JD's historical {@code open_id} or current {@code xid} as one canonical subject.
     *
     * @param object decoded token success
     * @return stable canonical open identifier
     */
    private static String canonicalOpenId(final JsonValue.ObjectValue object) {
        final String openId = optionalString(object, "open_id");
        final String xid = optionalString(object, "xid");
        if (openId == null && xid == null || openId != null && openId.isBlank() || xid != null && xid.isBlank()
                || openId != null && xid != null && !openId.equals(xid)) {
            throw new ValidateException("JD token response lacks one consistent canonical open identifier");
        }
        return openId == null ? xid : openId;
    }

    /**
     * Produces JD's uppercase MD5 signature from sorted non-empty raw parameters.
     *
     * @param appSecret  sensitive operation-scoped application secret
     * @param parameters raw unencoded system parameters
     * @return uppercase 32-character hexadecimal signature
     */
    private static String signature(final String appSecret, final Map<String, String> parameters) {
        final StringBuilder canonical = new StringBuilder(appSecret);
        new TreeMap<>(parameters).forEach((name, value) -> {
            if (name != null && !name.isEmpty() && value != null && !value.isEmpty()) {
                canonical.append(name).append(value);
            }
        });
        canonical.append(appSecret);
        return Builder.md5().digestHex(canonical.toString(), Charset.UTF_8).toUpperCase(Locale.ROOT);
    }

    /**
     * Classifies JD's current top-level code/msg/requestId error shape.
     *
     * @param object      decoded response
     * @param status      HTTP status
     * @param description safe operation description
     * @param <T>         expected success type
     * @return rejected platform error or operational failure
     */
    private static <T> Outcome<T> currentError(
            final JsonValue.ObjectValue object,
            final int status,
            final String description) {
        if (object.values().size() != 3 || !object.values().containsKey("code") || !object.values().containsKey("msg")
                || !object.values().containsKey("requestId")) {
            throw new ValidateException("JD current error envelope is invalid");
        }
        final long code = exactLong(required(object, "code"), "code");
        requiredString(object, "msg");
        final String requestId = requiredString(object, "requestId");
        return classifiedError(code, requestId, status, description);
    }

    /**
     * Classifies JD's historical nested error_response shape.
     *
     * @param object      decoded response
     * @param status      HTTP status
     * @param description safe operation description
     * @param <T>         expected success type
     * @return rejected platform error or operational failure
     */
    private static <T> Outcome<T> legacyError(
            final JsonValue.ObjectValue object,
            final int status,
            final String description) {
        if (object.values().size() != 1 || !object.values().containsKey("error_response")) {
            throw new ValidateException("JD historical error envelope is invalid");
        }
        final JsonValue.ObjectValue error = object(required(object, "error_response"), "JD error_response");
        if (!legacyErrorMembers(error)) {
            throw new ValidateException("JD historical error_response contains an unknown member");
        }
        requiredString(error, "zh_desc");
        final JsonValue codeValue = error.values().get("code");
        final long code = codeValue == null ? 0L : exactLong(codeValue, "code");
        final String requestId = optionalString(error, "request_id");
        optionalString(error, "en_desc");
        return classifiedError(code, requestId, status, description);
    }

    /**
     * Verifies the documented members of JD's historical error object.
     *
     * @param object decoded historical error object
     * @return whether every present member has a registered meaning
     */
    private static boolean legacyErrorMembers(final JsonValue.ObjectValue object) {
        for (String member : object.values().keySet()) {
            final boolean known = switch (member) {
                case "code", "zh_desc", "en_desc", "request_id" -> true;
                default -> false;
            };
            if (!known) {
                return false;
            }
        }
        return true;
    }

    /**
     * Maps a structurally valid JD platform error without retaining descriptions or bodies.
     *
     * @param code        platform numeric error code when supplied
     * @param requestId   platform request identifier when supplied
     * @param status      HTTP status
     * @param description safe operation description
     * @param <T>         expected success type
     * @return rejected caller error or failed upstream condition
     */
    private static <T> Outcome<T> classifiedError(
            final long code,
            final String requestId,
            final int status,
            final String description) {
        final Map<String, JsonValue> details = new LinkedHashMap<>();
        details.put("status", number(status));
        if (code != 0L) {
            details.put("vendor_code", number(code));
        }
        if (requestId != null && !requestId.isBlank()) {
            final String digest = Builder.sha256().digestHex(requestId, Charset.UTF_8).toLowerCase(Locale.ROOT);
            details.put("request_id_sha256", new JsonValue.StringValue(digest));
        }
        if (status == Http.Status.TOO_MANY_REQUESTS || status >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(ErrorCode._502, "JD endpoint returned an upstream error", details);
        }
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Requires one provider-neutral JSON object.
     *
     * @param value JSON value candidate
     * @param label safe structural label
     * @return object value
     */
    private static JsonValue.ObjectValue object(final JsonValue value, final String label) {
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException(label + " must be a JSON object");
        }
        return object;
    }

    /**
     * Returns one required JSON member.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return present member
     */
    private static JsonValue required(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            throw new ValidateException("JD response lacks required member: " + name);
        }
        return value;
    }

    /**
     * Reads one required non-blank JSON string.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return non-blank string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = optionalString(object, name);
        if (value == null || value.isBlank()) {
            throw new ValidateException("JD response requires non-blank string member: " + name);
        }
        return value;
    }

    /**
     * Reads one optional JSON string.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return string or {@code null} when absent
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("JD response member must be a JSON string: " + name);
        }
        return string.value();
    }

    /**
     * Reads one exact integral JSON number.
     *
     * @param value number candidate
     * @param name  safe member name
     * @return exact long
     */
    private static long exactLong(final JsonValue value, final String name) {
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("JD response member must be a JSON number: " + name);
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException cause) {
            throw new ValidateException("JD response member must be an exact long: " + name, cause);
        }
    }

    /**
     * Reads one positive exact integral JSON number.
     *
     * @param value number candidate
     * @param name  safe member name
     * @return positive exact long
     */
    private static long positiveLong(final JsonValue value, final String name) {
        final long decoded = exactLong(value, name);
        if (decoded <= 0L) {
            throw new ValidateException("JD response member must be positive: " + name);
        }
        return decoded;
    }

    /**
     * Converts an open secret lease to a transient UTF-8 form string and clears copied bytes.
     *
     * @param lease open application-secret lease
     * @return sensitive form or signing string
     */
    private static String secret(final SecretLease lease) {
        final char[] material = lease.material();
        try {
            return new String(material);
        } finally {
            clear(material);
        }
    }

    /**
     * Creates one exact integral JSON number.
     *
     * @param value integral value
     * @return JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
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
     * Clears transient sensitive bytes when present.
     *
     * @param value bytes or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) 0);
        }
    }

    /**
     * Clears transient sensitive characters when present.
     *
     * @param value characters or {@code null}
     */
    private static void clear(final char[] value) {
        if (value != null) {
            Arrays.fill(value, '\0');
        }
    }

    /**
     * Narrows a delegated outcome through the declared response class.
     *
     * @param stage        delegated stage
     * @param responseType declared response class
     * @param <S>          expected success type
     * @return type-safe outcome stage
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
     * Creates an already completed asynchronous outcome.
     *
     * @param outcome completed outcome
     * @param <T>     success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates a safe expected JD rejection.
     *
     * @param description non-sensitive description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, emptyObject()));
    }

    /**
     * Creates a safe JD operational failure using a shared Bus error code.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive description
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final org.miaixz.bus.core.basic.normal.Errors code, final String description) {
        return Outcome.failed(new Outcome.Failure(code, description, emptyObject()));
    }

    /**
     * Creates a safe JD operational failure with bounded non-sensitive details.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive description
     * @param details     bounded failure details
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(
            final org.miaixz.bus.core.basic.normal.Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.failed(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Returns JD's exact frozen Source capability manifest.
     *
     * @return immutable redirect Source authentication and O2A manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variantDefinition.manifest();
    }

    /**
     * Routes only JD capabilities declared by the selected variant definition.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing JD-private token or profile records
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "JD capability must not be null");
        Assert.notNull(context, "JD invocation context must not be null");
        Assert.notNull(timeout, "JD invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("JD capability is not declared"));
        }
        if (capability.key().equals(SourceAuthentication.INITIATE.key())
                && request instanceof SourceAuthenticationRequest.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (capability.key().equals(SourceAuthentication.COMPLETE.key())
                && request instanceof SourceAuthenticationRequest.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::complete, context, timeout),
                    capability.responseType());
        }
        if (standardAdapter.manifest().capabilities().contains(capability)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return completed(rejected("JD capability request is invalid"));
    }

    /**
     * Builds JD's exact authorization redirect from generated one-time state.
     *
     * @param initiation generated browser correlation
     * @param context    immutable invocation context retained for the uniform callback
     * @param timeout    shared end-to-end budget
     * @return exact prepared redirect and state binding
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "JD authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "JD authorization has no remaining time budget"));
        }
        if (initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "JD browser flow must not generate nonce or PKCE material"));
        }
        final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, settings.clientId(),
                settings.redirectUri(), Optional.of(new Scope(settings.scopes())), Optional.of(initiation.state()),
                Optional.empty(), Optional.empty(), emptyObject());
        return standardAdapter.invoke(OAuth2SourceProfile.AUTHORIZATION, request, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<UnoUrl> success -> Outcome
                            .succeeded(new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                    case Outcome.Rejected<UnoUrl> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<UnoUrl> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Maps the standard OAuth client identifier to JD's registered {@code app_key} query field.
     *
     * @param request standard OAuth Authorization Request
     * @return exact JD authorization URL or a safe rejection
     */
    private CompletionStage<Outcome<UnoUrl>> authorize(final AuthorizationRequest request) {
        try {
            if (!valid(request)) {
                return completed(rejected("JD Authorization Request differs from the registered definition"));
            }
            final UnoUrl url = variantDefinition.targets().resolve(settings).authorization().getOrNull().url()
                    .newBuilder().query("app_key", settings.clientId())
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query(OAuth2.Parameters.REDIRECT_URI, settings.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.SCOPE, request.scope().getOrNull().format())
                    .query(OAuth2.Parameters.STATE, request.state().getOrNull()).build();
            return completed(Outcome.succeeded(url));
        } catch (RuntimeException cause) {
            return completed(rejected("JD Authorization Request is invalid"));
        }
    }

    /**
     * Validates the exact standard Authorization Request subset accepted by JD.
     *
     * @param request request to inspect
     * @return whether all registered values match and PKCE is absent
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request == null ? null : request.scope().getOrNull();
        return request != null && ResponseType.CODE.equals(request.responseType())
                && settings.clientId().equals(request.clientId())
                && settings.redirectUri().equals(request.redirectUri()) && scope != null
                && settings.scopes().equals(scope.values()) && request.state().isPresent()
                && request.codeChallenge().isEmpty() && request.codeChallengeMethod().isEmpty()
                && request.extensions().values().isEmpty();
    }

    /**
     * Extracts the unique state from an exact JD callback branch.
     *
     * @param callback raw inbound callback
     * @return non-blank state
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Validates and indexes one JD GET callback.
     *
     * @param callback raw inbound callback
     * @return immutable unique callback values
     * @throws ValidateException if transport, target, multiplicity, or branch shape is invalid
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "JD callback must not be null");
        if (callback.method() != Http.Method.GET || !settings.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("JD callback transport or target is invalid");
        }
        final Map<String, String> values = new LinkedHashMap<>();
        for (Callback.Parameter parameter : callback.parameters()) {
            if (values.putIfAbsent(parameter.name(), parameter.value()) != null) {
                throw new ValidateException("JD callback parameter names must be unique");
            }
        }
        final String code = values.get(OAuth2.Parameters.CODE);
        final String errorValue = values.get(OAuth2.Parameters.ERROR);
        final String description = values.get(OAuth2.Parameters.ERROR_DESCRIPTION);
        final String state = values.get(OAuth2.Parameters.STATE);
        final boolean success = code != null && errorValue == null && description == null && values.size() == 2;
        final boolean error = code == null && errorValue != null && values.size() == (description == null ? 2 : 3);
        if ((!success && !error) || values.values().stream().anyMatch(String::isBlank)) {
            throw new ValidateException("JD callback has an invalid success or error branch");
        }
        return new CallbackWire(code, errorValue, description, state);
    }

    /**
     * Completes one correlated JD callback through the private token and signed Zeus profile sequence.
     *
     * @param completion consumed callback and state correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return verified JD external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> complete(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._400, "JD authorization callback is invalid"));
        }
        if (values.error() != null) {
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400, "JD authorization endpoint rejected the request",
                                    new JsonValue.ObjectValue(
                                            Map.of("oauth_error", new JsonValue.StringValue(values.error()))))));
        }
        if (completion.correlation().nonce().isPresent() || completion.codeVerifier().isPresent()) {
            return completed(failed(ErrorCode._500, "JD callback contains unexpected OIDC or PKCE material"));
        }
        return services.secretResolver().resolve(settings.credential(), context, timeout)
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(
                            values.code(),
                            success.value(),
                            timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Keeps one application-secret lease open across token exchange and Zeus profile signing.
     *
     * @param code    one-time authorization code
     * @param secret  owned application-secret lease
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
                    case Outcome.Succeeded<PrivateToken> success -> profile(success.value(), secret, timeout);
                    case Outcome.Rejected<PrivateToken> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<PrivateToken> failed -> Outcome.failed(failed.failure());
                };
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "JD authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Sends JD's exact authorization-code token form.
     *
     * @param code    one-time authorization code
     * @param secret  open application-secret lease
     * @param timeout shared end-to-end budget
     * @return private token data containing the canonical open identifier
     */
    private Outcome<PrivateToken> token(final String code, final SecretLease secret, final Timeout.Budget timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "JD token request has no remaining time budget");
            }
            body = formCodec.encode(
                    List.of(
                            new Parameter("app_key", settings.clientId()),
                            new Parameter("app_secret", secret(secret)),
                            new Parameter(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()),
                            new Parameter(OAuth2.Parameters.CODE, code)));
            final String endpoint = variantDefinition.targets().resolve(settings).token().getOrNull().url().toString();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.POST)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "JD token request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Strictly discriminates JD token success from its two documented error shapes.
     *
     * @param response owned token endpoint response
     * @return private token or safely classified failure
     */
    private Outcome<PrivateToken> token(final HttpResponse response) {
        try {
            final JsonValue.ObjectValue object = object(response, MediaType.APPLICATION_JSON_TYPE);
            final boolean currentError = object.values().containsKey("code") || object.values().containsKey("msg")
                    || object.values().containsKey("requestId");
            final boolean legacyError = object.values().containsKey("error_response");
            final boolean success = object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                    || object.values().containsKey(OAuth2.Parameters.REFRESH_TOKEN)
                    || object.values().containsKey("open_id") || object.values().containsKey("xid");
            if ((currentError ? 1 : 0) + (legacyError ? 1 : 0) + (success ? 1 : 0) != 1) {
                throw new ValidateException("JD token response mixes or omits response branches");
            }
            if (currentError) {
                return currentError(object, response.code(), "JD token endpoint rejected the request");
            }
            if (legacyError) {
                return legacyError(object, response.code(), "JD token endpoint rejected the request");
            }
            if (response.code() != Http.Status.OK) {
                return failed(ErrorCode._502, "JD token endpoint returned success fields with a non-success status");
            }
            final String openId = canonicalOpenId(object);
            final Scope scope = Scope.parse(requiredString(object, OAuth2.Parameters.SCOPE));
            if (!scope.values().containsAll(settings.scopes())) {
                throw new ValidateException("JD token response scope omits a requested scope");
            }
            return Outcome.succeeded(
                    new PrivateToken(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN),
                            requiredString(object, OAuth2.Parameters.REFRESH_TOKEN),
                            positiveLong(required(object, OAuth2.Parameters.EXPIRES_IN), OAuth2.Parameters.EXPIRES_IN),
                            scope, openId));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "JD token endpoint returned an invalid response");
        }
    }

    /**
     * Signs and invokes JD's Zeus account-profile method.
     *
     * @param token   private token and canonical identity data
     * @param secret  open application-secret lease
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> profile(
            final PrivateToken token,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        byte[] profileJson = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "JD profile request has no remaining time budget");
            }
            profileJson = services.jsonProvider()
                    .writeValue(new JsonValue.ObjectValue(Map.of("openId", new JsonValue.StringValue(token.openId()))));
            final String parameterJson = new String(profileJson, Charset.UTF_8);
            final String timestamp = LocalDateTime.ofInstant(timeout.clock().now(), ZoneOffset.UTC)
                    .format(TIMESTAMP_FORMAT);
            final Map<String, String> parameters = new LinkedHashMap<>();
            parameters.put(OAuth2.Parameters.ACCESS_TOKEN, token.accessToken());
            parameters.put("app_key", settings.clientId());
            parameters.put("method", PROFILE_METHOD);
            parameters.put("360buy_param_json", parameterJson);
            parameters.put("timestamp", timestamp);
            parameters.put("v", "2.0");
            final String signature = signature(secret(secret), parameters);
            UnoUrl.Builder url = variantDefinition.targets().resolve(settings).userInfo().getOrNull().url()
                    .newBuilder();
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                url = url.query(parameter.getKey(), parameter.getValue());
            }
            final String endpoint = url.query("sign", signature).build().toString();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.POST)
                    .header(Http.Header.ACCEPT, MediaType.TEXT_PLAIN).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
                    return failed(ErrorCode._429, "JD profile endpoint rate limited the request");
                }
                if (response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
                    return failed(ErrorCode._502, "JD profile endpoint returned an upstream error");
                }
                final JsonValue.ObjectValue object = object(response, MediaType.TEXT_PLAIN_TYPE);
                if (object.values().containsKey("error_response")) {
                    return legacyError(object, response.code(), "JD profile endpoint rejected the request");
                }
                if (response.code() != Http.Status.OK) {
                    return failed(ErrorCode._502, "JD profile endpoint returned an invalid status");
                }
                return identity(object, token.openId(), timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "JD profile request or response is invalid");
        } finally {
            clear(profileJson);
        }
    }

    /**
     * Decodes JD's two exact profile envelope spellings and maps verified attributes.
     *
     * @param object  decoded top-level response
     * @param openId  canonical identity key obtained from the token response
     * @param timeout shared clock used for evidence time
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> identity(
            final JsonValue.ObjectValue object,
            final String openId,
            final Timeout.Budget timeout) {
        final boolean current = object.values().containsKey(PROFILE_RESPONSE);
        final boolean historical = object.values().containsKey(PROFILE_RESPONCE);
        if (current == historical || object.values().size() != 1) {
            throw new ValidateException("JD profile response must contain exactly one registered envelope spelling");
        }
        final JsonValue.ObjectValue envelope = object(
                required(object, current ? PROFILE_RESPONSE : PROFILE_RESPONCE),
                "JD profile envelope");
        if (envelope.values().size() != 1 || !envelope.values().containsKey(PROFILE_RESULT)) {
            throw new ValidateException("JD profile envelope contains an invalid member set");
        }
        final JsonValue.ObjectValue result = object(required(envelope, PROFILE_RESULT), "JD profile result");
        if (!result.values().containsKey("code") || !result.values().containsKey("msg")
                || !result.values().containsKey("data") || exactLong(required(result, "code"), "code") != 0L) {
            throw new ValidateException("JD profile business result is not successful");
        }
        requiredString(result, "msg");
        final JsonValue.ObjectValue data = object(required(result, "data"), "JD profile data");
        final String nickname = requiredString(data, "nickName");
        final Map<String, JsonValue> attributes = new LinkedHashMap<>();
        attributes.put("nickName", new JsonValue.StringValue(nickname));
        final String image = optionalString(data, "imageUrl");
        if (image != null) {
            if (image.isBlank()) {
                throw new ValidateException("JD profile imageUrl must not be blank when present");
            }
            attributes.put("imageUrl", new JsonValue.StringValue(image));
        }
        final JsonValue genderValue = data.values().get("gendar");
        if (genderValue != null) {
            final String genderText;
            if (genderValue instanceof JsonValue.StringValue string
                    && (Symbol.ZERO.equals(string.value()) || Symbol.ONE.equals(string.value()))) {
                genderText = string.value();
            } else if (genderValue instanceof JsonValue.NumberValue) {
                final long number = exactLong(genderValue, "gendar");
                if (number != 0L && number != 1L) {
                    throw new ValidateException("JD numeric gendar must be zero or one");
                }
                genderText = Long.toString(number);
            } else {
                throw new ValidateException("JD gendar has an unsupported type or value");
            }
            attributes.put("gender", new JsonValue.StringValue(Gender.of(genderText).name()));
        }
        final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                new Evidence.Claim("open_id", new JsonValue.StringValue(openId), "https://open.jd.com",
                        timeout.clock().now()));
        return Outcome.succeeded(
                new ExternalIdentity(sourceId, openId, new JsonValue.ObjectValue(attributes), List.of(evidence)));
    }

    /**
     * Strictly reads one bounded JD JSON object under the documented response media type.
     *
     * @param response  response whose body remains owned by the caller
     * @param mediaType expected compatible media type
     * @return immutable provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final HttpResponse response, final MediaType mediaType) {
        if (!mediaType.isCompatible(response.body().media())) {
            throw new ValidateException("JD response media type is invalid");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        return object(value, "JD response");
    }

    /**
     * Carries JD token data only inside one Source completion invocation.
     *
     * @param accessToken  sensitive access token
     * @param refreshToken sensitive refresh token
     * @param expiresIn    positive lifetime in seconds
     * @param scope        effective standard scope
     * @param openId       canonical stable JD identity key
     * @author Kimi Liu
     */
    private record PrivateToken(String accessToken, String refreshToken, long expiresIn, Scope scope, String openId) {

        /**
         * Validates the private token data retained for the immediately following profile call.
         */
        private PrivateToken {
            Assert.notBlank(accessToken, "JD access token must not be blank");
            Assert.notBlank(refreshToken, "JD refresh token must not be blank");
            if (expiresIn <= 0L) {
                throw new ValidateException("JD access-token lifetime must be positive");
            }
            Assert.notNull(scope, "JD effective scope must not be null");
            Assert.notBlank(openId, "JD canonical open identifier must not be blank");
        }

        /**
         * Returns a diagnostic summary without token or identity material.
         *
         * @return redacted token summary
         */
        @Override
        public String toString() {
            return "PrivateToken[accessToken=[REDACTED], refreshToken=[REDACTED], expiresIn=" + expiresIn + ", scope="
                    + scope + ", openId=[REDACTED]]";
        }

    }

    /**
     * Carries one validated JD authorization response without an untyped field contract.
     *
     * @param code             authorization code on the success branch
     * @param error            standard OAuth error on the error branch
     * @param errorDescription optional provider error description
     * @param state            mandatory browser correlation state
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String error, String errorDescription, String state) {

        /**
         * Retains callback values after transport and branch validation.
         */
        private CallbackWire {
            // No initialization required.
        }

    }

}
