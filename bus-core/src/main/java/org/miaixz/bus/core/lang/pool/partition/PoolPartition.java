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
package org.miaixz.bus.core.lang.pool.partition;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.pool.ObjectFactory;
import org.miaixz.bus.core.lang.pool.ObjectPool;
import org.miaixz.bus.core.lang.pool.PoolConfig;
import org.miaixz.bus.core.lang.pool.Poolable;

/**
 * 对象池分区 一个分区实际为一个小的对象池，持有一个阻塞队列。 初始化时创建{@link PoolConfig#getMinSize()}个对象作为初始池对象。
 *
 * 当借出对象时，从队列头部取出并验证，验证通过后使用，验证不通过直接调用{@link #free(Object)} 销毁并重新获取， 当池中对象都被借出（空了），创建新的对象并入队列，直到队列满为止，当满时等待归还，超时则报错。
 *
 * @param <T> 对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class PoolPartition<T> implements ObjectPool<T> {

    private static final long serialVersionUID = -1L;

    private final PoolConfig config;
    private final ObjectFactory<T> objectFactory;
    private BlockingQueue<Poolable<T>> queue;
    /**
     * 记录对象总数（包括借出对象）
     */
    private int total;

    /**
     * 构造
     *
     * @param config        池配置
     * @param queue         阻塞队列类型
     * @param objectFactory 对象工厂，用于管理对象创建、检查和销毁
     */
    public PoolPartition(final PoolConfig config, final BlockingQueue<Poolable<T>> queue,
            final ObjectFactory<T> objectFactory) {
        this.config = config;
        this.queue = queue;
        this.objectFactory = objectFactory;

        // 初始化，按照配置最小大小创建对象
        doIncrease(config.getMinSize());
    }

    @Override
    public T borrowObject() {
        // 非阻塞获取
        Poolable<T> poolable = this.queue.poll();
        if (null != poolable) {
            final T object = poolable.getRaw();
            // 检查对象是否可用
            if (this.objectFactory.validate(object)) {
                // 检查是否超过最长空闲时间
                final long maxIdle = this.config.getMaxIdle();
                if (maxIdle <= 0 || poolable.getIdle() <= maxIdle) {
                    return object;
                }
            }

            // 对象不可用或超过最长空闲期，销毁之
            free(object);
            // 继续借，而不扩容
            return borrowObject();
        }

        // 池中无对象，扩容
        if (increase(1) <= 0) {
            // 池分区已满，只能等待是否有返还的对象
            poolable = waitingPoll();
            if (null == poolable) {
                // 池空间达到最大值，但是无可用对象
                throw new InternalException("Pool exhausted!");
            }
        }

        // 扩容成功，继续借对象
        // 如果线程1扩容，但是被线程2借走，则继续递归扩容获取对象，直到获取到或全部借走为止
        return borrowObject();
    }

    /**
     * 归还对象
     *
     * @param object 归还的对象
     * @return this
     */
    @Override
    public PoolPartition<T> returnObject(final T object) {
        // 检查对象可用性
        if (this.objectFactory.validate(object)) {
            try {
                this.queue.put(wrapPoolable(object));
            } catch (final InterruptedException e) {
                throw new InternalException(e);
            }
        } else {
            // 对象不可用
            free(object);
        }

        return this;
    }

    /**
     * 扩容并填充对象池队列 如果传入的扩容大小大于可用大小（即扩容大小加现有大小大于最大大小，则实际扩容到最大）
     *
     * @param increaseSize 扩容大小
     * @return 实际扩容大小，0表示已经达到最大，未成功扩容
     */
    public synchronized int increase(final int increaseSize) {
        return doIncrease(increaseSize);
    }

    /**
     * 销毁对象，注意此方法操作的对象必须在队列外
     *
     * @param object 被销毁的对象
     * @return this
     */
    @Override
    public synchronized PoolPartition<T> free(final T object) {
        objectFactory.destroy(object);
        total--;
        return this;
    }

    @Override
    public int getTotal() {
        return this.total;
    }

    @Override
    public int getIdleCount() {
        return this.queue.size();
    }

    @Override
    public int getActiveCount() {
        return getTotal() - getIdleCount();
    }

    @Override
    synchronized public void close() throws IOException {
        this.queue.forEach((poolable) -> objectFactory.destroy(poolable.getRaw()));
        this.queue.clear();
        this.queue = null;
        this.total = 0;
    }

    /**
     * 创建{@link PartitionPoolable}
     *
     * @return {@link PartitionPoolable}
     */
    protected Poolable<T> createPoolable() {
        final T t = objectFactory.create();
        return null == t ? null : wrapPoolable(t);
    }

    private Poolable<T> wrapPoolable(final T t) {
        if (t instanceof Poolable) {
            return (Poolable<T>) t;
        }
        return new PartitionPoolable<>(t, this);
    }

    /**
     * 非线程安全的扩容并填充对象池队列 如果传入的扩容大小大于可用大小（即扩容大小加现有大小大于最大大小，则实际扩容到最大）
     *
     * @param increaseSize 扩容大小
     * @return 实际扩容大小，0表示已经达到最大，未成功扩容
     */
    private int doIncrease(int increaseSize) {
        final int maxSize = config.getMaxSize();
        if (increaseSize + total > maxSize) {
            increaseSize = maxSize - total;
        }

        try {
            for (int i = 0; i < increaseSize; i++) {
                queue.put(createPoolable());
            }
            total += increaseSize;
        } catch (final InterruptedException e) {
            throw new InternalException(e);
        }
        return increaseSize;
    }

    /**
     * 从队列中取出头部的对象，如果队列为空，则等待 等待的时间取决于{@link PoolConfig#getMaxWait()}，小于等于0时一直等待，否则等待给定毫秒数
     *
     * @return 取出的池对象
     * @throws InternalException 中断异常
     */
    private Poolable<T> waitingPoll() throws InternalException {
        final long maxWait = this.config.getMaxWait();
        try {
            if (maxWait <= 0) {
                return this.queue.take();
            }
            return this.queue.poll(maxWait, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            throw new InternalException(e);
        }
    }

}
