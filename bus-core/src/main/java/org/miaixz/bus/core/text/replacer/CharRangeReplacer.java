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
package org.miaixz.bus.core.text.replacer;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * 区间字符串替换，指定区间，将区间中的所有字符去除，替换为指定的字符，字符重复次数为区间长度，即替换后字符串长度不变
 * 此方法使用{@link String#codePoints()}完成拆分替换
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharRangeReplacer extends StringReplacer {

    private static final long serialVersionUID = -1L;

    private final int beginInclude;
    private final int endExclude;
    private final char replacedChar;
    private final boolean isCodePoint;

    /**
     * 构造
     *
     * @param beginInclude 开始位置（包含）
     * @param endExclude   结束位置（不包含）
     * @param replacedChar 被替换的字符串
     * @param isCodePoint  是否code point模式，此模式下emoji等会被作为单独的字符
     */
    public CharRangeReplacer(final int beginInclude, final int endExclude, final char replacedChar, final boolean isCodePoint) {
        this.beginInclude = beginInclude;
        this.endExclude = endExclude;
        this.replacedChar = replacedChar;
        this.isCodePoint = isCodePoint;
    }

    @Override
    public String apply(final CharSequence text) {
        if (StringKit.isEmpty(text)) {
            return StringKit.toString(text);
        }

        final String originalStr = StringKit.toString(text);
        final int[] chars = (isCodePoint ? originalStr.codePoints() : originalStr.chars()).toArray();
        final int strLength = chars.length;

        final int beginInclude = this.beginInclude;
        if (beginInclude > strLength) {
            return originalStr;
        }
        int endExclude = this.endExclude;
        if (endExclude > strLength) {
            endExclude = strLength;
        }
        if (beginInclude > endExclude) {
            // 如果起始位置大于结束位置，不替换
            return originalStr;
        }

        // 新字符串长度不变
        final StringBuilder stringBuilder = new StringBuilder(originalStr.length());
        for (int i = 0; i < strLength; i++) {
            if (i >= beginInclude && i < endExclude) {
                // 区间内的字符全部替换
                replace(originalStr, i, stringBuilder);
            } else {
                // 其它字符保留
                append(stringBuilder, chars[i]);
            }
        }
        return stringBuilder.toString();
    }

    @Override
    protected int replace(final CharSequence text, final int pos, final StringBuilder out) {
        out.appendCodePoint(replacedChar);
        return pos;
    }

    /**
     * 追加字符
     *
     * @param stringBuilder {@link StringBuilder}
     * @param c             字符
     */
    private void append(final StringBuilder stringBuilder, final int c) {
        if (isCodePoint) {
            stringBuilder.appendCodePoint(c);
        } else {
            stringBuilder.append((char) c);
        }
    }

}
