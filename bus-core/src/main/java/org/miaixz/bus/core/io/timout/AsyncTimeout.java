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
 * 此超时使用后台线程在超时发生时精确地执行操作 用它来
 * 在本地不支持超时的地方实现超时,例如对阻塞的套接字操作.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AsyncTimeout extends Timeout {

    /**
     * Don't write more than 64 KiB of data at a time, give or take a segment. Otherwise slow
     * connections may suffer timeouts even when they're making (slow) progress. Without this, writing
     * a single 1 MiB buffer may never succeed on a sufficiently slow connection.
     */
    private static final int TIMEOUT_WRITE_SIZE = 64 * 1024;

    /**
     * Duration for the watchdog thread to be idle before it shuts itself down.
     */
    private static final long IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final long IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS);

    /**
     * The watchdog thread processes a linked list of pending timeouts, sorted in the order to be
     * triggered. This class synchronizes on AsyncTimeout.class. This lock guards the queue.
     *
     * <p>Head's 'next' points to the first element of the linked list. The first element is the next
     * node to time out, or null if the queue is empty. The head is null until the watchdog thread is
     * started and also after being idle for {@link #IDLE_TIMEOUT_MILLIS}.
     */
    static AsyncTimeout head;

    /**
     * True if this node is currently in the queue.
     */
    private boolean inQueue;

    /**
     * The next node in the linked list.
     */
    private AsyncTimeout next;

    /**
     * If scheduled, this is the time that the watchdog should time this out.
     */
    private long timeoutAt;

    private static synchronized void scheduleTimeout(
            AsyncTimeout node, long timeoutNanos, boolean hasDeadline) {
        // Start the watchdog thread and create the head node when the first timeout is scheduled.
        if (head == null) {
            head = new AsyncTimeout();
            new Watchdog().start();
        }

        long now = System.nanoTime();
        if (timeoutNanos != 0 && hasDeadline) {
            // Compute the earliest event; either timeout or deadline. Because nanoTime can wrap around,
            // Math.min() is undefined for absolute values, but meaningful for relative ones.
            node.timeoutAt = now + Math.min(timeoutNanos, node.deadlineNanoTime() - now);
        } else if (timeoutNanos != 0) {
            node.timeoutAt = now + timeoutNanos;
        } else if (hasDeadline) {
            node.timeoutAt = node.deadlineNanoTime();
        } else {
            throw new AssertionError();
        }

        // Insert the node in sorted order.
        long remainingNanos = node.remainingNanos(now);
        for (AsyncTimeout prev = head; true; prev = prev.next) {
            if (prev.next == null || remainingNanos < prev.next.remainingNanos(now)) {
                node.next = prev.next;
                prev.next = node;
                if (prev == head) {
                    AsyncTimeout.class.notify(); // Wake up the watchdog when inserting at the front.
                }
                break;
            }
        }
    }

    /**
     * Returns true if the timeout occurred.
     */
    private static synchronized boolean cancelScheduledTimeout(AsyncTimeout node) {
        // Remove the node from the linked list.
        for (AsyncTimeout prev = head; prev != null; prev = prev.next) {
            if (prev.next == node) {
                prev.next = node.next;
                node.next = null;
                return false;
            }
        }

        // The node wasn't found in the linked list: it must have timed out!
        return true;
    }

    /**
     * Removes and returns the node at the head of the list, waiting for it to time out if necessary.
     * This returns {@link #head} if there was no node at the head of the list when starting, and
     * there continues to be no node after waiting {@code IDLE_TIMEOUT_NANOS}. It returns null if a
     * new node was inserted while waiting. Otherwise this returns the node being waited on that has
     * been removed.
     */
    static AsyncTimeout awaitTimeout() throws InterruptedException {
        // Get the next eligible node.
        AsyncTimeout node = head.next;

        // The queue is empty. Wait until either something is enqueued or the idle timeout elapses.
        if (node == null) {
            long startNanos = System.nanoTime();
            AsyncTimeout.class.wait(IDLE_TIMEOUT_MILLIS);
            return head.next == null && (System.nanoTime() - startNanos) >= IDLE_TIMEOUT_NANOS
                    ? head  // The idle timeout elapsed.
                    : null; // The situation has changed.
        }

        long waitNanos = node.remainingNanos(System.nanoTime());

        // The head of the queue hasn't timed out yet. Await that.
        if (waitNanos > 0) {
            // Waiting is made complicated by the fact that we work in nanoseconds,
            // but the API wants (millis, nanos) in two arguments.
            long waitMillis = waitNanos / 1000000L;
            waitNanos -= (waitMillis * 1000000L);
            AsyncTimeout.class.wait(waitMillis, (int) waitNanos);
            return null;
        }

        // The head of the queue has timed out. Remove it.
        head.next = node.next;
        node.next = null;
        return node;
    }

    public final void enter() {
        if (inQueue) throw new IllegalStateException("Unbalanced enter/exit");
        long timeoutNanos = timeoutNanos();
        boolean hasDeadline = hasDeadline();
        if (timeoutNanos == 0 && !hasDeadline) {
            return; // No timeout and no deadline? Don't bother with the queue.
        }
        inQueue = true;
        scheduleTimeout(this, timeoutNanos, hasDeadline);
    }

    /**
     * Returns true if the timeout occurred.
     */
    public final boolean exit() {
        if (!inQueue) return false;
        inQueue = false;
        return cancelScheduledTimeout(this);
    }

    /**
     * Returns the amount of time left until the time out. This will be negative if the timeout has
     * elapsed and the timeout should occur immediately.
     */
    private long remainingNanos(long now) {
        return timeoutAt - now;
    }

    /**
     * Invoked by the watchdog thread when the time between calls to {@link #enter()} and {@link
     * #exit()} has exceeded the timeout.
     */
    protected void timedOut() {
    }

    /**
     * Returns a new sink that delegates to {@code sink}, using this to implement timeouts. This works
     * best if {@link #timedOut} is overridden to interrupt {@code sink}'s current operation.
     */
    public final Sink sink(final Sink sink) {
        return new Sink() {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                IoKit.checkOffsetAndCount(source.size, 0, byteCount);

                while (byteCount > 0L) {
                    // Count how many bytes to write. This loop guarantees we split on a segment boundary.
                    long toWrite = 0L;
                    for (SectionBuffer s = source.head; toWrite < TIMEOUT_WRITE_SIZE; s = s.next) {
                        int segmentSize = s.limit - s.pos;
                        toWrite += segmentSize;
                        if (toWrite >= byteCount) {
                            toWrite = byteCount;
                            break;
                        }
                    }

                    // Emit one write. Only this section is subject to the timeout.
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
     * Returns a new source that delegates to {@code source}, using this to implement timeouts. This
     * works best if {@link #timedOut} is overridden to interrupt {@code sink}'s current operation.
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
     * Throws an IOException if {@code throwOnTimeout} is {@code true} and a timeout occurred. See
     * {@link #newTimeoutException(IOException)} for the type of exception thrown.
     */
    final void exit(boolean throwOnTimeout) throws IOException {
        boolean timedOut = exit();
        if (timedOut && throwOnTimeout) throw newTimeoutException(null);
    }

    /**
     * Returns either {@code cause} or an IOException that's caused by {@code cause} if a timeout
     * occurred. See {@link #newTimeoutException(IOException)} for the type of exception
     * returned.
     */
    final IOException exit(IOException cause) throws IOException {
        if (!exit()) return cause;
        return newTimeoutException(cause);
    }

    /**
     * Returns an {@link IOException} to represent a timeout. By default this method returns {@link
     * InterruptedIOException}. If {@code cause} is non-null it is set as the cause of the
     * returned exception.
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
            super("Okio Watchdog");
            setDaemon(true);
        }

        public void run() {
            while (true) {
                try {
                    AsyncTimeout timedOut;
                    synchronized (AsyncTimeout.class) {
                        timedOut = awaitTimeout();

                        // Didn't find a node to interrupt. Try again.
                        if (timedOut == null) continue;

                        // The queue is completely empty. Let this thread exit and let another watchdog thread
                        // get created on the next call to scheduleTimeout().
                        if (timedOut == head) {
                            head = null;
                            return;
                        }
                    }

                    // Close the timed out node.
                    timedOut.timedOut();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

}
