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
import org.miaixz.bus.core.text.finder.*;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.CharsKit;
import org.miaixz.bus.core.toolkit.PatternKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 字符串切分器，封装统一的字符串分割静态方法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TextSplitter {

    /**
     * 使用空白符切分字符串
     * 切分后的字符串两边不包含空白符，空串或空白符串并不做为元素之一
     * 如果为空字符串或者null 则返回空集合
     *
     * @param text  被切分的字符串
     * @param limit 限制分片数
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence text, int limit) {
        if (null == text) {
            return new ArrayList<>(0);
        }
        final SplitIterator SplitIterator = new SplitIterator(text, new MatcherFinder(CharsKit::isBlankChar), limit, true);
        return SplitIterator.toList(false);
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
    public static List<String> split(CharSequence text, char separator, boolean isTrim, boolean ignoreEmpty) {
        return split(text, separator, 0, isTrim, ignoreEmpty);
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
    public static List<String> split(CharSequence text, String separator, boolean isTrim, boolean ignoreEmpty) {
        return split(text, separator, -1, isTrim, ignoreEmpty, false);
    }

    /**
     * 切分字符串，大小写敏感
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence text, char separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return split(text, separator, limit, isTrim, ignoreEmpty, false);
    }

    /**
     * 通过正则切分字符串
     * 如果为空字符串或者null 则返回空集合
     *
     * @param text        字符串
     * @param separator   分隔符正则{@link Pattern}
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(String text, Pattern separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        if (null == text) {
            return new ArrayList<>(0);
        }
        final SplitIterator SplitIterator = new SplitIterator(text, new PatternFinder(separator), limit, ignoreEmpty);
        return SplitIterator.toList(isTrim);
    }

    /**
     * 切分字符串，不忽略大小写
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence text, String separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return split(text, separator, limit, isTrim, ignoreEmpty, false);
    }

    /**
     * 切分字符串，大小写敏感
     *
     * @param <R>         切分后的元素类型
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，-1不限制
     * @param ignoreEmpty 是否忽略空串
     * @param mapping     切分后的字符串元素的转换方法
     * @return 切分后的集合，元素类型是经过 mapping 转换后的
     */
    public static <R> List<R> split(CharSequence text, char separator, int limit, boolean ignoreEmpty, Function<String, R> mapping) {
        return split(text, separator, limit, ignoreEmpty, false, mapping);
    }

    /**
     * 切分字符串
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @param ignoreCase  是否忽略大小写
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence text, char separator, int limit, boolean isTrim, boolean ignoreEmpty, boolean ignoreCase) {
        return split(text, separator, limit, ignoreEmpty, ignoreCase, trimFunc(isTrim));
    }

    /**
     * 切分字符串
     * 如果为空字符串或者null 则返回空集合
     *
     * @param <R>         切分后的元素类型
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，-1不限制
     * @param ignoreEmpty 是否忽略空串
     * @param ignoreCase  是否忽略大小写
     * @param mapping     切分后的字符串元素的转换方法
     * @return 切分后的集合，元素类型是经过 mapping 转换后的
     */
    public static <R> List<R> split(CharSequence text, char separator, int limit, boolean ignoreEmpty, boolean ignoreCase, Function<String, R> mapping) {
        if (null == text) {
            return new ArrayList<>(0);
        }
        final SplitIterator SplitIterator = new SplitIterator(text, new CharFinder(separator, ignoreCase), limit, ignoreEmpty);
        return SplitIterator.toList(mapping);
    }

    /**
     * 切分字符串
     * 如果为空字符串或者null 则返回空集合
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数，小于等于0表示无限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @param ignoreCase  是否忽略大小写
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence text, String separator, int limit, boolean isTrim, boolean ignoreEmpty, boolean ignoreCase) {
        if (null == text) {
            return new ArrayList<>(0);
        }
        final SplitIterator SplitIterator = new SplitIterator(text, new StringFinder(separator, ignoreCase), limit, ignoreEmpty);
        return SplitIterator.toList(isTrim);
    }

    /**
     * 切分字符串，去除每个元素两边空格，忽略大小写
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符串
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitTrim(CharSequence text, String separator, boolean ignoreEmpty) {
        return split(text, separator, true, ignoreEmpty);
    }

    /**
     * 切分字符串
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitTrim(CharSequence text, char separator, boolean ignoreEmpty) {
        return split(text, separator, 0, true, ignoreEmpty);
    }

    /**
     * 切分字符串，去除每个元素两边空格，忽略大小写
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitTrim(CharSequence text, String separator, int limit, boolean ignoreEmpty) {
        return split(text, separator, limit, true, ignoreEmpty);
    }

    /**
     * 切分字符串，大小写敏感，去除每个元素两边空白符
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，-1不限制
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitTrim(CharSequence text, char separator, int limit, boolean ignoreEmpty) {
        return split(text, separator, limit, true, ignoreEmpty, false);
    }

    /**
     * 切分字符串为字符串数组
     *
     * @param text      被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     */
    public static String[] splitToArray(String text, String separator) {
        return splitToArray(text, separator, true, true);
    }

    /**
     * 切分字符串为字符串数组
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符， 每个字符都被单独视为分隔符
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static String[] splitToArray(String text, String separator, boolean isTrim, boolean ignoreEmpty) {
        if (text == null) {
            return Normal.EMPTY_STRING_ARRAY;
        } else {
            StringTokenizer st = new StringTokenizer(text, separator);
            List tokens = new ArrayList();

            while (true) {
                String token;
                do {
                    if (!st.hasMoreTokens()) {
                        return ArrayKit.toArray(tokens);
                    }

                    token = st.nextToken();
                    if (isTrim) {
                        token = token.trim();
                    }
                } while (ignoreEmpty && token.length() <= 0);

                tokens.add(token);
            }
        }
    }

    /**
     * 切分字符串为字符串数组
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，小于等于0表示无限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static String[] splitToArray(CharSequence text, String separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return ArrayKit.toArray(split(text, separator, limit, isTrim, ignoreEmpty));
    }

    /**
     * 切分字符串为字符串数组
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static String[] splitToArray(CharSequence text, char separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return ArrayKit.toArray(split(text, separator, limit, isTrim, ignoreEmpty));
    }

    /**
     * 切分字符串为字符串数组
     *
     * @param text  被切分的字符串
     * @param limit 限制分片数
     * @return 切分后的集合
     */
    public static String[] splitToArray(String text, int limit) {
        return ArrayKit.toArray(split(text, limit));
    }

    /**
     * 通过正则切分字符串为字符串数组
     *
     * @param text             被切分的字符串
     * @param separatorPattern 分隔符正则{@link Pattern}
     * @param limit            限制分片数
     * @param isTrim           是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty      是否忽略空串
     * @return 切分后的集合
     */
    public static String[] splitToArray(String text, Pattern separatorPattern, int limit, boolean isTrim, boolean ignoreEmpty) {
        return ArrayKit.toArray(split(text, separatorPattern, limit, isTrim, ignoreEmpty));
    }

    /**
     * 切分字符串，忽略大小写
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitIgnoreCase(CharSequence text, char separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return split(text, separator, limit, isTrim, ignoreEmpty, true);
    }

    /**
     * 切分字符串，忽略大小写
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitIgnoreCase(CharSequence text, String separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return split(text, separator, limit, isTrim, ignoreEmpty, true);
    }

    /**
     * 切分字符串，去除每个元素两边空格，忽略大小写
     *
     * @param text        被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitTrimIgnoreCase(CharSequence text, String separator, int limit, boolean ignoreEmpty) {
        return split(text, separator, limit, true, ignoreEmpty, true);
    }

    /**
     * 切分字符串路径，仅支持Unix分界符：/
     *
     * @param text 被切分的字符串
     * @return 切分后的集合
     */
    public static List<String> splitPath(CharSequence text) {
        return splitPath(text, 0);
    }

    /**
     * 切分字符串路径，仅支持Unix分界符：/
     *
     * @param text  被切分的字符串
     * @param limit 限制分片数
     * @return 切分后的集合
     */
    public static List<String> splitPath(CharSequence text, int limit) {
        return split(text, Symbol.C_SLASH, limit, true, true);
    }

    /**
     * 切分字符串路径，仅支持Unix分界符：/
     *
     * @param text 被切分的字符串
     * @return 切分后的集合
     */
    public static String[] splitPathToArray(CharSequence text) {
        return ArrayKit.toArray(splitPath(text));
    }

    /**
     * 切分字符串路径，仅支持Unix分界符：/
     *
     * @param text  被切分的字符串
     * @param limit 限制分片数
     * @return 切分后的集合
     */
    public static String[] splitPathToArray(CharSequence text, int limit) {
        return ArrayKit.toArray(splitPath(text, limit));
    }

    /**
     * 根据给定长度，将给定字符串截取为多个部分
     *
     * @param text 字符串
     * @param len  每一个小节的长度
     * @return 截取后的字符串数组
     */
    public static String[] splitByLength(String text, int len) {
        if (null == text) {
            return new String[0];
        }
        SplitIterator SplitIterator = new SplitIterator(text, new LengthFinder(len), -1, false);
        return SplitIterator.toArray(false);
    }

    /**
     * 通过正则切分字符串
     *
     * @param text           字符串
     * @param separatorRegex 分隔符正则
     * @param limit          限制分片数
     * @param isTrim         是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty    是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitByRegex(String text, String separatorRegex, int limit, boolean isTrim, boolean ignoreEmpty) {
        final Pattern pattern = PatternKit.get(separatorRegex);
        return split(text, pattern, limit, isTrim, ignoreEmpty);
    }

    /**
     * Trim函数
     *
     * @param isTrim 是否trim
     * @return {@link Function}
     */
    public static Function<String, String> trimFunc(boolean isTrim) {
        return (text) -> isTrim ? StringKit.trim(text) : text;
    }

}
