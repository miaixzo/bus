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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.cron.crontab.CronCrontab;
import org.miaixz.bus.cron.crontab.Crontab;

/**
 * 作业执行管理器 负责管理作业的启动、停止等
 *
 * <p>
 * 此类用于管理正在运行的作业情况，作业启动后加入任务列表，任务结束移除
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Manager implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852293196122L;

    /**
     * 执行器列表
     */
    private final List<Executor> executors = new ArrayList<>();
    protected Scheduler scheduler;

    /**
     * 构造
     *
     * @param scheduler {@link Scheduler}
     */
    public Manager(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 获取所有正在执行的任务调度执行器
     *
     * @return 任务执行器列表
     */
    public List<Executor> getExecutors() {
        return Collections.unmodifiableList(this.executors);
    }

    /**
     * 启动 执行器TaskExecutor，即启动作业
     *
     * @param task {@link Crontab}
     * @return {@link Executor}
     */
    public Executor spawnExecutor(final CronCrontab task) {
        final Executor executor = new Executor(this.scheduler, task);
        synchronized (this.executors) {
            this.executors.add(executor);
        }
        this.scheduler.threadExecutor.execute(executor);
        return executor;
    }

    /**
     * 执行器执行完毕调用此方法，将执行器从执行器列表移除，此方法由{@link Executor}对象调用，用于通知管理器自身已完成执行
     *
     * @param executor 执行器 {@link Executor}
     * @return this
     */
    public Manager notifyExecutorCompleted(final Executor executor) {
        synchronized (executors) {
            executors.remove(executor);
        }
        return this;
    }

}
