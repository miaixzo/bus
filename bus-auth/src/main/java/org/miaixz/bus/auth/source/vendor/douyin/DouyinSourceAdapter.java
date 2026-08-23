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
package org.miaixz.bus.auth.source.vendor.douyin;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.Identity.Evidence;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.codec.QueryCodec;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.SourceServices;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.protocol.oauth2.ResponseType;
import org.miaixz.bus.auth.source.vendor.RedirectManager;
import org.miaixz.bus.auth.source.vendor.VendorAdapter;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements Douyin open-platform browser authentication and ordinary mini-program direct authentication.
 * <p>
 * Open-platform tokens remain private to the immediate profile request because their envelope is not an OAuth token
 * response. Mini-program code-to-session output is validated only to derive an identity; its session key is never
 * returned, persisted, logged, or represented as an access token.
 * </p>
 *
 * @author Kimi Liu
 */
public class DouyinSourceAdapter implements VendorAdapter {

    /**
     * Trusted open-platform authority recorded in identity evidence.
     */
    private static final String OPEN_AUTHORITY = "https://open.douyin.com";

    /**
     * Trusted ordinary mini-program authority recorded in identity evidence and replay isolation.
     */
    private static final String MINI_AUTHORITY = "https://developer.toutiao.com";

    /**
     * Replay-purpose label of a mini-program one-time login code.
     */
    private static final String MINI_CODE_PURPOSE = "douyin-mini-code";

    /**
     * Source space used only for replay digest isolation.
     */
    private final String spaceId;

    /**
     * Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Douyin variant manifest.
     */
    private final VendorManifest.Variant variant;

    /**
     * Validated externally loaded Douyin options.
     */
    private final DouyinOptions options;

    /**
     * Caller-owned replay, loaders, parsers, JSON, network, clock, and execution dependencies.
     */
    private final SourceServices services;

    /**
     * Browser correlation lifecycle present only for the open variant.
     */
    private final RedirectManager redirectManager;

    /**
     * Strict query encoder for the open authorization redirect.
     */
    private final QueryCodec queryCodec = new QueryCodec();

    /**
     * Strict form encoder for open token and profile operations.
     */
    private final FormCodec formCodec = new FormCodec();

    /**
     * Creates one Source-bound Douyin adapter for the selected frozen variant.
     *
     * @param spaceId  Source space used to isolate state, credentials, and replay digests
     * @param sourceId Source identifier
     * @param manifest selected Douyin manifest
     * @param variant  exact selected variant manifest
     * @param options  decoded externally loaded options
     * @param services capability-limited Source services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, variant, protocol, or options routing is inconsistent
     */
    public DouyinSourceAdapter(final String spaceId, final String sourceId, final DouyinManifest manifest,
            final VendorManifest.Variant variant, final DouyinOptions options, final SourceServices services) {
        final DouyinManifest selectedProfile = Assert.notNull(manifest, "Douyin manifest must not be null");
        this.spaceId = Assert.notBlank(spaceId, "Douyin space id must not be blank");
        this.sourceId = Assert.notBlank(sourceId, "Douyin Source id must not be blank");
        this.variant = Assert.notNull(variant, "Douyin manifest must not be null");
        this.options = Assert.notNull(options, "Douyin options must not be null");
        this.services = Assert.notNull(services, "Douyin execution services must not be null");
        if (!DouyinManifest.ID.equals(selectedProfile.vendor())
                || !selectedProfile.variant(options.variant()).equals(variant)
                || !options.variant().equals(variant.variant()) || !DouyinManifest.ID.equals(options.vendor())
                || DouyinManifest.OPEN.equals(options.variant()) && variant.protocol() != Protocol.OAUTH2
                || DouyinManifest.MINI_PROGRAM.equals(options.variant()) && variant.protocol() != Protocol.HTTPS) {
            throw new ValidateException("Douyin adapter profile, variant, and options must match");
        }
        this.redirectManager = DouyinManifest.OPEN.equals(options.variant())
                ? RedirectManager.create(spaceId, sourceId, variant, options, services)
                : null;
    }

    /**
     * Copies one optional string profile attribute.
     *
     * @param source decoded profile data
     * @param target mutable destination used only during identity construction
     * @param name   exact attribute name
     */
    private static void copyString(
            final JsonValue.ObjectValue source,
            final Map<String, JsonValue> target,
            final String name) {
        final String value = optionalString(source, name);
        if (value != null) {
            target.put(name, new JsonValue.StringValue(value));
        }
    }

    /**
     * Verifies that every member belongs to one private Douyin data shape.
     *
     * @param object decoded private data object
     * @param shape  selected response shape
     * @return whether every member is registered for that shape
     */
    private static boolean members(final JsonValue.ObjectValue object, final Shape shape) {
        for (String member : object.values().keySet()) {
            final boolean known = switch (shape) {
                case TOKEN -> switch (member) {
                    case "error_code", OAuth2.Parameters.ACCESS_TOKEN, "open_id", OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.SCOPE, OAuth2.Parameters.EXPIRES_IN, "refresh_expires_in", "captcha", "desc_url", "description", "log_id" -> true;
                    default -> false;
                };
                case PROFILE -> switch (member) {
                    case "error_code", "client_key", "open_id", "union_id", "nickname", "avatar", "description", "e_account_role" -> true;
                    default -> false;
                };
                case MINI -> switch (member) {
                    case "session_key", "openid", "anonymous_openid", "unionid" -> true;
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
     * Classifies documented mini-program code failures as expected request rejections.
     *
     * @param code numeric platform error code
     * @return whether the error denotes an invalid or expired caller-supplied code
     */
    private static boolean miniRejection(final long code) {
        return code == 40014L || code == 40015L || code == 40017L || code == 40018L;
    }

    /**
     * Reads one required non-blank JSON string.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return non-blank string value
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = optionalString(object, name);
        if (value == null) {
            throw new ValidateException("Douyin response requires a non-blank string field");
        }
        return value;
    }

    /**
     * Reads one required JSON string that may be empty by protocol manifest.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return present string value
     */
    private static String requiredStringAllowEmpty(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Douyin response requires a string field");
        }
        return string.value();
    }

    /**
     * Reads one optional non-blank JSON string.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return string value or {@code null} when absent
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Douyin optional response field must be a non-blank string");
        }
        return string.value();
    }

    /**
     * Reads one exact integral JSON number.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return exact long value
     */
    private static long exactLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Douyin response requires an integral number field");
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException cause) {
            throw new ValidateException("Douyin response number must be an exact long", cause);
        }
    }

    /**
     * Reads one exact positive integral JSON number.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return positive long value
     */
    private static long positiveLong(final JsonValue.ObjectValue object, final String name) {
        final long value = exactLong(object, name);
        if (value <= 0L) {
            throw new ValidateException("Douyin response lifetime must be positive");
        }
        return value;
    }

    /**
     * Creates one verified federated identity evidence claim.
     *
     * @param name      exact subject field name
     * @param subject   verified subject value
     * @param authority trusted platform authority
     * @param timeout   shared clock source
     * @return immutable evidence value
     */
    private static Evidence evidence(
            final String name,
            final String subject,
            final String authority,
            final Timeout timeout) {
        return new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                new Evidence.Claim(name, new JsonValue.StringValue(subject), authority, timeout.clock().now()));
    }

    /**
     * Erases an optional temporary byte array.
     *
     * @param value temporary sensitive bytes, or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) 0);
        }
    }

    /**
     * Propagates a decoded JSON rejection or failure to another expected success type.
     *
     * @param outcome decoded object outcome
     * @param <T>     target success type
     * @return same closed failure category and details
     */
    private static <T> Outcome<T> propagate(final Outcome<JsonValue.ObjectValue> outcome) {
        return switch (outcome) {
            case Outcome.Succeeded<JsonValue.ObjectValue> ignored -> failed(
                    "Douyin response propagation received an unexpected success");
            case Outcome.Rejected<JsonValue.ObjectValue> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<JsonValue.ObjectValue> failed -> Outcome.failed(failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        };
    }

    /**
     * Narrows a delegated outcome through the declared response class.
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
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
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
     * Creates a safe expected rejection without sensitive details.
     *
     * @param description non-sensitive rejection description
     * @param <T>         expected successful value type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, emptyObject()));
    }

    /**
     * Creates a safe platform rejection retaining only its numeric error code.
     *
     * @param code        platform numeric error code
     * @param description non-sensitive rejection description
     * @param <T>         expected successful value type
     * @return rejected outcome with bounded details
     */
    private static <T> Outcome<T> rejected(final long code, final String description) {
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(
                        Map.of("code", new JsonValue.NumberValue(BigDecimal.valueOf(code))))));
    }

    /**
     * Creates a safe operational failure.
     *
     * @param description non-sensitive failure description
     * @param <T>         expected successful value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._502, description, emptyObject()));
    }

    /**
     * Creates an immutable empty safe-details object.
     *
     * @return empty JSON object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Returns the exact selected variant capability manifest.
     *
     * @return immutable Douyin capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes open browser operations or the mini-program direct one-time-code operation.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed Source authentication outcome
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "Douyin capability must not be null");
        Assert.notNull(context, "Douyin invocation context must not be null");
        Assert.notNull(timeout, "Douyin invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Douyin capability is not declared by the selected variant"));
        }
        if (DouyinManifest.OPEN.equals(options.variant()) && capability.key().equals(SourceWorkflow.INITIATE.key())
                && request instanceof SourceWorkflow.Request.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (DouyinManifest.OPEN.equals(options.variant()) && capability.key().equals(SourceWorkflow.COMPLETE.key())
                && request instanceof SourceWorkflow.Request.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::identity, context, timeout),
                    capability.responseType());
        }
        if (DouyinManifest.MINI_PROGRAM.equals(options.variant())
                && capability.key().equals(SourceWorkflow.INITIATE.key())
                && request instanceof SourceWorkflow.Request.OneTimeCode oneTimeCode
                && sourceId.equals(oneTimeCode.sourceId())) {
            return narrow(mini(oneTimeCode.code(), context, timeout), capability.responseType());
        }
        return completed(rejected("Douyin capability request is invalid"));
    }

    /**
     * Builds the exact ordered open-platform authorization redirect.
     *
     * @param initiation generated browser correlation
     * @param context    immutable invocation context retained by the uniform operation signature
     * @param timeout    shared end-to-end timeout
     * @return prepared authorization redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(context, "Douyin authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed("Douyin authorization security material violates the open variant manifest"));
        }
        try {
            final List<NameValue> parameters = List.of(
                    new NameValue("client_key", options.clientId()),
                    new NameValue(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value()),
                    new NameValue(OAuth2.Parameters.SCOPE, String.join(Symbol.COMMA, options.scopes())),
                    new NameValue(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()),
                    new NameValue(OAuth2.Parameters.STATE, initiation.state()));
            final String endpoint = variant.targets().resolve(options).authorization().getOrNull().url().toString();
            return completed(
                    Outcome.succeeded(
                            new RedirectManager.Prepared(
                                    endpoint + Symbol.C_QUESTION_MARK + queryCodec.encode(parameters),
                                    initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("Douyin authorization request is invalid"));
        }
    }

    /**
     * Extracts one open callback state after exact callback validation.
     *
     * @param callback raw inbound callback
     * @return unique non-blank state value
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Resolves the open client secret and starts the private token-to-profile chain.
     *
     * @param completion consumed callback correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified open-platform external identity
     */
    private CompletionStage<Outcome<Identity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final String code;
        try {
            code = callback(completion.callback()).code();
        } catch (RuntimeException cause) {
            return completed(rejected("Douyin authorization callback is invalid"));
        }
        return resolve(context, timeout).thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<SecretLease> success -> token(code, success.value(), timeout);
            case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Sends the exact open-platform authorization-code form and closes the client-secret lease.
     *
     * @param code    consumed authorization code
     * @param secret  owned client-secret lease
     * @param timeout shared end-to-end timeout
     * @return verified identity after token and profile processing
     */
    private CompletionStage<Outcome<Identity>> token(
            final String code,
            final SecretLease secret,
            final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try (secret) {
                if (timeout.expired()) {
                    return DouyinSourceAdapter.<Access>failed("Douyin token request has no remaining timeout");
                }
                body = formCodec.encode(
                        List.of(
                                new NameValue("client_key", options.clientId()),
                                new NameValue(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material())),
                                new NameValue(OAuth2.Parameters.CODE,
                                        Assert.notBlank(code, "Douyin authorization code must not be blank")),
                                new NameValue(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value())));
                final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
                try (Response response = FabricX.http(Protocol.OAUTH2, timeout, services.policies()).url(endpoint)
                        .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                    return decodeToken(response);
                }
            } catch (RuntimeException cause) {
                return DouyinSourceAdapter.<Access>failed("Douyin token request failed");
            } finally {
                clear(body);
            }
        }, services.executor()).thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<Access> success -> profile(success.value(), timeout);
            case Outcome.Rejected<Access> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<Access> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Strictly decodes one open-platform token envelope without fabricating {@code token_type}.
     *
     * @param response owned token HTTP response
     * @return private access result or safely classified failure
     */
    private Outcome<Access> decodeToken(final Response response) {
        final Outcome<JsonValue.ObjectValue> decoded = object(response, "token");
        if (!(decoded instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(decoded);
        }
        final JsonValue.ObjectValue root = success.value();
        try {
            if (root.values().size() != 2 || !root.values().containsKey("message") || !root.values().containsKey("data")
                    || !(root.values().get("data") instanceof JsonValue.ObjectValue data)
                    || !members(data, Shape.TOKEN)) {
                throw new ValidateException("Douyin token envelope is invalid");
            }
            final String message = requiredString(root, "message");
            final long error = exactLong(data, "error_code");
            if (error != 0L || !"success".equals(message)) {
                return rejected(error, "Douyin token endpoint returned an error");
            }
            final String accessToken = requiredString(data, OAuth2.Parameters.ACCESS_TOKEN);
            final String openId = requiredString(data, "open_id");
            requiredString(data, OAuth2.Parameters.REFRESH_TOKEN);
            requiredString(data, OAuth2.Parameters.SCOPE);
            positiveLong(data, OAuth2.Parameters.EXPIRES_IN);
            positiveLong(data, "refresh_expires_in");
            return Outcome.succeeded(new Access(accessToken, openId));
        } catch (RuntimeException cause) {
            return failed("Douyin token response is malformed");
        }
    }

    /**
     * Retrieves the open-platform user profile using only its official form fields.
     *
     * @param access  private token and open identifier
     * @param timeout shared end-to-end timeout
     * @return verified external identity
     */
    private CompletionStage<Outcome<Identity>> profile(final Access access, final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try {
                if (timeout.expired()) {
                    return DouyinSourceAdapter.<Identity>failed("Douyin profile request has no remaining timeout");
                }
                body = formCodec.encode(
                        List.of(
                                new NameValue(OAuth2.Parameters.ACCESS_TOKEN, access.accessToken()),
                                new NameValue("open_id", access.openId())));
                final String endpoint = variant.targets().resolve(options).userInfo().getOrNull().url().toString();
                try (Response response = FabricX.http(Protocol.OAUTH2, timeout, services.policies()).url(endpoint)
                        .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                    return decodeProfile(response, timeout);
                }
            } catch (RuntimeException cause) {
                return DouyinSourceAdapter.<Identity>failed("Douyin profile request failed");
            } finally {
                clear(body);
            }
        }, services.executor());
    }

    /**
     * Maps one strict open-platform profile using only {@code union_id} as subject.
     *
     * @param response owned profile HTTP response
     * @param timeout  shared clock used for evidence
     * @return verified external identity
     */
    private Outcome<Identity> decodeProfile(final Response response, final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> decoded = object(response, "profile");
        if (!(decoded instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(decoded);
        }
        try {
            final JsonValue.ObjectValue root = success.value();
            if (root.values().size() != 4 || !root.values().containsKey("err_no")
                    || !root.values().containsKey("err_msg") || !root.values().containsKey("log_id")
                    || !root.values().containsKey("data")
                    || !(root.values().get("data") instanceof JsonValue.ObjectValue data)
                    || !members(data, Shape.PROFILE)) {
                throw new ValidateException("Douyin profile envelope is invalid");
            }
            final long rootError = exactLong(root, "err_no");
            requiredString(root, "err_msg");
            requiredString(root, "log_id");
            final String dataError = optionalString(data, "error_code");
            if (rootError != 0L || dataError != null && !Symbol.ZERO.equals(dataError)) {
                return rejected(rootError, "Douyin profile endpoint returned an error");
            }
            final String openId = requiredString(data, "open_id");
            final String subject = requiredString(data, "union_id");
            requiredString(data, "nickname");
            requiredString(data, "avatar");
            final String clientKey = optionalString(data, "client_key");
            if (clientKey != null && !options.clientId().equals(clientKey)) {
                throw new ValidateException("Douyin profile client key binding is invalid");
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            copyString(data, attributes, "open_id");
            copyString(data, attributes, "nickname");
            copyString(data, attributes, "avatar");
            copyString(data, attributes, "description");
            final JsonValue role = data.values().get("e_account_role");
            if (role != null && !(role instanceof JsonValue.NullValue)) {
                if (role instanceof JsonValue.ObjectValue || role instanceof JsonValue.ArrayValue) {
                    throw new ValidateException("Douyin account role must be a scalar JSON value");
                }
                attributes.put("e_account_role", role);
            }
            final Evidence evidence = evidence("union_id", subject, OPEN_AUTHORITY, timeout);
            return Outcome.succeeded(
                    new Identity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return rejected("Douyin profile response is invalid");
        }
    }

    /**
     * Registers a mini-program code replay marker before resolving its App Secret.
     *
     * @param code    runtime one-time code
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return completed direct Source initiation
     */
    private CompletionStage<Outcome<SourceWorkflow.Stage>> mini(
            final String code,
            final Context context,
            final Timeout timeout) {
        final var rule = services.policies().require(Protocol.HTTPS);
        return new org.miaixz.bus.auth.guard.ReplayGuard(services.replayCache()).register(
                spaceId,
                Protocol.HTTPS,
                MINI_AUTHORITY,
                MINI_CODE_PURPOSE,
                code,
                timeout.clock().now().plus(rule.minimumReplayWindow()),
                timeout).thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Void> ignored -> resolve(context, timeout)
                            .thenCompose(resolved -> switch (resolved) {
                                case Outcome.Succeeded<SecretLease> success -> codeSession(
                                        code,
                                        success.value(),
                                        timeout);
                                case Outcome.Rejected<SecretLease> rejected -> completed(
                                        Outcome.rejected(rejected.failure()));
                                case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                                default -> throw new IllegalStateException("Unsupported Outcome implementation");
                            });
                    case Outcome.Rejected<Void> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<Void> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Sends the exact mini-program code-to-session JSON while the App Secret lease is alive.
     *
     * @param code    replay-registered one-time code
     * @param secret  owned App Secret lease
     * @param timeout shared end-to-end timeout
     * @return completed direct Source initiation
     */
    private CompletionStage<Outcome<SourceWorkflow.Stage>> codeSession(
            final String code,
            final SecretLease secret,
            final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try (secret) {
                if (timeout.expired()) {
                    return DouyinSourceAdapter.<SourceWorkflow.Stage>failed(
                            "Douyin mini-program request has no remaining timeout");
                }
                final Map<String, JsonValue> fields = new LinkedHashMap<>();
                fields.put("appid", new JsonValue.StringValue(options.clientId()));
                fields.put("secret", new JsonValue.StringValue(new String(secret.material())));
                fields.put("anonymous_code", new JsonValue.StringValue(Normal.EMPTY));
                fields.put("code", new JsonValue.StringValue(code));
                body = JsonKit.writeValue(new JsonValue.ObjectValue(fields));
                final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
                try (Response response = FabricX.http(Protocol.HTTPS, timeout, services.policies()).url(endpoint)
                        .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .body(body, MediaType.APPLICATION_JSON_TYPE).execute()) {
                    final Outcome<Identity> identity = decodeMini(response, timeout);
                    return switch (identity) {
                        case Outcome.Succeeded<Identity> success -> Outcome
                                .succeeded(new SourceWorkflow.Stage.Completed(success.value()));
                        case Outcome.Rejected<Identity> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<Identity> failed -> Outcome.failed(failed.failure());
                        default -> throw new IllegalStateException("Unsupported Outcome implementation");
                    };
                }
            } catch (RuntimeException cause) {
                return DouyinSourceAdapter.<SourceWorkflow.Stage>failed("Douyin mini-program request failed");
            } finally {
                clear(body);
            }
        }, services.executor());
    }

    /**
     * Maps one strict mini-program response while discarding its verified session key.
     *
     * @param response owned code-to-session HTTP response
     * @param timeout  shared clock used for evidence
     * @return verified mini-program identity
     */
    private Outcome<Identity> decodeMini(final Response response, final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> decoded = object(response, "mini-program");
        if (!(decoded instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(decoded);
        }
        try {
            final JsonValue.ObjectValue root = success.value();
            if (root.values().size() != 4 || !root.values().containsKey("err_no")
                    || !root.values().containsKey("err_tips") || !root.values().containsKey("log_id")
                    || !root.values().containsKey("data")
                    || !(root.values().get("data") instanceof JsonValue.ObjectValue data)
                    || !members(data, Shape.MINI)) {
                throw new ValidateException("Douyin mini-program envelope is invalid");
            }
            final long error = exactLong(root, "err_no");
            requiredString(root, "err_tips");
            requiredString(root, "log_id");
            if (error != 0L) {
                return miniRejection(error) ? rejected(error, "Douyin mini-program rejected the one-time code")
                        : failed("Douyin mini-program returned an operational error");
            }
            requiredString(data, "session_key");
            final String subject = requiredString(data, "openid");
            final String anonymous = requiredStringAllowEmpty(data, "anonymous_openid");
            if (!anonymous.isEmpty()) {
                throw new ValidateException("Douyin anonymous login is not enabled by this variant manifest");
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            final String unionId = optionalString(data, "unionid");
            if (unionId != null) {
                attributes.put("unionid", new JsonValue.StringValue(unionId));
            }
            final Evidence evidence = evidence("openid", subject, MINI_AUTHORITY, timeout);
            return Outcome.succeeded(
                    new Identity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed("Douyin mini-program response is malformed");
        }
    }

    /**
     * Resolves one short-lived client secret lease with closed exception handling.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return secret resolution outcome stage
     */
    private CompletionStage<Outcome<SecretLease>> resolve(final Context context, final Timeout timeout) {
        try {
            final CompletionStage<Outcome<SecretLease>> stage = Outcome.mapStage(
                    () -> services.secretLoader()
                            .load(new SecretLoader.Request(services.entry(), options.credential()), context, timeout),
                    loaded -> services.secretParser().parse(services.entry(), options.credential(), loaded));
            if (stage == null) {
                return completed(failed("Douyin client-secret loader returned no stage"));
            }
            return stage.handle(
                    (outcome, cause) -> cause == null && outcome != null ? outcome
                            : DouyinSourceAdapter.<SecretLease>failed("Douyin client-secret resolution failed"));
        } catch (RuntimeException cause) {
            return completed(failed("Douyin client-secret resolution failed"));
        }
    }

    /**
     * Validates the exact open-platform GET callback.
     *
     * @param callback raw inbound callback
     * @return typed private callback value
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Douyin callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Douyin callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert.notBlank(parameter.value(), "Douyin callback value must not be blank");
            if (OAuth2.Parameters.CODE.equals(parameter.name())) {
                if (code != null) {
                    throw new ValidateException("Douyin callback parameter names must be unique");
                }
                code = value;
            } else if (OAuth2.Parameters.STATE.equals(parameter.name())) {
                if (state != null) {
                    throw new ValidateException("Douyin callback parameter names must be unique");
                }
                state = value;
            } else {
                throw new ValidateException("Douyin callback contains an unregistered parameter");
            }
        }
        if (code == null || state == null) {
            throw new ValidateException("Douyin callback must contain exactly code and state");
        }
        return new CallbackWire(code, state);
    }

    /**
     * Strictly decodes one bounded HTTP 200 JSON object and classifies transport status.
     *
     * @param response  owned HTTP response
     * @param operation non-sensitive operation label
     * @return decoded object or closed rejection/failure
     */
    private Outcome<JsonValue.ObjectValue> object(final Response response, final String operation) {
        if (response.code() == Http.Status.TOO_MANY_REQUESTS || response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed("Douyin " + operation + " endpoint is unavailable");
        }
        if (response.code() >= Http.Status.BAD_REQUEST) {
            return rejected("Douyin " + operation + " endpoint rejected the request");
        }
        try {
            if (response.code() != Http.Status.OK
                    || !MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
                throw new ValidateException("Douyin response must use HTTP 200 application/json");
            }
            final JsonValue value = JsonKit.readValue(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES), Normal._64, true);
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("Douyin JSON root must be an object");
            }
            return Outcome.succeeded(object);
        } catch (RuntimeException cause) {
            return failed("Douyin " + operation + " endpoint returned malformed JSON");
        }
    }

    /**
     * Identifies one private Douyin response data shape.
     *
     * @author Kimi Liu
     */
    private enum Shape {

        /**
         * Open-platform token data.
         */
        TOKEN,

        /**
         * Open-platform profile data.
         */
        PROFILE,

        /**
         * Ordinary mini-program code-to-session data.
         */
        MINI

    }

    /**
     * Carries the exact private Douyin open-platform callback.
     *
     * @param code  authorization code
     * @param state mandatory browser correlation value
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state) {

        /**
         * Validates one decoded callback value.
         *
         * @throws IllegalArgumentException if either value is blank
         */
        private CallbackWire {
            Assert.notBlank(code, "Douyin callback code must not be blank");
            Assert.notBlank(state, "Douyin callback state must not be blank");
        }

    }

    /**
     * Carries open-platform token values only until the immediately following profile request completes.
     *
     * @param accessToken sensitive access token
     * @param openId      application-scoped open identifier required by the profile endpoint
     * @author Kimi Liu
     */
    private record Access(String accessToken, String openId) {

        /**
         * Validates one private open-platform access result.
         *
         * @throws IllegalArgumentException if a value is blank
         */
        private Access {
            Assert.notBlank(accessToken, "Douyin private access token must not be blank");
            Assert.notBlank(openId, "Douyin private open identifier must not be blank");
        }

        /**
         * Returns a diagnostic representation without token or identifier data.
         *
         * @return redacted private token summary
         */
        @Override
        public String toString() {
            return "Access[accessToken=[REDACTED], openId=[REDACTED]]";
        }

    }

}
