/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.map;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Map包装类,通过包装一个已有Map实现特定功能 例如自定义Key的规则或Value规则
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class MapWrapper<K, V> implements Map<K, V>, Iterable<Map.Entry<K, V>>, Serializable, Cloneable {

    /**
     * 默认增长因子
     */
    protected static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /**
     * 默认初始大小
     */
    protected static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

    private final Map<K, V> raw;

    /**
     * 构造
     *
     * @param raw 被包装的Map
     */
    public MapWrapper(Map<K, V> raw) {
        this.raw = raw;
    }

    /**
     * 获取原始的Map
     *
     * @return Map
     */
    public Map<K, V> getRaw() {
        return this.raw;
    }

    @Override
    public int size() {
        return raw.size();
    }

    @Override
    public boolean isEmpty() {
        return raw.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return raw.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return raw.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return raw.get(key);
    }

    @Override
    public V put(K key, V value) {
        return raw.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return raw.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        raw.putAll(m);
    }

    @Override
    public void clear() {
        raw.clear();
    }

    @Override
    public Set<K> keySet() {
        return raw.keySet();
    }

    @Override
    public Collection<V> values() {
        return raw.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return raw.entrySet();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return this.entrySet().iterator();
    }

}
