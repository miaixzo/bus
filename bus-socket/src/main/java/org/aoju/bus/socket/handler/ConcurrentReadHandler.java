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
package org.aoju.bus.socket.handler;

import org.aoju.bus.socket.TcpAioSession;

import java.util.concurrent.*;

/**
 * 读写事件回调处理类
 *
 * @author Kimi Liu
 * @version 6.2.2
 * @since JDK 1.8+
 */
public class ConcurrentReadHandler<T> extends CompletionReadHandler<T> {

    /**
     * 读回调资源信号量
     */
    private final Semaphore semaphore;

    private final ThreadLocal<ConcurrentReadHandler<T>> threadLocal = new ThreadLocal<>();

    private final LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService = new ThreadPoolExecutor(1, 1,
            60L, TimeUnit.SECONDS, taskQueue);

    public ConcurrentReadHandler(final Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    @Override
    public void completed(final Integer result, final TcpAioSession<T> aioSession) {
        if (null != threadLocal.get()) {
            super.completed(result, aioSession);
            return;
        }
        if (semaphore.tryAcquire()) {
            threadLocal.set(this);
            //处理当前读回调任务
            super.completed(result, aioSession);
            Runnable task;
            while (null != (task = taskQueue.poll())) {
                task.run();
            }
            semaphore.release();
            threadLocal.set(null);
            return;
        }
        //线程资源不足,暂时积压任务
        executorService.execute(() -> ConcurrentReadHandler.super.completed(result, aioSession));

    }

    /**
     * 停止内部线程
     */
    public void shutdown() {
        executorService.shutdown();
    }

}