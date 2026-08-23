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
package org.miaixz.bus.auth.source.vendor.qq;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Identity;
import org.miaixz.bus.auth.Identity.Evidence;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.*;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.AuthorizationResponseDecoder;
import org.miaixz.bus.auth.source.vendor.RedirectManager;
import org.miaixz.bus.auth.source.vendor.StandardAdapter;
import org.miaixz.bus.auth.source.vendor.VendorAdapter;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements QQ Open Platform browser login and QQ Mini Program direct authentication.
 * <p>
 * The open variant publishes only standard OAuth authorization. Its non-standard text token, JSONP OpenID, and query
 * profile documents remain private. The mini-program variant replay-protects its runtime code, discards the returned
 * session key after validation, and emits only a verified OpenID identity.
 * </p>
 *
 * @author Kimi Liu
 */
public class QqSourceAdapter implements VendorAdapter {

    /**
     * Trusted QQ Open Platform authority recorded in federated identity evidence.
     */
    private static final String OPEN_AUTHORITY = "https://graph.qq.com";

    /**
     * Trusted QQ Mini Program authority used for evidence and replay isolation.
     */
    private static final String MINI_AUTHORITY = "https://api.q.qq.com";

    /**
     * Replay-purpose label of one QQ Mini Program login code.
     */
    private static final String MINI_CODE_PURPOSE = "qq-mini-program-code";

    /**
     * Source space used for Mini Program replay digest isolation.
     */
    private final String spaceId;

    /**
     * Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable QQ variant manifest.
     */
    private final VendorManifest.Variant variant;

    /**
     * Validated externally loaded QQ options.
     */
    private final QqOptions options;

    /**
     * Caller-owned replay, secret, JSON, network, clock, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Browser correlation lifecycle present only for the open variant.
     */
    private final RedirectManager redirectManager;

    /**
     * Optional uniform adapter containing the open variant's public OAuth authorization binding.
     */
    private final Optional<StandardAdapter> standardAdapter;

    /**
     * Strict standard authorization callback decoder used by the open variant.
     */
    private final AuthorizationResponseDecoder callbackDecoder;

    /**
     * Shared strict form decoder for QQ's text token parameters.
     */
    private final FormCodec formCodec;

    /**
     * Creates one Source-bound QQ adapter for the selected frozen variant.
     *
     * @param spaceId  Source space isolating state, replay, and credentials
     * @param sourceId Source identifier
     * @param manifest selected QQ manifest
     * @param variant  exact selected variant manifest
     * @param options  decoded externally loaded options
     * @param services caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, variant, protocol, or options routing is inconsistent
     */
    public QqSourceAdapter(final String spaceId, final String sourceId, final QqManifest manifest,
            final VendorManifest.Variant variant, final QqOptions options, final DriverServices services) {
        final QqManifest selected = Assert.notNull(manifest, "QQ manifest must not be null");
        this.spaceId = Assert.notBlank(spaceId, "QQ space id must not be blank");
        this.sourceId = Assert.notBlank(sourceId, "QQ Source id must not be blank");
        this.variant = Assert.notNull(variant, "QQ manifest must not be null");
        this.options = Assert.notNull(options, "QQ options must not be null");
        this.services = Assert.notNull(services, "QQ execution services must not be null");
        if (!QqManifest.ID.equals(selected.vendor()) || !selected.variant(options.variant()).equals(variant)
                || !options.variant().equals(variant.variant()) || !QqManifest.ID.equals(options.vendor())
                || QqManifest.OPEN.equals(options.variant()) && variant.protocol() != Protocol.OAUTH2
                || QqManifest.MINI_PROGRAM.equals(options.variant()) && variant.protocol() != Protocol.HTTPS) {
            throw new ValidateException("QQ adapter profile, variant, protocol, and options must match");
        }
        this.redirectManager = QqManifest.OPEN.equals(options.variant())
                ? RedirectManager.create(spaceId, sourceId, variant, options, services)
                : null;
        this.callbackDecoder = new AuthorizationResponseDecoder();
        this.formCodec = new FormCodec();
        this.standardAdapter = QqManifest.OPEN.equals(options.variant())
                ? Optional.of(
                        new StandardAdapter(variant, options, Optional.of(redirectManager),
                                List.of(
                                        new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION,
                                                (request, context, timeout) -> authorization(request)))))
                : Optional.empty();
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
     * Verifies one decoded member name against the selected QQ private wire document.
     *
     * @param kind private document kind
     * @param name exact decoded member name
     * @return whether the member belongs to that document
     */
    private static boolean member(final WireKind kind, final String name) {
        return switch (kind) {
            case OPEN_ID -> switch (name) {
                case OAuth2.Parameters.CLIENT_ID, "openid", "unionid" -> true;
                default -> false;
            };
            case OPEN_ID_ERROR -> switch (name) {
                case OAuth2.Parameters.ERROR, OAuth2.Parameters.ERROR_DESCRIPTION -> true;
                default -> false;
            };
            case PROFILE -> switch (name) {
                case "ret", "msg", "is_lost", "nickname", "gender", "gender_type", "province", "city", "year", "figureurl", "figureurl_1", "figureurl_2", "figureurl_qq", "figureurl_qq_1", "figureurl_qq_2", "figureurl_type", "is_yellow_vip", "vip", "yellow_vip_level", "level", "is_yellow_year_vip" -> true;
                default -> false;
            };
            case MINI -> switch (name) {
                case "errcode", "errmsg", "session_key", "openid", "unionid" -> true;
                default -> false;
            };
        };
    }

    /**
     * Verifies that every decoded member belongs to one selected QQ private wire document.
     *
     * @param kind  private document kind
     * @param names decoded member names
     * @return whether all names have registered semantics
     */
    private static boolean members(final WireKind kind, final Iterable<String> names) {
        for (String name : names) {
            if (!member(kind, name)) {
                return false;
            }
        }
        return true;
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
            throw new ValidateException("QQ response requires a non-blank string member: " + name);
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
            throw new ValidateException("QQ response member must be a JSON string: " + name);
        }
        return string.value();
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
        } catch (ArithmeticException cause) {
            throw new ValidateException("QQ numeric member must be an exact long: " + name, cause);
        }
        throw new ValidateException("QQ response requires an integral member: " + name);
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
            final Timeout timeout) {
        return new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                new Evidence.Claim(claim, new JsonValue.StringValue(subject), authority, timeout.clock().now()));
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
                new Outcome.Failure(ErrorCode._400, "QQ authorization endpoint returned a standard error",
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
     * @return implementation-neutral empty object
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
     * Returns the exact selected QQ variant capability manifest.
     *
     * @return immutable QQ capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes open browser operations, public authorization, or Mini Program direct authentication.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing private QQ response models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "QQ capability must not be null");
        Assert.notNull(context, "QQ invocation context must not be null");
        Assert.notNull(timeout, "QQ invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("QQ capability is not declared by the selected variant"));
        }
        if (QqManifest.OPEN.equals(options.variant()) && capability.key().equals(SourceWorkflow.INITIATE.key())
                && request instanceof SourceWorkflow.Request.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (QqManifest.OPEN.equals(options.variant()) && capability.key().equals(SourceWorkflow.COMPLETE.key())
                && request instanceof SourceWorkflow.Request.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::openIdentity, context, timeout),
                    capability.responseType());
        }
        if (standardAdapter.isPresent() && standardAdapter.getOrNull().manifest().capabilities().contains(capability)) {
            return standardAdapter.getOrNull().invoke(capability, request, context, timeout);
        }
        if (QqManifest.MINI_PROGRAM.equals(options.variant()) && capability.key().equals(SourceWorkflow.INITIATE.key())
                && request instanceof SourceWorkflow.Request.OneTimeCode oneTimeCode
                && sourceId.equals(oneTimeCode.sourceId())) {
            return narrow(mini(oneTimeCode.code(), context, timeout), capability.responseType());
        }
        return completed(rejected("QQ capability request is invalid"));
    }

    /**
     * Builds the QQ Open Platform redirect around generated one-time state.
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
        Assert.notNull(context, "QQ authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "QQ browser material violates the open variant manifest"));
        }
        try {
            final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                    options.redirectUri(), Optional.of(new Scope(effectiveScopes())), Optional.of(initiation.state()),
                    Optional.empty(), Optional.empty(), emptyObject());
            return standardAdapter.getOrNull().invoke(OAuth2ClientScheme.AUTHORIZATION, request, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<Url> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<Url> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<Url> failed -> Outcome.failed(failed.failure());
                        default -> throw new IllegalStateException("Unsupported Outcome implementation");
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("QQ authorization request is invalid"));
        }
    }

    /**
     * Encodes one validated standard authorization request using QQ's comma-delimited scope.
     *
     * @param request standard authorization request
     * @return asynchronous exact QQ authorization URL outcome
     */
    private CompletionStage<Outcome<Url>> authorization(final AuthorizationRequest request) {
        if (!valid(request)) {
            return completed(rejected("QQ authorization request differs from the configured Source"));
        }
        try {
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final Url location = endpoint.url().newBuilder()
                    .query(OAuth2.Parameters.RESPONSE_TYPE, request.responseType().value())
                    .query(OAuth2.Parameters.CLIENT_ID, request.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, request.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.STATE, request.state().getOrNull())
                    .query(OAuth2.Parameters.SCOPE, String.join(Symbol.COMMA, effectiveScopes())).build();
            return completed(Outcome.succeeded(location));
        } catch (RuntimeException cause) {
            return completed(rejected("QQ authorization request is invalid"));
        }
    }

    /**
     * Validates one public standard authorization request against the QQ registration.
     *
     * @param request standard OAuth authorization request
     * @return whether every public request field matches the selected Source
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
                    .orElseThrow(() -> new ValidateException("QQ authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("QQ authorization error requires state"));
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
    }

    /**
     * Completes QQ's private token, OpenID, and profile chain.
     *
     * @param completion consumed browser correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified QQ Open Platform identity
     */
    private CompletionStage<Outcome<Identity>> openIdentity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = decode(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("QQ authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(oauthError(error));
        }
        if (completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(failed(ErrorCode._500, "QQ callback contains unexpected browser material"));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        return resolve(context, timeout).thenCompose(resolved -> switch (resolved) {
            case Outcome.Succeeded<SecretLease> success -> open(response.code(), success.value(), timeout);
            case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Executes the private open-platform chain while closing the client-secret lease after token use.
     *
     * @param code    consumed authorization code
     * @param secret  owned App Key lease
     * @param timeout shared end-to-end timeout
     * @return verified QQ Open Platform identity
     */
    private CompletionStage<Outcome<Identity>> open(
            final String code,
            final SecretLease secret,
            final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            final Outcome<Access> token;
            try (secret) {
                token = token(code, secret, timeout);
            } catch (RuntimeException cause) {
                return QqSourceAdapter.<Identity>failed(ErrorCode._502, "QQ token operation failed");
            }
            return switch (token) {
                case Outcome.Succeeded<Access> success -> switch (openId(success.value(), timeout)) {
                    case Outcome.Succeeded<OpenIdentity> identity -> profile(
                            success.value(),
                            identity.value(),
                            timeout);
                    case Outcome.Rejected<OpenIdentity> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<OpenIdentity> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                };
                case Outcome.Rejected<Access> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<Access> failed -> Outcome.failed(failed.failure());
                default -> throw new IllegalStateException("Unsupported Outcome implementation");
            };
        }, services.executor());
    }

    /**
     * Sends QQ's query-authenticated empty-form token request.
     *
     * @param code    consumed authorization code
     * @param secret  open App Key lease
     * @param timeout shared end-to-end timeout
     * @return private token material without fabricated token type
     */
    private Outcome<Access> token(final String code, final SecretLease secret, final Timeout timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "QQ token request has no remaining timeout");
        }
        try {
            final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
            try (Response response = FabricX.http(Protocol.OAUTH2, timeout, services.policies()).url(endpoint)
                    .method(Http.Method.POST)
                    .query(OAuth2.Parameters.CODE, Assert.notBlank(code, "QQ authorization code must not be blank"))
                    .query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.CLIENT_SECRET, secret(secret))
                    .query(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .header(Http.Header.ACCEPT, MediaType.TEXT_PLAIN)
                    .body(Normal.EMPTY_BYTE_ARRAY, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "QQ token request failed");
        }
    }

    /**
     * Strictly decodes QQ's text parameter token response.
     *
     * @param response owned token endpoint response
     * @return private access result or safely classified failure
     */
    private Outcome<Access> token(final Response response) {
        try {
            final TextTokenWire wire = TextTokenWire
                    .decode(formCodec.decode(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES)));
            if (wire.failed()) {
                return response.code() >= Http.Status.INTERNAL_SERVER_ERROR
                        ? failed(ErrorCode._502, "QQ token endpoint returned an upstream error")
                        : rejected("QQ token endpoint rejected the authorization code");
            }
            if (response.code() != Http.Status.OK) {
                throw new ValidateException("QQ token response members are invalid");
            }
            return Outcome.succeeded(new Access(wire.accessToken()));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "QQ token endpoint returned an invalid response");
        }
    }

    /**
     * Resolves the QQ OpenID and optional UnionID from its exact JSONP operation.
     *
     * @param access  private access result
     * @param timeout shared end-to-end timeout
     * @return client-bound OpenID result
     */
    private Outcome<OpenIdentity> openId(final Access access, final Timeout timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "QQ OpenID request has no remaining timeout");
        }
        try {
            final String endpoint = variant.targets().resolve(options).introspection().getOrNull().url().toString();
            try (Response response = FabricX.http(Protocol.OAUTH2, timeout, services.policies()).url(endpoint)
                    .method(Http.Method.GET).query(OAuth2.Parameters.ACCESS_TOKEN, access.accessToken())
                    .query("unionid", options.preferUnionId() ? 1 : 0).header(Http.Header.ACCEPT, MediaType.TEXT_PLAIN)
                    .execute()) {
                return openId(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "QQ OpenID request failed");
        }
    }

    /**
     * Strictly unwraps and validates one QQ JSONP OpenID response.
     *
     * @param response owned OpenID endpoint response
     * @return client-bound OpenID and optional UnionID
     */
    private Outcome<OpenIdentity> openId(final Response response) {
        try {
            if (response.code() != Http.Status.OK) {
                return response.code() >= Http.Status.INTERNAL_SERVER_ERROR
                        ? failed(ErrorCode._502, "QQ OpenID endpoint returned an upstream error")
                        : rejected("QQ OpenID endpoint rejected the access token");
            }
            final String document = new String(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES), Charset.UTF_8).trim();
            if (!document.startsWith("callback(") || !document.endsWith(");")) {
                throw new ValidateException("QQ OpenID response must use the callback JSONP wrapper");
            }
            final String json = document.substring("callback(".length(), document.length() - 2).trim();
            final JsonValue value = JsonKit.readValue(json.getBytes(Charset.UTF_8), Normal._32, true);
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("QQ OpenID JSONP payload must be an object");
            }
            if (object.values().size() == 2 && members(WireKind.OPEN_ID_ERROR, object.values().keySet())) {
                requiredString(object, OAuth2.Parameters.ERROR);
                requiredString(object, OAuth2.Parameters.ERROR_DESCRIPTION);
                return rejected("QQ OpenID endpoint returned a platform error");
            }
            if (!members(WireKind.OPEN_ID, object.values().keySet())) {
                throw new ValidateException("QQ OpenID response members are invalid");
            }
            final String clientId = requiredString(object, OAuth2.Parameters.CLIENT_ID);
            if (!options.clientId().equals(clientId)) {
                return rejected("QQ OpenID response client binding is invalid");
            }
            return Outcome
                    .succeeded(new OpenIdentity(requiredString(object, "openid"), optionalString(object, "unionid")));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "QQ OpenID endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves the QQ profile and binds it to the previously resolved OpenID.
     *
     * @param access   private access result
     * @param identity client-bound OpenID result
     * @param timeout  shared end-to-end timeout
     * @return verified QQ Open Platform identity
     */
    private Outcome<Identity> profile(final Access access, final OpenIdentity identity, final Timeout timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "QQ profile request has no remaining timeout");
        }
        try {
            final String endpoint = variant.targets().resolve(options).userInfo().getOrNull().url().toString();
            try (Response response = FabricX.http(Protocol.OAUTH2, timeout, services.policies()).url(endpoint)
                    .method(Http.Method.GET).query(OAuth2.Parameters.ACCESS_TOKEN, access.accessToken())
                    .query("oauth_consumer_key", options.clientId()).query("openid", identity.openId())
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).execute()) {
                return profile(response, identity, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "QQ profile request failed");
        }
    }

    /**
     * Strictly maps one QQ profile using the selected OpenID or UnionID identity key.
     *
     * @param response owned profile endpoint response
     * @param identity verified OpenID result
     * @param timeout  shared clock used for evidence timestamping
     * @return verified external identity
     */
    private Outcome<Identity> profile(final Response response, final OpenIdentity identity, final Timeout timeout) {
        try {
            final JsonValue.ObjectValue object = object(response, Protocol.OAUTH2);
            if (!members(WireKind.PROFILE, object.values().keySet())) {
                throw new ValidateException("QQ profile members are invalid");
            }
            final long result = exactLong(object, "ret");
            if (response.code() != Http.Status.OK || result != 0L) {
                optionalString(object, "msg");
                return response.code() >= Http.Status.INTERNAL_SERVER_ERROR
                        ? failed(ErrorCode._502, "QQ profile endpoint returned an upstream error")
                        : rejected("QQ profile endpoint rejected the access token");
            }
            final String unionId = identity.unionId();
            final String subject = options.preferUnionId() && unionId != null ? unionId : identity.openId();
            final String claim = options.preferUnionId() && unionId != null ? "unionid" : "openid";
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            attributes.put("openid", new JsonValue.StringValue(identity.openId()));
            if (unionId != null) {
                attributes.put("unionid", new JsonValue.StringValue(unionId));
            }
            ProfileWire.decode(object).copyAttributes(attributes);
            final Evidence evidence = evidence(claim, subject, OPEN_AUTHORITY, timeout);
            return Outcome.succeeded(
                    new Identity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "QQ profile endpoint returned an invalid response");
        }
    }

    /**
     * Registers a Mini Program code replay marker before resolving its App Secret.
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
     * Sends the exact QQ Mini Program query code-to-session request.
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
            try (secret) {
                if (timeout.expired()) {
                    return QqSourceAdapter.<SourceWorkflow.Stage>failed(
                            ErrorCode._408,
                            "QQ Mini Program request has no remaining timeout");
                }
                final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
                try (Response response = FabricX.http(Protocol.HTTPS, timeout, services.policies()).url(endpoint)
                        .method(Http.Method.GET).query("appid", options.clientId()).query("secret", secret(secret))
                        .query("js_code", code)
                        .query(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value())
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).execute()) {
                    final Outcome<Identity> identity = mini(response, timeout);
                    return switch (identity) {
                        case Outcome.Succeeded<Identity> success -> Outcome
                                .succeeded(new SourceWorkflow.Stage.Completed(success.value()));
                        case Outcome.Rejected<Identity> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<Identity> failed -> Outcome.failed(failed.failure());
                        default -> throw new IllegalStateException("Unsupported Outcome implementation");
                    };
                }
            } catch (RuntimeException cause) {
                return QqSourceAdapter.<SourceWorkflow.Stage>failed(ErrorCode._502, "QQ Mini Program request failed");
            }
        }, services.executor());
    }

    /**
     * Strictly validates a Mini Program response while discarding its session key.
     *
     * @param response owned code-to-session response
     * @param timeout  shared clock used for evidence timestamping
     * @return verified Mini Program identity
     */
    private Outcome<Identity> mini(final Response response, final Timeout timeout) {
        try {
            final JsonValue.ObjectValue object = object(response, Protocol.HTTPS);
            if (!members(WireKind.MINI, object.values().keySet())) {
                throw new ValidateException("QQ Mini Program response members are invalid");
            }
            final long error = exactLong(object, "errcode");
            if (response.code() != Http.Status.OK || error != 0L) {
                optionalString(object, "errmsg");
                return response.code() >= Http.Status.INTERNAL_SERVER_ERROR
                        ? failed(ErrorCode._502, "QQ Mini Program returned an upstream error")
                        : rejected("QQ Mini Program rejected the one-time code");
            }
            requiredString(object, "session_key");
            final String subject = requiredString(object, "openid");
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            final String unionId = optionalString(object, "unionid");
            if (unionId != null) {
                attributes.put("unionid", new JsonValue.StringValue(unionId));
            }
            final Evidence evidence = evidence("openid", subject, MINI_AUTHORITY, timeout);
            return Outcome.succeeded(
                    new Identity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "QQ Mini Program response is invalid");
        }
    }

    /**
     * Resolves one operation-scoped QQ client secret with closed exception handling.
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
                return completed(failed(ErrorCode._502, "QQ client-secret loader returned no stage"));
            }
            return stage.handle(
                    (outcome, cause) -> cause == null && outcome != null ? outcome
                            : QqSourceAdapter
                                    .<SecretLease>failed(ErrorCode._502, "QQ client-secret resolution failed"));
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "QQ client-secret resolution failed"));
        }
    }

    /**
     * Validates callback ownership before strict standard response decoding.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     */
    private AuthorizationResponseDecoder.Decoded decode(final Callback.Inbound callback) {
        final Callback.Inbound inbound = Assert.notNull(callback, "QQ callback must not be null");
        if (inbound.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(inbound.requestUri())) {
            throw new ValidateException("QQ callback URI or method is invalid");
        }
        return callbackDecoder.decode(inbound);
    }

    /**
     * Strictly reads one bounded QQ JSON object.
     *
     * @param response response whose body remains owned by the caller
     * @param protocol protocol whose security rule limits the response body
     * @return immutable implementation-neutral JSON object
     */
    private JsonValue.ObjectValue object(final Response response, final Protocol protocol) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("QQ response must use application/json");
        }
        final JsonValue value = JsonKit.readValue(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES), Normal._32, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("QQ response root must be a JSON object");
        }
        services.policies().require(protocol);
        return object;
    }

    /**
     * Returns explicit QQ scopes or the immutable manifest default.
     *
     * @return ordered effective open-platform scopes
     */
    private List<String> effectiveScopes() {
        return options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
    }

    /**
     * Identifies each private QQ wire document admitted by the adapter.
     */
    private enum WireKind {

        /**
         * Successful JSONP OpenID document.
         */
        OPEN_ID,

        /**
         * Failed JSONP OpenID document.
         */
        OPEN_ID_ERROR,

        /**
         * Open Platform profile document.
         */
        PROFILE,

        /**
         * Mini Program code-to-session document.
         */
        MINI

    }

    /**
     * Carries the exact private QQ text token success or error branch.
     *
     * @param accessToken  successful sensitive access token
     * @param expiresIn    optional positive token lifetime text
     * @param refreshToken optional sensitive refresh token
     * @param errorCode    failed platform error code
     * @param errorMessage failed platform error message
     */
    private record TextTokenWire(String accessToken, String expiresIn, String refreshToken, String errorCode,
            String errorMessage) {

        /**
         * Decodes unique form parameters directly into a typed private document.
         *
         * @param parameters ordered decoded form parameters
         * @return validated success or error branch
         */
        private static TextTokenWire decode(final List<NameValue> parameters) {
            String accessToken = null;
            String expiresIn = null;
            String refreshToken = null;
            String errorCode = null;
            String errorMessage = null;
            for (NameValue parameter : parameters) {
                final String name = Assert.notBlank(parameter.name(), "QQ token parameter name must not be blank");
                final String value = parameter.value();
                switch (name) {
                    case OAuth2.Parameters.ACCESS_TOKEN -> accessToken = unique(accessToken, value, name);
                    case OAuth2.Parameters.EXPIRES_IN -> expiresIn = unique(expiresIn, value, name);
                    case OAuth2.Parameters.REFRESH_TOKEN -> refreshToken = unique(refreshToken, value, name);
                    case "code" -> errorCode = unique(errorCode, value, name);
                    case "msg" -> errorMessage = unique(errorMessage, value, name);
                    default -> throw new ValidateException("QQ token response contains an unknown parameter");
                }
            }
            final boolean success = accessToken != null && errorCode == null && errorMessage == null;
            final boolean failure = accessToken == null && expiresIn == null && refreshToken == null
                    && errorCode != null && errorMessage != null;
            if (!success && !failure) {
                throw new ValidateException("QQ token response mixes or omits branch parameters");
            }
            if (expiresIn != null) {
                positiveLifetime(expiresIn);
            }
            return new TextTokenWire(accessToken, expiresIn, refreshToken, errorCode, errorMessage);
        }

        /**
         * Rejects duplicate parameters while retaining their non-blank value.
         *
         * @param current   previously decoded value
         * @param candidate current decoded value
         * @param name      exact parameter name
         * @return validated candidate
         */
        private static String unique(final String current, final String candidate, final String name) {
            if (current != null) {
                throw new ValidateException("QQ token response repeats parameter: " + name);
            }
            return Assert.notBlank(candidate, "QQ token response parameter must not be blank: " + name);
        }

        /**
         * Validates one positive exact decimal token lifetime.
         *
         * @param value lifetime parameter text
         */
        private static void positiveLifetime(final String value) {
            try {
                if (Long.parseLong(value) <= 0L) {
                    throw new ValidateException("QQ token lifetime must be positive");
                }
            } catch (NumberFormatException cause) {
                throw new ValidateException("QQ token lifetime must be an exact decimal long", cause);
            }
        }

        /**
         * Indicates whether this document is the platform error branch.
         *
         * @return {@code true} for the error branch
         */
        private boolean failed() {
            return errorCode != null;
        }

    }

    /**
     * Carries the retained private QQ Open Platform profile projection.
     *
     * @param nickname    optional display name
     * @param gender      optional localized gender label
     * @param province    optional province name
     * @param city        optional city name
     * @param year        optional birth year text
     * @param smallAvatar optional small QQ avatar URL
     * @param largeAvatar optional large QQ avatar URL
     */
    private record ProfileWire(String nickname, String gender, String province, String city, String year,
            String smallAvatar, String largeAvatar) {

        /**
         * Decodes one already member-validated QQ profile object.
         *
         * @param object private profile response object
         * @return immutable typed projection
         */
        private static ProfileWire decode(final JsonValue.ObjectValue object) {
            return new ProfileWire(optionalString(object, "nickname"), optionalString(object, "gender"),
                    optionalString(object, "province"), optionalString(object, "city"), optionalString(object, "year"),
                    optionalString(object, "figureurl_qq_1"), optionalString(object, "figureurl_qq_2"));
        }

        /**
         * Copies one optional string attribute into the identity projection.
         *
         * @param attributes mutable destination owned by the identity mapper
         * @param name       exact QQ wire member name
         * @param value      optional decoded value
         */
        private static void put(final Map<String, JsonValue> attributes, final String name, final String value) {
            if (value != null) {
                attributes.put(name, new JsonValue.StringValue(value));
            }
        }

        /**
         * Copies present profile attributes using their exact QQ wire names.
         *
         * @param attributes mutable destination owned by the identity mapper
         */
        private void copyAttributes(final Map<String, JsonValue> attributes) {
            put(attributes, "nickname", nickname);
            put(attributes, "gender", gender);
            put(attributes, "province", province);
            put(attributes, "city", city);
            put(attributes, "year", year);
            put(attributes, "figureurl_qq_1", smallAvatar);
            put(attributes, "figureurl_qq_2", largeAvatar);
        }

    }

    /**
     * Holds private QQ token fields whose response lacks the standard token type.
     *
     * @param accessToken sensitive access token
     * @author Kimi Liu
     */
    private record Access(String accessToken) {

        /**
         * Validates one private QQ access result.
         *
         * @throws IllegalArgumentException if the access token is blank
         */
        private Access {
            Assert.notBlank(accessToken, "QQ access token must not be blank");
        }

    }

    /**
     * Holds the client-bound QQ OpenID and optional UnionID.
     *
     * @param openId  required OpenID
     * @param unionId optional UnionID or {@code null}
     * @author Kimi Liu
     */
    private record OpenIdentity(String openId, String unionId) {

        /**
         * Validates one private QQ identity-key result.
         *
         * @throws IllegalArgumentException if OpenID is blank or a present UnionID is blank
         */
        private OpenIdentity {
            Assert.notBlank(openId, "QQ OpenID must not be blank");
            if (unionId != null) {
                Assert.notBlank(unionId, "QQ UnionID must not be blank when present");
            }
        }

    }

}
