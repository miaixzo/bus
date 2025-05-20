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
package org.miaixz.bus.mapper.mapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 实体类字段信息类，参考 {@link java.lang.reflect.Field}，提供字段相关操作
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapperFields {

    /**
     * 所在实体类类型
     */
    protected Class<?> entityClass;

    /**
     * 对应实体类中的 Java 字段（可扩展方法注解）
     */
    protected Field field;

    /**
     * 默认构造函数
     */
    public MapperFields() {
    }

    /**
     * 构造函数，初始化实体类和字段信息
     *
     * @param entityClass 实体类类型
     * @param field       Java 字段
     */
    public MapperFields(Class<?> entityClass, Field field) {
        this.entityClass = entityClass;
        this.field = field;
        this.field.setAccessible(true);
    }

    /**
     * 获取字段对象
     *
     * @return Java 字段
     */
    public Field getField() {
        return field;
    }

    /**
     * 获取当前字段所在的类
     *
     * @return 声明字段的类
     */
    public Class<?> getDeclaringClass() {
        return field.getDeclaringClass();
    }

    /**
     * 获取字段名
     *
     * @return 字段名称
     */
    public String getName() {
        return field.getName();
    }

    /**
     * 获取字段类型
     *
     * @return 字段的实际类型
     */
    public Class<?> getType() {
        return GenericTypeResolver.resolveFieldClass(field, entityClass);
    }

    /**
     * 获取字段上的指定注解
     *
     * @param annotationClass 注解类型
     * @param <T>             注解泛型
     * @return 指定类型的注解实例，若不存在则返回 null
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    /**
     * 获取字段上的全部注解
     *
     * @return 注解数组
     */
    public Annotation[] getAnnotations() {
        return field.getAnnotations();
    }

    /**
     * 检查字段是否配置了指定注解
     *
     * @param annotationClass 注解类型
     * @return true 表示存在指定注解，false 表示不存在
     */
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }

    /**
     * 通过反射获取字段值
     *
     * @param obj 对象
     * @return 字段值
     * @throws RuntimeException 如果反射操作失败
     */
    public Object get(Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error getting field value by reflection", e);
        }
    }

    /**
     * 通过反射设置字段值
     *
     * @param obj   对象
     * @param value 字段值
     * @throws RuntimeException 如果反射操作失败
     */
    public void set(Object obj, Object value) {
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error in reflection setting field value", e);
        }
    }

}