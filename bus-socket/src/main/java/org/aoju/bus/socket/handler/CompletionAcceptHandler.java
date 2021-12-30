/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.socket.handler;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.socket.NioQuickServer;

import java.io.IOException;
import java.nio.channels.*;

/**
 * 接入完成回调，单例使用
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public class CompletionAcceptHandler implements CompletionHandler<ServerSocketChannel, NioQuickServer> {

    /**
     * 注册通道的指定操作到指定Selector上
     *
     * @param selector Selector
     * @param channel  通道
     * @param ops      注册的通道监听（操作）类型
     */
    public static void registerChannel(Selector selector, SelectableChannel channel, int ops) {
        try {
            if (null == channel) {
                return;
            }
            channel.configureBlocking(false);
            // 注册通道
            channel.register(selector, ops);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    @Override
    public void completed(ServerSocketChannel serverSocketChannel, NioQuickServer nioQuickServer) {
        SocketChannel socketChannel;
        try {
            // 获取连接到此服务器的客户端通道
            socketChannel = serverSocketChannel.accept();
            Logger.debug("Client [{}] accepted.", socketChannel.getRemoteAddress());
        } catch (IOException e) {
            throw new InstrumentException(e);
        }

        // SocketChannel通道的可读事件注册到Selector中
        registerChannel(nioQuickServer.getSelector(), socketChannel, SelectionKey.OP_READ);
    }

    @Override
    public void failed(Throwable exc, NioQuickServer nioQuickServer) {
        Logger.error(exc);
    }

}
