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

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

import org.miaixz.bus.core.bean.copier.BeanCopier;
import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.bean.copier.ValueProvider;
import org.miaixz.bus.core.center.map.MapProxy;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.*;

/**
 * Bean转换器，支持：
 * 
 * <pre>
 * Map = Bean
 * Bean = Bean
 * ValueProvider = Bean
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanConverter implements Converter, Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 单例对象
     */
    public static BeanConverter INSTANCE = new BeanConverter();

    private final CopyOptions copyOptions;

    /**
     * 构造
     */
    public BeanConverter() {
        this(CopyOptions.of().setIgnoreError(true));
    }

    /**
     * 构造
     *
     * @param copyOptions Bean转换选项参数
     */
    public BeanConverter(final CopyOptions copyOptions) {
        this.copyOptions = copyOptions;
    }

    @Override
    public Object convert(final Type targetType, final Object value) throws ConvertException {
        Assert.notNull(targetType);
        if (null == value) {
            return null;
        }

        // value本身实现了Converter接口，直接调用
        if (value instanceof Converter) {
            return ((Converter) value).convert(targetType, value);
        }

        final Class<?> targetClass = TypeKit.getClass(targetType);
        Assert.notNull(targetClass, "Target type is not a class!");

        return convertInternal(targetType, targetClass, value);
    }

    private Object convertInternal(final Type targetType, final Class<?> targetClass, final Object value) {
        if (value instanceof Map || value instanceof ValueProvider || BeanKit.isWritableBean(value.getClass())) {
            if (value instanceof Map && targetClass.isInterface()) {
                // 将Map动态代理为Bean
                return MapProxy.of((Map<?, ?>) value).toProxyBean(targetClass);
            }

            // 限定被转换对象类型
            return BeanCopier.of(value, ReflectKit.newInstanceIfPossible(targetClass), targetType, this.copyOptions)
                    .copy();
        } else if (value instanceof byte[]) {
            // 尝试反序列化
            return SerializeKit.deserialize((byte[]) value);
        } else if (ObjectKit.isEmptyIfString(value)) {
            return null;
        }

        throw new ConvertException("Unsupported source type: [{}] to [{}]", value.getClass(), targetType);
    }

}
