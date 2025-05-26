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
import org.miaixz.bus.core.lang.annotation.Ignore;
import org.miaixz.bus.core.lang.exception.BeanException;
import org.miaixz.bus.core.lang.reflect.Invoker;
import org.miaixz.bus.core.lang.reflect.field.FieldInvoker;
import org.miaixz.bus.core.lang.reflect.method.MethodInvoker;
import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.ModifierKit;

/**
 * 属性描述，包括了字段、getter、setter和相应的方法执行
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PropDesc {

    /**
     * Getter方法
     */
    protected Invoker getter;
    /**
     * Setter方法
     */
    protected Invoker setter;
    /**
     * 字段名
     */
    private String fieldName;
    /**
     * 字段
     */
    private Invoker field;
    /**
     * 是否在getter上具有transient关键字或注解
     */
    private Boolean hasTransientForGetter;
    /**
     * 是否在setter上具有transient关键字或注解
     */
    private Boolean hasTransientForSetter;
    /**
     * 属性是否可读
     */
    private Boolean isReadable;
    /**
     * 属性是否可写
     */
    private Boolean isWritable;

    /**
     * 构造 Getter和Setter方法设置为默认可访问
     *
     * @param field  字段
     * @param getter get方法
     * @param setter set方法
     */
    public PropDesc(final Field field, final Method getter, final Method setter) {
        this(FieldKit.getFieldName(field), getter, setter);
        this.field = FieldInvoker.of(field);
    }

    /**
     * 构造 Getter和Setter方法设置为默认可访问
     *
     * @param fieldName 字段名
     * @param getter    get方法
     * @param setter    set方法
     */
    public PropDesc(final String fieldName, final Method getter, final Method setter) {
        this(fieldName, MethodInvoker.of(getter), MethodInvoker.of(setter));
    }

    /**
     * 构造 Getter和Setter方法设置为默认可访问
     *
     * @param fieldName 字段名
     * @param getter    get方法执行器
     * @param setter    set方法执行器
     */
    public PropDesc(final String fieldName, final Invoker getter, final Invoker setter) {
        this.fieldName = fieldName;
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * 检查字段是否被忽略读，通过{@link Ignore} 注解完成，规则为：
     *
     * <pre>
     *     1. 在字段上有{@link Ignore} 注解
     *     2. 在getXXX方法上有{@link Ignore} 注解
     * </pre>
     *
     * @param field  字段，可为{@code null}
     * @param getter 读取方法，可为{@code null}
     * @return 是否忽略读
     */
    private static boolean isIgnoreGet(final Field field, final Method getter) {
        return AnnoKit.hasAnnotation(field, Ignore.class) || AnnoKit.hasAnnotation(getter, Ignore.class);
    }

    /**
     * 检查字段是否被忽略写，通过{@link Ignore} 注解完成，规则为：
     *
     * <pre>
     *     1. 在字段上有{@link Ignore} 注解
     *     2. 在setXXX方法上有{@link Ignore} 注解
     * </pre>
     *
     * @param field  字段，可为{@code null}
     * @param setter 写方法，可为{@code null}
     * @return 是否忽略写
     */
    private static boolean isIgnoreSet(final Field field, final Method setter) {
        return AnnoKit.hasAnnotation(field, Ignore.class) || AnnoKit.hasAnnotation(setter, Ignore.class);
    }

    /**
     * 字段和Getter方法是否为Transient关键字修饰的
     *
     * @param field  字段，可为{@code null}
     * @param getter 读取方法，可为{@code null}
     * @return 是否为Transient关键字修饰的
     */
    private static boolean isTransientForGet(final Field field, final Method getter) {
        boolean isTransient = ModifierKit.hasAny(field, EnumValue.Modifier.TRANSIENT);

        // 检查Getter方法
        if (!isTransient && null != getter) {
            isTransient = ModifierKit.hasAny(getter, EnumValue.Modifier.TRANSIENT);

            // 检查注解
            if (!isTransient) {
                isTransient = AnnoKit.hasAnnotation(getter, Keys.JAVA_BEANS_TRANSIENT);
            }
        }

        return isTransient;
    }

    /**
     * 字段和Getter方法是否为Transient关键字修饰的
     *
     * @param field  字段，可为{@code null}
     * @param setter 写方法，可为{@code null}
     * @return 是否为Transient关键字修饰的
     */
    private static boolean isTransientForSet(final Field field, final Method setter) {
        boolean isTransient = ModifierKit.hasAny(field, EnumValue.Modifier.TRANSIENT);

        // 检查Getter方法
        if (!isTransient && null != setter) {
            isTransient = ModifierKit.hasAny(setter, EnumValue.Modifier.TRANSIENT);

            // 检查注解
            if (!isTransient) {
                isTransient = AnnoKit.hasAnnotation(setter, Keys.JAVA_BEANS_TRANSIENT);
            }
        }

        return isTransient;
    }

    /**
     * 获取字段名，如果存在Alias注解，读取注解的值作为名称
     *
     * @return 字段名
     */
    public String getFieldName() {
        return this.fieldName;
    }

    /**
     * 获取字段名称
     *
     * @return 字段名
     */
    public String getRawFieldName() {
        if (null == this.field) {
            return this.fieldName;
        }

        return this.field.getName();
    }

    /**
     * 获取字段
     *
     * @return 字段
     */
    public Field getField() {
        if (null != this.field && this.field instanceof FieldInvoker) {
            return ((FieldInvoker) this.field).getField();
        }
        return null;
    }

    /**
     * 获得字段类型 先获取字段的类型，如果字段不存在，则获取Getter方法的返回类型，否则获取Setter的第一个参数类型
     *
     * @return 字段类型
     */
    public Type getFieldType() {
        if (null != this.field) {
            return this.field.getType();
        }
        return findPropType(getter, setter);
    }

    /**
     * 获得字段类型 先获取字段的类型，如果字段不存在，则获取Getter方法的返回类型，否则获取Setter的第一个参数类型
     *
     * @return 字段类型
     */
    public Class<?> getFieldClass() {
        if (null != this.field) {
            return this.field.getTypeClass();
        }
        return findPropClass(getter, setter);
    }

    /**
     * 获取Getter方法，可能为{@code null}
     *
     * @return Getter方法
     */
    public Invoker getGetter() {
        return this.getter;
    }

    /**
     * 获取Setter方法，可能为{@code null}
     *
     * @return {@link Method}Setter 方法Invoker
     */
    public Invoker getSetter() {
        return this.setter;
    }

    /**
     * 检查属性是否可读（即是否可以通过{@link #getValue(Object,boolean)}获取到值）
     *
     * @param checkTransient 是否检查Transient关键字或注解
     * @return 是否可读
     */
    public boolean isReadable(final boolean checkTransient) {
        cacheReadable();

        if (checkTransient && this.hasTransientForGetter) {
            return false;
        }
        return this.isReadable;
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
    public PropDesc setValue(final Object bean, final Object value, final boolean ignoreNull,
            final boolean ignoreError) {
        return setValue(bean, value, ignoreNull, ignoreError, true);
    }

    /**
     * 获取属性值 首先调用字段对应的Getter方法获取值，如果Getter方法不存在，则判断字段如果为public，则直接获取字段值 此方法不检查任何注解，使用前需调用 {@link #isReadable(boolean)}
     * 检查是否可读
     *
     * @param bean Bean对象
     * @return 字段值
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

    @Override
    public String toString() {
        return "PropDesc{" + "field=" + field + ", fieldName=" + fieldName + ", getter=" + getter + ", setter=" + setter
                + '}';
    }

    /**
     * 获取属性值，自动转换属性值类型 首先调用字段对应的Getter方法获取值，如果Getter方法不存在，则判断字段如果为public，则直接获取字段值
     *
     * @param bean        Bean对象
     * @param targetType  返回属性值需要转换的类型，null表示不转换
     * @param ignoreError 是否忽略错误，包括转换错误和注入错误
     * @return this
     */
    public Object getValue(final Object bean, final Type targetType, final boolean ignoreError) {
        final Object result = getValue(bean, ignoreError);

        if (null != result && null != targetType) {
            // 尝试将结果转换为目标类型，如果转换失败，返回null，即跳过此属性值。
            // 当忽略错误情况下，目标类型转换失败应返回null
            // 如果返回原值，在集合注入时会成功，但是集合取值时会报类型转换错误
            return Convert.convertWithCheck(targetType, result, null, ignoreError);
        }
        return result;
    }

    /**
     * 检查属性是否可读（即是否可以通过{@link #getValue(Object, boolean)}获取到值）
     *
     * @param checkTransient 是否检查Transient关键字或注解
     * @return 是否可读
     */
    public boolean isWritable(final boolean checkTransient) {
        cacheWritable();

        if (checkTransient && this.hasTransientForSetter) {
            return false;
        }
        return this.isWritable;
    }

    /**
     * 设置Bean的字段值 首先调用字段对应的Setter方法，如果Setter方法不存在，则判断字段如果为public，则直接赋值字段值 此方法不检查任何注解，使用前需调用 {@link #isWritable(boolean)}
     * 检查是否可写
     *
     * @param bean  Bean对象
     * @param value 值，必须与字段值类型匹配
     * @return this
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
     * 设置属性值，可以自动转换字段类型为目标类型
     *
     * @param bean        Bean对象
     * @param value       属性值，可以为任意类型
     * @param ignoreNull  是否忽略{@code null}值，true表示忽略
     * @param ignoreError 是否忽略错误，包括转换错误和注入错误
     * @param override    是否覆盖目标值，如果不覆盖，会先读取bean的值，{@code null}则写，否则忽略。如果覆盖，则不判断直接写
     * @return this
     */
    public PropDesc setValue(final Object bean, Object value, final boolean ignoreNull, final boolean ignoreError,
            final boolean override) {
        if (null == value && ignoreNull) {
            return this;
        }

        // 非覆盖模式下，如果目标值存在，则跳过
        if (!override && null != getValue(bean, ignoreError)) {
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

    /**
     * 缓存读取属性的可读性，如果已经检查过，直接返回true
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

        // 检查transient关键字和@Transient注解
        this.hasTransientForGetter = isTransientForGet(field, getterMethod);

        // 检查@PropIgnore注解
        if (isIgnoreGet(field, getterMethod)) {
            this.isReadable = false;
            return;
        }

        // 检查是否有getter方法或是否为public修饰
        this.isReadable = null != getterMethod || ModifierKit.isPublic(field);
    }

    /**
     * 缓存写入属性的可写性，如果已经检查过，直接返回true
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

        // 检查transient关键字和@Transient注解
        this.hasTransientForSetter = isTransientForSet(field, setterMethod);

        // 检查@PropIgnore注解
        if (isIgnoreSet(field, setterMethod)) {
            this.isWritable = false;
            return;
        }

        // 检查是否有setter方法或是否为public修饰
        this.isWritable = null != setterMethod || ModifierKit.isPublic(field);
    }

    /**
     * 通过Getter和Setter方法中找到属性类型
     *
     * @param getter Getter方法
     * @param setter Setter方法
     * @return {@link Type}
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
     * 通过Getter和Setter方法中找到属性类型
     *
     * @param getter Getter方法
     * @param setter Setter方法
     * @return {@link Type}
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
