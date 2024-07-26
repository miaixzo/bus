/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.cron.crontab.CronCrontab;
import org.miaixz.bus.cron.crontab.Crontab;

/**
 * 作业执行器 执行具体的作业，执行完毕销毁 作业执行器唯一关联一个作业，负责管理作业的运行的生命周期。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Executor implements Runnable {

    private final Scheduler scheduler;
    private final CronCrontab task;

    /**
     * 构造
     *
     * @param scheduler 调度器
     * @param task      被执行的任务
     */
    public Executor(final Scheduler scheduler, final CronCrontab task) {
        this.scheduler = scheduler;
        this.task = task;
    }

    /**
     * 获得原始任务对象
     *
     * @return 任务对象
     */
    public Crontab getTask() {
        return this.task.getRaw();
    }

    /**
     * 获得原始任务对象
     *
     * @return 任务对象
     */
    public CronCrontab getCronTask() {
        return this.task;
    }

    @Override
    public void run() {
        try {
            scheduler.listenerManager.notifyTaskStart(this);
            task.execute();
            scheduler.listenerManager.notifyTaskSucceeded(this);
        } catch (final Exception e) {
            scheduler.listenerManager.notifyTaskFailed(this, e);
        } finally {
            scheduler.manager.notifyExecutorCompleted(this);
        }
    }

}
