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

import org.miaixz.bus.auth.codec.FormCodec;
import org.miaixz.bus.auth.codec.NameValue;
import org.miaixz.bus.auth.protocol.oauth2.*;
import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;

/**
 * Decodes every enabled OAuth token grant from the single standard form-encoded token endpoint.
 * <p>
 * Client credentials remain in the original HTTP request for {@code ClientAuthenticator}; they are never retained in
 * the protocol request model or its extensions.
 * </p>
 *
 * @author Kimi Liu
 */
public final class TokenRequestDecoder implements Decoder<HttpRequest, TokenRequest> {

    /**
     * Maximum form body materialized by the token endpoint decoder.
     */
    private static final long MAXIMUM_FORM_BYTES = 64 * Normal.KIBI;

    /**
     * Shared stateless UTF-8 form codec.
     */
    private final FormCodec formCodec;

    /**
     * Creates a strict token request decoder using the shared form codec.
     */
    public TokenRequestDecoder() {
        this.formCodec = new FormCodec();
    }

    /**
     * Validates the HTTP token endpoint transport and media contract.
     *
     * @param request immutable Fabric HTTP request
     * @throws ValidateException if method, URL, media type, charset, or body size is invalid
     */
    private static void validateTransport(final HttpRequest request) {
        if (request.method() != Http.Method.POST) {
            throw new ValidateException("OAuth 2.x token endpoint requires HTTP POST");
        }
        if (!request.url().query().isEmpty() || request.url().fragment() != null) {
            throw new ValidateException("OAuth 2.x token request parameters must not use URL query or fragment");
        }
        final MediaType media = request.body().media();
        if (!MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(media)) {
            throw new ValidateException("OAuth 2.x token endpoint requires application/x-www-form-urlencoded");
        }
        final String charset = media.parameter(MediaType.CHARSET_PARAMETER);
        if (charset != null && !Charset.UTF_8.equals(media.charset())) {
            throw new ValidateException("OAuth 2.x token request form charset must be UTF-8");
        }
        if (request.body().length() < 0 || request.body().length() > MAXIMUM_FORM_BYTES) {
            throw new ValidateException("OAuth 2.x token request form exceeds the allowed size");
        }
        if (!request.body().repeatable()) {
            throw new ValidateException(
                    "OAuth 2.x token request form must be buffered for decoder and client authentication reuse");
        }
    }

    /**
     * Groups form parameters while retaining the two RFC 8693 repeated fields.
     *
     * @param decoded ordered decoded form parameters
     * @return mutable insertion-ordered grouped parameters
     * @throws ValidateException if any other name occurs more than once
     */
    private static Map<String, List<String>> parameters(final List<NameValue> decoded) {
        final Map<String, List<String>> parameters = new LinkedHashMap<>(decoded.size());
        for (NameValue parameter : decoded) {
            if (!parameterName(parameter.name())) {
                throw new ValidateException("OAuth 2.x token request contains an invalid parameter name");
            }
            final List<String> values = parameters.computeIfAbsent(parameter.name(), ignored -> new ArrayList<>());
            if (!values.isEmpty() && !OAuth2.Parameters.RESOURCE.equals(parameter.name())
                    && !OAuth2.Parameters.AUDIENCE.equals(parameter.name())) {
                throw new ValidateException("OAuth 2.x token request parameter must not be repeated");
            }
            values.add(parameter.value());
        }
        return parameters;
    }

    /**
     * Tests the OAuth registration-name grammar used by token request parameter names.
     *
     * @param value decoded parameter name
     * @return whether the name is non-empty and contains only registered-name characters
     */
    private static boolean parameterName(final String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            final boolean valid = character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z
                    || character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z
                    || character >= Symbol.C_ZERO && character <= Symbol.C_NINE || character == Symbol.C_MINUS
                    || character == Symbol.C_DOT || character == Symbol.C_UNDERLINE;
            if (!valid) {
                return false;
            }
        }
        return true;
    }

    /**
     * Builds an authorization-code grant from its exact registered fields.
     *
     * @param parameters mutable grouped form parameters
     * @return standard authorization-code grant
     */
    private static AuthorizationCodeGrant authorizationCode(final Map<String, List<String>> parameters) {
        return new AuthorizationCodeGrant(required(parameters, OAuth2.Parameters.CODE),
                Optional.ofNullable(optional(parameters, OAuth2.Parameters.REDIRECT_URI)),
                Optional.ofNullable(optional(parameters, OAuth2.Parameters.CLIENT_ID)),
                Optional.ofNullable(optional(parameters, OAuth2.Parameters.CODE_VERIFIER)));
    }

    /**
     * Builds a refresh-token grant and discards transport-only client identification.
     *
     * @param parameters mutable grouped form parameters
     * @return standard refresh-token grant
     */
    private static RefreshTokenGrant refreshToken(final Map<String, List<String>> parameters) {
        optional(parameters, OAuth2.Parameters.CLIENT_ID);
        return new RefreshTokenGrant(required(parameters, OAuth2.Parameters.REFRESH_TOKEN), scope(parameters));
    }

    /**
     * Builds a client-credentials grant and discards its transport-only client identification.
     *
     * @param parameters mutable grouped form parameters
     * @return standard client-credentials grant
     */
    private static ClientCredentialsGrant clientCredentials(final Map<String, List<String>> parameters) {
        optional(parameters, OAuth2.Parameters.CLIENT_ID);
        return new ClientCredentialsGrant(scope(parameters));
    }

    /**
     * Builds an RFC 8693 token-exchange grant from its exact registered fields.
     *
     * @param parameters mutable grouped form parameters
     * @return standard token-exchange grant
     */
    private static TokenExchangeGrant exchange(final Map<String, List<String>> parameters) {
        optional(parameters, OAuth2.Parameters.CLIENT_ID);
        return new TokenExchangeGrant(repeated(parameters, OAuth2.Parameters.RESOURCE),
                repeated(parameters, OAuth2.Parameters.AUDIENCE), required(parameters, OAuth2.Parameters.SUBJECT_TOKEN),
                required(parameters, OAuth2.Parameters.SUBJECT_TOKEN_TYPE),
                Optional.ofNullable(optional(parameters, OAuth2.Parameters.REQUESTED_TOKEN_TYPE)),
                Optional.ofNullable(optional(parameters, OAuth2.Parameters.ACTOR_TOKEN)),
                Optional.ofNullable(optional(parameters, OAuth2.Parameters.ACTOR_TOKEN_TYPE)), scope(parameters));
    }

    /**
     * Builds an RFC 8628 device-code grant from its exact registered fields.
     *
     * @param parameters mutable grouped form parameters
     * @return standard device-code grant
     */
    private static DeviceCodeGrant deviceCode(final Map<String, List<String>> parameters) {
        return new DeviceCodeGrant(required(parameters, OAuth2.Parameters.DEVICE_CODE),
                Optional.ofNullable(optional(parameters, OAuth2.Parameters.CLIENT_ID)));
    }

    /**
     * Removes and parses the optional standard scope field.
     *
     * @param parameters mutable grouped form parameters
     * @return optional validated scope
     */
    private static Optional<Scope> scope(final Map<String, List<String>> parameters) {
        final String value = optional(parameters, OAuth2.Parameters.SCOPE);
        return value == null ? Optional.empty() : Optional.of(Scope.parse(value));
    }

    /**
     * Removes one required non-empty single-valued parameter.
     *
     * @param parameters mutable grouped form parameters
     * @param name       registered parameter name
     * @return required wire value
     * @throws ValidateException if the parameter is absent or empty
     */
    private static String required(final Map<String, List<String>> parameters, final String name) {
        final String value = optional(parameters, name);
        if (value == null || value.isEmpty()) {
            throw new ValidateException("OAuth 2.x token request requires non-empty " + name);
        }
        return value;
    }

    /**
     * Removes one optional single-valued parameter.
     *
     * @param parameters mutable grouped form parameters
     * @param name       parameter name
     * @return sole value or {@code null} when absent
     * @throws ValidateException if the parameter has multiple occurrences
     */
    private static String optional(final Map<String, List<String>> parameters, final String name) {
        final List<String> values = parameters.remove(name);
        if (values == null) {
            return null;
        }
        if (values.size() != 1) {
            throw new ValidateException("OAuth 2.x token request parameter must not be repeated: " + name);
        }
        return values.get(0);
    }

    /**
     * Removes one repeatable RFC 8693 parameter while preserving wire order.
     *
     * @param parameters mutable grouped form parameters
     * @param name       repeatable parameter name
     * @return immutable ordered values, or an empty list when absent
     */
    private static List<String> repeated(final Map<String, List<String>> parameters, final String name) {
        final List<String> values = parameters.remove(name);
        return values == null ? List.of() : List.copyOf(values);
    }

    /**
     * Identifies token request parameters owned by a standard supported grant.
     *
     * @param name exact form parameter name
     * @return {@code true} for a typed token request component
     */
    private static boolean registered(final String name) {
        return switch (name) {
            case OAuth2.Parameters.GRANT_TYPE, OAuth2.Parameters.CODE, OAuth2.Parameters.REDIRECT_URI, OAuth2.Parameters.CLIENT_ID, OAuth2.Parameters.CODE_VERIFIER, OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.SCOPE, OAuth2.Parameters.RESOURCE, OAuth2.Parameters.AUDIENCE, OAuth2.Parameters.SUBJECT_TOKEN, OAuth2.Parameters.SUBJECT_TOKEN_TYPE, OAuth2.Parameters.REQUESTED_TOKEN_TYPE, OAuth2.Parameters.ACTOR_TOKEN, OAuth2.Parameters.ACTOR_TOKEN_TYPE, OAuth2.Parameters.DEVICE_CODE -> true;
            default -> false;
        };
    }

    /**
     * Decodes one standard token endpoint request and selects its exact grant record.
     *
     * @param encoded immutable Fabric HTTP request
     * @return standard token request
     * @throws IllegalArgumentException if the request is {@code null}
     * @throws ProtocolException        with {@code unsupported_grant_type} for an unknown grant type
     * @throws ValidateException        if transport, form, multiplicity, or grant parameter syntax is invalid
     */
    @Override
    public TokenRequest decode(final HttpRequest encoded) {
        Assert.notNull(encoded, "OAuth 2.x token HTTP request must not be null");
        validateTransport(encoded);
        final Map<String, List<String>> parameters = parameters(
                formCodec.decode(encoded.body().bytes(MAXIMUM_FORM_BYTES)));
        final GrantType grantType = new GrantType(required(parameters, OAuth2.Parameters.GRANT_TYPE));
        optional(parameters, OAuth2.Parameters.CLIENT_SECRET);

        final TokenRequest.Grant grant;
        if (GrantType.AUTHORIZATION_CODE.equals(grantType)) {
            grant = authorizationCode(parameters);
        } else if (GrantType.REFRESH_TOKEN.equals(grantType)) {
            grant = refreshToken(parameters);
        } else if (GrantType.CLIENT_CREDENTIALS.equals(grantType)) {
            grant = clientCredentials(parameters);
        } else if (GrantType.TOKEN_EXCHANGE.equals(grantType)) {
            grant = exchange(parameters);
        } else if (GrantType.DEVICE_CODE.equals(grantType)) {
            grant = deviceCode(parameters);
        } else {
            throw new ProtocolException(OAuth2ErrorCode.UNSUPPORTED_GRANT_TYPE.value(),
                    "OAuth 2.x token request uses an unsupported grant_type");
        }

        for (String name : parameters.keySet()) {
            if (registered(name)) {
                throw new ValidateException(
                        "OAuth 2.x token request contains a parameter not defined for its grant: " + name);
            }
        }
        final Map<String, JsonValue> extensions = new LinkedHashMap<>(parameters.size());
        parameters.forEach((name, values) -> extensions.put(name, new JsonValue.StringValue(values.get(0))));
        return new TokenRequest(grant, new JsonValue.ObjectValue(extensions));
    }

}
