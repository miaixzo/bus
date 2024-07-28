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

import org.miaixz.bus.core.center.function.FunctionPool;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.text.TextSimilarity;
import org.miaixz.bus.core.text.placeholder.StringFormatter;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * 字符串工具类
 *
 * @author Kimi Liu
 * @see CharsBacker#split(CharSequence, CharSequence) 对字符串分割
 * @see ArrayKit#hasBlank(CharSequence...) 对多个字符串判空
 * @since Java 17+
 */
public class StringKit extends CharsBacker {

    /**
     * 给定字符串数组全部做去首尾空格
     *
     * @param args 字符串数组
     */
    public static void trim(final String[] args) {
        if (null == args) {
            return;
        }
        String text;
        for (int i = 0; i < args.length; i++) {
            text = args[i];
            if (null != text) {
                args[i] = trim(text);
            }
        }
    }

    /**
     * 将对象转为字符串
     *
     * <pre>
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组
     * 2、对象数组会调用Arrays.toString方法
     * </pre>
     *
     * @param obj 对象
     * @return 字符串
     */
    public static String toString(final Object obj) {
        return toString(obj, Charset.UTF_8);
    }

    /**
     * 将对象转为字符串
     * 
     * <pre>
     * 	 1、Byte数组和ByteBuffer会被转换为对应字符串的数组
     * 	 2、char[]会直接构造String
     * 	 3、对象数组会调用Arrays.toString方法
     * </pre>
     *
     * @param obj     对象
     * @param charset 字符集
     * @return 字符串
     */
    public static String toString(final Object obj, final java.nio.charset.Charset charset) {
        if (null == obj) {
            return null;
        }

        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof char[]) {
            return new String((char[]) obj);
        } else if (obj instanceof byte[]) {
            return toString((byte[]) obj, charset);
        } else if (obj instanceof Byte[]) {
            return toString((Byte[]) obj, charset);
        } else if (obj instanceof ByteBuffer) {
            return toString((ByteBuffer) obj, charset);
        } else if (ArrayKit.isArray(obj)) {
            return ArrayKit.toString(obj);
        }

        return obj.toString();
    }

    /**
     * 解码字节码
     *
     * @param data    字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 解码后的字符串
     */
    public static String toString(final byte[] data, final java.nio.charset.Charset charset) {
        if (data == null) {
            return null;
        }

        if (null == charset) {
            return new String(data);
        }
        return new String(data, charset);
    }

    /**
     * 解码字节码
     *
     * @param data    字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 解码后的字符串
     */
    public static String toString(final Byte[] data, final java.nio.charset.Charset charset) {
        if (data == null) {
            return null;
        }

        final byte[] bytes = new byte[data.length];
        Byte dataByte;
        for (int i = 0; i < data.length; i++) {
            dataByte = data[i];
            bytes[i] = (null == dataByte) ? -1 : dataByte;
        }

        return toString(bytes, charset);
    }

    /**
     * 将编码的byteBuffer数据转换为字符串
     *
     * @param data    数据
     * @param charset 字符集，如果为空使用当前系统字符集
     * @return 字符串
     */
    public static String toString(final ByteBuffer data, java.nio.charset.Charset charset) {
        if (null == charset) {
            charset = java.nio.charset.Charset.defaultCharset();
        }
        return charset.decode(data).toString();
    }

    /**
     * JDK8中，通过{@code String(char[] value, boolean share)}这个内部构造创建String对象。
     * 此函数通过传入char[]，实现zero-copy的String创建，效率很高。但是要求传入的char[]不可以在其他地方修改。
     *
     * @param value char[]值，注意这个数组不可修改！！
     * @return String
     */
    public static String stringFast(final char[] value) {
        return FunctionPool.createString(value);
    }

    /**
     * 创建StringBuilder对象
     *
     * @return StringBuilder对象
     */
    public static StringBuilder builder() {
        return new StringBuilder();
    }

    /**
     * 创建StringBuilder对象
     *
     * @param capacity 初始大小
     * @return StringBuilder对象
     */
    public static StringBuilder builder(final int capacity) {
        return new StringBuilder(capacity);
    }

    /**
     * 获得StringReader
     *
     * @param text 字符串
     * @return StringReader
     */
    public static StringReader getReader(final CharSequence text) {
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
     * 反转字符串 例如：abcd = dcba
     *
     * @param text 被反转的字符串
     * @return 反转后的字符串
     */
    public static String reverse(final String text) {
        return new String(ArrayKit.reverse(text.toCharArray()));
    }

    /**
     * 将已有字符串填充为规定长度，如果已有字符串超过这个长度则返回这个字符串 字符填充于字符串前
     *
     * @param text       被填充的字符串
     * @param filledChar 填充的字符
     * @param len        填充长度
     * @return 填充后的字符串
     */
    public static String fillBefore(final String text, final char filledChar, final int len) {
        return fill(text, filledChar, len, true);
    }

    /**
     * 将已有字符串填充为规定长度，如果已有字符串超过这个长度则返回这个字符串 字符填充于字符串后
     *
     * @param text       被填充的字符串
     * @param filledChar 填充的字符
     * @param len        填充长度
     * @return 填充后的字符串
     */
    public static String fillAfter(final String text, final char filledChar, final int len) {
        return fill(text, filledChar, len, false);
    }

    /**
     * 将已有字符串填充为规定长度，如果已有字符串超过这个长度则返回这个字符串
     *
     * @param text       被填充的字符串
     * @param filledChar 填充的字符
     * @param len        填充长度
     * @param isPre      是否填充在前
     * @return 填充后的字符串
     */
    public static String fill(final String text, final char filledChar, final int len, final boolean isPre) {
        final int strLen = text.length();
        if (strLen > len) {
            return text;
        }

        final String filledStr = repeat(filledChar, len - strLen);
        return isPre ? filledStr.concat(text) : text.concat(filledStr);
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
     * 计算两个字符串的相似度
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 相似度
     */
    public static double similar(final String str1, final String str2) {
        return TextSimilarity.similar(str1, str2);
    }

    /**
     * 计算两个字符串的相似度百分比
     *
     * @param str1  字符串1
     * @param str2  字符串2
     * @param scale 相似度
     * @return 相似度百分比
     */
    public static String similar(final String str1, final String str2, final int scale) {
        return TextSimilarity.similar(str1, str2, scale);
    }

    /**
     * 格式化文本，使用 {varName} 占位 map = {a: "aValue", b: "bValue"} format("{a} and {b}", map) = aValue and bValue
     *
     * @param template 文本模板，被替换的部分用 {data} 表示
     * @param map      参数值对
     * @return 格式化后的文本
     */
    public static String format(final CharSequence template, final Map<?, ?> map) {
        return format(template, map, true);
    }

    /**
     * 格式化文本，使用 {varName} 占位 map = {a: "aValue", b: "bValue"} format("{a} and {b}", map) = aValue and bValue
     *
     * @param template   文本模板，被替换的部分用 {data} 表示
     * @param map        参数值对
     * @param ignoreNull 是否忽略 {@code null} 值，忽略则 {@code null} 值对应的变量不被替换，否则替换为""
     * @return 格式化后的文本
     */
    public static String format(final CharSequence template, final Map<?, ?> map, final boolean ignoreNull) {
        return StringFormatter.format(template, map, ignoreNull);
    }

    /**
     * 检查给定的{@code String}是否包含实际的文本。 更具体地说，如果{@code String}不是{@code null}， 那么这个方法返回{@code true}，它的长度大于0，并且至少包含一个非空白字符
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
     * 构建新的字符串
     *
     * @param original     原始对象
     * @param middle       中间隐藏信息
     * @param prefixLength 前边信息长度
     * @return 构建后的新字符串
     */
    public static String build(final Object original, final String middle, final int prefixLength) {
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
     * 按{@link Character#toTitleCase(int)} 将第一个字符更改为标题大小写.其他字符没有改变
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
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen;) {
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
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen;) {
            final int codepoint = text.codePointAt(inOffset);
            newCodePoints[outOffset++] = codepoint;
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

}
