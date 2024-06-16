/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.text;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.CollKit;

import java.util.function.Predicate;

/**
 * 字符串检查工具类，提供字符串的blank和empty等检查
 * <ul>
 *     <li>empty定义：{@code null} or 空字符串：{@code ""}</li>
 *     <li>blank定义：{@code null} or 空字符串：{@code ""} or 空格、全角空格、制表符、换行符，等不可见字符</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringValidator {

    /**
     * <p>字符串是否为空白，空白的定义如下：</p>
     * <ol>
     *     <li>{@code null}</li>
     *     <li>空字符串：{@code ""}</li>
     *     <li>空格、全角空格、制表符、换行符，等不可见字符</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StringKit.isBlank(null)     // true}</li>
     *     <li>{@code StringKit.isBlank("")       // true}</li>
     *     <li>{@code StringKit.isBlank(" \t\n")  // true}</li>
     *     <li>{@code StringKit.isBlank("abc")    // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isEmpty(CharSequence)} 的区别是：
     * 该方法会校验空白字符，且性能相对于 {@link #isEmpty(CharSequence)} 略慢。</p>
     *
     *
     * <p>建议：</p>
     * <ul>
     *     <li>该方法建议仅对于客户端（或第三方接口）传入的参数使用该方法。</li>
     *     <li>需要同时校验多个字符串时，建议采用 {@link ArrayKit#hasBlank(CharSequence...)} 或 {@link ArrayKit#isAllBlank(CharSequence...)}</li>
     * </ul>
     *
     * @param text 被检测的字符串
     * @return 若为空白，则返回 true
     * @see #isEmpty(CharSequence)
     */
    public static boolean isBlank(final CharSequence text) {
        final int length;

        if ((text == null) || ((length = text.length()) == 0)) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            // 只要有一个非空字符即为非空字符串
            if (!CharKit.isBlankChar(text.charAt(i))) {
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
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StringKit.isNotBlank(null)     // false}</li>
     *     <li>{@code StringKit.isNotBlank("")       // false}</li>
     *     <li>{@code StringKit.isNotBlank(" \t\n")  // false}</li>
     *     <li>{@code StringKit.isNotBlank("abc")    // true}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isNotEmpty(CharSequence)} 的区别是：
     * 该方法会校验空白字符，且性能相对于 {@link #isNotEmpty(CharSequence)} 略慢。</p>
     * <p>建议：仅对于客户端（或第三方接口）传入的参数使用该方法。</p>
     *
     * @param text 被检测的字符串
     * @return 是否为非空
     * @see #isBlank(CharSequence)
     */
    public static boolean isNotBlank(final CharSequence text) {
        final int length;

        if ((text == null) || ((length = text.length()) == 0)) {
            // empty
            return false;
        }

        for (int i = 0; i < length; i++) {
            // 只要有一个非空字符即为非空字符串
            if (!CharKit.isBlankChar(text.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    /**
     * <p>字符串是否为空，空的定义如下：</p>
     * <ol>
     *     <li>{@code null}</li>
     *     <li>空字符串：{@code ""}</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StringKit.isEmpty(null)     // true}</li>
     *     <li>{@code StringKit.isEmpty("")       // true}</li>
     *     <li>{@code StringKit.isEmpty(" \t\n")  // false}</li>
     *     <li>{@code StringKit.isEmpty("abc")    // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isBlank(CharSequence)} 的区别是：该方法不校验空白字符。</p>
     * <p>建议：</p>
     * <ul>
     *     <li>该方法建议用于工具类或任何可以预期的方法参数的校验中。</li>
     *     <li>需要同时校验多个字符串时，建议采用 {@link #hasEmpty(CharSequence...)} 或 {@link #isAllEmpty(CharSequence...)}</li>
     * </ul>
     *
     * @param text 被检测的字符串
     * @return 是否为空
     * @see #isBlank(CharSequence)
     */
    public static boolean isEmpty(final CharSequence text) {
        return text == null || text.length() == 0;
    }

    /**
     * <p>字符串是否为非空白，非空白的定义如下： </p>
     * <ol>
     *     <li>不为 {@code null}</li>
     *     <li>不为空字符串：{@code ""}</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StringKit.isNotEmpty(null)     // false}</li>
     *     <li>{@code StringKit.isNotEmpty("")       // false}</li>
     *     <li>{@code StringKit.isNotEmpty(" \t\n")  // true}</li>
     *     <li>{@code StringKit.isNotEmpty("abc")    // true}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isNotBlank(CharSequence)} 的区别是：该方法不校验空白字符。</p>
     * <p>建议：该方法建议用于工具类或任何可以预期的方法参数的校验中。</p>
     *
     * @param text 被检测的字符串
     * @return 是否为非空
     * @see #isEmpty(CharSequence)
     */
    public static boolean isNotEmpty(final CharSequence text) {
        return !isEmpty(text);
    }

    /**
     * <p>是否包含空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true。</p>
     *
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StringKit.hasEmpty()                  // true}</li>
     *     <li>{@code StringKit.hasEmpty("", null)          // true}</li>
     *     <li>{@code StringKit.hasEmpty("123", "")         // true}</li>
     *     <li>{@code StringKit.hasEmpty("123", "abc")      // false}</li>
     *     <li>{@code StringKit.hasEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isAllEmpty(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>hasEmpty(CharSequence...)            等价于 {@code isEmpty(...) || isEmpty(...) || ...}</li>
     *     <li>{@link #isAllEmpty(CharSequence...)} 等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param args 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasEmpty(final CharSequence... args) {
        if (ArrayKit.isEmpty(args)) {
            return true;
        }

        for (final CharSequence text : args) {
            if (isEmpty(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>是否包含空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true。</p>
     *
     * @param args 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasEmpty(final Iterable<? extends CharSequence> args) {
        if (CollKit.isEmpty(args)) {
            return true;
        }

        for (final CharSequence text : args) {
            if (isEmpty(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>指定字符串数组中的元素，是否全部为空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true。</p>
     *
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StringKit.isAllEmpty()                  // true}</li>
     *     <li>{@code StringKit.isAllEmpty("", null)          // true}</li>
     *     <li>{@code StringKit.isAllEmpty("123", "")         // false}</li>
     *     <li>{@code StringKit.isAllEmpty("123", "abc")      // false}</li>
     *     <li>{@code StringKit.isAllEmpty(" ", "\t", "\n")   // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #hasEmpty(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>{@link #hasEmpty(CharSequence...)}   等价于 {@code isEmpty(...) || isEmpty(...) || ...}</li>
     *     <li>isAllEmpty(CharSequence...)          等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param args 字符串列表
     * @return 所有字符串是否为空白
     */
    public static boolean isAllEmpty(final CharSequence... args) {
        if (ArrayKit.isNotEmpty(args)) {
            for (final CharSequence text : args) {
                if (isNotEmpty(text)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * <p>指定字符串数组中的元素，是否全部为空字符串。</p>
     * <p>如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true。</p>
     *
     * @param args 字符串列表
     * @return 所有字符串是否为空白
     */
    public static boolean isAllEmpty(final Iterable<? extends CharSequence> args) {
        if (CollKit.isNotEmpty(args)) {
            for (final CharSequence text : args) {
                if (isNotEmpty(text)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * <p>指定字符串数组中的元素，是否都不为空字符串。</p>
     * <p>如果指定的字符串数组的长度不为 0，或者所有元素都不是空字符串，则返回 true。</p>
     *
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code StringKit.isAllNotEmpty()                  // false}</li>
     *     <li>{@code StringKit.isAllNotEmpty("", null)          // false}</li>
     *     <li>{@code StringKit.isAllNotEmpty("123", "")         // false}</li>
     *     <li>{@code StringKit.isAllNotEmpty("123", "abc")      // true}</li>
     *     <li>{@code StringKit.isAllNotEmpty(" ", "\t", "\n")   // true}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isAllEmpty(CharSequence...)} 的区别在于：</p>
     * <ul>
     *     <li>{@link #isAllEmpty(CharSequence...)}    等价于 {@code isEmpty(...) && isEmpty(...) && ...}</li>
     *     <li>isAllNotEmpty(CharSequence...)          等价于 {@code !isEmpty(...) && !isEmpty(...) && ...}</li>
     * </ul>
     *
     * @param args 字符串数组
     * @return 所有字符串是否都不为为空白
     */
    public static boolean isAllNotEmpty(final CharSequence... args) {
        return !hasEmpty(args);
    }

    /**
     * 检查字符串是否为null、“null”、“undefined”
     *
     * @param text 被检查的字符串
     * @return 是否为null、“null”、“undefined”
     */
    public static boolean isNullOrUndefined(final CharSequence text) {
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
    public static boolean isEmptyOrUndefined(final CharSequence text) {
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
    public static boolean isBlankOrUndefined(final CharSequence text) {
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
    private static boolean isNullOrUndefinedString(final CharSequence text) {
        final String strString = text.toString().trim();
        return Normal.NULL.equals(strString) || "undefined".equals(strString);
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
        for (int i = value.length(); --i >= 0; ) {
            if (!matcher.test(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
