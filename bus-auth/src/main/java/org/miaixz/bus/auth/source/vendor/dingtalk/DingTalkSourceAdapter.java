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
package org.miaixz.bus.auth.source.vendor.dingtalk;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.Identity.Evidence;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.codec.QueryCodec;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.SourceServices;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.*;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.source.vendor.RedirectManager;
import org.miaixz.bus.auth.source.vendor.VendorAdapter;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements both frozen DingTalk browser authentication variants behind one Source adapter contract.
 * <p>
 * The delegated variant exposes only its standards-compatible OAuth authorization operation and privately processes
 * DingTalk's JSON token and access-token header. The account variant signs a one-time temporary code with a short-lived
 * shared-secret lease and never constructs an OAuth token model.
 * </p>
 *
 * @author Kimi Liu
 */
public class DingTalkSourceAdapter implements VendorAdapter {

    /**
     * Trusted authority of delegated DingTalk identity responses.
     */
    private static final String DELEGATED_AUTHORITY = "https://api.dingtalk.com";

    /**
     * Trusted authority of signed account-login responses.
     */
    private static final String ACCOUNT_AUTHORITY = "https://oapi.dingtalk.com";

    /**
     * Source identifier copied into verified external identities.
     */
    private final String sourceId;

    /**
     * Selected immutable DingTalk variant manifest.
     */
    private final VendorManifest.Variant variant;

    /**
     * Validated externally loaded DingTalk options.
     */
    private final DingTalkOptions options;

    /**
     * Caller-owned loaders, parsers, JSON, network, crypto-policy, clock, and execution dependencies.
     */
    private final SourceServices services;

    /**
     * Shared one-time state lifecycle for both browser variants.
     */
    private final RedirectManager redirectManager;

    /**
     * Strict RFC 3986 encoder used for platform-specific ordered query sequences.
     */
    private final QueryCodec queryCodec = new QueryCodec();

    /**
     * Standard OAuth authorization encoder present only for the delegated variant.
     */
    private final AuthorizationRequestEncoder authorizationEncoder;

    /**
     * Creates one Source-bound DingTalk adapter for the selected frozen variant.
     *
     * @param spaceId  Source space used to isolate state and credentials
     * @param sourceId Source identifier
     * @param manifest selected DingTalk manifest
     * @param variant  exact selected variant manifest
     * @param options  decoded externally loaded options
     * @param services capability-limited Source services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, variant, protocol, or options routing is inconsistent
     */
    public DingTalkSourceAdapter(final String spaceId, final String sourceId, final DingTalkManifest manifest,
            final VendorManifest.Variant variant, final DingTalkOptions options, final SourceServices services) {
        final DingTalkManifest selectedProfile = Assert.notNull(manifest, "DingTalk manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "DingTalk Source id must not be blank");
        this.variant = Assert.notNull(variant, "DingTalk manifest must not be null");
        this.options = Assert.notNull(options, "DingTalk options must not be null");
        this.services = Assert.notNull(services, "DingTalk execution services must not be null");
        if (!DingTalkManifest.ID.equals(selectedProfile.vendor())
                || !selectedProfile.variant(options.variant()).equals(variant)
                || !options.variant().equals(variant.variant()) || !DingTalkManifest.ID.equals(options.vendor())
                || DingTalkManifest.OAUTH2.equals(options.variant()) && variant.protocol() != Protocol.OAUTH2
                || DingTalkManifest.ACCOUNT.equals(options.variant()) && variant.protocol() != Protocol.HTTPS) {
            throw new ValidateException("DingTalk adapter profile, variant, and options must match");
        }
        this.redirectManager = RedirectManager.create(spaceId, sourceId, variant, options, services);
        this.authorizationEncoder = DingTalkManifest.OAUTH2.equals(options.variant())
                ? new AuthorizationRequestEncoder(variant.targets().resolve(options).authorization().getOrNull())
                : null;
    }

    /**
     * Detects the exact delegated platform error branch.
     *
     * @param object decoded response object
     * @return whether all response members form the official error vocabulary and contain code
     */
    private static boolean isError(final JsonValue.ObjectValue object) {
        if (!object.values().containsKey("code")) {
            return false;
        }
        for (String member : object.values().keySet()) {
            if (!switch (member) {
                case "code", "message", "requestid" -> true;
                default -> false;
            }) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts a delegated official error object to a safe rejection retaining only its numeric code.
     *
     * @param object      decoded official error object
     * @param description non-sensitive operation description
     * @param <T>         expected successful value type
     * @return safely bounded rejection or malformed-response failure
     */
    private static <T> Outcome<T> platformError(final JsonValue.ObjectValue object, final String description) {
        try {
            final long code = exactLong(object, "code");
            optionalString(object, "message");
            optionalString(object, "requestid");
            return rejected(code, description);
        } catch (RuntimeException cause) {
            return failed("DingTalk platform error response is invalid");
        }
    }

    /**
     * Copies optional non-blank string attributes from a strict response.
     *
     * @param source decoded response object
     * @param target mutable destination used only during identity construction
     */
    private static void copyStrings(final JsonValue.ObjectValue source, final Map<String, JsonValue> target) {
        copyString(source, target, "openId");
        copyString(source, target, "nick");
        copyString(source, target, "avatarUrl");
        copyString(source, target, "mobile");
        copyString(source, target, "stateCode");
        copyString(source, target, "email");
    }

    /**
     * Copies one present private string member to identity attributes.
     *
     * @param source decoded private response object
     * @param target mutable identity attribute map
     * @param name   exact DingTalk member name
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
     * Tests one response member against a private DingTalk response shape.
     *
     * @param member response member name
     * @param shape  selected private response shape
     * @return whether the selected decoder recognizes the member
     */
    private static boolean member(final String member, final Shape shape) {
        return switch (shape) {
            case TOKEN -> switch (member) {
                case "accessToken", "refreshToken", "expireIn", "corpId" -> true;
                default -> false;
            };
            case USER -> switch (member) {
                case "unionId", "openId", "nick", "avatarUrl", "mobile", "stateCode", "email" -> true;
                default -> false;
            };
            case ACCOUNT -> switch (member) {
                case "errcode", "errmsg", "user_info" -> true;
                default -> false;
            };
            case ACCOUNT_USER -> switch (member) {
                case "unionid", "openid", "nick", "main_org_auth_high_level" -> true;
                default -> false;
            };
        };
    }

    /**
     * Verifies that every member belongs to one private DingTalk response shape.
     *
     * @param object decoded response object
     * @param shape  selected private response shape
     * @return whether every member is recognized
     */
    private static boolean members(final JsonValue.ObjectValue object, final Shape shape) {
        return object.values().keySet().stream().allMatch(member -> member(member, shape));
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
            throw new ValidateException("DingTalk response requires a non-blank string field");
        }
        return value;
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
            throw new ValidateException("DingTalk optional response field must be a non-blank string");
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
            throw new ValidateException("DingTalk response requires an integral number field");
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException cause) {
            throw new ValidateException("DingTalk response number must be an exact long", cause);
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
            throw new ValidateException("DingTalk response lifetime must be positive");
        }
        return value;
    }

    /**
     * Creates one verified federated evidence claim.
     *
     * @param name      exact verified subject field name
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
     * Propagates a decoded response rejection or failure to another expected success type.
     *
     * @param outcome decoded response outcome
     * @param <T>     target success type
     * @return same closed failure category and details
     */
    private static <T> Outcome<T> propagate(final Outcome<JsonValue.ObjectValue> outcome) {
        return switch (outcome) {
            case Outcome.Succeeded<JsonValue.ObjectValue> ignored -> failed(
                    "DingTalk response propagation received an unexpected success");
            case Outcome.Rejected<JsonValue.ObjectValue> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<JsonValue.ObjectValue> failed -> Outcome.failed(failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        };
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
     * @return immutable DingTalk capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and the delegated variant's single standard authorization operation.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed operation outcome
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "DingTalk capability must not be null");
        Assert.notNull(context, "DingTalk invocation context must not be null");
        Assert.notNull(timeout, "DingTalk invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("DingTalk capability is not declared by the selected variant"));
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
        if (DingTalkManifest.OAUTH2.equals(options.variant()) && capability.equals(OAuth2ClientScheme.AUTHORIZATION)
                && request instanceof AuthorizationRequest authorization) {
            return authorize(authorization, capability.responseType(), timeout);
        }
        return completed(rejected("DingTalk capability request is invalid"));
    }

    /**
     * Validates, enriches, and standard-encodes one public delegated OAuth authorization request.
     *
     * @param request      caller-supplied standard authorization request
     * @param responseType declared capability response type
     * @param timeout      shared end-to-end timeout
     * @param <S>          successful response type
     * @return encoded absolute authorization URL outcome
     */
    private <S> CompletionStage<Outcome<S>> authorize(
            final AuthorizationRequest request,
            final Class<S> responseType,
            final Timeout timeout) {
        if (timeout.expired()) {
            return completed(failed("DingTalk authorization request has no remaining timeout"));
        }
        try {
            final AuthorizationRequest enriched = authorization(request, request.state());
            return completed(Outcome.succeeded(responseType.cast(authorizationEncoder.encode(enriched))));
        } catch (RuntimeException cause) {
            return completed(rejected("DingTalk authorization request is invalid"));
        }
    }

    /**
     * Builds the exact ordered redirect for the selected browser variant.
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
        Assert.notNull(context, "DingTalk authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed("DingTalk authorization security material violates the selected manifest"));
        }
        try {
            final String endpoint = variant.targets().resolve(options).authorization().getOrNull().url().toString();
            final List<NameValue> parameters = DingTalkManifest.OAUTH2.equals(options.variant())
                    ? delegatedParameters(initiation.state())
                    : accountParameters(initiation.state());
            final String redirect = endpoint + Symbol.C_QUESTION_MARK + queryCodec.encode(parameters);
            return completed(Outcome.succeeded(new RedirectManager.Prepared(redirect, initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("DingTalk authorization request is invalid"));
        }
    }

    /**
     * Creates a standard delegated request with exact official DingTalk extensions.
     *
     * @param source validated caller request
     * @param state  state container retained or generated by the calling operation
     * @return enriched standard request
     */
    private AuthorizationRequest authorization(final AuthorizationRequest source, final Optional<String> state) {
        if (source.responseType() != ResponseType.CODE || !options.clientId().equals(source.clientId())
                || !options.redirectUri().equals(source.redirectUri()) || source.scope().isEmpty()
                || !requestedScopes().equals(source.scope().getOrNull().values()) || source.codeChallenge().isPresent()
                || source.codeChallengeMethod().isPresent() || !source.extensions().values().isEmpty()) {
            throw new ValidateException("DingTalk delegated authorization binding is invalid");
        }
        return new AuthorizationRequest(ResponseType.CODE, options.clientId(), options.redirectUri(),
                Optional.of(new Scope(requestedScopes())), state, Optional.empty(), Optional.empty(), extensions());
    }

    /**
     * Returns delegated authorization parameters in DingTalk's frozen order.
     *
     * @param state generated one-time state
     * @return ordered exact query parameters
     */
    private List<NameValue> delegatedParameters(final String state) {
        final List<NameValue> parameters = new ArrayList<>();
        parameters.add(new NameValue(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value()));
        parameters.add(new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()));
        parameters.add(new NameValue(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()));
        parameters.add(new NameValue(OAuth2.Parameters.SCOPE, String.join(Symbol.SPACE, requestedScopes())));
        parameters.add(new NameValue("prompt", "consent"));
        parameters.add(new NameValue(OAuth2.Parameters.STATE, state));
        options.orgType().ifPresent(value -> parameters.add(new NameValue("org_type", value)));
        options.corpId().ifPresent(value -> parameters.add(new NameValue("corpId", value)));
        if (options.exclusiveLogin()) {
            parameters.add(new NameValue("exclusiveLogin", Normal.TRUE));
            parameters.add(new NameValue("exclusiveCorpId", options.exclusiveCorpId().getOrNull()));
        }
        return List.copyOf(parameters);
    }

    /**
     * Returns account-login authorization parameters in DingTalk's frozen order.
     *
     * @param state generated one-time state
     * @return ordered exact query parameters
     */
    private List<NameValue> accountParameters(final String state) {
        return List.of(
                new NameValue("appid", options.clientId()),
                new NameValue(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value()),
                new NameValue(OAuth2.Parameters.SCOPE, "snsapi_login"),
                new NameValue(OAuth2.Parameters.STATE, state),
                new NameValue(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()));
    }

    /**
     * Creates exact scalar extension members used by the public standard authorization encoder.
     *
     * @return immutable official DingTalk extension object
     */
    private JsonValue.ObjectValue extensions() {
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        values.put("prompt", new JsonValue.StringValue("consent"));
        options.orgType().ifPresent(value -> values.put("org_type", new JsonValue.StringValue(value)));
        options.corpId().ifPresent(value -> values.put("corpId", new JsonValue.StringValue(value)));
        if (options.exclusiveLogin()) {
            values.put("exclusiveLogin", new JsonValue.BooleanValue(true));
            values.put("exclusiveCorpId", new JsonValue.StringValue(options.exclusiveCorpId().getOrNull()));
        }
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Extracts the callback state after exact variant-specific validation.
     *
     * @param callback raw inbound callback
     * @return unique non-blank state
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Resolves the variant credential and completes its private identity chain.
     *
     * @param completion consumed callback correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified DingTalk external identity
     */
    private CompletionStage<Outcome<Identity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("DingTalk authorization callback is invalid"));
        }
        if (values.error() != null) {
            return completed(rejected("DingTalk authorization endpoint returned an error"));
        }
        final CompletionStage<Outcome<SecretLease>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.secretLoader()
                            .load(new SecretLoader.Request(services.entry(), options.credential()), context, timeout),
                    loaded -> services.secretParser().parse(services.entry(), options.credential(), loaded));
        } catch (RuntimeException cause) {
            return completed(failed("DingTalk credential resolution failed"));
        }
        if (resolution == null) {
            return completed(failed("DingTalk credential loader returned no stage"));
        }
        return resolution
                .handle(
                        (outcome, cause) -> cause == null && outcome != null ? outcome
                                : DingTalkSourceAdapter.<SecretLease>failed("DingTalk credential resolution failed"))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SecretLease> success -> DingTalkManifest.OAUTH2.equals(options.variant())
                            ? token(values.code(), success.value(), timeout)
                            : account(values.code(), success.value(), timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Sends DingTalk's exact delegated authorization-code JSON and closes the client-secret lease.
     *
     * @param code    consumed delegated authorization code
     * @param secret  owned client-secret lease
     * @param timeout shared end-to-end timeout
     * @return verified identity after token and current-user processing
     */
    private CompletionStage<Outcome<Identity>> token(
            final String code,
            final SecretLease secret,
            final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try (secret) {
                if (timeout.expired()) {
                    return DingTalkSourceAdapter.<Access>failed("DingTalk token request has no remaining timeout");
                }
                final Map<String, JsonValue> fields = new LinkedHashMap<>();
                fields.put("clientId", new JsonValue.StringValue(options.clientId()));
                fields.put("clientSecret", new JsonValue.StringValue(new String(secret.material())));
                fields.put(
                        OAuth2.Parameters.CODE,
                        new JsonValue.StringValue(Assert.notBlank(code, "DingTalk authCode must not be blank")));
                fields.put("grantType", new JsonValue.StringValue(GrantType.AUTHORIZATION_CODE.value()));
                body = JsonKit.writeValue(new JsonValue.ObjectValue(fields));
                final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
                try (Response response = FabricX.http(Protocol.OAUTH2, timeout, services.policies()).url(endpoint)
                        .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .body(body, MediaType.APPLICATION_JSON_TYPE).execute()) {
                    return decodeToken(response);
                }
            } catch (RuntimeException cause) {
                return DingTalkSourceAdapter.<Access>failed("DingTalk token request failed");
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
     * Strictly decodes delegated token success or official platform error members.
     *
     * @param response owned token HTTP response
     * @return private token value or safely classified failure
     */
    private Outcome<Access> decodeToken(final Response response) {
        final Outcome<JsonValue.ObjectValue> decoded = response(response, "token");
        if (!(decoded instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(decoded);
        }
        final JsonValue.ObjectValue object = success.value();
        if (isError(object)) {
            return platformError(object, "DingTalk token endpoint returned an error");
        }
        if (!members(object, Shape.TOKEN)) {
            return failed("DingTalk token response contains an unknown member");
        }
        try {
            final String accessToken = requiredString(object, "accessToken");
            requiredString(object, "refreshToken");
            final long expireIn = positiveLong(object, "expireIn");
            final String corpId = optionalString(object, "corpId");
            return Outcome.succeeded(new Access(accessToken, expireIn, Optional.ofNullable(corpId)));
        } catch (RuntimeException cause) {
            return rejected("DingTalk token success response is invalid");
        }
    }

    /**
     * Retrieves and validates the current delegated DingTalk user.
     *
     * @param access  private delegated access token
     * @param timeout shared end-to-end timeout
     * @return verified external identity
     */
    private CompletionStage<Outcome<Identity>> profile(final Access access, final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (timeout.expired()) {
                    return DingTalkSourceAdapter.<Identity>failed("DingTalk profile request has no remaining timeout");
                }
                final String endpoint = variant.targets().resolve(options).userInfo().getOrNull().url().toString();
                try (Response response = FabricX.http(Protocol.OAUTH2, timeout, services.policies()).url(endpoint)
                        .method(Http.Method.GET).header("x-acs-dingtalk-access-token", access.accessToken())
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).execute()) {
                    return decodeProfile(response, timeout);
                }
            } catch (RuntimeException cause) {
                return DingTalkSourceAdapter.<Identity>failed("DingTalk profile request failed");
            }
        }, services.executor());
    }

    /**
     * Maps the delegated current-user response using only {@code unionId} as subject.
     *
     * @param response owned current-user HTTP response
     * @param timeout  shared clock used for evidence
     * @return verified external identity
     */
    private Outcome<Identity> decodeProfile(final Response response, final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> decoded = response(response, "profile");
        if (!(decoded instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(decoded);
        }
        final JsonValue.ObjectValue object = success.value();
        if (isError(object)) {
            return platformError(object, "DingTalk profile endpoint returned an error");
        }
        try {
            if (!members(object, Shape.USER)) {
                throw new ValidateException("DingTalk user response contains an unknown member");
            }
            final String subject = requiredString(object, "unionId");
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            copyStrings(object, attributes);
            final Evidence evidence = evidence("unionId", subject, DELEGATED_AUTHORITY, timeout);
            return Outcome.succeeded(
                    new Identity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return rejected("DingTalk profile response is invalid");
        }
    }

    /**
     * Signs and sends the proprietary account temporary-code request while the shared-secret lease is alive.
     *
     * @param code    consumed temporary authorization code
     * @param secret  owned shared-secret lease
     * @param timeout shared end-to-end timeout
     * @return verified account identity
     */
    private CompletionStage<Outcome<Identity>> account(
            final String code,
            final SecretLease secret,
            final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] key = null;
            byte[] message = null;
            byte[] signatureBytes = null;
            byte[] body = null;
            try (secret) {
                if (timeout.expired()) {
                    return DingTalkSourceAdapter.<Identity>failed("DingTalk account request has no remaining timeout");
                }
                final String timestamp = Long.toString(timeout.clock().now().toEpochMilli());
                key = new String(secret.material()).getBytes(Charset.UTF_8);
                message = timestamp.getBytes(Charset.UTF_8);
                signatureBytes = Builder.hmacSha256(key).digest(message);
                final String signature = Base64.encode(signatureBytes);
                body = JsonKit.writeValue(
                        new JsonValue.ObjectValue(Map.of(
                                "tmp_auth_code",
                                new JsonValue.StringValue(Assert
                                        .notBlank(code, "DingTalk temporary authorization code must not be blank")))));
                final String endpoint = variant.targets().resolve(options).userInfo().getOrNull().url().toString();
                try (Response response = FabricX.http(Protocol.HTTPS, timeout, services.policies()).url(endpoint)
                        .method(Http.Method.POST).query("accessKey", options.clientId()).query("timestamp", timestamp)
                        .query("signature", signature).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .body(body, MediaType.APPLICATION_JSON_TYPE).execute()) {
                    return decodeAccount(response, timeout);
                }
            } catch (RuntimeException cause) {
                return DingTalkSourceAdapter.<Identity>failed("DingTalk account request failed");
            } finally {
                clear(key);
                clear(message);
                clear(signatureBytes);
                clear(body);
            }
        }, services.executor());
    }

    /**
     * Maps the proprietary account response using only {@code unionid} as subject.
     *
     * @param response owned account HTTP response
     * @param timeout  shared clock used for evidence
     * @return verified external identity or safely classified failure
     */
    private Outcome<Identity> decodeAccount(final Response response, final Timeout timeout) {
        final Outcome<JsonValue.ObjectValue> decoded = response(response, "account");
        if (!(decoded instanceof Outcome.Succeeded<JsonValue.ObjectValue> success)) {
            return propagate(decoded);
        }
        final JsonValue.ObjectValue object = success.value();
        try {
            if (!members(object, Shape.ACCOUNT)) {
                throw new ValidateException("DingTalk account response contains an unknown member");
            }
            final long error = exactLong(object, "errcode");
            if (error != 0L) {
                return rejected(error, "DingTalk account endpoint returned an error");
            }
            if (!(object.values().get("user_info") instanceof JsonValue.ObjectValue user)
                    || !members(user, Shape.ACCOUNT_USER)) {
                throw new ValidateException("DingTalk account user response is invalid");
            }
            final String subject = requiredString(user, "unionid");
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            final String openId = optionalString(user, "openid");
            final String nick = optionalString(user, "nick");
            if (openId != null) {
                attributes.put("openid", new JsonValue.StringValue(openId));
            }
            if (nick != null) {
                attributes.put("nick", new JsonValue.StringValue(nick));
            }
            final JsonValue highLevel = user.values().get("main_org_auth_high_level");
            if (highLevel != null && !(highLevel instanceof JsonValue.NullValue)) {
                if (!(highLevel instanceof JsonValue.BooleanValue)) {
                    throw new ValidateException("DingTalk account organization flag must be boolean");
                }
                attributes.put("main_org_auth_high_level", highLevel);
            }
            final Evidence evidence = evidence("unionid", subject, ACCOUNT_AUTHORITY, timeout);
            return Outcome.succeeded(
                    new Identity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return rejected("DingTalk account response is invalid");
        }
    }

    /**
     * Validates and indexes the exact callback branch for the selected variant.
     *
     * @param callback raw inbound callback
     * @return typed private callback value
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "DingTalk callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("DingTalk callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        String error = null;
        final String successName = DingTalkManifest.OAUTH2.equals(options.variant()) ? "authCode" : "code";
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert.notBlank(parameter.value(), "DingTalk callback value must not be blank");
            if (successName.equals(parameter.name())) {
                if (code != null) {
                    throw new ValidateException("DingTalk callback parameter names must be unique");
                }
                code = value;
            } else if (OAuth2.Parameters.STATE.equals(parameter.name())) {
                if (state != null) {
                    throw new ValidateException("DingTalk callback parameter names must be unique");
                }
                state = value;
            } else if (OAuth2.Parameters.ERROR.equals(parameter.name())) {
                if (error != null) {
                    throw new ValidateException("DingTalk callback parameter names must be unique");
                }
                error = value;
            } else {
                throw new ValidateException("DingTalk callback contains an unregistered parameter");
            }
        }
        if (state == null || !((code != null) ^ (error != null))) {
            throw new ValidateException("DingTalk callback must contain one exact success or error branch");
        }
        return new CallbackWire(code, state, error);
    }

    /**
     * Decodes one bounded DingTalk HTTP response and classifies transport status.
     *
     * @param response  owned HTTP response
     * @param operation non-sensitive operation label
     * @return decoded object or closed rejection/failure
     */
    private Outcome<JsonValue.ObjectValue> response(final Response response, final String operation) {
        if (response.code() == Http.Status.TOO_MANY_REQUESTS || response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed("DingTalk " + operation + " endpoint is unavailable");
        }
        if (response.code() >= Http.Status.BAD_REQUEST) {
            return rejected("DingTalk " + operation + " endpoint rejected the request");
        }
        try {
            if (response.code() != Http.Status.OK
                    || !MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
                throw new ValidateException("DingTalk response must use HTTP 200 application/json");
            }
            final JsonValue value = JsonKit
                    .readValue(response.bytes(org.miaixz.bus.auth.Builder.MAXIMUM_DOCUMENT_BYTES), Normal._64, true);
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("DingTalk JSON root must be an object");
            }
            return Outcome.succeeded(object);
        } catch (RuntimeException cause) {
            return failed("DingTalk " + operation + " endpoint returned malformed JSON");
        }
    }

    /**
     * Returns explicit scopes or the immutable selected variant default.
     *
     * @return ordered effective DingTalk scopes
     */
    private List<String> requestedScopes() {
        return options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
    }

    /**
     * Identifies one private DingTalk response shape for local member validation.
     *
     * @author Kimi Liu
     */
    private enum Shape {

        /**
         * Delegated access-token response.
         */
        TOKEN,

        /**
         * Delegated current-user response.
         */
        USER,

        /**
         * Proprietary account response envelope.
         */
        ACCOUNT,

        /**
         * Proprietary account user object.
         */
        ACCOUNT_USER

    }

    /**
     * Carries one exact DingTalk callback branch without publishing platform-specific field names.
     *
     * @param code  authorization code on success
     * @param state mandatory browser correlation value
     * @param error platform error on rejection
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state, String error) {

        /**
         * Validates one exact callback branch.
         *
         * @throws IllegalArgumentException if state is blank
         * @throws ValidateException        if success and error are both present or absent
         */
        private CallbackWire {
            Assert.notBlank(state, "DingTalk callback state must not be blank");
            if (!((code != null) ^ (error != null))) {
                throw new ValidateException("DingTalk callback branch is invalid");
            }
        }

    }

    /**
     * Carries delegated token data only until the immediately following current-user request completes.
     *
     * @param accessToken sensitive delegated access token
     * @param expireIn    positive token lifetime in seconds
     * @param corpId      optional selected organization identifier
     * @author Kimi Liu
     */
    private record Access(String accessToken, long expireIn, Optional<String> corpId) {

        /**
         * Validates and freezes one private delegated token result.
         *
         * @throws IllegalArgumentException if the token or optional container is invalid
         * @throws ValidateException        if the lifetime is not positive
         */
        private Access {
            Assert.notBlank(accessToken, "DingTalk private access token must not be blank");
            if (expireIn <= 0L) {
                throw new ValidateException("DingTalk private access-token lifetime must be positive");
            }
            Assert.notNull(corpId, "DingTalk private organization id container must not be null");
            corpId = Optional.ofNullable(corpId.getOrNull());
        }

        /**
         * Returns a diagnostic representation without token or organization data.
         *
         * @return redacted private token summary
         */
        @Override
        public String toString() {
            return "Access[accessToken=[REDACTED], expireIn=" + expireIn + ", corpId=[REDACTED]]";
        }

    }

}
