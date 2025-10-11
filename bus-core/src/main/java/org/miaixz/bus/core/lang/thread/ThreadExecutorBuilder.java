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

import java.util.concurrent.*;

/**
 * A utility class for creating various types of {@link Executor} instances, similar to {@link Executors}. It provides
 * convenient methods to build thread pools with common configurations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThreadExecutorBuilder {

    /**
     * Creates a thread pool with a fixed number of threads (corePoolSize == maximumPoolSize). Core threads will always
     * remain in the pool and will not be reclaimed. If a core thread terminates due to an exception, a new thread will
     * be created. This executor uses an unbounded {@link LinkedBlockingQueue}.
     *
     * @param corePoolSize The number of threads to keep in the pool, even if they are idle.
     * @param prefix       The prefix for the names of threads created in this pool.
     * @return A new {@link Executor} instance configured as a fixed-size thread pool.
     */
    public static Executor newFixedFastThread(final int corePoolSize, final String prefix) {
        return new ThreadPoolExecutor(corePoolSize, corePoolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new NamedThreadFactory(prefix));
    }

    /**
     * Creates a cached thread pool. This pool will create new threads as needed to execute new tasks, but will reuse
     * previously constructed threads when they are available. Threads that have not been used for sixty seconds are
     * terminated and removed from the cache. This pool is suitable for applications that may launch many short-lived
     * tasks. It uses a {@link SynchronousQueue} and has a core pool size of 0 and a maximum pool size of
     * {@link Integer#MAX_VALUE}.
     *
     * @param prefix The prefix for the names of threads created in this pool.
     * @return A new {@link Executor} instance configured as a cached thread pool.
     */
    public static Executor newCachedFastThread(final String prefix) {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new NamedThreadFactory(prefix));
    }

    /**
     * Creates a new {@link Executor} with the given initial parameters. This method provides fine-grained control over
     * the thread pool's configuration.
     *
     * @param corePoolSize    The number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize The maximum number of threads allowed in the pool.
     * @param keepAliveTime   When the number of threads is greater than the core pool size, this is the maximum time
     *                        that excess idle threads will wait for new tasks before terminating.
     * @param unit            The time unit for the {@code keepAliveTime} argument.
     * @param workQueue       The queue to use for holding tasks before they are executed. This queue will only hold
     *                        {@link Runnable} tasks submitted by the {@code execute} method.
     * @param prefix          The prefix for the names of threads created in this pool.
     * @param handler         The {@link RejectedExecutionHandler} to use when the {@link Executor} cannot accept a new
     *                        task, for example, when its maximum capacity and queue capacity are reached.
     * @return A new {@link Executor} instance configured with the specified parameters.
     */
    public static Executor newLimitedFastThread(
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue,
            final String prefix,
            final RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                new NamedThreadFactory(prefix), handler);
    }

}
