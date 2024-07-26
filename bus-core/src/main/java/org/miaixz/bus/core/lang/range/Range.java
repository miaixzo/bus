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
package org.miaixz.bus.core.lang.range;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.thread.lock.NoLock;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 范围生成器。根据给定的初始值、结束值和步进生成一个步进列表生成器 由于用户自行实现{@link Stepper}来定义步进，因此Range本身无法判定边界（是否达到end），需在step实现边界判定逻辑。
 *
 * <p>
 * 此类使用{@link ReentrantReadWriteLock}保证线程安全
 * </p>
 *
 * @param <T> 生成范围对象的类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class Range<T> implements Iterable<T>, Iterator<T>, Serializable {

    private static final long serialVersionUID = -1L;
    /**
     * 起始对象
     */
    private final T start;
    /**
     * 结束对象
     */
    private final T end;
    /**
     * 步进
     */
    private final Stepper<T> stepper;
    /**
     * 是否包含第一个元素
     */
    private final boolean includeStart;
    /**
     * 是否包含最后一个元素
     */
    private final boolean includeEnd;
    /**
     * 锁保证线程安全
     */
    private Lock lock = new ReentrantLock();
    /**
     * 下一个对象
     */
    private T next;
    /**
     * 索引
     */
    private int index = 0;

    /**
     * 构造
     *
     * @param start   起始对象（包括）
     * @param stepper 步进
     */
    public Range(final T start, final Stepper<T> stepper) {
        this(start, null, stepper);
    }

    /**
     * 构造
     *
     * @param start   起始对象（包含）
     * @param end     结束对象（包含）
     * @param stepper 步进
     */
    public Range(final T start, final T end, final Stepper<T> stepper) {
        this(start, end, stepper, true, true);
    }

    /**
     * 构造
     *
     * @param start          起始对象
     * @param end            结束对象
     * @param stepper        步进
     * @param isIncludeStart 是否包含第一个元素
     * @param isIncludeEnd   是否包含最后一个元素
     */
    public Range(final T start, final T end, final Stepper<T> stepper, final boolean isIncludeStart,
            final boolean isIncludeEnd) {
        Assert.notNull(start, "First element must be not null!");
        this.start = start;
        this.end = end;
        this.stepper = stepper;
        this.next = safeStep(this.start);
        this.includeStart = isIncludeStart;
        this.includeEnd = isIncludeEnd;
    }

    /**
     * 禁用锁，调用此方法后不再使用锁保护
     *
     * @return this
     */
    public Range<T> disableLock() {
        this.lock = new NoLock();
        return this;
    }

    @Override
    public boolean hasNext() {
        lock.lock();
        try {
            if (0 == this.index && this.includeStart) {
                return true;
            }
            if (null == this.next) {
                return false;
            } else if (!includeEnd && this.next.equals(this.end)) {
                return false;
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public T next() {
        lock.lock();
        try {
            if (!this.hasNext()) {
                throw new NoSuchElementException("Has no next range!");
            }
            return nextUncheck();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取下一个元素，并将下下个元素准备好
     */
    private T nextUncheck() {
        final T current;
        if (0 == this.index) {
            current = start;
            if (!this.includeStart) {
                // 获取下一组元素
                index++;
                return nextUncheck();
            }
        } else {
            current = next;
            this.next = safeStep(this.next);
        }

        index++;
        return current;
    }

    /**
     * 不抛异常的获取下一步进的元素，如果获取失败返回{@code null}
     *
     * @param base 上一个元素
     * @return 下一步进
     */
    private T safeStep(final T base) {
        final int index = this.index;
        T next = null;
        try {
            next = stepper.step(base, this.end, index);
        } catch (final Exception e) {
            // ignore
        }

        return next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Can not remove ranged element!");
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }

    /**
     * 重置Range
     *
     * @return this
     */
    public Range<T> reset() {
        lock.lock();
        try {
            this.index = 0;
            this.next = safeStep(this.start);
        } finally {
            lock.unlock();
        }
        return this;
    }

}
