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
import java.security.SecureRandom;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.codec.json.JsonValues;
import org.miaixz.bus.auth.codec.state.StateJsonCodec;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.*;
import org.miaixz.bus.auth.resolver.SecretResolver;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonProvider;

/**
 * Stores and atomically consumes OAuth pushed authorization requests.
 * <p>
 * A request is fully validated and its client is authenticated before persistence. The returned request URI is an
 * opaque tenant-isolated reference with an exact 90-second lifetime. Consumption uses the state-store atomic take
 * operation, rejects client substitution, and therefore admits no replay winner after the first successful read.
 * </p>
 *
 * @author Kimi Liu
 */
public final class PushedAuthorizationRequest {

    /**
     * Exact pushed-request lifetime.
     */
    private static final Duration LIFETIME = Duration.ofSeconds(90);

    /**
     * Registered request URI prefix.
     */
    private static final String PREFIX = "urn:ietf:params:oauth:request_uri:";

    /**
     * Random byte count for an opaque request URI.
     */
    private static final int CREDENTIAL_BYTES = Normal._32;

    /**
     * Trusted OAuth policy.
     */
    private final Policy policy;

    /**
     * Resolvers and grant persistence required by OAuth flows.
     */
    private final OAuth2Dependencies dependencies;

    /**
     * Atomic tenant-scoped storage for one-time pushed requests.
     */
    private final StateStore states;

    /**
     * Tenant-aware resolver for confidential-client secrets.
     */
    private final SecretResolver secrets;

    /**
     * Cryptographically secure source for opaque request identifiers.
     */
    private final SecureRandom random;

    /**
     * JSON provider used only through the bounded state codec.
     */
    private final JsonProvider json;

    /**
     * Closed allocation and parsing limits applied to request state.
     */
    private final Limits limits;

    /**
     * Creates one pushed-request service.
     *
     * @param policy       trusted OAuth policy
     * @param dependencies non-null OAuth resolver and grant dependencies
     * @param states       non-null atomic state store
     * @param secrets      non-null client-secret resolver
     * @param random       non-null cryptographically secure random source
     * @param json         non-null JSON provider
     * @param limits       non-null parser and allocation limits
     * @throws IllegalArgumentException if any collaborator is {@code null}
     */
    public PushedAuthorizationRequest(final Policy policy, final OAuth2Dependencies dependencies,
            final StateStore states, final SecretResolver secrets, final SecureRandom random, final JsonProvider json,
            final Limits limits) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.dependencies = Assert.notNull(dependencies, "OAuth dependencies must be not null!");
        this.states = Assert.notNull(states, "State store must be not null!");
        this.secrets = Assert.notNull(secrets, "Secret resolver must be not null!");
        this.random = Assert.notNull(random, "Secure random must be not null!");
        this.json = Assert.notNull(json, "JSON provider must be not null!");
        this.limits = Assert.notNull(limits, "Limits must be not null!");
    }

    /**
     * Resolves one exact response-mode wire value.
     *
     * @param value exact wire value
     * @return response mode
     * @throws ProtocolException if the value is not a registered response mode
     */
    private static ResponseMode responseMode(final String value) {
        for (final ResponseMode mode : ResponseMode.values()) {
            if (mode.value().equals(value)) {
                return mode;
            }
        }
        throw new ProtocolException(ProtocolError.INVALID_REQUEST_URI);
    }

    /**
     * Converts a nullable string into its state representation.
     *
     * @param value optional value
     * @return non-null state value
     */
    private static String optional(final String value) {
        return value == null ? "" : value;
    }

    /**
     * Creates the fixed invalid-request-URI JSON member failure.
     *
     * @return new OAuth invalid-request-URI protocol failure
     */
    private static RuntimeException invalidRequestUri() {
        return new ProtocolException(ProtocolError.INVALID_REQUEST_URI);
    }

    /**
     * Authenticates, validates, and atomically stores one pushed request.
     *
     * @param invocation tenant-scoped operation context
     * @param request    pushed authorization input
     * @return stage containing the one-time request URI
     * @throws IllegalArgumentException if a required input or dependency result is {@code null}
     */
    public CompletionStage<PushedAuthorizationResponse> push(
            final Context invocation,
            final org.miaixz.bus.auth.protocol.oauth2.OAuth2.PushedAuthorizationRequest request) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final org.miaixz.bus.auth.protocol.oauth2.OAuth2.PushedAuthorizationRequest input = Assert
                .notNull(request, "Pushed authorization request must be not null!");
        final AuthorizationRequest authorization = input.authorization();
        final CompletionStage<Optional<RegisteredClient>> resolved = Assert.notNull(
                dependencies.clients().resolve(context, authorization.clientId()),
                "RegisteredClient resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final RegisteredClient client = OAuth2Validator.client(optional, authorization.clientId());
            OAuth2Validator.authorization(authorization, client, policy.scopes(), limits);
            return OAuth2Validator.authenticate(context, client, input.clientSecret(), secrets);
        }).thenCompose(client -> {
            final String requestUri = PREFIX
                    + OAuth2Support.credential(random, CREDENTIAL_BYTES, ProtocolError.TEMPORARILY_UNAVAILABLE);
            final String key = OAuth2Support.key(context, "par", requestUri);
            final CompletionStage<Boolean> created = Assert.notNull(
                    states.putIfAbsent(context, key, encode(authorization), LIFETIME),
                    "State-store create stage must be not null!");
            return created.thenCompose(
                    success -> Boolean.TRUE.equals(success)
                            ? CompletableFuture
                                    .completedFuture(new PushedAuthorizationResponse(requestUri, LIFETIME.toSeconds()))
                            : OAuth2Support.failed(ProtocolError.INVALID_REQUEST_URI));
        });
    }

    /**
     * Atomically consumes one request URI for its exact registered client.
     *
     * @param invocation tenant-scoped operation context
     * @param requestUri one-time pushed request URI
     * @param clientId   exact client identifier
     * @return stage containing the stored authorization request
     * @throws IllegalArgumentException if the context or state-store stage is {@code null}
     */
    public CompletionStage<AuthorizationRequest> take(
            final Context invocation,
            final String requestUri,
            final String clientId) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        if (StringKit.isBlank(requestUri) || !requestUri.startsWith(PREFIX) || StringKit.isBlank(clientId)) {
            return OAuth2Support.failed(ProtocolError.INVALID_REQUEST_URI);
        }
        final CompletionStage<Optional<byte[]>> taken = Assert.notNull(
                states.take(context, OAuth2Support.key(context, "par", requestUri)),
                "State-store take stage must be not null!");
        return taken.thenCompose(optional -> {
            if (optional == null || optional.isEmpty()) {
                return OAuth2Support.failed(ProtocolError.INVALID_REQUEST_URI);
            }
            final AuthorizationRequest stored = decode(optional.get());
            return clientId.equals(stored.clientId()) ? CompletableFuture.completedFuture(stored)
                    : OAuth2Support.failed(ProtocolError.INVALID_REQUEST_URI);
        });
    }

    /**
     * Encodes one authorization request into the common state envelope.
     *
     * @param request validated authorization request
     * @return encoded state
     * @throws ProtocolException if bounded JSON encoding fails
     */
    private byte[] encode(final AuthorizationRequest request) {
        final Map<String, Object> values = new LinkedHashMap<>();
        values.put("client", request.clientId());
        values.put("redirect", request.redirectUri().toASCIIString());
        values.put("scopes", OAuth2Support.encodeScopes(request.scopes()));
        values.put("state", optional(request.state()));
        values.put("challenge", request.codeChallenge());
        values.put("challenge_method", request.codeChallengeMethod().value());
        values.put("request_object", optional(request.requestObject()));
        values.put("response_mode", request.responseMode().value());
        values.put("nonce", optional(request.nonce()));
        try {
            return new StateJsonCodec(json, limits.maxJsonBytes(), limits.maxJsonDepth()).encode(values);
        } catch (final RuntimeException failure) {
            throw new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE.getKey(),
                    ProtocolError.TEMPORARILY_UNAVAILABLE.getValue(), failure);
        }
    }

    /**
     * Decodes one strict stored authorization request.
     *
     * @param envelope encoded state
     * @return immutable authorization request
     * @throws ProtocolException if the state envelope is malformed or exceeds a configured bound
     */
    private AuthorizationRequest decode(final byte[] envelope) {
        final Map<String, Object> values = new StateJsonCodec(json, limits.maxJsonBytes(), limits.maxJsonDepth())
                .decode(envelope);
        final int maximum = limits.maxJsonBytes();
        final Set<String> scopeSet = OAuth2Support.decodeScopes(
                JsonValues.text(values, "scopes", maximum, PushedAuthorizationRequest::invalidRequestUri),
                ProtocolError.INVALID_REQUEST_URI);
        try {
            final String state = JsonValues
                    .text(values, "state", maximum, PushedAuthorizationRequest::invalidRequestUri);
            final String requestObject = JsonValues
                    .text(values, "request_object", maximum, PushedAuthorizationRequest::invalidRequestUri);
            final String nonce = JsonValues
                    .text(values, "nonce", maximum, PushedAuthorizationRequest::invalidRequestUri);
            return new AuthorizationRequest(
                    JsonValues.requiredText(values, "client", maximum, PushedAuthorizationRequest::invalidRequestUri),
                    URI.create(
                            JsonValues.requiredText(
                                    values,
                                    "redirect",
                                    maximum,
                                    PushedAuthorizationRequest::invalidRequestUri)),
                    scopeSet, state.isEmpty() ? null : state,
                    JsonValues
                            .requiredText(values, "challenge", maximum, PushedAuthorizationRequest::invalidRequestUri),
                    CodeChallengeMethod.valueOf(
                            JsonValues.requiredText(
                                    values,
                                    "challenge_method",
                                    maximum,
                                    PushedAuthorizationRequest::invalidRequestUri)),
                    requestObject.isEmpty() ? null : requestObject, null,
                    responseMode(
                            JsonValues.requiredText(
                                    values,
                                    "response_mode",
                                    maximum,
                                    PushedAuthorizationRequest::invalidRequestUri)),
                    nonce.isEmpty() ? null : nonce);
        } catch (final IllegalArgumentException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST_URI.getKey(),
                    ProtocolError.INVALID_REQUEST_URI.getValue(), failure);
        }
    }

}
