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

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.lang.Assert;

/**
 * 文本查找抽象类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class TextFinder implements Finder, Serializable {

    @Serial
    private static final long serialVersionUID = 2852275865568L;

    /**
     * 文本
     */
    protected CharSequence text;
    /**
     * 结束位置
     */
    protected int endIndex = -1;
    /**
     * 是否反向查找
     */
    protected boolean negative;

    /**
     * 设置被查找的文本
     *
     * @param text 文本
     * @return this
     */
    public TextFinder setText(final CharSequence text) {
        this.text = Assert.notNull(text, "Text must be not null!");
        return this;
    }

    /**
     * 设置查找的结束位置 如果从前向后查找，结束位置最大为text.length() 如果从后向前，结束位置为-1
     *
     * @param endIndex 结束位置（不包括）
     * @return this
     */
    public TextFinder setEndIndex(final int endIndex) {
        this.endIndex = endIndex;
        return this;
    }

    /**
     * 设置是否反向查找，{@code true}表示从后向前查找
     *
     * @param negative 结束位置（不包括）
     * @return this
     */
    public TextFinder setNegative(final boolean negative) {
        this.negative = negative;
        return this;
    }

    /**
     * 获取有效结束位置 如果{@link #endIndex}小于0，在反向模式下是开头（-1），正向模式是结尾（text.length()）
     *
     * @return 有效结束位置
     */
    protected int getValidEndIndex() {
        if (negative && -1 == endIndex) {
            // 反向查找模式下，-1表示0前面的位置，即字符串反向末尾的位置
            return -1;
        }
        final int limit;
        if (endIndex < 0) {
            limit = endIndex + text.length() + 1;
        } else {
            limit = Math.min(endIndex, text.length());
        }
        return limit;
    }

}
