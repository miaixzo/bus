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
import java.util.Map;

import org.miaixz.bus.core.bean.copier.ValueProvider;
import org.miaixz.bus.core.bean.copier.provider.BeanValueProvider;
import org.miaixz.bus.core.bean.copier.provider.MapValueProvider;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.KotlinKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * Kotlin Bean转换器，支持：
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
public class KBeanConverter implements MatcherConverter, Serializable {

    @Serial
    private static final long serialVersionUID = 2852271998273L;

    /**
     * 单例对象
     */
    public static KBeanConverter INSTANCE = new KBeanConverter();

    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return KotlinKit.isKotlinClass(rawType);
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
        ValueProvider<String> valueProvider = null;
        if (value instanceof ValueProvider) {
            valueProvider = (ValueProvider<String>) value;
        } else if (value instanceof Map) {
            valueProvider = new MapValueProvider((Map<String, ?>) value);
        } else if (BeanKit.isWritableBean(value.getClass())) {
            valueProvider = new BeanValueProvider(value);
        }

        if (null != valueProvider) {
            return KotlinKit.newInstance(targetClass, valueProvider);
        }

        throw new ConvertException("Unsupported source type: [{}] to [{}]", value.getClass(), targetType);
    }

}
