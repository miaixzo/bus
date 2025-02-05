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
 * 数组工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ArrayKit extends PrimitiveArray {

    /**
     * 转为数组，如果values为数组，返回，否则返回一个只有values一个元素的数组 注意：values的元素类型或其本身类型必须和提供的elementType完全一致
     *
     * @param <A>         数组类型
     * @param values      元素值
     * @param elementType 数组元素类型，{@code null}表示使用values的类型
     * @return 数组
     */
    public static <A> A castOrWrapSingle(final Object values, final Class<?> elementType) {
        if (isArray(values)) {
            return (A) values;
        }

        return wrapSingle(values, elementType);
    }

    /**
     * 包装单一元素为数组
     *
     * @param <A>         数组类型
     * @param value       元素值
     * @param elementType 数组元素类型，{@code null}表示使用value的类型
     * @return 数组
     */
    public static <A> A wrapSingle(final Object value, final Class<?> elementType) {
        // 插入单个元素
        final Object newInstance = Array.newInstance(null == elementType ? value.getClass() : elementType, 1);
        Array.set(newInstance, 0, value);
        return (A) newInstance;
    }

    /**
     * 将集合转为数组，如果集合为{@code null}，则返回空的数组（元素个数为0）
     *
     * @param <T>           数组元素类型
     * @param iterable      {@link Iterable}
     * @param componentType 集合元素类型
     * @return 数组
     */
    public static <T> T[] ofArray(final Iterable<T> iterable, final Class<T> componentType) {
        if (null == iterable) {
            return newArray(componentType, 0);
        }

        if (iterable instanceof List) {
            // List
            return ((List<T>) iterable).toArray(newArray(componentType, 0));
        } else if (iterable instanceof Collection) {
            // 其它集合
            final int size = ((Collection<T>) iterable).size();
            final T[] result = newArray(componentType, size);
            int i = 0;
            for (final T element : iterable) {
                result[i] = element;
                i++;
            }
        }

        // 自定义Iterable转为List处理
        return ListKit.of(iterable.iterator()).toArray(newArray(componentType, 0));
    }

    /**
     * 新建一个空数组
     *
     * @param <T>           数组元素类型
     * @param componentType 元素类型，例如：{@code Integer.class}，但是不能使用原始类型，例如：{@code int.class}
     * @param newSize       大小
     * @return 空数组
     */
    public static <T> T[] newArray(final Class<?> componentType, final int newSize) {
        return (T[]) Array.newInstance(componentType, newSize);
    }

    /**
     * 新建一个{@code Object}类型空数组
     *
     * @param newSize 大小
     * @return {@code Object}类型的空数组
     */
    public static Object[] newArray(final int newSize) {
        return new Object[newSize];
    }

    /**
     * 获取数组对象的元素类型，方法调用参数与返回结果举例：
     * <ul>
     * <li>Object[] = Object.class</li>
     * <li>String[] = String.class</li>
     * <li>int[] = int.class</li>
     * <li>Integer[] = Integer.class</li>
     * <li>null = null</li>
     * <li>String = null</li>
     * </ul>
     *
     * @param array 数组对象
     * @return 元素类型
     */
    public static Class<?> getComponentType(final Object array) {
        return null == array ? null : getComponentType(array.getClass());
    }

    /**
     * 获取数组对象的元素类型，方法调用参数与返回结果举例：
     * <ul>
     * <li>Object[].class = Object.class</li>
     * <li>String[].class = String.class</li>
     * <li>int[].class = int.class</li>
     * <li>Integer[].class = Integer.class</li>
     * <li>null = null</li>
     * <li>String.class = null</li>
     * </ul>
     *
     * @param arrayClass 数组对象的class
     * @return 元素类型
     */
    public static Class<?> getComponentType(final Class<?> arrayClass) {
        return null == arrayClass ? null : arrayClass.getComponentType();
    }

    /**
     * 根据数组元素类型，获取数组的类型 方法是通过创建一个空数组从而获取其类型
     * <p>
     * 本方法是 {@link #getComponentType(Class)}的逆方法
     * </p>
     *
     * @param componentType 数组元素类型
     * @return 数组类型
     */
    public static Class<?> getArrayType(final Class<?> componentType) {
        return Array.newInstance(componentType, 0).getClass();
    }

    /**
     * 强转数组类型 强制转换的前提是数组元素类型可被强制转换 强制转换后会生成一个新数组
     *
     * @param type     数组类型或数组元素类型
     * @param arrayObj 原数组
     * @return 转换后的数组类型
     * @throws NullPointerException     提供参数为空
     * @throws IllegalArgumentException 参数arrayObj不是数组
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
        final Object[] result = ArrayKit.newArray(componentType, array.length);
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    /**
     * 将新元素添加到已有数组中 添加新元素会生成一个新的数组，不影响原数组
     *
     * @param <T>         数组元素类型
     * @param buffer      已有数组
     * @param newElements 新元素
     * @return 新数组
     */
    @SafeVarargs
    public static <T> T[] append(final T[] buffer, final T... newElements) {
        if (isEmpty(buffer)) {
            return newElements;
        }
        return insert(buffer, buffer.length, newElements);
    }

    /**
     * 将新元素添加到已有数组中 添加新元素会生成一个新的数组，不影响原数组
     *
     * @param <A>         数组类型
     * @param <T>         数组元素类型
     * @param array       已有数组
     * @param newElements 新元素
     * @return 新数组
     */
    @SafeVarargs
    public static <A, T> A append(final A array, final T... newElements) {
        if (isEmpty(array)) {
            if (null == array) {
                return (A) newElements;
            }
            // 可变长参数可能为包装类型，如果array是原始类型，则此处强转不合适，采用万能转换器完成转换
            return (A) Convert.convert(array.getClass(), newElements);
        }
        return insert(array, length(array), newElements);
    }

    /**
     * 将元素值设置为数组的某个位置，当给定的index大于等于数组长度，则追加
     *
     * @param <T>   数组元素类型
     * @param array 已有数组
     * @param index 位置，大于等于长度则追加，否则替换
     * @param value 新值
     * @return 新数组或原有数组
     */
    public static <T> T[] setOrAppend(final T[] array, final int index, final T value) {
        if (isEmpty(array)) {
            return wrapSingle(value, null == array ? null : array.getClass().getComponentType());
        }
        return ArrayWrapper.of(array).setOrAppend(index, value).getRaw();
    }

    /**
     * 将元素值设置为数组的某个位置，当给定的index大于等于数组长度，则追加
     *
     * @param <A>   数组类型
     * @param array 已有数组
     * @param index 位置，大于等于长度则追加，否则替换
     * @param value 新值
     * @return 新数组或原有数组
     */
    public static <A> A setOrAppend(final A array, final int index, final Object value) {
        if (isEmpty(array)) {
            return wrapSingle(value, null == array ? null : array.getClass().getComponentType());
        }
        return ArrayWrapper.of(array).setOrAppend(index, value).getRaw();
    }

    /**
     * 将元素值设置为数组的某个位置，当index小于数组的长度时，替换指定位置的值，否则追加{@code null}或{@code 0}直到到达index后，设置值
     *
     * @param <A>   数组类型
     * @param array 已有数组
     * @param index 位置，大于等于长度则追加，否则替换
     * @param value 新值
     * @return 新数组或原有数组
     */
    public static <A> A setOrPadding(final A array, final int index, final Object value) {
        if (index == 0 && isEmpty(array)) {
            return wrapSingle(value, null == array ? null : array.getClass().getComponentType());
        }
        return ArrayWrapper.of(array).setOrPadding(index, value).getRaw();
    }

    /**
     * 将元素值设置为数组的某个位置，当index小于数组的长度时，替换指定位置的值，否则追加paddingValue直到到达index后，设置值
     *
     * @param <A>          数组类型
     * @param <E>          元素类型
     * @param array        已有数组
     * @param index        位置，大于等于长度则追加，否则替换
     * @param value        新值
     * @param paddingValue 填充值
     * @return 新数组或原有数组
     */
    public static <A, E> A setOrPadding(final A array, final int index, final E value, final E paddingValue) {
        if (index == 0 && isEmpty(array)) {
            return wrapSingle(value, null == array ? null : array.getClass().getComponentType());
        }
        return ArrayWrapper.of(array).setOrPadding(index, value, paddingValue).getRaw();
    }

    /**
     * 合并所有数组，返回合并后的新数组 忽略null的数组
     *
     * @param <T>    数组元素类型
     * @param arrays 数组集合
     * @return 合并后的数组
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
     * 从数组中的指定位置开始，按顺序使用新元素替换旧元素
     * <ul>
     * <li>如果 指定位置 为负数，那么生成一个新数组，其中新元素按顺序放在数组头部</li>
     * <li>如果 指定位置 大于等于 旧数组长度，那么生成一个新数组，其中新元素按顺序放在数组尾部</li>
     * <li>如果 指定位置 加上 新元素数量 大于 旧数组长度，那么生成一个新数组，指定位置之前是旧数组元素，指定位置及之后为新元素</li>
     * <li>否则，从已有数组中的指定位置开始，按顺序使用新元素替换旧元素，返回旧数组</li>
     * </ul>
     *
     * @param <T>    数组元素类型
     * @param array  已有数组
     * @param index  位置
     * @param values 新值
     * @return 新数组或原有数组
     */
    public static <T> T[] replace(final T[] array, final int index, final T... values) {
        if (isEmpty(array)) {
            return values;
        }
        return ArrayWrapper.of(array).replace(index, values).getRaw();
    }

    /**
     * 从数组中的指定位置开始，按顺序使用新元素替换旧元素
     * <ul>
     * <li>如果 指定位置 为负数，那么生成一个新数组，其中新元素按顺序放在数组头部</li>
     * <li>如果 指定位置 大于等于 旧数组长度，那么生成一个新数组，其中新元素按顺序放在数组尾部</li>
     * <li>如果 指定位置 加上 新元素数量 大于 旧数组长度，那么生成一个新数组，指定位置之前是旧数组元素，指定位置及之后为新元素</li>
     * <li>否则，从已有数组中的指定位置开始，按顺序使用新元素替换旧元素，返回旧数组</li>
     * </ul>
     *
     * @param <A>    数组类型
     * @param array  已有数组
     * @param index  位置
     * @param values 新值
     * @return 新数组或原有数组
     */
    public static <A> A replace(final A array, final int index, final A values) {
        if (isEmpty(array)) {
            return castOrWrapSingle(values, null == array ? null : array.getClass().getComponentType());
        }
        return ArrayWrapper.of(array).replace(index, values).getRaw();
    }

    /**
     * 将新元素插入到已有数组中的某个位置 添加新元素会生成一个新的数组，不影响原数组 如果插入位置为负数，从原数组从后向前计数，若大于原数组长度，则空白处用null填充
     *
     * @param <T>         数组元素类型
     * @param buffer      已有数组
     * @param index       插入位置，此位置为对应此位置元素之前的空档
     * @param newElements 新元素
     * @return 新数组
     */
    public static <T> T[] insert(final T[] buffer, final int index, final T... newElements) {
        return (T[]) insert((Object) buffer, index, newElements);
    }

    /**
     * 将新元素插入到已有数组中的某个位置 添加新元素会生成一个新的数组，不影响原数组 如果插入位置为负数，从原数组从后向前计数，若大于原数组长度，则空白处用默认值填充
     *
     * @param <A>         数组类型
     * @param <E>         数组元素类型
     * @param array       已有数组，可以为原始类型数组
     * @param index       插入位置，此位置为对应此位置元素之前的空档
     * @param newElements 新元素
     * @return 新数组
     */
    @SafeVarargs
    public static <A, E> A insert(final A array, final int index, final E... newElements) {
        return ArrayWrapper.of(array).insertArray(index, (A) newElements).getRaw();
    }

    /**
     * 生成一个新的重新设置大小的数组 调整大小后，按顺序拷贝原数组到新数组中，新长度更小则截断
     *
     * @param <T>           数组元素类型
     * @param data          原数组
     * @param newSize       新的数组大小
     * @param componentType 数组元素类型
     * @return 调整后的新数组
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
     * 生成一个新的重新设置大小的数组 调整大小后，按顺序拷贝原数组到新数组中，新长度更小则截断
     *
     * @param array   原数组
     * @param newSize 新的数组大小
     * @return 调整后的新数组
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
     * 生成一个新的重新设置大小的数组 调整大小后，按顺序拷贝原数组到新数组中，新长度更小则截断原数组
     *
     * @param <T>     数组元素类型
     * @param buffer  原数组
     * @param newSize 新的数组大小
     * @return 调整后的新数组
     */
    public static <T> T[] resize(final T[] buffer, final int newSize) {
        return resize(buffer, newSize, buffer.getClass().getComponentType());
    }

    /**
     * 包装 {@link System#arraycopy(Object, int, Object, int, int)} 数组复制，源数组和目标数组都是从位置0开始复制，复制长度为源数组的长度
     *
     * @param <T>  目标数组类型
     * @param src  源数组
     * @param dest 目标数组
     * @return 目标数组
     */
    public static <T> T copy(final Object src, final T dest) {
        return copy(src, dest, length(src));
    }

    /**
     * 包装 {@link System#arraycopy(Object, int, Object, int, int)} 数组复制，源数组和目标数组都是从位置0开始复制
     *
     * @param <T>    目标数组类型
     * @param src    源数组
     * @param dest   目标数组
     * @param length 拷贝数组长度
     * @return 目标数组
     */
    public static <T> T copy(final Object src, final T dest, final int length) {
        return copy(src, 0, dest, 0, length);
    }

    /**
     * 包装 {@link System#arraycopy(Object, int, Object, int, int)} 数组复制
     *
     * @param <T>     目标数组类型
     * @param src     源数组
     * @param srcPos  源数组开始位置
     * @param dest    目标数组
     * @param destPos 目标数组开始位置
     * @param length  拷贝数组长度
     * @return 目标数组
     */
    public static <T> T copy(final Object src, final int srcPos, final T dest, final int destPos, final int length) {
        // noinspection SuspiciousSystemArraycopy
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }

    /**
     * 克隆数组
     *
     * @param <T>   数组元素类型
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
     * 克隆数组，如果非数组返回{@code null}
     *
     * @param <T>    数组元素类型
     * @param object 数组对象
     * @return 克隆后的数组对象
     */
    public static <T> T clone(final T object) {
        if (null == object) {
            return null;
        }
        if (isArray(object)) {
            final Object result;
            final Class<?> componentType = object.getClass().getComponentType();
            // 原始类型
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
     * 对每个数组元素执行指定操作，返回操作后的元素 这个Editor实现可以实现以下功能：
     * <ol>
     * <li>过滤出需要的对象，如果返回{@code null}则抛弃这个元素对象</li>
     * <li>修改元素对象，返回修改后的对象</li>
     * </ol>
     *
     * @param <T>    数组元素类型
     * @param array  数组
     * @param editor 编辑器接口，为 {@code null}则返回原数组
     * @return 编辑后的数组
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
     * 过滤数组元素 保留 {@link Predicate#test(Object)}为{@code true}的元素
     *
     * @param <T>       数组元素类型
     * @param array     数组
     * @param predicate 过滤器接口，用于定义过滤规则，为{@code null}则返回原数组
     * @return 过滤后的数组
     */
    public static <T> T[] filter(final T[] array, final Predicate<T> predicate) {
        if (null == array || null == predicate) {
            return array;
        }
        return edit(array, t -> predicate.test(t) ? t : null);
    }

    /**
     * 去除 {@code null} 元素
     *
     * @param <T>   数组元素类型
     * @param array 数组
     * @return 处理后的数组
     */
    public static <T> T[] removeNull(final T[] array) {
        // 返回元素本身，如果为null便自动过滤
        return edit(array, UnaryOperator.identity());
    }

    /**
     * 去除{@code null}或者"" 元素
     *
     * @param <T>   数组元素类型
     * @param array 数组
     * @return 处理后的数组
     */
    public static <T extends CharSequence> T[] removeEmpty(final T[] array) {
        return filter(array, StringKit::isNotEmpty);
    }

    /**
     * 去除{@code null}或者""或者空白字符串 元素
     *
     * @param <T>   数组元素类型
     * @param array 数组
     * @return 处理后的数组
     */
    public static <T extends CharSequence> T[] removeBlank(final T[] array) {
        return filter(array, StringKit::isNotBlank);
    }

    /**
     * 数组元素中的null转换为""
     *
     * @param array 数组
     * @return 处理后的数组
     */
    public static String[] nullToEmpty(final String[] array) {
        return edit(array, t -> null == t ? Normal.EMPTY : t);
    }

    /**
     * 映射键值（参考Python的zip()函数） 例如： keys = [a,b,c,d] values = [1,2,3,4] 则得到的Map是 {a=1, b=2, c=3, d=4} 如果两个数组长度不同，则只对应最短部分
     *
     * @param <K>     Key类型
     * @param <V>     Value类型
     * @param keys    键列表
     * @param values  值列表
     * @param isOrder Map中的元素是否保留键值数组本身的顺序
     * @return Map
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
     * 映射键值（参考Python的zip()函数），返回Map无序 例如： keys = [a,b,c,d] values = [1,2,3,4] 则得到的Map是 {a=1, b=2, c=3, d=4}
     * 如果两个数组长度不同，则只对应最短部分
     *
     * @param <K>    Key类型
     * @param <V>    Value类型
     * @param keys   键列表
     * @param values 值列表
     * @return Map
     */
    public static <K, V> Map<K, V> zip(final K[] keys, final V[] values) {
        return zip(keys, values, false);
    }

    /**
     * 数组中是否包含指定元素
     *
     * @param <T>   数组元素类型
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static <T> boolean contains(final T[] array, final T value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * 数组中是否包含指定元素中的任意一个
     *
     * @param <T>    数组元素类型
     * @param array  数组
     * @param values 被检查的多个元素
     * @return 是否包含指定元素中的任意一个
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
     * 数组中是否包含所有指定元素
     *
     * @param <T>    数组元素类型
     * @param array  数组
     * @param values 被检查的多个元素
     * @return 是否包含所有指定元素
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
     * 数组中是否包含元素，忽略大小写
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean containsIgnoreCase(final CharSequence[] array, final CharSequence value) {
        return indexOfIgnoreCase(array, value) > Normal.__1;
    }

    /**
     * 包装数组对象
     *
     * @param object 对象，可以是对象数组或者基本类型数组
     * @return 包装类型数组或对象数组
     * @throws InternalException 对象为非数组
     */
    public static Object[] wrap(final Object object) {
        if (null == object) {
            return null;
        }
        if (isArray(object)) {
            try {
                final String className = object.getClass().getComponentType().getName();
                switch (className) {
                case "long":
                    return wrap((long[]) object);
                case "int":
                    return wrap((int[]) object);
                case "short":
                    return wrap((short[]) object);
                case "char":
                    return wrap((char[]) object);
                case "byte":
                    return wrap((byte[]) object);
                case "boolean":
                    return wrap((boolean[]) object);
                case "float":
                    return wrap((float[]) object);
                case "double":
                    return wrap((double[]) object);
                default:
                    return (Object[]) object;
                }
            } catch (final Exception e) {
                throw ExceptionKit.wrapRuntime(e);
            }
        }
        throw new InternalException(StringKit.format("[{}] is not Array!", object.getClass()));
    }

    /**
     * 获取数组对象中指定index的值，支持负数，例如-1表示倒数第一个值 如果数组下标越界，返回null
     *
     * @param <E>   数组元素类型
     * @param array 数组对象
     * @param index 下标，支持负数
     * @return 值
     */
    public static <E> E get(final Object array, final int index) {
        return (E) ArrayWrapper.of(array).get(index);
    }

    /**
     * 获取满足条件的第一个元素
     *
     * @param array     数组
     * @param predicate 条件
     * @param <E>       元素类型
     * @return 满足条件的第一个元素，未找到返回{@code null}
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
     * 获取数组中所有指定位置的元素值，组成新数组
     *
     * @param <T>     数组元素类型
     * @param array   数组，如果提供为{@code null}则返回{@code null}
     * @param indexes 下标列表
     * @return 指定位置的元素值数组
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
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param <T>         数组元素类型
     * @param array       数组
     * @param conjunction 分隔符
     * @return 连接后的字符串
     */
    public static <T> String join(final T[] array, final CharSequence conjunction) {
        return join(array, conjunction, null, null);
    }

    /**
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param <T>       数组元素类型
     * @param array     数组
     * @param delimiter 分隔符
     * @param prefix    每个元素添加的前缀，null表示不添加
     * @param suffix    每个元素添加的后缀，null表示不添加
     * @return 连接后的字符串
     */
    public static <T> String join(final T[] array, final CharSequence delimiter, final String prefix,
            final String suffix) {
        if (null == array) {
            return null;
        }

        return StringJoiner.of(delimiter, prefix, suffix)
                // 每个元素都添加前后缀
                .setWrapElement(true).append(array).toString();
    }

    /**
     * 先处理数组元素，再以 conjunction 为分隔符将数组转换为字符串
     *
     * @param <T>         数组元素类型
     * @param array       数组
     * @param conjunction 分隔符
     * @param editor      每个元素的编辑器，null表示不编辑
     * @return 连接后的字符串
     */
    public static <T> String join(final T[] array, final CharSequence conjunction, final UnaryOperator<T> editor) {
        return StringJoiner.of(conjunction).append(edit(array, editor)).toString();
    }

    /**
     * 以 conjunction 为分隔符将数组转换为字符串
     *
     * @param array       数组
     * @param conjunction 分隔符
     * @return 连接后的字符串
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
     * 移除数组中对应位置的元素 copier from commons-lang
     *
     * @param <T>   数组元素类型
     * @param array 数组对象，可以是对象数组，也可以原始类型数组
     * @param index 位置，如果位置小于0或者大于长度，返回原数组
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static <T> T[] remove(final T[] array, final int index) throws IllegalArgumentException {
        return (T[]) remove((Object) array, index);
    }

    /**
     * 移除数组中指定的元素 只会移除匹配到的第一个元素 copier from commons-lang
     *
     * @param <T>     数组元素类型
     * @param array   数组对象，可以是对象数组，也可以原始类型数组
     * @param element 要移除的元素
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static <T> T[] removeEle(final T[] array, final T element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param <T>                 数组元素类型
     * @param array               数组，会变更
     * @param startIndexInclusive 开始位置（包含）
     * @param endIndexExclusive   结束位置（不包含）
     * @return 变更后的原数组
     */
    public static <T> T[] reverse(final T[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
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
     * 反转数组，会变更原数组
     *
     * @param <T>   数组元素类型
     * @param array 数组，会变更
     * @return 变更后的原数组
     */
    public static <T> T[] reverse(final T[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * 取最小值
     *
     * @param <T>         元素类型
     * @param numberArray 数字数组
     * @return 最小值
     */
    public static <T extends Comparable<? super T>> T min(final T[] numberArray) {
        return min(numberArray, null);
    }

    /**
     * 取最小值
     *
     * @param <T>         元素类型
     * @param numberArray 数字数组
     * @param comparator  比较器，null按照默认比较
     * @return 最小值
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
     * 取最大值
     *
     * @param <T>         元素类型
     * @param numberArray 数字数组
     * @return 最大值
     */
    public static <T extends Comparable<? super T>> T max(final T[] numberArray) {
        return max(numberArray, null);
    }

    /**
     * 取最大值
     *
     * @param <T>         元素类型
     * @param numberArray 数字数组
     * @param comparator  比较器，null表示默认比较器
     * @return 最大值
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
     * 打乱数组顺序，会变更原数组 使用Fisher–Yates洗牌算法，以线性时间复杂度打乱数组顺序
     *
     * @param <T>   元素类型
     * @param array 数组，会变更
     * @return 打乱后的数组
     */
    public static <T> T[] shuffle(final T[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * 打乱数组顺序，会变更原数组 使用Fisher–Yates洗牌算法，以线性时间复杂度打乱数组顺序
     *
     * @param <T>    元素类型
     * @param array  数组，会变更
     * @param random 随机数生成器
     * @return 打乱后的数组
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
     * 交换数组中两个位置的值
     *
     * @param <T>    元素类型
     * @param array  数组
     * @param index1 位置1
     * @param index2 位置2
     * @return 交换后的数组，与传入数组为同一对象
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
     * 交换数组中两个位置的值
     *
     * @param array  数组对象
     * @param index1 位置1
     * @param index2 位置2
     * @return 交换后的数组，与传入数组为同一对象
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
     * 去重数组中的元素，去重后生成新的数组，原数组不变 此方法通过{@link LinkedHashSet} 去重
     *
     * @param <T>   数组元素类型
     * @param array 数组
     * @return 去重后的数组
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
     * 去重数组中的元素，去重后生成新的数组，原数组不变 此方法通过{@link LinkedHashSet} 去重
     *
     * @param <T>             数组元素类型
     * @param <K>             唯一键类型
     * @param array           数组
     * @param uniqueGenerator 唯一键生成器
     * @param override        是否覆盖模式，如果为{@code true}，加入的新值会覆盖相同key的旧值，否则会忽略新加值
     * @return 去重后的数组
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
     * 按照指定规则，将一种类型的数组转换为另一种类型
     *
     * @param array               被转换的数组
     * @param targetComponentType 标的元素类型，只能为包装类型
     * @param func                转换规则函数
     * @param <T>                 原数组类型
     * @param <R>                 目标数组类型
     * @return 转换后的数组
     */
    public static <T, R> R[] map(final Object array, final Class<R> targetComponentType,
            final Function<? super T, ? extends R> func) {
        final int length = length(array);
        final R[] result = newArray(targetComponentType, length);
        for (int i = 0; i < length; i++) {
            result[i] = func.apply(get(array, i));
        }
        return result;
    }

    /**
     * 按照指定规则，将一种类型的数组元素转换为另一种类型，并保存为 {@link List}
     *
     * @param array 被转换的数组
     * @param func  转换规则函数
     * @param <T>   原数组类型
     * @param <R>   目标数组类型
     * @return 列表
     */
    public static <T, R> List<R> mapToList(final T[] array, final Function<? super T, ? extends R> func) {
        return Arrays.stream(array).map(func).collect(Collectors.toList());
    }

    /**
     * 按照指定规则，将一种类型的数组元素转换为另一种类型，并保存为 {@link Set}
     *
     * @param array 被转换的数组
     * @param func  转换规则函数
     * @param <T>   原数组类型
     * @param <R>   目标数组类型
     * @return 集合
     */
    public static <T, R> Set<R> mapToSet(final T[] array, final Function<? super T, ? extends R> func) {
        return Arrays.stream(array).map(func).collect(Collectors.toSet());
    }

    /**
     * 按照指定规则，将一种类型的数组元素转换为另一种类型，并保存为 {@link Set}
     *
     * @param array     被转换的数组
     * @param func      转换规则函数
     * @param generator 数组生成器，如返回String[]，则传入String[]::new
     * @param <T>       原数组类型
     * @param <R>       目标数组类型
     * @return 集合
     */
    public static <T, R> R[] mapToArray(final T[] array, final Function<? super T, ? extends R> func,
            final IntFunction<R[]> generator) {
        return Arrays.stream(array).map(func).toArray(generator);
    }

    /**
     * 判断两个数组是否相等，判断依据包括数组长度和每个元素都相等。
     *
     * @param array1 数组1
     * @param array2 数组2
     * @return 是否相等
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
     * 获取子数组
     *
     * @param <T>   数组元素类型
     * @param array 数组，不允许为空
     * @param start 开始位置（包括）
     * @param end   结束位置（不包括）
     * @return 新的数组
     * @see Arrays#copyOfRange(Object[], int, int)
     */
    public static <T> T[] sub(final T[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = length(array);
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        if (start > end) {
            final int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length) {
            return newArray(array.getClass().getComponentType(), 0);
        }
        if (end > length) {
            end = length;
        }
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * 获取子数组
     *
     * @param array        数组
     * @param beginInclude 开始位置（包括）
     * @param endExclude   结束位置（不包括）
     * @param <A>          数组类型
     * @return 新的数组
     */
    public static <A> A sub(final A array, final int beginInclude, final int endExclude) {
        return ArrayWrapper.of(array).getSub(beginInclude, endExclude);
    }

    /**
     * 获取子数组
     *
     * @param array        数组
     * @param beginInclude 开始位置（包括）
     * @param endExclude   结束位置（不包括）
     * @param step         步进
     * @param <A>          数组类型
     * @return 新的数组
     */
    public static <A> A sub(final A array, final int beginInclude, final int endExclude, final int step) {
        return ArrayWrapper.of(array).getSub(beginInclude, endExclude, step);
    }

    /**
     * 查找最后一个子数组的开始位置
     *
     * @param array    数组
     * @param subArray 子数组
     * @param <T>      数组元素类型
     * @return 最后一个子数组的开始位置，即子数字第一个元素在数组中的位置
     */
    public static <T> int lastIndexOfSub(final T[] array, final T[] subArray) {
        if (isEmpty(array) || isEmpty(subArray)) {
            return Normal.__1;
        }
        return lastIndexOfSub(array, array.length - 1, subArray);
    }

    /**
     * 查找最后一个子数组的开始位置
     *
     * @param array      数组
     * @param endInclude 从后往前查找时的开始位置（包含）
     * @param subArray   子数组
     * @param <T>        数组元素类型
     * @return 最后一个子数组的开始位置，即从后往前，子数字第一个元素在数组中的位置
     */
    public static <T> int lastIndexOfSub(final T[] array, final int endInclude, final T[] subArray) {
        if (isEmpty(array) || isEmpty(subArray) || subArray.length > array.length || endInclude < 0) {
            return Normal.__1;
        }

        final int firstIndex = lastIndexOf(array, subArray[0], endInclude);
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
     * 判断数组中是否有相同元素
     * <p>
     * 若传入空数组，则返回{@code false}
     * </p>
     *
     * @param <T>   数组元素类型
     * @param array 数组
     * @return 数组是否有相同元素
     */
    public static <T> Boolean hasSameElement(final T[] array) {
        if (isEmpty(array)) {
            return false;
        }

        final Set<T> elementSet = SetKit.of(Arrays.asList(array));
        return elementSet.size() != array.length;
    }

    /**
     * array数组是否以prefix开头，每个元素的匹配使用{@link ObjectKit#equals(Object, Object)}匹配。
     * <ul>
     * <li>array和prefix为同一个数组（即array == prefix），返回{@code true}</li>
     * <li>array或prefix为空数组（null或length为0的数组），返回{@code true}</li>
     * <li>prefix长度大于array，返回{@code false}</li>
     * </ul>
     *
     * @param array  数组
     * @param prefix 前缀
     * @param <T>    数组元素类型
     * @return 是否开头
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
            if (ObjectKit.notEquals(array[i], prefix[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 比较给定的数组和{@code Arrays.equals}，根据数组元素而不是数组引用执行相等性检查
     *
     * @param o1 第一个比较对象
     * @param o2 第二个比较对象
     * @return 给定对象是否相等
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
