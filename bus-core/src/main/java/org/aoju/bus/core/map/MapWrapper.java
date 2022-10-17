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

import org.aoju.bus.core.lang.function.XWrapper;
import org.aoju.bus.core.toolkit.ObjectKit;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Map包装类,通过包装一个已有Map实现特定功能 例如自定义Key的规则或Value规则
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapWrapper<K, V> implements Map<K, V>, Iterable<Map.Entry<K, V>>, XWrapper<Map<K, V>>, Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认增长因子
     */
    protected static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /**
     * 默认初始大小
     */
    protected static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

    private Map<K, V> raw;

    /**
     * 构造
     *
     * @param raw 被包装的Map
     */
    public MapWrapper(Map<K, V> raw) {
        this.raw = raw;
    }

    /**
     * 构造
     * 通过传入一个Map从而确定Map的类型，子类需创建一个空的Map，而非传入一个已有Map，否则值可能会被修改
     *
     * @param mapFactory 空Map创建工厂
     */
    public MapWrapper(Supplier<Map<K, V>> mapFactory) {
        this(mapFactory.get());
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
    public Collection<V> values() {
        return raw.values();
    }

    @Override
    public Set<K> keySet() {
        return raw.keySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return raw.entrySet();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return this.entrySet().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        MapWrapper<?, ?> that = (MapWrapper<?, ?>) o;
        return Objects.equals(raw, that.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw);
    }

    @Override
    public String toString() {
        return raw.toString();
    }


    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        raw.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        raw.replaceAll(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return raw.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return raw.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return raw.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        return raw.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return raw.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return raw.getOrDefault(key, defaultValue);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return raw.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return raw.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return raw.merge(key, value, remappingFunction);
    }

    @Override
    public MapWrapper<K, V> clone() throws CloneNotSupportedException {
        final MapWrapper<K, V> clone = (MapWrapper<K, V>) super.clone();
        clone.raw = ObjectKit.clone(raw);
        return clone;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.raw);
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        raw = (Map<K, V>) in.readObject();
    }

}
