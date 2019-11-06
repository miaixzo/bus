package org.aoju.bus.core.utils;

import java.util.*;
import java.util.function.Supplier;

/**
 * 工具包集合，工具类快捷方式
 *
 * @author Kimi Liu
 * @version 5.1.0
 * @since JDK 1.8+
 */
public class $ {

    /**
     * 断言，必须不能为 null
     *
     * @param obj the object reference to check for nullity
     * @param <T> the type of the reference
     * @return {@code obj} if not {@code null}
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    public static <T> T requireNotNull(T obj) {
        return Objects.requireNonNull(obj);
    }

    /**
     * 断言，必须不能为 null
     *
     * @param obj     the object reference to check for nullity
     * @param message detail message to be used in the event that a {@code
     *                NullPointerException} is thrown
     * @param <T>     the type of the reference
     * @return {@code obj} if not {@code null}
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    public static <T> T requireNotNull(T obj, String message) {
        return Objects.requireNonNull(obj, message);
    }

    /**
     * 断言，必须不能为 null
     *
     * @param obj             the object reference to check for nullity
     * @param messageSupplier supplier of the detail message to be
     *                        used in the event that a {@code NullPointerException} is thrown
     * @param <T>             the type of the reference
     * @return {@code obj} if not {@code null}
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    public static <T> T requireNotNull(T obj, Supplier<String> messageSupplier) {
        return Objects.requireNonNull(obj, messageSupplier);
    }

    /**
     * 判断对象为true
     *
     * @param object 对象
     * @return 对象是否为true
     */
    public static boolean isTrue(Boolean object) {
        return ObjectUtils.isTrue(object);
    }

    /**
     * 判断对象为false
     *
     * @param object 对象
     * @return 对象是否为false
     */
    public static boolean isFalse(Boolean object) {
        return ObjectUtils.isFalse(object);
    }

    /**
     * 判断对象是否为null
     * <p>
     * This method exists to be used as a
     * {@link java.util.function.Predicate}, {@code context($::isNull)}
     * </p>
     *
     * @param obj a reference to be checked against {@code null}
     * @return {@code true} if the provided reference is {@code null} otherwise
     * {@code false}
     * @see java.util.function.Predicate
     */
    public static boolean isNull(Object obj) {
        return Objects.isNull(obj);
    }

    /**
     * 判断对象是否 not null
     * <p>
     * This method exists to be used as a
     * {@link java.util.function.Predicate}, {@code context($::notNull)}
     * </p>
     *
     * @param obj a reference to be checked against {@code null}
     * @return {@code true} if the provided reference is non-{@code null}
     * otherwise {@code false}
     * @see java.util.function.Predicate
     */
    public static boolean isNotNull(Object obj) {
        return Objects.nonNull(obj);
    }

    /**
     * 首字母变小写
     *
     * @param str 字符串
     * @return {String}
     */
    public static String firstCharToLower(String str) {
        return StringUtils.firstCharToLower(str);
    }

    /**
     * 首字母变大写
     *
     * @param str 字符串
     * @return {String}
     */
    public static String firstCharToUpper(String str) {
        return StringUtils.firstCharToUpper(str);
    }

    /**
     * 判断是否为空字符串
     * <pre class="code">
     * $.isBlank(null)		= true
     * $.isBlank("")		= true
     * $.isBlank(" ")		= true
     * $.isBlank("12345")	= false
     * $.isBlank(" 12345 ")	= false
     * </pre>
     *
     * @param cs the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null},
     * its length is greater than 0, and it does not contain whitespace only
     * @see Character#isWhitespace
     */
    public static boolean isBlank(final CharSequence cs) {
        return StringUtils.isBlank(cs);
    }

    /**
     * 判断不为空字符串
     * <pre>
     * $.isNotBlank(null)	= false
     * $.isNotBlank("")		= false
     * $.isNotBlank(" ")	= false
     * $.isNotBlank("bob")	= true
     * $.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is
     * not empty and not null and not whitespace
     * @see Character#isWhitespace
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return StringUtils.isNotBlank(cs);
    }

    /**
     * 判断是否有任意一个 空字符串
     *
     * @param css CharSequence
     * @return boolean
     */
    public static boolean isAnyBlank(final CharSequence... css) {
        return StringUtils.isAnyBlank(css);
    }

    /**
     * 判断对象是数组
     *
     * @param obj the object to check
     * @return 是否数组
     */
    public static boolean isArray(Object obj) {
        return ObjectUtils.isArray(obj);
    }

    /**
     * 判断空对象 object、map、list、set、字符串、数组
     *
     * @param obj the object to check
     * @return 数组是否为空
     */
    public static boolean isEmpty(Object obj) {
        return ObjectUtils.isEmpty(obj);
    }

    /**
     * 对象不为空 object、map、list、set、字符串、数组
     *
     * @param obj the object to check
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Object obj) {
        return !ObjectUtils.isEmpty(obj);
    }

    /**
     * 判断数组为空
     *
     * @param array the array to check
     * @return 数组是否为空
     */
    public static boolean isEmpty(Object[] array) {
        return ObjectUtils.isEmpty(array);
    }

    /**
     * 判断数组不为空
     *
     * @param array 数组
     * @return 数组是否不为空
     */
    public static boolean isNotEmpty(Object[] array) {
        return ObjectUtils.isNotEmpty(array);
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
        return StringUtils.format(message, params);
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
        return StringUtils.format(message, arguments);
    }

    /**
     * 清理字符串，清理出某些不可见字符和一些sql特殊字符
     *
     * @param txt 文本
     * @return {String}
     */
    public static String cleanText(String txt) {
        return StringUtils.cleanText(txt);
    }

    /**
     * 获取标识符，用于参数清理
     *
     * @param param 参数
     * @return 清理后的标识符
     */
    public static String cleanIdentifier(String param) {
        return StringUtils.cleanIdentifier(param);
    }

    /**
     * 安全的 equals
     *
     * @param o1 first Object to compare
     * @param o2 second Object to compare
     * @return whether the given objects are equal
     * @see Object#equals(Object)
     * @see java.util.Arrays#equals
     */
    public static boolean equalsSafe(Object o1, Object o2) {
        return ObjectUtils.nullSafeEquals(o1, o2);
    }

    /**
     * 对象 eq
     *
     * @param o1 Object
     * @param o2 Object
     * @return 是否eq
     */
    public static boolean equals(Object o1, Object o2) {
        return Objects.equals(o1, o2);
    }

    /**
     * 比较两个对象是否不相等。<br>
     *
     * @param o1 对象1
     * @param o2 对象2
     * @return 是否不eq
     */
    public static boolean isNotEqual(Object o1, Object o2) {
        return !Objects.equals(o1, o2);
    }

    /**
     * 返回对象的 hashCode
     *
     * @param obj Object
     * @return hashCode
     */
    public static int hashCode(Object obj) {
        return Objects.hashCode(obj);
    }

    /**
     * 如果对象为null，返回默认值
     *
     * @param object       Object
     * @param defaultValue 默认值
     * @return Object
     */
    public static Object defaultIfNull(Object object, Object defaultValue) {
        return object != null ? object : defaultValue;
    }

    /**
     * 判断数组中是否包含元素
     *
     * @param array   the Array to check
     * @param element the element to look for
     * @param <T>     The generic tag
     * @return {@code true} if found, {@code false} else
     */
    public static <T> boolean contains(T[] array, final T element) {
        return CollUtils.contains(array, element);
    }

    /**
     * 判断迭代器中是否包含元素
     *
     * @param iterator the Iterator to check
     * @param element  the element to look for
     * @return {@code true} if found, {@code false} otherwise
     */
    public static boolean contains(Iterator<?> iterator, Object element) {
        return CollUtils.contains(iterator, element);
    }

    /**
     * 判断枚举是否包含该元素
     *
     * @param enumeration the Enumeration to check
     * @param element     the element to look for
     * @return {@code true} if found, {@code false} otherwise
     */
    public static boolean contains(Enumeration<?> enumeration, Object element) {
        return CollUtils.contains(enumeration, element);
    }

    /**
     * Concatenates 2 arrays
     *
     * @param one   数组1
     * @param other 数组2
     * @return 新数组
     */
    public static String[] concat(String[] one, String[] other) {
        return CollUtils.concat(one, other, String.class);
    }

    /**
     * Concatenates 2 arrays
     *
     * @param <T>   对象
     * @param one   数组1
     * @param other 数组2
     * @param clazz 数组类
     * @return 新数组
     */
    public static <T> T[] concat(T[] one, T[] other, Class<T> clazz) {
        return CollUtils.concat(one, other, clazz);
    }

    /**
     * 不可变 Set
     *
     * @param es  对象
     * @param <E> 泛型
     * @return 集合
     */
    public static <E> Set<E> ofImmutableSet(E... es) {
        return CollUtils.ofImmutableSet(es);
    }

    /**
     * 不可变 List
     *
     * @param es  对象
     * @param <E> 泛型
     * @return 集合
     */
    public static <E> List<E> ofImmutableList(E... es) {
        return CollUtils.ofImmutableList(es);
    }

}
