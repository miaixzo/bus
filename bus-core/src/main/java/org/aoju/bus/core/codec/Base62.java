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

import org.aoju.bus.core.codec.provider.Base62Provider;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Base62工具类，提供Base62的编码和解码方案
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since Java 17+
 */
public class Base62 {

    /**
     * Base62编码
     *
     * @param source 被编码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String encode(CharSequence source) {
        return encode(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * Base62编码
     *
     * @param source  被编码的Base62字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(CharSequence source, Charset charset) {
        return encode(StringKit.bytes(source, charset));
    }

    /**
     * Base62编码
     *
     * @param source 被编码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String encode(byte[] source) {
        return new String(Base62Provider.INSTANCE.encode(source));
    }

    /**
     * Base62编码
     *
     * @param in 被编码Base62的流（一般为图片流或者文件流）
     * @return 被加密后的字符串
     */
    public static String encode(InputStream in) {
        return encode(IoKit.readBytes(in));
    }

    /**
     * Base62编码
     *
     * @param file 被编码Base62的文件
     * @return 被加密后的字符串
     */
    public static String encode(File file) {
        return encode(FileKit.readBytes(file));
    }

    /**
     * Base62编码（反转字母表模式）
     *
     * @param source 被编码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String encodeInverted(CharSequence source) {
        return encodeInverted(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * Base62编码（反转字母表模式）
     *
     * @param source  被编码的Base62字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encodeInverted(CharSequence source, Charset charset) {
        return encodeInverted(StringKit.bytes(source, charset));
    }

    /**
     * Base62编码（反转字母表模式）
     *
     * @param source 被编码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String encodeInverted(byte[] source) {
        return new String(Base62Provider.INSTANCE.encode(source, true));
    }

    /**
     * Base62编码
     *
     * @param in 被编码Base62的流（一般为图片流或者文件流）
     * @return 被加密后的字符串
     */
    public static String encodeInverted(InputStream in) {
        return encodeInverted(IoKit.readBytes(in));
    }

    /**
     * Base62编码（反转字母表模式）
     *
     * @param file 被编码Base62的文件
     * @return 被加密后的字符串
     */
    public static String encodeInverted(File file) {
        return encodeInverted(FileKit.readBytes(file));
    }

    // -------------------------------------------------------------------- decode

    /**
     * Base62解码
     *
     * @param source 被解码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String decodeStrGbk(CharSequence source) {
        return decodeStr(source, org.aoju.bus.core.lang.Charset.GBK);
    }

    /**
     * Base62解码
     *
     * @param source 被解码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String decodeStr(CharSequence source) {
        return decodeStr(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * Base62解码
     *
     * @param source  被解码的Base62字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStr(CharSequence source, Charset charset) {
        return StringKit.toString(decode(source), charset);
    }

    /**
     * Base62解码
     *
     * @param Base62   被解码的Base62字符串
     * @param destFile 目标文件
     * @return 目标文件
     */
    public static File decodeToFile(CharSequence Base62, File destFile) {
        return FileKit.writeBytes(decode(Base62), destFile);
    }

    /**
     * Base62解码
     *
     * @param base62Str  被解码的Base62字符串
     * @param out        写出到的流
     * @param isCloseOut 是否关闭输出流
     */
    public static void decodeToStream(CharSequence base62Str, OutputStream out, boolean isCloseOut) {
        IoKit.write(out, isCloseOut, decode(base62Str));
    }

    /**
     * Base62解码
     *
     * @param base62Str 被解码的Base62字符串
     * @return 被加密后的字符串
     */
    public static byte[] decode(CharSequence base62Str) {
        return decode(StringKit.bytes(base62Str, org.aoju.bus.core.lang.Charset.UTF_8));
    }

    /**
     * 解码Base62
     *
     * @param base62bytes Base62输入
     * @return 解码后的bytes
     */
    public static byte[] decode(byte[] base62bytes) {
        return Base62Provider.INSTANCE.decode(base62bytes);
    }

    /**
     * Base62解码（反转字母表模式）
     *
     * @param source 被解码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String decodeStrInverted(CharSequence source) {
        return decodeStrInverted(source, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * Base62解码（反转字母表模式）
     *
     * @param source  被解码的Base62字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStrInverted(CharSequence source, Charset charset) {
        return StringKit.toString(decodeInverted(source), charset);
    }

    /**
     * Base62解码（反转字母表模式）
     *
     * @param Base62   被解码的Base62字符串
     * @param destFile 目标文件
     * @return 目标文件
     */
    public static File decodeToFileInverted(CharSequence Base62, File destFile) {
        return FileKit.writeBytes(decodeInverted(Base62), destFile);
    }

    /**
     * Base62解码（反转字母表模式）
     *
     * @param base62Str  被解码的Base62字符串
     * @param out        写出到的流
     * @param isCloseOut 是否关闭输出流
     */
    public static void decodeToStreamInverted(CharSequence base62Str, OutputStream out, boolean isCloseOut) {
        IoKit.write(out, isCloseOut, decodeInverted(base62Str));
    }

    /**
     * Base62解码（反转字母表模式）
     *
     * @param base62Str 被解码的Base62字符串
     * @return 被加密后的字符串
     */
    public static byte[] decodeInverted(CharSequence base62Str) {
        return decodeInverted(StringKit.bytes(base62Str, org.aoju.bus.core.lang.Charset.UTF_8));
    }

    /**
     * 解码Base62（反转字母表模式）
     *
     * @param base62bytes Base62输入
     * @return 解码后的bytes
     */
    public static byte[] decodeInverted(byte[] base62bytes) {
        return Base62Provider.INSTANCE.decode(base62bytes, true);
    }
}
