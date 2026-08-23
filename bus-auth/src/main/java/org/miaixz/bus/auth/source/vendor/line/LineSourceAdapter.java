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
package org.miaixz.bus.auth.source.vendor.line;

import java.security.Key;
import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Identity;
import org.miaixz.bus.auth.Identity.Evidence;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.guard.IssuerValidator;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.shared.jose.*;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.jwt.JwtVerifier;
import org.miaixz.bus.auth.shared.pkce.PkceMethod;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.source.SourceWorkflow;
import org.miaixz.bus.auth.source.protocol.oauth2.*;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientOptions;
import org.miaixz.bus.auth.source.protocol.oauth2.client.OAuth2ClientScheme;
import org.miaixz.bus.auth.source.protocol.oidc.*;
import org.miaixz.bus.auth.source.protocol.oidc.client.DiscoveryClient;
import org.miaixz.bus.auth.source.protocol.oidc.client.IdTokenVerifier;
import org.miaixz.bus.auth.source.protocol.oidc.client.OpenIdClientOptions;
import org.miaixz.bus.auth.source.protocol.oidc.client.OpenIdClientScheme;
import org.miaixz.bus.auth.source.protocol.oidc.codec.IdTokenCodec;
import org.miaixz.bus.auth.source.protocol.oidc.codec.JwkSetCodec;
import org.miaixz.bus.auth.source.protocol.oidc.codec.OpenIdProviderMetadataCodec;
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
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements the frozen LINE Login OpenID Connect relying-party and Source-authentication contract.
 * <p>
 * Public operations retain their standard OIDC and OAuth request and response types. Platform adaptation is limited to
 * LINE's exact authorization order, dual HS256/ES256 web verification, legacy profile resource, and
 * {@code access_token} revocation form. No LINE-private token or profile model crosses the Roster boundary.
 * </p>
 *
 * @author Kimi Liu
 */
public class LineSourceAdapter implements VendorAdapter {

    /**
     * Trusted LINE OpenID Provider issuer.
     */
    private static final String ISSUER = "https://access.line.me";

    /**
     * Maximum access-token lifetime registered by LINE Login, in seconds.
     */
    private static final long MAXIMUM_ACCESS_TOKEN_SECONDS = 30L * 24L * 60L * 60L;

    /**
     * Standard OAuth errors representing rejected LINE token requests.
     */
    private static final Set<OAuth2ErrorCode> REJECTED_TOKEN_ERRORS = Set.of(
            OAuth2ErrorCode.INVALID_REQUEST,
            OAuth2ErrorCode.INVALID_CLIENT,
            OAuth2ErrorCode.INVALID_GRANT,
            OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
            OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE,
            OAuth2ErrorCode.INVALID_SCOPE);

    /**
     * Source identifier copied into verified external identities.
     */
    private final String sourceId;

    /**
     * Selected immutable LINE manifest.
     */
    private final VendorManifest.Variant variant;

    /**
     * Validated externally loaded LINE options.
     */
    private final LineOptions options;

    /**
     * Caller-owned runtime, secret, JSON, crypto, network, clock, and execution dependencies.
     */
    private final DriverServices services;

    /**
     * Unified router for LINE's public standard OIDC and OAuth capabilities.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Standard OpenID Connect Discovery client used before LINE metadata pinning.
     */
    private final DiscoveryClient discoveryClient;

    /**
     * Shared one-time state, nonce, and S256 verifier lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Shared strict UTF-8 form encoder.
     */
    private final FormCodec formCodec;

    /**
     * Strict standard public JWK Set response codec.
     */
    private final JwkSetCodec jwkSetCodec;

    /**
     * Shared exact JWK rotation candidate selector.
     */
    private final JwkSelector jwkSelector;

    /**
     * Profile-scoped JWS parser and verifier restricted to LINE's two registered algorithms.
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
     * Monitor guarding the small issuer-bound JWK Set cache and its expiry instant.
     */
    private final Object jwkCacheMonitor = new Object();

    /**
     * Most recently validated public JWK Set, or {@code null} when response directives prohibit reuse.
     */
    private volatile JwkSet cachedJwkSet;

    /**
     * Exclusive expiry of the cached JWK Set according to the response {@code max-age} directive.
     */
    private volatile Instant cachedJwkSetExpiresAt = Instant.EPOCH;

    /**
     * Creates one Source-bound LINE adapter from the frozen default manifest.
     *
     * @param spaceId  Source space used to isolate browser state and credentials
     * @param sourceId Source identifier
     * @param manifest selected LINE manifest
     * @param variant  exact selected default manifest
     * @param options  decoded externally loaded LINE options
     * @param services caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, options, or the security rules differ from the frozen LINE
     *                                  web profile
     */
    public LineSourceAdapter(final String spaceId, final String sourceId, final LineManifest manifest,
            final VendorManifest.Variant variant, final LineOptions options, final DriverServices services) {
        final LineManifest selectedProfile = Assert.notNull(manifest, "LINE manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "LINE Source id must not be blank");
        this.variant = Assert.notNull(variant, "LINE manifest must not be null");
        this.options = Assert.notNull(options, "LINE options must not be null");
        this.services = Assert.notNull(services, "LINE execution services must not be null");
        if (!LineManifest.ID.equals(selectedProfile.vendor())
                || !selectedProfile.variant(LineManifest.DEFAULT).equals(variant)
                || !LineManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OIDC
                || !LineManifest.ID.equals(options.vendor()) || !LineManifest.DEFAULT.equals(options.variant())
                || options.redirectUri().isEmpty()) {
            throw new ValidateException("LINE adapter requires the line/default OIDC manifest");
        }
        final Set<String> algorithms = services.policies().require(Protocol.OIDC).algorithms();
        if (!algorithms.contains(JwaAlgorithm.HS256.name()) || !algorithms.contains(JwaAlgorithm.ES256.name())) {
            throw new ValidateException("LINE HS256 and ES256 must be permitted by the OIDC security rules");
        }
        this.redirectManager = RedirectManager.create(spaceId, sourceId, variant, options, services);
        final var targets = variant.targets().resolve(options);
        final OAuth2ClientOptions oauthSettings = new OAuth2ClientOptions(targets.authorization(), targets.token(),
                Optional.empty(), Optional.empty(), targets.revocation(), Optional.empty(), Optional.of(ISSUER),
                options.clientId(), Set.of(options.redirectUri().getOrNull()),
                Endpoint.Authentication.CLIENT_SECRET_POST, Optional.of(options.credential()), true, false);
        final OpenIdClientOptions openIdSettings = new OpenIdClientOptions(oauthSettings, targets.discovery(),
                Optional.empty(), targets.jwks(), Optional.empty(), Set.of(JwaAlgorithm.HS256, JwaAlgorithm.ES256));
        this.discoveryClient = new DiscoveryClient(openIdSettings, services, new OpenIdProviderMetadataCodec());
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OpenIdClientScheme.AUTHENTICATION,
                                (request, context, timeout) -> authentication(request)),
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.TOKEN, this::token),
                        new StandardAdapter.Binding<>(OAuth2ClientScheme.REVOCATION, this::revoke),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.DISCOVERY,
                                (ignored, context, timeout) -> discover(context, timeout)),
                        new StandardAdapter.Binding<>(OpenIdClientScheme.JWK_SET,
                                (ignored, context, timeout) -> jwks(timeout))));
        this.formCodec = new FormCodec();
        this.jwkSetCodec = new JwkSetCodec();
        this.jwkSelector = new JwkSelector();
        this.issuerValidator = new IssuerValidator();
        this.jwsService = new JwsService(new org.miaixz.bus.auth.guard.AlgorithmGuard(),
                Set.of(JwaAlgorithm.HS256.name(), JwaAlgorithm.ES256.name()));
        final JweService dormantJwe = new JweService(new org.miaixz.bus.auth.guard.AlgorithmGuard(),
                Set.of(JwaAlgorithm.RSA_OAEP_256.name()), Set.of(JwaAlgorithm.A256GCM.name()));
        this.idTokenVerifier = new IdTokenVerifier(new IdTokenCodec(new JwtVerifier(jwsService, dormantJwe)),
                issuerValidator, new org.miaixz.bus.auth.guard.TimeGuard(FabricX.clock(),
                        services.policies().require(Protocol.OIDC).maximumClockSkew()));
    }

    /**
     * Copies one optional official string ID Token claim into identity attributes.
     *
     * @param claims     verified ID Token extension claims
     * @param attributes destination identity attributes
     * @param name       exact official claim name
     * @throws ValidateException if a present claim is not a string
     */
    private static void copyClaim(
            final JsonValue.ObjectValue claims,
            final Map<String, JsonValue> attributes,
            final String name) {
        final JsonValue value = claims.values().get(name);
        if (value != null) {
            if (!(value instanceof JsonValue.StringValue)) {
                throw new ValidateException("LINE optional ID Token identity claim must be a string");
            }
            attributes.put(name, value);
        }
    }

    /**
     * Converts one selected public LINE P-256 JWK through the bus-crypto EC key primitive.
     *
     * @param jwk selected public EC JWK
     * @return JCA public verification key
     * @throws ValidateException if the curve, coordinates, or private-material boundary is invalid
     */
    private static PublicKey ecPublicKey(final Jwk jwk) {
        if (!"EC".equals(jwk.keyType()) || jwk.hasPrivateMaterial() || !"P-256".equals(requiredJwkString(jwk, "crv"))) {
            throw new ValidateException("LINE ES256 requires a public P-256 JWK");
        }
        final byte[] x = binary(jwk, "x");
        final byte[] y = binary(jwk, "y");
        final byte[] point = new byte[65];
        try {
            if (x.length != 32 || y.length != 32) {
                throw new ValidateException("LINE P-256 coordinates must each contain 32 octets");
            }
            point[0] = 0x04;
            System.arraycopy(x, 0, point, 1, x.length);
            System.arraycopy(y, 0, point, 33, y.length);
            return Keeper.decodeECPoint(point, "secp256r1");
        } finally {
            clear(x);
            clear(y);
            clear(point);
        }
    }

    /**
     * Decodes one required unpadded Base64URL JWK coordinate.
     *
     * @param jwk  source JWK
     * @param name exact coordinate member name
     * @return newly allocated coordinate octets
     * @throws ValidateException if the member is absent, typed incorrectly, or empty
     */
    private static byte[] binary(final Jwk jwk, final String name) {
        final String value = requiredJwkString(jwk, name);
        final byte[] decoded = Base64.decode(value);
        if (decoded.length == 0) {
            throw new ValidateException("LINE JWK coordinate must not be empty");
        }
        return decoded;
    }

    /**
     * Reads one mandatory JWK string member.
     *
     * @param jwk  source JWK
     * @param name exact member name
     * @return non-blank string value
     * @throws ValidateException if absent, blank, or another JSON type
     */
    private static String requiredJwkString(final Jwk jwk, final String name) {
        final JsonValue value = jwk.parameter(name)
                .orElseThrow(() -> new ValidateException("LINE JWK lacks required member " + name));
        if (!(value instanceof JsonValue.StringValue text) || text.value().isBlank()) {
            throw new ValidateException("LINE JWK member must be a non-blank string");
        }
        return text.value();
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
            throw new ValidateException("LINE response requires a non-blank string member");
        }
        return text.value();
    }

    /**
     * Reads one optional LINE JSON string member.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return string value or {@code null} when absent or JSON null
     * @throws ValidateException if a present member has another JSON type
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null || value instanceof JsonValue.NullValue) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue text)) {
            throw new ValidateException("LINE optional response member must be a string");
        }
        return text.value();
    }

    /**
     * Reads one positive exact integral JSON number.
     *
     * @param value decoded JSON value
     * @param name  safe member name
     * @return positive long value
     * @throws ValidateException if absent, non-integral, out of range, or not positive
     */
    private static long positiveLong(final JsonValue value, final String name) {
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("LINE " + name + " must be a JSON number");
        }
        try {
            final long result = number.value().longValueExact();
            if (result <= 0L) {
                throw new ValidateException("LINE " + name + " must be positive");
            }
            return result;
        } catch (ArithmeticException cause) {
            throw new ValidateException("LINE " + name + " must be an exact long", cause);
        }
    }

    /**
     * Reports whether one sensitive token has exactly three non-empty compact JWS segments.
     *
     * @param value candidate compact ID Token
     * @return whether the value has compact JWS structure
     */
    private static boolean compactJwt(final String value) {
        final String[] segments = value.split("\\.", -1);
        return segments.length == 3 && !segments[0].isEmpty() && !segments[1].isEmpty() && !segments[2].isEmpty();
    }

    /**
     * Reads one mandatory non-blank string-valued Discovery extension.
     *
     * @param metadata decoded metadata
     * @param name     exact extension name
     * @return exact extension value
     * @throws ValidateException if absent, blank, or another JSON type
     */
    private static String extension(final OpenIdProviderMetadata metadata, final String name) {
        final JsonValue value = metadata.extensions().values().get(name);
        if (!(value instanceof JsonValue.StringValue text) || text.value().isBlank()) {
            throw new ValidateException("LINE Discovery string extension is invalid");
        }
        return text.value();
    }

    /**
     * Reads one mandatory array of unique non-blank Discovery extension strings.
     *
     * @param metadata decoded metadata
     * @param name     exact extension name
     * @return immutable extension values
     * @throws ValidateException if absent, typed incorrectly, blank, or duplicated
     */
    private static List<String> extensionArray(final OpenIdProviderMetadata metadata, final String name) {
        final JsonValue value = metadata.extensions().values().get(name);
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("LINE Discovery array extension is invalid");
        }
        final List<String> values = new ArrayList<>(array.values().size());
        for (JsonValue item : array.values()) {
            if (!(item instanceof JsonValue.StringValue text) || text.value().isBlank()
                    || values.contains(text.value())) {
                throw new ValidateException("LINE Discovery array extension contains an invalid value");
            }
            values.add(text.value());
        }
        return List.copyOf(values);
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
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
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
     * Creates a safe expected rejection without sensitive details.
     *
     * @param description non-sensitive rejection description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, emptyObject()));
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
     * Creates a safe operational failure with explicitly non-sensitive structured details.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param details     non-sensitive error identifiers
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
     * Creates one immutable implementation-neutral empty JSON object.
     *
     * @return empty JSON object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
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
     * Copies the active channel secret into a transient request value and clears intermediate characters.
     *
     * @param lease open channel-secret lease
     * @return transient secret request value
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
     * Identifies a documented LINE callback rejection.
     *
     * @param error callback error value
     * @return whether the value represents an expected request or user rejection
     */
    private static boolean rejectedCallbackError(final String error) {
        return switch (error) {
            case "INVALID_REQUEST", "ACCESS_DENIED", "UNSUPPORTED_RESPONSE_TYPE", "INVALID_SCOPE", "LOGIN_REQUIRED", "INTERACTION_REQUIRED" -> true;
            default -> false;
        };
    }

    /**
     * Verifies the exact token success members for an initial or refresh response.
     *
     * @param object  decoded token success
     * @param initial whether an ID Token is mandatory
     * @return whether the member shape is exact
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue object, final boolean initial) {
        if (object.values().size() != (initial ? 6 : 5) || !object.values().containsKey(OAuth2.Parameters.ACCESS_TOKEN)
                || !object.values().containsKey(OAuth2.Parameters.REFRESH_TOKEN)
                || !object.values().containsKey(OAuth2.Parameters.TOKEN_TYPE)
                || !object.values().containsKey(OAuth2.Parameters.EXPIRES_IN)
                || !object.values().containsKey(OAuth2.Parameters.SCOPE)) {
            return false;
        }
        return initial == object.values().containsKey(OpenIdConnect.Parameters.ID_TOKEN);
    }

    /**
     * Verifies that a LINE profile contains only documented legacy profile members.
     *
     * @param object decoded profile object
     * @return whether every member has a registered profile meaning
     */
    private static boolean profileMembers(final JsonValue.ObjectValue object) {
        for (String member : object.values().keySet()) {
            final boolean known = switch (member) {
                case "userId", "displayName", "pictureUrl", "statusMessage" -> true;
                default -> false;
            };
            if (!known) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the exact capability manifest frozen by the selected LINE manifest.
     *
     * @return immutable Source-authentication and standard OIDC manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes only the Source and OIDC capabilities declared by LINE's frozen manifest.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing LINE-private token or profile records
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "LINE capability must not be null");
        Assert.notNull(context, "LINE invocation context must not be null");
        Assert.notNull(timeout, "LINE invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("LINE capability is not declared"));
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
        return completed(rejected("LINE capability request is invalid"));
    }

    /**
     * Builds LINE's exact Authentication Request using generated state, nonce, and S256 material.
     *
     * @param initiation generated one-time browser security material
     * @param context    immutable invocation context retained for operation consistency
     * @param timeout    shared end-to-end timeout
     * @return exact redirect and state correlation
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(context, "LINE authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "LINE authorization has no remaining timeout"));
        }
        final var challenge = initiation.codeChallenge().getOrNull();
        final String nonce = initiation.nonce().getOrNull();
        if (challenge == null || nonce == null || !PkceMethod.S256.equals(challenge.method())) {
            return completed(
                    failed(ErrorCode._500, "LINE browser flow did not generate required nonce and S256 PKCE material"));
        }
        final AuthorizationRequest authorization = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                options.redirectUri(), Optional.of(new Scope(options.scopes())), Optional.of(initiation.state()),
                Optional.of(challenge.value()), Optional.of(PkceMethod.S256.value()), emptyObject());
        final AuthenticationRequest authentication = new AuthenticationRequest(authorization, Optional.of(nonce),
                Optional.empty(), Optional.empty(), Optional.empty(), List.of(), Optional.empty(), Optional.empty(),
                List.of(), Optional.empty(), Optional.empty(), emptyObject());
        return standardAdapter.invoke(OpenIdClientScheme.AUTHENTICATION, authentication, context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Url> success -> Outcome
                            .succeeded(new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                    case Outcome.Rejected<Url> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Url> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Encodes one exact public LINE Authentication Request.
     *
     * @param request standard OpenID Connect Authentication Request
     * @return exact authorization URL or a safe rejection
     */
    private CompletionStage<Outcome<Url>> authentication(final AuthenticationRequest request) {
        try {
            if (!valid(request)) {
                return completed(rejected("LINE Authentication Request differs from the registered manifest"));
            }
            final AuthorizationRequest authorization = request.authorizationRequest();
            final Url url = variant.targets().resolve(options).authorization().getOrNull().url().newBuilder()
                    .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                    .query(OAuth2.Parameters.CLIENT_ID, options.clientId())
                    .query(OAuth2.Parameters.REDIRECT_URI, options.redirectUri().getOrNull())
                    .query(OAuth2.Parameters.STATE, authorization.state().getOrNull())
                    .query("nonce", request.nonce().getOrNull())
                    .query(OAuth2.Parameters.SCOPE, authorization.scope().getOrNull().format())
                    .query(OAuth2.Parameters.CODE_CHALLENGE, authorization.codeChallenge().getOrNull())
                    .query(OAuth2.Parameters.CODE_CHALLENGE_METHOD, PkceMethod.S256.value()).build();
            return completed(Outcome.succeeded(url));
        } catch (RuntimeException cause) {
            return completed(rejected("LINE Authentication Request is invalid"));
        }
    }

    /**
     * Validates the exact standard Authentication Request subset accepted by LINE web login.
     *
     * @param request request to inspect
     * @return whether all registered values and required one-time security fields are present
     */
    private boolean valid(final AuthenticationRequest request) {
        if (request == null) {
            return false;
        }
        final AuthorizationRequest authorization = request.authorizationRequest();
        final Scope scope = authorization.scope().getOrNull();
        return ResponseType.CODE.equals(authorization.responseType())
                && options.clientId().equals(authorization.clientId())
                && options.redirectUri().equals(authorization.redirectUri()) && scope != null
                && options.scopes().equals(scope.values()) && authorization.state().isPresent()
                && authorization.codeChallenge().isPresent()
                && PkceMethod.S256.value().equals(authorization.codeChallengeMethod().getOrNull())
                && authorization.extensions().values().isEmpty() && request.nonce().isPresent()
                && request.display().isEmpty() && request.prompt().isEmpty() && request.maxAge().isEmpty()
                && request.uiLocales().isEmpty() && request.idTokenHint().isEmpty() && request.loginHint().isEmpty()
                && request.acrValues().isEmpty() && request.claims().isEmpty() && request.responseMode().isEmpty()
                && request.extensions().values().isEmpty();
    }

    /**
     * Extracts the unique LINE callback state before consuming the browser correlation.
     *
     * @param callback raw inbound callback
     * @return non-blank unique state
     * @throws IllegalArgumentException if the callback or state is absent
     * @throws ValidateException        if callback transport or parameter multiplicity is invalid
     */
    private String state(final Callback.Inbound callback) {
        return Assert.notBlank(parameters(callback).state(), "LINE callback state must not be blank");
    }

    /**
     * Redeems one correlated LINE callback and verifies ID Token and profile subject binding.
     *
     * @param completion consumed state, nonce, and PKCE verifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return fully verified LINE external identity
     */
    private CompletionStage<Outcome<Identity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "LINE authorization callback is malformed"));
        }
        if (values.error() != null) {
            if (rejectedCallbackError(values.error())) {
                return completed(rejected("LINE authorization endpoint rejected the request"));
            }
            return completed(failed(ErrorCode._502, "LINE authorization endpoint returned a server error"));
        }
        final String verifier = completion.codeVerifier().isPresent() ? completion.codeVerifier().getOrNull().value()
                : null;
        final String nonce = completion.correlation().nonce().getOrNull();
        if (verifier == null || nonce == null) {
            return completed(failed(ErrorCode._500, "LINE callback lacks required nonce or PKCE verifier"));
        }
        final TokenRequest tokenRequest = new TokenRequest(new AuthorizationCodeGrant(values.code(),
                options.redirectUri(), Optional.empty(), Optional.of(verifier)), emptyObject());
        final CompletionStage<Outcome<SecretLease>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.secretLoader()
                            .load(new SecretLoader.Request(services.entry(), options.credential()), context, timeout),
                    loaded -> services.secretParser().parse(services.entry(), options.credential(), loaded));
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "LINE channel-secret resolution failed"));
        }
        if (resolution == null) {
            return completed(failed(ErrorCode._502, "LINE channel-secret loader returned no stage"));
        }
        return resolution
                .handle(
                        (outcome, cause) -> cause == null && outcome != null ? outcome
                                : LineSourceAdapter
                                        .<SecretLease>failed(ErrorCode._502, "LINE channel-secret resolution failed"))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(
                            tokenRequest,
                            values.code(),
                            success.value(),
                            completion,
                            context,
                            timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Runs LINE token, ID Token, and profile processing under one owned channel-secret lease.
     *
     * @param request    validated authorization-code token request
     * @param code       consumed authorization code used for optional code-hash validation
     * @param secret     owned channel-secret lease
     * @param completion consumed browser security material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified LINE identity
     */
    private CompletionStage<Outcome<Identity>> authenticate(
            final TokenRequest request,
            final String code,
            final SecretLease secret,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final CompletionStage<Outcome<Identity>> stage = CompletableFuture
                .supplyAsync(() -> sendToken(request, secret, timeout), services.executor())
                .thenCompose(token -> switch (token) {
                    case Outcome.Succeeded<TokenEndpointResponse> success -> success
                            .value() instanceof OpenIdTokenResponse openId
                                    ? verifyToken(openId, code, secret, completion, context, timeout)
                                    : completed(rejected("LINE authorization-code response omitted the ID Token"));
                    case Outcome.Rejected<TokenEndpointResponse> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<TokenEndpointResponse> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
        return stage.whenComplete((ignored, cause) -> secret.close());
    }

    /**
     * Executes only LINE's registered authorization-code and refresh-token grants.
     *
     * @param request standard token request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return strict standard token response or closed failure
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> token(
            final TokenRequest request,
            final Context context,
            final Timeout timeout) {
        if (!valid(request)) {
            return completed(rejected("LINE token request does not match the registered grant contract"));
        }
        final CompletionStage<Outcome<SecretLease>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.secretLoader()
                            .load(new SecretLoader.Request(services.entry(), options.credential()), context, timeout),
                    loaded -> services.secretParser().parse(services.entry(), options.credential(), loaded));
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "LINE channel-secret resolution failed"));
        }
        if (resolution == null) {
            return completed(failed(ErrorCode._502, "LINE channel-secret loader returned no stage"));
        }
        return resolution.thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<SecretLease> success -> CompletableFuture.supplyAsync(() -> {
                try (SecretLease secret = success.value()) {
                    return sendToken(request, secret, timeout);
                } catch (RuntimeException cause) {
                    return failed(ErrorCode._502, "LINE token operation failed");
                }
            }, services.executor());
            case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Validates one public LINE token request without adding or dropping wire fields.
     *
     * @param request standard token request
     * @return whether the request uses one exact supported grant
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
        return request.grant() instanceof RefreshTokenGrant grant && grant.scope().isEmpty();
    }

    /**
     * Sends one exact LINE token form under an open channel-secret lease.
     *
     * @param request validated standard token request
     * @param secret  open channel-secret lease
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
                return failed(ErrorCode._408, "LINE token request has no remaining timeout");
            }
            body = formCodec.encode(tokenParameters(request, secret));
            final var endpoint = variant.targets().resolve(options).token().getOrNull();
            try (Response response = FabricX.http(Protocol.OIDC, timeout, services.policies())
                    .url(endpoint.url().toString()).method(Http.Method.POST)
                    .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response, request.grant() instanceof AuthorizationCodeGrant);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "LINE token endpoint request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Produces LINE's exact token form order for one supported grant.
     *
     * @param request validated standard token request
     * @param secret  open channel-secret lease
     * @return immutable ordered form parameters
     */
    private List<NameValue> tokenParameters(final TokenRequest request, final SecretLease secret) {
        if (request.grant() instanceof AuthorizationCodeGrant grant) {
            return List.of(
                    new NameValue(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()),
                    new NameValue(OAuth2.Parameters.CODE, grant.code()),
                    new NameValue(OAuth2.Parameters.REDIRECT_URI, grant.redirectUri().getOrNull()),
                    new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                    new NameValue(OAuth2.Parameters.CLIENT_SECRET, secret(secret)),
                    new NameValue(OAuth2.Parameters.CODE_VERIFIER, grant.codeVerifier().getOrNull()));
        }
        if (request.grant() instanceof RefreshTokenGrant grant) {
            return List.of(
                    new NameValue(OAuth2.Parameters.GRANT_TYPE, GrantType.REFRESH_TOKEN.value()),
                    new NameValue(OAuth2.Parameters.REFRESH_TOKEN, grant.refreshToken()),
                    new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                    new NameValue(OAuth2.Parameters.CLIENT_SECRET, secret(secret)));
        }
        throw new ValidateException("LINE token request uses an unsupported grant");
    }

    /**
     * Strictly decodes one LINE token success or standard OAuth error response.
     *
     * @param response owned token endpoint response
     * @param initial  whether an authorization-code response must carry an ID Token
     * @return standard token response or safely classified failure
     */
    private Outcome<TokenEndpointResponse> token(final Response response, final boolean initial) {
        try {
            final JsonValue.ObjectValue object = object(response, "token");
            if (response.code() == Http.Status.OK) {
                if (!tokenMembers(object, initial)) {
                    throw new ValidateException("LINE token success members are invalid");
                }
                final String idToken = initial ? requiredString(object, OpenIdConnect.Parameters.ID_TOKEN) : null;
                if (idToken != null && !compactJwt(idToken)) {
                    throw new ValidateException("LINE ID Token must use compact JWS serialization");
                }
                final Map<String, JsonValue> extensions = idToken == null ? Map.of()
                        : Map.of(OpenIdConnect.Parameters.ID_TOKEN, new JsonValue.StringValue(idToken));
                final TokenResponse token = new TokenResponse(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN),
                        new TokenType(requiredString(object, OAuth2.Parameters.TOKEN_TYPE)),
                        Optional.of(
                                positiveLong(
                                        object.values().get(OAuth2.Parameters.EXPIRES_IN),
                                        OAuth2.Parameters.EXPIRES_IN)),
                        Optional.of(requiredString(object, OAuth2.Parameters.REFRESH_TOKEN)),
                        Optional.of(Scope.parse(requiredString(object, OAuth2.Parameters.SCOPE))),
                        new JsonValue.ObjectValue(extensions));
                if (!TokenType.BEARER.equals(token.tokenType())
                        || token.expiresIn().getOrNull() > MAXIMUM_ACCESS_TOKEN_SECONDS
                        || !options.scopes().equals(token.scope().getOrNull().values())) {
                    throw new ValidateException("LINE token success does not match the registered manifest");
                }
                return Outcome.succeeded(initial ? OpenIdTokenResponse.from(token) : token);
            }
            return tokenError(response.code(), object);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "LINE token endpoint returned an invalid response");
        }
    }

    /**
     * Classifies one strict LINE standard OAuth token error.
     *
     * @param status exact HTTP status
     * @param object decoded error object
     * @return rejected client error or failed upstream condition
     */
    private Outcome<TokenEndpointResponse> tokenError(final int status, final JsonValue.ObjectValue object) {
        if (!object.values().containsKey(OAuth2.Parameters.ERROR) || object.values()
                .size() != (object.values().containsKey(OAuth2.Parameters.ERROR_DESCRIPTION) ? 2 : 1)) {
            throw new ValidateException("LINE token error envelope is invalid");
        }
        final OAuth2ErrorCode error = new OAuth2ErrorCode(requiredString(object, OAuth2.Parameters.ERROR));
        optionalString(object, OAuth2.Parameters.ERROR_DESCRIPTION);
        final Map<String, JsonValue> details = Map.of(Builder.OAUTH_ERROR, new JsonValue.StringValue(error.value()));
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "LINE token endpoint rate limited the request", details);
        }
        if (status >= Http.Status.INTERNAL_SERVER_ERROR || OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE.equals(error)) {
            return failed(ErrorCode._502, "LINE token endpoint returned an upstream error", details);
        }
        return REJECTED_TOKEN_ERRORS.contains(error)
                ? Outcome.rejected(
                        new Outcome.Failure(ErrorCode._400, "LINE token endpoint rejected the request",
                                new JsonValue.ObjectValue(details)))
                : failed(ErrorCode._502, "LINE token endpoint returned an unknown OAuth error", details);
    }

    /**
     * Selects LINE's exact HS256 or ES256 verification path before applying complete OIDC claim validation.
     *
     * @param token      strict token response
     * @param code       consumed authorization code
     * @param secret     still-open channel-secret lease for HS256 verification
     * @param completion consumed nonce and state binding
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return identity stage after ID Token and profile verification
     */
    private CompletionStage<Outcome<Identity>> verifyToken(
            final OpenIdTokenResponse token,
            final String code,
            final SecretLease secret,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final String compact = token.idToken().compact();
        final JwsService.Jws parsed;
        final JoseHeader header;
        try {
            parsed = jwsService.parseCompact(compact, Set.of());
            header = parsed.signatures().get(0).header();
            if (!"JWT".equals(header.type().orElse(null)) || !header.unprotectedParameters().values().isEmpty()) {
                throw new ValidateException("LINE ID Token must use a protected typ=JWT header");
            }
        } catch (RuntimeException cause) {
            return completed(rejected("LINE ID Token header is invalid"));
        }
        if (JwaAlgorithm.HS256.name().equals(header.algorithm())) {
            return verifyHs256(token, code, secret, completion, context, timeout, header);
        }
        if (JwaAlgorithm.ES256.name().equals(header.algorithm())) {
            return jwks(timeout).thenCompose(keys -> switch (keys) {
                case Outcome.Succeeded<JwkSet> success -> verifyEs256(
                        token,
                        code,
                        completion,
                        context,
                        timeout,
                        header,
                        success.value(),
                        false);
                case Outcome.Rejected<JwkSet> rejected -> completed(Outcome.rejected(rejected.failure()));
                case Outcome.Failed<JwkSet> failed -> completed(Outcome.failed(failed.failure()));
                default -> throw new IllegalStateException("Unsupported Outcome implementation");
            });
        }
        return completed(rejected("LINE ID Token uses an unsupported algorithm"));
    }

    /**
     * Verifies a LINE web-channel HS256 ID Token with the current channel-secret lease.
     *
     * @param token      strict token response
     * @param code       consumed authorization code
     * @param secret     open channel-secret lease
     * @param completion consumed nonce and state
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param header     parsed protected JOSE header
     * @return identity stage after symmetric verification and profile binding
     */
    private CompletionStage<Outcome<Identity>> verifyHs256(
            final OpenIdTokenResponse token,
            final String code,
            final SecretLease secret,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout,
            final JoseHeader header) {
        if (header.keyId().isPresent() || header.protectedParameters().values().size() != 2
                || !header.protectedParameters().values().containsKey("alg")
                || !header.protectedParameters().values().containsKey("typ")) {
            return completed(rejected("LINE HS256 ID Token header is invalid"));
        }
        byte[] material = null;
        try {
            material = secret(secret).getBytes(Charset.UTF_8);
            final Key key = Keeper.generateKey(Algorithm.HMACSHA256.getValue(), material);
            return verify(token, code, completion, context, timeout, key);
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._500, "LINE HS256 ID Token verification could not be prepared"));
        } finally {
            clear(material);
        }
    }

    /**
     * Selects one exact LINE P-256 key and verifies an ES256 ID Token.
     *
     * @param token      strict token response
     * @param code       consumed authorization code
     * @param completion consumed nonce and state
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param header     parsed protected JOSE header
     * @param keys       current issuer public JWK Set
     * @param refreshed  whether an unknown key has already forced its single allowed refresh
     * @return identity stage after asymmetric verification and profile binding
     */
    private CompletionStage<Outcome<Identity>> verifyEs256(
            final OpenIdTokenResponse token,
            final String code,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout,
            final JoseHeader header,
            final JwkSet keys,
            final boolean refreshed) {
        if (header.protectedParameters().values().size() != 3
                || !header.protectedParameters().values().containsKey("alg")
                || !header.protectedParameters().values().containsKey("typ")
                || !header.protectedParameters().values().containsKey("kid")) {
            return completed(rejected("LINE ES256 ID Token header members are invalid"));
        }
        final String keyId = header.keyId().orElse(null);
        if (keyId == null) {
            return completed(rejected("LINE ES256 ID Token requires kid"));
        }
        final PublicKey key;
        try {
            final Jwk selected = jwkSelector.requireUnique(
                    keys,
                    new JwkSelector.Selection(Optional.of(keyId), JwaAlgorithm.ES256.name(),
                            JwaAlgorithm.Kind.SIGNATURE, Optional.of(Builder.SIGNATURE), Optional.of(Builder.VERIFY),
                            Optional.of("EC")));
            if (selected.keyId().filter(keyId::equals).isEmpty()
                    || selected.algorithm().filter(JwaAlgorithm.ES256.name()::equals).isEmpty()) {
                throw new ValidateException("LINE JWK must explicitly bind kid and alg=ES256");
            }
            key = ecPublicKey(selected);
        } catch (RuntimeException cause) {
            if (refreshed) {
                return completed(rejected("LINE ES256 ID Token key selection failed after refresh"));
            }
            return jwks(timeout, true).thenCompose(refreshedKeys -> switch (refreshedKeys) {
                case Outcome.Succeeded<JwkSet> success -> verifyEs256(
                        token,
                        code,
                        completion,
                        context,
                        timeout,
                        header,
                        success.value(),
                        true);
                case Outcome.Rejected<JwkSet> rejected -> completed(Outcome.rejected(rejected.failure()));
                case Outcome.Failed<JwkSet> failed -> completed(Outcome.failed(failed.failure()));
                default -> throw new IllegalStateException("Unsupported Outcome implementation");
            });
        }
        return verify(token, code, completion, context, timeout, key);
    }

    /**
     * Applies full OpenID Connect issuer, audience, time, nonce, and optional artifact-hash validation.
     *
     * @param token      strict token response
     * @param code       consumed authorization code
     * @param completion consumed nonce and state
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param key        explicit symmetric or public verification key
     * @return identity stage after profile subject binding
     */
    private CompletionStage<Outcome<Identity>> verify(
            final OpenIdTokenResponse token,
            final String code,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout,
            final Key key) {
        final IdTokenVerifier.Request verification = new IdTokenVerifier.Request(token.idToken(),
                new JwtVerifier.Signed(key, Set.of()), ISSUER, options.clientId(),
                completion.correlation().nonce().getOrNull(), Optional.empty(), Optional.of(token.accessToken()),
                Optional.of(code), Optional.of(completion.correlation().state()));
        return idTokenVerifier.verify(verification, context, timeout).thenCompose(claims -> switch (claims) {
            case Outcome.Succeeded<IdTokenClaims> success -> profile(token, success.value(), timeout);
            case Outcome.Rejected<IdTokenClaims> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<IdTokenClaims> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Retrieves LINE's legacy bearer profile after local ID Token verification.
     *
     * @param token   strict token response
     * @param claims  cryptographically verified ID Token claims
     * @param timeout shared end-to-end timeout and evidence clock
     * @return subject-bound external identity
     */
    private CompletionStage<Outcome<Identity>> profile(
            final OpenIdTokenResponse token,
            final IdTokenClaims claims,
            final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (timeout.expired()) {
                    return failed(ErrorCode._408, "LINE profile request has no remaining timeout");
                }
                final var endpoint = variant.targets().resolve(options).userInfo().getOrNull();
                try (Response response = FabricX.http(Protocol.OIDC, timeout, services.policies())
                        .url(endpoint.url().toString()).method(Http.Method.GET)
                        .header(Http.Header.AUTHORIZATION, "Bearer " + token.accessToken())
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).execute()) {
                    return profile(response, claims, timeout);
                }
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "LINE profile request failed");
            }
        }, services.executor());
    }

    /**
     * Strictly maps LINE's profile only after matching its user identifier to the verified subject.
     *
     * @param response owned profile response
     * @param claims   verified ID Token claims
     * @param timeout  shared evidence clock
     * @return verified external identity or safely classified failure
     */
    private Outcome<Identity> profile(final Response response, final IdTokenClaims claims, final Timeout timeout) {
        if (response.code() == Http.Status.UNAUTHORIZED || response.code() == Http.Status.FORBIDDEN) {
            return rejected("LINE profile endpoint rejected the access token");
        }
        if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "LINE profile endpoint rate limited the request");
        }
        if (response.code() != Http.Status.OK) {
            return failed(ErrorCode._502, "LINE profile endpoint returned an invalid status");
        }
        try {
            final JsonValue.ObjectValue object = object(response, "profile");
            if (!profileMembers(object)) {
                throw new ValidateException("LINE profile contains an unregistered member");
            }
            final String userId = requiredString(object, "userId");
            final String displayName = requiredString(object, "displayName");
            if (!claims.subject().equals(userId)) {
                return failed(ErrorCode._502, "LINE profile subject does not match the verified ID Token");
            }
            final String pictureUrl = optionalString(object, "pictureUrl");
            final String statusMessage = optionalString(object, "statusMessage");
            if (pictureUrl != null) {
                final Url picture = Url.parse(pictureUrl);
                if (!Protocol.HTTPS.name.equals(picture.scheme()) || picture.host().isEmpty()
                        || !picture.username().isEmpty() || !picture.password().isEmpty()) {
                    throw new ValidateException("LINE pictureUrl must be a credential-free HTTPS URL");
                }
            }
            final Map<String, JsonValue> attributes = new LinkedHashMap<>();
            attributes.put("displayName", new JsonValue.StringValue(displayName));
            if (pictureUrl != null) {
                attributes.put("pictureUrl", new JsonValue.StringValue(pictureUrl));
            }
            if (statusMessage != null) {
                attributes.put("statusMessage", new JsonValue.StringValue(statusMessage));
            }
            copyClaim(claims.extensions(), attributes, "name");
            copyClaim(claims.extensions(), attributes, "picture");
            copyClaim(claims.extensions(), attributes, "email");
            final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                    new Evidence.Claim(JwtClaims.SUBJECT, new JsonValue.StringValue(claims.subject()), ISSUER,
                            timeout.clock().now()));
            return Outcome.succeeded(
                    new Identity(sourceId, claims.subject(), new JsonValue.ObjectValue(attributes), List.of(evidence)));
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "LINE profile endpoint returned an invalid response");
        }
    }

    /**
     * Executes LINE's exact {@code access_token} revocation form under one channel-secret lease.
     *
     * @param request standard RFC 7009 request
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return standard empty success or safely classified failure
     */
    private CompletionStage<Outcome<Void>> revoke(
            final RevocationRequest request,
            final Context context,
            final Timeout timeout) {
        final String hint = request.tokenTypeHint().getOrNull();
        if (hint != null && !OAuth2.Parameters.ACCESS_TOKEN.equals(hint)) {
            return completed(rejected("LINE revocation only accepts the access_token hint"));
        }
        final CompletionStage<Outcome<SecretLease>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.secretLoader()
                            .load(new SecretLoader.Request(services.entry(), options.credential()), context, timeout),
                    loaded -> services.secretParser().parse(services.entry(), options.credential(), loaded));
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "LINE channel-secret resolution failed"));
        }
        if (resolution == null) {
            return completed(failed(ErrorCode._502, "LINE channel-secret loader returned no stage"));
        }
        return resolution.thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<SecretLease> success -> CompletableFuture.supplyAsync(() -> {
                try (SecretLease secret = success.value()) {
                    return sendRevocation(request, secret, timeout);
                } catch (RuntimeException cause) {
                    return failed(ErrorCode._502, "LINE revocation operation failed");
                }
            }, services.executor());
            case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Sends LINE's exact revocation form and accepts only an HTTP 200 empty body.
     *
     * @param request validated standard revocation request
     * @param secret  open channel-secret lease
     * @param timeout shared end-to-end timeout
     * @return standard empty success or safely classified failure
     */
    private Outcome<Void> sendRevocation(
            final RevocationRequest request,
            final SecretLease secret,
            final Timeout timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "LINE revocation has no remaining timeout");
            }
            body = formCodec.encode(
                    List.of(
                            new NameValue(OAuth2.Parameters.ACCESS_TOKEN, request.token()),
                            new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                            new NameValue(OAuth2.Parameters.CLIENT_SECRET, secret(secret))));
            final var endpoint = variant.targets().resolve(options).revocation().getOrNull();
            try (Response response = FabricX.http(Protocol.OIDC, timeout, services.policies())
                    .url(endpoint.url().toString()).method(Http.Method.POST)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                if (response.code() == Http.Status.OK) {
                    return response.body().length() == 0L ? Outcome.succeeded(null)
                            : failed(ErrorCode._502, "LINE revocation success body must be empty");
                }
                if (response.code() == Http.Status.TOO_MANY_REQUESTS) {
                    return failed(ErrorCode._429, "LINE revocation endpoint rate limited the request");
                }
                if (response.code() == Http.Status.BAD_REQUEST || response.code() == Http.Status.UNAUTHORIZED
                        || response.code() == Http.Status.FORBIDDEN) {
                    return rejected("LINE revocation endpoint rejected the request");
                }
                return failed(ErrorCode._502, "LINE revocation endpoint returned an invalid status");
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "LINE revocation request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Delegates standard Discovery and binds every security-relevant value to the frozen LINE manifest.
     *
     * @param context immutable invocation context
     * @param timeout shared end-to-end timeout
     * @return unchanged standard metadata only after exact profile validation
     */
    private CompletionStage<Outcome<OpenIdProviderMetadata>> discover(final Context context, final Timeout timeout) {
        return discoveryClient.discover(context, timeout).thenApply(outcome -> switch (outcome) {
            case Outcome.Succeeded<OpenIdProviderMetadata> success -> metadata(success.value());
            case Outcome.Rejected<OpenIdProviderMetadata> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<OpenIdProviderMetadata> failed -> Outcome.failed(failed.failure());
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Validates LINE Discovery metadata without allowing remote values to rewrite compiled endpoints.
     *
     * @param metadata issuer-bound standard metadata
     * @return unchanged metadata or safe rejection
     */
    private Outcome<OpenIdProviderMetadata> metadata(final OpenIdProviderMetadata metadata) {
        try {
            final var resolvedTargets = variant.targets().resolve(options);
            issuerValidator.validate(ISSUER, metadata.issuer());
            if (!resolvedTargets.authorization().getOrNull().url().toString().equals(metadata.authorizationEndpoint())
                    || !resolvedTargets.token().getOrNull().url().toString().equals(metadata.tokenEndpoint())
                    || metadata.userInfoEndpoint().isEmpty()
                    || !"https://api.line.me/oauth2/v2.1/userinfo".equals(metadata.userInfoEndpoint().getOrNull())
                    || !resolvedTargets.jwks().getOrNull().url().toString().equals(metadata.jwksUri())
                    || !metadata.responseTypesSupported().equals(List.of(ResponseType.CODE))
                    || !metadata.subjectTypesSupported().contains(SubjectType.PAIRWISE)
                    || !metadata.scopesSupported().containsAll(List.of("openid", "profile", "email"))
                    || !metadata.idTokenSigningAlgValuesSupported().equals(List.of(JwaAlgorithm.ES256))
                    || !metadata.tokenEndpointAuthMethodsSupported()
                            .contains(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    || !extension(metadata, OAuth2.Metadata.REVOCATION_ENDPOINT)
                            .equals(resolvedTargets.revocation().getOrNull().url().toString())
                    || !extensionArray(metadata, OAuth2.Metadata.CODE_CHALLENGE_METHODS_SUPPORTED)
                            .contains(PkceMethod.S256.value())) {
                throw new ValidateException("LINE Discovery metadata differs from the frozen manifest");
            }
            return Outcome.succeeded(metadata);
        } catch (RuntimeException cause) {
            return rejected("LINE Discovery metadata does not match the configured Source");
        }
    }

    /**
     * Retrieves and strictly decodes LINE's configured public JWK Set.
     *
     * @param timeout shared end-to-end timeout
     * @return current issuer public key set
     */
    private CompletionStage<Outcome<JwkSet>> jwks(final Timeout timeout) {
        return jwks(timeout, false);
    }

    /**
     * Retrieves LINE's JWK Set with response-directive caching and one explicit rotation refresh path.
     *
     * @param timeout shared end-to-end timeout
     * @param force   whether to bypass a still-fresh cached set after an unknown {@code kid}
     * @return current issuer public key set
     */
    private CompletionStage<Outcome<JwkSet>> jwks(final Timeout timeout, final boolean force) {
        final Instant now = timeout.clock().now();
        final JwkSet current = cachedJwkSet;
        if (!force && current != null && now.isBefore(cachedJwkSetExpiresAt)) {
            return completed(Outcome.succeeded(current));
        }
        return CompletableFuture.supplyAsync(() -> {
            synchronized (jwkCacheMonitor) {
                final Instant checkedAt = timeout.clock().now();
                final JwkSet doubleChecked = cachedJwkSet;
                if (!force && doubleChecked != null && checkedAt.isBefore(cachedJwkSetExpiresAt)) {
                    return Outcome.succeeded(doubleChecked);
                }
                try {
                    if (timeout.expired()) {
                        return failed(ErrorCode._408, "LINE JWK Set request has no remaining timeout");
                    }
                    final String endpoint = variant.targets().resolve(options).jwks().getOrNull().url().toString();
                    final Response response = FabricX.http(Protocol.OIDC, timeout, services.policies()).url(endpoint)
                            .method(Http.Method.GET).execute();
                    final var control = response.cacheControl();
                    final int maximumAge = control.maxAgeSeconds();
                    final JwkSet fetched = jwkSetCodec.decode(response);
                    if (!control.noCache() && !control.noStore() && maximumAge > 0) {
                        cachedJwkSet = fetched;
                        cachedJwkSetExpiresAt = checkedAt.plusSeconds(maximumAge);
                    } else {
                        cachedJwkSet = null;
                        cachedJwkSetExpiresAt = Instant.EPOCH;
                    }
                    return Outcome.succeeded(fetched);
                } catch (RuntimeException cause) {
                    return failed(ErrorCode._502, "LINE JWK Set endpoint request failed");
                }
            }
        }, services.executor());
    }

    /**
     * Validates one exact LINE callback success or registered error branch.
     *
     * @param callback raw inbound callback
     * @return immutable unique callback values
     * @throws ValidateException if target, transport, branch, multiplicity, or values are invalid
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        final CallbackWire values = parameters(callback);
        final boolean success = values.code() != null && values.error() == null && values.errorDescription() == null
                && values.memberCount() == 2;
        final boolean error = values.code() == null && values.error() != null
                && values.memberCount() == (values.errorDescription() == null ? 2 : 3);
        if ((!success && !error) || !values.knownMembers() || values.blank()) {
            throw new ValidateException("LINE callback must contain one exact success or error branch");
        }
        if (error && !rejectedCallbackError(values.error()) && !"SERVER_ERROR".equals(values.error())) {
            throw new ValidateException("LINE callback error is not registered");
        }
        return values;
    }

    /**
     * Validates callback transport and indexes each parameter exactly once.
     *
     * @param callback raw inbound callback
     * @return typed callback values with member-shape metadata
     * @throws ValidateException if target, method, or parameter multiplicity is invalid
     */
    private CallbackWire parameters(final Callback.Inbound callback) {
        Assert.notNull(callback, "LINE callback must not be null");
        if (callback.method() != Http.Method.GET || !options.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("LINE callback transport or target is invalid");
        }
        final Map<String, String> values = new LinkedHashMap<>();
        for (Callback.Parameter parameter : callback.parameters()) {
            if (values.putIfAbsent(parameter.name(), parameter.value()) != null) {
                throw new ValidateException("LINE callback parameter names must be unique");
            }
        }
        boolean knownMembers = true;
        for (String member : values.keySet()) {
            if (!OAuth2.Parameters.CODE.equals(member) && !OAuth2.Parameters.ERROR.equals(member)
                    && !OAuth2.Parameters.ERROR_DESCRIPTION.equals(member) && !OAuth2.Parameters.STATE.equals(member)) {
                knownMembers = false;
                break;
            }
        }
        return new CallbackWire(values.get(OAuth2.Parameters.CODE), values.get(OAuth2.Parameters.ERROR),
                values.get(OAuth2.Parameters.ERROR_DESCRIPTION), values.get(OAuth2.Parameters.STATE), values.size(),
                knownMembers, values.values().stream().anyMatch(String::isBlank));
    }

    /**
     * Decodes one bounded LINE JSON object without accepting duplicate members.
     *
     * @param response  response whose body remains open
     * @param operation safe operation label
     * @return strict implementation-neutral JSON object
     * @throws ValidateException if media type, shape, depth, or duplicate members are invalid
     */
    private JsonValue.ObjectValue object(final Response response, final String operation) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("LINE " + operation + " response must use application/json");
        }
        final JsonValue value = JsonKit.readValue(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES), Normal._32, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("LINE " + operation + " response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries indexed LINE callback values without exposing a field-name map as a contract.
     *
     * @param code             authorization code on the success branch
     * @param error            registered callback error on the error branch
     * @param errorDescription optional callback error description
     * @param state            browser correlation state
     * @param memberCount      exact number of received parameters
     * @param knownMembers     whether every received name belongs to the callback contract
     * @param blank            whether any received value is blank
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String error, String errorDescription, String state, int memberCount,
            boolean knownMembers, boolean blank) {

        /**
         * Retains callback values after transport and multiplicity validation.
         */
        private CallbackWire {
            // No initialization required.
        }

    }

}
