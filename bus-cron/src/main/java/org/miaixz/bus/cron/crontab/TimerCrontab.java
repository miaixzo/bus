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
package org.miaixz.bus.cron.crontab;

import org.miaixz.bus.cron.timings.TimerTaskList;

/**
 * 延迟任务
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TimerCrontab {

    /**
     * 延迟时间
     */
    private final long delayMs;

    /**
     * 任务
     */
    private final Runnable task;
    /**
     * 任务描述
     */
    public String desc;
    /**
     * 时间槽
     */
    public TimerTaskList timerTaskList;
    /**
     * 下一个节点
     */
    public TimerCrontab next;
    /**
     * 上一个节点
     */
    public TimerCrontab prev;

    /**
     * 构造
     *
     * @param task    任务
     * @param delayMs 延迟毫秒数（以当前时间为准）
     */
    public TimerCrontab(final Runnable task, final long delayMs) {
        this.delayMs = System.currentTimeMillis() + delayMs;
        this.task = task;
    }

    /**
     * 获取任务
     *
     * @return 任务
     */
    public Runnable getTask() {
        return task;
    }

    /**
     * 获取延迟时间点，即创建时间+延迟时长（单位毫秒）
     *
     * @return 延迟时间点
     */
    public long getDelayMs() {
        return delayMs;
    }

    @Override
    public String toString() {
        return desc;
    }

}
