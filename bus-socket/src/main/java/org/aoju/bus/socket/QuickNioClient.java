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
import org.aoju.bus.core.toolkit.ThreadKit;
import org.aoju.bus.socket.handler.ChannelSocketHandler;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * NIO客户端
 *
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
public class QuickNioClient implements Closeable {

    private Selector selector;
    private SocketChannel channel;
    private ChannelSocketHandler handler;

    /**
     * 构造
     *
     * @param host 服务器地址
     * @param port 端口
     */
    public QuickNioClient(String host, int port) {
        init(new InetSocketAddress(host, port));
    }

    /**
     * 构造
     *
     * @param address 服务器地址
     */
    public QuickNioClient(InetSocketAddress address) {
        init(address);
    }

    /**
     * 初始化
     *
     * @param address 地址和端口
     * @return this
     */
    public QuickNioClient init(InetSocketAddress address) {
        try {
            //创建一个SocketChannel对象，配置成非阻塞模式
            this.channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(address);

            //创建一个选择器，并把SocketChannel交给selector对象
            this.selector = Selector.open();
            channel.register(this.selector, SelectionKey.OP_READ);

            // 等待建立连接
            while (false == channel.finishConnect()) {
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return this;
    }

    /**
     * 设置NIO数据处理器
     *
     * @param handler {@link ChannelSocketHandler}
     * @return this
     */
    public QuickNioClient setChannelHandler(ChannelSocketHandler handler) {
        this.handler = handler;
        return this;
    }

    /**
     * 开始监听
     */
    public void listen() {
        ThreadKit.execute(() -> {
            try {
                doListen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
        // 读事件就绪
        if (key.isReadable()) {
            final SocketChannel socketChannel = (SocketChannel) key.channel();
            try {
                handler.handle(socketChannel);
            } catch (Exception e) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 实现写逻辑<br>
     * 当收到写出准备就绪的信号后，回调此方法，用户可向客户端发送消息
     *
     * @param datas 发送的数据
     * @return this
     */
    public QuickNioClient write(ByteBuffer... datas) {
        try {
            this.channel.write(datas);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return this;
    }

    /**
     * 获取SocketChannel
     *
     * @return SocketChannel
     */
    public SocketChannel getChannel() {
        return this.channel;
    }

    @Override
    public void close() {
        IoKit.close(this.selector);
        IoKit.close(this.channel);
    }

}
