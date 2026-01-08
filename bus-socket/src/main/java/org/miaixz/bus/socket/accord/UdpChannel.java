/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.             ~
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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.Context;
import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.Worker;
import org.miaixz.bus.socket.buffer.BufferPage;
import org.miaixz.bus.socket.buffer.VirtualBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Wraps the underlying UDP channel and provides communication and session management.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UdpChannel {

    /**
     * The service context, containing configuration settings.
     */
    public final Context context;
    /**
     * The buffer page for write operations.
     */
    private final BufferPage writeBufferPage;
    /**
     * The underlying UDP channel.
     */
    private final DatagramChannel channel;
    /**
     * A queue for pending outgoing messages.
     */
    private ConcurrentLinkedQueue<ResponseUnit> responseTasks;
    /**
     * The worker responsible for handling I/O operations.
     */
    private Worker worker;
    /**
     * The selection key for this channel's registration with a selector.
     */
    private SelectionKey selectionKey;
    /**
     * Holds a response unit that failed to send and needs to be retried.
     */
    private ResponseUnit failResponseUnit;

    /**
     * Constructs a UdpChannel.
     *
     * @param channel         the underlying datagram channel
     * @param context         the service context
     * @param writeBufferPage the buffer page for writing
     */
    UdpChannel(final DatagramChannel channel, Context context, BufferPage writeBufferPage) {
        this.channel = channel;
        this.writeBufferPage = writeBufferPage;
        this.context = context;
    }

    /**
     * Constructs a UdpChannel with a worker.
     *
     * @param channel         the underlying datagram channel
     * @param worker          the I/O worker
     * @param context         the service context
     * @param writeBufferPage the buffer page for writing
     */
    UdpChannel(final DatagramChannel channel, Worker worker, Context context, BufferPage writeBufferPage) {
        this(channel, context, writeBufferPage);
        responseTasks = new ConcurrentLinkedQueue<>();
        this.worker = worker;
        worker.addRegister(selector -> {
            try {
                UdpChannel.this.selectionKey = channel.register(selector, SelectionKey.OP_READ, UdpChannel.this);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Writes data to the specified session. If the send fails, the data is queued for a later attempt.
     *
     * @param virtualBuffer the buffer containing the data to write
     * @param session       the session to write to
     */
    void write(VirtualBuffer virtualBuffer, UdpSession session) {
        if (send(virtualBuffer, session)) {
            return;
        }
        // The write semaphore is already held, so each session will have at most one buffer in the responseTasks queue.
        responseTasks.offer(new ResponseUnit(session, virtualBuffer));
        synchronized (this) {
            if (selectionKey == null) {
                worker.addRegister(
                        selector -> selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE));
            } else {
                if ((selectionKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                }
            }
        }
    }

    /**
     * Processes the queue of pending write operations.
     */
    public void doWrite() {
        while (true) {
            ResponseUnit responseUnit;
            if (failResponseUnit == null) {
                responseUnit = responseTasks.poll();
            } else {
                responseUnit = failResponseUnit;
                failResponseUnit = null;
            }
            if (responseUnit == null) {
                if (responseTasks.isEmpty()) {
                    selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                    if (!responseTasks.isEmpty()) {
                        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                    }
                }
                return;
            }
            if (!send(responseUnit.response, responseUnit.session)) {
                failResponseUnit = responseUnit;
                Logger.warn("Send failed, will retry...");
                break;
            }
        }
    }

    /**
     * Sends data to the specified session.
     *
     * @param virtualBuffer the buffer containing the data to send
     * @param session       the session to send to
     * @return {@code true} if the send was successful, {@code false} otherwise
     */
    private boolean send(VirtualBuffer virtualBuffer, UdpSession session) {
        if (context.getMonitor() != null) {
            context.getMonitor().beforeWrite(session);
        }
        int size;
        try {
            size = channel.send(virtualBuffer.buffer(), session.getRemoteAddress());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (size == 0) {
            return false;
        }
        if (context.getMonitor() != null) {
            context.getMonitor().afterWrite(session, size);
        }
        virtualBuffer.clean();
        session.writeBuffer().finishWrite();
        session.writeBuffer().flush();
        return true;
    }

    /**
     * Establishes a session with a remote service for data transmission.
     *
     * @param remote the remote address to connect to
     * @return a new {@link Session}
     */
    public Session connect(SocketAddress remote) {
        return new UdpSession(this, remote, writeBufferPage);
    }

    /**
     * Establishes a session with a remote service for data transmission.
     *
     * @param host the remote host
     * @param port the remote port
     * @return a new {@link Session}
     */
    public Session connect(String host, int port) {
        return connect(new InetSocketAddress(host, port));
    }

    /**
     * Closes the current connection.
     */
    public void close() {
        Logger.info("Closing channel...");
        if (selectionKey != null) {
            Selector selector = selectionKey.selector();
            selectionKey.cancel();
            selector.wakeup();
            selectionKey = null;
        }
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (IOException e) {
            Logger.error(Normal.EMPTY, e);
        }
        // Clean up resources
        ResponseUnit task;
        while ((task = responseTasks.poll()) != null) {
            task.response.clean();
        }
        if (failResponseUnit != null) {
            failResponseUnit.response.clean();
        }
    }

    /**
     * Gets the underlying datagram channel.
     *
     * @return the {@link DatagramChannel}
     */
    public DatagramChannel getChannel() {
        return channel;
    }

    /**
     * A data holder for a pending response, containing the session and the data to be sent.
     */
    static final class ResponseUnit {

        /**
         * The session to which the data should be sent.
         */
        private final UdpSession session;
        /**
         * The data to be sent.
         */
        private final VirtualBuffer response;

        /**
         * Constructs a ResponseUnit.
         *
         * @param session  the session (destination)
         * @param response the data to be sent
         */
        public ResponseUnit(UdpSession session, VirtualBuffer response) {
            this.session = session;
            this.response = response;
        }

    }

}
