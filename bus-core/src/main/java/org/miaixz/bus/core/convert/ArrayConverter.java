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

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.*;

/**
 * 数组转换器，包括原始类型数组
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ArrayConverter extends AbstractConverter implements MatcherConverter {

    @Serial
    private static final long serialVersionUID = 2852263350173L;

    /**
     * 单例
     */
    public static final ArrayConverter INSTANCE = new ArrayConverter();

    /**
     * 是否忽略元素转换错误
     */
    private boolean ignoreElementError;

    /**
     * 构造
     */
    public ArrayConverter() {
        this(false);
    }

    /**
     * 构造
     *
     * @param ignoreElementError 是否忽略元素转换错误
     */
    public ArrayConverter(final boolean ignoreElementError) {
        this.ignoreElementError = ignoreElementError;
    }

    @Override
    protected Object convertInternal(final Class<?> targetClass, final Object value) {
        final Class<?> targetComponentType;
        if (targetClass.isArray()) {
            targetComponentType = targetClass.getComponentType();
        } else {
            // 用户传入类为非数组时，按照数组元素类型对待
            targetComponentType = targetClass;
        }

        return value.getClass().isArray() ? convertArrayToArray(targetComponentType, value)
                : convertObjectToArray(targetComponentType, value);
    }

    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return rawType.isArray();
    }

    /**
     * 设置是否忽略元素转换错误
     *
     * @param ignoreElementError 是否忽略元素转换错误
     */
    public void setIgnoreElementError(final boolean ignoreElementError) {
        this.ignoreElementError = ignoreElementError;
    }

    /**
     * 数组对数组转换
     *
     * @param array 被转换的数组值
     * @return 转换后的数组
     */
    private Object convertArrayToArray(final Class<?> targetComponentType, final Object array) {
        final Class<?> valueComponentType = ArrayKit.getComponentType(array);

        if (valueComponentType == targetComponentType) {
            return array;
        }

        final int len = ArrayKit.length(array);
        final Object result = Array.newInstance(targetComponentType, len);

        for (int i = 0; i < len; i++) {
            Array.set(result, i, convertComponentType(targetComponentType, Array.get(array, i)));
        }
        return result;
    }

    /**
     * 非数组对数组转换
     *
     * @param targetComponentType 目标单个节点类型
     * @param value               被转换值
     * @return 转换后的数组
     */
    private Object convertObjectToArray(final Class<?> targetComponentType, Object value) {
        if (value instanceof CharSequence) {
            if (targetComponentType == char.class || targetComponentType == Character.class) {
                return convertArrayToArray(targetComponentType, value.toString().toCharArray());
            }

            // 字符串转bytes，首先判断是否为Base64，是则转换，否则按照默认getBytes方法。
            if (targetComponentType == byte.class) {
                final String text = value.toString();
                if (Base64.isTypeBase64(text)) {
                    return Base64.decode(value.toString());
                }
                return text.getBytes();
            }

            // 单纯字符串情况下按照逗号分隔后劈开
            final String[] strings = StringKit.splitToArray(value.toString(), Symbol.COMMA);
            return convertArrayToArray(targetComponentType, strings);
        }

        if (value instanceof Iterator) {
            value = IteratorKit.asIterable((Iterator<?>) value);
        }

        final Object result;
        if (value instanceof Iterable) {
            result = convertIterableToArray(targetComponentType, (Iterable<?>) value);
        } else if (value instanceof Number && byte.class == targetComponentType) {
            // 用户可能想序列化指定对象
            result = ByteKit.toBytes((Number) value);
        } else if (value instanceof Serializable && byte.class == targetComponentType) {
            // 用户可能想序列化指定对象
            result = SerializeKit.serialize(value);
        } else {
            // everything else:
            result = convertToSingleElementArray(targetComponentType, value);
        }

        return result;
    }

    /**
     * 迭代器转数组
     *
     * @param targetComponentType 目标单个节点类型
     * @param value               迭代器实现值
     * @return 数组
     */
    private Object convertIterableToArray(final Class<?> targetComponentType, final Iterable<?> value) {
        final Object result;
        if (value instanceof List) {
            // List转数组
            final List<?> list = (List<?>) value;
            final int size = list.size();
            result = Array.newInstance(targetComponentType, size);
            for (int i = 0; i < size; i++) {
                Array.set(result, i, convertComponentType(targetComponentType, list.get(i)));
            }
        } else if (value instanceof Collection) {
            // 集合转数组
            final Collection<?> collection = (Collection<?>) value;
            result = Array.newInstance(targetComponentType, collection.size());

            int i = 0;
            for (final Object element : collection) {
                Array.set(result, i, convertComponentType(targetComponentType, element));
                i++;
            }
        } else {
            // 可循环对象转数组，可循环对象无法获取长度，因此先转为List后转为数组
            final List<?> list = ListKit.of(value);
            final int size = list.size();
            result = Array.newInstance(targetComponentType, size);
            for (int i = 0; i < size; i++) {
                Array.set(result, i, convertComponentType(targetComponentType, list.get(i)));
            }
        }
        return result;
    }

    /**
     * 单元素数组
     *
     * @param value 被转换的值
     * @return 数组，只包含一个元素
     */
    private Object[] convertToSingleElementArray(final Class<?> targetComponentType, final Object value) {
        final Object[] singleElementArray = ArrayKit.newArray(targetComponentType, 1);
        singleElementArray[0] = convertComponentType(targetComponentType, value);
        return singleElementArray;
    }

    /**
     * 转换元素类型
     *
     * @param value 值
     * @return 转换后的值，转换失败若{@link #ignoreElementError}为true，返回null，否则抛出异常
     */
    private Object convertComponentType(final Class<?> targetComponentType, final Object value) {
        return Convert.convertWithCheck(targetComponentType, value, null, this.ignoreElementError);
    }

}
