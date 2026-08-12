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

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Client;
import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.JWT.VerificationPolicy;
import org.miaixz.bus.auth.metric.OAuth2.*;
import org.miaixz.bus.auth.metric.jwt.JWTPayload;
import org.miaixz.bus.auth.metric.jwt.JWTVerifier;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;

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
    private final Runtime runtime;

    /**
     * Creates one JWT-secured request verifier.
     *
     * @param policy  trusted OAuth policy
     * @param runtime validated authentication runtime
     */
    public JwtAuthorizationRequest(final Policy policy, final Runtime runtime) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.runtime = Assert.notNull(runtime, "Authentication runtime must be not null!");
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
     * @param claims verified claims
     * @param name   claim name
     * @return parsed URI
     */
    private static URI uri(final Map<String, Object> claims, final String name) {
        try {
            return URI.create(text(claims, name));
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
     * Reads one required non-blank string claim.
     *
     * @param claims verified claims
     * @param name   claim name
     * @return exact claim text
     */
    private static String text(final Map<String, Object> claims, final String name) {
        final Object value = claims.get(name);
        if (!(value instanceof String) || ((String) value).isBlank()) {
            throw invalid(null);
        }
        return (String) value;
    }

    /**
     * Reads one optional exact string claim.
     *
     * @param claims verified claims
     * @param name   claim name
     * @return absent or exact claim text
     */
    private static String optionalText(final Map<String, Object> claims, final String name) {
        final Object value = claims.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String) || ((String) value).isBlank()) {
            throw invalid(null);
        }
        return (String) value;
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
        final Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                ? failure.getCause()
                : failure;
        return cause == null ? new ProtocolException(ProtocolError.INVALID_REQUEST_OBJECT)
                : new ProtocolException(ProtocolError.INVALID_REQUEST_OBJECT.getKey(),
                        ProtocolError.INVALID_REQUEST_OBJECT.getValue(), cause);
    }

    /**
     * Verifies one signed request object and returns its canonical authorization request.
     *
     * @param invocation tenant-scoped operation context
     * @param request    outer authorization request carrying the JWT
     * @return stage containing the verified canonical request
     */
    public CompletionStage<AuthorizationRequest> verify(
            final Invocation invocation,
            final AuthorizationRequest request) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final AuthorizationRequest outer = Assert.notNull(request, "Authorization request must be not null!");
        if (StringKit.isBlank(outer.clientId()) || StringKit.isBlank(outer.requestObject())
                || outer.requestUri() != null) {
            throw invalid(null);
        }
        final CompletionStage<Optional<Client>> resolved = Assert.notNull(
                runtime.clients().resolve(context, outer.clientId()),
                "Client resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final Client client = OAuth2Validator.client(optional, outer.clientId());
            final VerificationPolicy jwtPolicy = new VerificationPolicy(policy.tokenAlgorithm(), client.id(),
                    Set.of(policy.issuer()), CLOCK_SKEW, MAXIMUM_LIFETIME, true);
            return JWTVerifier.verify(outer.requestObject(), jwtPolicy, context, runtime)
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
            final Client client) {
        final Map<String, Object> claims = payload.claims();
        require("code".equals(text(claims, "response_type")));
        final String clientId = text(claims, "client_id");
        require(outer.clientId().equals(clientId));
        final URI redirect = uri(claims, "redirect_uri");
        final Set<String> scopes = scopes(claims.get("scope"));
        final String state = optionalText(claims, "state");
        final String challenge = text(claims, "code_challenge");
        require(CodeChallengeMethod.S256.value().equals(text(claims, "code_challenge_method")));
        final ResponseMode responseMode = responseMode(optionalText(claims, "response_mode"));
        final String nonce = optionalText(claims, "nonce");
        consistent(outer.redirectUri(), redirect);
        consistent(outer.scopes().isEmpty() ? null : outer.scopes(), scopes);
        consistent(outer.state(), state);
        consistent(outer.codeChallenge(), challenge);
        consistent(outer.codeChallengeMethod(), CodeChallengeMethod.S256);
        consistent(outer.responseMode(), responseMode);
        consistent(outer.nonce(), nonce);
        final AuthorizationRequest canonical = new AuthorizationRequest(clientId, redirect, scopes, state, challenge,
                CodeChallengeMethod.S256, null, null, responseMode, nonce);
        OAuth2Validator.authorization(canonical, client, policy.scopes(), runtime.limits());
        return canonical;
    }

}
