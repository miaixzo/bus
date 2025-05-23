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
package org.miaixz.bus.core.center.queue;

import java.io.Serial;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.lang.thread.SimpleScheduler;
import org.miaixz.bus.core.xyz.RuntimeKit;

/**
 * 内存安全的{@link LinkedBlockingQueue}，可以解决OOM问题。 原理是通过Runtime#freeMemory()获取剩余内存，当剩余内存低于指定的阈值时，不再加入。
 *
 * <p>
 * 此类来自于：<a href=
 * "https://github.com/apache/incubator-shenyu/blob/master/shenyu-common/src/main/java/org/apache/shenyu/common/concurrent/MemorySafeLinkedBlockingQueue.java">
 * MemorySafeLinkedBlockingQueue</a>
 * </p>
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class MemorySafeLinkedBlockingQueue<E> extends CheckedLinkedBlockingQueue<E> {

    @Serial
    private static final long serialVersionUID = 2852237105197L;

    /**
     * 构造
     *
     * @param maxFreeMemory 最大剩余内存大小，当实际内存小于这个值时，不再加入元素
     */
    public MemorySafeLinkedBlockingQueue(final long maxFreeMemory) {
        super(new MemoryChecker<>(maxFreeMemory));
    }

    /**
     * 构造
     *
     * @param c             初始集合
     * @param maxFreeMemory 最大剩余内存大小，当实际内存小于这个值时，不再加入元素
     */
    public MemorySafeLinkedBlockingQueue(final Collection<? extends E> c, final long maxFreeMemory) {
        super(c, new MemoryChecker<>(maxFreeMemory));
    }

    /**
     * 获得最大可用内存
     *
     * @return 最大可用内存限制
     */
    public long getMaxFreeMemory() {
        return ((MemoryChecker<E>) this.checker).maxFreeMemory;
    }

    /**
     * 设置最大可用内存
     *
     * @param maxFreeMemory 最大可用内存
     */
    public void setMaxFreeMemory(final int maxFreeMemory) {
        ((MemoryChecker<E>) this.checker).maxFreeMemory = maxFreeMemory;
    }

    /**
     * 根据剩余内存判定的检查器
     *
     * @param <E> 元素类型
     */
    private static class MemoryChecker<E> implements Predicate<E> {

        private long maxFreeMemory;

        private MemoryChecker(final long maxFreeMemory) {
            this.maxFreeMemory = maxFreeMemory;
        }

        @Override
        public boolean test(final E e) {
            Console.log(FreeMemoryCalculator.INSTANCE.getResult());
            return FreeMemoryCalculator.INSTANCE.getResult() > maxFreeMemory;
        }
    }

    /**
     * 获取内存剩余大小的定时任务
     */
    private static class FreeMemoryCalculator extends SimpleScheduler<Long> {
        private static final FreeMemoryCalculator INSTANCE = new FreeMemoryCalculator();

        FreeMemoryCalculator() {
            super(new SimpleScheduler.Job<>() {
                private volatile long maxAvailable = RuntimeKit.getFreeMemory();

                @Override
                public Long getResult() {
                    return this.maxAvailable;
                }

                @Override
                public void run() {
                    this.maxAvailable = RuntimeKit.getFreeMemory();
                }
            }, 50);
        }
    }

}
