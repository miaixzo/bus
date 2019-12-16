/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package org.aoju.bus.office.metric;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.Builder;
import org.aoju.bus.office.builtin.MadeInOffice;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OfficeManagerPool负责维护一个用于执行{@link MadeInOffice}的{@link OfficeProcessPoolEntry}池
 * 当调用{@link #execute(MadeInOffice)}函数时，池将使用第一个{@link OfficeProcessPoolEntry}来执行给定的任务
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public abstract class AbstractOfficePool extends AbstractOfficeManager {

    private static final int POOL_STOPPED = 0;
    private static final int POOL_STARTED = 1;
    private static final int POOL_SHUTDOWN = 2;
    protected final OfficeManagerPoolConfig config;
    private final AtomicInteger poolState = new AtomicInteger(POOL_STOPPED);
    private final BlockingQueue<OfficeManager> pool;
    private OfficeManager[] entries;

    /**
     * 使用指定的设置构造类的新实例.
     *
     * @param poolSize 池的大小
     * @param config   配置项
     */
    protected AbstractOfficePool(final int poolSize, final OfficeManagerPoolConfig config) {
        super(config);

        this.config = config;

        pool = new ArrayBlockingQueue<>(poolSize);
    }

    /**
     * 在池启动时创建池条目.
     *
     * @return 一个池条目数组.
     */
    protected abstract OfficeManager[] createPoolEntries();

    @Override
    public void execute(final MadeInOffice task) throws InstrumentException {
        if (!isRunning()) {
            throw new IllegalStateException("This office manager is not running.");
        }

        OfficeManager entry = null;
        try {
            entry = acquireManager();
            entry.execute(task);
        } finally {
            if (entry != null) {
                releaseManager(entry);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return poolState.get() == POOL_STARTED;
    }

    @Override
    public void start() throws InstrumentException {
        synchronized (this) {
            if (poolState.get() == POOL_SHUTDOWN) {
                throw new IllegalStateException("This office manager has been shutdown.");
            }

            if (poolState.get() == POOL_STARTED) {
                throw new IllegalStateException("This office manager is already running.");
            }

            entries = createPoolEntries();

            doStart();

            makeTempDir();

            poolState.set(POOL_STARTED);
        }
    }

    @Override
    public void stop() throws InstrumentException {
        synchronized (this) {
            if (poolState.get() == POOL_SHUTDOWN) {
                return;
            }

            poolState.set(POOL_SHUTDOWN);
            try {
                doStop();
            } finally {
                deleteTempDir();
            }
        }
    }

    /**
     * 获取管理器，等待配置的超时以使某个条目可用.
     *
     * @return 一个有空的office管理器.
     * @throws InstrumentException 如果我们找不到管理器.
     */
    private OfficeManager acquireManager() throws InstrumentException {
        try {
            final OfficeManager manager = pool.poll(config.getTaskQueueTimeout(), TimeUnit.MILLISECONDS);
            if (manager == null) {
                throw new InstrumentException(
                        "No office manager available after " + config.getTaskQueueTimeout() + " millisec.");
            }
            return manager;
        } catch (InterruptedException interruptedEx) {
            throw new InstrumentException(
                    "Thread has been interrupted while waiting for a manager to become available.",
                    interruptedEx);
        }
    }

    /**
     * 使给定的管理器可用于执行任务.
     *
     * @param manager office管理器.
     * @throws InstrumentException 如果我们不能释放管理器.
     */
    private void releaseManager(final OfficeManager manager) throws InstrumentException {
        try {
            pool.put(manager);
        } catch (InterruptedException interruptedEx) {
            throw new InstrumentException("interrupted", interruptedEx);
        }
    }

    /**
     * 允许基类在池启动时执行操作.
     *
     * @throws InstrumentException 如果发生错误.
     */
    protected void doStart() throws InstrumentException {
        for (final OfficeManager manager : entries) {
            manager.start();
            releaseManager(manager);
        }
    }

    private void doStop() throws InstrumentException {
        Logger.info("Stopping the office manager pool...");
        pool.clear();

        InstrumentException firstException = null;
        for (final OfficeManager manager : entries) {
            try {
                manager.stop();
            } catch (InstrumentException ex) {
                if (firstException == null) {
                    firstException = ex;
                }
            }
        }

        if (firstException != null) {
            throw firstException;
        }

        Logger.info("Office manager stopped");
    }

    /**
     * 构造{@link AbstractOfficePool}的生成器.
     */
    public abstract static class AbstractOfficeManagerPoolBuilder<
            B extends AbstractOfficeManagerPoolBuilder<B>>
            extends AbstractOfficeManagerBuilder<B> {

        protected long taskExecutionTimeout = Builder.DEFAULT_TASK_EXECUTION_TIMEOUT;
        protected long taskQueueTimeout = Builder.DEFAULT_TASK_QUEUE_TIMEOUT;

        protected AbstractOfficeManagerPoolBuilder() {
            super();
        }

        /**
         * 指定允许处理任务的最大时间。如果任务的处理时间长于此超时，则此任务将中止并处理下一个任务.
         * 默认: 120000毫秒 (2 分钟)
         *
         * @param taskExecutionTimeout T任务执行超时，以毫秒为单位.
         * @return 当前实例信息.
         */
        public B taskExecutionTimeout(final long taskExecutionTimeout) {
            Assert.notNull(
                    taskExecutionTimeout,
                    String.format(
                            "The taskExecutionTimeout %s must greater than or equal to 0", taskExecutionTimeout));
            this.taskExecutionTimeout = taskExecutionTimeout;
            return (B) this;
        }

        /**
         * 指定转换队列中任务的最大生存时间。如果等待时间长于此超时，则任务将从队列中删除.
         * 默认: 30000毫秒 (30秒)
         *
         * @param taskQueueTimeout 任务队列超时，以毫秒为单位.
         * @return 当前实例信息.
         */
        public B taskQueueTimeout(final long taskQueueTimeout) {
            this.taskQueueTimeout = taskQueueTimeout;
            return (B) this;
        }

        /**
         * 创建此生成器指定的管理器.
         *
         * @return 由该生成器指定的管理器.
         */
        protected abstract AbstractOfficePool build();
    }

}
