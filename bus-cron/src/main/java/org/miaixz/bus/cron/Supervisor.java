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
import java.util.List;

/**
 * 作业启动管理器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Supervisor implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852293708939L;
    /**
     * 启动器列表
     */
    protected final List<Launcher> launchers = new ArrayList<>();
    protected Scheduler scheduler;

    /**
     * 构造
     *
     * @param scheduler {@link Scheduler}
     */
    public Supervisor(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 启动 Launcher
     *
     * @param millis 触发事件的毫秒数
     * @return {@link Launcher}
     */
    protected Launcher spawnLauncher(final long millis) {
        final Launcher launcher = new Launcher(this.scheduler, millis);
        synchronized (this.launchers) {
            this.launchers.add(launcher);
        }
        this.scheduler.threadExecutor.execute(launcher);
        return launcher;
    }

    /**
     * 启动器启动完毕，启动完毕后从执行器列表中移除
     *
     * @param launcher 启动器 {@link Launcher}
     */
    protected void notifyLauncherCompleted(final Launcher launcher) {
        synchronized (launchers) {
            launchers.remove(launcher);
        }
    }

}
