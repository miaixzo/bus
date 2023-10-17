/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org sandao and other contributors.               *
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

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 读写事件回调处理类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConcurrentReadCompletionHandler extends ReadCompletionHandler {

    /**
     * 读回调资源信号量
     */
    private final Semaphore semaphore;

    private final ThreadLocal<ConcurrentReadCompletionHandler> threadLocal = new ThreadLocal<>();

    private final ThreadPoolExecutor threadPoolExecutor;

    public ConcurrentReadCompletionHandler(final Semaphore semaphore, ThreadPoolExecutor threadPoolExecutor) {
        this.semaphore = semaphore;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public void completed(final Integer result, final TcpAioSession aioSession) {
        if (threadLocal.get() != null) {
            super.completed(result, aioSession);
            return;
        }
        if (semaphore.tryAcquire()) {
            threadLocal.set(this);
            //处理当前读回调任务
            super.completed(result, aioSession);
            Runnable task;
            while ((task = threadPoolExecutor.getQueue().poll()) != null) {
                task.run();
            }
            semaphore.release();
            threadLocal.set(null);
            return;
        }
        // 线程资源不足,暂时积压任务
        threadPoolExecutor.execute(() -> ConcurrentReadCompletionHandler.super.completed(result, aioSession));
    }

}