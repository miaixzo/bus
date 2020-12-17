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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.Editor;
import org.aoju.bus.core.lang.Filter;
import org.aoju.bus.core.lang.Types;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.core.map.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * Map相关工具类
 *
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
public class MapKit {

    /**
     * 默认初始大小
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 16;
    /**
     * 默认增长因子,当Map的size达到 容量*增长因子时,开始扩充Map
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Map是否为空
     *
     * @param map 集合
     * @return 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return null == map || map.isEmpty();
    }

    /**
     * Map是否为非空
     *
     * @param map 集合
     * @return 是否为非空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return null != map && false == map.isEmpty();
    }

    /**
     * 新建一个HashMap
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @return HashMap对象
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>     Key类型
     * @param <V>     Value类型
     * @param size    初始大小,由于默认负载因子0.75,传入的size会实际初始大小为size / 0.75
     * @param isOrder Map的Key是否有序,有序返回 {@link LinkedHashMap},否则返回 {@link HashMap}
     * @return HashMap对象
     */
    public static <K, V> HashMap<K, V> newHashMap(int size, boolean isOrder) {
        int initialCapacity = (int) (size / DEFAULT_LOAD_FACTOR);
        return isOrder ? new LinkedHashMap<>(initialCapacity) : new HashMap<>(initialCapacity);
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @param size 初始大小,由于默认负载因子0.75,传入的size会实际初始大小为size / 0.75
     * @return HashMap对象
     */
    public static <K, V> HashMap<K, V> newHashMap(int size) {
        return newHashMap(size, false);
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>     Key类型
     * @param <V>     Value类型
     * @param isOrder Map的Key是否有序,有序返回 {@link LinkedHashMap},否则返回 {@link HashMap}
     * @return HashMap对象
     */
    public static <K, V> HashMap<K, V> newHashMap(boolean isOrder) {
        return newHashMap(DEFAULT_INITIAL_CAPACITY, isOrder);
    }

    /**
     * 新建TreeMap,Key有序的Map
     *
     * @param <K>        Key类型
     * @param <V>        Value类型
     * @param comparator Key比较器
     * @return TreeMap
     */
    public static <K, V> TreeMap<K, V> newTreeMap(Comparator<? super K> comparator) {
        return new TreeMap<>(comparator);
    }

    /**
     * 新建TreeMap,Key有序的Map
     *
     * @param <K>        Key类型
     * @param <V>        Value类型
     * @param map        Map
     * @param comparator Key比较器
     * @return TreeMap
     */
    public static <K, V> TreeMap<K, V> newTreeMap(Map<K, V> map, Comparator<? super K> comparator) {
        final TreeMap<K, V> treeMap = new TreeMap<>(comparator);
        if (false == isEmpty(map)) {
            treeMap.putAll(map);
        }
        return treeMap;
    }

    /**
     * 创建Map
     * 传入抽象Map{@link AbstractMap}和{@link Map}类将默认创建{@link HashMap}
     *
     * @param <K>     map键类型
     * @param <V>     map值类型
     * @param mapType map类型
     * @return {@link Map}实例
     */
    public static <K, V> Map<K, V> createMap(Class<?> mapType) {
        if (mapType.isAssignableFrom(AbstractMap.class)) {
            return new HashMap<>();
        } else {
            try {
                return (Map<K, V>) ReflectKit.newInstance(mapType);
            } catch (Exception e) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 将单一键值对转换为Map
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param key   键
     * @param value 值
     * @return {@link HashMap}
     */
    public static <K, V> HashMap<K, V> of(K key, V value) {
        return of(key, value, false);
    }

    /**
     * 将单一键值对转换为Map
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param key     键
     * @param value   值
     * @param isOrder 是否有序
     * @return {@link HashMap}
     */
    public static <K, V> HashMap<K, V> of(K key, V value, boolean isOrder) {
        final HashMap<K, V> map = newHashMap(isOrder);
        map.put(key, value);
        return map;
    }

    /**
     * 将数组转换为Map(HashMap),支持数组元素类型为：
     *
     * <pre>
     * Map.Entry
     * 长度大于1的数组(取前两个值),如果不满足跳过此元素
     * Iterable 长度也必须大于1(取前两个值),如果不满足跳过此元素
     * Iterator 长度也必须大于1(取前两个值),如果不满足跳过此元素
     * </pre>
     *
     * <pre>
     * Map&lt;Object, Object&gt; colorMap = MapKit.of(new String[][] {{
     *     {"RED", "#FF0000"},
     *     {"GREEN", "#00FF00"},
     *     {"BLUE", "#0000FF"}});
     * </pre>
     * <p>
     * 参考：commons-lang
     *
     * @param array 数组 元素类型为Map.Entry、数组、Iterable、Iterator
     * @return {@link HashMap}
     */
    public static HashMap<Object, Object> of(Object[] array) {
        if (array == null) {
            return null;
        }
        final HashMap<Object, Object> map = new HashMap<>((int) (array.length * 1.5));
        for (int i = 0; i < array.length; i++) {
            final Object object = array[i];
            if (object instanceof Map.Entry) {
                Entry entry = (Entry) object;
                map.put(entry.getKey(), entry.getValue());
            } else if (object instanceof Object[]) {
                final Object[] entry = (Object[]) object;
                if (entry.length > 1) {
                    map.put(entry[0], entry[1]);
                }
            } else if (object instanceof Iterable) {
                final Iterator iter = ((Iterable) object).iterator();
                if (iter.hasNext()) {
                    final Object key = iter.next();
                    if (iter.hasNext()) {
                        final Object value = iter.next();
                        map.put(key, value);
                    }
                }
            } else if (object instanceof Iterator) {
                final Iterator iter = ((Iterator) object);
                if (iter.hasNext()) {
                    final Object key = iter.next();
                    if (iter.hasNext()) {
                        final Object value = iter.next();
                        map.put(key, value);
                    }
                }
            } else {
                throw new IllegalArgumentException(StringKit.format("Array element {}, '{}', is not type of Map.Entry or Array or Iterable or Iterator", i, object));
            }
        }
        return map;
    }

    /**
     * 根据给定的Pair数组创建Map对象
     *
     * @param pairs 键值对
     * @param <K>   键类型
     * @param <V>   值类型
     * @return Map对象
     */
    public static <K, V> Map<K, V> of(Pair<K, V>... pairs) {
        final Map<K, V> map = new HashMap<>();
        for (Pair<K, V> pair : pairs) {
            map.put(pair.getKey(), pair.getValue());
        }
        return map;
    }

    /**
     * 行转列,合并相同的键,值合并为列表
     * 将Map列表中相同key的值组成列表做为Map的value
     * 是{@link #toMapList(Map)}的逆方法
     * 比如传入数据：
     *
     * <pre>
     * [
     *  {a: 1, b: 1, c: 1}
     *  {a: 2, b: 2}
     *  {a: 3, b: 3}
     *  {a: 4}
     * ]
     * </pre>
     * <p>
     * 结果是：
     *
     * <pre>
     * {
     *   a: [1,2,3,4]
     *   b: [1,2,3,]
     *   c: [1]
     * }
     * </pre>
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param mapList Map列表
     * @return Map
     */
    public static <K, V> Map<K, List<V>> toListMap(Iterable<? extends Map<K, V>> mapList) {
        final HashMap<K, List<V>> resultMap = new HashMap<>();
        if (CollKit.isEmpty(mapList)) {
            return resultMap;
        }

        Set<Entry<K, V>> entrySet;
        for (Map<K, V> map : mapList) {
            entrySet = map.entrySet();
            K key;
            List<V> valueList;
            for (Entry<K, V> entry : entrySet) {
                key = entry.getKey();
                valueList = resultMap.get(key);
                if (null == valueList) {
                    valueList = CollKit.newArrayList(entry.getValue());
                    resultMap.put(key, valueList);
                } else {
                    valueList.add(entry.getValue());
                }
            }
        }

        return resultMap;
    }

    /**
     * 列转行 将Map中值列表分别按照其位置与key组成新的map
     * 是{@link #toListMap(Iterable)}的逆方法
     * 比如传入数据：
     *
     * <pre>
     * {
     *   a: [1,2,3,4]
     *   b: [1,2,3,]
     *   c: [1]
     * }
     * </pre>
     * <p>
     * 结果是：
     *
     * <pre>
     * [
     *  {a: 1, b: 1, c: 1}
     *  {a: 2, b: 2}
     *  {a: 3, b: 3}
     *  {a: 4}
     * ]
     * </pre>
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param listMap 列表Map
     * @return Map列表
     */
    public static <K, V> List<Map<K, V>> toMapList(Map<K, ? extends Iterable<V>> listMap) {
        final List<Map<K, V>> resultList = new ArrayList<>();
        if (isEmpty(listMap)) {
            return resultList;
        }

        boolean isEnd = true;// 是否结束 标准是元素列表已耗尽
        int index = 0;// 值索引
        Map<K, V> map;
        do {
            isEnd = true;
            map = new HashMap<>();
            List<V> vList;
            int vListSize;
            for (Entry<K, ? extends Iterable<V>> entry : listMap.entrySet()) {
                vList = CollKit.newArrayList(entry.getValue());
                vListSize = vList.size();
                if (index < vListSize) {
                    map.put(entry.getKey(), vList.get(index));
                    if (index != vListSize - 1) {
                        // 当值列表中还有更多值(非最后一个),继续循环
                        isEnd = false;
                    }
                }
            }
            if (false == map.isEmpty()) {
                resultList.add(map);
            }
            index++;
        } while (false == isEnd);

        return resultList;
    }

    /**
     * 将已知Map转换为key为驼峰风格的Map
     * 如果KEY为非String类型,保留原值
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param map 原Map
     * @return 驼峰风格Map
     */
    public static <K, V> Map<K, V> toCamelCaseMap(Map<K, V> map) {
        return (map instanceof LinkedHashMap) ? new CamelCaseLinkedMap<>(map) : new CamelCaseMap<>(map);
    }

    /**
     * 将键值对转换为二维数组,第一维是key,第二纬是value
     *
     * @param map Map
     * @return 数组
     */
    public static Object[][] toObjectArray(Map<?, ?> map) {
        if (map == null) {
            return null;
        }
        final Object[][] result = new Object[map.size()][2];
        if (map.isEmpty()) {
            return result;
        }
        int index = 0;
        for (Entry<?, ?> entry : map.entrySet()) {
            result[index][0] = entry.getKey();
            result[index][1] = entry.getValue();
            index++;
        }
        return result;
    }

    /**
     * 将map转成字符串
     *
     * @param <K>               键类型
     * @param <V>               值类型
     * @param map               Map
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @return 连接字符串
     */
    public static <K, V> String join(Map<K, V> map, String separator, String keyValueSeparator) {
        return join(map, separator, keyValueSeparator, false);
    }

    /**
     * 将map转成字符串,忽略null的键和值
     *
     * @param <K>               键类型
     * @param <V>               值类型
     * @param map               Map
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @return 连接后的字符串
     */
    public static <K, V> String joinIgnoreNull(Map<K, V> map, String separator, String keyValueSeparator) {
        return join(map, separator, keyValueSeparator, true);
    }

    /**
     * 将map转成字符串
     *
     * @param <K>               键类型
     * @param <V>               值类型
     * @param map               Map
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @return 连接后的字符串
     */
    public static <K, V> String join(Map<K, V> map, String separator, String keyValueSeparator, boolean isIgnoreNull) {
        final StringBuilder stringBuilder = new StringBuilder();
        boolean isFirst = true;
        for (Entry<K, V> entry : map.entrySet())
            if (false == isIgnoreNull || entry.getKey() != null && entry.getValue() != null) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    stringBuilder.append(separator);
                }
                stringBuilder.append(Convert.toString(entry.getKey())).append(keyValueSeparator).append(Convert.toString(entry.getValue()));
            }
        return stringBuilder.toString();
    }

    /**
     * 将map转成字符串
     *
     * @param <K>               键类型
     * @param <V>               值类型
     * @param map               Map
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param otherParams       其它附加参数字符串(例如密钥)
     * @return 连接字符串
     */
    public static <K, V> String join(Map<K, V> map, String separator, String keyValueSeparator, String... otherParams) {
        return join(map, separator, keyValueSeparator, false, otherParams);
    }

    /**
     * 根据参数排序后拼接为字符串，常用于签名
     *
     * @param params            参数
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @param otherParams       其它附加参数字符串(例如密钥)
     * @return 签名字符串
     */
    public static String sortJoin(Map<?, ?> params, String separator, String keyValueSeparator, boolean isIgnoreNull,
                                  String... otherParams) {
        return join(sort(params), separator, keyValueSeparator, isIgnoreNull, otherParams);
    }

    /**
     * 将map转成字符串，忽略null的键和值
     *
     * @param <K>               键类型
     * @param <V>               值类型
     * @param map               Map
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param otherParams       其它附加参数字符串(例如密钥)
     * @return 连接后的字符串
     */
    public static <K, V> String joinIgnoreNull(Map<K, V> map, String separator, String keyValueSeparator, String... otherParams) {
        return join(map, separator, keyValueSeparator, true, otherParams);
    }

    /**
     * 将map转成字符串
     *
     * @param <K>               键类型
     * @param <V>               值类型
     * @param map               Map，为空返回otherParams拼接
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @param otherParams       其它附加参数字符串(例如密钥)
     * @return 连接后的字符串，map和otherParams为空返回""
     */
    public static <K, V> String join(Map<K, V> map, String separator, String keyValueSeparator, boolean isIgnoreNull, String... otherParams) {
        final StringBuilder stringBuilder = StringKit.builder();
        boolean isFirst = true;
        if (isNotEmpty(map)) {
            for (Entry<K, V> entry : map.entrySet()) {
                if (false == isIgnoreNull || entry.getKey() != null && entry.getValue() != null) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        stringBuilder.append(separator);
                    }
                    stringBuilder.append(Convert.toString(entry.getKey())).append(keyValueSeparator).append(Convert.toString(entry.getValue()));
                }
            }
        }
        // 补充其它字符串到末尾，默认无分隔符
        if (ArrayKit.isNotEmpty(otherParams)) {
            for (String otherParam : otherParams) {
                stringBuilder.append(otherParam);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 过滤
     * 过滤过程通过传入的Editor实现来返回需要的元素内容,这个Editor实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象,如果返回null表示这个元素对象抛弃
     * 2、修改元素对象,返回集合中为修改后的对象
     * </pre>
     *
     * @param <K>    Key类型
     * @param <V>    Value类型
     * @param map    Map
     * @param editor 编辑器接口
     * @return 过滤后的Map
     */
    public static <K, V> Map<K, V> filter(Map<K, V> map, Editor<Entry<K, V>> editor) {
        final Map<K, V> map2 = ObjectKit.clone(map);
        if (isEmpty(map2)) {
            return map2;
        }

        map2.clear();
        Entry<K, V> modified;
        for (Entry<K, V> entry : map.entrySet()) {
            modified = editor.edit(entry);
            if (null != modified) {
                map2.put(modified.getKey(), modified.getValue());
            }
        }
        return map2;
    }

    /**
     * 过滤
     * 过滤过程通过传入的Editor实现来返回需要的元素内容,这个Editor实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象,如果返回null表示这个元素对象抛弃
     * 2、修改元素对象,返回集合中为修改后的对象
     * </pre>
     *
     * @param <K>    Key类型
     * @param <V>    Value类型
     * @param map    Map
     * @param filter 编辑器接口
     * @return 过滤后的Map
     */
    public static <K, V> Map<K, V> filter(Map<K, V> map, Filter<Entry<K, V>> filter) {
        final Map<K, V> map2 = ObjectKit.clone(map);
        if (isEmpty(map2)) {
            return map2;
        }

        map2.clear();
        for (Entry<K, V> entry : map.entrySet()) {
            if (filter.accept(entry)) {
                map2.put(entry.getKey(), entry.getValue());
            }
        }
        return map2;
    }

    /**
     * 过滤Map保留指定键值对,如果键不存在跳过
     *
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @param map  原始Map
     * @param keys 键列表
     * @return Map 结果,结果的Map类型与原Map保持一致
     */
    public static <K, V> Map<K, V> filter(Map<K, V> map, K... keys) {
        final Map<K, V> map2 = ObjectKit.clone(map);
        if (isEmpty(map2)) {
            return map2;
        }

        map2.clear();
        for (K key : keys) {
            if (map.containsKey(key)) {
                map2.put(key, map.get(key));
            }
        }
        return map2;
    }

    /**
     * Map的键和值互换
     *
     * @param <T> 键和值类型
     * @param map Map对象,键值类型必须一致
     * @return 互换后的Map
     */
    public static <T> Map<T, T> reverse(Map<T, T> map) {
        return filter(map, (Editor<Entry<T, T>>) t -> new Entry<T, T>() {

            @Override
            public T getKey() {
                return t.getValue();
            }

            @Override
            public T getValue() {
                return t.getKey();
            }

            @Override
            public T setValue(T value) {
                throw new UnsupportedOperationException("Unsupported setValue method !");
            }
        });
    }

    /**
     * 排序已有Map,Key有序的Map,使用默认Key排序方式(字母顺序)
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param map Map
     * @return TreeMap
     * @see #newTreeMap(Map, Comparator)
     */
    public static <K, V> TreeMap<K, V> sort(Map<K, V> map) {
        return sort(map, null);
    }

    /**
     * 排序已有Map,Key有序的Map
     *
     * @param <K>        Key类型
     * @param <V>        Value类型
     * @param map        Map
     * @param comparator Key比较器
     * @return TreeMap
     * @see #newTreeMap(Map, Comparator)
     */
    public static <K, V> TreeMap<K, V> sort(Map<K, V> map, Comparator<? super K> comparator) {
        TreeMap<K, V> result;
        if (map instanceof TreeMap) {
            // 已经是可排序Map,此时只有比较器一致才返回原map
            result = (TreeMap<K, V>) map;
            if (null == comparator || comparator.equals(result.comparator())) {
                return result;
            }
        } else {
            result = newTreeMap(map, comparator);
        }

        return result;
    }

    /**
     * 创建Map包装类MapWrapper
     * {@link MapWrapper}对Map做一次包装
     *
     * @param <K> key的类型
     * @param <V> value的类型
     * @param map 被代理的Map
     * @return {@link MapWrapper}
     */
    public static <K, V> MapWrapper<K, V> wrap(Map<K, V> map) {
        return new MapWrapper<>(map);
    }

    /**
     * 将对应Map转换为不可修改的Map
     *
     * @param map Map
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 不修改Map
     */
    public static <K, V> Map<K, V> unmodifiable(Map<K, V> map) {
        return Collections.unmodifiableMap(map);
    }

    /**
     * 创建代理Map
     * {@link MapProxy}对Map做一次包装,提供各种getXXX方法
     *
     * @param map 被代理的Map
     * @return {@link MapProxy}
     */
    public static MapProxy createProxy(Map<?, ?> map) {
        return MapProxy.create(map);
    }

    /**
     * 创建链接调用map
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @return map创建类
     */
    public static <K, V> MapBuilder<K, V> builder() {
        return builder(new HashMap<>());
    }

    /**
     * 创建链接调用map
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param map 实际使用的map
     * @return map创建类
     */
    public static <K, V> MapBuilder<K, V> builder(Map<K, V> map) {
        return new MapBuilder<>(map);
    }

    /**
     * 创建链接调用map
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param k   key
     * @param v   value
     * @return map创建类
     */
    public static <K, V> MapBuilder<K, V> builder(K k, V v) {
        return (builder(new HashMap<K, V>())).put(k, v);
    }

    /**
     * 获取Map的部分key生成新的Map
     *
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @param map  Map
     * @param keys 键列表
     * @return 新Map, 只包含指定的key
     */
    public static <K, V> Map<K, V> getAny(Map<K, V> map, final K... keys) {
        return filter(map, (Filter<Entry<K, V>>) entry -> ArrayKit.contains(keys, entry.getKey()));
    }

    /**
     * 获取Map指定key的值,并转换为字符串
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static String getString(Map<?, ?> map, Object key) {
        return get(map, key, String.class);
    }

    /**
     * 获取Map指定key的值,并转换为Integer
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Integer getInt(Map<?, ?> map, Object key) {
        return get(map, key, Integer.class);
    }

    /**
     * 获取Map指定key的值,并转换为Double
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Double getDouble(Map<?, ?> map, Object key) {
        return get(map, key, Double.class);
    }

    /**
     * 获取Map指定key的值，并转换为Double
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Double getDouble(Map<?, ?> map, Object key, Double defaultValue) {
        return get(map, key, Double.class, defaultValue);
    }

    /**
     * 获取Map指定key的值,并转换为Float
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Float getFloat(Map<?, ?> map, Object key) {
        return get(map, key, Float.class);
    }

    /**
     * 获取Map指定key的值，并转换为Float
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Float getFloat(Map<?, ?> map, Object key, Float defaultValue) {
        return get(map, key, Float.class, defaultValue);
    }

    /**
     * 获取Map指定key的值,并转换为Short
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Short getShort(Map<?, ?> map, Object key) {
        return get(map, key, Short.class);
    }

    /**
     * 获取Map指定key的值，并转换为Short
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Short getShort(Map<?, ?> map, Object key, Short defaultValue) {
        return get(map, key, Short.class, defaultValue);
    }

    /**
     * 获取Map指定key的值,并转换为Bool
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Boolean getBool(Map<?, ?> map, Object key) {
        return get(map, key, Boolean.class);
    }

    /**
     * 获取Map指定key的值，并转换为Bool
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Boolean getBool(Map<?, ?> map, Object key, Boolean defaultValue) {
        return get(map, key, Boolean.class, defaultValue);
    }

    /**
     * 获取Map指定key的值,并转换为Character
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Character getChar(Map<?, ?> map, Object key) {
        return get(map, key, Character.class);
    }

    /**
     * 获取Map指定key的值，并转换为Character
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Character getChar(Map<?, ?> map, Object key, Character defaultValue) {
        return get(map, key, Character.class, defaultValue);
    }

    /**
     * 获取Map指定key的值,并转换为Long
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Long getLong(Map<?, ?> map, Object key) {
        return get(map, key, Long.class);
    }

    /**
     * 获取Map指定key的值，并转换为Long
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Long getLong(Map<?, ?> map, Object key, Long defaultValue) {
        return get(map, key, Long.class, defaultValue);
    }

    /**
     * 获取Map指定key的值,并转换为{@link Date}
     *
     * @param map Map
     * @param key 键
     * @return 值
     */
    public static Date getDate(Map<?, ?> map, Object key) {
        return get(map, key, Date.class);
    }

    /**
     * 获取Map指定key的值，并转换为{@link Date}
     *
     * @param map          Map
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static Date getDate(Map<?, ?> map, Object key, Date defaultValue) {
        return get(map, key, Date.class, defaultValue);
    }

    /**
     * 获取Map指定key的值,并转换为指定类型
     *
     * @param <T>  目标值类型
     * @param map  Map
     * @param key  键
     * @param type 值类型
     * @return 值
     */
    public static <T> T get(Map<?, ?> map, Object key, Class<T> type) {
        return null == map ? null : Convert.convert(type, map.get(key));
    }

    /**
     * 获取Map指定key的值，并转换为指定类型
     *
     * @param <T>          目标值类型
     * @param map          Map
     * @param key          键
     * @param type         值类型
     * @param defaultValue 默认值
     * @return 值
     */
    public static <T> T get(Map<?, ?> map, Object key, Types<T> type, T defaultValue) {
        return null == map ? null : Convert.convert(type, map.get(key), defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为指定类型
     *
     * @param <T>          目标值类型
     * @param map          Map
     * @param key          键
     * @param type         值类型
     * @param defaultValue 默认值
     * @return 值
     */
    public static <T> T get(Map<?, ?> map, Object key, Class<T> type, T defaultValue) {
        return null == map ? null : Convert.convert(type, map.get(key), defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为指定类型，此方法在转换失败后不抛异常，返回null。
     *
     * @param <T>          目标值类型
     * @param map          Map
     * @param key          键
     * @param type         值类型
     * @param defaultValue 默认值
     * @return 值
     */
    public static <T> T getQuietly(Map<?, ?> map, Object key, Class<T> type, T defaultValue) {
        return null == map ? null : Convert.convertQuietly(type, map.get(key), defaultValue);
    }

    /**
     * 获取Map指定key的值，并转换为指定类型，转换失败后返回null，不抛异常
     *
     * @param <T>          目标值类型
     * @param map          Map
     * @param key          键
     * @param type         值类型
     * @param defaultValue 默认值
     * @return 值
     */
    public static <T> T getQuietly(Map<?, ?> map, Object key, Types<T> type, T defaultValue) {
        return null == map ? null : Convert.convertQuietly(type, map.get(key), defaultValue);
    }

    /**
     * 重命名键
     * 实现方式为一处然后重新put,当旧的key不存在直接返回
     * 当新的key存在,抛出{@link IllegalArgumentException} 异常
     *
     * @param <K>    Key类型
     * @param <V>    Value类型
     * @param map    Map
     * @param oldKey 原键
     * @param newKey 新键
     * @return map
     */
    public static <K, V> Map<K, V> renameKey(Map<K, V> map, K oldKey, K newKey) {
        if (isNotEmpty(map) && map.containsKey(oldKey)) {
            if (map.containsKey(newKey)) {
                throw new IllegalArgumentException(StringKit.format("The key '{}' exist !", newKey));
            }
            map.put(newKey, map.remove(oldKey));
        }
        return map;
    }

    /**
     * 去除Map中值为{@code null}的键值对
     * 注意：此方法在传入的Map上直接修改。
     *
     * @param <K> key的类型
     * @param <V> value的类型
     * @param map Map
     * @return map
     */
    public static <K, V> Map<K, V> removeNullValue(Map<K, V> map) {
        if (isEmpty(map)) {
            return map;
        }

        final Iterator<Entry<K, V>> iter = map.entrySet().iterator();
        Entry<K, V> entry;
        while (iter.hasNext()) {
            entry = iter.next();
            if (null == entry.getValue()) {
                iter.remove();
            }
        }

        return map;
    }

    /**
     * 去掉Map中指定key的键值对，修改原Map
     *
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @param map  Map
     * @param keys 键列表
     * @return 修改后的key
     */
    public static <K, V> Map<K, V> removeAny(Map<K, V> map, final K... keys) {
        for (K key : keys) {
            map.remove(key);
        }
        return map;
    }

    /**
     * 返回一个空Map
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 空Map
     * @see Collections#emptyMap()
     */
    public static <K, V> Map<K, V> empty() {
        return Collections.emptyMap();
    }

    /**
     * 根据传入的Map类型不同，返回对应类型的空Map
     *
     * <pre>
     *     1. NavigableMap
     *     2. SortedMap
     *     3. Map
     * </pre>
     *
     * @param <K>      键类型
     * @param <V>      值类型
     * @param <T>      Map类型
     * @param mapClass Map类型，null返回默认的Map
     * @return 空Map
     */
    public static <K, V, T extends Map<K, V>> T empty(Class<?> mapClass) {
        if (null == mapClass) {
            return (T) Collections.emptyMap();
        }
        if (NavigableMap.class == mapClass) {
            return (T) Collections.emptyNavigableMap();
        } else if (SortedMap.class == mapClass) {
            return (T) Collections.emptySortedMap();
        } else if (Map.class == mapClass) {
            return (T) Collections.emptyMap();
        }

        // 不支持空集合的集合类型
        throw new IllegalArgumentException(StringKit.format("[{}] is not support to get empty!", mapClass));
    }

    /**
     * 清除一个或多个Map集合内的元素，每个Map调用clear()方法
     *
     * @param maps 一个或多个Map
     */
    public static void clear(Map<?, ?>... maps) {
        for (Map<?, ?> map : maps) {
            if (isNotEmpty(map)) {
                map.clear();
            }
        }
    }

}
