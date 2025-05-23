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

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.lang.reflect.TypeReference;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * 复合转换器，融合了所有支持类型和自定义类型的转换规则 在此类中，存放着默认转换器和自定义转换器，默认转换器是预定义的一些转换器，自定义转换器存放用户自定的转换器。 转换过程类似于转换链，过程如下：
 *
 * <pre>{@code
 *     处理null、Optional --> 自定义匹配转换器 --> 自定义类型转换器 --> 预注册的标准转换器 --> Map、集合、Enum等特殊转换器 --> Bean转换器
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompositeConverter implements Converter, Serializable {

    @Serial
    private static final long serialVersionUID = 2852259259531L;

    private RegisterConverter registerConverter;
    private SpecialConverter specialConverter;

    /**
     * 构造
     */
    private CompositeConverter() {

    }

    /**
     * 获得单例的 CompositeConverter
     *
     * @return CompositeConverter
     */
    public static CompositeConverter getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 登记自定义转换器，符合{@link MatcherConverter#match(Type, Class, Object)}则使用其转换器 注意：如果单例使用，此方法会影响全局
     *
     * @param converter 转换器
     * @return this
     */
    public CompositeConverter register(final MatcherConverter converter) {
        registerConverter.register(converter);
        return this;
    }

    /**
     * 登记自定义转换器，登记的目标类型必须一致 注意：如果单例使用，此方法会影响全局
     *
     * @param type      转换的目标类型
     * @param converter 转换器
     * @return this
     */
    public CompositeConverter register(final Type type, final Converter converter) {
        registerConverter.register(type, converter);
        return this;
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
     * 转换值为指定类型,自定义转换器优先
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
    public <T> T convert(Type type, Object value, final T defaultValue, final boolean isCustomFirst)
            throws ConvertException {
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

        if (value instanceof Optional<?>) {
            value = ((Optional<T>) value).getOrNull();
            if (ObjectKit.isNull(value)) {
                return defaultValue;
            }
        }
        if (value instanceof java.util.Optional) {
            value = ((Optional<T>) value).orElse(null);
            if (ObjectKit.isNull(value)) {
                return defaultValue;
            }
        }

        // value本身实现了Converter接口，直接调用
        if (value instanceof Converter) {
            return ((Converter) value).convert(type, value, defaultValue);
        }

        if (type instanceof TypeReference) {
            type = ((TypeReference<?>) type).getType();
        }

        // 标准转换器
        final Converter converter = registerConverter.getConverter(type, value, isCustomFirst);
        if (null != converter) {
            return converter.convert(type, value, defaultValue);
        }

        Class<T> rawType = (Class<T>) TypeKit.getClass(type);
        if (null == rawType) {
            if (null != defaultValue) {
                rawType = (Class<T>) defaultValue.getClass();
            } else {
                throw new ConvertException("Can not get class from type: {}", type);
            }
        }

        // 特殊类型转换，包括Collection、Map、强转、Array等
        final T result = (T) specialConverter.convert(type, rawType, value);
        if (null != result) {
            return result;
        }

        // 尝试转Bean
        if (BeanKit.isWritableBean(rawType)) {
            return (T) BeanConverter.INSTANCE.convert(type, value);
        }

        // 无法转换
        throw new ConvertException("Can not support from {}: [{}] to [{}]", value.getClass().getName(), value,
                type.getTypeName());
    }

    /**
     * 类级的内部类，也就是静态的成员式内部类，该内部类的实例与外部类的实例 没有绑定关系，而且只有被调用到才会装载，从而实现了延迟加载
     */
    private static class SingletonHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static final CompositeConverter INSTANCE;
        static {
            INSTANCE = new CompositeConverter();
            INSTANCE.registerConverter = new RegisterConverter(INSTANCE);
            INSTANCE.specialConverter = new SpecialConverter(INSTANCE);
        }
    }

}
