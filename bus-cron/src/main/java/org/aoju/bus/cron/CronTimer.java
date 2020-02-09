/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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

import org.aoju.bus.core.lang.Fields;
import org.aoju.bus.core.utils.ThreadUtils;

/**
 * 定时任务计时器
 * 计时器线程每隔一分钟检查一次任务列表,一旦匹配到执行对应的Task
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8+
 */
public class CronTimer extends Thread {

    /**
     * 定时单元：秒
     */
    private long TIMER_UNIT_SECOND = Fields.Unit.SECOND.getMillis();
    /**
     * 定时单元：分
     */
    private long TIMER_UNIT_MINUTE = Fields.Unit.MINUTE.getMillis();

    /**
     * 定时任务是否已经被强制关闭
     */
    private boolean isStoped;
    private Scheduler scheduler;

    /**
     * 构造
     *
     * @param scheduler {@link Scheduler}
     */
    public CronTimer(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        final long timerUnit = this.scheduler.matchSecond ? TIMER_UNIT_SECOND : TIMER_UNIT_MINUTE;

        long thisTime = System.currentTimeMillis();
        long nextTime;
        long sleep;
        while (false == isStoped) {
            //下一时间计算是按照上一个执行点开始时间计算的
            nextTime = ((thisTime / timerUnit) + 1) * timerUnit;
            sleep = nextTime - System.currentTimeMillis();
            if (sleep > 0 && false == ThreadUtils.safeSleep(sleep)) {
                //等待直到下一个时间点,如果被中断直接退出Timer
                break;
            }

            //执行点,时间记录为执行开始的时间,而非结束时间
            thisTime = System.currentTimeMillis();
            spawnLauncher(thisTime);
        }
    }

    /**
     * 关闭定时器
     */
    synchronized public void stopTimer() {
        this.isStoped = true;
        ThreadUtils.interupt(this, true);
    }

    /**
     * 启动匹配
     *
     * @param millis 当前时间
     */
    private void spawnLauncher(final long millis) {
        this.scheduler.launcherManager.spawnLauncher(millis);
    }
}
