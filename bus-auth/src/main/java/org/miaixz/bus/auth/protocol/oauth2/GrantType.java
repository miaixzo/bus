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
package org.miaixz.bus.auth.protocol.oauth2;

import java.net.URI;
import java.net.URISyntaxException;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Preserves a registered OAuth 2.x grant type name or extension URI-reference.
 * <p>
 * Constants advertise the grant variants implemented by the token endpoint. The value object remains open so decoders
 * and external registrations can preserve additional standards-compliant grant identifiers without treating them as
 * executable.
 * </p>
 *
 * @param value case-sensitive grant type wire value
 * @author Kimi Liu
 */
public record GrantType(String value) {

    /**
     * Authorization code grant defined by RFC 6749.
     */
    public static final GrantType AUTHORIZATION_CODE = new GrantType("authorization_code");

    /**
     * Refresh token grant defined by RFC 6749.
     */
    public static final GrantType REFRESH_TOKEN = new GrantType(OAuth2.Parameters.REFRESH_TOKEN);

    /**
     * Client credentials grant defined by RFC 6749.
     */
    public static final GrantType CLIENT_CREDENTIALS = new GrantType("client_credentials");

    /**
     * Token exchange grant defined by RFC 8693.
     */
    public static final GrantType TOKEN_EXCHANGE = new GrantType("urn:ietf:params:oauth:grant-type:token-exchange");

    /**
     * Device authorization grant defined by RFC 8628.
     */
    public static final GrantType DEVICE_CODE = new GrantType("urn:ietf:params:oauth:grant-type:device_code");

    /**
     * Validates the RFC 6749 grant-name or URI-reference grammar.
     *
     * @throws IllegalArgumentException if the value is {@code null} or empty
     * @throws ValidateException        if the value is neither a grant-name nor a valid URI-reference
     */
    public GrantType {
        Assert.notEmpty(value, "OAuth 2.x grant type must not be empty");
        if (!isGrantName(value)) {
            try {
                new URI(value);
            } catch (URISyntaxException exception) {
                throw new ValidateException("OAuth 2.x grant type must be a grant-name or URI-reference", exception);
            }
        }
    }

    /**
     * Tests the RFC 6749 grant-name production.
     *
     * @param value candidate grant type value
     * @return {@code true} when every character is an allowed name character
     */
    private static boolean isGrantName(final String value) {
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

}
