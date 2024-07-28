/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.*;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * {@link Map.Entry} 转换器，支持以下类型转为Entry
 * <ul>
 * <li>{@link Map}</li>
 * <li>{@link Map.Entry}</li>
 * <li>带分隔符的字符串，支持分隔符{@code :}、{@code =}、{@code ,}</li>
 * <li>Bean，包含{@code getKey}和{@code getValue}方法</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EntryConverter implements Converter {

    /**
     * 单例
     */
    public static final EntryConverter INSTANCE = new EntryConverter();

    /**
     * 字符串转单个键值对的Map，支持分隔符{@code :}、{@code =}、{@code ,}
     *
     * @param text 字符串
     * @return map or null
     */
    private static Map<CharSequence, CharSequence> strToMap(final CharSequence text) {
        // data:value data=value data,value
        final int index = StringKit.indexOf(text,
                c -> c == Symbol.C_COLON || c == Symbol.C_EQUAL || c == Symbol.C_COMMA, 0, text.length());

        if (index > -1) {
            return MapKit.of(text.subSequence(0, index), text.subSequence(index + 1, text.length()));
        }
        return null;
    }

    /**
     * Map转Entry
     *
     * @param targetType 目标的Map类型
     * @param keyType    键类型
     * @param valueType  值类型
     * @param map        被转换的map
     * @return Entry
     */
    private static Map.Entry<?, ?> mapToEntry(final Type targetType, final Type keyType, final Type valueType,
            final Map map) {

        Object key = null;
        Object value = null;
        if (1 == map.size()) {
            final Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();
            key = entry.getKey();
            value = entry.getValue();
        } else if (2 == map.size()) {
            key = map.get("id");
            value = map.get("value");
        }

        final CompositeConverter convert = CompositeConverter.getInstance();
        return (Map.Entry<?, ?>) ReflectKit.newInstance(TypeKit.getClass(targetType),
                TypeKit.isUnknown(keyType) ? key : convert.convert(keyType, key),
                TypeKit.isUnknown(valueType) ? value : convert.convert(valueType, value));
    }

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
     * 转换对象为指定键值类型的指定类型Map
     *
     * @param targetType 目标的Map类型
     * @param keyType    键类型
     * @param valueType  值类型
     * @param value      被转换的值
     * @return 转换后的Map
     * @throws ConvertException 转换异常或不支持的类型
     */
    public Map.Entry<?, ?> convert(final Type targetType, final Type keyType, final Type valueType, final Object value)
            throws ConvertException {
        Map map = null;
        if (value instanceof Map.Entry) {
            final Map.Entry entry = (Map.Entry) value;
            map = MapKit.of(entry.getKey(), entry.getValue());
        } else if (value instanceof Pair) {
            final Pair entry = (Pair<?, ?>) value;
            map = MapKit.of(entry.getLeft(), entry.getRight());
        } else if (value instanceof Map) {
            map = (Map) value;
        } else if (value instanceof CharSequence) {
            final CharSequence text = (CharSequence) value;
            map = strToMap(text);
        } else if (BeanKit.isWritableBean(value.getClass())) {
            map = BeanKit.beanToMap(value);
        }

        if (null != map) {
            return mapToEntry(targetType, keyType, valueType, map);
        }

        throw new ConvertException("Unsupported to map from [{}] of type: {}", value, value.getClass().getName());
    }

}
