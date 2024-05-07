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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.function.XFunction;
import org.miaixz.bus.core.text.ASCIICache;
import org.miaixz.bus.core.text.NamingCase;
import org.miaixz.bus.core.text.TextFormatter;
import org.miaixz.bus.core.text.TextSplitter;
import org.miaixz.bus.core.text.finder.CharFinder;
import org.miaixz.bus.core.text.finder.StringFinder;

import java.lang.System;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * 字符工具类
 * 部分工具来自于Apache
 *
 * @author Kimi Liu
 * @since Java 17+
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
     * @param args 被检查的字符处
     * @return true表示为ASCII字符, ASCII字符位于0~127之间
     */
    public static boolean isAscii(char args) {
        return args < 128;
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
     * @param args 被检查的字符处
     * @return true表示为ASCII可见字符, 可见字符位于32~126之间
     */
    public static boolean isAsciiPrintable(char args) {
        return args >= Normal._32 && args < 127;
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
     * @param args 被检查的字符
     * @return true表示为控制符, 控制符位于0~31和127
     */
    public static boolean isAsciiControl(final char args) {
        return args < Normal._32 || args == 127;
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
     * @param args 被检查的字符
     * @return true表示为字母(包括大写字母和小写字母)字母包括A~Z和a~z
     */
    public static boolean isLetter(char args) {
        return isLetterUpper(args) || isLetterLower(args);
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
     * @param args 被检查的字符
     * @return true表示为大写字母, 大写字母包括A~Z
     */
    public static boolean isLetterUpper(final char args) {
        return args >= 'A' && args <= 'Z';
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
     * @param args 被检查的字符
     * @return true表示为小写字母, 小写字母指a~z
     */
    public static boolean isLetterLower(final char args) {
        return args >= 'a' && args <= 'z';
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
     * @param args 被检查的字符
     * @return true表示为数字字符, 数字字符指0~9
     */
    public static boolean isNumber(char args) {
        return args >= Symbol.C_ZERO && args <= Symbol.C_NINE;
    }

    /**
     * 是否为16进制规范的字符,判断是否为如下字符
     * <pre>
     * 1. 0~9
     * 2. a~f
     * 4. A~F
     * </pre>
     *
     * @param args 字符
     * @return 是否为16进制规范的字符
     */
    public static boolean isHexChar(char args) {
        return isNumber(args) || (args >= 'a' && args <= 'f') || (args >= 'A' && args <= 'F');
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
     * @param args 被检查的字符
     * @return true表示为字母或数字, 包括A~Z、a~z、0~9
     */
    public static boolean isLetterOrNumber(final char args) {
        return isLetter(args) || isNumber(args);
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
     * @param args 被检查的对象
     * @return true表示为字符类
     */
    public static boolean isChar(Object args) {
        return args instanceof Character || args.getClass() == char.class;
    }

    /**
     * 是否空白符
     * 空白符包括空格、制表符、全角空格和不间断空格
     *
     * @param args 字符
     * @return 是否空白符
     * @see Character#isWhitespace(int)
     * @see Character#isSpaceChar(int)
     */
    public static boolean isBlankChar(final char args) {
        return isBlankChar((int) args);
    }

    /**
     * 是否空白符
     * 空白符包括空格、制表符、全角空格和不间断空格
     *
     * @param args 字符
     * @return 是否空白符
     * @see Character#isWhitespace(int)
     * @see Character#isSpaceChar(int)
     */
    public static boolean isBlankChar(final int args) {
        return Character.isWhitespace(args)
                || Character.isSpaceChar(args)
                || args == '\ufeff'
                || args == '\u202a'
                || args == '\u0000'
                || args == '\u3164'
                || args == '\u2800'
                || args == '\u180e';
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
     *     <li>{@code isBlank(null)     // true}</li>
     *     <li>{@code isBlank("")       // true}</li>
     *     <li>{@code isBlank(" \t\n")  // true}</li>
     *     <li>{@code isBlank("abc")    // false}</li>
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
        final int length;

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
     *     <li>{@code isNotBlank(null)     // false}</li>
     *     <li>{@code isNotBlank("")       // false}</li>
     *     <li>{@code isNotBlank(" \t\n")  // false}</li>
     *     <li>{@code isNotBlank("abc")    // true}</li>
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
     * 检查是否没有字符序列为空("")、空字符或仅为空格
     *
     * <pre>
     * isNoneBlank(null)             = false
     * isNoneBlank(null, "foo")      = false
     * isNoneBlank(null, null)       = false
     * isNoneBlank("", "bar")        = false
     * isNoneBlank("bob", "")        = false
     * isNoneBlank("  bob  ", null)  = false
     * isNoneBlank(" ", "bar")       = false
     * isNoneBlank(new String[] {})  = true
     * isNoneBlank(new String[]{""}) = false
     * isNoneBlank("foo", "bar")     = true
     * </pre>
     *
     * @param args 要检查的字符串可以为null或空
     * @return 所有字符序列都不为空或null或仅为空格
     */
    public static boolean isNoneBlank(final CharSequence... args) {
        return !isAnyBlank(args);
    }

    /**
     * 检查任何一个字符序列是否为空("")，或为空，或仅为空白
     *
     * <pre>
     * isAnyBlank((String) null)    = true
     * isAnyBlank((String[]) null)  = false
     * isAnyBlank(null, "foo")      = true
     * isAnyBlank(null, null)       = true
     * isAnyBlank("", "bar")        = true
     * isAnyBlank("bob", "")        = true
     * isAnyBlank("  bob  ", null)  = true
     * isAnyBlank(" ", "bar")       = true
     * isAnyBlank(new String[] {})  = false
     * isAnyBlank(new String[]{""}) = true
     * isAnyBlank("foo", "bar")     = false
     * </pre>
     *
     * @param args 要检查的字符序列可以为空或空
     * @return 如果任何一个字符序列是空的，或者是空的，或者只有空白
     */
    public static boolean isAnyBlank(final CharSequence... args) {
        if (ArrayKit.isEmpty(args)) {
            return false;
        }
        for (final CharSequence text : args) {
            if (isBlank(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 指定字符串数组中的元素，是否全部为空字符串
     * 如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true
     * 例：
     * <ul>
     *     <li>{@code isAllBlank()                  // true}</li>
     *     <li>{@code isAllBlank("", null, " ")     // true}</li>
     *     <li>{@code isAllBlank("123", " ")        // false}</li>
     *     <li>{@code isAllBlank("123", "abc")      // false}</li>
     * </ul>
     * 注意：该方法与 {@link #hasBlank(CharSequence...)} 的区别在于：
     * <ul>
     *     <li>{@link #hasBlank(CharSequence...)}   等价于 {@code isBlank(...) || isBlank(...) || ...}</li>
     *     <li>isAllBlank(CharSequence...)          等价于 {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param args 字符串列表
     * @return 所有字符串是否为空白
     */
    public static boolean isAllBlank(CharSequence... args) {
        if (ArrayKit.isEmpty(args)) {
            return true;
        }

        for (CharSequence text : args) {
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
     *     <li>{@code isEmpty(null)     // true}</li>
     *     <li>{@code isEmpty("")       // true}</li>
     *     <li>{@code isEmpty(" \t\n")  // false}</li>
     *     <li>{@code isEmpty("abc")    // false}</li>
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
        return text == null || text.length() == 0 || text.isEmpty();
    }

    /**
     * 字符串是否为非空白，非空白的定义如下：
     * <ol>
     *     <li>不为 {@code null}</li>
     *     <li>不为空字符串：{@code ""}</li>
     * </ol>
     * 例：
     * <ul>
     *     <li>{@code isNotEmpty(null)     // false}</li>
     *     <li>{@code isNotEmpty("")       // false}</li>
     *     <li>{@code isNotEmpty(" \t\n")  // true}</li>
     *     <li>{@code isNotEmpty("abc")    // true}</li>
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
     *     <li>{@code isAllEmpty()                  // true}</li>
     *     <li>{@code isAllEmpty("", null)          // true}</li>
     *     <li>{@code isAllEmpty("123", "")         // false}</li>
     *     <li>{@code isAllEmpty("123", "abc")      // false}</li>
     *     <li>{@code isAllEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     * 注意：该方法与 {@link #hasEmpty(CharSequence...)} 的区别在于：
     * <ul>
     *     <li>{@link #hasEmpty(CharSequence...)}   等价于 {@code isEmpty(...) || isEmpty(...) || ...}</li>
     *     <li>isAllEmpty(CharSequence...)          等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param args 字符串列表
     * @return 所有字符串是否为空白
     */
    public static boolean isAllEmpty(CharSequence... args) {
        if (ArrayKit.isEmpty(args)) {
            return true;
        }

        for (CharSequence text : args) {
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
     *     <li>{@code isAllNotEmpty()                  // false}</li>
     *     <li>{@code isAllNotEmpty("", null)          // false}</li>
     *     <li>{@code isAllNotEmpty("123", "")         // false}</li>
     *     <li>{@code isAllNotEmpty("123", "abc")      // true}</li>
     *     <li>{@code isAllNotEmpty(" ", "\t", "\n")   // true}</li>
     * </ul>
     * 注意：该方法与 {@link #isAllEmpty(CharSequence...)} 的区别在于：
     * <ul>
     *     <li>{@link #isAllEmpty(CharSequence...)}    等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     *     <li>isAllNotEmpty(CharSequence...)          等价于 {@code !isEmpty(...) && !isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param text 字符串数组
     * @return 所有字符串是否都不为为空白
     */
    public static boolean isAllNotEmpty(CharSequence... text) {
        return false == hasEmpty(text);
    }

    /**
     * 是否存都不为{@code null}或空对象或空白符的对象，通过{@link #hasBlank(CharSequence...)} 判断元素
     *
     * @param text 被检查的对象,一个或者多个
     * @return 是否都不为空
     */
    public static boolean isAllNotBlank(CharSequence... text) {
        return false == hasBlank(text);
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
        return isNullOrUndefinedString(text);
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
        return isNullOrUndefinedString(text);
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
        return isNullOrUndefinedString(text);
    }

    /**
     * 是否为“null”、“undefined”，不做空指针检查
     *
     * @param text 字符串
     * @return 是否为“null”、“undefined”
     */
    private static boolean isNullOrUndefinedString(CharSequence text) {
        String strString = text.toString().trim();
        return Normal.NULL.equals(strString) || Normal.UNDEFINED.equals(strString);
    }

    /**
     * 是否不为“null”、“undefined”，不做空指针检查
     *
     * @param text 字符串
     * @return 是否不为“null”、“undefined”，不为“null”、“undefined”返回true，否则false
     */
    private static boolean isNotNullAndNotUndefinedString(CharSequence text) {
        String strString = text.toString().trim();
        return !Normal.NULL.equals(strString) && !Normal.UNDEFINED.equals(strString);
    }

    /**
     * 指定字符串数组中，是否包含空字符串
     * 如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true
     * 例：
     * <ul>
     *     <li>{@code hasBlank()                  // true}</li>
     *     <li>{@code hasBlank("", null, " ")     // true}</li>
     *     <li>{@code hasBlank("123", " ")        // true}</li>
     *     <li>{@code hasBlank("123", "abc")      // false}</li>
     * </ul>
     * 注意：该方法与 {@link #isAllBlank(CharSequence...)} 的区别在于：
     * <ul>
     *     <li>hasBlank(CharSequence...)            等价于 {@code isBlank(...) || isBlank(...) || ...}</li>
     *     <li>{@link #isAllBlank(CharSequence...)} 等价于 {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param args 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasBlank(CharSequence... args) {
        if (ArrayKit.isEmpty(args)) {
            return true;
        }

        for (CharSequence text : args) {
            if (isBlank(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 字符串的每一个字符是否都与定义的匹配器匹配
     *
     * @param value   字符串
     * @param matcher 匹配器
     * @return 是否全部匹配
     */
    public static boolean isAllCharMatch(final CharSequence value, final Predicate<Character> matcher) {
        if (isBlank(value)) {
            return false;
        }
        int len = value.length();
        for (int i = 0; i < len; i++) {
            if (false == matcher.test(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 当给定字符串为null时，转换为Empty
     *
     * @param text 被检查的字符串
     * @return 原字符串或者空串
     * @see #nullToEmpty(CharSequence)
     */
    public static String emptyIfNull(CharSequence text) {
        return ObjectKit.defaultIfNull(text, Normal.EMPTY).toString();
    }

    /**
     * 当给定字符串为空字符串时，转换为<code>null</code>
     *
     * @param text 被转换的字符串
     * @return 转换后的字符串
     */
    public static String emptyToNull(CharSequence text) {
        return isEmpty(text) ? null : text.toString();
    }

    /**
     * 是否包含空字符串
     * 如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true
     * 例：
     * <ul>
     *     <li>{@code hasEmpty()                  // true}</li>
     *     <li>{@code hasEmpty("", null)          // true}</li>
     *     <li>{@code hasEmpty("123", "")         // true}</li>
     *     <li>{@code hasEmpty("123", "abc")      // false}</li>
     *     <li>{@code hasEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     * 注意：该方法与 {@link #isAllEmpty(CharSequence...)} 的区别在于：
     * <ul>
     *     <li>hasEmpty(CharSequence...)            等价于 {@code isEmpty(...) || isEmpty(...) || ...}</li>
     *     <li>{@link #isAllEmpty(CharSequence...)} 等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param args 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasEmpty(CharSequence... args) {
        if (ArrayKit.isEmpty(args)) {
            return true;
        }

        for (CharSequence text : args) {
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
     * 统计指定内容中包含指定字符串的数量
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
     * @param text 字符出啊
     * @return 给定字符串的所有字符是否都一样
     */
    public static boolean isCharEquals(CharSequence text) {
        Assert.notEmpty(text, "text to check must be not empty!");
        return count(text, text.charAt(0)) == text.length();
    }

    /**
     * 对字符串归一化处理，如 "Á" 可以使用 "u00C1"或 "u0041u0301"表示，实际测试中两个字符串并不equals
     * 因此使用此方法归一为一种表示形式，默认按照W3C通常建议的，在NFC中交换文本。
     *
     * @param text 归一化的字符串
     * @return 归一化后的字符串
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static String normalize(CharSequence text) {
        return Normalizer.normalize(text, Normalizer.Form.NFC);
    }

    /**
     * 在给定字符串末尾填充指定字符，以达到给定长度
     * 如果字符串本身的长度大于等于length，返回原字符串
     *
     * @param text      字符串
     * @param fixedChar 补充的字符
     * @param length    补充到的长度
     * @return 补充后的字符串
     */
    public static String fixLength(CharSequence text, char fixedChar, int length) {
        final int fixedLength = length - text.length();
        if (fixedLength <= 0) {
            return text.toString();
        }
        return text + repeat(fixedChar, fixedLength);
    }

    /**
     * 字符串去空格
     *
     * @param text 原始字符串
     * @return 返回字符串
     */
    public static String trim(CharSequence text) {
        return (null == text) ? null : trim(text, 0);
    }

    /**
     * 除去字符串头尾部的空白符,如果字符串是null,依然返回null
     *
     * @param text 要处理的字符串
     * @param mode -1去除开始位置,0全部去除, 1去除结束位置
     * @return 除去指定字符后的的字符串, 如果原字串为null, 则返回null
     */
    public static String trim(CharSequence text, int mode) {
        if (null == text) {
            return null;
        }

        int length = text.length();
        int start = 0;
        int end = length;

        // 扫描字符串头部
        if (mode <= 0) {
            while ((start < end) && (CharsKit.isBlankChar(text.charAt(start)))) {
                start++;
            }
        }

        // 扫描字符串尾部
        if (mode >= 0) {
            while ((start < end) && (CharsKit.isBlankChar(text.charAt(end - 1)))) {
                end--;
            }
        }

        if ((start > 0) || (end < length)) {
            return text.toString().substring(start, end);
        }

        return text.toString();
    }

    /**
     * 按照断言，除去字符串头尾部的断言为真的字符，如果字符串是{@code null}，依然返回{@code null}。
     *
     * @param text      要处理的字符串
     * @param mode      {@code -1}表示trimStart，{@code 0}表示trim全部， {@code 1}表示trimEnd
     * @param predicate 断言是否过掉字符，返回{@code true}表述过滤掉，{@code false}表示不过滤
     * @return 除去指定字符后的的字符串，如果原字串为{@code null}，则返回{@code null}
     */
    public static String trim(CharSequence text, int mode, Predicate<Character> predicate) {
        String result;
        if (text == null) {
            result = null;
        } else {
            int length = text.length();
            int start = 0;
            int end = length;// 扫描字符串头部
            if (mode <= 0) {
                while ((start < end) && (predicate.test(text.charAt(start)))) {
                    start++;
                }
            }// 扫描字符串尾部
            if (mode >= 0) {
                while ((start < end) && (predicate.test(text.charAt(end - 1)))) {
                    end--;
                }
            }
            if ((start > 0) || (end < length)) {
                result = text.toString().substring(start, end);
            } else {
                result = text.toString();
            }
        }
        return result;
    }

    /**
     * 删除字符串两端的空白字符(char &lt;= 32)，如果字符串在修剪后为空("")
     * 或者如果字符串为{@code null}，则返回{@code null}
     * <pre>
     * trimToNull(null)           = null
     * trimToNull("")             = null
     * trimToNull("     ")        = null
     * trimToNull("abc")          = "abc"
     * trimToEmpty("    abc    ") = "abc"
     * </pre>
     *
     * @param text 字符串
     * @return 去除两边空白符后的字符串, 如果为空返回null
     */
    public static String trimToNull(CharSequence text) {
        final String trimStr = trim(text);
        return Normal.EMPTY.equals(trimStr) ? null : trimStr;
    }

    /**
     * 去掉首部指定长度的字符串并将剩余字符串首字母小写
     * 例如：text=setName, preLength=3 =  return name
     *
     * @param text      被处理的字符串
     * @param preLength 去掉的长度
     * @return 处理后的字符串, 不符合规范返回null
     */
    public static String removePreAndLowerFirst(CharSequence text, int preLength) {
        if (null == text) {
            return null;
        }
        if (text.length() > preLength) {
            char first = Character.toLowerCase(text.charAt(preLength));
            if (text.length() > preLength + 1) {
                return first + text.toString().substring(preLength + 1);
            }
            return String.valueOf(first);
        } else {
            return text.toString();
        }
    }

    /**
     * 去掉首部指定长度的字符串并将剩余字符串首字母小写
     * 例如：text=setName, prefix=set =  return name
     *
     * @param text   被处理的字符串
     * @param prefix 前缀
     * @return 处理后的字符串, 不符合规范返回null
     */
    public static String removePreAndLowerFirst(CharSequence text, CharSequence prefix) {
        return lowerFirst(removePrefix(text, prefix));
    }


    /**
     * 去掉指定前缀
     *
     * @param text   字符串
     * @param prefix 前缀
     * @return 切掉后的字符串, 若前缀不是 preffix, 返回原字符串
     */
    public static String removePrefix(CharSequence text, CharSequence prefix) {
        if (isEmpty(text) || isEmpty(prefix)) {
            return toString(text);
        }

        final String str2 = text.toString();
        if (str2.startsWith(prefix.toString())) {
            return subSuf(str2, prefix.length());// 截取后半段
        }
        return str2;
    }

    /**
     * 忽略大小写去掉指定前缀
     *
     * @param text   字符串
     * @param prefix 前缀
     * @return 切掉后的字符串, 若前缀不是 prefix, 返回原字符串
     */
    public static String removePrefixIgnoreCase(CharSequence text, CharSequence prefix) {
        if (isEmpty(text) || isEmpty(prefix)) {
            return toString(text);
        }

        final String str2 = text.toString();
        if (startWithIgnoreCase(text, prefix)) {
            return subSuf(str2, prefix.length());// 截取后半段
        }
        return str2;
    }

    /**
     * 去掉指定后缀
     *
     * @param text   字符串
     * @param suffix 后缀
     * @return 切掉后的字符串, 若后缀不是 suffix, 返回原字符串
     */
    public static String removeSuffix(CharSequence text, CharSequence suffix) {
        if (isEmpty(text) || isEmpty(suffix)) {
            return toString(text);
        }

        final String str2 = text.toString();
        if (str2.endsWith(suffix.toString())) {
            return subPre(str2, str2.length() - suffix.length());// 截取前半段
        }
        return str2;
    }

    /**
     * 去掉指定后缀，并小写首字母
     *
     * @param text   字符串
     * @param suffix 后缀
     * @return 切掉后的字符串，若后缀不是 suffix， 返回原字符串
     */
    public static String removeSufAndLowerFirst(CharSequence text, CharSequence suffix) {
        return lowerFirst(removeSuffix(text, suffix));
    }

    /**
     * 忽略大小写去掉指定后缀
     *
     * @param text   字符串
     * @param suffix 后缀
     * @return 切掉后的字符串，若后缀不是 suffix， 返回原字符串
     */
    public static String removeSuffixIgnoreCase(CharSequence text, CharSequence suffix) {
        if (isEmpty(text) || isEmpty(suffix)) {
            return toString(text);
        }

        final String str2 = text.toString();
        if (endWithIgnoreCase(text, suffix)) {
            return subPre(str2, str2.length() - suffix.length());
        }
        return str2;
    }

    /**
     * 原字符串首字母大写并在其首部添加指定字符串
     * 例如：text=name, preString=get = return getName
     *
     * @param text      被处理的字符串
     * @param preString 添加的首部
     * @return 处理后的字符串
     */
    public static String upperFirstAndAddPre(CharSequence text, String preString) {
        if (null == text || null == preString) {
            return null;
        }
        return preString + upperFirst(text);
    }

    /**
     * 大写首字母
     * 例如：text = name, return Name
     *
     * @param text 字符串
     * @return 字符串
     */
    public static String upperFirst(CharSequence text) {
        if (null == text) {
            return null;
        }
        if (text.length() > 0) {
            char firstChar = text.charAt(0);
            if (Character.isLowerCase(firstChar)) {
                return Character.toUpperCase(firstChar) + subSuf(text, 1);
            }
        }
        return text.toString();
    }

    /**
     * 小写首字母
     * 例如：text = Name, return name
     *
     * @param text 字符串
     * @return 字符串
     */
    public static String lowerFirst(CharSequence text) {
        if (null == text) {
            return null;
        }
        if (text.length() > 0) {
            char firstChar = text.charAt(0);
            if (Character.isUpperCase(firstChar)) {
                return Character.toLowerCase(firstChar) + subSuf(text, 1);
            }
        }
        return text.toString();
    }

    /**
     * 将驼峰式命名的字符串转换为下划线方式
     * 如果转换前的驼峰式命名的字符串为空,则返回空字符串
     * 例如：HelloWorld= hello_world
     *
     * @param word 转换前的驼峰式命名的字符串
     * @return 转换后下划线大写方式命名的字符串
     */
    public static String toUnderlineCase(CharSequence word) {
        return NamingCase.toUnderlineCase(word);
    }

    /**
     * 将驼峰式命名的字符串转换为使用符号连接方式
     * 如果转换前的驼峰式命名的字符串为空，则返回空字符串
     *
     * @param text   转换前的驼峰式命名的字符串，也可以为符号连接形式
     * @param symbol 连接符
     * @return 转换后符号连接方式命名的字符串
     */
    public static String toSymbolCase(CharSequence text, char symbol) {
        return NamingCase.toSymbolCase(text, symbol);
    }

    /**
     * 将下划线方式命名的字符串转换为驼峰式
     * 如果转换前的下划线大写方式命名的字符串为空，则返回空字符串
     * 例如：hello_world = helloWorld
     *
     * @param text 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String toCamelCase(CharSequence text) {
        return NamingCase.toCamelCase(text);
    }

    /**
     * 将连接符方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串
     * 例如：hello_world = helloWorld; hello-world = helloWorld
     *
     * @param name   转换前的下划线大写方式命名的字符串
     * @param symbol 连接符
     * @return 转换后的驼峰式命名的字符串
     * @see NamingCase#toCamelCase(CharSequence, char)
     */
    public static String toCamelCase(CharSequence name, char symbol) {
        return NamingCase.toCamelCase(name, symbol);
    }

    /**
     * 移除字符串中所有给定字符串
     * 例：removeAll("aa-bb-cc-dd", "-") - aabbccdd
     *
     * @param text 字符串
     * @param word 被移除的字符串
     * @return 移除后的字符串
     */
    public static String removeAll(CharSequence text, CharSequence word) {
        if (isEmpty(text) || isEmpty(word)) {
            return toString(text);
        }
        return text.toString().replace(word, Normal.EMPTY);
    }

    /**
     * 去除字符串中指定的多个字符，如有多个则全部去除
     *
     * @param text  字符串
     * @param chars 字符列表
     * @return 去除后的字符
     */
    public static String removeAll(CharSequence text, char... chars) {
        if (null == text || ArrayKit.isEmpty(chars)) {
            return toString(text);
        }
        final int len = text.length();
        if (0 == len) {
            return toString(text);
        }
        final StringBuilder builder = new StringBuilder(len);
        char c;
        for (int i = 0; i < len; i++) {
            c = text.charAt(i);
            if (false == ArrayKit.contains(chars, c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * 移除字符串中所有给定字符串，当某个字符串出现多次，则全部移除
     * 例：removeAny("aa-bb-cc-dd", "a", "b") - --cc-dd
     *
     * @param text 字符串
     * @param word 被移除的字符串
     * @return 移除后的字符串
     */
    public static String removeAny(CharSequence text, CharSequence... word) {
        String result = toString(text);
        if (isNotEmpty(text)) {
            for (CharSequence x : word) {
                result = removeAll(result, x);
            }
        }
        return result;
    }

    /**
     * 格式化文本
     *
     * @param template 文本模板，被替换的部分用 {key} 表示
     * @param args     参数值对
     * @return 格式化后的文本
     */
    public static String format(CharSequence template, Map<?, ?> args) {
        return format(template, args, true);
    }

    /**
     * 格式化文本, {} 表示占位符
     * 此方法只是简单将占位符 {} 按照顺序替换为参数
     * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可
     * 例：
     * 通常使用：format("this is {} for {}", "a", "b") =  this is a for b
     * 转义{}： format("this is \\{} for {}", "a", "b") =  this is \{} for a
     * 转义\： format("this is \\\\{} for {}", "a", "b") =  this is \a for b
     *
     * @param template 文本模板，被替换的部分用 {} 表示，如果模板为null，返回"null"
     * @param args     参数值
     * @return 格式化后的文本，如果模板为null，返回"null"
     */
    public static String format(CharSequence template, Object... args) {
        if (null == template) {
            return Normal.NULL;
        }
        if (ArrayKit.isEmpty(args) || isBlank(template)) {
            return template.toString();
        }
        return TextFormatter.format(template.toString(), args);
    }

    /**
     * 格式化文本，使用 {varName} 占位
     * map = {a: "aValue", b: "bValue"} format("{a} and {b}", map) - aValue and bValue
     *
     * @param template   文本模板，被替换的部分用 {key} 表示
     * @param map        参数值对
     * @param ignoreNull 是否忽略 {@code null} 值，忽略则 {@code null} 值对应的变量不被替换，否则替换为""
     * @return 格式化后的文本
     */
    public static String format(CharSequence template, Map<?, ?> map, boolean ignoreNull) {
        return TextFormatter.format(template, map, ignoreNull);
    }

    /**
     * 指定字符串数组中，是否包含空字符串
     * 如果传入参数对象不是为空,则返回false
     * 如果传入的参数不是String则返回false 如果字符串包含字母,不区分大小写,则返回true
     *
     * @param obj 对象
     * @return 如果为字符串, 是否有字母
     */
    public static boolean hasLetter(Object obj) {
        if (null == obj) {
            return false;
        } else if (obj instanceof String) {
            char[] chars = ((String) obj).toCharArray();
            for (char c : chars) {
                if (isLetter(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 改进JDK subString
     * index从0开始计算,最后一个字符为-1
     * 如果from和to位置一样,返回 ""
     * 如果from或to为负数,则按照length从后向前数位置,如果绝对值大于字符串长度,则from归到0,to归到length
     * 如果经过修正的index中from大于to,则互换
     * abcdefgh 2 3 =  c
     * abcdefgh 2 -3 =  cde
     *
     * @param text      String
     * @param fromIndex 开始的index(包括)
     * @param toIndex   结束的index(不包括)
     * @return 字串
     */
    public static String sub(CharSequence text, int fromIndex, int toIndex) {
        if (isEmpty(text)) {
            return toString(text);
        }
        int len = text.length();

        if (fromIndex < 0) {
            fromIndex = len + fromIndex;
            if (fromIndex < 0) {
                fromIndex = 0;
            }
        } else if (fromIndex > len) {
            fromIndex = len;
        }

        if (toIndex < 0) {
            toIndex = len + toIndex;
            if (toIndex < 0) {
                toIndex = len;
            }
        } else if (toIndex > len) {
            toIndex = len;
        }

        if (toIndex < fromIndex) {
            int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }

        if (fromIndex == toIndex) {
            return Normal.EMPTY;
        }

        return text.toString().substring(fromIndex, toIndex);
    }

    /**
     * 切割指定位置之前部分的字符串
     *
     * @param string  字符串
     * @param toIndex 切割到的位置(不包括)
     * @return 切割后的剩余的前半部分字符串
     */
    public static String subPre(CharSequence string, int toIndex) {
        return sub(string, 0, toIndex);
    }

    /**
     * 截取部分字符串，这里一个汉字的长度认为是2
     *
     * @param text   字符串
     * @param len    bytes切割到的位置（包含）
     * @param suffix 切割后加上后缀
     * @return 切割后的字符串
     */
    public static String subPreGbk(CharSequence text, int len, CharSequence suffix) {
        return subPreGbk(text, len, true) + suffix;
    }

    /**
     * 截取部分字符串，这里一个汉字的长度认为是2
     * 可以自定义halfUp，如len为10，如果截取后最后一个字符是半个字符，{@code true}表示保留，则长度是11，否则长度9
     *
     * @param text 字符串
     * @param len  bytes切割到的位置（包含）
     * @param word 遇到截取一半的GBK字符，是否保留。
     * @return 切割后的字符串
     */
    public static String subPreGbk(CharSequence text, int len, boolean word) {
        if (isEmpty(text)) {
            return toString(text);
        }

        int counterOfDoubleByte = 0;
        final byte[] b = bytes(text, Charset.GBK);
        if (b.length <= len) {
            return text.toString();
        }
        for (int i = 0; i < len; i++) {
            if (b[i] < 0) {
                counterOfDoubleByte++;
            }
        }

        if (counterOfDoubleByte % 2 != 0) {
            if (word) {
                len += 1;
            } else {
                len -= 1;
            }
        }
        return new String(b, 0, len, Charset.GBK);
    }

    /**
     * 切割指定位置之后部分的字符串
     *
     * @param string    字符串
     * @param fromIndex 切割开始的位置(包括)
     * @return 切割后后剩余的后半部分字符串
     */
    public static String subSuf(CharSequence string, int fromIndex) {
        if (isEmpty(string)) {
            return null;
        }
        return sub(string, fromIndex, string.length());
    }

    /**
     * 截取分隔字符串之前的字符串,不包括分隔字符串
     * 如果给定的字符串为空串(null或"")或者分隔字符串为null,返回原字符串
     * 如果分隔字符串为空串"",则返回空串,如果分隔字符串未找到,返回原字符串,举例如下：
     *
     * <pre>
     * subBefore(null, *)      = null
     * subBefore("", *)        = ""
     * subBefore("abc", "a")   = ""
     * subBefore("abcba", "b") = "a"
     * subBefore("abc", "c")   = "ab"
     * subBefore("abc", "d")   = "abc"
     * subBefore("abc", "")    = ""
     * subBefore("abc", null)  = "abc"
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串(不包括)
     * @param isLastSeparator 是否查找最后一个分隔字符串(多次出现分隔字符串时选取最后一个),true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subBefore(CharSequence string, CharSequence separator, boolean isLastSeparator) {
        if (isEmpty(string) || null == separator) {
            return null == string ? null : Normal.EMPTY;
        }

        final String text = string.toString();
        final String sep = separator.toString();
        if (sep.isEmpty()) {
            return Normal.EMPTY;
        }
        final int pos = isLastSeparator ? text.lastIndexOf(sep) : text.indexOf(sep);
        if (Normal.__1 == pos) {
            return text;
        }
        if (0 == pos) {
            return Normal.EMPTY;
        }
        return text.substring(0, pos);
    }

    /**
     * 截取分隔字符串之前的字符串,不包括分隔字符串
     * 如果给定的字符串为空串(null或"")或者分隔字符串为null,返回原字符串
     * 如果分隔字符串未找到,返回原字符串,举例如下：
     *
     * <pre>
     * subBefore(null, *)      = null
     * subBefore("", *)        = ""
     * subBefore("abc", 'a')   = ""
     * subBefore("abcba", 'b') = "a"
     * subBefore("abc", 'c')   = "ab"
     * subBefore("abc", 'd')   = "abc"
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串(不包括)
     * @param isLastSeparator 是否查找最后一个分隔字符串(多次出现分隔字符串时选取最后一个),true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subBefore(CharSequence string, char separator, boolean isLastSeparator) {
        if (isEmpty(string)) {
            return null == string ? null : Normal.EMPTY;
        }

        final String text = string.toString();
        final int pos = isLastSeparator ? text.lastIndexOf(separator) : text.indexOf(separator);
        if (Normal.__1 == pos) {
            return text;
        }
        if (0 == pos) {
            return Normal.EMPTY;
        }
        return text.substring(0, pos);
    }

    /**
     * 截取分隔字符串之后的字符串,不包括分隔字符串
     * 如果给定的字符串为空串(null或""),返回原字符串
     * 如果分隔字符串为空串(null或""),则返回空串,如果分隔字符串未找到,返回空串,举例如下：
     *
     * <pre>
     * subAfter(null, *)      = null
     * subAfter("", *)        = ""
     * subAfter(*, null)      = ""
     * subAfter("abc", "a")   = "bc"
     * subAfter("abcba", "b") = "cba"
     * subAfter("abc", "c")   = ""
     * subAfter("abc", "d")   = ""
     * subAfter("abc", "")    = "abc"
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串(不包括)
     * @param isLastSeparator 是否查找最后一个分隔字符串(多次出现分隔字符串时选取最后一个),true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subAfter(CharSequence string, CharSequence separator, boolean isLastSeparator) {
        if (isEmpty(string)) {
            return null == string ? null : Normal.EMPTY;
        }
        if (null == separator) {
            return Normal.EMPTY;
        }
        final String text = string.toString();
        final String sep = separator.toString();
        final int pos = isLastSeparator ? text.lastIndexOf(sep) : text.indexOf(sep);
        if (Normal.__1 == pos || (string.length() - 1) == pos) {
            return Normal.EMPTY;
        }
        return text.substring(pos + separator.length());
    }

    /**
     * 截取分隔字符串之后的字符串,不包括分隔字符串
     * 如果给定的字符串为空串(null或""),返回原字符串
     * 如果分隔字符串为空串(null或""),则返回空串,如果分隔字符串未找到,返回空串,举例如下：
     *
     * <pre>
     * subAfter(null, *)      = null
     * subAfter("", *)        = ""
     * subAfter("abc", 'a')   = "bc"
     * subAfter("abcba", 'b') = "cba"
     * subAfter("abc", 'c')   = ""
     * subAfter("abc", 'd')   = ""
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串(不包括)
     * @param isLastSeparator 是否查找最后一个分隔字符串(多次出现分隔字符串时选取最后一个),true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subAfter(CharSequence string, char separator, boolean isLastSeparator) {
        if (isEmpty(string)) {
            return null == string ? null : Normal.EMPTY;
        }
        final String text = string.toString();
        final int pos = isLastSeparator ? text.lastIndexOf(separator) : text.indexOf(separator);
        if (Normal.__1 == pos) {
            return Normal.EMPTY;
        }
        return text.substring(pos + 1);
    }

    /**
     * 截取指定字符串中间部分,不包括标识字符串
     *
     * @param text   被切割的字符串
     * @param before 截取开始的字符串标识
     * @param after  截取到的字符串标识
     * @return 截取后的字符串
     */
    public static String subBetween(CharSequence text, CharSequence before, CharSequence after) {
        return subBetween(text.toString(), before.toString(), after.toString());
    }

    /**
     * 截取指定字符串中间部分,不包括标识字符串
     *
     * <pre>
     * subBetween("wx[b]yz", "[", "]")     = "b"
     * subBetween(null, *, *)              = null
     * subBetween(*, null, *)              = null
     * subBetween(*, *, null)              = null
     * subBetween("", "", "")              = ""
     * subBetween("", "", "]")             = null
     * subBetween("", "[", "]")            = null
     * subBetween("yabcz", "", "")         = ""
     * subBetween("yabcz", "y", "z")       = "abc"
     * subBetween("yabczyabcz", "y", "z")  = "abc"
     * </pre>
     *
     * @param text   被切割的字符串
     * @param before 截取开始的字符串标识
     * @param after  截取到的字符串标识
     * @return 截取后的字符串
     */
    public static String subBetween(String text, String before, String after) {
        if (null == text || null == before || null == after) {
            return null;
        }
        int start = text.indexOf(before);
        if (start != Normal.__1) {
            int end = text.indexOf(after, start + before.length());
            if (end != Normal.__1) {
                return text.substring(start + before.length(), end);
            }
        }
        return null;
    }

    /**
     * 截取指定字符串中间部分,不包括标识字符串
     * <pre>
     * subBetween(null, *)            = null
     * subBetween("", "")             = ""
     * subBetween("", "tag")          = null
     * subBetween("tagabctag", null)  = null
     * subBetween("tagabctag", "")    = ""
     * subBetween("tagabctag", "tag") = "abc"
     * </pre>
     *
     * @param text           被切割的字符串
     * @param beforeAndAfter 截取开始和结束的字符串标识
     * @return 截取后的字符串
     */
    public static String subBetween(CharSequence text, CharSequence beforeAndAfter) {
        return subBetween(text, beforeAndAfter, beforeAndAfter);
    }

    /**
     * 截取指定字符串多段中间部分，不包括标识字符串
     * <pre>
     * subBetweenAll("wx[b]y[z]", "[", "]") 		    = ["b","z"]
     * subBetweenAll(null, *, *)          			= []
     * subBetweenAll(*, null, *)          			= []
     * subBetweenAll(*, *, null)          			= []
     * subBetweenAll("", "", "")          			= []
     * subBetweenAll("", "", "]")         			= []
     * subBetweenAll("", "[", "]")        			= []
     * subBetweenAll("yabcz", "", "")     			= []
     * subBetweenAll("yabcz", "y", "z")   			= ["abc"]
     * subBetweenAll("yabczyabcz", "y", "z")   		= ["abc","abc"]
     * subBetweenAll("[yabc[zy]abcz]", "[", "]");     = ["zy"]
     * </pre>
     *
     * @param text   被切割的字符串
     * @param prefix 截取开始的字符串标识
     * @param suffix 截取到的字符串标识
     * @return 截取后的字符串
     */
    public static String[] subBetweenAll(CharSequence text, CharSequence prefix, CharSequence suffix) {
        if (hasEmpty(text, prefix, suffix) ||
                // 不包含起始字符串，则肯定没有子串
                false == contains(text, prefix)) {
            return new String[0];
        }

        final List<String> result = new LinkedList<>();
        final String[] split = splitToArray(text, prefix);
        if (prefix.equals(suffix)) {
            // 前后缀字符相同，单独处理
            for (int i = 1, length = split.length - 1; i < length; i += 2) {
                result.add(split[i]);
            }
        } else {
            int suffixIndex;
            String fragment;
            for (int i = 1; i < split.length; i++) {
                fragment = split[i];
                suffixIndex = fragment.indexOf(suffix.toString());
                if (suffixIndex > 0) {
                    result.add(fragment.substring(0, suffixIndex));
                }
            }
        }

        return result.toArray(new String[0]);
    }

    /**
     * 截取指定字符串多段中间部分，不包括标识字符串
     * <p>
     * 栗子：
     *
     * <pre>
     * subBetweenAll(null, *)          			= []
     * subBetweenAll(*, null)          			= []
     * subBetweenAll(*, *)          			    = []
     * subBetweenAll("", "")          			= []
     * subBetweenAll("", "#")         			= []
     * subBetweenAll("hello", "")     		    = []
     * subBetweenAll("#hello#", "#")   		    = ["hello"]
     * subBetweenAll("#hello# #world#!", "#")     = ["hello", "world"]
     * subBetweenAll("#hello# world#!", "#");     = ["hello"]
     * </pre>
     *
     * @param text            被切割的字符串
     * @param prefixAndSuffix 截取开始和结束的字符串标识
     * @return 截取后的字符串
     */
    public static String[] subBetweenAll(final CharSequence text, final CharSequence prefixAndSuffix) {
        return subBetweenAll(text, prefixAndSuffix, prefixAndSuffix);
    }

    /**
     * 切割指定长度的后部分的字符串
     *
     * <pre>
     * subSufByLength("abcde", 3)      =    "cde"
     * subSufByLength("abcde", 0)      =    ""
     * subSufByLength("abcde", -5)     =    ""
     * subSufByLength("abcde", -1)     =    ""
     * subSufByLength("abcde", 5)      =    "abcde"
     * subSufByLength("abcde", 10)     =    "abcde"
     * subSufByLength(null, 3)         =     null
     * </pre>
     *
     * @param text   字符串
     * @param length 切割长度
     * @return 切割后后剩余的后半部分字符串
     */
    public static String subSufByLength(final CharSequence text, int length) {
        if (isEmpty(text)) {
            return null;
        }
        if (length <= 0) {
            return Normal.EMPTY;
        }
        return sub(text, -length, text.length());
    }

    /**
     * 截取字符串,从指定位置开始,截取指定长度的字符串
     *
     * @param input     原始字符串
     * @param fromIndex 开始的index,包括
     * @param length    要截取的长度
     * @return 截取后的字符串
     */
    public static String subByLength(final CharSequence input, final int fromIndex, final int length) {
        if (isEmpty(input)) {
            return null;
        }
        if (length <= 0) {
            return Normal.EMPTY;
        }

        final int toIndex;
        if (fromIndex < 0) {
            toIndex = fromIndex - length;
        } else {
            toIndex = fromIndex + length;
        }
        return sub(input, fromIndex, toIndex);
    }

    /**
     * 比较两个字符串(大小写敏感)
     *
     * <pre>
     * equals(null, null)   = true
     * equals(null, &quot;abc&quot;)  = false
     * equals(&quot;abc&quot;, null)  = false
     * equals(&quot;abc&quot;, &quot;abc&quot;) = true
     * equals(&quot;abc&quot;, &quot;ABC&quot;) = false
     * </pre>
     *
     * @param x 要比较的字符串
     * @param y 要比较的字符串
     * @return 如果两个字符串相同，或者都是<code>null</code>，则返回<code>true</code>
     */
    public static boolean equals(CharSequence x, CharSequence y) {
        return equals(x, y, false);
    }

    /**
     * 比较两个字符串是否相等
     * <ul>
     *     <li>x和y都为{@code null}</li>
     *     <li>忽略大小写使用{@link String#equalsIgnoreCase(String)}判断相等</li>
     *     <li>不忽略大小写使用{@link String#contentEquals(CharSequence)}判断相等</li>
     * </ul>
     *
     * @param x          要比较的字符串
     * @param y          要比较的字符串
     * @param ignoreCase 是否忽略大小写
     * @return 如果两个字符串相同，或者都是<code>null</code>，则返回<code>true</code>
     */
    public static boolean equals(CharSequence x, CharSequence y, boolean ignoreCase) {
        if (null == x) {
            // 只有两个都为null才判断相等
            return null == y;
        }
        if (null == y) {
            // 字符串2空，字符串1非空，直接false
            return false;
        }

        if (ignoreCase) {
            return x.toString().equalsIgnoreCase(y.toString());
        } else {
            return x.equals(y);
        }
    }

    /**
     * 比较两个字符串(大小写不敏感)
     *
     * <pre>
     * equalsIgnoreCase(null, null)   = true
     * equalsIgnoreCase(null, &quot;abc&quot;)  = false
     * equalsIgnoreCase(&quot;abc&quot;, null)  = false
     * equalsIgnoreCase(&quot;abc&quot;, &quot;abc&quot;) = true
     * equalsIgnoreCase(&quot;abc&quot;, &quot;ABC&quot;) = true
     * </pre>
     *
     * @param x 要比较的字符串
     * @param y 要比较的字符串
     * @return 如果两个字符串相同，或者都是<code>null</code>，则返回<code>true</code>
     */
    public static boolean equalsIgnoreCase(CharSequence x, CharSequence y) {
        return equals(x, y, true);
    }

    /**
     * 给定字符串是否与提供的中任一字符串相同(忽略大小写)，相同则返回{@code true}，没有相同的返回{@code false}
     * 如果参与比对的字符串列表为空，返回{@code false}
     *
     * @param x 给定需要检查的字符串
     * @param y 需要参与比对的字符串列表
     * @return 是否相同
     */
    public static boolean equalsAnyIgnoreCase(CharSequence x, CharSequence... y) {
        return equalsAny(x, true, y);
    }

    /**
     * 给定字符串是否与提供的中任一字符串相同，相同则返回{@code true}，没有相同的返回{@code false}
     * 如果参与比对的字符串列表为空，返回{@code false}
     *
     * @param text 给定需要检查的字符串
     * @param args 需要参与比对的字符串列表
     * @return 是否相同
     */
    public static boolean equalsAny(CharSequence text, CharSequence... args) {
        return equalsAny(text, false, args);
    }

    /**
     * 给定字符串是否与提供的中任一字符串相同，相同则返回{@code true}，没有相同的返回{@code false}
     * 如果参与比对的字符串列表为空，返回{@code false}
     *
     * @param text       给定需要检查的字符串
     * @param ignoreCase 是否忽略大小写
     * @param args       需要参与比对的字符串列表
     * @return 是否相同
     */
    public static boolean equalsAny(CharSequence text, boolean ignoreCase, CharSequence... args) {
        if (ArrayKit.isEmpty(text)) {
            return false;
        }

        for (CharSequence t : args) {
            if (equals(text, t, ignoreCase)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param text 字符串
     * @param word 被查找的字符
     * @return 位置
     */
    public static int indexOf(final CharSequence text, char word) {
        return indexOf(text, word, 0);
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param text  字符串
     * @param word  被查找的字符
     * @param start 起始位置,如果小于0,从0开始查找
     * @return 位置
     */
    public static int indexOf(final CharSequence text, char word, int start) {
        if (text instanceof String) {
            return ((String) text).indexOf(word, start);
        } else {
            return indexOf(text, word, start, -1);
        }
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param text  字符串
     * @param word  被查找的字符
     * @param start 起始位置,如果小于0,从0开始查找
     * @param end   终止位置,如果超过text.length()则默认查找到字符串末尾
     * @return 位置
     */
    public static int indexOf(final CharSequence text, char word, int start, int end) {
        if (isEmpty(text)) {
            return Normal.__1;
        }
        return new CharFinder(word).setText(text).setEndIndex(end).start(start);
    }

    /**
     * 指定范围内查找字符串,忽略大小写
     *
     * <pre>
     * indexOfIgnoreCase(null, *, *)          = -1
     * indexOfIgnoreCase(*, null, *)          = -1
     * indexOfIgnoreCase("", "", 0)           = 0
     * indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * indexOfIgnoreCase("abc", "", 9)        = -1
     * </pre>
     *
     * @param text 字符串
     * @param word 需要查找位置的字符串
     * @return 位置
     */
    public static int indexOfIgnoreCase(final CharSequence text, final CharSequence word) {
        return indexOfIgnoreCase(text, word, 0);
    }

    /**
     * 指定范围内查找字符串
     *
     * <pre>
     * indexOfIgnoreCase(null, *, *)          = -1
     * indexOfIgnoreCase(*, null, *)          = -1
     * indexOfIgnoreCase("", "", 0)           = 0
     * indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * indexOfIgnoreCase("abc", "", 9)        = -1
     * </pre>
     *
     * @param text 字符串
     * @param word 需要查找位置的字符串
     * @param from 起始位置
     * @return 位置
     */
    public static int indexOfIgnoreCase(CharSequence text, CharSequence word, int from) {
        return indexOf(text, word, from, true);
    }

    /**
     * 指定范围内反向查找字符串
     *
     * @param text       字符串，空则返回-1
     * @param word       需要查找位置的字符串，空则返回-1
     * @param from       起始位置（包含）
     * @param ignoreCase 是否忽略大小写
     * @return 位置
     */
    public static int indexOf(CharSequence text, CharSequence word, int from, boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(word)) {
            if (equals(text, word)) {
                return 0;
            } else {
                return Normal.__1;
            }
        }
        return new StringFinder(word, ignoreCase).setText(text).start(from);
    }

    /**
     * 指定范围内查找字符串,忽略大小写
     *
     * @param text 字符串
     * @param word 需要查找位置的字符串
     * @return 位置
     */
    public static int lastIndexOfIgnoreCase(CharSequence text, CharSequence word) {
        return lastIndexOfIgnoreCase(text, word, text.length());
    }

    /**
     * 指定范围内查找字符串,忽略大小写
     *
     * @param text 字符串
     * @param word 需要查找位置的字符串
     * @param from 起始位置,从后往前计数
     * @return 位置
     */
    public static int lastIndexOfIgnoreCase(CharSequence text, CharSequence word, int from) {
        return lastIndexOf(text, word, from, true);
    }

    /**
     * 指定范围内查找字符串
     *
     * @param text       字符串
     * @param word       需要查找位置的字符串
     * @param from       起始位置,从后往前计数
     * @param ignoreCase 是否忽略大小写
     * @return 位置
     */
    public static int lastIndexOf(CharSequence text, CharSequence word, int from, boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(word)) {
            if (equals(text, word)) {
                return 0;
            } else {
                return Normal.__1;
            }
        }
        return new StringFinder(word, ignoreCase).setText(text).setNegative(true).start(from);
    }

    /**
     * 切分字符串为long数组
     *
     * @param text      被切分的字符串
     * @param separator 分隔符
     * @return 切分后long数组
     */
    public static long[] splitToLong(CharSequence text, char separator) {
        return Convert.convert(long[].class, splitTrim(text, separator));
    }

    /**
     * 切分字符串为long数组
     *
     * @param text      被切分的字符串
     * @param separator 分隔符字符串
     * @return 切分后long数组
     */
    public static long[] splitToLong(CharSequence text, CharSequence separator) {
        return Convert.convert(long[].class, splitTrim(text, separator));
    }

    /**
     * 切分字符串为int数组
     *
     * @param text      被切分的字符串
     * @param separator 分隔符
     * @return 切分后long数组
     */
    public static int[] splitToInt(CharSequence text, char separator) {
        return Convert.convert(int[].class, splitTrim(text, separator));
    }

    /**
     * 切分字符串为int数组
     *
     * @param text      被切分的字符串
     * @param separator 分隔符字符串
     * @return 切分后long数组
     */
    public static int[] splitToInt(CharSequence text, CharSequence separator) {
        return Convert.convert(int[].class, splitTrim(text, separator));
    }

    /**
     * 切分字符串，如果分隔符不存在则返回原字符串
     *
     * @param text      被切分的字符串
     * @param separator 分隔符
     * @return 字符串
     */
    public static String[] splitToArray(CharSequence text, CharSequence separator) {
        if (text == null) {
            return new String[]{};
        }

        return TextSplitter.splitToArray(text.toString(), toString(separator), 0, false, false);
    }

    /**
     * 切分字符串
     *
     * @param text      被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的数组
     */
    public static String[] splitToArray(CharSequence text, char separator) {
        return splitToArray(text, separator, 0);
    }

    /**
     * 切分字符串
     *
     * @param text      被切分的字符串
     * @param separator 分隔符字符
     * @param limit     限制分片数
     * @return 切分后的数组
     */
    public static String[] splitToArray(CharSequence text, char separator, int limit) {
        Assert.notNull(text, "Text must be not null!");
        return TextSplitter.splitToArray(text.toString(), separator, limit, false, false);
    }

    /**
     * 切分字符串
     * a#b#c =》 [a,b,c]
     * a##b#c =》 [a,"",b,c]
     *
     * @param text      被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence text, char separator) {
        return split(text, separator, 0);
    }

    /**
     * 切分字符串，去除切分后每个元素两边的空白符，去除空白项
     *
     * @param text      被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     */
    public static List<String> splitTrim(CharSequence text, char separator) {
        return splitTrim(text, separator, -1);
    }

    /**
     * 切分字符串，去除切分后每个元素两边的空白符，去除空白项
     *
     * @param text      被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     */
    public static List<String> splitTrim(CharSequence text, CharSequence separator) {
        return splitTrim(text, separator, -1);
    }

    /**
     * 切分字符串，去除切分后每个元素两边的空白符，去除空白项
     *
     * @param text      被切分的字符串
     * @param separator 分隔符字符
     * @param limit     限制分片数，-1不限制
     * @return 切分后的集合
     */
    public static List<String> splitTrim(CharSequence text, char separator, int limit) {
        return split(text, separator, limit, true, true);
    }

    /**
     * 切分字符串，去除切分后每个元素两边的空白符，去除空白项
     *
     * @param text      被切分的字符串
     * @param separator 分隔符字符
     * @param limit     限制分片数，-1不限制
     * @return 切分后的集合
     */
    public static List<String> splitTrim(CharSequence text, CharSequence separator, int limit) {
        return split(text, separator, limit, true, true);
    }

    /**
     * 根据给定长度，将给定字符串截取为多个部分
     *
     * @param text 字符串
     * @param len  每一个小节的长度
     * @return 截取后的字符串数组
     * @see TextSplitter#splitByLength(String, int)
     */
    public static String[] split(CharSequence text, int len) {
        if (null == text) {
            return new String[]{};
        }
        return TextSplitter.splitByLength(text.toString(), len);
    }

    /**
     * 切分字符串，如果分隔符不存在则返回原字符串
     *
     * @param text      被切分的字符串
     * @param separator 分隔符
     * @return 字符串
     */
    public static List<String> split(CharSequence text, CharSequence separator) {
        return split(text, separator, false, false);
    }

    /**
     * 切分字符串，不去除切分后每个元素两边的空白符，不去除空白项
     *
     * @param text      被切分的字符串
     * @param separator 分隔符字符
     * @param limit     限制分片数，-1不限制
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence text, char separator, int limit) {
        return split(text, separator, limit, false, false);
    }

    /**
     * 切分字符串，不限制分片数量
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence text, char separator, boolean isTrim, boolean ignoreEmpty) {
        return split(text, separator, 0, isTrim, ignoreEmpty);
    }

    /**
     * 切分字符串
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence text, CharSequence separator, boolean isTrim, boolean ignoreEmpty) {
        return split(text, separator, 0, isTrim, ignoreEmpty);
    }

    /**
     * 切分字符串
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence text, char separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return TextSplitter.split(text, separator, limit, isTrim, ignoreEmpty);
    }

    /**
     * 切分字符串
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence text, CharSequence separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        if (null == text) {
            return new ArrayList<>(0);
        }
        final String separatorStr = (null == separator) ? null : separator.toString();
        return TextSplitter.split(text.toString(), separatorStr, limit, isTrim, ignoreEmpty);
    }

    /**
     * 切分字符串
     *
     * @param <R>         切分后元素类型
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，-1不限制
     * @param ignoreEmpty 是否忽略空串
     * @param mapping     切分后的字符串元素的转换方法
     * @return 切分后的集合，元素类型是经过 mapping 转换后的
     */
    public static <R> List<R> split(CharSequence text, char separator, int limit, boolean ignoreEmpty, Function<String, R> mapping) {
        return TextSplitter.split(text.toString(), separator, limit, ignoreEmpty, mapping);
    }

    /**
     * 通过CodePoint截取字符串，可以截断Emoji
     *
     * @param text      string
     * @param fromIndex 开始的index(包括)
     * @param toIndex   结束的index(不包括)
     * @return 字串
     */
    public static String subCodePoint(CharSequence text, int fromIndex, int toIndex) {
        if (isEmpty(text)) {
            return toString(text);
        }

        if (fromIndex < 0 || fromIndex > toIndex) {
            throw new IllegalArgumentException();
        }

        if (fromIndex == toIndex) {
            return Normal.EMPTY;
        }

        final StringBuilder sb = new StringBuilder();
        final int subLen = toIndex - fromIndex;
        text.toString().codePoints().skip(fromIndex).limit(subLen).forEach(v -> sb.append(Character.toChars(v)));
        return sb.toString();
    }

    /**
     * 当给定字符串为null时，转换为Empty
     *
     * @param text 被转换的字符串
     * @return 转换后的字符串
     */
    public static String nullToEmpty(CharSequence text) {
        return nullToDefault(text, Normal.EMPTY);
    }

    /**
     * 如果字符串是<code>null</code>，则返回指定默认字符串，否则返回字符串本身。
     *
     * <pre>
     * nullToDefault(null, &quot;default&quot;)  = &quot;default&quot;
     * nullToDefault(&quot;&quot;, &quot;default&quot;)    = &quot;&quot;
     * nullToDefault(&quot;  &quot;, &quot;default&quot;)  = &quot;  &quot;
     * nullToDefault(&quot;bat&quot;, &quot;default&quot;) = &quot;bat&quot;
     * </pre>
     *
     * @param text       要转换的字符串
     * @param defaultStr 默认字符串
     * @return 字符串本身或指定的默认字符串
     */
    public static String nullToDefault(CharSequence text, String defaultStr) {
        return null == text ? defaultStr : text.toString();
    }

    /**
     * 如果字符串是<code>null</code>或者&quot;&quot;，则返回指定默认字符串，否则返回字符串本身。
     *
     * <pre>
     * emptyToDefault(null, &quot;default&quot;)  = &quot;default&quot;
     * emptyToDefault(&quot;&quot;, &quot;default&quot;)    = &quot;default&quot;
     * emptyToDefault(&quot;  &quot;, &quot;default&quot;)  = &quot;  &quot;
     * emptyToDefault(&quot;bat&quot;, &quot;default&quot;) = &quot;bat&quot;
     * </pre>
     *
     * @param text       要转换的字符串
     * @param defaultStr 默认字符串
     * @return 字符串本身或指定的默认字符串
     */
    public static String emptyToDefault(CharSequence text, String defaultStr) {
        return isEmpty(text) ? defaultStr : text.toString();
    }

    /**
     * 如果字符串是<code>null</code>或者&quot;&quot;或者空白，则返回指定默认字符串，否则返回字符串本身。
     *
     * <pre>
     * emptyToDefault(null, &quot;default&quot;)  = &quot;default&quot;
     * emptyToDefault(&quot;&quot;, &quot;default&quot;)    = &quot;default&quot;
     * emptyToDefault(&quot;  &quot;, &quot;default&quot;)  = &quot;default&quot;
     * emptyToDefault(&quot;bat&quot;, &quot;default&quot;) = &quot;bat&quot;
     * </pre>
     *
     * @param text       要转换的字符串
     * @param defaultStr 默认字符串
     * @return 字符串本身或指定的默认字符串
     */
    public static String blankToDefault(CharSequence text, String defaultStr) {
        return isBlank(text) ? defaultStr : text.toString();
    }

    /**
     * 截取第一个字串的部分字符，与第二个字符串比较（长度一致），判断截取的子串是否相同
     * 任意一个字符串为null返回false
     *
     * @param str1       第一个字符串
     * @param start1     第一个字符串开始的位置
     * @param str2       第二个字符串
     * @param ignoreCase 是否忽略大小写
     * @return 子串是否相同
     */
    public static boolean isSubEquals(CharSequence str1, int start1, CharSequence str2, boolean ignoreCase) {
        return isSubEquals(str1, start1, str2, 0, str2.length(), ignoreCase);
    }

    /**
     * 截取两个字符串的不同部分(长度一致),判断截取的子串是否相同
     * 任意一个字符串为null返回false
     *
     * @param str1       第一个字符串
     * @param start1     第一个字符串开始的位置
     * @param str2       第二个字符串
     * @param start2     第二个字符串开始的位置
     * @param length     截取长度
     * @param ignoreCase 是否忽略大小写
     * @return 子串是否相同
     */
    public static boolean isSubEquals(CharSequence str1, int start1, CharSequence str2, int start2, int length, boolean ignoreCase) {
        if (null == str1 || null == str2) {
            return false;
        }

        return str1.toString().regionMatches(ignoreCase, start1, str2.toString(), start2, length);
    }

    /**
     * 重复某个字符
     *
     * @param c     被重复的字符
     * @param count 重复的数目,如果小于等于0则返回""
     * @return 重复字符字符串
     */
    public static String repeat(char c, int count) {
        if (count <= 0) {
            return Normal.EMPTY;
        }

        char[] result = new char[count];
        for (int i = 0; i < count; i++) {
            result[i] = c;
        }
        return new String(result);
    }

    /**
     * 重复某个字符串
     *
     * @param text  被重复的字符
     * @param count 重复的数目
     * @return 重复字符字符串
     */
    public static String repeat(CharSequence text, int count) {
        if (null == text) {
            return null;
        }
        if (count <= 0 || text.length() == 0) {
            return Normal.EMPTY;
        }
        if (count == 1) {
            return text.toString();
        }

        // 检查
        final int len = text.length();
        final long longSize = (long) len * (long) count;
        final int size = (int) longSize;
        if (size != longSize) {
            throw new ArrayIndexOutOfBoundsException("Required String length is too large: " + longSize);
        }

        final char[] array = new char[size];
        text.toString().getChars(0, len, array, 0);
        int n;
        for (n = len; n < size - n; n <<= 1) {// n <<= 1相当于n *2
            System.arraycopy(array, 0, array, n, n);
        }
        System.arraycopy(array, 0, array, n, size - n);
        return new String(array);
    }

    /**
     * 重复某个字符串到指定长度
     *
     * @param text   被重复的字符
     * @param padLen 指定长度
     * @return 重复字符字符串
     */
    public static String repeatByLength(CharSequence text, int padLen) {
        if (null == text) {
            return null;
        }
        if (padLen <= 0) {
            return Normal.EMPTY;
        }
        final int strLen = text.length();
        if (strLen == padLen) {
            return text.toString();
        } else if (strLen > padLen) {
            return subPre(text, padLen);
        }

        // 重复，直到达到指定长度
        final char[] padding = new char[padLen];
        for (int i = 0; i < padLen; i++) {
            padding[i] = text.charAt(i % strLen);
        }
        return new String(padding);
    }

    /**
     * 重复某个字符串并通过分界符连接
     *
     * <pre>
     * repeatAndJoin("?", 5, ",")   = "?,?,?,?,?"
     * repeatAndJoin("?", 0, ",")   = ""
     * repeatAndJoin("?", 5, null)  = "?????"
     * </pre>
     *
     * @param text      被重复的字符串
     * @param count     数量
     * @param delimiter 分界符
     * @return 连接后的字符串
     */
    public static String repeatAndJoin(CharSequence text, int count, CharSequence delimiter) {
        if (count <= 0) {
            return Normal.EMPTY;
        }
        final StringBuilder builder = new StringBuilder(text.length() * count);
        builder.append(text);
        count--;

        final boolean isAppendDelimiter = isNotEmpty(delimiter);
        while (count-- > 0) {
            if (isAppendDelimiter) {
                builder.append(delimiter);
            }
            builder.append(text);
        }
        return builder.toString();
    }

    /**
     * 反转字符串
     * 例如：abcd = dcba
     *
     * @param text 被反转的字符串
     * @return 反转后的字符串
     */
    public static String reverse(String text) {
        char[] chars = text.toCharArray();
        ArrayKit.reverse(chars);
        return new String(chars);
    }

    /**
     * 编码字符串
     * 使用系统默认编码
     *
     * @param text 字符串
     * @return 编码后的字节码
     */
    public static byte[] bytes(CharSequence text) {
        return bytes(text, java.nio.charset.Charset.defaultCharset());
    }

    /**
     * 编码字符串
     *
     * @param text    字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 编码后的字节码
     */
    public static byte[] bytes(CharSequence text, String charset) {
        return bytes(text, isBlank(charset) ? java.nio.charset.Charset.defaultCharset() : java.nio.charset.Charset.forName(charset));
    }

    /**
     * 编码字符串
     *
     * @param text    字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 编码后的字节码
     */
    public static byte[] bytes(CharSequence text, java.nio.charset.Charset charset) {
        if (null == text) {
            return null;
        }

        if (null == charset) {
            return text.toString().getBytes();
        }
        return text.toString().getBytes(charset);
    }


    /**
     * 替换字符串中的指定字符串,忽略大小写
     *
     * @param text        字符串
     * @param word        被查找的字符串
     * @param replacement 被替换的字符串
     * @return 替换后的字符串
     */
    public static String replaceIgnoreCase(CharSequence text, CharSequence word, CharSequence replacement) {
        return replace(text, 0, word, replacement, true);
    }

    /**
     * 替换字符串中的空格、回车、换行符、制表符
     *
     * @param text 字符串信息
     * @return 替换后的字符串
     */
    public static String replace(CharSequence text) {
        String val = Normal.EMPTY;
        if (null != text) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            java.util.regex.Matcher m = p.matcher(text);
            val = m.replaceAll(Normal.EMPTY);
        }
        return val;
    }

    /**
     * 替换字符串中的指定字符串
     *
     * @param text        字符串
     * @param word        被查找的字符串
     * @param replacement 被替换的字符串
     * @return 替换后的字符串
     */
    public static String replace(CharSequence text, CharSequence word, CharSequence replacement) {
        return replace(text, 0, word, replacement, false);
    }

    /**
     * 替换字符串中的指定字符串
     *
     * @param text        字符串
     * @param word        被查找的字符串
     * @param replacement 被替换的字符串
     * @param ignoreCase  是否忽略大小写
     * @return 替换后的字符串
     */
    public static String replace(CharSequence text, CharSequence word, CharSequence replacement,
                                 boolean ignoreCase) {
        return replace(text, 0, word, replacement, ignoreCase);
    }

    /**
     * 替换字符串中的指定字符串
     *
     * @param text        字符串
     * @param fromIndex   开始位置(包括)
     * @param word        被查找的字符串
     * @param replacement 被替换的字符串
     * @param ignoreCase  是否忽略大小写
     * @return 替换后的字符串
     */
    public static String replace(CharSequence text, int fromIndex, CharSequence word, CharSequence replacement,
                                 boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(word)) {
            return toString(text);
        }
        if (null == replacement) {
            replacement = Normal.EMPTY;
        }

        final int textLength = text.length();
        final int wordLength = word.length();
        if (textLength < wordLength) {
            return toString(text);
        }

        if (fromIndex > textLength) {
            return toString(text);
        } else if (fromIndex < 0) {
            fromIndex = 0;
        }

        final StringBuilder result = new StringBuilder(textLength - wordLength + replacement.length());
        if (0 != fromIndex) {
            result.append(text.subSequence(0, fromIndex));
        }

        int preIndex = fromIndex;
        int index;
        while ((index = indexOf(text, word, preIndex, ignoreCase)) > -1) {
            result.append(text.subSequence(preIndex, index));
            result.append(replacement);
            preIndex = index + wordLength;
        }

        if (preIndex < textLength) {
            // 结尾部分
            result.append(text.subSequence(preIndex, textLength));
        }
        return result.toString();
    }

    /**
     * 替换所有正则匹配的文本，并使用自定义函数决定如何替换
     *
     * @param text       要替换的字符串
     * @param regex      用于匹配的正则式
     * @param replaceFun 决定如何替换的函数
     * @return 替换后的字符串
     */
    public static String replace(CharSequence text, String regex, XFunction<java.util.regex.Matcher, String> replaceFun) {
        return PatternKit.replaceAll(text, regex, replaceFun);
    }

    /**
     * 替换指定字符串的指定区间内字符为指定字符串，字符串只重复一次
     * 此方法使用{@link String#codePoints()}完成拆分替换
     *
     * @param text         字符串
     * @param startInclude 开始位置（包含）
     * @param endExclude   结束位置（不包含）
     * @param replacedChar 被替换的字符串
     * @return 替换后的字符串
     */
    public static String replace(CharSequence text, int startInclude, int endExclude, CharSequence replacedChar) {
        if (isEmpty(text)) {
            return toString(text);
        }
        final String originalStr = toString(text);
        final int[] strCodePoints = originalStr.codePoints().toArray();
        final int strLength = strCodePoints.length;
        if (startInclude > strLength) {
            return originalStr;
        }
        if (endExclude > strLength) {
            endExclude = strLength;
        }
        if (startInclude > endExclude) {
            // 如果起始位置大于结束位置，不替换
            return originalStr;
        }

        // 新字符串长度 <= 旧长度 - (被替换区间codePoints数量) + 替换字符串长度
        final StringBuilder stringBuilder = new StringBuilder(originalStr.length() - (endExclude - startInclude) + replacedChar.length());
        for (int i = 0; i < startInclude; i++) {
            stringBuilder.appendCodePoint(strCodePoints[i]);
        }
        stringBuilder.append(replacedChar);
        for (int i = endExclude; i < strLength; i++) {
            stringBuilder.appendCodePoint(strCodePoints[i]);
        }
        return stringBuilder.toString();
    }

    /**
     * 替换所有正则匹配的文本，并使用自定义函数决定如何替换
     * replaceFun可以通过{@link java.util.regex.Matcher}提取出匹配到的内容的不同部分，然后经过重新处理、组装变成新的内容放回原位。
     *
     * <pre class="code">
     *     replace(this.content, "(\\d+)", parameters -&gt; "-" + parameters.group(1) + "-")
     *     // 结果为："ZZZaaabbbccc中文-1234-"
     * </pre>
     *
     * @param text       要替换的字符串
     * @param pattern    用于匹配的正则式
     * @param replaceFun 决定如何替换的函数
     * @return 替换后的字符串
     */
    public static String replace(CharSequence text, Pattern pattern, XFunction<java.util.regex.Matcher, String> replaceFun) {
        return PatternKit.replaceAll(text, pattern, replaceFun);
    }

    /**
     * 替换指定字符串的指定区间内字符为固定字符
     *
     * @param text         字符串
     * @param startInclude 开始位置(包含)
     * @param endExclude   结束位置(不包含)
     * @param replacedChar 被替换的字符
     * @return 替换后的字符串
     */
    public static String replace(CharSequence text, int startInclude, int endExclude, char replacedChar) {
        if (isEmpty(text)) {
            return toString(text);
        }
        final String original = toString(text);
        final int[] strCodePoints = original.codePoints().toArray();
        final int strLength = strCodePoints.length;
        if (startInclude > strLength) {
            return original;
        }
        if (endExclude > strLength) {
            endExclude = strLength;
        }
        if (startInclude > endExclude) {
            // 如果起始位置大于结束位置，不替换
            return original;
        }

        final StringBuilder stringBuilder = new StringBuilder(original.length());
        for (int i = 0; i < strLength; i++) {
            if (i >= startInclude && i < endExclude) {
                stringBuilder.append(replacedChar);
            } else {
                stringBuilder.appendCodePoint(strCodePoints[i]);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 替换字符串中最后一个指定字符串
     *
     * @param text         字符串
     * @param searchStr    被查找的字符串
     * @param replacedChar 被替换的字符串
     * @return 替换后的字符串
     */
    public static String replaceLast(CharSequence text, CharSequence searchStr, CharSequence replacedChar) {
        return replaceLast(text, searchStr, replacedChar, false);
    }

    /**
     * 替换字符串中最后一个指定字符串
     *
     * @param text         字符串
     * @param searchStr    被查找的字符串
     * @param replacedChar 被替换的字符串
     * @param ignoreCase   是否忽略大小写
     * @return 替换后的字符串
     */
    public static String replaceLast(CharSequence text, CharSequence searchStr, CharSequence replacedChar, boolean ignoreCase) {
        if (isEmpty(text)) {
            return toString(text);
        }
        int lastIndex = lastIndexOf(text, searchStr, text.length(), ignoreCase);
        if (-1 == lastIndex) {
            return toString(text);
        }
        return replace(text, lastIndex, searchStr, replacedChar, ignoreCase);
    }

    /**
     * 替换字符串中第一个指定字符串
     *
     * @param text         字符串
     * @param searchText   被查找的字符串
     * @param replacedText 被替换的字符串
     * @return 替换后的字符串
     */
    public static String replaceFirst(CharSequence text, CharSequence searchText, CharSequence replacedText) {
        return replaceFirst(text, searchText, replacedText, false);
    }

    /**
     * 替换字符串中第一个指定字符串
     *
     * @param text         字符串
     * @param searchText   被查找的字符串
     * @param replacedText 被替换的字符串
     * @param ignoreCase   是否忽略大小写
     * @return 替换后的字符串
     */
    public static String replaceFirst(CharSequence text, CharSequence searchText, CharSequence replacedText, boolean ignoreCase) {
        if (isEmpty(text)) {
            return toString(text);
        }
        int startInclude = indexOf(text, searchText, 0, ignoreCase);
        if (-1 == startInclude) {
            return toString(text);
        }
        return replace(text, startInclude, startInclude + searchText.length(), replacedText);
    }

    /**
     * 替换指定字符串的指定区间内字符为"*"
     *
     * @param text         字符串
     * @param startInclude 开始位置(包含)
     * @param endExclude   结束位置(不包含)
     * @return 替换后的字符串
     */
    public static String hide(CharSequence text, int startInclude, int endExclude) {
        return replace(text, startInclude, endExclude, Symbol.C_STAR);
    }

    /**
     * 替换字符字符数组中所有的字符为replacedStr
     * 提供的chars为所有需要被替换的字符,例如："\r\n",则"\r"和"\n"都会被替换,哪怕他们单独存在
     *
     * @param text        被检查的字符串
     * @param chars       需要替换的字符列表,用一个字符串表示这个字符列表
     * @param replacedStr 替换成的字符串
     * @return 新字符串
     */
    public static String replaceChars(CharSequence text, String chars, CharSequence replacedStr) {
        if (isEmpty(text) || isEmpty(chars)) {
            return toString(text);
        }
        return replaceChars(text, chars.toCharArray(), replacedStr);
    }

    /**
     * 替换字符字符数组中所有的字符为replacedStr
     *
     * @param text        被检查的字符串
     * @param chars       需要替换的字符列表
     * @param replacedStr 替换成的字符串
     * @return 新字符串
     */
    public static String replaceChars(CharSequence text, char[] chars, CharSequence replacedStr) {
        if (isEmpty(text) || ArrayKit.isEmpty(chars)) {
            return toString(text);
        }

        final Set<Character> set = new HashSet<>(chars.length);
        for (char c : chars) {
            set.add(c);
        }
        int strLen = text.length();
        final StringBuilder builder = new StringBuilder();
        char c;
        for (int i = 0; i < strLen; i++) {
            c = text.charAt(i);
            builder.append(set.contains(c) ? replacedStr : c);
        }
        return builder.toString();
    }

    /**
     * 清理空白字符
     *
     * @param text 被清理的字符串
     * @return 清理后的字符串
     */
    public static String cleanBlank(CharSequence text) {
        return filter(text, c -> !CharsKit.isBlankChar(c));
    }

    /**
     * 包装指定字符串
     * 当前缀和后缀一致时使用此方法
     *
     * @param text            被包装的字符串
     * @param prefixAndSuffix 前缀和后缀
     * @return 包装后的字符串
     */
    public static String wrap(CharSequence text, CharSequence prefixAndSuffix) {
        return wrap(text, prefixAndSuffix, prefixAndSuffix);
    }

    /**
     * 包装指定字符串
     *
     * @param text   被包装的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 包装后的字符串
     */
    public static String wrap(CharSequence text, CharSequence prefix, CharSequence suffix) {
        return nullToEmpty(prefix).concat(nullToEmpty(text)).concat(nullToEmpty(suffix));
    }

    /**
     * 包装多个字符串
     *
     * @param prefixAndSuffix 前缀和后缀
     * @param args            多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAll(CharSequence prefixAndSuffix, CharSequence... args) {
        return wrapAll(prefixAndSuffix, prefixAndSuffix, args);
    }

    /**
     * 包装多个字符串
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @param args   多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAll(CharSequence prefix, CharSequence suffix, CharSequence... args) {
        final String[] results = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            results[i] = wrap(args[i], prefix, suffix);
        }
        return results;
    }

    /**
     * 去掉字符包装,如果未被包装则返回原字符串
     *
     * @param text   字符串
     * @param prefix 前置字符串
     * @param suffix 后置字符串
     * @return 去掉包装字符的字符串
     */
    public static String unWrap(CharSequence text, String prefix, String suffix) {
        if (isWrap(text, prefix, suffix)) {
            return sub(text, prefix.length(), text.length() - suffix.length());
        }
        return text.toString();
    }

    /**
     * 去掉字符包装,如果未被包装则返回原字符串
     *
     * @param text   字符串
     * @param prefix 前置字符
     * @param suffix 后置字符
     * @return 去掉包装字符的字符串
     */
    public static String unWrap(CharSequence text, char prefix, char suffix) {
        if (isEmpty(text)) {
            return toString(text);
        }
        if (text.charAt(0) == prefix && text.charAt(text.length() - 1) == suffix) {
            return sub(text, 1, text.length() - 1);
        }
        return text.toString();
    }

    /**
     * 去掉字符包装,如果未被包装则返回原字符串
     *
     * @param text            字符串
     * @param prefixAndSuffix 前置和后置字符
     * @return 去掉包装字符的字符串
     */
    public static String unWrap(CharSequence text, char prefixAndSuffix) {
        return unWrap(text, prefixAndSuffix, prefixAndSuffix);
    }

    /**
     * 指定字符串是否被包装
     *
     * @param text   字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 是否被包装
     */
    public static boolean isWrap(CharSequence text, String prefix, String suffix) {
        if (ArrayKit.hasNull(text, prefix, suffix)) {
            return false;
        }
        final String str2 = text.toString();
        return str2.startsWith(prefix) && str2.endsWith(suffix);
    }

    /**
     * 指定字符串是否被同一字符包装(前后都有这些字符串)
     *
     * @param text    字符串
     * @param wrapper 包装字符串
     * @return 是否被包装
     */
    public static boolean isWrap(CharSequence text, String wrapper) {
        return isWrap(text, wrapper, wrapper);
    }

    /**
     * 指定字符串是否被同一字符包装(前后都有这些字符串)
     *
     * @param text    字符串
     * @param wrapper 包装字符
     * @return 是否被包装
     */
    public static boolean isWrap(CharSequence text, char wrapper) {
        return isWrap(text, wrapper, wrapper);
    }

    /**
     * 指定字符串是否被包装
     *
     * @param text       字符串
     * @param prefixChar 前缀
     * @param suffixChar 后缀
     * @return 是否被包装
     */
    public static boolean isWrap(CharSequence text, char prefixChar, char suffixChar) {
        if (null == text) {
            return false;
        }

        return text.charAt(0) == prefixChar && text.charAt(text.length() - 1) == suffixChar;
    }

    /**
     * 包装指定字符串，如果前缀或后缀已经包含对应的字符串，则不再包装
     *
     * @param text   被包装的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 包装后的字符串
     */
    public static String wrapIfMissing(CharSequence text, CharSequence prefix, CharSequence suffix) {
        int len = 0;
        if (isNotEmpty(text)) {
            len += text.length();
        }
        if (isNotEmpty(prefix)) {
            len += prefix.length();
        }
        if (isNotEmpty(suffix)) {
            len += suffix.length();
        }
        StringBuilder stringBuilder = new StringBuilder(len);
        if (isNotEmpty(prefix) && false == startWith(text, prefix)) {
            stringBuilder.append(prefix);
        }
        if (isNotEmpty(text)) {
            stringBuilder.append(text);
        }
        if (isNotEmpty(suffix) && false == endWith(text, suffix)) {
            stringBuilder.append(suffix);
        }
        return stringBuilder.toString();
    }

    /**
     * 包装多个字符串，如果已经包装，则不再包装
     *
     * @param prefixAndSuffix 前缀和后缀
     * @param args            多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAllIfMissing(CharSequence prefixAndSuffix, CharSequence... args) {
        return wrapAllIfMissing(prefixAndSuffix, prefixAndSuffix, args);
    }

    /**
     * 包装多个字符串，如果已经包装，则不再包装
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @param args   多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAllIfMissing(CharSequence prefix, CharSequence suffix, CharSequence... args) {
        final String[] results = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            results[i] = wrapIfMissing(args[i], prefix, suffix);
        }
        return results;
    }

    /**
     * 字符串是否以给定字符开始
     *
     * @param text 字符串
     * @param word 字符
     * @return 是否开始
     */
    public static boolean startWith(CharSequence text, char word) {
        if (true == isEmpty(text)) {
            return false;
        }
        return word == text.charAt(0);
    }

    /**
     * 是否以指定字符串开头
     * 如果给定的字符串和开头字符串都为null则返回true,否则任意一个值为null返回false
     *
     * @param text       被监测字符串
     * @param prefix     开头字符串
     * @param ignoreCase 是否忽略大小写
     * @return 是否以指定字符串开头
     */
    public static boolean startWith(CharSequence text, CharSequence prefix, boolean ignoreCase) {
        return startWith(text, prefix, ignoreCase, false);
    }

    /**
     * 是否以指定字符串开头
     * 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     *
     * @param text         被监测字符串
     * @param prefix       开头字符串
     * @param ignoreCase   是否忽略大小写
     * @param ignoreEquals 是否忽略字符串相等的情况
     * @return 是否以指定字符串开头
     */
    public static boolean startWith(CharSequence text, CharSequence prefix, boolean ignoreCase, boolean ignoreEquals) {
        if (null == text || null == prefix) {
            if (ignoreEquals) {
                return false;
            }
            return null == text && null == prefix;
        }

        boolean isStartWith = text.toString()
                .regionMatches(ignoreCase, 0, prefix.toString(), 0, prefix.length());

        if (isStartWith) {
            return (false == ignoreEquals) || (false == equals(text, prefix, ignoreCase));
        }
        return false;
    }

    /**
     * 是否以指定字符串开头
     *
     * @param text   被监测字符串
     * @param prefix 开头字符串
     * @return 是否以指定字符串开头
     */
    public static boolean startWith(CharSequence text, CharSequence prefix) {
        return startWith(text, prefix, false);
    }

    /**
     * 是否以指定字符串开头，忽略相等字符串的情况
     *
     * @param text   被监测字符串
     * @param prefix 开头字符串
     * @return 是否以指定字符串开头并且两个字符串不相等
     */
    public static boolean startWithIgnoreEquals(CharSequence text, CharSequence prefix) {
        return startWith(text, prefix, false, true);
    }

    /**
     * 给定字符串是否以任何一个字符串开始
     * 给定字符串和数组为空都返回false
     *
     * @param text 给定字符串
     * @param args 需要检测的开始字符串
     * @return 给定字符串是否以任何一个字符串开始
     */
    public static boolean startWithAny(CharSequence text, CharSequence... args) {
        if (isEmpty(text) || ArrayKit.isEmpty(args)) {
            return false;
        }

        for (CharSequence suffix : args) {
            if (startWith(text, suffix, false)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 给定字符串是否以任何一个字符串结尾（忽略大小写）
     * 给定字符串和数组为空都返回false
     *
     * @param text 给定字符串
     * @param args 需要检测的结尾字符串
     * @return 给定字符串是否以任何一个字符串结尾
     */
    public static boolean startWithAnyIgnoreCase(final CharSequence text, final CharSequence... args) {
        if (isEmpty(text) || ArrayKit.isEmpty(args)) {
            return false;
        }

        for (final CharSequence suffix : args) {
            if (startWith(text, suffix, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否以指定字符串开头,忽略大小写
     *
     * @param text   被监测字符串
     * @param prefix 开头字符串
     * @return 是否以指定字符串开头
     */
    public static boolean startWithIgnoreCase(CharSequence text, CharSequence prefix) {
        return startWith(text, prefix, true);
    }

    /**
     * 字符串是否以给定字符结尾
     *
     * @param text 字符串
     * @param c    字符
     * @return 是否结尾
     */
    public static boolean endWith(CharSequence text, char c) {
        if (isEmpty(text)) {
            return false;
        }
        return c == text.charAt(text.length() - 1);
    }

    /**
     * 是否以指定字符串结尾
     * 如果给定的字符串和开头字符串都为null则返回true,否则任意一个值为null返回false
     *
     * @param text       被监测字符串
     * @param suffix     结尾字符串
     * @param ignoreCase 是否忽略大小写
     * @return 是否以指定字符串结尾
     */
    public static boolean endWith(CharSequence text, CharSequence suffix, boolean ignoreCase) {
        return endWith(text, suffix, ignoreCase, false);
    }

    /**
     * 是否以指定字符串结尾
     * 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     *
     * @param text         被监测字符串
     * @param suffix       结尾字符串
     * @param ignoreCase   是否忽略大小写
     * @param ignoreEquals 是否忽略字符串相等的情况
     * @return 是否以指定字符串结尾
     */
    public static boolean endWith(CharSequence text, CharSequence suffix, boolean ignoreCase, boolean ignoreEquals) {
        if (null == text || null == suffix) {
            if (ignoreEquals) {
                return false;
            }
            return null == text && null == suffix;
        }

        final int strOffset = text.length() - suffix.length();
        boolean isEndWith = text.toString()
                .regionMatches(ignoreCase, strOffset, suffix.toString(), 0, suffix.length());

        if (isEndWith) {
            return (false == ignoreEquals) || (false == equals(text, suffix, ignoreCase));
        }
        return false;
    }


    /**
     * 是否以指定字符串结尾
     *
     * @param text   被监测字符串
     * @param suffix 结尾字符串
     * @return 是否以指定字符串结尾
     */
    public static boolean endWith(CharSequence text, CharSequence suffix) {
        return endWith(text, suffix, false);
    }

    /**
     * 给定字符串是否以任何一个字符串结尾
     * 给定字符串和数组为空都返回false
     *
     * @param text 给定字符串
     * @param args 需要检测的结尾字符串
     * @return 给定字符串是否以任何一个字符串结尾
     */
    public static boolean endWithAny(CharSequence text, CharSequence... args) {
        if (isEmpty(text) || ArrayKit.isEmpty(args)) {
            return false;
        }

        for (CharSequence suffix : args) {
            if (endWith(text, suffix, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 给定字符串是否以任何一个字符串结尾（忽略大小写）
     * 给定字符串和数组为空都返回false
     *
     * @param text 给定字符串
     * @param args 需要检测的结尾字符串
     * @return 给定字符串是否以任何一个字符串结尾
     */
    public static boolean endWithAnyIgnoreCase(CharSequence text, CharSequence... args) {
        if (isEmpty(text) || ArrayKit.isEmpty(args)) {
            return false;
        }

        for (CharSequence suffix : args) {
            if (endWith(text, suffix, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否以指定字符串结尾,忽略大小写
     *
     * @param text   被监测字符串
     * @param suffix 结尾字符串
     * @return 是否以指定字符串结尾
     */
    public static boolean endWithIgnoreCase(CharSequence text, CharSequence suffix) {
        return endWith(text, suffix, true);
    }

    /**
     * 去除两边的指定字符串
     *
     * @param text           被处理的字符串
     * @param prefixOrSuffix 前缀或后缀
     * @return 处理后的字符串
     */
    public static String strip(CharSequence text, CharSequence prefixOrSuffix) {
        return strip(text, prefixOrSuffix, prefixOrSuffix);
    }

    /**
     * 去除两边的指定字符串
     *
     * @param text   被处理的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 处理后的字符串
     */
    public static String strip(CharSequence text, CharSequence prefix, CharSequence suffix) {
        if (isEmpty(text)) {
            return toString(text);
        }
        int from = 0;
        int to = text.length();

        String value = text.toString();
        if (startWith(value, prefix)) {
            from = prefix.length();
        }
        if (endWith(value, suffix)) {
            to -= suffix.length();
        }
        return value.substring(from, to);
    }

    /**
     * 去除两边的指定字符串,忽略大小写
     *
     * @param text           被处理的字符串
     * @param prefixOrSuffix 前缀或后缀
     * @return 处理后的字符串
     */
    public static String stripIgnoreCase(CharSequence text, CharSequence prefixOrSuffix) {
        return stripIgnoreCase(text, prefixOrSuffix, prefixOrSuffix);
    }

    /**
     * 去除两边的指定字符串,忽略大小写
     *
     * @param text   被处理的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 处理后的字符串
     */
    public static String stripIgnoreCase(CharSequence text, CharSequence prefix, CharSequence suffix) {
        if (isEmpty(text)) {
            return toString(text);
        }
        int from = 0;
        int to = text.length();

        String value = text.toString();
        if (startWithIgnoreCase(value, prefix)) {
            from = prefix.length();
        }
        if (endWithIgnoreCase(value, suffix)) {
            to -= suffix.length();
        }
        return value.substring(from, to);
    }

    /**
     * 如果给定字符串不是以prefix开头的,在开头补充 prefix
     *
     * @param text   字符串
     * @param prefix 前缀
     * @return 补充后的字符串
     */
    public static String addPrefixIfNot(CharSequence text, CharSequence prefix) {
        if (isEmpty(text) || isEmpty(prefix)) {
            return toString(text);
        }

        final String value = text.toString();
        final String xSuffix = prefix.toString();
        if (false == value.startsWith(xSuffix)) {
            return xSuffix.concat(value);
        }
        return value;
    }

    /**
     * 如果给定字符串不是以suffix结尾的,在尾部补充 suffix
     *
     * @param text   字符串
     * @param suffix 后缀
     * @return 补充后的字符串
     */
    public static String addSuffixIfNot(CharSequence text, CharSequence suffix) {
        if (isEmpty(text) || isEmpty(suffix)) {
            return toString(text);
        }

        final String value = text.toString();
        final String xSuffix = suffix.toString();
        if (false == value.endsWith(xSuffix)) {
            return value.concat(xSuffix);
        }
        return value;
    }

    /**
     * 指定字符是否在字符串中出现过
     *
     * @param text 字符串
     * @param word 被查找的字符
     * @return 是否包含
     */
    public static boolean contains(CharSequence text, char word) {
        return indexOf(text, word) > -1;
    }

    /**
     * 指定字符串是否在字符串中出现过
     *
     * @param text 字符串
     * @param word 被查找的字符串
     * @return 是否包含
     */
    public static boolean contains(CharSequence text, CharSequence word) {
        if (null == text || null == word) {
            return false;
        }
        return text.toString().contains(word);
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串
     *
     * @param text     指定字符串
     * @param testStrs 需要检查的字符串数组
     * @return 是否包含任意一个字符串
     */
    public static boolean containsAny(CharSequence text, CharSequence... testStrs) {
        return null != getContainsAny(text, testStrs);
    }

    /**
     * 查找指定字符串是否包含指定字符列表中的任意一个字符
     *
     * @param text      指定字符串
     * @param testChars 需要检查的字符数组
     * @return 是否包含任意一个字符
     */
    public static boolean containsAny(CharSequence text, char... testChars) {
        if (false == isEmpty(text)) {
            int len = text.length();
            for (int i = 0; i < len; i++) {
                if (ArrayKit.contains(testChars, text.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查指定字符串中是否只包含给定的字符
     *
     * @param text      字符串
     * @param testChars 检查的字符
     * @return 字符串含有非检查的字符, 返回false
     */
    public static boolean containsOnly(CharSequence text, char... testChars) {
        if (false == isEmpty(text)) {
            int len = text.length();
            for (int i = 0; i < len; i++) {
                if (false == ArrayKit.contains(testChars, text.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查指定字符串中是否含给定的所有字符串
     *
     * @param text 字符串
     * @param args 检查的字符
     * @return 字符串含有非检查的字符，返回false
     */
    public static boolean containsAll(CharSequence text, CharSequence... args) {
        if (isBlank(text) || ArrayKit.isEmpty(args)) {
            return false;
        }
        for (CharSequence value : args) {
            if (false == contains(text, value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 给定字符串是否包含空白符(空白符包括空格、制表符、全角空格和不间断空格)
     * 如果给定字符串为null或者"",则返回false
     *
     * @param text 字符串
     * @return 是否包含空白符
     */
    public static boolean containsBlank(CharSequence text) {
        if (null == text) {
            return false;
        }
        final int length = text.length();
        if (0 == length) {
            return false;
        }

        for (int i = 0; i < length; i += 1) {
            if (CharsKit.isBlankChar(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串,如果包含返回找到的第一个字符串
     *
     * @param text 指定字符串
     * @param args 需要检查的字符串数组
     * @return 被包含的第一个字符串
     */
    public static String getContainsAny(CharSequence text, CharSequence... args) {
        if (isEmpty(text) || ArrayKit.isEmpty(args)) {
            return null;
        }
        for (CharSequence val : args) {
            if (val.toString().contains(text)) {
                return val.toString();
            }
        }
        return null;
    }

    /**
     * 是否包含特定字符,忽略大小写,如果给定两个参数都为<code>null</code>,返回true
     *
     * @param text 被检测字符串
     * @param word 被测试是否包含的字符串
     * @return 是否包含
     */
    public static boolean containsIgnoreCase(CharSequence text, CharSequence word) {
        if (null == text) {
            // 如果被监测字符串和
            return null == word;
        }
        return indexOfIgnoreCase(text, word) > -1;
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串
     * 忽略大小写
     *
     * @param text 指定字符串
     * @param args 需要检查的字符串数组
     * @return 是否包含任意一个字符串
     */
    public static boolean containsAnyIgnoreCase(CharSequence text, CharSequence... args) {
        return null != getContainsStrIgnoreCase(text, args);
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串,如果包含返回找到的第一个字符串
     * 忽略大小写
     *
     * @param text 指定字符串
     * @param args 需要检查的字符串数组
     * @return 被包含的第一个字符串
     */
    public static String getContainsStrIgnoreCase(CharSequence text, CharSequence... args) {
        if (isEmpty(text) || ArrayKit.isEmpty(args)) {
            return null;
        }
        for (CharSequence value : args) {
            if (containsIgnoreCase(text, value)) {
                return value.toString();
            }
        }
        return null;
    }

    /**
     * 给定字符串是否被字符包围
     *
     * @param text   字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 是否包围, 空串不包围
     */
    public static boolean isSurround(CharSequence text, CharSequence prefix, CharSequence suffix) {
        if (isBlank(text)) {
            return false;
        }
        if (text.length() < (prefix.length() + suffix.length())) {
            return false;
        }

        final String str2 = text.toString();
        return str2.startsWith(prefix.toString()) && str2.endsWith(suffix.toString());
    }

    /**
     * 给定字符串是否被字符包围
     *
     * @param text   字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 是否包围, 空串不包围
     */
    public static boolean isSurround(CharSequence text, char prefix, char suffix) {
        if (isBlank(text)) {
            return false;
        }
        if (text.length() < 2) {
            return false;
        }

        return text.charAt(0) == prefix && text.charAt(text.length() - 1) == suffix;
    }

    /**
     * 给定字符串中的字母是否全部为大写,判断依据如下：
     *
     * <pre>
     * 1. 大写字母包括A-Z
     * 2. 其它非字母的Unicode符都算作大写
     * </pre>
     *
     * @param text 被检查的字符串
     * @return 是否全部为大写
     */
    public static boolean isUpperCase(CharSequence text) {
        if (null == text) {
            return false;
        }
        final int len = text.length();
        for (int i = 0; i < len; i++) {
            if (Character.isLowerCase(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 给定字符串中的字母是否全部为小写,判断依据如下：
     *
     * <pre>
     * 1. 小写字母包括a-z
     * 2. 其它非字母的Unicode符都算作小写
     * </pre>
     *
     * @param text 被检查的字符串
     * @return 是否全部为小写
     */
    public static boolean isLowerCase(CharSequence text) {
        if (null == text) {
            return false;
        }
        final int len = text.length();
        for (int i = 0; i < len; i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 给定字符串转为bytes后的byte数(byte长度)
     *
     * @param cs      字符串
     * @param charset 编码
     * @return byte长度
     */
    public static int byteLength(CharSequence cs, java.nio.charset.Charset charset) {
        return null == cs ? 0 : cs.toString().getBytes(charset).length;
    }


    /**
     * 将字符串切分为N等份
     *
     * @param text       字符串
     * @param partLength 每等份的长度
     * @return 切分后的数组
     */
    public static String[] cut(CharSequence text, int partLength) {
        if (null == text) {
            return null;
        }
        int len = text.length();
        if (len < partLength) {
            return new String[]{text.toString()};
        }
        int part = MathKit.count(len, partLength);
        final String[] array = new String[part];

        final String str2 = text.toString();
        for (int i = 0; i < part; i++) {
            array[i] = str2.substring(i * partLength, (i == part - 1) ? len : (partLength + i * partLength));
        }
        return array;
    }

    /**
     * 将给定字符串,变成 "xxx...xxx" 形式的字符串
     *
     * <ul>
     *     <li>abcdef 5 - a...f</li>
     *     <li>abcdef 4 - a..f</li>
     *     <li>abcdef 3 - a.f</li>
     *     <li>abcdef 2 - a.</li>
     *     <li>abcdef 1 - a</li>
     * </ul>
     *
     * @param text      字符串
     * @param maxLength 最大长度
     * @return 截取后的字符串
     */
    public static String brief(CharSequence text, int maxLength) {
        if (null == text) {
            return null;
        }
        final int strLength = text.length();
        if (maxLength <= 0 || strLength <= maxLength) {
            return text.toString();
        }

        switch (maxLength) {
            case 1:
                return String.valueOf(text.charAt(0));
            case 2:
                return text.charAt(0) + Symbol.DOT;
            case 3:
                return text.charAt(0) + Symbol.DOT + text.charAt(strLength - 1);
            case 4:
                return text.charAt(0) + Symbol.DOUBLE_DOT + text.charAt(strLength - 1);
        }

        final int suffixLength = (maxLength - 3) / 2;
        final int preLength = suffixLength + (maxLength - 3) % 2;
        final String str2 = text.toString();
        return format("{}...{}",
                str2.substring(0, preLength),
                str2.substring(strLength - suffixLength));
    }

    /**
     * 如果给定字符串 {@code text} 中不包含 {@code suffix}，则在{@code text} 后追加{@code suffix}
     * 如果已包含 {@code suffix}，则在 {@code text} 后追加 {@code args}
     *
     * @param text   给定的字符串
     * @param suffix 需要追加的内容
     * @param args   当{@code suffix}不满足时追加到{@code text}后的内容
     * @return 追加后的字符串
     */
    public static String appendIfMissing(final String text,
                                         final String suffix,
                                         final String args) {
        if (StringKit.isEmpty(text) || StringKit.isEmpty(suffix)) {
            return text;
        }
        if (text.contains(suffix)) {
            return text.concat(args);
        }
        return text.concat(suffix);
    }

    /**
     * 如果字符串还没有以后缀结尾，则将后缀追加到字符串的末尾
     *
     * @param text       字符串.
     * @param suffix     附加到字符串末尾的后缀
     * @param ignoreCase 指示比较是否应忽略大小写
     * @param args       有效终止符的附加后缀(可选)
     * @return 如果添加了后缀，则为新字符串，否则为相同的字符
     */
    private static String appendIfMissing(final String text,
                                          final CharSequence suffix,
                                          final boolean ignoreCase,
                                          final CharSequence... args) {
        if (null == text || isEmpty(suffix) || endWith(text, suffix, ignoreCase)) {
            return toString(text);
        }
        if (null != args && args.length > 0) {
            for (final CharSequence s : args) {
                if (endWith(text, s, ignoreCase)) {
                    return text;
                }
            }
        }
        return text.concat(suffix.toString());
    }


    /**
     * 如果字符串还没有以任何后缀结尾，则将后缀追加到字符串的末尾
     *
     * <pre>
     * appendIfMissing(null, null) = null
     * appendIfMissing("abc", null) = "abc"
     * appendIfMissing("", "xyz") = "xyz"
     * appendIfMissing("abc", "xyz") = "abcxyz"
     * appendIfMissing("abcxyz", "xyz") = "abcxyz"
     * appendIfMissing("abcXYZ", "xyz") = "abcXYZxyz"
     * </pre>
     * <p>With additional suffixes,</p>
     * <pre>
     * appendIfMissing(null, null, null) = null
     * appendIfMissing("abc", null, null) = "abc"
     * appendIfMissing("", "xyz", null) = "xyz"
     * appendIfMissing("abc", "xyz", new CharSequence[]{null}) = "abcxyz"
     * appendIfMissing("abc", "xyz", "") = "abc"
     * appendIfMissing("abc", "xyz", "mno") = "abcxyz"
     * appendIfMissing("abcxyz", "xyz", "mno") = "abcxyz"
     * appendIfMissing("abcmno", "xyz", "mno") = "abcmno"
     * appendIfMissing("abcXYZ", "xyz", "mno") = "abcXYZxyz"
     * appendIfMissing("abcMNO", "xyz", "mno") = "abcMNOxyz"
     * </pre>
     *
     * @param text   字符串
     * @param suffix 附加到字符串末尾的后缀
     * @param args   有效终止符的附加后缀(可选)
     * @return 如果添加了后缀，则为新字符串，否则为相同的字符串
     */
    public static String appendIfMissing(final String text,
                                         final CharSequence suffix,
                                         final CharSequence... args) {
        return appendIfMissing(text, suffix, false, args);
    }

    /**
     * 如果字符串还没有结束，则使用任何后缀将后缀追加到字符串的末尾，不区分大小写.
     *
     * <pre>
     * appendIfMissingIgnoreCase(null, null) = null
     * appendIfMissingIgnoreCase("abc", null) = "abc"
     * appendIfMissingIgnoreCase("", "xyz") = "xyz"
     * appendIfMissingIgnoreCase("abc", "xyz") = "abcxyz"
     * appendIfMissingIgnoreCase("abcxyz", "xyz") = "abcxyz"
     * appendIfMissingIgnoreCase("abcXYZ", "xyz") = "abcXYZ"
     * </pre>
     * <p>With additional suffixes,</p>
     * <pre>
     * appendIfMissingIgnoreCase(null, null, null) = null
     * appendIfMissingIgnoreCase("abc", null, null) = "abc"
     * appendIfMissingIgnoreCase("", "xyz", null) = "xyz"
     * appendIfMissingIgnoreCase("abc", "xyz", new CharSequence[]{null}) = "abcxyz"
     * appendIfMissingIgnoreCase("abc", "xyz", "") = "abc"
     * appendIfMissingIgnoreCase("abc", "xyz", "mno") = "axyz"
     * appendIfMissingIgnoreCase("abcxyz", "xyz", "mno") = "abcxyz"
     * appendIfMissingIgnoreCase("abcmno", "xyz", "mno") = "abcmno"
     * appendIfMissingIgnoreCase("abcXYZ", "xyz", "mno") = "abcXYZ"
     * appendIfMissingIgnoreCase("abcMNO", "xyz", "mno") = "abcMNO"
     * </pre>
     *
     * @param text   字符串
     * @param suffix 附加到字符串末尾的后缀
     * @param args   有效终止符的附加后缀(可选)
     * @return 如果添加了后缀，则为新字符串，否则为相同的字符串
     */
    public static String appendIfMissingIgnoreCase(final String text,
                                                   final CharSequence suffix,
                                                   final CharSequence... args) {
        return appendIfMissing(text, suffix, true, args);
    }

    /**
     * 如果字符串还没有以任何前缀开始，则将前缀添加到字符串的开头
     *
     * @param text       字符串
     * @param prefix     在字符串开始前的前缀
     * @param ignoreCase 指示比较是否应忽略大小写
     * @param args       有效的附加前缀(可选)
     * @return 如果前缀是前缀，则为新字符串，否则为相同的字符串
     */
    private static String prependIfMissing(final String text,
                                           final CharSequence prefix,
                                           final boolean ignoreCase,
                                           final CharSequence... args) {
        if (null == text || isEmpty(prefix) || startWith(text, prefix, ignoreCase)) {
            return toString(text);
        }
        if (null != args && args.length > 0) {
            for (final CharSequence s : args) {
                if (startWith(text, s, ignoreCase)) {
                    return text;
                }
            }
        }
        return prefix.toString().concat(text);
    }

    /**
     * 如果字符串还没有以任何前缀开始，则将前缀添加到字符串的开头
     *
     * <pre>
     * prependIfMissing(null, null) = null
     * prependIfMissing("abc", null) = "abc"
     * prependIfMissing("", "xyz") = "xyz"
     * prependIfMissing("abc", "xyz") = "xyzabc"
     * prependIfMissing("xyzabc", "xyz") = "xyzabc"
     * prependIfMissing("XYZabc", "xyz") = "xyzXYZabc"
     * </pre>
     * <p>With additional prefixes,</p>
     * <pre>
     * prependIfMissing(null, null, null) = null
     * prependIfMissing("abc", null, null) = "abc"
     * prependIfMissing("", "xyz", null) = "xyz"
     * prependIfMissing("abc", "xyz", new CharSequence[]{null}) = "xyzabc"
     * prependIfMissing("abc", "xyz", "") = "abc"
     * prependIfMissing("abc", "xyz", "mno") = "xyzabc"
     * prependIfMissing("xyzabc", "xyz", "mno") = "xyzabc"
     * prependIfMissing("mnoabc", "xyz", "mno") = "mnoabc"
     * prependIfMissing("XYZabc", "xyz", "mno") = "xyzXYZabc"
     * prependIfMissing("MNOabc", "xyz", "mno") = "xyzMNOabc"
     * </pre>
     *
     * @param text   T字符串
     * @param prefix 在字符串开始前的前缀
     * @param args   有效的附加前缀(可选)
     * @return 如果前缀是前缀，则为新字符串，否则为相同的字符串
     */
    public static String prependIfMissing(final String text,
                                          final CharSequence prefix,
                                          final CharSequence... args) {
        return prependIfMissing(text, prefix, false, args);
    }

    /**
     * 如果字符串尚未开始，则将前缀添加到字符串的开头，不区分大小写，并使用任何前缀
     *
     * <pre>
     * prependIfMissingIgnoreCase(null, null) = null
     * prependIfMissingIgnoreCase("abc", null) = "abc"
     * prependIfMissingIgnoreCase("", "xyz") = "xyz"
     * prependIfMissingIgnoreCase("abc", "xyz") = "xyzabc"
     * prependIfMissingIgnoreCase("xyzabc", "xyz") = "xyzabc"
     * prependIfMissingIgnoreCase("XYZabc", "xyz") = "XYZabc"
     * </pre>
     * <p>With additional prefixes,</p>
     * <pre>
     * prependIfMissingIgnoreCase(null, null, null) = null
     * prependIfMissingIgnoreCase("abc", null, null) = "abc"
     * prependIfMissingIgnoreCase("", "xyz", null) = "xyz"
     * prependIfMissingIgnoreCase("abc", "xyz", new CharSequence[]{null}) = "xyzabc"
     * prependIfMissingIgnoreCase("abc", "xyz", "") = "abc"
     * prependIfMissingIgnoreCase("abc", "xyz", "mno") = "xyzabc"
     * prependIfMissingIgnoreCase("xyzabc", "xyz", "mno") = "xyzabc"
     * prependIfMissingIgnoreCase("mnoabc", "xyz", "mno") = "mnoabc"
     * prependIfMissingIgnoreCase("XYZabc", "xyz", "mno") = "XYZabc"
     * prependIfMissingIgnoreCase("MNOabc", "xyz", "mno") = "MNOabc"
     * </pre>
     *
     * @param text   字符串
     * @param prefix 在字符串开始前的前缀
     * @param args   有效的附加前缀(可选)
     * @return 如果前缀是前缀，则为新字符串，否则为相同的字符串
     */
    public static String prependIfMissingIgnoreCase(final String text,
                                                    final CharSequence prefix,
                                                    final CharSequence... args) {
        return prependIfMissing(text, prefix, true, args);
    }

    /**
     * 限制字符串长度，如果超过指定长度，截取指定长度并在末尾加"..."
     *
     * @param string 字符串
     * @param length 最大长度
     * @return 切割后的剩余的前半部分字符串+"..."
     */
    public static String maxLength(CharSequence string, int length) {
        Assert.isTrue(length > 0);
        if (null == string) {
            return null;
        }
        if (string.length() <= length) {
            return string.toString();
        }
        return sub(string, 0, length) + "...";
    }

    /**
     * 居中字符串，两边补充指定字符串，如果指定长度小于字符串，则返回原字符串
     *
     * <pre>
     * center(null, *)   = null
     * center("", 4)     = "    "
     * center("ab", -1)  = "ab"
     * center("ab", 4)   = " ab "
     * center("abcd", 2) = "abcd"
     * center("a", 4)    = " a  "
     * </pre>
     *
     * @param text 字符串
     * @param size 指定长度
     * @return 补充后的字符串
     */
    public static String center(CharSequence text, final int size) {
        return center(text, size, Symbol.C_SPACE);
    }

    /**
     * 居中字符串，两边补充指定字符串，如果指定长度小于字符串，则返回原字符串
     *
     * <pre>
     * center(null, *, *)     = null
     * center("", 4, ' ')     = "    "
     * center("ab", -1, ' ')  = "ab"
     * center("ab", 4, ' ')   = " ab "
     * center("abcd", 2, ' ') = "abcd"
     * center("a", 4, ' ')    = " a  "
     * center("a", 4, 'y')   = "yayy"
     * center("abc", 7, ' ')   = "  abc  "
     * </pre>
     *
     * @param text    字符串
     * @param size    指定长度
     * @param padChar 两边补充的字符
     * @return 补充后的字符串
     */
    public static String center(CharSequence text, final int size, char padChar) {
        if (null == text || size <= 0) {
            return toString(text);
        }
        final int strLen = text.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return text.toString();
        }
        text = padPre(text, strLen + pads / 2, padChar);
        text = padAfter(text, size, padChar);
        return text.toString();
    }

    /**
     * 居中字符串，两边补充指定字符串，如果指定长度小于字符串，则返回原字符串
     *
     * <pre>
     * center(null, *, *)     = null
     * center("", 4, " ")     = "    "
     * center("ab", -1, " ")  = "ab"
     * center("ab", 4, " ")   = " ab "
     * center("abcd", 2, " ") = "abcd"
     * center("a", 4, " ")    = " a  "
     * center("a", 4, "yz")   = "yayz"
     * center("abc", 7, null) = "  abc  "
     * center("abc", 7, "")   = "  abc  "
     * </pre>
     *
     * @param text    字符串
     * @param size    指定长度
     * @param padText 两边补充的字符串
     * @return 补充后的字符串
     */
    public static String center(CharSequence text, final int size, CharSequence padText) {
        if (null == text || size <= 0) {
            return toString(text);
        }
        if (isEmpty(padText)) {
            padText = Symbol.SPACE;
        }
        final int strLen = text.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return text.toString();
        }
        text = padPre(text, strLen + pads / 2, padText);
        text = padAfter(text, size, padText);
        return text.toString();
    }

    /**
     * 补充字符串以满足最小长度
     *
     * <pre>
     * padPre(null, *, *);//null
     * padPre("1", 3, "ABC");//"AB1"
     * padPre("123", 2, "ABC");//"12"
     * </pre>
     *
     * @param text      字符串
     * @param minLength 最小长度
     * @param padText   补充的字符
     * @return 补充后的字符串
     */
    public static String padPre(CharSequence text, int minLength, CharSequence padText) {
        if (null == text) {
            return null;
        }
        final int strLen = text.length();
        if (strLen == minLength) {
            return text.toString();
        } else if (strLen > minLength) {
            return subPre(text, minLength);
        }

        return repeatByLength(padText, minLength - strLen).concat(text.toString());
    }

    /**
     * 补充字符串以满足最小长度
     *
     * <pre>
     * padPre(null, *, *);//null
     * padPre("1", 3, '0');//"001"
     * padPre("123", 2, '0');//"12"
     * </pre>
     *
     * @param text      字符串
     * @param minLength 最小长度
     * @param padChar   补充的字符
     * @return 补充后的字符串
     */
    public static String padPre(CharSequence text, int minLength, char padChar) {
        if (null == text) {
            return null;
        }
        final int strLen = text.length();
        if (strLen == minLength) {
            return text.toString();
        } else if (strLen > minLength) {
            return subPre(text, minLength);
        }

        return repeat(padChar, minLength - strLen).concat(text.toString());
    }

    /**
     * 补充字符串以满足最小长度
     *
     * <pre>
     * padAfter(null, *, *);//null
     * padAfter("1", 3, '0');//"100"
     * padAfter("123", 2, '0');//"23"
     * </pre>
     *
     * @param text      字符串，如果为<code>null</code>，直接返回null
     * @param minLength 最小长度
     * @param padChar   补充的字符
     * @return 补充后的字符串
     */
    public static String padAfter(CharSequence text, int minLength, char padChar) {
        if (null == text) {
            return null;
        }
        final int strLen = text.length();
        if (strLen == minLength) {
            return text.toString();
        } else if (strLen > minLength) {
            return sub(text, strLen - minLength, strLen);
        }

        return text.toString().concat(repeat(padChar, minLength - strLen));
    }

    /**
     * 补充字符串以满足最小长度
     *
     * <pre>
     * padAfter(null, *, *);//null
     * padAfter("1", 3, "ABC");//"1AB"
     * padAfter("123", 2, "ABC");//"23"
     * </pre>
     *
     * @param text      字符串，如果为<code>null</code>，直接返回null
     * @param minLength 最小长度
     * @param padText   补充的字符
     * @return 补充后的字符串
     */
    public static String padAfter(CharSequence text, int minLength, CharSequence padText) {
        if (null == text) {
            return null;
        }
        final int strLen = text.length();
        if (strLen == minLength) {
            return text.toString();
        } else if (strLen > minLength) {
            // 如果提供的字符串大于指定长度，截断之
            return subSufByLength(text, minLength);
        }

        return text.toString().concat(repeatByLength(padText, minLength - strLen));
    }

    /**
     * 字符串指定位置的字符是否与给定字符相同
     * 如果字符串为null，返回false
     * 如果给定的位置大于字符串长度，返回false
     * 如果给定的位置小于0，返回false
     *
     * @param text     字符串
     * @param position 位置
     * @param c        需要对比的字符
     * @return 字符串指定位置的字符是否与给定字符相同
     */
    public static boolean equalsCharAt(CharSequence text, int position, char c) {
        if (null == text || position < 0) {
            return false;
        }
        return text.length() > position && c == text.charAt(position);
    }

    /**
     * 过滤字符串
     *
     * @param text   字符串
     * @param filter 过滤器
     * @return 过滤后的字符串
     */
    public static String filter(CharSequence text, Predicate<Character> filter) {
        if (null == text || null == filter) {
            return toString(text);
        }

        int len = text.length();
        final StringBuilder sb = new StringBuilder(len);
        char c;
        for (int i = 0; i < len; i++) {
            c = text.charAt(i);
            if (filter.test(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 创建StringBuilder对象
     *
     * @return StringBuilder对象
     */
    public static StringBuilder builder() {
        return new StringBuilder();
    }

    /**
     * 创建StringBuilder对象
     *
     * @param capacity 初始大小
     * @return StringBuilder对象
     */
    public static StringBuilder builder(final int capacity) {
        return new StringBuilder(capacity);
    }

    /**
     * 创建StringBuilder对象
     *
     * @param args 初始字符串列表
     * @return StringBuilder对象
     */
    public static StringBuilder builder(CharSequence... args) {
        final StringBuilder sb = new StringBuilder();
        for (CharSequence text : args) {
            sb.append(text);
        }
        return sb;
    }

    /**
     * 获取字符串的长度,如果为null返回0
     *
     * @param text 字符串
     * @return 字符串的长度, 如果为null返回0
     */
    public static int length(final CharSequence text) {
        return null == text ? 0 : text.length();
    }

    /**
     * 返回字符串 word 在字符串 text 中第 ordinal 次出现的位置
     * 如果 text=null 或 word=null 或 ordinal小于等于0 则返回-1
     *
     * <pre>
     * ordinalIndexOf(null, *, *)          = -1
     * ordinalIndexOf(*, null, *)          = -1
     * ordinalIndexOf("", "", *)           = 0
     * ordinalIndexOf("aabaabaa", "a", 1)  = 0
     * ordinalIndexOf("aabaabaa", "a", 2)  = 1
     * ordinalIndexOf("aabaabaa", "b", 1)  = 2
     * ordinalIndexOf("aabaabaa", "b", 2)  = 5
     * ordinalIndexOf("aabaabaa", "ab", 1) = 1
     * ordinalIndexOf("aabaabaa", "ab", 2) = 4
     * ordinalIndexOf("aabaabaa", "", 1)   = 0
     * ordinalIndexOf("aabaabaa", "", 2)   = 0
     * </pre>
     *
     * @param text    被检查的字符串,可以为null
     * @param word    被查找的字符串,可以为null
     * @param ordinal 第几次出现的位置
     * @return 查找到的位置
     */
    public static int ordinalIndexOf(CharSequence text, CharSequence word, int ordinal) {
        if (null == text || null == word || ordinal <= 0) {
            return Normal.__1;
        }
        if (word.length() == 0) {
            return 0;
        }
        int found = 0;
        int index = Normal.__1;
        do {
            index = indexOf(text, word, index + 1, false);
            if (index < 0) {
                return index;
            }
            found++;
        } while (found < ordinal);
        return index;
    }

    /**
     * {@link CharSequence} 转为字符串,null安全
     *
     * @param text {@link CharSequence}
     * @return 字符串
     */
    public static String toString(CharSequence text) {
        return null == text ? null : text.toString();
    }

    /**
     * 字符转为字符串
     * 如果为ASCII字符,使用缓存
     *
     * @param text 字符
     * @return 字符串
     */
    public static String toString(char text) {
        return ASCIICache.toString(text);
    }

    /**
     * 检查CharSequence是否以提供的大小写敏感的后缀结尾.
     *
     * <pre>
     * endsWithAny(null, null)      = false
     * endsWithAny(null, new String[] {"abc"})  = false
     * endsWithAny("abcxyz", null)     = false
     * endsWithAny("abcxyz", new String[] {""}) = true
     * endsWithAny("abcxyz", new String[] {"xyz"}) = true
     * endsWithAny("abcxyz", new String[] {null, "xyz", "abc"}) = true
     * endsWithAny("abcXYZ", "def", "XYZ") = true
     * endsWithAny("abcXYZ", "def", "xyz") = false
     * </pre>
     *
     * @param text 要检查的CharSequence可能为空
     * @param args 要查找的区分大小写的字符序列可以是空的，也可以包含{@code null}
     * @return {如果输入{@code sequence}是{@code null}， 并且没有提供{@code searchstring}，
     * 或者输入{@code sequence}以提供的区分大小写的{@code searchstring}结尾.
     */
    public static boolean endsWithAny(final CharSequence text, final CharSequence... args) {
        if (isEmpty(text) || ArrayKit.isEmpty(args)) {
            return false;
        }
        for (final CharSequence val : args) {
            if (endWith(text, val)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回第一个非{@code null}元素
     *
     * @param args 多个元素
     * @param <T>  元素类型
     * @return 第一个非空元素，如果给定的数组为空或者都为空，返回{@code null}
     */
    public static <T extends CharSequence> T firstNonNull(T... args) {
        return ArrayKit.firstNonNull(args);
    }

    /**
     * 返回第一个非empty元素
     *
     * @param args 多个元素
     * @param <T>  元素类型
     * @return 第一个非空元素，如果给定的数组为空或者都为空，返回{@code null}
     * @see #isNotEmpty(CharSequence)
     */
    public static <T extends CharSequence> T firstNonEmpty(T... args) {
        return ArrayKit.firstNonNull(CharsKit::isNotEmpty, args);
    }

    /**
     * 返回第一个非blank 元素
     *
     * @param args 多个元素
     * @param <T>  元素类型
     * @return 第一个非空元素，如果给定的数组为空或者都为空，返回{@code null}
     * @see #isNotBlank(CharSequence)
     */
    public static <T extends CharSequence> T firstNonBlank(T... args) {
        return ArrayKit.firstNonNull(CharsKit::isNotBlank, args);
    }

}
