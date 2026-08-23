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
package org.miaixz.bus.auth.shared.pkce;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Represents a sensitive RFC 7636 code_verifier without exposing it through diagnostic rendering.
 *
 * @param value 43 to 128 unreserved ASCII characters
 * @author Kimi Liu
 */
public record CodeVerifier(String value) {

    /**
     * Validates the exact code_verifier grammar.
     *
     * @throws IllegalArgumentException if value is {@code null} or blank
     * @throws ValidateException        if length or character grammar is invalid
     */
    public CodeVerifier {
        validate(value, "PKCE code verifier");
    }

    /**
     * Validates the shared 43 to 128 unreserved grammar used by verifier and plain challenge.
     *
     * @param value candidate text
     * @param label semantic diagnostic label
     */
    static void validate(final String value, final String label) {
        Assert.notBlank(value, label + " must not be blank");
        if (value.length() < 43 || value.length() > 128 || !unreserved(value)) {
            throw new ValidateException(label + " must contain 43 to 128 unreserved characters");
        }
    }

    /**
     * Tests the RFC 3986 unreserved ASCII character set.
     *
     * @param value candidate text
     * @return whether every character is unreserved
     */
    static boolean unreserved(final String value) {
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (!(character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z)
                    && !(character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z)
                    && !(character >= Symbol.C_ZERO && character <= Symbol.C_NINE) && character != Symbol.C_MINUS
                    && character != Symbol.C_DOT && character != Symbol.C_UNDERLINE && character != Symbol.C_TILDE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a fixed non-sensitive diagnostic representation.
     *
     * @return redacted verifier label
     */
    @Override
    public String toString() {
        return "CodeVerifier[value=[REDACTED]]";
    }

}
