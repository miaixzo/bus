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
package org.miaixz.bus.cron.timings;

import org.miaixz.bus.cron.crontab.TimerCrontab;
import org.miaixz.bus.logger.Logger;

import java.util.function.Consumer;

/**
 * 多层时间轮，常用于延时任务 时间轮是一种环形数据结构，由多个槽组成，每个槽中存放任务集合 一个单独的线程推进时间一槽一槽的移动，并执行槽中的任务
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TimingWheel {

    /**
     * 一个时间槽的范围
     */
    private final long tickMs;

    /**
     * 时间轮大小，时间轮中时间槽的个数
     */
    private final int wheelSize;

    /**
     * 时间跨度，当前时间轮总间隔，即单个槽的跨度*槽个数
     */
    private final long interval;

    /**
     * 时间槽
     */
    private final TimerTaskList[] timerTaskLists;
    /**
     * 任务处理器
     */
    private final Consumer<TimerTaskList> consumer;
    /**
     * 当前时间，指向当前操作的时间格，代表当前时间
     */
    private long currentTime;
    /**
     * 上层时间轮
     */
    private volatile TimingWheel overflowWheel;

    /**
     * 构造
     *
     * @param tickMs    一个时间槽的范围，单位毫秒
     * @param wheelSize 时间轮大小
     * @param consumer  任务处理器
     */
    public TimingWheel(final long tickMs, final int wheelSize, final Consumer<TimerTaskList> consumer) {
        this(tickMs, wheelSize, System.currentTimeMillis(), consumer);
    }

    /**
     * 构造
     *
     * @param tickMs      一个时间槽的范围，单位毫秒
     * @param wheelSize   时间轮大小
     * @param currentTime 当前时间
     * @param consumer    任务处理器
     */
    public TimingWheel(final long tickMs, final int wheelSize, final long currentTime,
            final Consumer<TimerTaskList> consumer) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.interval = tickMs * wheelSize;
        this.timerTaskLists = new TimerTaskList[wheelSize];
        for (int i = 0; i < wheelSize; i++) {
            this.timerTaskLists[i] = new TimerTaskList();
        }

        // currentTime为tickMs的整数倍 这里做取整操作
        this.currentTime = currentTime - (currentTime % tickMs);
        this.consumer = consumer;
    }

    /**
     * 添加任务到时间轮
     *
     * @param timerCrontab 任务
     * @return 是否成功
     */
    public boolean addTask(final TimerCrontab timerCrontab) {
        final long expiration = timerCrontab.getDelayMs();
        // 过期任务直接执行
        if (expiration < currentTime + tickMs) {
            return false;
        } else if (expiration < currentTime + interval) {
            // 当前时间轮可以容纳该任务 加入时间槽
            final long virtualId = expiration / tickMs;
            final int index = (int) (virtualId % wheelSize);
            Logger.debug("tickMs: {} ------index: {} ------expiration: {}", tickMs, index, expiration);

            final TimerTaskList timerTaskList = timerTaskLists[index];
            timerTaskList.addTask(timerCrontab);
            if (timerTaskList.setExpiration(virtualId * tickMs)) {
                // 添加到delayQueue中
                consumer.accept(timerTaskList);
            }
        } else {
            // 放到上一层的时间轮
            final TimingWheel timeWheel = getOverflowWheel();
            timeWheel.addTask(timerCrontab);
        }
        return true;
    }

    /**
     * 推进时间
     *
     * @param timestamp 推进的时间
     */
    public void advanceClock(final long timestamp) {
        if (timestamp >= currentTime + tickMs) {
            currentTime = timestamp - (timestamp % tickMs);
            if (overflowWheel != null) {
                // 推进上层时间轮时间
                this.getOverflowWheel().advanceClock(timestamp);
            }
        }
    }

    /**
     * 创建或者获取上层时间轮
     */
    private TimingWheel getOverflowWheel() {
        if (overflowWheel == null) {
            synchronized (this) {
                if (overflowWheel == null) {
                    overflowWheel = new TimingWheel(interval, wheelSize, currentTime, consumer);
                }
            }
        }
        return overflowWheel;
    }

}
