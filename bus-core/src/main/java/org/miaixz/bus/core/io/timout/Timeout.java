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
package org.miaixz.bus.core.io.timout;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

/**
 * 在放弃一项任务之前要花多少时间的策略 当一个任务 超时时,它处于未指定的状态,应该被放弃 例如,如果从源读取超时,则应关闭该源并 稍后应重试读取 如果向接收器写入超时,也是一样 适用规则:关闭洗涤槽,稍后重试
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Timeout {

    /**
     * 既不跟踪也不检测超时的空超时。在不需要超时 的情况下使用它，例如在操作不会阻塞的实现中
     */
    public static final Timeout NONE = new Timeout() {
        @Override
        public Timeout timeout(long timeout, TimeUnit unit) {
            return this;
        }

        @Override
        public Timeout deadlineNanoTime(long deadlineNanoTime) {
            return this;
        }

        @Override
        public void throwIfReached() {
        }
    };

    /**
     * 如果定义了 {@code deadlineNanoTime}，则为 True。 对于 {@link System#nanoTime}，没有与 null 或 0 等价的值
     */
    private boolean hasDeadline;
    /**
     * 截止时间
     */
    private long deadlineNanoTime;
    /**
     * 超时时间
     */
    private long timeoutNanos;

    /**
     * 构建
     */
    public Timeout() {

    }

    /**
     * 最多等待 {@code timeout} 时间，然后中止操作。 使用每个操作超时意味着只要向前推进，任何操作序列都不会失败。
     *
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return {@link Timeout}
     */
    public Timeout timeout(long timeout, TimeUnit unit) {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout < 0: " + timeout);
        if (unit == null)
            throw new IllegalArgumentException("unit == null");
        this.timeoutNanos = unit.toNanos(timeout);
        return this;
    }

    /**
     * 返回以纳秒为单位的超时时间，或 {@code 0} 表示无超时。
     */
    public long timeoutNanos() {
        return timeoutNanos;
    }

    /**
     * 如果启用了截止期限，则返回 true
     */
    public boolean hasDeadline() {
        return hasDeadline;
    }

    /**
     * 返回截止期限的 {@linkplain System#nanoTime()}
     *
     * @throws IllegalStateException 如果没有设定截止时间
     */
    public long deadlineNanoTime() {
        if (!hasDeadline)
            throw new IllegalStateException("No deadline");
        return deadlineNanoTime;
    }

    /**
     * 设置达到截止期限的 {@linkplain System#nanoTime()} 所有操作必须在此时间之前完成。使用截止期限来设置一系列操作所花费时间的最大限度。
     */
    public Timeout deadlineNanoTime(long deadlineNanoTime) {
        this.hasDeadline = true;
        this.deadlineNanoTime = deadlineNanoTime;
        return this;
    }

    /**
     * 设定现在加上{@code duration} 时间的截止时间
     *
     * @param duration 期间
     * @param unit     时间单位
     * @return {@link Timeout}
     */
    public final Timeout deadline(long duration, TimeUnit unit) {
        if (duration <= 0)
            throw new IllegalArgumentException("duration <= 0: " + duration);
        if (unit == null)
            throw new IllegalArgumentException("unit == null");
        return deadlineNanoTime(System.nanoTime() + unit.toNanos(duration));
    }

    /**
     * 清除超时
     */
    public Timeout clearTimeout() {
        this.timeoutNanos = 0;
        return this;
    }

    /**
     * 清除最后期限
     */
    public Timeout clearDeadline() {
        this.hasDeadline = false;
        return this;
    }

    /**
     * 如果已达到截止时间或当前线程已中断，则抛出 {@link InterruptedIOException}。 此方法不检测超时；应实施超时以异步中止正在进行的操作
     */
    public void throwIfReached() throws IOException {
        if (Thread.interrupted()) {
            // 保留中断状态
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("interrupted");
        }

        if (hasDeadline && deadlineNanoTime - System.nanoTime() <= 0) {
            throw new InterruptedIOException("deadline reached");
        }
    }

    /**
     * 等待 {@code monitor} 直到收到通知。 如果线程中断或在 {@code monitor} 收到通知之前超时，则抛出 {@link InterruptedIOException}。 调用者必须在
     * {@code monitor} 上同步。
     *
     * @param monitor 监视器
     * @throws InterruptedIOException 异常
     */
    public final void waitUntilNotified(Object monitor) throws InterruptedIOException {
        try {
            boolean hasDeadline = hasDeadline();
            long timeoutNanos = timeoutNanos();

            if (!hasDeadline && timeoutNanos == 0L) {
                // 没有超时：永远等待
                monitor.wait();
                return;
            }

            // 计算一下要等待多久
            long waitNanos;
            long start = System.nanoTime();
            if (hasDeadline && timeoutNanos != 0) {
                long deadlineNanos = deadlineNanoTime() - start;
                waitNanos = Math.min(timeoutNanos, deadlineNanos);
            } else if (hasDeadline) {
                waitNanos = deadlineNanoTime() - start;
            } else {
                waitNanos = timeoutNanos;
            }

            // 尝试等待那么长时间。如果监视器收到通知，这将提前触发
            long elapsedNanos = 0L;
            if (waitNanos > 0L) {
                long waitMillis = waitNanos / 1000000L;
                monitor.wait(waitMillis, (int) (waitNanos - waitMillis * 1000000L));
                elapsedNanos = System.nanoTime() - start;
            }

            // 如果在监视器收到通知之前已经超时，则抛出
            if (elapsedNanos >= waitNanos) {
                throw new InterruptedIOException("timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("interrupted");
        }
    }

}
