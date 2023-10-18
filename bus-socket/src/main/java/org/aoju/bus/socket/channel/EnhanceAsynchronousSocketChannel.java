/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org sandao and other contributors.               *
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

import org.aoju.bus.socket.buffers.BufferArray;
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
 * @since Java 17+
 */
public final class EnhanceAsynchronousSocketChannel extends AsynchronousSocketChannel {
    /**
     * 实际的Socket通道
     */
    private final SocketChannel channel;
    /**
     * 处理当前连接IO事件的资源组
     */
    private final EnhanceAsynchronousChannelGroup group;
    /**
     * 处理 read 事件的线程资源
     */
    private final EnhanceAsynchronousChannelGroup.Worker readWorker;
    /**
     * 处理 write 事件的线程资源
     */
    private final EnhanceAsynchronousChannelGroup.Worker writeWorker;
    /**
     * 处理 connect 事件的线程资源
     */
    private final EnhanceAsynchronousChannelGroup.Worker connectWorker;
    /**
     * 用于接收 read 通道数据的缓冲区，经解码后腾出缓冲区以供下一批数据的读取
     */
    private ByteBuffer readBuffer;
    /**
     * 用于接收 read 通道数据的缓冲区集合
     */
    private BufferArray scatteringReadBuffer;
    /**
     * 存放待输出数据的缓冲区
     */
    private ByteBuffer writeBuffer;
    /**
     * 存放待输出数据的缓冲区集合
     */
    private BufferArray gatheringWriteBuffer;
    /**
     * read 回调事件处理器
     */
    private CompletionHandler<Number, Object> readCompletionHandler;
    /**
     * write 回调事件处理器
     */
    private CompletionHandler<Number, Object> writeCompletionHandler;
    /**
     * connect 回调事件处理器
     */
    private CompletionHandler<Void, Object> connectCompletionHandler;
    private FutureCompletionHandler<Void, Void> connectFuture;
    private FutureCompletionHandler<? extends Number, Object> readFuture;
    private FutureCompletionHandler<? extends Number, Object> writeFuture;
    /**
     * read 回调事件关联绑定的附件对象
     */
    private Object readAttachment;
    /**
     * write 回调事件关联绑定的附件对象
     */
    private Object writeAttachment;
    /**
     * connect 回调事件关联绑定的附件对象
     */
    private Object connectAttachment;
    private SelectionKey readSelectionKey;
    private SelectionKey readFutureSelectionKey;
    private SelectionKey writeSelectionKey;
    private SelectionKey writeFutureSelectionKey;
    private SelectionKey connectSelectionKey;
    /**
     * 当前是否正在执行 write 操作
     */
    private boolean writePending;
    /**
     * 当前是否正在执行 read 操作
     */
    private boolean readPending;
    /**
     * 当前是否正在执行 connect 操作
     */
    private boolean connectionPending;
    /**
     * 远程连接的地址
     */
    private SocketAddress remote;
    private int writeInvoker;

    private boolean lowMemory;

    public EnhanceAsynchronousSocketChannel(EnhanceAsynchronousChannelGroup group, SocketChannel channel, boolean lowMemory) throws IOException {
        super(group.provider());
        this.group = group;
        this.channel = channel;
        readWorker = group.getReadWorker();
        writeWorker = group.getWriteWorker();
        connectWorker = group.getConnectWorker();
        this.lowMemory = lowMemory;
    }

    @Override
    public void close() throws IOException {
        IOException exception = null;
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            exception = e;
        }
        if (readSelectionKey != null) {
            readSelectionKey.cancel();
            readSelectionKey = null;
        }
        if (readFutureSelectionKey != null) {
            readFutureSelectionKey.cancel();
            readFutureSelectionKey = null;
        }
        if (writeSelectionKey != null) {
            writeSelectionKey.cancel();
            writeSelectionKey = null;
        }
        if (writeFutureSelectionKey != null) {
            writeFutureSelectionKey.cancel();
            writeFutureSelectionKey = null;
        }
        if (connectSelectionKey != null) {
            connectSelectionKey.cancel();
            connectSelectionKey = null;
        }
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    public AsynchronousSocketChannel bind(SocketAddress local) throws IOException {
        channel.bind(local);
        return this;
    }

    @Override
    public <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
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
    public AsynchronousSocketChannel shutdownInput() throws IOException {
        channel.shutdownInput();
        return this;
    }

    @Override
    public AsynchronousSocketChannel shutdownOutput() throws IOException {
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

    private <V extends Number, A> void read0(ByteBuffer readBuffer, BufferArray scattering, long timeout, TimeUnit unit, A attachment, CompletionHandler<V, ? super A> handler) {
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
        doRead(readFuture != null);
    }

    @Override
    public Future<Integer> read(ByteBuffer readBuffer) {
        FutureCompletionHandler<Integer, Object> readFuture = new FutureCompletionHandler<>();
        this.readFuture = readFuture;
        read(readBuffer, 0, TimeUnit.MILLISECONDS, null, readFuture);
        return readFuture;
    }

    @Override
    public <A> void read(ByteBuffer[] dsts, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler) {
        read0(null, new BufferArray(dsts, offset, length), timeout, unit, attachment, handler);
    }

    @Override
    public <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler) {
        write0(src, null, timeout, unit, attachment, handler);
    }

    private <V extends Number, A> void write0(ByteBuffer writeBuffer, BufferArray gathering, long timeout, TimeUnit unit, A attachment, CompletionHandler<V, ? super A> handler) {
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
        this.writeFuture = writeFuture;
        write0(src, null, 0, TimeUnit.MILLISECONDS, null, writeFuture);
        return writeFuture;
    }

    @Override
    public <A> void write(ByteBuffer[] srcs, int offset, int length, long timeout, TimeUnit unit, A attachment, CompletionHandler<Long, ? super A> handler) {
        write0(null, new BufferArray(srcs, offset, length), timeout, unit, attachment, handler);
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return channel.getLocalAddress();
    }

    public void doConnect() {
        try {
            //此前通过Future调用,且触发了cancel
            if (connectFuture != null && connectFuture.isDone()) {
                resetConnect();
                return;
            }
            boolean connected = channel.isConnectionPending();
            if (connected || channel.connect(remote)) {
                connected = channel.finishConnect();
            }
            channel.configureBlocking(false);
            if (connected) {
                CompletionHandler<Void, Object> completionHandler = connectCompletionHandler;
                Object attach = connectAttachment;
                resetConnect();
                completionHandler.completed(null, attach);
            } else if (connectSelectionKey == null) {
                connectWorker.addRegister(selector -> {
                    try {
                        connectSelectionKey = channel.register(selector, SelectionKey.OP_CONNECT, EnhanceAsynchronousSocketChannel.this);
                    } catch (ClosedChannelException e) {
                        connectCompletionHandler.failed(e, connectAttachment);
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

    public void doRead(boolean direct) {
        try {
            // 此前通过Future调用,且触发了cancel
            if (readFuture != null && readFuture.isDone()) {
                group.removeOps(readSelectionKey, SelectionKey.OP_READ);
                resetRead();
                return;
            }
            if (lowMemory && direct && readBuffer == null) {
                CompletionHandler<Number, Object> completionHandler = readCompletionHandler;
                Object attach = readAttachment;
                resetRead();
                completionHandler.completed(EnhanceAsynchronousChannelProvider.READABLE_SIGNAL, attach);
                return;
            }
            boolean directRead = direct || (Thread.currentThread() == readWorker.getWorkerThread() && readWorker.invoker++ < EnhanceAsynchronousChannelGroup.MAX_INVOKER);

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

            // 注册至异步线程
            if (readFuture != null && readSize == 0) {
                group.removeOps(readSelectionKey, SelectionKey.OP_READ);
                group.registerFuture(selector -> {
                    try {
                        readFutureSelectionKey = channel.register(selector, SelectionKey.OP_READ, EnhanceAsynchronousSocketChannel.this);
                    } catch (ClosedChannelException e) {
                        e.printStackTrace();
                        doRead(true);
                    }
                }, SelectionKey.OP_READ);
                return;
            }
            // 释放内存
            if (lowMemory && readSize == 0 && readBuffer.position() == 0) {
                readBuffer = null;
                readCompletionHandler.completed(EnhanceAsynchronousChannelProvider.READ_MONITOR_SIGNAL, readAttachment);
            }

            if (readSize != 0 || !hasRemain) {
                CompletionHandler<Number, Object> completionHandler = readCompletionHandler;
                Object attach = readAttachment;
                BufferArray scattering = scatteringReadBuffer;
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
                        readSelectionKey = channel.register(selector, SelectionKey.OP_READ, EnhanceAsynchronousSocketChannel.this);
                    } catch (ClosedChannelException e) {
                        readCompletionHandler.failed(e, readAttachment);
                    }
                });
            } else {
                group.interestOps(readWorker, readSelectionKey, SelectionKey.OP_READ);
            }

        } catch (Throwable e) {
            if (readCompletionHandler == null) {
                e.printStackTrace();
                try {
                    close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } else {
                readCompletionHandler.failed(e, readAttachment);
            }
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
            int invoker = 0;
            // 防止无限递归导致堆栈溢出
            if (writeWorker.getWorkerThread() == Thread.currentThread()) {
                invoker = ++writeWorker.invoker;
            } else if (readWorker.getWorkerThread() != Thread.currentThread()) {
                invoker = ++writeInvoker;
            }
            int writeSize = 0;
            boolean hasRemain = true;
            if (invoker < EnhanceAsynchronousChannelGroup.MAX_INVOKER) {
                if (gatheringWriteBuffer != null) {
                    writeSize = (int) channel.write(gatheringWriteBuffer.getBuffers(), gatheringWriteBuffer.getOffset(), gatheringWriteBuffer.getLength());
                    hasRemain = hasRemaining(gatheringWriteBuffer);
                } else {
                    writeSize = channel.write(writeBuffer);
                    hasRemain = writeBuffer.hasRemaining();
                }
            } else {
                writeInvoker = 0;
            }

            //注册至异步线程
            if (writeFuture != null && writeSize == 0) {
                group.removeOps(writeSelectionKey, SelectionKey.OP_WRITE);
                group.registerFuture(selector -> {
                    try {
                        writeFutureSelectionKey = channel.register(selector, SelectionKey.OP_WRITE, EnhanceAsynchronousSocketChannel.this);
                    } catch (ClosedChannelException e) {
                        e.printStackTrace();
                        doWrite();
                    }
                }, SelectionKey.OP_WRITE);
                return;
            }

            if (writeSize != 0 || !hasRemain) {
                CompletionHandler<Number, Object> completionHandler = writeCompletionHandler;
                Object attach = writeAttachment;
                resetWrite();
                completionHandler.completed(writeSize, attach);
            } else if (writeSelectionKey == null) {
                writeWorker.addRegister(selector -> {
                    try {
                        writeSelectionKey = channel.register(selector, SelectionKey.OP_WRITE, EnhanceAsynchronousSocketChannel.this);
                    } catch (ClosedChannelException e) {
                        writeCompletionHandler.failed(e, writeAttachment);
                    }
                });
            } else {
                group.interestOps(writeWorker, writeSelectionKey, SelectionKey.OP_WRITE);
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
    }

    private boolean hasRemaining(BufferArray scattering) {
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

}
