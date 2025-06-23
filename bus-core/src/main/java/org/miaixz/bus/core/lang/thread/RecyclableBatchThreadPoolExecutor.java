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
 * 可召回批处理线程池执行器。
 * <p>
 * 功能：
 * <ul>
 * <li>支持数据分批并行处理。</li>
 * <li>主线程空闲时召回线程池队列中的任务执行。</li>
 * <li>线程安全，多任务并发执行，线程池满载时效率等同单线程，无阻塞风险。</li>
 * </ul>
 * 适用场景：
 * <ul>
 * <li>同步批量处理数据，提升吞吐量，防止任务堆积 ({@link #process(List, int, Function)})。</li>
 * <li>加速普通查询接口 ({@link #processByWarp(Warp[])})。</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RecyclableBatchThreadPoolExecutor {

    private final ExecutorService executor;

    /**
     * 构造线程池执行器。
     *
     * @param poolSize 线程池大小
     */
    public RecyclableBatchThreadPoolExecutor(final int poolSize) {
        this(poolSize, "recyclable-batch-pool-");
    }

    /**
     * 构造线程池执行器，推荐使用。
     * <p>
     * 特性：
     * <ul>
     * <li>使用无界队列，主线程召回任务执行，避免任务堆积，无需拒绝策略。</li>
     * <li>高并发场景（如 Web 应用）可能导致内存溢出，建议限制请求或优化资源管理。</li>
     * </ul>
     *
     * @param poolSize         线程池大小
     * @param threadPoolPrefix 线程名前缀
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
     * 使用自定义线程池构造。
     * <p>
     * 通常无需使用，推荐默认构造方法。
     *
     * @param executor 自定义线程池
     */
    public RecyclableBatchThreadPoolExecutor(final ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * 关闭线程池，拒绝接受新任务。
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * 获取底层线程池。
     *
     * @return 线程池执行器
     */
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * 分批处理数据并返回合并结果。
     * <p>
     * 特性：
     * <ul>
     * <li>所有批次完成后过滤空值，保持输入数据顺序，处理器返回 null 可忽略结果。</li>
     * <li>{@link Function} 需自行处理异常并保证线程安全。</li>
     * <li>数据分片后可能被外部修改，必要时需提前复制数据。</li>
     * <li>主线程参与批处理，异步任务建议使用普通线程池。</li>
     * </ul>
     *
     * @param <T>       输入数据类型
     * @param <R>       输出数据类型
     * @param data      待处理数据集合
     * @param batchSize 每批次数据量
     * @param processor 单条数据处理函数
     * @return 处理结果集合
     * @throws IllegalArgumentException 如果 batchSize 小于 1
     */
    public <T, R> List<R> process(final List<T> data, final int batchSize, final Function<T, R> processor) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize 必须大于等于 1");
        }
        final List<List<T>> batches = splitData(data, batchSize);
        final int batchCount = batches.size();
        final int minusOne = batchCount - 1;
        final ArrayDeque<IdempotentTask<R>> taskQueue = new ArrayDeque<>(minusOne);
        final Map<Integer, Future<TaskResult<R>>> futuresMap = new HashMap<>();
        // 提交前 batchCount-1 批任务
        for (int i = 0; i < minusOne; i++) {
            final int index = i;
            final IdempotentTask<R> task = new IdempotentTask<>(i, () -> processBatch(batches.get(index), processor));
            taskQueue.add(task);
            futuresMap.put(i, executor.submit(task));
        }
        final List<R>[] resultArr = new ArrayList[batchCount];
        // 处理最后一批
        resultArr[minusOne] = processBatch(batches.get(minusOne), processor);
        // 处理剩余任务
        processRemainingTasks(taskQueue, futuresMap, resultArr);
        // 排序、过滤空值
        return Stream.of(resultArr).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * 处理剩余任务并收集结果。
     *
     * @param <R>        输出数据类型
     * @param taskQueue  任务队列
     * @param futuresMap 异步任务映射
     * @param resultArr  结果存储数组
     * @throws RuntimeException 如果任务执行过程中发生异常
     */
    private <R> void processRemainingTasks(final Queue<IdempotentTask<R>> taskQueue,
            final Map<Integer, Future<TaskResult<R>>> futuresMap, final List<R>[] resultArr) {
        // 主线程消费未执行任务
        IdempotentTask<R> task;
        while ((task = taskQueue.poll()) != null) {
            try {
                final TaskResult<R> call = task.call();
                if (call.effective) {
                    // 取消被主线程执行的任务
                    final Future<TaskResult<R>> future = futuresMap.remove(task.index);
                    future.cancel(false);
                    // 加入结果集
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
     * 幂等任务包装类，确保任务仅执行一次。
     *
     * @param <R> 结果类型
     */
    private static class IdempotentTask<R> implements Callable<TaskResult<R>> {

        private final int index;
        private final Callable<List<R>> delegate;
        private final AtomicBoolean executed = new AtomicBoolean(false);

        /**
         * 构造幂等任务。
         *
         * @param index    任务索引
         * @param delegate 任务执行逻辑
         */
        IdempotentTask(final int index, final Callable<List<R>> delegate) {
            this.index = index;
            this.delegate = delegate;
        }

        /**
         * 执行任务并返回结果。
         *
         * @return 任务结果
         * @throws Exception 如果任务执行失败
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
     * 任务结果包装类，标记结果有效性。
     *
     * @param <R> 结果类型
     */
    private static class TaskResult<R> {

        private final List<R> result;
        private final boolean effective;

        /**
         * 构造任务结果。
         *
         * @param result    处理结果
         * @param effective 结果是否有效
         */
        TaskResult(final List<R> result, final boolean effective) {
            this.result = result;
            this.effective = effective;
        }
    }

    /**
     * 将数据分片为批次。
     *
     * @param <T>       数据类型
     * @param data      原始数据
     * @param batchSize 每批次数据量
     * @return 分片后的二维集合
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
     * 处理单批次数据。
     *
     * @param <T>       输入数据类型
     * @param <R>       输出数据类型
     * @param batch     单批次数据
     * @param processor 处理函数
     * @return 处理结果集合
     */
    private static <T, R> List<R> processBatch(final List<T> batch, final Function<T, R> processor) {
        return batch.stream().map(processor).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 处理 Warp 数组。
     * <p>
     * 示例：
     * 
     * <pre>{@code
     * Warp<String> warp1 = Warp.of(this::select1);
     * Warp<List<String>> warp2 = Warp.of(this::select2);
     * executor.processByWarp(warp1, warp2);
     * String r1 = warp1.get();
     * List<String> r2 = warp2.get();
     * }</pre>
     *
     * @param warps Warp 数组
     * @return Warp 集合，结果中空值不会被过滤
     */
    public List<Warp<?>> processByWarp(final Warp<?>... warps) {
        return processByWarp(Arrays.asList(warps));
    }

    /**
     * 处理 Warp 集合。
     *
     * @param warps Warp 集合
     * @return Warp 集合，结果中空值不会被过滤
     */
    public List<Warp<?>> processByWarp(final List<Warp<?>> warps) {
        return process(warps, 1, Warp::execute);
    }

    /**
     * 处理逻辑包装类。
     *
     * @param <R> 结果类型
     */
    public static class Warp<R> {

        private final Supplier<R> supplier;
        private R result;

        /**
         * 构造 Warp。
         *
         * @param supplier 执行逻辑
         * @throws NullPointerException 如果 supplier 为 null
         */
        private Warp(final Supplier<R> supplier) {
            Objects.requireNonNull(supplier);
            this.supplier = supplier;
        }

        /**
         * 创建 Warp 实例。
         *
         * @param <R>      结果类型
         * @param supplier 执行逻辑
         * @return Warp 实例
         */
        public static <R> Warp<R> of(final Supplier<R> supplier) {
            return new Warp<>(supplier);
        }

        /**
         * 获取处理结果。
         *
         * @return 处理结果
         */
        public R get() {
            return result;
        }

        /**
         * 执行处理逻辑。
         *
         * @return 当前 Warp 实例
         */
        public Warp<R> execute() {
            result = supplier.get();
            return this;
        }
    }

}