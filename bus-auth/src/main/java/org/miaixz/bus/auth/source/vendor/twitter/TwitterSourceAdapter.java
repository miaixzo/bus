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
package org.miaixz.bus.auth.source.vendor.twitter;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.Identity.Evidence;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.auth.source.SourceServices;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.*;
import org.miaixz.bus.auth.source.protocol.oauth2.client.AuthorizationClient;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.source.vendor.RedirectManager;
import org.miaixz.bus.auth.source.vendor.StandardAdapter;
import org.miaixz.bus.auth.source.vendor.VendorAdapter;
import org.miaixz.bus.auth.source.vendor.VendorManifest;
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
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements Twitter.com OAuth App login while publishing only standard OAuth authorization.
 * <p>
 * Mandatory S256 state material is generated and consumed by the shared browser lifecycle before the durable X user
 * identifier is emitted.
 * </p>
 *
 * @author Kimi Liu
 */
public class TwitterSourceAdapter implements VendorAdapter {

    /**
     * Trusted Twitter API authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://api.x.com";

    /**
     * Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Twitter manifest.
     */
    private final VendorManifest.Variant variant;

    /**
     * Validated externally loaded Twitter options.
     */
    private final TwitterOptions options;

    /**
     * Caller-owned execution services.
     */
    private final SourceServices services;

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
     * Creates one Source-bound Twitter.com OAuth App adapter.
     *
     * @param spaceId  Source space used to isolate state and credential resolution
     * @param sourceId Source identifier
     * @param manifest selected Twitter manifest
     * @param variant  exact selected default manifest
     * @param options  decoded externally loaded Twitter options
     * @param services capability-limited Source services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, manifest, callback, or authorization is inconsistent
     */
    public TwitterSourceAdapter(final String spaceId, final String sourceId, final TwitterManifest manifest,
            final VendorManifest.Variant variant, final TwitterOptions options, final SourceServices services) {
        final TwitterManifest selected = Assert.notNull(manifest, "Twitter manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Twitter Source id must not be blank");
        this.variant = Assert.notNull(variant, "Twitter manifest must not be null");
        this.options = Assert.notNull(options, "Twitter options must not be null");
        this.services = Assert.notNull(services, "Twitter execution services must not be null");
        if (!TwitterManifest.ID.equals(selected.vendor()) || !selected.variant(TwitterManifest.DEFAULT).equals(variant)
                || !TwitterManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !TwitterManifest.ID.equals(options.vendor()) || !TwitterManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("Twitter adapter requires the twitter/default OAuth 2.0 manifest");
        }
        this.redirectManager = RedirectManager.create(spaceId, sourceId, variant, options, services);
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
     * Reads one required non-blank string member.
     *
     * @param object decoded object
     * @param name   member name
     * @return non-blank value
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = string(object, name);
        if (value.isBlank()) {
            throw new ValidateException("Twitter requires a non-blank string member");
        }
        return value;
    }

    /**
     * Reads one required string member.
     *
     * @param object decoded object
     * @param name   member name
     * @return exact string value
     */
    private static String string(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException("Twitter requires a string member");
        }
        return text.value();
    }

    /**
     * Tests whether the response is the exact Twitter OAuth error shape.
     *
     * @param object decoded token response
     * @return whether exactly the two documented error members are present
     */
    private static boolean errorMembers(final JsonValue.ObjectValue object) {
        return object.values().size() == 2 && object.values().containsKey(OAuth2.Parameters.ERROR)
                && object.values().containsKey(OAuth2.Parameters.ERROR_DESCRIPTION);
    }

    /**
     * Classifies one Twitter OAuth error as an expected request rejection.
     *
     * @param error exact Twitter OAuth error
     * @return whether caller or authorization state caused the error
     */
    private static boolean rejectedError(final String error) {
        return "invalid_client".equals(error) || "invalid_grant".equals(error) || "invalid_request".equals(error)
                || "access_denied".equals(error);
    }

    /**
     * Tests one exact non-expiring or expiring Twitter token branch.
     *
     * @param object      decoded token response
     * @param refreshable whether the response declares a refresh-token member
     * @return whether the response contains exactly the selected branch members
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue object, final boolean refreshable) {
        final int size = refreshable ? 5 : 4;
        return object.values().size() == size && object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                && object.values().containsKey(OAuth2.Parameters.TOKEN_TYPE)
                && object.values().containsKey(OAuth2.Parameters.SCOPE)
                && object.values().containsKey(OAuth2.Parameters.EXPIRES_IN)
                && (!refreshable || object.values().containsKey(OAuth2.Parameters.REFRESH_TOKEN));
    }

    /**
     * Constructs the mutable RFC 7617 credential bytes used only for the token request.
     *
     * @param clientId Twitter client identifier
     * @param secret   caller-owned client secret
     * @return mutable Basic credential bytes
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
     * Reads one required positive integral number.
     *
     * @param object decoded object
     * @param name   member name
     * @return positive exact long
     */
    private static long requiredPositiveLong(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Twitter requires a numeric member");
        }
        try {
            final long result = number.value().longValueExact();
            if (result <= 0L) {
                throw new ValidateException("Twitter numeric member must be positive");
            }
            return result;
        } catch (ArithmeticException cause) {
            throw new ValidateException("Twitter numeric member must be integral", cause);
        }
    }

    /**
     * Classifies one HTTP status without copying its response body.
     *
     * @param status      exact status
     * @param description safe description
     * @param <T>         expected success type
     * @return rejected or failed outcome
     */
    private static <T> Outcome<T> status(final int status, final String description) {
        final Map<String, JsonValue> details = Map.of("status", number(status));
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
     * Creates one JSON integer.
     *
     * @param value integral value
     * @return JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Narrows one delegated outcome.
     *
     * @param stage        delegated stage
     * @param responseType response class
     * @param <S>          response type
     * @return narrowed outcome stage
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
     * Creates one completed outcome stage.
     *
     * @param outcome completed outcome
     * @param <T>     success type
     * @return completed stage
     */
    private static <T> CompletionStage<Outcome<T>> completed(final Outcome<T> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Creates an expected rejection.
     *
     * @param description safe description
     * @param <T>         success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates an operational failure without details.
     *
     * @param code        shared Bus error code
     * @param description safe description
     * @param <T>         success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final Errors code, final String description) {
        return failed(code, description, Map.of());
    }

    /**
     * Creates an operational failure with safe details.
     *
     * @param code        shared Bus error code
     * @param description safe description
     * @param details     non-sensitive details
     * @param <T>         success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(
            final Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.failed(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Erases one mutable sensitive byte array.
     *
     * @param value bytes or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) 0);
        }
    }

    /**
     * Returns the exact capability manifest frozen by the selected Twitter manifest.
     *
     * @return immutable Source authentication and OAuth authorization manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication and standard OAuth authorization.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Twitter-private response models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "Twitter capability must not be null");
        Assert.notNull(context, "Twitter invocation context must not be null");
        Assert.notNull(timeout, "Twitter invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Twitter capability is not declared by the selected manifest"));
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
        if (capability.equals(OAuth2ClientScheme.AUTHORIZATION) && request instanceof AuthorizationRequest authorization
                && valid(authorization)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return completed(rejected("Twitter request does not match the selected capability contract"));
    }

    /**
     * Builds the exact Twitter authorization redirect.
     *
     * @param initiation generated state and S256 challenge
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return exact redirect and state correlation
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(context, "Twitter authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Twitter authorization has no remaining timeout"));
        }
        try {
            final var challenge = initiation.codeChallenge().getOrNull();
            if (initiation.nonce().isPresent() || challenge == null || !PkceMethod.S256.equals(challenge.method())) {
                return completed(failed(ErrorCode._500, "Twitter browser flow lacks required S256 PKCE material"));
            }
            final var endpoint = variant.targets().resolve(options).authorization().getOrNull();
            final String location = endpoint.url().newBuilder().query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.SCOPE, String.join(Symbol.SPACE, options.scopes()))
                    .query(OAuth2.Parameters.STATE, initiation.state())
                    .query(OAuth2.Parameters.CODE_CHALLENGE, challenge.value())
                    .query(OAuth2.Parameters.CODE_CHALLENGE_METHOD, PkceMethod.S256.value()).build().toString();
            return completed(Outcome.succeeded(new RedirectManager.Prepared(location, initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("Twitter authorization request is invalid"));
        }
    }

    /**
     * Extracts the unique callback state.
     *
     * @param callback raw inbound callback
     * @return unique non-blank state
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Completes one correlated Twitter browser authorization.
     *
     * @param completion consumed callback and PKCE verifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified Twitter identity
     */
    private CompletionStage<Outcome<Identity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Twitter authorization callback is invalid"));
        }
        if (values.failed()) {
            return completed(rejected("Twitter authorization endpoint rejected the request"));
        }
        if (completion.codeVerifier().isEmpty()) {
            return completed(failed(ErrorCode._500, "Twitter callback lacks its required PKCE verifier"));
        }
        final String verifier = completion.codeVerifier().getOrNull().value();
        return Outcome.mapStage(
                () -> services.secretLoader()
                        .load(new SecretLoader.Request(services.entry(), options.credential()), context, timeout),
                loaded -> services.secretParser().parse(services.entry(), options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(
                            values.code(),
                            verifier,
                            success.value(),
                            timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Runs the private token and REST profile chain under one secret lease.
     *
     * @param code     one-time authorization code
     * @param verifier one-time RFC 7636 verifier
     * @param secret   owned Client Secret lease
     * @param timeout  shared end-to-end timeout
     * @return verified external identity
     */
    private CompletionStage<Outcome<Identity>> authenticate(
            final String code,
            final String verifier,
            final SecretLease secret,
            final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try (secret) {
                return switch (token(code, verifier, secret, timeout)) {
                    case Outcome.Succeeded<Access> success -> profile(success.value(), timeout);
                    case Outcome.Rejected<Access> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Access> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                };
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Twitter authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Sends the X OAuth 2.0 authorization-code form with PKCE and HTTP Basic client authentication.
     *
     * @param code     one-time authorization code
     * @param verifier one-time RFC 7636 verifier
     * @param secret   open Client Secret lease
     * @param timeout  shared end-to-end timeout
     * @return private access result
     */
    private Outcome<Access> token(
            final String code,
            final String verifier,
            final SecretLease secret,
            final Timeout timeout) {
        byte[] body = null;
        byte[] basicBytes = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Twitter token request has no remaining timeout");
            }
            body = formCodec.encode(
                    List.of(
                            new NameValue(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()),
                            new NameValue(OAuth2.Parameters.CODE,
                                    Assert.notBlank(code, "Twitter code must not be blank")),
                            new NameValue(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()),
                            new NameValue(OAuth2.Parameters.CODE_VERIFIER,
                                    Assert.notBlank(verifier, "Twitter verifier must not be blank"))));
            basicBytes = basic(options.clientId(), secret.material());
            final String authorization = "Basic " + Base64.encode(basicBytes);
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            try (Response response = FabricX.http(Protocol.OAUTH2, timeout, services.policies())
                    .url(endpoint.url().toString()).method(Http.Method.POST)
                    .header(Http.Header.AUTHORIZATION, authorization)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Twitter token request failed");
        } finally {
            clear(body);
            clear(basicBytes);
        }
    }

    /**
     * Strictly decodes one Twitter token response.
     *
     * @param response owned token response
     * @return private access result or classified failure
     */
    private Outcome<Access> token(final Response response) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (errorMembers(object)) {
                final String error = requiredString(object, OAuth2.Parameters.ERROR);
                requiredString(object, OAuth2.Parameters.ERROR_DESCRIPTION);
                final Map<String, JsonValue> details = Map
                        .of(Builder.OAUTH_ERROR, new JsonValue.StringValue(error), "status", number(response.code()));
                return rejectedError(error)
                        ? Outcome.rejected(
                                new Outcome.Failure(ErrorCode._400, "Twitter token endpoint rejected the request",
                                        new JsonValue.ObjectValue(details)))
                        : failed(ErrorCode._502, "Twitter token endpoint returned an unknown error", details);
            }
            if (response.code() != Http.Status.OK || object.values().containsKey(OAuth2.Parameters.ERROR)) {
                return status(response.code(), "Twitter token endpoint failed the request");
            }
            if (!object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                    || !object.values().containsKey(OAuth2.Parameters.TOKEN_TYPE)
                    || !object.values().containsKey(OAuth2.Parameters.SCOPE)) {
                throw new ValidateException("Twitter token response lacks required fields");
            }
            final boolean refreshable = object.values().containsKey(OAuth2.Parameters.REFRESH_TOKEN);
            if (!tokenMembers(object, refreshable) || !"bearer"
                    .equals(requiredString(object, OAuth2.Parameters.TOKEN_TYPE).toLowerCase(Locale.ROOT))) {
                throw new ValidateException("Twitter token response has an invalid branch");
            }
            final String scope = string(object, OAuth2.Parameters.SCOPE);
            if (!scope.isEmpty()) {
                for (String token : scope.split(Symbol.SPACE, -1)) {
                    Scope.parse(token);
                }
            }
            requiredPositiveLong(object, OAuth2.Parameters.EXPIRES_IN);
            if (refreshable) {
                requiredString(object, OAuth2.Parameters.REFRESH_TOKEN);
            }
            return Outcome.succeeded(new Access(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Twitter token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves and validates the versioned Twitter current-user resource.
     *
     * @param access  private access-token result
     * @param timeout shared end-to-end timeout
     * @return verified external identity
     */
    private Outcome<Identity> profile(final Access access, final Timeout timeout) {
        try {
            final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
            try (Response response = FabricX.http(Protocol.OAUTH2, timeout, services.policies())
                    .url(endpoint.url().toString()).method(Http.Method.GET)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + access.accessToken()).execute()) {
                if (response.code() != Http.Status.OK) {
                    return status(response.code(), "Twitter current-user endpoint rejected or failed the request");
                }
                final JsonValue.ObjectValue envelope = object(response);
                if (envelope.values().size() != 1
                        || !(envelope.values().get("data") instanceof JsonValue.ObjectValue object)) {
                    throw new ValidateException("Twitter profile requires the X API data envelope");
                }
                final String subject = requiredString(object, "id");
                requiredString(object, "name");
                requiredString(object, "username");
                final Map<String, JsonValue> attributes = new LinkedHashMap<>(object.values());
                attributes.remove("id");
                final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                        new Evidence.Claim("twitter_user_id", new JsonValue.StringValue(subject), AUTHORITY,
                                timeout.clock().now()));
                return Outcome.succeeded(
                        new Identity(sourceId, subject, new JsonValue.ObjectValue(attributes), List.of(evidence)));
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Twitter current-user endpoint returned an invalid response");
        }
    }

    /**
     * Validates one public standard authorization request.
     *
     * @param request standard OAuth authorization request
     * @return whether every registered value and S256 field is exact
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
     * Validates and indexes one exact Twitter callback branch.
     *
     * @param callback raw inbound callback
     * @return typed exact callback branch
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Twitter callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Twitter callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        String error = null;
        String errorDescription = null;
        String errorUri = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert.notBlank(parameter.value(), "Twitter callback value must not be blank");
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code, value);
                case OAuth2.Parameters.STATE -> state = unique(state, value);
                case OAuth2.Parameters.ERROR -> error = unique(error, value);
                case OAuth2.Parameters.ERROR_DESCRIPTION -> errorDescription = unique(errorDescription, value);
                case OAuth2.Parameters.ERROR_URI -> errorUri = unique(errorUri, value);
                default -> throw new ValidateException("Twitter callback contains an unsupported parameter");
            }
        }
        return new CallbackWire(code, state, error, errorDescription, errorUri);
    }

    /**
     * Accepts one callback value only once.
     *
     * @param current previous value, or {@code null}
     * @param value   newly decoded non-blank value
     * @return newly decoded value
     */
    private String unique(final String current, final String value) {
        if (current != null) {
            throw new ValidateException("Twitter callback names must be unique");
        }
        return value;
    }

    /**
     * Decodes one bounded Twitter JSON object.
     *
     * @param response response whose body remains open
     * @return strict implementation-neutral JSON object
     */
    private JsonValue.ObjectValue object(final Response response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Twitter response must use application/json");
        }
        final JsonValue value = JsonKit.readValue(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES), Normal._64, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Twitter response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries one exact Twitter authorization callback branch.
     *
     * @param code             authorization code
     * @param state            browser correlation value
     * @param error            OAuth error
     * @param errorDescription OAuth error description
     * @param errorUri         OAuth error documentation URI
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state, String error, String errorDescription, String errorUri) {

        /**
         * Validates one exact success or OAuth error callback.
         */
        private CallbackWire {
            Assert.notBlank(state, "Twitter callback state must not be blank");
            final boolean success = code != null && error == null && errorDescription == null && errorUri == null;
            final boolean failure = code == null && error != null && errorDescription != null && errorUri != null;
            if (!success && !failure) {
                throw new ValidateException("Twitter callback has an invalid branch");
            }
        }

        /**
         * Reports whether the callback carries an OAuth error.
         *
         * @return {@code true} for an error callback
         */
        private boolean failed() {
            return error != null;
        }

    }

    /**
     * Carries the private access token used by the immediate REST request.
     *
     * @param accessToken sensitive bearer token
     * @author Kimi Liu
     */
    private record Access(String accessToken) {

        /**
         * Validates one private token value.
         */
        private Access {
            Assert.notBlank(accessToken, "Twitter private access token must not be blank");
        }

        /**
         * Returns a redacted diagnostic value.
         *
         * @return redacted token summary
         */
        @Override
        public String toString() {
            return "Access[accessToken=[REDACTED]]";
        }

    }

}
