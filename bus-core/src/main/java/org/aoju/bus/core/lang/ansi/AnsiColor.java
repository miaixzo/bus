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
 * ANSI标准颜色
 *
 * <p>来自Spring Boot</p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum AnsiColor implements AnsiElement {

    /**
     * 默认前景色
     */
    DEFAULT("39"),

    /**
     * 黑
     */
    BLACK("30"),

    /**
     * 红
     */
    RED("31"),

    /**
     * 绿
     */
    GREEN("32"),

    /**
     * 黄
     */
    YELLOW("33"),

    /**
     * 蓝
     */
    BLUE("34"),

    /**
     * 品红
     */
    MAGENTA("35"),

    /**
     * 青
     */
    CYAN("36"),

    /**
     * 白
     */
    WHITE("37"),

    /**
     * 亮黑
     */
    BRIGHT_BLACK("90"),

    /**
     * 亮红
     */
    BRIGHT_RED("91"),

    /**
     * 亮绿
     */
    BRIGHT_GREEN("92"),

    /**
     * 亮黄
     */
    BRIGHT_YELLOW("93"),

    /**
     * 亮蓝
     */
    BRIGHT_BLUE("94"),

    /**
     * 亮品红
     */
    BRIGHT_MAGENTA("95"),

    /**
     * 亮青
     */
    BRIGHT_CYAN("96"),

    /**
     * 亮白
     */
    BRIGHT_WHITE("97");

    private final String code;

    AnsiColor(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code;
    }

}
