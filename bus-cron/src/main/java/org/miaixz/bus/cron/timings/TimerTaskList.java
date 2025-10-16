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

    @Override
    public long getDelay(final TimeUnit unit) {
        return Math.max(0, unit.convert(expire.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public int compareTo(final Delayed o) {
        if (o instanceof TimerTaskList) {
            return Long.compare(expire.get(), ((TimerTaskList) o).expire.get());
        }
        return 0;
    }

}
