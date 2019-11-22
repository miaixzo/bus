/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.io.segment;

import org.aoju.bus.core.utils.IoUtils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

/**
 * 此超时使用后台线程在超时发生时精确地执行操作 用它来
 * 在本地不支持超时的地方实现超时,例如对阻塞的套接字操作.
 *
 * @author Kimi Liu
 * @version 5.2.3
 * @since JDK 1.8+
 */
public class Awaits extends Timeout {

    private static final int TIMEOUT_WRITE_SIZE = 64 * 1024;

    private static final long IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final long IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS);

    static Awaits head;

    private boolean inQueue;

    private Awaits next;

    private long timeoutAt;

    private static synchronized void scheduleTimeout(
            Awaits node, long timeoutNanos, boolean hasDeadline) {
        // Start the watchdog thread and create the head node when the first timeout is scheduled.
        if (head == null) {
            head = new Awaits();
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
        for (Awaits prev = head; true; prev = prev.next) {
            if (prev.next == null || remainingNanos < prev.next.remainingNanos(now)) {
                node.next = prev.next;
                prev.next = node;
                if (prev == head) {
                    Awaits.class.notify(); // Wake up the watchdog when inserting at the front.
                }
                break;
            }
        }
    }

    private static synchronized boolean cancelScheduledTimeout(Awaits node) {
        // Remove the node from the linked list.
        for (Awaits prev = head; prev != null; prev = prev.next) {
            if (prev.next == node) {
                prev.next = node.next;
                node.next = null;
                return false;
            }
        }

        // The node wasn't found in the linked list: it must have timed out!
        return true;
    }

    static Awaits awaitTimeout() throws InterruptedException {
        // Get the next eligible node.
        Awaits node = head.next;

        // The queue is empty. Wait until either something is enqueued or the idle timeout elapses.
        if (node == null) {
            long startNanos = System.nanoTime();
            Awaits.class.wait(IDLE_TIMEOUT_MILLIS);
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
            Awaits.class.wait(waitMillis, (int) waitNanos);
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


    public final boolean exit() {
        if (!inQueue) return false;
        inQueue = false;
        return cancelScheduledTimeout(this);
    }

    private long remainingNanos(long now) {
        return timeoutAt - now;
    }

    protected void timedOut() {
    }

    public final Sink sink(final Sink sink) {
        return new Sink() {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                IoUtils.checkOffsetAndCount(source.size, 0, byteCount);

                while (byteCount > 0L) {
                    // Count how many bytes to write. This loop guarantees we split on a segment boundary.
                    long toWrite = 0L;
                    for (Segment s = source.head; toWrite < TIMEOUT_WRITE_SIZE; s = s.next) {
                        int segmentSize = s.limit - s.pos;
                        toWrite += segmentSize;
                        if (toWrite >= byteCount) {
                            toWrite = byteCount;
                            break;
                        }
                    }

                    // Emit first write. Only this section is subject to the timeout.
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
                return Awaits.this;
            }

            @Override
            public String toString() {
                return "AsyncTimeout.sink(" + sink + ")";
            }
        };
    }

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
                return Awaits.this;
            }

            @Override
            public String toString() {
                return "AsyncTimeout.source(" + source + ")";
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
     * @param cause IOException
     * @return IOException  either {@code cause} or an IOException that's caused by {@code cause} if a timeout
     * occurred. See {@link #newTimeoutException(IOException)} for the type of exception
     * returned.
     */
    final IOException exit(IOException cause) throws IOException {
        if (!exit()) return cause;
        return newTimeoutException(cause);
    }

    /**
     * @param cause IOException
     * @return IOException an {@link IOException} to represent a timeout. By default this method returns {@link
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
            super("IoUtils.Watchdog");
            setDaemon(true);
        }

        public void run() {
            while (true) {
                try {
                    Awaits timedOut;
                    synchronized (Awaits.class) {
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
