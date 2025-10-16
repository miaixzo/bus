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
package org.miaixz.bus.socket.accord;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.socket.*;
import org.miaixz.bus.socket.buffer.BufferPage;
import org.miaixz.bus.socket.buffer.VirtualBuffer;
import org.miaixz.bus.socket.buffer.WriteBuffer;
import org.miaixz.bus.socket.metric.channel.AsynchronousChannelProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Represents a TCP session for AIO (Asynchronous I/O).
 * <p>
 * This class is the core component, encapsulating the {@link AsynchronousSocketChannel} API to simplify I/O operations.
 * The public API for users includes methods for managing the session lifecycle, accessing addresses, and handling data
 * streams.
 * </p>
 * Publicly accessible methods include:
 * <ol>
 * <li>{@link #close()}
 * <li>{@link #close(boolean)}
 * <li>{@link #getAttachment()}
 * <li>{@link #getInputStream()}
 * <li>{@link #getInputStream(int)}
 * <li>{@link #getLocalAddress()}
 * <li>{@link #getRemoteAddress()}
 * <li>{@link #getSessionID()}
 * <li>{@link #isInvalid()}
 * <li>{@link #setAttachment(Object)}
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TcpSession extends Session {

    /**
     * The underlying asynchronous socket channel for communication.
     */
    private final AsynchronousSocketChannel channel;
    /**
     * The buffer for handling outgoing data.
     */
    private final WriteBuffer byteBuf;
    /**
     * The server or client context.
     */
    private final Context context;
    /**
     * A supplier for providing read buffers.
     */
    private final Supplier<VirtualBuffer> readBufferSupplier;
    /**
     * The buffer for reading incoming data. Its size is determined by the `setReadBufferSize` setting in
     * AioClient/AioServer.
     */
    private VirtualBuffer readBuffer;
    /**
     * The buffer for writing outgoing data.
     */
    private VirtualBuffer writeBuffer;
    /**
     * The input stream for synchronous reading.
     */
    private InputStream inputStream;

    /**
     * Constructs a new TcpSession.
     *
     * @param channel            the underlying socket channel
     * @param context            the server/client context
     * @param writeBufferPage    the buffer page for writing
     * @param readBufferSupplier a supplier for read buffers
     */
    public TcpSession(AsynchronousSocketChannel channel, Context context, BufferPage writeBufferPage,
            Supplier<VirtualBuffer> readBufferSupplier) {
        this.channel = channel;
        this.context = context;
        this.readBufferSupplier = readBufferSupplier;
        this.byteBuf = new WriteBuffer(writeBufferPage, this::continueWrite, this.context.getWriteBufferSize(),
                this.context.getWriteBufferCapacity());
        // Trigger the state machine for a new session
        this.context.getProcessor().stateEvent(this, Status.NEW_SESSION, null);
        doRead();
    }

    /**
     * Initializes the read buffer and signals the start of a read operation.
     */
    void doRead() {
        this.readBuffer = readBufferSupplier.get();
        this.readBuffer.buffer().flip();
        signalRead();
    }

    /**
     * Called upon completion of an AIO write operation. Requires synchronous control.
     *
     * @param result the number of bytes written
     */
    void writeCompleted(int result) {
        Monitor monitor = context.getMonitor();
        if (monitor != null) {
            monitor.afterWrite(this, result);
        }
        VirtualBuffer currentWriteBuffer = this.writeBuffer;
        this.writeBuffer = null;
        if (currentWriteBuffer == null) {
            currentWriteBuffer = byteBuf.poll();
        } else if (!currentWriteBuffer.buffer().hasRemaining()) {
            currentWriteBuffer.clean();
            currentWriteBuffer = byteBuf.poll();
        }

        if (currentWriteBuffer != null) {
            continueWrite(currentWriteBuffer);
            return;
        }
        byteBuf.finishWrite();
        // The session might be in a Closing or Closed state
        if (status != SESSION_STATUS_ENABLED) {
            close();
        } else {
            // New messages might have been added to the write queue via the write method
            byteBuf.flush();
        }
    }

    /**
     * Closes the session.
     *
     * @param immediate if {@code true}, the session is closed immediately; otherwise, it closes after all pending
     *                  messages are sent.
     */
    public synchronized void close(boolean immediate) {
        // Check if the close method has already been called
        if (status == SESSION_STATUS_CLOSED) {
            return;
        }
        status = immediate ? SESSION_STATUS_CLOSED : SESSION_STATUS_CLOSING;
        if (immediate) {
            try {
                byteBuf.close();
                if (readBuffer != null) {
                    readBuffer.clean();
                    readBuffer = null;
                }
                if (writeBuffer != null) {
                    writeBuffer.clean();
                    writeBuffer = null;
                }
            } finally {
                IoKit.close(channel);
                context.getProcessor().stateEvent(this, Status.SESSION_CLOSED, null);
            }
        } else if ((writeBuffer == null || !writeBuffer.buffer().hasRemaining()) && byteBuf.isEmpty()) {
            close(true);
        } else {
            context.getProcessor().stateEvent(this, Status.SESSION_CLOSING, null);
            byteBuf.flush();
        }
    }

    /**
     * Gets the write buffer for this session.
     *
     * @return the {@link WriteBuffer}
     */
    public WriteBuffer writeBuffer() {
        return byteBuf;
    }

    @Override
    public ByteBuffer readBuffer() {
        return readBuffer.buffer();
    }

    @Override
    public void awaitRead() {
        modCount++;
    }

    /**
     * Called upon completion of a read operation.
     *
     * @param result the number of bytes read, or -1 if the end of the stream has been reached
     */
    void readCompleted(int result) {
        // Release the buffer
        if (result == AsynchronousChannelProvider.READ_MONITOR_SIGNAL) {
            this.readBuffer.clean();
            this.readBuffer = null;
            return;
        }
        if (result == AsynchronousChannelProvider.READABLE_SIGNAL) {
            doRead();
            return;
        }
        // Pre-process the received message
        Monitor monitor = context.getMonitor();
        if (monitor != null) {
            monitor.afterRead(this, result);
        }
        this.eof = result == -1;
        if (SESSION_STATUS_CLOSED != status) {
            this.readBuffer.buffer().flip();
            signalRead();
        }
    }

    /**
     * Completion handler for read events.
     */
    private static final CompletionHandler<Integer, TcpSession> READ_COMPLETION_HANDLER = new CompletionHandler<>() {

        @Override
        public void completed(Integer result, TcpSession session) {
            try {
                session.readCompleted(result);
            } catch (Throwable throwable) {
                failed(throwable, session);
            }
        }

        @Override
        public void failed(Throwable exc, TcpSession session) {
            try {
                session.context.getProcessor().stateEvent(session, Status.INPUT_EXCEPTION, exc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                session.close(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Triggers the channel's read callback operation.
     */
    public void signalRead() {
        int modCount = this.modCount;
        if (status == SESSION_STATUS_CLOSED) {
            return;
        }
        final ByteBuffer readBuffer = this.readBuffer.buffer();
        final Handler handler = context.getProcessor();
        while (readBuffer.hasRemaining() && status == SESSION_STATUS_ENABLED) {
            Object dataEntry;
            try {
                dataEntry = context.getProtocol().decode(readBuffer, this);
            } catch (Exception e) {
                handler.stateEvent(this, Status.DECODE_EXCEPTION, e);
                throw e;
            }
            if (dataEntry == null) {
                break;
            }

            // Process the message
            try {
                handler.process(this, dataEntry);
                if (modCount != this.modCount) {
                    return;
                }
            } catch (Exception e) {
                handler.stateEvent(this, Status.PROCESS_EXCEPTION, e);
            }
        }

        if (eof || status == SESSION_STATUS_CLOSING) {
            close(false);
            handler.stateEvent(this, Status.INPUT_SHUTDOWN, null);
            return;
        }
        if (status == SESSION_STATUS_CLOSED) {
            return;
        }

        byteBuf.flush();

        readBuffer.compact();
        // If the read buffer is full, it indicates a potential issue with the protocol decoding
        if (!readBuffer.hasRemaining()) {
            InternalException exception = new InternalException(
                    "readBuffer overflow. The current TCP connection will be closed. Please fix your "
                            + context.getProtocol().getClass().getSimpleName() + "#decode bug.");
            handler.stateEvent(this, Status.DECODE_EXCEPTION, exception);
            throw exception;
        }

        // Read from the channel
        Monitor monitor = context.getMonitor();
        if (monitor != null) {
            monitor.beforeRead(this);
        }
        channel.read(readBuffer, 0L, TimeUnit.MILLISECONDS, this, READ_COMPLETION_HANDLER);
    }

    /**
     * Triggers a write operation.
     *
     * @param writeBuffer the buffer containing the data to be written
     */
    private void continueWrite(VirtualBuffer writeBuffer) {
        this.writeBuffer = writeBuffer;
        Monitor monitor = context.getMonitor();
        if (monitor != null) {
            monitor.beforeWrite(this);
        }
        channel.write(writeBuffer.buffer(), 0L, TimeUnit.MILLISECONDS, this, WRITE_COMPLETION_HANDLER);
    }

    /**
     * Gets the local address of the socket.
     *
     * @return the local address
     * @throws IOException if an I/O error occurs
     * @see AsynchronousSocketChannel#getLocalAddress()
     */
    public InetSocketAddress getLocalAddress() throws IOException {
        assertChannel();
        return (InetSocketAddress) channel.getLocalAddress();
    }

    /**
     * Gets the remote address of the socket.
     *
     * @return the remote address
     * @throws IOException if an I/O error occurs
     * @see AsynchronousSocketChannel#getRemoteAddress()
     */
    public InetSocketAddress getRemoteAddress() throws IOException {
        assertChannel();
        return (InetSocketAddress) channel.getRemoteAddress();
    }

    /**
     * Performs a synchronous read from the channel.
     *
     * @return the number of bytes read
     * @throws IOException if an I/O error occurs
     */
    private int synRead() throws IOException {
        ByteBuffer buffer = readBuffer.buffer();
        if (buffer.remaining() > 0) {
            return 0;
        }
        try {
            buffer.clear();
            int size = channel.read(buffer).get();
            buffer.flip();
            return size;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Asserts that the current session is available.
     *
     * @throws IOException if the session is closed
     */
    private void assertChannel() throws IOException {
        if (status == SESSION_STATUS_CLOSED || channel == null) {
            throw new IOException("session is closed");
        }
    }

    /**
     * Gets the input stream for this session.
     * <p>
     * Calling this method in faster mode will throw an {@link UnsupportedOperationException}. Using this method when
     * the handler processes messages asynchronously may cause exceptions.
     * </p>
     *
     * @return the input stream for synchronous reading
     * @throws IOException if an I/O error occurs
     */
    public InputStream getInputStream() throws IOException {
        return inputStream == null ? getInputStream(-1) : inputStream;
    }

    /**
     * Gets an input stream with a known length.
     *
     * @param length the length of the input stream
     * @return the input stream for synchronous reading
     * @throws IOException if an I/O error occurs or if a previous stream is still open
     */
    public InputStream getInputStream(int length) throws IOException {
        if (inputStream != null) {
            throw new IOException("previous inputStream has not been closed");
        }
        synchronized (this) {
            if (inputStream == null) {
                inputStream = new InnerInputStream(length);
            }
        }
        return inputStream;
    }

    /**
     * An inner class providing a synchronous InputStream for the session.
     */
    private class InnerInputStream extends InputStream {

        /**
         * The number of bytes remaining to be read from this input stream.
         */
        private int remainLength;

        /**
         * Constructs an InnerInputStream.
         *
         * @param length the total length of the stream, or -1 if unknown.
         */
        InnerInputStream(int length) {
            this.remainLength = length >= 0 ? length : -1;
        }

        @Override
        public int read() throws IOException {
            if (remainLength == 0) {
                return -1;
            }
            ByteBuffer readBuffer = TcpSession.this.readBuffer.buffer();
            if (readBuffer.hasRemaining()) {
                remainLength--;
                return readBuffer.get() & 0xFF; // Return unsigned byte
            }
            if (synRead() == -1) {
                remainLength = 0;
            }
            return read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }
            if (remainLength == 0) {
                return -1;
            }
            if (remainLength > 0 && remainLength < len) {
                len = remainLength;
            }
            ByteBuffer readBuffer = TcpSession.this.readBuffer.buffer();
            int size = 0;
            while (len > 0 && synRead() != -1) {
                int readSize = Math.min(readBuffer.remaining(), len);
                readBuffer.get(b, off + size, readSize);
                size += readSize;
                len -= readSize;
            }
            if (size > 0 && remainLength > 0) {
                remainLength -= size;
            }
            return size == 0 ? -1 : size;
        }

        @Override
        public int available() throws IOException {
            if (remainLength == 0) {
                return 0;
            }
            if (synRead() == -1) {
                remainLength = 0;
                return 0;
            }
            ByteBuffer readBuffer = TcpSession.this.readBuffer.buffer();
            if (remainLength < 0) { // remainLength is -1
                return readBuffer.remaining();
            } else {
                return Math.min(remainLength, readBuffer.remaining());
            }
        }

        @Override
        public void close() {
            if (TcpSession.this.inputStream == InnerInputStream.this) {
                TcpSession.this.inputStream = null;
            }
        }
    }

    /**
     * Completion handler for write events.
     */
    private static final CompletionHandler<Integer, TcpSession> WRITE_COMPLETION_HANDLER = new CompletionHandler<>() {

        @Override
        public void completed(Integer result, TcpSession session) {
            try {
                session.writeCompleted(result);
            } catch (Throwable throwable) {
                failed(throwable, session);
            }
        }

        @Override
        public void failed(Throwable exc, TcpSession session) {
            try {
                session.context.getProcessor().stateEvent(session, Status.OUTPUT_EXCEPTION, exc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}
