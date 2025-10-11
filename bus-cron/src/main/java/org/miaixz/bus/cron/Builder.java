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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.cron.crontab.Crontab;
import org.miaixz.bus.cron.pattern.CronPattern;
import org.miaixz.bus.cron.pattern.parser.PatternParser;
import org.miaixz.bus.setting.Setting;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A tool class for scheduled tasks. This tool holds a global {@link Scheduler}, and all scheduled tasks are executed in
 * the same scheduler. The {@link #setMatchSecond(boolean)} method is used to define whether to use the second matching
 * mode. If true, the first digit in the scheduled task expression is the second, otherwise it is the minute (default).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Builder {

    /**
     * The path to the Crontab configuration file.
     */
    public static final String CRONTAB_CONFIG_PATH = "config/cron.setting";
    /**
     * An alternative path to the Crontab configuration file.
     */
    public static final String CRONTAB_CONFIG_PATH2 = "cron.setting";

    private static final Lock lock = new ReentrantLock();
    private static final Scheduler scheduler = new Scheduler();
    private static Setting crontabSetting;

    /**
     * Sets a custom scheduled task configuration file.
     *
     * @param cronSetting The scheduled task configuration file.
     */
    public static void setCronSetting(final Setting cronSetting) {
        crontabSetting = cronSetting;
    }

    /**
     * Sets a custom scheduled task configuration file path.
     *
     * @param cronSettingPath The path to the scheduled task configuration file (can be relative or absolute).
     */
    public static void setCronSetting(final String cronSettingPath) {
        try {
            crontabSetting = new Setting(cronSettingPath, Setting.DEFAULT_CHARSET, false);
        } catch (final InternalException e) {
            // ignore setting file parse error and no config error
        }
    }

    /**
     * Sets whether to support second matching. This method is used to define whether to use the second matching mode.
     * If true, the first digit in the scheduled task expression is the second, otherwise it is the minute (default).
     *
     * @param isMatchSecond {@code true} to support, {@code false} to not support.
     */
    public static void setMatchSecond(final boolean isMatchSecond) {
        scheduler.setMatchSecond(isMatchSecond);
    }

    /**
     * Adds a scheduled task.
     *
     * @param schedulingPattern The crontab expression for the execution time of the scheduled task.
     * @param crontab           The task to be executed.
     * @return The ID of the scheduled task.
     */
    public static String schedule(final String schedulingPattern, final Crontab crontab) {
        return scheduler.schedule(schedulingPattern, crontab);
    }

    /**
     * Adds a scheduled task with a specified ID.
     *
     * @param id                The ID of the scheduled task.
     * @param schedulingPattern The crontab expression for the execution time of the scheduled task.
     * @param crontab           The task to be executed.
     * @return The ID of the scheduled task.
     */
    public static String schedule(final String id, final String schedulingPattern, final Crontab crontab) {
        scheduler.schedule(id, schedulingPattern, crontab);
        return id;
    }

    /**
     * Batch adds scheduled tasks from a configuration file.
     *
     * @param cronSetting The scheduled task settings file.
     */
    public static void schedule(final Setting cronSetting) {
        scheduler.schedule(cronSetting);
    }

    /**
     * Removes a task.
     *
     * @param schedulerId The ID of the task.
     * @return {@code true} if the task was successfully removed, {@code false} if the task with the corresponding ID
     *         was not found.
     */
    public static boolean remove(final String schedulerId) {
        return scheduler.descheduleWithStatus(schedulerId);
    }

    /**
     * Updates the execution time rule of a task.
     *
     * @param id      The ID of the task.
     * @param pattern The new {@link CronPattern}.
     */
    public static void updatePattern(final String id, final CronPattern pattern) {
        scheduler.updatePattern(id, pattern);
    }

    /**
     * Gets the Scheduler object.
     *
     * @return The Scheduler object.
     */
    public static Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Starts the scheduler in non-daemon mode.
     *
     * @see #start(boolean)
     */
    public static void start() {
        start(false);
    }

    /**
     * Starts the scheduler.
     *
     * @param isDaemon Whether to start as a daemon thread. If true, the scheduled tasks being executed will terminate
     *                 immediately after calling the {@link #stop()} method, otherwise they will wait for completion.
     */
    synchronized public static void start(final boolean isDaemon) {
        if (scheduler.isStarted()) {
            throw new InternalException("Scheduler has been started, please stop it first!");
        }

        lock.lock();
        try {
            if (null == crontabSetting) {
                // Try to find config/cron.setting
                setCronSetting(CRONTAB_CONFIG_PATH);
            }
            // Try to find cron.setting
            if (null == crontabSetting) {
                setCronSetting(CRONTAB_CONFIG_PATH2);
            }
        } finally {
            lock.unlock();
        }

        schedule(crontabSetting);
        scheduler.start(isDaemon);
    }

    /**
     * Restarts the scheduled tasks. This method will clear dynamically loaded tasks. After restarting, whether it is a
     * daemon thread or not will remain the same as before.
     */
    public static void restart() {
        lock.lock();
        try {
            if (null != crontabSetting) {
                // Reload the configuration file
                crontabSetting.load();
            }
            if (scheduler.isStarted()) {
                // Stop and clear existing tasks
                stop();
            }
        } finally {
            lock.unlock();
        }

        // Reload tasks
        schedule(crontabSetting);
        // Restart
        scheduler.start();
    }

    /**
     * Stops the scheduler.
     */
    public static void stop() {
        scheduler.stop(true);
    }

    /**
     * Validates if the given expression is a valid Cron expression.
     *
     * @param expression The expression to validate.
     * @return {@code true} if the expression is valid, {@code false} otherwise.
     */
    public static boolean isValidExpression(String expression) {
        if (expression == null) {
            return false;
        } else {
            try {
                PatternParser.parse(expression);
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
    }

}
