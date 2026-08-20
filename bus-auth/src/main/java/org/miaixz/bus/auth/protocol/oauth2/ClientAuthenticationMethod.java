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

import org.miaixz.bus.auth.Endpoint;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents an OAuth token endpoint client-authentication method published through RFC 8414 metadata.
 * <p>
 * This protocol value is narrower than {@link Endpoint.Authentication}: it describes an IANA-registered metadata value,
 * while the root type declares authentication accepted by one concrete deployment endpoint.
 * </p>
 *
 * @param value case-sensitive registered method wire value
 * @author Kimi Liu
 */
public record ClientAuthenticationMethod(String value) {

    /**
     * Public-client method that performs no client authentication.
     */
    public static final ClientAuthenticationMethod NONE = new ClientAuthenticationMethod(
            Endpoint.Authentication.NONE.value());

    /**
     * Client secret carried by HTTP Basic authentication.
     */
    public static final ClientAuthenticationMethod CLIENT_SECRET_BASIC = new ClientAuthenticationMethod(
            Endpoint.Authentication.CLIENT_SECRET_BASIC.value());

    /**
     * Client secret carried in the form-encoded token request body.
     */
    public static final ClientAuthenticationMethod CLIENT_SECRET_POST = new ClientAuthenticationMethod(
            Endpoint.Authentication.CLIENT_SECRET_POST.value());

    /**
     * Private-key JWT client assertion authentication.
     */
    public static final ClientAuthenticationMethod PRIVATE_KEY_JWT = new ClientAuthenticationMethod(
            Endpoint.Authentication.PRIVATE_KEY_JWT.value());

    /**
     * PKI mutual-TLS client authentication defined by RFC 8705.
     */
    public static final ClientAuthenticationMethod TLS_CLIENT_AUTH = new ClientAuthenticationMethod(
            Endpoint.Authentication.TLS_CLIENT_AUTH.value());

    /**
     * Self-signed certificate mutual-TLS client authentication defined by RFC 8705.
     */
    public static final ClientAuthenticationMethod SELF_SIGNED_TLS_CLIENT_AUTH = new ClientAuthenticationMethod(
            Endpoint.Authentication.SELF_SIGNED_TLS_CLIENT_AUTH.value());

    /**
     * Validates an extensible OAuth client-authentication method registration name.
     *
     * @throws IllegalArgumentException if the value is {@code null} or empty
     * @throws ValidateException        if the value contains a character outside the OAuth registration-name grammar
     */
    public ClientAuthenticationMethod {
        Assert.notEmpty(value, "OAuth 2.x client authentication method must not be empty");
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            final boolean valid = character >= 'A' && character <= 'Z' || character >= 'a' && character <= 'z'
                    || character >= Symbol.C_ZERO && character <= Symbol.C_NINE || character == Symbol.C_MINUS
                    || character == Symbol.C_DOT || character == Symbol.C_UNDERLINE;
            if (!valid) {
                throw new ValidateException(
                        "OAuth 2.x client authentication method contains an invalid registration-name character");
            }
        }
    }

}
