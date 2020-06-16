/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.beans;

import org.aoju.bus.core.annotation.Alias;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.map.CaseInsensitiveMap;
import org.aoju.bus.core.toolkit.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bean信息描述做为BeanInfo替代方案,此对象持有JavaBean中的setters和getters等相关信息描述
 * 查找Getter和Setter方法时会：
 *
 * <pre>
 * 1. 忽略字段和方法名的大小写
 * 2. Getter查找getXXX、isXXX、getIsXXX
 * 3. Setter查找setXXX、setIsXXX
 * 4. Setter忽略参数值与字段值不匹配的情况,因此有多个参数类型的重载时,会调用首次匹配的
 * </pre>
 *
 * @author Kimi Liu
 * @version 6.0.0
 * @since JDK 1.8+
 */
public class BeanDesc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Bean类
     */
    private Class<?> beanClass;
    /**
     * 属性Map
     */
    private Map<String, PropDesc> propMap = new LinkedHashMap<>();

    /**
     * 构造
     *
     * @param beanClass Bean类
     */
    public BeanDesc(Class<?> beanClass) {
        Assert.notNull(beanClass);
        this.beanClass = beanClass;
        init();
    }

    /**
     * 获取Bean的全类名
     *
     * @return Bean的类名
     */
    public String getName() {
        return this.beanClass.getName();
    }

    /**
     * 获取Bean的简单类名
     *
     * @return Bean的类名
     */
    public String getSimpleName() {
        return this.beanClass.getSimpleName();
    }

    /**
     * 获取字段名-字段属性Map
     *
     * @param ignoreCase 是否忽略大小写,true为忽略,false不忽略
     * @return 字段名-字段属性Map
     */
    public Map<String, PropDesc> getPropMap(boolean ignoreCase) {
        return ignoreCase ? new CaseInsensitiveMap<>(1, this.propMap) : this.propMap;
    }

    /**
     * 获取字段属性列表
     *
     * @return {@link PropDesc} 列表
     */
    public Collection<PropDesc> getProps() {
        return this.propMap.values();
    }

    /**
     * 获取属性,如果不存在返回null
     *
     * @param fieldName 字段名
     * @return {@link PropDesc}
     */
    public PropDesc getProp(String fieldName) {
        return this.propMap.get(fieldName);
    }

    /**
     * 获得字段名对应的字段对象,如果不存在返回null
     *
     * @param fieldName 字段名
     * @return 字段值
     */
    public Field getField(String fieldName) {
        final PropDesc desc = this.propMap.get(fieldName);
        return null == desc ? null : desc.getField();
    }

    /**
     * 获取Getter方法,如果不存在返回null
     *
     * @param fieldName 字段名
     * @return Getter方法
     */
    public Method getGetter(String fieldName) {
        final PropDesc desc = this.propMap.get(fieldName);
        return null == desc ? null : desc.getGetter();
    }

    /**
     * 获取Setter方法,如果不存在返回null
     *
     * @param fieldName 字段名
     * @return Setter方法
     */
    public Method getSetter(String fieldName) {
        final PropDesc desc = this.propMap.get(fieldName);
        return null == desc ? null : desc.getSetter();
    }

    /**
     * 初始化
     * 只有与属性关联的相关Getter和Setter方法才会被读取,无关的getXXX和setXXX都被忽略
     *
     * @return this
     */
    private BeanDesc init() {
        for (Field field : ReflectKit.getFields(this.beanClass)) {
            if (false == BeanKit.isStatic(field)) {
                //只针对非static属性
                this.propMap.put(ReflectKit.getFieldName(field), createProp(field));
            }
        }
        return this;
    }

    /**
     * 根据字段创建属性描述
     * 查找Getter和Setter方法时会：
     *
     * <pre>
     * 1. 忽略字段和方法名的大小写
     * 2. Getter查找getXXX、isXXX、getIsXXX
     * 3. Setter查找setXXX、setIsXXX
     * 4. Setter忽略参数值与字段值不匹配的情况,因此有多个参数类型的重载时,会调用首次匹配的
     * </pre>
     *
     * @param field 字段
     * @return {@link PropDesc}
     */
    private PropDesc createProp(Field field) {
        final String fieldName = field.getName();
        final Class<?> fieldType = field.getType();
        final boolean isBooeanField = BooleanKit.isBoolean(fieldType);

        Method getter = null;
        Method setter = null;

        String methodName;
        Class<?>[] parameterTypes;
        for (Method method : ReflectKit.getMethods(this.beanClass)) {
            parameterTypes = method.getParameterTypes();
            if (parameterTypes.length > 1) {
                // 多于1个参数说明非Getter或Setter
                continue;
            }

            methodName = method.getName();
            if (parameterTypes.length == 0) {
                // 无参数,可能为Getter方法
                if (isMatchGetter(methodName, fieldName, isBooeanField)) {
                    // 方法名与字段名匹配,则为Getter方法
                    getter = method;
                }
            } else if (isMatchSetter(methodName, fieldName, isBooeanField)) {
                // 只有一个参数的情况下方法名与字段名对应匹配,则为Setter方法
                setter = method;
            }
            if (null != getter && null != setter) {
                // 如果Getter和Setter方法都找到了,不再继续寻找
                break;
            }
        }
        return new PropDesc(field, getter, setter);
    }

    /**
     * 方法是否为Getter方法
     * 匹配规则如下(忽略大小写)：
     *
     * <pre>
     * 字段名    -》 方法名
     * isName  -》 isName
     * isName  -》 isIsName
     * isName  -》 getIsName
     * name     -》 isName
     * name     -》 getName
     * </pre>
     *
     * @param methodName    方法名
     * @param fieldName     字段名
     * @param isBooeanField 是否为Boolean类型字段
     * @return 是否匹配
     */
    private boolean isMatchGetter(String methodName, String fieldName, boolean isBooeanField) {
        // 全部转为小写,忽略大小写比较
        methodName = methodName.toLowerCase();
        fieldName = fieldName.toLowerCase();

        if (false == methodName.startsWith(Normal.GET) && false == methodName.startsWith(Normal.IS)) {
            // 非标准Getter方法
            return false;
        }
        if ("getclass".equals(methodName)) {
            //跳过getClass方法
            return false;
        }

        // 针对Boolean类型特殊检查
        if (isBooeanField) {
            if (fieldName.startsWith(Normal.IS)) {
                // 字段已经是is开头
                if (methodName.equals(fieldName) // isName -》 isName
                        || methodName.equals(Normal.GET + fieldName)// isName -》 getIsName
                        || methodName.equals(Normal.IS + fieldName)// isName -》 isIsName
                ) {
                    return true;
                }
            } else if (methodName.equals(Normal.IS + fieldName)) {
                // 字段非is开头, name -》 isName
                return true;
            }
        }

        // 包括boolean的任何类型只有一种匹配情况：name -》 getName
        return methodName.equals(Normal.GET + fieldName);
    }

    /**
     * 方法是否为Setter方法
     * 匹配规则如下(忽略大小写)：
     *
     * <pre>
     * 字段名    -》 方法名
     * isName  -》 setName
     * isName  -》 setIsName
     * name     -》 setName
     * </pre>
     *
     * @param methodName    方法名
     * @param fieldName     字段名
     * @param isBooeanField 是否为Boolean类型字段
     * @return 是否匹配
     */
    private boolean isMatchSetter(String methodName, String fieldName, boolean isBooeanField) {
        // 全部转为小写,忽略大小写比较
        methodName = methodName.toLowerCase();
        fieldName = fieldName.toLowerCase();

        // 非标准Setter方法跳过
        if (false == methodName.startsWith(Normal.SET)) {
            return false;
        }

        // 针对Boolean类型特殊检查
        if (isBooeanField && fieldName.startsWith(Normal.IS)) {
            // 字段是is开头
            if (methodName.equals(Normal.SET + StringKit.removePrefix(fieldName, Normal.IS))// isName -》 setName
                    || methodName.equals(Normal.SET + fieldName)// isName -》 setIsName
            ) {
                return true;
            }
        }

        // 包括boolean的任何类型只有一种匹配情况：name -》 setName
        return methodName.equals(Normal.SET + fieldName);
    }

    /**
     * 属性描述
     */
    public static class PropDesc {

        /**
         * 字段
         */
        private Field field;
        /**
         * Getter方法
         */
        private Method getter;
        /**
         * Setter方法
         */
        private Method setter;

        /**
         * 构造
         * Getter和Setter方法设置为默认可访问
         *
         * @param field  字段
         * @param getter get方法
         * @param setter set方法
         */
        public PropDesc(Field field, Method getter, Method setter) {
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
         * 获取字段值
         * 首先调用字段对应的Getter方法获取值,如果Getter方法不存在,则判断字段如果为public,则直接获取字段值
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
         * 设置Bean的字段值
         * 首先调用字段对应的Setter方法,如果Setter方法不存在,则判断字段如果为public,则直接赋值字段值
         *
         * @param bean  Bean对象
         * @param value 值
         * @return this
         */
        public PropDesc setValue(Object bean, Object value) {
            if (null != this.setter) {
                ReflectKit.invoke(bean, this.setter, value);
            } else if (BeanKit.isPublic(this.field)) {
                ReflectKit.setFieldValue(bean, this.field, value);
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
    }

}
