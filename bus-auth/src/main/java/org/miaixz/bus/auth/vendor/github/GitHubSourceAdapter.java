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
package org.miaixz.bus.auth.vendor.github;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.Parameter;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationRequest;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.ResponseType;
import org.miaixz.bus.auth.protocol.oauth2.Scope;
import org.miaixz.bus.auth.protocol.oauth2.client.AuthorizationClient;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
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
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements GitHub.com OAuth App login while publishing only standard OAuth authorization.
 * <p>
 * GitHub's grant-type omission, comma-delimited token scope, optional expiring-token pair, and versioned REST user
 * representation remain private Source behavior. Mandatory S256 state material is generated and consumed by the shared
 * browser lifecycle before the durable numeric user identifier is emitted.
 * </p>
 *
 * @author Kimi Liu
 */
public final class GitHubSourceAdapter implements VendorAdapter {

    /**
     * Trusted GitHub API authority recorded in federated identity evidence.
     */
    private static final String AUTHORITY = "https://api.github.com";

    /**
     * Current GitHub REST representation requested by the frozen vector.
     */
    private static final String REST_MEDIA = "application/vnd.github+json";

    /**
     * Current GitHub REST API version requested by the frozen vector.
     */
    private static final String REST_VERSION = "2026-03-10";

    /**
     * Maximum accepted GitHub JSON response size.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum accepted GitHub JSON nesting depth.
     */
    private static final int MAXIMUM_JSON_DEPTH = 64;

    /**
     * Registered Source identifier copied into verified identities.
     */
    private final String sourceId;

    /**
     * Selected immutable GitHub manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded GitHub options.
     */
    private final GitHubOptions options;

    /**
     * Caller-owned execution services.
     */
    private final ExecutionServices services;

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
     * Creates one Source-bound GitHub.com OAuth App adapter.
     *
     * @param namespaceId registration namespace used to isolate state and credential resolution
     * @param sourceId    registered Source identifier
     * @param manifest    selected GitHub manifest
     * @param variant     exact selected default manifest
     * @param options     decoded externally loaded GitHub options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, manifest, callback, or authorization is inconsistent
     */
    public GitHubSourceAdapter(final String namespaceId, final String sourceId, final GitHubManifest manifest,
            final VariantManifest.Variant variant, final GitHubOptions options, final ExecutionServices services) {
        final GitHubManifest selected = Assert.notNull(manifest, "GitHub manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "GitHub Source id must not be blank");
        this.variant = Assert.notNull(variant, "GitHub manifest must not be null");
        this.options = Assert.notNull(options, "GitHub options must not be null");
        this.services = Assert.notNull(services, "GitHub execution services must not be null");
        if (!GitHubManifest.ID.equals(selected.vendor()) || !selected.variant(GitHubManifest.DEFAULT).equals(variant)
                || !GitHubManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OAUTH2
                || !GitHubManifest.ID.equals(options.vendor()) || !GitHubManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("GitHub adapter requires the github/default OAuth 2.0 manifest");
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
     * Reads one required non-blank string member.
     *
     * @param object decoded object
     * @param name   member name
     * @return non-blank value
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = string(object, name);
        if (value.isBlank()) {
            throw new ValidateException("GitHub requires a non-blank string member");
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
            throw new ValidateException("GitHub requires a string member");
        }
        return text.value();
    }

    /**
     * Tests whether the response is the exact GitHub OAuth error shape.
     *
     * @param object decoded token response
     * @return whether exactly the three documented error members are present
     */
    private static boolean errorMembers(final JsonValue.ObjectValue object) {
        return object.values().size() == 3 && object.values().containsKey(OAuth2.Parameters.ERROR)
                && object.values().containsKey(OAuth2.Parameters.ERROR_DESCRIPTION)
                && object.values().containsKey(OAuth2.Parameters.ERROR_URI);
    }

    /**
     * Classifies one GitHub OAuth error as an expected request rejection.
     *
     * @param error exact GitHub OAuth error
     * @return whether caller or authorization state caused the error
     */
    private static boolean rejectedError(final String error) {
        return "incorrect_client_credentials".equals(error) || "redirect_uri_mismatch".equals(error)
                || "bad_verification_code".equals(error) || "unverified_user_email".equals(error)
                || "bad_refresh_token".equals(error) || "access_denied".equals(error);
    }

    /**
     * Tests one exact non-expiring or expiring GitHub token branch.
     *
     * @param object   decoded token response
     * @param expiring whether the response declares expiry or refresh members
     * @return whether the response contains exactly the selected branch members
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue object, final boolean expiring) {
        final int size = expiring ? 6 : 3;
        return object.values().size() == size && object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                && object.values().containsKey(OAuth2.Parameters.TOKEN_TYPE)
                && object.values().containsKey(OAuth2.Parameters.SCOPE)
                && (!expiring || object.values().containsKey(OAuth2.Parameters.EXPIRES_IN)
                        && object.values().containsKey(OAuth2.Parameters.REFRESH_TOKEN)
                        && object.values().containsKey("refresh_token_expires_in"));
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
            throw new ValidateException("GitHub requires a numeric member");
        }
        try {
            final long result = number.value().longValueExact();
            if (result <= 0L) {
                throw new ValidateException("GitHub numeric member must be positive");
            }
            return result;
        } catch (ArithmeticException cause) {
            throw new ValidateException("GitHub numeric member must be integral", cause);
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
     * Returns the exact capability manifest frozen by the selected GitHub manifest.
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
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing GitHub-private response models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "GitHub capability must not be null");
        Assert.notNull(context, "GitHub invocation context must not be null");
        Assert.notNull(timeout, "GitHub invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("GitHub capability is not declared by the selected manifest"));
        }
        if (capability.key().equals(SourceAuthentication.INITIATE.key())
                && request instanceof SourceAuthenticationRequest.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (capability.key().equals(SourceAuthentication.COMPLETE.key())
                && request instanceof SourceAuthenticationRequest.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::identity, context, timeout),
                    capability.responseType());
        }
        if (capability.equals(OAuth2ClientScheme.AUTHORIZATION) && request instanceof AuthorizationRequest authorization
                && valid(authorization)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return completed(rejected("GitHub request does not match the selected capability contract"));
    }

    /**
     * Builds the exact GitHub authorization redirect.
     *
     * @param initiation generated state and S256 challenge
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return exact redirect and state correlation
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "GitHub authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "GitHub authorization has no remaining time budget"));
        }
        try {
            final var challenge = initiation.codeChallenge().getOrNull();
            if (initiation.nonce().isPresent() || challenge == null || !PkceMethod.S256.equals(challenge.method())) {
                return completed(failed(ErrorCode._500, "GitHub browser flow lacks required S256 PKCE material"));
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
            return completed(rejected("GitHub authorization request is invalid"));
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
     * Completes one correlated GitHub browser authorization.
     *
     * @param completion consumed callback and PKCE verifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return verified GitHub identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("GitHub authorization callback is invalid"));
        }
        if (values.failed()) {
            return completed(rejected("GitHub authorization endpoint rejected the request"));
        }
        if (completion.codeVerifier().isEmpty()) {
            return completed(failed(ErrorCode._500, "GitHub callback lacks its required PKCE verifier"));
        }
        final String verifier = completion.codeVerifier().getOrNull().value();
        return org.miaixz.bus.auth.runtime.LoadResult
                .parse(
                        services.secretLoader().load(options.credential(), context, timeout),
                        loaded -> services.secretParser().parse(options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(
                            values.code(),
                            verifier,
                            success.value(),
                            timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Runs the private token and REST profile chain under one secret lease.
     *
     * @param code     one-time authorization code
     * @param verifier one-time RFC 7636 verifier
     * @param secret   owned Client Secret lease
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
                return failed(ErrorCode._502, "GitHub authentication completion failed");
            }
        }, services.executor());
    }

    /**
     * Sends GitHub's exact authorization-code form without an invented grant type.
     *
     * @param code     one-time authorization code
     * @param verifier one-time RFC 7636 verifier
     * @param secret   open Client Secret lease
     * @param timeout  shared end-to-end budget
     * @return private access result
     */
    private Outcome<Access> token(
            final String code,
            final String verifier,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "GitHub token request has no remaining time budget");
            }
            body = formCodec.encode(
                    List.of(
                            new Parameter(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                            new Parameter(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material())),
                            new Parameter(OAuth2.Parameters.CODE,
                                    Assert.notBlank(code, "GitHub code must not be blank")),
                            new Parameter(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull()),
                            new Parameter(OAuth2.Parameters.CODE_VERIFIER,
                                    Assert.notBlank(verifier, "GitHub verifier must not be blank"))));
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy())
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "GitHub token request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Strictly decodes one GitHub token response.
     *
     * @param response owned token response
     * @return private access result or classified failure
     */
    private Outcome<Access> token(final HttpResponse response) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (errorMembers(object)) {
                final String error = requiredString(object, OAuth2.Parameters.ERROR);
                requiredString(object, OAuth2.Parameters.ERROR_DESCRIPTION);
                requiredString(object, OAuth2.Parameters.ERROR_URI);
                final Map<String, JsonValue> details = Map
                        .of("oauth_error", new JsonValue.StringValue(error), "status", number(response.code()));
                return rejectedError(error)
                        ? Outcome.rejected(
                                new Outcome.Failure(ErrorCode._400, "GitHub token endpoint rejected the request",
                                        new JsonValue.ObjectValue(details)))
                        : failed(ErrorCode._502, "GitHub token endpoint returned an unknown error", details);
            }
            if (response.code() != Http.Status.OK || object.values().containsKey(OAuth2.Parameters.ERROR)) {
                return status(response.code(), "GitHub token endpoint failed the request");
            }
            if (!object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                    || !object.values().containsKey(OAuth2.Parameters.TOKEN_TYPE)
                    || !object.values().containsKey(OAuth2.Parameters.SCOPE)) {
                throw new ValidateException("GitHub token response lacks required fields");
            }
            final boolean expiring = object.values().containsKey(OAuth2.Parameters.EXPIRES_IN)
                    || object.values().containsKey(OAuth2.Parameters.REFRESH_TOKEN)
                    || object.values().containsKey("refresh_token_expires_in");
            if (!tokenMembers(object, expiring) || !"bearer"
                    .equals(requiredString(object, OAuth2.Parameters.TOKEN_TYPE).toLowerCase(Locale.ROOT))) {
                throw new ValidateException("GitHub token response has an invalid branch");
            }
            final String scope = string(object, OAuth2.Parameters.SCOPE);
            if (!scope.isEmpty()) {
                for (String token : scope.split(Symbol.COMMA, -1)) {
                    Scope.parse(token);
                }
            }
            if (expiring) {
                requiredPositiveLong(object, OAuth2.Parameters.EXPIRES_IN);
                requiredString(object, OAuth2.Parameters.REFRESH_TOKEN);
                requiredPositiveLong(object, "refresh_token_expires_in");
            }
            return Outcome.succeeded(new Access(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "GitHub token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves and validates the versioned GitHub current-user resource.
     *
     * @param access  private access-token result
     * @param timeout shared end-to-end budget
     * @return verified external identity
     */
    private Outcome<ExternalIdentity> profile(final Access access, final Timeout.Budget timeout) {
        try {
            final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.GET).header(Http.Header.ACCEPT, REST_MEDIA)
                    .header(Http.Header.AUTHORIZATION, Http.Auth.BEARER_PREFIX + access.accessToken())
                    .header("X-GitHub-Api-Version", REST_VERSION).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OAUTH2).addressPolicy()).execute()) {
                if (response.code() != Http.Status.OK) {
                    return status(response.code(), "GitHub current-user endpoint rejected or failed the request");
                }
                final JsonValue.ObjectValue object = object(response);
                final long identifier = requiredPositiveLong(object, "id");
                requiredString(object, "login");
                requiredString(object, "node_id");
                requiredString(object, "avatar_url");
                requiredString(object, "url");
                requiredString(object, "html_url");
                requiredString(object, "type");
                requiredString(object, "created_at");
                requiredString(object, "updated_at");
                if (!(object.values().get("site_admin") instanceof JsonValue.BooleanValue)) {
                    throw new ValidateException("GitHub profile requires site_admin boolean");
                }
                final String subject = Long.toUnsignedString(identifier);
                final Map<String, JsonValue> attributes = new LinkedHashMap<>(object.values());
                attributes.remove("id");
                final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                        new Evidence.Claim("github_user_id", new JsonValue.StringValue(subject), AUTHORITY,
                                timeout.clock().now()));
                return Outcome.succeeded(
                        new ExternalIdentity(sourceId, subject, new JsonValue.ObjectValue(attributes),
                                List.of(evidence)));
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "GitHub current-user endpoint returned an invalid response");
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
     * Validates and indexes one exact GitHub callback branch.
     *
     * @param callback raw inbound callback
     * @return typed exact callback branch
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "GitHub callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("GitHub callback transport or target is invalid");
        }
        String code = null;
        String state = null;
        String error = null;
        String errorDescription = null;
        String errorUri = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert.notBlank(parameter.value(), "GitHub callback value must not be blank");
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code, value);
                case OAuth2.Parameters.STATE -> state = unique(state, value);
                case OAuth2.Parameters.ERROR -> error = unique(error, value);
                case OAuth2.Parameters.ERROR_DESCRIPTION -> errorDescription = unique(errorDescription, value);
                case OAuth2.Parameters.ERROR_URI -> errorUri = unique(errorUri, value);
                default -> throw new ValidateException("GitHub callback contains an unsupported parameter");
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
            throw new ValidateException("GitHub callback names must be unique");
        }
        return value;
    }

    /**
     * Decodes one bounded GitHub JSON object.
     *
     * @param response response whose body remains open
     * @return strict provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final HttpResponse response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("GitHub response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("GitHub response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries one exact GitHub authorization callback branch.
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
            Assert.notBlank(state, "GitHub callback state must not be blank");
            final boolean success = code != null && error == null && errorDescription == null && errorUri == null;
            final boolean failure = code == null && error != null && errorDescription != null && errorUri != null;
            if (!success && !failure) {
                throw new ValidateException("GitHub callback has an invalid branch");
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
            Assert.notBlank(accessToken, "GitHub private access token must not be blank");
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
