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
package org.miaixz.bus.core.xyz;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.convert.CompositeConverter;
import org.miaixz.bus.core.convert.Converter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.annotation.Alias;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.field.FieldInvoker;
import org.miaixz.bus.core.lang.reflect.field.FieldReflect;

/**
 * Utility class for reflection on {@link Field}s.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FieldKit {

    /**
     * Constructs a new FieldKit. Utility class constructor for static access.
     */
    private FieldKit() {
    }

    /**
     * Field cache.
     */
    private static final WeakConcurrentMap<Class<?>, FieldReflect> FIELDS_CACHE = new WeakConcurrentMap<>();

    /**
     * Clears the field cache.
     */
    synchronized static void clearCache() {
        FIELDS_CACHE.clear();
    }

    /**
     * Checks if a field is a reference to the outer class (e.g., `this$0`).
     *
     * @param field The field.
     * @return `true` if it is an outer class reference field.
     */
    public static boolean isOuterClassField(final Field field) {
        return "this$0".equals(field.getName());
    }

    /**
     * Checks if a class contains a field with the specified name, including superclasses.
     *
     * @param beanClass The class to inspect.
     * @param name      The field name.
     * @return `true` if the field exists.
     * @throws SecurityException if a security manager denies access.
     */
    public static boolean hasField(final Class<?> beanClass, final String name) throws SecurityException {
        return null != getField(beanClass, name);
    }

    /**
     * Gets the name of a field, using the value of an {@link Alias} annotation if present.
     *
     * @param field The field.
     * @return The field name.
     */
    public static String getFieldName(final Field field) {
        return getFieldName(field, true);
    }

    /**
     * Gets the name of a field.
     *
     * @param field    The field.
     * @param useAlias If true, checks for an {@link Alias} annotation.
     * @return The field name.
     */
    public static String getFieldName(final Field field, final boolean useAlias) {
        if (null == field) {
            return null;
        }
        if (useAlias) {
            final Alias alias = field.getAnnotation(Alias.class);
            if (null != alias) {
                return alias.value();
            }
        }
        return field.getName();
    }

    /**
     * Gets a declared field of a class by name, including private fields but not inherited ones.
     *
     * @param beanClass The class.
     * @param name      The field name.
     * @return The `Field` object, or `null` if not found.
     */
    public static Field getDeclaredField(final Class<?> beanClass, final String name) {
        final Field[] fields = getDeclaredFields(beanClass, (field -> StringKit.equals(name, field.getName())));
        return ArrayKit.isEmpty(fields) ? null : fields[0];
    }

    /**
     * Gets a field of a class by name, including private and inherited fields.
     *
     * @param beanClass The class.
     * @param name      The field name.
     * @return The `Field` object, or `null` if not found.
     * @throws SecurityException if a security manager denies access.
     */
    public static Field getField(final Class<?> beanClass, final String name) throws SecurityException {
        final Field[] fields = getFields(beanClass, (field -> StringKit.equals(name, field.getName())));
        return ArrayKit.isEmpty(fields) ? null : fields[0];
    }

    /**
     * Gets a map of field names to `Field` objects for a class, including inherited fields.
     *
     * @param beanClass The class.
     * @return A map of field names to fields.
     */
    public static Map<String, Field> getFieldMap(final Class<?> beanClass) {
        final Field[] fields = getFields(beanClass);
        final HashMap<String, Field> map = MapKit.newHashMap(fields.length, true);
        for (final Field field : fields) {
            map.putIfAbsent(field.getName(), field);
        }
        return map;
    }

    /**
     * Gets all fields of a class, including inherited fields.
     *
     * @param beanClass The class.
     * @return An array of fields.
     * @throws SecurityException if a security manager denies access.
     */
    public static Field[] getFields(final Class<?> beanClass) throws SecurityException {
        return getFields(beanClass, null);
    }

    /**
     * Gets all fields of a class that satisfy a predicate, including inherited fields.
     *
     * @param beanClass The class.
     * @param filter    A predicate to filter the fields.
     * @return An array of fields.
     * @throws SecurityException if a security manager denies access.
     */
    public static Field[] getFields(final Class<?> beanClass, final Predicate<Field> filter) throws SecurityException {
        Assert.notNull(beanClass);
        return FIELDS_CACHE.computeIfAbsent(beanClass, FieldReflect::of).getAllFields(filter);
    }

    /**
     * Gets all declared fields of a class that satisfy a predicate (does not include inherited fields).
     *
     * @param beanClass The class.
     * @param filter    A predicate to filter the fields.
     * @return An array of fields.
     * @throws SecurityException if a security manager denies access.
     */
    public static Field[] getDeclaredFields(final Class<?> beanClass, final Predicate<Field> filter)
            throws SecurityException {
        Assert.notNull(beanClass);
        return FIELDS_CACHE.computeIfAbsent(beanClass, FieldReflect::of).getDeclaredFields(filter);
    }

    /**
     * Gets all fields of a class directly via reflection (no cache).
     *
     * @param beanClass            The class.
     * @param withSuperClassFields If true, includes fields from superclasses.
     * @return An array of fields.
     * @throws SecurityException if a security manager denies access.
     */
    public static Field[] getFieldsDirectly(final Class<?> beanClass, final boolean withSuperClassFields)
            throws SecurityException {
        return FieldReflect.of(beanClass).getFieldsDirectly(withSuperClassFields);
    }

    /**
     * Gets the value of a field.
     *
     * @param object    The object (or class for a static field).
     * @param fieldName The field name.
     * @return The field's value.
     * @throws InternalException if access fails.
     */
    public static Object getFieldValue(final Object object, final String fieldName) throws InternalException {
        if (null == object || StringKit.isBlank(fieldName)) {
            return null;
        }
        return getFieldValue(
                object,
                getField(object instanceof Class ? (Class<?>) object : object.getClass(), fieldName));
    }

    /**
     * Gets the value of a static field.
     *
     * @param field The field.
     * @return The field's value.
     * @throws InternalException if access fails.
     */
    public static Object getStaticFieldValue(final Field field) throws InternalException {
        return getFieldValue(null, field);
    }

    /**
     * Gets the value of a field.
     *
     * @param object The object (or `null` for a static field).
     * @param field  The field.
     * @return The field's value.
     * @throws InternalException if access fails.
     */
    public static Object getFieldValue(Object object, final Field field) throws InternalException {
        if (null == field) {
            return null;
        }
        if (object instanceof Class) {
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
     * Gets the values of all fields of an object.
     *
     * @param object The object (or class for static fields).
     * @return An array of field values.
     */
    public static Object[] getFieldsValue(final Object object) {
        return getFieldsValue(object, null);
    }

    /**
     * Gets the values of all fields of an object that satisfy a predicate.
     *
     * @param object The object.
     * @param filter The field filter.
     * @return An array of field values.
     */
    public static Object[] getFieldsValue(final Object object, final Predicate<Field> filter) {
        if (null != object) {
            final Field[] fields = getFields(object instanceof Class ? (Class<?>) object : object.getClass(), filter);
            if (null != fields) {
                return ArrayKit.mapToArray(fields, field -> getFieldValue(object, field), Object[]::new);
            }
        }
        return null;
    }

    /**
     * Gets a map of all field names to their corresponding values.
     *
     * @param object The object.
     * @return A map of field names to values.
     */
    public static Object getFieldsAndValue(final Object object) {
        if (null != object) {
            final Field[] fields = getFields(object instanceof Class ? (Class<?>) object : object.getClass());
            if (null != fields) {
                Map<String, Object> map = new HashMap<>();
                for (Field field : fields) {
                    Object obj = getFieldValue(object, field);
                    if (ObjectKit.isNotEmpty(obj) && !isSerialVersionUID(field)) {
                        map.put(field.getName(), getFieldValue(object, field));
                    }
                }
                return map;
            }
        }
        return null;
    }

    /**
     * Gets a map of field names to their values for all fields that satisfy a predicate.
     *
     * @param object The object.
     * @param filter The field filter.
     * @return A map of field names to values.
     */
    public static Object getFieldsAndValue(final Object object, final Predicate<Field> filter) {
        if (null != object) {
            final Field[] fields = getFields(object instanceof Class ? (Class<?>) object : object.getClass(), filter);
            if (null != fields) {
                Map<String, Object> map = new HashMap<>();
                for (Field field : fields) {
                    Object obj = getFieldValue(object, field);
                    if (ObjectKit.isNotEmpty(obj) && !isSerialVersionUID(field)) {
                        map.put(field.getName(), getFieldValue(object, field));
                    }
                }
                return map;
            }
        }
        return null;
    }

    /**
     * Sets the value of a field.
     *
     * @param object    The object (or class for a static field).
     * @param fieldName The field name.
     * @param value     The new value.
     * @throws InternalException if the field doesn't exist or access fails.
     */
    public static void setFieldValue(final Object object, final String fieldName, final Object value)
            throws InternalException {
        Assert.notNull(object, "Object must be not null !");
        Assert.notBlank(fieldName);

        final Field field = getField((object instanceof Class) ? (Class<?>) object : object.getClass(), fieldName);
        Assert.notNull(field, "Field [{}] is not exist in [{}]", fieldName, object.getClass().getName());
        setFieldValue(object, field, value);
    }

    /**
     * Sets the value of a static field.
     *
     * @param field The field.
     * @param value The new value.
     * @throws InternalException if access fails.
     */
    public static void setStaticFieldValue(final Field field, final Object value) throws InternalException {
        setFieldValue(null, field, value);
    }

    /**
     * Sets the value of a field, with automatic type conversion.
     *
     * @param object The object (or `null` for a static field).
     * @param field  The field.
     * @param value  The new value.
     * @throws InternalException if access fails.
     */
    public static void setFieldValue(final Object object, final Field field, Object value) throws InternalException {
        setFieldValue(object, field, value, CompositeConverter.getInstance());
    }

    /**
     * Sets the value of a field, with automatic type conversion using a specified converter.
     *
     * @param object    The object (or `null` for a static field).
     * @param field     The field.
     * @param value     The new value.
     * @param converter The converter to use for type conversion.
     * @throws InternalException if access fails.
     */
    public static void setFieldValue(
            final Object object,
            final Field field,
            final Object value,
            final Converter converter) throws InternalException {
        Assert.notNull(field, "Field in [{}] not exist !", object);

        FieldInvoker.of(field).setConverter(converter).invokeSet(object, value);
    }

    /**
     * Checks if a field is the `serialVersionUID` field.
     *
     * @param field The field.
     * @return `true` if it is the `serialVersionUID` field.
     */
    public static boolean isSerialVersionUID(Field field) {
        return "serialVersionUID".equals(field.getName())
                && (Long.class.equals(field.getType()) || long.class.equals(field.getType()))
                && field.getModifiers() == (Modifier.PRIVATE + Modifier.STATIC + Modifier.FINAL);
    }

}
