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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * 字符串查找器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringFinder extends TextFinder {

    @Serial
    private static final long serialVersionUID = 2852237656798L;

    private final CharSequence strToFind;
    private final boolean caseInsensitive;

    /**
     * 构造
     *
     * @param strToFind       查找的字符串
     * @param caseInsensitive 是否忽略大小写
     */
    public StringFinder(final CharSequence strToFind, final boolean caseInsensitive) {
        Assert.notEmpty(strToFind);
        this.strToFind = strToFind;
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * 创建查找器，构造后须调用{@link #setText(CharSequence)} 设置被查找的文本
     *
     * @param strToFind       查找的字符串
     * @param caseInsensitive 是否忽略大小写
     * @return {@code StringFinder}
     */
    public static StringFinder of(final CharSequence strToFind, final boolean caseInsensitive) {
        return new StringFinder(strToFind, caseInsensitive);
    }

    @Override
    public int start(int from) {
        Assert.notNull(this.text, "Text to find must be not null!");
        final int subLen = strToFind.length();

        if (from < 0) {
            from = 0;
        }
        int endLimit = getValidEndIndex();
        if (negative) {
            for (int i = from; i > endLimit; i--) {
                if (CharsBacker.isSubEquals(text, i, strToFind, 0, subLen, caseInsensitive)) {
                    return i;
                }
            }
        } else {
            endLimit = endLimit - subLen + 1;
            for (int i = from; i < endLimit; i++) {
                if (CharsBacker.isSubEquals(text, i, strToFind, 0, subLen, caseInsensitive)) {
                    return i;
                }
            }
        }

        return Normal.__1;
    }

    @Override
    public int end(final int start) {
        if (start < 0) {
            return -1;
        }
        return start + strToFind.length();
    }

}
