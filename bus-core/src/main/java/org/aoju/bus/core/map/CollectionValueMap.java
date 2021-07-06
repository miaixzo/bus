/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.map;

import org.aoju.bus.core.lang.Func;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 值作为集合的Map实现,通过调用putValue可以在相同key时加入多个值,多个值用集合表示
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 6.2.3
 * @since JDK 1.8+
 */
public class CollectionValueMap<K, V> extends AbsCollValueMap<K, V, Collection<V>> {

    private final Func.Func0<Collection<V>> collectionCreateFunc;

    /**
     * 构造
     */
    public CollectionValueMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public CollectionValueMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造
     *
     * @param m Map
     */
    public CollectionValueMap(Map<? extends K, ? extends Collection<V>> m) {
        this(DEFAULT_LOAD_FACTOR, m);
    }

    /**
     * 构造
     *
     * @param loadFactor 加载因子
     * @param m          Map
     */
    public CollectionValueMap(float loadFactor, Map<? extends K, ? extends Collection<V>> m) {
        this(loadFactor, m, ArrayList::new);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子
     */
    public CollectionValueMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, ArrayList::new);
    }

    /**
     * 构造
     *
     * @param loadFactor           加载因子
     * @param m                    Map
     * @param collectionCreateFunc Map中值的集合创建函数
     */
    public CollectionValueMap(float loadFactor, Map<? extends K, ? extends Collection<V>> m, Func.Func0<Collection<V>> collectionCreateFunc) {
        this(m.size(), loadFactor, collectionCreateFunc);
        this.putAll(m);
    }

    /**
     * 构造
     *
     * @param initialCapacity      初始大小
     * @param loadFactor           加载因子
     * @param collectionCreateFunc Map中值的集合创建函数
     */
    public CollectionValueMap(int initialCapacity, float loadFactor, Func.Func0<Collection<V>> collectionCreateFunc) {
        super(new HashMap<>(initialCapacity, loadFactor));
        this.collectionCreateFunc = collectionCreateFunc;
    }

    @Override
    protected Collection<V> createCollection() {
        return collectionCreateFunc.callWithRuntimeException();
    }

}
