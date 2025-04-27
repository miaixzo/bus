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
package org.miaixz.bus.core.bean.copier.provider;

import java.lang.reflect.Type;

import org.miaixz.bus.core.bean.copier.ValueProvider;
import org.miaixz.bus.core.bean.desc.BeanDesc;
import org.miaixz.bus.core.bean.desc.PropDesc;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.xyz.BeanKit;

/**
 * Bean值提供器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanValueProvider implements ValueProvider<String> {

    private final Object bean;
    private final BeanDesc beanDesc;

    /**
     * 构造
     *
     * @param bean Bean
     */
    public BeanValueProvider(final Object bean) {
        this(bean, null);
    }

    /**
     * 构造
     *
     * @param bean     Bean
     * @param beanDesc 自定义的{@link BeanDesc}，默认为{@link BeanKit#getBeanDesc(Class)}
     */
    public BeanValueProvider(final Object bean, BeanDesc beanDesc) {
        this.bean = bean;
        if (null == beanDesc) {
            beanDesc = BeanKit.getBeanDesc(bean.getClass());
        }
        this.beanDesc = beanDesc;
    }

    @Override
    public Object value(final String key, final Type valueType) {
        final PropDesc prop = beanDesc.getProp(key);
        if (null != prop) {
            return Convert.convert(valueType, prop.getValue(bean, false));
        }
        return null;
    }

    @Override
    public boolean containsKey(final String key) {
        return null != beanDesc.getProp(key);
    }

}
