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
package org.miaixz.bus.core.center;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 集合的stream操作封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollectionStream {

    /**
     * 将collection转化为类型不变的map
     * <B>{@code Collection<V>  ---->  Map<K,V>}</B>
     *
     * @param collection 需要转化的集合
     * @param key        V类型转化为K类型的lambda方法
     * @param <V>        collection中的泛型
     * @param <K>        map中的key类型
     * @return 转化后的map
     */
    public static <V, K> Map<K, V> toIdentityMap(final Collection<V> collection, final Function<V, K> key) {
        return toIdentityMap(collection, key, false);
    }

    /**
     * 将collection转化为类型不变的map
     * <B>{@code Collection<V>  ---->  Map<K,V>}</B>
     *
     * @param collection 需要转化的集合
     * @param key        V类型转化为K类型的lambda方法
     * @param isParallel 是否并行流
     * @param <V>        collection中的泛型
     * @param <K>        map中的key类型
     * @return 转化后的map
     */
    public static <V, K> Map<K, V> toIdentityMap(final Collection<V> collection, final Function<V, K> key, final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return toMap(collection, (v) -> Optional.ofNullable(v).map(key).get(), Function.identity(), isParallel);
    }

    /**
     * 将Collection转化为map(value类型与collection的泛型不同)
     * <B>{@code Collection<E> -----> Map<K,V>  }</B>
     *
     * @param collection 需要转化的集合
     * @param key        E类型转化为K类型的lambda方法
     * @param value      E类型转化为V类型的lambda方法
     * @param <E>        collection中的泛型
     * @param <K>        map中的key类型
     * @param <V>        map中的value类型
     * @return 转化后的map
     */
    public static <E, K, V> Map<K, V> toMap(final Collection<E> collection, final Function<E, K> key, final Function<E, V> value) {
        return toMap(collection, key, value, false);
    }

    /**
     * @param collection 需要转化的集合
     * @param key        E类型转化为K类型的lambda方法
     * @param value      E类型转化为V类型的lambda方法
     * @param isParallel 是否并行流
     * @param <E>        collection中的泛型
     * @param <K>        map中的key类型
     * @param <V>        map中的value类型
     * @return 转化后的map
     */
    public static <E, K, V> Map<K, V> toMap(final Collection<E> collection, final Function<E, K> key, final Function<E, V> value, final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return StreamKit.of(collection, isParallel)
                .collect(HashMap::new, (m, v) -> m.put(key.apply(v), value.apply(v)), HashMap::putAll);
    }


    /**
     * 将collection按照规则(比如有相同的班级id)分组成map
     * <B>{@code Collection<E> -------> Map<K,List<E>> } </B>
     *
     * @param collection 需要分组的集合
     * @param key        分组的规则
     * @param <E>        collection中的泛型
     * @param <K>        map中的key类型
     * @return 分组后的map
     */
    public static <E, K> Map<K, List<E>> groupByKey(final Collection<E> collection, final Function<E, K> key) {
        return groupByKey(collection, key, false);
    }

    /**
     * 将collection按照规则(比如有相同的班级id)分组成map
     * <B>{@code Collection<E> -------> Map<K,List<E>> } </B>
     *
     * @param collection 需要分组的集合
     * @param key        键分组的规则
     * @param isParallel 是否并行流
     * @param <E>        collection中的泛型
     * @param <K>        map中的key类型
     * @return 分组后的map
     */
    public static <E, K> Map<K, List<E>> groupByKey(final Collection<E> collection, final Function<E, K> key, final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return groupBy(collection, key, Collectors.toList(), isParallel);
    }

    /**
     * 将collection按照两个规则(比如有相同的年级id,班级id)分组成双层map
     * <B>{@code Collection<E>  --->  Map<T,Map<U,List<E>>> } </B>
     *
     * @param collection 需要分组的集合
     * @param key1       第一个分组的规则
     * @param key2       第二个分组的规则
     * @param <E>        集合元素类型
     * @param <K>        第一个map中的key类型
     * @param <U>        第二个map中的key类型
     * @return 分组后的map
     */
    public static <E, K, U> Map<K, Map<U, List<E>>> groupBy2Key(final Collection<E> collection, final Function<E, K> key1, final Function<E, U> key2) {
        return groupBy2Key(collection, key1, key2, false);
    }


    /**
     * 将collection按照两个规则(比如有相同的年级id,班级id)分组成双层map
     * <B>{@code Collection<E>  --->  Map<T,Map<U,List<E>>> } </B>
     *
     * @param collection 需要分组的集合
     * @param key1       第一个分组的规则
     * @param key2       第二个分组的规则
     * @param isParallel 是否并行流
     * @param <E>        集合元素类型
     * @param <K>        第一个map中的key类型
     * @param <U>        第二个map中的key类型
     * @return 分组后的map
     */
    public static <E, K, U> Map<K, Map<U, List<E>>> groupBy2Key(final Collection<E> collection, final Function<E, K> key1,
                                                                final Function<E, U> key2, final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return groupBy(collection, key1, CollectorKit.groupingBy(key2, Collectors.toList()), isParallel);
    }

    /**
     * 将collection按照两个规则(比如有相同的年级id,班级id)分组成双层map
     * <B>{@code Collection<E>  --->  Map<T,Map<U,E>> } </B>
     *
     * @param collection 需要分组的集合
     * @param key1       第一个分组的规则
     * @param key2       第二个分组的规则
     * @param <T>        第一个map中的key类型
     * @param <U>        第二个map中的key类型
     * @param <E>        collection中的泛型
     * @return 分组后的map
     */
    public static <E, T, U> Map<T, Map<U, E>> group2Map(final Collection<E> collection, final Function<E, T> key1, final Function<E, U> key2) {
        return group2Map(collection, key1, key2, false);
    }

    /**
     * 将collection按照两个规则(比如有相同的年级id,班级id)分组成双层map
     * <B>{@code Collection<E>  --->  Map<T,Map<U,E>> } </B>
     *
     * @param collection 需要分组的集合
     * @param key1       第一个分组的规则
     * @param key2       第二个分组的规则
     * @param isParallel 是否并行流
     * @param <T>        第一个map中的key类型
     * @param <U>        第二个map中的key类型
     * @param <E>        collection中的泛型
     * @return 分组后的map
     */
    public static <E, T, U> Map<T, Map<U, E>> group2Map(final Collection<E> collection,
                                                        final Function<E, T> key1, final Function<E, U> key2, final boolean isParallel) {
        if (CollKit.isEmpty(collection) || key1 == null || key2 == null) {
            return MapKit.zero();
        }
        return groupBy(collection, key1, CollectorKit.toMap(key2, Function.identity(), (l, r) -> l), isParallel);
    }

    /**
     * 将collection按照规则(比如有相同的班级id)分组成map，map中的key为班级id，value为班级名
     * <B>{@code Collection<E> -------> Map<K,List<V>> } </B>
     *
     * @param collection 需要分组的集合
     * @param key        键分组的规则
     * @param value      值分组的规则
     * @param <E>        collection中的泛型
     * @param <K>        map中的key类型
     * @param <V>        List中的value类型
     * @return 分组后的map
     */
    public static <E, K, V> Map<K, List<V>> groupKeyValue(final Collection<E> collection, final Function<E, K> key,
                                                          final Function<E, V> value) {
        return groupKeyValue(collection, key, value, false);
    }

    /**
     * 将collection按照规则(比如有相同的班级id)分组成map，map中的key为班级id，value为班级名
     * <B>{@code Collection<E> -------> Map<K,List<V>> } </B>
     *
     * @param collection 需要分组的集合
     * @param key        键分组的规则
     * @param value      值分组的规则
     * @param isParallel 是否并行流
     * @param <E>        collection中的泛型
     * @param <K>        map中的key类型
     * @param <V>        List中的value类型
     * @return 分组后的map
     */
    public static <E, K, V> Map<K, List<V>> groupKeyValue(final Collection<E> collection, final Function<E, K> key,
                                                          final Function<E, V> value, final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return groupBy(collection, key, Collectors.mapping(v -> Optional.ofNullable(v).map(value).orElse(null), Collectors.toList()), isParallel);
    }

    /**
     * 作为所有groupingBy的公共方法，更接近于原生，灵活性更强
     *
     * @param collection 需要分组的集合
     * @param key        第一次分组时需要的key
     * @param downstream 分组后需要进行的操作
     * @param <E>        collection中的泛型
     * @param <K>        map中的key类型
     * @param <D>        后续操作的返回值
     * @return 分组后的map
     */
    public static <E, K, D> Map<K, D> groupBy(final Collection<E> collection, final Function<E, K> key, final Collector<E, ?, D> downstream) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return groupBy(collection, key, downstream, false);
    }

    /**
     * 作为所有groupingBy的公共方法，更接近于原生，灵活性更强
     *
     * @param collection 需要分组的集合
     * @param key        第一次分组时需要的key
     * @param downstream 分组后需要进行的操作
     * @param isParallel 是否并行流
     * @param <E>        collection中的泛型
     * @param <K>        map中的key类型
     * @param <D>        后续操作的返回值
     * @return 分组后的map
     * @see Collectors#groupingBy(Function, Collector)
     */
    public static <E, K, D> Map<K, D> groupBy(final Collection<E> collection, final Function<E, K> key, final Collector<E, ?, D> downstream, final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return StreamKit.of(collection, isParallel).collect(CollectorKit.groupingBy(key, downstream));
    }

    /**
     * 将collection转化为List集合，但是两者的泛型不同
     * <B>{@code Collection<E>  ------>  List<T> } </B>
     *
     * @param collection 需要转化的集合
     * @param function   collection中的泛型转化为list泛型的lambda表达式
     * @param <E>        collection中的泛型
     * @param <T>        List中的泛型
     * @return 转化后的list
     */
    public static <E, T> List<T> toList(final Collection<E> collection, final Function<E, T> function) {
        return toList(collection, function, false);
    }

    /**
     * 将collection转化为List集合，但是两者的泛型不同
     * <B>{@code Collection<E>  ------>  List<T> } </B>
     *
     * @param collection 需要转化的集合
     * @param function   collection中的泛型转化为list泛型的lambda表达式
     * @param isParallel 是否并行流
     * @param <E>        collection中的泛型
     * @param <T>        List中的泛型
     * @return 转化后的list
     */
    public static <E, T> List<T> toList(final Collection<E> collection, final Function<E, T> function, final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return ListKit.zero();
        }
        return StreamKit.of(collection, isParallel)
                .map(function)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 将collection转化为Set集合，但是两者的泛型不同
     * <B>{@code Collection<E>  ------>  Set<T> } </B>
     *
     * @param collection 需要转化的集合
     * @param function   collection中的泛型转化为set泛型的lambda表达式
     * @param <E>        collection中的泛型
     * @param <T>        Set中的泛型
     * @return 转化后的Set
     */
    public static <E, T> Set<T> toSet(final Collection<E> collection, final Function<E, T> function) {
        return toSet(collection, function, false);
    }

    /**
     * 将collection转化为Set集合，但是两者的泛型不同
     * <B>{@code Collection<E>  ------>  Set<T> } </B>
     *
     * @param collection 需要转化的集合
     * @param function   collection中的泛型转化为set泛型的lambda表达式
     * @param isParallel 是否并行流
     * @param <E>        collection中的泛型
     * @param <T>        Set中的泛型
     * @return 转化后的Set
     */
    public static <E, T> Set<T> toSet(final Collection<E> collection, final Function<E, T> function, final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return SetKit.zero();
        }
        return StreamKit.of(collection, isParallel)
                .map(function)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }


    /**
     * 合并两个相同key类型的map
     *
     * @param map1  第一个需要合并的 map
     * @param map2  第二个需要合并的 map
     * @param merge 合并的lambda，将key  value1 value2合并成最终的类型,注意value可能为空的情况
     * @param <K>   map中的key类型
     * @param <X>   第一个 map的value类型
     * @param <Y>   第二个 map的value类型
     * @param <V>   最终map的value类型
     * @return 合并后的map
     */
    public static <K, X, Y, V> Map<K, V> merge(Map<K, X> map1, Map<K, Y> map2, final BiFunction<X, Y, V> merge) {
        if (MapKit.isEmpty(map1) && MapKit.isEmpty(map2)) {
            return MapKit.zero();
        } else if (MapKit.isEmpty(map1)) {
            map1 = MapKit.empty();
        } else if (MapKit.isEmpty(map2)) {
            map2 = MapKit.empty();
        }
        final Set<K> key = new HashSet<>();
        key.addAll(map1.keySet());
        key.addAll(map2.keySet());
        final Map<K, V> map = MapKit.newHashMap(key.size());
        for (final K t : key) {
            final X x = map1.get(t);
            final Y y = map2.get(t);
            final V z = merge.apply(x, y);
            if (z != null) {
                map.put(t, z);
            }
        }
        return map;
    }

}
