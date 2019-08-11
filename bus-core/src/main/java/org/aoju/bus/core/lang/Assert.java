/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.lang;

import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * 断言<br>
 * 断言某些对象或值是否符合规定，否则抛出异常。经常用于做变量检查
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class Assert {

    /**
     * 断言是否为真，如果为 {@code false} 抛出 {@code IllegalArgumentException} 异常<br>
     *
     * <pre class="criteria">
     * Assert.isTrue(i &gt; 0, "The value must be greater than zero");
     * </pre>
     *
     * @param expression       波尔值
     * @param errorMsgTemplate 错误抛出异常附带的消息模板，变量用{}代替
     * @param params           参数列表
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(boolean expression, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (false == expression) {
            throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
        }
    }

    /**
     * 断言是否为真，如果为 {@code false} 抛出 {@code IllegalArgumentException} 异常<br>
     *
     * <pre class="criteria">
     * Assert.isTrue(i &gt; 0, "The value must be greater than zero");
     * </pre>
     *
     * @param expression 波尔值
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(boolean expression) throws IllegalArgumentException {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    /**
     * 断言是否为假，如果为 {@code true} 抛出 {@code IllegalArgumentException} 异常<br>
     *
     * <pre class="criteria">
     * Assert.isFalse(i &lt; 0, "The value must be greater than zero");
     * </pre>
     *
     * @param expression       波尔值
     * @param errorMsgTemplate 错误抛出异常附带的消息模板，变量用{}代替
     * @param params           参数列表
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isFalse(boolean expression, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (expression) {
            throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
        }
    }

    /**
     * 断言是否为假，如果为 {@code true} 抛出 {@code IllegalArgumentException} 异常<br>
     *
     * <pre class="criteria">
     * Assert.isFalse(i &lt; 0);
     * </pre>
     *
     * @param expression 波尔值
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isFalse(boolean expression) throws IllegalArgumentException {
        isFalse(expression, "[Assertion failed] - this expression must be false");
    }

    /**
     * 断言对象是否为{@code null} ，如果不为{@code null} 抛出{@link IllegalArgumentException} 异常
     *
     * <pre class="criteria">
     * Assert.isNull(value, "The value must be null");
     * </pre>
     *
     * @param object           被检查的对象
     * @param errorMsgTemplate 消息模板，变量使用{}表示
     * @param params           参数列表
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void isNull(Object object, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (object != null) {
            throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
        }
    }

    /**
     * 断言对象是否为{@code null} ，如果不为{@code null} 抛出{@link IllegalArgumentException} 异常
     *
     * <pre class="criteria">
     * Assert.isNull(value);
     * </pre>
     *
     * @param object 被检查对象
     * @throws NullPointerException if the object is not {@code null}
     */
    public static void isNull(Object object) throws NullPointerException {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    /**
     * 断言对象是否不为{@code null} ，如果为{@code null} 抛出{@link IllegalArgumentException} 异常 Assert that an object is not {@code null} .
     *
     * <pre class="criteria">
     * Assert.notNull(clazz, "The class must not be null");
     * </pre>
     *
     * @param <T>              被检查对象泛型类型
     * @param object           被检查对象
     * @param errorMsgTemplate 错误消息模板，变量使用{}表示
     * @param params           参数
     * @return 被检查后的对象
     * @throws NullPointerException if the object is {@code null}
     */
    public static <T> T notNull(T object, String errorMsgTemplate, Object... params) throws NullPointerException {
        if (object == null) {
            throw new NullPointerException(StringUtils.format(errorMsgTemplate, params));
        }
        return object;
    }

    /**
     * 断言对象是否不为{@code null} ，如果为{@code null} 抛出{@link IllegalArgumentException} 异常
     *
     * <pre class="criteria">
     * Assert.notNull(clazz);
     * </pre>
     *
     * @param <T>    被检查对象类型
     * @param object 被检查对象
     * @return 非空对象
     * @throws NullPointerException if the object is {@code null}
     */
    public static <T> T notNull(T object) throws NullPointerException {
        return notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    /**
     * 检查给定字符串是否为空，为空抛出 {@link IllegalArgumentException}
     *
     * <pre class="criteria">
     * Assert.notEmpty(name, "Name must not be empty");
     * </pre>
     *
     * @param text             被检查字符串
     * @param errorMsgTemplate 错误消息模板，变量使用{}表示
     * @param params           参数
     * @return 非空字符串
     * @throws IllegalArgumentException 被检查字符串为空
     */
    public static String notEmpty(String text, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (StringUtils.isEmpty(text)) {
            throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
        }
        return text;
    }

    /**
     * 检查给定字符串是否为空，为空抛出 {@link IllegalArgumentException}
     *
     * <pre class="criteria">
     * Assert.notEmpty(name);
     * </pre>
     *
     * @param text 被检查字符串
     * @return 被检查的字符串
     * @throws IllegalArgumentException 被检查字符串为空
     */
    public static String notEmpty(String text) throws IllegalArgumentException {
        return notEmpty(text, "[Assertion failed] - this String argument must have length; it must not be null or empty");
    }

    /**
     * 检查给定字符串是否为空白（null、空串或只包含空白符），为空抛出 {@link IllegalArgumentException}
     *
     * <pre class="criteria">
     * Assert.notBlank(name, "Name must not be blank");
     * </pre>
     *
     * @param text             被检查字符串
     * @param errorMsgTemplate 错误消息模板，变量使用{}表示
     * @param params           参数
     * @return 非空字符串
     * @throws IllegalArgumentException 被检查字符串为空白
     * @see StringUtils#isNotBlank(CharSequence)
     */
    public static String notBlank(String text, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
        }
        return text;
    }

    /**
     * 检查给定字符串是否为空白（null、空串或只包含空白符），为空抛出 {@link IllegalArgumentException}
     *
     * <pre class="criteria">
     * Assert.notBlank(name, "Name must not be blank");
     * </pre>
     *
     * @param text 被检查字符串
     * @return 非空字符串
     * @throws IllegalArgumentException 被检查字符串为空白
     * @see StringUtils#isNotBlank(CharSequence)
     */
    public static String notBlank(String text) throws IllegalArgumentException {
        return notBlank(text, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
    }

    /**
     * 断言给定字符串是否不被另一个字符串包含（既是否为子串）
     *
     * <pre class="criteria">
     * Assert.doesNotContain(name, "rod", "Name must not contain 'rod'");
     * </pre>
     *
     * @param textToSearch     被搜索的字符串
     * @param substring        被检查的子串
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 被检查的子串
     * @throws IllegalArgumentException 非子串抛出异常
     */
    public static String notContain(String textToSearch, String substring, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (StringUtils.isNotEmpty(textToSearch) && StringUtils.isNotEmpty(substring) && textToSearch.contains(substring)) {
            throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
        }
        return substring;
    }

    /**
     * 断言给定字符串是否不被另一个字符串包含（既是否为子串）
     *
     * <pre class="criteria">
     * Assert.doesNotContain(name, "rod", "Name must not contain 'rod'");
     * </pre>
     *
     * @param textToSearch 被搜索的字符串
     * @param substring    被检查的子串
     * @return 被检查的子串
     * @throws IllegalArgumentException 非子串抛出异常
     */
    public static String notContain(String textToSearch, String substring) throws IllegalArgumentException {
        return notContain(textToSearch, substring, "[Assertion failed] - this String argument must not contain the substring [{}]", substring);
    }

    /**
     * 断言给定数组是否包含元素，数组必须不为 {@code null} 且至少包含一个元素
     *
     * <pre class="criteria">
     * Assert.notEmpty(array, "The array must have elements");
     * </pre>
     *
     * @param array            被检查的数组
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 被检查的数组
     * @throws IllegalArgumentException if the object array is {@code null} or has no elements
     */
    public static Object[] notEmpty(Object[] array, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (ArrayUtils.isEmpty(array)) {
            throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
        }
        return array;
    }

    /**
     * 断言给定数组是否包含元素，数组必须不为 {@code null} 且至少包含一个元素
     *
     * <pre class="criteria">
     * Assert.notEmpty(array, "The array must have elements");
     * </pre>
     *
     * @param array 被检查的数组
     * @return 被检查的数组
     * @throws IllegalArgumentException if the object array is {@code null} or has no elements
     */
    public static Object[] notEmpty(Object[] array) throws IllegalArgumentException {
        return notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element");
    }

    /**
     * 断言给定数组是否不包含{@code null}元素，如果数组为空或 {@code null}将被认为不包含
     *
     * <pre class="criteria">
     * Assert.noNullElements(array, "The array must have non-null elements");
     * </pre>
     *
     * @param <T>              数组元素类型
     * @param array            被检查的数组
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 被检查的数组
     * @throws IllegalArgumentException if the object array contains a {@code null} element
     */
    public static <T> T[] noNullElements(T[] array, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (ArrayUtils.hasNull(array)) {
            throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
        }
        return array;
    }

    /**
     * 断言给定数组是否不包含{@code null}元素，如果数组为空或 {@code null}将被认为不包含
     *
     * <pre class="criteria">
     * Assert.noNullElements(array);
     * </pre>
     *
     * @param <T>   数组元素类型
     * @param array 被检查的数组
     * @return 被检查的数组
     * @throws IllegalArgumentException if the object array contains a {@code null} element
     */
    public static <T> T[] noNullElements(T[] array) throws IllegalArgumentException {
        return noNullElements(array, "[Assertion failed] - this array must not contain any null elements");
    }

    /**
     * 断言给定集合非空
     *
     * <pre class="criteria">
     * Assert.notEmpty(collection, "Collection must have elements");
     * </pre>
     *
     * @param <T>              集合元素类型
     * @param collection       被检查的集合
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 非空集合
     * @throws IllegalArgumentException if the collection is {@code null} or has no elements
     */
    public static <T> Collection<T> notEmpty(Collection<T> collection, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (CollUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
        }
        return collection;
    }

    /**
     * 断言给定集合非空
     *
     * <pre class="criteria">
     * Assert.notEmpty(collection);
     * </pre>
     *
     * @param <T>        集合元素类型
     * @param collection 被检查的集合
     * @return 被检查集合
     * @throws IllegalArgumentException if the collection is {@code null} or has no elements
     */
    public static <T> Collection<T> notEmpty(Collection<T> collection) throws IllegalArgumentException {
        return notEmpty(collection, "[Assertion failed] - this collection must not be empty: it must contain at least 1 element");
    }

    /**
     * 断言给定Map非空
     *
     * <pre class="criteria">
     * Assert.notEmpty(map, "Map must have entries");
     * </pre>
     *
     * @param <K>              Key类型
     * @param <V>              Value类型
     * @param map              被检查的Map
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 被检查的Map
     * @throws IllegalArgumentException if the map is {@code null} or has no entries
     */
    public static <K, V> Map<K, V> notEmpty(Map<K, V> map, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (CollUtils.isEmpty(map)) {
            throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
        }
        return map;
    }

    /**
     * 断言给定Map非空
     *
     * <pre class="criteria">
     * Assert.notEmpty(map, "Map must have entries");
     * </pre>
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param map 被检查的Map
     * @return 被检查的Map
     * @throws IllegalArgumentException if the map is {@code null} or has no entries
     */
    public static <K, V> Map<K, V> notEmpty(Map<K, V> map) throws IllegalArgumentException {
        return notEmpty(map, "[Assertion failed] - this map must not be empty; it must contain at least first entry");
    }

    /**
     * 断言给定对象是否是给定类的实例
     *
     * <pre class="criteria">
     * Assert.instanceOf(Foo.class, foo);
     * </pre>
     *
     * @param <T>  被检查对象泛型类型
     * @param type 被检查对象匹配的类型
     * @param obj  被检查对象
     * @return 被检查的对象
     * @throws IllegalArgumentException if the object is not an instance of clazz
     * @see Class#isInstance(Object)
     */
    public static <T> T isInstanceOf(Class<?> type, T obj) {
        return isInstanceOf(type, obj, "Object [{}] is not instanceof [{}]", obj, type);
    }

    /**
     * 断言给定对象是否是给定类的实例
     *
     * <pre class="criteria">
     * Assert.instanceOf(Foo.class, foo);
     * </pre>
     *
     * @param <T>              被检查对象泛型类型
     * @param type             被检查对象匹配的类型
     * @param obj              被检查对象
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 被检查对象
     * @throws IllegalArgumentException if the object is not an instance of clazz
     * @see Class#isInstance(Object)
     */
    public static <T> T isInstanceOf(Class<?> type, T obj, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        notNull(type, "Type to check against must not be null");
        if (false == type.isInstance(obj)) {
            throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
        }
        return obj;
    }

    /**
     * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
     *
     * <pre class="criteria">
     * Assert.isAssignable(Number.class, myClass);
     * </pre>
     *
     * @param superType the super type to check
     * @param subType   the sub type to check
     * @throws IllegalArgumentException if the classes are not assignable
     */
    public static void isAssignable(Class<?> superType, Class<?> subType) throws IllegalArgumentException {
        isAssignable(superType, subType, "{} is not assignable to {})", subType, superType);
    }

    /**
     * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
     *
     * <pre class="criteria">
     * Assert.isAssignable(Number.class, myClass);
     * </pre>
     *
     * @param superType        the super type to check against
     * @param subType          the sub type to check
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @throws IllegalArgumentException if the classes are not assignable
     */
    public static void isAssignable(Class<?> superType, Class<?> subType, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        notNull(superType, "Type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw new IllegalArgumentException(StringUtils.format(errorMsgTemplate, params));
        }
    }

    /**
     * Assert a boolean expression, throwing {@code IllegalStateException} if the test result is {@code false}. Call isTrue if you wish to throw IllegalArgumentException on an assertion failure.
     *
     * <pre class="criteria">
     * Assert.state(id == null, "The id property must not already be initialized");
     * </pre>
     *
     * @param expression       a boolean expression
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @throws IllegalStateException if expression is {@code false}
     */
    public static void state(boolean expression, String errorMsgTemplate, Object... params) throws IllegalStateException {
        if (false == expression) {
            throw new IllegalStateException(StringUtils.format(errorMsgTemplate, params));
        }
    }

    /**
     * Assert a boolean expression, throwing {@link IllegalStateException} if the test result is {@code false}.
     * <p>
     * Call {@link #isTrue(boolean)} if you wish to throw {@link IllegalArgumentException} on an assertion failure.
     *
     * <pre class="criteria">
     * Assert.state(id == null);
     * </pre>
     *
     * @param expression a boolean expression
     * @throws IllegalStateException if the supplied expression is {@code false}
     */
    public static void state(boolean expression) throws IllegalStateException {
        state(expression, "[Assertion failed] - this state invariant must be true");
    }

    /**
     * Assert that the given String contains valid text content; that is, it must not
     * be {@code null} and must contain at least one non-whitespace character.
     * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
     *
     * @param text    the String to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the text does not contain valid text content
     * @see StringUtils#hasText
     */
    public static void hasText(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

}
