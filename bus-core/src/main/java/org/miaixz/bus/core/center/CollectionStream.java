/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.*;

/**
 * Provides a rich set of stream-based operations for Java collections. This utility class offers convenient methods for
 * transforming, grouping, and merging collections by leveraging the power of the Java Stream API.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollectionStream extends CollectionValidator {

    /**
     * Constructs a new CollectionStream. Utility class constructor for static access.
     */
    public CollectionStream() {
    }

    /**
     * Converts a collection into a map, where the keys are derived from the elements and the values are the elements
     * themselves.
     * <p>
     * <b>Transformation:</b> {@code Collection<V> -> Map<K, V>}
     *
     * @param <V>        The type of elements in the collection and values in the map.
     * @param <K>        The type of keys in the map.
     * @param collection The source collection to be converted.
     * @param key        A function to extract the key from an element.
     * @return A map where keys are extracted from elements and values are the elements themselves.
     */
    public static <V, K> Map<K, V> toIdentityMap(final Collection<V> collection, final FunctionX<V, K> key) {
        return toIdentityMap(collection, key, false);
    }

    /**
     * Converts a collection into a map, where the keys are derived from the elements and the values are the elements
     * themselves, with an option for parallel processing.
     * <p>
     * <b>Transformation:</b> {@code Collection<V> -> Map<K, V>}
     *
     * @param <V>        The type of elements in the collection and values in the map.
     * @param <K>        The type of keys in the map.
     * @param collection The source collection to be converted.
     * @param key        A function to extract the key from an element.
     * @param isParallel If {@code true}, the conversion is performed in parallel.
     * @return A map where keys are extracted from elements and values are the elements themselves.
     */
    public static <V, K> Map<K, V> toIdentityMap(
            final Collection<V> collection,
            final FunctionX<V, K> key,
            final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return toMap(collection, (v) -> Optional.ofNullable(v).map(key).getOrNull(), Function.identity(), isParallel);
    }

    /**
     * Converts a collection into a map by applying separate key and value mapping functions to each element.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> Map<K, V>}
     *
     * @param <E>        The type of elements in the source collection.
     * @param <K>        The type of keys in the resulting map.
     * @param <V>        The type of values in the resulting map.
     * @param collection The source collection to be converted.
     * @param key        A function to extract the key from an element.
     * @param value      A function to extract the value from an element.
     * @return A map created by applying the key and value functions to each element.
     */
    public static <E, K, V> Map<K, V> toMap(
            final Collection<E> collection,
            final Function<E, K> key,
            final Function<E, V> value) {
        return toMap(collection, key, value, false);
    }

    /**
     * Converts a collection into a map by applying separate key and value mapping functions to each element, with an
     * option for parallel processing.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> Map<K, V>}
     *
     * @param <E>        The type of elements in the source collection.
     * @param <K>        The type of keys in the resulting map.
     * @param <V>        The type of values in the resulting map.
     * @param collection The source collection to be converted.
     * @param key        A function to extract the key from an element.
     * @param value      A function to extract the value from an element.
     * @param isParallel If {@code true}, the conversion is performed in parallel.
     * @return A map created by applying the key and value functions to each element.
     */
    public static <E, K, V> Map<K, V> toMap(
            final Collection<E> collection,
            final Function<E, K> key,
            final Function<E, V> value,
            final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return StreamKit.of(collection, isParallel)
                .collect(HashMap::new, (m, v) -> m.put(key.apply(v), value.apply(v)), HashMap::putAll);
    }

    /**
     * Groups elements of a collection into a map, where keys are derived from the elements.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> Map<K, List<E>>}
     *
     * @param <E>        The type of elements in the collection.
     * @param <K>        The type of keys in the map.
     * @param collection The collection to be grouped.
     * @param key        A function to extract the grouping key from an element.
     * @return A map where elements are grouped by the extracted key.
     */
    public static <E, K> Map<K, List<E>> groupByKey(final Collection<E> collection, final Function<E, K> key) {
        return groupByKey(collection, key, false);
    }

    /**
     * Groups elements of a collection into a map, where keys are derived from the elements, with an option for parallel
     * processing.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> Map<K, List<E>>}
     *
     * @param <E>        The type of elements in the collection.
     * @param <K>        The type of keys in the map.
     * @param collection The collection to be grouped.
     * @param key        A function to extract the grouping key from an element.
     * @param isParallel If {@code true}, the grouping is performed in parallel.
     * @return A map where elements are grouped by the extracted key.
     */
    public static <E, K> Map<K, List<E>> groupByKey(
            final Collection<E> collection,
            final Function<E, K> key,
            final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return groupBy(collection, key, Collectors.toList(), isParallel);
    }

    /**
     * Groups a collection into a two-level nested map based on two key extraction functions.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> Map<K, Map<U, List<E>>>}
     *
     * @param <E>        The type of elements in the collection.
     * @param <K>        The type of keys in the first-level map.
     * @param <U>        The type of keys in the second-level map.
     * @param collection The collection to be grouped.
     * @param key1       A function to extract the first-level key.
     * @param key2       A function to extract the second-level key.
     * @return A two-level nested map representing the grouped elements.
     */
    public static <E, K, U> Map<K, Map<U, List<E>>> groupBy2Key(
            final Collection<E> collection,
            final Function<E, K> key1,
            final Function<E, U> key2) {
        return groupBy2Key(collection, key1, key2, false);
    }

    /**
     * Groups a collection into a two-level nested map based on two key extraction functions, with an option for
     * parallel processing.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> Map<K, Map<U, List<E>>>}
     *
     * @param <E>        The type of elements in the collection.
     * @param <K>        The type of keys in the first-level map.
     * @param <U>        The type of keys in the second-level map.
     * @param collection The collection to be grouped.
     * @param key1       A function to extract the first-level key.
     * @param key2       A function to extract the second-level key.
     * @param isParallel If {@code true}, the grouping is performed in parallel.
     * @return A two-level nested map representing the grouped elements.
     */
    public static <E, K, U> Map<K, Map<U, List<E>>> groupBy2Key(
            final Collection<E> collection,
            final Function<E, K> key1,
            final Function<E, U> key2,
            final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return groupBy(collection, key1, CollectorKit.groupingBy(key2, Collectors.toList()), isParallel);
    }

    /**
     * Groups a collection into a two-level map where the innermost value is a single element. This assumes that the
     * combination of the two keys is unique for each element.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> Map<T, Map<U, E>>}
     *
     * @param <T>        The type of keys in the first-level map.
     * @param <U>        The type of keys in the second-level map.
     * @param <E>        The type of elements in the collection.
     * @param collection The collection to be grouped.
     * @param key1       A function to extract the first-level key.
     * @param key2       A function to extract the second-level key.
     * @return A two-level map where each key pair maps to a single element.
     */
    public static <E, T, U> Map<T, Map<U, E>> group2Map(
            final Collection<E> collection,
            final Function<E, T> key1,
            final Function<E, U> key2) {
        return group2Map(collection, key1, key2, false);
    }

    /**
     * Groups a collection into a two-level map where the innermost value is a single element, with an option for
     * parallel processing.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> Map<T, Map<U, E>>}
     *
     * @param <T>        The type of keys in the first-level map.
     * @param <U>        The type of keys in the second-level map.
     * @param <E>        The type of elements in the collection.
     * @param collection The collection to be grouped.
     * @param key1       A function to extract the first-level key.
     * @param key2       A function to extract the second-level key.
     * @param isParallel If {@code true}, the grouping is performed in parallel.
     * @return A two-level map where each key pair maps to a single element.
     */
    public static <E, T, U> Map<T, Map<U, E>> group2Map(
            final Collection<E> collection,
            final Function<E, T> key1,
            final Function<E, U> key2,
            final boolean isParallel) {
        if (CollKit.isEmpty(collection) || key1 == null || key2 == null) {
            return MapKit.zero();
        }
        return groupBy(collection, key1, CollectorKit.toMap(key2, Function.identity(), (l, r) -> l), isParallel);
    }

    /**
     * Groups a collection into a map where keys are extracted and values are transformed into a list.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> Map<K, List<V>>}
     *
     * @param <E>        The type of elements in the collection.
     * @param <K>        The type of keys in the map.
     * @param <V>        The type of values in the list.
     * @param collection The collection to be grouped.
     * @param key        A function to extract the key for grouping.
     * @param value      A function to transform elements into the desired value type.
     * @return A map with grouped keys and lists of transformed values.
     */
    public static <E, K, V> Map<K, List<V>> groupKeyValue(
            final Collection<E> collection,
            final FunctionX<E, K> key,
            final FunctionX<E, V> value) {
        return groupKeyValue(collection, key, value, false);
    }

    /**
     * Groups a collection into a map where keys are extracted and values are transformed into a list, with an option
     * for parallel processing.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> Map<K, List<V>>}
     *
     * @param <E>        The type of elements in the collection.
     * @param <K>        The type of keys in the map.
     * @param <V>        The type of values in the list.
     * @param collection The collection to be grouped.
     * @param key        A function to extract the key for grouping.
     * @param value      A function to transform elements into the desired value type.
     * @param isParallel If {@code true}, the grouping is performed in parallel.
     * @return A map with grouped keys and lists of transformed values.
     */
    public static <E, K, V> Map<K, List<V>> groupKeyValue(
            final Collection<E> collection,
            final FunctionX<E, K> key,
            final FunctionX<E, V> value,
            final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return groupBy(
                collection,
                key,
                Collectors.mapping(v -> Optional.ofNullable(v).map(value).orElse(null), Collectors.toList()),
                isParallel);
    }

    /**
     * A generic grouping method that provides flexibility similar to native stream grouping.
     *
     * @param <E>        The type of elements in the collection.
     * @param <K>        The type of keys in the map.
     * @param <D>        The type of the result from the downstream collector.
     * @param collection The collection to be grouped.
     * @param key        A function to extract the first-level grouping key.
     * @param downstream The downstream {@link Collector} to apply to elements in each group.
     * @return The resulting grouped map.
     */
    public static <E, K, D> Map<K, D> groupBy(
            final Collection<E> collection,
            final Function<E, K> key,
            final Collector<E, ?, D> downstream) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return groupBy(collection, key, downstream, false);
    }

    /**
     * A generic grouping method that provides flexibility similar to native stream grouping, with an option for
     * parallel processing.
     *
     * @param <E>        The type of elements in the collection.
     * @param <K>        The type of keys in the map.
     * @param <D>        The type of the result from the downstream collector.
     * @param collection The collection to be grouped.
     * @param key        A function to extract the first-level grouping key.
     * @param downstream The downstream {@link Collector} to apply to elements in each group.
     * @param isParallel If {@code true}, the grouping is performed in parallel.
     * @return The resulting grouped map.
     * @see Collectors#groupingBy(Function, Collector)
     */
    public static <E, K, D> Map<K, D> groupBy(
            final Collection<E> collection,
            final Function<E, K> key,
            final Collector<E, ?, D> downstream,
            final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return MapKit.zero();
        }
        return StreamKit.of(collection, isParallel).collect(CollectorKit.groupingBy(key, downstream));
    }

    /**
     * Converts a collection to a list by applying a transformation function to each element.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> List<T>}
     *
     * @param <E>        The type of elements in the source collection.
     * @param <T>        The type of elements in the resulting list.
     * @param collection The collection to be converted.
     * @param function   A function to transform elements from type E to type T.
     * @return A new list containing the transformed elements.
     */
    public static <E, T> List<T> toList(final Collection<E> collection, final Function<E, T> function) {
        return toList(collection, function, false);
    }

    /**
     * Converts a collection to a list by applying a transformation function to each element, with an option for
     * parallel processing.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> List<T>}
     *
     * @param <E>        The type of elements in the source collection.
     * @param <T>        The type of elements in the resulting list.
     * @param collection The collection to be converted.
     * @param function   A function to transform elements from type E to type T.
     * @param isParallel If {@code true}, the conversion is performed in parallel.
     * @return A new list containing the transformed elements.
     */
    public static <E, T> List<T> toList(
            final Collection<E> collection,
            final Function<E, T> function,
            final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return ListKit.zero();
        }
        return StreamKit.of(collection, isParallel).map(function).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Converts a collection to a set by applying a transformation function to each element.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> Set<T>}
     *
     * @param <E>        The type of elements in the source collection.
     * @param <T>        The type of elements in the resulting set.
     * @param collection The collection to be converted.
     * @param function   A function to transform elements from type E to type T.
     * @return A new set containing the transformed elements.
     */
    public static <E, T> Set<T> toSet(final Collection<E> collection, final Function<E, T> function) {
        return toSet(collection, function, false);
    }

    /**
     * Converts a collection to a set by applying a transformation function to each element, with an option for parallel
     * processing.
     * <p>
     * <b>Transformation:</b> {@code Collection<E> -> Set<T>}
     *
     * @param <E>        The type of elements in the source collection.
     * @param <T>        The type of elements in the resulting set.
     * @param collection The collection to be converted.
     * @param function   A function to transform elements from type E to type T.
     * @param isParallel If {@code true}, the conversion is performed in parallel.
     * @return A new set containing the transformed elements.
     */
    public static <E, T> Set<T> toSet(
            final Collection<E> collection,
            final Function<E, T> function,
            final boolean isParallel) {
        if (CollKit.isEmpty(collection)) {
            return SetKit.zero();
        }
        return StreamKit.of(collection, isParallel).map(function).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * Merges two maps with the same key type into a new map.
     *
     * @param <K>   The type of keys in the maps.
     * @param <X>   The type of values in the first map.
     * @param <Y>   The type of values in the second map.
     * @param <V>   The type of values in the resulting merged map.
     * @param map1  The first map to merge.
     * @param map2  The second map to merge.
     * @param merge A {@link BiFunction} to combine values from {@code map1} and {@code map2} for a given key. Note that
     *              values from either map might be {@code null}.
     * @return The merged map.
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

    /**
     * Computes the Cartesian product of a list of sets. For more information, see:
     * <a href="https://www.baeldung-cn.com/java-cartesian-product-sets">Java Cartesian Product of Sets</a>
     *
     * @param sets  A list of sets, where each set is represented as a {@code List<Object>}.
     * @param index The current index in the list of sets, used for recursion.
     * @return A {@link Stream} of {@code List<Object>}, where each inner list represents a unique combination from the
     *         Cartesian product.
     */
    public static Stream<List<Object>> cartesianProduct(final List<List<Object>> sets, final int index) {
        if (index == sets.size()) {
            return Stream.of(ListKit.zero());
        }
        final List<Object> currentSet = sets.get(index);
        return currentSet.stream().flatMap(element -> cartesianProduct(sets, index + 1).map(list -> {
            final List<Object> newList = new ArrayList<>(list);
            newList.add(0, element);
            return newList;
        }));
    }

}
