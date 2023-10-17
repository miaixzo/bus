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
package org.aoju.bus.limiter.support.lock.jdk;

import org.aoju.bus.logger.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于ConcurrentHashMap和ReentrantLock实现的一个简单的锁组件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkLock extends org.aoju.bus.limiter.support.lock.Lock {

    private String lockName;

    private ConcurrentHashMap<Object, Lock> locks;

    public JdkLock(String lockName, int initialCapacity, float loadFactor, int concurrencyLevel) {
        this.lockName = lockName;
        locks = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    public JdkLock(String lockName) {
        this.lockName = lockName;
        locks = new ConcurrentHashMap<>();
    }

    @Override
    public boolean lock(Object key) {
        // 对于一个良好的资源 竞态条件的不应该频繁产生
        Lock lock = new ReentrantLock();
        Lock oldLock = locks.putIfAbsent(key, lock);
        if (null != oldLock) {
            boolean ret = oldLock.tryLock();
            if (ret) {
                Logger.info("acquire lock on  {}  success", key);
            } else {
                Logger.info("acquire lock on {} fail", key);
            }
            return ret;
        } else {
            boolean ret = lock.tryLock();
            if (ret) {
                Logger.info("acquire lock on  {}  success", key);
            } else {
                Logger.info("acquire lock on {} fail", key);
            }
            return ret;
        }
    }

    @Override
    public void unlock(Object key) {
        Lock lock = locks.remove(key);
        if (null == lock) {
            throw new RuntimeException("未找到该锁！");
        }
        lock.unlock();
    }

    @Override
    public String getLimiterName() {
        return lockName;
    }
}
