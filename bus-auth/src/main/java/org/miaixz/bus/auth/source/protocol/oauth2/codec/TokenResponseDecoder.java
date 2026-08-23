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

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.source.protocol.oauth2.*;
import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Decodes a token endpoint HTTP response into exactly one standard OAuth 2.x success or error model.
 * <p>
 * This codec owns the supplied response and closes it on every return and failure path. The nested discriminated result
 * exists only at the Java boundary and is never serialized as an OAuth response envelope.
 * </p>
 *
 * @author Kimi Liu
 */
public class TokenResponseDecoder implements Decoder<Response, TokenResponseDecoder.Decoded> {

    /**
     * Creates a strict token endpoint response decoder that parses JSON through {@link JsonKit}.
     */
    public TokenResponseDecoder() {
    }

    /**
     * Validates the response length and JSON media declaration.
     *
     * @param response response whose body metadata is inspected
     * @throws ValidateException if the body is oversized or is not UTF-8 JSON
     */
    private static void validateMedia(final Response response) {
        if (response.body().length() > Builder.MAXIMUM_DOCUMENT_BYTES) {
            throw new ValidateException("OAuth 2.x token response exceeds the maximum JSON size");
        }
        final MediaType media = response.body().media();
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(media)) {
            throw new ValidateException("OAuth 2.x token response must use application/json");
        }
        final String declaredCharset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (declaredCharset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("OAuth 2.x token response JSON charset must be UTF-8");
        }
    }

    /**
     * Decodes an HTTP 200 token success response.
     *
     * @param object parsed response object
     * @return discriminated successful token response
     * @throws ValidateException if required or optional registered members have invalid types or values
     */
    private static Success success(final JsonValue.ObjectValue object) {
        final Map<String, JsonValue> values = object.values();
        for (String name : values.keySet()) {
            if (errorMember(name)) {
                throw new ValidateException("OAuth 2.x token success response must not contain error members");
            }
        }
        final String accessToken = requiredString(values, OAuth2.Parameters.ACCESS_TOKEN);
        final String tokenType = requiredString(values, OAuth2.Parameters.TOKEN_TYPE);
        final Long expiresIn = optionalLong(values, OAuth2.Parameters.EXPIRES_IN);
        final String refreshToken = optionalString(values, OAuth2.Parameters.REFRESH_TOKEN);
        final String scope = optionalString(values, OAuth2.Parameters.SCOPE);
        final String issuedTokenType = optionalString(values, OAuth2.Parameters.ISSUED_TOKEN_TYPE);
        final Map<String, JsonValue> extensions = new LinkedHashMap<>();
        values.forEach((name, value) -> {
            if (!successMember(name) && !errorMember(name)) {
                extensions.put(name, value);
            }
        });
        final JsonValue.ObjectValue extensionValues = new JsonValue.ObjectValue(extensions);
        if (issuedTokenType != null) {
            return new Success(new TokenExchangeResponse(accessToken, issuedTokenType, new TokenType(tokenType),
                    Optional.ofNullable(expiresIn), scope == null ? Optional.empty() : Optional.of(Scope.parse(scope)),
                    Optional.ofNullable(refreshToken), extensionValues));
        }
        return new Success(new TokenResponse(accessToken, new TokenType(tokenType), Optional.ofNullable(expiresIn),
                Optional.ofNullable(refreshToken), scope == null ? Optional.empty() : Optional.of(Scope.parse(scope)),
                extensionValues));
    }

    /**
     * Decodes a token endpoint HTTP error response.
     *
     * @param object parsed response object
     * @param status HTTP client or server error status
     * @return discriminated standard OAuth error response
     * @throws ValidateException if the response is missing error or mixes success and error members
     */
    private static Error error(final JsonValue.ObjectValue object, final int status) {
        final Map<String, JsonValue> values = object.values();
        for (String name : values.keySet()) {
            if (successMember(name)) {
                throw new ValidateException("OAuth 2.x token error response must not contain success members");
            }
        }
        final Map<String, JsonValue> extensions = new LinkedHashMap<>();
        values.forEach((name, value) -> {
            if (!errorMember(name)) {
                extensions.put(name, value);
            }
        });
        return new Error(new TokenErrorResponse(new OAuth2ErrorCode(requiredString(values, OAuth2.Parameters.ERROR)),
                Optional.ofNullable(optionalString(values, OAuth2.Parameters.ERROR_DESCRIPTION)),
                Optional.ofNullable(optionalString(values, OAuth2.Parameters.ERROR_URI)),
                new JsonValue.ObjectValue(extensions)), status);
    }

    /**
     * Reads a required non-empty JSON string member.
     *
     * @param values response object members
     * @param name   registered member name
     * @return decoded string value
     * @throws ValidateException if the member is absent, empty, null, or not a string
     */
    private static String requiredString(final Map<String, JsonValue> values, final String name) {
        final String value = optionalString(values, name);
        if (value == null || value.isEmpty()) {
            throw new ValidateException("OAuth 2.x token response requires non-empty string member: " + name);
        }
        return value;
    }

    /**
     * Reads an optional JSON string member while rejecting explicit null and other JSON types.
     *
     * @param values response object members
     * @param name   registered member name
     * @return decoded string, or {@code null} when the member is absent
     * @throws ValidateException if a present member is not a JSON string
     */
    private static String optionalString(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonValue.StringValue string) {
            return string.value();
        }
        throw new ValidateException("OAuth 2.x token response member must be a string: " + name);
    }

    /**
     * Reads the optional exact non-negative integral token lifetime.
     *
     * @param values response object members
     * @param name   registered lifetime member name
     * @return decoded lifetime, or {@code null} when absent
     * @throws ValidateException if the member is not an exact non-negative long JSON number
     */
    private static Long optionalLong(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("OAuth 2.x token response member must be a JSON number: " + name);
        }
        try {
            final long decoded = number.value().longValueExact();
            if (decoded < 0L) {
                throw new ValidateException("OAuth 2.x token response lifetime must not be negative");
            }
            return decoded;
        } catch (ArithmeticException exception) {
            throw new ValidateException("OAuth 2.x token response lifetime must be an exact integral long", exception);
        }
    }

    /**
     * Identifies successful OAuth token response members.
     *
     * @param name exact JSON member name
     * @return {@code true} for an OAuth token success member
     */
    private static boolean successMember(final String name) {
        return switch (name) {
            case OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.TOKEN_TYPE, OAuth2.Parameters.EXPIRES_IN, OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.SCOPE, OAuth2.Parameters.ISSUED_TOKEN_TYPE -> true;
            default -> false;
        };
    }

    /**
     * Identifies standard OAuth token error members.
     *
     * @param name exact JSON member name
     * @return {@code true} for an OAuth token error member
     */
    private static boolean errorMember(final String name) {
        return switch (name) {
            case OAuth2.Parameters.ERROR, OAuth2.Parameters.ERROR_DESCRIPTION, OAuth2.Parameters.ERROR_URI -> true;
            default -> false;
        };
    }

    /**
     * Decodes one bounded JSON token endpoint response and closes the response before returning.
     *
     * @param encoded owned Fabric HTTP response
     * @return discriminated standard token success or OAuth error response
     * @throws IllegalArgumentException if the response is {@code null}
     * @throws ValidateException        if status, media metadata, JSON shape, or registered members are invalid
     */
    @Override
    public Decoded decode(final Response encoded) {
        final Response response = Assert.notNull(encoded, "OAuth 2.x token HTTP response must not be null");
        try (response) {
            validateMedia(response);
            final JsonValue value = JsonKit.readValue(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES));
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("OAuth 2.x token response JSON root must be an object");
            }
            return decode(response.code(), object);
        }
    }

    /**
     * Decodes a parsed token endpoint object while preserving its HTTP status branch.
     *
     * @param status original token endpoint HTTP status
     * @param body   parsed token endpoint JSON object
     * @return discriminated standard OAuth token response
     * @throws IllegalArgumentException if body is {@code null}
     * @throws ValidateException        if status or response branch members are invalid
     */
    public Decoded decode(final int status, final JsonValue.ObjectValue body) {
        Assert.notNull(body, "OAuth 2.x token response object must not be null");
        if (status == Http.Status.OK) {
            return success(body);
        }
        if (status >= Http.Status.BAD_REQUEST && status < Http.Status.INTERNAL_SERVER_ERROR + 100) {
            return error(body, status);
        }
        throw new ValidateException("OAuth 2.x token endpoint returned a non-standard HTTP status");
    }

    /**
     * Discriminates the mutually exclusive standard token success and OAuth error response models.
     *
     * @author Kimi Liu
     */
    public interface Decoded {

    }

    /**
     * Carries a decoded standard token endpoint success response.
     *
     * @param response standard successful token response
     * @author Kimi Liu
     */
    public record Success(TokenEndpointResponse response) implements Decoded {

        /**
         * Creates a successful decoded branch.
         *
         * @throws IllegalArgumentException if response is {@code null}
         */
        public Success {
            Assert.notNull(response, "OAuth 2.x decoded token success response must not be null");
        }

    }

    /**
     * Carries a decoded standard OAuth token endpoint error and its transport status.
     *
     * @param response standard OAuth error response
     * @param status   original HTTP 4xx or 5xx status
     * @author Kimi Liu
     */
    public record Error(TokenErrorResponse response, int status) implements Decoded {

        /**
         * Creates an erroneous decoded branch.
         *
         * @throws IllegalArgumentException if response is {@code null}
         * @throws ValidateException        if status is not an HTTP 4xx or 5xx value
         */
        public Error {
            Assert.notNull(response, "OAuth 2.x decoded token error response must not be null");
            if (status < Http.Status.BAD_REQUEST || status >= Http.Status.INTERNAL_SERVER_ERROR + 100) {
                throw new ValidateException("OAuth 2.x decoded token error status must be 4xx or 5xx");
            }
        }

    }

}
