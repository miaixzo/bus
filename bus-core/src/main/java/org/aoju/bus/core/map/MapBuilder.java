/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.builder.Builder;
import org.aoju.bus.core.toolkit.MapKit;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Map创建类
 *
 * @param <K> Key类型
 * @param <V> Value类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapBuilder<K, V> implements Builder<Map<K, V>> {

    private static final long serialVersionUID = 1L;

    private final Map<K, V> map;

    /**
     * 链式Map创建类
     *
     * @param map 要使用的Map实现类
     */
    public MapBuilder(final Map<K, V> map) {
        this.map = map;
    }

    /**
     * 创建Builder，默认HashMap实现
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @return this
     */
    public static <K, V> MapBuilder<K, V> of() {
        return of(false);
    }

    /**
     * 创建Builder
     *
     * @param <K>      Key类型
     * @param <V>      Value类型
     * @param isLinked true创建LinkedHashMap，false创建HashMap
     * @return this
     */
    public static <K, V> MapBuilder<K, V> of(final boolean isLinked) {
        return of(MapKit.newHashMap(isLinked));
    }

    /**
     * 创建Builder
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param map Map实体类
     * @return this
     */
    public static <K, V> MapBuilder<K, V> of(final Map<K, V> map) {
        return new MapBuilder<>(map);
    }

    /**
     * 链式Map创建
     *
     * @param k Key类型
     * @param v Value类型
     * @return this
     */
    public MapBuilder<K, V> put(final K k, final V v) {
        map.put(k, v);
        return this;
    }

    /**
     * 链式Map创建
     *
     * @param condition put条件
     * @param k         Key类型
     * @param v         Value类型
     * @return this
     */
    public MapBuilder<K, V> put(final boolean condition, final K k, final V v) {
        if (condition) {
            put(k, v);
        }
        return this;
    }

    /**
     * 链式Map创建
     *
     * @param condition put条件
     * @param k         Key类型
     * @param supplier  Value类型结果提供方
     * @return this
     */
    public MapBuilder<K, V> put(final boolean condition, final K k, final Supplier<V> supplier) {
        if (condition) {
            put(k, supplier.get());
        }
        return this;
    }

    /**
     * 链式Map创建
     *
     * @param map 合并map
     * @return this
     */
    public MapBuilder<K, V> putAll(final Map<K, V> map) {
        this.map.putAll(map);
        return this;
    }

    /**
     * 清空Map
     *
     * @return this
     */
    public MapBuilder<K, V> clear() {
        this.map.clear();
        return this;
    }

    /**
     * 创建后的map
     *
     * @return 创建后的map
     */
    public Map<K, V> map() {
        return map;
    }

    /**
     * 创建后的map
     *
     * @return 创建后的map
     */
    @Override
    public Map<K, V> build() {
        return map();
    }

    /**
     * 将map转成字符串
     *
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @return 连接字符串
     */
    public String join(final String separator, final String keyValueSeparator) {
        return MapKit.join(this.map, separator, keyValueSeparator);
    }

    /**
     * 将map转成字符串
     *
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @return 连接后的字符串
     */
    public String joinIgnoreNull(final String separator, final String keyValueSeparator) {
        return MapKit.joinIgnoreNull(this.map, separator, keyValueSeparator);
    }

    /**
     * 将map转成字符串
     *
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @return 连接后的字符串
     */
    public String join(final String separator, final String keyValueSeparator, final boolean isIgnoreNull) {
        return MapKit.join(this.map, separator, keyValueSeparator, isIgnoreNull);
    }

}
