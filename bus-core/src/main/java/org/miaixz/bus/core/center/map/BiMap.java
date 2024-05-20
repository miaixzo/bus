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
package org.miaixz.bus.core.center.map;

import org.miaixz.bus.core.xyz.MapKit;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 双向Map
 * 互换键值对不检查值是否有重复，如果有则后加入的元素替换先加入的元素
 * 值的顺序在HashMap中不确定，所以谁覆盖谁也不确定，在有序的Map中按照先后顺序覆盖，保留最后的值
 * 它与TableMap的区别是，BiMap维护两个Map实现高效的正向和反向查找
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class BiMap<K, V> extends MapWrapper<K, V> {

    private static final long serialVersionUID = -1L;

    private Map<V, K> inverse;

    /**
     * 构造
     *
     * @param raw 被包装的Map
     */
    public BiMap(final Map<K, V> raw) {
        super(raw);
    }

    @Override
    public V put(final K key, final V value) {
        final V oldValue = super.put(key, value);
        if (null != this.inverse) {
            if (null != oldValue) {
                // 如果put的key相同，value不同，需要在inverse中移除旧的关联
                this.inverse.remove(oldValue);
            }
            this.inverse.put(value, key);
        }
        return oldValue;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        super.putAll(m);
        if (null != this.inverse) {
            m.forEach((key, value) -> this.inverse.put(value, key));
        }
    }

    @Override
    public V remove(final Object key) {
        final V v = super.remove(key);
        if (null != this.inverse && null != v) {
            this.inverse.remove(v);
        }
        return v;
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        return super.remove(key, value) && null != this.inverse && this.inverse.remove(value, key);
    }

    @Override
    public void clear() {
        super.clear();
        this.inverse = null;
    }

    /**
     * 获取反向Map
     *
     * @return 反向Map
     */
    public Map<V, K> getInverse() {
        if (null == this.inverse) {
            inverse = MapKit.inverse(getRaw());
        }
        return this.inverse;
    }

    /**
     * 根据值获得键
     *
     * @param value 值
     * @return 键
     */
    public K getKey(final V value) {
        return getInverse().get(value);
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        if (null != this.inverse) {
            this.inverse.putIfAbsent(value, key);
        }
        return super.putIfAbsent(key, value);
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        final V result = super.computeIfAbsent(key, mappingFunction);
        resetInverseMap();
        return result;
    }

    @Override
    public V computeIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        final V result = super.computeIfPresent(key, remappingFunction);
        resetInverseMap();
        return result;
    }

    @Override
    public V compute(final K key, final BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        final V result = super.compute(key, remappingFunction);
        resetInverseMap();
        return result;
    }

    @Override
    public V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        final V result = super.merge(key, value, remappingFunction);
        resetInverseMap();
        return result;
    }

    /**
     * 重置反转的Map，如果反转map为空，则不操作。
     */
    private void resetInverseMap() {
        if (null != this.inverse) {
            inverse = null;
        }
    }

}
