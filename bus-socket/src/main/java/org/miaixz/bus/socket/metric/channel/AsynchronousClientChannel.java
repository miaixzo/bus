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
import java.nio.channels.*;
import java.util.concurrent.Future;

/**
 * An {@link AsynchronousSocketChannel} implementation that simulates the JDK7 AIO processing style. This class extends
 * {@link AsynchronousServerChannel} to provide client-side asynchronous socket operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
final class AsynchronousClientChannel extends AsynchronousServerChannel {

    /**
     * The {@link AsynchronousChannelGroup} responsible for handling I/O events for this connection.
     */
    private final AsynchronousChannelGroup group;

    /**
     * Constructs an {@code AsynchronousClientChannel}.
     *
     * @param group     the asynchronous channel group to which this channel belongs
     * @param channel   the underlying {@link SocketChannel}
     * @param lowMemory a boolean indicating whether low memory mode is enabled
     * @throws IOException if an I/O error occurs
     */
    public AsynchronousClientChannel(AsynchronousChannelGroup group, SocketChannel channel, boolean lowMemory)
            throws IOException {
        super(group, channel, lowMemory);
        this.group = group;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param <A>        the type of the attachment
     * @param remote     the remote address to connect to
     * @param attachment the object to attach to the I/O operation
     * @param handler    the completion handler
     * @throws ShutdownChannelGroupException if the channel group has been shut down
     * @throws AlreadyConnectedException     if this channel is already connected
     * @throws ConnectionPendingException    if a connection operation is already in progress
     */
    @Override
    public <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler) {
        if (group.isTerminated()) {
            throw new ShutdownChannelGroupException();
        }
        if (channel.isConnected()) {
            throw new AlreadyConnectedException();
        }
        if (channel.isConnectionPending()) {
            throw new ConnectionPendingException();
        }
        doConnect(remote, attachment, handler);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param remote the remote address to connect to
     * @return a {@link Future} representing the pending result
     */
    @Override
    public Future<Void> connect(SocketAddress remote) {
        FutureCompletionHandler<Void, Void> connectFuture = new FutureCompletionHandler<>();
        connect(remote, null, connectFuture);
        return connectFuture;
    }

    /**
     * Initiates an asynchronous connection to a remote address.
     *
     * @param <A>               the type of the attachment object
     * @param remote            the remote address to connect to
     * @param attachment        an object to attach to the operation, for use by the completion handler
     * @param completionHandler the handler to invoke when the connection operation completes
     */
    public <A> void doConnect(
            SocketAddress remote,
            A attachment,
            CompletionHandler<Void, ? super A> completionHandler) {
        try {
            // If previously called via Future and cancelled, return.
            if (completionHandler instanceof FutureCompletionHandler
                    && ((FutureCompletionHandler) completionHandler).isDone()) {
                return;
            }
            boolean connected = channel.isConnectionPending();
            if (connected || channel.connect(remote)) {
                connected = channel.finishConnect();
            }
            // Do not modify this line of code.
            channel.configureBlocking(false);
            if (connected) {
                completionHandler.completed(null, attachment);
            } else {
                group.commonWorker.addRegister(selector -> {
                    try {
                        channel.register(
                                selector,
                                SelectionKey.OP_CONNECT,
                                (Runnable) () -> doConnect(remote, attachment, completionHandler));
                    } catch (ClosedChannelException e) {
                        completionHandler.failed(e, attachment);
                    }
                });
            }
        } catch (IOException e) {
            completionHandler.failed(e, attachment);
        }
    }

}
