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

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * 自定义键和值转换的的Map
 * 继承此类后，通过实现{@link #customKey(Object)}和{@link #customValue(Object)}，按照给定规则加入到map或获取值。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class TransitionMap<K, V> extends MapWrapper<K, V> {

    private static final long serialVersionUID = 1L;

    /**
     * 构造
     * 通过传入一个Map从而确定Map的类型，子类需创建一个空的Map，而非传入一个已有Map，否则值可能会被修改
     *
     * @param mapFactory 空Map创建工厂
     */
    public TransitionMap(Supplier<Map<K, V>> mapFactory) {
        super(mapFactory);
    }

    /**
     * 构造
     * 通过传入一个Map从而确定Map的类型，子类需创建一个空的Map，而非传入一个已有Map，否则值可能会被修改
     *
     * @param emptyMap Map 被包装的Map，必须为空Map，否则自定义key会无效
     */
    public TransitionMap(Map<K, V> emptyMap) {
        super(emptyMap);
    }

    @Override
    public V get(Object key) {
        return super.get(customKey(key));
    }

    @Override
    public V put(K key, V value) {
        return super.put(customKey(key), customValue(value));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(customKey(key));
    }

    @Override
    public V remove(Object key) {
        return super.remove(customKey(key));
    }

    @Override
    public boolean remove(Object key, Object value) {
        return super.remove(customKey(key), customValue(value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return super.replace(customKey(key), customValue(oldValue), customValue(values()));
    }

    @Override
    public V replace(K key, V value) {
        return super.replace(customKey(key), customValue(value));
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return super.getOrDefault(customKey(key), customValue(defaultValue));
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.computeIfPresent(customKey(key), (k, v) -> remappingFunction.apply(customKey(k), customValue(v)));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.compute(customKey(key), (k, v) -> remappingFunction.apply(customKey(k), customValue(v)));
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return super.merge(customKey(key), customValue(value), (v1, v2) -> remappingFunction.apply(customValue(v1), customValue(v2)));
    }

    /**
     * 自定义键
     *
     * @param key KEY
     * @return 自定义KEY
     */
    protected abstract K customKey(Object key);

    /**
     * 自定义值
     *
     * @param value 值
     * @return 自定义值
     */
    protected abstract V customValue(Object value);

}
