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
import org.miaixz.bus.socket.buffer.BufferPagePool;
import org.miaixz.bus.socket.buffer.VirtualBuffer;
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
import java.util.function.Supplier;

/**
 * AIO服务端
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AioServer {

    /**
     * 线程序号
     */
    private static long threadSeqNumber;
    /**
     * 客户端服务配置 调用AioClient的各setXX()方法，都是为了设置config的各配置项
     */
    private final Context context = new Context();
    /**
     * 异步服务器套接字通道
     */
    private AsynchronousServerSocketChannel serverSocketChannel = null;
    /**
     * 异步通道组
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;
    /**
     * 是否开启低内存模式
     */
    private boolean lowMemory = true;
    /**
     * write 内存池
     */
    private BufferPagePool writeBufferPool = null;
    /**
     * read 内存池
     */
    private BufferPagePool readBufferPool = null;

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
        asynchronousChannelGroup = new AsynchronousChannelProvider(lowMemory).openAsynchronousChannelGroup(
                context.getThreadNum(), r -> new Thread(r, "Socket:Thread-" + (threadSeqNumber++)));
        start(asynchronousChannelGroup);
    }

    /**
     * 内部启动逻辑
     *
     * @throws IOException IO异常
     */
    public void start(AsynchronousChannelGroup asynchronousChannelGroup) throws IOException {
        try {
            if (writeBufferPool == null) {
                this.writeBufferPool = BufferPagePool.DEFAULT_BUFFER_PAGE_POOL;
            }
            if (readBufferPool == null) {
                this.readBufferPool = BufferPagePool.DEFAULT_BUFFER_PAGE_POOL;
            }

            this.serverSocketChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup);
            // 设置套接字选项
            if (context.getSocketOptions() != null) {
                for (Map.Entry<SocketOption<Object>, Object> entry : context.getSocketOptions().entrySet()) {
                    this.serverSocketChannel.setOption(entry.getKey(), entry.getValue());
                }
            }
            // 绑定主机
            if (context.getHost() != null) {
                serverSocketChannel.bind(new InetSocketAddress(context.getHost(), context.getPort()),
                        context.getBacklog());
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
        Supplier<VirtualBuffer> readBufferSupplier = () -> readBufferPool.allocateBufferPage()
                .allocate(context.getReadBufferSize());
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
                    createSession(channel, readBufferSupplier);
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
     * @param channel            当前已建立连接通道
     * @param readBufferSupplier
     */
    private void createSession(AsynchronousSocketChannel channel, Supplier<VirtualBuffer> readBufferSupplier) {
        // 连接成功则构造AIOSession对象
        TcpSession session = null;
        AsynchronousSocketChannel acceptChannel = channel;
        try {
            if (context.getMonitor() != null) {
                acceptChannel = context.getMonitor().shouldAccept(channel);
            }
            if (acceptChannel != null) {
                acceptChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
                session = new TcpSession(acceptChannel, this.context, writeBufferPool.allocateBufferPage(),
                        readBufferSupplier);
            } else {
                context.getProcessor().stateEvent(null, Status.REJECT_ACCEPT, null);
                IoKit.close(channel);
            }
        } catch (Exception e) {
            if (session == null) {
                IoKit.close(channel);
            } else {
                session.close();
            }
            context.getProcessor().stateEvent(null, Status.INTERNAL_EXCEPTION, e);
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
    }

    /**
     * 设置读缓存区大小
     *
     * @param size 单位：byte
     * @return this
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
     * @return this
     */
    public <V> AioServer setOption(SocketOption<V> socketOption, V value) {
        context.setOption(socketOption, value);
        return this;
    }

    /**
     * 设置服务工作线程数,设置数值必须大于等于2
     *
     * @param threadNum 线程数
     * @return this
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
     * @return this
     */
    public AioServer setWriteBuffer(int bufferSize, int bufferCapacity) {
        this.context.setWriteBufferSize(bufferSize);
        this.context.setWriteBufferCapacity(bufferCapacity);
        return this;
    }

    /**
     * 设置 backlog 大小
     *
     * @param backlog backlog大小
     * @return this
     */
    public final AioServer setBacklog(int backlog) {
        this.context.setBacklog(backlog);
        return this;
    }

    /**
     * 设置读写内存池。 该方法适用于多个AioQuickServer、AioQuickClient共享内存池的场景， <b>以获得更好的性能表现</b>
     *
     * @param bufferPool 内存池对象
     * @return this
     */
    public AioServer setBufferPagePool(BufferPagePool bufferPool) {
        return setBufferPagePool(bufferPool, bufferPool);
    }

    /**
     * 设置读写内存池。 该方法适用于多个AioQuickServer、AioQuickClient共享内存池的场景， <b>以获得更好的性能表现</b>
     *
     * @param readBufferPool  读内存池对象
     * @param writeBufferPool 写内存池对象
     * @return this
     */
    public AioServer setBufferPagePool(BufferPagePool readBufferPool, BufferPagePool writeBufferPool) {
        this.writeBufferPool = writeBufferPool;
        this.readBufferPool = readBufferPool;
        return this;
    }

    /**
     * 禁用低代码模式
     *
     * @return this
     */
    public AioServer disableLowMemory() {
        this.lowMemory = false;
        return this;
    }

}
