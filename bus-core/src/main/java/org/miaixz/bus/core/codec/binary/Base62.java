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

import org.miaixz.bus.core.codec.binary.provider.Base62Provider;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base62工具类，提供Base62的编码和解码方案
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base62 {

    /**
     * Base62编码
     *
     * @param source 被编码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String encode(final CharSequence source) {
        return encode(source, Charset.UTF_8);
    }

    /**
     * Base62编码
     *
     * @param source  被编码的Base62字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encode(final CharSequence source, final java.nio.charset.Charset charset) {
        return encode(StringKit.bytes(source, charset));
    }

    /**
     * Base62编码
     *
     * @param source 被编码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String encode(final byte[] source) {
        return new String(Base62Provider.INSTANCE.encode(source));
    }

    /**
     * Base62编码
     *
     * @param in 被编码Base62的流（一般为图片流或者文件流）
     * @return 被加密后的字符串
     */
    public static String encode(final InputStream in) {
        return encode(IoKit.readBytes(in));
    }

    /**
     * Base62编码
     *
     * @param file 被编码Base62的文件
     * @return 被加密后的字符串
     */
    public static String encode(final File file) {
        return encode(FileKit.readBytes(file));
    }

    /**
     * Base62编码（反转字母表模式）
     *
     * @param source 被编码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String encodeInverted(final CharSequence source) {
        return encodeInverted(source, Charset.UTF_8);
    }

    /**
     * Base62编码（反转字母表模式）
     *
     * @param source  被编码的Base62字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encodeInverted(final CharSequence source, final java.nio.charset.Charset charset) {
        return encodeInverted(StringKit.bytes(source, charset));
    }

    /**
     * Base62编码（反转字母表模式）
     *
     * @param source 被编码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String encodeInverted(final byte[] source) {
        return new String(Base62Provider.INSTANCE.encode(source, true));
    }

    /**
     * Base62编码
     *
     * @param in 被编码Base62的流（一般为图片流或者文件流）
     * @return 被加密后的字符串
     */
    public static String encodeInverted(final InputStream in) {
        return encodeInverted(IoKit.readBytes(in));
    }

    /**
     * Base62编码（反转字母表模式）
     *
     * @param file 被编码Base62的文件
     * @return 被加密后的字符串
     */
    public static String encodeInverted(final File file) {
        return encodeInverted(FileKit.readBytes(file));
    }

    /**
     * Base62解码
     *
     * @param base62 被解码的Base62字符串
     * @return 被加密后的字符串
     */
    public static byte[] decode(final CharSequence base62) {
        return decode(StringKit.bytes(base62, Charset.UTF_8));
    }

    /**
     * 解码Base62
     *
     * @param base62 Base62输入
     * @return 解码后的bytes
     */
    public static byte[] decode(final byte[] base62) {
        return Base62Provider.INSTANCE.decode(base62);
    }

    /**
     * Base62解码
     *
     * @param source 被解码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String decodeString(final CharSequence source) {
        return decodeString(source, Charset.UTF_8);
    }

    /**
     * Base62解码
     *
     * @param source  被解码的Base62字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeString(final CharSequence source, final java.nio.charset.Charset charset) {
        return StringKit.toString(decode(source), charset);
    }

    /**
     * Base62解码
     *
     * @param base62   被解码的Base62字符串
     * @param destFile 目标文件
     * @return 目标文件
     */
    public static File decodeToFile(final CharSequence base62, final File destFile) {
        return FileKit.writeBytes(decode(base62), destFile);
    }

    /**
     * Base62解码
     *
     * @param base62     被解码的Base62字符串
     * @param out        写出到的流
     * @param isCloseOut 是否关闭输出流
     */
    public static void decodeToStream(final CharSequence base62, final OutputStream out, final boolean isCloseOut) {
        IoKit.write(out, isCloseOut, decode(base62));
    }

    /**
     * Base62解码（反转字母表模式）
     *
     * @param source 被解码的Base62字符串
     * @return 被加密后的字符串
     */
    public static String decodeStrInverted(final CharSequence source) {
        return decodeStrInverted(source, Charset.UTF_8);
    }

    /**
     * Base62解码（反转字母表模式）
     *
     * @param source  被解码的Base62字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeStrInverted(final CharSequence source, final java.nio.charset.Charset charset) {
        return StringKit.toString(decodeInverted(source), charset);
    }

    /**
     * Base62解码（反转字母表模式）
     *
     * @param Base62   被解码的Base62字符串
     * @param destFile 目标文件
     * @return 目标文件
     */
    public static File decodeToFileInverted(final CharSequence Base62, final File destFile) {
        return FileKit.writeBytes(decodeInverted(Base62), destFile);
    }

    /**
     * Base62解码（反转字母表模式）
     *
     * @param base62     被解码的Base62字符串
     * @param out        写出到的流
     * @param isCloseOut 是否关闭输出流
     */
    public static void decodeToStreamInverted(final CharSequence base62, final OutputStream out, final boolean isCloseOut) {
        IoKit.write(out, isCloseOut, decodeInverted(base62));
    }

    /**
     * Base62解码（反转字母表模式）
     *
     * @param base62 被解码的Base62字符串
     * @return 被加密后的字符串
     */
    public static byte[] decodeInverted(final CharSequence base62) {
        return decodeInverted(StringKit.bytes(base62, Charset.UTF_8));
    }

    /**
     * 解码Base62（反转字母表模式）
     *
     * @param base62 Base62输入
     * @return 解码后的bytes
     */
    public static byte[] decodeInverted(final byte[] base62) {
        return Base62Provider.INSTANCE.decode(base62, true);
    }

}
