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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.socket.Context;
import org.miaixz.bus.socket.Handler;
import org.miaixz.bus.socket.Message;
import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.buffer.BufferPagePool;
import org.miaixz.bus.socket.metric.channels.AsynchronousChannelProvider;

/**
 * AIO实现的客户端服务
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class AioClient {

    /**
     * 健康检查
     */
    private static final ScheduledExecutorService CONNECT_TIMEOUT_EXECUTOR = Executors
            .newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "connection-timeout-monitor");
                thread.setDaemon(true);
                return thread;
            });
    /**
     * 客户端服务配置 调用AioClient的各setXX()方法，都是为了设置各配置项
     */
    private final Context context = new Context();
    /**
     * 网络连接的会话对象
     *
     * @see TcpSession
     */
    private TcpSession session;
    /**
     * IO事件处理线程组 作为客户端，该AsynchronousChannelGroup只需保证2个长度的线程池大小即可满足通信读写所需。
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;
    /**
     * 绑定本地地址
     */
    private SocketAddress localAddress;
    /**
     * 连接超时时间
     */
    private int connectTimeout;

    /**
     * write 内存池
     */
    private BufferPagePool writeBufferPool = null;
    /**
     * read 内存池
     */
    private BufferPagePool readBufferPool = null;
    /**
     * 是否开启低内存模式
     */
    private boolean lowMemory = true;

    /**
     * 当前构造方法设置了启动Aio客户端的必要参数，基本实现开箱即用。
     *
     * @param host    远程服务器地址
     * @param port    远程服务器端口号
     * @param message 协议编解码
     * @param handler 消息处理器
     */
    public <T> AioClient(String host, int port, Message<T> message, Handler<T> handler) {
        context.setHost(host);
        context.setPort(port);
        context.setProtocol(message);
        context.setProcessor(handler);
    }

    /**
     * 采用异步的方式启动客户端
     *
     * @param attachment 可传入回调方法中的附件对象
     * @param handler    异步回调
     * @param <A>        附件对象类型
     * @throws IOException
     */
    public <A> void start(A attachment, CompletionHandler<Session, ? super A> handler) throws IOException {
        this.asynchronousChannelGroup = new AsynchronousChannelProvider(lowMemory).openAsynchronousChannelGroup(2,
                Thread::new);
        start(asynchronousChannelGroup, attachment, handler);
    }

    /**
     * 采用异步的方式启动客户端
     *
     * @param asynchronousChannelGroup 通信线程资源组
     * @param attachment               可传入回调方法中的附件对象
     * @param handler                  异步回调
     * @param <A>                      附件对象类型
     * @throws IOException
     */
    public <A> void start(AsynchronousChannelGroup asynchronousChannelGroup, A attachment,
            CompletionHandler<Session, ? super A> handler) throws IOException {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open(asynchronousChannelGroup);
        if (connectTimeout > 0) {
            CONNECT_TIMEOUT_EXECUTOR.schedule(() -> {
                if (session == null) {
                    IoKit.close(socketChannel);
                    shutdownNow();
                }
            }, connectTimeout, TimeUnit.MILLISECONDS);
        }
        if (writeBufferPool == null) {
            this.writeBufferPool = BufferPagePool.DEFAULT_BUFFER_PAGE_POOL;
        }
        if (readBufferPool == null) {
            this.readBufferPool = BufferPagePool.DEFAULT_BUFFER_PAGE_POOL;
        }
        // set socket options
        if (context.getSocketOptions() != null) {
            for (Map.Entry<SocketOption<Object>, Object> entry : context.getSocketOptions().entrySet()) {
                socketChannel.setOption(entry.getKey(), entry.getValue());
            }
        }
        // bind host
        if (localAddress != null) {
            socketChannel.bind(localAddress);
        }
        socketChannel.connect(new InetSocketAddress(context.getHost(), context.getPort()), socketChannel,
                new CompletionHandler<Void, AsynchronousSocketChannel>() {
                    @Override
                    public void completed(Void result, AsynchronousSocketChannel socketChannel) {
                        try {
                            AsynchronousSocketChannel connectedChannel = socketChannel;
                            if (context.getMonitor() != null) {
                                connectedChannel = context.getMonitor().shouldAccept(socketChannel);
                            }
                            if (connectedChannel == null) {
                                throw new RuntimeException("Monitor refuse channel");
                            }
                            // 连接成功则构造Session对象
                            session = new TcpSession(connectedChannel, context, writeBufferPool.allocateBufferPage(),
                                    () -> readBufferPool.allocateBufferPage().allocate(context.getReadBufferSize()));
                            handler.completed(session, attachment);
                        } catch (Exception e) {
                            failed(e, socketChannel);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, AsynchronousSocketChannel socketChannel) {
                        try {
                            handler.failed(exc, attachment);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (socketChannel != null) {
                                IoKit.close(socketChannel);
                            }
                            shutdownNow();
                        }
                    }
                });
    }

    /**
     * 启动客户端。 在与服务端建立连接期间，该方法处于阻塞状态。直至连接建立成功，或者发生异常。 该start方法支持外部指定AsynchronousChannelGroup，实现多个客户端共享一组线程池资源，有效提升资源利用率。
     *
     * @param asynchronousChannelGroup IO事件处理线程组
     * @return 建立连接后的会话对象
     * @throws IOException IOException
     * @see AsynchronousSocketChannel#connect(SocketAddress)
     */
    public Session start(AsynchronousChannelGroup asynchronousChannelGroup) throws IOException {
        CompletableFuture<Session> future = new CompletableFuture<>();
        start(asynchronousChannelGroup, future, new CompletionHandler<>() {
            @Override
            public void completed(Session session, CompletableFuture<Session> future) {
                if (future.isDone() || future.isCancelled()) {
                    session.close();
                } else {
                    future.complete(session);
                }
            }

            @Override
            public void failed(Throwable exc, CompletableFuture<Session> future) {
                future.completeExceptionally(exc);
            }
        });
        try {
            return future.get();
        } catch (Exception e) {
            future.cancel(false);
            shutdownNow();
            throw new IOException(e.getCause() == null ? e : e.getCause());
        }
    }

    public TcpSession getSession() {
        return session;
    }

    /**
     * 启动客户端。 本方法会构建线程数为2的{@code asynchronousChannelGroup}，并通过调用{@link AioClient#start(AsynchronousChannelGroup)}启动服务。
     *
     * @return 建立连接后的会话对象
     * @throws IOException IOException
     * @see AioClient#start(AsynchronousChannelGroup)
     */
    public Session start() throws IOException {
        this.asynchronousChannelGroup = new AsynchronousChannelProvider(lowMemory).openAsynchronousChannelGroup(2,
                Thread::new);
        return start(asynchronousChannelGroup);
    }

    /**
     * 停止客户端服务.
     * 调用该方法会触发Session的close方法，并且如果当前客户端若是通过执行{@link AioClient#start()}方法构建的，同时会触发asynchronousChannelGroup的shutdown动作。
     */
    public void shutdown() {
        shutdown0(false);
    }

    /**
     * 立即关闭客户端
     */
    public void shutdownNow() {
        shutdown0(true);
    }

    /**
     * 停止client
     *
     * @param flag 是否立即停止
     */
    private synchronized void shutdown0(boolean flag) {
        if (session != null) {
            session.close(flag);
            session = null;
        }
        // 仅Client内部创建的ChannelGroup需要shutdown
        if (asynchronousChannelGroup != null) {
            asynchronousChannelGroup.shutdown();
            asynchronousChannelGroup = null;
        }
    }

    /**
     * 设置读缓存区大小
     *
     * @param size 单位：byte
     * @return 当前AioClient对象
     */
    public AioClient setReadBufferSize(int size) {
        this.context.setReadBufferSize(size);
        return this;
    }

    /**
     * 设置Socket的TCP参数配置
     * <p>
     * AIO客户端的有效可选范围为： 1. StandardSocketOptions.SO_SNDBUF 2. StandardSocketOptions.SO_RCVBUF 3.
     * StandardSocketOptions.SO_KEEPALIVE 4. StandardSocketOptions.SO_REUSEADDR 5. StandardSocketOptions.TCP_NODELAY
     * </p>
     *
     * @param socketOption 配置项
     * @param value        配置值
     * @param <V>          泛型
     * @return 当前客户端实例
     */
    public <V> AioClient setOption(SocketOption<V> socketOption, V value) {
        context.setOption(socketOption, value);
        return this;
    }

    /**
     * 绑定本机地址、端口用于连接远程服务
     *
     * @param local 若传null则由系统自动获取
     * @param port  若传0则由系统指定
     * @return 当前客户端实例
     */
    public AioClient bindLocal(String local, int port) {
        localAddress = local == null ? new InetSocketAddress(port) : new InetSocketAddress(local, port);
        return this;
    }

    /**
     * 设置内存池。 通过该方法设置的内存池，在AioClient执行shutdown时不会触发内存池的释放。 该方法适用于多个AioServer、AioClient共享内存池的场景。
     * <b>在启用内存池的情况下会有更好的性能表现</b>
     *
     * @param bufferPool 内存池对象
     * @return 当前客户端实例
     */
    public AioClient setBufferPagePool(BufferPagePool bufferPool) {
        return setBufferPagePool(bufferPool, bufferPool);
    }

    public AioClient setBufferPagePool(BufferPagePool readBufferPool, BufferPagePool writeBufferPool) {
        this.writeBufferPool = writeBufferPool;
        this.readBufferPool = readBufferPool;
        return this;
    }

    /**
     * 设置输出缓冲区容量
     *
     * @param bufferSize     单个内存块大小
     * @param bufferCapacity 内存块数量上限
     * @return 当前客户端实例
     */
    public AioClient setWriteBuffer(int bufferSize, int bufferCapacity) {
        context.setWriteBufferSize(bufferSize);
        context.setWriteBufferCapacity(bufferCapacity);
        return this;
    }

    /**
     * 客户端连接超时时间，单位:毫秒
     *
     * @param timeout 超时时间
     * @return 当前客户端实例
     */
    public AioClient connectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    /**
     * 禁用低代码模式
     *
     * @return
     */
    public AioClient disableLowMemory() {
        this.lowMemory = false;
        return this;
    }

}
