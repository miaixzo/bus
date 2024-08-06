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

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.concurrent.Future;

import org.miaixz.bus.socket.metric.handler.FutureCompletionHandler;

/**
 * 模拟JDK7的AIO处理方式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
final class AsynchronousClientChannel extends AsynchronousServerChannel {

    /**
     * 处理当前连接IO事件的资源组
     */
    private final AsynchronousChannelGroup group;

    public AsynchronousClientChannel(AsynchronousChannelGroup group, SocketChannel channel, boolean lowMemory)
            throws IOException {
        super(group, channel, lowMemory);
        this.group = group;
    }

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

    @Override
    public Future<Void> connect(SocketAddress remote) {
        FutureCompletionHandler<Void, Void> connectFuture = new FutureCompletionHandler<>();
        connect(remote, null, connectFuture);
        return connectFuture;
    }

    public <A> void doConnect(SocketAddress remote, A attachment,
            CompletionHandler<Void, ? super A> completionHandler) {
        try {
            // 此前通过Future调用,且触发了cancel
            if (completionHandler instanceof FutureCompletionHandler
                    && ((FutureCompletionHandler) completionHandler).isDone()) {
                return;
            }
            boolean connected = channel.isConnectionPending();
            if (connected || channel.connect(remote)) {
                connected = channel.finishConnect();
            }
            // 这行代码不要乱动
            channel.configureBlocking(false);
            if (connected) {
                completionHandler.completed(null, attachment);
            } else {
                group.commonWorker.addRegister(selector -> {
                    try {
                        channel.register(selector, SelectionKey.OP_CONNECT,
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
