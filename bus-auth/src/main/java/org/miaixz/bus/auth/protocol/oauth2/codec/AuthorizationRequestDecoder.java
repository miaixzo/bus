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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.protocol.oauth2.AuthorizationRequest;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.ResponseType;
import org.miaixz.bus.auth.protocol.oauth2.Scope;
import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;

/**
 * Decodes a standard OAuth 2.x authorization endpoint query into its immutable protocol model.
 * <p>
 * Fabric has already performed strict URL percent and UTF-8 decoding. This codec preserves parameter order, rejects
 * every duplicate name, and leaves client registration and authorization policy to the server service.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AuthorizationRequestDecoder implements Decoder<HttpRequest, AuthorizationRequest> {

    /**
     * Creates a stateless strict authorization request decoder.
     */
    public AuthorizationRequestDecoder() {
        // No initialization required.
    }

    /**
     * Copies decoded URL pairs into an insertion-ordered unique-name map.
     *
     * @param url parsed Fabric request URL
     * @return mutable unique parameter map
     * @throws ValidateException if any parameter name occurs more than once
     */
    private static Map<String, List<String>> parameters(final UnoUrl url) {
        final Map<String, List<String>> values = new LinkedHashMap<>(url.querySize());
        for (int index = 0; index < url.querySize(); index++) {
            final String name = url.queryParameterName(index);
            final String value = url.queryParameterValue(index);
            final List<String> existing = values.computeIfAbsent(name, ignored -> new ArrayList<>());
            if (!existing.isEmpty() && !OAuth2.Parameters.RESOURCE.equals(name)) {
                throw new ValidateException("OAuth 2.x authorization request parameters must not be repeated");
            }
            existing.add(value);
        }
        return values;
    }

    /**
     * Removes one required non-empty parameter.
     *
     * @param parameters mutable unique parameter map
     * @param name       registered parameter name
     * @return required wire value
     * @throws ValidateException if the parameter is absent or empty
     */
    private static String required(final Map<String, List<String>> parameters, final String name) {
        final String value = optional(parameters, name);
        if (value == null || value.isEmpty()) {
            throw new ValidateException("OAuth 2.x authorization request requires non-empty " + name);
        }
        return value;
    }

    /**
     * Removes one optional parameter that may occur no more than once.
     *
     * @param parameters mutable grouped parameter map
     * @param name       registered parameter name
     * @return sole wire value or {@code null} when absent
     * @throws ValidateException if the parameter occurs more than once
     */
    private static String optional(final Map<String, List<String>> parameters, final String name) {
        final List<String> values = parameters.remove(name);
        if (values == null) {
            return null;
        }
        if (values.size() != 1) {
            throw new ValidateException("OAuth 2.x authorization parameter must not be repeated: " + name);
        }
        return values.get(0);
    }

    /**
     * Converts one unknown query parameter group to its provider-neutral extension representation.
     *
     * @param name   extension parameter name
     * @param values ordered wire values
     * @return string scalar or RFC 8707 string array
     * @throws ValidateException if a non-resource extension is repeated
     */
    private static JsonValue extension(final String name, final List<String> values) {
        if (values.size() == 1) {
            return new JsonValue.StringValue(values.get(0));
        }
        if (!OAuth2.Parameters.RESOURCE.equals(name)) {
            throw new ValidateException("OAuth 2.x authorization extension parameters must not be repeated");
        }
        return new JsonValue.ArrayValue(
                values.stream().map(JsonValue.StringValue::new).map(JsonValue.class::cast).toList());
    }

    /**
     * Decodes one GET authorization request without applying deployment policy.
     *
     * @param encoded immutable Fabric HTTP request
     * @return validated standard authorization request
     * @throws IllegalArgumentException if the request is {@code null}
     * @throws ValidateException        if method, body, fragment, multiplicity, or parameter syntax is invalid
     */
    @Override
    public AuthorizationRequest decode(final HttpRequest encoded) {
        Assert.notNull(encoded, "OAuth 2.x authorization HTTP request must not be null");
        if (encoded.method() != Http.Method.GET) {
            throw new ValidateException("OAuth 2.x authorization endpoint requires HTTP GET");
        }
        if (encoded.body().length() != 0) {
            throw new ValidateException("OAuth 2.x authorization GET request must not contain a body");
        }
        final UnoUrl url = encoded.url();
        if (url.fragment() != null) {
            throw new ValidateException("OAuth 2.x authorization request URL must not contain a fragment");
        }

        final Map<String, List<String>> parameters = parameters(url);
        final String responseType = required(parameters, OAuth2.Parameters.RESPONSE_TYPE);
        final String clientId = required(parameters, OAuth2.Parameters.CLIENT_ID);
        final String redirectUri = optional(parameters, OAuth2.Parameters.REDIRECT_URI);
        final String scope = optional(parameters, OAuth2.Parameters.SCOPE);
        final String state = optional(parameters, OAuth2.Parameters.STATE);
        final String codeChallenge = optional(parameters, OAuth2.Parameters.CODE_CHALLENGE);
        final String codeChallengeMethod = optional(parameters, OAuth2.Parameters.CODE_CHALLENGE_METHOD);
        final Map<String, JsonValue> extensions = new LinkedHashMap<>(parameters.size());
        parameters.forEach((name, values) -> extensions.put(name, extension(name, values)));

        return new AuthorizationRequest(new ResponseType(responseType), clientId, Optional.ofNullable(redirectUri),
                scope == null ? Optional.empty() : Optional.of(Scope.parse(scope)), Optional.ofNullable(state),
                Optional.ofNullable(codeChallenge), Optional.ofNullable(codeChallengeMethod),
                new JsonValue.ObjectValue(extensions));
    }

}
