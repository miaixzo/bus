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
package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.convert.ConverterRegistry;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.IterUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 数组转换器，包括原始类型数组
 *
 * @author Kimi Liu
 * @version 3.5.1
 * @since JDK 1.8
 */
public class ArrayConverter extends AbstractConverter<Object> {

    private final Class<?> targetType;
    /**
     * 目标元素类型
     */
    private final Class<?> targetComponentType;

    /**
     * 构造
     *
     * @param targetType 目标数组类型
     */
    public ArrayConverter(Class<?> targetType) {
        if (null == targetType) {
            // 默认Object数组
            targetType = Object[].class;
        } else {
            Assert.isTrue(targetType.isArray(), "Target type must be a array!");
        }
        this.targetType = targetType;
        this.targetComponentType = targetType.getComponentType();
    }

    @Override
    protected Object convertInternal(Object value) {
        return value.getClass().isArray() ? convertArrayToArray(value) : convertObjectToArray(value);
    }

    @Override
    public Class getTargetType() {
        return this.targetType;
    }

    /**
     * 数组对数组转换
     *
     * @param array 被转换的数组值
     * @return 转换后的数组
     */
    private Object convertArrayToArray(Object array) {
        final Class<?> valueComponentType = ArrayUtils.getComponentType(array);

        if (valueComponentType == targetComponentType) {
            return array;
        }

        final int len = ArrayUtils.length(array);
        final Object result = Array.newInstance(targetComponentType, len);

        final ConverterRegistry converter = ConverterRegistry.getInstance();
        for (int i = 0; i < len; i++) {
            Array.set(result, i, converter.convert(targetComponentType, Array.get(array, i)));
        }
        return result;
    }

    /**
     * 非数组对数组转换
     *
     * @param value 被转换值
     * @return 转换后的数组
     */
    private Object convertObjectToArray(Object value) {
        if (value instanceof CharSequence) {
            if (targetComponentType == char.class || targetComponentType == Character.class) {
                return convertArrayToArray(value.toString().toCharArray());
            }

            // 单纯字符串情况下按照逗号分隔后劈开
            final String[] strings = StringUtils.split(value.toString(), Symbol.COMMA);
            return convertArrayToArray(strings);
        }

        final ConverterRegistry converter = ConverterRegistry.getInstance();
        Object result = null;
        if (value instanceof List) {
            // List转数组
            final List<?> list = (List<?>) value;
            result = Array.newInstance(targetComponentType, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(result, i, converter.convert(targetComponentType, list.get(i)));
            }
        } else if (value instanceof Collection) {
            // 集合转数组
            final Collection<?> collection = (Collection<?>) value;
            result = Array.newInstance(targetComponentType, collection.size());

            int i = 0;
            for (Object element : collection) {
                Array.set(result, i, converter.convert(targetComponentType, element));
                i++;
            }
        } else if (value instanceof Iterable) {
            // 可循环对象转数组，可循环对象无法获取长度，因此先转为List后转为数组
            final List<?> list = IterUtils.toList((Iterable<?>) value);
            result = Array.newInstance(targetComponentType, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(result, i, converter.convert(targetComponentType, list.get(i)));
            }
        } else if (value instanceof Iterator) {
            // 可循环对象转数组，可循环对象无法获取长度，因此先转为List后转为数组
            final List<?> list = IterUtils.toList((Iterator<?>) value);
            result = Array.newInstance(targetComponentType, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(result, i, converter.convert(targetComponentType, list.get(i)));
            }
        } else {
            // everything else:
            result = convertToSingleElementArray(value);
        }

        return result;
    }

    /**
     * 单元素数组
     *
     * @param value 被转换的值
     * @return 数组，只包含一个元素
     */
    private Object[] convertToSingleElementArray(Object value) {
        final Object[] singleElementArray = ArrayUtils.newArray(targetComponentType, 1);
        singleElementArray[0] = ConverterRegistry.getInstance().convert(targetComponentType, value);
        return singleElementArray;
    }

}
