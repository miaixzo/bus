/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.codec;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.ByteUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.nio.charset.Charset;

/**
 * Base64编码
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
 */
public class Base64Encoder {

    /**
     * 编码为Base64,非URL安全的
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
     * @since 3.1.9
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
    public static String encode(String source) {
        return encode(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * base64编码,URL安全
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     * @since 3.1.9
     */
    public static String encodeUrlSafe(String source) {
        return encodeUrlSafe(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * base64编码
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(String source, String charset) {
        return encode(StringUtils.bytes(source, charset), charset);
    }

    /**
     * base64编码,URL安全
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     * @since 3.1.9
     */
    public static String encodeUrlSafe(String source, String charset) {
        return encodeUrlSafe(StringUtils.bytes(source, charset), charset);
    }

    /**
     * base64编码
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(String source, Charset charset) {
        return encode(StringUtils.bytes(source, charset), charset);
    }

    /**
     * base64编码,URL安全的
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     * @since 3.1.9
     */
    public static String encodeUrlSafe(String source, Charset charset) {
        return encodeUrlSafe(StringUtils.bytes(source, charset), charset);
    }

    /**
     * base64编码
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encode(byte[] source) {
        return encode(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * base64编码,URL安全的
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     * @since 3.1.9
     */
    public static String encodeUrlSafe(byte[] source) {
        return encodeUrlSafe(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * base64编码
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(byte[] source, String charset) {
        return StringUtils.str(encode(source, false), charset);
    }

    /**
     * base64编码,URL安全的
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     * @since 3.1.9
     */
    public static String encodeUrlSafe(byte[] source, String charset) {
        return StringUtils.str(encodeUrlSafe(source, false), charset);
    }

    /**
     * base64编码
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(byte[] source, Charset charset) {
        return StringUtils.str(encode(source, false), charset);
    }

    /**
     * base64编码,URL安全的
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     * @since 3.1.9
     */
    public static String encodeUrlSafe(byte[] source, Charset charset) {
        return StringUtils.str(encodeUrlSafe(source, false), charset);
    }

    /**
     * 只接受一个字节缓冲区并返回一个包含已编码缓冲区的字符串
     *
     * @param buffer    被编码的数组
     * @param charset   字符集
     * @param isUrlSafe 是否使用URL安全字符,一般为<code>false</code>
     * @return 编码后的字符串
     */
    public static String encodeBuffer(byte[] buffer, boolean isUrlSafe, Charset charset) {
        return StringUtils.str(encode(buffer, true, isUrlSafe), charset);
    }

    /**
     * 只接受一个字节缓冲区并返回一个包含已编码缓冲区的字符串
     *
     * @param buffer    被编码的数组
     * @param isUrlSafe 是否使用URL安全字符,一般为<code>false</code>
     * @return 编码后的字符串
     */
    public static String encodeBuffer(byte[] buffer, boolean isUrlSafe) {
        return encodeBuffer(buffer, isUrlSafe, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 只接受一个字节缓冲区并返回一个包含已编码缓冲区的字符串
     *
     * @param buffer  被编码的数组
     * @param charset 字符集
     * @return 编码后的字符串
     */
    public static String encodeBuffer(byte[] buffer, Charset charset) {
        return encodeBuffer(buffer, false, charset);
    }

    /**
     * 只接受一个字节缓冲区并返回一个包含已编码缓冲区的字符串
     *
     * @param buffer 被编码的数组
     * @return 编码后的字符串
     */
    public static String encodeBuffer(byte[] buffer) {
        return encodeBuffer(buffer, false);
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

        byte[] encodeTable = isUrlSafe ? Normal.URL_SAFE_ENCODE_TABLE : ByteUtils.getBytes(Normal.STANDARD_ENCODE_TABLE);

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

}
