/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.socket.origin.nio;

import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * NIO客户端
 *
 * @author Kimi Liu
 * @version 3.6.8
 * @since JDK 1.8+
 */
public class NioClient {

    private SocketChannel channel;

    /**
     * 构造
     *
     * @param host 服务器地址
     * @param port 端口
     */
    public NioClient(String host, int port) {
        init(new InetSocketAddress(host, port));
    }

    /**
     * 构造
     *
     * @param address 服务器地址
     */
    public NioClient(InetSocketAddress address) {
        init(address);
    }

    /**
     * 初始化
     *
     * @param address 地址和端口
     * @return this
     */
    public NioClient init(InetSocketAddress address) {
        try {
            this.channel = SocketChannel.open(address);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return this;
    }

    /**
     * 处理读事件
     * 当收到读取准备就绪的信号后，回调此方法，用户可读取从客户端传世来的消息
     *
     * @param buffer 服务端数据存储缓存
     * @return the object
     */
    public NioClient read(ByteBuffer buffer) {
        try {
            this.channel.read(buffer);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return this;
    }

    /**
     * 实现写逻辑
     * 当收到写出准备就绪的信号后，回调此方法，用户可向客户端发送消息
     *
     * @param datas 发送的数据
     * @return the object
     */
    public NioClient write(ByteBuffer... datas) {
        try {
            this.channel.write(datas);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return this;
    }

}
