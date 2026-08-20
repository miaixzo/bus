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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents an RFC 6749 authorization-code success response without admitting error parameters.
 *
 * @param code       authorization code issued by the authorization server
 * @param state      exact request state when the authorization request contained state
 * @param extensions additional response parameters defined by compatible OAuth extensions
 * @author Kimi Liu
 */
public record AuthorizationCodeResponse(String code, Optional<String> state, JsonValue.ObjectValue extensions)
        implements AuthorizationResponse {

    /**
     * Validates the standard success components and isolates extension parameters.
     *
     * @throws IllegalArgumentException if a component or container is {@code null}
     * @throws ValidateException        if code/state syntax is invalid or an extension replaces a standard component
     */
    public AuthorizationCodeResponse {
        Assert.notBlank(code, "OAuth authorization code must not be blank");
        Assert.notNull(state, "OAuth authorization response state container must not be null");
        Assert.notNull(extensions, "OAuth authorization response extensions must not be null");
        requireVisibleAscii(code, "OAuth authorization code");
        state.ifPresent(value -> {
            Assert.notBlank(value, "OAuth authorization response state must not be blank");
            requireVisibleAscii(value, "OAuth authorization response state");
        });
        for (String name : extensions.values().keySet()) {
            Assert.notBlank(name, "OAuth authorization response extension name must not be blank");
            if (OAuth2.Parameters.CODE.equals(name) || OAuth2.Parameters.STATE.equals(name)) {
                throw new ValidateException("OAuth authorization response extension replaces a standard component");
            }
        }
        extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Validates an RFC 6749 visible-ASCII response value.
     *
     * @param value candidate response value
     * @param label safe diagnostic label
     */
    private static void requireVisibleAscii(final String value, final String label) {
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < 0x20 || character > 0x7e) {
                throw new ValidateException(label + " contains a character outside RFC 6749 VSCHAR");
            }
        }
    }

}
