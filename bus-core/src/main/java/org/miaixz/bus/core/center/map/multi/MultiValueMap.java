/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CollKit;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * 一个键对应多个值的集合{@link Map}实现，提供针对键对应的值集合中的元素而非值集合本身的一些快捷操作，
 * 本身可作为一个值为{@link Collection}类型的{@link Map}使用。
 *
 * <p>值集合类型</p>
 * <p>
 * 值集合的类型由接口的实现类自行维护，当通过{@link MultiValueMap}定义的方法进行增删改操作时，
 * 实现类应保证通过通过实例方法获得的集合类型都一致。但是若用户直接通过{@link Map}定义的方法进行增删改操作时，
 * 实例无法保证通过实例方法获得的集合类型都一致。因此，若无必要则更推荐通过{@link MultiValueMap}定义的方法进行操作。
 *
 * <p>对值集合的修改</p>
 * <p>
 * 当通过实例方法获得值集合时，若该集合允许修改，则对值集合的修改将会影响到其所属的{@link MultiValueMap}实例，反之亦然。
 * 因此当同时遍历当前实例或者值集合时，若存在写操作，则需要注意可能引发的{@link ConcurrentModificationException}。
 * </P>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @see AbstractCollValueMap
 * @see CollectionValueMap
 * @see ListValueMap
 * @see SetValueMap
 * @since Java 17+
 */
public interface MultiValueMap<K, V> extends Map<K, Collection<V>> {

    /**
     * 更新键对应的值集合
     * 注意：该操作将移除键对应的旧值集合，若仅需向值集合追加应值，则应使用{@link #putAllValues(Object, Collection)}
     *
     * @param key   键
     * @param value 键对应的新值集合
     * @return 旧值集合
     */
    @Override
    Collection<V> put(K key, Collection<V> value);

    /**
     * 更新全部键的值集合
     * 注意：该操作将移除键对应的旧值集合，若仅需向值集合追加应值，则应使用{@link #putAllValues(Object, Collection)}
     *
     * @param map 需要更新的键值对集合
     */
    @Override
    void putAll(Map<? extends K, ? extends Collection<V>> map);

    /**
     * 将集合中的全部键值对追加到当前实例中，效果等同于：
     * <pre>{@code
     * for (Entry<K, Collection<V>> entry : m.entrySet()) {
     * 	K data = entry.getKey();
     * 	Collection<V> coll = entry.getValues();
     * 	for (V val : coll) {
     * 		map.putValue(data, val)
     *    }
     * }
     * }</pre>
     *
     * @param m 待添加的集合
     */
    default void putAllValues(final Map<? extends K, ? extends Collection<V>> m) {
        if (CollKit.isNotEmpty(m)) {
            m.forEach(this::putAllValues);
        }
    }

    /**
     * 将集合中的全部元素对追加到指定键对应的值集合中，效果等同于：
     * <pre>{@code
     * 	for (V val : coll) {
     * 		map.putValue(data, val)
     *    }
     * }</pre>
     *
     * @param key  键
     * @param coll 待添加的值集合
     * @return 是否成功添加
     */
    boolean putAllValues(K key, final Collection<V> coll);

    /**
     * 将数组中的全部元素追加到指定的值集合中，效果等同于：
     * <pre>{@code
     * 	for (V val : values) {
     * 		map.putValue(data, val)
     *    }
     * }</pre>
     *
     * @param key    键
     * @param values 待添加的值
     * @return boolean
     */
    default boolean putValues(final K key, final V... values) {
        return ArrayKit.isNotEmpty(values) && putAllValues(key, Arrays.asList(values));
    }

    /**
     * 向指定键对应的值集合追加值，效果等同于：
     * <pre>{@code
     * Collection<V> coll = map.get(data);
     * if(null == coll) {
     * 	coll.add(value);
     * 	map.put(coll);
     * } else {
     * 	coll.add(value);
     * }
     * }</pre>
     *
     * @param key   键
     * @param value 值
     * @return 是否成功添加
     */
    boolean putValue(final K key, final V value);

    /**
     * 将值从指定键下的值集合中删除
     *
     * @param key   键
     * @param value 值
     * @return 是否成功删除
     */
    boolean removeValue(final K key, final V value);

    /**
     * 将一批值从指定键下的值集合中删除
     *
     * @param key    键
     * @param values 值数组
     * @return 是否成功删除
     */
    default boolean removeValues(final K key, final V... values) {
        return ArrayKit.isNotEmpty(values) && removeAllValues(key, Arrays.asList(values));
    }

    /**
     * 将一批值从指定键下的值集合中删除
     *
     * @param key    键
     * @param values 值集合
     * @return 是否成功删除
     */
    boolean removeAllValues(final K key, final Collection<V> values);

    /**
     * 根据条件过滤所有值集合中的值，并以新值生成新的值集合，新集合中的值集合类型与当前实例的默认值集合类型保持一致
     *
     * @param filter 判断方法
     * @return 当前实例
     */
    default MultiValueMap<K, V> filterAllValues(final Predicate<V> filter) {
        return filterAllValues((k, v) -> filter.test(v));
    }

    /**
     * 根据条件过滤所有值集合中的值，并以新值生成新的值集合，新集合中的值集合类型与当前实例的默认值集合类型保持一致
     *
     * @param filter 判断方法
     * @return 当前实例
     */
    MultiValueMap<K, V> filterAllValues(BiPredicate<K, V> filter);

    /**
     * 根据条件替换所有值集合中的值，并以新值生成新的值集合，新集合中的值集合类型与当前实例的默认值集合类型保持一致
     *
     * @param operate 替换方法
     * @return 当前实例
     */
    default MultiValueMap<K, V> replaceAllValues(final UnaryOperator<V> operate) {
        return replaceAllValues((k, v) -> operate.apply(v));
    }

    /**
     * 根据条件替换所有值集合中的值，并以新值生成新的值集合，新集合中的值集合类型与当前实例的默认值集合类型保持一致
     *
     * @param operate 替换方法
     * @return 当前实例
     */
    MultiValueMap<K, V> replaceAllValues(BiFunction<K, V, V> operate);

    /**
     * 获取指定序号的值，若值不存在，返回{@code null}
     *
     * @param key   键
     * @param index 第几个值的索引，越界返回null
     * @return 值或null
     */
    default V getValue(final K key, final int index) {
        final Collection<V> collection = get(key);
        return CollKit.get(collection, index);
    }

    /**
     * 获取键对应的值，若值不存在，则返回{@link Collections#emptyList()}。效果等同于：
     * <pre>{@code
     * map.getOrDefault(data, Collections.emptyList())
     * }</pre>
     *
     * @param key 键
     * @return 值集合
     */
    default Collection<V> getValues(final K key) {
        return getOrDefault(key, Collections.emptyList());
    }

    /**
     * 获取键对应值的数量，若键对应的值不存在，则返回{@code 0}
     *
     * @param key 键
     * @return 值的数量
     */
    default int size(final K key) {
        return getValues(key).size();
    }

    /**
     * 遍历所有键值对，效果等同于：
     * <pre>{@code
     * for (Entry<K, Collection<V>> entry : entrySet()) {
     * 	K data = entry.getKey();
     * 	Collection<V> coll = entry.getValues();
     * 	for (V val : coll) {
     * 		consumer.accept(data, val);
     *    }
     * }
     * }</pre>
     *
     * @param consumer 操作
     */
    default void allForEach(final BiConsumer<K, V> consumer) {
        forEach((k, coll) -> coll.forEach(v -> consumer.accept(k, v)));
    }

    /**
     * 获取所有的值，效果等同于：
     * <pre>{@code
     * List<V> results = new ArrayList<>();
     * for (Collection<V> coll : values()) {
     * 	results.addAll(coll);
     * }
     * }</pre>
     *
     * @return 值
     */
    default Collection<V> allValues() {
        return values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}
