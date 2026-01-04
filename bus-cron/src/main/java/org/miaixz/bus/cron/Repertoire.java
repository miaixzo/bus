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

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.miaixz.bus.core.center.map.TripletTable;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cron.crontab.Crontab;
import org.miaixz.bus.cron.pattern.CronPattern;

/**
 * Task table for cron jobs. This class holds a mapping between task IDs, cron patterns, and the tasks themselves. The
 * scheduler periodically checks all tasks in this table to see if their patterns match the current time, and if so,
 * executes the corresponding task. Read-write locks are used to ensure thread safety for adding and removing tasks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Repertoire implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852287269608L;

    /**
     * Default initial capacity for the task table.
     */
    public static final int DEFAULT_CAPACITY = 256;
    /**
     * Read-write lock to ensure thread safety.
     */
    public final ReadWriteLock lock;
    /**
     * Task table with one-to-one mapping of ID, pattern, and task. Uses TripleTable for storage to facilitate fast
     * lookup and updates.
     */
    public final TripletTable<String, CronPattern, Crontab> table;

    /**
     * Constructs a new Repertoire with the default capacity.
     */
    public Repertoire() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructor.
     *
     * @param initialCapacity Capacity, i.e., the estimated maximum number of tasks.
     */
    public Repertoire(final int initialCapacity) {
        lock = new ReentrantReadWriteLock();
        this.table = new TripletTable<>(initialCapacity);
    }

    /**
     * Size of the task table, i.e., the number of added tasks.
     *
     * @return Size of the task table, i.e., the number of added tasks.
     */
    public int size() {
        return this.table.size();
    }

    /**
     * Whether the task table is empty.
     *
     * @return {@code true} if empty.
     */
    public boolean isEmpty() {
        return size() < 1;
    }

    /**
     * Gets all IDs, returns an immutable list, i.e., the list cannot be modified.
     *
     * @return List of IDs.
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
     * Gets all scheduled tasks, returns an immutable list, i.e., the list cannot be modified.
     *
     * @return List of scheduled tasks.
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
     * Gets the {@link Crontab} at the specified position.
     *
     * @param index Position.
     * @return {@link Crontab}.
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
     * Gets the {@link Crontab} with the specified ID.
     *
     * @param id ID.
     * @return {@link Crontab}.
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
     * Gets all cron patterns, returns an immutable list, i.e., the list cannot be modified.
     *
     * @return List of cron patterns.
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
     * Gets the {@link CronPattern} with the specified ID.
     *
     * @param id ID.
     * @return {@link CronPattern}.
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
     * Gets the {@link CronPattern} at the specified position.
     *
     * @param index Position.
     * @return {@link CronPattern}.
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
     * Adds a new Task.
     *
     * @param id      ID.
     * @param pattern {@link CronPattern}.
     * @param crontab {@link Crontab}.
     * @return this.
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
     * Removes a Task.
     *
     * @param id ID of the Task.
     * @return Whether the removal was successful, {@code false} means no task with the corresponding ID was found.
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
     * Updates the cron pattern of a specific Task
     *
     * @param id      ID of the Task
     * @param pattern New cron pattern
     * @return Whether the update was successful, returns false if the pattern corresponding to the ID does not exist
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
     * Returns a string representation of the task table, showing all tasks with their IDs, patterns, and task objects.
     *
     * @return a string representation of the task table.
     */
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
     * Matches tasks based on the given timestamp, and if a match is successful, executes the corresponding Task using
     * the scheduler.
     *
     * @param scheduler Scheduler.
     * @param millis    Timestamp.
     */
    public abstract void execute(final Scheduler scheduler, final long millis);

}
