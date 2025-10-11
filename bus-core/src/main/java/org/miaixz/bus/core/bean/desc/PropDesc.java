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
package org.miaixz.bus.core.bean.desc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.annotation.Alias;
import org.miaixz.bus.core.lang.annotation.Ignore;
import org.miaixz.bus.core.lang.exception.BeanException;
import org.miaixz.bus.core.lang.reflect.Invoker;
import org.miaixz.bus.core.lang.reflect.field.FieldInvoker;
import org.miaixz.bus.core.lang.reflect.method.MethodInvoker;
import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.ModifierKit;

/**
 * Describes a property of a Java Bean, including its associated field, getter method, setter method, and their
 * invocation details.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PropDesc {

    /**
     * The invoker for the getter method of the property.
     */
    protected Invoker getter;
    /**
     * The invoker for the setter method of the property.
     */
    protected Invoker setter;
    /**
     * The name of the field, potentially an alias if an {@link Alias} annotation is present.
     */
    private String fieldName;
    /**
     * The invoker for the field itself, if accessible.
     */
    private Invoker field;
    /**
     * A cached boolean indicating whether the getter (or field if no getter) has a {@code transient} modifier or
     * annotation.
     */
    private Boolean hasTransientForGetter;
    /**
     * A cached boolean indicating whether the setter (or field if no setter) has a {@code transient} modifier or
     * annotation.
     */
    private Boolean hasTransientForSetter;
    /**
     * A cached boolean indicating whether the property is readable.
     */
    private Boolean isReadable;
    /**
     * A cached boolean indicating whether the property is writable.
     */
    private Boolean isWritable;

    /**
     * Constructs a {@code PropDesc} with a field, getter method, and setter method. Getter and Setter methods are set
     * to be accessible by default.
     *
     * @param field  The {@link Field} object for the property.
     * @param getter The getter {@link Method} for the property.
     * @param setter The setter {@link Method} for the property.
     */
    public PropDesc(final Field field, final Method getter, final Method setter) {
        this(FieldKit.getFieldName(field), getter, setter);
        this.field = FieldInvoker.of(field);
    }

    /**
     * Constructs a {@code PropDesc} with a field name, getter method, and setter method. Getter and Setter methods are
     * set to be accessible by default.
     *
     * @param fieldName The name of the field.
     * @param getter    The getter {@link Method} for the property.
     * @param setter    The setter {@link Method} for the property.
     */
    public PropDesc(final String fieldName, final Method getter, final Method setter) {
        this(fieldName, MethodInvoker.of(getter), MethodInvoker.of(setter));
    }

    /**
     * Constructs a {@code PropDesc} with a field name, getter invoker, and setter invoker.
     *
     * @param fieldName The name of the field.
     * @param getter    The {@link Invoker} for the getter method.
     * @param setter    The {@link Invoker} for the setter method.
     */
    public PropDesc(final String fieldName, final Invoker getter, final Invoker setter) {
        this.fieldName = fieldName;
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * Checks if a field should be ignored for reading, based on the {@link Ignore} annotation. The rules are:
     * <ol>
     * <li>The field itself has an {@link Ignore} annotation.</li>
     * <li>The getter method has an {@link Ignore} annotation.</li>
     * </ol>
     *
     * @param field  The field, may be {@code null}.
     * @param getter The getter method, may be {@code null}.
     * @return {@code true} if reading the field should be ignored, {@code false} otherwise.
     */
    private static boolean isIgnoreGet(final Field field, final Method getter) {
        return AnnoKit.hasAnnotation(field, Ignore.class) || AnnoKit.hasAnnotation(getter, Ignore.class);
    }

    /**
     * Checks if a field should be ignored for writing, based on the {@link Ignore} annotation. The rules are:
     * <ol>
     * <li>The field itself has an {@link Ignore} annotation.</li>
     * <li>The setter method has an {@link Ignore} annotation.</li>
     * </ol>
     *
     * @param field  The field, may be {@code null}.
     * @param setter The setter method, may be {@code null}.
     * @return {@code true} if writing to the field should be ignored, {@code false} otherwise.
     */
    private static boolean isIgnoreSet(final Field field, final Method setter) {
        return AnnoKit.hasAnnotation(field, Ignore.class) || AnnoKit.hasAnnotation(setter, Ignore.class);
    }

    /**
     * Checks if the field or its getter method is marked as {@code transient} (either by keyword or annotation).
     *
     * @param field  The field, may be {@code null}.
     * @param getter The getter method, may be {@code null}.
     * @return {@code true} if the field or getter is transient, {@code false} otherwise.
     */
    private static boolean isTransientForGet(final Field field, final Method getter) {
        boolean isTransient = ModifierKit.hasAny(field, EnumValue.Modifier.TRANSIENT);

        // Check Getter method
        if (!isTransient && null != getter) {
            isTransient = ModifierKit.hasAny(getter, EnumValue.Modifier.TRANSIENT);

            // Check annotation
            if (!isTransient) {
                isTransient = AnnoKit.hasAnnotation(getter, Keys.JAVA_BEANS_TRANSIENT);
            }
        }

        return isTransient;
    }

    /**
     * Checks if the field or its setter method is marked as {@code transient} (either by keyword or annotation).
     *
     * @param field  The field, may be {@code null}.
     * @param setter The setter method, may be {@code null}.
     * @return {@code true} if the field or setter is transient, {@code false} otherwise.
     */
    private static boolean isTransientForSet(final Field field, final Method setter) {
        boolean isTransient = ModifierKit.hasAny(field, EnumValue.Modifier.TRANSIENT);

        // Check Setter method
        if (!isTransient && null != setter) {
            isTransient = ModifierKit.hasAny(setter, EnumValue.Modifier.TRANSIENT);

            // Check annotation
            if (!isTransient) {
                isTransient = AnnoKit.hasAnnotation(setter, Keys.JAVA_BEANS_TRANSIENT);
            }
        }

        return isTransient;
    }

    /**
     * Retrieves the name of the field. If an {@link Alias} annotation is present, its value is used as the name.
     *
     * @return The name of the field.
     */
    public String getFieldName() {
        return this.fieldName;
    }

    /**
     * Retrieves the raw name of the field, ignoring any {@link Alias} annotation.
     *
     * @return The raw name of the field.
     */
    public String getRawFieldName() {
        if (null == this.field) {
            return this.fieldName;
        }

        return this.field.getName();
    }

    /**
     * Retrieves the {@link Field} object associated with this property.
     *
     * @return The {@link Field} object, or {@code null} if the property is not backed by a direct field (e.g., it's a
     *         synthetic property).
     */
    public Field getField() {
        if (null != this.field && this.field instanceof FieldInvoker) {
            return ((FieldInvoker) this.field).getField();
        }
        return null;
    }

    /**
     * Retrieves the generic type of the field. It first attempts to get the type from the field itself. If the field is
     * not available, it tries to get the return type of the getter method. If neither is available, it gets the type of
     * the first parameter of the setter method.
     *
     * @return The generic type of the field.
     */
    public Type getFieldType() {
        if (null != this.field) {
            return this.field.getType();
        }
        return findPropType(getter, setter);
    }

    /**
     * Retrieves the class type of the field. It first attempts to get the class from the field itself. If the field is
     * not available, it tries to get the return class of the getter method. If neither is available, it gets the class
     * of the first parameter of the setter method.
     *
     * @return The class type of the field.
     */
    public Class<?> getFieldClass() {
        if (null != this.field) {
            return this.field.getTypeClass();
        }
        return findPropClass(getter, setter);
    }

    /**
     * Retrieves the {@link Invoker} for the getter method of this property.
     *
     * @return The {@link Invoker} for the getter method, or {@code null} if no getter exists.
     */
    public Invoker getGetter() {
        return this.getter;
    }

    /**
     * Retrieves the {@link Invoker} for the setter method of this property.
     *
     * @return The {@link Invoker} for the setter method, or {@code null} if no setter exists.
     */
    public Invoker getSetter() {
        return this.setter;
    }

    /**
     * Checks if the property is readable (i.e., if a value can be obtained via {@link #getValue(Object, boolean)}).
     *
     * @param checkTransient Whether to consider {@code transient} modifiers or annotations. If {@code true}, a
     *                       transient property will be considered not readable.
     * @return {@code true} if the property is readable, {@code false} otherwise.
     */
    public boolean isReadable(final boolean checkTransient) {
        cacheReadable();

        if (checkTransient && this.hasTransientForGetter) {
            return false;
        }
        return this.isReadable;
    }

    /**
     * Sets the value of the property on the given bean, with automatic type conversion.
     *
     * @param bean        The bean object on which to set the property.
     * @param value       The value to set. Can be of any type.
     * @param ignoreNull  If {@code true}, {@code null} values will be ignored and not set.
     * @param ignoreError If {@code true}, conversion errors and injection errors will be ignored.
     * @return This {@code PropDesc} instance for chaining.
     */
    public PropDesc setValue(
            final Object bean,
            final Object value,
            final boolean ignoreNull,
            final boolean ignoreError) {
        return setValue(bean, value, ignoreNull, ignoreError, true);
    }

    /**
     * Retrieves the value of the property from the given bean. It first attempts to use the getter method. If no getter
     * exists, it tries to access the public field directly. This method does not check any annotations;
     * {@link #isReadable(boolean)} should be called beforehand.
     *
     * @param bean        The bean object from which to get the property value.
     * @param ignoreError If {@code true}, exceptions during value retrieval will be ignored and {@code null} returned.
     * @return The value of the property, or {@code null} if an error occurs and {@code ignoreError} is {@code true}.
     * @throws BeanException If an error occurs during reflection and {@code ignoreError} is {@code false}.
     */
    public Object getValue(final Object bean, final boolean ignoreError) {
        try {
            if (null != this.getter) {
                return this.getter.invoke(bean);
            } else if (null != this.field) {
                return field.invoke(bean);
            }
        } catch (final Exception e) {
            if (!ignoreError) {
                throw new BeanException(e, "Get value of [{}] error!", getFieldName());
            }
        }

        return null;
    }

    /**
     * Returns a string representation of this property descriptor.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return "PropDesc{" + "field=" + field + ", fieldName=" + fieldName + ", getter=" + getter + ", setter=" + setter
                + '}';
    }

    /**
     * Retrieves the value of the property from the given bean, with optional type conversion. It first attempts to use
     * the getter method. If no getter exists, it tries to access the public field directly.
     *
     * @param bean        The bean object from which to get the property value.
     * @param targetType  The target type to convert the property value to. If {@code null}, no conversion is performed.
     * @param ignoreError If {@code true}, conversion errors and injection errors will be ignored.
     * @return The converted property value, or {@code null} if an error occurs or conversion fails and
     *         {@code ignoreError} is {@code true}.
     */
    public Object getValue(final Object bean, final Type targetType, final boolean ignoreError) {
        final Object result = getValue(bean, ignoreError);

        if (null != result && null != targetType) {
            // Attempt to convert the result to the target type. If conversion fails, return null (i.e., skip this
            // property value).
            // When errors are ignored, a failed conversion to the target type should return null.
            // If the original value is returned, it might succeed in collection injection but cause a type conversion
            // error when retrieving from the collection.
            return Convert.convertWithCheck(targetType, result, null, ignoreError);
        }
        return result;
    }

    /**
     * Checks if the property is writable (i.e., if a value can be set via {@link #setValue(Object, Object)}).
     *
     * @param checkTransient Whether to consider {@code transient} modifiers or annotations. If {@code true}, a
     *                       transient property will be considered not writable.
     * @return {@code true} if the property is writable, {@code false} otherwise.
     */
    public boolean isWritable(final boolean checkTransient) {
        cacheWritable();

        if (checkTransient && this.hasTransientForSetter) {
            return false;
        }
        return this.isWritable;
    }

    /**
     * Sets the value of the property on the given bean. It first attempts to use the setter method. If no setter
     * exists, it tries to set the public field directly. This method does not check any annotations;
     * {@link #isWritable(boolean)} should be called beforehand.
     *
     * @param bean  The bean object on which to set the property.
     * @param value The value to set. Must be compatible with the field's type.
     * @return This {@code PropDesc} instance for chaining.
     */
    public PropDesc setValue(final Object bean, final Object value) {
        if (null != this.setter) {
            this.setter.invoke(bean, value);
        } else if (null != this.field) {
            field.invoke(bean, value);
        }
        return this;
    }

    /**
     * Sets the value of the property on the given bean, with automatic type conversion and optional behaviors.
     *
     * @param bean        The bean object on which to set the property.
     * @param value       The value to set. Can be of any type.
     * @param ignoreNull  If {@code true}, {@code null} values will be ignored and not set.
     * @param ignoreError If {@code true}, conversion errors and injection errors will be ignored.
     * @param override    If {@code true}, the target value will always be overwritten. If {@code false}, the existing
     *                    value in the bean will be read first, and if it's not {@code null}, the new value will be
     *                    ignored.
     * @return This {@code PropDesc} instance for chaining.
     */
    public PropDesc setValue(
            final Object bean,
            Object value,
            final boolean ignoreNull,
            final boolean ignoreError,
            final boolean override) {
        if (null == value && ignoreNull) {
            return this;
        }

        // In non-override mode, if the target value already exists, skip it.
        if (!override && null != getValue(bean, ignoreError)) {
            return this;
        }

        // Perform default conversion if types do not match.
        if (null != value) {
            final Class<?> propClass = getFieldClass();
            if (!propClass.isInstance(value)) {
                value = Convert.convertWithCheck(propClass, value, null, ignoreError);
            }
        }

        // Assign the property value.
        if (null != value || !ignoreNull) {
            try {
                this.setValue(bean, value);
            } catch (final Exception e) {
                if (!ignoreError) {
                    throw new BeanException(e, "Set value of [{}] error!", getFieldName());
                }
                // Ignore injection failure.
            }
        }

        return this;
    }

    /**
     * Caches the readability status of the property. If already checked, it returns immediately.
     */
    private void cacheReadable() {
        if (null != this.isReadable) {
            return;
        }

        Field field = null;
        if (this.field instanceof FieldInvoker) {
            field = ((FieldInvoker) this.field).getField();
        }
        Method getterMethod = null;
        if (this.getter instanceof MethodInvoker) {
            getterMethod = ((MethodInvoker) this.getter).getMethod();
        }

        // Check for transient keyword and @Transient annotation.
        this.hasTransientForGetter = isTransientForGet(field, getterMethod);

        // Check for @Ignore annotation.
        if (isIgnoreGet(field, getterMethod)) {
            this.isReadable = false;
            return;
        }

        // Check for getter method existence or public modifier.
        this.isReadable = null != getterMethod || ModifierKit.isPublic(field);
    }

    /**
     * Caches the writability status of the property. If already checked, it returns immediately.
     */
    private void cacheWritable() {
        if (null != this.isWritable) {
            return;
        }

        Field field = null;
        if (this.field instanceof FieldInvoker) {
            field = ((FieldInvoker) this.field).getField();
        }
        Method setterMethod = null;
        if (this.setter instanceof MethodInvoker) {
            setterMethod = ((MethodInvoker) this.setter).getMethod();
        }

        // Check for transient keyword and @Transient annotation.
        this.hasTransientForSetter = isTransientForSet(field, setterMethod);

        // Check for @Ignore annotation.
        if (isIgnoreSet(field, setterMethod)) {
            this.isWritable = false;
            return;
        }

        // Check for setter method existence or public modifier.
        this.isWritable = null != setterMethod || ModifierKit.isPublic(field);
    }

    /**
     * Determines the generic type of the property by checking the getter and setter methods.
     *
     * @param getter The {@link Invoker} for the getter method.
     * @param setter The {@link Invoker} for the setter method.
     * @return The generic type of the property.
     */
    private Type findPropType(final Invoker getter, final Invoker setter) {
        Type type = null;
        if (null != getter) {
            type = getter.getType();
        }
        if (null == type && null != setter) {
            type = setter.getType();
        }
        return type;
    }

    /**
     * Determines the class type of the property by checking the getter and setter methods.
     *
     * @param getter The {@link Invoker} for the getter method.
     * @param setter The {@link Invoker} for the setter method.
     * @return The class type of the property.
     */
    private Class<?> findPropClass(final Invoker getter, final Invoker setter) {
        Class<?> type = null;
        if (null != getter) {
            type = getter.getTypeClass();
        }
        if (null == type && null != setter) {
            type = setter.getTypeClass();
        }
        return type;
    }

}
