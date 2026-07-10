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
package org.miaixz.bus.core.io.buffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritePendingException;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * Queue-backed output stream that writes {@link SliceBuffer} slices asynchronously.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WriteBuffer extends OutputStream {

    /**
     * Pending write slices.
     */
    private final SliceBuffer[] pendingSlices;

    /**
     * Slab used for slice allocation.
     */
    private final SlabBuffer slabBuffer;

    /**
     * Consumer that starts a physical write.
     */
    private final Consumer<SliceBuffer> writeConsumer;

    /**
     * Default chunk size.
     */
    private final int chunkSize;

    /**
     * Guard that allows only one active physical write.
     */
    private final Semaphore semaphore = new Semaphore(1);

    /**
     * Queue take index.
     */
    private int takeIndex;

    /**
     * Queue put index.
     */
    private int putIndex;

    /**
     * Number of queued chunks.
     */
    private int count;

    /**
     * Current application write slice.
     */
    private SliceBuffer currentWriteSliceBuffer;

    /**
     * Whether this stream is closed.
     */
    private boolean closed;

    /**
     * Cache for primitive writes.
     */
    private byte[] cacheByte;

    /**
     * Completion callback for asynchronous write calls.
     */
    private Consumer<WriteBuffer> completionConsumer;

    /**
     * Creates a write buffer.
     *
     * @param slabBuffer    the allocation slab
     * @param writeConsumer the physical write starter
     * @param chunkSize     the default chunk size
     * @param capacity      the queue capacity
     */
    public WriteBuffer(SlabBuffer slabBuffer, Consumer<SliceBuffer> writeConsumer, int chunkSize, int capacity) {
        this.slabBuffer = slabBuffer;
        this.writeConsumer = writeConsumer;
        this.pendingSlices = new SliceBuffer[capacity];
        this.chunkSize = chunkSize;
    }

    /**
     * Writes one byte.
     *
     * @param value the byte value
     */
    @Override
    public void write(int value) {
        writeByte((byte) value);
    }

    /**
     * Writes a two-byte big-endian short.
     *
     * @param value the short value
     * @throws IOException if writing fails
     */
    public void writeShort(short value) throws IOException {
        initCacheBytes();
        cacheByte[0] = (byte) ((value >>> 8) & 0xff);
        cacheByte[1] = (byte) (value & 0xff);
        write(cacheByte, 0, 2);
    }

    /**
     * Writes one byte.
     *
     * @param value the byte value
     */
    public synchronized void writeByte(byte value) {
        if (currentWriteSliceBuffer == null) {
            currentWriteSliceBuffer = slabBuffer.allocate(chunkSize);
        }
        currentWriteSliceBuffer.buffer().put(value);
        flushWriteBuffer(false);
    }

    /**
     * Flushes the current slice when it is full or forced.
     *
     * @param forceFlush whether to flush a non-full chunk
     */
    private void flushWriteBuffer(boolean forceFlush) {
        if (!forceFlush && currentWriteSliceBuffer.buffer().hasRemaining()) {
            return;
        }
        currentWriteSliceBuffer.buffer().flip();
        SliceBuffer sliceBuffer = currentWriteSliceBuffer;
        currentWriteSliceBuffer = null;
        if (count == 0 && semaphore.tryAcquire()) {
            writeConsumer.accept(sliceBuffer);
            return;
        }
        try {
            while (count == pendingSlices.length) {
                wait();
                if (closed) {
                    sliceBuffer.release();
                    return;
                }
            }
            pendingSlices[putIndex] = sliceBuffer;
            if (++putIndex == pendingSlices.length) {
                putIndex = 0;
            }
            count++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } finally {
            flush();
        }
    }

    /**
     * Writes a four-byte big-endian integer.
     *
     * @param value the integer value
     * @throws IOException if writing fails
     */
    public void writeInt(int value) throws IOException {
        initCacheBytes();
        cacheByte[0] = (byte) ((value >>> 24) & 0xff);
        cacheByte[1] = (byte) ((value >>> 16) & 0xff);
        cacheByte[2] = (byte) ((value >>> 8) & 0xff);
        cacheByte[3] = (byte) (value & 0xff);
        write(cacheByte, 0, 4);
    }

    /**
     * Writes an eight-byte big-endian long.
     *
     * @param value the long value
     * @throws IOException if writing fails
     */
    public void writeLong(long value) throws IOException {
        initCacheBytes();
        cacheByte[0] = (byte) ((value >>> 56) & 0xff);
        cacheByte[1] = (byte) ((value >>> 48) & 0xff);
        cacheByte[2] = (byte) ((value >>> 40) & 0xff);
        cacheByte[3] = (byte) ((value >>> 32) & 0xff);
        cacheByte[4] = (byte) ((value >>> 24) & 0xff);
        cacheByte[5] = (byte) ((value >>> 16) & 0xff);
        cacheByte[6] = (byte) ((value >>> 8) & 0xff);
        cacheByte[7] = (byte) (value & 0xff);
        write(cacheByte, 0, 8);
    }

    /**
     * Writes bytes into this buffer.
     *
     * @param bytes  the source bytes
     * @param offset the source offset
     * @param length the byte count
     * @throws IOException if this buffer is closed
     */
    @Override
    public synchronized void write(byte[] bytes, int offset, int length) throws IOException {
        if (length == 0) {
            return;
        }
        if (currentWriteSliceBuffer == null) {
            if (chunkSize >= length) {
                currentWriteSliceBuffer = slabBuffer.allocate(chunkSize);
            } else {
                int remainder = length % chunkSize;
                currentWriteSliceBuffer = slabBuffer.allocate(remainder == 0 ? length : length + chunkSize - remainder);
            }
        }
        ByteBuffer writeBuffer = currentWriteSliceBuffer.buffer();
        if (closed) {
            currentWriteSliceBuffer.release();
            currentWriteSliceBuffer = null;
            throw new IOException("writeBuffer has closed");
        }
        int remaining = writeBuffer.remaining();
        if (remaining > length) {
            writeBuffer.put(bytes, offset, length);
            return;
        }
        writeBuffer.put(bytes, offset, remaining);
        flushWriteBuffer(true);
        if (length > remaining) {
            write(bytes, offset + remaining, length - remaining);
        }
    }

    /**
     * Writes bytes and registers a completion callback.
     *
     * @param bytes    the source bytes
     * @param offset   the source offset
     * @param length   the byte count
     * @param consumer the completion callback
     * @throws IOException if writing fails
     */
    public synchronized void write(byte[] bytes, int offset, int length, Consumer<WriteBuffer> consumer)
            throws IOException {
        if (completionConsumer != null) {
            throw new WritePendingException();
        }
        completionConsumer = consumer;
        write(bytes, offset, length);
        flush();
    }

    /**
     * Writes bytes and registers a completion callback.
     *
     * @param bytes    the source bytes
     * @param consumer the completion callback
     * @throws IOException if writing fails
     */
    public synchronized void write(byte[] bytes, Consumer<WriteBuffer> consumer) throws IOException {
        write(bytes, 0, bytes.length, consumer);
    }

    /**
     * Transfers a {@link ByteBuffer} into the asynchronous write queue.
     *
     * @param byteBuffer the source buffer
     * @param consumer   the completion callback
     * @throws IOException if writing fails
     */
    public synchronized void transferFrom(ByteBuffer byteBuffer, Consumer<WriteBuffer> consumer) throws IOException {
        if (!byteBuffer.hasRemaining()) {
            throw new IllegalStateException("none remaining in byteBuffer");
        }
        if (currentWriteSliceBuffer != null && currentWriteSliceBuffer.buffer().position() > 0) {
            flushWriteBuffer(true);
        }
        if (completionConsumer != null) {
            throw new WritePendingException();
        }
        if (currentWriteSliceBuffer != null && currentWriteSliceBuffer.buffer().position() > 0) {
            throw new IllegalStateException("currentWriteSliceBuffer should be null or empty");
        }
        completionConsumer = consumer;
        SliceBuffer wrappedSlice = SliceBuffer.wrap(byteBuffer);
        if (count == 0 && semaphore.tryAcquire()) {
            writeConsumer.accept(wrappedSlice);
            return;
        }
        try {
            while (count == pendingSlices.length) {
                wait();
                if (closed) {
                    return;
                }
            }
            pendingSlices[putIndex] = wrappedSlice;
            if (++putIndex == pendingSlices.length) {
                putIndex = 0;
            }
            count++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } finally {
            flush();
        }
    }

    /**
     * Initializes the primitive write cache.
     */
    private void initCacheBytes() {
        if (cacheByte == null) {
            cacheByte = new byte[8];
        }
    }

    /**
     * Starts a pending physical write when possible.
     */
    @Override
    public void flush() {
        if (closed) {
            throw new IllegalStateException("OutputStream has been closed");
        }
        if (semaphore.tryAcquire()) {
            SliceBuffer sliceBuffer = poll();
            if (sliceBuffer == null) {
                semaphore.release();
            } else {
                writeConsumer.accept(sliceBuffer);
            }
        }
    }

    /**
     * Closes this write buffer and releases pending slices.
     */
    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        flush();
        closed = true;
        if (currentWriteSliceBuffer != null) {
            currentWriteSliceBuffer.release();
            currentWriteSliceBuffer = null;
        }
        SliceBuffer pendingSlice;
        while ((pendingSlice = poll()) != null) {
            pendingSlice.release();
        }
    }

    /**
     * Returns whether no bytes are pending.
     *
     * @return {@code true} if no bytes are pending
     */
    public boolean isEmpty() {
        return count == 0 && (currentWriteSliceBuffer == null || currentWriteSliceBuffer.buffer().position() == 0);
    }

    /**
     * Marks the active physical write as finished.
     */
    public void finishWrite() {
        semaphore.release();
    }

    /**
     * Polls the next queued slice.
     *
     * @return the next slice, or {@code null}
     */
    private SliceBuffer pollItem() {
        if (count == 0) {
            if (completionConsumer != null) {
                Consumer<WriteBuffer> consumer = completionConsumer;
                completionConsumer = null;
                consumer.accept(this);
            }
            return null;
        }
        SliceBuffer pendingSlice = pendingSlices[takeIndex];
        pendingSlices[takeIndex] = null;
        if (++takeIndex == pendingSlices.length) {
            takeIndex = 0;
        }
        if (count-- == pendingSlices.length) {
            notifyAll();
        }
        return pendingSlice;
    }

    /**
     * Polls a slice for physical writing.
     *
     * @return the next slice, or {@code null}
     */
    public synchronized SliceBuffer poll() {
        SliceBuffer item = pollItem();
        if (item != null) {
            return item;
        }
        if (currentWriteSliceBuffer != null && currentWriteSliceBuffer.buffer().position() > 0) {
            currentWriteSliceBuffer.buffer().flip();
            SliceBuffer sliceBuffer = currentWriteSliceBuffer;
            currentWriteSliceBuffer = null;
            return sliceBuffer;
        }
        return null;
    }

}
