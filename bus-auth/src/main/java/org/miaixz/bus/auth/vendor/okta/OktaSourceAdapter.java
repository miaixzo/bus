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
package org.miaixz.bus.auth.vendor.okta;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.guard.IssuerValidator;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.*;
import org.miaixz.bus.auth.protocol.oauth2.codec.*;
import org.miaixz.bus.auth.protocol.oidc.*;
import org.miaixz.bus.auth.protocol.oidc.client.*;
import org.miaixz.bus.auth.protocol.oidc.codec.*;
import org.miaixz.bus.auth.shared.jose.*;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.jwt.JwtVerifier;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements Okta Source authentication while preserving the standard OpenID Connect public surface.
 * <p>
 * Public OIA, OIT, OIR, OID, OIJ, and OIU operations are delegated unchanged to the shared standard client. The
 * application-level Source authentication path adds one-time state and nonce correlation, verifies the returned RS256
 * ID Token against the issuer-bound Okta JWK Set, requires UserInfo subject equality, and emits only a framework
 * {@link ExternalIdentity}.
 * </p>
 *
 * @author Kimi Liu
 */
public final class OktaSourceAdapter implements VendorAdapter {

    /**
     * Registered Source identifier copied into verified external identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Okta variant manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Okta registration options.
     */
    private final OktaOptions options;

    /**
     * Caller-owned runtime dependencies and security services.
     */
    private final DriverServices services;

    /**
     * Resolved exact issuer shared by callback, Discovery, and ID Token validation.
     */
    private final String issuer;

    /**
     * Shared standard OIDC operation adapter.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time browser state and nonce coordinator.
     */
    private final RedirectManager redirectManager;

    /**
     * Strict standard OAuth authorization-response decoder.
     */
    private final AuthorizationResponseDecoder callbackDecoder;

    /**
     * Protected-header-aware JWK selector.
     */
    private final JwkSelector jwkSelector;

    /**
     * Profile-scoped compact JWS parser and verifier.
     */
    private final JwsService jwsService;

    /**
     * Complete OpenID Connect ID Token verification service.
     */
    private final IdTokenVerifier idTokenVerifier;

    /**
     * Strict standard JWK Set response codec used by the issuer-bound key operation.
     */
    private final JwkSetCodec jwkSetCodec;

    /**
     * Creates one Source-bound Okta adapter from the frozen default manifest.
     *
     * @param namespaceId registration namespace isolating browser and credential state
     * @param sourceId    registered Source identifier
     * @param manifest    selected Okta manifest
     * @param variant     selected default variant manifest
     * @param options     decoded externally loaded Okta options
     * @param services    caller-owned runtime dependencies
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if profile, manifest, options, or security baseline do not match the frozen
     *                                  variant
     */
    public OktaSourceAdapter(final String namespaceId, final String sourceId, final OktaManifest manifest,
            final VariantManifest.Variant variant, final OktaOptions options, final DriverServices services) {
        Assert.notNull(manifest, "Okta manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Okta Source id must not be blank");
        this.variant = Assert.notNull(variant, "Okta manifest must not be null");
        this.options = Assert.notNull(options, "Okta options must not be null");
        this.services = Assert.notNull(services, "Okta execution services must not be null");
        if (!OktaManifest.ID.equals(manifest.vendor()) || !manifest.variant(OktaManifest.DEFAULT).equals(variant)
                || !OktaManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OIDC
                || !OktaManifest.ID.equals(options.vendor()) || !OktaManifest.DEFAULT.equals(options.variant())) {
            throw new ValidateException("Okta adapter requires the okta/default OIDC manifest");
        }
        if (!services.securityBaseline().require(Protocol.OIDC).algorithms().contains(JwaAlgorithm.RS256.name())) {
            throw new ValidateException("Okta RS256 is not enabled by the OIDC security baseline");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        final var targets = variant.targets().resolve(options);
        final String authorizationEndpoint = targets.authorization().getOrNull().url().toString();
        if (!authorizationEndpoint.endsWith("/v1/authorize")) {
            throw new ValidateException("Okta authorization target cannot determine the registered issuer");
        }
        this.issuer = authorizationEndpoint.substring(0, authorizationEndpoint.length() - "/v1/authorize".length());
        final OAuth2ClientOptions oauthSettings = new OAuth2ClientOptions(targets.authorization(), targets.token(),
                Optional.empty(), targets.revocation(), Optional.empty(), Optional.empty(), Optional.of(issuer),
                options.clientId(), Set.of(options.redirectUri().getOrNull()),
                Endpoint.Authentication.CLIENT_SECRET_BASIC, Optional.of(options.credential()), false, false);
        final OAuth2Client oauthClient = new OAuth2Client(
                new AuthorizationClient(oauthSettings,
                        new AuthorizationRequestEncoder(targets.authorization().getOrNull())),
                new TokenClient(oauthSettings, services, new TokenRequestEncoder(),
                        new TokenResponseDecoder(services.jsonProvider())),
                Optional.empty(),
                Optional.of(new RevocationClient(oauthSettings, services, new RevocationRequestEncoder())),
                Optional.empty(), Optional.empty());
        final OpenIdClientOptions openIdSettings = new OpenIdClientOptions(oauthSettings, targets.discovery(),
                targets.userInfo(), targets.jwks(), targets.endSession(), Set.of(JwaAlgorithm.RS256));
        final OpenIdClient openIdClient = new OpenIdClient(oauthClient,
                new AuthenticationRequestEncoder(services.jsonProvider()),
                new DiscoveryClient(openIdSettings, services, new OpenIdProviderMetadataCodec(services.jsonProvider())),
                Optional.of(new UserInfoClient(openIdSettings, services, new UserInfoCodec(services.jsonProvider()))),
                Optional.empty());
        this.jwkSetCodec = new JwkSetCodec(services.jsonProvider());
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager), List.of(
                new StandardAdapter.Binding<>(OpenIdClientScheme.AUTHENTICATION, openIdClient::authorize),
                new StandardAdapter.Binding<>(OpenIdClientScheme.TOKEN, openIdClient::token),
                new StandardAdapter.Binding<>(OpenIdClientScheme.REVOCATION, openIdClient::revoke),
                new StandardAdapter.Binding<>(OpenIdClientScheme.DISCOVERY,
                        (ignored, context, timeout) -> openIdClient.discover(context, timeout)),
                new StandardAdapter.Binding<>(OpenIdClientScheme.JWK_SET, (ignored, context, timeout) -> jwks(timeout)),
                new StandardAdapter.Binding<>(OpenIdClientScheme.USERINFO, openIdClient::userInfo)));
        this.callbackDecoder = new AuthorizationResponseDecoder();
        this.jwkSelector = new JwkSelector();
        this.jwsService = new JwsService(services.jsonProvider(), services.securityBaseline().algorithmGuard(),
                Set.of(JwaAlgorithm.RS256.name()));
        final JweService dormantJweService = new JweService(services.jsonProvider(),
                services.securityBaseline().algorithmGuard(), Set.of(JwaAlgorithm.RSA_OAEP_256.name()),
                Set.of(JwaAlgorithm.A256GCM.name()));
        final IdTokenCodec idTokenCodec = new IdTokenCodec(
                new JwtVerifier(services.jsonProvider(), jwsService, dormantJweService));
        this.idTokenVerifier = new IdTokenVerifier(idTokenCodec, new IssuerValidator(),
                services.securityBaseline().timeGuard(Protocol.OIDC, services.fabricContext().clock()));
    }

    /**
     * Maps a standard authorization error without copying description or URI into diagnostic details.
     *
     * @param response decoded standard OAuth error response
     * @return safe rejected identity outcome
     */
    private static Outcome<ExternalIdentity> authorizationError(final AuthorizationErrorResponse response) {
        final Map<String, JsonValue> details = Map
                .of(Builder.OAUTH_ERROR, new JsonValue.StringValue(response.error().value()));
        return Outcome.rejected(
                new Outcome.Failure(ErrorCode._400, "Okta authorization endpoint returned a standard error",
                        new JsonValue.ObjectValue(details)));
    }

    /**
     * Converts one selected public RSA JWK through the existing Bus cryptographic primitive.
     *
     * @param jwk selected issuer key
     * @return JCA RSA public verification key
     * @throws ValidateException if required public RSA members are absent or invalid
     */
    private static PublicKey rsaPublicKey(final Jwk jwk) {
        if (!"RSA".equals(jwk.keyType()) || jwk.hasPrivateMaterial()) {
            throw new ValidateException("Okta ID Token requires a public RSA JWK");
        }
        final byte[] modulus = binary(jwk, "n");
        final byte[] exponent = binary(jwk, "e");
        try {
            return Keeper.getRSAPublicKey(new BigInteger(1, modulus), new BigInteger(1, exponent));
        } finally {
            Arrays.fill(modulus, (byte) 0);
            Arrays.fill(exponent, (byte) 0);
        }
    }

    /**
     * Decodes one required unpadded Base64URL JWK integer member.
     *
     * @param jwk  source public JWK
     * @param name exact registered member name
     * @return newly allocated unsigned integer bytes
     * @throws ValidateException if the member is absent, mistyped, or empty
     */
    private static byte[] binary(final Jwk jwk, final String name) {
        final JsonValue value = jwk.parameter(name)
                .orElseThrow(() -> new ValidateException("Okta RSA JWK lacks required member " + name));
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Okta RSA JWK member must be a Base64URL string");
        }
        final byte[] decoded = Base64.decode(string.value());
        if (decoded == null || decoded.length == 0) {
            throw new ValidateException("Okta RSA JWK integer must not be empty");
        }
        return decoded;
    }

    /**
     * Narrows a delegated outcome through the capability's declared response class.
     *
     * @param stage        delegated outcome stage
     * @param responseType exact successful response type
     * @param <S>          expected successful response type
     * @return type-safe delegated stage
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
     * Creates an immutable empty JSON object for standard extension fields.
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
     * Creates a safe expected request or protocol rejection.
     *
     * @param description non-sensitive rejection description
     * @param <T>         expected successful value type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, emptyObject()));
    }

    /**
     * Creates a safe operational failure using a shared Bus error code.
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
     * Returns the exact frozen Okta capability manifest.
     *
     * @return immutable Source authentication and standard OIDC capabilities
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication locally and delegates every public OIDC operation unchanged.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source authentication request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed operation outcome without a platform-specific public model
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Okta capability must not be null");
        Assert.notNull(context, "Okta invocation context must not be null");
        Assert.notNull(timeout, "Okta invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Okta capability is not declared"));
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
        return completed(rejected("Okta capability request is invalid"));
    }

    /**
     * Builds the standard OIDC Authentication Request around generated state and nonce values.
     *
     * @param initiation generated one-time browser material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @return standard-client-produced authorization location bound to state
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        final String nonce = initiation.nonce().getOrNull();
        if (nonce == null || initiation.codeChallenge().isPresent()) {
            return completed(failed(ErrorCode._500, "Okta browser flow generated invalid OIDC correlation material"));
        }
        try {
            final AuthorizationRequest authorization = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                    options.redirectUri(), Optional.of(new Scope(effectiveScopes())), Optional.of(initiation.state()),
                    Optional.empty(), Optional.empty(), emptyObject());
            final AuthenticationRequest authentication = new AuthenticationRequest(authorization, Optional.of(nonce),
                    Optional.empty(), Optional.empty(), Optional.empty(), List.of(), Optional.empty(), Optional.empty(),
                    List.of(), Optional.empty(), Optional.empty(), emptyObject());
            return standardAdapter.invoke(OpenIdClientScheme.AUTHENTICATION, authentication, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<UnoUrl> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<UnoUrl> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<UnoUrl> failed -> Outcome.failed(failed.failure());
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Okta Authentication Request is invalid"));
        }
    }

    /**
     * Extracts the unique standard callback state after validating callback ownership and syntax.
     *
     * @param callback raw callback captured by the external Web project
     * @return required callback state
     * @throws ValidateException if transport, URI, branch, or state is invalid
     */
    private String state(final Callback.Inbound callback) {
        return switch (decode(callback)) {
            case AuthorizationResponseDecoder.Success success -> success.response().state()
                    .orElseThrow(() -> new ValidateException("Okta authorization success requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state()
                    .orElseThrow(() -> new ValidateException("Okta authorization error requires state"));
        };
    }

    /**
     * Completes the correlated code flow and produces a cryptographically verified external identity.
     *
     * @param completion consumed callback and one-time nonce material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @return verified Okta external identity or a closed standard failure
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = decode(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Okta authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            return completed(authorizationError(error.response()));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        if (response.issuer().isEmpty() || !issuer.equals(response.issuer().getOrNull())
                || completion.correlation().nonce().isEmpty() || completion.codeVerifier().isPresent()) {
            return completed(rejected("Okta authorization response does not satisfy the registered OIDC manifest"));
        }
        final TokenRequest tokenRequest = new TokenRequest(
                new AuthorizationCodeGrant(response.code(), options.redirectUri(), Optional.empty(), Optional.empty()),
                emptyObject());
        return standardAdapter.invoke(OpenIdClientScheme.TOKEN, tokenRequest, context, timeout)
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<TokenEndpointResponse> success -> success
                            .value() instanceof OpenIdTokenResponse openId
                                    ? verify(openId, response, completion, context, timeout)
                                    : completed(rejected("Okta authorization-code response omitted the ID Token"));
                    case Outcome.Rejected<TokenEndpointResponse> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<TokenEndpointResponse> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Retrieves the current issuer-bound JWK Set before selecting and validating the ID Token.
     *
     * @param token      standard token response
     * @param response   correlated authorization response
     * @param completion consumed nonce and state material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @return verified external identity stage
     */
    private CompletionStage<Outcome<ExternalIdentity>> verify(
            final OpenIdTokenResponse token,
            final AuthorizationCodeResponse response,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        if (!TokenType.BEARER.equals(token.tokenType())) {
            return completed(rejected("Okta token response requires Bearer and id_token"));
        }
        return standardAdapter.invoke(OpenIdClientScheme.JWK_SET, null, context, timeout)
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<JwkSet> success -> verify(
                            token,
                            token.idToken().compact(),
                            success.value(),
                            response,
                            completion,
                            context,
                            timeout);
                    case Outcome.Rejected<JwkSet> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<JwkSet> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Selects the protected-header-bound RSA key and applies complete OIDC ID Token verification.
     *
     * @param token      standard token response
     * @param compact    sensitive compact ID Token
     * @param keys       current issuer public JWK Set
     * @param response   correlated authorization response
     * @param completion consumed nonce and state material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end time budget
     * @return identity stage after ID Token and UserInfo subject verification
     */
    private CompletionStage<Outcome<ExternalIdentity>> verify(
            final OpenIdTokenResponse token,
            final String compact,
            final JwkSet keys,
            final AuthorizationCodeResponse response,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final PublicKey key;
        try {
            final JwsService.Jws parsed = jwsService.parseCompact(compact, Set.of());
            final JoseHeader header = parsed.signatures().get(0).header();
            if (!JwaAlgorithm.RS256.name().equals(header.algorithm())) {
                throw new ValidateException("Okta ID Token must use RS256");
            }
            final String keyId = header.keyId()
                    .orElseThrow(() -> new ValidateException("Okta ID Token requires a protected kid"));
            final Jwk selected = jwkSelector.requireUnique(
                    keys,
                    new JwkSelector.Selection(header.keyId(), JwaAlgorithm.RS256.name(), JwaAlgorithm.Kind.SIGNATURE,
                            Optional.of(Builder.SIGNATURE), Optional.of(Builder.VERIFY), Optional.of("RSA")));
            if (selected.keyId().filter(keyId::equals).isEmpty()
                    || selected.algorithm().filter(JwaAlgorithm.RS256.name()::equals).isEmpty()
                    || selected.publicKeyUse().filter(Builder.SIGNATURE::equals).isEmpty()) {
                throw new ValidateException("Okta JWK must bind kid, alg=RS256, and use=sig");
            }
            key = rsaPublicKey(selected);
        } catch (RuntimeException cause) {
            return completed(rejected("Okta ID Token key selection failed"));
        }
        final IdTokenVerifier.Request verification = new IdTokenVerifier.Request(new IdToken(compact),
                new JwtVerifier.Signed(key, Set.of()), issuer, options.clientId(),
                completion.correlation().nonce().getOrNull(), Optional.empty(), Optional.of(token.accessToken()),
                Optional.of(response.code()), Optional.of(completion.correlation().state()));
        return idTokenVerifier.verify(verification, context, timeout).thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<IdTokenClaims> success -> userInfo(token, success.value(), context, timeout);
            case Outcome.Rejected<IdTokenClaims> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<IdTokenClaims> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Retrieves standard UserInfo and binds its subject to the verified ID Token subject.
     *
     * @param token   standard token response containing the bearer access token
     * @param claims  cryptographically verified ID Token claims
     * @param context immutable invocation context
     * @param timeout shared end-to-end time budget
     * @return verified external identity stage
     */
    private CompletionStage<Outcome<ExternalIdentity>> userInfo(
            final OpenIdTokenResponse token,
            final IdTokenClaims claims,
            final Context context,
            final Timeout.Budget timeout) {
        return standardAdapter
                .invoke(OpenIdClientScheme.USERINFO, new UserInfoRequest(token.accessToken()), context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<UserInfoResponse> success -> identity(success.value(), claims, timeout);
                    case Outcome.Rejected<UserInfoResponse> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<UserInfoResponse> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Maps standard claims only after enforcing the OpenID Connect UserInfo subject rule.
     *
     * @param userInfo standard UserInfo response
     * @param claims   verified ID Token claims
     * @param timeout  shared clock used for evidence timestamping
     * @return verified external identity or a safe rejection
     */
    private Outcome<ExternalIdentity> identity(
            final UserInfoResponse userInfo,
            final IdTokenClaims claims,
            final Timeout.Budget timeout) {
        if (!claims.subject().equals(userInfo.subject())) {
            return rejected("Okta UserInfo subject does not match the verified ID Token");
        }
        final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                new Evidence.Claim(JwtClaims.SUBJECT, new JsonValue.StringValue(claims.subject()), issuer,
                        timeout.clock().now()));
        return Outcome
                .succeeded(new ExternalIdentity(sourceId, claims.subject(), userInfo.claims(), List.of(evidence)));
    }

    /**
     * Validates exact callback ownership before applying the standard response decoder.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     * @throws IllegalArgumentException if callback is {@code null}
     * @throws ValidateException        if callback URI differs from the registered lexical value
     */
    private AuthorizationResponseDecoder.Decoded decode(final Callback.Inbound callback) {
        final Callback.Inbound inbound = Assert.notNull(callback, "Okta callback must not be null");
        if (!options.redirectUri().getOrNull().equals(inbound.requestUri()) || inbound.method() != Http.Method.GET) {
            throw new ValidateException("Okta callback URI or method is invalid");
        }
        return callbackDecoder.decode(inbound);
    }

    /**
     * Retrieves and decodes the issuer-bound standard Okta JWK Set.
     *
     * @param timeout shared end-to-end time budget
     * @return current standard JWK Set or a safe operational failure
     */
    private CompletionStage<Outcome<JwkSet>> jwks(final Timeout.Budget timeout) {
        return CompletableFuture.supplyAsync(() -> {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Okta JWK Set request has no remaining time budget");
            }
            try {
                final String endpoint = variant.targets().resolve(options).jwks().getOrNull().url().toString();
                try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                        .timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.OIDC).addressPolicy()).execute()) {
                    return Outcome.succeeded(jwkSetCodec.decode(response));
                }
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Okta JWK Set endpoint request failed");
            }
        }, services.executor());
    }

    /**
     * Returns explicit Source scopes or the immutable manifest defaults.
     *
     * @return ordered effective Okta scopes
     */
    private List<String> effectiveScopes() {
        return options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
    }

}
