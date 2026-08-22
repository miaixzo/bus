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
package org.miaixz.bus.auth.vendor.rednote;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Capability;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.FabricX;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.rednote.RedNoteManifest.MarketingAuthorizationRequest;
import org.miaixz.bus.auth.vendor.rednote.RedNoteManifest.MarketingTokenRequest;
import org.miaixz.bus.auth.vendor.rednote.RedNoteManifest.MarketingTokenResponse;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
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

/**
 * Implements the Vendor-defined Xiaohongshu marketing authorization-only API.
 * <p>
 * The adapter exposes exactly two Vendor capabilities, resolves application secrets only for one token operation, maps
 * no response to OAuth protocol models, and never creates an external identity or Source-authentication result.
 * </p>
 *
 * @author Kimi Liu
 */
public class RedNoteSourceAdapter implements VendorAdapter {

    /**
     * Maximum bounded JSON response size accepted from the marketing API.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Maximum JSON nesting accepted from the marketing API.
     */
    private static final int MAXIMUM_JSON_DEPTH = Normal._16;

    /**
     * Selected immutable RedNote marketing manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded marketing registration options.
     */
    private final RedNoteOptions options;

    /**
     * Caller-owned secret, JSON, network, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Shared strict application-form encoder.
     */
    private final FormCodec formCodec;

    /**
     * Creates one authorization-only RedNote adapter.
     *
     * @param namespaceId registration namespace retained by the uniform adapter construction contract
     * @param sourceId    registered Source identifier retained for Registry routing only
     * @param manifest    selected RedNote manifest
     * @param variant     exact marketing manifest
     * @param options     decoded externally loaded marketing options
     * @param services    caller-owned runtime dependencies
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, manifest, or options differ from marketing
     */
    public RedNoteSourceAdapter(final String namespaceId, final String sourceId, final RedNoteManifest manifest,
            final VariantManifest.Variant variant, final RedNoteOptions options, final DriverServices services) {
        Assert.notBlank(namespaceId, "RedNote namespace id must not be blank");
        Assert.notBlank(sourceId, "RedNote Source id must not be blank");
        final RedNoteManifest selected = Assert.notNull(manifest, "RedNote manifest must not be null");
        this.variant = Assert.notNull(variant, "RedNote manifest must not be null");
        this.options = Assert.notNull(options, "RedNote options must not be null");
        this.services = Assert.notNull(services, "RedNote execution services must not be null");
        if (!RedNoteManifest.ID.equals(selected.vendor())
                || !selected.variant(RedNoteManifest.MARKETING).equals(variant)
                || !RedNoteManifest.MARKETING.equals(variant.variant()) || variant.protocol() != Protocol.VENDOR_AUTH
                || !RedNoteManifest.ID.equals(options.vendor())
                || !RedNoteManifest.MARKETING.equals(options.variant())) {
            throw new ValidateException("RedNote adapter requires the rednote/marketing Vendor manifest");
        }
        this.formCodec = new FormCodec();
    }

    /**
     * Materializes an operation-scoped application secret and clears the intermediate character buffer.
     *
     * @param lease open secret lease owned by the caller
     * @return transient string required by the form encoder
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
     * Verifies that every decoded member belongs to the RedNote token response union.
     *
     * @param object decoded token response object
     * @return whether every member has registered RedNote semantics
     */
    private static boolean responseMembers(final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            if (!switch (name) {
                case "code", OAuth2.Parameters.ERROR, "sub_error", OAuth2.Parameters.ERROR_DESCRIPTION, OAuth2.Parameters.ACCESS_TOKEN, "access_token_expires_in", OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.SCOPE, OAuth2.Parameters.EXPIRES_IN -> true;
                default -> false;
            }) {
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
            throw new ValidateException("RedNote response requires a non-blank string member: " + name);
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
            throw new ValidateException("RedNote response member must be a JSON string: " + name);
        }
        return string.value();
    }

    /**
     * Reads one exact integral JSON member.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return exact long value
     */
    private static long exactLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("RedNote response requires a numeric member: " + name);
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException cause) {
            throw new ValidateException("RedNote numeric member must be an exact long: " + name, cause);
        }
    }

    /**
     * Reads one optional positive exact integral JSON lifetime.
     *
     * @param object decoded response object
     * @param name   exact lifetime member name
     * @return optional positive exact long
     */
    private static Optional<Long> optionalPositiveLong(final JsonValue.ObjectValue object, final String name) {
        if (!object.values().containsKey(name)) {
            return Optional.empty();
        }
        final long value = exactLong(object, name);
        if (value <= 0L) {
            throw new ValidateException("RedNote token lifetime must be positive: " + name);
        }
        return Optional.of(value);
    }

    /**
     * Narrows a delegated outcome through the declared response type.
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
     * Returns the exact authorization-only RedNote capability manifest.
     *
     * @return immutable marketing capability set
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes only the two profile-scoped RedNote marketing operations.
     *
     * @param capability exact runtime-selected capability
     * @param request    exact nested marketing request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed Vendor outcome without standard OAuth or identity models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "RedNote capability must not be null");
        Assert.notNull(context, "RedNote invocation context must not be null");
        Assert.notNull(timeout, "RedNote invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("RedNote capability is not declared"));
        }
        if (capability.equals(RedNoteManifest.REDNOTE_MARKETING_AUTHORIZE)
                && request instanceof MarketingAuthorizationRequest authorization) {
            return completed(authorize(authorization, capability.responseType()));
        }
        if (capability.equals(RedNoteManifest.REDNOTE_MARKETING_TOKEN)
                && request instanceof MarketingTokenRequest token) {
            return narrow(token(token, context, timeout), capability.responseType());
        }
        return completed(rejected("RedNote capability request is invalid"));
    }

    /**
     * Builds one exact camel-case marketing authorization URL.
     *
     * @param request      profile-scoped authorization request
     * @param responseType exact declared URL response type
     * @param <S>          declared successful response type
     * @return typed authorization outcome
     */
    private <S> Outcome<S> authorize(final MarketingAuthorizationRequest request, final Class<S> responseType) {
        if (!options.redirectUri().getOrNull().equals(request.redirectUri())) {
            return rejected("RedNote authorization callback differs from the registration");
        }
        final List<String> scopes = request.scopes().isEmpty() ? effectiveScopes() : request.scopes();
        if (!effectiveScopes().containsAll(scopes) || scopes.isEmpty()) {
            return rejected("RedNote authorization scopes exceed the registration");
        }
        try {
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final Url location = endpoint.url().newBuilder().query("appId", options.clientId())
                    .query(OAuth2.Parameters.SCOPE, String.join(Symbol.SPACE, scopes))
                    .query("redirectUri", request.redirectUri()).query(OAuth2.Parameters.STATE, request.state())
                    .build();
            return Outcome.succeeded(responseType.cast(location));
        } catch (RuntimeException cause) {
            return rejected("RedNote authorization request is invalid");
        }
    }

    /**
     * Resolves one operation-scoped application secret and executes the selected token branch.
     *
     * @param request exact initial or refresh token request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return marketing token outcome stage
     */
    private CompletionStage<Outcome<MarketingTokenResponse>> token(
            final MarketingTokenRequest request,
            final Context context,
            final Timeout timeout) {
        try {
            final CompletionStage<Outcome<SecretLease>> stage = Outcome.mapStage(
                    () -> services.secretLoader().load(
                            new SecretLoader.Request(services.registration(), options.credential()),
                            context,
                            timeout),
                    loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded));
            if (stage == null) {
                return completed(failed(ErrorCode._502, "RedNote secret loader returned no stage"));
            }
            return stage
                    .handle(
                            (outcome, cause) -> cause == null && outcome != null ? outcome
                                    : RedNoteSourceAdapter
                                            .<SecretLease>failed(ErrorCode._502, "RedNote secret resolution failed"))
                    .thenCompose(outcome -> switch (outcome) {
                        case Outcome.Succeeded<SecretLease> success -> CompletableFuture.supplyAsync(() -> {
                            try (SecretLease secret = success.value()) {
                                return send(request, secret, timeout);
                            } catch (RuntimeException cause) {
                                return failed(ErrorCode._502, "RedNote token operation failed");
                            }
                        }, services.executor());
                        case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                        default -> throw new IllegalStateException("Unsupported Outcome implementation");
                    });
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "RedNote secret resolution failed"));
        }
    }

    /**
     * Sends one exact RedNote initial-token or refresh-token form.
     *
     * @param request validated profile-scoped request
     * @param secret  open application-secret lease
     * @param timeout shared end-to-end timeout
     * @return decoded platform token response
     */
    private Outcome<MarketingTokenResponse> send(
            final MarketingTokenRequest request,
            final SecretLease secret,
            final Timeout timeout) {
        if (timeout.expired()) {
            return failed(ErrorCode._408, "RedNote token request has no remaining timeout");
        }
        byte[] body = null;
        try {
            final boolean initial = request.code().isPresent();
            body = formCodec.encode(
                    List.of(
                            new NameValue("app_id", options.clientId()),
                            new NameValue("secret", secret(secret)),
                            new NameValue(initial ? "code" : OAuth2.Parameters.REFRESH_TOKEN,
                                    initial ? request.code().getOrNull() : request.refreshToken().getOrNull())));
            final var resolvedTargets = variant.targets().resolve(options);
            final String endpoint = (initial ? resolvedTargets.token() : resolvedTargets.refresh()).getOrNull().url()
                    .toString();
            try (Response response = FabricX.http(services.fabric(), Protocol.VENDOR_AUTH, timeout).url(endpoint)
                    .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return decode(response, initial);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "RedNote token request failed");
        } finally {
            if (body != null) {
                Arrays.fill(body, (byte) 0);
            }
        }
    }

    /**
     * Strictly decodes one RedNote token or refresh response union.
     *
     * @param response owned token endpoint response
     * @param initial  whether the active branch exchanges an authorization code
     * @return exact profile-scoped token response or safely classified failure
     */
    private Outcome<MarketingTokenResponse> decode(final Response response, final boolean initial) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (!responseMembers(object)) {
                throw new ValidateException("RedNote token response members are invalid");
            }
            final long code = exactLong(object, "code");
            final String error = optionalString(object, OAuth2.Parameters.ERROR);
            if (response.code() != Http.Status.OK || code != 0L || error != null) {
                optionalString(object, "sub_error");
                optionalString(object, OAuth2.Parameters.ERROR_DESCRIPTION);
                if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
                    return failed(ErrorCode._429, "RedNote token endpoint rate limited the request");
                }
                return response.code() >= Http.Status.INTERNAL_SERVER_ERROR
                        ? failed(ErrorCode._502, "RedNote token endpoint returned an upstream error")
                        : rejected("RedNote token endpoint rejected the request");
            }
            final Optional<Long> expiresIn = optionalPositiveLong(
                    object,
                    initial ? "access_token_expires_in" : OAuth2.Parameters.EXPIRES_IN);
            return Outcome.succeeded(
                    new MarketingTokenResponse(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN),
                            Optional.ofNullable(optionalString(object, OAuth2.Parameters.REFRESH_TOKEN)),
                            Optional.ofNullable(optionalString(object, OAuth2.Parameters.SCOPE)), expiresIn));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "RedNote token endpoint returned an invalid response");
        }
    }

    /**
     * Strictly reads one bounded RedNote JSON object.
     *
     * @param response response whose body remains owned by the caller
     * @return immutable provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final Response response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("RedNote response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("RedNote response root must be a JSON object");
        }
        return object;
    }

    /**
     * Returns explicit configured scopes or the immutable manifest default.
     *
     * @return ordered effective marketing scopes
     */
    private List<String> effectiveScopes() {
        return options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
    }

}
