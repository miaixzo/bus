/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.cron;

import org.aoju.bus.cron.factory.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * 作业执行管理器
 * 负责管理作业的启动、停止等
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class ExecutorManager {

    protected Scheduler scheduler;
    /**
     * 执行器列表
     */
    private List<TaskExecutor> executors = new ArrayList<>();

    public ExecutorManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 启动 TaskExecutor
     *
     * @param task {@link Task}
     * @return {@link TaskExecutor}
     */
    public TaskExecutor spawnExecutor(Task task) {
        final TaskExecutor executor = new TaskExecutor(this.scheduler, task);
        synchronized (this.executors) {
            this.executors.add(executor);
        }
        // 子线程是否为deamon线程取决于父线程,因此此处无需显示调用
        // executor.setDaemon(this.scheduler.daemon);
//		executor.start();
        this.scheduler.threadExecutor.execute(executor);
        return executor;
    }

    /**
     * 执行器执行完毕调用此方法,将执行器从执行器列表移除
     *
     * @param executor 执行器 {@link TaskExecutor}
     * @return this
     */
    public ExecutorManager notifyExecutorCompleted(TaskExecutor executor) {
        synchronized (executors) {
            executors.remove(executor);
        }
        return this;
    }

}
