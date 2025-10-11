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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A recyclable batch thread pool executor designed for efficient parallel processing of tasks.
 * <p>
 * Key Features:
 * <ul>
 * <li>Supports parallel processing of data in batches.</li>
 * <li>Allows the main thread to reclaim and execute tasks from the thread pool queue when idle.</li>
 * <li>Thread-safe, enabling concurrent execution of multiple tasks. When the thread pool is fully loaded, its
 * efficiency is comparable to a single-threaded execution, without blocking risks.</li>
 * </ul>
 * Applicable Scenarios:
 * <ul>
 * <li>Synchronous batch processing of data to improve throughput and prevent task accumulation (see
 * {@link #process(List, int, Function)}).</li>
 * <li>Accelerating general query interface calls (see {@link #processByWarp(Warp[])}).</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RecyclableBatchThreadPoolExecutor {

    /**
     * The underlying {@link ExecutorService} used for executing tasks.
     */
    private final ExecutorService executor;

    /**
     * Constructs a {@code RecyclableBatchThreadPoolExecutor} with a specified pool size.
     *
     * @param poolSize The fixed size of the thread pool.
     */
    public RecyclableBatchThreadPoolExecutor(final int poolSize) {
        this(poolSize, "recyclable-batch-pool-");
    }

    /**
     * Constructs a {@code RecyclableBatchThreadPoolExecutor} with a specified pool size and thread name prefix. This
     * constructor is recommended for most use cases.
     * <p>
     * Characteristics:
     * <ul>
     * <li>Uses an unbounded queue, allowing the main thread to reclaim and execute tasks, thus avoiding task
     * accumulation and eliminating the need for rejection policies.</li>
     * <li>In high-concurrency scenarios (e.g., web applications), this might lead to out-of-memory errors. It is
     * advisable to limit requests or optimize resource management.</li>
     * </ul>
     *
     * @param poolSize         The fixed size of the thread pool.
     * @param threadPoolPrefix The prefix for naming threads within this pool.
     */
    public RecyclableBatchThreadPoolExecutor(final int poolSize, final String threadPoolPrefix) {
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final ThreadFactory threadFactory = r -> {
            final Thread t = new Thread(r, threadPoolPrefix + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        };
        this.executor = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), threadFactory);
    }

    /**
     * Constructs a {@code RecyclableBatchThreadPoolExecutor} using a custom {@link ExecutorService}.
     * <p>
     * Typically, this constructor is not needed; the default constructors are usually sufficient.
     *
     * @param executor The custom {@link ExecutorService} to be used.
     */
    public RecyclableBatchThreadPoolExecutor(final ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Splits a list of data into smaller batches.
     *
     * @param <T>       The type of data elements.
     * @param data      The original list of data.
     * @param batchSize The maximum number of elements in each batch.
     * @return A list of lists, where each inner list represents a batch of data.
     */
    private static <T> List<List<T>> splitData(final List<T> data, final int batchSize) {
        final int batchCount = (data.size() + batchSize - 1) / batchSize;
        return new AbstractList<>() {

            @Override
            public List<T> get(final int index) {
                final int from = index * batchSize;
                final int to = Math.min((index + 1) * batchSize, data.size());
                return data.subList(from, to);
            }

            @Override
            public int size() {
                return batchCount;
            }
        };
    }

    /**
     * Processes a single batch of data using the provided processor function.
     *
     * @param <T>       The type of input data elements in the batch.
     * @param <R>       The type of output data elements.
     * @param batch     The list representing a single batch of data.
     * @param processor The function to apply to each element in the batch.
     * @return A list of processed results for the batch, with nulls filtered out.
     */
    private static <T, R> List<R> processBatch(final List<T> batch, final Function<T, R> processor) {
        return batch.stream().map(processor).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Shuts down the thread pool, rejecting any new tasks. Previously submitted tasks will be executed.
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * Retrieves the underlying {@link ExecutorService} used by this batch executor.
     *
     * @return The {@link ExecutorService} instance.
     */
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Processes a list of data in batches, returning a merged list of results.
     * <p>
     * Characteristics:
     * <ul>
     * <li>After all batches are completed, null values are filtered out. The order of input data is maintained. A
     * processor returning {@code null} for an item will cause that item's result to be ignored.</li>
     * <li>The provided {@link Function} must handle its own exceptions and ensure thread safety.</li>
     * <li>Data may be modified externally after being split into batches; copy data beforehand if necessary.</li>
     * <li>The main thread participates in batch processing. For asynchronous tasks, a regular thread pool is
     * recommended.</li>
     * </ul>
     *
     * @param <T>       The type of the input data elements.
     * @param <R>       The type of the output data elements.
     * @param data      The collection of data to be processed.
     * @param batchSize The number of data elements in each batch.
     * @param processor The function to apply to each individual data element.
     * @return A list of processed results, with nulls filtered out, maintaining the original order.
     * @throws IllegalArgumentException If {@code batchSize} is less than 1.
     * @throws RuntimeException         If any task execution encounters an exception.
     */
    public <T, R> List<R> process(final List<T> data, final int batchSize, final Function<T, R> processor) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be greater than or equal to 1");
        }
        final List<List<T>> batches = splitData(data, batchSize);
        final int batchCount = batches.size();
        final int minusOne = batchCount - 1;
        final ArrayDeque<IdempotentTask<R>> taskQueue = new ArrayDeque<>(minusOne);
        final Map<Integer, Future<TaskResult<R>>> futuresMap = new HashMap<>();
        // Submit the first batchCount-1 tasks
        for (int i = 0; i < minusOne; i++) {
            final int index = i;
            final IdempotentTask<R> task = new IdempotentTask<>(i, () -> processBatch(batches.get(index), processor));
            taskQueue.add(task);
            futuresMap.put(i, executor.submit(task));
        }
        final List<R>[] resultArr = new ArrayList[batchCount];
        // Process the last batch on the current thread
        resultArr[minusOne] = processBatch(batches.get(minusOne), processor);
        // Process remaining tasks, potentially by the main thread
        processRemainingTasks(taskQueue, futuresMap, resultArr);
        // Sort and filter null values
        return Stream.of(resultArr).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * Processes any remaining tasks in the queue and collects their results. The main thread attempts to execute tasks
     * from the queue. Any tasks already submitted to the executor will have their results retrieved from their
     * respective {@link Future}s.
     *
     * @param <R>        The type of the output data elements.
     * @param taskQueue  The queue of {@link IdempotentTask}s that are yet to be processed or whose results are pending.
     * @param futuresMap A map of task indices to their corresponding {@link Future}s, for tasks submitted to the
     *                   executor.
     * @param resultArr  The array to store the results of each batch.
     * @throws RuntimeException If a task execution fails or is interrupted.
     */
    private <R> void processRemainingTasks(
            final Queue<IdempotentTask<R>> taskQueue,
            final Map<Integer, Future<TaskResult<R>>> futuresMap,
            final List<R>[] resultArr) {
        // Main thread consumes unexecuted tasks
        IdempotentTask<R> task;
        while ((task = taskQueue.poll()) != null) {
            try {
                final TaskResult<R> call = task.call();
                if (call.effective) {
                    // Cancel tasks that were executed by the main thread
                    final Future<TaskResult<R>> future = futuresMap.remove(task.index);
                    if (future != null) {
                        future.cancel(false);
                    }
                    // Add result to the result set
                    resultArr[task.index] = call.result;
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        futuresMap.forEach((index, future) -> {
            try {
                final TaskResult<R> taskResult = future.get();
                if (taskResult.effective) {
                    resultArr[index] = taskResult.result;
                }
            } catch (final InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Processes an array of {@link Warp} objects concurrently.
     * <p>
     * Example:
     *
     * <pre>{@code
     * Warp<String> warp1 = Warp.of(this::select1);
     * Warp<List<String>> warp2 = Warp.of(this::select2);
     * executor.processByWarp(warp1, warp2);
     * String r1 = warp1.get();
     * List<String> r2 = warp2.get();
     * }</pre>
     *
     * @param warps An array of {@link Warp} objects to be processed.
     * @return A list of {@link Warp} objects, where null results are not filtered out.
     */
    public List<Warp<?>> processByWarp(final Warp<?>... warps) {
        return processByWarp(Arrays.asList(warps));
    }

    /**
     * Processes a collection of {@link Warp} objects concurrently.
     *
     * @param warps A list of {@link Warp} objects to be processed.
     * @return A list of {@link Warp} objects, where null results are not filtered out.
     */
    public List<Warp<?>> processByWarp(final List<Warp<?>> warps) {
        return process(warps, 1, Warp::execute);
    }

    /**
     * An idempotent task wrapper that ensures a task is executed only once. This is useful when a task might be
     * submitted to a thread pool but also potentially executed by the main thread if it becomes idle.
     *
     * @param <R> The type of the result returned by the task.
     */
    private static class IdempotentTask<R> implements Callable<TaskResult<R>> {

        /**
         * The index of this task within the batch processing sequence.
         */
        private final int index;
        /**
         * The delegate {@link Callable} representing the actual task logic.
         */
        private final Callable<List<R>> delegate;
        /**
         * An atomic boolean to ensure the task's delegate is executed only once.
         */
        private final AtomicBoolean executed = new AtomicBoolean(false);

        /**
         * Constructs an {@code IdempotentTask}.
         *
         * @param index    The index of the task.
         * @param delegate The actual task logic to be executed.
         */
        IdempotentTask(final int index, final Callable<List<R>> delegate) {
            this.index = index;
            this.delegate = delegate;
        }

        /**
         * Executes the task's delegate if it has not been executed before, ensuring idempotency.
         *
         * @return A {@link TaskResult} containing the result of the delegate execution and a flag indicating if it was
         *         effective.
         * @throws Exception If the delegate task execution fails.
         */
        @Override
        public TaskResult<R> call() throws Exception {
            if (executed.compareAndSet(false, true)) {
                return new TaskResult<>(delegate.call(), true);
            }
            return new TaskResult<>(null, false);
        }
    }

    /**
     * A wrapper class for task results, indicating whether the result is effective (i.e., the task was actually
     * executed).
     *
     * @param <R> The type of the result.
     */
    private static class TaskResult<R> {

        /**
         * The list of results produced by the task.
         */
        private final List<R> result;
        /**
         * A boolean flag indicating whether this result is effective (i.e., the task was executed).
         */
        private final boolean effective;

        /**
         * Constructs a {@code TaskResult}.
         *
         * @param result    The list of results from the task execution.
         * @param effective {@code true} if the task was effectively executed and produced this result; {@code false}
         *                  otherwise.
         */
        TaskResult(final List<R> result, final boolean effective) {
            this.result = result;
            this.effective = effective;
        }
    }

    /**
     * A wrapper class for encapsulating a processing logic ({@link Supplier}) and its result. This allows for deferred
     * execution and retrieval of results.
     *
     * @param <R> The type of the result produced by the encapsulated logic.
     */
    public static class Warp<R> {

        /**
         * The {@link Supplier} that provides the processing logic.
         */
        private final Supplier<R> supplier;
        /**
         * The result of the execution of the supplier.
         */
        private R result;

        /**
         * Constructs a {@code Warp} with the given {@link Supplier}.
         *
         * @param supplier The execution logic to be wrapped. Must not be {@code null}.
         * @throws NullPointerException If {@code supplier} is {@code null}.
         */
        private Warp(final Supplier<R> supplier) {
            Objects.requireNonNull(supplier);
            this.supplier = supplier;
        }

        /**
         * Creates a new {@code Warp} instance with the specified {@link Supplier}.
         *
         * @param <R>      The type of the result.
         * @param supplier The execution logic.
         * @return A new {@code Warp} instance.
         */
        public static <R> Warp<R> of(final Supplier<R> supplier) {
            return new Warp<>(supplier);
        }

        /**
         * Retrieves the result of the encapsulated processing logic. The result is available after {@link #execute()}
         * has been called.
         *
         * @return The processed result.
         */
        public R get() {
            return result;
        }

        /**
         * Executes the encapsulated processing logic (the {@link Supplier}) and stores its result.
         *
         * @return This {@code Warp} instance for method chaining.
         */
        public Warp<R> execute() {
            result = supplier.get();
            return this;
        }
    }

}
