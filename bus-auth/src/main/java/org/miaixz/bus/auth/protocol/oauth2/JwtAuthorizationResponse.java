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
package org.miaixz.bus.auth.protocol.oauth2;

import java.security.Key;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Callback.Mode;
import org.miaixz.bus.auth.Callback.Outbound;
import org.miaixz.bus.auth.Claims;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.protocol.jwt.JWT.TrustedAlgorithm;
import org.miaixz.bus.auth.protocol.jwt.JWT.VerificationPolicy;
import org.miaixz.bus.auth.protocol.jwt.JWTCreator;
import org.miaixz.bus.auth.protocol.jwt.KeyMaterial;
import org.miaixz.bus.auth.protocol.jwt.KeyResolver;
import org.miaixz.bus.auth.protocol.jwt.signature.JWTSigner;
import org.miaixz.bus.auth.protocol.jwt.signature.JWTSignerBuilder;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.Policy;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.ProtocolError;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

/**
 * Creates signed JWT-secured OAuth authorization success and error responses.
 * <p>
 * Signing algorithm and key identifier come exclusively from trusted policy. Private key material is resolved through
 * the runtime key port, filtered by exact use, algorithm, and key identifier, and passed to the completed M07 JWT
 * creator. Both success and error responses use the same issuer, audience, lifetime, and stable wire-error mapping.
 * </p>
 *
 * @author Kimi Liu
 */
public final class JwtAuthorizationResponse {

    /**
     * Exact JWT-secured authorization response lifetime.
     */
    private static final Duration LIFETIME = Duration.ofSeconds(60);

    /**
     * Trusted OAuth policy.
     */
    private final Policy policy;

    /**
     * Validated authentication runtime.
     */
    private final KeyResolver keys;

    /**
     * Fabric clock used for issued-at and expiration claims.
     */
    private final Clock clock;

    /**
     * Secure random source used for response JWT identifiers.
     */
    private final SecureRandom random;

    /**
     * Explicit JSON provider used for compact JWT serialization.
     */
    private final JsonProvider json;

    /**
     * Closed parser and allocation limits applied during JWT creation.
     */
    private final Limits limits;

    /**
     * Creates one JWT-secured response signer.
     *
     * @param policy trusted OAuth policy
     * @param keys   trusted signing-key resolver
     * @param clock  Fabric protocol clock
     * @param random secure random source for JWT identifiers
     * @param json   explicit JSON provider for serialization
     * @param limits closed parser and allocation limits
     */
    public JwtAuthorizationResponse(final Policy policy, final KeyResolver keys, final Clock clock,
            final SecureRandom random, final JsonProvider json, final Limits limits) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.keys = Assert.notNull(keys, "Key resolver must be not null!");
        this.clock = Assert.notNull(clock, "Clock must be not null!");
        this.random = Assert.notNull(random, "Secure random must be not null!");
        this.json = Assert.notNull(json, "JSON provider must be not null!");
        this.limits = Assert.notNull(limits, "Limits must be not null!");
    }

    /**
     * Builds one signer from trusted private or symmetric key material.
     *
     * @param algorithm trusted signing algorithm
     * @param material  encoded key material
     * @return signer bound to the trusted algorithm
     */
    private static JWTSigner signer(final TrustedAlgorithm algorithm, final byte[] material) {
        if (material == null || material.length == Normal._0) {
            throw new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE);
        }
        if (algorithm == TrustedAlgorithm.HS256) {
            return JWTSignerBuilder.createSigner(algorithm.identifier(), material);
        }
        final String keyAlgorithm = switch (algorithm) {
            case RS256, PS256 -> Algorithm.RSA.getValue();
            case ES256 -> Algorithm.EC.getValue();
            case EDDSA -> Algorithm.ED25519.getValue();
            case HS256 -> throw new IllegalStateException("HMAC key material is not asymmetric");
            case NONE -> throw new IllegalStateException("Unsecured JWT does not use asymmetric key material");
        };
        final Key privateKey = Keeper.generatePrivateKey(keyAlgorithm, material);
        return JWTSignerBuilder.createSigner(algorithm.identifier(), privateKey);
    }

    /**
     * Adds one optional claim only when present.
     *
     * @param claims destination claims
     * @param name   claim name
     * @param value  optional claim value
     */
    private static void optional(final Map<String, Object> claims, final String name, final String value) {
        if (value != null) {
            claims.put(name, value);
        }
    }

    /**
     * Signs one successful authorization response for the registered client audience.
     *
     * @param invocation tenant-scoped operation context
     * @param clientId   exact client audience
     * @param response   validated authorization response
     * @return stage containing the JWT-secured response
     */
    public CompletionStage<Outbound> secure(final Context invocation, final String clientId, final Outbound response) {
        final Outbound input = Assert.notNull(response, "Authorization response must be not null!");
        final String code = input.parameters().single("code").orElse(null);
        if (StringKit.isBlank(code)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final LinkedHashMap<String, Object> claims = new LinkedHashMap<>();
        claims.put("code", code);
        optional(claims, "state", input.parameters().single("state").orElse(null));
        return sign(invocation, clientId, input.destination().toUri(), input.mode(), claims);
    }

    /**
     * Signs one stable OAuth authorization error response.
     *
     * @param invocation  tenant-scoped operation context
     * @param clientId    exact client audience
     * @param redirectUri previously validated redirect URI
     * @param state       optional client state
     * @param error       fixed OAuth protocol error
     * @return stage containing the JWT-secured error response
     */
    public CompletionStage<Outbound> error(
            final Context invocation,
            final String clientId,
            final java.net.URI redirectUri,
            final String state,
            final ProtocolError error) {
        final ProtocolError fixed = Assert.notNull(error, "OAuth protocol error must be not null!");
        final LinkedHashMap<String, Object> claims = new LinkedHashMap<>();
        claims.put("error", fixed.getKey());
        claims.put("error_description", fixed.getValue());
        optional(claims, "state", state);
        return sign(invocation, clientId, redirectUri, Mode.JWT, claims);
    }

    /**
     * Resolves the configured signing key and creates the compact response JWT.
     *
     * @param invocation  tenant-scoped operation context
     * @param clientId    exact client audience
     * @param redirectUri validated redirect URI
     * @param mode        secured response transport mode
     * @param claims      success or error claims
     * @return stage containing the secured response
     */
    private CompletionStage<Outbound> sign(
            final Context invocation,
            final String clientId,
            final java.net.URI redirectUri,
            final Mode mode,
            final Map<String, Object> claims) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        Assert.notBlank(clientId, "Client identifier must be not blank!");
        Assert.notNull(redirectUri, "Redirect URI must be not null!");
        final CompletionStage<List<KeyMaterial>> resolved = Assert.notNull(
                keys.resolve(context, "sig", policy.tokenAlgorithm().identifier(), policy.signingKeyId()),
                "Key resolver stage must be not null!");
        return resolved.thenApply(candidates -> {
            final KeyMaterial material = select(candidates);
            final JWTSigner signer = signer(policy.tokenAlgorithm(), material.material());
            final VerificationPolicy jwtPolicy = new VerificationPolicy(policy.tokenAlgorithm(), policy.issuer(),
                    Set.of(clientId), Duration.ZERO, LIFETIME, false);
            final String token = JWTCreator.create(
                    Claims.from(claims),
                    jwtPolicy,
                    clock,
                    random,
                    json,
                    limits.maxHeaderBytes(),
                    limits.maxJsonBytes(),
                    limits.maxJwtBytes(),
                    signer);
            return Callback.outbound(context).destination(org.miaixz.bus.fabric.Address.from(redirectUri)).mode(mode)
                    .parameter("response", token).build();
        });
    }

    /**
     * Selects exactly one metadata-matching signing key.
     *
     * @param candidates resolver candidates
     * @return selected key material
     */
    private KeyMaterial select(final List<KeyMaterial> candidates) {
        if (candidates == null || candidates.isEmpty() || candidates.size() > limits.maxParameters()) {
            throw new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE);
        }
        KeyMaterial selected = null;
        for (final KeyMaterial candidate : candidates) {
            if (candidate != null && "sig".equals(candidate.use())
                    && policy.tokenAlgorithm().identifier().equals(candidate.algorithm())
                    && policy.signingKeyId().equals(candidate.keyId())) {
                if (selected != null) {
                    throw new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE);
                }
                selected = candidate;
            }
        }
        if (selected == null) {
            throw new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE);
        }
        return selected;
    }

}
