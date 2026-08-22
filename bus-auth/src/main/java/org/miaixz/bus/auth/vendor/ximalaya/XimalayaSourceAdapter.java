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
package org.miaixz.bus.auth.vendor.ximalaya;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationResponseDecoder;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements Ximalaya browser authentication and signed profile retrieval.
 * <p>
 * The adapter exposes only the standard OAuth authorization operation and the framework's uniform Source sign-in
 * contract. Ximalaya token material never becomes a public TokenResponse because the platform response omits
 * {@code token_type}; signed profile access remains an internal platform resource operation.
 * </p>
 *
 * @author Kimi Liu
 */
public class XimalayaSourceAdapter implements VendorAdapter {

    /**
     * Registered Source identifier used in the resulting external identity.
     */
    private final String sourceId;

    /**
     * Exact immutable Ximalaya manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Ximalaya options.
     */
    private final XimalayaOptions options;

    /**
     * Caller-owned secret, JSON, network, clock, crypto, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Shared one-time browser-state lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Standard OAuth authorization request encoder bound to the resolved Ximalaya authorization endpoint.
     */
    private final AuthorizationRequestEncoder authorizationEncoder;

    /**
     * Trusted authority derived from the resolved Ximalaya profile endpoint.
     */
    private final String authority;

    /**
     * Strict standard OAuth authorization callback decoder.
     */
    private final AuthorizationResponseDecoder callbackDecoder;

    /**
     * Shared strict UTF-8 form encoder.
     */
    private final FormCodec formCodec;

    /**
     * Uniform adapter that publishes the public OAuth authorization capability.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Creates one Source-bound Ximalaya adapter from the frozen default manifest.
     *
     * @param spaceId  registration space isolating browser state and credentials
     * @param sourceId registered Source identifier
     * @param manifest selected Ximalaya manifest
     * @param variant  exact selected default manifest
     * @param options  decoded externally loaded Ximalaya options
     * @param services caller-owned runtime dependencies
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, manifest, options, or routing differ from the frozen variant
     */
    public XimalayaSourceAdapter(final String spaceId, final String sourceId, final XimalayaManifest manifest,
            final VariantManifest.Variant variant, final XimalayaOptions options, final DriverServices services) {
        final XimalayaManifest selected = Assert.notNull(manifest, "Ximalaya manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Ximalaya Source id must not be blank");
        this.variant = Assert.notNull(variant, "Ximalaya manifest must not be null");
        this.options = Assert.notNull(options, "Ximalaya options must not be null");
        this.services = Assert.notNull(services, "Ximalaya execution services must not be null");
        if (!XimalayaManifest.ID.equals(selected.vendor())
                || !selected.variant(XimalayaManifest.DEFAULT).equals(variant)
                || !XimalayaManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !XimalayaManifest.ID.equals(options.vendor())
                || !XimalayaManifest.DEFAULT.equals(options.variant())) {
            throw new ValidateException("Ximalaya adapter requires the ximalaya/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(spaceId, sourceId, variant, options, services);
        final VendorTargets.Resolved resolvedTargets = variant.targets().resolve(options);
        this.authorizationEncoder = new AuthorizationRequestEncoder(resolvedTargets.authorization().getOrNull());
        this.authority = Protocol.HTTPS_PREFIX + resolvedTargets.userInfo().getOrNull().url().host();
        this.callbackDecoder = new AuthorizationResponseDecoder();
        this.formCodec = new FormCodec();
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION,
                                (request, context, timeout) -> authorization(request))));
    }

    /**
     * Generates the historical Ximalaya HMAC-SHA1 then MD5 lowercase signature.
     *
     * @param parameters sorted request parameters excluding {@code sig}
     * @param secret     client-secret characters owned by the active lease
     * @return lowercase hexadecimal signature
     */
    private static String signature(final Map<String, String> parameters, final char[] secret) {
        final List<byte[]> parts = new ArrayList<>(parameters.size() * 2);
        int length = Math.max(0, parameters.size() - 1);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            final byte[] name = entry.getKey().getBytes(Charset.UTF_8);
            final byte[] value = entry.getValue().getBytes(Charset.UTF_8);
            parts.add(name);
            parts.add(value);
            length += name.length + 1 + value.length;
        }
        final byte[] canonical = new byte[length];
        int offset = 0;
        for (int index = 0; index < parts.size(); index += 2) {
            if (index != 0) {
                canonical[offset++] = Symbol.C_AND;
            }
            final byte[] name = parts.get(index);
            final byte[] value = parts.get(index + 1);
            System.arraycopy(name, 0, canonical, offset, name.length);
            offset += name.length;
            canonical[offset++] = Symbol.C_EQUAL;
            System.arraycopy(value, 0, canonical, offset, value.length);
            offset += value.length;
        }
        final byte[] encoded = Base64.encode(canonical, false);
        final byte[] key = ByteKit.toBytes(secret);
        try {
            final byte[] hmac = Builder.hmacSha1(key).digest(encoded);
            try {
                return Builder.md5().digestHex(hmac);
            } finally {
                Arrays.fill(hmac, (byte) 0);
            }
        } finally {
            parts.forEach(part -> Arrays.fill(part, (byte) 0));
            Arrays.fill(canonical, (byte) 0);
            Arrays.fill(encoded, (byte) 0);
            Arrays.fill(key, (byte) 0);
        }
    }

    /**
     * Validates a closed historical or current platform error object.
     *
     * @param object decoded response object
     * @return whether the response is a valid error branch
     */
    private static boolean error(final JsonValue.ObjectValue object) {
        final boolean marked = object.values().containsKey("errcode") || object.values().containsKey("error_no")
                || object.values().containsKey("error_code");
        if (!marked) {
            return false;
        }
        if (!members(WireKind.ERROR, object)) {
            throw new ValidateException("Ximalaya error response mixes branches or contains an unknown member");
        }
        optionalScalar(object, "errcode");
        optionalScalar(object, "error_no");
        optionalScalar(object, "error_code");
        requiredString(object, "error_desc");
        optionalString(object, "service");
        return true;
    }

    /**
     * Verifies every member of one selected private Ximalaya document by semantic document kind.
     *
     * @param kind   selected private document kind
     * @param object decoded Ximalaya object
     * @return whether every member is registered for the selected document
     */
    private static boolean members(final WireKind kind, final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            final boolean known = switch (kind) {
                case TOKEN -> switch (name) {
                    case OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.EXPIRES_IN, "uid", "device_id", OAuth2.Parameters.SCOPE -> true;
                    default -> false;
                };
                case ERROR -> switch (name) {
                    case "errcode", "error_no", "error_code", "error_desc", "service" -> true;
                    default -> false;
                };
                case PROFILE -> switch (name) {
                    case "id", "nickname", "avatar_url" -> true;
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
     * Reads one required non-blank JSON string.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return required non-blank string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = optionalString(object, name);
        if (value == null) {
            throw new ValidateException("Ximalaya response requires a non-blank string member: " + name);
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
            throw new ValidateException("Ximalaya response member must be a non-blank string: " + name);
        }
        return string.value();
    }

    /**
     * Validates one optional non-blank string or exact integral error marker.
     *
     * @param object decoded response object
     * @param name   exact error member name
     */
    private static void optionalScalar(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return;
        }
        if (value instanceof JsonValue.StringValue string && !string.value().isBlank()) {
            return;
        }
        if (value instanceof JsonValue.NumberValue number) {
            try {
                number.value().toBigIntegerExact();
                return;
            } catch (ArithmeticException cause) {
                throw new ValidateException("Ximalaya error marker must be integral", cause);
            }
        }
        throw new ValidateException("Ximalaya error marker must be a string or integer: " + name);
    }

    /**
     * Reads a positive decimal identity from JSON string or number form.
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
                throw new ValidateException("Ximalaya identity must be an exact integer", cause);
            }
        } else {
            throw new ValidateException("Ximalaya response requires a decimal identity member: " + name);
        }
        if (!lexical.matches("[1-9][0-9]*")) {
            throw new ValidateException("Ximalaya identity must be positive unpadded decimal text");
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
        final JsonValue value = object.values().get(name);
        try {
            final long exact;
            if (value instanceof JsonValue.NumberValue number) {
                exact = number.value().longValueExact();
            } else if (value instanceof JsonValue.StringValue string && string.value().matches("[1-9][0-9]*")) {
                exact = Long.parseLong(string.value());
            } else {
                throw new ValidateException("Ximalaya response requires an integral member: " + name);
            }
            if (exact <= 0L) {
                throw new ValidateException("Ximalaya numeric member must be positive: " + name);
            }
            return exact;
        } catch (ArithmeticException | NumberFormatException cause) {
            throw new ValidateException("Ximalaya numeric member must be an exact long: " + name, cause);
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
                new Outcome.Failure(ErrorCode._400, "Ximalaya authorization endpoint returned a standard error",
                        new JsonValue.ObjectValue(Map.of(
                                org.miaixz.bus.auth.Builder.OAUTH_ERROR,
                                new JsonValue.StringValue(error.response().error().value())))));
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
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
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
     * Materializes one operation-scoped application secret and clears the intermediate character buffer.
     *
     * @param lease open secret lease owned by the current operation
     * @return transient string required by the private form encoder
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
     * Marks one callback member as seen exactly once.
     *
     * @param current whether the member was already seen
     * @return {@code true} for the first occurrence
     * @throws ValidateException if the member is repeated
     */
    private static boolean unique(final boolean current) {
        if (current) {
            throw new ValidateException("Ximalaya callback parameters must not be repeated");
        }
        return true;
    }

    /**
     * Returns the exact frozen Ximalaya capability manifest.
     *
     * @return immutable Source authentication and authorization capability set
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication or the standard OAuth authorization operation.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing private Ximalaya models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "Ximalaya capability must not be null");
        Assert.notNull(context, "Ximalaya invocation context must not be null");
        Assert.notNull(timeout, "Ximalaya invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Ximalaya capability is not declared"));
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
        return completed(rejected("Ximalaya capability request is invalid"));
    }

    /**
     * Builds the Ximalaya redirect around generated one-time state.
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
        Assert.notNull(context, "Ximalaya authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "Ximalaya browser material violates the frozen manifest"));
        }
        try {
            final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                    options.redirectUri(), Optional.empty(), Optional.of(initiation.state()), Optional.empty(),
                    Optional.empty(), emptyObject());
            return completed(
                    Outcome.succeeded(new RedirectManager.Prepared(authorize(request).toString(), initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("Ximalaya authorization request is invalid"));
        }
    }

    /**
     * Encodes one validated authorization request with Ximalaya device parameters.
     *
     * @param request standard OAuth authorization request
     * @return exact Ximalaya authorization URL
     */
    private Url authorize(final AuthorizationRequest request) {
        final Map<String, JsonValue> extensions = new LinkedHashMap<>(request.extensions().values());
        extensions.put(XimalayaManifest.Parameters.CLIENT_OS_TYPE, new JsonValue.StringValue(options.clientOsType()));
        extensions.put(XimalayaManifest.Parameters.DEVICE_ID, new JsonValue.StringValue(options.deviceId()));
        return authorizationEncoder.encode(
                new AuthorizationRequest(request.responseType(), request.clientId(), request.redirectUri(),
                        request.scope(), request.state(), request.codeChallenge(), request.codeChallengeMethod(),
                        new JsonValue.ObjectValue(extensions)));
    }

    /**
     * Applies the public OAuth authorization contract before adding registered device extensions.
     *
     * @param request standard OAuth authorization request
     * @return completed authorization URL outcome
     */
    private CompletionStage<Outcome<Url>> authorization(final AuthorizationRequest request) {
        try {
            return valid(request) ? completed(Outcome.succeeded(authorize(request)))
                    : completed(rejected("Ximalaya authorization request does not match the compiled Source"));
        } catch (RuntimeException cause) {
            return completed(rejected("Ximalaya authorization request is invalid"));
        }
    }

    /**
     * Validates one public authorization request against the registered Source.
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
     * Extracts required state from one strict callback branch.
     *
     * @param callback raw inbound callback
     * @return unique correlation state
     */
    private String state(final Callback.Inbound callback) {
        return switch (decode(callback)) {
            case AuthorizationResponseDecoder.Success success -> success.response().state()
                    .orElseThrow(() -> new ValidateException("Ximalaya authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Ximalaya authorization error requires state"));
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
    }

    /**
     * Completes the correlated private token and profile chain.
     *
     * @param completion consumed browser correlation
     * @param context    immutable invocation context used for secret resolution
     * @param timeout    shared end-to-end timeout
     * @return verified Ximalaya identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = decode(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Ximalaya authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(oauthError(error));
        }
        if (completion.codeVerifier().isPresent() || completion.correlation().nonce().isPresent()) {
            return completed(failed(ErrorCode._500, "Ximalaya callback contains unexpected browser material"));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        final JsonValue device = response.extensions().values().get("device_id");
        if (response.scope().isPresent() || response.issuer().isPresent() || response.extensions().values().size() != 1
                || !(device instanceof JsonValue.StringValue actualDevice)
                || !options.deviceId().equals(actualDevice.value())) {
            return completed(rejected("Ximalaya callback device or success parameters do not match registration"));
        }
        return resolve(context, timeout).thenCompose(resolved -> switch (resolved) {
            case Outcome.Succeeded<SecretLease> success -> authenticate(response.code(), success.value(), timeout);
            case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Executes private token and signed profile operations while owning the client-secret lease.
     *
     * @param code    consumed authorization code
     * @param secret  owned client-secret lease
     * @param timeout shared end-to-end timeout
     * @return verified Ximalaya identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> authenticate(
            final String code,
            final SecretLease secret,
            final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try (secret) {
                return switch (token(code, secret, timeout)) {
                    case Outcome.Succeeded<Access> success -> profile(success.value(), secret, timeout);
                    case Outcome.Rejected<Access> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Access> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                };
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Ximalaya authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Sends Ximalaya's client-secret form token request.
     *
     * @param code    consumed authorization code
     * @param secret  live client-secret lease
     * @param timeout shared end-to-end timeout
     * @return private access result without a fabricated token type
     */
    private Outcome<Access> token(final String code, final SecretLease secret, final Timeout timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Ximalaya token request has no remaining timeout");
            }
            final List<NameValue> parameters = List.of(
                    new NameValue(OAuth2.Parameters.CODE,
                            Assert.notBlank(code, "Ximalaya authorization code must not be blank")),
                    new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                    new NameValue(OAuth2.Parameters.CLIENT_SECRET, secret(secret)),
                    new NameValue("device_id", options.deviceId()),
                    new NameValue(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()),
                    new NameValue(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()));
            body = formCodec.encode(parameters);
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            try (Response response = FabricX.http(services.fabric(), Protocol.OAUTH2, timeout)
                    .url(endpoint.url().toString()).method(Http.Method.POST)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Ximalaya token request failed");
        } finally {
            if (body != null) {
                Arrays.fill(body, (byte) 0);
            }
        }
    }

    /**
     * Strictly decodes one private Ximalaya token response.
     *
     * @param response owned token endpoint response
     * @return private access result or safely classified failure
     */
    private Outcome<Access> token(final Response response) {
        if (response.code() != Http.Status.OK) {
            return status(response.code(), "Ximalaya token endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue object = object(response, "token");
            if (error(object)) {
                return rejected("Ximalaya token endpoint returned a platform error");
            }
            if (!members(WireKind.TOKEN, object)) {
                throw new ValidateException("Ximalaya token response contains an unknown member");
            }
            final String device = optionalString(object, "device_id");
            if (device != null && !options.deviceId().equals(device)) {
                return rejected("Ximalaya token device does not match registration");
            }
            optionalString(object, OAuth2.Parameters.SCOPE);
            requiredString(object, OAuth2.Parameters.REFRESH_TOKEN);
            positiveLong(object, OAuth2.Parameters.EXPIRES_IN);
            return Outcome.succeeded(
                    new Access(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN),
                            requiredIdentifier(object, "uid")));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Ximalaya token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves and verifies the signed Ximalaya profile.
     *
     * @param access  private access token and token-bound user identifier
     * @param secret  live client-secret lease used only for the registered signature
     * @param timeout shared end-to-end timeout
     * @return verified identity
     */
    private Outcome<ExternalIdentity> profile(final Access access, final SecretLease secret, final Timeout timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "Ximalaya profile request has no remaining timeout");
        }
        final Map<String, String> query = new TreeMap<>();
        query.put(OAuth2.Parameters.ACCESS_TOKEN, access.accessToken());
        query.put("app_key", options.clientId());
        query.put("client_os_type", options.clientOsType());
        query.put("device_id", options.deviceId());
        query.put("pack_id", options.packageId());
        final String signature;
        final char[] material = secret.material();
        try {
            signature = signature(query, material);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._500, "Ximalaya profile signature generation failed");
        } finally {
            Arrays.fill(material, Symbol.C_NUL);
        }
        try {
            final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
            final var request = FabricX.http(services.fabric(), Protocol.OAUTH2, timeout).url(endpoint.url().toString())
                    .method(Http.Method.GET);
            query.forEach(request::query);
            try (Response response = request.query(org.miaixz.bus.auth.Builder.SIGNATURE, signature)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).execute()) {
                return profile(response, access, timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Ximalaya profile request failed");
        }
    }

    /**
     * Strictly maps one Ximalaya profile and binds it to the token user identifier.
     *
     * @param response owned profile response
     * @param access   private token-bound identifier
     * @param timeout  shared clock used for evidence timestamping
     * @return verified Ximalaya identity
     */
    private Outcome<ExternalIdentity> profile(final Response response, final Access access, final Timeout timeout) {
        if (response.code() != Http.Status.OK) {
            return status(response.code(), "Ximalaya profile endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue object = object(response, "profile");
            if (error(object)) {
                return rejected("Ximalaya profile endpoint returned a platform error");
            }
            if (!members(WireKind.PROFILE, object)) {
                throw new ValidateException("Ximalaya profile response contains an unknown member");
            }
            final String subject = requiredIdentifier(object, "id");
            if (!access.userId().equals(subject)) {
                return rejected("Ximalaya profile id does not match token uid");
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            ProfileWire.decode(object).copyAttributes(attributes);
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("id", new JsonValue.StringValue(subject), authority, timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Ximalaya profile endpoint returned an invalid response");
        }
    }

    /**
     * Resolves one operation-scoped Ximalaya application secret.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return secret resolution outcome stage
     */
    private CompletionStage<Outcome<SecretLease>> resolve(final Context context, final Timeout timeout) {
        try {
            final CompletionStage<Outcome<SecretLease>> stage = Outcome.mapStage(
                    () -> services.secretLoader().load(
                            new SecretLoader.Request(services.registration(), options.credential()),
                            context,
                            timeout),
                    loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded));
            if (stage == null) {
                return completed(failed(ErrorCode._502, "Ximalaya application-secret loader returned no stage"));
            }
            return stage.handle(
                    (outcome, cause) -> cause == null && outcome != null ? outcome
                            : XimalayaSourceAdapter.<SecretLease>failed(
                                    ErrorCode._502,
                                    "Ximalaya application-secret resolution failed"));
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "Ximalaya application-secret resolution failed"));
        }
    }

    /**
     * Validates callback ownership and branch vocabulary before standard decoding.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     */
    private AuthorizationResponseDecoder.Decoded decode(final Callback.Inbound callback) {
        final Callback.Inbound inbound = Assert.notNull(callback, "Ximalaya callback must not be null");
        if (inbound.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(inbound.requestUri())) {
            throw new ValidateException("Ximalaya callback URI or method is invalid");
        }
        boolean code = false;
        boolean state = false;
        boolean device = false;
        boolean error = false;
        int count = 0;
        for (Callback.Parameter parameter : inbound.parameters()) {
            count++;
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code);
                case OAuth2.Parameters.STATE -> state = unique(state);
                case XimalayaManifest.Parameters.DEVICE_ID -> device = unique(device);
                case OAuth2.Parameters.ERROR -> error = unique(error);
                case OAuth2.Parameters.ERROR_DESCRIPTION, OAuth2.Parameters.ERROR_URI -> {
                    // Standard optional OAuth error members are validated by the shared decoder.
                }
                default -> throw new ValidateException("Ximalaya callback contains an unsupported parameter");
            }
        }
        final boolean success = code && state && device && !error && count == 3;
        final boolean failure = !code && !device && error && state && count >= 2 && count <= 4;
        if (!success && !failure) {
            throw new ValidateException("Ximalaya callback error members are invalid");
        }
        return callbackDecoder.decode(inbound);
    }

    /**
     * Strictly reads one bounded duplicate-rejecting Ximalaya JSON object.
     *
     * @param response  response whose body remains owned by the caller
     * @param operation safe operation name used in validation failures
     * @return immutable provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final Response response, final String operation) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Ximalaya " + operation + " response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(org.miaixz.bus.auth.Builder.MAXIMUM_DOCUMENT_BYTES), Normal._32, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Ximalaya " + operation + " response root must be an object");
        }
        return object;
    }

    /**
     * Identifies each private Ximalaya JSON document with a distinct member contract.
     */
    private enum WireKind {

        /**
         * Private token success document.
         */
        TOKEN,

        /**
         * Historical or current platform error document.
         */
        ERROR,

        /**
         * Signed profile success document.
         */
        PROFILE

    }

    /**
     * Carries the retained non-sensitive Ximalaya profile projection.
     *
     * @param nickname  optional display name
     * @param avatarUrl optional avatar URL
     */
    private record ProfileWire(String nickname, String avatarUrl) {

        /**
         * Decodes one member-validated Ximalaya profile object.
         *
         * @param object private profile response object
         * @return immutable typed profile projection
         */
        private static ProfileWire decode(final JsonValue.ObjectValue object) {
            return new ProfileWire(optionalString(object, "nickname"), optionalString(object, "avatar_url"));
        }

        /**
         * Copies one optional string into the identity projection.
         *
         * @param attributes mutable destination owned by the identity mapper
         * @param name       exact Ximalaya wire member name
         * @param value      optional decoded value
         */
        private static void put(final Map<String, JsonValue> attributes, final String name, final String value) {
            if (value != null) {
                attributes.put(name, new JsonValue.StringValue(value));
            }
        }

        /**
         * Copies present profile attributes using their exact Ximalaya wire names.
         *
         * @param attributes mutable destination owned by the identity mapper
         */
        private void copyAttributes(final Map<String, JsonValue> attributes) {
            put(attributes, "nickname", nickname);
            put(attributes, "avatar_url", avatarUrl);
        }

    }

    /**
     * Holds private Ximalaya token material and its token-bound user identifier.
     *
     * @param accessToken sensitive access token
     * @param userId      token-bound Ximalaya user identifier
     * @author Kimi Liu
     */
    private record Access(String accessToken, String userId) {

        /**
         * Validates private Ximalaya access material.
         *
         * @throws IllegalArgumentException if a component is blank
         */
        private Access {
            Assert.notBlank(accessToken, "Ximalaya access token must not be blank");
            Assert.notBlank(userId, "Ximalaya token user identifier must not be blank");
        }

    }

}
