/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Type;

import org.miaixz.bus.core.lang.exception.ConvertException;

/**
 * Converter for primitive types. Supported types:
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
 * @since Java 21+
 */
public class PrimitiveConverter extends AbstractConverter implements MatcherConverter, Serializable {

    @Serial
    private static final long serialVersionUID = 2852271020075L;

    /**
     * Singleton instance
     */
    public static final PrimitiveConverter INSTANCE = new PrimitiveConverter();

    /**
     * Checks if this converter can handle the conversion to the specified target type.
     *
     * @param targetType the target type
     * @param rawType    the raw class of the target type
     * @param value      the value to be converted
     * @return {@code true} if the raw type is a primitive type
     */
    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return rawType.isPrimitive();
    }

    /**
     * Converts the given value to a primitive type.
     * <p>
     * Delegates to the appropriate wrapper type converter (NumberConverter, CharacterConverter, BooleanConverter).
     * </p>
     *
     * @param primitiveClass the primitive class to convert to
     * @param value          the value to convert
     * @return the converted primitive value (as wrapper type)
     * @throws ConvertException if the target type is not supported or conversion fails
     */
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
