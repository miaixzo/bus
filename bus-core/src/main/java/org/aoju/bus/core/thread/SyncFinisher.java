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
package org.aoju.bus.core.thread;

import org.aoju.bus.core.lang.exception.InstrumentException;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * 线程同步结束器
 * 在完成一组正在其他线程中执行的操作之前,
 * 它允许一个或多个线程一直等待
 * 不能保证同时开始
 *
 * @author Kimi Liu
 * @version 5.3.5
 * @since JDK 1.8+
 */
public class SyncFinisher {

    private CountDownLatch countDownLatch;
    private Set<Worker> workers = new LinkedHashSet<Worker>();

    /**
     * 增加工作线程
     *
     * @param worker 工作线程
     */
    synchronized public void addWorker(Worker worker) {
        workers.add(worker);
    }

    /**
     * 开始工作
     */
    public void start() {
        countDownLatch = new CountDownLatch(workers.size());
        for (Worker worker : workers) {
            worker.start();
        }
    }

    /**
     * 等待所有Worker工作结束,否则阻塞
     *
     * @throws InterruptedException 用户中断
     */
    public void await() throws InterruptedException {
        if (countDownLatch == null) {
            throw new InstrumentException("Please call start() method first!");
        }

        countDownLatch.await();
    }

    /**
     * 清空工作线程对象
     */
    public void clearWorker() {
        workers.clear();
    }

    /**
     * @return 并发数
     */
    public long count() {
        return countDownLatch.getCount();
    }

    /**
     * 工作者,为一个线程
     */
    public abstract class Worker extends Thread {

        @Override
        public void run() {
            try {
                work();
            } finally {
                countDownLatch.countDown();
            }
        }

        public abstract void work();
    }

}
