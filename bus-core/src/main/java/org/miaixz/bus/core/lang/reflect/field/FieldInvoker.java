/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.reflect.field;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.miaixz.bus.core.convert.Converter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.Invoker;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ReflectKit;

/**
 * Field invoker for reading or setting field values using reflection.
 * <p>
 * To read a field value:
 * 
 * <pre>{@code
 * FieldInvoker.of(Field).invoke(object);
 * }</pre>
 * <p>
 * To set a field value:
 * 
 * <pre>{@code
 * FieldInvoker.of(Field).invoke(object, value);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldInvoker implements Invoker {

    /**
     * The field to be invoked.
     */
    private final Field field;
    /**
     * The converter used to convert the value before setting it to the field. If {@code null}, no conversion is
     * performed.
     */
    private Converter converter;

    /**
     * Constructs a new {@code FieldInvoker} for the given field.
     *
     * @param field The field to be invoked. Must not be {@code null}.
     * @throws IllegalArgumentException if {@code field} is {@code null}.
     */
    public FieldInvoker(final Field field) {
        this.field = Assert.notNull(field);
    }

    /**
     * Creates a new {@code FieldInvoker} instance for the given field.
     *
     * @param field The field to be invoked.
     * @return A new {@code FieldInvoker} instance, or {@code null} if the provided field is {@code null}.
     */
    public static FieldInvoker of(final Field field) {
        return null == field ? null : new FieldInvoker(field);
    }

    /**
     * Retrieves the underlying {@link Field} object.
     *
     * @return The {@link Field} object associated with this invoker.
     */
    public Field getField() {
        return this.field;
    }

    /**
     * Gets the name of this object.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return this.field.getName();
    }

    /**
     * Gettype method.
     *
     * @return the Type value
     */
    @Override
    public Type getType() {
        return field.getGenericType();
    }

    /**
     * Gettypeclass method.
     *
     * @return the Class&lt;?&gt; value
     */
    @Override
    public Class<?> getTypeClass() {
        return field.getType();
    }

    /**
     * Sets the converter for field values.
     *
     * @param converter The converter to use for value conversion. If {@code null}, no conversion will be performed.
     * @return This {@code FieldInvoker} instance for method chaining.
     */
    public FieldInvoker setConverter(final Converter converter) {
        this.converter = converter;
        return this;
    }

    /**
     * Invokes the field operation (get or set) on the specified target object.
     * <ul>
     * <li>If {@code args} is empty, it attempts to read the field's value.</li>
     * <li>If {@code args} contains one element, it attempts to set the field's value to that element.</li>
     * <li>Otherwise, an {@code InternalException} is thrown.</li>
     * </ul>
     *
     * @param target The target object on which the field operation is to be performed. For static fields, this can be
     *               {@code null} or the class itself.
     * @param args   The arguments for the field operation. Empty for get operation, one element for set operation.
     * @param <T>    The expected return type for a get operation.
     * @return The field's value if performing a get operation, or {@code null} for a set operation.
     * @throws InternalException if the number of arguments is not 0 or 1, or if an access error occurs.
     */
    @Override
    public <T> T invoke(final Object target, final Object... args) {
        if (ArrayKit.isEmpty(args)) {
            // Default to get operation
            return (T) invokeGet(target);
        } else if (args.length == 1) {
            invokeSet(target, args[0]);
            return null;
        }

        throw new InternalException("Field [{}] cannot be set with [{}] args", field.getName(), args.length);
    }

    /**
     * Retrieves the value of the field from the specified object.
     *
     * @param object The object from which to get the field value. For static fields, this can be {@code null} or the
     *               class itself.
     * @return The value of the field.
     * @throws InternalException if an {@link IllegalAccessException} occurs during field access.
     */
    public Object invokeGet(Object object) throws InternalException {
        if (null == field) {
            return null;
        }
        if (object instanceof Class) {
            // For static fields, the object parameter should be null.
            object = null;
        }

        ReflectKit.setAccessible(field);
        final Object result;
        try {
            result = field.get(object);
        } catch (final IllegalAccessException e) {
            throw new InternalException(e, "IllegalAccess for {}.{}", field.getDeclaringClass(), field.getName());
        }
        return result;
    }

    /**
     * Sets the value of the field on the specified object. The provided value must be compatible with the field's type;
     * otherwise, an exception may be thrown or conversion attempted if a converter is set.
     *
     * @param object The object on which to set the field value. For static fields, this can be {@code null} or the
     *               class itself.
     * @param value  The value to set. Its type must be compatible with the field's type.
     * @throws InternalException if an {@link IllegalAccessException} occurs during field access.
     */
    public void invokeSet(final Object object, final Object value) throws InternalException {
        ReflectKit.setAccessible(field);
        try {
            field.set(object instanceof Class ? null : object, convertValue(value));
        } catch (final IllegalAccessException e) {
            throw new InternalException(e, "IllegalAccess for [{}.{}]",
                    null == object ? field.getDeclaringClass() : object, field.getName());
        }
    }

    /**
     * Converts the given value to a type compatible with the field's type, if a converter is set. If no converter is
     * set, the original value is returned. If the value is {@code null}, it returns the default value for primitive
     * types to prevent {@code NullPointerException}.
     *
     * @param value The value to convert.
     * @return The converted value, or the original value if no conversion is needed or possible.
     */
    private Object convertValue(final Object value) {
        if (null == converter) {
            return value;
        }

        // Value type check and conversion
        final Class<?> fieldType = field.getType();
        if (null != value) {
            if (!fieldType.isAssignableFrom(value.getClass())) {
                // For fields with different types, attempt conversion. If conversion fails, use the original object
                // type.
                final Object targetValue = converter.convert(fieldType, value);
                if (null != targetValue) {
                    return targetValue;
                }
            }
        } else {
            // Get the default value for null to prevent NullPointerException for primitive types.
            return ClassKit.getDefaultValue(fieldType);
        }

        return value;
    }

}
