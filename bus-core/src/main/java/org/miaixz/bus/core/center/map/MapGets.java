/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.map;

import java.util.Date;
import java.util.Map;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.reflect.TypeReference;

/**
 * Provides utility methods for safely retrieving and converting values from a {@link Map}. This class extends
 * {@link MapValidator} and offers a suite of {@code getXXX} methods for common data types, simplifying data extraction
 * and type conversion.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapGets extends MapValidator {

    /**
     * Retrieves the value for the specified key and converts it to a {@code String}.
     *
     * @param map The map to query.
     * @param key The key whose associated value is to be returned.
     * @return The value as a {@code String}, or {@code null} if the key is not found.
     */
    public static String getString(final Map<?, ?> map, final Object key) {
        return get(map, key, String.class);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code String}, returning a default value if not
     * found.
     *
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param defaultValue The default value to return if the key is not found or the value is {@code null}.
     * @return The value as a {@code String}, or the default value.
     */
    public static String getString(final Map<?, ?> map, final Object key, final String defaultValue) {
        return get(map, key, String.class, defaultValue);
    }

    /**
     * Retrieves the value for the specified key and converts it to an {@code Integer}.
     *
     * @param map The map to query.
     * @param key The key whose associated value is to be returned.
     * @return The value as an {@code Integer}, or {@code null} if the key is not found.
     */
    public static Integer getInt(final Map<?, ?> map, final Object key) {
        return get(map, key, Integer.class);
    }

    /**
     * Retrieves the value for the specified key and converts it to an {@code Integer}, returning a default value if not
     * found.
     *
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param defaultValue The default value to return if the key is not found or the value is {@code null}.
     * @return The value as an {@code Integer}, or the default value.
     */
    public static Integer getInt(final Map<?, ?> map, final Object key, final Integer defaultValue) {
        return get(map, key, Integer.class, defaultValue);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code Double}.
     *
     * @param map The map to query.
     * @param key The key whose associated value is to be returned.
     * @return The value as a {@code Double}, or {@code null} if the key is not found.
     */
    public static Double getDouble(final Map<?, ?> map, final Object key) {
        return get(map, key, Double.class);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code Double}, returning a default value if not
     * found.
     *
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param defaultValue The default value to return if the key is not found or the value is {@code null}.
     * @return The value as a {@code Double}, or the default value.
     */
    public static Double getDouble(final Map<?, ?> map, final Object key, final Double defaultValue) {
        return get(map, key, Double.class, defaultValue);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code Float}.
     *
     * @param map The map to query.
     * @param key The key whose associated value is to be returned.
     * @return The value as a {@code Float}, or {@code null} if the key is not found.
     */
    public static Float getFloat(final Map<?, ?> map, final Object key) {
        return get(map, key, Float.class);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code Float}, returning a default value if not
     * found.
     *
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param defaultValue The default value to return if the key is not found or the value is {@code null}.
     * @return The value as a {@code Float}, or the default value.
     */
    public static Float getFloat(final Map<?, ?> map, final Object key, final Float defaultValue) {
        return get(map, key, Float.class, defaultValue);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code Short}.
     *
     * @param map The map to query.
     * @param key The key whose associated value is to be returned.
     * @return The value as a {@code Short}, or {@code null} if the key is not found.
     */
    public static Short getShort(final Map<?, ?> map, final Object key) {
        return get(map, key, Short.class);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code Short}, returning a default value if not
     * found.
     *
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param defaultValue The default value to return if the key is not found or the value is {@code null}.
     * @return The value as a {@code Short}, or the default value.
     */
    public static Short getShort(final Map<?, ?> map, final Object key, final Short defaultValue) {
        return get(map, key, Short.class, defaultValue);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code Boolean}.
     *
     * @param map The map to query.
     * @param key The key whose associated value is to be returned.
     * @return The value as a {@code Boolean}, or {@code null} if the key is not found.
     */
    public static Boolean getBoolean(final Map<?, ?> map, final Object key) {
        return get(map, key, Boolean.class);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code Boolean}, returning a default value if not
     * found.
     *
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param defaultValue The default value to return if the key is not found or the value is {@code null}.
     * @return The value as a {@code Boolean}, or the default value.
     */
    public static Boolean getBoolean(final Map<?, ?> map, final Object key, final Boolean defaultValue) {
        return get(map, key, Boolean.class, defaultValue);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code Character}.
     *
     * @param map The map to query.
     * @param key The key whose associated value is to be returned.
     * @return The value as a {@code Character}, or {@code null} if the key is not found.
     */
    public static Character getChar(final Map<?, ?> map, final Object key) {
        return get(map, key, Character.class);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code Character}, returning a default value if
     * not found.
     *
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param defaultValue The default value to return if the key is not found or the value is {@code null}.
     * @return The value as a {@code Character}, or the default value.
     */
    public static Character getChar(final Map<?, ?> map, final Object key, final Character defaultValue) {
        return get(map, key, Character.class, defaultValue);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code Long}.
     *
     * @param map The map to query.
     * @param key The key whose associated value is to be returned.
     * @return The value as a {@code Long}, or {@code null} if the key is not found.
     */
    public static Long getLong(final Map<?, ?> map, final Object key) {
        return get(map, key, Long.class);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@code Long}, returning a default value if not
     * found.
     *
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param defaultValue The default value to return if the key is not found or the value is {@code null}.
     * @return The value as a {@code Long}, or the default value.
     */
    public static Long getLong(final Map<?, ?> map, final Object key, final Long defaultValue) {
        return get(map, key, Long.class, defaultValue);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@link Date}.
     *
     * @param map The map to query.
     * @param key The key whose associated value is to be returned.
     * @return The value as a {@link Date}, or {@code null} if the key is not found.
     */
    public static Date getDate(final Map<?, ?> map, final Object key) {
        return get(map, key, Date.class);
    }

    /**
     * Retrieves the value for the specified key and converts it to a {@link Date}, returning a default value if not
     * found.
     *
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param defaultValue The default value to return if the key is not found or the value is {@code null}.
     * @return The value as a {@link Date}, or the default value.
     */
    public static Date getDate(final Map<?, ?> map, final Object key, final Date defaultValue) {
        return get(map, key, Date.class, defaultValue);
    }

    /**
     * Retrieves the value for the specified key and converts it to the specified type.
     *
     * @param <T>  The target type.
     * @param map  The map to query.
     * @param key  The key whose associated value is to be returned.
     * @param type The {@code Class} of the target type.
     * @return The converted value, or {@code null} if the key is not found.
     */
    public static <T> T get(final Map<?, ?> map, final Object key, final Class<T> type) {
        return get(map, key, type, null);
    }

    /**
     * Retrieves the value for the specified key and converts it to the specified type, returning a default value if not
     * found.
     *
     * @param <T>          The target type.
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param type         The {@code Class} of the target type.
     * @param defaultValue The default value to return if the key is not found or the value is {@code null}.
     * @return The converted value, or the default value.
     */
    public static <T> T get(final Map<?, ?> map, final Object key, final Class<T> type, final T defaultValue) {
        return null == map ? defaultValue : Convert.convert(type, map.get(key), defaultValue);
    }

    /**
     * Retrieves and converts a value from the map, suppressing any conversion exceptions.
     *
     * @param <T>          The target type.
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param type         The {@code Class} of the target type.
     * @param defaultValue The default value to return if the key is not found, the value is {@code null}, or conversion
     *                     fails.
     * @return The converted value, or the default value on failure.
     */
    public static <T> T getQuietly(final Map<?, ?> map, final Object key, final Class<T> type, final T defaultValue) {
        return null == map ? defaultValue : Convert.convertQuietly(type, map.get(key), defaultValue);
    }

    /**
     * Retrieves the value for a key and converts it to a generic type specified by a {@link TypeReference}.
     *
     * @param <T>  The target generic type.
     * @param map  The map to query.
     * @param key  The key whose associated value is to be returned.
     * @param type A {@link TypeReference} describing the target type.
     * @return The converted value, or {@code null} if the key is not found.
     */
    public static <T> T get(final Map<?, ?> map, final Object key, final TypeReference<T> type) {
        return get(map, key, type, null);
    }

    /**
     * Retrieves and converts a value to a generic type, returning a default value if not found.
     *
     * @param <T>          The target generic type.
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param type         A {@link TypeReference} describing the target type.
     * @param defaultValue The default value to return if the key is not found or the value is {@code null}.
     * @return The converted value, or the default value.
     */
    public static <T> T get(final Map<?, ?> map, final Object key, final TypeReference<T> type, final T defaultValue) {
        return null == map ? defaultValue : Convert.convert(type, map.get(key), defaultValue);
    }

    /**
     * Retrieves and converts a value to a generic type, suppressing any conversion exceptions.
     *
     * @param <T>          The target generic type.
     * @param map          The map to query.
     * @param key          The key whose associated value is to be returned.
     * @param type         A {@link TypeReference} describing the target type.
     * @param defaultValue The default value to return if the key is not found, the value is {@code null}, or conversion
     *                     fails.
     * @return The converted value, or the default value on failure.
     */
    public static <T> T getQuietly(
            final Map<?, ?> map,
            final Object key,
            final TypeReference<T> type,
            final T defaultValue) {
        return null == map ? defaultValue : Convert.convertQuietly(type, map.get(key), defaultValue);
    }

}
