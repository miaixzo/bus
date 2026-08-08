/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.xyz;

import java.time.Duration;

import org.miaixz.bus.core.center.function.BiPredicateX;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.thread.RetryableTask;

/**
 * Retry class. For more advanced custom functionality, please use the {@link RetryableTask} class.
 *
 * @see RetryableTask
 * @author Kimi Liu
 * @since Java 21+
 */
public class RetryKit {

    /**
     * Creates an instance for retrying tasks after configured failures.
     */
    public RetryKit() {
        // No initialization required.
    }

    /**
     * Retries a task based on specified exceptions. This method has no return value.
     *
     * @param runnable       The task to execute.
     * @param maxAttempts    The maximum number of retry attempts. Must be greater than 0.
     * @param delay          The delay between retries.
     * @param recovery       The recovery task to run if the maximum number of retries is reached.
     * @param retryableTypes The specified exception types that trigger a retry.
     */
    @SafeVarargs
    public static void ofException(
            final Runnable runnable,
            final long maxAttempts,
            final Duration delay,
            final Runnable recovery,
            Class<? extends Throwable>... retryableTypes) {
        if (ArrayKit.isEmpty(retryableTypes)) {
            retryableTypes = ArrayKit.append(retryableTypes, RuntimeException.class);
        }
        final RetryableTask<?> task = RetryableTask.retryForExceptions(runnable, retryableTypes)
                .maxAttempts(maxAttempts).delay(delay).execute();
        if (!task.success()) {
            recovery.run();
        }
    }

    /**
     * Retries a task based on specified exceptions. This method has a return value.
     *
     * @param supplier       The task to execute, which returns a value.
     * @param maxAttempts    The maximum number of retry attempts. Must be greater than 0.
     * @param delay          The delay between retries.
     * @param recovery       The recovery task to run if retries fail, which returns a value.
     * @param retryableTypes The specified exception types that trigger a retry.
     * @param <T>            The type of the result.
     * @return The result of the execution.
     */
    @SafeVarargs
    public static <T> T ofException(
            final SupplierX<T> supplier,
            final long maxAttempts,
            final Duration delay,
            final SupplierX<T> recovery,
            Class<? extends Throwable>... retryableTypes) {
        if (ArrayKit.isEmpty(retryableTypes)) {
            retryableTypes = ArrayKit.append(retryableTypes, RuntimeException.class);
        }
        final RetryableTask<T> task = RetryableTask.retryForExceptions(supplier, retryableTypes)
                .maxAttempts(maxAttempts).delay(delay).execute();
        return task.success() ? task.get().orElse(null) : recovery.get();
    }

    /**
     * Retries a task based on a custom predicate. This method has no return value.
     *
     * @param runnable    The task to execute.
     * @param maxAttempts The maximum number of retry attempts. Must be greater than 0.
     * @param delay       The delay between retries.
     * @param recovery    The recovery task to run if retries fail.
     * @param predicate   A custom predicate to determine if a retry is needed. Returns `true` to retry.
     */
    public static void ofPredicate(
            final Runnable runnable,
            final long maxAttempts,
            final Duration delay,
            final SupplierX<Void> recovery,
            final BiPredicateX<Void, Throwable> predicate) {
        final RetryableTask<?> task = RetryableTask.retryForPredicate(runnable, predicate).delay(delay)
                .maxAttempts(maxAttempts).execute();
        if (!task.success()) {
            recovery.get();
        }
    }

    /**
     * Retries a task based on a custom predicate. This method has a return value.
     *
     * @param supplier    The task to execute, which returns a value.
     * @param maxAttempts The maximum number of retry attempts. Must be greater than 0.
     * @param delay       The delay between retries.
     * @param recovery    The recovery task to run if retries fail, which returns a value.
     * @param predicate   A custom predicate to determine if a retry is needed. Returns `true` to retry.
     * @param <T>         The type of the result.
     * @return The result of the execution.
     */
    public static <T> T ofPredicate(
            final SupplierX<T> supplier,
            final long maxAttempts,
            final Duration delay,
            final SupplierX<T> recovery,
            final BiPredicateX<T, Throwable> predicate) {
        final RetryableTask<T> task = RetryableTask.retryForPredicate(supplier, predicate).delay(delay)
                .maxAttempts(maxAttempts).execute();
        return task.success() ? task.get().orElse(null) : recovery.get();
    }

}
