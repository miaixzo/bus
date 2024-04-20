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
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.core.toolkit.TypeKit;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * 复合转换器，融合了所有支持类型和自定义类型的转换规则
 * 将各种类型Convert对象放入符合转换器，通过convert方法查找目标类型对应的转换器，将被转换对象转换之
 * 在此类中，存放着默认转换器和自定义转换器，默认转换器是bus中预定义的一些转换器，自定义转换器存放用户自定的转换器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompositeConverter extends RegistryConverter {

    private static final long serialVersionUID = 1L;

    /**
     * 构造
     */
    public CompositeConverter() {
        super();
    }

    /**
     * 获得单例的 RegistryConverter
     *
     * @return RegistryConverter
     */
    public static CompositeConverter getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 转换值为指定类型
     *
     * @param type  类型
     * @param value 值
     * @return 转换后的值，默认为{@code null}
     * @throws ConvertException 转换器不存在
     */
    @Override
    public Object convert(final Type type, final Object value) throws ConvertException {
        return convert(type, value, null);
    }

    /**
     * 转换值为指定类型<br>
     * 自定义转换器优先
     *
     * @param <T>          转换的目标类型（转换器转换到的类型）
     * @param type         类型
     * @param value        值
     * @param defaultValue 默认值
     * @return 转换后的值
     * @throws ConvertException 转换器不存在
     */
    @Override
    public <T> T convert(final Type type, final Object value, final T defaultValue) throws ConvertException {
        return convert(type, value, defaultValue, true);
    }

    /**
     * 转换值为指定类型
     *
     * @param <T>           转换的目标类型（转换器转换到的类型）
     * @param type          类型目标
     * @param value         被转换值
     * @param defaultValue  默认值
     * @param isCustomFirst 是否自定义转换器优先
     * @return 转换后的值
     * @throws ConvertException 转换器不存在
     */
    public <T> T convert(Type type, final Object value, final T defaultValue, final boolean isCustomFirst) throws ConvertException {
        if (ObjectKit.isNull(value)) {
            return defaultValue;
        }
        if (TypeKit.isUnknown(type)) {
            // 对于用户不指定目标类型的情况，返回原值
            if (null == defaultValue) {
                return (T) value;
            }
            type = defaultValue.getClass();
        }

        // value本身实现了Converter接口，直接调用
        if (value instanceof Converter) {
            return ((Converter) value).convert(type, value, defaultValue);
        }

        if (type instanceof Types) {
            type = ((Types<?>) type).getType();
        }

        // 标准转换器
        final Converter converter = getConverter(type, isCustomFirst);
        if (null != converter) {
            return converter.convert(type, value, defaultValue);
        }

        Class<T> rowType = (Class<T>) TypeKit.getClass(type);
        if (null == rowType) {
            if (null != defaultValue) {
                rowType = (Class<T>) defaultValue.getClass();
            } else {
                throw new ConvertException("Can not get class from type: {}", type);
            }
        }

        // 特殊类型转换，包括Collection、Map、强转、Array等
        final T result = convertSpecial(type, rowType, value, defaultValue);
        if (null != result) {
            return result;
        }

        // 尝试转Bean
        if (BeanKit.isBean(rowType)) {
            return (T) BeanConverter.INSTANCE.convert(type, value);
        }

        // 无法转换
        throw new ConvertException("Can not convert from {}: [{}] to [{}]", value.getClass().getName(), value, type.getTypeName());
    }

    /**
     * 特殊类型转换<br>
     * 包括：
     *
     * <pre>
     * Collection
     * Map
     * 强转（无需转换）
     * 数组
     * </pre>
     *
     * @param <T>          转换的目标类型（转换器转换到的类型）
     * @param type         类型
     * @param value        值
     * @param defaultValue 默认值
     * @return 转换后的值
     */
    private <T> T convertSpecial(final Type type, final Class<T> rowType, final Object value, final T defaultValue) {
        if (null == rowType) {
            return null;
        }

        // 集合转换（含有泛型参数，不可以默认强转）
        if (Collection.class.isAssignableFrom(rowType)) {
            return (T) CollectionConverter.INSTANCE.convert(type, value, (Collection<?>) defaultValue);
        }

        // Map类型（含有泛型参数，不可以默认强转）
        if (Map.class.isAssignableFrom(rowType)) {
            return (T) MapConverter.INSTANCE.convert(type, value, (Map<?, ?>) defaultValue);
        }

        // 默认强转
        if (rowType.isInstance(value)) {
            return (T) value;
        }

        // 原始类型转换
        if (rowType.isPrimitive()) {
            return PrimitiveConverter.INSTANCE.convert(type, value, defaultValue);
        }

        // 数字类型转换
        if (Number.class.isAssignableFrom(rowType)) {
            return NumberConverter.INSTANCE.convert(type, value, defaultValue);
        }

        // 枚举转换
        if (rowType.isEnum()) {
            return EnumConverter.INSTANCE.convert(type, value, defaultValue);
        }

        // 数组转换
        if (rowType.isArray()) {
            return ArrayConverter.INSTANCE.convert(type, value, defaultValue);
        }

        // 表示非需要特殊转换的对象
        return null;
    }

    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 没有绑定关系，而且只有被调用到才会装载，从而实现了延迟加载
     */
    private static class SingletonHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static final CompositeConverter INSTANCE = new CompositeConverter();
    }

}
