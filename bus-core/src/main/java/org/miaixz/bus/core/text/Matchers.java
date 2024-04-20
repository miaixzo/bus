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
package org.miaixz.bus.core.text;


import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.StringKit;

import java.util.Arrays;

/**
 * 一个匹配器类,可以查询它来确定一个字符数组是否存在部分匹配
 * 如果这些还不够,您可以子类化并实现自己的匹配器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Matchers {

    /**
     * 匹配逗号字符.
     */
    private static final Matchers COMMA_MATCHER = new CharMatcher(Symbol.C_COMMA);
    /**
     * 匹配tab字符.
     */
    private static final Matchers TAB_MATCHER = new CharMatcher(Symbol.C_HT);
    /**
     * 匹配空格字符.
     */
    private static final Matchers SPACE_MATCHER = new CharMatcher(Symbol.C_SPACE);
    /**
     * 匹配相同的字符,即空格、制表符、换行符等.
     */
    private static final Matchers SPLIT_MATCHER = new CharSetMatcher(" \t\n\r\f".toCharArray());
    /**
     * 匹配字符串trim()空白字符.
     */
    private static final Matchers TRIM_MATCHER = new TrimMatcher();
    /**
     * 匹配双引号字符.
     */
    private static final Matchers SINGLE_QUOTE_MATCHER = new CharMatcher(Symbol.C_SINGLE_QUOTE);
    /**
     * 匹配双引号字符.
     */
    private static final Matchers DOUBLE_QUOTE_MATCHER = new CharMatcher(Symbol.C_DOUBLE_QUOTES);
    /**
     * 匹配单引号或双引号字符.
     */
    private static final Matchers QUOTE_MATCHER = new CharSetMatcher("'\"".toCharArray());
    /**
     * 匹配任何字符.
     */
    private static final Matchers NONE_MATCHER = new NoMatcher();

    protected Matchers() {
        super();
    }

    /**
     * 返回一个匹配逗号字符的匹配器
     *
     * @return 逗号的匹配器
     */
    public static Matchers commaMatcher() {
        return COMMA_MATCHER;
    }

    /**
     * 返回一个匹配tab字符的匹配器
     *
     * @return tab的匹配器
     */
    public static Matchers tabMatcher() {
        return TAB_MATCHER;
    }

    /**
     * 返回一个匹配空格字符的匹配器
     *
     * @return 空格的匹配器
     */
    public static Matchers spaceMatcher() {
        return SPACE_MATCHER;
    }

    /**
     * 匹配相同的字符,即空格、制表符、换行符等.
     *
     * @return 分割匹配器
     */
    public static Matchers splitMatcher() {
        return SPLIT_MATCHER;
    }

    /**
     * 匹配字符串trim()空白字符.
     *
     * @return 空白匹配器
     */
    public static Matchers trimMatcher() {
        return TRIM_MATCHER;
    }

    /**
     * 匹配字符串单引号字符.
     *
     * @return 单引号匹配器
     */
    public static Matchers singleQuoteMatcher() {
        return SINGLE_QUOTE_MATCHER;
    }

    /**
     * 匹配字符串双引号字符.
     *
     * @return 双引号匹配器
     */
    public static Matchers doubleQuoteMatcher() {
        return DOUBLE_QUOTE_MATCHER;
    }

    /**
     * 匹配字符串单引号/双引号字符.
     *
     * @return 单引号/双引号匹配器
     */
    public static Matchers quoteMatcher() {
        return QUOTE_MATCHER;
    }

    /**
     * 匹配任何字符.
     *
     * @return 什么也不匹配的匹配器
     */
    public static Matchers noneMatcher() {
        return NONE_MATCHER;
    }

    /**
     * 构造函数
     *
     * @param ch 匹配的字符不能为空
     * @return 给定的字符返回一个新的匹配器
     */
    public static Matchers charMatcher(final char ch) {
        return new CharMatcher(ch);
    }

    /**
     * 构造函数
     *
     * @param chars 对要匹配的字符进行字符切分,null或empty不匹配任何字符
     * @return 给定字符的新匹配器[]
     */
    public static Matchers charSetMatcher(final char... chars) {
        if (null == chars || chars.length == 0) {
            return NONE_MATCHER;
        }
        if (chars.length == 1) {
            return new CharMatcher(chars[0]);
        }
        return new CharSetMatcher(chars);
    }

    /**
     * 构造函数
     *
     * @param chars 对要匹配的字符进行字符切分,null或empty不匹配任何字符
     * @return 给定字符的新匹配器
     */
    public static Matchers charSetMatcher(final String chars) {
        if (StringKit.isEmpty(chars)) {
            return NONE_MATCHER;
        }
        if (chars.length() == 1) {
            return new CharMatcher(chars.charAt(0));
        }
        return new CharSetMatcher(chars.toCharArray());
    }

    /**
     * 构造函数
     *
     * @param text 匹配的字符串为null或空,不匹配任何内容
     * @return 给定字符串返回一个新的匹配器
     */
    public static Matchers stringMatcher(final String text) {
        if (StringKit.isEmpty(text)) {
            return NONE_MATCHER;
        }
        return new StringMatcher(text);
    }

    /**
     * 返回匹配字符的数量,如果没有匹配,则返回0
     *
     * @param buffer      要匹配的文本内容,不要更改
     * @param pos         匹配的起始位置,对buffer有效
     * @param bufferStart 缓冲区中的第一个活动索引,对缓冲区有效
     * @param bufferEnd   活动缓冲区的结束索引(排除),对缓冲区有效
     * @return 匹配字符的数量, 如果没有匹配, 则返回0
     */
    public abstract int isMatch(char[] buffer, int pos, int bufferStart, int bufferEnd);

    /**
     * 返回匹配字符的数量,如果没有匹配,则返回0
     *
     * @param pos    匹配的起始位置,对buffer有效
     * @param buffer 要匹配的文本内容,不要更改
     * @return 匹配字符的数量, 如果没有匹配, 则返回0
     */
    public int isMatch(final char[] buffer, final int pos) {
        return isMatch(buffer, pos, 0, buffer.length);
    }

    /**
     * 用于定义用于匹配目的的一组字符.
     */
    static final class CharSetMatcher extends Matchers {

        /**
         * 要匹配的字符集.
         */
        private final char[] chars;

        CharSetMatcher(final char[] chars) {
            super();
            this.chars = chars.clone();
            Arrays.sort(this.chars);
        }

        /**
         * 返回给定字符是否匹配
         *
         * @param buffer      要匹配的文本内容,不要更改
         * @param pos         匹配的起始位置,对buffer有效
         * @param bufferStart 缓冲区中的第一个活动索引,对缓冲区有效
         * @param bufferEnd   活动缓冲区的结束索引,对缓冲区有效
         * @return 匹配字符的数量, 如果没有匹配, 则返回0
         */
        @Override
        public int isMatch(final char[] buffer, final int pos, final int bufferStart, final int bufferEnd) {
            return Arrays.binarySearch(chars, buffer[pos]) >= 0 ? 1 : 0;
        }
    }

    /**
     * 用于定义用于匹配目的的字符.
     */
    static final class CharMatcher extends Matchers {

        /**
         * 要匹配的字符集.
         */
        private final char ch;

        CharMatcher(final char ch) {
            super();
            this.ch = ch;
        }

        /**
         * 返回给定字符是否匹配
         *
         * @param buffer      要匹配的文本内容,不要更改
         * @param pos         匹配的起始位置,对buffer有效
         * @param bufferStart 缓冲区中的第一个活动索引,对缓冲区有效
         * @param bufferEnd   活动缓冲区的结束索引,对缓冲区有效
         * @return 匹配字符的数量, 如果没有匹配, 则返回0
         */
        @Override
        public int isMatch(final char[] buffer, final int pos, final int bufferStart, final int bufferEnd) {
            return ch == buffer[pos] ? 1 : 0;
        }
    }

    /**
     * 用于定义用于匹配目的的一组字符.
     */
    static final class StringMatcher extends Matchers {

        /**
         * 要匹配的字符集.
         */
        private final char[] chars;

        StringMatcher(final String text) {
            super();
            chars = text.toCharArray();
        }

        /**
         * 返回给定文本是否与存储的字符串匹配
         *
         * @param buffer      要匹配的文本内容,不要更改
         * @param pos         匹配的起始位置,对buffer有效
         * @param bufferStart 缓冲区中的第一个活动索引,对缓冲区有效
         * @param bufferEnd   活动缓冲区的结束索引,对缓冲区有效
         * @return 匹配字符的数量, 如果没有匹配, 则返回0
         */
        @Override
        public int isMatch(final char[] buffer, int pos, final int bufferStart, final int bufferEnd) {
            final int len = chars.length;
            if (pos + len > bufferEnd) {
                return 0;
            }
            for (int i = 0; i < chars.length; i++, pos++) {
                if (chars[i] != buffer[pos]) {
                    return 0;
                }
            }
            return len;
        }

        @Override
        public String toString() {
            return super.toString() + Symbol.C_SPACE + Arrays.toString(chars);
        }

    }

    /**
     * 用于不匹配任何字符.
     */
    static final class NoMatcher extends Matchers {

        NoMatcher() {
            super();
        }

        /**
         * 总是返回false
         *
         * @param buffer      要匹配的文本内容,不要更改
         * @param pos         匹配的起始位置,对buffer有效
         * @param bufferStart 缓冲区中的第一个活动索引,对缓冲区有效
         * @param bufferEnd   活动缓冲区的结束索引,对缓冲区有效
         * @return 匹配字符的数量, 如果没有匹配, 则返回0
         */
        @Override
        public int isMatch(final char[] buffer, final int pos, final int bufferStart, final int bufferEnd) {
            return 0;
        }
    }

    /**
     * 用于根据trim()匹配空格.
     */
    static final class TrimMatcher extends Matchers {

        TrimMatcher() {
            super();
        }

        /**
         * 返回给定字符是否匹配
         *
         * @param buffer      要匹配的文本内容,不要更改
         * @param pos         匹配的起始位置,对buffer有效
         * @param bufferStart 缓冲区中的第一个活动索引,对缓冲区有效
         * @param bufferEnd   活动缓冲区的结束索引,对缓冲区有效
         * @return 匹配字符的数量, 如果没有匹配, 则返回0
         */
        @Override
        public int isMatch(final char[] buffer, final int pos, final int bufferStart, final int bufferEnd) {
            return buffer[pos] <= Normal._32 ? 1 : 0;
        }
    }

}
