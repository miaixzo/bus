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
package org.miaixz.bus.tempus.crontab;

import org.miaixz.bus.tempus.timings.TimerTaskList;

/**
 * Represents a delayed task to be executed by a timer. This class is a node in a doubly linked list, used within a
 * {@link TimerTaskList}.
 *
 * @author Kimi Liu
 * @since Java 21+
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
