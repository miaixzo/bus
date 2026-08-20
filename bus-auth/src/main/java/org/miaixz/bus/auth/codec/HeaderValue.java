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
package org.miaixz.bus.auth.codec;

import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Represents one decoded RFC 9110 authentication Header field value.
 * <p>
 * The value contains one authentication scheme followed by either token68 or an ordered auth-param list. It excludes
 * the Header field name and does not model a comma-separated list of challenges. Header credentials are sensitive at
 * use sites and must not be logged merely because they are held in this syntax value.
 * </p>
 *
 * @param scheme     case-insensitive RFC 9110 authentication scheme token
 * @param token68    optional token68 credentials
 * @param parameters ordered decoded auth-param entries
 * @author Kimi Liu
 */
public record HeaderValue(String scheme, Optional<String> token68, List<Parameter> parameters) {

    /**
     * Creates an immutable single authentication Header value.
     *
     * @param scheme     RFC 9110 token identifying the authentication scheme
     * @param token68    optional token68 credentials
     * @param parameters ordered decoded auth-param entries
     * @throws IllegalArgumentException if syntax is invalid, token68 and parameters are both present, or auth-param
     *                                  names are duplicated ignoring ASCII case
     */
    public HeaderValue {
        Assert.isTrue(token(scheme), "Authentication scheme must be an RFC 9110 token");
        Assert.notNull(token68, "Authentication token68 container must not be null");
        if (!token68.isEmpty()) {
            Assert.isTrue(token68(token68.getOrNull()), "Authentication token68 value is invalid");
        }
        token68 = Optional.ofNullable(token68.getOrNull());
        Assert.notNull(parameters, "Authentication Header parameters must not be null");
        final List<Parameter> copy = new ArrayList<>(parameters.size());
        final Set<String> names = new HashSet<>(parameters.size());
        for (Parameter parameter : parameters) {
            final Parameter value = Assert.notNull(parameter, "Authentication Header parameter must not be null");
            Assert.isTrue(token(value.name()), "Authentication Header parameter name must be an RFC 9110 token");
            Assert.isTrue(
                    names.add(value.name().toLowerCase(Locale.ROOT)),
                    "Authentication Header parameter names must be unique ignoring case");
            copy.add(value);
        }
        parameters = List.copyOf(copy);
        Assert.isTrue(
                token68.isEmpty() || parameters.isEmpty(),
                "Authentication Header token68 and parameters are mutually exclusive");
    }

    /**
     * Tests RFC 9110 token syntax using the fixed ASCII tchar set.
     *
     * @param value candidate token text
     * @return {@code true} when the value is a non-empty RFC 9110 token
     */
    private static boolean token(final String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            final char character = value.charAt(i);
            if (!alphaNumeric(character) && "!#$%&'*+-.^_`|~".indexOf(character) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests RFC 9110 token68 syntax, including trailing padding only.
     *
     * @param value candidate token68 text
     * @return {@code true} when the value satisfies the token68 grammar
     */
    private static boolean token68(final String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        boolean padding = false;
        boolean content = false;
        for (int i = 0; i < value.length(); i++) {
            final char character = value.charAt(i);
            if (character == Symbol.C_EQUAL) {
                padding = true;
            } else {
                if (padding || !alphaNumeric(character) && "-._~+/".indexOf(character) < 0) {
                    return false;
                }
                content = true;
            }
        }
        return content;
    }

    /**
     * Tests whether one character is an ASCII letter or digit.
     *
     * @param value candidate character
     * @return {@code true} for an ASCII letter or digit
     */
    private static boolean alphaNumeric(final char value) {
        return value >= 'a' && value <= 'z' || value >= 'A' && value <= 'Z'
                || value >= Symbol.C_ZERO && value <= Symbol.C_NINE;
    }

}
