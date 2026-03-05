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
