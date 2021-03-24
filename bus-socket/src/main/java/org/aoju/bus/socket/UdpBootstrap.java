/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org sandao and other contributors.               *
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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * UDP服务启动类
 *
 * @param <R> 请求信息
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
public class UdpBootstrap<R> {

    private final static int MAX_READ_TIMES = 16;
    /**
     * 服务ID
     */
    private static int UID;
    /**
     * 缓存页
     */
    private final PageBuffer bufferPage = new org.aoju.bus.core.io.ByteBuffer(1024 * 1024, 1, -1, true).allocatePageBuffer();
    /**
     * 服务配置
     */
    private final ServerConfig<R> config = new ServerConfig<>();

    private Worker worker;

    private UdpDispatcher<R>[] workerGroup;
    private ExecutorService executorService;

    private boolean running = true;

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
        //启动线程服务
        if (null == worker) {
            initThreadServer();
        }

        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        if (port > 0) {
            InetSocketAddress inetSocketAddress =null == host ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
            channel.socket().bind(inetSocketAddress);
        }
        UdpChannel<R> udpChannel = new UdpChannel(channel, worker, config, bufferPage);
        worker.addRegister(selector -> {
            try {
                SelectionKey selectionKey = channel.register(selector, SelectionKey.OP_READ);
                udpChannel.setSelectionKey(selectionKey);
                selectionKey.attach(udpChannel);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        });
        return udpChannel;
    }

    private synchronized void initThreadServer() throws IOException {
        if (null != worker) {
            return;
        }

        int uid = UdpBootstrap.UID++;

        //启动worker线程组
        workerGroup = new UdpDispatcher[config.getThreadNum()];
        executorService = new ThreadPoolExecutor(config.getThreadNum(), config.getThreadNum(),
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactory() {
            int i = 0;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "socket:udp-" + uid + "-" + (++i));
            }
        });
        for (int i = 0; i < config.getThreadNum(); i++) {
            workerGroup[i] = new UdpDispatcher(config.getProcessor());
            executorService.execute(workerGroup[i]);
        }
        //启动Boss线程组
        worker = new Worker();
        new Thread(worker, "socket:udp-" + uid).start();
    }

    private void doRead(VirtualBuffer readBuffer, UdpChannel channel) throws IOException {
        int count = MAX_READ_TIMES;
        while (count-- > 0) {
            // 接收数据
            ByteBuffer buffer = readBuffer.buffer();
            buffer.clear();
            SocketAddress remote = channel.getChannel().receive(buffer);
            if (null == remote) {
                return;
            }
            buffer.flip();

            UdpAioSession aioSession = channel.createAndCacheSession(remote);
            NetMonitor netMonitor = config.getMonitor();
            if (null != netMonitor) {
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
            if (null == request) {
                config.getProcessor().stateEvent(aioSession, SocketStatus.DECODE_EXCEPTION, new InstrumentException("decode result is null"));
            } else {
                // 任务分发
                workerGroup[(remote.hashCode() & Integer.MAX_VALUE) % workerGroup.length].dispatch(aioSession, request);
            }
        }
    }

    public void shutdown() {
        running = false;
        worker.selector.wakeup();

        for (UdpDispatcher<R> dispatcher : workerGroup) {
            dispatcher.dispatch(dispatcher.EXECUTE_TASK_OR_SHUTDOWN);
        }
        executorService.shutdown();
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

    class Worker implements Runnable {
        /**
         * 当前Worker绑定的Selector
         */
        private final Selector selector;

        /**
         * 待注册的事件
         */
        private final ConcurrentLinkedQueue<Consumer<Selector>> registers = new ConcurrentLinkedQueue<>();

        Worker() throws IOException {
            this.selector = Selector.open();
        }

        /**
         * 注册事件
         */
        final void addRegister(Consumer<Selector> register) {
            registers.offer(register);
            selector.wakeup();
        }

        @Override
        public final void run() {
            // 优先获取SelectionKey,若无关注事件触发则阻塞在selector.select(),减少select被调用次数
            Set<SelectionKey> keySet = selector.selectedKeys();
            //读缓冲区
            VirtualBuffer readBuffer = bufferPage.allocate(config.getReadBufferSize());
            try {
                while (running) {
                    Consumer<Selector> register;
                    while (null != (register = registers.poll())) {
                        register.accept(selector);
                    }
                    if (keySet.isEmpty() && selector.select() == 0) {
                        continue;
                    }
                    Iterator<SelectionKey> keyIterator = keySet.iterator();
                    // 执行本次已触发待处理的事件
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        UdpChannel udpChannel = (UdpChannel) key.attachment();
                        if (!key.isValid()) {
                            udpChannel.close();
                            continue;
                        }

                        if (key.isReadable()) {
                            doRead(readBuffer, udpChannel);
                        }
                        if (key.isWritable()) {
                            udpChannel.doWrite();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //读缓冲区内存回收
                readBuffer.clean();
            }
        }
    }

}

