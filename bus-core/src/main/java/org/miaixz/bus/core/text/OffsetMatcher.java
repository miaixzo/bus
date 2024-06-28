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

import org.miaixz.bus.core.xyz.StringKit;

import java.io.Serializable;
import java.util.function.BiPredicate;

/**
 * 字符串区域匹配器，用于匹配字串是头部匹配还是尾部匹配，亦或者是某个位置的匹配。
 * offset用于锚定开始或结束位置，正数表示从开始偏移，负数表示从后偏移
 * <pre>
 *     a  b  c  d  e  f
 *     |  |        |  |
 *     0  1  c  d -2 -1
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OffsetMatcher implements BiPredicate<CharSequence, CharSequence>, Serializable {

    private static final long serialVersionUID = -1L;

    private final boolean ignoreCase;
    private final boolean ignoreEquals;
    /**
     * 匹配位置，正数表示从开始偏移，负数表示从后偏移
     */
    private final int offset;

    /**
     * 构造
     *
     * @param ignoreCase   是否忽略大小写
     * @param ignoreEquals 是否忽略字符串相等的情况
     * @param isPrefix     {@code true}表示检查开头匹配，{@code false}检查末尾匹配
     */
    public OffsetMatcher(final boolean ignoreCase, final boolean ignoreEquals, final boolean isPrefix) {
        this(ignoreCase, ignoreEquals, isPrefix ? 0 : -1);
    }

    /**
     * 构造
     *
     * @param ignoreCase   是否忽略大小写
     * @param ignoreEquals 是否忽略字符串相等的情况
     * @param offset       匹配位置，正数表示从开始偏移，负数表示从后偏移
     */
    public OffsetMatcher(final boolean ignoreCase, final boolean ignoreEquals, final int offset) {
        this.ignoreCase = ignoreCase;
        this.ignoreEquals = ignoreEquals;
        this.offset = offset;
    }

    @Override
    public boolean test(final CharSequence text, final CharSequence check) {
        if (null == text || null == check) {
            if (ignoreEquals) {
                return false;
            }
            return null == text && null == check;
        }

        final int strToCheckLength = check.length();
        final int toffset = this.offset >= 0 ?
                this.offset : text.length() - strToCheckLength + this.offset + 1;
        final boolean matches = text.toString()
                .regionMatches(ignoreCase, toffset, check.toString(), 0, strToCheckLength);

        if (matches) {
            return (!ignoreEquals) || (!StringKit.equals(text, check, ignoreCase));
        }
        return false;
    }

}
