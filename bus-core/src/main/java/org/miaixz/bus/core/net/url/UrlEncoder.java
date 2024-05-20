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
package org.miaixz.bus.core.net.url;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.toolkit.CharKit;

/**
 * URL编码器，提供百分号编码实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UrlEncoder {

    private static final java.nio.charset.Charset DEFAULT_CHARSET = Charset.UTF_8;

    /**
     * 编码URL，默认使用UTF-8编码
     * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。。不参与编码的字符：
     * <pre>
     *     unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
     * </pre>
     *
     * @param url URL
     * @return 编码后的URL
     * @throws InternalException UnsupportedEncodingException
     */
    public static String encodeAll(final String url) {
        return encodeAll(url, DEFAULT_CHARSET);
    }

    /**
     * 编码URL
     * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。不参与编码的字符：
     * <pre>
     *     unreserved  = ALPHA / DIGIT / "-" / "." / "_" / "~"
     * </pre>
     *
     * @param url     URL
     * @param charset 编码，为null表示不编码
     * @return 编码后的URL
     * @throws InternalException UnsupportedEncodingException
     */
    public static String encodeAll(final String url, final java.nio.charset.Charset charset) throws InternalException {
        return RFC3986.UNRESERVED.encode(url, charset);
    }

    /**
     * 编码URL，默认使用UTF-8编码
     * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。
     * 此方法用于POST请求中的请求体自动编码，转义大部分特殊字符
     *
     * @param url URL
     * @return 编码后的URL
     */
    public static String encodeQuery(final String url) {
        return encodeQuery(url, DEFAULT_CHARSET);
    }

    /**
     * 编码字符为URL中查询语句
     * 将需要转换的内容（ASCII码形式之外的内容），用十六进制表示法转换出来，并在之前加上%开头。
     * 此方法用于POST请求中的请求体自动编码，转义大部分特殊字符
     *
     * @param url     被编码内容
     * @param charset 编码
     * @return 编码后的字符
     */
    public static String encodeQuery(final String url, final java.nio.charset.Charset charset) {
        return RFC3986.QUERY.encode(url, charset);
    }

    /**
     * 单独编码URL中的空白符，空白符编码为%20
     *
     * @param urlStr URL字符串
     * @return 编码后的字符串
     */
    public static String encodeBlank(final CharSequence urlStr) {
        if (urlStr == null) {
            return null;
        }

        final int len = urlStr.length();
        final StringBuilder sb = new StringBuilder(len);
        char c;
        for (int i = 0; i < len; i++) {
            c = urlStr.charAt(i);
            if (CharKit.isBlankChar(c)) {
                sb.append("%20");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
