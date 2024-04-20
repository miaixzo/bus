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
package org.miaixz.bus.core.lock;

import org.miaixz.bus.core.lang.Console;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * Map对象读写锁
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapWithLock<K, V> extends ObjectWithLock<Map<K, V>> {

    private static final long serialVersionUID = 1L;

    /**
     * 构造对象
     */
    public MapWithLock() {
        this(new HashMap<>());
    }

    /**
     * 构造对象
     *
     * @param initCapacity 大小
     */
    public MapWithLock(int initCapacity) {
        this(new HashMap<>(initCapacity));
    }

    /**
     * 构造对象
     *
     * @param map 对象
     */
    public MapWithLock(Map<K, V> map) {
        super(map);
    }

    /**
     * 构造对象
     *
     * @param map  对象
     * @param lock 锁
     */
    public MapWithLock(Map<K, V> map, ReentrantReadWriteLock lock) {
        super(map, lock);
    }

    /**
     * @param key   键信息
     * @param value 值信息
     * @return 对象
     */
    public V put(K key, V value) {
        WriteLock writeLock = this.writeLock();
        writeLock.lock();
        try {
            Map<K, V> map = this.getObject();
            return map.put(key, value);
        } catch (Throwable e) {
            Console.error(e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }
        return null;
    }

    /**
     * 如果key值已经存在，则不会把新value put进去
     * 如果key值不存在，此方法同put(key, value)
     *
     * @param key   键信息
     * @param value 值信息
     * @return 对象
     */
    public V putIfAbsent(K key, V value) {
        WriteLock writeLock = this.writeLock();
        writeLock.lock();
        try {
            Map<K, V> map = this.getObject();
            V oldValue = map.putIfAbsent(key, value);
            if (null == oldValue) {
                return value;
            } else {
                return oldValue;
            }
        } catch (Throwable e) {
            Console.error(e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }
        return null;
    }

    /**
     * 添加对象
     *
     * @param map 对象
     */
    public void putAll(Map<K, V> map) {
        if (null == map || map.isEmpty()) {
            return;
        }

        WriteLock writeLock = this.writeLock();
        writeLock.lock();
        try {
            this.getObject().putAll(map);
        } catch (Throwable e) {
            Console.error(e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 清空
     */
    public void clear() {
        WriteLock writeLock = this.writeLock();
        writeLock.lock();
        try {
            Map<K, V> map = this.getObject();
            map.clear();
        } catch (Throwable e) {
            Console.error(e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 删除对象
     *
     * @param key 键信息
     * @return the true/false
     */
    public V remove(K key) {
        WriteLock writeLock = this.writeLock();
        writeLock.lock();
        try {
            Map<K, V> map = this.getObject();
            return map.remove(key);
        } catch (Throwable e) {
            Console.error(e.getMessage(), e);
        } finally {
            writeLock.unlock();
        }
        return null;
    }

    /**
     * 获取对象
     *
     * @param key 键信息
     * @return 返回对象
     */
    public V get(K key) {
        ReadLock readLock = this.readLock();
        readLock.lock();
        try {
            Map<K, V> map = this.getObject();
            return map.get(key);
        } catch (Throwable e) {
            Console.error(e.getMessage(), e);
        } finally {
            readLock.unlock();
        }
        return null;
    }

    /**
     * 对象大小
     *
     * @return the int
     */
    public int size() {
        ReadLock readLock = this.readLock();
        readLock.lock();
        try {
            Map<K, V> map = this.getObject();
            return map.size();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 复制对象
     *
     * @return 如果没值，则返回null，否则返回一个新map
     */
    public Map<K, V> copy() {
        ReadLock readLock = readLock();
        readLock.lock();
        try {
            if (this.getObject().size() > 0) {
                return new HashMap<>(getObject());
            }
            return null;
        } finally {
            readLock.unlock();
        }
    }

}
