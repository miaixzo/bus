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

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cron.Repertoire;
import org.miaixz.bus.cron.Scheduler;
import org.miaixz.bus.cron.pattern.CronPattern;

/**
 * Task table with trigger queue<br>
 * When a user adds a task, the next trigger time is added to the queue, and tasks are taken from the queue for
 * execution.<br>
 * When executing tasks, the queue is checked, and when the trigger time of tasks in the queue is less than the current
 * time, they are taken from the queue and executed.<br>
 * After execution, the next trigger time is added to the queue.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TriggerCrontab extends Repertoire {

    private final PriorityBlockingQueue<TriggerTime> triggerQueue;

    /**
     * Constructor with default capacity of {@link Repertoire#DEFAULT_CAPACITY}
     */
    public TriggerCrontab() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructor
     *
     * @param initialCapacity Initial capacity
     */
    public TriggerCrontab(final int initialCapacity) {
        super(initialCapacity);
        this.triggerQueue = new PriorityBlockingQueue<>(initialCapacity);
    }

    @Override
    public TriggerCrontab add(String id, CronPattern pattern, Crontab crontab) {
        super.add(id, pattern, crontab);
        // Add the next trigger time and task to the queue
        this.triggerQueue.offer(new TriggerTime(id, pattern.nextMatchFromNow()));
        return this;
    }

    @Override
    public boolean remove(String id) {
        // Remove the task from the queue
        this.triggerQueue.removeIf(task -> StringKit.equals(task.id(), id));
        return super.remove(id);

    }

    @Override
    public boolean updatePattern(String id, CronPattern pattern) {
        // Remove the task from the queue
        this.triggerQueue.removeIf(task -> StringKit.equals(task.id(), id));
        // Add the next trigger time and task to the queue
        this.triggerQueue.offer(new TriggerTime(id, pattern.nextMatchFromNow()));
        return super.updatePattern(id, pattern);
    }

    @Override
    public void execute(final Scheduler scheduler, final long millis) {
        final Lock readLock = lock.readLock();
        readLock.lock();
        try {
            executeTaskBeforeInternal(scheduler, millis);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Execute all tasks at the given timestamp and before<br>
     * This method depends on the trigger queue
     *
     * @param scheduler {@link Scheduler}
     * @param millis    Time in milliseconds
     */
    private void executeTaskBeforeInternal(final Scheduler scheduler, final long millis) {
        while (true) {
            final TriggerTime triggerTime = this.triggerQueue.poll();
            if (null == triggerTime) {
                // Queue is empty
                break;
            }

            final long triggerTimestamp = triggerTime.timestamp();
            if (triggerTimestamp > millis) {
                // Task time has not arrived yet
                this.triggerQueue.offer(triggerTime);
                break;
            }

            // Execute the task
            final String id = triggerTime.id();
            scheduler.manager.spawnExecutor(getCronTask(id));

            // Add the next trigger time and task to the queue
            long nextMillis = millis;
            if ((millis - triggerTimestamp) / 1000 == 0) {
                // Same second level, indicating this second has already been executed, start from the next second
                nextMillis += 1000;
            }
            this.triggerQueue.offer(new TriggerTime(id, getPattern(id).nextMatch(nextMillis)));
        }
    }

    /**
     * Get a {@link CronCrontab} without lock
     *
     * @param id ID
     * @return {@link CronCrontab}
     */
    private CronCrontab getCronTask(final String id) {
        final int index = this.table.indexOfLeft(id);
        return index > -1 ? new CronCrontab(id, this.table.getMiddle(index), this.table.getRight(index)) : null;
    }

    /**
     * Trigger time
     *
     * @param id        ID
     * @param timestamp Trigger time
     */
    private record TriggerTime(String id, long timestamp) implements Comparable<TriggerTime> {

        @Override
        public int compareTo(TriggerTime other) {
            return Long.compare(this.timestamp, other.timestamp);
        }
    }

}
