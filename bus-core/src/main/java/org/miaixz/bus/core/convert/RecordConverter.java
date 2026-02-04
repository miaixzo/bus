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
import java.lang.reflect.Type;
import java.util.Map;

import org.miaixz.bus.core.bean.copier.ValueProvider;
import org.miaixz.bus.core.bean.copier.provider.BeanValueProvider;
import org.miaixz.bus.core.bean.copier.provider.MapValueProvider;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.RecordKit;

/**
 * Converter for Record classes, supporting:
 *
 * <pre>
 *   Map = Record
 *   Bean = Record
 *   ValueProvider = Record
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RecordConverter extends AbstractConverter implements MatcherConverter {

    @Serial
    private static final long serialVersionUID = 2852271152563L;

    /**
     * Singleton instance
     */
    public static RecordConverter INSTANCE = new RecordConverter();

    /**
     * Checks if this converter can handle the conversion to the specified target type.
     *
     * @param targetType the target type
     * @param rawType    the raw class of the target type
     * @param value      the value to be converted
     * @return {@code true} if the target type is a Record class
     */
    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return RecordKit.isRecord(rawType);
    }

    /**
     * Converts the given value to a Record instance.
     * <p>
     * Supports conversion from Map, Bean, and ValueProvider.
     * </p>
     *
     * @param targetClass the target Record class
     * @param value       the value to convert (Map, Bean, or ValueProvider)
     * @return the created Record instance
     * @throws ConvertException if the source type is not supported
     */
    @Override
    protected Object convertInternal(final Class<?> targetClass, final Object value) {
        ValueProvider<String> valueProvider = null;
        if (value instanceof ValueProvider) {
            valueProvider = (ValueProvider<String>) value;
        } else if (value instanceof Map) {
            valueProvider = new MapValueProvider((Map<String, ?>) value);
        } else if (BeanKit.isReadableBean(value.getClass())) {
            valueProvider = new BeanValueProvider(value);
        }

        if (null != valueProvider) {
            return RecordKit.newInstance(targetClass, valueProvider);
        }

        throw new ConvertException("Unsupported source type: [{}] to [{}]", value.getClass(), targetClass);
    }

}
