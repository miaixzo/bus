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
import java.util.Map;

import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * Converter for {@link Triplet} objects, supports the following types to convert to Triplet:
 * <ul>
 * <li>Bean objects with {@code getLeft}, {@code getMiddle}, and {@code getRight} methods</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TripletConverter extends ConverterWithRoot implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852272275571L;

    /**
     * Constructs a new TripletConverter
     *
     * @param rootConverter the root converter for converting unrecognized objects
     */
    public TripletConverter(final Converter rootConverter) {
        super(rootConverter);
    }

    /**
     * Converts Map to Triplet
     *
     * @param leftType  the key type
     * @param rightType the value type
     * @param map       the map to convert
     * @return the Triplet
     */
    private Triplet<?, ?, ?> mapToTriple(
            final Type leftType,
            final Type middleType,
            final Type rightType,
            final Map map) {

        final Object left = map.get("left");
        final Object middle = map.get("middle");
        final Object right = map.get("right");

        return Triplet.of(
                TypeKit.isUnknown(leftType) ? left : converter.convert(leftType, left),
                TypeKit.isUnknown(middleType) ? middle : converter.convert(middleType, middle),
                TypeKit.isUnknown(rightType) ? right : converter.convert(rightType, right));
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
        final Type leftType = TypeKit.getTypeArgument(targetType, 0);
        final Type middileType = TypeKit.getTypeArgument(targetType, 1);
        final Type rightType = TypeKit.getTypeArgument(targetType, 2);

        return convert(leftType, middileType, rightType, value);
    }

    /**
     * Converts an object to Map with specified key and value types
     *
     * @param leftType   the key type
     * @param middleType the middle value type
     * @param rightType  the value type
     * @param value      the value to convert
     * @return the converted Map
     * @throws ConvertException if conversion fails or type is unsupported
     */
    public Triplet<?, ?, ?> convert(
            final Type leftType,
            final Type middleType,
            final Type rightType,
            final Object value) throws ConvertException {
        Map map = null;
        if (value instanceof Map) {
            map = (Map) value;
        } else if (BeanKit.isReadableBean(value.getClass())) {
            // For one-time read-only scenarios, wrapping as Map is more efficient
            map = BeanKit.toBeanMap(value);
        }

        if (null != map) {
            return mapToTriple(leftType, middleType, rightType, map);
        }

        throw new ConvertException("Unsupported to map from [{}] of type: {}", value, value.getClass().getName());
    }

}
