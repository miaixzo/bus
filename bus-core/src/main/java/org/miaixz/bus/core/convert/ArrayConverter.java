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
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.*;

/**
 * Converts an object to an array, including arrays of primitive types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ArrayConverter extends AbstractConverter implements MatcherConverter {

    @Serial
    private static final long serialVersionUID = 2852263350173L;

    /**
     * Singleton instance.
     */
    public static final ArrayConverter INSTANCE = new ArrayConverter();

    /**
     * Whether to ignore errors during the conversion of array elements.
     */
    private boolean ignoreElementError;

    /**
     * Constructs a new {@code ArrayConverter} with default settings.
     */
    public ArrayConverter() {
        this(false);
    }

    /**
     * Constructs a new {@code ArrayConverter}.
     *
     * @param ignoreElementError If {@code true}, errors encountered during element conversion will be ignored.
     */
    public ArrayConverter(final boolean ignoreElementError) {
        this.ignoreElementError = ignoreElementError;
    }

    /**
     * Convertinternal method.
     *
     * @return the Object value
     */
    @Override
    protected Object convertInternal(final Class<?> targetClass, final Object value) {
        final Class<?> targetComponentType = targetClass.isArray() ? targetClass.getComponentType() : targetClass;
        return value.getClass().isArray() ? convertArrayToArray(targetComponentType, value)
                : convertObjectToArray(targetComponentType, value);
    }

    /**
     * Checks if this converter can handle the conversion to the specified target type.
     *
     * @param targetType The target type.
     * @param rawType    The raw class of the target type.
     * @param value      The value to be converted.
     * @return {@code true} if the target type is an array, {@code false} otherwise.
     */
    @Override
    public boolean match(final Type targetType, final Class<?> rawType, final Object value) {
        return rawType.isArray();
    }

    /**
     * Sets whether to ignore errors that occur during the conversion of array elements.
     *
     * @param ignoreElementError {@code true} to ignore element conversion errors.
     */
    public void setIgnoreElementError(final boolean ignoreElementError) {
        this.ignoreElementError = ignoreElementError;
    }

    /**
     * Converts an array to another type of array.
     *
     * @param targetComponentType The component type of the target array.
     * @param array               The source array to convert.
     * @return The converted array.
     */
    private Object convertArrayToArray(final Class<?> targetComponentType, final Object array) {
        final Class<?> valueComponentType = ArrayKit.getComponentType(array);

        if (valueComponentType == targetComponentType) {
            return array; // No conversion needed if component types are the same.
        }

        final int len = ArrayKit.length(array);
        final Object result = Array.newInstance(targetComponentType, len);

        for (int i = 0; i < len; i++) {
            Array.set(result, i, convertComponentType(targetComponentType, Array.get(array, i)));
        }
        return result;
    }

    /**
     * Converts a non-array object to an array.
     *
     * @param targetComponentType The component type of the target array.
     * @param value               The object to convert.
     * @return The resulting array.
     */
    private Object convertObjectToArray(final Class<?> targetComponentType, Object value) {
        if (value instanceof CharSequence) {
            if (targetComponentType == char.class || targetComponentType == Character.class) {
                return convertArrayToArray(targetComponentType, value.toString().toCharArray());
            }

            // Handle byte array conversion from Base64 or plain string.
            if (targetComponentType == byte.class) {
                final String text = value.toString();
                return Base64.isTypeBase64(text) ? Base64.decode(text) : text.getBytes();
            }

            // Split comma-separated string into an array.
            final String[] strings = StringKit.splitToArray(value.toString(), Symbol.COMMA);
            return convertArrayToArray(targetComponentType, strings);
        }

        if (value instanceof Iterator) {
            value = IteratorKit.asIterable((Iterator<?>) value);
        }

        if (value instanceof Iterable) {
            return convertIterableToArray(targetComponentType, (Iterable<?>) value);
        } else if (value instanceof Number && byte.class == targetComponentType) {
            return ByteKit.toBytes((Number) value);
        } else if (value instanceof Serializable && byte.class == targetComponentType) {
            return SerializeKit.serialize(value);
        } else {
            return convertToSingleElementArray(targetComponentType, value);
        }
    }

    /**
     * Converts an {@link Iterable} to an array.
     *
     * @param targetComponentType The component type of the target array.
     * @param iterable            The iterable to convert.
     * @return The converted array.
     */
    private Object convertIterableToArray(final Class<?> targetComponentType, final Iterable<?> iterable) {
        Collection<?> collection = (iterable instanceof Collection) ? (Collection<?>) iterable : ListKit.of(iterable);
        final Object result = Array.newInstance(targetComponentType, collection.size());

        int i = 0;
        for (final Object element : collection) {
            Array.set(result, i++, convertComponentType(targetComponentType, element));
        }
        return result;
    }

    /**
     * Creates a new array containing a single element.
     *
     * @param targetComponentType The component type of the array.
     * @param value               The value to be placed in the array.
     * @return A new array with the specified component type and containing the given value.
     */
    private Object[] convertToSingleElementArray(final Class<?> targetComponentType, final Object value) {
        final Object[] singleElementArray = ArrayKit.newArray(targetComponentType, 1);
        singleElementArray[0] = convertComponentType(targetComponentType, value);
        return singleElementArray;
    }

    /**
     * Converts a single value to the target component type.
     *
     * @param targetComponentType The target component type.
     * @param value               The value to convert.
     * @return The converted value. Returns {@code null} if conversion fails and {@code ignoreElementError} is true.
     */
    private Object convertComponentType(final Class<?> targetComponentType, final Object value) {
        return Convert.convertWithCheck(targetComponentType, value, null, this.ignoreElementError);
    }

}
