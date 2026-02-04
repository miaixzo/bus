/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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

    /**
     * Priority queue for managing trigger times of scheduled tasks.
     */
    private final PriorityBlockingQueue<TriggerTime> triggerQueue;

    /**
     * Constructor with default capacity of {@link Repertoire#DEFAULT_CAPACITY}.
     */
    public TriggerCrontab() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructor.
     *
     * @param initialCapacity Initial capacity.
     */
    public TriggerCrontab(final int initialCapacity) {
        super(initialCapacity);
        this.triggerQueue = new PriorityBlockingQueue<>(initialCapacity);
    }

    /**
     * Adds a task to the schedule and adds its next trigger time to the queue.
     *
     * @param id      the unique identifier for the task.
     * @param pattern the cron pattern defining when the task should execute.
     * @param crontab the task to be scheduled.
     * @return this {@link TriggerCrontab} instance.
     */
    @Override
    public TriggerCrontab add(String id, CronPattern pattern, Crontab crontab) {
        super.add(id, pattern, crontab);
        // Add the next trigger time and task to the queue
        this.triggerQueue.offer(new TriggerTime(id, pattern.nextMatchFromNow()));
        return this;
    }

    /**
     * Removes a task from the schedule and the trigger queue.
     *
     * @param id the unique identifier for the task to remove.
     * @return {@code true} if the task was found and removed, {@code false} otherwise.
     */
    @Override
    public boolean remove(String id) {
        // Remove the task from the queue
        this.triggerQueue.removeIf(task -> StringKit.equals(task.id(), id));
        return super.remove(id);

    }

    /**
     * Updates the cron pattern for an existing task and recalculates its next trigger time.
     *
     * @param id      the unique identifier for the task to update.
     * @param pattern the new cron pattern.
     * @return {@code true} if the task was found and updated, {@code false} otherwise.
     */
    @Override
    public boolean updatePattern(String id, CronPattern pattern) {
        // Remove the task from the queue
        this.triggerQueue.removeIf(task -> StringKit.equals(task.id(), id));
        // Add the next trigger time and task to the queue
        this.triggerQueue.offer(new TriggerTime(id, pattern.nextMatchFromNow()));
        return super.updatePattern(id, pattern);
    }

    /**
     * Executes all tasks scheduled to run at or before the given timestamp.
     * <p>
     * This implementation uses a priority queue to efficiently retrieve tasks in order of their trigger times.
     * </p>
     *
     * @param scheduler the scheduler that will execute the tasks.
     * @param millis    the current timestamp in milliseconds.
     */
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
     * Execute all tasks at the given timestamp and before.
     * <p>
     * This method depends on the trigger queue.
     * </p>
     *
     * @param scheduler {@link Scheduler}.
     * @param millis    Time in milliseconds.
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
     * Get a {@link CronCrontab} without lock.
     *
     * @param id ID.
     * @return {@link CronCrontab}.
     */
    private CronCrontab getCronTask(final String id) {
        final int index = this.table.indexOfLeft(id);
        return index > -1 ? new CronCrontab(id, this.table.getMiddle(index), this.table.getRight(index)) : null;
    }

    /**
     * Trigger time record.
     *
     * @param id        ID.
     * @param timestamp Trigger time.
     */
    private record TriggerTime(String id, long timestamp) implements Comparable<TriggerTime> {

        /**
         * Compares this trigger time with another based on timestamp.
         *
         * @param other the other trigger time to compare to.
         * @return a negative integer, zero, or a positive integer as this trigger time is less than, equal to, or
         *         greater than the specified trigger time.
         */
        @Override
        public int compareTo(TriggerTime other) {
            return Long.compare(this.timestamp, other.timestamp);
        }
    }

}
