/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.thread;

/**
 * 带有信号量控制的{@link Runnable} 接口抽象实现
 *
 * <p>
 * 通过设置信号量,可以限制可以访问某些资源（物理或逻辑的）线程数目
 * 例如：设置信号量为2,表示最多有两个线程可以同时执行方法逻辑,
 * 其余线程等待,直到此线程逻辑执行完毕
 * </p>
 *
 * @author Kimi Liu
 * @version 5.8.5
 * @since JDK 1.8+
 */
public class Semaphore implements Runnable {

    /**
     * 实际执行的逻辑
     */
    private Runnable runnable;
    /**
     * 信号量
     */
    private java.util.concurrent.Semaphore semaphore;

    /**
     * 构造
     *
     * @param runnable  实际执行的线程逻辑
     * @param semaphore 信号量,多个线程必须共享同一信号量
     */
    public Semaphore(Runnable runnable, java.util.concurrent.Semaphore semaphore) {
        this.runnable = runnable;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        if (null != this.semaphore) {
            try {
                semaphore.acquire();
                this.runnable.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
            }
        }
    }

}
