/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.io.Serializable;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 字符串头尾指定字符去除器 按照断言，除去字符串头尾部的断言为真的字符，如果字符串是{@code null}，依然返回{@code null}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringTrimer implements UnaryOperator<CharSequence>, Serializable {

    /**
     * 去除两边空白符
     */
    public static final StringTrimer TRIM_BLANK = new StringTrimer(TrimMode.BOTH, CharKit::isBlankChar);
    /**
     * 去除头部空白符
     */
    public static final StringTrimer TRIM_PREFIX_BLANK = new StringTrimer(TrimMode.PREFIX, CharKit::isBlankChar);
    /**
     * 去除尾部空白符
     */
    public static final StringTrimer TRIM_SUFFIX_BLANK = new StringTrimer(TrimMode.SUFFIX, CharKit::isBlankChar);
    private static final long serialVersionUID = -1L;
    private final TrimMode mode;
    private final Predicate<Character> predicate;

    /**
     * 构造
     *
     * @param mode      去除模式，可选去除头部、尾部、两边
     * @param predicate 断言是否过掉字符，返回{@code true}表述过滤掉，{@code false}表示不过滤
     */
    public StringTrimer(final TrimMode mode, final Predicate<Character> predicate) {
        this.mode = mode;
        this.predicate = predicate;
    }

    @Override
    public String apply(final CharSequence text) {
        if (StringKit.isEmpty(text)) {
            return StringKit.toStringOrNull(text);
        }

        final int length = text.length();
        int begin = 0;
        int end = length;// 扫描字符串头部

        if (mode == TrimMode.PREFIX || mode == TrimMode.BOTH) {
            // 扫描字符串头部
            while ((begin < end) && (predicate.test(text.charAt(begin)))) {
                begin++;
            }
        }
        if (mode == TrimMode.SUFFIX || mode == TrimMode.BOTH) {
            // 扫描字符串尾部
            while ((begin < end) && (predicate.test(text.charAt(end - 1)))) {
                end--;
            }
        }

        final String result;
        if ((begin > 0) || (end < length)) {
            result = text.toString().substring(begin, end);
        } else {
            result = text.toString();
        }

        return result;
    }

    /**
     * 去除模式
     */
    public enum TrimMode {
        /**
         * 字符串头部
         */
        PREFIX,
        /**
         * 字符串尾部
         */
        SUFFIX,
        /**
         * 字符串两边
         */
        BOTH
    }

}
