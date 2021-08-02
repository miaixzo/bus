/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.mapper.entity;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 封装字段和方法,统一调用某些方法
 *
 * @author Kimi Liu
 * @version 6.2.6
 * @since JDK 1.8+
 */
public class EntityField {

    private String name;
    private Field field;
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
        if (null != field) {
            this.field = field;
            this.name = field.getName();
            this.javaType = field.getType();
        }
        if (null != propertyDescriptor) {
            this.name = propertyDescriptor.getName();
            this.setter = propertyDescriptor.getWriteMethod();
            this.getter = propertyDescriptor.getReadMethod();
            this.javaType = propertyDescriptor.getPropertyType();
        }
    }

    /**
     * 先创建field,然后可以通过该方法获取property等属性
     *
     * @param other field
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
        if (null != field) {
            result = field.isAnnotationPresent(annotationClass);
        }
        if (!result && null != setter) {
            result = setter.isAnnotationPresent(annotationClass);
        }
        if (!result && null != getter) {
            result = getter.isAnnotationPresent(annotationClass);
        }
        return result;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T result = null;
        if (null != field) {
            result = field.getAnnotation(annotationClass);
        }
        if (null == result && null != setter) {
            result = setter.getAnnotation(annotationClass);
        }
        if (null == result && null != getter) {
            result = getter.getAnnotation(annotationClass);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        EntityField that = (EntityField) o;

        return !(null != name ? !name.equals(that.name) : null != that.name);

    }

    @Override
    public int hashCode() {
        return null != name ? name.hashCode() : 0;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public String getName() {
        return name;
    }
}
