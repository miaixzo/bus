/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
import java.nio.channels.*;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * 异步服务套接字通道
 *
 * @author Kimi Liu
 * @since Java 17+
 */
final class AsynchronousServerSocketChannel extends java.nio.channels.AsynchronousServerSocketChannel {

    private final ServerSocketChannel serverSocketChannel;
    private final AsynchronousChannelGroup asynchronousChannelGroup;
    private final boolean lowMemory;
    private CompletionHandler<AsynchronousSocketChannel, Object> acceptCompletionHandler;
    private FutureCompletionHandler<AsynchronousSocketChannel, Void> acceptFuture;
    private Object attachment;
    private SelectionKey selectionKey;
    private boolean acceptPending;
    private int acceptInvoker;

    AsynchronousServerSocketChannel(AsynchronousChannelGroup asynchronousChannelGroup, boolean lowMemory)
            throws IOException {
        super(asynchronousChannelGroup.provider());
        this.asynchronousChannelGroup = asynchronousChannelGroup;
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        this.lowMemory = lowMemory;
    }

    @Override
    public java.nio.channels.AsynchronousServerSocketChannel bind(SocketAddress local, int backlog) throws IOException {
        serverSocketChannel.bind(local, backlog);
        return this;
    }

    @Override
    public <T> java.nio.channels.AsynchronousServerSocketChannel setOption(SocketOption<T> name, T value)
            throws IOException {
        serverSocketChannel.setOption(name, value);
        return this;
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return serverSocketChannel.getOption(name);
    }

    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return serverSocketChannel.supportedOptions();
    }

    @Override
    public <A> void accept(A attachment, CompletionHandler<AsynchronousSocketChannel, ? super A> handler) {
        if (acceptPending) {
            throw new AcceptPendingException();
        }
        acceptPending = true;
        this.acceptCompletionHandler = (CompletionHandler<AsynchronousSocketChannel, Object>) handler;
        this.attachment = attachment;
        doAccept();
    }

    public void doAccept() {
        try {
            // 此前通过Future调用,且触发了cancel
            if (acceptFuture != null && acceptFuture.isDone()) {
                resetAccept();
                AsynchronousChannelGroup.removeOps(selectionKey, SelectionKey.OP_ACCEPT);
                return;
            }
            SocketChannel socketChannel = null;
            if (acceptInvoker++ < AsynchronousChannelGroup.MAX_INVOKER) {
                socketChannel = serverSocketChannel.accept();
            }
            if (socketChannel != null) {
                AsynchronousServerChannel asynchronousSocketChannel = new AsynchronousServerChannel(
                        asynchronousChannelGroup, socketChannel, lowMemory);
                // 这行代码不要乱动
                socketChannel.configureBlocking(false);
                socketChannel.finishConnect();
                CompletionHandler<AsynchronousSocketChannel, Object> completionHandler = acceptCompletionHandler;
                Object attach = attachment;
                resetAccept();
                completionHandler.completed(asynchronousSocketChannel, attach);
                if (!acceptPending && selectionKey != null) {
                    AsynchronousChannelGroup.removeOps(selectionKey, SelectionKey.OP_ACCEPT);
                }
            }
            // 首次注册selector
            else if (selectionKey == null) {
                asynchronousChannelGroup.commonWorker.addRegister(selector -> {
                    try {
                        selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT,
                                AsynchronousServerSocketChannel.this);
                    } catch (ClosedChannelException e) {
                        acceptCompletionHandler.failed(e, attachment);
                    }
                });
            } else {
                AsynchronousChannelGroup.interestOps(asynchronousChannelGroup.commonWorker, selectionKey,
                        SelectionKey.OP_ACCEPT);
            }
        } catch (IOException e) {
            this.acceptCompletionHandler.failed(e, attachment);
        } finally {
            acceptInvoker = 0;
        }
    }

    private void resetAccept() {
        acceptPending = false;
        acceptFuture = null;
        acceptCompletionHandler = null;
        attachment = null;
    }

    @Override
    public Future<AsynchronousSocketChannel> accept() {
        FutureCompletionHandler<AsynchronousSocketChannel, Void> acceptFuture = new FutureCompletionHandler<>();
        accept(null, acceptFuture);
        this.acceptFuture = acceptFuture;
        return acceptFuture;
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return serverSocketChannel.getLocalAddress();
    }

    @Override
    public boolean isOpen() {
        return serverSocketChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        serverSocketChannel.close();
    }

}
