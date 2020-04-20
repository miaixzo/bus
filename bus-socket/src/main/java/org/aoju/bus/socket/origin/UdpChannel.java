/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
 ********************************************************************************/
package org.aoju.bus.socket.origin;

import org.aoju.bus.core.io.EventFactory;
import org.aoju.bus.core.io.PageBuffer;
import org.aoju.bus.core.io.RingBuffer;
import org.aoju.bus.core.io.VirtualBuffer;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
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

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public final class UdpChannel<Request> {

    private PageBuffer pageBuffer;
    private int writeQueueCapacity;
    /**
     * 真实的UDP通道
     */
    private DatagramChannel channel;

    private SelectionKey selectionKey;

    /**
     * 与当前UDP通道对接的会话
     */
    private ConcurrentHashMap<String, UdpAioSession<Request>> udpAioSessionConcurrentHashMap = new ConcurrentHashMap<>();

    /**
     * 待输出消息
     */
    private RingBuffer<UdpWriteEvent> writeRingBuffer;
    /**
     * 已完成解码待业务处理的消息集合
     */
    private Object lock = new Object();


    private int writeBacklog = 2048;

    UdpChannel(final DatagramChannel channel, SelectionKey selectionKey, int writeQueueCapacity, PageBuffer pageBuffer) {
        this.channel = channel;
        writeRingBuffer = new RingBuffer<>(writeBacklog, new EventFactory<UdpWriteEvent>() {
            @Override
            public UdpWriteEvent newInstance() {
                return new UdpWriteEvent();
            }

            @Override
            public void restEntity(UdpWriteEvent entity) {
                entity.setResponse(null);
                entity.setRemote(null);
            }
        });
        this.selectionKey = selectionKey;
        this.writeQueueCapacity = writeQueueCapacity;
        this.pageBuffer = pageBuffer;
    }

    private void write(VirtualBuffer virtualBuffer, SocketAddress remote) throws IOException, InterruptedException {
        int index = writeRingBuffer == null ? -1 : writeRingBuffer.tryNextWriteIndex();
        //缓存区已满,同步输出确保线程不发送死锁
        if (index < 0) {
            try {
                channel.send(virtualBuffer.buffer(), remote);
            } finally {
                virtualBuffer.clean();
            }
            return;
        }
        UdpWriteEvent event = writeRingBuffer.get(index);
        event.setResponse(virtualBuffer);
        event.setRemote(remote);
        writeRingBuffer.publishWriteIndex(index);

        if ((selectionKey.interestOps() & SelectionKey.OP_WRITE) == 0) {
            synchronized (lock) {
                selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                selectionKey.selector().wakeup();
            }
        }
    }

    void doWrite() throws IOException {
        int writeSize = -1;
        do {
            int index = writeRingBuffer.tryNextReadIndex();
            //无可写数据,去除写关注
            if (index < 0) {
                synchronized (lock) {
                    selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                    selectionKey.selector().wakeup();
                }
                index = writeRingBuffer.tryNextReadIndex();
                if (index < 0) {
                    return;
                } else {
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                }
            }

            UdpWriteEvent event = writeRingBuffer.get(index);
            VirtualBuffer response = event.getResponse();
            SocketAddress remote = event.getRemote();
            writeRingBuffer.publishReadIndex(index);

            ByteBuffer buffer = response.buffer();
            writeSize = channel.send(buffer, remote);
            response.clean();
            if (buffer.hasRemaining()) {
                Logger.error("buffer has remaining!");
            }
        } while (writeSize > 0);
    }

    /**
     * 建立与远程服务的连接会话,通过AioSession可进行数据传输
     *
     * @param remote 远程信息
     * @return the object
     */
    public AioSession<Request> connect(SocketAddress remote) {
        return createAndCacheSession(remote);
    }

    /**
     * 创建并缓存与指定地址的会话信息
     *
     * @param remote 远程信息
     * @return the object
     */
    UdpAioSession<Request> createAndCacheSession(final SocketAddress remote) {
        if (!(remote instanceof InetSocketAddress)) {
            throw new UnsupportedOperationException();

        }
        InetSocketAddress address = (InetSocketAddress) remote;
        String key = address.getHostName() + Symbol.COLON + address.getPort();
        UdpAioSession<Request> session = udpAioSessionConcurrentHashMap.get(key);
        if (session != null) {
            return session;
        }
        synchronized (this) {
            if (session != null) {
                return session;
            }
            Function<WriteBuffer, Void> function = writeBuffer -> {
                VirtualBuffer virtualBuffer = writeBuffer.poll();
                if (virtualBuffer == null) {
                    return null;
                }
                try {
                    write(virtualBuffer, remote);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            };
            WriteBuffer writeBuffer = new WriteBuffer(pageBuffer, function, writeQueueCapacity);
            session = new UdpAioSession<>(this, remote, writeBuffer);
            udpAioSessionConcurrentHashMap.put(key, session);
        }
        return session;
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
        for (Map.Entry<String, UdpAioSession<Request>> entry : udpAioSessionConcurrentHashMap.entrySet()) {
            entry.getValue().close();
        }
        try {
            if (channel != null) {
                channel.close();
                channel = null;
            }
        } catch (IOException e) {
            Logger.error(Normal.EMPTY, e);
        }
    }

    DatagramChannel getChannel() {
        return channel;
    }

}
