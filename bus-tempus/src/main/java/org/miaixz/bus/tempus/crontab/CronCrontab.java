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
package org.miaixz.bus.tempus.crontab;

import org.miaixz.bus.tempus.pattern.CronPattern;

/**
 * A scheduled job that encapsulates a task, its execution pattern, and its ID. This class holds not only the job to be
 * executed but also its scheduling information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CronCrontab implements Crontab {

    /**
     * The unique identifier for this task.
     */
    private final String id;
    /**
     * The underlying cron task to be executed.
     */
    private final Crontab crontab;
    /**
     * The cron pattern defining when this task should execute.
     */
    private CronPattern pattern;

    /**
     * Constructs a new CronCrontab.
     *
     * @param id      The unique identifier for the task.
     * @param pattern The cron pattern that defines the execution schedule.
     * @param crontab The task to be executed.
     */
    public CronCrontab(final String id, final CronPattern pattern, final Crontab crontab) {
        this.id = id;
        this.pattern = pattern;
        this.crontab = crontab;
    }

    /**
     * Executes the wrapped cron task.
     */
    @Override
    public void execute() {
        crontab.execute();
    }

    /**
     * Gets the unique identifier of this task.
     *
     * @return The task ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the cron pattern for this task.
     *
     * @return The {@link CronPattern}.
     */
    public CronPattern getPattern() {
        return pattern;
    }

    /**
     * Sets a new cron pattern for this task.
     *
     * @param pattern The new cron pattern.
     * @return this {@link CronCrontab} instance for chaining.
     */
    public CronCrontab setPattern(final CronPattern pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Gets the raw, underlying task.
     *
     * @return The original {@link Crontab} instance.
     */
    public Crontab getRaw() {
        return this.crontab;
    }

}
