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

import org.miaixz.bus.cron.crontab.TimerCrontab;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 任务队列，任务双向链表
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TimerTaskList implements Delayed {

    /**
     * 过期时间
     */
    private final AtomicLong expire;

    /**
     * 根节点
     */
    private final TimerCrontab root;

    /**
     * 构造
     */
    public TimerTaskList() {
        expire = new AtomicLong(-1L);

        root = new TimerCrontab(null, -1L);
        root.prev = root;
        root.next = root;
    }

    /**
     * 设置过期时间
     *
     * @param expire 过期时间，单位毫秒
     * @return 是否设置成功
     */
    public boolean setExpiration(final long expire) {
        return this.expire.getAndSet(expire) != expire;
    }

    /**
     * 获取过期时间
     *
     * @return 过期时间
     */
    public long getExpire() {
        return expire.get();
    }

    /**
     * 新增任务，将任务加入到双向链表的头部
     *
     * @param timerCrontab 延迟任务
     */
    public void addTask(final TimerCrontab timerCrontab) {
        synchronized (this) {
            if (timerCrontab.timerTaskList == null) {
                timerCrontab.timerTaskList = this;
                final TimerCrontab tail = root.prev;
                timerCrontab.next = root;
                timerCrontab.prev = tail;
                tail.next = timerCrontab;
                root.prev = timerCrontab;
            }
        }
    }

    /**
     * 移除任务
     *
     * @param timerCrontab 任务
     */
    public void removeTask(final TimerCrontab timerCrontab) {
        synchronized (this) {
            if (this.equals(timerCrontab.timerTaskList)) {
                timerCrontab.next.prev = timerCrontab.prev;
                timerCrontab.prev.next = timerCrontab.next;
                timerCrontab.timerTaskList = null;
                timerCrontab.next = null;
                timerCrontab.prev = null;
            }
        }
    }

    /**
     * 重新分配，即将列表中的任务全部处理
     *
     * @param flush 任务处理函数
     */
    public synchronized void flush(final Consumer<TimerCrontab> flush) {
        TimerCrontab timerCrontab = root.next;
        while (!timerCrontab.equals(root)) {
            this.removeTask(timerCrontab);
            flush.accept(timerCrontab);
            timerCrontab = root.next;
        }
        expire.set(-1L);
    }

    @Override
    public long getDelay(final TimeUnit unit) {
        return Math.max(0, unit.convert(expire.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public int compareTo(final Delayed o) {
        if (o instanceof TimerTaskList) {
            return Long.compare(expire.get(), ((TimerTaskList) o).expire.get());
        }
        return 0;
    }

}
