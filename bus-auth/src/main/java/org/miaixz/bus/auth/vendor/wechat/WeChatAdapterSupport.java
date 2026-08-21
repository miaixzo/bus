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
package org.miaixz.bus.auth.vendor.wechat;

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
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.*;
import org.miaixz.bus.auth.source.DriverServices;
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
 * Implements the exact authentication chain of every frozen WeChat Source variant.
 * <p>
 * Open Platform and Official Account expose only their OAuth 2.0 authorization operation; query-authenticated token and
 * profile documents stay private because they are not standard token or UserInfo responses. Mini Program and WeCom
 * operations are proprietary Source-authentication steps. Access tokens, refresh tokens, session keys, provider tokens,
 * authorization codes, and client secrets never enter an identity, failure detail, or public response model.
 * </p>
 *
 * @author Kimi Liu
 */
public abstract class WeChatAdapterSupport implements VendorAdapter {

    /**
     * Trusted public WeChat API authority used by Open, Official Account, and Mini Program evidence.
     */
    private static final String WECHAT_AUTHORITY = "https://api.weixin.qq.com";

    /**
     * Trusted WeCom API authority used by all enterprise identity evidence.
     */
    private static final String WORK_AUTHORITY = "https://qyapi.weixin.qq.com";

    /**
     * Replay-purpose label of one Mini Program login code.
     */
    private static final String MINI_CODE_PURPOSE = "wechat-mini-code";

    /**
     * WeChat profile language retained from the historical providers.
     */
    private static final String PROFILE_LANGUAGE = "zh_CN";

    /**
     * Maximum response body accepted from any WeChat endpoint.
     */
    private static final long MAXIMUM_RESPONSE_BYTES = Normal.MEBI;

    /**
     * Maximum JSON nesting accepted from WeChat endpoints.
     */
    private static final int MAXIMUM_JSON_DEPTH = 32;

    /**
     * Registered namespace used to isolate replay and browser correlation keys.
     */
    private final String namespaceId;

    /**
     * Registered Source identifier copied into verified external identities.
     */
    private final String sourceId;

    /**
     * Complete WeChat manifest owning product-specific endpoint associations.
     */
    private final WeChatManifest manifest;

    /**
     * Exact immutable selected WeChat variant manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded WeChat registration values.
     */
    private final WeChatOptions options;

    /**
     * Caller-owned secret, replay, JSON, network, clock, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Browser correlation lifecycle, absent only for Mini Program.
     */
    private final RedirectManager redirectManager;

    /**
     * Strict standard OAuth authorization callback decoder used by Open and Official Account.
     */
    private final AuthorizationResponseDecoder callbackDecoder;

    /**
     * Uniform capability adapter for the public OAuth authorization operation.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Creates one Source-bound adapter for the selected frozen WeChat variant.
     *
     * @param namespaceId registration namespace isolating state, replay, and credentials
     * @param sourceId    registered Source identifier
     * @param manifest    selected WeChat manifest
     * @param variant     exact selected variant manifest
     * @param options     decoded externally loaded options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, variant, protocol, or options routing is inconsistent
     */
    protected WeChatAdapterSupport(final String namespaceId, final String sourceId, final WeChatManifest manifest,
            final VariantManifest.Variant variant, final WeChatOptions options, final DriverServices services) {
        final WeChatManifest selected = Assert.notNull(manifest, "WeChat manifest must not be null");
        this.namespaceId = Assert.notBlank(namespaceId, "WeChat namespace id must not be blank");
        this.sourceId = Assert.notBlank(sourceId, "WeChat Source id must not be blank");
        this.manifest = selected;
        this.variant = Assert.notNull(variant, "WeChat manifest must not be null");
        this.options = Assert.notNull(options, "WeChat options must not be null");
        this.services = Assert.notNull(services, "WeChat execution services must not be null");
        if (!WeChatManifest.ID.equals(selected.vendor()) || !selected.variant(options.variant()).equals(variant)
                || !options.variant().equals(variant.variant()) || !WeChatManifest.ID.equals(options.vendor())
                || oauthVariant() && variant.protocol() != Protocol.OAUTH2
                || !oauthVariant() && variant.protocol() != Protocol.VENDOR_AUTH) {
            throw new ValidateException("WeChat adapter profile, variant, protocol, and options must match");
        }
        this.redirectManager = WeChatManifest.MINI.equals(options.variant()) ? null
                : RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.callbackDecoder = new AuthorizationResponseDecoder();
        this.standardAdapter = new StandardAdapter(variant, options, Optional.ofNullable(redirectManager),
                oauthVariant() ? List.of(
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION,
                                (request, context, timeout) -> authorization(request)))
                        : List.of());
    }

    /**
     * Classifies HTTP or platform errors and returns {@code null} only for an exact success status.
     *
     * @param response  owned platform response
     * @param object    decoded response object
     * @param operation non-sensitive operation label
     * @param <T>       expected successful value type
     * @return rejection/failure, or {@code null} for HTTP 200 with absent/zero errcode
     */
    private static <T> Outcome<T> platformError(
            final HttpResponse response,
            final JsonValue.ObjectValue object,
            final String operation) {
        if (response.code() == Http.Status.TOO_MANY_REQUESTS || response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(ErrorCode._502, operation + " is unavailable");
        }
        if (response.code() != Http.Status.OK) {
            return rejected(operation + " rejected the request");
        }
        final JsonValue value = object.values().get("errcode");
        if (value == null || value instanceof JsonValue.NullValue) {
            if (object.values().containsKey("errmsg")) {
                throw new ValidateException("WeChat errmsg requires errcode");
            }
            return null;
        }
        final long code = exactLong(object, "errcode");
        optionalString(object, "errmsg");
        return code == 0L ? null : rejected(operation + " returned a platform error");
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
            throw new ValidateException("WeChat response requires a non-blank string member: " + name);
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
            throw new ValidateException("WeChat response member must be a non-blank JSON string: " + name);
        }
        return string.value();
    }

    /**
     * Reads one required nested JSON object.
     *
     * @param object decoded parent object
     * @param name   exact nested member name
     * @return required nested object
     */
    private static JsonValue.ObjectValue requiredObject(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.ObjectValue nested)) {
            throw new ValidateException("WeChat response requires a JSON object member: " + name);
        }
        return nested;
    }

    /**
     * Validates one optional nested JSON object while deliberately excluding it from identity attributes.
     *
     * @param object decoded parent object
     * @param name   exact optional member name
     */
    private static void optionalObject(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value != null && !(value instanceof JsonValue.NullValue) && !(value instanceof JsonValue.ObjectValue)) {
            throw new ValidateException("WeChat optional response member must be a JSON object: " + name);
        }
    }

    /**
     * Validates one optional nested JSON array while deliberately excluding it from identity attributes.
     *
     * @param object decoded parent object
     * @param name   exact optional member name
     */
    private static void optionalArray(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value != null && !(value instanceof JsonValue.NullValue) && !(value instanceof JsonValue.ArrayValue)) {
            throw new ValidateException("WeChat optional response member must be a JSON array: " + name);
        }
    }

    /**
     * Verifies every member of one selected private WeChat document by semantic document kind.
     *
     * @param kind   selected private document kind
     * @param object decoded platform object
     * @return whether every decoded member is registered for the selected document
     */
    private static boolean members(final WireKind kind, final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            final boolean known = switch (kind) {
                case WECHAT_TOKEN -> switch (name) {
                    case "errcode", "errmsg", OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.EXPIRES_IN, OAuth2.Parameters.REFRESH_TOKEN, "openid", OAuth2.Parameters.SCOPE, "unionid", "is_snapshotuser" -> true;
                    default -> false;
                };
                case WECHAT_PROFILE -> switch (name) {
                    case "errcode", "errmsg", "openid", "nickname", "sex", "province", "city", "country", "headimgurl", "privilege", "unionid" -> true;
                    default -> false;
                };
                case MINI -> switch (name) {
                    case "errcode", "errmsg", "session_key", "openid", "unionid" -> true;
                    default -> false;
                };
                case WORK_TOKEN -> switch (name) {
                    case "errcode", "errmsg", OAuth2.Parameters.ACCESS_TOKEN, "provider_access_token", OAuth2.Parameters.EXPIRES_IN -> true;
                    default -> false;
                };
                case EE_IDENTITY -> switch (name) {
                    case "errcode", "errmsg", "userid", "user_ticket", OAuth2.Parameters.EXPIRES_IN, "open_userid" -> true;
                    default -> false;
                };
                case EE_MEMBER -> switch (name) {
                    case "errcode", "errmsg", "userid", "name", "department", "order", "position", "mobile", "gender", "email", "biz_mail", "is_leader_in_dept", "direct_leader", "avatar", "thumb_avatar", "telephone", "alias", "address", "open_userid", "main_department", "extattr", "external_position", "external_profile", "status", "qr_code" -> true;
                    default -> false;
                };
                case EE_SENSITIVE -> switch (name) {
                    case "errcode", "errmsg", "userid", "gender", "avatar", "qr_code", "mobile", "email", "biz_mail", "address" -> true;
                    default -> false;
                };
                case EE_WEB_IDENTITY -> switch (name) {
                    case "errcode", "errmsg", "UserId", "DeviceId", "OpenId" -> true;
                    default -> false;
                };
                case WORK_THIRD_PARTY -> switch (name) {
                    case "errcode", "errmsg", "usertype", "corp_info", "user_info", "redirect_login_info", "agent", "auth_info" -> true;
                    default -> false;
                };
                case WORK_CORPORATION -> "corpid".equals(name);
                case WORK_USER -> switch (name) {
                    case "userid", "open_userid", "name", "avatar" -> true;
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
     * Reads one exact integral JSON number or decimal string.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return exact long value
     */
    private static long exactLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        try {
            if (value instanceof JsonValue.NumberValue number) {
                return number.value().longValueExact();
            }
            if (value instanceof JsonValue.StringValue string && !string.value().isBlank()) {
                return Long.parseLong(string.value());
            }
        } catch (ArithmeticException | NumberFormatException cause) {
            throw new ValidateException("WeChat numeric member must be an exact long: " + name, cause);
        }
        throw new ValidateException("WeChat response requires an integral member: " + name);
    }

    /**
     * Reads and validates one positive integral lifetime.
     *
     * @param object decoded response object
     * @param name   exact lifetime member name
     * @return positive lifetime in seconds
     */
    private static long positiveLong(final JsonValue.ObjectValue object, final String name) {
        final long value = exactLong(object, name);
        if (value <= 0L) {
            throw new ValidateException("WeChat lifetime must be positive: " + name);
        }
        return value;
    }

    /**
     * Reads an optional numeric zero-or-one platform marker.
     *
     * @param object decoded response object
     * @param name   exact marker member name
     * @return {@code true} only for marker value one
     */
    private static boolean optionalZeroOrOne(final JsonValue.ObjectValue object, final String name) {
        if (!object.values().containsKey(name) || object.values().get(name) instanceof JsonValue.NullValue) {
            return false;
        }
        final long value = exactLong(object, name);
        if (value != 0L && value != 1L) {
            throw new ValidateException("WeChat marker must be zero or one: " + name);
        }
        return value == 1L;
    }

    /**
     * Copies one optional string member into the identity attribute map.
     *
     * @param source decoded response object
     * @param target mutable map used only during identity construction
     * @param name   exact attribute name
     */
    private static void copyString(
            final JsonValue.ObjectValue source,
            final Map<String, JsonValue> target,
            final String name) {
        add(target, name, optionalString(source, name));
    }

    /**
     * Copies one optional exact integral number into the identity attribute map.
     *
     * @param source decoded response object
     * @param target mutable map used only during identity construction
     * @param name   exact attribute name
     */
    private static void copyNumber(
            final JsonValue.ObjectValue source,
            final Map<String, JsonValue> target,
            final String name) {
        final JsonValue value = source.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return;
        }
        final long number = exactLong(source, name);
        target.put(name, new JsonValue.NumberValue(java.math.BigDecimal.valueOf(number)));
    }

    /**
     * Copies one optional immutable JSON array into the identity attribute map.
     *
     * @param source decoded response object
     * @param target mutable map used only during identity construction
     * @param name   exact attribute name
     */
    private static void copyArray(
            final JsonValue.ObjectValue source,
            final Map<String, JsonValue> target,
            final String name) {
        final JsonValue value = source.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return;
        }
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("WeChat response member must be a JSON array: " + name);
        }
        for (JsonValue item : array.values()) {
            if (!(item instanceof JsonValue.StringValue string) || string.value().isBlank()) {
                throw new ValidateException("WeChat privilege array must contain non-blank strings");
            }
        }
        target.put(name, new JsonValue.ArrayValue(array.values()));
    }

    /**
     * Adds one non-null string attribute without admitting sensitive token material.
     *
     * @param attributes mutable identity attribute map
     * @param name       safe attribute name
     * @param value      optional non-sensitive value
     */
    private static void add(final Map<String, JsonValue> attributes, final String name, final String value) {
        if (value != null) {
            attributes.put(name, new JsonValue.StringValue(value));
        }
    }

    /**
     * Creates one federated identity evidence item.
     *
     * @param claim     exact subject claim name
     * @param subject   verified subject value
     * @param authority trusted platform authority
     * @param timeout   shared clock source
     * @return immutable evidence item
     */
    private static Evidence evidence(
            final String claim,
            final String subject,
            final String authority,
            final Timeout.Budget timeout) {
        return new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                new Evidence.Claim(claim, new JsonValue.StringValue(subject), authority, timeout.clock().now()));
    }

    /**
     * Maps one standard OAuth authorization error to a safe rejected outcome.
     *
     * @param error decoded standard authorization error branch
     * @param <T>   expected successful value type
     * @return rejected outcome retaining only the standard error identifier
     */
    private static <T> Outcome<T> oauthError(final AuthorizationResponseDecoder.Error error) {
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400, "WeChat authorization endpoint returned a standard error",
                        new JsonValue.ObjectValue(
                                Map.of("oauth_error", new JsonValue.StringValue(error.response().error().value())))));
    }

    /**
     * Narrows a delegated Source outcome through the declared response class.
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
     * Erases an optional temporary request body.
     *
     * @param value temporary sensitive bytes, or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) 0);
        }
    }

    /**
     * Materializes one operation-scoped client secret and clears the intermediate character buffer.
     *
     * @param lease open secret lease owned by the current operation
     * @return transient string required by the platform encoder
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
     * Creates an already completed asynchronous outcome.
     *
     * @param outcome completed outcome value
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
     * Accepts one proprietary callback value only once.
     *
     * @param current  previously decoded value
     * @param incoming newly decoded value
     * @return incoming value when no value was previously decoded
     * @throws ValidateException if the parameter occurred more than once
     */
    private static String unique(final String current, final String incoming) {
        if (current != null) {
            throw new ValidateException("WeCom callback parameter names must be unique");
        }
        return incoming;
    }

    /**
     * Copies only registered non-error member values into the identity projection.
     *
     * @param source     validated member document
     * @param attributes mutable operation-local identity attributes
     */
    private static void copyMemberAttributes(
            final JsonValue.ObjectValue source,
            final Map<String, JsonValue> attributes) {
        source.values().forEach((name, value) -> {
            if (!"errcode".equals(name) && !"errmsg".equals(name) && !"userid".equals(name)
                    && !(value instanceof JsonValue.NullValue)) {
                attributes.put(name, value);
            }
        });
    }

    /**
     * Returns the exact selected WeChat variant capability manifest.
     *
     * @return immutable WeChat capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes browser Source authentication, Mini Program direct authentication, or public OAuth authorization.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing private WeChat response models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "WeChat capability must not be null");
        Assert.notNull(context, "WeChat invocation context must not be null");
        Assert.notNull(timeout, "WeChat invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("WeChat capability is not declared by the selected variant"));
        }
        if (!WeChatManifest.MINI.equals(options.variant())
                && capability.key().equals(SourceAuthentication.INITIATE.key())
                && request instanceof SourceAuthentication.Request.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (!WeChatManifest.MINI.equals(options.variant())
                && capability.key().equals(SourceAuthentication.COMPLETE.key())
                && request instanceof SourceAuthentication.Request.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::browserIdentity, context, timeout),
                    capability.responseType());
        }
        if (standardAdapter.manifest().capabilities().contains(capability)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        if (WeChatManifest.MINI.equals(options.variant())
                && capability.key().equals(SourceAuthentication.INITIATE.key())
                && request instanceof SourceAuthentication.Request.OneTimeCode oneTimeCode
                && sourceId.equals(oneTimeCode.sourceId())) {
            return narrow(miniProgram(oneTimeCode.code(), context, timeout), capability.responseType());
        }
        return completed(rejected("WeChat capability request is invalid"));
    }

    /**
     * Builds the selected browser redirect around generated one-time state.
     *
     * @param initiation generated browser correlation material
     * @param context    immutable invocation context retained by the uniform operation signature
     * @param timeout    shared end-to-end budget
     * @return prepared authorization redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "WeChat authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "WeChat browser material violates the selected manifest"));
        }
        try {
            final String location = oauthVariant()
                    ? authorize(
                            new AuthorizationRequest(ResponseType.CODE, options.clientId(), options.redirectUri(),
                                    Optional.of(new Scope(options.scopes())), Optional.of(initiation.state()),
                                    Optional.empty(), Optional.empty(), emptyObject())).toString()
                    : workAuthorization(initiation.state()).toString();
            return completed(Outcome.succeeded(new RedirectManager.Prepared(location, initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("WeChat authorization request is invalid"));
        }
    }

    /**
     * Encodes one validated OAuth authorization request using the selected WeChat field names.
     *
     * @param request standard OAuth 2.0 authorization request
     * @return exact WeChat authorization URL
     */
    private UnoUrl authorize(final AuthorizationRequest request) {
        final UnoUrl.Builder builder = variant.targets().resolve(options).authorization().getOrNull().url()
                .newBuilder();
        if (WeChatManifest.OPEN.equals(options.variant())) {
            return builder.query(OAuth2.Parameters.RESPONSE_TYPE, request.responseType().value())
                    .query("appid", request.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, request.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.SCOPE, String.join(Symbol.COMMA, request.scope().getOrNull().values()))
                    .query(OAuth2.Parameters.STATE, request.state().getOrNull()).build();
        }
        return builder.query("appid", request.clientId())
                .query(OAuth2.Parameters.REDIRECT_URI, request.redirectUri().getOrNull())
                .query(OAuth2.Parameters.RESPONSE_TYPE, request.responseType().value())
                .query(OAuth2.Parameters.SCOPE, String.join(Symbol.COMMA, request.scope().getOrNull().values()))
                .query(OAuth2.Parameters.STATE, request.state().getOrNull()).fragment("wechat_redirect").build();
    }

    /**
     * Applies the standard authorization capability contract before encoding registered WeChat deviations.
     *
     * @param request standard OAuth authorization request
     * @return completed authorization URL outcome
     */
    private CompletionStage<Outcome<UnoUrl>> authorization(final AuthorizationRequest request) {
        try {
            return valid(request) ? completed(Outcome.succeeded(authorize(request)))
                    : completed(rejected("WeChat authorization request does not match the compiled Source"));
        } catch (RuntimeException cause) {
            return completed(rejected("WeChat authorization request is invalid"));
        }
    }

    /**
     * Validates one public standard authorization request against the selected WeChat registration.
     *
     * @param request standard OAuth authorization request
     * @return whether every public request field matches the selected Source
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
     * Encodes one proprietary WeCom browser authorization URL.
     *
     * @param state generated one-time correlation value
     * @return exact selected WeCom redirect target
     */
    private UnoUrl workAuthorization(final String state) {
        final UnoUrl.Builder builder = variant.targets().resolve(options).authorization().getOrNull().url()
                .newBuilder();
        if (WeChatManifest.EE.equals(options.variant())) {
            builder.query("login_type", options.loginType()).query("appid", options.clientId());
            if (!options.agentId().isEmpty()) {
                builder.query("agentid", options.agentId());
            }
            return builder.query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.STATE, state).query("lang", options.language()).fragment("wechat_redirect")
                    .build();
        }
        if (WeChatManifest.EE_QRCODE.equals(options.variant())) {
            return builder.query("appid", options.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.STATE, state).query("usertype", options.userType()).build();
        }
        return builder.query("appid", options.clientId()).query("agentid", options.agentId())
                .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                .query(OAuth2.Parameters.SCOPE, String.join(Symbol.COMMA, options.scopes()))
                .query(OAuth2.Parameters.STATE, state).fragment("wechat_redirect").build();
    }

    /**
     * Extracts the unique state from the selected strict callback branch.
     *
     * @param callback raw inbound callback
     * @return one-time browser correlation value
     */
    private String state(final Callback.Inbound callback) {
        if (oauthVariant()) {
            return switch (decodeOAuth(callback)) {
                case AuthorizationResponseDecoder.Success success -> success.response().state()
                        .orElseThrow(() -> new ValidateException("WeChat OAuth success requires state"));
                case AuthorizationResponseDecoder.Error error -> error.response().state()
                        .orElseThrow(() -> new ValidateException("WeChat OAuth error requires state"));
            };
        }
        return callback(callback).state();
    }

    /**
     * Completes the selected browser flow after RedirectManager consumes its correlation.
     *
     * @param completion consumed browser correlation and raw callback
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return verified WeChat or WeCom external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> browserIdentity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final String code;
        try {
            if (oauthVariant()) {
                final AuthorizationResponseDecoder.Decoded decoded = decodeOAuth(completion.callback());
                if (decoded instanceof AuthorizationResponseDecoder.Error error) {
                    return completed(oauthError(error));
                }
                final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
                if (response.scope().isPresent() || response.issuer().isPresent()
                        || !response.extensions().values().isEmpty()) {
                    return completed(rejected("WeChat OAuth success contains unregistered response parameters"));
                }
                code = response.code();
            } else {
                final CallbackWire values = callback(completion.callback());
                if (values.failed()) {
                    return completed(rejected("WeCom authorization endpoint rejected the browser interaction"));
                }
                code = values.code();
            }
        } catch (RuntimeException cause) {
            return completed(rejected("WeChat authorization callback is invalid"));
        }
        if (completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(failed(ErrorCode._500, "WeChat callback contains unexpected browser material"));
        }
        return resolve(context, timeout).thenCompose(resolved -> switch (resolved) {
            case Outcome.Succeeded<SecretLease> success -> browserIdentity(code, success.value(), timeout);
            case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Runs the private token and identity steps while closing the client-secret lease after token acquisition.
     *
     * @param code    consumed browser authorization code
     * @param secret  owned application or provider secret lease
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> browserIdentity(
            final String code,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            final Outcome<PrivateAccess> access;
            try (secret) {
                access = acquireAccess(code, secret, timeout);
            } catch (RuntimeException cause) {
                return WeChatAdapterSupport
                        .<ExternalIdentity>failed(ErrorCode._502, "WeChat private credential operation failed");
            }
            return switch (access) {
                case Outcome.Succeeded<PrivateAccess> success -> identity(success.value(), timeout);
                case Outcome.Rejected<PrivateAccess> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<PrivateAccess> failed -> Outcome.failed(failed.failure());
            };
        }, services.executor());
    }

    /**
     * Selects the exact private access operation used by the active browser variant.
     *
     * @param code    consumed callback code retained for the following identity request
     * @param secret  live client-secret lease
     * @param timeout shared end-to-end budget
     * @return private access material
     */
    private Outcome<PrivateAccess> acquireAccess(
            final String code,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "WeChat token request has no remaining time budget");
        }
        return WeChatManifest.EE_QRCODE.equals(options.variant()) ? providerAccess(code, secret, timeout)
                : queryAccess(code, secret, timeout);
    }

    /**
     * Sends the historical query-authenticated token or WeCom application-token request.
     *
     * @param code    callback code retained for identity retrieval
     * @param secret  live App Secret or Corp Secret lease
     * @param timeout shared end-to-end budget
     * @return private access material
     */
    private Outcome<PrivateAccess> queryAccess(
            final String code,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        try {
            final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
            final var request = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET);
            if (oauthVariant()) {
                request.query(
                        OAuth2.Parameters.CODE,
                        Assert.notBlank(code, "WeChat authorization code must not be blank"))
                        .query("appid", options.clientId()).query("secret", secret(secret))
                        .query(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value());
            } else {
                request.query("corpid", options.clientId()).query("corpsecret", secret(secret));
            }
            try (HttpResponse response = request.header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(variant.protocol()).addressPolicy()).execute()) {
                return oauthVariant() ? decodeWeChatAccess(response) : decodeWorkAccess(response, code, false);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeChat query credential request failed");
        }
    }

    /**
     * Sends the WeCom service-provider JSON credential request.
     *
     * @param code    callback authorization code retained for get_login_info
     * @param secret  live provider-secret lease
     * @param timeout shared end-to-end budget
     * @return provider access material
     */
    private Outcome<PrivateAccess> providerAccess(
            final String code,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        byte[] body = null;
        try {
            body = services.jsonProvider().writeValue(
                    new JsonValue.ObjectValue(Map.of(
                            "corpid",
                            new JsonValue.StringValue(options.clientId()),
                            "provider_secret",
                            new JsonValue.StringValue(secret(secret)))));
            final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.POST)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.VENDOR_AUTH).addressPolicy())
                    .body(body, MediaType.APPLICATION_JSON_TYPE).execute()) {
                return decodeWorkAccess(response, code, true);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeCom provider credential request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Strictly decodes the non-standard WeChat browser token document without fabricating token type.
     *
     * @param response owned token endpoint response
     * @return private access material or safely classified failure
     */
    private Outcome<PrivateAccess> decodeWeChatAccess(final HttpResponse response) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!members(WireKind.WECHAT_TOKEN, object)) {
                throw new ValidateException("WeChat token response contains an unknown member");
            }
            final Outcome<PrivateAccess> error = platformError(response, object, "WeChat token endpoint");
            if (error != null) {
                return error;
            }
            positiveLong(object, OAuth2.Parameters.EXPIRES_IN);
            optionalString(object, OAuth2.Parameters.REFRESH_TOKEN);
            final String subject = requiredString(object, "openid");
            final String scope = optionalString(object, OAuth2.Parameters.SCOPE);
            final String unionId = optionalString(object, "unionid");
            final boolean snapshot = optionalZeroOrOne(object, "is_snapshotuser");
            return Outcome.succeeded(
                    new PrivateAccess(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN), subject, null, scope,
                            unionId, snapshot));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeChat token endpoint returned an invalid response");
        }
    }

    /**
     * Strictly decodes a WeCom application or provider access-token document.
     *
     * @param response owned token endpoint response
     * @param code     callback authorization code retained for identity retrieval
     * @param provider whether the response must carry {@code provider_access_token}
     * @return private WeCom access material
     */
    private Outcome<PrivateAccess> decodeWorkAccess(
            final HttpResponse response,
            final String code,
            final boolean provider) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!members(WireKind.WORK_TOKEN, object)) {
                throw new ValidateException("WeCom token response contains an unknown member");
            }
            final Outcome<PrivateAccess> error = platformError(response, object, "WeCom token endpoint");
            if (error != null) {
                return error;
            }
            positiveLong(object, OAuth2.Parameters.EXPIRES_IN);
            final String token = requiredString(
                    object,
                    provider ? "provider_access_token" : OAuth2.Parameters.ACCESS_TOKEN);
            if (provider && object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                    || !provider && object.values().containsKey("provider_access_token")) {
                throw new ValidateException("WeCom token response uses the wrong credential member");
            }
            return Outcome.succeeded(
                    new PrivateAccess(token, null, Assert.notBlank(code, "WeCom authorization code must not be blank"),
                            null, null, false));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeCom token endpoint returned an invalid response");
        }
    }

    /**
     * Selects the exact private identity operation after access acquisition.
     *
     * @param access  private access material
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> identity(final PrivateAccess access, final Timeout.Budget timeout) {
        if (oauthVariant()) {
            return wechatIdentity(access, timeout);
        }
        if (WeChatManifest.EE_QRCODE.equals(options.variant())) {
            return thirdPartyIdentity(access, timeout);
        }
        return workIdentity(access, timeout);
    }

    /**
     * Builds a snapshot identity or retrieves and validates the public WeChat profile.
     *
     * @param access  private WeChat token result
     * @param timeout shared end-to-end budget
     * @return verified OpenID identity
     */
    private Outcome<ExternalIdentity> wechatIdentity(final PrivateAccess access, final Timeout.Budget timeout) {
        if (WeChatManifest.MP.equals(options.variant()) && (options.scopes().contains("snsapi_base")
                || "snsapi_base".equals(access.scope()) || access.snapshot())) {
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            attributes.put("snapshot", new JsonValue.BooleanValue(true));
            add(attributes, OAuth2.Parameters.SCOPE, access.scope());
            add(attributes, "unionid", access.unionId());
            return Outcome.succeeded(external(access.subject(), attributes, "openid", WECHAT_AUTHORITY, timeout));
        }
        if (timeout.expired()) {
            return failed(ErrorCode._408, "WeChat profile request has no remaining time budget");
        }
        try {
            final String endpoint = variant.targets().resolve(options).userInfo().getOrNull().url().toString();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                    .query(OAuth2.Parameters.ACCESS_TOKEN, access.token()).query("openid", access.subject())
                    .query("lang", PROFILE_LANGUAGE).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return decodeWeChatProfile(response, access, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeChat profile request failed");
        }
    }

    /**
     * Strictly maps one WeChat profile and binds any returned OpenID to the token document.
     *
     * @param response owned profile endpoint response
     * @param access   private token result containing the authoritative OpenID
     * @param timeout  shared clock used for evidence timestamping
     * @return verified OpenID identity
     */
    private Outcome<ExternalIdentity> decodeWeChatProfile(
            final HttpResponse response,
            final PrivateAccess access,
            final Timeout.Budget timeout) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!members(WireKind.WECHAT_PROFILE, object)) {
                throw new ValidateException("WeChat profile response contains an unknown member");
            }
            final Outcome<ExternalIdentity> error = platformError(response, object, "WeChat profile endpoint");
            if (error != null) {
                return error;
            }
            final String returnedOpenId = optionalString(object, "openid");
            if (returnedOpenId != null && !access.subject().equals(returnedOpenId)) {
                return rejected("WeChat profile OpenID does not match the token response");
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            copyString(object, attributes, "nickname");
            copyNumber(object, attributes, "sex");
            copyString(object, attributes, "province");
            copyString(object, attributes, "city");
            copyString(object, attributes, "country");
            copyString(object, attributes, "headimgurl");
            copyArray(object, attributes, "privilege");
            final String profileUnionId = optionalString(object, "unionid");
            if (access.unionId() != null && profileUnionId != null && !access.unionId().equals(profileUnionId)) {
                return rejected("WeChat profile UnionID does not match the token response");
            }
            add(attributes, "unionid", profileUnionId == null ? access.unionId() : profileUnionId);
            return Outcome.succeeded(external(access.subject(), attributes, "openid", WECHAT_AUTHORITY, timeout));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeChat profile endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves and maps a WeCom corporate QR or web identity.
     *
     * @param access  private application access and callback code
     * @param timeout shared end-to-end budget
     * @return verified WeCom identity
     */
    private Outcome<ExternalIdentity> workIdentity(final PrivateAccess access, final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "WeCom identity request has no remaining time budget");
        }
        try {
            final String endpoint = variant.targets().resolve(options).userInfo().getOrNull().url().toString();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                    .query(OAuth2.Parameters.ACCESS_TOKEN, access.token()).query(OAuth2.Parameters.CODE, access.code())
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.VENDOR_AUTH).addressPolicy())
                    .execute()) {
                return WeChatManifest.EE.equals(options.variant()) ? decodeWorkQrIdentity(response, access, timeout)
                        : decodeWorkWebIdentity(response, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeCom identity request failed");
        }
    }

    /**
     * Strictly maps one WeCom corporate QR identity using lowercase {@code userid}.
     *
     * @param response owned identity endpoint response
     * @param access   application access token retained only for the private member-detail chain
     * @param timeout  shared clock used for evidence timestamping
     * @return verified WeCom user identity
     */
    private Outcome<ExternalIdentity> decodeWorkQrIdentity(
            final HttpResponse response,
            final PrivateAccess access,
            final Timeout.Budget timeout) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!members(WireKind.EE_IDENTITY, object)) {
                throw new ValidateException("WeCom QR identity response contains an unknown member");
            }
            final Outcome<ExternalIdentity> error = platformError(response, object, "WeCom QR identity endpoint");
            if (error != null) {
                return error;
            }
            final String subject = requiredString(object, "userid");
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            copyString(object, attributes, "open_userid");
            final String ticket = optionalString(object, "user_ticket");
            if (ticket != null) {
                positiveLong(object, OAuth2.Parameters.EXPIRES_IN);
            } else if (object.values().containsKey(OAuth2.Parameters.EXPIRES_IN)) {
                throw new ValidateException("WeCom QR expires_in requires user_ticket");
            }
            return enterpriseIdentity(access.token(), subject, ticket, attributes, timeout);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeCom QR identity response is invalid");
        }
    }

    /**
     * Reads the visible enterprise member and optionally exchanges the callback ticket for authorized sensitive fields.
     *
     * @param token      private application access token
     * @param subject    verified callback userid
     * @param userTicket optional one-time member ticket
     * @param attributes identity attributes established by the callback response
     * @param timeout    shared end-to-end budget
     * @return verified and enriched enterprise identity
     */
    private Outcome<ExternalIdentity> enterpriseIdentity(
            final String token,
            final String subject,
            final String userTicket,
            final Map<String, JsonValue> attributes,
            final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "WeCom member request has no remaining time budget");
        }
        final WeChatTargets targets = manifest.enterpriseTargets();
        try (HttpResponse response = Fabric.http(services.fabricContext())
                .url(targets.member().endpoint().url().toString()).method(Http.Method.GET)
                .query(OAuth2.Parameters.ACCESS_TOKEN, token).query("userid", subject)
                .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                .addressPolicy(services.securityBaseline().require(Protocol.VENDOR_AUTH).addressPolicy()).execute()) {
            final Outcome<ExternalIdentity> member = decodeEnterpriseMember(
                    response,
                    token,
                    subject,
                    userTicket,
                    attributes,
                    timeout);
            return member;
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeCom member request failed");
        }
    }

    /**
     * Validates the visible member document before entering the optional sensitive-member operation.
     *
     * @param response   owned member response
     * @param token      private application access token
     * @param subject    verified callback userid
     * @param userTicket optional one-time member ticket
     * @param attributes mutable operation-local safe attribute projection
     * @param timeout    shared end-to-end budget
     * @return verified and enriched enterprise identity
     */
    private Outcome<ExternalIdentity> decodeEnterpriseMember(
            final HttpResponse response,
            final String token,
            final String subject,
            final String userTicket,
            final Map<String, JsonValue> attributes,
            final Timeout.Budget timeout) {
        final JsonValue.ObjectValue object = object(response);
        if (!members(WireKind.EE_MEMBER, object)) {
            return failed(ErrorCode._502, "WeCom member response contains an unknown member");
        }
        final Outcome<ExternalIdentity> error = platformError(response, object, "WeCom member endpoint");
        if (error != null) {
            return error;
        }
        if (!subject.equals(requiredString(object, "userid"))) {
            return rejected("WeCom member userid does not match the login identity");
        }
        copyMemberAttributes(object, attributes);
        return userTicket == null ? Outcome.succeeded(external(subject, attributes, "userid", WORK_AUTHORITY, timeout))
                : sensitiveIdentity(token, subject, userTicket, attributes, timeout);
    }

    /**
     * Exchanges a user ticket for member-approved sensitive fields and clears the temporary request body.
     *
     * @param token      private application access token
     * @param subject    verified callback userid
     * @param userTicket one-time member ticket
     * @param attributes mutable operation-local safe attribute projection
     * @param timeout    shared end-to-end budget
     * @return verified enterprise identity containing authorized fields
     */
    private Outcome<ExternalIdentity> sensitiveIdentity(
            final String token,
            final String subject,
            final String userTicket,
            final Map<String, JsonValue> attributes,
            final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "WeCom sensitive-member request has no remaining time budget");
        }
        byte[] body = null;
        try {
            body = services.jsonProvider().writeValue(
                    new JsonValue.ObjectValue(Map.of("user_ticket", new JsonValue.StringValue(userTicket))));
            final WeChatTargets targets = manifest.enterpriseTargets();
            try (HttpResponse response = Fabric.http(services.fabricContext())
                    .url(targets.sensitive().endpoint().url().toString()).method(Http.Method.POST)
                    .query(OAuth2.Parameters.ACCESS_TOKEN, token).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.VENDOR_AUTH).addressPolicy())
                    .body(body, MediaType.APPLICATION_JSON_TYPE).execute()) {
                final JsonValue.ObjectValue object = object(response);
                if (!members(WireKind.EE_SENSITIVE, object)) {
                    return failed(ErrorCode._502, "WeCom sensitive-member response contains an unknown member");
                }
                final Outcome<ExternalIdentity> error = platformError(
                        response,
                        object,
                        "WeCom sensitive-member endpoint");
                if (error != null) {
                    return error;
                }
                if (!subject.equals(requiredString(object, "userid"))) {
                    return rejected("WeCom sensitive-member userid does not match the login identity");
                }
                copyMemberAttributes(object, attributes);
                return Outcome.succeeded(external(subject, attributes, "userid", WORK_AUTHORITY, timeout));
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeCom sensitive-member request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Strictly maps one WeCom web identity using the official case-sensitive {@code UserId} field.
     *
     * @param response owned identity endpoint response
     * @param timeout  shared clock used for evidence timestamping
     * @return verified WeCom user identity
     */
    private Outcome<ExternalIdentity> decodeWorkWebIdentity(final HttpResponse response, final Timeout.Budget timeout) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!members(WireKind.EE_WEB_IDENTITY, object)) {
                throw new ValidateException("WeCom web identity response contains an unknown member");
            }
            final Outcome<ExternalIdentity> error = platformError(response, object, "WeCom web identity endpoint");
            if (error != null) {
                return error;
            }
            final String subject = requiredString(object, "UserId");
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            copyString(object, attributes, "DeviceId");
            copyString(object, attributes, "OpenId");
            return Outcome.succeeded(external(subject, attributes, "UserId", WORK_AUTHORITY, timeout));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeCom web identity response is invalid");
        }
    }

    /**
     * Sends and maps the WeCom service-provider get_login_info request.
     *
     * @param access  private provider access and callback authorization code
     * @param timeout shared end-to-end budget
     * @return verified service-provider user identity
     */
    private Outcome<ExternalIdentity> thirdPartyIdentity(final PrivateAccess access, final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "WeCom service-provider identity request has no remaining time budget");
        }
        byte[] body = null;
        try {
            body = services.jsonProvider().writeValue(
                    new JsonValue.ObjectValue(Map.of("auth_code", new JsonValue.StringValue(access.code()))));
            final String endpoint = variant.targets().resolve(options).userInfo().getOrNull().url().toString();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.POST)
                    .query(OAuth2.Parameters.ACCESS_TOKEN, access.token())
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.VENDOR_AUTH).addressPolicy())
                    .body(body, MediaType.APPLICATION_JSON_TYPE).execute()) {
                return decodeThirdPartyIdentity(response, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeCom service-provider identity request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Strictly maps service-provider login using {@code user_info.userid} as the Source-local subject.
     *
     * @param response owned get_login_info response
     * @param timeout  shared clock used for evidence timestamping
     * @return verified service-provider user identity
     */
    private Outcome<ExternalIdentity> decodeThirdPartyIdentity(
            final HttpResponse response,
            final Timeout.Budget timeout) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!members(WireKind.WORK_THIRD_PARTY, object)) {
                throw new ValidateException("WeCom service-provider response contains an unknown member");
            }
            final Outcome<ExternalIdentity> error = platformError(
                    response,
                    object,
                    "WeCom service-provider identity endpoint");
            if (error != null) {
                return error;
            }
            final JsonValue.ObjectValue corporation = requiredObject(object, "corp_info");
            final JsonValue.ObjectValue user = requiredObject(object, "user_info");
            if (!members(WireKind.WORK_CORPORATION, corporation) || !members(WireKind.WORK_USER, user)) {
                throw new ValidateException("WeCom service-provider nested identity members are invalid");
            }
            final String subject = requiredString(user, "userid");
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            attributes.put("corpid", new JsonValue.StringValue(requiredString(corporation, "corpid")));
            copyString(user, attributes, "open_userid");
            copyString(user, attributes, "name");
            copyString(user, attributes, "avatar");
            copyNumber(object, attributes, "usertype");
            optionalObject(object, "redirect_login_info");
            optionalObject(object, "auth_info");
            optionalArray(object, "agent");
            return Outcome.succeeded(external(subject, attributes, "user_info.userid", WORK_AUTHORITY, timeout));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeCom service-provider identity response is invalid");
        }
    }

    /**
     * Registers a Mini Program code replay marker before resolving its App Secret.
     *
     * @param code    runtime one-time code
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return completed direct Source initiation
     */
    private CompletionStage<Outcome<SourceAuthentication.Stage>> miniProgram(
            final String code,
            final Context context,
            final Timeout.Budget timeout) {
        final var policy = services.securityBaseline().require(Protocol.VENDOR_AUTH);
        return services.securityBaseline().replayGuard(services.replayCache())
                .register(
                        namespaceId,
                        Protocol.VENDOR_AUTH,
                        WECHAT_AUTHORITY,
                        MINI_CODE_PURPOSE,
                        code,
                        timeout.clock().now().plus(policy.minimumReplayWindow()),
                        timeout)
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Void> ignored -> resolve(context, timeout)
                            .thenCompose(resolved -> switch (resolved) {
                                case Outcome.Succeeded<SecretLease> success -> codeSession(
                                        code,
                                        success.value(),
                                        timeout);
                                case Outcome.Rejected<SecretLease> rejected -> completed(
                                        Outcome.rejected(rejected.failure()));
                                case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                            });
                    case Outcome.Rejected<Void> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<Void> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Sends the exact Mini Program query code-to-session request.
     *
     * @param code    replay-registered one-time code
     * @param secret  owned App Secret lease
     * @param timeout shared end-to-end budget
     * @return completed direct Source initiation
     */
    private CompletionStage<Outcome<SourceAuthentication.Stage>> codeSession(
            final String code,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try (secret) {
                if (timeout.expired()) {
                    return WeChatAdapterSupport.<SourceAuthentication.Stage>failed(
                            ErrorCode._408,
                            "WeChat Mini Program request has no remaining time budget");
                }
                final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
                try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                        .query("appid", options.clientId()).query("secret", secret(secret)).query("js_code", code)
                        .query(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value())
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.VENDOR_AUTH).addressPolicy())
                        .execute()) {
                    final Outcome<ExternalIdentity> identity = decodeMiniProgram(response, timeout);
                    return switch (identity) {
                        case Outcome.Succeeded<ExternalIdentity> success -> Outcome.succeeded(
                                new SourceAuthentication.Stage.Completed(success.value()));
                        case Outcome.Rejected<ExternalIdentity> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<ExternalIdentity> failed -> Outcome.failed(failed.failure());
                    };
                }
            } catch (RuntimeException cause) {
                return WeChatAdapterSupport
                        .<SourceAuthentication.Stage>failed(ErrorCode._502, "WeChat Mini Program request failed");
            }
        }, services.executor());
    }

    /**
     * Strictly validates Mini Program code-to-session while discarding the returned session key.
     *
     * @param response owned code-to-session response
     * @param timeout  shared clock used for evidence timestamping
     * @return verified Mini Program OpenID identity
     */
    private Outcome<ExternalIdentity> decodeMiniProgram(final HttpResponse response, final Timeout.Budget timeout) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!members(WireKind.MINI, object)) {
                throw new ValidateException("WeChat Mini Program response contains an unknown member");
            }
            final Outcome<ExternalIdentity> error = platformError(response, object, "WeChat Mini Program endpoint");
            if (error != null) {
                return error;
            }
            requiredString(object, "session_key");
            final String subject = requiredString(object, "openid");
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            copyString(object, attributes, "unionid");
            return Outcome.succeeded(external(subject, attributes, "openid", WECHAT_AUTHORITY, timeout));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "WeChat Mini Program response is invalid");
        }
    }

    /**
     * Resolves one operation-scoped WeChat client secret with closed exception handling.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return secret resolution outcome stage
     */
    private CompletionStage<Outcome<SecretLease>> resolve(final Context context, final Timeout.Budget timeout) {
        try {
            final CompletionStage<Outcome<SecretLease>> stage = Outcome.mapStage(
                    () -> services.secretLoader().load(options.credential(), context, timeout),
                    loaded -> services.secretParser().parse(options.credential(), loaded));
            if (stage == null) {
                return completed(failed(ErrorCode._502, "WeChat client-secret loader returned no stage"));
            }
            return stage.handle(
                    (outcome, cause) -> cause == null && outcome != null ? outcome
                            : WeChatAdapterSupport
                                    .<SecretLease>failed(ErrorCode._502, "WeChat client-secret resolution failed"));
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "WeChat client-secret resolution failed"));
        }
    }

    /**
     * Decodes an OAuth callback after enforcing exact Source callback ownership.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     */
    private AuthorizationResponseDecoder.Decoded decodeOAuth(final Callback.Inbound callback) {
        final Callback.Inbound inbound = Assert.notNull(callback, "WeChat callback must not be null");
        if (inbound.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(inbound.requestUri())) {
            throw new ValidateException("WeChat callback URI or method is invalid");
        }
        return callbackDecoder.decode(inbound);
    }

    /**
     * Validates and indexes one exact proprietary WeCom callback branch.
     *
     * @param callback raw inbound callback
     * @return immutable typed callback branch
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        final Callback.Inbound inbound = Assert.notNull(callback, "WeCom callback must not be null");
        if (inbound.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(inbound.requestUri())) {
            throw new ValidateException("WeCom callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        String error = null;
        String errorDescription = null;
        for (Callback.Parameter parameter : inbound.parameters()) {
            final String value = parameter.value();
            if (value.isBlank()) {
                throw new ValidateException("WeCom callback parameter values must not be blank");
            }
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code, value);
                case OAuth2.Parameters.STATE -> state = unique(state, value);
                case OAuth2.Parameters.ERROR -> error = unique(error, value);
                case OAuth2.Parameters.ERROR_DESCRIPTION -> errorDescription = unique(errorDescription, value);
                default -> throw new ValidateException("WeCom callback contains an unsupported parameter");
            }
        }
        return new CallbackWire(code, state, error, errorDescription);
    }

    /**
     * Reads one bounded duplicate-rejecting WeChat JSON object.
     *
     * @param response response whose body remains owned by the caller
     * @return immutable provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final HttpResponse response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("WeChat response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_RESPONSE_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("WeChat response root must be a JSON object");
        }
        return object;
    }

    /**
     * Creates one verified external identity and its federated evidence.
     *
     * @param subject    verified Source-local subject
     * @param attributes safe provider-neutral identity attributes
     * @param claim      exact verified platform subject field
     * @param authority  trusted platform authority
     * @param timeout    shared clock source
     * @return immutable verified external identity
     */
    private ExternalIdentity external(
            final String subject,
            final Map<String, JsonValue> attributes,
            final String claim,
            final String authority,
            final Timeout.Budget timeout) {
        return new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes),
                List.of(evidence(claim, subject, authority, timeout)));
    }

    /**
     * Reports whether the selected variant publicly exposes OAuth authorization.
     *
     * @return {@code true} for Open Platform or Official Account
     */
    private boolean oauthVariant() {
        return WeChatManifest.OPEN.equals(options.variant()) || WeChatManifest.MP.equals(options.variant());
    }

    /**
     * Identifies each private WeChat or WeCom JSON document with a distinct member contract.
     */
    private enum WireKind {

        /**
         * Open Platform or Official Account token document.
         */
        WECHAT_TOKEN,

        /**
         * Public WeChat profile document.
         */
        WECHAT_PROFILE,

        /**
         * Mini Program code-to-session document.
         */
        MINI,

        /**
         * WeCom application or provider token document.
         */
        WORK_TOKEN,

        /**
         * WeCom corporate QR identity document.
         */
        EE_IDENTITY,

        /**
         * WeCom visible member profile document.
         */
        EE_MEMBER,

        /**
         * WeCom ticket-authorized sensitive member document.
         */
        EE_SENSITIVE,

        /**
         * WeCom web identity document.
         */
        EE_WEB_IDENTITY,

        /**
         * WeCom service-provider login document.
         */
        WORK_THIRD_PARTY,

        /**
         * Nested WeCom corporation document.
         */
        WORK_CORPORATION,

        /**
         * Nested WeCom user document.
         */
        WORK_USER

    }

    /**
     * Carries one exact proprietary WeCom authorization callback branch.
     *
     * @param code             authorization code for success
     * @param state            mandatory browser correlation value
     * @param error            platform error for failure
     * @param errorDescription optional platform error description
     */
    private record CallbackWire(String code, String state, String error, String errorDescription) {

        /**
         * Validates one complete success or failure branch.
         *
         * @throws IllegalArgumentException if state is blank
         * @throws ValidateException        if members do not form one exact branch
         */
        private CallbackWire {
            Assert.notBlank(state, "WeCom callback state must not be blank");
            final boolean success = code != null && error == null && errorDescription == null;
            final boolean failure = code == null && error != null;
            if (!success && !failure) {
                throw new ValidateException("WeCom callback must contain one exact success or error branch");
            }
        }

        /**
         * Reports whether the callback carries a platform error.
         *
         * @return {@code true} for the failure branch
         */
        private boolean failed() {
            return error != null;
        }

    }

    /**
     * Holds private WeChat or WeCom access material for one synchronous authentication chain.
     *
     * @param token    sensitive access or provider token
     * @param subject  token-bound OpenID for public WeChat flows, otherwise {@code null}
     * @param code     consumed callback code for WeCom identity retrieval, otherwise {@code null}
     * @param scope    optional granted WeChat scope, otherwise {@code null}
     * @param unionId  optional token-bound UnionID, otherwise {@code null}
     * @param snapshot Official Account snapshot-user marker
     * @author Kimi Liu
     */
    private record PrivateAccess(String token, String subject, String code, String scope, String unionId,
            boolean snapshot) {

        /**
         * Validates private access material without copying it into any public result.
         *
         * @throws IllegalArgumentException if the token is blank or a present optional string is blank
         */
        private PrivateAccess {
            Assert.notBlank(token, "WeChat private access token must not be blank");
            present(subject, "WeChat private access subject");
            present(code, "WeChat private access callback code");
            present(scope, "WeChat private access scope");
            present(unionId, "WeChat private access UnionID");
        }

        /**
         * Validates one nullable private string when present.
         *
         * @param value nullable private value
         * @param label validation label
         */
        private static void present(final String value, final String label) {
            if (value != null) {
                Assert.notBlank(value, label + " must not be blank when present");
            }
        }

    }

}
