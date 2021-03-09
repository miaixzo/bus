/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org sandao and other contributors.               *
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

import org.aoju.bus.core.io.ByteBuffer;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.socket.handler.CompletionReadHandler;
import org.aoju.bus.socket.handler.CompletionWriteHandler;
import org.aoju.bus.socket.handler.ConcurrentReadHandler;
import org.aoju.bus.socket.process.MessageProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * AIO服务端
 *
 * @param <T> 消息对象类型
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
public class AioQuickServer<T> {

    private static final String BUS_ASYNCHRONOUS_CHANNEL_PROVIDER = "org.aoju.bus.socket.channel.AsynchronousChannelProvider";
    private static final String AIO_ASYNCHRONOUS_CHANNEL_PROVIDER = "java.nio.channels.spi.AsynchronousChannelProvider";

    /**
     * Server端服务配置。
     * 调用AioQuickServer的各setXX()方法，都是为了设置config的各配置项
     */
    private final ServerConfig<T> config = new ServerConfig<>();
    /**
     * 内存池
     */
    private ByteBuffer bufferPool;
    /**
     * 读回调事件处理
     */
    private CompletionReadHandler<T> aioCompletionReadHandler;
    /**
     * 写回调事件处理
     */
    private CompletionWriteHandler<T> aioCompletionWriteHandler;
    private ByteBuffer innerBufferPool = null;

    /**
     * 连接会话实例化Function
     */
    private Function<AsynchronousSocketChannel, TcpAioSession<T>> aioSessionFunction;
    /**
     * asynchronousServerSocketChannel
     */
    private AsynchronousServerSocketChannel serverSocketChannel = null;
    /**
     * asynchronousChannelGroup
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;

    private BufferFactory.VirtualBufferFactory readBufferFactory = bufferPage -> bufferPage.allocate(config.getReadBufferSize());

    /**
     * 设置服务端启动必要参数配置
     *
     * @param port             绑定服务端口号
     * @param protocol         协议编解码
     * @param messageProcessor 消息处理器
     */
    public AioQuickServer(int port, Protocol<T> protocol, MessageProcessor<T> messageProcessor) {
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
    public AioQuickServer(String host, int port, Protocol<T> protocol, MessageProcessor<T> messageProcessor) {
        this(port, protocol, messageProcessor);
        config.setHost(host);
    }

    /**
     * 启动Server端的AIO服务
     *
     * @throws IOException IO异常
     */
    public void start() throws IOException {
        start0(channel -> new TcpAioSession<>(channel, config, aioCompletionReadHandler, aioCompletionWriteHandler, bufferPool.allocatePageBuffer()));
    }

    /**
     * 内部启动逻辑
     *
     * @param aioSessionFunction 实例化会话的Function
     * @throws IOException IO异常
     */
    private void start0(Function<AsynchronousSocketChannel, TcpAioSession<T>> aioSessionFunction) throws IOException {
        checkAndResetConfig();
        try {
            aioCompletionWriteHandler = new CompletionWriteHandler<>();
            if (bufferPool == null) {
                this.bufferPool = config.getBufferFactory().create();
                this.innerBufferPool = bufferPool;
            }
            this.aioSessionFunction = aioSessionFunction;
            if (BUS_ASYNCHRONOUS_CHANNEL_PROVIDER.equals(System.getProperty(AIO_ASYNCHRONOUS_CHANNEL_PROVIDER))) {
                aioCompletionReadHandler = new CompletionReadHandler<>();
            } else {
                aioCompletionReadHandler = new ConcurrentReadHandler<>(new Semaphore(config.getThreadNum() - 1));
            }
            asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(config.getThreadNum(), new ThreadFactory() {
                private byte index = 0;

                @Override
                public Thread newThread(Runnable r) {
                    return bufferPool.newThread(r, "bus-socket:Thread-" + (++index));
                }
            });
            this.serverSocketChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup);
            // 设置socket属性
            if (config.getSocketOptions() != null) {
                for (Map.Entry<SocketOption<Object>, Object> entry : config.getSocketOptions().entrySet()) {
                    this.serverSocketChannel.setOption(entry.getKey(), entry.getValue());
                }
            }
            // 绑定地址
            if (config.getHost() != null) {
                serverSocketChannel.bind(new InetSocketAddress(config.getHost(), config.getPort()), config.getBacklog());
            } else {
                serverSocketChannel.bind(new InetSocketAddress(config.getPort()), config.getBacklog());
            }

            startAcceptThread();
        } catch (IOException e) {
            shutdown();
            throw e;
        }
        System.out.println("bus-socket server started on port " + config.getPort() + ",threadNum:" + config.getThreadNum());
        System.out.println("bus-socket server config is " + config);
    }

    private void startAcceptThread() {
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel channel, Void attachment) {
                createSession(channel);
                try {
                    serverSocketChannel.accept(attachment, this);
                } catch (Throwable throwable) {
                    config.getProcessor().stateEvent(null, SocketStatus.ACCEPT_EXCEPTION, throwable);
                    failed(throwable, attachment);
                    serverSocketChannel.accept(attachment, this);
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
            }
        });
    }

    /**
     * 检查配置项
     */
    private void checkAndResetConfig() {
        // 确保单核CPU默认初始化至少2个线程
        if (config.getThreadNum() == 1) {
            config.setThreadNum(2);
        }
    }

    /**
     * 为每个新建立的连接创建AIOSession对象
     *
     * @param channel 当前已建立连接通道
     */
    private void createSession(AsynchronousSocketChannel channel) {
        // 连接成功则构造AIOSession对象
        TcpAioSession<T> session = null;
        AsynchronousSocketChannel acceptChannel = channel;
        try {
            if (config.getMonitor() != null) {
                acceptChannel = config.getMonitor().shouldAccept(channel);
            }
            if (acceptChannel != null) {
                acceptChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
                session = aioSessionFunction.apply(acceptChannel);
                session.initSession(readBufferFactory.newBuffer(bufferPool.allocatePageBuffer()));
            } else {
                config.getProcessor().stateEvent(null, SocketStatus.REJECT_ACCEPT, null);
                IoKit.close(channel);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (session == null) {
                IoKit.close(channel);
            } else {
                session.close();
            }
        }
    }

    /**
     * 停止服务端
     */
    public final void shutdown() {
        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
                serverSocketChannel = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!asynchronousChannelGroup.isTerminated()) {
            try {
                asynchronousChannelGroup.shutdownNow();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            asynchronousChannelGroup.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (innerBufferPool != null) {
            innerBufferPool.release();
        }
        aioCompletionReadHandler.shutdown();
    }

    /**
     * 设置读缓存区大小
     *
     * @param size 单位：byte
     * @return 当前AioQuickServer对象
     */
    public final AioQuickServer<T> setReadBufferSize(int size) {
        this.config.setReadBufferSize(size);
        return this;
    }

    /**
     * 设置Socket的TCP参数配置
     * AIO客户端的有效可选范围为：
     * 2. StandardSocketOptions.SO_RCVBUF
     * 4. StandardSocketOptions.SO_REUSEADDR
     *
     * @param socketOption 配置项
     * @param value        配置值
     * @param <V>          配置项类型
     * @return 当前AioQuickServer对象
     */
    public final <V> AioQuickServer<T> setOption(SocketOption<V> socketOption, V value) {
        config.setOption(socketOption, value);
        return this;
    }

    /**
     * 设置服务工作线程数,设置数值必须大于等于2
     *
     * @param threadNum 线程数
     * @return 当前AioQuickServer对象
     */
    public final AioQuickServer<T> setThreadNum(int threadNum) {
        if (threadNum <= 1) {
            throw new InvalidParameterException("threadNum must >= 2");
        }
        config.setThreadNum(threadNum);
        return this;
    }


    /**
     * 设置输出缓冲区容量
     *
     * @param bufferSize     单个内存块大小
     * @param bufferCapacity 内存块数量上限
     * @return 当前AioQuickServer对象
     */
    public final AioQuickServer<T> setWriteBuffer(int bufferSize, int bufferCapacity) {
        config.setWriteBufferSize(bufferSize);
        config.setWriteBufferCapacity(bufferCapacity);
        return this;
    }

    /**
     * 设置 backlog 大小
     *
     * @param backlog backlog大小
     * @return 当前AioQuickServer对象
     */
    public final AioQuickServer<T> setBacklog(int backlog) {
        config.setBacklog(backlog);
        return this;
    }

    /**
     * 设置内存池。
     * 通过该方法设置的内存池，在AioQuickServer执行shutdown时不会触发内存池的释放。
     * 该方法适用于多个AioQuickServer、AioQuickClient共享内存池的场景。
     * 在启用内存池的情况下会有更好的性能表现
     *
     * @param bufferPool 内存池对象
     * @return 当前AioQuickServer对象
     */
    public final AioQuickServer<T> setPageBufferPool(ByteBuffer bufferPool) {
        this.bufferPool = bufferPool;
        this.config.setBufferFactory(BufferFactory.DISABLED_BUFFER_FACTORY);
        return this;
    }

    /**
     * 设置内存池的构造工厂。
     * 通过工厂形式生成的内存池会强绑定到当前AioQuickServer对象，
     * 在AioQuickServer执行shutdown时会释放内存池
     * 在启用内存池的情况下会有更好的性能表现
     *
     * @param bufferFactory 内存池工厂
     * @return 当前AioQuickServer对象
     */
    public final AioQuickServer<T> setBufferFactory(BufferFactory bufferFactory) {
        this.config.setBufferFactory(bufferFactory);
        this.bufferPool = null;
        return this;
    }

    public final AioQuickServer<T> setReadBufferFactory(BufferFactory.VirtualBufferFactory readBufferFactory) {
        this.readBufferFactory = readBufferFactory;
        return this;
    }

}
