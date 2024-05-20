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
package org.miaixz.bus.cron.timings;

import org.miaixz.bus.core.toolkit.ThreadKit;
import org.miaixz.bus.cron.crontab.TimerCrontab;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 系统计时器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SystemTimer {

    /**
     * 底层时间轮
     */
    private final TimingWheel timeWheel;

    /**
     * 一个Timer只有一个delayQueue
     */
    private final DelayQueue<TimerTaskList> delayQueue = new DelayQueue<>();

    /**
     * 执行队列取元素超时时长，单位毫秒，默认100
     */
    private long delayQueueTimeout = 100;

    /**
     * 轮询delayQueue获取过期任务线程
     */
    private ExecutorService bossThreadPool;
    private volatile boolean isRunning;

    /**
     * 构造
     */
    public SystemTimer() {
        timeWheel = new TimingWheel(1, 20, delayQueue::offer);
    }

    /**
     * 设置执行队列取元素超时时长，单位毫秒
     *
     * @param delayQueueTimeout 执行队列取元素超时时长，单位毫秒
     * @return this
     */
    public SystemTimer setDelayQueueTimeout(final long delayQueueTimeout) {
        this.delayQueueTimeout = delayQueueTimeout;
        return this;
    }

    /**
     * 启动，异步
     *
     * @return this
     */
    public SystemTimer start() {
        bossThreadPool = ThreadKit.newSingleExecutor();
        isRunning = true;
        bossThreadPool.submit(() -> {
            while (true) {
                if (!advanceClock()) {
                    break;
                }
            }
        });
        return this;
    }

    /**
     * 强制结束
     */
    public void stop() {
        this.isRunning = false;
        this.bossThreadPool.shutdown();
    }

    /**
     * 添加任务
     *
     * @param timerCrontab 任务
     */
    public void addTask(final TimerCrontab timerCrontab) {
        //添加失败任务直接执行
        if (!timeWheel.addTask(timerCrontab)) {
            ThreadKit.execAsync(timerCrontab.getTask());
        }
    }

    /**
     * 指针前进并获取过期任务
     *
     * @return 是否结束
     */
    private boolean advanceClock() {
        if (!isRunning) {
            return false;
        }
        try {
            final TimerTaskList timerTaskList = poll();
            if (null != timerTaskList) {
                //推进时间
                timeWheel.advanceClock(timerTaskList.getExpire());
                //执行过期任务（包含降级操作）
                timerTaskList.flush(this::addTask);
            }
        } catch (final InterruptedException ignore) {
            return false;
        }
        return true;
    }

    /**
     * 执行队列取任务列表
     *
     * @return 任务列表
     * @throws InterruptedException 中断异常
     */
    private TimerTaskList poll() throws InterruptedException {
        return this.delayQueueTimeout > 0 ?
                delayQueue.poll(delayQueueTimeout, TimeUnit.MILLISECONDS) :
                delayQueue.poll();
    }

}
