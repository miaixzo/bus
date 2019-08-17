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
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.socket.origin.OriginConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;

/**
 * 基于AIO的Socket服务端实现
 *
 * @author Kimi Liu
 * @version 3.0.9
 * @since JDK 1.8
 */
public class AioServer {

    private static AcceptHandler ACCEPT_HANDLER = new AcceptHandler();
    protected IoAction<ByteBuffer> ioAction;
    protected OriginConfig config;
    private AsynchronousChannelGroup group;
    private AsynchronousServerSocketChannel channel;

    /**
     * 构造
     *
     * @param port 端口
     */
    public AioServer(int port) {
        this(new InetSocketAddress(port), new OriginConfig());
    }

    /**
     * 构造
     *
     * @param address 地址
     * @param config  {@link OriginConfig} 配置项
     */
    public AioServer(InetSocketAddress address, OriginConfig config) {
        this.config = config;
        init(address);
    }

    /**
     * 初始化
     *
     * @param address 地址和端口
     * @return this
     */
    public AioServer init(InetSocketAddress address) {
        try {
            this.group = AsynchronousChannelGroup.withFixedThreadPool(//
                    config.getThreadPoolSize(), // 默认线程池大小
                    ThreadFactoryBuilder.create().setNamePrefix("Aoju-socket-").build()//
            );
            this.channel = AsynchronousServerSocketChannel.open(group).bind(address);
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return this;
    }

    /**
     * 开始监听
     *
     * @param sync 是否阻塞
     */
    public void start(boolean sync) {
        try {
            doStart(sync);
        } catch (IOException e) {
            throw new CommonException(e);
        }
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
    public <T> AioServer setOption(SocketOption<T> name, T value) throws IOException {
        this.channel.setOption(name, value);
        return this;
    }

    /**
     * 获取IO处理器
     *
     * @return {@link IoAction}
     */
    public IoAction<ByteBuffer> getIoAction() {
        return this.ioAction;
    }

    /**
     * 设置IO处理器，单例存在
     *
     * @param ioAction {@link IoAction}
     * @return this;
     */
    public AioServer setIoAction(IoAction<ByteBuffer> ioAction) {
        this.ioAction = ioAction;
        return this;
    }

    /**
     * 获取{@link AsynchronousServerSocketChannel}
     *
     * @return {@link AsynchronousServerSocketChannel}
     */
    public AsynchronousServerSocketChannel getChannel() {
        return this.channel;
    }

    /**
     * 处理接入的客户端
     *
     * @return this
     */
    public AioServer accept() {
        this.channel.accept(this, ACCEPT_HANDLER);
        return this;
    }

    /**
     * 服务是否开启状态
     *
     * @return 服务是否开启状态
     */
    public boolean isOpen() {
        return (null == this.channel) ? false : this.channel.isOpen();
    }

    /**
     * 关闭服务
     */
    public void close() {
        IoUtils.close(this.channel);

        if (null != this.group && false == this.group.isShutdown()) {
            try {
                this.group.shutdownNow();
            } catch (IOException e) {
                // ignore
            }
        }

        // 结束阻塞
        synchronized (this) {
            this.notify();
        }
    }

    /**
     * 开始监听
     *
     * @param sync 是否阻塞
     * @throws IOException IO异常
     */
    private void doStart(boolean sync) throws IOException {
        // 接收客户端连接
        accept();

        if (sync) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

}
