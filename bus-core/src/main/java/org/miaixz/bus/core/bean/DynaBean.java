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
 * Dynamic Bean, which operates on Bean-related methods through reflection. Supports Map, plain Bean, and Collection
 * types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DynaBean implements Cloneable, Serializable {

    @Serial
    private static final long serialVersionUID = 2852291223632L;

    /**
     * The class of the bean.
     */
    private final Class<?> beanClass;
    /**
     * The actual bean object.
     */
    private Object bean;

    /**
     * Constructs a new {@code DynaBean} instance.
     *
     * @param bean The original bean object. Can be a Map, a plain Java Bean, or a Class. If it's a {@code DynaBean},
     *             its wrapped bean is extracted. If it's a {@code Class}, a new instance of that class is created.
     */
    public DynaBean(final Object bean) {
        Assert.notNull(bean);
        if (bean instanceof DynaBean) {
            // If it's already a DynaBean, extract the wrapped object.
            this.bean = ((DynaBean) bean).getBean();
            this.beanClass = ((DynaBean) bean).getBeanClass();
        } else if (bean instanceof Class) {
            // If a Class is provided, treat it as a default instance of this class.
            this.bean = ReflectKit.newInstance((Class<?>) bean);
            this.beanClass = (Class<?>) bean;
        } else {
            // Regular bean.
            this.bean = bean;
            this.beanClass = ClassKit.getClass(bean);
        }
    }

    /**
     * Creates a {@code DynaBean} from a given class, instantiating it with provided arguments.
     *
     * @param beanClass The class of the bean to create.
     * @param args      Arguments required for constructing the bean.
     * @return A new {@code DynaBean} instance.
     */
    public static DynaBean of(final Class<?> beanClass, final Object... args) {
        return of(ReflectKit.newInstance(beanClass, args));
    }

    /**
     * Creates a {@code DynaBean} from an existing object.
     *
     * @param bean The object to wrap as a {@code DynaBean}.
     * @return A new {@code DynaBean} instance.
     */
    public static DynaBean of(final Object bean) {
        return new DynaBean(bean);
    }

    /**
     * Retrieves the value of a field (property) from the wrapped bean. Supports Map, Collection (by index), Array (by
     * index), and standard Bean properties.
     *
     * @param <T>       The expected type of the field value.
     * @param fieldName The name of the field or property.
     * @return The value of the field.
     * @throws BeanException If an error occurs during reflection to get the property or field value.
     */
    public <T> T get(final String fieldName) throws BeanException {
        if (Map.class.isAssignableFrom(beanClass)) {
            return (T) ((Map<?, ?>) bean).get(fieldName);
        } else if (bean instanceof Collection) {
            try {
                return (T) CollKit.get((Collection<?>) bean, Integer.parseInt(fieldName));
            } catch (final NumberFormatException e) {
                // Not a number, treat as property name for elements in collection.
                return (T) CollKit.map((Collection<?>) bean, (beanEle) -> DynaBean.of(beanEle).get(fieldName), false);
            }
        } else if (ArrayKit.isArray(bean)) {
            try {
                return ArrayKit.get(bean, Integer.parseInt(fieldName));
            } catch (final NumberFormatException e) {
                // Not a number, treat as property name for elements in array.
                return (T) ArrayKit.map(bean, Object.class, (beanEle) -> DynaBean.of(beanEle).get(fieldName));
            }
        } else {
            final PropDesc prop = BeanKit.getBeanDesc(beanClass).getProp(fieldName);
            if (null == prop) {
                // If the node field does not exist, similar to a Map without a key, return null instead of throwing an
                // error.
                return null;
            }
            return (T) prop.getValue(bean, false);
        }
    }

    /**
     * Checks if the wrapped bean contains a property with the specified name.
     *
     * @param fieldName The name of the field or property.
     * @return {@code true} if the bean has the property, {@code false} otherwise.
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
     * Retrieves the value of a field (property) from the wrapped bean, returning {@code null} if an exception occurs
     * during retrieval.
     *
     * @param <T>       The expected type of the field value.
     * @param fieldName The name of the field or property.
     * @return The value of the field, or {@code null} if an error occurs.
     */
    public <T> T safeGet(final String fieldName) {
        try {
            return get(fieldName);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Sets the value of a field (property) in the wrapped bean. Supports Map, List (by index, with padding), and
     * standard Bean properties.
     *
     * @param fieldName The name of the field or property.
     * @param value     The value to set.
     * @return This {@code DynaBean} instance for chaining.
     * @throws BeanException If an error occurs during reflection to set the property or field value.
     */
    public DynaBean set(final String fieldName, final Object value) throws BeanException {
        if (Map.class.isAssignableFrom(beanClass)) {
            ((Map) bean).put(fieldName, value);
        } else if (bean instanceof List) {
            ListKit.setOrPadding((List) bean, Convert.toInt(fieldName), value);
        } else if (ArrayKit.isArray(bean)) {
            // Appending creates a new array, so the new array is returned here.
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
     * Invokes a method on the original wrapped bean.
     *
     * @param methodName The name of the method to invoke.
     * @param args       Arguments to pass to the method.
     * @return The result of the method invocation, which may be {@code null}.
     */
    public Object invoke(final String methodName, final Object... args) {
        return MethodKit.invoke(this.bean, methodName, args);
    }

    /**
     * Retrieves the original wrapped bean object.
     *
     * @param <T> The type of the bean.
     * @return The original bean object.
     */
    public <T> T getBean() {
        return (T) this.bean;
    }

    /**
     * Retrieves the class of the original wrapped bean.
     *
     * @param <T> The type of the bean.
     * @return The class of the bean.
     */
    public <T> Class<T> getBeanClass() {
        return (Class<T>) this.beanClass;
    }

    /**
     * Computes the hash code for this {@code DynaBean} based on its wrapped bean.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bean == null) ? 0 : bean.hashCode());
        return result;
    }

    /**
     * Compares this {@code DynaBean} to the specified object for equality. Two {@code DynaBean} instances are
     * considered equal if they wrap the same bean object.
     *
     * @param object The object to compare with.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
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

    /**
     * Returns a string representation of the wrapped bean.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return this.bean.toString();
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return A clone of this instance.
     * @throws CloneException If the object's class does not support the {@code Cloneable} interface.
     */
    @Override
    public DynaBean clone() {
        try {
            return (DynaBean) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new CloneException(e);
        }
    }

}
