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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Bounded multi-producer/single-writer HTTP/2 command scheduler.
 *
 * <p>
 * Control and DATA commands use separate sequence-published rings so control traffic cannot be starved by body traffic.
 * Producers fill command fields before release-publishing a slot; the sole writer acquires the sequence before reading
 * and processes bounded batches.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class Http2WriteScheduler implements AutoCloseable {

    /**
     * Structured DATA frame command routed through the data ring.
     */
    static final int DATA = 0;

    /**
     * Structured HEADERS command routed through the control ring.
     */
    static final int HEADERS = 1;

    /**
     * Structured WINDOW_UPDATE command routed through the control ring.
     */
    static final int WINDOW_UPDATE = 2;

    /**
     * Structured RST_STREAM command routed through the control ring.
     */
    static final int RST = 3;

    /**
     * SETTINGS acknowledgement command routed through the control ring.
     */
    static final int SETTINGS_ACK = 4;

    /**
     * PING acknowledgement command routed through the control ring.
     */
    static final int PING_ACK = 5;

    /**
     * GOAWAY command routed through the control ring.
     */
    static final int GOAWAY = 6;

    /**
     * Explicit flush command routed through the control ring.
     */
    static final int FLUSH = 7;

    /**
     * Writer close command routed through the control ring.
     */
    static final int CLOSE = 8;

    /**
     * Raw non-DATA HTTP/2 frame command used by the connection facade.
     */
    static final int FRAME = 9;

    /**
     * Raw DATA frame command routed through the bounded data ring.
     */
    static final int DATA_FRAME = 10;

    /**
     * Total preallocated command slots across the control and data rings.
     */
    private static final int TOTAL_CAPACITY = 4096;

    /**
     * Slots reserved exclusively for control traffic.
     */
    private static final int CONTROL_CAPACITY = 512;

    /**
     * Slots available exclusively to DATA traffic.
     */
    private static final int DATA_CAPACITY = TOTAL_CAPACITY - CONTROL_CAPACITY;

    /**
     * Maximum number of commands processed in one writer batch.
     */
    private static final int MAX_BATCH_COMMANDS = 64;

    /**
     * Maximum sum of payload bytes processed in one writer batch.
     */
    private static final long MAX_BATCH_BYTES = 64L * 1024L;

    /**
     * Maximum nanoseconds spent aggregating one writer batch or parking an idle writer.
     */
    private static final long MAX_BATCH_NANOS = 10_000L;

    /**
     * Maximum idle park; producers still wake the writer immediately.
     */
    private static final long IDLE_PARK_NANOS = 200_000L;

    /**
     * Short producer back-pressure park used only while its selected ring is temporarily full.
     */
    private static final long PRODUCER_PARK_NANOS = 10_000L;

    /**
     * Bounded MPSC ring reserved for control commands.
     */
    private final Ring control = new Ring(CONTROL_CAPACITY);

    /**
     * Bounded MPSC ring reserved for DATA commands.
     */
    private final Ring data = new Ring(DATA_CAPACITY);

    /**
     * Callback invoked only by the sole physical writer thread.
     */
    private final Handler handler;

    /**
     * Atomic guard refusing new publications and requesting writer shutdown.
     */
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Terminal connection abort skips draining commands whose transport no longer exists.
     */
    private final AtomicBoolean aborted = new AtomicBoolean();

    /**
     * Sole physical writer thread while the writer loop is active, otherwise null.
     */
    private volatile Thread writer;

    /**
     * Creates a scheduler.
     *
     * @param handler sole writer command handler
     */
    Http2WriteScheduler(final Handler handler) {
        if (handler == null) {
            throw new ValidateException("HTTP/2 scheduler handler must not be null");
        }
        this.handler = handler;
    }

    /**
     * Publishes a command. Payload ownership transfers only after successful publication.
     *
     * @param type     command type from {@link #DATA} through {@link #DATA_FRAME}
     * @param streamId non-negative stream identifier
     * @param flags    command-specific frame flags
     * @param value    command-specific scalar value
     * @param payload  producer buffer whose readable bytes transfer only on acceptance, or null
     * @return whether the bounded queue accepted the command
     */
    boolean offer(final int type, final int streamId, final int flags, final long value, final Buffer payload) {
        if (closed.get()) {
            return false;
        }
        if (type < DATA || type > DATA_FRAME || streamId < 0) {
            throw new ValidateException("Invalid HTTP/2 writer command");
        }
        final Buffer owned;
        if (payload == null || payload.size() == 0L) {
            owned = null;
        } else {
            owned = new Buffer();
            owned.write(payload, payload.size());
        }
        final Ring target = type == DATA || type == DATA_FRAME ? data : control;
        while (!closed.get()) {
            if (target.offer(type, streamId, flags, value, owned)) {
                final Thread currentWriter = writer;
                if (currentWriter != null) {
                    LockSupport.unpark(currentWriter);
                }
                return true;
            }
            final Thread currentWriter = writer;
            if (currentWriter != null) {
                LockSupport.unpark(currentWriter);
            }
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            LockSupport.parkNanos(this, PRODUCER_PARK_NANOS);
        }
        if (owned != null) {
            payload.write(owned, owned.size());
        }
        return false;
    }

    /**
     * Runs the sole writer loop until close is requested and both queues drain.
     */
    void runWriter() {
        if (writer != null) {
            throw new IllegalStateException("HTTP/2 writer already started");
        }
        writer = Thread.currentThread();
        try {
            while (!aborted.get() && (!closed.get() || !control.isEmpty() || !data.isEmpty())) {
                if (Thread.currentThread().isInterrupted()) {
                    closed.set(true);
                    break;
                }
                final int processed = drainBatch();
                if (processed == 0) {
                    LockSupport.parkNanos(this, IDLE_PARK_NANOS);
                }
            }
            if (!aborted.get() && !Thread.currentThread().isInterrupted()) {
                handler.flush();
            }
        } finally {
            writer = null;
        }
    }

    /**
     * Drains a bounded batch, always checking control traffic before DATA.
     *
     * @return processed command count
     */
    private int drainBatch() {
        final long deadline = System.nanoTime() + MAX_BATCH_NANOS;
        int commands = 0;
        long bytes = 0L;
        while (commands < MAX_BATCH_COMMANDS && bytes < MAX_BATCH_BYTES && System.nanoTime() <= deadline) {
            if (aborted.get() || Thread.currentThread().isInterrupted()) {
                break;
            }
            Slot slot = control.poll();
            Ring owner = control;
            if (slot == null) {
                slot = data.poll();
                owner = data;
            }
            if (slot == null) {
                if (commands == 0) {
                    break;
                }
                final long remaining = deadline - System.nanoTime();
                if (remaining <= 0L) {
                    break;
                }
                // A newly awakened writer commonly observes only the first producer publication. Keep the short
                // aggregation window open so concurrent stream HEADERS share one TLS record and one transport flush.
                LockSupport.parkNanos(this, remaining);
                continue;
            }
            final long payloadBytes = slot.payload == null ? 0L : slot.payload.size();
            try {
                handler.handle(slot.type, slot.streamId, slot.flags, slot.value, slot.payload);
            } catch (final RuntimeException failure) {
                closed.set(true);
                handler.failed(failure);
            } finally {
                owner.release(slot);
            }
            commands++;
            bytes += payloadBytes;
        }
        if (commands != 0) {
            handler.flush();
        }
        return commands;
    }

    /**
     * Refuses new commands and wakes the writer so it can drain both rings and stop.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            final Thread currentWriter = writer;
            if (currentWriter != null) {
                LockSupport.unpark(currentWriter);
            }
        }
    }

    /**
     * Stops the writer without draining commands after the owning physical connection is terminal.
     */
    void abort() {
        aborted.set(true);
        closed.set(true);
        final Thread currentWriter = writer;
        if (currentWriter != null) {
            currentWriter.interrupt();
            LockSupport.unpark(currentWriter);
        }
    }

    /**
     * Sole-writer callback contract for acquired commands and batch lifecycle events.
     */
    interface Handler {

        /**
         * Handles one acquired command before its slot is released.
         *
         * @param type     scheduler command type
         * @param streamId target stream identifier
         * @param flags    command-specific frame flags
         * @param value    command-specific scalar value
         * @param payload  transferred payload buffer, or null
         */
        void handle(int type, int streamId, int flags, long value, Buffer payload);

        /**
         * Flushes a non-empty completed writer batch or the final drained writer state.
         */
        void flush();

        /**
         * Receives a runtime failure thrown while handling an acquired command.
         *
         * @param failure command-handler failure
         */
        void failed(RuntimeException failure);
    }

    /**
     * Sequence-published bounded multi-producer/single-consumer ring.
     */
    private static final class Ring {

        /**
         * Atomic monotonic position claimed by competing producers.
         */
        private final AtomicLong producer = new AtomicLong();

        /**
         * Fixed command slots initialized with producer-available sequence values.
         */
        private final Slot[] slots;

        /**
         * Writer-thread-owned monotonic position of the next slot to consume.
         */
        private long consumer;

        /**
         * Creates and sequence-initializes a bounded ring.
         *
         * @param capacity positive number of preallocated slots
         */
        private Ring(final int capacity) {
            slots = new Slot[capacity];
            for (int index = 0; index < capacity; index++) {
                slots[index] = new Slot(index);
            }
        }

        /**
         * Attempts to reserve, populate, and release-publish one slot.
         *
         * @param type     scheduler command type
         * @param streamId target stream identifier
         * @param flags    command-specific frame flags
         * @param value    command-specific scalar value
         * @param payload  transferred payload buffer, or null
         * @return true when a producer claimed and published a slot; false when the ring is full
         */
        private boolean offer(
                final int type,
                final int streamId,
                final int flags,
                final long value,
                final Buffer payload) {
            for (;;) {
                final long position = producer.get();
                final Slot slot = slots[(int) (position % slots.length)];
                final long sequence = slot.sequenceAcquire();
                final long difference = sequence - position;
                if (difference == 0L) {
                    if (producer.compareAndSet(position, position + 1L)) {
                        slot.type = type;
                        slot.streamId = streamId;
                        slot.flags = flags;
                        slot.value = value;
                        slot.payload = payload;
                        slot.sequenceRelease(position + 1L);
                        return true;
                    }
                } else if (difference < 0L) {
                    return false;
                } else {
                    Thread.onSpinWait();
                }
            }
        }

        /**
         * Acquires the next published slot without advancing or releasing the consumer position.
         *
         * @return next published slot, or null when no command is currently available
         */
        private Slot poll() {
            final Slot slot = slots[(int) (consumer % slots.length)];
            return slot.sequenceAcquire() == consumer + 1L ? slot : null;
        }

        /**
         * Clears transferred references, release-publishes a reusable sequence, and advances the consumer.
         *
         * @param slot acquired slot at the current consumer position
         */
        private void release(final Slot slot) {
            slot.payload = null;
            slot.sequenceRelease(consumer + slots.length);
            consumer++;
        }

        /**
         * Returns whether the next consumer slot is not currently published.
         *
         * @return true when no command is immediately available
         */
        private boolean isEmpty() {
            return slots[(int) (consumer % slots.length)].sequenceAcquire() != consumer + 1L;
        }
    }

    /**
     * Preallocated command slot guarded by its own release/acquire publication sequence.
     */
    private static final class Slot {

        /**
         * Var-handle providing acquire and release access to {@link #sequence}.
         */
        private static final VarHandle SEQUENCE;

        static {
            try {
                SEQUENCE = MethodHandles.lookup().findVarHandle(Slot.class, "sequence", long.class);
            } catch (final ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        /**
         * Sequence value coordinating producer ownership, publication, consumption, and reuse.
         */
        private volatile long sequence;

        /**
         * Scheduler command type written before publication.
         */
        private int type;

        /**
         * Target stream identifier written before publication.
         */
        private int streamId;

        /**
         * Command-specific frame flags written before publication.
         */
        private int flags;

        /**
         * Command-specific scalar value written before publication.
         */
        private long value;

        /**
         * Payload buffer owned by the slot from publication through release, or null.
         */
        private Buffer payload;

        /**
         * Creates a slot with its initial producer-available sequence.
         *
         * @param sequence initial sequence matching the slot's first producer position
         */
        private Slot(final long sequence) {
            this.sequence = sequence;
        }

        /**
         * Reads the publication sequence with acquire semantics.
         *
         * @return current sequence value
         */
        private long sequenceAcquire() {
            return (long) SEQUENCE.getAcquire(this);
        }

        /**
         * Publishes a producer-ready or consumer-ready sequence with release semantics.
         *
         * @param value sequence value to publish
         */
        private void sequenceRelease(final long value) {
            SEQUENCE.setRelease(this, value);
        }
    }

}
