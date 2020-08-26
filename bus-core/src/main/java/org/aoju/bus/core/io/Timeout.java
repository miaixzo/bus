/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.io;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

/**
 * 在放弃一项任务之前要花多少时间的策略 当一个任务
 * 超时时,它处于未指定的状态,应该被放弃
 * 例如,如果从源读取超时,则应关闭该源并
 * 稍后应重试读取 如果向接收器写入超时,也是一样
 * 适用规则:关闭洗涤槽,稍后重试
 *
 * @author Kimi Liu
 * @version 6.0.8
 * @since JDK 1.8+
 */
public class Timeout {

    /**
     * 既不跟踪也不检测超时的空超时。在不需要超时
     * 的情况下使用它，例如在操作不会阻塞的实现中.
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
     * True if {@code deadlineNanoTime} is defined. There is no equivalent to null
     * or 0 for {@link System#nanoTime}.
     */
    private boolean hasDeadline;
    private long deadlineNanoTime;
    private long timeoutNanos;

    public Timeout() {
    }

    /**
     * @param timeout long
     * @param unit    TimeUnit
     * @return timeout
     * <p>
     * Wait at most {@code timeout} time before aborting an operation. Using a
     * per-operation timeout means that as long as forward progress is being made,
     * no sequence of operations will fail.
     * <p>If {@code timeout == 0}, operations will run indefinitely. (Operating
     * system timeouts may still apply.)
     */
    public Timeout timeout(long timeout, TimeUnit unit) {
        if (timeout < 0) throw new IllegalArgumentException("timeout < 0: " + timeout);
        if (unit == null) throw new IllegalArgumentException("unit == null");
        this.timeoutNanos = unit.toNanos(timeout);
        return this;
    }

    /**
     * @return the timeout in nanoseconds, or {@code 0} for no timeout.
     */
    public long timeoutNanos() {
        return timeoutNanos;
    }

    /**
     * @return hasDeadline true if a deadline is enabled.
     */
    public boolean hasDeadline() {
        return hasDeadline;
    }

    /**
     * @return deadlineNanoTime
     * Returns the {@linkplain System#nanoTime() nano time} when the deadline will
     * be reached.
     * @throws IllegalStateException if no deadline is set.
     */
    public long deadlineNanoTime() {
        if (!hasDeadline) throw new IllegalStateException("No deadline");
        return deadlineNanoTime;
    }

    /**
     * @param deadlineNanoTime long
     * @return timeout
     * Sets the {@linkplain System#nanoTime() nano time} when the deadline will be
     * reached. All operations must complete before this time. Use a deadline to
     * set a maximum bound on the time spent on a sequence of operations.
     */
    public Timeout deadlineNanoTime(long deadlineNanoTime) {
        this.hasDeadline = true;
        this.deadlineNanoTime = deadlineNanoTime;
        return this;
    }

    /**
     * @param duration long
     * @param unit     TimeUnit
     * @return timeout
     * Set a deadline of now plus {@code duration} time.
     */
    public final Timeout deadline(long duration, TimeUnit unit) {
        if (duration <= 0) throw new IllegalArgumentException("duration <= 0: " + duration);
        if (unit == null) throw new IllegalArgumentException("unit == null");
        return deadlineNanoTime(System.nanoTime() + unit.toNanos(duration));
    }

    /**
     * @return this Clears the timeout. Operating system timeouts may still apply.
     */
    public Timeout clearTimeout() {
        this.timeoutNanos = 0;
        return this;
    }

    /**
     * @return this Clears the deadline.
     */
    public Timeout clearDeadline() {
        this.hasDeadline = false;
        return this;
    }

    /**
     * Throws an {@link InterruptedIOException} if the deadline has been reached or if the current
     * thread has been interrupted. This method doesn't detect timeouts; that should be implemented to
     * asynchronously abort an in-progress operation.
     *
     * @throws IOException 抛出异常
     */
    public void throwIfReached() throws IOException {
        if (Thread.interrupted()) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("interrupted");
        }

        if (hasDeadline && deadlineNanoTime - System.nanoTime() <= 0) {
            throw new InterruptedIOException("deadline reached");
        }
    }

    /**
     * @param monitor Waits on {@code monitor} until it is notified. Throws {@link InterruptedIOException} if either
     *                the thread is interrupted or if this timeout elapses before {@code monitor} is notified. The
     *                caller must be synchronized on {@code monitor}.
     * @throws InterruptedIOException 抛出异常
     */
    public final void waitUntilNotified(Object monitor) throws InterruptedIOException {
        try {
            boolean hasDeadline = hasDeadline();
            long timeoutNanos = timeoutNanos();

            if (!hasDeadline && timeoutNanos == 0L) {
                monitor.wait();
                return;
            }

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

            long elapsedNanos = 0L;
            if (waitNanos > 0L) {
                long waitMillis = waitNanos / 1000000L;
                monitor.wait(waitMillis, (int) (waitNanos - waitMillis * 1000000L));
                elapsedNanos = System.nanoTime() - start;
            }

            if (elapsedNanos >= waitNanos) {
                throw new InterruptedIOException("timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("interrupted");
        }
    }

}
