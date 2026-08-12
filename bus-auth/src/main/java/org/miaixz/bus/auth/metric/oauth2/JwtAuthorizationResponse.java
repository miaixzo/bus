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
package org.miaixz.bus.auth.metric.oauth2;

import java.security.Key;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.KeyMaterial;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.JWT.TrustedAlgorithm;
import org.miaixz.bus.auth.metric.JWT.VerificationPolicy;
import org.miaixz.bus.auth.metric.OAuth2.AuthorizationResponse;
import org.miaixz.bus.auth.metric.OAuth2.Policy;
import org.miaixz.bus.auth.metric.OAuth2.ProtocolError;
import org.miaixz.bus.auth.metric.jwt.JWTCreator;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSignerBuilder;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Keeper;

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
    private final Runtime runtime;

    /**
     * Creates one JWT-secured response signer.
     *
     * @param policy  trusted OAuth policy
     * @param runtime validated authentication runtime
     */
    public JwtAuthorizationResponse(final Policy policy, final Runtime runtime) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.runtime = Assert.notNull(runtime, "Authentication runtime must be not null!");
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
    public CompletionStage<AuthorizationResponse> secure(
            final Invocation invocation,
            final String clientId,
            final AuthorizationResponse response) {
        final AuthorizationResponse input = Assert.notNull(response, "Authorization response must be not null!");
        if (StringKit.isBlank(input.code())) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final LinkedHashMap<String, Object> claims = new LinkedHashMap<>();
        claims.put("code", input.code());
        optional(claims, "state", input.state());
        return sign(invocation, clientId, input.redirectUri(), claims);
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
    public CompletionStage<AuthorizationResponse> error(
            final Invocation invocation,
            final String clientId,
            final java.net.URI redirectUri,
            final String state,
            final ProtocolError error) {
        final ProtocolError fixed = Assert.notNull(error, "OAuth protocol error must be not null!");
        final LinkedHashMap<String, Object> claims = new LinkedHashMap<>();
        claims.put("error", fixed.getKey());
        claims.put("error_description", fixed.getValue());
        optional(claims, "state", state);
        return sign(invocation, clientId, redirectUri, claims);
    }

    /**
     * Resolves the configured signing key and creates the compact response JWT.
     *
     * @param invocation  tenant-scoped operation context
     * @param clientId    exact client audience
     * @param redirectUri validated redirect URI
     * @param claims      success or error claims
     * @return stage containing the secured response
     */
    private CompletionStage<AuthorizationResponse> sign(
            final Invocation invocation,
            final String clientId,
            final java.net.URI redirectUri,
            final Map<String, Object> claims) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        Assert.notBlank(clientId, "Client identifier must be not blank!");
        Assert.notNull(redirectUri, "Redirect URI must be not null!");
        final CompletionStage<List<KeyMaterial>> resolved = Assert.notNull(
                runtime.keys().resolve(context, "sig", policy.tokenAlgorithm().identifier(), policy.signingKeyId()),
                "Key resolver stage must be not null!");
        return resolved.thenApply(candidates -> {
            final KeyMaterial material = select(candidates);
            final JWTSigner signer = signer(policy.tokenAlgorithm(), material.material());
            final VerificationPolicy jwtPolicy = new VerificationPolicy(policy.tokenAlgorithm(), policy.issuer(),
                    Set.of(clientId), Duration.ZERO, LIFETIME, false);
            final String token = JWTCreator.create(claims, jwtPolicy, runtime, signer);
            return new AuthorizationResponse(redirectUri, null, null, token);
        });
    }

    /**
     * Selects exactly one metadata-matching signing key.
     *
     * @param candidates resolver candidates
     * @return selected key material
     */
    private KeyMaterial select(final List<KeyMaterial> candidates) {
        if (candidates == null || candidates.isEmpty() || candidates.size() > runtime.limits().maxParameters()) {
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
