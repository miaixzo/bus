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

import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * This {@link Timeout} implementation uses a background thread to precisely enforce timeouts. It is used to implement
 * timeouts where they are not natively supported, such as for blocking socket operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AsyncTimeout extends Timeout {

    /**
     * The maximum number of bytes to write at once. No more than 64 KiB should be written at a time, regardless of the
     * total size. Otherwise, slow connections may time out even if progress is (slowly) being made. Without this,
     * writing a single 1 MiB buffer might never succeed on a sufficiently slow connection.
     */
    private static final int TIMEOUT_WRITE_SIZE = 64 * 1024;

    /**
     * The duration in milliseconds that the watchdog thread will remain idle before shutting down.
     */
    private static final long IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);
    /**
     * The duration in nanoseconds that the watchdog thread will remain idle before shutting down.
     */
    private static final long IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS);

    /**
     * The head of a singly-linked list of pending timeouts, ordered by when they are due to expire. This list is
     * managed by the watchdog thread.
     */
    static AsyncTimeout head;

    /**
     * A flag indicating whether this node is currently in the timeout queue.
     */
    private boolean inQueue;

    /**
     * The next node in the singly-linked list of pending timeouts.
     */
    private AsyncTimeout next;

    /**
     * The time in nanoseconds when this timeout is scheduled to expire.
     */
    private long timeoutAt;

    /**
     * Schedules a timeout to be enforced by the watchdog thread. This method adds the given {@code node} to the sorted
     * linked list of pending timeouts.
     *
     * @param node         The {@code AsyncTimeout} instance to schedule.
     * @param timeoutNanos The timeout duration in nanoseconds.
     * @param hasDeadline  {@code true} if a deadline is set for this timeout, {@code false} otherwise.
     */
    private static synchronized void scheduleTimeout(AsyncTimeout node, long timeoutNanos, boolean hasDeadline) {
        // Start the watchdog thread and create the head node if this is the first timeout being scheduled.
        if (head == null) {
            head = new AsyncTimeout();
            new Watchdog().start();
        }

        long now = System.nanoTime();
        if (timeoutNanos != 0 && hasDeadline) {
            // Compute the earliest event: either the timeout or the deadline.
            // Since nanoTime can wrap around, Math.min() is undefined for absolute values,
            // but it is meaningful for relative values.
            node.timeoutAt = now + Math.min(timeoutNanos, node.deadlineNanoTime() - now);
        } else if (timeoutNanos != 0) {
            node.timeoutAt = now + timeoutNanos;
        } else if (hasDeadline) {
            node.timeoutAt = node.deadlineNanoTime();
        } else {
            throw new AssertionError(); // Should not happen if timeoutNanos or hasDeadline is true.
        }

        // Insert the node in sorted order.
        long remainingNanos = node.remainingNanos(now);
        for (AsyncTimeout prev = head; true; prev = prev.next) {
            if (prev.next == null || remainingNanos < prev.next.remainingNanos(now)) {
                node.next = prev.next;
                prev.next = node;
                if (prev == head) {
                    // If inserted at the front, wake up the watchdog thread.
                    AsyncTimeout.class.notify();
                }
                break;
            }
        }
    }

    /**
     * Cancels a scheduled timeout. This method removes the given {@code node} from the linked list of pending timeouts.
     *
     * @param node The {@code AsyncTimeout} instance to cancel.
     * @return {@code true} if the node was found in the list and successfully cancelled before it timed out,
     *         {@code false} if the node was not found (meaning it has already timed out).
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
        // Node not found in the linked list: it must have already timed out!
        return true;
    }

    /**
     * Removes and returns the head of the list, waiting for it to time out if necessary. If there are no nodes at the
     * head of the list when started, it returns {@link #head} if there are still no nodes after waiting for
     * {@code IDLE_TIMEOUT_NANOS}. If a new node is inserted during the wait, it returns {@code null}. Otherwise, it
     * returns the removed node that has timed out.
     *
     * @return The {@code AsyncTimeout} node that has timed out, {@link #head} if idle timeout occurred, or {@code null}
     *         if a new node was inserted during the wait.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    static AsyncTimeout awaitTimeout() throws InterruptedException {
        // Get the next eligible node.
        AsyncTimeout node = head.next;

        // If the queue is empty, wait until content is enqueued or the idle timeout expires.
        if (node == null) {
            long startNanos = System.nanoTime();
            AsyncTimeout.class.wait(IDLE_TIMEOUT_MILLIS);
            return head.next == null && (System.nanoTime() - startNanos) >= IDLE_TIMEOUT_NANOS ? head // Idle timeout
                                                                                                      // passed
                    : null; // Something has changed
        }

        long waitNanos = node.remainingNanos(System.nanoTime());

        // The head of the queue has not yet timed out. Wait.
        if (waitNanos > 0) {
            // Waiting is complicated because we work in nanoseconds, but the API requires two arguments (milliseconds,
            // nanoseconds).
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

    /**
     * Enters the timeout. This method should be called before a blocking operation that is subject to this timeout. If
     * a timeout or deadline is set, this timeout will be scheduled with the watchdog thread.
     *
     * @throws IllegalStateException If this timeout is already in the queue (unbalanced enter/exit calls).
     */
    public final void enter() {
        if (inQueue)
            throw new IllegalStateException("Unbalanced enter/exit");
        long timeoutNanos = timeoutNanos();
        boolean hasDeadline = hasDeadline();
        if (timeoutNanos == 0 && !hasDeadline) {
            // No timeout and no deadline? No need to queue.
            return;
        }
        inQueue = true;
        scheduleTimeout(this, timeoutNanos, hasDeadline);
    }

    /**
     * Exits the timeout. This method should be called after a blocking operation that was subject to this timeout. It
     * removes this timeout from the watchdog thread's queue.
     *
     * @return {@code true} if the timeout elapsed before the operation completed, {@code false} otherwise.
     */
    public final boolean exit() {
        if (!inQueue)
            return false;
        inQueue = false;
        return cancelScheduledTimeout(this);
    }

    /**
     * Returns the remaining time in nanoseconds until this timeout expires. If the timeout has already passed, the
     * value will be negative.
     *
     * @param now The current time in nanoseconds, typically from {@link System#nanoTime()}.
     * @return The remaining time in nanoseconds.
     */
    private long remainingNanos(long now) {
        return timeoutAt - now;
    }

    /**
     * Called by the watchdog thread when the time between {@link #enter()} and {@link #exit()} exceeds the timeout.
     * Subclasses should override this method to implement custom timeout handling, such as interrupting the current
     * operation.
     */
    protected void timedOut() {
    }

    /**
     * Returns a new {@link Sink} that delegates to {@code sink} and enforces this timeout. This is most effective if
     * {@link #timedOut()} is overridden to interrupt the current operation of the {@code sink}.
     *
     * @param sink The underlying {@link Sink} to wrap.
     * @return A new {@link Sink} that applies this timeout.
     */
    public final Sink sink(final Sink sink) {
        return new Sink() {

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                IoKit.checkOffsetAndCount(source.size, 0, byteCount);
                while (byteCount > 0L) {
                    // Compute the number of bytes to write. This loop ensures we split on segment boundaries.
                    long toWrite = 0L;
                    for (SectionBuffer s = source.head; toWrite < TIMEOUT_WRITE_SIZE; s = s.next) {
                        int segmentSize = s.limit - s.pos;
                        toWrite += segmentSize;
                        if (toWrite >= byteCount) {
                            toWrite = byteCount;
                            break;
                        }
                    }

                    // Perform a single write. Only this part is subject to the timeout.
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
     * Returns a new {@link Source} that delegates to {@code source} and enforces this timeout. This is most effective
     * if {@link #timedOut()} is overridden to interrupt the current operation of the {@code source}.
     *
     * @param source The underlying {@link Source} to wrap.
     * @return A new {@link Source} that applies this timeout.
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
     * Throws an {@link IOException} if {@code throwOnTimeout} is {@code true} and a timeout occurred. See
     * {@link #newTimeoutException(IOException)} for the type of exception thrown.
     *
     * @param throwOnTimeout If {@code true}, an exception will be thrown if a timeout occurred.
     * @throws IOException If a timeout occurred and {@code throwOnTimeout} is {@code true}.
     */
    final void exit(boolean throwOnTimeout) throws IOException {
        boolean timedOut = exit();
        if (timedOut && throwOnTimeout)
            throw newTimeoutException(null);
    }

    /**
     * Returns {@code cause} or an {@link IOException} caused by {@code cause} if a timeout occurred. See
     * {@link #newTimeoutException(IOException)} for the type of exception returned.
     *
     * @param cause The original {@link IOException} that occurred, or {@code null}.
     * @return The original {@link IOException} if no timeout occurred, or a new {@link IOException} (potentially
     *         wrapping the original cause) if a timeout occurred.
     */
    final IOException exit(IOException cause) throws IOException {
        if (!exit())
            return cause;
        return newTimeoutException(cause);
    }

    /**
     * Returns an {@link IOException} to indicate a timeout. By default, this method returns an
     * {@link InterruptedIOException}. If {@code cause} is non-null, it is set as the cause of the returned exception.
     *
     * @param cause The original {@link IOException} that led to the timeout, or {@code null}.
     * @return An {@link InterruptedIOException} representing the timeout.
     */
    protected IOException newTimeoutException(IOException cause) {
        InterruptedIOException e = new InterruptedIOException("timeout");
        if (cause != null) {
            e.initCause(cause);
        }
        return e;
    }

    /**
     * A daemon thread that monitors scheduled {@link AsyncTimeout} instances and calls their {@link #timedOut()} method
     * when their timeout expires.
     */
    private static final class Watchdog extends Thread {

        /**
         * Constructs a new Watchdog thread.
         */
        Watchdog() {
            super("Watchdog");
            setDaemon(true);
        }

        /**
         * The main loop of the watchdog thread. It continuously waits for timeouts to expire and calls
         * {@link AsyncTimeout#timedOut()} on the expired instances.
         */
        public void run() {
            while (true) {
                try {
                    AsyncTimeout timedOut;
                    synchronized (AsyncTimeout.class) {
                        timedOut = awaitTimeout();

                        // No node to interrupt found. Retry.
                        if (timedOut == null)
                            continue;

                        // The queue is completely empty. Let this thread exit, and another watchdog thread
                        // will be created the next time scheduleTimeout() is called.
                        if (timedOut == head) {
                            head = null;
                            return;
                        }
                    }

                    // Close the timed out node.
                    timedOut.timedOut();
                } catch (InterruptedException ignored) {
                    // Ignored, the loop will continue.
                }
            }
        }
    }

}
