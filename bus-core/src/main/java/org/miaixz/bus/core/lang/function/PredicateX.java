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
package org.miaixz.bus.core.lang.function;

import org.miaixz.bus.core.exception.InternalException;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 表示一个参数的谓词（布尔值函数）
 *
 * @param <T> 输入类型
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface PredicateX<T> extends Predicate<T>, Serializable {

    /**
     * 执行断言函数
     *
     * @param predicates 断言函数
     * @param <T>        输入类型
     * @return lambda
     */
    @SafeVarargs
    static <T> PredicateX<T> multiAnd(final PredicateX<T>... predicates) {
        return Stream.of(predicates).reduce(PredicateX::and).orElseGet(() -> o -> true);
    }

    /**
     * 执行断言函数
     *
     * @param predicates 断言函数
     * @param <T>        输入类型
     * @return lambda
     */
    @SafeVarargs
    static <T> PredicateX<T> multiOr(final PredicateX<T>... predicates) {
        return Stream.of(predicates).reduce(PredicateX::or).orElseGet(() -> o -> false);
    }

    /**
     * 返回一个断言，根据 {@link Objects#equals(Object, Object)} 测试两个参数是否相等
     *
     * @param <T>       参数类型
     * @param targetRef 用于比较相等性的对象引用，可能是 {@code null}
     * @return 根据 {@link Objects#equals(Object, Object)} 测试两个参数是否相等的谓词
     */
    static <T> PredicateX<T> isEqual(final Object... targetRef) {
        return (null == targetRef)
                ? Objects::isNull
                : object -> Stream.of(targetRef).allMatch(target -> target.equals(object));
    }

    /**
     * 根据给定参数评估
     *
     * @param t 输入参数
     * @return {@code true} 如果输入参数匹配，否则 {@code false}
     * @throws Exception 包装的检查异常
     */
    boolean testing(T t) throws Exception;

    /**
     * 根据给定参数评估
     *
     * @param t 输入参数
     * @return {@code true} 如果输入参数匹配，否则 {@code false}
     */
    @Override
    default boolean test(final T t) {
        try {
            return testing(t);
        } catch (final Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 返回一个组合断言函数，该谓词表示此断言函数与另一个断言函数的短路逻辑与
     * 在评估组合断言函数时，如果此断言函数为 {@code false}，则不评估 {@code other} 断言函数
     *
     * @param other 将与该断言函数进行逻辑与运算的断言函数
     * @return 一个组合断言函数，表示此断言函数与 {@code other} 断言函数的短路逻辑与
     * @throws NullPointerException 如果其他为空
     */
    default PredicateX<T> and(final PredicateX<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) && other.test(t);
    }

    /**
     * 返回表示此断言函数的逻辑否定的断言函数。
     *
     * @return 表示此断言函数的逻辑否定的断言函数
     */
    @Override
    default PredicateX<T> negate() {
        return t -> !test(t);
    }

    /**
     * 返回一个组合断言函数，该断言函数表示此谓词与另一个断言函数的短路逻辑或
     * 在评估组合断言函数时，如果此断言函数为 {@code true}，则不评估 {@code other} 断言函数
     *
     * @param other 将与此断言函数进行逻辑或的断言函数
     * @return 表示此断言函数与 {@code other} 断言函数的短路逻辑 OR 的组合断言函数
     * @throws NullPointerException 如果其他为空
     */
    default PredicateX<T> or(final PredicateX<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) || other.test(t);
    }

}
