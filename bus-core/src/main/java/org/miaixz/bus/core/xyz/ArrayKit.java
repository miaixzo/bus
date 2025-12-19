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

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.array.ArrayWrapper;
import org.miaixz.bus.core.center.array.PrimitiveArray;
import org.miaixz.bus.core.center.set.UniqueKeySet;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.StringJoiner;

/**
 * Array utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ArrayKit extends PrimitiveArray {

    /**
     * Converts to an array. If {@code values} is already an array, it is returned. Otherwise, a new array containing
     * only {@code values} is returned. Note: The element type of {@code values} or its own type must exactly match the
     * provided {@code elementType}.
     *
     * @param <A>         The array type.
     * @param values      The element value(s).
     * @param elementType The element type of the array. {@code null} indicates using the type of {@code values}.
     * @return The array.
     */
    public static <A> A castOrWrapSingle(final Object values, final Class<?> elementType) {
        if (isArray(values)) {
            return (A) values;
        }

        return wrapSingle(values, elementType);
    }

    /**
     * Wraps a single element into an array.
     *
     * @param <A>         The array type.
     * @param value       The element value.
     * @param elementType The element type of the array. {@code null} indicates using the type of {@code value}.
     * @return The new array.
     */
    public static <A> A wrapSingle(final Object value, final Class<?> elementType) {
        // Insert a single element
        final Object newInstance = Array.newInstance(null == elementType ? value.getClass() : elementType, 1);
        Array.set(newInstance, 0, value);
        return (A) newInstance;
    }

    /**
     * Converts a collection to an array. If the collection is {@code null}, an empty array (with 0 elements) is
     * returned.
     *
     * @param <T>           The array element type.
     * @param iterable      The {@link Iterable} collection.
     * @param componentType The component type of the collection elements.
     * @return The array.
     */
    public static <T> T[] ofArray(final Iterable<T> iterable, final Class<T> componentType) {
        if (null == iterable) {
            return newArray(componentType, 0);
        }

        if (iterable instanceof List) {
            // List
            return ((List<T>) iterable).toArray(newArray(componentType, 0));
        } else if (iterable instanceof Collection) {
            // Other collections
            final int size = ((Collection<T>) iterable).size();
            final T[] result = newArray(componentType, size);
            int i = 0;
            for (final T element : iterable) {
                result[i] = element;
                i++;
            }
        }

        // Custom Iterable processed as List
        return ListKit.of(iterable.iterator()).toArray(newArray(componentType, 0));
    }

    /**
     * Creates a new empty array.
     *
     * @param <T>           The array element type.
     * @param componentType The element type, e.g., {@code Integer.class}, but not primitive types like
     *                      {@code int.class}.
     * @param newSize       The size of the new array.
     * @return The empty array.
     */
    public static <T> T[] newArray(final Class<?> componentType, final int newSize) {
        return (T[]) Array.newInstance(componentType, newSize);
    }

    /**
     * Creates a new empty {@code Object} array.
     *
     * @param newSize The size of the new array.
     * @return An empty {@code Object} array.
     */
    public static Object[] newArray(final int newSize) {
        return new Object[newSize];
    }

    /**
     * Gets the component type of an array object. Examples of method call parameters and return results:
     * <ul>
     * <li>Object[] = Object.class</li>
     * <li>String[] = String.class</li>
     * <li>int[] = int.class</li>
     * <li>Integer[] = Integer.class</li>
     * <li>null = null</li>
     * <li>String = null</li>
     * </ul>
     *
     * @param array The array object.
     * @return The component type.
     */
    public static Class<?> getComponentType(final Object array) {
        return null == array ? null : getComponentType(array.getClass());
    }

    /**
     * Gets the component type of an array class. Examples of method call parameters and return results:
     * <ul>
     * <li>Object[].class = Object.class</li>
     * <li>String[].class = String.class</li>
     * <li>int[].class = int.class</li>
     * <li>Integer[].class = Integer.class</li>
     * <li>null = null</li>
     * <li>String.class = null</li>
     * </ul>
     *
     * @param arrayClass The class of the array object.
     * @return The component type.
     */
    public static Class<?> getComponentType(final Class<?> arrayClass) {
        return null == arrayClass ? null : arrayClass.getComponentType();
    }

    /**
     * Gets the array type based on the array component type. This method obtains the type by creating an empty array.
     * <p>
     * This method is the inverse of {@link #getComponentType(Class)}.
     *
     * @param componentType The array component type.
     * @return The array type.
     */
    public static Class<?> getArrayType(final Class<?> componentType) {
        if (null == componentType) {
            return null;
        }
        return Array.newInstance(componentType, 0).getClass();
    }

    /**
     * Casts an array type. The prerequisite for forced conversion is that the array element type can be forcibly
     * converted. A new array will be generated after forced conversion.
     *
     * @param type     The target array type or array element type.
     * @param arrayObj The original array.
     * @return The converted array.
     * @throws NullPointerException     If the provided parameter is null.
     * @throws IllegalArgumentException If {@code arrayObj} is not an array.
     */
    public static Object[] cast(final Class<?> type, final Object arrayObj)
            throws NullPointerException, IllegalArgumentException {
        if (null == arrayObj) {
            throw new NullPointerException("Argument [arrayObj] is null !");
        }
        if (!arrayObj.getClass().isArray()) {
            throw new IllegalArgumentException("Argument [arrayObj] is not array !");
        }
        if (null == type) {
            return (Object[]) arrayObj;
        }

        final Class<?> componentType = type.isArray() ? type.getComponentType() : type;
        final Object[] array = (Object[]) arrayObj;
        final Object[] result = newArray(componentType, array.length);
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    /**
     * Appends new elements to an existing array. Appending new elements generates a new array, leaving the original
     * array unchanged.
     *
     * @param <T>         The array element type.
     * @param buffer      The existing array.
     * @param newElements The new elements to append.
     * @return The new array.
     */
    @SafeVarargs
    public static <T> T[] append(final T[] buffer, final T... newElements) {
        if (isEmpty(buffer)) {
            return newElements;
        }
        return insert(buffer, buffer.length, newElements);
    }

    /**
     * Appends new elements to an existing array. Appending new elements generates a new array, leaving the original
     * array unchanged.
     *
     * @param <A>         The array type.
     * @param <T>         The array element type.
     * @param array       The existing array.
     * @param newElements The new elements to append.
     * @return The new array.
     */
    @SafeVarargs
    public static <A, T> A append(final A array, final T... newElements) {
        if (isEmpty(array)) {
            if (null == array) {
                return (A) newElements;
            }
            // Variable arguments may be wrapper types, if array is primitive type, this cast is not appropriate, use
            // universal converter to complete the conversion
            return (A) Convert.convert(array.getClass(), newElements);
        }
        return insert(array, length(array), newElements);
    }

    /**
     * Sets an element value at a specific position in the array. If the given index is greater than or equal to the
     * array length, the element is appended.
     *
     * @param <T>   The array element type.
     * @param array The existing array.
     * @param index The position. If greater than or equal to the length, it appends; otherwise, it replaces.
     * @param value The new value.
     * @return The new array or the original array if no change.
     */
    public static <T> T[] setOrAppend(final T[] array, final int index, final T value) {
        if (isEmpty(array)) {
            return wrapSingle(value, null == array ? null : array.getClass().getComponentType());
        }
        return ArrayWrapper.of(array).setOrAppend(index, value).getRaw();
    }

    /**
     * Sets an element value at a specific position in the array. If the given index is greater than or equal to the
     * array length, the element is appended.
     *
     * @param <A>   The array type.
     * @param array The existing array.
     * @param index The position. If greater than or equal to the length, it appends; otherwise, it replaces.
     * @param value The new value.
     * @return The new array or the original array if no change.
     */
    public static <A> A setOrAppend(final A array, final int index, final Object value) {
        if (isEmpty(array)) {
            return wrapSingle(value, null == array ? null : array.getClass().getComponentType());
        }
        return ArrayWrapper.of(array).setOrAppend(index, value).getRaw();
    }

    /**
     * Sets an element value at a specific position in the array. If the index is less than the array length, the value
     * at the specified position is replaced. Otherwise, {@code null} or {@code 0} is appended until the index is
     * reached, and then the value is set.
     *
     * @param <A>   The array type.
     * @param array The existing array.
     * @param index The position. If less than length, replaces; otherwise, pads with default values and then sets.
     * @param value The new value.
     * @return The new array or the original array if no change.
     */
    public static <A> A setOrPadding(final A array, final int index, final Object value) {
        if (index == 0 && isEmpty(array)) {
            return wrapSingle(value, null == array ? null : array.getClass().getComponentType());
        }
        return ArrayWrapper.of(array).setOrPadding(index, value).getRaw();
    }

    /**
     * Sets an element value at a specific position in the array. If the index is less than the array length, the value
     * at the specified position is replaced. Otherwise, {@code paddingValue} is appended until the index is reached,
     * and then the value is set.
     *
     * @param <A>          The array type.
     * @param <E>          The element type.
     * @param array        The existing array.
     * @param index        The position. If less than length, replaces; otherwise, pads with {@code paddingValue} and
     *                     then sets.
     * @param value        The new value.
     * @param paddingValue The value to use for padding.
     * @return The new array or the original array if no change.
     */
    public static <A, E> A setOrPadding(final A array, final int index, final E value, final E paddingValue) {
        if (index == 0 && isEmpty(array)) {
            return wrapSingle(value, null == array ? null : array.getClass().getComponentType());
        }
        return ArrayWrapper.of(array).setOrPadding(index, value, paddingValue).getRaw();
    }

    /**
     * Combines all arrays into a new merged array. Null arrays are ignored.
     *
     * @param <T>    The array element type.
     * @param arrays The arrays to combine.
     * @return The merged array.
     */
    @SafeVarargs
    public static <T> T[] addAll(final T[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        int length = 0;
        for (final T[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final T[] result = newArray(arrays.getClass().getComponentType().getComponentType(), length);
        if (length == 0) {
            return result;
        }

        length = 0;
        for (final T[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * Replaces old elements with new elements sequentially starting from a specified position in the array.
     * <ul>
     * <li>If the specified position is negative, a new array is generated with new elements placed at the
     * beginning.</li>
     * <li>If the specified position is greater than or equal to the old array length, a new array is generated with new
     * elements placed at the end.</li>
     * <li>If the specified position plus the number of new elements is greater than the old array length, a new array
     * is generated where elements before the specified position are from the old array, and elements at and after the
     * specified position are new elements.</li>
     * <li>Otherwise, new elements replace old elements sequentially starting from the specified position in the
     * existing array, and the old array is returned.</li>
     * </ul>
     *
     * @param <T>    The array element type.
     * @param array  The existing array.
     * @param index  The starting position for replacement.
     * @param values The new values to replace with.
     * @return The new array or the original array if elements are replaced in place.
     */
    public static <T> T[] replace(final T[] array, final int index, final T... values) {
        if (isEmpty(array)) {
            return values;
        }
        return ArrayWrapper.of(array).replace(index, values).getRaw();
    }

    /**
     * Replaces old elements with new elements sequentially starting from a specified position in the array.
     * <ul>
     * <li>If the specified position is negative, a new array is generated with new elements placed at the
     * beginning.</li>
     * <li>If the specified position is greater than or equal to the old array length, a new array is generated with new
     * elements placed at the end.</li>
     * <li>If the specified position plus the number of new elements is greater than the old array length, a new array
     * is generated where elements before the specified position are from the old array, and elements at and after the
     * specified position are new elements.</li>
     * <li>Otherwise, new elements replace old elements sequentially starting from the specified position in the
     * existing array, and the old array is returned.</li>
     * </ul>
     *
     * @param <A>    The array type.
     * @param array  The existing array.
     * @param index  The starting position for replacement.
     * @param values The new values to replace with.
     * @return The new array or the original array if elements are replaced in place.
     */
    public static <A> A replace(final A array, final int index, final A values) {
        if (isEmpty(array)) {
            return castOrWrapSingle(values, null == array ? null : array.getClass().getComponentType());
        }
        return ArrayWrapper.of(array).replace(index, values).getRaw();
    }

    /**
     * Inserts new elements into an existing array at a specified position. Inserting new elements generates a new
     * array, leaving the original array unchanged. If the insertion position is negative, it counts backward from the
     * end of the original array. If it exceeds the original array length, the empty spaces are filled with null.
     *
     * @param <T>         The array element type.
     * @param buffer      The existing array.
     * @param index       The insertion position. This position is the gap before the element at this index.
     * @param newElements The new elements to insert.
     * @return The new array.
     */
    public static <T> T[] insert(final T[] buffer, final int index, final T... newElements) {
        return (T[]) insert((Object) buffer, index, newElements);
    }

    /**
     * Inserts new elements into an existing array at a specified position. Inserting new elements generates a new
     * array, leaving the original array unchanged. If the insertion position is negative, it counts backward from the
     * end of the original array. If it exceeds the original array length, the empty spaces are filled with default
     * values.
     *
     * @param <A>         The array type.
     * @param <E>         The array element type.
     * @param array       The existing array, can be a primitive array.
     * @param index       The insertion position. This position is the gap before the element at this index.
     * @param newElements The new elements to insert.
     * @return The new array.
     */
    @SafeVarargs
    public static <A, E> A insert(final A array, final int index, final E... newElements) {
        return ArrayWrapper.of(array).insertArray(index, (A) newElements).getRaw();
    }

    /**
     * Generates a new array with a reset size. After resizing, the original array is copied to the new array
     * sequentially. If the new length is smaller, the original array is truncated.
     *
     * @param <T>           The array element type.
     * @param data          The original array.
     * @param newSize       The new size of the array.
     * @param componentType The component type of the array.
     * @return The resized new array.
     */
    public static <T> T[] resize(final T[] data, final int newSize, final Class<?> componentType) {
        if (newSize < 0) {
            return data;
        }

        final T[] newArray = newArray(componentType, newSize);
        if (newSize > 0 && isNotEmpty(data)) {
            System.arraycopy(data, 0, newArray, 0, Math.min(data.length, newSize));
        }
        return newArray;
    }

    /**
     * Generates a new array with a reset size. After resizing, the original array is copied to the new array
     * sequentially. If the new length is smaller, the original array is truncated.
     *
     * @param array   The original array.
     * @param newSize The new size of the array.
     * @return The resized new array.
     * @see System#arraycopy(Object, int, Object, int, int)
     */
    public static Object resize(final Object array, final int newSize) {
        if (newSize < 0) {
            return array;
        }
        if (null == array) {
            return null;
        }
        final int length = length(array);
        final Object newArray = Array.newInstance(array.getClass().getComponentType(), newSize);
        if (newSize > 0 && isNotEmpty(array)) {
            // noinspection SuspiciousSystemArraycopy
            System.arraycopy(array, 0, newArray, 0, Math.min(length, newSize));
        }
        return newArray;
    }

    /**
     * Generates a new array with a reset size. After resizing, the original array is copied to the new array
     * sequentially. If the new length is smaller, the original array is truncated.
     *
     * @param <T>     The array element type.
     * @param buffer  The original array.
     * @param newSize The new size of the array.
     * @return The resized new array.
     */
    public static <T> T[] resize(final T[] buffer, final int newSize) {
        return resize(buffer, newSize, buffer.getClass().getComponentType());
    }

    /**
     * Wraps {@link System#arraycopy(Object, int, Object, int, int)} for array copying. Both source and destination
     * arrays start copying from position 0, and the copy length is the length of the source array.
     *
     * @param <T>  The target array type.
     * @param src  The source array.
     * @param dest The destination array.
     * @return The destination array.
     */
    public static <T> T copy(final Object src, final T dest) {
        return copy(src, dest, length(src));
    }

    /**
     * Wraps {@link System#arraycopy(Object, int, Object, int, int)} for array copying. Both source and destination
     * arrays start copying from position 0.
     *
     * @param <T>    The target array type.
     * @param src    The source array.
     * @param dest   The destination array.
     * @param length The length of the array to copy.
     * @return The destination array.
     */
    public static <T> T copy(final Object src, final T dest, final int length) {
        return copy(src, 0, dest, 0, length);
    }

    /**
     * Wraps {@link System#arraycopy(Object, int, Object, int, int)} for array copying.
     *
     * @param <T>     The target array type.
     * @param src     The source array.
     * @param srcPos  The starting position in the source array.
     * @param dest    The destination array.
     * @param destPos The starting position in the destination array.
     * @param length  The length of the array to copy.
     * @return The destination array.
     */
    public static <T> T copy(final Object src, final int srcPos, final T dest, final int destPos, final int length) {
        // noinspection SuspiciousSystemArraycopy
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }

    /**
     * Clones an array.
     *
     * @param <T>   The array element type.
     * @param array The array to be cloned.
     * @return The new cloned array.
     */
    public static <T> T[] clone(final T[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * Clones an array. If the object is not an array, {@code null} is returned.
     *
     * @param <T>    The array element type.
     * @param object The array object.
     * @return The cloned array object.
     */
    public static <T> T clone(final T object) {
        if (null == object) {
            return null;
        }
        if (isArray(object)) {
            final Object result;
            final Class<?> componentType = object.getClass().getComponentType();
            // Primitive type
            if (componentType.isPrimitive()) {
                final int length = Array.getLength(object);
                result = Array.newInstance(componentType, length);
                copy(object, result, length);
            } else {
                result = ((Object[]) object).clone();
            }
            return (T) result;
        }
        return null;
    }

    /**
     * Performs a specified operation on each array element and returns the modified elements. This editor
     * implementation can achieve the following functions:
     * <ol>
     * <li>Filter out required objects; if {@code null} is returned, the element object is discarded.</li>
     * <li>Modify the element object and return the modified object.</li>
     * </ol>
     *
     * @param <T>    The array element type.
     * @param array  The array.
     * @param editor The editor interface; if {@code null}, the original array is returned.
     * @return The edited array.
     */
    public static <T> T[] edit(final T[] array, final UnaryOperator<T> editor) {
        if (null == array || null == editor) {
            return array;
        }

        final List<T> list = new ArrayList<>(array.length);
        T modified;
        for (final T t : array) {
            modified = editor.apply(t);
            if (null != modified) {
                list.add(modified);
            }
        }
        final T[] result = newArray(array.getClass().getComponentType(), list.size());
        return list.toArray(result);
    }

    /**
     * Filters array elements, retaining elements for which {@link Predicate#test(Object)} returns {@code true}.
     *
     * @param <T>       The array element type.
     * @param array     The array.
     * @param predicate The filter interface, used to define filtering rules. If {@code null}, the original array is
     *                  returned.
     * @return The filtered array.
     */
    public static <T> T[] filter(final T[] array, final Predicate<T> predicate) {
        if (null == array || null == predicate) {
            return array;
        }
        return edit(array, t -> predicate.test(t) ? t : null);
    }

    /**
     * Removes {@code null} elements from the array.
     *
     * @param <T>   The array element type.
     * @param array The array.
     * @return The processed array.
     */
    public static <T> T[] removeNull(final T[] array) {
        // Return the element itself; if null, it is automatically filtered out.
        return edit(array, UnaryOperator.identity());
    }

    /**
     * Removes {@code null} or empty string elements from the array.
     *
     * @param <T>   The array element type.
     * @param array The array.
     * @return The processed array.
     */
    public static <T extends CharSequence> T[] removeEmpty(final T[] array) {
        return filter(array, StringKit::isNotEmpty);
    }

    /**
     * Removes {@code null}, empty string, or whitespace-only string elements from the array.
     *
     * @param <T>   The array element type.
     * @param array The array.
     * @return The processed array.
     */
    public static <T extends CharSequence> T[] removeBlank(final T[] array) {
        return filter(array, StringKit::isNotBlank);
    }

    /**
     * Converts {@code null} elements in the array to empty strings.
     *
     * @param array The array.
     * @return The processed array.
     */
    public static String[] nullToEmpty(final String[] array) {
        return edit(array, t -> null == t ? Normal.EMPTY : t);
    }

    /**
     * Maps keys and values (referencing Python's zip() function). For example: keys = [a,b,c,d], values = [1,2,3,4]
     * will result in the Map {a=1, b=2, c=3, d=4}. If the two arrays have different lengths, only the shortest part is
     * mapped.
     *
     * @param <K>     The Key type.
     * @param <V>     The Value type.
     * @param keys    The list of keys.
     * @param values  The list of values.
     * @param isOrder Whether the elements in the Map retain the order of the key-value arrays themselves.
     * @return The Map.
     */
    public static <K, V> Map<K, V> zip(final K[] keys, final V[] values, final boolean isOrder) {
        if (isEmpty(keys) || isEmpty(values)) {
            return MapKit.newHashMap(0, isOrder);
        }

        final int size = Math.min(keys.length, values.length);
        final Map<K, V> map = MapKit.newHashMap(size, isOrder);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }

        return map;
    }

    /**
     * Maps keys and values (referencing Python's zip() function), returning an unordered Map. For example: keys =
     * [a,b,c,d], values = [1,2,3,4] will result in the Map {a=1, b=2, c=3, d=4}. If the two arrays have different
     * lengths, only the shortest part is mapped.
     *
     * @param <K>    The Key type.
     * @param <V>    The Value type.
     * @param keys   The list of keys.
     * @param values The list of values.
     * @return The Map.
     */
    public static <K, V> Map<K, V> zip(final K[] keys, final V[] values) {
        return zip(keys, values, false);
    }

    /**
     * Checks if the array contains the specified element.
     *
     * @param <T>   The array element type.
     * @param array The array.
     * @param value The element to check.
     * @return {@code true} if the array contains the element, {@code false} otherwise.
     */
    public static <T> boolean contains(final T[] array, final T value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * Checks if the array contains any of the specified elements.
     *
     * @param <T>    The array element type.
     * @param array  The array.
     * @param values The elements to check.
     * @return {@code true} if the array contains any of the specified elements, {@code false} otherwise.
     */
    public static <T> boolean containsAny(final T[] array, final T... values) {
        for (final T value : values) {
            if (contains(array, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the array contains all of the specified elements.
     *
     * @param <T>    The array element type.
     * @param array  The array.
     * @param values The elements to check.
     * @return {@code true} if the array contains all of the specified elements, {@code false} otherwise.
     */
    public static <T> boolean containsAll(final T[] array, final T... values) {
        for (final T value : values) {
            if (!contains(array, value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the array contains the specified element, ignoring case.
     *
     * @param array The array.
     * @param value The element to check.
     * @return {@code true} if the array contains the element (case-insensitive), {@code false} otherwise.
     */
    public static boolean containsIgnoreCase(final CharSequence[] array, final CharSequence value) {
        return indexOfIgnoreCase(array, value) > Normal.__1;
    }

    /**
     * Wraps an array object. The object can be an object array or a primitive array.
     *
     * @param object The object, which can be an object array or a primitive array.
     * @return The wrapped array (wrapper type array or object array).
     * @throws InternalException If the object is not an array.
     */
    public static Object[] wrap(final Object object) {
        if (null == object) {
            return null;
        }
        if (isArray(object)) {
            try {
                final String className = object.getClass().getComponentType().getName();
                return switch (className) {
                    case "long" -> wrap((long[]) object);
                    case "int" -> wrap((int[]) object);
                    case "short" -> wrap((short[]) object);
                    case "char" -> wrap((char[]) object);
                    case "byte" -> wrap((byte[]) object);
                    case "boolean" -> wrap((boolean[]) object);
                    case "float" -> wrap((float[]) object);
                    case "double" -> wrap((double[]) object);
                    default -> (Object[]) object;
                };
            } catch (final Exception e) {
                throw ExceptionKit.wrapRuntime(e);
            }
        }
        throw new InternalException(StringKit.format("[{}] is not Array!", object.getClass()));
    }

    /**
     * Gets the value at the specified index in the array object. Supports negative indices, e.g., -1 for the last
     * element. If the array index is out of bounds, {@code null} is returned.
     *
     * @param <E>   The array element type.
     * @param array The array object.
     * @param index The index, supports negative values.
     * @return The value at the specified index.
     */
    public static <E> E get(final Object array, final int index) {
        return (E) ArrayWrapper.of(array).get(index);
    }

    /**
     * Gets the first element that satisfies the given condition.
     *
     * @param array     The array.
     * @param predicate The condition.
     * @param <E>       The element type.
     * @return The first element that satisfies the condition, or {@code null} if not found.
     */
    public static <E> E get(final E[] array, final Predicate<E> predicate) {
        for (final E e : array) {
            if (predicate.test(e)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Gets all elements at specified positions in the array, forming a new array.
     *
     * @param <T>     The array element type.
     * @param array   The array. If {@code null} is provided, {@code null} is returned.
     * @param indexes The list of indices.
     * @return An array of element values at the specified positions.
     */
    public static <T> T[] getAny(final Object array, final int... indexes) {
        if (null == array) {
            return null;
        }
        if (null == indexes) {
            return newArray(array.getClass().getComponentType(), 0);
        }

        final T[] result = newArray(array.getClass().getComponentType(), indexes.length);
        for (int i = 0; i < indexes.length; i++) {
            result[i] = ArrayKit.get(array, indexes[i]);
        }
        return result;
    }

    /**
     * Joins array elements into a string using the specified conjunction as a separator.
     *
     * @param <T>         The array element type.
     * @param array       The array.
     * @param conjunction The separator.
     * @return The joined string.
     */
    public static <T> String join(final T[] array, final CharSequence conjunction) {
        return join(array, conjunction, null, null);
    }

    /**
     * Joins array elements into a string using the specified delimiter as a separator.
     *
     * @param <T>       The array element type.
     * @param array     The array.
     * @param delimiter The delimiter.
     * @param prefix    The prefix to add to each element; {@code null} means no prefix.
     * @param suffix    The suffix to add to each element; {@code null} means no suffix.
     * @return The joined string.
     */
    public static <T> String join(
            final T[] array,
            final CharSequence delimiter,
            final String prefix,
            final String suffix) {
        if (null == array) {
            return null;
        }

        return StringJoiner.of(delimiter, prefix, suffix)
                // Add prefix and suffix to each element
                .setWrapElement(true).append(array).toString();
    }

    /**
     * Processes array elements first, then joins them into a string using the specified conjunction as a separator.
     *
     * @param <T>         The array element type.
     * @param array       The array.
     * @param conjunction The separator.
     * @param editor      The editor for each element; {@code null} means no editing.
     * @return The joined string.
     */
    public static <T> String join(final T[] array, final CharSequence conjunction, final UnaryOperator<T> editor) {
        return StringJoiner.of(conjunction).append(edit(array, editor)).toString();
    }

    /**
     * Joins array elements into a string using the specified conjunction as a separator.
     *
     * @param array       The array.
     * @param conjunction The separator.
     * @return The joined string.
     */
    public static String join(final Object array, final CharSequence conjunction) {
        if (null == array) {
            return null;
        }
        if (!isArray(array)) {
            throw new IllegalArgumentException(StringKit.format("[{}] is not a Array!", array.getClass()));
        }

        return StringJoiner.of(conjunction).append(array).toString();
    }

    /**
     * Removes the element at the corresponding position in the array. Copied from commons-lang.
     *
     * @param <T>   The array element type.
     * @param array The array object, can be an object array or a primitive array.
     * @param index The position. If the position is less than 0 or greater than the length, the original array is
     *              returned.
     * @return A new array with the specified element removed, or the original array if no change.
     * @throws IllegalArgumentException If the parameter object is not an array object.
     */
    public static <T> T[] remove(final T[] array, final int index) throws IllegalArgumentException {
        return (T[]) remove((Object) array, index);
    }

    /**
     * Removes the specified element from the array. Only the first matching element is removed. Copied from
     * commons-lang.
     *
     * @param <T>     The array element type.
     * @param array   The array object, can be an object array or a primitive array.
     * @param element The element to remove.
     * @return A new array with the specified element removed, or the original array if no change.
     * @throws IllegalArgumentException If the parameter object is not an array object.
     */
    public static <T> T[] removeEle(final T[] array, final T element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * Reverses the order of elements in a portion of the array. This modifies the original array.
     *
     * @param <T>       The array element type.
     * @param array     The array to be reversed (will be modified).
     * @param fromIndex The starting index (inclusive).
     * @param toIndex   The ending index (exclusive).
     * @return The modified original array.
     */
    public static <T> T[] reverse(final T[] array, final int fromIndex, final int toIndex) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(fromIndex, 0);
        int j = Math.min(array.length, toIndex) - 1;
        T tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
        return array;
    }

    /**
     * Reverses the order of all elements in the array. This modifies the original array.
     *
     * @param <T>   The array element type.
     * @param array The array to be reversed (will be modified).
     * @return The modified original array.
     */
    public static <T> T[] reverse(final T[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * Finds the minimum value in a number array.
     *
     * @param <T>         The element type, must be {@link Comparable}.
     * @param numberArray The number array.
     * @return The minimum value.
     */
    public static <T extends Comparable<? super T>> T min(final T[] numberArray) {
        return min(numberArray, null);
    }

    /**
     * Finds the minimum value in a number array using a custom comparator.
     *
     * @param <T>         The element type, must be {@link Comparable}.
     * @param numberArray The number array.
     * @param comparator  The comparator; if {@code null}, natural ordering is used.
     * @return The minimum value.
     */
    public static <T extends Comparable<? super T>> T min(final T[] numberArray, final Comparator<T> comparator) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        T min = numberArray[0];
        for (final T t : numberArray) {
            if (CompareKit.compare(min, t, comparator) > 0) {
                min = t;
            }
        }
        return min;
    }

    /**
     * Finds the maximum value in a number array.
     *
     * @param <T>         The element type, must be {@link Comparable}.
     * @param numberArray The number array.
     * @return The maximum value.
     */
    public static <T extends Comparable<? super T>> T max(final T[] numberArray) {
        return max(numberArray, null);
    }

    /**
     * Finds the maximum value in a number array using a custom comparator.
     *
     * @param <T>         The element type, must be {@link Comparable}.
     * @param numberArray The number array.
     * @param comparator  The comparator; if {@code null}, natural ordering is used.
     * @return The maximum value.
     */
    public static <T extends Comparable<? super T>> T max(final T[] numberArray, final Comparator<T> comparator) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        T max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (CompareKit.compare(max, numberArray[i], comparator) < 0) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * Shuffles the array elements randomly using the Fisher-Yates shuffle algorithm, modifying the original array. This
     * algorithm has linear time complexity.
     *
     * @param <T>   The element type.
     * @param array The array to shuffle (will be modified).
     * @return The shuffled array.
     */
    public static <T> T[] shuffle(final T[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * Shuffles the array elements randomly using the Fisher-Yates shuffle algorithm and a provided random number
     * generator, modifying the original array. This algorithm has linear time complexity.
     *
     * @param <T>    The element type.
     * @param array  The array to shuffle (will be modified).
     * @param random The random number generator.
     * @return The shuffled array.
     */
    public static <T> T[] shuffle(final T[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }

        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }

        return array;
    }

    /**
     * Swaps the values at two specified positions in the array. This modifies the original array.
     *
     * @param <T>    The element type.
     * @param array  The array.
     * @param index1 The first position.
     * @param index2 The second position.
     * @return The array after swapping, which is the same object as the input array.
     */
    public static <T> T[] swap(final T[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Array must not empty !");
        }
        final T tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * Swaps the values at two specified positions in the array. This modifies the original array.
     *
     * @param array  The array object.
     * @param index1 The first position.
     * @param index2 The second position.
     * @return The array after swapping, which is the same object as the input array.
     */
    public static Object swap(final Object array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Array must not empty !");
        }
        final Object tmp = get(array, index1);
        Array.set(array, index1, Array.get(array, index2));
        Array.set(array, index2, tmp);
        return array;
    }

    /**
     * Removes duplicate elements from the array, generating a new array. The original array remains unchanged. This
     * method uses {@link LinkedHashSet} for deduplication.
     *
     * @param <T>   The array element type.
     * @param array The array.
     * @return The deduplicated array.
     */
    public static <T> T[] distinct(final T[] array) {
        if (isEmpty(array)) {
            return array;
        }

        final Set<T> set = new LinkedHashSet<>(array.length, 1);
        Collections.addAll(set, array);
        return ofArray(set, (Class<T>) getComponentType(array));
    }

    /**
     * Removes duplicate elements from the array based on a unique key generator, generating a new array. The original
     * array remains unchanged. This method uses {@link LinkedHashSet} for deduplication.
     *
     * @param <T>             The array element type.
     * @param <K>             The unique key type.
     * @param array           The array.
     * @param uniqueGenerator The unique key generator.
     * @param override        Whether to use override mode. If {@code true}, new values will overwrite old values with
     *                        the same key; otherwise, new values will be ignored.
     * @return The deduplicated array.
     */
    public static <T, K> T[] distinct(final T[] array, final Function<T, K> uniqueGenerator, final boolean override) {
        if (isEmpty(array)) {
            return array;
        }

        final UniqueKeySet<K, T> set = new UniqueKeySet<>(true, uniqueGenerator);
        if (override) {
            Collections.addAll(set, array);
        } else {
            for (final T t : array) {
                set.addIfAbsent(t);
            }
        }
        return ofArray(set, (Class<T>) getComponentType(array));
    }

    /**
     * Transforms an array of one type to another type according to a specified rule.
     *
     * @param array               The array to be converted.
     * @param targetComponentType The target element type, must be a wrapper type.
     * @param func                The conversion rule function.
     * @param <T>                 The original array type.
     * @param <R>                 The target array type.
     * @return The converted array.
     */
    public static <T, R> R[] map(
            final Object array,
            final Class<R> targetComponentType,
            final Function<? super T, ? extends R> func) {
        final int length = length(array);
        final R[] result = newArray(targetComponentType, length);
        for (int i = 0; i < length; i++) {
            result[i] = func.apply(get(array, i));
        }
        return result;
    }

    /**
     * Transforms array elements of one type to another type according to a specified rule, and saves them as a
     * {@link List}.
     *
     * @param array The array to be converted.
     * @param func  The conversion rule function.
     * @param <T>   The original array element type.
     * @param <R>   The target list element type.
     * @return The list.
     */
    public static <T, R> List<R> mapToList(final T[] array, final Function<? super T, ? extends R> func) {
        return Arrays.stream(array).map(func).collect(Collectors.toList());
    }

    /**
     * Transforms array elements of one type to another type according to a specified rule, and saves them as a
     * {@link Set}.
     *
     * @param array The array to be converted.
     * @param func  The conversion rule function.
     * @param <T>   The original array element type.
     * @param <R>   The target set element type.
     * @return The set.
     */
    public static <T, R> Set<R> mapToSet(final T[] array, final Function<? super T, ? extends R> func) {
        return Arrays.stream(array).map(func).collect(Collectors.toSet());
    }

    /**
     * Transforms array elements of one type to another type according to a specified rule, and saves them as an array.
     *
     * @param array     The array to be converted.
     * @param func      The conversion rule function.
     * @param generator The array generator, e.g., {@code String[]::new} to return {@code String[]}.
     * @param <T>       The original array element type.
     * @param <R>       The target array element type.
     * @return The array.
     */
    public static <T, R> R[] mapToArray(
            final T[] array,
            final Function<? super T, ? extends R> func,
            final IntFunction<R[]> generator) {
        return Arrays.stream(array).map(func).toArray(generator);
    }

    /**
     * Determines if two arrays are equal, based on array length and equality of each element.
     *
     * @param array1 The first array.
     * @param array2 The second array.
     * @return {@code true} if the arrays are equal, {@code false} otherwise.
     */
    public static boolean equals(final Object array1, final Object array2) {
        if (array1 == array2) {
            return true;
        }
        if (hasNull(array1, array2)) {
            return false;
        }

        Assert.isTrue(isArray(array1), "First is not a Array !");
        Assert.isTrue(isArray(array2), "Second is not a Array !");

        if (array1 instanceof long[]) {
            return Arrays.equals((long[]) array1, (long[]) array2);
        } else if (array1 instanceof int[]) {
            return Arrays.equals((int[]) array1, (int[]) array2);
        } else if (array1 instanceof short[]) {
            return Arrays.equals((short[]) array1, (short[]) array2);
        } else if (array1 instanceof char[]) {
            return Arrays.equals((char[]) array1, (char[]) array2);
        } else if (array1 instanceof byte[]) {
            return Arrays.equals((byte[]) array1, (byte[]) array2);
        } else if (array1 instanceof double[]) {
            return Arrays.equals((double[]) array1, (double[]) array2);
        } else if (array1 instanceof float[]) {
            return Arrays.equals((float[]) array1, (float[]) array2);
        } else if (array1 instanceof boolean[]) {
            return Arrays.equals((boolean[]) array1, (boolean[]) array2);
        } else {
            // Not an array of primitives
            return Arrays.deepEquals((Object[]) array1, (Object[]) array2);
        }
    }

    /**
     * Gets a sub-array.
     *
     * @param <T>       The array element type.
     * @param array     The array, must not be null.
     * @param fromIndex The starting position (inclusive).
     * @param toIndex   The ending position (exclusive).
     * @return The new sub-array.
     * @see Arrays#copyOfRange(Object[], int, int)
     */
    public static <T> T[] sub(final T[] array, int fromIndex, int toIndex) {
        Assert.notNull(array, "array must be not null !");
        final int length = length(array);
        if (fromIndex < 0) {
            fromIndex += length;
        }
        if (toIndex < 0) {
            toIndex += length;
        }
        if (fromIndex > toIndex) {
            final int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }
        if (fromIndex >= length) {
            return newArray(array.getClass().getComponentType(), 0);
        }
        if (toIndex > length) {
            toIndex = length;
        }
        return Arrays.copyOfRange(array, fromIndex, toIndex);
    }

    /**
     * Gets a sub-array.
     *
     * @param array     The array.
     * @param fromIndex The starting position (inclusive).
     * @param toIndex   The ending position (exclusive).
     * @param <A>       The array type.
     * @return The new sub-array.
     */
    public static <A> A sub(final A array, final int fromIndex, final int toIndex) {
        return ArrayWrapper.of(array).getSub(fromIndex, toIndex);
    }

    /**
     * Gets a sub-array with a specified step.
     *
     * @param array     The array.
     * @param fromIndex The starting position (inclusive).
     * @param toIndex   The ending position (exclusive).
     * @param step      The step size.
     * @param <A>       The array type.
     * @return The new sub-array.
     */
    public static <A> A sub(final A array, final int fromIndex, final int toIndex, final int step) {
        return ArrayWrapper.of(array).getSub(fromIndex, toIndex, step);
    }

    /**
     * Finds the starting position of the last sub-array.
     *
     * @param array    The array.
     * @param subArray The sub-array.
     * @param <T>      The array element type.
     * @return The starting position of the last sub-array, i.e., the position of the first element of the sub-array in
     *         the array.
     */
    public static <T> int lastIndexOfSub(final T[] array, final T[] subArray) {
        if (isEmpty(array) || isEmpty(subArray)) {
            return Normal.__1;
        }
        return lastIndexOfSub(array, array.length - 1, subArray);
    }

    /**
     * Finds the starting position of the last sub-array, searching backward from a specified inclusive end position.
     *
     * @param array     The array.
     * @param fromIndex The starting position of the search (backward), inclusive.
     * @param subArray  The sub-array.
     * @param <T>       The array element type.
     * @return The starting position of the last sub-array, i.e., the position of the first element of the sub-array in
     *         the array when searching backward.
     */
    public static <T> int lastIndexOfSub(final T[] array, final int fromIndex, final T[] subArray) {
        if (isEmpty(array) || isEmpty(subArray) || subArray.length > array.length || fromIndex < 0) {
            return Normal.__1;
        }

        final int firstIndex = lastIndexOf(array, subArray[0], fromIndex);
        if (firstIndex < 0 || firstIndex + subArray.length > array.length) {
            return Normal.__1;
        }

        for (int i = 0; i < subArray.length; i++) {
            if (!ObjectKit.equals(array[i + firstIndex], subArray[i])) {
                return lastIndexOfSub(array, firstIndex - 1, subArray);
            }
        }

        return firstIndex;
    }

    /**
     * Checks if the array contains duplicate elements.
     * <p>
     * If an empty array is passed, {@code false} is returned.
     *
     * @param <T>   The array element type.
     * @param array The array.
     * @return {@code true} if the array has duplicate elements, {@code false} otherwise.
     */
    public static <T> Boolean hasSameElement(final T[] array) {
        if (isEmpty(array)) {
            return false;
        }

        final Set<T> elementSet = SetKit.of(Arrays.asList(array));
        return elementSet.size() != array.length;
    }

    /**
     * Checks if the {@code array} starts with the {@code prefix}. Each element is matched using
     * {@link ObjectKit#equals(Object, Object)}.
     * <ul>
     * <li>If {@code array} and {@code prefix} are the same array (i.e., {@code array == prefix}), returns
     * {@code true}.</li>
     * <li>If {@code array} or {@code prefix} is an empty array (null or an array with length 0), returns
     * {@code true}.</li>
     * <li>If {@code prefix} length is greater than {@code array} length, returns {@code false}.</li>
     * </ul>
     *
     * @param array  The array.
     * @param prefix The prefix array.
     * @param <T>    The array element type.
     * @return {@code true} if the array starts with the prefix, {@code false} otherwise.
     */
    public static <T> boolean startWith(final T[] array, final T[] prefix) {
        if (array == prefix) {
            return true;
        }
        if (isEmpty(array)) {
            return isEmpty(prefix);
        }
        if (prefix.length > array.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (!ObjectKit.equals(array[i], prefix[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares two given arrays for equality, similar to {@code Arrays.equals}, performing an equality check based on
     * array elements rather than array references.
     *
     * @param o1 The first object to compare.
     * @param o2 The second object to compare.
     * @return {@code true} if the given objects are equal, {@code false} otherwise.
     * @see ObjectKit#nullSafeEquals(Object, Object)
     * @see Arrays#equals
     */
    public static boolean arrayEquals(Object o1, Object o2) {
        if (o1 instanceof Object[] && o2 instanceof Object[]) {
            return Arrays.equals((Object[]) o1, (Object[]) o2);
        }
        if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
            return Arrays.equals((boolean[]) o1, (boolean[]) o2);
        }
        if (o1 instanceof byte[] && o2 instanceof byte[]) {
            return Arrays.equals((byte[]) o1, (byte[]) o2);
        }
        if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals((char[]) o1, (char[]) o2);
        }
        if (o1 instanceof double[] && o2 instanceof double[]) {
            return Arrays.equals((double[]) o1, (double[]) o2);
        }
        if (o1 instanceof float[] && o2 instanceof float[]) {
            return Arrays.equals((float[]) o1, (float[]) o2);
        }
        if (o1 instanceof int[] && o2 instanceof int[]) {
            return Arrays.equals((int[]) o1, (int[]) o2);
        }
        if (o1 instanceof long[] && o2 instanceof long[]) {
            return Arrays.equals((long[]) o1, (long[]) o2);
        }
        if (o1 instanceof short[] && o2 instanceof short[]) {
            return Arrays.equals((short[]) o1, (short[]) o2);
        }
        return false;
    }

}
