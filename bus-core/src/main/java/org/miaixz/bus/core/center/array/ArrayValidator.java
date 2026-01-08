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
package org.miaixz.bus.core.center.array;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Array validation utility class, providing checks for blank and empty objects.
 * <ul>
 * <li>Empty definition: {@code null} or empty string: {@code ""}</li>
 * <li>Blank definition: {@code null} or empty string: {@code ""} or whitespace characters (spaces, full-width spaces,
 * tabs, newlines, etc.)</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ArrayValidator {

    /**
     * Constructs a new ArrayValidator. Utility class constructor for static access.
     */
    ArrayValidator() {
    }

    /**
     * Checks if the given long array is empty.
     *
     * @param array The long array to check.
     * @return {@code true} if the array is {@code null} or has a length of 0, {@code false} otherwise.
     */
    public static boolean isEmpty(final long[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the given int array is empty.
     *
     * @param array The int array to check.
     * @return {@code true} if the array is {@code null} or has a length of 0, {@code false} otherwise.
     */
    public static boolean isEmpty(final int[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the given short array is empty.
     *
     * @param array The short array to check.
     * @return {@code true} if the array is {@code null} or has a length of 0, {@code false} otherwise.
     */
    public static boolean isEmpty(final short[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the given char array is empty.
     *
     * @param array The char array to check.
     * @return {@code true} if the array is {@code null} or has a length of 0, {@code false} otherwise.
     */
    public static boolean isEmpty(final char[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the given byte array is empty.
     *
     * @param array The byte array to check.
     * @return {@code true} if the array is {@code null} or has a length of 0, {@code false} otherwise.
     */
    public static boolean isEmpty(final byte[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the given double array is empty.
     *
     * @param array The double array to check.
     * @return {@code true} if the array is {@code null} or has a length of 0, {@code false} otherwise.
     */
    public static boolean isEmpty(final double[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the given float array is empty.
     *
     * @param array The float array to check.
     * @return {@code true} if the array is {@code null} or has a length of 0, {@code false} otherwise.
     */
    public static boolean isEmpty(final float[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the given boolean array is empty.
     *
     * @param array The boolean array to check.
     * @return {@code true} if the array is {@code null} or has a length of 0, {@code false} otherwise.
     */
    public static boolean isEmpty(final boolean[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the given long array is not empty.
     *
     * @param array The long array to check.
     * @return {@code true} if the array is not {@code null} and has a length greater than 0, {@code false} otherwise.
     */
    public static boolean isNotEmpty(final long[] array) {
        return !isEmpty(array);
    }

    /**
     * Checks if the given int array is not empty.
     *
     * @param array The int array to check.
     * @return {@code true} if the array is not {@code null} and has a length greater than 0, {@code false} otherwise.
     */
    public static boolean isNotEmpty(final int[] array) {
        return !isEmpty(array);
    }

    /**
     * Checks if the given short array is not empty.
     *
     * @param array The short array to check.
     * @return {@code true} if the array is not {@code null} and has a length greater than 0, {@code false} otherwise.
     */
    public static boolean isNotEmpty(final short[] array) {
        return !isEmpty(array);
    }

    /**
     * Checks if the given char array is not empty.
     *
     * @param array The char array to check.
     * @return {@code true} if the array is not {@code null} and has a length greater than 0, {@code false} otherwise.
     */
    public static boolean isNotEmpty(final char[] array) {
        return !isEmpty(array);
    }

    /**
     * Checks if the given byte array is not empty.
     *
     * @param array The byte array to check.
     * @return {@code true} if the array is not {@code null} and has a length greater than 0, {@code false} otherwise.
     */
    public static boolean isNotEmpty(final byte[] array) {
        return !isEmpty(array);
    }

    /**
     * Checks if the given double array is not empty.
     *
     * @param array The double array to check.
     * @return {@code true} if the array is not {@code null} and has a length greater than 0, {@code false} otherwise.
     */
    public static boolean isNotEmpty(final double[] array) {
        return !isEmpty(array);
    }

    /**
     * Checks if the given float array is not empty.
     *
     * @param array The float array to check.
     * @return {@code true} if the array is not {@code null} and has a length greater than 0, {@code false} otherwise.
     */
    public static boolean isNotEmpty(final float[] array) {
        return !isEmpty(array);
    }

    /**
     * Checks if the given boolean array is not empty.
     *
     * @param array The boolean array to check.
     * @return {@code true} if the array is not {@code null} and has a length greater than 0, {@code false} otherwise.
     */
    public static boolean isNotEmpty(final boolean[] array) {
        return !isEmpty(array);
    }

    /**
     * Checks if the given object is an array.
     *
     * @param object The object to check.
     * @return {@code true} if the object is an array and not {@code null}, {@code false} otherwise.
     */
    public static boolean isArray(final Object object) {
        return null != object && object.getClass().isArray();
    }

    /**
     * <p>
     * Checks if any of the specified {@code CharSequence} array elements are blank.
     * 
     * <p>
     * Returns {@code true} if the array is {@code null} or empty, or if any element is blank.
     * 
     * <ul>
     * <li>{@code hasBlank()                  // true}</li>
     * <li>{@code hasBlank("", null, " ")     // true}</li>
     * <li>{@code hasBlank("123", " ")        // true}</li>
     * <li>{@code hasBlank("123", "abc")      // false}</li>
     * </ul>
     *
     * <p>
     * Note: The difference between this method and {@link #isAllBlank(CharSequence...)} is:
     * 
     * <ul>
     * <li>{@code hasBlank(CharSequence...)} is equivalent to {@code isBlank(...) || isBlank(...) || ...}</li>
     * <li>{@link #isAllBlank(CharSequence...)} is equivalent to {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param args The {@code CharSequence} array to check.
     * @return {@code true} if any element is blank or the array is empty/null, {@code false} otherwise.
     */
    public static boolean hasBlank(final CharSequence... args) {
        if (isEmpty(args)) {
            return true;
        }

        for (final CharSequence text : args) {
            if (StringKit.isBlank(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if all elements in the given {@code CharSequence} array are not {@code null}, empty, or blank. This method
     * uses {@link #hasBlank(CharSequence...)} internally.
     *
     * @param args The {@code CharSequence} array to check.
     * @return {@code true} if all elements are not blank, {@code false} otherwise.
     */
    public static boolean isAllNotBlank(final CharSequence... args) {
        return !hasBlank(args);
    }

    /**
     * <p>
     * Checks if all elements in the specified {@code CharSequence} array are blank.
     * 
     * <p>
     * Returns {@code true} if the array is {@code null} or empty, or if all elements are blank.
     * 
     * <ul>
     * <li>{@code isAllBlank()                  // true}</li>
     * <li>{@code isAllBlank("", null, " ")     // true}</li>
     * <li>{@code isAllBlank("123", " ")        // false}</li>
     * <li>{@code isAllBlank("123", "abc")      // false}</li>
     * </ul>
     *
     * <p>
     * Note: The difference between this method and {@link #hasBlank(CharSequence...)} is:
     * 
     * <ul>
     * <li>{@link #hasBlank(CharSequence...)} is equivalent to {@code isBlank(...) || isBlank(...) || ...}</li>
     * <li>{@code isAllBlank(CharSequence...)} is equivalent to {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param args The {@code CharSequence} array to check.
     * @return {@code true} if all elements are blank or the array is empty/null, {@code false} otherwise.
     */
    public static boolean isAllBlank(final CharSequence... args) {
        if (isEmpty(args)) {
            return true;
        }

        for (final CharSequence text : args) {
            if (StringKit.isNotBlank(text)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given generic array is empty.
     *
     * @param <T>   The type of elements in the array.
     * @param array The generic array to check.
     * @return {@code true} if the array is {@code null} or has a length of 0, {@code false} otherwise.
     */
    public static <T> boolean isEmpty(final T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Returns the original array if it is not empty, otherwise returns the default array.
     *
     * @param <T>          The type of elements in the array.
     * @param array        The array to check.
     * @param defaultArray The default array to return if the original array is empty.
     * @return The original array if not empty, otherwise the default array.
     */
    public static <T> T[] defaultIfEmpty(final T[] array, final T[] defaultArray) {
        return isEmpty(array) ? defaultArray : array;
    }

    /**
     * Checks if the given object, which may be an array, is empty. If the object is {@code null}, it returns
     * {@code true}. If the object is not an array, it is considered a single element, and thus not empty, returning
     * {@code false}. If the object is an array, it returns {@code true} if its length is 0, {@code false} otherwise.
     *
     * @param array The object to check, which can be an array or a single element.
     * @return {@code true} if the object is {@code null} or an empty array, {@code false} otherwise.
     */
    public static boolean isEmpty(final Object array) {
        if (array != null) {
            if (isArray(array)) {
                return 0 == Array.getLength(array);
            }
            return false;
        }
        return true;
    }

    /**
     * Checks if the given generic array is not empty.
     *
     * @param <T>   The type of elements in the array.
     * @param array The generic array to check.
     * @return {@code true} if the array is not {@code null} and has a length greater than 0, {@code false} otherwise.
     */
    public static <T> boolean isNotEmpty(final T[] array) {
        return !isEmpty(array);
    }

    /**
     * Checks if the given object, which may be an array, is not empty. If the object is {@code null}, it returns
     * {@code false}. If the object is not an array, it is considered a single element, and thus not empty, returning
     * {@code true}. If the object is an array, it returns {@code true} if its length is greater than 0, {@code false}
     * otherwise.
     *
     * @param array The object to check, which can be an array or a single element.
     * @return {@code true} if the object is not {@code null} and not an empty array, {@code false} otherwise.
     */
    public static boolean isNotEmpty(final Object array) {
        return !isEmpty(array);
    }

    /**
     * Checks if the array is sorted in ascending or descending order using the specified comparator.
     * <p>
     * If an empty array or a {@code null} comparator is provided, it returns {@code false}.
     * 
     * <p>
     * If all elements are equal, it returns {@code true}.
     * 
     *
     * @param <T>        The type of elements in the array.
     * @param array      The array to check.
     * @param comparator The comparator to use for comparison. It should handle {@code null} values if necessary.
     * @return {@code true} if the array is sorted (ascending or descending), {@code false} otherwise.
     */
    public static <T> boolean isSorted(final T[] array, final Comparator<? super T> comparator) {
        if (isEmpty(array) || null == comparator) {
            return false;
        }

        final int size = array.length - 1;
        final int cmp = comparator.compare(array[0], array[size]);
        if (cmp < 0) {
            return isSortedASC(array, comparator);
        } else if (cmp > 0) {
            return isSortedDESC(array, comparator);
        }
        for (int i = 0; i < size; i++) {
            if (comparator.compare(array[i], array[i + 1]) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the array is sorted in ascending or descending order.
     * <p>
     * If an empty array is provided, it returns {@code false}.
     * 
     * <p>
     * If all elements are equal, it returns {@code true}.
     * 
     *
     * @param <T>   The type of elements in the array, which must implement {@link Comparable}.
     * @param array The array to check.
     * @return {@code true} if the array is sorted (ascending or descending), {@code false} otherwise.
     * @throws NullPointerException If any array element is {@code null}.
     */
    public static <T extends Comparable<? super T>> boolean isSorted(final T[] array) {
        if (isEmpty(array)) {
            return false;
        }
        final int size = array.length - 1;
        final int cmp = array[0].compareTo(array[size]);
        if (cmp < 0) {
            return isSortedASC(array);
        } else if (cmp > 0) {
            return isSortedDESC(array);
        }
        for (int i = 0; i < size; i++) {
            if (array[i].compareTo(array[i + 1]) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the byte array is sorted in ascending order (i.e., {@code array[i] <= array[i+1]}). If an empty array
     * is provided, it returns {@code false}.
     *
     * @param array The byte array to check.
     * @return {@code true} if the array is sorted in ascending order, {@code false} otherwise.
     */
    public static boolean isSortedASC(final byte[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the byte array is sorted in descending order (i.e., {@code array[i] >= array[i+1]}). If an empty array
     * is provided, it returns {@code false}.
     *
     * @param array The byte array to check.
     * @return {@code true} if the array is sorted in descending order, {@code false} otherwise.
     */
    public static boolean isSortedDESC(final byte[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] < array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the short array is sorted in ascending order (i.e., {@code array[i] <= array[i+1]}). If an empty array
     * is provided, it returns {@code false}.
     *
     * @param array The short array to check.
     * @return {@code true} if the array is sorted in ascending order, {@code false} otherwise.
     */
    public static boolean isSortedASC(final short[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the short array is sorted in descending order (i.e., {@code array[i] >= array[i+1]}). If an empty array
     * is provided, it returns {@code false}.
     *
     * @param array The short array to check.
     * @return {@code true} if the array is sorted in descending order, {@code false} otherwise.
     */
    public static boolean isSortedDESC(final short[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] < array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the char array is sorted in ascending order (i.e., {@code array[i] <= array[i+1]}). If an empty array
     * is provided, it returns {@code false}.
     *
     * @param array The char array to check.
     * @return {@code true} if the array is sorted in ascending order, {@code false} otherwise.
     */
    public static boolean isSortedASC(final char[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the char array is sorted in descending order (i.e., {@code array[i] >= array[i+1]}). If an empty array
     * is provided, it returns {@code false}.
     *
     * @param array The char array to check.
     * @return {@code true} if the array is sorted in descending order, {@code false} otherwise.
     */
    public static boolean isSortedDESC(final char[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] < array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the int array is sorted in ascending order (i.e., {@code array[i] <= array[i+1]}). If an empty array is
     * provided, it returns {@code false}.
     *
     * @param array The int array to check.
     * @return {@code true} if the array is sorted in ascending order, {@code false} otherwise.
     */
    public static boolean isSortedASC(final int[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the int array is sorted in descending order (i.e., {@code array[i] >= array[i+1]}). If an empty array
     * is provided, it returns {@code false}.
     *
     * @param array The int array to check.
     * @return {@code true} if the array is sorted in descending order, {@code false} otherwise.
     */
    public static boolean isSortedDESC(final int[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] < array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the long array is sorted in ascending order (i.e., {@code array[i] <= array[i+1]}). If an empty array
     * is provided, it returns {@code false}.
     *
     * @param array The long array to check.
     * @return {@code true} if the array is sorted in ascending order, {@code false} otherwise.
     */
    public static boolean isSortedASC(final long[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the long array is sorted in descending order (i.e., {@code array[i] >= array[i+1]}). If an empty array
     * is provided, it returns {@code false}.
     *
     * @param array The long array to check.
     * @return {@code true} if the array is sorted in descending order, {@code false} otherwise.
     */
    public static boolean isSortedDESC(final long[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] < array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the double array is sorted in ascending order (i.e., {@code array[i] <= array[i+1]}). If an empty array
     * is provided, it returns {@code false}.
     *
     * @param array The double array to check.
     * @return {@code true} if the array is sorted in ascending order, {@code false} otherwise.
     */
    public static boolean isSortedASC(final double[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the double array is sorted in descending order (i.e., {@code array[i] >= array[i+1]}). If an empty
     * array is provided, it returns {@code false}.
     *
     * @param array The double array to check.
     * @return {@code true} if the array is sorted in descending order, {@code false} otherwise.
     */
    public static boolean isSortedDESC(final double[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] < array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the float array is sorted in ascending order (i.e., {@code array[i] <= array[i+1]}). If an empty array
     * is provided, it returns {@code false}.
     *
     * @param array The float array to check.
     * @return {@code true} if the array is sorted in ascending order, {@code false} otherwise.
     */
    public static boolean isSortedASC(final float[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the float array is sorted in descending order (i.e., {@code array[i] >= array[i+1]}). If an empty array
     * is provided, it returns {@code false}.
     *
     * @param array The float array to check.
     * @return {@code true} if the array is sorted in descending order, {@code false} otherwise.
     */
    public static boolean isSortedDESC(final float[] array) {
        if (isEmpty(array)) {
            return false;
        }

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] < array[i + 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the generic array is sorted in ascending order (i.e., {@code array[i].compareTo(array[i + 1]) <= 0}).
     * <p>
     * If an empty array is provided, it returns {@code false}.
     * 
     *
     * @param <T>   The type of elements in the array, which must implement {@link Comparable}.
     * @param array The generic array to check.
     * @return {@code true} if the array is sorted in ascending order, {@code false} otherwise.
     * @throws NullPointerException If any array element is {@code null}.
     */
    public static <T extends Comparable<? super T>> boolean isSortedASC(final T[] array) {
        if (isEmpty(array)) {
            return false;
        }

        final int size = array.length - 1;
        for (int i = 0; i < size; i++) {
            if (array[i].compareTo(array[i + 1]) > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the generic array is sorted in descending order (i.e., {@code array[i].compareTo(array[i + 1]) >= 0}).
     * <p>
     * If an empty array is provided, it returns {@code false}.
     * 
     *
     * @param <T>   The type of elements in the array, which must implement {@link Comparable}.
     * @param array The generic array to check.
     * @return {@code true} if the array is sorted in descending order, {@code false} otherwise.
     * @throws NullPointerException If any array element is {@code null}.
     */
    public static <T extends Comparable<? super T>> boolean isSortedDESC(final T[] array) {
        if (isEmpty(array)) {
            return false;
        }

        final int size = array.length - 1;
        for (int i = 0; i < size; i++) {
            if (array[i].compareTo(array[i + 1]) < 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the generic array is sorted in ascending order using the specified comparator (i.e.,
     * {@code comparator.compare(array[i], array[i + 1]) <= 0}).
     * <p>
     * If an empty array or a {@code null} comparator is provided, it returns {@code false}.
     * 
     *
     * @param <T>        The type of elements in the array.
     * @param array      The generic array to check.
     * @param comparator The comparator to use for comparison. It should handle {@code null} values if necessary.
     * @return {@code true} if the array is sorted in ascending order, {@code false} otherwise.
     */
    public static <T> boolean isSortedASC(final T[] array, final Comparator<? super T> comparator) {
        if (isEmpty(array) || null == comparator) {
            return false;
        }

        final int size = array.length - 1;
        for (int i = 0; i < size; i++) {
            if (comparator.compare(array[i], array[i + 1]) > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the generic array is sorted in descending order using the specified comparator (i.e.,
     * {@code comparator.compare(array[i], array[i + 1]) >= 0}).
     * <p>
     * If an empty array or a {@code null} comparator is provided, it returns {@code false}.
     * 
     *
     * @param <T>        The type of elements in the array.
     * @param array      The generic array to check.
     * @param comparator The comparator to use for comparison. It should handle {@code null} values if necessary.
     * @return {@code true} if the array is sorted in descending order, {@code false} otherwise.
     */
    public static <T> boolean isSortedDESC(final T[] array, final Comparator<? super T> comparator) {
        if (isEmpty(array) || null == comparator) {
            return false;
        }

        final int size = array.length - 1;
        for (int i = 0; i < size; i++) {
            if (comparator.compare(array[i], array[i + 1]) < 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if all elements in the given array are {@code null} or empty objects, using
     * {@link ObjectKit#isEmpty(Object)} to determine emptiness. If the provided array itself is empty, it returns
     * {@code true}.
     *
     * @param <T>  The type of elements in the array.
     * @param args The array of objects to check.
     * @return {@code true} if all elements are {@code null} or empty, {@code false} otherwise.
     */
    public static <T> boolean isAllEmpty(final T[] args) {
        for (final T object : args) {
            if (!ObjectKit.isEmpty(object)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a sub-array matches a portion of a larger array. This is equivalent to comparing the following
     * sub-sequences for equality:
     * 
     * <pre>
     *     array[offset, subArray.length]
     *                  ||
     *               subArray
     * </pre>
     *
     * @param array    The main array.
     * @param offset   The starting offset in the main array.
     * @param subArray The sub-array to compare.
     * @return {@code true} if the sub-array matches the specified portion of the main array, {@code false} otherwise.
     * @throws IndexOutOfBoundsException If {@code offset + subArray.length} exceeds the bounds of {@code array}.
     */
    public static boolean isSubEquals(final byte[] array, final int offset, final byte... subArray) {
        if (array == subArray) {
            return true;
        }
        if (array.length < subArray.length) {
            return false;
        }
        return regionMatches(array, offset, subArray, 0, subArray.length);
    }

    /**
     * Checks if the {@code subArray} is a sub-array of {@code array}.
     *
     * @param array    The main array.
     * @param subArray The sub-array to check for.
     * @param <T>      The type of elements in the arrays.
     * @return {@code true} if {@code subArray} is found within {@code array}, {@code false} otherwise.
     */
    public static <T> boolean isSub(final T[] array, final T[] subArray) {
        return indexOfSub(array, subArray) > Normal.__1;
    }

    /**
     * Finds the starting index of the first occurrence of a sub-array within a larger array, starting the search from a
     * specified index.
     *
     * @param array        The main array to search within.
     * @param beginInclude The starting index (inclusive) in the main array to begin the search.
     * @param subArray     The sub-array to search for.
     * @param <T>          The type of elements in the arrays.
     * @return The starting index of the sub-array if found, or {@link Normal#__1} if not found or if arrays are empty.
     */
    public static <T> int indexOfSub(final T[] array, int beginInclude, final T[] subArray) {
        if (isEmpty(array) || isEmpty(subArray)) {
            return Normal.__1;
        }
        if (beginInclude < 0) {
            beginInclude += array.length;
        }
        if (beginInclude < 0 || beginInclude > array.length - 1) {
            return Normal.__1;
        }
        if (array.length - beginInclude < subArray.length) {
            // Remaining length is insufficient
            return Normal.__1;
        }

        for (int i = beginInclude; i <= array.length - subArray.length; i++) {
            boolean found = true;
            for (int j = 0; j < subArray.length; j++) {
                if (ObjectKit.notEquals(array[i + j], subArray[j])) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }

        return Normal.__1;
    }

    /**
     * Checks if all elements in the given array are {@code null} or empty objects, using
     * {@link ObjectKit#isEmpty(Object)} to determine emptiness.
     * <p>
     * If the provided array itself is empty, it returns {@code true}.
     * 
     * <p>
     * <strong>Constraint: Each item in {@code args} must not be an array or a collection.</strong>
     * 
     *
     * @param <T>  The type of elements in the array.
     * @param args The array of objects to check.
     * @return {@code true} if all elements are {@code null} or empty, {@code false} otherwise.
     * @throws IllegalArgumentException If any item in {@code args} is an array or a collection.
     */
    @SafeVarargs
    public static <T> boolean isAllEmptyVarargs(final T... args) {
        return isAllEmpty(args);
    }

    /**
     * Checks if all elements in the given array are not {@code null} or empty objects, using
     * {@link ObjectKit#isEmpty(Object)} to determine emptiness.
     * <p>
     * If the provided array itself is empty, it returns {@code true}.
     * 
     *
     * @param args The array of objects to check.
     * @return {@code true} if all elements are not {@code null} or empty, {@code false} otherwise.
     */
    public static boolean isAllNotEmpty(final Object... args) {
        return !hasEmpty(args);
    }

    /**
     * Checks if the given array contains any {@code null} elements.
     * <p>
     * If the array is {@code null}, it returns {@code true}.
     * 
     * <p>
     * If the array is empty, it returns {@code false}.
     * 
     *
     * @param <T>   The type of elements in the array.
     * @param array The array to check.
     * @return {@code true} if the array contains any {@code null} elements or is {@code null}, {@code false} otherwise.
     */
    public static <T> boolean hasNull(final T... array) {
        if (isNotEmpty(array)) {
            for (final T element : array) {
                if (ObjectKit.isNull(element)) {
                    return true;
                }
            }
        }
        return array == null;
    }

    /**
     * Checks if the given array contains any {@code null} or empty objects, using {@link ObjectKit#isEmpty(Object)} to
     * determine emptiness.
     * <p>
     * If the provided array itself is empty, it returns {@code false}.
     * 
     *
     * @param <T>  The type of elements in the array.
     * @param args The array of objects to check.
     * @return {@code true} if any element is {@code null} or empty, {@code false} otherwise.
     */
    public static <T> boolean hasEmpty(final T[] args) {
        if (isNotEmpty(args)) {
            for (final T element : args) {
                if (ObjectKit.isEmpty(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the given array contains any {@code null} or empty objects, using {@link ObjectKit#isEmpty(Object)} to
     * determine emptiness.
     * <p>
     * If the provided array itself is empty, it returns {@code false}.
     * 
     * <p>
     * <strong>Constraint: Each item in {@code args} must not be an array or a collection.</strong>
     * 
     *
     * @param <T>  The type of elements in the array.
     * @param args The array of objects to check.
     * @return {@code true} if any element is {@code null} or empty, {@code false} otherwise.
     * @throws IllegalArgumentException If any item in {@code args} is an array or a collection.
     */
    @SafeVarargs
    public static <T> boolean hasEmptyVarargs(final T... args) {
        return hasEmpty(args);
    }

    /**
     * Checks if all elements in the given array are {@code null}.
     * <p>
     * If the array is {@code null} or empty, it returns {@code true}.
     * 
     *
     * @param <T>   The type of elements in the array.
     * @param array The array to check.
     * @return {@code true} if all elements are {@code null} or the array is {@code null}/empty, {@code false}
     *         otherwise.
     */
    public static <T> boolean isAllNull(final T... array) {
        return null == firstNonNull(array);
    }

    /**
     * Checks if all elements in the given array are not {@code null}.
     * <p>
     * If the provided array is {@code null}, it returns {@code false}.
     * 
     * <p>
     * If the provided array is empty, it returns {@code true}.
     * 
     *
     * @param <T>   The type of elements in the array.
     * @param array The array to check.
     * @return {@code true} if all elements are not {@code null} or the array is empty, {@code false} otherwise.
     */
    public static <T> boolean isAllNotNull(final T... array) {
        return !hasNull(array);
    }

    /**
     * Checks if the given array contains any non-{@code null} elements.
     * <p>
     * If the array is {@code null} or empty, it returns {@code false}.
     * 
     * <p>
     * Otherwise, it returns {@code true} if there is at least one non-{@code null} element.
     * 
     *
     * @param <T>   The type of elements in the array.
     * @param array The array to check.
     * @return {@code true} if the array contains at least one non-{@code null} element, {@code false} otherwise.
     */
    public static <T> boolean hasNonNull(final T... array) {
        return null != firstNonNull(array);
    }

    /**
     * Counts the number of {@code null} or empty elements in the given array, using {@link ObjectKit#isEmpty(Object)}
     * to determine emptiness.
     *
     * @param args The array of objects to count.
     * @return The count of {@code null} or empty elements.
     */
    public static int emptyCount(final Object... args) {
        int count = 0;
        if (isNotEmpty(args)) {
            for (final Object element : args) {
                if (ObjectKit.isEmpty(element)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Returns the first non-{@code null} element from the given array.
     *
     * @param <T>   The type of elements in the array.
     * @param array The array to search.
     * @return The first non-{@code null} element, or {@code null} if no non-{@code null} elements are found or the
     *         array is empty.
     */
    public static <T> T firstNonNull(final T... array) {
        if (isEmpty(array)) {
            return null;
        }
        return firstMatch(ObjectKit::isNotNull, array);
    }

    /**
     * Returns the first element in the array that matches the given predicate.
     *
     * @param <T>     The type of elements in the array.
     * @param matcher The predicate to use for matching.
     * @param array   The array to search.
     * @return The first matching element, or {@code null} if no matching element is found or the array is empty.
     */
    public static <T> T firstMatch(final Predicate<T> matcher, final T... array) {
        final int index = matchIndex(matcher, array);
        if (index == Normal.__1) {
            return null;
        }

        return array[index];
    }

    /**
     * Returns the index of the first element in the array that matches the given predicate.
     *
     * @param <T>     The type of elements in the array.
     * @param matcher The predicate to use for matching.
     * @param array   The array to search.
     * @return The index of the first matching element, or {@link Normal#__1} if no matching element is found.
     */
    public static <T> int matchIndex(final Predicate<T> matcher, final T... array) {
        return matchIndex(0, matcher, array);
    }

    /**
     * Returns the index of the first element in the array that matches the given predicate, starting the search from a
     * specified index.
     *
     * @param <E>               The type of elements in the array.
     * @param matcher           The predicate to use for matching.
     * @param beginIndexInclude The starting index (inclusive) to begin the search. Must not be negative.
     * @param array             The array to search.
     * @return The index of the first matching element, or {@link Normal#__1} if no matching element is found.
     */
    public static <E> int matchIndex(final int beginIndexInclude, final Predicate<E> matcher, final E... array) {
        if (isEmpty(array)) {
            return Normal.__1;
        }
        final ArrayWrapper<E[], E> arrayWrapper = ArrayWrapper.of(array);
        return arrayWrapper.matchIndex(beginIndexInclude, matcher);
    }

    /**
     * Returns the index of the first occurrence of the specified value in the array, starting the search from a
     * specified index.
     *
     * @param <T>               The type of elements in the array.
     * @param array             The array to search within.
     * @param value             The element to search for.
     * @param beginIndexInclude The starting index (inclusive) to begin the search.
     * @return The index of the first occurrence of the value, or {@link Normal#__1} if not found.
     */
    public static <T> int indexOf(final T[] array, final Object value, final int beginIndexInclude) {
        return ArrayWrapper.of(array).indexOf(value, beginIndexInclude);
    }

    /**
     * Returns the index of the first occurrence of the specified value in the array.
     *
     * @param <T>   The type of elements in the array.
     * @param array The array to search within.
     * @param value The element to search for.
     * @return The index of the first occurrence of the value, or {@link Normal#__1} if not found.
     */
    public static <T> int indexOf(final T[] array, final Object value) {
        return ArrayWrapper.of(array).indexOf(value);
    }

    /**
     * Returns the index of the first occurrence of the specified {@code CharSequence} value in the array, ignoring case
     * considerations.
     *
     * @param array The array of {@code CharSequence} to search within.
     * @param value The {@code CharSequence} element to search for.
     * @return The index of the first occurrence of the value (case-insensitive), or {@link Normal#__1} if not found.
     */
    public static int indexOfIgnoreCase(final CharSequence[] array, final CharSequence value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (StringKit.equalsIgnoreCase(array[i], value)) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Returns the index of the last occurrence of the specified value in the array.
     *
     * @param <T>   The type of elements in the array.
     * @param array The array to search within.
     * @param value The element to search for.
     * @return The index of the last occurrence of the value, or {@link Normal#__1} if not found.
     */
    public static <T> int lastIndexOf(final T[] array, final Object value) {
        if (isEmpty(array)) {
            return Normal.__1;
        }
        return lastIndexOf(array, value, array.length - 1);
    }

    /**
     * Returns the index of the last occurrence of the specified value in the array, searching backward from a specified
     * starting index.
     *
     * @param <T>        The type of elements in the array.
     * @param array      The array to search within.
     * @param value      The element to search for.
     * @param endInclude The starting index (inclusive) for the backward search, typically {@code array.length - 1}.
     * @return The index of the last occurrence of the value, or {@link Normal#__1} if not found.
     */
    public static <T> int lastIndexOf(final T[] array, final Object value, final int endInclude) {
        if (isNotEmpty(array)) {
            for (int i = endInclude; i >= 0; i--) {
                if (ObjectKit.equals(value, array[i])) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Finds the starting index of the first occurrence of a sub-array within a larger array.
     *
     * @param array    The main array to search within.
     * @param subArray The sub-array to search for.
     * @param <T>      The type of elements in the arrays.
     * @return The starting index of the sub-array if found, or {@link Normal#__1} if not found or if arrays are empty.
     */
    public static <T> int indexOfSub(final T[] array, final T[] subArray) {
        return indexOfSub(array, 0, subArray);
    }

    /**
     * Checks if a region of the first byte array matches a region of the second byte array. This is equivalent to
     * comparing the following sub-sequences for equality:
     * 
     * <pre>
     *     array1[offset1 : offset1 + length]
     *                  ||
     *     array2[offset2 : offset2 + length]
     * </pre>
     *
     * @param array1  The first byte array.
     * @param offset1 The starting offset in the first array.
     * @param array2  The second byte array.
     * @param offset2 The starting offset in the second array.
     * @param length  The number of bytes to compare.
     * @return {@code true} if the specified regions match, {@code false} otherwise.
     * @throws IndexOutOfBoundsException If the specified offsets and length exceed the bounds of either array.
     */
    public static boolean regionMatches(
            final byte[] array1,
            final int offset1,
            final byte[] array2,
            final int offset2,
            final int length) {
        if (array1.length < offset1 + length) {
            throw new IndexOutOfBoundsException("[byte1] length must be >= [offset1 + length]");
        }
        if (array2.length < offset2 + length) {
            throw new IndexOutOfBoundsException("[byte2] length must be >= [offset2 + length]");
        }

        for (int i = 0; i < length; i++) {
            if (array1[i + offset1] != array2[i + offset2]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts an array or collection object to its string representation. The format for arrays is similar to that of
     * collections.
     *
     * @param object The array or collection object to convert.
     * @return The string representation of the array or collection, or {@code null} if the object is {@code null}.
     */
    public static String toString(final Object object) {
        if (Objects.isNull(object)) {
            return null;
        }
        if (object instanceof long[]) {
            return Arrays.toString((long[]) object);
        } else if (object instanceof int[]) {
            return Arrays.toString((int[]) object);
        } else if (object instanceof short[]) {
            return Arrays.toString((short[]) object);
        } else if (object instanceof char[]) {
            return Arrays.toString((char[]) object);
        } else if (object instanceof byte[]) {
            return Arrays.toString((byte[]) object);
        } else if (object instanceof boolean[]) {
            return Arrays.toString((boolean[]) object);
        } else if (object instanceof float[]) {
            return Arrays.toString((float[]) object);
        } else if (object instanceof double[]) {
            return Arrays.toString((double[]) object);
        } else if (ArrayKit.isArray(object)) {
            // Object array
            try {
                return Arrays.deepToString((Object[]) object);
            } catch (final Exception ignore) {
                // ignore
            }
        }

        return object.toString();
    }

    /**
     * Gets the length of an array object. If the parameter is {@code null}, it returns 0.
     *
     * <pre>
     * ArrayKit.length(null)            = 0
     * ArrayKit.length([])              = 0
     * ArrayKit.length([null])          = 1
     * ArrayKit.length([true, false])   = 2
     * ArrayKit.length([1, 2, 3])       = 3
     * ArrayKit.length(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array The array object.
     * @return The length of the array.
     * @throws IllegalArgumentException If the parameter is not an array.
     * @see Array#getLength(Object)
     */
    public static int length(final Object array) throws IllegalArgumentException {
        if (null == array) {
            return 0;
        }
        return Array.getLength(array);
    }

}
