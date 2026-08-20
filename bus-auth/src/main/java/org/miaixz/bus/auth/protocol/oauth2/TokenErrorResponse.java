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
 * Represents an RFC 6749 token-endpoint error body independently of successful token endpoint responses.
 *
 * @param error            token endpoint error code
 * @param errorDescription optional human-readable NQSCHAR diagnostic
 * @param errorUri         optional explanatory URI-reference
 * @param extensions       additional token error members
 * @author Kimi Liu
 */
public record TokenErrorResponse(OAuth2ErrorCode error, Optional<String> errorDescription, Optional<String> errorUri,
        JsonValue.ObjectValue extensions) {

    /**
     * Validates standard error members and isolates extension values.
     *
     * @throws IllegalArgumentException if a component or container is {@code null}
     * @throws ValidateException        if a value violates RFC 6749 or an extension replaces a standard member
     */
    public TokenErrorResponse {
        Assert.notNull(error, "OAuth token error code must not be null");
        Assert.notNull(errorDescription, "OAuth token error-description container must not be null");
        Assert.notNull(errorUri, "OAuth token error-URI container must not be null");
        Assert.notNull(extensions, "OAuth token error extensions must not be null");
        errorDescription.ifPresent(TokenErrorResponse::requireNqschar);
        errorUri.ifPresent(TokenErrorResponse::requireUriReference);
        for (String name : extensions.values().keySet()) {
            Assert.notBlank(name, "OAuth token error extension member name must not be blank");
            if (OAuth2.Parameters.ERROR.equals(name) || OAuth2.Parameters.ERROR_DESCRIPTION.equals(name)
                    || OAuth2.Parameters.ERROR_URI.equals(name)) {
                throw new ValidateException("OAuth token error extension replaces a standard member");
            }
        }
        extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Validates RFC 6749 NQSCHAR syntax.
     *
     * @param value human-readable diagnostic value
     */
    private static void requireNqschar(final String value) {
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < 0x20 || character > 0x7e || character == 0x22 || character == 0x5c) {
                throw new ValidateException("OAuth token error description contains a non-NQSCHAR character");
            }
        }
    }

    /**
     * Validates an explanatory RFC 3986 URI-reference.
     *
     * @param value URI-reference value
     */
    private static void requireUriReference(final String value) {
        Assert.notBlank(value, "OAuth token error URI must not be blank");
        try {
            new URI(value);
        } catch (URISyntaxException cause) {
            throw new ValidateException("OAuth token error URI must be a valid URI-reference", cause);
        }
    }

}
