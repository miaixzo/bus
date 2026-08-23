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

import java.util.Locale;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Preserves an OAuth access-token type name with the case-insensitive comparison required by RFC 6749.
 * <p>
 * The original spelling remains available for wire encoding. Unknown registered type names remain representable so
 * extension specifications do not require a framework release.
 * </p>
 *
 * @param value original access-token type wire value
 * @author Kimi Liu
 */
public record TokenType(String value) {

    /**
     * Bearer access-token type defined by RFC 6750.
     */
    public static final TokenType BEARER = new TokenType("Bearer");

    /**
     * Token-exchange marker for an issued token that is not usable as an OAuth access token.
     */
    public static final TokenType N_A = new TokenType("N_A");

    /**
     * Demonstrating Proof-of-Possession access-token type defined by RFC 9449.
     */
    public static final TokenType DPOP = new TokenType("DPoP");

    /**
     * Validates the extensible RFC 6749 access-token type-name grammar.
     *
     * @throws IllegalArgumentException if the value is {@code null} or empty
     * @throws ValidateException        if the value contains a character outside the type-name grammar
     */
    public TokenType {
        Assert.notEmpty(value, "OAuth 2.x token type must not be empty");
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            final boolean valid = character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z
                    || character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z
                    || character >= Symbol.C_ZERO && character <= Symbol.C_NINE || character == Symbol.C_MINUS
                    || character == Symbol.C_DOT || character == Symbol.C_UNDERLINE;
            if (!valid) {
                throw new ValidateException("OAuth 2.x token type contains a character outside RFC 6749 type-name");
            }
        }
    }

    /**
     * Compares token type names without regard to ASCII case as required by RFC 6749.
     *
     * @param other comparison candidate
     * @return {@code true} when both type names are equal ignoring case
     */
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof TokenType tokenType && value.equalsIgnoreCase(tokenType.value);
    }

    /**
     * Returns a hash derived from the case-insensitive token type name.
     *
     * @return hash compatible with {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return value.toLowerCase(Locale.ROOT).hashCode();
    }

    /**
     * Returns the original non-sensitive wire spelling.
     *
     * @return token type wire value
     */
    @Override
    public String toString() {
        return value;
    }

}
