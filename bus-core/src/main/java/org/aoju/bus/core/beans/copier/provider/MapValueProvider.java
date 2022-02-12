/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.beans.copier.ValueProvider;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.map.CaseInsensitiveMap;
import org.aoju.bus.core.toolkit.StringKit;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Map值提供者
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public class MapValueProvider implements ValueProvider<String> {

    private final Map<?, ?> map;

    private final boolean ignoreError;

    /**
     * 构造
     *
     * @param map        Map
     * @param ignoreCase 是否忽略key的大小写
     */
    public MapValueProvider(Map<?, ?> map, boolean ignoreCase) {
        this(map, ignoreCase, false);
    }

    /**
     * 构造
     *
     * @param map         Map
     * @param ignoreCase  是否忽略key的大小写
     * @param ignoreError 是否忽略错误
     */
    public MapValueProvider(Map<?, ?> map, boolean ignoreCase, boolean ignoreError) {
        if (false == ignoreCase || map instanceof CaseInsensitiveMap) {
            //不忽略大小写或者提供的Map本身为CaseInsensitiveMap则无需转换
            this.map = map;
        } else {
            //转换为大小写不敏感的Map
            this.map = new CaseInsensitiveMap<>(map);
        }
        this.ignoreError = ignoreError;
    }

    @Override
    public Object value(String key, Type valueType) {
        final String keys = getKey(key, valueType);
        if (null == keys) {
            return null;
        }
        return Convert.convertWithCheck(valueType, map.get(keys), null, this.ignoreError);
    }

    @Override
    public boolean containsKey(String key) {
        return null != getKey(key, null);
    }

    /**
     * 获得map中可能包含的key,不包含返回null
     *
     * @param key       map中可能包含的key
     * @param valueType 值类型，用于判断是否为Boolean，可以为null
     * @return map中可能包含的key
     */
    private String getKey(String key, Type valueType) {
        if (map.containsKey(key)) {
            return key;
        }

        //检查下划线模式
        String customKey = StringKit.toUnderlineCase(key);
        if (map.containsKey(customKey)) {
            return customKey;
        }

        //检查boolean类型
        if (null == valueType || Boolean.class == valueType || boolean.class == valueType) {
            //boolean类型字段字段名支持两种方式
            customKey = StringKit.upperFirstAndAddPre(key, "is");
            if (map.containsKey(customKey)) {
                return customKey;
            }

            //检查下划线模式
            customKey = StringKit.toUnderlineCase(customKey);
            if (map.containsKey(customKey)) {
                return customKey;
            }
        }
        return null;
    }

}
