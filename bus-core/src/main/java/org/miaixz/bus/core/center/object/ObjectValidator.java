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
package org.miaixz.bus.core.center.object;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.text.CharsValidator;
import org.miaixz.bus.core.xyz.*;

/**
 * 对象检查工具类，提供字对象的blank和empty等检查
 * <ul>
 * <li>empty定义：{@code null} or 空字对象：{@code ""}</li>
 * <li>blank定义：{@code null} or 空字对象：{@code ""} or 空格、全角空格、制表符、换行符，等不可见字符</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ObjectValidator {

    /**
     * 检查对象是否为{@code null}
     *
     * @param object 对象
     * @return 是否为null
     */
    public static boolean isNull(final Object object) {
        return null == object;
    }

    /**
     * 检查对象是否不为{@code null}
     *
     * @param object 对象
     * @return 是否不为null
     */
    public static boolean isNotNull(final Object object) {
        return null != object;
    }

    /**
     * 判断指定对象是否为空，支持类型包括：
     * <ul>
     * <li>{@code null}：默认返回{@code true}；</li>
     * <li>数组：等同于{@link ArrayKit#isEmpty(Object)}；</li>
     * <li>{@link CharSequence}：等同于{@link CharsBacker#isEmpty(CharSequence)}；</li>
     * <li>{@link Collection}：等同于{@link CollKit#isEmpty(Collection)}；</li>
     * <li>{@link Map}：等同于{@link MapKit#isEmpty(Map)}；</li>
     * <li>{@link Iterator}或{@link Iterable}：等同于{@link IteratorKit#isEmpty(Iterator)}、
     * {@link IteratorKit#isEmpty(Iterable)}；</li>
     * </ul>
     *
     * @param object 被判断的对象
     * @return 是否为空，如果类型不支持，返回false
     * @see StringKit#isEmpty(CharSequence)
     * @see MapKit#isEmpty(Map)
     * @see IteratorKit#isEmpty(Iterable)
     * @see IteratorKit#isEmpty(Iterator)
     * @see ArrayKit#isEmpty(Object)
     */
    public static boolean isEmpty(final Object object) {
        if (null == object) {
            return true;
        }

        if (object instanceof CharSequence) {
            return StringKit.isEmpty((CharSequence) object);
        } else if (object instanceof Collection) {
            return CollKit.isEmpty((Collection) object);
        } else if (object instanceof Map) {
            return MapKit.isEmpty((Map) object);
        } else if (object instanceof Iterable) {
            return IteratorKit.isEmpty((Iterable) object);
        } else if (object instanceof Iterator) {
            return IteratorKit.isEmpty((Iterator) object);
        } else if (ArrayKit.isArray(object)) {
            return ArrayKit.isEmpty(object);
        }

        return false;
    }

    /**
     * 判断指定对象是否为非空
     *
     * @param object 被判断的对象
     * @return 是否不为空，如果类型不支持，返回true
     * @see #isEmpty(Object)
     */
    public static boolean isNotEmpty(final Object object) {
        return !isEmpty(object);
    }

    /**
     * 如果对象是字符串是否为空白，空白的定义如下：
     * <ol>
     * <li>{@code null}</li>
     * <li>空字符串：{@code ""}</li>
     * <li>空格、全角空格、制表符、换行符，等不可见字符</li>
     * </ol>
     * <ul>
     * <li>{@code isBlankIfString(null)     // true}</li>
     * <li>{@code isBlankIfString("")       // true}</li>
     * <li>{@code isBlankIfString(" \t\n")  // true}</li>
     * <li>{@code isBlankIfString("abc")    // false}</li>
     * </ul>
     * 注意：该方法与 {@link #isEmptyIfString(Object)} 的区别是： 该方法会校验空白字符，且性能相对于 {@link #isEmptyIfString(Object)} 略慢。
     *
     * @param object 对象
     * @return 如果为字符串是否为空串
     * @see StringKit#isBlank(CharSequence)
     */
    public static boolean isBlankIfString(final Object object) {
        if (null == object) {
            return true;
        } else if (object instanceof CharSequence) {
            return CharsValidator.isBlank((CharSequence) object);
        }
        return false;
    }

    /**
     * 如果对象是字符串是否为空串，空的定义如下：
     * <ol>
     * <li>{@code null}</li>
     * <li>空字符串：{@code ""}</li>
     * </ol>
     * <ul>
     * <li>{@code isEmptyIfString(null)     // true}</li>
     * <li>{@code isEmptyIfString("")       // true}</li>
     * <li>{@code isEmptyIfString(" \t\n")  // false}</li>
     * <li>{@code isEmptyIfString("abc")    // false}</li>
     * </ul>
     *
     * <p>
     * 注意：该方法与 {@link #isBlankIfString(Object)} 的区别是：该方法不校验空白字符。
     * </p>
     *
     * @param object 对象
     * @return 如果为字符串是否为空串
     */
    public static boolean isEmptyIfString(final Object object) {
        if (null == object) {
            return true;
        } else if (object instanceof CharSequence) {
            return 0 == ((CharSequence) object).length();
        }
        return false;
    }

    /**
     * 如果给定对象为{@code null}返回默认值
     * 
     * <pre>{@code
     * ObjectKit.defaultIfNull(null, null);      // = null
     * ObjectKit.defaultIfNull(null, "");        // = ""
     * ObjectKit.defaultIfNull(null, "zz");      // = "zz"
     * ObjectKit.defaultIfNull("abc", *);        // = "abc"
     * ObjectKit.defaultIfNull(Boolean.TRUE, *); // = Boolean.TRUE
     * }</pre>
     *
     * @param <T>          对象类型
     * @param object       被检查对象，可能为{@code null}
     * @param defaultValue 被检查对象为{@code null}返回的默认值，可以为{@code null}
     * @return 被检查对象不为 {@code null} 返回原值，否则返回默认值
     */
    public static <T> T defaultIfNull(final T object, final T defaultValue) {
        return isNull(object) ? defaultValue : object;
    }

    /**
     * 如果给定对象不为{@code null} 返回原值, 否则返回 {@link Supplier#get()} 提供的默认值
     *
     * @param <T>             被检查对象类型
     * @param source          被检查对象，可能为{@code null}
     * @param defaultSupplier 为空时的默认值提供者
     * @return 被检查对象不为 {@code null} 返回原值，否则返回 {@link Supplier#get()} 提供的默认值
     */
    public static <T> T defaultIfNull(final T source, final Supplier<? extends T> defaultSupplier) {
        if (isNotNull(source)) {
            return source;
        }
        return defaultSupplier.get();
    }

    /**
     * 如果给定对象不为{@code null} 返回自定义handler处理后的结果，否则返回默认值
     *
     * @param <R>          返回值类型
     * @param <T>          被检查对象类型
     * @param source       被检查对象，可能为{@code null}
     * @param handler      非空时自定义的处理方法
     * @param defaultValue 为空时的默认返回值
     * @return 被检查对象不为 {@code null} 返回处理后的结果，否则返回默认值
     */
    public static <T, R> R defaultIfNull(final T source, final Function<? super T, ? extends R> handler,
            final R defaultValue) {
        return isNull(source) ? defaultValue : handler.apply(source);
    }

    /**
     * 如果给定对象不为{@code null} 返回自定义handler处理后的结果，否则返回 {@link Supplier#get()} 提供的默认值
     *
     * @param <R>             返回值类型
     * @param <T>             被检查对象类型
     * @param source          被检查对象，可能为{@code null}
     * @param handler         非空时自定义的处理方法
     * @param defaultSupplier 为空时的默认值提供者
     * @return 被检查对象不为 {@code null} 返回处理后的结果，否则返回 {@link Supplier#get()} 提供的默认值
     */
    public static <T, R> R defaultIfNull(final T source, final Function<? super T, ? extends R> handler,
            final Supplier<? extends R> defaultSupplier) {
        if (isNotNull(source)) {
            return handler.apply(source);
        }
        return defaultSupplier.get();
    }

    /**
     * 比较两个对象是否相等，满足下述任意条件即返回{@code true}：
     * <ul>
     * <li>若两对象皆为{@link BigDecimal}，且满足{@code 0 == obj1.compareTo(obj2)}</li>
     * <li>若两对象都为数组，调用Arrays.equals完成判断</li>
     * <li>{@code obj1 == null && obj2 == null}</li>
     * <li>{@code obj1.equals(obj2)}</li>
     * </ul>
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否相等
     */
    public static boolean equals(final Object obj1, final Object obj2) {
        if (obj1 instanceof Number && obj2 instanceof Number) {
            return MathKit.equals((Number) obj1, (Number) obj2);
        } else if (ArrayKit.isArray(obj1) && ArrayKit.isArray(obj2)) {
            return ArrayKit.equals(obj1, obj2);
        }
        return Objects.equals(obj1, obj2);
    }

    /**
     * 比较两个对象是否不相等
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否不等
     * @see #equals(Object, Object)
     */
    public static boolean notEquals(final Object obj1, final Object obj2) {
        return !equals(obj1, obj2);
    }

    /**
     * 是否为基本类型，包括包装类型和原始类型
     *
     * @param object 被检查对象，{@code null}返回{@code false}
     * @return 是否为基本类型
     * @see ClassKit#isBasicType(Class)
     */
    public static boolean isBasicType(final Object object) {
        if (null == object) {
            return false;
        }
        return ClassKit.isBasicType(object.getClass());
    }

    /**
     * 是否为有效的数字，主要用于检查浮点数是否为有意义的数值 若对象不为{@link Number}类型，则直接返回{@code true}，否则：
     * <ul>
     * <li>若对象类型为{@link Double}，则检查{@link Double#isInfinite()}或{@link Double#isNaN()}；</li>
     * <li>若对象类型为{@link Float}，则检查{@link Float#isInfinite()}或{@link Float#isNaN()}；</li>
     * </ul>
     *
     * @param object 被检查对象
     * @return 检查结果，非数字类型和{@code null}将返回{@code true}
     * @see MathKit#isValidNumber(Number)
     */
    public static boolean isValidIfNumber(final Object object) {
        if (object instanceof Number) {
            return MathKit.isValidNumber((Number) object);
        }
        return true;
    }

}
