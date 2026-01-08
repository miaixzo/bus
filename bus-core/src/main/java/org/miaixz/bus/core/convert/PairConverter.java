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
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * Converter for {@link Pair} objects, supports the following types to convert to Pair:
 * <ul>
 * <li>{@link Map}</li>
 * <li>{@link Map.Entry}</li>
 * <li>String with separators, supports separators {@code :}, {@code =}, {@code ,}</li>
 * <li>Bean objects with {@code getLeft} and {@code getRight} methods</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PairConverter extends ConverterWithRoot implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852270287618L;

    /**
     * Constructs a new PairConverter
     *
     * @param rootConverter the root converter for converting Pair values
     */
    public PairConverter(final Converter rootConverter) {
        super(rootConverter);
    }

    /**
     * Converts string to single key-value Map, supporting separators {@code :}, {@code =}, {@code ,}
     *
     * @param text the string
     * @return map or null
     */
    private static Map<CharSequence, CharSequence> strToMap(final CharSequence text) {
        // data:value data=value data,value
        final int index = StringKit.indexOf(
                text,
                c -> c == Symbol.C_COLON || c == Symbol.C_EQUAL || c == Symbol.C_COMMA,
                0,
                text.length());

        if (index > -1) {
            return MapKit.of(text.subSequence(0, index), text.subSequence(index + 1, text.length()));
        }
        return null;
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
        final Type rightType = TypeKit.getTypeArgument(targetType, 1);

        return convert(leftType, rightType, value);
    }

    /**
     * Converts Map to Pair
     *
     * @param keyType   the key type
     * @param valueType the value type
     * @param map       the map to convert
     * @return the Pair
     */
    private Pair<?, ?> mapToPair(final Type keyType, final Type valueType, final Map map) {
        final Object left;
        final Object right;
        if (1 == map.size()) {
            final Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();
            left = entry.getKey();
            right = entry.getValue();
        } else {
            // Ignore other properties in Map
            left = map.get("left");
            right = map.get("right");
        }

        return Pair.of(
                TypeKit.isUnknown(keyType) ? left : converter.convert(keyType, left),
                TypeKit.isUnknown(valueType) ? right : converter.convert(valueType, right));
    }

    /**
     * Converts an object to Map with specified key and value types
     *
     * @param leftType  the key type
     * @param rightType the value type
     * @param value     the value to convert
     * @return the converted Map
     * @throws ConvertException if conversion fails or type is unsupported
     */
    public Pair<?, ?> convert(final Type leftType, final Type rightType, final Object value) throws ConvertException {
        Map map = null;
        if (value instanceof Map.Entry entry) {
            map = MapKit.of(entry.getKey(), entry.getValue());
        } else if (value instanceof Pair) {
            final Pair entry = (Pair<?, ?>) value;
            map = MapKit.of(entry.getLeft(), entry.getRight());
        } else if (value instanceof Map) {
            map = (Map) value;
        } else if (value instanceof CharSequence) {
            map = strToMap((CharSequence) value);
        } else if (BeanKit.isReadableBean(value.getClass())) {
            map = BeanKit.toBeanMap(value);
        }

        if (null != map) {
            return mapToPair(leftType, rightType, map);
        }

        throw new ConvertException("Unsupported to map from [{}] of type: {}", value, value.getClass().getName());
    }

}
