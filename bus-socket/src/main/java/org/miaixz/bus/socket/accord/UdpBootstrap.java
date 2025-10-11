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
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ a ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.socket.accord;

import org.miaixz.bus.socket.Context;
import org.miaixz.bus.socket.Handler;
import org.miaixz.bus.socket.Message;
import org.miaixz.bus.socket.Worker;
import org.miaixz.bus.socket.buffer.BufferPagePool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * A bootstrap class for creating and configuring UDP services.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UdpBootstrap {

    /**
     * The service context, containing configuration settings.
     */
    private final Context context = new Context();
    /**
     * The worker responsible for handling I/O operations.
     */
    private Worker worker;
    /**
     * Flag indicating whether the worker was created internally by this bootstrap.
     */
    private boolean innerWorker = false;
    /**
     * The buffer pool for write operations.
     */
    private BufferPagePool writeBufferPool = null;
    /**
     * The buffer pool for read operations.
     */
    private BufferPagePool readBufferPool = null;

    /**
     * Constructs a UdpBootstrap with a specified message codec, handler, and worker.
     *
     * @param <Request> the type of the request message
     * @param message   the message codec for handling data packets
     * @param handler   the message handler for processing requests
     * @param worker    the worker to handle I/O operations
     */
    public <Request> UdpBootstrap(Message<Request> message, Handler<Request> handler, Worker worker) {
        this(message, handler);
        this.worker = worker;
    }

    /**
     * Constructs a UdpBootstrap with a specified message codec and handler.
     *
     * @param <Request> the type of the request message
     * @param message   the message codec for handling data packets
     * @param handler   the message handler for processing requests
     */
    public <Request> UdpBootstrap(Message<Request> message, Handler<Request> handler) {
        context.setProtocol(message);
        context.setProcessor(handler);
    }

    /**
     * Opens a UDP channel on a random port.
     *
     * @return the newly created {@link UdpChannel}
     * @throws IOException if an I/O error occurs
     */
    public UdpChannel open() throws IOException {
        return open(0);
    }

    /**
     * Opens a UDP channel on a specified port.
     *
     * @param port the port to bind to; if 0, a random port will be chosen
     * @return the newly created {@link UdpChannel}
     * @throws IOException if an I/O error occurs
     */
    public UdpChannel open(int port) throws IOException {
        return open(null, port);
    }

    /**
     * Opens a UDP channel on a specified host and port.
     *
     * @param host the host address to bind to
     * @param port the port to bind to; if 0, a random port will be chosen
     * @return the newly created {@link UdpChannel}
     * @throws IOException if an I/O error occurs
     */
    public UdpChannel open(String host, int port) throws IOException {
        // Initialize buffer pools if they are not set
        if (writeBufferPool == null) {
            this.writeBufferPool = BufferPagePool.DEFAULT_BUFFER_PAGE_POOL;
        }
        if (readBufferPool == null) {
            this.readBufferPool = BufferPagePool.DEFAULT_BUFFER_PAGE_POOL;
        }

        // Initialize the worker thread if it is not set
        if (worker == null) {
            innerWorker = true;
            worker = new Worker(readBufferPool.allocateBufferPage(), writeBufferPool, this.context.getThreadNum());
        }

        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        if (port > 0) {
            InetSocketAddress inetSocketAddress = host == null ? new InetSocketAddress(port)
                    : new InetSocketAddress(host, port);
            channel.socket().bind(inetSocketAddress);
        }
        return new UdpChannel(channel, worker, this.context, writeBufferPool.allocateBufferPage());
    }

    /**
     * Initializes the worker if it has not been initialized.
     */
    private synchronized void initWorker() {
        if (worker != null) {
            return;
        }
    }

    /**
     * Shuts down the bootstrap. If the worker was created internally, it will also be shut down.
     */
    public void shutdown() {
        if (innerWorker) {
            worker.shutdown();
        }
    }

    /**
     * Sets the read buffer size.
     *
     * @param size the size in bytes
     * @return this {@link UdpBootstrap} instance
     */
    public final UdpBootstrap setReadBufferSize(int size) {
        this.context.setReadBufferSize(size);
        return this;
    }

    /**
     * Sets the number of threads for the worker.
     *
     * @param num the number of threads
     * @return this {@link UdpBootstrap} instance
     */
    public final UdpBootstrap setThreadNum(int num) {
        this.context.setThreadNum(num);
        return this;
    }

    /**
     * Sets the buffer pool. The buffer pool set by this method will not be released when the server is shut down. This
     * is suitable for scenarios where multiple servers and clients share a buffer pool for better performance.
     *
     * @param bufferPool the buffer pool to use
     * @return this {@link UdpBootstrap} instance
     */
    public final UdpBootstrap setBufferPagePool(BufferPagePool bufferPool) {
        this.readBufferPool = bufferPool;
        this.writeBufferPool = bufferPool;
        return this;
    }

}
