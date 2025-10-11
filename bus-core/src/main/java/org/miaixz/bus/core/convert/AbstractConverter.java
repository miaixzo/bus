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

import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * An abstract converter that provides common conversion logic. Subclasses should implement the
 * {@link #convertInternal(Class, Object)} method for type-specific conversion logic.
 * <p>
 * This converter handles common scenarios:
 * <ul>
 * <li>Returns {@code null} if the input value is {@code null}.</li>
 * <li>Throws {@link ConvertException} for unknown or non-class target types.</li>
 * <li>Performs a direct cast if the value is already an instance of the target type.</li>
 * </ul>
 * Note: This abstract implementation is not suitable for types with generic parameters like Map, Collection, etc.,
 * which require special handling.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractConverter implements Converter, Serializable {

    /**
     * The serial version UID.
     */
    @Serial
    private static final long serialVersionUID = 2852263159195L;

    /**
     * Converts the given value to the specified target type.
     *
     * @param targetType The target type to convert to.
     * @param value      The value to convert.
     * @return The converted value, or {@code null} if the input value is {@code null}.
     * @throws ConvertException if the target type is unknown or not a class.
     */
    @Override
    public Object convert(final Type targetType, final Object value) throws ConvertException {
        if (null == value) {
            return null;
        }
        if (TypeKit.isUnknown(targetType)) {
            throw new ConvertException("Unsupported conversion to unknown type: {}", targetType);
        }

        final Class<?> targetClass = TypeKit.getClass(targetType);
        if (null == targetClass) {
            throw new ConvertException("Target type [{}] is not a class!", targetType);
        }

        // If the value is already an instance of the target class, no conversion is needed.
        if (targetClass.isInstance(value)) {
            return value;
        }
        return convertInternal(targetClass, value);
    }

    /**
     * Performs the internal conversion logic. This method is called by {@link #convert(Type, Object)} after basic
     * checks have been performed.
     * <p>
     * If the conversion fails, implementations can either return {@code null} or throw a {@link RuntimeException}.
     *
     * @param targetClass The target class to convert to.
     * @param value       The non-null value to be converted.
     * @return The converted object.
     */
    protected abstract Object convertInternal(Class<?> targetClass, Object value);

    /**
     * Converts an object to its string representation. This is a utility method for converters that require an
     * intermediate string form.
     * <ul>
     * <li>{@link CharSequence} objects are converted using {@code toString()}.</li>
     * <li>Arrays are converted to a comma-separated string.</li>
     * <li>Other objects are converted using their default {@code toString()} method.</li>
     * </ul>
     *
     * @param value The object to convert.
     * @return The string representation, or {@code null} if the input is {@code null}.
     */
    protected String convertToString(final Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof CharSequence) {
            return value.toString();
        } else if (ArrayKit.isArray(value)) {
            return ArrayKit.toString(value);
        } else if (CharKit.isChar(value)) {
            // For ASCII characters, use cache to speed up conversion and reduce space creation
            return CharKit.toString((char) value);
        }
        return value.toString();
    }

}
