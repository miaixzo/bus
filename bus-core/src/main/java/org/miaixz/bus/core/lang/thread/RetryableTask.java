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
package org.miaixz.bus.core.lang.thread;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * A utility class for executing tasks with retry logic. It allows specifying retry conditions based on exceptions or
 * custom predicates, along with maximum attempts and delay between retries.
 *
 * @param <T> The type of the task result.
 * @author Kimi Liu
 * @since Java 17+
 */
public class RetryableTask<T> {

    /**
     * The supplier representing the task to be executed.
     */
    private final Supplier<T> sup;
    /**
     * The retry strategy, a {@link BiPredicate} that returns {@code true} if a retry should occur. The predicate
     * receives the task result and any thrown {@link Throwable}.
     */
    private final BiPredicate<T, Throwable> predicate;
    /**
     * The result of the task execution.
     */
    private T result;
    /**
     * The maximum number of retry attempts. Default is 3.
     */
    private long maxAttempts = 3;
    /**
     * The delay duration between retry attempts. Default is 1 second.
     */
    private Duration delay = Duration.ofSeconds(1);
    /**
     * The last {@link Throwable} encountered during task execution, if any.
     */
    private Throwable throwable;

    /**
     * Private constructor for {@code RetryableTask}. Use static factory methods like
     * {@link #retryForExceptions(Runnable, Class[])} or {@link #retryForPredicate(Supplier, BiPredicate)} to create
     * instances.
     *
     * @param sup       The {@link Supplier} representing the task to be executed.
     * @param predicate The {@link BiPredicate} defining the retry strategy. Returns {@code true} to retry.
     * @throws IllegalArgumentException if {@code sup} or {@code predicate} is {@code null}.
     */
    private RetryableTask(final Supplier<T> sup, final BiPredicate<T, Throwable> predicate) {
        Assert.notNull(sup, "task parameter cannot be null");
        Assert.notNull(predicate, "predicate parameter cannot be null");

        this.predicate = predicate;
        this.sup = sup;
    }

    /**
     * Creates a {@code RetryableTask} that retries execution if a specified exception type is thrown. This method is
     * for tasks that do not return a value.
     *
     * @param <T> The type of the task result (will be {@code Void} for {@link Runnable} tasks).
     * @param run The {@link Runnable} task to execute.
     * @param ths An array of {@link Throwable} classes. The task will retry if any of these exceptions are caught.
     * @return A new {@code RetryableTask} instance configured for exception-based retries.
     * @throws IllegalArgumentException if {@code ths} is empty.
     */
    @SafeVarargs
    public static <T> RetryableTask<T> retryForExceptions(final Runnable run, final Class<? extends Throwable>... ths) {
        return retryForExceptions(() -> {
            run.run();
            return null;
        }, ths);
    }

    /**
     * Creates a {@code RetryableTask} that retries execution if a specified exception type is thrown. This method is
     * for tasks that return a value.
     *
     * @param <T> The type of the task result.
     * @param sup The {@link Supplier} representing the task to execute.
     * @param ths An array of {@link Throwable} classes. The task will retry if any of these exceptions are caught.
     * @return A new {@code RetryableTask} instance configured for exception-based retries.
     * @throws IllegalArgumentException if {@code ths} is empty.
     */
    @SafeVarargs
    public static <T> RetryableTask<T> retryForExceptions(
            final Supplier<T> sup,
            final Class<? extends Throwable>... ths) {
        Assert.isTrue(ths.length != 0, "exs cannot be empty");

        final BiPredicate<T, Throwable> strategy = (t, e) -> {
            if (ObjectKit.isNotNull(e)) {
                return Arrays.stream(ths).anyMatch(ex -> ex.isAssignableFrom(e.getClass()));
            }
            return false;
        };

        return new RetryableTask<>(sup, strategy);
    }

    /**
     * Creates a {@code RetryableTask} that retries execution based on a custom {@link BiPredicate}. This method is for
     * tasks that do not return a value.
     *
     * @param <T>       The type of the task result (will be {@code Void} for {@link Runnable} tasks).
     * @param run       The {@link Runnable} task to execute.
     * @param predicate The {@link BiPredicate} defining the retry strategy. Returns {@code true} to retry.
     * @return A new {@code RetryableTask} instance configured for predicate-based retries.
     */
    public static <T> RetryableTask<T> retryForPredicate(
            final Runnable run,
            final BiPredicate<T, Throwable> predicate) {
        return retryForPredicate(() -> {
            run.run();
            return null;
        }, predicate);
    }

    /**
     * Creates a {@code RetryableTask} that retries execution based on a custom {@link BiPredicate}. This method is for
     * tasks that return a value.
     *
     * @param <T>       The type of the task result.
     * @param sup       The {@link Supplier} representing the task to execute.
     * @param predicate The {@link BiPredicate} defining the retry strategy. Returns {@code true} to retry.
     * @return A new {@code RetryableTask} instance configured for predicate-based retries.
     */
    public static <T> RetryableTask<T> retryForPredicate(
            final Supplier<T> sup,
            final BiPredicate<T, Throwable> predicate) {
        return new RetryableTask<>(sup, predicate);
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

        this.delay = delay;
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
     * {@code predicate}, {@code maxAttempts}, and {@code delay}.
     *
     * @return This {@code RetryableTask} instance after all attempts are made or the task succeeds.
     */
    private RetryableTask<T> doExecute() {
        Throwable th = null;

        // The task is executed at least once
        do {
            try {
                this.result = this.sup.get();
            } catch (final Throwable t) {
                th = t;
            }

            // Determine if a retry is needed based on the predicate
            if (!this.predicate.test(this.result, th)) {
                // If conditions for retry are not met, break the loop
                break;
            }

            // Avoid sleeping after the last attempt if no more retries are left
            if (this.maxAttempts > 0) {
                ThreadKit.sleep(delay.toMillis());
            }
        } while (--this.maxAttempts >= 0);

        this.throwable = th;
        return this;
    }

}
