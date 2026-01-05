/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.metric.channel;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * An asynchronous channel group implementation.
 * <p>
 * This class extends {@link java.nio.channels.AsynchronousChannelGroup} to provide a custom implementation for managing
 * asynchronous channels, including thread pools for read and write operations.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
class AsynchronousChannelGroup extends java.nio.channels.AsynchronousChannelGroup {

    /**
     * The maximum number of recursive callbacks allowed.
     */
    public static final int MAX_INVOKER = 8;
    /**
     * The worker responsible for write operations.
     */
    final Worker commonWorker;
    final Worker writeWorker;
    /**
     * The thread pool for handling read callbacks, which can also be used for business processing.
     */
    private final ExecutorService readExecutorService;
    /**
     * The thread pool for handling common I/O operations and write callbacks.
     */
    private final ExecutorService commonExecutorService;
    /**
     * An array of workers dedicated to read operations.
     */
    private final Worker[] readWorkers;
    /**
     * An atomic integer used as an index to distribute read tasks among {@code readWorkers}.
     */
    private final AtomicInteger readIndex = new AtomicInteger(0);

    /**
     * The running status of the channel group.
     */
    boolean running = true;

    /**
     * Initializes a new instance of this class.
     *
     * @param provider            The asynchronous channel provider for this group.
     * @param readExecutorService The executor service for read operations.
     * @param threadNum           The number of threads to use for read workers.
     * @throws IOException if an I/O error occurs.
     */
    protected AsynchronousChannelGroup(AsynchronousChannelProvider provider, ExecutorService readExecutorService,
            int threadNum) throws IOException {
        super(provider);
        // init threadPool for read
        this.readExecutorService = readExecutorService;
        this.readWorkers = new Worker[threadNum];
        for (int i = 0; i < threadNum; i++) {
            readWorkers[i] = new Worker(Selector.open(), selectionKey -> {
                AsynchronousServerChannel asynchronousSocketChannel = (AsynchronousServerChannel) selectionKey
                        .attachment();
                asynchronousSocketChannel.doRead(true);
            });
            this.readExecutorService.execute(readWorkers[i]);
        }

        // init threadPool for write and connect
        writeWorker = new Worker(Selector.open(), selectionKey -> {
            AsynchronousServerChannel asynchronousSocketChannel = (AsynchronousServerChannel) selectionKey.attachment();
            // Directly calling interestOps is more effective than removeOps(selectionKey, SelectionKey.OP_WRITE)
            if (running) {
                selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
            }
            while (asynchronousSocketChannel.doWrite())
                ;
        });
        commonWorker = new Worker(Selector.open(), selectionKey -> {
            if (selectionKey.isAcceptable()) {
                AsynchronousServerSocketChannel serverSocketChannel = (AsynchronousServerSocketChannel) selectionKey
                        .attachment();
                serverSocketChannel.doAccept();
            } else if (selectionKey.isConnectable()) {
                Runnable runnable = (Runnable) selectionKey.attachment();
                runnable.run();
            } else if (selectionKey.isReadable()) {
                // This thread resource is only used for synchronous reads
                AsynchronousServerChannel asynchronousSocketChannel = (AsynchronousServerChannel) selectionKey
                        .attachment();
                removeOps(selectionKey, SelectionKey.OP_READ);
                asynchronousSocketChannel.doRead(true);
            } else {
                throw new IllegalStateException("Unexpected callback, key valid:" + selectionKey.isValid()
                        + " ,interestOps:" + selectionKey.interestOps());
            }
        });

        commonExecutorService = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                r -> new Thread(r, "Socket:common"));
        commonExecutorService.execute(writeWorker);
        commonExecutorService.execute(commonWorker);
    }

    /**
     * Removes the specified operation from the interest set of a {@link SelectionKey}.
     *
     * @param selectionKey the {@link SelectionKey} to modify
     * @param opt          the operation to remove (e.g., {@link SelectionKey#OP_READ})
     */
    public static void removeOps(SelectionKey selectionKey, int opt) {
        if (selectionKey.isValid() && (selectionKey.interestOps() & opt) != 0) {
            selectionKey.interestOps(selectionKey.interestOps() & ~opt);
        }
    }

    /**
     * Adds the specified operation to the interest set of a {@link SelectionKey}. If the worker thread is not the
     * current thread, the selector will be woken up.
     *
     * @param worker       the {@link Worker} associated with the selection key
     * @param selectionKey the {@link SelectionKey} to modify
     * @param opt          the operation to add (e.g., {@link SelectionKey#OP_WRITE})
     */
    public static void interestOps(Worker worker, SelectionKey selectionKey, int opt) {
        if ((selectionKey.interestOps() & opt) != 0) {
            return;
        }
        selectionKey.interestOps(selectionKey.interestOps() | opt);
        // Worker threads do not need to be woken up
        if (worker.workerThread != Thread.currentThread()) {
            selectionKey.selector().wakeup();
        }
    }

    /**
     * Gets a read worker using a round-robin strategy.
     *
     * @return a {@link Worker} instance for read operations
     */
    public Worker getReadWorker() {
        return readWorkers[(readIndex.getAndIncrement() & Integer.MAX_VALUE) % readWorkers.length];
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return {@code true} if this channel group has been shut down, {@code false} otherwise
     */
    @Override
    public boolean isShutdown() {
        return readExecutorService.isShutdown();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return {@code true} if all tasks have completed following shutdown, {@code false} otherwise
     */
    @Override
    public boolean isTerminated() {
        return readExecutorService.isTerminated();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This implementation interrupts all worker threads and shuts down both the read and common executor services.
     * </p>
     */
    @Override
    public void shutdown() {
        running = false;
        commonWorker.workerThread.interrupt();
        writeWorker.workerThread.interrupt();
        for (Worker worker : readWorkers) {
            worker.workerThread.interrupt();
        }
        readExecutorService.shutdown();
        commonExecutorService.shutdown();
    }

    /**
     * Description inherited from parent class or interface.
     * <p>
     * This implementation delegates to {@link #shutdown()}.
     * </p>
     */
    @Override
    public void shutdownNow() {
        shutdown();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return {@code true} if this channel group terminated and {@code false} if the timeout elapsed before termination
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return readExecutorService.awaitTermination(timeout, unit);
    }

    /**
     * A worker class that manages a {@link Selector} and processes its selected keys. Each worker runs in its own
     * thread.
     */
    class Worker implements Runnable {

        /**
         * The {@link Selector} bound to this worker.
         */
        final Selector selector;
        private final Consumer<SelectionKey> consumer;
        private final ConcurrentLinkedQueue<Consumer<Selector>> consumers = new ConcurrentLinkedQueue<>();
        private Thread workerThread;

        /**
         * Constructs a new Worker.
         *
         * @param selector the {@link Selector} to be managed by this worker
         * @param consumer a {@link Consumer} to process the selected {@link SelectionKey}s
         */
        Worker(Selector selector, Consumer<SelectionKey> consumer) {
            this.selector = selector;
            this.consumer = consumer;
        }

        /**
         * Adds a registration task to be executed by this worker's selector.
         *
         * @param register a {@link Consumer} that performs the registration on the {@link Selector}
         */
        final void addRegister(Consumer<Selector> register) {
            consumers.offer(register);
            selector.wakeup();
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * This worker continuously processes I/O events by:
         * </p>
         * <ul>
         * <li>Executing pending registration tasks</li>
         * <li>Waiting for selector events via {@link Selector#select()}</li>
         * <li>Processing triggered events through the configured consumer</li>
         * <li>Cleaning up resources when the worker stops</li>
         * </ul>
         */
        @Override
        public final void run() {
            workerThread = Thread.currentThread();
            // Prioritize obtaining SelectionKey; if no interested events are triggered, block on selector.select(),
            // reducing select call frequency.
            Set<SelectionKey> keySet = selector.selectedKeys();
            try {
                while (running) {
                    Consumer<Selector> selectorConsumer;
                    while ((selectorConsumer = consumers.poll()) != null) {
                        selectorConsumer.accept(selector);
                    }
                    selector.select();

                    // Execute the events triggered in this cycle
                    for (SelectionKey key : keySet) {
                        consumer.accept(key);
                    }
                    keySet.clear();
                }
                selector.keys().forEach(key -> {
                    try {
                        consumer.accept(key);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
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
