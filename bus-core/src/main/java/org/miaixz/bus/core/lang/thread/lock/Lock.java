/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.lang.thread.lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * 锁相关类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Lock {

    private static final NoLock NO_LOCK = new NoLock();

    /**
     * 创建{@link StampedLock}锁
     *
     * @return {@link StampedLock}锁
     */
    public static StampedLock createStampLock() {
        return new StampedLock();
    }

    /**
     * 创建{@link ReentrantReadWriteLock}锁
     *
     * @param fair 是否公平锁
     * @return {@link ReentrantReadWriteLock}锁
     */
    public static ReentrantReadWriteLock createReadWriteLock(final boolean fair) {
        return new ReentrantReadWriteLock(fair);
    }

    /**
     * 获取单例的无锁对象
     *
     * @return {@link NoLock}
     */
    public static NoLock getNoLock() {
        return NO_LOCK;
    }

}
