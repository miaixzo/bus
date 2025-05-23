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

import java.io.Serial;
import java.io.Serializable;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 字符串裁剪器，用于裁剪字符串前后缀 强调去除两边或某一边的指定字符串，如果一边不存在，另一边不影响去除
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringStripper implements UnaryOperator<CharSequence>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852319273650L;

    private final CharSequence prefix;
    private final CharSequence suffix;
    private final boolean ignoreCase;
    private final boolean stripAll;

    /**
     * 构造
     *
     * @param prefix     前缀，{@code null}忽略
     * @param suffix     后缀，{@code null}忽略
     * @param ignoreCase 是否忽略大小写
     * @param stripAll   是否去除全部
     */
    public StringStripper(final CharSequence prefix, final CharSequence suffix, final boolean ignoreCase,
            final boolean stripAll) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.ignoreCase = ignoreCase;
        this.stripAll = stripAll;
    }

    @Override
    public String apply(final CharSequence charSequence) {
        return this.stripAll ? stripAll(charSequence) : stripOnce(charSequence);
    }

    /**
     * 去除两边的指定字符串 两边字符如果存在，则去除，不存在不做处理
     * 
     * <pre>{@code
     * "aaa_STRIPPED_bbb", "a", "b"  -> "aa_STRIPPED_bb"
     * "aaa_STRIPPED_bbb", null, null  -> "aaa_STRIPPED_bbb"
     * "aaa_STRIPPED_bbb", "", ""  -> "aaa_STRIPPED_bbb"
     * "aaa_STRIPPED_bbb", "", "b"  -> "aaa_STRIPPED_bb"
     * "aaa_STRIPPED_bbb", null, "b"  -> "aaa_STRIPPED_bb"
     * "aaa_STRIPPED_bbb", "a", ""  -> "aa_STRIPPED_bbb"
     * "aaa_STRIPPED_bbb", "a", null  -> "aa_STRIPPED_bbb"
     *
     * "a", "a", "a"  -> ""
     * }</pre>
     *
     * @param charSequence 被处理的字符串
     * @return 处理后的字符串
     */
    private String stripOnce(final CharSequence charSequence) {
        if (StringKit.isEmpty(charSequence)) {
            return StringKit.toStringOrNull(charSequence);
        }

        final String str = charSequence.toString();
        int from = 0;
        int to = str.length();

        if (StringKit.isNotEmpty(this.prefix) && startWith(str, this.prefix, 0)) {
            from = this.prefix.length();
            if (from == to) {
                // "a", "a", "a" -> ""
                return Normal.EMPTY;
            }
        }
        if (endWithSuffix(str)) {
            to -= this.suffix.length();
            if (from == to) {
                // "a", "a", "a" -> ""
                return Normal.EMPTY;
            } else if (to < from) {
                // pre去除后和suffix有重叠，如 ("aba", "ab", "ba") -> "a"
                to += this.suffix.length();
            }
        }

        return str.substring(from, to);
    }

    /**
     * 去除两边所有的指定字符串
     *
     * <pre>{@code
     * "aaa_STRIPPED_bbb", "a", "b"  -> "_STRIPPED_"
     * "aaa_STRIPPED_bbb", null, null  -> "aaa_STRIPPED_bbb"
     * "aaa_STRIPPED_bbb", "", ""  -> "aaa_STRIPPED_bbb"
     * "aaa_STRIPPED_bbb", "", "b"  -> "aaa_STRIPPED_"
     * "aaa_STRIPPED_bbb", null, "b"  -> "aaa_STRIPPED_"
     * "aaa_STRIPPED_bbb", "a", ""  -> "_STRIPPED_bbb"
     * "aaa_STRIPPED_bbb", "a", null  -> "_STRIPPED_bbb"
     *
     * // special test
     * "aaaaaabbb", "aaa", null  -> "bbb"
     * "aaaaaaabbb", "aa", null  -> "abbb"
     *
     * "aaaaaaaaa", "aaa", "aa"  -> ""
     * "a", "a", "a"  -> ""
     * }</pre>
     *
     * @param charSequence 被处理的字符串
     * @return 处理后的字符串
     */
    private String stripAll(final CharSequence charSequence) {
        if (StringKit.isEmpty(charSequence)) {
            return StringKit.toStringOrNull(charSequence);
        }

        final String str = charSequence.toString();
        int from = 0;
        int to = str.length();

        if (StringKit.isNotEmpty(this.prefix)) {
            while (startWith(str, this.prefix, from)) {
                from += this.prefix.length();
                if (from == to) {
                    // "a", "a", "a" -> ""
                    return Normal.EMPTY;
                }
            }
        }
        if (StringKit.isNotEmpty(suffix)) {
            final int suffixLength = this.suffix.length();
            while (startWith(str, suffix, to - suffixLength)) {
                to -= suffixLength;
                if (from == to) {
                    // "a", "a", "a" -> ""
                    return Normal.EMPTY;
                } else if (to < from) {
                    // pre去除后和suffix有重叠，如 ("aba", "ab", "ba") -> "a"
                    to += suffixLength;
                    break;
                }
            }
        }

        return str.substring(from, to);
    }

    /**
     * 判断是否以指定前缀开头
     *
     * @param charSequence 被检查的字符串
     * @param from         开始位置
     * @return 是否以指定前缀开头
     */
    private boolean startWith(final CharSequence charSequence, final CharSequence strToCheck, final int from) {
        return new OffsetMatcher(this.ignoreCase, false, from).test(charSequence, strToCheck);
    }

    /**
     * 判断是否以指定后缀结尾
     *
     * @param charSequence 被检查的字符串
     * @return 是否以指定后缀结尾
     */
    private boolean endWithSuffix(final CharSequence charSequence) {
        return StringKit.isNotEmpty(suffix) && StringKit.endWith(charSequence, suffix, ignoreCase);
    }

}
