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
package org.miaixz.bus.auth.vendor.coding;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.codec.QueryCodec;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.ResponseType;
import org.miaixz.bus.auth.protocol.oauth2.TokenType;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements CODING team browser authentication using its registered OAuth 2.0 wire contract.
 * <p>
 * CODING's comma-separated authorization scope, string {@code expires_in}, callback team binding, and OpenAPI response
 * envelope remain private adaptations. The adapter therefore exposes only Source authentication and never presents
 * those platform documents as standard OAuth token or OpenID Connect UserInfo responses.
 * </p>
 *
 * @author Kimi Liu
 */
public class CodingSourceAdapter implements VendorAdapter {

    /**
     * Trusted CODING authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://coding.net";

    /**
     * Registered Source identifier copied into the verified identity.
     */
    private final String sourceId;

    /**
     * Selected immutable CODING manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded CODING options.
     */
    private final CodingOptions options;

    /**
     * Caller-owned runtime, loaders, parsers, JSON, network, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Shared one-time state lifecycle for the browser operation.
     */
    private final RedirectManager redirectManager;

    /**
     * Shared strict RFC 3986 query encoder.
     */
    private final QueryCodec queryCodec = new QueryCodec();

    /**
     * Shared strict form encoder for CODING's token endpoint.
     */
    private final FormCodec formCodec = new FormCodec();

    /**
     * Creates one Source-bound CODING adapter from the frozen default manifest.
     *
     * @param spaceId  registration space used to isolate browser state and credentials
     * @param sourceId registered Source identifier
     * @param manifest selected CODING manifest
     * @param variant  selected default variant manifest
     * @param options  decoded externally loaded CODING options
     * @param services caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, manifest, or options differ from the frozen manifest
     */
    public CodingSourceAdapter(final String spaceId, final String sourceId, final CodingManifest manifest,
            final VariantManifest.Variant variant, final CodingOptions options, final DriverServices services) {
        Assert.notNull(manifest, "CODING manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "CODING Source id must not be blank");
        this.variant = Assert.notNull(variant, "CODING manifest must not be null");
        this.options = Assert.notNull(options, "CODING options must not be null");
        this.services = Assert.notNull(services, "CODING execution services must not be null");
        if (!CodingManifest.ID.equals(manifest.vendor()) || !manifest.variant(CodingManifest.DEFAULT).equals(variant)
                || !CodingManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !CodingManifest.ID.equals(options.vendor()) || !CodingManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("CODING adapter requires the coding/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(spaceId, sourceId, variant, options, services);
    }

    /**
     * Splits and validates one exact non-empty platform scope sequence.
     *
     * @param value     encoded scope value
     * @param separator literal delimiter
     * @return immutable ordered scope list
     */
    private static List<String> separated(final String value, final String separator) {
        final String[] parts = Assert.notBlank(value, "CODING scope value must not be blank").split(separator, -1);
        final List<String> values = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isBlank() || values.contains(part)) {
                throw new ValidateException("CODING scope sequence is invalid");
            }
            values.add(part);
        }
        return List.copyOf(values);
    }

    /**
     * Reads one required non-blank JSON string.
     *
     * @param object decoded response object
     * @param name   exact member name
     * @return non-blank string value
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("CODING response requires a non-blank string field");
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
            throw new ValidateException("CODING response requires an integral number field");
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException cause) {
            throw new ValidateException("CODING response number must be an exact long", cause);
        }
    }

    /**
     * Tests whether an object has exactly the private CODING token success shape.
     *
     * @param object decoded token object
     * @return whether every and only success member is present
     */
    private static boolean tokenSuccess(final JsonValue.ObjectValue object) {
        if (object.values().size() != 6) {
            return false;
        }
        for (String member : object.values().keySet()) {
            switch (member) {
                case OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.SCOPE, "team", OAuth2.Parameters.TOKEN_TYPE, OAuth2.Parameters.EXPIRES_IN -> {
                    // Registered success member.
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Tests whether an object has exactly the private CODING token error shape.
     *
     * @param object decoded token object
     * @return whether every and only error member is present
     */
    private static boolean tokenError(final JsonValue.ObjectValue object) {
        if (object.values().size() != 3) {
            return false;
        }
        for (String member : object.values().keySet()) {
            switch (member) {
                case "code", "msg", "data" -> {
                    // Registered error member.
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Tests one member against CODING's private current-user vocabulary.
     *
     * @param member current-user member name
     * @return whether the profile decoder recognizes the member
     */
    private static boolean userMember(final String member) {
        return switch (member) {
            case "Id", "Status", "Email", "GlobalKey", "Avatar", "Gravatar", "Name", "NamePinYin", "Phone", "PhoneValidation", "EmailValidation", "PhoneRegionCode", "Region", "TeamId", "WeComId" -> true;
            default -> false;
        };
    }

    /**
     * Parses CODING's documented positive decimal string lifetime.
     *
     * @param object decoded token response
     * @param name   exact member name
     * @return positive lifetime in seconds
     */
    private static long positiveDecimalString(final JsonValue.ObjectValue object, final String name) {
        final String value = requiredString(object, name);
        if (!value.chars().allMatch(Character::isDigit)) {
            throw new ValidateException("CODING token lifetime must contain decimal digits only");
        }
        try {
            final long parsed = Long.parseLong(value);
            if (parsed <= 0L) {
                throw new ValidateException("CODING token lifetime must be positive");
            }
            return parsed;
        } catch (NumberFormatException cause) {
            throw new ValidateException("CODING token lifetime exceeds the supported range", cause);
        }
    }

    /**
     * Narrows a delegated outcome through the capability's declared response class.
     *
     * @param stage        delegated outcome stage
     * @param responseType declared successful response class
     * @param <S>          expected successful value type
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
     * Creates a safe platform rejection retaining only CODING's numeric error code.
     *
     * @param code        documented nonzero platform error code
     * @param description non-sensitive rejection description
     * @param <T>         expected successful value type
     * @return rejected outcome with bounded safe details
     */
    private static <T> Outcome<T> rejected(final long code, final String description) {
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(
                        Map.of("code", new JsonValue.NumberValue(java.math.BigDecimal.valueOf(code))))));
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
     * Creates an immutable empty JSON details object.
     *
     * @return empty JSON object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Accepts one callback value only once.
     *
     * @param current previously decoded value or {@code null}
     * @param value   next non-blank value
     * @return next value
     * @throws ValidateException if the callback member is duplicated
     */
    private static String unique(final String current, final String value) {
        if (current != null) {
            throw new ValidateException("CODING callback parameter names must be unique");
        }
        return value;
    }

    /**
     * Returns the exact Source-authentication-only capability manifest.
     *
     * @return immutable CODING capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes the two CODING Source authentication operations.
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
        Assert.notNull(capability, "CODING capability must not be null");
        Assert.notNull(context, "CODING invocation context must not be null");
        Assert.notNull(timeout, "CODING invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("CODING capability is not declared"));
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
        return completed(rejected("CODING capability request is invalid"));
    }

    /**
     * Builds CODING's exact ordered authorization query using generated one-time state.
     *
     * @param initiation generated browser correlation
     * @param context    immutable invocation context retained by the uniform operation signature
     * @param timeout    shared end-to-end timeout
     * @return prepared CODING authorization redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(context, "CODING authorization context must not be null");
        if (timeout.expired() || initiation.nonce().isPresent() || initiation.codeChallenge().isPresent()) {
            return completed(failed("CODING authorization security material violates the frozen manifest"));
        }
        final List<NameValue> parameters = List.of(
                new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                new NameValue(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()),
                new NameValue(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value()),
                new NameValue(OAuth2.Parameters.STATE, initiation.state()),
                new NameValue(OAuth2.Parameters.SCOPE, String.join(Symbol.COMMA, requestedScopes())));
        final String endpoint = variant.targets().resolve(options).authorization().getOrNull().url().toString();
        final String redirect = endpoint + Symbol.C_QUESTION_MARK + queryCodec.encode(parameters);
        return completed(Outcome.succeeded(new RedirectManager.Prepared(redirect, initiation.state())));
    }

    /**
     * Extracts state after validating CODING's exact success or OAuth error callback.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return unique non-blank state value
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Resolves the client secret and completes the private token and current-user chain.
     *
     * @param completion consumed browser correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified CODING external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("CODING authorization callback is invalid"));
        }
        if (values.error() != null) {
            return completed(rejected("CODING authorization endpoint returned an OAuth error"));
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
            return completed(failed("CODING client-secret resolution failed"));
        }
        if (resolution == null) {
            return completed(failed("CODING client-secret loader returned no stage"));
        }
        return resolution
                .handle(
                        (outcome, cause) -> cause == null && outcome != null ? outcome
                                : CodingSourceAdapter.<SecretLease>failed("CODING client-secret resolution failed"))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SecretLease> success -> token(values.code(), success.value(), timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Sends CODING's exact authorization-code form and closes the client-secret lease.
     *
     * @param code    consumed authorization code
     * @param secret  owned client-secret lease
     * @param timeout shared end-to-end timeout
     * @return verified identity after token and OpenAPI processing
     */
    private CompletionStage<Outcome<ExternalIdentity>> token(
            final String code,
            final SecretLease secret,
            final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try (secret) {
                if (timeout.expired()) {
                    return CodingSourceAdapter.<Access>failed("CODING token request has no remaining timeout");
                }
                body = formCodec.encode(
                        List.of(
                                new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                                new NameValue(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material())),
                                new NameValue(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()),
                                new NameValue(OAuth2.Parameters.CODE, code)));
                final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
                try (Response response = FabricX.http(services.fabric(), Protocol.OAUTH2, timeout).url(endpoint)
                        .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                    return decodeToken(response);
                }
            } catch (RuntimeException cause) {
                return CodingSourceAdapter.<Access>failed("CODING token request failed");
            } finally {
                if (body != null) {
                    Arrays.fill(body, (byte) 0);
                }
            }
        }, services.executor()).thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<Access> success -> profile(success.value(), timeout);
            case Outcome.Rejected<Access> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<Access> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Strictly decodes CODING's private token success or documented error envelope.
     *
     * @param response owned CODING token HTTP response
     * @return private access value or safely classified failure
     */
    private Outcome<Access> decodeToken(final Response response) {
        if (response.code() == Http.Status.TOO_MANY_REQUESTS || response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed("CODING token endpoint is unavailable");
        }
        if (response.code() >= Http.Status.BAD_REQUEST) {
            return rejected("CODING token endpoint rejected the authorization code");
        }
        final JsonValue.ObjectValue object;
        try {
            object = object(response, "token");
        } catch (RuntimeException cause) {
            return failed("CODING token endpoint returned an invalid response");
        }
        if (tokenError(object)) {
            try {
                final long code = exactLong(object, "code");
                if (code == 0L || !(object.values().get("msg") instanceof JsonValue.ObjectValue)
                        || !(object.values().get("data") instanceof JsonValue.ObjectValue)) {
                    throw new ValidateException("CODING token error envelope is invalid");
                }
                return rejected(code, "CODING token endpoint returned a platform error");
            } catch (RuntimeException cause) {
                return failed("CODING token error response is invalid");
            }
        }
        if (!tokenSuccess(object)) {
            return failed("CODING token response members are invalid");
        }
        try {
            final String accessToken = requiredString(object, OAuth2.Parameters.ACCESS_TOKEN);
            requiredString(object, OAuth2.Parameters.REFRESH_TOKEN);
            final String tokenType = requiredString(object, OAuth2.Parameters.TOKEN_TYPE);
            final String team = requiredString(object, "team");
            final List<String> scopes = separated(requiredString(object, OAuth2.Parameters.SCOPE), Symbol.SPACE);
            final long expiresIn = positiveDecimalString(object, OAuth2.Parameters.EXPIRES_IN);
            if (!TokenType.BEARER.value().equalsIgnoreCase(tokenType) || !options.team().equals(team)
                    || !scopes.equals(requestedScopes())) {
                throw new ValidateException("CODING token binding is invalid");
            }
            return Outcome.succeeded(new Access(accessToken, expiresIn));
        } catch (RuntimeException cause) {
            return rejected("CODING token success response is invalid");
        }
    }

    /**
     * Calls CODING OpenAPI with the private bearer token and exact current-user action.
     *
     * @param access  private access-token result
     * @param timeout shared end-to-end timeout
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> profile(final Access access, final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] body = null;
            try {
                if (timeout.expired()) {
                    return CodingSourceAdapter
                            .<ExternalIdentity>failed("CODING profile request has no remaining timeout");
                }
                body = services.jsonProvider().writeValue(
                        new JsonValue.ObjectValue(
                                Map.of("Action", new JsonValue.StringValue("DescribeCodingCurrentUser"))));
                final String endpoint = variant.targets().resolve(options).userInfo().getOrNull().url().toString();
                try (Response response = FabricX.http(services.fabric(), Protocol.OAUTH2, timeout).url(endpoint)
                        .method(Http.Method.POST).header(Http.Header.AUTHORIZATION, "Bearer " + access.accessToken())
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .body(body, MediaType.APPLICATION_JSON_TYPE).execute()) {
                    return decodeProfile(response, timeout);
                }
            } catch (RuntimeException cause) {
                return CodingSourceAdapter.<ExternalIdentity>failed("CODING profile request failed");
            } finally {
                if (body != null) {
                    Arrays.fill(body, (byte) 0);
                }
            }
        }, services.executor());
    }

    /**
     * Maps CODING's exact OpenAPI envelope using only the positive integral user identifier as subject.
     *
     * @param response owned CODING OpenAPI response
     * @param timeout  shared clock used for identity evidence
     * @return verified external identity or safely classified failure
     */
    private Outcome<ExternalIdentity> decodeProfile(final Response response, final Timeout timeout) {
        if (response.code() == Http.Status.TOO_MANY_REQUESTS || response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed("CODING profile endpoint is unavailable");
        }
        if (response.code() >= Http.Status.BAD_REQUEST) {
            return rejected("CODING profile endpoint rejected the access token");
        }
        try {
            final JsonValue.ObjectValue root = object(response, "profile");
            if (root.values().size() != 1 || !root.values().containsKey("Response")
                    || !(root.values().get("Response") instanceof JsonValue.ObjectValue envelope)
                    || envelope.values().size() != 2 || !envelope.values().containsKey("RequestId")
                    || !envelope.values().containsKey("User")) {
                throw new ValidateException("CODING profile envelope is invalid");
            }
            requiredString(envelope, "RequestId");
            if (!(envelope.values().get("User") instanceof JsonValue.ObjectValue user)) {
                throw new ValidateException("CODING profile User members are invalid");
            }
            for (String member : user.values().keySet()) {
                if (!userMember(member)) {
                    throw new ValidateException("CODING profile User members are invalid");
                }
            }
            final Profile profile = Profile.decode(user);
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("Id", new JsonValue.StringValue(profile.subject()), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, profile.subject(), profile.attributes(), List.of(evidence)));
        } catch (RuntimeException cause) {
            return rejected("CODING profile response is invalid");
        }
    }

    /**
     * Strictly decodes one bounded HTTP 200 JSON object.
     *
     * @param response  response whose body remains open
     * @param operation non-sensitive operation label used in validation messages
     * @return decoded object
     */
    private JsonValue.ObjectValue object(final Response response, final String operation) {
        if (response.code() != Http.Status.OK
                || !MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("CODING " + operation + " response must use HTTP 200 application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES), Normal._64, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("CODING JSON response root must be an object");
        }
        return object;
    }

    /**
     * Validates and indexes CODING's exact registered GET callback.
     *
     * @param callback raw inbound callback
     * @return typed private callback value
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "CODING callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("CODING callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        String team = null;
        String scope = null;
        String error = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert.notBlank(parameter.value(), "CODING callback value must not be blank");
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code, value);
                case OAuth2.Parameters.STATE -> state = unique(state, value);
                case "team" -> team = unique(team, value);
                case OAuth2.Parameters.SCOPE -> scope = unique(scope, value);
                case OAuth2.Parameters.ERROR -> error = unique(error, value);
                default -> throw new ValidateException("CODING callback contains an unregistered parameter");
            }
        }
        final boolean success = code != null && state != null && team != null && scope != null && error == null;
        final boolean rejected = code == null && state != null && team == null && scope == null && error != null;
        if (!success && !rejected) {
            throw new ValidateException("CODING callback branch is invalid");
        }
        if (success && (!options.team().equals(team) || !separated(scope, Symbol.COMMA).equals(requestedScopes()))) {
            throw new ValidateException("CODING callback team or scope binding is invalid");
        }
        return new CallbackWire(code, state, team, scope, error);
    }

    /**
     * Returns explicit registered scopes or the immutable manifest default.
     *
     * @return ordered effective CODING scopes
     */
    private List<String> requestedScopes() {
        return options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
    }

    /**
     * Carries CODING's private callback branch and its platform binding values.
     *
     * @param code  authorization code on success
     * @param state mandatory browser correlation value
     * @param team  team binding on success
     * @param scope comma-delimited scope binding on success
     * @param error OAuth error on rejection
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state, String team, String scope, String error) {

        /**
         * Validates one exact success or rejection branch.
         *
         * @throws IllegalArgumentException if state is blank
         * @throws ValidateException        if branch fields are inconsistent
         */
        private CallbackWire {
            Assert.notBlank(state, "CODING callback state must not be blank");
            final boolean success = code != null && team != null && scope != null && error == null;
            final boolean rejected = code == null && team == null && scope == null && error != null;
            if (!success && !rejected) {
                throw new ValidateException("CODING callback branch is invalid");
            }
        }

    }

    /**
     * Carries the validated private CODING current-user result.
     *
     * @param subject    positive CODING user identifier
     * @param attributes validated scalar profile attributes
     * @author Kimi Liu
     */
    private record Profile(String subject, JsonValue.ObjectValue attributes) {

        /**
         * Validates one typed private profile.
         *
         * @throws IllegalArgumentException if a component is absent or blank
         */
        private Profile {
            Assert.notBlank(subject, "CODING private profile subject must not be blank");
            attributes = Assert.notNull(attributes, "CODING private profile attributes must not be null");
        }

        /**
         * Decodes the current-user object without maintaining a parallel field-name collection.
         *
         * @param user validated private current-user object
         * @return typed private profile
         * @throws ValidateException if the identifier or retained attribute is invalid
         */
        private static Profile decode(final JsonValue.ObjectValue user) {
            final long identifier = exactLong(user, "Id");
            if (identifier <= 0L) {
                throw new ValidateException("CODING user identifier must be positive");
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            for (Map.Entry<String, JsonValue> entry : user.values().entrySet()) {
                switch (entry.getKey()) {
                    case "Status", "Email", "GlobalKey", "Avatar", "Gravatar", "Name", "NamePinYin", "PhoneValidation", "EmailValidation", "PhoneRegionCode", "Region", "TeamId", "WeComId" -> add(
                            attributes,
                            entry.getKey(),
                            entry.getValue());
                    case "Id", "Phone" -> {
                        // Subject and intentionally excluded sensitive phone value.
                    }
                    default -> throw new ValidateException("CODING profile contains an unregistered member");
                }
            }
            return new Profile(Long.toString(identifier), new JsonValue.ObjectValue(attributes));
        }

        /**
         * Adds one present scalar profile attribute.
         *
         * @param attributes mutable map confined to profile decoding
         * @param member     exact CODING member name
         * @param value      decoded JSON value
         * @throws ValidateException if the value is structured rather than scalar
         */
        private static void add(final Map<String, JsonValue> attributes, final String member, final JsonValue value) {
            if (value == null || value instanceof JsonValue.NullValue) {
                return;
            }
            if (value instanceof JsonValue.ArrayValue || value instanceof JsonValue.ObjectValue) {
                throw new ValidateException("CODING profile attribute must be a JSON scalar");
            }
            attributes.put(member, value);
        }

    }

    /**
     * Carries CODING's private access token only until the current-user request completes.
     *
     * @param accessToken sensitive bearer access token
     * @param expiresIn   positive token lifetime in seconds
     * @author Kimi Liu
     */
    private record Access(String accessToken, long expiresIn) {

        /**
         * Validates one private token result.
         *
         * @throws IllegalArgumentException if the token is blank
         * @throws ValidateException        if the lifetime is not positive
         */
        private Access {
            Assert.notBlank(accessToken, "CODING private access token must not be blank");
            if (expiresIn <= 0L) {
                throw new ValidateException("CODING private access-token lifetime must be positive");
            }
        }

        /**
         * Returns a diagnostic representation without bearer material.
         *
         * @return redacted private token summary
         */
        @Override
        public String toString() {
            return Builder.REDACTED_ACCESS_TOKEN + expiresIn + Symbol.BRACKET_RIGHT;
        }

    }

}
