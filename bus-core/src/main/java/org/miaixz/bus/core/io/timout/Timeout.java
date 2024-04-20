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
package org.miaixz.bus.core.io.timout;

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
 * @since Java 17+
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

    static long minTimeout(long aNanos, long bNanos) {
        if (aNanos == 0L) return bNanos;
        if (bNanos == 0L) return aNanos;
        if (aNanos < bNanos) return aNanos;
        return bNanos;
    }

    /**
     * Wait at most {@code timeout} time before aborting an operation. Using a
     * per-operation timeout means that as long as forward progress is being made,
     * no sequence of operations will fail.
     *
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
     * Returns the timeout in nanoseconds, or {@code 0} for no timeout.
     */
    public long timeoutNanos() {
        return timeoutNanos;
    }

    /**
     * Returns true if a deadline is enabled.
     */
    public boolean hasDeadline() {
        return hasDeadline;
    }

    /**
     * Returns the {@linkplain System#nanoTime() nano time} when the deadline will
     * be reached.
     *
     * @throws IllegalStateException if no deadline is set.
     */
    public long deadlineNanoTime() {
        if (!hasDeadline) throw new IllegalStateException("No deadline");
        return deadlineNanoTime;
    }

    /**
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
     * Set a deadline of now plus {@code duration} time.
     */
    public final Timeout deadline(long duration, TimeUnit unit) {
        if (duration <= 0) throw new IllegalArgumentException("duration <= 0: " + duration);
        if (unit == null) throw new IllegalArgumentException("unit == null");
        return deadlineNanoTime(System.nanoTime() + unit.toNanos(duration));
    }

    /**
     * Clears the timeout. Operating system timeouts may still apply.
     */
    public Timeout clearTimeout() {
        this.timeoutNanos = 0;
        return this;
    }

    /**
     * Clears the deadline.
     */
    public Timeout clearDeadline() {
        this.hasDeadline = false;
        return this;
    }

    /**
     * Throws an {@link InterruptedIOException} if the deadline has been reached or if the current
     * thread has been interrupted. This method doesn't detect timeouts; that should be implemented to
     * asynchronously abort an in-progress operation.
     */
    public void throwIfReached() throws IOException {
        if (Thread.interrupted()) {
            Thread.currentThread().interrupt(); // Retain interrupted status.
            throw new InterruptedIOException("interrupted");
        }

        if (hasDeadline && deadlineNanoTime - System.nanoTime() <= 0) {
            throw new InterruptedIOException("deadline reached");
        }
    }

    /**
     * Waits on {@code monitor} until it is notified. Throws {@link InterruptedIOException} if either
     * the thread is interrupted or if this timeout elapses before {@code monitor} is notified. The
     * caller must be synchronized on {@code monitor}.
     *
     * <p>Here's a sample class that uses {@code waitUntilNotified()} to await a specific state. Note
     * that the call is made within a loop to avoid unnecessary waiting and to mitigate spurious
     * notifications. <pre>{@code
     *
     *   class Dice {
     *     Random random = new Random();
     *     int latestTotal;
     *
     *     public synchronized void roll() {
     *       latestTotal = 2 + random.nextInt(6) + random.nextInt(6);
     *       System.out.println("Rolled " + latestTotal);
     *       notifyAll();
     *     }
     *
     *     public void rollAtFixedRate(int period, TimeUnit timeUnit) {
     *       Executors.newScheduledThreadPool(0).scheduleAtFixedRate(new Runnable() {
     *         public void run() {
     *           roll();
     *          }
     *       }, 0, period, timeUnit);
     *     }
     *
     *     public synchronized void awaitTotal(Timeout timeout, int total)
     *         throws InterruptedIOException {
     *       while (latestTotal != total) {
     *         timeout.waitUntilNotified(this);
     *       }
     *     }
     *   }
     * }</pre>
     */
    public final void waitUntilNotified(Object monitor) throws InterruptedIOException {
        try {
            boolean hasDeadline = hasDeadline();
            long timeoutNanos = timeoutNanos();

            if (!hasDeadline && timeoutNanos == 0L) {
                monitor.wait(); // There is no timeout: wait forever.
                return;
            }

            // Compute how long we'll wait.
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

            // Attempt to wait that long. This will break out early if the monitor is notified.
            long elapsedNanos = 0L;
            if (waitNanos > 0L) {
                long waitMillis = waitNanos / 1000000L;
                monitor.wait(waitMillis, (int) (waitNanos - waitMillis * 1000000L));
                elapsedNanos = System.nanoTime() - start;
            }

            // Throw if the timeout elapsed before the monitor was notified.
            if (elapsedNanos >= waitNanos) {
                throw new InterruptedIOException("timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("interrupted");
        }
    }

}
