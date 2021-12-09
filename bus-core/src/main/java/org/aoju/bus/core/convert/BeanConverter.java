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
package org.aoju.bus.core.convert;

import org.aoju.bus.core.beans.copier.BeanCopier;
import org.aoju.bus.core.beans.copier.CopyOptions;
import org.aoju.bus.core.beans.copier.ValueProvider;
import org.aoju.bus.core.map.MapProxy;
import org.aoju.bus.core.toolkit.BeanKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.ReflectKit;
import org.aoju.bus.core.toolkit.TypeKit;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Bean转换器,支持：
 * <pre>
 * Map = Bean
 * Bean = Bean
 * ValueProvider = Bean
 * </pre>
 *
 * @param <T> Bean类型
 * @author Kimi Liu
 * @version 6.3.2
 * @since JDK 1.8+
 */
public class BeanConverter<T> extends AbstractConverter<T> {

    private static final long serialVersionUID = 1L;

    private final Type beanType;
    private final Class<T> beanClass;
    private final CopyOptions copyOptions;

    /**
     * 构造，默认转换选项，注入失败的字段忽略
     *
     * @param beanType 转换成的目标Bean类型
     */
    public BeanConverter(Type beanType) {
        this(beanType, CopyOptions.create().setIgnoreError(true));
    }

    /**
     * 构造，默认转换选项，注入失败的字段忽略
     *
     * @param beanClass 转换成的目标Bean类
     */
    public BeanConverter(Class<T> beanClass) {
        this(beanClass, CopyOptions.create().setIgnoreError(true));
    }

    /**
     * 构造
     *
     * @param beanType    转换成的目标Bean类
     * @param copyOptions Bean转换选项参数
     */
    public BeanConverter(Type beanType, CopyOptions copyOptions) {
        this.beanType = beanType;
        this.beanClass = (Class<T>) TypeKit.getClass(beanType);
        this.copyOptions = copyOptions;
    }

    @Override
    protected T convertInternal(Object value) {
        if (value instanceof Map || value instanceof ValueProvider || BeanKit.isBean(value.getClass())) {
            if (value instanceof Map && this.beanClass.isInterface()) {
                // 将Map动态代理为Bean
                return MapProxy.create((Map<?, ?>) value).toProxyBean(this.beanClass);
            }
            // 限定被转换对象类型
            return BeanCopier.create(value, ReflectKit.newInstanceIfPossible(this.beanClass), this.beanType, this.copyOptions).copy();
        } else if (value instanceof byte[]) {
            // 尝试反序列化
            return ObjectKit.deserialize((byte[]) value);
        }
        return null;
    }

    @Override
    public Class<T> getTargetType() {
        return this.beanClass;
    }

}
