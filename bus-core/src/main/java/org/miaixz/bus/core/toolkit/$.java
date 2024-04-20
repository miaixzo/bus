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

import java.util.*;
import java.util.function.Supplier;

/**
 * 工具包集合,工具类快捷方式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class $ {

    /**
     * 断言,必须不能为 null
     *
     * @param object 检查无效的对象引用
     * @param <T>    引用的类型
     * @return {@code object} 是否为 {@code null}
     * @throws NullPointerException 如果 {@code object} 为 {@code null}
     */
    public static <T> T requireNotNull(T object) {
        return Objects.requireNonNull(object);
    }

    /**
     * 断言,必须不能为 null
     *
     * @param object  检查是否为空的对象引用
     * @param message 在抛出{@code NullPointerException}时使用的详细信息
     * @param <T>     引用的类型
     * @return {@code object} 如果不为 {@code null}
     * @throws NullPointerException 如果 {@code object} 为 {@code null}
     */
    public static <T> T requireNotNull(T object, String message) {
        return Objects.requireNonNull(object, message);
    }

    /**
     * 断言,必须不能为 null
     *
     * @param object          检查是否为空的对象引用
     * @param messageSupplier 在抛出{@code NullPointerException}时使用的详细信息的提供者
     * @param <T>             引用的类型
     * @return {@code object} 如果不为 {@code null}
     * @throws NullPointerException 如果 {@code object} 为 {@code null}
     */
    public static <T> T requireNotNull(T object, Supplier<String> messageSupplier) {
        return Objects.requireNonNull(object, messageSupplier);
    }

    /**
     * 判断对象为true
     *
     * @param object 对象
     * @return 对象是否为true
     */
    public static boolean isTrue(Boolean object) {
        return ObjectKit.isTrue(object);
    }

    /**
     * 判断对象为false
     *
     * @param object 对象
     * @return 对象是否为false
     */
    public static boolean isFalse(Boolean object) {
        return ObjectKit.isFalse(object);
    }

    /**
     * 判断对象是否为null
     *
     * @param object 要根据{@code null}检查的引用
     * @return 如果提供的引用是{@code null}，则为{@code true};否则为{@code false}
     * @see java.util.function.Predicate
     */
    public static boolean isNull(Object object) {
        return Objects.isNull(object);
    }

    /**
     * 判断对象是否 not null
     *
     * @param object 要根据{@code null}检查的引用
     * @return 如果提供的引用是{@code null}，则为{@code true};否则为{@code false}
     * @see java.util.function.Predicate
     */
    public static boolean isNotNull(Object object) {
        return Objects.nonNull(object);
    }

    /**
     * 判断是否为空字符串
     * <pre class="code">
     * $.isBlank(null)		   = true
     * $.isBlank("")		   = true
     * $.isBlank(" ")		   = true
     * $.isBlank("12345")	   = false
     * $.isBlank(" 12345 ")	   = false
     * </pre>
     *
     * @param cs 要检查的{@code CharSequence} 可以是{@code null}
     * @return 如果{@code CharSequence}不是{@code null}，那么它的长度大于0，并且不包含空格
     * @see Character#isWhitespace
     */
    public static boolean isBlank(final CharSequence cs) {
        return StringKit.isBlank(cs);
    }

    /**
     * 判断不为空字符串
     * <pre>
     * $.isNotBlank(null)	   = false
     * $.isNotBlank("")		   = false
     * $.isNotBlank(" ")	   = false
     * $.isNotBlank("bob")	   = true
     * $.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param cs 要检查的CharSequence可能为空
     * @return 如果CharSequence不为空，不为空白，则为{@code true}
     * @see Character#isWhitespace
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return StringKit.isNotBlank(cs);
    }

    /**
     * 判断是否有任意一个 空字符串
     *
     * @param cs 要检查的CharSequence可能为空
     * @return 如果CharSequence不为空，不为空白，则为{@code true}
     */
    public static boolean isAnyBlank(final CharSequence... cs) {
        return StringKit.isAnyBlank(cs);
    }

    /**
     * 判断对象是数组
     *
     * @param object 要检查的对象可能为空
     * @return 是否数组
     */
    public static boolean isArray(Object object) {
        return ObjectKit.isArray(object);
    }

    /**
     * 判断空对象 object、map、list、set、字符串、数组
     *
     * @param object 要检查的对象可能为空
     * @return 数组是否为空
     */
    public static boolean isEmpty(Object object) {
        return ObjectKit.isEmpty(object);
    }

    /**
     * 对象不为空 object、map、list、set、字符串、数组
     *
     * @param object 要检查的对象可能为空
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Object object) {
        return !ObjectKit.isEmpty(object);
    }

    /**
     * 判断数组为空
     *
     * @param array 要检查的数组可能为空
     * @return 数组是否为空
     */
    public static boolean isEmpty(Object[] array) {
        return ObjectKit.isEmpty(array);
    }

    /**
     * 判断数组不为空
     *
     * @param array 数组
     * @return 数组是否不为空
     */
    public static boolean isNotEmpty(Object[] array) {
        return ObjectKit.isNotEmpty(array);
    }

    /**
     * 将字符串中特定模式的字符转换成map中对应的值
     * <p>
     * use: format("my name is ${name}, and i like ${like}!", {"name":"L.cm", "like": "Java"})
     *
     * @param message 需要转换的字符串
     * @param params  转换所需的键值对集合
     * @return 转换后的字符串
     */
    public static String format(String message, Map<String, Object> params) {
        return StringKit.format(message, params);
    }

    /**
     * 同 log 格式的 format 规则
     * <p>
     * use: format("my name is {}, and i like {}!", "L.cm", "Java")
     *
     * @param message   需要转换的字符串
     * @param arguments 需要替换的变量
     * @return 转换后的字符串
     */
    public static String format(String message, Object... arguments) {
        return StringKit.format(message, arguments);
    }

    /**
     * 清理字符串,清理出某些不可见字符和一些sql特殊字符
     *
     * @param txt 文本
     * @return {String}
     */
    public static String cleanText(String txt) {
        return StringKit.cleanText(txt);
    }

    /**
     * 安全的 equals
     *
     * @param o1 第一个比较对象
     * @param o2 第二个比较对象
     * @return 给定对象是否相等
     * @see Object#equals(Object)
     * @see java.util.Arrays#equals
     */
    public static boolean equalsSafe(Object o1, Object o2) {
        return ObjectKit.nullSafeEquals(o1, o2);
    }

    /**
     * 对象 equals
     *
     * @param o1 第一个比较对象
     * @param o2 第二个比较对象
     * @return 给定对象是否相等
     */
    public static boolean equals(Object o1, Object o2) {
        return Objects.equals(o1, o2);
    }

    /**
     * 比较两个对象是否不相等
     *
     * @param o1 第一个比较对象
     * @param o2 第二个比较对象
     * @return 给定对象是否相等
     */
    public static boolean isNotEqual(Object o1, Object o2) {
        return !Objects.equals(o1, o2);
    }

    /**
     * 返回对象的 hashCode
     *
     * @param object Object
     * @return the int
     */
    public static int hashCode(Object object) {
        return Objects.hashCode(object);
    }

    /**
     * 如果对象为null,返回默认值
     *
     * @param object       Object
     * @param defaultValue 默认值
     * @return Object
     */
    public static Object defaultIfNull(Object object, Object defaultValue) {
        return null != object ? object : defaultValue;
    }

    /**
     * 判断数组中是否包含元素
     *
     * @param array   要检查的数组
     * @param element 要查找的元素
     * @param <T>     引用的类型
     * @return 如果找到 {@code true},否则{@code false}
     */
    public static <T> boolean contains(T[] array, final T element) {
        return CollKit.contains(array, element);
    }

    /**
     * 判断迭代器中是否包含元素
     *
     * @param iterator 要检查的迭代器
     * @param element  要查找的元素
     * @return 如果找到 {@code true},否则{@code false}
     */
    public static boolean contains(Iterator<?> iterator, Object element) {
        return CollKit.contains(iterator, element);
    }

    /**
     * 判断枚举是否包含该元素
     *
     * @param enumeration 要检查的枚举
     * @param element     要查找的元素
     * @return 如果找到 {@code true},否则{@code false}
     */
    public static boolean contains(Enumeration<?> enumeration, Object element) {
        return CollKit.contains(enumeration, element);
    }

    /**
     * 连接两个数组
     *
     * @param one   第一个数组
     * @param other 第二个数组
     * @return 连接后的新数组
     */
    public static String[] concat(String[] one, String[] other) {
        return CollKit.concat(one, other, String.class);
    }

    /**
     * 连接两个数组
     *
     * @param <T>   对象
     * @param one   第一个数组
     * @param other 第二个数组
     * @param clazz 数组类
     * @return 连接后的新数组
     */
    public static <T> T[] concat(T[] one, T[] other, Class<T> clazz) {
        return CollKit.concat(one, other, clazz);
    }

    /**
     * 不可变 Set
     *
     * @param es  对象
     * @param <E> 泛型
     * @return 集合
     */
    public static <E> Set<E> ofImmutableSet(E... es) {
        return CollKit.ofImmutableSet(es);
    }

    /**
     * 不可变 List
     *
     * @param es  对象
     * @param <E> 泛型
     * @return 集合
     */
    public static <E> List<E> ofImmutableList(E... es) {
        return CollKit.ofImmutableList(es);
    }

}
