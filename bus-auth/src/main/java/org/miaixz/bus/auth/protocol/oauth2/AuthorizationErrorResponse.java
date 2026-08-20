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
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents an RFC 6749 authorization-endpoint error response independently of token-endpoint errors.
 *
 * @param error            authorization error code
 * @param errorDescription optional human-readable NQSCHAR diagnostic
 * @param errorUri         optional explanatory URI-reference
 * @param state            exact request state when the authorization request contained state
 * @param extensions       additional authorization response parameters
 * @author Kimi Liu
 */
public record AuthorizationErrorResponse(OAuth2ErrorCode error, Optional<String> errorDescription,
        Optional<String> errorUri, Optional<String> state, JsonValue.ObjectValue extensions)
        implements AuthorizationResponse {

    /**
     * Validates standard error components and isolates extension parameters.
     *
     * @throws IllegalArgumentException if a component or container is {@code null}
     * @throws ValidateException        if a component violates RFC 6749 or an extension replaces it
     */
    public AuthorizationErrorResponse {
        Assert.notNull(error, "OAuth authorization error code must not be null");
        Assert.notNull(errorDescription, "OAuth authorization error-description container must not be null");
        Assert.notNull(errorUri, "OAuth authorization error-URI container must not be null");
        Assert.notNull(state, "OAuth authorization error state container must not be null");
        Assert.notNull(extensions, "OAuth authorization error extensions must not be null");
        errorDescription.ifPresent(value -> requireCharacters(value, false, "OAuth error description"));
        errorUri.ifPresent(AuthorizationErrorResponse::requireUriReference);
        state.ifPresent(value -> {
            Assert.notBlank(value, "OAuth authorization error state must not be blank");
            requireCharacters(value, true, "OAuth authorization error state");
        });
        for (String name : extensions.values().keySet()) {
            Assert.notBlank(name, "OAuth authorization error extension name must not be blank");
            if (standard(name)) {
                throw new ValidateException("OAuth authorization error extension replaces a standard component");
            }
        }
        extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Identifies standard authorization error response parameters.
     *
     * @param name exact parameter name
     * @return {@code true} for a dedicated response component
     */
    private static boolean standard(final String name) {
        return switch (name) {
            case OAuth2.Parameters.ERROR, OAuth2.Parameters.ERROR_DESCRIPTION, OAuth2.Parameters.ERROR_URI, OAuth2.Parameters.STATE -> true;
            default -> false;
        };
    }

    /**
     * Validates an RFC 3986 URI-reference without requiring an absolute URI.
     *
     * @param value explanatory URI-reference
     */
    private static void requireUriReference(final String value) {
        Assert.notBlank(value, "OAuth authorization error URI must not be blank");
        try {
            new URI(value);
        } catch (URISyntaxException cause) {
            throw new ValidateException("OAuth authorization error URI must be a valid URI-reference", cause);
        }
    }

    /**
     * Enforces NQSCHAR or VSCHAR syntax for an authorization error parameter.
     *
     * @param value         parameter value
     * @param quotesAllowed whether quotation mark and reverse solidus are permitted
     * @param label         safe diagnostic label
     */
    private static void requireCharacters(final String value, final boolean quotesAllowed, final String label) {
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < 0x20 || character > 0x7e || !quotesAllowed && (character == 0x22 || character == 0x5c)) {
                throw new ValidateException(label + " contains a character outside its RFC 6749 grammar");
            }
        }
    }

}
