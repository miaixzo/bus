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
package org.miaixz.bus.cron.crontab;

import org.miaixz.bus.cron.timings.TimerTaskList;

/**
 * Represents a delayed task to be executed by a timer. This class is a node in a doubly linked list, used within a
 * {@link TimerTaskList}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TimerCrontab {

    /**
     * The expiration time in milliseconds. The task will be executed at or after this time.
     */
    private final long delayMs;

    /**
     * The task to be executed.
     */
    private final Runnable task;
    /**
     * A description of the task.
     */
    public String desc;
    /**
     * The time slot (bucket) this task belongs to.
     */
    public TimerTaskList timerTaskList;
    /**
     * The next task in the linked list.
     */
    public TimerCrontab next;
    /**
     * The previous task in the linked list.
     */
    public TimerCrontab prev;

    /**
     * Constructs a new delayed task.
     *
     * @param task    The {@link Runnable} to execute.
     * @param delayMs The delay in milliseconds from the current time.
     */
    public TimerCrontab(final Runnable task, final long delayMs) {
        this.delayMs = System.currentTimeMillis() + delayMs;
        this.task = task;
    }

    /**
     * Gets the task to be executed.
     *
     * @return The {@link Runnable} task.
     */
    public Runnable getTask() {
        return task;
    }

    /**
     * Gets the absolute expiration time in milliseconds. This is calculated as the creation time plus the delay
     * duration.
     *
     * @return The expiration time in milliseconds.
     */
    public long getDelayMs() {
        return delayMs;
    }

    /**
     * Returns the string representation of this task, which is its description.
     *
     * @return the task description, or {@code null} if no description is set.
     */
    @Override
    public String toString() {
        return desc;
    }

}
