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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.cache.provider.*;

/**
 * 缓存工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheKit {

    /**
     * 创建FIFO(first in first out) 先进先出缓存.
     *
     * @param <K>      Key类型
     * @param <V>      Value类型
     * @param capacity 容量
     * @param timeout  过期时长，单位：毫秒
     * @return {@link FIFOCache}
     */
    public static <K, V> FIFOCache<K, V> newFIFOCache(final int capacity, final long timeout) {
        return new FIFOCache<>(capacity, timeout);
    }

    /**
     * 创建FIFO(first in first out) 先进先出缓存.
     *
     * @param <K>      Key类型
     * @param <V>      Value类型
     * @param capacity 容量
     * @return {@link FIFOCache}
     */
    public static <K, V> FIFOCache<K, V> newFIFOCache(final int capacity) {
        return new FIFOCache<>(capacity);
    }

    /**
     * 创建LFU(least frequently used) 最少使用率缓存.
     *
     * @param <K>      Key类型
     * @param <V>      Value类型
     * @param capacity 容量
     * @param timeout  过期时长，单位：毫秒
     * @return {@link LFUCache}
     */
    public static <K, V> LFUCache<K, V> newLFUCache(final int capacity, final long timeout) {
        return new LFUCache<>(capacity, timeout);
    }

    /**
     * 创建LFU(least frequently used) 最少使用率缓存.
     *
     * @param <K>      Key类型
     * @param <V>      Value类型
     * @param capacity 容量
     * @return {@link LFUCache}
     */
    public static <K, V> LFUCache<K, V> newLFUCache(final int capacity) {
        return new LFUCache<>(capacity);
    }

    /**
     * 创建LRU (least recently used)最近最久未使用缓存.
     *
     * @param <K>      Key类型
     * @param <V>      Value类型
     * @param capacity 容量
     * @param timeout  过期时长，单位：毫秒
     * @return {@link LRUCache}
     */
    public static <K, V> LRUCache<K, V> newLRUCache(final int capacity, final long timeout) {
        return new LRUCache<>(capacity, timeout);
    }

    /**
     * 创建LRU (least recently used)最近最久未使用缓存.
     *
     * @param <K>      Key类型
     * @param <V>      Value类型
     * @param capacity 容量
     * @return {@link LRUCache}
     */
    public static <K, V> LRUCache<K, V> newLRUCache(final int capacity) {
        return new LRUCache<>(capacity);
    }

    /**
     * 创建定时缓存，通过定时任务自动清除过期缓存对象
     *
     * @param <K>                Key类型
     * @param <V>                Value类型
     * @param timeout            过期时长，单位：毫秒
     * @param schedulePruneDelay 间隔时长，单位毫秒
     * @return {@link TimedCache}
     */
    public static <K, V> TimedCache<K, V> newTimedCache(final long timeout, final long schedulePruneDelay) {
        final TimedCache<K, V> cache = newTimedCache(timeout);
        return cache.schedulePrune(schedulePruneDelay);
    }

    /**
     * 创建定时缓存.
     *
     * @param <K>     Key类型
     * @param <V>     Value类型
     * @param timeout 过期时长，单位：毫秒
     * @return {@link TimedCache}
     */
    public static <K, V> TimedCache<K, V> newTimedCache(final long timeout) {
        return new TimedCache<>(timeout);
    }

    /**
     * 创建弱引用缓存.
     *
     * @param <K>     Key类型
     * @param <V>     Value类型
     * @param timeout 过期时长，单位：毫秒
     * @return {@link WeakCache}
     */
    public static <K, V> WeakCache<K, V> newWeakCache(final long timeout) {
        return new WeakCache<>(timeout);
    }

    /**
     * 创建无缓存实现.
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @return {@link NoCache}
     */
    public static <K, V> NoCache<K, V> newNoCache() {
        return new NoCache<>();
    }

}
