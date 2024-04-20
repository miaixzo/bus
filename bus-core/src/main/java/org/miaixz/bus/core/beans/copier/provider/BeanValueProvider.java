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
package org.miaixz.bus.core.beans.copier.provider;

import org.miaixz.bus.core.beans.PropertyDesc;
import org.miaixz.bus.core.beans.copier.ValueProvider;
import org.miaixz.bus.core.lang.Editor;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.map.FuncKeyMap;
import org.miaixz.bus.core.toolkit.BeanKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Bean的值提供者
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BeanValueProvider implements ValueProvider<String> {

    final Map<String, PropertyDesc> sourcePdMap;
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
        this(bean, ignoreCase, ignoreError, null);
    }

    /**
     * 构造
     *
     * @param bean        Bean
     * @param ignoreCase  是否忽略字段大小写
     * @param ignoreError 是否忽略字段值读取错误
     * @param keyEditor   键编辑器
     */
    public BeanValueProvider(Object bean, boolean ignoreCase, boolean ignoreError, Editor<String> keyEditor) {
        this.source = bean;
        this.ignoreError = ignoreError;
        final Map<String, PropertyDesc> sourcePdMap = BeanKit.getBeanDesc(source.getClass()).getPropMap(ignoreCase);
        // 如果用户定义了键编辑器，则提供的map中的数据必须全部转换key
        this.sourcePdMap = new FuncKeyMap<>(new HashMap<>(sourcePdMap.size(), 1), (key) -> {
            if (ignoreCase && key instanceof CharSequence) {
                key = key.toString().toLowerCase();
            }
            if (null != keyEditor) {
                key = keyEditor.edit(key.toString());
            }
            return key.toString();
        });
        this.sourcePdMap.putAll(sourcePdMap);
    }

    @Override
    public Object value(String key, Type valueType) {
        final PropertyDesc sourcePd = getPropertyDesc(key, valueType);
        Object result = null;
        if (null != sourcePd) {
            result = sourcePd.getValue(this.source, valueType, this.ignoreError);
        }
        return result;
    }

    @Override
    public boolean containsKey(String key) {
        final PropertyDesc sourcePd = getPropertyDesc(key, null);
        // 字段描述不存在或忽略读的情况下，表示不存在
        return null != sourcePd && sourcePd.isReadable(false);
    }

    /**
     * 获得属性描述
     *
     * @param key       字段名
     * @param valueType 值类型，用于判断是否为Boolean，可以为null
     * @return 属性描述
     */
    private PropertyDesc getPropertyDesc(String key, Type valueType) {
        PropertyDesc sourcePd = sourcePdMap.get(key);
        if (null == sourcePd && (null == valueType || Boolean.class == valueType || boolean.class == valueType)) {
            // boolean类型字段字段名支持两种方式
            sourcePd = sourcePdMap.get(StringKit.upperFirstAndAddPre(key, Normal.IS));
        }

        return sourcePd;
    }

}
