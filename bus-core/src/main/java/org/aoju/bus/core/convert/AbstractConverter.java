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
package org.aoju.bus.core.convert;

import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.CharUtils;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.util.Map;

/**
 * 抽象转换器，提供通用的转换逻辑，同时通过convertInternal实现对应类型的专属逻辑
 * 转换器不会抛出转换异常，转换失败时会返回{@code null}
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public abstract class AbstractConverter<T> implements Converter<T> {

    @Override
    public T convert(Object value, T defaultValue) {
        Class<T> targetType = getTargetType();
        if (null == targetType && null == defaultValue) {
            throw new NullPointerException(StringUtils.format("[type] and [defaultValue] are both null for Converter [{}], we can not know what type to convert !", this.getClass().getName()));
        }
        if (null == targetType) {
            targetType = (Class<T>) defaultValue.getClass();
        }
        if (null == value) {
            return defaultValue;
        }

        if (null == defaultValue || targetType.isInstance(defaultValue)) {
            if (targetType.isInstance(value) && false == Map.class.isAssignableFrom(targetType)) {
                // 除Map外，已经是目标类型，不需要转换（Map类型涉及参数类型，需要单独转换）
                return targetType.cast(value);
            }
            T result;
            try {
                result = convertInternal(value);
            } catch (RuntimeException e) {
                return defaultValue;
            }
            return ((null == result) ? defaultValue : result);
        } else {
            throw new IllegalArgumentException(StringUtils.format("Default value [{}] is not the instance of [{}]", defaultValue, targetType));
        }
    }

    /**
     * 不抛异常转换<br>
     * 当转换失败时返回默认值
     *
     * @param value        被转换的值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    public T convertQuietly(Object value, T defaultValue) {
        try {
            return convert(value, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 内部转换器，被 {@link AbstractConverter#convert(Object, Object)} 调用，实现基本转换逻辑
     * 内部转换器转换后如果转换失败可以做如下操作，处理结果都为返回默认值：
     *
     * <pre>
     * 1、返回{@code null}
     * 2、抛出一个{@link RuntimeException}异常
     * </pre>
     *
     * @param value 值
     * @return 转换后的类型
     */
    protected abstract T convertInternal(Object value);

    /**
     * 值转为String
     * 转换规则为：
     *
     * <pre>
     * 1、字符串类型将被强转
     * 2、数组将被转换为逗号分隔的字符串
     * 3、其它类型将调用默认的toString()方法
     * </pre>
     *
     * @param value 值
     * @return String
     */
    protected String convertToStr(Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        } else if (ArrayUtils.isArray(value)) {
            return ArrayUtils.toString(value);
        } else if (CharUtils.isChar(value)) {
            return CharUtils.toString((char) value);
        }
        return value.toString();
    }

    /**
     * 获得此类实现类的泛型类型
     *
     * @return 此类的泛型类型，可能为{@code null}
     */
    public Class<T> getTargetType() {
        return (Class<T>) ClassUtils.getTypeArgument(getClass());
    }

}
