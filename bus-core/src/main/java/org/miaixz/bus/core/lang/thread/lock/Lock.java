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
package org.miaixz.bus.core.lang.thread.lock;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReadWriteLock;
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

    // region ----- SegmentLock
    /**
     * 创建分段锁（强引用），使用 ReentrantLock
     *
     * @param segments 分段数量，必须大于 0
     * @return 分段锁实例
     */
    public static SegmentLock<java.util.concurrent.locks.Lock> createSegmentLock(final int segments) {
        return SegmentLock.lock(segments);
    }

    /**
     * 创建分段读写锁（强引用），使用 ReentrantReadWriteLock
     *
     * @param segments 分段数量，必须大于 0
     * @return 分段读写锁实例
     */
    public static SegmentLock<ReadWriteLock> createSegmentReadWriteLock(final int segments) {
        return SegmentLock.readWriteLock(segments);
    }

    /**
     * 创建分段信号量（强引用）
     *
     * @param segments 分段数量，必须大于 0
     * @param permits  每个信号量的许可数
     * @return 分段信号量实例
     */
    public static SegmentLock<Semaphore> createSegmentSemaphore(final int segments, final int permits) {
        return SegmentLock.semaphore(segments, permits);
    }

    /**
     * 创建弱引用分段锁，使用 ReentrantLock，懒加载
     *
     * @param segments 分段数量，必须大于 0
     * @return 弱引用分段锁实例
     */
    public static SegmentLock<java.util.concurrent.locks.Lock> createLazySegmentLock(final int segments) {
        return SegmentLock.lazyWeakLock(segments);
    }

    /**
     * 根据 key 获取分段锁（强引用）
     *
     * @param segments 分段数量，必须大于 0
     * @param key      用于映射分段的 key
     * @return 对应的 Lock 实例
     */
    public static java.util.concurrent.locks.Lock getSegmentLock(final int segments, final Object key) {
        return SegmentLock.lock(segments).get(key);
    }

    /**
     * 根据 key 获取分段读锁（强引用）
     *
     * @param segments 分段数量，必须大于 0
     * @param key      用于映射分段的 key
     * @return 对应的读锁实例
     */
    public static java.util.concurrent.locks.Lock getSegmentReadLock(final int segments, final Object key) {
        return SegmentLock.readWriteLock(segments).get(key).readLock();
    }

    /**
     * 根据 key 获取分段写锁（强引用）
     *
     * @param segments 分段数量，必须大于 0
     * @param key      用于映射分段的 key
     * @return 对应的写锁实例
     */
    public static java.util.concurrent.locks.Lock getSegmentWriteLock(final int segments, final Object key) {
        return SegmentLock.readWriteLock(segments).get(key).writeLock();
    }

    /**
     * 根据 key 获取分段信号量（强引用）
     *
     * @param segments 分段数量，必须大于 0
     * @param permits  每个信号量的许可数
     * @param key      用于映射分段的 key
     * @return 对应的 Semaphore 实例
     */
    public static Semaphore getSegmentSemaphore(final int segments, final int permits, final Object key) {
        return SegmentLock.semaphore(segments, permits).get(key);
    }

    /**
     * 根据 key 获取弱引用分段锁，懒加载
     *
     * @param segments 分段数量，必须大于 0
     * @param key      用于映射分段的 key
     * @return 对应的 Lock 实例
     */
    public static java.util.concurrent.locks.Lock getLazySegmentLock(final int segments, final Object key) {
        return SegmentLock.lazyWeakLock(segments).get(key);
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
