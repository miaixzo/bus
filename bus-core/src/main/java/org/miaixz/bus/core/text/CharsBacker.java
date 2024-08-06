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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.compare.VersionCompare;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.finder.*;
import org.miaixz.bus.core.text.placeholder.StringFormatter;
import org.miaixz.bus.core.text.replacer.CharRangeReplacer;
import org.miaixz.bus.core.text.replacer.SearchReplacer;
import org.miaixz.bus.core.text.replacer.StringRangeReplacer;
import org.miaixz.bus.core.xyz.*;

/**
 * {@link CharSequence} 相关类封装，包括但不限于：
 * <ul>
 * <li>字符串补充前缀或后缀：addXXX</li>
 * <li>字符串补充长度：padXXX</li>
 * <li>字符串包含关系：containsXXX</li>
 * <li>字符串默认值：defaultIfXXX</li>
 * <li>字符串查找：indexOf</li>
 * <li>字符串判断以什么结尾：endWith</li>
 * <li>字符串判断以什么开始：startWith</li>
 * <li>字符串匹配：equals</li>
 * <li>字符串格式化：format</li>
 * <li>字符串去除：removeXXX</li>
 * <li>字符串重复：repeat</li>
 * <li>获取子串：sub</li>
 * <li>去除两边的指定字符串（只去除一次）：strip</li>
 * <li>去除两边的指定所有字符：trim</li>
 * <li>去除两边的指定所有字符包装和去除包装：wrap</li>
 * </ul>
 * <p>
 * 需要注意的是，strip、trim、wrap（unWrap）的策略不同：
 * <ul>
 * <li>strip： 强调去除两边或某一边的指定字符串，这个字符串不会重复去除，如果一边不存在，另一边不影响去除</li>
 * <li>trim： 强调去除两边指定字符，如果这个字符有多个，全部去除，例如去除两边所有的空白符。</li>
 * <li>unWrap：强调去包装，要求包装的前后字符都要存在，只有一个则不做处理，如去掉双引号包装。</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharsBacker extends CharsValidator {

    /**
     * 调用对象的toString方法，null会返回“null”
     *
     * @param obj 对象
     * @return 字符串
     * @see String#valueOf(Object)
     */
    public static String toString(final Object obj) {
        return String.valueOf(obj);
    }

    /**
     * 调用对象的toString方法，{@code null}会返回{@code null}
     *
     * @param obj 对象
     * @return 字符串 or {@code null}
     */
    public static String toStringOrNull(final Object obj) {
        return null == obj ? null : obj.toString();
    }

    /**
     * 调用对象的toString方法，{@code null}会返回空字符串 "" 如果仅仅是对{@link CharSequence}处理，请使用{@link #emptyIfNull(CharSequence)}
     *
     * @param obj 对象
     * @return {@link String }
     * @see #emptyIfNull(CharSequence)
     */
    public static String toStringOrEmpty(final Object obj) {
        return null == obj ? Normal.EMPTY : obj.toString();
    }

    /**
     * 当给定字符串为空字符串时，转换为"" 此方法与{@link #toStringOrEmpty(Object)}不同的是，如果提供的{@link CharSequence}非String，则保持原状
     *
     * @param text 被转换的字符串
     * @return 转换后的字符串
     * @see #toStringOrEmpty(Object)
     */
    public static CharSequence emptyIfNull(final CharSequence text) {
        return null == text ? Normal.EMPTY : text;
    }

    /**
     * 当给定字符串为空字符串时，转换为{@code null}
     *
     * @param <T>  字符串类型
     * @param text 被转换的字符串
     * @return 转换后的字符串
     */
    public static <T extends CharSequence> T nullIfEmpty(final T text) {
        return isEmpty(text) ? null : text;
    }

    /**
     * <p>
     * 如果给定字符串为{@code null}返回默认值
     * 
     * <pre>{@code
     *   defaultIfNull(null, null);       = null
     *   defaultIfNull(null, "");         = ""
     *   defaultIfNull(null, "zz");       = "zz"
     *   defaultIfNull("abc", *);         = "abc"
     * }</pre>
     *
     * @param <T>          字符串类型
     * @param text         被检查字符串，可能为{@code null}
     * @param defaultValue 被检查字符串为{@code null}返回的默认值，可以为{@code null}
     * @return 被检查字符串不为 {@code null} 返回原值，否则返回默认值
     * @see ObjectKit#defaultIfNull(Object, Object)
     */
    public static <T extends CharSequence> T defaultIfNull(final T text, final T defaultValue) {
        return ObjectKit.defaultIfNull(text, defaultValue);
    }

    /**
     * 如果给定字符串不为{@code null} 返回原值, 否则返回 {@link Supplier#get()} 提供的默认值
     *
     * @param <T>             被检查字符串类型
     * @param source          被检查字符串，可能为{@code null}
     * @param defaultSupplier 为空时的默认值提供者
     * @return 被检查字符串不为 {@code null} 返回原值，否则返回 {@link Supplier#get()} 提供的默认值
     * @see ObjectKit#defaultIfNull(Object, Supplier)
     */
    public static <T extends CharSequence> T defaultIfNull(final T source,
            final Supplier<? extends T> defaultSupplier) {
        return ObjectKit.defaultIfNull(source, defaultSupplier);
    }

    /**
     * 如果给定字符串不为{@code null} 返回自定义handler处理后的结果，否则返回 {@link Supplier#get()} 提供的默认值
     *
     * @param <R>             返回值类型
     * @param <T>             被检查对象类型
     * @param source          被检查对象，可能为{@code null}
     * @param handler         非空时自定义的处理方法
     * @param defaultSupplier 为空时的默认值提供者
     * @return 被检查对象不为 {@code null} 返回处理后的结果，否则返回 {@link Supplier#get()} 提供的默认值
     * @see ObjectKit#defaultIfNull(Object, Function, Supplier)
     */
    public static <T extends CharSequence, R> R defaultIfNull(final T source,
            final Function<? super T, ? extends R> handler, final Supplier<? extends R> defaultSupplier) {
        return ObjectKit.defaultIfNull(source, handler, defaultSupplier);
    }

    /**
     * 如果给定对象为{@code null}或者 "" 返回默认值
     *
     * <pre>
     *   defaultIfEmpty(null, null)      = null
     *   defaultIfEmpty(null, "")        = ""
     *   defaultIfEmpty("", "zz")        = "zz"
     *   defaultIfEmpty(" ", "zz")       = " "
     *   defaultIfEmpty("abc", *)        = "abc"
     * </pre>
     *
     * @param <T>          对象类型（必须实现CharSequence接口）
     * @param text         被检查对象，可能为{@code null}
     * @param defaultValue 被检查对象为{@code null}或者 ""返回的默认值，可以为{@code null}或者 ""
     * @return 被检查对象为{@code null}或者 ""返回默认值，否则返回原值
     */
    public static <T extends CharSequence> T defaultIfEmpty(final T text, final T defaultValue) {
        return isEmpty(text) ? defaultValue : text;
    }

    /**
     * 如果给定对象为{@code null}或者{@code ""}返回原值, 否则返回自定义handler处理后的返回值
     *
     * @param <T>             被检查对象类型
     * @param text            String 类型
     * @param defaultSupplier empty时的处理方法
     * @return 处理后的返回值
     */
    public static <T extends CharSequence> T defaultIfEmpty(final T text, final Supplier<? extends T> defaultSupplier) {
        return isEmpty(text) ? defaultSupplier.get() : text;
    }

    /**
     * 如果给定对象为{@code null}或者{@code ""}返回defaultHandler处理的结果, 否则返回自定义handler处理后的返回值
     *
     * @param <T>             被检查对象类型
     * @param <V>             结果类型
     * @param text            String 类型
     * @param handler         非empty的处理方法
     * @param defaultSupplier empty时的处理方法
     * @return 处理后的返回值
     */
    public static <T extends CharSequence, V> V defaultIfEmpty(final T text, final Function<T, V> handler,
            final Supplier<? extends V> defaultSupplier) {
        return isEmpty(text) ? defaultSupplier.get() : handler.apply(text);
    }

    /**
     * 如果给定对象为{@code null}或者""或者空白符返回默认值
     *
     * <pre>
     *   defaultIfBlank(null, null)      = null
     *   defaultIfBlank(null, "")        = ""
     *   defaultIfBlank("", "zz")        = "zz"
     *   defaultIfBlank(" ", "zz")       = "zz"
     *   defaultIfBlank("abc", *)        = "abc"
     * </pre>
     *
     * @param <T>          对象类型（必须实现CharSequence接口）
     * @param text         被检查对象，可能为{@code null}
     * @param defaultValue 被检查对象为{@code null}或者 ""或者空白符返回的默认值，可以为{@code null}或者 ""或者空白符
     * @return 被检查对象为{@code null}或者 ""或者空白符返回默认值，否则返回原值
     */
    public static <T extends CharSequence> T defaultIfBlank(final T text, final T defaultValue) {
        return isBlank(text) ? defaultValue : text;
    }

    /**
     * 如果被检查对象为 {@code null} 或 "" 或 空白字符串时，返回默认值（由 defaultValueSupplier 提供）；否则直接返回
     *
     * @param text            被检查对象
     * @param handler         非blank的处理方法
     * @param defaultSupplier 默认值提供者
     * @param <T>             对象类型（必须实现CharSequence接口）
     * @param <V>             结果类型
     * @return 被检查对象为{@code null}返回默认值，否则返回自定义handle处理后的返回值
     * @throws NullPointerException {@code defaultValueSupplier == null} 时，抛出
     */
    public static <T extends CharSequence, V> V defaultIfBlank(final T text, final Function<T, V> handler,
            final Supplier<? extends V> defaultSupplier) {
        if (isBlank(text)) {
            return defaultSupplier.get();
        }
        return handler.apply(text);
    }

    /**
     * 除去字符串头尾部的空白，如果字符串是{@code null}，依然返回{@code null}。
     *
     * <p>
     * 注意，和{@link String#trim()}不同，此方法使用{@link CharKit#isBlankChar(char)} 来判定空白， 因而可以除去英文字符集之外的其它空白，如中文空格。
     * <ul>
     * <li>去除字符串空格罗列相关如下：</li>
     * <li>{@link #trimPrefix(CharSequence)}去除头部空格</li>
     * <li>{@link #trimSuffix(CharSequence)}去除尾部空格</li>
     * <li>{@link #cleanBlank(CharSequence)}去除头部、尾部、中间空格</li>
     * </ul>
     *
     * <pre>
     * trim(null)                         = null
     * trim(&quot;&quot;)                 = &quot;&quot;
     * trim(&quot;     &quot;)            = &quot;&quot;
     * trim(&quot;abc&quot;)              = &quot;abc&quot;
     * trim(&quot;    abc    &quot;)      = &quot;abc&quot;
     * </pre>
     *
     * @param text 要处理的字符串
     * @return 除去头尾空白的字符串，如果原字串为{@code null}，则返回{@code null}
     */
    public static String trim(final CharSequence text) {
        return StringTrimer.TRIM_BLANK.apply(text);
    }

    /**
     * 除去字符串头尾部的空白，如果字符串是{@code null}，返回{@code ""}。
     *
     * <pre>
     * trimToEmpty(null)                  = ""
     * trimToEmpty("")                    = ""
     * trimToEmpty("     ")               = ""
     * trimToEmpty("abc")                 = "abc"
     * trimToEmpty("    abc    ")         = "abc"
     * </pre>
     *
     * @param text 字符串
     * @return 去除两边空白符后的字符串, 如果为null返回""
     */
    public static String trimToEmpty(final CharSequence text) {
        return text == null ? Normal.EMPTY : trim(text);
    }

    /**
     * 除去字符串头尾部的空白，如果字符串是{@code null}或者""，返回{@code null}。
     *
     * <pre>
     * trimToNull(null)                   = null
     * trimToNull("")                     = null
     * trimToNull("     ")                = null
     * trimToNull("abc")                  = "abc"
     * trimToEmpty("    abc    ")         = "abc"
     * </pre>
     *
     * @param text 字符串
     * @return 去除两边空白符后的字符串, 如果为空返回null
     */
    public static String trimToNull(final CharSequence text) {
        final String trim = trim(text);
        return Normal.EMPTY.equals(trim) ? null : trim;
    }

    /**
     * 除去字符串头部的空白，如果字符串是{@code null}，则返回{@code null}。
     *
     * <p>
     * 注意，和{@link String#trim()}不同，此方法使用{@link CharKit#isBlankChar(char)} 来判定空白， 因而可以除去英文字符集之外的其它空白，如中文空格。
     *
     * <pre>
     * trimPrefix(null)                   = null
     * trimPrefix(&quot;&quot;)           = &quot;&quot;
     * trimPrefix(&quot;abc&quot;)        = &quot;abc&quot;
     * trimPrefix(&quot;  abc&quot;)      = &quot;abc&quot;
     * trimPrefix(&quot;abc  &quot;)      = &quot;abc  &quot;
     * trimPrefix(&quot; abc &quot;)      = &quot;abc &quot;
     * </pre>
     *
     * @param text 要处理的字符串
     * @return 除去空白的字符串，如果原字串为{@code null}或结果字符串为{@code ""}，则返回 {@code null}
     */
    public static String trimPrefix(final CharSequence text) {
        return StringTrimer.TRIM_PREFIX_BLANK.apply(text);
    }

    /**
     * 除去字符串尾部的空白，如果字符串是{@code null}，则返回{@code null}。
     *
     * <p>
     * 注意，和{@link String#trim()}不同，此方法使用{@link CharKit#isBlankChar(char)} 来判定空白， 因而可以除去英文字符集之外的其它空白，如中文空格。
     *
     * <pre>
     * trimSuffix(null)                  = null
     * trimSuffix(&quot;&quot;)          = &quot;&quot;
     * trimSuffix(&quot;abc&quot;)       = &quot;abc&quot;
     * trimSuffix(&quot;  abc&quot;)     = &quot;  abc&quot;
     * trimSuffix(&quot;abc  &quot;)     = &quot;abc&quot;
     * trimSuffix(&quot; abc &quot;)     = &quot; abc&quot;
     * </pre>
     *
     * @param text 要处理的字符串
     * @return 除去空白的字符串，如果原字串为{@code null}或结果字符串为{@code ""}，则返回 {@code null}
     */
    public static String trimSuffix(final CharSequence text) {
        return StringTrimer.TRIM_SUFFIX_BLANK.apply(text);
    }

    /**
     * 除去字符串头尾部的空白符，如果字符串是{@code null}，依然返回{@code null}。
     *
     * @param text 要处理的字符串
     * @param mode 去除模式，可选去除头部、尾部、两边
     * @return 除去指定字符后的的字符串，如果原字串为{@code null}，则返回{@code null}
     */
    public static String trim(final CharSequence text, final StringTrimer.TrimMode mode) {
        return new StringTrimer(mode, CharKit::isBlankChar).apply(text);
    }

    /**
     * 按照断言，除去字符串头尾部的断言为真的字符，如果字符串是{@code null}，依然返回{@code null}。
     *
     * @param text      要处理的字符串
     * @param mode      去除模式，可选去除头部、尾部、两边
     * @param predicate 断言是否过掉字符，返回{@code true}表述过滤掉，{@code false}表示不过滤
     * @return 除去指定字符后的的字符串，如果原字串为{@code null}，则返回{@code null}
     */
    public static String trim(final CharSequence text, final StringTrimer.TrimMode mode,
            final Predicate<Character> predicate) {
        return new StringTrimer(mode, predicate).apply(text);
    }

    /**
     * 字符串是否以给定字符开始
     *
     * @param text 字符串
     * @param c    字符
     * @return 是否开始
     */
    public static boolean startWith(final CharSequence text, final char c) {
        if (isEmpty(text)) {
            return false;
        }
        return c == text.charAt(0);
    }

    /**
     * 是否以指定字符串开头
     *
     * @param text   被监测字符串
     * @param prefix 开头字符串
     * @return 是否以指定字符串开头
     */
    public static boolean startWith(final CharSequence text, final CharSequence prefix) {
        return startWith(text, prefix, false);
    }

    /**
     * 是否以指定字符串开头，忽略相等字符串的情况
     *
     * @param text   被监测字符串
     * @param prefix 开头字符串
     * @return 是否以指定字符串开头并且两个字符串不相等
     */
    public static boolean startWithIgnoreEquals(final CharSequence text, final CharSequence prefix) {
        return startWith(text, prefix, false, true);
    }

    /**
     * 是否以指定字符串开头，忽略大小写
     *
     * @param text   被监测字符串
     * @param prefix 开头字符串
     * @return 是否以指定字符串开头
     */
    public static boolean startWithIgnoreCase(final CharSequence text, final CharSequence prefix) {
        return startWith(text, prefix, true);
    }

    /**
     * 给定字符串是否以任何一个字符串开始 给定字符串和数组为空都返回false
     *
     * @param text     给定字符串
     * @param prefixes 需要检测的开始字符串
     * @return 给定字符串是否以任何一个字符串开始
     */
    public static boolean startWithAny(final CharSequence text, final CharSequence... prefixes) {
        if (isEmpty(text) || ArrayKit.isEmpty(prefixes)) {
            return false;
        }

        for (CharSequence prefix : prefixes) {
            if (startWith(text, prefix, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 给定字符串是否以任何一个字符串开始（忽略大小写） 给定字符串和数组为空都返回false
     *
     * @param text     给定字符串
     * @param prefixes 需要检测的开始字符串
     * @return 给定字符串是否以任何一个字符串开始
     */
    public static boolean startWithAnyIgnoreCase(final CharSequence text, final CharSequence... prefixes) {
        if (isEmpty(text) || ArrayKit.isEmpty(prefixes)) {
            return false;
        }

        for (final CharSequence prefix : prefixes) {
            if (startWith(text, prefix, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否以指定字符串开头 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     *
     * @param text       被监测字符串
     * @param prefix     开头字符串
     * @param ignoreCase 是否忽略大小写
     * @return 是否以指定字符串开头
     */
    public static boolean startWith(final CharSequence text, final CharSequence prefix, final boolean ignoreCase) {
        return startWith(text, prefix, ignoreCase, false);
    }

    /**
     * 是否以指定字符串开头 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     * 
     * <pre>
     *     CharsBacker.startWith("123", "123", false, true);   -- false
     *     CharsBacker.startWith("ABCDEF", "abc", true, true); -- true
     *     CharsBacker.startWith("abc", "abc", true, true);    -- false
     * </pre>
     *
     * @param text         被监测字符串
     * @param prefix       开头字符串
     * @param ignoreCase   是否忽略大小写
     * @param ignoreEquals 是否忽略字符串相等的情况
     * @return 是否以指定字符串开头
     */
    public static boolean startWith(final CharSequence text, final CharSequence prefix, final boolean ignoreCase,
            final boolean ignoreEquals) {
        return new OffsetMatcher(ignoreCase, ignoreEquals, true).test(text, prefix);
    }

    /**
     * 字符串是否以给定字符结尾
     *
     * @param text 字符串
     * @param c    字符
     * @return 是否结尾
     */
    public static boolean endWith(final CharSequence text, final char c) {
        if (isEmpty(text)) {
            return false;
        }
        return c == text.charAt(text.length() - 1);
    }

    /**
     * 是否以指定字符串结尾
     *
     * @param text   被监测字符串
     * @param suffix 结尾字符串
     * @return 是否以指定字符串结尾
     */
    public static boolean endWith(final CharSequence text, final CharSequence suffix) {
        return endWith(text, suffix, false);
    }

    /**
     * 是否以指定字符串结尾，忽略大小写
     *
     * @param text   被监测字符串
     * @param suffix 结尾字符串
     * @return 是否以指定字符串结尾
     */
    public static boolean endWithIgnoreCase(final CharSequence text, final CharSequence suffix) {
        return endWith(text, suffix, true);
    }

    /**
     * 给定字符串是否以任何一个字符串结尾 给定字符串和数组为空都返回false
     *
     * @param text     给定字符串
     * @param suffixes 需要检测的结尾字符串
     * @return 给定字符串是否以任何一个字符串结尾
     */
    public static boolean endWithAny(final CharSequence text, final CharSequence... suffixes) {
        if (isEmpty(text) || ArrayKit.isEmpty(suffixes)) {
            return false;
        }

        for (final CharSequence suffix : suffixes) {
            if (endWith(text, suffix, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 给定字符串是否以任何一个字符串结尾（忽略大小写） 给定字符串和数组为空都返回false
     *
     * @param text     给定字符串
     * @param suffixes 需要检测的结尾字符串
     * @return 给定字符串是否以任何一个字符串结尾
     */
    public static boolean endWithAnyIgnoreCase(final CharSequence text, final CharSequence... suffixes) {
        if (isEmpty(text) || ArrayKit.isEmpty(suffixes)) {
            return false;
        }

        for (final CharSequence suffix : suffixes) {
            if (endWith(text, suffix, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否以指定字符串结尾 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     *
     * @param text       被监测字符串
     * @param suffix     结尾字符串
     * @param ignoreCase 是否忽略大小写
     * @return 是否以指定字符串结尾
     */
    public static boolean endWith(final CharSequence text, final CharSequence suffix, final boolean ignoreCase) {
        return endWith(text, suffix, ignoreCase, false);
    }

    /**
     * 是否以指定字符串结尾 如果给定的字符串和开头字符串都为null则返回true，否则任意一个值为null返回false
     *
     * @param text         被监测字符串
     * @param suffix       结尾字符串
     * @param ignoreCase   是否忽略大小写
     * @param ignoreEquals 是否忽略字符串相等的情况
     * @return 是否以指定字符串结尾
     */
    public static boolean endWith(final CharSequence text, final CharSequence suffix, final boolean ignoreCase,
            final boolean ignoreEquals) {
        return new OffsetMatcher(ignoreCase, ignoreEquals, false).test(text, suffix);
    }

    /**
     * 指定字符是否在字符串中出现过
     *
     * @param text 字符串
     * @param args 被查找的字符
     * @return 是否包含
     */
    public static boolean contains(final CharSequence text, final char args) {
        return indexOf(text, args) > -1;
    }

    /**
     * 指定字符串是否在字符串中出现过
     *
     * @param text 字符串
     * @param args 被查找的字符串
     * @return 是否包含
     */
    public static boolean contains(final CharSequence text, final CharSequence args) {
        if (null == text || null == args) {
            return false;
        }
        return text.toString().contains(args);
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串
     *
     * @param text 指定字符串
     * @param args 需要检查的字符串数组
     * @return 是否包含任意一个字符串
     */
    public static boolean containsAny(final CharSequence text, final CharSequence... args) {
        return null != getContainsString(text, args);
    }

    /**
     * 查找指定字符串是否包含指定字符列表中的任意一个字符
     *
     * @param text 指定字符串
     * @param args 需要检查的字符数组
     * @return 是否包含任意一个字符
     */
    public static boolean containsAny(final CharSequence text, final char... args) {
        if (isNotEmpty(text)) {
            final int len = text.length();
            for (int i = 0; i < len; i++) {
                if (ArrayKit.contains(args, text.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查指定字符串中是否只包含给定的字符 这里的containsOnly并不是必须全部给定的args都需要有，而是一个子集。 args是个限定集合，检查字符串中的字符是否在这个限定集合中。
     * <ul>
     * <li>text 是 null，args 也是 null，直接返回 true</li>
     * <li>text 是 null，args 不是 null，直接返回 true</li>
     * <li>text 不是 null，args 是 null，直接返回 false</li>
     * </ul>
     *
     * @param text 字符串
     * @param args 检查的字符
     * @return 字符串含有非检查的字符，返回false
     */
    public static boolean containsOnly(final CharSequence text, final char... args) {
        if (isNotEmpty(text)) {
            final int len = text.length();
            for (int i = 0; i < len; i++) {
                if (!ArrayKit.contains(args, text.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 给定字符串是否包含空白符（空白符包括空格、制表符、全角空格和不间断空格） 如果给定字符串为null或者""，则返回false
     *
     * @param text 字符串
     * @return 是否包含空白符
     */
    public static boolean containsBlank(final CharSequence text) {
        if (null == text) {
            return false;
        }
        final int length = text.length();
        if (0 == length) {
            return false;
        }

        for (int i = 0; i < length; i += 1) {
            if (CharKit.isBlankChar(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串，如果包含返回找到的第一个字符串
     *
     * @param text 指定字符串
     * @param args 需要检查的字符串数组
     * @return 被包含的第一个字符串
     */
    public static String getContainsString(final CharSequence text, final CharSequence... args) {
        if (isEmpty(text) || ArrayKit.isEmpty(args)) {
            return null;
        }
        for (final CharSequence checkStr : args) {
            if (contains(text, checkStr)) {
                return checkStr.toString();
            }
        }
        return null;
    }

    /**
     * 是否包含特定字符，忽略大小写，如果给定两个参数都为{@code null}，返回true
     *
     * @param text 被检测字符串
     * @param args 被测试是否包含的字符串
     * @return 是否包含
     */
    public static boolean containsIgnoreCase(final CharSequence text, final CharSequence args) {
        if (null == text) {
            // 如果被监测字符串和
            return null == args;
        }
        return indexOfIgnoreCase(text, args) > -1;
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串 忽略大小写
     *
     * @param text 指定字符串
     * @param args 需要检查的字符串数组
     * @return 是否包含任意一个字符串
     */
    public static boolean containsAnyIgnoreCase(final CharSequence text, final CharSequence... args) {
        return null != getContainsStrIgnoreCase(text, args);
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串，如果包含返回找到的第一个字符串 忽略大小写
     *
     * @param text 指定字符串
     * @param args 需要检查的字符串数组
     * @return 被包含的第一个字符串
     */
    public static String getContainsStrIgnoreCase(final CharSequence text, final CharSequence... args) {
        if (isEmpty(text) || ArrayKit.isEmpty(args)) {
            return null;
        }
        for (final CharSequence testStr : args) {
            if (containsIgnoreCase(text, testStr)) {
                return testStr.toString();
            }
        }
        return null;
    }

    /**
     * 检查指定字符串中是否含给定的所有字符串
     *
     * @param text 字符串
     * @param args 检查的字符
     * @return 字符串含有非检查的字符，返回false
     */
    public static boolean containsAll(final CharSequence text, final CharSequence... args) {
        if (isBlank(text) || ArrayKit.isEmpty(args)) {
            return false;
        }
        for (final CharSequence testChar : args) {
            if (!contains(text, testChar)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param text 字符串
     * @param args 被查找的字符
     * @return 位置
     */
    public static int indexOf(final CharSequence text, final char args) {
        return indexOf(text, args, 0);
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param text  字符串
     * @param args  被查找的字符
     * @param start 起始位置，如果小于0，从0开始查找
     * @return 位置
     */
    public static int indexOf(final CharSequence text, final char args, final int start) {
        if (text instanceof String) {
            return ((String) text).indexOf(args, start);
        } else {
            return indexOf(text, args, start, -1);
        }
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param text  字符串
     * @param args  被查找的字符
     * @param start 起始位置，如果小于0，从0开始查找
     * @param end   终止位置，如果超过str.length()则默认查找到字符串末尾
     * @return 位置
     */
    public static int indexOf(final CharSequence text, final char args, final int start, final int end) {
        if (isEmpty(text)) {
            return Normal.__1;
        }
        return new CharFinder(args).setText(text).setEndIndex(end).start(start);
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param text    字符串
     * @param matcher 被查找的字符匹配器
     * @param start   起始位置，如果小于0，从0开始查找
     * @param end     终止位置，如果超过str.length()则默认查找到字符串末尾
     * @return 位置
     */
    public static int indexOf(final CharSequence text, final Predicate<Character> matcher, final int start,
            final int end) {
        if (isEmpty(text)) {
            return Normal.__1;
        }
        return new MatcherFinder(matcher).setText(text).setEndIndex(end).start(start);
    }

    /**
     * 指定范围内查找字符串，忽略大小写
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
     * @param args 需要查找位置的字符串
     * @return 位置
     */
    public static int indexOfIgnoreCase(final CharSequence text, final CharSequence args) {
        return indexOfIgnoreCase(text, args, 0);
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
     * @param text      字符串
     * @param args      需要查找位置的字符串
     * @param fromIndex 起始位置
     * @return 位置
     */
    public static int indexOfIgnoreCase(final CharSequence text, final CharSequence args, final int fromIndex) {
        return indexOf(text, args, fromIndex, true);
    }

    /**
     * 指定范围内查找字符串
     *
     * @param text       字符串，空则返回-1
     * @param args       需要查找位置的字符串，空则返回-1
     * @param from       起始位置（包含）
     * @param ignoreCase 是否忽略大小写
     * @return 位置
     */
    public static int indexOf(final CharSequence text, final CharSequence args, final int from,
            final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(args)) {
            if (equals(text, args)) {
                return 0;
            } else {
                return Normal.__1;
            }
        }
        return StringFinder.of(args, ignoreCase).setText(text).start(from);
    }

    /**
     * 指定范围内查找字符串，忽略大小写
     *
     * @param text 字符串
     * @param args 需要查找位置的字符串
     * @return 位置
     */
    public static int lastIndexOfIgnoreCase(final CharSequence text, final CharSequence args) {
        return lastIndexOfIgnoreCase(text, args, text.length());
    }

    /**
     * 指定范围内查找字符串，忽略大小写 fromIndex 为搜索起始位置，从后往前计数
     *
     * @param text      字符串
     * @param args      需要查找位置的字符串
     * @param fromIndex 起始位置，从后往前计数
     * @return 位置
     */
    public static int lastIndexOfIgnoreCase(final CharSequence text, final CharSequence args, final int fromIndex) {
        return lastIndexOf(text, args, fromIndex, true);
    }

    /**
     * 指定范围内查找字符串 fromIndex 为搜索起始位置，从后往前计数
     *
     * @param text       字符串
     * @param args       需要查找位置的字符串
     * @param from       起始位置，从后往前计数
     * @param ignoreCase 是否忽略大小写
     * @return 位置
     */
    public static int lastIndexOf(final CharSequence text, final CharSequence args, final int from,
            final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(args)) {
            if (equals(text, args)) {
                return 0;
            } else {
                return Normal.__1;
            }
        }
        return StringFinder.of(args, ignoreCase).setText(text).setNegative(true).start(from);
    }

    /**
     * 返回字符串 args 在字符串 text1 中第 ordinal 次出现的位置。
     *
     * <p>
     * 如果 text1=null 或 args=null 或 ordinal&le;0 则返回-1 此方法来自：Apache-Commons-Lang
     * <p>
     * 示例（*代表任意字符）：
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
     * @param text    被检查的字符串，可以为null
     * @param args    被查找的字符串，可以为null
     * @param ordinal 第几次出现的位置
     * @return 查找到的位置
     */
    public static int ordinalIndexOf(final CharSequence text, final CharSequence args, final int ordinal) {
        if (text == null || args == null || ordinal <= 0) {
            return Normal.__1;
        }
        if (args.length() == 0) {
            return 0;
        }
        int found = 0;
        int index = Normal.__1;
        do {
            index = indexOf(text, args, index + 1, false);
            if (index < 0) {
                return index;
            }
            found++;
        } while (found < ordinal);
        return index;
    }

    /**
     * 移除字符串中所有给定字符串 例：removeAll("aa-bb-cc-dd", "-") = aabbccdd
     *
     * @param text 字符串
     * @param args 被移除的字符串
     * @return 移除后的字符串
     */
    public static String removeAll(final CharSequence text, final CharSequence args) {
        // args如果为空， 也不用继续后面的逻辑
        if (isEmpty(text) || isEmpty(args)) {
            return toStringOrNull(text);
        }
        return text.toString().replace(args, Normal.EMPTY);
    }

    /**
     * 移除字符串中所有给定字符串，当某个字符串出现多次，则全部移除 例：removeAny("aa-bb-cc-dd", "a", "b") = --cc-dd
     *
     * @param text 字符串
     * @param args 被移除的字符串
     * @return 移除后的字符串
     */
    public static String removeAny(final CharSequence text, final CharSequence... args) {
        String result = toStringOrNull(text);
        if (isNotEmpty(text)) {
            for (final CharSequence remove : args) {
                result = removeAll(result, remove);
            }
        }
        return result;
    }

    /**
     * 去除字符串中指定的多个字符，如有多个则全部去除
     *
     * @param text  字符串
     * @param chars 字符列表
     * @return 去除后的字符
     */
    public static String removeAll(final CharSequence text, final char... chars) {
        if (isEmpty(text) || ArrayKit.isEmpty(chars)) {
            return toStringOrNull(text);
        }
        return filter(text, (c) -> !ArrayKit.contains(chars, c));
    }

    /**
     * 去除所有换行符，包括：
     *
     * <pre>
     * 1. \r
     * 1. \n
     * </pre>
     *
     * @param text 字符串
     * @return 处理后的字符串
     */
    public static String removeAllLineBreaks(final CharSequence text) {
        return removeAll(text, Symbol.C_CR, Symbol.C_LF);
    }

    /**
     * 去掉首部指定长度的字符串并将剩余字符串首字母小写 例如：text1=setName, preLength=3 = return name
     *
     * @param text      被处理的字符串
     * @param preLength 去掉的长度
     * @return 处理后的字符串，不符合规范返回null
     */
    public static String removePreAndLowerFirst(final CharSequence text, final int preLength) {
        if (text == null) {
            return null;
        }
        if (text.length() > preLength) {
            final char first = Character.toLowerCase(text.charAt(preLength));
            if (text.length() > preLength + 1) {
                return first + text.toString().substring(preLength + 1);
            }
            return String.valueOf(first);
        } else {
            return text.toString();
        }
    }

    /**
     * 去掉首部指定长度的字符串并将剩余字符串首字母小写 例如：text1=setName, prefix=set = return name
     *
     * @param text   被处理的字符串
     * @param prefix 前缀
     * @return 处理后的字符串，不符合规范返回null
     */
    public static String removePreAndLowerFirst(final CharSequence text, final CharSequence prefix) {
        return lowerFirst(removePrefix(text, prefix));
    }

    /**
     * 去掉指定前缀
     *
     * @param text   字符串
     * @param prefix 前缀
     * @return 切掉后的字符串，若前缀不是 prefix， 返回原字符串
     */
    public static String removePrefix(final CharSequence text, final CharSequence prefix) {
        return removePrefix(text, prefix, false);
    }

    /**
     * 忽略大小写去掉指定前缀
     *
     * @param text   字符串
     * @param prefix 前缀
     * @return 切掉后的字符串，若前缀不是 prefix， 返回原字符串
     */
    public static String removePrefixIgnoreCase(final CharSequence text, final CharSequence prefix) {
        return removePrefix(text, prefix, true);
    }

    /**
     * 去掉指定前缀
     *
     * @param text       字符串
     * @param prefix     前缀
     * @param ignoreCase 是否忽略大小写
     * @return 切掉后的字符串，若前缀不是 prefix， 返回原字符串
     */
    public static String removePrefix(final CharSequence text, final CharSequence prefix, final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(prefix)) {
            return toStringOrNull(text);
        }

        final String text2 = text.toString();
        if (startWith(text, prefix, ignoreCase)) {
            return subSuf(text2, prefix.length());// 截取后半段
        }
        return text2;
    }

    /**
     * 去掉指定所有后缀，如：
     * 
     * <pre>{@code
     *     str=11abab, suffix=ab => return 11
     *     str=11ab, suffix=ab => return 11
     *     str=11ab, suffix="" => return 11ab
     *     str=11ab, suffix=null => return 11ab
     * }</pre>
     *
     * @param text   字符串，空返回原字符串
     * @param suffix 后缀字符串，空返回原字符串
     * @return 去掉所有后缀的字符串，若后缀不是 suffix， 返回原字符串
     */
    public static String removeAllSuffix(final CharSequence text, final CharSequence suffix) {
        if (isEmpty(text) || isEmpty(suffix)) {
            return toStringOrNull(text);
        }

        final String suffixStr = suffix.toString();
        final int suffixLength = suffixStr.length();

        final String str2 = text.toString();
        int toIndex = str2.length();
        while (str2.startsWith(suffixStr, toIndex - suffixLength)) {
            toIndex -= suffixLength;
        }
        return subPre(str2, toIndex);
    }

    /**
     * 去掉指定后缀
     *
     * @param text   字符串
     * @param suffix 后缀
     * @return 切掉后的字符串，若后缀不是 suffix， 返回原字符串
     */
    public static String removeSuffix(final CharSequence text, final CharSequence suffix) {
        if (isEmpty(text) || isEmpty(suffix)) {
            return toStringOrNull(text);
        }

        final String text2 = text.toString();
        if (text2.endsWith(suffix.toString())) {
            // 截取前半段
            return subPre(text2, text2.length() - suffix.length());
        }
        return text2;
    }

    /**
     * 去掉指定后缀，并小写首字母
     *
     * @param text   字符串
     * @param suffix 后缀
     * @return 切掉后的字符串，若后缀不是 suffix， 返回原字符串
     */
    public static String removeSufAndLowerFirst(final CharSequence text, final CharSequence suffix) {
        return lowerFirst(removeSuffix(text, suffix));
    }

    /**
     * 忽略大小写去掉指定后缀
     *
     * @param text   字符串
     * @param suffix 后缀
     * @return 切掉后的字符串，若后缀不是 suffix， 返回原字符串
     */
    public static String removeSuffixIgnoreCase(final CharSequence text, final CharSequence suffix) {
        if (isEmpty(text) || isEmpty(suffix)) {
            return toStringOrNull(text);
        }

        final String text2 = text.toString();
        if (endWithIgnoreCase(text, suffix)) {
            return subPre(text2, text2.length() - suffix.length());
        }
        return text2;
    }

    /**
     * 清理空白字符
     *
     * @param text 被清理的字符串
     * @return 清理后的字符串
     */
    public static String cleanBlank(final CharSequence text) {
        return filter(text, c -> !CharKit.isBlankChar(c));
    }

    /**
     * 去除两边的指定字符串
     *
     * @param text           被处理的字符串
     * @param prefixOrSuffix 前缀或后缀
     * @return 处理后的字符串
     */
    public static String strip(final CharSequence text, final CharSequence prefixOrSuffix) {
        if (equals(text, prefixOrSuffix)) {
            // 对于去除相同字符的情况单独处理
            return Normal.EMPTY;
        }
        return strip(text, prefixOrSuffix, prefixOrSuffix);
    }

    /**
     * 去除两边的指定字符串 两边字符如果存在，则去除，不存在不做处理
     *
     * @param text   被处理的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 处理后的字符串
     */
    public static String strip(final CharSequence text, final CharSequence prefix, final CharSequence suffix) {
        return strip(text, prefix, suffix, false);
    }

    /**
     * 去除两边的指定字符串 两边字符如果存在，则去除，不存在不做处理
     * 
     * <pre>{@code
     *  "aaa_STRIPPED_bbb", "a", "b"       -> "aa_STRIPPED_bb"
     *  "aaa_STRIPPED_bbb", null, null     -> "aaa_STRIPPED_bbb"
     *  "aaa_STRIPPED_bbb", "", ""         -> "aaa_STRIPPED_bbb"
     *  "aaa_STRIPPED_bbb", "", "b"        -> "aaa_STRIPPED_bb"
     *  "aaa_STRIPPED_bbb", null, "b"      -> "aaa_STRIPPED_bb"
     *  "aaa_STRIPPED_bbb", "a", ""        -> "aa_STRIPPED_bbb"
     *  "aaa_STRIPPED_bbb", "a", null      -> "aa_STRIPPED_bbb"
     *
     *  "a", "a", "a"  -> ""
     * }
     * </pre>
     *
     * @param text       被处理的字符串
     * @param prefix     前缀
     * @param suffix     后缀
     * @param ignoreCase 是否忽略大小写
     * @return 处理后的字符串
     */
    public static String strip(final CharSequence text, final CharSequence prefix, final CharSequence suffix,
            final boolean ignoreCase) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }

        int from = 0;
        int to = text.length();

        final String text2 = text.toString();
        if (startWith(text2, prefix, ignoreCase)) {
            from = prefix.length();
        }
        if (endWith(text2, suffix, ignoreCase)) {
            to -= suffix.length();
        }

        return text2.substring(Math.min(from, to), Math.max(from, to));
    }

    /**
     * 去除两边的指定字符串，忽略大小写
     *
     * @param text           被处理的字符串
     * @param prefixOrSuffix 前缀或后缀
     * @return 处理后的字符串
     */
    public static String stripIgnoreCase(final CharSequence text, final CharSequence prefixOrSuffix) {
        return stripIgnoreCase(text, prefixOrSuffix, prefixOrSuffix);
    }

    /**
     * 去除两边的指定字符串，忽略大小写
     *
     * @param text   被处理的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 处理后的字符串
     */
    public static String stripIgnoreCase(final CharSequence text, final CharSequence prefix,
            final CharSequence suffix) {
        return strip(text, prefix, suffix, true);
    }

    /**
     * 如果给定字符串不是以prefix开头的，在开头补充 prefix
     *
     * @param text   字符串
     * @param prefix 前缀
     * @return 补充后的字符串
     * @see #prependIfMissing(CharSequence, CharSequence, CharSequence...)
     */
    public static String addPrefixIfNot(final CharSequence text, final CharSequence prefix) {
        return prependIfMissing(text, prefix, prefix);
    }

    /**
     * 如果给定字符串不是以suffix结尾的，在尾部补充 suffix
     *
     * @param text   字符串
     * @param suffix 后缀
     * @return 补充后的字符串
     * @see #appendIfMissing(CharSequence, CharSequence, CharSequence...)
     */
    public static String addSuffixIfNot(final CharSequence text, final CharSequence suffix) {
        return appendIfMissing(text, suffix);
    }

    /**
     * 将字符串切分为N等份
     *
     * @param text       字符串
     * @param partLength 每等份的长度
     * @return 切分后的数组
     */
    public static String[] cut(final CharSequence text, final int partLength) {
        if (null == text) {
            return null;
        }
        final int len = text.length();
        if (len < partLength) {
            return new String[] { text.toString() };
        }
        final int part = MathKit.count(len, partLength);
        final String[] array = new String[part];

        final String text2 = text.toString();
        for (int i = 0; i < part; i++) {
            array[i] = text2.substring(i * partLength, (i == part - 1) ? len : (partLength + i * partLength));
        }
        return array;
    }

    /**
     * 改进JDK subString
     * <ul>
     * <li>index从0开始计算，最后一个字符为-1，即sub("miaixz", 0, -1)得到"miaixz"</li>
     * <li>如果from和to位置一样，返回 ""</li>
     * <li>如果from或to为负数，则按照length从后向前数位置，如果绝对值大于字符串长度，则from归到0，to归到length</li>
     * <li>如果经过修正的index中from大于to，则互换from和to，如abcdefgh 2 3 =》 c，abcdefgh 2 -3 =》 cde</li>
     * </ul>
     *
     * @param text             String
     * @param fromIndexInclude 开始的index（包括）
     * @param toIndexExclude   结束的index（不包括）
     * @return 字串
     */
    public static String sub(final CharSequence text, int fromIndexInclude, int toIndexExclude) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }
        final int len = text.length();

        if (fromIndexInclude < 0) {
            fromIndexInclude = len + fromIndexInclude;
            if (fromIndexInclude < 0) {
                fromIndexInclude = 0;
            }
        } else if (fromIndexInclude > len) {
            fromIndexInclude = len;
        }

        if (toIndexExclude < 0) {
            toIndexExclude = len + toIndexExclude;
            if (toIndexExclude < 0) {
                toIndexExclude = len;
            }
        } else if (toIndexExclude > len) {
            toIndexExclude = len;
        }

        if (toIndexExclude < fromIndexInclude) {
            final int tmp = fromIndexInclude;
            fromIndexInclude = toIndexExclude;
            toIndexExclude = tmp;
        }

        if (fromIndexInclude == toIndexExclude) {
            return Normal.EMPTY;
        }

        return text.toString().substring(fromIndexInclude, toIndexExclude);
    }

    /**
     * 通过CodePoint截取字符串，可以截断Emoji
     *
     * @param text      String
     * @param fromIndex 开始的index（包括）
     * @param toIndex   结束的index（不包括）
     * @return 字串
     */
    public static String subByCodePoint(final CharSequence text, final int fromIndex, final int toIndex) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
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
     * 截取部分字符串，这里一个汉字的长度认为是2
     *
     * @param text   字符串
     * @param len    bytes切割到的位置（包含）
     * @param suffix 切割后加上后缀
     * @return 切割后的字符串
     */
    public static String subPreGbk(final CharSequence text, final int len, final CharSequence suffix) {
        return subPreGbk(text, len, true) + suffix;
    }

    /**
     * 截取部分字符串，这里一个汉字的长度认为是2 可以自定义halfUp，如len为10，如果截取后最后一个字符是半个字符，{@code true}表示保留，则长度是11，否则长度9
     *
     * @param text   字符串
     * @param len    bytes切割到的位置（包含）
     * @param halfUp 遇到截取一半的GBK字符，是否保留。
     * @return 切割后的字符串
     */
    public static String subPreGbk(final CharSequence text, int len, final boolean halfUp) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }

        int counterOfDoubleByte = 0;
        final byte[] b = ByteKit.toBytes(text, Charset.GBK);
        if (b.length <= len) {
            return text.toString();
        }
        for (int i = 0; i < len; i++) {
            if (b[i] < 0) {
                counterOfDoubleByte++;
            }
        }

        if (counterOfDoubleByte % 2 != 0) {
            if (halfUp) {
                len += 1;
            } else {
                len -= 1;
            }
        }
        return new String(b, 0, len, Charset.GBK);
    }

    /**
     * 切割指定位置之前部分的字符串 安全的subString,允许：string为null，允许string长度小于toIndexExclude长度
     * 
     * <pre>{@code
     * Assert.assertEquals(subPre(null, 3), null);
     * Assert.assertEquals(subPre("ab", 3), "ab");
     * Assert.assertEquals(subPre("abc", 3), "abc");
     * Assert.assertEquals(subPre("abcd", 3), "abc");
     * Assert.assertEquals(subPre("abcd", -3), "a");
     * Assert.assertEquals(subPre("ab", 3), "ab");
     * }</pre>
     *
     * @param text           字符串
     * @param toIndexExclude 切割到的位置（不包括）
     * @return 切割后的剩余的前半部分字符串
     */
    public static String subPre(final CharSequence text, final int toIndexExclude) {
        if (isEmpty(text) || text.length() == toIndexExclude) {
            return toStringOrNull(text);
        }
        return sub(text, 0, toIndexExclude);
    }

    /**
     * 切割指定位置之后部分的字符串
     * <ul>
     * <li>fromIndex为0或字符串为空，返回原字符串</li>
     * <li>fromIndex大于字符串本身的长度，返回""</li>
     * <li>fromIndex支持负数，-1表示length-1</li>
     * </ul>
     *
     * @param text      字符串
     * @param fromIndex 切割开始的位置（包括）
     * @return 切割后后剩余的后半部分字符串
     */
    public static String subSuf(final CharSequence text, final int fromIndex) {
        if (0 == fromIndex || isEmpty(text)) {
            return toStringOrNull(text);
        }
        return sub(text, fromIndex, text.length());
    }

    /**
     * 切割指定长度的后部分的字符串
     *
     * <pre>
     * subSufByLength("abcde", 3)      =    "cde"
     * subSufByLength("abcde", 0)      =    ""
     * subSufByLength("abcde", -5)     =    ""
     * subSufByLength("abcde", -1)     =    ""
     * subSufByLength("abcde", 5)       =    "abcde"
     * subSufByLength("abcde", 10)     =    "abcde"
     * subSufByLength(null, 3)               =    null
     * </pre>
     *
     * @param text   字符串
     * @param length 切割长度
     * @return 切割后后剩余的后半部分字符串
     */
    public static String subSufByLength(final CharSequence text, final int length) {
        if (isEmpty(text)) {
            return null;
        }
        if (length <= 0) {
            return Normal.EMPTY;
        }
        return sub(text, -length, text.length());
    }

    /**
     * 截取字符串,从指定位置开始,截取指定长度的字符串 当fromIndex为正数时，这个index指的是插空位置，如下：
     * 
     * <pre>
     *     0   1   2   3   4
     *       A   B   C   D
     * </pre>
     * 
     * 当fromIndex为负数时，为反向插空位置，其中-1表示最后一个字符之前的位置：
     * 
     * <pre>
     *       -3   -2   -1   length
     *     A    B    C    D
     * </pre>
     *
     * @param input     原始字符串
     * @param fromIndex 开始的index,包括，可以为负数
     * @param length    要截取的长度
     * @return 截取后的字符串
     */
    public static String subByLength(final String input, final int fromIndex, final int length) {
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
     * 截取分隔字符串之前的字符串，不包括分隔字符串 如果给定的字符串为空串（null或""）或者分隔字符串为null，返回原字符串 如果分隔字符串为空串""，则返回空串，如果分隔字符串未找到，返回原字符串，举例如下：
     *
     * <pre>
     * subBefore(null, *, false)      = null
     * subBefore("", *, false)        = ""
     * subBefore("abc", "a", false)   = ""
     * subBefore("abcba", "b", false) = "a"
     * subBefore("abc", "c", false)   = "ab"
     * subBefore("abc", "d", false)   = "abc"
     * subBefore("abc", "", false)    = ""
     * subBefore("abc", null, false)  = "abc"
     * </pre>
     *
     * @param text            被查找的字符串
     * @param separator       分隔字符串（不包括）
     * @param isLastSeparator 是否查找最后一个分隔字符串（多次出现分隔字符串时选取最后一个），true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subBefore(final CharSequence text, final CharSequence separator,
            final boolean isLastSeparator) {
        if (isEmpty(text) || separator == null) {
            return null == text ? null : text.toString();
        }

        final String string = text.toString();
        final String sep = separator.toString();
        if (sep.isEmpty()) {
            return Normal.EMPTY;
        }
        final int pos = isLastSeparator ? string.lastIndexOf(sep) : string.indexOf(sep);
        if (Normal.__1 == pos) {
            return string;
        }
        if (0 == pos) {
            return Normal.EMPTY;
        }
        return string.substring(0, pos);
    }

    /**
     * 截取分隔字符串之前的字符串，不包括分隔字符串 如果给定的字符串为空串（null或""）或者分隔字符串为null，返回原字符串 如果分隔字符串未找到，返回原字符串，举例如下：
     *
     * <pre>
     * subBefore(null, *, false)      = null
     * subBefore("", *, false)        = ""
     * subBefore("abc", 'a', false)   = ""
     * subBefore("abcba", 'b', false) = "a"
     * subBefore("abc", 'c', false)   = "ab"
     * subBefore("abc", 'd', false)   = "abc"
     * </pre>
     *
     * @param text            被查找的字符串
     * @param separator       分隔字符串（不包括）
     * @param isLastSeparator 是否查找最后一个分隔字符串（多次出现分隔字符串时选取最后一个），true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subBefore(final CharSequence text, final char separator, final boolean isLastSeparator) {
        if (isEmpty(text)) {
            return null == text ? null : Normal.EMPTY;
        }

        final String newText = text.toString();
        final int pos = isLastSeparator ? newText.lastIndexOf(separator) : newText.indexOf(separator);
        if (Normal.__1 == pos) {
            return newText;
        }
        if (0 == pos) {
            return Normal.EMPTY;
        }
        return newText.substring(0, pos);
    }

    /**
     * 截取分隔字符串之后的字符串，不包括分隔字符串 如果给定的字符串为空串（null或""），返回原字符串 如果分隔字符串为空串（null或""），则返回空串，如果分隔字符串未找到，返回空串，举例如下：
     *
     * <pre>
     * subAfter(null, *, false)      = null
     * subAfter("", *, false)        = ""
     * subAfter(*, null, false)      = ""
     * subAfter("abc", "a", false)   = "ciphers"
     * subAfter("abcba", "b", false) = "cba"
     * subAfter("abc", "c", false)   = ""
     * subAfter("abc", "d", false)   = ""
     * subAfter("abc", "", false)    = "abc"
     * </pre>
     *
     * @param text            被查找的字符串
     * @param separator       分隔字符串（不包括）
     * @param isLastSeparator 是否查找最后一个分隔字符串（多次出现分隔字符串时选取最后一个），true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subAfter(final CharSequence text, final CharSequence separator,
            final boolean isLastSeparator) {
        if (isEmpty(text)) {
            return null == text ? null : Normal.EMPTY;
        }
        if (separator == null) {
            return Normal.EMPTY;
        }
        final String newText = text.toString();
        final String sep = separator.toString();
        final int pos = isLastSeparator ? newText.lastIndexOf(sep) : newText.indexOf(sep);
        if (Normal.__1 == pos || (text.length() - 1) == pos) {
            return Normal.EMPTY;
        }
        return newText.substring(pos + separator.length());
    }

    /**
     * 截取分隔字符串之后的字符串，不包括分隔字符串 如果给定的字符串为空串（null或""），返回原字符串 如果分隔字符串为空串（null或""），则返回空串，如果分隔字符串未找到，返回空串，举例如下：
     *
     * <pre>
     * subAfter(null, *, false)      = null
     * subAfter("", *, false)        = ""
     * subAfter("abc", 'a', false)   = "ciphers"
     * subAfter("abcba", 'b', false) = "cba"
     * subAfter("abc", 'c', false)   = ""
     * subAfter("abc", 'd', false)   = ""
     * </pre>
     *
     * @param text            被查找的字符串
     * @param separator       分隔字符串（不包括）
     * @param isLastSeparator 是否查找最后一个分隔字符串（多次出现分隔字符串时选取最后一个），true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subAfter(final CharSequence text, final char separator, final boolean isLastSeparator) {
        if (isEmpty(text)) {
            return null == text ? null : Normal.EMPTY;
        }
        final String newText = text.toString();
        final int pos = isLastSeparator ? newText.lastIndexOf(separator) : newText.indexOf(separator);
        if (Normal.__1 == pos) {
            return Normal.EMPTY;
        }
        return newText.substring(pos + 1);
    }

    /**
     * 截取指定字符串中间部分，不包括标识字符串
     * 
     * <pre>
     * subBetween("wx[b]yz", "[", "]") = "b"
     * subBetween(null, *, *)          = null
     * subBetween(*, null, *)          = null
     * subBetween(*, *, null)          = null
     * subBetween("", "", "")          = ""
     * subBetween("", "", "]")         = null
     * subBetween("", "[", "]")        = null
     * subBetween("yabcz", "", "")     = ""
     * subBetween("yabcz", "y", "z")   = "abc"
     * subBetween("yabczyabcz", "y", "z")   = "abc"
     * </pre>
     *
     * @param text   被切割的字符串
     * @param before 截取开始的字符串标识
     * @param after  截取到的字符串标识
     * @return 截取后的字符串
     */
    public static String subBetween(final CharSequence text, final CharSequence before, final CharSequence after) {
        if (text == null || before == null || after == null) {
            return null;
        }

        final String text2 = text.toString();
        final String before2 = before.toString();
        final String after2 = after.toString();

        final int start = text2.indexOf(before2);
        if (start != Normal.__1) {
            final int end = text2.indexOf(after2, start + before2.length());
            if (end != Normal.__1) {
                return text2.substring(start + before2.length(), end);
            }
        }
        return null;
    }

    /**
     * 截取指定字符串中间部分，不包括标识字符串
     * 
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
    public static String subBetween(final CharSequence text, final CharSequence beforeAndAfter) {
        return subBetween(text, beforeAndAfter, beforeAndAfter);
    }

    /**
     * 截取指定字符串多段中间部分，不包括标识字符串
     * 
     * <pre>
     * subBetweenAll("wx[b]y[z]", "[", "]") 		= ["b","z"]
     * subBetweenAll(null, *, *)          			= []
     * subBetweenAll(*, null, *)          			= []
     * subBetweenAll(*, *, null)          			= []
     * subBetweenAll("", "", "")          			= []
     * subBetweenAll("", "", "]")         			= []
     * subBetweenAll("", "[", "]")        			= []
     * subBetweenAll("yabcz", "", "")     			= []
     * subBetweenAll("yabcz", "y", "z")   			= ["abc"]
     * subBetweenAll("yabczyabcz", "y", "z")   		= ["abc","abc"]
     * subBetweenAll("[yabc[zy]abcz]", "[", "]");   = ["zy"]           重叠时只截取内部，
     * </pre>
     *
     * @param text   被切割的字符串
     * @param prefix 截取开始的字符串标识
     * @param suffix 截取到的字符串标识
     * @return 截取后的字符串
     */
    public static String[] subBetweenAll(final CharSequence text, final CharSequence prefix,
            final CharSequence suffix) {
        if (hasEmpty(text, prefix, suffix) ||
        // 不包含起始字符串，则肯定没有子串
                !contains(text, prefix)) {
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
     * 
     * <pre>
     * subBetweenAll(null, *)          			= []
     * subBetweenAll(*, null)          			= []
     * subBetweenAll(*, *)          			= []
     * subBetweenAll("", "")          			= []
     * subBetweenAll("", "#")         			= []
     * subBetweenAll("gotanks", "")     		= []
     * subBetweenAll("#gotanks#", "#")   		= ["gotanks"]
     * subBetweenAll("#hello# #world#!", "#")   = ["hello", "world"]
     * subBetweenAll("#hello# world#!", "#");   = ["hello"]
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
     * 重复某个字符
     *
     * <pre>
     * repeat('e', 0)  = ""
     * repeat('e', 3)  = "eee"
     * repeat('e', -2) = ""
     * </pre>
     *
     * @param c     被重复的字符
     * @param count 重复的数目，如果小于等于0则返回""
     * @return 重复字符字符串
     */
    public static String repeat(final char c, final int count) {
        if (count <= 0) {
            return Normal.EMPTY;
        }
        return StringRepeater.of(count).repeat(c);
    }

    /**
     * 重复某个字符串
     *
     * @param text  被重复的字符
     * @param count 重复的数目
     * @return 重复字符字符串
     */
    public static String repeat(final CharSequence text, final int count) {
        if (null == text) {
            return null;
        }
        return StringRepeater.of(count).repeat(text);
    }

    /**
     * 重复某个字符串到指定长度
     * <ul>
     * <li>如果指定长度非指定字符串的整数倍，截断到固定长度</li>
     * <li>如果指定长度小于字符串本身的长度，截断之</li>
     * </ul>
     *
     * @param text   被重复的字符
     * @param padLen 指定长度
     * @return 重复字符字符串
     */
    public static String repeatByLength(final CharSequence text, final int padLen) {
        if (null == text) {
            return null;
        }
        if (padLen <= 0) {
            return Normal.EMPTY;
        }
        return StringRepeater.of(padLen).repeatByLength(text);
    }

    /**
     * 重复某个字符串并通过分界符连接
     *
     * <pre>
     * repeatAndJoin("?", 5, ",")   = "?,?,?,?,?"
     * repeatAndJoin("?", 0, ",")   = ""
     * repeatAndJoin("?", 5, null) = "?????"
     * </pre>
     *
     * @param text      被重复的字符串
     * @param count     数量
     * @param delimiter 分界符
     * @return 连接后的字符串
     */
    public static String repeatAndJoin(final CharSequence text, final int count, final CharSequence delimiter) {
        if (count <= 0) {
            return Normal.EMPTY;
        }
        return StringRepeater.of(count).repeatAndJoin(text, delimiter);
    }

    /**
     * 比较两个字符串（大小写敏感）。
     *
     * <pre>
     * equals(null, null)   = true
     * equals(null, &quot;abc&quot;)  = false
     * equals(&quot;abc&quot;, null)  = false
     * equals(&quot;abc&quot;, &quot;abc&quot;) = true
     * equals(&quot;abc&quot;, &quot;ABC&quot;) = false
     * </pre>
     *
     * @param text1 要比较的字符串1
     * @param text2 要比较的字符串2
     * @return 如果两个字符串相同，或者都是{@code null}，则返回{@code true}
     */
    public static boolean equals(final CharSequence text1, final CharSequence text2) {
        return equals(text1, text2, false);
    }

    /**
     * 比较两个字符串（大小写不敏感）。
     *
     * <pre>
     * equalsIgnoreCase(null, null)   = true
     * equalsIgnoreCase(null, &quot;abc&quot;)  = false
     * equalsIgnoreCase(&quot;abc&quot;, null)  = false
     * equalsIgnoreCase(&quot;abc&quot;, &quot;abc&quot;) = true
     * equalsIgnoreCase(&quot;abc&quot;, &quot;ABC&quot;) = true
     * </pre>
     *
     * @param text1 要比较的字符串1
     * @param text2 要比较的字符串2
     * @return 如果两个字符串相同，或者都是{@code null}，则返回{@code true}
     */
    public static boolean equalsIgnoreCase(final CharSequence text1, final CharSequence text2) {
        return equals(text1, text2, true);
    }

    /**
     * 比较两个字符串是否相等，规则如下
     * <ul>
     * <li>str1和str2都为{@code null}</li>
     * <li>忽略大小写使用{@link String#equalsIgnoreCase(String)}判断相等</li>
     * <li>不忽略大小写使用{@link String#contentEquals(CharSequence)}判断相等</li>
     * </ul>
     *
     * @param text1      要比较的字符串1
     * @param text2      要比较的字符串2
     * @param ignoreCase 是否忽略大小写
     * @return 如果两个字符串相同，或者都是{@code null}，则返回{@code true}
     */
    public static boolean equals(final CharSequence text1, final CharSequence text2, final boolean ignoreCase) {
        if (null == text1) {
            // 只有两个都为null才判断相等
            return text2 == null;
        }
        if (null == text2) {
            // 字符串2空，字符串1非空，直接false
            return false;
        }

        if (ignoreCase) {
            return text1.toString().equalsIgnoreCase(text2.toString());
        } else {
            return text1.toString().contentEquals(text2);
        }
    }

    /**
     * 给定字符串是否与提供的中任意一个字符串相同（忽略大小写），相同则返回{@code true}，没有相同的返回{@code false} 如果参与比对的字符串列表为空，返回{@code false}
     *
     * @param text1 给定需要检查的字符串
     * @param strs  需要参与比对的字符串列表
     * @return 是否相同
     */
    public static boolean equalsAnyIgnoreCase(final CharSequence text1, final CharSequence... strs) {
        return equalsAny(text1, true, strs);
    }

    /**
     * 给定字符串是否与提供的中任一字符串相同，相同则返回{@code true}，没有相同的返回{@code false} 如果参与比对的字符串列表为空，返回{@code false}
     *
     * @param text1 给定需要检查的字符串
     * @param strs  需要参与比对的字符串列表
     * @return 是否相同
     */
    public static boolean equalsAny(final CharSequence text1, final CharSequence... strs) {
        return equalsAny(text1, false, strs);
    }

    /**
     * 给定字符串是否与提供的中任一字符串相同，相同则返回{@code true}，没有相同的返回{@code false} 如果参与比对的字符串列表为空，返回{@code false}
     *
     * @param text       给定需要检查的字符串
     * @param ignoreCase 是否忽略大小写
     * @param args       需要参与比对的字符串列表
     * @return 是否相同
     */
    public static boolean equalsAny(final CharSequence text, final boolean ignoreCase, final CharSequence... args) {
        if (ArrayKit.isEmpty(args)) {
            return false;
        }

        for (final CharSequence cs : args) {
            if (equals(text, cs, ignoreCase)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 字符串指定位置的字符是否与给定字符相同 如果字符串为null，返回false 如果给定的位置大于字符串长度，返回false 如果给定的位置小于0，返回false
     *
     * @param text     字符串
     * @param position 位置
     * @param c        需要对比的字符
     * @return 字符串指定位置的字符是否与给定字符相同
     */
    public static boolean equalsCharAt(final CharSequence text, final int position, final char c) {
        if (null == text || position < 0) {
            return false;
        }
        return text.length() > position && c == text.charAt(position);
    }

    /**
     * 截取第一个字串的部分字符，与第二个字符串比较（长度一致），判断截取的子串是否相同 任意一个字符串为null返回false
     *
     * @param text1      第一个字符串
     * @param offset1    第一个字符串开始的位置
     * @param text2      第二个字符串
     * @param ignoreCase 是否忽略大小写
     * @return 子串是否相同
     * @see String#regionMatches(boolean, int, String, int, int)
     */
    public static boolean isSubEquals(final CharSequence text1, final int offset1, final CharSequence text2,
            final boolean ignoreCase) {
        return isSubEquals(text1, offset1, text2, 0, text2.length(), ignoreCase);
    }

    /**
     * 截取两个字符串的不同部分（长度一致），判断截取的子串是否相同 任意一个字符串为null返回false
     *
     * @param text1      第一个字符串
     * @param offset1    第一个字符串开始的位置
     * @param text2      第二个字符串
     * @param offset2    第二个字符串开始的位置
     * @param length     截取长度
     * @param ignoreCase 是否忽略大小写
     * @return 子串是否相同
     * @see String#regionMatches(boolean, int, String, int, int)
     */
    public static boolean isSubEquals(final CharSequence text1, final int offset1, final CharSequence text2,
            final int offset2, final int length, final boolean ignoreCase) {
        if (null == text1 || null == text2) {
            return false;
        }

        return text1.toString().regionMatches(ignoreCase, offset1, text2.toString(), offset2, length);
    }

    /**
     * 格式化文本, {} 表示占位符 此方法只是简单将占位符 {} 按照顺序替换为参数 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可 例： 通常使用：format("this
     * is {} for {}", "a", "b") = this is a for b 转义{}： format("this is \\{} for {}", "a", "b") = this is {} for a 转义\：
     * format("this is \\\\{} for {}", "a", "b") = this is \a for b
     *
     * @param template 文本模板，被替换的部分用 {} 表示，如果模板为null，返回"null"
     * @param params   参数值
     * @return 格式化后的文本，如果模板为null，返回"null"
     */
    public static String format(final CharSequence template, final Object... params) {
        if (null == template) {
            return Normal.NULL;
        }
        if (ArrayKit.isEmpty(params) || isBlank(template)) {
            return template.toString();
        }
        return StringFormatter.format(template.toString(), params);
    }

    /**
     * 有序的格式化文本，使用{number}做为占位符 通常使用：format("this is {0} for {1}", "a", "b") = this is a for b
     *
     * @param pattern   文本格式
     * @param arguments 参数
     * @return 格式化后的文本
     */
    public static String indexedFormat(final CharSequence pattern, final Object... arguments) {
        return MessageFormat.format(pattern.toString(), arguments);
    }

    /**
     * 包装指定字符串 当前缀和后缀一致时使用此方法
     *
     * @param text            被包装的字符串
     * @param prefixAndSuffix 前缀和后缀
     * @return 包装后的字符串
     */
    public static String wrap(final CharSequence text, final CharSequence prefixAndSuffix) {
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
    public static String wrap(final CharSequence text, final CharSequence prefix, final CharSequence suffix) {
        return toStringOrEmpty(prefix).concat(toStringOrEmpty(text)).concat(toStringOrEmpty(suffix));
    }

    /**
     * 包装指定字符串
     *
     * @param text   被包装的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 包装后的字符串
     */
    public static String wrap(final CharSequence text, final char prefix, final char suffix) {
        return prefix + toStringOrEmpty(text) + suffix;
    }

    /**
     * 使用单个字符包装多个字符串
     *
     * @param prefixAndSuffix 前缀和后缀
     * @param strs            多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAllWithPair(final CharSequence prefixAndSuffix, final CharSequence... strs) {
        return wrapAll(prefixAndSuffix, prefixAndSuffix, strs);
    }

    /**
     * 包装多个字符串
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @param strs   多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAll(final CharSequence prefix, final CharSequence suffix, final CharSequence... strs) {
        final String[] results = new String[strs.length];
        for (int i = 0; i < strs.length; i++) {
            results[i] = wrap(strs[i], prefix, suffix);
        }
        return results;
    }

    /**
     * 包装指定字符串，如果前缀或后缀已经包含对应的字符串，则不再包装
     *
     * @param text   被包装的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 包装后的字符串
     */
    public static String wrapIfMissing(final CharSequence text, final CharSequence prefix, final CharSequence suffix) {
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
        final StringBuilder sb = new StringBuilder(len);
        if (isNotEmpty(prefix) && !startWith(text, prefix)) {
            sb.append(prefix);
        }
        if (isNotEmpty(text)) {
            sb.append(text);
        }
        if (isNotEmpty(suffix) && !endWith(text, suffix)) {
            sb.append(suffix);
        }
        return sb.toString();
    }

    /**
     * 使用成对的字符包装多个字符串，如果已经包装，则不再包装
     *
     * @param prefixAndSuffix 前缀和后缀
     * @param strs            多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAllWithPairIfMissing(final CharSequence prefixAndSuffix, final CharSequence... strs) {
        return wrapAllIfMissing(prefixAndSuffix, prefixAndSuffix, strs);
    }

    /**
     * 包装多个字符串，如果已经包装，则不再包装
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @param strs   多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAllIfMissing(final CharSequence prefix, final CharSequence suffix,
            final CharSequence... strs) {
        final String[] results = new String[strs.length];
        for (int i = 0; i < strs.length; i++) {
            results[i] = wrapIfMissing(strs[i], prefix, suffix);
        }
        return results;
    }

    /**
     * 去掉字符包装，如果未被包装则返回原字符串 此方法要求prefix和suffix都存在，如果只有一个，不做去除。
     *
     * @param text   字符串
     * @param prefix 前置字符串
     * @param suffix 后置字符串
     * @return 去掉包装字符的字符串
     */
    public static String unWrap(final CharSequence text, final String prefix, final String suffix) {
        if (isWrap(text, prefix, suffix)) {
            return sub(text, prefix.length(), text.length() - suffix.length());
        }
        return text.toString();
    }

    /**
     * 去掉字符包装，如果未被包装则返回原字符串
     *
     * @param text   字符串
     * @param prefix 前置字符
     * @param suffix 后置字符
     * @return 去掉包装字符的字符串
     */
    public static String unWrap(final CharSequence text, final char prefix, final char suffix) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }
        if (isWrap(text, prefix, suffix)) {
            return sub(text, 1, text.length() - 1);
        }
        return text.toString();
    }

    /**
     * 去掉字符包装，如果未被包装则返回原字符串
     *
     * @param text            字符串
     * @param prefixAndSuffix 前置和后置字符
     * @return 去掉包装字符的字符串
     */
    public static String unWrap(final CharSequence text, final char prefixAndSuffix) {
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
    public static boolean isWrap(final CharSequence text, final CharSequence prefix, final CharSequence suffix) {
        if (ArrayKit.hasNull(text, prefix, suffix)) {
            return false;
        }
        if (text.length() < (prefix.length() + suffix.length())) {
            return false;
        }

        final String text2 = text.toString();
        return text2.startsWith(prefix.toString()) && text2.endsWith(suffix.toString());
    }

    /**
     * 指定字符串是否被同一字符包装（前后都有这些字符串）
     *
     * @param text    字符串
     * @param wrapper 包装字符串
     * @return 是否被包装
     */
    public static boolean isWrap(final CharSequence text, final String wrapper) {
        return isWrap(text, wrapper, wrapper);
    }

    /**
     * 指定字符串是否被同一字符包装（前后都有这些字符串）
     *
     * @param text    字符串
     * @param wrapper 包装字符
     * @return 是否被包装
     */
    public static boolean isWrap(final CharSequence text, final char wrapper) {
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
    public static boolean isWrap(final CharSequence text, final char prefixChar, final char suffixChar) {
        if (null == text || text.length() < 2) {
            return false;
        }

        return text.charAt(0) == prefixChar && text.charAt(text.length() - 1) == suffixChar;
    }

    /**
     * 获取字符串中最左边的{@code len}字符
     *
     * <pre>
     *  left(null, *)    = null
     *  left(*, -ve)     = ""
     *  left("", *)      = ""
     *  left("abc", 0)   = ""
     *  left("abc", 2)   = "ab"
     *  left("abc", 4)   = "abc"
     * </pre>
     *
     * @param text 要从中获取字符的字符串可能为空
     * @param len  所需字符串的长度
     * @return 最左边的字符，{@code null}如果输入为空字符串
     */
    public static String left(final String text, final int len) {
        if (null == text) {
            return null;
        }
        if (len < 0) {
            return Normal.EMPTY;
        }
        if (text.length() <= len) {
            return text;
        }
        return text.substring(0, len);
    }

    /**
     * 获取字符串中最右边的{@code len}字符
     *
     * <pre>
     *  right(null, *)    = null
     *  right(*, -ve)     = ""
     *  right("", *)      = ""
     *  right("abc", 0)   = ""
     *  right("abc", 2)   = "bc"
     *  right("abc", 4)   = "abc"
     * </pre>
     *
     * @param text 要从中获取字符的字符串可能为空
     * @param len  所需字符串的长度
     * @return 最右边的字符，{@code null}如果输入为空字符串
     */
    public static String right(final String text, final int len) {
        if (null == text) {
            return null;
        }
        if (len < 0) {
            return Normal.EMPTY;
        }
        if (text.length() <= len) {
            return text;
        }
        return text.substring(text.length() - len);
    }

    /**
     * 从字符串中间获取{@code len}字符.
     *
     * <pre>
     *  mid(null, *, *)    = null
     *  mid(*, *, -ve)     = ""
     *  mid("", 0, *)      = ""
     *  mid("abc", 0, 2)   = "ab"
     *  mid("abc", 0, 4)   = "abc"
     *  mid("abc", 2, 4)   = "c"
     *  mid("abc", 4, 2)   = ""
     *  mid("abc", -2, 2)  = "ab"
     * </pre>
     *
     * @param text 要从中获取字符的字符串可能为空
     * @param pos  开始时的位置，负为零
     * @param len  所需字符串的长度
     * @return 中间的字符，{@code null}如果输入为空字符串
     */
    public static String mid(final String text, int pos, final int len) {
        if (null == text) {
            return null;
        }
        if (len < 0 || pos > text.length()) {
            return Normal.EMPTY;
        }
        if (pos < 0) {
            pos = 0;
        }
        if (text.length() <= pos + len) {
            return text.substring(pos);
        }
        return text.substring(pos, pos + len);
    }

    /**
     * 补充字符串以满足指定长度，如果提供的字符串大于指定长度，截断之 同：leftPad (org.apache.commons.lang3.leftPad)
     *
     * <pre>
     * padPre(null, *, *);//null
     * padPre("1", 3, "ABC");//"AB1"
     * padPre("123", 2, "ABC");//"12"
     * padPre("1039", -1, "0");//"103"
     * </pre>
     *
     * @param text   字符串
     * @param length 长度
     * @param padStr 补充的字符
     * @return 补充后的字符串
     */
    public static String padPre(final CharSequence text, final int length, final CharSequence padStr) {
        if (null == text) {
            return null;
        }
        final int strLen = text.length();
        if (strLen == length) {
            return text.toString();
        } else if (strLen > length) {
            // 如果提供的字符串大于指定长度，截断之
            return subPre(text, length);
        }

        return repeatByLength(padStr, length - strLen).concat(text.toString());
    }

    /**
     * 补充字符串以满足最小长度，如果提供的字符串大于指定长度，截断之 同：leftPad (org.apache.commons.lang3.leftPad)
     *
     * <pre>
     * padPre(null, *, *);//null
     * padPre("1", 3, '0');//"001"
     * padPre("123", 2, '0');//"12"
     * </pre>
     *
     * @param text    字符串
     * @param length  长度
     * @param padChar 补充的字符
     * @return 补充后的字符串
     */
    public static String padPre(final CharSequence text, final int length, final char padChar) {
        if (null == text) {
            return null;
        }
        final int strLen = text.length();
        if (strLen == length) {
            return text.toString();
        } else if (strLen > length) {
            // 如果提供的字符串大于指定长度，截断之
            return subPre(text, length);
        }

        return repeat(padChar, length - strLen).concat(text.toString());
    }

    /**
     * 补充字符串以满足最小长度，如果提供的字符串大于指定长度，截断之
     *
     * <pre>
     * padAfter(null, *, *);//null
     * padAfter("1", 3, '0');//"100"
     * padAfter("123", 2, '0');//"23"
     * padAfter("123", -1, '0')//"" 空串
     * </pre>
     *
     * @param text    字符串，如果为{@code null}，直接返回null
     * @param length  长度
     * @param padChar 补充的字符
     * @return 补充后的字符串
     */
    public static String padAfter(final CharSequence text, final int length, final char padChar) {
        if (null == text) {
            return null;
        }
        final int strLen = text.length();
        if (strLen == length) {
            return text.toString();
        } else if (strLen > length) {
            // 如果提供的字符串大于指定长度，截断之
            return sub(text, strLen - length, strLen);
        }

        return text.toString().concat(repeat(padChar, length - strLen));
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
     * @param text   字符串，如果为{@code null}，直接返回null
     * @param length 长度
     * @param padStr 补充的字符
     * @return 补充后的字符串
     */
    public static String padAfter(final CharSequence text, final int length, final CharSequence padStr) {
        if (null == text) {
            return null;
        }
        final int strLen = text.length();
        if (strLen == length) {
            return text.toString();
        } else if (strLen > length) {
            // 如果提供的字符串大于指定长度，截断之
            return subSufByLength(text, length);
        }

        return text.toString().concat(repeatByLength(padStr, length - strLen));
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
    public static String center(final CharSequence text, final int size) {
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
    public static String center(CharSequence text, final int size, final char padChar) {
        if (text == null || size <= 0) {
            return toStringOrNull(text);
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
     * @param text   字符串
     * @param size   指定长度
     * @param padStr 两边补充的字符串
     * @return 补充后的字符串
     */
    public static String center(CharSequence text, final int size, CharSequence padStr) {
        if (text == null || size <= 0) {
            return toStringOrNull(text);
        }
        if (isEmpty(padStr)) {
            padStr = Symbol.SPACE;
        }
        final int strLen = text.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return text.toString();
        }
        text = padPre(text, strLen + pads / 2, padStr);
        text = padAfter(text, size, padStr);
        return text.toString();
    }

    /**
     * 统计指定内容中包含指定字符串的数量 参数为 {@code null} 或者 "" 返回 {@code 0}.
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
    public static int count(final CharSequence content, final CharSequence strForSearch) {
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
    public static int count(final CharSequence content, final char charForSearch) {
        int count = 0;
        if (isEmpty(content)) {
            return 0;
        }
        final int contentLength = content.length();
        for (int i = 0; i < contentLength; i++) {
            if (charForSearch == content.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 比较两个字符串，用于排序
     *
     * <pre>
     * compare(null, null, *)     = 0
     * compare(null , "a", true)  &lt; 0
     * compare(null , "a", false) &gt; 0
     * compare("a", null, true)   &gt; 0
     * compare("a", null, false)  &lt; 0
     * compare("abc", "abc", *)   = 0
     * compare("a", "b", *)       &lt; 0
     * compare("b", "a", *)       &gt; 0
     * compare("a", "B", *)       &gt; 0
     * compare("ab", "abc", *)    &lt; 0
     * </pre>
     *
     * @param text1      字符串1
     * @param text2      字符串2
     * @param nullIsLess {@code null} 值是否排在前（null是否小于非空值）
     * @return 排序值。负数：text1 &lt; text2，正数：text1 &gt; text2, 0：text1 == text2
     */
    public static int compare(final CharSequence text1, final CharSequence text2, final boolean nullIsLess) {
        if (text1 == text2) {
            return 0;
        }
        if (text1 == null) {
            return nullIsLess ? -1 : 1;
        }
        if (text2 == null) {
            return nullIsLess ? 1 : -1;
        }
        return text1.toString().compareTo(text2.toString());
    }

    /**
     * 比较两个字符串，用于排序，大小写不敏感
     *
     * <pre>
     * compareIgnoreCase(null, null, *)     = 0
     * compareIgnoreCase(null , "a", true)  &lt; 0
     * compareIgnoreCase(null , "a", false) &gt; 0
     * compareIgnoreCase("a", null, true)   &gt; 0
     * compareIgnoreCase("a", null, false)  &lt; 0
     * compareIgnoreCase("abc", "abc", *)   = 0
     * compareIgnoreCase("abc", "ABC", *)   = 0
     * compareIgnoreCase("a", "b", *)       &lt; 0
     * compareIgnoreCase("b", "a", *)       &gt; 0
     * compareIgnoreCase("a", "B", *)       &lt; 0
     * compareIgnoreCase("A", "b", *)       &lt; 0
     * compareIgnoreCase("ab", "abc", *)    &lt; 0
     * </pre>
     *
     * @param text1      字符串1
     * @param text2      字符串2
     * @param nullIsLess {@code null} 值是否排在前（null是否小于非空值）
     * @return 排序值。负数：text1 &lt; text2，正数：text1 &gt; text2, 0：text1 == text2
     */
    public static int compareIgnoreCase(final CharSequence text1, final CharSequence text2, final boolean nullIsLess) {
        if (text1 == text2) {
            return 0;
        }
        if (text1 == null) {
            return nullIsLess ? -1 : 1;
        }
        if (text2 == null) {
            return nullIsLess ? 1 : -1;
        }
        return text1.toString().compareToIgnoreCase(text2.toString());
    }

    /**
     * 比较两个版本 null版本排在最小：即：
     *
     * <pre>
     * compareVersion(null, "v1") &lt; 0
     * compareVersion("v1", "v1")  = 0
     * compareVersion(null, null)   = 0
     * compareVersion("v1", null) &gt; 0
     * compareVersion("1.0.0", "1.0.2") &lt; 0
     * compareVersion("1.0.2", "1.0.2a") &lt; 0
     * compareVersion("1.13.0", "1.12.1c") &gt; 0
     * compareVersion("V0.0.20170102", "V0.0.20170101") &gt; 0
     * </pre>
     *
     * @param version1 版本1
     * @param version2 版本2
     * @return 排序值。负数：version1 &lt; version2，正数：version1 &gt; version2, 0：version1 == version2
     */
    public static int compareVersion(final CharSequence version1, final CharSequence version2) {
        return VersionCompare.INSTANCE.compare(toStringOrNull(version1), toStringOrNull(version2));
    }

    /**
     * 如果给定字符串不是以给定的一个或多个字符串为结尾，则在尾部添加结尾字符串 不忽略大小写
     *
     * @param text     被检查的字符串
     * @param suffix   需要添加到结尾的字符串
     * @param suffixes 需要额外检查的结尾字符串，如果以这些中的一个为结尾，则不再添加
     * @return 如果已经结尾，返回原字符串，否则返回添加结尾的字符串
     */
    public static String appendIfMissing(final CharSequence text, final CharSequence suffix,
            final CharSequence... suffixes) {
        return appendIfMissing(text, suffix, false, suffixes);
    }

    /**
     * 如果给定字符串不是以给定的一个或多个字符串为结尾，则在尾部添加结尾字符串 忽略大小写
     *
     * @param text     被检查的字符串
     * @param suffix   需要添加到结尾的字符串
     * @param suffixes 需要额外检查的结尾字符串，如果以这些中的一个为结尾，则不再添加
     * @return 如果已经结尾，返回原字符串，否则返回添加结尾的字符串
     */
    public static String appendIfMissingIgnoreCase(final CharSequence text, final CharSequence suffix,
            final CharSequence... suffixes) {
        return appendIfMissing(text, suffix, true, suffixes);
    }

    /**
     * 如果给定字符串不是以给定的一个或多个字符串为结尾，则在尾部添加结尾字符串
     *
     * @param text         被检查的字符串
     * @param suffix       需要添加到结尾的字符串，不参与检查匹配
     * @param ignoreCase   检查结尾时是否忽略大小写
     * @param testSuffixes 需要额外检查的结尾字符串，如果以这些中的一个为结尾，则不再添加
     * @return 如果已经结尾，返回原字符串，否则返回添加结尾的字符串
     */
    public static String appendIfMissing(final CharSequence text, final CharSequence suffix, final boolean ignoreCase,
            final CharSequence... testSuffixes) {
        if (text == null || isEmpty(suffix) || endWith(text, suffix, ignoreCase)) {
            return toStringOrNull(text);
        }
        if (ArrayKit.isNotEmpty(testSuffixes)) {
            for (final CharSequence testSuffix : testSuffixes) {
                if (endWith(text, testSuffix, ignoreCase)) {
                    return text.toString();
                }
            }
        }
        return text.toString().concat(suffix.toString());
    }

    /**
     * 如果给定字符串不是以给定的一个或多个字符串为开头，则在前面添加起始字符串 不忽略大小写
     *
     * @param text     被检查的字符串
     * @param prefix   需要添加到首部的字符串
     * @param prefixes 需要额外检查的首部字符串，如果以这些中的一个为起始，则不再添加
     * @return 如果已经结尾，返回原字符串，否则返回添加结尾的字符串
     */
    public static String prependIfMissing(final CharSequence text, final CharSequence prefix,
            final CharSequence... prefixes) {
        return prependIfMissing(text, prefix, false, prefixes);
    }

    /**
     * 如果给定字符串不是以给定的一个或多个字符串为开头，则在首部添加起始字符串 忽略大小写
     *
     * @param text     被检查的字符串
     * @param prefix   需要添加到首部的字符串
     * @param prefixes 需要额外检查的首部字符串，如果以这些中的一个为起始，则不再添加
     * @return 如果已经结尾，返回原字符串，否则返回添加结尾的字符串
     */
    public static String prependIfMissingIgnoreCase(final CharSequence text, final CharSequence prefix,
            final CharSequence... prefixes) {
        return prependIfMissing(text, prefix, true, prefixes);
    }

    /**
     * 如果给定字符串不是以给定的一个或多个字符串为开头，则在首部添加起始字符串
     *
     * @param text       被检查的字符串
     * @param prefix     需要添加到首部的字符串
     * @param ignoreCase 检查结尾时是否忽略大小写
     * @param prefixes   需要额外检查的首部字符串，如果以这些中的一个为起始，则不再添加
     * @return 如果已经结尾，返回原字符串，否则返回添加结尾的字符串
     */
    public static String prependIfMissing(final CharSequence text, final CharSequence prefix, final boolean ignoreCase,
            final CharSequence... prefixes) {
        if (text == null || isEmpty(prefix) || startWith(text, prefix, ignoreCase)) {
            return toStringOrNull(text);
        }
        if (ArrayKit.isNotEmpty(prefixes)) {
            for (final CharSequence s : prefixes) {
                if (startWith(text, s, ignoreCase)) {
                    return text.toString();
                }
            }
        }
        return prefix.toString().concat(text.toString());
    }

    /**
     * 替换字符串中第一个指定字符串
     *
     * @param text        字符串
     * @param searchStr   被查找的字符串
     * @param replacedStr 被替换的字符串
     * @param ignoreCase  是否忽略大小写
     * @return 替换后的字符串
     */
    public static String replaceFirst(final CharSequence text, final CharSequence searchStr,
            final CharSequence replacedStr, final boolean ignoreCase) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }
        final int startInclude = indexOf(text, searchStr, 0, ignoreCase);
        if (Normal.__1 == startInclude) {
            return toStringOrNull(text);
        }
        return replaceByCodePoint(text, startInclude, startInclude + searchStr.length(), replacedStr);
    }

    /**
     * 替换字符串中最后一个指定字符串
     *
     * @param text        字符串
     * @param searchStr   被查找的字符串
     * @param replacedStr 被替换的字符串
     * @param ignoreCase  是否忽略大小写
     * @return 替换后的字符串
     */
    public static String replaceLast(final CharSequence text, final CharSequence searchStr,
            final CharSequence replacedStr, final boolean ignoreCase) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }
        final int lastIndex = lastIndexOf(text, searchStr, text.length(), ignoreCase);
        if (Normal.__1 == lastIndex) {
            return toStringOrNull(text);
        }
        return replace(text, lastIndex, searchStr, replacedStr, ignoreCase);
    }

    /**
     * 替换字符串中的指定字符串，忽略大小写
     *
     * @param text        字符串
     * @param searchStr   被查找的字符串
     * @param replacement 被替换的字符串
     * @return 替换后的字符串
     */
    public static String replaceIgnoreCase(final CharSequence text, final CharSequence searchStr,
            final CharSequence replacement) {
        return replace(text, 0, searchStr, replacement, true);
    }

    /**
     * 替换字符串中的指定字符串
     *
     * @param text        字符串
     * @param searchStr   被查找的字符串
     * @param replacement 被替换的字符串
     * @return 替换后的字符串
     */
    public static String replace(final CharSequence text, final CharSequence searchStr,
            final CharSequence replacement) {
        return replace(text, 0, searchStr, replacement, false);
    }

    /**
     * 替换字符串中的指定字符串
     *
     * @param text        字符串
     * @param searchStr   被查找的字符串
     * @param replacement 被替换的字符串
     * @param ignoreCase  是否忽略大小写
     * @return 替换后的字符串
     */
    public static String replace(final CharSequence text, final CharSequence searchStr, final CharSequence replacement,
            final boolean ignoreCase) {
        return replace(text, 0, searchStr, replacement, ignoreCase);
    }

    /**
     * 替换字符串中的指定字符串 如果指定字符串出现多次，则全部替换
     *
     * @param text        字符串
     * @param fromIndex   开始位置（包括）
     * @param searchStr   被查找的字符串
     * @param replacement 被替换的字符串
     * @param ignoreCase  是否忽略大小写
     * @return 替换后的字符串
     */
    public static String replace(final CharSequence text, final int fromIndex, final CharSequence searchStr,
            final CharSequence replacement, final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(searchStr)) {
            return toStringOrNull(text);
        }
        return new SearchReplacer(fromIndex, searchStr, replacement, ignoreCase).apply(text);
    }

    /**
     * 替换指定字符串的指定区间内字符为固定字符，替换后字符串长度不变 如替换的区间长度为10，则替换后的字符重复10次 此方法使用{@link String#codePoints()}完成拆分替换
     *
     * @param text         字符串
     * @param beginInclude 开始位置（包含）
     * @param endExclude   结束位置（不包含）
     * @param replacedChar 被替换的字符
     * @return 替换后的字符串
     */
    public static String replaceByCodePoint(final CharSequence text, final int beginInclude, final int endExclude,
            final char replacedChar) {
        return new CharRangeReplacer(beginInclude, endExclude, replacedChar, true).apply(text);
    }

    /**
     * 替换指定字符串的指定区间内字符为指定字符串，字符串只重复一次 此方法使用{@link String#codePoints()}完成拆分替换
     *
     * @param text         字符串
     * @param beginInclude 开始位置（包含）
     * @param endExclude   结束位置（不包含）
     * @param replacedStr  被替换的字符串
     * @return 替换后的字符串
     */
    public static String replaceByCodePoint(final CharSequence text, final int beginInclude, final int endExclude,
            final CharSequence replacedStr) {
        return new StringRangeReplacer(beginInclude, endExclude, replacedStr, true).apply(text);
    }

    /**
     * 替换所有正则匹配的文本，并使用自定义函数决定如何替换 replaceFun可以提取出匹配到的内容的不同部分，然后经过重新处理、组装变成新的内容放回原位。
     * 
     * <pre class="code">
     * replace(this.content, "(\\d+)", parameters -&gt; "-" + parameters.group(1) + "-")
     * // 结果为："ZZZaaabbbccc中文-1234-"
     * </pre>
     *
     * @param text       要替换的字符串
     * @param pattern    用于匹配的正则式
     * @param replaceFun 决定如何替换的函数
     * @return 替换后的字符串
     * @see PatternKit#replaceAll(CharSequence, java.util.regex.Pattern, FunctionX)
     */
    public static String replace(final CharSequence text, final java.util.regex.Pattern pattern,
            final FunctionX<Matcher, String> replaceFun) {
        return PatternKit.replaceAll(text, pattern, replaceFun);
    }

    /**
     * 替换所有正则匹配的文本，并使用自定义函数决定如何替换
     *
     * @param text       要替换的字符串
     * @param regex      用于匹配的正则式
     * @param replaceFun 决定如何替换的函数
     * @return 替换后的字符串
     * @see PatternKit#replaceAll(CharSequence, String, FunctionX)
     */
    public static String replace(final CharSequence text, final String regex,
            final FunctionX<Matcher, String> replaceFun) {
        return PatternKit.replaceAll(text, regex, replaceFun);
    }

    /**
     * 替换指定字符串的指定区间内字符为"*"
     *
     * <pre>
     * hide(null,*,*)=null
     * hide("",0,*)=""
     * hide("jackduan@163.com",-1,4)   ****duan@163.com
     * hide("jackduan@163.com",2,3)    ja*kduan@163.com
     * hide("jackduan@163.com",3,2)    jackduan@163.com
     * hide("jackduan@163.com",16,16)  jackduan@163.com
     * hide("jackduan@163.com",16,17)  jackduan@163.com
     * </pre>
     *
     * @param text         字符串
     * @param startInclude 开始位置（包含）
     * @param endExclude   结束位置（不包含）
     * @return 替换后的字符串
     */
    public static String hide(final CharSequence text, final int startInclude, final int endExclude) {
        return replaceByCodePoint(text, startInclude, endExclude, Symbol.C_STAR);
    }

    /**
     * 替换字符字符数组中所有的字符为replacedStr 提供的chars为所有需要被替换的字符，例如："\r\n"，则"\r"和"\n"都会被替换，哪怕他们单独存在
     *
     * @param text        被检查的字符串
     * @param chars       需要替换的字符列表，用一个字符串表示这个字符列表
     * @param replacedStr 替换成的字符串
     * @return 新字符串
     */
    public static String replaceChars(final CharSequence text, final String chars, final CharSequence replacedStr) {
        if (isEmpty(text) || isEmpty(chars)) {
            return toStringOrNull(text);
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
    public static String replaceChars(final CharSequence text, final char[] chars, final CharSequence replacedStr) {
        if (isEmpty(text) || ArrayKit.isEmpty(chars)) {
            return toStringOrNull(text);
        }

        final Set<Character> set = new HashSet<>(chars.length);
        for (final char c : chars) {
            set.add(c);
        }
        final int strLen = text.length();
        final StringBuilder builder = new StringBuilder();
        char c;
        for (int i = 0; i < strLen; i++) {
            c = text.charAt(i);
            builder.append(set.contains(c) ? replacedStr : c);
        }
        return builder.toString();
    }

    /**
     * 按照给定逻辑替换指定位置的字符，如字符大小写转换等
     *
     * @param text     字符串
     * @param index    位置，-1表示最后一个字符
     * @param operator 替换逻辑，给定原字符，返回新字符
     * @return 替换后的字符串
     */
    public static String replaceAt(final CharSequence text, int index, final UnaryOperator<Character> operator) {
        if (text == null) {
            return null;
        }

        // 支持负数
        final int length = text.length();
        if (index < 0) {
            index += length;
        }

        final String string = text.toString();
        if (index < 0 || index >= length) {
            return string;
        }

        // 检查转换前后是否有编码，无变化则不转换，返回原字符串
        final char c = string.charAt(index);
        final Character newC = operator.apply(c);
        if (c == newC) {
            // 无变化，返回原字符串
            return string;
        }

        // 此处不复用传入的CharSequence，防止修改原对象
        final char[] chars = string.toCharArray();
        chars[index] = newC;
        return new String(chars);
    }

    /**
     * 获取字符串的长度，如果为null返回0
     *
     * @param cs a 字符串
     * @return 字符串的长度，如果为null返回0
     */
    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /**
     * 获取字符串的Unicode字符长度，如果为{@code null}返回0 Unicode字符长度指实际Unicode字符个数，如emoji算一个字符
     *
     * @param cs a 字符串
     * @return 字符串的长度，如果为{@code null}返回0
     */
    public static int codeLength(final CharSequence cs) {
        return cs == null ? 0 : cs.toString().codePointCount(0, cs.length());
    }

    /**
     * 给定字符串转为bytes后的byte数（byte长度）
     *
     * @param cs      字符串
     * @param charset 编码
     * @return byte长度
     */
    public static int byteLength(final CharSequence cs, final java.nio.charset.Charset charset) {
        return cs == null ? 0 : cs.toString().getBytes(charset).length;
    }

    /**
     * 给定字符串数组的总长度 null字符长度定义为0
     *
     * @param args 字符串数组
     * @return 总长度
     */
    public static int totalLength(final CharSequence... args) {
        int totalLength = 0;
        for (final CharSequence text : args) {
            totalLength += length(text);
        }
        return totalLength;
    }

    /**
     * 限制字符串长度，如果超过指定长度，截取指定长度并在末尾加"..."
     *
     * @param text   字符串
     * @param length 最大长度
     * @return 切割后的剩余的前半部分字符串+"..."
     */
    public static String limitLength(final CharSequence text, final int length) {
        Assert.isTrue(length > 0);
        if (null == text) {
            return null;
        }
        if (text.length() <= length) {
            return text.toString();
        }
        return sub(text, 0, length) + "...";
    }

    /**
     * 截断字符串，使用UTF8编码为字节后不超过maxBytes长度
     *
     * @param text           原始字符串
     * @param maxBytesLength 最大字节数
     * @param appendDots     截断后是否追加省略号(...)
     * @return 限制后的长度
     */
    public static String limitByteLengthUtf8(final CharSequence text, final int maxBytesLength,
            final boolean appendDots) {
        return limitByteLength(text, Charset.UTF_8, maxBytesLength, 4, appendDots);
    }

    /**
     * 截断字符串，使用其按照指定编码为字节后不超过maxBytes长度 此方法用于截取总bytes数不超过指定长度，如果字符出没有超出原样输出，如果超出了，则截取掉超出部分，并可选添加...，
     * 但是添加“...”后总长度也不超过限制长度。
     *
     * @param text           原始字符串
     * @param charset        指定编码
     * @param maxBytesLength 最大字节数
     * @param factor         速算因子，取该编码下单个字符的最大可能字节数
     * @param appendDots     截断后是否追加省略号(...)
     * @return 限制后的长度
     */
    public static String limitByteLength(final CharSequence text, final java.nio.charset.Charset charset,
            final int maxBytesLength, final int factor, final boolean appendDots) {
        // 字符数*速算因子<=最大字节数
        if (text == null || text.length() * factor <= maxBytesLength) {
            return toStringOrNull(text);
        }
        final byte[] sba = ByteKit.toBytes(text, charset);
        if (sba.length <= maxBytesLength) {
            return toStringOrNull(text);
        }
        // 限制字节数
        final int limitBytes;
        if (appendDots) {
            limitBytes = maxBytesLength - "...".getBytes(charset).length;
        } else {
            limitBytes = maxBytesLength;
        }
        final ByteBuffer bb = ByteBuffer.wrap(sba, 0, limitBytes);
        final CharBuffer cb = CharBuffer.allocate(limitBytes);
        final CharsetDecoder decoder = charset.newDecoder();
        // 忽略被截断的字符
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        decoder.decode(bb, cb, true);
        decoder.flush(cb);
        final String result = new String(cb.array(), 0, cb.position());
        if (appendDots) {
            return result + "...";
        }
        return result;
    }

    /**
     * 返回第一个非{@code null} 元素
     *
     * @param args 多个元素
     * @param <T>  元素类型
     * @return 第一个非空元素，如果给定的数组为空或者都为空，返回{@code null}
     */
    public static <T extends CharSequence> T firstNonNull(final T... args) {
        return ArrayKit.firstNonNull(args);
    }

    /**
     * 返回第一个非empty 元素
     *
     * @param args 多个元素
     * @param <T>  元素类型
     * @return 第一个非空元素，如果给定的数组为空或者都为空，返回{@code null}
     * @see #isNotEmpty(CharSequence)
     */
    public static <T extends CharSequence> T firstNonEmpty(final T... args) {
        return ArrayKit.firstMatch(CharsBacker::isNotEmpty, args);
    }

    /**
     * 返回第一个非blank 元素
     *
     * @param args 多个元素
     * @param <T>  元素类型
     * @return 第一个非空元素，如果给定的数组为空或者都为空，返回{@code null}
     * @see #isNotBlank(CharSequence)
     */
    public static <T extends CharSequence> T firstNonBlank(final T... args) {
        return ArrayKit.firstMatch(CharsBacker::isNotBlank, args);
    }

    /**
     * 原字符串首字母大写并在其首部添加指定字符串 例如：text=name, preString=get = return getName
     *
     * @param text      被处理的字符串
     * @param preString 添加的首部
     * @return 处理后的字符串
     */
    public static String upperFirstAndAddPre(final CharSequence text, final String preString) {
        if (text == null || preString == null) {
            return null;
        }
        return preString + upperFirst(text);
    }

    /**
     * 大写首字母 例如：text = name, return Name
     *
     * @param text 字符串
     * @return 字符串
     */
    public static String upperFirst(final CharSequence text) {
        return upperAt(text, 0);
    }

    /**
     * 大写对应下标字母
     *
     * <pre>
     * 例如: text = name,index = 1, return nAme
     * </pre>
     *
     * @param text  字符串
     * @param index 下标，支持负数，-1表示最后一个字符
     * @return 字符串
     */
    public static String upperAt(final CharSequence text, final int index) {
        return replaceAt(text, index, Character::toUpperCase);
    }

    /**
     * 小写首字母 例如：text = Name, return name
     *
     * @param text 字符串
     * @return 字符串
     */
    public static String lowerFirst(final CharSequence text) {
        return lowerAt(text, 0);
    }

    /**
     * 小写对应下标字母 例如: text = NAME,index = 1, return NaME
     *
     * @param text  字符串
     * @param index 下标，支持负数，-1表示最后一个字符
     * @return 字符串
     */
    public static String lowerAt(final CharSequence text, final int index) {
        return replaceAt(text, index, Character::toLowerCase);
    }

    /**
     * 过滤字符串
     *
     * @param text      字符串
     * @param predicate 过滤器，{@link Predicate#test(Object)}为{@code true}保留字符
     * @return 过滤后的字符串
     */
    public static String filter(final CharSequence text, final Predicate<Character> predicate) {
        if (text == null || predicate == null) {
            return toStringOrNull(text);
        }

        final int len = text.length();
        final StringBuilder sb = new StringBuilder(len);
        char c;
        for (int i = 0; i < len; i++) {
            c = text.charAt(i);
            if (predicate.test(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 给定字符串中的字母是否全部为大写，判断依据如下：
     *
     * <pre>
     * 1. 大写字母包括A-Z
     * 2. 其它非字母的Unicode符都算作大写
     * </pre>
     *
     * @param text 被检查的字符串
     * @return 是否全部为大写
     */
    public static boolean isUpperCase(final CharSequence text) {
        return NamingCase.isUpperCase(text);
    }

    /**
     * 给定字符串中的字母是否全部为小写，判断依据如下：
     *
     * <pre>
     * 1. 小写字母包括a-z
     * 2. 其它非字母的Unicode符都算作小写
     * </pre>
     *
     * @param text 被检查的字符串
     * @return 是否全部为小写
     */
    public static boolean isLowerCase(final CharSequence text) {
        return NamingCase.isLowerCase(text);
    }

    /**
     * 切换给定字符串中的大小写。大写转小写，小写转大写。
     *
     * <pre>
     * swapCase(null)                 = null
     * swapCase("")                   = ""
     * swapCase("The dog has a BONE") = "tHE DOG HAS A bone"
     * </pre>
     *
     * @param text 字符串
     * @return 交换后的字符串
     */
    public static String swapCase(final String text) {
        return NamingCase.swapCase(text);
    }

    /**
     * 将驼峰式命名的字符串转换为下划线方式。如果转换前的驼峰式命名的字符串为空，则返回空字符串。 例如：
     *
     * <pre>
     * HelloWorld = hello_world
     * Hello_World = hello_world
     * HelloWorld_test = hello_world_test
     * </pre>
     *
     * @param text 转换前的驼峰式命名的字符串，也可以为下划线形式
     * @return 转换后下划线方式命名的字符串
     * @see NamingCase#toUnderlineCase(CharSequence)
     */
    public static String toUnderlineCase(final CharSequence text) {
        return NamingCase.toUnderlineCase(text);
    }

    /**
     * 将驼峰式命名的字符串转换为使用符号连接方式。如果转换前的驼峰式命名的字符串为空，则返回空字符串。
     *
     * @param text   转换前的驼峰式命名的字符串，也可以为符号连接形式
     * @param symbol 连接符
     * @return 转换后符号连接方式命名的字符串
     * @see NamingCase#toSymbolCase(CharSequence, char)
     */
    public static String toSymbolCase(final CharSequence text, final char symbol) {
        return NamingCase.toSymbolCase(text, symbol);
    }

    /**
     * 将下划线方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。 例如：hello_world = helloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     * @see NamingCase#toCamelCase(CharSequence)
     */
    public static String toCamelCase(final CharSequence name) {
        return NamingCase.toCamelCase(name);
    }

    /**
     * 将连接符方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。 例如：hello_world = helloWorld; hello-world = helloWorld
     *
     * @param name   转换前的下划线大写方式命名的字符串
     * @param symbol 连接符
     * @return 转换后的驼峰式命名的字符串
     * @see NamingCase#toCamelCase(CharSequence, char)
     */
    public static String toCamelCase(final CharSequence name, final char symbol) {
        return NamingCase.toCamelCase(name, symbol);
    }

    /**
     * 创建StringBuilder对象 如果对象本身为{@link StringBuilder}，直接返回，否则新建
     *
     * @param text {@link CharSequence}
     * @return StringBuilder对象
     */
    public static StringBuilder builder(final CharSequence text) {
        return text instanceof StringBuilder ? (StringBuilder) text : new StringBuilder(text);
    }

    /**
     * 创建StringBuilder对象
     *
     * @param args 初始字符串列表
     * @return StringBuilder对象
     */
    public static StringBuilder builder(final CharSequence... args) {
        final StringBuilder sb = new StringBuilder();
        for (final CharSequence text : args) {
            sb.append(text);
        }
        return sb;
    }

    /**
     * 获得set或get或is方法对应的标准属性名 例如：setName 返回 name
     *
     * <pre>
     * getName = name
     * setName = name
     * isName  = name
     * </pre>
     *
     * @param getOrSetMethodName Get或Set方法名
     * @return 如果是set或get方法名，返回field， 否则null
     */
    public static String getGeneralField(final CharSequence getOrSetMethodName) {
        final String getOrSetMethodNameStr = getOrSetMethodName.toString();
        if (getOrSetMethodNameStr.startsWith("get") || getOrSetMethodNameStr.startsWith("set")) {
            return removePreAndLowerFirst(getOrSetMethodName, 3);
        } else if (getOrSetMethodNameStr.startsWith("is")) {
            return removePreAndLowerFirst(getOrSetMethodName, 2);
        }
        return null;
    }

    /**
     * 生成set方法名 例如：name 返回 setName
     *
     * @param fieldName 属性名
     * @return setXxx
     */
    public static String genSetter(final CharSequence fieldName) {
        return upperFirstAndAddPre(fieldName, "set");
    }

    /**
     * 生成get方法名
     *
     * @param fieldName 属性名
     * @return getXxx
     */
    public static String genGetter(final CharSequence fieldName) {
        return upperFirstAndAddPre(fieldName, "get");
    }

    /**
     * 连接多个字符串为一个
     *
     * @param isNullToEmpty 是否null转为""
     * @param args          字符串数组
     * @return 连接后的字符串
     */
    public static String concat(final boolean isNullToEmpty, final CharSequence... args) {
        final StringBuilder sb = new StringBuilder();
        for (final CharSequence text : args) {
            sb.append(isNullToEmpty ? toStringOrEmpty(text) : text);
        }
        return sb.toString();
    }

    /**
     * 将给定字符串，变成 "xxx...xxx" 形式的字符串
     *
     * <ul>
     * <li>abcdefgh 9 - abcdefgh</li>
     * <li>abcdefgh 8 - abcdefgh</li>
     * <li>abcdefgh 7 - ab...gh</li>
     * <li>abcdefgh 6 - ab...h</li>
     * <li>abcdefgh 5 - a...h</li>
     * <li>abcdefgh 4 - a..h</li>
     * <li>abcdefgh 3 - a.h</li>
     * <li>abcdefgh 2 - a.</li>
     * <li>abcdefgh 1 - a</li>
     * <li>abcdefgh 0 - abcdefgh</li>
     * <li>abcdefgh -1 - abcdefgh</li>
     * </ul>
     *
     * @param text      字符串
     * @param maxLength 结果的最大长度
     * @return 截取后的字符串
     */
    public static String brief(final CharSequence text, final int maxLength) {
        if (null == text) {
            return null;
        }
        final int strLength = text.length();
        if (maxLength <= 0 || strLength <= maxLength) {
            return text.toString();
        }

        // 特殊长度
        switch (maxLength) {
        case 1:
            return String.valueOf(text.charAt(0));
        case 2:
            return text.charAt(0) + ".";
        case 3:
            return text.charAt(0) + "." + text.charAt(strLength - 1);
        case 4:
            return text.charAt(0) + ".." + text.charAt(strLength - 1);
        }

        final int suffixLength = (maxLength - 3) / 2;
        final int preLength = suffixLength + (maxLength - 3) % 2; // suffixLength 或 suffixLength + 1
        final String text2 = text.toString();
        return format("{}...{}", text2.substring(0, preLength), text2.substring(strLength - suffixLength));
    }

    /**
     * 以 conjunction 为分隔符将多个对象转换为字符串
     *
     * @param conjunction 分隔符 {@link Symbol#COMMA}
     * @param objs        数组
     * @return 连接后的字符串
     * @see ArrayKit#join(Object, CharSequence)
     */
    public static String join(final CharSequence conjunction, final Object... objs) {
        return ArrayKit.join(objs, conjunction);
    }

    /**
     * 以 conjunction 为分隔符将多个对象转换为字符串
     *
     * @param <T>         元素类型
     * @param conjunction 分隔符 {@link Symbol#COMMA}
     * @param iterable    集合
     * @return 连接后的字符串
     * @see CollKit#join(Iterable, CharSequence)
     */
    public static <T> String join(final CharSequence conjunction, final Iterable<T> iterable) {
        return CollKit.join(iterable, conjunction);
    }

    /**
     * 检查字符串是否都为数字组成
     *
     * @param text 字符串
     * @return 是否都为数字组成
     */
    public static boolean isNumeric(final CharSequence text) {
        return isAllCharMatch(text, Character::isDigit);
    }

    /**
     * 循环位移指定位置的字符串为指定距离 当moveLength大于0向右位移，小于0向左位移，0不位移 当moveLength大于字符串长度时采取循环位移策略，即位移到头后从头（尾）位移，例如长度为10，位移13则表示位移3
     *
     * @param text         字符串
     * @param startInclude 起始位置（包括）
     * @param endExclude   结束位置（不包括）
     * @param moveLength   移动距离，负数表示左移，正数为右移
     * @return 位移后的字符串
     */
    public static String move(final CharSequence text, final int startInclude, final int endExclude, int moveLength) {
        if (isEmpty(text)) {
            return toStringOrNull(text);
        }
        final int len = text.length();
        if (Math.abs(moveLength) > len) {
            // 循环位移，当越界时循环
            moveLength = moveLength % len;
        }
        final StringBuilder strBuilder = new StringBuilder(len);
        if (moveLength > 0) {
            final int endAfterMove = Math.min(endExclude + moveLength, text.length());
            strBuilder.append(text.subSequence(0, startInclude)).append(text.subSequence(endExclude, endAfterMove))
                    .append(text.subSequence(startInclude, endExclude))
                    .append(text.subSequence(endAfterMove, text.length()));
        } else if (moveLength < 0) {
            final int startAfterMove = Math.max(startInclude + moveLength, 0);
            strBuilder.append(text.subSequence(0, startAfterMove)).append(text.subSequence(startInclude, endExclude))
                    .append(text.subSequence(startAfterMove, startInclude))
                    .append(text.subSequence(endExclude, text.length()));
        } else {
            return toStringOrNull(text);
        }
        return strBuilder.toString();
    }

    /**
     * 检查给定字符串的所有字符是否都一样
     *
     * @param text 字符出啊
     * @return 给定字符串的所有字符是否都一样
     */
    public static boolean isCharEquals(final CharSequence text) {
        Assert.notEmpty(text, "Text to check must be not empty!");
        return count(text, text.charAt(0)) == text.length();
    }

    /**
     * 对字符串归一化处理，如 "Á" 可以使用 "u00C1"或 "u0041u0301"表示，实际测试中两个字符串并不equals 因此使用此方法归一为一种表示形式，默认按照W3C通常建议的，在NFC中交换文本。
     *
     * @param text 归一化的字符串
     * @return 归一化后的字符串
     * @see Normalizer#normalize(CharSequence, Normalizer.Form)
     */
    public static String normalize(final CharSequence text) {
        return Normalizer.normalize(text, Normalizer.Form.NFC);
    }

    /**
     * 在给定字符串末尾填充指定字符，以达到给定长度 如果字符串本身的长度大于等于length，返回原字符串
     *
     * @param text      字符串
     * @param fixedChar 补充的字符
     * @param length    补充到的长度
     * @return 补充后的字符串
     */
    public static String fixLength(final CharSequence text, final char fixedChar, final int length) {
        final int fixedLength = length - text.length();
        if (fixedLength <= 0) {
            return text.toString();
        }
        return text + repeat(fixedChar, fixedLength);
    }

    /**
     * 获取两个字符串的公共前缀
     * 
     * <pre>{@code
     * commonPrefix("abb", "acc") // "a"
     * }</pre>
     *
     * @param text1 字符串1
     * @param text2 字符串2
     * @return 字符串1和字符串2的公共前缀
     */
    public static CharSequence commonPrefix(final CharSequence text1, final CharSequence text2) {
        if (isEmpty(text1) || isEmpty(text2)) {
            return Normal.EMPTY;
        }
        final int minLength = Math.min(text1.length(), text2.length());
        int index = 0;
        for (; index < minLength; index++) {
            if (text1.charAt(index) != text2.charAt(index)) {
                break;
            }
        }
        return text1.subSequence(0, index);
    }

    /**
     * 获取两个字符串的公共后缀
     * 
     * <pre>{@code
     * commonSuffix("aba", "cba") // "ba"
     * }</pre>
     *
     * @param text1 字符串1
     * @param text2 字符串2
     * @return 字符串1和字符串2的公共后缀
     */
    public static CharSequence commonSuffix(final CharSequence text1, final CharSequence text2) {
        if (isEmpty(text1) || isEmpty(text2)) {
            return Normal.EMPTY;
        }
        int str1Index = text1.length() - 1;
        int str2Index = text2.length() - 1;
        for (; str1Index >= 0 && str2Index >= 0; str1Index--, str2Index--) {

            if (text1.charAt(str1Index) != text2.charAt(str2Index)) {
                break;
            }

        }
        return text1.subSequence(str1Index + 1, text1.length());
    }

    /**
     * 切分字符串，去除切分后每个元素两边的空白符，去除空白项，并转为结果类型
     *
     * @param <T>        结果类型
     * @param text       被切分的字符串
     * @param separator  分隔符字符
     * @param resultType 结果类型的类，可以是数组或集合
     * @return long数组
     */
    public static <T> T splitTo(final CharSequence text, final CharSequence separator, final Class<T> resultType) {
        return Convert.convert(resultType, splitTrim(text, separator));
    }

    /**
     * 切分字符串，去除切分后每个元素两边的空白符，去除空白项
     *
     * @param text      被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     */
    public static List<String> splitTrim(final CharSequence text, final CharSequence separator) {
        return split(text, separator, true, true);
    }

    /**
     * 切分字符串，如果分隔符不存在则返回原字符串 此方法不会去除切分字符串后每个元素两边的空格，不忽略空串
     *
     * @param text      被切分的字符串
     * @param separator 分隔符
     * @return 字符串
     */
    public static String[] splitToArray(final CharSequence text, final CharSequence separator) {
        if (text == null) {
            return new String[] {};
        }
        return split(text, separator).toArray(new String[0]);
    }

    /**
     * 切分字符串
     *
     * @param text 被切分的字符串
     * @return 字符串
     */
    public static String split(String text) {
        return split(text, Symbol.COMMA, Symbol.COMMA);
    }

    /**
     * 切分字符串
     *
     * @param text      被切分的字符串
     * @param separator 分隔符
     * @param reserve   替换后的分隔符
     * @return 字符串
     */
    public static String split(String text, String separator, String reserve) {
        StringBuffer sb = new StringBuffer();
        if (isNotEmpty(text)) {
            String[] arr = splitToArray(text, separator);
            for (int i = 0; i < arr.length; i++) {
                if (i == 0) {
                    sb.append(Symbol.SINGLE_QUOTE).append(arr[i]).append(Symbol.SINGLE_QUOTE);
                } else {
                    sb.append(reserve).append(Symbol.SINGLE_QUOTE).append(arr[i]).append(Symbol.SINGLE_QUOTE);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 切分字符串，如果分隔符不存在则返回原字符串 此方法不会去除切分字符串后每个元素两边的空格，不忽略空串
     *
     * @param text      被切分的字符串
     * @param separator 分隔符
     * @return 字符串
     */
    public static List<String> split(final CharSequence text, final CharSequence separator) {
        return split(text, separator, false, false);
    }

    /**
     * 切分字符串，不忽略大小写
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符串
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(final CharSequence text, final CharSequence separator, final boolean isTrim,
            final boolean ignoreEmpty) {
        return split(text, separator, Normal.__1, isTrim, ignoreEmpty, false);
    }

    /**
     * 切分字符串，不忽略大小写
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数，小于等于0表示无限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(final CharSequence text, final CharSequence separator, final int limit,
            final boolean isTrim, final boolean ignoreEmpty) {
        return split(text, separator, limit, isTrim, ignoreEmpty, false);
    }

    /**
     * 切分字符串 如果提供的字符串为{@code null}，则返回一个空的{@link ArrayList}
     * 如果提供的字符串为""，则当ignoreEmpty时返回空的{@link ArrayList}，否则返回只有一个""元素的{@link ArrayList}
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数，小于等于0表示无限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @param ignoreCase  是否忽略大小写
     * @return 切分后的集合
     */
    public static List<String> split(final CharSequence text, final CharSequence separator, final int limit,
            final boolean isTrim, final boolean ignoreEmpty, final boolean ignoreCase) {
        return split(text, separator, limit, ignoreEmpty, ignoreCase, trimFunc(isTrim));
    }

    /**
     * 切分字符串 如果提供的字符串为{@code null}，则返回一个空的{@link ArrayList}
     * 如果提供的字符串为""，则当ignoreEmpty时返回空的{@link ArrayList}，否则返回只有一个""元素的{@link ArrayList}
     *
     * @param <R>         元素类型
     * @param text        被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数，小于等于0表示无限制
     * @param ignoreEmpty 是否忽略空串
     * @param ignoreCase  是否忽略大小写
     * @param mapping     切分后字段映射函数
     * @return 切分后的集合
     */
    public static <R> List<R> split(final CharSequence text, final CharSequence separator, final int limit,
            final boolean ignoreEmpty, final boolean ignoreCase, final Function<String, R> mapping) {
        if (null == text) {
            return ListKit.zero();
        } else if (0 == text.length() && ignoreEmpty) {
            return ListKit.zero();
        }
        Assert.notEmpty(separator, "Separator must be not empty!");

        // 查找分隔符的方式
        final TextFinder finder = separator.length() == 1 ? new CharFinder(separator.charAt(0), ignoreCase)
                : StringFinder.of(separator, ignoreCase);

        final StringSplitter stringSplitter = new StringSplitter(text, finder, limit, ignoreEmpty);
        return stringSplitter.toList(mapping);
    }

    /**
     * 切分路径字符串 如果为空字符串或者null 则返回空集合 空路径会被忽略
     *
     * @param text 被切分的字符串
     * @return 切分后的集合
     */
    public static List<String> splitPath(final CharSequence text) {
        return splitPath(text, Normal.__1);
    }

    /**
     * 切分路径字符串 如果为空字符串或者null 则返回空集合 空路径会被忽略
     *
     * @param text  被切分的字符串
     * @param limit 限制分片数，小于等于0表示无限制
     * @return 切分后的集合
     */
    public static List<String> splitPath(final CharSequence text, final int limit) {
        if (isBlank(text)) {
            return ListKit.zero();
        }

        final StringSplitter stringSplitter = new StringSplitter(text,
                new MatcherFinder((c) -> c == Symbol.C_SLASH || c == Symbol.C_BACKSLASH),
                // 路径中允许空格
                limit, true);
        return stringSplitter.toList(false);
    }

    /**
     * 使用空白符切分字符串 切分后的字符串两边不包含空白符，空串或空白符串并不做为元素之一 如果为空字符串或者null 则返回空集合
     *
     * @param text 被切分的字符串
     * @return 切分后的集合
     */
    public static List<String> splitByBlank(final CharSequence text) {
        return splitByBlank(text, Normal.__1);
    }

    /**
     * 使用空白符切分字符串 切分后的字符串两边不包含空白符，空串或空白符串并不做为元素之一 如果为空字符串或者null 则返回空集合
     *
     * @param text  被切分的字符串
     * @param limit 限制分片数，小于等于0表示无限制
     * @return 切分后的集合
     */
    public static List<String> splitByBlank(final CharSequence text, final int limit) {
        if (isBlank(text)) {
            return ListKit.zero();
        }
        final StringSplitter stringSplitter = new StringSplitter(text, new MatcherFinder(CharKit::isBlankChar), limit,
                true);
        return stringSplitter.toList(false);
    }

    /**
     * 切分字符串为字符串数组
     *
     * @param text  被切分的字符串
     * @param limit 限制分片数，小于等于0表示无限制
     * @return 切分后的集合
     */
    public static String[] splitByBlankToArray(final CharSequence text, final int limit) {
        return splitByBlank(text, limit).toArray(new String[0]);
    }

    /**
     * 通过正则切分字符串，规则如下：
     * <ul>
     * <li>当提供的str为{@code null}时，返回new ArrayList(0)</li>
     * <li>当提供的str为{@code ""}时，返回[""]</li>
     * <li>当提供的separatorRegex为empty(null or "")时，返回[text]，即只有原串一个元素的数组</li>
     * </ul>
     *
     * @param text           字符串
     * @param separatorRegex 分隔符正则
     * @param limit          限制分片数，小于等于0表示无限制
     * @param isTrim         是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty    是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitByRegex(final CharSequence text, final String separatorRegex, final int limit,
            final boolean isTrim, final boolean ignoreEmpty) {
        return splitByRegex(text,
                // 给定字符串或正则为empty，就不再需要解析pattern
                (isEmpty(text) || isEmpty(separatorRegex)) ? null : Pattern.get(separatorRegex), limit, isTrim,
                ignoreEmpty);
    }

    /**
     * 通过正则切分字符串，规则如下：
     * <ul>
     * <li>当提供的str为{@code null}时，返回new ArrayList(0)</li>
     * <li>当提供的str为{@code ""}时，返回[""]</li>
     * <li>当提供的separatorRegex为empty(null or "")时，返回[text]，即只有原串一个元素的数组</li>
     * </ul>
     *
     * @param text             字符串
     * @param separatorPattern 分隔符正则{@link java.util.regex.Pattern}
     * @param limit            限制分片数，小于等于0表示无限制
     * @param isTrim           是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty      是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitByRegex(final CharSequence text, final java.util.regex.Pattern separatorPattern,
            final int limit, final boolean isTrim, final boolean ignoreEmpty) {
        if (null == text) {
            return ListKit.zero();
        }
        if (0 == text.length()) {
            return ignoreEmpty ? ListKit.zero() : ListKit.of(Normal.EMPTY);
        }
        if (null == separatorPattern) {
            final String result = text.toString();
            if (isEmpty(result)) {
                return ignoreEmpty ? ListKit.zero() : ListKit.of(Normal.EMPTY);
            }
            return ListKit.of(result);
        }
        final StringSplitter stringSplitter = new StringSplitter(text, new PatternFinder(separatorPattern), limit,
                ignoreEmpty);
        return stringSplitter.toList(isTrim);
    }

    /**
     * 通过正则切分字符串为字符串数组
     *
     * @param text             被切分的字符串
     * @param separatorPattern 分隔符正则{@link java.util.regex.Pattern}
     * @param limit            限制分片数，小于等于0表示无限制
     * @param isTrim           是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty      是否忽略空串
     * @return 切分后的集合
     */
    public static String[] splitByRegexToArray(final CharSequence text, final java.util.regex.Pattern separatorPattern,
            final int limit, final boolean isTrim, final boolean ignoreEmpty) {
        return splitByRegex(text, separatorPattern, limit, isTrim, ignoreEmpty).toArray(new String[0]);
    }

    /**
     * 根据给定长度，将给定字符串截取为多个部分
     *
     * @param text 字符串
     * @param len  每一个小节的长度，必须大于0
     * @return 截取后的字符串数组
     */
    public static String[] splitByLength(final CharSequence text, final int len) {
        if (isEmpty(text)) {
            return new String[0];
        }
        final StringSplitter stringSplitter = new StringSplitter(text, new LengthFinder(len), -1, false);
        return stringSplitter.toArray(false);
    }

    /**
     * Trim函数
     *
     * @param isTrim 是否trim
     * @return {@link Function}
     */
    public static Function<String, String> trimFunc(final boolean isTrim) {
        return isTrim ? CharsBacker::trim : Function.identity();
    }

    /**
     * 将字符串转换为字符数组
     *
     * @param text        字符串
     * @param isCodePoint 是否为Unicode码点（即支持emoji等多char字符）
     * @return 字符数组
     */
    public static int[] toChars(final CharSequence text, final boolean isCodePoint) {
        if (null == text) {
            return null;
        }
        return (isCodePoint ? text.codePoints() : text.chars()).toArray();
    }

}
