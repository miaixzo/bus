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
package org.miaixz.bus.shade.screw.mapping;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * A utility class for mapping {@link ResultSet} data to Java objects.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Mapping {

    /**
     * Private constructor to prevent instantiation.
     */
    private Mapping() {
    }

    /**
     * Converts a single row from a {@link ResultSet} into an object of the specified class.
     *
     * @param <T>       The generic type of the target object.
     * @param resultSet The {@link ResultSet} containing the data.
     * @param clazz     The target class to which the data will be mapped.
     * @return An instance of the target class populated with data from the result set.
     * @throws InternalException if a mapping or reflection error occurs.
     */
    public static <T> T convert(ResultSet resultSet, Class<T> clazz) throws InternalException {
        // Map to store column names and their corresponding values.
        Map<String, Object> values = new HashMap<>(Normal._16);
        try {
            // Get metadata from the result set.
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            // Iterate through the result set.
            while (resultSet.next()) {
                // Store column values in the map.
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    values.put(columnName, resultSet.getString(columnName));
                }
            }
            // If there are results, create and populate the object.
            if (!values.isEmpty()) {
                // Get field and method information for the target class.
                List<FieldMethod> fieldMethods = getFieldMethods(clazz);
                // Create and populate the object.
                return getObject(clazz, fieldMethods, values);
            }
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * Converts multiple rows from a {@link ResultSet} into a list of objects of the specified class.
     *
     * @param <T>       The generic type of the target objects.
     * @param resultSet The {@link ResultSet} containing the data.
     * @param clazz     The target class to which the data will be mapped.
     * @return A list of instances of the target class.
     * @throws InternalException if a mapping or reflection error occurs.
     */
    public static <T> List<T> convertList(ResultSet resultSet, Class<T> clazz) throws InternalException {
        // List to store maps of column names and values for each row.
        List<Map<String, Object>> values = new ArrayList<>(Normal._16);
        // List to store the resulting objects.
        List<T> list = new ArrayList<>();
        try {
            // Get metadata from the result set.
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            // Iterate through the result set.
            while (resultSet.next()) {
                // Map for the current row.
                HashMap<String, Object> value = new HashMap<>(Normal._16);
                // Get column values for the current row.
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    value.put(columnName, resultSet.getString(i));
                }
                values.add(value);
            }
            // Get field and method information for the target class.
            List<FieldMethod> fieldMethods = getFieldMethods(clazz);
            // Create and populate objects for each row.
            for (Map<String, Object> map : values) {
                T rsp = getObject(clazz, fieldMethods, map);
                list.add(rsp);
            }
        } catch (Exception e) {
            throw new InternalException(e);
        }
        return list;
    }

    /**
     * Creates an object of the specified class and populates its fields from a map.
     *
     * @param <T>          The generic type of the target object.
     * @param clazz        The target class.
     * @param fieldMethods A list of {@link FieldMethod} pairs for the class.
     * @param map          A map of data to populate the object with.
     * @return An instance of the target class populated with data.
     * @throws InstantiationException    if the class cannot be instantiated.
     * @throws IllegalAccessException    if a field or method is not accessible.
     * @throws InvocationTargetException if a setter method throws an exception.
     * @throws NoSuchMethodException     if a required constructor is not found.
     */
    private static <T> T getObject(Class<T> clazz, List<FieldMethod> fieldMethods, Map<String, Object> map)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        T rsp = clazz.getConstructor().newInstance();
        // Set property values.
        for (FieldMethod filed : fieldMethods) {
            Field field = filed.getField();
            Method method = filed.getMethod();
            MappingField jsonField = field.getAnnotation(MappingField.class);
            if (!Objects.isNull(jsonField)) {
                method.invoke(rsp, map.get(jsonField.value()));
            }
        }
        return rsp;
    }

    /**
     * Retrieves a list of {@link FieldMethod} pairs for a given class.
     *
     * @param <T>   The generic type of the class.
     * @param clazz The class to introspect.
     * @return A list of {@link FieldMethod} pairs.
     * @throws IntrospectionException if an error occurs during introspection.
     * @throws NoSuchFieldException   if a field described by a property descriptor is not found.
     */
    private static <T> List<FieldMethod> getFieldMethods(Class<T> clazz)
            throws IntrospectionException, NoSuchFieldException {
        // List to store the results.
        List<FieldMethod> fieldMethods = new ArrayList<>();
        // Get bean info for the class.
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        // Iterate through property descriptors.
        for (PropertyDescriptor pd : pds) {
            Method writeMethod = pd.getWriteMethod();
            if (null == writeMethod) {
                continue;
            }
            // Get the corresponding field.
            Field field = clazz.getDeclaredField(pd.getName());
            // Create and populate a FieldMethod object.
            FieldMethod fieldMethod = new FieldMethod();
            fieldMethod.setField(field);
            fieldMethod.setMethod(writeMethod);
            // Add to the list.
            fieldMethods.add(fieldMethod);
        }
        return fieldMethods;
    }

}
