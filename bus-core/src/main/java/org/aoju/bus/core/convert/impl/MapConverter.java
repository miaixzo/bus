package org.aoju.bus.core.convert.impl;

import org.aoju.bus.core.convert.AbstractConverter;
import org.aoju.bus.core.convert.ConverterRegistry;
import org.aoju.bus.core.utils.BeanUtils;
import org.aoju.bus.core.utils.MapUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.core.utils.TypeUtils;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link Map} 转换器
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MapConverter extends AbstractConverter<Map<?, ?>> {

    /**
     * Map类型
     */
    private final Type mapType;
    /**
     * 键类型
     */
    private final Type keyType;
    /**
     * 值类型
     */
    private final Type valueType;

    /**
     * 构造，Map的key和value泛型类型自动获取
     *
     * @param mapType Map类型
     */
    public MapConverter(Type mapType) {
        this(mapType, TypeUtils.getTypeArgument(mapType, 0), TypeUtils.getTypeArgument(mapType, 1));
    }

    /**
     * 构造
     *
     * @param mapType   Map类型
     * @param keyType   键类型
     * @param valueType 值类型
     */
    public MapConverter(Type mapType, Type keyType, Type valueType) {
        this.mapType = mapType;
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    protected Map<?, ?> convertInternal(Object value) {
        Map map = null;
        if (value instanceof Map) {
            map = MapUtils.createMap(TypeUtils.getClass(this.mapType));
            convertMapToMap((Map) value, map);
        } else if (BeanUtils.isBean(value.getClass())) {
            map = BeanUtils.beanToMap(value);
        } else {
            throw new UnsupportedOperationException(StringUtils.format("Unsupport toMap value type: {}", value.getClass().getName()));
        }
        return map;
    }

    /**
     * Map转Map
     *
     * @param srcMap    源Map
     * @param targetMap 目标Map
     */
    private void convertMapToMap(Map<?, ?> srcMap, Map<Object, Object> targetMap) {
        final ConverterRegistry convert = ConverterRegistry.getInstance();
        Object key;
        Object value;
        for (Entry<?, ?> entry : srcMap.entrySet()) {
            key = (null == this.keyType) ? entry.getKey() : convert.convert(this.keyType, entry.getKey());
            value = (null == this.valueType) ? entry.getValue() : convert.convert(this.keyType, entry.getValue());
            targetMap.put(key, value);
        }
    }

    @Override
    public Class<Map<?, ?>> getTargetType() {
        return (Class<Map<?, ?>>) TypeUtils.getClass(this.mapType);
    }

}
