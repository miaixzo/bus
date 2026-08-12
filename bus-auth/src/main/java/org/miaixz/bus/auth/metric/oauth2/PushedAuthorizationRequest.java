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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Client;
import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.OAuth2.*;
import org.miaixz.bus.auth.metric.shared.json.StrictJsonReader;
import org.miaixz.bus.auth.metric.shared.security.ReplayKey;
import org.miaixz.bus.auth.metric.shared.state.StateEnvelopeCodec;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.xyz.StringKit;

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
     * Validated authentication runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one pushed-request service.
     *
     * @param policy  trusted OAuth policy
     * @param runtime validated authentication runtime
     */
    public PushedAuthorizationRequest(final Policy policy, final Runtime runtime) {
        this.policy = Assert.notNull(policy, "OAuth policy must be not null!");
        this.runtime = Assert.notNull(runtime, "Authentication runtime must be not null!");
    }

    /**
     * Resolves one exact response-mode wire value.
     *
     * @param value exact wire value
     * @return response mode
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
     * Reads one required string member.
     *
     * @param values decoded object
     * @param name   member name
     * @return exact string
     */
    private static String text(final Map<?, ?> values, final String name) {
        final Object value = values.get(name);
        if (!(value instanceof String)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST_URI);
        }
        return (String) value;
    }

    /**
     * Reads one stored optional string.
     *
     * @param values decoded object
     * @param name   member name
     * @return null or exact string
     */
    private static String nullable(final Map<?, ?> values, final String name) {
        final String value = text(values, name);
        return value.isEmpty() ? null : value;
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
     * Derives one tenant-isolated pushed-request key.
     *
     * @param invocation tenant-scoped operation context
     * @param requestUri opaque request URI
     * @return protected lookup key
     */
    private static String key(final Invocation invocation, final String requestUri) {
        return ReplayKey.derive(invocation.tenantId(), "oauth2", "par", requestUri);
    }

    /**
     * Creates an already failed invalid-request-URI stage.
     *
     * @param <T> result type
     * @return failed stage
     */
    private static <T> CompletionStage<T> failed() {
        return CompletableFuture.failedFuture(new ProtocolException(ProtocolError.INVALID_REQUEST_URI));
    }

    /**
     * Authenticates, validates, and atomically stores one pushed request.
     *
     * @param invocation tenant-scoped operation context
     * @param request    pushed authorization input
     * @return stage containing the one-time request URI
     */
    public CompletionStage<PushedAuthorizationResponse> push(
            final Invocation invocation,
            final org.miaixz.bus.auth.metric.OAuth2.PushedAuthorizationRequest request) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        final org.miaixz.bus.auth.metric.OAuth2.PushedAuthorizationRequest input = Assert
                .notNull(request, "Pushed authorization request must be not null!");
        final AuthorizationRequest authorization = input.authorization();
        final CompletionStage<Optional<Client>> resolved = Assert.notNull(
                runtime.clients().resolve(context, authorization.clientId()),
                "Client resolver stage must be not null!");
        return resolved.thenCompose(optional -> {
            final Client client = OAuth2Validator.client(optional, authorization.clientId());
            OAuth2Validator.authorization(authorization, client, policy.scopes(), runtime.limits());
            return OAuth2Validator.authenticate(context, client, input.clientSecret(), runtime);
        }).thenCompose(client -> {
            final String requestUri = PREFIX + credential();
            final String key = key(context, requestUri);
            final CompletionStage<Boolean> created = Assert.notNull(
                    runtime.states().putIfAbsent(context, key, encode(authorization), LIFETIME),
                    "State-store create stage must be not null!");
            return created.thenCompose(
                    success -> Boolean.TRUE.equals(success)
                            ? CompletableFuture.completedFuture(
                                    new PushedAuthorizationResponse(requestUri, LIFETIME.toSeconds()))
                            : failed());
        });
    }

    /**
     * Atomically consumes one request URI for its exact registered client.
     *
     * @param invocation tenant-scoped operation context
     * @param requestUri one-time pushed request URI
     * @param clientId   exact client identifier
     * @return stage containing the stored authorization request
     */
    public CompletionStage<AuthorizationRequest> take(
            final Invocation invocation,
            final String requestUri,
            final String clientId) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
        if (StringKit.isBlank(requestUri) || !requestUri.startsWith(PREFIX) || StringKit.isBlank(clientId)) {
            return failed();
        }
        final CompletionStage<Optional<byte[]>> taken = Assert.notNull(
                runtime.states().take(context, key(context, requestUri)),
                "State-store take stage must be not null!");
        return taken.thenCompose(optional -> {
            if (optional == null || optional.isEmpty()) {
                return failed();
            }
            final AuthorizationRequest stored = decode(optional.get());
            return clientId.equals(stored.clientId()) ? CompletableFuture.completedFuture(stored) : failed();
        });
    }

    /**
     * Encodes one authorization request into the common state envelope.
     *
     * @param request validated authorization request
     * @return encoded state
     */
    private byte[] encode(final AuthorizationRequest request) {
        final Map<String, Object> values = new LinkedHashMap<>();
        values.put("client", request.clientId());
        values.put("redirect", request.redirectUri().toASCIIString());
        values.put("scopes", String.join(Symbol.SPACE, request.scopes()));
        values.put("state", optional(request.state()));
        values.put("challenge", request.codeChallenge());
        values.put("challenge_method", request.codeChallengeMethod().value());
        values.put("request_object", optional(request.requestObject()));
        values.put("response_mode", request.responseMode().value());
        values.put("nonce", optional(request.nonce()));
        final byte[] json = runtime.json().write(Map.copyOf(values));
        if (json == null || json.length == Normal._0 || json.length > runtime.limits().maxJsonBytes()) {
            throw new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE);
        }
        return StateEnvelopeCodec.encode(json);
    }

    /**
     * Decodes one strict stored authorization request.
     *
     * @param envelope encoded state
     * @return immutable authorization request
     */
    private AuthorizationRequest decode(final byte[] envelope) {
        final Object decoded = new StrictJsonReader(runtime.json(), runtime.limits())
                .read(StateEnvelopeCodec.decode(envelope), Map.class);
        if (!(decoded instanceof Map<?, ?> values)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST_URI);
        }
        final String scopes = text(values, "scopes");
        final Set<String> scopeSet = scopes.isEmpty() ? Set.of()
                : Set.copyOf(new LinkedHashSet<>(StringKit.split(scopes, Symbol.SPACE)));
        try {
            return new AuthorizationRequest(text(values, "client"), URI.create(text(values, "redirect")), scopeSet,
                    nullable(values, "state"), text(values, "challenge"),
                    CodeChallengeMethod.valueOf(text(values, "challenge_method")), nullable(values, "request_object"),
                    null, responseMode(text(values, "response_mode")), nullable(values, "nonce"));
        } catch (final IllegalArgumentException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST_URI.getKey(),
                    ProtocolError.INVALID_REQUEST_URI.getValue(), failure);
        }
    }

    /**
     * Generates one opaque request URI credential.
     *
     * @return Base64url credential
     */
    private String credential() {
        final byte[] value = runtime.random().nextBytes(CREDENTIAL_BYTES);
        if (value == null || value.length != CREDENTIAL_BYTES) {
            throw new ProtocolException(ProtocolError.TEMPORARILY_UNAVAILABLE);
        }
        return Base64.encodeUrlSafe(value);
    }

}
