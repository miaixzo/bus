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

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 一些{@link Predicate}相关封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PredicateKit {

    /**
     * 创建一个匹配任何方法的方法匹配器
     *
     * @param <T> 参数类型
     * @return 方法匹配器
     */
    public static <T> Predicate<T> alwaysTrue() {
        return method -> true;
    }

    /**
     * 强制转换 {@code Predicate<? super T>} 为 {@code Predicate<T>}.
     *
     * @param <T>       参数类型
     * @param predicate {@link Predicate}
     * @return 强转后的{@link Predicate}
     */
    static <T> Predicate<T> coerce(final Predicate<? super T> predicate) {
        return (Predicate<T>) predicate;
    }

    /**
     * 反向条件
     *
     * @param predicate 条件
     * @param <T>       参数类型
     * @return 反向条件 {@link Predicate}
     */
    public static <T> Predicate<T> negate(final Predicate<T> predicate) {
        return predicate.negate();
    }

    /**
     * 多个条件转换为”与“复合条件，即所有条件都为true时，才返回true
     *
     * @param <T>        判断条件的对象类型
     * @param components 多个条件
     * @return 复合条件
     */
    public static <T> Predicate<T> and(final Iterable<Predicate<T>> components) {
        return StreamKit.of(components, false).reduce(Predicate::and).orElseGet(() -> o -> true);
    }

    /**
     * 多个条件转换为”与“复合条件，即所有条件都为true时，才返回true
     *
     * @param <T>        判断条件的对象类型
     * @param components 多个条件
     * @return 复合条件
     */
    @SafeVarargs
    public static <T> Predicate<T> and(final Predicate<T>... components) {
        return StreamKit.of(components).reduce(Predicate::and).orElseGet(() -> o -> true);
    }

    /**
     * 多个条件转换为”或“复合条件，即任意一个条件都为true时，返回true
     *
     * @param <T>        判断条件的对象类型
     * @param components 多个条件
     * @return 复合条件
     */
    public static <T> Predicate<T> or(final Iterable<Predicate<T>> components) {
        return StreamKit.of(components, false).reduce(Predicate::or).orElseGet(() -> o -> false);
    }

    /**
     * 多个条件转换为”或“复合条件，即任意一个条件都为true时，返回true
     *
     * @param <T>        判断条件的对象类型
     * @param components 多个条件
     * @return 复合条件
     */
    @SafeVarargs
    public static <T> Predicate<T> or(final Predicate<T>... components) {
        return StreamKit.of(components).reduce(Predicate::or).orElseGet(() -> o -> false);
    }

    /**
     * 用于组合多个方法匹配器的方法匹配器，即所有条件都为false时，才返回true，也可理解为，任一条件为true时，返回false
     *
     * @param <T>        判断条件的对象类型
     * @param components 多个条件
     * @return 复合条件
     */
    @SafeVarargs
    public static <T> Predicate<T> none(final Predicate<T>... components) {
        return t -> Stream.of(components).noneMatch(matcher -> matcher.test(t));
    }

}
