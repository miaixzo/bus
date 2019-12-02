/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.utils;


import org.aoju.bus.core.builder.HashCodeBuilder;
import org.aoju.bus.core.builder.ToStringBuilder;
import org.aoju.bus.core.builder.ToStringStyle;
import org.aoju.bus.core.lang.Editor;
import org.aoju.bus.core.lang.Filter;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.lang.mutable.MutableInt;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * 数组工具类
 *
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
public class ArrayUtils {

    /**
     * {@code Object} array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    /**
     * {@code Class} array.
     */
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    /**
     * {@code String} array.
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    /**
     * {@code long} array.
     */
    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    /**
     * {@code Long} array.
     */
    public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];
    /**
     * {@code int} array.
     */
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    /**
     * {@code Integer} array.
     */
    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];
    /**
     * {@code short} array.
     */
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];
    /**
     * {@code Short} array.
     */
    public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];
    /**
     * {@code byte} array.
     */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    /**
     * {@code Byte} array.
     */
    public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];
    /**
     * {@code double} array.
     */
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    /**
     * {@code Double} array.
     */
    public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];
    /**
     * {@code float} array.
     */
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    /**
     * {@code Float} array.
     */
    public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];
    /**
     * {@code boolean} array.
     */
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    /**
     * {@code Boolean} array.
     */
    public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];
    /**
     * {@code char} array.
     */
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    /**
     * {@code Character} array.
     */
    public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];

    /**
     * 列表或数组中没有找到元素时的索引值 : {@code -1}.
     * 此值由该类中的方法返回,也可用于与所返回的值进行比较{@link java.util.List}.
     */
    public static final int INDEX_NOT_FOUND = -1;

    /**
     * 数组或集合转String
     *
     * @param array 集合或数组对象
     * @return 数组字符串, 与集合转字符串格式相同
     */
    public static String toString(final Object array) {
        return toString(array, "{}");
    }

    /**
     * 数组或集合转String
     *
     * @param array        集合或数组对象
     * @param stringIfNull 是否null
     * @return 数组字符串, 与集合转字符串格式相同
     */
    public static String toString(final Object array, final String stringIfNull) {
        if (array == null) {
            return stringIfNull;
        }
        return new ToStringBuilder(array, ToStringStyle.SIMPLE_STYLE).append(array).toString();
    }

    /**
     * 获得一个数组哈希码,用于正确处理多维数组
     * 多维基元数组也可以用该方法正确处理
     *
     * @param array 数组
     * @return 返回数组的哈希码
     */
    public static int hashCode(final Object array) {
        return new HashCodeBuilder().append(array).toHashCode();
    }

    /**
     * 将给定数组转换为{@link Map}
     * 数组的每个元素必须是{@link Map} 或数组,其中至少包含两个
     * 元素,其中第一个元素用作键,第二个元素用作值
     * <pre>
     * Map colorMap = ArrayUtils.toMap(new String[][] {
     *     {"RED", "#FF0000"},
     *     {"GREEN", "#00FF00"},
     *     {"BLUE", "#0000FF"}});
     * </pre>
     *
     * @param array 数组
     * @return {@link Map}
     */
    public static Map<Object, Object> toMap(final Object[] array) {
        if (array == null) {
            return null;
        }
        final Map<Object, Object> map = new HashMap<>((int) (array.length * 1.5));
        for (int i = 0; i < array.length; i++) {
            final Object object = array[i];
            if (object instanceof Map.Entry<?, ?>) {
                final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
                map.put(entry.getKey(), entry.getValue());
            } else if (object instanceof Object[]) {
                final Object[] entry = (Object[]) object;
                if (entry.length < 2) {
                    throw new IllegalArgumentException("Array element " + i + ", '"
                            + object
                            + "', has a length less than 2");
                }
                map.put(entry[0], entry[1]);
            } else {
                throw new IllegalArgumentException("Array element " + i + ", '"
                        + object
                        + "', is neither of type Map.Entry nor an Array");
            }
        }
        return map;
    }

    /**
     * 该方法通常用于调用者本身使用泛型类型的场景
     * 必须组合成数组
     * <p>
     * 注意,此方法只适用于提供相同类型的参数,以便
     * 编译器可以推断数组本身的类型 虽然可以选择
     * 显式输入like in
     * <code>Number[] array = ArrayUtils.toArray(Integer.valueOf(42),Double.valueOf(Math.PI))</code>
     * 与……相比没有什么真正的优势
     * <code>new Number[]{整数.valueof(42),Double.valueOf(Math.PI)}</code>
     *
     * @param <T>   对象
     * @param items 数组
     * @return 返回数组, 除非传入一个空数组, 否则不为空
     */
    public static <T> T[] toArray(final T... items) {
        return items;
    }

    /**
     * 克隆数组
     *
     * @param <T>   对象
     * @param array 被克隆的数组
     * @return 新数组
     */
    public static <T> T[] clone(final T[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * 克隆数组,如果非数组返回null
     *
     * @param <T> 数组元素类型
     * @param obj 数组对象
     * @return 克隆后的数组对象
     */
    public static <T> T clone(T obj) {
        if (null == obj) {
            return null;
        }
        if (isArray(obj)) {
            final Object result;
            final Class<?> componentType = obj.getClass().getComponentType();
            if (componentType.isPrimitive()) {// 原始类型
                int length = Array.getLength(obj);
                result = Array.newInstance(componentType, length);
                while (length-- > 0) {
                    Array.set(result, length, Array.get(obj, length));
                }
            } else {
                result = ((Object[]) obj).clone();
            }
            return (T) result;
        }
        return null;
    }

    /**
     * 克隆数组
     *
     * @param array 被克隆的数组
     * @return 新数组
     */
    public static long[] clone(final long[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * 克隆数组
     *
     * @param array 被克隆的数组
     * @return 新数组
     */
    public static int[] clone(final int[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * 克隆数组
     *
     * @param array 被克隆的数组
     * @return 新数组
     */
    public static short[] clone(final short[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * 克隆数组
     *
     * @param array 被克隆的数组
     * @return 新数组
     */
    public static char[] clone(final char[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * 克隆数组
     *
     * @param array 被克隆的数组
     * @return 新数组
     */
    public static byte[] clone(final byte[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * 克隆数组
     *
     * @param array 被克隆的数组
     * @return 新数组
     */
    public static double[] clone(final double[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * 克隆数组
     *
     * @param array 被克隆的数组
     * @return 新数组
     */
    public static float[] clone(final float[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * 克隆数组
     *
     * @param array 被克隆的数组
     * @return 新数组
     */
    public static boolean[] clone(final boolean[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }


    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array the array to check for {@code null} or empty
     * @param type  the class representation of the desired array
     * @param <T>   the class type
     * @return the same array, {@code public static} empty array if {@code null}
     * @throws IllegalArgumentException if the type argument is null
     * @since 3.5.0
     */
    public static <T> T[] nullToEmpty(final T[] array, final Class<T[]> type) {
        if (type == null) {
            throw new IllegalArgumentException("The type must not be null");
        }

        if (array == null) {
            return type.cast(Array.newInstance(type.getComponentType(), 0));
        }
        return array;
    }


    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static Object[] nullToEmpty(final Object[] array) {
        if (isEmpty(array)) {
            return EMPTY_OBJECT_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static Class<?>[] nullToEmpty(final Class<?>[] array) {
        if (isEmpty(array)) {
            return EMPTY_CLASS_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static String[] nullToEmpty(final String[] array) {
        if (isEmpty(array)) {
            return EMPTY_STRING_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static long[] nullToEmpty(final long[] array) {
        if (isEmpty(array)) {
            return EMPTY_LONG_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static int[] nullToEmpty(final int[] array) {
        if (isEmpty(array)) {
            return EMPTY_INT_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static short[] nullToEmpty(final short[] array) {
        if (isEmpty(array)) {
            return EMPTY_SHORT_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static char[] nullToEmpty(final char[] array) {
        if (isEmpty(array)) {
            return EMPTY_CHAR_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static byte[] nullToEmpty(final byte[] array) {
        if (isEmpty(array)) {
            return EMPTY_BYTE_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static double[] nullToEmpty(final double[] array) {
        if (isEmpty(array)) {
            return EMPTY_DOUBLE_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static float[] nullToEmpty(final float[] array) {
        if (isEmpty(array)) {
            return EMPTY_FLOAT_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static boolean[] nullToEmpty(final boolean[] array) {
        if (isEmpty(array)) {
            return EMPTY_BOOLEAN_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static Long[] nullToEmpty(final Long[] array) {
        if (isEmpty(array)) {
            return EMPTY_LONG_OBJECT_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static Integer[] nullToEmpty(final Integer[] array) {
        if (isEmpty(array)) {
            return EMPTY_INTEGER_OBJECT_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static Short[] nullToEmpty(final Short[] array) {
        if (isEmpty(array)) {
            return EMPTY_SHORT_OBJECT_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static Character[] nullToEmpty(final Character[] array) {
        if (isEmpty(array)) {
            return EMPTY_CHARACTER_OBJECT_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static Byte[] nullToEmpty(final Byte[] array) {
        if (isEmpty(array)) {
            return EMPTY_BYTE_OBJECT_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static Double[] nullToEmpty(final Double[] array) {
        if (isEmpty(array)) {
            return EMPTY_DOUBLE_OBJECT_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static Float[] nullToEmpty(final Float[] array) {
        if (isEmpty(array)) {
            return EMPTY_FLOAT_OBJECT_ARRAY;
        }
        return array;
    }

    /**
     * 输入数组返回一个空数组或原始数组
     *
     * @param array 数组
     * @return 空数组或原始数组
     */
    public static Boolean[] nullToEmpty(final Boolean[] array) {
        if (isEmpty(array)) {
            return EMPTY_BOOLEAN_OBJECT_ARRAY;
        }
        return array;
    }

    /**
     * 生成一个新的数组,其中包含元素开始索引取值至结束位置索引
     * 开始索引包含,结束索引不包含,空数组输入产生空输出
     *
     * @param <T>        对象
     * @param array      对象数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @return 新数组
     */
    public static <T> T[] subarray(final T[] array, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex > array.length) {
            endIndex = array.length;
        }
        final int newSize = endIndex - startIndex;
        final Class<?> type = array.getClass().getComponentType();
        if (newSize <= 0) {
            final T[] emptyArray = (T[]) Array.newInstance(type, 0);
            return emptyArray;
        }
        final T[] subarray = (T[]) Array.newInstance(type, newSize);
        System.arraycopy(array, startIndex, subarray, 0, newSize);
        return subarray;
    }

    /**
     * 生成一个新的数组,其中包含元素开始索引取值至结束位置索引
     * 开始索引包含,结束索引不包含,空数组输入产生空输出
     *
     * @param array      对象数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @return 新数组
     */
    public static long[] subarray(final long[] array, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex > array.length) {
            endIndex = array.length;
        }
        final int newSize = endIndex - startIndex;
        if (newSize <= 0) {
            return EMPTY_LONG_ARRAY;
        }

        final long[] subarray = new long[newSize];
        System.arraycopy(array, startIndex, subarray, 0, newSize);
        return subarray;
    }

    /**
     * 生成一个新的数组,其中包含元素开始索引取值至结束位置索引
     * 开始索引包含,结束索引不包含,空数组输入产生空输出
     *
     * @param array      对象数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @return 新数组
     */
    public static int[] subarray(final int[] array, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex > array.length) {
            endIndex = array.length;
        }
        final int newSize = endIndex - startIndex;
        if (newSize <= 0) {
            return EMPTY_INT_ARRAY;
        }

        final int[] subarray = new int[newSize];
        System.arraycopy(array, startIndex, subarray, 0, newSize);
        return subarray;
    }

    /**
     * 生成一个新的数组,其中包含元素开始索引取值至结束位置索引
     * 开始索引包含,结束索引不包含,空数组输入产生空输出
     *
     * @param array      对象数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @return 新数组
     */
    public static short[] subarray(final short[] array, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex > array.length) {
            endIndex = array.length;
        }
        final int newSize = endIndex - startIndex;
        if (newSize <= 0) {
            return EMPTY_SHORT_ARRAY;
        }

        final short[] subarray = new short[newSize];
        System.arraycopy(array, startIndex, subarray, 0, newSize);
        return subarray;
    }

    /**
     * 生成一个新的数组,其中包含元素开始索引取值至结束位置索引
     * 开始索引包含,结束索引不包含,空数组输入产生空输出
     *
     * @param array      对象数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @return 新数组
     */
    public static char[] subarray(final char[] array, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex > array.length) {
            endIndex = array.length;
        }
        final int newSize = endIndex - startIndex;
        if (newSize <= 0) {
            return EMPTY_CHAR_ARRAY;
        }

        final char[] subarray = new char[newSize];
        System.arraycopy(array, startIndex, subarray, 0, newSize);
        return subarray;
    }

    /**
     * 生成一个新的数组,其中包含元素开始索引取值至结束位置索引
     * 开始索引包含,结束索引不包含,空数组输入产生空输出
     *
     * @param array      对象数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @return 新数组
     */
    public static byte[] subarray(final byte[] array, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex > array.length) {
            endIndex = array.length;
        }
        final int newSize = endIndex - startIndex;
        if (newSize <= 0) {
            return EMPTY_BYTE_ARRAY;
        }

        final byte[] subarray = new byte[newSize];
        System.arraycopy(array, startIndex, subarray, 0, newSize);
        return subarray;
    }

    /**
     * 生成一个新的数组,其中包含元素开始索引取值至结束位置索引
     * 开始索引包含,结束索引不包含,空数组输入产生空输出
     *
     * @param array      对象数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @return 新数组
     */
    public static double[] subarray(final double[] array, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex > array.length) {
            endIndex = array.length;
        }
        final int newSize = endIndex - startIndex;
        if (newSize <= 0) {
            return EMPTY_DOUBLE_ARRAY;
        }

        final double[] subarray = new double[newSize];
        System.arraycopy(array, startIndex, subarray, 0, newSize);
        return subarray;
    }

    /**
     * 生成一个新的数组,其中包含元素开始索引取值至结束位置索引
     * 开始索引包含,结束索引不包含,空数组输入产生空输出
     *
     * @param array      对象数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @return 新数组
     */
    public static float[] subarray(final float[] array, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex > array.length) {
            endIndex = array.length;
        }
        final int newSize = endIndex - startIndex;
        if (newSize <= 0) {
            return EMPTY_FLOAT_ARRAY;
        }

        final float[] subarray = new float[newSize];
        System.arraycopy(array, startIndex, subarray, 0, newSize);
        return subarray;
    }

    /**
     * 生成一个新的数组,其中包含元素开始索引取值至结束位置索引
     * 开始索引包含,结束索引不包含,空数组输入产生空输出
     *
     * @param array      对象数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @return 新数组
     */
    public static boolean[] subarray(final boolean[] array, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex > array.length) {
            endIndex = array.length;
        }
        final int newSize = endIndex - startIndex;
        if (newSize <= 0) {
            return EMPTY_BOOLEAN_ARRAY;
        }

        final boolean[] subarray = new boolean[newSize];
        System.arraycopy(array, startIndex, subarray, 0, newSize);
        return subarray;
    }

    /**
     * 检查两个数组是否相同长度
     *
     * @param array1 参数1
     * @param array2 参数2
     * @return 如果长度匹配返回true否则false
     */
    public static boolean isSameLength(final Object[] array1, final Object[] array2) {
        return getLength(array1) == getLength(array2);
    }

    /**
     * 检查两个数组是否相同长度
     *
     * @param array1 参数1
     * @param array2 参数2
     * @return 如果长度匹配返回true否则false
     */
    public static boolean isSameLength(final long[] array1, final long[] array2) {
        return getLength(array1) == getLength(array2);
    }

    /**
     * 检查两个数组是否相同长度
     *
     * @param array1 参数1
     * @param array2 参数2
     * @return 如果长度匹配返回true否则false
     */
    public static boolean isSameLength(final int[] array1, final int[] array2) {
        return getLength(array1) == getLength(array2);
    }

    /**
     * 检查两个数组是否相同长度
     *
     * @param array1 参数1
     * @param array2 参数2
     * @return 如果长度匹配返回true否则false
     */
    public static boolean isSameLength(final short[] array1, final short[] array2) {
        return getLength(array1) == getLength(array2);
    }

    /**
     * 检查两个数组是否相同长度
     *
     * @param array1 参数1
     * @param array2 参数2
     * @return 如果长度匹配返回true否则false
     */
    public static boolean isSameLength(final char[] array1, final char[] array2) {
        return getLength(array1) == getLength(array2);
    }

    /**
     * 检查两个数组是否相同长度
     *
     * @param array1 参数1
     * @param array2 参数2
     * @return 如果长度匹配返回true否则false
     */
    public static boolean isSameLength(final byte[] array1, final byte[] array2) {
        return getLength(array1) == getLength(array2);
    }

    /**
     * 检查两个数组是否相同长度
     *
     * @param array1 参数1
     * @param array2 参数2
     * @return 如果长度匹配返回true否则false
     */
    public static boolean isSameLength(final double[] array1, final double[] array2) {
        return getLength(array1) == getLength(array2);
    }

    /**
     * 检查两个数组是否相同长度
     *
     * @param array1 参数1
     * @param array2 参数2
     * @return 如果长度匹配返回true否则false
     */
    public static boolean isSameLength(final float[] array1, final float[] array2) {
        return getLength(array1) == getLength(array2);
    }

    /**
     * 检查两个数组是否相同长度
     *
     * @param array1 参数1
     * @param array2 参数2
     * @return 如果长度匹配返回true否则false
     */
    public static boolean isSameLength(final boolean[] array1, final boolean[] array2) {
        return getLength(array1) == getLength(array2);
    }

    /**
     * 返回指定数组的长度
     *
     * <pre>
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array the array to retrieve the length from, may be null
     * @return The length of the array, or {@code 0} if the array is {@code null}
     * @throws IllegalArgumentException if the object argument is not an array.
     * @since 2.1.0
     */
    public static int getLength(final Object array) {
        if (array == null) {
            return 0;
        }
        return Array.getLength(array);
    }

    /**
     * 检查两个数组是否为同一类型
     *
     * @param array1 the first array, must not be {@code null}
     * @param array2 the second array, must not be {@code null}
     * @return {@code true} if type of arrays matches
     * @throws IllegalArgumentException if either array is {@code null}
     */
    public static boolean isSameType(final Object array1, final Object array2) {
        if (array1 == null || array2 == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        return array1.getClass().getName().equals(array2.getClass().getName());
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array 数组,会变更
     */
    public static void reverse(final Object[] array) {
        if (array == null) {
            return;
        }
        reverse(array, 0, array.length);
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array 数组,会变更
     */
    public static void reverse(final long[] array) {
        if (array == null) {
            return;
        }
        reverse(array, 0, array.length);
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array 数组,会变更
     */
    public static void reverse(final int[] array) {
        if (array == null) {
            return;
        }
        reverse(array, 0, array.length);
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array 数组,会变更
     */
    public static void reverse(final short[] array) {
        if (array == null) {
            return;
        }
        reverse(array, 0, array.length);
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array 数组,会变更
     */
    public static void reverse(final char[] array) {
        if (array == null) {
            return;
        }
        reverse(array, 0, array.length);
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array 数组,会变更
     */
    public static void reverse(final byte[] array) {
        if (array == null) {
            return;
        }
        reverse(array, 0, array.length);
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array 数组,会变更
     */
    public static void reverse(final double[] array) {
        if (array == null) {
            return;
        }
        reverse(array, 0, array.length);
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array 数组,会变更
     */
    public static void reverse(final float[] array) {
        if (array == null) {
            return;
        }
        reverse(array, 0, array.length);
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array 数组,会变更
     */
    public static void reverse(final boolean[] array) {
        if (array == null) {
            return;
        }
        reverse(array, 0, array.length);
    }

    /**
     * @param array      数组,会变更
     * @param startIndex 其实位置（包含）
     * @param endIndex   结束位置（不包含）
     */
    public static void reverse(final boolean[] array, final int startIndex, final int endIndex) {
        if (array == null) {
            return;
        }
        int i = startIndex < 0 ? 0 : startIndex;
        int j = Math.min(array.length, endIndex) - 1;
        boolean tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array      数组,会变更
     * @param startIndex 其实位置（包含）
     * @param endIndex   结束位置（不包含）
     */
    public static void reverse(final byte[] array, final int startIndex, final int endIndex) {
        if (array == null) {
            return;
        }
        int i = startIndex < 0 ? 0 : startIndex;
        int j = Math.min(array.length, endIndex) - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array      数组,会变更
     * @param startIndex 其实位置（包含）
     * @param endIndex   结束位置（不包含）
     */
    public static void reverse(final char[] array, final int startIndex, final int endIndex) {
        if (array == null) {
            return;
        }
        int i = startIndex < 0 ? 0 : startIndex;
        int j = Math.min(array.length, endIndex) - 1;
        char tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array      数组,会变更
     * @param startIndex 其实位置（包含）
     * @param endIndex   结束位置（不包含）
     */
    public static void reverse(final double[] array, final int startIndex, final int endIndex) {
        if (array == null) {
            return;
        }
        int i = startIndex < 0 ? 0 : startIndex;
        int j = Math.min(array.length, endIndex) - 1;
        double tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array      数组,会变更
     * @param startIndex 其实位置（包含）
     * @param endIndex   结束位置（不包含）
     */
    public static void reverse(final float[] array, final int startIndex, final int endIndex) {
        if (array == null) {
            return;
        }
        int i = startIndex < 0 ? 0 : startIndex;
        int j = Math.min(array.length, endIndex) - 1;
        float tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array      数组,会变更
     * @param startIndex 其实位置（包含）
     * @param endIndex   结束位置（不包含）
     */
    public static void reverse(final int[] array, final int startIndex, final int endIndex) {
        if (array == null) {
            return;
        }
        int i = startIndex < 0 ? 0 : startIndex;
        int j = Math.min(array.length, endIndex) - 1;
        int tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array      数组,会变更
     * @param startIndex 其实位置（包含）
     * @param endIndex   结束位置（不包含）
     */
    public static void reverse(final long[] array, final int startIndex, final int endIndex) {
        if (array == null) {
            return;
        }
        int i = startIndex < 0 ? 0 : startIndex;
        int j = Math.min(array.length, endIndex) - 1;
        long tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array      数组,会变更
     * @param startIndex 其实位置（包含）
     * @param endIndex   结束位置（不包含）
     */
    public static void reverse(final Object[] array, final int startIndex, final int endIndex) {
        if (array == null) {
            return;
        }
        int i = startIndex < 0 ? 0 : startIndex;
        int j = Math.min(array.length, endIndex) - 1;
        Object tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    /**
     * 反转数组,会变更原数组
     *
     * @param array      数组,会变更
     * @param startIndex 其实位置（包含）
     * @param endIndex   结束位置（不包含）
     */
    public static void reverse(final short[] array, final int startIndex, final int endIndex) {
        if (array == null) {
            return;
        }
        int i = startIndex < 0 ? 0 : startIndex;
        int j = Math.min(array.length, endIndex) - 1;
        short tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }


    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap(["1", "2", "3"], 0, 2) -&gt; ["3", "2", "1"]</li>
     *                <li>ArrayUtils.swap(["1", "2", "3"], 0, 0) -&gt; ["1", "2", "3"]</li>
     *                <li>ArrayUtils.swap(["1", "2", "3"], 1, 0) -&gt; ["2", "1", "3"]</li>
     *                <li>ArrayUtils.swap(["1", "2", "3"], 0, 5) -&gt; ["1", "2", "3"]</li>
     *                <li>ArrayUtils.swap(["1", "2", "3"], -1, 1) -&gt; ["2", "1", "3"]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final Object[] array, final int offset1, final int offset2) {
        if (array == null || array.length == 0) {
            return;
        }
        swap(array, offset1, offset2, 1);
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([true, false, true], 0, 2) -&gt; [true, false, true]</li>
     *                <li>ArrayUtils.swap([true, false, true], 0, 0) -&gt; [true, false, true]</li>
     *                <li>ArrayUtils.swap([true, false, true], 1, 0) -&gt; [false, true, true]</li>
     *                <li>ArrayUtils.swap([true, false, true], 0, 5) -&gt; [true, false, true]</li>
     *                <li>ArrayUtils.swap([true, false, true], -1, 1) -&gt; [false, true, true]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final long[] array, final int offset1, final int offset2) {
        if (array == null || array.length == 0) {
            return;
        }
        swap(array, offset1, offset2, 1);
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 2) -&gt; [3, 2, 1]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 0) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 1, 0) -&gt; [2, 1, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 5) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], -1, 1) -&gt; [2, 1, 3]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final int[] array, final int offset1, final int offset2) {
        if (array == null || array.length == 0) {
            return;
        }
        swap(array, offset1, offset2, 1);
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 2) -&gt; [3, 2, 1]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 0) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 1, 0) -&gt; [2, 1, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 5) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], -1, 1) -&gt; [2, 1, 3]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final short[] array, final int offset1, final int offset2) {
        if (array == null || array.length == 0) {
            return;
        }
        swap(array, offset1, offset2, 1);
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 2) -&gt; [3, 2, 1]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 0) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 1, 0) -&gt; [2, 1, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 5) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], -1, 1) -&gt; [2, 1, 3]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final char[] array, final int offset1, final int offset2) {
        if (array == null || array.length == 0) {
            return;
        }
        swap(array, offset1, offset2, 1);
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 2) -&gt; [3, 2, 1]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 0) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 1, 0) -&gt; [2, 1, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 5) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], -1, 1) -&gt; [2, 1, 3]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final byte[] array, final int offset1, final int offset2) {
        if (array == null || array.length == 0) {
            return;
        }
        swap(array, offset1, offset2, 1);
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 2) -&gt; [3, 2, 1]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 0) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 1, 0) -&gt; [2, 1, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 5) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], -1, 1) -&gt; [2, 1, 3]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final double[] array, final int offset1, final int offset2) {
        if (array == null || array.length == 0) {
            return;
        }
        swap(array, offset1, offset2, 1);
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 2) -&gt; [3, 2, 1]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 0) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 1, 0) -&gt; [2, 1, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 5) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], -1, 1) -&gt; [2, 1, 3]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final float[] array, final int offset1, final int offset2) {
        if (array == null || array.length == 0) {
            return;
        }
        swap(array, offset1, offset2, 1);
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 2) -&gt; [3, 2, 1]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 0) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 1, 0) -&gt; [2, 1, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], 0, 5) -&gt; [1, 2, 3]</li>
     *                <li>ArrayUtils.swap([1, 2, 3], -1, 1) -&gt; [2, 1, 3]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final boolean[] array, final int offset1, final int offset2) {
        if (array == null || array.length == 0) {
            return;
        }
        swap(array, offset1, offset2, 1);
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     * @param len     从给定索引开始交换的元素数量
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([true, false, true, false], 0, 2, 1) -&gt; [true, false, true, false]</li>
     *                <li>ArrayUtils.swap([true, false, true, false], 0, 0, 1) -&gt; [true, false, true, false]</li>
     *                <li>ArrayUtils.swap([true, false, true, false], 0, 2, 2) -&gt; [true, false, true, false]</li>
     *                <li>ArrayUtils.swap([true, false, true, false], -3, 2, 2) -&gt; [true, false, true, false]</li>
     *                <li>ArrayUtils.swap([true, false, true, false], 0, 3, 3) -&gt; [false, false, true, true]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final boolean[] array, int offset1, int offset2, int len) {
        if (array == null || array.length == 0 || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        if (offset1 < 0) {
            offset1 = 0;
        }
        if (offset2 < 0) {
            offset2 = 0;
        }
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        for (int i = 0; i < len; i++, offset1++, offset2++) {
            final boolean aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
        }
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     * @param len     从给定索引开始交换的元素数量
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final byte[] array, int offset1, int offset2, int len) {
        if (array == null || array.length == 0 || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        if (offset1 < 0) {
            offset1 = 0;
        }
        if (offset2 < 0) {
            offset2 = 0;
        }
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        for (int i = 0; i < len; i++, offset1++, offset2++) {
            final byte aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
        }
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     * @param len     从给定索引开始交换的元素数量
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final char[] array, int offset1, int offset2, int len) {
        if (array == null || array.length == 0 || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        if (offset1 < 0) {
            offset1 = 0;
        }
        if (offset2 < 0) {
            offset2 = 0;
        }
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        for (int i = 0; i < len; i++, offset1++, offset2++) {
            final char aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
        }
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     * @param len     从给定索引开始交换的元素数量
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final double[] array, int offset1, int offset2, int len) {
        if (array == null || array.length == 0 || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        if (offset1 < 0) {
            offset1 = 0;
        }
        if (offset2 < 0) {
            offset2 = 0;
        }
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        for (int i = 0; i < len; i++, offset1++, offset2++) {
            final double aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
        }
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     * @param len     从给定索引开始交换的元素数量
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final float[] array, int offset1, int offset2, int len) {
        if (array == null || array.length == 0 || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        if (offset1 < 0) {
            offset1 = 0;
        }
        if (offset2 < 0) {
            offset2 = 0;
        }
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        for (int i = 0; i < len; i++, offset1++, offset2++) {
            final float aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
        }

    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     * @param len     从给定索引开始交换的元素数量
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]</li>
     *                </ul>
     */
    public static void swap(final int[] array, int offset1, int offset2, int len) {
        if (array == null || array.length == 0 || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        if (offset1 < 0) {
            offset1 = 0;
        }
        if (offset2 < 0) {
            offset2 = 0;
        }
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        for (int i = 0; i < len; i++, offset1++, offset2++) {
            final int aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
        }
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     * @param len     从给定索引开始交换的元素数量
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]</li>
     *                </ul>
     */
    public static void swap(final long[] array, int offset1, int offset2, int len) {
        if (array == null || array.length == 0 || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        if (offset1 < 0) {
            offset1 = 0;
        }
        if (offset2 < 0) {
            offset2 = 0;
        }
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        for (int i = 0; i < len; i++, offset1++, offset2++) {
            final long aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
        }
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     * @param len     从给定索引开始交换的元素数量
     *                <p>
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap(["1", "2", "3", "4"], 0, 2, 1) -&gt; ["3", "2", "1", "4"]</li>
     *                <li>ArrayUtils.swap(["1", "2", "3", "4"], 0, 0, 1) -&gt; ["1", "2", "3", "4"]</li>
     *                <li>ArrayUtils.swap(["1", "2", "3", "4"], 2, 0, 2) -&gt; ["3", "4", "1", "2"]</li>
     *                <li>ArrayUtils.swap(["1", "2", "3", "4"], -3, 2, 2) -&gt; ["3", "4", "1", "2"]</li>
     *                <li>ArrayUtils.swap(["1", "2", "3", "4"], 0, 3, 3) -&gt; ["4", "2", "3", "1"]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final Object[] array, int offset1, int offset2, int len) {
        if (array == null || array.length == 0 || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        if (offset1 < 0) {
            offset1 = 0;
        }
        if (offset2 < 0) {
            offset2 = 0;
        }
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        for (int i = 0; i < len; i++, offset1++, offset2++) {
            final Object aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
        }
    }

    /**
     * 交换数组中连个位置的值
     *
     * @param array   数组
     * @param offset1 位置1
     * @param offset2 位置2
     * @param len     从给定索引开始交换的元素数量
     *                Examples:
     *                <ul>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 2, 1) -&gt; [3, 2, 1, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 0, 1) -&gt; [1, 2, 3, 4]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 2, 0, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], -3, 2, 2) -&gt; [3, 4, 1, 2]</li>
     *                <li>ArrayUtils.swap([1, 2, 3, 4], 0, 3, 3) -&gt; [4, 2, 3, 1]</li>
     *                </ul>
     * @since 3.5.0
     */
    public static void swap(final short[] array, int offset1, int offset2, int len) {
        if (array == null || array.length == 0 || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        if (offset1 < 0) {
            offset1 = 0;
        }
        if (offset2 < 0) {
            offset2 = 0;
        }
        if (offset1 == offset2) {
            return;
        }
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        for (int i = 0; i < len; i++, offset1++, offset2++) {
            final short aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
        }
    }


    /**
     * Shifts the order of the given array.
     *
     * <p>There is no special handling for multi-dimensional arrays. This method
     * does nothing for {@code null} or empty input arrays.</p>
     *
     * @param array  the array to shift, may be {@code null}
     * @param offset The number of positions to rotate the elements.  If the offset is larger than the number of elements to
     *               rotate, than the effective offset is modulo the number of elements to rotate.
     * @since 3.5.0
     */
    public static void shift(final Object[] array, final int offset) {
        if (array == null) {
            return;
        }
        shift(array, 0, array.length, offset);
    }

    /**
     * Shifts the order of the given long array.
     *
     * <p>There is no special handling for multi-dimensional arrays. This method
     * does nothing for {@code null} or empty input arrays.</p>
     *
     * @param array  the array to shift, may be {@code null}
     * @param offset The number of positions to rotate the elements.  If the offset is larger than the number of elements to
     *               rotate, than the effective offset is modulo the number of elements to rotate.
     * @since 3.5.0
     */
    public static void shift(final long[] array, final int offset) {
        if (array == null) {
            return;
        }
        shift(array, 0, array.length, offset);
    }

    /**
     * Shifts the order of the given int array.
     *
     * <p>There is no special handling for multi-dimensional arrays. This method
     * does nothing for {@code null} or empty input arrays.</p>
     *
     * @param array  the array to shift, may be {@code null}
     * @param offset The number of positions to rotate the elements.  If the offset is larger than the number of elements to
     *               rotate, than the effective offset is modulo the number of elements to rotate.
     * @since 3.5.0
     */
    public static void shift(final int[] array, final int offset) {
        if (array == null) {
            return;
        }
        shift(array, 0, array.length, offset);
    }

    /**
     * 改变数组的顺序
     *
     * @param array  原数组
     * @param offset 偏移量
     */
    public static void shift(final short[] array, final int offset) {
        if (array == null) {
            return;
        }
        shift(array, 0, array.length, offset);
    }

    /**
     * 改变数组的顺序
     *
     * @param array  原数组
     * @param offset 偏移量
     */
    public static void shift(final char[] array, final int offset) {
        if (array == null) {
            return;
        }
        shift(array, 0, array.length, offset);
    }

    /**
     * 改变数组的顺序
     *
     * @param array  原数组
     * @param offset 偏移量
     */
    public static void shift(final byte[] array, final int offset) {
        if (array == null) {
            return;
        }
        shift(array, 0, array.length, offset);
    }

    /**
     * 改变数组的顺序
     *
     * @param array  原数组
     * @param offset 偏移量
     */
    public static void shift(final double[] array, final int offset) {
        if (array == null) {
            return;
        }
        shift(array, 0, array.length, offset);
    }

    /**
     * 改变数组的顺序
     *
     * @param array  原数组
     * @param offset 偏移量
     */
    public static void shift(final float[] array, final int offset) {
        if (array == null) {
            return;
        }
        shift(array, 0, array.length, offset);
    }

    /**
     * 改变数组的顺序
     *
     * @param array  原数组
     * @param offset 偏移量
     */
    public static void shift(final boolean[] array, final int offset) {
        if (array == null) {
            return;
        }
        shift(array, 0, array.length, offset);
    }

    /**
     * 改变数组的顺序
     *
     * @param array      原数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param offset     偏移量
     */
    public static void shift(final boolean[] array, int startIndex, int endIndex, int offset) {
        if (array == null) {
            return;
        }
        if (startIndex >= array.length - 1 || endIndex <= 0) {
            return;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex >= array.length) {
            endIndex = array.length;
        }
        int n = endIndex - startIndex;
        if (n <= 1) {
            return;
        }
        offset %= n;
        if (offset < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            final int n_offset = n - offset;

            if (offset > n_offset) {
                swap(array, startIndex, startIndex + n - n_offset, n_offset);
                n = offset;
                offset -= n_offset;
            } else if (offset < n_offset) {
                swap(array, startIndex, startIndex + n_offset, offset);
                startIndex += offset;
                n = n_offset;
            } else {
                swap(array, startIndex, startIndex + n_offset, offset);
                break;
            }
        }
    }

    /**
     * 改变数组的顺序
     *
     * @param array      原数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param offset     偏移量
     */
    public static void shift(final byte[] array, int startIndex, int endIndex, int offset) {
        if (array == null) {
            return;
        }
        if (startIndex >= array.length - 1 || endIndex <= 0) {
            return;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex >= array.length) {
            endIndex = array.length;
        }
        int n = endIndex - startIndex;
        if (n <= 1) {
            return;
        }
        offset %= n;
        if (offset < 0) {
            offset += n;
        }
        // For algorithm explanations and proof of O(n) time complexity and O(1) space complexity
        // see https://beradrian.wordpress.com/2015/04/07/shift-an-array-in-on-in-place/
        while (n > 1 && offset > 0) {
            final int n_offset = n - offset;

            if (offset > n_offset) {
                swap(array, startIndex, startIndex + n - n_offset, n_offset);
                n = offset;
                offset -= n_offset;
            } else if (offset < n_offset) {
                swap(array, startIndex, startIndex + n_offset, offset);
                startIndex += offset;
                n = n_offset;
            } else {
                swap(array, startIndex, startIndex + n_offset, offset);
                break;
            }
        }
    }

    /**
     * 改变数组的顺序
     *
     * @param array      原数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param offset     偏移量
     */
    public static void shift(final char[] array, int startIndex, int endIndex, int offset) {
        if (array == null) {
            return;
        }
        if (startIndex >= array.length - 1 || endIndex <= 0) {
            return;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex >= array.length) {
            endIndex = array.length;
        }
        int n = endIndex - startIndex;
        if (n <= 1) {
            return;
        }
        offset %= n;
        if (offset < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            final int n_offset = n - offset;

            if (offset > n_offset) {
                swap(array, startIndex, startIndex + n - n_offset, n_offset);
                n = offset;
                offset -= n_offset;
            } else if (offset < n_offset) {
                swap(array, startIndex, startIndex + n_offset, offset);
                startIndex += offset;
                n = n_offset;
            } else {
                swap(array, startIndex, startIndex + n_offset, offset);
                break;
            }
        }
    }

    /**
     * 改变数组的顺序
     *
     * @param array      原数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param offset     偏移量
     */
    public static void shift(final double[] array, int startIndex, int endIndex, int offset) {
        if (array == null) {
            return;
        }
        if (startIndex >= array.length - 1 || endIndex <= 0) {
            return;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex >= array.length) {
            endIndex = array.length;
        }
        int n = endIndex - startIndex;
        if (n <= 1) {
            return;
        }
        offset %= n;
        if (offset < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            final int n_offset = n - offset;

            if (offset > n_offset) {
                swap(array, startIndex, startIndex + n - n_offset, n_offset);
                n = offset;
                offset -= n_offset;
            } else if (offset < n_offset) {
                swap(array, startIndex, startIndex + n_offset, offset);
                startIndex += offset;
                n = n_offset;
            } else {
                swap(array, startIndex, startIndex + n_offset, offset);
                break;
            }
        }
    }

    /**
     * 改变数组的顺序
     *
     * @param array      原数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param offset     偏移量
     */
    public static void shift(final float[] array, int startIndex, int endIndex, int offset) {
        if (array == null) {
            return;
        }
        if (startIndex >= array.length - 1 || endIndex <= 0) {
            return;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex >= array.length) {
            endIndex = array.length;
        }
        int n = endIndex - startIndex;
        if (n <= 1) {
            return;
        }
        offset %= n;
        if (offset < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            final int n_offset = n - offset;

            if (offset > n_offset) {
                swap(array, startIndex, startIndex + n - n_offset, n_offset);
                n = offset;
                offset -= n_offset;
            } else if (offset < n_offset) {
                swap(array, startIndex, startIndex + n_offset, offset);
                startIndex += offset;
                n = n_offset;
            } else {
                swap(array, startIndex, startIndex + n_offset, offset);
                break;
            }
        }
    }

    /**
     * 改变数组的顺序
     *
     * @param array      原数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param offset     偏移量
     */
    public static void shift(final int[] array, int startIndex, int endIndex, int offset) {
        if (array == null) {
            return;
        }
        if (startIndex >= array.length - 1 || endIndex <= 0) {
            return;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex >= array.length) {
            endIndex = array.length;
        }
        int n = endIndex - startIndex;
        if (n <= 1) {
            return;
        }
        offset %= n;
        if (offset < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            final int n_offset = n - offset;

            if (offset > n_offset) {
                swap(array, startIndex, startIndex + n - n_offset, n_offset);
                n = offset;
                offset -= n_offset;
            } else if (offset < n_offset) {
                swap(array, startIndex, startIndex + n_offset, offset);
                startIndex += offset;
                n = n_offset;
            } else {
                swap(array, startIndex, startIndex + n_offset, offset);
                break;
            }
        }
    }

    /**
     * 改变数组的顺序
     *
     * @param array      原数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param offset     偏移量
     */
    public static void shift(final long[] array, int startIndex, int endIndex, int offset) {
        if (array == null) {
            return;
        }
        if (startIndex >= array.length - 1 || endIndex <= 0) {
            return;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex >= array.length) {
            endIndex = array.length;
        }
        int n = endIndex - startIndex;
        if (n <= 1) {
            return;
        }
        offset %= n;
        if (offset < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            final int n_offset = n - offset;

            if (offset > n_offset) {
                swap(array, startIndex, startIndex + n - n_offset, n_offset);
                n = offset;
                offset -= n_offset;
            } else if (offset < n_offset) {
                swap(array, startIndex, startIndex + n_offset, offset);
                startIndex += offset;
                n = n_offset;
            } else {
                swap(array, startIndex, startIndex + n_offset, offset);
                break;
            }
        }
    }

    /**
     * 改变数组的顺序
     *
     * @param array      原数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param offset     偏移量
     */
    public static void shift(final Object[] array, int startIndex, int endIndex, int offset) {
        if (array == null) {
            return;
        }
        if (startIndex >= array.length - 1 || endIndex <= 0) {
            return;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex >= array.length) {
            endIndex = array.length;
        }
        int n = endIndex - startIndex;
        if (n <= 1) {
            return;
        }
        offset %= n;
        if (offset < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            final int n_offset = n - offset;

            if (offset > n_offset) {
                swap(array, startIndex, startIndex + n - n_offset, n_offset);
                n = offset;
                offset -= n_offset;
            } else if (offset < n_offset) {
                swap(array, startIndex, startIndex + n_offset, offset);
                startIndex += offset;
                n = n_offset;
            } else {
                swap(array, startIndex, startIndex + n_offset, offset);
                break;
            }
        }
    }

    /**
     * 改变数组的顺序
     *
     * @param array      原数组
     * @param startIndex 开始索引
     * @param endIndex   结束索引
     * @param offset     偏移量
     */
    public static void shift(final short[] array, int startIndex, int endIndex, int offset) {
        if (array == null) {
            return;
        }
        if (startIndex >= array.length - 1 || endIndex <= 0) {
            return;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (endIndex >= array.length) {
            endIndex = array.length;
        }
        int n = endIndex - startIndex;
        if (n <= 1) {
            return;
        }
        offset %= n;
        if (offset < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            final int n_offset = n - offset;

            if (offset > n_offset) {
                swap(array, startIndex, startIndex + n - n_offset, n_offset);
                n = offset;
                offset -= n_offset;
            } else if (offset < n_offset) {
                swap(array, startIndex, startIndex + n_offset, offset);
                startIndex += offset;
                n = n_offset;
            } else {
                swap(array, startIndex, startIndex + n_offset, offset);
                break;
            }
        }
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final Object[] array, final Object value) {
        return indexOf(array, value, 0);
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final Object[] array, final Object value, int index) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            index = 0;
        }
        if (value == null) {
            for (int i = index; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = index; i < array.length; i++) {
                if (value.equals(array[i])) {
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final Object[] array, final Object value) {
        return lastIndexOf(array, value, Integer.MAX_VALUE);
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final Object[] array, final Object value, int index) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            return INDEX_NOT_FOUND;
        } else if (index >= array.length) {
            index = array.length - 1;
        }
        if (value == null) {
            for (int i = index; i >= 0; i--) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else if (array.getClass().getComponentType().isInstance(value)) {
            for (int i = index; i >= 0; i--) {
                if (value.equals(array[i])) {
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final Object[] array, final Object value) {
        return indexOf(array, value) != INDEX_NOT_FOUND;
    }


    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final long[] array, final long value) {
        return indexOf(array, value, 0);
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final long[] array, final long value, int index) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            index = 0;
        }
        for (int i = index; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final long[] array, final long value) {
        return lastIndexOf(array, value, Integer.MAX_VALUE);
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final long[] array, final long value, int index) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            return INDEX_NOT_FOUND;
        } else if (index >= array.length) {
            index = array.length - 1;
        }
        for (int i = index; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final long[] array, final long value) {
        return indexOf(array, value) != INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final int[] array, final int value) {
        return indexOf(array, value, 0);
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final int[] array, final int value, int index) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            index = 0;
        }
        for (int i = index; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final int[] array, final int value) {
        return lastIndexOf(array, value, Integer.MAX_VALUE);
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final int[] array, final int value, int index) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            return INDEX_NOT_FOUND;
        } else if (index >= array.length) {
            index = array.length - 1;
        }
        for (int i = index; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final int[] array, final int value) {
        return indexOf(array, value) != INDEX_NOT_FOUND;
    }


    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final short[] array, final short value) {
        return indexOf(array, value, 0);
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final short[] array, final short value, int index) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            index = 0;
        }
        for (int i = index; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final short[] array, final short value) {
        return lastIndexOf(array, value, Integer.MAX_VALUE);
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final short[] array, final short value, int index) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            return INDEX_NOT_FOUND;
        } else if (index >= array.length) {
            index = array.length - 1;
        }
        for (int i = index; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final short[] array, final short value) {
        return indexOf(array, value) != INDEX_NOT_FOUND;
    }


    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final char[] array, final char value) {
        return indexOf(array, value, 0);
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final char[] array, final char value, int index) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            index = 0;
        }
        for (int i = index; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final char[] array, final char value) {
        return lastIndexOf(array, value, Integer.MAX_VALUE);
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final char[] array, final char value, int index) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            return INDEX_NOT_FOUND;
        } else if (index >= array.length) {
            index = array.length - 1;
        }
        for (int i = index; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final char[] array, final char value) {
        return indexOf(array, value) != INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final byte[] array, final byte value) {
        return indexOf(array, value, 0);
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final byte[] array, final byte value, int index) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            index = 0;
        }
        for (int i = index; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final byte[] array, final byte value) {
        return lastIndexOf(array, value, Integer.MAX_VALUE);
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final byte[] array, final byte value, int index) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            return INDEX_NOT_FOUND;
        } else if (index >= array.length) {
            index = array.length - 1;
        }
        for (int i = index; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final byte[] array, final byte value) {
        return indexOf(array, value) != INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final double[] array, final double value) {
        return indexOf(array, value, 0);
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array     数组
     * @param value     被检查的元素
     * @param tolerance 容差
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final double[] array, final double value, final double tolerance) {
        return indexOf(array, value, 0, tolerance);
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final double[] array, final double value, int index) {
        if (isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            index = 0;
        }
        for (int i = index; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array     数组
     * @param value     被检查的元素
     * @param index     索引
     * @param tolerance 容差
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final double[] array, final double value, int index, final double tolerance) {
        if (isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            index = 0;
        }
        final double min = value - tolerance;
        final double max = value + tolerance;
        for (int i = index; i < array.length; i++) {
            if (array[i] >= min && array[i] <= max) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final double[] array, final double value) {
        return lastIndexOf(array, value, Integer.MAX_VALUE);
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array     数组
     * @param value     被检查的元素
     * @param tolerance 容差
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final double[] array, final double value, final double tolerance) {
        return lastIndexOf(array, value, Integer.MAX_VALUE, tolerance);
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final double[] array, final double value, int index) {
        if (isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            return INDEX_NOT_FOUND;
        } else if (index >= array.length) {
            index = array.length - 1;
        }
        for (int i = index; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array     数组
     * @param value     被检查的元素
     * @param index     索引
     * @param tolerance 容差
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final double[] array, final double value, int index, final double tolerance) {
        if (isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            return INDEX_NOT_FOUND;
        } else if (index >= array.length) {
            index = array.length - 1;
        }
        final double min = value - tolerance;
        final double max = value + tolerance;
        for (int i = index; i >= 0; i--) {
            if (array[i] >= min && array[i] <= max) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final double[] array, final double value) {
        return indexOf(array, value) != INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array     数组
     * @param value     被检查的元素
     * @param tolerance 容差
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static boolean contains(final double[] array, final double value, final double tolerance) {
        return indexOf(array, value, 0, tolerance) != INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final float[] array, final float value) {
        return indexOf(array, value, 0);
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final float[] array, final float value, int index) {
        if (isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            index = 0;
        }
        for (int i = index; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final float[] array, final float value) {
        return lastIndexOf(array, value, Integer.MAX_VALUE);
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final float[] array, final float value, int index) {
        if (isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            return INDEX_NOT_FOUND;
        } else if (index >= array.length) {
            index = array.length - 1;
        }
        for (int i = index; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final float[] array, final float value) {
        return indexOf(array, value) != INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final boolean[] array, final boolean value) {
        return indexOf(array, value, 0);
    }

    /**
     * 返回数组中指定元素所在位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int indexOf(final boolean[] array, final boolean value, int index) {
        if (isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            index = 0;
        }
        for (int i = index; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final boolean[] array, final boolean value) {
        return lastIndexOf(array, value, Integer.MAX_VALUE);
    }

    /**
     * 返回数组中指定元素所在最后的位置,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @param index 索引
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.0.7
     */
    public static int lastIndexOf(final boolean[] array, final boolean value, int index) {
        if (isEmpty(array)) {
            return INDEX_NOT_FOUND;
        }
        if (index < 0) {
            return INDEX_NOT_FOUND;
        } else if (index >= array.length) {
            index = array.length - 1;
        }
        for (int i = index; i >= 0; i--) {
            if (value == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final boolean[] array, final boolean value) {
        return indexOf(array, value) != INDEX_NOT_FOUND;
    }

    /**
     * <p>Converts an array of object Characters to primitives.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code Character} array, may be {@code null}
     * @return a {@code char} array, {@code null} if null array input
     * @throws NullPointerException if array content is {@code null}
     */
    public static char[] toPrimitive(final Character[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_CHAR_ARRAY;
        }
        final char[] result = new char[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].charValue();
        }
        return result;
    }

    /**
     * <p>Converts an array of object Character to primitives handling {@code null}.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array        a {@code Character} array, may be {@code null}
     * @param valueForNull the value to insert if {@code null} found
     * @return a {@code char} array, {@code null} if null array input
     */
    public static char[] toPrimitive(final Character[] array, final char valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_CHAR_ARRAY;
        }
        final char[] result = new char[array.length];
        for (int i = 0; i < array.length; i++) {
            final Character b = array[i];
            result[i] = (b == null ? valueForNull : b.charValue());
        }
        return result;
    }

    /**
     * <p>Converts an array of primitive chars to objects.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code char} array
     * @return a {@code Character} array, {@code null} if null array input
     */
    public static Character[] toObject(final char[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_CHARACTER_OBJECT_ARRAY;
        }
        final Character[] result = new Character[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Character.valueOf(array[i]);
        }
        return result;
    }

    /**
     * <p>Converts an array of object Longs to primitives.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code Long} array, may be {@code null}
     * @return a {@code long} array, {@code null} if null array input
     */
    public static long[] toPrimitive(final Long[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_LONG_ARRAY;
        }
        final long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].longValue();
        }
        return result;
    }

    /**
     * <p>Converts an array of object Long to primitives handling {@code null}.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array        a {@code Long} array, may be {@code null}
     * @param valueForNull the value to insert if {@code null} found
     * @return a {@code long} array, {@code null} if null array input
     */
    public static long[] toPrimitive(final Long[] array, final long valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_LONG_ARRAY;
        }
        final long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            final Long b = array[i];
            result[i] = (b == null ? valueForNull : b.longValue());
        }
        return result;
    }

    /**
     * <p>Converts an array of primitive longs to objects.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code long} array
     * @return a {@code Long} array, {@code null} if null array input
     */
    public static Long[] toObject(final long[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_LONG_OBJECT_ARRAY;
        }
        final Long[] result = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Long.valueOf(array[i]);
        }
        return result;
    }

    /**
     * <p>Converts an array of object Integers to primitives.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code Integer} array, may be {@code null}
     * @return an {@code int} array, {@code null} if null array input
     * @throws NullPointerException if array content is {@code null}
     */
    public static int[] toPrimitive(final Integer[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_INT_ARRAY;
        }
        final int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].intValue();
        }
        return result;
    }

    /**
     * <p>Converts an array of object Integer to primitives handling {@code null}.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array        a {@code Integer} array, may be {@code null}
     * @param valueForNull the value to insert if {@code null} found
     * @return an {@code int} array, {@code null} if null array input
     */
    public static int[] toPrimitive(final Integer[] array, final int valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_INT_ARRAY;
        }
        final int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            final Integer b = array[i];
            result[i] = (b == null ? valueForNull : b.intValue());
        }
        return result;
    }

    /**
     * <p>Converts an array of primitive ints to objects.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array an {@code int} array
     * @return an {@code Integer} array, {@code null} if null array input
     */
    public static Integer[] toObject(final int[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_INTEGER_OBJECT_ARRAY;
        }
        final Integer[] result = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Integer.valueOf(array[i]);
        }
        return result;
    }

    /**
     * <p>Converts an array of object Shorts to primitives.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code Short} array, may be {@code null}
     * @return a {@code byte} array, {@code null} if null array input
     * @throws NullPointerException if array content is {@code null}
     */
    public static short[] toPrimitive(final Short[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_SHORT_ARRAY;
        }
        final short[] result = new short[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].shortValue();
        }
        return result;
    }

    /**
     * <p>Converts an array of object Short to primitives handling {@code null}.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array        a {@code Short} array, may be {@code null}
     * @param valueForNull the value to insert if {@code null} found
     * @return a {@code byte} array, {@code null} if null array input
     */
    public static short[] toPrimitive(final Short[] array, final short valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_SHORT_ARRAY;
        }
        final short[] result = new short[array.length];
        for (int i = 0; i < array.length; i++) {
            final Short b = array[i];
            result[i] = (b == null ? valueForNull : b.shortValue());
        }
        return result;
    }

    /**
     * <p>Converts an array of primitive shorts to objects.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code short} array
     * @return a {@code Short} array, {@code null} if null array input
     */
    public static Short[] toObject(final short[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_SHORT_OBJECT_ARRAY;
        }
        final Short[] result = new Short[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Short.valueOf(array[i]);
        }
        return result;
    }

    /**
     * <p>Converts an array of object Bytes to primitives.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code Byte} array, may be {@code null}
     * @return a {@code byte} array, {@code null} if null array input
     * @throws NullPointerException if array content is {@code null}
     */
    public static byte[] toPrimitive(final Byte[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        final byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].byteValue();
        }
        return result;
    }

    /**
     * <p>Converts an array of object Bytes to primitives handling {@code null}.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array        a {@code Byte} array, may be {@code null}
     * @param valueForNull the value to insert if {@code null} found
     * @return a {@code byte} array, {@code null} if null array input
     */
    public static byte[] toPrimitive(final Byte[] array, final byte valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        final byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            final Byte b = array[i];
            result[i] = (b == null ? valueForNull : b.byteValue());
        }
        return result;
    }

    /**
     * <p>Converts an array of primitive bytes to objects.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code byte} array
     * @return a {@code Byte} array, {@code null} if null array input
     */
    public static Byte[] toObject(final byte[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_BYTE_OBJECT_ARRAY;
        }
        final Byte[] result = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Byte.valueOf(array[i]);
        }
        return result;
    }

    /**
     * <p>Converts an array of object Doubles to primitives.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code Double} array, may be {@code null}
     * @return a {@code double} array, {@code null} if null array input
     * @throws NullPointerException if array content is {@code null}
     */
    public static double[] toPrimitive(final Double[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_DOUBLE_ARRAY;
        }
        final double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].doubleValue();
        }
        return result;
    }

    /**
     * <p>Converts an array of object Doubles to primitives handling {@code null}.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array        a {@code Double} array, may be {@code null}
     * @param valueForNull the value to insert if {@code null} found
     * @return a {@code double} array, {@code null} if null array input
     */
    public static double[] toPrimitive(final Double[] array, final double valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_DOUBLE_ARRAY;
        }
        final double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            final Double b = array[i];
            result[i] = (b == null ? valueForNull : b.doubleValue());
        }
        return result;
    }

    /**
     * <p>Converts an array of primitive doubles to objects.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code double} array
     * @return a {@code Double} array, {@code null} if null array input
     */
    public static Double[] toObject(final double[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_DOUBLE_OBJECT_ARRAY;
        }
        final Double[] result = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Double.valueOf(array[i]);
        }
        return result;
    }

    /**
     * <p>Converts an array of object Floats to primitives.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code Float} array, may be {@code null}
     * @return a {@code float} array, {@code null} if null array input
     * @throws NullPointerException if array content is {@code null}
     */
    public static float[] toPrimitive(final Float[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_FLOAT_ARRAY;
        }
        final float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].floatValue();
        }
        return result;
    }

    /**
     * <p>Converts an array of object Floats to primitives handling {@code null}.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array        a {@code Float} array, may be {@code null}
     * @param valueForNull the value to insert if {@code null} found
     * @return a {@code float} array, {@code null} if null array input
     */
    public static float[] toPrimitive(final Float[] array, final float valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_FLOAT_ARRAY;
        }
        final float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            final Float b = array[i];
            result[i] = (b == null ? valueForNull : b.floatValue());
        }
        return result;
    }

    /**
     * <p>Converts an array of primitive floats to objects.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code float} array
     * @return a {@code Float} array, {@code null} if null array input
     */
    public static Float[] toObject(final float[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_FLOAT_OBJECT_ARRAY;
        }
        final Float[] result = new Float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Float.valueOf(array[i]);
        }
        return result;
    }

    /**
     * <p>Create an array of primitive type from an array of wrapper types.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array an array of wrapper object
     * @return an array of the corresponding primitive type, or the original array
     * @since 3.5.0
     */
    public static Object toPrimitive(final Object array) {
        if (array == null) {
            return null;
        }
        final Class<?> ct = array.getClass().getComponentType();
        final Class<?> pt = ClassUtils.wrapperToPrimitive(ct);
        if (Integer.TYPE.equals(pt)) {
            return toPrimitive((Integer[]) array);
        }
        if (Long.TYPE.equals(pt)) {
            return toPrimitive((Long[]) array);
        }
        if (Short.TYPE.equals(pt)) {
            return toPrimitive((Short[]) array);
        }
        if (Double.TYPE.equals(pt)) {
            return toPrimitive((Double[]) array);
        }
        if (Float.TYPE.equals(pt)) {
            return toPrimitive((Float[]) array);
        }
        return array;
    }

    /**
     * <p>Converts an array of object Booleans to primitives.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code Boolean} array, may be {@code null}
     * @return a {@code boolean} array, {@code null} if null array input
     * @throws NullPointerException if array content is {@code null}
     */
    public static boolean[] toPrimitive(final Boolean[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_BOOLEAN_ARRAY;
        }
        final boolean[] result = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].booleanValue();
        }
        return result;
    }

    /**
     * <p>Converts an array of object Booleans to primitives handling {@code null}.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array        a {@code Boolean} array, may be {@code null}
     * @param valueForNull the value to insert if {@code null} found
     * @return a {@code boolean} array, {@code null} if null array input
     */
    public static boolean[] toPrimitive(final Boolean[] array, final boolean valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_BOOLEAN_ARRAY;
        }
        final boolean[] result = new boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            final Boolean b = array[i];
            result[i] = (b == null ? valueForNull : b.booleanValue());
        }
        return result;
    }

    /**
     * <p>Converts an array of primitive booleans to objects.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param array a {@code boolean} array
     * @return a {@code Boolean} array, {@code null} if null array input
     */
    public static Boolean[] toObject(final boolean[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_BOOLEAN_OBJECT_ARRAY;
        }
        final Boolean[] result = new Boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = (array[i] ? Boolean.TRUE : Boolean.FALSE);
        }
        return result;
    }

    /**
     * 数组是否为空
     * 此方法会匹配单一对象,如果此对象为{@code null}则返回true
     * 如果此对象为非数组,理解为此对象为数组的第一个元素,则返回false
     * 如果此对象为数组对象,数组长度大于0情况下返回false,否则返回true
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(Object array) {
        if (null == array) {
            return true;
        } else if (isArray(array)) {
            return 0 == Array.getLength(array);
        }
        throw new InstrumentException("Object to provide is not a Array !");
    }

    /**
     * 数组是否为空
     * 此方法会匹配单一对象,如果此对象为{@code null}则返回true
     * 如果此对象为非数组,理解为此对象为数组的第一个元素,则返回false
     * 如果此对象为数组对象,数组长度大于0情况下返回false,否则返回true
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final Object[] array) {
        return getLength(array) == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final long[] array) {
        return getLength(array) == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final int[] array) {
        return getLength(array) == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final short[] array) {
        return getLength(array) == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final char[] array) {
        return getLength(array) == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final byte[] array) {
        return getLength(array) == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final double[] array) {
        return getLength(array) == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final float[] array) {
        return getLength(array) == 0;
    }

    /**
     * 数组是否为空
     *
     * @param array 数组
     * @return 是否为空
     */
    public static boolean isEmpty(final boolean[] array) {
        return getLength(array) == 0;
    }

    /**
     * 数组是否为非空
     * 此方法会匹配单一对象,如果此对象为{@code null}则返回false
     * 如果此对象为非数组,理解为此对象为数组的第一个元素,则返回true
     * 如果此对象为数组对象,数组长度大于0情况下返回true,否则返回false
     *
     * @param <T>   对象
     * @param array 数组
     * @return 是否为非空
     */
    public static <T> boolean isNotEmpty(final T[] array) {
        return !isEmpty(array);
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
     * <p>Adds all the elements of the given arrays into a new array.
     * <p>The new array contains all of the element of {@code array1} followed
     * by all of the elements {@code array2}. When an array is returned, it is always
     * a new array.
     *
     * <pre>
     * ArrayUtils.addAll(null, null)     = null
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * ArrayUtils.addAll([null], [null]) = [null, null]
     * ArrayUtils.addAll(["a", "b", "c"], ["1", "2", "3"]) = ["a", "b", "c", "1", "2", "3"]
     * </pre>
     *
     * @param <T>    the component type of the array
     * @param array1 the first array whose elements are added to the new array, may be {@code null}
     * @param array2 the second array whose elements are added to the new array, may be {@code null}
     * @return The new array, {@code null} if both arrays are {@code null}.
     * The type of the new array is the type of the first array,
     * unless the first array is null, in which case the type is the same as the second array.
     * @throws IllegalArgumentException if the array types are incompatible
     * @since 2.1.0
     */
    public static <T> T[] addAll(final T[] array1, final T... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final Class<?> type1 = array1.getClass().getComponentType();
        final T[] joinedArray = (T[]) Array.newInstance(type1, array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        try {
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        } catch (final ArrayStoreException ase) {
            // Check if problem was due to incompatible types
            /*
             * We do this here, rather than before the copy because:
             * - it would be a wasted check most of the time
             * - safer, in case check turns out to be too strict
             */
            final Class<?> type2 = array2.getClass().getComponentType();
            if (!type1.isAssignableFrom(type2)) {
                throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of "
                        + type1.getName(), ase);
            }
            throw ase; // No, so rethrow original
        }
        return joinedArray;
    }

    /**
     * <p>Adds all the elements of the given arrays into a new array.
     * <p>The new array contains all of the element of {@code array1} followed
     * by all of the elements {@code array2}. When an array is returned, it is always
     * a new array.
     *
     * <pre>
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * </pre>
     *
     * @param array1 the first array whose elements are added to the new array.
     * @param array2 the second array whose elements are added to the new array.
     * @return The new boolean[] array.
     * @since 2.1.0
     */
    public static boolean[] addAll(final boolean[] array1, final boolean... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final boolean[] joinedArray = new boolean[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    /**
     * <p>Adds all the elements of the given arrays into a new array.
     * <p>The new array contains all of the element of {@code array1} followed
     * by all of the elements {@code array2}. When an array is returned, it is always
     * a new array.
     *
     * <pre>
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * </pre>
     *
     * @param array1 the first array whose elements are added to the new array.
     * @param array2 the second array whose elements are added to the new array.
     * @return The new char[] array.
     * @since 2.1.0
     */
    public static char[] addAll(final char[] array1, final char... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final char[] joinedArray = new char[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    /**
     * <p>Adds all the elements of the given arrays into a new array.
     * <p>The new array contains all of the element of {@code array1} followed
     * by all of the elements {@code array2}. When an array is returned, it is always
     * a new array.
     *
     * <pre>
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * </pre>
     *
     * @param array1 the first array whose elements are added to the new array.
     * @param array2 the second array whose elements are added to the new array.
     * @return The new byte[] array.
     * @since 2.1.0
     */
    public static byte[] addAll(final byte[] array1, final byte... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    /**
     * <p>Adds all the elements of the given arrays into a new array.
     * <p>The new array contains all of the element of {@code array1} followed
     * by all of the elements {@code array2}. When an array is returned, it is always
     * a new array.
     *
     * <pre>
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * </pre>
     *
     * @param array1 the first array whose elements are added to the new array.
     * @param array2 the second array whose elements are added to the new array.
     * @return The new short[] array.
     * @since 2.1.0
     */
    public static short[] addAll(final short[] array1, final short... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final short[] joinedArray = new short[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    /**
     * <p>Adds all the elements of the given arrays into a new array.
     * <p>The new array contains all of the element of {@code array1} followed
     * by all of the elements {@code array2}. When an array is returned, it is always
     * a new array.
     *
     * <pre>
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * </pre>
     *
     * @param array1 the first array whose elements are added to the new array.
     * @param array2 the second array whose elements are added to the new array.
     * @return The new int[] array.
     * @since 2.1.0
     */
    public static int[] addAll(final int[] array1, final int... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final int[] joinedArray = new int[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    /**
     * <p>Adds all the elements of the given arrays into a new array.
     * <p>The new array contains all of the element of {@code array1} followed
     * by all of the elements {@code array2}. When an array is returned, it is always
     * a new array.
     *
     * <pre>
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * </pre>
     *
     * @param array1 the first array whose elements are added to the new array.
     * @param array2 the second array whose elements are added to the new array.
     * @return The new long[] array.
     * @since 2.1.0
     */
    public static long[] addAll(final long[] array1, final long... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final long[] joinedArray = new long[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    /**
     * <p>Adds all the elements of the given arrays into a new array.
     * <p>The new array contains all of the element of {@code array1} followed
     * by all of the elements {@code array2}. When an array is returned, it is always
     * a new array.
     *
     * <pre>
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * </pre>
     *
     * @param array1 the first array whose elements are added to the new array.
     * @param array2 the second array whose elements are added to the new array.
     * @return The new float[] array.
     * @since 2.1.0
     */
    public static float[] addAll(final float[] array1, final float... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final float[] joinedArray = new float[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    /**
     * <p>Adds all the elements of the given arrays into a new array.
     * <p>The new array contains all of the element of {@code array1} followed
     * by all of the elements {@code array2}. When an array is returned, it is always
     * a new array.
     *
     * <pre>
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * </pre>
     *
     * @param array1 the first array whose elements are added to the new array.
     * @param array2 the second array whose elements are added to the new array.
     * @return The new double[] array.
     * @since 2.1.0
     */
    public static double[] addAll(final double[] array1, final double... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final double[] joinedArray = new double[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    /**
     * <p>Copies the given array and adds the given element at the end of the new array.
     *
     * <p>The new array contains the same elements of the input
     * array plus the given element in the last position. The component type of
     * the new array is the same as that of the input array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element, unless the element itself is null,
     * in which case the return type is Object[]
     *
     * <pre>
     * ArrayUtils.add(null, null)      = IllegalArgumentException
     * ArrayUtils.add(null, "a")       = ["a"]
     * ArrayUtils.add(["a"], null)     = ["a", null]
     * ArrayUtils.add(["a"], "b")      = ["a", "b"]
     * ArrayUtils.add(["a", "b"], "c") = ["a", "b", "c"]
     * </pre>
     *
     * @param <T>     the component type of the array
     * @param array   the array to "add" the element to, may be {@code null}
     * @param element the object to add, may be {@code null}
     * @return A new array containing the existing elements plus the new element
     * The returned array type will be that of the input array (unless null),
     * in which case it will have the same type as the element.
     * If both are null, an IllegalArgumentException is thrown
     * @throws IllegalArgumentException if both arguments are null
     * @since 2.1.0
     */
    public static <T> T[] add(final T[] array, final T element) {
        Class<?> type;
        if (array != null) {
            type = array.getClass().getComponentType();
        } else if (element != null) {
            type = element.getClass();
        } else {
            throw new IllegalArgumentException("Arguments cannot both be null");
        }
        final T[] newArray = (T[]) copyArrayGrow1(array, type);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    /**
     * <p>Copies the given array and adds the given element at the end of the new array.
     *
     * <p>The new array contains the same elements of the input
     * array plus the given element in the last position. The component type of
     * the new array is the same as that of the input array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add(null, true)          = [true]
     * ArrayUtils.add([true], false)       = [true, false]
     * ArrayUtils.add([true, false], true) = [true, false, true]
     * </pre>
     *
     * @param array   the array to copy and add the element to, may be {@code null}
     * @param element the object to add at the last index of the new array
     * @return A new array containing the existing elements plus the new element
     * @since 2.1.0
     */
    public static boolean[] add(final boolean[] array, final boolean element) {
        final boolean[] newArray = (boolean[]) copyArrayGrow1(array, Boolean.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    /**
     * <p>Copies the given array and adds the given element at the end of the new array.
     *
     * <p>The new array contains the same elements of the input
     * array plus the given element in the last position. The component type of
     * the new array is the same as that of the input array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add(null, 0)   = [0]
     * ArrayUtils.add([1], 0)    = [1, 0]
     * ArrayUtils.add([1, 0], 1) = [1, 0, 1]
     * </pre>
     *
     * @param array   the array to copy and add the element to, may be {@code null}
     * @param element the object to add at the last index of the new array
     * @return A new array containing the existing elements plus the new element
     * @since 2.1.0
     */
    public static byte[] add(final byte[] array, final byte element) {
        final byte[] newArray = (byte[]) copyArrayGrow1(array, Byte.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    /**
     * <p>Copies the given array and adds the given element at the end of the new array.
     *
     * <p>The new array contains the same elements of the input
     * array plus the given element in the last position. The component type of
     * the new array is the same as that of the input array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add(null, '0')       = ['0']
     * ArrayUtils.add(['1'], '0')      = ['1', '0']
     * ArrayUtils.add(['1', '0'], '1') = ['1', '0', '1']
     * </pre>
     *
     * @param array   the array to copy and add the element to, may be {@code null}
     * @param element the object to add at the last index of the new array
     * @return A new array containing the existing elements plus the new element
     * @since 2.1.0
     */
    public static char[] add(final char[] array, final char element) {
        final char[] newArray = (char[]) copyArrayGrow1(array, Character.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    /**
     * <p>Copies the given array and adds the given element at the end of the new array.
     *
     * <p>The new array contains the same elements of the input
     * array plus the given element in the last position. The component type of
     * the new array is the same as that of the input array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add(null, 0)   = [0]
     * ArrayUtils.add([1], 0)    = [1, 0]
     * ArrayUtils.add([1, 0], 1) = [1, 0, 1]
     * </pre>
     *
     * @param array   the array to copy and add the element to, may be {@code null}
     * @param element the object to add at the last index of the new array
     * @return A new array containing the existing elements plus the new element
     * @since 2.1.0
     */
    public static double[] add(final double[] array, final double element) {
        final double[] newArray = (double[]) copyArrayGrow1(array, Double.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    /**
     * <p>Copies the given array and adds the given element at the end of the new array.
     *
     * <p>The new array contains the same elements of the input
     * array plus the given element in the last position. The component type of
     * the new array is the same as that of the input array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add(null, 0)   = [0]
     * ArrayUtils.add([1], 0)    = [1, 0]
     * ArrayUtils.add([1, 0], 1) = [1, 0, 1]
     * </pre>
     *
     * @param array   the array to copy and add the element to, may be {@code null}
     * @param element the object to add at the last index of the new array
     * @return A new array containing the existing elements plus the new element
     * @since 2.1.0
     */
    public static float[] add(final float[] array, final float element) {
        final float[] newArray = (float[]) copyArrayGrow1(array, Float.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    /**
     * <p>Copies the given array and adds the given element at the end of the new array.
     *
     * <p>The new array contains the same elements of the input
     * array plus the given element in the last position. The component type of
     * the new array is the same as that of the input array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add(null, 0)   = [0]
     * ArrayUtils.add([1], 0)    = [1, 0]
     * ArrayUtils.add([1, 0], 1) = [1, 0, 1]
     * </pre>
     *
     * @param array   the array to copy and add the element to, may be {@code null}
     * @param element the object to add at the last index of the new array
     * @return A new array containing the existing elements plus the new element
     * @since 2.1.0
     */
    public static int[] add(final int[] array, final int element) {
        final int[] newArray = (int[]) copyArrayGrow1(array, Integer.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    /**
     * <p>Copies the given array and adds the given element at the end of the new array.
     *
     * <p>The new array contains the same elements of the input
     * array plus the given element in the last position. The component type of
     * the new array is the same as that of the input array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add(null, 0)   = [0]
     * ArrayUtils.add([1], 0)    = [1, 0]
     * ArrayUtils.add([1, 0], 1) = [1, 0, 1]
     * </pre>
     *
     * @param array   the array to copy and add the element to, may be {@code null}
     * @param element the object to add at the last index of the new array
     * @return A new array containing the existing elements plus the new element
     * @since 2.1.0
     */
    public static long[] add(final long[] array, final long element) {
        final long[] newArray = (long[]) copyArrayGrow1(array, Long.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    /**
     * <p>Copies the given array and adds the given element at the end of the new array.
     *
     * <p>The new array contains the same elements of the input
     * array plus the given element in the last position. The component type of
     * the new array is the same as that of the input array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add(null, 0)   = [0]
     * ArrayUtils.add([1], 0)    = [1, 0]
     * ArrayUtils.add([1, 0], 1) = [1, 0, 1]
     * </pre>
     *
     * @param array   the array to copy and add the element to, may be {@code null}
     * @param element the object to add at the last index of the new array
     * @return A new array containing the existing elements plus the new element
     * @since 2.1.0
     */
    public static short[] add(final short[] array, final short element) {
        final short[] newArray = (short[]) copyArrayGrow1(array, Short.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    /**
     * Returns a copy of the given array of size 1 greater than the argument.
     * The last value of the array is left to the default value.
     *
     * @param array                 The array to copy, must not be {@code null}.
     * @param newArrayComponentType If {@code array} is {@code null}, create a
     *                              size 1 array of this type.
     * @return A new copy of the array of size 1 greater than the input.
     */
    private static Object copyArrayGrow1(final Object array, final Class<?> newArrayComponentType) {
        if (array != null) {
            final int arrayLength = Array.getLength(array);
            final Object newArray = Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
            System.arraycopy(array, 0, newArray, 0, arrayLength);
            return newArray;
        }
        return Array.newInstance(newArrayComponentType, 1);
    }

    /**
     * <p>Inserts the specified element at the specified position in the array.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array plus the given element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add(null, 0, null)      = IllegalArgumentException
     * ArrayUtils.add(null, 0, "a")       = ["a"]
     * ArrayUtils.add(["a"], 1, null)     = ["a", null]
     * ArrayUtils.add(["a"], 1, "b")      = ["a", "b"]
     * ArrayUtils.add(["a", "b"], 3, "c") = ["a", "b", "c"]
     * </pre>
     *
     * @param <T>     the component type of the array
     * @param array   the array to add the element to, may be {@code null}
     * @param index   the position of the new object
     * @param element the object to add
     * @return A new array containing the existing elements and the new element
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt; array.length).
     * @throws IllegalArgumentException  if both array and element are null
     *                                   may be removed in a future release. Please note the handling of {@code null} input arrays differs
     *                                   in the new method: inserting {@code X} into a {@code null} array results in {@code null} not {@code X}.
     */
    public static <T> T[] add(final T[] array, final int index, final T element) {
        Class<?> clss = null;
        if (array != null) {
            clss = array.getClass().getComponentType();
        } else if (element != null) {
            clss = element.getClass();
        } else {
            throw new IllegalArgumentException("Array and element cannot both be null");
        }
        final T[] newArray = (T[]) add(array, index, element, clss);
        return newArray;
    }

    /**
     * <p>Inserts the specified element at the specified position in the array.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array plus the given element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add(null, 0, true)          = [true]
     * ArrayUtils.add([true], 0, false)       = [false, true]
     * ArrayUtils.add([false], 1, true)       = [false, true]
     * ArrayUtils.add([true, false], 1, true) = [true, true, false]
     * </pre>
     *
     * @param array   the array to add the element to, may be {@code null}
     * @param index   the position of the new object
     * @param element the object to add
     * @return A new array containing the existing elements and the new element
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt; array.length).
     *                                   may be removed in a future release. Please note the handling of {@code null} input arrays differs
     *                                   in the new method: inserting {@code X} into a {@code null} array results in {@code null} not {@code X}.
     */
    public static boolean[] add(final boolean[] array, final int index, final boolean element) {
        return (boolean[]) add(array, index, Boolean.valueOf(element), Boolean.TYPE);
    }

    /**
     * <p>Inserts the specified element at the specified position in the array.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array plus the given element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add(null, 0, 'a')            = ['a']
     * ArrayUtils.add(['a'], 0, 'b')           = ['b', 'a']
     * ArrayUtils.add(['a', 'b'], 0, 'c')      = ['c', 'a', 'b']
     * ArrayUtils.add(['a', 'b'], 1, 'k')      = ['a', 'k', 'b']
     * ArrayUtils.add(['a', 'b', 'c'], 1, 't') = ['a', 't', 'b', 'c']
     * </pre>
     *
     * @param array   the array to add the element to, may be {@code null}
     * @param index   the position of the new object
     * @param element the object to add
     * @return A new array containing the existing elements and the new element
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt; array.length).
     */
    public static char[] add(final char[] array, final int index, final char element) {
        return (char[]) add(array, index, Character.valueOf(element), Character.TYPE);
    }

    /**
     * <p>Inserts the specified element at the specified position in the array.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array plus the given element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add([1], 0, 2)         = [2, 1]
     * ArrayUtils.add([2, 6], 2, 3)      = [2, 6, 3]
     * ArrayUtils.add([2, 6], 0, 1)      = [1, 2, 6]
     * ArrayUtils.add([2, 6, 3], 2, 1)   = [2, 6, 1, 3]
     * </pre>
     *
     * @param array   the array to add the element to, may be {@code null}
     * @param index   the position of the new object
     * @param element the object to add
     * @return A new array containing the existing elements and the new element
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt; array.length).
     */
    public static byte[] add(final byte[] array, final int index, final byte element) {
        return (byte[]) add(array, index, Byte.valueOf(element), Byte.TYPE);
    }

    /**
     * <p>Inserts the specified element at the specified position in the array.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array plus the given element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add([1], 0, 2)         = [2, 1]
     * ArrayUtils.add([2, 6], 2, 10)     = [2, 6, 10]
     * ArrayUtils.add([2, 6], 0, -4)     = [-4, 2, 6]
     * ArrayUtils.add([2, 6, 3], 2, 1)   = [2, 6, 1, 3]
     * </pre>
     *
     * @param array   the array to add the element to, may be {@code null}
     * @param index   the position of the new object
     * @param element the object to add
     * @return A new array containing the existing elements and the new element
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt; array.length).
     */
    public static short[] add(final short[] array, final int index, final short element) {
        return (short[]) add(array, index, Short.valueOf(element), Short.TYPE);
    }

    /**
     * <p>Inserts the specified element at the specified position in the array.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array plus the given element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add([1], 0, 2)         = [2, 1]
     * ArrayUtils.add([2, 6], 2, 10)     = [2, 6, 10]
     * ArrayUtils.add([2, 6], 0, -4)     = [-4, 2, 6]
     * ArrayUtils.add([2, 6, 3], 2, 1)   = [2, 6, 1, 3]
     * </pre>
     *
     * @param array   the array to add the element to, may be {@code null}
     * @param index   the position of the new object
     * @param element the object to add
     * @return A new array containing the existing elements and the new element
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt; array.length).
     */
    public static int[] add(final int[] array, final int index, final int element) {
        return (int[]) add(array, index, Integer.valueOf(element), Integer.TYPE);
    }

    /**
     * <p>Inserts the specified element at the specified position in the array.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array plus the given element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add([1L], 0, 2L)           = [2L, 1L]
     * ArrayUtils.add([2L, 6L], 2, 10L)      = [2L, 6L, 10L]
     * ArrayUtils.add([2L, 6L], 0, -4L)      = [-4L, 2L, 6L]
     * ArrayUtils.add([2L, 6L, 3L], 2, 1L)   = [2L, 6L, 1L, 3L]
     * </pre>
     *
     * @param array   the array to add the element to, may be {@code null}
     * @param index   the position of the new object
     * @param element the object to add
     * @return A new array containing the existing elements and the new element
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt; array.length).
     */
    public static long[] add(final long[] array, final int index, final long element) {
        return (long[]) add(array, index, Long.valueOf(element), Long.TYPE);
    }

    /**
     * <p>Inserts the specified element at the specified position in the array.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array plus the given element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add([1.1f], 0, 2.2f)               = [2.2f, 1.1f]
     * ArrayUtils.add([2.3f, 6.4f], 2, 10.5f)        = [2.3f, 6.4f, 10.5f]
     * ArrayUtils.add([2.6f, 6.7f], 0, -4.8f)        = [-4.8f, 2.6f, 6.7f]
     * ArrayUtils.add([2.9f, 6.0f, 0.3f], 2, 1.0f)   = [2.9f, 6.0f, 1.0f, 0.3f]
     * </pre>
     *
     * @param array   the array to add the element to, may be {@code null}
     * @param index   the position of the new object
     * @param element the object to add
     * @return A new array containing the existing elements and the new element
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt; array.length).
     */
    public static float[] add(final float[] array, final int index, final float element) {
        return (float[]) add(array, index, Float.valueOf(element), Float.TYPE);
    }

    /**
     * <p>Inserts the specified element at the specified position in the array.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array plus the given element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, a new one element array is returned
     * whose component type is the same as the element.
     *
     * <pre>
     * ArrayUtils.add([1.1], 0, 2.2)              = [2.2, 1.1]
     * ArrayUtils.add([2.3, 6.4], 2, 10.5)        = [2.3, 6.4, 10.5]
     * ArrayUtils.add([2.6, 6.7], 0, -4.8)        = [-4.8, 2.6, 6.7]
     * ArrayUtils.add([2.9, 6.0, 0.3], 2, 1.0)    = [2.9, 6.0, 1.0, 0.3]
     * </pre>
     *
     * @param array   the array to add the element to, may be {@code null}
     * @param index   the position of the new object
     * @param element the object to add
     * @return A new array containing the existing elements and the new element
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt; array.length).
     */
    public static double[] add(final double[] array, final int index, final double element) {
        return (double[]) add(array, index, Double.valueOf(element), Double.TYPE);
    }

    /**
     * Underlying implementation of add(array, index, element) methods.
     * The last parameter is the class, which may not equal element.getClass
     * for primitives.
     *
     * @param array   the array to add the element to, may be {@code null}
     * @param index   the position of the new object
     * @param element the object to add
     * @param clss    the type of the element being added
     * @return A new array containing the existing elements and the new element
     */
    private static Object add(final Object array, final int index, final Object element, final Class<?> clss) {
        if (array == null) {
            if (index != 0) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Length: 0");
            }
            final Object joinedArray = Array.newInstance(clss, 1);
            Array.set(joinedArray, 0, element);
            return joinedArray;
        }
        final int length = Array.getLength(array);
        if (index > length || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }
        final Object result = Array.newInstance(clss, length + 1);
        System.arraycopy(array, 0, result, 0, index);
        Array.set(result, index, element);
        if (index < length) {
            System.arraycopy(array, index, result, index + 1, length - index);
        }
        return result;
    }

    /**
     * <p>Removes the element at the specified position from the specified array.
     * All subsequent elements are shifted to the left (subtracts one from
     * their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.remove(["a"], 0)           = []
     * ArrayUtils.remove(["a", "b"], 0)      = ["b"]
     * ArrayUtils.remove(["a", "b"], 1)      = ["a"]
     * ArrayUtils.remove(["a", "b", "c"], 1) = ["a", "c"]
     * </pre>
     *
     * @param <T>   the component type of the array
     * @param array the array to remove the element from, may not be {@code null}
     * @param index the position of the element to be removed
     * @return A new array containing the existing elements except the element
     * at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     */
    public static <T> T[] remove(final T[] array, final int index) {
        return (T[]) remove((Object) array, index);
    }

    /**
     * <p>Removes the first occurrence of the specified element from the
     * specified array. All subsequent elements are shifted to the left
     * (subtracts one from their indices). If the array doesn't contains
     * such an element, no elements are removed from the array.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the first occurrence of the specified element. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <pre>
     * ArrayUtils.removeElement(null, "a")            = null
     * ArrayUtils.removeElement([], "a")              = []
     * ArrayUtils.removeElement(["a"], "b")           = ["a"]
     * ArrayUtils.removeElement(["a", "b"], "a")      = ["b"]
     * ArrayUtils.removeElement(["a", "b", "a"], "a") = ["b", "a"]
     * </pre>
     *
     * @param <T>     the component type of the array
     * @param array   the array to remove the element from, may be {@code null}
     * @param element the element to be removed
     * @return A new array containing the existing elements except the first
     * occurrence of the specified element.
     * @since 2.1.0
     */
    public static <T> T[] removeElement(final T[] array, final Object element) {
        final int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }
        return remove(array, index);
    }

    /**
     * <p>Removes the element at the specified position from the specified array.
     * All subsequent elements are shifted to the left (subtracts one from
     * their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.remove([true], 0)              = []
     * ArrayUtils.remove([true, false], 0)       = [false]
     * ArrayUtils.remove([true, false], 1)       = [true]
     * ArrayUtils.remove([true, true, false], 1) = [true, false]
     * </pre>
     *
     * @param array the array to remove the element from, may not be {@code null}
     * @param index the position of the element to be removed
     * @return A new array containing the existing elements except the element
     * at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 2.1.0
     */
    public static boolean[] remove(final boolean[] array, final int index) {
        return (boolean[]) remove((Object) array, index);
    }

    /**
     * <p>Removes the first occurrence of the specified element from the
     * specified array. All subsequent elements are shifted to the left
     * (subtracts one from their indices). If the array doesn't contains
     * such an element, no elements are removed from the array.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the first occurrence of the specified element. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <pre>
     * ArrayUtils.removeElement(null, true)                = null
     * ArrayUtils.removeElement([], true)                  = []
     * ArrayUtils.removeElement([true], false)             = [true]
     * ArrayUtils.removeElement([true, false], false)      = [true]
     * ArrayUtils.removeElement([true, false, true], true) = [false, true]
     * </pre>
     *
     * @param array   the array to remove the element from, may be {@code null}
     * @param element the element to be removed
     * @return A new array containing the existing elements except the first
     * occurrence of the specified element.
     * @since 2.1.0
     */
    public static boolean[] removeElement(final boolean[] array, final boolean element) {
        final int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }
        return remove(array, index);
    }

    /**
     * <p>Removes the element at the specified position from the specified array.
     * All subsequent elements are shifted to the left (subtracts one from
     * their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.remove([1], 0)          = []
     * ArrayUtils.remove([1, 0], 0)       = [0]
     * ArrayUtils.remove([1, 0], 1)       = [1]
     * ArrayUtils.remove([1, 0, 1], 1)    = [1, 1]
     * </pre>
     *
     * @param array the array to remove the element from, may not be {@code null}
     * @param index the position of the element to be removed
     * @return A new array containing the existing elements except the element
     * at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 2.1.0
     */
    public static byte[] remove(final byte[] array, final int index) {
        return (byte[]) remove((Object) array, index);
    }

    /**
     * <p>Removes the first occurrence of the specified element from the
     * specified array. All subsequent elements are shifted to the left
     * (subtracts one from their indices). If the array doesn't contains
     * such an element, no elements are removed from the array.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the first occurrence of the specified element. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <pre>
     * ArrayUtils.removeElement(null, 1)        = null
     * ArrayUtils.removeElement([], 1)          = []
     * ArrayUtils.removeElement([1], 0)         = [1]
     * ArrayUtils.removeElement([1, 0], 0)      = [1]
     * ArrayUtils.removeElement([1, 0, 1], 1)   = [0, 1]
     * </pre>
     *
     * @param array   the array to remove the element from, may be {@code null}
     * @param element the element to be removed
     * @return A new array containing the existing elements except the first
     * occurrence of the specified element.
     * @since 2.1.0
     */
    public static byte[] removeElement(final byte[] array, final byte element) {
        final int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }
        return remove(array, index);
    }

    /**
     * <p>Removes the element at the specified position from the specified array.
     * All subsequent elements are shifted to the left (subtracts one from
     * their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.remove(['a'], 0)           = []
     * ArrayUtils.remove(['a', 'b'], 0)      = ['b']
     * ArrayUtils.remove(['a', 'b'], 1)      = ['a']
     * ArrayUtils.remove(['a', 'b', 'c'], 1) = ['a', 'c']
     * </pre>
     *
     * @param array the array to remove the element from, may not be {@code null}
     * @param index the position of the element to be removed
     * @return A new array containing the existing elements except the element
     * at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 2.1.0
     */
    public static char[] remove(final char[] array, final int index) {
        return (char[]) remove((Object) array, index);
    }

    /**
     * <p>Removes the first occurrence of the specified element from the
     * specified array. All subsequent elements are shifted to the left
     * (subtracts one from their indices). If the array doesn't contains
     * such an element, no elements are removed from the array.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the first occurrence of the specified element. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <pre>
     * ArrayUtils.removeElement(null, 'a')            = null
     * ArrayUtils.removeElement([], 'a')              = []
     * ArrayUtils.removeElement(['a'], 'b')           = ['a']
     * ArrayUtils.removeElement(['a', 'b'], 'a')      = ['b']
     * ArrayUtils.removeElement(['a', 'b', 'a'], 'a') = ['b', 'a']
     * </pre>
     *
     * @param array   the array to remove the element from, may be {@code null}
     * @param element the element to be removed
     * @return A new array containing the existing elements except the first
     * occurrence of the specified element.
     * @since 2.1.0
     */
    public static char[] removeElement(final char[] array, final char element) {
        final int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }
        return remove(array, index);
    }

    /**
     * <p>Removes the element at the specified position from the specified array.
     * All subsequent elements are shifted to the left (subtracts one from
     * their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.remove([1.1], 0)           = []
     * ArrayUtils.remove([2.5, 6.0], 0)      = [6.0]
     * ArrayUtils.remove([2.5, 6.0], 1)      = [2.5]
     * ArrayUtils.remove([2.5, 6.0, 3.8], 1) = [2.5, 3.8]
     * </pre>
     *
     * @param array the array to remove the element from, may not be {@code null}
     * @param index the position of the element to be removed
     * @return A new array containing the existing elements except the element
     * at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 2.1.0
     */
    public static double[] remove(final double[] array, final int index) {
        return (double[]) remove((Object) array, index);
    }

    /**
     * <p>Removes the first occurrence of the specified element from the
     * specified array. All subsequent elements are shifted to the left
     * (subtracts one from their indices). If the array doesn't contains
     * such an element, no elements are removed from the array.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the first occurrence of the specified element. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <pre>
     * ArrayUtils.removeElement(null, 1.1)            = null
     * ArrayUtils.removeElement([], 1.1)              = []
     * ArrayUtils.removeElement([1.1], 1.2)           = [1.1]
     * ArrayUtils.removeElement([1.1, 2.3], 1.1)      = [2.3]
     * ArrayUtils.removeElement([1.1, 2.3, 1.1], 1.1) = [2.3, 1.1]
     * </pre>
     *
     * @param array   the array to remove the element from, may be {@code null}
     * @param element the element to be removed
     * @return A new array containing the existing elements except the first
     * occurrence of the specified element.
     * @since 2.1.0
     */
    public static double[] removeElement(final double[] array, final double element) {
        final int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }
        return remove(array, index);
    }

    /**
     * <p>Removes the element at the specified position from the specified array.
     * All subsequent elements are shifted to the left (subtracts one from
     * their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.remove([1.1], 0)           = []
     * ArrayUtils.remove([2.5, 6.0], 0)      = [6.0]
     * ArrayUtils.remove([2.5, 6.0], 1)      = [2.5]
     * ArrayUtils.remove([2.5, 6.0, 3.8], 1) = [2.5, 3.8]
     * </pre>
     *
     * @param array the array to remove the element from, may not be {@code null}
     * @param index the position of the element to be removed
     * @return A new array containing the existing elements except the element
     * at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 2.1.0
     */
    public static float[] remove(final float[] array, final int index) {
        return (float[]) remove((Object) array, index);
    }

    /**
     * <p>Removes the first occurrence of the specified element from the
     * specified array. All subsequent elements are shifted to the left
     * (subtracts one from their indices). If the array doesn't contains
     * such an element, no elements are removed from the array.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the first occurrence of the specified element. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <pre>
     * ArrayUtils.removeElement(null, 1.1)            = null
     * ArrayUtils.removeElement([], 1.1)              = []
     * ArrayUtils.removeElement([1.1], 1.2)           = [1.1]
     * ArrayUtils.removeElement([1.1, 2.3], 1.1)      = [2.3]
     * ArrayUtils.removeElement([1.1, 2.3, 1.1], 1.1) = [2.3, 1.1]
     * </pre>
     *
     * @param array   the array to remove the element from, may be {@code null}
     * @param element the element to be removed
     * @return A new array containing the existing elements except the first
     * occurrence of the specified element.
     * @since 2.1.0
     */
    public static float[] removeElement(final float[] array, final float element) {
        final int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }
        return remove(array, index);
    }

    /**
     * <p>Removes the element at the specified position from the specified array.
     * All subsequent elements are shifted to the left (subtracts one from
     * their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.remove([1], 0)         = []
     * ArrayUtils.remove([2, 6], 0)      = [6]
     * ArrayUtils.remove([2, 6], 1)      = [2]
     * ArrayUtils.remove([2, 6, 3], 1)   = [2, 3]
     * </pre>
     *
     * @param array the array to remove the element from, may not be {@code null}
     * @param index the position of the element to be removed
     * @return A new array containing the existing elements except the element
     * at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 2.1.0
     */
    public static int[] remove(final int[] array, final int index) {
        return (int[]) remove((Object) array, index);
    }

    /**
     * <p>Removes the first occurrence of the specified element from the
     * specified array. All subsequent elements are shifted to the left
     * (subtracts one from their indices). If the array doesn't contains
     * such an element, no elements are removed from the array.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the first occurrence of the specified element. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <pre>
     * ArrayUtils.removeElement(null, 1)      = null
     * ArrayUtils.removeElement([], 1)        = []
     * ArrayUtils.removeElement([1], 2)       = [1]
     * ArrayUtils.removeElement([1, 3], 1)    = [3]
     * ArrayUtils.removeElement([1, 3, 1], 1) = [3, 1]
     * </pre>
     *
     * @param array   the array to remove the element from, may be {@code null}
     * @param element the element to be removed
     * @return A new array containing the existing elements except the first
     * occurrence of the specified element.
     * @since 2.1.0
     */
    public static int[] removeElement(final int[] array, final int element) {
        final int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }
        return remove(array, index);
    }

    /**
     * <p>Removes the element at the specified position from the specified array.
     * All subsequent elements are shifted to the left (subtracts one from
     * their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.remove([1], 0)         = []
     * ArrayUtils.remove([2, 6], 0)      = [6]
     * ArrayUtils.remove([2, 6], 1)      = [2]
     * ArrayUtils.remove([2, 6, 3], 1)   = [2, 3]
     * </pre>
     *
     * @param array the array to remove the element from, may not be {@code null}
     * @param index the position of the element to be removed
     * @return A new array containing the existing elements except the element
     * at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 2.1.0
     */
    public static long[] remove(final long[] array, final int index) {
        return (long[]) remove((Object) array, index);
    }

    /**
     * <p>Removes the first occurrence of the specified element from the
     * specified array. All subsequent elements are shifted to the left
     * (subtracts one from their indices). If the array doesn't contains
     * such an element, no elements are removed from the array.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the first occurrence of the specified element. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <pre>
     * ArrayUtils.removeElement(null, 1)      = null
     * ArrayUtils.removeElement([], 1)        = []
     * ArrayUtils.removeElement([1], 2)       = [1]
     * ArrayUtils.removeElement([1, 3], 1)    = [3]
     * ArrayUtils.removeElement([1, 3, 1], 1) = [3, 1]
     * </pre>
     *
     * @param array   the array to remove the element from, may be {@code null}
     * @param element the element to be removed
     * @return A new array containing the existing elements except the first
     * occurrence of the specified element.
     * @since 2.1.0
     */
    public static long[] removeElement(final long[] array, final long element) {
        final int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }
        return remove(array, index);
    }

    /**
     * <p>Removes the element at the specified position from the specified array.
     * All subsequent elements are shifted to the left (subtracts one from
     * their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.remove([1], 0)         = []
     * ArrayUtils.remove([2, 6], 0)      = [6]
     * ArrayUtils.remove([2, 6], 1)      = [2]
     * ArrayUtils.remove([2, 6, 3], 1)   = [2, 3]
     * </pre>
     *
     * @param array the array to remove the element from, may not be {@code null}
     * @param index the position of the element to be removed
     * @return A new array containing the existing elements except the element
     * at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 2.1.0
     */
    public static short[] remove(final short[] array, final int index) {
        return (short[]) remove((Object) array, index);
    }

    /**
     * <p>Removes the first occurrence of the specified element from the
     * specified array. All subsequent elements are shifted to the left
     * (subtracts one from their indices). If the array doesn't contains
     * such an element, no elements are removed from the array.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the first occurrence of the specified element. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <pre>
     * ArrayUtils.removeElement(null, 1)      = null
     * ArrayUtils.removeElement([], 1)        = []
     * ArrayUtils.removeElement([1], 2)       = [1]
     * ArrayUtils.removeElement([1, 3], 1)    = [3]
     * ArrayUtils.removeElement([1, 3, 1], 1) = [3, 1]
     * </pre>
     *
     * @param array   the array to remove the element from, may be {@code null}
     * @param element the element to be removed
     * @return A new array containing the existing elements except the first
     * occurrence of the specified element.
     * @since 2.1.0
     */
    public static short[] removeElement(final short[] array, final short element) {
        final int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }
        return remove(array, index);
    }

    /**
     * <p>Removes the element at the specified position from the specified array.
     * All subsequent elements are shifted to the left (subtracts one from
     * their indices).
     *
     * <p>This method returns a new array with the same elements of the input
     * array except the element on the specified position. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * @param array the array to remove the element from, may not be {@code null}
     * @param index the position of the element to be removed
     * @return A new array containing the existing elements except the element
     * at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 2.1.0
     */
    private static Object remove(final Object array, final int index) {
        final int length = getLength(array);
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }

        final Object result = Array.newInstance(array.getClass().getComponentType(), length - 1);
        System.arraycopy(array, 0, result, 0, index);
        if (index < length - 1) {
            System.arraycopy(array, index + 1, result, index, length - index - 1);
        }

        return result;
    }

    /**
     * <p>Removes the elements at the specified positions from the specified array.
     * All remaining elements are shifted to the left.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except those at the specified positions. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.removeAll(["a", "b", "c"], 0, 2) = ["b"]
     * ArrayUtils.removeAll(["a", "b", "c"], 1, 2) = ["a"]
     * </pre>
     *
     * @param <T>     the component type of the array
     * @param array   the array to remove the element from, may not be {@code null}
     * @param indices the positions of the elements to be removed
     * @return A new array containing the existing elements except those
     * at the specified positions.
     * @throws IndexOutOfBoundsException if any index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 3.0.1
     */
    public static <T> T[] removeAll(final T[] array, final int... indices) {
        return (T[]) removeAll((Object) array, indices);
    }

    /**
     * <p>Removes occurrences of specified elements, in specified quantities,
     * from the specified array. All subsequent elements are shifted left.
     * For any element-to-be-removed specified in greater quantities than
     * contained in the original array, no change occurs beyond the
     * removal of the existing matching items.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except for the earliest-encountered occurrences of the specified
     * elements. The component type of the returned array is always the same
     * as that of the input array.
     *
     * <pre>
     * ArrayUtils.removeElements(null, "a", "b")            = null
     * ArrayUtils.removeElements([], "a", "b")              = []
     * ArrayUtils.removeElements(["a"], "b", "c")           = ["a"]
     * ArrayUtils.removeElements(["a", "b"], "a", "c")      = ["b"]
     * ArrayUtils.removeElements(["a", "b", "a"], "a")      = ["b", "a"]
     * ArrayUtils.removeElements(["a", "b", "a"], "a", "a") = ["b"]
     * </pre>
     *
     * @param <T>    the component type of the array
     * @param array  the array to remove the element from, may be {@code null}
     * @param values the elements to be removed
     * @return A new array containing the existing elements except the
     * earliest-encountered occurrences of the specified elements.
     * @since 3.0.1
     */
    public static <T> T[] removeElements(final T[] array, final T... values) {
        if (isEmpty(array) || isEmpty(values)) {
            return clone(array);
        }
        final HashMap<T, MutableInt> occurrences = new HashMap<>(values.length);
        for (final T v : values) {
            final MutableInt count = occurrences.get(v);
            if (count == null) {
                occurrences.put(v, new MutableInt(1));
            } else {
                count.increment();
            }
        }
        final BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; i++) {
            final T key = array[i];
            final MutableInt count = occurrences.get(key);
            if (count != null) {
                if (count.get() == 0) {
                    occurrences.remove(key);
                }
                toRemove.set(i);
            }
        }
        final T[] result = (T[]) removeAll(array, toRemove);
        return result;
    }

    /**
     * <p>Removes the elements at the specified positions from the specified array.
     * All remaining elements are shifted to the left.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except those at the specified positions. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.removeAll([1], 0)             = []
     * ArrayUtils.removeAll([2, 6], 0)          = [6]
     * ArrayUtils.removeAll([2, 6], 0, 1)       = []
     * ArrayUtils.removeAll([2, 6, 3], 1, 2)    = [2]
     * ArrayUtils.removeAll([2, 6, 3], 0, 2)    = [6]
     * ArrayUtils.removeAll([2, 6, 3], 0, 1, 2) = []
     * </pre>
     *
     * @param array   the array to remove the element from, may not be {@code null}
     * @param indices the positions of the elements to be removed
     * @return A new array containing the existing elements except those
     * at the specified positions.
     * @throws IndexOutOfBoundsException if any index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 3.0.1
     */
    public static byte[] removeAll(final byte[] array, final int... indices) {
        return (byte[]) removeAll((Object) array, indices);
    }

    /**
     * <p>Removes occurrences of specified elements, in specified quantities,
     * from the specified array. All subsequent elements are shifted left.
     * For any element-to-be-removed specified in greater quantities than
     * contained in the original array, no change occurs beyond the
     * removal of the existing matching items.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except for the earliest-encountered occurrences of the specified
     * elements. The component type of the returned array is always the same
     * as that of the input array.
     *
     * <pre>
     * ArrayUtils.removeElements(null, 1, 2)      = null
     * ArrayUtils.removeElements([], 1, 2)        = []
     * ArrayUtils.removeElements([1], 2, 3)       = [1]
     * ArrayUtils.removeElements([1, 3], 1, 2)    = [3]
     * ArrayUtils.removeElements([1, 3, 1], 1)    = [3, 1]
     * ArrayUtils.removeElements([1, 3, 1], 1, 1) = [3]
     * </pre>
     *
     * @param array  the array to remove the element from, may be {@code null}
     * @param values the elements to be removed
     * @return A new array containing the existing elements except the
     * earliest-encountered occurrences of the specified elements.
     * @since 3.0.1
     */
    public static byte[] removeElements(final byte[] array, final byte... values) {
        if (isEmpty(array) || isEmpty(values)) {
            return clone(array);
        }
        final Map<Byte, MutableInt> occurrences = new HashMap<>(values.length);
        for (final byte v : values) {
            final Byte boxed = Byte.valueOf(v);
            final MutableInt count = occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
            } else {
                count.increment();
            }
        }
        final BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; i++) {
            final byte key = array[i];
            final MutableInt count = occurrences.get(key);
            if (count != null) {
                if (count.get() == 0) {
                    occurrences.remove(key);
                }
                toRemove.set(i);
            }
        }
        return (byte[]) removeAll(array, toRemove);
    }

    /**
     * <p>Removes the elements at the specified positions from the specified array.
     * All remaining elements are shifted to the left.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except those at the specified positions. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.removeAll([1], 0)             = []
     * ArrayUtils.removeAll([2, 6], 0)          = [6]
     * ArrayUtils.removeAll([2, 6], 0, 1)       = []
     * ArrayUtils.removeAll([2, 6, 3], 1, 2)    = [2]
     * ArrayUtils.removeAll([2, 6, 3], 0, 2)    = [6]
     * ArrayUtils.removeAll([2, 6, 3], 0, 1, 2) = []
     * </pre>
     *
     * @param array   the array to remove the element from, may not be {@code null}
     * @param indices the positions of the elements to be removed
     * @return A new array containing the existing elements except those
     * at the specified positions.
     * @throws IndexOutOfBoundsException if any index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 3.0.1
     */
    public static short[] removeAll(final short[] array, final int... indices) {
        return (short[]) removeAll((Object) array, indices);
    }

    /**
     * <p>Removes occurrences of specified elements, in specified quantities,
     * from the specified array. All subsequent elements are shifted left.
     * For any element-to-be-removed specified in greater quantities than
     * contained in the original array, no change occurs beyond the
     * removal of the existing matching items.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except for the earliest-encountered occurrences of the specified
     * elements. The component type of the returned array is always the same
     * as that of the input array.
     *
     * <pre>
     * ArrayUtils.removeElements(null, 1, 2)      = null
     * ArrayUtils.removeElements([], 1, 2)        = []
     * ArrayUtils.removeElements([1], 2, 3)       = [1]
     * ArrayUtils.removeElements([1, 3], 1, 2)    = [3]
     * ArrayUtils.removeElements([1, 3, 1], 1)    = [3, 1]
     * ArrayUtils.removeElements([1, 3, 1], 1, 1) = [3]
     * </pre>
     *
     * @param array  the array to remove the element from, may be {@code null}
     * @param values the elements to be removed
     * @return A new array containing the existing elements except the
     * earliest-encountered occurrences of the specified elements.
     * @since 3.0.1
     */
    public static short[] removeElements(final short[] array, final short... values) {
        if (isEmpty(array) || isEmpty(values)) {
            return clone(array);
        }
        final HashMap<Short, MutableInt> occurrences = new HashMap<>(values.length);
        for (final short v : values) {
            final Short boxed = Short.valueOf(v);
            final MutableInt count = occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
            } else {
                count.increment();
            }
        }
        final BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; i++) {
            final short key = array[i];
            final MutableInt count = occurrences.get(key);
            if (count != null) {
                if (count.get() == 0) {
                    occurrences.remove(key);
                }
                toRemove.set(i);
            }
        }
        return (short[]) removeAll(array, toRemove);
    }

    /**
     * <p>Removes the elements at the specified positions from the specified array.
     * All remaining elements are shifted to the left.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except those at the specified positions. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.removeAll([1], 0)             = []
     * ArrayUtils.removeAll([2, 6], 0)          = [6]
     * ArrayUtils.removeAll([2, 6], 0, 1)       = []
     * ArrayUtils.removeAll([2, 6, 3], 1, 2)    = [2]
     * ArrayUtils.removeAll([2, 6, 3], 0, 2)    = [6]
     * ArrayUtils.removeAll([2, 6, 3], 0, 1, 2) = []
     * </pre>
     *
     * @param array   the array to remove the element from, may not be {@code null}
     * @param indices the positions of the elements to be removed
     * @return A new array containing the existing elements except those
     * at the specified positions.
     * @throws IndexOutOfBoundsException if any index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 3.0.1
     */
    public static int[] removeAll(final int[] array, final int... indices) {
        return (int[]) removeAll((Object) array, indices);
    }

    /**
     * <p>Removes occurrences of specified elements, in specified quantities,
     * from the specified array. All subsequent elements are shifted left.
     * For any element-to-be-removed specified in greater quantities than
     * contained in the original array, no change occurs beyond the
     * removal of the existing matching items.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except for the earliest-encountered occurrences of the specified
     * elements. The component type of the returned array is always the same
     * as that of the input array.
     *
     * <pre>
     * ArrayUtils.removeElements(null, 1, 2)      = null
     * ArrayUtils.removeElements([], 1, 2)        = []
     * ArrayUtils.removeElements([1], 2, 3)       = [1]
     * ArrayUtils.removeElements([1, 3], 1, 2)    = [3]
     * ArrayUtils.removeElements([1, 3, 1], 1)    = [3, 1]
     * ArrayUtils.removeElements([1, 3, 1], 1, 1) = [3]
     * </pre>
     *
     * @param array  the array to remove the element from, may be {@code null}
     * @param values the elements to be removed
     * @return A new array containing the existing elements except the
     * earliest-encountered occurrences of the specified elements.
     * @since 3.0.1
     */
    public static int[] removeElements(final int[] array, final int... values) {
        if (isEmpty(array) || isEmpty(values)) {
            return clone(array);
        }
        final HashMap<Integer, MutableInt> occurrences = new HashMap<>(values.length);
        for (final int v : values) {
            final Integer boxed = Integer.valueOf(v);
            final MutableInt count = occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
            } else {
                count.increment();
            }
        }
        final BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; i++) {
            final int key = array[i];
            final MutableInt count = occurrences.get(key);
            if (count != null) {
                if (count.get() == 0) {
                    occurrences.remove(key);
                }
                toRemove.set(i);
            }
        }
        return (int[]) removeAll(array, toRemove);
    }

    /**
     * <p>Removes the elements at the specified positions from the specified array.
     * All remaining elements are shifted to the left.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except those at the specified positions. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.removeAll([1], 0)             = []
     * ArrayUtils.removeAll([2, 6], 0)          = [6]
     * ArrayUtils.removeAll([2, 6], 0, 1)       = []
     * ArrayUtils.removeAll([2, 6, 3], 1, 2)    = [2]
     * ArrayUtils.removeAll([2, 6, 3], 0, 2)    = [6]
     * ArrayUtils.removeAll([2, 6, 3], 0, 1, 2) = []
     * </pre>
     *
     * @param array   the array to remove the element from, may not be {@code null}
     * @param indices the positions of the elements to be removed
     * @return A new array containing the existing elements except those
     * at the specified positions.
     * @throws IndexOutOfBoundsException if any index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 3.0.1
     */
    public static char[] removeAll(final char[] array, final int... indices) {
        return (char[]) removeAll((Object) array, indices);
    }

    /**
     * <p>Removes occurrences of specified elements, in specified quantities,
     * from the specified array. All subsequent elements are shifted left.
     * For any element-to-be-removed specified in greater quantities than
     * contained in the original array, no change occurs beyond the
     * removal of the existing matching items.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except for the earliest-encountered occurrences of the specified
     * elements. The component type of the returned array is always the same
     * as that of the input array.
     *
     * <pre>
     * ArrayUtils.removeElements(null, 1, 2)      = null
     * ArrayUtils.removeElements([], 1, 2)        = []
     * ArrayUtils.removeElements([1], 2, 3)       = [1]
     * ArrayUtils.removeElements([1, 3], 1, 2)    = [3]
     * ArrayUtils.removeElements([1, 3, 1], 1)    = [3, 1]
     * ArrayUtils.removeElements([1, 3, 1], 1, 1) = [3]
     * </pre>
     *
     * @param array  the array to remove the element from, may be {@code null}
     * @param values the elements to be removed
     * @return A new array containing the existing elements except the
     * earliest-encountered occurrences of the specified elements.
     * @since 3.0.1
     */
    public static char[] removeElements(final char[] array, final char... values) {
        if (isEmpty(array) || isEmpty(values)) {
            return clone(array);
        }
        final HashMap<Character, MutableInt> occurrences = new HashMap<>(values.length);
        for (final char v : values) {
            final Character boxed = Character.valueOf(v);
            final MutableInt count = occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
            } else {
                count.increment();
            }
        }
        final BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; i++) {
            final char key = array[i];
            final MutableInt count = occurrences.get(key);
            if (count != null) {
                if (count.get() == 0) {
                    occurrences.remove(key);
                }
                toRemove.set(i);
            }
        }
        return (char[]) removeAll(array, toRemove);
    }

    /**
     * <p>Removes the elements at the specified positions from the specified array.
     * All remaining elements are shifted to the left.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except those at the specified positions. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.removeAll([1], 0)             = []
     * ArrayUtils.removeAll([2, 6], 0)          = [6]
     * ArrayUtils.removeAll([2, 6], 0, 1)       = []
     * ArrayUtils.removeAll([2, 6, 3], 1, 2)    = [2]
     * ArrayUtils.removeAll([2, 6, 3], 0, 2)    = [6]
     * ArrayUtils.removeAll([2, 6, 3], 0, 1, 2) = []
     * </pre>
     *
     * @param array   the array to remove the element from, may not be {@code null}
     * @param indices the positions of the elements to be removed
     * @return A new array containing the existing elements except those
     * at the specified positions.
     * @throws IndexOutOfBoundsException if any index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 3.0.1
     */
    public static long[] removeAll(final long[] array, final int... indices) {
        return (long[]) removeAll((Object) array, indices);
    }

    /**
     * <p>Removes occurrences of specified elements, in specified quantities,
     * from the specified array. All subsequent elements are shifted left.
     * For any element-to-be-removed specified in greater quantities than
     * contained in the original array, no change occurs beyond the
     * removal of the existing matching items.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except for the earliest-encountered occurrences of the specified
     * elements. The component type of the returned array is always the same
     * as that of the input array.
     *
     * <pre>
     * ArrayUtils.removeElements(null, 1, 2)      = null
     * ArrayUtils.removeElements([], 1, 2)        = []
     * ArrayUtils.removeElements([1], 2, 3)       = [1]
     * ArrayUtils.removeElements([1, 3], 1, 2)    = [3]
     * ArrayUtils.removeElements([1, 3, 1], 1)    = [3, 1]
     * ArrayUtils.removeElements([1, 3, 1], 1, 1) = [3]
     * </pre>
     *
     * @param array  the array to remove the element from, may be {@code null}
     * @param values the elements to be removed
     * @return A new array containing the existing elements except the
     * earliest-encountered occurrences of the specified elements.
     * @since 3.0.1
     */
    public static long[] removeElements(final long[] array, final long... values) {
        if (isEmpty(array) || isEmpty(values)) {
            return clone(array);
        }
        final HashMap<Long, MutableInt> occurrences = new HashMap<>(values.length);
        for (final long v : values) {
            final Long boxed = Long.valueOf(v);
            final MutableInt count = occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
            } else {
                count.increment();
            }
        }
        final BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; i++) {
            final long key = array[i];
            final MutableInt count = occurrences.get(key);
            if (count != null) {
                if (count.get() == 0) {
                    occurrences.remove(key);
                }
                toRemove.set(i);
            }
        }
        return (long[]) removeAll(array, toRemove);
    }

    /**
     * <p>Removes the elements at the specified positions from the specified array.
     * All remaining elements are shifted to the left.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except those at the specified positions. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.removeAll([1], 0)             = []
     * ArrayUtils.removeAll([2, 6], 0)          = [6]
     * ArrayUtils.removeAll([2, 6], 0, 1)       = []
     * ArrayUtils.removeAll([2, 6, 3], 1, 2)    = [2]
     * ArrayUtils.removeAll([2, 6, 3], 0, 2)    = [6]
     * ArrayUtils.removeAll([2, 6, 3], 0, 1, 2) = []
     * </pre>
     *
     * @param array   the array to remove the element from, may not be {@code null}
     * @param indices the positions of the elements to be removed
     * @return A new array containing the existing elements except those
     * at the specified positions.
     * @throws IndexOutOfBoundsException if any index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 3.0.1
     */
    public static float[] removeAll(final float[] array, final int... indices) {
        return (float[]) removeAll((Object) array, indices);
    }

    /**
     * <p>Removes occurrences of specified elements, in specified quantities,
     * from the specified array. All subsequent elements are shifted left.
     * For any element-to-be-removed specified in greater quantities than
     * contained in the original array, no change occurs beyond the
     * removal of the existing matching items.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except for the earliest-encountered occurrences of the specified
     * elements. The component type of the returned array is always the same
     * as that of the input array.
     *
     * <pre>
     * ArrayUtils.removeElements(null, 1, 2)      = null
     * ArrayUtils.removeElements([], 1, 2)        = []
     * ArrayUtils.removeElements([1], 2, 3)       = [1]
     * ArrayUtils.removeElements([1, 3], 1, 2)    = [3]
     * ArrayUtils.removeElements([1, 3, 1], 1)    = [3, 1]
     * ArrayUtils.removeElements([1, 3, 1], 1, 1) = [3]
     * </pre>
     *
     * @param array  the array to remove the element from, may be {@code null}
     * @param values the elements to be removed
     * @return A new array containing the existing elements except the
     * earliest-encountered occurrences of the specified elements.
     * @since 3.0.1
     */
    public static float[] removeElements(final float[] array, final float... values) {
        if (isEmpty(array) || isEmpty(values)) {
            return clone(array);
        }
        final HashMap<Float, MutableInt> occurrences = new HashMap<>(values.length);
        for (final float v : values) {
            final Float boxed = Float.valueOf(v);
            final MutableInt count = occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
            } else {
                count.increment();
            }
        }
        final BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; i++) {
            final float key = array[i];
            final MutableInt count = occurrences.get(key);
            if (count != null) {
                if (count.get() == 0) {
                    occurrences.remove(key);
                }
                toRemove.set(i);
            }
        }
        return (float[]) removeAll(array, toRemove);
    }

    /**
     * <p>Removes the elements at the specified positions from the specified array.
     * All remaining elements are shifted to the left.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except those at the specified positions. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.removeAll([1], 0)             = []
     * ArrayUtils.removeAll([2, 6], 0)          = [6]
     * ArrayUtils.removeAll([2, 6], 0, 1)       = []
     * ArrayUtils.removeAll([2, 6, 3], 1, 2)    = [2]
     * ArrayUtils.removeAll([2, 6, 3], 0, 2)    = [6]
     * ArrayUtils.removeAll([2, 6, 3], 0, 1, 2) = []
     * </pre>
     *
     * @param array   the array to remove the element from, may not be {@code null}
     * @param indices the positions of the elements to be removed
     * @return A new array containing the existing elements except those
     * at the specified positions.
     * @throws IndexOutOfBoundsException if any index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 3.0.1
     */
    public static double[] removeAll(final double[] array, final int... indices) {
        return (double[]) removeAll((Object) array, indices);
    }

    /**
     * <p>Removes occurrences of specified elements, in specified quantities,
     * from the specified array. All subsequent elements are shifted left.
     * For any element-to-be-removed specified in greater quantities than
     * contained in the original array, no change occurs beyond the
     * removal of the existing matching items.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except for the earliest-encountered occurrences of the specified
     * elements. The component type of the returned array is always the same
     * as that of the input array.
     *
     * <pre>
     * ArrayUtils.removeElements(null, 1, 2)      = null
     * ArrayUtils.removeElements([], 1, 2)        = []
     * ArrayUtils.removeElements([1], 2, 3)       = [1]
     * ArrayUtils.removeElements([1, 3], 1, 2)    = [3]
     * ArrayUtils.removeElements([1, 3, 1], 1)    = [3, 1]
     * ArrayUtils.removeElements([1, 3, 1], 1, 1) = [3]
     * </pre>
     *
     * @param array  the array to remove the element from, may be {@code null}
     * @param values the elements to be removed
     * @return A new array containing the existing elements except the
     * earliest-encountered occurrences of the specified elements.
     * @since 3.0.1
     */
    public static double[] removeElements(final double[] array, final double... values) {
        if (isEmpty(array) || isEmpty(values)) {
            return clone(array);
        }
        final HashMap<Double, MutableInt> occurrences = new HashMap<>(values.length);
        for (final double v : values) {
            final Double boxed = Double.valueOf(v);
            final MutableInt count = occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
            } else {
                count.increment();
            }
        }
        final BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; i++) {
            final double key = array[i];
            final MutableInt count = occurrences.get(key);
            if (count != null) {
                if (count.get() == 0) {
                    occurrences.remove(key);
                }
                toRemove.set(i);
            }
        }
        return (double[]) removeAll(array, toRemove);
    }

    /**
     * <p>Removes the elements at the specified positions from the specified array.
     * All remaining elements are shifted to the left.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except those at the specified positions. The component
     * type of the returned array is always the same as that of the input
     * array.
     *
     * <p>If the input array is {@code null}, an IndexOutOfBoundsException
     * will be thrown, because in that case no valid index can be specified.
     *
     * <pre>
     * ArrayUtils.removeAll([true, false, true], 0, 2) = [false]
     * ArrayUtils.removeAll([true, false, true], 1, 2) = [true]
     * </pre>
     *
     * @param array   the array to remove the element from, may not be {@code null}
     * @param indices the positions of the elements to be removed
     * @return A new array containing the existing elements except those
     * at the specified positions.
     * @throws IndexOutOfBoundsException if any index is out of range
     *                                   (index &lt; 0 || index &gt;= array.length), or if the array is {@code null}.
     * @since 3.0.1
     */
    public static boolean[] removeAll(final boolean[] array, final int... indices) {
        return (boolean[]) removeAll((Object) array, indices);
    }

    /**
     * <p>Removes occurrences of specified elements, in specified quantities,
     * from the specified array. All subsequent elements are shifted left.
     * For any element-to-be-removed specified in greater quantities than
     * contained in the original array, no change occurs beyond the
     * removal of the existing matching items.
     *
     * <p>This method returns a new array with the same elements of the input
     * array except for the earliest-encountered occurrences of the specified
     * elements. The component type of the returned array is always the same
     * as that of the input array.
     *
     * <pre>
     * ArrayUtils.removeElements(null, true, false)               = null
     * ArrayUtils.removeElements([], true, false)                 = []
     * ArrayUtils.removeElements([true], false, false)            = [true]
     * ArrayUtils.removeElements([true, false], true, true)       = [false]
     * ArrayUtils.removeElements([true, false, true], true)       = [false, true]
     * ArrayUtils.removeElements([true, false, true], true, true) = [false]
     * </pre>
     *
     * @param array  the array to remove the element from, may be {@code null}
     * @param values the elements to be removed
     * @return A new array containing the existing elements except the
     * earliest-encountered occurrences of the specified elements.
     * @since 3.0.1
     */
    public static boolean[] removeElements(final boolean[] array, final boolean... values) {
        if (isEmpty(array) || isEmpty(values)) {
            return clone(array);
        }
        final HashMap<Boolean, MutableInt> occurrences = new HashMap<>(2); // only two possible values here
        for (final boolean v : values) {
            final Boolean boxed = Boolean.valueOf(v);
            final MutableInt count = occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
            } else {
                count.increment();
            }
        }
        final BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; i++) {
            final boolean key = array[i];
            final MutableInt count = occurrences.get(key);
            if (count != null) {
                if (count.get() == 0) {
                    occurrences.remove(key);
                }
                toRemove.set(i);
            }
        }
        return (boolean[]) removeAll(array, toRemove);
    }

    /**
     * Removes multiple array elements specified by index.
     *
     * @param array   source
     * @param indices to remove
     * @return new array of same type minus elements specified by unique values of {@code indices}
     * @since 3.0.1
     */
    // package protected for access by unit tests
    static Object removeAll(final Object array, final int... indices) {
        final int length = getLength(array);
        int diff = 0; // number of distinct indexes, i.e. number of entries that will be removed
        final int[] clonedIndices = clone(indices);
        Arrays.sort(clonedIndices);

        // identify length of result array
        if (isNotEmpty(clonedIndices)) {
            int i = clonedIndices.length;
            int prevIndex = length;
            while (--i >= 0) {
                final int index = clonedIndices[i];
                if (index < 0 || index >= length) {
                    throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
                }
                if (index >= prevIndex) {
                    continue;
                }
                diff++;
                prevIndex = index;
            }
        }

        // create result array
        final Object result = Array.newInstance(array.getClass().getComponentType(), length - diff);
        if (diff < length) {
            int end = length; // index just after last copy
            int dest = length - diff; // number of entries so far not copied
            for (int i = clonedIndices.length - 1; i >= 0; i--) {
                final int index = clonedIndices[i];
                if (end - index > 1) { // same as (cp > 0)
                    final int cp = end - index - 1;
                    dest -= cp;
                    System.arraycopy(array, index + 1, result, dest, cp);
                    // Afer this copy, we still have room for dest items.
                }
                end = index;
            }
            if (end > 0) {
                System.arraycopy(array, 0, result, 0, end);
            }
        }
        return result;
    }

    /**
     * Removes multiple array elements specified by indices.
     *
     * @param array   source
     * @param indices to remove
     * @return new array of same type minus elements specified by the set bits in {@code indices}
     * @since 3.2.0
     */
    static Object removeAll(final Object array, final BitSet indices) {
        final int srcLength = getLength(array);
        final int removals = indices.cardinality(); // true bits are items to remove
        final Object result = Array.newInstance(array.getClass().getComponentType(), srcLength - removals);
        int srcIndex = 0;
        int destIndex = 0;
        int count;
        int set;
        while ((set = indices.nextSetBit(srcIndex)) != -1) {
            count = set - srcIndex;
            if (count > 0) {
                System.arraycopy(array, srcIndex, result, destIndex, count);
                destIndex += count;
            }
            srcIndex = indices.nextClearBit(set);
        }
        count = srcLength - srcIndex;
        if (count > 0) {
            System.arraycopy(array, srcIndex, result, destIndex, count);
        }
        return result;
    }

    /**
     * <p>This method checks whether the provided array is sorted according to the class's
     * {@code compareTo} method.
     *
     * @param array the array to check
     * @param <T>   the datatype of the array to check, it must implement {@code Comparable}
     * @return whether the array is sorted
     * @since 3.5.0
     */
    public static <T extends Comparable<? super T>> boolean isSorted(final T[] array) {
        return isSorted(array, new Comparator<T>() {
            @Override
            public int compare(final T o1, final T o2) {
                return o1.compareTo(o2);
            }
        });
    }


    /**
     * <p>This method checks whether the provided array is sorted according to the provided {@code Comparator}.
     *
     * @param array      the array to check
     * @param comparator the {@code Comparator} to compare over
     * @param <T>        the datatype of the array
     * @return whether the array is sorted
     * @since 3.5.0
     */
    public static <T> boolean isSorted(final T[] array, final Comparator<T> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator should not be null.");
        }

        if (array == null || array.length < 2) {
            return true;
        }

        T previous = array[0];
        final int n = array.length;
        for (int i = 1; i < n; i++) {
            final T current = array[i];
            if (comparator.compare(previous, current) > 0) {
                return false;
            }

            previous = current;
        }
        return true;
    }

    /**
     * <p>This method checks whether the provided array is sorted according to natural ordering.
     *
     * @param array the array to check
     * @return whether the array is sorted according to natural ordering
     * @since 3.5.0
     */
    public static boolean isSorted(final int[] array) {
        if (array == null || array.length < 2) {
            return true;
        }

        int previous = array[0];
        final int n = array.length;
        for (int i = 1; i < n; i++) {
            final int current = array[i];
            if (NumberUtils.compare(previous, current) > 0) {
                return false;
            }

            previous = current;
        }
        return true;
    }

    /**
     * <p>This method checks whether the provided array is sorted according to natural ordering.
     *
     * @param array the array to check
     * @return whether the array is sorted according to natural ordering
     * @since 3.5.0
     */
    public static boolean isSorted(final long[] array) {
        if (array == null || array.length < 2) {
            return true;
        }

        long previous = array[0];
        final int n = array.length;
        for (int i = 1; i < n; i++) {
            final long current = array[i];
            if (NumberUtils.compare(previous, current) > 0) {
                return false;
            }

            previous = current;
        }
        return true;
    }

    /**
     * <p>This method checks whether the provided array is sorted according to natural ordering.
     *
     * @param array the array to check
     * @return whether the array is sorted according to natural ordering
     * @since 3.5.0
     */
    public static boolean isSorted(final short[] array) {
        if (array == null || array.length < 2) {
            return true;
        }

        short previous = array[0];
        final int n = array.length;
        for (int i = 1; i < n; i++) {
            final short current = array[i];
            if (NumberUtils.compare(previous, current) > 0) {
                return false;
            }

            previous = current;
        }
        return true;
    }

    /**
     * <p>This method checks whether the provided array is sorted according to natural ordering.
     *
     * @param array the array to check
     * @return whether the array is sorted according to natural ordering
     * @since 3.5.0
     */
    public static boolean isSorted(final double[] array) {
        if (array == null || array.length < 2) {
            return true;
        }

        double previous = array[0];
        final int n = array.length;
        for (int i = 1; i < n; i++) {
            final double current = array[i];
            if (Double.compare(previous, current) > 0) {
                return false;
            }

            previous = current;
        }
        return true;
    }

    /**
     * <p>This method checks whether the provided array is sorted according to natural ordering.
     *
     * @param array the array to check
     * @return whether the array is sorted according to natural ordering
     * @since 3.5.0
     */
    public static boolean isSorted(final float[] array) {
        if (array == null || array.length < 2) {
            return true;
        }

        float previous = array[0];
        final int n = array.length;
        for (int i = 1; i < n; i++) {
            final float current = array[i];
            if (Float.compare(previous, current) > 0) {
                return false;
            }

            previous = current;
        }
        return true;
    }

    /**
     * <p>This method checks whether the provided array is sorted according to natural ordering.
     *
     * @param array the array to check
     * @return whether the array is sorted according to natural ordering
     * @since 3.5.0
     */
    public static boolean isSorted(final byte[] array) {
        if (array == null || array.length < 2) {
            return true;
        }

        byte previous = array[0];
        final int n = array.length;
        for (int i = 1; i < n; i++) {
            final byte current = array[i];
            if (NumberUtils.compare(previous, current) > 0) {
                return false;
            }

            previous = current;
        }
        return true;
    }

    /**
     * <p>This method checks whether the provided array is sorted according to natural ordering.
     *
     * @param array the array to check
     * @return whether the array is sorted according to natural ordering
     * @since 3.5.0
     */
    public static boolean isSorted(final char[] array) {
        if (array == null || array.length < 2) {
            return true;
        }

        char previous = array[0];
        final int n = array.length;
        for (int i = 1; i < n; i++) {
            final char current = array[i];
            if (CharUtils.compare(previous, current) > 0) {
                return false;
            }

            previous = current;
        }
        return true;
    }

    /**
     * <p>This method checks whether the provided array is sorted according to natural ordering
     * ({@code false} before {@code true}).
     *
     * @param array the array to check
     * @return whether the array is sorted according to natural ordering
     * @since 3.5.0
     */
    public static boolean isSorted(final boolean[] array) {
        if (array == null || array.length < 2) {
            return true;
        }

        boolean previous = array[0];
        final int n = array.length;
        for (int i = 1; i < n; i++) {
            final boolean current = array[i];
            if (BooleanUtils.compare(previous, current) > 0) {
                return false;
            }

            previous = current;
        }
        return true;
    }

    /**
     * Removes the occurrences of the specified element from the specified boolean array.
     *
     * <p>
     * All subsequent elements are shifted to the left (subtracts one from their indices).
     * If the array doesn't contains such an element, no elements are removed from the array.
     * <code>null</code> will be returned if the input array is <code>null</code>.
     * </p>
     *
     * @param element the element to remove
     * @param array   the input array
     * @return A new array containing the existing elements except the occurrences of the specified element.
     * @since 3.5.0
     */
    public static boolean[] removeAllOccurences(final boolean[] array, final boolean element) {
        int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }

        final int[] indices = new int[array.length - index];
        indices[0] = index;
        int count = 1;

        while ((index = indexOf(array, element, indices[count - 1] + 1)) != INDEX_NOT_FOUND) {
            indices[count++] = index;
        }

        return removeAll(array, Arrays.copyOf(indices, count));
    }

    /**
     * Removes the occurrences of the specified element from the specified char array.
     *
     * <p>
     * All subsequent elements are shifted to the left (subtracts one from their indices).
     * If the array doesn't contains such an element, no elements are removed from the array.
     * <code>null</code> will be returned if the input array is <code>null</code>.
     * </p>
     *
     * @param element the element to remove
     * @param array   the input array
     * @return A new array containing the existing elements except the occurrences of the specified element.
     * @since 3.5.0
     */
    public static char[] removeAllOccurences(final char[] array, final char element) {
        int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }

        final int[] indices = new int[array.length - index];
        indices[0] = index;
        int count = 1;

        while ((index = indexOf(array, element, indices[count - 1] + 1)) != INDEX_NOT_FOUND) {
            indices[count++] = index;
        }

        return removeAll(array, Arrays.copyOf(indices, count));
    }

    /**
     * Removes the occurrences of the specified element from the specified byte array.
     *
     * <p>
     * All subsequent elements are shifted to the left (subtracts one from their indices).
     * If the array doesn't contains such an element, no elements are removed from the array.
     * <code>null</code> will be returned if the input array is <code>null</code>.
     * </p>
     *
     * @param element the element to remove
     * @param array   the input array
     * @return A new array containing the existing elements except the occurrences of the specified element.
     * @since 3.5.0
     */
    public static byte[] removeAllOccurences(final byte[] array, final byte element) {
        int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }

        final int[] indices = new int[array.length - index];
        indices[0] = index;
        int count = 1;

        while ((index = indexOf(array, element, indices[count - 1] + 1)) != INDEX_NOT_FOUND) {
            indices[count++] = index;
        }

        return removeAll(array, Arrays.copyOf(indices, count));
    }

    /**
     * Removes the occurrences of the specified element from the specified short array.
     *
     * <p>
     * All subsequent elements are shifted to the left (subtracts one from their indices).
     * If the array doesn't contains such an element, no elements are removed from the array.
     * <code>null</code> will be returned if the input array is <code>null</code>.
     * </p>
     *
     * @param element the element to remove
     * @param array   the input array
     * @return A new array containing the existing elements except the occurrences of the specified element.
     * @since 3.5.0
     */
    public static short[] removeAllOccurences(final short[] array, final short element) {
        int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }

        final int[] indices = new int[array.length - index];
        indices[0] = index;
        int count = 1;

        while ((index = indexOf(array, element, indices[count - 1] + 1)) != INDEX_NOT_FOUND) {
            indices[count++] = index;
        }

        return removeAll(array, Arrays.copyOf(indices, count));
    }

    /**
     * Removes the occurrences of the specified element from the specified int array.
     *
     * <p>
     * All subsequent elements are shifted to the left (subtracts one from their indices).
     * If the array doesn't contains such an element, no elements are removed from the array.
     * <code>null</code> will be returned if the input array is <code>null</code>.
     * </p>
     *
     * @param element the element to remove
     * @param array   the input array
     * @return A new array containing the existing elements except the occurrences of the specified element.
     * @since 3.5.0
     */
    public static int[] removeAllOccurences(final int[] array, final int element) {
        int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }

        final int[] indices = new int[array.length - index];
        indices[0] = index;
        int count = 1;

        while ((index = indexOf(array, element, indices[count - 1] + 1)) != INDEX_NOT_FOUND) {
            indices[count++] = index;
        }

        return removeAll(array, Arrays.copyOf(indices, count));
    }

    /**
     * Removes the occurrences of the specified element from the specified long array.
     *
     * <p>
     * All subsequent elements are shifted to the left (subtracts one from their indices).
     * If the array doesn't contains such an element, no elements are removed from the array.
     * <code>null</code> will be returned if the input array is <code>null</code>.
     * </p>
     *
     * @param element the element to remove
     * @param array   the input array
     * @return A new array containing the existing elements except the occurrences of the specified element.
     * @since 3.5.0
     */
    public static long[] removeAllOccurences(final long[] array, final long element) {
        int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }

        final int[] indices = new int[array.length - index];
        indices[0] = index;
        int count = 1;

        while ((index = indexOf(array, element, indices[count - 1] + 1)) != INDEX_NOT_FOUND) {
            indices[count++] = index;
        }

        return removeAll(array, Arrays.copyOf(indices, count));
    }

    /**
     * Removes the occurrences of the specified element from the specified float array.
     *
     * <p>
     * All subsequent elements are shifted to the left (subtracts one from their indices).
     * If the array doesn't contains such an element, no elements are removed from the array.
     * <code>null</code> will be returned if the input array is <code>null</code>.
     * </p>
     *
     * @param element the element to remove
     * @param array   the input array
     * @return A new array containing the existing elements except the occurrences of the specified element.
     * @since 3.5.0
     */
    public static float[] removeAllOccurences(final float[] array, final float element) {
        int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }

        final int[] indices = new int[array.length - index];
        indices[0] = index;
        int count = 1;

        while ((index = indexOf(array, element, indices[count - 1] + 1)) != INDEX_NOT_FOUND) {
            indices[count++] = index;
        }

        return removeAll(array, Arrays.copyOf(indices, count));
    }

    /**
     * Removes the occurrences of the specified element from the specified double array.
     *
     * <p>
     * All subsequent elements are shifted to the left (subtracts one from their indices).
     * If the array doesn't contains such an element, no elements are removed from the array.
     * <code>null</code> will be returned if the input array is <code>null</code>.
     * </p>
     *
     * @param element the element to remove
     * @param array   the input array
     * @return A new array containing the existing elements except the occurrences of the specified element.
     * @since 3.5.0
     */
    public static double[] removeAllOccurences(final double[] array, final double element) {
        int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }

        final int[] indices = new int[array.length - index];
        indices[0] = index;
        int count = 1;

        while ((index = indexOf(array, element, indices[count - 1] + 1)) != INDEX_NOT_FOUND) {
            indices[count++] = index;
        }

        return removeAll(array, Arrays.copyOf(indices, count));
    }

    /**
     * Removes the occurrences of the specified element from the specified array.
     *
     * <p>
     * All subsequent elements are shifted to the left (subtracts one from their indices).
     * If the array doesn't contains such an element, no elements are removed from the array.
     * <code>null</code> will be returned if the input array is <code>null</code>.
     * </p>
     *
     * @param <T>     the type of object in the array
     * @param element the element to remove
     * @param array   the input array
     * @return A new array containing the existing elements except the occurrences of the specified element.
     * @since 3.5.0
     */
    public static <T> T[] removeAllOccurences(final T[] array, final T element) {
        int index = indexOf(array, element);
        if (index == INDEX_NOT_FOUND) {
            return clone(array);
        }

        final int[] indices = new int[array.length - index];
        indices[0] = index;
        int count = 1;

        while ((index = indexOf(array, element, indices[count - 1] + 1)) != INDEX_NOT_FOUND) {
            indices[count++] = index;
        }

        return removeAll(array, Arrays.copyOf(indices, count));
    }

    /**
     * <p>Returns an array containing the string representation of each element in the argument array.</p>
     *
     * <p>This method returns {@code null} for a {@code null} input array.</p>
     *
     * @param array the {@code Object[]} to be processed, may be null
     * @return {@code String[]} of the same size as the source with its element's string representation,
     * {@code null} if null array input
     * @throws NullPointerException if array contains {@code null}
     */
    public static String[] toStringArray(final Object[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_STRING_ARRAY;
        }

        final String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].toString();
        }

        return result;
    }

    /**
     * <p>Returns an array containing the string representation of each element in the argument
     * array handling {@code null} elements.</p>
     *
     * <p>This method returns {@code null} for a {@code null} input array.</p>
     *
     * @param array                the Object[] to be processed, may be null
     * @param valueForNullElements the value to insert if {@code null} is found
     * @return a {@code String} array, {@code null} if null array input
     */
    public static String[] toStringArray(final Object[] array, final String valueForNullElements) {
        if (null == array) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_STRING_ARRAY;
        }

        final String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            final Object object = array[i];
            result[i] = (object == null ? valueForNullElements : object.toString());
        }

        return result;
    }

    /**
     * 将新元素插入到到已有数组中的某个位置
     * 添加新元素会生成一个新的数组,不影响原数组
     * 如果插入位置为为负数,从原数组从后向前计数,若大于原数组长度,则空白处用null填充
     *
     * @param <T>         数组元素类型
     * @param array       已有数组
     * @param index       插入位置,此位置为对应此位置元素之前的空档
     * @param newElements 新元素
     * @return 新数组
     */

    public static <T> Object insert(Object array, int index, T... newElements) {
        if (isEmpty(newElements)) {
            return array;
        }
        if (isEmpty(array)) {
            return newElements;
        }

        final int len = length(array);
        if (index < 0) {
            index = (index % len) + len;
        }

        final T[] result = newArray(array.getClass().getComponentType(), Math.max(len, index) + newElements.length);
        System.arraycopy(array, 0, result, 0, Math.min(len, index));
        System.arraycopy(newElements, 0, result, index, newElements.length);
        if (index < len) {
            System.arraycopy(array, index, result, index + newElements.length, len - index);
        }
        return result;
    }

    /**
     * <p>Inserts elements into an array at the given index (starting from zero).</p>
     *
     * <p>When an array is returned, it is always a new array.</p>
     *
     * <pre>
     * ArrayUtils.insert(index, null, null)      = null
     * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
     * ArrayUtils.insert(index, null, values)    = null
     * </pre>
     *
     * @param index  the position within {@code array} to insert the new values
     * @param array  the array to insert the values into, may be {@code null}
     * @param values the new values to insert, may be {@code null}
     * @return The new array.
     * @throws IndexOutOfBoundsException if {@code array} is provided
     *                                   and either {@code index < 0} or {@code index > array.length}
     */
    public static boolean[] insert(final int index, final boolean[] array, final boolean... values) {
        if (array == null) {
            return null;
        }
        if (values == null || values.length == 0) {
            return clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }

        final boolean[] result = new boolean[array.length + values.length];

        System.arraycopy(values, 0, result, index, values.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
        }
        return result;
    }

    /**
     * <p>Inserts elements into an array at the given index (starting from zero).</p>
     *
     * <p>When an array is returned, it is always a new array.</p>
     *
     * <pre>
     * ArrayUtils.insert(index, null, null)      = null
     * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
     * ArrayUtils.insert(index, null, values)    = null
     * </pre>
     *
     * @param index  the position within {@code array} to insert the new values
     * @param array  the array to insert the values into, may be {@code null}
     * @param values the new values to insert, may be {@code null}
     * @return The new array.
     * @throws IndexOutOfBoundsException if {@code array} is provided
     *                                   and either {@code index < 0} or {@code index > array.length}
     */
    public static byte[] insert(final int index, final byte[] array, final byte... values) {
        if (array == null) {
            return null;
        }
        if (values == null || values.length == 0) {
            return clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }

        final byte[] result = new byte[array.length + values.length];

        System.arraycopy(values, 0, result, index, values.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
        }
        return result;
    }

    /**
     * <p>Inserts elements into an array at the given index (starting from zero).</p>
     *
     * <p>When an array is returned, it is always a new array.</p>
     *
     * <pre>
     * ArrayUtils.insert(index, null, null)      = null
     * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
     * ArrayUtils.insert(index, null, values)    = null
     * </pre>
     *
     * @param index  the position within {@code array} to insert the new values
     * @param array  the array to insert the values into, may be {@code null}
     * @param values the new values to insert, may be {@code null}
     * @return The new array.
     * @throws IndexOutOfBoundsException if {@code array} is provided
     *                                   and either {@code index < 0} or {@code index > array.length}
     */
    public static char[] insert(final int index, final char[] array, final char... values) {
        if (array == null) {
            return null;
        }
        if (values == null || values.length == 0) {
            return clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }

        final char[] result = new char[array.length + values.length];

        System.arraycopy(values, 0, result, index, values.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
        }
        return result;
    }

    /**
     * <p>Inserts elements into an array at the given index (starting from zero).</p>
     *
     * <p>When an array is returned, it is always a new array.</p>
     *
     * <pre>
     * ArrayUtils.insert(index, null, null)      = null
     * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
     * ArrayUtils.insert(index, null, values)    = null
     * </pre>
     *
     * @param index  the position within {@code array} to insert the new values
     * @param array  the array to insert the values into, may be {@code null}
     * @param values the new values to insert, may be {@code null}
     * @return The new array.
     * @throws IndexOutOfBoundsException if {@code array} is provided
     *                                   and either {@code index < 0} or {@code index > array.length}
     */
    public static double[] insert(final int index, final double[] array, final double... values) {
        if (array == null) {
            return null;
        }
        if (values == null || values.length == 0) {
            return clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }

        final double[] result = new double[array.length + values.length];

        System.arraycopy(values, 0, result, index, values.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
        }
        return result;
    }

    /**
     * <p>Inserts elements into an array at the given index (starting from zero).</p>
     *
     * <p>When an array is returned, it is always a new array.</p>
     *
     * <pre>
     * ArrayUtils.insert(index, null, null)      = null
     * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
     * ArrayUtils.insert(index, null, values)    = null
     * </pre>
     *
     * @param index  the position within {@code array} to insert the new values
     * @param array  the array to insert the values into, may be {@code null}
     * @param values the new values to insert, may be {@code null}
     * @return The new array.
     * @throws IndexOutOfBoundsException if {@code array} is provided
     *                                   and either {@code index < 0} or {@code index > array.length}
     */
    public static float[] insert(final int index, final float[] array, final float... values) {
        if (array == null) {
            return null;
        }
        if (values == null || values.length == 0) {
            return clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }

        final float[] result = new float[array.length + values.length];

        System.arraycopy(values, 0, result, index, values.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
        }
        return result;
    }

    /**
     * <p>Inserts elements into an array at the given index (starting from zero).</p>
     *
     * <p>When an array is returned, it is always a new array.</p>
     *
     * <pre>
     * ArrayUtils.insert(index, null, null)      = null
     * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
     * ArrayUtils.insert(index, null, values)    = null
     * </pre>
     *
     * @param index  the position within {@code array} to insert the new values
     * @param array  the array to insert the values into, may be {@code null}
     * @param values the new values to insert, may be {@code null}
     * @return The new array.
     * @throws IndexOutOfBoundsException if {@code array} is provided
     *                                   and either {@code index < 0} or {@code index > array.length}
     */
    public static int[] insert(final int index, final int[] array, final int... values) {
        if (array == null) {
            return null;
        }
        if (values == null || values.length == 0) {
            return clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }

        final int[] result = new int[array.length + values.length];

        System.arraycopy(values, 0, result, index, values.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
        }
        return result;
    }

    /**
     * <p>Inserts elements into an array at the given index (starting from zero).</p>
     *
     * <p>When an array is returned, it is always a new array.</p>
     *
     * <pre>
     * ArrayUtils.insert(index, null, null)      = null
     * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
     * ArrayUtils.insert(index, null, values)    = null
     * </pre>
     *
     * @param index  the position within {@code array} to insert the new values
     * @param array  the array to insert the values into, may be {@code null}
     * @param values the new values to insert, may be {@code null}
     * @return The new array.
     * @throws IndexOutOfBoundsException if {@code array} is provided
     *                                   and either {@code index < 0} or {@code index > array.length}
     */
    public static long[] insert(final int index, final long[] array, final long... values) {
        if (array == null) {
            return null;
        }
        if (values == null || values.length == 0) {
            return clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }

        final long[] result = new long[array.length + values.length];

        System.arraycopy(values, 0, result, index, values.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
        }
        return result;
    }

    /**
     * <p>Inserts elements into an array at the given index (starting from zero).</p>
     *
     * <p>When an array is returned, it is always a new array.</p>
     *
     * <pre>
     * ArrayUtils.insert(index, null, null)      = null
     * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
     * ArrayUtils.insert(index, null, values)    = null
     * </pre>
     *
     * @param index  the position within {@code array} to insert the new values
     * @param array  the array to insert the values into, may be {@code null}
     * @param values the new values to insert, may be {@code null}
     * @return The new array.
     * @throws IndexOutOfBoundsException if {@code array} is provided
     *                                   and either {@code index < 0} or {@code index > array.length}
     */
    public static short[] insert(final int index, final short[] array, final short... values) {
        if (array == null) {
            return null;
        }
        if (values == null || values.length == 0) {
            return clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }

        final short[] result = new short[array.length + values.length];

        System.arraycopy(values, 0, result, index, values.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
        }
        return result;
    }

    /**
     * <p>Inserts elements into an array at the given index (starting from zero).</p>
     *
     * <p>When an array is returned, it is always a new array.</p>
     *
     * <pre>
     * ArrayUtils.insert(index, null, null)      = null
     * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
     * ArrayUtils.insert(index, null, values)    = null
     * </pre>
     *
     * @param <T>    The type of elements in {@code array} and {@code values}
     * @param index  the position within {@code array} to insert the new values
     * @param array  the array to insert the values into, may be {@code null}
     * @param values the new values to insert, may be {@code null}
     * @return The new array.
     * @throws IndexOutOfBoundsException if {@code array} is provided
     *                                   and either {@code index < 0} or {@code index > array.length}
     */
    public static <T> T[] insert(final int index, final T[] array, final T... values) {
        /*
         * Note on use of @SafeVarargs:
         *
         * By returning null when 'array' is null, we avoid returning the vararg
         * array to the caller. We also avoid relying on the type of the vararg
         * array, by inspecting the component type of 'array'.
         */

        if (array == null) {
            return null;
        }
        if (values == null || values.length == 0) {
            return clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }

        final Class<?> type = array.getClass().getComponentType();
        final T[] result = (T[]) Array.newInstance(type, array.length + values.length);

        System.arraycopy(values, 0, result, index, values.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values.length, array.length - index);
        }
        return result;
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array the array to shuffle
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final Object[] array) {
        shuffle(array, new Random());
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array  the array to shuffle
     * @param random the source of randomness used to permute the elements
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final Object[] array, final Random random) {
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array the array to shuffle
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final boolean[] array) {
        shuffle(array, new Random());
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array  the array to shuffle
     * @param random the source of randomness used to permute the elements
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final boolean[] array, final Random random) {
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array the array to shuffle
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final byte[] array) {
        shuffle(array, new Random());
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array  the array to shuffle
     * @param random the source of randomness used to permute the elements
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final byte[] array, final Random random) {
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array the array to shuffle
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final char[] array) {
        shuffle(array, new Random());
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array  the array to shuffle
     * @param random the source of randomness used to permute the elements
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final char[] array, final Random random) {
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array the array to shuffle
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final short[] array) {
        shuffle(array, new Random());
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array  the array to shuffle
     * @param random the source of randomness used to permute the elements
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final short[] array, final Random random) {
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array the array to shuffle
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final int[] array) {
        shuffle(array, new Random());
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array  the array to shuffle
     * @param random the source of randomness used to permute the elements
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final int[] array, final Random random) {
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array the array to shuffle
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final long[] array) {
        shuffle(array, new Random());
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array  the array to shuffle
     * @param random the source of randomness used to permute the elements
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final long[] array, final Random random) {
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array the array to shuffle
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final float[] array) {
        shuffle(array, new Random());
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array  the array to shuffle
     * @param random the source of randomness used to permute the elements
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final float[] array, final Random random) {
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array the array to shuffle
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final double[] array) {
        shuffle(array, new Random());
    }

    /**
     * Randomly permutes the elements of the specified array using the Fisher-Yates algorithm.
     *
     * @param array  the array to shuffle
     * @param random the source of randomness used to permute the elements
     * @see <a href="https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle">Fisher-Yates shuffle algorithm</a>
     */
    public static void shuffle(final double[] array, final Random random) {
        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    /**
     * Returns whether a given array can safely be accessed at the given index.
     *
     * @param <T>   the component type of the array
     * @param array the array to inspect, may be null
     * @param index the index of the array to be inspected
     * @return Whether the given index is safely-accessible in the given array
     */
    public static <T> boolean isArrayIndexValid(T[] array, int index) {
        if (getLength(array) == 0 || array.length <= index) {
            return false;
        }

        return index >= 0;
    }


    /**
     * 是否包含{@code null}元素
     *
     * @param <T>   数组元素类型
     * @param array 被检查的数组
     * @return 是否包含null元素
     * @since 3.0.7
     */

    public static <T> boolean hasNull(T... array) {
        if (isNotEmpty(array)) {
            for (T element : array) {
                if (null == element) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 返回数组中第一个非空元素
     *
     * @param <T>   数组元素类型
     * @param array 数组
     * @return 非空元素, 如果不存在非空元素或数组为空, 返回{@code null}
     * @since 3.0.7
     */

    public static <T> T firstNonNull(T... array) {
        if (isNotEmpty(array)) {
            for (final T val : array) {
                if (null != val) {
                    return val;
                }
            }
        }
        return null;
    }

    /**
     * 新建一个空数组
     *
     * @param <T>           数组元素类型
     * @param componentType 元素类型
     * @param newSize       大小
     * @return 空数组
     */

    public static <T> T[] newArray(Class<?> componentType, int newSize) {
        return (T[]) Array.newInstance(componentType, newSize);
    }

    /**
     * 新建一个空数组
     *
     * @param <T>     数组元素类型
     * @param newSize 大小
     * @return 空数组
     * @since 3.3.0
     */

    public static <T> T[] newArray(int newSize) {
        return (T[]) new Object[newSize];
    }

    /**
     * 获取数组对象的元素类型
     *
     * @param array 数组对象
     * @return 元素类型
     * @since 5.3.0
     */
    public static Class<?> getComponentType(Object array) {
        return null == array ? null : array.getClass().getComponentType();
    }

    /**
     * 获取数组对象的元素类型
     *
     * @param arrayClass 数组类
     * @return 元素类型
     * @since 5.3.0
     */
    public static Class<?> getComponentType(Class<?> arrayClass) {
        return null == arrayClass ? null : arrayClass.getComponentType();
    }

    /**
     * 根据数组元素类型,获取数组的类型
     * 方法是通过创建一个空数组从而获取其类型
     *
     * @param componentType 数组元素类型
     * @return 数组类型
     * @since 5.3.0
     */
    public static Class<?> getArrayType(Class<?> componentType) {
        return newArray(componentType, 0).getClass();
    }

    /**
     * 强转数组类型
     * 强制转换的前提是数组元素类型可被强制转换
     * 强制转换后会生成一个新数组
     *
     * @param type     数组类型或数组元素类型
     * @param arrayObj 原数组
     * @return 转换后的数组类型
     * @throws NullPointerException     提供参数为空
     * @throws IllegalArgumentException 参数arrayObj不是数组
     * @since 3.1.9
     */
    public static Object[] cast(Class<?> type, Object arrayObj) throws NullPointerException, IllegalArgumentException {
        if (null == arrayObj) {
            throw new NullPointerException("Argument [arrayObj] is null !");
        }
        if (false == arrayObj.getClass().isArray()) {
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
     * 将新元素添加到已有数组中
     * 添加新元素会生成一个新的数组,不影响原数组
     *
     * @param <T>         数组元素类型
     * @param buffer      已有数组
     * @param newElements 新元素
     * @return 新数组
     */
    public static <T> T[] append(T[] buffer, T... newElements) {
        if (isEmpty(buffer)) {
            return newElements;
        }
        return insert(buffer, buffer.length, newElements);
    }

    /**
     * 将新元素添加到已有数组中
     * 添加新元素会生成一个新的数组,不影响原数组
     *
     * @param <T>         数组元素类型
     * @param array       已有数组
     * @param newElements 新元素
     * @return 新数组
     */
    public static <T> Object append(Object array, T... newElements) {
        if (isEmpty(array)) {
            return newElements;
        }
        return insert(array, length(array), newElements);
    }

    /**
     * 将元素值设置为数组的某个位置,当给定的index大于数组长度,则追加
     *
     * @param <T>    数组元素类型
     * @param buffer 已有数组
     * @param index  位置,大于长度追加,否则替换
     * @param value  新值
     * @return 新数组或原有数组
     */
    public static <T> T[] setOrAppend(T[] buffer, int index, T value) {
        if (index < buffer.length) {
            Array.set(buffer, index, value);
            return buffer;
        } else {
            return append(buffer, value);
        }
    }

    /**
     * 将新元素插入到到已有数组中的某个位置
     * 添加新元素会生成一个新的数组,不影响原数组
     * 如果插入位置为为负数,从原数组从后向前计数,若大于原数组长度,则空白处用null填充
     *
     * @param <T>         数组元素类型
     * @param array       已有数组
     * @param index       插入位置,此位置为对应此位置元素之前的空档
     * @param newElements 新元素
     * @return 新数组
     *
     */
    /**
     * 将元素值设置为数组的某个位置,当给定的index大于数组长度,则追加
     *
     * @param array 已有数组
     * @param index 位置,大于长度追加,否则替换
     * @param value 新值
     * @return 新数组或原有数组
     */
    public static Object setOrAppend(Object array, int index, Object value) {
        if (index < length(array)) {
            Array.set(array, index, value);
            return array;
        } else {
            return append(array, value);
        }
    }

    public static <T> T[] insert(T[] array, int index, T... newElements) {
        if (isEmpty(newElements)) {
            return array;
        }
        if (isEmpty(array)) {
            return newElements;
        }

        final int len = length(array);
        if (index < 0) {
            index = (index % len) + len;
        }

        final T[] result = newArray(array.getClass().getComponentType(), Math.max(len, index) + newElements.length);
        System.arraycopy(array, 0, result, 0, Math.min(len, index));
        System.arraycopy(newElements, 0, result, index, newElements.length);
        if (index < len) {
            System.arraycopy(array, index, result, index + newElements.length, len - index);
        }
        return result;
    }

    /**
     * 生成一个新的重新设置大小的数组
     * 调整大小后拷贝原数组到新数组下 扩大则占位前N个位置,缩小则截断
     *
     * @param <T>           数组元素类型
     * @param buffer        原数组
     * @param newSize       新的数组大小
     * @param componentType 数组元素类型
     * @return 调整后的新数组
     */
    public static <T> T[] resize(T[] buffer, int newSize, Class<?> componentType) {
        T[] newArray = newArray(componentType, newSize);
        if (isNotEmpty(buffer)) {
            System.arraycopy(buffer, 0, newArray, 0, Math.min(buffer.length, newSize));
        }
        return newArray;
    }

    /**
     * 生成一个新的重新设置大小的数组
     * 新数组的类型为原数组的类型,调整大小后拷贝原数组到新数组下 扩大则占位前N个位置,缩小则截断
     *
     * @param <T>     数组元素类型
     * @param buffer  原数组
     * @param newSize 新的数组大小
     * @return 调整后的新数组
     */
    public static <T> T[] resize(T[] buffer, int newSize) {
        return resize(buffer, newSize, buffer.getClass().getComponentType());
    }

    /**
     * 生成一个新的重新设置大小的数组<br>
     * 调整大小后拷贝原数组到新数组下 扩大则占位前N个位置,其它位置补充0,缩小则截断
     *
     * @param bytes   原数组
     * @param newSize 新的数组大小
     * @return 调整后的新数组
     */
    public static byte[] resize(byte[] bytes, int newSize) {
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
     * 将多个数组合并在一起
     * 忽略null的数组
     *
     * @param <T>    数组元素类型
     * @param arrays 数组集合
     * @return 合并后的数组
     */
    public static <T> T[] addAll(T[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        int length = 0;
        for (T[] array : arrays) {
            if (array == null) {
                continue;
            }
            length += array.length;
        }
        T[] result = newArray(arrays.getClass().getComponentType().getComponentType(), length);

        length = 0;
        for (T[] array : arrays) {
            if (array == null) {
                continue;
            }
            System.arraycopy(array, 0, result, length, array.length);
            length += array.length;
        }
        return result;
    }

    /**
     * 包装 {@link System#arraycopy(Object, int, Object, int, int)}
     * 数组复制
     *
     * @param src     源数组
     * @param srcPos  源数组开始位置
     * @param dest    目标数组
     * @param destPos 目标数组开始位置
     * @param length  拷贝数组长度
     * @return 目标数组
     * @since 3.1.9
     */
    public static Object copy(Object src, int srcPos, Object dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }

    /**
     * 包装 {@link System#arraycopy(Object, int, Object, int, int)}
     * 数组复制,缘数组和目标数组都是从位置0开始复制
     *
     * @param src    源数组
     * @param dest   目标数组
     * @param length 拷贝数组长度
     * @return 目标数组
     * @since 3.1.9
     */
    public static Object copy(Object src, Object dest, int length) {
        System.arraycopy(src, 0, dest, 0, length);
        return dest;
    }

    /**
     * 生成一个从0开始的数字列表
     *
     * @param excludedEnd 结束的数字（不包含）
     * @return 数字列表
     */
    public static int[] range(int excludedEnd) {
        return range(0, excludedEnd, 1);
    }

    /**
     * 生成一个数字列表
     * 自动判定正序反序
     *
     * @param includedStart 开始的数字（包含）
     * @param excludedEnd   结束的数字（不包含）
     * @return 数字列表
     */
    public static int[] range(int includedStart, int excludedEnd) {
        return range(includedStart, excludedEnd, 1);
    }

    /**
     * 生成一个数字列表
     * 自动判定正序反序
     *
     * @param includedStart 开始的数字（包含）
     * @param excludedEnd   结束的数字（不包含）
     * @param step          步进
     * @return 数字列表
     */
    public static int[] range(int includedStart, int excludedEnd, int step) {
        if (includedStart > excludedEnd) {
            int tmp = includedStart;
            includedStart = excludedEnd;
            excludedEnd = tmp;
        }

        if (step <= 0) {
            step = 1;
        }

        int deviation = excludedEnd - includedStart;
        int length = deviation / step;
        if (deviation % step != 0) {
            length += 1;
        }
        int[] range = new int[length];
        for (int i = 0; i < length; i++) {
            range[i] = includedStart;
            includedStart += step;
        }
        return range;
    }

    /**
     * 拆分byte数组为几个等份（最后一份可能小于len）
     *
     * @param array 数组
     * @param len   每个小节的长度
     * @return 拆分后的数组
     */
    public static byte[][] split(byte[] array, int len) {
        int x = array.length / len;
        int y = array.length % len;
        int z = 0;
        if (y != 0) {
            z = 1;
        }
        byte[][] arrays = new byte[x + z][];
        byte[] arr;
        for (int i = 0; i < x + z; i++) {
            arr = new byte[len];
            if (i == x + z - 1 && y != 0) {
                System.arraycopy(array, i * len, arr, 0, y);
            } else {
                System.arraycopy(array, i * len, arr, 0, len);
            }
            arrays[i] = arr;
        }
        return arrays;
    }

    /**
     * 返回数组中指定元素所在位置,忽略大小写,未找到返回{@link #INDEX_NOT_FOUND}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置, 未找到返回{@link #INDEX_NOT_FOUND}
     * @since 3.1.9
     */
    public static int indexOfIgnoreCase(CharSequence[] array, CharSequence value) {
        if (null != array) {
            for (int i = 0; i < array.length; i++) {
                if (StringUtils.equalsIgnoreCase(array[i], value)) {
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }


    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static int[] unWrap(Integer... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new int[0];
        }

        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i].intValue();
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Long[] wrap(long... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new Long[0];
        }

        Long[] array = new Long[length];
        for (int i = 0; i < length; i++) {
            array[i] = Long.valueOf(values[i]);
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static long[] unWrap(Long... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new long[0];
        }

        long[] array = new long[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i].longValue();
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Character[] wrap(char... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new Character[0];
        }

        Character[] array = new Character[length];
        for (int i = 0; i < length; i++) {
            array[i] = Character.valueOf(values[i]);
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static char[] unWrap(Character... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new char[0];
        }

        char[] array = new char[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i].charValue();
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Byte[] wrap(byte... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new Byte[0];
        }

        Byte[] array = new Byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = Byte.valueOf(values[i]);
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static byte[] unWrap(Byte... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new byte[0];
        }

        byte[] array = new byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i].byteValue();
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Short[] wrap(short... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new Short[0];
        }

        Short[] array = new Short[length];
        for (int i = 0; i < length; i++) {
            array[i] = Short.valueOf(values[i]);
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static short[] unWrap(Short... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new short[0];
        }

        short[] array = new short[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i].shortValue();
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Float[] wrap(float... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new Float[0];
        }

        Float[] array = new Float[length];
        for (int i = 0; i < length; i++) {
            array[i] = Float.valueOf(values[i]);
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static float[] unWrap(Float... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new float[0];
        }

        float[] array = new float[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i].floatValue();
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Double[] wrap(double... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new Double[0];
        }

        Double[] array = new Double[length];
        for (int i = 0; i < length; i++) {
            array[i] = Double.valueOf(values[i]);
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static double[] unWrap(Double... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new double[0];
        }

        double[] array = new double[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i].doubleValue();
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Boolean[] wrap(boolean... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new Boolean[0];
        }

        Boolean[] array = new Boolean[length];
        for (int i = 0; i < length; i++) {
            array[i] = Boolean.valueOf(values[i]);
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static boolean[] unWrap(Boolean... values) {
        if (null == values) {
            return null;
        }
        int length = values.length;
        if (0 == length) {
            return new boolean[0];
        }

        boolean[] array = new boolean[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i].booleanValue();
        }
        return array;
    }

    /**
     * 包装数组对象
     *
     * @param obj 对象,可以是对象数组或者基本类型数组
     * @return 包装类型数组或对象数组
     * @throws InstrumentException 对象为非数组
     */
    public static Object[] wrap(Object obj) {
        if (null == obj) {
            return null;
        }
        if (isArray(obj)) {
            try {
                return (Object[]) obj;
            } catch (Exception e) {
                String className = obj.getClass().getComponentType().getName();
                switch (className) {
                    case "long":
                        return wrap((long[]) obj);
                    case "int":
                        return wrap(obj);
                    case "short":
                        return wrap((short[]) obj);
                    case "char":
                        return wrap((char[]) obj);
                    case "byte":
                        return wrap((byte[]) obj);
                    case "boolean":
                        return wrap((boolean[]) obj);
                    case "float":
                        return wrap((float[]) obj);
                    case "double":
                        return wrap((double[]) obj);
                    default:
                        throw new InstrumentException(e);
                }
            }
        }
        throw new InstrumentException("is not Array!");
    }

    /**
     * 对象是否为数组对象
     *
     * @param obj 对象
     * @return 是否为数组对象, 如果为{@code null} 返回false
     */
    public static boolean isArray(Object obj) {
        if (null == obj) {
            return false;
        }
        return obj.getClass().isArray();
    }

    /**
     * 获取数组对象中指定index的值,支持负数,例如-1表示倒数第一个值
     *
     * @param <T>   数组元素类型
     * @param array 数组对象
     * @param index 下标,支持负数
     * @return 值
     */
    public static <T> T get(Object array, int index) {
        if (index < 0) {
            index += Array.getLength(array);
        }
        return (T) Array.get(array, index);
    }

    /**
     * 获取数组中指定多个下标元素值,组成新数组
     *
     * @param <T>     数组元素类型
     * @param array   数组
     * @param indexes 下标列表
     * @return 结果
     */
    public static <T> T[] getAny(Object array, int... indexes) {
        T[] result = newArray(indexes.length);
        for (int i : indexes) {
            result[i] = get(array, i);
        }
        return result;
    }

    /**
     * 获取子数组
     *
     * @param array 数组
     * @param start 开始位置（包括）
     * @param end   结束位置（不包括）
     * @return 新的数组
     */
    public static Object[] sub(Object array, int start, int end) {
        return sub(array, start, end, 1);
    }

    /**
     * 获取子数组
     *
     * @param array 数组
     * @param start 开始位置（包括）
     * @param end   结束位置（不包括）
     * @param step  步进
     * @return 新的数组
     */
    public static Object[] sub(Object array, int start, int end, int step) {
        int length = length(array);
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        if (start == length) {
            return new Object[0];
        }
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (end > length) {
            if (start >= length) {
                return new Object[0];
            }
            end = length;
        }

        if (step <= 1) {
            step = 1;
        }

        ArrayList<Object> list = new ArrayList<>();
        for (int i = start; i < end; i += step) {
            list.add(get(array, i));
        }

        return list.toArray();
    }


    /**
     * 获取数组长度
     * 如果参数为{@code null},返回0
     *
     * <pre>
     * ArrayUtils.length(null)            = 0
     * ArrayUtils.length([])              = 0
     * ArrayUtils.length([null])          = 1
     * ArrayUtils.length([true, false])   = 2
     * ArrayUtils.length([1, 2, 3])       = 3
     * ArrayUtils.length(["a", "b", "c"]) = 3
     * </pre>
     *
     * @param array 数组对象
     * @return 数组长度
     * @throws IllegalArgumentException 如果参数不为数组,抛出此异常
     * @see Array#getLength(Object)
     */
    public static int length(Object array) throws IllegalArgumentException {
        if (null == array) {
            return 0;
        }
        return Array.getLength(array);
    }

    /**
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param array       数组
     * @param conjunction 分隔符
     * @return 连接后的字符串
     */
    public static String join(long[] array, CharSequence conjunction) {
        if (null == array) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (long item : array) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(conjunction);
            }
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param <T>         被处理的集合
     * @param array       数组
     * @param conjunction 分隔符
     * @param prefix      每个元素添加的前缀,null表示不添加
     * @param suffix      每个元素添加的后缀,null表示不添加
     * @return 连接后的字符串
     */
    public static <T> String join(T[] array, CharSequence conjunction, String prefix, String suffix) {
        if (null == array) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (T item : array) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(conjunction);
            }
            if (ArrayUtils.isArray(item)) {
                sb.append(join(ArrayUtils.wrap(item), conjunction, prefix, suffix));
            } else if (item instanceof Iterable<?>) {
                sb.append(IterUtils.join((Iterable<?>) item, conjunction, prefix, suffix));
            } else if (item instanceof Iterator<?>) {
                sb.append(IterUtils.join((Iterator<?>) item, conjunction, prefix, suffix));
            } else {
                sb.append(StringUtils.wrap(StringUtils.toString(item), prefix, suffix));
            }
        }
        return sb.toString();
    }

    /**
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param array       数组
     * @param conjunction 分隔符
     * @return 连接后的字符串
     */
    public static String join(int[] array, CharSequence conjunction) {
        if (null == array) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (int item : array) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(conjunction);
            }
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param array       数组
     * @param conjunction 分隔符
     * @return 连接后的字符串
     */
    public static String join(short[] array, CharSequence conjunction) {
        if (null == array) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (short item : array) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(conjunction);
            }
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param array       数组
     * @param conjunction 分隔符
     * @return 连接后的字符串
     */
    public static String join(char[] array, CharSequence conjunction) {
        if (null == array) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (char item : array) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(conjunction);
            }
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param array       数组
     * @param conjunction 分隔符
     * @return 连接后的字符串
     */
    public static String join(byte[] array, CharSequence conjunction) {
        if (null == array) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (byte item : array) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(conjunction);
            }
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param array       数组
     * @param conjunction 分隔符
     * @return 连接后的字符串
     */
    public static String join(boolean[] array, CharSequence conjunction) {
        if (null == array) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (boolean item : array) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(conjunction);
            }
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param array       数组
     * @param conjunction 分隔符
     * @return 连接后的字符串
     */
    public static String join(float[] array, CharSequence conjunction) {
        if (null == array) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (float item : array) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(conjunction);
            }
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param array       数组
     * @param conjunction 分隔符
     * @return 连接后的字符串
     */
    public static String join(double[] array, CharSequence conjunction) {
        if (null == array) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (double item : array) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(conjunction);
            }
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param array       数组
     * @param conjunction 分隔符
     * @return 连接后的字符串
     */
    public static String join(Object array, CharSequence conjunction) {
        if (isArray(array)) {
            Class<?> componentType = array.getClass().getComponentType();
            if (componentType.isPrimitive()) {
                String componentTypeName = componentType.getName();
                switch (componentTypeName) {
                    case "long":
                        return join((long[]) array, conjunction);
                    case "int":
                        return join((int[]) array, conjunction);
                    case "short":
                        return join((short[]) array, conjunction);
                    case "char":
                        return join((char[]) array, conjunction);
                    case "byte":
                        return join((byte[]) array, conjunction);
                    case "boolean":
                        return join((boolean[]) array, conjunction);
                    case "float":
                        return join((float[]) array, conjunction);
                    case "double":
                        return join((double[]) array, conjunction);
                    default:
                        throw new InstrumentException("Unknown primitive type:" + componentTypeName);
                }
            } else {
                return join(array, conjunction);
            }
        }
        throw new InstrumentException("is not a Array!");
    }

    /**
     * {@link ByteBuffer} 转byte数组
     *
     * @param bytebuffer {@link ByteBuffer}
     * @return byte数组
     * @since 3.0.1
     */
    public static byte[] toArray(ByteBuffer bytebuffer) {
        if (false == bytebuffer.hasArray()) {
            int oldPosition = bytebuffer.position();
            bytebuffer.position(0);
            int size = bytebuffer.limit();
            byte[] buffers = new byte[size];
            bytebuffer.get(buffers);
            bytebuffer.position(oldPosition);
            return buffers;
        } else {
            return Arrays.copyOfRange(bytebuffer.array(), bytebuffer.position(), bytebuffer.limit());
        }
    }

    /**
     * 将集合转为数组
     *
     * @param <T>           数组元素类型
     * @param collection    集合
     * @param componentType 集合元素类型
     * @return 数组
     * @since 3.1.9
     */
    public static <T> T[] toArray(Collection<T> collection, Class<T> componentType) {
        T[] array = newArray(componentType, collection.size());
        return collection.toArray(array);
    }

    /**
     * 取最小值
     *
     * @param <T>         元素类型
     * @param numberArray 数字数组
     * @return 最小值
     * @since 3.1.9
     */
    public static <T extends Comparable<? super T>> T min(T[] numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        T min = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (ObjectUtils.compare(min, numberArray[i]) > 0) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @since 3.1.9
     */
    public static long min(long... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        long min = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @since 3.1.9
     */
    public static int min(int... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        int min = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @since 3.1.9
     */
    public static short min(short... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        short min = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @since 3.1.9
     */
    public static char min(char... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        char min = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @since 3.1.9
     */
    public static byte min(byte... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        byte min = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @since 3.1.9
     */
    public static double min(double... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        double min = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     * @since 3.1.9
     */
    public static float min(float... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        float min = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最大值
     *
     * @param <T>         元素类型
     * @param numberArray 数字数组
     * @return 最大值
     * @since 3.1.9
     */
    public static <T extends Comparable<? super T>> T max(T[] numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        T max = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (ObjectUtils.compare(max, numberArray[i]) < 0) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @since 3.1.9
     */
    public static long max(long... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        long max = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @since 3.1.9
     */
    public static int max(int... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        int max = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @since 3.1.9
     */
    public static short max(short... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        short max = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @since 3.1.9
     */
    public static char max(char... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        char max = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @since 3.1.9
     */
    public static byte max(byte... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        byte max = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @since 3.1.9
     */
    public static double max(double... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        double max = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     * @since 3.1.9
     */
    public static float max(float... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        float max = numberArray[0];
        for (int i = 0; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 映射键值（参考Python的zip()函数）
     * 例如：
     * keys = [a,b,c,d]
     * values = [1,2,3,4]
     * 则得到的Map是 {a=1, b=2, c=3, d=4}
     * 如果两个数组长度不同,则只对应最短部分
     *
     * @param <K>     Key类型
     * @param <V>     Value类型
     * @param keys    键列表
     * @param values  值列表
     * @param isOrder 是否有序
     * @return Map
     * @since 3.0.4
     */
    public static <K, V> Map<K, V> zip(K[] keys, V[] values, boolean isOrder) {
        if (isEmpty(keys) || isEmpty(values)) {
            return null;
        }

        int size = Math.min(keys.length, values.length);
        Map<K, V> map = CollUtils.newHashMap(size, isOrder);
        for (int i = 0; i < size; i++) {
            map.put(keys[i], values[i]);
        }

        return map;
    }

    /**
     * 过滤
     * 过滤过程通过传入的Editor实现来返回需要的元素内容,这个Editor实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象,如果返回null表示这个元素对象抛弃
     * 2、修改元素对象,返回集合中为修改后的对象
     * </pre>
     *
     * @param <T>    数组元素类型
     * @param array  数组
     * @param editor 编辑器接口
     * @return 过滤后的数组
     */
    public static <T> T[] filter(T[] array, Editor<T> editor) {
        ArrayList<T> list = new ArrayList<T>(array.length);
        T modified;
        for (T t : array) {
            modified = editor.edit(t);
            if (null != modified) {
                list.add(modified);
            }
        }
        return list.toArray(Arrays.copyOf(array, list.size()));
    }

    /**
     * 过滤
     * 过滤过程通过传入的Filter实现来过滤返回需要的元素内容,这个Editor实现可以实现以下功能：
     *
     * @param <T>    数组元素类型
     * @param array  数组
     * @param filter 过滤器接口,用于定义过滤规则
     * @return 过滤后的数组
     * @since 5.3.0
     */
    public static <T> T[] filter(T[] array, Filter<T> filter) {
        ArrayList<T> list = new ArrayList<T>(array.length);
        boolean isAccept;
        for (T t : array) {
            isAccept = filter.accept(t);
            if (isAccept) {
                list.add(t);
            }
        }
        return list.toArray(Arrays.copyOf(array, list.size()));
    }

    /**
     * 数组中是否包含元素,忽略大小写
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     * @since 3.1.9
     */
    public static boolean containsIgnoreCase(CharSequence[] array, CharSequence value) {
        return indexOfIgnoreCase(array, value) > INDEX_NOT_FOUND;
    }

    /**
     * 找到第一个不为 null 的元素
     *
     * @param objects 对象
     * @return 不为 null 的元素
     */
    public static Optional<Object> firstNotNullElem(Object[] objects) {
        if (isEmpty(objects)) {
            return Optional.empty();
        }

        for (Object elem : objects) {
            if (ObjectUtils.isNotNull(elem)) {
                return Optional.of(elem);
            }
        }
        return Optional.empty();
    }

}
