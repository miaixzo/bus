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
package org.miaixz.bus.auth.source.protocol.oauth2.codec;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.FabricX.UrlBuilder;
import org.miaixz.bus.auth.source.protocol.oauth2.AuthorizationRequest;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Encodes a standard OAuth 2.x authorization request into an authorization endpoint URL.
 * <p>
 * The encoder performs no browser navigation or network operation. It preserves deployment-owned endpoint query
 * parameters and appends typed protocol members plus scalar extension values using Fabric URL encoding.
 * </p>
 *
 * @author Kimi Liu
 */
public class AuthorizationRequestEncoder implements Encoder<AuthorizationRequest, Url> {

    /**
     * Validated deployment authorization endpoint.
     */
    private final Endpoint endpoint;

    /**
     * Creates an encoder bound to one HTTPS GET authorization endpoint.
     *
     * @param endpoint immutable authorization endpoint
     * @throws IllegalArgumentException if the endpoint is {@code null}
     * @throws ValidateException        if transport, method, user information, fragment, or preset query conflicts
     */
    public AuthorizationRequestEncoder(final Endpoint endpoint) {
        this.endpoint = Assert.notNull(endpoint, "OAuth 2.x authorization endpoint must not be null");
        if (endpoint.transport() != Endpoint.Transport.HTTPS || endpoint.method().isEmpty()
                || endpoint.method().getOrNull() != Http.Method.GET) {
            throw new ValidateException("OAuth 2.x authorization endpoint must use HTTPS GET");
        }
        final URI uri = endpoint.url().toUri();
        if (uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
            throw new ValidateException("OAuth 2.x authorization endpoint must not contain userinfo or fragment");
        }
        for (String name : endpoint.url().queryParameterNames()) {
            if (requestParameter(name)) {
                throw new ValidateException(
                        "OAuth 2.x authorization endpoint must not predefine request parameter: " + name);
            }
        }
    }

    /**
     * Appends one implementation-neutral extension using its permitted query representation.
     *
     * @param url   destination URL builder
     * @param name  extension parameter name
     * @param value implementation-neutral extension value
     * @throws ValidateException if a reserved name or unsupported JSON shape is supplied
     */
    private static void extension(final UrlBuilder url, final String name, final JsonValue value) {
        if (coreParameter(name)) {
            throw new ValidateException("OAuth 2.x authorization extension duplicates a registered parameter");
        }
        if (OAuth2.Parameters.RESOURCE.equals(name)) {
            resources(url, value);
        } else if (value instanceof JsonValue.StringValue text) {
            url.query(name, text.value());
        } else if (value instanceof JsonValue.NumberValue number) {
            url.query(name, number.value().toString());
        } else if (value instanceof JsonValue.BooleanValue flag) {
            url.query(name, Boolean.toString(flag.value()));
        } else {
            throw new ValidateException("OAuth 2.x authorization query extensions must be JSON scalars");
        }
    }

    /**
     * Identifies parameters represented by dedicated authorization request components.
     *
     * @param name exact authorization request parameter name
     * @return {@code true} when the parameter is represented by a core request component
     */
    private static boolean coreParameter(final String name) {
        return switch (name) {
            case OAuth2.Parameters.RESPONSE_TYPE, OAuth2.Parameters.CLIENT_ID, OAuth2.Parameters.REDIRECT_URI, OAuth2.Parameters.SCOPE, OAuth2.Parameters.STATE -> true;
            default -> false;
        };
    }

    /**
     * Identifies parameters that the authorization request owns at the endpoint boundary.
     *
     * @param name exact endpoint query parameter name
     * @return {@code true} for a core or recognized extension request parameter
     */
    private static boolean requestParameter(final String name) {
        return coreParameter(name) || switch (name) {
            case OAuth2.Parameters.CODE_CHALLENGE, OAuth2.Parameters.CODE_CHALLENGE_METHOD, OAuth2.Parameters.RESOURCE -> true;
            default -> false;
        };
    }

    /**
     * Appends one or more validated RFC 8707 resource indicator parameters.
     *
     * @param url   destination URL builder
     * @param value string scalar or string-array extension value
     * @throws ValidateException if type, cardinality, uniqueness, or URI syntax is invalid
     */
    private static void resources(final UrlBuilder url, final JsonValue value) {
        final Set<String> resources = new LinkedHashSet<>();
        if (value instanceof JsonValue.StringValue text) {
            resources.add(resource(text.value()));
        } else if (value instanceof JsonValue.ArrayValue array && !array.values().isEmpty()) {
            for (JsonValue item : array.values()) {
                if (!(item instanceof JsonValue.StringValue text) || !resources.add(resource(text.value()))) {
                    throw new ValidateException("OAuth 2.x resource indicators must be unique JSON strings");
                }
            }
        } else {
            throw new ValidateException("OAuth 2.x resource extension must be a string or non-empty string array");
        }
        resources.forEach(resource -> url.query(OAuth2.Parameters.RESOURCE, resource));
    }

    /**
     * Validates one RFC 8707 absolute fragment-free resource indicator.
     *
     * @param value resource indicator wire value
     * @return unchanged validated value
     * @throws ValidateException if the value is empty or is not an absolute fragment-free URI
     */
    private static String resource(final String value) {
        Assert.notEmpty(value, "OAuth 2.x resource indicator must not be empty");
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.getRawFragment() != null) {
                throw new ValidateException("OAuth 2.x resource indicator must be an absolute URI without fragment");
            }
            return value;
        } catch (URISyntaxException exception) {
            throw new ValidateException("OAuth 2.x resource indicator must be a valid URI", exception);
        }
    }

    /**
     * Appends one typed authorization request to the configured endpoint URL.
     *
     * @param data validated standard authorization request
     * @return immutable absolute authorization request URL
     * @throws IllegalArgumentException if data are {@code null}
     * @throws ValidateException        if an extension cannot be represented by the standard query contract
     */
    @Override
    public Url encode(final AuthorizationRequest data) {
        Assert.notNull(data, "OAuth 2.x authorization request must not be null");
        final UrlBuilder url = endpoint.url().newBuilder()
                .query(OAuth2.Parameters.RESPONSE_TYPE, data.responseType().value())
                .query(OAuth2.Parameters.CLIENT_ID, data.clientId());
        data.redirectUri().ifPresent(value -> url.query(OAuth2.Parameters.REDIRECT_URI, value));
        data.scope().ifPresent(value -> url.query(OAuth2.Parameters.SCOPE, value.format()));
        data.state().ifPresent(value -> url.query(OAuth2.Parameters.STATE, value));
        data.extensions().values().forEach((name, value) -> extension(url, name, value));
        return url.build();
    }

}
