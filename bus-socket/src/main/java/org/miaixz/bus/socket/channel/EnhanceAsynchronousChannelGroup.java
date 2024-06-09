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
package org.miaixz.bus.socket.channel;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class EnhanceAsynchronousChannelGroup extends AsynchronousChannelGroup {

    /**
     * 递归回调次数上限
     */
    public static final int MAX_INVOKER = 8;
    /**
     * 读回调处理线程池,可用于业务处理
     */
    private final ExecutorService readExecutorService;
    /**
     * 写回调线程池
     */
    private final ExecutorService writeExecutorService;
    /**
     * write工作组
     */
    private final Worker[] writeWorkers;
    /**
     * read工作组
     */
    private final Worker[] readWorkers;
    /**
     * 线程池分配索引
     */
    private final AtomicInteger readIndex = new AtomicInteger(0);
    private final AtomicInteger writeIndex = new AtomicInteger(0);
    /**
     * 定时任务线程池
     */
    private final ScheduledThreadPoolExecutor scheduledExecutor;
    /**
     * 服务端accept线程池
     */
    private final ExecutorService acceptExecutorService;
    /**
     * accept工作组
     */
    private final Worker[] acceptWorkers;
    private Worker futureWorker;
    /**
     * 同步IO线程池
     */
    private ExecutorService futureExecutorService;
    /**
     * group运行状态
     */
    private boolean running = true;

    /**
     * 初始化该类的新实例
     *
     * @param provider            此组的异步通道提供程序
     * @param readExecutorService 执行服务
     * @param threadNum           线程数量
     * @throws IOException 异常
     */
    public EnhanceAsynchronousChannelGroup(AsynchronousChannelProvider provider, ExecutorService readExecutorService, int threadNum) throws IOException {
        super(provider);
        // init threadPool for read
        this.readExecutorService = readExecutorService;
        this.readWorkers = new Worker[threadNum];
        for (int i = 0; i < threadNum; i++) {
            readWorkers[i] = new Worker(Selector.open(), selectionKey -> {
                EnhanceAsynchronousSocketChannel asynchronousSocketChannel = (EnhanceAsynchronousSocketChannel) selectionKey.attachment();
                asynchronousSocketChannel.doRead(true);
            });
            this.readExecutorService.execute(readWorkers[i]);
        }

        // init threadPool for write and connect
        final int writeThreadNum = 1;
        final int acceptThreadNum = 1;
        writeExecutorService = getSingleThreadExecutor("bus-socket:write");
        this.writeWorkers = new Worker[writeThreadNum];

        for (int i = 0; i < writeThreadNum; i++) {
            writeWorkers[i] = new Worker(Selector.open(), selectionKey -> {
                EnhanceAsynchronousSocketChannel asynchronousSocketChannel = (EnhanceAsynchronousSocketChannel) selectionKey.attachment();
                // 直接调用interestOps的效果比 removeOps(selectionKey, SelectionKey.OP_WRITE) 更好
                selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                asynchronousSocketChannel.doWrite();
            });
            writeExecutorService.execute(writeWorkers[i]);
        }

        //init threadPool for accept
        acceptExecutorService = getSingleThreadExecutor("bus-socket:connect");
        acceptWorkers = new Worker[acceptThreadNum];
        for (int i = 0; i < acceptThreadNum; i++) {
            acceptWorkers[i] = new Worker(Selector.open(), selectionKey -> {
                if (selectionKey.isAcceptable()) {
                    EnhanceAsynchronousServerSocketChannel serverSocketChannel = (EnhanceAsynchronousServerSocketChannel) selectionKey.attachment();
                    serverSocketChannel.doAccept();
                } else if (selectionKey.isConnectable()) {
                    EnhanceAsynchronousSocketChannel asynchronousSocketChannel = (EnhanceAsynchronousSocketChannel) selectionKey.attachment();
                    asynchronousSocketChannel.doConnect();
                }
            });
            acceptExecutorService.execute(acceptWorkers[i]);
        }

        scheduledExecutor = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "bus-socket:scheduled"));
    }

    /**
     * 同步IO注册异步线程，防止主IO线程阻塞
     */
    public synchronized void registerFuture(Consumer<Selector> register, int opType) throws IOException {
        if (futureWorker == null) {
            futureExecutorService = getSingleThreadExecutor("bus-socket:future");
            futureWorker = new Worker(Selector.open(), selectionKey -> {
                EnhanceAsynchronousSocketChannel asynchronousSocketChannel = (EnhanceAsynchronousSocketChannel) selectionKey.attachment();
                switch (opType) {
                    case SelectionKey.OP_READ:
                        removeOps(selectionKey, SelectionKey.OP_READ);
                        asynchronousSocketChannel.doRead(true);
                        break;
                    case SelectionKey.OP_WRITE:
                        removeOps(selectionKey, SelectionKey.OP_WRITE);
                        asynchronousSocketChannel.doWrite();
                        break;
                    default:
                        throw new UnsupportedOperationException("unSupport opType: " + opType);
                }

            });
            futureExecutorService.execute(futureWorker);
        }
        futureWorker.addRegister(register);
    }

    private ThreadPoolExecutor getSingleThreadExecutor(final String prefix) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), r -> new Thread(r, prefix));
    }

    /**
     * 移除关注事件
     *
     * @param selectionKey 待操作的selectionKey
     * @param opt          移除的事件
     */
    public void removeOps(SelectionKey selectionKey, int opt) {
        if (selectionKey.isValid() && (selectionKey.interestOps() & opt) != 0) {
            selectionKey.interestOps(selectionKey.interestOps() & ~opt);
        }
    }

    public Worker getReadWorker() {
        return readWorkers[(readIndex.getAndIncrement() & Integer.MAX_VALUE) % readWorkers.length];
    }

    public Worker getWriteWorker() {
        return writeWorkers[(writeIndex.getAndIncrement() & Integer.MAX_VALUE) % writeWorkers.length];
    }

    public Worker getAcceptWorker() {
        return acceptWorkers[(writeIndex.getAndIncrement() & Integer.MAX_VALUE) % acceptWorkers.length];
    }

    public Worker getConnectWorker() {
        return acceptWorkers[(writeIndex.getAndIncrement() & Integer.MAX_VALUE) % acceptWorkers.length];
    }

    public ScheduledThreadPoolExecutor getScheduledExecutor() {
        return scheduledExecutor;
    }

    @Override
    public boolean isShutdown() {
        return readExecutorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return readExecutorService.isTerminated();
    }

    @Override
    public void shutdown() {
        running = false;
        readExecutorService.shutdown();
        writeExecutorService.shutdown();
        if (acceptExecutorService != null) {
            acceptExecutorService.shutdown();
        }
        if (futureExecutorService != null) {
            futureExecutorService.shutdown();
        }
        scheduledExecutor.shutdown();
    }

    @Override
    public void shutdownNow() {
        running = false;
        readExecutorService.shutdownNow();
        writeExecutorService.shutdownNow();
        if (acceptExecutorService != null) {
            acceptExecutorService.shutdownNow();
        }
        if (futureExecutorService != null) {
            futureExecutorService.shutdownNow();
        }
        scheduledExecutor.shutdownNow();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return readExecutorService.awaitTermination(timeout, unit);
    }

    public void interestOps(Worker worker, SelectionKey selectionKey, int opt) {
        if ((selectionKey.interestOps() & opt) != 0) {
            return;
        }
        selectionKey.interestOps(selectionKey.interestOps() | opt);
        // Worker线程无需wakeup
        if (worker.getWorkerThread() != Thread.currentThread()) {
            selectionKey.selector().wakeup();
        }
    }

    public class Worker implements Runnable {

        /**
         * 当前Worker绑定的Selector
         */
        public final Selector selector;
        public final Consumer<SelectionKey> consumer;
        public final ConcurrentLinkedQueue<Consumer<Selector>> consumers = new ConcurrentLinkedQueue<>();
        public int invoker = 0;
        public Thread workerThread;

        Worker(Selector selector, Consumer<SelectionKey> consumer) {
            this.selector = selector;
            this.consumer = consumer;
        }

        /**
         * 注册事件
         */
        public final void addRegister(Consumer<Selector> register) {
            consumers.offer(register);
            selector.wakeup();
        }

        public final Thread getWorkerThread() {
            return workerThread;
        }

        @Override
        public final void run() {
            workerThread = Thread.currentThread();
            // 优先获取SelectionKey,若无关注事件触发则阻塞在selector.select(),减少select被调用次数
            Set<SelectionKey> keySet = selector.selectedKeys();
            try {
                while (running) {
                    Consumer<Selector> selectorConsumer;
                    while ((selectorConsumer = consumers.poll()) != null) {
                        selectorConsumer.accept(selector);
                    }
                    selector.select();
                    // 执行本次已触发待处理的事件
                    for (SelectionKey key : keySet) {
                        invoker = 0;
                        consumer.accept(key);
                    }
                    keySet.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
