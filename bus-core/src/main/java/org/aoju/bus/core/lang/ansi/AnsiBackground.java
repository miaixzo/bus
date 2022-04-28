/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.lang.ansi;

/**
 * ANSI背景颜色枚举
 *
 * <p>来自Spring Boot</p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum AnsiBackground implements AnsiElement {

    /**
     * 默认背景色
     */
    DEFAULT("49"),

    /**
     * 黑色
     */
    BLACK("40"),

    /**
     * 红
     */
    RED("41"),

    /**
     * 绿
     */
    GREEN("42"),

    /**
     * 黄
     */
    YELLOW("43"),

    /**
     * 蓝
     */
    BLUE("44"),

    /**
     * 品红
     */
    MAGENTA("45"),

    /**
     * 青
     */
    CYAN("46"),

    /**
     * 白
     */
    WHITE("47"),

    /**
     * 亮黑
     */
    BRIGHT_BLACK("100"),

    /**
     * 亮红
     */
    BRIGHT_RED("101"),

    /**
     * 亮绿
     */
    BRIGHT_GREEN("102"),

    /**
     * 亮黄
     */
    BRIGHT_YELLOW("103"),

    /**
     * 亮蓝
     */
    BRIGHT_BLUE("104"),

    /**
     * 亮品红
     */
    BRIGHT_MAGENTA("105"),

    /**
     * 亮青
     */
    BRIGHT_CYAN("106"),

    /**
     * 亮白
     */
    BRIGHT_WHITE("107");

    private final String code;

    AnsiBackground(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code;
    }

}
