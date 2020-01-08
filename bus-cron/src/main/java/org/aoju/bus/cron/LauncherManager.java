/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.cron;

import java.util.ArrayList;
import java.util.List;

/**
 * 作业启动管理器
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
public class LauncherManager {

    protected Scheduler scheduler;
    /**
     * 启动器列表
     */
    protected List<TaskLauncher> launchers = new ArrayList<>();

    public LauncherManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 启动 TaskLauncher
     *
     * @param millis 触发事件的毫秒数
     * @return {@link TaskLauncher}
     */
    protected TaskLauncher spawnLauncher(long millis) {
        final TaskLauncher launcher = new TaskLauncher(this.scheduler, millis);
        synchronized (this.launchers) {
            this.launchers.add(launcher);
        }
        //子线程是否为deamon线程取决于父线程,因此此处无需显示调用
        //launcher.setDaemon(this.scheduler.daemon);
//		launcher.start();
        this.scheduler.threadExecutor.execute(launcher);
        return launcher;
    }

    /**
     * 启动器启动完毕,启动完毕后从执行器列表中移除
     *
     * @param launcher 启动器 {@link TaskLauncher}
     */
    protected void notifyLauncherCompleted(TaskLauncher launcher) {
        synchronized (launchers) {
            launchers.remove(launcher);
        }
    }

}
