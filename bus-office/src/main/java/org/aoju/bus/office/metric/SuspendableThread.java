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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 可以挂起的线程池执行程序,池中只允许有一个线程
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
 */
public class SuspendableThread extends ThreadPoolExecutor {

    private final ReentrantLock suspendLock = new ReentrantLock();
    private final Condition availableCondition = suspendLock.newCondition();
    private boolean available;

    public SuspendableThread(final ThreadFactory threadFactory) {
        super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), threadFactory);
    }

    @Override
    protected void beforeExecute(final Thread thread, final Runnable task) {
        super.beforeExecute(thread, task);

        suspendLock.lock();
        try {
            while (!available) {
                availableCondition.await();
            }
        } catch (InterruptedException interruptedEx) {
            thread.interrupt();
        } finally {
            suspendLock.unlock();
        }
    }

    /**
     * 设置此执行程序的可用性.
     *
     * @param available 如果执行器可以执行任务{@code true}，否则{@code false} .
     */
    public void setAvailable(final boolean available) {
        suspendLock.lock();
        try {
            this.available = available;
            if (available) {
                availableCondition.signalAll();
            }
        } finally {
            suspendLock.unlock();
        }
    }

}
