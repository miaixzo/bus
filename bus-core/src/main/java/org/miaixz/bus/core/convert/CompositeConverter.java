/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * A composite converter that aggregates all supported default and custom conversion rules.
 * <p>
 * The conversion process follows a specific chain of responsibility:
 * <ol>
 * <li>Handle {@code null} and {@link Optional} values.</li>
 * <li>Attempt conversion using custom {@link MatcherConverter} instances.</li>
 * <li>Attempt conversion using custom type-specific converters.</li>
 * <li>Attempt conversion using pre-registered standard converters (e.g., for primitives).</li>
 * <li>Attempt conversion using special converters (e.g., for {@link java.util.Map}, {@link java.util.Collection},
 * {@link Enum}).</li>
 * <li>Finally, attempt conversion to a JavaBean.</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompositeConverter implements Converter, Serializable {

    @Serial
    private static final long serialVersionUID = 2852267583368L;

    /**
     * Manages registered standard and custom converters.
     */
    private RegisterConverter registerConverter;
    /**
     * Handles conversion for special types like collections, maps, and arrays.
     */
    private SpecialConverter specialConverter;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private CompositeConverter() {

    }

    /**
     * Returns the singleton instance of {@code CompositeConverter}.
     *
     * @return The singleton {@code CompositeConverter} instance.
     */
    public static CompositeConverter getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Registers a custom {@link MatcherConverter}. If the converter's {@code match} method returns {@code true}, it
     * will be used for conversion.
     * <p>
     * <strong>Warning:</strong> Since this is a singleton, this registration is global.
     * 
     *
     * @param converter The {@link MatcherConverter} to register.
     * @return This {@code CompositeConverter} instance.
     */
    public CompositeConverter register(final MatcherConverter converter) {
        registerConverter.register(converter);
        return this;
    }

    /**
     * Registers a custom converter for a specific target type.
     * <p>
     * <strong>Warning:</strong> Since this is a singleton, this registration is global.
     * 
     *
     * @param type      The target type for which this converter will be used.
     * @param converter The {@link Converter} to register.
     * @return This {@code CompositeConverter} instance.
     */
    public CompositeConverter register(final Type type, final Converter converter) {
        registerConverter.register(type, converter);
        return this;
    }

    /**
     * Converts the given value to the specified type.
     *
     * @param type  The target type.
     * @param value The value to convert.
     * @return The converted value.
     * @throws ConvertException if no suitable converter is found.
     */
    @Override
    public Object convert(final Type type, final Object value) throws ConvertException {
        return convert(type, value, null);
    }

    /**
     * Converts the given value to the specified type, returning a default value if conversion is not possible.
     *
     * @param <T>          The target type.
     * @param type         The target type.
     * @param value        The value to convert.
     * @param defaultValue The default value to return if conversion fails or the input is {@code null}.
     * @return The converted value, or the default value.
     * @throws ConvertException if conversion fails and no default value is provided.
     */
    @Override
    public <T> T convert(final Type type, final Object value, final T defaultValue) throws ConvertException {
        return convert(type, value, defaultValue, true);
    }

    /**
     * Converts the given value to the specified type, with control over converter priority.
     *
     * @param <T>           The target type.
     * @param type          The target type.
     * @param value         The value to be converted.
     * @param defaultValue  The default value to return on failure.
     * @param isCustomFirst If {@code true}, custom converters are given priority over default converters.
     * @return The converted value.
     * @throws ConvertException if no suitable converter is found.
     */
    public <T> T convert(Type type, Object value, final T defaultValue, final boolean isCustomFirst)
            throws ConvertException {
        if (ObjectKit.isNull(value)) {
            return defaultValue;
        }
        if (TypeKit.isUnknown(type)) {
            if (null == defaultValue) {
                return (T) value; // Return original value if target type is unknown and no default is set.
            }
            type = defaultValue.getClass();
        }

        // Unwrap Optional types
        if (value instanceof Optional<?>) {
            value = ((Optional<T>) value).getOrNull();
        }
        if (value instanceof java.util.Optional) {
            value = ((Optional<T>) value).orElse(null);
        }
        if (ObjectKit.isNull(value)) {
            return defaultValue;
        }

        // If the value is a Converter itself, use it.
        if (value instanceof Converter) {
            return ((Converter) value).convert(type, value, defaultValue);
        }

        if (type instanceof TypeReference) {
            type = ((TypeReference<?>) type).getType();
        }

        // Find and apply a suitable standard or custom converter.
        final Converter converter = registerConverter.getConverter(type, value, isCustomFirst);
        if (null != converter) {
            return converter.convert(type, value, defaultValue);
        }

        Class<T> rawType = (Class<T>) TypeKit.getClass(type);
        if (null == rawType) {
            if (null != defaultValue) {
                rawType = (Class<T>) defaultValue.getClass();
            } else {
                throw new ConvertException("Cannot determine raw class from type: {}", type);
            }
        }

        // Attempt conversion for special types (Collection, Map, Array, etc.).
        final T result = (T) specialConverter.convert(type, rawType, value);
        if (null != result) {
            return result;
        }

        // Attempt to convert to a JavaBean.
        if (BeanKit.isWritableBean(rawType)) {
            return (T) BeanConverter.INSTANCE.convert(type, value);
        }

        throw new ConvertException("No suitable converter found for from {}: [{}] to [{}]", value.getClass().getName(),
                value, type.getTypeName());
    }

    /**
     * A static inner class that holds the singleton instance, ensuring lazy initialization and thread safety.
     */
    private static class SingletonHolder {

        /**
         * The singleton instance, initialized on first access.
         */
        private static final CompositeConverter INSTANCE;
        static {
            INSTANCE = new CompositeConverter();
            INSTANCE.registerConverter = new RegisterConverter(INSTANCE);
            INSTANCE.specialConverter = new SpecialConverter(INSTANCE);
        }
    }

}
