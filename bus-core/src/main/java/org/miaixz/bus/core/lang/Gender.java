/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
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
