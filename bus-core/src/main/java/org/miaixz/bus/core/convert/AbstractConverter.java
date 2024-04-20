/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.convert;

import org.miaixz.bus.core.exception.ConvertException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.CharsKit;
import org.miaixz.bus.core.toolkit.TypeKit;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * 抽象转换器,提供通用的转换逻辑,同时通过convertInternal实现对应类型的专属逻辑
 * 转换器不会抛出转换异常,转换失败时会返回{@code null}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public Object convert(final Type targetType, final Object value) {
        if (null == value) {
            return null;
        }
        if (TypeKit.isUnknown(targetType)) {
            throw new ConvertException("Unsupported convert to unKnow type: {}", targetType);
        }

        final Class<?> targetClass = TypeKit.getClass(targetType);
        Assert.notNull(targetClass, "Target type is not a class!");

        // 尝试强转
        if (targetClass.isInstance(value) && !Map.class.isAssignableFrom(targetClass)) {
            // 除Map外，已经是目标类型，不需要转换（Map类型涉及参数类型，需要单独转换）
            return Assert.notNull(targetClass).cast(value);
        }
        return convertInternal(targetClass, value);
    }

    /**
     * 内部转换器，被 {@link AbstractConverter#convert(Type, Object)} 调用，实现基本转换逻辑<br>
     * 内部转换器转换后如果转换失败可以做如下操作，处理结果都为返回默认值：
     *
     * <pre>
     * 1、返回{@code null}
     * 2、抛出一个{@link RuntimeException}异常
     * </pre>
     *
     * @param targetClass 目标类型
     * @param value       值
     * @return 转换后的类型
     */
    protected abstract Object convertInternal(Class<?> targetClass, Object value);

    /**
     * 值转为String，用于内部转换中需要使用String中转的情况<br>
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
    protected String convertToString(final Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof CharSequence) {
            return value.toString();
        } else if (ArrayKit.isArray(value)) {
            return ArrayKit.toString(value);
        } else if (CharsKit.isChar(value)) {
            // 对于ASCII字符使用缓存加速转换，减少空间创建
            return CharsKit.toString((char) value);
        }
        return value.toString();
    }

}
