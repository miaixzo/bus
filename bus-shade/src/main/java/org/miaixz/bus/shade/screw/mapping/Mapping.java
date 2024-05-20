/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.shade.screw.mapping;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;

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

/**
 * 映射器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Mapping {

    private Mapping() {
    }

    /**
     * 将ResultSet 结果转为对象
     *
     * @param <T>       领域泛型
     * @param resultSet {@link ResultSet} 对象
     * @param clazz     领域类型
     * @return 领域对象
     * @throws InternalException 异常
     */
    public static <T> T convert(ResultSet resultSet, Class<T> clazz) throws InternalException {
        // 存放列名和结果
        Map<String, Object> values = new HashMap<>(Normal._16);
        try {
            // 处理 ResultSet
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            // 迭代
            while (resultSet.next()) {
                // 放入内存
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    values.put(columnName, resultSet.getString(columnName));
                }
            }
            // 有结果
            if (values.size() != 0) {
                // 获取类数据
                List<FieldMethod> fieldMethods = getFieldMethods(clazz);
                // 设置属性值
                return getObject(clazz, fieldMethods, values);
            }
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * @param resultSet {@link ResultSet} 对象
     * @param clazz     领域类型
     * @param <T>       领域泛型
     * @return 领域对象
     * @throws InternalException 异常
     */
    public static <T> List<T> convertList(ResultSet resultSet,
                                          Class<T> clazz) throws InternalException {
        // 存放列名和结果
        List<Map<String, Object>> values = new ArrayList<>(Normal._16);
        // 结果集合
        List<T> list = new ArrayList<>();
        try {
            // 处理 ResultSet
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            // 迭代
            while (resultSet.next()) {
                //map object
                HashMap<String, Object> value = new HashMap<>(Normal._16);
                // 循环所有的列，获取列名，根据列名获取值
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    value.put(columnName, resultSet.getString(i));
                }
                values.add(value);
            }
            // 获取类数据
            List<FieldMethod> fieldMethods = getFieldMethods(clazz);
            // 循环集合，根据类型反射构建对象
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
     * 获取对象
     *
     * @param clazz        class
     * @param fieldMethods List<FieldMethod>
     * @param map          数据集合
     * @param <T>          领域泛型
     * @return 领域对象
     * @throws InstantiationException    InstantiationException
     * @throws IllegalAccessException    IllegalAccessException
     * @throws InvocationTargetException InvocationTargetException
     */
    private static <T> T getObject(Class<T> clazz, List<FieldMethod> fieldMethods,
                                   Map<String, Object> map) throws InstantiationException,
            IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        T rsp = clazz.getConstructor().newInstance();
        // 设置属性值
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
     * 根据类型获取 FieldMethod
     *
     * @param clazz {@link Class}
     * @param <T>   {@link T}
     * @return {@link List<FieldMethod>}
     * @throws IntrospectionException IntrospectionException
     * @throws NoSuchFieldException   NoSuchFieldException
     */
    private static <T> List<FieldMethod> getFieldMethods(Class<T> clazz) throws IntrospectionException,
            NoSuchFieldException {
        // 结果集合
        List<FieldMethod> fieldMethods = new ArrayList<>();
        //BeanInfo
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        // 循环处理值
        for (PropertyDescriptor pd : pds) {
            Method writeMethod = pd.getWriteMethod();
            if (null == writeMethod) {
                continue;
            }
            // 获取字段
            Field field = clazz.getDeclaredField(pd.getName());
            // 获取只写方法
            FieldMethod fieldMethod = new FieldMethod();
            fieldMethod.setField(field);
            fieldMethod.setMethod(writeMethod);
            // 放入集合
            fieldMethods.add(fieldMethod);
        }
        return fieldMethods;
    }

    /**
     * 尝试获取属性
     * <p>
     * 不会抛出异常，不存在则返回null
     *
     * @param clazz    {@link Class}
     * @param itemName {@link String}
     * @return {@link Field}
     */
    private static Field tryGetFieldWithoutExp(Class<?> clazz, String itemName) {
        try {
            return clazz.getDeclaredField(itemName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取属性设置属性
     *
     * @param clazz {@link Class}
     * @param field {@link Field}
     * @return {@link Method}
     */
    private static <T> Method tryGetSetMethod(Class<T> clazz, Field field, String methodName) {
        try {
            return clazz.getDeclaredMethod(methodName, field.getType());
        } catch (Exception e) {
            return null;
        }
    }

}
