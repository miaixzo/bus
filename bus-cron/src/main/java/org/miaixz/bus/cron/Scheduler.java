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
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.lang.thread.ExecutorBuilder;
import org.miaixz.bus.core.lang.thread.ThreadFactoryBuilder;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cron.crontab.Crontab;
import org.miaixz.bus.cron.crontab.CrontabFactory;
import org.miaixz.bus.cron.crontab.InvokeCrontab;
import org.miaixz.bus.cron.crontab.RunnableCrontab;
import org.miaixz.bus.cron.listener.TaskListener;
import org.miaixz.bus.cron.listener.TaskListenerManager;
import org.miaixz.bus.cron.pattern.CronPattern;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.Setting;

/**
 * Task scheduler.
 * <p>
 * The scheduler startup process is as follows:
 *
 * <pre>
 * Start Timer -> Start TaskLauncher -> Start TaskExecutor
 * </pre>
 * <p>
 * The scheduler shutdown process is as follows:
 *
 * <pre>
 * Stop Timer -> Stop all running TaskLaunchers -> Stop all running TaskExecutors
 * </pre>
 *
 * Where:
 *
 * <pre>
 * Launcher: Called by the timer every minute (or every second if {@link Configure#isMatchSecond()} is {@code
 * true
 * }),
 * responsible for checking if the <strong>Repertoire</strong> has any tasks that match the current time to run.
 * </pre>
 *
 * <pre>
 * Executor: Triggered by the TaskLauncher upon a successful match, executes the specific job, and is destroyed upon completion.
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Scheduler implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852287508206L;

    /**
     * Scheduled task configuration
     */
    public final Configure config;
    /**
     * Timer
     */
    private CronTimer timer;
    /**
     * Scheduled task table
     */
    public Repertoire repertoire;
    /**
     * Thread pool for executing TaskLauncher and TaskExecutor
     */
    public ExecutorService threadExecutor;
    /**
     * Task manager
     */
    public Manager manager;
    /**
     * Listener manager list
     */
    public TaskListenerManager listenerManager;

    /**
     * Lock for scheduled tasks, used to synchronize add and delete operations
     */
    private final Lock lock;

    /**
     * Whether it has been started
     */
    private boolean started;

    /**
     * Sets the configure.
     */
    public Scheduler() {
        this(Configure.of());
    }

    /**
     * Sets the configure.
     *
     * @param config The Configure.
     */
    public Scheduler(final Configure config) {
        this.config = config;
        this.lock = new ReentrantLock();
        this.listenerManager = new TaskListenerManager();
        this.clear();
    }

    /**
     * Sets a custom thread pool. When using a custom thread pool, consider whether the execution threads should be
     * daemon threads.
     *
     * @param threadExecutor The custom thread pool.
     * @return this {@link Scheduler} instance.
     * @throws CrontabException if the scheduler is already started.
     */
    public Scheduler setThreadExecutor(final ExecutorService threadExecutor) throws CrontabException {
        lock.lock();
        try {
            checkStarted();
            this.threadExecutor = threadExecutor;
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * Sets whether to support second matching in cron expressions. Defaults to false.
     *
     * @param isMatchSecond {@code true} to enable, {@code false} to disable.
     * @return this {@link Scheduler} instance.
     */
    public Scheduler setMatchSecond(final boolean isMatchSecond) {
        lock.lock();
        try {
            checkStarted();
            this.config.setMatchSecond(isMatchSecond);
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * Adds a task listener.
     *
     * @param listener The {@link TaskListener} to add.
     * @return this {@link Scheduler} instance.
     */
    public Scheduler addListener(final TaskListener listener) {
        lock.lock();
        try {
            this.listenerManager.addListener(listener);
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * Removes a task listener.
     *
     * @param listener The {@link TaskListener} to remove.
     * @return this {@link Scheduler} instance.
     */
    public Scheduler removeListener(final TaskListener listener) {
        lock.lock();
        try {
            this.listenerManager.removeListener(listener);
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * Schedules tasks from a {@link Setting} configuration file. The configuration format is:
     * {@code com.example.MyClass.myMethod = * * * * *}.
     *
     * @param cronSetting The configuration file.
     * @return this {@link Scheduler} instance.
     */
    public Scheduler schedule(final Setting cronSetting) {
        if (MapKit.isNotEmpty(cronSetting)) {
            String group;
            for (final Entry<String, LinkedHashMap<String, String>> groupedEntry : cronSetting.getGroupedMap()
                    .entrySet()) {
                group = groupedEntry.getKey();
                for (final Entry<String, String> entry : groupedEntry.getValue().entrySet()) {
                    String jobClass = entry.getKey();
                    if (StringKit.isNotBlank(group)) {
                        jobClass = group + Symbol.C_DOT + jobClass;
                    }
                    final String pattern = entry.getValue();
                    Logger.debug("Load job: {} {}", pattern, jobClass);
                    try {
                        // Use a custom ID to avoid duplicates when reloading from the config file.
                        schedule("id_" + jobClass, pattern, new InvokeCrontab(jobClass));
                    } catch (final Exception e) {
                        throw new CrontabException("Schedule [{}] [{}] error!", pattern, jobClass);
                    }
                }
            }
        }
        return this;
    }

    /**
     * Schedules a task with a randomly generated UUID.
     *
     * @param pattern The cron expression string.
     * @param crontab The {@link Crontab} task.
     * @return The generated task ID.
     */
    public String schedule(final String pattern, final Crontab crontab) {
        final String id = ID.objectId();
        schedule(id, pattern, crontab);
        return id;
    }

    /**
     * Schedules a {@link Runnable} task with a specified ID.
     *
     * @param id      The unique ID for the task.
     * @param pattern The cron expression string.
     * @param task    The {@link Runnable} to execute.
     * @return this {@link Scheduler} instance.
     * @throws CrontabException if a task with the same ID already exists.
     */
    public Scheduler schedule(final String id, final String pattern, final Runnable task) {
        return schedule(id, new CronPattern(pattern), new RunnableCrontab(task));
    }

    /**
     * Schedules a {@link Crontab} task with a specified ID.
     *
     * @param id      The unique ID for the task.
     * @param pattern The cron expression string.
     * @param crontab The {@link Crontab} task.
     * @return this {@link Scheduler} instance.
     * @throws CrontabException if a task with the same ID already exists.
     */
    public Scheduler schedule(final String id, final String pattern, final Crontab crontab) {
        return schedule(id, new CronPattern(pattern), crontab);
    }

    /**
     * Schedules a {@link Crontab} task with a specified ID and {@link CronPattern}.
     *
     * @param id      The unique ID for the task.
     * @param pattern The {@link CronPattern}.
     * @param crontab The {@link Crontab} task.
     * @return this {@link Scheduler} instance.
     * @throws CrontabException if a task with the same ID already exists.
     */
    public Scheduler schedule(final String id, final CronPattern pattern, final Crontab crontab) {
        repertoire.add(id, pattern, crontab);
        return this;
    }

    /**
     * Removes a task by its ID.
     *
     * @param id The ID of the task to remove.
     * @return this {@link Scheduler} instance.
     */
    public Scheduler deschedule(final String id) {
        descheduleWithStatus(id);
        return this;
    }

    /**
     * Removes a task by its ID and returns whether the removal was successful.
     *
     * @param id The ID of the task to remove.
     * @return {@code true} if the task was found and removed, {@code false} otherwise.
     */
    public boolean descheduleWithStatus(final String id) {
        return this.repertoire.remove(id);
    }

    /**
     * Updates the cron pattern for an existing task.
     *
     * @param id      The ID of the task to update.
     * @param pattern The new {@link CronPattern}.
     * @return this {@link Scheduler} instance.
     */
    public Scheduler updatePattern(final String id, final CronPattern pattern) {
        this.repertoire.updatePattern(id, pattern);
        return this;
    }

    /**
     * Gets the task table. Note: This returns a direct reference, not a copy. Modifications to the returned object will
     * affect the scheduler.
     *
     * @return The task table ({@link Repertoire}).
     */
    public Repertoire getRepertoire() {
        return this.repertoire;
    }

    /**
     * Gets the {@link CronPattern} for a given task ID.
     *
     * @param id The task ID.
     * @return The {@link CronPattern}, or {@code null} if not found.
     */
    public CronPattern getPattern(final String id) {
        return this.repertoire.getPattern(id);
    }

    /**
     * Gets the {@link Crontab} for a given task ID.
     *
     * @param id The task ID.
     * @return The {@link Crontab}, or {@code null} if not found.
     */
    public Crontab getTask(final String id) {
        return this.repertoire.getTask(id);
    }

    /**
     * Checks if the scheduler has any tasks.
     *
     * @return {@code true} if there are no tasks, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return this.repertoire.isEmpty();
    }

    /**
     * Gets the number of scheduled tasks.
     *
     * @return The number of tasks.
     */
    public int size() {
        return this.repertoire.size();
    }

    /**
     * Clears all tasks from the scheduler.
     *
     * @return this {@link Scheduler} instance.
     */
    public Scheduler clear() {
        this.repertoire = CrontabFactory.create(this.config);
        return this;
    }

    /**
     * Checks if the scheduler is started.
     *
     * @return {@code true} if started, {@code false} otherwise.
     */
    public boolean isStarted() {
        return this.started;
    }

    /**
     * Starts the scheduler.
     *
     * @param isDaemon Whether to run as a daemon thread. If true, tasks will be terminated immediately when
     *                 {@link #stop()} is called; otherwise, they will run to completion.
     * @return this {@link Scheduler} instance.
     */
    public Scheduler start(final boolean isDaemon) {
        this.config.setDaemon(isDaemon);
        return start();
    }

    /**
     * Start
     *
     * @return this
     */
    public Scheduler start() {
        final boolean daemon = this.config.isDaemon();

        lock.lock();
        try {
            checkStarted();

            if (null == this.threadExecutor) {
                // Use an unbounded thread pool to ensure every task can run promptly,
                // while reusing existing threads to avoid repeated creation.
                this.threadExecutor = ExecutorBuilder.of().useSynchronousQueue().setThreadFactory(//
                        ThreadFactoryBuilder.of().setNamePrefix("x-cron-").setDaemon(daemon).build()//
                ).build();
            }
            this.manager = new Manager(this);

            // Start CronTimer
            timer = new CronTimer(this);
            timer.setDaemon(daemon);
            timer.start();
            this.started = true;
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * Stops the scheduler. This will immediately terminate the timer thread. If running in daemon mode, currently
     * executing jobs will also be terminated; otherwise, they will run to completion. This method does not clear the
     * task list. Use {@link #clear()} or {@link #stop(boolean)} to clear tasks.
     *
     * @return this {@link Scheduler} instance.
     */
    public Scheduler stop() {
        return stop(false);
    }

    /**
     * Stops the scheduler. This will immediately terminate the timer thread. If running in daemon mode, currently
     * executing jobs will also be terminated; otherwise, they will run to completion.
     *
     * @param clearTasks Whether to clear all tasks from the task list.
     * @return this {@link Scheduler} instance.
     */
    public Scheduler stop(final boolean clearTasks) {
        lock.lock();
        try {
            if (!started) {
                throw new IllegalStateException("Scheduler not started !");
            }

            // Stop CronTimer
            this.timer.stopTimer();
            this.timer = null;

            // Shutdown thread pool
            this.threadExecutor.shutdown();
            this.threadExecutor = null;

            // Optionally clear the task table
            if (clearTasks) {
                clear();
            }

            // Update started flag
            started = false;
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * Executes scheduled tasks from the task table that match the timestamp
     *
     * @param millis Millisecond timestamp
     */
    public void execute(final long millis) {
        this.repertoire.execute(this, millis);
    }

    /**
     * Checks if the scheduler has already been started.
     *
     * @throws CrontabException if the scheduler is already started.
     */
    private void checkStarted() throws CrontabException {
        if (this.started) {
            throw new CrontabException("Scheduler already started!");
        }
    }

}
