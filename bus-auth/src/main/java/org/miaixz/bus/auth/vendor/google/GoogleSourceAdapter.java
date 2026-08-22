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
package org.miaixz.bus.auth.vendor.google;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.guard.IssuerValidator;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.client.AuthorizationClient;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2Client;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.protocol.oauth2.client.TokenClient;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenRequestEncoder;
import org.miaixz.bus.auth.protocol.oauth2.codec.TokenResponseDecoder;
import org.miaixz.bus.auth.protocol.oidc.*;
import org.miaixz.bus.auth.protocol.oidc.client.*;
import org.miaixz.bus.auth.protocol.oidc.codec.*;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.shared.jose.*;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.jwt.JwtVerifier;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VariantManifest;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.worker.loader.SecretLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements the frozen Google OpenID Connect relying-party and Source authentication contract.
 * <p>
 * All Registry-visible protocol operations preserve their standard request and response types. Browser authentication
 * adds only the application-level correlation lifecycle needed to turn a verified ID Token and UserInfo response into
 * an {@link ExternalIdentity}; it never publishes a platform-specific token or profile model.
 * </p>
 *
 * @author Kimi Liu
 */
public class GoogleSourceAdapter implements VendorAdapter {

    /**
     * Trusted Google OpenID Provider issuer.
     */
    private static final String ISSUER = "https://accounts.google.com";

    /**
     * Legacy issuer value explicitly retained by Google's OpenID Connect documentation.
     */
    private static final String LEGACY_ISSUER = "accounts.google.com";

    /**
     * OIDC Discovery extension carrying the RFC 7009 revocation endpoint.
     */
    private static final String REVOCATION_ENDPOINT = OAuth2.Metadata.REVOCATION_ENDPOINT;

    /**
     * OIDC Discovery extension carrying the supported PKCE transformation methods.
     */
    private static final String CODE_CHALLENGE_METHODS = OAuth2.Metadata.CODE_CHALLENGE_METHODS_SUPPORTED;

    /**
     * Discovery extension requiring the RFC 9207 issuer parameter in authorization responses.
     */
    private static final String AUTHORIZATION_RESPONSE_ISSUER = "authorization_response_iss_parameter_supported";

    /**
     * Sole Google token success extension admitted by the frozen web-server manifest.
     */
    private static final String REFRESH_TOKEN_EXPIRES_IN = "refresh_token_expires_in";

    /**
     * Maximum bounded JSON document accepted from Google endpoints.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Maximum object and array nesting accepted from Google endpoint JSON.
     */
    private static final int MAXIMUM_JSON_DEPTH = Normal._16;

    /**
     * Registered Source identifier used for every produced external identity.
     */
    private final String sourceId;

    /**
     * Selected immutable Google variant manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Google Source options.
     */
    private final GoogleOptions options;

    /**
     * Caller-owned runtime dependencies and network resources.
     */
    private final DriverServices services;

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
    private final FormCodec formCodec;

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
     * Creates one Source-bound Google adapter from an immutable Vendor manifest.
     *
     * @param namespaceId registration namespace used to isolate browser state and credentials
     * @param sourceId    registered Source identifier
     * @param manifest    selected Google manifest
     * @param variant     selected default variant manifest
     * @param options     decoded externally loaded Google options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a required collaborator is {@code null}
     * @throws ValidateException        if the supplied profile, manifest, options, or security baseline does not
     *                                  represent the frozen Google OIDC variant
     */
    public GoogleSourceAdapter(final String namespaceId, final String sourceId, final GoogleManifest manifest,
            final VariantManifest.Variant variant, final GoogleOptions options, final DriverServices services) {
        Assert.notNull(manifest, "Google manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Google Source id must not be blank");
        this.variant = Assert.notNull(variant, "Google manifest must not be null");
        this.options = Assert.notNull(options, "Google options must not be null");
        this.services = Assert.notNull(services, "Google execution services must not be null");
        if (!GoogleManifest.ID.equals(manifest.vendor()) || !manifest.variant(GoogleManifest.DEFAULT).equals(variant)
                || !GoogleManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OIDC
                || !options.vendor().equals(GoogleManifest.ID) || !options.variant().equals(GoogleManifest.DEFAULT)) {
            throw new ValidateException("Google adapter requires the google/default OIDC manifest");
        }
        final Set<String> baselineAlgorithms = services.securityBaseline().require(Protocol.OIDC).algorithms();
        if (!baselineAlgorithms.contains(JwaAlgorithm.RS256.name())) {
            throw new ValidateException("Google RS256 is not enabled by the OIDC security baseline");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.formCodec = new FormCodec();
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
                services.securityBaseline().timeGuard(Protocol.OIDC, FabricX.clock(services.fabric())));
        final var targets = variant.targets().resolve(options);
        final OAuth2ClientOptions oauthSettings = new OAuth2ClientOptions(targets.authorization(), targets.token(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(ISSUER),
                options.clientId(), Set.of(options.redirectUri().getOrNull()),
                Endpoint.Authentication.CLIENT_SECRET_POST, Optional.of(options.credential()), true, false);
        final OAuth2Client oauthClient = new OAuth2Client(
                new AuthorizationClient(oauthSettings,
                        new AuthorizationRequestEncoder(targets.authorization().getOrNull())),
                new TokenClient(oauthSettings, services, new TokenRequestEncoder(),
                        new TokenResponseDecoder(services.jsonProvider())),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        final OpenIdClientOptions openIdSettings = new OpenIdClientOptions(oauthSettings, targets.discovery(),
                targets.userInfo(), targets.jwks(), targets.endSession(), Set.of(JwaAlgorithm.RS256));
        final OpenIdClient openIdClient = new OpenIdClient(oauthClient,
                new AuthenticationRequestEncoder(services.jsonProvider()),
                new DiscoveryClient(openIdSettings, services, new OpenIdProviderMetadataCodec(services.jsonProvider())),
                Optional.of(new UserInfoClient(openIdSettings, services, new UserInfoCodec(services.jsonProvider()))),
                Optional.empty());
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OpenIdClientScheme.AUTHENTICATION, openIdClient::authorize),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.TOKEN, openIdClient::token),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.REVOCATION, this::revoke),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.DISCOVERY,
                                (ignored, context, timeout) -> openIdClient.discover(context, timeout)
                                        .thenApply(outcome -> switch (outcome) {
                                            case Outcome.Succeeded<OpenIdProviderMetadata> success -> metadata(
                                                    success.value());
                                            case Outcome.Rejected<OpenIdProviderMetadata> rejected -> Outcome
                                                    .rejected(rejected.failure());
                                            case Outcome.Failed<OpenIdProviderMetadata> failed -> Outcome
                                                    .failed(failed.failure());
                                            default -> throw new IllegalStateException(
                                                    "Unsupported Outcome implementation");
                                        })),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.JWK_SET,
                                (ignored, context, timeout) -> jwks(context, timeout)),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.USERINFO, openIdClient::userInfo)));
    }

    /**
     * Accepts only the two RFC 7009 token type hints registered by Google.
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
     * Classifies one OAuth error as an expected Google token rejection.
     *
     * @param error decoded standard OAuth error
     * @return whether caller, client, grant, or scope state caused the error
     */
    private static boolean rejectedTokenError(final OAuth2ErrorCode error) {
        return OAuth2ErrorCode.INVALID_REQUEST.equals(error) || OAuth2ErrorCode.INVALID_CLIENT.equals(error)
                || OAuth2ErrorCode.INVALID_GRANT.equals(error) || OAuth2ErrorCode.UNAUTHORIZED_CLIENT.equals(error)
                || OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE.equals(error) || OAuth2ErrorCode.INVALID_SCOPE.equals(error);
    }

    /**
     * Verifies the private Google token success vocabulary.
     *
     * @param object decoded token response
     * @return whether every member is registered by the Google token success branch
     */
    private static boolean tokenSuccessMembers(final JsonValue.ObjectValue object) {
        for (String member : object.values().keySet()) {
            switch (member) {
                case OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.TOKEN_TYPE, OAuth2.Parameters.EXPIRES_IN, OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.SCOPE, OpenIdConnect.Parameters.ID_TOKEN, REFRESH_TOKEN_EXPIRES_IN -> {
                    // Registered Google token success member.
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Verifies one standard Google token error plus its optional subtype extension.
     *
     * @param object decoded token error response
     * @return whether error is present and every other member is registered
     */
    private static boolean tokenErrorMembers(final JsonValue.ObjectValue object) {
        if (!object.values().containsKey(OAuth2.Parameters.ERROR) || object.values().size() > 3) {
            return false;
        }
        for (String member : object.values().keySet()) {
            if (!OAuth2.Parameters.ERROR.equals(member) && !OAuth2.Parameters.ERROR_DESCRIPTION.equals(member)
                    && !"error_subtype".equals(member)) {
                return false;
            }
        }
        return true;
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
            throw new ValidateException("Google discovery string extension is invalid");
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
            throw new ValidateException("Google discovery array extension is invalid");
        }
        final ArrayList<String> values = new ArrayList<>(array.values().size());
        for (JsonValue item : array.values()) {
            if (!(item instanceof JsonValue.StringValue string) || string.value().isBlank()
                    || values.contains(string.value())) {
                throw new ValidateException("Google discovery array extension contains an invalid value");
            }
            values.add(string.value());
        }
        return List.copyOf(values);
    }

    /**
     * Reads one mandatory boolean-valued Discovery extension.
     *
     * @param metadata decoded OpenID Provider metadata
     * @param name     exact extension member name
     * @return exact boolean value
     * @throws ValidateException if the extension is absent or not a JSON boolean
     */
    private static boolean extensionBoolean(final OpenIdProviderMetadata metadata, final String name) {
        final JsonValue value = metadata.extensions().values().get(name);
        if (!(value instanceof JsonValue.BooleanValue flag)) {
            throw new ValidateException("Google discovery boolean extension is invalid");
        }
        return flag.value();
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
            throw new ValidateException("Google ID Token requires a public RSA JWK");
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
                .orElseThrow(() -> new ValidateException("Google RSA JWK lacks required member " + name));
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Google RSA JWK member must be a Base64URL string");
        }
        final byte[] decoded = Base64.decode(string.value());
        if (decoded == null || decoded.length == 0) {
            throw new ValidateException("Google RSA JWK integer must not be empty");
        }
        return decoded;
    }

    /**
     * Returns one required JSON member.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return present member value
     * @throws ValidateException if the member is absent
     */
    private static JsonValue required(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            throw new ValidateException("Google response lacks required member: " + name);
        }
        return value;
    }

    /**
     * Reads one required non-blank JSON string.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return non-blank string value
     * @throws ValidateException if the member is absent, blank, or has another JSON type
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = optionalString(object, name);
        if (value == null || value.isBlank()) {
            throw new ValidateException("Google response requires non-blank string member: " + name);
        }
        return value;
    }

    /**
     * Reads one optional JSON string.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return string value or {@code null} when absent
     * @throws ValidateException if a present member has another JSON type
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Google response member must be a JSON string: " + name);
        }
        return string.value();
    }

    /**
     * Reads one positive exact integral JSON number.
     *
     * @param value JSON number candidate
     * @param name  safe member name used in validation messages
     * @return positive exact long value
     * @throws ValidateException if the value is not a positive exact long
     */
    private static long positiveLong(final JsonValue value, final String name) {
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Google response member must be a JSON number: " + name);
        }
        try {
            final long decoded = number.value().longValueExact();
            if (decoded <= 0L) {
                throw new ValidateException("Google response lifetime must be positive: " + name);
            }
            return decoded;
        } catch (ArithmeticException cause) {
            throw new ValidateException("Google response lifetime must be an exact long: " + name, cause);
        }
    }

    /**
     * Creates one exact integral JSON number for safe failure details.
     *
     * @param value integral value
     * @return JSON number value
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Clears one transient byte array when present.
     *
     * @param value transient sensitive bytes or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) 0);
        }
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
        return completed(rejected("Google capability is not declared"));
    }

    /**
     * Creates a completed request-shape or unavailable-operation rejection.
     *
     * @param <S> expected successful response type
     * @return completed rejection stage
     */
    private static <S> CompletionStage<Outcome<S>> mismatch() {
        return completed(rejected("Google capability request is invalid"));
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
    private static <T> Outcome<T> failed(final Errors code, final String description) {
        return Outcome.failed(new Outcome.Failure(code, description, emptyObject()));
    }

    /**
     * Creates a safe operational failure with an explicit non-sensitive detail object.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param details     validated OAuth error identifiers and HTTP status only
     * @param <T>         expected successful value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(
            final Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.failed(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Returns the exact frozen Google capability manifest.
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
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed operation outcome without exposing private Vendor response models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "Google capability must not be null");
        Assert.notNull(context, "Google invocation context must not be null");
        Assert.notNull(timeout, "Google invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return missing();
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
        return mismatch();
    }

    /**
     * Executes only the authorization-code and refresh-token grants registered by the Google profile.
     *
     * @param request standard token request carrying one supported grant
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return standard token response after profile-specific success constraints are checked
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout timeout) {
        if (!valid(request)) {
            return completed(rejected("Google token request does not match the registered grant contract"));
        }
        return Outcome
                .mapStage(
                        () -> services.secretLoader().load(
                                new SecretLoader.Request(services.registration(), options.credential()),
                                context,
                                timeout),
                        loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> CompletableFuture.supplyAsync(() -> {
                        try (SecretLease secret = success.value()) {
                            return sendToken(request, secret, timeout);
                        } catch (RuntimeException cause) {
                            return failed(ErrorCode._502, "Google token operation failed");
                        }
                    }, services.executor());
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
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
            if (grant instanceof AuthorizationCodeGrant) {
                if (!(response instanceof OpenIdTokenResponse openId)) {
                    throw new ValidateException("Google authorization-code response requires an ID Token");
                }
                requireToken(openId);
                if (openId.refreshToken().isPresent()) {
                    throw new ValidateException(
                            "Google default authorization request does not permit an offline refresh token");
                }
            } else {
                final TokenResponse oauth = response instanceof OpenIdTokenResponse openId ? openId.tokenResponse()
                        : (TokenResponse) response;
                if (!TokenType.BEARER.equals(oauth.tokenType()) || oauth.expiresIn().isEmpty()
                        || oauth.expiresIn().getOrNull() <= 0L || oauth.scope().isEmpty()
                        || !requestedScopes().equals(oauth.scope().getOrNull().values())) {
                    throw new ValidateException("Google refresh response is missing its required token members");
                }
            }
            return Outcome.succeeded(response);
        } catch (RuntimeException cause) {
            return rejected("Google token response does not satisfy the registered grant profile");
        }
    }

    /**
     * Runs the private authorization-code exchange and identity resolution under one Client Secret lease.
     *
     * @param request    standard authorization-code token request
     * @param secret     owned Client Secret lease
     * @param response   correlated authorization response
     * @param completion consumed browser security material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return fully verified Google identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> authenticate(
            final TokenRequest request,
            final SecretLease secret,
            final AuthorizationCodeResponse response,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final CompletionStage<Outcome<ExternalIdentity>> stage = CompletableFuture
                .supplyAsync(() -> sendToken(request, secret, timeout), services.executor())
                .thenCompose(token -> switch (token) {
                    case Outcome.Succeeded<TokenEndpointResponse> success -> success
                            .value() instanceof OpenIdTokenResponse openId
                                    ? verifyToken(openId, response, completion, context, timeout)
                                    : completed(rejected("Google authorization-code response omitted the ID Token"));
                    case Outcome.Rejected<TokenEndpointResponse> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<TokenEndpointResponse> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
        return stage.whenComplete((ignored, failure) -> secret.close());
    }

    /**
     * Sends one exact Google authorization-code or refresh-token form under an open secret lease.
     *
     * @param request validated standard token request
     * @param secret  open Client Secret lease
     * @param timeout shared end-to-end timeout
     * @return standard token response or safely classified failure
     */
    private Outcome<TokenEndpointResponse> sendToken(
            final TokenRequest request,
            final SecretLease secret,
            final Timeout timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Google token request has no remaining timeout");
            }
            body = formCodec.encode(tokenParameters(request, secret));
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            try (Response response = FabricX.http(services.fabric(), Protocol.OIDC, timeout)
                    .url(endpoint.url().toString()).method(Http.Method.POST)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response, request.grant());
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Google token endpoint request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Produces the frozen Google token form order for one supported standard grant.
     *
     * @param request validated token request
     * @param secret  open Client Secret lease
     * @return immutable ordered form parameters
     */
    private List<NameValue> tokenParameters(final TokenRequest request, final SecretLease secret) {
        final ArrayList<NameValue> parameters = new ArrayList<>();
        if (request.grant() instanceof AuthorizationCodeGrant grant) {
            parameters.add(new NameValue(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()));
            parameters.add(new NameValue(OAuth2.Parameters.CODE, grant.code()));
            parameters.add(new NameValue(OAuth2.Parameters.REDIRECT_URI, grant.redirectUri().getOrNull()));
            parameters.add(new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()));
            parameters.add(new NameValue(OAuth2.Parameters.CODE_VERIFIER, grant.codeVerifier().getOrNull()));
        } else if (request.grant() instanceof RefreshTokenGrant grant) {
            parameters.add(new NameValue(OAuth2.Parameters.GRANT_TYPE, GrantType.REFRESH_TOKEN.value()));
            parameters.add(new NameValue(OAuth2.Parameters.REFRESH_TOKEN, grant.refreshToken()));
            grant.scope().ifPresent(scope -> parameters.add(new NameValue(OAuth2.Parameters.SCOPE, scope.format())));
            parameters.add(new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()));
        } else {
            throw new ValidateException("Google token request uses an unsupported grant");
        }
        parameters.add(new NameValue(OAuth2.Parameters.CLIENT_SECRET, new String(secret.material())));
        return List.copyOf(parameters);
    }

    /**
     * Strictly decodes one Google token endpoint response.
     *
     * @param response owned endpoint response
     * @param grant    exact submitted standard grant
     * @return standard token response or classified error
     */
    private Outcome<TokenEndpointResponse> token(final Response response, final TokenRequest.Grant grant) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (response.code() == Http.Status.OK) {
                if (!tokenSuccessMembers(object)) {
                    throw new ValidateException("Google token success contains an unknown member");
                }
                final JsonValue extension = object.values().get(REFRESH_TOKEN_EXPIRES_IN);
                final Map<String, JsonValue> extensions = new LinkedHashMap<>();
                if (extension != null) {
                    positiveLong(extension, REFRESH_TOKEN_EXPIRES_IN);
                    extensions.put(REFRESH_TOKEN_EXPIRES_IN, extension);
                }
                final String idToken = optionalString(object, OpenIdConnect.Parameters.ID_TOKEN);
                if (idToken != null) {
                    extensions.put(OpenIdConnect.Parameters.ID_TOKEN, new JsonValue.StringValue(idToken));
                }
                final TokenResponse decoded = new TokenResponse(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN),
                        new TokenType(requiredString(object, OAuth2.Parameters.TOKEN_TYPE)),
                        Optional.of(
                                positiveLong(
                                        required(object, OAuth2.Parameters.EXPIRES_IN),
                                        OAuth2.Parameters.EXPIRES_IN)),
                        Optional.ofNullable(optionalString(object, OAuth2.Parameters.REFRESH_TOKEN)),
                        Optional.of(Scope.parse(requiredString(object, OAuth2.Parameters.SCOPE))),
                        new JsonValue.ObjectValue(extensions));
                return token(idToken == null ? decoded : OpenIdTokenResponse.from(decoded), grant);
            }
            return tokenError(response.code(), object);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Google token endpoint returned an invalid response");
        }
    }

    /**
     * Classifies Google's strict standard token error plus optional {@code error_subtype} extension.
     *
     * @param status HTTP response status
     * @param object decoded error object
     * @return rejected client error or failed upstream error
     */
    private Outcome<TokenEndpointResponse> tokenError(final int status, final JsonValue.ObjectValue object) {
        if (!tokenErrorMembers(object)) {
            throw new ValidateException("Google token error envelope is invalid");
        }
        final OAuth2ErrorCode error = new OAuth2ErrorCode(requiredString(object, OAuth2.Parameters.ERROR));
        optionalString(object, OAuth2.Parameters.ERROR_DESCRIPTION);
        optionalString(object, "error_subtype");
        final Map<String, JsonValue> details = Map
                .of(Builder.OAUTH_ERROR, new JsonValue.StringValue(error.value()), "status", number(status));
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "Google token endpoint rate limited the request", details);
        }
        if (status >= Http.Status.INTERNAL_SERVER_ERROR || OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE.equals(error)) {
            return failed(ErrorCode._502, "Google token endpoint returned an upstream error", details);
        }
        return rejectedTokenError(error)
                ? Outcome.rejected(
                        new Outcome.Failure(ErrorCode._400, "Google token endpoint rejected the request",
                                new JsonValue.ObjectValue(details)))
                : failed(ErrorCode._502, "Google token endpoint returned an unknown OAuth error", details);
    }

    /**
     * Validates one public Google token request and its exact supported standard grant shape.
     *
     * @param request standard token request
     * @return whether the request can be emitted without adding or dropping a parameter
     */
    private boolean valid(final TokenRequest request) {
        if (request == null || !request.extensions().values().isEmpty()) {
            return false;
        }
        if (request.grant() instanceof AuthorizationCodeGrant grant) {
            final String clientId = grant.clientId().getOrNull();
            return options.redirectUri().equals(grant.redirectUri())
                    && (clientId == null || options.clientId().equals(clientId)) && grant.codeVerifier().isPresent();
        }
        if (request.grant() instanceof RefreshTokenGrant grant) {
            final Scope scope = grant.scope().getOrNull();
            return scope == null || requestedScopes().equals(scope.values());
        }
        return false;
    }

    /**
     * Executes Google's unauthenticated RFC 7009-compatible revocation form.
     *
     * @param request standard revocation request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return empty standard success or safely classified failure
     */
    private CompletionStage<Outcome<Void>> revoke(
            final RevocationRequest request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(context, "Google revocation context must not be null");
        if (request == null || !validHint(request)) {
            return completed(rejected("Google revocation token type hint is unsupported"));
        }
        return CompletableFuture.supplyAsync(() -> sendRevocation(request, timeout), services.executor());
    }

    /**
     * Sends one exact Google revocation request without resolving or transmitting a Client Secret.
     *
     * @param request validated standard revocation request
     * @param timeout shared end-to-end timeout
     * @return empty success or classified failure
     */
    private Outcome<Void> sendRevocation(final RevocationRequest request, final Timeout timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Google revocation has no remaining timeout");
            }
            final ArrayList<NameValue> parameters = new ArrayList<>();
            parameters.add(new NameValue("token", request.token()));
            request.tokenTypeHint()
                    .ifPresent(hint -> parameters.add(new NameValue(OAuth2.Parameters.TOKEN_TYPE_HINT, hint)));
            body = formCodec.encode(parameters);
            final var endpoint = variant.targets().resolve(options).revocation().getOrNull();
            try (Response response = FabricX.http(services.fabric(), Protocol.OIDC, timeout)
                    .url(endpoint.url().toString()).method(Http.Method.POST)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                if (response.code() == Http.Status.OK) {
                    return response.body().length() == 0L ? Outcome.succeeded(null)
                            : failed(ErrorCode._502, "Google revocation success body must be empty");
                }
                final Map<String, JsonValue> details = revocationDetails(response);
                if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
                    return failed(ErrorCode._429, "Google revocation endpoint rate limited the request", details);
                }
                if (response.code() >= Http.Status.INTERNAL_SERVER_ERROR) {
                    return failed(ErrorCode._502, "Google revocation endpoint returned an upstream error", details);
                }
                return response.code() == Http.Status.BAD_REQUEST
                        ? Outcome.rejected(
                                new Outcome.Failure(ErrorCode._400, "Google revocation endpoint rejected the request",
                                        new JsonValue.ObjectValue(details)))
                        : failed(ErrorCode._502, "Google revocation endpoint returned an invalid status", details);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Google revocation request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Extracts only a safe Google revocation error token and HTTP status.
     *
     * @param response owned non-success response
     * @return immutable non-sensitive failure details
     */
    private Map<String, JsonValue> revocationDetails(final Response response) {
        final Map<String, JsonValue> details = new LinkedHashMap<>();
        details.put("status", number(response.code()));
        if (response.body().length() > 0L && MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            final JsonValue.ObjectValue object = object(response);
            if (object.values().size() != 1 || !object.values().containsKey(OAuth2.Parameters.ERROR)) {
                throw new ValidateException("Google revocation error envelope is invalid");
            }
            details.put(
                    Builder.OAUTH_ERROR,
                    new JsonValue.StringValue(requiredString(object, OAuth2.Parameters.ERROR)));
        }
        return Map.copyOf(details);
    }

    /**
     * Builds a standard OIDC Authentication Request containing the generated nonce and mandatory S256 challenge.
     *
     * @param initiation generated one-time browser security material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return standard-client-produced authorization URL bound to the generated state
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout timeout) {
        try {
            final List<String> requestedScopes = requestedScopes();
            final var challenge = initiation.codeChallenge().getOrNull();
            final String nonce = initiation.nonce().getOrNull();
            if (challenge == null || nonce == null || !PkceMethod.S256.equals(challenge.method())) {
                return completed(
                        failed(
                                ErrorCode._500,
                                "Google browser flow did not generate required nonce and S256 PKCE material"));
            }
            final AuthorizationRequest authorization = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                    options.redirectUri(), Optional.of(new Scope(requestedScopes)), Optional.of(initiation.state()),
                    Optional.of(challenge.value()), Optional.of(PkceMethod.S256.value()), emptyObject());
            final AuthenticationRequest authentication = new AuthenticationRequest(authorization, Optional.of(nonce),
                    Optional.empty(), Optional.empty(), Optional.empty(), List.of(), Optional.empty(), Optional.empty(),
                    List.of(), Optional.empty(), Optional.empty(), emptyObject());
            return standardAdapter.invoke(OpenIdClientScheme.AUTHENTICATION, authentication, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<Url> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<Url> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<Url> failed -> Outcome.failed(failed.failure());
                        default -> throw new IllegalStateException("Unsupported Outcome implementation");
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Google Authentication Request is invalid"));
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
        return callback(callback).state();
    }

    /**
     * Redeems a correlated authorization response and verifies ID Token, UserInfo, and their common subject.
     *
     * @param completion consumed callback, nonce, and PKCE verifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return fully verified Google external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(rejected("Google authorization callback is invalid"));
        }
        if (values.failed()) {
            final Map<String, JsonValue> details = Map
                    .of(Builder.OAUTH_ERROR, new JsonValue.StringValue(values.error()));
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400,
                                    "Google authorization endpoint returned a standard error",
                                    new JsonValue.ObjectValue(details))));
        }
        final AuthorizationCodeResponse response = new AuthorizationCodeResponse(values.code(),
                Optional.of(values.state()),
                new JsonValue.ObjectValue(Map.of(OAuth2.Parameters.ISS, new JsonValue.StringValue(values.issuer()))));
        final String verifier = completion.codeVerifier().isPresent() ? completion.codeVerifier().getOrNull().value()
                : null;
        if (verifier == null || completion.correlation().nonce().isEmpty()) {
            return completed(
                    failed(ErrorCode._500, "Google browser correlation lacks required nonce or PKCE verifier"));
        }
        final TokenRequest tokenRequest = new TokenRequest(new AuthorizationCodeGrant(response.code(),
                options.redirectUri(), Optional.empty(), Optional.of(verifier)), emptyObject());
        return Outcome
                .mapStage(
                        () -> services.secretLoader().load(
                                new SecretLoader.Request(services.registration(), options.credential()),
                                context,
                                timeout),
                        loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(
                            tokenRequest,
                            success.value(),
                            response,
                            completion,
                            context,
                            timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Retrieves the current issuer-bound JWK Set before selecting and verifying the returned ID Token.
     *
     * @param token      standard token endpoint response
     * @param response   correlated authorization response
     * @param completion consumed browser correlation material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified identity operation stage
     */
    private CompletionStage<Outcome<ExternalIdentity>> verifyToken(
            final OpenIdTokenResponse token,
            final AuthorizationCodeResponse response,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final String compact;
        try {
            requireToken(token);
            compact = token.idToken().compact();
        } catch (RuntimeException cause) {
            return completed(rejected("Google token response does not satisfy the registered OIDC manifest"));
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
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
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
     * @param timeout    shared end-to-end timeout
     * @return identity stage after ID Token and UserInfo verification
     */
    private CompletionStage<Outcome<ExternalIdentity>> verifyIdToken(
            final OpenIdTokenResponse token,
            final String compact,
            final JwkSet keys,
            final AuthorizationCodeResponse response,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final PublicKey key;
        final String tokenIssuer;
        try {
            final JwsService.Jws parsed = jwsService.parseCompact(compact, Set.of());
            final JoseHeader header = parsed.signatures().get(0).header();
            if (!JwaAlgorithm.RS256.name().equals(header.algorithm())) {
                throw new ValidateException("Google ID Token must use RS256");
            }
            final String keyId = header.keyId()
                    .orElseThrow(() -> new ValidateException("Google ID Token requires a protected kid"));
            final Jwk selected = jwkSelector.requireUnique(
                    keys,
                    new JwkSelector.Selection(header.keyId(), JwaAlgorithm.RS256.name(), JwaAlgorithm.Kind.SIGNATURE,
                            Optional.of(Builder.SIGNATURE), Optional.of(Builder.VERIFY), Optional.of("RSA")));
            if (selected.keyId().filter(keyId::equals).isEmpty()
                    || selected.algorithm().filter(JwaAlgorithm.RS256.name()::equals).isEmpty()
                    || selected.publicKeyUse().filter(Builder.SIGNATURE::equals).isEmpty()) {
                throw new ValidateException("Google JWK must explicitly bind kid, alg=RS256, and use=sig");
            }
            key = rsaPublicKey(selected);
            tokenIssuer = idTokenIssuer(parsed);
        } catch (RuntimeException cause) {
            return completed(rejected("Google ID Token key selection failed"));
        }
        final IdTokenVerifier.Request verification = new IdTokenVerifier.Request(new IdToken(compact),
                new JwtVerifier.Signed(key, Set.of()), tokenIssuer, options.clientId(),
                completion.correlation().nonce().getOrNull(), Optional.empty(), Optional.of(token.accessToken()),
                Optional.of(response.code()), Optional.of(completion.correlation().state()));
        return idTokenVerifier.verify(verification, context, timeout).thenCompose(claims -> switch (claims) {
            case Outcome.Succeeded<IdTokenClaims> success -> userInfo(token, success.value(), context, timeout);
            case Outcome.Rejected<IdTokenClaims> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<IdTokenClaims> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Retrieves standard UserInfo and requires its subject to equal the cryptographically verified ID Token subject.
     *
     * @param token   standard token response containing the bearer access token
     * @param claims  verified typed ID Token claims
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> userInfo(
            final OpenIdTokenResponse token,
            final IdTokenClaims claims,
            final Context context,
            final Timeout timeout) {
        return standardAdapter
                .invoke(OpenIdClientScheme.USERINFO, new UserInfoRequest(token.accessToken()), context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<UserInfoResponse> success -> identity(success.value(), claims, timeout);
                    case Outcome.Rejected<UserInfoResponse> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<UserInfoResponse> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
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
            final Timeout timeout) {
        if (!claims.subject().equals(userInfo.subject())) {
            return rejected("Google UserInfo subject does not match the verified ID Token");
        }
        final Map<String, JsonValue> attributes = new LinkedHashMap<>();
        try {
            identityAttribute(userInfo, claims, attributes, "name");
            identityAttribute(userInfo, claims, attributes, "given_name");
            identityAttribute(userInfo, claims, attributes, "family_name");
            identityAttribute(userInfo, claims, attributes, "picture");
            identityAttribute(userInfo, claims, attributes, "email");
            identityAttribute(userInfo, claims, attributes, "locale");
            identityAttribute(userInfo, claims, attributes, "hd");
            final JsonValue verified = userInfo.claims().values().containsKey("email_verified")
                    ? userInfo.claims().values().get("email_verified")
                    : claims.extensions().values().get("email_verified");
            if (verified != null) {
                if (!(verified instanceof JsonValue.BooleanValue)) {
                    throw new ValidateException("Google email_verified identity attribute must be boolean");
                }
                attributes.put("email_verified", verified);
            }
        } catch (RuntimeException cause) {
            return rejected("Google identity attributes are invalid");
        }
        final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                new Evidence.Claim(JwtClaims.SUBJECT, new JsonValue.StringValue(claims.subject()), ISSUER,
                        timeout.clock().now()));
        return Outcome.succeeded(
                new ExternalIdentity(sourceId, claims.subject(), new JsonValue.ObjectValue(attributes),
                        List.of(evidence)));
    }

    /**
     * Copies one verified Google identity string with UserInfo taking precedence over ID Token extensions.
     *
     * @param userInfo   standard subject-bound UserInfo response
     * @param claims     verified ID Token claims
     * @param attributes identity attribute destination
     * @param name       exact registered claim name
     * @throws ValidateException if a present value is blank or not a JSON string
     */
    private void identityAttribute(
            final UserInfoResponse userInfo,
            final IdTokenClaims claims,
            final Map<String, JsonValue> attributes,
            final String name) {
        final JsonValue value = userInfo.claims().values().containsKey(name) ? userInfo.claims().values().get(name)
                : claims.extensions().values().get(name);
        if (value != null) {
            if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
                throw new ValidateException("Google identity attribute must be a non-blank string");
            }
            attributes.put(name, value);
        }
    }

    /**
     * Delegates OpenID Provider Discovery and binds every security-relevant returned endpoint and capability to the
     * frozen Google manifest.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return standard metadata only after all fixed-profile bindings succeed
     */
    private CompletionStage<Outcome<OpenIdProviderMetadata>> discover(final Context context, final Timeout timeout) {
        return standardAdapter.invoke(OpenIdClientScheme.DISCOVERY, null, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<OpenIdProviderMetadata> success -> metadata(success.value());
                    case Outcome.Rejected<OpenIdProviderMetadata> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<OpenIdProviderMetadata> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Validates Google metadata without allowing remote discovery to rewrite the compiled profile.
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
                    || !metadata.scopesSupported().containsAll(List.of("openid", "profile", "email"))
                    || !metadata.idTokenSigningAlgValuesSupported().contains(JwaAlgorithm.RS256)
                    || !metadata.tokenEndpointAuthMethodsSupported()
                            .contains(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    || !extension(metadata, REVOCATION_ENDPOINT)
                            .equals(resolvedTargets.revocation().getOrNull().url().toString())
                    || !extensionBoolean(metadata, AUTHORIZATION_RESPONSE_ISSUER)
                    || !extensionArray(metadata, CODE_CHALLENGE_METHODS).contains(PkceMethod.S256.value())) {
                throw new ValidateException("Google discovery metadata differs from the frozen manifest");
            }
            return Outcome.succeeded(metadata);
        } catch (RuntimeException cause) {
            return rejected("Google discovery metadata does not match the registered Source");
        }
    }

    /**
     * Enforces the token success constraints required by the frozen Google OIDC manifest.
     *
     * @param token decoded standard token response
     * @throws ValidateException if token type, lifetime, ID Token, or returned scope is invalid
     */
    private void requireToken(final OpenIdTokenResponse token) {
        Assert.notNull(token, "Google token response must not be null");
        if (!TokenType.BEARER.equals(token.tokenType()) || token.expiresIn().isEmpty()
                || token.expiresIn().getOrNull() <= 0L || token.scope().isEmpty()) {
            throw new ValidateException("Google authorization-code response requires Bearer, expires_in, and id_token");
        }
        final Scope returned = token.scope().getOrNull();
        if (!requestedScopes().equals(returned.values())) {
            throw new ValidateException("Google token response scope omits a requested scope");
        }
    }

    /**
     * Returns the explicit registered scopes or the immutable manifest defaults.
     *
     * @return ordered effective Google scopes
     */
    private List<String> requestedScopes() {
        return options.scopes().isEmpty() ? variant.defaultScopes() : options.scopes();
    }

    /**
     * Retrieves and strictly decodes the configured Google public JWK Set resource.
     *
     * @param context immutable invocation context retained for the uniform operation signature
     * @param timeout shared end-to-end timeout
     * @return current issuer public JWK Set or a closed framework failure
     */
    private CompletionStage<Outcome<JwkSet>> jwks(final Context context, final Timeout timeout) {
        Assert.notNull(context, "Google JWK Set context must not be null");
        Assert.notNull(timeout, "Google JWK Set timeout must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Google JWK Set request has no remaining timeout"));
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (timeout.expired()) {
                    return failed(ErrorCode._408, "Google JWK Set request exhausted its timeout");
                }
                final String endpoint = variant.targets().resolve(options).jwks().getOrNull().url().toString();
                return Outcome.succeeded(
                        jwkSetCodec.decode(
                                FabricX.http(services.fabric(), Protocol.OIDC, timeout).url(endpoint)
                                        .method(Http.Method.GET).execute()));
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Google JWK Set endpoint request failed");
            }
        }, services.executor());
    }

    /**
     * Reads the unverified issuer only to select between Google's two explicitly allow-listed issuer values.
     * <p>
     * The selected value is subsequently authenticated and compared by {@link IdTokenVerifier}; this preliminary read
     * never establishes trust in the payload.
     * </p>
     *
     * @param parsed structurally valid compact JWS
     * @return exact expected or documented legacy issuer
     * @throws ValidateException if the payload or issuer is not one of the two frozen values
     */
    private String idTokenIssuer(final JwsService.Jws parsed) {
        byte[] payload = null;
        try {
            payload = parsed.payload();
            final JsonValue value = services.jsonProvider().readValue(payload, MAXIMUM_JSON_DEPTH, true);
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("Google ID Token payload must be a JSON object");
            }
            final String issuer = requiredString(object, JwtClaims.ISSUER);
            if (!ISSUER.equals(issuer) && !LEGACY_ISSUER.equals(issuer)) {
                throw new ValidateException("Google ID Token issuer is not allow-listed");
            }
            return issuer;
        } finally {
            clear(payload);
        }
    }

    /**
     * Validates the exact registered callback URI before applying the standard response decoder.
     *
     * @param callback raw inbound callback
     * @return typed exact authorization callback branch
     * @throws ValidateException if the callback target differs from the registered redirect URI
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Google callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("Google callback transport or URI is invalid");
        }
        String code = null;
        String state = null;
        String issuer = null;
        String error = null;
        String errorDescription = null;
        for (Callback.Parameter parameter : callback.parameters()) {
            final String value = Assert.notBlank(parameter.value(), "Google callback value must not be blank");
            switch (parameter.name()) {
                case OAuth2.Parameters.CODE -> code = unique(code, value);
                case OAuth2.Parameters.STATE -> state = unique(state, value);
                case JwtClaims.ISSUER -> issuer = unique(issuer, value);
                case OAuth2.Parameters.ERROR -> error = unique(error, value);
                case OAuth2.Parameters.ERROR_DESCRIPTION -> errorDescription = unique(errorDescription, value);
                default -> throw new ValidateException("Google callback contains an unsupported parameter");
            }
        }
        return new CallbackWire(code, state, issuer, error, errorDescription);
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
            throw new ValidateException("Google callback parameter names must be unique");
        }
        return value;
    }

    /**
     * Strictly reads one bounded Google JSON response object.
     *
     * @param response response whose body remains owned by the caller
     * @return provider-neutral immutable JSON object
     * @throws ValidateException if media type, size, JSON syntax, duplicate names, or root shape is invalid
     */
    private JsonValue.ObjectValue object(final Response response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Google response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Google response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries one exact Google authorization response branch with RFC 9207 issuer binding.
     *
     * @param code             authorization code
     * @param state            browser correlation value
     * @param issuer           authorization response issuer
     * @param error            OAuth error
     * @param errorDescription optional OAuth error description
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String state, String issuer, String error, String errorDescription) {

        /**
         * Validates one exact success or OAuth error branch and the trusted Google issuer.
         */
        private CallbackWire {
            Assert.notBlank(state, "Google callback state must not be blank");
            if (!ISSUER.equals(issuer)) {
                throw new ValidateException("Google callback issuer is invalid");
            }
            final boolean success = code != null && error == null && errorDescription == null;
            final boolean failure = code == null && error != null;
            if (!success && !failure) {
                throw new ValidateException("Google callback has an invalid success or error branch");
            }
        }

        /**
         * Reports whether the callback carries a standard OAuth error.
         *
         * @return {@code true} for an error branch
         */
        private boolean failed() {
            return error != null;
        }

    }

}
