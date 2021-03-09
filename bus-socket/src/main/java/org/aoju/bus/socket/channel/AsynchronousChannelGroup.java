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
package org.aoju.bus.socket.channel;

import org.aoju.bus.socket.WorkerRegister;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
public class AsynchronousChannelGroup extends java.nio.channels.AsynchronousChannelGroup {

    /**
     * 递归回调次数上限
     */
    public static final int MAX_INVOKER = 8;
    /**
     * 写线程数
     */
    private static final String WRITE_THREAD_NUM = "org.aoju.bus.socket.writeThreadNum";
    /**
     * accept线程数,该线程数只可少于等于进程内启用的服务端个数，多出无效
     */
    private static final String ACCEPT_THREAD_NUM = "org.aoju.bus.socket.acceptThreadNum";
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
    private ExecutorService acceptExecutorService;
    /**
     * accept工作组
     */
    private Worker[] acceptWorkers = null;
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
    protected AsynchronousChannelGroup(AsynchronousChannelProvider provider, ExecutorService readExecutorService, int threadNum) throws IOException {
        super(provider);
        // init threadPool for read
        this.readExecutorService = readExecutorService;
        this.readWorkers = new Worker[threadNum];
        for (int i = 0; i < threadNum; i++) {
            readWorkers[i] = new Worker(Selector.open(), SelectionKey.OP_READ);
            this.readExecutorService.execute(readWorkers[i]);
        }

        // init threadPool for write and connect
        final int writeThreadNum = getIntSystemProperty(WRITE_THREAD_NUM, 1);
        final int acceptThreadNum = getIntSystemProperty(ACCEPT_THREAD_NUM, 1);
        writeExecutorService = getThreadPoolExecutor("bus-socket:write-", writeThreadNum);
        this.writeWorkers = new Worker[writeThreadNum];
        int validSelectionKey = SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT;
        // accept 复用 write线程组
        if (acceptThreadNum <= 0) {
            validSelectionKey |= SelectionKey.OP_ACCEPT;
            acceptWorkers = writeWorkers;
        }
        for (int i = 0; i < writeThreadNum; i++) {
            writeWorkers[i] = new Worker(Selector.open(), validSelectionKey);
            writeExecutorService.execute(writeWorkers[i]);
        }

        if (acceptThreadNum > 0) {
            acceptExecutorService = getThreadPoolExecutor("bus-socket:accept-", acceptThreadNum);
            acceptWorkers = new Worker[acceptThreadNum];
            for (int i = 0; i < acceptThreadNum; i++) {
                acceptWorkers[i] = new Worker(Selector.open(), SelectionKey.OP_ACCEPT);
                acceptExecutorService.execute(acceptWorkers[i]);
            }
        }

        scheduledExecutor = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "bus-socket:scheduled"));
    }

    private ThreadPoolExecutor getThreadPoolExecutor(final String prefix, int threadNum) {
        return new ThreadPoolExecutor(threadNum, threadNum, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactory() {
            private final AtomicInteger atomicInteger = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, prefix + atomicInteger.getAndIncrement());
            }
        });
    }

    private int getIntSystemProperty(String key, int defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
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
        return readWorkers[index(readWorkers.length, readIndex)];
    }

    public Worker getWriteWorker() {
        return writeWorkers[index(writeWorkers.length, writeIndex)];
    }

    public Worker getAcceptWorker() {
        return acceptWorkers[index(acceptWorkers.length, writeIndex)];
    }

    public ScheduledThreadPoolExecutor getScheduledExecutor() {
        return scheduledExecutor;
    }

    /**
     * 获取分配Worker的索引下标
     *
     * @param arrayLength 数组对象长度
     * @param index       索引游标
     * @return 分配到的下标
     */
    private int index(int arrayLength, AtomicInteger index) {
        int i = index.getAndIncrement() % arrayLength;
        if (i < 0) {
            i = -i;
        }
        return i;
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
        if (worker.selector != selectionKey.selector()) {
            throw new RuntimeException();
        }
        selectionKey.interestOps(selectionKey.interestOps() | opt);
        // Worker线程无需wakeup
        if (worker.getWorkerThread() != Thread.currentThread()) {
            selectionKey.selector().wakeup();
        }
    }

    class Worker implements Runnable {
        /**
         * 当前Worker关注的有效事件
         */
        private final int validSelectionKey;
        /**
         * 当前Worker绑定的Selector
         */
        private final Selector selector;
        /**
         * 待注册的事件
         */
        private final ConcurrentLinkedQueue<WorkerRegister> registers = new ConcurrentLinkedQueue<>();
        int invoker = 0;
        private Thread workerThread;

        Worker(Selector selector, int validSelectionKey) {
            this.selector = selector;
            this.validSelectionKey = validSelectionKey;
        }

        /**
         * 注册事件
         */
        void addRegister(WorkerRegister register) {
            registers.offer(register);
            selector.wakeup();
        }

        public Thread getWorkerThread() {
            return workerThread;
        }

        @Override
        public void run() {
            workerThread = Thread.currentThread();
            // 优先获取SelectionKey,若无关注事件触发则阻塞在selector.select(),减少select被调用次数
            Set<SelectionKey> keySet = selector.selectedKeys();
            try {
                while (running) {
                    WorkerRegister register;
                    while ((register = registers.poll()) != null) {
                        register.callback(selector);
                    }
                    if (keySet.isEmpty() && selector.select() == 0) {
                        continue;
                    }
                    Iterator<SelectionKey> keyIterator = keySet.iterator();
                    // 执行本次已触发待处理的事件
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        invoker = 0;
                        if (!key.isValid()) {
                            continue;
                        }
                        if ((validSelectionKey & SelectionKey.OP_ACCEPT) > 0 && key.isAcceptable()) {
                            AsynchronousServerSocketChannel serverSocketChannel = (AsynchronousServerSocketChannel) key.attachment();
                            serverSocketChannel.doAccept();
                            continue;
                        }
                        AsynchronousSocketChannel asynchronousSocketChannel = (AsynchronousSocketChannel) key.attachment();
                        // 读取客户端数据
                        if ((validSelectionKey & SelectionKey.OP_WRITE) > 0 && key.isWritable()) {// 输出数据至客户端
                            removeOps(key, SelectionKey.OP_WRITE);
                            asynchronousSocketChannel.doWrite();
                        } else if ((validSelectionKey & SelectionKey.OP_READ) > 0 && key.isReadable()) {
                            asynchronousSocketChannel.doRead();
                        } else if ((validSelectionKey & SelectionKey.OP_CONNECT) > 0 && key.isConnectable()) {
                            asynchronousSocketChannel.doConnect();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
