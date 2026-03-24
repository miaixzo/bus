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
package org.miaixz.bus.tempus;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.miaixz.bus.core.center.map.TripletTable;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.tempus.crontab.Crontab;
import org.miaixz.bus.tempus.pattern.CronPattern;

/**
 * Task table for cron jobs. This class holds a mapping between task IDs, cron patterns, and the tasks themselves. The
 * scheduler periodically checks all tasks in this table to see if their patterns match the current time, and if so,
 * executes the corresponding task. Read-write locks are used to ensure thread safety for adding and removing tasks.
 *
 * @author Kimi Liu
 * @since Java 21+
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
