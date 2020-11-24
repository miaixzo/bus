/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
import org.aoju.bus.logger.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

/**
 * 封装UDP底层真实渠道对象,并提供通信及会话管理
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
public final class UdpChannel<Request> {

    private final PageBuffer bufferPage;
    /**
     * 与当前UDP通道对接的会话
     */
    private final ConcurrentHashMap<String, UdpAioSession> udpAioSessionConcurrentHashMap = new ConcurrentHashMap<>();
    /**
     * 待输出消息
     */
    private final ConcurrentLinkedQueue<ResponseTask> responseTasks;
    private final Semaphore writeSemaphore = new Semaphore(1);
    IoServerConfig<Request> config;
    /**
     * 真实的UDP通道
     */
    private DatagramChannel channel;
    private SelectionKey selectionKey;
    private ResponseTask failWriteEvent;

    UdpChannel(final DatagramChannel channel, SelectionKey selectionKey, IoServerConfig<Request> config, PageBuffer bufferPage) {
        this.channel = channel;
        responseTasks = new ConcurrentLinkedQueue<>();
        this.selectionKey = selectionKey;
        this.bufferPage = bufferPage;
        this.config = config;
    }

    /**
     * @param virtualBuffer
     * @param remote
     * @throws IOException
     * @throws InterruptedException
     */
    private void write(VirtualBuffer virtualBuffer, SocketAddress remote) throws IOException {
        if (writeSemaphore.tryAcquire() && responseTasks.isEmpty() && send(virtualBuffer.buffer(), remote) > 0) {
            virtualBuffer.clean();
            writeSemaphore.release();
            return;
        }
        responseTasks.offer(new ResponseTask(remote, virtualBuffer));
        if ((selectionKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
        }
    }

    void flush() throws IOException {
        while (true) {
            ResponseTask responseTask;
            if (failWriteEvent == null) {
                responseTask = responseTasks.poll();
                Logger.info("poll from writeBuffer");
            } else {
                responseTask = failWriteEvent;
                failWriteEvent = null;
            }
            if (responseTask == null) {
                writeSemaphore.release();
                if (responseTasks.isEmpty()) {
                    selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                    if (!responseTasks.isEmpty()) {
                        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                    }
                }
                return;
            }
            if (send(responseTask.response.buffer(), responseTask.remote) > 0) {
                responseTask.response.clean();
            } else {
                failWriteEvent = responseTask;
                break;
            }
        }
    }

    private int send(ByteBuffer byteBuffer, SocketAddress remote) throws IOException {
        AioSession aioSession = udpAioSessionConcurrentHashMap.get(getSessionKey(remote));
        if (config.getMonitor() != null) {
            config.getMonitor().beforeWrite(aioSession);
        }
        int size = channel.send(byteBuffer, remote);
        if (config.getMonitor() != null) {
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
     * 创建并缓存与指定地址的会话信息
     *
     * @param remote 地址
     * @return 会话信息
     */
    UdpAioSession createAndCacheSession(final SocketAddress remote) {
        String key = getSessionKey(remote);
        UdpAioSession session = udpAioSessionConcurrentHashMap.computeIfAbsent(key, s -> {
            Function<WriteBuffer, Void> function = writeBuffer -> {
                VirtualBuffer virtualBuffer = writeBuffer.poll();
                if (virtualBuffer == null) {
                    return null;
                }
                try {
                    write(virtualBuffer, remote);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            };
            WriteBuffer writeBuffer = new WriteBuffer(bufferPage, function, config.getWriteBufferSize(), 1);
            return new UdpAioSession(UdpChannel.this, remote, writeBuffer);
        });
        return session;
    }

    private String getSessionKey(final SocketAddress remote) {
        if (!(remote instanceof InetSocketAddress)) {
            throw new UnsupportedOperationException();

        }
        InetSocketAddress address = (InetSocketAddress) remote;
        return address.getHostName() + ":" + address.getPort();
    }

    void removeSession(final SocketAddress remote) {
        String key = getSessionKey(remote);
        UdpAioSession udpAioSession = udpAioSessionConcurrentHashMap.remove(key);
        Logger.info("remove session:{}", udpAioSession);
    }

    /**
     * 关闭当前连接
     */
    public void close() {
        if (selectionKey != null) {
            Selector selector = selectionKey.selector();
            selectionKey.cancel();
            selector.wakeup();
            selectionKey = null;
        }
        for (Map.Entry<String, UdpAioSession> entry : udpAioSessionConcurrentHashMap.entrySet()) {
            entry.getValue().close();
        }
        try {
            if (channel != null) {
                channel.close();
                channel = null;
            }
        } catch (IOException e) {
            Logger.error("", e);
        }
        // 内存回收
        ResponseTask task;
        while ((task = responseTasks.poll()) != null) {
            task.response.clean();
        }
    }

    DatagramChannel getChannel() {
        return channel;
    }

    static final class ResponseTask {
        /**
         * 待输出数据的接受地址
         */
        private final SocketAddress remote;
        /**
         * 待输出数据
         */
        private final VirtualBuffer response;

        public ResponseTask(SocketAddress remote, VirtualBuffer response) {
            this.remote = remote;
            this.response = response;
        }
    }

}
