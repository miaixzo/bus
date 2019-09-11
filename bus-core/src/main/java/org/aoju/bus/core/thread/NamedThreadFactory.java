/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.core.utils.ThreadUtils;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程创建工厂类，此工厂可选配置：
 *
 * <pre>
 * 1. 自定义线程命名前缀
 * 2. 自定义是否守护线程
 * </pre>
 *
 * @author Kimi Liu
 * @version 3.2.6
 * @since JDK 1.8
 */
public class NamedThreadFactory implements ThreadFactory {

    /**
     * 命名前缀
     */
    private final String prefix;
    /**
     * 线程组
     */
    private final ThreadGroup group;
    /**
     * 线程组
     */
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    /**
     * 是否守护线程
     */
    private final boolean isDeamon;
    /**
     * 无法捕获的异常统一处理
     */
    private final UncaughtExceptionHandler handler;

    /**
     * 构造
     *
     * @param prefix   线程名前缀
     * @param isDeamon 是否守护线程
     */
    public NamedThreadFactory(String prefix, boolean isDeamon) {
        this(prefix, null, isDeamon);
    }

    /**
     * 构造
     *
     * @param prefix      线程名前缀
     * @param threadGroup 线程组，可以为null
     * @param isDeamon    是否守护线程
     */
    public NamedThreadFactory(String prefix, ThreadGroup threadGroup, boolean isDeamon) {
        this(prefix, threadGroup, isDeamon, null);
    }

    /**
     * 构造
     *
     * @param prefix      线程名前缀
     * @param threadGroup 线程组，可以为null
     * @param isDeamon    是否守护线程
     * @param handler     未捕获异常处理
     */
    public NamedThreadFactory(String prefix, ThreadGroup threadGroup, boolean isDeamon, UncaughtExceptionHandler handler) {
        this.prefix = StringUtils.isBlank(prefix) ? "Thread" : prefix;
        if (null == threadGroup) {
            threadGroup = ThreadUtils.currentThreadGroup();
        }
        this.group = threadGroup;
        this.isDeamon = isDeamon;
        this.handler = handler;
    }

    @Override
    public Thread newThread(Runnable r) {
        final Thread t = new Thread(this.group, r, StringUtils.format("{}{}", prefix, threadNumber.getAndIncrement()));

        //守护线程
        if (false == t.isDaemon()) {
            if (isDeamon) {
                // 原线程为非守护则设置为守护
                t.setDaemon(true);
            }
        } else if (false == isDeamon) {
            // 原线程为守护则还原为非守护
            t.setDaemon(false);
        }
        //异常处理
        if (null != this.handler) {
            t.setUncaughtExceptionHandler(handler);
        }
        //优先级
        if (Thread.NORM_PRIORITY != t.getPriority()) {
            // 标准优先级
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

}
