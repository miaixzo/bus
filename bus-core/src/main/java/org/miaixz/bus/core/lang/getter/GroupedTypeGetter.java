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
package org.miaixz.bus.core.lang.getter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.miaixz.bus.core.convert.Convert;

/**
 * Interface for retrieving grouped type values, providing methods to get values of various types from a grouped data
 * structure. This interface allows specifying a group key in addition to the regular key.
 *
 * @param <K> The type of the key.
 * @param <G> The type of the group key.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface GroupedTypeGetter<K, G> {

    /**
     * Retrieves an Object property value without any type conversion or validation.
     *
     * @param key          The key of the property.
     * @param group        The group key.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as an Object, or the default value if not found.
     */
    Object getObjectByGroup(K key, G group, Object defaultValue);

    /**
     * Retrieves an Object property value without any type conversion or validation.
     *
     * @param key   The key of the property.
     * @param group The group key.
     * @return The property value as an Object, or null if not found.
     */
    default Object getObjectByGroup(final K key, final G group) {
        return getObjectByGroup(key, group, null);
    }

    /**
     * Retrieves a value of the specified type, with automatic type conversion.
     *
     * @param <T>   The target type.
     * @param key   The key of the property.
     * @param group The group key.
     * @param type  The target type.
     * @return The converted value, or null if the property is not found or conversion fails.
     */
    default <T> T getByGroup(final K key, final G group, final Type type) {
        return getByGroup(key, group, type, null);
    }

    /**
     * Retrieves a value of the specified type, with automatic type conversion.
     *
     * @param <T>          The target type.
     * @param key          The key of the property.
     * @param group        The group key.
     * @param type         The target type.
     * @param defaultValue The default value to return if the property is not found or conversion fails.
     * @return The converted value, or the default value if not found or conversion fails.
     */
    default <T> T getByGroup(final K key, final G group, final Type type, final T defaultValue) {
        return Convert.convert(type, getObjectByGroup(key, group), defaultValue);
    }

    /**
     * Retrieves a String property value.
     *
     * @param key          The key of the property.
     * @param group        The group key.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a String, or the default value if not found.
     */
    default String getStringByGroup(final K key, final G group, final String defaultValue) {
        return getByGroup(key, group, String.class, defaultValue);
    }

    /**
     * Retrieves a String property value.
     *
     * @param key   The key of the property.
     * @param group The group key.
     * @return The property value as a String, or null if not found.
     */
    default String getStringByGroup(final K key, final G group) {
        return getStringByGroup(key, group, null);
    }

    /**
     * Retrieves an Integer property value.
     *
     * @param key          The key of the property.
     * @param group        The group key.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as an Integer, or the default value if not found.
     */
    default Integer getIntByGroup(final K key, final G group, final Integer defaultValue) {
        return getByGroup(key, group, Integer.class, defaultValue);
    }

    /**
     * Retrieves an Integer property value.
     *
     * @param key   The key of the property.
     * @param group The group key.
     * @return The property value as an Integer, or null if not found.
     */
    default Integer getIntByGroup(final K key, final G group) {
        return getIntByGroup(key, group, null);
    }

    /**
     * Retrieves a Short property value.
     *
     * @param key          The key of the property.
     * @param group        The group key.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a Short, or the default value if not found.
     */
    default Short getShortByGroup(final K key, final G group, final Short defaultValue) {
        return getByGroup(key, group, Short.class, defaultValue);
    }

    /**
     * Retrieves a Short property value.
     *
     * @param key   The key of the property.
     * @param group The group key.
     * @return The property value as a Short, or null if not found.
     */
    default Short getShortByGroup(final K key, final G group) {
        return getShortByGroup(key, group, null);
    }

    /**
     * Retrieves a Boolean property value.
     *
     * @param key          The key of the property.
     * @param group        The group key.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a Boolean, or the default value if not found.
     */
    default Boolean getBooleanByGroup(final K key, final G group, final Boolean defaultValue) {
        return getByGroup(key, group, Boolean.class, defaultValue);
    }

    /**
     * Retrieves a Boolean property value.
     *
     * @param key   The key of the property.
     * @param group The group key.
     * @return The property value as a Boolean, or null if not found.
     */
    default Boolean getBooleanByGroup(final K key, final G group) {
        return getBooleanByGroup(key, group, null);
    }

    /**
     * Retrieves a Long property value.
     *
     * @param key          The key of the property.
     * @param group        The group key.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a Long, or the default value if not found.
     */
    default Long getLongByGroup(final K key, final G group, final Long defaultValue) {
        return getByGroup(key, group, Long.class, defaultValue);
    }

    /**
     * Retrieves a Long property value.
     *
     * @param key   The key of the property.
     * @param group The group key.
     * @return The property value as a Long, or null if not found.
     */
    default Long getLongByGroup(final K key, final G group) {
        return getLongByGroup(key, group, null);
    }

    /**
     * Retrieves a Character property value.
     *
     * @param key          The key of the property.
     * @param group        The group key.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a Character, or the default value if not found.
     */
    default Character getCharByGroup(final K key, final G group, final Character defaultValue) {
        return getByGroup(key, group, Character.class, defaultValue);
    }

    /**
     * Retrieves a Character property value.
     *
     * @param key   The key of the property.
     * @param group The group key.
     * @return The property value as a Character, or null if not found.
     */
    default Character getCharByGroup(final K key, final G group) {
        return getCharByGroup(key, group, null);
    }

    /**
     * Retrieves a Double property value.
     *
     * @param key          The key of the property.
     * @param group        The group key.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a Double, or the default value if not found.
     */
    default Double getDoubleByGroup(final K key, final G group, final Double defaultValue) {
        return getByGroup(key, group, Double.class, defaultValue);
    }

    /**
     * Retrieves a Double property value.
     *
     * @param key   The key of the property.
     * @param group The group key.
     * @return The property value as a Double, or null if not found.
     */
    default Double getDoubleByGroup(final K key, final G group) {
        return getDoubleByGroup(key, group, null);
    }

    /**
     * Retrieves a Byte property value.
     *
     * @param key          The key of the property.
     * @param group        The group key.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a Byte, or the default value if not found.
     */
    default Byte getByteByGroup(final K key, final G group, final Byte defaultValue) {
        return getByGroup(key, group, Byte.class, defaultValue);
    }

    /**
     * Retrieves a Byte property value.
     *
     * @param key   The key of the property.
     * @param group The group key.
     * @return The property value as a Byte, or null if not found.
     */
    default Byte getByteByGroup(final K key, final G group) {
        return getByteByGroup(key, group, null);
    }

    /**
     * Retrieves a BigDecimal property value.
     *
     * @param key          The key of the property.
     * @param group        The group key.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a BigDecimal, or the default value if not found.
     */
    default BigDecimal getBigDecimalByGroup(final K key, final G group, final BigDecimal defaultValue) {
        return getByGroup(key, group, BigDecimal.class, defaultValue);
    }

    /**
     * Retrieves a BigDecimal property value.
     *
     * @param key   The key of the property.
     * @param group The group key.
     * @return The property value as a BigDecimal, or null if not found.
     */
    default BigDecimal getBigDecimalByGroup(final K key, final G group) {
        return getBigDecimalByGroup(key, group, null);
    }

    /**
     * Retrieves a BigInteger property value.
     *
     * @param key          The key of the property.
     * @param group        The group key.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a BigInteger, or the default value if not found.
     */
    default BigInteger getBigIntegerByGroup(final K key, final G group, final BigInteger defaultValue) {
        return getByGroup(key, group, BigInteger.class, defaultValue);
    }

    /**
     * Retrieves a BigInteger property value.
     *
     * @param key   The key of the property.
     * @param group The group key.
     * @return The property value as a BigInteger, or null if not found.
     */
    default BigInteger getBigIntegerByGroup(final K key, final G group) {
        return getBigIntegerByGroup(key, group, null);
    }

}
