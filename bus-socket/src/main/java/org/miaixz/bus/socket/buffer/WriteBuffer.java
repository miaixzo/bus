/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.buffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritePendingException;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * Wraps the virtual buffer allocated to the current session, providing a stream-based interface for writing data.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class WriteBuffer extends OutputStream {

    /**
     * An array to store virtual buffers that are ready to be written.
     */
    private final VirtualBuffer[] items;

    /**
     * The buffer page that provides the underlying memory for this WriteBuffer.
     */
    private final BufferPage bufferPage;
    /**
     * A consumer function that is called to flush the buffer data.
     */
    private final Consumer<VirtualBuffer> writeConsumer;
    /**
     * The default size of memory chunks allocated from the buffer page.
     */
    private final int chunkSize;
    /**
     * A semaphore to prevent concurrent write operations, which could lead to exceptions.
     */
    private final Semaphore semaphore = new Semaphore(1);
    /**
     * The read index for the {@code items} array, used for polling buffers.
     */
    private int takeIndex;
    /**
     * The write index for the {@code items} array, used for adding new buffers.
     */
    private int putIndex;
    /**
     * The number of virtual buffers currently stored in the {@code items} array.
     */
    private int count;
    /**
     * A temporary buffer to hold data currently being written by the application. It is enqueued to {@code items} when
     * full or flushed.
     */
    private VirtualBuffer writeInBuf;
    /**
     * A flag indicating whether this WriteBuffer has been closed.
     */
    private boolean closed = false;
    /**
     * A cache for writing small data types (up to 8 bytes) to avoid repeated array allocation.
     */
    private byte[] cacheByte;
    /**
     * A consumer that is notified upon completion of an asynchronous write operation.
     */
    private Consumer<WriteBuffer> completionConsumer;

    /**
     * Constructs a new WriteBuffer.
     *
     * @param bufferPage    the buffer page for memory allocation
     * @param writeConsumer the consumer to handle the actual writing of the buffer
     * @param chunkSize     the default size for allocated buffer chunks
     * @param capacity      the capacity of the internal buffer queue
     */
    public WriteBuffer(BufferPage bufferPage, Consumer<VirtualBuffer> writeConsumer, int chunkSize, int capacity) {
        this.bufferPage = bufferPage;
        this.writeConsumer = writeConsumer;
        this.items = new VirtualBuffer[capacity];
        this.chunkSize = chunkSize;
    }

    /**
     * Writes the specified byte to this output stream. As per the {@link OutputStream#write(int)} contract, the byte to
     * be written is the eight low-order bits of the argument {@code b}. The 24 high-order bits of {@code b} are
     * ignored. This can be ambiguous, so it is recommended to use {@link #writeByte(byte)} instead.
     *
     * @param b the {@code byte} to write.
     */
    @Override
    public void write(int b) {
        writeByte((byte) b);
    }

    /**
     * Writes a short value to the output stream.
     *
     * @param v the short value
     * @throws IOException if an I/O error occurs
     */
    public void writeShort(short v) throws IOException {
        initCacheBytes();
        cacheByte[0] = (byte) ((v >>> 8) & 0xFF);
        cacheByte[1] = (byte) (v & 0xFF);
        write(cacheByte, 0, 2);
    }

    /**
     * Writes a single byte to the buffer.
     *
     * @param b the byte to be written
     * @see #write(int)
     */
    public synchronized void writeByte(byte b) {
        if (writeInBuf == null) {
            writeInBuf = bufferPage.allocate(chunkSize);
        }
        writeInBuf.buffer().put(b);
        flushWriteBuffer(false);
    }

    /**
     * Flushes the internal write buffer if it is full or if forced.
     *
     * @param forceFlush if true, the buffer is flushed regardless of whether it is full
     */
    private void flushWriteBuffer(boolean forceFlush) {
        if (!forceFlush && writeInBuf.buffer().hasRemaining()) {
            return;
        }
        writeInBuf.buffer().flip();
        VirtualBuffer virtualBuffer = writeInBuf;
        writeInBuf = null;
        if (count == 0 && semaphore.tryAcquire()) {
            writeConsumer.accept(virtualBuffer);
            return;
        }

        try {
            while (count == items.length) {
                this.wait();
                // Prevent memory leaks if closed while waiting
                if (closed) {
                    virtualBuffer.clean();
                    return;
                }
            }

            items[putIndex] = virtualBuffer;
            if (++putIndex == items.length) {
                putIndex = 0;
            }
            count++;
        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
        } finally {
            flush();
        }
    }

    /**
     * Writes an integer value to the output stream (4 bytes).
     *
     * @param v the integer value
     * @throws IOException if an I/O error occurs
     */
    public void writeInt(int v) throws IOException {
        initCacheBytes();
        cacheByte[0] = (byte) ((v >>> 24) & 0xFF);
        cacheByte[1] = (byte) ((v >>> 16) & 0xFF);
        cacheByte[2] = (byte) ((v >>> 8) & 0xFF);
        cacheByte[3] = (byte) (v & 0xFF);
        write(cacheByte, 0, 4);
    }

    /**
     * Writes a long value to the output stream (8 bytes).
     *
     * @param v the long value
     * @throws IOException if an I/O error occurs
     */
    public void writeLong(long v) throws IOException {
        initCacheBytes();
        cacheByte[0] = (byte) ((v >>> 56) & 0xFF);
        cacheByte[1] = (byte) ((v >>> 48) & 0xFF);
        cacheByte[2] = (byte) ((v >>> 40) & 0xFF);
        cacheByte[3] = (byte) ((v >>> 32) & 0xFF);
        cacheByte[4] = (byte) ((v >>> 24) & 0xFF);
        cacheByte[5] = (byte) ((v >>> 16) & 0xFF);
        cacheByte[6] = (byte) ((v >>> 8) & 0xFF);
        cacheByte[7] = (byte) (v & 0xFF);
        write(cacheByte, 0, 8);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        if (writeInBuf == null) {
            if (chunkSize >= len) {
                writeInBuf = bufferPage.allocate(chunkSize);
            } else {
                int m = len % chunkSize;
                writeInBuf = bufferPage.allocate(m == 0 ? len : len + chunkSize - m);
            }
        }
        ByteBuffer writeBuffer = writeInBuf.buffer();
        if (closed) {
            writeInBuf.clean();
            writeInBuf = null;
            throw new IOException("writeBuffer has closed");
        }
        int remaining = writeBuffer.remaining();
        if (remaining > len) {
            writeBuffer.put(b, off, len);
        } else {
            writeBuffer.put(b, off, remaining);
            flushWriteBuffer(true);
            if (len > remaining) {
                write(b, off + remaining, len - remaining);
            }
        }
    }

    /**
     * Performs an asynchronous write operation. This method writes the specified byte array asynchronously and notifies
     * the provided consumer upon completion.
     *
     * @param bytes    the byte array to be written
     * @param offset   the offset within the byte array to start writing from
     * @param len      the number of bytes to write
     * @param consumer the consumer to be called upon completion of the write operation
     * @throws IOException           if an I/O error occurs during the write
     * @throws WritePendingException if another write operation is already pending
     */
    public synchronized void write(byte[] bytes, int offset, int len, Consumer<WriteBuffer> consumer)
            throws IOException {
        if (completionConsumer != null) {
            throw new WritePendingException();
        }
        this.completionConsumer = consumer;
        write(bytes, offset, len);
        flush();
    }

    /**
     * Performs an asynchronous write of the entire byte array.
     *
     * @param bytes    the byte array to write
     * @param consumer the consumer to be called upon completion
     * @throws IOException if an I/O error occurs
     */
    public synchronized void write(byte[] bytes, Consumer<WriteBuffer> consumer) throws IOException {
        write(bytes, 0, bytes.length, consumer);
    }

    /**
     * Transfers data from a {@link ByteBuffer} to this WriteBuffer asynchronously.
     *
     * @param byteBuffer the source ByteBuffer
     * @param consumer   the consumer to be called upon completion
     * @throws IOException if an I/O error occurs
     */
    public synchronized void transferFrom(ByteBuffer byteBuffer, Consumer<WriteBuffer> consumer) throws IOException {
        if (!byteBuffer.hasRemaining()) {
            throw new IllegalStateException("none remaining in byteBuffer");
        }
        if (writeInBuf != null && writeInBuf.buffer().position() > 0) {
            flushWriteBuffer(true);
        }
        if (completionConsumer != null) {
            throw new WritePendingException();
        }
        if (writeInBuf != null && writeInBuf.buffer().position() > 0) {
            throw new IllegalStateException("writeInBuf should be null or empty");
        }
        this.completionConsumer = consumer;
        VirtualBuffer wrap = VirtualBuffer.wrap(byteBuffer);
        if (count == 0 && semaphore.tryAcquire()) {
            writeConsumer.accept(wrap);
            return;
        }
        try {
            while (count == items.length) {
                this.wait();
                // Prevent memory leaks if closed while waiting
                if (closed) {
                    return;
                }
            }

            items[putIndex] = wrap;
            if (++putIndex == items.length) {
                putIndex = 0;
            }
            count++;
        } catch (InterruptedException e1) {
            throw new RuntimeException(e1);
        } finally {
            flush();
        }
    }

    /**
     * Initializes the 8-byte cache for small primitive writes.
     */
    private void initCacheBytes() {
        if (cacheByte == null) {
            cacheByte = new byte[8];
        }
    }

    @Override
    public void flush() {
        if (closed) {
            throw new RuntimeException("OutputStream has been closed");
        }
        if (semaphore.tryAcquire()) {
            VirtualBuffer virtualBuffer = poll();
            if (virtualBuffer == null) {
                semaphore.release();
            } else {
                writeConsumer.accept(virtualBuffer);
            }
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        flush();
        closed = true;
        if (writeInBuf != null) {
            writeInBuf.clean();
            writeInBuf = null;
        }
        VirtualBuffer byteBuf;
        while ((byteBuf = poll()) != null) {
            byteBuf.clean();
        }
    }

    /**
     * Checks if there is any pending data to be written.
     *
     * @return {@code true} if there is no pending data, {@code false} otherwise
     */
    public boolean isEmpty() {
        return count == 0 && (writeInBuf == null || writeInBuf.buffer().position() == 0);
    }

    /**
     * Signals that a write operation has finished, releasing the semaphore to allow the next write.
     */
    public void finishWrite() {
        semaphore.release();
    }

    /**
     * Polls a single item from the internal queue.
     *
     * @return the polled {@link VirtualBuffer}, or {@code null} if the queue is empty
     */
    private VirtualBuffer pollItem() {
        if (count == 0) {
            if (completionConsumer != null) {
                Consumer<WriteBuffer> consumer = completionConsumer;
                this.completionConsumer = null;
                consumer.accept(this);
            }
            return null;
        }
        VirtualBuffer x = items[takeIndex];
        items[takeIndex] = null;
        if (++takeIndex == items.length) {
            takeIndex = 0;
        }
        if (count-- == items.length) {
            this.notifyAll();
        }
        return x;
    }

    /**
     * Retrieves and removes the head of this buffer's queue of data to be written.
     *
     * @return the {@link VirtualBuffer} to be written, or {@code null} if there is no data
     */
    public synchronized VirtualBuffer poll() {
        VirtualBuffer item = pollItem();
        if (item != null) {
            return item;
        }
        if (writeInBuf != null && writeInBuf.buffer().position() > 0) {
            writeInBuf.buffer().flip();
            VirtualBuffer buffer = writeInBuf;
            writeInBuf = null;
            return buffer;
        } else {
            return null;
        }
    }

}
