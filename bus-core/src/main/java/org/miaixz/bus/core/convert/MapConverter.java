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
import org.miaixz.bus.core.lang.Types;
import org.miaixz.bus.core.toolkit.BeanKit;
import org.miaixz.bus.core.toolkit.MapKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.core.toolkit.TypeKit;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * {@link Map} 转换器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapConverter implements Converter, Serializable {

    private static final long serialVersionUID = 1L;

    public static MapConverter INSTANCE = new MapConverter();

    @Override
    public Object convert(Type targetType, final Object value) throws ConvertException {
        if (targetType instanceof Types<?>) {
            targetType = ((Types<?>) targetType).getType();
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
     */
    public Map<?, ?> convert(final Type targetType, final Type keyType, final Type valueType, final Object value) {
        Map map;
        if (value instanceof Map) {
            final Class<?> valueClass = value.getClass();
            if (valueClass.equals(targetType)) {
                final Type[] typeArguments = TypeKit.getTypeArguments(valueClass);
                if (null != typeArguments
                        && 2 == typeArguments.length
                        && Objects.equals(keyType, typeArguments[0])
                        && Objects.equals(valueType, typeArguments[1])) {
                    // 对于键值对类型一致的Map对象，不再做转换，直接返回原对象
                    return (Map) value;
                }
            }
            map = MapKit.createMap(TypeKit.getClass(targetType));
            convertMapToMap(keyType, valueType, (Map) value, map);
        } else if (BeanKit.isBean(value.getClass())) {
            map = BeanKit.beanToMap(value);
            // 二次转换，转换键值类型
            map = convert(targetType, keyType, valueType, map);
        } else {
            throw new UnsupportedOperationException(StringKit.format("Unsupported toMap value type: {}", value.getClass().getName()));
        }
        return map;
    }

    /**
     * Map转Map
     *
     * @param srcMap    源Map
     * @param targetMap 目标Map
     */
    private void convertMapToMap(final Type keyType, final Type valueType, final Map<?, ?> srcMap, final Map targetMap) {
        final CompositeConverter convert = CompositeConverter.getInstance();
        srcMap.forEach((key, value) -> {
            key = TypeKit.isUnknown(keyType) ? key : convert.convert(keyType, key, null);
            value = TypeKit.isUnknown(valueType) ? value : convert.convert(valueType, value, null);
            targetMap.put(key, value);
        });
    }

}
