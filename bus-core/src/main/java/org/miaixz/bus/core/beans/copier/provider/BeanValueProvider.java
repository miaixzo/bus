/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.beans.copier.provider;

import org.miaixz.bus.core.beans.BeanDesc;
import org.miaixz.bus.core.beans.PropDesc;
import org.miaixz.bus.core.beans.copier.ValueProvider;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.xyz.BeanKit;

import java.lang.reflect.Type;

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
        this.bean = bean;
        this.beanDesc = BeanKit.getBeanDesc(bean.getClass());
    }

    @Override
    public Object value(final String key, final Type valueType) {
        final PropDesc prop = beanDesc.getProp(key);
        if (null != prop) {
            return Convert.convert(valueType, prop.getValue(bean));
        }
        return null;
    }

    @Override
    public boolean containsKey(final String key) {
        return null != beanDesc.getProp(key);
    }

}
