/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.thread;

import java.util.concurrent.*;

/**
 * 类比 {@link java.util.concurrent.Executors}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThreadExecutorBuilder {

    /**
     * 创建一个线程数固定（corePoolSize==maximumPoolSize）的线程池 核心线程会一直存在，不被回收 如果一个核心线程由于异常跪了，会新创建一个线程 无界队列LinkedBlockingQueue
     *
     * @param corePoolSize 要保留在池中的线程数，即使它们是空闲的
     * @param prefix       线程名前缀
     */
    public static Executor newFixedFastThread(final int corePoolSize, final String prefix) {
        return new ThreadPoolExecutor(corePoolSize, corePoolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new NamedThreadFactory(prefix));
    }

    /**
     * corePoolSize==0 maximumPoolSize==Integer.MAX_VALUE 队列：SynchronousQueue 创建一个线程池：当池中的线程都处于忙碌状态时，会立即新建一个线程来处理新来的任务
     * 这种池将会在执行许多耗时短的异步任务的时候提高程序的性能 60秒钟内没有使用的线程将会被中止，并且从线程池中移除，因此几乎不必担心耗费资源
     *
     * @param prefix 线程名前缀
     */
    public static Executor newCachedFastThread(final String prefix) {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new NamedThreadFactory(prefix));
    }

    /**
     * 用给定的初始参数创建一个新的Executor
     *
     * @param corePoolSize    要保留在池中的线程数，即使它们是空闲的
     * @param maximumPoolSize 池中允许的最大线程数
     * @param keepAliveTime   当线程数大于核心时，这是多余空闲线程在终止前等待新任务的最大时间。
     * @param unit            keepAliveTime参数的时间单位
     * @param workQueue       任务执行之前用于保存任务的队列。此队列将仅保存由execute方法提交的可运行任务。
     * @param prefix          线程名前缀
     * @param handler         由于达到线程边界和队列容量而阻塞执行时使用的处理程序
     * @return {@link Executor}
     */
    public static Executor newLimitedFastThread(final int corePoolSize, final int maximumPoolSize,
            final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue, final String prefix,
            final RejectedExecutionHandler handler) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                new NamedThreadFactory(prefix), handler);
    }

}