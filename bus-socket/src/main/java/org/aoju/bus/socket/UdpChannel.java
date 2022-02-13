/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org sandao and other contributors.               *
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

import org.aoju.bus.core.io.PageBuffer;
import org.aoju.bus.core.io.VirtualBuffer;
import org.aoju.bus.core.io.WriteBuffer;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.logger.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * 封装UDP底层真实渠道对象,并提供通信及会话管理
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public class UdpChannel<Request> {

    private final PageBuffer pageBuffer;
    /**
     * 与当前UDP通道对接的会话
     */
    private final ConcurrentHashMap<SocketAddress, UdpAioSession> sessionMap = new ConcurrentHashMap<>();
    /**
     * 待输出消息
     */
    private final ConcurrentLinkedQueue<ResponseUnit> responseTasks;
    private final Semaphore writeSemaphore = new Semaphore(1);
    private final UdpBootstrap.Worker worker;
    ServerConfig config;
    /**
     * 真实的UDP通道
     */
    private DatagramChannel channel;
    private SelectionKey selectionKey;
    private ResponseUnit failResponseUnit;

    UdpChannel(final DatagramChannel channel, UdpBootstrap.Worker worker, ServerConfig config, PageBuffer pageBuffer) {
        this.channel = channel;
        responseTasks = new ConcurrentLinkedQueue<>();
        this.worker = worker;
        this.pageBuffer = pageBuffer;
        this.config = config;
    }

    private void write(VirtualBuffer virtualBuffer, SocketAddress remote) throws IOException {
        if (writeSemaphore.tryAcquire() && responseTasks.isEmpty() && send(virtualBuffer.buffer(), remote) > 0) {
            virtualBuffer.clean();
            writeSemaphore.release();
            return;
        }
        responseTasks.offer(new ResponseUnit(remote, virtualBuffer));
        if (null == selectionKey) {
            worker.addRegister(selector -> selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE));
        } else {
            if ((selectionKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
                selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
            }
        }
    }

    void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    void doWrite() throws IOException {
        while (true) {
            ResponseUnit responseUnit;
            if (null == failResponseUnit) {
                responseUnit = responseTasks.poll();
                Logger.info("poll from writeBuffer");
            } else {
                responseUnit = failResponseUnit;
                failResponseUnit = null;
            }
            if (null == responseUnit) {
                writeSemaphore.release();
                if (responseTasks.isEmpty()) {
                    selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                    if (!responseTasks.isEmpty()) {
                        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                    }
                }
                return;
            }
            if (send(responseUnit.response.buffer(), responseUnit.remote) > 0) {
                responseUnit.response.clean();
            } else {
                failResponseUnit = responseUnit;
                break;
            }
        }
    }

    private int send(ByteBuffer byteBuffer, SocketAddress remote) throws IOException {
        AioSession aioSession = sessionMap.get(remote);
        if (null != config.getMonitor()) {
            config.getMonitor().beforeWrite(aioSession);
        }
        int size = channel.send(byteBuffer, remote);
        if (null != config.getMonitor()) {
            config.getMonitor().afterWrite(aioSession, size);
        }
        return size;
    }

    /**
     * 建立与远程服务的连接会话,通过AioSession可进行数据传输
     *
     * @param remote 地址
     * @return 会话信息
     */
    public AioSession connect(SocketAddress remote) {
        return createAndCacheSession(remote);
    }

    /**
     * 建立与远程服务的连接会话,通过AioSession可进行数据传输
     *
     * @param host 地址
     * @param port 端口
     * @return 会话信息
     */
    public AioSession connect(String host, int port) {
        return connect(new InetSocketAddress(host, port));
    }

    /**
     * 创建并缓存与指定地址的会话信息
     */
    UdpAioSession createAndCacheSession(final SocketAddress remote) {
        return sessionMap.computeIfAbsent(remote, s -> {
            Consumer<WriteBuffer> consumer = writeBuffer -> {
                VirtualBuffer virtualBuffer = writeBuffer.poll();
                if (null == virtualBuffer) {
                    return;
                }
                try {
                    write(virtualBuffer, remote);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            WriteBuffer writeBuffer = new WriteBuffer(pageBuffer, consumer, config.getWriteBufferSize(), 1);
            return new UdpAioSession(UdpChannel.this, remote, writeBuffer);
        });
    }

    void removeSession(final SocketAddress remote) {
        UdpAioSession udpAioSession = sessionMap.remove(remote);
        Logger.info("remove session:{}", udpAioSession);
    }

    /**
     * 关闭当前连接
     */
    public void close() {
        if (null != selectionKey) {
            Selector selector = selectionKey.selector();
            selectionKey.cancel();
            selector.wakeup();
            selectionKey = null;
        }
        for (UdpAioSession session : sessionMap.values()) {
            session.close();
        }
        try {
            if (null != channel) {
                channel.close();
                channel = null;
            }
        } catch (IOException e) {
            Logger.error(Normal.EMPTY, e);
        }
        // 内存回收
        ResponseUnit task;
        while (null != (task = responseTasks.poll())) {
            task.response.clean();
        }
        if (null != failResponseUnit) {
            failResponseUnit.response.clean();
        }
    }

    DatagramChannel getChannel() {
        return channel;
    }

    static final class ResponseUnit {
        /**
         * 待输出数据的接受地址
         */
        private final SocketAddress remote;
        /**
         * 待输出数据
         */
        private final VirtualBuffer response;

        public ResponseUnit(SocketAddress remote, VirtualBuffer response) {
            this.remote = remote;
            this.response = response;
        }

    }

}
