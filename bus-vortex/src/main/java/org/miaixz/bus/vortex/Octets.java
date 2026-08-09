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
package org.miaixz.bus.vortex;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.miaixz.bus.vortex.guard.AsyncByteBudget;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides the single ownership boundary for reactive request and response body octets.
 * <p>
 * A body is aggregated only after its bytes have been charged to a shared {@link AsyncByteBudget}. Successfully
 * materialized bytes remain charged through the returned {@link BufferedBody} until it is closed.
 * </p>
 * <p>
 * The budget and lease implementations are thread-safe. This class does not retain source buffers, create a second
 * allocator or silently switch an atomic body to streaming when a limit is reached.
 * </p>
 *
 * @author Kimi Liu
 */
public final class Octets {

    /**
     * Maximum heap-backed chunk exposed to one downstream write operation.
     */
    private static final int WRITE_CHUNK_SIZE = 64 * 1024;

    /**
     * Direct write buffers share the single allocator used by all Vortex network channels.
     */
    private static final NettyDataBufferFactory WRITE_BUFFER_FACTORY = new NettyDataBufferFactory(Holder.allocator());

    /**
     * Restricts the class to static lifecycle operations.
     */
    private Octets() {
        throw new UnsupportedOperationException("Octets class cannot be instantiated");
    }

    /**
     * Reads a bounded response into exact-sized heap segments for later atomic relay.
     * <p>
     * A non-negative Content-Length is mandatory. Its exact logical size is acquired before source subscription and
     * remains owned by the returned body until that body is closed.
     *
     * @param body           source response buffers
     * @param maxBytes       per-body materialization limit
     * @param budget         process-wide response-byte budget
     * @param expectedLength declared Content-Length
     * @return buffered response with an attached logical-byte lease
     */
    public static Mono<BufferedBody> readForRelay(
            Flux<? extends DataBuffer> body,
            int maxBytes,
            AsyncByteBudget budget,
            long expectedLength) {
        Mono<Void> validation = validateKnownLength(body, maxBytes, budget, expectedLength);
        if (validation != null) {
            return validation.then(Mono.empty());
        }
        return awaitBufferingCapacity().then(budget.acquire(expectedLength))
                .timeout(Duration.ofSeconds(Holder.get().getBufferAcquireTimeoutSeconds()))
                .onErrorMap(
                        java.util.concurrent.TimeoutException.class,
                        error -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                "Buffered-byte capacity wait timed out", error))
                .flatMap(lease -> readReservedSegmented(body, maxBytes, expectedLength, lease));
    }

    /**
     * Reads a bounded body directly into one exact-sized array for parsing or transformation.
     * <p>
     * A non-negative Content-Length is mandatory. The exact logical size is acquired before source subscription, and
     * every source buffer is released immediately after its bytes have been copied.
     *
     * @param body           source body buffers
     * @param maxBytes       per-body materialization limit
     * @param budget         process-wide byte budget for this body category
     * @param expectedLength declared Content-Length
     * @return contiguous buffered body with an attached logical-byte lease
     */
    public static Mono<BufferedBody> readForParsing(
            Flux<? extends DataBuffer> body,
            int maxBytes,
            AsyncByteBudget budget,
            long expectedLength) {
        Mono<Void> validation = validateKnownLength(body, maxBytes, budget, expectedLength);
        if (validation != null) {
            return validation.then(Mono.empty());
        }
        return awaitBufferingCapacity().then(budget.acquire(expectedLength))
                .timeout(
                        Duration.ofSeconds(Holder.get().getBufferAcquireTimeoutSeconds()))
                .onErrorMap(
                        java.util.concurrent.TimeoutException.class,
                        error -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                "Buffered-byte capacity wait timed out", error))
                .flatMap(lease -> Mono.defer(() -> {
                    byte[] bytes = new byte[Math.toIntExact(expectedLength)];
                    int[] offset = new int[1];
                    return body.<Integer>handle((buffer, sink) -> {
                        try {
                            int readable = buffer.readableByteCount();
                            if (readable > bytes.length - offset[0]) {
                                sink.error(new DataBufferLimitException("Body exceeds declared Content-Length"));
                                return;
                            }
                            buffer.read(bytes, offset[0], readable);
                            offset[0] += readable;
                            sink.next(readable);
                        } finally {
                            release(buffer);
                        }
                    }).then(Mono.defer(() -> {
                        if (offset[0] != bytes.length) {
                            return Mono
                                    .error(new DataBufferLimitException("Body length does not match Content-Length"));
                        }
                        return Mono.just(new BufferedBody(bytes, lease));
                    })).doOnError(error -> lease.close()).doOnCancel(lease::close)
                            .doOnDiscard(BufferedBody.class, BufferedBody::close)
                            .doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release);
                }));
    }

    /**
     * Validates prerequisites before a budget is acquired or the source is subscribed.
     *
     * @param body           source body
     * @param maxBytes       positive per-body limit
     * @param budget         category-specific process-wide budget
     * @param expectedLength declared Content-Length
     * @return an error publisher when validation fails, or {@code null} when validation succeeds
     */
    private static Mono<Void> validateKnownLength(
            Flux<? extends DataBuffer> body,
            int maxBytes,
            AsyncByteBudget budget,
            long expectedLength) {
        if (body == null || budget == null || maxBytes <= 0) {
            return Mono.error(new IllegalArgumentException("body, positive maxBytes and budget are required"));
        }
        if (expectedLength < 0) {
            return Mono.error(
                    new ResponseStatusException(HttpStatus.LENGTH_REQUIRED, "Buffered bodies require Content-Length"));
        }
        if (expectedLength > maxBytes) {
            return Mono.error(new DataBufferLimitException("Exceeded buffered body limit of " + maxBytes + " bytes"));
        }
        return null;
    }

    /**
     * Waits asynchronously until sampled heap pressure permits new materialization.
     *
     * @return completion when buffering is currently allowed
     */
    private static Mono<Void> awaitBufferingCapacity() {
        if (!Holder.memoryPressure().rejectBuffering()) {
            return Mono.empty();
        }
        return Flux.interval(Duration.ofMillis(Holder.get().getMemorySampleIntervalMillis()))
                .filter(tick -> !Holder.memoryPressure().rejectBuffering()).next().then();
    }

    /**
     * Consumes a response into segments after its exact capacity has already been acquired.
     *
     * @param body           source response buffers
     * @param maxBytes       per-body materialization limit
     * @param expectedLength declared and reserved Content-Length
     * @param lease          exact response-byte lease
     * @return segmented buffered body that assumes ownership of the lease
     */
    private static Mono<BufferedBody> readReservedSegmented(
            Flux<? extends DataBuffer> body,
            int maxBytes,
            long expectedLength,
            AsyncByteBudget.Lease lease) {
        SegmentAccumulator accumulator = new SegmentAccumulator(expectedLength);
        return body.<Integer>handle((buffer, sink) -> {
            try {
                int bytes = buffer.readableByteCount();
                if ((long) accumulator.length() + bytes > expectedLength
                        || (long) accumulator.length() + bytes > maxBytes) {
                    sink.error(new DataBufferLimitException("Body exceeds declared Content-Length"));
                    return;
                }
                accumulator.append(buffer);
                sink.next(bytes);
            } finally {
                release(buffer);
            }
        }).then(Mono.defer(() -> {
            if (accumulator.length() != expectedLength) {
                return Mono.error(new DataBufferLimitException("Body length does not match Content-Length"));
            }
            return Mono.just(accumulator.body(lease));
        })).doOnError(error -> lease.close()).doOnCancel(lease::close)
                .doOnDiscard(BufferedBody.class, BufferedBody::close)
                .doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release);
    }

    /**
     * Collects a body into non-humongous heap segments while releasing every source buffer immediately.
     */
    private static final class SegmentAccumulator {

        /**
         * Exact-sized heap segments populated in source order.
         */
        private final List<byte[]> segments;

        /**
         * Declared body length used to size the final segment without over-allocation.
         */
        private final int expectedLength;

        /**
         * Number of source bytes copied so far.
         */
        private int length;

        /**
         * Creates an empty accumulator sized from a previously validated Content-Length.
         *
         * @param expectedLength exact number of bytes expected from the source
         */
        private SegmentAccumulator(long expectedLength) {
            this.expectedLength = Math.toIntExact(expectedLength);
            int capacity = expectedLength <= 0 ? 0
                    : Math.toIntExact((expectedLength + WRITE_CHUNK_SIZE - 1) / WRITE_CHUNK_SIZE);
            this.segments = new ArrayList<>(capacity);
        }

        /**
         * Copies one source buffer into exact-sized heap segments.
         *
         * @param buffer source buffer whose readable bytes are consumed
         */
        private void append(DataBuffer buffer) {
            int remaining = buffer.readableByteCount();
            while (remaining > 0) {
                int offset = this.length % WRITE_CHUNK_SIZE;
                if (offset == 0) {
                    this.segments.add(new byte[Math.min(WRITE_CHUNK_SIZE, this.expectedLength - this.length)]);
                }
                byte[] segment = this.segments.get(this.segments.size() - 1);
                int count = Math.min(remaining, segment.length - offset);
                buffer.read(segment, offset, count);
                this.length += count;
                remaining -= count;
            }
        }

        /**
         * Returns the number of source bytes copied into this accumulator.
         *
         * @return copied source bytes
         */
        private int length() {
            return this.length;
        }

        /**
         * Transfers the accumulated segments into a leased buffered body.
         *
         * @param reservation logical-byte lease acquired before source subscription
         * @return buffered body owning both segments and lease
         */
        private BufferedBody body(AutoCloseable reservation) {
            return new BufferedBody(this.segments.toArray(byte[][]::new), this.length, reservation);
        }
    }

    /**
     * Releases one buffer only when ownership is still active.
     *
     * @param buffer buffer to release
     */
    public static void release(DataBuffer buffer) {
        if (buffer instanceof PooledDataBuffer pooled && pooled.isAllocated()) {
            pooled.release();
        }
    }

    /**
     * Drains a body that cannot be forwarded and releases every received pooled buffer.
     *
     * @param body source body
     * @return completion after the source has been consumed
     */
    public static Mono<Void> discard(Flux<? extends DataBuffer> body) {
        if (body == null) {
            return Mono.empty();
        }
        return body.doOnNext(Octets::release).doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release).then();
    }

    /**
     * Exposes a buffered body as bounded direct chunks for a backpressure-aware network write.
     * <p>
     * Each heap segment is copied into a bounded direct buffer from the unified Vortex allocator. This avoids the JDK
     * NIO heap-to-direct temporary-buffer cache; the network write owns and reference-count releases each direct
     * buffer. This publisher does not close the body; use {@link #chunksAndClose(BufferedBody)} when transferring body
     * ownership to the network response.
     * </p>
     *
     * @param body buffered body
     * @return lazily generated direct write chunks
     */
    public static Flux<DataBuffer> chunks(BufferedBody body) {
        if (body == null) {
            return Flux.error(new IllegalArgumentException("body must not be null"));
        }
        byte[][] segments = body.segments();
        return Flux.<DataBuffer, int[]>generate(() -> new int[2], (cursor, sink) -> {
            if (cursor[0] >= segments.length) {
                sink.complete();
                return cursor;
            }
            byte[] segment = segments[cursor[0]];
            int length = Math.min(WRITE_CHUNK_SIZE, segment.length - cursor[1]);
            DataBuffer writeBuffer = WRITE_BUFFER_FACTORY.allocateBuffer(length);
            writeBuffer.write(segment, cursor[1], length);
            sink.next(writeBuffer);
            cursor[1] += length;
            if (cursor[1] >= segment.length) {
                cursor[0]++;
                cursor[1] = 0;
            }
            return cursor;
        }).doOnDiscard(PooledDataBuffer.class, DataBufferUtils::release);
    }

    /**
     * Transfers a buffered body to a network write and closes its logical-byte lease on every terminal signal.
     *
     * @param body buffered body whose ownership is transferred to the returned publisher
     * @return bounded direct-buffer chunks tied to the body lifecycle
     */
    public static Flux<DataBuffer> chunksAndClose(BufferedBody body) {
        return chunks(body).doFinally(signal -> body.close());
    }

    /**
     * Owns buffered bytes and their process-wide logical-byte lease until {@link #close()}.
     */
    public static final class BufferedBody implements AutoCloseable {

        /**
         * Current segmented representation; cleared after discard or close.
         */
        private volatile byte[][] segments;

        /**
         * Declared and verified logical body length.
         */
        private final int length;

        /**
         * Logical-byte lease retained while an equivalent representation remains live.
         */
        private final AutoCloseable reservation;

        /**
         * Lazily created contiguous representation for parsers; cleared after discard or close.
         */
        private volatile byte[] materialized;

        /**
         * Creates a body from already materialized bytes.
         *
         * @param bytes       materialized bytes
         * @param reservation process-wide logical-byte lease
         */
        public BufferedBody(byte[] bytes, AutoCloseable reservation) {
            if (bytes == null || reservation == null) {
                throw new IllegalArgumentException("buffered body fields must not be null");
            }
            this.segments = bytes.length == 0 ? new byte[0][] : new byte[][] { bytes };
            this.length = bytes.length;
            this.reservation = reservation;
            this.materialized = bytes;
        }

        /**
         * Creates a body from exact-sized relay segments.
         *
         * @param segments    ordered heap segments
         * @param length      declared and verified body length
         * @param reservation process-wide logical-byte lease
         */
        private BufferedBody(byte[][] segments, int length, AutoCloseable reservation) {
            if (segments == null || length < 0 || reservation == null) {
                throw new IllegalArgumentException("buffered body fields must not be null or negative");
            }
            this.segments = segments;
            this.length = length;
            this.reservation = reservation;
        }

        /**
         * Returns contiguous bytes, materializing them only for parsers that require an array.
         *
         * @return contiguous body bytes
         */
        public byte[] bytes() {
            byte[] result = this.materialized;
            if (result != null) {
                return result;
            }
            synchronized (this) {
                result = this.materialized;
                if (result == null) {
                    result = new byte[this.length];
                    int offset = 0;
                    for (byte[] segment : this.segments) {
                        int count = Math.min(segment.length, this.length - offset);
                        System.arraycopy(segment, 0, result, offset, count);
                        offset += count;
                    }
                    this.segments = this.length == 0 ? new byte[0][] : new byte[][] { result };
                    this.materialized = result;
                }
                return result;
            }
        }

        /**
         * Returns the declared and verified body length.
         *
         * @return exact body length
         */
        public int length() {
            return this.length;
        }

        /**
         * Returns the current relay segments.
         *
         * @return relay segments, valid only before discard or close
         */
        private byte[][] segments() {
            return this.segments;
        }

        /**
         * Drops this container's byte-array references while retaining the lease for an equivalent live representation.
         * <p>
         * This method is terminal for byte access: callers must not invoke {@link #bytes()} or relay chunks afterward.
         */
        public synchronized void discardBytes() {
            this.segments = null;
            this.materialized = null;
        }

        /**
         * Drops byte-array references and releases this body's process-wide budget ownership. Repeated calls are safe.
         */
        @Override
        public void close() {
            discardBytes();
            try {
                this.reservation.close();
            } catch (Exception ignored) {
                // Terminal cleanup must not mask the original reactive signal.
            }
        }
    }

}
