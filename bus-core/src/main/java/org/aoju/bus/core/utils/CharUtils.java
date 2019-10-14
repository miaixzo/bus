/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.utils;

import org.aoju.bus.core.consts.Symbol;

/**
 * 字符工具类
 * 部分工具来自于Apache
 *
 * @author Kimi Liu
 * @version 5.0.1
 * @since JDK 1.8+
 */
public class CharUtils {

    /**
     * 是否为ASCII字符，ASCII字符位于0~127之间
     *
     * <pre>
     *   CharUtils.isAscii('a')  = true
     *   CharUtils.isAscii('A')  = true
     *   CharUtils.isAscii('3')  = true
     *   CharUtils.isAscii('-')  = true
     *   CharUtils.isAscii('\n') = true
     *   CharUtils.isAscii('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符处
     * @return true表示为ASCII字符，ASCII字符位于0~127之间
     */
    public static boolean isAscii(char ch) {
        return ch < 128;
    }

    /**
     * 是否为可见ASCII字符，可见字符位于32~126之间
     *
     * <pre>
     *   CharUtils.isAsciiPrintable('a')  = true
     *   CharUtils.isAsciiPrintable('A')  = true
     *   CharUtils.isAsciiPrintable('3')  = true
     *   CharUtils.isAsciiPrintable('-')  = true
     *   CharUtils.isAsciiPrintable('\n') = false
     *   CharUtils.isAsciiPrintable('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符处
     * @return true表示为ASCII可见字符，可见字符位于32~126之间
     */
    public static boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }

    /**
     * 是否为ASCII控制符（不可见字符），控制符位于0~31和127
     *
     * <pre>
     *   CharUtils.isAsciiControl('a')  = false
     *   CharUtils.isAsciiControl('A')  = false
     *   CharUtils.isAsciiControl('3')  = false
     *   CharUtils.isAsciiControl('-')  = false
     *   CharUtils.isAsciiControl('\n') = true
     *   CharUtils.isAsciiControl('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为控制符，控制符位于0~31和127
     */
    public static boolean isAsciiControl(final char ch) {
        return ch < 32 || ch == 127;
    }

    /**
     * 判断是否为字母（包括大写字母和小写字母）
     * 字母包括A~Z和a~z
     *
     * <pre>
     *   CharUtils.isLetter('a')  = true
     *   CharUtils.isLetter('A')  = true
     *   CharUtils.isLetter('3')  = false
     *   CharUtils.isLetter('-')  = false
     *   CharUtils.isLetter('\n') = false
     *   CharUtils.isLetter('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为字母（包括大写字母和小写字母）字母包括A~Z和a~z
     */
    public static boolean isLetter(char ch) {
        return isLetterUpper(ch) || isLetterLower(ch);
    }

    /**
     * <p>
     * 判断是否为大写字母，大写字母包括A~Z
     * </p>
     *
     * <pre>
     *   CharUtils.isLetterUpper('a')  = false
     *   CharUtils.isLetterUpper('A')  = true
     *   CharUtils.isLetterUpper('3')  = false
     *   CharUtils.isLetterUpper('-')  = false
     *   CharUtils.isLetterUpper('\n') = false
     *   CharUtils.isLetterUpper('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为大写字母，大写字母包括A~Z
     */
    public static boolean isLetterUpper(final char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    /**
     * <p>
     * 检查字符是否为小写字母，小写字母指a~z
     * </p>
     *
     * <pre>
     *   CharUtils.isLetterLower('a')  = true
     *   CharUtils.isLetterLower('A')  = false
     *   CharUtils.isLetterLower('3')  = false
     *   CharUtils.isLetterLower('-')  = false
     *   CharUtils.isLetterLower('\n') = false
     *   CharUtils.isLetterLower('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为小写字母，小写字母指a~z
     */
    public static boolean isLetterLower(final char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    /**
     * <p>
     * 检查是否为数字字符，数字字符指0~9
     * </p>
     *
     * <pre>
     *   CharUtils.isNumber('a')  = false
     *   CharUtils.isNumber('A')  = false
     *   CharUtils.isNumber('3')  = true
     *   CharUtils.isNumber('-')  = false
     *   CharUtils.isNumber('\n') = false
     *   CharUtils.isNumber('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为数字字符，数字字符指0~9
     */
    public static boolean isNumber(char ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * 是否为16进制规范的字符，判断是否为如下字符
     * <pre>
     * 1. 0~9
     * 2. a~f
     * 4. A~F
     * </pre>
     *
     * @param c 字符
     * @return 是否为16进制规范的字符
     */
    public static boolean isHexChar(char c) {
        return isNumber(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    /**
     * 是否为字符或数字，包括A~Z、a~z、0~9
     *
     * <pre>
     *   CharUtils.isLetterOrNumber('a')  = true
     *   CharUtils.isLetterOrNumber('A')  = true
     *   CharUtils.isLetterOrNumber('3')  = true
     *   CharUtils.isLetterOrNumber('-')  = false
     *   CharUtils.isLetterOrNumber('\n') = false
     *   CharUtils.isLetterOrNumber('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为字符或数字，包括A~Z、a~z、0~9
     */
    public static boolean isLetterOrNumber(final char ch) {
        return isLetter(ch) || isNumber(ch);
    }


    /**
     * 给定类名是否为字符类，字符类包括：
     *
     * <pre>
     * Character.class
     * char.class
     * </pre>
     *
     * @param clazz 被检查的类
     * @return true表示为字符类
     */
    public static boolean isCharClass(Class<?> clazz) {
        return clazz == Character.class || clazz == char.class;
    }

    /**
     * 给定对象对应的类是否为字符类，字符类包括：
     *
     * <pre>
     * Character.class
     * char.class
     * </pre>
     *
     * @param value 被检查的对象
     * @return true表示为字符类
     */
    public static boolean isChar(Object value) {
        return value instanceof Character || value.getClass() == char.class;
    }

    /**
     * 是否空白符
     * 空白符包括空格、制表符、全角空格和不间断空格
     *
     * @param c 字符
     * @return 是否空白符
     * @see Character#isWhitespace(int)
     * @see Character#isSpaceChar(int)
     */
    public static boolean isBlankChar(char c) {
        return isBlankChar((int) c);
    }

    /**
     * 是否空白符
     * 空白符包括空格、制表符、全角空格和不间断空格
     *
     * @param c 字符
     * @return 是否空白符
     * @see Character#isWhitespace(int)
     * @see Character#isSpaceChar(int)
     */
    public static boolean isBlankChar(int c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c) || c == '\ufeff' || c == '\u202a';
    }

    /**
     * 判断是否为emoji表情符
     *
     * @param c 字符
     * @return 是否为emoji
     */
    public static boolean isEmoji(char c) {
        return false == ((c == 0x0) || //
                (c == 0x9) || //
                (c == 0xA) || //
                (c == 0xD) || //
                ((c >= 0x20) && (c <= 0xD7FF)) || //
                ((c >= 0xE000) && (c <= 0xFFFD)) || //
                ((c >= 0x10000) && (c <= 0x10FFFF)));
    }

    /**
     * 比较两个字符是否相同
     *
     * @param c1         字符1
     * @param c2         字符2
     * @param ignoreCase 是否忽略大小写
     * @return 是否相同
     */
    public static boolean equals(char c1, char c2, boolean ignoreCase) {
        if (ignoreCase) {
            return Character.toLowerCase(c1) == Character.toLowerCase(c2);
        }
        return c1 == c2;
    }

    /**
     * 字符转为字符串
     * 如果为ASCII字符，使用缓存
     *
     * @param c 字符
     * @return 字符串
     */
    public static String toString(char c) {
        String[] CACHE = new String[128];
        return c < 128 ? CACHE[c] : String.valueOf(c);
    }

    /**
     * 是否为Windows或者Linux（Unix）文件分隔符
     * Windows平台下分隔符为\，Linux（Unix）为/
     *
     * @param c 字符
     * @return 是否为Windows或者Linux（Unix）文件分隔符
     */
    public static boolean isFileSeparator(char c) {
        return Symbol.C_SLASH == c || Symbol.C_BACKSLASH == c;
    }

    /**
     * 对两个{@code char}值进行数值比较
     *
     * @param x {@code char}
     * @param y {@code char}
     *          如果{@code x == y}返回值{@code 0};
     *          如果{@code x < y}值小于{@code 0};和
     *          如果{@code x > y}
     * @return the int
     */
    public static int compare(final char x, final char y) {
        return x - y;
    }

}
