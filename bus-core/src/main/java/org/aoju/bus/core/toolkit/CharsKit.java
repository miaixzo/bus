/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.lang.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.text.Normalizer;

/**
 * 字符工具类
 * 部分工具来自于Apache
 *
 * @author Kimi Liu
 * @version 6.3.0
 * @since JDK 1.8+
 */
public class CharsKit {

    /**
     * 是否为ASCII字符,ASCII字符位于0~127之间
     *
     * <pre>
     *   CharKit.isAscii('a')  = true
     *   CharKit.isAscii('A')  = true
     *   CharKit.isAscii('3')  = true
     *   CharKit.isAscii('-')  = true
     *   CharKit.isAscii('\n') = true
     *   CharKit.isAscii('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符处
     * @return true表示为ASCII字符, ASCII字符位于0~127之间
     */
    public static boolean isAscii(char ch) {
        return ch < 128;
    }

    /**
     * 是否为可见ASCII字符,可见字符位于32~126之间
     *
     * <pre>
     *   CharKit.isAsciiPrintable('a')  = true
     *   CharKit.isAsciiPrintable('A')  = true
     *   CharKit.isAsciiPrintable('3')  = true
     *   CharKit.isAsciiPrintable('-')  = true
     *   CharKit.isAsciiPrintable('\n') = false
     *   CharKit.isAsciiPrintable('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符处
     * @return true表示为ASCII可见字符, 可见字符位于32~126之间
     */
    public static boolean isAsciiPrintable(char ch) {
        return ch >= Normal._32 && ch < 127;
    }

    /**
     * 是否为ASCII控制符(不可见字符),控制符位于0~31和127
     *
     * <pre>
     *   CharKit.isAsciiControl('a')  = false
     *   CharKit.isAsciiControl('A')  = false
     *   CharKit.isAsciiControl('3')  = false
     *   CharKit.isAsciiControl('-')  = false
     *   CharKit.isAsciiControl('\n') = true
     *   CharKit.isAsciiControl('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为控制符, 控制符位于0~31和127
     */
    public static boolean isAsciiControl(final char ch) {
        return ch < Normal._32 || ch == 127;
    }

    /**
     * 判断是否为字母(包括大写字母和小写字母)
     * 字母包括A~Z和a~z
     *
     * <pre>
     *   CharKit.isLetter('a')  = true
     *   CharKit.isLetter('A')  = true
     *   CharKit.isLetter('3')  = false
     *   CharKit.isLetter('-')  = false
     *   CharKit.isLetter('\n') = false
     *   CharKit.isLetter('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为字母(包括大写字母和小写字母)字母包括A~Z和a~z
     */
    public static boolean isLetter(char ch) {
        return isLetterUpper(ch) || isLetterLower(ch);
    }

    /**
     * 判断是否为大写字母,大写字母包括A~Z
     *
     * <pre>
     *   CharKit.isLetterUpper('a')  = false
     *   CharKit.isLetterUpper('A')  = true
     *   CharKit.isLetterUpper('3')  = false
     *   CharKit.isLetterUpper('-')  = false
     *   CharKit.isLetterUpper('\n') = false
     *   CharKit.isLetterUpper('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为大写字母, 大写字母包括A~Z
     */
    public static boolean isLetterUpper(final char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    /**
     * 检查字符是否为小写字母,小写字母指a~z
     *
     * <pre>
     *   CharKit.isLetterLower('a')  = true
     *   CharKit.isLetterLower('A')  = false
     *   CharKit.isLetterLower('3')  = false
     *   CharKit.isLetterLower('-')  = false
     *   CharKit.isLetterLower('\n') = false
     *   CharKit.isLetterLower('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为小写字母, 小写字母指a~z
     */
    public static boolean isLetterLower(final char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    /**
     * 检查是否为数字字符,数字字符指0~9
     *
     * <pre>
     *   CharKit.isNumber('a')  = false
     *   CharKit.isNumber('A')  = false
     *   CharKit.isNumber('3')  = true
     *   CharKit.isNumber('-')  = false
     *   CharKit.isNumber('\n') = false
     *   CharKit.isNumber('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为数字字符, 数字字符指0~9
     */
    public static boolean isNumber(char ch) {
        return ch >= Symbol.C_ZERO && ch <= Symbol.C_NINE;
    }

    /**
     * 是否为16进制规范的字符,判断是否为如下字符
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
     * 是否为字母或数字,包括A~Z、a~z、0~9
     *
     * <pre>
     *   CharKit.isLetterOrNumber('a')  = true
     *   CharKit.isLetterOrNumber('A')  = true
     *   CharKit.isLetterOrNumber('3')  = true
     *   CharKit.isLetterOrNumber('-')  = false
     *   CharKit.isLetterOrNumber('\n') = false
     *   CharKit.isLetterOrNumber('&copy;') = false
     * </pre>
     *
     * @param ch 被检查的字符
     * @return true表示为字母或数字, 包括A~Z、a~z、0~9
     */
    public static boolean isLetterOrNumber(final char ch) {
        return isLetter(ch) || isNumber(ch);
    }


    /**
     * 给定类名是否为字符类,字符类包括：
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
     * 给定对象对应的类是否为字符类,字符类包括：
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
        return Character.isWhitespace(c)
                || Character.isSpaceChar(c)
                || c == '\ufeff'
                || c == '\u202a'
                || c == '\u0000';
    }

    /**
     * 字符串是否为空白，空白的定义如下：
     * <ol>
     *     <li>{@code null}</li>
     *     <li>空字符串：{@code ""}</li>
     *     <li>空格、全角空格、制表符、换行符，等不可见字符</li>
     * </ol>
     * 例：
     * <ul>
     *     <li>{@code StringKit.isBlank(null)     // true}</li>
     *     <li>{@code StringKit.isBlank("")       // true}</li>
     *     <li>{@code StringKit.isBlank(" \t\n")  // true}</li>
     *     <li>{@code StringKit.isBlank("abc")    // false}</li>
     * </ul>
     * 注意：该方法与 {@link #isEmpty(CharSequence)} 的区别是：
     * 该方法会校验空白字符，且性能相对于 {@link #isEmpty(CharSequence)} 略慢
     * 建议：
     * <ul>
     *     <li>该方法建议仅对于客户端（或第三方接口）传入的参数使用该方法。</li>
     *     <li>需要同时校验多个字符串时，建议采用 {@link #hasBlank(CharSequence...)} 或 {@link #isAllBlank(CharSequence...)}</li>
     * </ul>
     *
     * @param text 被检测的字符串
     * @return 若为空白，则返回 true
     * @see #isEmpty(CharSequence)
     */
    public static boolean isBlank(CharSequence text) {
        int length;

        if ((text == null) || ((length = text.length()) == 0)) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            // 只要有一个非空字符即为非空字符串
            if (false == isBlankChar(text.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 字符串是否为非空白，非空白的定义如下：
     * <ol>
     *     <li>不为 {@code null}</li>
     *     <li>不为空字符串：{@code ""}</li>
     *     <li>不为空格、全角空格、制表符、换行符，等不可见字符</li>
     * </ol>
     * 例：
     * <ul>
     *     <li>{@code StringKit.isNotBlank(null)     // false}</li>
     *     <li>{@code StringKit.isNotBlank("")       // false}</li>
     *     <li>{@code StringKit.isNotBlank(" \t\n")  // false}</li>
     *     <li>{@code StringKit.isNotBlank("abc")    // true}</li>
     * </ul>
     * 注意：该方法与 {@link #isNotEmpty(CharSequence)} 的区别是：
     * 该方法会校验空白字符，且性能相对于 {@link #isNotEmpty(CharSequence)} 略慢
     * 建议：仅对于客户端（或第三方接口）传入的参数使用该方法
     *
     * @param text 被检测的字符串
     * @return 是否为非空
     * @see #isBlank(CharSequence)
     */
    public static boolean isNotBlank(CharSequence text) {
        return false == isBlank(text);
    }

    /**
     * 指定字符串数组中的元素，是否全部为空字符串
     * 如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true
     * 例：
     * <ul>
     *     <li>{@code StringKit.isAllBlank()                  // true}</li>
     *     <li>{@code StringKit.isAllBlank("", null, " ")     // true}</li>
     *     <li>{@code StringKit.isAllBlank("123", " ")        // false}</li>
     *     <li>{@code StringKit.isAllBlank("123", "abc")      // false}</li>
     * </ul>
     * 注意：该方法与 {@link #hasBlank(CharSequence...)} 的区别在于：
     * <ul>
     *     <li>{@link #hasBlank(CharSequence...)}   等价于 {@code isBlank(...) || isBlank(...) || ...}</li>
     *     <li>isAllBlank(CharSequence...)          等价于 {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param texts 字符串列表
     * @return 所有字符串是否为空白
     */
    public static boolean isAllBlank(CharSequence... texts) {
        if (ArrayKit.isEmpty(texts)) {
            return true;
        }

        for (CharSequence text : texts) {
            if (isNotBlank(text)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 字符串是否为空，空的定义如下：
     * <ol>
     *     <li>{@code null}</li>
     *     <li>空字符串：{@code ""}</li>
     * </ol>
     * 例：
     * <ul>
     *     <li>{@code StringKit.isEmpty(null)     // true}</li>
     *     <li>{@code StringKit.isEmpty("")       // true}</li>
     *     <li>{@code StringKit.isEmpty(" \t\n")  // false}</li>
     *     <li>{@code StringKit.isEmpty("abc")    // false}</li>
     * </ul>
     * 注意：该方法与 {@link #isBlank(CharSequence)} 的区别是：该方法不校验空白字符
     * 建议：
     * <ul>
     *     <li>该方法建议用于工具类或任何可以预期的方法参数的校验中</li>
     *     <li>需要同时校验多个字符串时，建议采用 {@link #hasEmpty(CharSequence...)} 或 {@link #isAllEmpty(CharSequence...)}</li>
     * </ul>
     *
     * @param text 被检测的字符串
     * @return 是否为空
     * @see #isBlank(CharSequence)
     */
    public static boolean isEmpty(CharSequence text) {
        return text == null || text.length() == 0;
    }

    /**
     * 字符串是否为非空白，非空白的定义如下：
     * <ol>
     *     <li>不为 {@code null}</li>
     *     <li>不为空字符串：{@code ""}</li>
     * </ol>
     * 例：
     * <ul>
     *     <li>{@code StringKit.isNotEmpty(null)     // false}</li>
     *     <li>{@code StringKit.isNotEmpty("")       // false}</li>
     *     <li>{@code StringKit.isNotEmpty(" \t\n")  // true}</li>
     *     <li>{@code StringKit.isNotEmpty("abc")    // true}</li>
     * </ul>
     * 注意：该方法与 {@link #isNotBlank(CharSequence)} 的区别是：该方法不校验空白字符
     * 建议：该方法建议用于工具类或任何可以预期的方法参数的校验中
     *
     * @param text 被检测的字符串
     * @return 是否为非空
     * @see #isEmpty(CharSequence)
     */
    public static boolean isNotEmpty(CharSequence text) {
        return false == isEmpty(text);
    }

    /**
     * 指定字符串数组中的元素，是否全部为空字符串
     * 如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true
     * 例：
     * <ul>
     *     <li>{@code StringKit.isAllEmpty()                  // true}</li>
     *     <li>{@code StringKit.isAllEmpty("", null)          // true}</li>
     *     <li>{@code StringKit.isAllEmpty("123", "")         // false}</li>
     *     <li>{@code StringKit.isAllEmpty("123", "abc")      // false}</li>
     *     <li>{@code StringKit.isAllEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     * 注意：该方法与 {@link #hasEmpty(CharSequence...)} 的区别在于：
     * <ul>
     *     <li>{@link #hasEmpty(CharSequence...)}   等价于 {@code isEmpty(...) || isEmpty(...) || ...}</li>
     *     <li>isAllEmpty(CharSequence...)          等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param texts 字符串列表
     * @return 所有字符串是否为空白
     */
    public static boolean isAllEmpty(CharSequence... texts) {
        if (ArrayKit.isEmpty(texts)) {
            return true;
        }

        for (CharSequence text : texts) {
            if (isNotEmpty(text)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 指定字符串数组中的元素，是否都不为空字符串
     * 如果指定的字符串数组的长度不为 0，或者所有元素都不是空字符串，则返回 true
     * 例：
     * <ul>
     *     <li>{@code StringKit.isAllNotEmpty()                  // false}</li>
     *     <li>{@code StringKit.isAllNotEmpty("", null)          // false}</li>
     *     <li>{@code StringKit.isAllNotEmpty("123", "")         // false}</li>
     *     <li>{@code StringKit.isAllNotEmpty("123", "abc")      // true}</li>
     *     <li>{@code StringKit.isAllNotEmpty(" ", "\t", "\n")   // true}</li>
     * </ul>
     * 注意：该方法与 {@link #isAllEmpty(CharSequence...)} 的区别在于：
     * <ul>
     *     <li>{@link #isAllEmpty(CharSequence...)}    等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     *     <li>isAllNotEmpty(CharSequence...)          等价于 {@code !isEmpty(...) && !isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param args 字符串数组
     * @return 所有字符串是否都不为为空白
     */
    public static boolean isAllNotEmpty(CharSequence... args) {
        return false == hasEmpty(args);
    }

    /**
     * 是否存都不为{@code null}或空对象或空白符的对象，通过{@link #hasBlank(CharSequence...)} 判断元素
     *
     * @param args 被检查的对象,一个或者多个
     * @return 是否都不为空
     */
    public static boolean isAllNotBlank(CharSequence... args) {
        return false == hasBlank(args);
    }

    /**
     * 检查字符串是否为null、“null”、“undefined”
     *
     * @param text 被检查的字符串
     * @return 是否为null、“null”、“undefined”
     */
    public static boolean isNullOrUndefined(CharSequence text) {
        if (null == text) {
            return true;
        }
        return isNullOrUndefinedStr(text);
    }

    /**
     * 检查字符串是否为null、“”、“null”、“undefined”
     *
     * @param text 被检查的字符串
     * @return 是否为null、“”、“null”、“undefined”
     */
    public static boolean isEmptyOrUndefined(CharSequence text) {
        if (isEmpty(text)) {
            return true;
        }
        return isNullOrUndefinedStr(text);
    }

    /**
     * 检查字符串是否为null、空白串、“null”、“undefined”
     *
     * @param text 被检查的字符串
     * @return 是否为null、空白串、“null”、“undefined”
     */
    public static boolean isBlankOrUndefined(CharSequence text) {
        if (isBlank(text)) {
            return true;
        }
        return isNullOrUndefinedStr(text);
    }

    /**
     * 是否为“null”、“undefined”，不做空指针检查
     *
     * @param text 字符串
     * @return 是否为“null”、“undefined”
     */
    private static boolean isNullOrUndefinedStr(CharSequence text) {
        String strString = text.toString().trim();
        return Normal.NULL.equals(strString) || Normal.UNDEFINED.equals(strString);
    }

    /**
     * 是否不为“null”、“undefined”，不做空指针检查
     *
     * @param str 字符串
     * @return 是否不为“null”、“undefined”，不为“null”、“undefined”返回true，否则false
     */
    private static boolean isNotNullAndNotUndefinedStr(CharSequence str) {
        String strString = str.toString().trim();
        return !Normal.NULL.equals(strString) && !Normal.UNDEFINED.equals(strString);
    }

    /**
     * 指定字符串数组中，是否包含空字符串
     * 如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true
     * 例：
     * <ul>
     *     <li>{@code StringKit.hasBlank()                  // true}</li>
     *     <li>{@code StringKit.hasBlank("", null, " ")     // true}</li>
     *     <li>{@code StringKit.hasBlank("123", " ")        // true}</li>
     *     <li>{@code StringKit.hasBlank("123", "abc")      // false}</li>
     * </ul>
     * 注意：该方法与 {@link #isAllBlank(CharSequence...)} 的区别在于：
     * <ul>
     *     <li>hasBlank(CharSequence...)            等价于 {@code isBlank(...) || isBlank(...) || ...}</li>
     *     <li>{@link #isAllBlank(CharSequence...)} 等价于 {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param texts 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasBlank(CharSequence... texts) {
        if (ArrayKit.isEmpty(texts)) {
            return true;
        }

        for (CharSequence text : texts) {
            if (isBlank(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否包含空字符串
     * 如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true
     * 例：
     * <ul>
     *     <li>{@code StringKit.hasEmpty()                  // true}</li>
     *     <li>{@code StringKit.hasEmpty("", null)          // true}</li>
     *     <li>{@code StringKit.hasEmpty("123", "")         // true}</li>
     *     <li>{@code StringKit.hasEmpty("123", "abc")      // false}</li>
     *     <li>{@code StringKit.hasEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     * 注意：该方法与 {@link #isAllEmpty(CharSequence...)} 的区别在于：
     * <ul>
     *     <li>hasEmpty(CharSequence...)            等价于 {@code isEmpty(...) || isEmpty(...) || ...}</li>
     *     <li>{@link #isAllEmpty(CharSequence...)} 等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param texts 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasEmpty(CharSequence... texts) {
        if (ArrayKit.isEmpty(texts)) {
            return true;
        }

        for (CharSequence text : texts) {
            if (isEmpty(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为emoji表情符
     *
     * @param c 字符
     * @return 是否为emoji
     */
    public static boolean isEmoji(char c) {
        return false == ((c == 0x0) ||
                (c == 0x9) ||
                (c == 0xA) ||
                (c == 0xD) ||
                ((c >= 0x20) && (c <= 0xD7FF)) ||
                ((c >= 0xE000) && (c <= 0xFFFD)) ||
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
     * 统计指定内容中包含指定字符串的数量<br>
     * 参数为 {@code null} 或者 "" 返回 {@code 0}.
     *
     * <pre>
     * count(null, *)       = 0
     * count("", *)         = 0
     * count("abba", null)  = 0
     * count("abba", "")    = 0
     * count("abba", "a")   = 2
     * count("abba", "ab")  = 1
     * count("abba", "xxx") = 0
     * </pre>
     *
     * @param content      被查找的字符串
     * @param strForSearch 需要查找的字符串
     * @return 查找到的个数
     */
    public static int count(CharSequence content, CharSequence strForSearch) {
        if (hasEmpty(content, strForSearch) || strForSearch.length() > content.length()) {
            return 0;
        }

        int count = 0;
        int idx = 0;
        final String content2 = content.toString();
        final String strForSearch2 = strForSearch.toString();
        while ((idx = content2.indexOf(strForSearch2, idx)) > -1) {
            count++;
            idx += strForSearch.length();
        }
        return count;
    }

    /**
     * 统计指定内容中包含指定字符的数量
     *
     * @param content       内容
     * @param charForSearch 被统计的字符
     * @return 包含数量
     */
    public static int count(CharSequence content, char charForSearch) {
        int count = 0;
        if (isEmpty(content)) {
            return 0;
        }
        int contentLength = content.length();
        for (int i = 0; i < contentLength; i++) {
            if (charForSearch == content.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 字符转为字符串
     * 如果为ASCII字符,使用缓存
     *
     * @param c 字符
     * @return 字符串
     */
    public static String toString(char c) {
        String[] CACHE = new String[ Normal._128];
        for (char i = 0; i <  Normal._128; i++) {
            CACHE[i] = String.valueOf(i);
        }
        return c <  Normal._128 ? CACHE[c] : String.valueOf(c);
    }

    /**
     * 是否为Windows或者Linux(Unix)文件分隔符
     * Windows平台下分隔符为\,Linux(Unix)为/
     *
     * @param c 字符
     * @return 是否为Windows或者Linux(Unix)文件分隔符
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

    public static char[] getChars(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes).flip();
        CharBuffer cb = Charset.UTF_8.decode(bb);
        return cb.array();
    }

    /**
     * byte转car
     *
     * @param b 字节信息
     * @return char
     */
    public static char byteToChar(byte[] b) {
        int hi = (b[0] & 0xFF) << 8;
        int lo = b[1] & 0xFF;
        return (char) (hi | lo);
    }

    /**
     * 将字母、数字转换为带圈的字符：
     * <pre>
     *     '1' -》 '①'
     *     'A' -》 'Ⓐ'
     *     'a' -》 'ⓐ'
     * </pre>
     * 获取带圈数字 /封闭式字母数字 ，从1-20,超过1-20报错
     * 0	1	2	3	4	5	6	7	8	9	A	B	C	D	E	F
     * U+246x	①	②	③	④	⑤	⑥	⑦	⑧	⑨	⑩	⑪	⑫	⑬	⑭	⑮	⑯
     * U+247x	⑰	⑱	⑲	⑳	⑴	⑵	⑶	⑷	⑸	⑹	⑺	⑻	⑼	⑽	⑾	⑿
     * U+248x	⒀	⒁	⒂	⒃	⒄	⒅	⒆	⒇	⒈	⒉	⒊	⒋	⒌	⒍	⒎	⒏
     * U+249x	⒐	⒑	⒒	⒓	⒔	⒕	⒖	⒗	⒘	⒙	⒚	⒛	⒜	⒝	⒞	⒟
     * U+24Ax	⒠	⒡	⒢	⒣	⒤	⒥	⒦	⒧	⒨	⒩	⒪	⒫	⒬	⒭	⒮	⒯
     * U+24Bx	⒰	⒱	⒲	⒳	⒴	⒵	Ⓐ	Ⓑ	Ⓒ	Ⓓ	Ⓔ	Ⓕ	Ⓖ	Ⓗ	Ⓘ	Ⓙ
     * U+24Cx	Ⓚ	Ⓛ	Ⓜ	Ⓝ	Ⓞ	Ⓟ	Ⓠ	Ⓡ	Ⓢ	Ⓣ	Ⓤ	Ⓥ	Ⓦ	Ⓧ	Ⓨ	Ⓩ
     * U+24Dx	ⓐ	ⓑ	ⓒ	ⓓ	ⓔ	ⓕ	ⓖ	ⓗ	ⓘ	ⓙ	ⓚ	ⓛ	ⓜ	ⓝ	ⓞ	ⓟ
     * U+24Ex	ⓠ	ⓡ	ⓢ	ⓣ	ⓤ	ⓥ	ⓦ	ⓧ	ⓨ	ⓩ	⓪	⓫	⓬	⓭	⓮	⓯
     * U+24Fx	⓰	⓱	⓲	⓳	⓴	⓵	⓶	⓷	⓸	⓹	⓺	⓻	⓼	⓽	⓾	⓿
     *
     * @param c 被转换的字符，如果字符不支持转换，返回原字符
     * @return 转换后的字符
     */
    public static char toCloseChar(char c) {
        int result = c;
        if (c >= '1' && c <= '9') {
            result = '①' + c - '1';
        } else if (c >= 'A' && c <= 'Z') {
            result = 'Ⓐ' + c - 'A';
        } else if (c >= 'a' && c <= 'z') {
            result = 'ⓐ' + c - 'a';
        }
        return (char) result;
    }

    /**
     * 封闭式字符，英文：Enclosed Alphanumerics
     * 将[1-20]数字转换为带圈的字符：
     * <pre>
     *     1 -》 '①'
     *     12 -》 '⑫'
     *     20 -》 '⑳'
     * </pre>
     *
     * @param number 被转换的数字
     * @return 转换后的字符
     */
    public static char toCloseByNumber(int number) {
        if (number > 20) {
            throw new IllegalArgumentException("Number must be [1-20]");
        }
        return (char) ('①' + number - 1);
    }

    /**
     * 字符串是否以(数字)开始
     *
     * @param text 字符串
     * @return 是否数字开始
     */
    public static boolean startWithNumber(CharSequence text) {
        return isNotBlank(text) && RegEx.NUMBERS.matcher(text.subSequence(0, 1)).find();
    }

    /**
     * 字符串是否以(英文字母 、数字和下划线)开始
     *
     * @param text 字符串
     * @return 是否英文字母 、数字和下划线开始
     */
    public static boolean startWithGeneral(CharSequence text) {
        return isNotBlank(text) && RegEx.GENERAL.matcher(text.subSequence(0, 1)).find();
    }

    /**
     * 字符串是否以(字母)开始
     *
     * @param text 字符串
     * @return 是否字母开始
     */
    public static boolean startWithWord(CharSequence text) {
        return isNotBlank(text) && RegEx.WORD.matcher(text.subSequence(0, 1)).find();
    }

    /**
     * 字符串是否以(中文汉字)开始
     *
     * @param text 字符串
     * @return 是否中文汉字开始
     */
    public static boolean startWithChinese(CharSequence text) {
        return isNotBlank(text) && RegEx.CHINESES.matcher(text.subSequence(0, 1)).find();
    }

    /**
     * 检查给定字符串的所有字符是否都一样
     *
     * @param str 字符出啊
     * @return 给定字符串的所有字符是否都一样
     */
    public static boolean isCharEquals(CharSequence str) {
        Assert.notEmpty(str, "Str to check must be not empty!");
        return count(str, str.charAt(0)) == str.length();
    }

    /**
     * 对字符串归一化处理，如 "Á" 可以使用 "u00C1"或 "u0041u0301"表示，实际测试中两个字符串并不equals
     * 因此使用此方法归一为一种表示形式，默认按照W3C通常建议的，在NFC中交换文本。
     *
     * @param str 归一化的字符串
     * @return 归一化后的字符串
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static String normalize(CharSequence str) {
        return Normalizer.normalize(str, Normalizer.Form.NFC);
    }

}
