/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.bean.copier.provider;

import java.lang.reflect.Type;

import org.miaixz.bus.core.bean.copier.ValueProvider;
import org.miaixz.bus.core.bean.desc.BeanDesc;
import org.miaixz.bus.core.bean.desc.PropDesc;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.xyz.BeanKit;

/**
 * A {@link ValueProvider} implementation that retrieves values from a Bean object. This provider uses {@link BeanDesc}
 * to access properties of the bean.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanValueProvider implements ValueProvider<String> {

    /**
     * The bean object from which values are retrieved.
     */
    private final Object bean;
    /**
     * The {@link BeanDesc} describing the properties of the bean.
     */
    private final BeanDesc beanDesc;

    /**
     * Constructs a new {@code BeanValueProvider} for the given bean. It uses the default {@link BeanDesc} obtained from
     * {@link BeanKit#getBeanDesc(Class)}.
     *
     * @param bean The bean object.
     */
    public BeanValueProvider(final Object bean) {
        this(bean, null);
    }

    /**
     * Constructs a new {@code BeanValueProvider} for the given bean with a custom {@link BeanDesc}. If {@code beanDesc}
     * is {@code null}, the default {@link BeanDesc} will be used.
     *
     * @param bean     The bean object.
     * @param beanDesc A custom {@link BeanDesc} for the bean, or {@code null} to use the default.
     */
    public BeanValueProvider(final Object bean, BeanDesc beanDesc) {
        this.bean = bean;
        if (null == beanDesc) {
            beanDesc = BeanKit.getBeanDesc(bean.getClass());
        }
        this.beanDesc = beanDesc;
    }

    /**
     * Retrieves the value of a property from the bean based on its key (field name). The value is converted to the
     * specified {@code valueType} if necessary.
     *
     * @param key       The name of the property (field name) in the Bean object.
     * @param valueType The type to which the retrieved value should be converted.
     * @return The value of the property, converted to {@code valueType}, or {@code null} if the property does not
     *         exist.
     */
    @Override
    public Object value(final String key, final Type valueType) {
        final PropDesc prop = beanDesc.getProp(key);
        if (null != prop) {
            return Convert.convert(valueType, prop.getValue(bean, false));
        }
        return null;
    }

    /**
     * Checks if the bean contains a property with the specified key (field name).
     *
     * @param key The name of the property (field name) in the Bean object.
     * @return {@code true} if the bean contains the property, {@code false} otherwise.
     */
    @Override
    public boolean containsKey(final String key) {
        return null != beanDesc.getProp(key);
    }

}
