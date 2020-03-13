/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.lang;

/**
 * 编码常量
 *
 * @author Kimi Liu
 * @version 5.6.9
 * @since JDK 1.8+
 */
public class Charset {

    /**
     * 默认字符集信息
     */
    public static final java.nio.charset.Charset DEFAULT = java.nio.charset.Charset.defaultCharset();
    public static final String DEFAULT_CHARSET = DEFAULT.displayName();
    /**
     * ISO拉丁字母第1号，即ISO- Latin -1
     */
    public static final String DEFAULT_ISO_8859_1 = "ISO-8859-1";
    public static final java.nio.charset.Charset ISO_8859_1 = java.nio.charset.Charset.forName(DEFAULT_ISO_8859_1);
    /**
     * 7位ASCII码，即ISO646-US，也就是Unicode字符集的基本拉丁字符块
     */
    public static final String DEFAULT_US_ASCII = "US-ASCII";
    public static final java.nio.charset.Charset US_ASCII = java.nio.charset.Charset.forName(DEFAULT_US_ASCII);
    /**
     * GBK UCS 转换格式
     */
    public static final String DEFAULT_GBK = "GBK";
    public static final java.nio.charset.Charset GBK = java.nio.charset.Charset.forName(DEFAULT_GBK);
    /**
     * 8位UCS转换格式
     */
    public static final String DEFAULT_UTF_8 = "UTF-8";
    public static final java.nio.charset.Charset UTF_8 = java.nio.charset.Charset.forName(DEFAULT_UTF_8);
    /**
     * 16位UCS转换格式，字节顺序由可选的字节顺序标记标识
     */
    public static final String DEFAULT_UTF_16 = "UTF-16";
    public static final java.nio.charset.Charset UTF_16 = java.nio.charset.Charset.forName(DEFAULT_UTF_16);
    /**
     * 16位UCS转换格式，大写字节顺序
     */
    public static final String DEFAULT_UTF_16_BE = "UTF-16BE";
    public static final java.nio.charset.Charset UTF_16_BE = java.nio.charset.Charset.forName(DEFAULT_UTF_16_BE);
    /**
     * 16位UCS转换格式，小写字节顺序
     */
    public static final String DEFAULT_UTF_16_LE = "UTF-16LE";
    public static final java.nio.charset.Charset UTF_16_LE = java.nio.charset.Charset.forName(DEFAULT_UTF_16_LE);
    /**
     * 32位UCS转换格式，大写字节顺序
     */
    public static final String DEFAULT_UTF_32_BE = "UTF-32BE";
    public static final java.nio.charset.Charset UTF_32_BE = java.nio.charset.Charset.forName(DEFAULT_UTF_32_BE);
    /**
     * 32位UCS转换格式，小写字节顺序
     */
    public static final String DEFAULT_UTF_32_LE = "UTF-32LE";
    public static final java.nio.charset.Charset UTF_32_LE = java.nio.charset.Charset.forName(DEFAULT_UTF_32_LE);

}
