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

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.CharUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.nio.charset.Charset;

/**
 * Base64编码
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
public class Base64Encoder {

    /**
     * 编码为Base64，非URL安全的
     *
     * @param arr     被编码的数组
     * @param lineSep 在76个char之后是CRLF还是EOF
     * @return 编码后的bytes
     */
    public static byte[] encode(byte[] arr, boolean lineSep) {
        return encode(arr, lineSep, false);
    }

    /**
     * 编码为Base64,URL安全的
     *
     * @param arr     被编码的数组
     * @param lineSep 在76个char之后是CRLF还是EOF
     * @return 编码后的bytes
     */
    public static byte[] encodeUrlSafe(byte[] arr, boolean lineSep) {
        return encode(arr, lineSep, true);
    }

    /**
     * base64编码
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encode(CharSequence source) {
        return encode(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * base64编码，URL安全
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(CharSequence source) {
        return encodeUrlSafe(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * base64编码
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(CharSequence source, Charset charset) {
        return encode(StringUtils.bytes(source, charset));
    }

    /**
     * base64编码，URL安全的
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(CharSequence source, Charset charset) {
        return encodeUrlSafe(StringUtils.bytes(source, charset));
    }

    /**
     * base64编码
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encode(byte[] source) {
        return StringUtils.toString(encode(source, false), org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * base64编码,URL安全的
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(byte[] source) {
        return StringUtils.toString(encodeUrlSafe(source, false), org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 编码为Base64
     * 如果isMultiLine为<code>true</code>,则每76个字符一个换行符,否则在一行显示
     *
     * @param arr         被编码的数组
     * @param isMultiLine 在76个char之后是CRLF还是EOF
     * @param isUrlSafe   是否使用URL安全字符,一般为<code>false</code>
     * @return 编码后的bytes
     */
    public static byte[] encode(byte[] arr, boolean isMultiLine, boolean isUrlSafe) {
        if (null == arr) {
            return null;
        }

        int len = arr.length;
        if (len == 0) {
            return Normal.EMPTY_BYTE_ARRAY;
        }

        int evenlen = (len / 3) * 3;
        int cnt = ((len - 1) / 3 + 1) << 2;
        int destlen = cnt + (isMultiLine ? (cnt - 1) / 76 << 1 : 0);
        byte[] dest = new byte[destlen];

        byte[] encodeTable = isUrlSafe ? Normal.ENCODE_URL_TABLE : Normal.ENCODE_64_TABLE;

        for (int s = 0, d = 0, cc = 0; s < evenlen; ) {
            int i = (arr[s++] & 0xff) << 16 | (arr[s++] & 0xff) << 8 | (arr[s++] & 0xff);

            dest[d++] = encodeTable[(i >>> 18) & 0x3f];
            dest[d++] = encodeTable[(i >>> 12) & 0x3f];
            dest[d++] = encodeTable[(i >>> 6) & 0x3f];
            dest[d++] = encodeTable[i & 0x3f];

            if (isMultiLine && ++cc == 19 && d < destlen - 2) {
                dest[d++] = Symbol.C_CR;
                dest[d++] = Symbol.C_LF;
                cc = 0;
            }
        }

        int left = len - evenlen;// 剩余位数
        if (left > 0) {
            int i = ((arr[evenlen] & 0xff) << 10) | (left == 2 ? ((arr[len - 1] & 0xff) << 2) : 0);

            dest[destlen - 4] = encodeTable[i >> 12];
            dest[destlen - 3] = encodeTable[(i >>> 6) & 0x3f];

            if (isUrlSafe) {
                // 在URL Safe模式下,=为URL中的关键字符,不需要补充 空余的byte位要去掉
                int urlSafeLen = destlen - 2;
                if (2 == left) {
                    dest[destlen - 2] = encodeTable[i & 0x3f];
                    urlSafeLen += 1;
                }
                byte[] urlSafeDest = new byte[urlSafeLen];
                System.arraycopy(dest, 0, urlSafeDest, 0, urlSafeLen);
                return urlSafeDest;
            } else {
                dest[destlen - 2] = (left == 2) ? encodeTable[i & 0x3f] : (byte) Symbol.C_EQUAL;
                dest[destlen - 1] = Symbol.C_EQUAL;
            }
        }
        return dest;
    }

    /**
     * 编码为Base64
     *
     * @param src     源字符信息
     * @param srcPos  开始位置
     * @param srcLen  长度
     * @param dest    字符信息
     * @param destPos 开始位置
     */
    public static void encode(byte[] src, int srcPos, int srcLen, char[] dest,
                              int destPos) {
        if (srcPos < 0 || srcLen < 0 || srcLen > src.length - srcPos)
            throw new IndexOutOfBoundsException();
        int destLen = (srcLen * 4 / 3 + 3) & ~3;
        if (destPos < 0 || destLen > dest.length - destPos)
            throw new IndexOutOfBoundsException();
        byte b1, b2, b3;
        int n = srcLen / 3;
        int r = srcLen - 3 * n;
        while (n-- > 0) {
            dest[destPos++] = CharUtils.getChars(Normal.ENCODE_64_TABLE)[((b1 = src[srcPos++]) >>> 2) & 0x3F];
            dest[destPos++] = CharUtils.getChars(Normal.ENCODE_64_TABLE)[((b1 & 0x03) << 4)
                    | (((b2 = src[srcPos++]) >>> 4) & 0x0F)];
            dest[destPos++] = CharUtils.getChars(Normal.ENCODE_64_TABLE)[((b2 & 0x0F) << 2)
                    | (((b3 = src[srcPos++]) >>> 6) & 0x03)];
            dest[destPos++] = CharUtils.getChars(Normal.ENCODE_64_TABLE)[b3 & 0x3F];
        }
        if (r > 0)
            if (r == 1) {
                dest[destPos++] = CharUtils.getChars(Normal.ENCODE_64_TABLE)[((b1 = src[srcPos]) >>> 2) & 0x3F];
                dest[destPos++] = CharUtils.getChars(Normal.ENCODE_64_TABLE)[((b1 & 0x03) << 4)];
                dest[destPos++] = Symbol.C_EQUAL;
                dest[destPos++] = Symbol.C_EQUAL;
            } else {
                dest[destPos++] = CharUtils.getChars(Normal.ENCODE_64_TABLE)[((b1 = src[srcPos++]) >>> 2) & 0x3F];
                dest[destPos++] = CharUtils.getChars(Normal.ENCODE_64_TABLE)[((b1 & 0x03) << 4)
                        | (((b2 = src[srcPos]) >>> 4) & 0x0F)];
                dest[destPos++] = CharUtils.getChars(Normal.ENCODE_64_TABLE)[(b2 & 0x0F) << 2];
                dest[destPos++] = Symbol.C_EQUAL;
            }
    }

}
