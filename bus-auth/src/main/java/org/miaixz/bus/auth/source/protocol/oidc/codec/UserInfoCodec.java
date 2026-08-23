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
package org.miaixz.bus.auth.source.protocol.oidc.codec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.FabricX.*;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2ErrorCode;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2ErrorResponse;
import org.miaixz.bus.auth.source.protocol.oauth2.TokenType;
import org.miaixz.bus.auth.source.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.source.protocol.oidc.UserInfoRequest;
import org.miaixz.bus.auth.source.protocol.oidc.UserInfoResponse;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Encodes and decodes the OpenID Connect UserInfo protected-resource representations.
 * <p>
 * Server request decoding accepts exactly one Authorization-header Bearer credential over HTTPS GET. Successful
 * responses use the standard JSON Claims object, while errors decode only a standard OAuth JSON error or RFC 6750
 * Bearer challenge. Every owned HTTP response is closed after bounded decoding.
 * </p>
 *
 * @author Kimi Liu
 */
public class UserInfoCodec {

    /**
     * Creates a strict UserInfo codec that processes JSON through {@link JsonKit}.
     */
    public UserInfoCodec() {
    }

    /**
     * Parses and validates the RFC 6750 Authorization credential syntax.
     *
     * @param value complete Authorization field value
     * @return sensitive bearer token
     * @throws ValidateException if scheme, whitespace, or b64token grammar is invalid
     */
    private static String bearerToken(final String value) {
        final String scheme = TokenType.BEARER.value();
        if (value == null || value.length() <= scheme.length()
                || !value.regionMatches(true, 0, scheme, 0, scheme.length())
                || value.charAt(scheme.length()) != Symbol.C_SPACE) {
            throw new ValidateException("OpenID Connect UserInfo Authorization scheme must be Bearer");
        }
        int index = scheme.length();
        while (index < value.length() && value.charAt(index) == Symbol.C_SPACE) {
            index++;
        }
        final String token = value.substring(index);
        if (token.isEmpty()) {
            throw new ValidateException("OpenID Connect UserInfo Bearer credential must not be empty");
        }
        boolean padding = false;
        for (int position = 0; position < token.length(); position++) {
            final char character = token.charAt(position);
            if (character == Symbol.C_EQUAL) {
                padding = true;
                continue;
            }
            final boolean valid = character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z
                    || character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z
                    || character >= Symbol.C_ZERO && character <= Symbol.C_NINE || character == Symbol.C_MINUS
                    || character == Symbol.C_DOT || character == Symbol.C_UNDERLINE || character == Symbol.C_TILDE
                    || character == Symbol.C_PLUS || character == Symbol.C_SLASH;
            if (!valid || padding) {
                throw new ValidateException("OpenID Connect UserInfo Bearer credential violates b64token syntax");
            }
        }
        return token;
    }

    /**
     * Identifies standard OAuth error response members.
     *
     * @param name exact member name
     * @return {@code true} for an OAuth error member
     */
    private static boolean errorMember(final String name) {
        return switch (name) {
            case OAuth2.Parameters.ERROR, OAuth2.Parameters.ERROR_DESCRIPTION, OAuth2.Parameters.ERROR_URI -> true;
            default -> false;
        };
    }

    /**
     * Identifies standard OpenID Connect UserInfo claims represented by typed components.
     *
     * @param name exact claim name
     * @return {@code true} for a typed standard claim
     */
    private static boolean standardClaim(final String name) {
        return switch (name) {
            case JwtClaims.SUBJECT, OpenIdConnect.Claims.NAME, OpenIdConnect.Claims.GIVEN_NAME, OpenIdConnect.Claims.FAMILY_NAME, OpenIdConnect.Claims.MIDDLE_NAME, OpenIdConnect.Claims.NICKNAME, OpenIdConnect.Claims.PREFERRED_USERNAME, OpenIdConnect.Claims.PROFILE, OpenIdConnect.Claims.PICTURE, OpenIdConnect.Claims.WEBSITE, OpenIdConnect.Claims.EMAIL, OpenIdConnect.Claims.EMAIL_VERIFIED, OpenIdConnect.Claims.GENDER, OpenIdConnect.Claims.BIRTHDATE, OpenIdConnect.Claims.ZONE_INFO, OpenIdConnect.Claims.LOCALE, OpenIdConnect.Claims.PHONE_NUMBER, OpenIdConnect.Claims.PHONE_NUMBER_VERIFIED, OpenIdConnect.Claims.ADDRESS, OpenIdConnect.Claims.UPDATED_AT -> true;
            default -> false;
        };
    }

    /**
     * Converts a standard UserInfo Claims object to the typed success branch.
     *
     * @param object parsed response object
     * @return decoded successful branch
     * @throws ValidateException if subject or branch exclusivity is invalid
     */
    private static Success success(final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            if (errorMember(name)) {
                throw new ValidateException("OpenID Connect UserInfo success must not contain OAuth error members");
            }
        }
        final JsonValue subjectValue = object.values().get(JwtClaims.SUBJECT);
        if (!(subjectValue instanceof JsonValue.StringValue subject) || subject.value().isBlank()) {
            throw new ValidateException("OpenID Connect UserInfo response requires non-blank string sub");
        }
        final Map<String, JsonValue> values = object.values();
        final Map<String, JsonValue> extensions = new LinkedHashMap<>();
        values.forEach((name, value) -> {
            if (!standardClaim(name)) {
                extensions.put(name, value);
            }
        });
        return new Success(new UserInfoResponse(new UserInfoResponse.Subject(subject.value()), name(values),
                address(values), phone(values), email(values), text(values, OpenIdConnect.Claims.PROFILE),
                text(values, OpenIdConnect.Claims.PICTURE), text(values, OpenIdConnect.Claims.WEBSITE),
                text(values, OpenIdConnect.Claims.GENDER), text(values, OpenIdConnect.Claims.BIRTHDATE),
                text(values, OpenIdConnect.Claims.ZONE_INFO), text(values, OpenIdConnect.Claims.LOCALE),
                integer(values, OpenIdConnect.Claims.UPDATED_AT), new JsonValue.ObjectValue(extensions)));
    }

    /**
     * Reads one optional string claim.
     *
     * @param values claim object members
     * @param name   exact claim name
     * @return normalized optional string
     */
    private static Optional<String> text(final Map<String, JsonValue> values, final String name) {
        return Optional.ofNullable(optionalString(values, name));
    }

    /**
     * Reads one optional Boolean claim.
     *
     * @param values claim object members
     * @param name   exact claim name
     * @return normalized optional Boolean
     */
    private static Optional<Boolean> flag(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof JsonValue.BooleanValue flag) {
            return Optional.of(flag.value());
        }
        throw new ValidateException("OpenID Connect UserInfo claim must be a Boolean: " + name);
    }

    /**
     * Reads one optional non-negative integral NumericDate claim.
     *
     * @param values claim object members
     * @param name   exact claim name
     * @return normalized optional long
     */
    private static Optional<Long> integer(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.NumberValue number)) {
            throw new ValidateException("OpenID Connect UserInfo NumericDate must be a JSON number");
        }
        try {
            return Optional.of(number.value().longValueExact());
        } catch (ArithmeticException cause) {
            throw new ValidateException("OpenID Connect UserInfo NumericDate must be an integral long", cause);
        }
    }

    /**
     * Tests whether any named claim is present.
     *
     * @param values claim object members
     * @param names  candidate claim names
     * @return {@code true} when at least one claim exists
     */
    private static boolean present(final Map<String, JsonValue> values, final String... names) {
        for (String name : names) {
            if (values.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Decodes the standard person-name claim group.
     *
     * @param values claim object members
     * @return optional typed name group
     */
    private static Optional<UserInfoResponse.Name> name(final Map<String, JsonValue> values) {
        if (!present(
                values,
                OpenIdConnect.Claims.NAME,
                OpenIdConnect.Claims.GIVEN_NAME,
                OpenIdConnect.Claims.FAMILY_NAME,
                OpenIdConnect.Claims.MIDDLE_NAME,
                OpenIdConnect.Claims.NICKNAME,
                OpenIdConnect.Claims.PREFERRED_USERNAME)) {
            return Optional.empty();
        }
        return Optional.of(
                new UserInfoResponse.Name(text(values, OpenIdConnect.Claims.NAME),
                        text(values, OpenIdConnect.Claims.GIVEN_NAME), text(values, OpenIdConnect.Claims.FAMILY_NAME),
                        text(values, OpenIdConnect.Claims.MIDDLE_NAME), text(values, OpenIdConnect.Claims.NICKNAME),
                        text(values, OpenIdConnect.Claims.PREFERRED_USERNAME)));
    }

    /**
     * Decodes the structured address claim.
     *
     * @param values claim object members
     * @return optional typed address
     */
    private static Optional<UserInfoResponse.Address> address(final Map<String, JsonValue> values) {
        final JsonValue value = values.get(OpenIdConnect.Claims.ADDRESS);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("OpenID Connect UserInfo address must be a JSON object");
        }
        for (String name : object.values().keySet()) {
            if (!addressMember(name)) {
                throw new ValidateException("OpenID Connect UserInfo address contains an unknown member");
            }
        }
        return Optional.of(
                new UserInfoResponse.Address(text(object.values(), OpenIdConnect.Claims.FORMATTED),
                        text(object.values(), OpenIdConnect.Claims.STREET_ADDRESS),
                        text(object.values(), OpenIdConnect.Claims.LOCALITY),
                        text(object.values(), OpenIdConnect.Claims.REGION),
                        text(object.values(), OpenIdConnect.Claims.POSTAL_CODE),
                        text(object.values(), OpenIdConnect.Claims.COUNTRY)));
    }

    /**
     * Identifies structured address members.
     *
     * @param name exact address member name
     * @return {@code true} for a standard address member
     */
    private static boolean addressMember(final String name) {
        return switch (name) {
            case OpenIdConnect.Claims.FORMATTED, OpenIdConnect.Claims.STREET_ADDRESS, OpenIdConnect.Claims.LOCALITY, OpenIdConnect.Claims.REGION, OpenIdConnect.Claims.POSTAL_CODE, OpenIdConnect.Claims.COUNTRY -> true;
            default -> false;
        };
    }

    /**
     * Decodes telephone claims.
     *
     * @param values claim object members
     * @return optional typed telephone group
     */
    private static Optional<UserInfoResponse.Phone> phone(final Map<String, JsonValue> values) {
        if (!present(values, OpenIdConnect.Claims.PHONE_NUMBER, OpenIdConnect.Claims.PHONE_NUMBER_VERIFIED)) {
            return Optional.empty();
        }
        return Optional.of(
                new UserInfoResponse.Phone(text(values, OpenIdConnect.Claims.PHONE_NUMBER),
                        flag(values, OpenIdConnect.Claims.PHONE_NUMBER_VERIFIED)));
    }

    /**
     * Decodes email claims.
     *
     * @param values claim object members
     * @return optional typed email group
     */
    private static Optional<UserInfoResponse.Email> email(final Map<String, JsonValue> values) {
        if (!present(values, OpenIdConnect.Claims.EMAIL, OpenIdConnect.Claims.EMAIL_VERIFIED)) {
            return Optional.empty();
        }
        return Optional.of(
                new UserInfoResponse.Email(text(values, OpenIdConnect.Claims.EMAIL),
                        flag(values, OpenIdConnect.Claims.EMAIL_VERIFIED)));
    }

    /**
     * Converts a strict standard OAuth JSON error object.
     *
     * @param object parsed error response object
     * @return standard OAuth error model
     * @throws ValidateException if members are missing, unknown, or have invalid types
     */
    private static OAuth2ErrorResponse error(final JsonValue.ObjectValue object) {
        for (String name : object.values().keySet()) {
            if (!errorMember(name)) {
                throw new ValidateException("OpenID Connect UserInfo OAuth error contains non-error members");
            }
        }
        return new OAuth2ErrorResponse(new OAuth2ErrorCode(requiredString(object.values(), OAuth2.Parameters.ERROR)),
                Optional.ofNullable(optionalString(object.values(), OAuth2.Parameters.ERROR_DESCRIPTION)),
                Optional.ofNullable(optionalString(object.values(), OAuth2.Parameters.ERROR_URI)), Optional.empty());
    }

    /**
     * Parses an optional unique RFC 6750 Bearer challenge.
     *
     * @param response error HTTP response
     * @return standard OAuth error response or {@code null} when no challenge exists
     * @throws ValidateException if multiplicity, scheme, or registered parameters are invalid
     */
    private static OAuth2ErrorResponse bearerChallenge(final Response response) {
        final List<String> values = response.headers().values(Http.Header.WWW_AUTHENTICATE);
        if (values.isEmpty()) {
            return null;
        }
        if (values.size() != 1) {
            throw new ValidateException("OpenID Connect UserInfo requires one WWW-Authenticate challenge");
        }
        final Challenge challenge = Challenge.parse(values.get(0));
        if (!TokenType.BEARER.value().equalsIgnoreCase(challenge.scheme())) {
            throw new ValidateException("OpenID Connect UserInfo challenge must use the Bearer scheme");
        }
        for (String name : challenge.parameters().keySet()) {
            if (!challengeParameter(name)) {
                throw new ValidateException("OpenID Connect UserInfo Bearer challenge contains an unknown parameter");
            }
        }
        final String error = challenge.parameters().get(OAuth2.Parameters.ERROR);
        if (error == null || error.isEmpty()) {
            throw new ValidateException("OpenID Connect UserInfo Bearer challenge requires error");
        }
        return new OAuth2ErrorResponse(new OAuth2ErrorCode(error),
                Optional.ofNullable(challenge.parameters().get(OAuth2.Parameters.ERROR_DESCRIPTION)),
                Optional.ofNullable(challenge.parameters().get(OAuth2.Parameters.ERROR_URI)), Optional.empty());
    }

    /**
     * Identifies parameters permitted in an RFC 6750 Bearer challenge.
     *
     * @param name exact challenge parameter name
     * @return {@code true} for a registered Bearer challenge parameter
     */
    private static boolean challengeParameter(final String name) {
        return "realm".equals(name) || errorMember(name) || OAuth2.Parameters.SCOPE.equals(name);
    }

    /**
     * Reads a required non-empty JSON string member.
     *
     * @param values parsed object members
     * @param name   exact member name
     * @return non-empty string value
     */
    private static String requiredString(final Map<String, JsonValue> values, final String name) {
        final String value = optionalString(values, name);
        if (value == null || value.isEmpty()) {
            throw new ValidateException("OpenID Connect UserInfo error requires non-empty " + name);
        }
        return value;
    }

    /**
     * Reads an optional JSON string member without coercion.
     *
     * @param values parsed object members
     * @param name   exact member name
     * @return string value or {@code null}
     */
    private static String optionalString(final Map<String, JsonValue> values, final String name) {
        final JsonValue value = values.get(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("OpenID Connect UserInfo error member must be a string: " + name);
        }
        return string.value();
    }

    /**
     * Adds an optional string claim.
     *
     * @param members mutable claim object
     * @param name    exact claim name
     * @param value   optional string value
     */
    private static void put(final Map<String, JsonValue> members, final String name, final Optional<String> value) {
        value.ifPresent(item -> members.put(name, new JsonValue.StringValue(item)));
    }

    /**
     * Adds an optional Boolean claim.
     *
     * @param members mutable claim object
     * @param name    exact claim name
     * @param value   optional Boolean value
     */
    private static void putFlag(
            final Map<String, JsonValue> members,
            final String name,
            final Optional<Boolean> value) {
        value.ifPresent(item -> members.put(name, new JsonValue.BooleanValue(item)));
    }

    /**
     * Decodes one implementation-neutral UserInfo Claims object into the typed standard response model.
     *
     * @param claims complete UserInfo Claims object
     * @return typed OpenID Connect UserInfo response
     * @throws IllegalArgumentException if {@code claims} is {@code null}
     * @throws ValidateException        if a standard claim is missing or has an invalid JSON type
     */
    public UserInfoResponse decodeClaims(final JsonValue.ObjectValue claims) {
        return success(Assert.notNull(claims, "OpenID Connect UserInfo claims must not be null")).response();
    }

    /**
     * Decodes one HTTPS GET request carrying exactly one Authorization-header Bearer token.
     *
     * @param request immutable buffered Fabric HTTP request
     * @return standard UserInfo request model
     * @throws IllegalArgumentException if {@code request} is {@code null}
     * @throws ValidateException        if transport, target, body, header multiplicity, or token syntax is invalid
     */
    public UserInfoRequest decodeRequest(final Request request) {
        Assert.notNull(request, "OpenID Connect UserInfo HTTP request must not be null");
        if (request.method() != Http.Method.GET || !Protocol.HTTPS.name.equalsIgnoreCase(request.url().scheme())) {
            throw new ValidateException("OpenID Connect UserInfo endpoint requires HTTPS GET");
        }
        if (!request.url().query().isEmpty() || request.url().fragment() != null || request.body().length() != 0L) {
            throw new ValidateException(
                    "OpenID Connect UserInfo bearer token must not use query, fragment, or request body");
        }
        final List<String> authorization = request.headers().values(Http.Header.AUTHORIZATION);
        if (authorization.size() != 1) {
            throw new ValidateException("OpenID Connect UserInfo requires exactly one Authorization field");
        }
        return new UserInfoRequest(bearerToken(authorization.get(0)));
    }

    /**
     * Encodes one successful standard UserInfo Claims object with mandatory cache prevention.
     *
     * @param request  originating Fabric HTTP request
     * @param response standard UserInfo response
     * @return complete HTTP 200 JSON response
     */
    public Response encodeResponse(final Request request, final UserInfoResponse response) {
        Assert.notNull(request, "OpenID Connect UserInfo HTTP request must not be null");
        Assert.notNull(response, "OpenID Connect UserInfo response must not be null");
        final Map<String, JsonValue> members = new LinkedHashMap<>();
        members.put(JwtClaims.SUBJECT, new JsonValue.StringValue(response.subject().value()));
        response.name().ifPresent(value -> {
            put(members, OpenIdConnect.Claims.NAME, value.formatted());
            put(members, OpenIdConnect.Claims.GIVEN_NAME, value.givenName());
            put(members, OpenIdConnect.Claims.FAMILY_NAME, value.familyName());
            put(members, OpenIdConnect.Claims.MIDDLE_NAME, value.middleName());
            put(members, OpenIdConnect.Claims.NICKNAME, value.nickname());
            put(members, OpenIdConnect.Claims.PREFERRED_USERNAME, value.preferredUsername());
        });
        response.address().ifPresent(value -> {
            final Map<String, JsonValue> address = new LinkedHashMap<>();
            put(address, OpenIdConnect.Claims.FORMATTED, value.formatted());
            put(address, OpenIdConnect.Claims.STREET_ADDRESS, value.streetAddress());
            put(address, OpenIdConnect.Claims.LOCALITY, value.locality());
            put(address, OpenIdConnect.Claims.REGION, value.region());
            put(address, OpenIdConnect.Claims.POSTAL_CODE, value.postalCode());
            put(address, OpenIdConnect.Claims.COUNTRY, value.country());
            members.put(OpenIdConnect.Claims.ADDRESS, new JsonValue.ObjectValue(address));
        });
        response.phone().ifPresent(value -> {
            put(members, OpenIdConnect.Claims.PHONE_NUMBER, value.number());
            putFlag(members, OpenIdConnect.Claims.PHONE_NUMBER_VERIFIED, value.verified());
        });
        response.email().ifPresent(value -> {
            put(members, OpenIdConnect.Claims.EMAIL, value.address());
            putFlag(members, OpenIdConnect.Claims.EMAIL_VERIFIED, value.verified());
        });
        put(members, OpenIdConnect.Claims.PROFILE, response.profile());
        put(members, OpenIdConnect.Claims.PICTURE, response.picture());
        put(members, OpenIdConnect.Claims.WEBSITE, response.website());
        put(members, OpenIdConnect.Claims.GENDER, response.gender());
        put(members, OpenIdConnect.Claims.BIRTHDATE, response.birthdate());
        put(members, OpenIdConnect.Claims.ZONE_INFO, response.zoneInfo());
        put(members, OpenIdConnect.Claims.LOCALE, response.locale());
        response.updatedAt().ifPresent(
                value -> members.put(
                        OpenIdConnect.Claims.UPDATED_AT,
                        new JsonValue.NumberValue(java.math.BigDecimal.valueOf(value))));
        response.extensions().values().forEach((name, value) -> {
            if (standardClaim(name)) {
                throw new ValidateException("OpenID Connect UserInfo extension duplicates a standard claim");
            }
            members.put(name, value);
        });
        final byte[] body = JsonKit.writeValue(new JsonValue.ObjectValue(members));
        return Response.builder().request(request).code(Http.Status.OK).headers(
                Headers.of(Http.Header.CACHE_CONTROL, Http.Cache.NO_STORE, Http.Header.PRAGMA, Http.Cache.NO_CACHE))
                .body(Body.of(body, MediaType.APPLICATION_JSON_TYPE)).build();
    }

    /**
     * Decodes and closes one bounded UserInfo success or standard OAuth Bearer error response.
     *
     * @param response owned Fabric HTTP response
     * @return discriminated standard success or error branch
     * @throws IllegalArgumentException if {@code response} is {@code null}
     * @throws ValidateException        if status, media, JSON, challenge, or branch shape is invalid
     */
    public Decoded decode(final Response response) {
        final Response encoded = Assert.notNull(response, "OpenID Connect UserInfo HTTP response must not be null");
        try (encoded) {
            if (encoded.body().length() > Builder.MAXIMUM_DOCUMENT_BYTES) {
                throw new ValidateException("OpenID Connect UserInfo response exceeds one MiB");
            }
            if (encoded.code() == Http.Status.OK) {
                return success(jsonObject(encoded));
            }
            if (encoded.code() < Http.Status.BAD_REQUEST || encoded.code() >= Http.Status.INTERNAL_SERVER_ERROR + 100) {
                throw new ValidateException("OpenID Connect UserInfo endpoint returned a non-standard HTTP status");
            }
            final OAuth2ErrorResponse challenge = bearerChallenge(encoded);
            final OAuth2ErrorResponse json = encoded.body().length() == 0L ? null : error(jsonObject(encoded));
            if (challenge == null && json == null) {
                throw new ValidateException("OpenID Connect UserInfo error requires OAuth JSON or a Bearer challenge");
            }
            if (challenge != null && json != null && !challenge.error().equals(json.error())) {
                throw new ValidateException("OpenID Connect UserInfo error representations disagree");
            }
            return new Error(json == null ? challenge : json, encoded.code());
        }
    }

    /**
     * Parses a bounded UTF-8 application/json response body as one JSON object.
     *
     * @param response owned HTTP response
     * @return parsed implementation-neutral object
     * @throws ValidateException if media, charset, size, JSON, or root type is invalid
     */
    private JsonValue.ObjectValue jsonObject(final Response response) {
        final MediaType media = response.body().media();
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(media)) {
            throw new ValidateException("OpenID Connect UserInfo JSON response must use application/json");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("OpenID Connect UserInfo JSON response charset must be UTF-8");
        }
        final JsonValue value = JsonKit.readValue(response.bytes(Builder.MAXIMUM_DOCUMENT_BYTES));
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("OpenID Connect UserInfo response JSON root must be an object");
        }
        return object;
    }

    /**
     * Discriminates standard UserInfo success from OAuth Bearer error responses.
     *
     * @author Kimi Liu
     */
    public interface Decoded {

    }

    /**
     * Carries one decoded successful UserInfo response.
     *
     * @param response standard UserInfo response
     * @author Kimi Liu
     */
    public record Success(UserInfoResponse response) implements Decoded {

        /**
         * Validates the successful decoded branch.
         */
        public Success {
            Assert.notNull(response, "Decoded OpenID Connect UserInfo response must not be null");
        }

    }

    /**
     * Carries one decoded standard OAuth Bearer error and its HTTP status.
     *
     * @param response standard OAuth error response
     * @param status   original HTTP 4xx or 5xx status
     * @author Kimi Liu
     */
    public record Error(OAuth2ErrorResponse response, int status) implements Decoded {

        /**
         * Validates the erroneous decoded branch.
         *
         * @throws ValidateException if the status is outside HTTP 4xx and 5xx
         */
        public Error {
            Assert.notNull(response, "Decoded OpenID Connect UserInfo error response must not be null");
            if (status < Http.Status.BAD_REQUEST || status >= Http.Status.INTERNAL_SERVER_ERROR + 100) {
                throw new ValidateException("OpenID Connect UserInfo error status must be 4xx or 5xx");
            }
        }

    }

}
