/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.fabric.protocol.http.http2;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.LongConsumer;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Bounded single-producer/single-consumer inbound DATA queue for one HTTP/2 stream.
 *
 * <p>
 * The connection reader transfers complete buffer segments into this queue. The request thread is the only consumer, so
 * neither side concurrently mutates the same {@link Buffer} chain. Published indexes use release/acquire ordering and
 * waiting uses a directed {@link LockSupport#unpark(Thread)} notification.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class Http2StreamBuffer {

    /**
     * Producer-index access used as the release/acquire publication barrier.
     */
    private static final VarHandle PRODUCER_INDEX;

    /**
     * Consumer-index access used when releasing queue slots back to the producer.
     */
    private static final VarHandle CONSUMER_INDEX;

    static {
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            PRODUCER_INDEX = lookup.findVarHandle(Http2StreamBuffer.class, "producerIndex", long.class);
            CONSUMER_INDEX = lookup.findVarHandle(Http2StreamBuffer.class, "consumerIndex", long.class);
        } catch (final ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Power-of-two ring whose occupied buffer slots are exclusively consumer-owned after publication.
     */
    private final Object[] slots;

    /**
     * Bit mask mapping monotonic producer and consumer positions to physical slots.
     */
    private final int mask;

    /**
     * Inclusive upper bound on DATA bytes published but not yet consumed.
     */
    private final long byteLimit;

    /**
     * Callback receiving batched byte counts for flow-control credit restoration.
     */
    private final LongConsumer consumed;

    /**
     * Connection-reader-owned monotonic position of the next publication.
     */
    private volatile long producerIndex;

    /**
     * Request-thread-owned monotonic position of the next slot to consume.
     */
    private volatile long consumerIndex;

    /**
     * Number of published DATA bytes not yet transferred to the application.
     */
    private final AtomicLong queuedBytes = new AtomicLong();

    /**
     * Request thread currently parked for input or terminal-state publication.
     */
    private volatile Thread waiter;

    /**
     * Runtime failure published by the connection reader, or null.
     */
    private volatile RuntimeException failure;

    /**
     * Whether the peer's clean END_STREAM has been published.
     */
    private volatile boolean finished;

    /**
     * Whether local cancellation has released queued data and stopped consumption.
     */
    private volatile boolean cancelled;

    /**
     * Application-consumed bytes accumulated since the previous flow-control callback.
     */
    private long unreportedConsumed;

    /**
     * Half-limit threshold, with a minimum of one byte, for reporting consumed credit.
     */
    private final long reportThreshold;

    /**
     * Creates a bounded stream buffer.
     *
     * @param slotCount power-of-two ring slot count
     * @param byteLimit positive retained-byte limit
     * @param consumed  consumed-byte callback
     */
    Http2StreamBuffer(final int slotCount, final long byteLimit, final LongConsumer consumed) {
        if (slotCount < 2 || Integer.bitCount(slotCount) != 1) {
            throw new ValidateException("HTTP/2 stream buffer slots must be a power of two greater than one");
        }
        if (byteLimit <= 0L) {
            throw new ValidateException("HTTP/2 stream buffer byte limit must be positive");
        }
        if (consumed == null) {
            throw new ValidateException("HTTP/2 stream buffer consumed callback must not be null");
        }
        this.slots = new Object[slotCount];
        this.mask = slotCount - 1;
        this.byteLimit = byteLimit;
        this.consumed = consumed;
        this.reportThreshold = Math.max(1L, byteLimit >>> 1);
    }

    /**
     * Transfers readable bytes from the reader-owned source into the queue.
     *
     * @param source source whose segment ownership is transferred
     * @param count  bytes to publish
     * @return {@code true} when published, or {@code false} when the queue is full or terminal
     */
    boolean offer(final Buffer source, final long count) {
        if (source == null || count < 0L || count > source.size()) {
            throw new ValidateException("Invalid HTTP/2 DATA publication count");
        }
        if (count == 0L) {
            return !isTerminal();
        }
        final long producer = producerIndex;
        final long consumer = (long) CONSUMER_INDEX.getAcquire(this);
        if (isTerminal() || producer - consumer >= slots.length || queuedBytes.get() + count > byteLimit) {
            return false;
        }
        final Buffer owned = new Buffer();
        owned.write(source, count);
        slots[(int) producer & mask] = owned;
        queuedBytes.addAndGet(count);
        PRODUCER_INDEX.setRelease(this, producer + 1L);
        signalWaiter();
        return true;
    }

    /**
     * Publishes an immutable DATA frame without constructing an intermediate reader buffer.
     *
     * @param data frame payload
     * @return {@code true} when published, or {@code false} when the queue is full or terminal
     */
    boolean offer(final ByteString data) {
        if (data == null) {
            throw new ValidateException("HTTP/2 DATA payload must not be null");
        }
        final int count = data.size();
        if (count == 0) {
            return !isTerminal();
        }
        final long producer = producerIndex;
        final long consumer = (long) CONSUMER_INDEX.getAcquire(this);
        if (isTerminal() || producer - consumer >= slots.length || queuedBytes.get() + count > byteLimit) {
            return false;
        }
        slots[(int) producer & mask] = data;
        queuedBytes.addAndGet(count);
        PRODUCER_INDEX.setRelease(this, producer + 1L);
        signalWaiter();
        return true;
    }

    /**
     * Reads published bytes, waiting until data, terminal state, or interruption.
     *
     * @param target destination buffer
     * @param limit  maximum bytes to read
     * @return bytes read, or {@code -1} at a clean end of stream
     * @throws IOException when the wait is interrupted or the stream fails
     */
    long read(final Buffer target, final long limit) throws IOException {
        if (target == null || limit < 0L) {
            throw new ValidateException("Invalid HTTP/2 stream read request");
        }
        if (limit == 0L) {
            return 0L;
        }
        for (;;) {
            final long consumer = consumerIndex;
            final long producer = (long) PRODUCER_INDEX.getAcquire(this);
            if (consumer != producer) {
                final int slot = (int) consumer & mask;
                final Object value = slots[slot];
                final long available = value instanceof Buffer owned ? owned.size() : ((ByteString) value).size();
                final long count = Math.min(limit, available);
                final boolean exhausted;
                if (value instanceof Buffer owned) {
                    target.write(owned, count);
                    exhausted = owned.size() == 0L;
                } else {
                    final ByteString bytes = (ByteString) value;
                    if (count == available) {
                        bytes.write(target);
                        exhausted = true;
                    } else {
                        bytes.substring(0, (int) count).write(target);
                        slots[slot] = bytes.substring((int) count);
                        exhausted = false;
                    }
                }
                queuedBytes.addAndGet(-count);
                reportConsumed(count, false);
                if (exhausted) {
                    slots[slot] = null;
                    CONSUMER_INDEX.setRelease(this, consumer + 1L);
                }
                return count;
            }
            final RuntimeException problem = failure;
            if (problem != null) {
                throw new SocketException("HTTP/2 stream failed", problem);
            }
            if (finished || cancelled) {
                // The producer can publish the final DATA slot immediately before the independent terminal flag.
                // Re-acquire its index after observing that flag so a consumer that raced both writes cannot report
                // EOF from an index value read before the terminal flag established the producer's prior writes.
                if (consumerIndex != (long) PRODUCER_INDEX.getAcquire(this)) {
                    continue;
                }
                reportConsumed(0L, true);
                return -1L;
            }
            waiter = Thread.currentThread();
            if (consumerIndex == (long) PRODUCER_INDEX.getAcquire(this) && !isTerminal()) {
                LockSupport.park(this);
            }
            waiter = null;
            if (Thread.interrupted()) {
                throw new IOException("Interrupted while reading HTTP/2 stream");
            }
        }
    }

    /**
     * Publishes a clean remote end-of-stream and wakes the waiting request thread.
     */
    void finish() {
        finished = true;
        signalWaiter();
    }

    /**
     * Publishes a terminal stream failure.
     *
     * @param problem non-null failure
     */
    void fail(final RuntimeException problem) {
        if (problem == null) {
            throw new ValidateException("HTTP/2 stream failure must not be null");
        }
        failure = problem;
        signalWaiter();
    }

    /**
     * Publishes local cancellation, releases all queued buffer references, and wakes the waiter.
     */
    void cancel() {
        cancelled = true;
        clear();
        signalWaiter();
    }

    /**
     * Returns the currently retained body-byte count.
     *
     * @return published bytes not yet consumed or cleared
     */
    long queuedBytes() {
        return queuedBytes.get();
    }

    /**
     * Returns whether any clean, cancelled, or failed terminal state has been published.
     *
     * @return true after finish, cancellation, or failure
     */
    boolean isTerminal() {
        return finished || cancelled || failure != null;
    }

    /**
     * Clears all published slots from the consumer side and flushes pending consumed-byte credit.
     */
    private void clear() {
        final long producer = (long) PRODUCER_INDEX.getAcquire(this);
        // Cancellation runs on the connection reader and may race the request-side consumer. Never chase a moving
        // index with an unbounded inequality loop: clear the fixed ring directly and advance monotonically.
        java.util.Arrays.fill(slots, null);
        queuedBytes.set(0L);
        CONSUMER_INDEX.setRelease(this, Math.max(consumerIndex, producer));
        reportConsumed(0L, true);
    }

    /**
     * Batches flow-control credit notifications.
     *
     * @param count newly consumed bytes
     * @param force whether to flush a partial batch
     */
    private void reportConsumed(final long count, final boolean force) {
        unreportedConsumed += count;
        if (unreportedConsumed >= reportThreshold || force && unreportedConsumed != 0L) {
            final long report = unreportedConsumed;
            unreportedConsumed = 0L;
            consumed.accept(report);
        }
    }

    /**
     * Unparks only the request thread currently registered as the queue waiter.
     */
    private void signalWaiter() {
        final Thread thread = waiter;
        if (thread != null) {
            LockSupport.unpark(thread);
        }
    }

}
