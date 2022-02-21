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
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.compare.NormalCompare;
import org.aoju.bus.core.compare.PinyinCompare;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.io.streams.ByteArrayOutputStream;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.text.TextBuilder;

import java.io.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 一些通用的函数
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
public class ObjectKit {

    /**
     * 检查对象是否为null
     *
     * @param obj 对象
     * @return 是否为null
     */
    public static boolean isNull(Object obj) {
        return null == obj || obj.equals(null);
    }

    /**
     * 检查对象是否不为null
     *
     * @param obj 对象
     * @return 是否为null
     */
    public static boolean isNotNull(Object obj) {
        return false == isNull(obj);
    }

    /**
     * 如果给定对象为{@code null}返回默认值
     *
     * @param <T>          对象类型
     * @param object       被检查对象,可能为{@code null}
     * @param defaultValue 被检查对象为{@code null}返回的默认值,可以为{@code null}
     * @return 被检查对象为{ null}返回默认值,否则返回原值
     */
    public static <T> T defaultIfNull(final T object, final T defaultValue) {
        return isNull(object) ? defaultValue : object;
    }

    /**
     * 如果被检查对象为 {@code null}， 返回默认值（由 defaultValueSupplier 提供）；否则直接返回
     *
     * @param source               被检查对象
     * @param defaultValueSupplier 默认值提供者
     * @param <T>                  对象类型
     * @return 被检查对象为{@code null}返回默认值，否则返回自定义handle处理后的返回值
     */
    public static <T> T defaultIfNull(T source, Supplier<? extends T> defaultValueSupplier) {
        if (isNull(source)) {
            return defaultValueSupplier.get();
        }
        return source;
    }

    /**
     * 如果给定对象为{@code null} 返回默认值, 如果不为null 返回自定义handle处理后的返回值
     *
     * @param source       Object 类型对象
     * @param handle       非空时自定义的处理方法
     * @param defaultValue 默认为空的返回值
     * @param <T>          被检查对象为{@code null}返回默认值，否则返回自定义handle处理后的返回值
     * @return 被检查对象为{ null}返回默认值,否则返回原值
     */
    public static <T> T defaultIfNull(final Object source, Supplier<? extends T> handle, final T defaultValue) {
        if (isNotNull(source)) {
            return handle.get();
        }
        return defaultValue;
    }

    /**
     * 如果被检查对象为 {@code null} 或 "" 时，返回默认值（由 defaultValueSupplier 提供）；否则直接返回
     *
     * @param text   被检查对象
     * @param handle 默认值提供者
     * @param <T>    对象类型（必须实现CharSequence接口）
     * @return 被检查对象为{@code null}返回默认值，否则返回自定义handle处理后的返回值
     */
    public static <T extends CharSequence> T defaultIfEmpty(T text, Supplier<? extends T> handle) {
        if (StringKit.isEmpty(text)) {
            return handle.get();
        }
        return text;
    }

    /**
     * 如果给定对象为{@code null}或者""返回默认值, 否则返回自定义handle处理后的返回值
     *
     * @param text         String 类型
     * @param handle       自定义的处理方法
     * @param defaultValue 默认为空的返回值
     * @param <T>          被检查对象为{@code null}或者 ""返回默认值，否则返回自定义handle处理后的返回值
     * @return 被检查对象为{ null}返回默认值,否则返回原值
     */
    public static <T> T defaultIfEmpty(final String text, Supplier<? extends T> handle, final T defaultValue) {
        if (StringKit.isNotEmpty(text)) {
            return handle.get();
        }
        return defaultValue;
    }

    /**
     * 如果给定对象为{@code null}或者 "" 返回默认值
     *
     * <pre>
     * ObjectKit.defaultIfEmpty(null, null)      = null
     * ObjectKit.defaultIfEmpty(null, "")        = ""
     * ObjectKit.defaultIfEmpty("", "zz")        = "zz"
     * ObjectKit.defaultIfEmpty(" ", "zz")       = " "
     * ObjectKit.defaultIfEmpty("abc", *)        = "abc"
     * </pre>
     *
     * @param <T>          对象类型(必须实现CharSequence接口)
     * @param text         被检查对象，可能为{@code null}
     * @param defaultValue 被检查对象为{@code null}或者 ""返回的默认值，可以为{@code null}或者 ""
     * @return 被检查对象为{@code null}或者 ""返回默认值，否则返回原值
     */
    public static <T extends CharSequence> T defaultIfEmpty(final T text, final T defaultValue) {
        return StringKit.isEmpty(text) ? defaultValue : text;
    }

    /**
     * 如果给定对象为{@code null}或者""或者空白符返回默认值
     *
     * <pre>
     * ObjectKit.defaultIfBlank(null, null)      = null
     * ObjectKit.defaultIfBlank(null, "")        = ""
     * ObjectKit.defaultIfBlank("", "zz")        = "zz"
     * ObjectKit.defaultIfBlank(" ", "zz")       = "zz"
     * ObjectKit.defaultIfBlank("abc", *)        = "abc"
     * </pre>
     *
     * @param <T>          对象类型(必须实现CharSequence接口)
     * @param text         被检查对象，可能为{@code null}
     * @param defaultValue 被检查对象为{@code null}或者 ""或者空白符返回的默认值，可以为{@code null}或者 ""或者空白符
     * @return 被检查对象为{@code null}或者 ""或者空白符返回默认值，否则返回原值
     */
    public static <T extends CharSequence> T defaultIfBlank(final T text, final T defaultValue) {
        return StringKit.isBlank(text) ? defaultValue : text;
    }

    /**
     * 如果被检查对象为 {@code null} 或 "" 或 空白字符串时，返回默认值（由 defaultValueSupplier 提供）；否则直接返回
     *
     * @param text   被检查对象
     * @param handle 默认值提供者
     * @param <T>    对象类型（必须实现CharSequence接口）
     * @return 被检查对象为{@code null}返回默认值，否则返回自定义handle处理后的返回值
     */
    public static <T extends CharSequence> T defaultIfBlank(T text, Supplier<? extends T> handle) {
        if (StringKit.isBlank(text)) {
            return handle.get();
        }
        return text;
    }

    /**
     * 判断对象为true
     *
     * @param object 对象
     * @return 对象是否为true
     */
    public static boolean isTrue(Boolean object) {
        return Boolean.TRUE.equals(object);
    }

    /**
     * 判断对象为false
     *
     * @param object 对象
     * @return 对象是否为false
     */
    public static boolean isFalse(Boolean object) {
        return null == object || Boolean.FALSE.equals(object);
    }

    /**
     * 确定给定的对象是一个数组:对象数组还是基元数组
     *
     * @param object 要检查的对象
     * @return the true/false
     */
    public static boolean isArray(Object object) {
        return null != object && object.getClass().isArray();
    }

    /**
     * 判断指定对象是否为空，支持：
     *
     * <pre>
     * 1. CharSequence
     * 2. Map
     * 3. Iterable
     * 4. Iterator
     * 5. Array
     * </pre>
     *
     * @param object 被判断的对象
     * @return 是否为空，如果类型不支持，返回false
     */
    public static boolean isEmpty(Object object) {
        if (null == object) {
            return true;
        }

        if (object instanceof CharSequence) {
            return StringKit.isEmpty((CharSequence) object);
        } else if (object instanceof Map) {
            return MapKit.isEmpty((Map) object);
        } else if (object instanceof Iterable) {
            return IterKit.isEmpty((Iterable) object);
        } else if (object instanceof Iterator) {
            return IterKit.isEmpty((Iterator) object);
        } else if (ArrayKit.isArray(object)) {
            return ArrayKit.isEmpty(object);
        }

        return false;
    }

    /**
     * 判断对象是否为NotEmpty(!null或元素大于0)
     * 实用于对如下对象做判断:String Collection及其子类 Map及其子类
     *
     * @param object 待检查对象
     * @return boolean 返回的布尔值
     */
    public static final boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    /**
     * 是否全都为{@code null}或空对象，通过{@link ObjectKit#isEmpty(Object)} 判断元素
     *
     * @param objs 被检查的对象,一个或者多个
     * @return 是否都为空
     */
    public static boolean isAllEmpty(Object... objs) {
        return ArrayKit.isAllEmpty(objs);
    }

    /**
     * 是否全都不为{@code null}或空对象，通过{@link ObjectKit#isEmpty(Object)} 判断元素
     *
     * @param objs 被检查的对象,一个或者多个
     * @return 是否都不为空
     */
    public static boolean isAllNotEmpty(Object... objs) {
        return ArrayKit.isAllNotEmpty(objs);
    }

    /**
     * 比较两个对象是否相等
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否相等
     * @see #equal(Object, Object)
     */
    public static boolean equals(Object obj1, Object obj2) {
        return equal(obj1, obj2);
    }

    /**
     * 比较两个对象是否相等
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否相等
     */
    public static boolean equal(Object obj1, Object obj2) {
        if (obj1 instanceof BigDecimal && obj2 instanceof BigDecimal) {
            return MathKit.equals((BigDecimal) obj1, (BigDecimal) obj2);
        }
        return Objects.equals(obj1, obj2);
    }

    /**
     * 比较两个摘要是否相等
     *
     * @param byte1 字节比较信息1
     * @param byte2 字节比较信息2
     * @return 如果相等，则为true，否则为false
     */
    public static boolean equal(byte[] byte1, byte[] byte2) {
        if (byte1 == byte2) {
            return true;
        }
        if (null == byte1 || null == byte2) {
            return false;
        }
        if (byte1.length != byte2.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < byte1.length; i++) {
            result |= byte1[i] ^ byte2[i];
        }
        return result == 0;
    }

    /**
     * 比较两个对象是否不相等
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否不等
     */
    public static boolean notEqual(Object obj1, Object obj2) {
        return false == equal(obj1, obj2);
    }

    /**
     * 计算对象长度,如果是字符串调用其length函数,
     * 集合类调用其size函数, 数组调用其length属性,
     * 其他可遍历对象遍历计算长度
     *
     * @param obj 被计算长度的对象
     * @return 长度
     */
    public static int length(Object obj) {
        if (null == obj) {
            return 0;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length();
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).size();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).size();
        }

        int count;
        if (obj instanceof Iterator) {
            Iterator<?> iter = (Iterator<?>) obj;
            count = 0;
            while (iter.hasNext()) {
                count++;
                iter.next();
            }
            return count;
        }
        if (obj instanceof Enumeration) {
            Enumeration<?> enumeration = (Enumeration<?>) obj;
            count = 0;
            while (enumeration.hasMoreElements()) {
                count++;
                enumeration.nextElement();
            }
            return count;
        }
        if (obj.getClass().isArray() == true) {
            return Array.getLength(obj);
        }
        return -1;
    }

    /**
     * 对象中是否包含元素
     *
     * @param obj     对象
     * @param element 元素
     * @return 是否包含
     */
    public static boolean contains(Object obj, Object element) {
        if (null == obj) {
            return false;
        }
        if (obj instanceof String) {
            if (null == element) {
                return false;
            }
            return ((String) obj).contains(element.toString());
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).contains(element);
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).containsValue(element);
        }

        if (obj instanceof Iterator) {
            Iterator<?> iter = (Iterator<?>) obj;
            while (iter.hasNext()) {
                Object o = iter.next();
                if (equal(o, element)) {
                    return true;
                }
            }
            return false;
        }
        if (obj instanceof Enumeration) {
            Enumeration<?> enumeration = (Enumeration<?>) obj;
            while (enumeration.hasMoreElements()) {
                Object o = enumeration.nextElement();
                if (equal(o, element)) {
                    return true;
                }
            }
            return false;
        }
        if (obj.getClass().isArray() == true) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                Object o = Array.get(obj, i);
                if (equal(o, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 克隆对象
     * 如果对象实现Cloneable接口,调用其clone方法
     * 如果实现Serializable接口,执行深度克隆
     * 否则返回null
     *
     * @param <T> 对象类型
     * @param obj 被克隆对象
     * @return 克隆后的对象
     */
    public static <T> T clone(T obj) {
        T result = ArrayKit.clone(obj);
        if (null == result) {
            if (obj instanceof Cloneable) {
                result = ReflectKit.invoke(obj, "clone", new Object[]{});
            } else {
                result = cloneByStream(obj);
            }
        }
        return result;
    }

    /**
     * 将Object转为String
     * 策略为：
     * <pre>
     *  1、null转为"null"
     *  2、调用Convert.toStr(Object)转换
     * </pre>
     *
     * @param obj Bean对象
     * @return Bean所有字段转为Map后的字符串
     */
    public static String toString(Object obj) {
        if (null == obj) {
            return Normal.NULL;
        }
        if (obj instanceof Map) {
            return obj.toString();
        }

        return Convert.toString(obj);
    }

    /**
     * 序列化后拷贝流的方式克隆
     * 对象必须实现Serializable接口
     *
     * @param <T> 对象类型
     * @param obj 被克隆对象
     * @return 克隆后的对象
     * @throws InstrumentException IO异常和ClassNotFoundException封装
     */
    public static <T> T cloneByStream(T obj) {
        if (null == obj || false == (obj instanceof Serializable)) {
            return null;
        }
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(byteOut);
            out.writeObject(obj);
            out.flush();
            final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
            return (T) in.readObject();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * {@code null}安全的对象比较，{@code null}对象小于任何对象
     *
     * @param <T> 被比较对象类型
     * @param c1  对象1，可以为{@code null}
     * @param c2  对象2，可以为{@code null}
     * @return 比较结果，如果c1 &lt; c2，返回数小于0，c1==c2返回0，c1 &gt; c2 大于0
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> int compare(T c1, T c2) {
        return compare(c1, c2, false);
    }

    /**
     * 比较2个版本号
     *
     * @param v1       版本1
     * @param v2       版本2
     * @param complete 是否完整的比较两个版本
     * @return (v1 小于 v2) ? -1 : ((v1 等于 v2) ? 0 : 1)
     */
    public static int compare(String v1, String v2, boolean complete) {
        // v1 null视为最小版本,排在前
        if (v1 == v2) {
            return 0;
        } else if (null == v1) {
            return -1;
        } else if (null == v2) {
            return 1;
        }
        // 去除空格
        v1 = v1.trim();
        v2 = v2.trim();
        if (v1.equals(v2)) {
            return 0;
        }
        String[] v1s = v1.split(Symbol.BACKSLASH + Symbol.DOT);
        String[] v2s = v2.split(Symbol.BACKSLASH + Symbol.DOT);
        int v1sLen = v1s.length;
        int v2sLen = v2s.length;
        int len = complete
                ? Math.max(v1sLen, v2sLen)
                : Math.min(v1sLen, v2sLen);

        for (int i = 0; i < len; i++) {
            String c1 = len > v1sLen || null == v1s[i] ? Normal.EMPTY : v1s[i];
            String c2 = len > v2sLen || null == v2s[i] ? Normal.EMPTY : v2s[i];

            int result = c1.compareTo(c2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    /**
     * {@code null}安全的对象比较
     *
     * @param <T>           被比较对象类型(必须实现Comparable接口)
     * @param c1            对象1，可以为{@code null}
     * @param c2            对象2，可以为{@code null}
     * @param isNullGreater 当被比较对象为null时是否排在前面，true表示null大于任何对象，false反之
     * @return 比较结果，如果c1 &lt; c2，返回数小于0，c1==c2返回0，c1 &gt; c2 大于0
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> int compare(T c1, T c2, boolean isNullGreater) {
        if (c1 == c2) {
            return 0;
        } else if (null == c1) {
            return isNullGreater ? 1 : -1;
        } else if (null == c2) {
            return isNullGreater ? -1 : 1;
        }
        return c1.compareTo(c2);
    }

    /**
     * 获取自然排序器，即默认排序器
     *
     * @param <E> 排序节点类型
     * @return 默认排序器
     */
    public static <E extends Comparable<? super E>> Comparator<E> naturalComparator() {
        return NormalCompare.INSTANCE;
    }

    /**
     * 对象比较，比较结果取决于comparator，如果被比较对象为null，传入的comparator对象应处理此情况
     * 如果传入comparator为null，则使用默认规则比较(此时被比较对象必须实现Comparable接口)
     * 一般而言，如果c1 &lt; c2，返回数小于0，c1==c2返回0，c1 &gt; c2 大于0
     *
     * @param <T>        被比较对象类型
     * @param c1         对象1
     * @param c2         对象2
     * @param comparator 比较器
     * @return 比较结果
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T> int compare(T c1, T c2, Comparator<T> comparator) {
        if (null == comparator) {
            return compare((Comparable) c1, (Comparable) c2);
        }
        return comparator.compare(c1, c2);
    }

    /**
     * 自然比较两个对象的大小，比较规则如下：
     *
     * <pre>
     * 1、如果实现Comparable调用compareTo比较
     * 2、o1.equals(o2)返回0
     * 3、比较hashCode值
     * 4、比较toString值
     * </pre>
     *
     * @param <T>           被比较对象类型
     * @param o1            对象1
     * @param o2            对象2
     * @param isNullGreater null值是否做为最大值
     * @return 比较结果，如果o1 &lt; o2，返回数小于0，o1==o2返回0，o1 &gt; o2 大于0
     */
    public static <T> int compare(T o1, T o2, boolean isNullGreater) {
        if (o1 == o2) {
            return 0;
        } else if (null == o1) {// null 排在后面
            return isNullGreater ? 1 : -1;
        } else if (null == o2) {
            return isNullGreater ? -1 : 1;
        }

        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            //如果bean可比较，直接比较bean
            return ((Comparable) o1).compareTo(o2);
        }

        if (o1.equals(o2)) {
            return 0;
        }

        int result = Integer.compare(o1.hashCode(), o2.hashCode());
        if (0 == result) {
            result = compare(o1.toString(), o2.toString());
        }

        return result;
    }

    /**
     * 中文字符比较器
     *
     * @param object 从对象中提取中文(参与比较的内容)
     * @param <T>    对象类型
     * @return 中文字符比较器
     */
    public static <T> Comparator<T> compare(Function<T, String> object) {
        return compare(object, false);
    }

    /**
     * 中文字符比较器
     *
     * @param object  从对象中提取中文(参与比较的内容)
     * @param reverse 是否反序
     * @param <T>     对象类型
     * @return 中文字符比较器
     */
    public static <T> Comparator<T> compare(Function<T, String> object, boolean reverse) {
        Objects.requireNonNull(object);
        PinyinCompare pinyinComparator = new PinyinCompare();
        if (reverse) {
            return (o1, o2) -> pinyinComparator.compare(object.apply(o2), object.apply(o1));
        }
        return (o1, o2) -> pinyinComparator.compare(object.apply(o1), object.apply(o2));
    }

    /**
     * 获取包括父类所有的属性
     *
     * @param object 对象
     * @return the field
     */
    public static Field[] getAllFields(Object object) {
        List<Field> fieldList = new ArrayList<>();
        Class tempClass = object.getClass();
        while (null != tempClass && !tempClass.getName().equalsIgnoreCase("java.lang.object")) {
            // 当父类为null的时候说明到达了最上层的父类(Object类).
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            // 得到父类,然后赋给自己
            tempClass = tempClass.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }

    /**
     * 将对象进行序列化
     *
     * @param obj 对象
     * @return 对象序列化后的数据
     */
    public static byte[] toByte(Object obj) {
        try {
            java.io.ByteArrayOutputStream byteOut = new java.io.ByteArrayOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(obj);
            return byteOut.toByteArray();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将base64的序列化数据转换为对象
     *
     * @param <T>    对象
     * @param base64 经过base64的序列化对象数据
     * @return 原对象
     */
    public static <T> T toObject(String base64) {
        return toObject(StringKit.base64ToByte(base64));
    }

    /**
     * 将序列化数据转换为对象
     *
     * @param <T> 对象
     * @param bts 序列化后的对象数据
     * @return 原对象
     */
    public static <T> T toObject(byte[] bts) {
        try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(bts);
            ObjectInputStream objIn = new ObjectInputStream(byteIn);
            return (T) objIn.readObject();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /***
     * 依据class的名称获取对应class
     * @param classAllName    类的全称(如: java.lang.String)
     * @return 返回依据类名映射的class对象
     */
    public static Class getClassByName(String classAllName) {
        try {
            return Class.forName(classAllName);
        } catch (ClassNotFoundException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 初始化对象
     *
     * @param <T>     对象
     * @param clazz   创建的对象的类型
     * @param attrMap 初始对象的属性值
     * @return 创建的对象
     */
    public static <T> T initObject(Class<T> clazz, Map<String, Object> attrMap) {
        try {
            T obj = clazz.newInstance();
            if (null != attrMap) {
                // 移除所有的常量赋值
                for (Class tempClass = clazz; !tempClass.equals(Object.class); tempClass = tempClass.getSuperclass()) {
                    Field[] fs = tempClass.getDeclaredFields();
                    for (Field f : fs) {
                        f.setAccessible(true);
                        if (Modifier.isFinal(f.getModifiers())) {
                            attrMap.remove(f.getName());
                        }
                        f.setAccessible(false);
                    }
                }
                // 开始赋值
                for (String attrName : attrMap.keySet()) {
                    setAttribute(obj, attrName, attrMap.get(attrName));
                }
            }
            return obj;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 给对象的属性赋值
     *
     * @param obj      对象
     * @param attrName 对象的属性名
     * @param value    对象的属性值
     */
    public static void setAttribute(Object obj, String attrName, Object value) {
        try {
            Class clazz = obj.getClass();
            while (!clazz.equals(Object.class)) {
                try {
                    Field f = clazz.getDeclaredField(attrName);
                    f.setAccessible(true);
                    f.set(obj, value);
                    f.setAccessible(false);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 从对象中取值
     *
     * @param obj      对象
     * @param attrName 要取值的属性名
     * @return 值
     */
    public static Object getAttributeValue(Object obj, String attrName) {
        try {
            Class clazz = obj.getClass();
            while (!clazz.equals(Object.class)) {
                try {
                    Field f = clazz.getDeclaredField(attrName);
                    f.setAccessible(true);
                    Object value = f.get(obj);
                    f.setAccessible(false);
                    return value;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }

            return null;

        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取对象中的所有属性
     *
     * @param bean 对象
     * @return 属性和值(Map[属性名, 属性值])
     */
    public static Map<String, Object> getFields(Object bean) {
        try {
            Map<String, Object> map = new HashMap<>();
            for (Class clazz = bean.getClass(); !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                Field[] fs = clazz.getDeclaredFields();
                for (Field f : fs) {
                    // 子类最大,父类值不覆盖子类
                    if (map.containsKey(f.getName())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object value = f.get(bean);
                    f.setAccessible(false);
                    map.put(f.getName(), value);
                }
            }
            map.remove("serialVersionUID");
            return map;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取类的所有属性与属性的类型
     *
     * @param clazz 类
     * @return 该类的所有属性名与属性类型(包含父类属性)
     */
    public static Map<String, Class> getFieldNames(Class clazz) {
        try {
            Map<String, Class> attrMap = new HashMap<>();
            for (; !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                Field[] fs = clazz.getDeclaredFields();
                for (Field f : fs) {
                    attrMap.put(f.getName(), f.getType());
                }
            }
            attrMap.remove("serialVersionUID");
            return attrMap;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取对象中的非空属性(属性如果是对象,则只会在同一个map中新增,不会出现map嵌套情况)
     *
     * @param bean         对象
     * @param hasInitValue 是否过滤掉初始值(true:过滤掉)
     * @return 非空属性和值(Map[属性名, 属性值])
     */
    public static Map<String, Object> getNotNullFields(Object bean, boolean hasInitValue) {
        try {
            if (hasInitValue) {
                cleanInitValue(bean);
            }
            Map<String, Object> map = new HashMap<>();
            for (Class clazz = bean.getClass(); !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                Field[] fs = clazz.getDeclaredFields();
                for (Field f : fs) {
                    // 子类最大,父类值不覆盖子类
                    if (map.containsKey(f.getName())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object value = f.get(bean);
                    f.setAccessible(false);
                    if (null != value) {
                        map.put(f.getName(), value);
                    }
                }
            }
            map.remove("serialVersionUID");
            return map;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取对象中的非空属性(属性如果是对象,则只会在同一个map中新增,不会出现map嵌套情况)
     * (不会清空初始值)
     *
     * @param bean 对象
     * @return 非空属性和值(Map[属性名, 属性值])
     */
    public static Map<String, Object> getNotNullFields(Object bean) {
        return getNotNullFields(bean, true);
    }


    /**
     * 获取对象中的非空属性(属性如果是对象,则只会在同一个map中新增,不会出现map嵌套情况)
     * (不会清空初始值)
     * <p>
     * request param
     *
     * @param bean 对象
     * @return 非空属性和值(Map[属性名, 属性值])
     */
    public static Map<String, List<String>> getNotNullFieldsParam(Object bean) {
        return getNotNullFieldsParam(bean, true);
    }

    /**
     * 获取对象中的非空属性(属性如果是对象,则只会在同一个map中新增,不会出现map嵌套情况)
     *
     * @param bean         对象
     * @param hasInitValue 是否过滤掉初始值(true:过滤掉)
     * @return 非空属性和值(Map[属性名, 属性值])
     */
    public static Map<String, List<String>> getNotNullFieldsParam(Object bean, boolean hasInitValue) {
        try {
            if (hasInitValue) {
                cleanInitValue(bean);
            }
            Map<String, List<String>> map = new HashMap<>();
            for (Class clazz = bean.getClass(); !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                Field[] fs = clazz.getDeclaredFields();
                for (Field f : fs) {
                    // 子类最大,父类值不覆盖子类
                    if (map.containsKey(f.getName())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object value = f.get(bean);
                    f.setAccessible(false);
                    if (null != value) {
                        List<String> list = new ArrayList<>();
                        list.add(String.valueOf(value));
                        map.put(f.getName(), list);
                    }
                }
            }
            map.remove("serialVersionUID");
            return map;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }


    /**
     * 获取对象中的非空属性(属性如果是对象,则会嵌套map)
     *
     * @param bean 对象
     * @return 非空属性和值(Map[属性名, 属性值])
     */
    public static Map<String, Object> getNotNullFieldsForStructure(Object bean) {
        try {
            Map<String, Object> map = new HashMap<>();
            for (Class clazz = bean.getClass(); !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                Field[] fs = clazz.getDeclaredFields();
                for (Field f : fs) {
                    // 子类最大,父类值不覆盖子类
                    if (map.containsKey(f.getName())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object value = f.get(bean);
                    f.setAccessible(false);
                    if (null != value) {
                        if (!isNotStructure(value)) {
                            map.put(f.getName(), getNotNullFieldsForStructure(value));
                        } else {
                            map.put(f.getName(), value);
                        }
                    }
                }
            }
            map.remove("serialVersionUID");
            return map;
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /***
     * 依据类,获取该类的泛型class
     * @param <T> 对象
     * @param clazz   类对象
     * @return 泛型类型
     */
    public static <T extends Object> Class<T> getGeneric(Class clazz) {
        try {
            Type genType = clazz.getGenericSuperclass();
            if (!(genType instanceof ParameterizedType)) {
                return (Class<T>) Object.class;
            }
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            return (Class<T>) params[0];
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将byte字节转换成对象
     *
     * @param <T> 对象
     * @param bts 字节数据
     * @return 对象
     */
    public static <T extends Object> T parseByteForObj(byte[] bts) {
        ByteArrayInputStream input = new ByteArrayInputStream(bts);
        ObjectInputStream objectInput = null;
        try {
            objectInput = new ObjectInputStream(input);
            return (T) objectInput.readObject();
        } catch (Exception e) {
            throw new InstrumentException(e);
        } finally {
            try {
                if (null != objectInput) {
                    objectInput.close();
                }
                if (null != input) {
                    input.close();
                }
            } catch (IOException e) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 将对象转换为byte数据
     *
     * @param obj 对象
     * @return byte数据
     */
    public static byte[] parseObjForByte(Object obj) {
        java.io.ByteArrayOutputStream byteOut = new java.io.ByteArrayOutputStream();
        ObjectOutputStream objOut = null;
        try {
            objOut = new ObjectOutputStream(byteOut);
            objOut.writeObject(obj);
            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            try {
                if (null != objOut) {
                    objOut.close();
                }
                if (null != byteOut) {
                    byteOut.close();
                }
            } catch (IOException e) {
                throw new InstrumentException(e);
            }
        }
    }

    /***
     * 转换类型
     * @param <T>     对象
     * @param value   字符串的值
     * @param type    要转换的类型
     * @return 转换后的值
     */
    public static <T> T parseToObject(Object value, Class<T> type) {
        Object result = null;
        if (null == value || type == String.class) {
            result = null == value ? null : value.toString();
        } else if (type == Character.class || type == char.class) {
            char[] chars = value.toString().toCharArray();
            result = chars.length > 0 ? chars.length > 1 ? chars : chars[0] : Character.MIN_VALUE;
        } else if (type == Boolean.class || type == boolean.class) {
            result = Boolean.parseBoolean(value.toString());
        }
        // 处理boolean值转换
        else if (type == Double.class || type == double.class) {
            result = value.toString().equalsIgnoreCase("true") ? true : value.toString().equalsIgnoreCase("false") ? false : value;
        } else if (type == Long.class || type == long.class) {
            result = Long.parseLong(value.toString());
        } else if (type == Integer.class || type == int.class) {
            result = Integer.parseInt(value.toString());
        } else if (type == Double.class || type == double.class) {
            result = Double.parseDouble(value.toString());
        } else if (type == Float.class || type == float.class) {
            result = Float.parseFloat(value.toString());
        } else if (type == Byte.class || type == byte.class) {
            result = Byte.parseByte(value.toString());
        } else if (type == Short.class || type == short.class) {
            result = Short.parseShort(value.toString());
        }
        return (T) result;
    }

    /***
     * 是否非结构体(不再解析)
     * @param value    要验证数据
     * @return 是否是结构体
     */
    private static boolean isNotStructure(Object value) {
        if (!isBaseClass(value)) {
            if (value instanceof Collection) {
                return true;
            } else if (value instanceof Map) {
                return true;
            } else if (value instanceof Date) {
                return true;
            } else return value.getClass().isArray();
        }
        return true;
    }

    /***
     * 校验是否是九种基础类型(即：非用户定义的类型)
     * @param value 字符串的值	要校验的值
     * @return 是否是基础类型(true : 已经是基础类型了)
     */
    public static boolean isBaseClass(Object value) {
        if (null == value) {
            return true;
        } else if (value instanceof Long) {
            return true;
        } else if (value instanceof Integer) {
            return true;
        } else if (value instanceof Double) {
            return true;
        } else if (value instanceof Float) {
            return true;
        } else if (value instanceof Byte) {
            return true;
        } else if (value instanceof Boolean) {
            return true;
        } else if (value instanceof Short) {
            return true;
        } else if (value instanceof Character) {
            return true;
        } else return value instanceof String;
    }

    /***
     * 克隆有序列化的对象
     * @param <T>	要返回的数据类型
     * @param clazz 反射类
     * @param bean  所有继承过BaseBean的对象
     * @return 克隆后的对象
     */
    public static <T> T CloneObject(Class<T> clazz, Object bean) {
        try {
            Map<String, Object> attrMap = getFields(bean);
            return initObject(clazz, attrMap);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /***
     * 克隆有序列化的对象
     * @param <T>	要返回的数据类型
     * @param bean    要克隆的对象
     * @return 克隆后的对象
     */
    public static <T> T CloneObject(T bean) {
        try {
            Map<String, Object> attrMap = getFields(bean);
            return (T) initObject(bean.getClass(), attrMap);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将新数据的非空属性值插入到基本数据中
     *
     * @param baseData 基本数据
     * @param newData  新数据
     */
    public static void insertObj(Object baseData, Object newData) {
        try {
            if (null == baseData || null == newData) {
                return;
            }
            // 清空初始值
            Map<String, Object> attrList = getNotNullFields(newData);
            Set<String> keys = attrList.keySet();
            if (null != keys && keys.size() > 0) {
                for (String key : keys) {
                    if (!key.equals("serialVersionUID")) {
                        setAttribute(baseData, key, attrList.get(key));
                    }
                }
            }
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 清空对象中所有属性的初始值
     *
     * @param <T>  对象
     * @param bean 对象
     */
    public static <T> void cleanInitValue(T bean) {
        if (null == bean) {
            return;
        }
        try {
            Class<?> clazz = bean.getClass();
            Object obj = clazz.newInstance();
            for (; !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                Field[] fs = clazz.getDeclaredFields();
                for (Field f : fs) {
                    if (Modifier.isFinal(f.getModifiers())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object initValue = f.get(obj);
                    Object oldValue = f.get(bean);
                    if (null != initValue && initValue.equals(oldValue)) {
                        f.set(bean, null);
                    }
                    f.setAccessible(false);
                }
            }
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取如果类没有覆盖toString本身，则{@code Object}将生成的toString.
     * {@code null}将返回{@code null}
     *
     * <pre>
     * ObjectKit.identityToString(null)         = null
     * ObjectKit.identityToString("")           = "java.lang.String@1e23"
     * ObjectKit.identityToString(Boolean.TRUE) = "java.lang.Boolean@7fa"
     * </pre>
     *
     * @param object 创建toString的对象可以是{@code null}
     * @return 如果传入{@code null}，则默认的toString文本或{@code null}
     */
    public static String identityToString(final Object object) {
        if (null == object) {
            return null;
        }
        final String name = object.getClass().getName();
        final String hexString = Integer.toHexString(System.identityHashCode(object));
        final StringBuilder builder = new StringBuilder(name.length() + 1 + hexString.length());

        builder.append(name)
                .append(Symbol.C_AT)
                .append(hexString);

        return builder.toString();
    }

    /**
     * 如果类没有覆盖toString本身，则附加由{@code Object}生成的toString.
     * {@code null}将为这两个参数中的任何一个抛出NullPointerException
     *
     * <pre>
     * ObjectKit.identityToString(appendable, "")            = appendable.append("java.lang.String@1e23"
     * ObjectKit.identityToString(appendable, Boolean.TRUE)  = appendable.append("java.lang.Boolean@7fa"
     * ObjectKit.identityToString(appendable, Boolean.TRUE)  = appendable.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param appendable 可以附加的信息
     * @param object     要为其创建toString的对象
     * @throws IOException 如果发生I/O错误
     */
    public static void identityToString(final Appendable appendable, final Object object) throws IOException {
        Assert.notNull(object, "Cannot get the toString of a null object");
        appendable.append(object.getClass().getName())
                .append(Symbol.C_AT)
                .append(Integer.toHexString(System.identityHashCode(object)));
    }

    /**
     * 如果类没有覆盖toString本身，则附加由{@code Object}生成的toString.
     * {@code null}将为这两个参数中的任何一个抛出NullPointerException
     *
     * <pre>
     * ObjectKit.identityToString(builder, "")            = builder.append("java.lang.String@1e23"
     * ObjectKit.identityToString(builder, Boolean.TRUE)  = builder.append("java.lang.Boolean@7fa"
     * ObjectKit.identityToString(builder, Boolean.TRUE)  = builder.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param builder 要附加到的生成器
     * @param object  要为其创建toString的对象
     */
    public static void identityToString(final TextBuilder builder, final Object object) {
        Assert.notNull(object, "Cannot get the toString of a null object");
        final String name = object.getClass().getName();
        final String hexString = Integer.toHexString(System.identityHashCode(object));
        builder.ensureCapacity(builder.length() + name.length() + 1 + hexString.length());
        builder.append(name)
                .append(Symbol.C_AT)
                .append(hexString);
    }

    /**
     * 如果类没有覆盖toString本身，则附加由{@code Object}生成的toString.
     * {@code null}将为这两个参数中的任何一个抛出NullPointerException
     *
     * <pre>
     * ObjectKit.identityToString(buf, "")            = buf.append("java.lang.String@1e23"
     * ObjectKit.identityToString(buf, Boolean.TRUE)  = buf.append("java.lang.Boolean@7fa"
     * ObjectKit.identityToString(buf, Boolean.TRUE)  = buf.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param buffer 要追加的缓冲区
     * @param object 要为其创建toString的对象
     */
    public static void identityToString(final StringBuffer buffer, final Object object) {
        Assert.notNull(object, "Cannot get the toString of a null object");
        final String name = object.getClass().getName();
        final String hexString = Integer.toHexString(System.identityHashCode(object));
        buffer.ensureCapacity(buffer.length() + name.length() + 1 + hexString.length());
        buffer.append(name)
                .append(Symbol.C_AT)
                .append(hexString);
    }

    /**
     * 如果类没有覆盖toString本身，则附加由{@code Object}生成的toString.
     * {@code null}将为这两个参数中的任何一个抛出NullPointerException
     *
     * <pre>
     * ObjectKit.identityToString(builder, "")            = builder.append("java.lang.String@1e23"
     * ObjectKit.identityToString(builder, Boolean.TRUE)  = builder.append("java.lang.Boolean@7fa"
     * ObjectKit.identityToString(builder, Boolean.TRUE)  = builder.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param builder 要附加到的生成器
     * @param object  要为其创建toString的对象
     */
    public static void identityToString(final StringBuilder builder, final Object object) {
        Assert.notNull(object, "Cannot get the toString of a null object");
        final String name = object.getClass().getName();
        final String hexString = Integer.toHexString(System.identityHashCode(object));
        builder.ensureCapacity(builder.length() + name.length() + 1 + hexString.length());
        builder.append(name)
                .append(Symbol.C_AT)
                .append(hexString);
    }

    /**
     * 确定给定的对象是否相等，如果两个对象都是{@code null}，
     * 则返回{@code true};如果只有一个对象是{@code null}，
     * 则返回{@code false}
     *
     * @param o1 第一个比较对象
     * @param o2 第二个比较对象
     * @return 给定对象是否相等
     * @see Object#equals(Object)
     * @see Arrays#equals
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (null == o1 || null == o2) {
            return false;
        }
        if (o1.equals(o2)) {
            return true;
        }
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            return arrayEquals(o1, o2);
        }
        return false;
    }

    /**
     * 比较给定的数组和{@code Arrays.equals}，根据数组元素而不是数组引用执行相等性检查
     *
     * @param o1 第一个比较对象
     * @param o2 第二个比较对象
     * @return 给定对象是否相等
     * @see #nullSafeEquals(Object, Object)
     * @see Arrays#equals
     */
    private static boolean arrayEquals(Object o1, Object o2) {
        if (o1 instanceof Object[] && o2 instanceof Object[]) {
            return Arrays.equals((Object[]) o1, (Object[]) o2);
        }
        if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
            return Arrays.equals((boolean[]) o1, (boolean[]) o2);
        }
        if (o1 instanceof byte[] && o2 instanceof byte[]) {
            return Arrays.equals((byte[]) o1, (byte[]) o2);
        }
        if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals((char[]) o1, (char[]) o2);
        }
        if (o1 instanceof double[] && o2 instanceof double[]) {
            return Arrays.equals((double[]) o1, (double[]) o2);
        }
        if (o1 instanceof float[] && o2 instanceof float[]) {
            return Arrays.equals((float[]) o1, (float[]) o2);
        }
        if (o1 instanceof int[] && o2 instanceof int[]) {
            return Arrays.equals((int[]) o1, (int[]) o2);
        }
        if (o1 instanceof long[] && o2 instanceof long[]) {
            return Arrays.equals((long[]) o1, (long[]) o2);
        }
        if (o1 instanceof short[] && o2 instanceof short[]) {
            return Arrays.equals((short[]) o1, (short[]) o2);
        }
        return false;
    }

    /**
     * 将给定的数组(可能是原始数组)转换为对象数组(如果需要原始包装器对象)
     * 一个{@code null}源值将被转换为一个空的对象数组.
     *
     * @param source 数组
     * @return 对应的对象数组
     */
    public static Object[] toObjectArray(Object source) {
        if (source instanceof Object[]) {
            return (Object[]) source;
        }
        if (null == source) {
            return Normal.EMPTY_OBJECT_ARRAY;
        }
        if (!source.getClass().isArray()) {
            throw new IllegalArgumentException("Source is not an array: " + source);
        }
        int length = Array.getLength(source);
        if (length == 0) {
            return Normal.EMPTY_OBJECT_ARRAY;
        }
        Class<?> wrapperType = Array.get(source, 0).getClass();
        Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
        for (int i = 0; i < length; i++) {
            newArray[i] = Array.get(source, i);
        }
        return newArray;
    }

    /**
     * 对象序列化
     * 对象必须实现Serializable接口
     *
     * @param <T> 对象类型
     * @param obj 要被序列化的对象
     * @return 序列化后的字节码
     */
    public static <T> byte[] serialize(T obj) {
        if (false == (obj instanceof Serializable)) {
            return null;
        }
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        IoKit.writeObjects(byteOut, false, (Serializable) obj);
        return byteOut.toByteArray();
    }

    /**
     * byte反序列化
     * 对象必须实现Serializable接口
     *
     * @param <T>   对象类型
     * @param bytes 反序列化的字节码
     * @return 反序列化后的对象
     */
    public static <T> T deserialize(byte[] bytes) {
        return IoKit.readObj(new ByteArrayInputStream(bytes));
    }

}
