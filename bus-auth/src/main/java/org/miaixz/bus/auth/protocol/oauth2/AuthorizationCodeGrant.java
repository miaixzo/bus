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

import org.miaixz.bus.auth.shared.pkce.CodeVerifier;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents RFC 6749 authorization-code grant parameters, including the RFC 7636 verifier when used.
 * <p>
 * Authorization codes and code verifiers are transient sensitive values. The record retains them only for token
 * endpoint processing and excludes both from its diagnostic representation.
 * </p>
 *
 * @param code         authorization code received from the authorization endpoint
 * @param redirectUri  optional redirect URI that must exactly match the authorization request
 * @param clientId     optional client identifier required for an unauthenticated client
 * @param codeVerifier optional PKCE code verifier
 * @author Kimi Liu
 */
public record AuthorizationCodeGrant(String code, Optional<String> redirectUri, Optional<String> clientId,
        Optional<String> codeVerifier) implements TokenRequest.Grant {

    /**
     * Creates and validates an authorization-code grant parameter set.
     *
     * @throws IllegalArgumentException if a required value or optional container is {@code null}
     * @throws ValidateException        if a value violates RFC 6749 or RFC 7636 wire syntax
     */
    public AuthorizationCodeGrant {
        Assert.notEmpty(code, "OAuth 2.x authorization code must not be empty");
        requireVisibleAscii(code, "OAuth 2.x authorization code");
        Assert.notNull(redirectUri, "OAuth 2.x authorization-code redirect URI container must not be null");
        Assert.notNull(clientId, "OAuth 2.x authorization-code client identifier container must not be null");
        Assert.notNull(codeVerifier, "OAuth 2.x authorization-code verifier container must not be null");

        final String redirect = redirectUri.getOrNull();
        if (redirect != null) {
            validateRedirectUri(redirect);
        }
        final String client = clientId.getOrNull();
        if (client != null) {
            Assert.notEmpty(client, "OAuth 2.x authorization-code client identifier must not be empty when present");
            requireVisibleAscii(client, "OAuth 2.x authorization-code client identifier");
        }
        final String verifier = codeVerifier.getOrNull();
        if (verifier != null) {
            new CodeVerifier(verifier);
        }

        redirectUri = Optional.ofNullable(redirect);
        clientId = Optional.ofNullable(client);
        codeVerifier = Optional.ofNullable(verifier);
    }

    /**
     * Validates an absolute no-fragment redirect URI.
     *
     * @param value redirect URI wire value
     * @throws ValidateException if the value is not a valid absolute no-fragment URI
     */
    private static void validateRedirectUri(final String value) {
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.getRawFragment() != null) {
                throw new ValidateException("OAuth 2.x redirect URI must be absolute and must not contain a fragment");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException("OAuth 2.x redirect URI must be a valid absolute URI", exception);
        }
    }

    /**
     * Validates one non-empty RFC 6749 {@code VSCHAR} value.
     *
     * @param value value to inspect
     * @param label safe component label used in diagnostics
     * @throws ValidateException if a character lies outside visible ASCII
     */
    private static void requireVisibleAscii(final String value, final String label) {
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < 0x20 || character > 0x7e) {
                throw new ValidateException(label + " contains a character outside RFC 6749 VSCHAR");
            }
        }
    }

    /**
     * Returns a diagnostic representation without authorization code or PKCE verifier material.
     *
     * @return redacted grant summary
     */
    @Override
    public String toString() {
        return "AuthorizationCodeGrant[code=[REDACTED], redirectUri=" + redirectUri + ", clientId=" + clientId
                + ", codeVerifier=[REDACTED]]";
    }

}
