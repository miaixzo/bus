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
package org.miaixz.bus.auth.metric.oidc;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.OAuth2.ProtocolError;
import org.miaixz.bus.auth.metric.OIDC.ProviderMetadata;
import org.miaixz.bus.auth.metric.shared.json.StrictJsonReader;
import org.miaixz.bus.auth.metric.shared.validation.UriValidator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;

/**
 * Fetches OpenID Provider metadata through the injected protocol transport. The resolver constructs the registered
 * discovery path, applies the closed HTTPS transport policy before I/O, accepts only a successful JSON response, and
 * requires the returned issuer to equal the configured issuer byte-for-byte.
 *
 * @author Kimi Liu
 */
public final class OidcDiscovery {

    /**
     * Authentication runtime.
     */
    private final Runtime runtime;

    /**
     * Creates one discovery resolver.
     *
     * @param runtime authentication runtime
     */
    public OidcDiscovery(final Runtime runtime) {
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("Authentication runtime must not be null"));
    }

    /**
     * Constructs the exact discovery endpoint below an issuer.
     *
     * @param issuer validated issuer URI
     * @return discovery endpoint
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
     * Tests whether a response declares the JSON media type.
     *
     * @param headers normalized response headers
     * @return whether the content type is JSON
     */
    private static boolean json(final Map<String, List<String>> headers) {
        final List<String> values = headers.get(Http.Header.CONTENT_TYPE.toLowerCase(Locale.ROOT));
        return values != null && values.size() == 1
                && values.get(0).toLowerCase(Locale.ROOT).startsWith(MediaType.APPLICATION_JSON);
    }

    /**
     * Resolves and validates one metadata endpoint.
     *
     * @param values decoded metadata
     * @param name   member name
     * @param policy strict transport policy
     * @return validated endpoint
     */
    private static URI endpoint(final Map<?, ?> values, final String name, final TransportPolicy policy) {
        try {
            final URI endpoint = URI.create(text(values, name));
            UriValidator.https(endpoint);
            return UriValidator.transport(endpoint, policy);
        } catch (final IllegalArgumentException failure) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST.getKey(),
                    ProtocolError.INVALID_REQUEST.getValue(), failure);
        }
    }

    /**
     * Reads one required string member.
     *
     * @param values decoded object
     * @param name   member name
     * @return required string
     */
    private static String text(final Map<?, ?> values, final String name) {
        final Object value = values.get(name);
        if (!(value instanceof String text) || text.isBlank() || text.getBytes(Charset.UTF_8).length > 8192) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        return text;
    }

    /**
     * Reads one required bounded string array.
     *
     * @param values decoded object
     * @param name   member name
     * @return immutable exact strings
     */
    private static Set<String> strings(final Map<?, ?> values, final String name) {
        final Object value = values.get(name);
        if (!(value instanceof List<?> list) || list.isEmpty() || list.size() > 128) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final LinkedHashSet<String> result = new LinkedHashSet<>();
        for (final Object item : list) {
            if (!(item instanceof String text) || text.isBlank() || !result.add(text)) {
                throw new ProtocolException(ProtocolError.INVALID_REQUEST);
            }
        }
        return Set.copyOf(result);
    }

    /**
     * Resolves and validates one provider metadata document.
     *
     * @param invocation operation context
     * @param issuer     exact expected issuer
     * @param policy     strict HTTPS transport policy
     * @return stage containing validated metadata
     */
    public CompletionStage<ProviderMetadata> resolve(
            final Invocation invocation,
            final String issuer,
            final TransportPolicy policy) {
        final Invocation context = Assert.notNull(invocation, "Invocation must be not null!");
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
        UriValidator.https(issuerUri);
        UriValidator.transport(issuerUri, transportPolicy);
        final URI endpoint = endpoint(issuerUri);
        UriValidator.transport(endpoint, transportPolicy);
        final Request request = new Request(Http.Method.GET, endpoint,
                Map.of(Http.Header.ACCEPT, List.of(MediaType.APPLICATION_JSON)), Map.of(), Normal.EMPTY, new byte[0]);
        final CompletionStage<Response> exchanged = Assert.notNull(
                runtime.transports().protocol().exchange(context, request, transportPolicy),
                "Protocol transport stage must be not null!");
        return exchanged.thenApply(response -> decode(response, expected, transportPolicy));
    }

    /**
     * Decodes and validates one metadata response.
     *
     * @param response       transport response
     * @param expectedIssuer exact expected issuer
     * @param policy         strict endpoint policy
     * @return validated metadata
     */
    private ProviderMetadata decode(
            final Response response,
            final String expectedIssuer,
            final TransportPolicy policy) {
        final Response source = Assert.notNull(response, "Discovery response must be not null!");
        if (source.status() != Http.Status.OK || !json(source.headers())) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final Object decoded = new StrictJsonReader(runtime.json(), runtime.limits()).read(source.body(), Map.class);
        if (!(decoded instanceof Map<?, ?> values)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final String actualIssuer = text(values, "issuer");
        if (!expectedIssuer.equals(actualIssuer)) {
            throw new ProtocolException(ProtocolError.INVALID_REQUEST);
        }
        final URI authorization = endpoint(values, "authorization_endpoint", policy);
        final URI token = endpoint(values, "token_endpoint", policy);
        final URI jwks = endpoint(values, "jwks_uri", policy);
        final URI userInfo = endpoint(values, "userinfo_endpoint", policy);
        final URI logout = endpoint(values, "end_session_endpoint", policy);
        return new ProviderMetadata(actualIssuer, authorization, token, jwks, userInfo, logout,
                strings(values, "response_types_supported"), strings(values, "subject_types_supported"),
                strings(values, "id_token_signing_alg_values_supported"));
    }

}
