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
package org.miaixz.bus.tempus.timings;

import org.miaixz.bus.tempus.crontab.TimerCrontab;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Represents a list of tasks in a timing wheel slot, implemented as a doubly linked list. Each {@code TimerTaskList}
 * acts as a bucket for tasks that expire within the same time range.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TimerTaskList implements Delayed {

    /**
     * The expiration time for this task list, in milliseconds.
     */
    private final AtomicLong expire;

    /**
     * The root node of the doubly linked list, which acts as a sentinel.
     */
    private final TimerCrontab root;

    /**
     * Constructs a new TimerTaskList.
     */
    public TimerTaskList() {
        expire = new AtomicLong(-1L);

        // Initialize the root sentinel node for the circular doubly linked list.
        root = new TimerCrontab(null, -1L);
        root.prev = root;
        root.next = root;
    }

    /**
     * Sets the expiration time for this task list.
     *
     * @param expire The expiration time in milliseconds.
     * @return {@code true} if the expiration time was successfully updated, {@code false} otherwise.
     */
    public boolean setExpiration(final long expire) {
        return this.expire.getAndSet(expire) != expire;
    }

    /**
     * Gets the expiration time for this task list.
     *
     * @return The expiration time in milliseconds.
     */
    public long getExpire() {
        return expire.get();
    }

    /**
     * Adds a task to the head of the doubly linked list.
     *
     * @param timerCrontab The delayed task to add.
     */
    public void addTask(final TimerCrontab timerCrontab) {
        synchronized (this) {
            if (timerCrontab.timerTaskList == null) {
                timerCrontab.timerTaskList = this;
                final TimerCrontab tail = root.prev;
                timerCrontab.next = root;
                timerCrontab.prev = tail;
                tail.next = timerCrontab;
                root.prev = timerCrontab;
            }
        }
    }

    /**
     * Removes a task from this list.
     *
     * @param timerCrontab The task to remove.
     */
    public void removeTask(final TimerCrontab timerCrontab) {
        synchronized (this) {
            if (this.equals(timerCrontab.timerTaskList)) {
                timerCrontab.next.prev = timerCrontab.prev;
                timerCrontab.prev.next = timerCrontab.next;
                timerCrontab.timerTaskList = null;
                timerCrontab.next = null;
                timerCrontab.prev = null;
            }
        }
    }

    /**
     * Processes all tasks in this list by passing them to the provided consumer. This is typically called when the list
     * expires.
     *
     * @param flush The consumer function to handle each task.
     */
    public synchronized void flush(final Consumer<TimerCrontab> flush) {
        TimerCrontab timerCrontab = root.next;
        while (!timerCrontab.equals(root)) {
            this.removeTask(timerCrontab);
            flush.accept(timerCrontab);
            timerCrontab = root.next;
        }
        expire.set(-1L);
    }

    /**
     * Returns the remaining delay associated with this object. If the remaining delay is zero or negative, the delay
     * has already elapsed.
     *
     * @param unit the time unit
     * @return the remaining delay; zero or negative values indicate that the delay has already elapsed
     */
    @Override
    public long getDelay(final TimeUnit unit) {
        return Math.max(0, unit.convert(expire.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
     * <p>
     * The comparison is based on the expiration time of this task list. If the specified object is not a
     * {@code TimerTaskList}, this method returns 0.
     * </p>
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final Delayed o) {
        if (o instanceof TimerTaskList) {
            return Long.compare(expire.get(), ((TimerTaskList) o).expire.get());
        }
        return 0;
    }

}
