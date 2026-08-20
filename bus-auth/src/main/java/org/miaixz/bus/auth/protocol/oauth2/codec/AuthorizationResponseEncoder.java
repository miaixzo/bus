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
package org.miaixz.bus.auth.protocol.oauth2.codec;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.codec.Parameter;
import org.miaixz.bus.auth.codec.QueryCodec;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationCodeResponse;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationErrorResponse;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationResponse;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Encodes an OAuth 2.x authorization success or error response as a safe HTTP redirect.
 * <p>
 * The redirect target has already passed exact client-registration validation in the authorization service. This codec
 * validates only the transport shape, appends standard response parameters, and never issues or verifies a code.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AuthorizationResponseEncoder {

    /**
     * Creates a stateless authorization response encoder.
     */
    public AuthorizationResponseEncoder() {
        // No initialization required.
    }

    /**
     * Appends one scalar response extension without creating JSON query syntax.
     *
     * @param parameters mutable ordered response parameter list
     * @param name       extension parameter name
     * @param value      provider-neutral scalar value
     * @throws ValidateException if a non-scalar JSON shape is supplied
     */
    private static void extension(final List<Parameter> parameters, final String name, final JsonValue value) {
        if (value instanceof JsonValue.StringValue text) {
            parameters.add(new Parameter(name, text.value()));
        } else if (value instanceof JsonValue.NumberValue number) {
            parameters.add(new Parameter(name, number.value().toString()));
        } else if (value instanceof JsonValue.BooleanValue flag) {
            parameters.add(new Parameter(name, Boolean.toString(flag.value())));
        } else {
            throw new ValidateException("OAuth 2.x authorization response extensions must be JSON scalars");
        }
    }

    /**
     * Converts one typed authorization response branch into ordered wire parameters.
     *
     * @param response standard authorization response
     * @return ordered response parameter list
     */
    private static List<Parameter> parameters(final AuthorizationResponse response) {
        final List<Parameter> parameters = new ArrayList<>();
        switch (response) {
            case AuthorizationCodeResponse success -> {
                parameters.add(new Parameter(OAuth2.Parameters.CODE, success.code()));
                success.state().ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.STATE, value)));
                success.extensions().values().forEach((name, value) -> extension(parameters, name, value));
            }
            case AuthorizationErrorResponse error -> {
                parameters.add(new Parameter(OAuth2.Parameters.ERROR, error.error().value()));
                error.errorDescription()
                        .ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.ERROR_DESCRIPTION, value)));
                error.errorUri().ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.ERROR_URI, value)));
                error.state().ifPresent(value -> parameters.add(new Parameter(OAuth2.Parameters.STATE, value)));
                error.extensions().values().forEach((name, value) -> extension(parameters, name, value));
            }
        }
        return List.copyOf(parameters);
    }

    /**
     * Validates the absolute fragment-free redirect transport shape without redoing client registration.
     *
     * @param value exact redirect URI lexical value
     * @return parsed absolute redirect URI retaining the validated lexical components
     * @throws ValidateException if URI syntax, absoluteness, or fragment policy is invalid
     */
    private static URI validateRedirect(final String value) {
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ValidateException(
                        "OAuth 2.x authorization redirect URI must be absolute, userinfo-free, and fragment-free");
            }
            return uri;
        } catch (URISyntaxException exception) {
            throw new ValidateException("OAuth 2.x authorization redirect URI must be a valid URI", exception);
        }
    }

    /**
     * Tests whether decoded existing redirect query parameters contain one exact name.
     *
     * @param parameters decoded existing redirect query
     * @param name       candidate response parameter name
     * @return whether the name already exists
     */
    private static boolean contains(final List<Parameter> parameters, final String name) {
        return parameters.stream().anyMatch(parameter -> parameter.name().equals(name));
    }

    /**
     * Appends a standard success or error response to an already validated redirect URI.
     *
     * @param request     originating Fabric authorization request
     * @param redirectUri exact redirect URI validated by the authorization service
     * @param response    standard authorization success or error response
     * @return complete empty HTTP 302 response
     * @throws IllegalArgumentException if an argument is {@code null} or redirect text is blank
     * @throws ValidateException        if the redirect or an extension cannot be represented safely
     */
    public HttpResponse encode(
            final HttpRequest request,
            final String redirectUri,
            final AuthorizationResponse response) {
        Assert.notNull(request, "OAuth 2.x authorization HTTP request must not be null");
        Assert.notBlank(redirectUri, "OAuth 2.x authorization redirect URI must not be blank");
        Assert.notNull(response, "OAuth 2.x authorization response must not be null");
        final URI target = validateRedirect(redirectUri);
        final QueryCodec queryCodec = new QueryCodec();
        final List<Parameter> existing = target.getRawQuery() == null ? List.of()
                : queryCodec.decode(target.getRawQuery());
        final List<Parameter> parameters = parameters(response);
        for (Parameter parameter : parameters) {
            if (contains(existing, parameter.name())) {
                throw new ValidateException(
                        "OAuth 2.x redirect URI must not predefine response parameter: " + parameter.name());
            }
        }
        final String encoded = queryCodec.encode(parameters);
        final String separator = target.getRawQuery() == null ? Symbol.QUESTION_MARK
                : target.getRawQuery().isEmpty() || target.getRawQuery().endsWith(Symbol.AND) ? Normal.EMPTY
                        : Symbol.AND;
        final String location = redirectUri + separator + encoded;
        return HttpResponse.builder().request(request).code(Http.Status.FOUND)
                .headers(
                        Headers.of(
                                Http.Header.LOCATION,
                                location,
                                Http.Header.CACHE_CONTROL,
                                Http.Cache.NO_STORE,
                                Http.Header.PRAGMA,
                                Http.Cache.NO_CACHE))
                .build();
    }

}
