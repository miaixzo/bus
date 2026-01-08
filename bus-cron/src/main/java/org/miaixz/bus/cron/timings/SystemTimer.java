/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.cron.timings;

import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.cron.crontab.TimerCrontab;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A system timer that manages delayed tasks using a {@link TimingWheel}. It uses a {@link DelayQueue} to efficiently
 * retrieve expired task lists (buckets) from the timing wheel.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SystemTimer {

    /**
     * The underlying timing wheel.
     */
    private final TimingWheel timeWheel;

    /**
     * A single delay queue is used for the timer to hold expired task lists.
     */
    private final DelayQueue<TimerTaskList> delayQueue = new DelayQueue<>();

    /**
     * The timeout for polling the delay queue, in milliseconds. Defaults to 100ms.
     */
    private long delayQueueTimeout = 100;

    /**
     * The thread pool for the boss thread that polls the delay queue for expired tasks.
     */
    private ExecutorService bossThreadPool;
    /**
     * A flag to control the running state of the boss thread.
     */
    private volatile boolean isRunning;

    /**
     * Constructs a new SystemTimer. Initializes a {@link TimingWheel} with a tick duration of 1ms and 20 slots.
     */
    public SystemTimer() {
        timeWheel = new TimingWheel(1, 20, delayQueue::offer);
    }

    /**
     * Sets the timeout for polling the delay queue.
     *
     * @param delayQueueTimeout The timeout in milliseconds.
     * @return this {@link SystemTimer} instance.
     */
    public SystemTimer setDelayQueueTimeout(final long delayQueueTimeout) {
        this.delayQueueTimeout = delayQueueTimeout;
        return this;
    }

    /**
     * Starts the timer asynchronously. A background thread is started to continuously check for and process expired
     * tasks.
     *
     * @return this {@link SystemTimer} instance.
     */
    public SystemTimer start() {
        bossThreadPool = ThreadKit.newSingleExecutor();
        isRunning = true;
        bossThreadPool.submit(() -> {
            while (true) {
                if (!advanceClock()) {
                    break;
                }
            }
        });
        return this;
    }

    /**
     * Forcibly stops the timer. This will stop the background thread and shut down the executor service.
     */
    public void stop() {
        this.isRunning = false;
        if (this.bossThreadPool != null) {
            this.bossThreadPool.shutdown();
        }
    }

    /**
     * Adds a delayed task to the timer.
     *
     * @param timerCrontab The {@link TimerCrontab} to add.
     */
    public void addTask(final TimerCrontab timerCrontab) {
        // If the task cannot be added to the timing wheel (e.g., its delay is in the past),
        // execute it immediately in a separate thread.
        if (!timeWheel.addTask(timerCrontab)) {
            ThreadKit.execAsync(timerCrontab.getTask());
        }
    }

    /**
     * Advances the clock of the timing wheel and processes any expired tasks. This method is called repeatedly by the
     * background thread.
     *
     * @return {@code true} if the timer should continue running, {@code false} if it has been stopped.
     */
    private boolean advanceClock() {
        if (!isRunning) {
            return false;
        }
        try {
            final TimerTaskList timerTaskList = poll();
            if (null != timerTaskList) {
                // Advance the timing wheel's clock to the expiration time of the retrieved list.
                timeWheel.advanceClock(timerTaskList.getExpire());
                // Execute all tasks in the expired list (this may include cascading to lower-level wheels).
                timerTaskList.flush(this::addTask);
            }
        } catch (final InterruptedException ignore) {
            // If interrupted, stop the timer.
            return false;
        }
        return true;
    }

    /**
     * Polls the delay queue to retrieve the next expired task list.
     *
     * @return The expired {@link TimerTaskList}, or {@code null} if the poll times out or returns immediately.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    private TimerTaskList poll() throws InterruptedException {
        return this.delayQueueTimeout > 0 ? delayQueue.poll(delayQueueTimeout, TimeUnit.MILLISECONDS)
                : delayQueue.poll();
    }

}
