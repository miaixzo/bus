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
package org.miaixz.bus.auth.vendor.aliyun;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.guard.IssuerValidator;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.*;
import org.miaixz.bus.auth.protocol.oauth2.codec.*;
import org.miaixz.bus.auth.protocol.oidc.*;
import org.miaixz.bus.auth.protocol.oidc.client.*;
import org.miaixz.bus.auth.protocol.oidc.codec.*;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.shared.jose.*;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.jwt.JwtVerifier;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;

/**
 * Implements the frozen Alibaba Cloud OpenID Connect relying-party and Source authentication contract.
 * <p>
 * All Registry-visible protocol operations preserve their standard request and response types. Browser authentication
 * adds only the application-level correlation lifecycle needed to turn a verified ID Token and UserInfo response into
 * an {@link ExternalIdentity}; it never publishes a platform-specific token or profile model.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AliyunSourceAdapter implements VendorAdapter {

    /**
     * Trusted Alibaba Cloud OpenID Provider issuer.
     */
    private static final String ISSUER = "https://oauth.aliyun.com";

    /**
     * OIDC Discovery extension carrying the RFC 7009 revocation endpoint.
     */
    private static final String REVOCATION_ENDPOINT = "revocation_endpoint";

    /**
     * OIDC Discovery extension carrying the supported PKCE transformation methods.
     */
    private static final String CODE_CHALLENGE_METHODS = "code_challenge_methods_supported";

    /**
     * Registered Source identifier used for every produced external identity.
     */
    private final String sourceId;

    /**
     * Selected immutable Alibaba Cloud variant manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Alibaba Cloud Source options.
     */
    private final AliyunOptions options;

    /**
     * Caller-owned runtime dependencies and network resources.
     */
    private final ExecutionServices services;

    /**
     * Existing standard OIDC client operations used without wire adaptation.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time state, nonce, and PKCE lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Strict standard authorization response decoder.
     */
    private final AuthorizationResponseDecoder authorizationResponseDecoder;

    /**
     * Strict standard public JWK Set response codec.
     */
    private final JwkSetCodec jwkSetCodec;

    /**
     * Shared JWK rotation candidate selector.
     */
    private final JwkSelector jwkSelector;

    /**
     * Profile-scoped JWS parser used to read protected key-selection metadata before verification.
     */
    private final JwsService jwsService;

    /**
     * Complete OpenID Connect ID Token verification service.
     */
    private final IdTokenVerifier idTokenVerifier;

    /**
     * Exact case-sensitive issuer comparison primitive.
     */
    private final IssuerValidator issuerValidator;

    /**
     * Creates one Source-bound Alibaba Cloud adapter from an immutable Vendor manifest.
     *
     * @param namespaceId registration namespace used to isolate browser state and credentials
     * @param sourceId    registered Source identifier
     * @param manifest    selected Alibaba Cloud manifest
     * @param variant     selected default variant manifest
     * @param options     decoded externally loaded Alibaba Cloud options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a required collaborator is {@code null}
     * @throws ValidateException        if the supplied profile, manifest, options, or security baseline does not
     *                                  represent the frozen Alibaba Cloud OIDC variant
     */
    public AliyunSourceAdapter(final String namespaceId, final String sourceId, final AliyunManifest manifest,
            final VariantManifest.Variant variant, final AliyunOptions options, final ExecutionServices services) {
        Assert.notNull(manifest, "Alibaba Cloud manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Alibaba Cloud Source id must not be blank");
        this.variant = Assert.notNull(variant, "Alibaba Cloud manifest must not be null");
        this.options = Assert.notNull(options, "Alibaba Cloud options must not be null");
        this.services = Assert.notNull(services, "Alibaba Cloud execution services must not be null");
        if (!AliyunManifest.ID.equals(manifest.vendor()) || !manifest.variant(AliyunManifest.DEFAULT).equals(variant)
                || !AliyunManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OIDC
                || !options.vendor().equals(AliyunManifest.ID) || !options.variant().equals(AliyunManifest.DEFAULT)) {
            throw new ValidateException("Alibaba Cloud adapter requires the aliyun/default OIDC manifest");
        }
        final Set<String> baselineAlgorithms = services.securityBaseline().require(Protocol.OIDC).algorithms();
        if (!baselineAlgorithms.contains(JwaAlgorithm.RS256.name())) {
            throw new ValidateException("Alibaba Cloud RS256 is not enabled by the OIDC security baseline");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.standardAdapter = standardAdapter(variant, options, services, redirectManager);
        this.authorizationResponseDecoder = new AuthorizationResponseDecoder();
        this.jwkSetCodec = new JwkSetCodec(services.jsonProvider());
        this.jwkSelector = new JwkSelector();
        this.issuerValidator = new IssuerValidator();
        this.jwsService = new JwsService(services.jsonProvider(), services.securityBaseline().algorithmGuard(),
                Set.of(JwaAlgorithm.RS256.name()));
        final JweService dormantJweService = new JweService(services.jsonProvider(),
                services.securityBaseline().algorithmGuard(), Set.of(JwaAlgorithm.RSA_OAEP_256.name()),
                Set.of(JwaAlgorithm.A256GCM.name()));
        final IdTokenCodec idTokenCodec = new IdTokenCodec(
                new JwtVerifier(services.jsonProvider(), jwsService, dormantJweService));
        this.idTokenVerifier = new IdTokenVerifier(idTokenCodec, issuerValidator,
                services.securityBaseline().timeGuard(Protocol.OIDC, services.fabricContext().clock()));
    }

    /**
     * Composes the standard OpenID Connect operations from protocol-owned clients and codecs.
     *
     * @param variant         selected Alibaba Cloud variant
     * @param options         validated Alibaba Cloud Source options
     * @param services        caller-owned execution services
     * @param redirectManager shared browser correlation lifecycle
     * @return adapter containing only standard protocol operation bindings
     */
    private static StandardAdapter standardAdapter(
            final VariantManifest.Variant variant,
            final AliyunOptions options,
            final ExecutionServices services,
            final RedirectManager redirectManager) {
        final var targets = variant.targets().resolve(options);
        final String redirectUri = options.redirectUri().orElseThrow(
                () -> new ValidateException("Alibaba Cloud browser Source requires a registered redirect URI"));
        final OAuth2ClientOptions oauthSettings = new OAuth2ClientOptions(targets.authorization(), targets.token(),
                targets.introspection(), targets.revocation(), targets.deviceAuthorization(), Optional.empty(),
                Optional.of(ISSUER), options.clientId(), Set.of(redirectUri),
                Endpoint.Authentication.CLIENT_SECRET_POST, Optional.of(options.credential()), true, false);
        final AuthorizationClient authorizationClient = new AuthorizationClient(oauthSettings,
                new AuthorizationRequestEncoder(targets.authorization().getOrNull()));
        final TokenClient tokenClient = new TokenClient(oauthSettings, services, new TokenRequestEncoder(),
                new TokenResponseDecoder(services.jsonProvider()));
        final RevocationClient revocationClient = new RevocationClient(oauthSettings, services,
                new RevocationRequestEncoder());
        final OAuth2Client oauthClient = new OAuth2Client(authorizationClient, tokenClient, Optional.empty(),
                Optional.of(revocationClient), Optional.empty(), Optional.empty());
        final OpenIdClientOptions openIdSettings = new OpenIdClientOptions(oauthSettings, targets.discovery(),
                targets.userInfo(), targets.jwks(), targets.endSession(), Set.of(JwaAlgorithm.RS256));
        final OpenIdClient openIdClient = new OpenIdClient(oauthClient,
                new AuthenticationRequestEncoder(services.jsonProvider()),
                new DiscoveryClient(openIdSettings, services, new OpenIdProviderMetadataCodec(services.jsonProvider())),
                Optional.of(new UserInfoClient(openIdSettings, services, new UserInfoCodec(services.jsonProvider()))),
                Optional.empty());
        return new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OpenIdClientScheme.AUTHENTICATION, openIdClient::authorize),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.TOKEN, openIdClient::token),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.REVOCATION, openIdClient::revoke),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.DISCOVERY,
                                (ignored, context, timeout) -> openIdClient.discover(context, timeout)),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.USERINFO, openIdClient::userInfo)));
    }

    /**
     * Accepts only the two RFC 7009 token type hints registered by Alibaba Cloud.
     *
     * @param request standard revocation request
     * @return {@code true} when the hint is absent, {@code access_token}, or {@code refresh_token}
     */
    private static boolean validHint(final RevocationRequest request) {
        final String hint = request.tokenTypeHint().getOrNull();
        return hint == null || OAuth2.Parameters.ACCESS_TOKEN.equals(hint)
                || OAuth2.Parameters.REFRESH_TOKEN.equals(hint);
    }

    /**
     * Reads one mandatory non-blank string-valued Discovery extension.
     *
     * @param metadata decoded OpenID Provider metadata
     * @param name     exact extension member name
     * @return exact extension value
     * @throws ValidateException if the extension is absent, blank, or not a string
     */
    private static String extension(final OpenIdProviderMetadata metadata, final String name) {
        final JsonValue value = metadata.extensions().values().get(name);
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Alibaba Cloud discovery string extension is invalid");
        }
        return string.value();
    }

    /**
     * Reads one mandatory array of unique non-blank string Discovery extension values.
     *
     * @param metadata decoded OpenID Provider metadata
     * @param name     exact extension member name
     * @return immutable extension string values
     * @throws ValidateException if the extension is absent or contains another JSON shape
     */
    private static List<String> extensionArray(final OpenIdProviderMetadata metadata, final String name) {
        final JsonValue value = metadata.extensions().values().get(name);
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("Alibaba Cloud discovery array extension is invalid");
        }
        final ArrayList<String> values = new ArrayList<>(array.values().size());
        for (JsonValue item : array.values()) {
            if (!(item instanceof JsonValue.StringValue string) || string.value().isBlank()
                    || values.contains(string.value())) {
                throw new ValidateException("Alibaba Cloud discovery array extension contains an invalid value");
            }
            values.add(string.value());
        }
        return List.copyOf(values);
    }

    /**
     * Converts one selected RSA JWK using the existing bus-crypto key construction primitive.
     *
     * @param jwk selected public RSA JWK
     * @return JCA RSA public verification key
     * @throws ValidateException if modulus or exponent is missing or cannot form an RSA public key
     */
    private static PublicKey rsaPublicKey(final Jwk jwk) {
        if (!"RSA".equals(jwk.keyType()) || jwk.hasPrivateMaterial()) {
            throw new ValidateException("Alibaba Cloud ID Token requires a public RSA JWK");
        }
        final byte[] modulus = binary(jwk, "n");
        final byte[] exponent = binary(jwk, "e");
        try {
            return Keeper.getRSAPublicKey(new BigInteger(1, modulus), new BigInteger(1, exponent));
        } finally {
            java.util.Arrays.fill(modulus, (byte) 0);
            java.util.Arrays.fill(exponent, (byte) 0);
        }
    }

    /**
     * Decodes one required unpadded Base64URL JWK integer member through the bus-core Base64 codec.
     *
     * @param jwk  source JWK
     * @param name exact registered member name
     * @return newly allocated unsigned integer octets
     * @throws ValidateException if the member is absent, has the wrong JSON type, or decodes to an empty value
     */
    private static byte[] binary(final Jwk jwk, final String name) {
        final JsonValue value = jwk.parameter(name)
                .orElseThrow(() -> new ValidateException("Alibaba Cloud RSA JWK lacks required member " + name));
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Alibaba Cloud RSA JWK member must be a Base64URL string");
        }
        final byte[] decoded = Base64.decode(string.value());
        if (decoded == null || decoded.length == 0) {
            throw new ValidateException("Alibaba Cloud RSA JWK integer must not be empty");
        }
        return decoded;
    }

    /**
     * Decodes the Alibaba Cloud profile extension object into one typed private wire value.
     *
     * @param userInfo verified standard UserInfo claims
     * @param claims   verified ID Token claims used only when UserInfo omits an extension
     * @return typed Alibaba Cloud profile extensions
     * @throws ValidateException if a present extension is not a non-blank string
     */
    private static ProfileClaims profileClaims(final UserInfoResponse userInfo, final IdTokenClaims claims) {
        return new ProfileClaims(profileClaim(userInfo, claims, "login_name"), profileClaim(userInfo, claims, "name"),
                profileClaim(userInfo, claims, "type"), profileClaim(userInfo, claims, "upn"),
                profileClaim(userInfo, claims, "aid"), profileClaim(userInfo, claims, "uid"));
    }

    /**
     * Reads one Alibaba Cloud profile extension with UserInfo taking precedence over the ID Token.
     *
     * @param userInfo verified standard UserInfo claims
     * @param claims   verified ID Token claims
     * @param name     exact Alibaba Cloud extension member
     * @return decoded string or {@code null} when the extension is absent
     * @throws ValidateException if a present extension is not a non-blank string
     */
    private static String profileClaim(final UserInfoResponse userInfo, final IdTokenClaims claims, final String name) {
        final JsonValue value = userInfo.claims().values().containsKey(name) ? userInfo.claims().values().get(name)
                : claims.extensions().values().get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Alibaba Cloud identity attribute must be a non-blank string");
        }
        return string.value();
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
        });
    }

    /**
     * Creates a provider-neutral empty JSON object for standard extension fields.
     *
     * @return immutable empty JSON object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Creates a completed undeclared-capability rejection.
     *
     * @param <S> expected successful response type
     * @return completed rejection stage
     */
    private static <S> CompletionStage<Outcome<S>> missing() {
        return completed(rejected("Alibaba Cloud capability is not declared"));
    }

    /**
     * Creates a completed request-shape or unavailable-operation rejection.
     *
     * @param <S> expected successful response type
     * @return completed rejection stage
     */
    private static <S> CompletionStage<Outcome<S>> mismatch() {
        return completed(rejected("Alibaba Cloud capability request is invalid"));
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
     * Creates a safe expected protocol or upstream rejection.
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
    private static <T> Outcome<T> failed(final org.miaixz.bus.core.basic.normal.Errors code, final String description) {
        return Outcome.failed(new Outcome.Failure(code, description, emptyObject()));
    }

    /**
     * Returns the exact frozen Alibaba Cloud capability manifest.
     *
     * @return immutable manifest containing Source authentication and supported standard OIDC operations
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes Source authentication, public JWK retrieval, and unmodified standard OIDC operations.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific request or {@code null} for a resource retrieval operation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed operation outcome without exposing private Vendor response models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Alibaba Cloud capability must not be null");
        Assert.notNull(context, "Alibaba Cloud invocation context must not be null");
        Assert.notNull(timeout, "Alibaba Cloud invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return missing();
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
        if (capability.equals(OpenIdClientScheme.JWK_SET) && request == null) {
            return narrow(jwks(context, timeout), capability.responseType());
        }
        if (capability.equals(OpenIdClientScheme.DISCOVERY) && request == null) {
            return narrow(discover(context, timeout), capability.responseType());
        }
        if (capability.equals(OpenIdClientScheme.TOKEN) && request instanceof TokenRequest tokenRequest) {
            return narrow(token(tokenRequest, context, timeout), capability.responseType());
        }
        if (capability.equals(OpenIdClientScheme.REVOCATION) && request instanceof RevocationRequest revocation) {
            return validHint(revocation) ? standardAdapter.invoke(capability, request, context, timeout) : mismatch();
        }
        if (standardAdapter.manifest().capabilities().contains(capability)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return mismatch();
    }

    /**
     * Executes only the authorization-code and refresh-token grants registered by the Alibaba Cloud profile.
     *
     * @param request standard token request carrying one supported grant
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return standard token response after profile-specific success constraints are checked
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        if (!(request.grant() instanceof AuthorizationCodeGrant) && !(request.grant() instanceof RefreshTokenGrant)) {
            return completed(rejected("Alibaba Cloud token endpoint does not support the requested grant type"));
        }
        return standardAdapter.invoke(OpenIdClientScheme.TOKEN, request, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<TokenEndpointResponse> success -> token(success.value(), request.grant());
                    case Outcome.Rejected<TokenEndpointResponse> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<TokenEndpointResponse> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Applies the success requirements belonging to the grant that produced a standard token response.
     *
     * @param response decoded standard token response
     * @param grant    exact standard grant submitted to the endpoint
     * @return unchanged successful response or a safe protocol rejection
     */
    private Outcome<TokenEndpointResponse> token(final TokenEndpointResponse response, final TokenRequest.Grant grant) {
        try {
            final TokenResponse oauth = response instanceof OpenIdTokenResponse openId ? openId.tokenResponse()
                    : (TokenResponse) response;
            if (!TokenType.BEARER.equals(oauth.tokenType()) || oauth.expiresIn().isEmpty()
                    || oauth.expiresIn().getOrNull() <= 0L) {
                throw new ValidateException("Alibaba Cloud token response requires Bearer and positive expires_in");
            }
            if (grant instanceof AuthorizationCodeGrant) {
                if (!(response instanceof OpenIdTokenResponse openId)) {
                    throw new ValidateException("Alibaba Cloud authorization-code response requires an ID Token");
                }
                requireToken(openId);
            }
            return Outcome.succeeded(response);
        } catch (RuntimeException cause) {
            return rejected("Alibaba Cloud token response does not satisfy the registered grant profile");
        }
    }

    /**
     * Builds a standard OIDC Authentication Request containing the generated nonce and mandatory S256 challenge.
     *
     * @param initiation generated one-time browser security material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return standard-client-produced authorization URL bound to the generated state
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        try {
            final List<String> requestedScopes = requestedScopes();
            final var challenge = initiation.codeChallenge().getOrNull();
            final String nonce = initiation.nonce().getOrNull();
            if (challenge == null || nonce == null || !PkceMethod.S256.equals(challenge.method())) {
                return completed(
                        failed(
                                ErrorCode._500,
                                "Alibaba Cloud browser flow did not generate required nonce and S256 PKCE material"));
            }
            final AuthorizationRequest authorization = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                    options.redirectUri(), Optional.of(new Scope(requestedScopes)), Optional.of(initiation.state()),
                    Optional.of(challenge.value()), Optional.of(PkceMethod.S256.value()), emptyObject());
            final AuthenticationRequest authentication = new AuthenticationRequest(authorization, Optional.of(nonce),
                    Optional.empty(), Optional.empty(), Optional.empty(), List.of(), Optional.empty(), Optional.empty(),
                    List.of(), Optional.empty(), Optional.empty(), emptyObject());
            return standardAdapter.invoke(OpenIdClientScheme.AUTHENTICATION, authentication, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<org.miaixz.bus.fabric.UnoUrl> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<org.miaixz.bus.fabric.UnoUrl> rejected -> Outcome
                                .rejected(rejected.failure());
                        case Outcome.Failed<org.miaixz.bus.fabric.UnoUrl> failed -> Outcome.failed(failed.failure());
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Alibaba Cloud Authentication Request is invalid"));
        }
    }

    /**
     * Extracts the unique standard callback state after checking the exact registered redirect transport.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return state from either the standard success or error branch
     * @throws ValidateException if the callback URI, method, multiplicity, branch, or state is invalid
     */
    private String state(final Callback.Inbound callback) {
        final AuthorizationResponseDecoder.Decoded decoded = callback(callback);
        return switch (decoded) {
            case AuthorizationResponseDecoder.Success success -> success.response().state().orElseThrow(
                    () -> new ValidateException("Alibaba Cloud authorization success response requires state"));
            case AuthorizationResponseDecoder.Error error -> error.response().state().orElseThrow(
                    () -> new ValidateException("Alibaba Cloud authorization error response requires state"));
        };
    }

    /**
     * Redeems a correlated authorization response and verifies ID Token, UserInfo, and their common subject.
     *
     * @param completion consumed callback, nonce, and PKCE verifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return fully verified Alibaba Cloud external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final AuthorizationResponseDecoder.Decoded decoded;
        try {
            decoded = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Alibaba Cloud authorization callback is invalid"));
        }
        if (decoded instanceof AuthorizationResponseDecoder.Error error) {
            final Map<String, JsonValue> details = Map
                    .of("oauth_error", new JsonValue.StringValue(error.response().error().value()));
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400,
                                    "Alibaba Cloud authorization endpoint returned a standard error",
                                    new JsonValue.ObjectValue(details))));
        }
        final AuthorizationCodeResponse response = ((AuthorizationResponseDecoder.Success) decoded).response();
        try {
            response.issuer().ifPresent(received -> issuerValidator.validate(ISSUER, received));
        } catch (RuntimeException cause) {
            return completed(rejected("Alibaba Cloud authorization response issuer does not match"));
        }
        final String verifier = completion.codeVerifier().isPresent() ? completion.codeVerifier().getOrNull().value()
                : null;
        if (verifier == null || completion.correlation().nonce().isEmpty()) {
            return completed(
                    failed(ErrorCode._500, "Alibaba Cloud browser correlation lacks required nonce or PKCE verifier"));
        }
        final TokenRequest tokenRequest = new TokenRequest(new AuthorizationCodeGrant(response.code(),
                options.redirectUri(), Optional.empty(), Optional.of(verifier)), emptyObject());
        return standardAdapter.invoke(OpenIdClientScheme.TOKEN, tokenRequest, context, timeout)
                .thenCompose(token -> switch (token) {
                    case Outcome.Succeeded<TokenEndpointResponse> success -> success
                            .value() instanceof OpenIdTokenResponse openId
                                    ? verifyToken(openId, response, completion, context, timeout)
                                    : completed(
                                            rejected("Alibaba Cloud authorization-code response omitted the ID Token"));
                    case Outcome.Rejected<TokenEndpointResponse> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<TokenEndpointResponse> failed -> completed(Outcome.failed(failed.failure()));
                });
    }

    /**
     * Retrieves the current issuer-bound JWK Set before selecting and verifying the returned ID Token.
     *
     * @param token      standard token endpoint response
     * @param response   correlated authorization response
     * @param completion consumed browser correlation material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return verified identity operation stage
     */
    private CompletionStage<Outcome<ExternalIdentity>> verifyToken(
            final OpenIdTokenResponse token,
            final AuthorizationCodeResponse response,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final String compact;
        try {
            requireToken(token);
            compact = token.idToken().compact();
        } catch (RuntimeException cause) {
            return completed(rejected("Alibaba Cloud token response does not satisfy the registered OIDC manifest"));
        }
        return jwks(context, timeout).thenCompose(keys -> switch (keys) {
            case Outcome.Succeeded<JwkSet> success -> verifyIdToken(
                    token,
                    compact,
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
     * Selects the current RSA verification key and applies the complete OIDC ID Token validation profile.
     *
     * @param token      standard token endpoint response
     * @param compact    sensitive compact ID Token
     * @param keys       current public JWK Set from the configured issuer endpoint
     * @param response   correlated authorization response
     * @param completion consumed nonce, state, and PKCE material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return identity stage after ID Token and UserInfo verification
     */
    private CompletionStage<Outcome<ExternalIdentity>> verifyIdToken(
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
                throw new ValidateException("Alibaba Cloud ID Token must use RS256");
            }
            final String keyId = header.keyId()
                    .orElseThrow(() -> new ValidateException("Alibaba Cloud ID Token requires a protected kid"));
            final Jwk selected = jwkSelector.requireUnique(
                    keys,
                    new JwkSelector.Selection(header.keyId(), JwaAlgorithm.RS256.name(), JwaAlgorithm.Kind.SIGNATURE,
                            Optional.of("sig"), Optional.of("verify"), Optional.of("RSA")));
            if (selected.keyId().filter(keyId::equals).isEmpty()
                    || selected.algorithm().filter(JwaAlgorithm.RS256.name()::equals).isEmpty()
                    || selected.publicKeyUse().filter("sig"::equals).isEmpty()) {
                throw new ValidateException("Alibaba Cloud JWK must explicitly bind kid, alg=RS256, and use=sig");
            }
            key = rsaPublicKey(selected);
        } catch (RuntimeException cause) {
            return completed(rejected("Alibaba Cloud ID Token key selection failed"));
        }
        final IdTokenVerifier.Request verification = new IdTokenVerifier.Request(new IdToken(compact),
                new JwtVerifier.Signed(key, Set.of()), ISSUER, options.clientId(),
                completion.correlation().nonce().getOrNull(), Optional.empty(), Optional.of(token.accessToken()),
                Optional.of(response.code()), Optional.of(completion.correlation().state()));
        return idTokenVerifier.verify(verification, context, timeout).thenCompose(claims -> switch (claims) {
            case Outcome.Succeeded<IdTokenClaims> success -> userInfo(token, success.value(), context, timeout);
            case Outcome.Rejected<IdTokenClaims> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<IdTokenClaims> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Retrieves standard UserInfo and requires its subject to equal the cryptographically verified ID Token subject.
     *
     * @param token   standard token response containing the bearer access token
     * @param claims  verified typed ID Token claims
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return verified external identity
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
     * Maps claims only after enforcing the OpenID Connect UserInfo subject-binding rule.
     *
     * @param userInfo standard UserInfo response
     * @param claims   verified ID Token claims
     * @param timeout  shared clock used to timestamp verification evidence
     * @return verified external identity or a safe rejection
     */
    private Outcome<ExternalIdentity> identity(
            final UserInfoResponse userInfo,
            final IdTokenClaims claims,
            final Timeout.Budget timeout) {
        if (!claims.subject().equals(userInfo.subject())) {
            return rejected("Alibaba Cloud UserInfo subject does not match the verified ID Token");
        }
        final JsonValue.ObjectValue attributes;
        try {
            attributes = profileClaims(userInfo, claims).attributes();
        } catch (RuntimeException cause) {
            return rejected("Alibaba Cloud identity attributes are invalid");
        }
        final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                new Evidence.Claim(JwtClaims.SUBJECT, new JsonValue.StringValue(claims.subject()), ISSUER,
                        timeout.clock().now()));
        return Outcome.succeeded(new ExternalIdentity(sourceId, claims.subject(), attributes, List.of(evidence)));
    }

    /**
     * Delegates OpenID Provider Discovery and binds every security-relevant returned endpoint and capability to the
     * frozen Alibaba Cloud manifest.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return standard metadata only after all fixed-profile bindings succeed
     */
    private CompletionStage<Outcome<OpenIdProviderMetadata>> discover(
            final Context context,
            final Timeout.Budget timeout) {
        return standardAdapter.invoke(OpenIdClientScheme.DISCOVERY, null, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<OpenIdProviderMetadata> success -> metadata(success.value());
                    case Outcome.Rejected<OpenIdProviderMetadata> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<OpenIdProviderMetadata> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Validates Alibaba Cloud metadata without allowing remote discovery to rewrite the compiled profile.
     *
     * @param metadata issuer-bound standard metadata
     * @return unchanged metadata or a safe rejection
     */
    private Outcome<OpenIdProviderMetadata> metadata(final OpenIdProviderMetadata metadata) {
        try {
            final var resolvedTargets = variant.targets().resolve(options);
            issuerValidator.validate(ISSUER, metadata.issuer());
            if (!resolvedTargets.authorization().getOrNull().url().toString().equals(metadata.authorizationEndpoint())
                    || !resolvedTargets.token().getOrNull().url().toString().equals(metadata.tokenEndpoint())
                    || metadata.userInfoEndpoint().isEmpty()
                    || !resolvedTargets.userInfo().getOrNull().url().toString()
                            .equals(metadata.userInfoEndpoint().getOrNull())
                    || !resolvedTargets.jwks().getOrNull().url().toString().equals(metadata.jwksUri())
                    || !metadata.responseTypesSupported().contains(ResponseType.CODE)
                    || !metadata.subjectTypesSupported().contains(SubjectType.PUBLIC)
                    || !metadata.scopesSupported().containsAll(List.of("openid", "profile", "aliuid"))
                    || !metadata.idTokenSigningAlgValuesSupported().contains(JwaAlgorithm.RS256)
                    || !extension(metadata, REVOCATION_ENDPOINT)
                            .equals(resolvedTargets.revocation().getOrNull().url().toString())
                    || !extensionArray(metadata, CODE_CHALLENGE_METHODS).contains(PkceMethod.S256.value())) {
                throw new ValidateException("Alibaba Cloud discovery metadata differs from the frozen manifest");
            }
            return Outcome.succeeded(metadata);
        } catch (RuntimeException cause) {
            return rejected("Alibaba Cloud discovery metadata does not match the registered Source");
        }
    }

    /**
     * Enforces the token success constraints required by the frozen Alibaba Cloud OIDC manifest.
     *
     * @param token decoded standard token response
     * @throws ValidateException if token type, lifetime, ID Token, or returned scope is invalid
     */
    private void requireToken(final OpenIdTokenResponse token) {
        Assert.notNull(token, "Alibaba Cloud token response must not be null");
        if (!TokenType.BEARER.equals(token.tokenType()) || token.expiresIn().isEmpty()
                || token.expiresIn().getOrNull() <= 0L) {
            throw new ValidateException(
                    "Alibaba Cloud authorization-code response requires Bearer, expires_in, and id_token");
        }
        final org.miaixz.bus.auth.protocol.oauth2.Scope returned = token.scope().getOrNull();
        if (returned != null && !returned.values().containsAll(requestedScopes())) {
            throw new ValidateException("Alibaba Cloud token response scope omits a requested scope");
        }
    }

    /**
     * Returns the explicit registered scopes or the immutable manifest defaults.
     *
     * @return ordered effective Alibaba Cloud scopes
     */
    private List<String> requestedScopes() {
        return options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
    }

    /**
     * Retrieves and strictly decodes the configured Alibaba Cloud public JWK Set resource.
     *
     * @param context immutable invocation context retained for the uniform operation signature
     * @param timeout shared end-to-end budget
     * @return current issuer public JWK Set or a closed framework failure
     */
    private CompletionStage<Outcome<JwkSet>> jwks(final Context context, final Timeout.Budget timeout) {
        Assert.notNull(context, "Alibaba Cloud JWK Set context must not be null");
        Assert.notNull(timeout, "Alibaba Cloud JWK Set budget must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Alibaba Cloud JWK Set request has no remaining time budget"));
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (timeout.expired()) {
                    return failed(ErrorCode._408, "Alibaba Cloud JWK Set request exhausted its time budget");
                }
                final String endpoint = variant.targets().resolve(options).jwks().getOrNull().url().toString();
                return Outcome.succeeded(
                        jwkSetCodec.decode(
                                Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                                        .timeout(timeout.forFabric())
                                        .addressPolicy(
                                                services.securityBaseline().require(Protocol.OIDC).addressPolicy())
                                        .execute()));
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Alibaba Cloud JWK Set endpoint request failed");
            }
        }, services.executor());
    }

    /**
     * Validates the exact registered callback URI before applying the standard response decoder.
     *
     * @param callback raw inbound callback
     * @return discriminated standard authorization response
     * @throws ValidateException if the callback target differs from the registered redirect URI
     */
    private AuthorizationResponseDecoder.Decoded callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Alibaba Cloud callback must not be null");
        if (!options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Alibaba Cloud callback URI does not match the registered redirect URI");
        }
        return authorizationResponseDecoder.decode(callback);
    }

    /**
     * Carries the Alibaba Cloud private profile extensions without exposing them as framework or OIDC properties.
     *
     * @param loginName         platform login name
     * @param name              platform display name
     * @param type              platform account type
     * @param userPrincipalName platform user principal name
     * @param accountId         platform account identifier
     * @param userId            platform user identifier
     * @author Kimi Liu
     */
    private record ProfileClaims(String loginName, String name, String type, String userPrincipalName, String accountId,
            String userId) {

        /**
         * Adds one present private profile member to an identity attribute object.
         *
         * @param values mutable assembly map confined to this conversion
         * @param member exact Alibaba Cloud wire member name
         * @param value  decoded value or {@code null} when absent
         */
        private static void add(final Map<String, JsonValue> values, final String member, final String value) {
            if (value != null) {
                values.put(member, new JsonValue.StringValue(value));
            }
        }

        /**
         * Converts present private profile values to the opaque external-identity attribute object.
         *
         * @return immutable JSON attribute object using Alibaba Cloud wire member names
         */
        private JsonValue.ObjectValue attributes() {
            final Map<String, JsonValue> values = new LinkedHashMap<>();
            add(values, "login_name", loginName);
            add(values, "name", name);
            add(values, "type", type);
            add(values, "upn", userPrincipalName);
            add(values, "aid", accountId);
            add(values, "uid", userId);
            return new JsonValue.ObjectValue(values);
        }

    }

}
