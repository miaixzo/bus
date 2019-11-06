/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.beans;

import org.aoju.bus.core.clone.Support;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.BeanUtils;
import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.ReflectUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 动态Bean，通过反射对Bean的相关方法做操作
 * 支持Map和普通Bean
 *
 * @author Kimi Liu
 * @version 5.1.0
 * @since JDK 1.8+
 */
public class DynaBean extends Support<DynaBean> implements Serializable {

    private static final long serialVersionUID = 1197818330017827323L;

    private Class<?> beanClass;
    private Object bean;

    /**
     * 构造
     *
     * @param beanClass Bean类
     * @param params    构造Bean所需要的参数
     */
    public DynaBean(Class<?> beanClass, Object... params) {
        this(ReflectUtils.newInstance(beanClass, params));
    }

    /**
     * 构造
     *
     * @param bean 原始Bean
     */
    public DynaBean(Object bean) {
        Assert.notNull(bean);
        if (bean instanceof DynaBean) {
            bean = ((DynaBean) bean).getBean();
        }
        this.bean = bean;
        this.beanClass = ClassUtils.getClass(bean);
    }

    /**
     * 创建一个{@link DynaBean}
     *
     * @param bean 普通Bean
     * @return {@link DynaBean}
     */
    public static DynaBean create(Object bean) {
        return new DynaBean(bean);
    }

    /**
     * 创建一个{@link DynaBean}
     *
     * @param beanClass Bean类
     * @param params    构造Bean所需要的参数
     * @return {@link DynaBean}
     */
    public static DynaBean create(Class<?> beanClass, Object... params) {
        return new DynaBean(beanClass, params);
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
            try {
                final Method method = BeanUtils.getBeanDesc(beanClass).getGetter(fieldName);
                if (null == method) {
                    throw new InstrumentException("No get method for {}", fieldName);
                }
                return (T) method.invoke(this.bean);
            } catch (Exception e) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 获得字段对应值，获取异常返回{@code null}
     *
     * @param <T>       属性值类型
     * @param fieldName 字段名
     * @return 字段值
     * @since 3.1.1
     */
    public <T> T safeGet(String fieldName) {
        try {
            return get(fieldName);
        } catch (Exception e) {
            return null;
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
            return;
        } else {
            try {
                final Method setter = BeanUtils.getBeanDesc(beanClass).getSetter(fieldName);
                if (null == setter) {
                    throw new InstrumentException("No set method for {}", fieldName);
                }
                setter.invoke(this.bean, value);
            } catch (Exception e) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 执行原始Bean中的方法
     *
     * @param methodName 方法名
     * @param params     参数
     * @return 执行结果，可能为null
     */
    public Object invoke(String methodName, Object... params) {
        return ReflectUtils.invoke(this.bean, methodName, params);
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
        result = prime * result + ((bean == null) ? 0 : bean.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DynaBean other = (DynaBean) obj;
        if (bean == null) {
            return other.bean == null;
        } else return bean.equals(other.bean);
    }

    @Override
    public String toString() {
        return this.bean.toString();
    }

}
