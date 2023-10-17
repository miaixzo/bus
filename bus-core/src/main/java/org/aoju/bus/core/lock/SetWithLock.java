/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.lock;

import org.aoju.bus.core.lang.Console;

import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Set对象读写锁
 *
 * @param <T> 所涉及对象的类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class SetWithLock<T> extends ObjectWithLock<Set<T>> {

    private static final long serialVersionUID = 1L;

    /**
     * 构造对象
     *
     * @param set 对象
     */
    public SetWithLock(Set<T> set) {
        super(set);
    }

    /**
     * @param set  对象
     * @param lock 读写锁
     */
    public SetWithLock(Set<T> set, ReentrantReadWriteLock lock) {
        super(set, lock);
    }

    /**
     * 构造对象
     *
     * @param t 对象
     * @return the true/false
     */
    public boolean add(T t) {
        ReentrantReadWriteLock.WriteLock writeLock = this.writeLock();
        writeLock.lock();
        try {
            Set<T> set = this.getObject();
            return set.add(t);
        } catch (Throwable e) {
            Console.error(e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }
        return false;
    }

    /**
     * 清空对象
     */
    public void clear() {
        ReentrantReadWriteLock.WriteLock writeLock = this.writeLock();
        writeLock.lock();
        try {
            Set<T> set = this.getObject();
            set.clear();
        } catch (Throwable e) {
            Console.error(e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 删除对象
     *
     * @param t 对象
     * @return the true/false
     */
    public boolean remove(T t) {
        ReentrantReadWriteLock.WriteLock writeLock = this.writeLock();
        writeLock.lock();
        try {
            Set<T> set = this.getObject();
            return set.remove(t);
        } catch (Throwable e) {
            Console.error(e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }
        return false;
    }

    /**
     * 对象大小
     *
     * @return the int
     */
    public int size() {
        ReentrantReadWriteLock.ReadLock readLock = this.readLock();
        readLock.lock();
        try {
            Set<T> set = this.getObject();
            return set.size();
        } finally {
            readLock.unlock();
        }
    }

}