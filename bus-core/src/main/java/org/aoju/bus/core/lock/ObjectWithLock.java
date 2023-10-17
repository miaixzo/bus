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

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * Object对象读写锁
 *
 * @param <T> 所涉及对象的类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class ObjectWithLock<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 读写锁
     */
    private final ReentrantReadWriteLock lock;

    /**
     * 对象信息
     */
    private T object;

    /**
     * 构造对象
     *
     * @param object 对象信息
     */
    public ObjectWithLock(T object) {
        this(object, new ReentrantReadWriteLock());
    }

    /**
     * 构造对象
     *
     * @param object 对象信息
     * @param lock   读写锁
     */
    public ObjectWithLock(T object, ReentrantReadWriteLock lock) {
        super();
        this.object = object;
        this.lock = lock;
    }

    /**
     * 获取读写锁
     *
     * @return 读写锁
     */
    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    /**
     * 获取写锁
     *
     * @return 写锁信息
     */
    public WriteLock writeLock() {
        return lock.writeLock();
    }

    /**
     * 获取读锁
     *
     * @return 读锁信息
     */
    public ReadLock readLock() {
        return lock.readLock();
    }

    /**
     * 获取对象
     *
     * @return 对象
     */
    public T getObject() {
        return object;
    }

    /**
     * 设置对象
     *
     * @param object 对象信息
     */
    public void setObject(T object) {
        this.object = object;
    }

    /**
     * 操作对象，带上读锁
     *
     * @param readLockHandler 拦截锁
     */
    public void read(LockHandler<T> readLockHandler) {
        ReadLock readLock = lock.readLock();
        readLock.lock();
        try {
            readLockHandler.read(object);
        } catch (Throwable e) {
            Console.error(e.getMessage(), e);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 操作对象，带上写锁
     *
     * @param writeLockHandler 拦截锁
     */
    public void write(LockHandler<T> writeLockHandler) {
        WriteLock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            writeLockHandler.write(object);
        } catch (Throwable e) {
            Console.error(e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }
    }

}
