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
package org.miaixz.bus.auth.protocol.oidc.server;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Subject;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.cache.AuthorizationCodeCache;
import org.miaixz.bus.auth.protocol.oauth2.GrantType;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2ErrorCode;
import org.miaixz.bus.auth.protocol.oauth2.TokenEndpointResponse;
import org.miaixz.bus.auth.protocol.oauth2.TokenResponse;
import org.miaixz.bus.auth.protocol.oauth2.grant.AccessTokenIssuer;
import org.miaixz.bus.auth.protocol.oidc.IdToken;
import org.miaixz.bus.auth.protocol.oidc.IdTokenClaims;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.protocol.oidc.OpenIdTokenResponse;
import org.miaixz.bus.auth.protocol.oidc.codec.IdTokenCodec;
import org.miaixz.bus.auth.resolver.KeyMaterial;
import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jose.JwsService;
import org.miaixz.bus.auth.shared.jwt.Jwt;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.jwt.JwtIssuer;
import org.miaixz.bus.auth.source.DriverServices;
import org.miaixz.bus.auth.worker.KeyLoader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.builtin.digest.Digester;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Adds the mandatory OpenID Connect ID Token to a successful authorization-code token response.
 * <p>
 * This class is public only for construction by the unexported OpenID Provider driver. It executes after OAuth token
 * state has been persisted; any rejection or failure is compensated by {@link AccessTokenIssuer}. It does not issue ID
 * Tokens at the authorization endpoint or for refresh and non-code grants.
 * </p>
 *
 * @author Kimi Liu
 */
public final class IdTokenIssuer implements AccessTokenIssuer.Augmenter {

    /**
     * Standard JWK key-use value for signing keys.
     */
    private static final String SIGNATURE_USE = Builder.SIGNATURE;

    /**
     * Safe failure-detail member consumed by the OAuth token error mapper.
     */
    private static final String OAUTH_ERROR = Builder.OAUTH_ERROR;

    /**
     * Frozen OpenID Provider options.
     */
    private final OpenIdServerOptions options;

    /**
     * External key, attribute, clock, JSON, and policy dependencies.
     */
    private final DriverServices services;

    /**
     * Shared signed JWT issuer scoped to the Provider's exact algorithm.
     */
    private final JwtIssuer jwtIssuer;

    /**
     * Typed ID Token codec used to preserve the protocol value boundary.
     */
    private final IdTokenCodec codec;

    /**
     * Creates an ID Token response augmenter for one compiled OpenID Provider.
     *
     * @param options  validated OpenID Provider options
     * @param services externally implemented runtime dependencies
     * @param codec    typed ID Token codec
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public IdTokenIssuer(final OpenIdServerOptions options, final DriverServices services, final IdTokenCodec codec) {
        this.options = Assert.notNull(options, "OpenID Provider options must not be null");
        this.services = Assert.notNull(services, "OpenID Provider execution services must not be null");
        this.codec = Assert.notNull(codec, "OpenID Connect ID Token codec must not be null");
        services.securityBaseline().require(Protocol.OIDC).algorithms().stream()
                .filter(options.idTokenSigningAlgorithm().name()::equals).findFirst().orElseThrow(
                        () -> new ValidateException(
                                "OpenID Connect ID Token signing algorithm is not enabled by the security baseline"));
        final JwsService jwsService = new JwsService(services.jsonProvider(),
                services.securityBaseline().algorithmGuard(), Set.of(options.idTokenSigningAlgorithm().name()));
        this.jwtIssuer = new JwtIssuer(services.jsonProvider(), jwsService, services.fabricContext().clock());
    }

    /**
     * Reports whether one standard individual claim request is essential.
     *
     * @param request individual claim request
     * @return whether it contains boolean {@code essential=true}
     */
    private static boolean essential(final JsonValue request) {
        return request instanceof JsonValue.ObjectValue object
                && object.values().get("essential") instanceof JsonValue.BooleanValue flag && flag.value();
    }

    /**
     * Computes an OIDC artifact hash from the exact signing algorithm family.
     *
     * @param artifact         sensitive source artifact
     * @param signingAlgorithm selected JWS algorithm
     * @return unpadded Base64URL left half of the digest
     */
    private static String artifactHash(final String artifact, final String signingAlgorithm) {
        final Algorithm algorithm = digestAlgorithm(signingAlgorithm);
        final byte[] digest = new Digester(algorithm).digest(artifact.getBytes(Charset.UTF_8));
        final byte[] left = Arrays.copyOf(digest, digest.length / 2);
        try {
            return Base64.encodeUrlSafe(left);
        } finally {
            Arrays.fill(digest, (byte) 0);
            Arrays.fill(left, (byte) 0);
        }
    }

    /**
     * Maps the configured JWS algorithm to the hash algorithm required by OIDC artifact hashes.
     *
     * @param signingAlgorithm exact JWS algorithm identifier
     * @return Bus digest algorithm
     * @throws ValidateException if no deterministic OIDC hash family is implemented
     */
    private static Algorithm digestAlgorithm(final String signingAlgorithm) {
        final JwaAlgorithm algorithm = JwaAlgorithm.of(signingAlgorithm);
        if (Set.of(JwaAlgorithm.HS256, JwaAlgorithm.RS256, JwaAlgorithm.PS256, JwaAlgorithm.ES256)
                .contains(algorithm)) {
            return Algorithm.SHA256;
        }
        if (Set.of(JwaAlgorithm.HS384, JwaAlgorithm.RS384, JwaAlgorithm.PS384, JwaAlgorithm.ES384)
                .contains(algorithm)) {
            return Algorithm.SHA384;
        }
        if (Set.of(JwaAlgorithm.HS512, JwaAlgorithm.RS512, JwaAlgorithm.PS512, JwaAlgorithm.ES512, JwaAlgorithm.EDDSA)
                .contains(algorithm)) {
            return Algorithm.SHA512;
        }
        throw new ValidateException("OpenID Connect signing algorithm has no implemented artifact hash mapping");
    }

    /**
     * Creates a safe token-endpoint failure with one registered OAuth error code.
     *
     * @param error       shared Bus error definition
     * @param oauthError  registered OAuth token error
     * @param description non-sensitive diagnostic description
     * @return immutable safe failure
     */
    private static Outcome.Failure failure(
            final Errors error,
            final OAuth2ErrorCode oauthError,
            final String description) {
        return new Outcome.Failure(error, description,
                new JsonValue.ObjectValue(Map.of(OAUTH_ERROR, new JsonValue.StringValue(oauthError.value()))));
    }

    /**
     * Creates an already completed token response outcome.
     *
     * @param outcome completed outcome
     * @return completed stage
     */
    private static CompletionStage<Outcome<TokenEndpointResponse>> completed(
            final Outcome<TokenEndpointResponse> outcome) {
        return CompletableFuture.completedFuture(outcome);
    }

    /**
     * Identifies claims owned by the typed ID Token model rather than subject attributes.
     *
     * @param name exact requested claim name
     * @return {@code true} when the typed model owns the claim
     */
    private static boolean registeredClaim(final String name) {
        return switch (name) {
            case JwtClaims.ISSUER, JwtClaims.SUBJECT, JwtClaims.AUDIENCE, JwtClaims.EXPIRATION, JwtClaims.ISSUED_AT, JwtClaims.NOT_BEFORE, JwtClaims.JWT_ID, OpenIdConnect.Claims.AUTH_TIME, OpenIdConnect.Claims.NONCE, OpenIdConnect.Claims.ACR, OpenIdConnect.Claims.AMR, OpenIdConnect.Claims.AUTHORIZED_PARTY, OpenIdConnect.Claims.ACCESS_TOKEN_HASH, OpenIdConnect.Claims.CODE_HASH, OpenIdConnect.Claims.STATE_HASH, OpenIdConnect.Claims.SESSION_ID -> true;
            default -> false;
        };
    }

    /**
     * Issues an ID Token for an OpenID Connect authorization-code grant and preserves every OAuth response member.
     *
     * @param response persisted base OAuth token response
     * @param grant    exact authorized internal grant
     * @param context  immutable invocation context
     * @param timeout  shared end-to-end operation budget
     * @return stage containing the augmented standard token response or a closed failure
     */
    @Override
    public CompletionStage<Outcome<TokenEndpointResponse>> augment(
            final TokenResponse response,
            final AccessTokenIssuer.Grant grant,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(response, "OpenID Connect base token response must not be null");
        Assert.notNull(grant, "OpenID Connect internal token grant must not be null");
        Assert.notNull(context, "OpenID Connect token context must not be null");
        Assert.notNull(timeout, "OpenID Connect token time budget must not be null");
        if (grant.grantType() != GrantType.AUTHORIZATION_CODE) {
            if (grant.grantType() != GrantType.REFRESH_TOKEN && grant.scope().contains(OpenIdConnect.Scopes.OPENID)) {
                return completed(
                        Outcome.rejected(
                                failure(
                                        ErrorCode._400,
                                        OAuth2ErrorCode.INVALID_SCOPE,
                                        "OpenID Connect openid scope requires an authorization-code grant")));
            }
            return completed(Outcome.succeeded(response));
        }
        final AuthorizationCodeCache.OpenIdBinding binding = grant.openIdBinding().getOrNull();
        if (binding == null || !grant.scope().contains(OpenIdConnect.Scopes.OPENID)) {
            return completed(
                    Outcome.rejected(
                            failure(
                                    ErrorCode._400,
                                    OAuth2ErrorCode.INVALID_GRANT,
                                    "OpenID Connect authorization code is missing its authentication binding")));
        }
        if (timeout.expired()) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._408,
                                    OAuth2ErrorCode.TEMPORARILY_UNAVAILABLE,
                                    "OpenID Connect ID Token issuance has no remaining time budget")));
        }

        final CompletionStage<Outcome<JsonValue.ObjectValue>> attributes;
        try {
            attributes = Outcome.mapStage(
                    () -> services.attributeLoader()
                            .load(services.registration(), new Subject.Key(grant.subjectId()), context, timeout),
                    loaded -> services.attributeParser()
                            .parse(services.registration(), new Subject.Key(grant.subjectId()), loaded));
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._500,
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OpenID Connect subject attribute resolution failed")));
        }
        return attributes
                .handle(
                        (outcome, thrown) -> thrown == null && outcome != null ? outcome
                                : Outcome.<JsonValue.ObjectValue>failed(
                                        failure(
                                                ErrorCode._500,
                                                OAuth2ErrorCode.SERVER_ERROR,
                                                "OpenID Connect subject attribute resolution failed")))
                .thenCompose(outcome -> switch (outcome) {
                    case Outcome.Succeeded<JsonValue.ObjectValue> success -> success.value() == null
                            ? completed(
                                    Outcome.failed(
                                            failure(
                                                    ErrorCode._500,
                                                    OAuth2ErrorCode.SERVER_ERROR,
                                                    "OpenID Connect attribute resolution returned no value")))
                            : resolveKey(response, grant, binding, success.value(), context, timeout);
                    case Outcome.Rejected<JsonValue.ObjectValue> rejected -> completed(
                            Outcome.rejected(
                                    failure(
                                            rejected.failure().error(),
                                            OAuth2ErrorCode.INVALID_GRANT,
                                            "OpenID Connect subject attributes are unavailable")));
                    case Outcome.Failed<JsonValue.ObjectValue> failed -> completed(
                            Outcome.failed(
                                    failure(
                                            failed.failure().error(),
                                            OAuth2ErrorCode.SERVER_ERROR,
                                            "OpenID Connect subject attribute resolution failed")));
                });
    }

    /**
     * Resolves the exact signing key after the claim set is known.
     *
     * @param response   base token response
     * @param grant      authorized internal grant
     * @param binding    authorization-code-bound OIDC context
     * @param attributes current subject attributes
     * @param context    immutable invocation context
     * @param timeout    shared operation budget
     * @return asynchronously augmented response
     */
    private CompletionStage<Outcome<TokenEndpointResponse>> resolveKey(
            final TokenResponse response,
            final AccessTokenIssuer.Grant grant,
            final AuthorizationCodeCache.OpenIdBinding binding,
            final JsonValue.ObjectValue attributes,
            final Context context,
            final Timeout.Budget timeout) {
        final Instant now = timeout.clock().now();
        final KeyLoader.Request query = new KeyLoader.Request(options.issuer(),
                Optional.of(options.idTokenSigningKeyId()), SIGNATURE_USE, options.idTokenSigningAlgorithm().name(),
                now);
        final CompletionStage<Outcome<KeyMaterial>> resolution;
        try {
            resolution = Outcome.mapStage(
                    () -> services.keyLoader().load(services.registration(), query, context, timeout),
                    loaded -> services.keyParser().parse(services.registration(), query, loaded));
        } catch (RuntimeException exception) {
            return completed(
                    Outcome.failed(
                            failure(
                                    ErrorCode._500,
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OpenID Connect signing key resolution failed")));
        }
        return resolution
                .handle(
                        (outcome, thrown) -> thrown == null && outcome != null ? outcome
                                : Outcome.<KeyMaterial>failed(
                                        failure(
                                                ErrorCode._500,
                                                OAuth2ErrorCode.SERVER_ERROR,
                                                "OpenID Connect signing key resolution failed")))
                .thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<KeyMaterial> success -> issue(
                            response,
                            grant,
                            binding,
                            attributes,
                            success.value(),
                            now);
                    case Outcome.Rejected<KeyMaterial> rejected -> Outcome.rejected(
                            failure(
                                    rejected.failure().error(),
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OpenID Connect signing key is unavailable"));
                    case Outcome.Failed<KeyMaterial> failed -> Outcome.failed(
                            failure(
                                    failed.failure().error(),
                                    OAuth2ErrorCode.SERVER_ERROR,
                                    "OpenID Connect signing key resolution failed"));
                });
    }

    /**
     * Builds, signs, and attaches one standard ID Token with an already resolved exact key.
     *
     * @param response    base token response
     * @param grant       authorized internal grant
     * @param binding     authorization-code-bound OIDC context
     * @param attributes  current subject attributes
     * @param resolvedKey exact signing key
     * @param now         current shared-clock instant
     * @return augmented token response outcome
     */
    private Outcome<TokenEndpointResponse> issue(
            final TokenResponse response,
            final AccessTokenIssuer.Grant grant,
            final AuthorizationCodeCache.OpenIdBinding binding,
            final JsonValue.ObjectValue attributes,
            final KeyMaterial resolvedKey,
            final Instant now) {
        try {
            validateResolvedKey(resolvedKey, now);
            final Instant expiration = now.plus(options.idTokenLifetime());
            final IdTokenClaims claims = new IdTokenClaims(options.issuer(), grant.subjectId(),
                    List.of(grant.clientId()), expiration, now, Optional.of(binding.authenticatedAt()), binding.nonce(),
                    binding.authenticationContextClass(), binding.authenticationMethods(), Optional.empty(),
                    Optional.of(artifactHash(response.accessToken(), options.idTokenSigningAlgorithm().name())),
                    Optional.empty(), Optional.empty(), Optional.of(binding.sessionKey().value()),
                    requestedClaims(binding, attributes));
            final Jwt jwt = jwtIssuer.issue(
                    new JwtIssuer.Request(
                            new JwtIssuer.Profile(options.issuer(), List.of(grant.clientId()),
                                    options.idTokenLifetime(), true),
                            JoseHeader.jws(
                                    options.idTokenSigningAlgorithm(),
                                    Optional.of(options.idTokenSigningKeyId()),
                                    Optional.empty(),
                                    Optional.empty(),
                                    new JsonValue.ObjectValue(Map.of())),
                            codec.encodeClaims(claims)),
                    resolvedKey.key());
            final IdToken idToken = codec.encode(jwt);
            return Outcome.succeeded(new OpenIdTokenResponse(response, idToken));
        } catch (ValidateException exception) {
            return Outcome.rejected(
                    failure(
                            ErrorCode._400,
                            OAuth2ErrorCode.INVALID_GRANT,
                            "OpenID Connect requested ID Token claims cannot be satisfied"));
        } catch (RuntimeException exception) {
            return Outcome.failed(
                    failure(ErrorCode._500, OAuth2ErrorCode.SERVER_ERROR, "OpenID Connect ID Token signing failed"));
        }
    }

    /**
     * Validates that the loader returned exactly the configured, currently valid signing key.
     *
     * @param key resolved execution key
     * @param now current shared-clock instant
     * @throws ValidateException if identity, algorithm, or validity interval differs from the query
     */
    private void validateResolvedKey(final KeyMaterial key, final Instant now) {
        if (key == null || !options.idTokenSigningKeyId().equals(key.keyId())
                || !options.idTokenSigningAlgorithm().name().equals(key.algorithm()) || now.isBefore(key.notBefore())
                || !now.isBefore(key.notAfter())) {
            throw new ValidateException("OpenID Connect signing key does not match the configured active key");
        }
    }

    /**
     * Selects explicitly requested ID Token claims from current subject attributes.
     *
     * @param binding    authorization-code-bound OIDC context
     * @param attributes current subject attributes
     * @return immutable extension claim object containing only permitted requested attributes
     * @throws ValidateException if an essential claim is unavailable
     */
    private JsonValue.ObjectValue requestedClaims(
            final AuthorizationCodeCache.OpenIdBinding binding,
            final JsonValue.ObjectValue attributes) {
        final Map<String, JsonValue> selected = new LinkedHashMap<>();
        final JsonValue.ObjectValue root = binding.requestedClaims().getOrNull();
        if (root == null || root.values().get(OpenIdConnect.Claims.ID_TOKEN) == null) {
            return new JsonValue.ObjectValue(selected);
        }
        if (!(root.values().get(OpenIdConnect.Claims.ID_TOKEN) instanceof JsonValue.ObjectValue requests)) {
            throw new ValidateException("OpenID Connect ID Token claims request must be an object");
        }
        for (Map.Entry<String, JsonValue> entry : requests.values().entrySet()) {
            final String name = entry.getKey();
            if (registeredClaim(name)) {
                continue;
            }
            if (!options.claimsSupported().contains(name)) {
                if (essential(entry.getValue())) {
                    throw new ValidateException("OpenID Connect essential claim is unsupported");
                }
                continue;
            }
            final JsonValue value = attributes.values().get(name);
            if (value == null) {
                if (essential(entry.getValue())) {
                    throw new ValidateException("OpenID Connect essential claim is unavailable");
                }
                continue;
            }
            selected.put(name, value);
        }
        return new JsonValue.ObjectValue(selected);
    }

}
