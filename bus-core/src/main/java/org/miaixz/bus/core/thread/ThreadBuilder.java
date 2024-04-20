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

import org.miaixz.bus.core.builder.Builder;
import org.miaixz.bus.core.toolkit.StringKit;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ThreadFactory创建器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThreadBuilder implements Builder<ThreadFactory> {

    /**
     * 用于线程创建的线程工厂类
     */
    private ThreadFactory backingThreadFactory;
    /**
     * 线程名的前缀
     */
    private String namePrefix;
    /**
     * 是否守护线程,默认false
     */
    private Boolean daemon;
    /**
     * 线程优先级
     */
    private Integer priority;
    /**
     * 未捕获异常处理器
     */
    private UncaughtExceptionHandler uncaughtExceptionHandler;

    /**
     * 创建{@link ThreadBuilder}
     *
     * @return {@link ThreadBuilder}
     */
    public static ThreadBuilder of() {
        return new ThreadBuilder();
    }

    /**
     * 构建
     *
     * @param builder {@link ThreadBuilder}
     * @return {@link ThreadFactory}
     */
    private static ThreadFactory build(ThreadBuilder builder) {
        final ThreadFactory backingThreadFactory = (null != builder.backingThreadFactory)//
                ? builder.backingThreadFactory //
                : Executors.defaultThreadFactory();
        final String namePrefix = builder.namePrefix;
        final Boolean daemon = builder.daemon;
        final Integer priority = builder.priority;
        final UncaughtExceptionHandler handler = builder.uncaughtExceptionHandler;
        final AtomicLong count = (null == namePrefix) ? null : new AtomicLong();
        return r -> {
            final Thread thread = backingThreadFactory.newThread(r);
            if (null != namePrefix) {
                thread.setName(namePrefix + count.getAndIncrement());
            }
            if (null != daemon) {
                thread.setDaemon(daemon);
            }
            if (null != priority) {
                thread.setPriority(priority);
            }
            if (null != handler) {
                thread.setUncaughtExceptionHandler(handler);
            }
            return thread;
        };
    }

    /**
     * 设置用于创建基础线程的线程工厂
     *
     * @param backingThreadFactory 用于创建基础线程的线程工厂
     * @return this
     */
    public ThreadBuilder setThreadFactory(ThreadFactory backingThreadFactory) {
        this.backingThreadFactory = backingThreadFactory;
        return this;
    }

    /**
     * 设置线程名前缀,例如设置前缀为Thread-thread-,则线程名为Thread-thread-1之类
     *
     * @param namePrefix 线程名前缀
     * @return this
     */
    public ThreadBuilder setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }

    /**
     * 设置是否守护线程
     *
     * @param daemon 是否守护线程
     * @return this
     */
    public ThreadBuilder setDaemon(boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    /**
     * 设置线程优先级
     *
     * @param priority 优先级
     * @return this
     * @see Thread#MIN_PRIORITY
     * @see Thread#NORM_PRIORITY
     * @see Thread#MAX_PRIORITY
     */
    public ThreadBuilder setPriority(int priority) {
        if (priority < Thread.MIN_PRIORITY) {
            throw new IllegalArgumentException(StringKit.format("Thread priority ({}) must be >= {}", priority, Thread.MIN_PRIORITY));
        }
        if (priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException(StringKit.format("Thread priority ({}) must be <= {}", priority, Thread.MAX_PRIORITY));
        }
        this.priority = priority;
        return this;
    }

    /**
     * 设置未捕获异常的处理方式
     *
     * @param uncaughtExceptionHandler {@link UncaughtExceptionHandler}
     * @return {@link ThreadBuilder}
     */
    public ThreadBuilder setUncaughtExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        return this;
    }

    /**
     * 构建{@link ThreadFactory}
     *
     * @return {@link ThreadFactory}
     */
    @Override
    public ThreadFactory build() {
        return build(this);
    }

}
