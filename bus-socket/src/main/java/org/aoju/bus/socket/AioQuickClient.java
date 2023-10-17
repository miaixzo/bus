/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org sandao and other contributors.               *
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

import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.socket.buffers.BufferFactory;
import org.aoju.bus.socket.buffers.BufferPool;
import org.aoju.bus.socket.buffers.VirtualFactory;
import org.aoju.bus.socket.handler.ReadCompletionHandler;
import org.aoju.bus.socket.handler.WriteCompletionHandler;
import org.aoju.bus.socket.process.MessageProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * AIO实现的客户端服务
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class AioQuickClient {

    /**
     * 客户端服务配置。
     * 调用AioQuickClient的各setXX()方法，都是为了设置config的各配置项
     */
    private final ServerConfig config = new ServerConfig();
    /**
     * 网络连接的会话对象
     */
    private TcpAioSession session;
    /**
     * 内存池
     */
    private BufferPool bufferPool = null;

    private BufferPool innerBufferPool = null;
    /**
     * IO事件处理线程组。
     * 作为客户端，该AsynchronousChannelGroup只需保证2个长度的线程池大小即可满足通信读写所需。
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

    private VirtualFactory readBufferFactory = bufferPage -> bufferPage.allocate(config.getReadBufferSize());

    /**
     * 当前构造方法设置了启动Aio客户端的必要参数，基本实现开箱即用。
     *
     * @param host             远程服务器地址
     * @param port             远程服务器端口号
     * @param protocol         协议编解码
     * @param messageProcessor 消息处理器
     */
    public <T> AioQuickClient(String host, int port, Protocol<T> protocol, MessageProcessor<T> messageProcessor) {
        config.setHost(host);
        config.setPort(port);
        config.setProtocol(protocol);
        config.setProcessor(messageProcessor);
    }

    /**
     * 采用异步的方式启动客户端
     *
     * @param attachment 可传入回调方法中的附件对象
     * @param handler    异步回调
     * @param <A>        附件对象类型
     * @throws IOException
     */
    public <A> void start(A attachment,
                          CompletionHandler<AioSession, ? super A> handler) throws IOException {
        this.asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(2, Thread::new);
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
                          CompletionHandler<AioSession, ? super A> handler) throws IOException {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open(asynchronousChannelGroup);
        if (bufferPool == null) {
            bufferPool = config.getBufferFactory().create();
            this.innerBufferPool = bufferPool;
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
        socketChannel.connect(new InetSocketAddress(config.getHost(), config.getPort()), socketChannel, new CompletionHandler<Void, AsynchronousSocketChannel>() {
            @Override
            public void completed(Void result, AsynchronousSocketChannel socketChannel) {
                try {
                    AsynchronousSocketChannel connectedChannel = socketChannel;
                    if (config.getMonitor() != null) {
                        connectedChannel = config.getMonitor().shouldAccept(socketChannel);
                    }
                    if (connectedChannel == null) {
                        throw new RuntimeException("NetMonitor refuse channel");
                    }
                    //连接成功则构造AIOSession对象
                    session = new TcpAioSession(connectedChannel, config, new ReadCompletionHandler(), new WriteCompletionHandler(), bufferPool.allocateBufferPage(), () -> readBufferFactory.newBuffer(bufferPool.allocateBufferPage()));
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
     * 启动客户端。
     * 在与服务端建立连接期间，该方法处于阻塞状态。直至连接建立成功，或者发生异常。
     * 该start方法支持外部指定AsynchronousChannelGroup，实现多个客户端共享一组线程池资源，有效提升资源利用率。
     *
     * @param asynchronousChannelGroup IO事件处理线程组
     * @return 建立连接后的会话对象
     * @throws IOException IOException
     * @see AsynchronousSocketChannel#connect(SocketAddress)
     */
    public AioSession start(AsynchronousChannelGroup asynchronousChannelGroup) throws IOException {
        CompletableFuture<AioSession> future = new CompletableFuture<>();
        start(asynchronousChannelGroup, future, new CompletionHandler<AioSession, CompletableFuture<AioSession>>() {
            @Override
            public void completed(AioSession session, CompletableFuture<AioSession> future) {
                if (future.isDone() || future.isCancelled()) {
                    session.close();
                } else {
                    future.complete(session);
                }
            }

            @Override
            public void failed(Throwable exc, CompletableFuture<AioSession> future) {
                future.completeExceptionally(exc);
            }
        });
        try {
            if (connectTimeout > 0) {
                return future.get(connectTimeout, TimeUnit.MILLISECONDS);
            } else {
                return future.get();
            }
        } catch (Exception e) {
            future.cancel(false);
            shutdownNow();
            throw new IOException(e);
        }
    }

    public TcpAioSession getSession() {
        return session;
    }

    /**
     * 启动客户端。
     * 本方法会构建线程数为2的{@code asynchronousChannelGroup}，并通过调用{@link AioQuickClient#start(AsynchronousChannelGroup)}启动服务。
     *
     * @return 建立连接后的会话对象
     * @throws IOException IOException
     * @see AioQuickClient#start(AsynchronousChannelGroup)
     */
    public final AioSession start() throws IOException {
        this.asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(2, Thread::new);
        return start(asynchronousChannelGroup);
    }

    /**
     * 停止客户端服务.
     * 调用该方法会触发AioSession的close方法，并且如果当前客户端若是通过执行{@link AioQuickClient#start()}方法构建的，同时会触发asynchronousChannelGroup的shutdown动作。
     */
    public final void shutdown() {
        shutdown0(false);
    }

    /**
     * 立即关闭客户端
     */
    public final void shutdownNow() {
        shutdown0(true);
    }

    /**
     * 停止client
     *
     * @param flag 是否立即停止
     */
    private void shutdown0(boolean flag) {
        if (session != null) {
            session.close(flag);
            session = null;
        }
        //仅Client内部创建的ChannelGroup需要shutdown
        if (asynchronousChannelGroup != null) {
            asynchronousChannelGroup.shutdown();
            asynchronousChannelGroup = null;
        }
        if (innerBufferPool != null) {
            innerBufferPool.release();
            innerBufferPool = null;
            bufferPool = null;
        }
    }

    /**
     * 设置读缓存区大小
     *
     * @param size 单位：byte
     * @return 当前AIOQuickClient对象
     */
    public final AioQuickClient setReadBufferSize(int size) {
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
     * @param socketOption 配置项
     * @param value        配置值
     * @param <V>          泛型
     * @return 当前客户端实例
     */
    public final <V> AioQuickClient setOption(SocketOption<V> socketOption, V value) {
        config.setOption(socketOption, value);
        return this;
    }

    /**
     * 绑定本机地址、端口用于连接远程服务
     *
     * @param local 若传null则由系统自动获取
     * @param port  若传0则由系统指定
     * @return 当前客户端实例
     */
    public final AioQuickClient bindLocal(String local, int port) {
        localAddress = local == null ? new InetSocketAddress(port) : new InetSocketAddress(local, port);
        return this;
    }

    /**
     * 设置内存池。
     * 通过该方法设置的内存池，在AioQuickClient执行shutdown时不会触发内存池的释放。
     * 该方法适用于多个AioQuickServer、AioQuickClient共享内存池的场景。
     * <b>在启用内存池的情况下会有更好的性能表现</b>
     *
     * @param bufferPool 内存池对象
     * @return 当前客户端实例
     */
    public final AioQuickClient setBufferPagePool(BufferPool bufferPool) {
        this.bufferPool = bufferPool;
        this.config.setBufferFactory(BufferFactory.DISABLED_BUFFER_FACTORY);
        return this;
    }

    /**
     * 设置内存池的构造工厂。
     * 通过工厂形式生成的内存池会强绑定到当前AioQuickClient对象，
     * 在AioQuickClient执行shutdown时会释放内存池。
     * <b>在启用内存池的情况下会有更好的性能表现</b>
     *
     * @param bufferFactory 内存池工厂
     * @return 当前客户端实例
     */
    public final AioQuickClient setBufferFactory(BufferFactory bufferFactory) {
        this.config.setBufferFactory(bufferFactory);
        this.bufferPool = null;
        return this;
    }

    /**
     * 设置输出缓冲区容量
     *
     * @param bufferSize     单个内存块大小
     * @param bufferCapacity 内存块数量上限
     * @return 当前客户端实例
     */
    public final AioQuickClient setWriteBuffer(int bufferSize, int bufferCapacity) {
        config.setWriteBufferSize(bufferSize);
        config.setWriteBufferCapacity(bufferCapacity);
        return this;
    }

    /**
     * 客户端连接超时时间，单位:毫秒
     *
     * @param timeout 超时时间
     * @return 当前客户端实例
     */
    public final AioQuickClient connectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    public final AioQuickClient setReadBufferFactory(VirtualFactory readBufferFactory) {
        this.readBufferFactory = readBufferFactory;
        return this;
    }
}
