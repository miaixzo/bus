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
package org.miaixz.bus.core.center.array;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Validator;
import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Array wrapper, providing a series of array manipulation methods.
 *
 * @param <A> The type of the array.
 * @param <E> The type of elements in the array.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ArrayWrapper<A, E> implements Wrapper<A>, Iterable<E> {

    /**
     * The component type of the array.
     */
    private final Class<E> componentType;
    /**
     * The wrapped array object.
     */
    private A array;
    /**
     * The length of the array.
     */
    private int length;

    /**
     * Constructs an {@code ArrayWrapper} with the given array.
     *
     * @param array The array object (must not be {@code null}).
     * @throws IllegalArgumentException if the provided object is not an array.
     * @throws NullPointerException     if the provided array is {@code null}.
     */
    public ArrayWrapper(final A array) {
        Assert.notNull(array, "Array must be not null!");
        if (!ArrayKit.isArray(array)) {
            throw new IllegalArgumentException("Object is not a array!");
        }
        this.componentType = (Class<E>) array.getClass().getComponentType();
        setNewArray(array);
    }

    /**
     * Creates an {@code ArrayWrapper} with a new empty array of the specified component type and length.
     *
     * @param componentType The element type of the array.
     * @param length        The length of the new array.
     * @param <A>           The type of the array.
     * @param <E>           The type of elements in the array.
     * @return A new {@code ArrayWrapper} instance.
     */
    public static <A, E> ArrayWrapper<A, E> of(final Class<E> componentType, final int length) {
        return (ArrayWrapper<A, E>) of(Array.newInstance(componentType, length));
    }

    /**
     * Wraps an existing array into an {@code ArrayWrapper}.
     *
     * @param array The array to wrap (must not be {@code null}).
     * @param <A>   The type of the array.
     * @param <E>   The type of elements in the array.
     * @return A new {@code ArrayWrapper} instance.
     * @throws IllegalArgumentException if the provided object is not an array.
     * @throws NullPointerException     if the provided array is {@code null}.
     */
    public static <A, E> ArrayWrapper<A, E> of(final A array) {
        return new ArrayWrapper<>(array);
    }

    @Override
    public A getRaw() {
        return this.array;
    }

    /**
     * Gets the length of the wrapped array.
     *
     * @return The length of the array.
     */
    public int length() {
        return length;
    }

    /**
     * Checks if the wrapped array is a primitive type array.
     *
     * @return {@code true} if the array is a primitive type array, {@code false} otherwise.
     */
    public boolean isPrimitive() {
        return this.componentType.isPrimitive();
    }

    /**
     * Gets the component type of the array. Examples:
     * <ul>
     * <li>{@code Object[]} returns {@code Object.class}</li>
     * <li>{@code String[]} returns {@code String.class}</li>
     * <li>{@code int[]} returns {@code int.class}</li>
     * <li>{@code Integer[]} returns {@code Integer.class}</li>
     * <li>{@code null} returns {@code null}</li>
     * <li>{@code String} (non-array) returns {@code null}</li>
     * </ul>
     *
     * @return The component type of the array.
     */
    public Class<?> getComponentType() {
        return this.componentType;
    }

    /**
     * Gets the class type of the wrapped array.
     *
     * @return The class type of the array.
     */
    public Class<?> getArrayType() {
        return array.getClass();
    }

    /**
     * Checks if the wrapped array is empty.
     *
     * @return {@code true} if the array has a length of 0, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return 0 == length;
    }

    /**
     * Gets the element at the specified index in the array. Supports negative indices, where -1 refers to the last
     * element. If the index is out of bounds, {@code null} is returned.
     *
     * @param index The index of the element to retrieve. Supports negative values (e.g., -1 for the last element).
     * @return The element at the specified index, or {@code null} if the index is out of bounds.
     */
    public E get(int index) {
        final int length = this.length;
        if (index < 0) {
            index += length;
        }
        if (index < 0 || index >= length) {
            return null;
        }
        return (E) Array.get(array, index);
    }

    /**
     * Returns the first non-{@code null} element in the array.
     *
     * @return The first non-{@code null} element, or {@code null} if no non-{@code null} elements are found or the
     *         array is empty.
     */
    public E firstNonNull() {
        return firstMatch(ObjectKit::isNotNull);
    }

    /**
     * Returns the first element in the array that matches the given predicate.
     *
     * @param matcher The predicate to use for matching.
     * @return The first matching element, or {@code null} if no matching element is found or the array is empty.
     */
    public E firstMatch(final Predicate<E> matcher) {
        final int index = matchIndex(matcher);
        if (index == Normal.__1) {
            return null;
        }

        return get(index);
    }

    /**
     * Returns the index of the first occurrence of the specified value in the array.
     *
     * @param value The element to search for.
     * @return The index of the first occurrence of the value, or {@link Normal#__1} if not found.
     */
    public int indexOf(final Object value) {
        return matchIndex((object) -> ObjectKit.equals(value, object));
    }

    /**
     * Returns the index of the first element in the array that matches the given predicate.
     *
     * @param matcher The predicate to use for matching.
     * @return The index of the first matching element, or {@link Normal#__1} if not found.
     */
    public int matchIndex(final Predicate<E> matcher) {
        return matchIndex(0, matcher);
    }

    /**
     * Returns the index of the first occurrence of the specified value in the array, starting the search from a
     * specified offset.
     *
     * @param value  The element to search for.
     * @param offset The starting index (inclusive) to begin the search.
     * @return The index of the first occurrence of the value, or {@link Normal#__1} if not found.
     */
    public int indexOf(final Object value, final int offset) {
        return matchIndex(offset, (object) -> ObjectKit.equals(value, object));
    }

    /**
     * Returns the index of the first element in the array that matches the given predicate, starting the search from a
     * specified offset.
     *
     * @param offset  The starting index (inclusive) to begin the search. Must not be negative.
     * @param matcher The predicate to use for matching.
     * @return The index of the first matching element, or {@link Normal#__1} if not found.
     */
    public int matchIndex(final int offset, final Predicate<E> matcher) {
        if (null == matcher && offset < this.length) {
            return offset;
        }
        for (int i = offset; i < length; i++) {
            if (matcher.test(get(i))) {
                return i;
            }
        }

        return Normal.__1;
    }

    /**
     * Returns the index of the last occurrence of the specified value in the array.
     *
     * @param value The element to search for.
     * @return The index of the last occurrence of the value, or {@link Normal#__1} if not found.
     */
    public int lastIndexOf(final Object value) {
        return matchLastIndex((object) -> ObjectKit.equals(value, object));
    }

    /**
     * Returns the index of the last element in the array that matches the given predicate (searching backward).
     *
     * @param matcher The predicate to use for matching.
     * @return The index of the last matching element, or {@link Normal#__1} if not found.
     */
    public int matchLastIndex(final Predicate<E> matcher) {
        return matchLastIndex(length - 1, matcher);
    }

    /**
     * Returns the index of the last element in the array that matches the given predicate (searching backward),
     * starting the search from a specified offset.
     *
     * @param offset  The starting index (inclusive) for the backward search, typically {@code array.length - 1}.
     * @param matcher The predicate to use for matching.
     * @return The index of the last matching element, or {@link Normal#__1} if not found.
     */
    public int matchLastIndex(final int offset, final Predicate<E> matcher) {
        if (null == matcher && offset >= 0) {
            return offset;
        }
        for (int i = Math.min(offset, length - 1); i >= 0; i--) {
            if (matcher.test(get(i))) {
                return i;
            }
        }

        return Normal.__1;
    }

    /**
     * Sets the element at the specified index in the array. If the index is less than the current array length, the
     * element at that position is replaced. If the index is greater than or equal to the current array length, the
     * array is padded with default values ({@code null} for object types, {@code 0} for primitive types) until the
     * index is reached, and then the new value is appended.
     *
     * @param index The index at which to set the element.
     * @param value The new element to set.
     * @return This {@code ArrayWrapper} instance.
     */
    public ArrayWrapper<A, E> setOrPadding(final int index, final E value) {
        return setOrPadding(index, value, (E) ClassKit.getDefaultValue(this.componentType));
    }

    /**
     * Sets the element at the specified index in the array. If the index is less than the current array length, the
     * element at that position is replaced. If the index is greater than or equal to the current array length, the
     * array is padded with the specified {@code paddingElement} until the index is reached, and then the new value is
     * appended.
     *
     * @param index          The index at which to set the element.
     * @param value          The new element to set.
     * @param paddingElement The element to use for padding if the index is beyond the current array length.
     * @return This {@code ArrayWrapper} instance.
     */
    public ArrayWrapper<A, E> setOrPadding(final int index, final E value, final E paddingElement) {
        return setOrPadding(index, value, paddingElement, (this.length + 1) * 10);
    }

    /**
     * Sets the element at the specified index in the array. If the index is less than the current array length, the
     * element at that position is replaced. If the index is greater than or equal to the current array length, the
     * array is padded with the specified {@code paddingElement} until the index is reached, and then the new value is
     * appended. A limit is imposed on how far the index can exceed the current length to prevent excessive memory
     * allocation.
     *
     * @param index          The index at which to set the element.
     * @param value          The new element to set.
     * @param paddingElement The element to use for padding if the index is beyond the current array length.
     * @param indexLimit     The maximum allowed index, used for safety checks to prevent excessively large arrays.
     * @return This {@code ArrayWrapper} instance.
     * @throws IndexOutOfBoundsException if the index exceeds the {@code indexLimit}.
     */
    public ArrayWrapper<A, E> setOrPadding(
            final int index,
            final E value,
            final E paddingElement,
            final int indexLimit) {
        if (index < this.length) {
            Array.set(array, index, value);
        } else {
            // Add a safety check, maximum 10 times the current length
            Validator.checkIndexLimit(index, indexLimit);

            for (int i = length; i < index; i++) {
                append(paddingElement);
            }
            append(value);
        }

        return this;
    }

    /**
     * Sets the element at the specified index in the array. If the index is less than the current array length, the
     * element at that position is replaced. If the index is greater than or equal to the current array length, the new
     * value is appended to the end of the array.
     *
     * @param index The index at which to set the element. If greater than or equal to the current length, the element
     *              is appended.
     * @param value The new element to set or append.
     * @return This {@code ArrayWrapper} instance.
     */
    public ArrayWrapper<A, E> setOrAppend(final int index, final E value) {
        if (index < this.length) {
            Array.set(array, index, value);
        } else {
            append(value);
        }

        return this;
    }

    /**
     * Appends a new element to the end of the array. This operation creates a new array and does not modify the
     * original array.
     *
     * @param element The new element to append.
     * @return A new {@code ArrayWrapper} instance containing the appended element.
     */
    public ArrayWrapper<A, E> append(final E element) {
        return insert(this.length, element);
    }

    /**
     * Appends a new array to the end of the current array. This operation creates a new array and does not modify the
     * original array.
     *
     * @param arrayToAppend The array to append.
     * @return A new {@code ArrayWrapper} instance containing the appended array.
     */
    public ArrayWrapper<A, E> appendArray(final A arrayToAppend) {
        return insertArray(this.length, arrayToAppend);
    }

    /**
     * Inserts a new element into the array at a specified position. If the insertion position is negative, it is
     * counted from the end of the original array. If the insertion position is greater than the original array's
     * length, the gap is filled with default values.
     *
     * @param index   The insertion position. Supports negative values (e.g., -1 inserts before the last element). This
     *                position refers to the gap *before* the element at that index.
     * @param element The element to insert.
     * @return A new {@code ArrayWrapper} instance with the element inserted.
     */
    public ArrayWrapper<A, E> insert(final int index, final E element) {
        return insertArray(index, ArrayKit.wrapSingle(element, this.componentType));
    }

    /**
     * Inserts a new array into the current array at a specified position. If the insertion position is negative, it is
     * counted from the end of the original array. If the insertion position is greater than the original array's
     * length, the gap is filled with default values.
     *
     * @param index         The insertion position. Supports negative values.
     * @param arrayToInsert The new array to insert.
     * @return A new {@code ArrayWrapper} instance with the array inserted.
     */
    public ArrayWrapper<A, E> insertArray(int index, A arrayToInsert) {
        final int appendLength = ArrayKit.length(arrayToInsert);
        if (0 == appendLength) {
            return this;
        }
        if (isEmpty()) {
            setNewArray((A) Convert.convert(array.getClass(), arrayToInsert));
            return this;
        }

        final int len = this.length;
        if (index < 0) {
            index = (index % len) + len;
        }

        // Component type of the existing array
        // If the component type of the existing array is primitive,
        // the new array must be converted to that type to avoid ArrayStoreException.
        if (this.componentType.isPrimitive()) {
            arrayToInsert = (A) Convert.convert(array.getClass(), arrayToInsert);
        }

        final A result = (A) Array.newInstance(this.componentType, Math.max(len, index) + appendLength);
        // Copy original array up to the insertion point
        System.arraycopy(array, 0, result, 0, Math.min(len, index));
        // Append the new array
        System.arraycopy(arrayToInsert, 0, result, index, appendLength);
        if (index < len) {
            // Copy remaining part of the original array
            System.arraycopy(array, index, result, index + appendLength, len - index);
        }
        setNewArray(result);

        return this;
    }

    /**
     * Replaces elements in the array starting from a specified position with new values.
     * <ul>
     * <li>If {@code index} is negative, a new array is generated with the new elements placed at the beginning.</li>
     * <li>If {@code index} is greater than or equal to the old array's length, a new array is generated with the new
     * elements appended to the end.</li>
     * <li>If {@code index + new_elements_count} is greater than the old array's length, a new array is generated where
     * elements before {@code index} are from the old array, and elements from {@code index} onwards are the new
     * elements.</li>
     * <li>Otherwise, elements from the specified {@code index} in the existing array are replaced with the new
     * elements, and the original array (modified) is returned.</li>
     * </ul>
     *
     * @param index  The starting position for replacement.
     * @param values The new values or new array to insert.
     * @return This {@code ArrayWrapper} instance with elements replaced.
     */
    public ArrayWrapper<A, E> replace(final int index, final A values) {
        final int valuesLength = ArrayKit.length(values);
        if (0 == valuesLength) {
            return this;
        }
        if (isEmpty()) {
            setNewArray((A) Convert.convert(array.getClass(), values));
        }
        if (index < 0) {
            // Prepend to the beginning
            return insertArray(0, values);
        }
        if (index >= length) {
            // Out of bounds, append to the end
            return appendArray(values);
        }

        // Within the original array's bounds
        if (length >= valuesLength + index) {
            System.arraycopy(values, 0, this.array, index, valuesLength);
            return this;
        }

        // Out of bounds, replacement length is greater than original array length, create new array
        final A result = (A) Array.newInstance(this.componentType, index + valuesLength);
        System.arraycopy(this.array, 0, result, 0, index);
        System.arraycopy(values, 0, result, index, valuesLength);
        setNewArray(result);

        return this;
    }

    /**
     * Applies a specified operation to each element of the array, replacing the element with the modified result.
     *
     * @param editor The editor interface ({@link UnaryOperator}) to apply to each element. If {@code null}, the
     *               original array is returned.
     * @return This {@code ArrayWrapper} instance with edited elements.
     */
    public ArrayWrapper<A, E> edit(final UnaryOperator<E> editor) {
        if (null == array || null == editor) {
            return this;
        }

        for (int i = 0; i < length; i++) {
            setOrAppend(i, editor.apply(get(i)));
        }
        return this;
    }

    /**
     * Gets a sub-array from the wrapped array.
     *
     * @param beginInclude The starting index (inclusive).
     * @param endExclude   The ending index (exclusive).
     * @return A new array representing the sub-array.
     * @see Arrays#copyOfRange(Object[], int, int)
     */
    public A getSub(int beginInclude, int endExclude) {
        final int length = this.length;
        if (beginInclude < 0) {
            beginInclude += length;
        }
        if (endExclude < 0) {
            endExclude += length;
        }
        if (beginInclude > endExclude) {
            final int tmp = beginInclude;
            beginInclude = endExclude;
            endExclude = tmp;
        }
        if (beginInclude >= length) {
            return (A) Array.newInstance(this.componentType, 0);
        }
        if (endExclude > length) {
            endExclude = length;
        }

        final A result = (A) Array.newInstance(this.componentType, endExclude - beginInclude);
        System.arraycopy(this.array, beginInclude, result, 0, endExclude - beginInclude);
        return result;
    }

    /**
     * Gets a sub-array from the wrapped array with a specified step.
     *
     * @param beginInclude The starting index (inclusive).
     * @param endExclude   The ending index (exclusive).
     * @param step         The step size for iterating through the array. If less than or equal to 1, it defaults to 1.
     * @return A new array representing the sub-array.
     */
    public A getSub(int beginInclude, int endExclude, int step) {
        final int length = this.length;
        if (beginInclude < 0) {
            beginInclude += length;
        }
        if (endExclude < 0) {
            endExclude += length;
        }
        if (beginInclude > endExclude) {
            final int tmp = beginInclude;
            beginInclude = endExclude;
            endExclude = tmp;
        }
        if (beginInclude >= length) {
            return (A) Array.newInstance(this.componentType, 0);
        }
        if (endExclude > length) {
            endExclude = length;
        }

        if (step <= 1) {
            step = 1;
        }

        final int size = (endExclude - beginInclude + step - 1) / step;
        final A result = (A) Array.newInstance(this.componentType, size);
        int j = 0;
        for (int i = beginInclude; i < endExclude; i += step) {
            Array.set(result, j, get(i));
            j++;
        }
        return result;
    }

    /**
     * Checks if the array is sorted in ascending or descending order using the specified comparator.
     * <p>
     * If an empty array is provided, it returns {@code false}.
     * 
     * <p>
     * If all elements are equal, it returns {@code true}.
     * 
     *
     * @param comparator The comparator to use for comparison.
     * @return {@code true} if the array is sorted (ascending or descending), {@code false} otherwise.
     * @throws NullPointerException If array elements contain {@code null} values and the comparator does not handle
     *                              them.
     */
    public boolean isSorted(final Comparator<E> comparator) {
        if (isEmpty()) {
            return false;
        }
        final int lastIndex = this.length - 1;
        // Compare the first and last elements to roughly estimate if the array is ascending or descending
        final int cmp = comparator.compare(get(0), get(lastIndex));
        if (cmp < 0) {
            return isSorted(comparator, false); // Ascending
        } else if (cmp > 0) {
            return isSorted(comparator, true); // Descending
        }

        // Potentially all elements are equal
        for (int i = 0; i < lastIndex; i++) {
            if (comparator.compare(get(i), get(i + 1)) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the array is sorted in a specified order (ascending or descending).
     * <ul>
     * <li>For descending order: returns {@code false} if a previous element is less than a subsequent element.</li>
     * <li>For ascending order: returns {@code false} if a previous element is greater than a subsequent element.</li>
     * </ul>
     *
     * @param comparator The {@link Comparator} to use for comparison.
     * @param isDESC     {@code true} to check for descending order, {@code false} for ascending order.
     * @return {@code true} if the array is sorted in the specified order, {@code false} otherwise.
     */
    public boolean isSorted(final Comparator<E> comparator, final boolean isDESC) {
        if (null == comparator) {
            return false;
        }

        int compare;
        for (int i = 0; i < this.length - 1; i++) {
            compare = comparator.compare(get(i), get(i + 1));
            // Descending: if previous < next, then it's not sorted descending
            if (isDESC && compare < 0) {
                return false;
            }
            // Ascending: if previous > next, then it's not sorted ascending
            if (!isDESC && compare > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterator<E> iterator() {
        return new ArrayIterator<>(this.array);
    }

    @Override
    public String toString() {
        return ArrayKit.toString(this.array);
    }

    /**
     * Sets a new array and updates its length.
     *
     * @param newArray The new array to set.
     */
    private void setNewArray(final A newArray) {
        this.array = newArray;
        this.length = Array.getLength(newArray);
    }

}
