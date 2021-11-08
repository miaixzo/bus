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

import org.aoju.bus.core.clone.Cloning;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.BeanKit;
import org.aoju.bus.core.toolkit.ClassKit;
import org.aoju.bus.core.toolkit.ReflectKit;

import java.io.Serializable;
import java.util.Map;

/**
 * 动态Bean,通过反射对Bean的相关方法做操作
 * 支持Map和普通Bean
 *
 * @author Kimi Liu
 * @version 6.3.1
 * @since JDK 1.8+
 */
public class DynamicBean extends Cloning<DynamicBean> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<?> beanClass;
    private final Object bean;

    /**
     * 构造
     *
     * @param beanClass Bean类
     * @param params    构造Bean所需要的参数
     */
    public DynamicBean(Class<?> beanClass, Object... params) {
        this(ReflectKit.newInstance(beanClass, params));
    }

    /**
     * 构造
     *
     * @param bean 原始Bean
     */
    public DynamicBean(Object bean) {
        Assert.notNull(bean);
        if (bean instanceof DynamicBean) {
            bean = ((DynamicBean) bean).getBean();
        }
        this.bean = bean;
        this.beanClass = ClassKit.getClass(bean);
    }

    /**
     * 构造
     *
     * @param beanClass Bean类
     */
    public DynamicBean(Class<?> beanClass) {
        this(ReflectKit.newInstance(beanClass));
    }

    /**
     * 创建一个{@link DynamicBean}
     *
     * @param bean 普通Bean
     * @return {@link DynamicBean}
     */
    public static DynamicBean create(Object bean) {
        return new DynamicBean(bean);
    }

    /**
     * 创建一个{@link DynamicBean}
     *
     * @param beanClass Bean类
     * @return {@link DynamicBean}
     */
    public static DynamicBean create(Class<?> beanClass) {
        return new DynamicBean(beanClass);
    }

    /**
     * 创建一个{@link DynamicBean}
     *
     * @param beanClass Bean类
     * @param params    构造Bean所需要的参数
     * @return {@link DynamicBean}
     */
    public static DynamicBean create(Class<?> beanClass, Object... params) {
        return new DynamicBean(beanClass, params);
    }

    /**
     * 执行原始Bean中的方法
     *
     * @param methodName 方法名
     * @param params     参数
     * @return 执行结果, 可能为null
     */
    public Object invoke(String methodName, Object... params) {
        return ReflectKit.invoke(this.bean, methodName, params);
    }

    /**
     * 获得字段对应值
     *
     * @param <T>       属性值类型
     * @param fieldName 字段名
     * @return 字段值
     * @throws InstrumentException 反射获取属性值或字段值导致的异常
     */
    public <T> T get(String fieldName) throws InstrumentException {
        if (Map.class.isAssignableFrom(beanClass)) {
            return (T) ((Map<?, ?>) bean).get(fieldName);
        } else {
            final PropertyDesc prop = BeanKit.getBeanDesc(beanClass).getProp(fieldName);
            if (null == prop) {
                throw new InstrumentException("No public field or get method for {}", fieldName);
            }
            return (T) prop.getValue(bean);
        }
    }

    /**
     * 设置字段值
     *
     * @param fieldName 字段名
     * @param value     字段值
     * @throws InstrumentException 反射获取属性值或字段值导致的异常
     */
    public void set(String fieldName, Object value) throws InstrumentException {
        if (Map.class.isAssignableFrom(beanClass)) {
            ((Map) bean).put(fieldName, value);
        } else {
            final PropertyDesc prop = BeanKit.getBeanDesc(beanClass).getProp(fieldName);
            if (null == prop) {
                throw new InstrumentException("No public field or set method for {}", fieldName);
            }
            prop.setValue(bean, value);
        }
    }

    /**
     * 检查是否有指定名称的bean属性
     *
     * @param fieldName 字段名
     * @return 是否有bean属性
     */
    public boolean contains(String fieldName) {
        return null != BeanKit.getBeanDesc(beanClass).getProp(fieldName);
    }

    /**
     * 获得原始Bean
     *
     * @param <T> Bean类型
     * @return bean
     */
    public <T> T getBean() {
        return (T) this.bean;
    }

    /**
     * 获得Bean的类型
     *
     * @param <T> Bean类型
     * @return Bean类型
     */
    public <T> Class<T> getBeanClass() {
        return (Class<T>) this.beanClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((null == bean) ? 0 : bean.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DynamicBean other = (DynamicBean) obj;
        if (null == bean) {
            return null == other.bean;
        } else {
            return bean.equals(other.bean);
        }
    }

    @Override
    public String toString() {
        return this.bean.toString();
    }

}
