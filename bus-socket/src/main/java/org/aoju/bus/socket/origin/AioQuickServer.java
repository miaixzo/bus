/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.socket.origin;

import org.aoju.bus.core.io.BufferPool;
import org.aoju.bus.core.io.EventFactory;
import org.aoju.bus.core.io.RingBuffer;
import org.aoju.bus.logger.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * AIO服务端
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8+
 */
public class AioQuickServer<T> {

    /**
     * Server端服务配置
     * <p>调用AioQuickServer的各setXX()方法,都是为了设置config的各配置项</p>
     */
    protected ServerConfig<T> config = new ServerConfig<>();
    protected BufferPool bufferPool;
    /**
     * 读回调事件处理
     */
    protected TcpReadHandler<T> aioReadCompletionHandler;
    /**
     * 写回调事件处理
     */
    protected TcpWriteHandler<T> aioWriteCompletionHandler;
    private Function<AsynchronousSocketChannel, TcpAioSession<T>> aioSessionFunction;
    private AsynchronousServerSocketChannel serverSocketChannel = null;
    private AsynchronousChannelGroup asynchronousChannelGroup;
    private Thread acceptThread = null;
    private volatile boolean running = true;

    /**
     * 设置服务端启动必要参数配置
     *
     * @param port             绑定服务端口号
     * @param protocol         协议编解码
     * @param messageProcessor 消息处理器
     */
    public AioQuickServer(int port, Protocol<T> protocol, Message<T> messageProcessor) {
        config.setPort(port);
        config.setProtocol(protocol);
        config.setProcessor(messageProcessor);
        config.setThreadNum(Runtime.getRuntime().availableProcessors());
    }

    /**
     * @param host             绑定服务端Host地址
     * @param port             绑定服务端口号
     * @param protocol         协议编解码
     * @param messageProcessor 消息处理器
     */
    public AioQuickServer(String host, int port, Protocol<T> protocol, Message<T> messageProcessor) {
        this(port, protocol, messageProcessor);
        config.setHost(host);
    }

    /**
     * 启动Server端的AIO服务
     *
     * @throws IOException 异常
     */
    public void start() throws IOException {
        start0(channel -> new TcpAioSession<>(channel, config, aioReadCompletionHandler, aioWriteCompletionHandler, bufferPool.allocateBufferPage()));
    }

    /**
     * 内部启动逻辑
     *
     * @param aioSessionFunction 会话信息
     * @throws IOException 异常
     */
    protected final void start0(Function<AsynchronousSocketChannel, TcpAioSession<T>> aioSessionFunction) throws IOException {
        //确保单核CPU默认初始化至少2个线程
        if (config.getThreadNum() == 1) {
            config.setThreadNum(2);
        }
        int threadNum = config.getThreadNum();
        try {

            ThreadLocal<CompletionHandler> recursionThreadLocal = new ThreadLocal<>();
            RingBuffer<TcpReadEvent> buffer = new RingBuffer<>(config.getReadBacklog(), new EventFactory<TcpReadEvent>() {
                @Override
                public TcpReadEvent newInstance() {
                    return new TcpReadEvent();
                }

                @Override
                public void restEntity(TcpReadEvent entity) {
                    entity.setReadSize(-1);
                    entity.setSession(null);
                }
            });
            aioReadCompletionHandler = new TcpReadHandler<>(buffer, recursionThreadLocal, new Semaphore(threadNum - 1));
            aioWriteCompletionHandler = new TcpWriteHandler<>();
            this.bufferPool = new BufferPool(ServerConfig.getIntProperty(ServerConfig.Property.SERVER_PAGE_SIZE, 1024 * 1024), ServerConfig.getIntProperty(ServerConfig.Property.BUFFER_PAGE_NUM, threadNum), ServerConfig.getBoolProperty(ServerConfig.Property.SERVER_PAGE_IS_DIRECT, true));
            this.aioSessionFunction = aioSessionFunction;

            asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(threadNum, new ThreadFactory() {
                byte index = 0;

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "bus-socket:Thread-" + (++index));
                }
            });
            this.serverSocketChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup);
            //set socket options
            if (config.getSocketOptions() != null) {
                for (Map.Entry<SocketOption<Object>, Object> entry : config.getSocketOptions().entrySet()) {
                    this.serverSocketChannel.setOption(entry.getKey(), entry.getValue());
                }
            }
            //bind host
            if (config.getHost() != null) {
                serverSocketChannel.bind(new InetSocketAddress(config.getHost(), config.getPort()), 1000);
            } else {
                serverSocketChannel.bind(new InetSocketAddress(config.getPort()), 1000);
            }
            acceptThread = new Thread(new Runnable() {
                NetMonitor<T> monitor = config.getMonitor();

                @Override
                public void run() {
                    Future<AsynchronousSocketChannel> nextFuture = serverSocketChannel.accept();
                    while (running) {
                        try {
                            final AsynchronousSocketChannel channel = nextFuture.get();
                            nextFuture = serverSocketChannel.accept();
                            if (monitor == null || monitor.acceptMonitor(channel)) {
                                createSession(channel);
                            } else {
                                config.getProcessor().stateEvent(null, StateMachine.REJECT_ACCEPT, null);
                                Logger.warn("reject accept channel:{}", channel);
                                closeChannel(channel);
                            }
                        } catch (Exception e) {
                            Logger.error("AcceptThread Exception", e);
                        }

                    }
                }
            }, "bus-socket:AcceptThread");
            acceptThread.start();
        } catch (IOException e) {
            shutdown();
            throw e;
        }
        Logger.info("server started on port {},threadNum:{}", config.getPort(), threadNum);
        Logger.info("server config is {}", config);
    }

    /**
     * 为每个新建立的连接创建AIOSession对象
     *
     * @param channel 当前已建立连接通道
     */
    private void createSession(AsynchronousSocketChannel channel) {
        //连接成功则构造AIOSession对象
        TcpAioSession<T> session = null;
        try {
            session = aioSessionFunction.apply(channel);
            session.initSession();
        } catch (Exception e1) {
            Logger.error(e1.getMessage(), e1);
            if (session == null) {
                closeChannel(channel);
            } else {
                session.close();
            }
        }
    }

    private void closeChannel(AsynchronousSocketChannel channel) {
        try {
            channel.shutdownInput();
        } catch (IOException e) {
            Logger.debug(e.getMessage(), e);
        }
        try {
            channel.shutdownOutput();
        } catch (IOException e) {
            Logger.debug(e.getMessage(), e);
        }
        try {
            channel.close();
        } catch (IOException e) {
            Logger.debug("close channel exception", e);
        }
    }

    /**
     * 停止服务端
     */
    public final void shutdown() {
        running = false;
        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
                serverSocketChannel = null;
            }
        } catch (IOException e) {
            Logger.warn(e.getMessage(), e);
        }

        if (!asynchronousChannelGroup.isTerminated()) {
            try {
                asynchronousChannelGroup.shutdownNow();
            } catch (IOException e) {
                Logger.error("shutdown exception", e);
            }
        }
        try {
            asynchronousChannelGroup.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.error("shutdown exception", e);
        }
    }

    /**
     * 设置读缓存区大小
     *
     * @param size 单位：byte
     * @return the object
     */
    public final AioQuickServer<T> setReadBufferSize(int size) {
        this.config.setReadBufferSize(size);
        return this;
    }

    /**
     * 是否启用控制台Banner打印
     *
     * @param bannerEnabled true:启用,false:禁用
     * @return the object
     */
    public final AioQuickServer<T> setBannerEnabled(boolean bannerEnabled) {
        config.setBannerEnabled(bannerEnabled);
        return this;
    }

    /**
     * 设置Socket的TCP参数配置
     * <p>
     * AIO客户端的有效可选范围为：
     * 2. StandardSocketOptions.SO_RCVBUF
     * 4. StandardSocketOptions.SO_REUSEADDR
     * </p>
     *
     * @param <V>          对象
     * @param socketOption 配置项
     * @param value        配置值
     * @return the object
     */
    public final <V> AioQuickServer<T> setOption(SocketOption<V> socketOption, V value) {
        config.setOption(socketOption, value);
        return this;
    }

    /**
     * 设置write缓冲区容量
     *
     * @param writeQueueCapacity 缓冲区大小
     * @return the object
     */
    public final AioQuickServer<T> setWriteQueueCapacity(int writeQueueCapacity) {
        config.setWriteQueueCapacity(writeQueueCapacity);
        return this;
    }

    /**
     * 设置服务工作线程数,设置数值必须大于等于2
     *
     * @param threadNum 线程数
     * @return the object
     */
    public final AioQuickServer<T> setThreadNum(int threadNum) {
        if (threadNum <= 1) {
            throw new InvalidParameterException("threadNum must >= 2");
        }
        config.setThreadNum(threadNum);
        return this;
    }

}
