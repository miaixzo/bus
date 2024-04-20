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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.map.CaseInsensitiveMap;
import org.miaixz.bus.core.toolkit.BeanKit;
import org.miaixz.bus.core.toolkit.BooleanKit;
import org.miaixz.bus.core.toolkit.ReflectKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bean信息描述做为BeanInfo替代方案
 * 此对象持有Bean中的setters和getters等相关信息描述
 * 查找Getter和Setter方法时会：
 * <pre>
 * 1. 忽略字段和方法名的大小写
 * 2. Getter查找getXXX、isXXX、getIsXXX
 * 3. Setter查找setXXX、setIsXXX
 * 4. Setter忽略参数值与字段值不匹配的情况,因此有多个参数类型的重载时,会调用首次匹配的
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanDesc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Bean类
     */
    private final Class<?> beanClass;
    /**
     * 属性Map
     */
    private final Map<String, PropertyDesc> propMap = new LinkedHashMap<>();

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
    public Map<String, PropertyDesc> getPropMap(boolean ignoreCase) {
        return ignoreCase ? new CaseInsensitiveMap<>(1, this.propMap) : this.propMap;
    }

    /**
     * 获取字段属性列表
     *
     * @return {@link PropertyDesc} 列表
     */
    public Collection<PropertyDesc> getProps() {
        return this.propMap.values();
    }

    /**
     * 获取属性,如果不存在返回null
     *
     * @param fieldName 字段名
     * @return {@link PropertyDesc}
     */
    public PropertyDesc getProp(String fieldName) {
        return this.propMap.get(fieldName);
    }

    /**
     * 获得字段名对应的字段对象,如果不存在返回null
     *
     * @param fieldName 字段名
     * @return 字段值
     */
    public Field getField(String fieldName) {
        final PropertyDesc desc = this.propMap.get(fieldName);
        return null == desc ? null : desc.getField();
    }

    /**
     * 获取Getter方法,如果不存在返回null
     *
     * @param fieldName 字段名
     * @return Getter方法
     */
    public Method getGetter(String fieldName) {
        final PropertyDesc desc = this.propMap.get(fieldName);
        return null == desc ? null : desc.getGetter();
    }

    /**
     * 获取Setter方法,如果不存在返回null
     *
     * @param fieldName 字段名
     * @return Setter方法
     */
    public Method getSetter(String fieldName) {
        final PropertyDesc desc = this.propMap.get(fieldName);
        return null == desc ? null : desc.getSetter();
    }

    /**
     * 初始化
     * 只有与属性关联的相关Getter和Setter方法才会被读取,无关的getXXX和setXXX都被忽略
     *
     * @return this
     */
    private BeanDesc init() {
        final Method[] gettersAndSetters = ReflectKit.getMethods(this.beanClass, ReflectKit::isGetterOrSetterIgnoreCase);
        PropertyDesc prop;
        for (Field field : ReflectKit.getFields(this.beanClass)) {
            // 排除静态属性和对象子类
            if (false == BeanKit.isStatic(field) && false == ReflectKit.isOuterClassField(field)) {
                prop = createProp(field, gettersAndSetters);
                // 只有不存在时才放入，防止父类属性覆盖子类属性
                this.propMap.putIfAbsent(prop.getFieldName(), prop);
            }
        }
        return this;
    }

    /**
     * 根据字段创建属性描述
     * 查找Getter和Setter方法
     *
     * <pre>
     * 1. 忽略字段和方法名的大小写
     * 2. Getter查找getXXX、isXXX、getIsXXX
     * 3. Setter查找setXXX、setIsXXX
     * 4. Setter忽略参数值与字段值不匹配的情况,因此有多个参数类型的重载时,会调用首次匹配的
     * </pre>
     *
     * @param field   字段
     * @param methods 类中所有的方法
     * @return {@link PropertyDesc}
     */
    private PropertyDesc createProp(Field field, Method[] methods) {
        final PropertyDesc prop = findProp(field, methods, false);
        // 忽略大小写重新匹配一次
        if (null == prop.getter || null == prop.setter) {
            final PropertyDesc propIgnoreCase = findProp(field, methods, true);
            if (null == prop.getter) {
                prop.getter = propIgnoreCase.getter;
            }
            if (null == prop.setter) {
                prop.setter = propIgnoreCase.setter;
            }
        }

        return prop;
    }

    /**
     * 查找字段对应的Getter和Setter方法
     *
     * @param field      字段
     * @param methods    类中所有的Getter或Setter方法
     * @param ignoreCase 是否忽略大小写匹配
     * @return this
     */
    private PropertyDesc findProp(Field field, Method[] methods, boolean ignoreCase) {
        final String fieldName = field.getName();
        final Class<?> fieldType = field.getType();
        final boolean isBooleanField = BooleanKit.isBoolean(fieldType);

        Method getter = null;
        Method setter = null;
        String methodName;
        for (Method method : methods) {
            methodName = method.getName();
            if (method.getParameterCount() == 0) {
                // 无参数，可能为Getter方法
                if (isMatchGetter(methodName, fieldName, isBooleanField, ignoreCase)) {
                    // 方法名与字段名匹配，则为Getter方法
                    getter = method;
                }
            } else if (isMatchSetter(methodName, fieldName, isBooleanField, ignoreCase)) {
                // setter方法的参数类型和字段类型必须一致，或参数类型是字段类型的子类
                if (fieldType.isAssignableFrom(method.getParameterTypes()[0])) {
                    setter = method;
                }
            }
            if (null != getter && null != setter) {
                // 如果Getter和Setter方法都找到了，不再继续寻找
                break;
            }
        }

        return new PropertyDesc(field, getter, setter);
    }

    /**
     * 方法是否为Getter方法
     * 匹配规则如下（忽略大小写
     *
     * <pre>
     * 字段名     方法名
     * isName  - isName
     * isName  - isIsName
     * isName  - getIsName
     * name    - isName
     * name    - getName
     * </pre>
     *
     * @param methodName     方法名
     * @param fieldName      字段名
     * @param isBooleanField 是否为Boolean类型字段
     * @param ignoreCase     匹配是否忽略大小写
     * @return 是否匹配
     */
    private boolean isMatchGetter(String methodName, String fieldName, boolean isBooleanField, boolean ignoreCase) {
        final String handledFieldName;
        if (ignoreCase) {
            // 全部转为小写，忽略大小写比较
            methodName = methodName.toLowerCase();
            handledFieldName = fieldName.toLowerCase();
            fieldName = handledFieldName;
        } else {
            handledFieldName = StringKit.upperFirst(fieldName);
        }

        // 针对Boolean类型特殊检查
        if (isBooleanField) {
            if (fieldName.startsWith(Normal.IS)) {
                // 字段已经是is开头
                if (methodName.equals(fieldName) // isName - isName
                        || methodName.equals(Normal.GET + handledFieldName)// isName - getIsName
                        || methodName.equals(Normal.IS + handledFieldName)// isName - isIsName
                ) {
                    return true;
                }
            } else if (methodName.equals(Normal.IS + handledFieldName)) {
                // 字段非is开头, name -> isName
                return true;
            }
        }

        // 包括boolean的任何类型只有一种匹配情况：name - getName
        return methodName.equals(Normal.GET + handledFieldName);
    }

    /**
     * 方法是否为Setter方法
     * 匹配规则如下(忽略大小写)：
     *
     * <pre>
     * 字段名   - 方法名
     * isName  - setName
     * isName  - setIsName
     * name    - setName
     * </pre>
     *
     * @param methodName     方法名
     * @param fieldName      字段名
     * @param isBooleanField 是否为Boolean类型字段
     * @param ignoreCase     匹配是否忽略大小写
     * @return 是否匹配
     */
    private boolean isMatchSetter(String methodName, String fieldName, boolean isBooleanField, boolean ignoreCase) {
        final String handledFieldName;
        if (ignoreCase) {
            // 全部转为小写，忽略大小写比较
            methodName = methodName.toLowerCase();
            handledFieldName = fieldName.toLowerCase();
            fieldName = handledFieldName;
        } else {
            handledFieldName = StringKit.upperFirst(fieldName);
        }

        // 非标准Setter方法跳过
        if (false == methodName.startsWith(Normal.SET)) {
            return false;
        }

        // 针对Boolean类型特殊检查
        if (isBooleanField && fieldName.startsWith(Normal.IS)) {
            // 字段是is开头
            if (methodName.equals(Normal.SET + StringKit.removePrefix(fieldName, Normal.IS))// isName - setName
                    || methodName.equals(Normal.SET + handledFieldName)// isName - setIsName
            ) {
                return true;
            }
        }

        // 包括boolean的任何类型只有一种匹配情况：name - setName
        return methodName.equals(Normal.SET + fieldName);
    }

}
