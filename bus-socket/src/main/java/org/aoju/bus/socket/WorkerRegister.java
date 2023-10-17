/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org sandao and other contributors.               *
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

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.socket.buffers.BufferPool;
import org.aoju.bus.socket.buffers.VirtualBuffer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class WorkerRegister implements Runnable {

    private final static int MAX_READ_TIMES = 16;
    private static final Runnable SELECTOR_CHANNEL = () -> {
    };
    private static final Runnable SHUTDOWN_CHANNEL = () -> {
    };
    /**
     * 当前Worker绑定的Selector
     */
    private final Selector selector;
    /**
     * 内存池
     */
    private final BufferPool bufferPool;
    private final BlockingQueue<Runnable> requestQueue = new ArrayBlockingQueue<>(256);
    /**
     * 待注册的事件
     */
    private final ConcurrentLinkedQueue<Consumer<Selector>> registers = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService;
    private VirtualBuffer standbyBuffer;

    public WorkerRegister(BufferPool bufferPool, int threadNum) throws IOException {
        this.bufferPool = bufferPool;
        this.selector = Selector.open();
        try {
            this.requestQueue.put(SELECTOR_CHANNEL);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 启动worker线程组
        executorService = new ThreadPoolExecutor(threadNum, threadNum,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactory() {
            int i = 0;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "bus-socket:udp-" + WorkerRegister.this.hashCode() + "-" + (++i));
            }
        });
        for (int i = 0; i < threadNum; i++) {
            executorService.execute(this);
        }
    }

    /**
     * 注册事件
     */
    void addRegister(Consumer<Selector> register) {
        registers.offer(register);
        selector.wakeup();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Runnable runnable = requestQueue.take();
                // 服务终止
                if (runnable == SHUTDOWN_CHANNEL) {
                    requestQueue.put(SHUTDOWN_CHANNEL);
                    selector.wakeup();
                    break;
                } else if (runnable == SELECTOR_CHANNEL) {
                    try {
                        doSelector();
                    } finally {
                        requestQueue.put(SELECTOR_CHANNEL);
                    }
                } else {
                    runnable.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doSelector() throws IOException {
        Consumer<Selector> register;
        while ((register = registers.poll()) != null) {
            register.accept(selector);
        }
        Set<SelectionKey> keySet = selector.selectedKeys();
        if (keySet.isEmpty()) {
            selector.select();
        }
        Iterator<SelectionKey> keyIterator = keySet.iterator();
        // 执行本次已触发待处理的事件
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            UdpChannel udpChannel = (UdpChannel) key.attachment();
            if (!key.isValid()) {
                keyIterator.remove();
                udpChannel.close();
                continue;
            }
            if (key.isWritable()) {
                udpChannel.doWrite();
            }
            if (key.isReadable() && !doRead(udpChannel)) {
                break;
            }
            keyIterator.remove();
        }
    }

    private boolean doRead(UdpChannel channel) throws IOException {
        int count = MAX_READ_TIMES;
        ServerConfig config = channel.config;
        while (count-- > 0) {
            if (standbyBuffer == null) {
                standbyBuffer = channel.getBufferPage().allocate(config.getReadBufferSize());
            }
            ByteBuffer buffer = standbyBuffer.buffer();
            SocketAddress remote = channel.getChannel().receive(buffer);
            if (remote == null) {
                buffer.clear();
                return true;
            }
            VirtualBuffer readyBuffer = standbyBuffer;
            standbyBuffer = channel.getBufferPage().allocate(config.getReadBufferSize());
            buffer.flip();
            Runnable runnable = () -> {
                // 解码
                UdpAioSession session = new UdpAioSession(channel, remote, bufferPool.allocateBufferPage());
                try {
                    NetMonitor netMonitor = config.getMonitor();
                    if (netMonitor != null) {
                        netMonitor.beforeRead(session);
                        netMonitor.afterRead(session, buffer.remaining());
                    }
                    do {
                        Object request = config.getProtocol().decode(buffer, session);
                        // 理论上每个UDP包都是一个完整的消息
                        if (request == null) {
                            config.getProcessor().stateEvent(session, SocketStatus.DECODE_EXCEPTION, new InternalException("decode result is null, buffer size: " + buffer.remaining()));
                            break;
                        } else {
                            config.getProcessor().process(session, request);
                        }
                    } while (buffer.hasRemaining());
                } catch (Throwable e) {
                    e.printStackTrace();
                    config.getProcessor().stateEvent(session, SocketStatus.DECODE_EXCEPTION, e);
                } finally {
                    session.writeBuffer().flush();
                    readyBuffer.clean();
                }
            };
            if (!requestQueue.offer(runnable)) {
                return false;
            }
        }
        return true;
    }

    void shutdown() {
        try {
            requestQueue.put(SHUTDOWN_CHANNEL);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        selector.wakeup();
        executorService.shutdown();
        try {
            selector.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}