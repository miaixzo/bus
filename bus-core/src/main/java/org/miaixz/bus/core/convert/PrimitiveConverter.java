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
import java.lang.reflect.Type;

import org.miaixz.bus.core.lang.exception.ConvertException;

/**
 * 原始类型转换器 支持类型为：
 * <ul>
 * <li>{@code byte}</li>
 * <li>{@code short}</li>
 * <li>{@code int}</li>
 * <li>{@code long}</li>
 * <li>{@code float}</li>
 * <li>{@code double}</li>
 * <li>{@code char}</li>
 * <li>{@code boolean}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrimitiveConverter extends AbstractConverter implements MatcherConverter, Serializable {

    @Serial
    private static final long serialVersionUID = 2852279552707L;

    /**
     * 单例对象
     */
    public static final PrimitiveConverter INSTANCE = new PrimitiveConverter();

    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return rawType.isPrimitive();
    }

    @Override
    protected Object convertInternal(final Class<?> primitiveClass, final Object value) {
        final Object result;
        if (byte.class == primitiveClass) {
            result = NumberConverter.INSTANCE.convert(Byte.class, value);
        } else if (short.class == primitiveClass) {
            result = NumberConverter.INSTANCE.convert(Short.class, value);
        } else if (int.class == primitiveClass) {
            result = NumberConverter.INSTANCE.convert(Integer.class, value);
        } else if (long.class == primitiveClass) {
            result = NumberConverter.INSTANCE.convert(Long.class, value);
        } else if (float.class == primitiveClass) {
            result = NumberConverter.INSTANCE.convert(Float.class, value);
        } else if (double.class == primitiveClass) {
            result = NumberConverter.INSTANCE.convert(Double.class, value);
        } else if (char.class == primitiveClass) {
            result = CharacterConverter.INSTANCE.convert(Character.class, value);
        } else if (boolean.class == primitiveClass) {
            result = BooleanConverter.INSTANCE.convert(Boolean.class, value);
        } else {
            throw new ConvertException("Unsupported target type: {}", primitiveClass);
        }

        if (null == result) {
            throw new ConvertException("Can not support {} to {}", value, primitiveClass);
        }

        return result;
    }

}
