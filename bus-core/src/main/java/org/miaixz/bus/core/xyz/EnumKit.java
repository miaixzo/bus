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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.Assert;

/**
 * Enum utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EnumKit {

    /**
     * Constructs a new EnumKit. Utility class constructor for static access.
     */
    private EnumKit() {
    }

    private static final Map<Class<?>, Enum<?>[]> CACHE = new ConcurrentHashMap<>();

    /**
     * Clears the enum cache, allowing enums to be reloaded.
     */
    public static void clear() {
        CACHE.clear();
    }

    /**
     * Checks if the specified type is an {@code Enum}.
     *
     * @param type The type to check.
     * @return `true` if the type is an {@code Enum}.
     */
    public static boolean isEnum(final Type type) {
        return Assert.notNull(TypeKit.getClass(type)).isEnum();
    }

    /**
     * Checks if the specified object is an {@code Enum}.
     *
     * @param object The object to check.
     * @return `true` if the object is an {@code Enum}.
     */
    public static boolean isEnum(final Object object) {
        return Assert.notNull(object).getClass().isEnum();
    }

    /**
     * Converts an {@code Enum} to a {@code String} by calling its `name()` method.
     *
     * @param e The enum.
     * @return The name of the enum, or `null` if the enum is `null`.
     */
    public static String toString(final Enum<?> e) {
        return null != e ? e.name() : null;
    }

    /**
     * Gets the enum constant at the specified index.
     *
     * @param <E>       The enum type.
     * @param enumClass The enum class.
     * @param index     The index of the enum constant.
     * @return The enum constant, or `null` if the index is out of bounds.
     */
    public static <E extends Enum<E>> E getEnumAt(final Class<E> enumClass, int index) {
        if (null == enumClass) {
            return null;
        }
        final E[] enumConstants = getEnums(enumClass);
        if (index < 0) {
            index = enumConstants.length + index;
        }
        return index >= 0 && index < enumConstants.length ? enumConstants[index] : null;
    }

    /**
     * Converts a string to an enum constant of the specified enum type.
     *
     * @param <E>       The enum type.
     * @param enumClass The enum class.
     * @param value     The string value.
     * @return The enum constant.
     */
    public static <E extends Enum<E>> E fromString(final Class<E> enumClass, final String value) {
        if (null == enumClass || StringKit.isBlank(value)) {
            return null;
        }
        return Enum.valueOf(enumClass, value);
    }

    /**
     * Converts a string to an enum constant, returning a default value if no match is found.
     *
     * @param <E>          The enum type.
     * @param enumClass    The enum class.
     * @param value        The string value.
     * @param defaultValue The default value to return if conversion fails.
     * @return The enum constant.
     */
    public static <E extends Enum<E>> E fromString(final Class<E> enumClass, final String value, final E defaultValue) {
        return ObjectKit.defaultIfNull(fromStringQuietly(enumClass, value), defaultValue);
    }

    /**
     * Converts a string to an enum constant, returning `null` on failure instead of throwing an exception.
     *
     * @param <E>       The enum type.
     * @param enumClass The enum class.
     * @param value     The string value.
     * @return The enum constant, or `null` if not found.
     */
    public static <E extends Enum<E>> E fromStringQuietly(final Class<E> enumClass, final String value) {
        if (null == enumClass || StringKit.isBlank(value)) {
            return null;
        }
        try {
            return fromString(enumClass, value);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Finds an enum constant by flexibly matching a value against any of its fields (including `name()`).
     *
     * @param <E>       The enum type.
     * @param enumClass The enum class.
     * @param value     The value to match.
     * @return The matching enum constant, or `null` if not found.
     */
    public static <E extends Enum<E>> E likeValueOf(final Class<E> enumClass, Object value) {
        if (null == enumClass || null == value) {
            return null;
        }
        if (value instanceof CharSequence) {
            value = value.toString().trim();
        }

        final Field[] fields = FieldKit.getFields(enumClass);
        final E[] enums = getEnums(enumClass);
        String fieldName;
        for (final Field field : fields) {
            fieldName = field.getName();
            if (field.getType().isEnum() || StringKit.equalsAny("ENUM$VALUES", "ordinal", fieldName)) {
                continue;
            }
            for (final E enumObj : enums) {
                if (ObjectKit.equals(value, FieldKit.getFieldValue(enumObj, field))) {
                    return enumObj;
                }
            }
        }
        return null;
    }

    /**
     * Gets a list of the names of all constants in an enum class.
     *
     * @param <E>   the enum type
     * @param clazz The enum class.
     * @return A list of names.
     */
    public static <E extends Enum<E>> List<String> getNames(final Class<E> clazz) {
        if (null == clazz) {
            return null;
        }
        final E[] enums = getEnums(clazz);
        if (null == enums) {
            return null;
        }
        final List<String> list = new ArrayList<>(enums.length);
        for (final E e : enums) {
            list.add(e.name());
        }
        return list;
    }

    /**
     * Gets a list of values for a specified field from all constants in an enum class.
     *
     * @param <E>       the enum type
     * @param clazz     The enum class.
     * @param fieldName The name of the field.
     * @return A list of field values.
     */
    public static <E extends Enum<E>> List<Object> getFieldValues(final Class<E> clazz, final String fieldName) {
        if (null == clazz || StringKit.isBlank(fieldName)) {
            return null;
        }
        final E[] enums = getEnums(clazz);
        if (null == enums) {
            return null;
        }
        final List<Object> list = new ArrayList<>(enums.length);
        for (final E e : enums) {
            list.add(FieldKit.getFieldValue(e, fieldName));
        }
        return list;
    }

    /**
     * Gets a list of all field names for an enum class, excluding internal fields.
     *
     * @param clazz The enum class.
     * @return A list of field names.
     */
    public static List<String> getFieldNames(final Class<? extends Enum<?>> clazz) {
        if (null == clazz) {
            return null;
        }
        final List<String> names = new ArrayList<>();
        final Field[] fields = FieldKit.getFields(clazz);
        String name;
        for (final Field field : fields) {
            name = field.getName();
            if (field.getType().isEnum() || name.contains("$VALUES") || "ordinal".equals(name)) {
                continue;
            }
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * Gets an enum constant by matching a field value.
     *
     * @param condition The function to extract the field to be compared.
     * @param value     The value to match.
     * @param <E>       The enum type.
     * @param <C>       The field type.
     * @return The corresponding enum constant, or `null` if not found.
     */
    public static <E extends Enum<E>, C> E getBy(final FunctionX<E, C> condition, final C value) {
        return getBy(condition, value, null);
    }

    /**
     * Gets an enum constant by matching a field value, returning a default value if not found.
     *
     * @param condition   The function to extract the field to be compared.
     * @param value       The value to match.
     * @param defaultEnum The default enum to return if no match is found.
     * @param <C>         The field type.
     * @param <E>         The enum type.
     * @return The corresponding enum constant, or the default value.
     */
    public static <E extends Enum<E>, C> E getBy(final FunctionX<E, C> condition, final C value, final E defaultEnum) {
        if (null == condition) {
            return null;
        }
        final Class<E> implClass = LambdaKit.getRealClass(condition);
        return getBy(implClass, condition, value, defaultEnum);
    }

    /**
     * Gets an enum constant by matching a field value, returning a default value if not found.
     *
     * @param enumClass   The enum class.
     * @param condition   The function to extract the field to be compared.
     * @param value       The value to match.
     * @param defaultEnum The default enum to return if no match is found.
     * @param <C>         The field type.
     * @param <E>         The enum type.
     * @return The corresponding enum constant, or the default value.
     */
    public static <E extends Enum<E>, C> E getBy(
            final Class<E> enumClass,
            final FunctionX<E, C> condition,
            final C value,
            final E defaultEnum) {
        if (null == condition) {
            return null;
        }
        return getBy(enumClass, constant -> ObjectKit.equals(condition.apply(constant), value), defaultEnum);
    }

    /**
     * Gets an enum constant that satisfies a predicate.
     *
     * @param enumClass The enum class.
     * @param predicate The condition.
     * @param <E>       The enum type.
     * @return The corresponding enum constant, or `null` if not found.
     */
    public static <E extends Enum<E>> E getBy(final Class<E> enumClass, final Predicate<? super E> predicate) {
        return getBy(enumClass, predicate, null);
    }

    /**
     * Gets an enum constant that satisfies a predicate, returning a default value if not found.
     *
     * @param enumClass   The enum class.
     * @param predicate   The condition.
     * @param defaultEnum The default enum to return if no match is found.
     * @param <E>         The enum type.
     * @return The corresponding enum constant, or the default value.
     */
    public static <E extends Enum<E>> E getBy(
            final Class<E> enumClass,
            final Predicate<? super E> predicate,
            final E defaultEnum) {
        if (null == enumClass || null == predicate) {
            return null;
        }
        return Arrays.stream(getEnums(enumClass)).filter(predicate).findAny().orElse(defaultEnum);
    }

    /**
     * Gets the value of a specific field from an enum constant that is found by a condition on another field.
     *
     * @param field     The function to extract the desired field value.
     * @param condition The function to extract the conditional field.
     * @param value     The value to match in the condition.
     * @param <E>       The enum type.
     * @param <F>       The type of the desired field.
     * @param <C>       The type of the conditional field.
     * @return The value of the desired field, or `null` if not found.
     */
    public static <E extends Enum<E>, F, C> F getFieldBy(
            final FunctionX<E, F> field,
            final Function<E, C> condition,
            final C value) {
        if (null == field || null == condition) {
            return null;
        }
        Class<E> implClass = LambdaKit.getRealClass(field);
        if (Enum.class.equals(implClass)) {
            implClass = LambdaKit.getRealClass(field);
        }
        return Arrays.stream(getEnums(implClass)).filter(constant -> ObjectKit.equals(condition.apply(constant), value))
                .findFirst().map(field).orElse(null);
    }

    /**
     * Creates a `LinkedHashMap` mapping the names of enum constants to the constants themselves, preserving declaration
     * order.
     *
     * @param <E>       The enum type.
     * @param enumClass The enum class.
     * @return A map from enum name to enum constant.
     */
    public static <E extends Enum<E>> LinkedHashMap<String, E> getEnumMap(final Class<E> enumClass) {
        if (null == enumClass) {
            return null;
        }
        final LinkedHashMap<String, E> map = new LinkedHashMap<>();
        for (final E e : getEnums(enumClass)) {
            map.put(e.name(), e);
        }
        return map;
    }

    /**
     * Creates a map where keys are the names of enum constants and values are the values of a specified field for each
     * constant.
     *
     * @param <E>       the enum type
     * @param clazz     The enum class.
     * @param fieldName The name of the field.
     * @return A map from enum name to field value.
     */
    public static <E extends Enum<E>> Map<String, Object> getNameFieldMap(
            final Class<E> clazz,
            final String fieldName) {
        if (null == clazz || StringKit.isBlank(fieldName)) {
            return null;
        }
        final E[] enums = getEnums(clazz);
        Assert.notNull(enums, "Class [{}] is not an Enum type!", clazz);
        final Map<String, Object> map = MapKit.newHashMap(enums.length, true);
        for (final E e : enums) {
            map.put(e.name(), FieldKit.getFieldValue(e, fieldName));
        }
        return map;
    }

    /**
     * Checks if an enum constant with the specified name exists in the enum class.
     *
     * @param <E>       The enum type.
     * @param enumClass The enum class.
     * @param name      The name to find.
     * @return `true` if the constant exists.
     */
    public static <E extends Enum<E>> boolean contains(final Class<E> enumClass, final String name) {
        final LinkedHashMap<String, E> enumMap = getEnumMap(enumClass);
        if (CollKit.isEmpty(enumMap)) {
            return false;
        }
        return enumMap.containsKey(name);
    }

    /**
     * Checks if an enum constant with the specified name does not exist in the enum class.
     *
     * @param <E>       The enum type.
     * @param enumClass The enum class.
     * @param val       The name to find.
     * @return `true` if the constant does not exist.
     */
    public static <E extends Enum<E>> boolean notContains(final Class<E> enumClass, final String val) {
        return !contains(enumClass, val);
    }

    /**
     * Checks if an enum's name matches a string, ignoring case.
     *
     * @param e   The enum constant.
     * @param val The string to compare.
     * @return `true` if they match, ignoring case.
     */
    public static boolean equalsIgnoreCase(final Enum<?> e, final String val) {
        return StringKit.equalsIgnoreCase(toString(e), val);
    }

    /**
     * Checks if an enum's name matches a string.
     *
     * @param e   The enum constant.
     * @param val The string to compare.
     * @return `true` if they match.
     */
    public static boolean equals(final Enum<?> e, final String val) {
        return StringKit.equals(toString(e), val);
    }

    /**
     * Gets all enum constants for a given enum class. Results are cached for performance.
     *
     * @param <E>       The enum type.
     * @param enumClass The enum class.
     * @return An array of the enum constants.
     */
    public static <E extends Enum<E>> E[] getEnums(final Class<E> enumClass) {
        if (null == enumClass) {
            return null;
        }
        return (E[]) CACHE.computeIfAbsent(enumClass, (k) -> enumClass.getEnumConstants());
    }

}
