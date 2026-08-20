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
package org.miaixz.bus.auth.vendor.linkedin.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.*;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.Parameter;
import org.miaixz.bus.auth.guard.IssuerValidator;
import org.miaixz.bus.auth.guard.TimeGuard;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientSettings;
import org.miaixz.bus.auth.protocol.oidc.*;
import org.miaixz.bus.auth.protocol.oidc.client.OpenIdClientSettings;
import org.miaixz.bus.auth.protocol.oidc.client.OpenIdSourceProfile;
import org.miaixz.bus.auth.protocol.oidc.client.UserInfoClient;
import org.miaixz.bus.auth.protocol.oidc.codec.IdTokenCodec;
import org.miaixz.bus.auth.protocol.oidc.codec.JwkSetCodec;
import org.miaixz.bus.auth.protocol.oidc.codec.UserInfoCodec;
import org.miaixz.bus.auth.shared.ExecutionServices;
import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.shared.jose.*;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.jwt.JwtVerifier;
import org.miaixz.bus.auth.source.ExternalIdentity;
import org.miaixz.bus.auth.source.SourceAuthentication;
import org.miaixz.bus.auth.source.SourceAuthenticationRequest;
import org.miaixz.bus.auth.vendor.RedirectManager;
import org.miaixz.bus.auth.vendor.StandardAdapter;
import org.miaixz.bus.auth.vendor.VendorAdapter;
import org.miaixz.bus.auth.vendor.VendorDefinition;
import org.miaixz.bus.auth.vendor.linkedin.LinkedInDefinition;
import org.miaixz.bus.auth.vendor.linkedin.LinkedInSourceSettings;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
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
 * Implements LinkedIn's current OpenID Connect Source and relying-party boundary.
 * <p>
 * Registry-visible operations use only standard OpenID Connect models. The incomplete LinkedIn token and Discovery
 * representations remain private to the application-level Source authentication chain, where the ID Token is locally
 * verified before a standard UserInfo response can produce an external identity.
 * </p>
 *
 * @author Kimi Liu
 */
public final class LinkedInSourceAdapter implements VendorAdapter {

    /**
     * Current issuer advertised by LinkedIn OpenID Provider Discovery.
     */
    private static final String ISSUER = "https://www.linkedin.com/oauth";

    /**
     * Historical issuer still documented by LinkedIn's current product page.
     */
    private static final String HISTORICAL_ISSUER = "https://www.linkedin.com";

    /**
     * Maximum bounded JSON document accepted from a LinkedIn endpoint.
     */
    private static final long MAXIMUM_JSON_BYTES = Normal.MEBI;

    /**
     * Maximum JSON nesting admitted for private LinkedIn wire documents.
     */
    private static final int MAXIMUM_JSON_DEPTH = 16;

    /**
     * Standard and LinkedIn token errors classified as request rejection.
     */
    private static final Set<OAuth2ErrorCode> REJECTED_TOKEN_ERRORS = Set.of(
            OAuth2ErrorCode.INVALID_REQUEST,
            OAuth2ErrorCode.INVALID_CLIENT,
            OAuth2ErrorCode.INVALID_GRANT,
            OAuth2ErrorCode.UNAUTHORIZED_CLIENT,
            OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE,
            OAuth2ErrorCode.INVALID_SCOPE,
            new OAuth2ErrorCode("invalid_redirect_uri"));

    /**
     * Standard UserInfo string claims retained as external identity attributes.
     */
    private static final List<String> IDENTITY_STRING_CLAIMS = List
            .of("name", "given_name", "family_name", "picture", "locale", "email");

    /**
     * Registered Source identifier copied into every verified external identity.
     */
    private final String sourceId;

    /**
     * Selected immutable LinkedIn variant definition.
     */
    private final VendorDefinition.Definition variantDefinition;

    /**
     * Validated externally loaded LinkedIn registration settings.
     */
    private final LinkedInSourceSettings settings;

    /**
     * Caller-owned runtime dependencies and transport resources.
     */
    private final ExecutionServices services;

    /**
     * Unified router for LinkedIn's public Authentication, JWK Set, and UserInfo capabilities.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared atomic browser correlation coordinator.
     */
    private final RedirectManager redirectManager;

    /**
     * Standard form encoder used for LinkedIn's private token request.
     */
    private final FormCodec formCodec;

    /**
     * Strict public JWK Set response codec.
     */
    private final JwkSetCodec jwkSetCodec;

    /**
     * Shared key selector constrained by protected JOSE metadata.
     */
    private final JwkSelector jwkSelector;

    /**
     * Profile-scoped JWS parser and verifier.
     */
    private final JwsService jwsService;

    /**
     * Typed ID Token codec backed by explicit-key JWT verification.
     */
    private final IdTokenCodec idTokenCodec;

    /**
     * Exact issuer comparison primitive.
     */
    private final IssuerValidator issuerValidator;

    /**
     * Shared OIDC clock and skew validator.
     */
    private final TimeGuard timeGuard;

    /**
     * Monitor protecting the small issuer-bound JWK Set cache.
     */
    private final Object jwkCacheMonitor = new Object();

    /**
     * Most recently cacheable LinkedIn public JWK Set.
     */
    private volatile JwkSet cachedJwkSet;

    /**
     * Exclusive expiration of the cached JWK Set derived from {@code max-age}.
     */
    private volatile Instant cachedJwkSetExpiresAt = Instant.EPOCH;

    /**
     * Creates one Source-bound LinkedIn adapter from the frozen current-product definition.
     *
     * @param namespaceId       registration namespace used to isolate browser state and credentials
     * @param sourceId          registered Source identifier
     * @param vendorDefinition  selected LinkedIn definition
     * @param variantDefinition selected default variant definition
     * @param settings          decoded externally loaded LinkedIn settings
     * @param services          caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a required collaborator is {@code null}
     * @throws ValidateException        if the Vendor definition, variant definition, settings, or security baseline
     *                                  differs from the frozen LinkedIn OIDC variant
     */
    public LinkedInSourceAdapter(final String namespaceId, final String sourceId,
            final LinkedInDefinition vendorDefinition, final VendorDefinition.Definition variantDefinition,
            final LinkedInSourceSettings settings, final ExecutionServices services) {
        Assert.notNull(vendorDefinition, "LinkedIn definition must not be null");
        this.sourceId = Assert.notBlank(sourceId, "LinkedIn Source id must not be blank");
        this.variantDefinition = Assert.notNull(variantDefinition, "LinkedIn definition must not be null");
        this.settings = Assert.notNull(settings, "LinkedIn settings must not be null");
        this.services = Assert.notNull(services, "LinkedIn execution services must not be null");
        if (!LinkedInDefinition.ID.equals(vendorDefinition.type())
                || !vendorDefinition.variant(LinkedInDefinition.DEFAULT).equals(variantDefinition)
                || !LinkedInDefinition.DEFAULT.equals(variantDefinition.variant())
                || variantDefinition.protocol() != Protocol.OIDC || !LinkedInDefinition.ID.equals(settings.vendor())
                || !LinkedInDefinition.DEFAULT.equals(settings.variant())) {
            throw new ValidateException("LinkedIn adapter requires the linkedin/default OIDC definition");
        }
        if (!services.securityBaseline().require(Protocol.OIDC).algorithms().contains(JwaAlgorithm.RS256.name())) {
            throw new ValidateException("LinkedIn RS256 is not enabled by the OIDC security baseline");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variantDefinition, settings, services);
        final var targets = variantDefinition.targets().resolve(settings);
        final OAuth2ClientSettings oauthSettings = new OAuth2ClientSettings(targets.authorization(), targets.token(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(ISSUER),
                settings.clientId(), Set.of(settings.redirectUri().getOrNull()),
                Endpoint.Authentication.CLIENT_SECRET_POST, Optional.of(settings.credential()), false, false);
        final OpenIdClientSettings openIdSettings = new OpenIdClientSettings(oauthSettings, targets.discovery(),
                targets.userInfo(), targets.jwks(), targets.endSession(), Set.of(JwaAlgorithm.RS256));
        final UserInfoClient userInfoClient = new UserInfoClient(openIdSettings, services,
                new UserInfoCodec(services.jsonProvider()));
        this.standardAdapter = new StandardAdapter(variantDefinition, settings, Optional.of(redirectManager),
                List.of(
                        new StandardAdapter.Binding<>(OpenIdSourceProfile.AUTHENTICATION,
                                (request, context, timeout) -> authentication(request)),
                        new StandardAdapter.Binding<>(OpenIdSourceProfile.JWK_SET,
                                (ignored, context, timeout) -> jwks(timeout, false)),
                        new StandardAdapter.Binding<>(OpenIdSourceProfile.USERINFO, userInfoClient::userInfo)));
        this.formCodec = new FormCodec();
        this.jwkSetCodec = new JwkSetCodec(services.jsonProvider());
        this.jwkSelector = new JwkSelector();
        this.issuerValidator = new IssuerValidator();
        this.timeGuard = services.securityBaseline().timeGuard(Protocol.OIDC, services.fabricContext().clock());
        this.jwsService = new JwsService(services.jsonProvider(), services.securityBaseline().algorithmGuard(),
                Set.of(JwaAlgorithm.RS256.name()));
        final JweService dormantJweService = new JweService(services.jsonProvider(),
                services.securityBaseline().algorithmGuard(), Set.of(JwaAlgorithm.RSA_OAEP_256.name()),
                Set.of(JwaAlgorithm.A256GCM.name()));
        this.idTokenCodec = new IdTokenCodec(new JwtVerifier(services.jsonProvider(), jwsService, dormantJweService));
    }

    /**
     * Converts one selected public RSA JWK through the existing bus-crypto key primitive.
     *
     * @param jwk selected public LinkedIn RSA JWK
     * @return JCA RSA public verification key
     * @throws ValidateException if key type, private-material boundary, modulus, or exponent is invalid
     */
    private static PublicKey rsaPublicKey(final Jwk jwk) {
        if (!"RSA".equals(jwk.keyType()) || jwk.hasPrivateMaterial()) {
            throw new ValidateException("LinkedIn ID Token requires a public RSA JWK");
        }
        final byte[] modulus = binary(jwk, "n");
        final byte[] exponent = binary(jwk, "e");
        try {
            return Keeper.getRSAPublicKey(new BigInteger(1, modulus), new BigInteger(1, exponent));
        } finally {
            clear(modulus);
            clear(exponent);
        }
    }

    /**
     * Identifies LinkedIn callback errors that represent explicit user cancellation.
     *
     * @param error callback error value
     * @return whether the user cancelled login or authorization
     */
    private static boolean cancelledCallback(final String error) {
        return "user_cancelled_login".equals(error) || "user_cancelled_authorize".equals(error);
    }

    /**
     * Verifies that a LinkedIn token success contains only documented members.
     *
     * @param object decoded token response
     * @return whether every present member has a registered token meaning
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue object) {
        for (String member : object.values().keySet()) {
            final boolean known = switch (member) {
                case OAuth2.Parameters.ACCESS_TOKEN, OpenIdConnect.Parameters.ID_TOKEN, OAuth2.Parameters.TOKEN_TYPE, OAuth2.Parameters.EXPIRES_IN, OAuth2.Parameters.SCOPE, OAuth2.Parameters.REFRESH_TOKEN, "refresh_token_expires_in" -> true;
                default -> false;
            };
            if (!known) {
                return false;
            }
        }
        return true;
    }

    /**
     * Decodes one required unpadded Base64URL JWK integer through the bus-core Base64 codec.
     *
     * @param jwk  source JWK
     * @param name exact registered integer member
     * @return newly allocated unsigned integer octets
     * @throws ValidateException if the member is absent, wrongly typed, or empty
     */
    private static byte[] binary(final Jwk jwk, final String name) {
        final JsonValue value = jwk.parameter(name)
                .orElseThrow(() -> new ValidateException("LinkedIn RSA JWK lacks required member " + name));
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("LinkedIn RSA JWK member must be a Base64URL string");
        }
        final byte[] decoded = Base64.decode(string.value());
        if (decoded == null || decoded.length == 0) {
            throw new ValidateException("LinkedIn RSA JWK integer must not be empty");
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
            throw new ValidateException("LinkedIn response lacks required member: " + name);
        }
        return value;
    }

    /**
     * Reads one required non-blank JSON string.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return non-blank string value
     * @throws ValidateException if the member is absent, blank, or another JSON type
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = optionalString(object, name);
        if (value == null || value.isBlank()) {
            throw new ValidateException("LinkedIn response requires non-blank string member: " + name);
        }
        return value;
    }

    /**
     * Reads one optional JSON string without type coercion.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return string value or {@code null} when absent
     * @throws ValidateException if a present member uses another JSON type
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("LinkedIn response member must be a JSON string: " + name);
        }
        return string.value();
    }

    /**
     * Reads one required array of unique non-blank JSON strings.
     *
     * @param object decoded object
     * @param name   exact array member name
     * @return immutable values in wire order
     * @throws ValidateException if the member is absent, wrongly typed, blank, or duplicated
     */
    private static List<String> strings(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = required(object, name);
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("LinkedIn Discovery member must be an array: " + name);
        }
        final List<String> values = new ArrayList<>(array.values().size());
        for (JsonValue item : array.values()) {
            if (!(item instanceof JsonValue.StringValue string) || string.value().isBlank()
                    || values.contains(string.value())) {
                throw new ValidateException("LinkedIn Discovery array contains an invalid value: " + name);
            }
            values.add(string.value());
        }
        return List.copyOf(values);
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
            throw new ValidateException("LinkedIn response member must be a JSON number: " + name);
        }
        try {
            final long decoded = number.value().longValueExact();
            if (decoded <= 0L) {
                throw new ValidateException("LinkedIn response lifetime must be positive: " + name);
            }
            return decoded;
        } catch (ArithmeticException cause) {
            throw new ValidateException("LinkedIn response lifetime must be an exact long: " + name, cause);
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
     * Copies the active client secret into a transient form value and clears intermediate characters.
     *
     * @param lease open client-secret lease
     * @return transient form value
     */
    private static String secret(final SecretLease lease) {
        final char[] material = lease.material();
        try {
            return new String(material);
        } finally {
            Arrays.fill(material, '\0');
        }
    }

    /**
     * Narrows a delegated outcome through the capability's declared response class.
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
     * Creates a provider-neutral empty JSON object.
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
        return completed(rejected("LinkedIn capability is not declared"));
    }

    /**
     * Creates a completed request-shape rejection.
     *
     * @param <S> expected successful response type
     * @return completed rejection stage
     */
    private static <S> CompletionStage<Outcome<S>> mismatch() {
        return completed(rejected("LinkedIn capability request is invalid"));
    }

    /**
     * Creates an already completed asynchronous result.
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
     * Creates a safe operational failure without response details.
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
     * Creates a safe operational failure carrying only allow-listed protocol details.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive failure description
     * @param details     safe error code and HTTP status values
     * @param <T>         expected successful value type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(
            final org.miaixz.bus.core.basic.normal.Errors code,
            final String description,
            final Map<String, JsonValue> details) {
        return Outcome.failed(new Outcome.Failure(code, description, new JsonValue.ObjectValue(details)));
    }

    /**
     * Returns the exact frozen LinkedIn capability manifest.
     *
     * @return immutable Source authentication, Authentication, JWK Set, and UserInfo capabilities
     */
    @Override
    public Capability.Manifest manifest() {
        return variantDefinition.manifest();
    }

    /**
     * Routes only operations explicitly published by the LinkedIn profile.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific request or {@code null} for JWK Set retrieval
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed operation outcome without private LinkedIn wire models
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(capability, "LinkedIn capability must not be null");
        Assert.notNull(context, "LinkedIn invocation context must not be null");
        Assert.notNull(timeout, "LinkedIn invocation budget must not be null");
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
        if (standardAdapter.manifest().capabilities().contains(capability)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return mismatch();
    }

    /**
     * Builds a standard LinkedIn Authentication Request URL using the exact registered query order.
     *
     * @param request standard OpenID Connect Authentication Request
     * @return exact authorization URL or a safe request rejection
     */
    private CompletionStage<Outcome<UnoUrl>> authentication(final AuthenticationRequest request) {
        try {
            if (!valid(request)) {
                return completed(rejected("LinkedIn Authentication Request differs from the registered definition"));
            }
            return completed(Outcome.succeeded(authorizationUrl(request)));
        } catch (RuntimeException cause) {
            return completed(rejected("LinkedIn Authentication Request is invalid"));
        }
    }

    /**
     * Validates the public Authentication Request shape supported by LinkedIn's current web product.
     * <p>
     * The framework model carries a nonce because it represents general OIDC. LinkedIn's current web contract does not
     * register that parameter, so this Vendor definition validates but does not transmit the model's nonce.
     * </p>
     *
     * @param request standard request to inspect
     * @return whether every transmitted member matches the frozen LinkedIn definition
     */
    private boolean valid(final AuthenticationRequest request) {
        if (request == null) {
            return false;
        }
        final AuthorizationRequest authorization = request.authorizationRequest();
        final Scope scope = authorization.scope().getOrNull();
        return ResponseType.CODE.equals(authorization.responseType())
                && settings.clientId().equals(authorization.clientId())
                && settings.redirectUri().equals(authorization.redirectUri()) && scope != null
                && requestedScopes().equals(scope.values()) && authorization.state().isPresent()
                && authorization.codeChallenge().isEmpty() && authorization.codeChallengeMethod().isEmpty()
                && authorization.extensions().values().isEmpty() && request.display().isEmpty()
                && request.prompt().isEmpty() && request.maxAge().isEmpty() && request.uiLocales().isEmpty()
                && request.idTokenHint().isEmpty() && request.loginHint().isEmpty() && request.acrValues().isEmpty()
                && request.claims().isEmpty() && request.responseMode().isEmpty()
                && request.extensions().values().isEmpty();
    }

    /**
     * Encodes the exact LinkedIn authorization query order without nonce, PKCE, or optional request parameters.
     *
     * @param request validated standard Authentication Request
     * @return immutable exact LinkedIn authorization URL
     */
    private UnoUrl authorizationUrl(final AuthenticationRequest request) {
        final AuthorizationRequest authorization = request.authorizationRequest();
        return variantDefinition.targets().resolve(settings).authorization().getOrNull().url().newBuilder()
                .query(OAuth2.Parameters.RESPONSE_TYPE, ResponseType.CODE.value())
                .query(OAuth2.Parameters.CLIENT_ID, authorization.clientId())
                .query(OAuth2.Parameters.REDIRECT_URI, authorization.redirectUri().getOrNull())
                .query(OAuth2.Parameters.STATE, authorization.state().getOrNull())
                .query(OAuth2.Parameters.SCOPE, authorization.scope().getOrNull().format()).build();
    }

    /**
     * Validates private Discovery metadata before preparing an authorization redirect.
     *
     * @param initiation generated one-time browser security material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return prepared redirect only after the frozen Discovery subset is confirmed
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout.Budget timeout) {
        return discovery(timeout).thenApply(discovered -> switch (discovered) {
            case Outcome.Succeeded<Void> ignored -> prepare(initiation);
            case Outcome.Rejected<Void> rejected -> Outcome.rejected(rejected.failure());
            case Outcome.Failed<Void> failed -> Outcome.failed(failed.failure());
        });
    }

    /**
     * Creates the standard request model whose exact LinkedIn encoding omits unsupported nonce and PKCE parameters.
     *
     * @param initiation generated state and internally consumed OIDC correlation material
     * @return prepared redirect or safe rejection
     */
    private Outcome<RedirectManager.Prepared> prepare(final RedirectManager.Initiation initiation) {
        try {
            final AuthorizationRequest authorization = new AuthorizationRequest(ResponseType.CODE, settings.clientId(),
                    settings.redirectUri(), Optional.of(new Scope(requestedScopes())), Optional.of(initiation.state()),
                    Optional.empty(), Optional.empty(), emptyObject());
            final String modelNonce = initiation.nonce().getOrNull();
            if (modelNonce == null) {
                return failed(ErrorCode._500, "LinkedIn browser flow did not create its internal OIDC correlation");
            }
            final AuthenticationRequest authentication = new AuthenticationRequest(authorization,
                    Optional.of(modelNonce), Optional.empty(), Optional.empty(), Optional.empty(), List.of(),
                    Optional.empty(), Optional.empty(), List.of(), Optional.empty(), Optional.empty(), emptyObject());
            return Outcome.succeeded(
                    new RedirectManager.Prepared(authorizationUrl(authentication).toString(), initiation.state()));
        } catch (RuntimeException cause) {
            return rejected("LinkedIn Authentication Request is invalid");
        }
    }

    /**
     * Extracts the unique state used to atomically consume the browser correlation.
     *
     * @param callback raw inbound callback captured by the external Web project
     * @return non-blank state value
     * @throws ValidateException if callback transport, target, multiplicity, or state is invalid
     */
    private String state(final Callback.Inbound callback) {
        final CallbackWire values = parameters(callback);
        final String state = values.state();
        if (state == null || state.isBlank()) {
            throw new ValidateException("LinkedIn callback requires a unique non-blank state");
        }
        return state;
    }

    /**
     * Completes a correlated LinkedIn authorization response and resolves a verified external identity.
     *
     * @param completion consumed callback and browser correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end budget
     * @return verified LinkedIn external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> identity(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout.Budget timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "LinkedIn authorization callback is malformed"));
        }
        if (values.error() != null) {
            final String error = values.error();
            final Map<String, JsonValue> details = Map.of(OAuth2.Parameters.ERROR, new JsonValue.StringValue(error));
            if (cancelledCallback(error)) {
                return completed(
                        Outcome.rejected(
                                new Outcome.Failure(ErrorCode._400, "LinkedIn authorization was cancelled",
                                        new JsonValue.ObjectValue(details))));
            }
            return completed(
                    failed(ErrorCode._502, "LinkedIn authorization endpoint returned an unknown error", details));
        }
        final String code = values.code();
        final CompletionStage<Outcome<SecretLease>> resolution;
        try {
            resolution = services.secretResolver().resolve(settings.credential(), context, timeout);
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "LinkedIn client-secret resolution failed"));
        }
        if (resolution == null) {
            return completed(failed(ErrorCode._502, "LinkedIn client-secret resolver returned no stage"));
        }
        return resolution.thenCompose(outcome -> switch (outcome) {
            case Outcome.Succeeded<SecretLease> success -> authenticate(code, success.value(), context, timeout);
            case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Runs token redemption, local ID Token verification, and UserInfo binding under one secret lease.
     *
     * @param code    single-use authorization code
     * @param secret  owned client-secret lease
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return fully verified LinkedIn identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> authenticate(
            final String code,
            final SecretLease secret,
            final Context context,
            final Timeout.Budget timeout) {
        final CompletionStage<Outcome<ExternalIdentity>> stage = CompletableFuture
                .supplyAsync(() -> sendToken(code, secret, timeout), services.executor())
                .thenCompose(token -> switch (token) {
                    case Outcome.Succeeded<OpenIdTokenResponse> success -> verifyToken(
                            success.value(),
                            context,
                            timeout);
                    case Outcome.Rejected<OpenIdTokenResponse> rejected -> completed(
                            Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<OpenIdTokenResponse> failed -> completed(Outcome.failed(failed.failure()));
                });
        return stage.whenComplete((ignored, failure) -> secret.close());
    }

    /**
     * Sends LinkedIn's exact private authorization-code token form.
     *
     * @param code    validated authorization code
     * @param secret  open client-secret lease
     * @param timeout shared end-to-end budget
     * @return private standard token model or safely classified failure
     */
    private Outcome<OpenIdTokenResponse> sendToken(
            final String code,
            final SecretLease secret,
            final Timeout.Budget timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "LinkedIn token request has no remaining time budget");
            }
            body = formCodec.encode(
                    List.of(
                            new Parameter(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()),
                            new Parameter(OAuth2.Parameters.CODE,
                                    Assert.notBlank(code, "LinkedIn authorization code must not be blank")),
                            new Parameter(OAuth2.Parameters.CLIENT_ID, settings.clientId()),
                            new Parameter(OAuth2.Parameters.CLIENT_SECRET, secret(secret)),
                            new Parameter(OAuth2.Parameters.REDIRECT_URI, settings.redirectUri().getOrNull())));
            final var endpoint = variantDefinition.targets().resolve(settings).token().getOrNull();
            try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint.url().toString())
                    .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .timeout(timeout.forFabric())
                    .addressPolicy(services.securityBaseline().require(Protocol.OIDC).addressPolicy())
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "LinkedIn token endpoint request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Strictly decodes LinkedIn's private token success or OAuth error representation.
     *
     * @param response owned token endpoint response
     * @return private standard token model, request rejection, or operational failure
     */
    private Outcome<OpenIdTokenResponse> token(final HttpResponse response) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (response.code() == Http.Status.OK) {
                return tokenSuccess(object);
            }
            return tokenError(response.code(), object);
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "LinkedIn token endpoint returned an invalid response");
        }
    }

    /**
     * Maps the exact admitted LinkedIn token success members to the standard internal token model.
     *
     * @param object decoded success object
     * @return successful private token outcome
     * @throws ValidateException if required members, optional refresh members, or returned scope is invalid
     */
    private Outcome<OpenIdTokenResponse> tokenSuccess(final JsonValue.ObjectValue object) {
        if (!tokenMembers(object)) {
            throw new ValidateException("LinkedIn token success contains an unknown member");
        }
        final TokenType tokenType = new TokenType(requiredString(object, OAuth2.Parameters.TOKEN_TYPE));
        if (!TokenType.BEARER.equals(tokenType)) {
            throw new ValidateException("LinkedIn token response requires Bearer token_type");
        }
        final Scope scope = Scope.parse(requiredString(object, OAuth2.Parameters.SCOPE));
        if (!requestedScopes().equals(scope.values())) {
            throw new ValidateException("LinkedIn token response scope differs from the requested scope");
        }
        final String refreshToken = optionalString(object, OAuth2.Parameters.REFRESH_TOKEN);
        final JsonValue refreshLifetime = object.values().get("refresh_token_expires_in");
        if ((refreshToken == null) != (refreshLifetime == null)) {
            throw new ValidateException("LinkedIn partner refresh token and lifetime must occur together");
        }
        final Map<String, JsonValue> extensions = new LinkedHashMap<>();
        if (refreshLifetime != null) {
            positiveLong(refreshLifetime, "refresh_token_expires_in");
            extensions.put("refresh_token_expires_in", refreshLifetime);
        }
        extensions.put(
                OpenIdConnect.Parameters.ID_TOKEN,
                new JsonValue.StringValue(requiredString(object, OpenIdConnect.Parameters.ID_TOKEN)));
        final TokenResponse token = new TokenResponse(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN), tokenType,
                Optional.of(positiveLong(required(object, OAuth2.Parameters.EXPIRES_IN), OAuth2.Parameters.EXPIRES_IN)),
                Optional.ofNullable(refreshToken), Optional.of(scope), new JsonValue.ObjectValue(extensions));
        return Outcome.succeeded(OpenIdTokenResponse.from(token));
    }

    /**
     * Classifies one strict LinkedIn OAuth token error without retaining diagnostic descriptions or response bodies.
     *
     * @param status HTTP response status
     * @param object decoded error object
     * @return rejected request or failed upstream operation
     */
    private Outcome<OpenIdTokenResponse> tokenError(final int status, final JsonValue.ObjectValue object) {
        if (!object.values().containsKey(OAuth2.Parameters.ERROR) || object.values()
                .size() != (object.values().containsKey(OAuth2.Parameters.ERROR_DESCRIPTION) ? 2 : 1)) {
            throw new ValidateException("LinkedIn token error envelope is invalid");
        }
        final OAuth2ErrorCode error = new OAuth2ErrorCode(requiredString(object, OAuth2.Parameters.ERROR));
        optionalString(object, OAuth2.Parameters.ERROR_DESCRIPTION);
        final Map<String, JsonValue> details = Map
                .of(OAuth2.Parameters.ERROR, new JsonValue.StringValue(error.value()), "status", number(status));
        if (status == Http.Status.TOO_MANY_REQUESTS) {
            return failed(ErrorCode._429, "LinkedIn token endpoint rate limited the request", details);
        }
        if (status >= Http.Status.INTERNAL_SERVER_ERROR) {
            return failed(ErrorCode._502, "LinkedIn token endpoint returned an upstream error", details);
        }
        if (REJECTED_TOKEN_ERRORS.contains(error)) {
            return Outcome.rejected(
                    new Outcome.Failure(ErrorCode._400, "LinkedIn token endpoint rejected the request",
                            new JsonValue.ObjectValue(details)));
        }
        return failed(ErrorCode._502, "LinkedIn token endpoint returned an unknown OAuth error", details);
    }

    /**
     * Retrieves a current key set and verifies LinkedIn's compact RS256 ID Token locally.
     *
     * @param token   private token response containing access token and ID Token
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return identity stage after key selection, claim validation, and UserInfo binding
     */
    private CompletionStage<Outcome<ExternalIdentity>> verifyToken(
            final OpenIdTokenResponse token,
            final Context context,
            final Timeout.Budget timeout) {
        final String compact = token.idToken().compact();
        final JoseHeader header;
        try {
            final JwsService.Jws parsed = jwsService.parseCompact(compact, Set.of());
            header = parsed.signatures().get(0).header();
            if (!JwaAlgorithm.RS256.name().equals(header.algorithm()) || header.keyId().isEmpty()) {
                throw new ValidateException("LinkedIn ID Token requires protected RS256 and kid");
            }
        } catch (RuntimeException cause) {
            return completed(rejected("LinkedIn ID Token protected header is invalid"));
        }
        return jwks(timeout, false).thenCompose(keys -> switch (keys) {
            case Outcome.Succeeded<JwkSet> success -> verifyWithRotation(
                    token,
                    compact,
                    header,
                    success.value(),
                    context,
                    timeout);
            case Outcome.Rejected<JwkSet> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<JwkSet> failed -> completed(Outcome.failed(failed.failure()));
        });
    }

    /**
     * Selects the protected {@code kid}, forcing at most one JWK refresh when the current set cannot resolve it.
     *
     * @param token   private token response
     * @param compact sensitive compact ID Token
     * @param header  structurally validated protected header
     * @param keys    current issuer-bound JWK Set
     * @param context immutable invocation context
     * @param timeout shared end-to-end budget
     * @return verified identity stage
     */
    private CompletionStage<Outcome<ExternalIdentity>> verifyWithRotation(
            final OpenIdTokenResponse token,
            final String compact,
            final JoseHeader header,
            final JwkSet keys,
            final Context context,
            final Timeout.Budget timeout) {
        try {
            final Jwk selected = select(keys, header);
            return verifyIdToken(token, compact, selected, context, timeout);
        } catch (RuntimeException firstSelectionFailure) {
            return jwks(timeout, true).thenCompose(refreshed -> switch (refreshed) {
                case Outcome.Succeeded<JwkSet> success -> {
                    try {
                        yield verifyIdToken(token, compact, select(success.value(), header), context, timeout);
                    } catch (RuntimeException secondSelectionFailure) {
                        yield completed(rejected("LinkedIn ID Token key selection failed"));
                    }
                }
                case Outcome.Rejected<JwkSet> rejected -> completed(Outcome.rejected(rejected.failure()));
                case Outcome.Failed<JwkSet> failed -> completed(Outcome.failed(failed.failure()));
            });
        }
    }

    /**
     * Selects one public RSA verification key using exact protected algorithm and key identifier metadata.
     *
     * @param keys   issuer-bound key set
     * @param header validated compact-JWS protected header
     * @return unique LinkedIn signing JWK
     * @throws ValidateException if selection is empty, ambiguous, private, or contradicts optional JWK metadata
     */
    private Jwk select(final JwkSet keys, final JoseHeader header) {
        final String keyId = header.keyId().orElseThrow(() -> new ValidateException("LinkedIn ID Token requires kid"));
        final Jwk selected = jwkSelector.requireUnique(
                keys,
                new JwkSelector.Selection(Optional.of(keyId), JwaAlgorithm.RS256.name(), JwaAlgorithm.Kind.SIGNATURE,
                        Optional.of("sig"), Optional.of("verify"), Optional.of("RSA")));
        if (selected.hasPrivateMaterial() || selected.keyId().filter(keyId::equals).isEmpty()
                || selected.algorithm().filter(value -> !JwaAlgorithm.RS256.name().equals(value)).isPresent()
                || selected.publicKeyUse().filter(value -> !"sig".equals(value)).isPresent()) {
            throw new ValidateException("LinkedIn JWK contradicts the protected RS256 selection");
        }
        return selected;
    }

    /**
     * Applies cryptographic verification and the exact LinkedIn issuer, audience, authorized-party, and time profile.
     *
     * @param token    private token response
     * @param compact  sensitive compact ID Token
     * @param selected selected public RSA JWK
     * @param context  immutable invocation context
     * @param timeout  shared end-to-end budget
     * @return UserInfo and identity stage after successful ID Token validation
     */
    private CompletionStage<Outcome<ExternalIdentity>> verifyIdToken(
            final OpenIdTokenResponse token,
            final String compact,
            final Jwk selected,
            final Context context,
            final Timeout.Budget timeout) {
        final IdTokenClaims claims;
        try {
            final IdTokenCodec.Decoded decoded = idTokenCodec
                    .decode(new IdToken(compact), new JwtVerifier.Signed(rsaPublicKey(selected), Set.of()));
            claims = decoded.claims();
            if (ISSUER.equals(claims.issuer())) {
                issuerValidator.validate(ISSUER, claims.issuer());
            } else if (HISTORICAL_ISSUER.equals(claims.issuer())) {
                issuerValidator.validate(HISTORICAL_ISSUER, claims.issuer());
            } else {
                throw new ValidateException("LinkedIn ID Token issuer is not allow-listed");
            }
            if (!claims.audience().contains(settings.clientId())) {
                throw new ValidateException("LinkedIn ID Token audience omits the client identifier");
            }
            final String authorizedParty = claims.authorizedParty().getOrNull();
            if (claims.audience().size() > 1 && authorizedParty == null) {
                throw new ValidateException("LinkedIn multi-audience ID Token requires azp");
            }
            if (authorizedParty != null && !settings.clientId().equals(authorizedParty)) {
                throw new ValidateException("LinkedIn ID Token azp differs from the client identifier");
            }
            timeGuard.validateWindow(claims.issuedAt(), Optional.empty(), claims.expiration(), timeout);
        } catch (RuntimeException cause) {
            return completed(rejected("LinkedIn ID Token validation failed"));
        }
        return userInfo(token, claims, context, timeout);
    }

    /**
     * Retrieves a standard UserInfo response and binds its subject to the verified ID Token subject.
     *
     * @param token   private token response carrying the Bearer access token
     * @param claims  cryptographically verified ID Token claims
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
                .invoke(OpenIdSourceProfile.USERINFO, new UserInfoRequest(token.accessToken()), context, timeout)
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<UserInfoResponse> success -> identity(success.value(), claims, timeout);
                    case Outcome.Rejected<UserInfoResponse> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<UserInfoResponse> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Produces an external identity only after exact ID Token and UserInfo subject binding.
     *
     * @param userInfo standard LinkedIn UserInfo response
     * @param claims   verified LinkedIn ID Token claims
     * @param timeout  shared clock used to timestamp evidence
     * @return verified external identity or a safe rejection
     */
    private Outcome<ExternalIdentity> identity(
            final UserInfoResponse userInfo,
            final IdTokenClaims claims,
            final Timeout.Budget timeout) {
        if (!claims.subject().equals(userInfo.subject())) {
            return rejected("LinkedIn UserInfo subject does not match the verified ID Token");
        }
        final Map<String, JsonValue> attributes = new LinkedHashMap<>();
        try {
            for (String name : IDENTITY_STRING_CLAIMS) {
                final JsonValue value = userInfo.claims().values().get(name);
                if (value != null) {
                    if (!(value instanceof JsonValue.StringValue)) {
                        throw new ValidateException("LinkedIn UserInfo string claim has an invalid JSON type");
                    }
                    attributes.put(name, value);
                }
            }
            final JsonValue verified = userInfo.claims().values().get("email_verified");
            if (verified != null) {
                if (!(verified instanceof JsonValue.BooleanValue)) {
                    throw new ValidateException("LinkedIn email_verified claim must be boolean");
                }
                attributes.put("email_verified", verified);
            }
        } catch (RuntimeException cause) {
            return rejected("LinkedIn UserInfo claims are invalid");
        }
        final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                new Evidence.Claim(JwtClaims.SUBJECT, new JsonValue.StringValue(claims.subject()), claims.issuer(),
                        timeout.clock().now()));
        return Outcome.succeeded(
                new ExternalIdentity(sourceId, claims.subject(), new JsonValue.ObjectValue(attributes),
                        List.of(evidence)));
    }

    /**
     * Retrieves and validates the exact Discovery subset used privately by LinkedIn Source authentication.
     *
     * @param timeout shared end-to-end budget
     * @return empty success after fixed metadata validation
     */
    private CompletionStage<Outcome<Void>> discovery(final Timeout.Budget timeout) {
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "LinkedIn Discovery has no remaining time budget"));
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (timeout.expired()) {
                    return failed(ErrorCode._408, "LinkedIn Discovery exhausted its time budget");
                }
                final String endpoint = variantDefinition.targets().resolve(settings).discovery().getOrNull().url()
                        .toString();
                try (HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint).method(Http.Method.GET)
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).timeout(timeout.forFabric())
                        .addressPolicy(services.securityBaseline().require(Protocol.OIDC).addressPolicy()).execute()) {
                    validateDiscovery(response);
                    return Outcome.succeeded(null);
                }
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "LinkedIn Discovery metadata validation failed");
            }
        }, services.executor());
    }

    /**
     * Validates LinkedIn's intentionally incomplete Discovery object without inventing absent metadata members.
     *
     * @param response owned Discovery HTTP response
     * @throws ValidateException if status, media, endpoints, issuer, or required arrays differ from the frozen
     *                           definition
     */
    private void validateDiscovery(final HttpResponse response) {
        if (response.code() != Http.Status.OK) {
            throw new ValidateException("LinkedIn Discovery endpoint must return HTTP 200");
        }
        final JsonValue.ObjectValue object = object(response);
        final var resolvedTargets = variantDefinition.targets().resolve(settings);
        issuerValidator.validate(ISSUER, requiredString(object, "issuer"));
        if (!resolvedTargets.authorization().getOrNull().url().toString()
                .equals(requiredString(object, "authorization_endpoint"))
                || !resolvedTargets.token().getOrNull().url().toString()
                        .equals(requiredString(object, "token_endpoint"))
                || !resolvedTargets.userInfo().getOrNull().url().toString()
                        .equals(requiredString(object, "userinfo_endpoint"))
                || !resolvedTargets.jwks().getOrNull().url().toString().equals(requiredString(object, "jwks_uri"))
                || !strings(object, OAuth2.Metadata.RESPONSE_TYPES_SUPPORTED).equals(List.of(ResponseType.CODE.value()))
                || !strings(object, "subject_types_supported").contains("pairwise")
                || !strings(object, "id_token_signing_alg_values_supported").equals(List.of(JwaAlgorithm.RS256.name()))
                || !strings(object, "scopes_supported").containsAll(List.of("openid", "profile", "email"))
                || !strings(object, "claims_supported").containsAll(
                        List.of(
                                JwtClaims.ISSUER,
                                JwtClaims.AUDIENCE,
                                JwtClaims.ISSUED_AT,
                                JwtClaims.EXPIRATION,
                                JwtClaims.SUBJECT,
                                "name",
                                "given_name",
                                "family_name",
                                "picture",
                                "email",
                                "email_verified",
                                "locale"))) {
            throw new ValidateException("LinkedIn Discovery metadata differs from the frozen definition");
        }
    }

    /**
     * Retrieves LinkedIn's JWK Set with response-directive caching and one explicit rotation-refresh path.
     *
     * @param timeout shared end-to-end budget
     * @param force   whether to bypass a still-fresh cache after unresolved protected {@code kid}
     * @return current issuer-bound public JWK Set
     */
    private CompletionStage<Outcome<JwkSet>> jwks(final Timeout.Budget timeout, final boolean force) {
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
                        return failed(ErrorCode._408, "LinkedIn JWK Set request has no remaining time budget");
                    }
                    final String endpoint = variantDefinition.targets().resolve(settings).jwks().getOrNull().url()
                            .toString();
                    final HttpResponse response = Fabric.http(services.fabricContext()).url(endpoint)
                            .method(Http.Method.GET).timeout(timeout.forFabric())
                            .addressPolicy(services.securityBaseline().require(Protocol.OIDC).addressPolicy())
                            .execute();
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
                    return failed(ErrorCode._502, "LinkedIn JWK Set endpoint request failed");
                }
            }
        }, services.executor());
    }

    /**
     * Validates one exact LinkedIn callback success or registered cancellation branch.
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
            throw new ValidateException("LinkedIn callback must contain one exact success or error branch");
        }
        return values;
    }

    /**
     * Validates callback target and transport and indexes every parameter exactly once.
     *
     * @param callback raw inbound callback
     * @return typed callback values with member-shape metadata
     * @throws ValidateException if target, method, source, or parameter multiplicity is invalid
     */
    private CallbackWire parameters(final Callback.Inbound callback) {
        Assert.notNull(callback, "LinkedIn callback must not be null");
        if (!sourceId.equals(callback.sourceId()) || callback.method() != Http.Method.GET
                || !settings.redirectUri().getOrNull().equals(callback.requestUri())) {
            throw new ValidateException("LinkedIn callback transport or target is invalid");
        }
        final Map<String, String> values = new LinkedHashMap<>();
        for (Callback.Parameter parameter : callback.parameters()) {
            if (values.putIfAbsent(parameter.name(), parameter.value()) != null) {
                throw new ValidateException("LinkedIn callback parameter names must be unique");
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
     * Strictly reads one bounded application/json LinkedIn response object.
     *
     * @param response response whose body remains owned by the caller
     * @return provider-neutral immutable JSON object
     * @throws ValidateException if media type, size, JSON syntax, duplicate names, or root shape is invalid
     */
    private JsonValue.ObjectValue object(final HttpResponse response) {
        final MediaType media = response.body().media();
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(media)) {
            throw new ValidateException("LinkedIn response must use application/json");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("LinkedIn JSON response charset must be UTF-8");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("LinkedIn response root must be a JSON object");
        }
        return object;
    }

    /**
     * Returns the immutable effective LinkedIn scopes.
     *
     * @return ordered explicit scopes or frozen definition defaults
     */
    private List<String> requestedScopes() {
        return settings.scopes().isEmpty() ? variantDefinition.defaultScopes() : settings.scopes();
    }

    /**
     * Carries indexed LinkedIn callback values without exposing a field-name map as a contract.
     *
     * @param code             authorization code on the success branch
     * @param error            callback error on the error branch
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
