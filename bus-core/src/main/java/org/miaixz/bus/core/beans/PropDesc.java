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
package org.miaixz.bus.core.beans;

import org.miaixz.bus.core.annotation.Ignore;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.EnumMap;
import org.miaixz.bus.core.lang.exception.BeanException;
import org.miaixz.bus.core.xyz.*;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 属性描述，包括了字段、getter、setter和相应的方法执行
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PropDesc {

    /**
     * 字段
     */
    final Field field;
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
    public PropDesc(final Field field, final Method getter, final Method setter) {
        this.field = field;
        this.getter = ReflectKit.setAccessible(getter);
        this.setter = ReflectKit.setAccessible(setter);
    }

    /**
     * 获取字段名，如果存在Alias注解，读取注解的值作为名称
     *
     * @return 字段名
     */
    public String getFieldName() {
        return FieldKit.getFieldName(this.field);
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
     * 先获取字段的类型，如果字段不存在，则获取Getter方法的返回类型，否则获取Setter的第一个参数类型
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
     * 先获取字段的类型，如果字段不存在，则获取Getter方法的返回类型，否则获取Setter的第一个参数类型
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
     * 获取Getter方法，可能为{@code null}
     *
     * @return Getter方法
     */
    public Method getGetter() {
        return this.getter;
    }

    /**
     * 获取Setter方法，可能为{@code null}
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
    public boolean isReadable(final boolean checkTransient) {
        // 检查是否有getter方法或是否为public修饰
        if (null == this.getter && !ModifierKit.isPublic(this.field)) {
            return false;
        }

        // 检查transient关键字和@Transient注解
        if (checkTransient && isTransientForGet()) {
            return false;
        }

        // 检查@Ignore注解
        return !isIgnoreGet();
    }

    /**
     * 获取属性值
     * 首先调用字段对应的Getter方法获取值，如果Getter方法不存在，则判断字段如果为public，则直接获取字段值
     * 此方法不检查任何注解，使用前需调用 {@link #isReadable(boolean)} 检查是否可读
     *
     * @param bean Bean对象
     * @return 字段值
     */
    public Object getValue(final Object bean) {
        if (null != this.getter) {
            try {
                return LambdaKit.buildGetter(this.getter).apply(bean);
            } catch (final Exception ignore) {
                // 在jdk14+多模块项目中，存在权限问题，使用传统反射
                return MethodKit.invoke(bean, this.getter);
            }
        } else if (ModifierKit.isPublic(this.field)) {
            return FieldKit.getFieldValue(bean, this.field);
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
    public Object getValue(final Object bean, final Type targetType, final boolean ignoreError) {
        Object result = null;
        try {
            result = getValue(bean);
        } catch (final Exception e) {
            if (!ignoreError) {
                throw new BeanException(e, "Get value of [{}] error!", getFieldName());
            }
        }

        if (null != result && null != targetType) {
            // 尝试将结果转换为目标类型，如果转换失败，返回null，即跳过此属性值。
            // 当忽略错误情况下，目标类型转换失败应返回null
            // 如果返回原值，在集合注入时会成功，但是集合取值时会报类型转换错误
            return Convert.convertWithCheck(targetType, result, null, ignoreError);
        }
        return result;
    }

    /**
     * 检查属性是否可读（即是否可以通过{@link #getValue(Object)}获取到值）
     *
     * @param checkTransient 是否检查Transient关键字或注解
     * @return 是否可读
     */
    public boolean isWritable(final boolean checkTransient) {
        // 检查是否有getter方法或是否为public修饰
        if (null == this.setter && !ModifierKit.isPublic(this.field)) {
            return false;
        }

        // 检查transient关键字和@Transient注解
        if (checkTransient && isTransientForSet()) {
            return false;
        }

        // 检查@Ignore注解
        return !isIgnoreSet();
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
    public PropDesc setValue(final Object bean, final Object value) {
        if (null != this.setter) {
            MethodKit.invoke(bean, this.setter, value);
        } else if (ModifierKit.isPublic(this.field)) {
            FieldKit.setFieldValue(bean, this.field, value);
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
    public PropDesc setValue(final Object bean, final Object value, final boolean ignoreNull, final boolean ignoreError) {
        return setValue(bean, value, ignoreNull, ignoreError, true);
    }

    /**
     * 设置属性值，可以自动转换字段类型为目标类型
     *
     * @param bean        Bean对象
     * @param value       属性值，可以为任意类型
     * @param ignoreNull  是否忽略{@code null}值，true表示忽略
     * @param ignoreError 是否忽略错误，包括转换错误和注入错误
     * @param override    是否覆盖目标值，如果不覆盖，会先读取bean的值，{@code null}则写，否则忽略。如果覆盖，则不判断直接写
     * @return this
     */
    public PropDesc setValue(final Object bean, Object value, final boolean ignoreNull, final boolean ignoreError, final boolean override) {
        if (null == value && ignoreNull) {
            return this;
        }

        // 非覆盖模式下，如果目标值存在，则跳过
        if (!override && null != getValue(bean)) {
            return this;
        }

        // 当类型不匹配的时候，执行默认转换
        if (null != value) {
            final Class<?> propClass = getFieldClass();
            if (!propClass.isInstance(value)) {
                value = Convert.convertWithCheck(propClass, value, null, ignoreError);
            }
        }

        // 属性赋值
        if (null != value || !ignoreNull) {
            try {
                this.setValue(bean, value);
            } catch (final Exception e) {
                if (!ignoreError) {
                    throw new BeanException(e, "Set value of [{}] error!", getFieldName());
                }
                // 忽略注入失败
            }
        }

        return this;
    }

    @Override
    public String toString() {
        return "PropDesc{" +
                "field=" + field +
                ", getter=" + getter +
                ", setter=" + setter +
                '}';
    }

    /**
     * 通过Getter和Setter方法中找到属性类型
     *
     * @param getter Getter方法
     * @param setter Setter方法
     * @return {@link Type}
     */
    private Type findPropType(final Method getter, final Method setter) {
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
    private Class<?> findPropClass(final Method getter, final Method setter) {
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
        boolean isTransient = ModifierKit.hasModifier(this.field, EnumMap.Modifier.TRANSIENT);

        // 检查Getter方法
        if (!isTransient && null != this.getter) {
            isTransient = ModifierKit.hasModifier(this.getter, EnumMap.Modifier.TRANSIENT);

            // 检查注解
            if (!isTransient) {
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
        boolean isTransient = ModifierKit.hasModifier(this.field, EnumMap.Modifier.TRANSIENT);

        // 检查Getter方法
        if (!isTransient && null != this.setter) {
            isTransient = ModifierKit.hasModifier(this.setter, EnumMap.Modifier.TRANSIENT);

            // 检查注解
            if (!isTransient) {
                isTransient = AnnoKit.hasAnnotation(this.setter, Transient.class);
            }
        }

        return isTransient;
    }

}
