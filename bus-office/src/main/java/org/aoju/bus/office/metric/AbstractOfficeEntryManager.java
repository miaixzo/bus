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

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.thread.NamedThreadFactory;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.office.builtin.MadeInOffice;

import java.util.concurrent.*;

/**
 * 所有office管理器池条目实现的基类.
 * 这里需要注意的一点是，要记住，子类负责管理任务执行器的可用性
 * 这个抽象类从未将可用性设置为true 只有当管理器停止时，可用性才设置为false.
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public abstract class AbstractOfficeEntryManager implements OfficeManager {

    protected final OfficeManagerEntryBuilder config;
    protected final SuspendableThread taskExecutor;
    protected Future<?> currentFuture;

    /**
     * 使用指定的配置初始化新的池条目.
     *
     * @param config 输入配置.
     */
    public AbstractOfficeEntryManager(final OfficeManagerEntryBuilder config) {

        this.config = config;
        taskExecutor = new SuspendableThread(new NamedThreadFactory("OfficeManagerPoolEntry"));
    }

    @Override
    public final void execute(final MadeInOffice task) throws InstrumentException {
        currentFuture =
                taskExecutor.submit(
                        new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                doExecute(task);
                                return null;
                            }
                        });
        try {
            Logger.debug("Waiting for task to complete...");
            currentFuture.get(config.getTaskExecutionTimeout(), TimeUnit.MILLISECONDS);
            Logger.debug("Task executed successfully");
        } catch (TimeoutException timeoutEx) {
            handleExecuteTimeoutException(timeoutEx);
            throw new InstrumentException("Task did not complete within timeout", timeoutEx);
        } catch (ExecutionException executionEx) {
            if (executionEx.getCause() instanceof InstrumentException) {
                throw (InstrumentException) executionEx.getCause();
            }
            throw new InstrumentException("Task failed", executionEx.getCause());
        } catch (Exception ex) {
            throw new InstrumentException("Task failed", ex);
        } finally {
            currentFuture = null;
        }
    }

    /**
     * 执行任务.
     *
     * @param task 任务
     * @throws Exception 如果转换期间发生任何错误.
     */
    protected abstract void doExecute(final MadeInOffice task) throws Exception;

    /**
     * 处理在执行任务时引发的超时异常.
     *
     * @param timeoutEx 抛出异常.
     */
    protected void handleExecuteTimeoutException(final TimeoutException timeoutEx) {
        Logger.debug("Handleling task execution timeout...");
    }

    @Override
    public boolean isRunning() {
        return !taskExecutor.isShutdown();
    }

    @Override
    public final void start() throws InstrumentException {
        if (taskExecutor.isShutdown()) {
            throw new IllegalStateException("This office manager (pool entry) has been shutdown.");
        }
        doStart();
    }

    /**
     * 当office管理器启动时，允许子类执行操作.
     *
     * @throws InstrumentException 如果启动管理器时发生错误.
     */
    protected abstract void doStart() throws InstrumentException;

    @Override
    public final void stop() throws InstrumentException {
        taskExecutor.setAvailable(false);
        taskExecutor.shutdownNow();
        doStop();
    }

    /**
     * 当office管理器停止时，允许子类执行操作.
     *
     * @throws InstrumentException 如果在停止管理器时发生错误.
     */
    protected abstract void doStop() throws InstrumentException;

}
