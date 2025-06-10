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
import java.util.function.BiPredicate;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * 可序列化的BiPredicate接口，支持异常抛出和逻辑组合操作。
 *
 * @param <T> 参数1的类型
 * @param <U> 参数2的类型
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface BiPredicateX<T, U> extends BiPredicate<T, U>, Serializable {

    /**
     * 对给定的两个参数评估此谓词，可能抛出异常。
     *
     * @param t 第一个输入参数
     * @param u 第二个输入参数
     * @return 如果参数匹配谓词返回true，否则返回false
     * @throws Throwable 可能抛出的异常
     */
    boolean testing(T t, U u) throws Throwable;

    /**
     * 对给定的两个参数评估此谓词，自动处理异常。
     *
     * @param t 第一个输入参数
     * @param u 第二个输入参数
     * @return 如果参数匹配谓词返回true，否则返回false
     * @throws RuntimeException 包装后的运行时异常
     */
    @Override
    default boolean test(final T t, final U u) {
        try {
            return testing(t, u);
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
    default BiPredicateX<T, U> and(final BiPredicateX<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (T t, U u) -> test(t, u) && other.test(t, u);
    }

    /**
     * 返回一个表示此谓词逻辑非的谓词。
     *
     * @return 表示此谓词逻辑非的谓词
     */
    @Override
    default BiPredicateX<T, U> negate() {
        return (T t, U u) -> !test(t, u);
    }

    /**
     * 返回一个组合谓词，表示此谓词与另一个谓词的短路逻辑或。如果此谓词为true，则不评估另一个谓词。
     *
     * @param other 与此谓词进行逻辑或的谓词
     * @return 组合谓词，表示此谓词与other谓词的短路逻辑或
     * @throws NullPointerException 如果other为null
     */
    default BiPredicateX<T, U> or(final BiPredicateX<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return (T t, U u) -> test(t, u) || other.test(t, u);
    }

}