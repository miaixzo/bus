/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.xyz;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;

/**
 * Type casting utility. Provides methods for up-casting and down-casting generic collections, maps, etc.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CastKit {

    /**
     * Constructs a new CastKit. Utility class constructor for static access.
     */
    private CastKit() {
    }

    /**
     * Forcefully casts an object to a specified type.
     *
     * @param <T>   The target type.
     * @param value The object to be cast.
     * @return The cast object.
     */
    public static <T> T cast(final Object value) {
        return (T) value;
    }

    /**
     * Forcefully casts an object to a specified type.
     *
     * @param <T>        The target type.
     * @param targetType The target class to cast to.
     * @param value      The object to be cast.
     * @return The cast object.
     */
    public static <T> T castTo(final Class<T> targetType, final Object value) {
        return Assert.notNull(targetType).cast(value);
    }

    /**
     * Up-casts a generic collection. For example, converts a {@code Collection<Integer>} to a
     * {@code Collection<Number>}.
     *
     * @param <T>        The target element type (superclass).
     * @param collection The collection to cast.
     * @return The cast collection.
     */
    public static <T> Collection<T> castUp(final Collection<? extends T> collection) {
        return (Collection<T>) collection;
    }

    /**
     * Down-casts a generic collection. For example, converts a {@code Collection<Number>} to a
     * {@code Collection<Integer>}.
     *
     * @param <T>        The target element type (subclass).
     * @param collection The collection to cast.
     * @return The cast collection.
     */
    public static <T> Collection<T> castDown(final Collection<? super T> collection) {
        return (Collection<T>) collection;
    }

    /**
     * Up-casts a generic set. For example, converts a {@code Set<Integer>} to a {@code Set<Number>}.
     *
     * @param <T> The target element type (superclass).
     * @param set The set to cast.
     * @return The cast set.
     */
    public static <T> Set<T> castUp(final Set<? extends T> set) {
        return (Set<T>) set;
    }

    /**
     * Down-casts a generic set. For example, converts a {@code Set<Number>} to a {@code Set<Integer>}.
     *
     * @param <T> The target element type (subclass).
     * @param set The set to cast.
     * @return The cast set.
     */
    public static <T> Set<T> castDown(final Set<? super T> set) {
        return (Set<T>) set;
    }

    /**
     * Up-casts a generic list. For example, converts a {@code List<Integer>} to a {@code List<Number>}.
     *
     * @param <T>  The target element type (superclass).
     * @param list The list to cast.
     * @return The cast list.
     */
    public static <T> List<T> castUp(final List<? extends T> list) {
        return (List<T>) list;
    }

    /**
     * Down-casts a generic list. For example, converts a {@code List<Number>} to a {@code List<Integer>}.
     *
     * @param <T>  The target element type (subclass).
     * @param list The list to cast.
     * @return The cast list.
     */
    public static <T> List<T> castDown(final List<? super T> list) {
        return (List<T>) list;
    }

    /**
     * Up-casts a generic map. For example, converts a {@code Map<Integer, Integer>} to a {@code Map<Number, Number>}.
     *
     * @param <K> The target key type (superclass).
     * @param <V> The target value type (superclass).
     * @param map The map to cast.
     * @return The cast map.
     */
    public static <K, V> Map<K, V> castUp(final Map<? extends K, ? extends V> map) {
        return (Map<K, V>) map;
    }

    /**
     * Down-casts a generic map. For example, converts a {@code Map<Number, Number>} to a {@code Map<Integer, Integer>}.
     *
     * @param <K> The target key type (subclass).
     * @param <V> The target value type (subclass).
     * @param map The map to cast.
     * @return The cast map.
     */
    public static <K, V> Map<K, V> castDown(final Map<? super K, ? super V> map) {
        return (Map<K, V>) map;
    }

}
