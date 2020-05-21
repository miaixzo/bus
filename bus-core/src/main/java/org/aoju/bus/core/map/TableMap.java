/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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

import org.aoju.bus.core.utils.CollUtils;
import org.aoju.bus.core.utils.ObjectUtils;

import java.io.Serializable;
import java.util.*;

/**
 * 无重复键的Map
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 5.9.2
 * @since JDK 1.8+
 */
public class TableMap<K, V> implements Map<K, V>, Iterable<Map.Entry<K, V>>, Serializable {

    private final List<K> keys;
    private final List<V> values;

    /**
     * 构造
     *
     * @param size 初始容量
     */
    public TableMap(int size) {
        this.keys = new ArrayList<>(size);
        this.values = new ArrayList<>(size);
    }

    /**
     * 构造
     *
     * @param keys   键列表
     * @param values 值列表
     */
    public TableMap(K[] keys, V[] values) {
        this.keys = CollUtils.toList(keys);
        this.values = CollUtils.toList(values);
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return CollUtils.isEmpty(keys);
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    @Override
    public V get(Object key) {
        final int index = keys.indexOf(key);
        if (index > -1 && index < values.size()) {
            return values.get(index);
        }
        return null;
    }

    /**
     * 根据value获得对应的key，只返回找到的第一个value对应的key值
     *
     * @param value 值
     * @return 键
     */
    public K getKey(V value) {
        final int index = values.indexOf(value);
        if (index > -1 && index < keys.size()) {
            return keys.get(index);
        }
        return null;
    }

    /**
     * 获取指定key对应的所有值
     *
     * @param key 键
     * @return 值列表
     */
    public List<V> getValues(K key) {
        return CollUtils.getAny(
                this.values,
                CollUtils.indexOfAll(this.keys, (ele) -> ObjectUtils.equal(ele, key))
        );
    }

    /**
     * 获取指定value对应的所有key
     *
     * @param value 值
     * @return 值列表
     */
    public List<K> getKeys(V value) {
        return CollUtils.getAny(
                this.keys,
                CollUtils.indexOfAll(this.values, (ele) -> ObjectUtils.equal(ele, value))
        );
    }

    @Override
    public V put(K key, V value) {
        keys.add(key);
        values.add(value);
        return null;
    }

    @Override
    public V remove(Object key) {
        int index = keys.indexOf(key);
        if (index > -1) {
            keys.remove(index);
            if (index < values.size()) {
                values.remove(index);
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        keys.clear();
        values.clear();
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<>(keys);
    }

    @Override
    public Collection<V> values() {
        return Collections.unmodifiableList(this.values);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        final Set<Map.Entry<K, V>> hashSet = new LinkedHashSet<>();
        for (int i = 0; i < size(); i++) {
            hashSet.add(new Entry<>(keys.get(i), values.get(i)));
        }
        return hashSet;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return new Iterator<Map.Entry<K, V>>() {
            private final Iterator<K> keysIter = keys.iterator();
            private final Iterator<V> valuesIter = values.iterator();

            @Override
            public boolean hasNext() {
                return keysIter.hasNext() && valuesIter.hasNext();
            }

            @Override
            public Map.Entry<K, V> next() {
                return new Entry<>(keysIter.next(), valuesIter.next());
            }

            @Override
            public void remove() {
                keysIter.remove();
                valuesIter.remove();
            }
        };
    }

    @Override
    public String toString() {
        return "TableMap{" + "keys=" + keys + ", values=" + values + '}';
    }

    private static class Entry<K, V> implements Map.Entry<K, V> {

        private final K key;
        private final V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("setValue not supported.");
        }

        @Override
        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                return Objects.equals(key, e.getKey()) &&
                        Objects.equals(value, e.getValue());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

    }

}
