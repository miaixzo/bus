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

import java.util.concurrent.Semaphore;

/**
 * 带有信号量控制的{@link Runnable} 接口抽象实现
 * <p>
 * 通过设置信号量，可以限制可以访问某些资源（物理或逻辑的）线程数目。 例如：设置信号量为2，表示最多有两个线程可以同时执行方法逻辑，其余线程等待，直到此线程逻辑执行完毕
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SemaphoreRunnable implements Runnable {

    /**
     * 实际执行的逻辑
     */
    private final Runnable runnable;
    /**
     * 信号量
     */
    private final Semaphore semaphore;

    /**
     * 构造
     *
     * @param runnable  实际执行的线程逻辑
     * @param semaphore 信号量，多个线程必须共享同一信号量
     */
    public SemaphoreRunnable(final Runnable runnable, final Semaphore semaphore) {
        this.runnable = runnable;
        this.semaphore = semaphore;
    }

    /**
     * 获得信号量
     *
     * @return {@link Semaphore}
     */
    public Semaphore getSemaphore() {
        return this.semaphore;
    }

    @Override
    public void run() {
        if (null != this.semaphore) {
            try {
                semaphore.acquire();
                try {
                    this.runnable.run();
                } finally {
                    semaphore.release();
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
