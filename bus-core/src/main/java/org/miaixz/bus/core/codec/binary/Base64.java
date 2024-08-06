/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.codec.binary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.miaixz.bus.core.codec.binary.decoder.Base64Decoder;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;

/**
 * Base64工具类，提供Base64的编码和解码方案 base64编码是用64（2的6次方）个ASCII字符来表示256（2的8次方）个ASCII字符，
 * 也就是三位二进制数组经过编码后变为四位的ASCII字符显示，长度比原来增加1/3。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base64 {

    /**
     * 编码为Base64，非URL安全的
     *
     * @param arr     被编码的数组
     * @param lineSep 在76个char之后是CRLF还是EOF
     * @return 编码后的bytes
     */
    public static byte[] encode(final byte[] arr, final boolean lineSep) {
        if (arr == null) {
            return null;
        }
        return lineSep ? java.util.Base64.getMimeEncoder().encode(arr) : java.util.Base64.getEncoder().encode(arr);
    }

    /**
     * base64编码
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encode(final CharSequence source) {
        return encode(source, Charset.UTF_8);
    }

    /**
     * base64编码，URL安全
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(final CharSequence source) {
        return encodeUrlSafe(source, Charset.UTF_8);
    }

    /**
     * base64编码
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被编码后的字符串
     */
    public static String encode(final CharSequence source, final java.nio.charset.Charset charset) {
        return encode(ByteKit.toBytes(source, charset));
    }

    /**
     * base64编码，URL安全的
     *
     * @param source  被编码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(final CharSequence source, final java.nio.charset.Charset charset) {
        return encodeUrlSafe(ByteKit.toBytes(source, charset));
    }

    /**
     * base64编码
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encode(final byte[] source) {
        if (source == null) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(source);
    }

    /**
     * base64编码，不进行padding(末尾不会填充'=')
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encodeWithoutPadding(final byte[] source) {
        if (source == null) {
            return null;
        }
        return java.util.Base64.getEncoder().withoutPadding().encodeToString(source);
    }

    /**
     * base64编码,URL安全的
     *
     * @param source 被编码的base64字符串
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(final byte[] source) {
        if (source == null) {
            return null;
        }
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(source);
    }

    /**
     * base64编码
     *
     * @param in 被编码base64的流（一般为图片流或者文件流）
     * @return 被加密后的字符串
     */
    public static String encode(final InputStream in) {
        if (in == null) {
            return null;
        }
        return encode(IoKit.readBytes(in));
    }

    /**
     * base64编码,URL安全的
     *
     * @param in 被编码base64的流（一般为图片流或者文件流）
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(final InputStream in) {
        if (in == null) {
            return null;
        }
        return encodeUrlSafe(IoKit.readBytes(in));
    }

    /**
     * base64编码
     *
     * @param file 被编码base64的文件
     * @return 被加密后的字符串
     */
    public static String encode(final File file) {
        return encode(FileKit.readBytes(file));
    }

    /**
     * base64编码,URL安全的
     *
     * @param file 被编码base64的文件
     * @return 被加密后的字符串
     */
    public static String encodeUrlSafe(final File file) {
        return encodeUrlSafe(FileKit.readBytes(file));
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
    public static void encode(byte[] src, int srcPos, int srcLen, char[] dest, int destPos) {
        if (srcPos < 0 || srcLen < 0 || srcLen > src.length - srcPos)
            throw new IndexOutOfBoundsException();
        int destLen = (srcLen * 4 / 3 + 3) & ~3;
        if (destPos < 0 || destLen > dest.length - destPos)
            throw new IndexOutOfBoundsException();
        byte b1, b2, b3;
        int n = srcLen / 3;
        int r = srcLen - 3 * n;
        while (n-- > 0) {
            dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b1 = src[srcPos++]) >>> 2) & 0x3F];
            dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b1 & 0x03) << 4)
                    | (((b2 = src[srcPos++]) >>> 4) & 0x0F)];
            dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b2 & 0x0F) << 2)
                    | (((b3 = src[srcPos++]) >>> 6) & 0x03)];
            dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[b3 & 0x3F];
        }
        if (r > 0)
            if (r == 1) {
                dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b1 = src[srcPos]) >>> 2) & 0x3F];
                dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b1 & 0x03) << 4)];
                dest[destPos++] = Symbol.C_EQUAL;
                dest[destPos++] = Symbol.C_EQUAL;
            } else {
                dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b1 = src[srcPos++]) >>> 2) & 0x3F];
                dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b1 & 0x03) << 4)
                        | (((b2 = src[srcPos]) >>> 4) & 0x0F)];
                dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[(b2 & 0x0F) << 2];
                dest[destPos++] = Symbol.C_EQUAL;
            }
    }

    /**
     * base64解码
     *
     * @param source 被解码的base64字符串
     * @return 被加密后的字符串
     */
    public static String decodeString(final CharSequence source) {
        return decodeString(source, Charset.UTF_8);
    }

    /**
     * base64解码
     *
     * @param source  被解码的base64字符串
     * @param charset 字符集
     * @return 被加密后的字符串
     */
    public static String decodeString(final CharSequence source, final java.nio.charset.Charset charset) {
        return StringKit.toString(decode(source), charset);
    }

    /**
     * base64解码
     *
     * @param base64   被解码的base64字符串
     * @param destFile 目标文件
     * @return 目标文件
     */
    public static File decodeToFile(final CharSequence base64, final File destFile) {
        return FileKit.writeBytes(decode(base64), destFile);
    }

    /**
     * base64解码
     *
     * @param base64     被解码的base64字符串
     * @param out        写出到的流
     * @param isCloseOut 是否关闭输出流
     */
    public static void decodeToStream(final CharSequence base64, final OutputStream out, final boolean isCloseOut) {
        IoKit.write(out, isCloseOut, decode(base64));
    }

    /**
     * base64解码
     *
     * @param base64 被解码的base64字符串
     * @return 解码后的bytes
     */
    public static byte[] decode(final CharSequence base64) {
        return decode(ByteKit.toBytes(base64, Charset.UTF_8));
    }

    /**
     * 解码Base64
     *
     * @param in 输入
     * @return 解码后的bytes
     */
    public static byte[] decode(final byte[] in) {
        return Base64Decoder.INSTANCE.decode(in);
    }

    /**
     * 解码Base64
     *
     * @param ch  字符信息
     * @param off 结束为止
     * @param len 长度
     * @param out 输出流
     */
    public static void decode(char[] ch, int off, int len, OutputStream out) {
        try {
            byte b2, b3;
            while ((len -= 2) >= 0) {
                out.write((byte) ((Normal.DECODE_64_TABLE[ch[off++]] << 2)
                        | ((b2 = Normal.DECODE_64_TABLE[ch[off++]]) >>> 4)));
                if ((len-- == 0) || ch[off] == Symbol.C_EQUAL)
                    break;
                out.write((byte) ((b2 << 4) | ((b3 = Normal.DECODE_64_TABLE[ch[off++]]) >>> 2)));
                if ((len-- == 0) || ch[off] == Symbol.C_EQUAL)
                    break;
                out.write((byte) ((b3 << 6) | Normal.DECODE_64_TABLE[ch[off++]]));
            }
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 检查是否为Base64
     *
     * @param base64 Base64的bytes
     * @return 是否为Base64
     */
    public static boolean isTypeBase64(final CharSequence base64) {
        if (base64 == null || base64.length() < 2) {
            return false;
        }

        final byte[] bytes = ByteKit.toBytes(base64);

        if (bytes.length != base64.length()) {
            // 如果长度不相等，说明存在双字节字符，肯定不是Base64，直接返回false
            return false;
        }

        return isTypeBase64(bytes);
    }

    /**
     * 检查是否为Base64格式
     *
     * @param base64Bytes Base64的bytes
     * @return 是否为Base64
     */
    public static boolean isTypeBase64(final byte[] base64Bytes) {
        if (base64Bytes == null || base64Bytes.length < 3) {
            return false;
        }

        boolean hasPadding = false;
        for (final byte base64Byte : base64Bytes) {
            if (hasPadding) {
                if (Symbol.C_EQUAL != base64Byte) {
                    // 前一个字符是'='，则后边的字符都必须是'='，即'='只能都位于结尾
                    return false;
                }
            } else if (Symbol.C_EQUAL == base64Byte) {
                // 发现'=' 标记之
                hasPadding = true;
            } else if (!(Base64Decoder.INSTANCE.isBase64Code(base64Byte) || isWhiteSpace(base64Byte))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWhiteSpace(final byte byteToCheck) {
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
