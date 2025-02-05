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
package org.miaixz.bus.core.xyz;

/**
 * 提供Unicode字符串和普通字符串之间的转换
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UnicodeKit {

    /**
     * Unicode字符串转为普通字符串 Unicode字符串的表现方式为：\\uXXXX
     *
     * @param unicode Unicode字符串
     * @return 普通字符串
     */
    public static String toString(final String unicode) {
        if (StringKit.isBlank(unicode)) {
            return unicode;
        }

        final int len = unicode.length();
        final StringBuilder sb = new StringBuilder(len);
        int i;
        int pos = 0;
        while ((i = StringKit.indexOfIgnoreCase(unicode, "\\u", pos)) != -1) {
            sb.append(unicode, pos, i);// 写入Unicode符之前的部分
            pos = i;
            if (i + 5 < len) {
                final char c;
                try {
                    c = (char) Integer.parseInt(unicode.substring(i + 2, i + 6), 16);
                    sb.append(c);
                    pos = i + 6;// 跳过整个Unicode符
                } catch (final NumberFormatException e) {
                    // 非法Unicode符，跳过
                    sb.append(unicode, pos, i + 2);// 写入"\\u"
                    pos = i + 2;
                }
            } else {
                // 非Unicode符，结束
                break;
            }
        }

        if (pos < len) {
            sb.append(unicode, pos, len);
        }
        return sb.toString();
    }

    /**
     * 字符编码为Unicode形式
     *
     * @param c 被编码的字符
     * @return Unicode字符串
     * @see HexKit#toUnicodeHex(char)
     */
    public static String toUnicode(final char c) {
        return HexKit.toUnicodeHex(c);
    }

    /**
     * 字符编码为Unicode形式
     *
     * @param c 被编码的字符
     * @return Unicode字符串
     * @see HexKit#toUnicodeHex(int)
     */
    public static String toUnicode(final int c) {
        return HexKit.toUnicodeHex(c);
    }

    /**
     * 字符串编码为Unicode形式
     *
     * @param text 被编码的字符串
     * @return Unicode字符串
     */
    public static String toUnicode(final CharSequence text) {
        return toUnicode(text, true);
    }

    /**
     * 字符串编码为Unicode形式
     *
     * @param text        被编码的字符串
     * @param isSkipAscii 是否跳过ASCII字符（只跳过可见字符）
     * @return Unicode字符串
     */
    public static String toUnicode(final CharSequence text, final boolean isSkipAscii) {
        if (StringKit.isEmpty(text)) {
            return StringKit.toStringOrNull(text);
        }

        final int len = text.length();
        final StringBuilder unicode = new StringBuilder(text.length() * 6);
        char c;
        for (int i = 0; i < len; i++) {
            c = text.charAt(i);
            if (isSkipAscii && CharKit.isAsciiPrintable(c)) {
                unicode.append(c);
            } else {
                unicode.append(HexKit.toUnicodeHex(c));
            }
        }
        return unicode.toString();
    }

}
