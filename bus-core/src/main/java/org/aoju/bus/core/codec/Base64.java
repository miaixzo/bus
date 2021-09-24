/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base64工具类,提供Base64的编码和解码方案
 * base64编码是用64(2的6次方)个ASCII字符来表示256(2的8次方)个ASCII字符,
 * 也就是三位二进制数组经过编码后变为四位的ASCII字符显示,长度比原来增加1/3
 *
 * @author Kimi Liu
 * @version 6.2.9
 * @since JDK 1.8+
 */
public class Base64 {

    /**
     * 编码为Base64，非URL安全的
     *
     * @param arr     被编码的数组
     * @param lineSep 在76个char之后是CRLF还是EOF
     * @return 编码后的bytes
     */
    public static byte[] encode(byte[] arr, boolean lineSep) {
        return Base64Encoder.encode(arr, lineSep);
    }

    /**
     * 编码为Base64，URL安全的
     *
     * @param arr     被编码的数组
     * @param lineSep 在76个char之后是CRLF还是EOF
     * @return 编码后的bytes
     */
    public static byte[] encodeUrlSafe(byte[] arr, boolean lineSep) {
        return Base64Encoder.encodeUrlSafe(arr, lineSep);
    }

    /**
     * base64编码
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encode(CharSequence source) {
        return Base64Encoder.encode(source);
    }

    /**
     * base64编码，URL安全
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(CharSequence source) {
        return Base64Encoder.encodeUrlSafe(source);
    }

    /**
     * base64编码
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(CharSequence source, String charset) {
        return Base64Encoder.encode(source, Charset.charset(charset));
    }

    /**
     * base64编码,URL安全
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(CharSequence source, String charset) {
        return Base64Encoder.encodeUrlSafe(source, Charset.charset(charset));
    }

    /**
     * base64编码
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(CharSequence source, java.nio.charset.Charset charset) {
        return Base64Encoder.encode(source, charset);
    }

    /**
     * base64编码，URL安全的
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(CharSequence source, java.nio.charset.Charset charset) {
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
     */
    public static String encodeUrlSafe(byte[] source) {
        return Base64Encoder.encodeUrlSafe(source);
    }

    /**
     * base64编码
     *
     * @param in 被编码base64的流(一般为图片流或者文件流)
     * @return 被加密后的字符串
     */
    public static String encode(InputStream in) {
        return Base64Encoder.encode(IoKit.readBytes(in));
    }

    /**
     * base64编码,URL安全的
     *
     * @param in 被编码base64的流(一般为图片流或者文件流)
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(InputStream in) {
        return Base64Encoder.encodeUrlSafe(IoKit.readBytes(in));
    }

    /**
     * base64编码
     *
     * @param file 被编码base64的文件
     * @return 被加密后的字符串
     */
    public static String encode(File file) {
        return Base64Encoder.encode(FileKit.readBytes(file));
    }

    /**
     * base64编码,URL安全的
     *
     * @param file 被编码base64的文件
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(File file) {
        return Base64Encoder.encodeUrlSafe(FileKit.readBytes(file));
    }

    /**
     * base64编码，不进行padding(末尾不会填充'=')
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encodeWithoutPadding(byte[] source) {
        return java.util.Base64.getEncoder().withoutPadding().encodeToString(source);
    }

    /**
     * base64编码，不进行padding(末尾不会填充'=')
     *
     * @param source  被编码的base64字符串
     * @param charset 编码
     * @return 被加密后的字符串
     */
    public static String encodeWithoutPadding(CharSequence source, String charset) {
        return encodeWithoutPadding(StringKit.bytes(source, charset));
    }

    /**
     * 编码为Base64
     * 如果isMultiLine为<code>true</code>，则每76个字符一个换行符，否则在一行显示
     *
     * @param arr         被编码的数组
     * @param isMultiLine 在76个char之后是CRLF还是EOF
     * @param isUrlSafe   是否使用URL安全字符，一般为<code>false</code>
     * @return 编码后的bytes
     */
    public static byte[] encode(byte[] arr, boolean isMultiLine, boolean isUrlSafe) {
        return Base64Encoder.encode(arr, isMultiLine, isUrlSafe);
    }

    public static void encode(byte[] src, int srcPos, int srcLen, char[] dest,
                              int destPos) {
        Base64Encoder.encode(src, srcPos, srcLen, dest, destPos);
    }

    /**
     * base64解码
     *
     * @param source 被解码的base64字符串
     * @return 被加密后的字符串
     */
    public static String decodeStrGbk(CharSequence source) {
        return Base64Decoder.decodeStr(source, Charset.GBK);
    }

    /**
     * base64解码
     *
     * @param source 被解码的base64字符串
     * @return 被加密后的字符串
     */
    public static String decodeStr(CharSequence source) {
        return Base64Decoder.decodeStr(source);
    }

    /**
     * base64解码
     *
     * @param source  被解码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStr(CharSequence source, String charset) {
        return Base64Decoder.decodeStr(source, Charset.charset(charset));
    }

    /**
     * base64解码
     *
     * @param source  被解码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStr(CharSequence source, java.nio.charset.Charset charset) {
        return Base64Decoder.decodeStr(source, charset);
    }

    /**
     * base64解码
     *
     * @param base64   被解码的base64字符串
     * @param destFile 目标文件
     * @return 目标文件
     */
    public static File decodeToFile(CharSequence base64, File destFile) {
        return FileKit.writeBytes(Base64Decoder.decode(base64), destFile);
    }

    /**
     * base64解码
     *
     * @param base64     被解码的base64字符串
     * @param out        写出到的流
     * @param isCloseOut 是否关闭输出流
     */
    public static void decodeToStream(CharSequence base64, OutputStream out, boolean isCloseOut) {
        IoKit.write(out, isCloseOut, Base64Decoder.decode(base64));
    }

    /**
     * base64解码
     *
     * @param base64 被解码的base64字符串
     * @return 被加密后的字符串
     */
    public static byte[] decode(CharSequence base64) {
        return Base64Decoder.decode(base64);
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

    public static void decode(char[] ch, int off, int len, OutputStream out) {
        try {
            Base64Decoder.decode(ch, off, len, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查是否为Base64
     *
     * @param base64 Base64的bytes
     * @return 是否为Base64
     */
    public static boolean isBase64(CharSequence base64) {
        return isBase64(StringKit.bytes(base64));
    }

    /**
     * 检查是否为Base64
     *
     * @param base64Bytes Base64的bytes
     * @return 是否为Base64
     */
    public static boolean isBase64(byte[] base64Bytes) {
        for (byte base64Byte : base64Bytes) {
            if (false == (Base64Decoder.isBase64Code(base64Byte) || isWhiteSpace(base64Byte))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWhiteSpace(byte byteToCheck) {
        switch (byteToCheck) {
            case Symbol.C_SPACE:
            case '\n':
            case '\r':
            case '\t':
                return true;
            default:
                return false;
        }
    }

}
