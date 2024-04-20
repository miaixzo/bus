/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.thread;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程拒绝策略枚举
 *
 * <p>
 * 如果设置了maxSize, 当总线程数达到上限,
 * 会调用RejectedExecutionHandler进行处理,
 * 此枚举为JDK预定义的几种策略枚举表示
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum RejectPolicy {

    /**
     * 处理程序遭到拒绝将抛出RejectedExecutionException
     */
    ABORT(new ThreadPoolExecutor.AbortPolicy()),
    /**
     * 放弃当前任务
     */
    DISCARD(new ThreadPoolExecutor.DiscardPolicy()),
    /**
     * 如果执行程序尚未关闭,则位于工作队列头部的任务将被删除,然后重试执行程序(如果再次失败,则重复此过程)
     */
    DISCARD_OLDEST(new ThreadPoolExecutor.DiscardOldestPolicy()),
    /**
     * 由主线程来直接执行
     */
    CALLER_RUNS(new ThreadPoolExecutor.CallerRunsPolicy()),
    /**
     * 当任务队列过长时处于阻塞状态，直到添加到队列中，固定并发数去访问，并且不希望丢弃任务时使用此策略
     */
    BLOCK(new BlockPolicy());

    private final RejectedExecutionHandler value;

    RejectPolicy(RejectedExecutionHandler handler) {
        this.value = handler;
    }

    /**
     * 获取RejectedExecutionHandler枚举值
     *
     * @return RejectedExecutionHandler
     */
    public RejectedExecutionHandler getValue() {
        return this.value;
    }

}
