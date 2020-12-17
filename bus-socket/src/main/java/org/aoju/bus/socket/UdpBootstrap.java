/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org sandao and other contributors.               *
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
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.socket.process.MessageProcessor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * UDP服务启动类
 *
 * @param <R> 请求信息
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
public class UdpBootstrap<R> {

    private final static int MAX_EVENT = 512;

    private final static int MAX_READ_TIMES = 16;
    /**
     * 服务ID
     */
    private static int UID;
    private final SelectionKey NEED_TO_POLL = new UdpSelectionKey();
    private final SelectionKey EXECUTE_TASK_OR_SHUTDOWN = new UdpSelectionKey();
    /**
     * 缓存页
     */
    private final PageBuffer pageBuffer = new org.aoju.bus.core.io.ByteBuffer(1024, 1, -1, true).allocatePageBuffer();
    /**
     * 服务配置
     */
    private final ServerConfig<R> config = new ServerConfig<>();
    /**
     * 服务状态
     */
    private volatile Status status = Status.STATUS_INIT;
    /**
     * 多路复用器
     */
    private Selector selector;
    private UdpDispatcher<R>[] workerGroup;

    public UdpBootstrap(Protocol<R> protocol, MessageProcessor<R> messageProcessor) {
        config.setProtocol(protocol);
        config.setProcessor(messageProcessor);
    }

    /**
     * 开启一个UDP通道，端口号随机
     *
     * @return UDP通道
     * @throws IOException 异常
     */
    public UdpChannel<R> open() throws IOException {
        return open(0);
    }

    /**
     * 开启一个UDP通道
     *
     * @param port 指定绑定端口号,为0则随机指定
     * @return the object
     * @throws IOException 异常
     */
    public UdpChannel<R> open(int port) throws IOException {
        return open(null, port);
    }

    /**
     * 开启一个UDP通道
     *
     * @param host 绑定本机地址
     * @param port 指定绑定端口号,为0则随机指定
     * @return the object
     * @throws IOException 异常
     */
    public UdpChannel<R> open(String host, int port) throws IOException {
        if (host != null) {
            config.setHost(host);
        }
        config.setPort(port);

        if (selector == null) {
            synchronized (this) {
                if (selector == null) {
                    selector = Selector.open();
                }
            }
        }

        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        if (port > 0) {
            InetSocketAddress inetSocketAddress = host == null ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
            channel.socket().bind(inetSocketAddress);
            if (host == null) {
                config.setHost(inetSocketAddress.getHostString());
            }
        } else {
            config.setHost("");
        }

        if (status == Status.STATUS_RUNNING) {
            selector.wakeup();
        }
        SelectionKey selectionKey = channel.register(selector, SelectionKey.OP_READ);
        UdpChannel<R> udpChannel = new UdpChannel<>(channel, selectionKey, config, pageBuffer);
        selectionKey.attach(udpChannel);

        //启动线程服务
        if (status == Status.STATUS_INIT) {
            initThreadServer();
        }

        System.out.println("bus-socket server started on port " + config.getPort() + ",threadNum:" + config.getThreadNum());
        System.out.println("bus-socket server config is " + config);
        return udpChannel;
    }

    private synchronized void initThreadServer() {
        if (status != Status.STATUS_INIT) {
            return;
        }

        this.status = Status.STATUS_RUNNING;
        int uid = UdpBootstrap.UID++;

        //启动worker线程组
        workerGroup = new UdpDispatcher[config.getThreadNum()];
        for (int i = 0; i < config.getThreadNum(); i++) {
            workerGroup[i] = new UdpDispatcher<>(config.getProcessor());
            new Thread(workerGroup[i], "UDP-Worker-" + i).start();
        }
        // 启动Boss线程组
        new Thread(() -> {
            // 读缓冲区
            VirtualBuffer readBuffer = pageBuffer.allocate(config.getReadBufferSize());
            try {
                while (true) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    if (selectionKeys.isEmpty()) {
                        selector.select();
                    }
                    Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        UdpChannel<R> udpChannel = (UdpChannel<R>) key.attachment();
                        if (!key.isValid()) {
                            udpChannel.close();
                            continue;
                        }

                        if (key.isReadable()) {
                            doRead(readBuffer, udpChannel);
                        }
                        if (key.isWritable()) {
                            udpChannel.flush();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 读缓冲区内存回收
                readBuffer.clean();
            }
        }, "UDP-Boss-" + uid).start();
    }

    /**
     * 去读数据
     *
     * @param channel 频道
     * @throws IOException 异常
     */
    private void doRead(VirtualBuffer readBuffer, UdpChannel<R> channel) throws IOException {
        int count = MAX_READ_TIMES;
        while (count-- > 0) {
            // 接收数据
            ByteBuffer buffer = readBuffer.buffer();
            buffer.clear();
            // The datagram's source address,
            // or null if this channel is in non-blocking mode and no datagram was immediately available
            SocketAddress remote = channel.getChannel().receive(buffer);
            if (remote == null) {
                return;
            }
            buffer.flip();

            UdpAioSession aioSession = channel.createAndCacheSession(remote);
            NetMonitor netMonitor = config.getMonitor();
            if (netMonitor != null) {
                netMonitor.beforeRead(aioSession);
                netMonitor.afterRead(aioSession, buffer.remaining());
            }
            R request;
            // 解码
            try {
                request = config.getProtocol().decode(buffer, aioSession);
            } catch (Exception e) {
                config.getProcessor().stateEvent(aioSession, SocketStatus.DECODE_EXCEPTION, e);
                aioSession.close();
                throw e;
            }
            // 理论上每个UDP包都是一个完整的消息
            if (request == null) {
                config.getProcessor().stateEvent(aioSession, SocketStatus.DECODE_EXCEPTION, new InstrumentException("decode result is null"));
                return;
            }

            // 任务分发
            int hashCode = remote.hashCode();
            if (hashCode < 0) {
                hashCode = -hashCode;
            }
            UdpDispatcher<R> dispatcher = workerGroup[hashCode % workerGroup.length];
            dispatcher.dispatch(aioSession, request);
        }
    }

    public void shutdown() {
        status = Status.STATUS_STOPPING;
        selector.wakeup();

        for (UdpDispatcher<R> dispatcher : workerGroup) {
            dispatcher.dispatch(dispatcher.EXECUTE_TASK_OR_SHUTDOWN);
        }
    }

    /**
     * 设置读缓存区大小
     *
     * @param size 单位：byte
     * @return the object
     */
    public final UdpBootstrap<R> setReadBufferSize(int size) {
        this.config.setReadBufferSize(size);
        return this;
    }


    /**
     * 设置线程大小
     *
     * @param num 大小
     * @return the object
     */
    public final UdpBootstrap<R> setThreadNum(int num) {
        this.config.setThreadNum(num);
        return this;
    }

    enum Status {
        /**
         * 状态：初始
         */
        STATUS_INIT,
        /**
         * 状态：初始
         */
        STATUS_STARTING,
        /**
         * 状态：运行中
         */
        STATUS_RUNNING,
        /**
         * 状态：停止中
         */
        STATUS_STOPPING,
        /**
         * 状态：已停止
         */
        STATUS_STOPPED
    }

}

