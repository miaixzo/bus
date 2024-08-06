/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * Array检查工具类，提供字对象的blank和empty等检查
 * <ul>
 * <li>empty定义：{@code null} or 空字对象：{@code ""}</li>
 * <li>blank定义：{@code null} or 空字对象：{@code ""} or 空格、全角空格、制表符、换行符，等不可见字符</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ArrayValidator {

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final long[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final int[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final short[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final char[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final byte[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final double[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final float[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final boolean[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 数组是否为非空
     *
     * @param array 数组
     * @return 是否为非空
     */
    public static boolean isNotEmpty(final long[] array) {
        return !isEmpty(array);
    }

    /**
     * 数组是否为非空
     *
     * @param array 数组
     * @return 是否为非空
     */
    public static boolean isNotEmpty(final int[] array) {
        return !isEmpty(array);
    }

    /**
     * 数组是否为非空
     *
     * @param array 数组
     * @return 是否为非空
     */
    public static boolean isNotEmpty(final short[] array) {
        return !isEmpty(array);
    }

    /**
     * 数组是否为非空
     *
     * @param array 数组
     * @return 是否为非空
     */
    public static boolean isNotEmpty(final char[] array) {
        return !isEmpty(array);
    }

    /**
     * 数组是否为非空
     *
     * @param array 数组
     * @return 是否为非空
     */
    public static boolean isNotEmpty(final byte[] array) {
        return !isEmpty(array);
    }

    /**
     * 数组是否为非空
     *
     * @param array 数组
     * @return 是否为非空
     */
    public static boolean isNotEmpty(final double[] array) {
        return !isEmpty(array);
    }

    /**
     * 数组是否为非空
     *
     * @param array 数组
     * @return 是否为非空
     */
    public static boolean isNotEmpty(final float[] array) {
        return !isEmpty(array);
    }

    /**
     * 数组是否为非空
     *
     * @param array 数组
     * @return 是否为非空
     */
    public static boolean isNotEmpty(final boolean[] array) {
        return !isEmpty(array);
    }

    /**
     * 对象是否为数组对象
     *
     * @param obj 对象
     * @return 是否为数组对象，如果为{@code null} 返回false
     */
    public static boolean isArray(final Object obj) {
        return null != obj && obj.getClass().isArray();
    }

    /**
     * <p>
     * 指定字符串数组中，是否包含空字符串。
     * </p>
     * <p>
     * 如果指定的字符串数组的长度为 0，或者其中的任意一个元素是空字符串，则返回 true。
     * </p>
     * <ul>
     * <li>{@code hasBlank()                  // true}</li>
     * <li>{@code hasBlank("", null, " ")     // true}</li>
     * <li>{@code hasBlank("123", " ")        // true}</li>
     * <li>{@code hasBlank("123", "abc")      // false}</li>
     * </ul>
     *
     * <p>
     * 注意：该方法与 {@link #isAllBlank(CharSequence...)} 的区别在于：
     * </p>
     * <ul>
     * <li>hasBlank(CharSequence...) 等价于 {@code isBlank(...) || isBlank(...) || ...}</li>
     * <li>{@link #isAllBlank(CharSequence...)} 等价于 {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param args 字符串列表
     * @return 是否包含空字符串
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
     * 是否存都不为{@code null}或空对象或空白符的对象，通过{@link #hasBlank(CharSequence...)} 判断元素
     *
     * @param args 被检查的对象,一个或者多个
     * @return 是否都不为空
     */
    public static boolean isAllNotBlank(final CharSequence... args) {
        return !hasBlank(args);
    }

    /**
     * <p>
     * 指定字符串数组中的元素，是否全部为空字符串。
     * </p>
     * <p>
     * 如果指定的字符串数组的长度为 0，或者所有元素都是空字符串，则返回 true。
     * </p>
     * <ul>
     * <li>{@code isAllBlank()                  // true}</li>
     * <li>{@code isAllBlank("", null, " ")     // true}</li>
     * <li>{@code isAllBlank("123", " ")        // false}</li>
     * <li>{@code isAllBlank("123", "abc")      // false}</li>
     * </ul>
     *
     * <p>
     * 注意：该方法与 {@link #hasBlank(CharSequence...)} 的区别在于：
     * </p>
     * <ul>
     * <li>{@link #hasBlank(CharSequence...)} 等价于 {@code isBlank(...) || isBlank(...) || ...}</li>
     * <li>isAllBlank(CharSequence...) 等价于 {@code isBlank(...) && isBlank(...) && ...}</li>
     * </ul>
     *
     * @param args 字符串列表
     * @return 所有字符串是否为空白
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
     * 数组是否为空
     *
     * @param <T>   数组元素类型
     * @param array 数组
     * @return 是否为空
     */
    public static <T> boolean isEmpty(final T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 如果给定数组为空，返回默认数组
     *
     * @param <T>          数组元素类型
     * @param array        数组
     * @param defaultArray 默认数组
     * @return 非空（empty）的原数组或默认数组
     */
    public static <T> T[] defaultIfEmpty(final T[] array, final T[] defaultArray) {
        return isEmpty(array) ? defaultArray : array;
    }

    /**
     * 数组是否为空 此方法会匹配单一对象，如果此对象为{@code null}则返回true 如果此对象为非数组，理解为此对象为数组的第一个元素，则返回false
     * 如果此对象为数组对象，数组长度大于0的情况下返回false，否则返回true
     *
     * @param array 数组
     * @return 是否为空
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
     * 数组是否为非空
     *
     * @param <T>   数组元素类型
     * @param array 数组
     * @return 是否为非空
     */
    public static <T> boolean isNotEmpty(final T[] array) {
        return !isEmpty(array);
    }

    /**
     * 数组是否为非空 此方法会匹配单一对象，如果此对象为{@code null}则返回false 如果此对象为非数组，理解为此对象为数组的第一个元素，则返回true
     * 如果此对象为数组对象，数组长度大于0的情况下返回true，否则返回false
     *
     * @param array 数组
     * @return 是否为非空
     */
    public static boolean isNotEmpty(final Object array) {
        return !isEmpty(array);
    }

    /**
     * 检查数组是否有序，升序或者降序，使用指定比较器比较
     * <p>
     * 若传入空数组或空比较器，则返回{@code false}；元素全部相等，返回 {@code true}
     * </p>
     *
     * @param <T>        数组元素类型
     * @param array      数组
     * @param comparator 比较器，需要自己处理null值比较
     * @return 数组是否有序
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
     * 检查数组是否有序，升序或者降序
     * <p>
     * 若传入空数组，则返回{@code false}；元素全部相等，返回 {@code true}
     * </p>
     *
     * @param <T>   数组元素类型，该类型需要实现Comparable接口
     * @param array 数组
     * @return 数组是否有序
     * @throws NullPointerException 如果数组元素含有null值
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
     * 检查数组是否升序，即{@code array[i] <= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否升序
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
     * 检查数组是否降序，即{@code array[i] >= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否降序
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
     * 检查数组是否升序，即{@code array[i] <= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否升序
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
     * 检查数组是否降序，即{@code array[i] >= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否降序
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
     * 检查数组是否升序，即{@code array[i] <= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否升序
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
     * 检查数组是否降序，即{@code array[i] >= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否降序
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
     * 检查数组是否升序，即{@code array[i] <= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否升序
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
     * 检查数组是否降序，即{@code array[i] >= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否降序
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
     * 检查数组是否升序，即{@code array[i] <= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否升序
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
     * 检查数组是否降序，即{@code array[i] >= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否降序
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
     * 检查数组是否升序，即{@code array[i] <= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否升序
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
     * 检查数组是否降序，即{@code array[i] >= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否降序
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
     * 检查数组是否升序，即{@code array[i] <= array[i+1]} 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否升序
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
     * 检查数组是否降序，即array[i] &gt;= array[i+1] 若传入空数组，则返回{@code false}
     *
     * @param array 数组
     * @return 数组是否降序
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
     * 检查数组是否升序，即 {@code array[i].compareTo(array[i + 1]) <= 0}
     * <p>
     * 若传入空数组，则返回{@code false}
     * </p>
     *
     * @param <T>   数组元素类型，该类型需要实现Comparable接口
     * @param array 数组
     * @return 数组是否升序
     * @throws NullPointerException 如果数组元素含有null值
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
     * 检查数组是否降序，即 {@code array[i].compareTo(array[i + 1]) >= 0}
     * <p>
     * 若传入空数组，则返回{@code false}
     * </p>
     *
     * @param <T>   数组元素类型，该类型需要实现Comparable接口
     * @param array 数组
     * @return 数组是否降序
     * @throws NullPointerException 如果数组元素含有null值
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
     * 检查数组是否升序，使用指定的比较器比较，即 {@code compare.compare(array[i], array[i + 1]) <= 0}
     * <p>
     * 若传入空数组或空比较器，则返回{@code false}
     * </p>
     *
     * @param <T>        数组元素类型
     * @param array      数组
     * @param comparator 比较器，需要自己处理null值比较
     * @return 数组是否升序
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
     * 检查数组是否降序，使用指定的比较器比较，即 {@code compare.compare(array[i], array[i + 1]) >= 0}
     * <p>
     * 若传入空数组或空比较器，则返回{@code false}
     * </p>
     *
     * @param <T>        数组元素类型
     * @param array      数组
     * @param comparator 比较器，需要自己处理null值比较
     * @return 数组是否降序
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
     * 是否所有元素都为{@code null}或空对象，通过{@link ObjectKit#isEmpty(Object)} 判断元素 如果提供的数组本身为空，则返回{@code true}
     *
     * @param <T>  元素类型
     * @param args 被检查的对象,一个或者多个
     * @return 是否都为空
     */
    public static <T> boolean isAllEmpty(final T[] args) {
        for (final T obj : args) {
            if (!ObjectKit.isEmpty(obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否局部匹配，相当于对比以下子串是否相等
     * 
     * <pre>
     *     array1[offset1, subArray.length]
     *                  ||
     *               subArray
     * </pre>
     *
     * @param array    数组
     * @param offset   开始位置
     * @param subArray 子数组
     * @return 是否局部匹配
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
     * 是否是数组的子数组
     *
     * @param array    数组
     * @param subArray 子数组
     * @param <T>      数组元素类型
     * @return 是否是数组的子数组
     */
    public static <T> boolean isSub(final T[] array, final T[] subArray) {
        return indexOfSub(array, subArray) > Normal.__1;
    }

    /**
     * 查找子数组的位置
     *
     * @param array        数组
     * @param beginInclude 查找开始的位置（包含）
     * @param subArray     子数组
     * @param <T>          数组元素类型
     * @return 子数组的开始位置，即子数字第一个元素在数组中的位置
     */
    public static <T> int indexOfSub(final T[] array, final int beginInclude, final T[] subArray) {
        if (isEmpty(array) || isEmpty(subArray) || subArray.length > array.length) {
            return Normal.__1;
        }
        final int firstIndex = indexOf(array, subArray[0], beginInclude);
        if (firstIndex < 0 || firstIndex + subArray.length > array.length) {
            return Normal.__1;
        }

        for (int i = 0; i < subArray.length; i++) {
            if (!ObjectKit.equals(array[i + firstIndex], subArray[i])) {
                return indexOfSub(array, firstIndex + 1, subArray);
            }
        }

        return firstIndex;
    }

    /**
     * 是否所有元素都为{@code null}或空对象，通过{@link ObjectKit#isEmpty(Object)} 判断元素
     * <p>
     * 如果提供的数组本身为空，则返回{@code true}
     * </p>
     * <p>
     * <strong>限制条件：args的每个item不能是数组、不能是集合</strong>
     * </p>
     *
     * @param <T>  元素类型
     * @param args 被检查的对象,一个或者多个
     * @return 是否都为空
     * @throws IllegalArgumentException 如果提供的args的item存在数组或集合，抛出异常
     */
    @SafeVarargs
    public static <T> boolean isAllEmptyVarargs(final T... args) {
        return isAllEmpty(args);
    }

    /**
     * 是否所有元素都不为{@code null}或空对象，通过{@link ObjectKit#isEmpty(Object)} 判断元素
     * <p>
     * 如果提供的数组本身为空，则返回{@code true}
     * </p>
     *
     * @param args 被检查的对象,一个或者多个
     * @return 是否都不为空
     */
    public static boolean isAllNotEmpty(final Object... args) {
        return !hasEmpty(args);
    }

    /**
     * 是否包含{@code null}元素
     * <p>
     * 如果数组为null，则返回{@code true}，如果数组为空，则返回{@code false}
     * </p>
     *
     * @param <T>   数组元素类型
     * @param array 被检查的数组
     * @return 是否包含 {@code null} 元素
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
     * 是否存在{@code null}或空对象，通过{@link ObjectKit#isEmpty(Object)} 判断元素
     * <p>
     * 如果提供的数组本身为空，则返回{@code false}
     * </p>
     *
     * @param <T>  元素类型
     * @param args 被检查对象
     * @return 是否存在 {@code null} 或空对象
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
     * 是否存在{@code null}或空对象，通过{@link ObjectKit#isEmpty(Object)} 判断元素
     * <p>
     * 如果提供的数组本身为空，则返回{@code false}
     * </p>
     * <p>
     * <strong>限制条件：args的每个item不能是数组、不能是集合</strong>
     * </p>
     *
     * @param <T>  元素类型
     * @param args 被检查对象
     * @return 是否存在 {@code null} 或空对象
     * @throws IllegalArgumentException 如果提供的args的item存在数组或集合，抛出异常
     */
    @SafeVarargs
    public static <T> boolean hasEmptyVarargs(final T... args) {
        return hasEmpty(args);
    }

    /**
     * 所有字段是否全为null
     * <p>
     * 如果数组为{@code null}或者空，则返回 {@code true}
     * </p>
     *
     * @param <T>   数组元素类型
     * @param array 被检查的数组
     * @return 所有字段是否全为null
     */
    public static <T> boolean isAllNull(final T... array) {
        return null == firstNonNull(array);
    }

    /**
     * 是否所有元素都不为 {@code null}
     * <p>
     * 如果提供的数组为null，则返回{@code false}，如果提供的数组为空，则返回{@code true}
     * </p>
     *
     * @param <T>   数组元素类型
     * @param array 被检查的数组
     * @return 是否所有元素都不为 {@code null}
     */
    public static <T> boolean isAllNotNull(final T... array) {
        return !hasNull(array);
    }

    /**
     * 是否包含非{@code null}元素
     * <p>
     * 如果数组是{@code null}或者空，返回{@code false}，否则当数组中有非{@code null}元素时返回{@code true}
     * </p>
     *
     * @param <T>   数组元素类型
     * @param array 被检查的数组
     * @return 是否包含非 {@code null} 元素
     */
    public static <T> boolean hasNonNull(final T... array) {
        return null != firstNonNull(array);
    }

    /**
     * 计算{@code null}或空元素对象的个数，通过{@link ObjectKit#isEmpty(Object)} 判断元素
     *
     * @param args 被检查的对象,一个或者多个
     * @return {@code null}或空元素对象的个数
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
     * 返回数组中第一个非空元素
     *
     * @param <T>   数组元素类型
     * @param array 数组
     * @return 第一个非空元素，如果 不存在非空元素 或 数组为空，返回{@code null}
     */
    public static <T> T firstNonNull(final T... array) {
        if (isEmpty(array)) {
            return null;
        }
        return firstMatch(ObjectKit::isNotNull, array);
    }

    /**
     * 返回数组中第一个匹配规则的值
     *
     * @param <T>     数组元素类型
     * @param matcher 匹配接口，实现此接口自定义匹配规则
     * @param array   数组
     * @return 第一个匹配元素，如果 不存在匹配元素 或 数组为空，返回 {@code null}
     */
    public static <T> T firstMatch(final Predicate<T> matcher, final T... array) {
        final int index = matchIndex(matcher, array);
        if (index == Normal.__1) {
            return null;
        }

        return array[index];
    }

    /**
     * 返回数组中第一个匹配规则的值的位置
     *
     * @param <T>     数组元素类型
     * @param matcher 匹配接口，实现此接口自定义匹配规则
     * @param array   数组
     * @return 第一个匹配元素的位置，{@link Normal#__1}表示未匹配到
     */
    public static <T> int matchIndex(final Predicate<T> matcher, final T... array) {
        return matchIndex(0, matcher, array);
    }

    /**
     * 返回数组中第一个匹配规则的值的位置
     *
     * @param <E>               数组元素类型
     * @param matcher           匹配接口，实现此接口自定义匹配规则
     * @param beginIndexInclude 检索开始的位置，不能为负数
     * @param array             数组
     * @return 第一个匹配元素的位置，{@link Normal#__1}表示未匹配到
     */
    public static <E> int matchIndex(final int beginIndexInclude, final Predicate<E> matcher, final E... array) {
        if (isEmpty(array)) {
            return Normal.__1;
        }
        final ArrayWrapper<E[], E> arrayWrapper = ArrayWrapper.of(array);
        return arrayWrapper.matchIndex(beginIndexInclude, matcher);
    }

    /**
     * 返回数组中指定元素所在位置，未找到返回{@link Normal#__1}
     *
     * @param <T>               数组类型
     * @param array             数组
     * @param value             被检查的元素
     * @param beginIndexInclude 检索开始的位置
     * @return 数组中指定元素所在位置，未找到返回{@link Normal#__1}
     */
    public static <T> int indexOf(final T[] array, final Object value, final int beginIndexInclude) {
        return ArrayWrapper.of(array).indexOf(value, beginIndexInclude);
    }

    /**
     * 返回数组中指定元素所在位置，未找到返回{@link Normal#__1}
     *
     * @param <T>   数组类型
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置，未找到返回{@link Normal#__1}
     */
    public static <T> int indexOf(final T[] array, final Object value) {
        return ArrayWrapper.of(array).indexOf(value);
    }

    /**
     * 返回数组中指定元素所在位置，忽略大小写，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置，未找到返回{@link Normal#__1}
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
     * 返回数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     *
     * @param <T>   数组类型
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     */
    public static <T> int lastIndexOf(final T[] array, final Object value) {
        if (isEmpty(array)) {
            return Normal.__1;
        }
        return lastIndexOf(array, value, array.length - 1);
    }

    /**
     * 返回数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     *
     * @param <T>        数组类型
     * @param array      数组
     * @param value      被检查的元素
     * @param endInclude 从后向前查找时的起始位置，一般为{@code array.length - 1}
     * @return 数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
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
     * 查找子数组的位置
     *
     * @param array    数组
     * @param subArray 子数组
     * @param <T>      数组元素类型
     * @return 子数组的开始位置，即子数字第一个元素在数组中的位置
     */
    public static <T> int indexOfSub(final T[] array, final T[] subArray) {
        return indexOfSub(array, 0, subArray);
    }

    /**
     * 是否局部匹配，相当于对比以下子串是否相等
     * 
     * <pre>
     *     array1[offset1 : offset1 + length]
     *                  ||
     *     array2[offset2 : offset2 + length]
     * </pre>
     *
     * @param array1  第一个数组
     * @param offset1 第一个数组开始位置
     * @param array2  第二个数组
     * @param offset2 第二个数组开始位置
     * @param length  检查长度
     * @return 是否局部匹配
     */
    public static boolean regionMatches(final byte[] array1, final int offset1, final byte[] array2, final int offset2,
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
     * 数组或集合转String
     *
     * @param obj 集合或数组对象
     * @return 数组字符串，与集合转字符串格式相同
     */
    public static String toString(final Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (obj instanceof long[]) {
            return Arrays.toString((long[]) obj);
        } else if (obj instanceof int[]) {
            return Arrays.toString((int[]) obj);
        } else if (obj instanceof short[]) {
            return Arrays.toString((short[]) obj);
        } else if (obj instanceof char[]) {
            return Arrays.toString((char[]) obj);
        } else if (obj instanceof byte[]) {
            return Arrays.toString((byte[]) obj);
        } else if (obj instanceof boolean[]) {
            return Arrays.toString((boolean[]) obj);
        } else if (obj instanceof float[]) {
            return Arrays.toString((float[]) obj);
        } else if (obj instanceof double[]) {
            return Arrays.toString((double[]) obj);
        } else if (ArrayKit.isArray(obj)) {
            // 对象数组
            try {
                return Arrays.deepToString((Object[]) obj);
            } catch (final Exception ignore) {
                // ignore
            }
        }

        return obj.toString();
    }

    /**
     * 获取数组长度 如果参数为{@code null}，返回0
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
     * @param array 数组对象
     * @return 数组长度
     * @throws IllegalArgumentException 如果参数不为数组，抛出此异常
     * @see Array#getLength(Object)
     */
    public static int length(final Object array) throws IllegalArgumentException {
        if (null == array) {
            return 0;
        }
        return Array.getLength(array);
    }

}
