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
package org.miaixz.bus.auth.vendor.figma;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.Parameter;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.AuthorizationClient;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements Figma OAuth browser login while publishing only standard OAuth authorization.
 * <p>
 * The private completion chain sends Figma's exact Basic-authenticated token request and validates that the current
 * REST user's durable identifier equals the token response's {@code user_id_string}. No private token or REST model is
 * exposed as a framework standard capability.
 * </p>
 *
 * @author Kimi Liu
 */
public final class FigmaSourceAdapter implements VendorAdapter {

    /**
     * Trusted Figma API authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://api.figma.com";

    /**
     * Maximum accepted Figma JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum accepted Figma JSON nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Figma manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Figma options.
     */
    private final FigmaOptions options;

    /**
     * Caller-owned secret, JSON, network, clock, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Shared standard OAuth authorization implementation.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time state and mandatory PKCE lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Shared strict UTF-8 form encoder.
     */
    private final FormCodec formCodec;

    /**
     * Creates one Source-bound Figma.com adapter.
     *
     * @param namespaceId registration namespace used to isolate state and credential resolution
     * @param sourceId    registered Source identifier
     * @param manifest    selected Figma manifest
     * @param variant     exact selected default manifest
     * @param options     decoded externally loaded Figma options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, manifest, callback, or authorization is inconsistent
     */
    public FigmaSourceAdapter(final String namespaceId, final String sourceId, final FigmaManifest manifest,
            final VariantManifest.Variant variant, final FigmaOptions options, final DriverServices services) {
        final FigmaManifest selectedProfile = Assert.notNull(manifest, "Figma manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Figma Source id must not be blank");
        this.variant = Assert.notNull(variant, "Figma manifest must not be null");
        this.options = Assert.notNull(options, "Figma options must not be null");
        this.services = Assert.notNull(services, "Figma execution services must not be null");
        if (!FigmaManifest.ID.equals(selectedProfile.vendor())
                || !selectedProfile.variant(FigmaManifest.DEFAULT).equals(variant)
                || !FigmaManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !FigmaManifest.ID.equals(options.vendor()) || !FigmaManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("Figma adapter requires the figma/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        final var targets = variant.targets().resolve(options);
        final OAuth2ClientOptions oauthSettings = new OAuth2ClientOptions(targets.authorization(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                options.clientId(), Set.of(options.redirectUri().getOrNull()), Endpoint.Authentication.NONE,
                Optional.empty(), true, false);
        final AuthorizationClient authorizationClient = new AuthorizationClient(oauthSettings,
                new AuthorizationRequestEncoder(targets.authorization().getOrNull()));
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager), List
                .of(new StandardAdapter.Binding<>(OAuth2ClientScheme.AUTHORIZATION, authorizationClient::authorize)));
        this.formCodec = new FormCodec();
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
            throw new ValidateException("Figma requires a non-blank string member");
        }
        return text.value();
    }

    /**
     * Verifies the private Figma token-success vocabulary.
     *
     * @param token decoded token response
     * @return whether every member belongs to the documented success response
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue token) {
        for (String member : token.values().keySet()) {
            switch (member) {
                case "user_id_string", "user_id", OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.EXPIRES_IN, OAuth2.Parameters.TOKEN_TYPE -> {
                    // Registered Figma token member.
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Verifies the exact private Figma current-user shape.
     *
     * @param user decoded current-user response
     * @return whether all four required members are present without unknown members
     */
    private static boolean userMembers(final JsonValue.ObjectValue user) {
        return user.values().size() == 4 && user.values().containsKey("id") && user.values().containsKey("handle")
                && user.values().containsKey("img_url") && user.values().containsKey("email");
    }

    /**
     * Reads one required positive exact integral JSON number.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return positive long value
     * @throws ValidateException if absent, non-integral, out of range, or not positive
     */
    private static long requiredPositiveLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Figma requires an integral numeric member");
        }
        try {
            final long result = number.value().longValueExact();
            if (result <= 0L) {
                throw new ValidateException("Figma token lifetime must be positive");
            }
            return result;
        } catch (ArithmeticException cause) {
            throw new ValidateException("Figma token lifetime must be an exact long", cause);
        }
    }

    /**
     * Classifies one Figma HTTP status without parsing its undocumented error body.
     *
     * @param status      exact response status
     * @param description non-sensitive operation description
     * @param <T>         expected success type
     * @return rejected client condition or failed upstream condition
     */
    private static <T> Outcome<T> status(final int status, final String description) {
        final Map<String, JsonValue> details = Map.of("status", new JsonValue.NumberValue(BigDecimal.valueOf(status)));
        if (status == Http.Status.TOO_MANY_REQUESTS || status >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(
                    status == Http.Status.TOO_MANY_REQUESTS ? ErrorCode._429 : ErrorCode._502,
                    description,
                    details);
        }
        if (status >= Http.Status.BAD_REQUEST && status < Http.Status.INTERNAL_SERVER_ERROR) {
            return Outcome
                    .rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(details)));
        }
        return failed(ErrorCode._502, description, details);
    }

    /**
     * Constructs mutable UTF-8 bytes for the exact Basic credential before Base64 encoding.
     *
     * @param clientId public Figma OAuth Client ID
     * @param secret   sensitive Client Secret bytes
     * @return newly allocated {@code clientId:secret} bytes
     */
    private static byte[] basic(final String clientId, final char[] secret) {
        final byte[] prefix = (clientId + Symbol.C_COLON).getBytes(Charset.UTF_8);
        final byte[] suffix = new String(secret).getBytes(Charset.UTF_8);
        final byte[] result = Arrays.copyOf(prefix, prefix.length + suffix.length);
        System.arraycopy(suffix, 0, result, prefix.length, suffix.length);
        clear(prefix);
        clear(suffix);
        return result;
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
     * Creates a safe missing-capability rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejected stage
     */
    private static <T> CompletionStage<Outcome<T>> missing() {
        return completed(rejected("Figma capability is not declared by the selected manifest"));
    }

    /**
     * Creates a safe request-contract rejection stage.
     *
     * @param <T> expected success type
     * @return completed rejected stage
     */
    private static <T> CompletionStage<Outcome<T>> mismatch() {
        return completed(rejected("Figma request does not match the selected capability contract"));
    }

    /**
     * Creates a safe expected rejection.
     *
     * @param description non-sensitive rejection description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
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
     * Creates a safe operational failure with non-sensitive structured details.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param details     non-sensitive HTTP status details
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
     * Erases one optional mutable byte array.
     *
     * @param value mutable sensitive bytes, or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) 0);
        }
    }

    /**
     * Returns the exact capability manifest frozen by the selected Figma manifest.
     *
     * @return immutable Source authentication and OAuth authorization manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and the registered standard OAuth authorization operation.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Figma-private response models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Figma capability must not be null");
        Assert.notNull(context, "Figma invocation context must not be null");
        Assert.notNull(timeout, "Figma invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return missing();
        }
        if (capability.key().equals(SourceAuthentication.INITIATE.key())
                && request instanceof SourceAuthentication.Request.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (capability.key().equals(SourceAuthentication.COMPLETE.key())
                && request instanceof SourceAuthentication.Request.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::identity, context, timeout),
                    capability.responseType());
        }
        if (capability.equals(OAuth2ClientScheme.AUTHORIZATION) && request instanceof AuthorizationRequest authorization
                && valid(authorization)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return mismatch();
    }

    /**
     * Builds the exact Figma authorization redirect from generated state and S256 challenge.
     *
     * @param initiation generated browser correlation and PKCE challenge
     * @param context    immutable invocation context retained for operation consistency
     * @param timeout    shared end-to-end budget
     * @return exact redirect and state correlation
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "Figma authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Figma authorization has no remaining time budget"));
        }
        try {
            final var challenge = initiation.codeChallenge().getOrNull();
            if (initiation.nonce().isPresent() || challenge == null || !PkceMethod.S256.equals(challenge.method())) {
                return completed(
                        failed(ErrorCode._500, "Figma browser flow did not generate the required S256 PKCE material"));
            }
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final String redirect = endpoint.url().newBuilder().query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.SCOPE, String.join(Symbol.SPACE, options.scopes()))
                    .query(OAuth2.Parameters.STATE, initiation.state())
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query(OAuth2.Parameters.CODE_CHALLENGE, challenge.value())
                    .query(OAuth2.Parameters.CODE_CHALLENGE_METHOD, PkceMethod.S256.value()).build().toString();
            return completed(Outcome.succeeded(new RedirectManager.Prepared(redirect, initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("Figma authorization request is invalid"));
        }
    }

    /**
     * Extracts the unique state from the exact Figma success callback.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return unique non-blank state
     * @throws ValidateException if callback transport, target, values, or multiplicity is invalid
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Exchanges one correlated Figma callback and resolves the verified current user.
     *
     * @param completion consumed callback correlation with the one-time PKCE verifier
     * @param context    immutable invocation context used for one secret resolution
     * @param timeout    shared end-to-end budget
     * @return verified Figma identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final String code;
        try {
            code = callback(completion.callback()).code();
        } catch (RuntimeException cause) {
            return completed(rejected("Figma authorization callback is invalid"));
        }
        if (completion.codeVerifier().isEmpty()) {
            return completed(failed(ErrorCode._500, "Figma callback lacks its required PKCE verifier"));
        }
        final String verifier = completion.codeVerifier().getOrNull().value();
        return Outcome.mapStage(
                        () -> services.secretLoader().load(options.credential(), context, timeout),
                        loaded -> services.secretParser().parse(options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(
                            code,
                            verifier,
                            success.value(),
                            timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Executes the private Figma token and profile requests under one owned secret lease.
     *
     * @param code     consumed authorization code
     * @param verifier consumed RFC 7636 verifier
     * @param secret   owned Client Secret lease closed by this asynchronous operation
     * @param timeout  shared end-to-end budget
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> authenticate(
            final String code,
            final String verifier,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try (secret) {
                return switch (token(code, verifier, secret, timeout)) {
                    case Outcome.Succeeded<Access> success -> profile(success.value(), timeout);
                    case Outcome.Rejected<Access> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Access> failed -> Outcome.failed(failed.failure());
                };
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Figma authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Executes the exact Figma Basic-authenticated authorization-code request.
     *
     * @param code     sensitive one-time authorization code
     * @param verifier sensitive one-time RFC 7636 verifier
     * @param secret   still-open Client Secret lease
     * @param timeout  shared end-to-end budget
     * @return private access result or safely classified Figma failure
     */
    private Outcome<Access> token(
            final String code,
            final String verifier,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        byte[] body = null;
        byte[] basicBytes = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Figma token request has no remaining time budget");
            }
            body = formCodec.encode(
                    List.of(
                            new Parameter(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()),
                            new Parameter(OAuth2.Parameters.CODE,
                                    Assert.notBlank(code, "Figma authorization code must not be blank")),
                            new Parameter(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()),
                            new Parameter(OAuth2.Parameters.CODE_VERIFIER,
                                    Assert.notBlank(verifier, "Figma code verifier must not be blank"))));
            basicBytes = basic(options.clientId(), secret.material());
            final String authorization = "Basic " + Base64.encode(basicBytes);
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST).header(Http.Header.AUTHORIZATION, authorization)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Figma token request failed");
        } finally {
            clear(body);
            clear(basicBytes);
        }
    }

    /**
     * Strictly decodes one Figma authorization-code token response.
     *
     * @param response owned token endpoint response
     * @return private access result or safely classified failure
     */
    private Outcome<Access> token(final HttpResponse response) {
        if (response.code() != Http.Status.OK) {
            return status(response.code(), "Figma token endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue object = object(response, "token");
            if (!tokenMembers(object) || !object.values().containsKey("user_id_string")
                    || !object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                    || !object.values().containsKey(OAuth2.Parameters.REFRESH_TOKEN)
                    || !object.values().containsKey(OAuth2.Parameters.EXPIRES_IN)
                    || !object.values().containsKey(OAuth2.Parameters.TOKEN_TYPE)
                    || object.values().containsKey(OAuth2.Parameters.ERROR)) {
                return failed(ErrorCode._502, "Figma token success response is invalid");
            }
            final String userId = requiredString(object, "user_id_string");
            final String accessToken = requiredString(object, OAuth2.Parameters.ACCESS_TOKEN);
            requiredString(object, OAuth2.Parameters.REFRESH_TOKEN);
            final long expiresIn = requiredPositiveLong(object, OAuth2.Parameters.EXPIRES_IN);
            if (!"bearer".equals(requiredString(object, OAuth2.Parameters.TOKEN_TYPE).toLowerCase(Locale.ROOT))) {
                throw new ValidateException("Figma token type must be Bearer");
            }
            return Outcome.succeeded(new Access(userId, accessToken, expiresIn));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Figma token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves and validates the Figma current user.
     *
     * @param access  private access-token result carrying the token user identifier
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> profile(final Access access, final Timeout.Budget timeout) {
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Figma profile request has no remaining time budget");
            }
            final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET)
                    .header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + access.accessToken())
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                return profile(response, access.userId(), timeout);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Figma profile request failed");
        }
    }

    /**
     * Strictly decodes the Figma current user and verifies token-to-user binding.
     *
     * @param response    owned current-user response
     * @param tokenUserId exact user identifier returned by the token endpoint
     * @param timeout     shared clock used for evidence verification time
     * @return verified identity or safely classified Figma failure
     */
    private Outcome<ExternalIdentity> profile(
            final HttpResponse response,
            final String tokenUserId,
            final Timeout.Budget timeout) {
        if (response.code() != Http.Status.OK) {
            return status(response.code(), "Figma current-user endpoint rejected or failed the request");
        }
        try {
            final JsonValue.ObjectValue object = object(response, "profile");
            if (!userMembers(object)) {
                return failed(ErrorCode._502, "Figma current-user response is invalid");
            }
            final String subject = requiredString(object, "id");
            if (!subject.equals(tokenUserId)) {
                return failed(ErrorCode._502, "Figma token and current-user identifiers do not match");
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            requiredString(object, "handle");
            requiredString(object, "img_url");
            requiredString(object, "email");
            attributes.put("handle", object.values().get("handle"));
            attributes.put("img_url", object.values().get("img_url"));
            attributes.put("email", object.values().get("email"));
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim("figma_user_id", new JsonValue.StringValue(subject), AUTHORITY,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Figma current-user endpoint returned an invalid response");
        }
    }

    /**
     * Validates one public standard authorization request against the Figma registration.
     *
     * @param request standard OAuth authorization request
     * @return {@code true} only when client, redirect, scope, state, response, PKCE, and extensions are exact
     */
    private boolean valid(final AuthorizationRequest request) {
        final Scope scope = request.scope().getOrNull();
        return ResponseType.CODE.equals(request.responseType()) && options.clientId().equals(request.clientId())
                && options.redirectUri().equals(request.redirectUri()) && scope != null
                && options.scopes().equals(scope.values()) && request.state().isPresent()
                && request.codeChallenge().isPresent()
                && PkceMethod.S256.value().equals(request.codeChallengeMethod().getOrNull())
                && request.extensions().values().isEmpty();
    }

    /**
     * Validates and indexes the exact Figma GET success callback.
     *
     * @param callback raw inbound callback
     * @return typed exact success callback
     * @throws ValidateException if target, transport, names, or values violate the frozen contract
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Figma callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Figma callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert.notBlank(parameter.value(), "Figma callback value must not be blank");
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code, value);
                case OAuth2.Parameters.STATE -> state = unique(state, value);
                default -> throw new ValidateException("Figma callback contains an unsupported parameter");
            }
        }
        return new CallbackWire(code, state);
    }

    /**
     * Accepts one callback value only once.
     *
     * @param current previously decoded value, or {@code null}
     * @param value   newly decoded non-blank value
     * @return newly decoded value
     * @throws ValidateException if the callback repeats a parameter
     */
    private String unique(final String current, final String value) {
        if (current != null) {
            throw new ValidateException("Figma callback parameter names must be unique");
        }
        return value;
    }

    /**
     * Decodes one bounded Figma JSON object.
     *
     * @param response  response whose body remains open
     * @param operation safe operation label used only in validation messages
     * @return strict provider-neutral JSON object
     * @throws ValidateException if media, JSON shape, depth, or duplicate members are invalid
     */
    private JsonValue.ObjectValue object(final HttpResponse response, final String operation) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Figma " + operation + " response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Figma " + operation + " response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries the exact Figma authorization success callback.
     *
     * @param code  authorization code
     * @param state browser correlation value
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state) {

        /**
         * Validates the mandatory success callback values.
         *
         * @throws IllegalArgumentException if code or state is blank
         */
        private CallbackWire {
            Assert.notBlank(code, "Figma callback code must not be blank");
            Assert.notBlank(state, "Figma callback state must not be blank");
        }

    }

    /**
     * Carries Figma's private token success fields required by the immediate current-user request.
     *
     * @param userId      durable Figma user identifier returned with the token
     * @param accessToken sensitive access token
     * @param expiresIn   positive access-token lifetime in seconds
     * @author Kimi Liu
     */
    private record Access(String userId, String accessToken, long expiresIn) {

        /**
         * Validates one private Figma token success value.
         *
         * @throws IllegalArgumentException if an identifier or token is blank
         * @throws ValidateException        if the lifetime is not positive
         */
        private Access {
            Assert.notBlank(userId, "Figma private user id must not be blank");
            Assert.notBlank(accessToken, "Figma private access token must not be blank");
            if (expiresIn <= 0L) {
                throw new ValidateException("Figma private access-token lifetime must be positive");
            }
        }

        /**
         * Returns a diagnostic representation without user or bearer material.
         *
         * @return redacted private token summary
         */
        @Override
        public String toString() {
            return "Access[userId=[REDACTED], accessToken=[REDACTED], expiresIn=" + expiresIn + Symbol.C_BRACKET_RIGHT;
        }

    }

}
