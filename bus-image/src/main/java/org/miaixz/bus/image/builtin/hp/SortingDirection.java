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
package org.miaixz.bus.image.builtin.hp;

/**
 * Hanging Protocol sorting direction.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum SortingDirection {

    /**
     * Constant for the increasing value.
     */
    INCREASING("INCREASING", 1),
    /**
     * Constant for the decreasing value.
     */
    DECREASING("DECREASING", -1);

    /**
     * The code string value.
     */
    private final String codeString;

    /**
     * The sign value.
     */
    private final int sign;

    /**
     * Creates a new instance.
     *
     * @param codeString the code string.
     * @param sign       the sign.
     */
    SortingDirection(String codeString, int sign) {
        this.codeString = codeString;
        this.sign = sign;
    }

    /**
     * Gets the code string.
     *
     * @return the code string.
     */
    public String getCodeString() {
        return codeString;
    }

    /**
     * Gets the sign.
     *
     * @return the sign.
     */
    public int getSign() {
        return sign;
    }

    /**
     * Creates a value from the supplied input.
     *
     * @param codeString the code string.
     * @return the operation result.
     */
    public static SortingDirection fromString(String codeString) {
        for (SortingDirection value : values()) {
            if (value.codeString.equalsIgnoreCase(codeString)) {
                return value;
            }
        }
        throw new IllegalArgumentException("codeString: " + codeString);
    }

}
