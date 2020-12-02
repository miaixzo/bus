/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.socket;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.logger.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 基于NIO的Socket服务端实现
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class QuickNioServer implements Closeable {

    private static final CompletionAcceptHandler ACCEPT_HANDLER = new CompletionAcceptHandler();

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private ChannelSocketHandler handler;

    /**
     * 构造
     *
     * @param port 端口
     */
    public QuickNioServer(int port) {
        init(new InetSocketAddress(port));
    }

    /**
     * 初始化
     *
     * @param address 地址和端口
     * @return this
     */
    public QuickNioServer init(InetSocketAddress address) {
        try {
            // 打开服务器套接字通道
            this.serverSocketChannel = ServerSocketChannel.open();
            // 设置为非阻塞状态
            this.serverSocketChannel.configureBlocking(false);
            // 绑定端口号
            this.serverSocketChannel.bind(address);

            // 打开一个选择器
            this.selector = Selector.open();
            // 服务器套接字注册到Selector中 并指定Selector监控连接事件
            this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }

        Logger.debug("Server listen on: [{}]...", address);

        return this;
    }

    /**
     * 设置NIO数据处理器
     *
     * @param handler {@link  ChannelSocketHandler}
     * @return this
     */
    public QuickNioServer setChannelHandler(ChannelSocketHandler handler) {
        this.handler = handler;
        return this;
    }

    /**
     * 获取{@link Selector}
     *
     * @return {@link Selector}
     */
    public Selector getSelector() {
        return this.selector;
    }

    /**
     * 启动NIO服务端，即开始监听
     *
     * @see #listen()
     */
    public void start() {
        listen();
    }

    /**
     * 开始监听
     */
    public void listen() {
        try {
            doListen();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 开始监听
     *
     * @throws IOException IO异常
     */
    private void doListen() throws IOException {
        while (this.selector.isOpen() && 0 != this.selector.select()) {
            // 返回已选择键的集合
            final Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
            while (keyIter.hasNext()) {
                handle(keyIter.next());
                keyIter.remove();
            }
        }
    }

    /**
     * 处理SelectionKey
     *
     * @param key SelectionKey
     */
    private void handle(SelectionKey key) {
        // 有客户端接入此服务端
        if (key.isAcceptable()) {
            ACCEPT_HANDLER.completed((ServerSocketChannel) key.channel(), this);
        }

        // 读事件就绪
        if (key.isReadable()) {
            final SocketChannel socketChannel = (SocketChannel) key.channel();
            try {
                handler.handle(socketChannel);
            } catch (Exception e) {
                IoKit.close(socketChannel);
                Logger.error(e);
            }
        }
    }

    @Override
    public void close() {
        IoKit.close(this.selector);
        IoKit.close(this.serverSocketChannel);
    }

}
