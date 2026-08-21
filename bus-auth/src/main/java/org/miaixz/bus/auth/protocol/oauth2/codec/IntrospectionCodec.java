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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;

/**
 * Encodes and decodes the standard RFC 7662 token introspection request and response representations.
 * <p>
 * Request client credentials remain transport authentication fields and never enter the introspection model. The
 * response discriminator distinguishes a valid RFC 7662 result from a standard OAuth error without changing either wire
 * representation.
 * </p>
 *
 * @author Kimi Liu
 */
public final class IntrospectionCodec {

    /**
     * Maximum accepted form request size in bytes.
     */
    private static final long MAXIMUM_FORM_BYTES = 64 * Normal.KIBI;

    /**
     * Maximum accepted JSON response size in bytes.
     */
    private static final long MAXIMUM_JSON_BYTES = Builder.MAXIMUM_DOCUMENT_BYTES;

    /**
     * Shared strict UTF-8 form codec.
     */
    private final FormCodec formCodec;

    /**
     * Provider-neutral JSON service.
     */
    private final JsonProvider jsonProvider;

    /**
     * Creates a strict RFC 7662 codec.
     *
     * @param jsonProvider provider-neutral JSON service
     * @throws IllegalArgumentException if the provider is {@code null}
     */
    public IntrospectionCodec(final JsonProvider jsonProvider) {
        this.formCodec = new FormCodec();
        this.jsonProvider = Assert.notNull(jsonProvider, "OAuth 2.x introspection JSON provider must not be null");
    }

    /**
     * Validates the introspection request HTTP representation.
     *
     * @param request request to inspect
     * @throws ValidateException if method, URL, media, size, or repeatability is invalid
     */
    private static void validateRequest(final HttpRequest request) {
        if (request.method() != Http.Method.POST) {
            throw new ValidateException("OAuth 2.x introspection endpoint requires HTTP POST");
        }
        if (!request.url().query().isEmpty() || request.url().fragment() != null) {
            throw new ValidateException("OAuth 2.x introspection parameters must not use URL query or fragment");
        }
        final MediaType media = request.body().media();
        if (!MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(media)) {
            throw new ValidateException("OAuth 2.x introspection request must be form encoded");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("OAuth 2.x introspection request charset must be UTF-8");
        }
        if (request.body().length() < 0L || request.body().length() > MAXIMUM_FORM_BYTES) {
            throw new ValidateException("OAuth 2.x introspection request exceeds the maximum form size");
        }
        if (!request.body().repeatable()) {
            throw new ValidateException(
                    "OAuth 2.x introspection request must be buffered for decoding and authentication reuse");
        }
    }

    /**
     * Validates the introspection response media declaration and declared size.
     *
     * @param response response to inspect
     * @throws ValidateException if media, charset, or size is invalid
     */
    private static void validateResponse(final HttpResponse response) {
        if (response.body().length() > MAXIMUM_JSON_BYTES) {
            throw new ValidateException("OAuth 2.x introspection response exceeds the maximum JSON size");
        }
        final MediaType media = response.body().media();
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(media)) {
            throw new ValidateException("OAuth 2.x introspection response must use application/json");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("OAuth 2.x introspection response charset must be UTF-8");
        }
    }

    /**
     * Copies decoded form parameters into a unique-name map.
     *
     * @param decoded ordered decoded form parameters
     * @return mutable insertion-ordered parameter map
     * @throws ValidateException if any name occurs more than once
     */
    private static Map<String, String> unique(final List<NameValue> decoded) {
        final Map<String, String> values = new LinkedHashMap<>(decoded.size());
        for (NameValue parameter : decoded) {
            if (values.putIfAbsent(parameter.name(), parameter.value()) != null) {
                throw new ValidateException("OAuth 2.x introspection parameters must not be repeated");
            }
        }
        return values;
    }

    /**
     * Removes one required non-empty form parameter.
     *
     * @param values mutable form parameters
     * @param name   required parameter name
     * @return required decoded value
     * @throws ValidateException if the parameter is absent or empty
     */
    private static String required(final Map<String, String> values, final String name) {
        final String value = values.remove(name);
        if (value == null || value.isEmpty()) {
            throw new ValidateException("OAuth 2.x introspection request requires non-empty " + name);
        }
        return value;
    }

    /**
     * Decodes a successful RFC 7662 response object.
     *
     * @param object parsed response object
     * @return discriminated introspection response
     * @throws ValidateException if branch exclusivity or a registered member is invalid
     */
    private static Success success(final JsonValue.ObjectValue object) {
        final Map<String, JsonValue> values = object.values();
        for (String name : values.keySet()) {
            if (errorMember(name)) {
                throw new ValidateException("OAuth 2.x introspection success must not contain error members");
            }
        }
        final JsonValue activeValue = values.get(OAuth2.Parameters.ACTIVE);
        if (!(activeValue instanceof JsonValue.BooleanValue active)) {
            throw new ValidateException("OAuth 2.x introspection response requires boolean active");
        }
        if (!active.value()) {
            if (values.size() != 1) {
                throw new ValidateException("Inactive OAuth 2.x introspection response must contain only active=false");
            }
            return new Success(new IntrospectionResponse(false, Optional.empty(), new JsonValue.ObjectValue(Map.of())));
        }
        final Map<String, JsonValue> extensions = new LinkedHashMap<>();
        values.forEach((name, value) -> {
            if (!successMember(name) && !errorMember(name)) {
                extensions.put(name, value);
            }
        });
        final IntrospectionResponse.TokenMetadata metadata = new IntrospectionResponse.TokenMetadata(
                optionalScope(values, OAuth2.Parameters.SCOPE),
                Optional.ofNullable(optionalString(values, OAuth2.Parameters.CLIENT_ID)),
                Optional.ofNullable(optionalString(values, OAuth2.Parameters.USERNAME)),
                optionalTokenType(values, OAuth2.Parameters.TOKEN_TYPE),
                Optional.ofNullable(optionalLong(values, JwtClaims.EXPIRATION)),
                Optional.ofNullable(optionalLong(values, JwtClaims.ISSUED_AT)),
                Optional.ofNullable(optionalLong(values, JwtClaims.NOT_BEFORE)),
                Optional.ofNullable(optionalString(values, JwtClaims.SUBJECT)),
                optionalAudience(values, JwtClaims.AUDIENCE),
                Optional.ofNullable(optionalString(values, JwtClaims.ISSUER)),
                Optional.ofNullable(optionalString(values, JwtClaims.JWT_ID)));
        return new Success(
                new IntrospectionResponse(true, Optional.of(metadata), new JsonValue.ObjectValue(extensions)));
    }

    /**
     * Decodes a standard OAuth error returned by the protected introspection endpoint.
     *
     * @param object parsed error object
     * @param status original HTTP client or server error status
     * @return discriminated standard OAuth error
     * @throws ValidateException if success members are mixed into the error or error fields have invalid types
     */
    private static Error error(final JsonValue.ObjectValue object, final int status) {
        final Map<String, JsonValue> values = object.values();
        for (String name : values.keySet()) {
            if (successMember(name)) {
                throw new ValidateException("OAuth 2.x introspection error must not contain success members");
            }
        }
        return new Error(
                new OAuth2ErrorResponse(new OAuth2ErrorCode(requiredString(values, OAuth2.Parameters.ERROR)),
                        Optional.ofNullable(optionalString(values, OAuth2.Parameters.ERROR_DESCRIPTION)),
                        Optional.ofNullable(optionalString(values, OAuth2.Parameters.ERROR_URI)), Optional.empty()),
                status);
    }

    /**
     * Reads a required non-empty JSON string member.
     *
     * @param values object members
     * @param name   required member name
     * @return decoded string value
     * @throws ValidateException if absent, empty, null, or not a string
     */
    private static String requiredString(final Map<String, JsonValue> values, final String name) {
        final String value = optionalString(values, name);
        if (value == null || value.isEmpty()) {
            throw new ValidateException("OAuth 2.x introspection response requires non-empty string member: " + name);
        }
        return value;
    }

    /**
     * Reads an optional JSON string while rejecting explicit null and other types.
     *
     * @param values object members
     * @param name   optional member name
     * @return decoded string, or {@code null} when absent
     * @throws ValidateException if a present member is not a string
     */
    private static String optionalString(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonValue.StringValue string) {
            return string.value();
        }
        throw new ValidateException("OAuth 2.x introspection response member must be a string: " + name);
    }

    /**
     * Reads an optional exact integral NumericDate.
     *
     * @param values object members
     * @param name   NumericDate member name
     * @return decoded long, or {@code null} when absent
     * @throws ValidateException if a present member is not an exact integral long JSON number
     */
    private static Long optionalLong(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("OAuth 2.x introspection NumericDate must be a JSON number: " + name);
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException exception) {
            throw new ValidateException("OAuth 2.x introspection NumericDate must be an exact integral long",
                    exception);
        }
    }

    /**
     * Reads an optional scope string into the standard scope model.
     *
     * @param values object members
     * @param name   scope member name
     * @return normalized optional scope
     */
    private static Optional<Scope> optionalScope(final Map<String, JsonValue> values, final String name) {
        final String value = optionalString(values, name);
        return value == null ? Optional.empty() : Optional.of(Scope.parse(value));
    }

    /**
     * Reads an optional access-token type string.
     *
     * @param values object members
     * @param name   token-type member name
     * @return normalized optional token type
     */
    private static Optional<TokenType> optionalTokenType(final Map<String, JsonValue> values, final String name) {
        final String value = optionalString(values, name);
        return value == null ? Optional.empty() : Optional.of(new TokenType(value));
    }

    /**
     * Reads audience as either one JSON string or an ordered JSON string array.
     *
     * @param values object members
     * @param name   audience member name
     * @return immutable audience list
     * @throws ValidateException if the member has another JSON shape or a non-string array element
     */
    private static List<String> optionalAudience(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return List.of();
        }
        if (value instanceof JsonValue.StringValue string) {
            return List.of(string.value());
        }
        if (value instanceof JsonValue.ArrayValue array) {
            final List<String> audience = new ArrayList<>(array.values().size());
            for (JsonValue element : array.values()) {
                if (!(element instanceof JsonValue.StringValue string)) {
                    throw new ValidateException("OAuth 2.x introspection audience array must contain only strings");
                }
                audience.add(string.value());
            }
            return List.copyOf(audience);
        }
        throw new ValidateException("OAuth 2.x introspection audience must be a string or string array");
    }

    /**
     * Converts one long value to an exact provider-neutral JSON number.
     *
     * @param value integral value
     * @return exact JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Converts an ordered audience list to a provider-neutral JSON string array.
     *
     * @param values audience values
     * @return immutable JSON array
     */
    private static JsonValue.ArrayValue strings(final List<String> values) {
        return new JsonValue.ArrayValue(
                values.stream().map(JsonValue.StringValue::new).map(JsonValue.class::cast).toList());
    }

    /**
     * Adds one introspection extension after checking its registered name.
     *
     * @param members mutable response members
     * @param name    extension member name
     * @param value   immutable provider-neutral JSON value
     * @throws ValidateException if name is registered
     */
    private static void extension(final Map<String, JsonValue> members, final String name, final JsonValue value) {
        if (successMember(name)) {
            throw new ValidateException("OAuth 2.x introspection extension duplicates a registered member");
        }
        members.put(name, value);
    }

    /**
     * Identifies response members represented by the introspection success model.
     *
     * @param name exact JSON member name
     * @return {@code true} for an RFC 7662 success member
     */
    private static boolean successMember(final String name) {
        return switch (name) {
            case OAuth2.Parameters.ACTIVE, OAuth2.Parameters.SCOPE, OAuth2.Parameters.CLIENT_ID, OAuth2.Parameters.USERNAME, OAuth2.Parameters.TOKEN_TYPE, JwtClaims.EXPIRATION, JwtClaims.ISSUED_AT, JwtClaims.NOT_BEFORE, JwtClaims.SUBJECT, JwtClaims.AUDIENCE, JwtClaims.ISSUER, JwtClaims.JWT_ID -> true;
            default -> false;
        };
    }

    /**
     * Identifies standard OAuth error members.
     *
     * @param name exact JSON member name
     * @return {@code true} for a standard OAuth error member
     */
    private static boolean errorMember(final String name) {
        return switch (name) {
            case OAuth2.Parameters.ERROR, OAuth2.Parameters.ERROR_DESCRIPTION, OAuth2.Parameters.ERROR_URI -> true;
            default -> false;
        };
    }

    /**
     * Encodes the standard introspection form parameters without client authentication fields.
     *
     * @param request standard introspection request
     * @return immutable ordered form parameters
     * @throws IllegalArgumentException if request is {@code null}
     */
    public List<NameValue> encode(final IntrospectionRequest request) {
        Assert.notNull(request, "OAuth 2.x introspection request must not be null");
        final List<NameValue> parameters = new ArrayList<>(2);
        parameters.add(new NameValue(OAuth2.Parameters.TOKEN, request.token()));
        request.tokenTypeHint()
                .ifPresent(value -> parameters.add(new NameValue(OAuth2.Parameters.TOKEN_TYPE_HINT, value)));
        return List.copyOf(parameters);
    }

    /**
     * Decodes a buffered standard introspection form request without retaining transport credentials.
     *
     * @param request immutable Fabric HTTP request
     * @return validated RFC 7662 request
     * @throws IllegalArgumentException if request is {@code null}
     * @throws ValidateException        if transport, form, multiplicity, or parameter syntax is invalid
     */
    public IntrospectionRequest decodeRequest(final HttpRequest request) {
        Assert.notNull(request, "OAuth 2.x introspection HTTP request must not be null");
        validateRequest(request);
        final Map<String, String> parameters = unique(formCodec.decode(request.body().bytes(MAXIMUM_FORM_BYTES)));
        final String token = required(parameters, OAuth2.Parameters.TOKEN);
        final String hint = parameters.remove(OAuth2.Parameters.TOKEN_TYPE_HINT);
        parameters.remove(OAuth2.Parameters.CLIENT_ID);
        parameters.remove(OAuth2.Parameters.CLIENT_SECRET);
        if (!parameters.isEmpty()) {
            throw new ValidateException("OAuth 2.x introspection request contains an unsupported parameter");
        }
        return new IntrospectionRequest(token, Optional.ofNullable(hint));
    }

    /**
     * Encodes one standard RFC 7662 response with mandatory cache prevention.
     *
     * @param request  originating introspection HTTP request
     * @param response standard introspection response
     * @return complete HTTP 200 JSON response
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if an extension duplicates a registered member
     */
    public HttpResponse encodeResponse(final HttpRequest request, final IntrospectionResponse response) {
        Assert.notNull(request, "OAuth 2.x introspection HTTP request must not be null");
        Assert.notNull(response, "OAuth 2.x introspection response must not be null");
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(OAuth2.Parameters.ACTIVE, new JsonValue.BooleanValue(response.active()));
        if (response.active()) {
            final IntrospectionResponse.TokenMetadata metadata = response.metadata().getOrNull();
            if (metadata != null) {
                metadata.scope().ifPresent(
                        value -> members.put(OAuth2.Parameters.SCOPE, new JsonValue.StringValue(value.format())));
                metadata.clientId()
                        .ifPresent(value -> members.put(OAuth2.Parameters.CLIENT_ID, new JsonValue.StringValue(value)));
                metadata.username()
                        .ifPresent(value -> members.put(OAuth2.Parameters.USERNAME, new JsonValue.StringValue(value)));
                metadata.tokenType().ifPresent(
                        value -> members.put(OAuth2.Parameters.TOKEN_TYPE, new JsonValue.StringValue(value.value())));
                metadata.exp().ifPresent(value -> members.put(JwtClaims.EXPIRATION, number(value)));
                metadata.iat().ifPresent(value -> members.put(JwtClaims.ISSUED_AT, number(value)));
                metadata.nbf().ifPresent(value -> members.put(JwtClaims.NOT_BEFORE, number(value)));
                metadata.subject().ifPresent(value -> members.put(JwtClaims.SUBJECT, new JsonValue.StringValue(value)));
                if (metadata.audience().size() == 1) {
                    members.put(JwtClaims.AUDIENCE, new JsonValue.StringValue(metadata.audience().get(0)));
                } else if (!metadata.audience().isEmpty()) {
                    members.put(JwtClaims.AUDIENCE, strings(metadata.audience()));
                }
                metadata.issuer().ifPresent(value -> members.put(JwtClaims.ISSUER, new JsonValue.StringValue(value)));
                metadata.jwtId().ifPresent(value -> members.put(JwtClaims.JWT_ID, new JsonValue.StringValue(value)));
            }
            response.extensions().values().forEach((name, value) -> extension(members, name, value));
        }
        final byte[] body = jsonProvider.writeValue(new JsonValue.ObjectValue(members));
        return HttpResponse.builder().request(request).code(Http.Status.OK).headers(
                Headers.of(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE, Http.Header.PRAGMA, Http.Cache.NO_CACHE))
                .body(PayloadBody.of(Payload.of(body), MediaType.APPLICATION_JSON_TYPE)).build();
    }

    /**
     * Decodes a bounded introspection endpoint response and closes it on every path.
     *
     * @param response owned Fabric HTTP response
     * @return discriminated RFC 7662 success or standard OAuth error
     * @throws IllegalArgumentException if response is {@code null}
     * @throws ValidateException        if HTTP, media, JSON, or registered-member syntax is invalid
     */
    public Decoded decode(final HttpResponse response) {
        final HttpResponse encoded = Assert.notNull(response, "OAuth 2.x introspection HTTP response must not be null");
        try (encoded) {
            validateResponse(encoded);
            final JsonValue value = jsonProvider.readValue(encoded.bytes(MAXIMUM_JSON_BYTES));
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("OAuth 2.x introspection response JSON root must be an object");
            }
            if (encoded.code() == Http.Status.OK) {
                return success(object);
            }
            if (encoded.code() >= Http.Status.BAD_REQUEST && encoded.code() < Http.Status.INTERNAL_SERVER_ERROR + 100) {
                return error(object, encoded.code());
            }
            throw new ValidateException("OAuth 2.x introspection endpoint returned a non-standard HTTP status");
        }
    }

    /**
     * Discriminates an RFC 7662 success from a standard OAuth endpoint error.
     *
     * @author Kimi Liu
     */
    public sealed interface Decoded permits Success, Error {

    }

    /**
     * Carries a decoded standard introspection response.
     *
     * @param response RFC 7662 introspection response
     * @author Kimi Liu
     */
    public record Success(IntrospectionResponse response) implements Decoded {

        /**
         * Creates a successful decoded branch.
         *
         * @throws IllegalArgumentException if response is {@code null}
         */
        public Success {
            Assert.notNull(response, "OAuth 2.x decoded introspection response must not be null");
        }

    }

    /**
     * Carries a standard OAuth error returned by the introspection endpoint.
     *
     * @param response standard OAuth error response
     * @param status   original HTTP 4xx or 5xx status
     * @author Kimi Liu
     */
    public record Error(OAuth2ErrorResponse response, int status) implements Decoded {

        /**
         * Creates an erroneous decoded branch.
         *
         * @throws IllegalArgumentException if response is {@code null}
         * @throws ValidateException        if status is not HTTP 4xx or 5xx
         */
        public Error {
            Assert.notNull(response, "OAuth 2.x decoded introspection error must not be null");
            if (status < Http.Status.BAD_REQUEST || status >= Http.Status.INTERNAL_SERVER_ERROR + 100) {
                throw new ValidateException("OAuth 2.x decoded introspection error status must be 4xx or 5xx");
            }
        }

    }

}
