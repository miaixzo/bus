/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
package org.aoju.bus.core.utils;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;

import java.awt.*;
import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * 十六进制（简写为hex或下标16）在数学中是一种逢16进1的进位制,一般用数字0到9和字母A到F表示（其中:A~F即10~15）
 * 例如十进制数57,在二进制写作111001,在16进制写作39
 * 像java,c这样的语言为了区分十六进制和十进制数值,会在十六进制数的前面加上 0x,比如0x20是十进制的32,而不是十进制的20
 * <p>
 *
 * @author Kimi Liu
 * @version 5.6.9
 * @since JDK 1.8+
 */
public class HexUtils {

    /**
     * 判断给定字符串是否为16进制数
     * 如果是,需要使用对应数字类型对象的decode方法解码
     * 例如：{@code Integer.decode}方法解码int类型的16进制数字
     *
     * @param value 值
     * @return 是否为16进制
     */
    public static boolean isHexNumber(String value) {
        int index = (value.startsWith(Symbol.HYPHEN) ? 1 : 0);
        return (value.startsWith("0x", index) || value.startsWith("0X", index) || value.startsWith(Symbol.SHAPE, index));
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data byte[]
     * @return 十六进制char[]
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param str     字符串
     * @param charset 编码
     * @return 十六进制char[]
     */
    public static char[] encodeHex(String str, Charset charset) {
        return encodeHex(StringUtils.bytes(str, charset), true);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data        byte[]
     * @param toLowerCase true 传换成小写格式 , false 传换成大写格式
     * @return 十六进制char[]
     */
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? Normal.DIGITS_16_LOWER : Normal.DIGITS_16_UPPER);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data byte[]
     * @return 十六进制String
     */
    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符串,结果为小写
     *
     * @param data    被编码的字符串
     * @param charset 编码
     * @return 十六进制String
     */
    public static String encodeHexStr(String data, Charset charset) {
        return encodeHexStr(StringUtils.bytes(data, charset), true);
    }

    /**
     * 将字节数组转换为十六进制字符串,结果为小写,默认编码是UTF-8
     *
     * @param data 被编码的字符串
     * @return 十六进制String
     */
    public static String encodeHexStr(String data) {
        return encodeHexStr(data, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data        byte[]
     * @param toLowerCase true 传换成小写格式 , false 传换成大写格式
     * @return 十六进制String
     */
    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? Normal.DIGITS_16_LOWER : Normal.DIGITS_16_UPPER);
    }

    /**
     * 将十六进制字符数组转换为字符串,默认编码UTF-8
     *
     * @param hexStr 十六进制String
     * @return 字符串
     */
    public static String decodeHexStr(String hexStr) {
        return decodeHexStr(hexStr, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 将十六进制字符数组转换为字符串
     *
     * @param hexStr  十六进制String
     * @param charset 编码
     * @return 字符串
     */
    public static String decodeHexStr(String hexStr, Charset charset) {
        if (StringUtils.isEmpty(hexStr)) {
            return hexStr;
        }
        return decodeHexStr(hexStr.toCharArray(), charset);
    }

    /**
     * 将十六进制字符数组转换为字符串
     *
     * @param hexData 十六进制char[]
     * @param charset 编码
     * @return 字符串
     */
    public static String decodeHexStr(char[] hexData, Charset charset) {
        return StringUtils.toString(decodeHex(hexData), charset);
    }

    /**
     * 将十六进制字符数组转换为字节数组
     *
     * @param hexData 十六进制char[]
     * @return byte[]
     * @throws RuntimeException 如果源十六进制字符数组是一个奇怪的长度,将抛出运行时异常
     */
    public static byte[] decodeHex(char[] hexData) {

        int len = hexData.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(hexData[j], j) << 4;
            j++;
            f = f | toDigit(hexData[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * 将十六进制字符串解码为byte[]
     *
     * @param hexStr 十六进制String
     * @return byte[]
     */
    public static byte[] decodeHex(String hexStr) {
        if (StringUtils.isEmpty(hexStr)) {
            return null;
        }
        hexStr = StringUtils.removeAll(hexStr, Symbol.C_SPACE);
        return decodeHex(hexStr.toCharArray());
    }

    /**
     * 将{@link Color}编码为Hex形式
     *
     * @param color {@link Color}
     * @return Hex字符串
     * @since 3.1.9
     */
    public static String encodeColor(Color color) {
        return encodeColor(color, Symbol.SHAPE);
    }

    /**
     * 将{@link Color}编码为Hex形式
     *
     * @param color  {@link Color}
     * @param prefix 前缀字符串,可以是#、0x等
     * @return Hex字符串
     * @since 3.1.9
     */
    public static String encodeColor(Color color, String prefix) {
        final StringBuffer builder = new StringBuffer(prefix);
        String colorHex;
        colorHex = Integer.toHexString(color.getRed());
        if (1 == colorHex.length()) {
            builder.append(Symbol.C_ZERO);
        }
        builder.append(colorHex);
        colorHex = Integer.toHexString(color.getGreen());
        if (1 == colorHex.length()) {
            builder.append(Symbol.C_ZERO);
        }
        builder.append(colorHex);
        colorHex = Integer.toHexString(color.getBlue());
        if (1 == colorHex.length()) {
            builder.append(Symbol.C_ZERO);
        }
        builder.append(colorHex);
        return builder.toString();
    }

    /**
     * 将Hex颜色值转为
     *
     * @param hexColor 16进制颜色值,可以以#开头,也可以用0x开头
     * @return {@link Color}
     * @since 3.1.9
     */
    public static Color decodeColor(String hexColor) {
        return Color.decode(hexColor);
    }

    /**
     * 将指定int值转换为Unicode字符串形式,常用于特殊字符（例如汉字）转Unicode形式
     * 转换的字符串如果u后不足4位,则前面用0填充,例如：
     *
     * <pre>
     * '我' =》\u4f60
     * </pre>
     *
     * @param value int值,也可以是char
     * @return Unicode表现形式
     */
    public static String toUnicodeHex(int value) {
        final StringBuilder builder = new StringBuilder(6);

        builder.append("\\u");
        String hex = Integer.toHexString(value);
        int len = hex.length();
        if (len < 4) {
            builder.append("0000", 0, 4 - len);// 不足4位补0
        }
        builder.append(hex);

        return builder.toString();
    }

    /**
     * 将指定char值转换为Unicode字符串形式,常用于特殊字符（例如汉字）转Unicode形式
     * 转换的字符串如果u后不足4位,则前面用0填充,例如：
     *
     * <pre>
     * '我' =》\u4f60
     * </pre>
     *
     * @param ch char值
     * @return Unicode表现形式
     */
    public static String toUnicodeHex(char ch) {
        StringBuilder sb = new StringBuilder(6);
        sb.append("\\u");
        sb.append(Normal.DIGITS_16_LOWER[(ch >> 12) & 15]);
        sb.append(Normal.DIGITS_16_LOWER[(ch >> 8) & 15]);
        sb.append(Normal.DIGITS_16_LOWER[(ch >> 4) & 15]);
        sb.append(Normal.DIGITS_16_LOWER[(ch) & 15]);
        return sb.toString();
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data     byte[]
     * @param toDigits 用于控制输出的char[]
     * @return 十六进制String
     */
    private static String encodeHexStr(byte[] data, char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data     byte[]
     * @param toDigits 用于控制输出的char[]
     * @return 十六进制char[]
     */
    private static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters from the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    /**
     * 将十六进制字符转换成一个整数
     *
     * @param ch    十六进制char
     * @param index 十六进制字符在字符数组中的位置
     * @return 一个整数
     * @throws RuntimeException 当ch不是一个合法的十六进制字符时,抛出运行时异常
     */
    private static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

    /**
     * Hex（16进制）字符串转为BigInteger
     *
     * @param hexStr Hex(16进制字符串)
     * @return {@link BigInteger}
     */
    public static BigInteger toBigInteger(String hexStr) {
        if (null == hexStr) {
            return null;
        }
        return new BigInteger(hexStr, 16);
    }

}
