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
 * 封装UDP底层真实渠道对象,并提供通信及会话管理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class UdpChannel {

    /**
     * 服务上下文
     */
    public final Context context;
    /**
     * 缓冲页
     */
    private final BufferPage writeBufferPage;
    /**
     * 真实的UDP通道
     */
    private final DatagramChannel channel;
    /**
     * 待输出消息
     */
    private ConcurrentLinkedQueue<ResponseUnit> responseTasks;
    /**
     * 工作者
     */
    private Worker worker;
    /**
     * 注册者key
     */
    private SelectionKey selectionKey;
    /**
     * 发送失败的
     */
    private ResponseUnit failResponseUnit;

    UdpChannel(final DatagramChannel channel, Context context, BufferPage writeBufferPage) {
        this.channel = channel;
        this.writeBufferPage = writeBufferPage;
        this.context = context;
    }

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

    void write(VirtualBuffer virtualBuffer, UdpSession session) {
        if (send(virtualBuffer, session)) {
            return;
        }
        // 已经持有write信号量，每个session在responseTasks中只会存一个带输出buffer
        responseTasks.offer(new ResponseUnit(session, virtualBuffer));
        synchronized (this) {
            if (selectionKey == null) {
                worker.addRegister(selector -> selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE));
            } else {
                if ((selectionKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                }
            }
        }
    }

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
                Logger.warn("send fail,will retry...");
                break;
            }
        }
    }

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
     * 建立与远程服务的连接会话,通过Session可进行数据传输
     */
    public Session connect(SocketAddress remote) {
        return new UdpSession(this, remote, writeBufferPage);
    }

    public Session connect(String host, int port) {
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
        // 内存回收
        ResponseUnit task;
        while ((task = responseTasks.poll()) != null) {
            task.response.clean();
        }
        if (failResponseUnit != null) {
            failResponseUnit.response.clean();
        }
    }

    public DatagramChannel getChannel() {
        return channel;
    }

    static final class ResponseUnit {
        /**
         * 待输出数据的接受地址
         */
        private final UdpSession session;
        /**
         * 待输出数据
         */
        private final VirtualBuffer response;

        public ResponseUnit(UdpSession session, VirtualBuffer response) {
            this.session = session;
            this.response = response;
        }

    }

}
