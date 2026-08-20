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

/**
 * Represents the standard OAuth 2.x error response parameters shared by authorization and token operations.
 * <p>
 * Transport status, response headers, and protocol success values intentionally remain outside this wire model. The
 * same record can therefore be encoded for a redirect response or a JSON error body according to the owning endpoint.
 * </p>
 *
 * @param error            required OAuth error code
 * @param errorDescription optional human-readable diagnostic text
 * @param errorUri         optional URI-reference identifying explanatory material
 * @param state            optional request-correlation value returned by an authorization endpoint
 * @author Kimi Liu
 */
public record OAuth2ErrorResponse(OAuth2ErrorCode error, Optional<String> errorDescription, Optional<String> errorUri,
        Optional<String> state) {

    /**
     * Creates and validates an OAuth 2.x error parameter set using RFC 6749 wire syntax.
     *
     * @throws IllegalArgumentException if a required component or optional container is {@code null}
     * @throws ValidateException        if a present component violates its RFC wire grammar
     */
    public OAuth2ErrorResponse {
        Assert.notNull(error, "OAuth 2.x error response code must not be null");
        Assert.notNull(errorDescription, "OAuth 2.x error description container must not be null");
        Assert.notNull(errorUri, "OAuth 2.x error URI container must not be null");
        Assert.notNull(state, "OAuth 2.x error state container must not be null");

        final String description = errorDescription.getOrNull();
        if (description != null) {
            requireCharacters(description, false, "OAuth 2.x error description");
        }
        final String uri = errorUri.getOrNull();
        if (uri != null) {
            try {
                new URI(uri);
            } catch (URISyntaxException exception) {
                throw new ValidateException("OAuth 2.x error URI must be a valid URI-reference", exception);
            }
        }
        final String stateValue = state.getOrNull();
        if (stateValue != null) {
            Assert.notEmpty(stateValue, "OAuth 2.x error state must not be empty when present");
            requireCharacters(stateValue, true, "OAuth 2.x error state");
        }

        errorDescription = Optional.ofNullable(description);
        errorUri = Optional.ofNullable(uri);
        state = Optional.ofNullable(stateValue);
    }

    /**
     * Enforces either the RFC {@code NQSCHAR} set or the inclusive visible ASCII range.
     *
     * @param value         value to inspect
     * @param quotesAllowed whether quotation mark and reverse solidus remain valid
     * @param label         safe component label used by diagnostics
     * @throws ValidateException if a character lies outside the selected grammar
     */
    private static void requireCharacters(final String value, final boolean quotesAllowed, final String label) {
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < 0x20 || character > 0x7e || (!quotesAllowed && (character == 0x22 || character == 0x5c))) {
                throw new ValidateException(label + " contains a character outside its RFC 6749 grammar");
            }
        }
    }

}
