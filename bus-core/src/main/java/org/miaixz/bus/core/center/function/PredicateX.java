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
package org.miaixz.bus.core.center.function;

import org.miaixz.bus.core.xyz.ExceptionKit;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 可序列化的Predicate
 *
 * @param <T> 参数类型
 * @author Kimi Liu
 * @see Predicate
 * @since Java 17+
 */
@FunctionalInterface
public interface PredicateX<T> extends Predicate<T>, Serializable {

    /**
     * multi
     *
     * @param predicates lambda
     * @param <T>        类型
     * @return lambda
     */
    @SafeVarargs
    static <T> PredicateX<T> multiAnd(final PredicateX<T>... predicates) {
        return Stream.of(predicates).reduce(PredicateX::and).orElseGet(() -> o -> true);
    }

    /**
     * multi
     *
     * @param predicates lambda
     * @param <T>        类型
     * @return lambda
     */
    @SafeVarargs
    static <T> PredicateX<T> multiOr(final PredicateX<T>... predicates) {
        return Stream.of(predicates).reduce(PredicateX::or).orElseGet(() -> o -> false);
    }

    /**
     * Returns a predicate that tests if two arguments are equal according to {@link Objects#equals(Object, Object)}.
     *
     * @param <T>       the type of arguments to the predicate
     * @param targetRef the object reference with which to compare for equality, which may be {@code null}
     * @return a predicate that tests if two arguments are equal according to {@link Objects#equals(Object, Object)}
     */
    static <T> PredicateX<T> isEqual(final Object... targetRef) {
        return (null == targetRef) ? Objects::isNull
                : object -> Stream.of(targetRef).allMatch(target -> target.equals(object));
    }

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     * @throws Exception wrapped checked exception
     */
    boolean testing(T t) throws Throwable;

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    @Override
    default boolean test(final T t) {
        try {
            return testing(t);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of this predicate and another. When
     * evaluating the composed predicate, if this predicate is {@code false}, then the {@code other} predicate is not
     * evaluated.
     *
     * <p>
     * Any exception thrown during evaluation of either predicate are relayed to the caller; if evaluation of this
     * predicate throws an exception, the {@code other} predicate will not be evaluated.
     *
     * @param other a predicate that will be logically-ANDed with this predicate
     * @return a composed predicate that represents the short-circuiting logical AND of this predicate and the
     *         {@code other} predicate
     * @throws NullPointerException if other is null
     */
    default PredicateX<T> and(final PredicateX<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) && other.test(t);
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     *
     * @return a predicate that represents the logical negation of this predicate
     */
    @Override
    default PredicateX<T> negate() {
        return t -> !test(t);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of this predicate and another. When
     * evaluating the composed predicate, if this predicate is {@code true}, then the {@code other} predicate is not
     * evaluated.
     *
     * <p>
     * Any exception thrown during evaluation of either predicate are relayed to the caller; if evaluation of this
     * predicate throws an exception, the {@code other} predicate will not be evaluated.
     *
     * @param other a predicate that will be logically-ORed with this predicate
     * @return a composed predicate that represents the short-circuiting logical OR of this predicate and the
     *         {@code other} predicate
     * @throws NullPointerException if other is null
     */
    default PredicateX<T> or(final PredicateX<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) || other.test(t);
    }

}
