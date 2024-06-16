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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

import java.util.Arrays;

/**
 * 字符串或字符重复器
 * 用于将给定字符串或字符赋值count次，然后拼接
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringRepeater {

    private final int countOrLength;

    /**
     * 构造
     *
     * @param countOrLength 重复次数或固定长度
     */
    public StringRepeater(final int countOrLength) {
        this.countOrLength = countOrLength;
    }

    /**
     * 创建StrRepeater
     *
     * @param countOrLength 重复次数或固定长度
     * @return StringRepeater
     */
    public static StringRepeater of(final int countOrLength) {
        return new StringRepeater(countOrLength);
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
     * @param c 被重复的字符
     * @return 重复字符字符串
     */
    public String repeat(final char c) {
        final int count = this.countOrLength;
        if (count <= 0) {
            return Normal.EMPTY;
        }

        final char[] result = new char[count];
        Arrays.fill(result, c);
        return new String(result);
    }

    /**
     * 重复某个字符串
     *
     * @param text 被重复的字符
     * @return 重复字符字符串
     */
    public String repeat(final CharSequence text) {
        if (null == text) {
            return null;
        }

        final int count = this.countOrLength;
        if (count <= 0 || text.length() == 0) {
            return Normal.EMPTY;
        }
        if (count == 1) {
            return text.toString();
        }

        // 检查
        final int len = text.length();
        final long longSize = (long) len * (long) count;
        final int size = (int) longSize;
        if (size != longSize) {
            throw new ArrayIndexOutOfBoundsException("Required String length is too large: " + longSize);
        }

        // 相比使用StringBuilder更高效
        final char[] array = new char[size];
        text.toString().getChars(0, len, array, 0);
        int n;
        for (n = len; n < size - n; n <<= 1) {// n <<= 1相当于n *2
            System.arraycopy(array, 0, array, n, n);
        }
        System.arraycopy(array, 0, array, n, size - n);
        return new String(array);
    }

    /**
     * 重复某个字符串到指定长度
     * <ul>
     *     <li>如果指定长度非指定字符串的整数倍，截断到固定长度</li>
     *     <li>如果指定长度小于字符串本身的长度，截断之</li>
     * </ul>
     *
     * @param text 被重复的字符
     * @return 重复字符字符串
     */
    public String repeatByLength(final CharSequence text) {
        if (null == text) {
            return null;
        }

        final int padLen = this.countOrLength;
        if (padLen <= 0) {
            return Normal.EMPTY;
        }
        final int strLen = text.length();
        if (strLen == padLen) {
            return text.toString();
        } else if (strLen > padLen) {
            return StringKit.subPre(text, padLen);
        }

        // 重复，直到达到指定长度
        final char[] padding = new char[padLen];
        for (int i = 0; i < padLen; i++) {
            padding[i] = text.charAt(i % strLen);
        }
        return new String(padding);
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
     * @param delimiter 分界符
     * @return 连接后的字符串
     */
    public String repeatAndJoin(final CharSequence text, final CharSequence delimiter) {
        int count = this.countOrLength;
        if (count <= 0) {
            return Normal.EMPTY;
        }
        if (StringKit.isEmpty(delimiter)) {
            return repeat(text);
        }

        // 初始大小 = 所有重复字符串长度 + 分界符总长度
        final StringBuilder builder = new StringBuilder(
                text.length() * count + delimiter.length() * (count - 1));
        builder.append(text);
        count--;

        while (count-- > 0) {
            builder.append(delimiter).append(text);
        }
        return builder.toString();
    }

}
