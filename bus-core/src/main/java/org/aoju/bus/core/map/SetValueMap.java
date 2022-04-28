/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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

import java.util.*;

/**
 * 值作为集合Set(LinkedHashSet)的Map实现,通过调用putValue可以在相同key时加入多个值,多个值用集合表示
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class SetValueMap<K, V> extends AbstractCollValueMap<K, V, Set<V>> {

    /**
     * 构造
     */
    public SetValueMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     */
    public SetValueMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 构造
     *
     * @param m Map
     */
    public SetValueMap(Map<? extends K, ? extends Collection<V>> m) {
        this(DEFAULT_LOAD_FACTOR, m);
    }

    /**
     * 构造
     *
     * @param loadFactor 加载因子
     * @param m          Map
     */
    public SetValueMap(float loadFactor, Map<? extends K, ? extends Collection<V>> m) {
        this(m.size(), loadFactor);
        this.putAllValues(m);
    }

    /**
     * 构造
     *
     * @param initialCapacity 初始大小
     * @param loadFactor      加载因子
     */
    public SetValueMap(int initialCapacity, float loadFactor) {
        super(new HashMap<>(initialCapacity, loadFactor));
    }

    @Override
    protected Set<V> createCollection() {
        return new LinkedHashSet<>(DEFAULT_COLLECTION_INITIAL_CAPACITY);
    }

}
