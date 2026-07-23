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

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;

/**
 * Connection-owned HTTP/2 frame batch writer.
 *
 * <p>
 * The sole writer thread emits the nine-byte frame header directly into one reusable batch. Payload writes transfer
 * segment ownership from their source buffer and a full batch is handed to the transport in one operation.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class Http2FrameWriter implements AutoCloseable {

    /**
     * Maximum accumulated batch size before an automatic transport write.
     */
    private static final long BATCH_LIMIT = 64L * 1024L;

    /**
     * Connection sink that receives emitted frame batches but remains externally owned.
     */
    private final Sink sink;

    /**
     * Reusable buffer accumulating complete encoded frames for one physical write.
     */
    private final Buffer batch = new Buffer();

    /**
     * Current peer-advertised maximum frame payload size.
     */
    private int maxFrameSize = Builder.HTTP2_DEFAULT_MAX_FRAME_SIZE;

    /**
     * Number of complete frames accumulated since the previous transport write.
     */
    private int frameCount;

    /**
     * Total number of non-empty batches written to the transport sink.
     */
    private long writeCount;

    /**
     * Writer-thread-confined guard preventing appends and flushes after successful closure.
     */
    private boolean closed;

    /**
     * Creates a writer for one connection.
     *
     * @param sink non-null externally owned transport sink for encoded batches
     */
    Http2FrameWriter(final Sink sink) {
        if (sink == null) {
            throw new ValidateException("HTTP/2 frame writer sink must not be null");
        }
        this.sink = sink;
    }

    /**
     * Updates the peer-advertised maximum frame payload.
     *
     * @param value peer-advertised payload limit from 16,384 through 16,777,215 bytes
     */
    void maxFrameSize(final int value) {
        if (value < Builder.HTTP2_DEFAULT_MAX_FRAME_SIZE || value > Builder.BYTES_16_MIB - Normal._1) {
            throw new ProtocolException("Invalid HTTP/2 maximum frame size");
        }
        maxFrameSize = value;
    }

    /**
     * Appends one frame and transfers its payload bytes.
     *
     * @param type     unsigned frame type byte
     * @param streamId non-negative stream identifier
     * @param flags    unsigned frame flags byte
     * @param payload  source buffer whose leading bytes are transferred, or null only for an empty frame
     * @param count    number of payload bytes to transfer, bounded by the peer frame-size limit
     * @throws IOException when an automatic batch emission fails
     */
    void writeFrame(final int type, final int streamId, final int flags, final Buffer payload, final long count)
            throws IOException {
        ensureOpen();
        if (type < 0 || type > 255 || flags < 0 || flags > 255 || streamId < 0 || (streamId & 0x80000000) != 0) {
            throw new ProtocolException("Invalid HTTP/2 frame header");
        }
        if (count < 0L || count > maxFrameSize || count > 0L && (payload == null || count > payload.size())) {
            throw new ProtocolException("Invalid HTTP/2 frame payload length");
        }
        if (batch.size() != 0L && batch.size() + Builder.HTTP2_FRAME_HEADER_BYTES + count > BATCH_LIMIT) {
            emit(false);
        }
        writeMedium(batch, (int) count);
        batch.writeByte(type);
        batch.writeByte(flags);
        batch.writeInt(streamId & 0x7fffffff);
        if (count != 0L) {
            batch.write(payload, count);
        }
        frameCount++;
        if (batch.size() >= BATCH_LIMIT || frameCount >= 64) {
            emit(false);
        }
    }

    /**
     * Appends a four-byte control-frame payload without allocating a temporary buffer.
     *
     * @param type     frame type whose low eight bits are written
     * @param streamId stream identifier whose reserved high bit is cleared
     * @param flags    frame flags whose low eight bits are written
     * @param value    four-byte network-order payload value
     * @throws IOException when the existing batch must be emitted and the transport write fails
     */
    void writeIntFrame(final int type, final int streamId, final int flags, final int value) throws IOException {
        ensureOpen();
        if (batch.size() + Builder.HTTP2_FRAME_HEADER_BYTES + Integer.BYTES > BATCH_LIMIT) {
            emit(false);
        }
        writeMedium(batch, Integer.BYTES);
        batch.writeByte(type);
        batch.writeByte(flags);
        batch.writeInt(streamId & 0x7fffffff);
        batch.writeInt(value);
        frameCount++;
    }

    /**
     * Flushes the current frame batch and the underlying transport.
     *
     * @throws IOException on transport failure
     */
    void flush() throws IOException {
        ensureOpen();
        emit(true);
    }

    /**
     * Returns the number of non-empty batches emitted to the transport.
     *
     * @return physical transport write count
     */
    long writeCount() {
        return writeCount;
    }

    /**
     * Returns the encoded bytes not yet emitted.
     *
     * @return current batch size in bytes
     */
    long batchBytes() {
        return batch.size();
    }

    /**
     * Writes the accumulated batch.
     *
     * @param flush whether to flush the underlying sink after writing any pending batch
     * @throws IOException when writing or flushing the transport fails
     */
    private void emit(final boolean flush) throws IOException {
        if (batch.size() != 0L) {
            sink.write(batch, batch.size());
            writeCount++;
            frameCount = 0;
        }
        if (flush) {
            sink.flush();
        }
    }

    /**
     * Writes an unsigned 24-bit frame length.
     *
     * @param target output batch receiving three big-endian bytes
     * @param value  non-negative frame length that fits in 24 bits
     */
    private static void writeMedium(final Buffer target, final int value) {
        target.writeByte(value >>> 16);
        target.writeByte(value >>> 8);
        target.writeByte(value);
    }

    /**
     * Rejects writer operations after successful closure.
     */
    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("HTTP/2 frame writer is closed");
        }
    }

    /**
     * Emits and flushes pending frames once while leaving closure of the externally owned sink to the connection.
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            emit(true);
            closed = true;
        }
    }

}
