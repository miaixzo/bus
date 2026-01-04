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
