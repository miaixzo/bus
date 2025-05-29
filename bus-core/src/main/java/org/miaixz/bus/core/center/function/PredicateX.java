/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * 可序列化的Predicate接口，支持异常抛出和逻辑组合操作。
 *
 * @param <T> 参数类型
 * @author Kimi Liu
 * @see Predicate
 * @since Java 17+
 */
@FunctionalInterface
public interface PredicateX<T> extends Predicate<T>, Serializable {

    /**
     * 组合多个PredicateX实例，执行短路逻辑与操作。
     *
     * @param predicates 要组合的PredicateX实例
     * @param <T>        参数类型
     * @return 组合后的PredicateX实例
     */
    @SafeVarargs
    static <T> PredicateX<T> multiAnd(final PredicateX<T>... predicates) {
        return Stream.of(predicates).reduce(PredicateX::and).orElseGet(() -> o -> true);
    }

    /**
     * 组合多个PredicateX实例，执行短路逻辑或操作。
     *
     * @param predicates 要组合的PredicateX实例
     * @param <T>        参数类型
     * @return 组合后的PredicateX实例
     */
    @SafeVarargs
    static <T> PredicateX<T> multiOr(final PredicateX<T>... predicates) {
        return Stream.of(predicates).reduce(PredicateX::or).orElseGet(() -> o -> false);
    }

    /**
     * 返回一个谓词，判断输入参数是否与目标对象相等。
     *
     * @param <T>       参数类型
     * @param targetRef 用于比较的目标对象引用，可能为null
     * @return PredicateX，判断输入参数是否与目标对象相等
     */
    static <T> PredicateX<T> isEqual(final Object... targetRef) {
        return (null == targetRef) ? Objects::isNull
                : object -> Stream.of(targetRef).allMatch(target -> target.equals(object));
    }

    /**
     * 对给定参数评估此谓词，可能抛出异常。
     *
     * @param t 输入参数
     * @return 如果参数匹配谓词返回true，否则返回false
     * @throws Throwable 可能抛出的异常
     */
    boolean testing(T t) throws Throwable;

    /**
     * 对给定参数评估此谓词，自动处理异常。
     *
     * @param t 输入参数
     * @return 如果参数匹配谓词返回true，否则返回false
     * @throws RuntimeException 包装后的运行时异常
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
     * 返回一个组合谓词，表示此谓词与另一个谓词的短路逻辑与。如果此谓词为false，则不评估另一个谓词。
     *
     * @param other 与此谓词进行逻辑与的谓词
     * @return 组合谓词，表示此谓词与other谓词的短路逻辑与
     * @throws NullPointerException 如果other为null
     */
    default PredicateX<T> and(final PredicateX<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) && other.test(t);
    }

    /**
     * 返回一个表示此谓词逻辑非的谓词。
     *
     * @return 表示此谓词逻辑非的谓词
     */
    @Override
    default PredicateX<T> negate() {
        return t -> !test(t);
    }

    /**
     * 返回一个组合谓词，表示此谓词与另一个谓词的短路逻辑或。如果此谓词为true，则不评估另一个谓词。
     *
     * @param other 与此谓词进行逻辑或的谓词
     * @return 组合谓词，表示此谓词与other谓词的短路逻辑或
     * @throws NullPointerException 如果other为null
     */
    default PredicateX<T> or(final PredicateX<? super T> other) {
        Objects.requireNonNull(other);
        return t -> test(t) || other.test(t);
    }

}