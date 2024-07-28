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

import org.miaixz.bus.core.lang.thread.RetryableTask;

import java.time.Duration;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * 重试工具类 自定义功能请使用{@link RetryableTask}类
 *
 * @author Kimi Liu
 * @see RetryableTask
 * @since Java 17+
 */
public class RetryKit {

    /**
     * 根据异常信息进行重试 没有返回值，重试执行方法
     *
     * @param run         执行方法
     * @param maxAttempts 最大的重试次数
     * @param delay       重试间隔
     * @param recover     达到最大重试次数后执行的备用方法，入参是重试过程中的异常
     * @param exs         指定的异常类型需要重试
     */
    public static void ofException(final Runnable run, final long maxAttempts, final Duration delay,
            final Runnable recover, Class<? extends Throwable>... exs) {
        if (ArrayKit.isEmpty(exs)) {
            exs = ArrayKit.append(exs, RuntimeException.class);
        }
        RetryableTask.retryForExceptions(run, exs).maxAttempts(maxAttempts).delay(delay).execute().get()
                .orElseGet(() -> {
                    recover.run();
                    return null;
                });
    }

    /**
     * 根据异常信息进行重试 有返回值，重试执行方法
     *
     * @param sup         执行方法
     * @param maxAttempts 最大的重试次数
     * @param delay       重试间隔
     * @param recover     达到最大重试次数后执行的备用方法，入参是重试过程中的异常
     * @param exs         指定的异常类型需要重试
     * @param <T>         结果类型
     * @return 执行结果
     */
    public static <T> T ofException(final Supplier<T> sup, final long maxAttempts, final Duration delay,
            final Supplier<T> recover, Class<? extends Throwable>... exs) {
        if (ArrayKit.isEmpty(exs)) {
            exs = ArrayKit.append(exs, RuntimeException.class);
        }
        return RetryableTask.retryForExceptions(sup, exs).maxAttempts(maxAttempts).delay(delay).execute().get()
                .orElseGet(recover);
    }

    /**
     * 根据自定义结果进行重试 没有返回值，重试执行方法
     *
     * @param run         执行方法
     * @param maxAttempts 最大的重试次数
     * @param delay       重试间隔
     * @param recover     达到最大重试次数后执行的备用方法，入参是重试过程中的异常
     * @param predicate   自定义重试条件
     */
    public static void ofPredicate(final Runnable run, final long maxAttempts, final Duration delay,
            final Supplier<Void> recover, final BiPredicate<Void, Throwable> predicate) {
        RetryableTask.retryForPredicate(run, predicate).delay(delay).maxAttempts(maxAttempts).execute().get()
                .orElseGet(recover);
    }

    /**
     * 根据异常信息进行重试 有返回值，重试执行方法
     *
     * @param sup         执行方法
     * @param maxAttempts 最大的重试次数
     * @param delay       重试间隔
     * @param recover     达到最大重试次数后执行的备用方法，入参是重试过程中的异常
     * @param predicate   自定义重试条件
     * @param <T>         结果类型
     * @return 执行结果
     */
    public static <T> T ofPredicate(final Supplier<T> sup, final long maxAttempts, final Duration delay,
            final Supplier<T> recover, final BiPredicate<T, Throwable> predicate) {
        return RetryableTask.retryForPredicate(sup, predicate).delay(delay).maxAttempts(maxAttempts).execute().get()
                .orElseGet(recover);
    }

}
