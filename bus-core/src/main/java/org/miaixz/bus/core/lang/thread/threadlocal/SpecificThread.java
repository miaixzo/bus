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
package org.miaixz.bus.core.lang.thread.threadlocal;

/**
 * 特殊的 {@link Thread}，提供对 {@link FastThreadLocal} 变量的快速访问。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpecificThread extends Thread {

    /**
     * 变量数据结构
     */
    private ThreadLocalMap threadLocalMap;

    /**
     * 构造
     */
    public SpecificThread() {
    }

    /**
     * 构造
     *
     * @param name 线程名称
     */
    public SpecificThread(String name) {
        super(name);
    }

    /**
     * 构造
     *
     * @param r 由新线程实例执行的可运行对象
     */
    public SpecificThread(Runnable r) {
        super(r);
    }

    /**
     * 构造
     *
     * @param r    由新线程实例执行的可运行对象
     * @param name 线程名称
     */
    public SpecificThread(Runnable r, String name) {
        super(r, name);
    }

    /**
     * 构造
     *
     * @param group 线程组
     * @param name  线程名称
     */
    public SpecificThread(ThreadGroup group, String name) {
        super(group, name);
    }

    /**
     * 构造
     *
     * @param group 线程组
     * @param r     由新线程实例执行的可运行对象
     */
    public SpecificThread(ThreadGroup group, Runnable r) {
        super(group, r);
    }

    /**
     * 构造
     *
     * @param group 线程组
     * @param r     由新线程实例执行的可运行对象
     * @param name  线程名称
     */
    public SpecificThread(ThreadGroup group, Runnable r, String name) {
        super(group, r, name);
    }

    /**
     * 构造
     *
     * @param group     线程组
     * @param r         由新线程实例执行的可运行对象
     * @param name      线程名称
     * @param stackSize 线程所需的堆栈大小
     */
    public SpecificThread(ThreadGroup group, Runnable r, String name, long stackSize) {
        super(group, r, name, stackSize);
    }

    /**
     * 返回将线程局部变量绑定到此线程的内部数据结构。
     * 请注意，此方法仅供内部使用，因此随时可能发生变化。
     */
    public final ThreadLocalMap getThreadLocalMap() {
        return threadLocalMap;
    }

    /**
     * 设置将线程局部变量绑定到此线程的内部数据结构。
     * 请注意，此方法仅供内部使用，因此随时可能发生变化。
     *
     * @param threadLocalMap 变量数据结构
     */
    public final void setThreadLocalMap(ThreadLocalMap threadLocalMap) {
        this.threadLocalMap = threadLocalMap;
    }

}