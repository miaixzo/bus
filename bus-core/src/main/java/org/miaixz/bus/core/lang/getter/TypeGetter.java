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
package org.miaixz.bus.core.lang.getter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.miaixz.bus.core.convert.CompositeConverter;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.convert.Converter;

/**
 * Interface for retrieving values of various basic types. This interface provides a unified definition for methods to
 * retrieve values, including:
 * <ul>
 * <li>Object</li>
 * <li>String</li>
 * <li>Integer</li>
 * <li>Short</li>
 * <li>Boolean</li>
 * <li>Long</li>
 * <li>Character</li>
 * <li>Float</li>
 * <li>Double</li>
 * <li>Byte</li>
 * <li>BigDecimal</li>
 * <li>BigInteger</li>
 * <li>Enum</li>
 * <li>Number</li>
 * <li>Date</li>
 * <li>java.sql.Time</li>
 * <li>java.sql.Timestamp</li>
 * <li>LocalDateTime</li>
 * <li>LocalDate</li>
 * <li>LocalTime</li>
 * </ul>
 * Implementations of this interface can most simply provide a concrete implementation for
 * {@link #getObject(Object, Object)} to enable all type-specific getter methods, which by default use {@link Convert}
 * for automatic type conversion. Custom implementations can override specific {@code getXXX} methods as needed.
 *
 * @param <K> The type of the key.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface TypeGetter<K> {

    /**
     * Retrieves an Object property value.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as an Object, or the default value if not found.
     */
    Object getObject(K key, Object defaultValue);

    /**
     * Retrieves an Object property value without any type conversion or validation.
     *
     * @param key The key of the property.
     * @return The property value as an Object, or null if not found.
     */
    default Object getObject(final K key) {
        return getObject(key, null);
    }

    /**
     * Retrieves a value of the specified type, with automatic type conversion.
     *
     * @param <T>  The target type.
     * @param key  The key of the property.
     * @param type The target type.
     * @return The converted value, or null if the property is not found or conversion fails.
     */
    default <T> T get(final K key, final Type type) {
        return get(key, type, null);
    }

    /**
     * Retrieves a value of the specified type, with automatic type conversion.
     *
     * @param <T>          The target type.
     * @param key          The key of the property.
     * @param type         The target type.
     * @param defaultValue The default value to return if the property is not found or conversion fails.
     * @return The converted value, or the default value if not found or conversion fails.
     */
    default <T> T get(final K key, final Type type, final T defaultValue) {
        return get(key, type, CompositeConverter.getInstance(), defaultValue);
    }

    /**
     * Retrieves a value of the specified type, with automatic type conversion using a custom converter.
     *
     * @param <T>          The target type.
     * @param key          The key of the property.
     * @param type         The target type.
     * @param converter    The custom converter to use for type conversion.
     * @param defaultValue The default value to return if the property is not found or conversion fails.
     * @return The converted value, or the default value if not found or conversion fails.
     */
    default <T> T get(final K key, final Type type, final Converter converter, final T defaultValue) {
        return converter.convert(type, getObject(key), defaultValue);
    }

    /**
     * Retrieves a String property value. If the retrieved value is an invisible character, the default value is used.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found, is null, or is an invisible
     *                     character.
     * @return The property value as a String, or the default value.
     */
    default String getString(final K key, final String defaultValue) {
        return get(key, String.class, defaultValue);
    }

    /**
     * Retrieves a String property value.
     *
     * @param key The key of the property.
     * @return The property value as a String, or null if not found.
     */
    default String getString(final K key) {
        return getString(key, null);
    }

    /**
     * Retrieves an Integer property value. If the retrieved value is an invisible character, the default value is used.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found, is null, or is an invisible
     *                     character.
     * @return The property value as an Integer, or the default value.
     */
    default Integer getInt(final K key, final Integer defaultValue) {
        return get(key, Integer.class, defaultValue);
    }

    /**
     * Retrieves an Integer property value.
     *
     * @param key The key of the property.
     * @return The property value as an Integer, or null if not found.
     */
    default Integer getInt(final K key) {
        return getInt(key, null);
    }

    /**
     * Retrieves a Short property value. If the retrieved value is an invisible character, the default value is used.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found, is null, or is an invisible
     *                     character.
     * @return The property value as a Short, or the default value.
     */
    default Short getShort(final K key, final Short defaultValue) {
        return get(key, Short.class, defaultValue);
    }

    /**
     * Retrieves a Short property value.
     *
     * @param key The key of the property.
     * @return The property value as a Short, or null if not found.
     */
    default Short getShort(final K key) {
        return getShort(key, null);
    }

    /**
     * Retrieves a Boolean property value. If the retrieved value is an invisible character, the default value is used.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found, is null, or is an invisible
     *                     character.
     * @return The property value as a Boolean, or the default value.
     */
    default Boolean getBoolean(final K key, final Boolean defaultValue) {
        return get(key, Boolean.class, defaultValue);
    }

    /**
     * Retrieves a Boolean property value.
     *
     * @param key The key of the property.
     * @return The property value as a Boolean, or null if not found.
     */
    default Boolean getBoolean(final K key) {
        return getBoolean(key, null);
    }

    /**
     * Retrieves a Long property value. If the retrieved value is an invisible character, the default value is used.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found, is null, or is an invisible
     *                     character.
     * @return The property value as a Long, or the default value.
     */
    default Long getLong(final K key, final Long defaultValue) {
        return get(key, Long.class, defaultValue);
    }

    /**
     * Retrieves a Long property value.
     *
     * @param key The key of the property.
     * @return The property value as a Long, or null if not found.
     */
    default Long getLong(final K key) {
        return getLong(key, null);
    }

    /**
     * Retrieves a Character property value. If the retrieved value is an invisible character, the default value is
     * used.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found, is null, or is an invisible
     *                     character.
     * @return The property value as a Character, or the default value.
     */
    default Character getChar(final K key, final Character defaultValue) {
        return get(key, Character.class, defaultValue);
    }

    /**
     * Retrieves a Character property value.
     *
     * @param key The key of the property.
     * @return The property value as a Character, or null if not found.
     */
    default Character getChar(final K key) {
        return getChar(key, null);
    }

    /**
     * Retrieves a Float property value. If the retrieved value is an invisible character, the default value is used.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found, is null, or is an invisible
     *                     character.
     * @return The property value as a Float, or the default value.
     */
    default Float getFloat(final K key, final Float defaultValue) {
        return get(key, Float.class, defaultValue);
    }

    /**
     * Retrieves a Float property value.
     *
     * @param key The key of the property.
     * @return The property value as a Float, or null if not found.
     */
    default Float getFloat(final K key) {
        return getFloat(key, null);
    }

    /**
     * Retrieves a Double property value. If the retrieved value is an invisible character, the default value is used.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found, is null, or is an invisible
     *                     character.
     * @return The property value as a Double, or the default value.
     */
    default Double getDouble(final K key, final Double defaultValue) {
        return get(key, Double.class, defaultValue);
    }

    /**
     * Retrieves a Double property value.
     *
     * @param key The key of the property.
     * @return The property value as a Double, or null if not found.
     */
    default Double getDouble(final K key) {
        return getDouble(key, null);
    }

    /**
     * Retrieves a Byte property value.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found, is null, or is an invisible
     *                     character.
     * @return The property value as a Byte, or the default value.
     */
    default Byte getByte(final K key, final Byte defaultValue) {
        return get(key, Byte.class, defaultValue);
    }

    /**
     * Retrieves a Byte property value.
     *
     * @param key The key of the property.
     * @return The property value as a Byte, or null if not found.
     */
    default Byte getByte(final K key) {
        return getByte(key, null);
    }

    /**
     * Retrieves a byte array property value.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a byte array, or the default value.
     */
    default byte[] getBytes(final K key, final byte[] defaultValue) {
        return get(key, byte[].class, defaultValue);
    }

    /**
     * Retrieves a byte array property value.
     *
     * @param key The key of the property.
     * @return The property value as a byte array, or null if not found.
     */
    default byte[] getBytes(final K key) {
        return getBytes(key, null);
    }

    /**
     * Retrieves a BigDecimal property value. If the retrieved value is an invisible character, the default value is
     * used.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found, is null, or is an invisible
     *                     character.
     * @return The property value as a BigDecimal, or the default value.
     */
    default BigDecimal getBigDecimal(final K key, final BigDecimal defaultValue) {
        return get(key, BigDecimal.class, defaultValue);
    }

    /**
     * Retrieves a BigDecimal property value.
     *
     * @param key The key of the property.
     * @return The property value as a BigDecimal, or null if not found.
     */
    default BigDecimal getBigDecimal(final K key) {
        return getBigDecimal(key, null);
    }

    /**
     * Retrieves a BigInteger property value. If the retrieved value is an invisible character, the default value is
     * used.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found, is null, or is an invisible
     *                     character.
     * @return The property value as a BigInteger, or the default value.
     */
    default BigInteger getBigInteger(final K key, final BigInteger defaultValue) {
        return get(key, BigInteger.class, defaultValue);
    }

    /**
     * Retrieves a BigInteger property value.
     *
     * @param key The key of the property.
     * @return The property value as a BigInteger, or null if not found.
     */
    default BigInteger getBigInteger(final K key) {
        return getBigInteger(key, null);
    }

    /**
     * Retrieves an Enum value of the specified enum class.
     *
     * @param <E>          The enum type.
     * @param clazz        The Class object of the enum.
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The enum value, or the default value.
     */
    default <E extends Enum<E>> E getEnum(final Class<E> clazz, final K key, final E defaultValue) {
        return get(key, clazz, defaultValue);
    }

    /**
     * Retrieves an Enum value of the specified enum class.
     *
     * @param <E>   The enum type.
     * @param clazz The Class object of the enum.
     * @param key   The key of the property.
     * @return The enum value, or null if not found.
     */
    default <E extends Enum<E>> E getEnum(final Class<E> clazz, final K key) {
        return getEnum(clazz, key, null);
    }

    /**
     * Retrieves a Number property value.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a Number, or the default value.
     */
    default Number getNumber(final K key, final Number defaultValue) {
        return get(key, Number.class, defaultValue);
    }

    /**
     * Retrieves a Number property value.
     *
     * @param key The key of the property.
     * @return The property value as a Number, or null if not found.
     */
    default Number getNumber(final K key) {
        return getNumber(key, null);
    }

    /**
     * Retrieves a Date property value.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a Date, or the default value.
     */
    default Date getDate(final K key, final Date defaultValue) {
        return get(key, Date.class, defaultValue);
    }

    /**
     * Retrieves a Date property value.
     *
     * @param key The key of the property.
     * @return The property value as a Date, or null if not found.
     */
    default Date getDate(final K key) {
        return getDate(key, null);
    }

    /**
     * Retrieves a java.sql.Time property value.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a java.sql.Time, or the default value.
     */
    default Time getSqlTime(final K key, final Time defaultValue) {
        return get(key, Time.class, defaultValue);
    }

    /**
     * Retrieves a java.sql.Time property value.
     *
     * @param key The key of the property.
     * @return The property value as a java.sql.Time, or null if not found.
     */
    default Time getSqlTime(final K key) {
        return getSqlTime(key, null);
    }

    /**
     * Retrieves a java.sql.Timestamp property value.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a java.sql.Timestamp, or the default value.
     */
    default Timestamp getSqlTimestamp(final K key, final Timestamp defaultValue) {
        return get(key, Timestamp.class, defaultValue);
    }

    /**
     * Retrieves a java.sql.Timestamp property value.
     *
     * @param key The key of the property.
     * @return The property value as a java.sql.Timestamp, or null if not found.
     */
    default Timestamp getSqlTimestamp(final K key) {
        return getSqlTimestamp(key, null);
    }

    /**
     * Retrieves a LocalDateTime property value.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a LocalDateTime, or the default value.
     */
    default LocalDateTime getLocalDateTime(final K key, final LocalDateTime defaultValue) {
        return get(key, LocalDateTime.class, defaultValue);
    }

    /**
     * Retrieves a LocalDateTime property value.
     *
     * @param key The key of the property.
     * @return The property value as a LocalDateTime, or null if not found.
     */
    default LocalDateTime getLocalDateTime(final K key) {
        return getLocalDateTime(key, null);
    }

    /**
     * Retrieves a LocalDate property value.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a LocalDate, or the default value.
     */
    default LocalDate getLocalDate(final K key, final LocalDate defaultValue) {
        return get(key, LocalDate.class, defaultValue);
    }

    /**
     * Retrieves a LocalDate property value.
     *
     * @param key The key of the property.
     * @return The property value as a LocalDate, or null if not found.
     */
    default LocalDate getLocalDate(final K key) {
        return getLocalDate(key, null);
    }

    /**
     * Retrieves a LocalTime property value.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value to return if the property is not found or is null.
     * @return The property value as a LocalTime, or the default value.
     */
    default LocalTime getLocalTime(final K key, final LocalTime defaultValue) {
        return get(key, LocalTime.class, defaultValue);
    }

    /**
     * Retrieves a LocalTime property value.
     *
     * @param key The key of the property.
     * @return The property value as a LocalTime, or null if not found.
     */
    default LocalTime getLocalTime(final K key) {
        return getLocalTime(key, null);
    }

}
