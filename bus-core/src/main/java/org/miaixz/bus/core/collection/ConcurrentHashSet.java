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
package org.miaixz.bus.core.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过{@link ConcurrentHashMap}实现的线程安全
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 持有对象。如果值为此对象表示有数据，否则无数据
     */
    private static final Boolean PRESENT = true;
    private final ConcurrentHashMap<E, Boolean> map;

    /**
     * 触发因子为默认的0.75
     */
    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<>();
    }

    /**
     * 触发因子为默认的0.75
     *
     * @param initialCapacity 初始大小
     */
    public ConcurrentHashSet(int initialCapacity) {
        map = new ConcurrentHashMap<>(initialCapacity);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子。此参数决定数据增长时触发的百分比
     */
    public ConcurrentHashSet(int initialCapacity, float loadFactor) {
        map = new ConcurrentHashMap<>(initialCapacity, loadFactor);
    }

    /**
     * 构造
     *
     * @param initialCapacity  初始大小
     * @param loadFactor       触发因子。此参数决定数据增长时触发的百分比
     * @param concurrencyLevel 线程并发度
     */
    public ConcurrentHashSet(int initialCapacity, float loadFactor, int concurrencyLevel) {
        map = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    /**
     * 从已有集合中构造
     *
     * @param iter {@link Iterable}
     */
    public ConcurrentHashSet(Iterable<E> iter) {
        if (iter instanceof Collection) {
            final Collection<E> collection = (Collection<E>) iter;
            map = new ConcurrentHashMap<>((int) (collection.size() / 0.75f));
            this.addAll(collection);
        } else {
            map = new ConcurrentHashMap<>();
            for (E e : iter) {
                this.add(e);
            }
        }
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean add(E e) {
        return null == map.put(e, PRESENT);
    }

    @Override
    public boolean remove(Object o) {
        return PRESENT.equals(map.remove(o));
    }

    @Override
    public void clear() {
        map.clear();
    }

}