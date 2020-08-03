/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.core.codec;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;

/**
 * Base32 - encodes and decodes RFC3548 Base32 (see http://www.faqs.org/rfcs/rfc3548.html )
 * base32就是用32(2的5次方)个特定ASCII码来表示256个ASCII码
 * 所以,5个ASCII字符经过base32编码后会变为8个字符(公约数为40),长度增加3/5.不足8n用“=”补足
 * see http://blog.csdn.net/earbao/article/details/44453937
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8+
 */
public final class Base32 {

    /**
     * 编码
     *
     * @param bytes 数据
     * @return base32
     */
    public static String encode(final byte[] bytes) {
        int i = 0;
        int index = 0;
        int digit;
        int currByte;
        int nextByte;
        StringBuilder base32 = new StringBuilder((bytes.length + 7) * 8 / 5);

        while (i < bytes.length) {
            currByte = (bytes[i] >= 0) ? bytes[i] : (bytes[i] + 256);

            if (index > 3) {
                if ((i + 1) < bytes.length) {
                    nextByte = (bytes[i + 1] >= 0) ? bytes[i + 1] : (bytes[i + 1] + 256);
                } else {
                    nextByte = 0;
                }

                digit = currByte & (0xFF >> index);
                index = (index + 5) % 8;
                digit <<= index;
                digit |= nextByte >> (8 - index);
                i++;
            } else {
                digit = (currByte >> (8 - (index + 5))) & 0x1F;
                index = (index + 5) % 8;
                if (index == 0) {
                    i++;
                }
            }
            base32.append(Normal.ENCODE_32_TABLE[digit]);
        }

        return base32.toString();
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
    public static String encode(String source, String charset) {
        return encode(StringKit.bytes(source, charset));
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
     * 解码
     *
     * @param base32 base32编码
     * @return 数据
     */
    public static byte[] decode(final String base32) {
        int i, index, lookup, offset, digit;
        byte[] bytes = new byte[base32.length() * 5 / 8];

        for (i = 0, index = 0, offset = 0; i < base32.length(); i++) {
            lookup = base32.charAt(i) - Symbol.C_ZERO;

            /* Skip chars outside the lookup table */
            if (lookup < 0 || lookup >= Normal.DECODE_32_TABLE.length) {
                continue;
            }

            digit = Normal.DECODE_32_TABLE[lookup];

            /* If this digit is not in the table, ignore it */
            if (digit == 0xFF) {
                continue;
            }

            if (index <= 3) {
                index = (index + 5) % 8;
                if (index == 0) {
                    bytes[offset] |= digit;
                    offset++;
                    if (offset >= bytes.length) {
                        break;
                    }
                } else {
                    bytes[offset] |= digit << (8 - index);
                }
            } else {
                index = (index + 5) % 8;
                bytes[offset] |= (digit >>> index);
                offset++;

                if (offset >= bytes.length) {
                    break;
                }
                bytes[offset] |= digit << (8 - index);
            }
        }
        return bytes;
    }

    /**
     * base32解码
     *
     * @param source 被解码的base32字符串
     * @return 被加密后的字符串
     */
    public static String decodeStr(String source) {
        return decodeStr(source, Charset.UTF_8);
    }

    /**
     * base32解码
     *
     * @param source  被解码的base32字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStr(String source, String charset) {
        return StringKit.toString(decode(source), charset);
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

}
