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

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.tempus.crontab.Crontab;
import org.miaixz.bus.tempus.pattern.CronPattern;
import org.miaixz.bus.tempus.pattern.parser.PatternParser;
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
    public static final String CRONTAB_CONFIG_PATH = "config/tempus.setting";
    /**
     * An alternative path to the Crontab configuration file.
     */
    public static final String CRONTAB_CONFIG_PATH2 = "tempus.setting";

    /**
     * Lock for thread-safe operations on the scheduler.
     */
    private static final Lock lock = new ReentrantLock();
    /**
     * The global scheduler instance for all cron tasks.
     */
    private static final Scheduler scheduler = new Scheduler();
    /**
     * The cron setting configuration.
     */
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
                // Try to find config/tempus.setting
                setCronSetting(CRONTAB_CONFIG_PATH);
            }
            // Try to find tempus.setting
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
