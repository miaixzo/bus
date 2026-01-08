/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.metric.channel;

import org.miaixz.bus.socket.metric.handler.FutureCompletionHandler;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * An abstract base class for asynchronous server channels, simulating the JDK7 AIO processing style. This class
 * provides common functionality for both client and server asynchronous socket channels.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
class AsynchronousServerChannel extends AsynchronousSocketChannel {

    /**
     * The actual underlying {@link SocketChannel}.
     */
    protected final SocketChannel channel;
    /**
     * The {@link AsynchronousChannelGroup.Worker} responsible for handling read events.
     */
    private final AsynchronousChannelGroup.Worker readWorker;
    private final boolean lowMemory;
    private final AsynchronousChannelGroup channelGroup;
    /**
     * The buffer used to receive data from the read channel. After decoding, the buffer is freed for the next batch of
     * data.
     */
    private ByteBuffer readBuffer;
    /**
     * The buffer used to hold data pending for write operations.
     */
    private ByteBuffer writeBuffer;
    /**
     * The completion handler for read events.
     */
    private CompletionHandler<Number, Object> readCompletionHandler;
    /**
     * The completion handler for write events.
     */
    private CompletionHandler<Number, Object> writeCompletionHandler;
    /**
     * The attachment object associated with the read completion handler.
     */
    private Object readAttachment;
    /**
     * The attachment object associated with the write completion handler.
     */
    private Object writeAttachment;
    private SelectionKey readSelectionKey;
    /**
     * A flag to indicate if a write operation was interrupted.
     */
    private boolean writeInterrupted;
    /**
     * Counter for read invoker recursion depth, to prevent excessive recursion.
     */
    private byte readInvoker = AsynchronousChannelGroup.MAX_INVOKER;

    /**
     * Constructs an {@code AsynchronousServerChannel}.
     *
     * @param group     the asynchronous channel group to which this channel belongs
     * @param channel   the underlying {@link SocketChannel}
     * @param lowMemory a boolean indicating whether low memory mode is enabled
     * @throws IOException if an I/O error occurs
     */
    public AsynchronousServerChannel(AsynchronousChannelGroup group, SocketChannel channel, boolean lowMemory)
            throws IOException {
        super(group.provider());
        this.channel = channel;
        this.channelGroup = group;
        readWorker = group.getReadWorker();
        this.lowMemory = lowMemory;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @throws IOException if an I/O error occurs while closing the channel
     */
    @Override
    public final void close() throws IOException {
        IOException exception = null;
        try {
            if (channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            exception = e;
        }
        if (readCompletionHandler != null) {
            doRead(true);
        }
        if (readSelectionKey != null) {
            readSelectionKey.cancel();
            readSelectionKey = null;
        }
        SelectionKey key = channel.keyFor(channelGroup.writeWorker.selector);
        if (key != null) {
            key.cancel();
        }
        key = channel.keyFor(channelGroup.commonWorker.selector);
        if (key != null) {
            key.cancel();
        }
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param local the local address to bind to
     * @return this channel
     * @throws IOException if an I/O error occurs
     */
    @Override
    public final AsynchronousSocketChannel bind(SocketAddress local) throws IOException {
        channel.bind(local);
        return this;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param <T>   the type of the socket option value
     * @param name  the socket option
     * @param value the value of the socket option
     * @return this channel
     * @throws IOException if an I/O error occurs
     */
    @Override
    public final <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        channel.setOption(name, value);
        return this;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param <T>  the type of the socket option value
     * @param name the socket option
     * @return the value of the socket option
     * @throws IOException if an I/O error occurs
     */
    @Override
    public final <T> T getOption(SocketOption<T> name) throws IOException {
        return channel.getOption(name);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return a set of the socket options supported by this channel
     */
    @Override
    public final Set<SocketOption<?>> supportedOptions() {
        return channel.supportedOptions();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return this channel
     * @throws IOException if an I/O error occurs
     */
    @Override
    public final AsynchronousSocketChannel shutdownInput() throws IOException {
        channel.shutdownInput();
        return this;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return this channel
     * @throws IOException if an I/O error occurs
     */
    @Override
    public final AsynchronousSocketChannel shutdownOutput() throws IOException {
        channel.shutdownOutput();
        return this;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the remote address, or {@code null} if the channel is not connected
     * @throws IOException if an I/O error occurs
     */
    @Override
    public final SocketAddress getRemoteAddress() throws IOException {
        return channel.getRemoteAddress();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This operation is not supported for server channels.
     * </p>
     *
     * @param <A>        the type of the attachment
     * @param remote     the remote address
     * @param attachment the attachment object
     * @param handler    the completion handler
     * @throws UnsupportedOperationException always
     */
    @Override
    public <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
        throw new UnsupportedOperationException("Connect operation is not supported for AsynchronousServerChannel");
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This operation is not supported for server channels.
     * </p>
     *
     * @param remote the remote address
     * @return a future representing the pending result
     * @throws UnsupportedOperationException always
     */
    @Override
    public Future<Void> connect(SocketAddress remote) {
        throw new UnsupportedOperationException("Connect operation is not supported for AsynchronousServerChannel");
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Timeout is not supported for read operations in this implementation.
     * </p>
     *
     * @param <A>        the type of the attachment
     * @param dst        the buffer into which bytes are to be transferred
     * @param timeout    the maximum time for the operation (must be 0)
     * @param unit       the time unit of the timeout argument
     * @param attachment the object to attach to the I/O operation
     * @param handler    the handler for consuming the result
     * @throws UnsupportedOperationException if timeout is greater than 0
     */
    @Override
    public final <A> void read(
            ByteBuffer dst,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        if (timeout > 0) {
            throw new UnsupportedOperationException("Timeout is not supported for read operations");
        }
        read0(dst, attachment, handler);
    }

    /**
     * Internal method to handle read operations.
     *
     * @param <V>        the type of the result of the completion handler
     * @param <A>        the type of the attachment object
     * @param readBuffer the buffer into which bytes are to be transferred
     * @param attachment an object to attach to the operation, for use by the completion handler
     * @param handler    the handler to invoke when the read operation completes
     */
    private <V extends Number, A> void read0(
            ByteBuffer readBuffer,
            A attachment,
            CompletionHandler<V, ? super A> handler) {
        if (this.readCompletionHandler != null) {
            throw new ReadPendingException();
        }
        this.readBuffer = readBuffer;
        this.readAttachment = attachment;
        this.readCompletionHandler = (CompletionHandler<Number, Object>) handler;
        doRead(handler instanceof FutureCompletionHandler);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param readBuffer the buffer into which bytes are to be transferred
     * @return a {@link Future} representing the pending result
     */
    @Override
    public final Future<Integer> read(ByteBuffer readBuffer) {
        FutureCompletionHandler<Integer, Object> readFuture = new FutureCompletionHandler<>();
        read(readBuffer, 0, TimeUnit.MILLISECONDS, null, readFuture);
        return readFuture;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Scattering read is not supported in this implementation.
     * </p>
     *
     * @param <A>        the type of the attachment
     * @param dsts       the buffers into which bytes are to be transferred
     * @param offset     the offset within the buffer array
     * @param length     the maximum number of buffers to be accessed
     * @param timeout    the maximum time for the operation
     * @param unit       the time unit of the timeout argument
     * @param attachment the object to attach to the I/O operation
     * @param handler    the handler for consuming the result
     * @throws UnsupportedOperationException always
     */
    @Override
    public final <A> void read(
            ByteBuffer[] dsts,
            int offset,
            int length,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Long, ? super A> handler) {
        throw new UnsupportedOperationException("Scattering read is not supported");
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Timeout is not supported for write operations in this implementation.
     * </p>
     *
     * @param <A>        the type of the attachment
     * @param src        the buffer from which bytes are to be retrieved
     * @param timeout    the maximum time for the operation (must be 0)
     * @param unit       the time unit of the timeout argument
     * @param attachment the object to attach to the I/O operation
     * @param handler    the handler for consuming the result
     * @throws UnsupportedOperationException if timeout is greater than 0
     */
    @Override
    public final <A> void write(
            ByteBuffer src,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        if (timeout > 0) {
            throw new UnsupportedOperationException("Timeout is not supported for write operations");
        }
        write0(src, attachment, handler);
    }

    /**
     * Internal method to handle write operations.
     *
     * @param <V>         the type of the result of the completion handler
     * @param <A>         the type of the attachment object
     * @param writeBuffer the buffer from which bytes are to be retrieved
     * @param attachment  an object to attach to the operation, for use by the completion handler
     * @param handler     the handler to invoke when the write operation completes
     */
    private <V extends Number, A> void write0(
            ByteBuffer writeBuffer,
            A attachment,
            CompletionHandler<V, ? super A> handler) {
        if (this.writeCompletionHandler != null) {
            throw new WritePendingException();
        }
        this.writeBuffer = writeBuffer;
        this.writeAttachment = attachment;
        this.writeCompletionHandler = (CompletionHandler<Number, Object>) handler;
        while (doWrite())
            ;
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Future-based write is not supported in this implementation.
     * </p>
     *
     * @param src the buffer from which bytes are to be retrieved
     * @return a future representing the pending result
     * @throws UnsupportedOperationException always
     */
    @Override
    public final Future<Integer> write(ByteBuffer src) {
        throw new UnsupportedOperationException("Future-based write is not supported");
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * Gathering write is not supported in this implementation.
     * </p>
     *
     * @param <A>        the type of the attachment
     * @param srcs       the buffers from which bytes are to be retrieved
     * @param offset     the offset within the buffer array
     * @param length     the maximum number of buffers to be accessed
     * @param timeout    the maximum time for the operation
     * @param unit       the time unit of the timeout argument
     * @param attachment the object to attach to the I/O operation
     * @param handler    the handler for consuming the result
     * @throws UnsupportedOperationException always
     */
    @Override
    public final <A> void write(
            ByteBuffer[] srcs,
            int offset,
            int length,
            long timeout,
            TimeUnit unit,
            A attachment,
            CompletionHandler<Long, ? super A> handler) {
        throw new UnsupportedOperationException("Gathering write is not supported");
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the local address, or {@code null} if the channel is not bound
     * @throws IOException if an I/O error occurs
     */
    @Override
    public final SocketAddress getLocalAddress() throws IOException {
        return channel.getLocalAddress();
    }

    /**
     * Performs a read operation on the channel.
     *
     * @param direct if {@code true}, attempts a direct read; otherwise, registers for a read event.
     */
    public final void doRead(boolean direct) {
        try {
            if (readCompletionHandler == null) {
                return;
            }
            if (!channelGroup.running) {
                throw new IOException("channelGroup is shutdown");
            }
            // If previously called via Future and cancelled, return.
            if (readCompletionHandler instanceof FutureCompletionHandler
                    && ((FutureCompletionHandler) readCompletionHandler).isDone()) {
                AsynchronousChannelGroup.removeOps(readSelectionKey, SelectionKey.OP_READ);
                resetRead();
                return;
            }
            if (lowMemory && direct && readBuffer == null) {
                CompletionHandler<Number, Object> completionHandler = readCompletionHandler;
                Object attach = readAttachment;
                resetRead();
                completionHandler.completed(AsynchronousChannelProvider.READABLE_SIGNAL, attach);
                return;
            }
            boolean directRead = direct || readInvoker++ < AsynchronousChannelGroup.MAX_INVOKER;

            int readSize = 0;
            boolean hasRemain = true;
            if (directRead) {
                readSize = channel.read(readBuffer);
                hasRemain = readBuffer.hasRemaining();
            }

            // Register to asynchronous thread if readSize is 0 and it's a FutureCompletionHandler
            if (readSize == 0 && readCompletionHandler instanceof FutureCompletionHandler) {
                AsynchronousChannelGroup.removeOps(readSelectionKey, SelectionKey.OP_READ);
                channelGroup.commonWorker.addRegister(selector -> {
                    try {
                        channel.register(selector, SelectionKey.OP_READ, AsynchronousServerChannel.this);
                    } catch (ClosedChannelException e) {
                        doRead(true);
                    }
                });
                return;
            }
            // Release memory if in low memory mode and readSize is 0 and buffer is empty
            if (lowMemory && readSize == 0 && readBuffer.position() == 0) {
                readBuffer = null;
                readCompletionHandler.completed(AsynchronousChannelProvider.READ_MONITOR_SIGNAL, readAttachment);
            }

            if (readSize != 0 || !hasRemain) {
                CompletionHandler<Number, Object> completionHandler = readCompletionHandler;
                Object attach = readAttachment;
                resetRead();
                completionHandler.completed(readSize, attach);

                if (readCompletionHandler == null && readSelectionKey != null) {
                    AsynchronousChannelGroup.removeOps(readSelectionKey, SelectionKey.OP_READ);
                }
            } else if (readSelectionKey == null) {
                readWorker.addRegister(selector -> {
                    try {
                        readSelectionKey = channel
                                .register(selector, SelectionKey.OP_READ, AsynchronousServerChannel.this);
                    } catch (ClosedChannelException e) {
                        readCompletionHandler.failed(e, readAttachment);
                    }
                });
            } else {
                AsynchronousChannelGroup.interestOps(readWorker, readSelectionKey, SelectionKey.OP_READ);
            }
        } catch (Throwable e) {
            if (readCompletionHandler == null) {
                try {
                    close();
                } catch (Throwable ignore) {
                }
            } else {
                CompletionHandler<Number, Object> completionHandler = readCompletionHandler;
                Object attach = readAttachment;
                resetRead();
                completionHandler.failed(e, attach);
            }
        } finally {
            readInvoker = 0;
        }
    }

    /**
     * Resets the state of the read operation.
     */
    private void resetRead() {
        readCompletionHandler = null;
        readAttachment = null;
        readBuffer = null;
    }

    /**
     * Performs a write operation on the channel.
     *
     * @return {@code true} if more data needs to be written, {@code false} otherwise.
     */
    public final boolean doWrite() {
        if (writeInterrupted) {
            writeInterrupted = false;
            return false;
        }
        try {
            if (!channelGroup.running) {
                throw new IOException("channelGroup is shutdown");
            }
            int writeSize = channel.write(writeBuffer);

            if (writeSize != 0 || !writeBuffer.hasRemaining()) {
                CompletionHandler<Number, Object> completionHandler = writeCompletionHandler;
                Object attach = writeAttachment;
                resetWrite();
                writeInterrupted = true;
                completionHandler.completed(writeSize, attach);
                if (!writeInterrupted) {
                    return true;
                }
                writeInterrupted = false;
            } else {
                SelectionKey commonSelectionKey = channel.keyFor(channelGroup.writeWorker.selector);
                if (commonSelectionKey == null) {
                    channelGroup.writeWorker.addRegister(selector -> {
                        try {
                            channel.register(selector, SelectionKey.OP_WRITE, AsynchronousServerChannel.this);
                        } catch (ClosedChannelException e) {
                            writeCompletionHandler.failed(e, writeAttachment);
                        }
                    });
                } else {
                    AsynchronousChannelGroup
                            .interestOps(channelGroup.writeWorker, commonSelectionKey, SelectionKey.OP_WRITE);
                }
            }
        } catch (Throwable e) {
            if (writeCompletionHandler == null) {
                e.printStackTrace();
                try {
                    close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } else {
                writeCompletionHandler.failed(e, writeAttachment);
            }
        }
        return false;
    }

    /**
     * Resets the state of the write operation.
     */
    private void resetWrite() {
        writeAttachment = null;
        writeCompletionHandler = null;
        writeBuffer = null;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return {@code true} if, and only if, this channel is open
     */
    @Override
    public final boolean isOpen() {
        return channel.isOpen();
    }

}
