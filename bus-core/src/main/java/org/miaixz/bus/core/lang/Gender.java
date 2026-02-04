/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration for gender-related information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum Gender {

    /**
     * Male gender. MALE/FEMALE are normal values. The actual gender is obtained through the {@link Gender#of(String)}
     * method. UNKNOWN is a fallback value for platforms that do not return user gender. It is used to mark all unknown
     * or unpredictable user gender information for consistency.
     */
    MALE(1, "M", "男"),
    /**
     * Female gender.
     */
    FEMALE(0, "F", "女"),
    /**
     * Unknown gender. Used when gender information is not available or cannot be determined.
     */
    UNKNOWN(-1, "U", "未知");

    /**
     * The integer key representing the gender.
     */
    private final int key;
    /**
     * The string code representing the gender (e.g., "M", "F", "U").
     */
    private final String code;
    /**
     * The descriptive name of the gender in Chinese.
     */
    private final String desc;

    /**
     * Returns the {@code Gender} enum constant corresponding to the given code. This method supports various
     * representations for male and female, including Chinese characters and numeric symbols.
     *
     * @param code The string code representing the gender (e.g., "M", "F", "男", "女", "1", "0").
     * @return The {@code Gender} enum constant, or {@link #UNKNOWN} if the code does not match any known gender.
     */
    public static Gender of(String code) {
        if (null == code) {
            return UNKNOWN;
        }
        String[] males = { "M", "男", Symbol.ONE, "MALE" };
        if (Arrays.asList(males).contains(code.toUpperCase())) {
            return MALE;
        }
        String[] females = { "F", "女", Symbol.ZERO, "FEMALE" };
        if (Arrays.asList(females).contains(code.toUpperCase())) {
            return FEMALE;
        }
        return UNKNOWN;
    }

}
