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
package org.miaixz.bus.core.io.timout;

import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

/**
 * 此超时使用后台线程在超时发生时精确地执行操作 用它来 在本地不支持超时的地方实现超时,例如对阻塞的套接字操作.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AsyncTimeout extends Timeout {

    /**
     * 每次写入的数据不得超过 64 KiB，无论大小。 否则，慢速连接即使正在（缓慢）进行，也可能会超时。 如果不这样做，在速度足够慢的连接上，写入单个 1 MiB 缓冲区可能永远不会成功。
     */
    private static final int TIMEOUT_WRITE_SIZE = 64 * 1024;

    /**
     * 守护线程在关闭之前处于空闲状态的持续时间
     */
    private static final long IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final long IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS);

    /**
     * 守护线程处理待处理超时的链接列表，按触发顺序排序。
     */
    static AsyncTimeout head;

    /**
     * 如果此节点当前在队列中，则为 True。
     */
    private boolean inQueue;

    /**
     * 链接列表中的下一个节点。
     */
    private AsyncTimeout next;

    /**
     * 如果已安排好，这是守护线程应该超时的时间。
     */
    private long timeoutAt;

    private static synchronized void scheduleTimeout(AsyncTimeout node, long timeoutNanos, boolean hasDeadline) {
        // 在第一次超时安排时启动守护线程并创建头节点
        if (head == null) {
            head = new AsyncTimeout();
            new Watchdog().start();
        }

        long now = System.nanoTime();
        if (timeoutNanos != 0 && hasDeadline) {
            // 计算最早的事件；超时或截止时间。由于 nanoTime 可以循环，因此 Math.min() 对于绝对值是未定义的，但对于相对值是有意义的。
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
            if (prev.next == null || remainingNanos < prev.next.remainingNanos(now)) {
                node.next = prev.next;
                prev.next = node;
                if (prev == head) {
                    // 前端插入时唤醒守护线程
                    AsyncTimeout.class.notify();
                }
                break;
            }
        }
    }

    /**
     * 如果发生超时则返回 true
     *
     * @param node
     * @return the true/false
     */
    private static synchronized boolean cancelScheduledTimeout(AsyncTimeout node) {
        // 从链接列表中删除该节点
        for (AsyncTimeout prev = head; prev != null; prev = prev.next) {
            if (prev.next == node) {
                prev.next = node.next;
                node.next = null;
                return false;
            }
        }
        // 链接列表中未找到该节点：一定是超时了！
        return true;
    }

    /**
     * 删除并返回列表头部的节点，必要时等待其超时。 如果启动时列表头部没有节点，则返回 {@link #head}，并且在等待 {@code IDLE_TIMEOUT_NANOS} 后仍然没有节点。
     * 如果在等待期间插入了新节点，则返回 null。否则，返回正在等待的已被删除的节点。
     */
    static AsyncTimeout awaitTimeout() throws InterruptedException {
        // 获取下一个合格节点
        AsyncTimeout node = head.next;

        // 队列为空。等待，直到有内容入队或空闲超时结束。
        if (node == null) {
            long startNanos = System.nanoTime();
            AsyncTimeout.class.wait(IDLE_TIMEOUT_MILLIS);
            return head.next == null && (System.nanoTime() - startNanos) >= IDLE_TIMEOUT_NANOS ? head // 空闲超时已过
                    : null; // 情况已经发生了变化
        }

        long waitNanos = node.remainingNanos(System.nanoTime());

        // 队头尚未超时。请等待
        if (waitNanos > 0) {
            // 由于我们以纳秒为单位进行工作，因此等待变得复杂，但 API 需要两个参数（毫秒，纳秒）。
            long waitMillis = waitNanos / 1000000L;
            waitNanos -= (waitMillis * 1000000L);
            AsyncTimeout.class.wait(waitMillis, (int) waitNanos);
            return null;
        }

        // 队列头部已超时。请将其移除
        head.next = node.next;
        node.next = null;
        return node;
    }

    public final void enter() {
        if (inQueue)
            throw new IllegalStateException("Unbalanced enter/exit");
        long timeoutNanos = timeoutNanos();
        boolean hasDeadline = hasDeadline();
        if (timeoutNanos == 0 && !hasDeadline) {
            // 没有超时和截止期限？不用排队
            return;
        }
        inQueue = true;
        scheduleTimeout(this, timeoutNanos, hasDeadline);
    }

    /**
     * 如果发生超时则返回 true
     *
     * @return the true/false
     */
    public final boolean exit() {
        if (!inQueue)
            return false;
        inQueue = false;
        return cancelScheduledTimeout(this);
    }

    /**
     * 返回距离超时还剩多少时间。如果超时已过且超时应立即发生，则该值为负数。
     */
    private long remainingNanos(long now) {
        return timeoutAt - now;
    }

    /**
     * 当调用 {@link #enter()} 和 {@link #exit()} 之间的时间超出超时时，由守护线程调用。
     */
    protected void timedOut() {
    }

    /**
     * 返回一个委托给 {@code sink} 的新接收器，使用它来实现超时。 如果覆盖 {@link #timedOut} 以中断 {@code sink} 的当前操作，则效果最佳。
     */
    public final Sink sink(final Sink sink) {
        return new Sink() {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                IoKit.checkOffsetAndCount(source.size, 0, byteCount);
                while (byteCount > 0L) {
                    // 计算要写入的字节数。此循环保证我们在段边界上进行分割。
                    long toWrite = 0L;
                    for (SectionBuffer s = source.head; toWrite < TIMEOUT_WRITE_SIZE; s = s.next) {
                        int segmentSize = s.limit - s.pos;
                        toWrite += segmentSize;
                        if (toWrite >= byteCount) {
                            toWrite = byteCount;
                            break;
                        }
                    }

                    // 发出一次写入。只有此部分受超时限制。
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
                return "AsyncTimeout.sink(" + sink + Symbol.PARENTHESE_RIGHT;
            }
        };
    }

    /**
     * 返回一个委托给 {@code source} 的新源，使用它来实现超时。 如果覆盖 {@link #timedOut} 以中断 {@code sink} 的当前操作，则效果最佳。
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
                return "AsyncTimeout.source(" + source + Symbol.PARENTHESE_RIGHT;
            }
        };
    }

    /**
     * 如果 {@code throwOnTimeout} 为 {@code true} 且发生超时，则抛出 IOException。 请参阅 {@link #newTimeoutException(IOException)}
     * 以了解抛出的异常类型。
     */
    final void exit(boolean throwOnTimeout) throws IOException {
        boolean timedOut = exit();
        if (timedOut && throwOnTimeout)
            throw newTimeoutException(null);
    }

    /**
     * 如果发生超时，则返回 {@code cause} 或由 {@code cause} 引起的 IOException。 有关返回的异常类型，请参阅
     * {@link #newTimeoutException(IOException)}。
     */
    final IOException exit(IOException cause) throws IOException {
        if (!exit())
            return cause;
        return newTimeoutException(cause);
    }

    /**
     * 返回一个 {@link IOException} 来表示超时。默认情况下，此方法返回 {@link InterruptedIOException}。 如果 {@code cause} 非空，则将其设置为返回异常的原因。
     */
    protected IOException newTimeoutException(IOException cause) {
        InterruptedIOException e = new InterruptedIOException("timeout");
        if (cause != null) {
            e.initCause(cause);
        }
        return e;
    }

    private static final class Watchdog extends Thread {
        Watchdog() {
            super("Watchdog");
            setDaemon(true);
        }

        public void run() {
            while (true) {
                try {
                    AsyncTimeout timedOut;
                    synchronized (AsyncTimeout.class) {
                        timedOut = awaitTimeout();

                        // 未找到要中断的节点。请重试
                        if (timedOut == null)
                            continue;

                        // 队列完全为空。让此线程退出，并在下次调用 scheduleTimeout() 时创建另一个守护线程。
                        if (timedOut == head) {
                            head = null;
                            return;
                        }
                    }

                    // 关闭超时节点
                    timedOut.timedOut();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

}
