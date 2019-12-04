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

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Kimi Liu
 * @version 5.3.2
 * @since JDK 1.8+
 */
class ProcessManager implements Manager {

    private final BlockingQueue<PooledManager> pool;
    private final PooledManager[] pooledManagers;
    private final long taskQueueTimeout;
    private final Logger logger = Logger.getLogger(ProcessManager.class.getName());
    private volatile boolean running = false;

    public ProcessManager(File officeHome, UnoUrl[] unoUrls, String[] runAsArgs, File templateProfileDir, File workDir,
                          long retryTimeout, long taskQueueTimeout, long taskExecutionTimeout, int maxTasksPerProcess,
                          org.aoju.bus.office.process.ProcessManager processManager) {
        this.taskQueueTimeout = taskQueueTimeout;
        pool = new ArrayBlockingQueue<PooledManager>(unoUrls.length);
        pooledManagers = new PooledManager[unoUrls.length];
        for (int i = 0; i < unoUrls.length; i++) {
            PooledSettings settings = new PooledSettings(unoUrls[i]);
            settings.setRunAsArgs(runAsArgs);
            settings.setTemplateProfileDir(templateProfileDir);
            settings.setWorkDir(workDir);
            settings.setOfficeHome(officeHome);
            settings.setRetryTimeout(retryTimeout);
            settings.setTaskExecutionTimeout(taskExecutionTimeout);
            settings.setMaxTasksPerProcess(maxTasksPerProcess);
            settings.setProcessManager(processManager);
            pooledManagers[i] = new PooledManager(settings);
        }
        logger.info("ProcessManager implementation is " + processManager.getClass().getSimpleName());
    }

    public synchronized void start() throws InstrumentException {
        for (int i = 0; i < pooledManagers.length; i++) {
            pooledManagers[i].start();
            releaseManager(pooledManagers[i]);
        }
        running = true;
    }

    public void execute(Office task) throws IllegalStateException, InstrumentException {
        if (!running) {
            throw new IllegalStateException("this OfficeManager is currently stopped");
        }
        PooledManager manager = null;
        try {
            manager = acquireManager();
            if (manager == null) {
                throw new InstrumentException("no office manager available");
            }
            manager.execute(task);
        } finally {
            if (manager != null) {
                releaseManager(manager);
            }
        }
    }

    public synchronized void stop() throws InstrumentException {
        running = false;
        logger.info("stopping");
        pool.clear();
        for (int i = 0; i < pooledManagers.length; i++) {
            pooledManagers[i].stop();
        }
        logger.info("stopped");
    }

    private PooledManager acquireManager() {
        try {
            return pool.poll(taskQueueTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interruptedException) {
            throw new InstrumentException("interrupted", interruptedException);
        }
    }

    private void releaseManager(PooledManager manager) {
        try {
            pool.put(manager);
        } catch (InterruptedException interruptedException) {
            throw new InstrumentException("interrupted", interruptedException);
        }
    }

    public boolean isRunning() {
        return running;
    }

}
