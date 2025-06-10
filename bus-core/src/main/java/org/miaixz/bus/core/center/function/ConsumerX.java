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
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * 可序列化的Consumer接口，支持异常抛出和多个消费者组合。
 *
 * @param <T> 参数类型
 * @author Kimi Liu
 * @see Consumer
 * @since Java 17+
 */
@FunctionalInterface
public interface ConsumerX<T> extends Consumer<T>, Serializable {

    /**
     * 组合多个ConsumerX实例，按顺序执行。
     *
     * @param consumers 要组合的ConsumerX实例
     * @param <T>       参数类型
     * @return 组合后的ConsumerX实例
     */
    @SafeVarargs
    static <T> ConsumerX<T> multi(final ConsumerX<T>... consumers) {
        return Stream.of(consumers).reduce(ConsumerX::andThen).orElseGet(() -> o -> {
        });
    }

    /**
     * 返回一个空操作的ConsumerX，用于占位。
     *
     * @param <T> 参数类型
     * @return 空操作的ConsumerX实例
     */
    static <T> ConsumerX<T> nothing() {
        return t -> {
        };
    }

    /**
     * 对给定参数执行操作，可能抛出异常。
     *
     * @param t 输入参数
     * @throws Throwable 可能抛出的异常
     */
    void accepting(T t) throws Throwable;

    /**
     * 对给定参数执行操作，自动处理异常。
     *
     * @param t 输入参数
     * @throws RuntimeException 包装后的运行时异常
     */
    @Override
    default void accept(final T t) {
        try {
            accepting(t);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * 返回一个组合的ConsumerX，先执行当前操作，再执行after操作。
     *
     * @param after 在当前操作后执行的操作
     * @return 组合后的ConsumerX实例，按顺序执行当前操作和after操作
     * @throws NullPointerException 如果after为null
     */
    default ConsumerX<T> andThen(final ConsumerX<? super T> after) {
        Objects.requireNonNull(after);
        return (final T t) -> {
            accept(t);
            after.accept(t);
        };
    }

}