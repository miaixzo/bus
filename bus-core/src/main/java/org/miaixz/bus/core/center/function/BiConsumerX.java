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
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * 可序列化的BiConsumer接口，支持异常抛出和多个消费者组合。
 *
 * @param <T> 第一个参数类型
 * @param <U> 第二个参数类型
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface BiConsumerX<T, U> extends BiConsumer<T, U>, Serializable {

    /**
     * 组合多个BiConsumerX实例，按顺序执行。
     *
     * @param consumers 要组合的BiConsumerX实例
     * @param <T>       第一个参数类型
     * @param <U>       第二个参数类型
     * @return 组合后的BiConsumerX实例
     */
    @SafeVarargs
    static <T, U> BiConsumerX<T, U> multi(final BiConsumerX<T, U>... consumers) {
        return Stream.of(consumers).reduce(BiConsumerX::andThen).orElseGet(() -> (o, q) -> {
        });
    }

    /**
     * 返回一个空操作的BiConsumerX，用于占位。
     *
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return 空操作的BiConsumerX实例
     */
    static <T, U> BiConsumerX<T, U> nothing() {
        return (l, r) -> {
        };
    }

    /**
     * 对给定的两个参数执行操作，可能抛出异常。
     *
     * @param t 第一个输入参数
     * @param u 第二个输入参数
     * @throws Throwable 可能抛出的异常
     */
    void accepting(T t, U u) throws Throwable;

    /**
     * 对给定的两个参数执行操作，自动处理异常。
     *
     * @param t 第一个输入参数
     * @param u 第二个输入参数
     * @throws RuntimeException 包装后的运行时异常
     */
    @Override
    default void accept(final T t, final U u) {
        try {
            accepting(t, u);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * 返回一个组合的BiConsumerX，先执行当前操作，再执行after操作。
     *
     * @param after 在当前操作后执行的操作
     * @return 组合后的BiConsumerX实例，按顺序执行当前操作和after操作
     * @throws NullPointerException 如果after为null
     */
    default BiConsumerX<T, U> andThen(final BiConsumerX<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (l, r) -> {
            accepting(l, r);
            after.accepting(l, r);
        };
    }

}