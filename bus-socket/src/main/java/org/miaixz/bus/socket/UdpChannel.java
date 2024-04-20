/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             *
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
package org.miaixz.bus.socket;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.buffers.BufferPage;
import org.miaixz.bus.socket.buffers.VirtualBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * 封装UDP底层真实渠道对象,并提供通信及会话管理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UdpChannel {

    public final ServerConfig config;
    public final BufferPage bufferPage;
    public final Semaphore writeSemaphore = new Semaphore(1);
    /**
     * 真实的UDP通道
     */
    public final DatagramChannel channel;
    /**
     * 待输出消息
     */
    private ConcurrentLinkedQueue<ResponseUnit> responseTasks;
    private WorkerRegister workerRegister;
    private SelectionKey selectionKey;
    //发送失败的
    private ResponseUnit failResponseUnit;

    UdpChannel(final DatagramChannel channel, ServerConfig config, BufferPage bufferPage) {
        this.channel = channel;
        this.bufferPage = bufferPage;
        this.config = config;
    }

    UdpChannel(final DatagramChannel channel, WorkerRegister workerRegister, ServerConfig config, BufferPage bufferPage) {
        this(channel, config, bufferPage);
        responseTasks = new ConcurrentLinkedQueue<>();
        this.workerRegister = workerRegister;
        workerRegister.addRegister(selector -> {
            try {
                UdpChannel.this.selectionKey = channel.register(selector, SelectionKey.OP_READ, UdpChannel.this);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        });
    }

    void write(VirtualBuffer virtualBuffer, UdpAioSession session) {
        if (writeSemaphore.tryAcquire() && responseTasks.isEmpty() && send(virtualBuffer.buffer(), session) > 0) {
            virtualBuffer.clean();
            writeSemaphore.release();
            session.writeBuffer().flush();
            return;
        }
        responseTasks.offer(new ResponseUnit(session, virtualBuffer));
        if (selectionKey == null) {
            workerRegister.addRegister(selector -> selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE));
        } else {
            if ((selectionKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
                selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
            }
        }
    }

    void doWrite() {
        while (true) {
            ResponseUnit responseUnit;
            if (failResponseUnit == null) {
                responseUnit = responseTasks.poll();
            } else {
                responseUnit = failResponseUnit;
                failResponseUnit = null;
            }
            if (responseUnit == null) {
                writeSemaphore.release();
                if (responseTasks.isEmpty()) {
                    selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                    if (!responseTasks.isEmpty()) {
                        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                    }
                }
                return;
            }
            if (send(responseUnit.response.buffer(), responseUnit.session) > 0) {
                responseUnit.response.clean();
                responseUnit.session.writeBuffer().flush();
            } else {
                failResponseUnit = responseUnit;
                Logger.warn("send fail,will retry...");
                break;
            }
        }
    }

    private int send(ByteBuffer byteBuffer, UdpAioSession session) {
        if (config.getMonitor() != null) {
            config.getMonitor().beforeWrite(session);
        }
        int size = 0;
        try {
            size = channel.send(byteBuffer, session.getRemoteAddress());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (config.getMonitor() != null) {
            config.getMonitor().afterWrite(session, size);
        }
        return size;
    }

    /**
     * 建立与远程服务的连接会话,通过AioSession可进行数据传输
     */
    public AioSession connect(SocketAddress remote) {
        return new UdpAioSession(this, remote, bufferPage);
    }

    public AioSession connect(String host, int port) {
        return connect(new InetSocketAddress(host, port));
    }

    /**
     * 关闭当前连接
     */
    public void close() {
        Logger.info("close channel...");
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
        //内存回收
        ResponseUnit task;
        while ((task = responseTasks.poll()) != null) {
            task.response.clean();
        }
        if (failResponseUnit != null) {
            failResponseUnit.response.clean();
        }
    }

    BufferPage getBufferPage() {
        return bufferPage;
    }

    DatagramChannel getChannel() {
        return channel;
    }

    static final class ResponseUnit {
        /**
         * 待输出数据的接受地址
         */
        private final UdpAioSession session;
        /**
         * 待输出数据
         */
        private final VirtualBuffer response;

        public ResponseUnit(UdpAioSession session, VirtualBuffer response) {
            this.session = session;
            this.response = response;
        }

    }

}
