/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.text.StrBuilder;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理类
 * 用于MD5,加解密和字符串编码转换
 *
 * @author Kimi Liu
 * @version 6.0.1
 * @since JDK 1.8+
 */
public class StringKit {

    private static final int INDEX_NOT_FOUND = -1;
    private static final int PAD_LIMIT = 8192;

    /**
     * 字符串去空格
     *
     * @param str 原始字符串
     * @return 返回字符串
     */
    public static String trim(CharSequence str) {
        return (null == str) ? null : trim(str, 0);
    }

    /**
     * 分别去空格
     *
     * @param array 数组
     * @return 数组
     */
    public static final String[] trim(String[] array) {
        if (ArrayKit.isEmpty(array)) {
            return array;
        }
        String[] resultArray = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            String param = array[i];
            resultArray[i] = trim(param);
        }
        return resultArray;
    }

    /**
     * 除去字符串头尾部的空白符,如果字符串是null,依然返回null
     *
     * @param str  要处理的字符串
     * @param mode -1去除开始位置,0全部去除, 1去除结束位置
     * @return 除去指定字符后的的字符串, 如果原字串为null, 则返回null
     */
    public static String trim(CharSequence str, int mode) {
        if (str == null) {
            return null;
        }

        int length = str.length();
        int start = 0;
        int end = length;

        // 扫描字符串头部
        if (mode <= 0) {
            while ((start < end) && (CharKit.isBlankChar(str.charAt(start)))) {
                start++;
            }
        }

        // 扫描字符串尾部
        if (mode >= 0) {
            while ((start < end) && (CharKit.isBlankChar(str.charAt(end - 1)))) {
                end--;
            }
        }

        if ((start > 0) || (end < length)) {
            return str.toString().substring(start, end);
        }

        return str.toString();
    }

    /**
     * 删除字符串两端的控制字符(char &lt;= 32)，如果字符串在修剪后为空("")，
     * 或者如果字符串为{@code null}，则返回{@code null}.
     *
     * <pre>
     * StringKit.trimToNull(null)          = null
     * StringKit.trimToNull("")            = null
     * StringKit.trimToNull("     ")       = null
     * StringKit.trimToNull("abc")         = "abc"
     * StringKit.trimToNull("    abc    ") = "abc"
     * </pre>
     *
     * @param str 要被裁剪的字符串可能是空的
     * @return 如果只包含字符 &lt;= 32，则为空字符串或空字符串输入
     */
    public static String trimToNull(final String str) {
        final String ts = trim(str);
        return isEmpty(ts) ? null : ts;
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
     * @param str 要被裁剪的字符串可能是空的
     * @return 如果{@code null}输入，则为空字符串
     */
    public static String trimToEmpty(final String str) {
        return str == null ? Normal.EMPTY : str.trim();
    }

    /**
     * 字符串的每一个字符是否都与定义的匹配器匹配
     *
     * @param value   字符串
     * @param matcher 匹配器
     * @return 是否全部匹配
     */
    public static boolean isAllCharMatch(CharSequence value, org.aoju.bus.core.lang.Matcher<Character> matcher) {
        if (isBlank(value)) {
            return false;
        }
        int len = value.length();
        boolean isAllMatch = true;
        for (int i = 0; i < len; i++) {
            isAllMatch &= matcher.match(value.charAt(i));
        }
        return isAllMatch;
    }

    /**
     * 字符串是否为空白 空白的定义如下：
     * 1、为null
     * 2、为不可见字符(如空格)
     * 3、""
     *
     * @param str 被检测的字符串
     * @return 是否为空
     */
    public static boolean isBlank(CharSequence str) {
        int length;

        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            // 只要有一个非空字符即为非空字符串
            if (false == CharKit.isBlankChar(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 如果对象是字符串是否为空白，空白的定义如下：
     * 1、为null
     * 2、为不可见字符(如空格)
     * 3、""
     *
     * @param obj 对象
     * @return 如果为字符串是否为空串
     */
    public static boolean isBlank(Object obj) {
        if (null == obj) {
            return true;
        } else if (obj instanceof CharSequence) {
            return isBlank((CharSequence) obj);
        }
        return false;
    }

    /**
     * 字符串是否为非空白
     * 定义如下：
     * 1、不为null
     * 2、不为不可见字符(如空格)
     * 3、不为""
     *
     * @param str 被检测的字符串
     * @return 是否为非空
     */
    public static boolean isNotBlank(CharSequence str) {
        return false == isBlank(str);
    }

    /**
     * 是否存都不为{@code null}或空对象或空白符的对象
     * 通过{@link StringKit#hasBlank(CharSequence...)} 判断元素
     *
     * @param args 被检查的对象,一个或者多个
     * @return 是否都不为空
     */
    public static boolean isAllNotBlank(CharSequence... args) {
        return false == hasBlank(args);
    }

    /**
     * 是否包含空字符串
     *
     * @param strs 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasBlank(CharSequence... strs) {
        if (ArrayKit.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isBlank(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 给定所有字符串是否为空白
     *
     * @param strs 字符串
     * @return 所有字符串是否为空白
     */
    public static boolean isAllBlank(CharSequence... strs) {
        if (ArrayKit.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isNotBlank(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查任何一个字符序列是否为空("")，或为空，或仅为空白
     *
     * <pre>
     * StringKit.isAnyBlank((String) null)    = true
     * StringKit.isAnyBlank((String[]) null)  = false
     * StringKit.isAnyBlank(null, "foo")      = true
     * StringKit.isAnyBlank(null, null)       = true
     * StringKit.isAnyBlank("", "bar")        = true
     * StringKit.isAnyBlank("bob", "")        = true
     * StringKit.isAnyBlank("  bob  ", null)  = true
     * StringKit.isAnyBlank(" ", "bar")       = true
     * StringKit.isAnyBlank(new String[] {})  = false
     * StringKit.isAnyBlank(new String[]{""}) = true
     * StringKit.isAnyBlank("foo", "bar")     = false
     * </pre>
     *
     * @param css 要检查的字符序列可以为空或空
     * @return 如果任何一个字符序列是空的，或者是空的，或者只有空白
     */
    public static boolean isAnyBlank(final CharSequence... css) {
        if (ArrayKit.isEmpty(css)) {
            return false;
        }
        for (final CharSequence cs : css) {
            if (isBlank(cs)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 字符串是否为空，空的定义如下:
     * 1、为null
     * 2、为""
     *
     * @param str 被检测的字符串
     * @return 是否为空 true/false
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * 是否全部为空字符串
     *
     * @param str 字符串列表
     * @return 是否全部为空字符串
     */
    public static boolean isAllEmpty(CharSequence... str) {
        if (ArrayKit.isEmpty(str)) {
            return true;
        }

        for (CharSequence val : str) {
            if (isNotEmpty(val)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否存都不为{@code null}或空对象
     * 通过{@link StringKit#hasEmpty(CharSequence...)} 判断元素
     *
     * @param args 被检查的对象,一个或者多个
     * @return 是否都不为空
     */
    public static boolean isAllNotEmpty(CharSequence... args) {
        return false == hasEmpty(args);
    }

    /**
     * 是否包含空字符串
     *
     * @param strs 字符串列表
     * @return 是否包含空字符串
     */
    public static boolean hasEmpty(CharSequence... strs) {
        if (ArrayKit.isEmpty(strs)) {
            return true;
        }

        for (CharSequence str : strs) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 字符串是否为非空白 空白的定义如下:
     * 1、不为null
     * 2、不为""
     *
     * @param str 被检测的字符串
     * @return 是否为非空 true/false
     */
    public static boolean isNotEmpty(CharSequence str) {
        return false == isEmpty(str);
    }

    /**
     * 当给定字符串为null时，转换为Empty
     *
     * @param str 被检查的字符串
     * @return 原字符串或者空串
     * @see #nullToEmpty(CharSequence)
     */
    public static String emptyIfNull(CharSequence str) {
        return nullToEmpty(str);
    }

    /**
     * 如果对象是字符串是否为空串空的定义如下:
     * 1、为null
     * 2、为""
     *
     * @param obj 对象
     * @return 如果为字符串是否为空串
     */
    public static boolean emptyIfStr(Object obj) {
        if (null == obj) {
            return true;
        } else if (obj instanceof CharSequence) {
            return 0 == ((CharSequence) obj).length();
        }
        return false;
    }

    /**
     * 当给定字符串为null时，转换为Empty
     *
     * @param str 被转换的字符串
     * @return 转换后的字符串
     */
    public static String nullToEmpty(CharSequence str) {
        return nullToDefault(str, Normal.EMPTY);
    }

    /**
     * 如果字符串是<code>null</code>，则返回指定默认字符串，否则返回字符串本身。
     *
     * <pre>
     * nullToDefault(null, &quot;default&quot;)  = &quot;default&quot;
     * nullToDefault(&quot;&quot;, &quot;default&quot;)    = &quot;&quot;
     * nullToDefault(&quot;  &quot;, &quot;default&quot;)  = &quot;  &quot;
     * nullToDefault(&quot;bat&quot;, &quot;default&quot;) = &quot;bat&quot;
     * </pre>
     *
     * @param str        要转换的字符串
     * @param defaultStr 默认字符串
     * @return 字符串本身或指定的默认字符串
     */
    public static String nullToDefault(CharSequence str, String defaultStr) {
        return (str == null) ? defaultStr : str.toString();
    }

    /**
     * 如果字符串是<code>null</code>或者&quot;&quot;，则返回指定默认字符串，否则返回字符串本身。
     *
     * <pre>
     * emptyToDefault(null, &quot;default&quot;)  = &quot;default&quot;
     * emptyToDefault(&quot;&quot;, &quot;default&quot;)    = &quot;default&quot;
     * emptyToDefault(&quot;  &quot;, &quot;default&quot;)  = &quot;  &quot;
     * emptyToDefault(&quot;bat&quot;, &quot;default&quot;) = &quot;bat&quot;
     * </pre>
     *
     * @param str        要转换的字符串
     * @param defaultStr 默认字符串
     * @return 字符串本身或指定的默认字符串
     */
    public static String emptyToDefault(CharSequence str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str.toString();
    }

    /**
     * 如果字符串是<code>null</code>或者&quot;&quot;或者空白，则返回指定默认字符串，否则返回字符串本身。
     *
     * <pre>
     * emptyToDefault(null, &quot;default&quot;)  = &quot;default&quot;
     * emptyToDefault(&quot;&quot;, &quot;default&quot;)    = &quot;default&quot;
     * emptyToDefault(&quot;  &quot;, &quot;default&quot;)  = &quot;default&quot;
     * emptyToDefault(&quot;bat&quot;, &quot;default&quot;) = &quot;bat&quot;
     * </pre>
     *
     * @param str        要转换的字符串
     * @param defaultStr 默认字符串
     * @return 字符串本身或指定的默认字符串
     */
    public static String blankToDefault(CharSequence str, String defaultStr) {
        return isBlank(str) ? defaultStr : str.toString();
    }

    /**
     * 当给定字符串为空字符串时，转换为<code>null</code>
     *
     * @param str 被转换的字符串
     * @return 转换后的字符串
     */
    public static String emptyToNull(CharSequence str) {
        return isEmpty(str) ? null : str.toString();
    }

    /**
     * 检查字符串是否为null、“null”、“undefined”
     *
     * @param str 被检查的字符串
     * @return 是否为null、“null”、“undefined”
     */
    public static boolean isNullOrUndefined(CharSequence str) {
        if (null == str) {
            return true;
        }
        return isNullOrUndefinedStr(str);
    }

    /**
     * 检查字符串是否为null、“”、“null”、“undefined”
     *
     * @param str 被检查的字符串
     * @return 是否为null、“”、“null”、“undefined”
     */
    public static boolean isEmptyOrUndefined(CharSequence str) {
        if (isEmpty(str)) {
            return true;
        }
        return isNullOrUndefinedStr(str);
    }

    /**
     * 检查字符串是否为null、空白串、“null”、“undefined”
     *
     * @param str 被检查的字符串
     * @return 是否为null、空白串、“null”、“undefined”
     */
    public static boolean isBlankOrUndefined(CharSequence str) {
        if (isBlank(str)) {
            return true;
        }
        return isNullOrUndefinedStr(str);
    }

    /**
     * 是否为“null”、“undefined”，不做空指针检查
     *
     * @param str 字符串
     * @return 是否为“null”、“undefined”
     */
    private static boolean isNullOrUndefinedStr(CharSequence str) {
        String strString = str.toString().trim();
        return Normal.NULL.equals(strString) || Normal.UNDEFINED.equals(strString);
    }

    public static boolean areNotEmpty(String... values) {
        boolean result = true;
        if (values != null && values.length != 0) {
            String[] var2 = values;
            int var3 = values.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                String value = var2[var4];
                result &= !isEmpty(value);
            }
        } else {
            result = false;
        }

        return result;
    }

    /**
     * 截取两个字符串的不同部分(长度一致),判断截取的子串是否相同
     * 任意一个字符串为null返回false
     *
     * @param str1       第一个字符串
     * @param start1     第一个字符串开始的位置
     * @param str2       第二个字符串
     * @param start2     第二个字符串开始的位置
     * @param length     截取长度
     * @param ignoreCase 是否忽略大小写
     * @return 子串是否相同
     */
    public static boolean isSubEquals(CharSequence str1, int start1, CharSequence str2, int start2, int length, boolean ignoreCase) {
        if (null == str1 || null == str2) {
            return false;
        }

        return str1.toString().regionMatches(ignoreCase, start1, str2.toString(), start2, length);
    }

    /**
     * 将对象转为字符串
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组
     * 2、对象数组会调用Arrays.toString方法
     *
     * @param obj 对象
     * @return 字符串
     */
    public static String toString(Object obj) {
        return toString(obj, Charset.UTF_8);
    }

    /**
     * {@link CharSequence} 转为字符串,null安全
     *
     * @param cs {@link CharSequence}
     * @return 字符串
     */
    public static String toString(CharSequence cs) {
        return null == cs ? null : cs.toString();
    }

    /**
     * 将对象转为字符串
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组
     * 2、对象数组会调用Arrays.toString方法
     *
     * @param obj     对象
     * @param charset 字符集
     * @return 字符串
     */
    public static String toString(Object obj, String charset) {
        return toString(obj, java.nio.charset.Charset.forName(charset));
    }

    /**
     * 返回传入的字符串，如果字符串是{@code null}，则返回{@code defaultStr}的值
     * <pre>
     * StringKit.toString(null, "NULL")  = "NULL"
     * StringKit.toString("", "NULL")    = ""
     * StringKit.toString("bat", "NULL") = "bat"
     * </pre>
     *
     * @param str        要检查的字符串可能为空
     * @param defaultStr 如果输入是{@code null}，返回的默认字符串可能是null
     * @return 传入的字符串，如果是{@code null}，则为默认值
     * @see String#valueOf(Object)
     */
    public static String toString(final String str, final String defaultStr) {
        return str == null ? defaultStr : str;
    }

    /**
     * 将对象转为字符串
     * 1、Byte数组和ByteBuffer会被转换为对应字符串的数组 2、对象数组会调用Arrays.toString方法
     *
     * @param obj     对象
     * @param charset 字符集
     * @return 字符串
     */
    public static String toString(Object obj, java.nio.charset.Charset charset) {
        if (ObjectKit.isEmpty(obj)) {
            return null;
        }

        if (obj instanceof String) {
            return (String) obj;
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
     * 将byte数组转为字符串
     *
     * @param bytes   byte数组
     * @param charset 字符集
     * @return 字符串
     */
    public static String toString(byte[] bytes, String charset) {
        return toString(bytes, isBlank(charset) ? java.nio.charset.Charset.defaultCharset() : java.nio.charset.Charset.forName(charset));
    }

    /**
     * 解码字节码
     *
     * @param data    字符串
     * @param charset 字符集,如果此字段为空,则解码的结果取决于平台
     * @return 解码后的字符串
     */
    public static String toString(byte[] data, java.nio.charset.Charset charset) {
        if (data == null) {
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
        return toString(bytes, isBlank(charset) ? java.nio.charset.Charset.defaultCharset() : java.nio.charset.Charset.forName(charset));
    }

    /**
     * 解码字节码
     *
     * @param data    字符串
     * @param charset 字符集,如果此字段为空,则解码的结果取决于平台
     * @return 解码后的字符串
     */
    public static String toString(Byte[] data, java.nio.charset.Charset charset) {
        if (data == null) {
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
        if (data == null) {
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
            builder.append(Symbol.COMMA + clazz.getCanonicalName());
        }

        String parameter = builder.toString().trim();
        if (parameter.length() > 0) {
            return parameter.substring(1);
        }

        return Normal.EMPTY;
    }

    /**
     * 将给定的{@code Collection}复制到{@code String}数组中
     * {@code Collection }必须只包含{@code String}元素
     *
     * @param collection 要复制的集合 {@code Collection}
     * @return {@code String} 数组
     */
    public static String[] toStringArray(Collection<String> collection) {
        return collection.toArray(Normal.EMPTY_STRING_ARRAY);
    }

    /**
     * 将给定的枚举复制到{@code String}数组中
     * 枚举必须只包含{@code String}元素
     *
     * @param enumeration 要复制的枚举 {@code Enumeration}
     * @return {@code String} 数组
     */
    public static String[] toStringArray(Enumeration<String> enumeration) {
        return toStringArray(Collections.list(enumeration));
    }

    /**
     * 检查给定的{@code String}是否包含实际的文本。
     * 更具体地说，如果{@code String}不是{@code null}，
     * 那么这个方法返回{@code true}，它的长度大于0，并且至少包含一个非空白字符
     *
     * @param str 要检查的{@code String}(可能是{@code null})
     * @return 如果{@code String}不是{@code null}，那么它的长度大于0，并且不包含空格
     */
    public static boolean hasText(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查给定的{@code String}既不是{@code null}也不是长度为0.
     *
     * @param str 要检查的{@code String}(可能是{@code null})
     * @return 如果{@code String}不是{@code null}，并且有长度，则为{@code true}
     * @see #hasText(String)
     */
    public static boolean hasLength(String str) {
        return (str != null && !str.isEmpty());
    }

    /**
     * 将base64字符串处理成String字节
     *
     * @param str base64的字符串
     * @return 原字节数据
     */
    public static byte[] base64ToByte(String str) {
        try {
            if (str == null) {
                return null;
            }
            return Base64.getDecoder().decode(str);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将base64字符串处理成String
     * (用默认的String编码集)
     *
     * @param str base64的字符串
     * @return 可显示的字符串
     */
    public static String base64ToString(String str) {
        try {
            if (str == null) {
                return null;
            }
            return new String(base64ToByte(str), Charset.UTF_8);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将base64字符串处理成String
     * (用默认的String编码集)
     *
     * @param str     base64的字符串
     * @param charset 编码格式(UTF-8/GBK)
     * @return 可显示的字符串
     */
    public static String base64ToString(String str, String charset) {
        try {
            if (str == null) {
                return null;
            }
            return new String(base64ToByte(str), charset);
        } catch (Exception e) {
            throw new InstrumentException(e);
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
     * @param hex 字符串
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hex) {
        if (isEmpty(hex)) {
            return Normal.EMPTY_BYTE_ARRAY;
        }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length() / 2; i++) {
            String subStr = hex.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

    /**
     * 将字符串转换为unicode编码
     *
     * @param input 要转换的字符串(主要是包含中文的字符串)
     * @return 转换后的unicode编码
     */
    public static String toUnicode(String input) {
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            // 取出每一个字符
            char c = input.charAt(i);
            String hexStr = Integer.toHexString(c);
            while (hexStr.length() < 4) {
                hexStr = Symbol.ZERO + hexStr;
            }
            // 转换为unicode
            unicode.append("\\u" + hexStr);
        }
        return unicode.toString();
    }

    /**
     * 字符串编码为Unicode形式
     *
     * @param str         被编码的字符串
     * @param isSkipAscii 是否跳过ASCII字符(只跳过可见字符)
     * @return Unicode字符串
     */
    public static String toUnicode(String str, boolean isSkipAscii) {
        if (StringKit.isEmpty(str)) {
            return str;
        }

        final int len = str.length();
        final TextKit unicode = TextKit.create(str.length() * 6);
        char c;
        for (int i = 0; i < len; i++) {
            c = str.charAt(i);
            if (isSkipAscii && CharKit.isAsciiPrintable(c)) {
                unicode.append(c);
            } else {
                unicode.append(HexKit.toUnicodeHex(c));
            }
        }
        return unicode.toString();
    }

    /**
     * 将unicode编码还原为字符串
     *
     * @param input unicode编码的字符串
     * @return 原始字符串
     */
    public static String toUnicodeString(String input) {
        StringBuilder string = new StringBuilder();
        String[] hex = input.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            // 转换出每一个代码点
            int data = Integer.parseInt(hex[i], 16);
            // 追加成string
            string.append((char) data);
        }
        return string.toString();
    }


    /**
     * Unicode字符串转为普通字符串
     * Unicode字符串的表现方式为：\\uXXXX
     *
     * @param unicode     Unicode字符串
     * @param isSkipAscii 是跳过Ascii
     * @return 普通字符串
     */
    public static String toUnicodeString(String unicode, boolean isSkipAscii) {
        if (StringKit.isBlank(unicode)) {
            return unicode;
        }

        if (isSkipAscii) {
            final int len = unicode.length();
            final TextKit sb = TextKit.create(len);
            int i = -1;
            int pos = 0;
            while ((i = indexOfIgnoreCase(unicode, "\\u", pos)) != -1) {
                sb.append(unicode, pos, i);//写入Unicode符之前的部分
                pos = i;
                if (i + 5 < len) {
                    char c = 0;
                    try {
                        c = (char) Integer.parseInt(unicode.substring(i + 2, i + 6), 16);
                        sb.append(c);
                        pos = i + 6;//跳过整个Unicode符
                    } catch (NumberFormatException e) {
                        //非法Unicode符,跳过
                        sb.append(unicode, pos, i + 2);//写入"\\u"
                        pos = i + 2;
                    }
                } else {
                    pos = i;//非Unicode符,结束
                    break;
                }
            }

            if (pos < len) {
                sb.append(unicode, pos, len);
            }
            return sb.toString();
        }
        return toUnicodeString(unicode);
    }

    /**
     * 比较两个字符串(大小写敏感)
     *
     * <pre>
     * equals(null, null)   = true
     * equals(null, &quot;abc&quot;)  = false
     * equals(&quot;abc&quot;, null)  = false
     * equals(&quot;abc&quot;, &quot;abc&quot;) = true
     * equals(&quot;abc&quot;, &quot;ABC&quot;) = false
     * </pre>
     *
     * @param stra 要比较的字符串
     * @param strb 要比较的字符串
     * @return 如果两个字符串相同，或者都是<code>null</code>，则返回<code>true</code>
     */
    public static boolean equals(CharSequence stra, CharSequence strb) {
        return equals(stra, strb, false);
    }

    /**
     * 比较两个字符串是否相等
     *
     * @param stra       要比较的字符串
     * @param strb       要比较的字符串
     * @param ignoreCase 是否忽略大小写
     * @return 如果两个字符串相同，或者都是<code>null</code>，则返回<code>true</code>
     */
    public static boolean equals(CharSequence stra, CharSequence strb, boolean ignoreCase) {
        if (null == stra) {
            // 只有两个都为null才判断相等
            return strb == null;
        }
        if (null == strb) {
            // 字符串2空，字符串1非空，直接false
            return false;
        }

        if (ignoreCase) {
            return stra.toString().equalsIgnoreCase(strb.toString());
        } else {
            return stra.equals(strb);
        }
    }

    /**
     * 比较两个字符串(大小写不敏感)
     *
     * <pre>
     * equalsIgnoreCase(null, null)   = true
     * equalsIgnoreCase(null, &quot;abc&quot;)  = false
     * equalsIgnoreCase(&quot;abc&quot;, null)  = false
     * equalsIgnoreCase(&quot;abc&quot;, &quot;abc&quot;) = true
     * equalsIgnoreCase(&quot;abc&quot;, &quot;ABC&quot;) = true
     * </pre>
     *
     * @param stra 要比较的字符串
     * @param strb 要比较的字符串
     * @return 如果两个字符串相同，或者都是<code>null</code>，则返回<code>true</code>
     */
    public static boolean equalsIgnoreCase(CharSequence stra, CharSequence strb) {
        return equals(stra, strb, true);
    }

    /**
     * 给定字符串是否与提供的中任一字符串相同(忽略大小写)，相同则返回{@code true}，没有相同的返回{@code false}
     * 如果参与比对的字符串列表为空，返回{@code false}
     *
     * @param stra 给定需要检查的字符串
     * @param strb 需要参与比对的字符串列表
     * @return 是否相同
     */
    public static boolean equalsAnyIgnoreCase(CharSequence stra, CharSequence... strb) {
        return equalsAny(stra, true, strb);
    }

    /**
     * 给定字符串是否与提供的中任一字符串相同，相同则返回{@code true}，没有相同的返回{@code false}
     * 如果参与比对的字符串列表为空，返回{@code false}
     *
     * @param str1 给定需要检查的字符串
     * @param strs 需要参与比对的字符串列表
     * @return 是否相同
     */
    public static boolean equalsAny(CharSequence str1, CharSequence... strs) {
        return equalsAny(str1, false, strs);
    }

    /**
     * 给定字符串是否与提供的中任一字符串相同，相同则返回{@code true}，没有相同的返回{@code false}
     * 如果参与比对的字符串列表为空，返回{@code false}
     *
     * @param str1       给定需要检查的字符串
     * @param ignoreCase 是否忽略大小写
     * @param strs       需要参与比对的字符串列表
     * @return 是否相同
     */
    public static boolean equalsAny(CharSequence str1, boolean ignoreCase, CharSequence... strs) {
        if (ArrayKit.isEmpty(strs)) {
            return false;
        }

        for (CharSequence str : strs) {
            if (equals(str1, str, ignoreCase)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 格式化文本, {} 表示占位符
     * 此方法只是简单将占位符 {} 按照顺序替换为参数
     * 如果想输出 {} 使用 \\转义 { 即可，如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可
     * 例：
     * 通常使用：format("this is {} for {}", "a", "b") =》 this is a for b
     * 转义{}： format("this is \\{} for {}", "a", "b") =》 this is \{} for a
     * 转义\： format("this is \\\\{} for {}", "a", "b") =》 this is \a for b
     *
     * @param template 文本模板，被替换的部分用 {} 表示
     * @param params   参数值
     * @return 格式化后的文本
     */
    public static String format(CharSequence template, Object... params) {
        if (null == template) {
            return null;
        }
        if (ArrayKit.isEmpty(params) || isBlank(template)) {
            return template.toString();
        }
        return format(template.toString(), params);
    }

    /**
     * 格式化字符串
     * 此方法只是简单将占位符 {} 按照顺序替换为参数
     * 如果想输出 {} 使用 \\转义 { 即可,如果想输出 {} 之前的 \ 使用双转义符 \\\\ 即可
     * 例：
     * 通常使用：format("this is {} for {}", "a", "b") =》 this is a for b
     * 转义{}： format("this is \\{} for {}", "a", "b") =》 this is \{} for a
     * 转义\：format("this is \\\\{} for {}", "a", "b") =》 this is \a for b
     *
     * @param val      字符串模板
     * @param argArray 参数列表
     * @return 结果
     */
    public static String format(final String val, final Object... argArray) {
        if (isBlank(val) || ArrayKit.isEmpty(argArray)) {
            return val;
        }
        final int strPatternLength = val.length();

        //初始化定义好的长度以获得更好的性能
        StringBuilder sbuf = new StringBuilder(strPatternLength + 50);

        int handledPosition = 0;//记录已经处理到的位置
        int delimIndex;//占位符所在位置
        for (int argIndex = 0; argIndex < argArray.length; argIndex++) {
            delimIndex = val.indexOf(Symbol.DELIM, handledPosition);
            if (delimIndex == -1) {//剩余部分无占位符
                if (handledPosition == 0) { //不带占位符的模板直接返回
                    return val;
                } else { //字符串模板剩余部分不再包含占位符,加入剩余部分后返回结果
                    sbuf.append(val, handledPosition, strPatternLength);
                    return sbuf.toString();
                }
            } else {
                if (delimIndex > 0 && val.charAt(delimIndex - 1) == Symbol.C_BACKSLASH) {//转义符
                    if (delimIndex > 1 && val.charAt(delimIndex - 2) == Symbol.C_BACKSLASH) {//双转义符
                        //转义符之前还有一个转义符,占位符依旧有效
                        sbuf.append(val, handledPosition, delimIndex - 1);
                        sbuf.append(toString(argArray[argIndex]));
                        handledPosition = delimIndex + 2;
                    } else {
                        //占位符被转义
                        argIndex--;
                        sbuf.append(val, handledPosition, delimIndex - 1);
                        sbuf.append(Symbol.C_BRACE_LEFT);
                        handledPosition = delimIndex + 1;
                    }
                } else {//正常占位符
                    sbuf.append(val, handledPosition, delimIndex);
                    sbuf.append(toString(argArray[argIndex]));
                    handledPosition = delimIndex + 2;
                }
            }
        }
        // append the characters following the last {} pair.
        //加入最后一个占位符后所有的字符
        sbuf.append(val, handledPosition, val.length());

        return sbuf.toString();
    }

    /**
     * 改进JDK subString
     * index从0开始计算,最后一个字符为-1
     * 如果from和to位置一样,返回 ""
     * 如果from或to为负数,则按照length从后向前数位置,如果绝对值大于字符串长度,则from归到0,to归到length
     * 如果经过修正的index中from大于to,则互换
     * abcdefgh 2 3 =》 c
     * abcdefgh 2 -3 =》 cde
     *
     * @param str       String
     * @param fromIndex 开始的index(包括)
     * @param toIndex   结束的index(不包括)
     * @return 字串
     */
    public static String sub(CharSequence str, int fromIndex, int toIndex) {
        if (isEmpty(str)) {
            return toString(str);
        }
        int len = str.length();

        if (fromIndex < 0) {
            fromIndex = len + fromIndex;
            if (fromIndex < 0) {
                fromIndex = 0;
            }
        } else if (fromIndex > len) {
            fromIndex = len;
        }

        if (toIndex < 0) {
            toIndex = len + toIndex;
            if (toIndex < 0) {
                toIndex = len;
            }
        } else if (toIndex > len) {
            toIndex = len;
        }

        if (toIndex < fromIndex) {
            int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }

        if (fromIndex == toIndex) {
            return Normal.EMPTY;
        }

        return str.toString().substring(fromIndex, toIndex);
    }

    /**
     * 切割指定位置之前部分的字符串
     *
     * @param string  字符串
     * @param toIndex 切割到的位置(不包括)
     * @return 切割后的剩余的前半部分字符串
     */
    public static String subPre(CharSequence string, int toIndex) {
        return sub(string, 0, toIndex);
    }

    /**
     * 切割指定位置之后部分的字符串
     *
     * @param string    字符串
     * @param fromIndex 切割开始的位置(包括)
     * @return 切割后后剩余的后半部分字符串
     */
    public static String subSuf(CharSequence string, int fromIndex) {
        if (isEmpty(string)) {
            return null;
        }
        return sub(string, fromIndex, string.length());
    }

    /**
     * 获取分隔符最后一次出现之前的子字符串
     *
     * @param str       要从中获取子字符串的字符串可以为空
     * @param separator 要搜索的字符串可能为空
     * @return 分隔符最后一次出现之前的子字符串
     */
    public static String subBeforeLast(final String str, final String separator) {
        if (isEmpty(str) || isEmpty(separator)) {
            return str;
        }
        final int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * 获取分隔符最后一次出现后的子字符串
     *
     * @param str       要从中获取子字符串的字符串可以为空
     * @param separator 要搜索的字符串可能为空
     * @return 分隔符最后一次出现后的子字符串
     */
    public static String subAfterLast(final String str, final String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(separator)) {
            return Normal.EMPTY;
        }
        final int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND || pos == str.length() - separator.length()) {
            return Normal.EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    /**
     * 通过CodePoint截取字符串，可以截断Emoji
     *
     * @param str       string
     * @param fromIndex 开始的index(包括)
     * @param toIndex   结束的index(不包括)
     * @return 字串
     */
    public static String subCodePoint(CharSequence str, int fromIndex, int toIndex) {
        if (isEmpty(str)) {
            return toString(str);
        }

        if (fromIndex < 0 || fromIndex > toIndex) {
            throw new IllegalArgumentException();
        }

        if (fromIndex == toIndex) {
            return Normal.EMPTY;
        }

        final StringBuilder sb = new StringBuilder();
        final int subLen = toIndex - fromIndex;
        str.toString().codePoints().skip(fromIndex).limit(subLen).forEach(v -> sb.append(Character.toChars(v)));
        return sb.toString();
    }


    /**
     * 截取分隔字符串之前的字符串,不包括分隔字符串
     * 如果给定的字符串为空串(null或"")或者分隔字符串为null,返回原字符串
     * 如果分隔字符串为空串"",则返回空串,如果分隔字符串未找到,返回原字符串,举例如下：
     *
     * <pre>
     * StringKit.subBefore(null, *)      = null
     * StringKit.subBefore("", *)        = ""
     * StringKit.subBefore("abc", "a")   = ""
     * StringKit.subBefore("abcba", "b") = "a"
     * StringKit.subBefore("abc", "c")   = "ab"
     * StringKit.subBefore("abc", "d")   = "abc"
     * StringKit.subBefore("abc", "")    = ""
     * StringKit.subBefore("abc", null)  = "abc"
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串(不包括)
     * @param isLastSeparator 是否查找最后一个分隔字符串(多次出现分隔字符串时选取最后一个),true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subBefore(CharSequence string, CharSequence separator, boolean isLastSeparator) {
        if (isEmpty(string) || separator == null) {
            return null == string ? null : string.toString();
        }

        final String str = string.toString();
        final String sep = separator.toString();
        if (sep.isEmpty()) {
            return Normal.EMPTY;
        }
        final int pos = isLastSeparator ? str.lastIndexOf(sep) : str.indexOf(sep);
        if (INDEX_NOT_FOUND == pos) {
            return str;
        }
        if (0 == pos) {
            return Normal.EMPTY;
        }
        return str.substring(0, pos);
    }

    /**
     * 截取分隔字符串之前的字符串,不包括分隔字符串
     * 如果给定的字符串为空串(null或"")或者分隔字符串为null,返回原字符串
     * 如果分隔字符串未找到,返回原字符串,举例如下：
     *
     * <pre>
     * StringKit.subBefore(null, *)      = null
     * StringKit.subBefore("", *)        = ""
     * StringKit.subBefore("abc", 'a')   = ""
     * StringKit.subBefore("abcba", 'b') = "a"
     * StringKit.subBefore("abc", 'c')   = "ab"
     * StringKit.subBefore("abc", 'd')   = "abc"
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串(不包括)
     * @param isLastSeparator 是否查找最后一个分隔字符串(多次出现分隔字符串时选取最后一个),true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subBefore(CharSequence string, char separator, boolean isLastSeparator) {
        if (isEmpty(string)) {
            return null == string ? null : string.toString();
        }

        final String str = string.toString();
        final int pos = isLastSeparator ? str.lastIndexOf(separator) : str.indexOf(separator);
        if (INDEX_NOT_FOUND == pos) {
            return str;
        }
        if (0 == pos) {
            return Normal.EMPTY;
        }
        return str.substring(0, pos);
    }

    /**
     * 截取分隔字符串之后的字符串,不包括分隔字符串
     * 如果给定的字符串为空串(null或""),返回原字符串
     * 如果分隔字符串为空串(null或""),则返回空串,如果分隔字符串未找到,返回空串,举例如下：
     *
     * <pre>
     * StringKit.subAfter(null, *)      = null
     * StringKit.subAfter("", *)        = ""
     * StringKit.subAfter(*, null)      = ""
     * StringKit.subAfter("abc", "a")   = "bc"
     * StringKit.subAfter("abcba", "b") = "cba"
     * StringKit.subAfter("abc", "c")   = ""
     * StringKit.subAfter("abc", "d")   = ""
     * StringKit.subAfter("abc", "")    = "abc"
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串(不包括)
     * @param isLastSeparator 是否查找最后一个分隔字符串(多次出现分隔字符串时选取最后一个),true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subAfter(CharSequence string, CharSequence separator, boolean isLastSeparator) {
        if (isEmpty(string)) {
            return null == string ? null : string.toString();
        }
        if (separator == null) {
            return Normal.EMPTY;
        }
        final String str = string.toString();
        final String sep = separator.toString();
        final int pos = isLastSeparator ? str.lastIndexOf(sep) : str.indexOf(sep);
        if (INDEX_NOT_FOUND == pos || (string.length() - 1) == pos) {
            return Normal.EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    /**
     * 截取分隔字符串之后的字符串,不包括分隔字符串
     * 如果给定的字符串为空串(null或""),返回原字符串
     * 如果分隔字符串为空串(null或""),则返回空串,如果分隔字符串未找到,返回空串,举例如下：
     *
     * <pre>
     * StringKit.subAfter(null, *)      = null
     * StringKit.subAfter("", *)        = ""
     * StringKit.subAfter("abc", 'a')   = "bc"
     * StringKit.subAfter("abcba", 'b') = "cba"
     * StringKit.subAfter("abc", 'c')   = ""
     * StringKit.subAfter("abc", 'd')   = ""
     * </pre>
     *
     * @param string          被查找的字符串
     * @param separator       分隔字符串(不包括)
     * @param isLastSeparator 是否查找最后一个分隔字符串(多次出现分隔字符串时选取最后一个),true为选取最后一个
     * @return 切割后的字符串
     */
    public static String subAfter(CharSequence string, char separator, boolean isLastSeparator) {
        if (isEmpty(string)) {
            return null == string ? null : string.toString();
        }
        final String str = string.toString();
        final int pos = isLastSeparator ? str.lastIndexOf(separator) : str.indexOf(separator);
        if (INDEX_NOT_FOUND == pos) {
            return Normal.EMPTY;
        }
        return str.substring(pos + 1);
    }

    /**
     * 截取指定字符串中间部分,不包括标识字符串
     *
     * @param str    被切割的字符串
     * @param before 截取开始的字符串标识
     * @param after  截取到的字符串标识
     * @return 截取后的字符串
     */
    public static String subBetween(CharSequence str, CharSequence before, CharSequence after) {
        return subBetween(str.toString(), before.toString(), after.toString());
    }

    /**
     * 截取指定字符串中间部分,不包括标识字符串
     *
     * <pre>
     * StringKit.subBetween("wx[b]yz", "[", "]")     = "b"
     * StringKit.subBetween(null, *, *)              = null
     * StringKit.subBetween(*, null, *)              = null
     * StringKit.subBetween(*, *, null)              = null
     * StringKit.subBetween("", "", "")              = ""
     * StringKit.subBetween("", "", "]")             = null
     * StringKit.subBetween("", "[", "]")            = null
     * StringKit.subBetween("yabcz", "", "")         = ""
     * StringKit.subBetween("yabcz", "y", "z")       = "abc"
     * StringKit.subBetween("yabczyabcz", "y", "z")  = "abc"
     * </pre>
     *
     * @param str    被切割的字符串
     * @param before 截取开始的字符串标识
     * @param after  截取到的字符串标识
     * @return 截取后的字符串
     */
    public static String subBetween(String str, String before, String after) {
        if (str == null || before == null || after == null) {
            return null;
        }
        int start = str.indexOf(before);
        if (start != INDEX_NOT_FOUND) {
            int end = str.indexOf(after, start + before.length());
            if (end != INDEX_NOT_FOUND) {
                return str.substring(start + before.length(), end);
            }
        }
        return null;
    }

    /**
     * 截取指定字符串中间部分,不包括标识字符串
     *
     * <pre>
     * StringKit.subBetween(null, *)            = null
     * StringKit.subBetween("", "")             = ""
     * StringKit.subBetween("", "tag")          = null
     * StringKit.subBetween("tagabctag", null)  = null
     * StringKit.subBetween("tagabctag", "")    = ""
     * StringKit.subBetween("tagabctag", "tag") = "abc"
     * </pre>
     *
     * @param str            被切割的字符串
     * @param beforeAndAfter 截取开始和结束的字符串标识
     * @return 截取后的字符串
     */
    public static String subBetween(CharSequence str, CharSequence beforeAndAfter) {
        return subBetween(str, beforeAndAfter, beforeAndAfter);
    }


    /**
     * 截取指定字符串多段中间部分，不包括标识字符串
     * <pre>
     * subBetweenAll("wx[b]y[z]", "[", "]") 		= ["b","z"]
     * subBetweenAll(null, *, *)          			= []
     * subBetweenAll(*, null, *)          			= []
     * subBetweenAll(*, *, null)          			= []
     * subBetweenAll("", "", "")          			= []
     * subBetweenAll("", "", "]")         			= []
     * subBetweenAll("", "[", "]")        			= []
     * subBetweenAll("yabcz", "", "")     			= []
     * subBetweenAll("yabcz", "y", "z")   			= ["abc"]
     * subBetweenAll("yabczyabcz", "y", "z")   		= ["abc","abc"]
     * subBetweenAll("[yabc[zy]abcz]", "[", "]");   = ["zy"]           重叠时只截取内部，
     * </pre>
     *
     * @param str    被切割的字符串
     * @param prefix 截取开始的字符串标识
     * @param suffix 截取到的字符串标识
     * @return 截取后的字符串
     */
    public static String[] subBetweenAll(CharSequence str, CharSequence prefix, CharSequence suffix) {
        if (hasEmpty(str, prefix, suffix) || false == contains(str, prefix)) {
            return new String[0];
        }

        final List<String> result = new LinkedList<>();
        for (String fragment : split(str, prefix)) {
            int suffixIndex = fragment.indexOf(suffix.toString());
            if (suffixIndex > 0) {
                result.add(fragment.substring(0, suffixIndex));
            }
        }

        return result.toArray(new String[0]);
    }

    /**
     * 切割指定长度的后部分的字符串
     *
     * <pre>
     * StringKit.subSufByLength("abcde", 3)      =    "cde"
     * StringKit.subSufByLength("abcde", 0)      =    ""
     * StringKit.subSufByLength("abcde", -5)     =    ""
     * StringKit.subSufByLength("abcde", -1)     =    ""
     * StringKit.subSufByLength("abcde", 5)      =    "abcde"
     * StringKit.subSufByLength("abcde", 10)     =    "abcde"
     * StringKit.subSufByLength(null, 3)         =     null
     * </pre>
     *
     * @param string 字符串
     * @param length 切割长度
     * @return 切割后后剩余的后半部分字符串
     */
    public static String subByLength(CharSequence string, int length) {
        if (isEmpty(string)) {
            return null;
        }
        if (length <= 0) {
            return Normal.EMPTY;
        }
        return sub(string, -length, string.length());
    }

    /**
     * 截取字符串,从指定位置开始,截取指定长度的字符串
     *
     * @param input     原始字符串
     * @param fromIndex 开始的index,包括
     * @param length    要截取的长度
     * @return 截取后的字符串
     */
    public static String subWithLength(String input, int fromIndex, int length) {
        return sub(input, fromIndex, fromIndex + length);
    }

    /**
     * 切分字符串路径,仅支持Unix分界符：/
     *
     * @param str 被切分的字符串
     * @return 切分后的集合
     */
    public static List<String> splitPath(String str) {
        return splitPath(str, 0);
    }

    /**
     * 切分字符串路径,仅支持Unix分界符：/
     *
     * @param str   被切分的字符串
     * @param limit 限制分片数
     * @return 切分后的集合
     */
    public static List<String> splitPath(String str, int limit) {
        return split(str, Symbol.C_SLASH, limit, true, true);
    }

    /**
     * 切分字符串路径,仅支持Unix分界符：/
     *
     * @param str 被切分的字符串
     * @return 切分后的集合
     */
    public static String[] splitPathToArray(String str) {
        return ArrayKit.toArray(splitPath(str));
    }

    /**
     * 切分字符串路径,仅支持Unix分界符：/
     *
     * @param str   被切分的字符串
     * @param limit 限制分片数
     * @return 切分后的集合
     */
    public static String[] splitPathToArray(String str, int limit) {
        return ArrayKit.toArray(splitPath(str, limit));
    }

    /**
     * 切分字符串,去除切分后每个元素两边的空白符,去除空白项
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     */
    public static List<String> splitTrim(CharSequence str, char separator) {
        return splitTrim(str.toString(), separator, true);
    }

    /**
     * 切分字符串，去除切分后每个元素两边的空白符，去除空白项
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     */
    public static List<String> splitTrim(CharSequence str, CharSequence separator) {
        return splitTrim(str, separator, -1);
    }

    /**
     * 切分字符串，去除切分后每个元素两边的空白符，去除空白项
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @param limit     限制分片数，-1不限制
     * @return 切分后的集合
     */
    public static List<String> splitTrim(CharSequence str, CharSequence separator, int limit) {
        return split(str, separator, limit, true, true);
    }

    /**
     * 切分字符串
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitTrim(String str, char separator, boolean ignoreEmpty) {
        return split(str, separator, 0, true, ignoreEmpty);
    }

    /**
     * 切分字符串,去除每个元素两边空格,忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitTrim(String str, String separator, boolean ignoreEmpty) {
        return split(str, separator, true, ignoreEmpty);
    }

    /**
     * 切分字符串,大小写敏感,去除每个元素两边空白符
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数,-1不限制
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitTrim(String str, char separator, int limit, boolean ignoreEmpty) {
        return split(str, separator, limit, true, ignoreEmpty, false);
    }

    /**
     * 切分字符串,去除每个元素两边空格,忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitTrim(String str, String separator, int limit, boolean ignoreEmpty) {
        return split(str, separator, limit, true, ignoreEmpty);
    }

    /**
     * 切分字符串,忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数,-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitIgnoreCase(String str, char separator, int limit, boolean isTrim,
                                               boolean ignoreEmpty) {
        return split(str, separator, limit, isTrim, ignoreEmpty, true);
    }

    /**
     * 切分字符串,忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitIgnoreCase(String str, String separator, int limit, boolean isTrim,
                                               boolean ignoreEmpty) {
        return split(str, separator, limit, isTrim, ignoreEmpty, true);
    }

    /**
     * 切分字符串,去除每个元素两边空格,忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitTrimIgnoreCase(String str, String separator, int limit, boolean ignoreEmpty) {
        return split(str, separator, limit, true, ignoreEmpty, true);
    }

    /**
     * 切分字符串为long数组
     *
     * @param str       被切分的字符串
     * @param separator 分隔符
     * @return 切分后long数组
     */
    public static long[] splitToLong(CharSequence str, char separator) {
        return Convert.convert(long[].class, splitTrim(str, separator));
    }

    /**
     * 切分字符串为long数组
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符串
     * @return 切分后long数组
     */
    public static long[] splitToLong(CharSequence str, CharSequence separator) {
        return Convert.convert(long[].class, splitTrim(str, separator));
    }

    /**
     * 切分字符串为int数组
     *
     * @param str       被切分的字符串
     * @param separator 分隔符
     * @return 切分后long数组
     */
    public static int[] splitToInt(CharSequence str, char separator) {
        return Convert.convert(int[].class, splitTrim(str, separator));
    }

    /**
     * 切分字符串为int数组
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符串
     * @return 切分后long数组
     */
    public static int[] splitToInt(CharSequence str, CharSequence separator) {
        return Convert.convert(int[].class, splitTrim(str, separator));
    }

    /**
     * 切分字符串
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @param limit     限制分片数
     * @return 切分后的集合
     */
    public static String[] splitToArray(CharSequence str, char separator, int limit) {
        if (null == str) {
            return new String[]{};
        }
        return splitToArray(str.toString(), separator, limit, false, false);
    }

    /**
     * 切分字符串为字符串数组
     *
     * @param str   被切分的字符串
     * @param limit 限制分片数
     * @return 切分后的集合
     */
    public static String[] splitToArray(String str, int limit) {
        return ArrayKit.toArray(split(str, limit));
    }

    /**
     * 切分字符串为字符串数组
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static String[] splitToArray(String str, char separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return ArrayKit.toArray(split(str, separator, limit, isTrim, ignoreEmpty));
    }

    /**
     * 切分字符串为字符串数组
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static String[] splitToArray(String str, String separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return ArrayKit.toArray(split(str, separator, limit, isTrim, ignoreEmpty));
    }

    /**
     * 通过正则切分字符串为字符串数组
     *
     * @param str              被切分的字符串
     * @param separatorPattern 分隔符正则{@link Pattern}
     * @param limit            限制分片数
     * @param isTrim           是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty      是否忽略空串
     * @return 切分后的集合
     */
    public static String[] splitToArray(String str, Pattern separatorPattern, int limit, boolean isTrim,
                                        boolean ignoreEmpty) {
        return ArrayKit.toArray(split(str, separatorPattern, limit, isTrim, ignoreEmpty));
    }

    /**
     * 通过正则切分字符串
     *
     * @param str            字符串
     * @param separatorRegex 分隔符正则
     * @param limit          限制分片数
     * @param isTrim         是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty    是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> splitByRegex(String str, String separatorRegex, int limit, boolean isTrim,
                                            boolean ignoreEmpty) {
        final Pattern pattern = PatternKit.get(separatorRegex);
        return split(str, pattern, limit, isTrim, ignoreEmpty);
    }

    /**
     * @param text 每字符串
     * @param len  每一个小节的长度
     * @return 截取后的字符串数组
     */
    public static String[] splitByLength(String text, int len) {
        int partCount = text.length() / len;
        int lastPartCount = text.length() % len;
        int fixPart = 0;
        if (lastPartCount != 0) {
            fixPart = 1;
        }

        final String[] strs = new String[partCount + fixPart];
        for (int i = 0; i < partCount + fixPart; i++) {
            if (i == partCount + fixPart - 1 && lastPartCount != 0) {
                strs[i] = text.substring(i * len, i * len + lastPartCount);
            } else {
                strs[i] = text.substring(i * len, i * len + len);
            }
        }
        return strs;
    }

    /**
     * 切分字符串
     *
     * @param str 被切分的字符串
     * @return 字符串
     */
    public static String split(String str) {
        return split(str, Symbol.COMMA, Symbol.COMMA);
    }

    /**
     * 切分字符串
     *
     * @param str       被切分的字符串
     * @param separator 分隔符
     * @return 字符串
     */
    public static String[] split(CharSequence str, CharSequence separator) {
        if (str == null) {
            return new String[]{};
        }

        final String separatorStr = (null == separator) ? null : separator.toString();
        return splitToArray(str.toString(), separatorStr, 0, false, false);
    }

    /**
     * 切分字符串
     *
     * @param str       被切分的字符串
     * @param separator 分隔符
     * @param reserve   替换后的分隔符
     * @return 字符串
     */
    public static String split(String str, CharSequence separator, CharSequence reserve) {
        StringBuffer sb = new StringBuffer();
        if (StringKit.isNotEmpty(str)) {
            String[] arr = split(str, separator);
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
     * 切分字符串,去除切分后每个元素两边的空白符,去除空白项
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     */
    public static List<String> split(String str, char separator) {
        return split(str, separator, -1);
    }

    /**
     * 切分字符串
     * a#b#c =》 [a,b,c]
     * a##b#c =》 [a,"",b,c]
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence str, char separator) {
        return split(str, separator, 0);
    }

    /**
     * 使用空白符切分字符串
     * 切分后的字符串两边不包含空白符,空串或空白符串并不做为元素之一
     *
     * @param str   被切分的字符串
     * @param limit 限制分片数
     * @return 切分后的集合
     */
    public static List<String> split(String str, int limit) {
        if (isEmpty(str)) {
            return new ArrayList<>(0);
        }
        if (limit == 1) {
            return CollKit.addAll(new ArrayList<>(1), str, true, true);
        }

        final List<String> list = new ArrayList<>();
        int len = str.length();
        int start = 0;//切分后每个部分的起始
        for (int i = 0; i < len; i++) {
            if (CharKit.isBlankChar(str.charAt(i))) {
                CollKit.addAll(list, str.substring(start, i), true, true);
                start = i + 1;//i+1同时将start与i保持一致

                //检查是否超出范围(最大允许limit-1个,剩下一个留给末尾字符串)
                if (limit > 0 && list.size() > limit - 2) {
                    break;
                }
            }
        }
        return CollKit.addAll(list, str.substring(start, len), true, true);//收尾
    }

    /**
     * 切分字符串,不去除切分后每个元素两边的空白符,不去除空白项
     *
     * @param str       被切分的字符串
     * @param separator 分隔符字符
     * @param limit     限制分片数,-1不限制
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence str, char separator, int limit) {
        return split(str.toString(), separator, limit, false, false);
    }

    /**
     * 切分字符串
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(String str, char separator, boolean isTrim, boolean ignoreEmpty) {
        return split(str, separator, 0, isTrim, ignoreEmpty);
    }

    /**
     * 切分字符串,不忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(String str, String separator, boolean isTrim, boolean ignoreEmpty) {
        return split(str, separator, -1, isTrim, ignoreEmpty, false);
    }

    /**
     * 切分字符串
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence str, CharSequence separator, int limit, boolean isTrim,
                                     boolean ignoreEmpty) {
        if (null == str) {
            return new ArrayList<>(0);
        }
        final String separatorStr = (null == separator) ? null : separator.toString();
        return split(str.toString(), separatorStr, limit, isTrim, ignoreEmpty);
    }

    /**
     * 切分字符串,大小写敏感
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数,-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(String str, char separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return split(str, separator, limit, isTrim, ignoreEmpty, false);
    }


    /**
     * 切分字符串,不忽略大小写
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(String str, String separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        return split(str, separator, limit, isTrim, ignoreEmpty, false);
    }

    /**
     * 切分字符串
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数，-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(CharSequence str, char separator, int limit, boolean isTrim,
                                     boolean ignoreEmpty) {
        if (null == str) {
            return new ArrayList<>(0);
        }
        return split(str.toString(), separator, limit, isTrim, ignoreEmpty);
    }

    /**
     * 通过正则切分字符串
     *
     * @param str         字符串
     * @param separator   分隔符正则{@link Pattern}
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @return 切分后的集合
     */
    public static List<String> split(String str, Pattern separator, int limit, boolean isTrim, boolean ignoreEmpty) {
        if (isEmpty(str)) {
            return new ArrayList<>(0);
        }
        if (limit == 1) {
            return CollKit.addAll(new ArrayList<>(1), str, isTrim, ignoreEmpty);
        }

        if (null == separator) {//分隔符为空时按照空白符切分
            return split(str, limit);
        }

        final Matcher matcher = separator.matcher(str);
        final List<String> list = new ArrayList<>();
        int len = str.length();
        int start = 0;
        while (matcher.find()) {
            CollKit.addAll(list, str.substring(start, matcher.start()), isTrim, ignoreEmpty);
            start = matcher.end();

            if (limit > 0 && list.size() > limit - 2) {
                break;
            }
        }
        return CollKit.addAll(list, str.substring(start, len), isTrim, ignoreEmpty);
    }

    /**
     * 切分字符串
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符
     * @param limit       限制分片数,-1不限制
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @param ignoreCase  是否忽略大小写
     * @return 切分后的集合
     */
    public static List<String> split(String str, char separator, int limit, boolean isTrim, boolean ignoreEmpty,
                                     boolean ignoreCase) {
        if (StringKit.isEmpty(str)) {
            return new ArrayList<>(0);
        }
        if (limit == 1) {
            return CollKit.addAll(new ArrayList<>(1), str, isTrim, ignoreEmpty);
        }

        final List<String> list = new ArrayList<>(limit > 0 ? limit : 16);
        int len = str.length();
        int start = 0;//切分后每个部分的起始
        for (int i = 0; i < len; i++) {
            if (MathKit.equals(separator, str.charAt(i), ignoreCase)) {
                CollKit.addAll(list, str.substring(start, i), isTrim, ignoreEmpty);
                start = i + 1;//i+1同时将start与i保持一致

                //检查是否超出范围(最大允许limit-1个，剩下一个留给末尾字符串)
                if (limit > 0 && list.size() > limit - 2) {
                    break;
                }
            }
        }
        return CollKit.addAll(list, str.substring(start, len), isTrim, ignoreEmpty);//收尾
    }

    /**
     * 切分字符串
     *
     * @param str         被切分的字符串
     * @param separator   分隔符字符串
     * @param limit       限制分片数
     * @param isTrim      是否去除切分字符串后每个元素两边的空格
     * @param ignoreEmpty 是否忽略空串
     * @param ignoreCase  是否忽略大小写
     * @return 切分后的集合
     */
    public static List<String> split(String str, String separator, int limit, boolean isTrim, boolean ignoreEmpty,
                                     boolean ignoreCase) {
        if (isEmpty(str)) {
            return new ArrayList<>(0);
        }
        if (limit == 1) {
            return CollKit.addAll(new ArrayList<>(1), str, isTrim, ignoreEmpty);
        }

        if (isEmpty(separator)) {//分隔符为空时按照空白符切分
            return split(str, limit);
        } else if (separator.length() == 1) {//分隔符只有一个字符长度时按照单分隔符切分
            return split(str, separator.charAt(0), limit, isTrim, ignoreEmpty, ignoreCase);
        }

        final List<String> list = new ArrayList<>();
        int len = str.length();
        int separatorLen = separator.length();
        int start = 0;
        int i = 0;
        while (i < len) {
            i = indexOf(str, separator, start, ignoreCase);
            if (i > -1) {
                CollKit.addAll(list, str.substring(start, i), isTrim, ignoreEmpty);
                start = i + separatorLen;

                //检查是否超出范围(最大允许limit-1个,剩下一个留给末尾字符串)
                if (limit > 0 && list.size() > limit - 2) {
                    break;
                }
            } else {
                break;
            }
        }
        return CollKit.addAll(list, str.substring(start, len), isTrim, ignoreEmpty);
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param str        字符串
     * @param searchChar 被查找的字符
     * @return 位置
     */
    public static int indexOf(final CharSequence str, char searchChar) {
        return indexOf(str, searchChar, 0);
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param str        字符串
     * @param searchChar 被查找的字符
     * @param start      起始位置,如果小于0,从0开始查找
     * @return 位置
     */
    public static int indexOf(final CharSequence str, char searchChar, int start) {
        if (str instanceof String) {
            return ((String) str).indexOf(searchChar, start);
        } else {
            return indexOf(str, searchChar, start, -1);
        }
    }

    /**
     * 指定范围内查找指定字符
     *
     * @param str        字符串
     * @param searchChar 被查找的字符
     * @param start      起始位置,如果小于0,从0开始查找
     * @param end        终止位置,如果超过str.length()则默认查找到字符串末尾
     * @return 位置
     */
    public static int indexOf(final CharSequence str, char searchChar, int start, int end) {
        final int len = str.length();
        if (start < 0 || start > len) {
            start = 0;
        }
        if (end > len || end < 0) {
            end = len;
        }
        for (int i = start; i < end; i++) {
            if (str.charAt(i) == searchChar) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 指定范围内查找字符串,忽略大小写
     *
     * <pre>
     * StringKit.indexOfIgnoreCase(null, *, *)          = -1
     * StringKit.indexOfIgnoreCase(*, null, *)          = -1
     * StringKit.indexOfIgnoreCase("", "", 0)           = 0
     * StringKit.indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * StringKit.indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * StringKit.indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * StringKit.indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * StringKit.indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * StringKit.indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * StringKit.indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * StringKit.indexOfIgnoreCase("abc", "", 9)        = -1
     * </pre>
     *
     * @param str       字符串
     * @param searchStr 需要查找位置的字符串
     * @return 位置
     */
    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr) {
        return indexOfIgnoreCase(str, searchStr, 0);
    }

    /**
     * 指定范围内查找字符串
     *
     * <pre>
     * StringKit.indexOfIgnoreCase(null, *, *)          = -1
     * StringKit.indexOfIgnoreCase(*, null, *)          = -1
     * StringKit.indexOfIgnoreCase("", "", 0)           = 0
     * StringKit.indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * StringKit.indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * StringKit.indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * StringKit.indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * StringKit.indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * StringKit.indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * StringKit.indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * StringKit.indexOfIgnoreCase("abc", "", 9)        = -1
     * </pre>
     *
     * @param str       字符串
     * @param searchStr 需要查找位置的字符串
     * @param fromIndex 起始位置
     * @return 位置
     */
    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr, int fromIndex) {
        return indexOf(str, searchStr, fromIndex, true);
    }

    /**
     * 指定范围内反向查找字符串
     *
     * @param str        字符串
     * @param searchStr  需要查找位置的字符串
     * @param fromIndex  起始位置
     * @param ignoreCase 是否忽略大小写
     * @return 位置
     */
    public static int indexOf(final CharSequence str, CharSequence searchStr, int fromIndex, boolean ignoreCase) {
        if (str == null || searchStr == null) {
            return INDEX_NOT_FOUND;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }

        final int endLimit = str.length() - searchStr.length() + 1;
        if (fromIndex > endLimit) {
            return INDEX_NOT_FOUND;
        }
        if (searchStr.length() == 0) {
            return fromIndex;
        }

        if (false == ignoreCase) {
            // 不忽略大小写调用JDK方法
            return str.toString().indexOf(searchStr.toString(), fromIndex);
        }

        for (int i = fromIndex; i < endLimit; i++) {
            if (isSubEquals(str, i, searchStr, 0, searchStr.length(), true)) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 指定范围内查找字符串,忽略大小写
     *
     * @param str       字符串
     * @param searchStr 需要查找位置的字符串
     * @return 位置
     */
    public static int lastIndexOfIgnoreCase(final CharSequence str, final CharSequence searchStr) {
        return lastIndexOfIgnoreCase(str, searchStr, str.length());
    }

    /**
     * 指定范围内查找字符串,忽略大小写
     *
     * @param str       字符串
     * @param searchStr 需要查找位置的字符串
     * @param fromIndex 起始位置,从后往前计数
     * @return 位置
     */
    public static int lastIndexOfIgnoreCase(final CharSequence str, final CharSequence searchStr, int fromIndex) {
        return lastIndexOf(str, searchStr, fromIndex, true);
    }

    /**
     * 指定范围内查找字符串
     *
     * @param str        字符串
     * @param searchStr  需要查找位置的字符串
     * @param fromIndex  起始位置,从后往前计数
     * @param ignoreCase 是否忽略大小写
     * @return 位置
     */
    public static int lastIndexOf(final CharSequence str, final CharSequence searchStr, int fromIndex,
                                  boolean ignoreCase) {
        if (str == null || searchStr == null) {
            return INDEX_NOT_FOUND;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        fromIndex = Math.min(fromIndex, str.length());

        if (searchStr.length() == 0) {
            return fromIndex;
        }

        if (false == ignoreCase) {
            // 不忽略大小写调用JDK方法
            return str.toString().lastIndexOf(searchStr.toString(), fromIndex);
        }

        for (int i = fromIndex; i > 0; i--) {
            if (isSubEquals(str, i, searchStr, 0, searchStr.length(), true)) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回字符串 searchStr 在字符串 str 中第 ordinal 次出现的位置
     * 如果 str=null 或 searchStr=null 或 ordinal小于等于0 则返回-1
     *
     * <pre>
     * StringKit.ordinalIndexOf(null, *, *)          = -1
     * StringKit.ordinalIndexOf(*, null, *)          = -1
     * StringKit.ordinalIndexOf("", "", *)           = 0
     * StringKit.ordinalIndexOf("aabaabaa", "a", 1)  = 0
     * StringKit.ordinalIndexOf("aabaabaa", "a", 2)  = 1
     * StringKit.ordinalIndexOf("aabaabaa", "b", 1)  = 2
     * StringKit.ordinalIndexOf("aabaabaa", "b", 2)  = 5
     * StringKit.ordinalIndexOf("aabaabaa", "ab", 1) = 1
     * StringKit.ordinalIndexOf("aabaabaa", "ab", 2) = 4
     * StringKit.ordinalIndexOf("aabaabaa", "", 1)   = 0
     * StringKit.ordinalIndexOf("aabaabaa", "", 2)   = 0
     * </pre>
     *
     * @param str       被检查的字符串,可以为null
     * @param searchStr 被查找的字符串,可以为null
     * @param ordinal   第几次出现的位置
     * @return 查找到的位置
     */
    public static int ordinalIndexOf(String str, String searchStr, int ordinal) {
        if (str == null || searchStr == null || ordinal <= 0) {
            return INDEX_NOT_FOUND;
        }
        if (searchStr.length() == 0) {
            return 0;
        }
        int found = 0;
        int index = INDEX_NOT_FOUND;
        do {
            index = str.indexOf(searchStr, index + 1);
            if (index < 0) {
                return index;
            }
            found++;
        } while (found < ordinal);
        return index;
    }

    /**
     * 重复某个字符
     *
     * @param c     被重复的字符
     * @param count 重复的数目,如果小于等于0则返回""
     * @return 重复字符字符串
     */
    public static String repeat(char c, int count) {
        if (count <= 0) {
            return Normal.EMPTY;
        }

        char[] result = new char[count];
        for (int i = 0; i < count; i++) {
            result[i] = c;
        }
        return new String(result);
    }

    /**
     * 重复某个字符串
     *
     * @param str   被重复的字符
     * @param count 重复的数目
     * @return 重复字符字符串
     */
    public static String repeat(CharSequence str, int count) {
        if (null == str) {
            return null;
        }
        if (count <= 0) {
            return Normal.EMPTY;
        }
        if (count == 1 || str.length() == 0) {
            return str.toString();
        }

        // 检查
        final int len = str.length();
        final long longSize = (long) len * (long) count;
        final int size = (int) longSize;
        if (size != longSize) {
            throw new ArrayIndexOutOfBoundsException("Required String length is too large: " + longSize);
        }

        final char[] array = new char[size];
        str.toString().getChars(0, len, array, 0);
        int n;
        for (n = len; n < size - n; n <<= 1) {// n <<= 1相当于n *2
            System.arraycopy(array, 0, array, n, n);
        }
        System.arraycopy(array, 0, array, n, size - n);
        return new String(array);
    }


    /**
     * 重复某个字符串到指定长度
     *
     * @param str    被重复的字符
     * @param padLen 指定长度
     * @return 重复字符字符串
     */
    public static String repeatByLength(CharSequence str, int padLen) {
        if (null == str) {
            return null;
        }
        if (padLen <= 0) {
            return Normal.EMPTY;
        }
        final int strLen = str.length();
        if (strLen == padLen) {
            return str.toString();
        } else if (strLen > padLen) {
            return subPre(str, padLen);
        }

        // 重复，直到达到指定长度
        final char[] padding = new char[padLen];
        for (int i = 0; i < padLen; i++) {
            padding[i] = str.charAt(i % strLen);
        }
        return new String(padding);
    }

    /**
     * 重复某个字符串并通过分界符连接
     *
     * <pre>
     * StringKit.repeatAndJoin("?", 5, ",")   = "?,?,?,?,?"
     * StringKit.repeatAndJoin("?", 0, ",")   = ""
     * StringKit.repeatAndJoin("?", 5, null) = "?????"
     * </pre>
     *
     * @param str         被重复的字符串
     * @param count       数量
     * @param conjunction 分界符
     * @return 连接后的字符串
     */
    public static String repeatAndJoin(CharSequence str, int count, CharSequence conjunction) {
        if (count <= 0) {
            return Normal.EMPTY;
        }
        final StrBuilder builder = StrBuilder.create();
        boolean isFirst = true;
        while (count-- > 0) {
            if (isFirst) {
                isFirst = false;
            } else if (isNotEmpty(conjunction.toString())) {
                builder.append(conjunction);
            }
            builder.append(str);
        }
        return builder.toString();
    }

    /**
     * 反转字符串
     * 例如：abcd =》dcba
     *
     * @param str 被反转的字符串
     * @return 反转后的字符串
     */
    public static String reverse(String str) {
        char[] chars = str.toCharArray();
        ArrayKit.reverse(chars);
        return new String(chars);
    }

    /**
     * 编码字符串
     * 使用系统默认编码
     *
     * @param str 字符串
     * @return 编码后的字节码
     */
    public static byte[] bytes(CharSequence str) {
        return bytes(str, java.nio.charset.Charset.defaultCharset());
    }

    /**
     * 编码字符串
     *
     * @param str     字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 编码后的字节码
     */
    public static byte[] bytes(CharSequence str, String charset) {
        return bytes(str, isBlank(charset) ? java.nio.charset.Charset.defaultCharset() : java.nio.charset.Charset.forName(charset));
    }

    /**
     * 编码字符串
     *
     * @param str     字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 编码后的字节码
     */
    public static byte[] bytes(CharSequence str, java.nio.charset.Charset charset) {
        if (str == null) {
            return null;
        }

        if (null == charset) {
            return str.toString().getBytes();
        }
        return str.toString().getBytes(charset);
    }

    /**
     * 删除指定字符串
     * 是否在开始位置,否则返回源字符串
     * A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.
     *
     * <pre>
     * StringKit.removeStart(null, *)      = null
     * StringKit.removeStart("", *)        = ""
     * StringKit.removeStart(*, null)      = *
     * StringKit.removeStart("www.domain.com", "www.")   = "domain.com"
     * StringKit.removeStart("domain.com", "www.")       = "domain.com"
     * StringKit.removeStart("www.domain.com", "domain") = "www.domain.com"
     * StringKit.removeStart("abc", "")    = "abc"
     * </pre>
     *
     * @param str    要搜索的源字符串可能为空
     * @param remove 要搜索和删除的字符串可能为空
     * @return 如果找到，则删除字符串，如果输入为空字符串，则{@code null}
     */
    public static String removeStart(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.startsWith(remove)) {
            return str.substring(remove.length());
        }
        return str;
    }

    /**
     * 去掉首部指定长度的字符串并将剩余字符串首字母小写
     * 例如：str=setName, preLength=3 =》 return name
     *
     * @param str       被处理的字符串
     * @param preLength 去掉的长度
     * @return 处理后的字符串, 不符合规范返回null
     */
    public static String removePreAndLowerFirst(CharSequence str, int preLength) {
        if (str == null) {
            return null;
        }
        if (str.length() > preLength) {
            char first = Character.toLowerCase(str.charAt(preLength));
            if (str.length() > preLength + 1) {
                return first + str.toString().substring(preLength + 1);
            }
            return String.valueOf(first);
        } else {
            return str.toString();
        }
    }

    /**
     * 去掉首部指定长度的字符串并将剩余字符串首字母小写
     * 例如：str=setName, prefix=set =》 return name
     *
     * @param str    被处理的字符串
     * @param prefix 前缀
     * @return 处理后的字符串, 不符合规范返回null
     */
    public static String removePreAndLowerFirst(CharSequence str, CharSequence prefix) {
        return lowerFirst(removePrefix(str, prefix));
    }


    /**
     * 去掉指定前缀
     *
     * @param str    字符串
     * @param prefix 前缀
     * @return 切掉后的字符串, 若前缀不是 preffix, 返回原字符串
     */
    public static String removePrefix(CharSequence str, CharSequence prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return toString(str);
        }

        final String str2 = str.toString();
        if (str2.startsWith(prefix.toString())) {
            return subSuf(str2, prefix.length());// 截取后半段
        }
        return str2;
    }

    /**
     * 忽略大小写去掉指定前缀
     *
     * @param str    字符串
     * @param prefix 前缀
     * @return 切掉后的字符串, 若前缀不是 prefix, 返回原字符串
     */
    public static String removePrefixIgnoreCase(CharSequence str, CharSequence prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return toString(str);
        }

        final String str2 = str.toString();
        if (str2.toLowerCase().startsWith(prefix.toString().toLowerCase())) {
            return subSuf(str2, prefix.length());// 截取后半段
        }
        return str2;
    }

    /**
     * 去掉指定后缀
     *
     * @param str    字符串
     * @param suffix 后缀
     * @return 切掉后的字符串, 若后缀不是 suffix, 返回原字符串
     */
    public static String removeSuffix(CharSequence str, CharSequence suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return toString(str);
        }

        final String str2 = str.toString();
        if (str2.endsWith(suffix.toString())) {
            return subPre(str2, str2.length() - suffix.length());// 截取前半段
        }
        return str2;
    }

    /**
     * 原字符串首字母大写并在其首部添加指定字符串
     * 例如：str=name, preString=get = return getName
     *
     * @param str       被处理的字符串
     * @param preString 添加的首部
     * @return 处理后的字符串
     */
    public static String upperFirstAndAddPre(CharSequence str, String preString) {
        if (str == null || preString == null) {
            return null;
        }
        return preString + upperFirst(str);
    }

    /**
     * 大写首字母
     * 例如：str = name, return Name
     *
     * @param str 字符串
     * @return 字符串
     */
    public static String upperFirst(CharSequence str) {
        if (null == str) {
            return null;
        }
        if (str.length() > 0) {
            char firstChar = str.charAt(0);
            if (Character.isLowerCase(firstChar)) {
                return Character.toUpperCase(firstChar) + subSuf(str, 1);
            }
        }
        return str.toString();
    }

    /**
     * 小写首字母
     * 例如：str = Name, return name
     *
     * @param str 字符串
     * @return 字符串
     */
    public static String lowerFirst(CharSequence str) {
        if (null == str) {
            return null;
        }
        if (str.length() > 0) {
            char firstChar = str.charAt(0);
            if (Character.isUpperCase(firstChar)) {
                return Character.toLowerCase(firstChar) + subSuf(str, 1);
            }
        }
        return str.toString();
    }


    /**
     * 将驼峰式命名的字符串转换为下划线方式
     * 如果转换前的驼峰式命名的字符串为空,则返回空字符串
     * 例如：HelloWorld=》hello_world
     *
     * @param camelCaseStr 转换前的驼峰式命名的字符串
     * @return 转换后下划线大写方式命名的字符串
     */
    public static String toUnderlineCase(CharSequence camelCaseStr) {
        if (camelCaseStr == null) {
            return null;
        }

        final int length = camelCaseStr.length();
        StringBuilder sb = new StringBuilder();
        char c;
        boolean isPreUpperCase = false;
        for (int i = 0; i < length; i++) {
            c = camelCaseStr.charAt(i);
            boolean isNextUpperCase = true;
            if (i < (length - 1)) {
                isNextUpperCase = Character.isUpperCase(camelCaseStr.charAt(i + 1));
            }
            if (Character.isUpperCase(c)) {
                if (!isPreUpperCase || !isNextUpperCase) {
                    if (i > 0) {
                        sb.append(Symbol.UNDERLINE);
                    }
                }
                isPreUpperCase = true;
            } else {
                isPreUpperCase = false;
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    /**
     * 将驼峰式命名的字符串转换为使用符号连接方式
     * 如果转换前的驼峰式命名的字符串为空，则返回空字符串
     *
     * @param str    转换前的驼峰式命名的字符串，也可以为符号连接形式
     * @param symbol 连接符
     * @return 转换后符号连接方式命名的字符串
     */
    public static String toSymbolCase(CharSequence str, char symbol) {
        if (str == null) {
            return null;
        }

        final int length = str.length();
        final StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0; i < length; i++) {
            c = str.charAt(i);
            final Character preChar = (i > 0) ? str.charAt(i - 1) : null;
            if (Character.isUpperCase(c)) {
                // 遇到大写字母处理
                final Character nextChar = (i < str.length() - 1) ? str.charAt(i + 1) : null;
                if (null != preChar && Character.isUpperCase(preChar)) {
                    // 前一个字符为大写，则按照一个词对待
                    sb.append(c);
                } else if (null != nextChar && Character.isUpperCase(nextChar)) {
                    // 后一个为大写字母，按照一个词对待
                    if (null != preChar && symbol != preChar) {
                        // 前一个是非大写时按照新词对待，加连接符
                        sb.append(symbol);
                    }
                    sb.append(c);
                } else {
                    // 前后都为非大写按照新词对待
                    if (null != preChar && symbol != preChar) {
                        // 前一个非连接符，补充连接符
                        sb.append(symbol);
                    }
                    sb.append(Character.toLowerCase(c));
                }
            } else {
                if (sb.length() > 0 && Character.isUpperCase(sb.charAt(sb.length() - 1)) && symbol != c) {
                    // 当结果中前一个字母为大写，当前为小写，说明此字符为新词开始(连接符也表示新词)
                    sb.append(symbol);
                }
                // 小写或符号
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 将下划线方式命名的字符串转换为驼峰式
     * 如果转换前的下划线大写方式命名的字符串为空，则返回空字符串
     * 例如：hello_world=》helloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String toCamelCase(CharSequence name) {
        if (null == name) {
            return null;
        }

        String name2 = name.toString();
        if (name2.contains(Symbol.UNDERLINE)) {
            final StringBuilder sb = new StringBuilder(name2.length());
            boolean upperCase = false;
            for (int i = 0; i < name2.length(); i++) {
                char c = name2.charAt(i);

                if (c == Symbol.C_UNDERLINE) {
                    upperCase = true;
                } else if (upperCase) {
                    sb.append(Character.toUpperCase(c));
                    upperCase = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
            return sb.toString();
        } else {
            return name2;
        }
    }

    /**
     * 替换字符串中的指定字符串.
     *
     * <pre>
     * StringKit.remove(null, *)        = null
     * StringKit.remove("", *)          = ""
     * StringKit.remove(*, null)        = *
     * StringKit.remove(*, "")          = *
     * StringKit.remove("queued", "ue") = "qd"
     * StringKit.remove("queued", "zz") = "queued"
     * </pre>
     *
     * @param str    要搜索的源字符串可能为空
     * @param remove 要搜索和删除的字符串可能为空
     * @return 如果找到，则删除字符串，如果输入为空字符串，则{@code null}
     */
    public static String remove(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        return replace(str, remove, Normal.EMPTY, -1);
    }

    /**
     * 替换字符串中的指定字符串.
     *
     * <pre>
     * StringKit.removeIgnoreCase(null, *)        = null
     * StringKit.removeIgnoreCase("", *)          = ""
     * StringKit.removeIgnoreCase(*, null)        = *
     * StringKit.removeIgnoreCase(*, "")          = *
     * StringKit.removeIgnoreCase("queued", "ue") = "qd"
     * StringKit.removeIgnoreCase("queued", "zz") = "queued"
     * StringKit.removeIgnoreCase("quEUed", "UE") = "qd"
     * StringKit.removeIgnoreCase("queued", "zZ") = "queued"
     * </pre>
     *
     * @param str    要搜索的源字符串可能为空
     * @param remove 要搜索和删除的字符串(不区分大小写)可能为空
     * @return 如果找到，则删除字符串，如果输入为空字符串，则{@code null}
     */
    public static String removeIgnoreCase(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        return replaceIgnoreCase(str, remove, Normal.EMPTY, -1);
    }

    /**
     * 替换字符串中的指定字符串.
     *
     * <pre>
     * StringKit.remove(null, *)       = null
     * StringKit.remove("", *)         = ""
     * StringKit.remove("queued", 'u') = "qeed"
     * StringKit.remove("queued", 'z') = "queued"
     * </pre>
     *
     * @param str    要搜索的源字符串可能为空
     * @param remove 要搜索和删除的字符串(不区分大小写)可能为空
     * @return 如果找到，则删除字符的子字符串，如果输入为空字符串，则{@code null}
     */
    public static String remove(final String str, final char remove) {
        if (isEmpty(str) || str.indexOf(remove) == INDEX_NOT_FOUND) {
            return str;
        }
        final char[] chars = str.toCharArray();
        int pos = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != remove) {
                chars[pos++] = chars[i];
            }
        }
        return new String(chars, 0, pos);
    }

    /**
     * 替换字符串中的指定字符串.
     *
     * <pre>
     * StringKit.removeAll(null, *)      = null
     * StringKit.removeAll("any", null)  = "any"
     * StringKit.removeAll("any", "")    = "any"
     * StringKit.removeAll("any", ".*")  = ""
     * StringKit.removeAll("any", ".+")  = ""
     * StringKit.removeAll("abc", ".?")  = ""
     * StringKit.removeAll("A&lt;__&gt;\n&lt;__&gt;B", "&lt;.*&gt;")      = "A\nB"
     * StringKit.removeAll("A&lt;__&gt;\n&lt;__&gt;B", "(?s)&lt;.*&gt;")  = "AB"
     * StringKit.removeAll("ABCabc123abc", "[a-z]")     = "ABC123"
     * </pre>
     *
     * @param text  要从中删除的文本可能为空
     * @param regex 要与此字符串匹配的正则表达式
     * @return 带有任何已处理删除的文本，{@code null}如果输入为空字符串
     * @throws java.util.regex.PatternSyntaxException 如果正则表达式的语法无效
     * @see #replaceAll(String, String, String)
     * @see String#replaceAll(String, String)
     * @see java.util.regex.Pattern
     * @see java.util.regex.Pattern#DOTALL
     */
    public static String removeAll(final String text, final String regex) {
        return replaceAll(text, regex, Normal.EMPTY);
    }

    /**
     * 去除字符串中指定的多个字符，如有多个则全部去除
     *
     * @param str   字符串
     * @param chars 字符列表
     * @return 去除后的字符
     */
    public static String removeAll(CharSequence str, char... chars) {
        if (null == str || ArrayKit.isEmpty(chars)) {
            return toString(str);
        }
        final int len = str.length();
        if (0 == len) {
            return toString(str);
        }
        final StringBuilder builder = builder(len);
        char c;
        for (int i = 0; i < len; i++) {
            c = str.charAt(i);
            if (false == ArrayKit.contains(chars, c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * 移除字符串中所有给定字符串
     * 例：removeAll("aa-bb-cc-dd", "-") - aabbccdd
     *
     * @param str         字符串
     * @param strToRemove 被移除的字符串
     * @return 移除后的字符串
     */
    public static String removeAll(CharSequence str, CharSequence strToRemove) {
        if (isEmpty(str)) {
            return toString(str);
        }
        return str.toString().replace(strToRemove, Normal.EMPTY);
    }

    /**
     * 移除字符串中所有给定字符串，当某个字符串出现多次，则全部移除
     * 例：removeAny("aa-bb-cc-dd", "a", "b") - --cc-dd
     *
     * @param str          字符串
     * @param strsToRemove 被移除的字符串
     * @return 移除后的字符串
     */
    public static String removeAny(CharSequence str, CharSequence... strsToRemove) {
        String result = toString(str);
        if (isNotEmpty(str)) {
            for (CharSequence strToRemove : strsToRemove) {
                result = removeAll(str, strToRemove);
            }
        }
        return result;
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringKit.removeFirst(null, *)      = null
     * StringKit.removeFirst("any", null)  = "any"
     * StringKit.removeFirst("any", "")    = "any"
     * StringKit.removeFirst("any", ".*")  = ""
     * StringKit.removeFirst("any", ".+")  = ""
     * StringKit.removeFirst("abc", ".?")  = "bc"
     * StringKit.removeFirst("A&lt;__&gt;\n&lt;__&gt;B", "&lt;.*&gt;")      = "A\n&lt;__&gt;B"
     * StringKit.removeFirst("A&lt;__&gt;\n&lt;__&gt;B", "(?s)&lt;.*&gt;")  = "AB"
     * StringKit.removeFirst("ABCabc123", "[a-z]")          = "ABCbc123"
     * StringKit.removeFirst("ABCabc123abc", "[a-z]+")      = "ABC123abc"
     *
     * @param text  要从中删除的文本可能为空
     * @param regex 要与此字符串匹配的正则表达式
     * @return 处理第一个替换的文本，{@code null}如果输入为空字符串
     * @throws java.util.regex.PatternSyntaxException 如果正则表达式的语法无效
     * @see #replaceFirst(String, String, String)
     * @see String#replaceFirst(String, String)
     * @see java.util.regex.Pattern
     * @see java.util.regex.Pattern#DOTALL
     */
    public static String removeFirst(final String text, final String regex) {
        return replaceFirst(text, regex, Normal.EMPTY);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringKit.replaceAll(null, *, *)       = null
     * StringKit.replaceAll("any", null, *)   = "any"
     * StringKit.replaceAll("any", *, null)   = "any"
     * StringKit.replaceAll("", "", "zzz")    = "zzz"
     * StringKit.replaceAll("", ".*", "zzz")  = "zzz"
     * StringKit.replaceAll("", ".+", "zzz")  = ""
     * StringKit.replaceAll("abc", "", "ZZ")  = "ZZaZZbZZcZZ"
     * StringKit.replaceAll("&lt;__&gt;\n&lt;__&gt;", "&lt;.*&gt;", "z")      = "z\nz"
     * StringKit.replaceAll("&lt;__&gt;\n&lt;__&gt;", "(?s)&lt;.*&gt;", "z")  = "z"
     * StringKit.replaceAll("ABCabc123", "[a-z]", "_")       = "ABC___123"
     * StringKit.replaceAll("ABCabc123", "[^A-Z0-9]+", "_")  = "ABC_123"
     * StringKit.replaceAll("ABCabc123", "[^A-Z0-9]+", "")   = "ABC123"
     * StringKit.replaceAll("Lorem ipsum  dolor   sit", "( +)([a-z]+)", "_$2")  = "Lorem_ipsum_dolor_sit"
     *
     * @param text        要搜索和替换的文本可能为空
     * @param regex       要与此字符串匹配的正则表达式
     * @param replacement 每个匹配项要替换的字符串
     * @return 处理了任何替换的文本，{@code null}如果输入为空字符串
     * @throws java.util.regex.PatternSyntaxException 如果正则表达式的语法无效
     * @see String#replaceAll(String, String)
     * @see java.util.regex.Pattern
     * @see java.util.regex.Pattern#DOTALL
     */
    public static String replaceAll(final String text, final String regex, final String replacement) {
        if (text == null || regex == null || replacement == null) {
            return text;
        }
        return text.replaceAll(regex, replacement);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringKit.replaceFirst(null, *, *)       = null
     * StringKit.replaceFirst("any", null, *)   = "any"
     * StringKit.replaceFirst("any", *, null)   = "any"
     * StringKit.replaceFirst("", "", "zzz")    = "zzz"
     * StringKit.replaceFirst("", ".*", "zzz")  = "zzz"
     * StringKit.replaceFirst("", ".+", "zzz")  = ""
     * StringKit.replaceFirst("abc", "", "ZZ")  = "ZZabc"
     * StringKit.replaceFirst("&lt;__&gt;\n&lt;__&gt;", "&lt;.*&gt;", "z")      = "z\n&lt;__&gt;"
     * StringKit.replaceFirst("&lt;__&gt;\n&lt;__&gt;", "(?s)&lt;.*&gt;", "z")  = "z"
     * StringKit.replaceFirst("ABCabc123", "[a-z]", "_")          = "ABC_bc123"
     * StringKit.replaceFirst("ABCabc123abc", "[^A-Z0-9]+", "_")  = "ABC_123abc"
     * StringKit.replaceFirst("ABCabc123abc", "[^A-Z0-9]+", "")   = "ABC123abc"
     * StringKit.replaceFirst("Lorem ipsum  dolor   sit", "( +)([a-z]+)", "_$2")  = "Lorem_ipsum  dolor   sit"
     *
     * @param text        要搜索和替换的文本可能为空
     * @param regex       要与此字符串匹配的正则表达式
     * @param replacement 将被替换为第一字符串
     * @return 处理第一个替换的文本，{@code null}如果输入为空字符串
     * @throws java.util.regex.PatternSyntaxException 如果正则表达式的语法无效
     * @see String#replaceFirst(String, String)
     * @see java.util.regex.Pattern
     * @see java.util.regex.Pattern#DOTALL
     */
    public static String replaceFirst(final String text, final String regex, final String replacement) {
        if (text == null || regex == null || replacement == null) {
            return text;
        }
        return text.replaceFirst(regex, replacement);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringKit.replace(null, *, *)        = null
     * StringKit.replace("", *, *)          = ""
     * StringKit.replace("any", null, *)    = "any"
     * StringKit.replace("any", *, null)    = "any"
     * StringKit.replace("any", "", *)      = "any"
     * StringKit.replace("aba", "a", null)  = "aba"
     * StringKit.replace("aba", "a", "")    = "b"
     * StringKit.replace("aba", "a", "z")   = "zbz"
     *
     * @param text         要搜索和替换的文本可能为空
     * @param searchString 要搜索的字符串可能为空
     * @param replacement  要替换它的字符串可能是null
     * @return 处理了任何替换的文本，{@code null}如果输入为空字符串
     * @see #replace(String text, String searchString, String replacement, int max)
     */
    public static String replace(final String text, final String searchString, final String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    /**
     * 替换字符串中的指定字符串.
     * StringKit.replaceIgnoreCase(null, *, *)        = null
     * StringKit.replaceIgnoreCase("", *, *)          = ""
     * StringKit.replaceIgnoreCase("any", null, *)    = "any"
     * StringKit.replaceIgnoreCase("any", *, null)    = "any"
     * StringKit.replaceIgnoreCase("any", "", *)      = "any"
     * StringKit.replaceIgnoreCase("aba", "a", null)  = "aba"
     * StringKit.replaceIgnoreCase("abA", "A", "")    = "b"
     * StringKit.replaceIgnoreCase("aba", "A", "z")   = "zbz"
     *
     * @param text         要搜索和替换的文本可能为空
     * @param searchString 要搜索的字符串(大小写不敏感)可以为空
     * @param replacement  要替换它的字符串可能是null
     * @return 处理了任何替换的文本，{@code null}如果输入为空字符串
     * @see #replaceIgnoreCase(String text, String searchString, String replacement, int max)
     */
    public static String replaceIgnoreCase(final String text, final String searchString, final String replacement) {
        return replaceIgnoreCase(text, searchString, replacement, -1);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringKit.replace(null, *, *, *)         = null
     * StringKit.replace("", *, *, *)           = ""
     * StringKit.replace("any", null, *, *)     = "any"
     * StringKit.replace("any", *, null, *)     = "any"
     * StringKit.replace("any", "", *, *)       = "any"
     * StringKit.replace("any", *, *, 0)        = "any"
     * StringKit.replace("abaa", "a", null, -1) = "abaa"
     * StringKit.replace("abaa", "a", "", -1)   = "b"
     * StringKit.replace("abaa", "a", "z", 0)   = "abaa"
     * StringKit.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringKit.replace("abaa", "a", "z", 2)   = "zbza"
     * StringKit.replace("abaa", "a", "z", -1)  = "zbzz"
     *
     * @param text         要搜索和替换的文本可能为空
     * @param searchString 要搜索的字符串可能为空
     * @param replacement  要替换它的字符串可能是null
     * @param max          要替换的值的最大数目，如果没有最大值，则为{@code -1}
     * @return 处理了任何替换的文本，{@code null}如果输入为空字符串
     */
    public static String replace(final String text, final String searchString, final String replacement,
                                 final int max) {
        return replace(text, searchString, replacement, max, false);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringKit.replace(null, *, *, *, false)         = null
     * StringKit.replace("", *, *, *, false)           = ""
     * StringKit.replace("any", null, *, *, false)     = "any"
     * StringKit.replace("any", *, null, *, false)     = "any"
     * StringKit.replace("any", "", *, *, false)       = "any"
     * StringKit.replace("any", *, *, 0, false)        = "any"
     * StringKit.replace("abaa", "a", null, -1, false) = "abaa"
     * StringKit.replace("abaa", "a", "", -1, false)   = "b"
     * StringKit.replace("abaa", "a", "z", 0, false)   = "abaa"
     * StringKit.replace("abaa", "A", "z", 1, false)   = "abaa"
     * StringKit.replace("abaa", "A", "z", 1, true)   = "zbaa"
     * StringKit.replace("abAa", "a", "z", 2, true)   = "zbza"
     * StringKit.replace("abAa", "a", "z", -1, true)  = "zbzz"
     *
     * @param text         要搜索和替换的文本可能为空
     * @param searchString 要搜索的字符串(大小写不敏感)可以为空
     * @param replacement  要替换它的字符串可能是null
     * @param max          要替换的值的最大数目，如果没有最大值，则为{@code -1}
     * @param ignoreCase   如果真替换不区分大小写，则为区分大小写
     * @return 处理了任何替换的文本，{@code null}如果输入为空字符串
     */
    private static String replace(final String text, String searchString, final String replacement, int max,
                                  final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        String searchText = text;
        if (ignoreCase) {
            searchText = text.toLowerCase();
            searchString = searchString.toLowerCase();
        }
        int start = 0;
        int end = searchText.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        final int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = increase < 0 ? 0 : increase;
        increase *= max < 0 ? 16 : max > 64 ? 64 : max;
        final StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = searchText.indexOf(searchString, start);
        }
        buf.append(text, start, text.length());
        return buf.toString();
    }

    /**
     * 替换字符串中的空格、回车、换行符、制表符
     *
     * @param str 字符串信息
     * @return 替换后的字符串
     */
    public static String replaceBlank(String str) {
        String val = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            val = m.replaceAll(Normal.EMPTY);
        }
        return val;
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * for the first {@code max} values of the search String.
     * StringKit.replaceIgnoreCase(null, *, *, *)         = null
     * StringKit.replaceIgnoreCase("", *, *, *)           = ""
     * StringKit.replaceIgnoreCase("any", null, *, *)     = "any"
     * StringKit.replaceIgnoreCase("any", *, null, *)     = "any"
     * StringKit.replaceIgnoreCase("any", "", *, *)       = "any"
     * StringKit.replaceIgnoreCase("any", *, *, 0)        = "any"
     * StringKit.replaceIgnoreCase("abaa", "a", null, -1) = "abaa"
     * StringKit.replaceIgnoreCase("abaa", "a", "", -1)   = "b"
     * StringKit.replaceIgnoreCase("abaa", "a", "z", 0)   = "abaa"
     * StringKit.replaceIgnoreCase("abaa", "A", "z", 1)   = "zbaa"
     * StringKit.replaceIgnoreCase("abAa", "a", "z", 2)   = "zbza"
     * StringKit.replaceIgnoreCase("abAa", "a", "z", -1)  = "zbzz"
     *
     * @param text         要搜索和替换的文本可能为空
     * @param searchString 要搜索的字符串(大小写不敏感)可以为空
     * @param replacement  要替换它的字符串可能是null
     * @param max          要替换的值的最大数目，如果没有最大值，则为{@code -1}
     * @return 处理了任何替换的文本，{@code null}如果输入为空字符串
     */
    public static String replaceIgnoreCase(final String text, final String searchString, final String replacement,
                                           final int max) {
        return replace(text, searchString, replacement, max, true);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringKit.replaceEach(null, *, *)        = null
     * StringKit.replaceEach("", *, *)          = ""
     * StringKit.replaceEach("aba", null, null) = "aba"
     * StringKit.replaceEach("aba", new String[0], null) = "aba"
     * StringKit.replaceEach("aba", null, new String[0]) = "aba"
     * StringKit.replaceEach("aba", new String[]{"a"}, null)  = "aba"
     * StringKit.replaceEach("aba", new String[]{"a"}, new String[]{""})  = "b"
     * StringKit.replaceEach("aba", new String[]{null}, new String[]{"a"})  = "aba"
     * StringKit.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"})  = "wcte"
     * (example of how it does not repeat)
     * StringKit.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"})  = "dcte"
     *
     * @param text            要搜索和替换的文本，如果为空则不执行操作
     * @param searchList      要搜索的字符串，如果为空则为no-op
     * @param replacementList 要替换它们的字符串，如果为空则为no-op
     * @return 处理了任何替换的文本，{@code null}如果输入为空字符串
     * @throws IllegalArgumentException 如果数组的长度不相同(可以为null，大小为0)
     */
    public static String replaceEach(final String text, final String[] searchList, final String[] replacementList) {
        return replaceEach(text, searchList, replacementList, false, 0);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringKit.replaceEachRepeatedly(null, *, *) = null
     * StringKit.replaceEachRepeatedly("", *, *) = ""
     * StringKit.replaceEachRepeatedly("aba", null, null) = "aba"
     * StringKit.replaceEachRepeatedly("aba", new String[0], null) = "aba"
     * StringKit.replaceEachRepeatedly("aba", null, new String[0]) = "aba"
     * StringKit.replaceEachRepeatedly("aba", new String[]{"a"}, null) = "aba"
     * StringKit.replaceEachRepeatedly("aba", new String[]{"a"}, new String[]{""}) = "b"
     * StringKit.replaceEachRepeatedly("aba", new String[]{null}, new String[]{"a"}) = "aba"
     * StringKit.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}) = "wcte"
     * (example of how it repeats)
     * StringKit.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}) = "tcte"
     * StringKit.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"}) = IllegalStateException
     *
     * @param text            要搜索和替换的文本，如果为空则不执行操作
     * @param searchList      要搜索的字符串，如果为空则为no-op
     * @param replacementList 要替换它们的字符串，如果为空则为no-op
     * @return 处理了任何替换的文本，{@code null}如果输入为空字符串
     * @throws IllegalStateException    如果搜索是重复的，并且由于一个的输出是另一个的输入而存在一个无限循环
     * @throws IllegalArgumentException 如果数组的长度不相同(可以为null，大小为0)
     */
    public static String replaceEachRepeatedly(final String text, final String[] searchList,
                                               final String[] replacementList) {
        final int timeToLive = searchList == null ? 0 : searchList.length;
        return replaceEach(text, searchList, replacementList, true, timeToLive);
    }

    /**
     * 替换字符串中的指定字符串.
     * {@link #replaceEachRepeatedly(String, String[], String[])}
     * StringKit.replaceEach(null, *, *, *, *) = null
     * StringKit.replaceEach("", *, *, *, *) = ""
     * StringKit.replaceEach("aba", null, null, *, *) = "aba"
     * StringKit.replaceEach("aba", new String[0], null, *, *) = "aba"
     * StringKit.replaceEach("aba", null, new String[0], *, *) = "aba"
     * StringKit.replaceEach("aba", new String[]{"a"}, null, *, *) = "aba"
     * StringKit.replaceEach("aba", new String[]{"a"}, new String[]{""}, *, >=0) = "b"
     * StringKit.replaceEach("aba", new String[]{null}, new String[]{"a"}, *, >=0) = "aba"
     * StringKit.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}, *, >=0) = "wcte"
     * (example of how it repeats)
     * StringKit.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, false, >=0) = "dcte"
     * StringKit.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, true, >=2) = "tcte"
     * StringKit.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"}, *, *) = IllegalStateException
     *
     * @param text            要搜索和替换的文本，如果为空则不执行操作
     * @param searchList      要搜索的字符串，如果为空则为no-op
     * @param replacementList 要替换它们的字符串，如果为空则为no-op
     * @param repeat          如果为真，则重复替换，直到没有其他可能的替换或timeToLive < 0
     * @param timeToLive      如果小于0，则存在循环引用和无限循环
     * @return 处理了任何替换的文本，{@code null}如果输入为空字符串
     * @throws IllegalStateException    如果搜索是重复的，并且由于一个的输出是另一个的输入而存在一个无限循环
     * @throws IllegalArgumentException 如果数组的长度不相同(可以为null，大小为0)
     */
    private static String replaceEach(
            final String text,
            final String[] searchList,
            final String[] replacementList,
            final boolean repeat,
            final int timeToLive) {
        if (text == null || text.isEmpty() || searchList == null ||
                searchList.length == 0 || replacementList == null || replacementList.length == 0) {
            return text;
        }

        if (timeToLive < 0) {
            throw new IllegalStateException("Aborting to protect against StackOverflowError - " +
                    "output of one loop is the input of another");
        }

        final int searchLength = searchList.length;
        final int replacementLength = replacementList.length;

        if (searchLength != replacementLength) {
            throw new IllegalArgumentException("Search and Replace array lengths don't match: "
                    + searchLength
                    + " vs "
                    + replacementLength);
        }

        final boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

        int textIndex = -1;
        int replaceIndex = -1;
        int tempIndex = -1;

        for (int i = 0; i < searchLength; i++) {
            if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
                    searchList[i].isEmpty() || replacementList[i] == null) {
                continue;
            }
            tempIndex = text.indexOf(searchList[i]);

            if (tempIndex == -1) {
                noMoreMatchesForReplIndex[i] = true;
            } else {
                if (textIndex == -1 || tempIndex < textIndex) {
                    textIndex = tempIndex;
                    replaceIndex = i;
                }
            }
        }

        if (textIndex == -1) {
            return text;
        }

        int start = 0;

        int increase = 0;

        for (int i = 0; i < searchList.length; i++) {
            if (searchList[i] == null || replacementList[i] == null) {
                continue;
            }
            final int greater = replacementList[i].length() - searchList[i].length();
            if (greater > 0) {
                increase += 3 * greater;
            }
        }

        increase = Math.min(increase, text.length() / 5);

        final StringBuilder buf = new StringBuilder(text.length() + increase);

        while (textIndex != -1) {

            for (int i = start; i < textIndex; i++) {
                buf.append(text.charAt(i));
            }
            buf.append(replacementList[replaceIndex]);

            start = textIndex + searchList[replaceIndex].length();

            textIndex = -1;
            replaceIndex = -1;
            for (int i = 0; i < searchLength; i++) {
                if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
                        searchList[i].isEmpty() || replacementList[i] == null) {
                    continue;
                }
                tempIndex = text.indexOf(searchList[i], start);

                if (tempIndex == -1) {
                    noMoreMatchesForReplIndex[i] = true;
                } else {
                    if (textIndex == -1 || tempIndex < textIndex) {
                        textIndex = tempIndex;
                        replaceIndex = i;
                    }
                }
            }

        }
        final int textLength = text.length();
        for (int i = start; i < textLength; i++) {
            buf.append(text.charAt(i));
        }
        final String result = buf.toString();
        if (!repeat) {
            return result;
        }

        return replaceEach(result, searchList, replacementList, repeat, timeToLive - 1);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * This is a null-safe version of {@link String#replace(char, char)}.
     * StringKit.replaceChars(null, *, *)        = null
     * StringKit.replaceChars("", *, *)          = ""
     * StringKit.replaceChars("abcba", 'b', 'y') = "aycya"
     * StringKit.replaceChars("abcba", 'z', 'y') = "abcba"
     *
     * @param str         要替换字符的字符串，可以为空
     * @param searchChar  要搜索的字符可能为空
     * @param replaceChar 要替换的字符可以为空
     * @return 修改的字符串，{@code null}如果输入的字符串为空
     */
    public static String replaceChars(final String str, final char searchChar, final char replaceChar) {
        if (str == null) {
            return null;
        }
        return str.replace(searchChar, replaceChar);
    }

    /**
     * 替换字符串中的指定字符串.
     * <p>
     * StringKit.replaceChars(null, *, *)           = null
     * StringKit.replaceChars("", *, *)             = ""
     * StringKit.replaceChars("abc", null, *)       = "abc"
     * StringKit.replaceChars("abc", "", *)         = "abc"
     * StringKit.replaceChars("abc", "b", null)     = "ac"
     * StringKit.replaceChars("abc", "b", "")       = "ac"
     * StringKit.replaceChars("abcba", "bc", "yz")  = "ayzya"
     * StringKit.replaceChars("abcba", "bc", "y")   = "ayya"
     * StringKit.replaceChars("abcba", "bc", "yzx") = "ayzya"
     *
     * @param str          要替换字符的字符串，可以为空
     * @param searchChars  要搜索的一组字符可能为空
     * @param replaceChars 要替换的一组字符可能为空
     * @return 修改的字符串，{@code null}如果输入的字符串为空
     */
    public static String replaceChars(final String str, final String searchChars, String replaceChars) {
        if (isEmpty(str) || isEmpty(searchChars)) {
            return str;
        }
        if (replaceChars == null) {
            replaceChars = Normal.EMPTY;
        }
        boolean modified = false;
        final int replaceCharsLength = replaceChars.length();
        final int strLength = str.length();
        final StringBuilder buf = new StringBuilder(strLength);
        for (int i = 0; i < strLength; i++) {
            final char ch = str.charAt(i);
            final int index = searchChars.indexOf(ch);
            if (index >= 0) {
                modified = true;
                if (index < replaceCharsLength) {
                    buf.append(replaceChars.charAt(index));
                }
            } else {
                buf.append(ch);
            }
        }
        if (modified) {
            return buf.toString();
        }
        return str;
    }

    /**
     * 替换字符串中的指定字符串,忽略大小写
     *
     * @param str         字符串
     * @param searchStr   被查找的字符串
     * @param replacement 被替换的字符串
     * @return 替换后的字符串
     */
    public static String replaceIgnoreCase(CharSequence str, CharSequence searchStr, CharSequence replacement) {
        return replace(str, 0, searchStr, replacement, true);
    }

    /**
     * 替换字符串中的指定字符串
     *
     * @param str         字符串
     * @param searchStr   被查找的字符串
     * @param replacement 被替换的字符串
     * @return 替换后的字符串
     */
    public static String replace(CharSequence str, CharSequence searchStr, CharSequence replacement) {
        return replace(str, 0, searchStr, replacement, false);
    }

    /**
     * 替换字符串中的指定字符串
     *
     * @param str         字符串
     * @param searchStr   被查找的字符串
     * @param replacement 被替换的字符串
     * @param ignoreCase  是否忽略大小写
     * @return 替换后的字符串
     */
    public static String replace(CharSequence str, CharSequence searchStr, CharSequence replacement,
                                 boolean ignoreCase) {
        return replace(str, 0, searchStr, replacement, ignoreCase);
    }

    /**
     * 替换字符串中的指定字符串
     *
     * @param str         字符串
     * @param fromIndex   开始位置(包括)
     * @param searchStr   被查找的字符串
     * @param replacement 被替换的字符串
     * @param ignoreCase  是否忽略大小写
     * @return 替换后的字符串
     */
    public static String replace(CharSequence str, int fromIndex, CharSequence searchStr, CharSequence replacement,
                                 boolean ignoreCase) {
        if (isEmpty(str) || isEmpty(searchStr)) {
            return toString(str);
        }
        if (null == replacement) {
            replacement = Normal.EMPTY;
        }

        final int strLength = str.length();
        final int searchStrLength = searchStr.length();
        if (fromIndex > strLength) {
            return toString(str);
        } else if (fromIndex < 0) {
            fromIndex = 0;
        }

        final TextKit result = TextKit.create(strLength + 16);
        if (0 != fromIndex) {
            result.append(str.subSequence(0, fromIndex));
        }

        int preIndex = fromIndex;
        int index = fromIndex;
        while ((index = indexOf(str, searchStr, preIndex, ignoreCase)) > -1) {
            result.append(str.subSequence(preIndex, index));
            result.append(replacement);
            preIndex = index + searchStrLength;
        }

        if (preIndex < strLength) {
            // 结尾部分
            result.append(str.subSequence(preIndex, strLength));
        }
        return result.toString();
    }

    /**
     * 替换指定字符串的指定区间内字符为固定字符
     *
     * @param str          字符串
     * @param startInclude 开始位置(包含)
     * @param endExclude   结束位置(不包含)
     * @param replacedChar 被替换的字符
     * @return 替换后的字符串
     */
    public static String replace(CharSequence str, int startInclude, int endExclude, char replacedChar) {
        if (isEmpty(str)) {
            return toString(str);
        }
        final int strLength = str.length();
        if (startInclude > strLength) {
            return toString(str);
        }
        if (endExclude > strLength) {
            endExclude = strLength;
        }
        if (startInclude > endExclude) {
            // 如果起始位置大于结束位置,不替换
            return toString(str);
        }

        final char[] chars = new char[strLength];
        for (int i = 0; i < strLength; i++) {
            if (i >= startInclude && i < endExclude) {
                chars[i] = replacedChar;
            } else {
                chars[i] = str.charAt(i);
            }
        }
        return new String(chars);
    }

    /**
     * 替换指定字符串的指定区间内字符为"*"
     *
     * @param str          字符串
     * @param startInclude 开始位置(包含)
     * @param endExclude   结束位置(不包含)
     * @return 替换后的字符串
     */
    public static String hide(CharSequence str, int startInclude, int endExclude) {
        return replace(str, startInclude, endExclude, Symbol.C_STAR);
    }

    /**
     * 替换字符字符数组中所有的字符为replacedStr
     * 提供的chars为所有需要被替换的字符,例如："\r\n",则"\r"和"\n"都会被替换,哪怕他们单独存在
     *
     * @param str         被检查的字符串
     * @param chars       需要替换的字符列表,用一个字符串表示这个字符列表
     * @param replacedStr 替换成的字符串
     * @return 新字符串
     */
    public static String replaceChars(CharSequence str, String chars, CharSequence replacedStr) {
        if (isEmpty(str) || isEmpty(chars)) {
            return toString(str);
        }
        return replaceChars(str, chars.toCharArray(), replacedStr);
    }

    /**
     * 替换字符字符数组中所有的字符为replacedStr
     *
     * @param str         被检查的字符串
     * @param chars       需要替换的字符列表
     * @param replacedStr 替换成的字符串
     * @return 新字符串
     */
    public static String replaceChars(CharSequence str, char[] chars, CharSequence replacedStr) {
        if (isEmpty(str) || ArrayKit.isEmpty(chars)) {
            return toString(str);
        }

        final Set<Character> set = new HashSet<>(chars.length);
        for (char c : chars) {
            set.add(c);
        }
        int strLen = str.length();
        final StringBuilder builder = new StringBuilder();
        char c;
        for (int i = 0; i < strLen; i++) {
            c = str.charAt(i);
            builder.append(set.contains(c) ? replacedStr : c);
        }
        return builder.toString();
    }


    /**
     * 清理空白字符
     *
     * @param str 被清理的字符串
     * @return 清理后的字符串
     */
    public static String cleanBlank(CharSequence str) {
        if (str == null) {
            return null;
        }

        int len = str.length();
        final StringBuilder sb = new StringBuilder(len);
        char c;
        for (int i = 0; i < len; i++) {
            c = str.charAt(i);
            if (false == CharKit.isBlankChar(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    /**
     * 包装指定字符串
     * 当前缀和后缀一致时使用此方法
     *
     * @param str             被包装的字符串
     * @param prefixAndSuffix 前缀和后缀
     * @return 包装后的字符串
     */
    public static String wrap(CharSequence str, CharSequence prefixAndSuffix) {
        return wrap(str, prefixAndSuffix, prefixAndSuffix);
    }

    /**
     * 包装指定字符串
     *
     * @param str    被包装的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 包装后的字符串
     */
    public static String wrap(CharSequence str, CharSequence prefix, CharSequence suffix) {
        return nullToEmpty(prefix).concat(nullToEmpty(str)).concat(nullToEmpty(suffix));
    }

    /**
     * 包装多个字符串
     *
     * @param prefixAndSuffix 前缀和后缀
     * @param strs            多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAll(CharSequence prefixAndSuffix, CharSequence... strs) {
        return wrapAll(prefixAndSuffix, prefixAndSuffix, strs);
    }

    /**
     * 包装多个字符串
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @param strs   多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAll(CharSequence prefix, CharSequence suffix, CharSequence... strs) {
        final String[] results = new String[strs.length];
        for (int i = 0; i < strs.length; i++) {
            results[i] = wrap(strs[i], prefix, suffix);
        }
        return results;
    }

    /**
     * 去掉字符包装,如果未被包装则返回原字符串
     *
     * @param str    字符串
     * @param prefix 前置字符串
     * @param suffix 后置字符串
     * @return 去掉包装字符的字符串
     */
    public static String unWrap(CharSequence str, String prefix, String suffix) {
        if (isWrap(str, prefix, suffix)) {
            return sub(str, prefix.length(), str.length() - suffix.length());
        }
        return str.toString();
    }

    /**
     * 去掉字符包装,如果未被包装则返回原字符串
     *
     * @param str    字符串
     * @param prefix 前置字符
     * @param suffix 后置字符
     * @return 去掉包装字符的字符串
     */
    public static String unWrap(CharSequence str, char prefix, char suffix) {
        if (isEmpty(str)) {
            return toString(str);
        }
        if (str.charAt(0) == prefix && str.charAt(str.length() - 1) == suffix) {
            return sub(str, 1, str.length() - 1);
        }
        return str.toString();
    }

    /**
     * 去掉字符包装,如果未被包装则返回原字符串
     *
     * @param str             字符串
     * @param prefixAndSuffix 前置和后置字符
     * @return 去掉包装字符的字符串
     */
    public static String unWrap(CharSequence str, char prefixAndSuffix) {
        return unWrap(str, prefixAndSuffix, prefixAndSuffix);
    }

    /**
     * 指定字符串是否被包装
     *
     * @param str    字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 是否被包装
     */
    public static boolean isWrap(CharSequence str, String prefix, String suffix) {
        if (ArrayKit.hasNull(str, prefix, suffix)) {
            return false;
        }
        final String str2 = str.toString();
        return str2.startsWith(prefix) && str2.endsWith(suffix);
    }

    /**
     * 指定字符串是否被同一字符包装(前后都有这些字符串)
     *
     * @param str     字符串
     * @param wrapper 包装字符串
     * @return 是否被包装
     */
    public static boolean isWrap(CharSequence str, String wrapper) {
        return isWrap(str, wrapper, wrapper);
    }

    /**
     * 指定字符串是否被同一字符包装(前后都有这些字符串)
     *
     * @param str     字符串
     * @param wrapper 包装字符
     * @return 是否被包装
     */
    public static boolean isWrap(CharSequence str, char wrapper) {
        return isWrap(str, wrapper, wrapper);
    }

    /**
     * 指定字符串是否被包装
     *
     * @param str        字符串
     * @param prefixChar 前缀
     * @param suffixChar 后缀
     * @return 是否被包装
     */
    public static boolean isWrap(CharSequence str, char prefixChar, char suffixChar) {
        if (null == str) {
            return false;
        }

        return str.charAt(0) == prefixChar && str.charAt(str.length() - 1) == suffixChar;
    }

    /**
     * 包装指定字符串，如果前缀或后缀已经包含对应的字符串，则不再包装
     *
     * @param str    被包装的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 包装后的字符串
     */
    public static String wrapIfMissing(CharSequence str, CharSequence prefix, CharSequence suffix) {
        int len = 0;
        if (isNotEmpty(str)) {
            len += str.length();
        }
        if (isNotEmpty(prefix)) {
            len += str.length();
        }
        if (isNotEmpty(suffix)) {
            len += str.length();
        }
        StringBuilder sb = new StringBuilder(len);
        if (isNotEmpty(prefix) && false == startWith(str, prefix)) {
            sb.append(prefix);
        }
        if (isNotEmpty(str)) {
            sb.append(str);
        }
        if (isNotEmpty(suffix) && false == endWith(str, suffix)) {
            sb.append(suffix);
        }
        return sb.toString();
    }

    /**
     * 包装多个字符串，如果已经包装，则不再包装
     *
     * @param prefixAndSuffix 前缀和后缀
     * @param strs            多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAllIfMissing(CharSequence prefixAndSuffix, CharSequence... strs) {
        return wrapAllIfMissing(prefixAndSuffix, prefixAndSuffix, strs);
    }

    /**
     * 包装多个字符串，如果已经包装，则不再包装
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @param strs   多个字符串
     * @return 包装的字符串数组
     */
    public static String[] wrapAllIfMissing(CharSequence prefix, CharSequence suffix, CharSequence... strs) {
        final String[] results = new String[strs.length];
        for (int i = 0; i < strs.length; i++) {
            results[i] = wrapIfMissing(strs[i], prefix, suffix);
        }
        return results;
    }

    /**
     * 字符串是否以给定字符开始
     *
     * @param str 字符串
     * @param c   字符
     * @return 是否开始
     */
    public static boolean startWith(CharSequence str, char c) {
        return c == str.charAt(0);
    }

    /**
     * 是否以指定字符串开头
     * 如果给定的字符串和开头字符串都为null则返回true,否则任意一个值为null返回false
     *
     * @param str          被监测字符串
     * @param prefix       开头字符串
     * @param isIgnoreCase 是否忽略大小写
     * @return 是否以指定字符串开头
     */
    public static boolean startWith(CharSequence str, CharSequence prefix, boolean isIgnoreCase) {
        if (null == str || null == prefix) {
            return null == str && null == prefix;
        }

        if (isIgnoreCase) {
            return str.toString().toLowerCase().startsWith(prefix.toString().toLowerCase());
        } else {
            return str.toString().startsWith(prefix.toString());
        }
    }

    /**
     * 是否以指定字符串开头
     *
     * @param str    被监测字符串
     * @param prefix 开头字符串
     * @return 是否以指定字符串开头
     */
    public static boolean startWith(CharSequence str, CharSequence prefix) {
        return startWith(str, prefix, false);
    }

    /**
     * 给定字符串是否以任何一个字符串开始
     * 给定字符串和数组为空都返回false
     *
     * @param str      给定字符串
     * @param prefixes 需要检测的开始字符串
     * @return 给定字符串是否以任何一个字符串开始
     */
    public static boolean startWithAny(CharSequence str, CharSequence... prefixes) {
        if (isEmpty(str) || ArrayKit.isEmpty(prefixes)) {
            return false;
        }

        for (CharSequence suffix : prefixes) {
            if (startWith(str, suffix, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否以指定字符串开头,忽略大小写
     *
     * @param str    被监测字符串
     * @param prefix 开头字符串
     * @return 是否以指定字符串开头
     */
    public static boolean startWithIgnoreCase(CharSequence str, CharSequence prefix) {
        return startWith(str, prefix, true);
    }

    /**
     * 字符串是否以给定字符结尾
     *
     * @param str 字符串
     * @param c   字符
     * @return 是否结尾
     */
    public static boolean endWith(CharSequence str, char c) {
        return c == str.charAt(str.length() - 1);
    }

    /**
     * 是否以指定字符串结尾
     * 如果给定的字符串和开头字符串都为null则返回true,否则任意一个值为null返回false
     *
     * @param str          被监测字符串
     * @param suffix       结尾字符串
     * @param isIgnoreCase 是否忽略大小写
     * @return 是否以指定字符串结尾
     */
    public static boolean endWith(CharSequence str, CharSequence suffix, boolean isIgnoreCase) {
        if (null == str || null == suffix) {
            return null == str && null == suffix;
        }

        if (isIgnoreCase) {
            return str.toString().toLowerCase().endsWith(suffix.toString().toLowerCase());
        } else {
            return str.toString().endsWith(suffix.toString());
        }
    }

    /**
     * 是否以指定字符串结尾
     *
     * @param str    被监测字符串
     * @param suffix 结尾字符串
     * @return 是否以指定字符串结尾
     */
    public static boolean endWith(CharSequence str, CharSequence suffix) {
        return endWith(str, suffix, false);
    }

    /**
     * 给定字符串是否以任何一个字符串结尾
     * 给定字符串和数组为空都返回false
     *
     * @param str      给定字符串
     * @param suffixes 需要检测的结尾字符串
     * @return 给定字符串是否以任何一个字符串结尾
     */
    public static boolean endWithAny(CharSequence str, CharSequence... suffixes) {
        if (isEmpty(str) || ArrayKit.isEmpty(suffixes)) {
            return false;
        }

        for (CharSequence suffix : suffixes) {
            if (endWith(str, suffix, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否以指定字符串结尾,忽略大小写
     *
     * @param str    被监测字符串
     * @param suffix 结尾字符串
     * @return 是否以指定字符串结尾
     */
    public static boolean endWithIgnoreCase(CharSequence str, CharSequence suffix) {
        return endWith(str, suffix, true);
    }

    /**
     * 去除两边的指定字符串
     *
     * @param str            被处理的字符串
     * @param prefixOrSuffix 前缀或后缀
     * @return 处理后的字符串
     */
    public static String strip(CharSequence str, CharSequence prefixOrSuffix) {
        return strip(str, prefixOrSuffix, prefixOrSuffix);
    }

    /**
     * 去除两边的指定字符串
     *
     * @param str    被处理的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 处理后的字符串
     */
    public static String strip(CharSequence str, CharSequence prefix, CharSequence suffix) {
        if (isEmpty(str)) {
            return toString(str);
        }
        int from = 0;
        int to = str.length();

        String str2 = str.toString();
        if (startWith(str2, prefix)) {
            from = prefix.length();
        }
        if (endWith(str2, suffix)) {
            to -= suffix.length();
        }
        return str2.substring(from, to);
    }

    /**
     * 去除两边的指定字符串,忽略大小写
     *
     * @param str            被处理的字符串
     * @param prefixOrSuffix 前缀或后缀
     * @return 处理后的字符串
     */
    public static String stripIgnoreCase(CharSequence str, CharSequence prefixOrSuffix) {
        return stripIgnoreCase(str, prefixOrSuffix, prefixOrSuffix);
    }

    /**
     * 去除两边的指定字符串,忽略大小写
     *
     * @param str    被处理的字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 处理后的字符串
     */
    public static String stripIgnoreCase(CharSequence str, CharSequence prefix, CharSequence suffix) {
        if (isEmpty(str)) {
            return toString(str);
        }
        int from = 0;
        int to = str.length();

        String str2 = str.toString();
        if (startWithIgnoreCase(str2, prefix)) {
            from = prefix.length();
        }
        if (endWithIgnoreCase(str2, suffix)) {
            to -= suffix.length();
        }
        return str2.substring(from, to);
    }

    /**
     * 如果给定字符串不是以prefix开头的,在开头补充 prefix
     *
     * @param str    字符串
     * @param prefix 前缀
     * @return 补充后的字符串
     */
    public static String addPrefixIfNot(CharSequence str, CharSequence prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return toString(str);
        }

        final String str2 = str.toString();
        final String prefix2 = prefix.toString();
        if (false == str2.startsWith(prefix2)) {
            return prefix2.concat(str2);
        }
        return str2;
    }

    /**
     * 如果给定字符串不是以suffix结尾的,在尾部补充 suffix
     *
     * @param str    字符串
     * @param suffix 后缀
     * @return 补充后的字符串
     */
    public static String addSuffixIfNot(CharSequence str, CharSequence suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return toString(str);
        }

        final String str2 = str.toString();
        final String suffix2 = suffix.toString();
        if (false == str2.endsWith(suffix2)) {
            return str2.concat(suffix2);
        }
        return str2;
    }

    /**
     * 指定字符是否在字符串中出现过
     *
     * @param str        字符串
     * @param searchChar 被查找的字符
     * @return 是否包含
     */
    public static boolean contains(CharSequence str, char searchChar) {
        return indexOf(str, searchChar) > -1;
    }

    /**
     * 指定字符串是否在字符串中出现过
     *
     * @param str       字符串
     * @param searchStr 被查找的字符串
     * @return 是否包含
     */
    public static boolean contains(CharSequence str, CharSequence searchStr) {
        if (null == str || null == searchStr) {
            return false;
        }
        return str.toString().contains(searchStr);
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串
     *
     * @param str      指定字符串
     * @param testStrs 需要检查的字符串数组
     * @return 是否包含任意一个字符串
     */
    public static boolean containsAny(CharSequence str, CharSequence... testStrs) {
        return null != getContainsAny(str, testStrs);
    }

    /**
     * 查找指定字符串是否包含指定字符列表中的任意一个字符
     *
     * @param str       指定字符串
     * @param testChars 需要检查的字符数组
     * @return 是否包含任意一个字符
     */
    public static boolean containsAny(CharSequence str, char... testChars) {
        if (false == isEmpty(str)) {
            int len = str.length();
            for (int i = 0; i < len; i++) {
                if (ArrayKit.contains(testChars, str.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查指定字符串中是否只包含给定的字符
     *
     * @param str       字符串
     * @param testChars 检查的字符
     * @return 字符串含有非检查的字符, 返回false
     */
    public static boolean containsOnly(CharSequence str, char... testChars) {
        if (false == isEmpty(str)) {
            int len = str.length();
            for (int i = 0; i < len; i++) {
                if (false == ArrayKit.contains(testChars, str.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 给定字符串是否包含空白符(空白符包括空格、制表符、全角空格和不间断空格)
     * 如果给定字符串为null或者"",则返回false
     *
     * @param str 字符串
     * @return 是否包含空白符
     */
    public static boolean containsBlank(CharSequence str) {
        if (null == str) {
            return false;
        }
        final int length = str.length();
        if (0 == length) {
            return false;
        }

        for (int i = 0; i < length; i += 1) {
            if (CharKit.isBlankChar(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串,如果包含返回找到的第一个字符串
     *
     * @param str      指定字符串
     * @param testStrs 需要检查的字符串数组
     * @return 被包含的第一个字符串
     */
    public static String getContainsAny(CharSequence str, CharSequence... testStrs) {
        if (isEmpty(str) || ArrayKit.isEmpty(testStrs)) {
            return null;
        }
        for (CharSequence checkStr : testStrs) {
            if (str.toString().contains(checkStr)) {
                return checkStr.toString();
            }
        }
        return null;
    }

    /**
     * 是否包含特定字符,忽略大小写,如果给定两个参数都为<code>null</code>,返回true
     *
     * @param str     被检测字符串
     * @param testStr 被测试是否包含的字符串
     * @return 是否包含
     */
    public static boolean containsIgnoreCase(CharSequence str, CharSequence testStr) {
        if (null == str) {
            // 如果被监测字符串和
            return null == testStr;
        }
        return str.toString().toLowerCase().contains(testStr.toString().toLowerCase());
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串
     * 忽略大小写
     *
     * @param str      指定字符串
     * @param testStrs 需要检查的字符串数组
     * @return 是否包含任意一个字符串
     */
    public static boolean containsAnyIgnoreCase(CharSequence str, CharSequence... testStrs) {
        return null != getContainsStrIgnoreCase(str, testStrs);
    }

    /**
     * 查找指定字符串是否包含指定字符串列表中的任意一个字符串,如果包含返回找到的第一个字符串
     * 忽略大小写
     *
     * @param str      指定字符串
     * @param testStrs 需要检查的字符串数组
     * @return 被包含的第一个字符串
     */
    public static String getContainsStrIgnoreCase(CharSequence str, CharSequence... testStrs) {
        if (isEmpty(str) || ArrayKit.isEmpty(testStrs)) {
            return null;
        }
        for (CharSequence testStr : testStrs) {
            if (containsIgnoreCase(str, testStr)) {
                return testStr.toString();
            }
        }
        return null;
    }

    /**
     * 给定字符串是否被字符包围
     *
     * @param str    字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 是否包围, 空串不包围
     */
    public static boolean isSurround(CharSequence str, CharSequence prefix, CharSequence suffix) {
        if (isBlank(str)) {
            return false;
        }
        if (str.length() < (prefix.length() + suffix.length())) {
            return false;
        }

        final String str2 = str.toString();
        return str2.startsWith(prefix.toString()) && str2.endsWith(suffix.toString());
    }

    /**
     * 给定字符串是否被字符包围
     *
     * @param str    字符串
     * @param prefix 前缀
     * @param suffix 后缀
     * @return 是否包围, 空串不包围
     */
    public static boolean isSurround(CharSequence str, char prefix, char suffix) {
        if (isBlank(str)) {
            return false;
        }
        if (str.length() < 2) {
            return false;
        }

        return str.charAt(0) == prefix && str.charAt(str.length() - 1) == suffix;
    }

    /**
     * 向右填充指定字符的字符串
     *
     * <pre>
     * StringKit.rightPad(null, *, *)      = null
     * StringKit.rightPad("", 3, "z")      = "zzz"
     * StringKit.rightPad("bat", 3, "yz")  = "bat"
     * StringKit.rightPad("bat", 5, "yz")  = "batyz"
     * StringKit.rightPad("bat", 8, "yz")  = "batyzyzy"
     * StringKit.rightPad("bat", 1, "yz")  = "bat"
     * StringKit.rightPad("bat", -1, "yz") = "bat"
     * StringKit.rightPad("bat", 5, null)  = "bat  "
     * StringKit.rightPad("bat", 5, "")    = "bat  "
     * </pre>
     *
     * @param str    要填充的字符串可能为空
     * @param size   字符大小
     * @param padStr 要填充的字符
     * @return 右填充字符串或原始字符串如果不需要填充，{@code null}如果输入为空字符串
     */
    public static String rightPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = Symbol.SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str;
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return rightPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return str.concat(padStr);
        } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return str.concat(new String(padding));
        }
    }


    /**
     * 向右填充指定字符的字符串
     *
     * <pre>
     * StringKit.rightPad(null, *, *)     = null
     * StringKit.rightPad("", 3, 'z')     = "zzz"
     * StringKit.rightPad("bat", 3, 'z')  = "bat"
     * StringKit.rightPad("bat", 5, 'z')  = "batzz"
     * StringKit.rightPad("bat", 1, 'z')  = "bat"
     * StringKit.rightPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str     要填充的字符串可能为空
     * @param size    字符大小
     * @param padChar 要填充的字符
     * @return 右填充字符串或原始字符串如果不需要填充，{@code null}如果输入为空字符串
     */
    public static String rightPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        if (pads > PAD_LIMIT) {
            return rightPad(str, size, String.valueOf(padChar));
        }
        return str.concat(repeat(padChar, pads));
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
     * @param str 要从中获取字符的字符串可能为空
     * @param len 所需字符串的长度
     * @return 最左边的字符，{@code null}如果输入为空字符串
     */
    public static String left(final String str, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return Normal.EMPTY;
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(0, len);
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
     * @param str 要从中获取字符的字符串可能为空
     * @param len 所需字符串的长度
     * @return 最右边的字符，{@code null}如果输入为空字符串
     */
    public static String right(final String str, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return Normal.EMPTY;
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(str.length() - len);
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
     * @param str 要从中获取字符的字符串可能为空
     * @param pos 开始时的位置，负为零
     * @param len 所需字符串的长度
     * @return 中间的字符，{@code null}如果输入为空字符串
     */
    public static String mid(final String str, int pos, final int len) {
        if (str == null) {
            return null;
        }
        if (len < 0 || pos > str.length()) {
            return Normal.EMPTY;
        }
        if (pos < 0) {
            pos = 0;
        }
        if (str.length() <= pos + len) {
            return str.substring(pos);
        }
        return str.substring(pos, pos + len);
    }

    /**
     * 用指定的字符串填充一个字符串
     *
     * <pre>
     * StringKit.leftPad(null, *)   = null
     * StringKit.leftPad("", 3)     = "   "
     * StringKit.leftPad("bat", 3)  = "bat"
     * StringKit.leftPad("bat", 5)  = "  bat"
     * StringKit.leftPad("bat", 1)  = "bat"
     * StringKit.leftPad("bat", -1) = "bat"
     * </pre>
     *
     * @param str  要填充的字符串可能为空
     * @param size 字符大小
     * @return 左填充字符串或原始字符串如果不需要填充，{@code null}如果输入为空字符串
     */
    public static String leftPad(final String str, final int size) {
        return leftPad(str, size, Symbol.C_SPACE);
    }

    /**
     * 用指定的字符串填充一个字符串
     *
     * <pre>
     * StringKit.leftPad(null, *, *)     = null
     * StringKit.leftPad("", 3, 'z')     = "zzz"
     * StringKit.leftPad("bat", 3, 'z')  = "bat"
     * StringKit.leftPad("bat", 5, 'z')  = "zzbat"
     * StringKit.leftPad("bat", 1, 'z')  = "bat"
     * StringKit.leftPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str     要填充的字符串可能为空
     * @param size    字符大小
     * @param padChar 要填充的字符
     * @return 左填充字符串或原始字符串如果不需要填充，{@code null}如果输入为空字符串
     */
    public static String leftPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        if (pads > PAD_LIMIT) {
            return leftPad(str, size, String.valueOf(padChar));
        }
        return repeat(padChar, pads).concat(str);
    }

    /**
     * 用指定的字符串填充一个字符串
     *
     * <pre>
     * StringKit.leftPad(null, *, *)      = null
     * StringKit.leftPad("", 3, "z")      = "zzz"
     * StringKit.leftPad("bat", 3, "yz")  = "bat"
     * StringKit.leftPad("bat", 5, "yz")  = "yzbat"
     * StringKit.leftPad("bat", 8, "yz")  = "yzyzybat"
     * StringKit.leftPad("bat", 1, "yz")  = "bat"
     * StringKit.leftPad("bat", -1, "yz") = "bat"
     * StringKit.leftPad("bat", 5, null)  = "  bat"
     * StringKit.leftPad("bat", 5, "")    = "  bat"
     * </pre>
     *
     * @param str    要填充的字符串可能为空
     * @param size   大小
     * @param padStr 要填充的字符串，null或empty被视为单个空格
     * @return 左填充字符串或原始字符串如果不需要填充，{@code null}如果输入为空字符串
     */
    public static String leftPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = Symbol.SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str;
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }

    /**
     * 获取字符串的长度,如果为null返回0
     *
     * @param cs a 字符串
     * @return 字符串的长度, 如果为null返回0
     */
    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /**
     * 构建新的字符串
     *
     * @param original     原始对象
     * @param middle       中间隐藏信息
     * @param prefixLength 前边信息长度
     * @return 构建后的新字符串
     */
    public static String buildString(final Object original,
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
     * @param strs          字符串数组
     * @return 连接后的字符串
     */
    public static String concat(boolean isNullToEmpty, CharSequence... strs) {
        final StrBuilder sb = new StrBuilder();
        for (CharSequence str : strs) {
            sb.append(isNullToEmpty ? nullToEmpty(str) : str);
        }
        return sb.toString();
    }

    /**
     * 给定字符串中的字母是否全部为大写,判断依据如下：
     *
     * <pre>
     * 1. 大写字母包括A-Z
     * 2. 其它非字母的Unicode符都算作大写
     * </pre>
     *
     * @param str 被检查的字符串
     * @return 是否全部为大写
     */
    public static boolean isUpperCase(CharSequence str) {
        if (null == str) {
            return false;
        }
        final int len = str.length();
        for (int i = 0; i < len; i++) {
            if (Character.isLowerCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 给定字符串中的字母是否全部为小写,判断依据如下：
     *
     * <pre>
     * 1. 小写字母包括a-z
     * 2. 其它非字母的Unicode符都算作小写
     * </pre>
     *
     * @param str 被检查的字符串
     * @return 是否全部为小写
     */
    public static boolean isLowerCase(CharSequence str) {
        if (null == str) {
            return false;
        }
        final int len = str.length();
        for (int i = 0; i < len; i++) {
            if (Character.isUpperCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 给定字符串转为bytes后的byte数(byte长度)
     *
     * @param cs      字符串
     * @param charset 编码
     * @return byte长度
     */
    public static int byteLength(CharSequence cs, java.nio.charset.Charset charset) {
        return cs == null ? 0 : cs.toString().getBytes(charset).length;
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
     * @param str 字符串
     * @return 交换后的字符串
     */
    public static String swapCase(final String str) {
        if (isEmpty(str)) {
            return str;
        }

        final char[] buffer = str.toCharArray();

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
     * @param str        被填充的字符串
     * @param filledChar 填充的字符
     * @param len        填充长度
     * @return 填充后的字符串
     */
    public static String fillBefore(String str, char filledChar, int len) {
        return fill(str, filledChar, len, true);
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

        String filled = repeat(charVal, len - strLen);
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
     * 创建StringBuilder对象
     *
     * @return StringBuilder对象
     */
    public static StringBuilder builder() {
        return new StringBuilder();
    }

    /**
     * 创建StrBuilder对象
     *
     * @return StrBuilder对象
     */
    public static StrBuilder strBuilder() {
        return new StrBuilder();
    }

    /**
     * 创建StringBuilder对象
     *
     * @param capacity 初始大小
     * @return StringBuilder对象
     */
    public static StringBuilder builder(int capacity) {
        return new StringBuilder(capacity);
    }

    /**
     * 创建StrBuilder对象
     *
     * @param capacity 初始大小
     * @return StrBuilder对象
     */
    public static StrBuilder strBuilder(int capacity) {
        return new StrBuilder(capacity);
    }

    /**
     * 创建StringBuilder对象
     *
     * @param strs 初始字符串列表
     * @return StringBuilder对象
     */
    public static StringBuilder builder(CharSequence... strs) {
        final StringBuilder sb = new StringBuilder();
        for (CharSequence str : strs) {
            sb.append(str);
        }
        return sb;
    }

    /**
     * 创建StrBuilder对象
     *
     * @param strs 初始字符串列表
     * @return StrBuilder对象
     */
    public static StrBuilder strBuilder(CharSequence... strs) {
        return new StrBuilder(strs);
    }

    /**
     * 获得StringReader
     *
     * @param str 字符串
     * @return StringReader
     */
    public static StringReader getReader(CharSequence str) {
        if (null == str) {
            return null;
        }
        return new StringReader(str.toString());
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
     * 统计指定内容中包含指定字符串的数量
     * 参数为 {@code null} 或者 "" 返回 {@code 0}.
     *
     * <pre>
     * StringKit.count(null, *)       = 0
     * StringKit.count("", *)         = 0
     * StringKit.count("abba", null)  = 0
     * StringKit.count("abba", "")    = 0
     * StringKit.count("abba", "a")   = 2
     * StringKit.count("abba", "ab")  = 1
     * StringKit.count("abba", "xxx") = 0
     * </pre>
     *
     * @param content      被查找的字符串
     * @param strForSearch 需要查找的字符串
     * @return 查找到的个数
     */
    public static int count(CharSequence content, CharSequence strForSearch) {
        if (hasEmpty(content, strForSearch) || strForSearch.length() > content.length()) {
            return 0;
        }

        int count = 0;
        int idx = 0;
        final String content2 = content.toString();
        final String strForSearch2 = strForSearch.toString();
        while ((idx = content2.indexOf(strForSearch2, idx)) > -1) {
            count++;
            idx += strForSearch.length();
        }
        return count;
    }

    /**
     * 统计指定内容中包含指定字符的数量
     *
     * @param content       内容
     * @param charForSearch 被统计的字符
     * @return 包含数量
     */
    public static int count(CharSequence content, char charForSearch) {
        int count = 0;
        if (isEmpty(content)) {
            return 0;
        }
        int contentLength = content.length();
        for (int i = 0; i < contentLength; i++) {
            if (charForSearch == content.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 将字符串切分为N等份
     *
     * @param str        字符串
     * @param partLength 每等份的长度
     * @return 切分后的数组
     */
    public static String[] cut(CharSequence str, int partLength) {
        if (null == str) {
            return null;
        }
        int len = str.length();
        if (len < partLength) {
            return new String[]{str.toString()};
        }
        int part = MathKit.count(len, partLength);
        final String[] array = new String[part];

        final String str2 = str.toString();
        for (int i = 0; i < part; i++) {
            array[i] = str2.substring(i * partLength, (i == part - 1) ? len : (partLength + i * partLength));
        }
        return array;
    }

    /**
     * 将给定字符串,变成 "xxx...xxx" 形式的字符串
     *
     * @param str       字符串
     * @param maxLength 最大长度
     * @return 截取后的字符串
     */
    public static String brief(CharSequence str, int maxLength) {
        if (null == str) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str.toString();
        }
        int w = maxLength / 2;
        int l = str.length() + 3;

        final String str2 = str.toString();
        return format("{}...{}", str2.substring(0, maxLength - w), str2.substring(l - w));
    }

    /**
     * 首字母变小写
     *
     * @param str 字符串
     * @return {String}
     */
    public static String firstCharToLower(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            char[] arr = str.toCharArray();
            arr[0] += ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 首字母变大写
     *
     * @param str 字符串
     * @return {String}
     */
    public static String firstCharToUpper(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'a' && firstChar <= 'z') {
            char[] arr = str.toCharArray();
            arr[0] -= ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    /**
     * 清理字符串,清理出某些不可见字符和一些sql特殊字符
     *
     * @param txt 文本
     * @return {String}
     */
    public static String cleanText(String txt) {
        if (txt == null) {
            return null;
        }
        return Pattern.compile("[`'\"|/,;()-+*%#·•�　\\s]").matcher(txt).replaceAll(Normal.EMPTY);
    }

    /**
     * 获取标识符,用于参数清理
     *
     * @param param 参数
     * @return 清理后的标识符
     */
    public static String cleanIdentifier(String param) {
        if (param == null) {
            return null;
        }
        StringBuilder paramBuilder = new StringBuilder();
        for (int i = 0; i < param.length(); i++) {
            char c = param.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                paramBuilder.append(c);
            }
        }
        return paramBuilder.toString();
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
     * @param str 以大写字母表示的字符串可以为空
     * @return 大写字符串{@code null}如果输入为空字符串
     */
    public static String upperCase(final String str) {
        if (str == null) {
            return null;
        }
        return str.toUpperCase();
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
     * @param str    以大写字母表示的字符串可以为空
     * @param locale 定义案例转换规则的区域设置不能为空
     * @return 大写字符串{@code null}如果输入为空字符串
     */
    public static String upperCase(final String str, final Locale locale) {
        if (str == null) {
            return null;
        }
        return str.toUpperCase(locale);
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
     * @param str 小写字符串可以为空
     * @return 小写字符串{@code null}如果输入为空字符串
     */
    public static String lowerCase(final String str) {
        if (str == null) {
            return null;
        }
        return str.toLowerCase();
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
     * @param str    小写字符串可以为空
     * @param locale t定义案例转换规则的区域设置不能为空
     * @return 小写字符串{@code null}如果输入为空字符串
     */
    public static String lowerCase(final String str, final Locale locale) {
        if (str == null) {
            return null;
        }
        return str.toLowerCase(locale);
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
     * @param str 要大写的字符串可以为空
     * @return 大写字符串，{@code null}如果输入为空字符串
     */
    public static String capitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        final int firstCodepoint = str.codePointAt(0);
        final int newCodePoint = Character.toTitleCase(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            return str;
        }

        final int newCodePoints[] = new int[strLen];
        int outOffset = 0;
        newCodePoints[outOffset++] = newCodePoint;
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
            final int codepoint = str.codePointAt(inOffset);
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
     * @param str 要取消大写的字符串可以为空
     * @return 未大写的字符串，{@code null}如果输入为空字符串
     */
    public static String uncapitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        final int firstCodepoint = str.codePointAt(0);
        final int newCodePoint = Character.toLowerCase(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            return str;
        }

        final int newCodePoints[] = new int[strLen];
        int outOffset = 0;
        newCodePoints[outOffset++] = newCodePoint;
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
            final int codepoint = str.codePointAt(inOffset);
            newCodePoints[outOffset++] = codepoint;
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

    /**
     * 检查CharSequence是否以提供的大小写敏感的后缀结尾.
     *
     * <pre>
     * StringKit.endsWithAny(null, null)      = false
     * StringKit.endsWithAny(null, new String[] {"abc"})  = false
     * StringKit.endsWithAny("abcxyz", null)     = false
     * StringKit.endsWithAny("abcxyz", new String[] {""}) = true
     * StringKit.endsWithAny("abcxyz", new String[] {"xyz"}) = true
     * StringKit.endsWithAny("abcxyz", new String[] {null, "xyz", "abc"}) = true
     * StringKit.endsWithAny("abcXYZ", "def", "XYZ") = true
     * StringKit.endsWithAny("abcXYZ", "def", "xyz") = false
     * </pre>
     *
     * @param sequence      要检查的CharSequence可能为空
     * @param searchStrings 要查找的区分大小写的字符序列可以是空的，也可以包含{@code null}
     * @return {如果输入{@code sequence}是{@code null}， 并且没有提供{@code searchstring}，
     * 或者输入{@code sequence}以提供的区分大小写的{@code searchstring}结尾.
     */
    public static boolean endsWithAny(final CharSequence sequence, final CharSequence... searchStrings) {
        if (isEmpty(sequence) || ArrayKit.isEmpty(searchStrings)) {
            return false;
        }
        for (final CharSequence searchString : searchStrings) {
            if (endWith(sequence, searchString)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 如果字符串还没有以后缀结尾，则将后缀追加到字符串的末尾.
     *
     * @param str        字符串.
     * @param suffix     附加到字符串末尾的后缀.
     * @param ignoreCase 指示比较是否应忽略大小写.
     * @param suffixes   有效终止符的附加后缀(可选).
     * @return 如果添加了后缀，则为新字符串，否则为相同的字符串.
     */
    private static String appendIfMissing(final String str, final CharSequence suffix, final boolean ignoreCase,
                                          final CharSequence... suffixes) {
        if (str == null || isEmpty(suffix) || endWith(str, suffix, ignoreCase)) {
            return toString(str);
        }
        if (suffixes != null && suffixes.length > 0) {
            for (final CharSequence s : suffixes) {
                if (endWith(str, s, ignoreCase)) {
                    return str;
                }
            }
        }
        return str.concat(suffix.toString());
    }

    /**
     * 如果字符串还没有以任何后缀结尾，则将后缀追加到字符串的末尾.
     *
     * <pre>
     * StringKit.appendIfMissing(null, null) = null
     * StringKit.appendIfMissing("abc", null) = "abc"
     * StringKit.appendIfMissing("", "xyz") = "xyz"
     * StringKit.appendIfMissing("abc", "xyz") = "abcxyz"
     * StringKit.appendIfMissing("abcxyz", "xyz") = "abcxyz"
     * StringKit.appendIfMissing("abcXYZ", "xyz") = "abcXYZxyz"
     * </pre>
     * <p>With additional suffixes,</p>
     * <pre>
     * StringKit.appendIfMissing(null, null, null) = null
     * StringKit.appendIfMissing("abc", null, null) = "abc"
     * StringKit.appendIfMissing("", "xyz", null) = "xyz"
     * StringKit.appendIfMissing("abc", "xyz", new CharSequence[]{null}) = "abcxyz"
     * StringKit.appendIfMissing("abc", "xyz", "") = "abc"
     * StringKit.appendIfMissing("abc", "xyz", "mno") = "abcxyz"
     * StringKit.appendIfMissing("abcxyz", "xyz", "mno") = "abcxyz"
     * StringKit.appendIfMissing("abcmno", "xyz", "mno") = "abcmno"
     * StringKit.appendIfMissing("abcXYZ", "xyz", "mno") = "abcXYZxyz"
     * StringKit.appendIfMissing("abcMNO", "xyz", "mno") = "abcMNOxyz"
     * </pre>
     *
     * @param str      字符串.
     * @param suffix   附加到字符串末尾的后缀.
     * @param suffixes 有效终止符的附加后缀(可选).
     * @return 如果添加了后缀，则为新字符串，否则为相同的字符串.
     */
    public static String appendIfMissing(final String str, final CharSequence suffix, final CharSequence...
            suffixes) {
        return appendIfMissing(str, suffix, false, suffixes);
    }

    /**
     * 如果字符串还没有结束，则使用任何后缀将后缀追加到字符串的末尾，不区分大小写.
     *
     * <pre>
     * StringKit.appendIfMissingIgnoreCase(null, null) = null
     * StringKit.appendIfMissingIgnoreCase("abc", null) = "abc"
     * StringKit.appendIfMissingIgnoreCase("", "xyz") = "xyz"
     * StringKit.appendIfMissingIgnoreCase("abc", "xyz") = "abcxyz"
     * StringKit.appendIfMissingIgnoreCase("abcxyz", "xyz") = "abcxyz"
     * StringKit.appendIfMissingIgnoreCase("abcXYZ", "xyz") = "abcXYZ"
     * </pre>
     * <p>With additional suffixes,</p>
     * <pre>
     * StringKit.appendIfMissingIgnoreCase(null, null, null) = null
     * StringKit.appendIfMissingIgnoreCase("abc", null, null) = "abc"
     * StringKit.appendIfMissingIgnoreCase("", "xyz", null) = "xyz"
     * StringKit.appendIfMissingIgnoreCase("abc", "xyz", new CharSequence[]{null}) = "abcxyz"
     * StringKit.appendIfMissingIgnoreCase("abc", "xyz", "") = "abc"
     * StringKit.appendIfMissingIgnoreCase("abc", "xyz", "mno") = "axyz"
     * StringKit.appendIfMissingIgnoreCase("abcxyz", "xyz", "mno") = "abcxyz"
     * StringKit.appendIfMissingIgnoreCase("abcmno", "xyz", "mno") = "abcmno"
     * StringKit.appendIfMissingIgnoreCase("abcXYZ", "xyz", "mno") = "abcXYZ"
     * StringKit.appendIfMissingIgnoreCase("abcMNO", "xyz", "mno") = "abcMNO"
     * </pre>
     *
     * @param str      字符串.
     * @param suffix   附加到字符串末尾的后缀.
     * @param suffixes 有效终止符的附加后缀(可选).
     * @return 如果添加了后缀，则为新字符串，否则为相同的字符串.
     */
    public static String appendIfMissingIgnoreCase(final String str, final CharSequence suffix, final CharSequence...
            suffixes) {
        return appendIfMissing(str, suffix, true, suffixes);
    }

    /**
     * 如果字符串还没有以任何前缀开始，则将前缀添加到字符串的开头.
     *
     * @param str        字符串.
     * @param prefix     在字符串开始前的前缀.
     * @param ignoreCase 指示比较是否应忽略大小写.
     * @param prefixes   有效的附加前缀(可选).
     * @return 如果前缀是前缀，则为新字符串，否则为相同的字符串.
     */
    private static String prependIfMissing(final String str, final CharSequence prefix, final boolean ignoreCase,
                                           final CharSequence... prefixes) {
        if (str == null || isEmpty(prefix) || startWith(str, prefix, ignoreCase)) {
            return toString(str);
        }
        if (prefixes != null && prefixes.length > 0) {
            for (final CharSequence s : prefixes) {
                if (startWith(str, s, ignoreCase)) {
                    return str;
                }
            }
        }
        return prefix.toString().concat(str);
    }

    /**
     * 如果字符串还没有以任何前缀开始，则将前缀添加到字符串的开头.
     *
     * <pre>
     * StringKit.prependIfMissing(null, null) = null
     * StringKit.prependIfMissing("abc", null) = "abc"
     * StringKit.prependIfMissing("", "xyz") = "xyz"
     * StringKit.prependIfMissing("abc", "xyz") = "xyzabc"
     * StringKit.prependIfMissing("xyzabc", "xyz") = "xyzabc"
     * StringKit.prependIfMissing("XYZabc", "xyz") = "xyzXYZabc"
     * </pre>
     * <p>With additional prefixes,</p>
     * <pre>
     * StringKit.prependIfMissing(null, null, null) = null
     * StringKit.prependIfMissing("abc", null, null) = "abc"
     * StringKit.prependIfMissing("", "xyz", null) = "xyz"
     * StringKit.prependIfMissing("abc", "xyz", new CharSequence[]{null}) = "xyzabc"
     * StringKit.prependIfMissing("abc", "xyz", "") = "abc"
     * StringKit.prependIfMissing("abc", "xyz", "mno") = "xyzabc"
     * StringKit.prependIfMissing("xyzabc", "xyz", "mno") = "xyzabc"
     * StringKit.prependIfMissing("mnoabc", "xyz", "mno") = "mnoabc"
     * StringKit.prependIfMissing("XYZabc", "xyz", "mno") = "xyzXYZabc"
     * StringKit.prependIfMissing("MNOabc", "xyz", "mno") = "xyzMNOabc"
     * </pre>
     *
     * @param str      T字符串.
     * @param prefix   在字符串开始前的前缀.
     * @param prefixes 有效的附加前缀(可选).
     * @return 如果前缀是前缀，则为新字符串，否则为相同的字符串.
     */
    public static String prependIfMissing(final String str, final CharSequence prefix, final CharSequence...
            prefixes) {
        return prependIfMissing(str, prefix, false, prefixes);
    }

    /**
     * 如果字符串尚未开始，则将前缀添加到字符串的开头，不区分大小写，并使用任何前缀.
     *
     * <pre>
     * StringKit.prependIfMissingIgnoreCase(null, null) = null
     * StringKit.prependIfMissingIgnoreCase("abc", null) = "abc"
     * StringKit.prependIfMissingIgnoreCase("", "xyz") = "xyz"
     * StringKit.prependIfMissingIgnoreCase("abc", "xyz") = "xyzabc"
     * StringKit.prependIfMissingIgnoreCase("xyzabc", "xyz") = "xyzabc"
     * StringKit.prependIfMissingIgnoreCase("XYZabc", "xyz") = "XYZabc"
     * </pre>
     * <p>With additional prefixes,</p>
     * <pre>
     * StringKit.prependIfMissingIgnoreCase(null, null, null) = null
     * StringKit.prependIfMissingIgnoreCase("abc", null, null) = "abc"
     * StringKit.prependIfMissingIgnoreCase("", "xyz", null) = "xyz"
     * StringKit.prependIfMissingIgnoreCase("abc", "xyz", new CharSequence[]{null}) = "xyzabc"
     * StringKit.prependIfMissingIgnoreCase("abc", "xyz", "") = "abc"
     * StringKit.prependIfMissingIgnoreCase("abc", "xyz", "mno") = "xyzabc"
     * StringKit.prependIfMissingIgnoreCase("xyzabc", "xyz", "mno") = "xyzabc"
     * StringKit.prependIfMissingIgnoreCase("mnoabc", "xyz", "mno") = "mnoabc"
     * StringKit.prependIfMissingIgnoreCase("XYZabc", "xyz", "mno") = "XYZabc"
     * StringKit.prependIfMissingIgnoreCase("MNOabc", "xyz", "mno") = "MNOabc"
     * </pre>
     *
     * @param str      T字符串.
     * @param prefix   在字符串开始前的前缀.
     * @param prefixes 有效的附加前缀(可选).
     * @return 如果前缀是前缀，则为新字符串，否则为相同的字符串.
     */
    public static String prependIfMissingIgnoreCase(final String str, final CharSequence prefix,
                                                    final CharSequence... prefixes) {
        return prependIfMissing(str, prefix, true, prefixes);
    }

    /**
     * 限制字符串长度，如果超过指定长度，截取指定长度并在末尾加"..."
     *
     * @param string 字符串
     * @param length 最大长度
     * @return 切割后的剩余的前半部分字符串+"..."
     */
    public static String maxLength(CharSequence string, int length) {
        Assert.isTrue(length > 0);
        if (null == string) {
            return null;
        }
        if (string.length() <= length) {
            return string.toString();
        }
        return sub(string, 0, length) + "...";
    }

    /**
     * 居中字符串，两边补充指定字符串，如果指定长度小于字符串，则返回原字符串
     *
     * <pre>
     * StringKit.center(null, *)   = null
     * StringKit.center("", 4)     = "    "
     * StringKit.center("ab", -1)  = "ab"
     * StringKit.center("ab", 4)   = " ab "
     * StringKit.center("abcd", 2) = "abcd"
     * StringKit.center("a", 4)    = " a  "
     * </pre>
     *
     * @param str  字符串
     * @param size 指定长度
     * @return 补充后的字符串
     */
    public static String center(CharSequence str, final int size) {
        return center(str, size, Symbol.C_SPACE);
    }

    /**
     * 居中字符串，两边补充指定字符串，如果指定长度小于字符串，则返回原字符串
     *
     * <pre>
     * StringKit.center(null, *, *)     = null
     * StringKit.center("", 4, ' ')     = "    "
     * StringKit.center("ab", -1, ' ')  = "ab"
     * StringKit.center("ab", 4, ' ')   = " ab "
     * StringKit.center("abcd", 2, ' ') = "abcd"
     * StringKit.center("a", 4, ' ')    = " a  "
     * StringKit.center("a", 4, 'y')   = "yayy"
     * StringKit.center("abc", 7, ' ')   = "  abc  "
     * </pre>
     *
     * @param str     字符串
     * @param size    指定长度
     * @param padChar 两边补充的字符
     * @return 补充后的字符串
     */
    public static String center(CharSequence str, final int size, char padChar) {
        if (str == null || size <= 0) {
            return toString(str);
        }
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str.toString();
        }
        str = padPre(str, strLen + pads / 2, padChar);
        str = padAfter(str, size, padChar);
        return str.toString();
    }

    /**
     * 居中字符串，两边补充指定字符串，如果指定长度小于字符串，则返回原字符串
     *
     * <pre>
     * StringKit.center(null, *, *)     = null
     * StringKit.center("", 4, " ")     = "    "
     * StringKit.center("ab", -1, " ")  = "ab"
     * StringKit.center("ab", 4, " ")   = " ab "
     * StringKit.center("abcd", 2, " ") = "abcd"
     * StringKit.center("a", 4, " ")    = " a  "
     * StringKit.center("a", 4, "yz")   = "yayz"
     * StringKit.center("abc", 7, null) = "  abc  "
     * StringKit.center("abc", 7, "")   = "  abc  "
     * </pre>
     *
     * @param str    字符串
     * @param size   指定长度
     * @param padStr 两边补充的字符串
     * @return 补充后的字符串
     */
    public static String center(CharSequence str, final int size, CharSequence padStr) {
        if (str == null || size <= 0) {
            return toString(str);
        }
        if (isEmpty(padStr)) {
            padStr = Symbol.SPACE;
        }
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str.toString();
        }
        str = padPre(str, strLen + pads / 2, padStr);
        str = padAfter(str, size, padStr);
        return str.toString();
    }

    /**
     * 补充字符串以满足最小长度
     *
     * <pre>
     * StringKit.padPre(null, *, *);//null
     * StringKit.padPre("1", 3, "ABC");//"AB1"
     * StringKit.padPre("123", 2, "ABC");//"12"
     * </pre>
     *
     * @param str       字符串
     * @param minLength 最小长度
     * @param padStr    补充的字符
     * @return 补充后的字符串
     */
    public static String padPre(CharSequence str, int minLength, CharSequence padStr) {
        if (null == str) {
            return null;
        }
        final int strLen = str.length();
        if (strLen == minLength) {
            return str.toString();
        } else if (strLen > minLength) {
            return subPre(str, minLength);
        }

        return repeatByLength(padStr, minLength - strLen).concat(str.toString());
    }

    /**
     * 补充字符串以满足最小长度
     *
     * <pre>
     * StringKit.padPre(null, *, *);//null
     * StringKit.padPre("1", 3, '0');//"001"
     * StringKit.padPre("123", 2, '0');//"12"
     * </pre>
     *
     * @param str       字符串
     * @param minLength 最小长度
     * @param padChar   补充的字符
     * @return 补充后的字符串
     */
    public static String padPre(CharSequence str, int minLength, char padChar) {
        if (null == str) {
            return null;
        }
        final int strLen = str.length();
        if (strLen == minLength) {
            return str.toString();
        } else if (strLen > minLength) {
            return subPre(str, minLength);
        }

        return repeat(padChar, minLength - strLen).concat(str.toString());
    }

    /**
     * 补充字符串以满足最小长度
     *
     * <pre>
     * StringKit.padAfter(null, *, *);//null
     * StringKit.padAfter("1", 3, '0');//"100"
     * StringKit.padAfter("123", 2, '0');//"23"
     * </pre>
     *
     * @param str       字符串，如果为<code>null</code>，直接返回null
     * @param minLength 最小长度
     * @param padChar   补充的字符
     * @return 补充后的字符串
     */
    public static String padAfter(CharSequence str, int minLength, char padChar) {
        if (null == str) {
            return null;
        }
        final int strLen = str.length();
        if (strLen == minLength) {
            return str.toString();
        } else if (strLen > minLength) {
            return sub(str, strLen - minLength, strLen);
        }

        return str.toString().concat(repeat(padChar, minLength - strLen));
    }

    /**
     * 补充字符串以满足最小长度
     *
     * <pre>
     * StringKit.padAfter(null, *, *);//null
     * StringKit.padAfter("1", 3, "ABC");//"1AB"
     * StringKit.padAfter("123", 2, "ABC");//"23"
     * </pre>
     *
     * @param str       字符串，如果为<code>null</code>，直接返回null
     * @param minLength 最小长度
     * @param padStr    补充的字符
     * @return 补充后的字符串
     */
    public static String padAfter(CharSequence str, int minLength, CharSequence padStr) {
        if (null == str) {
            return null;
        }
        final int strLen = str.length();
        if (strLen == minLength) {
            return str.toString();
        } else if (strLen > minLength) {
            return subByLength(str, minLength);
        }

        return str.toString().concat(repeatByLength(padStr, minLength - strLen));
    }

    /**
     * 字符串指定位置的字符是否与给定字符相同
     * 如果字符串为null，返回false
     * 如果给定的位置大于字符串长度，返回false
     * 如果给定的位置小于0，返回false
     *
     * @param str      字符串
     * @param position 位置
     * @param c        需要对比的字符
     * @return 字符串指定位置的字符是否与给定字符相同
     */
    public static boolean equalsCharAt(CharSequence str, int position, char c) {
        if (null == str || position < 0) {
            return false;
        }
        return str.length() > position && c == str.charAt(position);
    }

    /**
     * 字符串按照字符排序方法
     *
     * @param str 排序字段
     * @return {@link String}
     * @author sixawn.zheng
     */
    public static String sort(String str) {
        if (isEmpty(str) || Normal.EMPTY.equalsIgnoreCase(str)) {
            return null;
        }
        char[] strArray = str.toCharArray();

        Arrays.sort(strArray);

        return String.valueOf(strArray);
    }

}
