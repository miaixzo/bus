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
package org.miaixz.bus.auth.vendor.huawei;

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
import org.miaixz.bus.auth.protocol.oidc.codec.AuthenticationRequestEncoder;
import org.miaixz.bus.auth.protocol.oidc.codec.IdTokenCodec;
import org.miaixz.bus.auth.protocol.oidc.codec.OpenIdProviderMetadataCodec;
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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Implements Huawei Account Kit's frozen OpenID Connect web sign-in contract.
 * <p>
 * Registry operations retain standard Authentication Request, Discovery, JWK Set, and Revocation types. Huawei's
 * numeric token errors, {@code supportAlg}, form-carried profile token, and {@code NSP_STATUS} semantics remain private
 * to the Source completion chain and never appear as invented public protocol models.
 * </p>
 *
 * @author Kimi Liu
 */
public class HuaweiSourceAdapter implements VendorAdapter {

    /**
     * Exact issuer required from Discovery and every verified Huawei ID Token.
     */
    private static final String ISSUER = "https://accounts.huawei.com";

    /**
     * Maximum JSON response size accepted from Huawei endpoints.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Maximum nested JSON depth accepted from Huawei endpoints.
     */
    private static final int MAXIMUM_JSON_DEPTH = Normal._16;

    /**
     * Registered Source identifier written into verified external identities.
     */
    private final String sourceId;

    /**
     * Selected immutable Huawei variant manifest.
     */
    private final VariantManifest.Variant variant;

    /**
     * Validated externally loaded Huawei registration values.
     */
    private final HuaweiOptions options;

    /**
     * Caller-owned runtime, loaders, parsers, JSON, executor, clock, and Fabric dependencies.
     */
    private final DriverServices services;

    /**
     * Standard OpenID Connect operations composed from protocol-owned clients.
     */
    private final StandardAdapter standardAdapter;

    /**
     * Shared one-time state, nonce, and S256 verifier lifecycle.
     */
    private final RedirectManager redirectManager;

    /**
     * Shared strict form encoder for Huawei request bodies.
     */
    private final FormCodec formCodec;

    /**
     * Exact case-sensitive issuer comparison primitive.
     */
    private final IssuerValidator issuerValidator;

    /**
     * Shared exact public JWK selector.
     */
    private final JwkSelector jwkSelector;

    /**
     * Profile-scoped RS256 JWS parser used before public-key selection.
     */
    private final JwsService jwsService;

    /**
     * Complete OIDC ID Token signature and claims verifier.
     */
    private final IdTokenVerifier idTokenVerifier;

    /**
     * Creates one Source-bound Huawei adapter from the frozen default manifest.
     *
     * @param namespaceId registration namespace used for browser correlation isolation
     * @param sourceId    registered Source identifier
     * @param manifest    selected Huawei manifest
     * @param variant     selected default variant manifest
     * @param options     decoded externally loaded options
     * @param services    caller-owned execution services
     * @throws IllegalArgumentException if an identifier is blank or a collaborator is {@code null}
     * @throws ValidateException        if routing, protocol, entropy, or algorithm policy differs from the frozen
     *                                  profile
     */
    public HuaweiSourceAdapter(final String namespaceId, final String sourceId, final HuaweiManifest manifest,
            final VariantManifest.Variant variant, final HuaweiOptions options, final DriverServices services) {
        Assert.notNull(manifest, "Huawei manifest must not be null");
        this.sourceId = Assert.notBlank(sourceId, "Huawei Source id must not be blank");
        this.variant = Assert.notNull(variant, "Huawei manifest must not be null");
        this.options = Assert.notNull(options, "Huawei options must not be null");
        this.services = Assert.notNull(services, "Huawei execution services must not be null");
        if (!HuaweiManifest.ID.equals(manifest.vendor()) || !manifest.variant(HuaweiManifest.DEFAULT).equals(variant)
                || !HuaweiManifest.DEFAULT.equals(variant.variant()) || variant.protocol() != Protocol.OIDC
                || !HuaweiManifest.ID.equals(options.vendor()) || !HuaweiManifest.DEFAULT.equals(options.variant())) {
            throw new ValidateException("Huawei adapter requires the huawei/default OIDC manifest");
        }
        final var policy = services.securityBaseline().require(Protocol.OIDC);
        if (!policy.algorithms().contains(JwaAlgorithm.RS256.name()) || policy.minimumEntropyBits() > 400) {
            throw new ValidateException(
                    "Huawei requires RS256 and state entropy not exceeding its 100-character limit");
        }
        this.redirectManager = RedirectManager.create(namespaceId, sourceId, variant, options, services);
        this.formCodec = new FormCodec();
        this.issuerValidator = new IssuerValidator();
        this.jwkSelector = new JwkSelector();
        this.jwsService = new JwsService(services.jsonProvider(), services.securityBaseline().algorithmGuard(),
                Set.of(JwaAlgorithm.RS256.name()));
        final JweService dormantJwe = new JweService(services.jsonProvider(),
                services.securityBaseline().algorithmGuard(), Set.of(JwaAlgorithm.RSA_OAEP_256.name()),
                Set.of(JwaAlgorithm.A256GCM.name()));
        this.idTokenVerifier = new IdTokenVerifier(
                new IdTokenCodec(new JwtVerifier(services.jsonProvider(), jwsService, dormantJwe)), issuerValidator,
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
                Optional.empty(), targets.jwks(), Optional.empty(), Set.of(JwaAlgorithm.RS256));
        final OpenIdClient openIdClient = new OpenIdClient(oauthClient,
                new AuthenticationRequestEncoder(services.jsonProvider()),
                new DiscoveryClient(openIdSettings, services, new OpenIdProviderMetadataCodec(services.jsonProvider())),
                Optional.empty(), Optional.empty());
        this.standardAdapter = new StandardAdapter(variant, options, Optional.of(redirectManager), List.of(
                new StandardAdapter.Binding<>(OpenIdClientScheme.AUTHENTICATION, openIdClient::authorize),
                new StandardAdapter.Binding<>(OpenIdClientScheme.REVOCATION,
                        (request, ignored, timeout) -> revoke(request, timeout)),
                new StandardAdapter.Binding<>(OpenIdClientScheme.DISCOVERY, (ignored, context, timeout) -> openIdClient
                        .discover(context, timeout).thenApply(outcome -> switch (outcome) {
                            case Outcome.Succeeded<OpenIdProviderMetadata> success -> metadata(success.value());
                            case Outcome.Rejected<OpenIdProviderMetadata> rejected -> Outcome
                                    .rejected(rejected.failure());
                            case Outcome.Failed<OpenIdProviderMetadata> failed -> Outcome.failed(failed.failure());
                            default -> throw new IllegalStateException("Unsupported Outcome implementation");
                        })),
                new StandardAdapter.Binding<>(OpenIdClientScheme.JWK_SET,
                        (ignored, context, timeout) -> jwks(timeout))));
    }

    /**
     * Checks Huawei's documented state and nonce character and length constraints.
     *
     * @param value generated security value
     * @return whether the value is non-empty, at most 100 characters, and uses the registered alphabet
     */
    private static boolean stateValue(final String value) {
        if (value == null || value.isEmpty() || value.length() > 100) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (!(character >= Symbol.C_ZERO && character <= Symbol.C_NINE)
                    && !(character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z)
                    && !(character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z) && character != Symbol.C_COLON
                    && character != Symbol.C_SLASH && character != Symbol.C_DOT) {
                return false;
            }
        }
        return true;
    }

    /**
     * Decodes Huawei's exact three-member integral error envelope.
     *
     * @param status      HTTP status
     * @param object      decoded error object
     * @param description safe operation description
     * @param <T>         expected success type
     * @return rejected known caller error or failed upstream error
     */
    private static <T> Outcome<T> numericError(
            final int status,
            final JsonValue.ObjectValue object,
            final String description) {
        if (object.values().size() != 3 || !object.values().containsKey(OAuth2.Parameters.ERROR)
                || !object.values().containsKey("sub_error")
                || !object.values().containsKey(OAuth2.Parameters.ERROR_DESCRIPTION)) {
            throw new ValidateException("Huawei numeric error envelope is invalid");
        }
        final long error = exactLong(required(object, OAuth2.Parameters.ERROR), OAuth2.Parameters.ERROR);
        final long subError = exactLong(required(object, "sub_error"), "sub_error");
        requiredString(object, OAuth2.Parameters.ERROR_DESCRIPTION);
        final Map<String, JsonValue> details = Map
                .of("vendor_error", number(error), "vendor_sub_error", number(subError), "status", number(status));
        if (status == Http.Status.TOO_MANY_REQUESTS || status == Http.Status.SERVICE_UNAVAILABLE
                || status >= Http.Status.INTERNAL_SERVER_ERROR || error == 590L) {
            return failed(ErrorCode._502, "Huawei endpoint returned an upstream error", details);
        }
        if (status == Http.Status.BAD_REQUEST && (error == 1101L || error == 1102L || error == 1203L
                || subError == 1101L || subError == 1102L || subError == 1203L || subError == 31204L)) {
            return Outcome
                    .rejected(new Outcome.Failure(ErrorCode._400, description, new JsonValue.ObjectValue(details)));
        }
        return failed(ErrorCode._502, "Huawei endpoint returned an unknown numeric error", details);
    }

    /**
     * Narrows Huawei's generic numeric error outcome to a standard empty revocation result.
     *
     * @param status      HTTP status
     * @param object      decoded error object
     * @param description safe operation description
     * @return standard empty revocation failure outcome
     */
    private static Outcome<Void> narrowNumericError(
            final int status,
            final JsonValue.ObjectValue object,
            final String description) {
        return HuaweiSourceAdapter.<Void>numericError(status, object, description);
    }

    /**
     * Identifies OAuth authorization errors documented by Huawei.
     *
     * @param error returned standard OAuth error code
     * @return whether the error belongs to Huawei's documented authorization response
     */
    private static boolean knownAuthorizationError(final String error) {
        return switch (error) {
            case "access_denied", "invalid_request", "unauthorized_client", "unsupported_response_type", "invalid_scope" -> true;
            default -> false;
        };
    }

    /**
     * Verifies that a Huawei token success contains only its documented members.
     *
     * @param object decoded token response
     * @return whether every response member has a defined token meaning
     */
    private static boolean tokenMembers(final JsonValue.ObjectValue object) {
        for (String member : object.values().keySet()) {
            final boolean known = switch (member) {
                case OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.TOKEN_TYPE, OAuth2.Parameters.EXPIRES_IN, OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.SCOPE, OpenIdConnect.Parameters.ID_TOKEN -> true;
                default -> false;
            };
            if (!known) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies that a Huawei profile success contains only documented Account Kit members.
     *
     * @param object decoded profile response
     * @return whether every response member has a defined profile meaning
     */
    private static boolean profileMembers(final JsonValue.ObjectValue object) {
        for (String member : object.values().keySet()) {
            final boolean known = switch (member) {
                case "openID", "unionID", "displayName", "headPictureURL", "displayNameFlag" -> true;
                default -> false;
            };
            if (!known) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads one required JSON member.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return present member value
     */
    private static JsonValue required(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            throw new ValidateException("Huawei response lacks required member: " + name);
        }
        return value;
    }

    /**
     * Reads one required non-blank JSON string.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return non-blank string value
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final String value = optionalString(object, name);
        if (value == null || value.isBlank()) {
            throw new ValidateException("Huawei response requires non-blank string member: " + name);
        }
        return value;
    }

    /**
     * Reads one optional JSON string without accepting explicit null or another type.
     *
     * @param object decoded object
     * @param name   exact member name
     * @return string or {@code null} when absent
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Huawei response member must be a JSON string: " + name);
        }
        return string.value();
    }

    /**
     * Reads one optional non-blank string claim.
     *
     * @param claims verified ID Token extension claims
     * @param name   exact claim name
     * @return string or {@code null} when absent
     */
    private static String optionalClaimString(final JsonValue.ObjectValue claims, final String name) {
        final JsonValue value = claims.values().get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Huawei ID Token extension claim is invalid: " + name);
        }
        return string.value();
    }

    /**
     * Copies one optional non-blank string profile attribute.
     *
     * @param source decoded Huawei profile
     * @param target verified identity attributes
     * @param name   exact profile member name
     */
    private static void copyOptionalString(
            final JsonValue.ObjectValue source,
            final Map<String, JsonValue> target,
            final String name) {
        final String value = optionalString(source, name);
        if (value != null) {
            if (value.isBlank()) {
                throw new ValidateException("Huawei optional profile string must not be blank");
            }
            target.put(name, new JsonValue.StringValue(value));
        }
    }

    /**
     * Reads one exact integral JSON number.
     *
     * @param value number candidate
     * @param name  safe member name
     * @return exact long
     */
    private static long exactLong(final JsonValue value, final String name) {
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("Huawei response member must be a JSON number: " + name);
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException cause) {
            throw new ValidateException("Huawei response member must be an exact long: " + name, cause);
        }
    }

    /**
     * Reads one positive exact integral JSON number.
     *
     * @param value number candidate
     * @param name  safe member name
     * @return positive exact long
     */
    private static long positiveLong(final JsonValue value, final String name) {
        final long decoded = exactLong(value, name);
        if (decoded <= 0L) {
            throw new ValidateException("Huawei response member must be positive: " + name);
        }
        return decoded;
    }

    /**
     * Reads one mandatory non-blank Discovery string extension.
     *
     * @param metadata decoded metadata
     * @param name     exact extension name
     * @return extension string
     */
    private static String extension(final OpenIdProviderMetadata metadata, final String name) {
        final JsonValue value = metadata.extensions().values().get(name);
        if (!(value instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Huawei Discovery string extension is invalid");
        }
        return string.value();
    }

    /**
     * Reads one mandatory unique string-array Discovery extension.
     *
     * @param metadata decoded metadata
     * @param name     exact extension name
     * @return immutable extension values
     */
    private static List<String> extensionArray(final OpenIdProviderMetadata metadata, final String name) {
        final JsonValue value = metadata.extensions().values().get(name);
        if (!(value instanceof JsonValue.ArrayValue array)) {
            throw new ValidateException("Huawei Discovery array extension is invalid");
        }
        final ArrayList<String> values = new ArrayList<>(array.values().size());
        for (JsonValue item : array.values()) {
            if (!(item instanceof JsonValue.StringValue string) || string.value().isBlank()
                    || values.contains(string.value())) {
                throw new ValidateException("Huawei Discovery array extension contains an invalid value");
            }
            values.add(string.value());
        }
        return List.copyOf(values);
    }

    /**
     * Converts one selected RSA JWK using the existing bus-crypto primitive.
     *
     * @param jwk selected public key
     * @return JCA RSA verification key
     */
    private static PublicKey rsaPublicKey(final Jwk jwk) {
        if (!"RSA".equals(jwk.keyType()) || jwk.hasPrivateMaterial()) {
            throw new ValidateException("Huawei ID Token requires a public RSA JWK");
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
     * Decodes one required Base64URL JWK integer member.
     *
     * @param jwk  source JWK
     * @param name exact member name
     * @return newly allocated unsigned integer octets
     */
    private static byte[] binary(final Jwk jwk, final String name) {
        final JsonValue value = jwk.parameter(name)
                .orElseThrow(() -> new ValidateException("Huawei RSA JWK lacks required member " + name));
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Huawei RSA JWK member must be a Base64URL string");
        }
        final byte[] decoded = Base64.decode(string.value());
        if (decoded == null || decoded.length == 0) {
            throw new ValidateException("Huawei RSA JWK integer must not be empty");
        }
        return decoded;
    }

    /**
     * Copies one open Client Secret lease into the UTF-8 form representation and clears the transient octets.
     *
     * @param lease open Client Secret lease
     * @return sensitive form string retained only until request encoding completes
     */
    private static String secret(final SecretLease lease) {
        final char[] material = lease.material();
        try {
            return new String(material);
        } finally {
            clear(material);
        }
    }

    /**
     * Creates one exact integral JSON number.
     *
     * @param value integral value
     * @return JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Creates a provider-neutral empty JSON object.
     *
     * @return immutable empty object
     */
    private static JsonValue.ObjectValue emptyObject() {
        return new JsonValue.ObjectValue(Map.of());
    }

    /**
     * Clears transient sensitive bytes when present.
     *
     * @param value bytes or {@code null}
     */
    private static void clear(final byte[] value) {
        if (value != null) {
            Arrays.fill(value, (byte) 0);
        }
    }

    /**
     * Clears transient sensitive characters when present.
     *
     * @param value characters or {@code null}
     */
    private static void clear(final char[] value) {
        if (value != null) {
            Arrays.fill(value, Symbol.C_NUL);
        }
    }

    /**
     * Narrows a delegated outcome through the declared response class.
     *
     * @param stage        delegated stage
     * @param responseType declared response type
     * @param <S>          expected success type
     * @return type-safe outcome stage
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
     * Creates a safe expected Huawei rejection.
     *
     * @param description non-sensitive description
     * @param <T>         expected success type
     * @return rejected outcome
     */
    private static <T> Outcome<T> rejected(final String description) {
        return Outcome.rejected(new Outcome.Failure(ErrorCode._400, description, emptyObject()));
    }

    /**
     * Creates a safe operational failure using a shared Bus error code.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive description
     * @param <T>         expected success type
     * @return failed outcome
     */
    private static <T> Outcome<T> failed(final Errors code, final String description) {
        return Outcome.failed(new Outcome.Failure(code, description, emptyObject()));
    }

    /**
     * Creates a safe operational failure with bounded non-sensitive details.
     *
     * @param code        shared Bus error code
     * @param description non-sensitive description
     * @param details     bounded safe details
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
     * Returns the exact frozen Huawei capability manifest.
     *
     * @return immutable Source authentication and standard OIDC capability manifest
     */
    @Override
    public Capability.Manifest manifest() {
        return variant.capabilityManifest();
    }

    /**
     * Routes only Huawei capabilities declared by the selected variant manifest.
     *
     * @param capability exact runtime-selected capability
     * @param request    capability-specific standard or Source request
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @param <Q>        request type
     * @param <S>        successful response type
     * @return typed outcome without exposing Huawei-private response records
     */
    @Override
    public <Q, S> CompletionStage<Outcome<S>> invoke(
            final Capability<Q, S> capability,
            final Q request,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(capability, "Huawei capability must not be null");
        Assert.notNull(context, "Huawei invocation context must not be null");
        Assert.notNull(timeout, "Huawei invocation timeout must not be null");
        if (!manifest().capabilities().contains(capability)) {
            return completed(rejected("Huawei capability is not declared"));
        }
        if (capability.key().equals(SourceWorkflow.INITIATE.key())
                && request instanceof SourceWorkflow.Request.BrowserStart start) {
            return narrow(redirectManager.initiate(start, this::prepare, context, timeout), capability.responseType());
        }
        if (capability.key().equals(SourceWorkflow.COMPLETE.key())
                && request instanceof SourceWorkflow.Request.BrowserCallback callback) {
            return narrow(
                    redirectManager.complete(callback, this::state, this::complete, context, timeout),
                    capability.responseType());
        }
        if (standardAdapter.manifest().capabilities().contains(capability)) {
            return standardAdapter.invoke(capability, request, context, timeout);
        }
        return completed(rejected("Huawei capability request is invalid"));
    }

    /**
     * Builds the exact Huawei form-post Authentication Request from generated one-time material.
     *
     * @param initiation generated state, nonce, and S256 challenge
     * @param context    immutable invocation context retained for the uniform callback
     * @param timeout    shared end-to-end timeout
     * @return exact prepared redirect and state binding
     */
    private CompletionStage<Outcome<RedirectManager.Prepared>> prepare(
            final RedirectManager.Initiation initiation,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(context, "Huawei authorization context must not be null");
        if (timeout.expired()) {
            return completed(failed(ErrorCode._408, "Huawei authorization has no remaining timeout"));
        }
        try {
            final var challenge = initiation.codeChallenge().getOrNull();
            final String nonce = initiation.nonce().getOrNull();
            if (challenge == null || nonce == null || !PkceMethod.S256.equals(challenge.method())
                    || !stateValue(initiation.state()) || !stateValue(nonce)) {
                throw new ValidateException("Huawei browser security material violates platform syntax");
            }
            final AuthorizationRequest authorization = new AuthorizationRequest(ResponseType.CODE, options.clientId(),
                    options.redirectUri(), Optional.of(new Scope(options.scopes())), Optional.of(initiation.state()),
                    Optional.of(challenge.value()), Optional.of(PkceMethod.S256.value()), emptyObject());
            final AuthenticationRequest authentication = new AuthenticationRequest(authorization, Optional.of(nonce),
                    Optional.empty(), Optional.empty(), Optional.empty(), List.of(), Optional.empty(), Optional.empty(),
                    List.of(), Optional.empty(), Optional.of("form_post"), emptyObject());
            return standardAdapter.invoke(OpenIdClientScheme.AUTHENTICATION, authentication, context, timeout)
                    .thenApply(outcome -> switch (outcome) {
                        case Outcome.Succeeded<Url> success -> Outcome.succeeded(
                                new RedirectManager.Prepared(success.value().toString(), initiation.state()));
                        case Outcome.Rejected<Url> rejected -> Outcome.rejected(rejected.failure());
                        case Outcome.Failed<Url> failed -> Outcome.failed(failed.failure());
                        default -> throw new IllegalStateException("Unsupported Outcome implementation");
                    });
        } catch (RuntimeException cause) {
            return completed(rejected("Huawei Authentication Request is invalid"));
        }
    }

    /**
     * Extracts the unique callback state after enforcing Huawei's POST form transport and exact branch shape.
     *
     * @param callback raw inbound callback
     * @return unique non-blank state
     */
    private String state(final Callback.Inbound callback) {
        return callback(callback).state();
    }

    /**
     * Validates and indexes one Huawei form-post callback.
     *
     * @param callback raw inbound callback
     * @return immutable unique parameter values
     * @throws ValidateException if target, method, media type, multiplicity, issuer, or branch is invalid
     */
    private CallbackWire callback(final Callback.Inbound callback) {
        Assert.notNull(callback, "Huawei callback must not be null");
        final List<String> contentTypes = callback.headers().values(Http.Header.CONTENT_TYPE);
        final String contentType = contentTypes.size() == 1 ? contentTypes.get(0) : null;
        if (!options.redirectUri().getOrNull().equals(callback.requestUri()) || callback.method() != Http.Method.POST
                || contentType == null
                || !MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(MediaType.parse(contentType))) {
            throw new ValidateException("Huawei callback transport or target is invalid");
        }
        final Map<String, String> values = new LinkedHashMap<>();
        for (Callback.Parameter parameter : callback.parameters()) {
            if (values.putIfAbsent(parameter.name(), parameter.value()) != null) {
                throw new ValidateException("Huawei callback parameter names must be unique");
            }
        }
        final String code = values.get(OAuth2.Parameters.CODE);
        final String errorValue = values.get(OAuth2.Parameters.ERROR);
        final String description = values.get(OAuth2.Parameters.ERROR_DESCRIPTION);
        final String state = values.get(OAuth2.Parameters.STATE);
        final String issuer = values.get(JwtClaims.ISSUER);
        final boolean success = code != null && errorValue == null && description == null;
        final boolean error = code == null && errorValue != null;
        final int expected = success ? issuer == null ? 2 : 3
                : issuer == null ? description == null ? 2 : 3 : description == null ? 3 : 4;
        if ((!success && !error) || values.size() != expected || values.values().stream().anyMatch(String::isBlank)
                || !stateValue(state) || issuer != null && !ISSUER.equals(issuer)) {
            throw new ValidateException("Huawei callback has an invalid success or error branch");
        }
        return new CallbackWire(code, errorValue, description, state, issuer);
    }

    /**
     * Completes one correlated callback through Huawei's private token, ID Token, and profile sequence.
     *
     * @param completion consumed callback, nonce, state, and PKCE verifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified Huawei external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> complete(
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final CallbackWire values;
        try {
            values = callback(completion.callback());
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._400, "Huawei authorization callback is invalid"));
        }
        if (values.error() != null) {
            if (!knownAuthorizationError(values.error())) {
                return completed(failed(ErrorCode._502, "Huawei authorization endpoint returned an unknown error"));
            }
            return completed(
                    Outcome.rejected(
                            new Outcome.Failure(ErrorCode._400, "Huawei authorization endpoint rejected the request",
                                    new JsonValue.ObjectValue(
                                            Map.of(Builder.OAUTH_ERROR, new JsonValue.StringValue(values.error()))))));
        }
        if (completion.codeVerifier().isEmpty() || completion.correlation().nonce().isEmpty()) {
            return completed(failed(ErrorCode._500, "Huawei browser correlation lacks nonce or PKCE verifier"));
        }
        final TokenRequest request = new TokenRequest(new AuthorizationCodeGrant(values.code(), options.redirectUri(),
                Optional.of(options.clientId()), Optional.of(completion.codeVerifier().getOrNull().value())),
                emptyObject());
        return Outcome
                .mapStage(
                        () -> services.secretLoader().load(
                                new SecretLoader.Request(services.registration(), options.credential()),
                                context,
                                timeout),
                        loaded -> services.secretParser().parse(services.registration(), options.credential(), loaded))
                .thenCompose(resolved -> switch (resolved) {
                    case Outcome.Succeeded<SecretLease> success -> authenticate(
                            request,
                            success.value(),
                            values,
                            completion,
                            context,
                            timeout);
                    case Outcome.Rejected<SecretLease> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<SecretLease> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Keeps the sole Client Secret lease open across Huawei's token and profile stages.
     *
     * @param request    private authorization-code token request
     * @param secret     owned Client Secret lease
     * @param callback   validated callback values
     * @param completion consumed browser security material
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> authenticate(
            final TokenRequest request,
            final SecretLease secret,
            final CallbackWire callback,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final CompletionStage<Outcome<ExternalIdentity>> stage = CompletableFuture
                .supplyAsync(() -> sendToken(request, secret, timeout), services.executor())
                .thenCompose(token -> switch (token) {
                    case Outcome.Succeeded<PrivateToken> success -> verifyToken(
                            success.value(),
                            callback,
                            completion,
                            context,
                            timeout);
                    case Outcome.Rejected<PrivateToken> rejected -> completed(Outcome.rejected(rejected.failure()));
                    case Outcome.Failed<PrivateToken> failed -> completed(Outcome.failed(failed.failure()));
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
        return stage.whenComplete((ignored, failure) -> secret.close());
    }

    /**
     * Sends Huawei's private authorization-code form containing {@code supportAlg=RS256}.
     *
     * @param request validated authorization-code token request
     * @param secret  open Client Secret lease
     * @param timeout shared end-to-end timeout
     * @return private token data required by the identity chain
     */
    private Outcome<PrivateToken> sendToken(
            final TokenRequest request,
            final SecretLease secret,
            final Timeout timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Huawei token request has no remaining timeout");
            }
            final AuthorizationCodeGrant grant = (AuthorizationCodeGrant) request.grant();
            body = formCodec.encode(
                    List.of(
                            new NameValue(OAuth2.Parameters.GRANT_TYPE, GrantType.AUTHORIZATION_CODE.value()),
                            new NameValue(OAuth2.Parameters.CLIENT_ID, options.clientId()),
                            new NameValue(OAuth2.Parameters.CLIENT_SECRET, secret(secret)),
                            new NameValue(OAuth2.Parameters.CODE, grant.code()),
                            new NameValue(OAuth2.Parameters.REDIRECT_URI, grant.redirectUri().getOrNull()),
                            new NameValue(OAuth2.Parameters.CODE_VERIFIER, grant.codeVerifier().getOrNull()),
                            new NameValue("supportAlg", JwaAlgorithm.RS256.name())));
            final String endpoint = variant.targets().resolve(options).token().getOrNull().url().toString();
            try (Response response = FabricX.http(services.fabric(), Protocol.OIDC, timeout).url(endpoint)
                    .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                return token(response);
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Huawei token request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Strictly decodes Huawei's success or integral numeric-error token response.
     *
     * @param response owned token endpoint response
     * @return private token or safely classified error
     */
    private Outcome<PrivateToken> token(final Response response) {
        try {
            final JsonValue.ObjectValue object = object(response);
            if (response.code() == Http.Status.OK) {
                if (!tokenMembers(object)) {
                    throw new ValidateException("Huawei token success contains an unknown member");
                }
                final String tokenType = requiredString(object, OAuth2.Parameters.TOKEN_TYPE);
                if (!"bearer".equalsIgnoreCase(tokenType)) {
                    throw new ValidateException("Huawei token type must be Bearer");
                }
                final Scope scope = Scope.parse(requiredString(object, OAuth2.Parameters.SCOPE));
                if (!options.scopes().equals(scope.values())) {
                    throw new ValidateException("Huawei token response scope differs from the request");
                }
                return Outcome.succeeded(
                        new PrivateToken(requiredString(object, OAuth2.Parameters.ACCESS_TOKEN),
                                positiveLong(
                                        required(object, OAuth2.Parameters.EXPIRES_IN),
                                        OAuth2.Parameters.EXPIRES_IN),
                                optionalString(object, OAuth2.Parameters.REFRESH_TOKEN),
                                requiredString(object, OpenIdConnect.Parameters.ID_TOKEN), scope));
            }
            return numericError(response.code(), object, "Huawei token endpoint rejected the request");
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Huawei token endpoint returned an invalid response");
        }
    }

    /**
     * Retrieves Huawei's public keys before locally verifying the token-endpoint ID Token.
     *
     * @param token      private token response
     * @param callback   validated authorization response values
     * @param completion consumed nonce, state, and verifier
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified identity after profile subject binding
     */
    private CompletionStage<Outcome<ExternalIdentity>> verifyToken(
            final PrivateToken token,
            final CallbackWire callback,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        return jwks(timeout).thenCompose(keys -> switch (keys) {
            case Outcome.Succeeded<JwkSet> success -> verifyIdToken(
                    token,
                    success.value(),
                    callback,
                    completion,
                    context,
                    timeout);
            case Outcome.Rejected<JwkSet> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<JwkSet> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Selects one exact Huawei RSA signing key and applies complete OIDC ID Token verification.
     *
     * @param token      private token response
     * @param keys       current issuer public keys
     * @param callback   validated callback values
     * @param completion consumed browser correlation
     * @param context    immutable invocation context
     * @param timeout    shared end-to-end timeout
     * @return verified identity after Huawei profile retrieval
     */
    private CompletionStage<Outcome<ExternalIdentity>> verifyIdToken(
            final PrivateToken token,
            final JwkSet keys,
            final CallbackWire callback,
            final RedirectManager.Completion completion,
            final Context context,
            final Timeout timeout) {
        final PublicKey key;
        try {
            final JwsService.Jws parsed = jwsService.parseCompact(token.idToken(), Set.of());
            final JoseHeader header = parsed.signatures().get(0).header();
            if (!JwaAlgorithm.RS256.name().equals(header.algorithm())) {
                throw new ValidateException("Huawei ID Token must use RS256");
            }
            final String keyId = header.keyId()
                    .orElseThrow(() -> new ValidateException("Huawei ID Token requires a protected kid"));
            final Jwk selected = jwkSelector.requireUnique(
                    keys,
                    new JwkSelector.Selection(header.keyId(), JwaAlgorithm.RS256.name(), JwaAlgorithm.Kind.SIGNATURE,
                            Optional.of(Builder.SIGNATURE), Optional.of(Builder.VERIFY), Optional.of("RSA")));
            if (selected.keyId().filter(keyId::equals).isEmpty()
                    || selected.algorithm().filter(JwaAlgorithm.RS256.name()::equals).isEmpty()
                    || selected.publicKeyUse().filter(Builder.SIGNATURE::equals).isEmpty()) {
                throw new ValidateException("Huawei JWK must explicitly bind kid, alg=RS256, and use=sig");
            }
            key = rsaPublicKey(selected);
        } catch (RuntimeException cause) {
            return completed(failed(ErrorCode._502, "Huawei ID Token key selection failed"));
        }
        final IdTokenVerifier.Request verification = new IdTokenVerifier.Request(new IdToken(token.idToken()),
                new JwtVerifier.Signed(key, Set.of()), ISSUER, options.clientId(),
                completion.correlation().nonce().getOrNull(), Optional.empty(), Optional.of(token.accessToken()),
                Optional.of(callback.code()), Optional.of(completion.correlation().state()));
        return idTokenVerifier.verify(verification, context, timeout).thenCompose(claims -> switch (claims) {
            case Outcome.Succeeded<IdTokenClaims> success -> success.value().accessTokenHash().isPresent()
                    ? profile(token, success.value(), timeout)
                    : completed(failed(ErrorCode._502, "Huawei ID Token lacks required at_hash binding"));
            case Outcome.Rejected<IdTokenClaims> rejected -> completed(Outcome.rejected(rejected.failure()));
            case Outcome.Failed<IdTokenClaims> failed -> completed(Outcome.failed(failed.failure()));
            default -> throw new IllegalStateException("Unsupported Outcome implementation");
        });
    }

    /**
     * Calls Huawei's proprietary profile operation and binds both official identifiers to the verified subject.
     *
     * @param token   verified token set
     * @param claims  cryptographically verified ID Token claims
     * @param timeout shared end-to-end timeout
     * @return verified external identity
     */
    private CompletionStage<Outcome<ExternalIdentity>> profile(
            final PrivateToken token,
            final IdTokenClaims claims,
            final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> sendProfile(token, claims, timeout), services.executor());
    }

    /**
     * Sends the exact Huawei profile form without a Bearer header.
     *
     * @param token   verified token data
     * @param claims  verified ID Token claims
     * @param timeout shared end-to-end timeout
     * @return subject-bound external identity
     */
    private Outcome<ExternalIdentity> sendProfile(
            final PrivateToken token,
            final IdTokenClaims claims,
            final Timeout timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Huawei profile request has no remaining timeout");
            }
            body = formCodec.encode(
                    List.of(
                            new NameValue(OAuth2.Parameters.ACCESS_TOKEN, token.accessToken()),
                            new NameValue("getNickName", Symbol.ONE)));
            final String endpoint = variant.targets().resolve(options).userInfo().getOrNull().url().toString();
            try (Response response = FabricX.http(services.fabric(), Protocol.OIDC, timeout).url(endpoint)
                    .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                final List<String> statuses = response.headers().values("NSP_STATUS");
                if (statuses.size() > 1) {
                    throw new ValidateException("Huawei profile response repeats NSP_STATUS");
                }
                final String nsp = statuses.isEmpty() ? null : statuses.get(0);
                if (nsp != null) {
                    if (Symbol.SIX.equals(nsp) || "403".equals(nsp)) {
                        return rejected("Huawei profile endpoint rejected the access token");
                    }
                    return failed(ErrorCode._502, "Huawei profile endpoint returned an unknown NSP_STATUS");
                }
                if (response.code() != Http.Status.OK) {
                    return response.code() == Http.Status.UNAUTHORIZED || response.code() == Http.Status.FORBIDDEN
                            ? rejected("Huawei profile endpoint rejected the request")
                            : failed(ErrorCode._502, "Huawei profile endpoint request failed");
                }
                final JsonValue.ObjectValue profile = object(response);
                if (!profileMembers(profile)) {
                    throw new ValidateException("Huawei profile response contains an unknown member");
                }
                final String openId = requiredString(profile, "openID");
                final String unionId = requiredString(profile, "unionID");
                if (!claims.subject().equals(unionId)) {
                    throw new ValidateException("Huawei profile unionID does not match the verified subject");
                }
                final String boundOpenId = optionalClaimString(claims.extensions(), "openid");
                if (boundOpenId != null && !boundOpenId.equals(openId)) {
                    throw new ValidateException("Huawei profile openID does not match the verified ID Token");
                }
                final Map<String, JsonValue> attributes = new LinkedHashMap<>();
                attributes.put("openID", new JsonValue.StringValue(openId));
                copyOptionalString(profile, attributes, "displayName");
                copyOptionalString(profile, attributes, "headPictureURL");
                final JsonValue flag = profile.values().get("displayNameFlag");
                if (flag != null) {
                    final long value = exactLong(flag, "displayNameFlag");
                    if (value != 0L && value != 1L) {
                        throw new ValidateException("Huawei displayNameFlag must be zero or one");
                    }
                    attributes.put("displayNameFlag", flag);
                }
                final Evidence evidence = new Evidence(Evidence.Type.FEDERATED, Evidence.Strength.SINGLE_FACTOR,
                        new Evidence.Claim(JwtClaims.SUBJECT, new JsonValue.StringValue(unionId), ISSUER,
                                timeout.clock().now()));
                return Outcome.succeeded(
                        new ExternalIdentity(sourceId, unionId, new JsonValue.ObjectValue(attributes),
                                List.of(evidence)));
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Huawei profile response is invalid");
        } finally {
            clear(body);
        }
    }

    /**
     * Binds every security-relevant Huawei metadata value to the compiled manifest.
     *
     * @param metadata decoded standard metadata
     * @return unchanged metadata or a safe rejection
     */
    private Outcome<OpenIdProviderMetadata> metadata(final OpenIdProviderMetadata metadata) {
        try {
            final var resolvedTargets = variant.targets().resolve(options);
            issuerValidator.validate(ISSUER, metadata.issuer());
            if (!resolvedTargets.authorization().getOrNull().url().toString().equals(metadata.authorizationEndpoint())
                    || !resolvedTargets.token().getOrNull().url().toString().equals(metadata.tokenEndpoint())
                    || !resolvedTargets.jwks().getOrNull().url().toString().equals(metadata.jwksUri())
                    || !extension(metadata, OAuth2.Metadata.REVOCATION_ENDPOINT)
                            .equals(resolvedTargets.revocation().getOrNull().url().toString())
                    || !metadata.responseTypesSupported().contains(ResponseType.CODE)
                    || !metadata.responseModesSupported().contains("form_post")
                    || !metadata.subjectTypesSupported().contains(SubjectType.PAIRWISE)
                    || !metadata.scopesSupported().containsAll(options.scopes())
                    || !metadata.tokenEndpointAuthMethodsSupported()
                            .contains(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    || !metadata.idTokenSigningAlgValuesSupported().contains(JwaAlgorithm.RS256)
                    || !extensionArray(metadata, OAuth2.Metadata.CODE_CHALLENGE_METHODS_SUPPORTED).contains("S256")) {
                throw new ValidateException("Huawei Discovery metadata differs from the frozen manifest");
            }
            return Outcome.succeeded(metadata);
        } catch (RuntimeException cause) {
            return rejected("Huawei Discovery metadata does not match the registered Source");
        }
    }

    /**
     * Retrieves Huawei's JSON-media public JWK Set and applies the standard RFC 7517 model parser.
     *
     * @param timeout shared end-to-end timeout
     * @return current issuer public key set
     */
    private CompletionStage<Outcome<JwkSet>> jwks(final Timeout timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (timeout.expired()) {
                    return failed(ErrorCode._408, "Huawei JWK Set request has no remaining timeout");
                }
                final String endpoint = variant.targets().resolve(options).jwks().getOrNull().url().toString();
                try (Response response = FabricX.http(services.fabric(), Protocol.OIDC, timeout).url(endpoint)
                        .method(Http.Method.GET).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON).execute()) {
                    if (response.code() != Http.Status.OK
                            || !MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
                        throw new ValidateException("Huawei JWK Set response must use HTTP 200 application/json");
                    }
                    final JsonValue value = services.jsonProvider()
                            .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
                    if (!(value instanceof JsonValue.ObjectValue object)) {
                        throw new ValidateException("Huawei JWK Set root must be a JSON object");
                    }
                    return Outcome.succeeded(JwkSet.fromJson(object));
                }
            } catch (RuntimeException cause) {
                return failed(ErrorCode._502, "Huawei JWK Set request failed");
            }
        }, services.executor());
    }

    /**
     * Executes Huawei's unauthenticated standard revocation entry using its exact private error representation.
     *
     * @param request standard revocation request
     * @param timeout shared end-to-end timeout
     * @return empty standard success or safely classified failure
     */
    private CompletionStage<Outcome<Void>> revoke(final RevocationRequest request, final Timeout timeout) {
        if (request == null) {
            return completed(rejected("Huawei revocation request must not be null"));
        }
        return CompletableFuture.supplyAsync(() -> sendRevocation(request, timeout), services.executor());
    }

    /**
     * Sends only Huawei's registered {@code token} form field and omits client authentication and token hint.
     *
     * @param request standard revocation request
     * @param timeout shared end-to-end timeout
     * @return empty standard success or classified numeric error
     */
    private Outcome<Void> sendRevocation(final RevocationRequest request, final Timeout timeout) {
        byte[] body = null;
        try {
            if (timeout.expired()) {
                return failed(ErrorCode._408, "Huawei revocation has no remaining timeout");
            }
            body = formCodec.encode(List.of(new NameValue("token", request.token())));
            final String endpoint = variant.targets().resolve(options).revocation().getOrNull().url().toString();
            try (Response response = FabricX.http(services.fabric(), Protocol.OIDC, timeout).url(endpoint)
                    .method(Http.Method.POST).header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                    .body(body, MediaType.APPLICATION_FORM_URLENCODED_TYPE).execute()) {
                final JsonValue.ObjectValue object = object(response);
                if (response.code() == Http.Status.OK) {
                    return object.values().isEmpty() ? Outcome.succeeded(null)
                            : failed(ErrorCode._502, "Huawei revocation success must be an empty JSON object");
                }
                return narrowNumericError(response.code(), object, "Huawei revocation endpoint rejected the request");
            }
        } catch (RuntimeException cause) {
            return failed(ErrorCode._502, "Huawei revocation request failed");
        } finally {
            clear(body);
        }
    }

    /**
     * Strictly reads one bounded Huawei JSON response object.
     *
     * @param response response whose body remains owned by the caller
     * @return immutable provider-neutral JSON object
     */
    private JsonValue.ObjectValue object(final Response response) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(response.body().media())) {
            throw new ValidateException("Huawei response must use application/json");
        }
        final JsonValue value = services.jsonProvider()
                .readValue(response.bytes(MAXIMUM_JSON_BYTES), MAXIMUM_JSON_DEPTH, true);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("Huawei response root must be a JSON object");
        }
        return object;
    }

    /**
     * Carries Huawei token data only inside the Source completion chain.
     *
     * @param accessToken  sensitive bearer access token
     * @param expiresIn    positive access-token lifetime in seconds
     * @param refreshToken optional sensitive refresh token
     * @param idToken      compact signed ID Token
     * @param scope        exact effective standard scope
     * @author Kimi Liu
     */
    private record PrivateToken(String accessToken, long expiresIn, String refreshToken, String idToken, Scope scope) {

        /**
         * Validates private token data without copying it into public Vendor models.
         */
        private PrivateToken {
            Assert.notBlank(accessToken, "Huawei access token must not be blank");
            if (expiresIn <= 0L) {
                throw new ValidateException("Huawei access-token lifetime must be positive");
            }
            if (refreshToken != null) {
                Assert.notBlank(refreshToken, "Huawei refresh token must not be blank when present");
            }
            Assert.notBlank(idToken, "Huawei ID Token must not be blank");
            Assert.notNull(scope, "Huawei effective scope must not be null");
        }

        /**
         * Returns a diagnostic summary without token material.
         *
         * @return redacted private token summary
         */
        @Override
        public String toString() {
            return "PrivateToken[accessToken=[REDACTED], expiresIn=" + expiresIn
                    + ", refreshToken=[REDACTED], idToken=[REDACTED], scope=" + scope + Symbol.C_BRACKET_RIGHT;
        }

    }

    /**
     * Carries one validated Huawei authorization response without exposing an untyped parameter map.
     *
     * @param code             authorization code on the success branch
     * @param error            standard OAuth error on the error branch
     * @param errorDescription optional provider error description
     * @param state            mandatory browser correlation value
     * @param issuer           optional authorization-response issuer
     * @author Kimi Liu
     */
    private record CallbackWire(String code, String error, String errorDescription, String state, String issuer) {

        /**
         * Retains already validated wire values without additional initialization.
         */
        private CallbackWire {
            // No initialization required.
        }

    }

}
