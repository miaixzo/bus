/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org sandao and other contributors.               *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.socket.channel;

import org.aoju.bus.socket.handler.FutureCompletionHandler;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 模拟JDK7的AIO处理方式
 *
 * @author Kimi Liu
 * @version 6.2.0
 * @since JDK 1.8+
 */
public class AsynchronousSocketChannel extends java.nio.channels.AsynchronousSocketChannel {

    private final SocketChannel channel;
    private final AsynchronousChannelGroup group;
    private final AsynchronousChannelGroup.Worker readWorker;
    private final AsynchronousChannelGroup.Worker writeWorker;
    private ByteBuffer readBuffer;
    private ByteBufferArray scatteringReadBuffer;
    private ByteBuffer writeBuffer;
    private ByteBufferArray gatheringWriteBuffer;
    private CompletionHandler<Number, Object> readCompletionHandler;
    private CompletionHandler<Number, Object> writeCompletionHandler;
    private CompletionHandler<Void, Object> connectCompletionHandler;
    private FutureCompletionHandler<Void, Void> connectFuture;
    private FutureCompletionHandler<? extends Number, Object> readFuture;
    private FutureCompletionHandler<? extends Number, Object> writeFuture;
    private Object readAttachment;
    private Object writeAttachment;
    private Object connectAttachment;
    private SelectionKey readSelectionKey;
    private SelectionKey writeSelectionKey;
    private boolean writePending;
    private boolean readPending;
    private boolean connectionPending;
    private SocketAddress remote;

    public AsynchronousSocketChannel(AsynchronousChannelGroup group, SocketChannel channel) throws IOException {
        super(group.provider());
        this.group = group;
        this.channel = channel;
        readWorker = group.getReadWorker();
        writeWorker = group.getWriteWorker();
        channel.configureBlocking(false);
    }

    @Override
    public void close() throws IOException {
        IOException exception = null;
        try {
            channel.close();
        } catch (IOException e) {
            exception = e;
        }
        if (readSelectionKey != null) {
            readSelectionKey.cancel();
        }
        if (writeSelectionKey != null) {
            writeSelectionKey.cancel();
        }
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public java.nio.channels.AsynchronousSocketChannel bind(SocketAddress local) throws IOException {
        channel.bind(local);
        return this;
    }

    @Override
    public <T> java.nio.channels.AsynchronousSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        channel.setOption(name, value);
        return this;
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return channel.getOption(name);
    }

    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return channel.supportedOptions();
    }

    @Override
    public java.nio.channels.AsynchronousSocketChannel shutdownInput() throws IOException {
        channel.shutdownInput();
        return this;
    }

    @Override
    public java.nio.channels.AsynchronousSocketChannel shutdownOutput() throws IOException {
        channel.shutdownOutput();
        return this;
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return channel.getRemoteAddress();
    }

    @Override
    public <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
        if (group.isTerminated()) {
            throw new ShutdownChannelGroupException();
        }
        if (channel.isConnected()) {
            throw new AlreadyConnectedException();
        }
        if (connectionPending) {
            throw new ConnectionPendingException();
        }
        connectionPending = true;
        this.connectAttachment = attachment;
        this.connectCompletionHandler = (CompletionHandler<Void, Object>) handler;
        this.remote = remote;
        doConnect();
    }

    @Override
    public Future<Void> connect(SocketAddress remote) {
        FutureCompletionHandler<Void, Void> connectFuture = new FutureCompletionHandler<>();
        connect(remote, null, connectFuture);
        this.connectFuture = connectFuture;
        return connectFuture;
    }

    @Override
    public <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
        read0(dst, null, timeout, unit, attachment, handler);
    }

    private <V extends Number, A> void read0(ByteBuffer readBuffer, ByteBufferArray scattering, long timeout, TimeUnit unit, A attachment, CompletionHandler<V, ? super A> handler) {
        if (!channel.isConnected()) {
            throw new NotYetConnectedException();
        }
        if (readPending) {
            throw new ReadPendingException();
        }
        readPending = true;
        this.readBuffer = readBuffer;
        this.scatteringReadBuffer = scattering;
        this.readAttachment = attachment;
        if (timeout > 0) {
            readFuture = new FutureCompletionHandler<>((CompletionHandler<Number, Object>) handler, readAttachment);
            readCompletionHandler = (CompletionHandler<Number, Object>) readFuture;
            group.getScheduledExecutor().schedule(readFuture, timeout, unit);
        } else {
            this.readCompletionHandler = (CompletionHandler<Number, Object>) handler;
        }
        doRead();
    }

    @Override
    public Future<Integer> read(ByteBuffer readBuffer) {
        FutureCompletionHandler<Integer, Object> readFuture = new FutureCompletionHandler<>();
        read(readBuffer, 0, TimeUnit.MILLISECONDS, null, readFuture);
        this.readFuture = readFuture;
        return readFuture;
    }

    @Override
    public <A> void read(ByteBuffer[] dsts, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler) {
        read0(null, new ByteBufferArray(dsts, offset, length), timeout, unit, attachment, handler);
    }

    @Override
    public <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
        write0(src, null, timeout, unit, attachment, handler);
    }

    private <V extends Number, A> void write0(ByteBuffer writeBuffer, ByteBufferArray gathering, long timeout, TimeUnit unit, A attachment, CompletionHandler<V, ? super A> handler) {
        if (!channel.isConnected()) {
            throw new NotYetConnectedException();
        }
        if (writePending) {
            throw new WritePendingException();
        }

        writePending = true;
        this.writeBuffer = writeBuffer;
        this.gatheringWriteBuffer = gathering;
        this.writeAttachment = attachment;
        if (timeout > 0) {
            writeFuture = new FutureCompletionHandler<>((CompletionHandler<Number, Object>) handler, writeAttachment);
            writeCompletionHandler = (CompletionHandler<Number, Object>) writeFuture;
            group.getScheduledExecutor().schedule(writeFuture, timeout, unit);
        } else {
            this.writeCompletionHandler = (CompletionHandler<Number, Object>) handler;
        }
        doWrite();
    }

    @Override
    public Future<Integer> write(ByteBuffer src) {
        FutureCompletionHandler<Integer, Object> writeFuture = new FutureCompletionHandler<>();
        write0(src, null, 0, TimeUnit.MILLISECONDS, null, writeFuture);
        this.writeFuture = writeFuture;
        return writeFuture;
    }

    @Override
    public <A> void write(ByteBuffer[] srcs, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler) {
        write0(null, new ByteBufferArray(srcs, offset, length), timeout, unit, attachment, handler);
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return channel.getLocalAddress();
    }

    public void doConnect() {
        try {
            // 此前通过Future调用,且触发了cancel
            if (connectFuture != null && connectFuture.isDone()) {
                resetConnect();
                return;
            }
            boolean connected = channel.isConnectionPending();
            if (connected || channel.connect(remote)) {
                connected = channel.finishConnect();
            }
            if (connected) {
                CompletionHandler<Void, Object> completionHandler = connectCompletionHandler;
                Object attach = connectAttachment;
                resetConnect();
                completionHandler.completed(null, attach);
            } else if (writeSelectionKey == null) {
                writeWorker.addRegister(selector -> {
                    try {
                        writeSelectionKey = channel.register(selector, SelectionKey.OP_CONNECT);
                        writeSelectionKey.attach(AsynchronousSocketChannel.this);
                    } catch (ClosedChannelException e) {
                        writeCompletionHandler.failed(e, writeAttachment);
                    }
                });
            } else {
                throw new IOException("unKnow exception");
            }
        } catch (IOException e) {
            connectCompletionHandler.failed(e, connectAttachment);
        }

    }

    private void resetConnect() {
        connectionPending = false;
        connectFuture = null;
        connectAttachment = null;
        connectCompletionHandler = null;
    }

    public void doRead() {
        try {
            // 此前通过Future调用,且触发了cancel
            if (readFuture != null && readFuture.isDone()) {
                group.removeOps(readSelectionKey, SelectionKey.OP_READ);
                resetRead();
                return;
            }

            boolean directRead = Thread.currentThread() == readWorker.getWorkerThread()
                    && readWorker.invoker++ < AsynchronousChannelGroup.MAX_INVOKER;

            long readSize = 0;
            boolean hasRemain = true;
            if (directRead) {
                if (scatteringReadBuffer != null) {
                    readSize = channel.read(scatteringReadBuffer.getBuffers(), scatteringReadBuffer.getOffset(), scatteringReadBuffer.getLength());
                    hasRemain = hasRemaining(scatteringReadBuffer);
                } else {
                    readSize = channel.read(readBuffer);
                    hasRemain = readBuffer.hasRemaining();
                }
            }
            if (readSize != 0 || !hasRemain) {
                CompletionHandler<Number, Object> completionHandler = readCompletionHandler;
                Object attach = readAttachment;
                ByteBufferArray scattering = scatteringReadBuffer;
                resetRead();
                if (scattering == null) {
                    completionHandler.completed((int) readSize, attach);
                } else {
                    completionHandler.completed(readSize, attach);
                }

                if (!readPending && readSelectionKey != null) {
                    group.removeOps(readSelectionKey, SelectionKey.OP_READ);
                }
            } else if (readSelectionKey == null) {
                readWorker.addRegister(selector -> {
                    try {
                        readSelectionKey = channel.register(selector, SelectionKey.OP_READ);
                        readSelectionKey.attach(AsynchronousSocketChannel.this);
                    } catch (ClosedChannelException e) {
                        readCompletionHandler.failed(e, readAttachment);
                    }
                });
            } else {
                group.interestOps(readWorker, readSelectionKey, SelectionKey.OP_READ);
            }

        } catch (IOException e) {
            readCompletionHandler.failed(e, readAttachment);
        }
    }

    private void resetRead() {
        readPending = false;
        readFuture = null;
        readCompletionHandler = null;
        readAttachment = null;
        readBuffer = null;
        scatteringReadBuffer = null;
    }

    public void doWrite() {
        try {
            // 此前通过Future调用,且触发了cancel
            if (writeFuture != null && writeFuture.isDone()) {
                resetWrite();
                return;
            }
            boolean directWrite = writeWorker.getWorkerThread() != Thread.currentThread()
                    || writeWorker.invoker++ < AsynchronousChannelGroup.MAX_INVOKER;
            long writeSize = 0;
            boolean hasRemain = true;
            if (directWrite) {
                if (gatheringWriteBuffer != null) {
                    writeSize = channel.write(gatheringWriteBuffer.getBuffers(), gatheringWriteBuffer.getOffset(), gatheringWriteBuffer.getLength());
                    hasRemain = hasRemaining(gatheringWriteBuffer);
                } else {
                    writeSize = channel.write(writeBuffer);
                    hasRemain = writeBuffer.hasRemaining();
                }
            }

            if (writeSize != 0 || !hasRemain) {
                CompletionHandler<Number, Object> completionHandler = writeCompletionHandler;
                Object attach = writeAttachment;
                ByteBufferArray scattering = gatheringWriteBuffer;
                resetWrite();
                if (scattering == null) {
                    completionHandler.completed((int) writeSize, attach);
                } else {
                    completionHandler.completed(writeSize, attach);
                }
            } else if (writeSelectionKey == null) {
                writeWorker.addRegister(selector -> {
                    try {
                        writeSelectionKey = channel.register(selector, SelectionKey.OP_WRITE);
                        writeSelectionKey.attach(AsynchronousSocketChannel.this);
                    } catch (ClosedChannelException e) {
                        writeCompletionHandler.failed(e, writeAttachment);
                    }
                });
            } else {
                group.interestOps(writeWorker, writeSelectionKey, SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            writeCompletionHandler.failed(e, writeAttachment);
        }
    }

    private boolean hasRemaining(ByteBufferArray scattering) {
        for (int i = 0; i < scattering.getLength(); i++) {
            if (scattering.getBuffers()[scattering.getOffset() + i].hasRemaining()) {
                return true;
            }
        }
        return false;
    }

    private void resetWrite() {
        writePending = false;
        writeFuture = null;
        writeAttachment = null;
        writeCompletionHandler = null;
        writeBuffer = null;
        gatheringWriteBuffer = null;
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    final class ByteBufferArray {

        private final ByteBuffer[] buffers;
        private final int offset;
        private final int length;

        public ByteBufferArray(ByteBuffer[] buffers, int offset, int length) {
            this.buffers = buffers;
            this.offset = offset;
            this.length = length;
        }

        public ByteBuffer[] getBuffers() {
            return buffers;
        }

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }

    }

}
