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
package org.aoju.bus.core.beans.copier.provider;

import org.aoju.bus.core.beans.copier.ValueProvider;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.map.CaseInsensitiveMap;
import org.aoju.bus.core.utils.StringUtils;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Map值提供者
 *
 * @author Kimi Liu
 * @version 3.6.8
 * @since JDK 1.8+
 */
public class MapValueProvider implements ValueProvider<String> {

    private Map<?, ?> map;

    /**
     * 构造
     *
     * @param map        Map
     * @param ignoreCase 是否忽略key的大小写
     */
    public MapValueProvider(Map<?, ?> map, boolean ignoreCase) {
        if (false == ignoreCase || map instanceof CaseInsensitiveMap) {
            //不忽略大小写或者提供的Map本身为CaseInsensitiveMap则无需转换
            this.map = map;
        } else {
            //转换为大小写不敏感的Map
            this.map = new CaseInsensitiveMap<>(map);
        }
    }

    @Override
    public Object value(String key, Type valueType) {
        Object value = map.get(key);
        if (null == value) {
            //检查下划线模式
            value = map.get(StringUtils.toUnderlineCase(key));
        }
        return Convert.convert(valueType, value);
    }

    @Override
    public boolean containsKey(String key) {
        //检查下划线模式
        if (map.containsKey(key)) {
            return true;
        } else return map.containsKey(StringUtils.toUnderlineCase(key));
    }

}
