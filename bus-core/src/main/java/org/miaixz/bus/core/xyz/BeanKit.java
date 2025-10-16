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

import java.beans.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.miaixz.bus.core.bean.BeanCache;
import org.miaixz.bus.core.bean.DynaBean;
import org.miaixz.bus.core.bean.copier.BeanCopier;
import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.bean.copier.ValueProvider;
import org.miaixz.bus.core.bean.desc.BeanDesc;
import org.miaixz.bus.core.bean.desc.BeanDescFactory;
import org.miaixz.bus.core.bean.desc.PropDesc;
import org.miaixz.bus.core.bean.path.BeanPath;
import org.miaixz.bus.core.center.map.BeanMap;
import org.miaixz.bus.core.center.map.CaseInsensitiveMap;
import org.miaixz.bus.core.center.map.Dictionary;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.convert.RecordConverter;
import org.miaixz.bus.core.lang.annotation.Readables;
import org.miaixz.bus.core.lang.annotation.Writables;
import org.miaixz.bus.core.lang.exception.BeanException;
import org.miaixz.bus.core.lang.mutable.MutableEntry;

/**
 * Bean utility class.
 *
 * <p>
 * A class with getter and setter methods for its properties can be called a JavaBean.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanKit {

    /**
     * Creates a dynamic Bean.
     *
     * @param bean A regular Bean or a Map.
     * @return A {@link DynaBean} instance.
     */
    public static DynaBean createDynaBean(final Object bean) {
        return DynaBean.of(bean);
    }

    /**
     * Finds a type converter {@link PropertyEditor}.
     *
     * @param type The target type that needs to be converted.
     * @return A {@link PropertyEditor} instance.
     */
    public static PropertyEditor findEditor(final Class<?> type) {
        return PropertyEditorManager.findEditor(type);
    }

    /**
     * Retrieves {@link BeanDesc} Bean description information.
     *
     * @param clazz The Bean class.
     * @return A {@link BeanDesc} instance.
     */
    public static BeanDesc getBeanDesc(final Class<?> clazz) {
        return BeanDescFactory.getBeanDesc(clazz);
    }

    /**
     * Iterates through the properties of a Bean.
     *
     * @param clazz  The Bean class.
     * @param action The consumer to process each property description.
     */
    public static void descForEach(final Class<?> clazz, final Consumer<? super PropDesc> action) {
        getBeanDesc(clazz).getProps().forEach(action);
    }

    /**
     * Retrieves an array of Bean field descriptions.
     *
     * @param clazz The Bean class.
     * @return An array of field descriptions.
     * @throws BeanException If an error occurs while getting properties.
     */
    public static PropertyDescriptor[] getPropertyDescriptors(final Class<?> clazz) throws BeanException {
        final BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (final IntrospectionException e) {
            throw new BeanException(e);
        }
        return ArrayKit.filter(beanInfo.getPropertyDescriptors(), t -> {
            // Filter out the getClass method
            return !"class".equals(t.getName());
        });
    }

    /**
     * Retrieves a map of field names to field descriptions. The result is cached in {@link BeanCache}.
     *
     * @param clazz      The Bean class.
     * @param ignoreCase Whether to ignore case for field names.
     * @return A map of field names to field descriptions.
     * @throws BeanException If an error occurs while getting properties.
     */
    public static Map<String, PropertyDescriptor> getPropertyDescriptorMap(
            final Class<?> clazz,
            final boolean ignoreCase) throws BeanException {
        return BeanCache.INSTANCE
                .getPropertyDescriptorMap(clazz, ignoreCase, () -> internalGetPropertyDescriptorMap(clazz, ignoreCase));
    }

    /**
     * Retrieves a map of field names to field descriptions. Internal use, directly gets PropertyDescriptor of the Bean
     * class.
     *
     * @param clazz      The Bean class.
     * @param ignoreCase Whether to ignore case for field names.
     * @return A map of field names to field descriptions.
     * @throws BeanException If an error occurs while getting properties.
     */
    private static Map<String, PropertyDescriptor> internalGetPropertyDescriptorMap(
            final Class<?> clazz,
            final boolean ignoreCase) throws BeanException {
        final PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(clazz);
        final Map<String, PropertyDescriptor> map = ignoreCase
                ? new CaseInsensitiveMap<>(propertyDescriptors.length, 1f)
                : new HashMap<>(propertyDescriptors.length, 1);

        for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            map.put(propertyDescriptor.getName(), propertyDescriptor);
        }
        return map;
    }

    /**
     * Retrieves the property descriptor for a Bean class, case-sensitive.
     *
     * @param clazz     The Bean class.
     * @param fieldName The field name.
     * @return The PropertyDescriptor, or {@code null} if not found.
     * @throws BeanException If an error occurs while getting properties.
     */
    public static PropertyDescriptor getPropertyDescriptor(final Class<?> clazz, final String fieldName)
            throws BeanException {
        return getPropertyDescriptor(clazz, fieldName, false);
    }

    /**
     * Retrieves the property descriptor for a Bean class.
     *
     * @param clazz      The Bean class.
     * @param fieldName  The field name.
     * @param ignoreCase Whether to ignore case for field names.
     * @return The PropertyDescriptor, or {@code null} if not found.
     * @throws BeanException If an error occurs while getting properties.
     */
    public static PropertyDescriptor getPropertyDescriptor(
            final Class<?> clazz,
            final String fieldName,
            final boolean ignoreCase) throws BeanException {
        final Map<String, PropertyDescriptor> map = getPropertyDescriptorMap(clazz, ignoreCase);
        return (null == map) ? null : map.get(fieldName);
    }

    /**
     * Retrieves the field value directly via reflection, without invoking getter methods. Supports Map types, where
     * {@code fieldNameOrIndex} is the key.
     * <ul>
     * <li>Map: {@code fieldNameOrIndex} should be the key to get the corresponding value.</li>
     * <li>Collection: If {@code fieldNameOrIndex} is a number, returns the value at that index. If not a number,
     * iterates the collection and returns the value of the sub-bean's corresponding name.</li>
     * <li>Array: If {@code fieldNameOrIndex} is a number, returns the value at that index. If not a number, iterates
     * the array and returns the value of the sub-bean's corresponding name.</li>
     * </ul>
     *
     * @param bean             The Bean object.
     * @param fieldNameOrIndex The field name or index (supports negative numbers).
     * @return The field value.
     */
    public static Object getFieldValue(final Object bean, final String fieldNameOrIndex) {
        if (null == bean || null == fieldNameOrIndex) {
            return null;
        }

        if (bean instanceof Map) {
            return ((Map<?, ?>) bean).get(fieldNameOrIndex);
        } else if (bean instanceof Collection) {
            try {
                return CollKit.get((Collection<?>) bean, Integer.parseInt(fieldNameOrIndex));
            } catch (final NumberFormatException e) {
                // Not a number
                return CollKit.map((Collection<?>) bean, (beanEle) -> getFieldValue(beanEle, fieldNameOrIndex), false);
            }
        } else if (ArrayKit.isArray(bean)) {
            try {
                return ArrayKit.get(bean, Integer.parseInt(fieldNameOrIndex));
            } catch (final NumberFormatException e) {
                // Not a number
                return ArrayKit.map(bean, Object.class, (beanEle) -> getFieldValue(beanEle, fieldNameOrIndex));
            }
        } else {// Regular Bean object
            return FieldKit.getFieldValue(bean, fieldNameOrIndex);
        }
    }

    /**
     * Retrieves the property value from a Bean.
     *
     * @param <T>        The type of the property value.
     * @param bean       The Bean object, supports Map, List, Collection, Array.
     * @param expression The expression, e.g., "person.friend[5].name".
     * @return The Bean property value. Returns {@code null} if bean is {@code null} or expression is empty.
     * @see BeanPath#getValue(Object)
     */
    public static <T> T getProperty(final Object bean, final String expression) {
        if (null == bean || StringKit.isBlank(expression)) {
            return null;
        }

        // First try to get the property directly
        if (bean instanceof Map<?, ?> map) {
            if (map.containsKey(expression)) {
                return (T) map.get(expression);
            }
        }
        return (T) BeanPath.of(expression).getValue(bean);
    }

    /**
     * Sets the property value in a Bean.
     *
     * @param bean       The Bean object, supports Map, List, Collection, Array.
     * @param expression The expression, e.g., "person.friend[5].name".
     * @param value      The property value.
     * @see BeanPath#setValue(Object, Object)
     */
    public static void setProperty(final Object bean, final String expression, final Object value) {
        BeanPath.of(expression).setValue(bean, value);
    }

    /**
     * Converts an object or Map to a Bean.
     *
     * @param <T>    The type of the Bean to convert to.
     * @param source The Bean object or Map.
     * @param clazz  The target Bean type.
     * @return The Bean object.
     */
    public static <T> T toBean(final Object source, final Class<T> clazz) {
        return toBean(source, clazz, null);
    }

    /**
     * Converts an object or Map to a Bean.
     *
     * @param <T>     The type of the Bean to convert to.
     * @param source  The Bean object or Map.
     * @param clazz   The target Bean type.
     * @param options Property copy options.
     * @return The Bean object.
     */
    public static <T> T toBean(final Object source, final Class<T> clazz, final CopyOptions options) {
        return toBean(source, () -> ReflectKit.newInstanceIfPossible(clazz), options);
    }

    /**
     * Converts an object or Map to a Bean.
     *
     * @param <T>            The type of the Bean to convert to.
     * @param source         The Bean object, Map, or {@link ValueProvider}.
     * @param targetSupplier The supplier for creating the target Bean.
     * @param options        Property copy options.
     * @return The Bean object.
     */
    public static <T> T toBean(final Object source, final Supplier<T> targetSupplier, final CopyOptions options) {
        if (null == source || null == targetSupplier) {
            return null;
        }
        final T target = targetSupplier.get();
        copyProperties(source, target, options);
        return target;
    }

    /**
     * Core method for filling a Bean.
     *
     * @param <T>           The Bean type.
     * @param bean          The Bean instance.
     * @param valueProvider The value provider.
     * @param copyOptions   Copy options, see {@link CopyOptions}.
     * @return The filled Bean.
     */
    public static <T> T fillBean(
            final T bean,
            final ValueProvider<String> valueProvider,
            final CopyOptions copyOptions) {
        if (null == valueProvider) {
            return bean;
        }

        return BeanCopier.of(valueProvider, bean, copyOptions).copy();
    }

    /**
     * Fills a Bean object with values from a Map.
     *
     * @param <T>         The Bean type.
     * @param map         The Map.
     * @param bean        The Bean instance.
     * @param copyOptions Property copy options {@link CopyOptions}.
     * @return The filled Bean.
     */
    public static <T> T fillBeanWithMap(final Map<?, ?> map, final T bean, final CopyOptions copyOptions) {
        if (MapKit.isEmpty(map)) {
            return bean;
        }
        return copyProperties(map, bean, copyOptions);
    }

    /**
     * Wraps a Bean into a Map form.
     *
     * @param bean The Bean instance.
     * @return A {@link BeanMap} instance.
     */
    public static Map<String, Object> toBeanMap(final Object bean) {
        return BeanMap.of(bean);
    }

    /**
     * Converts some properties of a Bean into a Map. Optionally specifies which property values to copy. By default,
     * {@code null} values are not ignored.
     *
     * @param bean       The Bean instance.
     * @param properties The properties to copy. {@code null} or empty means copy all values.
     * @return The resulting Map.
     */
    public static Map<String, Object> beanToMap(final Object bean, final String... properties) {
        int mapSize = 16;
        UnaryOperator<MutableEntry<Object, Object>> editor = null;
        if (ArrayKit.isNotEmpty(properties)) {
            mapSize = properties.length;
            final Set<String> propertiesSet = SetKit.of(properties);
            editor = entry -> {
                final String key = StringKit.toStringOrNull(entry.getKey());
                entry.setKey(propertiesSet.contains(key) ? key : null);
                return entry;
            };
        }

        // If properties to copy are specified, do not ignore null values
        return beanToMap(bean, new LinkedHashMap<>(mapSize, 1), false, editor);
    }

    /**
     * Converts an object to a Map.
     *
     * @param bean              The bean object.
     * @param isToUnderlineCase Whether to convert to underscore case.
     * @param ignoreNullValue   Whether to ignore fields with null values.
     * @return The resulting Map.
     */
    public static Map<String, Object> beanToMap(
            final Object bean,
            final boolean isToUnderlineCase,
            final boolean ignoreNullValue) {
        if (null == bean) {
            return null;
        }
        return beanToMap(bean, new LinkedHashMap<>(), isToUnderlineCase, ignoreNullValue);
    }

    /**
     * Converts an object to a Map.
     *
     * @param bean              The bean object.
     * @param targetMap         The target Map to fill.
     * @param isToUnderlineCase Whether to convert to underscore case.
     * @param ignoreNullValue   Whether to ignore fields with null values.
     * @return The resulting Map.
     */
    public static Map<String, Object> beanToMap(
            final Object bean,
            final Map<String, Object> targetMap,
            final boolean isToUnderlineCase,
            final boolean ignoreNullValue) {
        if (null == bean) {
            return null;
        }

        return beanToMap(bean, targetMap, ignoreNullValue, entry -> {
            final String key = StringKit.toStringOrNull(entry.getKey());
            entry.setKey(isToUnderlineCase ? StringKit.toUnderlineCase(key) : key);
            return entry;
        });
    }

    /**
     * Converts an object to a Map. By implementing {@link UnaryOperator}, custom field values can be defined. If the
     * editor returns {@code null}, the field is ignored, allowing for:
     *
     * <pre>
     * 1. Field filtering, to remove unwanted fields.
     * 2. Field transformation, e.g., camelCase to underscore_case.
     * 3. Custom field prefixes or suffixes, etc.
     * </pre>
     *
     * @param <V>             The type of values in the Map.
     * @param bean            The bean object.
     * @param targetMap       The target Map to fill.
     * @param ignoreNullValue Whether to ignore fields with null values.
     * @param keyEditor       The property field (Map key) editor, used for filtering and editing keys. If this editor
     *                        returns null, the field is ignored.
     * @return The resulting Map.
     */
    public static <V> Map<String, V> beanToMap(
            final Object bean,
            final Map<String, V> targetMap,
            final boolean ignoreNullValue,
            final UnaryOperator<MutableEntry<Object, Object>> keyEditor) {
        if (null == bean) {
            return null;
        }

        return BeanCopier
                .of(bean, targetMap, CopyOptions.of().setIgnoreNullValue(ignoreNullValue).setFieldEditor(keyEditor))
                .copy();
    }

    /**
     * Converts an object to a Map using custom {@link CopyOptions}. This allows for:
     *
     * <pre>
     * 1. Field filtering, to remove unwanted fields.
     * 2. Field transformation, e.g., camelCase to underscore_case.
     * 3. Custom field prefixes or suffixes, etc.
     * 4. Field value processing.
     * ...
     * </pre>
     *
     * @param <V>         The type of values in the Map.
     * @param bean        The bean object.
     * @param targetMap   The target Map to fill.
     * @param copyOptions The copy options.
     * @return The resulting Map.
     */
    public static <V> Map<String, V> beanToMap(
            final Object bean,
            final Map<String, V> targetMap,
            final CopyOptions copyOptions) {
        if (null == bean) {
            return null;
        }

        return BeanCopier.of(bean, targetMap, copyOptions).copy();
    }

    /**
     * Creates a corresponding Class object based on the Bean object's properties, ignoring certain properties.
     *
     * @param <T>              The object type.
     * @param source           The source Bean object.
     * @param tClass           The target Class.
     * @param ignoreProperties The list of properties not to copy.
     * @return The target object.
     */
    public static <T> T copyProperties(final Object source, final Class<T> tClass, final String... ignoreProperties) {
        if (null == source) {
            return null;
        }
        if (RecordKit.isRecord(tClass)) {
            // When converting records, ignoreProperties is invalid
            return RecordConverter.INSTANCE.convert(tClass, source);
        }
        final T target = ReflectKit.newInstanceIfPossible(tClass);
        return copyProperties(source, target, CopyOptions.of().setIgnoreProperties(ignoreProperties));
    }

    /**
     * Copies properties from a source Bean object to a target Bean object. The editable class can be used to restrict
     * copied properties, e.g., to copy only some properties of a parent class.
     *
     * @param <T>              The target type.
     * @param source           The source Bean object.
     * @param target           The target Bean object.
     * @param ignoreProperties The list of properties not to copy.
     * @return The target object.
     */
    public static <T> T copyProperties(final Object source, final T target, final String... ignoreProperties) {
        return copyProperties(source, target, CopyOptions.of().setIgnoreProperties(ignoreProperties));
    }

    /**
     * Copies properties from a source Bean object to a target Bean object.
     *
     * @param <T>        The target type.
     * @param source     The source Bean object.
     * @param target     The target Bean object.
     * @param ignoreCase Whether to ignore case during property matching.
     * @return The target object.
     */
    public static <T> T copyProperties(final Object source, final T target, final boolean ignoreCase) {
        return BeanCopier.of(source, target, CopyOptions.of().setIgnoreCase(ignoreCase)).copy();
    }

    /**
     * Copies properties from a source Bean object to a target Bean object. The editable class can be used to restrict
     * copied properties, e.g., to copy only some properties of a parent class.
     *
     * @param <T>         The target type.
     * @param source      The source Bean object.
     * @param target      The target Bean object.
     * @param copyOptions Copy options, see {@link CopyOptions}.
     * @return The target object.
     */
    public static <T> T copyProperties(final Object source, final T target, final CopyOptions copyOptions) {
        if (null == source || null == target) {
            return null;
        }
        return BeanCopier.of(source, target, ObjectKit.defaultIfNull(copyOptions, CopyOptions::of)).copy();
    }

    /**
     * Copies Bean properties within a collection. This method iterates through each Bean in the collection, copies its
     * properties, and adds it to a new {@link List}.
     *
     * @param collection The original Bean collection.
     * @param targetType The target Bean type.
     * @param <T>        The Bean type.
     * @return A new List with copied Beans.
     */
    public static <T> List<T> copyToList(final Collection<?> collection, final Class<T> targetType) {
        return copyToList(collection, targetType, CopyOptions.of());
    }

    /**
     * Copies Bean properties within a collection. This method iterates through each Bean in the collection, copies its
     * properties, and adds it to a new {@link List}.
     *
     * @param collection  The original Bean collection.
     * @param targetType  The target Bean type.
     * @param copyOptions Copy options.
     * @param <T>         The Bean type.
     * @return A new List with copied Beans.
     */
    public static <T> List<T> copyToList(
            final Collection<?> collection,
            final Class<T> targetType,
            final CopyOptions copyOptions) {
        if (null == collection) {
            return null;
        }
        if (collection.isEmpty()) {
            return new ArrayList<>(0);
        }

        if (ClassKit.isBasicType(targetType) || String.class == targetType) {
            return Convert.toList(targetType, collection);
        }

        return collection.stream().map((source) -> {

            final T target = ReflectKit.newInstanceIfPossible(targetType);
            copyProperties(source, target, copyOptions);
            return target;
        }).collect(Collectors.toList());
    }

    /**
     * Checks if the given Bean's class name matches the specified class name string. If {@code isSimple} is
     * {@code true}, only the class name is matched, ignoring the package name. For example: "org.miaixz.TestEntity"
     * matches "TestEntity". If {@code isSimple} is {@code false}, the full class name including package is matched. For
     * example: "org.miaixz.TestEntity" matches "org.miaixz.TestEntity".
     *
     * @param bean          The Bean instance.
     * @param beanClassName The class name of the Bean.
     * @param isSimple      Whether to match only the class name and ignore the package name.
     * @return {@code true} if the class name matches, {@code false} otherwise.
     */
    public static boolean isMatchName(final Object bean, final String beanClassName, final boolean isSimple) {
        if (null == bean || StringKit.isBlank(beanClassName)) {
            return false;
        }
        return ClassKit.getClassName(bean, isSimple)
                .equals(isSimple ? StringKit.upperFirst(beanClassName) : beanClassName);
    }

    /**
     * Edits the fields of a Bean. Static fields are not processed. For example, to perform null checks or convert null
     * to "" for specified fields.
     *
     * @param bean   The Bean instance.
     * @param editor The editor function.
     * @param <T>    The type of the Bean being edited.
     * @return The edited Bean.
     */
    public static <T> T edit(final T bean, final UnaryOperator<Field> editor) {
        if (bean == null) {
            return null;
        }

        final Field[] fields = FieldKit.getFields(bean.getClass());
        for (final Field field : fields) {
            if (ModifierKit.isStatic(field)) {
                continue;
            }
            editor.apply(field);
        }
        return bean;
    }

    /**
     * Trims String fields in a Bean. This method directly modifies the passed Bean. Typically, when a bean is used to
     * bind input from a page, user input may have leading/trailing spaces, which usually need to be removed before
     * saving to the database.
     *
     * @param <T>         The Bean type.
     * @param bean        The Bean object.
     * @param ignoreField A list of field names (case-insensitive) to ignore during trimming.
     * @return The processed Bean object.
     */
    public static <T> T trimStringField(final T bean, final String... ignoreField) {
        return edit(bean, (field) -> {
            if (ignoreField != null && ArrayKit.containsIgnoreCase(ignoreField, field.getName())) {
                // Do not process ignored fields
                return field;
            }
            if (String.class.equals(field.getType())) {
                // Only process String fields
                final String val = (String) FieldKit.getFieldValue(bean, field);
                if (null != val) {
                    final String trimVal = StringKit.trim(val);
                    if (!val.equals(trimVal)) {
                        // Only process if field value is not null and has leading/trailing spaces
                        FieldKit.setFieldValue(bean, field, trimVal);
                    }
                }
            }
            return field;
        });
    }

    /**
     * Checks if a Bean is an empty object. An empty object means it is {@code null} or all its properties are
     * {@code null}. This method does not check static properties.
     *
     * @param bean             The Bean object.
     * @param ignoreFieldNames Field names to ignore during check.
     * @return {@code true} if empty, {@code false} if not empty.
     */
    public static boolean isEmpty(final Object bean, final String... ignoreFieldNames) {
        // Does not contain non-null fields
        return !isNotEmpty(bean, ignoreFieldNames);
    }

    /**
     * Checks if a Bean is a non-empty object. A non-empty object means it is not {@code null} or it contains at least
     * one non-{@code null} property.
     *
     * @param bean             The Bean object.
     * @param ignoreFieldNames Field names to ignore during check.
     * @return {@code true} if non-empty, {@code false} if empty.
     */
    public static boolean isNotEmpty(final Object bean, final String... ignoreFieldNames) {
        if (null == bean) {
            return false;
        }

        // Equivalent to hasNoneNullField
        return checkBean(
                bean,
                field -> (!ArrayKit.contains(ignoreFieldNames, field.getName()))
                        && null != FieldKit.getFieldValue(bean, field));
    }

    /**
     * Checks if it is a readable Bean object. The determination method is:
     *
     * <pre>
     *     1. Whether there is a getXXX method or isXXX method with no parameters.
     *     2. Whether there are public fields.
     * </pre>
     *
     * @param clazz The class to test.
     * @return {@code true} if it is a readable Bean object, {@code false} otherwise.
     * @see #hasGetter(Class)
     * @see #hasPublicField(Class)
     */
    public static boolean isReadableBean(final Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        if (clazz == String.class) {
            // String has getter methods, but it's a string, not a Bean
            return false;
        }

        if (AnnoKit.hasAnnotation(clazz, Readables.class)) {
            return true;
        }

        return hasGetter(clazz) || hasPublicField(clazz);
    }

    /**
     * Checks if it is a writable Bean object. The determination method is:
     *
     * <pre>
     *     1. Whether there is a setXXX method with one parameter.
     *     2. Whether there are public fields.
     * </pre>
     *
     * @param clazz The class to test.
     * @return {@code true} if it is a writable Bean object, {@code false} otherwise.
     * @see #hasSetter(Class)
     * @see #hasPublicField(Class)
     */
    public static boolean isWritableBean(final Class<?> clazz) {
        if (null == clazz) {
            return false;
        }

        // Exclude predefined classes that define setXXX
        if (Dictionary.class == clazz) {
            return false;
        }

        if (AnnoKit.hasAnnotation(clazz, Writables.class)) {
            return true;
        }

        return hasSetter(clazz) || hasPublicField(clazz);
    }

    /**
     * Checks if there is a Setter method. Determines if there is a setXXX method with only one parameter.
     *
     * @param clazz The class to test.
     * @return {@code true} if a Setter method exists, {@code false} otherwise.
     */
    public static boolean hasSetter(final Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        // Exclude predefined classes that define setXXX
        if (Dictionary.class == clazz) {
            return false;
        }

        if (ClassKit.isNormalClass(clazz)) {
            for (final Method method : clazz.getMethods()) {
                if (method.getParameterCount() == 1 && method.getName().startsWith("set")) {
                    // A standard JavaBean is considered to have a standard setXXX method
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if there is a getXXX or isXXX method.
     *
     * @param clazz The class to test.
     * @return {@code true} if a getXXX or isXXX method exists, {@code false} otherwise.
     */
    public static boolean hasGetter(final Class<?> clazz) {
        if (ClassKit.isNormalClass(clazz)) {
            for (final Method method : clazz.getMethods()) {
                if (method.getParameterCount() == 0) {
                    final String name = method.getName();
                    if (name.startsWith("get") || name.startsWith("is")) {
                        if (!"getClass".equals(name)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if the specified class has public fields (excluding static fields).
     *
     * @param clazz The class to test.
     * @return {@code true} if public fields exist, {@code false} otherwise.
     */
    public static boolean hasPublicField(final Class<?> clazz) {
        if (ClassKit.isNormalClass(clazz)) {
            for (final Field field : clazz.getFields()) {
                if (ModifierKit.isPublic(field) && !ModifierKit.isStatic(field)) {
                    // Non-static public fields
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the Bean contains properties with {@code null} values. Returns {@code true} if the Bean itself is
     * {@code null}.
     *
     * @param bean             The Bean object.
     * @param ignoreFieldNames Field names to ignore during check.
     * @return {@code true} if it contains {@code null} properties, {@code false} otherwise.
     */
    public static boolean hasNullField(final Object bean, final String... ignoreFieldNames) {
        return checkBean(
                bean,
                field -> (!ArrayKit.contains(ignoreFieldNames, field.getName()))
                        && null == FieldKit.getFieldValue(bean, field));
    }

    /**
     * Checks if the Bean contains properties with {@code null} values, or if a {@link CharSequence} field is empty
     * (null or ""). Returns {@code true} if the Bean itself is {@code null}.
     *
     * @param bean             The Bean object.
     * @param ignoreFieldNames Field names to ignore during check.
     * @return {@code true} if it contains {@code null} or empty properties, {@code false} otherwise.
     */
    public static boolean hasEmptyField(final Object bean, final String... ignoreFieldNames) {
        return checkBean(
                bean,
                field -> (!ArrayKit.contains(ignoreFieldNames, field.getName()))
                        && ObjectKit.isEmptyIfString(FieldKit.getFieldValue(bean, field)));
    }

    /**
     * Checks a Bean. Iterates through the fields of the Bean and asserts them. If the assertion for a field is
     * {@code true}, returns {@code true} and stops checking subsequent fields. If the assertion for a field is
     * {@code false}, continues checking subsequent fields.
     *
     * @param bean      The Bean instance.
     * @param predicate The predicate to apply to each field.
     * @return {@code true} if any field triggers the predicate to be true, {@code false} otherwise.
     */
    public static boolean checkBean(final Object bean, final Predicate<Field> predicate) {
        if (null == bean) {
            return true;
        }
        for (final Field field : FieldKit.getFields(bean.getClass())) {
            if (ModifierKit.isStatic(field)) {
                continue;
            }
            if (predicate.test(field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the field name corresponding to a Getter or Setter method name. The rules are as follows:
     * <ul>
     * <li>getXxxx gets xxxx, e.g., getName gets name.</li>
     * <li>setXxxx gets xxxx, e.g., setName gets name.</li>
     * <li>isXxxx gets xxxx, e.g., isName gets name.</li>
     * <li>Other method names that do not satisfy the rules throw {@link IllegalArgumentException}.</li>
     * </ul>
     *
     * @param getterOrSetterName The Getter or Setter method name.
     * @return The field name.
     * @throws IllegalArgumentException If it is not a Getter or Setter method.
     */
    public static String getFieldName(final String getterOrSetterName) {
        if (getterOrSetterName.startsWith("get") || getterOrSetterName.startsWith("set")) {
            return StringKit.removePreAndLowerFirst(getterOrSetterName, 3);
        } else if (getterOrSetterName.startsWith("is")) {
            return StringKit.removePreAndLowerFirst(getterOrSetterName, 2);
        } else {
            throw new IllegalArgumentException("Invalid Getter or Setter name: " + getterOrSetterName);
        }
    }

}
