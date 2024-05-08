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
package org.miaixz.bus.core.lang;

import org.miaixz.bus.core.lang.function.SupplierX;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 断言
 * 断言某些对象或值是否符合规定,否则抛出异常 经常用于做变量检查
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Assert {

    private static final String TEMPLATE_VALUE_MUST_BE_BETWEEN_AND = "The value must be between {} and {}.";

    /**
     * 断言是否为真，如果为 {@code false} 抛出异常
     * 并使用指定的函数获取错误信息返回
     * <pre class="code">
     *  Assert.isTrue(i &gt; 0, () -&gt; {
     *      return "relation message to return";
     *  });
     * </pre>
     *
     * @param expression       布尔值
     * @param errorMsgSupplier 错误抛出异常附带的消息生产接口
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(boolean expression, Supplier<String> errorMsgSupplier) throws IllegalArgumentException {
        if (!expression) {
            isTrue(false, errorMsgSupplier.get());
        }
    }

    /**
     * 断言是否为真，如果为 {@code false} 抛出给定的异常
     *
     * <pre class="code">
     * Assert.isTrue(i &gt; 0, IllegalArgumentException::new);
     * </pre>
     *
     * @param <X>        异常类型
     * @param expression 布尔值
     * @param supplier   指定断言不通过时抛出的异常
     * @throws X if expression is {@code false}
     */
    public static <X extends Throwable> void isTrue(boolean expression, SupplierX<? extends X> supplier) throws X {
        if (false == expression) {
            throw supplier.get();
        }
    }

    /**
     * 断言是否为真,如果为 {@code false} 抛出 {@code IllegalArgumentException} 异常
     *
     * <pre class="criteria">
     * Assert.isTrue(i &gt; 0, "The value must be greater than zero");
     * </pre>
     *
     * @param expression       布尔值
     * @param errorMsgTemplate 错误抛出异常附带的消息模板,变量用{}代替
     * @param params           参数列表
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(boolean expression, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (false == expression) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
        }
    }

    /**
     * 断言是否为真,如果为 {@code false} 抛出 {@code IllegalArgumentException} 异常
     *
     * <pre class="criteria">
     * Assert.isTrue(i &gt; 0, "The value must be greater than zero");
     * </pre>
     *
     * @param expression 布尔值
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(boolean expression) throws IllegalArgumentException {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    /**
     * 断言是否为假，如果为 {@code true} 抛出 {@code IllegalArgumentException} 异常
     * 并使用指定的函数获取错误信息返回
     * <pre class="code">
     *  Assert.isFalse(i &gt; 0, () -&gt; {
     *      return "relation message to return";
     *  });
     * </pre>
     *
     * @param expression       布尔值
     * @param errorMsgSupplier 错误抛出异常附带的消息生产接口
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isFalse(boolean expression, Supplier<String> errorMsgSupplier) throws IllegalArgumentException {
        if (expression) {
            isFalse(true, errorMsgSupplier.get());
        }
    }

    /**
     * 断言是否为假,如果为 {@code true} 抛出 {@code IllegalArgumentException} 异常
     *
     * <pre class="criteria">
     * Assert.isFalse(i &lt; 0, "The value must be greater than zero");
     * </pre>
     *
     * @param expression       布尔值
     * @param errorMsgTemplate 错误抛出异常附带的消息模板,变量用{}代替
     * @param params           参数列表
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isFalse(boolean expression, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (expression) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
        }
    }

    /**
     * 断言是否为假,如果为 {@code true} 抛出 {@code IllegalArgumentException} 异常
     *
     * <pre class="criteria">
     * Assert.isFalse(i &lt; 0);
     * </pre>
     *
     * @param expression 布尔值
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isFalse(boolean expression) throws IllegalArgumentException {
        isFalse(expression, "[Assertion failed] - this expression must be false");
    }

    /**
     * 断言对象是否为{@code null} ，如果不为{@code null} 抛出{@link IllegalArgumentException} 异常
     * 并使用指定的函数获取错误信息返回
     * <pre class="code">
     * Assert.isNull(value,  () -&gt; {
     *      return "relation message to return";
     *  });
     * </pre>
     *
     * @param object           被检查的对象
     * @param errorMsgSupplier 错误抛出异常附带的消息生产接口
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void isNull(Object object, Supplier<String> errorMsgSupplier) throws IllegalArgumentException {
        if (null != object) {
            isNull(object, errorMsgSupplier.get());
        }
    }

    /**
     * 断言对象是否为{@code null} ,如果不为{@code null} 抛出{@link IllegalArgumentException} 异常
     *
     * <pre class="criteria">
     * Assert.isNull(value, "The value must be null");
     * </pre>
     *
     * @param object           被检查的对象
     * @param errorMsgTemplate 消息模板,变量使用{}表示
     * @param params           参数列表
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void isNull(Object object, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (null != object) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
        }
    }

    /**
     * 断言对象是否为{@code null} ,如果不为{@code null} 抛出{@link IllegalArgumentException} 异常
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
     * 断言对象是否不为{@code null} ，如果为{@code null} 抛出指定类型异常
     * 并使用指定的函数获取错误信息返回
     * <pre class="code">
     * Assert.notNull(clazz, ()-&gt;{
     *      return new IllegalArgumentException("relation message to return");
     *  });
     * </pre>
     *
     * @param <T>           被检查对象泛型类型
     * @param <X>           异常类型
     * @param object        被检查对象
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @return 被检查后的对象
     * @throws X if the object is {@code null}
     */
    public static <T, X extends Throwable> T notNull(T object, Supplier<X> errorSupplier) throws X {
        if (ObjectKit.isNull(object)) {
            throw errorSupplier.get();
        }
        return object;
    }

    /**
     * 断言对象是否不为{@code null} ，如果为{@code null} 抛出{@link IllegalArgumentException} 异常 Assert that an object is not {@code null} .
     *
     * <pre class="code">
     * Assert.notNull(clazz, "The class must not be null");
     * </pre>
     *
     * @param <T>              被检查对象泛型类型
     * @param object           被检查对象
     * @param errorMsgTemplate 错误消息模板，变量使用{}表示
     * @param params           参数
     * @return 被检查后的对象
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static <T> T notNull(T object, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        return notNull(object, () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 断言对象是否不为{@code null} ，如果为{@code null} 抛出{@link IllegalArgumentException} 异常
     *
     * <pre class="code">
     * Assert.notNull(clazz);
     * </pre>
     *
     * @param <T>    被检查对象类型
     * @param object 被检查对象
     * @return 非空对象
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static <T> T notNull(T object) throws IllegalArgumentException {
        return notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    /**
     * 检查给定字符串是否为空，为空抛出 {@link IllegalArgumentException}
     * 并使用指定的函数获取错误信息返回
     * <pre class="code">
     * Assert.notEmpty(name, () -&gt; {
     *      return "relation message to return";
     *  });
     * </pre>
     *
     * @param <T>              字符串类型
     * @param text             被检查字符串
     * @param errorMsgSupplier 错误抛出异常附带的消息生产接口
     * @return 非空字符串
     * @throws IllegalArgumentException 被检查字符串为空
     * @see StringKit#isNotEmpty(CharSequence)
     */
    public static <T extends CharSequence> T notEmpty(T text, Supplier<String> errorMsgSupplier) throws IllegalArgumentException {
        if (StringKit.isEmpty(text)) {
            notEmpty(text, errorMsgSupplier.get());
        }
        return text;
    }

    /**
     * 检查给定字符串是否为空,为空抛出 {@link IllegalArgumentException}
     *
     * <pre class="criteria">
     * Assert.notEmpty(name, "Name must not be empty");
     * </pre>
     *
     * @param <T>              字符串类型
     * @param text             被检查字符串
     * @param errorMsgTemplate 错误消息模板，变量使用{}表示
     * @param params           参数
     * @return 非空字符串
     * @throws IllegalArgumentException 被检查字符串为空
     */
    public static <T extends CharSequence> T notEmpty(T text, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (StringKit.isEmpty(text)) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
        }
        return text;
    }

    /**
     * 检查给定字符串是否为空,为空抛出 {@link IllegalArgumentException}
     *
     * <pre class="criteria">
     * Assert.notEmpty(name);
     * </pre>
     *
     * @param <T>  字符串类型
     * @param text 被检查字符串
     * @return 被检查的字符串
     * @throws IllegalArgumentException 被检查字符串为空
     */
    public static <T extends CharSequence> T notEmpty(T text) throws IllegalArgumentException {
        return notEmpty(text, "[Assertion failed] - this String argument must have length; it must not be null or empty");
    }

    /**
     * 检查给定字符串是否为空白（null、空串或只包含空白符），为空抛出 {@link IllegalArgumentException}
     * 并使用指定的函数获取错误信息返回
     * <pre class="code">
     * Assert.notBlank(name, () -&gt; {
     *      return "relation message to return";
     *  });
     * </pre>
     *
     * @param <T>              字符串类型
     * @param text             被检查字符串
     * @param errorMsgSupplier 错误抛出异常附带的消息生产接口
     * @return 非空字符串
     * @throws IllegalArgumentException 被检查字符串为空白
     * @see StringKit#isNotBlank(CharSequence)
     */
    public static <T extends CharSequence> T notBlank(T text, Supplier<String> errorMsgSupplier) throws IllegalArgumentException {
        if (StringKit.isBlank(text)) {
            notBlank(text, errorMsgSupplier.get());
        }
        return text;
    }

    /**
     * 检查给定字符串是否为空白(null、空串或只包含空白符),为空抛出 {@link IllegalArgumentException}
     *
     * <pre class="criteria">
     * Assert.notBlank(name, "Name must not be blank");
     * </pre>
     *
     * @param <T>              字符串类型
     * @param text             被检查字符串
     * @param errorMsgTemplate 错误消息模板,变量使用{}表示
     * @param params           参数
     * @return 非空字符串
     * @throws IllegalArgumentException 被检查字符串为空白
     * @see StringKit#isNotBlank(CharSequence)
     */
    public static <T extends CharSequence> T notBlank(T text, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (StringKit.isBlank(text)) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
        }
        return text;
    }

    /**
     * 检查给定字符串是否为空白(null、空串或只包含空白符),为空抛出 {@link IllegalArgumentException}
     *
     * <pre class="criteria">
     * Assert.notBlank(name, "Name must not be blank");
     * </pre>
     *
     * @param <T>  字符串类型
     * @param text 被检查字符串
     * @return 非空字符串
     * @throws IllegalArgumentException 被检查字符串为空白
     */
    public static <T extends CharSequence> T notBlank(T text) throws IllegalArgumentException {
        return notBlank(text, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
    }

    /**
     * 断言给定字符串是否不被另一个字符串包含（即是否为子串）
     * 并使用指定的函数获取错误信息返回
     * <pre class="code">
     * Assert.doesNotContain(name, "rod", () -&gt; {
     *      return "relation message to return";
     *  });
     * </pre>
     *
     * @param textToSearch     被搜索的字符串
     * @param substring        被检查的子串
     * @param errorMsgSupplier 错误抛出异常附带的消息生产接口
     * @return 被检查的子串
     * @throws IllegalArgumentException 非子串抛出异常
     */
    public static String notContain(String textToSearch, String substring, Supplier<String> errorMsgSupplier) throws IllegalArgumentException {
        if (StringKit.isNotEmpty(textToSearch) && StringKit.isNotEmpty(substring) && textToSearch.contains(substring)) {
            throw new IllegalArgumentException(errorMsgSupplier.get());
        }
        return substring;
    }

    /**
     * 断言给定字符串是否不被另一个字符串包含(既是否为子串)
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
        if (StringKit.isNotEmpty(textToSearch) && StringKit.isNotEmpty(substring) && textToSearch.contains(substring)) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
        }
        return substring;
    }

    /**
     * 断言给定字符串是否不被另一个字符串包含(既是否为子串)
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
     * 并使用指定的函数获取错误信息返回
     * <pre class="code">
     * Assert.notEmpty(array, () -&gt; {
     *      return "relation message to return";
     *  });
     * </pre>
     *
     * @param array            被检查的数组
     * @param errorMsgSupplier 错误抛出异常附带的消息生产接口
     * @return 被检查的数组
     * @throws IllegalArgumentException if the object array is {@code null} or has no elements
     */
    public static Object[] notEmpty(Object[] array, Supplier<String> errorMsgSupplier) throws IllegalArgumentException {
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException(errorMsgSupplier.get());
        }
        return array;
    }

    /**
     * 断言给定数组是否包含元素,数组必须不为 {@code null} 且至少包含一个元素
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
        if (ArrayKit.isEmpty(array)) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
        }
        return array;
    }

    /**
     * 断言给定数组是否包含元素,数组必须不为 {@code null} 且至少包含一个元素
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
     * 并使用指定的函数获取错误信息返回
     * <pre class="code">
     * Assert.noNullElements(array, ()-&gt;{
     *      return new IllegalArgumentException("relation message to return ");
     *  });
     * </pre>
     *
     * @param <T>           数组元素类型
     * @param <X>           异常类型
     * @param array         被检查的数组
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @return 被检查的数组
     * @throws X if the object array contains a {@code null} element
     * @see ArrayKit#hasNull(Object[])
     */
    public static <T, X extends Throwable> T[] noNullElements(T[] array, Supplier<X> errorSupplier) throws X {
        if (ArrayKit.hasNull(array)) {
            throw errorSupplier.get();
        }
        return array;
    }

    /**
     * 断言给定数组是否不包含{@code null}元素,如果数组为空或 {@code null}将被认为不包含
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
        if (ArrayKit.hasNull(array)) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
        }
        return array;
    }

    /**
     * 断言给定数组是否不包含{@code null}元素,如果数组为空或 {@code null}将被认为不包含
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
     * 并使用指定的函数获取错误信息返回
     * <pre class="code">
     * Assert.notEmpty(collection, () -&gt; {
     *      return "relation message to return";
     *  });
     * </pre>
     *
     * @param <T>              集合元素类型
     * @param collection       被检查的集合
     * @param errorMsgSupplier 错误抛出异常附带的消息生产接口
     * @return 非空集合
     * @throws IllegalArgumentException if the collection is {@code null} or has no elements
     */
    public static <T> Collection<T> notEmpty(Collection<T> collection, Supplier<String> errorMsgSupplier) throws IllegalArgumentException {
        if (CollKit.isEmpty(collection)) {
            throw new IllegalArgumentException(errorMsgSupplier.get());
        }
        return collection;
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
        if (CollKit.isEmpty(collection)) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
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
     * 并使用指定的函数获取错误信息返回
     * <pre class="code">
     * Assert.notEmpty(map, () -&gt; {
     *      return "relation message to return";
     *  });
     * </pre>
     *
     * @param <K>              Key类型
     * @param <V>              Value类型
     * @param map              被检查的Map
     * @param errorMsgSupplier 错误抛出异常附带的消息生产接口
     * @return 被检查的Map
     * @throws IllegalArgumentException if the map is {@code null} or has no entries
     */
    public static <K, V> Map<K, V> notEmpty(Map<K, V> map, Supplier<String> errorMsgSupplier) throws IllegalArgumentException {
        if (CollKit.isEmpty(map)) {
            throw new IllegalArgumentException(errorMsgSupplier.get());
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
     * @param <K>              Key类型
     * @param <V>              Value类型
     * @param map              被检查的Map
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 被检查的Map
     * @throws IllegalArgumentException if the map is {@code null} or has no entries
     */
    public static <K, V> Map<K, V> notEmpty(Map<K, V> map, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        if (CollKit.isEmpty(map)) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
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
     * 断言两个对象是否不相等,如果两个对象相等 抛出IllegalArgumentException 异常
     * <pre class="code">
     *   Assert.notEquals(obj1,obj2);
     * </pre>
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @throws IllegalArgumentException obj1 must be not equals obj2
     */
    public static void notEquals(Object obj1, Object obj2) {
        notEquals(obj1, obj2, "({}) must be not equals ({})", obj1, obj2);
    }

    /**
     * 断言两个对象是否不相等,如果两个对象相等 抛出IllegalArgumentException 异常
     * <pre class="code">
     *   Assert.notEquals(obj1,obj2,"obj1 must be not equals obj2");
     * </pre>
     *
     * @param obj1             对象1
     * @param obj2             对象2
     * @param errorMsgTemplate 异常信息模板，类似于"aa{}bb{}cc"
     * @param params           异常信息参数，用于替换"{}"占位符
     * @throws IllegalArgumentException obj1 must be not equals obj2
     */
    public static void notEquals(Object obj1, Object obj2, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        notEquals(obj1, obj2, () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 断言两个对象是否不相等,如果两个对象相等,抛出指定类型异常,并使用指定的函数获取错误信息返回
     *
     * @param obj1          对象1
     * @param obj2          对象2
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @param <X>           异常类型
     * @throws X obj1 must be not equals obj2
     */
    public static <X extends Throwable> void notEquals(Object obj1, Object obj2, Supplier<X> errorSupplier) throws X {
        if (ObjectKit.equals(obj1, obj2)) {
            throw errorSupplier.get();
        }
    }

    /**
     * 断言两个对象是否相等,如果两个对象不相等 抛出IllegalArgumentException 异常
     * <pre class="code">
     *   Assert.isEquals(obj1,obj2);
     * </pre>
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @throws IllegalArgumentException obj1 must be equals obj2
     */
    public static void equals(Object obj1, Object obj2) {
        equals(obj1, obj2, "({}) must be equals ({})", obj1, obj2);
    }

    /**
     * 断言两个对象是否相等,如果两个对象不相等 抛出IllegalArgumentException 异常
     * <pre class="code">
     *   Assert.isEquals(obj1,obj2,"obj1 must be equals obj2");
     * </pre>
     *
     * @param obj1             对象1
     * @param obj2             对象2
     * @param errorMsgTemplate 异常信息模板，类似于"aa{}bb{}cc"
     * @param params           异常信息参数，用于替换"{}"占位符
     * @throws IllegalArgumentException obj1 must be equals obj2
     */
    public static void equals(Object obj1, Object obj2, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        equals(obj1, obj2, () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 断言两个对象是否相等,如果两个对象不相等,抛出指定类型异常,并使用指定的函数获取错误信息返回
     *
     * @param obj1          对象1
     * @param obj2          对象2
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @param <X>           异常类型
     * @throws X obj1 must be equals obj2
     */
    public static <X extends Throwable> void equals(Object obj1, Object obj2, Supplier<X> errorSupplier) throws X {
        if (ObjectKit.notEquals(obj1, obj2)) {
            throw errorSupplier.get();
        }
    }

    /**
     * 断言给定对象是否是给定类的实例
     *
     * <pre class="criteria">
     * Assert.instanceOf(Foo.class, foo);
     * </pre>
     *
     * @param <T>    被检查对象泛型类型
     * @param type   被检查对象匹配的类型
     * @param object 被检查对象
     * @return 被检查的对象
     * @throws IllegalArgumentException if the object is not an instance of clazz
     * @see Class#isInstance(Object)
     */
    public static <T> T isInstanceOf(Class<?> type, T object) {
        return isInstanceOf(type, object, "Object [{}] is not instanceof [{}]", object, type);
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
     * @param object           被检查对象
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 被检查对象
     * @throws IllegalArgumentException if the object is not an instance of clazz
     * @see Class#isInstance(Object)
     */
    public static <T> T isInstanceOf(Class<?> type, T object, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        notNull(type, "Type to check against must not be null");
        if (false == type.isInstance(object)) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
        }
        return object;
    }

    /**
     * {@code superType.isAssignableFrom(subType)} 是否为 {@code true}
     *
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass);
     * </pre>
     *
     * @param superType 需要检查的父类或接口
     * @param subType   需要检查的子类
     * @throws IllegalArgumentException 如果子类非继承父类，抛出此异常
     */
    public static void isAssignable(Class<?> superType, Class<?> subType) throws IllegalArgumentException {
        isAssignable(superType, subType, "{} is not assignable to {})", subType, superType);
    }

    /**
     * {@code superType.isAssignableFrom(subType)} 是否为 {@code true}
     *
     * <pre>
     * Assert.isAssignable(Number.class, myClass);
     * </pre>
     *
     * @param superType        需要检查的父类或接口
     * @param subType          需要检查的子类
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @throws IllegalArgumentException 如果子类非继承父类，抛出此异常
     */
    public static void isAssignable(Class<?> superType, Class<?> subType, String errorMsgTemplate, Object... params) throws IllegalArgumentException {
        notNull(superType, "Type to check against must not be null");
        if (null == subType || !superType.isAssignableFrom(subType)) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
        }
    }

    /**
     * 检查boolean表达式，当检查结果为false时抛出 {@code IllegalStateException}
     * 并使用指定的函数获取错误信息返回
     * <pre class="code">
     * Assert.state(null == id, () -&gt; {
     *      return "relation message to return";
     *  });
     * </pre>
     *
     * @param expression       boolean 表达式
     * @param errorMsgSupplier 错误抛出异常附带的消息生产接口
     * @throws IllegalStateException 表达式为 {@code false} 抛出此异常
     */
    public static void state(boolean expression, Supplier<String> errorMsgSupplier) throws IllegalStateException {
        if (false == expression) {
            throw new IllegalStateException(errorMsgSupplier.get());
        }
    }

    /**
     * 检查boolean表达式，当检查结果为false时抛出 {@code IllegalStateException}.
     *
     * <pre class="criteria">
     * Assert.state(null == id, "The id property must not already be initialized");
     * </pre>
     *
     * @param expression       boolean 表达式
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @throws IllegalStateException if expression is {@code false}
     */
    public static void state(boolean expression, String errorMsgTemplate, Object... params) throws IllegalStateException {
        if (false == expression) {
            throw new IllegalStateException(StringKit.format(errorMsgTemplate, params));
        }
    }

    /**
     * 检查boolean表达式，当检查结果为false时抛出 {@code IllegalStateException}.
     *
     * <pre class="criteria">
     * Assert.state(null == id);
     * </pre>
     *
     * @param expression boolean 表达式
     * @throws IllegalStateException 表达式为 {@code false} 抛出此异常
     */
    public static void state(boolean expression) throws IllegalStateException {
        state(expression, "[Assertion failed] - this state invariant must be true");
    }

    /**
     * 断言给定的字符串包含有效的文本内容.
     * <pre>
     *   Assert.hasText(name, "'name' must not be empty");
     * </pre>
     *
     * @param text    被检查字符串
     * @param message 如果断言失败，要使用的异常消息
     * @throws IllegalArgumentException 表达式为 {@code false} 抛出此异常
     */
    public static void hasText(String text, String message) {
        if (!StringKit.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 错误的下标时显示的消息
     *
     * @param index  下标
     * @param size   长度
     * @param desc   异常时的消息模板
     * @param params 参数列表
     * @return 消息
     */
    private static String badIndexMsg(int index, int size, String desc, Object... params) {
        if (index < 0) {
            return StringKit.format("{} ({}) must not be negative", StringKit.format(desc, params), index);
        } else if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        } else {
            return StringKit.format("{} ({}) must be less than size ({})", StringKit.format(desc, params), index, size);
        }
    }

    /**
     * 检查下标(数组、集合、字符串)是否符合要求，下标必须满足：
     *
     * <pre>
     * 0 &le; index &lt; size
     * </pre>
     *
     * @param index 下标
     * @param size  长度
     * @return 检查后的下标
     * @throws IllegalArgumentException  如果size &lt; 0 抛出此异常
     * @throws IndexOutOfBoundsException 如果index &lt; 0或者 index &ge; size 抛出此异常
     */
    public static int checkIndex(int index, int size) throws IllegalArgumentException, IndexOutOfBoundsException {
        return checkIndex(index, size, "[Assertion failed]");
    }

    /**
     * 检查下标(数组、集合、字符串)是否符合要求，下标必须满足：
     *
     * <pre>
     * 0 &le; index &lt; size
     * </pre>
     *
     * @param index            下标
     * @param size             长度
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 检查后的下标
     * @throws IllegalArgumentException  如果size &lt; 0 抛出此异常
     * @throws IndexOutOfBoundsException 如果index &lt; 0或者 index &ge; size 抛出此异常
     */
    public static int checkIndex(int index, int size, String errorMsgTemplate, Object... params) throws IllegalArgumentException, IndexOutOfBoundsException {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(badIndexMsg(index, size, errorMsgTemplate, params));
        }
        return index;
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param <X>           异常类型
     * @param value         值
     * @param min           最小值（包含）
     * @param max           最大值（包含）
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @return 经过检查后的值
     * @throws X 如果值超出界限
     */
    public static <X extends Throwable> int checkBetween(int value, int min, int max, Supplier<? extends X> errorSupplier) throws X {
        if (value < min || value > max) {
            throw errorSupplier.get();
        }

        return value;
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value    值
     * @param min      最小值（包含）
     * @param max      最大值（包含）
     * @param template 模板信息
     * @param params   参数
     * @return 经过检查后的值
     */
    public static int checkBetween(int value, int min, int max, String template, Object... params) {
        return checkBetween(value, min, max, () -> new IllegalArgumentException(StringKit.format(template, params)));
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value 值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @return 检查后的长度值
     */
    public static int checkBetween(int value, int min, int max) {
        return checkBetween(value, min, max, TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max);
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param <X>           泛型对象
     * @param value         值
     * @param min           最小值（包含）
     * @param max           最大值（包含）
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @return 经过检查后的值
     * @throws X 如果值超出界限
     */
    public static <X extends Throwable> long checkBetween(long value, long min, long max, Supplier<? extends X> errorSupplier) throws X {
        if (value < min || value > max) {
            throw errorSupplier.get();
        }

        return value;
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value    值
     * @param min      最小值（包含）
     * @param max      最大值（包含）
     * @param template 模板信息
     * @param params   参数
     * @return 经过检查后的值
     */
    public static long checkBetween(long value, long min, long max, String template, Object... params) {
        return checkBetween(value, min, max, () -> new IllegalArgumentException(StringKit.format(template, params)));
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value 值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @return 检查后的长度值
     */
    public static long checkBetween(long value, long min, long max) {
        return checkBetween(value, min, max, TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max);
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param <X>           泛型对象
     * @param value         值
     * @param min           最小值（包含）
     * @param max           最大值（包含）
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @return 经过检查后的值
     * @throws X 如果值超出界限
     */
    public static <X extends Throwable> double checkBetween(double value, double min, double max, Supplier<? extends X> errorSupplier) throws X {
        if (value < min || value > max) {
            throw errorSupplier.get();
        }

        return value;
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value    值
     * @param min      最小值（包含）
     * @param max      最大值（包含）
     * @param template 模板信息
     * @param params   参数
     * @return 经过检查后的值
     */
    public static double checkBetween(double value, double min, double max, String template, Object... params) {
        return checkBetween(value, min, max, () -> new IllegalArgumentException(StringKit.format(template, params)));
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value 值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @return 检查后的长度值
     */
    public static double checkBetween(double value, double min, double max) {
        return checkBetween(value, min, max, TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max);
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value 值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @return 检查后的长度值
     */
    public static Number checkBetween(Number value, Number min, Number max) {
        notNull(value);
        notNull(min);
        notNull(max);
        double valueDouble = value.doubleValue();
        double minDouble = min.doubleValue();
        double maxDouble = max.doubleValue();
        if (valueDouble < minDouble || valueDouble > maxDouble) {
            throw new IllegalArgumentException(StringKit.format(TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max));
        }
        return value;
    }

}
