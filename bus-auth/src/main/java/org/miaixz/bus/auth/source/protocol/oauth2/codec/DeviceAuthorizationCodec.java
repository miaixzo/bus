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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.FabricX.Body;
import org.miaixz.bus.auth.FabricX.Headers;
import org.miaixz.bus.auth.FabricX.Request;
import org.miaixz.bus.auth.FabricX.Response;
import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.source.protocol.oauth2.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Encodes and decodes the standard RFC 8628 device authorization endpoint representations.
 * <p>
 * Client authentication remains a transport concern. Successful device credentials and standard OAuth errors remain
 * separate wire branches and are distinguished only by the Java-only {@link Decoded} result.
 * </p>
 *
 * @author Kimi Liu
 */
public class DeviceAuthorizationCodec {

    /**
     * Maximum accepted device authorization form size in bytes.
     */
    private static final long MAXIMUM_FORM_BYTES = 64 * Normal.KIBI;

    /**
     * Shared strict UTF-8 form codec.
     */
    private final FormCodec formCodec;

    /**
     * Creates a strict RFC 8628 codec that processes JSON through {@link JsonKit}.
     */
    public DeviceAuthorizationCodec() {
        this.formCodec = new FormCodec();
    }

    /**
     * Validates the device authorization request transport representation.
     *
     * @param request request to inspect
     * @throws ValidateException if method, URL, media, size, or repeatability is invalid
     */
    private static void validateRequest(final Request request) {
        if (request.method() != Http.Method.POST) {
            throw new ValidateException("OAuth 2.x device authorization endpoint requires HTTP POST");
        }
        if (!request.url().query().isEmpty() || request.url().fragment() != null) {
            throw new ValidateException("OAuth 2.x device authorization parameters must not use URL query or fragment");
        }
        final MediaType media = request.body().media();
        if (!MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(media)) {
            throw new ValidateException("OAuth 2.x device authorization request must be form encoded");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("OAuth 2.x device authorization request charset must be UTF-8");
        }
        if (request.body().length() < 0L || request.body().length() > MAXIMUM_FORM_BYTES) {
            throw new ValidateException("OAuth 2.x device authorization request exceeds the maximum form size");
        }
        if (!request.body().repeatable()) {
            throw new ValidateException(
                    "OAuth 2.x device authorization request must be buffered for decoding and authentication reuse");
        }
    }

    /**
     * Validates response media metadata and declared length.
     *
     * @param response response to inspect
     * @throws ValidateException if media, charset, or size is invalid
     */
    private static void validateResponse(final Response response) {
        if (response.body().length() > Builder.MAXIMUM_DOCUMENT_BYTES) {
            throw new ValidateException("OAuth 2.x device authorization response exceeds the maximum JSON size");
        }
        final MediaType media = response.body().media();
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(media)) {
            throw new ValidateException("OAuth 2.x device authorization response must use application/json");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("OAuth 2.x device authorization response charset must be UTF-8");
        }
    }

    /**
     * Copies decoded form parameters to a unique insertion-ordered map.
     *
     * @param decoded ordered decoded form parameters
     * @return mutable unique parameter map
     * @throws ValidateException if any name occurs more than once
     */
    private static Map<String, String> unique(final List<NameValue> decoded) {
        final Map<String, String> values = new LinkedHashMap<>(decoded.size());
        for (NameValue parameter : decoded) {
            if (values.putIfAbsent(parameter.name(), parameter.value()) != null) {
                throw new ValidateException("OAuth 2.x device authorization parameters must not be repeated");
            }
        }
        return values;
    }

    /**
     * Decodes an HTTP 200 device authorization response.
     *
     * @param object parsed response object
     * @return discriminated successful response
     * @throws ValidateException if required fields, types, or branch exclusivity are invalid
     */
    private static Success success(final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            if (errorMember(name)) {
                throw new ValidateException("OAuth 2.x device authorization success must not contain error members");
            }
            if (!successMember(name)) {
                throw new ValidateException("OAuth 2.x device authorization success contains an unknown member");
            }
        }
        final Map<String, JsonValue> values = object.values();
        return new Success(new DeviceAuthorizationResponse(requiredString(values, OAuth2.Parameters.DEVICE_CODE),
                requiredString(values, OAuth2.Parameters.USER_CODE),
                requiredString(values, OAuth2.Parameters.VERIFICATION_URI),
                Optional.ofNullable(optionalString(values, OAuth2.Parameters.VERIFICATION_URI_COMPLETE)),
                requiredLong(values, OAuth2.Parameters.EXPIRES_IN),
                Optional.ofNullable(optionalLong(values, OAuth2.Parameters.INTERVAL))));
    }

    /**
     * Decodes a standard OAuth error from the device authorization endpoint.
     *
     * @param object parsed error object
     * @param status original HTTP client or server error status
     * @return discriminated standard OAuth error response
     * @throws ValidateException if success fields are mixed into the error or error types are invalid
     */
    private static Error error(final JsonValue.ObjectValue object, final int status) {
        for (String name : object.values().keySet()) {
            if (successMember(name)) {
                throw new ValidateException("OAuth 2.x device authorization error must not contain success members");
            }
        }
        final Map<String, JsonValue> values = object.values();
        return new Error(
                new OAuth2ErrorResponse(new OAuth2ErrorCode(requiredString(values, OAuth2.Parameters.ERROR)),
                        Optional.ofNullable(optionalString(values, OAuth2.Parameters.ERROR_DESCRIPTION)),
                        Optional.ofNullable(optionalString(values, OAuth2.Parameters.ERROR_URI)), Optional.empty()),
                status);
    }

    /**
     * Reads a required non-empty string member.
     *
     * @param values object members
     * @param name   required member name
     * @return decoded string
     * @throws ValidateException if absent, empty, null, or not a string
     */
    private static String requiredString(final Map<String, JsonValue> values, final String name) {
        final String value = optionalString(values, name);
        if (value == null || value.isEmpty()) {
            throw new ValidateException("OAuth 2.x device authorization requires non-empty member: " + name);
        }
        return value;
    }

    /**
     * Reads an optional string member while rejecting explicit null and other types.
     *
     * @param values object members
     * @param name   optional member name
     * @return decoded string, or {@code null} when absent
     * @throws ValidateException if a present value is not a string
     */
    private static String optionalString(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonValue.StringValue string) {
            return string.value();
        }
        throw new ValidateException("OAuth 2.x device authorization member must be a string: " + name);
    }

    /**
     * Reads a required exact integral number member.
     *
     * @param values object members
     * @param name   required numeric member name
     * @return exact decoded long
     * @throws ValidateException if absent, null, nonnumeric, fractional, or outside long range
     */
    private static long requiredLong(final Map<String, JsonValue> values, final String name) {
        final Long value = optionalLong(values, name);
        if (value == null) {
            throw new ValidateException("OAuth 2.x device authorization requires numeric member: " + name);
        }
        return value;
    }

    /**
     * Reads an optional exact integral number member.
     *
     * @param values object members
     * @param name   optional numeric member name
     * @return exact decoded long, or {@code null} when absent
     * @throws ValidateException if a present value is not an exact integral long JSON number
     */
    private static Long optionalLong(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("OAuth 2.x device authorization member must be a JSON number: " + name);
        }
        try {
            return number.value().longValueExact();
        } catch (ArithmeticException exception) {
            throw new ValidateException("OAuth 2.x device authorization number must be an exact integral long",
                    exception);
        }
    }

    /**
     * Creates an exact implementation-neutral JSON number.
     *
     * @param value integral value
     * @return exact JSON number
     */
    private static JsonValue.NumberValue number(final long value) {
        return new JsonValue.NumberValue(BigDecimal.valueOf(value));
    }

    /**
     * Identifies successful device authorization response members.
     *
     * @param name exact JSON member name
     * @return {@code true} for an RFC 8628 success member
     */
    private static boolean successMember(final String name) {
        return switch (name) {
            case OAuth2.Parameters.DEVICE_CODE, OAuth2.Parameters.USER_CODE, OAuth2.Parameters.VERIFICATION_URI, OAuth2.Parameters.VERIFICATION_URI_COMPLETE, OAuth2.Parameters.EXPIRES_IN, OAuth2.Parameters.INTERVAL -> true;
            default -> false;
        };
    }

    /**
     * Identifies standard OAuth error response members.
     *
     * @param name exact JSON member name
     * @return {@code true} for an OAuth error member
     */
    private static boolean errorMember(final String name) {
        return switch (name) {
            case OAuth2.Parameters.ERROR, OAuth2.Parameters.ERROR_DESCRIPTION, OAuth2.Parameters.ERROR_URI -> true;
            default -> false;
        };
    }

    /**
     * Encodes one standard device authorization form request without client credentials.
     *
     * @param request validated RFC 8628 request
     * @return immutable ordered form parameters
     * @throws IllegalArgumentException if request is {@code null}
     */
    public List<NameValue> encode(final DeviceAuthorizationRequest request) {
        Assert.notNull(request, "OAuth 2.x device authorization request must not be null");
        final List<NameValue> parameters = new ArrayList<>(2);
        parameters.add(new NameValue(OAuth2.Parameters.CLIENT_ID, request.clientId()));
        request.scope().ifPresent(value -> parameters.add(new NameValue(OAuth2.Parameters.SCOPE, value.format())));
        return List.copyOf(parameters);
    }

    /**
     * Decodes one buffered standard device authorization request.
     *
     * @param request immutable Fabric HTTP request
     * @return validated RFC 8628 request
     * @throws IllegalArgumentException if request is {@code null}
     * @throws ValidateException        if transport, form, multiplicity, or parameter syntax is invalid
     */
    public DeviceAuthorizationRequest decodeRequest(final Request request) {
        Assert.notNull(request, "OAuth 2.x device authorization HTTP request must not be null");
        validateRequest(request);
        final Map<String, String> values = unique(formCodec.decode(request.body().bytes(MAXIMUM_FORM_BYTES)));
        final String clientId = values.remove(OAuth2.Parameters.CLIENT_ID);
        if (clientId == null || clientId.isEmpty()) {
            throw new ValidateException("OAuth 2.x device authorization request requires non-empty client_id");
        }
        final String scope = values.remove(OAuth2.Parameters.SCOPE);
        values.remove(OAuth2.Parameters.CLIENT_SECRET);
        if (!values.isEmpty()) {
            throw new ValidateException("OAuth 2.x device authorization request contains an unsupported parameter");
        }
        return new DeviceAuthorizationRequest(clientId,
                scope == null ? Optional.empty() : Optional.of(Scope.parse(scope)));
    }

    /**
     * Encodes one successful RFC 8628 response with cache prevention.
     *
     * @param request  originating device authorization request
     * @param response standard successful device authorization response
     * @return complete HTTP 200 JSON response
     * @throws IllegalArgumentException if an argument is {@code null}
     */
    public Response encodeResponse(final Request request, final DeviceAuthorizationResponse response) {
        Assert.notNull(request, "OAuth 2.x device authorization HTTP request must not be null");
        Assert.notNull(response, "OAuth 2.x device authorization response must not be null");
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(OAuth2.Parameters.DEVICE_CODE, new JsonValue.StringValue(response.deviceCode()));
        members.put(OAuth2.Parameters.USER_CODE, new JsonValue.StringValue(response.userCode()));
        members.put(OAuth2.Parameters.VERIFICATION_URI, new JsonValue.StringValue(response.verificationUri()));
        response.verificationUriComplete().ifPresent(
                value -> members.put(OAuth2.Parameters.VERIFICATION_URI_COMPLETE, new JsonValue.StringValue(value)));
        members.put(OAuth2.Parameters.EXPIRES_IN, number(response.expiresIn()));
        response.interval().ifPresent(value -> members.put(OAuth2.Parameters.INTERVAL, number(value)));
        final byte[] body = JsonKit.writeValue(new JsonValue.ObjectValue(members));
        return Response.builder().request(request).code(Http.Status.OK).headers(
                Headers.of(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE, Http.Header.PRAGMA, Http.Cache.NO_CACHE))
                .body(Body.of(body, MediaType.APPLICATION_JSON_TYPE)).build();
    }

    /**
     * Decodes a bounded device authorization endpoint response and closes it on every path.
     *
     * @param response owned Fabric HTTP response
     * @return discriminated standard success or OAuth error response
     * @throws IllegalArgumentException if response is {@code null}
     * @throws ValidateException        if HTTP, media, JSON, or member syntax is invalid
     */
    public Decoded decode(final Response response) {
        final Response encoded = Assert
                .notNull(response, "OAuth 2.x device authorization HTTP response must not be null");
        try (encoded) {
            validateResponse(encoded);
            final JsonValue value = JsonKit.readValue(encoded.bytes(Builder.MAXIMUM_DOCUMENT_BYTES));
            if (!(value instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("OAuth 2.x device authorization response JSON root must be an object");
            }
            if (encoded.code() == Http.Status.OK) {
                return success(object);
            }
            if (encoded.code() >= Http.Status.BAD_REQUEST && encoded.code() < Http.Status.INTERNAL_SERVER_ERROR + 100) {
                return error(object, encoded.code());
            }
            throw new ValidateException("OAuth 2.x device authorization endpoint returned a non-standard status");
        }
    }

    /**
     * Discriminates the standard RFC 8628 success and OAuth error response models.
     *
     * @author Kimi Liu
     */
    public interface Decoded {

    }

    /**
     * Carries a decoded successful device authorization response.
     *
     * @param response standard RFC 8628 success response
     * @author Kimi Liu
     */
    public record Success(DeviceAuthorizationResponse response) implements Decoded {

        /**
         * Creates a successful decoded branch.
         *
         * @throws IllegalArgumentException if response is {@code null}
         */
        public Success {
            Assert.notNull(response, "OAuth 2.x decoded device authorization response must not be null");
        }

    }

    /**
     * Carries a standard OAuth device authorization endpoint error.
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
            Assert.notNull(response, "OAuth 2.x decoded device authorization error must not be null");
            if (status < Http.Status.BAD_REQUEST || status >= Http.Status.INTERNAL_SERVER_ERROR + 100) {
                throw new ValidateException("OAuth 2.x decoded device authorization status must be 4xx or 5xx");
            }
        }

    }

}
