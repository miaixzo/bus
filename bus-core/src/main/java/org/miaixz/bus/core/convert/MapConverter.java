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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * {@link Map} converter that implements:
 * <ul>
 * <li>Map to Map conversion with automatic key and value type conversion</li>
 * <li>Bean to Map conversion with automatic field and value type conversion</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapConverter extends ConverterWithRoot implements MatcherConverter, Serializable {

    @Serial
    private static final long serialVersionUID = 2852269171705L;

    /**
     * Singleton instance
     */
    public static final MapConverter INSTANCE = new MapConverter(CompositeConverter.getInstance());

    /**
     * Constructs a new MapConverter
     *
     * @param rootConverter the root converter for converting map entry values, must be non-{@code null}
     */
    public MapConverter(final Converter rootConverter) {
        super(rootConverter);
    }

    /**
     * Match method.
     *
     * @return the boolean value
     */
    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return Map.class.isAssignableFrom(rawType);
    }

    /**
     * Convert method.
     *
     * @return the Object value
     */
    @Override
    public Object convert(Type targetType, final Object value) throws ConvertException {
        if (targetType instanceof TypeReference) {
            targetType = ((TypeReference<?>) targetType).getType();
        }
        final Type keyType = TypeKit.getTypeArgument(targetType, 0);
        final Type valueType = TypeKit.getTypeArgument(targetType, 1);

        return convert(targetType, keyType, valueType, value);
    }

    /**
     * Converts an object to a Map with specified key and value types
     *
     * @param targetType the target Map type
     * @param keyType    the key type
     * @param valueType  the value type
     * @param value      the value to convert
     * @return the converted Map
     * @throws ConvertException if conversion fails or type is unsupported
     */
    public Map<?, ?> convert(final Type targetType, final Type keyType, final Type valueType, final Object value)
            throws ConvertException {
        Map map;
        if (value instanceof Map) {
            final Class<?> valueClass = value.getClass();
            if (valueClass.equals(targetType)) {
                final Type[] typeArguments = TypeKit.getTypeArguments(valueClass);
                if (null != typeArguments && 2 == typeArguments.length && Objects.equals(keyType, typeArguments[0])
                        && Objects.equals(valueType, typeArguments[1])) {
                    // For Map objects with matching key-value types, return directly without conversion
                    return (Map) value;
                }
            }

            map = MapKit.createMap(TypeKit.getClass(targetType), LinkedHashMap::new);
            convertMapToMap(keyType, valueType, (Map) value, map);
        } else if (BeanKit.isWritableBean(value.getClass())) {
            map = BeanKit.beanToMap(value);
            // Second conversion to convert key-value types
            map = convert(targetType, keyType, valueType, map);
        } else {
            throw new ConvertException("Unsupported to map from [{}] of type: {}", value, value.getClass().getName());
        }
        return map;
    }

    /**
     * Converts Map to Map
     *
     * @param srcMap    the source Map
     * @param targetMap the target Map
     */
    private void convertMapToMap(
            final Type keyType,
            final Type valueType,
            final Map<?, ?> srcMap,
            final Map targetMap) {
        srcMap.forEach(
                (key, value) -> targetMap.put(
                        TypeKit.isUnknown(keyType) ? key : converter.convert(keyType, key),
                        TypeKit.isUnknown(valueType) ? value : converter.convert(valueType, value)));
    }

}
