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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.codec.binary.Hex;
import org.miaixz.bus.core.codec.binary.provider.Base16Provider;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import java.awt.*;
import java.math.BigInteger;

/**
 * 十六进制（简写为hex或下标16）在数学中是一种逢16进1的进位制，一般用数字0到9和字母A到F表示（其中:A~F即10~15）。
 * 例如十进制数57，在二进制写作111001，在16进制写作39。
 * 像java,c这样的语言为了区分十六进制和十进制数值,会在十六进制数的前面加上 0x,比如0x20是十进制的32,而不是十进制的20
 * <p>
 * 此工具类为16进制组合工具类，除了继承{@link Hex}实现编码解码外，提供其它转换类和识别类工具。
 *
 * @author Kimi Liu
 * @see Hex
 * @since Java 17+
 */
public class HexKit extends Hex {

    /**
     * 将{@link Color}编码为Hex形式
     *
     * @param color {@link Color}
     * @return Hex字符串
     */
    public static String encodeColor(final Color color) {
        return encodeColor(color, Symbol.SHAPE);
    }

    /**
     * 将{@link Color}编码为Hex形式
     *
     * @param color  {@link Color}
     * @param prefix 前缀字符串，可以是#、0x等
     * @return Hex字符串
     */
    public static String encodeColor(final Color color, final String prefix) {
        final StringBuilder builder = new StringBuilder(prefix);
        String colorHex;
        colorHex = Integer.toHexString(color.getRed());
        if (1 == colorHex.length()) {
            builder.append('0');
        }
        builder.append(colorHex);
        colorHex = Integer.toHexString(color.getGreen());
        if (1 == colorHex.length()) {
            builder.append('0');
        }
        builder.append(colorHex);
        colorHex = Integer.toHexString(color.getBlue());
        if (1 == colorHex.length()) {
            builder.append('0');
        }
        builder.append(colorHex);
        return builder.toString();
    }

    /**
     * 将Hex颜色值转为
     *
     * @param hexColor 16进制颜色值，可以以#开头，也可以用0x开头
     * @return {@link Color}
     */
    public static Color decodeColor(final String hexColor) {
        return Color.decode(hexColor);
    }

    /**
     * 判断给定字符串是否为16进制数
     * 如果是，需要使用对应数字类型对象的{@code decode}方法解码
     * 例如：{@code Integer.decode}方法解码int类型的16进制数字
     *
     * @param value 值
     * @return 是否为16进制
     */
    public static boolean isHexNumber(final String value) {
        if (StringKit.startWith(value, Symbol.C_MINUS)) {
            return false;
        }
        int index = 0;
        if (value.startsWith("0x", index) || value.startsWith("0X", index)) {
            index += 2;
        } else if (value.startsWith(Symbol.SHAPE, index)) {
            index++;
        }
        try {
            new BigInteger(value.substring(index), 16);
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * 将指定int值转换为Unicode字符串形式，常用于特殊字符（例如汉字）转Unicode形式
     * 转换的字符串如果u后不足4位，则前面用0填充，例如：
     *
     * <pre>
     * 你 = &#92;u4f60
     * </pre>
     *
     * @param value int值，也可以是char
     * @return Unicode表现形式
     */
    public static String toUnicodeHex(final int value) {
        final StringBuilder builder = new StringBuilder(6);

        builder.append("\\u");
        final String hex = toHex(value);
        final int len = hex.length();
        if (len < 4) {
            builder.append("0000", 0, 4 - len);// 不足4位补0
        }
        builder.append(hex);

        return builder.toString();
    }

    /**
     * 将指定char值转换为Unicode字符串形式，常用于特殊字符（例如汉字）转Unicode形式
     * 转换的字符串如果u后不足4位，则前面用0填充，例如：
     *
     * <pre>
     * 你 = &#92;u4f60
     * </pre>
     *
     * @param ch char值
     * @return Unicode表现形式
     */
    public static String toUnicodeHex(final char ch) {
        return Base16Provider.CODEC_LOWER.toUnicodeHex(ch);
    }

    /**
     * 转为16进制字符串
     *
     * @param value int值
     * @return 16进制字符串
     */
    public static String toHex(final int value) {
        return Integer.toHexString(value);
    }

    /**
     * 16进制字符串转为int
     *
     * @param value 16进制字符串
     * @return 16进制字符串int值
     */
    public static int hexToInt(final String value) {
        return Integer.parseInt(value, 16);
    }

    /**
     * 转为16进制字符串
     *
     * @param value int值
     * @return 16进制字符串
     */
    public static String toHex(final long value) {
        return Long.toHexString(value);
    }

    /**
     * 16进制字符串转为long
     *
     * @param value 16进制字符串
     * @return long值
     */
    public static long hexToLong(final String value) {
        return Long.parseLong(value, 16);
    }

    /**
     * 将byte值转为16进制并添加到{@link StringBuilder}中
     *
     * @param builder     {@link StringBuilder}
     * @param b           byte
     * @param toLowerCase 是否使用小写
     */
    public static void appendHex(final StringBuilder builder, final byte b, final boolean toLowerCase) {
        (toLowerCase ? Base16Provider.CODEC_LOWER : Base16Provider.CODEC_UPPER).appendHex(builder, b);
    }

    /**
     * Hex（16进制）字符串转为BigInteger
     *
     * @param hexStr Hex(16进制字符串)
     * @return {@link BigInteger}
     */
    public static BigInteger toBigInteger(final String hexStr) {
        if (null == hexStr) {
            return null;
        }
        return new BigInteger(hexStr, 16);
    }

    /**
     * 格式化Hex字符串，结果为每2位加一个空格，类似于：
     * <pre>
     *     e8 8c 67 03 80 cb 22 00 95 26 8f
     * </pre>
     *
     * @param hexStr Hex字符串
     * @return 格式化后的字符串
     */
    public static String format(final String hexStr) {
        return format(hexStr, Normal.EMPTY);
    }

    /**
     * 格式化Hex字符串，结果为每2位加一个空格，类似于：
     * <pre>
     *     e8 8c 67 03 80 cb 22 00 95 26 8f
     * </pre>
     *
     * @param hexStr Hex字符串
     * @param prefix 自定义前缀，如0x
     * @return 格式化后的字符串
     */
    public static String format(final String hexStr, String prefix) {
        if (null == prefix) {
            prefix = Normal.EMPTY;
        }

        final int length = hexStr.length();
        final StringBuilder builder = StringKit.builder(length + length / 2 + (length / 2 * prefix.length()));
        builder.append(prefix).append(hexStr.charAt(0)).append(hexStr.charAt(1));
        for (int i = 2; i < length - 1; i += 2) {
            builder.append(Symbol.C_SPACE).append(prefix).append(hexStr.charAt(i)).append(hexStr.charAt(i + 1));
        }
        return builder.toString();
    }

}
