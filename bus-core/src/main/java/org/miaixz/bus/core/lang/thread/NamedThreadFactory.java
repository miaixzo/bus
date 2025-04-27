/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.thread.threadlocal.SpecificThread;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 线程创建工厂类 此工厂可选配置：
 * 
 * <pre>
 * 1. 自定义线程命名前缀
 * 2. 自定义是否守护线程
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NamedThreadFactory implements ThreadFactory {

    /**
     * 线程名称前缀
     */
    protected final String prefix;
    /**
     * 线程组
     */
    protected final ThreadGroup group;
    /**
     * 是否守护线程
     */
    protected final boolean daemon;
    /**
     * 线程的优先级
     */
    protected final int priority;
    /**
     * 线程池中的线程ID
     */
    protected final AtomicInteger nextId = new AtomicInteger(1);
    /**
     * 无法捕获的异常统一处理
     */
    protected final UncaughtExceptionHandler handler;

    /**
     * 构造
     */
    public NamedThreadFactory() {
        this(null, null, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(Class<?> clazz) {
        this(clazz, null, Thread.NORM_PRIORITY);
    }

    /**
     * 构造
     *
     * @param prefix 线程名前缀
     */
    public NamedThreadFactory(String prefix) {
        this(null, prefix, Thread.NORM_PRIORITY);
    }

    /**
     * 构造
     *
     * @param prefix   线程名前缀
     * @param priority 优先级
     */
    public NamedThreadFactory(final Class<?> clazz, String prefix, int priority) {
        this(clazz, prefix, true, priority);
    }

    /**
     * 构造
     *
     * @param prefix 线程名前缀
     * @param daemon 是否守护线程
     */
    public NamedThreadFactory(final String prefix, final boolean daemon) {
        this(null, prefix, null, daemon, Thread.NORM_PRIORITY);
    }

    /**
     * 构造
     *
     * @param prefix   线程名前缀
     * @param daemon   是否守护线程
     * @param priority 优先级
     */
    public NamedThreadFactory(final Class<?> clazz, final String prefix, final boolean daemon, int priority) {
        this(clazz, prefix, null, daemon, priority);
    }

    /**
     * 构造
     *
     * @param prefix 线程名前缀
     * @param group  线程组，可以为null
     * @param daemon 是否守护线程
     */
    public NamedThreadFactory(final String prefix, final ThreadGroup group, final boolean daemon) {
        this(null, prefix, group, daemon, Thread.NORM_PRIORITY, null);
    }

    /**
     * 构造
     *
     * @param prefix   线程名前缀
     * @param group    线程组，可以为null
     * @param daemon   是否守护线程
     * @param priority 优先级
     */
    public NamedThreadFactory(final Class<?> clazz, final String prefix, final ThreadGroup group, final boolean daemon,
            int priority) {
        this(clazz, prefix, group, daemon, priority, null);
    }

    /**
     * 构造
     *
     * @param prefix  线程名前缀
     * @param group   线程组，可以为null
     * @param daemon  是否守护线程
     * @param handler 未捕获异常处理
     */
    public NamedThreadFactory(final String prefix, ThreadGroup group, final boolean daemon,
            final UncaughtExceptionHandler handler) {
        this(null, prefix, group, daemon, Thread.NORM_PRIORITY, handler);
    }

    /**
     * 构造
     *
     * @param prefix   线程名前缀
     * @param group    线程组，可以为null
     * @param daemon   是否守护线程
     * @param priority 优先级
     * @param handler  未捕获异常处理
     */
    public NamedThreadFactory(final Class<?> clazz, final String prefix, ThreadGroup group, final boolean daemon,
            int priority, final UncaughtExceptionHandler handler) {
        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException(
                    "priority: " + priority + " (expected: Thread.MIN_PRIORITY <= priority <= Thread.MAX_PRIORITY)");
        }

        if (ObjectKit.isEmpty(clazz) && StringKit.isEmpty(prefix)) {
            // 线程前缀，两者都为空使用默认值
            this.prefix = Symbol.X;
        } else {
            // 线程前缀，默认使用prefix，反之使用clazz
            this.prefix = StringKit.isBlank(prefix) ? ClassKit.getClassName(clazz, true) : prefix;
        }

        if (null == group) {
            // 当前线程组
            group = Thread.currentThread().getThreadGroup();
        }
        this.group = group;
        this.daemon = daemon;
        this.priority = priority;
        this.handler = handler;
    }

    /**
     * 构造一个新的未启动线程来运行给定的可运行对象
     *
     * @param r 由新线程实例执行的可运行对象
     * @return 新新线程信息
     */
    @Override
    public Thread newThread(final Runnable r) {
        final Thread t = this.newThread(r, StringKit.format("{}{}", this.prefix, this.nextId.getAndIncrement()));
        // 守护线程
        if (!t.isDaemon()) {
            if (this.daemon) {
                // 原线程为非守护则设置为守护
                t.setDaemon(true);
            }
        } else if (!this.daemon) {
            // 原线程为守护则还原为非守护
            t.setDaemon(false);
        }
        if (t.getPriority() != this.priority) {
            t.setPriority(this.priority);
        }

        // 异常处理
        if (null != this.handler) {
            t.setUncaughtExceptionHandler(this.handler);
        }
        return t;
    }

    /**
     * 构造一个新的未启动线程来运行给定的可运行对象
     *
     * @param r    由新线程实例执行的可运行对象
     * @param name 线程名称
     * @return 新新线程信息
     */
    protected Thread newThread(Runnable r, String name) {
        return new SpecificThread(this.group, r, name);
    }

}
