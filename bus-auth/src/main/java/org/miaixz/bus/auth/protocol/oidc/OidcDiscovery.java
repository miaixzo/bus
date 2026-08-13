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
package org.miaixz.bus.auth.protocol.oidc;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.bridge.TransportPolicy;
import org.miaixz.bus.auth.codec.http.HttpValues;
import org.miaixz.bus.auth.codec.json.JsonValues;
import org.miaixz.bus.auth.codec.json.StrictJsonReader;
import org.miaixz.bus.auth.guard.UriValidator;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2.ProtocolError;
import org.miaixz.bus.auth.protocol.oidc.OIDC.ProviderMetadata;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.HttpX;

/**
 * Fetches OpenID Provider metadata through the injected protocol transport. The resolver constructs the registered
 * discovery path, applies the closed HTTPS transport policy before I/O, accepts only a successful JSON response, and
 * requires the returned issuer to equal the configured issuer byte-for-byte.
 *
 * @author Kimi Liu
 */
public final class OidcDiscovery {

    /**
     * Explicit Fabric context that owns outbound HTTP resources.
     */
    private final org.miaixz.bus.fabric.Context fabric;

    /**
     * JSON provider used only through the strict bounded reader.
     */
    private final JsonProvider json;

    /**
     * Closed HTTP and JSON parsing and allocation limits.
     */
    private final Limits limits;

    /**
     * Creates one discovery resolver.
     *
     * @param fabric non-null Fabric context for outbound HTTP
     * @param json   non-null JSON provider
     * @param limits non-null parser and allocation limits
     * @throws ValidateException if any collaborator is {@code null}
     */
    public OidcDiscovery(final org.miaixz.bus.fabric.Context fabric, final JsonProvider json, final Limits limits) {
        this.fabric = Assert.notNull(fabric, () -> new ValidateException("Fabric context must not be null"));
        this.json = Assert.notNull(json, () -> new ValidateException("JSON provider must not be null"));
        this.limits = Assert.notNull(limits, () -> new ValidateException("Limits must not be null"));
    }

    /**
     * Constructs the exact discovery endpoint below an issuer.
     *
     * @param issuer validated issuer URI
     * @return discovery endpoint
     * @throws ProtocolException if the discovery endpoint cannot be represented
     */
    private static URI endpoint(final URI issuer) {
        final String value = issuer.toASCIIString();
        final String suffix = value.endsWith(Symbol.SLASH) ? Http.Path.OPENID_CONFIGURATION.substring(1)
                : Http.Path.OPENID_CONFIGURATION;
        try {
            return URI.create(value + suffix);
        } catch (final IllegalArgumentException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                    ProtocolError.INVALID_REQUEST.getValue(), failure);
        }
    }

    /**
     * Resolves and validates one metadata endpoint.
     *
     * @param values decoded metadata
     * @param name   member name
     * @param policy strict transport policy
     * @return validated endpoint
     * @throws ProtocolException if a member is absent, malformed, non-HTTPS, or denied by transport policy
     */
    private static URI endpoint(final Map<?, ?> values, final String name, final TransportPolicy policy) {
        try {
            final URI endpoint = URI.create(JsonValues.requiredText(values, name, 8192, OidcDiscovery::invalidRequest));
            UriValidator.https(endpoint);
            return UriValidator.transport(endpoint, policy.addressPolicy());
        } catch (final RuntimeException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                    ProtocolError.INVALID_REQUEST.getValue(), failure);
        }
    }

    /**
     * Creates the fixed invalid-request failure.
     *
     * @return new OAuth invalid-request protocol failure
     */
    private static RuntimeException invalidRequest() {
        return new ProtocolException(ProtocolError.INVALID_REQUEST);
    }

    /**
     * Resolves and validates one provider metadata document.
     *
     * @param invocation operation context
     * @param issuer     exact expected issuer
     * @param policy     strict HTTPS transport policy
     * @return stage containing validated metadata
     * @throws IllegalArgumentException if a required input is {@code null} or blank
     */
    public CompletionStage<ProviderMetadata> resolve(
            final Context invocation,
            final String issuer,
            final TransportPolicy policy) {
        final Context context = Assert.notNull(invocation, "Context must be not null!");
        final String expected = Assert.notBlank(issuer, "OIDC issuer must be not blank!");
        final TransportPolicy transportPolicy = Assert.notNull(policy, "Transport policy must be not null!");
        final URI issuerUri;
        try {
            issuerUri = URI.create(expected);
        } catch (final IllegalArgumentException failure) {
            return CompletableFuture.failedFuture(
                    new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                            ProtocolError.INVALID_REQUEST.getValue(), failure));
        }
        final URI endpoint;
        try {
            UriValidator.https(issuerUri);
            UriValidator.transport(issuerUri, transportPolicy.addressPolicy());
            endpoint = endpoint(issuerUri);
            UriValidator.transport(endpoint, transportPolicy.addressPolicy());
        } catch (final RuntimeException failure) {
            return CompletableFuture.failedFuture(
                    new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                            ProtocolError.INVALID_REQUEST.getValue(), failure));
        }
        return CompletableFuture.supplyAsync(
                () -> HttpX.builder(fabric).get(endpoint.toASCIIString())
                        .header(Http.Header.ACCEPT, MediaType.APPLICATION_JSON)
                        .addressPolicy(transportPolicy.addressPolicy()).timeout(transportPolicy.timeout()).build()
                        .execute())
                .thenApply(response -> decode(response, expected, transportPolicy));
    }

    /**
     * Decodes and validates one metadata response.
     *
     * @param response       transport response
     * @param expectedIssuer exact expected issuer
     * @param policy         strict endpoint policy
     * @return validated metadata
     * @throws ProtocolException if status, media type, bounds, JSON, issuer, or endpoints are invalid
     */
    private ProviderMetadata decode(
            final HttpResponse response,
            final String expectedIssuer,
            final TransportPolicy policy) {
        try (HttpResponse source = Assert.notNull(response, "Discovery response must be not null!")) {
            if (source.code() != Http.Status.OK
                    || !HttpValues.json(source.headers().asMap(), OidcDiscovery::invalidRequest)) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
            final Object decoded = new StrictJsonReader(json, limits.maxJsonBytes(), limits.maxJsonDepth())
                    .read(source.bytes(limits.maxJsonBytes()), Map.class);
            if (!(decoded instanceof Map<?, ?> values)) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
            return metadata(values, expectedIssuer, policy);
        }
    }

    /**
     * Maps a validated discovery JSON object to immutable provider metadata.
     *
     * @param values         decoded discovery document
     * @param expectedIssuer exact expected issuer
     * @param policy         strict endpoint policy
     * @return immutable validated provider metadata
     * @throws ProtocolException if issuer, endpoints, or supported-value sets are invalid
     */
    private ProviderMetadata metadata(
            final Map<?, ?> values,
            final String expectedIssuer,
            final TransportPolicy policy) {
        final String actualIssuer = JsonValues.requiredText(values, "issuer", 8192, OidcDiscovery::invalidRequest);
        if (!expectedIssuer.equals(actualIssuer)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final URI authorization = endpoint(values, "authorization_endpoint", policy);
        final URI token = endpoint(values, "token_endpoint", policy);
        final URI jwks = endpoint(values, "jwks_uri", policy);
        final URI userInfo = endpoint(values, "userinfo_endpoint", policy);
        final URI logout = endpoint(values, "end_session_endpoint", policy);
        return new ProviderMetadata(actualIssuer, authorization, token, jwks, userInfo, logout,
                JsonValues.stringSet(values, "response_types_supported", 128, 8192, OidcDiscovery::invalidRequest),
                JsonValues.stringSet(values, "subject_types_supported", 128, 8192, OidcDiscovery::invalidRequest),
                JsonValues.stringSet(
                        values,
                        "id_token_signing_alg_values_supported",
                        128,
                        8192,
                        OidcDiscovery::invalidRequest));
    }

}
