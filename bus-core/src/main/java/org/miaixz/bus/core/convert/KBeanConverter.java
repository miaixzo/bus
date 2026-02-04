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

import org.miaixz.bus.core.bean.copier.ValueProvider;
import org.miaixz.bus.core.bean.copier.provider.BeanValueProvider;
import org.miaixz.bus.core.bean.copier.provider.MapValueProvider;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.KotlinKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * Converter for Kotlin Beans, supporting:
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
    private static final long serialVersionUID = 2852268927129L;

    /**
     * Singleton instance
     */
    public static KBeanConverter INSTANCE = new KBeanConverter();

    /**
     * Match method.
     *
     * @return the boolean value
     */
    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return KotlinKit.isKotlinClass(rawType);
    }

    /**
     * Convert method.
     *
     * @return the Object value
     */
    @Override
    public Object convert(final Type targetType, final Object value) throws ConvertException {
        Assert.notNull(targetType);
        if (null == value) {
            return null;
        }

        // If value itself implements Converter interface, call it directly
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
