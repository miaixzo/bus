/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.map.multi;

import java.io.Serial;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.map.MapWrapper;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * {@link MultiValueMap}的基本实现
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @see CollectionValueMap
 * @see SetValueMap
 * @see ListValueMap
 * @since Java 17+
 */
public abstract class AbstractCollValueMap<K, V> extends MapWrapper<K, Collection<V>> implements MultiValueMap<K, V> {

    @Serial
    private static final long serialVersionUID = 2852268699398L;

    /**
     * 默认集合初始大小
     */
    protected static final int DEFAULT_COLLECTION_INITIAL_CAPACITY = 3;

    /**
     * 使用{@code mapFactory}创建的集合构造一个多值映射Map集合
     *
     * @param mapFactory 生成集合的工厂方法
     */
    protected AbstractCollValueMap(final Supplier<Map<K, Collection<V>>> mapFactory) {
        super(mapFactory);
    }

    /**
     * 基于{@link HashMap}构造一个多值映射集合
     *
     * @param map 提供初始数据的集合
     */
    protected AbstractCollValueMap(final Map<K, Collection<V>> map) {
        super(new HashMap<>(map));
    }

    /**
     * 基于{@link HashMap}构造一个多值映射集合
     */
    protected AbstractCollValueMap() {
        super(new HashMap<>(16));
    }

    /**
     * 将集合中的全部元素对追加到指定键对应的值集合中，效果等同于：
     * 
     * <pre>{@code
     * coll.forEach(t -> map.putValue(data, t))
     * }</pre>
     *
     * @param key  键
     * @param coll 待添加的值集合
     * @return 是否成功添加
     */
    @Override
    public boolean putAllValues(final K key, final Collection<V> coll) {
        if (ObjectKit.isNull(coll)) {
            return false;
        }
        return super.computeIfAbsent(key, k -> createCollection()).addAll(coll);
    }

    /**
     * 向指定键对应的值集合追加值，效果等同于：
     * 
     * <pre>{@code
     * map.computeIfAbsent(data, k -> new Collection()).add(value)
     * }</pre>
     *
     * @param key   键
     * @param value 值
     * @return 是否成功添加
     */
    @Override
    public boolean putValue(final K key, final V value) {
        return super.computeIfAbsent(key, k -> createCollection()).add(value);
    }

    /**
     * 将值从指定键下的值集合中删除
     *
     * @param key   键
     * @param value 值
     * @return 是否成功删除
     */
    @Override
    public boolean removeValue(final K key, final V value) {
        return Optional.ofNullable(super.get(key)).map(t -> t.remove(value)).orElse(false);
    }

    /**
     * 将一批值从指定键下的值集合中删除
     *
     * @param key    键
     * @param values 值
     * @return 是否成功删除
     */
    @Override
    public boolean removeAllValues(final K key, final Collection<V> values) {
        if (CollKit.isEmpty(values)) {
            return false;
        }
        final Collection<V> coll = get(key);
        return ObjectKit.isNotNull(coll) && coll.removeAll(values);
    }

    /**
     * 根据条件过滤所有值集合中的值，并以新值生成新的值集合，新集合中的值集合类型与当前实例的默认值集合类型保持一致
     *
     * @param filter 判断方法
     * @return 当前实例
     */
    @Override
    public MultiValueMap<K, V> filterAllValues(final BiPredicate<K, V> filter) {
        entrySet().forEach(e -> {
            final K k = e.getKey();
            final Collection<V> coll = e.getValue().stream().filter(v -> filter.test(k, v))
                    .collect(Collectors.toCollection(this::createCollection));
            e.setValue(coll);
        });
        return this;
    }

    /**
     * 根据条件替换所有值集合中的值，并以新值生成新的值集合，新集合中的值集合类型与当前实例的默认值集合类型保持一致
     *
     * @param operate 替换方法
     * @return 当前实例
     */
    @Override
    public MultiValueMap<K, V> replaceAllValues(final BiFunction<K, V, V> operate) {
        entrySet().forEach(e -> {
            final K k = e.getKey();
            final Collection<V> coll = e.getValue().stream().map(v -> operate.apply(k, v))
                    .collect(Collectors.toCollection(this::createCollection));
            e.setValue(coll);
        });
        return this;
    }

    /**
     * 创建集合 此方法用于创建在putValue后追加值所在的集合，子类实现此方法创建不同类型的集合
     *
     * @return {@link Collection}
     */
    protected abstract Collection<V> createCollection();

}
