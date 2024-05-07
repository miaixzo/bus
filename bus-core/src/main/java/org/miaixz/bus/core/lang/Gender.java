/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 性别相关类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@AllArgsConstructor
public enum Gender {

    /**
     * MALE/FAMALE为正常值,通过{@link Gender#of(String)}
     * 方法获取真实的性别UNKNOWN为容错值,部分平台不会返回用户性别,
     * 为了方便统一,使用UNKNOWN标记所有未知或不可测的用户性别信息
     */
    MALE(1, "M", "男"),
    FEMALE(0, "F", "女"),
    UNKNOWN(-1, "U", "未知");

    private final int key;
    private final String code;
    private final String desc;

    public static Gender of(String code) {
        if (null == code) {
            return UNKNOWN;
        }
        String[] males = {"M", "男", Symbol.ONE, "MALE"};
        if (Arrays.asList(males).contains(code.toUpperCase())) {
            return MALE;
        }
        String[] females = {"F", "女", Symbol.ZERO, "FEMALE"};
        if (Arrays.asList(females).contains(code.toUpperCase())) {
            return FEMALE;
        }
        return UNKNOWN;
    }

}
