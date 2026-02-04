/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.Wrapper;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.*;

/**
 * Converter for {@link Map.Entry} objects, supports the following types to convert to Entry:
 * <ul>
 * <li>{@link Map}</li>
 * <li>{@link Map.Entry}</li>
 * <li>String with separators, supports separators {@code :}, {@code =}, {@code ,}</li>
 * <li>Bean objects with {@code getKey} and {@code getValue} methods</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EntryConverter extends ConverterWithRoot implements MatcherConverter, Serializable {

    @Serial
    private static final long serialVersionUID = 2852268537319L;

    /**
     * Constructs a new EntryConverter
     *
     * @param converter the converter for converting Entry key and value to specified type objects
     */
    public EntryConverter(final Converter converter) {
        super(converter);
    }

    /**
     * Converts string to single key-value Map, supporting separators {@code :}, {@code =}, {@code ,}
     *
     * @param text the string
     * @return map or null
     */
    private static Map<CharSequence, CharSequence> strToMap(final CharSequence text) {
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
        final Type keyType = TypeKit.getTypeArgument(targetType, 0);
        final Type valueType = TypeKit.getTypeArgument(targetType, 1);

        return convert(targetType, keyType, valueType, value);
    }

    /**
     * Converts an object to Map with specified key and value types
     *
     * @param targetType the target Map type
     * @param keyType    the key type
     * @param valueType  the value type
     * @param value      the value to convert
     * @return the converted Map
     * @throws ConvertException if conversion fails or type is unsupported
     */
    public Map.Entry<?, ?> convert(final Type targetType, final Type keyType, final Type valueType, final Object value)
            throws ConvertException {
        Map map = null;
        if (value instanceof Map.Entry entry) {
            map = MapKit.of(entry.getKey(), entry.getValue());
        } else if (value instanceof Pair) {
            final Pair entry = (Pair<?, ?>) value;
            map = MapKit.of(entry.getLeft(), entry.getRight());
        } else if (value instanceof Map) {
            map = (Map) value;
        } else if (value instanceof CharSequence text) {
            map = strToMap(text);
        } else if (BeanKit.isWritableBean(value.getClass())) {
            map = BeanKit.toBeanMap(value);
        }

        if (null != map) {
            return mapToEntry(targetType, keyType, valueType, map);
        }

        throw new ConvertException("Unsupported to map from [{}] of type: {}", value, value.getClass().getName());
    }

    /**
     * Match method.
     *
     * @return the boolean value
     */
    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return Map.Entry.class.isAssignableFrom(rawType);
    }

    /**
     * Converts Map to Entry
     *
     * @param targetType the target Map type
     * @param keyType    the key type
     * @param valueType  the value type
     * @param map        the map to convert
     * @return the Entry
     */
    private Map.Entry<?, ?> mapToEntry(final Type targetType, final Type keyType, final Type valueType, final Map map) {
        final Object key;
        Object value;
        if (1 == map.size()) {
            final Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();
            key = entry.getKey();
            value = entry.getValue();
        } else {
            // Ignore other properties in Map
            key = map.get("key");
            value = map.get("value");
        }

        if (value instanceof Wrapper) {
            value = ((Wrapper) value).getRaw();
        }

        return (Map.Entry<?, ?>) ReflectKit.newInstance(
                TypeKit.getClass(targetType),
                TypeKit.isUnknown(keyType) ? key : converter.convert(keyType, key),
                TypeKit.isUnknown(valueType) ? value : converter.convert(valueType, value));
    }

}
