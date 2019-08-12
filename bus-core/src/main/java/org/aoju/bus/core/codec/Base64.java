/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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

import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Base64工具类，提供Base64的编码和解码方案
 * base64编码是用64（2的6次方）个ASCII字符来表示256（2的8次方）个ASCII字符，
 * 也就是三位二进制数组经过编码后变为四位的ASCII字符显示，长度比原来增加1/3。
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class Base64 {

    /**
     * 标准编码表
     */
    public static final byte[] STANDARD_ENCODE_TABLE = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    /**
     * URL安全的编码表，将 + 和 / 替换为 - 和 _
     */
    public static final byte[] URL_SAFE_ENCODE_TABLE = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

    /**
     * Base64解码表，共128位，-1表示非base64字符，-2表示padding
     */
    public static final byte[] DECODE_TABLE = {
            // 0 1 2 3 4 5 6 7 8 9 A B C D E F
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 00-0f
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 10-1f
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, // 20-2f + - /
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, // 30-3f 0-9
            -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, // 40-4f A-O
            15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, // 50-5f P-Z _
            -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, // 60-6f a-o
            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 // 70-7a p-z
    };

    /**
     * Base64解码表，共128位，-1表示非base64字符，-2表示padding
     */
    public static final byte PADDING = -2;


    private static String encode(byte[] in, byte[] map) {
        int length = (in.length + 2) / 3 * 4;
        byte[] out = new byte[length];
        int index = 0, end = in.length - in.length % 3;
        for (int i = 0; i < end; i += 3) {
            out[index++] = map[(in[i] & 0xff) >> 2];
            out[index++] = map[((in[i] & 0x03) << 4) | ((in[i + 1] & 0xff) >> 4)];
            out[index++] = map[((in[i + 1] & 0x0f) << 2) | ((in[i + 2] & 0xff) >> 6)];
            out[index++] = map[(in[i + 2] & 0x3f)];
        }
        switch (in.length % 3) {
            case 1:
                out[index++] = map[(in[end] & 0xff) >> 2];
                out[index++] = map[(in[end] & 0x03) << 4];
                out[index++] = '=';
                out[index++] = '=';
                break;
            case 2:
                out[index++] = map[(in[end] & 0xff) >> 2];
                out[index++] = map[((in[end] & 0x03) << 4) | ((in[end + 1] & 0xff) >> 4)];
                out[index++] = map[((in[end + 1] & 0x0f) << 2)];
                out[index++] = '=';
                break;
        }
        return new String(out, StandardCharsets.US_ASCII);
    }


    public static void encode(byte[] src, int srcPos, int srcLen, byte[] dest,
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
            dest[destPos++] = STANDARD_ENCODE_TABLE[((b1 = src[srcPos++]) >>> 2) & 0x3F];
            dest[destPos++] = STANDARD_ENCODE_TABLE[((b1 & 0x03) << 4)
                    | (((b2 = src[srcPos++]) >>> 4) & 0x0F)];
            dest[destPos++] = STANDARD_ENCODE_TABLE[((b2 & 0x0F) << 2)
                    | (((b3 = src[srcPos++]) >>> 6) & 0x03)];
            dest[destPos++] = STANDARD_ENCODE_TABLE[b3 & 0x3F];
        }
        if (r > 0)
            if (r == 1) {
                dest[destPos++] = STANDARD_ENCODE_TABLE[((b1 = src[srcPos]) >>> 2) & 0x3F];
                dest[destPos++] = STANDARD_ENCODE_TABLE[((b1 & 0x03) << 4)];
                dest[destPos++] = '=';
                dest[destPos++] = '=';
            } else {
                dest[destPos++] = STANDARD_ENCODE_TABLE[((b1 = src[srcPos++]) >>> 2) & 0x3F];
                dest[destPos++] = STANDARD_ENCODE_TABLE[((b1 & 0x03) << 4)
                        | (((b2 = src[srcPos]) >>> 4) & 0x0F)];
                dest[destPos++] = STANDARD_ENCODE_TABLE[(b2 & 0x0F) << 2];
                dest[destPos++] = '=';
            }
    }

    public static void decode(char[] ch, int off, int len, OutputStream out)
            throws IOException {
        byte b2, b3;
        while ((len -= 2) >= 0) {
            out.write((byte) ((DECODE_TABLE[ch[off++]] << 2)
                    | ((b2 = DECODE_TABLE[ch[off++]]) >>> 4)));
            if ((len-- == 0) || ch[off] == '=')
                break;
            out.write((byte) ((b2 << 4)
                    | ((b3 = DECODE_TABLE[ch[off++]]) >>> 2)));
            if ((len-- == 0) || ch[off] == '=')
                break;
            out.write((byte) ((b3 << 6) | DECODE_TABLE[ch[off++]]));
        }
    }

    /**
     * 编码为Base64，非URL安全的
     *
     * @param in      被编码的数组
     * @param lineSep 在76个char之后是CRLF还是EOF
     * @return 编码后的bytes
     */
    public static byte[] encode(byte[] in, boolean lineSep) {
        return Base64Encoder.encode(in, lineSep);
    }

    /**
     * 编码为Base64，URL安全的
     *
     * @param in      被编码的数组
     * @param lineSep 在76个char之后是CRLF还是EOF
     * @return 编码后的bytes
     * @since 3.0.6
     */
    public static byte[] encodeUrlSafe(byte[] in, boolean lineSep) {
        return Base64Encoder.encodeUrlSafe(in, lineSep);
    }

    /**
     * base64编码
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encode(String source) {
        return Base64Encoder.encode(source);
    }

    /**
     * base64编码，URL安全
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     * @since 3.0.6
     */
    public static String encodeUrlSafe(String source) {
        return Base64Encoder.encodeUrlSafe(source);
    }

    /**
     * base64编码
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(String source, String charset) {
        return Base64Encoder.encode(source, charset);
    }

    /**
     * base64编码,URL安全
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     * @since 3.0.6
     */
    public static String encodeUrlSafe(String source, String charset) {
        return Base64Encoder.encodeUrlSafe(source, charset);
    }

    /**
     * base64编码
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(String source, Charset charset) {
        return Base64Encoder.encode(source, charset);
    }

    /**
     * base64编码，URL安全的
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     * @since 3.0.6
     */
    public static String encodeUrlSafe(String source, Charset charset) {
        return Base64Encoder.encodeUrlSafe(source, charset);
    }

    /**
     * base64编码
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encode(byte[] source) {
        return Base64Encoder.encode(source);
    }

    /**
     * base64编码,URL安全的
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     * @since 3.0.6
     */
    public static String encodeUrlSafe(byte[] source) {
        return Base64Encoder.encodeUrlSafe(source);
    }

    /**
     * base64编码
     *
     * @param in 被编码base64的流（一般为图片流或者文件流）
     * @return 被加密后的字符串
     * @since 4.0.9
     */
    public static String encode(InputStream in) {
        return Base64Encoder.encode(IoUtils.readBytes(in));
    }

    /**
     * base64编码,URL安全的
     *
     * @param in 被编码base64的流（一般为图片流或者文件流）
     * @return 被加密后的字符串
     * @since 4.0.9
     */
    public static String encodeUrlSafe(InputStream in) {
        return Base64Encoder.encodeUrlSafe(IoUtils.readBytes(in));
    }

    /**
     * base64编码
     *
     * @param file 被编码base64的文件
     * @return 被加密后的字符串
     * @since 4.0.9
     */
    public static String encode(File file) {
        return Base64Encoder.encode(FileUtils.readBytes(file));
    }

    /**
     * base64编码,URL安全的
     *
     * @param file 被编码base64的文件
     * @return 被加密后的字符串
     * @since 4.0.9
     */
    public static String encodeUrlSafe(File file) {
        return Base64Encoder.encodeUrlSafe(FileUtils.readBytes(file));
    }

    /**
     * base64编码
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(byte[] source, String charset) {
        return Base64Encoder.encode(source, charset);
    }

    /**
     * base64编码，URL安全的
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     * @since 3.0.6
     */
    public static String encodeUrlSafe(byte[] source, String charset) {
        return Base64Encoder.encodeUrlSafe(source, charset);
    }

    /**
     * base64编码
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(byte[] source, Charset charset) {
        return Base64Encoder.encode(source, charset);
    }

    /**
     * base64编码，URL安全的
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     * @since 3.0.6
     */
    public static String encodeUrlSafe(byte[] source, Charset charset) {
        return Base64Encoder.encodeUrlSafe(source, charset);
    }

    /**
     * 编码为Base64
     * 如果isMultiLine为 true，则每76个字符一个换行符，否则在一行显示
     *
     * @param arr         被编码的数组
     * @param isMultiLine 在76个char之后是CRLF还是EOF
     * @param isUrlSafe   是否使用URL安全字符，一般为false
     * @return 编码后的bytes
     */
    public static byte[] encode(byte[] arr, boolean isMultiLine, boolean isUrlSafe) {
        return Base64Encoder.encode(arr, isMultiLine, isUrlSafe);
    }

    /**
     * base64解码
     *
     * @param source 被解码的base64字符串
     * @return 被加密后的字符串
     */
    public static String decodeStr(String source) {
        return Base64Decoder.decodeStr(source);
    }

    /**
     * base64解码
     *
     * @param source  被解码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStr(String source, String charset) {
        return Base64Decoder.decodeStr(source, charset);
    }

    /**
     * base64解码
     *
     * @param source  被解码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStr(String source, Charset charset) {
        return Base64Decoder.decodeStr(source, charset);
    }

    /**
     * base64解码
     *
     * @param base64   被解码的base64字符串
     * @param destFile 目标文件
     * @return 目标文件
     * @since 4.0.9
     */
    public static File decodeToFile(String base64, File destFile) {
        return FileUtils.writeBytes(Base64Decoder.decode(base64), destFile);
    }

    /**
     * base64解码
     *
     * @param base64     被解码的base64字符串
     * @param out        写出到的流
     * @param isCloseOut 是否关闭输出流
     * @since 4.0.9
     */
    public static void decodeToStream(String base64, OutputStream out, boolean isCloseOut) {
        IoUtils.write(out, isCloseOut, Base64Decoder.decode(base64));
    }

    /**
     * base64解码
     *
     * @param base64 被解码的base64字符串
     * @return 被加密后的字符串
     */
    public static byte[] decode(String base64) {
        return Base64Decoder.decode(base64);
    }

    /**
     * base64解码
     *
     * @param source  被解码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static byte[] decode(String source, String charset) {
        return Base64Decoder.decode(source, charset);
    }

    /**
     * base64解码
     *
     * @param source  被解码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static byte[] decode(String source, Charset charset) {
        return Base64Decoder.decode(source, charset);
    }

    /**
     * 解码Base64
     *
     * @param in 输入
     * @return 解码后的bytes
     */
    public static byte[] decode(byte[] in) {
        return Base64Decoder.decode(in);
    }

}
