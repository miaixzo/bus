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
package org.miaixz.bus.core.cache;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.ThreadKit;

/**
 * A global timer pool for scheduling cache pruning tasks. This is used by cache implementations that require expiration
 * support.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum GlobalPruneTimer {

    /**
     * The singleton instance.
     */
    INSTANCE;

    /**
     * A counter for naming the cache task threads.
     */
    private final AtomicInteger cacheTaskNumber = new AtomicInteger(1);

    /**
     * The scheduler for pruning tasks.
     */
    private ScheduledExecutorService pruneTimer;

    /**
     * Private constructor to initialize the timer.
     */
    GlobalPruneTimer() {
        init();
    }

    /**
     * Schedules a task to run periodically at a fixed rate.
     *
     * @param task  The task to be executed.
     * @param delay The period between successive executions, in milliseconds.
     * @return A {@link ScheduledFuture} representing the pending completion of the task, which can be used to cancel
     *         it.
     */
    public ScheduledFuture<?> schedule(final Runnable task, final long delay) {
        return this.pruneTimer.scheduleAtFixedRate(task, delay, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Initializes the timer. If a timer already exists, it is shut down before creating a new one.
     */
    public void init() {
        if (null != pruneTimer) {
            shutdownNow();
        }
        this.pruneTimer = new ScheduledThreadPoolExecutor(1,
                r -> ThreadKit.newThread(r, StringKit.format("Pure-Timer-{}", cacheTaskNumber.getAndIncrement())));
    }

    /**
     * Shuts down the global timer gracefully, allowing existing tasks to complete.
     */
    public void shutdown() {
        if (null != pruneTimer) {
            pruneTimer.shutdown();
        }
    }

    /**
     * Shuts down the global timer immediately, attempting to stop all actively executing tasks.
     *
     * @return A list of tasks that were awaiting execution, or {@code null} if the timer was not running.
     */
    public List<Runnable> shutdownNow() {
        if (null != pruneTimer) {
            return pruneTimer.shutdownNow();
        }
        return null;
    }

}
