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
package org.miaixz.bus.auth.protocol.oidc.codec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.FabricX.Request;
import org.miaixz.bus.auth.protocol.oauth2.AuthorizationRequest;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationRequestDecoder;
import org.miaixz.bus.auth.protocol.oidc.AuthenticationRequest;
import org.miaixz.bus.auth.protocol.oidc.Display;
import org.miaixz.bus.auth.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.auth.protocol.oidc.Prompt;
import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Decodes an OpenID Connect Authorization Code Flow Authentication Request over the shared OAuth query decoder.
 * <p>
 * OAuth owns method, body, fragment, duplicate-name, client, redirect, scope, state, PKCE, and resource parsing. This
 * decoder removes only registered OIDC parameters from the nested OAuth extension object and applies their exact OIDC
 * wire grammar without performing client, user, consent, or authentication policy.
 * </p>
 *
 * @author Kimi Liu
 */
public class AuthenticationRequestDecoder implements Decoder<Request, AuthenticationRequest> {

    /**
     * Maximum accepted UTF-8 size of the claims parameter JSON document.
     */
    private static final int MAXIMUM_CLAIMS_BYTES = Math.toIntExact(64 * Normal.KIBI);

    /**
     * Shared strict OAuth authorization request decoder.
     */
    private final AuthorizationRequestDecoder oauthDecoder;

    /**
     * Externally selected provider-neutral JSON implementation.
     */
    private final JsonProvider jsonProvider;

    /**
     * Creates an OIDC Authentication Request decoder over the shared OAuth codec.
     *
     * @param oauthDecoder strict OAuth authorization request decoder
     * @param jsonProvider externally selected provider-neutral JSON implementation
     * @throws IllegalArgumentException if a collaborator is {@code null}
     */
    public AuthenticationRequestDecoder(final AuthorizationRequestDecoder oauthDecoder,
            final JsonProvider jsonProvider) {
        this.oauthDecoder = Assert
                .notNull(oauthDecoder, "OpenID Connect OAuth authorization request decoder must not be null");
        this.jsonProvider = Assert
                .notNull(jsonProvider, "OpenID Connect Authentication Request JSON provider must not be null");
    }

    /**
     * Removes one required non-empty string-valued OIDC parameter.
     *
     * @param parameters mutable OAuth extension map
     * @param name       exact registered parameter name
     * @return required wire value
     * @throws ValidateException if absent, empty, or not represented as a scalar string
     */
    private static String requiredString(final Map<String, JsonValue> parameters, final String name) {
        final String value = optionalString(parameters, name);
        if (value == null || value.isEmpty()) {
            throw new ValidateException("OpenID Connect Authentication Request requires non-empty " + name);
        }
        return value;
    }

    /**
     * Removes one optional scalar string-valued OIDC parameter.
     *
     * @param parameters mutable OAuth extension map
     * @param name       exact registered parameter name
     * @return wire value or {@code null}
     * @throws ValidateException if a present value is not a JSON string scalar
     */
    private static String optionalString(final Map<String, JsonValue> parameters, final String name) {
        final JsonValue value = parameters.remove(name);
        if (value == null) {
            return null;
        }
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("OpenID Connect Authentication Request parameter must be a string: " + name);
        }
        return string.value();
    }

    /**
     * Parses the {@code max_age} ABNF as a non-negative decimal integer without signs or coercion.
     *
     * @param value exact wire value
     * @return non-negative whole seconds
     * @throws ValidateException if syntax or range is invalid
     */
    private static long maximumAge(final String value) {
        if (value.isEmpty()) {
            throw new ValidateException("OpenID Connect max_age must contain at least one decimal digit");
        }
        for (int index = 0; index < value.length(); index++) {
            if (value.charAt(index) < Symbol.C_ZERO || value.charAt(index) > Symbol.C_NINE) {
                throw new ValidateException("OpenID Connect max_age must contain only decimal digits");
            }
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new ValidateException("OpenID Connect max_age exceeds the supported integer range", exception);
        }
    }

    /**
     * Parses one strict single-space-delimited OIDC string sequence.
     *
     * @param value optional wire value
     * @param label safe parameter label
     * @return immutable sequence, empty when the parameter is absent
     * @throws ValidateException if a present value is empty or contains repeated separators
     */
    private static List<String> spaceSeparated(final String value, final String label) {
        if (value == null) {
            return List.of();
        }
        if (value.isEmpty()) {
            throw new ValidateException(label + " must not be empty when present");
        }
        final String[] parts = value.split(Symbol.SPACE, -1);
        final List<String> values = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isEmpty()) {
                throw new ValidateException(label + " must use one ASCII space between values");
            }
            values.add(part);
        }
        return List.copyOf(values);
    }

    /**
     * Decodes one strict OIDC Authentication Request without executing authentication policy.
     *
     * @param encoded immutable Fabric HTTP request
     * @return validated typed Authentication Request
     * @throws IllegalArgumentException if the request is {@code null}
     * @throws ValidateException        if an OIDC parameter has invalid syntax or type
     * @throws ProtocolException        with a registered OIDC error code for unsupported Request Object parameters
     */
    @Override
    public AuthenticationRequest decode(final Request encoded) {
        final AuthorizationRequest oauth = oauthDecoder
                .decode(Assert.notNull(encoded, "OpenID Connect Authentication HTTP request must not be null"));
        final Map<String, JsonValue> parameters = new LinkedHashMap<>(oauth.extensions().values());
        if (parameters.remove(OpenIdConnect.Parameters.REQUEST) != null) {
            throw new ProtocolException("request_not_supported",
                    "OpenID Connect Request Objects by value are not supported");
        }
        if (parameters.remove(OpenIdConnect.Parameters.REQUEST_URI) != null) {
            throw new ProtocolException("request_uri_not_supported",
                    "OpenID Connect Request Objects by reference are not supported");
        }
        final String nonce = optionalString(parameters, OpenIdConnect.Claims.NONCE);
        final String display = optionalString(parameters, OpenIdConnect.Parameters.DISPLAY);
        final String prompt = optionalString(parameters, OpenIdConnect.Parameters.PROMPT);
        final String maxAge = optionalString(parameters, OpenIdConnect.Parameters.MAX_AGE);
        final String uiLocales = optionalString(parameters, OpenIdConnect.Parameters.UI_LOCALES);
        final String idTokenHint = optionalString(parameters, OpenIdConnect.Parameters.ID_TOKEN_HINT);
        final String loginHint = optionalString(parameters, OpenIdConnect.Parameters.LOGIN_HINT);
        final String acrValues = optionalString(parameters, OpenIdConnect.Parameters.ACR_VALUES);
        final String claims = optionalString(parameters, OpenIdConnect.Parameters.CLAIMS);
        final String responseMode = optionalString(parameters, OpenIdConnect.Parameters.RESPONSE_MODE);

        final AuthorizationRequest cleaned = new AuthorizationRequest(oauth.responseType(), oauth.clientId(),
                oauth.redirectUri(), oauth.scope(), oauth.state(), oauth.codeChallenge(), oauth.codeChallengeMethod(),
                new JsonValue.ObjectValue(parameters));
        return new AuthenticationRequest(cleaned, Optional.ofNullable(nonce),
                Optional.ofNullable(display == null ? null : new Display(display)),
                Optional.ofNullable(prompt == null ? null : Prompt.parse(prompt)),
                Optional.ofNullable(maxAge == null ? null : maximumAge(maxAge)),
                spaceSeparated(uiLocales, "OpenID Connect ui_locales"), Optional.ofNullable(idTokenHint),
                Optional.ofNullable(loginHint), spaceSeparated(acrValues, "OpenID Connect acr_values"),
                Optional.ofNullable(claims == null ? null : claims(claims)), Optional.ofNullable(responseMode),
                new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Parses one bounded UTF-8 claims parameter as an exact JSON object.
     *
     * @param value complete claims parameter JSON text
     * @return detached provider-neutral claims request object
     * @throws ValidateException if size, JSON syntax, or root type is invalid
     */
    private JsonValue.ObjectValue claims(final String value) {
        final byte[] bytes = value.getBytes(Charset.UTF_8);
        if (bytes.length == 0 || bytes.length > MAXIMUM_CLAIMS_BYTES) {
            throw new ValidateException("OpenID Connect claims parameter must contain one JSON object within 64 KiB");
        }
        final JsonValue decoded;
        try {
            decoded = jsonProvider.readValue(bytes);
        } catch (RuntimeException exception) {
            throw new ValidateException("OpenID Connect claims parameter must contain valid JSON", exception);
        }
        if (!(decoded instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("OpenID Connect claims parameter root must be a JSON object");
        }
        return new JsonValue.ObjectValue(object.values());
    }

}
