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

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationCodeResponse;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationErrorResponse;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.protocol.oauth2.OAuth2ErrorCode;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Decodes an OAuth 2.x browser callback into exactly one standard success or error response model.
 * <p>
 * The nested discriminated result exists only to make the two mutually exclusive Java types explicit. It is never
 * encoded as an OAuth parameter set or custom HTTP response body.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AuthorizationResponseDecoder
        implements Decoder<Callback.Inbound, AuthorizationResponseDecoder.Decoded> {

    /**
     * Creates a stateless strict callback response decoder.
     */
    public AuthorizationResponseDecoder() {
        // No initialization required.
    }

    /**
     * Decodes a successful authorization response branch.
     *
     * @param parameters mutable unique callback parameter map
     * @return discriminated standard success response
     */
    private static Success success(final Map<String, String> parameters) {
        if (parameters.containsKey(OAuth2.Parameters.ERROR_DESCRIPTION)
                || parameters.containsKey(OAuth2.Parameters.ERROR_URI)) {
            throw new ValidateException(
                    "OAuth 2.x successful authorization response must not contain error parameters");
        }
        final String code = required(parameters, OAuth2.Parameters.CODE);
        final String state = parameters.remove(OAuth2.Parameters.STATE);
        parameters.remove(OAuth2.Parameters.ERROR);
        parameters.remove(OAuth2.Parameters.ERROR_DESCRIPTION);
        parameters.remove(OAuth2.Parameters.ERROR_URI);
        return new Success(new AuthorizationCodeResponse(code, Optional.ofNullable(state), extensions(parameters)));
    }

    /**
     * Decodes an authorization error response branch.
     *
     * @param parameters mutable unique callback parameter map
     * @return discriminated standard error response
     */
    private static Error error(final Map<String, String> parameters) {
        if (parameters.containsKey(OAuth2.Parameters.CODE) || parameters.containsKey(OAuth2.Parameters.SCOPE)
                || parameters.containsKey(JwtClaims.ISSUER)) {
            throw new ValidateException("OAuth 2.x authorization error response must not contain success parameters");
        }
        final String error = required(parameters, OAuth2.Parameters.ERROR);
        final String description = parameters.remove(OAuth2.Parameters.ERROR_DESCRIPTION);
        final String errorUri = parameters.remove(OAuth2.Parameters.ERROR_URI);
        final String state = parameters.remove(OAuth2.Parameters.STATE);
        return new Error(new AuthorizationErrorResponse(new OAuth2ErrorCode(error), Optional.ofNullable(description),
                Optional.ofNullable(errorUri), Optional.ofNullable(state), extensions(parameters)));
    }

    /**
     * Converts remaining unique callback parameters into string-valued protocol extensions.
     *
     * @param parameters unconsumed callback parameters
     * @return immutable extension object
     */
    private static JsonValue.ObjectValue extensions(final Map<String, String> parameters) {
        final Map<String, JsonValue> extensions = new LinkedHashMap<>();
        parameters.forEach((name, value) -> extensions.put(name, new JsonValue.StringValue(value)));
        return new JsonValue.ObjectValue(extensions);
    }

    /**
     * Copies callback parameters to a unique insertion-ordered map.
     *
     * @param callback raw protocol-neutral callback
     * @return mutable unique parameter map
     * @throws ValidateException if any parameter name occurs more than once
     */
    private static Map<String, String> parameters(final Callback.Inbound callback) {
        final Map<String, String> values = new LinkedHashMap<>(callback.parameters().size());
        for (Callback.Parameter parameter : callback.parameters()) {
            if (values.putIfAbsent(parameter.name(), parameter.value()) != null) {
                throw new ValidateException("OAuth 2.x authorization response parameters must not be repeated");
            }
        }
        return values;
    }

    /**
     * Removes one required non-empty callback parameter.
     *
     * @param parameters mutable unique callback parameter map
     * @param name       registered response parameter name
     * @return required wire value
     * @throws ValidateException if the parameter is absent or empty
     */
    private static String required(final Map<String, String> parameters, final String name) {
        final String value = parameters.remove(name);
        if (value == null || value.isEmpty()) {
            throw new ValidateException("OAuth 2.x authorization response requires non-empty " + name);
        }
        return value;
    }

    /**
     * Decodes one GET callback into a standard authorization success or error response.
     *
     * @param encoded raw protocol-neutral callback captured by the external Web project
     * @return discriminated standard response
     * @throws IllegalArgumentException if the callback is {@code null}
     * @throws ValidateException        if method, multiplicity, exclusivity, or standard parameter syntax is invalid
     */
    @Override
    public Decoded decode(final Callback.Inbound encoded) {
        Assert.notNull(encoded, "OAuth 2.x authorization callback must not be null");
        if (encoded.method() != Http.Method.GET) {
            throw new ValidateException("OAuth 2.x query authorization response callback requires HTTP GET");
        }
        final Map<String, String> parameters = parameters(encoded);
        final boolean success = parameters.containsKey(OAuth2.Parameters.CODE);
        final boolean error = parameters.containsKey(OAuth2.Parameters.ERROR);
        if (success == error) {
            throw new ValidateException("OAuth 2.x authorization callback must contain exactly one of code or error");
        }
        return success ? success(parameters) : error(parameters);
    }

    /**
     * Discriminates the two mutually exclusive standard authorization response models.
     *
     * @author Kimi Liu
     */
    public sealed interface Decoded permits Success, Error {

    }

    /**
     * Carries a decoded standard authorization success response.
     *
     * @param response standard success response
     * @author Kimi Liu
     */
    public record Success(AuthorizationCodeResponse response) implements Decoded {

        /**
         * Creates a successful decoded branch.
         *
         * @throws IllegalArgumentException if response is {@code null}
         */
        public Success {
            Assert.notNull(response, "OAuth 2.x decoded authorization success response must not be null");
        }

    }

    /**
     * Carries a decoded standard OAuth authorization error response.
     *
     * @param response standard error response
     * @author Kimi Liu
     */
    public record Error(AuthorizationErrorResponse response) implements Decoded {

        /**
         * Creates an erroneous decoded branch.
         *
         * @throws IllegalArgumentException if response is {@code null}
         */
        public Error {
            Assert.notNull(response, "OAuth 2.x decoded authorization error response must not be null");
        }

    }

}
