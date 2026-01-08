/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.time.Duration;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.thread.RetryableTask;

/**
 * Retry utility class. For more advanced custom functionality, please use the {@link RetryableTask} class.
 *
 * @author Kimi Liu
 * @see RetryableTask
 * @since Java 17+
 */
public class RetryKit {

    /**
     * Retries a task based on specified exceptions. This method has no return value.
     *
     * @param run         The task to execute.
     * @param maxAttempts The maximum number of retry attempts. If less than 1, no retries will occur, but the task will
     *                    be executed at least once.
     * @param delay       The delay between retries.
     * @param recover     The recovery task to run if the maximum number of retries is reached.
     * @param exs         The specified exception types that trigger a retry.
     */
    @SafeVarargs
    public static void ofException(
            final Runnable run,
            final long maxAttempts,
            final Duration delay,
            final Runnable recover,
            Class<? extends Throwable>... exs) {
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
     * Retries a task based on specified exceptions. This method has a return value.
     *
     * @param sup         The task to execute, which returns a value.
     * @param maxAttempts The maximum number of retry attempts.
     * @param delay       The delay between retries.
     * @param recover     The recovery task to run if retries fail, which returns a value.
     * @param exs         The specified exception types that trigger a retry.
     * @param <T>         The type of the result.
     * @return The result of the execution.
     */
    @SafeVarargs
    public static <T> T ofException(
            final Supplier<T> sup,
            final long maxAttempts,
            final Duration delay,
            final Supplier<T> recover,
            Class<? extends Throwable>... exs) {
        if (ArrayKit.isEmpty(exs)) {
            exs = ArrayKit.append(exs, RuntimeException.class);
        }
        return RetryableTask.retryForExceptions(sup, exs).maxAttempts(maxAttempts).delay(delay).execute().get()
                .orElseGet(recover);
    }

    /**
     * Retries a task based on a custom predicate. This method has no return value.
     *
     * @param run         The task to execute.
     * @param maxAttempts The maximum number of retry attempts.
     * @param delay       The delay between retries.
     * @param recover     The recovery task to run if retries fail.
     * @param predicate   A custom predicate to determine if a retry is needed. Returns `true` to retry.
     */
    public static void ofPredicate(
            final Runnable run,
            final long maxAttempts,
            final Duration delay,
            final Supplier<Void> recover,
            final BiPredicate<Void, Throwable> predicate) {
        RetryableTask.retryForPredicate(run, predicate).delay(delay).maxAttempts(maxAttempts).execute().get()
                .orElseGet(recover);
    }

    /**
     * Retries a task based on a custom predicate. This method has a return value.
     *
     * @param sup         The task to execute, which returns a value.
     * @param maxAttempts The maximum number of retry attempts.
     * @param delay       The delay between retries.
     * @param recover     The recovery task to run if retries fail, which returns a value.
     * @param predicate   A custom predicate to determine if a retry is needed. Returns `true` to retry.
     * @param <T>         The type of the result.
     * @return The result of the execution.
     */
    public static <T> T ofPredicate(
            final Supplier<T> sup,
            final long maxAttempts,
            final Duration delay,
            final Supplier<T> recover,
            final BiPredicate<T, Throwable> predicate) {
        return RetryableTask.retryForPredicate(sup, predicate).delay(delay).maxAttempts(maxAttempts).execute().get()
                .orElseGet(recover);
    }

}
