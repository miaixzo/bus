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
