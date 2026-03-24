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

import java.util.TimeZone;

/**
 * Cron task configuration.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Configure {

    /**
     * Time zone.
     */
    private TimeZone timezone = TimeZone.getDefault();
    /**
     * Whether to support second matching.
     */
    private boolean matchSecond;
    /**
     * Whether to use daemon thread.
     */
    private boolean daemon;
    /**
     * Whether to use trigger queue.
     */
    private boolean useTriggerQueue;

    /**
     * Default constructor.
     */
    public Configure() {

    }

    /**
     * Creates Cron configuration.
     *
     * @return Cron configuration.
     */
    public static Configure of() {
        return new Configure();
    }

    /**
     * Gets the time zone.
     *
     * @return The time zone, defaulting to {@link TimeZone#getDefault()}.
     */
    public TimeZone getTimeZone() {
        return this.timezone;
    }

    /**
     * Sets the time zone.
     *
     * @param timezone The time zone.
     * @return this {@link Configure} instance for chaining.
     */
    public Configure setTimeZone(final TimeZone timezone) {
        this.timezone = timezone;
        return this;
    }

    /**
     * Checks if second matching is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public boolean isMatchSecond() {
        return this.matchSecond;
    }

    /**
     * Sets whether to support second matching in cron expressions. Defaults to false.
     *
     * @param isMatchSecond {@code true} to enable, {@code false} to disable.
     * @return this {@link Configure} instance for chaining.
     */
    public Configure setMatchSecond(final boolean isMatchSecond) {
        this.matchSecond = isMatchSecond;
        return this;
    }

    /**
     * Checks if the thread is a daemon thread.
     *
     * @return {@code true} if daemon thread, {@code false} if non-daemon thread
     */
    public boolean isDaemon() {
        return this.daemon;
    }

    /**
     * Sets whether to use a daemon thread.
     *
     * @param daemon {@code true} for daemon thread, {@code false} for non-daemon thread
     * @return this {@link Configure} instance for chaining.
     */
    public Configure setDaemon(final boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    /**
     * Whether to use trigger queue
     *
     * @return {@code true} if used, {@code false} if not used
     */
    public boolean isUseTriggerQueue() {
        return this.useTriggerQueue;
    }

    /**
     * Sets whether to use trigger queue<br>
     * {@code true} uses the trigger queue method, which pre-adds the next trigger time of tasks to the queue. When the
     * trigger time of tasks in the queue is less than the current time, they are taken out of the queue and
     * executed.<br>
     * {@code false} uses the normal trigger method, which checks the task table. When the expression in the task table
     * matches the specified time, the corresponding Task is executed.
     *
     * @param useTriggerQueue {@code true} to use, {@code false} not to use
     * @return this
     */
    public Configure setUseTriggerQueue(final boolean useTriggerQueue) {
        this.useTriggerQueue = useTriggerQueue;
        return this;
    }

}
