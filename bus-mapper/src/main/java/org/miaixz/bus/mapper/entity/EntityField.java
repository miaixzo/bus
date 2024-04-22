/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         *
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
package org.miaixz.bus.mapper.entity;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 封装字段和方法，统一调用某些方法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EntityField {

    /**
     * 字段属性名
     */
    private String name;
    private Field field;
    /**
     * 设置javaType
     */
    private Class<?> javaType;
    private Method setter;
    private Method getter;

    /**
     * 构造方法
     *
     * @param field              字段
     * @param propertyDescriptor 字段name对应的property
     */
    public EntityField(Field field, PropertyDescriptor propertyDescriptor) {
        if (field != null) {
            this.field = field;
            this.name = field.getName();
            this.javaType = field.getType();
        }
        if (propertyDescriptor != null) {
            this.name = propertyDescriptor.getName();
            this.setter = propertyDescriptor.getWriteMethod();
            this.getter = propertyDescriptor.getReadMethod();
            this.javaType = propertyDescriptor.getPropertyType();
        }
    }

    /**
     * 先创建field，然后可以通过该方法获取property等属性
     *
     * @param other 名称信息
     */
    public void copyFromPropertyDescriptor(EntityField other) {
        this.setter = other.setter;
        this.getter = other.getter;
        this.javaType = other.javaType;
        this.name = other.name;
    }

    /**
     * 是否有该注解
     *
     * @param annotationClass 注解
     * @return the boolean
     */
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        boolean result = false;
        if (field != null) {
            result = field.isAnnotationPresent(annotationClass);
        }
        if (!result && setter != null) {
            result = setter.isAnnotationPresent(annotationClass);
        }
        if (!result && getter != null) {
            result = getter.isAnnotationPresent(annotationClass);
        }
        return result;
    }

    /**
     * 获取指定的注解
     *
     * @param annotationClass 注解信息
     * @param <T>             对象引用
     * @return the annotation
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T result = null;
        if (field != null) {
            result = field.getAnnotation(annotationClass);
        }
        if (result == null && setter != null) {
            result = setter.getAnnotation(annotationClass);
        }
        if (result == null && getter != null) {
            result = getter.getAnnotation(annotationClass);
        }
        return result;
    }

    /**
     * 反射获取值
     *
     * @param object 对象
     * @return the object
     * @throws IllegalAccessException    异常
     * @throws InvocationTargetException 异常
     */
    public Object getValue(Object object) throws IllegalAccessException, InvocationTargetException {
        Object result = null;
        if (getter != null) {
            result = getter.invoke(object);
        } else if (field != null) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            result = field.get(object);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityField that = (EntityField) o;

        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /**
     * 获取javaType
     *
     * @return
     */
    public Class<?> getJavaType() {
        return javaType;
    }

    /**
     * 设置javaType
     *
     * @param javaType
     */
    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    /**
     * 字段属性名
     *
     * @return
     */
    public String getName() {
        return name;
    }

}
