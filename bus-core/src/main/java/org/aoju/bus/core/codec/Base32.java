/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.codec;

import org.aoju.bus.core.codec.provider.Base32Provider;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.toolkit.StringKit;

/**
 * Base32 - encodes and decodes RFC3548 Base32 (see http://www.faqs.org/rfcs/rfc3548.html )
 * base32就是用32(2的5次方)个特定ASCII码来表示256个ASCII码
 * 所以,5个ASCII字符经过base32编码后会变为8个字符(公约数为40),长度增加3/5.不足8n用“=”补足
 * see http://blog.csdn.net/earbao/article/details/44453937
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since Java 17+
 */
public class Base32 {

    /**
     * 编码
     *
     * @param bytes 数据
     * @return base32
     */
    public static String encode(final byte[] bytes) {
        return Base32Provider.INSTANCE.encode(bytes);
    }

    /**
     * base32编码
     *
     * @param source 被编码的base32字符串
     * @return 被加密后的字符串
     */
    public static String encode(String source) {
        return encode(source, Charset.UTF_8);
    }

    /**
     * base32编码
     *
     * @param source  被编码的base32字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(String source, java.nio.charset.Charset charset) {
        return encode(StringKit.bytes(source, charset));
    }

    /**
     * 编码
     *
     * @param bytes 数据（Hex模式）
     * @return base32
     */
    public static String encodeHex(final byte[] bytes) {
        return Base32Provider.INSTANCE.encode(bytes, true);
    }

    /**
     * base32编码（Hex模式）
     *
     * @param source 被编码的base32字符串
     * @return 被加密后的字符串
     */
    public static String encodeHex(String source) {
        return encodeHex(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * base32编码（Hex模式）
     *
     * @param source  被编码的base32字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encodeHex(String source, java.nio.charset.Charset charset) {
        return encodeHex(StringKit.bytes(source, charset));
    }

    /**
     * 解码
     *
     * @param base32 base32编码
     * @return 数据
     */
    public static byte[] decode(String base32) {
        return Base32Provider.INSTANCE.decode(base32);
    }

    /**
     * base32解码
     *
     * @param source 被解码的base32字符串
     * @return 被加密后的字符串
     */
    public static String decodeStr(String source) {
        return decodeStr(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * base32解码
     *
     * @param source  被解码的base32字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStr(String source, java.nio.charset.Charset charset) {
        return StringKit.toString(decode(source), charset);
    }

    /**
     * 解码
     *
     * @param base32 base32编码
     * @return 数据
     */
    public static byte[] decodeHex(String base32) {
        return Base32Provider.INSTANCE.decode(base32, true);
    }

    /**
     * base32解码
     *
     * @param source 被解码的base32字符串
     * @return 被加密后的字符串
     */
    public static String decodeStrHex(String source) {
        return decodeStrHex(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * base32解码
     *
     * @param source  被解码的base32字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStrHex(String source, java.nio.charset.Charset charset) {
        return StringKit.toString(decodeHex(source), charset);
    }

}
