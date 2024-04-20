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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.RegEx;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.Similarity;
import org.miaixz.bus.core.text.TextBuilder;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 字符串处理类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringKit extends CharsKit {

    /**
     * 如果对象是字符串是否为空白，空白的定义如下：
     * 1、为null
     * 2、为不可见字符(如空格)
     * 3、""
     *
     * @param object 对象
     * @return 如果为字符串是否为空串
     */
    public static boolean isBlank(Object object) {
        if (null == object) {
            return true;
        } else if (object instanceof CharSequence) {
            return CharsKit.isBlank((CharSequence) object);
        }
        return false;
    }

    /**
     * 字符串去空格
     *
     * @param array 数组
     * @return 数组
     */
    public static String[] trim(String[] array) {
        if (ArrayKit.isEmpty(array)) {
            return array;
        }
        String[] resultArray = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            String param = array[i];
            resultArray[i] = CharsKit.trim(param);
        }
        return resultArray;
    }

    /**
     * 删除字符串两端的控制字符(char &lt;= 32)，如果字符串在修剪后为空("")，
     * 或者如果字符串为{@code null}，则返回空字符串("")
     *
     * <pre>
     * StringKit.trimToEmpty(null)          = ""
     * StringKit.trimToEmpty("")            = ""
     * StringKit.trimToEmpty("     ")       = ""
     * StringKit.trimToEmpty("abc")         = "abc"
     * StringKit.trimToEmpty("    abc    ") = "abc"
     * </pre>
     *
     * @param text 要被裁剪的字符串可能是空的
     * @return 如果{@code null}输入，则为空字符串
     */
    public static String trimToEmpty(final String text) {
        return null == text ? Normal.EMPTY : text.trim();
    }

    /**
     * 如果对象是字符串是否为空串空的定义如下:
     * 1、为null
     * 2、为""
     *
     * @param object 对象
     * @return 如果为字符串是否为空串
     */
    public static boolean emptyIfString(Object object) {
        if (null == object) {
            return true;
        } else if (object instanceof CharSequence) {
            return 0 == ((CharSequence) object).length();
        }
        return false;
    }

    public static boolean areNotEmpty(String... values) {
        boolean result = true;
        if (null != values && values.length != 0) {
            String[] var2 = values;
            int var3 = values.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                String value = var2[var4];
                result &= !CharsKit.isEmpty(value);
            }
        } else {
            result = false;
        }

        return result;
    }

    /**
     * 将对象转为字符串
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组
     * 2、对象数组会调用Arrays.toString方法
     *
     * @param object 对象
     * @return 字符串
     */
    public static String toString(Object object) {
        return toString(object, Charset.UTF_8);
    }

    /**
     * 将对象转为字符串
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组
     * 2、对象数组会调用Arrays.toString方法
     *
     * @param object  对象
     * @param charset 字符集
     * @return 字符串
     */
    public static String toString(Object object, String charset) {
        return toString(object, java.nio.charset.Charset.forName(charset));
    }

    /**
     * 返回传入的字符串，如果字符串是{@code null}，则返回{@code defaultStr}的值
     * <pre>
     * StringKit.toString(null, "NULL")  = "NULL"
     * StringKit.toString("", "NULL")    = ""
     * StringKit.toString("bat", "NULL") = "bat"
     * </pre>
     *
     * @param text       要检查的字符串可能为空
     * @param defaultStr 如果输入是{@code null}，返回的默认字符串可能是null
     * @return 传入的字符串，如果是{@code null}，则为默认值
     * @see String#valueOf(Object)
     */
    public static String toString(final String text, final String defaultStr) {
        return null == text ? defaultStr : text;
    }

    /**
     * 将对象转为字符串
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
     *
     * @param object  对象
     * @param charset 字符集
     * @return 字符串
     */
    public static String toString(Object object, java.nio.charset.Charset charset) {
        if (ObjectKit.isEmpty(object)) {
            return null;
        }

        if (object instanceof String) {
            return (String) object;
        } else if (object instanceof byte[]) {
            return toString((byte[]) object, charset);
        } else if (object instanceof Byte[]) {
            return toString((Byte[]) object, charset);
        } else if (object instanceof ByteBuffer) {
            return toString((ByteBuffer) object, charset);
        } else if (ArrayKit.isArray(object)) {
            return ArrayKit.toString(object);
        }

        return object.toString();
    }

    /**
     * 将byte数组转为字符串
     *
     * @param bytes   byte数组
     * @param charset 字符集
     * @return 字符串
     */
    public static String toString(byte[] bytes, String charset) {
        return toString(bytes, Charset.charset(charset));
    }

    /**
     * 解码字节码
     *
     * @param data    字符串
     * @param charset 字符集,如果此字段为空,则解码的结果取决于平台
     * @return 解码后的字符串
     */
    public static String toString(byte[] data, java.nio.charset.Charset charset) {
        if (null == data) {
            return null;
        }

        if (null == charset) {
            return new String(data);
        }
        return new String(data, charset);
    }

    /**
     * 将Byte数组转为字符串
     *
     * @param bytes   byte数组
     * @param charset 字符集
     * @return 字符串
     */
    public static String toString(Byte[] bytes, String charset) {
        return toString(bytes, Charset.charset(charset));
    }

    /**
     * 解码字节码
     *
     * @param data    字符串
     * @param charset 字符集,如果此字段为空,则解码的结果取决于平台
     * @return 解码后的字符串
     */
    public static String toString(Byte[] data, java.nio.charset.Charset charset) {
        if (null == data) {
            return null;
        }

        byte[] bytes = new byte[data.length];
        Byte dataByte;
        for (int i = 0; i < data.length; i++) {
            dataByte = data[i];
            bytes[i] = (null == dataByte) ? -1 : dataByte.byteValue();
        }

        return toString(bytes, charset);
    }

    /**
     * 将编码的byteBuffer数据转换为字符串
     *
     * @param data    数据
     * @param charset 字符集,如果为空使用当前系统字符集
     * @return 字符串
     */
    public static String toString(ByteBuffer data, String charset) {
        if (null == data) {
            return null;
        }

        return toString(data, java.nio.charset.Charset.forName(charset));
    }

    /**
     * 将编码的byteBuffer数据转换为字符串
     *
     * @param data    数据
     * @param charset 字符集,如果为空使用当前系统字符集
     * @return 字符串
     */
    public static String toString(ByteBuffer data, java.nio.charset.Charset charset) {
        if (null == charset) {
            charset = java.nio.charset.Charset.defaultCharset();
        }
        return charset.decode(data).toString();
    }

    /**
     * 转换String数组成字符串格式
     *
     * @param values 字符数组
     * @return 字符串信息
     */
    public static String toString(String[] values) {
        if (ArrayKit.isEmpty(values)) {
            return Normal.EMPTY;
        }

        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            builder.append(Symbol.COMMA + value);
        }

        String parameter = builder.toString().trim();
        if (parameter.length() > 0) {
            return parameter.substring(1);
        }

        return Normal.EMPTY;
    }

    /**
     * 转换Class数组成字符串格式
     *
     * @param parameterTypes 对象数组
     * @return 字符串信息
     */
    public static String toString(Class<?>[] parameterTypes) {
        if (ArrayKit.isEmpty(parameterTypes)) {
            return Normal.EMPTY;
        }

        StringBuilder builder = new StringBuilder();
        for (Class<?> clazz : parameterTypes) {
            builder.append(Symbol.COMMA + clazz.getName());
        }

        String parameter = builder.toString().trim();
        if (parameter.length() > 0) {
            return parameter.substring(1);
        }

        return Normal.EMPTY;
    }

    /**
     * 调用对象的toString方法，null会返回{@code null}
     *
     * @param object 对象
     * @return 字符串 or {@code null}
     */
    public static String toStringOrNull(Object object) {
        return null == object ? null : object.toString();
    }

    /**
     * 检查给定的{@code String}是否包含实际的文本。
     * 更具体地说，如果{@code String}不是{@code null}，
     * 那么这个方法返回{@code true}，它的长度大于0，并且至少包含一个非空白字符
     *
     * @param text 要检查的{@code String}(可能是{@code null})
     * @return 如果{@code String}不是{@code null}，那么它的长度大于0，并且不包含空格
     */
    public static boolean hasText(String text) {
        if (null == text || text.isEmpty()) {
            return false;
        }
        int strLen = text.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查给定的{@code String}既不是{@code null}也不是长度为0.
     *
     * @param text 要检查的{@code String}(可能是{@code null})
     * @return 如果{@code String}不是{@code null}，并且有长度，则为{@code true}
     * @see #hasText(String)
     */
    public static boolean hasLength(String text) {
        return (null != text && !text.isEmpty());
    }

    /**
     * 将base64字符串处理成String字节
     *
     * @param text base64的字符串
     * @return 原字节数据
     */
    public static byte[] base64ToByte(String text) {
        try {
            if (null == text) {
                return null;
            }
            return Base64.getDecoder().decode(text);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 将base64字符串处理成String
     * (用默认的String编码集)
     *
     * @param text base64的字符串
     * @return 可显示的字符串
     */
    public static String base64ToString(String text) {
        try {
            if (null == text) {
                return null;
            }
            return new String(base64ToByte(text), Charset.UTF_8);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 将base64字符串处理成String
     * (用默认的String编码集)
     *
     * @param text    base64的字符串
     * @param charset 编码格式(UTF-8/GBK)
     * @return 可显示的字符串
     */
    public static String base64ToString(String text, String charset) {
        try {
            if (null == text) {
                return null;
            }
            return new String(base64ToByte(text), charset);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 将字节数组换成16进制的字符串
     *
     * @param byteArray 字节数组
     * @return string
     */
    public static String byteArrayToHex(byte[] byteArray) {
        // 首先初始化一个字符数组,用来存放每个16进制字符

        // new一个字符数组,这个就是用来组成结果字符串的(解释一下：一个byte是八位二进制,也就是2位十六进制字符(2的8次方等于16的2次方))
        char[] resultCharArray = new char[byteArray.length * 2];
        // 遍历字节数组,通过位运算(位运算效率高),转换成字符放到字符数组中去
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = Normal.DIGITS_16_LOWER[b >>> 4 & 0xf];
            resultCharArray[index++] = Normal.DIGITS_16_LOWER[b & 0xf];
        }
        // 字符数组组合成字符串返回
        return new String(resultCharArray);
    }

    /**
     * bytes字符串转换为Byte值
     * src Byte字符串,每个Byte之间没有分隔符
     *
     * @param hexText 字符串
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hexText) {
        if (CharsKit.isEmpty(hexText)) {
            return Normal.EMPTY_BYTE_ARRAY;
        }
        int len = hexText.length();
        // 检查字符串是否为有效十六进制
        if (!RegEx.VALID_HEX.matcher(hexText).matches() || (len & 0x1) != 0) {
            return Normal.EMPTY_BYTE_ARRAY;
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) (Character.digit(hexText.charAt(i), Normal._16) << 4
                    | Character.digit(hexText.charAt(i + 1), Normal._16));
        }
        return data;
    }

    /**
     * 字符编码为Unicode形式
     *
     * @param c 被编码的字符
     * @return Unicode字符串
     * @see HexKit#toUnicodeHex(char)
     */
    public static String toUnicode(char c) {
        return HexKit.toUnicodeHex(c);
    }

    /**
     * 字符编码为Unicode形式
     *
     * @param c 被编码的字符
     * @return Unicode字符串
     * @see HexKit#toUnicodeHex(int)
     */
    public static String toUnicode(int c) {
        return HexKit.toUnicodeHex(c);
    }

    /**
     * 字符串编码为Unicode形式
     *
     * @param text 被编码的字符串
     * @return Unicode字符串
     */
    public static String toUnicode(String text) {
        return toUnicode(text, true);
    }

    /**
     * 字符串编码为Unicode形式
     *
     * @param text        被编码的字符串
     * @param isSkipAscii 是否跳过ASCII字符（只跳过可见字符）
     * @return Unicode字符串
     */
    public static String toUnicode(String text, boolean isSkipAscii) {
        if (StringKit.isEmpty(text)) {
            return text;
        }

        final int len = text.length();
        final StringBuilder unicode = new StringBuilder(text.length() * 6);
        char c;
        for (int i = 0; i < len; i++) {
            c = text.charAt(i);
            if (isSkipAscii && CharsKit.isAsciiPrintable(c)) {
                unicode.append(c);
            } else {
                unicode.append(HexKit.toUnicodeHex(c));
            }
        }
        return unicode.toString();
    }

    /**
     * Unicode字符串转为普通字符串
     * Unicode字符串的表现方式为：\\uXXXX
     *
     * @param text Unicode字符串
     * @return 普通字符串
     */
    public static String toUnicodeString(String text) {
        if (StringKit.isBlank(text)) {
            return text;
        }

        final int len = text.length();
        StringBuilder sb = new StringBuilder(len);
        int i;
        int pos = 0;
        while ((i = StringKit.indexOfIgnoreCase(text, Symbol.UNICODE_START_CHAR, pos)) != -1) {
            // 写入Unicode符之前的部分
            sb.append(text, pos, i);
            pos = i;
            if (i + 5 < len) {
                char c;
                try {
                    c = (char) Integer.parseInt(text.substring(i + 2, i + 6), Normal._16);
                    sb.append(c);
                    pos = i + 6;// 跳过整个Unicode符
                } catch (NumberFormatException e) {
                    // 非法Unicode符，跳过,  写入"\\u"
                    sb.append(text, pos, i + 2);
                    pos = i + 2;
                }
            } else {
                // 非Unicode符，结束
                break;
            }
        }

        if (pos < len) {
            sb.append(text, pos, len);
        }
        return sb.toString();
    }

    /**
     * 截取字符串,从指定位置开始,截取指定长度的字符串
     *
     * @param input     原始字符串
     * @param fromIndex 开始的index,包括
     * @param length    要截取的长度
     * @return 截取后的字符串
     */
    public static String sub(String input, int fromIndex, int length) {
        return CharsKit.sub(input, fromIndex, fromIndex + length);
    }

    /**
     * 切分字符串
     *
     * @param text 被切分的字符串
     * @return 字符串
     */
    public static String split(String text) {
        return split(text, Symbol.COMMA, Symbol.COMMA);
    }

    /**
     * 切分字符串
     *
     * @param text      被切分的字符串
     * @param separator 分隔符
     * @param reserve   替换后的分隔符
     * @return 字符串
     */
    public static String split(String text, String separator, String reserve) {
        StringBuffer sb = new StringBuffer();
        if (CharsKit.isNotEmpty(text)) {
            String[] arr = CharsKit.splitToArray(text, separator);
            for (int i = 0; i < arr.length; i++) {
                if (i == 0) {
                    sb.append(Symbol.SINGLE_QUOTE).append(arr[i]).append(Symbol.SINGLE_QUOTE);
                } else {
                    sb.append(reserve).append(Symbol.SINGLE_QUOTE).append(arr[i]).append(Symbol.SINGLE_QUOTE);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 获取字符串中最左边的{@code len}字符
     *
     * <pre>
     * StringKit.left(null, *)    = null
     * StringKit.left(*, -ve)     = ""
     * StringKit.left("", *)      = ""
     * StringKit.left("abc", 0)   = ""
     * StringKit.left("abc", 2)   = "ab"
     * StringKit.left("abc", 4)   = "abc"
     * </pre>
     *
     * @param text 要从中获取字符的字符串可能为空
     * @param len  所需字符串的长度
     * @return 最左边的字符，{@code null}如果输入为空字符串
     */
    public static String left(final String text, final int len) {
        if (null == text) {
            return null;
        }
        if (len < 0) {
            return Normal.EMPTY;
        }
        if (text.length() <= len) {
            return text;
        }
        return text.substring(0, len);
    }

    /**
     * 获取字符串中最右边的{@code len}字符
     *
     * <pre>
     * StringKit.right(null, *)    = null
     * StringKit.right(*, -ve)     = ""
     * StringKit.right("", *)      = ""
     * StringKit.right("abc", 0)   = ""
     * StringKit.right("abc", 2)   = "bc"
     * StringKit.right("abc", 4)   = "abc"
     * </pre>
     *
     * @param text 要从中获取字符的字符串可能为空
     * @param len  所需字符串的长度
     * @return 最右边的字符，{@code null}如果输入为空字符串
     */
    public static String right(final String text, final int len) {
        if (null == text) {
            return null;
        }
        if (len < 0) {
            return Normal.EMPTY;
        }
        if (text.length() <= len) {
            return text;
        }
        return text.substring(text.length() - len);
    }

    /**
     * 从字符串中间获取{@code len}字符.
     *
     * <pre>
     * StringKit.mid(null, *, *)    = null
     * StringKit.mid(*, *, -ve)     = ""
     * StringKit.mid("", 0, *)      = ""
     * StringKit.mid("abc", 0, 2)   = "ab"
     * StringKit.mid("abc", 0, 4)   = "abc"
     * StringKit.mid("abc", 2, 4)   = "c"
     * StringKit.mid("abc", 4, 2)   = ""
     * StringKit.mid("abc", -2, 2)  = "ab"
     * </pre>
     *
     * @param text 要从中获取字符的字符串可能为空
     * @param pos  开始时的位置，负为零
     * @param len  所需字符串的长度
     * @return 中间的字符，{@code null}如果输入为空字符串
     */
    public static String mid(final String text, int pos, final int len) {
        if (null == text) {
            return null;
        }
        if (len < 0 || pos > text.length()) {
            return Normal.EMPTY;
        }
        if (pos < 0) {
            pos = 0;
        }
        if (text.length() <= pos + len) {
            return text.substring(pos);
        }
        return text.substring(pos, pos + len);
    }

    /**
     * 删除指定字符串
     * 是否在开始位置,否则返回源字符串
     * <pre>
     * StringKit.removeStart(null, *)                    = null
     * StringKit.removeStart("", *)                      = ""
     * StringKit.removeStart(*, null)                    = *
     * StringKit.removeStart("www.domain.com", "www.")   = "domain.com"
     * StringKit.removeStart("domain.com", "www.")       = "domain.com"
     * StringKit.removeStart("www.domain.com", "domain") = "www.domain.com"
     * StringKit.removeStart("abc", "")                  = "abc"
     * </pre>
     *
     * @param text   要搜索的源字符串可能为空
     * @param remove 要搜索和删除的字符串可能为空
     * @return 如果找到，则删除字符串，如果输入为空字符串，则{@code null}
     */
    public static String removeStart(final String text, final String remove) {
        if (CharsKit.isEmpty(text) || CharsKit.isEmpty(remove)) {
            return text;
        }
        if (text.startsWith(remove)) {
            return text.substring(remove.length());
        }
        return text;
    }

    /**
     * 构建新的字符串
     *
     * @param original     原始对象
     * @param middle       中间隐藏信息
     * @param prefixLength 前边信息长度
     * @return 构建后的新字符串
     */
    public static String build(final Object original,
                               final String middle,
                               final int prefixLength) {
        if (ObjectKit.isNull(original)) {
            return null;
        }

        final String string = original.toString();
        final int stringLength = string.length();

        String prefix;

        if (stringLength >= prefixLength) {
            prefix = string.substring(0, prefixLength);
        } else {
            prefix = string.substring(0, stringLength);
        }

        String suffix = Normal.EMPTY;
        int suffixLength = stringLength - prefix.length() - middle.length();
        if (suffixLength > 0) {
            suffix = string.substring(stringLength - suffixLength);
        }

        return prefix + middle + suffix;
    }

    /**
     * 连接多个字符串为一个
     *
     * @param isNullToEmpty 是否null转为""
     * @param texts         字符串数组
     * @return 连接后的字符串
     */
    public static String concat(boolean isNullToEmpty, CharSequence... texts) {
        final TextBuilder sb = new TextBuilder();
        for (CharSequence text : texts) {
            sb.append(isNullToEmpty ? CharsKit.nullToEmpty(text) : text);
        }
        return sb.toString();
    }

    /**
     * 切换给定字符串中的大小写 大写转小写,小写转大写
     *
     * <pre>
     * StringKit.swapCase(null)                 = null
     * StringKit.swapCase("")                   = ""
     * StringKit.swapCase("The dog has a BONE") = "tHE DOG HAS A bone"
     * </pre>
     *
     * @param text 字符串
     * @return 交换后的字符串
     */
    public static String swapCase(final String text) {
        if (CharsKit.isEmpty(text)) {
            return text;
        }

        final char[] buffer = text.toCharArray();

        for (int i = 0; i < buffer.length; i++) {
            final char ch = buffer[i];
            if (Character.isUpperCase(ch)) {
                buffer[i] = Character.toLowerCase(ch);
            } else if (Character.isTitleCase(ch)) {
                buffer[i] = Character.toLowerCase(ch);
            } else if (Character.isLowerCase(ch)) {
                buffer[i] = Character.toUpperCase(ch);
            }
        }
        return new String(buffer);
    }

    /**
     * 将已有字符串填充为规定长度,如果已有字符串超过这个长度则返回这个字符串
     * 字符填充于字符串前
     *
     * @param text       被填充的字符串
     * @param filledChar 填充的字符
     * @param len        填充长度
     * @return 填充后的字符串
     */
    public static String fillBefore(String text, char filledChar, int len) {
        return fill(text, filledChar, len, true);
    }

    /**
     * 将已有字符串填充为规定长度,如果已有字符串超过这个长度则返回这个字符串
     * 字符填充于字符串后
     *
     * @param strVal  被填充的字符串
     * @param charVal 填充的字符
     * @param len     填充长度
     * @return 填充后的字符串
     */
    public static String fillAfter(String strVal, char charVal, int len) {
        return fill(strVal, charVal, len, false);
    }

    /**
     * 将已有字符串填充为规定长度,如果已有字符串超过这个长度则返回这个字符串
     *
     * @param strVal  被填充的字符串
     * @param charVal 填充的字符
     * @param len     填充长度
     * @param isPre   是否填充在前
     * @return 填充后的字符串
     */
    public static String fill(String strVal, char charVal, int len, boolean isPre) {
        final int strLen = strVal.length();
        if (strLen > len) {
            return strVal;
        }

        String filled = CharsKit.repeat(charVal, len - strLen);
        return isPre ? filled.concat(strVal) : strVal.concat(filled);
    }

    /**
     * 输出指定长度字符
     *
     * @param count   长度
     * @param charVal 字符
     * @return 填充后的字符串
     */
    public static String fill(int count, char charVal) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be greater than or equal 0.");
        }
        char[] chs = new char[count];
        for (int i = 0; i < count; i++) {
            chs[i] = charVal;
        }
        return new String(chs);
    }

    /**
     * 输出指定长度字符
     *
     * @param count  长度
     * @param strVal 字符
     * @return 填充后的字符串
     */
    public static String fill(int count, String strVal) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be greater than or equal 0.");
        }
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(strVal);
        }
        return sb.toString();
    }

    /**
     * 获得StringReader
     *
     * @param text 字符串
     * @return StringReader
     */
    public static StringReader getReader(CharSequence text) {
        if (null == text) {
            return null;
        }
        return new StringReader(text.toString());
    }

    /**
     * 获得StringWriter
     *
     * @return StringWriter
     */
    public static StringWriter getWriter() {
        return new StringWriter();
    }

    /**
     * 统计 字符串 中单词出现次数(不排序)
     *
     * @param text      字符串
     * @param separator 分隔符
     * @return 统计次数 如: {"hello":10}
     */
    public static Map<String, Long> count(String text, String separator) {
        return count(Collections.singletonList(text), separator);
    }

    /**
     * 统计 字符串 中单词出现次数(根据value排序)
     *
     * @param text        字符串
     * @param separator   分隔符
     * @param isValueDesc 是否倒叙排列
     * @return 统计次数 如: {"hello":10}
     */
    public static Map<String, Long> count(String text, String separator, boolean isValueDesc) {
        return count(Collections.singletonList(text), separator, isValueDesc);
    }

    /**
     * 统计list中单词出现次数(不排序)
     *
     * @param list      list容器
     * @param separator 分隔符
     * @return 统计次数
     */
    public static Map<String, Long> count(List<String> list, String separator) {
        Map<String, Long> countMap = MapKit.newHashMap();
        for (String text : list) {
            String[] words = text.split(separator);
            for (String word : words) {
                countMap.put(word, countMap.getOrDefault(word, 0L) + 1);
            }
        }
        return countMap;
    }

    /**
     * 统计 字符串list 中单词出现次数(根据value排序)
     *
     * @param list        list容器
     * @param separator   分隔符
     * @param isValueDesc 是否根据value倒叙排列
     * @return 统计次数
     */
    public static Map<String, Long> count(List<String> list, String separator, boolean isValueDesc) {
        return MapKit.sort(count(list, separator), isValueDesc);
    }

    /**
     * 清理字符串,清理出某些不可见字符和一些sql特殊字符
     *
     * @param txt 文本
     * @return {String}
     */
    public static String cleanText(String txt) {
        if (null == txt) {
            return null;
        }
        return Pattern.compile("[`'\"|/,;()-+*%#·•�　\\s]").matcher(txt).replaceAll(Normal.EMPTY);
    }

    /**
     * 根据{@link String#toUpperCase()}将字符串转换为大写.
     *
     * <pre>
     * StringKit.upperCase(null)  = null
     * StringKit.upperCase("")    = ""
     * StringKit.upperCase("aBc") = "ABC"
     * </pre>
     *
     * @param text 字符串可以为空
     * @return 大写字符串{@code null}如果输入为空字符串
     */
    public static String upperCase(final String text) {
        if (null == text) {
            return null;
        }
        return text.toUpperCase();
    }

    /**
     * 根据{@link String#toUpperCase()}将字符串转换为大写
     *
     * <pre>
     * StringKit.upperCase(null, Locale.ENGLISH)  = null
     * StringKit.upperCase("", Locale.ENGLISH)    = ""
     * StringKit.upperCase("aBc", Locale.ENGLISH) = "ABC"
     * </pre>
     *
     * @param text   字符串可以为空
     * @param locale 定义案例转换规则的区域设置不能为空
     * @return 大写字符串{@code null}如果输入为空字符串
     */
    public static String upperCase(final String text, final Locale locale) {
        if (null == text) {
            return null;
        }
        return text.toUpperCase(locale);
    }

    /**
     * 根据{@link String#toLowerCase()}将字符串转换为小写
     *
     * <pre>
     * StringKit.lowerCase(null)  = null
     * StringKit.lowerCase("")    = ""
     * StringKit.lowerCase("aBc") = "abc"
     * </pre>
     *
     * @param text 字符串可以为空
     * @return 小写字符串{@code null}如果输入为空字符串
     */
    public static String lowerCase(final String text) {
        if (null == text) {
            return null;
        }
        return text.toLowerCase();
    }

    /**
     * 根据{@link String#toLowerCase()}将字符串转换为小写
     *
     * <pre>
     * StringKit.lowerCase(null, Locale.ENGLISH)  = null
     * StringKit.lowerCase("", Locale.ENGLISH)    = ""
     * StringKit.lowerCase("aBc", Locale.ENGLISH) = "abc"
     * </pre>
     *
     * @param text   字符串可以为空
     * @param locale t定义案例转换规则的区域设置不能为空
     * @return 小写字符串{@code null}如果输入为空字符串
     */
    public static String lowerCase(final String text, final Locale locale) {
        if (null == text) {
            return null;
        }
        return text.toLowerCase(locale);
    }

    /**
     * 按{@link Character#toTitleCase(int)}
     * 将第一个字符更改为标题大小写.其他字符没有改变
     *
     * <pre>
     * StringKit.capitalize(null)  = null
     * StringKit.capitalize("")    = ""
     * StringKit.capitalize("cat") = "Cat"
     * StringKit.capitalize("cAt") = "CAt"
     * StringKit.capitalize("'cat'") = "'cat'"
     * </pre>
     *
     * @param text 要大写的字符串可以为空
     * @return 大写字符串，{@code null}如果输入为空字符串
     */
    public static String capitalize(final String text) {
        int strLen;
        if (null == text || (strLen = text.length()) == 0) {
            return text;
        }

        final int firstCodepoint = text.codePointAt(0);
        final int newCodePoint = Character.toTitleCase(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            return text;
        }

        final int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        newCodePoints[outOffset++] = newCodePoint;
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
            final int codepoint = text.codePointAt(inOffset);
            newCodePoints[outOffset++] = codepoint;
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

    /**
     * 取消字符串的大小写，将第一个字符改为小写。其他字符没有改变
     *
     * <pre>
     * StringKit.uncapitalize(null)  = null
     * StringKit.uncapitalize("")    = ""
     * StringKit.uncapitalize("cat") = "cat"
     * StringKit.uncapitalize("Cat") = "cat"
     * StringKit.uncapitalize("CAT") = "cAT"
     * </pre>
     *
     * @param text 要取消大写的字符串可以为空
     * @return 未大写的字符串，{@code null}如果输入为空字符串
     */
    public static String unCapitalize(final String text) {
        int strLen;
        if (null == text || (strLen = text.length()) == 0) {
            return text;
        }

        final int firstCodepoint = text.codePointAt(0);
        final int newCodePoint = Character.toLowerCase(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            return text;
        }

        final int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        newCodePoints[outOffset++] = newCodePoint;
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
            final int codepoint = text.codePointAt(inOffset);
            newCodePoints[outOffset++] = codepoint;
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

    /**
     * 字符串按照字符排序方法
     *
     * @param text 排序字段
     * @return {@link String}
     */
    public static String sort(String text) {
        if (CharsKit.isEmpty(text) || Normal.EMPTY.equalsIgnoreCase(text)) {
            return null;
        }
        char[] strArray = text.toCharArray();

        Arrays.sort(strArray);

        return String.valueOf(strArray);
    }

    /**
     * 计算两个字符串的相似度
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 相似度
     */
    public static double similar(String str1, String str2) {
        return Similarity.similar(str1, str2);
    }

    /**
     * 计算两个字符串的相似度百分比
     *
     * @param str1  字符串1
     * @param str2  字符串2
     * @param scale 相似度
     * @return 相似度百分比
     */
    public static String similar(String str1, String str2, int scale) {
        return Similarity.similar(str1, str2, scale);
    }

    /**
     * 字符串按照指定长度换行
     *
     * @param content 字符内容
     * @param length  换行长度
     * @return 换行后的内容
     */
    public static String newLine(String content, int length) {
        String stVal = Normal.EMPTY;
        if (length > 0) {
            if (content.length() > length) {
                int rows = (content.length() + length - 1) / length;
                for (int i = 0; i < rows; i++) {
                    if (i == rows - 1) {
                        stVal += content.substring(i * length);
                    } else {
                        stVal += content.substring(i * length, i * length + length) + "\r\n";
                    }
                }
            } else {
                stVal = content;
            }
        }
        return stVal;
    }

}
