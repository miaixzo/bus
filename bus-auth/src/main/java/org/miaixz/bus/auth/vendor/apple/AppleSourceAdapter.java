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
package org.miaixz.bus.auth.vendor.apple;

import java.math.BigInteger;
import java.net.URI;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.Parameter;
import org.miaixz.bus.auth.guard.IssuerValidator;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.RevocationRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenResponseDecoder;
import org.miaixz.bus.auth.protocol.oidc.*;
import org.miaixz.bus.auth.protocol.oidc.client.IdTokenVerifier;
import org.miaixz.bus.auth.protocol.oidc.client.OpenIdClientScheme;
import org.miaixz.bus.auth.protocol.oidc.codec.IdTokenCodec;
import org.miaixz.bus.auth.protocol.oidc.codec.JwkSetCodec;
import org.miaixz.bus.auth.protocol.oidc.codec.OpenIdProviderMetadataCodec;
import org.miaixz.bus.auth.resolver.KeyMaterial;
import org.miaixz.bus.auth.runtime.ExecutionServices;
import org.miaixz.bus.auth.shared.jose.*;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.jwt.JwtVerifier;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.worker.KeyLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Fabric;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Implements the frozen Sign in with Apple OpenID Connect Source contract.
 * <p>
 * The adapter keeps OAuth and OpenID Connect public models on the Registry boundary. Apple-only form-post callback
 * handling and the ES256 client-secret JWT remain private wire adaptations and never become framework operations.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AppleSourceAdapter implements VendorAdapter {

    /**
     * Exact Sign in with Apple issuer and client-secret audience.
     */
    private static final String ISSUER = "https://appleid.apple.com";

    /**
     * Exact key-use value supplied to the external key loader.
     */
    private static final String SIGNATURE_USE = "sig";

    /**
     * Apple client-secret JWT lifetime measured from the shared invocation clock.
     */
    private static final Duration CLIENT_SECRET_LIFETIME = Duration.ofMinutes(5);

    /**
     * Registered Source identifier copied into verified external identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Sign in with Apple manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Sign in with Apple options.
     */
    private final AppleOptions options;

    /**
     * Caller-owned runtime, cryptographic, network, and JSON dependencies.
     */
    private final ExecutionServices services;

    /**
     * Shared one-time state and OpenID Connect nonce lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Standard OAuth authorization request encoder bound to Apple's fixed endpoint.
     */
    private final AuthorizationRequestEncoder authorizationRequestEncoder;

    /**
     * Profile-scoped JWS service used only for Apple client-secret JWT signing.
     */
    private final JwsService clientSecretJws;

    /**
     * Standard OAuth token request field encoder.
     */
    private final TokenRequestEncoder tokenRequestEncoder;

    /**
     * Standard RFC 7009 revocation request field encoder.
     */
    private final RevocationRequestEncoder revocationRequestEncoder;

    /**
     * Shared strict form body codec.
     */
    private final FormCodec formCodec;

    /**
     * Strict standard OAuth token response decoder.
     */
    private final TokenResponseDecoder tokenResponseDecoder;

    /**
     * Strict OpenID Provider Metadata response codec.
     */
    private final OpenIdProviderMetadataCodec metadataCodec;

    /**
     * Strict public JWK Set response codec.
     */
    private final JwkSetCodec jwkSetCodec;

    /**
     * Shared exact public-key selector.
     */
    private final JwkSelector jwkSelector;

    /**
     * Profile-scoped RS256 parser and verifier for Apple ID Tokens.
     */
    private final JwsService idTokenJws;

    /**
     * Complete OIDC ID Token claim and artifact verifier.
     */
    private final IdTokenVerifier idTokenVerifier;

    /**
     * Exact case-sensitive issuer validator.
     */
    private final IssuerValidator issuerValidator;

    /**
     * Creates one Source-bound Sign in with Apple adapter.
     *
     * @param namespaceId registration namespace used to isolate browser correlation and key resolution
     * @param sourceId    registered Source identifier
     * @param manifest    selected Sign in with Apple manifest
     * @param variant     selected default variant manifest
     * @param options     decoded externally loaded Sign in with Apple options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, callback, or algorithm policy differs from the frozen
     *                                  profile
     */
    public AppleSourceAdapter(final String namespaceId, final String sourceId, final AppleManifest manifest,
            final VariantManifest.Variant variant, final AppleOptions options, final ExecutionServices services) {
        Assert.notNull(manifest, "Sign in with Apple manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Sign in with Apple Source id must not be blank");
        this.variant = Assert.notNull(variant, "Sign in with Apple manifest must not be null");
        this.options = Assert.notNull(options, "Sign in with Apple options must not be null");
        this.services = Assert.notNull(services, "Sign in with Apple execution services must not be null");
        if (!AppleManifest.ID.equals(manifest.vendor()) || !manifest.variant(AppleManifest.DEFAULT).equals(variant)
                || !AppleManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OIDC
                || !AppleManifest.ID.equals(options.vendor()) || !AppleManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("Sign in with Apple adapter requires the apple/default OIDC manifest");
        }
        validateRedirectUri(options.redirectUri().getOrNull());
        final Set<String> algorithms = services.securityBaseline().require(Protocol.OIDC).algorithms();
        if (!algorithms.contains(JwaAlgorithm.ES256.name()) || !algorithms.contains(JwaAlgorithm.RS256.name())) {
            throw new ValidateException("Sign in with Apple requires ES256 client signing and RS256 ID Tokens");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.authorizationRequestEncoder = new AuthorizationRequestEncoder(
                variant.targets().resolve(options).authorization().getOrNull());
        this.clientSecretJws = new JwsService(services.jsonProvider(), services.securityBaseline().algorithmGuard(),
                Set.of(JwaAlgorithm.ES256.name()));
        this.tokenRequestEncoder = new TokenRequestEncoder();
        this.revocationRequestEncoder = new RevocationRequestEncoder();
        this.formCodec = new FormCodec();
        this.tokenResponseDecoder = new TokenResponseDecoder(services.jsonProvider());
        this.metadataCodec = new OpenIdProviderMetadataCodec(services.jsonProvider());
        this.jwkSetCodec = new JwkSetCodec(services.jsonProvider());
        this.jwkSelector = new JwkSelector();
        this.issuerValidator = new IssuerValidator();
        this.idTokenJws = new JwsService(services.jsonProvider(), services.securityBaseline().algorithmGuard(),
                Set.of(JwaAlgorithm.RS256.name()));
        final JweService dormantJwe = new JweService(services.jsonProvider(),
                services.securityBaseline().algorithmGuard(), Set.of(JwaAlgorithm.RSA_OAEP_256.name()),
                Set.of(JwaAlgorithm.A256GCM.name()));
        final IdTokenCodec idTokenCodec = new IdTokenCodec(
                new JwtVerifier(services.jsonProvider(), idTokenJws, dormantJwe));
        this.idTokenVerifier = new IdTokenVerifier(idTokenCodec, issuerValidator,
                services.securityBaseline().timeGuard(Protocol.OIDC, services.fabricContext().clock()));
    }

    /**
     * Applies Apple success constraints without changing the standard token response model.
     *
     * @param response decoded standard token response
     * @param grant    grant that produced the response
     * @return unchanged valid response or safe rejection
     */
    private static Outcome<TokenEndpointResponse> validateToken(
            final TokenEndpointResponse response,
            final TokenRequest.Grant grant) {
        if (!(response instanceof TokenResponse oauthResponse)) {
            return rejected("Sign in with Apple token endpoint returned an unsupported success type");
        }
        final TokenEndpointResponse normalized;
        try {
            normalized = oauthResponse.extensions().values().containsKey(OpenIdConnect.Parameters.ID_TOKEN)
                    ? OpenIdTokenResponse.from(oauthResponse)
                    : oauthResponse;
        } catch (RuntimeException cause) {
            return rejected("Sign in with Apple token response contains an invalid id_token");
        }
        final TokenResponse oauth = normalized instanceof OpenIdTokenResponse openId ? openId.tokenResponse()
                : (TokenResponse) normalized;
        if (!TokenType.BEARER.equals(oauth.tokenType())) {
            return rejected("Sign in with Apple token response requires Bearer");
        }
        if (oauth.expiresIn().isPresent() && oauth.expiresIn().getOrNull() <= 0L) {
            return rejected("Sign in with Apple token response contains an invalid expires_in");
        }
        if (grant instanceof AuthorizationCodeGrant
                && (!(normalized instanceof OpenIdTokenResponse) || oauth.refreshToken().isEmpty())) {
            return rejected("Sign in with Apple authorization-code response requires refresh_token and id_token");
        }
        return Outcome.succeeded(normalized);
    }

    /**
     * Copies one optional claim after enforcing its exact JSON type and non-blank string semantics.
     *
     * @param source source claim object
     * @param target verified identity attributes
     * @param name   exact claim name
     * @param type   required JSON value type
     */
    private static void copyClaim(
            final JsonValue.ObjectValue source,
            final Map<String, JsonValue> target,
            final String name,
            final Class<? extends JsonValue> type) {
        final JsonValue value = source.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return;
        }
        if (!type.isInstance(value) || value instanceof JsonValue.StringValue string && string.value().isBlank()) {
            throw new ValidateException("Sign in with Apple identity claim has an invalid type or value");
        }
        target.put(name, value);
    }

    /**
     * Converts one selected RSA JWK through the shared bus-crypto key primitive.
     *
     * @param jwk selected public RSA JWK
     * @return JCA RSA public verification key
     */
    private static PublicKey rsaPublicKey(final Jwk jwk) {
        if (!"RSA".equals(jwk.keyType()) || jwk.hasPrivateMaterial()) {
            throw new ValidateException("Sign in with Apple ID Token requires a public RSA JWK");
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
     * @param jwk  source JWK
     * @param name exact member name
     * @return newly allocated unsigned integer octets
     */
    private static byte[] binary(final Jwk jwk, final String name) {
        final JsonValue value = jwk.parameter(name)
                .orElseThrow(() -> new ValidateException("Sign in with Apple RSA JWK lacks required member"));
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Sign in with Apple RSA JWK member must be a Base64URL string");
        }
        final byte[] decoded = Base64.decode(string.value());
        if (decoded == null || decoded.length == 0) {
            throw new ValidateException("Sign in with Apple RSA JWK integer must not be empty");
        }
        return decoded;
    }

    /**
     * Validates the Apple-registered redirect URI security profile.
     *
     * @param value exact registered redirect URI
     * @throws ValidateException if the URI is not HTTPS, is local or numeric, or contains query or fragment data
     */
    private static void validateRedirectUri(final String value) {
        final URI redirect;
        try {
            redirect = URI.create(value);
        } catch (RuntimeException cause) {
            throw new ValidateException("Sign in with Apple redirect URI is invalid", cause);
        }
        final String host = redirect.getHost();
        if (!Protocol.HTTPS.name.equalsIgnoreCase(redirect.getScheme()) || host == null
                || Protocol.HOST_LOCAL.equalsIgnoreCase(host) || Validator.isIpv4(host) || Validator.isIpv6(host)
                || redirect.getRawUserInfo() != null || redirect.getRawQuery() != null
                || redirect.getRawFragment() != null) {
            throw new ValidateException(
                    "Sign in with Apple redirect URI must be registered HTTPS on a non-local DNS host");
        }
    }

    /**
     * Creates one immutable provider-neutral empty JSON object.
     *
     * @return empty extension object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Creates a provider-neutral JSON integer.
     *
     * @param value integer value
     * @return JSON numeric value
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(java.math.BigDecimal.valueOf(value));
    }

    /**
     * Narrows an operation outcome through its declared response type.
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
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates a safe internal failure without a custom exception or sensitive detail.
     *
     * @param description non-sensitive failure description
     * @param <T>         expected successful value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final String description) {
        return Outcome.failed(new Outcome.Failure(ErrorCode._500, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Creates a safe operational failure using one shared Bus error code.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param <T>         expected successful value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final org.miaixz.bus.core.basic.normal.Errors code, final String description) {
        return Outcome.failed(new Outcome.Failure(code, description, new JsonValue.ObjectValue(Map.of())));
    }

    /**
     * Accepts one callback value only once.
     *
     * @param current previously decoded value or {@code null}
     * @param value   next non-blank value
     * @return next value
     * @throws ValidateException if the member already appeared
     */
    private static String unique(final String current, final String value) {
        if (current != null) {
            throw new ValidateException("Sign in with Apple callback parameters must be unique");
        }
        return value;
    }

    /**
     * Returns the exact capability manifest frozen by the selected Apple manifest.
     *
     * @return immutable Source authentication, token, revocation, discovery, and JWK Set manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes only capabilities declared by the selected Sign in with Apple variant.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing an Apple-private DTO
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "Sign in with Apple capability must not be null");
        Assert.notNull(context, "Sign in with Apple invocation context must not be null");
        Assert.notNull(timeout, "Sign in with Apple invocation budget must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Sign in with Apple capability is not declared"));
        }
        if (capability.key().equals(SourceAuthentication.INITIATE.key())
                && request instanceof SourceAuthenticationRequest.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (capability.key().equals(SourceAuthentication.COMPLETE.key())
                && request instanceof SourceAuthenticationRequest.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::complete, context, timeout),
                    capability.responseType());
        }
        if (capability.equals(OpenIdClientScheme.TOKEN) && request instanceof TokenRequest tokenRequest) {
            return narrow(token(tokenRequest, context, timeout), capability.responseType());
        }
        if (capability.equals(OpenIdClientScheme.REVOCATION) && request instanceof RevocationRequest revocation) {
            return narrow(revoke(revocation, context, timeout), capability.responseType());
        }
        if (capability.equals(OpenIdClientScheme.DISCOVERY) && request == null) {
            return narrow(discover(context, timeout), capability.responseType());
        }
        if (capability.equals(OpenIdClientScheme.JWK_SET) && request == null) {
            return narrow(jwks(context, timeout), capability.responseType());
        }
        return completed(rejected("Sign in with Apple capability request is invalid"));
    }

    /**
     * Builds Apple's authorization-code request using standard OAuth fields plus registered Apple query extensions.
     *
     * @param initiation generated state and OpenID Connect nonce
     * @param context    immutable invocation context retained for the uniform operation signature
     * @param timeout    shared end-to-end budget
     * @return prepared Apple form-post authorization redirect
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "Sign in with Apple authorization context must not be null");
        Assert.notNull(timeout, "Sign in with Apple authorization budget must not be null");
        try {
            final String nonce = initiation.nonce().getOrNull();
            if (nonce == null || initiation.codeChallenge().isPresent()) {
                return completed(rejected("Sign in with Apple requires nonce and prohibits PKCE"));
            }
            final Map<String, JsonValue> extensions = new LinkedHashMap<>();
            extensions.put("nonce", new JsonValue.StringValue(nonce));
            extensions.put("response_mode", new JsonValue.StringValue("form_post"));
            final AuthorizationRequest request = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                    options.redirectUri(), Optional.of(new Scope(requestedScopes())), Optional.of(initiation.state()),
                    Optional.empty(), Optional.empty(), new JsonValue.ObjectValue(extensions));
            final UnoUrl redirect = authorizationRequestEncoder.encode(request);
            return completed(Outcome.succeeded(new RedirectManager.Prepared(redirect.toString(), initiation.state())));
        } catch (RuntimeException cause) {
            return completed(rejected("Sign in with Apple authorization request is invalid"));
        }
    }

    /**
     * Extracts the unique state from an exact POST form callback.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return unique non-blank state value
     * @throws ValidateException if transport, target, multiplicity, or branch shape is invalid
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Continues a consumed Apple callback after browser correlation succeeds.
     *
     * @param completion consumed callback, nonce, and correlation material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return terminal identity outcome once token and ID Token processing is available
     */
    private CompletionStage<Outcome<ExternalIdentity>> complete(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(completion, "Sign in with Apple browser completion must not be null");
        Assert.notNull(context, "Sign in with Apple completion context must not be null");
        Assert.notNull(timeout, "Sign in with Apple completion budget must not be null");
        final CallbackWire callback;
        try {
            callback = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Sign in with Apple authorization callback is invalid"));
        }
        if (callback.error() != null) {
            return completed(rejected("Sign in with Apple authorization endpoint rejected the request"));
        }
        if (completion.codeVerifier().isPresent() || completion.correlation().nonce().isEmpty()) {
            return completed(failed("Sign in with Apple browser security material violates the registered manifest"));
        }
        final String code = callback.code();
        final TokenRequest request = new TokenRequest(
                new AuthorizationCodeGrant(code, options.redirectUri(), Optional.empty(), Optional.empty()),
                emptyObject());
        return token(request, context, timeout).thenCompose(tokens -> switch (tokens) {
            case Outcome.Succeeded<TokenEndpointResponse> success -> success
                    .value() instanceof OpenIdTokenResponse openId
                            ? verifyToken(openId, callback, completion, context, timeout)
                            : completed(
                                    rejected("Sign in with Apple authorization-code response omitted the ID Token"));
            case Outcome.Rejected<TokenEndpointResponse> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<TokenEndpointResponse> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Executes one standard authorization-code or refresh-token request with Apple's dynamic client credential.
     *
     * @param request standard OAuth token request
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return decoded standard token response
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        if (!valid(request)) {
            return completed(rejected("Sign in with Apple token request differs from the registered OAuth manifest"));
        }
        return clientSecret(context, timeout).thenCompose(secret -> switch (secret) {
            case Outcome.Succeeded<String> success -> CompletableFuture
                    .supplyAsync(() -> sendToken(request, success.value(), timeout), services.executor());
            case Outcome.Rejected<String> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<String> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Validates the complete standard token request against the frozen Apple registration.
     *
     * @param request standard OAuth token request
     * @return whether the request uses one supported grant and exact registration bindings
     */
    private boolean valid(final TokenRequest request) {
        if (request == null || !request.extensions().values().isEmpty()) {
            return false;
        }
        if (request.grant() instanceof AuthorizationCodeGrant grant) {
            return options.redirectUri().equals(grant.redirectUri()) && grant.codeVerifier().isEmpty()
                    && (grant.clientId().isEmpty() || options.clientId().equals(grant.clientId().getOrNull()));
        }
        if (request.grant() instanceof RefreshTokenGrant grant) {
            return grant.scope().isEmpty();
        }
        return false;
    }

    /**
     * Sends one standard Apple token form and decodes exactly one standard OAuth response branch.
     *
     * @param request      validated standard token request
     * @param clientSecret sensitive short-lived Apple client-secret JWT
     * @param timeout      shared end-to-end budget
     * @return standard token response or safely classified failure
     */
    private Outcome<TokenEndpointResponse> sendToken(
            final TokenRequest request,
            final String clientSecret,
            final Timeout.Budget timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Sign in with Apple token request has no remaining time budget");
            }
            final List<Parameter> fields = new ArrayList<>(tokenRequestEncoder.encode(request));
            fields.add(new Parameter(OAuth2.Parameters.CLIENT_ID, options.clientId()));
            fields.add(new Parameter(OAuth2.Parameters.CLIENT_SECRET, clientSecret));
            body = formCodec.encode(fields);
            final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
            final HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.POST)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OIDC).addressPolicy())
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute();
            if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
                response.close();
                return failed(ErrorCode._429, "Sign in with Apple token endpoint rate limited the request");
            }
            if (response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
                response.close();
                return failed(ErrorCode._502, "Sign in with Apple token endpoint is unavailable");
            }
            return switch (tokenResponseDecoder.decode(response)) {
                case TokenResponseDecoder.Success success -> validateToken(success.response(), request.grant());
                case TokenResponseDecoder.Error error -> rejected(
                        "Sign in with Apple token endpoint rejected the request");
            };
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Sign in with Apple token request failed");
        } finally {
            if (body != null) {
                Arrays.fill(body, (byte) 0);
            }
        }
    }

    /**
     * Executes standard RFC 7009 revocation with Apple's dynamic client credential.
     *
     * @param request standard revocation request
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return successful empty response or safely classified failure
     */
    private CompletionStage<Outcome<Void>> revoke(
            final RevocationRequest request,
            final Context context,
            final Timeout.Budget timeout) {
        final String hint = request == null ? null : request.tokenTypeHint().getOrNull();
        if (request == null || hint != null && !OAuth2.Parameters.ACCESS_TOKEN.equals(hint)
                && !OAuth2.Parameters.REFRESH_TOKEN.equals(hint)) {
            return completed(rejected("Sign in with Apple revocation token type hint is not supported"));
        }
        return clientSecret(context, timeout).thenCompose(secret -> switch (secret) {
            case Outcome.Succeeded<String> success -> CompletableFuture
                    .supplyAsync(() -> sendRevocation(request, success.value(), timeout), services.executor());
            case Outcome.Rejected<String> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<String> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Sends one Apple revocation form while retaining the standard empty-success contract.
     *
     * @param request      validated RFC 7009 request
     * @param clientSecret sensitive short-lived Apple client-secret JWT
     * @param timeout      shared end-to-end budget
     * @return empty success, safe rejection, or operational failure
     */
    private Outcome<Void> sendRevocation(
            final RevocationRequest request,
            final String clientSecret,
            final Timeout.Budget timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Sign in with Apple revocation has no remaining time budget");
            }
            final List<Parameter> fields = new ArrayList<>(revocationRequestEncoder.encode(request));
            fields.add(new Parameter(OAuth2.Parameters.CLIENT_ID, options.clientId()));
            fields.add(new Parameter(OAuth2.Parameters.CLIENT_SECRET, clientSecret));
            body = formCodec.encode(fields);
            final String endpoint = variant.targets().resolve(options).revocation().getOrNull().url().toString();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.POST)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OIDC).addressPolicy())
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
                    return failed(ErrorCode._429, "Sign in with Apple revocation endpoint rate limited the request");
                }
                if (response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
                    return failed(ErrorCode._502, "Sign in with Apple revocation endpoint is unavailable");
                }
                if (response.code() >= Http.Status.BAD_REQUEST) {
                    return appleError(response)
                            ? rejected("Sign in with Apple revocation endpoint rejected the request")
                            : failed(
                                    ErrorCode._502,
                                    "Sign in with Apple revocation endpoint returned an invalid error response");
                }
                if (response.code() != Http.Status.OK || response.body().length() != 0L) {
                    return failed(
                            ErrorCode._502,
                            "Sign in with Apple revocation endpoint returned an invalid response");
                }
                return Outcome.succeeded(null);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Sign in with Apple revocation request failed");
        } finally {
            if (body != null) {
                Arrays.fill(body, (byte) 0);
            }
        }
    }

    /**
     * Resolves the configured EC private key and produces a five-minute Apple client-secret JWT.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return dynamic client-secret JWT outcome
     */
    private CompletionStage<Outcome<String>> clientSecret(final Context context, final Timeout.Budget timeout) {
        final Instant now = timeout.clock().now();
        final KeyLoader.Request query = new KeyLoader.Request(ISSUER, Optional.of(options.credential().id()),
                SIGNATURE_USE, JwaAlgorithm.ES256.name(), now);
        final CompletionStage<Outcome<KeyMaterial>> resolution;
        try {
            resolution = org.miaixz.bus.auth.runtime.LoadResult.parse(
                    services.keyLoader().load(query, context, timeout),
                    loaded -> services.keyParser().parse(query, loaded));
        } catch (RuntimeException cause) {
            return completed(failed("Sign in with Apple signing key resolution failed"));
        }
        if (resolution == null) {
            return completed(failed("Sign in with Apple signing key loader returned no stage"));
        }
        return resolution.handle((outcome, cause) -> {
            if (cause != null || outcome == null) {
                return AppleSourceAdapter.<String>failed("Sign in with Apple signing key resolution failed");
            }
            return switch (outcome) {
                case Outcome.Succeeded<KeyMaterial> success -> sign(success.value(), now);
                case Outcome.Rejected<KeyMaterial> rejected -> Outcome.rejected(rejected.failure());
                case Outcome.Failed<KeyMaterial> failed -> Outcome.failed(failed.failure());
            };
        });
    }

    /**
     * Signs the exact Apple client-secret protected header and Claims Set.
     *
     * @param resolved exact externally resolved signing key
     * @param now      shared-clock signing instant
     * @return compact ES256 client-secret JWT or a safe rejection
     */
    private Outcome<String> sign(final KeyMaterial resolved, final Instant now) {
        if (!options.credential().id().equals(resolved.keyId())
                || !JwaAlgorithm.ES256.name().equals(resolved.algorithm())
                || !(resolved.key() instanceof ECPrivateKey privateKey) || now.isBefore(resolved.notBefore())
                || !now.isBefore(resolved.notAfter())
                || resolved.notAfter().isBefore(now.plus(CLIENT_SECRET_LIFETIME))) {
            return rejected("Sign in with Apple signing key does not match the configured key");
        }
        try {
            final Map<String, JsonValue> headers = new LinkedHashMap<>();
            headers.put(JoseHeader.ALGORITHM, new JsonValue.StringValue(JwaAlgorithm.ES256.name()));
            headers.put(JoseHeader.KEY_ID, new JsonValue.StringValue(options.keyId()));
            final Map<String, JsonValue> claims = new LinkedHashMap<>();
            claims.put(JwtClaims.ISSUER, new JsonValue.StringValue(options.teamId()));
            claims.put(JwtClaims.ISSUED_AT, number(now.getEpochSecond()));
            claims.put(JwtClaims.EXPIRATION, number(now.plus(CLIENT_SECRET_LIFETIME).getEpochSecond()));
            claims.put(JwtClaims.AUDIENCE, new JsonValue.StringValue(ISSUER));
            claims.put(JwtClaims.SUBJECT, new JsonValue.StringValue(options.clientId()));
            final byte[] payload = services.jsonProvider().writeValue(new JsonValue.ObjectValue(claims));
            try {
                final var signature = clientSecretJws
                        .sign(JoseHeader.protectedOnly(new JsonValue.ObjectValue(headers)), payload, privateKey);
                return Outcome.succeeded(clientSecretJws.compact(new JwsService.Jws(payload, List.of(signature))));
            } finally {
                java.util.Arrays.fill(payload, (byte) 0);
            }
        } catch (RuntimeException cause) {
            return failed("Sign in with Apple client-secret signing failed");
        }
    }

    /**
     * Retrieves and validates Apple's OpenID Provider Metadata without allowing discovery to rewrite fixed endpoints.
     *
     * @param context immutable invocation context retained for the uniform operation signature
     * @param timeout shared end-to-end budget
     * @return standard issuer-bound OpenID Provider Metadata
     */
    private CompletionStage<Outcome<OpenIdProviderMetadata>> discover(
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(context, "Sign in with Apple discovery context must not be null");
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (timeout.expired()) {
                    return failed(ErrorCode._408, "Sign in with Apple discovery has no remaining time budget");
                }
                final String endpoint = variant.targets().resolve(options).discovery().getOrNull().url().toString();
                final OpenIdProviderMetadata metadata = metadataCodec.decode(
                        Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                                .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                                .addressPolicy(services.securityBaseline().require(Protocol.OIDC).addressPolicy())
                                .execute());
                return validateMetadata(metadata);
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Sign in with Apple discovery request failed");
            }
        }, services.executor());
    }

    /**
     * Binds security-relevant discovery members to the frozen Apple manifest.
     *
     * @param metadata decoded standard OpenID Provider Metadata
     * @return unchanged metadata or a safe rejection
     */
    private Outcome<OpenIdProviderMetadata> validateMetadata(final OpenIdProviderMetadata metadata) {
        try {
            final var resolvedTargets = variant.targets().resolve(options);
            issuerValidator.validate(ISSUER, metadata.issuer());
            if (!resolvedTargets.authorization().getOrNull().url().toString().equals(metadata.authorizationEndpoint())
                    || !resolvedTargets.token().getOrNull().url().toString().equals(metadata.tokenEndpoint())
                    || !resolvedTargets.jwks().getOrNull().url().toString().equals(metadata.jwksUri())
                    || !metadata.responseTypesSupported().contains(ResponseType.CODE)
                    || !metadata.responseModesSupported().contains("form_post")
                    || !metadata.subjectTypesSupported().contains(SubjectType.PUBLIC)
                    || !metadata.idTokenSigningAlgValuesSupported().contains(JwaAlgorithm.RS256)) {
                throw new ValidateException("Sign in with Apple metadata differs from the frozen manifest");
            }
            return Outcome.succeeded(metadata);
        } catch (RuntimeException cause) {
            return rejected("Sign in with Apple discovery metadata does not match the registered Source");
        }
    }

    /**
     * Retrieves Apple's current public JWK Set through the strict standard resource codec.
     *
     * @param context immutable invocation context retained for the uniform operation signature
     * @param timeout shared end-to-end budget
     * @return current standard public JWK Set
     */
    private CompletionStage<Outcome<JwkSet>> jwks(final Context context, final Timeout.Budget timeout) {
        Assert.notNull(context, "Sign in with Apple JWK Set context must not be null");
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (timeout.expired()) {
                    return failed(ErrorCode._408, "Sign in with Apple JWK Set request has no remaining time budget");
                }
                final String endpoint = variant.targets().resolve(options).jwks().getOrNull().url().toString();
                return Outcome.succeeded(
                        jwkSetCodec.decode(
                                Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JWK_SET_JSON)
                                        .timeout(timeout.forFabric())
                                        .addressPolicy(
                                                services.securityBaseline().require(Protocol.OIDC).addressPolicy())
                                        .execute()));
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Sign in with Apple JWK Set request failed");
            }
        }, services.executor());
    }

    /**
     * Verifies the token-endpoint ID Token and an optional independently returned callback ID Token.
     *
     * @param token      standard token endpoint response
     * @param callback   validated callback parameters
     * @param completion consumed browser correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return verified Apple external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> verifyToken(
            final OpenIdTokenResponse token,
            final CallbackWire callback,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final String compact = token.idToken().compact();
        return jwks(context, timeout).thenCompose(keys -> switch (keys) {
            case Outcome.Succeeded<JwkSet> success -> verifyIdToken(
                    compact,
                    success.value(),
                    token.accessToken(),
                    callback.code(),
                    completion,
                    context,
                    timeout).thenCompose(primary -> switch (primary) {
                        case Outcome.Succeeded<IdTokenClaims> claims -> verifyCallbackToken(
                                claims.value(),
                                success.value(),
                                token,
                                callback,
                                completion,
                                context,
                                timeout);
                        case Outcome.Rejected<IdTokenClaims> rejected -> completed(
                                Outcome.rejected(rejected.failure()));
                        case Outcome.Failed<IdTokenClaims> failed -> completed(Outcome.failed(failed.failure()));
                    });
            case Outcome.Rejected<JwkSet> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<JwkSet> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Verifies the optional front-channel ID Token and requires the same subject as the token-endpoint ID Token.
     *
     * @param primary    verified token-endpoint claims
     * @param keys       issuer-bound public keys
     * @param token      standard token response
     * @param callback   validated callback parameters
     * @param completion consumed browser correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return verified Apple external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> verifyCallbackToken(
            final IdTokenClaims primary,
            final JwkSet keys,
            final OpenIdTokenResponse token,
            final CallbackWire callback,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final String frontChannel = callback.idToken();
        if (frontChannel == null) {
            return completed(identity(primary, callback.user(), timeout));
        }
        return verifyIdToken(frontChannel, keys, null, callback.code(), completion, context, timeout)
                .thenApply(verified -> switch (verified) {
                    case Outcome.Succeeded<IdTokenClaims> success -> primary.subject().equals(success.value().subject())
                            ? identity(primary, callback.user(), timeout)
                            : rejected("Sign in with Apple ID Token subjects do not match");
                    case Outcome.Rejected<IdTokenClaims> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<IdTokenClaims> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Selects one exact RSA JWK and applies the complete OIDC ID Token verifier.
     *
     * @param compact     sensitive compact ID Token
     * @param keys        current issuer public JWK Set
     * @param accessToken optional access token for at_hash verification
     * @param code        authorization code for c_hash verification
     * @param completion  consumed nonce and state correlation
     * @param context     immutable invocation context
     * @param timeout     shared end-to-end budget
     * @return cryptographically and semantically verified ID Token claims
     */
    private CompletionStage<Outcome<IdTokenClaims>> verifyIdToken(
            final String compact,
            final JwkSet keys,
            final String accessToken,
            final String code,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final PublicKey key;
        try {
            final JwsService.Jws parsed = idTokenJws.parseCompact(compact, Set.of());
            final JoseHeader header = parsed.signatures().get(0).header();
            if (!JwaAlgorithm.RS256.name().equals(header.algorithm())) {
                throw new ValidateException("Sign in with Apple ID Token must use RS256");
            }
            final String keyId = header.keyId()
                    .orElseThrow(() -> new ValidateException("Sign in with Apple ID Token requires protected kid"));
            final Jwk selected = jwkSelector.requireUnique(
                    keys,
                    new JwkSelector.Selection(header.keyId(), JwaAlgorithm.RS256.name(), JwaAlgorithm.Kind.SIGNATURE,
                            Optional.of("sig"), Optional.of("verify"), Optional.of("RSA")));
            if (selected.keyId().filter(keyId::equals).isEmpty()) {
                throw new ValidateException("Sign in with Apple JWK kid does not match the ID Token");
            }
            key = rsaPublicKey(selected);
        } catch (RuntimeException cause) {
            return completed(rejected("Sign in with Apple ID Token key selection failed"));
        }
        final IdTokenVerifier.Request verification = new IdTokenVerifier.Request(new IdToken(compact),
                new JwtVerifier.Signed(key, Set.of()), ISSUER, options.clientId(),
                completion.correlation().nonce().getOrNull(), Optional.empty(), Optional.ofNullable(accessToken),
                Optional.of(code), Optional.of(completion.correlation().state()));
        return idTokenVerifier.verify(verification, context, timeout);
    }

    /**
     * Maps only verified ID Token claims and optional first-authorization user data into an external identity.
     *
     * @param claims       verified token-endpoint ID Token claims
     * @param callbackUser optional Apple first-authorization user JSON
     * @param timeout      shared clock used to timestamp verification evidence
     * @return verified external identity or safe rejection
     */
    private Outcome<ExternalIdentity> identity(
            final IdTokenClaims claims,
            final String callbackUser,
            final Timeout.Budget timeout) {
        try {
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            copyClaim(claims.extensions(), attributes, "email", JsonValue.StringValue.class);
            copyClaim(claims.extensions(), attributes, "email_verified", JsonValue.BooleanValue.class);
            copyClaim(claims.extensions(), attributes, "is_private_email", JsonValue.BooleanValue.class);
            if (callbackUser != null) {
                final byte[] userBytes = callbackUser.getBytes(Charset.UTF_8);
                if (userBytes.length > Normal.MEBI) {
                    throw new ValidateException("Sign in with Apple callback user exceeds one MiB");
                }
                final JsonValue value = services.jsonProvider().readValue(userBytes, 32, true);
                if (!(value instanceof JsonValue.ObjectValue user)) {
                    throw new ValidateException("Sign in with Apple callback user must be a JSON object");
                }
                for (String member : user.values().keySet()) {
                    if (!"name".equals(member) && !"email".equals(member)) {
                        throw new ValidateException("Sign in with Apple callback user contains an unregistered member");
                    }
                }
                final JsonValue callbackEmail = user.values().get("email");
                if (callbackEmail != null) {
                    if (!(callbackEmail instanceof JsonValue.StringValue email) || email.value().isBlank()) {
                        throw new ValidateException("Sign in with Apple callback email is invalid");
                    }
                    final JsonValue verifiedEmail = attributes.get("email");
                    if (verifiedEmail instanceof JsonValue.StringValue verified
                            && !verified.value().equals(email.value())) {
                        throw new ValidateException("Sign in with Apple callback email does not match ID Token");
                    }
                    attributes.putIfAbsent("email", callbackEmail);
                }
                final JsonValue name = user.values().get("name");
                if (name != null) {
                    if (!(name instanceof JsonValue.ObjectValue object)) {
                        throw new ValidateException("Sign in with Apple callback name is invalid");
                    }
                    for (String member : object.values().keySet()) {
                        if (!"firstName".equals(member) && !"lastName".equals(member)) {
                            throw new ValidateException(
                                    "Sign in with Apple callback name contains an unregistered member");
                        }
                    }
                    final Map<String, JsonValue> checkedName = new LinkedHashMap<>();
                    copyClaim(object, checkedName, "firstName", JsonValue.StringValue.class);
                    copyClaim(object, checkedName, "lastName", JsonValue.StringValue.class);
                    if (!checkedName.isEmpty()) {
                        attributes.put("name", new JsonValue.ObjectValue(checkedName));
                    }
                }
            }
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim(JwtClaims.SUBJECT, new JsonValue.StringValue(claims.subject()), ISSUER,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new ExternalIdentity(sourceId, claims.subject(), new JsonValue.ObjectValue(attributes),
                            List.of(evidence)));
        } catch (RuntimeException cause) {
            return rejected("Sign in with Apple identity claims are invalid");
        }
    }

    /**
     * Validates and indexes one exact Apple form-post callback.
     *
     * @param callback raw inbound callback
     * @return typed private Apple callback value
     * @throws ValidateException if the callback violates the frozen Apple transport or branch contract
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Sign in with Apple callback must not be null");
        final List<String> contentTypes = callback.headers().values(Http.Header.CONTENT_TYPE);
        final String contentType = contentTypes.size() == 1 ? contentTypes.get(0) : null;
        if (!options.redirectUri().getOrNull().equals(callback.requestUri()) || callback.method() != Http.Method.POST
                || contentType == null
                || !MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(MediaType.parse(contentType))) {
            throw new ValidateException("Sign in with Apple callback must use the exact registered POST target");
        }
        String code = null;
        String state = null;
        String idToken = null;
        String user = null;
        String error = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert
                    .notBlank(parameter.value(), "Sign in with Apple callback value must not be blank");
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code, value);
                case OAuth2.Parameters.STATE -> state = unique(state, value);
                case OpenIdConnect.Parameters.ID_TOKEN -> idToken = unique(idToken, value);
                case "user" -> user = unique(user, value);
                case OAuth2.Parameters.ERROR -> error = unique(error, value);
                default -> throw new ValidateException(
                        "Sign in with Apple callback contains an unregistered parameter");
            }
        }
        final boolean success = code != null && error == null;
        final boolean rejected = error != null && code == null && idToken == null && user == null;
        if ((!success && !rejected) || state == null) {
            throw new ValidateException("Sign in with Apple callback must contain one success or error branch");
        }
        return new CallbackWire(code, state, idToken, user, error);
    }

    /**
     * Strictly recognizes one Apple JSON error response without retaining its description or body.
     *
     * @param response owned revocation response inspected before its enclosing resource scope closes
     * @return whether the response is a bounded JSON object with a non-blank {@code error} string
     */
    private boolean appleError(final HttpResponse response) {
        try {
            if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())
                    || response.body().length() > Normal.MEBI) {
                return false;
            }
            final JsonValue value = services.jsonProvider().readValue(response.bytes(Normal.MEBI), 16, true);
            if (!(value instanceof JsonValue.ObjectValue object)) {
                return false;
            }
            for (String member : object.values().keySet()) {
                switch (member) {
                    case OAuth2.Parameters.ERROR, OAuth2.Parameters.ERROR_DESCRIPTION, OAuth2.Parameters.ERROR_URI -> {
                        // Registered Apple error member.
                    }
                    default -> {
                        return false;
                    }
                }
            }
            final JsonValue error = object.values().get(OAuth2.Parameters.ERROR);
            return error instanceof JsonValue.StringValue string && !string.value().isBlank();
        } catch (RuntimeException cause) {
            return false;
        }
    }

    /**
     * Returns explicit registered scopes or the immutable Apple defaults.
     *
     * @return ordered effective Sign in with Apple scopes
     */
    private List<String> requestedScopes() {
        return options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
    }

    /**
     * Carries Apple's private form-post authorization callback without exposing a platform DTO publicly.
     *
     * @param code    authorization code on the success branch
     * @param state   mandatory browser correlation value
     * @param idToken optional front-channel ID Token
     * @param user    optional first-authorization user JSON
     * @param error   registered OAuth error on the rejection branch
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state, String idToken, String user, String error) {

        /**
         * Verifies the already decoded callback branch invariants.
         *
         * @throws ValidateException if state or branch members are inconsistent
         */
        private CallbackWire {
            Assert.notBlank(state, "Sign in with Apple callback state must not be blank");
            final boolean success = code != null && error == null;
            final boolean rejected = error != null && code == null && idToken == null && user == null;
            if (!success && !rejected) {
                throw new ValidateException("Sign in with Apple callback branch is invalid");
            }
        }

    }

}
