/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.socket;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.socket.accord.UdpChannel;
import org.miaixz.bus.socket.accord.UdpSession;
import org.miaixz.bus.socket.buffer.BufferPage;
import org.miaixz.bus.socket.buffer.BufferPagePool;
import org.miaixz.bus.socket.buffer.VirtualBuffer;

/**
 * A worker thread implementation that manages a {@link Selector} for handling I/O events, specifically for UDP.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Worker implements Runnable {

    private static final Runnable SELECTOR_CHANNEL = () -> {
    };
    private static final Runnable SHUTDOWN_CHANNEL = () -> {
    };
    /**
     * The Selector bound to this worker.
     */
    private final Selector selector;
    /**
     * Task queue for processing decoded messages.
     */
    private final BlockingQueue<Runnable> requestQueue = new ArrayBlockingQueue<>(256);
    /**
     * Queue for pending channel registration events.
     */
    private final ConcurrentLinkedQueue<Consumer<Selector>> registers = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService;
    /**
     * Buffer pool for write operations.
     */
    private BufferPagePool writeBufferPool = null;
    /**
     * Buffer page for read operations.
     */
    private BufferPage readBufferPage = null;
    private VirtualBuffer standbyBuffer;

    public Worker(BufferPagePool writeBufferPool, int threadNum) throws IOException {
        this(writeBufferPool.allocateBufferPage(), writeBufferPool, threadNum);
    }

    public Worker(BufferPage readBufferPage, BufferPagePool writeBufferPool, int threadNum) throws IOException {
        this.readBufferPage = readBufferPage;
        this.writeBufferPool = writeBufferPool;
        this.selector = Selector.open();
        try {
            this.requestQueue.put(SELECTOR_CHANNEL);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Start the worker thread pool
        executorService = new ThreadPoolExecutor(threadNum, threadNum, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactory() {

                    int i = 0;

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "smart-socket:udp-" + Worker.this.hashCode() + "-" + (++i));
                    }
                });
        for (int i = 0; i < threadNum; i++) {
            executorService.execute(this);
        }
    }

    /**
     * Adds a channel registration task to the queue.
     *
     * @param register The registration task.
     */
    public void addRegister(Consumer<Selector> register) {
        registers.offer(register);
        selector.wakeup();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Runnable runnable = requestQueue.take();
                // Shutdown signal
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

    public void doSelector() throws IOException {
        Consumer<Selector> register;
        while ((register = registers.poll()) != null) {
            register.accept(selector);
        }
        Set<SelectionKey> keySet = selector.selectedKeys();
        if (keySet.isEmpty()) {
            selector.select();
        }
        Iterator<SelectionKey> keyIterator = keySet.iterator();
        // Process the triggered and pending events
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

    public boolean doRead(UdpChannel channel) throws IOException {
        int count = Normal._16;
        Context context = channel.context;
        while (count-- > 0) {
            if (standbyBuffer == null) {
                standbyBuffer = readBufferPage.allocate(context.getReadBufferSize());
            }
            ByteBuffer buffer = standbyBuffer.buffer();
            SocketAddress remote = channel.getChannel().receive(buffer);
            if (remote == null) {
                buffer.clear();
                return true;
            }
            VirtualBuffer readyBuffer = standbyBuffer;
            standbyBuffer = readBufferPage.allocate(context.getReadBufferSize());
            buffer.flip();
            Runnable runnable = () -> {
                // Decode
                UdpSession session = new UdpSession(channel, remote, writeBufferPool.allocateBufferPage());
                try {
                    Monitor monitor = context.getMonitor();
                    if (monitor != null) {
                        monitor.beforeRead(session);
                        monitor.afterRead(session, buffer.remaining());
                    }
                    do {
                        Object request = context.getProtocol().decode(buffer, session);
                        // In theory, each UDP packet is a complete message
                        if (request == null) {
                            context.getProcessor().stateEvent(
                                    session,
                                    Status.DECODE_EXCEPTION,
                                    new InternalException("decode result is null, buffer size: " + buffer.remaining()));
                            break;
                        } else {
                            context.getProcessor().process(session, request);
                        }
                    } while (buffer.hasRemaining());
                } catch (Throwable e) {
                    e.printStackTrace();
                    context.getProcessor().stateEvent(session, Status.DECODE_EXCEPTION, e);
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

    /**
     * Shuts down the worker and its associated resources.
     */
    public void shutdown() {
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
