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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.RandomKit;

/**
 * A utility class for working with primitive type arrays.
 *
 * <ol>
 * <li>{@code int[]}</li>
 * <li>{@code long[]}</li>
 * <li>{@code double[]}</li>
 * <li>{@code float[]}</li>
 * <li>{@code short[]}</li>
 * <li>{@code char[]}</li>
 * <li>{@code byte[]}</li>
 * <li>{@code boolean[]}</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrimitiveArray extends ArrayValidator {

    /**
     * Constructs a new PrimitiveArray. Utility class constructor for static access.
     */
    public PrimitiveArray() {
    }

    /**
     * Creates a new array of a specified size, copying elements from the original array. If the new size is larger, the
     * new array is padded with default values (0). If smaller, the array is truncated.
     *
     * @param bytes   The original array.
     * @param newSize The size of the new array.
     * @return The resized new array.
     */
    public static byte[] resize(final byte[] bytes, final int newSize) {
        if (newSize < 0) {
            return bytes;
        }
        final byte[] newArray = new byte[newSize];
        if (newSize > 0 && isNotEmpty(bytes)) {
            System.arraycopy(bytes, 0, newArray, 0, Math.min(bytes.length, newSize));
        }
        return newArray;
    }

    /**
     * Merges multiple primitive arrays into a single new array. Null or empty arrays in the input are ignored.
     *
     * @param arrays The collection of arrays to merge.
     * @return The merged array.
     */
    public static byte[] addAll(final byte[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        // Total
        int length = 0;
        for (final byte[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final byte[] result = new byte[length];
        length = 0;
        for (final byte[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * Merges multiple primitive arrays into a single new array.
     *
     * @param arrays The collection of arrays to merge.
     * @return The merged array.
     */
    public static int[] addAll(final int[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        // Total
        int length = 0;
        for (final int[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final int[] result = new int[length];
        length = 0;
        for (final int[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * Merges multiple primitive arrays into a single new array.
     *
     * @param arrays The collection of arrays to merge.
     * @return The merged array.
     */
    public static long[] addAll(final long[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        int length = 0;
        for (final long[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final long[] result = new long[length];
        length = 0;
        for (final long[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * Merges multiple primitive arrays into a single new array.
     *
     * @param arrays The collection of arrays to merge.
     * @return The merged array.
     */
    public static double[] addAll(final double[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        int length = 0;
        for (final double[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final double[] result = new double[length];
        length = 0;
        for (final double[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * Merges multiple primitive arrays into a single new array.
     *
     * @param arrays The collection of arrays to merge.
     * @return The merged array.
     */
    public static float[] addAll(final float[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        int length = 0;
        for (final float[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final float[] result = new float[length];
        length = 0;
        for (final float[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * Merges multiple primitive arrays into a single new array.
     *
     * @param arrays The collection of arrays to merge.
     * @return The merged array.
     */
    public static char[] addAll(final char[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        int length = 0;
        for (final char[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final char[] result = new char[length];
        length = 0;
        for (final char[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * Merges multiple primitive arrays into a single new array.
     *
     * @param arrays The collection of arrays to merge.
     * @return The merged array.
     */
    public static boolean[] addAll(final boolean[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        int length = 0;
        for (final boolean[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final boolean[] result = new boolean[length];
        length = 0;
        for (final boolean[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * Merges multiple primitive arrays into a single new array.
     *
     * @param arrays The collection of arrays to merge.
     * @return The merged array.
     */
    public static short[] addAll(final short[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        int length = 0;
        for (final short[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final short[] result = new short[length];
        length = 0;
        for (final short[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * Splits a byte array into a two-dimensional array of smaller arrays of a specified size. The last chunk may be
     * smaller than the specified size if the original array's length is not a multiple of the size.
     *
     * @param array The array to split.
     * @param len   The size of each smaller array.
     * @return The split array.
     */
    public static byte[][] split(final byte[] array, final int len) {
        final int amount = array.length / len;
        final int remainder = array.length % len;
        final boolean hasRemainder = remainder > 0;
        final byte[][] arrays = new byte[hasRemainder ? (amount + 1) : amount][];
        int start = 0;
        for (int i = 0; i < amount; i++) {
            byte[] arr = new byte[len];
            System.arraycopy(array, start, arr, 0, len);
            arrays[i] = arr;
            start += len;
        }
        if (hasRemainder) {
            byte[] arr = new byte[remainder];
            System.arraycopy(array, start, arr, 0, remainder);
            arrays[amount] = arr;
        }
        return arrays;
    }

    /**
     * Finds the first index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The index of the value, or -1 if not found.
     */
    public static int indexOf(final long[] array, final long value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Finds the last index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The last index of the value, or -1 if not found.
     */
    public static int lastIndexOf(final long[] array, final long value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Checks if the array contains the given value.
     *
     * @param array The array to check.
     * @param value The value to check for.
     * @return {@code true} if the value is found, {@code false} otherwise.
     */
    public static boolean contains(final long[] array, final long value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * Finds the first index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The index of the value, or -1 if not found.
     */
    public static int indexOf(final int[] array, final int value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Finds the last index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The last index of the value, or -1 if not found.
     */
    public static int lastIndexOf(final int[] array, final int value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Checks if the array contains the given value.
     *
     * @param array The array to check.
     * @param value The value to check for.
     * @return {@code true} if the value is found, {@code false} otherwise.
     */
    public static boolean contains(final int[] array, final int value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * Finds the first index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The index of the value, or -1 if not found.
     */
    public static int indexOf(final short[] array, final short value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Finds the last index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The last index of the value, or -1 if not found.
     */
    public static int lastIndexOf(final short[] array, final short value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Checks if the array contains the given value.
     *
     * @param array The array to check.
     * @param value The value to check for.
     * @return {@code true} if the value is found, {@code false} otherwise.
     */
    public static boolean contains(final short[] array, final short value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * Finds the first index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The index of the value, or -1 if not found.
     */
    public static int indexOf(final char[] array, final char value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Finds the last index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The last index of the value, or -1 if not found.
     */
    public static int lastIndexOf(final char[] array, final char value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Checks if the array contains the given value.
     *
     * @param array The array to check.
     * @param value The value to check for.
     * @return {@code true} if the value is found, {@code false} otherwise.
     */
    public static boolean contains(final char[] array, final char value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * Finds the first index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The index of the value, or -1 if not found.
     */
    public static int indexOf(final byte[] array, final byte value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Finds the last index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The last index of the value, or -1 if not found.
     */
    public static int lastIndexOf(final byte[] array, final byte value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Checks if the array contains the given value.
     *
     * @param array The array to check.
     * @param value The value to check for.
     * @return {@code true} if the value is found, {@code false} otherwise.
     */
    public static boolean contains(final byte[] array, final byte value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * Finds the first index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The index of the value, or -1 if not found.
     */
    public static int indexOf(final double[] array, final double value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (MathKit.equals(value, array[i])) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Finds the last index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The last index of the value, or -1 if not found.
     */
    public static int lastIndexOf(final double[] array, final double value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (MathKit.equals(value, array[i])) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Checks if the array contains the given value.
     *
     * @param array The array to check.
     * @param value The value to check for.
     * @return {@code true} if the value is found, {@code false} otherwise.
     */
    public static boolean contains(final double[] array, final double value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * Finds the first index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The index of the value, or -1 if not found.
     */
    public static int indexOf(final float[] array, final float value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (MathKit.equals(value, array[i])) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Finds the last index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The last index of the value, or -1 if not found.
     */
    public static int lastIndexOf(final float[] array, final float value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (MathKit.equals(value, array[i])) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Checks if the array contains the given value.
     *
     * @param array The array to check.
     * @param value The value to check for.
     * @return {@code true} if the value is found, {@code false} otherwise.
     */
    public static boolean contains(final float[] array, final float value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * Finds the first index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The index of the value, or -1 if not found.
     */
    public static int indexOf(final boolean[] array, final boolean value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Finds the last index of the given value in the array.
     *
     * @param array The array to search in.
     * @param value The value to search for.
     * @return The last index of the value, or -1 if not found.
     */
    public static int lastIndexOf(final boolean[] array, final boolean value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * Checks if the array contains the given value.
     *
     * @param array The array to check.
     * @param value The value to check for.
     * @return {@code true} if the value is found, {@code false} otherwise.
     */
    public static boolean contains(final boolean[] array, final boolean value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * Converts a primitive {@code int[]} array to its corresponding wrapper {@code Integer[]} array.
     *
     * @param values The primitive array.
     * @return The wrapper array.
     */
    public static Integer[] wrap(final int... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Integer[0];
        }

        final Integer[] array = new Integer[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * Converts a wrapper {@code Integer[]} array to its corresponding primitive {@code int[]} array. Null values in the
     * wrapper array are converted to 0.
     *
     * @param values The wrapper array.
     * @return The primitive array.
     */
    public static int[] unWrap(final Integer... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new int[0];
        }

        final int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], 0);
        }
        return array;
    }

    /**
     * Converts a primitive {@code long[]} array to its corresponding wrapper {@code Long[]} array.
     *
     * @param values The primitive array.
     * @return The wrapper array.
     */
    public static Long[] wrap(final long... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Long[0];
        }

        final Long[] array = new Long[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * Converts a wrapper {@code Long[]} array to its corresponding primitive {@code long[]} array. Null values are
     * converted to 0L.
     *
     * @param values The wrapper array.
     * @return The primitive array.
     */
    public static long[] unWrap(final Long... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new long[0];
        }

        final long[] array = new long[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], 0L);
        }
        return array;
    }

    /**
     * Converts a primitive {@code char[]} array to its corresponding wrapper {@code Character[]} array.
     *
     * @param values The primitive array.
     * @return The wrapper array.
     */
    public static Character[] wrap(final char... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Character[0];
        }

        final Character[] array = new Character[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * Converts a wrapper {@code Character[]} array to its corresponding primitive {@code char[]} array. Null values are
     * converted to the null character.
     *
     * @param values The wrapper array.
     * @return The primitive array.
     */
    public static char[] unWrap(final Character... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new char[0];
        }

        final char[] array = new char[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], Character.MIN_VALUE);
        }
        return array;
    }

    /**
     * Converts a primitive {@code byte[]} array to its corresponding wrapper {@code Byte[]} array.
     *
     * @param values The primitive array.
     * @return The wrapper array.
     */
    public static Byte[] wrap(final byte... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Byte[0];
        }

        final Byte[] array = new Byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * Converts a wrapper {@code Byte[]} array to its corresponding primitive {@code byte[]} array. Null values are
     * converted to 0.
     *
     * @param values The wrapper array.
     * @return The primitive array.
     */
    public static byte[] unWrap(final Byte... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new byte[0];
        }

        final byte[] array = new byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], (byte) 0);
        }
        return array;
    }

    /**
     * Converts a primitive {@code short[]} array to its corresponding wrapper {@code Short[]} array.
     *
     * @param values The primitive array.
     * @return The wrapper array.
     */
    public static Short[] wrap(final short... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Short[0];
        }

        final Short[] array = new Short[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * Converts a wrapper {@code Short[]} array to its corresponding primitive {@code short[]} array. Null values are
     * converted to 0.
     *
     * @param values The wrapper array.
     * @return The primitive array.
     */
    public static short[] unWrap(final Short... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new short[0];
        }

        final short[] array = new short[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], (short) 0);
        }
        return array;
    }

    /**
     * Converts a primitive {@code float[]} array to its corresponding wrapper {@code Float[]} array.
     *
     * @param values The primitive array.
     * @return The wrapper array.
     */
    public static Float[] wrap(final float... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Float[0];
        }

        final Float[] array = new Float[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * Converts a wrapper {@code Float[]} array to its corresponding primitive {@code float[]} array. Null values are
     * converted to 0F.
     *
     * @param values The wrapper array.
     * @return The primitive array.
     */
    public static float[] unWrap(final Float... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new float[0];
        }

        final float[] array = new float[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], 0F);
        }
        return array;
    }

    /**
     * Converts a primitive {@code double[]} array to its corresponding wrapper {@code Double[]} array.
     *
     * @param values The primitive array.
     * @return The wrapper array.
     */
    public static Double[] wrap(final double... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Double[0];
        }

        final Double[] array = new Double[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * Converts a wrapper {@code Double[]} array to its corresponding primitive {@code double[]} array. Null values are
     * converted to 0D.
     *
     * @param values The wrapper array.
     * @return The primitive array.
     */
    public static double[] unWrap(final Double... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new double[0];
        }

        final double[] array = new double[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], 0D);
        }
        return array;
    }

    /**
     * Converts a primitive {@code boolean[]} array to its corresponding wrapper {@code Boolean[]} array.
     *
     * @param values The primitive array.
     * @return The wrapper array.
     */
    public static Boolean[] wrap(final boolean... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Boolean[0];
        }

        final Boolean[] array = new Boolean[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * Converts a wrapper {@code Boolean[]} array to its corresponding primitive {@code boolean[]} array. Null values
     * are treated as {@code false}.
     *
     * @param values The wrapper array.
     * @return The primitive array.
     */
    public static boolean[] unWrap(final Boolean... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new boolean[0];
        }

        final boolean[] array = new boolean[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], false);
        }
        return array;
    }

    /**
     * Creates a subarray from the given array.
     *
     * @param array The array.
     * @param start The starting index (inclusive).
     * @param end   The ending index (exclusive).
     * @return The new subarray.
     */
    public static byte[] sub(final byte[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = array.length;
        if (start < 0)
            start += length;
        if (end < 0)
            end += length;
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length)
            return new byte[0];
        if (end > length)
            end = length;
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * Creates a subarray from the given array.
     *
     * @param array The array.
     * @param start The starting index (inclusive).
     * @param end   The ending index (exclusive).
     * @return The new subarray.
     */
    public static int[] sub(final int[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = array.length;
        if (start < 0)
            start += length;
        if (end < 0)
            end += length;
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length)
            return new int[0];
        if (end > length)
            end = length;
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * Creates a subarray from the given array.
     *
     * @param array The array.
     * @param start The starting index (inclusive).
     * @param end   The ending index (exclusive).
     * @return The new subarray.
     */
    public static long[] sub(final long[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = array.length;
        if (start < 0)
            start += length;
        if (end < 0)
            end += length;
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length)
            return new long[0];
        if (end > length)
            end = length;
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * Creates a subarray from the given array.
     *
     * @param array The array.
     * @param start The starting index (inclusive).
     * @param end   The ending index (exclusive).
     * @return The new subarray.
     */
    public static short[] sub(final short[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = array.length;
        if (start < 0)
            start += length;
        if (end < 0)
            end += length;
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length)
            return new short[0];
        if (end > length)
            end = length;
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * Creates a subarray from the given array.
     *
     * @param array The array.
     * @param start The starting index (inclusive).
     * @param end   The ending index (exclusive).
     * @return The new subarray.
     */
    public static char[] sub(final char[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = array.length;
        if (start < 0)
            start += length;
        if (end < 0)
            end += length;
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length)
            return new char[0];
        if (end > length)
            end = length;
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * Creates a subarray from the given array.
     *
     * @param array The array.
     * @param start The starting index (inclusive).
     * @param end   The ending index (exclusive).
     * @return The new subarray.
     */
    public static double[] sub(final double[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = array.length;
        if (start < 0)
            start += length;
        if (end < 0)
            end += length;
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length)
            return new double[0];
        if (end > length)
            end = length;
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * Creates a subarray from the given array.
     *
     * @param array The array.
     * @param start The starting index (inclusive).
     * @param end   The ending index (exclusive).
     * @return The new subarray.
     */
    public static float[] sub(final float[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = array.length;
        if (start < 0)
            start += length;
        if (end < 0)
            end += length;
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length)
            return new float[0];
        if (end > length)
            end = length;
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * Creates a subarray from the given array.
     *
     * @param array The array.
     * @param start The starting index (inclusive).
     * @param end   The ending index (exclusive).
     * @return The new subarray.
     */
    public static boolean[] sub(final boolean[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = array.length;
        if (start < 0)
            start += length;
        if (end < 0)
            end += length;
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length)
            return new boolean[0];
        if (end > length)
            end = length;
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * Removes an element at a specified index from an array, creating a new, smaller array.
     *
     * @param array The source array.
     * @param index The index of the element to remove.
     * @return The new array without the specified element.
     * @throws IllegalArgumentException if the array is null.
     */
    public static long[] remove(final long[] array, final int index) throws IllegalArgumentException {
        return (long[]) remove((Object) array, index);
    }

    /**
     * Removes an element at a specified index.
     * 
     * @param array The source array.
     * @param index The index to remove.
     * @return The new array.
     */
    public static int[] remove(final int[] array, final int index) throws IllegalArgumentException {
        return (int[]) remove((Object) array, index);
    }

    /**
     * Removes an element at a specified index.
     * 
     * @param array The source array.
     * @param index The index to remove.
     * @return The new array.
     */
    public static short[] remove(final short[] array, final int index) throws IllegalArgumentException {
        return (short[]) remove((Object) array, index);
    }

    /**
     * Removes an element at a specified index.
     * 
     * @param array The source array.
     * @param index The index to remove.
     * @return The new array.
     */
    public static char[] remove(final char[] array, final int index) throws IllegalArgumentException {
        return (char[]) remove((Object) array, index);
    }

    /**
     * Removes an element at a specified index.
     * 
     * @param array The source array.
     * @param index The index to remove.
     * @return The new array.
     */
    public static byte[] remove(final byte[] array, final int index) throws IllegalArgumentException {
        return (byte[]) remove((Object) array, index);
    }

    /**
     * Removes an element at a specified index.
     * 
     * @param array The source array.
     * @param index The index to remove.
     * @return The new array.
     */
    public static double[] remove(final double[] array, final int index) throws IllegalArgumentException {
        return (double[]) remove((Object) array, index);
    }

    /**
     * Removes an element at a specified index.
     * 
     * @param array The source array.
     * @param index The index to remove.
     * @return The new array.
     */
    public static float[] remove(final float[] array, final int index) throws IllegalArgumentException {
        return (float[]) remove((Object) array, index);
    }

    /**
     * Removes an element at a specified index.
     * 
     * @param array The source array.
     * @param index The index to remove.
     * @return The new array.
     */
    public static boolean[] remove(final boolean[] array, final int index) throws IllegalArgumentException {
        return (boolean[]) remove((Object) array, index);
    }

    /**
     * Removes an element at a specified index from a generic array (primitive or object).
     *
     * @param array The source array.
     * @param index The index of the element to remove.
     * @return The new array without the specified element, or the original array if the index is out of bounds.
     * @throws IllegalArgumentException if the provided object is not an array.
     */
    public static Object remove(final Object array, final int index) throws IllegalArgumentException {
        if (null == array) {
            return null;
        }
        final int length = Array.getLength(array);
        if (index < 0 || index >= length) {
            return array;
        }

        final Object result = Array.newInstance(array.getClass().getComponentType(), length - 1);
        System.arraycopy(array, 0, result, 0, index);
        if (index < length - 1) {
            System.arraycopy(array, index + 1, result, index, length - index - 1);
        }

        return result;
    }

    /**
     * Removes the first occurrence of a specified element from an array.
     *
     * @param array   The source array.
     * @param element The element to remove.
     * @return The new array without the first occurrence of the element.
     * @throws IllegalArgumentException if the object is not an array.
     */
    public static long[] removeEle(final long[] array, final long element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * Removes the first occurrence of a specified element.
     * 
     * @param array   The source array.
     * @param element The element to remove.
     * @return The new array.
     */
    public static int[] removeEle(final int[] array, final int element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * Removes the first occurrence of a specified element.
     * 
     * @param array   The source array.
     * @param element The element to remove.
     * @return The new array.
     */
    public static short[] removeEle(final short[] array, final short element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * Removes the first occurrence of a specified element.
     * 
     * @param array   The source array.
     * @param element The element to remove.
     * @return The new array.
     */
    public static char[] removeEle(final char[] array, final char element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * Removes the first occurrence of a specified element.
     * 
     * @param array   The source array.
     * @param element The element to remove.
     * @return The new array.
     */
    public static byte[] removeEle(final byte[] array, final byte element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * Removes the first occurrence of a specified element.
     * 
     * @param array   The source array.
     * @param element The element to remove.
     * @return The new array.
     */
    public static double[] removeEle(final double[] array, final double element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * Removes the first occurrence of a specified element.
     * 
     * @param array   The source array.
     * @param element The element to remove.
     * @return The new array.
     */
    public static float[] removeEle(final float[] array, final float element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * Removes the first occurrence of a specified element.
     * 
     * @param array   The source array.
     * @param element The element to remove.
     * @return The new array.
     */
    public static boolean[] removeEle(final boolean[] array, final boolean element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * Removes specified characters from a char array.
     *
     * @param array    The original char array
     * @param elements The characters to remove
     * @return A new array with specified characters removed
     */
    public static char[] removeElements(final char[] array, final char... elements) {
        if (isEmpty(array) || isEmpty(elements)) {
            return array;
        }

        return filter(array, t -> !ArrayKit.contains(elements, t));
    }

    /**
     * Removes specified byte elements from a byte array.
     *
     * @param array    The original byte array
     * @param elements The byte elements to remove
     * @return A new array with specified elements removed
     */
    public static byte[] removeElements(final byte[] array, final byte... elements) {
        if (isEmpty(array) || isEmpty(elements)) {
            return array;
        }

        return filter(array, t -> !contains(elements, t));
    }

    /**
     * Removes specified short elements from a short array.
     *
     * @param array    The original short array
     * @param elements The short elements to remove
     * @return A new array with specified elements removed
     */
    public static short[] removeElements(final short[] array, final short... elements) {
        if (isEmpty(array) || isEmpty(elements)) {
            return array;
        }

        return filter(array, t -> !contains(elements, t));
    }

    /**
     * Removes specified int elements from an int array.
     *
     * @param array    The original int array
     * @param elements The int elements to remove
     * @return A new array with specified elements removed
     */
    public static int[] removeElements(final int[] array, final int... elements) {
        if (isEmpty(array) || isEmpty(elements)) {
            return array;
        }

        return filter(array, t -> !contains(elements, t));
    }

    /**
     * Removes specified long elements from a long array.
     *
     * @param array    The original long array
     * @param elements The long elements to remove
     * @return A new array with specified elements removed
     */
    public static long[] removeElements(final long[] array, final long... elements) {
        if (isEmpty(array) || isEmpty(elements)) {
            return array;
        }

        return filter(array, t -> !contains(elements, t));
    }

    /**
     * Removes specified float elements from a float array.
     *
     * @param array    The original float array
     * @param elements The float elements to remove
     * @return A new array with specified elements removed
     */
    public static float[] removeElements(final float[] array, final float... elements) {
        if (isEmpty(array) || isEmpty(elements)) {
            return array;
        }

        return filter(array, t -> !contains(elements, t));
    }

    /**
     * Removes specified double elements from a double array.
     *
     * @param array    The original double array
     * @param elements The double elements to remove
     * @return A new array with specified elements removed
     */
    public static double[] removeElements(final double[] array, final double... elements) {
        if (isEmpty(array) || isEmpty(elements)) {
            return array;
        }

        return filter(array, t -> !contains(elements, t));
    }

    /**
     * Removes specified boolean elements from a boolean array.
     *
     * @param array    The original boolean array
     * @param elements The boolean elements to remove
     * @return A new array with specified elements removed
     */
    public static boolean[] removeElements(final boolean[] array, final boolean... elements) {
        if (isEmpty(array) || isEmpty(elements)) {
            return array;
        }

        return filter(array, t -> !contains(elements, t));
    }

    /**
     * Applies a specified operation to each array element and returns the modified elements.<br>
     * This Editor implementation can achieve the following functions:
     * <ol>
     * <li>Filter out required objects, discard element objects if {@code null} is returned</li>
     * <li>Modify element objects and return the modified objects</li>
     * </ol>
     *
     * @param array  The array
     * @param editor The editor interface, returns the original array if {@code null}
     * @return The edited array
     */
    public static char[] edit(final char[] array, final UnaryOperator<Character> editor) {
        if (null == array || null == editor) {
            return array;
        }

        final List<Character> resultList = new ArrayList<>(array.length);
        Character modified;
        for (final Character t : array) {
            modified = editor.apply(t);
            if (null != modified) {
                resultList.add(modified);
            }
        }

        // Convert List to char[] array
        final int size = resultList.size();
        final char[] resultArray = new char[size];
        for (int i = 0; i < size; i++) {
            resultArray[i] = resultList.get(i);
        }

        return resultArray;
    }

    /**
     * Applies a specified operation to each byte array element and returns the modified elements.<br>
     * This Editor implementation can achieve the following functions:
     * <ol>
     * <li>Filter out required objects, discard element objects if {@code null} is returned</li>
     * <li>Modify element objects and return the modified objects</li>
     * </ol>
     *
     * @param array  The array
     * @param editor The editor interface, returns the original array if {@code null}
     * @return The edited array
     */
    public static byte[] edit(final byte[] array, final UnaryOperator<Byte> editor) {
        if (null == array || null == editor) {
            return array;
        }

        final List<Byte> resultList = new ArrayList<>(array.length);
        Byte modified;
        for (final Byte t : array) {
            modified = editor.apply(t);
            if (null != modified) {
                resultList.add(modified);
            }
        }

        // Convert List to byte[] array
        final int size = resultList.size();
        final byte[] resultArray = new byte[size];
        for (int i = 0; i < size; i++) {
            resultArray[i] = resultList.get(i);
        }

        return resultArray;
    }

    /**
     * Applies a specified operation to each short array element and returns the modified elements.<br>
     * This Editor implementation can achieve the following functions:
     * <ol>
     * <li>Filter out required objects, discard element objects if {@code null} is returned</li>
     * <li>Modify element objects and return the modified objects</li>
     * </ol>
     *
     * @param array  The array
     * @param editor The editor interface, returns the original array if {@code null}
     * @return The edited array
     */
    public static short[] edit(final short[] array, final UnaryOperator<Short> editor) {
        if (null == array || null == editor) {
            return array;
        }

        final List<Short> resultList = new ArrayList<>(array.length);
        Short modified;
        for (final Short t : array) {
            modified = editor.apply(t);
            if (null != modified) {
                resultList.add(modified);
            }
        }

        // Convert List to short[] array
        final int size = resultList.size();
        final short[] resultArray = new short[size];
        for (int i = 0; i < size; i++) {
            resultArray[i] = resultList.get(i);
        }

        return resultArray;
    }

    /**
     * Applies a specified operation to each int array element and returns the modified elements.<br>
     * This Editor implementation can achieve the following functions:
     * <ol>
     * <li>Filter out required objects, discard element objects if {@code null} is returned</li>
     * <li>Modify element objects and return the modified objects</li>
     * </ol>
     *
     * @param array  The array
     * @param editor The editor interface, returns the original array if {@code null}
     * @return The edited array
     */
    public static int[] edit(final int[] array, final UnaryOperator<Integer> editor) {
        if (null == array || null == editor) {
            return array;
        }

        final List<Integer> resultList = new ArrayList<>(array.length);
        Integer modified;
        for (final Integer t : array) {
            modified = editor.apply(t);
            if (null != modified) {
                resultList.add(modified);
            }
        }

        // Convert List to int[] array
        final int size = resultList.size();
        final int[] resultArray = new int[size];
        for (int i = 0; i < size; i++) {
            resultArray[i] = resultList.get(i);
        }

        return resultArray;
    }

    /**
     * Applies a specified operation to each long array element and returns the modified elements.<br>
     * This Editor implementation can achieve the following functions:
     * <ol>
     * <li>Filter out required objects, discard element objects if {@code null} is returned</li>
     * <li>Modify element objects and return the modified objects</li>
     * </ol>
     *
     * @param array  The array
     * @param editor The editor interface, returns the original array if {@code null}
     * @return The edited array
     */
    public static long[] edit(final long[] array, final UnaryOperator<Long> editor) {
        if (null == array || null == editor) {
            return array;
        }

        final List<Long> resultList = new ArrayList<>(array.length);
        Long modified;
        for (final Long t : array) {
            modified = editor.apply(t);
            if (null != modified) {
                resultList.add(modified);
            }
        }

        // Convert List to long[] array
        final int size = resultList.size();
        final long[] resultArray = new long[size];
        for (int i = 0; i < size; i++) {
            resultArray[i] = resultList.get(i);
        }

        return resultArray;
    }

    /**
     * Applies a specified operation to each float array element and returns the modified elements.<br>
     * This Editor implementation can achieve the following functions:
     * <ol>
     * <li>Filter out required objects, discard element objects if {@code null} is returned</li>
     * <li>Modify element objects and return the modified objects</li>
     * </ol>
     *
     * @param array  The array
     * @param editor The editor interface, returns the original array if {@code null}
     * @return The edited array
     */
    public static float[] edit(final float[] array, final UnaryOperator<Float> editor) {
        if (null == array || null == editor) {
            return array;
        }

        final List<Float> resultList = new ArrayList<>(array.length);
        Float modified;
        for (final Float t : array) {
            modified = editor.apply(t);
            if (null != modified) {
                resultList.add(modified);
            }
        }

        // Convert List to float[] array
        final int size = resultList.size();
        final float[] resultArray = new float[size];
        for (int i = 0; i < size; i++) {
            resultArray[i] = resultList.get(i);
        }

        return resultArray;
    }

    /**
     * Applies a specified operation to each double array element and returns the modified elements.<br>
     * This Editor implementation can achieve the following functions:
     * <ol>
     * <li>Filter out required objects, discard element objects if {@code null} is returned</li>
     * <li>Modify element objects and return the modified objects</li>
     * </ol>
     *
     * @param array  The array
     * @param editor The editor interface, returns the original array if {@code null}
     * @return The edited array
     */
    public static double[] edit(final double[] array, final UnaryOperator<Double> editor) {
        if (null == array || null == editor) {
            return array;
        }

        final List<Double> resultList = new ArrayList<>(array.length);
        Double modified;
        for (final Double t : array) {
            modified = editor.apply(t);
            if (null != modified) {
                resultList.add(modified);
            }
        }

        // Convert List to double[] array
        final int size = resultList.size();
        final double[] resultArray = new double[size];
        for (int i = 0; i < size; i++) {
            resultArray[i] = resultList.get(i);
        }

        return resultArray;
    }

    /**
     * Applies a specified operation to each boolean array element and returns the modified elements.<br>
     * This Editor implementation can achieve the following functions:
     * <ol>
     * <li>Filter out required objects, discard element objects if {@code null} is returned</li>
     * <li>Modify element objects and return the modified objects</li>
     * </ol>
     *
     * @param array  The array
     * @param editor The editor interface, returns the original array if {@code null}
     * @return The edited array
     */
    public static boolean[] edit(final boolean[] array, final UnaryOperator<Boolean> editor) {
        if (null == array || null == editor) {
            return array;
        }

        final List<Boolean> resultList = new ArrayList<>(array.length);
        Boolean modified;
        for (final Boolean t : array) {
            modified = editor.apply(t);
            if (null != modified) {
                resultList.add(modified);
            }
        }

        // Convert List to boolean[] array
        final int size = resultList.size();
        final boolean[] resultArray = new boolean[size];
        for (int i = 0; i < size; i++) {
            resultArray[i] = resultList.get(i);
        }

        return resultArray;
    }

    /**
     * Filters array elements<br>
     * Keeps elements where {@link Predicate#test(Object)} returns {@code true}
     *
     * @param array     The array
     * @param predicate The filter interface for defining filter rules, returns the original array if {@code null}
     * @return The filtered array
     */
    public static char[] filter(final char[] array, final Predicate<Character> predicate) {
        if (null == array || null == predicate) {
            return array;
        }
        return edit(array, t -> predicate.test(t) ? t : null);
    }

    /**
     * Filters byte array elements<br>
     * Keeps elements where {@link Predicate#test(Object)} returns {@code true}
     *
     * @param array     The array
     * @param predicate The filter interface for defining filter rules, returns the original array if {@code null}
     * @return The filtered array
     */
    public static byte[] filter(final byte[] array, final Predicate<Byte> predicate) {
        if (null == array || null == predicate) {
            return array;
        }
        return edit(array, t -> predicate.test(t) ? t : null);
    }

    /**
     * Filters short array elements<br>
     * Keeps elements where {@link Predicate#test(Object)} returns {@code true}
     *
     * @param array     The array
     * @param predicate The filter interface for defining filter rules, returns the original array if {@code null}
     * @return The filtered array
     */
    public static short[] filter(final short[] array, final Predicate<Short> predicate) {
        if (null == array || null == predicate) {
            return array;
        }
        return edit(array, t -> predicate.test(t) ? t : null);
    }

    /**
     * Filters int array elements<br>
     * Keeps elements where {@link Predicate#test(Object)} returns {@code true}
     *
     * @param array     The array
     * @param predicate The filter interface for defining filter rules, returns the original array if {@code null}
     * @return The filtered array
     */
    public static int[] filter(final int[] array, final Predicate<Integer> predicate) {
        if (null == array || null == predicate) {
            return array;
        }
        return edit(array, t -> predicate.test(t) ? t : null);
    }

    /**
     * Filters long array elements<br>
     * Keeps elements where {@link Predicate#test(Object)} returns {@code true}
     *
     * @param array     The array
     * @param predicate The filter interface for defining filter rules, returns the original array if {@code null}
     * @return The filtered array
     */
    public static long[] filter(final long[] array, final Predicate<Long> predicate) {
        if (null == array || null == predicate) {
            return array;
        }
        return edit(array, t -> predicate.test(t) ? t : null);
    }

    /**
     * Filters float array elements<br>
     * Keeps elements where {@link Predicate#test(Object)} returns {@code true}
     *
     * @param array     The array
     * @param predicate The filter interface for defining filter rules, returns the original array if {@code null}
     * @return The filtered array
     */
    public static float[] filter(final float[] array, final Predicate<Float> predicate) {
        if (null == array || null == predicate) {
            return array;
        }
        return edit(array, t -> predicate.test(t) ? t : null);
    }

    /**
     * Filters double array elements<br>
     * Keeps elements where {@link Predicate#test(Object)} returns {@code true}
     *
     * @param array     The array
     * @param predicate The filter interface for defining filter rules, returns the original array if {@code null}
     * @return The filtered array
     */
    public static double[] filter(final double[] array, final Predicate<Double> predicate) {
        if (null == array || null == predicate) {
            return array;
        }
        return edit(array, t -> predicate.test(t) ? t : null);
    }

    /**
     * Filters boolean array elements<br>
     * Keeps elements where {@link Predicate#test(Object)} returns {@code true}
     *
     * @param array     The array
     * @param predicate The filter interface for defining filter rules, returns the original array if {@code null}
     * @return The filtered array
     */
    public static boolean[] filter(final boolean[] array, final Predicate<Boolean> predicate) {
        if (null == array || null == predicate) {
            return array;
        }
        return edit(array, t -> predicate.test(t) ? t : null);
    }

    /**
     * Reverses the order of elements in a portion of an array in-place.
     *
     * @param array               The array to reverse.
     * @param startIndexInclusive The starting index (inclusive).
     * @param endIndexExclusive   The ending index (exclusive).
     * @return The modified array.
     */
    public static long[] reverse(final long[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * Reverses the order of elements in an array in-place.
     *
     * @param array The array to reverse.
     * @return The modified array.
     */
    public static long[] reverse(final long[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * Reverses the order of elements in a portion of an array in-place.
     * 
     * @param array               The array to reverse.
     * @param startIndexInclusive The starting index (inclusive).
     * @param endIndexExclusive   The ending index (exclusive).
     * @return The modified array.
     */
    public static int[] reverse(final int[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * Reverses the order of elements in an array in-place.
     * 
     * @param array The array to reverse.
     * @return The modified array.
     */
    public static int[] reverse(final int[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * Reverses the order of elements in a portion of an array in-place.
     * 
     * @param array               The array to reverse.
     * @param startIndexInclusive The starting index (inclusive).
     * @param endIndexExclusive   The ending index (exclusive).
     * @return The modified array.
     */
    public static short[] reverse(final short[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * Reverses the order of elements in an array in-place.
     * 
     * @param array The array to reverse.
     * @return The modified array.
     */
    public static short[] reverse(final short[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * Reverses the order of elements in a portion of an array in-place.
     * 
     * @param array               The array to reverse.
     * @param startIndexInclusive The starting index (inclusive).
     * @param endIndexExclusive   The ending index (exclusive).
     * @return The modified array.
     */
    public static char[] reverse(final char[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * Reverses the order of elements in an array in-place.
     * 
     * @param array The array to reverse.
     * @return The modified array.
     */
    public static char[] reverse(final char[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * Reverses the order of elements in a portion of an array in-place.
     * 
     * @param array               The array to reverse.
     * @param startIndexInclusive The starting index (inclusive).
     * @param endIndexExclusive   The ending index (exclusive).
     * @return The modified array.
     */
    public static byte[] reverse(final byte[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * Reverses the order of elements in an array in-place.
     * 
     * @param array The array to reverse.
     * @return The modified array.
     */
    public static byte[] reverse(final byte[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * Reverses the order of elements in a portion of an array in-place.
     * 
     * @param array               The array to reverse.
     * @param startIndexInclusive The starting index (inclusive).
     * @param endIndexExclusive   The ending index (exclusive).
     * @return The modified array.
     */
    public static double[] reverse(final double[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * Reverses the order of elements in an array in-place.
     * 
     * @param array The array to reverse.
     * @return The modified array.
     */
    public static double[] reverse(final double[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * Reverses the order of elements in a portion of an array in-place.
     * 
     * @param array               The array to reverse.
     * @param startIndexInclusive The starting index (inclusive).
     * @param endIndexExclusive   The ending index (exclusive).
     * @return The modified array.
     */
    public static float[] reverse(final float[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * Reverses the order of elements in an array in-place.
     * 
     * @param array The array to reverse.
     * @return The modified array.
     */
    public static float[] reverse(final float[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * Reverses the order of elements in a portion of an array in-place.
     * 
     * @param array               The array to reverse.
     * @param startIndexInclusive The starting index (inclusive).
     * @param endIndexExclusive   The ending index (exclusive).
     * @return The modified array.
     */
    public static boolean[] reverse(final boolean[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * Reverses the order of elements in an array in-place.
     * 
     * @param array The array to reverse.
     * @return The modified array.
     */
    public static boolean[] reverse(final boolean[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * Finds the minimum value in a primitive number array.
     *
     * @param numberArray The array of numbers.
     * @return The minimum value.
     */
    public static long min(final long... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        long min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * Finds the minimum value in a primitive number array.
     * 
     * @param numberArray The array of numbers.
     * @return The minimum value.
     */
    public static int min(final int... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        int min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * Finds the minimum value in a primitive number array.
     * 
     * @param numberArray The array of numbers.
     * @return The minimum value.
     */
    public static short min(final short... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        short min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * Finds the minimum value in a primitive character array.
     * 
     * @param numberArray The array of characters.
     * @return The minimum value.
     */
    public static char min(final char... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        char min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * Finds the minimum value in a primitive number array.
     * 
     * @param numberArray The array of numbers.
     * @return The minimum value.
     */
    public static byte min(final byte... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        byte min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * Finds the minimum value in a primitive number array.
     * 
     * @param numberArray The array of numbers.
     * @return The minimum value.
     */
    public static double min(final double... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        double min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (Double.compare(min, numberArray[i]) > 0) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * Finds the minimum value in a primitive number array.
     * 
     * @param numberArray The array of numbers.
     * @return The minimum value.
     */
    public static float min(final float... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        float min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (Float.compare(min, numberArray[i]) > 0) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * Finds the maximum value in a primitive number array.
     *
     * @param numberArray The array of numbers.
     * @return The maximum value.
     */
    public static long max(final long... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        long max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * Finds the maximum value in a primitive number array.
     * 
     * @param numberArray The array of numbers.
     * @return The maximum value.
     */
    public static int max(final int... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        int max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * Finds the maximum value in a primitive number array.
     * 
     * @param numberArray The array of numbers.
     * @return The maximum value.
     */
    public static short max(final short... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        short max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * Finds the maximum value in a primitive character array.
     * 
     * @param numberArray The array of characters.
     * @return The maximum value.
     */
    public static char max(final char... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        char max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * Finds the maximum value in a primitive number array.
     * 
     * @param numberArray The array of numbers.
     * @return The maximum value.
     */
    public static byte max(final byte... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        byte max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * Finds the maximum value in a primitive number array.
     * 
     * @param numberArray The array of numbers.
     * @return The maximum value.
     */
    public static double max(final double... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        double max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (Double.compare(max, numberArray[i]) < 0) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * Finds the maximum value in a primitive number array.
     * 
     * @param numberArray The array of numbers.
     * @return The maximum value.
     */
    public static float max(final float... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not be empty!");
        }
        float max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (Float.compare(max, numberArray[i]) < 0) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * Shuffles the elements of an array in-place using the Fisher-Yates algorithm.
     *
     * @param array The array to shuffle.
     * @return The shuffled array (the same instance as the input).
     */
    public static int[] shuffle(final int[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * Shuffles the elements of an array in-place using the Fisher-Yates algorithm.
     *
     * @param array  The array to shuffle.
     * @param random The random number generator to use.
     * @return The shuffled array (the same instance as the input).
     */
    public static int[] shuffle(final int[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }
        return array;
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array The array to shuffle.
     * @return The shuffled array.
     */
    public static long[] shuffle(final long[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array  The array to shuffle.
     * @param random The random number generator.
     * @return The shuffled array.
     */
    public static long[] shuffle(final long[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }
        return array;
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array The array to shuffle.
     * @return The shuffled array.
     */
    public static double[] shuffle(final double[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array  The array to shuffle.
     * @param random The random number generator.
     * @return The shuffled array.
     */
    public static double[] shuffle(final double[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }
        return array;
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array The array to shuffle.
     * @return The shuffled array.
     */
    public static float[] shuffle(final float[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array  The array to shuffle.
     * @param random The random number generator.
     * @return The shuffled array.
     */
    public static float[] shuffle(final float[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }
        return array;
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array The array to shuffle.
     * @return The shuffled array.
     */
    public static boolean[] shuffle(final boolean[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array  The array to shuffle.
     * @param random The random number generator.
     * @return The shuffled array.
     */
    public static boolean[] shuffle(final boolean[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }
        return array;
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array The array to shuffle.
     * @return The shuffled array.
     */
    public static byte[] shuffle(final byte[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array  The array to shuffle.
     * @param random The random number generator.
     * @return The shuffled array.
     */
    public static byte[] shuffle(final byte[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }
        return array;
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array The array to shuffle.
     * @return The shuffled array.
     */
    public static char[] shuffle(final char[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array  The array to shuffle.
     * @param random The random number generator.
     * @return The shuffled array.
     */
    public static char[] shuffle(final char[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }
        return array;
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array The array to shuffle.
     * @return The shuffled array.
     */
    public static short[] shuffle(final short[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * Shuffles the elements of an array in-place.
     * 
     * @param array  The array to shuffle.
     * @param random The random number generator.
     * @return The shuffled array.
     */
    public static short[] shuffle(final short[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }
        return array;
    }

    /**
     * Swaps two elements in an array at the specified indices.
     *
     * @param array  The array.
     * @param index1 The index of the first element.
     * @param index2 The index of the second element.
     * @return The modified array.
     */
    public static int[] swap(final int[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Array must not be empty!");
        }
        final int tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * Swaps two elements in an array.
     * 
     * @param array  The array.
     * @param index1 The first index.
     * @param index2 The second index.
     * @return The modified array.
     */
    public static long[] swap(final long[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Array must not be empty!");
        }
        final long tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * Swaps two elements in an array.
     * 
     * @param array  The array.
     * @param index1 The first index.
     * @param index2 The second index.
     * @return The modified array.
     */
    public static double[] swap(final double[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Array must not be empty!");
        }
        final double tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * Swaps two elements in an array.
     * 
     * @param array  The array.
     * @param index1 The first index.
     * @param index2 The second index.
     * @return The modified array.
     */
    public static float[] swap(final float[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Array must not be empty!");
        }
        final float tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * Swaps two elements in an array.
     * 
     * @param array  The array.
     * @param index1 The first index.
     * @param index2 The second index.
     * @return The modified array.
     */
    public static boolean[] swap(final boolean[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Array must not be empty!");
        }
        final boolean tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * Swaps two elements in an array.
     * 
     * @param array  The array.
     * @param index1 The first index.
     * @param index2 The second index.
     * @return The modified array.
     */
    public static byte[] swap(final byte[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Array must not be empty!");
        }
        final byte tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * Swaps two elements in an array.
     * 
     * @param array  The array.
     * @param index1 The first index.
     * @param index2 The second index.
     * @return The modified array.
     */
    public static char[] swap(final char[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Array must not be empty!");
        }
        final char tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * Swaps two elements in an array.
     * 
     * @param array  The array.
     * @param index1 The first index.
     * @param index2 The second index.
     * @return The modified array.
     */
    public static short[] swap(final short[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Array must not be empty!");
        }
        final short tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * Checks if an array starts with the given prefix array.
     *
     * @param array  The array to check.
     * @param prefix The prefix to look for.
     * @return {@code true} if the array starts with the prefix.
     */
    public static boolean startWith(final boolean[] array, final boolean... prefix) {
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
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if an array starts with the given prefix array.
     *
     * @param array  The array to check.
     * @param prefix The prefix to look for.
     * @return {@code true} if the array starts with the prefix.
     */
    public static boolean startWith(final byte[] array, final byte... prefix) {
        if (array == prefix) {
            return true;
        }
        if (isEmpty(array)) {
            return isEmpty(prefix);
        }
        if (prefix.length > array.length) {
            return false;
        }
        return isSubEquals(array, 0, prefix);
    }

    /**
     * Checks if an array starts with the given prefix array.
     *
     * @param array  The array to check.
     * @param prefix The prefix to look for.
     * @return {@code true} if the array starts with the prefix.
     */
    public static boolean startWith(final char[] array, final char... prefix) {
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
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if an array starts with the given prefix array.
     *
     * @param array  The array to check.
     * @param prefix The prefix to look for.
     * @return {@code true} if the array starts with the prefix.
     */
    public static boolean startWith(final double[] array, final double... prefix) {
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
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if an array starts with the given prefix array.
     *
     * @param array  The array to check.
     * @param prefix The prefix to look for.
     * @return {@code true} if the array starts with the prefix.
     */
    public static boolean startWith(final float[] array, final float... prefix) {
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
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if an array starts with the given prefix array.
     *
     * @param array  The array to check.
     * @param prefix The prefix to look for.
     * @return {@code true} if the array starts with the prefix.
     */
    public static boolean startWith(final int[] array, final int... prefix) {
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
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if an array starts with the given prefix array.
     *
     * @param array  The array to check.
     * @param prefix The prefix to look for.
     * @return {@code true} if the array starts with the prefix.
     */
    public static boolean startWith(final long[] array, final long... prefix) {
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
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if an array starts with the given prefix array.
     *
     * @param array  The array to check.
     * @param prefix The prefix to look for.
     * @return {@code true} if the array starts with the prefix.
     */
    public static boolean startWith(final short[] array, final short... prefix) {
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
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

}
