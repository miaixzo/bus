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
package org.miaixz.bus.core.xml;

import org.miaixz.bus.core.lang.Symbol;

import java.util.regex.Pattern;

/**
 * XML相关常量
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class XmlConsts {

    /**
     * 字符串常量：XML 不间断空格转义 {@code "&nbsp;" -> " "}
     */
    public static final String NBSP = "&nbsp;";

    /**
     * 字符串常量：XML And 符转义 {@code "&amp;" -> "&"}
     */
    public static final String AMP = "&amp;";
    /**
     * The Character '&amp;'.
     */
    public static final Character C_AMP = Symbol.C_AND;

    /**
     * 字符串常量：XML 双引号转义 {@code "&quot;" -> "\""}
     */
    public static final String QUOTE = "&quot;";

    /**
     * 字符串常量：XML 单引号转义 {@code "&apos" -> "'"}
     */
    public static final String APOS = "&apos;";
    /**
     * The Character '''.
     */
    public static final Character C_APOS = Symbol.C_SINGLE_QUOTE;

    /**
     * 字符串常量：XML 小于号转义 {@code "&lt;" -> "<"}
     */
    public static final String LT = "&lt;";

    /**
     * The Character '&lt;'.
     */
    public static final Character C_LT = '<';

    /**
     * 字符串常量：XML 大于号转义 {@code "&gt;" -> ">"}
     */
    public static final String GT = "&gt;";

    /**
     * The Character '&gt;'.
     */
    public static final Character C_GT = '>';

    /**
     * The Character '!'.
     */
    public static final Character C_BANG = '!';

    /**
     * The Character '?'.
     */
    public static final Character C_QUEST = '?';

    /**
     * 在XML中无效的字符 正则
     */
    public static final Pattern INVALID_PATTERN = Pattern.compile("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]");
    /**
     * 在XML中注释的内容 正则
     */
    public static final Pattern COMMENT_PATTERN = Pattern.compile("(?s)<!--.+?-->");
    /**
     * XML格式化输出默认缩进量
     */
    public static final int INDENT_DEFAULT = 2;

}
