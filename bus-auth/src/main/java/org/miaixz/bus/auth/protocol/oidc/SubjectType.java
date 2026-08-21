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
package org.miaixz.bus.auth.protocol.oidc;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Preserves an OpenID Connect subject-identifier type registration or legal extension value.
 *
 * @param value exact case-sensitive subject-type wire value
 * @author Kimi Liu
 */
public record SubjectType(String value) {

    /**
     * Public subject identifier type defined by OpenID Connect Core.
     */
    public static final SubjectType PUBLIC = new SubjectType("public");

    /**
     * Pairwise pseudonymous subject identifier type defined by OpenID Connect Core.
     */
    public static final SubjectType PAIRWISE = new SubjectType("pairwise");

    /**
     * Validates the extensible registration-name grammar without changing case.
     *
     * @throws IllegalArgumentException if {@code value} is {@code null} or empty
     * @throws ValidateException        if a character is outside the OAuth registration-name grammar
     */
    public SubjectType {
        Assert.notEmpty(value, "OpenID Connect subject type must not be empty");
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            final boolean valid = character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z
                    || character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z
                    || character >= Symbol.C_ZERO && character <= Symbol.C_NINE || character == Symbol.C_MINUS
                    || character == Symbol.C_DOT || character == Symbol.C_UNDERLINE;
            if (!valid) {
                throw new ValidateException(
                        "OpenID Connect subject type contains an invalid registration-name character");
            }
        }
    }

}
