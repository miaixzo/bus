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
package org.miaixz.bus.core.convert;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

import org.miaixz.bus.core.convert.stringer.BlobStringer;
import org.miaixz.bus.core.convert.stringer.ClobStringer;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.XmlKit;

/**
 * 字符串转换器，提供各种对象转换为字符串的逻辑封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringConverter extends AbstractConverter {

    private static final long serialVersionUID = -1L;

    private Map<Class<?>, Function<Object, String>> stringer;

    /**
     * 加入自定义对象类型的toString规则
     *
     * @param clazz          类型
     * @param stringFunction 序列化函数
     * @return this
     */
    public StringConverter putStringer(final Class<?> clazz, final Function<Object, String> stringFunction) {
        if (null == stringer) {
            stringer = new HashMap<>();
        }
        stringer.put(clazz, stringFunction);
        return this;
    }

    @Override
    protected String convertInternal(final Class<?> targetClass, final Object value) {
        // 自定义toString
        if (MapKit.isNotEmpty(stringer)) {
            final Function<Object, String> stringFunction = stringer.get(targetClass);
            if (null != stringFunction) {
                return stringFunction.apply(value);
            }
        }

        if (value instanceof TimeZone) {
            return ((TimeZone) value).getID();
        } else if (value instanceof org.w3c.dom.Node) {
            return XmlKit.toString((org.w3c.dom.Node) value);
        } else if (value instanceof java.sql.Clob) {
            return ClobStringer.INSTANCE.apply(value);
        } else if (value instanceof java.sql.Blob) {
            return BlobStringer.INSTANCE.apply(value);
        } else if (value instanceof Type) {
            return ((Type) value).getTypeName();
        }

        // 其它情况
        return convertToString(value);
    }

}
