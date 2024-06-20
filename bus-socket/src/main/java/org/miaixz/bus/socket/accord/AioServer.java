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
package org.miaixz.bus.socket.accord;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.socket.Context;
import org.miaixz.bus.socket.Handler;
import org.miaixz.bus.socket.Message;
import org.miaixz.bus.socket.Status;
import org.miaixz.bus.socket.buffer.*;
import org.miaixz.bus.socket.metric.channels.AsynchronousChannelProvider;

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
import java.util.function.Function;

/**
 * AIO服务端
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class AioServer {

    private static long threadSeqNumber;
    /**
     * 客户端服务配置
     * 调用AioClient的各setXX()方法，都是为了设置config的各配置项
     */
    private final Context context = new Context();
    /**
     * 内存池
     */
    private BufferPagePool innerBufferPool = null;
    /**
     * asynchronousServerSocketChannel
     */
    private AsynchronousServerSocketChannel serverSocketChannel = null;
    /**
     * asynchronousChannelGroup
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;
    /**
     * 是否开启低内存模式
     */
    private boolean lowMemory;
    /**
     * 内存池
     */
    private BufferPagePool bufferPool = null;

    private VirtualBufferFactory readBufferFactory = bufferPage -> bufferPage.allocate(context.getReadBufferSize());

    /**
     * 设置服务端启动必要参数配置
     *
     * @param port    绑定服务端口号
     * @param message 协议编解码
     * @param handler 消息处理器
     */
    public <T> AioServer(int port, Message<T> message, Handler<T> handler) {
        context.setPort(port);
        context.setProtocol(message);
        context.setProcessor(handler);
        context.setThreadNum(Runtime.getRuntime().availableProcessors());
    }

    /**
     * @param host    绑定服务端Host地址
     * @param port    绑定服务端口号
     * @param message 协议编解码
     * @param handler 消息处理器
     */
    public <T> AioServer(String host, int port, Message<T> message, Handler<T> handler) {
        this(port, message, handler);
        context.setHost(host);
    }

    /**
     * 启动Server端的AIO服务
     *
     * @throws IOException IO异常
     */
    public void start() throws IOException {
        if (bufferPool == null) {
            this.bufferPool = context.getBufferFactory().create();
            this.innerBufferPool = bufferPool;
        }
        asynchronousChannelGroup = new AsynchronousChannelProvider(lowMemory).openAsynchronousChannelGroup(context.getThreadNum(), r -> new Thread(r, "Socket:Thread-" + (threadSeqNumber++)));
        start(asynchronousChannelGroup);
    }

    /**
     * 内部启动逻辑
     *
     * @throws IOException IO异常
     */
    public void start(AsynchronousChannelGroup asynchronousChannelGroup) throws IOException {
        try {
            if (bufferPool == null) {
                this.bufferPool = context.getBufferFactory().create();
                this.innerBufferPool = bufferPool;
            }

            this.serverSocketChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup);
            // set socket options
            if (context.getSocketOptions() != null) {
                for (Map.Entry<SocketOption<Object>, Object> entry : context.getSocketOptions().entrySet()) {
                    this.serverSocketChannel.setOption(entry.getKey(), entry.getValue());
                }
            }
            // bind host
            if (context.getHost() != null) {
                serverSocketChannel.bind(new InetSocketAddress(context.getHost(), context.getPort()), context.getBacklog());
            } else {
                serverSocketChannel.bind(new InetSocketAddress(context.getPort()), context.getBacklog());
            }

            startAcceptThread();
        } catch (IOException e) {
            shutdown();
            throw e;
        }
    }

    private void startAcceptThread() {
        Function<BufferPage, VirtualBuffer> function = bufferPage -> readBufferFactory.newBuffer(bufferPage);
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel channel, Void attachment) {
                try {
                    serverSocketChannel.accept(attachment, this);
                } catch (Throwable throwable) {
                    context.getProcessor().stateEvent(null, Status.ACCEPT_EXCEPTION, throwable);
                    failed(throwable, attachment);
                    serverSocketChannel.accept(attachment, this);
                } finally {
                    createSession(channel, function);
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
            }
        });
    }

    /**
     * 为每个新建立的连接创建Session对象
     *
     * @param channel 当前已建立连接通道
     */
    private void createSession(AsynchronousSocketChannel channel, Function<BufferPage, VirtualBuffer> function) {
        // 连接成功则构造Session对象
        TcpSession session = null;
        AsynchronousSocketChannel acceptChannel = channel;
        try {
            if (context.getMonitor() != null) {
                acceptChannel = context.getMonitor().shouldAccept(channel);
            }
            if (acceptChannel != null) {
                acceptChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
                session = new TcpSession(acceptChannel, this.context, bufferPool.allocateBufferPage(), function);
            } else {
                context.getProcessor().stateEvent(null, Status.REJECT_ACCEPT, null);
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
    public void shutdown() {
        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
                serverSocketChannel = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (asynchronousChannelGroup != null) {
            asynchronousChannelGroup.shutdown();
        }
        if (innerBufferPool != null) {
            innerBufferPool.release();
        }
    }

    /**
     * 设置读缓存区大小
     *
     * @param size 单位：byte
     * @return 当前AioServer对象
     */
    public AioServer setReadBufferSize(int size) {
        this.context.setReadBufferSize(size);
        return this;
    }

    /**
     * 设置Socket的TCP参数配置。
     * <p>
     * AIO客户端的有效可选范围为：<br/>
     * 2. StandardSocketOptions.SO_RCVBUF<br/>
     * 4. StandardSocketOptions.SO_REUSEADDR<br/>
     * </p>
     *
     * @param socketOption 配置项
     * @param value        配置值
     * @param <V>          配置项类型
     * @return 当前AioServer对象
     */
    public <V> AioServer setOption(SocketOption<V> socketOption, V value) {
        context.setOption(socketOption, value);
        return this;
    }

    /**
     * 设置服务工作线程数,设置数值必须大于等于2
     *
     * @param threadNum 线程数
     * @return 当前AioServer对象
     */
    public AioServer setThreadNum(int threadNum) {
        if (threadNum <= 1) {
            throw new InvalidParameterException("threadNum must >= 2");
        }
        context.setThreadNum(threadNum);
        return this;
    }

    /**
     * 设置输出缓冲区容量
     *
     * @param bufferSize     单个内存块大小
     * @param bufferCapacity 内存块数量上限
     * @return 当前AioServer对象
     */
    public AioServer setWriteBuffer(int bufferSize, int bufferCapacity) {
        context.setWriteBufferSize(bufferSize);
        context.setWriteBufferCapacity(bufferCapacity);
        return this;
    }

    /**
     * 设置 backlog 大小
     *
     * @param backlog backlog大小
     * @return 当前AioServer对象
     */
    public AioServer setBacklog(int backlog) {
        context.setBacklog(backlog);
        return this;
    }

    /**
     * 设置内存池。
     * 通过该方法设置的内存池，在AioServer执行shutdown时不会触发内存池的释放。
     * 该方法适用于多个AioServer、AioClient共享内存池的场景。
     * <b>在启用内存池的情况下会有更好的性能表现</b>
     *
     * @param bufferPool 内存池对象
     * @return 当前AioServer对象
     */
    public AioServer setBufferPagePool(BufferPagePool bufferPool) {
        this.bufferPool = bufferPool;
        this.context.setBufferFactory(BufferFactory.DISABLED_BUFFER_FACTORY);
        return this;
    }

    /**
     * 设置内存池的构造工厂。
     * 通过工厂形式生成的内存池会强绑定到当前AioServer对象，
     * 在AioServer执行shutdown时会释放内存池。
     * <b>在启用内存池的情况下会有更好的性能表现</b>
     *
     * @param bufferFactory 内存池工厂
     * @return 当前AioServer对象
     */
    public AioServer setBufferFactory(BufferFactory bufferFactory) {
        this.context.setBufferFactory(bufferFactory);
        this.bufferPool = null;
        return this;
    }

    public AioServer setReadBufferFactory(VirtualBufferFactory readBufferFactory) {
        this.readBufferFactory = readBufferFactory;
        return this;
    }

    public AioServer setLowMemory(boolean lowMemory) {
        this.lowMemory = lowMemory;
        return this;
    }

}
