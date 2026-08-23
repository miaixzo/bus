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
package org.miaixz.bus.auth.source.protocol.oauth2;

import java.util.HashSet;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Preserves an OAuth authorization endpoint response-type value using the RFC 6749 open grammar.
 * <p>
 * The authorization endpoint generates only the authorization-code response. Additional registered response names can
 * still be decoded and represented without causing the runtime to advertise or execute unsupported flows.
 * </p>
 *
 * @param value case-sensitive, space-delimited response-type wire value
 * @author Kimi Liu
 */
public record ResponseType(String value) {

    /**
     * Authorization code response type defined by RFC 6749.
     */
    public static final ResponseType CODE = new ResponseType("code");

    /**
     * Validates the RFC 6749 response-type and response-name productions.
     *
     * @throws IllegalArgumentException if the value is {@code null} or empty
     * @throws ValidateException        if spacing, characters, or duplicate names violate the response-type grammar
     */
    public ResponseType {
        Assert.notEmpty(value, "OAuth 2.x response type must not be empty");
        final String[] names = value.split(Symbol.SPACE, -1);
        final Set<String> unique = new HashSet<>(names.length);
        for (String name : names) {
            validateName(name);
            if (!unique.add(name)) {
                throw new ValidateException("OAuth 2.x response type must not contain duplicate response names");
            }
        }
    }

    /**
     * Validates one RFC 6749 response-name.
     *
     * @param name response name to validate
     * @throws ValidateException if the name is empty or contains a character outside {@code response-char}
     */
    private static void validateName(final String name) {
        if (name.isEmpty()) {
            throw new ValidateException("OAuth 2.x response type must use one ASCII space between response names");
        }
        for (int index = 0; index < name.length(); index++) {
            final char character = name.charAt(index);
            if (character < 0x21 || character > 0x7e || character == 0x22 || character == 0x5c) {
                throw new ValidateException(
                        "OAuth 2.x response name contains a character outside RFC 6749 response-char");
            }
        }
    }

}
