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
package org.miaixz.bus.cron.timings;

import org.miaixz.bus.cron.crontab.TimerCrontab;
import org.miaixz.bus.logger.Logger;

import java.util.function.Consumer;

/**
 * A hierarchical timing wheel, commonly used for managing delayed tasks. A timing wheel is a circular data structure
 * with multiple slots, each holding a collection of tasks. A single thread advances the time, moving from one slot to
 * the next and executing the tasks within.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TimingWheel {

    /**
     * The duration of a single time slot (tick) in milliseconds.
     */
    private final long tickMs;

    /**
     * The number of slots in the timing wheel.
     */
    private final int wheelSize;

    /**
     * The total time span of this wheel, calculated as {@code tickMs * wheelSize}.
     */
    private final long interval;

    /**
     * The array of slots, where each slot is a list of tasks.
     */
    private final TimerTaskList[] timerTaskLists;
    /**
     * The handler for processing expired task lists.
     */
    private final Consumer<TimerTaskList> consumer;
    /**
     * The current time of the wheel, aligned to the nearest {@code tickMs}.
     */
    private long currentTime;
    /**
     * The next-level (overflow) timing wheel for tasks with delays beyond this wheel's interval.
     */
    private volatile TimingWheel overflowWheel;

    /**
     * Constructs a new TimingWheel.
     *
     * @param tickMs    The duration of a single time slot in milliseconds.
     * @param wheelSize The number of slots in the wheel.
     * @param consumer  The handler for processing expired task lists.
     */
    public TimingWheel(final long tickMs, final int wheelSize, final Consumer<TimerTaskList> consumer) {
        this(tickMs, wheelSize, System.currentTimeMillis(), consumer);
    }

    /**
     * Constructs a new TimingWheel.
     *
     * @param tickMs      The duration of a single time slot in milliseconds.
     * @param wheelSize   The number of slots in the wheel.
     * @param currentTime The initial current time in milliseconds.
     * @param consumer    The handler for processing expired task lists.
     */
    public TimingWheel(final long tickMs, final int wheelSize, final long currentTime,
            final Consumer<TimerTaskList> consumer) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.interval = tickMs * wheelSize;
        this.timerTaskLists = new TimerTaskList[wheelSize];
        for (int i = 0; i < wheelSize; i++) {
            this.timerTaskLists[i] = new TimerTaskList();
        }

        // Align the current time to the nearest tick.
        this.currentTime = currentTime - (currentTime % tickMs);
        this.consumer = consumer;
    }

    /**
     * Adds a task to the timing wheel.
     *
     * @param timerCrontab The task to add.
     * @return {@code true} if the task was successfully added, {@code false} if the task was already expired.
     */
    public boolean addTask(final TimerCrontab timerCrontab) {
        final long expiration = timerCrontab.getDelayMs();
        // If the task is already expired, it cannot be added.
        if (expiration < currentTime + tickMs) {
            return false;
        } else if (expiration < currentTime + interval) {
            // The task fits within the current wheel's time span.
            final long virtualId = expiration / tickMs;
            final int index = (int) (virtualId % wheelSize);
            Logger.debug("tickMs: {} ------index: {} ------expiration: {}", tickMs, index, expiration);

            final TimerTaskList timerTaskList = timerTaskLists[index];
            timerTaskList.addTask(timerCrontab);
            // If the expiration time of the list is updated, it needs to be re-inserted into the delay queue.
            if (timerTaskList.setExpiration(virtualId * tickMs)) {
                consumer.accept(timerTaskList);
            }
        } else {
            // The task's delay is too long for this wheel; pass it to the overflow wheel.
            final TimingWheel timeWheel = getOverflowWheel();
            timeWheel.addTask(timerCrontab);
        }
        return true;
    }

    /**
     * Advances the clock of the timing wheel to the specified timestamp.
     *
     * @param timestamp The new timestamp to advance to.
     */
    public void advanceClock(final long timestamp) {
        if (timestamp >= currentTime + tickMs) {
            currentTime = timestamp - (timestamp % tickMs);
            if (overflowWheel != null) {
                // Propagate the time advance to the overflow wheel.
                this.getOverflowWheel().advanceClock(timestamp);
            }
        }
    }

    /**
     * Lazily creates and returns the overflow (higher-level) timing wheel.
     *
     * @return The overflow {@link TimingWheel}.
     */
    private TimingWheel getOverflowWheel() {
        if (overflowWheel == null) {
            synchronized (this) {
                if (overflowWheel == null) {
                    overflowWheel = new TimingWheel(interval, wheelSize, currentTime, consumer);
                }
            }
        }
        return overflowWheel;
    }

}
