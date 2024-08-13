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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import org.miaixz.bus.core.center.object.ObjectValidator;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * 对象工具类，包括判空、克隆、序列化等操作
 * 原数组相关操作见：{@link ArrayKit#hasBlank(CharSequence...)}、{@link ArrayKit#isAllBlank(CharSequence...)}等等
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ObjectKit extends ObjectValidator {

    /**
     * 计算对象长度，支持类型包括：
     * <ul>
     * <li>{@code null}：默认返回{@code 0}；</li>
     * <li>数组：返回数组长度；</li>
     * <li>{@link CharSequence}：返回{@link CharSequence#length()}；</li>
     * <li>{@link Collection}：返回{@link Collection#size()}；</li>
     * <li>{@link Iterator}或{@link Iterable}：可迭代的元素数量；</li>
     * <li>{@link Enumeration}：返回可迭代的元素数量；</li>
     * </ul>
     *
     * @param obj 被计算长度的对象
     * @return 长度
     */
    public static int length(final Object obj) {
        if (obj == null) {
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

        int count = 0;
        if (obj instanceof Iterator || obj instanceof Iterable) {
            final Iterator<?> iter = (obj instanceof Iterator) ? (Iterator<?>) obj : ((Iterable<?>) obj).iterator();
            while (iter.hasNext()) {
                count++;
                iter.next();
            }
            return count;
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj);
        }
        if (obj instanceof Enumeration) {
            final Enumeration<?> enumeration = (Enumeration<?>) obj;
            while (enumeration.hasMoreElements()) {
                count++;
                enumeration.nextElement();
            }
            return count;
        }
        return -1;
    }

    /**
     * 检查{@code obj}中是否包含{@code element}，若{@code obj}为{@code null}，则直接返回{@code false}。 支持类型包括：
     * <ul>
     * <li>{@code null}：默认返回{@code false}；</li>
     * <li>{@link String}：等同{@link String#contains(CharSequence)}；</li>
     * <li>{@link Collection}：等同{@link Collection#contains(Object)}；</li>
     * <li>{@link Map}：等同{@link Map#containsValue(Object)}；</li>
     * <li>{@link Iterator}、{@link Iterable}、{@link Enumeration}或数组： 等同于遍历后对其元素调用{@link #equals(Object, Object)}方法；</li>
     * </ul>
     *
     * @param obj     对象
     * @param element 元素
     * @return 是否包含
     */
    public static boolean contains(final Object obj, final Object element) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            if (element == null) {
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

        if (obj instanceof Iterator || obj instanceof Iterable) {
            final Iterator<?> iter = obj instanceof Iterator ? (Iterator<?>) obj : ((Iterable<?>) obj).iterator();
            while (iter.hasNext()) {
                final Object o = iter.next();
                if (equals(o, element)) {
                    return true;
                }
            }
            return false;
        }
        if (obj instanceof Enumeration) {
            final Enumeration<?> enumeration = (Enumeration<?>) obj;
            while (enumeration.hasMoreElements()) {
                final Object o = enumeration.nextElement();
                if (equals(o, element)) {
                    return true;
                }
            }
            return false;
        }
        if (ArrayKit.isArray(obj)) {
            final int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                final Object o = Array.get(obj, i);
                if (equals(o, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 如果指定的对象不为 {@code null},则应用提供的映射函数并返回结果,否则返回 {@code null}。
     *
     * @param source  要检查的对象
     * @param handler 要应用的映射函数
     * @param <T>     输入对象的类型
     * @param <R>     映射函数的返回类型
     * @return 映射函数的结果, 如果输入对象为 null,则返回 null
     */
    public static <T, R> R apply(final T source, final Function<T, R> handler) {
        return defaultIfNull(source, handler, (R) null);
    }

    /**
     * 如果指定的对象不为 {@code null},则执行{@link Consumer}处理source，否则不进行操作
     *
     * @param source   要检查的对象
     * @param consumer source处理逻辑
     * @param <T>      输入对象的类型
     */
    public static <T> void accept(final T source, final Consumer<T> consumer) {
        if (null != source) {
            consumer.accept(source);
        }
    }

    /**
     * 克隆对象
     * <ol>
     * <li>如果对象是数组，则等同于{@link ArrayKit#clone(Object)}；</li>
     * <li>如果对象实现了{@link Cloneable}接口，调用 {@link Object#clone()}方法；</li>
     * <li>如果对象实现了{@link Serializable}接口，执行深度克隆；</li>
     * <li>不符合上述任意情况则返回{@code null}；</li>
     * </ol>
     *
     * @param <T> 对象类型
     * @param obj 被克隆对象
     * @return 克隆后的对象
     * @see ArrayKit#clone(Object)
     * @see Object#clone()
     * @see #cloneByStream(Object)
     */
    public static <T> T clone(final T obj) {
        final T result = ArrayKit.clone(obj);
        if (null != result) {
            // 数组
            return result;
        }

        if (obj instanceof Cloneable) {
            try {
                return MethodKit.invoke(obj, "clone");
            } catch (final InternalException e) {
                if (e.getCause() instanceof IllegalAccessException) {
                    // JDK9+下可能无权限
                    return cloneByStream(obj);
                } else {
                    throw e;
                }
            }
        }

        return cloneByStream(obj);
    }

    /**
     * 返回克隆后的对象，如果克隆失败，返回原对象
     *
     * @param <T> 对象类型
     * @param obj 对象
     * @return 克隆对象或原对象
     * @see #clone(Object)
     */
    public static <T> T cloneIfPossible(final T obj) {
        T clone = null;
        try {
            clone = clone(obj);
        } catch (final Exception e) {
            // pass
        }
        return clone == null ? obj : clone;
    }

    /**
     * 序列化后拷贝流的方式克隆 若对象未实现{@link Serializable}接口，则返回{@code null}
     *
     * @param <T> 对象类型
     * @param obj 被克隆对象
     * @return 克隆后的对象
     * @throws InternalException IO异常和ClassNotFoundException封装
     * @see SerializeKit#clone(Object)
     */
    public static <T> T cloneByStream(final T obj) {
        return SerializeKit.clone(obj);
    }

    /**
     * 获得给定类的第一个泛型参数
     *
     * @param obj 被检查的实体对象
     * @return {@link Class}
     */
    public static Class<?> getTypeArgument(final Object obj) {
        return getTypeArgument(obj, 0);
    }

    /**
     * 获得给定类指定下标的泛型参数
     *
     * @param obj   被检查的实体对象
     * @param index 泛型类型的索引号，即第几个泛型类型
     * @return {@link Class}
     * @see ClassKit#getTypeArgument(Class, int)
     */
    public static Class<?> getTypeArgument(final Object obj, final int index) {
        return ClassKit.getTypeArgument(obj.getClass(), index);
    }

    /**
     * 确定给定的对象是否相等，如果两个对象都是{@code null}， 则返回{@code true};如果只有一个对象是{@code null}， 则返回{@code false}
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
            return ArrayKit.arrayEquals(o1, o2);
        }
        return false;
    }

}
