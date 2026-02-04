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

import java.lang.reflect.Type;

import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Type conversion interface function, user-defined conversion rules based on given value and target type
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface Converter {

    /**
     * Returns a converter that returns the original value without conversion
     *
     * @return this
     */
    static Converter identity() {
        return (targetType, value) -> value;
    }

    /**
     * Converts to the specified type. If the type cannot be determined, the default value's type will be used as the
     * target type
     *
     * @param targetType target Type, used for non-generic classes
     * @param value      the original value, if the object implements this interface, value is this
     * @return the converted value
     * @throws ConvertException throw this exception when conversion cannot be completed normally or conversion
     *                          exception occurs
     */
    Object convert(Type targetType, Object value) throws ConvertException;

    /**
     * Converts to the specified type. If the type cannot be determined, the default value's type will be used as the
     * target type
     *
     * @param <T>        the target type
     * @param targetType the target type
     * @param value      the original value, if the object implements this interface, value is this
     * @return the converted value
     * @throws ConvertException throw this exception when conversion cannot be completed normally or conversion
     *                          exception occurs
     */
    default <T> T convert(final Class<T> targetType, final Object value) throws ConvertException {
        return (T) convert((Type) targetType, value);
    }

    /**
     * Converts value to the specified type, optionally without throwing exceptions. Returns default value when
     * conversion fails
     *
     * @param <T>          the target type
     * @param targetType   the target type
     * @param value        the value
     * @param defaultValue the default value
     * @return the converted value
     */
    default <T> T convert(final Type targetType, final Object value, final T defaultValue) {
        return (T) ObjectKit.defaultIfNull(convert(targetType, value), defaultValue);
    }

}
