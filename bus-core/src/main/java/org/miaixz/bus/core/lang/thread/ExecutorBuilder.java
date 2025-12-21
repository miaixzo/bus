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

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.*;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A builder class for creating and configuring {@link ThreadPoolExecutor} instances. This builder simplifies the
 * process of setting up a thread pool with various parameters such as core pool size, maximum pool size, keep-alive
 * time, work queue, thread factory, and rejected execution handler.
 *
 * <p>
 * The execution flow of tasks in a {@link ThreadPoolExecutor} is generally as follows:
 * <ol>
 * <li>If the number of tasks currently in the pool is less than {@code corePoolSize}, a new thread is created to
 * execute the task immediately.</li>
 * <li>If the number of tasks in the pool is equal to or greater than {@code corePoolSize}, the task is added to the
 * work queue.</li>
 * <li>If the work queue is full, a new thread is created to execute the task immediately, provided the number of active
 * threads is less than {@code maxPoolSize}.</li>
 * <li>If the number of active threads is equal to {@code maxPoolSize} and the work queue is also full, the
 * {@link RejectedExecutionHandler} is invoked.</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExecutorBuilder implements Builder<ThreadPoolExecutor> {

    /**
     * Constructs a new ExecutorBuilder. Utility class constructor for static access.
     */
    private ExecutorBuilder() {
    }

    @Serial
    private static final long serialVersionUID = 2852279106156L;

    /**
     * The default capacity for the waiting queue, used if no specific queue is provided.
     */
    public static final int DEFAULT_QUEUE_CAPACITY = Integer.MAX_VALUE;
    /**
     * The number of core threads in the pool. These threads are kept alive even if they are idle.
     */
    private int corePoolSize;
    /**
     * The maximum number of threads allowed in the pool. This includes both core and non-core threads.
     */
    private int maxPoolSize = Integer.MAX_VALUE;
    /**
     * The maximum time that excess idle threads will wait for new tasks before terminating. Default is 60 seconds,
     * converted to nanoseconds.
     */
    private long keepAliveTime = TimeUnit.SECONDS.toNanos(60);
    /**
     * The queue used to hold tasks before they are executed. This queue will only hold {@link Runnable} tasks.
     */
    private BlockingQueue<Runnable> workQueue;
    /**
     * The factory used to create new threads. Allows for custom thread naming and configuration.
     */
    private ThreadFactory threadFactory;
    /**
     * The handler for tasks that cannot be executed by the thread pool, either because the pool is saturated or shut
     * down. This is invoked when the thread pool and its waiting queue are both full.
     */
    private RejectedExecutionHandler handler;
    /**
     * A boolean flag indicating whether core threads are allowed to time out and terminate if they remain idle for the
     * {@code keepAliveTime}.
     */
    private Boolean allowCoreThreadTimeOut;

    /**
     * Creates a new {@code ExecutorBuilder} instance, initiating the build process for a {@link ThreadPoolExecutor}.
     *
     * @return A new {@code ExecutorBuilder} instance.
     */
    public static ExecutorBuilder of() {
        return new ExecutorBuilder();
    }

    /**
     * Builds a {@link ThreadPoolExecutor} based on the configurations provided by the given {@code ExecutorBuilder}.
     *
     * @param builder The {@code ExecutorBuilder} containing the desired thread pool configurations.
     * @return A newly constructed {@link ThreadPoolExecutor}.
     */
    private static ThreadPoolExecutor build(final ExecutorBuilder builder) {
        final int corePoolSize = builder.corePoolSize;
        final int maxPoolSize = builder.maxPoolSize;
        final long keepAliveTime = builder.keepAliveTime;
        final BlockingQueue<Runnable> workQueue;
        // If corePoolSize is 0, use SynchronousQueue to avoid infinite blocking.
        workQueue = Objects.requireNonNullElseGet(
                builder.workQueue,
                () -> (corePoolSize <= 0) ? new SynchronousQueue<>()
                        : new LinkedBlockingQueue<>(DEFAULT_QUEUE_CAPACITY));
        final ThreadFactory threadFactory = (null != builder.threadFactory) ? builder.threadFactory
                : Executors.defaultThreadFactory();
        final RejectedExecutionHandler handler = ObjectKit
                .defaultIfNull(builder.handler, RejectPolicy.ABORT.getValue());

        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime,
                TimeUnit.NANOSECONDS, workQueue, threadFactory, handler);
        if (null != builder.allowCoreThreadTimeOut) {
            threadPoolExecutor.allowCoreThreadTimeOut(builder.allowCoreThreadTimeOut);
        }
        return threadPoolExecutor;
    }

    /**
     * Sets the core pool size for the thread pool. This is the number of threads that will be kept alive in the pool,
     * even if they are idle.
     *
     * @param corePoolSize The initial number of threads in the pool. Default is 0.
     * @return This {@code ExecutorBuilder} instance for method chaining.
     */
    public ExecutorBuilder setCorePoolSize(final int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    /**
     * Sets the maximum pool size for the thread pool. This is the maximum number of threads that can exist in the pool
     * at any given time.
     *
     * @param maxPoolSize The maximum number of threads allowed to execute concurrently. Default is
     *                    {@link Integer#MAX_VALUE}.
     * @return This {@code ExecutorBuilder} instance for method chaining.
     */
    public ExecutorBuilder setMaxPoolSize(final int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    /**
     * Sets the thread keep-alive time. This is the duration for which excess idle threads (threads beyond the core pool
     * size) will wait for new tasks before terminating.
     *
     * @param keepAliveTime The time duration.
     * @param unit          The time unit of the {@code keepAliveTime} argument.
     * @return This {@code ExecutorBuilder} instance for method chaining.
     */
    public ExecutorBuilder setKeepAliveTime(final long keepAliveTime, final TimeUnit unit) {
        return setKeepAliveTime(unit.toNanos(keepAliveTime));
    }

    /**
     * Sets the thread keep-alive time in nanoseconds. This is the duration for which excess idle threads (threads
     * beyond the core pool size) will wait for new tasks before terminating.
     *
     * @param keepAliveTime The thread keep-alive time in nanoseconds.
     * @return This {@code ExecutorBuilder} instance for method chaining.
     */
    public ExecutorBuilder setKeepAliveTime(final long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    /**
     * Sets the {@link BlockingQueue} to be used as the work queue for tasks awaiting execution. Available queue types
     * include:
     *
     * <pre>
     * 1. {@link SynchronousQueue}: Directly hands off tasks to threads without buffering. If no thread is available
     *    and the number of active threads is less than {@code
     * maxPoolSize
     * }, a new thread is created; otherwise, the rejection policy is triggered.
     * 2. {@link LinkedBlockingQueue}: A default unbounded queue. If the number of active threads exceeds {@code
     * corePoolSize
     * },
     *    tasks are always placed in this queue, making {@code
     * maxPoolSize
     * } ineffective. If constructed with a capacity,
     *    it becomes a bounded queue. When full, if active threads are less than {@code
     * maxPoolSize
     * }, a new thread is created;
     *    otherwise, the rejection policy is triggered.
     * 3. {@link ArrayBlockingQueue}: A bounded queue that helps control queue size. When full, if active threads are less than
     *    {@code
     * maxPoolSize
     * }, a new thread is created; otherwise, the rejection policy is triggered.
     * </pre>
     *
     * @param workQueue The {@link BlockingQueue} to use for holding tasks.
     * @return This {@code ExecutorBuilder} instance for method chaining.
     */
    public ExecutorBuilder setWorkQueue(final BlockingQueue<Runnable> workQueue) {
        this.workQueue = workQueue;
        return this;
    }

    /**
     * Configures the builder to use a {@link LinkedBlockingQueue} as the waiting queue. When the queue is full, if the
     * number of active threads is less than {@code maxPoolSize}, a new thread will be created; otherwise, the rejection
     * policy will be triggered.
     *
     * @param capacity The capacity of the {@link LinkedBlockingQueue}.
     * @return This {@code ExecutorBuilder} instance for method chaining.
     */
    public ExecutorBuilder useLinkedBlockingQueue(final int capacity) {
        return setWorkQueue(new LinkedBlockingQueue<>(capacity));
    }

    /**
     * Configures the builder to use an {@link ArrayBlockingQueue} as the waiting queue. This is a bounded queue, which
     * helps in controlling the queue size. When the queue is full, if the number of active threads is less than
     * {@code maxPoolSize}, a new thread will be created; otherwise, the rejection policy will be triggered.
     *
     * @param capacity The capacity of the {@link ArrayBlockingQueue}.
     * @return This {@code ExecutorBuilder} instance for method chaining.
     */
    public ExecutorBuilder useArrayBlockingQueue(final int capacity) {
        return setWorkQueue(new ArrayBlockingQueue<>(capacity));
    }

    /**
     * Configures the builder to use a non-fair {@link SynchronousQueue} as the waiting queue. A
     * {@link SynchronousQueue} directly hands off tasks to threads without buffering. If the number of active threads
     * is less than {@code maxPoolSize}, a new thread is created; otherwise, the rejection policy is triggered.
     *
     * @return This {@code ExecutorBuilder} instance for method chaining.
     */
    public ExecutorBuilder useSynchronousQueue() {
        return useSynchronousQueue(false);
    }

    /**
     * Configures the builder to use a {@link SynchronousQueue} as the waiting queue, with an option for fairness. A
     * {@link SynchronousQueue} directly hands off tasks to threads without buffering. If the number of active threads
     * is less than {@code maxPoolSize}, a new thread is created; otherwise, the rejection policy is triggered.
     *
     * @param fair {@code true} to use a fair access policy; {@code false} for a non-fair policy.
     * @return This {@code ExecutorBuilder} instance for method chaining.
     */
    public ExecutorBuilder useSynchronousQueue(final boolean fair) {
        return setWorkQueue(new SynchronousQueue<>(fair));
    }

    /**
     * Sets the {@link ThreadFactory} to be used for creating new threads in the pool. This allows for custom thread
     * naming, daemon status, and priority settings.
     *
     * @param threadFactory The {@link ThreadFactory} to use.
     * @return This {@code ExecutorBuilder} instance for method chaining.
     * @see ThreadFactoryBuilder
     */
    public ExecutorBuilder setThreadFactory(final ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * Sets the {@link RejectedExecutionHandler} for the thread pool. This handler is invoked when the
     * {@link ThreadPoolExecutor} cannot accept a new task, typically because the pool is saturated and its work queue
     * is full.
     *
     * @param handler The {@link RejectedExecutionHandler} to use.
     * @return This {@code ExecutorBuilder} instance for method chaining.
     * @see RejectPolicy
     */
    public ExecutorBuilder setHandler(final RejectedExecutionHandler handler) {
        this.handler = handler;
        return this;
    }

    /**
     * Sets whether core threads are allowed to time out and terminate if they remain idle for the
     * {@code keepAliveTime}. By default, core threads do not time out.
     *
     * @param allowCoreThreadTimeOut {@code true} to allow core threads to time out; {@code false} otherwise.
     * @return This {@code ExecutorBuilder} instance for method chaining.
     */
    public ExecutorBuilder setAllowCoreThreadTimeOut(final boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        return this;
    }

    /**
     * Builds and returns a new {@link ThreadPoolExecutor} instance based on the current builder's configuration.
     *
     * @return A new {@link ThreadPoolExecutor}.
     */
    @Override
    public ThreadPoolExecutor build() {
        return build(this);
    }

    /**
     * Creates an {@link ExecutorService} that is wrapped by a {@link FinalizableDelegatedExecutorService}. This
     * provides an {@link ExecutorService} that can be gracefully shut down and has its resources reclaimed.
     *
     * @return A new {@link ExecutorService} with finalization capabilities.
     */
    public ExecutorService buildFinalizable() {
        return new FinalizableDelegatedExecutorService(build());
    }

}
