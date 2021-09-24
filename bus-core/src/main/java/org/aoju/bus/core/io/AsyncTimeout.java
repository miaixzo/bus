/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.io;

import org.aoju.bus.core.toolkit.IoKit;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

/**
 * 此超时使用后台线程在超时发生时精确地执行操作 用它来
 * 在本地不支持超时的地方实现超时,例如对阻塞的套接字操作.
 *
 * @author Kimi Liu
 * @version 6.2.9
 * @since JDK 1.8+
 */
public class AsyncTimeout extends Timeout {
    /**
     * 一次不要写超过64 KiB的数据，否则，慢速连接可能会遭受超时
     */
    private static final int TIMEOUT_WRITE_SIZE = 64 * 1024;
    /**
     * 任务线程在关闭之前的空闲时间
     */
    private static final long IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final long IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS);
    /**
     * 线程处理一个挂起超时的链表，按要触发的顺序排序。该类在AsyncTimeout.class上同步。这个锁保护队列
     */
    static AsyncTimeout head;
    /**
     * 如果此节点当前在队列中，则为True
     */
    private boolean inQueue;
    /**
     * 链表中的下一个节点
     */
    private AsyncTimeout next;

    /**
     * 这就是任务超时时间
     */
    private long timeoutAt;

    private static synchronized void scheduleTimeout(
            AsyncTimeout node,
            long timeoutNanos,
            boolean hasDeadline) {
        // 启动任务线程，并在安排第一次超时时创建head节点
        if (null == head) {
            head = new AsyncTimeout();
            new Watchdog().start();
        }

        long now = System.nanoTime();
        if (timeoutNanos != 0 && hasDeadline) {
            // 计算最早的事件;要么超时，要么截止。因为nanoTime可以封装，
            // 所以Math.min()对于绝对值没有定义，但是对于相对值有意义
            node.timeoutAt = now + Math.min(timeoutNanos, node.deadlineNanoTime() - now);
        } else if (timeoutNanos != 0) {
            node.timeoutAt = now + timeoutNanos;
        } else if (hasDeadline) {
            node.timeoutAt = node.deadlineNanoTime();
        } else {
            throw new AssertionError();
        }
        // 按排序顺序插入节点
        long remainingNanos = node.remainingNanos(now);
        for (AsyncTimeout prev = head; true; prev = prev.next) {
            if (null == prev.next || remainingNanos < prev.next.remainingNanos(now)) {
                node.next = prev.next;
                prev.next = node;
                if (prev == head) {
                    // 在前面插入时，唤醒任务
                    AsyncTimeout.class.notify();
                }
                break;
            }
        }
    }

    /**
     * 如果超时发生，则返回true
     *
     * @param node 节点信息
     * @return the true/false
     */
    private static synchronized boolean cancelScheduledTimeout(AsyncTimeout node) {
        // 从链表中删除节点
        for (AsyncTimeout prev = head; null != prev; prev = prev.next) {
            if (prev.next == node) {
                prev.next = node.next;
                node.next = null;
                return false;
            }
        }
        // 在链表中没有找到节点:它一定超时了!
        return true;
    }

    /**
     * 删除并返回列表顶部的节点，如有必要，等待它超时。
     * 如果在开始时列表的头部没有节点，并且在等待{@code IDLE_TIMEOUT_NANOS}之后仍然没有节点，
     * 则返回{@link #head}。如果在等待时插入了新节点，则返回null。否则，它将返回被等待的已被删除的节点
     *
     * @return 超时信息 {@link AsyncTimeout}
     * @throws InterruptedException 异常
     */
    static AsyncTimeout awaitTimeout() throws InterruptedException {
        // 获取下一个符合条件的节点
        AsyncTimeout node = head.next;
        // 队列为空。等待，直到某物进入队列或空闲超时过期
        if (null == node) {
            long startNanos = System.nanoTime();
            AsyncTimeout.class.wait(IDLE_TIMEOUT_MILLIS);
            return null == head.next && (System.nanoTime() - startNanos) >= IDLE_TIMEOUT_NANOS
                    ? head  // 空闲超时过期
                    : null; // 情况发生了变化
        }

        long waitNanos = node.remainingNanos(System.nanoTime());

        // 队伍的头还没有超时。等待
        if (waitNanos > 0) {
            // 由于我们的工作时间是十亿分之一秒，所以等待变得很复杂，但是API需要两个参数(millis, nanos)
            long waitMillis = waitNanos / 1000000L;
            waitNanos -= (waitMillis * 1000000L);
            AsyncTimeout.class.wait(waitMillis, (int) waitNanos);
            return null;
        }
        // 队列的头已经超时了，删除它
        head.next = node.next;
        node.next = null;
        return node;
    }

    /**
     * 调用者应该在执行超时工作之前调用{@link #enter}，然后调用{@link #exit}
     * {@link #exit}的返回值指示是否触发超时。注意，对{@link #timedOut}的调用是异步的，
     * 可以在{@link #exit}之后调用
     */
    public final void enter() {
        if (inQueue) throw new IllegalStateException("Unbalanced enter/exit");
        long timeoutNanos = timeoutNanos();
        boolean hasDeadline = hasDeadline();
        if (timeoutNanos == 0 && !hasDeadline) {
            // 没有暂停和截止日期?别去排队
            return;
        }
        inQueue = true;
        scheduleTimeout(this, timeoutNanos, hasDeadline);
    }

    /**
     * 如果超时发生，则返回true
     *
     * @return the true/false
     */
    public final boolean exit() {
        if (!inQueue) return false;
        inQueue = false;
        return cancelScheduledTimeout(this);
    }

    /**
     * 返回在超时之前剩余的时间量
     * 如果超时已经过去，并且应该立即发生超时，则该值为负
     *
     * @param now 当前时间
     * @return
     */
    private long remainingNanos(long now) {
        return timeoutAt - now;
    }

    /**
     * 当对{@link #enter()}和{@link #exit()}的调用之间的时间超过超时时，watchdog线程将调用它
     */
    protected void timedOut() {
    }

    /**
     * 返回一个委托给{@code sink}的新缓冲接收器，使用它来实现超时。
     * 如果{@link #timedOut}被覆盖以中断{@code sink}的当前操作，那么这是最有效的
     *
     * @param sink 缓冲接收器
     * @return 新缓冲接收器
     */
    public final Sink sink(final Sink sink) {
        return new Sink() {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                IoKit.checkOffsetAndCount(source.size, 0, byteCount);
                while (byteCount > 0L) {
                    // 计算要写入的字节数。这个循环保证我们在一个段边界上分割
                    long toWrite = 0L;
                    for (Segment s = source.head; toWrite < TIMEOUT_WRITE_SIZE; s = s.next) {
                        int segmentSize = s.limit - s.pos;
                        toWrite += segmentSize;
                        if (toWrite >= byteCount) {
                            toWrite = byteCount;
                            break;
                        }
                    }
                    // 发出一个写。只有这个部分会超时
                    boolean throwOnTimeout = false;
                    enter();
                    try {
                        sink.write(source, toWrite);
                        byteCount -= toWrite;
                        throwOnTimeout = true;
                    } catch (IOException e) {
                        throw exit(e);
                    } finally {
                        exit(throwOnTimeout);
                    }
                }
            }

            @Override
            public void flush() throws IOException {
                boolean throwOnTimeout = false;
                enter();
                try {
                    sink.flush();
                    throwOnTimeout = true;
                } catch (IOException e) {
                    throw exit(e);
                } finally {
                    exit(throwOnTimeout);
                }
            }

            @Override
            public void close() throws IOException {
                boolean throwOnTimeout = false;
                enter();
                try {
                    sink.close();
                    throwOnTimeout = true;
                } catch (IOException e) {
                    throw exit(e);
                } finally {
                    exit(throwOnTimeout);
                }
            }

            @Override
            public Timeout timeout() {
                return AsyncTimeout.this;
            }

            @Override
            public String toString() {
                return "Awaits.sink(" + sink + ")";
            }
        };
    }

    /**
     * 返回一个委托给{@code source}的新源，使用它来实现超时。
     * 如果{@link #timedOut}被覆盖以中断{@code sink}的当前操作，那么这是最有效的
     *
     * @param source 源
     * @return 新源
     */
    public final Source source(final Source source) {
        return new Source() {
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                boolean throwOnTimeout = false;
                enter();
                try {
                    long result = source.read(sink, byteCount);
                    throwOnTimeout = true;
                    return result;
                } catch (IOException e) {
                    throw exit(e);
                } finally {
                    exit(throwOnTimeout);
                }
            }

            @Override
            public void close() throws IOException {
                boolean throwOnTimeout = false;
                enter();
                try {
                    source.close();
                    throwOnTimeout = true;
                } catch (IOException e) {
                    throw exit(e);
                } finally {
                    exit(throwOnTimeout);
                }
            }

            @Override
            public Timeout timeout() {
                return AsyncTimeout.this;
            }

            @Override
            public String toString() {
                return "Awaits.source(" + source + ")";
            }
        };
    }

    /**
     * 如果{@code throwOnTimeout}为{@code true}且发生超时，则抛出IOException。
     * 有关抛出的异常类型，请参见{@link #newTimeoutException(java.io.IOException)}
     *
     * @param throwOnTimeout 超时时间
     * @throws IOException 异常
     */
    final void exit(boolean throwOnTimeout) throws IOException {
        boolean timedOut = exit();
        if (timedOut && throwOnTimeout) throw newTimeoutException(null);
    }

    /**
     * 如果超时，则返回{@code cause}或{@code cause}引起的IOException。
     * 有关返回的异常类型，请参见{@link #newTimeoutException(java.io.IOException)}
     *
     * @param cause 异常
     * @return 异常信息
     */
    final IOException exit(IOException cause) {
        if (!exit()) return cause;
        return newTimeoutException(cause);
    }

    /**
     * 返回{@link IOException}表示超时。默认情况下，该方法返回{@link java.io.InterruptedIOException}。
     * 如果{@code cause}非空，则将其设置为返回异常的原因
     *
     * @param cause 异常
     * @return 异常信息
     */
    protected IOException newTimeoutException(IOException cause) {
        InterruptedIOException e = new InterruptedIOException("timeout");
        if (null != cause) {
            e.initCause(cause);
        }
        return e;
    }

    private static final class Watchdog extends Thread {
        Watchdog() {
            super("IoKit.Watchdog");
            setDaemon(true);
        }

        public void run() {
            while (true) {
                try {
                    AsyncTimeout timedOut;
                    synchronized (AsyncTimeout.class) {
                        timedOut = awaitTimeout();

                        if (null == timedOut) {
                            continue;
                        }

                        if (timedOut == head) {
                            head = null;
                            return;
                        }
                    }

                    timedOut.timedOut();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

}
