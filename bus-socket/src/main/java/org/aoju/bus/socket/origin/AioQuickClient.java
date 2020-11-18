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
 ********************************************************************************/
package org.aoju.bus.socket.origin;

import org.aoju.bus.core.io.BufferPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * AIO实现的客户端服务
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
public class AioQuickClient<T> {

    /**
     * 客户端服务配置
     * 调用AioQuickClient的各setXX()方法,都是为了设置config的各配置项
     */
    protected ServerConfig<T> config = new ServerConfig<>();
    /**
     * 网络连接的会话对象
     *
     * @see TcpAioSession
     */
    protected TcpAioSession<T> session;
    protected BufferPool bufferPool = null;
    /**
     * IO事件处理线程组
     * <p>
     * 作为客户端,该AsynchronousChannelGroup只需保证2个长度的线程池大小即可满足通信读写所需
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;

    /**
     * 绑定本地地址
     */
    private SocketAddress localAddress;

    /**
     * 当前构造方法设置了启动Aio客户端的必要参数,基本实现开箱即用
     *
     * @param host             远程服务器地址
     * @param port             远程服务器端口号
     * @param protocol         协议编解码
     * @param messageProcessor 消息处理器
     */
    public AioQuickClient(String host, int port, Protocol<T> protocol, Message<T> messageProcessor) {
        config.setHost(host);
        config.setPort(port);
        config.setProtocol(protocol);
        config.setProcessor(messageProcessor);
    }

    /**
     * 启动客户端
     * 在与服务端建立连接期间,该方法处于阻塞状态 直至连接建立成功,或者发生异常
     * 该start方法支持外部指定AsynchronousChannelGroup,实现多个客户端共享一组线程池资源,有效提升资源利用率
     *
     * @param asynchronousChannelGroup IO事件处理线程组
     * @return the object
     * @throws IOException          异常
     * @throws ExecutionException   异常
     * @throws InterruptedException 异常
     * @see AsynchronousSocketChannel#connect(SocketAddress)
     */
    public AioSession<T> start(AsynchronousChannelGroup asynchronousChannelGroup) throws IOException, ExecutionException, InterruptedException {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open(asynchronousChannelGroup);
        if (bufferPool == null) {
            bufferPool = new BufferPool(ServerConfig.getIntProperty(ServerConfig.Property.CLIENT_PAGE_SIZE, 1024 * 256), 1, ServerConfig.getBoolProperty(ServerConfig.Property.CLIENT_PAGE_IS_DIRECT, true));
        }
        //set socket options
        if (config.getSocketOptions() != null) {
            for (Map.Entry<SocketOption<Object>, Object> entry : config.getSocketOptions().entrySet()) {
                socketChannel.setOption(entry.getKey(), entry.getValue());
            }
        }
        //bind host
        if (localAddress != null) {
            socketChannel.bind(localAddress);
        }
        socketChannel.connect(new InetSocketAddress(config.getHost(), config.getPort())).get();
        //连接成功则构造AIOSession对象
        session = new TcpAioSession<>(socketChannel, config, new TcpReadHandler<>(), new TcpWriteHandler<>(), bufferPool.allocateBufferPage());
        session.initSession();
        return session;
    }

    /**
     * 启动客户端
     * 本方法会构建线程数为2的{@code asynchronousChannelGroup}, 并通过调用
     * {@link AioQuickClient#start(AsynchronousChannelGroup)}启动服务
     *
     * @return the object
     * @throws IOException          异常
     * @throws ExecutionException   异常
     * @throws InterruptedException 异常
     * @see AioQuickClient#start(AsynchronousChannelGroup)
     */
    public final AioSession<T> start() throws IOException, ExecutionException, InterruptedException {
        this.asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(2, r -> new Thread(r));
        return start(asynchronousChannelGroup);
    }

    /**
     * 停止客户端服务.
     * 调用该方法会触发AioSession的close方法,并且如果当前客户端若是通过执行
     * {@link AioQuickClient#start()}方法构建的,
     * 同时会触发asynchronousChannelGroup的shutdown动作
     */
    public final void shutdown() {
        showdown0(false);
    }

    /**
     * 立即关闭客户端
     */
    public final void shutdownNow() {
        showdown0(true);
    }

    private void showdown0(boolean flag) {
        if (session != null) {
            session.close(flag);
            session = null;
        }
        //仅Client内部创建的ChannelGroup需要shutdown
        if (asynchronousChannelGroup != null) {
            asynchronousChannelGroup.shutdown();
        }
    }

    /**
     * 设置读缓存区大小
     *
     * @param size 单位：byte
     * @return the object
     */
    public final AioQuickClient<T> setReadBufferSize(int size) {
        this.config.setReadBufferSize(size);
        return this;
    }

    /**
     * 设置Socket的TCP参数配置
     * AIO客户端的有效可选范围为：
     * 1. StandardSocketOptions.SO_SNDBUF
     * 2. StandardSocketOptions.SO_RCVBUF
     * 3. StandardSocketOptions.SO_KEEPALIVE
     * 4. StandardSocketOptions.SO_REUSEADDR
     * 5. StandardSocketOptions.TCP_NODELAY
     *
     * @param <V>          对象
     * @param socketOption 配置项
     * @param value        配置值
     * @return the object
     */
    public final <V> AioQuickClient<T> setOption(SocketOption<V> socketOption, V value) {
        config.setOption(socketOption, value);
        return this;
    }

    /**
     * 绑定本机地址、端口用于连接远程服务
     *
     * @param local 若传null则由系统自动获取
     * @param port  若传0则由系统指定
     * @return the object
     */
    public final AioQuickClient<T> bindLocal(String local, int port) {
        localAddress = local == null ? new InetSocketAddress(port) : new InetSocketAddress(local, port);
        return this;
    }

    public final AioQuickClient<T> setBufferPagePool(BufferPool bufferPool) {
        this.bufferPool = bufferPool;
        return this;
    }

    /**
     * 设置write缓冲区容量
     *
     * @param writeQueueCapacity 缓冲区大小
     * @return the object
     */
    public final AioQuickClient<T> setWriteQueueCapacity(int writeQueueCapacity) {
        config.setWriteQueueCapacity(writeQueueCapacity);
        return this;
    }

}
