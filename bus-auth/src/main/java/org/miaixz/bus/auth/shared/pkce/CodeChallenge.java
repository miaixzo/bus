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
 * Binds one RFC 7636 code_challenge value to its exact code_challenge_method.
 *
 * @param value  encoded challenge value
 * @param method registered derivation method
 * @author Kimi Liu
 */
public record CodeChallenge(String value, PkceMethod method) {

    /**
     * Validates method-specific challenge grammar.
     *
     * @throws IllegalArgumentException if a component is {@code null} or value is blank
     * @throws ValidateException        if method is unknown or value violates its registered grammar
     */
    public CodeChallenge {
        Assert.notBlank(value, "PKCE code challenge must not be blank");
        Assert.notNull(method, "PKCE code challenge method must not be null");
        if (PkceMethod.S256.equals(method)) {
            if (value.length() != 43 || !base64Url(value)) {
                throw new ValidateException("PKCE S256 challenge must contain 43 Base64URL characters");
            }
        } else if (PkceMethod.PLAIN.equals(method)) {
            CodeVerifier.validate(value, "PKCE plain challenge");
        } else {
            throw new ValidateException("PKCE challenge method is not supported");
        }
    }

    /**
     * Tests the unpadded Base64URL alphabet required for an S256 challenge.
     *
     * @param candidate encoded challenge text
     * @return whether every character belongs to the Base64URL alphabet
     */
    private static boolean base64Url(final String candidate) {
        for (int index = 0; index < candidate.length(); index++) {
            final char character = candidate.charAt(index);
            if (!(character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z)
                    && !(character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z)
                    && !(character >= Symbol.C_ZERO && character <= Symbol.C_NINE) && character != Symbol.C_MINUS
                    && character != Symbol.C_UNDERLINE) {
                return false;
            }
        }
        return true;
    }

}
