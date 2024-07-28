/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.metric.channels;

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
 * 模拟JDK7的AIO处理方式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
class AsynchronousServerChannel extends AsynchronousSocketChannel {

    /**
     * 实际的Socket通道
     */
    protected final SocketChannel channel;
    /**
     * 处理 read 事件的线程资源
     */
    private final AsynchronousChannelGroup.Worker readWorker;
    private final boolean lowMemory;
    private final AsynchronousChannelGroup channelGroup;
    /**
     * 用于接收 read 通道数据的缓冲区，经解码后腾出缓冲区以供下一批数据的读取
     */
    private ByteBuffer readBuffer;
    /**
     * 存放待输出数据的缓冲区
     */
    private ByteBuffer writeBuffer;
    /**
     * read 回调事件处理器
     */
    private CompletionHandler<Number, Object> readCompletionHandler;
    /**
     * write 回调事件处理器
     */
    private CompletionHandler<Number, Object> writeCompletionHandler;
    /**
     * read 回调事件关联绑定的附件对象
     */
    private Object readAttachment;
    /**
     * write 回调事件关联绑定的附件对象
     */
    private Object writeAttachment;
    private SelectionKey readSelectionKey;
    /**
     * 中断写操作
     */
    private boolean writeInterrupted;
    private byte readInvoker = AsynchronousChannelGroup.MAX_INVOKER;

    public AsynchronousServerChannel(AsynchronousChannelGroup group, SocketChannel channel, boolean lowMemory)
            throws IOException {
        super(group.provider());
        this.channel = channel;
        this.channelGroup = group;
        readWorker = group.getReadWorker();
        this.lowMemory = lowMemory;
    }

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

    @Override
    public final AsynchronousSocketChannel bind(SocketAddress local) throws IOException {
        channel.bind(local);
        return this;
    }

    @Override
    public final <T> AsynchronousSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        channel.setOption(name, value);
        return this;
    }

    @Override
    public final <T> T getOption(SocketOption<T> name) throws IOException {
        return channel.getOption(name);
    }

    @Override
    public final Set<SocketOption<?>> supportedOptions() {
        return channel.supportedOptions();
    }

    @Override
    public final AsynchronousSocketChannel shutdownInput() throws IOException {
        channel.shutdownInput();
        return this;
    }

    @Override
    public final AsynchronousSocketChannel shutdownOutput() throws IOException {
        channel.shutdownOutput();
        return this;
    }

    @Override
    public final SocketAddress getRemoteAddress() throws IOException {
        return channel.getRemoteAddress();
    }

    @Override
    public <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Void> connect(SocketAddress remote) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        if (timeout > 0) {
            throw new UnsupportedOperationException();
        }
        read0(dst, attachment, handler);
    }

    private <V extends Number, A> void read0(ByteBuffer readBuffer, A attachment,
            CompletionHandler<V, ? super A> handler) {
        if (this.readCompletionHandler != null) {
            throw new ReadPendingException();
        }
        this.readBuffer = readBuffer;
        this.readAttachment = attachment;
        this.readCompletionHandler = (CompletionHandler<Number, Object>) handler;
        doRead(handler instanceof FutureCompletionHandler);
    }

    @Override
    public final Future<Integer> read(ByteBuffer readBuffer) {
        FutureCompletionHandler<Integer, Object> readFuture = new FutureCompletionHandler<>();
        read(readBuffer, 0, TimeUnit.MILLISECONDS, null, readFuture);
        return readFuture;
    }

    @Override
    public final <A> void read(ByteBuffer[] dsts, int offset, int length, long timeout, TimeUnit unit, A attachment,
            CompletionHandler<Long, ? super A> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment,
            CompletionHandler<Integer, ? super A> handler) {
        if (timeout > 0) {
            throw new UnsupportedOperationException();
        }
        write0(src, attachment, handler);
    }

    private <V extends Number, A> void write0(ByteBuffer writeBuffer, A attachment,
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

    @Override
    public final Future<Integer> write(ByteBuffer src) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final <A> void write(ByteBuffer[] srcs, int offset, int length, long timeout, TimeUnit unit, A attachment,
            CompletionHandler<Long, ? super A> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final SocketAddress getLocalAddress() throws IOException {
        return channel.getLocalAddress();
    }

    public final void doRead(boolean direct) {
        try {
            if (readCompletionHandler == null) {
                return;
            }
            if (!channelGroup.running) {
                throw new IOException("channelGroup is shutdown");
            }
            // 此前通过Future调用,且触发了cancel
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

            // 注册至异步线程
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
            // 释放内存
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
                        readSelectionKey = channel.register(selector, SelectionKey.OP_READ,
                                AsynchronousServerChannel.this);
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

    private void resetRead() {
        readCompletionHandler = null;
        readAttachment = null;
        readBuffer = null;
    }

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
                    AsynchronousChannelGroup.interestOps(channelGroup.writeWorker, commonSelectionKey,
                            SelectionKey.OP_WRITE);
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

    private void resetWrite() {
        writeAttachment = null;
        writeCompletionHandler = null;
        writeBuffer = null;
    }

    @Override
    public final boolean isOpen() {
        return channel.isOpen();
    }

}
