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
package org.miaixz.bus.core.bean;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.bean.desc.PropDesc;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.BeanException;
import org.miaixz.bus.core.lang.exception.CloneException;
import org.miaixz.bus.core.xyz.*;

/**
 * 动态Bean，通过反射对Bean的相关方法做操作 支持Map和普通Bean和Collection
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DynaBean implements Cloneable, Serializable {

    @Serial
    private static final long serialVersionUID = 2852235609071L;

    /**
     * bean类
     */
    private final Class<?> beanClass;
    /**
     * bean对象
     */
    private Object bean;

    /**
     * 构造
     *
     * @param bean 原始Bean
     */
    public DynaBean(final Object bean) {
        Assert.notNull(bean);
        if (bean instanceof DynaBean) {
            // 已经是动态Bean，则提取对象
            this.bean = ((DynaBean) bean).getBean();
            this.beanClass = ((DynaBean) bean).getBeanClass();
        } else if (bean instanceof Class) {
            // 用户传入类，默认按照此类的默认实例对待
            this.bean = ReflectKit.newInstance((Class<?>) bean);
            this.beanClass = (Class<?>) bean;
        } else {
            // 普通Bean
            this.bean = bean;
            this.beanClass = ClassKit.getClass(bean);
        }
    }

    /**
     * 创建一个DynaBean
     *
     * @param beanClass Bean类
     * @param params    构造Bean所需要的参数
     * @return DynaBean
     */
    public static DynaBean of(final Class<?> beanClass, final Object... params) {
        return of(ReflectKit.newInstance(beanClass, params));
    }

    /**
     * 创建一个DynaBean
     *
     * @param bean 普通Bean
     * @return DynaBean
     */
    public static DynaBean of(final Object bean) {
        return new DynaBean(bean);
    }

    /**
     * 获得字段对应值
     *
     * @param <T>       属性值类型
     * @param fieldName 字段名
     * @return 字段值
     * @throws BeanException 反射获取属性值或字段值导致的异常
     */
    public <T> T get(final String fieldName) throws BeanException {
        if (Map.class.isAssignableFrom(beanClass)) {
            return (T) ((Map<?, ?>) bean).get(fieldName);
        } else if (bean instanceof Collection) {
            try {
                return (T) CollKit.get((Collection<?>) bean, Integer.parseInt(fieldName));
            } catch (final NumberFormatException e) {
                // 非数字
                return (T) CollKit.map((Collection<?>) bean, (beanEle) -> DynaBean.of(beanEle).get(fieldName), false);
            }
        } else if (ArrayKit.isArray(bean)) {
            try {
                return ArrayKit.get(bean, Integer.parseInt(fieldName));
            } catch (final NumberFormatException e) {
                // 非数字
                return (T) ArrayKit.map(bean, Object.class, (beanEle) -> DynaBean.of(beanEle).get(fieldName));
            }
        } else {
            final PropDesc prop = BeanKit.getBeanDesc(beanClass).getProp(fieldName);
            if (null == prop) {
                // 节点字段不存在，类似于Map无key，返回null而非报错
                return null;
            }
            return (T) prop.getValue(bean, false);
        }
    }

    /**
     * 检查是否有指定名称的bean属性
     *
     * @param fieldName 字段名
     * @return 是否有bean属性
     */
    public boolean containsProp(final String fieldName) {
        if (Map.class.isAssignableFrom(beanClass)) {
            return ((Map<?, ?>) bean).containsKey(fieldName);
        } else if (bean instanceof Collection) {
            return CollKit.size(bean) > Integer.parseInt(fieldName);
        } else {
            return null != BeanKit.getBeanDesc(beanClass).getProp(fieldName);
        }
    }

    /**
     * 获得字段对应值，获取异常返回{@code null}
     *
     * @param <T>       属性值类型
     * @param fieldName 字段名
     * @return 字段值
     */
    public <T> T safeGet(final String fieldName) {
        try {
            return get(fieldName);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * 设置字段值
     *
     * @param fieldName 字段名
     * @param value     字段值
     * @return this;
     * @throws BeanException 反射获取属性值或字段值导致的异常
     */
    public DynaBean set(final String fieldName, final Object value) throws BeanException {
        if (Map.class.isAssignableFrom(beanClass)) {
            ((Map) bean).put(fieldName, value);
        } else if (bean instanceof List) {
            ListKit.setOrPadding((List) bean, Convert.toInt(fieldName), value);
        } else if (ArrayKit.isArray(bean)) {
            // 追加产生新数组，此处返回新数组
            this.bean = ArrayKit.setOrPadding(bean, Convert.toInt(fieldName), value);
        } else {
            final PropDesc prop = BeanKit.getBeanDesc(beanClass).getProp(fieldName);
            if (null == prop) {
                throw new BeanException("No public field or set method for '{}'", fieldName);
            }

            prop.setValue(bean, value, false, false);
        }
        return this;
    }

    /**
     * 执行原始Bean中的方法
     *
     * @param methodName 方法名
     * @param params     参数
     * @return 执行结果，可能为null
     */
    public Object invoke(final String methodName, final Object... params) {
        return MethodKit.invoke(this.bean, methodName, params);
    }

    /**
     * 获得原始Bean
     *
     * @param <T> Bean类型
     * @return beans
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
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final DynaBean other = (DynaBean) object;
        if (bean == null) {
            return other.bean == null;
        } else
            return bean.equals(other.bean);
    }

    @Override
    public String toString() {
        return this.bean.toString();
    }

    @Override
    public DynaBean clone() {
        try {
            return (DynaBean) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new CloneException(e);
        }
    }

}
