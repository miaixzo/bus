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
package org.miaixz.bus.core.lang.thread;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.miaixz.bus.core.center.function.BiPredicateX;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * A utility class for executing tasks with retry logic. It allows specifying retry conditions based on exceptions or
 * custom predicates, along with maximum attempts and backoff between retries.
 *
 * @param <T> The type of the task result.
 * @author Kimi Liu
 * @since Java 21+
 */
public class RetryableTask<T> {

    /**
     * The supplier representing the task to be executed.
     */
    private final SupplierX<T> supplier;

    /**
     * The retry strategy, a {@link BiPredicateX} that returns {@code true} if a retry should occur. The predicate
     * receives the task result and any thrown {@link Throwable}.
     */
    private final BiPredicateX<T, Throwable> predicate;

    /**
     * The result of the task execution.
     */
    private T result;

    /**
     * The maximum number of retry attempts. Default is 3.
     */
    private long maxAttempts = 3;

    /**
     * The last {@link Throwable} encountered during task execution, if any.
     */
    private Throwable throwable;

    /**
     * Whether the task finished without an exception and no more retries were requested.
     */
    private boolean success;

    /**
     * The retry backoff strategy. Default is a fixed 1 second delay.
     */
    private Backoff backoff = new FixedBackoff(Duration.ofSeconds(1));

    /**
     * Private constructor for {@code RetryableTask}. Use static factory methods like
     * {@link #retryForExceptions(Runnable, Class[])} or {@link #retryForPredicate(SupplierX, BiPredicateX)} to create
     * instances.
     *
     * @param supplier  The {@link SupplierX} representing the task to be executed.
     * @param predicate The {@link BiPredicateX} defining the retry strategy. Returns {@code true} to retry.
     * @throws IllegalArgumentException if {@code supplier} or {@code predicate} is {@code null}.
     */
    private RetryableTask(final SupplierX<T> supplier, final BiPredicateX<T, Throwable> predicate) {
        Assert.notNull(supplier, "task parameter cannot be null");
        Assert.notNull(predicate, "predicate parameter cannot be null");

        this.predicate = predicate;
        this.supplier = supplier;
    }

    /**
     * Creates a {@code RetryableTask} that retries execution if a specified exception type is thrown. This method is
     * for tasks that do not return a value.
     *
     * @param <T>            The type of the task result (will be {@code Void} for {@link Runnable} tasks).
     * @param runnable       The {@link Runnable} task to execute.
     * @param retryableTypes The {@link Throwable} types that trigger a retry.
     * @return A new {@code RetryableTask} instance configured for exception-based retries.
     * @throws IllegalArgumentException if {@code retryableTypes} is empty.
     */
    @SafeVarargs
    public static <T> RetryableTask<T> retryForExceptions(
            final Runnable runnable,
            final Class<? extends Throwable>... retryableTypes) {
        return retryForExceptions(() -> {
            runnable.run();
            return null;
        }, retryableTypes);
    }

    /**
     * Creates a {@code RetryableTask} that retries execution if a specified exception type is thrown. This method is
     * for tasks that return a value.
     *
     * @param <T>            The type of the task result.
     * @param supplier       The {@link SupplierX} representing the task to execute.
     * @param retryableTypes The {@link Throwable} types that trigger a retry.
     * @return A new {@code RetryableTask} instance configured for exception-based retries.
     * @throws IllegalArgumentException if {@code retryableTypes} is empty.
     */
    @SafeVarargs
    public static <T> RetryableTask<T> retryForExceptions(
            final SupplierX<T> supplier,
            final Class<? extends Throwable>... retryableTypes) {
        Assert.isTrue(retryableTypes.length != 0, "retryableTypes cannot be empty");

        final BiPredicateX<T, Throwable> retryPredicate = (ignoredResult, failure) -> {
            if (ObjectKit.isNotNull(failure)) {
                return Arrays.stream(retryableTypes).anyMatch(type -> type.isAssignableFrom(failure.getClass()));
            }
            return false;
        };

        return new RetryableTask<>(supplier, retryPredicate);
    }

    /**
     * Creates a {@code RetryableTask} that retries execution based on a custom {@link BiPredicateX}. This method is for
     * tasks that do not return a value.
     *
     * @param <T>       The type of the task result (will be {@code Void} for {@link Runnable} tasks).
     * @param runnable  The {@link Runnable} task to execute.
     * @param predicate The {@link BiPredicateX} defining the retry strategy. Returns {@code true} to retry.
     * @return A new {@code RetryableTask} instance configured for predicate-based retries.
     */
    public static <T> RetryableTask<T> retryForPredicate(
            final Runnable runnable,
            final BiPredicateX<T, Throwable> predicate) {
        return retryForPredicate(() -> {
            runnable.run();
            return null;
        }, predicate);
    }

    /**
     * Creates a {@code RetryableTask} that retries execution based on a custom {@link BiPredicateX}. This method is for
     * tasks that return a value.
     *
     * @param <T>       The type of the task result.
     * @param supplier  The {@link SupplierX} representing the task to execute.
     * @param predicate The {@link BiPredicateX} defining the retry strategy. Returns {@code true} to retry.
     * @return A new {@code RetryableTask} instance configured for predicate-based retries.
     */
    public static <T> RetryableTask<T> retryForPredicate(
            final SupplierX<T> supplier,
            final BiPredicateX<T, Throwable> predicate) {
        return new RetryableTask<>(supplier, predicate);
    }

    /**
     * Sets the maximum number of attempts for this retryable task.
     *
     * @param maxAttempts The maximum number of times the task should be attempted, including the initial execution.
     * @return This {@code RetryableTask} instance for method chaining.
     * @throws IllegalArgumentException if {@code maxAttempts} is not greater than 0.
     */
    public RetryableTask<T> maxAttempts(final long maxAttempts) {
        Assert.isTrue(maxAttempts > 0, "maxAttempts must be greater than 0");

        this.maxAttempts = maxAttempts;
        return this;
    }

    /**
     * Sets the delay duration between retry attempts.
     *
     * @param delay The {@link Duration} to wait before retrying the task.
     * @return This {@code RetryableTask} instance for method chaining.
     * @throws IllegalArgumentException if {@code delay} is {@code null}.
     */
    public RetryableTask<T> delay(final Duration delay) {
        Assert.notNull(delay, "delay parameter cannot be null");
        return this.backoff(new FixedBackoff(delay));
    }

    /**
     * Sets the retry backoff strategy.
     *
     * @param backoff The backoff strategy.
     * @return This {@code RetryableTask} instance for method chaining.
     * @throws IllegalArgumentException if {@code backoff} is {@code null}.
     */
    public RetryableTask<T> backoff(final Backoff backoff) {
        Assert.notNull(backoff, "backoff parameter cannot be null");
        this.backoff = backoff;
        return this;
    }

    /**
     * Retrieves the result of the task execution, or throws the last encountered exception if the task failed.
     *
     * @return The result of the task.
     * @throws Throwable The last {@link Throwable} thrown during task execution if no result was obtained.
     */
    public T orElseThrow() throws Throwable {
        return Optional.ofNullable(this.result).orElseThrow(() -> this.throwable().orElse(new RuntimeException()));
    }

    /**
     * Returns an {@link Optional} containing the last {@link Throwable} encountered during task execution, if any.
     *
     * @return An {@link Optional} describing the {@link Throwable}, or an empty {@link Optional} if no exception
     *         occurred.
     */
    public Optional<Throwable> throwable() {
        return Optional.ofNullable(this.throwable);
    }

    /**
     * Returns an {@link Optional} containing the result of the task execution, if successful.
     *
     * @return An {@link Optional} describing the result, or an empty {@link Optional} if the task failed or returned
     *         {@code null}.
     */
    public Optional<T> get() {
        return Optional.ofNullable(this.result);
    }

    /**
     * Returns whether the task finished without an exception and no more retries were requested.
     *
     * @return {@code true} if the task completed successfully.
     */
    public boolean success() {
        return this.success;
    }

    /**
     * Executes the retryable task asynchronously.
     *
     * @return A {@link CompletableFuture} that will complete with this {@code RetryableTask} instance after execution.
     */
    public CompletableFuture<RetryableTask<T>> asyncExecute() {
        return CompletableFuture.supplyAsync(this::doExecute, GlobalThreadPool.getExecutor());
    }

    /**
     * Executes the retryable task synchronously.
     *
     * @return This {@code RetryableTask} instance after execution.
     */
    public RetryableTask<T> execute() {
        return doExecute();
    }

    /**
     * Performs the actual retry logic. The task is executed at least once, and then retried based on the configured
     * {@code predicate}, {@code maxAttempts}, and {@code backoff}.
     *
     * @return This {@code RetryableTask} instance after all attempts are made or the task succeeds.
     */
    private RetryableTask<T> doExecute() {
        Throwable failure;
        int retryAttempt = 0;
        this.success = false;

        // The task is executed at least once
        do {
            try {
                this.result = this.supplier.get();
                failure = null;
            } catch (final Throwable t) {
                failure = t;
                this.result = null;
            }

            // Determine if a retry is needed based on the predicate
            if (!this.predicate.test(this.result, failure)) {
                // If conditions for retry are not met, break the loop
                this.success = null == failure;
                break;
            }

            // Avoid sleeping after the last attempt if no more retries are left
            if (this.maxAttempts > 0) {
                retryAttempt++;
                ThreadKit.sleep(this.backoff.next(retryAttempt).toMillis());
            }
        } while (--this.maxAttempts >= 0);

        this.throwable = failure;
        return this;
    }

}
