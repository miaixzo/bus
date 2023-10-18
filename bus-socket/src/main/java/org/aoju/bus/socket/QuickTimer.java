/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org sandao and other contributors.               *
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
package org.aoju.bus.socket;

import org.aoju.bus.logger.Logger;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 服务器定时任务
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class QuickTimer implements Runnable {

    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1, r -> {
        Thread thread = new Thread(r, "Quick Timer");
        thread.setDaemon(true);
        return thread;
    });

    public QuickTimer() {
        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(this, getDelay(), getPeriod(), TimeUnit.MILLISECONDS);
        Logger.info("Register QuickTimer---- " + this.getClass().getSimpleName());
    }

    public static void cancelQuickTask() {
        SCHEDULED_EXECUTOR_SERVICE.shutdown();
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period) {
        return SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(command, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取定时任务的延迟启动时间
     *
     * @return the long
     */
    protected long getDelay() {
        return 0;
    }

    /**
     * 获取定时任务的执行频率
     *
     * @return the long
     */
    protected abstract long getPeriod();

}
