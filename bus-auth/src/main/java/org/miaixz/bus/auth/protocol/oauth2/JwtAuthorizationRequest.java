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

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.codec.json.JsonValues;
import org.miaixz.bus.auth.protocol.jwt.JWT.VerificationPolicy;
import org.miaixz.bus.auth.protocol.jwt.JWTPayload;
import org.miaixz.bus.auth.protocol.jwt.JWTVerifier;
import org.miaixz.bus.auth.protocol.jwt.KeyResolver;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.*;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

/**
 * Verifies and materializes JWT-secured OAuth authorization requests.
 * <p>
 * The trusted algorithm, issuer, and authorization-server audience are selected independently of protected-header and
 * claim input. The common JWT verifier performs signature, time, and mandatory {@code jti} replay admission before this
 * class reads OAuth claims. Any outer parameter duplicated by the request object must match exactly.
 * </p>
 *
 * @author Kimi Liu
 */
public final class JwtAuthorizationRequest {

    /**
     * Maximum accepted request-object lifetime.
     */
    private static final Duration MAXIMUM_LIFETIME = Duration.ofMinutes(5);

    /**
     * Accepted clock skew for request objects.
     */
    private static final Duration CLOCK_SKEW = Duration.ofSeconds(30);

    /**
     * Trusted OAuth policy.
     */
    private final Policy policy;

    /**
     * Validated authentication runtime.
     */
    private final OAuth2Dependencies dependencies;

    /**
     * Explicit JSON provider used by the common strict JWT verifier.
     */
    private final JsonProvider json;

    /**
     * Fabric clock used for request-object time validation.
     */
    private final Clock clock;

    /**
     * Trusted key resolver used by the common JWT verifier.
     */
    private final KeyResolver keys;

    /**
     * Tenant-isolated replay state store used for mandatory request-object {@code jti}.
     */
    private final StateStore states;

    /**
     * Closed parser and allocation limits applied to the request object.
     */
    private final Limits limits;

    /**
     * Creates one JWT-secured request verifier.
     *
     * @param policy       trusted OAuth policy
     * @param dependencies registered-client and authorization-grant product ports
     * @param json         explicit JSON provider for JWT parsing
     * @param clock        Fabric clock for time validation
     * @param keys         trusted JWT verification-key resolver
     * @param states       tenant-isolated replay state store
     * @param limits       closed parser and allocation limits
     */
    public JwtAuthorizationRequest(final Policy policy, final OAuth2Dependencies dependencies, final JsonProvider json,
            final Clock clock, final KeyResolver keys, final StateStore states, final Limits limits) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.dependencies = Assert.notNull(dependencies, "OAuth dependencies must be not null!");
        this.json = Assert.notNull(json, "JSON provider must be not null!");
        this.clock = Assert.notNull(clock, "Clock must be not null!");
        this.keys = Assert.notNull(keys, "Key resolver must be not null!");
        this.states = Assert.notNull(states, "State store must be not null!");
        this.limits = Assert.notNull(limits, "Limits must be not null!");
    }

    /**
     * Parses one exact request-object response mode.
     *
     * @param value optional wire value
     * @return query mode when absent or exact registered mode
     */
    private static ResponseMode responseMode(final String value) {
        if (value == null) {
            return ResponseMode.QUERY;
        }
        for (final ResponseMode mode : ResponseMode.values()) {
            if (mode.value().equals(value)) {
                return mode;
            }
        }
        throw invalid(null);
    }

    /**
     * Parses a required absolute URI claim.
     *
     * @param claims       verified claims
     * @param name         claim name
     * @param maximumBytes maximum accepted UTF-8 bytes
     * @return parsed URI
     */
    private static URI uri(final Map<String, Object> claims, final String name, final int maximumBytes) {
        try {
            return URI.create(JsonValues.requiredText(claims, name, maximumBytes, () -> invalid(null)));
        } catch (final IllegalArgumentException failure) {
            throw invalid(failure);
        }
    }

    /**
     * Parses a scope claim represented by a space-delimited string or string collection.
     *
     * @param value scope claim
     * @return immutable scope set
     */
    private static Set<String> scopes(final Object value) {
        final LinkedHashSet<String> result = new LinkedHashSet<>();
        if (value instanceof String text) {
            result.addAll(StringKit.split(text, Symbol.SPACE));
        } else if (value instanceof Collection<?> values) {
            for (final Object item : values) {
                require(item instanceof String && !((String) item).isBlank());
                result.add((String) item);
            }
        } else {
            throw invalid(null);
        }
        require(!result.isEmpty());
        return Set.copyOf(result);
    }

    /**
     * Requires an outer duplicate to equal the verified claim.
     *
     * @param outer    optional outer value
     * @param verified verified request-object value
     */
    private static void consistent(final Object outer, final Object verified) {
        if (outer != null && !outer.equals(verified)) {
            throw invalid(null);
        }
    }

    /**
     * Requires one request-object invariant.
     *
     * @param condition required condition
     */
    private static void require(final boolean condition) {
        if (!condition) {
            throw invalid(null);
        }
    }

    /**
     * Maps every request-object failure to the fixed OAuth wire error.
     *
     * @param failure optional underlying failure
     * @return stable protocol failure
     */
    private static ProtocolException invalid(final Throwable failure) {
        final Throwable cause = failure == null ? null : ExceptionKit.unwrap(failure);
        return cause == null ? new ProtocolException(ProtocolError.INVALID_REQUEST_OBJECT)
                : new ProtocolException(ProtocolError.INVALID_REQUEST_OBJECT.getKey(),
                        ProtocolError.INVALID_REQUEST_OBJECT.getValue(), cause);
    }

    /**
     * Returns the verified payload or rejects a non-success JWT outcome.
     */
    private static JWTPayload success(final org.miaixz.bus.auth.Outcome<JWTPayload> outcome) {
        if (outcome instanceof org.miaixz.bus.auth.Outcome.Success<JWTPayload> success) {
            return success.value();
        }
        throw new ProtocolException(ProtocolError.INVALID_REQUEST);
    }

    /**
     * Verifies one signed request object and returns its canonical authorization request.
     *
     * @param invocation tenant-scoped operation context
     * @param request    outer authorization request carrying the JWT
     * @return stage containing the verified canonical request
     */
    public CompletionStage<AuthorizationRequest> verify(final Context invocation, final AuthorizationRequest request) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final AuthorizationRequest outer = Assert.notNull(request, "Authorization request must be not null!");
        if (StringKit.isBlank(outer.clientId()) || StringKit.isBlank(outer.requestObject())
                || outer.requestUri() != null) {
            throw invalid(null);
        }
        final CompletionStage<Optional<RegisteredClient>> resolved = Assert.notNull(
                dependencies.clients().resolve(context, outer.clientId()),
                "RegisteredClient resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final RegisteredClient client = OAuth2Validator.client(optional, outer.clientId());
            final VerificationPolicy jwtPolicy = new VerificationPolicy(policy.tokenAlgorithm(), client.id(),
                    Set.of(policy.issuer()), CLOCK_SKEW, MAXIMUM_LIFETIME, true);
            return JWTVerifier
                    .verify(
                            outer.requestObject(),
                            jwtPolicy,
                            context,
                            json,
                            clock,
                            keys,
                            states,
                            limits.maxJwtBytes(),
                            limits.maxHeaderBytes(),
                            limits.maxJsonBytes(),
                            limits.maxJsonDepth(),
                            limits.maxParameters())
                    .thenApply(JwtAuthorizationRequest::success)
                    .thenApply(payload -> canonical(outer, payload, client));
        }).exceptionally(failure -> {
            throw invalid(failure);
        });
    }

    /**
     * Builds and validates the canonical request from verified claims.
     *
     * @param outer   outer request parameters
     * @param payload verified immutable JWT payload
     * @param client  asynchronously resolved registered client
     * @return canonical authorization request
     */
    private AuthorizationRequest canonical(
            final AuthorizationRequest outer,
            final JWTPayload payload,
            final RegisteredClient client) {
        final Map<String, Object> claims = payload.snapshot();
        final int maximum = limits.maxParameterBytes();
        require("code".equals(JsonValues.requiredText(claims, "response_type", maximum, () -> invalid(null))));
        final String clientId = JsonValues.requiredText(claims, "client_id", maximum, () -> invalid(null));
        require(outer.clientId().equals(clientId));
        final URI redirect = uri(claims, "redirect_uri", maximum);
        final Set<String> scopes = scopes(claims.get("scope"));
        final String state = JsonValues.optionalText(claims, "state", maximum, () -> invalid(null));
        final String challenge = JsonValues.requiredText(claims, "code_challenge", maximum, () -> invalid(null));
        require(
                CodeChallengeMethod.S256.value().equals(
                        JsonValues.requiredText(claims, "code_challenge_method", maximum, () -> invalid(null))));
        final ResponseMode responseMode = responseMode(
                JsonValues.optionalText(claims, "response_mode", maximum, () -> invalid(null)));
        final String nonce = JsonValues.optionalText(claims, "nonce", maximum, () -> invalid(null));
        consistent(outer.redirectUri(), redirect);
        consistent(outer.scopes().isEmpty() ? null : outer.scopes(), scopes);
        consistent(outer.state(), state);
        consistent(outer.codeChallenge(), challenge);
        consistent(outer.codeChallengeMethod(), CodeChallengeMethod.S256);
        consistent(outer.responseMode(), responseMode);
        consistent(outer.nonce(), nonce);
        final AuthorizationRequest canonical = new AuthorizationRequest(clientId, redirect, scopes, state, challenge,
                CodeChallengeMethod.S256, null, null, responseMode, nonce);
        OAuth2Validator.authorization(canonical, client, policy.scopes(), limits);
        return canonical;
    }

}
