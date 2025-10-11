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
 * @since Java 17+
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
