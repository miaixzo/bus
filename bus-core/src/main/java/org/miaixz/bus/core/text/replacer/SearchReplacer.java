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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.StringKit;

/**
 * 查找替换器
 * 查找给定的字符串，并全部替换为新的字符串，其它字符不变
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SearchReplacer extends StringReplacer {

    private static final long serialVersionUID = -1L;

    private final int fromIndex;
    private final CharSequence searchText;
    private final int searchTextLength;
    private final CharSequence replacement;
    private final boolean ignoreCase;

    /**
     * 构造
     *
     * @param fromIndex   开始位置（包括）
     * @param searchText  被查找的字符串
     * @param replacement 被替换的字符串
     * @param ignoreCase  是否忽略大小写
     */
    public SearchReplacer(final int fromIndex, final CharSequence searchText, final CharSequence replacement, final boolean ignoreCase) {
        this.fromIndex = Math.max(fromIndex, 0);
        this.searchText = Assert.notEmpty(searchText, "'searchStr' must be not empty!");
        this.searchTextLength = searchText.length();
        this.replacement = StringKit.emptyIfNull(replacement);
        this.ignoreCase = ignoreCase;
    }

    @Override
    public String apply(final CharSequence text) {
        if (StringKit.isEmpty(text)) {
            return StringKit.toString(text);
        }

        final int strLength = text.length();
        if (strLength < this.searchTextLength) {
            return StringKit.toString(text);
        }

        final int fromIndex = this.fromIndex;
        if (fromIndex > strLength) {
            // 越界截断
            return Normal.EMPTY;
        }

        final StringBuilder result = new StringBuilder(
                strLength - this.searchTextLength + this.replacement.length());
        if (0 != fromIndex) {
            // 开始部分
            result.append(text.subSequence(0, fromIndex));
        }

        // 替换部分
        int pos = fromIndex;
        int consumed;//处理过的字符数
        while ((consumed = replace(text, pos, result)) > 0) {
            pos += consumed;
        }

        if (pos < strLength) {
            // 结尾部分
            result.append(text.subSequence(pos, strLength));
        }
        return result.toString();
    }

    @Override
    protected int replace(final CharSequence text, final int pos, final StringBuilder out) {
        final int index = StringKit.indexOf(text, this.searchText, pos, this.ignoreCase);
        if (index > Normal.__1) {
            // 无需替换的部分
            out.append(text.subSequence(pos, index));
            // 替换的部分
            out.append(replacement);

            //已经处理的长度 = 无需替换的长度（查找字符串位置 - 开始的位置） + 替换的长度
            return index - pos + searchTextLength;
        }

        // 未找到
        return Normal.__1;
    }

}
