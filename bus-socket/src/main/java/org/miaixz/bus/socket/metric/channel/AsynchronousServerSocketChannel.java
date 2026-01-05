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
package org.miaixz.bus.socket.metric.channel;

import org.miaixz.bus.socket.metric.handler.FutureCompletionHandler;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.*;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * An {@link java.nio.channels.AsynchronousServerSocketChannel} implementation that provides asynchronous server socket
 * operations.
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

    /**
     * Constructs an {@code AsynchronousServerSocketChannel}.
     *
     * @param asynchronousChannelGroup the asynchronous channel group to which this channel belongs
     * @param lowMemory                a boolean indicating whether low memory mode is enabled
     * @throws IOException if an I/O error occurs
     */
    AsynchronousServerSocketChannel(AsynchronousChannelGroup asynchronousChannelGroup, boolean lowMemory)
            throws IOException {
        super(asynchronousChannelGroup.provider());
        this.asynchronousChannelGroup = asynchronousChannelGroup;
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        this.lowMemory = lowMemory;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param local   the local address to bind to
     * @param backlog the backlog to use for the connection
     * @return this channel
     * @throws IOException if an I/O error occurs
     */
    @Override
    public java.nio.channels.AsynchronousServerSocketChannel bind(SocketAddress local, int backlog) throws IOException {
        serverSocketChannel.bind(local, backlog);
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
    public <T> java.nio.channels.AsynchronousServerSocketChannel setOption(SocketOption<T> name, T value)
            throws IOException {
        serverSocketChannel.setOption(name, value);
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
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return serverSocketChannel.getOption(name);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return a set of the socket options supported by this channel
     */
    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return serverSocketChannel.supportedOptions();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param <A>        the type of the attachment
     * @param attachment the object to attach to the I/O operation
     * @param handler    the completion handler
     * @throws AcceptPendingException if an accept operation is already in progress
     */
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

    /**
     * Initiates an asynchronous accept operation.
     */
    public void doAccept() {
        try {
            // If previously called via Future and cancelled, return.
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
                // Do not modify this line of code.
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
            // Register selector for the first time
            else if (selectionKey == null) {
                asynchronousChannelGroup.commonWorker.addRegister(selector -> {
                    try {
                        selectionKey = serverSocketChannel
                                .register(selector, SelectionKey.OP_ACCEPT, AsynchronousServerSocketChannel.this);
                    } catch (ClosedChannelException e) {
                        acceptCompletionHandler.failed(e, attachment);
                    }
                });
            } else {
                AsynchronousChannelGroup
                        .interestOps(asynchronousChannelGroup.commonWorker, selectionKey, SelectionKey.OP_ACCEPT);
            }
        } catch (IOException e) {
            this.acceptCompletionHandler.failed(e, attachment);
        } finally {
            acceptInvoker = 0;
        }
    }

    /**
     * Resets the accept operation state.
     */
    private void resetAccept() {
        acceptPending = false;
        acceptFuture = null;
        acceptCompletionHandler = null;
        attachment = null;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return a {@link Future} representing the pending result
     */
    @Override
    public Future<AsynchronousSocketChannel> accept() {
        FutureCompletionHandler<AsynchronousSocketChannel, Void> acceptFuture = new FutureCompletionHandler<>();
        accept(null, acceptFuture);
        this.acceptFuture = acceptFuture;
        return acceptFuture;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the local address, or {@code null} if the channel is not bound
     * @throws IOException if an I/O error occurs
     */
    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return serverSocketChannel.getLocalAddress();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return {@code true} if, and only if, this channel is open
     */
    @Override
    public boolean isOpen() {
        return serverSocketChannel.isOpen();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        serverSocketChannel.close();
    }

}
