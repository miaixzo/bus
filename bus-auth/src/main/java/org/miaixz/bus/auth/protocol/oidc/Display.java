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
 * Preserves an OpenID Connect display-mode registration or legal extension value.
 *
 * @param value exact case-sensitive display wire value
 * @author Kimi Liu
 */
public record Display(String value) {

    /**
     * Full user-agent page display mode.
     */
    public static final Display PAGE = new Display("page");

    /**
     * Popup user-agent window display mode.
     */
    public static final Display POPUP = new Display("popup");

    /**
     * Touch-oriented device display mode.
     */
    public static final Display TOUCH = new Display("touch");

    /**
     * Feature-phone WAP display mode.
     */
    public static final Display WAP = new Display("wap");

    /**
     * Validates the extensible display registration-name grammar.
     *
     * @throws IllegalArgumentException if value is {@code null} or empty
     * @throws ValidateException        if a character is outside the OAuth registration-name grammar
     */
    public Display {
        Assert.notEmpty(value, "OpenID Connect display value must not be empty");
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            final boolean valid = character >= 'A' && character <= 'Z' || character >= 'a' && character <= 'z'
                    || character >= Symbol.C_ZERO && character <= Symbol.C_NINE || character == Symbol.C_MINUS
                    || character == Symbol.C_DOT || character == Symbol.C_UNDERLINE;
            if (!valid) {
                throw new ValidateException("OpenID Connect display contains an invalid registration-name character");
            }
        }
    }

}
