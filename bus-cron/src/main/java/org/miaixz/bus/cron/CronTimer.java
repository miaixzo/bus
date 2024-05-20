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
package org.miaixz.bus.cron;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.logger.Logger;

import java.io.Serializable;

/**
 * 定时任务计时器
 * 计时器线程每隔一分钟（一秒钟）检查一次任务列表，一旦匹配到执行对应的Task
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CronTimer extends Thread implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 定时单元：秒
     */
    private final long TIMER_UNIT_SECOND = Fields.Units.SECOND.getMillis();
    /**
     * 定时单元：分
     */
    private final long TIMER_UNIT_MINUTE = Fields.Units.MINUTE.getMillis();
    private final Scheduler scheduler;
    /**
     * 定时任务是否已经被强制关闭
     */
    private boolean isStop;

    /**
     * 构造
     *
     * @param scheduler {@link Scheduler}
     */
    public CronTimer(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 检查是否为有效的sleep毫秒数，包括：
     * <pre>
     *     1. 是否&gt;0，防止用户向未来调整时间
     *     1. 是否&lt;两倍的间隔单位，防止用户向历史调整时间
     * </pre>
     *
     * @param millis    毫秒数
     * @param timerUnit 定时单位，为秒或者分的毫秒值
     * @return 是否为有效的sleep毫秒数
     */
    private static boolean isValidSleepMillis(final long millis, final long timerUnit) {
        return millis > 0 &&
                // 防止用户向前调整时间导致的长时间sleep
                millis < (2 * timerUnit);
    }

    @Override
    public void run() {
        final long timerUnit = this.scheduler.config.matchSecond ? TIMER_UNIT_SECOND : TIMER_UNIT_MINUTE;

        long thisTime = System.currentTimeMillis();
        long nextTime;
        long sleep;
        while (!isStop) {
            // 下一时间计算是按照上一个执行点开始时间计算的
            // 此处除以定时单位是为了清零单位以下部分，例如单位是分则秒和毫秒清零
            nextTime = ((thisTime / timerUnit) + 1) * timerUnit;
            sleep = nextTime - System.currentTimeMillis();
            if (isValidSleepMillis(sleep, timerUnit)) {
                if (!ThreadKit.safeSleep(sleep)) {
                    // 等待直到下一个时间点，如果被中断直接退出Timer
                    break;
                }

                // 执行点，时间记录为执行开始的时间，而非结束时间
                spawnLauncher(nextTime);

                // 采用叠加方式，确保正好是1分钟或1秒，避免sleep晚醒问题
                // 此处无需校验，因为每次循环都是sleep与上触发点的时间差。
                // 当上一次晚醒后，本次会减少sleep时间，保证误差在一个unit内，并不断修正。
                thisTime = nextTime;
            } else {
                // 非正常时间重新计算
                thisTime = System.currentTimeMillis();
            }
        }
        Logger.debug("cron timer stopped.");
    }

    /**
     * 关闭定时器
     */
    synchronized public void stopTimer() {
        this.isStop = true;
        ThreadKit.interrupt(this, true);
    }

    /**
     * 启动匹配
     *
     * @param millis 当前时间
     */
    private void spawnLauncher(final long millis) {
        this.scheduler.supervisor.spawnLauncher(millis);
    }

}
