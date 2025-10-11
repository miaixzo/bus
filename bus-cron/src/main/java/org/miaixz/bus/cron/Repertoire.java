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
package org.miaixz.bus.cron;

import org.miaixz.bus.core.center.map.TripleTable;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cron.crontab.CronCrontab;
import org.miaixz.bus.cron.crontab.Crontab;
import org.miaixz.bus.cron.pattern.CronPattern;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Task table for cron jobs. This class holds a mapping between task IDs, cron patterns, and the tasks themselves. The
 * scheduler periodically checks all tasks in this table to see if their patterns match the current time, and if so,
 * executes the corresponding task. Read-write locks are used to ensure thread safety for adding and removing tasks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Repertoire implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852287269608L;

    /**
     * Default initial capacity for the task table.
     */
    public static final int DEFAULT_CAPACITY = 10;
    private final ReadWriteLock lock;
    private final TripleTable<String, CronPattern, Crontab> table;

    /**
     * Constructs a new Repertoire with the default capacity.
     */
    public Repertoire() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs a new Repertoire with the specified initial capacity.
     *
     * @param initialCapacity The initial capacity, representing the estimated maximum number of tasks.
     */
    public Repertoire(final int initialCapacity) {
        lock = new ReentrantReadWriteLock();
        this.table = new TripleTable<>(initialCapacity);
    }

    /**
     * Adds a new task to the table.
     *
     * @param id      The task ID.
     * @param pattern The {@link CronPattern}.
     * @param crontab The {@link Crontab} task.
     * @return this {@link Repertoire} instance.
     * @throws CrontabException if the ID already exists.
     */
    public Repertoire add(final String id, final CronPattern pattern, final Crontab crontab) {
        final Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            if (this.table.containLeft(id)) {
                throw new CrontabException("Id [{}] has been existed!", id);
            }
            this.table.put(id, pattern, crontab);
        } finally {
            writeLock.unlock();
        }
        return this;
    }

    /**
     * Gets an unmodifiable list of all task IDs.
     *
     * @return The list of task IDs.
     */
    public List<String> getIds() {
        final Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return this.table.getLefts();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets an unmodifiable list of all cron patterns.
     *
     * @return The list of cron patterns.
     */
    public List<CronPattern> getPatterns() {
        final Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return this.table.getMiddles();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets an unmodifiable list of all tasks.
     *
     * @return The list of tasks.
     */
    public List<Crontab> getTasks() {
        final Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return this.table.getRights();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Removes a task by its ID.
     *
     * @param id The ID of the task to remove.
     * @return {@code true} if the task was successfully removed, {@code false} if no task with the given ID was found.
     */
    public boolean remove(final String id) {
        final Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            final int index = this.table.indexOfLeft(id);
            if (index > -1) {
                this.table.remove(index);
                return true;
            }
        } finally {
            writeLock.unlock();
        }
        return false;
    }

    /**
     * Updates the cron pattern for a specific task.
     *
     * @param id      The ID of the task.
     * @param pattern The new cron pattern.
     * @return {@code true} if the update was successful, {@code false} if no task with the given ID was found.
     */
    public boolean updatePattern(final String id, final CronPattern pattern) {
        final Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            final int index = this.table.indexOfLeft(id);
            if (index > -1) {
                this.table.setMiddle(index, pattern);
                return true;
            }
        } finally {
            writeLock.unlock();
        }
        return false;
    }

    /**
     * Gets the {@link Crontab} at the specified index.
     *
     * @param index The index.
     * @return The {@link Crontab} at the given index.
     */
    public Crontab getTask(final int index) {
        final Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return this.table.getRight(index);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets the {@link Crontab} for the specified ID.
     *
     * @param id The task ID.
     * @return The {@link Crontab}, or {@code null} if not found.
     */
    public Crontab getTask(final String id) {
        final Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return table.getRightByLeft(id);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets the {@link CronPattern} for the specified ID.
     *
     * @param id The task ID.
     * @return The {@link CronPattern}, or {@code null} if not found.
     */
    public CronPattern getPattern(final String id) {
        final Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return table.getMiddleByLeft(id);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets the {@link CronPattern} at the specified index.
     *
     * @param index The index.
     * @return The {@link CronPattern} at the given index.
     */
    public CronPattern getPattern(final int index) {
        final Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return table.getMiddle(index);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the number of tasks in the table.
     *
     * @return The number of scheduled tasks.
     */
    public int size() {
        return this.table.size();
    }

    /**
     * Checks if the task table is empty.
     *
     * @return {@code true} if the table is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return size() < 1;
    }

    /**
     * Executes all tasks that match the given time. This method acquires a read lock.
     *
     * @param scheduler The {@link Scheduler}.
     * @param millis    The current time in milliseconds.
     */
    public void executeTaskIfMatch(final Scheduler scheduler, final long millis) {
        final Lock readLock = lock.readLock();
        readLock.lock();
        try {
            executeTaskIfMatchInternal(scheduler, millis);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String toString() {
        final int size = this.size();
        final StringBuilder builder = StringKit.builder();
        for (int i = 0; i < size; i++) {
            builder.append(
                    StringKit.format(
                            "[{}] [{}] [{}]\n",
                            this.table.getLeft(i),
                            this.table.getMiddle(i),
                            this.table.getRight(i)));
        }
        return builder.toString();
    }

    /**
     * Internal method to execute matching tasks without acquiring a lock. The caller is responsible for locking.
     *
     * @param scheduler The {@link Scheduler}.
     * @param millis    The current time in milliseconds.
     */
    protected void executeTaskIfMatchInternal(final Scheduler scheduler, final long millis) {
        final int size = size();
        for (int i = 0; i < size; i++) {
            if (this.table.getMiddle(i).match(scheduler.config.timezone, millis, scheduler.config.matchSecond)) {
                scheduler.manager.spawnExecutor(
                        new CronCrontab(this.table.getLeft(i), this.table.getMiddle(i), this.table.getRight(i)));
            }
        }
    }

}
