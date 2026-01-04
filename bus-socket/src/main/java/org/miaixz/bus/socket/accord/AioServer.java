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
import org.miaixz.bus.socket.Status;
import org.miaixz.bus.socket.buffer.BufferPagePool;
import org.miaixz.bus.socket.buffer.VirtualBuffer;
import org.miaixz.bus.socket.metric.channel.AsynchronousChannelProvider;

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
 * AIO Server Implementation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AioServer {

    /**
     * Thread sequence number for naming.
     */
    private static long threadSeqNumber;
    /**
     * Server configuration context. All setXX() methods of AioServer are used to set configuration items in this
     * context.
     */
    private final Context context = new Context();
    /**
     * Asynchronous server socket channel.
     */
    private AsynchronousServerSocketChannel serverSocketChannel = null;
    /**
     * Asynchronous channel group for handling I/O events.
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;
    /**
     * Whether to enable low memory mode.
     */
    private boolean lowMemory = true;
    /**
     * Write buffer pool.
     */
    private BufferPagePool writeBufferPool = null;
    /**
     * Read buffer pool.
     */
    private BufferPagePool readBufferPool = null;

    /**
     * Sets the necessary parameters to start the AIO server.
     *
     * @param <T>     the type of message handled by this server
     * @param port    the port number to bind the server to
     * @param message the protocol message codec
     * @param handler the message handler
     */
    public <T> AioServer(int port, Message<T> message, Handler<T> handler) {
        context.setPort(port);
        context.setProtocol(message);
        context.setProcessor(handler);
        context.setThreadNum(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Sets the necessary parameters to start the AIO server.
     *
     * @param <T>     the type of message handled by this server
     * @param host    the host address to bind the server to
     * @param port    the port number to bind the server to
     * @param message the protocol message codec
     * @param handler the message handler
     */
    public <T> AioServer(String host, int port, Message<T> message, Handler<T> handler) {
        this(port, message, handler);
        context.setHost(host);
    }

    /**
     * Starts the AIO server.
     *
     * @throws IOException if an I/O error occurs
     */
    public void start() throws IOException {
        asynchronousChannelGroup = new AsynchronousChannelProvider(lowMemory).openAsynchronousChannelGroup(
                context.getThreadNum(),
                r -> new Thread(r, "Socket:Thread-" + (threadSeqNumber++)));
        start(asynchronousChannelGroup);
    }

    /**
     * Internal startup logic.
     *
     * @param asynchronousChannelGroup the asynchronous channel group to use
     * @throws IOException if an I/O error occurs
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
            // Set socket options
            if (context.getSocketOptions() != null) {
                for (Map.Entry<SocketOption<Object>, Object> entry : context.getSocketOptions().entrySet()) {
                    this.serverSocketChannel.setOption(entry.getKey(), entry.getValue());
                }
            }
            // Bind host
            if (context.getHost() != null) {
                serverSocketChannel
                        .bind(new InetSocketAddress(context.getHost(), context.getPort()), context.getBacklog());
            } else {
                serverSocketChannel.bind(new InetSocketAddress(context.getPort()), context.getBacklog());
            }

            startAcceptThread();
        } catch (IOException e) {
            shutdown();
            throw e;
        }
    }

    /**
     * Starts the thread to accept incoming connections.
     */
    private void startAcceptThread() {
        Supplier<VirtualBuffer> readBufferSupplier = () -> readBufferPool.allocateBufferPage()
                .allocate(context.getReadBufferSize());
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

            /**
             * {@inheritDoc}
             */
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

            /**
             * {@inheritDoc}
             */
            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
            }
        });
    }

    /**
     * Creates a session for each new connection.
     *
     * @param channel            the newly established channel
     * @param readBufferSupplier a supplier for read buffers
     */
    private void createSession(AsynchronousSocketChannel channel, Supplier<VirtualBuffer> readBufferSupplier) {
        // On successful connection, create an AioSession object
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
     * Stops the server.
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
     * Sets the read buffer size.
     *
     * @param size the size in bytes
     * @return this AioServer instance
     */
    public AioServer setReadBufferSize(int size) {
        this.context.setReadBufferSize(size);
        return this;
    }

    /**
     * Sets the TCP socket options.
     * <p>
     * The valid options for an AIO server are: 1. StandardSocketOptions.SO_RCVBUF 2. StandardSocketOptions.SO_REUSEADDR
     * </p>
     *
     * @param <V>          the type of the socket option value
     * @param socketOption the socket option
     * @param value        the value of the socket option
     * @return this AioServer instance
     */
    public <V> AioServer setOption(SocketOption<V> socketOption, V value) {
        context.setOption(socketOption, value);
        return this;
    }

    /**
     * Sets the number of server worker threads. The value must be greater than or equal to 2.
     *
     * @param threadNum the number of threads
     * @return this AioServer instance
     */
    public AioServer setThreadNum(int threadNum) {
        if (threadNum <= 1) {
            throw new InvalidParameterException("threadNum must >= 2");
        }
        context.setThreadNum(threadNum);
        return this;
    }

    /**
     * Sets the output buffer capacity.
     *
     * @param bufferSize     the size of a single buffer block
     * @param bufferCapacity the maximum number of buffer blocks
     * @return this AioServer instance
     */
    public AioServer setWriteBuffer(int bufferSize, int bufferCapacity) {
        this.context.setWriteBufferSize(bufferSize);
        this.context.setWriteBufferCapacity(bufferCapacity);
        return this;
    }

    /**
     * Sets the backlog size.
     *
     * @param backlog the backlog size
     * @return this AioServer instance
     */
    public final AioServer setBacklog(int backlog) {
        this.context.setBacklog(backlog);
        return this;
    }

    /**
     * Sets the read and write buffer pool. This method is suitable for scenarios where multiple AioServers and
     * AioClients share a buffer pool to achieve better performance.
     *
     * @param bufferPool the buffer pool to use
     * @return this AioServer instance
     */
    public AioServer setBufferPagePool(BufferPagePool bufferPool) {
        return setBufferPagePool(bufferPool, bufferPool);
    }

    /**
     * Sets the read and write buffer pools. This method is suitable for scenarios where multiple AioServers and
     * AioClients share buffer pools to achieve better performance.
     *
     * @param readBufferPool  the buffer pool for read operations
     * @param writeBufferPool the buffer pool for write operations
     * @return this AioServer instance
     */
    public AioServer setBufferPagePool(BufferPagePool readBufferPool, BufferPagePool writeBufferPool) {
        this.writeBufferPool = writeBufferPool;
        this.readBufferPool = readBufferPool;
        return this;
    }

    /**
     * Disables low memory mode.
     *
     * @return this AioServer instance
     */
    public AioServer disableLowMemory() {
        this.lowMemory = false;
        return this;
    }

}
