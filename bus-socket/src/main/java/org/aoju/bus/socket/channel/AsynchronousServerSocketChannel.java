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
import java.nio.channels.*;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author Kimi Liu
 * @version 6.3.0
 * @since JDK 1.8+
 */
public class AsynchronousServerSocketChannel extends java.nio.channels.AsynchronousServerSocketChannel {

    private final ServerSocketChannel serverSocketChannel;
    private final AsynchronousChannelGroup asynchronousChannelGroup;
    private final AsynchronousChannelGroup.Worker acceptWorker;
    private CompletionHandler<java.nio.channels.AsynchronousSocketChannel, Object> acceptCompletionHandler;
    private FutureCompletionHandler<java.nio.channels.AsynchronousSocketChannel, Void> acceptFuture;
    private Object attachment;
    private SelectionKey selectionKey;
    private boolean acceptPending;

    protected AsynchronousServerSocketChannel(AsynchronousChannelGroup asynchronousChannelGroup) throws IOException {
        super(asynchronousChannelGroup.provider());
        this.asynchronousChannelGroup = asynchronousChannelGroup;
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        acceptWorker = asynchronousChannelGroup.getAcceptWorker();
    }

    @Override
    public java.nio.channels.AsynchronousServerSocketChannel bind(SocketAddress local, int backlog) throws IOException {
        serverSocketChannel.bind(local, backlog);
        return this;
    }

    @Override
    public <T> java.nio.channels.AsynchronousServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
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
    public <A> void accept(A attachment, CompletionHandler<java.nio.channels.AsynchronousSocketChannel, ? super A> handler) {
        if (acceptPending) {
            throw new AcceptPendingException();
        }
        acceptPending = true;
        this.acceptCompletionHandler = (CompletionHandler<java.nio.channels.AsynchronousSocketChannel, Object>) handler;
        this.attachment = attachment;
        doAccept();
    }

    public void doAccept() {
        try {
            // 此前通过Future调用,且触发了cancel
            if (null != acceptFuture && acceptFuture.isDone()) {
                resetAccept();
                asynchronousChannelGroup.removeOps(selectionKey, SelectionKey.OP_ACCEPT);
                return;
            }
            boolean directAccept = (acceptWorker.getWorkerThread() == Thread.currentThread()
                    && acceptWorker.invoker++ < AsynchronousChannelGroup.MAX_INVOKER);
            SocketChannel socketChannel = null;
            if (directAccept) {
                socketChannel = serverSocketChannel.accept();
            }
            if (null != socketChannel) {
                AsynchronousSocketChannel asynchronousSocketChannel = new AsynchronousSocketChannel(asynchronousChannelGroup, socketChannel);
                socketChannel.finishConnect();
                CompletionHandler<java.nio.channels.AsynchronousSocketChannel, Object> completionHandler = acceptCompletionHandler;
                Object attach = attachment;
                resetAccept();
                completionHandler.completed(asynchronousSocketChannel, attach);
                if (!acceptPending && null != selectionKey) {
                    asynchronousChannelGroup.removeOps(selectionKey, SelectionKey.OP_ACCEPT);
                }
            }
            // 首次注册selector
            else if (null == selectionKey) {
                acceptWorker.addRegister(selector -> {
                    try {
                        selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                        selectionKey.attach(AsynchronousServerSocketChannel.this);
                    } catch (ClosedChannelException e) {
                        acceptCompletionHandler.failed(e, attachment);
                    }
                });
            } else {
                asynchronousChannelGroup.interestOps(acceptWorker, selectionKey, SelectionKey.OP_ACCEPT);
            }
        } catch (IOException e) {
            this.acceptCompletionHandler.failed(e, attachment);
        }

    }

    private void resetAccept() {
        acceptPending = false;
        acceptFuture = null;
        acceptCompletionHandler = null;
        attachment = null;
    }

    @Override
    public Future<java.nio.channels.AsynchronousSocketChannel> accept() {
        FutureCompletionHandler<java.nio.channels.AsynchronousSocketChannel, Void> acceptFuture = new FutureCompletionHandler<>();
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
