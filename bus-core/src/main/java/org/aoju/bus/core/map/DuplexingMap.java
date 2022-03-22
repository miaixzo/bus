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

import org.aoju.bus.core.toolkit.MapKit;

import java.util.Map;

/**
 * 双向Map
 * 互换键值对不检查值是否有重复，如果有则后加入的元素替换先加入的元素
 * 值的顺序在HashMap中不确定，所以谁覆盖谁也不确定，在有序的Map中按照先后顺序覆盖，保留最后的值
 * 它与TableMap的区别是，BiMap维护两个Map实现高效的正向和反向查找
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
public class DuplexingMap<K, V> extends MapWrapper<K, V> {

    private static final long serialVersionUID = 1L;

    private Map<V, K> inverse;

    /**
     * 构造
     *
     * @param raw 被包装的Map
     */
    public DuplexingMap(Map<K, V> raw) {
        super(raw);
    }

    @Override
    public V put(K key, V value) {
        if (null != this.inverse) {
            this.inverse.put(value, key);
        }
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        super.putAll(m);
        if (null != this.inverse) {
            m.forEach((key, value) -> this.inverse.put(value, key));
        }
    }

    @Override
    public V remove(Object key) {
        final V v = super.remove(key);
        if (null != this.inverse && null != v) {
            this.inverse.remove(v);
        }
        return v;
    }

    @Override
    public boolean remove(Object key, Object value) {
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
    public K getKey(V value) {
        return getInverse().get(value);
    }

}
