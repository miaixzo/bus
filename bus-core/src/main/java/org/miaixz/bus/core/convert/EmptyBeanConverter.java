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

import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.ReflectKit;

/**
 * Converter for null or empty objects, converting to an instance of the target type
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EmptyBeanConverter extends AbstractConverter implements MatcherConverter, Serializable {

    /**
     * Constructs a new EmptyBeanConverter. Utility class constructor for static access.
     */
    public EmptyBeanConverter() {
    }

    @Serial
    private static final long serialVersionUID = 2852268257237L;

    /**
     * Singleton instance
     */
    public static final EmptyBeanConverter INSTANCE = new EmptyBeanConverter();

    /**
     * Checks if this converter can handle the conversion to the specified target type.
     *
     * @param targetType the target type
     * @param rawType    the raw class of the target type
     * @param value      the value to be converted
     * @return {@code true} if the value is null or empty
     */
    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return ObjectKit.isEmpty(value);
    }

    /**
     * Converts the given value (which must be null or empty) to an instance of the target class.
     * <p>
     * Creates a new instance of the target class using reflection.
     * </p>
     *
     * @param targetClass the target class to instantiate
     * @param value       the value (should be null or empty)
     * @return a new instance of the target class
     */
    @Override
    protected Object convertInternal(final Class<?> targetClass, final Object value) {
        // For null values, instantiate the target class directly
        return ReflectKit.newInstanceIfPossible(targetClass);
    }

}
