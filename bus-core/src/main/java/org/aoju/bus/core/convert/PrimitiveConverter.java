/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.convert;

import org.aoju.bus.core.lang.exception.ConvertException;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.util.function.Function;

/**
 * 原始类型转换器
 * 支持类型为：
 * <ul>
 * <li><code>byte</code></li>
 * <li><code>short</code></li>
 * <li><code>int</code></li>
 * <li><code>long</code></li>
 * <li><code>float</code></li>
 * <li><code>double</code></li>
 * <li><code>char</code></li>
 * <li><code>boolean</code></li>
 * </ul>
 *
 * @author Kimi Liu
 * @version 6.3.2
 * @since JDK 1.8+
 */
public class PrimitiveConverter extends AbstractConverter<Object> {

    private static final long serialVersionUID = 1L;

    /**
     * 目标类型
     */
    private final Class<?> targetType;

    /**
     * 构造
     *
     * @param clazz 需要转换的原始
     * @throws IllegalArgumentException 传入的转换类型非原始类型时抛出
     */
    public PrimitiveConverter(Class<?> clazz) {
        if (null == clazz) {
            throw new NullPointerException("PrimitiveConverter not allow null target type!");
        } else if (false == clazz.isPrimitive()) {
            throw new IllegalArgumentException("[" + clazz + "] is not a primitive class!");
        }
        this.targetType = clazz;
    }

    @Override
    protected String convertString(Object value) {
        return StringKit.trim(super.convertString(value));
    }

    @Override
    public Class<Object> getTargetType() {
        return (Class<Object>) this.targetType;
    }

    @Override
    protected Object convertInternal(Object value) {
        return PrimitiveConverter.convert(value, this.targetType, this::convertString);
    }

    /**
     * 将指定值转换为原始类型的值
     *
     * @param value          值
     * @param primitiveClass 原始类型
     * @param toStringFunc   当无法直接转换时，转为字符串后再转换的函数
     * @return 转换结果
     */
    protected static Object convert(Object value, Class<?> primitiveClass, Function<Object, String> toStringFunc) {
        if (byte.class == primitiveClass) {
            return ObjectKit.defaultIfNull(NumberConverter.convert(value, Byte.class, toStringFunc), 0);
        } else if (short.class == primitiveClass) {
            return ObjectKit.defaultIfNull(NumberConverter.convert(value, Short.class, toStringFunc), 0);
        } else if (int.class == primitiveClass) {
            return ObjectKit.defaultIfNull(NumberConverter.convert(value, Integer.class, toStringFunc), 0);
        } else if (long.class == primitiveClass) {
            return ObjectKit.defaultIfNull(NumberConverter.convert(value, Long.class, toStringFunc), 0);
        } else if (float.class == primitiveClass) {
            return ObjectKit.defaultIfNull(NumberConverter.convert(value, Float.class, toStringFunc), 0);
        } else if (double.class == primitiveClass) {
            return ObjectKit.defaultIfNull(NumberConverter.convert(value, Double.class, toStringFunc), 0);
        } else if (char.class == primitiveClass) {
            return Convert.convert(Character.class, value);
        } else if (boolean.class == primitiveClass) {
            return Convert.convert(Boolean.class, value);
        }

        throw new ConvertException("Unsupported target type: {}", primitiveClass);
    }

}
