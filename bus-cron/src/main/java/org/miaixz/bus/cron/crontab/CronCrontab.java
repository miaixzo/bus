/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.cron.pattern.CronPattern;

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
