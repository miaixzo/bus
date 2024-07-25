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
package org.miaixz.bus.core.bean.path.node;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 列表节点
 * [num0,num1,num2...]模式或者['key0','key1']模式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ListNode implements Node {

    final List<String> names;

    /**
     * 列表节点
     *
     * @param expression 表达式
     */
    public ListNode(final String expression) {
        this.names = CharsBacker.splitTrim(expression, Symbol.COMMA);
    }

    @Override
    public Object getValue(final Object bean) {
        final List<String> names = this.names;

        if (bean instanceof Collection) {
            return CollKit.getAny((Collection<?>) bean, Convert.convert(int[].class, names));
        } else if (ArrayKit.isArray(bean)) {
            return ArrayKit.getAny(bean, Convert.convert(int[].class, names));
        } else {
            final String[] unWrappedNames = getUnWrappedNames(names);
            if (bean instanceof Map) {
                // 只支持String为key的Map
                return MapKit.getAny((Map<String, ?>) bean, unWrappedNames);
            } else {
                final Map<String, Object> map = BeanKit.beanToMap(bean);
                return MapKit.getAny(map, unWrappedNames);
            }
        }
    }

    @Override
    public Object setValue(final Object bean, final Object value) {
        throw new UnsupportedOperationException("Can not set value to multi names.");
    }

    @Override
    public String toString() {
        return this.names.toString();
    }

    /**
     * 将列表中的name，去除单引号
     *
     * @param names name列表
     * @return 处理后的name列表
     */
    private String[] getUnWrappedNames(final List<String> names) {
        final String[] unWrappedNames = new String[names.size()];
        for (int i = 0; i < unWrappedNames.length; i++) {
            unWrappedNames[i] = StringKit.unWrap(names.get(i), Symbol.C_SINGLE_QUOTE);
        }

        return unWrappedNames;
    }

}
