/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.socket.origin.aio;

import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.thread.ThreadFactoryBuilder;
import org.aoju.bus.socket.origin.OriginConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

/**
 * Aio Socket客户端
 *
 * @author Kimi Liu
 * @version 3.2.6
 * @since JDK 1.8
 */
public class AioClient {

    private AioSession session;

    /**
     * 构造
     *
     * @param address  地址
     * @param ioAction IO处理类
     */
    public AioClient(InetSocketAddress address, IoAction<ByteBuffer> ioAction) {
        this(address, ioAction, new OriginConfig());
    }

    /**
     * 构造
     *
     * @param address  地址
     * @param ioAction IO处理类
     * @param config   配置项
     */
    public AioClient(InetSocketAddress address, IoAction<ByteBuffer> ioAction, OriginConfig config) {
        this(createChannel(address, config.getThreadPoolSize()), ioAction, config);
    }

    /**
     * 构造
     *
     * @param channel  {@link AsynchronousSocketChannel}
     * @param ioAction IO处理类
     * @param config   配置项
     */
    public AioClient(AsynchronousSocketChannel channel, IoAction<ByteBuffer> ioAction, OriginConfig config) {
        this.session = new AioSession(channel, ioAction, config);
        ioAction.accept(this.session);
    }

    /**
     * 初始化
     *
     * @param address  地址和端口
     * @param poolSize 线程池大小
     * @return this
     */
    private static AsynchronousSocketChannel createChannel(InetSocketAddress address, int poolSize) {

        AsynchronousSocketChannel channel;
        try {
            AsynchronousChannelGroup group = AsynchronousChannelGroup.withFixedThreadPool(//
                    poolSize, // 默认线程池大小
                    ThreadFactoryBuilder.create().setNamePrefix("hi-socket-").build()//
            );
            channel = AsynchronousSocketChannel.open(group);
        } catch (IOException e) {
            throw new CommonException(e);
        }

        try {
            channel.connect(address).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new CommonException(e);
        }
        return channel;
    }

    /**
     * 设置 Socket 的 Option 选项
     * 选项见：{@link java.net.StandardSocketOptions}
     *
     * @param <T>   选项泛型
     * @param name  {@link SocketOption} 枚举
     * @param value SocketOption参数
     * @return the object
     * @throws IOException IO异常
     */
    public <T> AioClient setOption(SocketOption<T> name, T value) throws IOException {
        this.session.getChannel().setOption(name, value);
        return this;
    }

    /**
     * 获取IO处理器
     *
     * @return {@link IoAction}
     */
    public IoAction<ByteBuffer> getIoAction() {
        return this.session.getIoAction();
    }

    /**
     * 从服务端读取数据
     *
     * @return this
     */
    public AioClient read() {
        this.session.read();
        return this;
    }

    /**
     * 写数据到服务端
     *
     * @param data 字节
     * @return this
     */
    public AioClient write(ByteBuffer data) {
        this.session.write(data);
        return this;
    }

    /**
     * 关闭客户端
     */
    public void close() {
        this.session.close();
    }

}
