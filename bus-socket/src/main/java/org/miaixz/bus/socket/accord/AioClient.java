/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
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
import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.buffer.BufferPagePool;
import org.miaixz.bus.socket.metric.channel.AsynchronousChannelProvider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.*;

/**
 * AIO implementation of a client service.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AioClient {

    /**
     * Executor for monitoring connection timeouts.
     */
    private static final ScheduledExecutorService CONNECT_TIMEOUT_EXECUTOR = Executors
            .newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "connection-timeout-monitor");
                thread.setDaemon(true);
                return thread;
            });
    /**
     * Client service configuration. All setXX() methods of AioClient are used to set configuration items.
     */
    private final Context context = new Context();
    /**
     * The session object for the network connection.
     *
     * @see TcpSession
     */
    private TcpSession session;
    /**
     * IO event handling thread group. As a client, this AsynchronousChannelGroup only needs a thread pool of size 2 to
     * meet the communication read and write requirements.
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;
    /**
     * Binds the local address.
     */
    private SocketAddress localAddress;
    /**
     * Connection timeout in milliseconds.
     */
    private int connectTimeout;

    /**
     * Write buffer pool.
     */
    private BufferPagePool writeBufferPool = null;
    /**
     * Read buffer pool.
     */
    private BufferPagePool readBufferPool = null;
    /**
     * Whether to enable low memory mode.
     */
    private boolean lowMemory = true;

    /**
     * This constructor sets the necessary parameters to start the AioClient, providing an out-of-the-box experience.
     *
     * @param <T>     the type of message handled by this client
     * @param host    the remote server address
     * @param port    the remote server port
     * @param message the protocol message codec
     * @param handler the message handler
     */
    public <T> AioClient(String host, int port, Message<T> message, Handler<T> handler) {
        context.setHost(host);
        context.setPort(port);
        context.setProtocol(message);
        context.setProcessor(handler);
    }

    /**
     * Starts the client asynchronously.
     *
     * @param <A>        the type of the attachment
     * @param attachment the object to pass to the completion handler
     * @param handler    the asynchronous completion handler
     * @throws IOException if an I/O error occurs
     */
    public <A> void start(A attachment, CompletionHandler<Session, ? super A> handler) throws IOException {
        this.asynchronousChannelGroup = new AsynchronousChannelProvider(lowMemory)
                .openAsynchronousChannelGroup(2, Thread::new);
        start(asynchronousChannelGroup, attachment, handler);
    }

    /**
     * Starts the client asynchronously with a shared asynchronous channel group.
     *
     * @param <A>                      the type of the attachment
     * @param asynchronousChannelGroup the shared asynchronous channel group for communication threads
     * @param attachment               the object to pass to the completion handler
     * @param handler                  the asynchronous completion handler
     * @throws IOException if an I/O error occurs
     */
    public <A> void start(
            AsynchronousChannelGroup asynchronousChannelGroup,
            A attachment,
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
        socketChannel.connect(
                new InetSocketAddress(context.getHost(), context.getPort()),
                socketChannel,
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
                            // On successful connection, create a Session object
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
     * Starts the client. This method blocks until a connection is established with the server or an exception occurs.
     * This start method supports an external AsynchronousChannelGroup, allowing multiple clients to share a thread
     * pool, which improves resource utilization.
     *
     * @param asynchronousChannelGroup the IO event handling thread group
     * @return the session object after the connection is established
     * @throws IOException if an I/O error occurs
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

    /**
     * Gets the current TCP session.
     *
     * @return the current {@link TcpSession}
     */
    public TcpSession getSession() {
        return session;
    }

    /**
     * Starts the client. This method creates an {@code asynchronousChannelGroup} with two threads and starts the
     * service by calling {@link AioClient#start(AsynchronousChannelGroup)}.
     *
     * @return the session object after the connection is established
     * @throws IOException if an I/O error occurs
     * @see AioClient#start(AsynchronousChannelGroup)
     */
    public Session start() throws IOException {
        this.asynchronousChannelGroup = new AsynchronousChannelProvider(lowMemory)
                .openAsynchronousChannelGroup(2, Thread::new);
        return start(asynchronousChannelGroup);
    }

    /**
     * Stops the client service. Calling this method will trigger the session's close method. If the client was started
     * using the {@link AioClient#start()} method, it will also trigger the shutdown of the asynchronousChannelGroup.
     */
    public void shutdown() {
        shutdown0(false);
    }

    /**
     * Shuts down the client immediately.
     */
    public void shutdownNow() {
        shutdown0(true);
    }

    /**
     * Stops the client.
     *
     * @param flag whether to stop immediately
     */
    private synchronized void shutdown0(boolean flag) {
        if (session != null) {
            session.close(flag);
            session = null;
        }
        // Only shutdown the ChannelGroup created internally by the Client
        if (asynchronousChannelGroup != null) {
            asynchronousChannelGroup.shutdown();
            asynchronousChannelGroup = null;
        }
    }

    /**
     * Sets the read buffer size.
     *
     * @param size the size in bytes
     * @return the current AioClient instance
     */
    public AioClient setReadBufferSize(int size) {
        this.context.setReadBufferSize(size);
        return this;
    }

    /**
     * Sets the TCP socket options.
     * <p>
     * The valid options for an AIO client are: 1. StandardSocketOptions.SO_SNDBUF 2. StandardSocketOptions.SO_RCVBUF 3.
     * StandardSocketOptions.SO_KEEPALIVE 4. StandardSocketOptions.SO_REUSEADDR 5. StandardSocketOptions.TCP_NODELAY
     * </p>
     *
     * @param <V>          the type of the socket option value
     * @param socketOption the socket option
     * @param value        the value of the socket option
     * @return the current client instance
     */
    public <V> AioClient setOption(SocketOption<V> socketOption, V value) {
        context.setOption(socketOption, value);
        return this;
    }

    /**
     * Binds the client to a local address and port for connecting to the remote service.
     *
     * @param local the local address to bind to; if null, the system will pick one
     * @param port  the local port to bind to; if 0, the system will pick one
     * @return the current client instance
     */
    public AioClient bindLocal(String local, int port) {
        localAddress = local == null ? new InetSocketAddress(port) : new InetSocketAddress(local, port);
        return this;
    }

    /**
     * Sets the buffer pool. The buffer pool set by this method will not be released when the AioClient is shut down.
     * This method is suitable for scenarios where multiple AioServers and AioClients share a buffer pool. <b>Using a
     * buffer pool provides better performance.</b>
     *
     * @param bufferPool the buffer pool to use
     * @return the current client instance
     */
    public AioClient setBufferPagePool(BufferPagePool bufferPool) {
        return setBufferPagePool(bufferPool, bufferPool);
    }

    /**
     * Sets the read and write buffer pools.
     *
     * @param readBufferPool  the buffer pool for read operations
     * @param writeBufferPool the buffer pool for write operations
     * @return the current client instance
     */
    public AioClient setBufferPagePool(BufferPagePool readBufferPool, BufferPagePool writeBufferPool) {
        this.writeBufferPool = writeBufferPool;
        this.readBufferPool = readBufferPool;
        return this;
    }

    /**
     * Sets the output buffer capacity.
     *
     * @param bufferSize     the size of a single buffer block
     * @param bufferCapacity the maximum number of buffer blocks
     * @return the current client instance
     */
    public AioClient setWriteBuffer(int bufferSize, int bufferCapacity) {
        context.setWriteBufferSize(bufferSize);
        context.setWriteBufferCapacity(bufferCapacity);
        return this;
    }

    /**
     * Sets the client connection timeout in milliseconds.
     *
     * @param timeout the timeout in milliseconds
     * @return the current client instance
     */
    public AioClient connectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    /**
     * Disables low memory mode.
     *
     * @return the current client instance
     */
    public AioClient disableLowMemory() {
        this.lowMemory = false;
        return this;
    }

}
