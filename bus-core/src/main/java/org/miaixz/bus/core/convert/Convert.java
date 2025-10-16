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
package org.miaixz.bus.core.convert;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.math.ChineseNumberFormatter;
import org.miaixz.bus.core.math.ChineseNumberParser;
import org.miaixz.bus.core.math.EnglishNumberFormatter;
import org.miaixz.bus.core.xyz.*;

/**
 * Type Converter utility.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Convert {

    /**
     * Converts the given value to a {@code String}. If the value is null or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static String toString(final Object value, final String defaultValue) {
        return convertQuietly(String.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code String}. If the value is `null` or conversion fails, `null` is returned. No
     * exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static String toString(final Object value) {
        return toString(value, null);
    }

    /**
     * Converts the given value to a {@code String}. If the value is `null` or conversion fails, the string "null" is
     * returned. No exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static String toStringOrNull(final Object value) {
        return toString(value, Normal.NULL);
    }

    /**
     * Converts the given value to a `String` array.
     *
     * @param value The value to be converted.
     * @return The `String` array.
     */
    public static String[] toStringArray(final Object value) {
        return convert(String[].class, value);
    }

    /**
     * Converts the given value to a {@code Character}. If the value is null or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static Character toChar(final Object value, final Character defaultValue) {
        return convertQuietly(Character.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code Character}. If the value is `null` or conversion fails, `null` is returned.
     * No exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Character toChar(final Object value) {
        return toChar(value, null);
    }

    /**
     * Converts the given value to a `Character` array.
     *
     * @param value The value to be converted.
     * @return The `Character` array.
     */
    public static Character[] toCharArray(final Object value) {
        return convert(Character[].class, value);
    }

    /**
     * Converts the given value to a {@code Byte}. If the value is `null` or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static Byte toByte(final Object value, final Byte defaultValue) {
        return convertQuietly(Byte.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code Byte}. If the value is `null` or conversion fails, `null` is returned. No
     * exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Byte toByte(final Object value) {
        return toByte(value, null);
    }

    /**
     * Converts the given value to a `Byte` array.
     *
     * @param value The value to be converted.
     * @return The `Byte` array.
     */
    public static Byte[] toByteArray(final Object value) {
        return convert(Byte[].class, value);
    }

    /**
     * Converts the given value to a primitive `byte` array.
     *
     * @param value The value to be converted.
     * @return The primitive `byte` array.
     */
    public static byte[] toPrimitiveByteArray(final Object value) {
        return convert(byte[].class, value);
    }

    /**
     * Converts the given value to a {@code Short}. If the value is `null` or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static Short toShort(final Object value, final Short defaultValue) {
        return convertQuietly(Short.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code Short}. If the value is `null` or conversion fails, `null` is returned. No
     * exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Short toShort(final Object value) {
        return toShort(value, null);
    }

    /**
     * Converts the given value to a `Short` array.
     *
     * @param value The value to be converted.
     * @return The `Short` array.
     */
    public static Short[] toShortArray(final Object value) {
        return convert(Short[].class, value);
    }

    /**
     * Converts the given value to a {@code Number}. If the value is empty or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static Number toNumber(final Object value, final Number defaultValue) {
        return convertQuietly(Number.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code Number}. If the value is empty or conversion fails, `null` is returned. No
     * exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Number toNumber(final Object value) {
        return toNumber(value, null);
    }

    /**
     * Converts the given value to a `Number` array.
     *
     * @param value The value to be converted.
     * @return The `Number` array.
     */
    public static Number[] toNumberArray(final Object value) {
        return convert(Number[].class, value);
    }

    /**
     * Converts the given value to an {@code Integer}. If the value is empty or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static Integer toInt(final Object value, final Integer defaultValue) {
        return convertQuietly(Integer.class, value, defaultValue);
    }

    /**
     * Converts the given value to an {@code Integer}. If the value is `null` or conversion fails, `null` is returned.
     * No exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Integer toInt(final Object value) {
        return toInt(value, null);
    }

    /**
     * Converts the given value to an `Integer` array.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Integer[] toIntArray(final Object value) {
        return convert(Integer[].class, value);
    }

    /**
     * Converts the given value to a {@code Long}. If the value is empty or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static Long toLong(final Object value, final Long defaultValue) {
        return convertQuietly(Long.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code Long}. If the value is `null` or conversion fails, `null` is returned. No
     * exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Long toLong(final Object value) {
        return toLong(value, null);
    }

    /**
     * Converts the given value to a `Long` array.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Long[] toLongArray(final Object value) {
        return convert(Long[].class, value);
    }

    /**
     * Converts the given value to a {@code Double}. If the value is empty or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static Double toDouble(final Object value, final Double defaultValue) {
        return convertQuietly(Double.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code Double}. If the value is empty or conversion fails, `null` is returned. No
     * exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Double toDouble(final Object value) {
        return toDouble(value, null);
    }

    /**
     * Converts the given value to a `Double` array.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Double[] toDoubleArray(final Object value) {
        return convert(Double[].class, value);
    }

    /**
     * Converts the given value to a {@code Float}. If the value is empty or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static Float toFloat(final Object value, final Float defaultValue) {
        return convertQuietly(Float.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code Float}. If the value is empty or conversion fails, `null` is returned. No
     * exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Float toFloat(final Object value) {
        return toFloat(value, null);
    }

    /**
     * Converts the given value to a `Float` array.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Float[] toFloatArray(final Object value) {
        return convert(Float[].class, value);
    }

    /**
     * Converts the given value to a {@code Boolean}. Supported `String` values are: "true", "false", "yes", "ok", "no",
     * "1", "0". If the value is empty or conversion fails, the default value is returned. No exceptions are thrown on
     * failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static Boolean toBoolean(final Object value, final Boolean defaultValue) {
        return convertQuietly(Boolean.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code Boolean}. If the value is empty or conversion fails, `null` is returned. No
     * exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Boolean toBoolean(final Object value) {
        return toBoolean(value, null);
    }

    /**
     * Converts the given value to a `Boolean` array.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Boolean[] toBooleanArray(final Object value) {
        return convert(Boolean[].class, value);
    }

    /**
     * Converts the given value to a {@code BigInteger}. If the value is empty or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static BigInteger toBigInteger(final Object value, final BigInteger defaultValue) {
        return convertQuietly(BigInteger.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code BigInteger}. If the value is empty or conversion fails, `null` is returned.
     * No exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static BigInteger toBigInteger(final Object value) {
        return toBigInteger(value, null);
    }

    /**
     * Converts the given value to a {@code BigDecimal}. If the value is empty or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static BigDecimal toBigDecimal(final Object value, final BigDecimal defaultValue) {
        return convertQuietly(BigDecimal.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code BigDecimal}. If the value is empty or conversion fails, `null` is returned.
     * No exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static BigDecimal toBigDecimal(final Object value) {
        return toBigDecimal(value, null);
    }

    /**
     * Converts the given value to a {@code Date}. If the value is empty or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static Date toDate(final Object value, final Date defaultValue) {
        return convertQuietly(Date.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code LocalDateTime}. If the value is empty or conversion fails, the default value
     * is returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static LocalDateTime toLocalDateTime(final Object value, final LocalDateTime defaultValue) {
        return convertQuietly(LocalDateTime.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code LocalDateTime}. If the value is empty or conversion fails, `null` is
     * returned. No exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static LocalDateTime toLocalDateTime(final Object value) {
        return toLocalDateTime(value, null);
    }

    /**
     * Converts the given value to an {@code Instant}. If the value is empty or conversion fails, the default value is
     * returned. No exceptions are thrown on failure.
     *
     * @param value        The value to be converted.
     * @param defaultValue The default value to return on conversion error.
     * @return The converted result.
     */
    public static Instant toInstant(final Object value, final Instant defaultValue) {
        return convertQuietly(Instant.class, value, defaultValue);
    }

    /**
     * Converts the given value to a {@code Date}. If the value is empty or conversion fails, `null` is returned. No
     * exceptions are thrown on failure.
     *
     * @param value The value to be converted.
     * @return The converted result.
     */
    public static Date toDate(final Object value) {
        return toDate(value, null);
    }

    /**
     * Converts the given value to an {@code Enum} of the specified class.
     *
     * @param <E>          The enum type.
     * @param clazz        The `Enum` class.
     * @param value        The value.
     * @param defaultValue The default value to return on failure.
     * @return The `Enum` constant.
     */
    public static <E extends Enum<E>> E toEnum(final Class<E> clazz, final Object value, final E defaultValue) {
        try {
            return EnumConverter.INSTANCE.convert(clazz, value);
        } catch (final Exception ignore) {
            return defaultValue;
        }
    }

    /**
     * Converts the given value to an {@code Enum} of the specified class. Returns `null` on failure.
     *
     * @param <E>   The enum type.
     * @param clazz The `Enum` class.
     * @param value The value.
     * @return The `Enum` constant.
     */
    public static <E extends Enum<E>> E toEnum(final Class<E> clazz, final Object value) {
        return toEnum(clazz, value, null);
    }

    /**
     * Converts the given value to a `Collection`.
     *
     * @param collectionType The target collection type.
     * @param elementType    The element type of the collection.
     * @param value          The value to be converted.
     * @return A {@link Collection}.
     */
    public static Collection<?> toCollection(
            final Class<?> collectionType,
            final Class<?> elementType,
            final Object value) {
        return new CollectionConverter().convert(collectionType, elementType, value);
    }

    /**
     * Converts the given value to a `List`.
     *
     * @param value The value to be converted.
     * @return A {@link List}.
     */
    public static List<?> toList(final Object value) {
        return convert(List.class, value);
    }

    /**
     * Converts the given value to a `List` with a specified element type.
     *
     * @param <T>         The element type.
     * @param elementType The element type class.
     * @param value       The value to be converted.
     * @return An {@link ArrayList}.
     */
    public static <T> List<T> toList(final Class<T> elementType, final Object value) {
        return (List<T>) toCollection(ArrayList.class, elementType, value);
    }

    /**
     * Converts the given value to a `Set` with a specified element type.
     *
     * @param <T>         The element type.
     * @param elementType The element type class.
     * @param value       The value to be converted.
     * @return A {@link HashSet}.
     */
    public static <T> Set<T> toSet(final Class<T> elementType, final Object value) {
        return (Set<T>) toCollection(HashSet.class, elementType, value);
    }

    /**
     * Converts the given value to a `Map`.
     *
     * @param <K>       The key type.
     * @param <V>       The value type.
     * @param keyType   The key type class.
     * @param valueType The value type class.
     * @param value     The value to be converted.
     * @return A {@link Map}.
     */
    public static <K, V> Map<K, V> toMap(final Class<K> keyType, final Class<V> valueType, final Object value) {
        if (value instanceof Map) {
            return toMap(value.getClass(), keyType, valueType, value);
        } else {
            return toMap(HashMap.class, keyType, valueType, value);
        }
    }

    /**
     * Converts the given value to a specific `Map` type.
     *
     * @param <K>       The key type.
     * @param <V>       The value type.
     * @param mapType   The target `Map` class.
     * @param keyType   The key type class.
     * @param valueType The value type class.
     * @param value     The value to be converted.
     * @return A {@link Map}.
     */
    public static <K, V> Map<K, V> toMap(
            final Class<?> mapType,
            final Class<K> keyType,
            final Class<V> valueType,
            final Object value) {
        return (Map<K, V>) MapConverter.INSTANCE.convert(mapType, keyType, valueType, value);
    }

    /**
     * Converts a value to a specified type using its class name.
     *
     * @param <T>       The target type.
     * @param className The string representation of the class.
     * @param value     The value.
     * @return The converted value.
     * @throws ConvertException if no suitable converter is found.
     */
    public static <T> T convertByClassName(final String className, final Object value) throws ConvertException {
        return convert(ClassKit.loadClass(className), value);
    }

    /**
     * Converts a value to a specified type.
     *
     * @param <T>   The target type.
     * @param type  The type.
     * @param value The value.
     * @return The converted value.
     * @throws ConvertException if no suitable converter is found.
     */
    public static <T> T convert(final Class<T> type, final Object value) throws ConvertException {
        return convert((Type) type, value);
    }

    /**
     * Converts a value to a specified generic type using a `TypeReference`.
     *
     * @param <T>       The target type.
     * @param reference The type reference holding the generic type information.
     * @param value     The value.
     * @return The converted value.
     * @throws ConvertException if no suitable converter is found.
     */
    public static <T> T convert(final TypeReference<T> reference, final Object value) throws ConvertException {
        return convert(reference.getType(), value, null);
    }

    /**
     * Converts a value to a specified type.
     *
     * @param <T>   The target type.
     * @param type  The type.
     * @param value The value.
     * @return The converted value.
     * @throws ConvertException if no suitable converter is found.
     */
    public static <T> T convert(final Type type, final Object value) throws ConvertException {
        return convert(type, value, null);
    }

    /**
     * Converts a value to a specified type, returning a default value on failure.
     *
     * @param <T>          The target type.
     * @param type         The type.
     * @param value        The value.
     * @param defaultValue The default value.
     * @return The converted value.
     * @throws ConvertException if no suitable converter is found.
     */
    public static <T> T convert(final Class<T> type, final Object value, final T defaultValue) throws ConvertException {
        return convert((Type) type, value, defaultValue);
    }

    /**
     * Converts a value to a specified type, returning a default value on failure.
     *
     * @param <T>          The target type.
     * @param type         The type.
     * @param value        The value.
     * @param defaultValue The default value.
     * @return The converted value.
     * @throws ConvertException if no suitable converter is found.
     */
    public static <T> T convert(final Type type, final Object value, final T defaultValue) throws ConvertException {
        return convertWithCheck(type, value, defaultValue, false);
    }

    /**
     * Converts a value to a specified type quietly (without throwing exceptions).
     *
     * @param <T>   The target type.
     * @param type  The target type.
     * @param value The value.
     * @return The converted value, or `null` on failure.
     */
    public static <T> T convertQuietly(final Type type, final Object value) {
        return convertQuietly(type, value, null);
    }

    /**
     * Converts a value to a specified type quietly, returning a default value on failure.
     *
     * @param <T>          The target type.
     * @param type         The target type.
     * @param value        The value.
     * @param defaultValue The default value.
     * @return The converted value.
     */
    public static <T> T convertQuietly(final Type type, final Object value, final T defaultValue) {
        return convertWithCheck(type, value, defaultValue, true);
    }

    /**
     * Converts a value to a specified type, with an option for quiet conversion.
     *
     * @param <T>          The target type.
     * @param type         The target type.
     * @param value        The value.
     * @param defaultValue The default value.
     * @param quietly      If `true`, returns the default value on failure instead of throwing an exception.
     * @return The converted value.
     */
    public static <T> T convertWithCheck(
            final Type type,
            final Object value,
            final T defaultValue,
            final boolean quietly) {
        final CompositeConverter compositeConverter = CompositeConverter.getInstance();
        try {
            return compositeConverter.convert(type, value, defaultValue);
        } catch (final Exception e) {
            if (quietly) {
                return defaultValue;
            }
            throw e;
        }
    }

    /**
     * Converts half-width characters (SBCS) to full-width characters (DBCS).
     *
     * @param input The string.
     * @return The full-width string.
     */
    public static String toSBC(final String input) {
        return toSBC(input, null);
    }

    /**
     * Converts half-width characters to full-width characters, optionally skipping some characters.
     *
     * @param input         The string.
     * @param notConvertSet A set of characters to not convert.
     * @return The full-width string.
     */
    public static String toSBC(final String input, final Set<Character> notConvertSet) {
        if (StringKit.isEmpty(input)) {
            return input;
        }
        final char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (null != notConvertSet && notConvertSet.contains(c[i])) {
                continue;
            }

            if (c[i] == Symbol.C_SPACE) {
                c[i] = '\u3000'; // Full-width space
            } else if (c[i] < '\177') {
                c[i] = (char) (c[i] + 65248);
            }
        }
        return new String(c);
    }

    /**
     * Converts full-width characters (DBCS) to half-width characters (SBCS).
     *
     * @param input The string.
     * @return The half-width string.
     */
    public static String toDBC(final String input) {
        return toDBC(input, null);
    }

    /**
     * Converts full-width characters to half-width characters, optionally skipping some characters.
     *
     * @param text          The text.
     * @param notConvertSet A set of characters to not convert.
     * @return The converted string.
     */
    public static String toDBC(final String text, final Set<Character> notConvertSet) {
        if (StringKit.isBlank(text)) {
            return text;
        }
        final char[] c = text.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (null != notConvertSet && notConvertSet.contains(c[i])) {
                continue;
            }

            if (c[i] == '\u3000' || c[i] == '\u00a0' || c[i] == '\u2007' || c[i] == '\u202F') {
                c[i] = Symbol.C_SPACE;
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    /**
     * Converts a string to a hexadecimal string (lowercase).
     *
     * @param text    The string to convert.
     * @param charset The charset.
     * @return The hex string.
     * @see HexKit#encodeString(CharSequence, java.nio.charset.Charset)
     */
    public static String toHex(final String text, final java.nio.charset.Charset charset) {
        return HexKit.encodeString(text, charset);
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param bytes The byte array.
     * @return The hex string.
     * @see HexKit#encodeString(byte[])
     */
    public static String toHex(final byte[] bytes) {
        return HexKit.encodeString(bytes);
    }

    /**
     * Converts a hexadecimal string to a byte array.
     *
     * @param src The hex string.
     * @return The byte array.
     * @see HexKit#decode(char[])
     */
    public static byte[] hexToBytes(final String src) {
        return HexKit.decode(src.toCharArray());
    }

    /**
     * Converts a hexadecimal string to a normal string.
     *
     * @param data    The hex string.
     * @param charset The charset.
     * @return The decoded string.
     * @see HexKit#decodeString(CharSequence, java.nio.charset.Charset)
     */
    public static String hexToString(final CharSequence data, final java.nio.charset.Charset charset) {
        return HexKit.decodeString(data, charset);
    }

    /**
     * Converts a string to its Unicode-escaped representation.
     *
     * @param data The string.
     * @return The Unicode-escaped string.
     * @see UnicodeKit#toUnicode(CharSequence)
     */
    public static String strToUnicode(final String data) {
        return UnicodeKit.toUnicode(data);
    }

    /**
     * Converts a Unicode-escaped string to a normal string.
     *
     * @param unicode The Unicode-escaped string.
     * @return The decoded string.
     * @see UnicodeKit#toString(String)
     */
    public static String unicodeToString(final String unicode) {
        return UnicodeKit.toString(unicode);
    }

    /**
     * Converts the character encoding of a string.
     *
     * @param text          The text.
     * @param sourceCharset The source charset.
     * @param destCharset   The destination charset.
     * @return The converted string.
     * @see Charset#convert(String, String, String)
     */
    public static String convertCharset(final String text, final String sourceCharset, final String destCharset) {
        if (ArrayKit.hasBlank(text, sourceCharset, destCharset)) {
            return text;
        }
        return Charset.convert(text, sourceCharset, destCharset);
    }

    /**
     * Converts a duration from a source `TimeUnit` to a destination `TimeUnit`.
     *
     * @param sourceDuration The source duration.
     * @param sourceUnit     The source time unit.
     * @param destUnit       The destination time unit.
     * @return The duration in the destination unit.
     */
    public static long convertTime(final long sourceDuration, final TimeUnit sourceUnit, final TimeUnit destUnit) {
        Assert.notNull(sourceUnit, "sourceUnit is null !");
        Assert.notNull(destUnit, "destUnit is null !");
        return destUnit.convert(sourceDuration, sourceUnit);
    }

    /**
     * Converts a primitive `Class` to its corresponding wrapper `Class`.
     *
     * @param clazz The primitive class.
     * @return The wrapper class.
     * @see EnumValue.Type#wrap(Class)
     */
    public static Class<?> wrap(final Class<?> clazz) {
        return EnumValue.Type.wrap(clazz);
    }

    /**
     * Converts a wrapper `Class` to its corresponding primitive `Class`.
     *
     * @param clazz The wrapper class.
     * @return The primitive class.
     * @see EnumValue.Type#unWrap(Class)
     */
    public static Class<?> unWrap(final Class<?> clazz) {
        return EnumValue.Type.unWrap(clazz);
    }

    /**
     * Converts a number to its English word representation.
     *
     * @param number The `Number` object.
     * @return The English representation.
     */
    public static String numberToWord(final Number number) {
        return EnglishNumberFormatter.format(number);
    }

    /**
     * Converts a number to a compact representation (e.g., 1200 -> 1.2k).
     *
     * @param number The `Number` object.
     * @return The compact string representation.
     */
    public static String numberToSimple(final Number number) {
        return EnglishNumberFormatter.formatSimple(number.longValue());
    }

    /**
     * Converts an Arabic numeral to its Chinese character representation.
     *
     * @param number           The number.
     * @param isUseTraditional If true, uses traditional Chinese characters (for currency).
     * @return The Chinese representation.
     */
    public static String numberToChinese(final double number, final boolean isUseTraditional) {
        return ChineseNumberFormatter.of().setUseTraditional(isUseTraditional).format(number);
    }

    /**
     * Converts a Chinese numeral string to a number.
     *
     * @param number The Chinese numeral string.
     * @return The number as a `BigDecimal`.
     */
    public static BigDecimal chineseToNumber(final String number) {
        return ChineseNumberParser.parseFromChineseNumber(number);
    }

    /**
     * Converts a number to its formal Chinese currency representation.
     *
     * @param n The number.
     * @return The Chinese currency string.
     */
    public static String digitToChinese(Number n) {
        if (null == n) {
            n = 0;
        }
        return ChineseNumberFormatter.of().setUseTraditional(true).setMoneyMode(true).format(n.doubleValue());
    }

    /**
     * Converts a formal Chinese currency string to a number.
     *
     * @param chineseMoneyAmount The Chinese currency string.
     * @return The number as a `BigDecimal`.
     */
    public static BigDecimal chineseMoneyToNumber(final String chineseMoneyAmount) {
        return ChineseNumberParser.parseFromChineseMoney(chineseMoneyAmount);
    }

    /**
     * Converts an `int` to a `byte`.
     *
     * @param intValue The int value.
     * @return The byte value.
     */
    public static byte intToByte(final int intValue) {
        return (byte) intValue;
    }

    /**
     * Converts a `byte` to an unsigned `int`.
     *
     * @param byteValue The byte value.
     * @return The unsigned int value.
     */
    public static int byteToUnsignedInt(final byte byteValue) {
        return byteValue & 0xFF;
    }

    /**
     * Converts a byte array to a `short` (little-endian by default).
     *
     * @param bytes The byte array.
     * @return The short value.
     */
    public static short bytesToShort(final byte[] bytes) {
        return ByteKit.toShort(bytes);
    }

    /**
     * Converts a `short` to a byte array (little-endian by default).
     *
     * @param shortValue The short value.
     * @return The byte array.
     */
    public static byte[] shortToBytes(final short shortValue) {
        return ByteKit.toBytes(shortValue);
    }

    /**
     * Converts a byte array to an `int` (little-endian by default).
     *
     * @param bytes The byte array.
     * @return The int value.
     */
    public static int bytesToInt(final byte[] bytes) {
        return ByteKit.toInt(bytes);
    }

    /**
     * Converts an `int` to a byte array (little-endian by default).
     *
     * @param intValue The int value.
     * @return The byte array.
     */
    public static byte[] intToBytes(final int intValue) {
        return ByteKit.toBytes(intValue);
    }

    /**
     * Converts a `long` to a byte array (little-endian by default).
     *
     * @param longValue The long value.
     * @return The byte array.
     */
    public static byte[] longToBytes(final long longValue) {
        return ByteKit.toBytes(longValue);
    }

    /**
     * Converts a byte array to a `long` (little-endian by default).
     *
     * @param bytes The byte array.
     * @return The long value.
     */
    public static long bytesToLong(final byte[] bytes) {
        return ByteKit.toLong(bytes);
    }

}
