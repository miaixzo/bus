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
package org.miaixz.bus.core.xyz;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.function.Consumer3X;
import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.center.map.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

/**
 * Map related utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapKit extends MapGets {

    /**
     * Returns the given map if it is not empty, otherwise returns the default map.
     *
     * @param <T>        The type of the map.
     * @param <K>        The type of keys in the map.
     * @param <V>        The type of values in the map.
     * @param map        The map to check.
     * @param defaultMap The default map to return if the given map is empty.
     * @return The non-empty original map or the default map.
     */
    public static <T extends Map<K, V>, K, V> T defaultIfEmpty(final T map, final T defaultMap) {
        return isEmpty(map) ? defaultMap : map;
    }

    /**
     * Creates a new empty {@link HashMap}.
     *
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @return A new {@link HashMap} object.
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * Creates a new {@link HashMap} with a specified initial size and order preference.
     *
     * @param <K>      The type of keys in the map.
     * @param <V>      The type of values in the map.
     * @param size     The initial desired size. The actual initial capacity will be calculated as
     *                 {@code size / 0.75 + 1}.
     * @param isLinked If {@code true}, a {@link LinkedHashMap} is returned (keys maintain insertion order); otherwise,
     *                 a {@link HashMap} is returned.
     * @return A new {@link HashMap} or {@link LinkedHashMap} object.
     */
    public static <K, V> HashMap<K, V> newHashMap(final int size, final boolean isLinked) {
        final int initialCapacity = (int) (size / Normal.DEFAULT_LOAD_FACTOR) + 1;
        return isLinked ? new LinkedHashMap<>(initialCapacity) : new HashMap<>(initialCapacity);
    }

    /**
     * Creates a new {@link HashMap} with a specified initial size.
     *
     * @param <K>  The type of keys in the map.
     * @param <V>  The type of values in the map.
     * @param size The initial desired size. The actual initial capacity will be calculated as {@code size / 0.75 + 1}.
     * @return A new {@link HashMap} object.
     */
    public static <K, V> HashMap<K, V> newHashMap(final int size) {
        return newHashMap(size, false);
    }

    /**
     * Creates a new {@link HashMap} with a default initial capacity and order preference.
     *
     * @param <K>      The type of keys in the map.
     * @param <V>      The type of values in the map.
     * @param isLinked If {@code true}, a {@link LinkedHashMap} is returned (keys maintain insertion order); otherwise,
     *                 a {@link HashMap} is returned.
     * @return A new {@link HashMap} or {@link LinkedHashMap} object.
     */
    public static <K, V> HashMap<K, V> newHashMap(final boolean isLinked) {
        return newHashMap(Normal._16, isLinked);
    }

    /**
     * Creates a new {@link TreeMap} with keys ordered by the given comparator.
     *
     * @param <K>        The type of keys in the map.
     * @param <V>        The type of values in the map.
     * @param comparator The comparator to order the keys.
     * @return A new {@link TreeMap}.
     */
    public static <K, V> TreeMap<K, V> newTreeMap(final Comparator<? super K> comparator) {
        return new TreeMap<>(comparator);
    }

    /**
     * Creates a new {@link TreeMap} initialized with the given map and ordered by the given comparator.
     *
     * @param <K>        The type of keys in the map.
     * @param <V>        The type of values in the map.
     * @param map        The map to initialize the new TreeMap with.
     * @param comparator The comparator to order the keys.
     * @return A new {@link TreeMap}.
     */
    public static <K, V> TreeMap<K, V> newTreeMap(final Map<K, V> map, final Comparator<? super K> comparator) {
        final TreeMap<K, V> treeMap = new TreeMap<>(comparator);
        if (isNotEmpty(map)) {
            treeMap.putAll(map);
        }
        return treeMap;
    }

    /**
     * Creates a new {@link IdentityHashMap} with a specified initial capacity. In an {@link IdentityHashMap}, key
     * equality is determined by reference equality ({@code ==}) instead of object equality ({@code equals()}).
     *
     * @param <K>  The type of keys in the map.
     * @param <V>  The type of values in the map.
     * @param size The initial capacity.
     * @return A new {@link IdentityHashMap}.
     */
    public static <K, V> Map<K, V> newIdentityMap(final int size) {
        return new IdentityHashMap<>(size);
    }

    /**
     * Creates a new {@link Map} instance based on the provided map type. If an abstract map type like
     * {@link AbstractMap} or {@link Map} is provided, a {@link HashMap} will be created by default.
     *
     * @param <K>     The type of keys in the map.
     * @param <V>     The type of values in the map.
     * @param mapType The class of the map type to create.
     * @return A new {@link Map} instance.
     */
    public static <K, V> Map<K, V> createMap(final Class<?> mapType) {
        return createMap(mapType, HashMap::new);
    }

    /**
     * Creates a new {@link Map} instance based on the provided map type. If creation via reflection fails or an
     * abstract map type is provided, the {@code defaultMap} supplier is used.
     *
     * @param <K>        The type of keys in the map.
     * @param <V>        The type of values in the map.
     * @param mapType    The class of the map type to create.
     * @param defaultMap A supplier for a default map to use if reflection creation fails or the type is abstract.
     * @return A new {@link Map} instance.
     */
    public static <K, V> Map<K, V> createMap(final Class<?> mapType, final Supplier<Map<K, V>> defaultMap) {
        Map<K, V> result = null;
        if (null != mapType && !mapType.isAssignableFrom(AbstractMap.class)) {
            try {
                result = (Map<K, V>) ReflectKit.newInstanceIfPossible(mapType);
            } catch (final Exception ignore) {
                // JDK9+ may throw java.lang.reflect.InaccessibleObjectException
                // Skip and use default map
            }
        }

        if (null == result) {
            result = defaultMap.get();
        }

        if (!result.isEmpty()) {
            // If the constructor puts values, clear them to ensure an empty map is returned.
            result.clear();
        }

        return result;
    }

    /**
     * Converts a single key-value pair into a {@link HashMap}.
     *
     * @param <K>   The type of the key.
     * @param <V>   The type of the value.
     * @param key   The key.
     * @param value The value.
     * @return A new {@link HashMap} containing the single key-value pair.
     */
    public static <K, V> HashMap<K, V> of(final K key, final V value) {
        return of(key, value, false);
    }

    /**
     * Converts a single key-value pair into a {@link HashMap}, with an option for ordered keys.
     *
     * @param <K>     The type of the key.
     * @param <V>     The type of the value.
     * @param key     The key.
     * @param value   The value.
     * @param isOrder If {@code true}, a {@link LinkedHashMap} is created; otherwise, a {@link HashMap}.
     * @return A new {@link HashMap} or {@link LinkedHashMap} containing the single key-value pair.
     */
    public static <K, V> HashMap<K, V> of(final K key, final V value, final boolean isOrder) {
        final HashMap<K, V> map = newHashMap(isOrder);
        map.put(key, value);
        return map;
    }

    /**
     * Creates a {@link Map} object from an array of key-value pairs. The input array must consist of alternating keys
     * and values (e.g., key1, value1, key2, value2, ...). Keys will be cast to type K, and values to type V.
     * <p>
     * Example:
     * 
     * <pre>
     * 
     * LinkedHashMap map = MapKit.ofKvs(false, "RED", "#FF0000", "GREEN", "#00FF00", "BLUE", "#0000FF");
     * </pre>
     *
     * @param isLinked      If {@code true}, a {@link LinkedHashMap} is created; otherwise, a {@link HashMap}.
     * @param keysAndValues An array of alternating keys and values. Must have an even number of elements.
     * @param <K>           The type of keys in the map.
     * @param <V>           The type of values in the map.
     * @return A new {@link Map} containing the provided key-value pairs.
     * @throws IllegalArgumentException if the number of elements in {@code keysAndValues} is odd.
     * @see org.miaixz.bus.core.center.map.Dictionary#ofKvs(Object...)
     */
    public static <K, V> Map<K, V> ofKvs(final boolean isLinked, final Object... keysAndValues) {
        if (ArrayKit.isEmpty(keysAndValues)) {
            return newHashMap(0, isLinked);
        }

        Assert.isTrue(keysAndValues.length % 2 == 0, "keysAndValues not in pairs!");

        final Map<K, V> map = newHashMap(keysAndValues.length / 2, isLinked);
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
        }
        return map;
    }

    /**
     * Creates a {@link Map} object from an array of {@link Map.Entry} objects.
     *
     * @param <K>     The type of keys in the map.
     * @param <V>     The type of values in the map.
     * @param entries An array of {@link Map.Entry} objects.
     * @return A new {@link Map} containing the provided entries.
     * @see #entry(Object, Object)
     */
    @SafeVarargs
    public static <K, V> Map<K, V> ofEntries(final Map.Entry<K, V>... entries) {
        return ofEntries((Iterator<Entry<K, V>>) new ArrayIterator<>(entries));
    }

    /**
     * Converts an iterable of {@link Map.Entry} objects into a {@link HashMap}.
     *
     * @param <K>       The type of keys in the map.
     * @param <V>       The type of values in the map.
     * @param entryIter An iterable of {@link Map.Entry} objects.
     * @return A new {@link HashMap} containing the entries from the iterable.
     */
    public static <K, V> HashMap<K, V> ofEntries(final Iterable<Entry<K, V>> entryIter) {
        return ofEntries(IteratorKit.getIter(entryIter));
    }

    /**
     * Converts an iterator of {@link Map.Entry} objects into a {@link HashMap}.
     *
     * @param <K>       The type of keys in the map.
     * @param <V>       The type of values in the map.
     * @param entryIter An iterator of {@link Map.Entry} objects.
     * @return A new {@link HashMap} containing the entries from the iterator.
     */
    public static <K, V> HashMap<K, V> ofEntries(final Iterator<Entry<K, V>> entryIter) {
        final HashMap<K, V> map = new HashMap<>();
        if (IteratorKit.isNotEmpty(entryIter)) {
            Entry<K, V> entry;
            while (entryIter.hasNext()) {
                entry = entryIter.next();
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    /**
     * Converts an array into a {@link HashMap}. Supported array element types are:
     * <ul>
     * <li>{@link Map.Entry}</li>
     * <li>Arrays of length greater than 1 (takes the first two elements as key and value). Elements not meeting this
     * condition are skipped.</li>
     * <li>{@link Iterable} with length greater than 1 (takes the first two elements as key and value). Elements not
     * meeting this condition are skipped.</li>
     * <li>{@link Iterator} with length greater than 1 (takes the first two elements as key and value). Elements not
     * meeting this condition are skipped.</li>
     * </ul>
     * <p>
     * Example:
     * 
     * <pre>
     * 
     * Map&lt;Object, Object&gt; colorMap = MapKit
     *         .of(new String[][] { { "RED", "#FF0000" }, { "GREEN", "#00FF00" }, { "BLUE", "#0000FF" } });
     * </pre>
     * <p>
     * Reference: commons-lang
     *
     * @param array The array. Element types can be {@link Map.Entry}, array, {@link Iterable}, or {@link Iterator}.
     * @return A new {@link HashMap} created from the array, or {@code null} if the input array is {@code null}.
     * @throws IllegalArgumentException if an array element is not a supported type or does not have enough elements.
     */
    public static HashMap<Object, Object> of(final Object[] array) {
        if (array == null) {
            return null;
        }
        final HashMap<Object, Object> map = new HashMap<>((int) (array.length * 1.5));
        for (int i = 0; i < array.length; i++) {
            final Object object = array[i];
            if (object instanceof final Map.Entry entry) {
                map.put(entry.getKey(), entry.getValue());
            } else if (object instanceof final Object[] entry) {
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
            } else if (object instanceof final Iterator iter) {
                if (iter.hasNext()) {
                    final Object key = iter.next();
                    if (iter.hasNext()) {
                        final Object value = iter.next();
                        map.put(key, value);
                    }
                }
            } else {
                throw new IllegalArgumentException(StringKit.format(
                        "Array element {}, '{}', is not type of Map.Entry or Array or Iterable or Iterator",
                        i,
                        object));
            }
        }
        return map;
    }

    /**
     * Transforms a list of maps into a single map where common keys have their values merged into a list. This is the
     * inverse operation of {@link #toMapList(Map)}.
     * <p>
     * For example, given the input data:
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
     * The result will be:
     * 
     * <pre>
     * {
     *   a: [1,2,3,4]
     *   b: [1,2,3,]
     *   c: [1]
     * }
     * </pre>
     *
     * @param <K>     The type of keys in the maps.
     * @param <V>     The type of values in the maps.
     * @param mapList An iterable of maps.
     * @return A new map where keys map to lists of values, or an empty map if the input list is empty.
     */
    public static <K, V> Map<K, List<V>> toListMap(final Iterable<? extends Map<K, V>> mapList) {
        final Map<K, List<V>> resultMap = new HashMap<>();
        if (CollKit.isEmpty(mapList)) {
            return resultMap;
        }

        for (final Map<K, V> map : mapList) {
            for (final Entry<K, V> entry : map.entrySet()) {
                resultMap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
            }
        }

        return resultMap;
    }

    /**
     * Transforms a map with list values into a list of maps. Each element in the value lists, along with its
     * corresponding key, forms a new map in the resulting list. This is the inverse operation of
     * {@link #toListMap(Iterable)}.
     * <p>
     * For example, given the input data:
     * 
     * <pre>
     * {
     *   a: [1,2,3,4]
     *   b: [1,2,3,]
     *   c: [1]
     * }
     * </pre>
     * 
     * The result will be:
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
     * @param <K>     The type of keys in the map.
     * @param <V>     The type of values in the lists.
     * @param listMap The map where values are iterables of type V.
     * @return A list of maps, or an empty list if the input map is empty.
     */
    public static <K, V> List<Map<K, V>> toMapList(final Map<K, ? extends Iterable<V>> listMap) {
        if (isEmpty(listMap)) {
            return ListKit.zero();
        }

        final List<Map<K, V>> resultList = new ArrayList<>();
        for (final Entry<K, ? extends Iterable<V>> entry : listMap.entrySet()) {
            final Iterator<V> iterator = IteratorKit.getIter(entry.getValue());
            if (IteratorKit.isEmpty(iterator)) {
                continue;
            }
            final K key = entry.getKey();
            // Add elements to existing maps
            for (final Map<K, V> map : resultList) {
                // If there are more elements to add
                if (iterator.hasNext()) {
                    map.put(key, iterator.next());
                } else {
                    break;
                }
            }
            // If there are more values in the entry's list than existing maps, create new maps
            while (iterator.hasNext()) {
                resultList.add(MapKit.of(key, iterator.next()));
            }
        }

        return resultList;
    }

    /**
     * Converts a given map into a new map where keys are in camel case style. If a key is not a String type, its
     * original value is retained.
     *
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @param map The original map.
     * @return A new map with camel case keys.
     */
    public static <K, V> Map<K, V> toCamelCaseMap(final Map<K, V> map) {
        return (map instanceof LinkedHashMap) ? new CamelCaseLinkedMap<>(map) : new CamelCaseMap<>(map);
    }

    /**
     * Converts a map into a two-dimensional array, where the first dimension represents keys and the second represents
     * values.
     *
     * @param map The map to convert.
     * @return A two-dimensional array of objects, or {@code null} if the input map is {@code null}.
     */
    public static Object[][] toObjectArray(final Map<?, ?> map) {
        if (map == null) {
            return null;
        }
        final Object[][] result = new Object[map.size()][2];
        if (map.isEmpty()) {
            return result;
        }
        int index = 0;
        for (final Entry<?, ?> entry : map.entrySet()) {
            result[index][0] = entry.getKey();
            result[index][1] = entry.getValue();
            index++;
        }
        return result;
    }

    /**
     * Joins the entries of a map into a single string.
     *
     * @param <K>               The type of keys in the map.
     * @param <V>               The type of values in the map.
     * @param map               The map to join.
     * @param separator         The separator string between entries.
     * @param keyValueSeparator The separator string between keys and values.
     * @param args              Additional parameter strings (e.g., a secret key) to append.
     * @return The joined string.
     */
    public static <K, V> String join(
            final Map<K, V> map,
            final String separator,
            final String keyValueSeparator,
            final String... args) {
        return join(map, separator, keyValueSeparator, false, args);
    }

    /**
     * Joins the entries of a map into a single string after sorting them by key. Commonly used for generating
     * signatures.
     *
     * @param params            The map of parameters.
     * @param separator         The separator string between entries.
     * @param keyValueSeparator The separator string between keys and values.
     * @param isIgnoreNull      If {@code true}, null keys and values are ignored.
     * @param args              Additional parameter strings (e.g., a secret key) to append.
     * @return The signature string.
     */
    public static String sortJoin(
            final Map<?, ?> params,
            final String separator,
            final String keyValueSeparator,
            final boolean isIgnoreNull,
            final String... args) {
        return join(sort(params), separator, keyValueSeparator, isIgnoreNull, args);
    }

    /**
     * Joins the entries of a map into a single string, ignoring null keys and values.
     *
     * @param <K>               The type of keys in the map.
     * @param <V>               The type of values in the map.
     * @param map               The map to join.
     * @param separator         The separator string between entries.
     * @param keyValueSeparator The separator string between keys and values.
     * @param args              Additional parameter strings (e.g., a secret key) to append.
     * @return The joined string.
     */
    public static <K, V> String joinIgnoreNull(
            final Map<K, V> map,
            final String separator,
            final String keyValueSeparator,
            final String... args) {
        return join(map, separator, keyValueSeparator, true, args);
    }

    /**
     * Joins the entries of a map into a single string.
     *
     * @param <K>               The type of keys in the map.
     * @param <V>               The type of values in the map.
     * @param map               The map to join. If empty, only {@code args} are joined.
     * @param separator         The separator string between entries.
     * @param keyValueSeparator The separator string between keys and values.
     * @param isIgnoreNull      If {@code true}, null keys and values are ignored.
     * @param args              Additional parameter strings (e.g., a secret key) to append.
     * @return The joined string. Returns an empty string if both map and args are empty.
     */
    public static <K, V> String join(
            final Map<K, V> map,
            final String separator,
            final String keyValueSeparator,
            final boolean isIgnoreNull,
            final String... args) {
        return join(
                map,
                separator,
                keyValueSeparator,
                (entry) -> !isIgnoreNull || entry.getKey() != null && entry.getValue() != null,
                args);
    }

    /**
     * Joins the entries of a map into a single string, applying a predicate to filter entries.
     *
     * @param <K>               The type of keys in the map.
     * @param <V>               The type of values in the map.
     * @param map               The map to join. If empty, only {@code args} are joined.
     * @param separator         The separator string between entries.
     * @param keyValueSeparator The separator string between keys and values.
     * @param predicate         A predicate to filter map entries. Entries for which {@link Predicate#test(Object)}
     *                          returns {@code true} are included.
     * @param args              Additional parameter strings (e.g., a secret key) to append.
     * @return The joined string. Returns an empty string if both map and args are empty.
     */
    public static <K, V> String join(
            final Map<K, V> map,
            final String separator,
            final String keyValueSeparator,
            final Predicate<Entry<K, V>> predicate,
            final String... args) {
        return MapJoiner.of(separator, keyValueSeparator).append(map, predicate).append(args).toString();
    }

    /**
     * Edits the entries of a map using a provided {@link UnaryOperator}. The editor can filter out entries (by
     * returning {@code null}) or modify them.
     *
     * @param <K>    The type of keys in the map.
     * @param <V>    The type of values in the map.
     * @param map    The original map.
     * @param editor The editor function that transforms each entry. If it returns {@code null}, the entry is discarded.
     * @return A new map with the edited entries.
     */
    public static <K, V> Map<K, V> edit(final Map<K, V> map, final UnaryOperator<Entry<K, V>> editor) {
        if (null == map || null == editor) {
            return map;
        }

        final Map<K, V> map2 = createMap(map.getClass(), () -> new HashMap<>(map.size(), 1f));
        if (isEmpty(map)) {
            return map2;
        }

        Entry<K, V> modified;
        for (final Entry<K, V> entry : map.entrySet()) {
            modified = editor.apply(entry);
            if (null != modified) {
                map2.put(modified.getKey(), modified.getValue());
            }
        }
        return map2;
    }

    /**
     * Filters the entries of a map using a provided {@link Predicate}.
     *
     * @param <K>       The type of keys in the map.
     * @param <V>       The type of values in the map.
     * @param map       The original map.
     * @param predicate The filter predicate. Entries for which {@link Predicate#test(Object)} returns {@code true} are
     *                  retained. If {@code null}, the original map is returned.
     * @return A new map containing only the filtered entries.
     */
    public static <K, V> Map<K, V> filter(final Map<K, V> map, final Predicate<Entry<K, V>> predicate) {
        if (null == map || null == predicate) {
            return map;
        }
        return edit(map, t -> predicate.test(t) ? t : null);
    }

    /**
     * Transforms the values of a map using a provided {@link BiFunction} to create a new map with potentially different
     * value types.
     *
     * @param map        The original map.
     * @param biFunction The {@link BiFunction} that takes a key and a value from the original map and returns a new
     *                   value for the resulting map.
     * @param <K>        The type of keys in the map.
     * @param <V>        The type of values in the original map.
     * @param <R>        The type of values in the resulting map.
     * @return A new map with transformed values, or an empty map if the input map or function is null.
     */
    public static <K, V, R> Map<K, R> map(final Map<K, V> map, final BiFunction<K, V, R> biFunction) {
        if (null == map || null == biFunction) {
            return MapKit.newHashMap();
        }
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, m -> biFunction.apply(m.getKey(), m.getValue())));
    }

    /**
     * Filters a map to retain only the entries whose keys are present in the given key list.
     *
     * @param <K>  The type of keys in the map.
     * @param <V>  The type of values in the map.
     * @param map  The original map.
     * @param keys The list of keys to retain. If {@code null}, the original map is returned.
     * @return A new map containing only the entries with the specified keys. The type of the resulting map matches the
     *         original map.
     */
    public static <K, V> Map<K, V> filter(final Map<K, V> map, final K... keys) {
        if (null == map || null == keys) {
            return map;
        }

        final Map<K, V> map2 = createMap(map.getClass(), () -> new HashMap<>(map.size(), 1f));
        if (isEmpty(map)) {
            return map2;
        }

        for (final K key : keys) {
            if (map.containsKey(key)) {
                map2.put(key, map.get(key));
            }
        }
        return map2;
    }

    /**
     * Swaps the keys and values of a map. The resulting map will have values as keys and keys as values. If there are
     * duplicate values in the original map, the last encountered key for that value will overwrite previous ones. The
     * order of overwriting in a {@link HashMap} is not guaranteed, but in ordered maps, it follows insertion order.
     *
     * @param <T> The common type of keys and values in the original map.
     * @param map The map object whose keys and values are to be swapped. Keys and values must be of the same type.
     * @return A new map with keys and values swapped.
     * @see #inverse(Map)
     */
    public static <T> Map<T, T> reverse(final Map<T, T> map) {
        return edit(map, t -> new Entry<>() {

            @Override
            public T getKey() {
                return t.getValue();
            }

            @Override
            public T getValue() {
                return t.getKey();
            }

            @Override
            public T setValue(final T value) {
                throw new UnsupportedOperationException("Unsupported setValue method !");
            }
        });
    }

    /**
     * Swaps the keys and values of a map. The resulting map will have values as keys and keys as values. If there are
     * duplicate values in the original map, the last encountered key for that value will overwrite previous ones. The
     * order of overwriting in a {@link HashMap} is not guaranteed, but in ordered maps, it follows insertion order.
     *
     * @param <K> The type of keys in the original map.
     * @param <V> The type of values in the original map.
     * @param map The map object whose keys and values are to be swapped.
     * @return A new map with keys and values swapped.
     */
    public static <K, V> Map<V, K> inverse(final Map<K, V> map) {
        final Map<V, K> result = createMap(map.getClass());
        map.forEach((key, value) -> result.put(value, key));
        return result;
    }

    /**
     * Sorts an existing map by its keys using the default natural ordering (alphabetical order).
     *
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @param map The map to sort.
     * @return A new {@link TreeMap} with sorted keys, or {@code null} if the input map is {@code null}.
     * @see #newTreeMap(Map, Comparator)
     */
    public static <K, V> TreeMap<K, V> sort(final Map<K, V> map) {
        return sort(map, null);
    }

    /**
     * Sorts an existing map by its keys using a custom comparator.
     *
     * @param <K>        The type of keys in the map.
     * @param <V>        The type of values in the map.
     * @param map        The map to sort. If {@code null}, returns {@code null}.
     * @param comparator The comparator to order the keys. If {@code null}, natural ordering is used.
     * @return A new {@link TreeMap} with sorted keys, or {@code null} if the input map is {@code null}.
     * @see #newTreeMap(Map, Comparator)
     */
    public static <K, V> TreeMap<K, V> sort(final Map<K, V> map, final Comparator<? super K> comparator) {
        if (null == map) {
            return null;
        }

        if (map instanceof final TreeMap<K, V> result) {
            // If it's already a sortable map, return the original only if the comparator is consistent.
            if (null == comparator || comparator.equals(result.comparator())) {
                return result;
            }
        }

        return newTreeMap(map, comparator);
    }

    /**
     * Sorts a map by its values, with an option for descending order.
     *
     * @param map    The map whose values are to be sorted.
     * @param <K>    The type of keys in the map.
     * @param <V>    The type of values in the map, which must be comparable.
     * @param isDesc If {@code true}, values are sorted in descending order; otherwise, ascending order.
     * @return A new {@link LinkedHashMap} with entries sorted by value.
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
            final Map<K, V> map,
            final boolean isDesc) {
        final Map<K, V> result = new LinkedHashMap<>();
        Comparator<Entry<K, V>> entryComparator = Entry.comparingByValue();
        if (isDesc) {
            entryComparator = entryComparator.reversed();
        }
        map.entrySet().stream().sorted(entryComparator).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    /**
     * Creates a {@link MapProxy} to wrap a given map, providing various {@code getXXX} methods for convenient access.
     *
     * @param map The map to be proxied.
     * @return A new {@link MapProxy} instance.
     */
    public static MapProxy createProxy(final Map<?, ?> map) {
        return MapProxy.of(map);
    }

    /**
     * Creates a {@link MapWrapper} to wrap a given map.
     *
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @param map The map to be wrapped.
     * @return A new {@link MapWrapper} instance.
     */
    public static <K, V> MapWrapper<K, V> wrap(final Map<K, V> map) {
        return new MapWrapper<>(map);
    }

    /**
     * Returns an unmodifiable view of the given map.
     *
     * @param map The map to make unmodifiable.
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @return An unmodifiable {@link Map}.
     */
    public static <K, V> Map<K, V> view(final Map<K, V> map) {
        return Collections.unmodifiableMap(map);
    }

    /**
     * Creates a new {@link MapBuilder} for building maps with a fluent API.
     *
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @return A new {@link MapBuilder} instance, initialized with a {@link HashMap}.
     */
    public static <K, V> MapBuilder<K, V> builder() {
        return builder(new HashMap<>());
    }

    /**
     * Creates a new {@link MapBuilder} for building maps with a fluent API, using a provided map as the underlying
     * storage.
     *
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @param map The actual map to be used by the builder.
     * @return A new {@link MapBuilder} instance.
     */
    public static <K, V> MapBuilder<K, V> builder(final Map<K, V> map) {
        return new MapBuilder<>(map);
    }

    /**
     * Creates a new {@link MapBuilder} initialized with a single key-value pair.
     *
     * @param <K> The type of the key.
     * @param <V> The type of the value.
     * @param k   The key.
     * @param v   The value.
     * @return A new {@link MapBuilder} instance, initialized with a {@link HashMap} containing the given key-value
     *         pair.
     */
    public static <K, V> MapBuilder<K, V> builder(final K k, final V v) {
        return (builder(new HashMap<K, V>())).put(k, v);
    }

    /**
     * Retrieves a new map containing only the entries whose keys are present in the given key list.
     *
     * @param <K>  The type of keys in the map.
     * @param <V>  The type of values in the map.
     * @param map  The original map.
     * @param keys The list of keys to retrieve.
     * @return A new map containing only the specified keys and their corresponding values.
     */
    public static <K, V> Map<K, V> getAny(final Map<K, V> map, final K... keys) {
        return filter(map, entry -> ArrayKit.contains(keys, entry.getKey()));
    }

    /**
     * Removes key-value pairs from the given map based on a list of keys. The original map is modified.
     *
     * @param <K>  The type of keys in the map.
     * @param <V>  The type of values in the map.
     * @param <T>  The concrete type of the map.
     * @param map  The map from which to remove entries.
     * @param keys The list of keys to remove.
     * @return The modified map.
     */
    public static <K, V, T extends Map<K, V>> T removeAny(final T map, final K... keys) {
        for (final K key : keys) {
            map.remove(key);
        }
        return map;
    }

    /**
     * Renames a key in the map. This is achieved by removing the old key and re-inserting the value with the new key.
     * If the old key does not exist, the map remains unchanged. If the new key already exists, an
     * {@link IllegalArgumentException} is thrown.
     *
     * @param <K>    The type of keys in the map.
     * @param <V>    The type of values in the map.
     * @param map    The map to modify.
     * @param oldKey The existing key to rename.
     * @param newKey The new key name.
     * @return The modified map.
     * @throws IllegalArgumentException If the new key already exists in the map.
     */
    public static <K, V> Map<K, V> renameKey(final Map<K, V> map, final K oldKey, final K newKey) {
        if (isNotEmpty(map) && map.containsKey(oldKey)) {
            if (map.containsKey(newKey)) {
                throw new IllegalArgumentException(StringKit.format("The data '{}' exist !", newKey));
            }
            map.put(newKey, map.remove(oldKey));
        }
        return map;
    }

    /**
     * Removes all key-value pairs from the map where the value is {@code null}. The original map is modified directly.
     *
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @param map The map to modify.
     * @return The modified map.
     */
    public static <K, V> Map<K, V> removeNullValue(final Map<K, V> map) {
        return removeIf(map, entry -> null == entry.getValue());
    }

    /**
     * Removes all key-value pairs from the map where the value matches the given value. The original map is modified
     * directly.
     *
     * @param <K>   The type of keys in the map.
     * @param <V>   The type of values in the map.
     * @param map   The map to modify.
     * @param value The value to match for removal.
     * @return The modified map.
     */
    public static <K, V> Map<K, V> removeByValue(final Map<K, V> map, final V value) {
        return removeIf(map, entry -> ObjectKit.equals(value, entry.getValue()));
    }

    /**
     * Removes key-value pairs from the map based on a provided predicate. The original map is modified directly.
     *
     * @param <K>       The type of keys in the map.
     * @param <V>       The type of values in the map.
     * @param map       The map to modify.
     * @param predicate The removal condition. Entries for which {@link Predicate#test(Object)} returns {@code true} are
     *                  removed.
     * @return The modified map.
     */
    public static <K, V> Map<K, V> removeIf(final Map<K, V> map, final Predicate<Entry<K, V>> predicate) {
        if (isEmpty(map)) {
            return map;
        }
        map.entrySet().removeIf(predicate);
        return map;
    }

    /**
     * Returns an empty, unmodifiable map.
     *
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @return An empty, unmodifiable {@link Map}.
     * @see Collections#emptyMap()
     */
    public static <K, V> Map<K, V> empty() {
        return Collections.emptyMap();
    }

    /**
     * Returns a new {@link HashMap} with an initial capacity of 0. This map can have elements added to it.
     *
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @return A new {@link HashMap} with an initial capacity of 0.
     */
    public static <K, V> Map<K, V> zero() {
        return new HashMap<>(0, 1);
    }

    /**
     * Returns an unmodifiable map containing only a single key-value pair.
     *
     * @param key   The key.
     * @param value The value.
     * @param <K>   The type of the key.
     * @param <V>   The type of the value.
     * @return An unmodifiable {@link Map} with a single entry.
     */
    public static <K, V> Map<K, V> singleton(final K key, final V value) {
        return Collections.singletonMap(key, value);
    }

    /**
     * Returns an empty map of the specified type. Supported types include {@link NavigableMap}, {@link SortedMap}, and
     * {@link Map}.
     *
     * @param <K>      The type of keys in the map.
     * @param <V>      The type of values in the map.
     * @param <T>      The concrete type of the map.
     * @param mapClass The class of the map type. If {@code null}, a default empty {@link Map} is returned.
     * @return An empty map of the specified type.
     * @throws IllegalArgumentException if the provided map type is not supported for empty collections.
     */
    public static <K, V, T extends Map<K, V>> T empty(final Class<?> mapClass) {
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

        // Unsupported collection type for empty collections
        throw new IllegalArgumentException(StringKit.format("[{}] is not support to get empty!", mapClass));
    }

    /**
     * Clears the elements of one or more map collections by calling the {@code clear()} method on each.
     *
     * @param maps One or more maps to clear.
     */
    public static void clear(final Map<?, ?>... maps) {
        for (final Map<?, ?> map : maps) {
            if (isNotEmpty(map)) {
                map.clear();
            }
        }
    }

    /**
     * Retrieves a list of values from a map corresponding to the given list of keys. If a key does not exist in the map
     * or its corresponding value is {@code null}, the value at that position in the result list will also be
     * {@code null}.
     *
     * @param <K>  The type of keys in the map.
     * @param <V>  The type of values in the map.
     * @param map  The {@link Map} to retrieve values from.
     * @param keys The array of keys.
     * @return A {@link List} of values corresponding to the provided keys.
     */
    public static <K, V> List<V> valuesOfKeys(final Map<K, V> map, final K... keys) {
        return valuesOfKeys(map, (Iterator<K>) new ArrayIterator<>(keys));
    }

    /**
     * Retrieves a list of values from a map corresponding to the given iterable of keys. If a key does not exist in the
     * map or its corresponding value is {@code null}, the value at that position in the result list will also be
     * {@code null}.
     *
     * @param <K>  The type of keys in the map.
     * @param <V>  The type of values in the map.
     * @param map  The {@link Map} to retrieve values from.
     * @param keys The iterable of keys.
     * @return A {@link List} of values corresponding to the provided keys.
     */
    public static <K, V> List<V> valuesOfKeys(final Map<K, V> map, final Iterable<K> keys) {
        return valuesOfKeys(map, keys.iterator());
    }

    /**
     * Retrieves a list of values from a map corresponding to the given iterator of keys. If a key does not exist in the
     * map or its corresponding value is {@code null}, the value at that position in the result list will also be
     * {@code null}.
     *
     * @param <K>  The type of keys in the map.
     * @param <V>  The type of values in the map.
     * @param map  The {@link Map} to retrieve values from.
     * @param keys The iterator of keys.
     * @return A {@link List} of values corresponding to the provided keys.
     */
    public static <K, V> List<V> valuesOfKeys(final Map<K, V> map, final Iterator<K> keys) {
        final List<V> list = new ArrayList<>();
        while (keys.hasNext()) {
            list.add(map.get(keys.next()));
        }
        return list;
    }

    /**
     * Converts a key and a value into an unmodifiable {@link AbstractMap.SimpleImmutableEntry}.
     *
     * @param key   The key.
     * @param value The value.
     * @param <K>   The type of the key.
     * @param <V>   The type of the value.
     * @return An unmodifiable {@link AbstractMap.SimpleImmutableEntry}.
     */
    public static <K, V> Map.Entry<K, V> entry(final K key, final V value) {
        return entry(key, value, true);
    }

    /**
     * Converts a key and a value into either an {@link AbstractMap.SimpleEntry} or an
     * {@link AbstractMap.SimpleImmutableEntry}.
     *
     * @param key         The key.
     * @param value       The value.
     * @param <K>         The type of the key.
     * @param <V>         The type of the value.
     * @param isImmutable If {@code true}, an unmodifiable {@link AbstractMap.SimpleImmutableEntry} is returned;
     *                    otherwise, a modifiable {@link AbstractMap.SimpleEntry} is returned.
     * @return An {@link AbstractMap.SimpleEntry} or {@link AbstractMap.SimpleImmutableEntry}.
     */
    public static <K, V> Map.Entry<K, V> entry(final K key, final V value, final boolean isImmutable) {
        return isImmutable ? new AbstractMap.SimpleImmutableEntry<>(key, value)
                : new AbstractMap.SimpleEntry<>(key, value);
    }

    /**
     * Populates a map with entries generated from an iterable of values, using a key mapper function.
     *
     * @param resultMap The map to populate. If {@code null}, a new {@link HashMap} is used.
     * @param iterable  The iterable of values.
     * @param keyMapper A function that maps each value to its corresponding key.
     * @param <K>       The type of keys in the map.
     * @param <V>       The type of values in the map.
     * @return The populated map.
     */
    public static <K, V> Map<K, V> putAll(
            final Map<K, V> resultMap,
            final Iterable<V> iterable,
            final Function<V, K> keyMapper) {
        return putAll(resultMap, iterable, keyMapper, Function.identity());
    }

    /**
     * Populates a map with entries generated from an iterable of objects, using key and value mapper functions.
     *
     * @param resultMap   The map to populate. The type of this map determines the type of the result.
     * @param iterable    The iterable of objects.
     * @param keyMapper   A function that maps each object to its corresponding key.
     * @param valueMapper A function that maps each object to its corresponding value.
     * @param <T>         The type of objects in the iterable.
     * @param <K>         The type of keys in the map.
     * @param <V>         The type of values in the map.
     * @return The populated map.
     */
    public static <T, K, V> Map<K, V> putAll(
            final Map<K, V> resultMap,
            final Iterable<T> iterable,
            final Function<T, K> keyMapper,
            final Function<T, V> valueMapper) {
        return putAll(resultMap, IteratorKit.getIter(iterable), keyMapper, valueMapper);
    }

    /**
     * Populates a map with entries generated from an iterator of values, using a key mapper function.
     *
     * @param resultMap The map to populate. If {@code null}, a new {@link HashMap} is used.
     * @param iterator  The iterator of values.
     * @param keyMapper A function that maps each value to its corresponding key.
     * @param <K>       The type of keys in the map.
     * @param <V>       The type of values in the map.
     * @return The populated map.
     */
    public static <K, V> Map<K, V> putAll(
            final Map<K, V> resultMap,
            final Iterator<V> iterator,
            final Function<V, K> keyMapper) {
        return putAll(resultMap, iterator, keyMapper, Function.identity());
    }

    /**
     * Populates a map with entries generated from an iterator of objects, using key and value mapper functions.
     *
     * @param resultMap   The map to populate. If {@code null}, a new {@link HashMap} is used.
     * @param iterator    The iterator of objects.
     * @param keyMapper   A function that maps each object to its corresponding key.
     * @param valueMapper A function that maps each object to its corresponding value.
     * @param <T>         The type of objects in the iterator.
     * @param <K>         The type of keys in the map.
     * @param <V>         The type of values in the map.
     * @return The populated map.
     */
    public static <T, K, V> Map<K, V> putAll(
            Map<K, V> resultMap,
            final Iterator<T> iterator,
            final Function<T, K> keyMapper,
            final Function<T, V> valueMapper) {
        if (null == resultMap) {
            resultMap = MapKit.newHashMap();
        }
        if (ObjectKit.isNull(iterator)) {
            return resultMap;
        }

        T value;
        while (iterator.hasNext()) {
            value = iterator.next();
            resultMap.put(keyMapper.apply(value), valueMapper.apply(value));
        }
        return resultMap;
    }

    /**
     * Groups a list of map entries by their keys.
     *
     * @param <K>     The type of keys in the entries.
     * @param <V>     The type of values in the entries.
     * @param entries The iterable of map entries to group.
     * @return A new map where each key maps to a list of its corresponding values.
     */
    public static <K, V> Map<K, List<V>> grouping(final Iterable<Map.Entry<K, V>> entries) {
        if (CollKit.isEmpty(entries)) {
            return zero();
        }

        final Map<K, List<V>> map = new HashMap<>();
        for (final Map.Entry<K, V> pair : entries) {
            final List<V> values = map.computeIfAbsent(pair.getKey(), k -> new ArrayList<>());
            values.add(pair.getValue());
        }
        return map;
    }

    /**
     * Partitions a map into a list of sub-maps, each with a maximum specified size.
     *
     * @param <K>  The type of keys in the map.
     * @param <V>  The type of values in the map.
     * @param map  The map to partition.
     * @param size The maximum size of each sub-map. Must be greater than 0.
     * @return A list of sub-maps.
     * @throws IllegalArgumentException if {@code size} is less than or equal to 0.
     * @throws NullPointerException     if {@code map} is {@code null}.
     */
    public static <K, V> List<Map<K, V>> partition(final Map<K, V> map, final int size) {
        Assert.notNull(map);
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        final List<Map<K, V>> list = new ArrayList<>();
        final Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map<K, V> subMap = new HashMap<>(size);
            for (int i = 0; i < size && iterator.hasNext(); i++) {
                final Map.Entry<K, V> entry = iterator.next();
                subMap.put(entry.getKey(), entry.getValue());
            }
            list.add(subMap);
        }
        return list;
    }

    /**
     * Iterates through the map and returns the value of the first entry that matches the given predicate.
     *
     * @param map       The map to iterate.
     * @param predicate The predicate to test each entry.
     * @param <K>       The type of keys in the map.
     * @param <V>       The type of values in the map.
     * @return The value of the first matching entry, or {@code null} if no match is found or the map is empty.
     */
    public static <K, V> V firstMatchValue(final Map<K, V> map, final Predicate<Entry<K, V>> predicate) {
        final Entry<K, V> kvEntry = firstMatch(map, predicate);
        if (null != kvEntry) {
            return kvEntry.getValue();
        }
        return null;
    }

    /**
     * Iterates through the map and returns the first entry that matches the given predicate.
     *
     * @param map       The map to iterate.
     * @param predicate The predicate to test each entry.
     * @param <K>       The type of keys in the map.
     * @param <V>       The type of values in the map.
     * @return The first matching entry, or {@code null} if no match is found or the map is empty.
     */
    public static <K, V> Entry<K, V> firstMatch(final Map<K, V> map, final Predicate<Entry<K, V>> predicate) {
        if (isNotEmpty(map)) {
            for (final Entry<K, V> entry : map.entrySet()) {
                if (predicate.test(entry)) {
                    return entry;
                }
            }
        }
        return null;
    }

    /**
     * Iterates over the entries of a map, providing the index, key, and value to a {@link Consumer3X}. Unlike
     * {@link Map#forEach(BiConsumer)}, this method provides an index for each entry.
     *
     * @param <K>        The type of keys in the map.
     * @param <V>        The type of values in the map.
     * @param map        The {@link Map} to iterate.
     * @param kvConsumer The {@link Consumer3X} to process each entry, receiving the index, key, and value.
     */
    public static <K, V> void forEach(final Map<K, V> map, final Consumer3X<Integer, K, V> kvConsumer) {
        if (map == null) {
            return;
        }
        int index = 0;
        for (final Entry<K, V> entry : map.entrySet()) {
            kvConsumer.accept(index, entry.getKey(), entry.getValue());
            index++;
        }
    }

    /**
     * Flattens a multi-level map into a single-level map.
     *
     * @param map The input multi-level map.
     * @param <K> The type of keys in the map.
     * @param <V> The type of values in the map.
     * @return A new single-level map containing all entries from the multi-level map.
     */
    public static <K, V> Map<K, V> flatten(final Map<K, V> map) {
        return flatten(map, new HashMap<>());
    }

    /**
     * Recursively flattens a multi-level map into a single-level map.
     *
     * @param map     The input multi-level map.
     * @param flatMap The map to store the flattened result. If {@code null}, a new {@link HashMap} is created.
     * @param <K>     The type of keys in the map.
     * @param <V>     The type of values in the map.
     * @return The single-level map containing all entries from the multi-level map.
     * @throws NullPointerException if the input {@code map} is {@code null}.
     */
    public static <K, V> Map<K, V> flatten(final Map<K, V> map, Map<K, V> flatMap) {
        Assert.notNull(map);
        if (null == flatMap) {
            flatMap = new HashMap<>();
        }

        final Map<K, V> finalFlatMap = flatMap;
        map.forEach((k, v) -> {
            // Avoid nested loops
            if (v instanceof Map && v != map) {
                flatten((Map<K, V>) v, finalFlatMap);
            } else {
                finalFlatMap.put(k, v);
            }
        });

        return flatMap;
    }

    /**
     * Retrieves the first non-null value from a map based on a prioritized list of keys.
     * <p>
     * This method checks multiple keys in the map in the specified order of priority, returning the value associated
     * with the first key that has a non-null value. It automatically optimizes for performance by choosing between
     * sequential or parallel streams based on the number of keys.
     *
     * @param <K>  The type of keys in the map.
     * @param <V>  The type of values in the map.
     * @param map  The parameter map, can be {@code null}.
     * @param keys The list of keys to check, in order of priority, can be {@code null}.
     * @return The first non-null value found, or {@code null} if no non-null value is found or parameters are invalid.
     */
    public static <K, V> V getFirstNonNull(final Map<K, V> map, final K... keys) {
        // Fail fast: if map is null or keys are null/empty, return null directly.
        if (map == null || keys == null || keys.length == 0) {
            return null;
        }

        // Optimization: if map is empty, return null directly.
        if (map.isEmpty()) {
            return null;
        }

        // For a small number of keys, a sequential stream is more efficient.
        if (keys.length < 10) {
            return Arrays.stream(keys).filter(Objects::nonNull).map(map::get).filter(Objects::nonNull).findFirst()
                    .orElse(null);
        }

        // For a large number of keys, a parallel stream can be more efficient.
        return Arrays.stream(keys).parallel().filter(Objects::nonNull).map(map::get).filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

}
