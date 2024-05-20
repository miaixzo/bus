/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.cron.crontab;

import org.miaixz.bus.cron.pattern.CronPattern;

/**
 * 定时作业，此类除了定义了作业，也定义了作业的执行周期以及ID。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CronCrontab implements Crontab {

    private final String id;
    private final Crontab crontab;
    private CronPattern pattern;

    /**
     * 构造
     *
     * @param id      ID
     * @param pattern 表达式
     * @param crontab 作业
     */
    public CronCrontab(final String id, final CronPattern pattern, final Crontab crontab) {
        this.id = id;
        this.pattern = pattern;
        this.crontab = crontab;
    }

    @Override
    public void execute() {
        crontab.execute();
    }

    /**
     * 获取作业ID
     *
     * @return 作业ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取表达式
     *
     * @return 表达式
     */
    public CronPattern getPattern() {
        return pattern;
    }

    /**
     * 设置新的定时表达式
     *
     * @param pattern 表达式
     * @return this
     */
    public CronCrontab setPattern(final CronPattern pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * 获取原始作业
     *
     * @return 作业
     */
    public Crontab getRaw() {
        return this.crontab;
    }

}
