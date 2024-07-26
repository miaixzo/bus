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
package org.miaixz.bus.core.lang;

import org.miaixz.bus.core.xyz.*;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 断言 断言某些对象或值是否符合规定，否则抛出异常。经常用于做变量检查
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Assert {

    private static final String TEMPLATE_VALUE_MUST_BE_BETWEEN_AND = "The value must be between {} and {}.";

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
    public static <X extends Throwable> void isTrue(final boolean expression, final Supplier<? extends X> supplier)
            throws X {
        if (!expression) {
            throw supplier.get();
        }
    }

    /**
     * 断言是否为真，如果为 {@code false} 抛出 {@code IllegalArgumentException} 异常
     *
     * <pre class="code">
     * Assert.isTrue(i &gt; 0, "The value must be greater than zero");
     * </pre>
     *
     * @param expression       布尔值
     * @param errorMsgTemplate 错误抛出异常附带的消息模板，变量用{}代替
     * @param params           参数列表
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(final boolean expression, final String errorMsgTemplate, final Object... params)
            throws IllegalArgumentException {
        isTrue(expression, () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 断言是否为真，如果为 {@code false} 抛出 {@code IllegalArgumentException} 异常
     *
     * <pre class="code">
     * Assert.isTrue(i &gt; 0);
     * </pre>
     *
     * @param expression 布尔值
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(final boolean expression) throws IllegalArgumentException {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    /**
     * 断言是否为假，如果为 {@code true} 抛出指定类型异常 并使用指定的函数获取错误信息返回
     * 
     * <pre class="code">
     * Assert.isFalse(i &gt; 0, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <X>           异常类型
     * @param expression    布尔值
     * @param errorSupplier 指定断言不通过时抛出的异常
     * @throws X if expression is {@code false}
     */
    public static <X extends Throwable> void isFalse(final boolean expression, final Supplier<X> errorSupplier)
            throws X {
        if (expression) {
            throw errorSupplier.get();
        }
    }

    /**
     * 断言是否为假，如果为 {@code true} 抛出 {@code IllegalArgumentException} 异常
     *
     * <pre class="code">
     * Assert.isFalse(i &lt; 0, "The value must not be negative");
     * </pre>
     *
     * @param expression       布尔值
     * @param errorMsgTemplate 错误抛出异常附带的消息模板，变量用{}代替
     * @param params           参数列表
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isFalse(final boolean expression, final String errorMsgTemplate, final Object... params)
            throws IllegalArgumentException {
        isFalse(expression, () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 断言是否为假，如果为 {@code true} 抛出 {@code IllegalArgumentException} 异常
     *
     * <pre class="code">
     * Assert.isFalse(i &lt; 0);
     * </pre>
     *
     * @param expression 布尔值
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isFalse(final boolean expression) throws IllegalArgumentException {
        isFalse(expression, "[Assertion failed] - this expression must be false");
    }

    /**
     * 断言对象是否为{@code null} ，如果不为{@code null} 抛出指定类型异常 并使用指定的函数获取错误信息返回
     * 
     * <pre class="code">
     * Assert.isNull(value, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <X>           异常类型
     * @param object        被检查的对象
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @throws X if the object is not {@code null}
     */
    public static <X extends Throwable> void isNull(final Object object, final Supplier<X> errorSupplier) throws X {
        if (null != object) {
            throw errorSupplier.get();
        }
    }

    /**
     * 断言对象是否为{@code null} ，如果不为{@code null} 抛出{@link IllegalArgumentException} 异常
     * 
     * <pre class="code">
     * Assert.isNull(value, "The value must be null");
     * </pre>
     *
     * @param object           被检查的对象
     * @param errorMsgTemplate 消息模板，变量使用{}表示
     * @param params           参数列表
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void isNull(final Object object, final String errorMsgTemplate, final Object... params)
            throws IllegalArgumentException {
        isNull(object, () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 断言对象是否为{@code null} ，如果不为{@code null} 抛出{@link IllegalArgumentException} 异常
     * 
     * <pre class="code">
     * Assert.isNull(value);
     * </pre>
     *
     * @param object 被检查对象
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void isNull(final Object object) throws IllegalArgumentException {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    /**
     * 断言对象是否不为{@code null} ，如果为{@code null} 抛出指定类型异常 并使用指定的函数获取错误信息返回
     * 
     * <pre class="code">
     * Assert.notNull(clazz, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <T>           被检查对象泛型类型
     * @param <X>           异常类型
     * @param object        被检查对象
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @return 被检查后的对象
     * @throws X if the object is {@code null}
     */
    public static <T, X extends Throwable> T notNull(final T object, final Supplier<X> errorSupplier) throws X {
        if (null == object) {
            throw errorSupplier.get();
        }
        return object;
    }

    /**
     * 断言对象是否不为{@code null} ，如果为{@code null} 抛出{@link IllegalArgumentException} 异常 Assert that an object is not
     * {@code null} .
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
    public static <T> T notNull(final T object, final String errorMsgTemplate, final Object... params)
            throws IllegalArgumentException {
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
    public static <T> T notNull(final T object) throws IllegalArgumentException {
        return notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    /**
     * 检查给定字符串是否为空，为空抛出自定义异常，并使用指定的函数获取错误信息返回。
     * 
     * <pre class="code">
     * Assert.notEmpty(name, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <X>           异常类型
     * @param <T>           字符串类型
     * @param text          被检查字符串
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @return 非空字符串
     * @throws X 被检查字符串为空抛出此异常
     * @see StringKit#isNotEmpty(CharSequence)
     */
    public static <T extends CharSequence, X extends Throwable> T notEmpty(final T text,
            final Supplier<X> errorSupplier) throws X {
        if (StringKit.isEmpty(text)) {
            throw errorSupplier.get();
        }
        return text;
    }

    /**
     * 检查给定字符串是否为空，为空抛出 {@link IllegalArgumentException}
     *
     * <pre class="code">
     * Assert.notEmpty(name, "Name must not be empty");
     * </pre>
     *
     * @param <T>              字符串类型
     * @param text             被检查字符串
     * @param errorMsgTemplate 错误消息模板，变量使用{}表示
     * @param params           参数
     * @return 非空字符串
     * @throws IllegalArgumentException 被检查字符串为空
     * @see StringKit#isNotEmpty(CharSequence)
     */
    public static <T extends CharSequence> T notEmpty(final T text, final String errorMsgTemplate,
            final Object... params) throws IllegalArgumentException {
        return notEmpty(text, () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 检查给定字符串是否为空，为空抛出 {@link IllegalArgumentException}
     *
     * <pre class="code">
     * Assert.notEmpty(name);
     * </pre>
     *
     * @param <T>  字符串类型
     * @param text 被检查字符串
     * @return 被检查的字符串
     * @throws IllegalArgumentException 被检查字符串为空
     * @see StringKit#isNotEmpty(CharSequence)
     */
    public static <T extends CharSequence> T notEmpty(final T text) throws IllegalArgumentException {
        return notEmpty(text,
                "[Assertion failed] - this String argument must have length; it must not be null or empty");
    }

    /**
     * 检查给定字符串是否为空白（null、空串或只包含空白符），为空抛出自定义异常。 并使用指定的函数获取错误信息返回
     * 
     * <pre class="code">
     * Assert.notBlank(name, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <X>              异常类型
     * @param <T>              字符串类型
     * @param text             被检查字符串
     * @param errorMsgSupplier 错误抛出异常附带的消息生产接口
     * @return 非空字符串
     * @throws X 被检查字符串为空白
     * @see StringKit#isNotBlank(CharSequence)
     */
    public static <T extends CharSequence, X extends Throwable> T notBlank(final T text,
            final Supplier<X> errorMsgSupplier) throws X {
        if (StringKit.isBlank(text)) {
            throw errorMsgSupplier.get();
        }
        return text;
    }

    /**
     * 检查给定字符串是否为空白（null、空串或只包含空白符），为空抛出 {@link IllegalArgumentException}
     *
     * <pre class="code">
     * Assert.notBlank(name, "Name must not be blank");
     * </pre>
     *
     * @param <T>              字符串类型
     * @param text             被检查字符串
     * @param errorMsgTemplate 错误消息模板，变量使用{}表示
     * @param params           参数
     * @return 非空字符串
     * @throws IllegalArgumentException 被检查字符串为空白
     * @see StringKit#isNotBlank(CharSequence)
     */
    public static <T extends CharSequence> T notBlank(final T text, final String errorMsgTemplate,
            final Object... params) throws IllegalArgumentException {
        return notBlank(text, () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 检查给定字符串是否为空白（null、空串或只包含空白符），为空抛出 {@link IllegalArgumentException}
     *
     * <pre class="code">
     * Assert.notBlank(name);
     * </pre>
     *
     * @param <T>  字符串类型
     * @param text 被检查字符串
     * @return 非空字符串
     * @throws IllegalArgumentException 被检查字符串为空白
     * @see StringKit#isNotBlank(CharSequence)
     */
    public static <T extends CharSequence> T notBlank(final T text) throws IllegalArgumentException {
        return notBlank(text,
                "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
    }

    /**
     * 断言给定字符串是否不被另一个字符串包含（即是否为子串），并使用指定的函数获取错误信息返回 如果非子串，返回子串，如果是子串，则抛出{@link IllegalArgumentException}异常。
     * 
     * <pre class="code">
     * Assert.notContain(name, "rod", () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return ");
     * });
     * </pre>
     *
     * @param <T>           字符串类型
     * @param <X>           异常类型
     * @param textToSearch  被搜索的字符串
     * @param substring     被检查的子串
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @return 被检查的子串
     * @throws X 非子串抛出异常
     * @see StringKit#contains(CharSequence, CharSequence)
     */
    public static <T extends CharSequence, X extends Throwable> T notContain(final CharSequence textToSearch,
            final T substring, final Supplier<X> errorSupplier) throws X {
        if (StringKit.contains(textToSearch, substring)) {
            throw errorSupplier.get();
        }
        return substring;
    }

    /**
     * 断言给定字符串是否不被另一个字符串包含（即是否为子串） 如果非子串，返回子串，如果是子串，则抛出{@link IllegalArgumentException}异常。
     * 
     * <pre class="code">
     * Assert.notContain(name, "rod", "Name must not contain 'rod'");
     * </pre>
     *
     * @param textToSearch     被搜索的字符串
     * @param subString        被检查的子串
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 被检查的子串
     * @throws IllegalArgumentException 非子串抛出异常
     */
    public static String notContain(final String textToSearch, final String subString, final String errorMsgTemplate,
            final Object... params) throws IllegalArgumentException {
        return notContain(textToSearch, subString,
                () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 断言给定字符串是否不被另一个字符串包含（即是否为子串），即subString是否不是textToSearch的子串。
     * 如果非子串，返回子串，如果是子串，则抛出{@link IllegalArgumentException}异常。
     * 
     * <pre class="code">
     * Assert.notContain(name, "rod");
     * </pre>
     *
     * @param textToSearch 被搜索的字符串
     * @param subString    被检查的子串
     * @return 被检查的子串
     * @throws IllegalArgumentException 非子串抛出异常
     */
    public static String notContain(final String textToSearch, final String subString) throws IllegalArgumentException {
        return notContain(textToSearch, subString,
                "[Assertion failed] - this String argument must not contain the substring [{}]", subString);
    }

    /**
     * 断言给定数组是否包含元素，数组必须不为 {@code null} 且至少包含一个元素 并使用指定的函数获取错误信息返回
     * 
     * <pre class="code">
     * Assert.notEmpty(array, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <T>           数组元素类型
     * @param <X>           异常类型
     * @param array         被检查的数组
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @return 被检查的数组
     * @throws X if the object array is {@code null} or has no elements
     * @see ArrayKit#isNotEmpty(Object[])
     */
    public static <T, X extends Throwable> T[] notEmpty(final T[] array, final Supplier<X> errorSupplier) throws X {
        if (ArrayKit.isEmpty(array)) {
            throw errorSupplier.get();
        }
        return array;
    }

    /**
     * 断言给定数组是否包含元素，数组必须不为 {@code null} 且至少包含一个元素
     * 
     * <pre class="code">
     * Assert.notEmpty(array, "The array must have elements");
     * </pre>
     *
     * @param <T>              数组元素类型
     * @param array            被检查的数组
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 被检查的数组
     * @throws IllegalArgumentException if the object array is {@code null} or has no elements
     */
    public static <T> T[] notEmpty(final T[] array, final String errorMsgTemplate, final Object... params)
            throws IllegalArgumentException {
        return notEmpty(array, () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 断言给定数组是否包含元素，数组必须不为 {@code null} 且至少包含一个元素
     * 
     * <pre class="code">
     * Assert.notEmpty(array, "The array must have elements");
     * </pre>
     *
     * @param <T>   数组元素类型
     * @param array 被检查的数组
     * @return 被检查的数组
     * @throws IllegalArgumentException if the object array is {@code null} or has no elements
     */
    public static <T> T[] notEmpty(final T[] array) throws IllegalArgumentException {
        return notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element");
    }

    /**
     * 断言给定数组是否不包含{@code null}元素，如果数组为空或 {@code null}将被认为不包含 并使用指定的函数获取错误信息返回
     * 
     * <pre class="code">
     * Assert.noNullElements(array, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return ");
     * });
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
    public static <T, X extends Throwable> T[] noNullElements(final T[] array, final Supplier<X> errorSupplier)
            throws X {
        if (ArrayKit.hasNull(array)) {
            throw errorSupplier.get();
        }
        return array;
    }

    /**
     * 断言给定数组是否不包含{@code null}元素，如果数组为空或 {@code null}将被认为不包含
     * 
     * <pre class="code">
     * Assert.noNullElements(array, "The array must not have null elements");
     * </pre>
     *
     * @param <T>              数组元素类型
     * @param array            被检查的数组
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 被检查的数组
     * @throws IllegalArgumentException if the object array contains a {@code null} element
     */
    public static <T> T[] noNullElements(final T[] array, final String errorMsgTemplate, final Object... params)
            throws IllegalArgumentException {
        return noNullElements(array, () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 断言给定数组是否不包含{@code null}元素，如果数组为空或 {@code null}将被认为不包含
     * 
     * <pre class="code">
     * Assert.noNullElements(array);
     * </pre>
     *
     * @param <T>   数组元素类型
     * @param array 被检查的数组
     * @return 被检查的数组
     * @throws IllegalArgumentException if the object array contains a {@code null} element
     */
    public static <T> T[] noNullElements(final T[] array) throws IllegalArgumentException {
        return noNullElements(array, "[Assertion failed] - this array must not contain any null elements");
    }

    /**
     * 断言给定集合非空 并使用指定的函数获取错误信息返回
     * 
     * <pre class="code">
     * Assert.notEmpty(collection, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <E>           集合元素类型
     * @param <T>           集合类型
     * @param <X>           异常类型
     * @param collection    被检查的集合
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @return 非空集合
     * @throws X if the collection is {@code null} or has no elements
     * @see CollKit#isNotEmpty(Iterable)
     */
    public static <E, T extends Iterable<E>, X extends Throwable> T notEmpty(final T collection,
            final Supplier<X> errorSupplier) throws X {
        if (CollKit.isEmpty(collection)) {
            throw errorSupplier.get();
        }
        return collection;
    }

    /**
     * 断言给定集合非空
     * 
     * <pre class="code">
     * Assert.notEmpty(collection, "Collection must have elements");
     * </pre>
     *
     * @param <E>              集合元素类型
     * @param <T>              集合类型
     * @param collection       被检查的集合
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 非空集合
     * @throws IllegalArgumentException if the collection is {@code null} or has no elements
     */
    public static <E, T extends Iterable<E>> T notEmpty(final T collection, final String errorMsgTemplate,
            final Object... params) throws IllegalArgumentException {
        return notEmpty(collection, () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 断言给定集合非空
     * 
     * <pre class="code">
     * Assert.notEmpty(collection);
     * </pre>
     *
     * @param <E>        集合元素类型
     * @param <T>        集合类型
     * @param collection 被检查的集合
     * @return 被检查集合
     * @throws IllegalArgumentException if the collection is {@code null} or has no elements
     */
    public static <E, T extends Iterable<E>> T notEmpty(final T collection) throws IllegalArgumentException {
        return notEmpty(collection,
                "[Assertion failed] - this collection must not be empty: it must contain at least 1 element");
    }

    /**
     * 断言给定Map非空 并使用指定的函数获取错误信息返回
     * 
     * <pre class="code">
     * Assert.notEmpty(map, () -&gt; {
     *     // to query relation message
     *     return new IllegalArgumentException("relation message to return");
     * });
     * </pre>
     *
     * @param <K>           Key类型
     * @param <V>           Value类型
     * @param <T>           Map类型
     * @param <X>           异常类型
     * @param map           被检查的Map
     * @param errorSupplier 错误抛出异常附带的消息生产接口
     * @return 被检查的Map
     * @throws X if the map is {@code null} or has no entries
     * @see MapKit#isNotEmpty(Map)
     */
    public static <K, V, T extends Map<K, V>, X extends Throwable> T notEmpty(final T map,
            final Supplier<X> errorSupplier) throws X {
        if (MapKit.isEmpty(map)) {
            throw errorSupplier.get();
        }
        return map;
    }

    /**
     * 断言给定Map非空
     * 
     * <pre class="code">
     * Assert.notEmpty(map, "Map must have entries");
     * </pre>
     *
     * @param <K>              Key类型
     * @param <V>              Value类型
     * @param <T>              Map类型
     * @param map              被检查的Map
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @return 被检查的Map
     * @throws IllegalArgumentException if the map is {@code null} or has no entries
     */
    public static <K, V, T extends Map<K, V>> T notEmpty(final T map, final String errorMsgTemplate,
            final Object... params) throws IllegalArgumentException {
        return notEmpty(map, () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 断言给定Map非空
     * 
     * <pre class="code">
     * Assert.notEmpty(map, "Map must have entries");
     * </pre>
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param <T> Map类型
     * @param map 被检查的Map
     * @return 被检查的Map
     * @throws IllegalArgumentException if the map is {@code null} or has no entries
     */
    public static <K, V, T extends Map<K, V>> T notEmpty(final T map) throws IllegalArgumentException {
        return notEmpty(map, "[Assertion failed] - this map must not be empty; it must contain at least one entry");
    }

    /**
     * 断言给定对象是否是给定类的实例
     * 
     * <pre class="code">
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
    public static <T> T isInstanceOf(final Class<?> type, final T obj) {
        return isInstanceOf(type, obj, "Object [{}] is not instanceof [{}]", obj, type);
    }

    /**
     * 断言给定对象是否是给定类的实例
     * 
     * <pre class="code">
     * Assert.instanceOf(Foo.class, foo, "foo must be an instance of class Foo");
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
    public static <T> T isInstanceOf(final Class<?> type, final T obj, final String errorMsgTemplate,
            final Object... params) throws IllegalArgumentException {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
        }
        return obj;
    }

    /**
     * 断言 {@code superType.isAssignableFrom(subType)} 是否为 {@code true}.
     * 
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass);
     * </pre>
     *
     * @param superType 需要检查的父类或接口
     * @param subType   需要检查的子类
     * @throws IllegalArgumentException 如果子类非继承父类，抛出此异常
     */
    public static void isAssignable(final Class<?> superType, final Class<?> subType) throws IllegalArgumentException {
        isAssignable(superType, subType, "{} is not assignable to {})", subType, superType);
    }

    /**
     * 断言 {@code superType.isAssignableFrom(subType)} 是否为 {@code true}.
     * 
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass, "myClass must can be assignable to class Number");
     * </pre>
     *
     * @param superType        需要检查的父类或接口
     * @param subType          需要检查的子类
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @throws IllegalArgumentException 如果子类非继承父类，抛出此异常
     */
    public static void isAssignable(final Class<?> superType, final Class<?> subType, final String errorMsgTemplate,
            final Object... params) throws IllegalArgumentException {
        notNull(superType, "Type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw new IllegalArgumentException(StringKit.format(errorMsgTemplate, params));
        }
    }

    /**
     * 检查boolean表达式，当检查结果为false时抛出 {@code IllegalStateException}。 并使用指定的函数获取错误信息返回
     * 
     * <pre class="code">
     * Assert.state(data == null, () -&gt; {
     *     // to query relation message
     *     return "relation message to return ";
     * });
     * </pre>
     *
     * @param expression       boolean 表达式
     * @param errorMsgSupplier 错误抛出异常附带的消息生产接口
     * @throws IllegalStateException 表达式为 {@code false} 抛出此异常
     */
    public static void state(final boolean expression, final Supplier<String> errorMsgSupplier)
            throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException(errorMsgSupplier.get());
        }
    }

    /**
     * 检查boolean表达式，当检查结果为false时抛出 {@code IllegalStateException}。
     * 
     * <pre class="code">
     * Assert.state(data == null, "The data property must not already be initialized");
     * </pre>
     *
     * @param expression       boolean 表达式
     * @param errorMsgTemplate 异常时的消息模板
     * @param params           参数列表
     * @throws IllegalStateException 表达式为 {@code false} 抛出此异常
     */
    public static void state(final boolean expression, final String errorMsgTemplate, final Object... params)
            throws IllegalStateException {
        if (!expression) {
            throw new IllegalStateException(StringKit.format(errorMsgTemplate, params));
        }
    }

    /**
     * 检查boolean表达式，当检查结果为false时抛出 {@code IllegalStateException}。
     * 
     * <pre class="code">
     * Assert.state(data == null);
     * </pre>
     *
     * @param expression boolean 表达式
     * @throws IllegalStateException 表达式为 {@code false} 抛出此异常
     */
    public static void state(final boolean expression) throws IllegalStateException {
        state(expression, "[Assertion failed] - this state invariant must be true");
    }

    /**
     * 检查下标（数组、集合、字符串）是否符合要求，下标必须满足：
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
    public static int checkIndex(final int index, final int size)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        return checkIndex(index, size, "[Assertion failed]");
    }

    /**
     * 检查下标（数组、集合、字符串）是否符合要求，下标必须满足：
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
    public static int checkIndex(final int index, final int size, final String errorMsgTemplate, final Object... params)
            throws IllegalArgumentException, IndexOutOfBoundsException {
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
     * @throws X if value is out of bound
     */
    public static <X extends Throwable> int checkBetween(final int value, final int min, final int max,
            final Supplier<? extends X> errorSupplier) throws X {
        if (value < min || value > max) {
            throw errorSupplier.get();
        }

        return value;
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value            值
     * @param min              最小值（包含）
     * @param max              最大值（包含）
     * @param errorMsgTemplate 异常信息模板，类似于"aa{}bb{}cc"
     * @param params           异常信息参数，用于替换"{}"占位符
     * @return 经过检查后的值
     */
    public static int checkBetween(final int value, final int min, final int max, final String errorMsgTemplate,
            final Object... params) {
        return checkBetween(value, min, max,
                () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value 值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @return 检查后的长度值
     */
    public static int checkBetween(final int value, final int min, final int max) {
        return checkBetween(value, min, max, TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max);
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
     * @throws X if value is out of bound
     */
    public static <X extends Throwable> long checkBetween(final long value, final long min, final long max,
            final Supplier<? extends X> errorSupplier) throws X {
        if (value < min || value > max) {
            throw errorSupplier.get();
        }

        return value;
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value            值
     * @param min              最小值（包含）
     * @param max              最大值（包含）
     * @param errorMsgTemplate 异常信息模板，类似于"aa{}bb{}cc"
     * @param params           异常信息参数，用于替换"{}"占位符
     * @return 经过检查后的值
     */
    public static long checkBetween(final long value, final long min, final long max, final String errorMsgTemplate,
            final Object... params) {
        return checkBetween(value, min, max,
                () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value 值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @return 检查后的长度值
     */
    public static long checkBetween(final long value, final long min, final long max) {
        return checkBetween(value, min, max, TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max);
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
     * @throws X if value is out of bound
     */
    public static <X extends Throwable> double checkBetween(final double value, final double min, final double max,
            final Supplier<? extends X> errorSupplier) throws X {
        if (value < min || value > max) {
            throw errorSupplier.get();
        }

        return value;
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value            值
     * @param min              最小值（包含）
     * @param max              最大值（包含）
     * @param errorMsgTemplate 异常信息模板，类似于"aa{}bb{}cc"
     * @param params           异常信息参数，用于替换"{}"占位符
     * @return 经过检查后的值
     */
    public static double checkBetween(final double value, final double min, final double max,
            final String errorMsgTemplate, final Object... params) {
        return checkBetween(value, min, max,
                () -> new IllegalArgumentException(StringKit.format(errorMsgTemplate, params)));
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value 值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @return 检查后的长度值
     */
    public static double checkBetween(final double value, final double min, final double max) {
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
    public static Number checkBetween(final Number value, final Number min, final Number max) {
        notNull(value);
        notNull(min);
        notNull(max);
        final double valueDouble = value.doubleValue();
        final double minDouble = min.doubleValue();
        final double maxDouble = max.doubleValue();
        if (valueDouble < minDouble || valueDouble > maxDouble) {
            throw new IllegalArgumentException(StringKit.format(TEMPLATE_VALUE_MUST_BE_BETWEEN_AND, min, max));
        }
        return value;
    }

    /**
     * 断言两个对象是否不相等,如果两个对象相等 抛出IllegalArgumentException 异常
     * 
     * <pre class="code">
     * Assert.notEquals(obj1, obj2);
     * </pre>
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @throws IllegalArgumentException obj1 must be not equals obj2
     */
    public static void notEquals(final Object obj1, final Object obj2) {
        notEquals(obj1, obj2, "({}) must be not equals ({})", obj1, obj2);
    }

    /**
     * 断言两个对象是否不相等,如果两个对象相等 抛出IllegalArgumentException 异常
     * 
     * <pre class="code">
     * Assert.notEquals(obj1, obj2, "obj1 must be not equals obj2");
     * </pre>
     *
     * @param obj1             对象1
     * @param obj2             对象2
     * @param errorMsgTemplate 异常信息模板，类似于"aa{}bb{}cc"
     * @param params           异常信息参数，用于替换"{}"占位符
     * @throws IllegalArgumentException obj1 must be not equals obj2
     */
    public static void notEquals(final Object obj1, final Object obj2, final String errorMsgTemplate,
            final Object... params) throws IllegalArgumentException {
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
    public static <X extends Throwable> void notEquals(final Object obj1, final Object obj2,
            final Supplier<X> errorSupplier) throws X {
        if (ObjectKit.equals(obj1, obj2)) {
            throw errorSupplier.get();
        }
    }

    /**
     * 断言两个对象是否相等,如果两个对象不相等 抛出IllegalArgumentException 异常
     * 
     * <pre class="code">
     * Assert.isEquals(obj1, obj2);
     * </pre>
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @throws IllegalArgumentException obj1 must be equals obj2
     */
    public static void equals(final Object obj1, final Object obj2) {
        equals(obj1, obj2, "({}) must be equals ({})", obj1, obj2);
    }

    /**
     * 断言两个对象是否相等,如果两个对象不相等 抛出IllegalArgumentException 异常
     * 
     * <pre class="code">
     * Assert.isEquals(obj1, obj2, "obj1 must be equals obj2");
     * </pre>
     *
     * @param obj1             对象1
     * @param obj2             对象2
     * @param errorMsgTemplate 异常信息模板，类似于"aa{}bb{}cc"
     * @param params           异常信息参数，用于替换"{}"占位符
     * @throws IllegalArgumentException obj1 must be equals obj2
     */
    public static void equals(final Object obj1, final Object obj2, final String errorMsgTemplate,
            final Object... params) throws IllegalArgumentException {
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
    public static <X extends Throwable> void equals(final Object obj1, final Object obj2,
            final Supplier<X> errorSupplier) throws X {
        if (ObjectKit.notEquals(obj1, obj2)) {
            throw errorSupplier.get();
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
    private static String badIndexMsg(final int index, final int size, final String desc, final Object... params) {
        if (index < 0) {
            return StringKit.format("{} ({}) must not be negative", StringKit.format(desc, params), index);
        } else if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        } else { // index >= size
            return StringKit.format("{} ({}) must be less than size ({})", StringKit.format(desc, params), index, size);
        }
    }

    /**
     * 断言给定的字符串包含有效的文本内容;也就是说，它不能是{@code null}，并且必须包含至少一个非空白字符。
     * 
     * <pre class="code">
     * Assert.hasText(name, "'name' must not be empty");
     * </pre>
     *
     * @param text    要检查的字符串
     * @param message 断言失败时要使用的异常消息
     * @throws IllegalArgumentException 如果文本不包含有效的文本内容
     * @see StringKit#hasText
     */
    public static void hasText(String text, String message) {
        if (!StringKit.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言给定的字符串包含有效的文本内容;也就是说，它不能是{@code null}，并且必须包含至少一个非空白字符。
     * 
     * <pre class="code">
     * Assert.hasText(account.getName(), () -&gt; "Name for account '" + account.getId() + "' must not be empty");
     * </pre>
     *
     * @param text            要检查的字符串
     * @param messageSupplier 断言失败时要使用的异常消息的提供者
     * @throws IllegalArgumentException 如果文本不包含有效的文本内容
     * @see StringKit#hasText
     */
    public static void hasText(String text, Supplier<String> messageSupplier) {
        if (!StringKit.hasText(text)) {
            throw new IllegalArgumentException((messageSupplier != null ? messageSupplier.get() : null));
        }
    }

}
