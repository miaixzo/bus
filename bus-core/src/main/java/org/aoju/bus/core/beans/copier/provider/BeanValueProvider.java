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
package org.aoju.bus.core.beans.copier.provider;

import org.aoju.bus.core.beans.PropertyDescription;
import org.aoju.bus.core.beans.copier.ValueProvider;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.BeanKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Bean的值提供者
 *
 * @author Kimi Liu
 * @version 6.2.2
 * @since JDK 1.8+
 */
public class BeanValueProvider implements ValueProvider<String> {

    final Map<String, PropertyDescription> sourcePdMap;
    private final Object source;
    private final boolean ignoreError;

    /**
     * 构造
     *
     * @param bean        Bean
     * @param ignoreCase  是否忽略字段大小写
     * @param ignoreError 是否忽略字段值读取错误
     */
    public BeanValueProvider(Object bean, boolean ignoreCase, boolean ignoreError) {
        this.source = bean;
        this.ignoreError = ignoreError;
        sourcePdMap = BeanKit.getBeanDesc(source.getClass()).getPropMap(ignoreCase);
    }

    @Override
    public Object value(String key, Type valueType) {
        PropertyDescription sourcePd = sourcePdMap.get(key);
        if (null == sourcePd && (Boolean.class == valueType || boolean.class == valueType)) {
            // boolean类型字段字段名支持两种方式
            sourcePd = sourcePdMap.get(StringKit.upperFirstAndAddPre(key, Normal.IS));
        }

        Object result = null;
        if (null != sourcePd) {
            final Method getter = sourcePd.getGetter();
            if (null != getter) {
                try {
                    result = getter.invoke(source);
                } catch (Exception e) {
                    if (false == ignoreError) {
                        throw new InstrumentException("Inject [{}] error!", key);
                    }
                }
                // 尝试转换为目标类型，失败将返回原类型
                final Object convertValue = Convert.convertWithCheck(valueType, result, null, ignoreError);
                if (null != convertValue) {
                    result = convertValue;
                }
            }
        }
        return result;
    }

    @Override
    public boolean containsKey(String key) {
        return sourcePdMap.containsKey(key) || sourcePdMap.containsKey(StringKit.upperFirstAndAddPre(key, Normal.IS));
    }

}
