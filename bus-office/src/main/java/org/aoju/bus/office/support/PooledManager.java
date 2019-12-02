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
package org.aoju.bus.office.support;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Manager;
import org.aoju.bus.office.Office;
import org.aoju.bus.office.magic.UnoUrl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
class PooledManager implements Manager {

    private final PooledSettings settings;
    private final ManagedProcess managedOfficeProcess;
    private final SuspendableExecutor taskExecutor;
    private final Logger logger = Logger.getLogger(getClass().getName());
    private volatile boolean stopping = false;
    private int taskCount;
    private Future<?> currentTask;
    private ConnectionListener connectionEventListener = new ConnectionListener() {
        public void connected(ConnectionEvent event) {
            taskCount = 0;
            taskExecutor.setAvailable(true);
        }

        public void disconnected(ConnectionEvent event) {
            taskExecutor.setAvailable(false);
            if (stopping) {
                // expected
                stopping = false;
            } else {
                logger.warning("connection lost unexpectedly; attempting restart");
                if (currentTask != null) {
                    currentTask.cancel(true);
                }
                managedOfficeProcess.restartDueToLostConnection();
            }
        }
    };

    public PooledManager(UnoUrl unoUrl) {
        this(new PooledSettings(unoUrl));
    }

    public PooledManager(PooledSettings settings) {
        this.settings = settings;
        managedOfficeProcess = new ManagedProcess(settings);
        managedOfficeProcess.getConnection().addConnectionEventListener(connectionEventListener);
        taskExecutor = new SuspendableExecutor(new NamedThreadFactory("OfficeTaskThread"));
    }

    public void execute(final Office task) throws InstrumentException {
        Future<?> futureTask = taskExecutor.submit(new Runnable() {
            public void run() {
                if (settings.getMaxTasksPerProcess() > 0 && ++taskCount == settings.getMaxTasksPerProcess() + 1) {
                    logger.info(String.format("reached limit of %d maxTasksPerProcess: restarting", settings.getMaxTasksPerProcess()));
                    taskExecutor.setAvailable(false);
                    stopping = true;
                    managedOfficeProcess.restartAndWait();
                    //FIXME taskCount will be 0 rather than 1 at this point
                }
                task.execute(managedOfficeProcess.getConnection());
            }
        });
        currentTask = futureTask;
        try {
            futureTask.get(settings.getTaskExecutionTimeout(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException timeoutException) {
            managedOfficeProcess.restartDueToTaskTimeout();
            throw new InstrumentException("task did not complete within timeout", timeoutException);
        } catch (ExecutionException executionException) {
            if (executionException.getCause() instanceof InstrumentException) {
                throw (InstrumentException) executionException.getCause();
            } else {
                throw new InstrumentException("task failed", executionException.getCause());
            }
        } catch (Exception exception) {
            throw new InstrumentException("task failed", exception);
        }
    }

    public void start() throws InstrumentException {
        managedOfficeProcess.startAndWait();
    }

    public void stop() throws InstrumentException {
        taskExecutor.setAvailable(false);
        stopping = true;
        taskExecutor.shutdownNow();
        managedOfficeProcess.stopAndWait();
    }

    public boolean isRunning() {
        return managedOfficeProcess.isConnected();
    }

}
