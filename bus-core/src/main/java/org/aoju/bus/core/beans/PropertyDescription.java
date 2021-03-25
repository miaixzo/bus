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
package org.aoju.bus.core.beans;

import org.aoju.bus.core.annotation.Alias;
import org.aoju.bus.core.annotation.Ignore;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.*;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 属性描述
 *
 * @author Kimi Liu
 * @version 6.2.2
 * @since JDK 1.8+
 */
public class PropertyDescription {

    /**
     * 字段
     */
    protected final Field field;
    /**
     * Getter方法
     */
    protected Method getter;
    /**
     * Setter方法
     */
    protected Method setter;

    /**
     * 构造
     * Getter和Setter方法设置为默认可访问
     *
     * @param field  字段
     * @param getter get方法
     * @param setter set方法
     */
    public PropertyDescription(Field field, Method getter, Method setter) {
        this.field = field;
        this.getter = ClassKit.setAccessible(getter);
        this.setter = ClassKit.setAccessible(setter);
    }

    /**
     * 获取字段名，如果存在{@link Alias}注解，读取注解的值作为名称
     *
     * @return 字段名
     */
    public String getFieldName() {
        return ReflectKit.getFieldName(this.field);
    }

    /**
     * 获取字段名称
     *
     * @return 字段名
     */
    public String getRawFieldName() {
        return null == this.field ? null : this.field.getName();
    }

    /**
     * 获取字段
     *
     * @return 字段
     */
    public Field getField() {
        return this.field;
    }

    /**
     * 获得字段类型
     * 先获取字段的类型,如果字段不存在,则获取Getter方法的返回类型,否则获取Setter的第一个参数类型
     *
     * @return 字段类型
     */
    public Type getFieldType() {
        if (null != this.field) {
            return TypeKit.getType(this.field);
        }
        return findPropType(getter, setter);
    }

    /**
     * 获得字段类型
     * 先获取字段的类型,如果字段不存在,则获取Getter方法的返回类型,否则获取Setter的第一个参数类型
     *
     * @return 字段类型
     */
    public Class<?> getFieldClass() {
        if (null != this.field) {
            return TypeKit.getClass(this.field);
        }
        return findPropClass(getter, setter);
    }

    /**
     * 获取Getter方法
     *
     * @return Getter方法
     */
    public Method getGetter() {
        return this.getter;
    }

    /**
     * 获取Setter方法
     *
     * @return {@link Method}Setter 方法对象
     */
    public Method getSetter() {
        return this.setter;
    }

    /**
     * 检查属性是否可读（即是否可以通过{@link #getValue(Object)}获取到值）
     *
     * @param checkTransient 是否检查Transient关键字或注解
     * @return 是否可读
     */
    public boolean isReadable(boolean checkTransient) {
        // 检查是否有getter方法或是否为public修饰
        if (null == this.getter && false == BeanKit.isPublic(this.field)) {
            return false;
        }

        // 检查transient关键字和@Transient注解
        if (checkTransient && isTransientForGet()) {
            return false;
        }

        // 检查@PropIgnore注解
        return false == isIgnoreGet();
    }

    /**
     * 获取属性值
     * 首先调用字段对应的Getter方法获取值，如果Getter方法不存在，则判断字段如果为public，则直接获取字段值
     * 此方法不检查任何注解，使用前需调用 {@link #isReadable(boolean)} 检查是否可读
     *
     * @param bean Bean对象
     * @return 字段值
     */
    public Object getValue(Object bean) {
        if (null != this.getter) {
            return ReflectKit.invoke(bean, this.getter);
        } else if (BeanKit.isPublic(this.field)) {
            return ReflectKit.getFieldValue(bean, this.field);
        }
        return null;
    }

    /**
     * 获取属性值，自动转换属性值类型
     * 首先调用字段对应的Getter方法获取值，如果Getter方法不存在，则判断字段如果为public，则直接获取字段值
     *
     * @param bean        Bean对象
     * @param targetType  返回属性值需要转换的类型，null表示不转换
     * @param ignoreError 是否忽略错误，包括转换错误和注入错误
     * @return this
     */
    public Object getValue(Object bean, Type targetType, boolean ignoreError) {
        Object result = null;
        try {
            result = getValue(bean);
        } catch (Exception e) {
            if (false == ignoreError) {
                throw new InstrumentException("Get value of [{}] error!", getFieldName());
            }
        }

        if (null != result && null != targetType) {
            // 尝试将结果转换为目标类型，如果转换失败，返回原类型。
            final Object convertValue = Convert.convertWithCheck(targetType, result, null, ignoreError);
            if (null != convertValue) {
                result = convertValue;
            }
        }
        return result;
    }

    /**
     * 检查属性是否可读（即是否可以通过{@link #getValue(Object)}获取到值）
     *
     * @param checkTransient 是否检查Transient关键字或注解
     * @return 是否可读
     */
    public boolean isWritable(boolean checkTransient) {
        // 检查是否有getter方法或是否为public修饰
        if (null == this.setter && false == BeanKit.isPublic(this.field)) {
            return false;
        }

        // 检查transient关键字和@Transient注解
        if (checkTransient && isTransientForSet()) {
            return false;
        }

        // 检查@PropIgnore注解
        return false == isIgnoreSet();
    }

    /**
     * 设置Bean的字段值
     * 首先调用字段对应的Setter方法，如果Setter方法不存在，则判断字段如果为public，则直接赋值字段值
     * 此方法不检查任何注解，使用前需调用 {@link #isWritable(boolean)} 检查是否可写
     *
     * @param bean  Bean对象
     * @param value 值，必须与字段值类型匹配
     * @return this
     */
    public PropertyDescription setValue(Object bean, Object value) {
        if (null != this.setter) {
            ReflectKit.invoke(bean, this.setter, value);
        } else if (BeanKit.isPublic(this.field)) {
            ReflectKit.setFieldValue(bean, this.field, value);
        }
        return this;
    }

    /**
     * 设置属性值，可以自动转换字段类型为目标类型
     *
     * @param bean        Bean对象
     * @param value       属性值，可以为任意类型
     * @param ignoreNull  是否忽略{@code null}值，true表示忽略
     * @param ignoreError 是否忽略错误，包括转换错误和注入错误
     * @return this
     */
    public PropertyDescription setValue(Object bean, Object value, boolean ignoreNull, boolean ignoreError) {
        if (ignoreNull && null == value) {
            return this;
        }

        // 当类型不匹配的时候，执行默认转换
        if (null != value) {
            final Class<?> propClass = getFieldClass();
            if (false == propClass.isInstance(value)) {
                value = Convert.convertWithCheck(propClass, value, null, ignoreError);
            }
        }

        // 属性赋值
        if (null != value || false == ignoreNull) {
            try {
                this.setValue(bean, value);
            } catch (Exception e) {
                if (false == ignoreError) {
                    throw new InstrumentException("Set value of [{}] error!", getFieldName());
                }
                // 忽略注入失败
            }
        }

        return this;
    }

    /**
     * 通过Getter和Setter方法中找到属性类型
     *
     * @param getter Getter方法
     * @param setter Setter方法
     * @return {@link Type}
     */
    private Type findPropType(Method getter, Method setter) {
        Type type = null;
        if (null != getter) {
            type = TypeKit.getReturnType(getter);
        }
        if (null == type && null != setter) {
            type = TypeKit.getParamType(setter, 0);
        }
        return type;
    }

    /**
     * 通过Getter和Setter方法中找到属性类型
     *
     * @param getter Getter方法
     * @param setter Setter方法
     * @return {@link Type}
     */
    private Class<?> findPropClass(Method getter, Method setter) {
        Class<?> type = null;
        if (null != getter) {
            type = TypeKit.getReturnClass(getter);
        }
        if (null == type && null != setter) {
            type = TypeKit.getFirstParamClass(setter);
        }
        return type;
    }

    /**
     * 检查字段是否被忽略写，通过{@link Ignore} 注解完成，规则为：
     * <pre>
     *     1. 在字段上有{@link Ignore} 注解
     *     2. 在setXXX方法上有{@link Ignore} 注解
     * </pre>
     *
     * @return 是否忽略写
     */
    private boolean isIgnoreSet() {
        return AnnoKit.hasAnnotation(this.field, Ignore.class)
                || AnnoKit.hasAnnotation(this.setter, Ignore.class);
    }

    /**
     * 检查字段是否被忽略读，通过{@link Ignore} 注解完成，规则为：
     * <pre>
     *     1. 在字段上有{@link Ignore} 注解
     *     2. 在getXXX方法上有{@link Ignore} 注解
     * </pre>
     *
     * @return 是否忽略读
     */
    private boolean isIgnoreGet() {
        return AnnoKit.hasAnnotation(this.field, Ignore.class)
                || AnnoKit.hasAnnotation(this.getter, Ignore.class);
    }

    /**
     * 字段和Getter方法是否为Transient关键字修饰的
     *
     * @return 是否为Transient关键字修饰的
     */
    private boolean isTransientForGet() {
        boolean isTransient = BeanKit.hasModifier(this.field, BeanKit.ModifierType.TRANSIENT);

        // 检查Getter方法
        if (false == isTransient && null != this.getter) {
            isTransient = BeanKit.hasModifier(this.getter, BeanKit.ModifierType.TRANSIENT);

            // 检查注解
            if (false == isTransient) {
                isTransient = AnnoKit.hasAnnotation(this.getter, Transient.class);
            }
        }

        return isTransient;
    }

    /**
     * 字段和Getter方法是否为Transient关键字修饰的
     *
     * @return 是否为Transient关键字修饰的
     */
    private boolean isTransientForSet() {
        boolean isTransient = BeanKit.hasModifier(this.field, BeanKit.ModifierType.TRANSIENT);

        // 检查Getter方法
        if (false == isTransient && null != this.setter) {
            isTransient = BeanKit.hasModifier(this.setter, BeanKit.ModifierType.TRANSIENT);

            // 检查注解
            if (false == isTransient) {
                isTransient = AnnoKit.hasAnnotation(this.setter, Transient.class);
            }
        }

        return isTransient;
    }

}
