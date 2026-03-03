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
package org.miaixz.bus.cron.crontab;

import org.miaixz.bus.cron.Repertoire;
import org.miaixz.bus.cron.Scheduler;

import java.util.concurrent.locks.Lock;

/**
 * Task table based on matching<br>
 * Each time checks if the expressions in the task table match the specified time, and executes the corresponding Task
 * if they match
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MatchCrontab extends Repertoire {

    /**
     * Constructor with default capacity of {@link Repertoire#DEFAULT_CAPACITY}.
     */
    public MatchCrontab() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructor.
     *
     * @param initialCapacity Initial capacity.
     */
    public MatchCrontab(final int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Executes all tasks whose cron patterns match the given timestamp.
     * <p>
     * This implementation iterates through all tasks and checks if their patterns match the current time.
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
            executeTaskIfMatchInternal(scheduler, millis);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Execute the corresponding Task if the time matches, without lock.
     *
     * @param scheduler {@link Scheduler}.
     * @param millis    Time in milliseconds.
     */
    private void executeTaskIfMatchInternal(final Scheduler scheduler, final long millis) {
        final int size = size();
        for (int i = 0; i < size; i++) {
            if (this.table.getMiddle(i)
                    .match(scheduler.config.getTimeZone(), millis, scheduler.config.isMatchSecond())) {
                scheduler.manager.spawnExecutor(
                        new CronCrontab(this.table.getLeft(i), this.table.getMiddle(i), this.table.getRight(i)));
            }
        }
    }

}
