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
package org.miaixz.bus.core.text.finder;

import java.util.regex.Matcher;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;

/**
 * 正则查找器 通过传入正则表达式，查找指定字符串中匹配正则的开始和结束位置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatternFinder extends TextFinder {

    private static final long serialVersionUID = -1L;

    private final java.util.regex.Pattern pattern;
    private Matcher matcher;

    /**
     * 构造
     *
     * @param regex           被查找的正则表达式
     * @param caseInsensitive 是否忽略大小写
     */
    public PatternFinder(final String regex, final boolean caseInsensitive) {
        this(Pattern.get(regex, caseInsensitive ? java.util.regex.Pattern.CASE_INSENSITIVE : 0));
    }

    /**
     * 构造
     *
     * @param pattern 被查找的正则{@link java.util.regex.Pattern}
     */
    public PatternFinder(final java.util.regex.Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public TextFinder setText(final CharSequence text) {
        this.matcher = pattern.matcher(text);
        return super.setText(text);
    }

    @Override
    public TextFinder setNegative(final boolean negative) {
        throw new UnsupportedOperationException("Negative is invalid for Pattern!");
    }

    @Override
    public int start(final int from) {
        if (matcher.find(from)) {
            final int end = matcher.end();
            // 只有匹配到的字符串结尾在limit范围内，才算找到
            if (end <= getValidEndIndex()) {
                final int start = matcher.start();
                if (start == end) {
                    // 如果匹配空串，按照未匹配对待，避免死循环
                    return Normal.__1;
                }

                return start;
            }
        }
        return Normal.__1;
    }

    @Override
    public int end(final int start) {
        final int end = matcher.end();
        final int limit;
        if (endIndex < 0) {
            limit = text.length();
        } else {
            limit = Math.min(endIndex, text.length());
        }
        return end <= limit ? end : Normal.__1;
    }

    @Override
    public PatternFinder reset() {
        this.matcher.reset();
        return this;
    }

}
