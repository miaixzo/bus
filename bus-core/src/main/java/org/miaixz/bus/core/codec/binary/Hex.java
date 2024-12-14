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

import org.miaixz.bus.core.codec.binary.provider.Base16Provider;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 十六进制（简写为hex或下标16）在数学中是一种逢16进1的进位制，一般用数字0到9和字母A到F表示（其中:A~F即10~15）。 例如十进制数57，在二进制写作111001，在16进制写作39。
 * 像java,c这样的语言为了区分十六进制和十进制数值,会在十六进制数的前面加上 0x,比如0x20是十进制的32,而不是十进制的20
 *
 * @author Kimi Liu
 * @see Base16Provider
 * @since Java 17+
 */
public class Hex {

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data byte[]
     * @return 十六进制char[]
     */
    public static char[] encode(final byte[] data) {
        return encode(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param text    字符串
     * @param charset 编码
     * @return 十六进制char[]
     */
    public static char[] encode(final String text, final java.nio.charset.Charset charset) {
        return encode(ByteKit.toBytes(text, charset), true);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data        byte[]
     * @param toLowerCase {@code true} 传换成小写格式 ， {@code false} 传换成大写格式
     * @return 十六进制char[]。如果提供的data为{@code null}，返回{@code null}
     */
    public static char[] encode(final byte[] data, final boolean toLowerCase) {
        if (null == data) {
            return null;
        }
        return (toLowerCase ? Base16Provider.CODEC_LOWER : Base16Provider.CODEC_UPPER).encode(data);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data byte[]
     * @return 十六进制String
     */
    public static String encodeString(final byte[] data) {
        return encodeString(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data        byte[]
     * @param toLowerCase {@code true} 传换成小写格式 ， {@code false} 传换成大写格式
     * @return 十六进制String
     */
    public static String encodeString(final byte[] data, final boolean toLowerCase) {
        return StringKit.toString(encode(data, toLowerCase), Charset.UTF_8);
    }

    /**
     * 将字符串转换为十六进制字符串，结果为小写，默认编码是UTF-8
     *
     * @param data 被编码的字符串
     * @return 十六进制String
     */
    public static String encodeString(final CharSequence data) {
        return encodeString(data, Charset.UTF_8);
    }

    /**
     * 将字符串转换为十六进制字符串，结果为小写
     *
     * @param data    需要被编码的字符串
     * @param charset 编码
     * @return 十六进制String
     */
    public static String encodeString(final CharSequence data, final java.nio.charset.Charset charset) {
        return encodeString(ByteKit.toBytes(data, charset), true);
    }

    /**
     * 将十六进制字符数组转换为字节数组
     *
     * @param data 十六进制字符串
     * @return byte[]
     * @throws InternalException 如果源十六进制字符数组是一个奇怪的长度，将抛出运行时异常
     */
    public static byte[] decode(final CharSequence data) {
        return Base16Provider.CODEC_LOWER.decode(data);
    }

    /**
     * 将十六进制字符数组转换为字节数组
     *
     * @param data 十六进制char[]
     * @return byte[]
     * @throws RuntimeException 如果源十六进制字符数组是一个奇怪的长度，将抛出运行时异常
     */
    public static byte[] decode(final char[] data) {
        return decode(String.valueOf(data));
    }

    /**
     * 将十六进制字符数组转换为字符串，默认编码UTF-8
     *
     * @param data 十六进制String
     * @return 字符串
     */
    public static String decodeString(final CharSequence data) {
        return decodeString(data, Charset.UTF_8);
    }

    /**
     * 将十六进制字符数组转换为字符串
     *
     * @param data    十六进制String
     * @param charset 编码
     * @return 字符串
     */
    public static String decodeString(final CharSequence data, final java.nio.charset.Charset charset) {
        if (StringKit.isEmpty(data)) {
            return StringKit.toStringOrNull(data);
        }
        return StringKit.toString(decode(data), charset);
    }

    /**
     * 将十六进制字符数组转换为字符串
     *
     * @param data    十六进制char[]
     * @param charset 编码
     * @return 字符串
     */
    public static String decodeString(final char[] data, final java.nio.charset.Charset charset) {
        return StringKit.toString(decode(data), charset);
    }

}
