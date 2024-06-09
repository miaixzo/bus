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

import org.miaixz.bus.core.xyz.RuntimeKit;
import org.miaixz.bus.core.xyz.ThreadKit;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 简单单线程任务调度器
 * 通过自定义Job定时执行任务，通过{@link #getResult()} 可以获取调取时的执行结果
 *
 * @param <T> 结果类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleScheduler<T> {

    private final Job<T> job;

    /**
     * 构造
     *
     * @param job    任务
     * @param period 任务间隔，单位毫秒
     */
    public SimpleScheduler(final Job<T> job, final long period) {
        this(job, 0, period, true);
    }

    /**
     * 构造
     *
     * @param job                   任务
     * @param initialDelay          初始延迟，单位毫秒
     * @param period                执行周期，单位毫秒
     * @param fixedRateOrFixedDelay {@code true}表示fixedRate模式，{@code false}表示fixedDelay模式
     */
    public SimpleScheduler(final Job<T> job, final long initialDelay, final long period, final boolean fixedRateOrFixedDelay) {
        this.job = job;

        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        // 启动定时任务
        ThreadKit.schedule(scheduler, job, initialDelay, period, fixedRateOrFixedDelay);
        // 定时任务在程序结束时结束
        RuntimeKit.addShutdownHook(scheduler::shutdown);
    }

    /**
     * 获取执行任务的阶段性结果
     *
     * @return 结果
     */
    public T getResult() {
        return this.job.getResult();
    }

    /**
     * 带有结果计算的任务
     * 用户实现此接口，通过{@link #run()}实现定时任务的内容，定时任务每次执行或多次执行都可以产生一个结果
     * 这个结果存储在一个volatile的对象属性中，通过{@link #getResult()}来读取这一阶段的结果。
     *
     * @param <T> 结果类型
     */
    public interface Job<T> extends Runnable {
        /**
         * 获取执行结果
         *
         * @return 执行结果
         */
        T getResult();
    }

}
