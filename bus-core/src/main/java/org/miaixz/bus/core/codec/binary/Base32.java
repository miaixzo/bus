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
package org.miaixz.bus.core.codec.binary;

import org.miaixz.bus.core.codec.binary.provider.Base32Provider;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.toolkit.ByteKit;
import org.miaixz.bus.core.toolkit.StringKit;

/**
 * Base32 - encodes and decodes RFC4648 Base32
 * (see <a href="https://datatracker.ietf.org/doc/html/rfc4648#section-6">https://datatracker.ietf.org/doc/html/rfc4648#section-6</a> )
 * base32就是用32（2的5次方）个特定ASCII码来表示256个ASCII码。
 * 所以，5个ASCII字符经过base32编码后会变为8个字符（公约数为40），长度增加3/5.不足8n用“=”补足。
 * 根据RFC4648 Base32规范，支持两种模式：
 * <ul>
 *     <li>Base 32 Alphabet                 (ABCDEFGHIJKLMNOPQRSTUVWXYZ234567)</li>
 *     <li>"Extended Hex" Base 32 Alphabet  (0123456789ABCDEFGHIJKLMNOPQRSTUV)</li>
 * </ul>
 *
 * @author Kimi Liu
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
    public static String encode(final String source) {
        return encode(source, Charset.UTF_8);
    }

    /**
     * base32编码
     *
     * @param source  被编码的base32字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(final String source, final java.nio.charset.Charset charset) {
        return encode(ByteKit.toBytes(source, charset));
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
    public static String encodeHex(final String source) {
        return encodeHex(source, Charset.UTF_8);
    }

    /**
     * base32编码（Hex模式）
     *
     * @param source  被编码的base32字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encodeHex(final String source, final java.nio.charset.Charset charset) {
        return encodeHex(ByteKit.toBytes(source, charset));
    }

    /**
     * 解码
     *
     * @param base32 base32编码
     * @return 数据
     */
    public static byte[] decode(final String base32) {
        return Base32Provider.INSTANCE.decode(base32);
    }

    /**
     * base32解码
     *
     * @param source 被解码的base32字符串
     * @return 被加密后的字符串
     */
    public static String decodeString(final String source) {
        return decodeString(source, Charset.UTF_8);
    }

    /**
     * base32解码
     *
     * @param source  被解码的base32字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeString(final String source, final java.nio.charset.Charset charset) {
        return StringKit.toString(decode(source), charset);
    }

    /**
     * 解码
     *
     * @param base32 base32编码
     * @return 数据
     */
    public static byte[] decodeHex(final String base32) {
        return Base32Provider.INSTANCE.decode(base32, true);
    }

    /**
     * base32解码
     *
     * @param source 被解码的base32字符串
     * @return 被加密后的字符串
     */
    public static String decodeStrHex(final String source) {
        return decodeStrHex(source, Charset.UTF_8);
    }

    /**
     * base32解码
     *
     * @param source  被解码的base32字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStrHex(final String source, final java.nio.charset.Charset charset) {
        return StringKit.toString(decodeHex(source), charset);
    }

}
