/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.convert;

import java.io.Serial;
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
 * Converts an object to a JavaBean object.
 * <p>
 * This converter supports the following source types:
 * <ul>
 * <li>{@link Map}</li>
 * <li>Another JavaBean</li>
 * <li>{@link ValueProvider}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanConverter implements Converter, Serializable {

    @Serial
    private static final long serialVersionUID = 2852265579700L;

    /**
     * Singleton instance with default copy options.
     */
    public static BeanConverter INSTANCE = new BeanConverter();

    /**
     * The options to control the bean copying process.
     */
    private final CopyOptions copyOptions;

    /**
     * Constructs a new {@code BeanConverter} with default copy options (errors ignored).
     */
    public BeanConverter() {
        this(CopyOptions.of().setIgnoreError(true));
    }

    /**
     * Constructs a new {@code BeanConverter} with the specified copy options.
     *
     * @param copyOptions The options for bean copying.
     */
    public BeanConverter(final CopyOptions copyOptions) {
        this.copyOptions = copyOptions;
    }

    /**
     * Converts the given value to the specified target type.
     *
     * @param targetType The target type to convert to.
     * @param value      The value to convert.
     * @return The converted object, or {@code null} if the input value is {@code null}.
     * @throws ConvertException if the conversion is not supported or fails.
     */
    @Override
    public Object convert(final Type targetType, final Object value) throws ConvertException {
        Assert.notNull(targetType, "Target type must not be null.");
        if (null == value) {
            return null;
        }

        // If the value is a Converter itself, use it for conversion.
        if (value instanceof Converter) {
            return ((Converter) value).convert(targetType, value);
        }

        final Class<?> targetClass = TypeKit.getClass(targetType);
        Assert.notNull(targetClass, "Target type is not a class!");

        return convertInternal(targetType, targetClass, value);
    }

    /**
     * Performs the internal conversion logic.
     *
     * @param targetType  The target type.
     * @param targetClass The target class.
     * @param value       The value to be converted.
     * @return The converted object.
     * @throws ConvertException if the source type is not supported.
     */
    private Object convertInternal(final Type targetType, final Class<?> targetClass, final Object value) {
        // Handle conversion from Map, ValueProvider, or another readable bean.
        if (value instanceof Map || value instanceof ValueProvider || BeanKit.isReadableBean(value.getClass())) {
            if (value instanceof Map && targetClass.isInterface()) {
                // Create a dynamic proxy for the Map to act as a bean.
                return MapProxy.of((Map<?, ?>) value).toProxyBean(targetClass);
            }

            // Copy properties from the source to a new instance of the target class.
            return BeanCopier.of(value, ReflectKit.newInstanceIfPossible(targetClass), targetType, this.copyOptions)
                    .copy();
        } else if (value instanceof byte[]) {
            // Attempt to deserialize from a byte array.
            return SerializeKit.deserialize((byte[]) value);
        } else if (ObjectKit.isEmptyIfString(value)) {
            // Return null for empty string values.
            return null;
        }

        throw new ConvertException("Unsupported source type: [{}] to [{}]", value.getClass(), targetType);
    }

}
